# LP07 — preflight Poco X8 Pro dan paket pengujian terpisah

Tanggal: 3 November 2026, Asia/Jakarta. **Pembacaan informasi sudah diizinkan dan selesai. Instalasi serta pengujian belum diizinkan atau dijalankan.** Ini lampiran plan LP yang aktif, bukan plan baru.

## Perangkat yang benar-benar terbaca

| Informasi | Hasil snapshot pembacaan |
|---|---|
| Manufacturer / brand / market name | Xiaomi / POCO / POCO X8 Pro |
| Model / device | 2511FPC34G / klee |
| Android / API | 16 / 36 |
| Patch keamanan | 2026-10-01 |
| WebView aktif | com.google.android.webview 156.0.8078.47 |
| MemTotal kernel | 11.597.820 KiB, sekitar 11,06 GiB; bukan pembacaan kapasitas RAM pemasaran |
| Tampilan saat diperiksa | 1268×2756, renderFrameRate sekitar 120 Hz; mode yang didukung mencakup 30/60/90/120 Hz |
| Daya saat diperiksa | Baterai 95%, terhubung daya, BatterySaverEnabled=false |
| Game utama | com.tradingglobal3d.mobile, versionCode 1, versionName 1.0.0-2d-portrait, debug, sudah terpasang |

Bukti perintah/exit code dan output ada di `logs/device-info-2026-11-03/`. `commands.json` memuat perintah baca yang dilakukan; tidak ada pembukaan game atau pembacaan save. APK terpasang disalin dari lokasi paket aplikasi publik `/data/app/.../base.apk` hanya untuk membaca sertifikat, bukan dari direktori data pemain.

SHA-256 sertifikat terpasang dan paket produksi terbaru sama:

`D4FDEB3F61EEA56DF127D25129C66D3F3968634C805DAC1844D585FA4F679377`

Kecocokan sertifikat bukan pengujian migrasi save lama. Pengujian yang diusulkan memakai aplikasi QA terpisah.

## Paket konkret untuk persetujuan instalasi

- Nama aplikasi: **Game Saham QA**.
- Application ID: **com.tradingglobal3d.mobile.qa**.
- Activity: `com.tradingglobal3d.mobile.MainActivity` dalam paket QA.
- APK: `Artifacts/Codex-LP-Verification/host-final-2026-11-03/GameSaham-QA-debug.apk`.
- SHA-256 APK: `F766BA7DC04FB7E6F2E1342F1BB3C23D1317DECDE76C25E14D425EC1094DDD93`.
- Tanda tangan debug terverifikasi. Build offline berhasil dalam 24 detik.
- Output QA berada di `qa-device-build/`, memakai `qa-device.init.gradle` dan overlay `qa-device-manifest.xml`. Build.gradle produksi, application ID produksi dan APK produksi tidak diubah.
- **427 anggota APK yang dibandingkan—seluruh assets dan DEX produksi—identik byte-for-byte.** Identitas paket/label dan metadata packaging QA dibedakan. Bukti: `logs/device-info-2026-11-03/qa-equivalence.json`.
- Verifier native QA: `logs/apk-check-1793698979350036400/apk.json`, exit 0; 405 aset web cocok, 13 DEX diperiksa, server QA/kasus unit tidak dikemas.
- `MainActivity` membuat `SaveManager` memakai `getFilesDir()` milik aplikasi. Application ID QA dan ketiadaan sharedUserId memisahkan ruang data QA dari game utama. Pemisahan direktori/UID aktual akan diperiksa setelah instalasi bila diizinkan.

## Sesi perangkat yang diajukan untuk persetujuan

Ponsel dipakai sekitar 60 menit untuk aplikasi QA. Tidak mengubah pengaturan global, jam, refresh rate, mode daya atau data game utama. Semua interaksi, transaksi simulasi dan save berasal dari aplikasi QA.

1. Instalasi baru paket QA dengan identitas di atas; bukan update game utama. Jika paket QA ternyata sudah ada, jangan menimpa/menghapus datanya tanpa pemeriksaan lebih lanjut.
2. Startup, navigasi, kuotasi, candle/garis, pilihan candle, filter, holder, tiket order dan transaksi simulasi yang tersedia pada karier QA. Catat layar/keadaan yang benar-benar dicapai, bukan menganggap semua fitur terbuka.
3. Pengukuran frame/input dan memori yang tersedia pada awal/tengah/akhir. Verifikasi alat ukur terhadap frame/surface aplikasi QA; tidak menganggap `gfxinfo`, RPC ADB atau dua callback animation frame sebagai bukti otomatis FPS/input-to-photon.
4. Background/resume dan force-stop **aplikasi QA** lalu buka kembali. Periksa progres, catch-up 48× dan kewajiban cutoff/settlement yang benar-benar dilintasi. Tidak memajukan jam sistem atau memakai kontrol skip pemain.
5. Screenshot/rekaman/log hanya untuk pengujian QA. Hentikan input otomatis bila aplikasi lain berada di depan; jangan mengambil screenshot aplikasi lain atau mengubah launcher.
6. Sesi ketahanan nyata 60 menit; catat elapsed, error/crash/ANR, kondisi daya/refresh yang benar-benar diamati, serta metrik dan batas alat. Bukti host 10×100 tetap terpisah dari aksi perangkat.

Target awal plan 60 FPS, respons visual p95 <=100 ms dan tidak ada frame beku >700 ms tetap merupakan target pengujian, bukan hasil preflight. Perangkat saat preflight berada pada 120 Hz; analisis harus mencatat refresh aktif saat skenario berjalan dan tidak mengklaim uji mode 60 Hz jika mode tersebut tidak dipakai. Bila suatu metrik tidak dapat diukur dengan alat yang tersedia, gate terkait tetap NEEDS_VALIDATION.

Setelah persetujuan sesi, perintah dibatasi pada serial perangkat yang sudah diperiksa dan paket QA. Tidak uninstall/reset game utama, tidak menyalin save pemain, dan tidak mengganti APK produksi pada perangkat tanpa izin pembaruan terpisah.

## Gerbang host setelah penambahan alat QA

Persiapan alat analisis host: Android Studio sudah menyediakan `plugins/android/resources/trace_processor_server/trace-processor.exe` dan `trace_processor_daemon/trace_processor_daemon.exe`, beserta JAR protokol Perfetto. Bantuan CLI sudah diperiksa dan disimpan di `logs/profiling-tools/`. Binary ini memakai layanan profiler/RPC Android Studio, bukan CLI SQL Perfetto biasa; integrasi dan ketepatan metrik masih perlu diverifikasi terhadap trace QA sebenarnya. Tidak ada profiler yang dijalankan pada ponsel dan tidak ada dependency yang diunduh/dipasang pada tahap persiapan ini.

`quality_gate.py run` terakhir: exit 0, `logs/gate-1793699081424565400/gate.json`; source produksi tidak berubah. Verifier APK kini menerima path APK QA opsional dan tetap memverifikasi aset serta definisi kelas dengan cara yang sama. Tidak melonggarkan assertion. Gate penuh tetap belum selesai sampai bukti perangkat terpenuhi.

Next action pada checkpoint preflight: persetujuan instalasi paket **Game Saham QA** dan sesi perangkat 60 menit yang dijelaskan di atas. Pernyataan belum install/launch/input/profiling di tahap ini adalah riwayat sebelum izin dan pelaksanaan QA.

## Pembaruan setelah instruksi host dahulu

Izin instalasi/pengujian QA sudah diberikan dan dua sesi diagnostik perangkat pernah dijalankan, tetapi belum ada sesi 60 menit selesai. Koneksi terakhir terputus dan pemilik memilih meneruskan host. Status aktual, paket terbaru dan batas bukti ada di [TEMUAN-PENGUJIAN-PERANGKAT.md](TEMUAN-PENGUJIAN-PERANGKAT.md) serta `logs/save-owner-host-verification.json`. Tidak ada tindakan perangkat baru pada kelanjutan host ini.

Saat perangkat kembali tersedia, identitas QA terpasang harus dicocokkan dengan artefak arsip. Setelah klarifikasi tabel statistik LP09, alat sesi memakai `python Work/Perbaikan-Lanjutan-Performa-2026-11-03/device_session.py --apk Artifacts/Codex-LP-Verification/lp09-daily-stats-2026-11-03/GameSaham-QA-debug.apk` setelah update QA yang sesuai. Argumen itu bukan bukti paket sudah terpasang; runner menarik APK publik QA dan menolak mismatch sebelum input. Uji host alat tanpa perangkat: `python Work/Perbaikan-Lanjutan-Performa-2026-11-03/test_device_session.py`. Laporan host terbaru: [LP09-TABEL-STATISTIK-DAN-TANGGAL.md](LP09-TABEL-STATISTIK-DAN-TANGGAL.md).
