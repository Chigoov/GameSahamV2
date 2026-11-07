# Temuan nyata pengujian Poco X8 Pro dan keadaan pekerjaan terbaru

**Checkpoint LP10 terbaru:** pesan gagal muat daftar saham dan Coba lagi sudah disetujui dan diterapkan, diuji host dan dibangun. Paket untuk pengujian berikutnya adalah `Artifacts/Codex-LP-Verification/lp10-market-retry-2026-11-03/GameSaham-QA-debug.apk`, SHA256 `3fafd98668c12d6b8afe987c7fc37063369b769a9d7a5a35524837c08a6de806`. Bukti `logs/lp10-host-verification.json`; belum dipasang. Saat perangkat tersedia, pakai argumen --apk eksplisit pada runner device_session.py untuk paket ini, jangan default ke paket lama. Seluruh keterangan paket LP09/menu-fix/storage-fix di bawah tetap riwayat sesuai waktunya; tidak ada ADB atau aktivitas ponsel baru pada LP10.

**Klarifikasi LP09 terkini:** pemilik menunjuk tabel statistik Open/Prev/Lot/High/ARA/Val/Low/ARB/Avg di bawah chart, serta tanggal/bulan yang berkedip. Cakupan berita di checkpoint bawah dibatalkan dan daftar transaksi proposal tidak diterapkan. Rujukan aktual: [LP09-TABEL-STATISTIK-DAN-TANGGAL.md](LP09-TABEL-STATISTIK-DAN-TANGGAL.md). Bukti akhir host `logs/lp09-stats-host-verification.json`: 95 tes / 27 kelompok browser, 1.000 aksi, 70 layout, 1.350 sampel host serta build/paket lulus. Paket terbaru `lp09-daily-stats-2026-11-03`; paket `lp09-calendar-news` adalah riwayat sebelum klarifikasi. Belum ada instalasi atau pengujian fisik pada tahap host terbaru.

**Checkpoint terbaru LP09:** laporan baru pemilik tentang glitch kalender dan informasi terakhir emiten ditindaklanjuti di host. Jam mundur, tanggal tertinggal, filter agenda UTC/WIB dan urutan/pembaruan berita direproduksi lalu diperbaiki. Rincian: [LP09-JAM-KALENDER-DAN-BERITA.md](LP09-JAM-KALENDER-DAN-BERITA.md); bukti `logs/lp09-host-verification.json`, **93 tes Java / 27 kelompok browser / 1.000 aksi / 70 layout** lulus. Paket terbaru kini folder `Artifacts/Codex-LP-Verification/lp09-calendar-news-2026-11-03/`; paket save-owner/menu-fix di bawah adalah riwayat. APK LP09 belum dipasang dan pengujian perangkat tidak dilanjutkan pada checkpoint host ini.

Tanggal: 3 November 2026, Asia/Jakarta. **Belum ada sesi ketahanan 60 menit yang selesai.** Pengujian perangkat menemukan dua masalah tambahan yang perlu diperbaiki, lalu koneksi USB terputus ketika pengukuran pembanding berlangsung. Artefak lama tidak diberi label lulus untuk snapshot terbaru.

**Pembaruan host terbaru setelah pemilik memilih host dahulu:** NV01 diperluas dan diperbaiki lagi setelah reproduksi membuktikan facade lama dapat membuat timestamp baru lalu menimpa progres pemilik pengganti. Otoritas epoch kini diperiksa di titik commit; save terakhir tanpa pengganti tetap diterima. Detail sebelum/sesudah dan batas bukti: [NV01-DUA-PENULIS.md](NV01-DUA-PENULIS.md). Bukti gabungan terbaru `logs/save-owner-host-verification.json`: **93 tes / 19 suite terpilih dan 23 kelompok browser**, build QA/produksi berhasil, 405 aset web dan 13 DEX per paket diperiksa, 427 anggota assets/DEX QA identik dengan produksi, tanda tangan valid. Save pemain tetap sama hash-nya. Angka 1.000 aksi/70 layout pada paragraf berikut milik snapshot sebelumnya; tidak diklaim dijalankan ulang pada checkpoint baru ini.

Paket terbaru kini `Artifacts/Codex-LP-Verification/device-save-owner-fix-2026-11-03/GameSaham-debug.apk` (SHA-256 `6D29B1E2CD0844F0F328C168326B00EC751E6C7D91B73FBA8D8CF58CAA0D2C7D`) dan `GameSaham-QA-debug.apk` (`50254CBBC21A24833B11CD4BF002A31E4D232E2B8F465DC3D51DD94CA0CC90E5`). Belum dipasang/diuji di ponsel. Paket menu-fix di bagian riwayat bawah bukan lagi paket terbaru.

`device_session.py` kini wajib diberi `--apk` ke artefak arsip, lalu memeriksa byte APK QA terpasang sebelum input/screenshot. Folder build sementara tidak lagi menjadi sumber identitas sesi. Tes host `test_device_session.py` memakai ADB tiruan: paket cocok diterima, hash berbeda/paket hilang ditolak. Cleanup forward tidak lagi menutupi kegagalan utama. Log `logs/save-owner-device-helper-check.log`. Ini pemeriksaan alat secara host saja; tidak ada koneksi, instalasi atau input ponsel baru.

Pembaruan setelah instruksi pemilik untuk melanjutkan perbaikan: source dua perbaikan terkonfirmasi tetap masuk APK terbaru. Pengujian menu diperkuat tanpa menghapus assertion sebelumnya: snapshot lengkap tidak membaca profil/progres tambahan, snapshot tanpa level hanya mengambil level, dan snapshot tanpa kedua field tetap memakai fallback lama. Gate baru `logs/gate-1793703027825981500/gate.json` exit 0, memverifikasi **91 tes / 19 suite terpilih dan 23 kelompok browser**. Pada source yang sama, `logs/verified-continuation-sequence-current/sequences.json` mencatat 1.000 aksi UI terkontrol lulus dan `logs/verified-continuation-layout-current/layouts.json` mencatat 70 kasus layout lulus. Bukti gabungan, hasil gate check 0/regression dan 2/full, identitas APK serta hash save: `logs/continuation-host-verification.json`.

Alat benchmark perangkat kini mencatat `INCOMPLETE` beserta penyebab awal jika koneksi hilang; kegagalan membersihkan forward tidak boleh menutupi penyebab utama. Negative control saat perangkat tidak tersambung menghasilkan status INCOMPLETE, nol kelompok pengukuran, tanpa input perangkat: `logs/device-session-2026-11-03/disconnect-preflight-check/completion.json`. Ini perbaikan kejujuran bukti alat, bukan pengujian performa game. Saat pembaruan ini, `adb devices -l` masih tidak menampilkan perangkat; tidak ada percobaan instalasi atau pengujian ponsel baru yang dijalankan.

## Dua temuan tambahan

### LP07/NV01 — commit dua instance save masih dapat saling menimpa

`SaveManager.save` sebelumnya hanya tersinkronisasi per instance. Dua penulis bisa membaca keadaan lama yang sama, kemudian penulis dengan snapshot lebih tua melakukan commit setelah penulis baru. Reproduksi dua thread menghasilkan disk bertimestamp lebih rendah daripada yang diharapkan. Rincian sebelum/sesudah: [NV01-DUA-PENULIS.md](NV01-DUA-PENULIS.md).

Perbaikan: monitor bersama mengikat pemeriksaan versi, backup dan atomic replacement; pembersihan temp mencakup penolakan versi. Tes concurrency dan pembersihan temp lulus. Batas: monitor satu proses; tidak membuktikan koordinasi antaraplikasi/proses terpisah atau perilaku saat jam sistem dimanipulasi.

### LP08 — lencana menu mengambil profil keuangan lengkap tanpa kebutuhan

Pada APK perangkat setelah perbaikan storage, satu diagnostik mengukur `getSnapshot=3,3 ms`, `getPlayerProfile=139,3 ms`, dan `getPlayerProgress=1 ms`. Ketiganya hanya tiga pembacaan individual, bukan distribusi benchmark. Bukti `logs/device-session-2026-11-03/bridge-read-diagnostic.json`.

Lencana panel Lainnya hanya membutuhkan jabatan dan level, tetapi memanggil dua API tersebut lagi. Kedua nilai sudah berada pada `latestSnapshot.career.title` dan `latestSnapshot.playerProgress.level`. UI sekarang memakai snapshot itu dan mempertahankan fallback lama jika field tidak tersedia. Teks, angka dan desain tidak diganti.

Regression check sebelum perbaikan benar-benar gagal karena `profile=1, progress=1`: `logs/verified-menu-badge-before/results.json`. Sesudah perbaikan, check memerlukan nol pembacaan tambahan dan teks lencana sesuai snapshot canonical. Gerbang `logs/gate-1793702246106527500/gate.json` lulus: **91 tes / 19 suite terpilih serta 23 kelompok browser**. Ini belum membuktikan hasil latensi perangkat sesudah perbaikan menu, karena APK menu terbaru belum dipasang.

## Yang sudah diamati di perangkat

- POCO X8 Pro 2511FPC34G, Android 16, WebView 156.0.8078.47; layar saat preflight 120 Hz dan penghemat baterai nonaktif. Data awal ada pada `PREFLIGHT-POCO-X8-PRO.md`.
- Aplikasi QA terpisah `com.tradingglobal3d.mobile.qa`, ruang data terpisah dan APK yang terpasang cocok dengan hash paket storage-fix yang diperiksa.
- Order MARKET QA sebanyak **100 BBCA** diterima `OPEN` saat IDX tutup lalu menjadi **FILLED 100, remaining 0** setelah bursa buka. Filled value Rp1.027.500, biaya Rp1.541 dan cash impact Rp1.029.041. Bukti `qa-order-100.json` dan `qa-order-after-open.json` di direktori sesi.
- Background/resume 30 detik tercatat pada kedua run diagnostik, dengan runId terjaga dan waktu game maju. Ini bukti catch-up/resume, bukan bukti proses terus hidup ketika ditutup.
- FrameTimeline awal pada APK storage-fix: **241 frame**, p95 **16,643077 ms**, p99 21,251154 ms, max 23,216923 ms, tidak ada frame >700 ms pada **rekaman 20 detik itu**. Tetap ada Late Present, Buffer Stuffing dan App Deadline Missed pada mode layar 120 Hz. Tidak mengekstrapolasi hasil ini menjadi 60 FPS atau bebas jank selama satu jam.
- Pengukuran klik-ke-dua-frame dilakukan di dalam WebView untuk menghindari memasukkan jeda pengambilan hasil CDP. Ini tetap bukan input-to-photon atau sentuhan jari fisik. Pengukuran awal yang mencakup round-trip alat hanya diagnostik dan tidak digunakan untuk meluluskan target input perangkat.

## Sesi dan percobaan yang belum selesai

| Bukti | Keadaan sebenarnya |
|---|---|
| `run-1793699769975913700` | Sekitar 18 menit, 281 aksi tercatat; dihentikan setelah bug dua penulis terkonfirmasi. Tidak dihitung sebagai endurance selesai. |
| `run-1793701001700954400` | Sekitar 19,7 menit, 320 aksi; dihentikan lewat STOP setelah pembacaan profil mahal terkonfirmasi. Tidak dihitung sebagai endurance selesai. |
| `endurance-trace/trace.perfetto-trace` | Rekaman diagnostik sekitar sisa run kedua; proses trace sendiri dihentikan secara terarah dan hasil disimpan. Bukan trace satu jam selesai. |
| `navigation-before-badge-fix` | Tiga kelompok awal selesai; kemudian rute aktif tidak sesuai assertion. Bukti kegagalan disimpan. |
| `navigation-before-badge-paced` | Pencatatan target klik awal tidak cocok; tidak digunakan sebagai pembanding lulus. |
| `badge-before-focused` | Satu kelompok 30 sampel selesai; koneksi USB/target WebView hilang sebelum tiga pengulangan selesai. Tidak dihitung sebagai benchmark tiga pengulangan. |

Diagnostik koordinat sentuhan terpisah menunjukkan satu sentuhan diterima pada koordinat CSS dan tombol yang dimaksud. Ini tidak membuktikan sebab dua percobaan input terdahulu. Kasus input yang tidak cocok tetap dicatat sebagai ketidakpastian alat/interaksi dan tidak dihapus atau diasumsikan bug aplikasi tanpa reproduksi lebih lanjut.

Force-stop terkontrol, sepuluh relaunch Activity, pemulihan posisi/kas setelahnya, sampling memori awal/tengah/akhir dan sesi ketahanan final **masih belum selesai**. Skrip pemeriksaan disiapkan, tetapi keberadaan skrip bukan bukti sudah dijalankan.

## Paket terbaru, sudah dibangun tetapi belum diuji ulang di ponsel

- Produksi: `Artifacts/Codex-LP-Verification/device-menu-fix-2026-11-03/GameSaham-debug.apk`.
- SHA-256 produksi: `2113E18AE04A8BA43DE7C12441BBA3D344F3A3FA04177E9744B0040F0A511294`.
- QA: `Artifacts/Codex-LP-Verification/device-menu-fix-2026-11-03/GameSaham-QA-debug.apk`.
- SHA-256 QA: `7A1FDC2FD6FCBD430C0A1FC61852179BFA7DC805FA89EDCE5D03D5181EFB34A2`.
- 427 anggota assets/DEX QA dan produksi identik. Verifikasi paket terbaru memeriksa 405 aset web dan 13 DEX, tanpa kelas server QA atau kelas source test. Bukti `logs/apk-check-1793702795832113800/apk.json` dan `logs/device-session-2026-11-03/menu-fix-handoff.json`.
- APK QA yang terakhir terpasang sebelum USB terputus masih versi storage-fix: `AB89FCE983D50D8534A2D6ED90BB3BD18F1CB97A01A7529C825695B8CA9BFEA8`. Jangan menyebutnya versi menu-fix.
- Metadata game utama tetap menunjukkan tanggal pembaruan 31 Oktober; game utama tidak diperbarui. Save pemain lokal hanya di-hash dan tetap `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`.

## Next action

Izin instalasi QA dan pengujian sudah diberikan dan tetap berlaku; **tidak meminta izin yang sama lagi**. Saat Poco tersambung kembali, periksa identitas perangkat dan APK QA, update hanya paket QA, selesaikan pemeriksaan input/performa terarah serta lifecycle, lalu jalankan 60 menit pada snapshot yang stabil. Goal keseluruhan belum tercapai dan tidak ada jaminan semua layar sudah smooth.
