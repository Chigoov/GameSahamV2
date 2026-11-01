# Catatan pemilik: UX, motion, visual dan pembaruan otomatis

Tanggal: 21 Oktober 2026, WIB.
Root produksi: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`.
Baseline source saat catatan dibuat: `8c61169`; verifikasi kembali sebelum eksekusi.
Status catatan: keputusan dan acceptance untuk pekerjaan berikutnya, bukan bukti implementasi atau tes lulus. Pada tugas ini Codex hanya membaca source dan menyimpan catatan/referensi; tidak mengubah game atau membangun APK.

## 1. Keputusan dan preferensi pemilik

Pemilik menyetujui prioritas berikut:
1. Tingkatkan cara game memperlihatkan hasil tindakan dan perkembangan pemain.
2. Perjelas beranda, tambahkan feedback transaksi dan pencapaian, lalu perkuat visual bisnis/koleksi.
3. Jika membutuhkan ilustrasi/gambar, Codex yang membuatnya menggunakan skill `imagegen`. Agen implementasi menginventaris kebutuhan aset dan mengintegrasikan keluaran yang disediakan; jangan mengganti ilustrasi yang diminta dengan placeholder final atau menganggap file sudah tersedia.

Pemilik juga menanyakan apakah game dapat berjalan dan memperbarui uang otomatis tanpa refresh manual, misalnya sesudah penjualan. Jadikan hal ini acceptance eksplisit pada tugas berikutnya. Jangan mengubah jadwal settlement atau aturan pendapatan hanya agar angka terlihat bergerak terus-menerus.

File `libflutter.so` dan `libapp.so` milik aplikasi referensi berhubungan dengan engine Flutter dan hasil kompilasi Dart. Game ini memakai Java + Android WebView, sehingga kode ekonomi berada dalam DEX dan UI dalam aset web; tidak memerlukan dua file Flutter tersebut. Kekurangan ilustrasi bisnis/koleksi adalah persoalan aset yang terpisah dari pilihan engine. Jangan migrasi ke Flutter untuk mengejar ukuran APK atau meniru tampilan.

## 2. Tiga referensi visual pemilik

Salinan referensi tersedia di `Docs/Design/References/Owner-2026-10-21/`. Ketiganya adalah referensi komposisi/visual, bukan gambar halaman yang dipasang menjadi UI produksi. Angka, grafik, logo, avatar, label PRO/verified, aktivitas kripto dan tombol transfer dari mockup tidak otomatis menjadi requirement atau data game.

| Referensi | Penerapan yang disarankan | Bagian yang diadaptasi |
|---|---|---|
| `01-wealth-and-asset-detail.jpg` | Beranda, ringkasan Portofolio, detail instrumen | Satu angka utama yang jelas, perubahan nilai pada periode yang disebut, pintasan ringkas, daftar aset, grafik detail dan CTA kontekstual |
| `02-market-and-candlestick.jpg` | Pasar, watchlist, detail saham, komposisi Portofolio | Kartu instrumen, sparkline dari data nyata game, grafik candle, filter periode, marker pilihan pengguna, tombol Beli/Jual yang jelas |
| `03-portfolio-and-quick-actions.jpg` | Portofolio, kepemilikan, Beranda dan daftar pantauan | Kartu nilai aset yang ringkas, tren kekayaan dari riwayat, daftar posisi, kelompok aksi cepat, hierarki informasi |

Arah gabungan: gunakan hierarki dan kerapian informasi gambar 1; grafik/data gambar 2; daftar kepemilikan dan aksi gambar 3. Pertahankan satu token warna/font/spacing/radius untuk seluruh game. Jangan menerapkan tiga tema terpisah. Pertahankan lima tab Beranda/Pasar/Portofolio/Bisnis/Lainnya dan fitur existing yang masih berlaku.

Angka mockup tidak boleh dipakai sebagai data produksi. Periode return harus jelas dan sesuai metode domain. Bila history belum ada, tampilkan empty state; jangan membuat grafik dekoratif atau candle intraday dari OHLC harian. Sparkline tidak memerlukan engine/framework tambahan.

Untuk kontrol/chart/kartu/gradient/ikon yang sesuai sistem existing, gunakan HTML/CSS/SVG/native yang sudah tersedia. Ilustrasi raster orisinal melalui Codex imagegen cocok untuk katalog bisnis, koleksi, kendaraan/properti, pegawai atau pencapaian bila dibutuhkan. Susun daftar aset berdasarkan katalog aktual, lokasi pemakaian, ukuran/rasio, gaya bersama dan kebutuhan transparansi. Simpan aset final dalam repo dan catat ukurannya; jangan membundel screenshot referensi sebagai gambar layar interaktif. Avatar/nama/branding dalam referensi tidak otomatis menjadi identitas game.

## 3. Hasil pemeriksaan source realtime saat catatan dibuat

Pemeriksaan ini bersifat source trace, belum pengujian runtime Android atau pembuktian seluruh layar.

- `app.js:1834–1846`: submit order memanggil bridge, menampilkan receipt, lalu `refreshSnapshot()`.
- `app.js:191–285`: refreshSnapshot memperbarui snapshot utama, header kekayaan, kas pada Beranda, clock/karier/XP dan beberapa ringkasan. Fungsi ini tidak merender ulang seluruh portofolio/perusahaan/detail.
- `app.js:4185–4201`: saat halaman aktif, timer sekitar 1000 ms memanggil tick domain dan refreshSnapshot. Daftar quote Beranda/Pasar diperbarui ketika tidak ada flow/detail terbuka. Timer ini bukan batas latensi yang dijamin dan tidak membuktikan semua layar live.
- `app.js:1808–1811`: detail instrumen mempunyai tombol manual Perbarui harga & antrean.
- `app.js:1849–1866`: receipt order dibuat dari hasil saat itu, dengan tombol Periksa status. Belum ditemukan pembaruan receipt otomatis pada interval umum.
- `app.js:448...` dan `580...`: Portofolio/Bisnis mempunyai renderer tersendiri yang dipanggil saat membuka layar atau sesudah aksi tertentu. Periksa data yang menjadi stale selama layar tetap terbuka.
- `MatchingEngine.java` mencatat TRADE_SELL dan biaya ketika fill benar-benar terjadi. Order yang baru dikirim/antre bukan seluruhnya hasil penjualan. Partial fill hanya mengkredit bagian terisi.
- `TradingFacade.java:1329–1335`: operasional perusahaan diproses pada siklus harian domain dan dikreditkan ke akun perusahaan. Jangan menampilkan pendapatan harian sebagai uang yang dicetak setiap frame atau langsung menjadi kas pribadi.
- Reksa dana mempunyai antrean/cutoff/settlement sendiri. Order pencairan diterima tidak otomatis berarti seluruh dana sudah cair.

Kesimpulan sementara: kas/snapshot utama memiliki jalur pembaruan otomatis; pengalaman seluruh layar belum dapat disebut sepenuhnya realtime. Telusuri current source dan ukur sebelum memperbaiki. Nomor baris adalah snapshot; cari fungsi/simbol bila bergeser.

## 4. Acceptance pekerjaan berikutnya

### A. Hasil transaksi terlihat tanpa refresh manual

Setelah transaksi terkonfirmasi sesuai aturan domain, layar yang terlihat harus menampilkan kas tersedia/tertahan, posisi/unit, biaya, status dan riwayat terkait dari revisi state yang konsisten tanpa pindah tab, tekan refresh, buka ulang detail atau restart.

Uji paling sedikit: jual saham langsung terisi; limit yang awalnya mengantre lalu terisi saat layar tetap terbuka; partial fill; cancel sisa; duplicate/retry request; jual koleksi; jual multi-aset; FX; reksa dana saat submit serta saat settlement; income/payroll perusahaan; reward/pencapaian; perubahan hari; kembali dari background. Sertakan account context pribadi/klien/perusahaan dan kedua mata uang bila relevan.

Kenaikan kas berasal dari settlement/fill yang sah dikurangi biaya. Kas berbeda dari kekayaan bersih; penjualan mengubah aset menjadi kas, bukan seluruh proceeds menjadi profit. Dana klien/perusahaan tidak boleh tampil sebagai kas pribadi. Pending/unknown/error tidak diberi animasi sukses. Hasil yang tidak diketahui harus dipulihkan menggunakan requestId yang sama, tanpa transaksi ganda.

Jangan merender ulang seluruh halaman setiap detik hingga input, scroll, pilihan, fokus dan konfirmasi hilang. Perbaiki jalur sinkronisasi shared dengan struktur existing; tidak membuat ekonomi/clock kedua di JS, framework state baru atau event bus spekulatif. Polling ringan yang memperbarui bagian terdampak dapat dipakai bila memadai; bandingkan dengan mekanisme existing sebelum menambah infrastruktur.

Simpan tes browser otomatis dengan domain produksi dan save QA yang membuktikan saldo/receipt/posisi berubah ketika pengguna tidak melakukan refresh. Rekam latensi input → hasil domain terlihat; untuk event tertunda, rekam interval setelah state domain berubah. Target aksi biasa P95 ≤300 ms sesuai standar existing; identifikasi secara terpisah delay polling 1 detik. Animasi tidak boleh menunda hasil transaksi yang sudah terkonfirmasi.

### B. Beranda dan perkembangan pemain

Beranda menjawab: keadaan sekarang, perubahan penting terakhir, dan tindakan berikutnya. Kurangi pengulangan angka kekayaan/header; pertahankan detail kas/posisi/utang yang dapat dibuka. Tampilkan agenda/promosi/misi dan konsekuensi yang relevan tanpa mengubah aturan hadiah yang belum diuji.

Berikan feedback singkat untuk order terisi, perubahan kas, XP/level/badge, peningkatan bisnis dan laporan periode. Nilai akhir harus tepat sesuai domain; angka animasi hanya presentasi. Dedup berdasarkan identitas kejadian/revisi agar polling, tab switch, restart atau retry tidak mengulang perayaan/hadiah yang sama. Awal load tidak dianggap transaksi baru.

Tetap bedakan submitted/open, partial, settled, rejected, cancelled dan unknown. Hindari alert yang memutus interaksi rutin bila feedback inline/receipt cukup; pertahankan konfirmasi aksi penting.

### C. Motion dan aksesibilitas

Reuse animasi tombol/transisi existing. Tambahkan motion secara terarah pada perpindahan yang perlu dijelaskan, perubahan angka dan pencapaian; jangan membuat semua kartu bergerak terus-menerus. Respect pengaturan reduced motion serta preferensi sistem, dengan status teks yang tetap jelas saat animasi mati. Mode hemat daya tidak mengubah hasil/perilaku ekonomi.

Grafik dari history domain, bukan animasi random. Tetap uji tujuh viewport, teks 100%/200%, contrast/fokus, target sentuh, serta input/scroll yang bertahan selama pembaruan otomatis. Pengujian Android fisik tetap gate tersendiri sesuai scope; jangan menyimpulkan mulus 60/120 FPS dari CSS atau benchmark browser.

### D. Ilustrasi dan penyerahan

Agen berikutnya membuat inventaris kebutuhan ilustrasi untuk Codex imagegen bila diperlukan; implementasi layout, sinkronisasi dan tes yang independen dapat diteruskan. Bedakan asset requested/generated/reviewed/integrated; jangan mengklaim asset tersedia atau sudah masuk APK hanya karena ada daftar kebutuhan. Ikuti instruksi terbaru pemilik untuk siapa yang membuat gambar.

Setelah implementasi: review terpisah sesuai kemampuan nyata, rerun regresi terkait, bukti UI/live update/performa, build APK baru dan verifikasi aset/source/signing/hash. Update tracker berdasarkan bukti. Pertahankan save pribadi dan batas repo/otorisasi pada AGENTS.md. Jangan push atau menjalankan agen otomatis dari catatan ini.

## 5. Titik mulai agen berikutnya

1. Preflight root/Git/local changes/writer dan baseline source terbaru.
2. Reproduksi perbedaan header live versus detail/order/portofolio yang stale dengan save QA.
3. Perbaiki sinkronisasi hasil transaksi tanpa refresh, lalu rapikan hierarki Beranda.
4. Tambahkan feedback/motion yang mengikuti kejadian domain; susun kebutuhan ilustrasi untuk Codex.
5. Jalankan acceptance di atas, review, dan bangun APK baru sesudah source stabil.

Catatan ini tidak mengaktifkan sesi Hermes, tidak memulai implementasi, dan tidak menjadi pengganti prompt eksekusi long task atau completion gate proyek.
