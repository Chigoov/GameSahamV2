# Acuan UI dari aplikasi di HP pemilik

Inspeksi 19 Oktober 2026 melalui ADB. Status: pemetaan visual selesai untuk layar di bawah; redesign produksi BELUM diimplementasikan pada checkpoint ini.

## Keputusan pemilik yang berlaku

CONFIRMED: portrait; isi halaman boleh scroll vertikal, navigasi bawah tetap diam. Jawaban langsung pemilik: “Ikuti pola aplikasi lama: isi boleh scroll, navigasi tetap.” Ini menggantikan preferensi fixed-page/tanpa scroll sebelumnya. Palet tetap graphite + amber + teal; level/XP dan badge berdampingan dengan karier, tanpa hadiah uang achievement; akses fitur mengikuti karier.

## Lingkup bukti

- HP fisik tersambung: Android 16, model yang dilaporkan ADB `2511FPC34G`, resolusi 1268 × 2756, density 520. Lebar ekuivalen sekitar 390 dp merupakan perkiraan dari density, bukan hasil pembacaan viewport CSS aplikasi lama.
- Foreground yang diinspeksi: `com.lydia.tycoongame/.MainActivity`, aplikasi yang dibuka pemilik sebagai referensi. Tidak dilakukan dekompilasi, ekstraksi APK, atau penyalinan source/aset privat.
- Navigasi layar, toggle chart, dan scroll dilakukan. Tidak menekan beli/jual, klaim, iklan, pengajuan pinjaman, pembelian, atau reset. Membuka layar bisa memicu progres onboarding otomatis pada aplikasi referensi; save referensi tidak diaudit.
- Screenshot disimpan lokal di `Artifacts/Phone-UI-Reference/` (ignored Git). Angka, profil, mata uang LYD, ranking dan ticker di screenshot adalah isi aplikasi referensi; bukan data atau requirement ekonomi game baru.
- XML elemen bukan selalu snapshot yang serentak dengan PNG. `07-portfolio.xml` berbeda konteks dengan PNG; jangan gunakan pasangan itu sebagai bukti koordinat. Pembacaan XML terakhir gagal dua kali karena berkas dump tidak tersedia; inspeksi dilanjutkan dari PNG. Tidak menyimpulkan ini bug aplikasi.
- Beranda dikembalikan sebagai halaman akhir; posisi scroll tidak dijamin sama dengan awal.
- Bukti ini membuktikan inspeksi aplikasi referensi pada HP fisik. APK baru `com.tradingglobal3d.mobile` BELUM dipasang/diuji pada perangkat dari kegiatan ini.

## Indeks screenshot yang diperiksa

| Berkas PNG | Isi yang benar-benar terlihat |
|---|---|
| 01-current-screen | Beranda: greeting, wealth hero, onboarding |
| 02-market | Pasar: sorotan naik/turun, kategori, search, daftar saham |
| 03-stock-detail | Detail QPAY, harga, high/low, timeframe, grafik garis |
| 04-stock-candles | Toggle candlestick pada instrumen yang sama |
| 05-stock-detail-lower | Metrik saham, rasio beli/jual dan posisi milik pemain |
| 06-stock-order-form | Input lot, total, kas, tombol beli/jual; tidak dieksekusi |
| 07-portfolio | Kekayaan Saya: daily result, wealth hero, alokasi donat, kesehatan |
| 08-current-confirm | Profil/statistik pada posisi scroll saat pengambilan |
| 09-home-return | Beranda: onboarding dan koleksi; bukan misi |
| 10-missions | Beranda bagian bawah: rank, liga, pintasan misi/achievement; namanya bukan isi layar |
| 11-missions-view | Halaman Misi Harian, tab harian/mingguan, progress dan daftar target |
| 12-home-achievements-entry | Beranda bagian bawah sebelum membuka achievement |
| 13-achievements | Pencapaian: filter kategori, total progres, kartu badge dua kolom |
| 14-home-final | Beranda bagian bawah setelah kembali dari achievement |

## Hasil rekonstruksi visual

| Area | Pola referensi | Penerapan pada game kita |
|---|---|---|
| Kerangka | Dark background, panel berlapis, 5 tab tetap di bawah | Pertahankan 5 root produksi Beranda/Pasar/Aset/Firma/Riset; satu area konten scroll, label tab selalu terlihat |
| Beranda | Wealth hero paling dominan, perubahan nilai berupa pill, progres onboarding | Kekayaan bersih pribadi dari snapshot Java; kas tersedia/tertahan menjadi rincian, bukan semuanya angka hero; karier/XP dan target selanjutnya ringkas |
| Pasar | Sorotan naik/turun, kategori horizontal, search, filter, baris saham | Indeks simulasi existing, daftar/quote domain, ticker dan harga mudah dipindai; label simulasi singkat tetap jelas |
| Detail | Harga besar, pill perubahan, high/low, timeframe dan grafik luas | Gunakan riwayat game existing; timeframe tanpa data menampilkan keadaan kosong; jangan mengarang candle atau metrik |
| Posisi | Kuantitas, biaya, nilai kini dan P/L dalam satu kartu | Data posisi domain; bedakan estimasi dan hasil terealisasi, akun dan mata uang jelas |
| Order | Form lot/total/kas dan tindakan mudah dibedakan | Tetap gunakan preview → review → submit → receipt existing; label Beli/Jual, ikon dan state tidak bergantung warna saja |
| Aset | Hasil hari ini, hero kekayaan, alokasi, risiko, daftar kepemilikan | Pisahkan kekayaan bersih, nilai aset, utang dan kas. Alokasi bisa grafik sederhana jika sumber domain tersedia; daftar tetap menjelaskan angka |
| Misi | Tab, progres agregat, target singkat, status/aksi kontekstual | Pakai misi domain existing. Jangan menyalin nominal, daily reset atau reward referensi sebagai aturan baru |
| Achievement | Filter, total diraih, kartu berikon dan state selesai | Gunakan badge/XP existing tanpa hadiah uang; kategori relevan dengan data yang benar-benar tersedia |
| Karier | Emblem, nama rank, level dan bar progres | Tampilkan jabatan, syarat promosi domain dan XP secara terpisah; XP tidak boleh tampak sebagai pembuka akses fitur |

## Koreksi konkret terhadap UI produksi sekarang

1. Header `index.html` memuat brand, badge “2D PORTRAIT”, level, wealth, jam, tiga tombol utilitas dan context strip sekaligus. Padatkan ke identitas halaman dan kontrol penting; utilitas masuk menu sekunder. Total wealth tetap dapat diakses di kanan atas, dengan hero lengkap pada Beranda/Aset.
2. `app.js` menampilkan kode FR/S/OQ dan paragraf penjelasan implementasi di layar pemain. Pindahkan kode ke dokumentasi dan ringkas penjelasan produk; biaya, risiko, data tidak tersedia dan konsekuensi transaksi tetap terbaca.
3. Banyak tombol diberi bobot yang sama. Tetapkan satu CTA utama tiap langkah, tombol sekunder outline/tonal, link detail dengan chevron, disabled dengan alasan dan tanpa handler yang dapat dieksekusi.
4. Grafik dan daftar pasar harus mendapat ruang lebih besar daripada penjelasan berulang. Gunakan harga/perubahan di awal, detail bertingkat sesudahnya.
5. CSS memiliki dua strategi scroll: body flex dengan main scroller, kemudian `@media (max-height: 44rem)` mengubah body menjadi scroll dan nav statis ikut bergerak. Rapikan shared layout agar nav tetap terlihat; jangan menambal tiap halaman. Uji teks 200% dan keyboard supaya ruang konten tetap dapat dipakai.
6. `.flow-page` saat ini menutupi nav. Detail baca dapat memakai tab root yang tetap terlihat; review transaksi fokus tetap harus mencegah submit ganda serta menangani perpindahan tab/draft dengan benar. Jangan sekadar menaikkan z-index nav di atas dialog konfirmasi.

## Batas adaptasi

Referensi membantu bentuk, urutan informasi, state dan feedback. Tidak otomatis menambah multiplayer, liga, ranking online, klaim iklan, reward streak, mata uang LYD, atau koleksi berbayar. Volume, investor, rasio beli/jual dan statistik baru hanya ditampilkan bila kontrak domain menyediakan data yang bermakna. Ilustrasi/avatar dari screenshot tidak diekstrak sebagai aset produksi.

Pakai font lokal existing, native HTML/CSS/JS, ikon konsisten dan token shared; tidak perlu framework atau dependency baru. Radius sekitar 16–20 CSS px, gutter 16 px, judul 24–28 px, body 14–16 px dan hero 30–36 px adalah titik awal desain, bukan hasil ekstraksi font/CSS dari aplikasi referensi. Pertahankan target sentuh minimal 48 dp ekuivalen dan reduced motion.

## Urutan implementasi berikutnya dan pemeriksaan

1. Shared shell portrait dan nav tetap, lalu Beranda/Aset/Pasar, detail/chart, karier/misi/achievement, disusul Firma/Riset dan flow lanjutan dengan token yang sama.
2. Reuse elemen/event/bridge existing. Domain Java tetap satu pemilik ekonomi, waktu dan progres; tidak membuat mesin harga/clock/XP kedua di JavaScript.
3. Periksa 360/390/412/430 CSS px, viewport pendek, zoom/teks 200%, keyboard input order, safe area, Back, posisi scroll per root dan reduced motion. Nav harus tetap terlihat saat konten di-scroll; konten terakhir tidak tertutup nav; tanpa overflow horizontal halaman.
4. Jalankan smoke Java-host browser untuk journey inti setelah perubahan, lalu build APK. Jangan mengubah status native/performa menjadi lulus dari screenshot browser atau keberadaan HP referensi.

Checkpoint ini hanya menambah acuan desain dan keputusan pemilik. Tidak ada perubahan source game, ekonomi, APK atau klaim kelulusan gate produk. Semua paket produk tetap PARTIAL (0/14 acceptance penuh); next action adalah menerapkan shell dan layar utama menggunakan referensi ini.
