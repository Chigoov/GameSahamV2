# Verifikasi host terakhir dan gate perangkat yang tersisa

**Rujukan terbaru setelah klarifikasi pemilik:** [LP09-TABEL-STATISTIK-DAN-TANGGAL.md](LP09-TABEL-STATISTIK-DAN-TANGGAL.md). Yang diperbaiki adalah tanggal/bulan berkedip dan sembilan kolom statistik existing di bawah chart. Cakupan berita pada checkpoint LP09 sebelumnya dikembalikan, daftar transaksi proposal tidak diterapkan. Baca bukti snapshot tabel terbaru, bukan APK/news-check pada riwayat di bawah.

**Pembaruan LP09 setelah laporan bug pemilik:** rujukan terbaru [LP09-JAM-KALENDER-DAN-BERITA.md](LP09-JAM-KALENDER-DAN-BERITA.md) dan `logs/lp09-host-verification.json`. Perbaikan jam/tanggal kalender serta berita emiten hari berjalan lulus 93 tes / 27 kelompok browser, 1.000 aksi UI dan 70 layout. APK baru sudah dibangun/diperiksa tetapi belum dipasang. Checkpoint save-owner dan hitungan 23 kelompok di bawah tetap riwayat tahap sebelumnya.

**Status terbaru, kelanjutan host 3 November:** gunakan `logs/save-owner-host-verification.json` dan [NV01-DUA-PENULIS.md](NV01-DUA-PENULIS.md). Penguatan kepemilikan save sudah lulus **93 tes / 19 suite terpilih, 23 kelompok browser**; APK QA dan produksi terbaru sudah dibangun serta diperiksa, tetapi belum dipasang di ponsel. Angka dan status persetujuan pada bagian checkpoint di bawah adalah riwayat; izin sesi QA sudah diberikan, dan pemilik memilih host dahulu setelah koneksi hilang. Goal penuh tetap belum selesai.

Tanggal: 3 November 2026, Asia/Jakarta. Plan aktif: LP00, LP01, LP02, LP03, LP04, LP08, LP05, LP06, LP07 pada `plan.md`. Root `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`, branch `codex/classic-ui-new-assets`, HEAD `9653f7a992405b4dfaf28acddbf1caf158d03957` dengan perubahan lokal dipertahankan.

**Hasil host pada snapshot terakhir lulus. Goal keseluruhan belum selesai: gate Poco X8 Pro/NV01 dan sesi fisik 60 menit tetap NEEDS_VALIDATION.** Dokumen ini menggantikan status terkini pada laporan pengambilalihan awal; log lama tetap merupakan bukti snapshot masing-masing.

Pembaruan perangkat, 3 November 2026: pemilik telah mengizinkan `adb devices -l`. Perintah berhasil, satu perangkat berstatus `device`, model ADB `2511FPC34G`, product `klee_global`. Bukti: `logs/poco-connection-2026-11-03.txt`. Izin ini hanya pemeriksaan koneksi; metadata lebih lanjut, instalasi dan pengujian belum dilakukan. Pernyataan belum ada ADB pada bagian riwayat di bawah berlaku sebelum pembaruan ini.

Pembaruan berikutnya: metadata perangkat dan game utama telah dibaca dengan izin pemilik. POCO X8 Pro/Android 16 terkonfirmasi; paket **Game Saham QA** yang terpisah sudah dibangun dan diperiksa, tetapi belum dipasang. Bukti, identitas APK dan lingkup persetujuan sesi ada di [PREFLIGHT-POCO-X8-PRO.md](PREFLIGHT-POCO-X8-PRO.md). Karena alat verifikasi paket ditambah argumen path QA, gate host diulang dan lulus pada `logs/gate-1793699081424565400/gate.json`; gate sebelumnya menjadi riwayat alat pada snapshot sebelumnya.

## Perbaikan tambahan setelah laporan awal

1. **LP08/T15, akun hilang setelah restore.** Uji baru membuat akun klien melalui Java, memilihnya di tiket produksi, lalu memuat save QA yang dibuat sebelum klien itu ada. Sebelum perbaikan, `ACC-CLIENT-1049` masih menjadi akun aktif meski tidak tercantum pada snapshot hasil restore. Run baru pada tanggal sama juga mempertahankan konteks akun/kas lama. Bukti gagal: `logs/verified-context-real-reproduction/results.json`.
2. **Perbaikan sumber:** `refreshSnapshot` mencocokkan pilihan dengan `tradingAccounts` canonical. Klien yang masih tersedia tetap terpilih dan datanya diperbarui; jika pilihan tidak lagi tersedia, gunakan akun pribadi yang memang menjadi konteks awal existing. Tidak menambah kontrol, teks atau aturan transaksi.
3. **LP04, catatan cakupan tersembunyi.** Java mengirim `coverage=PARTIAL` dan `coverageNote`, tetapi updater menyembunyikan status pada success/cache hit. Pemilik menyetujui preview kedua. Catatan sekarang bertahan pada success, cache hit dan retry; teks berasal dari Java. Error kembali menjadi status biasa setelah pulih. Payload yang tidak berubah mempertahankan node teks catatan dan kartu holder.
4. **LP07, paket tercampur kelas QA.** Pemeriksaan DEX native membuktikan APK sebelum pembersihan memuat empat definisi `WireQaServer` beserta kelas internal/synthetic. Dua file `.class` lama ditemukan di output kompilasi Android. Hanya dua file generated yang diidentifikasi tersebut dihapus setelah lokasi absolut dipastikan berada dalam `android/app/build`; tidak ada source/save/Git cleanup. Seluruh task build dijalankan ulang. Verifier native sesudahnya membuktikan kelas QA tersebut serta kelas dari source test tidak ada dalam APK.

Preview persetujuan catatan: `logs/verified-ownership-review/ownership-current.png`, `ownership-proposed.png`, dan `ownership-preview.json`. Scope persetujuan hanya catatan yang ditunjukkan. Keputusan fallback candle terbaru yang disetujui sebelumnya tetap dipertahankan.

## Bukti aktual pada source terakhir

| Pemeriksaan | Hasil dan bukti |
|---|---|
| Gerbang wajib | `quality_gate.py run`: exit **0**, `logs/gate-1793697721341420200/gate.json`. Manifest mengikat source, tests dan alat uji. |
| Domain | **90 tes / 18 suite terpilih lulus**, pada job domain gate tersebut; bukan seluruh repository. |
| Browser | **22 kelompok lulus**, tanpa page error; `logs/verified-gate-1793697721341420200-browser/results.json`. |
| UI berulang | **10 × 100 aksi terkontrol lulus**, source terakhir; `logs/verified-host-final-sequence/sequences.json`. |
| Ukuran layar/teks | **70 kombinasi**: lima layar × tujuh viewport × skala root 100/200%, tanpa page error atau pelanggaran batas horizontal container yang diperiksa; `logs/verified-host-final-layout/layouts.json`. |
| Paket | `verify_apk.py`: exit **0**; **405 aset web cocok**, 13 DEX berhasil diperiksa, tidak ada definisi `WireQaServer` atau kelas dari source test. `logs/apk-check-1793697422795103500/apk.json`. |
| Build | Offline, `assembleDebug --rerun-tasks`, seluruh 34 task berjalan, **BUILD SUCCESSFUL in 21s**; `logs/host-final-apk-rebuild.log`. |
| Tanda tangan | Verifikasi apksigner berhasil, v2, satu sertifikat Android Debug; `logs/host-final-signature.log`. Ini belum membuktikan kecocokan signer aplikasi yang mungkin terpasang di ponsel. |
| Identitas paket | `com.tradingglobal3d.mobile`, versionCode 1, minSdk 26, targetSdk 36; `logs/host-final-package-metadata.log`. |
| Save pemain | SHA-256 tetap sama dengan referensi. Hanya di-hash; tidak dibaca sebagai data, disalin, dimuat atau dijadikan fixture. |

Tujuh viewport: 320×568, 360×640, 390×844, 412×915, 430×932, 768×1024, 1440×900. Lima layar: Beranda, Pasar, candle, garis, pemegang saham. Reflow ini memeriksa container terkait dan screenshot, bukan semua route/kontras/aksesibilitas aplikasi.

Tiga skenario T15 tambahan memakai domain Java dan save QA baru: pergantian pribadi/klien saat tiket terbuka, restore yang menghapus akun terpilih, dan run baru pada tanggal sama. Prasyarat karier Portfolio Manager disediakan sebagai fixture sintetis di server QA lokal; penerimaan mandat, akun, saldo, save/load dan run tetap diproses Java sebenarnya. Ini tidak membuktikan perjalanan karier alami. Endpoint fixture hanya berada pada `Work/WireQaServer.java`, dan verifier DEX memastikan kelas server tersebut tidak dikemas dalam APK.

T13 juga memakai mutasi keuangan nyata pada timestamp sama: order LIMIT OPEN menahan **Rp901.350**, mengubah kas tersedia dari **Rp250.000.000 menjadi Rp249.098.650**. Snapshot antrean scroll tidak mengembalikan kas lama setelah hasil mutasi diterapkan. Nilai dan ID order tercatat pada hasil browser gate.

Verifier paket mereproduksi kegagalan sebelum perbaikan dengan exit 1: `logs/apk-check-1793697274072014600/apk.json`. DEX sebelum perbaikan dipertahankan di direktori itu. Daftar dua file generated yang dihapus beserta hash: `logs/removed-generated-qa-classes.json`. Tidak mengubah dependency, application ID, signing key atau konfigurasi global.

## Peninjauan perubahan tes dan gate

- Pemeriksaan lama tidak dihapus atau dilonggarkan. Tambahan wajib: listener persistent, tiga skenario akun/restore Java, dan reservasi order pada timestamp sama. Kasus yang tidak ada atau gagal ditolak gate.
- T10 kini memeriksa catatan benar-benar terlihat, node teks bertahan pada cache hit, serta role `alert` kembali menjadi `status` sesudah retry sukses.
- Fixture karier baru menghindari klaim palsu bahwa akun klien bisa dibuat dari karier awal. Kegagalan setup awal dicatat sebagai kegagalan setup, bukan bug produk.
- `verify_apk.py` memakai `dexdump` Android SDK untuk definisi kelas nyata; pemeriksaan string mentah saja tidak dipakai sebagai bukti akhir.
- Tiga negative control pada `test_quality_gate.py` memeriksa penolakan kasus hilang/gagal/duplikat, perubahan source/save dan klaim penuh. Ini menguji alat gate, bukan tiga tes permainan tambahan.
- `check <gate.json>` harus exit **0** untuk regresi saat ini. `check <gate.json> --claim full` harus tetap exit **2** karena bukti perangkat belum ada. Exit 2 tidak diubah menjadi PASS penuh.

## Pengukuran source terakhir

Rincian: `logs/host-current-performance-summary.json`, dengan tautan JSON sampel mentah. Tiga pengulangan, masing-masing 30 sampel per layar, lima warm-up dikeluarkan; p95 nearest rank atas 90 sampel. Pengukuran di Chromium dari update sampai dua callback animation frame. Scroll mencakup debounce produksi 150 ms.

Baseline pasangan ini adalah snapshot setelah empat perbaikan awal, sebelum perbaikan akun/catatan; bukan before PF. Kedua sisi memakai Java dan penyiapan QA yang sama. Beban: lima posisi hasil order Java FILLED, 24 order LIMIT OPEN, dan 96 bar grafik QA.

| Skenario | p95 sebelum, ms | p95 sesudah, ms |
|---|---:|---:|
| Beranda | 34,9 | 34,5 |
| Pasar | 34,2 | 34,4 |
| Detail candle | 50,0 | 50,6 |
| Detail garis | 49,5 | 34,8 |
| Flush scroll utama | 184,5 | 200,6 |
| Flush scroll detail | 201,1 | 201,1 |
| Event resume browser | 34,9 | 50,7 |

Perbedaan sekitar satu interval frame browser tidak membuktikan kenaikan/penurunan FPS perangkat. Tidak mengklaim peningkatan besar; biaya flush/resume dan variasinya tetap ditampilkan. Angka lama pada laporan awal berlaku untuk snapshot lama, bukan paket terbaru ini. Heap/GC Android, input-to-photon, jank dan sesi perangkat 60 menit belum diukur.

## Paket yang siap diperiksa pemilik

Salinan penyerahan: `Artifacts/Codex-LP-Verification/host-final-2026-11-03/GameSaham-debug.apk`.

- Ukuran: **16.239.753 byte**.
- SHA-256: `CB1680844D41611BBB6BF5CFFB7765340A689F8B26B1ED366A9F6181F4C8657C`.
- Salinan identik dengan output build yang diperiksa DEX/aset/tanda tangannya.
- Save pemain tetap `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`.

File berubah pada tambahan ini: `android/app/src/main/assets/www/app.js`, QA-only `Work/WireQaServer.java`, runner `verify_ui.py`, penguatan `quality_gate.py`, verifier paket `verify_apk.py`, dan dokumen hasil. Semua perubahan lokal sebelumnya dipertahankan. Tidak commit, push atau publish.

## Perintah verifikasi dan langkah tersisa

```powershell
python Work/Perbaikan-Lanjutan-Performa-2026-11-03/quality_gate.py run
python Work/Perbaikan-Lanjutan-Performa-2026-11-03/quality_gate.py check Work/Perbaikan-Lanjutan-Performa-2026-11-03/logs/gate-1793697721341420200/gate.json
python Work/Perbaikan-Lanjutan-Performa-2026-11-03/quality_gate.py check Work/Perbaikan-Lanjutan-Performa-2026-11-03/logs/gate-1793697721341420200/gate.json --claim full
python Work/Perbaikan-Lanjutan-Performa-2026-11-03/verify_apk.py
```

LP00–LP06 dan LP08 memiliki bukti host sesuai cakupan di atas; bagian host LP07 juga lulus. **LP07/T17 fisik dan NV01 tetap NEEDS_VALIDATION:** identitas Poco X8 Pro, versi Android/WebView, kondisi refresh rate/daya, kompatibilitas instalasi yang melindungi progres, background/resume/force-close/recovery, frame/input/memori dan sesi 60 menit. Tidak ada keputusan tampilan yang masih tertunda untuk dua preview LP yang telah disetujui pemilik.

Next action: minta izin pemeriksaan koneksi `adb devices -l` sebagai langkah perangkat pertama. Izin koneksi tidak otomatis mengizinkan instalasi, penyalinan data atau pengujian perangkat. Belum menjalankan ADB pada pengambilalihan ini.
