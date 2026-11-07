# LP07 / NV01: reproduksi dua penulis save dan perbaikan commit

Tanggal: 3 November 2026, Asia/Jakarta. Lingkup tambahan ini menutup gate penyimpanan/lifecycle yang memang masih terbuka pada LP07. Tidak mengubah aturan ekonomi, UI, skema save atau data pemain.

**Checkpoint host terbaru:** pemeriksaan NV01 diperluas ke pemilik bridge yang sudah digantikan. Bukti gabungan terbaru ada di `logs/save-owner-host-verification.json`; bagian concurrency dan APK storage-fix di bawah tetap merupakan riwayat sebelum penguatan ini. Pengujian lifecycle Android dan sesi 60 menit belum selesai.

## Kegagalan aktual

Tes R04 sebelumnya memanggil dua save secara berurutan. Ia membuktikan penolakan versi lama jika versi baru sudah ada ketika disk dibaca, tetapi tidak membuktikan commit antar-instance yang bersaing.

`SaveWritersConcurrencyRegressionTest.twoInstancesCannotCommitOldStateAfterNewState` memakai dua instance `SaveManager`, dua thread dan latch. Penulis lama dihentikan saat memvalidasi byte primary yang sudah dibacanya. Penulis baru kemudian mencoba menyimpan snapshot dengan `savedTimestamp` lebih tinggi. Pada source sebelum perbaikan, penulis baru dapat selesai; penulis lama melanjutkan pemeriksaan terhadap byte lama yang sudah dibacanya lalu menimpa hasil baru.

Bukti reproduksi `logs/save-writers-reproduction/result.log`: **1 test, 1 failure**. Assertion akhir mengharapkan `1793700686440`, tetapi disk berisi `1793700686439`. Ini hasil aktual pada direktori QA baru, bukan edit terhadap save pemain atau klaim dari pembacaan source.

## Perbaikan minimum

`SaveManager.save` tetap menjadi titik tunggal penulisan. Monitor `SaveManager.class` mengikat pembacaan primary, pemeriksaan versi, rotasi backup dan atomic replacement sebagai satu commit lintas-instance. Serialisasi serta penulisan ke temp unik dilakukan sebelum monitor; seluruh temp berada dalam `finally` agar penolakan versi atau kegagalan sebelumnya juga dibersihkan.

Monitor bersifat satu proses dan global untuk instance SaveManager: direktori yang berbeda ikut menunggu commit. Ini cukup untuk proses aplikasi Android saat ini, yang tidak mendeklarasikan proses penulis lain. Komentar `ponytail:` mencatat batas tersebut dan jalur per-path lock bila banyak direktori benar-benar berebut. Tidak menambah registry lock, engine, dependency atau skema generasi baru yang spekulatif. Koordinasi antarproses terpisah dan perubahan jam sistem belum diluluskan oleh tes ini.

Tes sesudah perbaikan mempertahankan assertion snapshot terbaru, memeriksa kedua thread berakhir, dan memeriksa penolakan save lama tidak meninggalkan `.tmp`. Ketika monitor bersama menahan penulis kedua, latch melepas penulis lama tanpa deadlock, lalu versi baru harus tetap menjadi hasil akhir. Assertion hasil tidak diganti atau dilonggarkan agar tes lulus.

Runner dan gerbang wajib kini memasukkan `SaveWritersConcurrencyRegressionTest`. `quality_gate.py run` pada `logs/gate-1793700919610934500/gate.json` exit 0: **91 tes pada 19 suite terpilih** dan pemeriksaan browser lulus. File `SaveManager.java`, tes dan runner memiliki identitas pada manifest gate tersebut. Perubahan setelah snapshot ini memerlukan gate ulang.

## Artefak dan sesi perangkat

- APK QA perbaikan: `Artifacts/Codex-LP-Verification/device-storage-fix-2026-11-03/GameSaham-QA-debug.apk`.
- SHA-256 QA: `AB89FCE983D50D8534A2D6ED90BB3BD18F1CB97A01A7529C825695B8CA9BFEA8`.
- APK produksi perbaikan: `Artifacts/Codex-LP-Verification/device-storage-fix-2026-11-03/GameSaham-debug.apk`.
- SHA-256 produksi: `F0EE2457B0B7308C32D8C455ED5BBE14AEBF0F2B723758C8CE8C2D9DB8AB5721`.
- Seluruh 427 anggota assets/DEX dibandingkan identik antara pasangan QA/produksi. Bukti `logs/device-session-2026-11-03/storage-fix-equivalence.json`.
- APK terpasang pada perangkat ditarik dari lokasi paket publik dan hash-nya cocok dengan QA perbaikan. Game utama tidak diperbarui. Bukti `installed-final-identity.json` pada direktori sesi.

Baseline awal `run-1793699769975913700` dihentikan sekitar 18 menit setelah bug host terkonfirmasi; statusnya `INTERRUPTED_FOR_CONFIRMED_STORAGE_FIX`. Sampel tetap disimpan, **tidak dihitung sebagai sesi ketahanan selesai**.

Sesi 60 menit sesudah perbaikan memakai `run-1793701001700954400`. Status dan hasil harus dibaca dari `session.json` aktual; keberadaan folder atau dokumen ini tidak membuktikan sesi selesai. Android background/resume dan force-stop masih memerlukan hasil aktual pada akhir sesi. Tidak menganggap kelulusan concurrency host sebagai bukti otomatis seluruh lifecycle Android.

## Pendalaman host: pemilik lama dengan timestamp lebih baru

Monitor commit dan pemeriksaan timestamp saja belum cukup: `TradingFacade.save()` membuat timestamp dari waktu panggilan sekarang. Facade milik Activity lama bisa memanggil save terlambat dengan keadaan domain yang tertinggal, tetapi timestamp lebih tinggi daripada save Activity baru. Pemeriksaan epoch pada worker juga belum menutup jeda antara pemeriksaan tersebut dan commit storage.

Reproduksi `supersededBridgeCannotSaveEvenWithALaterWallTimestamp` membuat dua facade/SaveManager pada direktori QA yang sama dan dua bridge sebenarnya. Setelah facade baru menyimpan nama `QA Current`, facade lama mencoba menyimpan lagi. Source sebelum penguatan gagal mempertahankan byte primary: `logs/save-owner-reproduction/before.log`, **2 tes / 1 kegagalan**. Ini reproduksi host terhadap panggilan terlambat dari pemilik lama; bukan klaim bahwa interleaving Activity Android sudah direproduksi di Poco.

Perbaikan tetap pada titik commit `SaveManager.save`: cek otoritas pemilik sebelum membaca/merotasi primary atau backup. Bridge memakai epoch existing sebagai otoritasnya. Aktivasi epoch baru memakai monitor commit yang sama, sehingga tidak memotong commit yang sudah memegang monitor. Di aplikasi Android saat ini, bridge dibuat sebelum startup/load worker dijalankan. Pemeriksaan timestamp existing tetap dipertahankan sebagai perlindungan terpisah.

Otoritas tidak dicabut hanya karena `destroy()` dipanggil: save terakhir yang sudah melewati guard worker masih boleh selesai selama belum ada pemilik pengganti. `destroyed` menjadi volatile agar guard worker membaca perubahan lifecycle lintas-thread. Cakupan masih satu proses/satu game aktif sesuai arsitektur existing; tidak menambah skema save atau koordinasi lintas proses.

Suite yang sama kini memuat **3 tes**: dua penulis concurrent, pemilik yang sudah digantikan, dan save terakhir dari bridge yang ditutup tanpa pengganti. Assertion sebelumnya dipertahankan; tambahan memeriksa penolakan khusus pemilik lama, byte primary/backup tidak berubah dan temp tidak bocor. Tidak mengganti expected value agar source lulus. Ketiganya lulus dalam gerbang host terbaru bersama 93 tes / 19 suite terpilih serta 23 kelompok browser. Bukti dan exit pemeriksaan regression/full ada di `logs/save-owner-host-verification.json`.

Pasangan APK terbaru berada di `Artifacts/Codex-LP-Verification/device-save-owner-fix-2026-11-03/`: produksi `6D29B1E2CD0844F0F328C168326B00EC751E6C7D91B73FBA8D8CF58CAA0D2C7D`, QA `50254CBBC21A24833B11CD4BF002A31E4D232E2B8F465DC3D51DD94CA0CC90E5`. 427 anggota assets/DEX identik dan kedua tanda tangan diverifikasi. **Belum dipasang atau diuji di ponsel.**
