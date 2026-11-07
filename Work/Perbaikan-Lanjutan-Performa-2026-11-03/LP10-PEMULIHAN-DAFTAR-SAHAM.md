# LP10: pemulihan daftar saham setelah pembacaan gagal

Pemilik menyetujui preview pesan dan tombol coba lagi: **Ya, terapkan pesan dan tombol coba lagi**. Persetujuan ini tidak memasukkan tema Stockbit ke produksi.

## Reproduksi dan sebab

Baseline `logs/lp10-market-retry-before.js` identik dengan app.js sebelum LP10, SHA256 `e3c9dc144aa7373acafdb32b34758af626639ab7eb859a29bf24ccc143731c12`.

Saat memilih filter US pada daftar terisi, kegagalan getInstruments() keluar tanpa pesan terlihat. Throw/JSON rusak meninggalkan baris lama; hasil null/objek error dapat mengosongkan daftar lalu menimbulkan pageerror. Reproduksi memakai actual app.js dan kegagalan baca yang dikontrol di browser QA; ini bukan klaim backend selalu gagal.

Caller loadStocks(): pembukaan Pasar, debounce pencarian, filter exchange/sektor/watchlist, chip filter, pembaruan watchlist dan jalur QA beli. Pembacaan awal sektor/indeks di loadPasarView() sebelumnya hanya mencatat console.error. Caller ditelusuri sebelum perubahan.

## Perbaikan dan bukti

Produksi hanya mengubah app.js: validasi array setelah parse, tangkap kegagalan pada batas baca bersama, satu showMarketLoadError() untuk dua jalur. Baris saham/penguat/pelemah lama disembunyikan; state-panel existing berisi role alert, judul **Daftar saham belum dapat dimuat**, penjelasan **Harga sebelumnya tidak ditampilkan. Coba muat ulang daftar saham.**, dan tombol native **Coba lagi**. Retry memakai loadPasarView() existing; input query/exchange/sektor/watchlist dipertahankan. Tidak menambah dependency, polling atau cache; tidak mengubah uang, waktu, transaksi atau save. Salinan preview tema mendapat perbaikan yang sama.

`python Work/Perbaikan-Lanjutan-Performa-2026-11-03/check_market_retry.py --production` exit 0. Bukti individual, source dan hash: `logs/lp10-market-retry/results.json`.

- Empat bentuk kegagalan: throw, JSON rusak, objek error, null. Baseline masing-masing pageerror tanpa pesan; versi baru satu panel, 0 baris lama, 0 pageerror.
- Retry berulang saat gagal tidak menggandakan panel. Ketika pulih, daftar US kembali; pencarian AAPL tetap tepat satu emiten. Debounce dan kegagalan indeks awal juga diuji.
- Keyboard Enter lulus. 32 kombinasi dua tema preview, empat lebar, teks 100%/200% tidak membuat panel meluap. Screenshot `logs/lp10-market-retry/` diperiksa visual.
- Gate domain/browser `logs/gate-1793724138858454100/gate.json`: exit 0, regression PASS; full plan belum terverifikasi. Log eksekusi dan manifest harus dibaca bersama status.

Build, pemeriksaan APK, aksi/layout dan hash akhir dicatat pada `logs/lp10-host-verification.json` setelah pemeriksaan selesai. Jangan memakai dokumen ini untuk menganggap tahap yang belum ada buktinya telah lulus.

Checkpoint akhir host: **95 tes Java / 19 suite, 27 kelompok browser, 1.000 aksi terkontrol dan 70 layout existing**, ditambah uji LP10 empat bentuk kegagalan/indeks/debounce/keyboard, 32 layout panel dan pemindaian 57 pasangan teks per tema tanpa kegagalan kontras. Semua job selesai exit 0. Gate source-bound diperiksa ulang exit 0 untuk regression dan exit 2 yang diharapkan untuk klaim full-plan. Ini tidak menutup gate fisik. Diff LP10 terhadap baseline hanya penanganan error app.js; save pemain tetap SHA256 referensi.

Paket `Artifacts/Codex-LP-Verification/lp10-market-retry-2026-11-03/`: produksi `GameSaham-debug.apk` SHA256 `148fdf9d3341448a4692615c98a9893cdfe9587f93d649eea6f51af9da9e32ae`; QA `GameSaham-QA-debug.apk` SHA256 `3fafd98668c12d6b8afe987c7fc37063369b769a9d7a5a35524837c08a6de806`. Kedua tanda tangan valid, 427 anggota assets/DEX identik; 405 aset web dan 13 DEX diperiksa per APK, tanpa kelas server/test dan tanpa aset tema preview. Paket QA tetap `com.tradingglobal3d.mobile.qa`. Belum dipasang ke Poco.

## Gate desain perubahan yang disetujui

- Hard Gate PASS dalam scope panel: tombol native/role alert; copy disetujui; error nyata dikontrol QA; retry/keyboard/reflow diuji; tanpa angka contoh/klaim performa/kontrol kosong baru.
- Purpose Gate PASS: state-panel/card-title/btn-secondary existing, tanpa ikon/efek/dependency baru; tujuan mencegah baris lama dianggap hasil filter terbaru.
- Liveliness PASS: judul, alasan, satu aksi mengikuti layar existing; ENERGY 1 / RHYTHM 2 / MOTION 1; preview diperiksa sebelum persetujuan.
- Craftsmanship PASS: satu batas error bersama, empat bentuk kegagalan, retry berulang, filter/search tetap, 32 layout dan keyboard. Ini bukan audit seluruh layar/ekonomi.

## Batas dan pekerjaan berikutnya

APK baru tidak membuktikan terpasang/lancar pada Poco. Perangkat terakhir dinyatakan belum tersedia; izin QA tetap berlaku ketika tersedia. Tidak ADB/instalasi/perubahan global/save pemain/reset/push dalam tahap ini.

Next action: cek seluruh bukti host/paket LP10, lalu lanjutkan identitas APK QA, lifecycle/performa dan sesi fisik 60 menit ketika Poco tersedia. Tema Stockbit tetap membutuhkan keputusan penerapan terpisah.

## Audit penutupan host pada source LP10

Empat job benchmark existing diulang serial pada hash app.js LP10 `3cac7ac7d5874e60d91c5179747a4f3f6051bf6bd9dbe6ecd0c8f9b7c52956cc`, seluruhnya exit 0, tanpa pageerror. Bukti `logs/lp10-performance-jobs.json`, sampel mentah per job dan ringkasan `logs/lp10-performance-summary.json`, juga ditautkan dari host-verification.

Ada 1.350 sampel terukur: tiga pengulangan x 30 sampel per kategori, lima warmup dikeluarkan tiap pengulangan; p95 nearest rank. Beban kecil, 96 bar, serta lima posisi nyata/24 order OPEN diuji. P95 pembaruan Beranda/Pasar sekitar 34-35 ms; candle ramai 51,0 ms dan garis ramai 50,3 ms. Flush scroll utama 199,6 ms dan detail 201,1 ms termasuk debounce 150 ms; resume browser 50,3 ms. Ini durasi browser sampai dua frame, bukan FPS/IPC Android, latensi sentuhan fisik atau bukti lebih cepat daripada versi lain.

Audit sesi perangkat lama menemukan tiga session.json berstatus terminal/incomplete, tidak ada sesi 60 menit pada APK QA LP10. Status lama STOPPED_WITH_ERROR / INTERRUPTED_FOR_CONFIRMED_STORAGE_FIX dan STOP tidak dianggap proses hidup atau kelulusan. Pemilik terakhir menyatakan Poco belum tersedia; tidak melakukan polling ADB atau meminta izin pengujian yang sama lagi. Dengan pekerjaan host dan preview yang diizinkan sudah ditutup dalam scope plan, hambatan tersisa memerlukan perangkat tersedia kembali.
