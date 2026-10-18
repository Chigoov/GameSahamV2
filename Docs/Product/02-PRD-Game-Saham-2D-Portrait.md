# PRD — Game Saham 2D Portrait

**Versi:** 1.0 — draf spesifikasi produk  
**Tanggal:** 18 Oktober 2026  
**Platform:** Android  
**Hasil akhir produk:** APK  
**Status:** arah produk sudah ditetapkan; keputusan yang belum dijawab tercatat pada bagian 10.

**Keputusan pemilik yang menjadi dasar:**

- Trading dan perjalanan karier menjadi fund manager sama penting.
- Seluruh kelompok fitur game sebelumnya wajib masuk versi pertama.
- Menggunakan perusahaan nyata, dengan dukungan pasar internal game dan pemutaran ulang data historis nyata.
- Tampilan 2D portrait dengan fokus pada pengalaman pengguna. Ruang, kamera, dan nuansa 3D dihapus.
- Produk akhir diserahkan sebagai APK.

**Cara membaca PRD ini:** persyaratan di bawah menetapkan perilaku yang harus dibangun, bukan menyatakan fitur tersebut sudah tersedia. Target pengguna spesifik, angka ekonomi, cakupan data historis, serta target performa yang belum disepakati diberi status **TBD**. Pola navigasi dan alur layar merupakan usulan desain dalam PRD ini.

---

## 1. Problem statement

Pemain membutuhkan game saham yang menghadirkan hubungan sebab-akibat yang masuk akal antara keputusan investasi, likuiditas pasar, informasi, risiko, dan hasil keuangan. Jika harga hanya bergerak secara acak, order selalu berhasil, atau biaya tidak terlihat, pemain kesulitan memahami mengapa keputusan mereka menghasilkan keuntungan atau kerugian.

Pengalaman yang dicari juga mencakup perkembangan dari trader pribadi menjadi pengelola dana klien. Tanpa karier, mandat, kepercayaan klien, dan konsekuensi keputusan, pertumbuhan pemain berhenti pada peningkatan saldo.

Konsep game sebelumnya memuat fitur yang luas dan penyajian landscape dengan ruang 3D. Arah baru mengutamakan antarmuka 2D portrait yang nyaman digunakan di ponsel. Penghapusan 3D adalah keputusan produk; besarnya penghematan ukuran aplikasi, RAM, dan baterai harus diukur setelah tersedia build yang dapat dibandingkan.

**Pihak yang terdampak:**

| Pihak | Masalah | Dampak |
|---|---|---|
| Pemain yang berfokus pada trading | Eksekusi, harga, dan risiko sulit dipercaya jika mekaniknya terlalu sederhana atau tidak transparan | Keputusan terasa tidak bermakna |
| Pemain yang berfokus pada karier | Perkembangan hanya berupa saldo atau label pangkat | Kurang memiliki tujuan dan tanggung jawab jangka panjang |
| Pemain di ponsel | Banyak panel dan informasi harus dibaca pada layar sempit | Sulit menemukan tindakan, membaca angka, dan memeriksa order |
| Pemilik produk | Cakupan fitur besar, tetapi batas “selesai” belum terdefinisi per fitur | APK dapat terbentuk sementara gameplay masih berisi placeholder atau alur yang terputus |

**Rumusan masalah produk:**

> Bagaimana menghadirkan game saham Android 2D portrait yang memungkinkan pemain menjalani perdagangan dan karier pengelolaan investasi secara utuh, dengan mekanik keuangan yang konsisten, informasi yang dapat ditelusuri, serta antarmuka yang nyaman di ponsel?

Pernyataan kebutuhan di atas merupakan dasar perancangan. Belum ada riset pengguna dalam pekerjaan ini yang membuktikan prevalensi masalah atau besarnya pasar.

---

## 2. Target user dan dua persona

### 2.1 Target user

**Sudah dikonfirmasi:** pemain menginginkan trading dan perkembangan menjadi fund manager sebagai dua pilar pengalaman yang sama penting.

**Belum dikonfirmasi:** produk hanya untuk pemilik secara pribadi atau juga pengguna umum; tingkat pengetahuan pasar modal; rentang usia; serta kebiasaan bermain.

Untuk merancang alur produk, PRD menggunakan dua **persona perilaku sementara**. Keduanya bukan profil hasil wawancara dan tidak menyatakan demografi pengguna sudah diketahui.

### 2.2 Persona A — Pemain yang berfokus pada trading

| Aspek | Deskripsi |
|---|---|
| Tujuan | Mengambil keputusan perdagangan dan memahami akibatnya |
| Aktivitas utama | Memantau saham, membaca berita, memeriksa grafik dan antrean, memasang order, mengelola posisi |
| Kebutuhan | Harga dan eksekusi yang konsisten, biaya terbuka, status order jelas, riwayat keputusan |
| Frustrasi | Order selalu terisi, stop-loss dianggap menjamin harga, grafik tidak sesuai transaksi, berita tanpa hubungan dengan pasar |
| Ukuran keberhasilan pribadi | Dapat menjelaskan hasil transaksi dan memperbaiki strategi berdasarkan bukti dalam game |

### 2.3 Persona B — Pemain yang berfokus pada karier dan pengelolaan dana

| Aspek | Deskripsi |
|---|---|
| Tujuan | Berkembang dari trader pribadi menjadi pengelola dana yang dipercaya |
| Aktivitas utama | Menyelesaikan tugas, menerima mandat, menyusun alokasi, mengelola risiko, membuat laporan, mempertahankan klien |
| Kebutuhan | Syarat promosi jelas, akun terpisah, tujuan klien berbeda, kepercayaan dan pendapatan yang dipengaruhi keputusan |
| Frustrasi | Pangkat naik hanya karena saldo, mandat tidak memiliki konsekuensi, uang klien tercampur dengan kekayaan pribadi |
| Ukuran keberhasilan pribadi | Mampu meningkatkan tanggung jawab dan dana kelolaan sambil menjaga mandat serta kepercayaan |

Kedua persona dapat muncul pada pemain yang sama.

---

## 3. Goals dan non-goals

### 3.1 Goals

**G-01 — Pasar yang konsisten.**  
Harga, order, likuiditas, berita, dan perilaku pelaku pasar mengikuti aturan yang dapat ditelusuri.

**G-02 — Dua pilar gameplay yang utuh.**  
Trading pribadi dan karier pengelolaan dana sama-sama memiliki tujuan, keputusan, konsekuensi, dan laporan hasil.

**G-03 — Seluruh fitur lama tersedia secara fungsional.**  
Cakupan 26 layar konsep lama dipertahankan melalui halaman, tab, atau dialog yang sesuai untuk portrait. Tidak wajib mempertahankan bentuk 26 halaman terpisah.

**G-04 — Mendukung dua konteks pasar.**

- Pasar internal dengan perusahaan nyata dan harga yang dibentuk mekanik game.
- Replay menggunakan dataset historis nyata.

Keduanya wajib tersedia. Perilaku transaksi dalam replay masih membutuhkan keputusan pada OQ-03.

**G-05 — Pengalaman 2D portrait yang nyaman.**  
Informasi disajikan bertahap, tindakan utama mudah dijangkau, dan pemain dapat memeriksa akun, mata uang, biaya, serta risiko sebelum bertransaksi.

**G-06 — Progres dan uang dapat dipercaya.**  
Tidak ada transaksi ganda, kehilangan save tanpa penjelasan, pencampuran dana, atau perubahan saldo tanpa pencatatan.

**G-07 — Produk menjadi APK yang dapat digunakan.**  
APK dapat dipasang dan dibuka secara mandiri, dengan aset serta dependensi runtime yang diperlukan.

**G-08 — Lebih ringan secara terukur.**  
Ukuran paket, waktu buka, penggunaan memori, dan kelancaran dicatat pada perangkat sasaran. Ambang kelulusan menunggu penetapan perangkat dan anggaran performa.

### 3.2 Non-goals

- Ruang trading 3D, kamera antarmeja, dan navigasi berbasis lingkungan 3D.
- Transaksi uang nyata, koneksi broker, rekening investasi, atau eksekusi order ke bursa nyata.
- Feed harga langsung; tidak diminta dalam keputusan terbaru.
- Multiplayer, kompetisi daring, dan leaderboard publik.
- Publikasi Play Store sebagai syarat versi pertama.
- Pembelian dalam aplikasi atau langganan uang nyata.
- Server atau layanan AI berbayar yang wajib digunakan untuk memainkan game.
- Penilaian hukum atau saran investasi yang diposisikan sebagai nasihat profesional.
- Menyalin aset, merek terminal, atau konten berbayar pihak lain.
- Menulis ulang seluruh mesin game hanya karena antarmukanya berubah. Kebutuhan penggunaan ulang ditentukan melalui pemeriksaan teknis terpisah.

Batas penggunaan pribadi dan kemampuan bermain offline mengikuti dokumen sebelumnya sampai ada keputusan baru.

---

## 4. User stories

| ID | User story |
|---|---|
| US-01 | Sebagai pemain baru, saya ingin memahami tujuan, akun, dan tindakan dasar supaya dapat memulai tanpa membaca seluruh sistem sekaligus. |
| US-02 | Sebagai trader, saya ingin mencari perusahaan, indeks, dan instrumen supaya dapat memilih peluang yang ingin saya pelajari. |
| US-03 | Sebagai trader, saya ingin membaca grafik, antrean, volume, dan transaksi terakhir supaya dapat menilai harga serta likuiditas. |
| US-04 | Sebagai trader, saya ingin melihat perkiraan biaya, jumlah terisi, dan slippage sebelum mengirim order supaya memahami konsekuensi transaksi. |
| US-05 | Sebagai trader, saya ingin memantau dan membatalkan sisa order supaya dapat mengendalikan eksposur saya. |
| US-06 | Sebagai pemain, saya ingin mengetahui berita dan peristiwa yang berkaitan dengan posisi saya supaya dapat memahami perubahan portofolio. |
| US-07 | Sebagai pemain, saya ingin melihat saldo, posisi, laba/rugi, dan riwayat transaksi yang konsisten supaya dapat menilai kondisi keuangan saya. |
| US-08 | Sebagai pemain pasar global, saya ingin menukar IDR dan USD dengan biaya yang jelas supaya dapat membeli instrumen dalam mata uang yang sesuai. |
| US-09 | Sebagai pemain karier, saya ingin melihat tugas dan syarat promosi supaya mengetahui langkah menuju peran berikutnya. |
| US-10 | Sebagai fund manager, saya ingin menilai dan menerima mandat klien supaya dapat memilih tanggung jawab yang sesuai dengan kemampuan saya. |
| US-11 | Sebagai fund manager, saya ingin mengelola setiap akun klien secara terpisah supaya dana dan hasil masing-masing mandat tetap jelas. |
| US-12 | Sebagai fund manager, saya ingin menyusun alokasi, memantau risiko, dan mengirim laporan supaya dapat menjaga kepercayaan klien. |
| US-13 | Sebagai investor, saya ingin mengikuti dividen, rights issue, IPO, M&A, dan voting supaya kepemilikan saya memiliki konsekuensi korporasi yang bermakna. |
| US-14 | Sebagai pengguna margin dan short, saya ingin mengetahui pinjaman, biaya, jaminan, dan risiko likuidasi supaya dapat mengambil keputusan dengan sadar. |
| US-15 | Sebagai pemain, saya ingin mencoba stress test dan strategi pada ruang terpisah supaya dapat belajar tanpa mengubah permainan utama. |
| US-16 | Sebagai pemain, saya ingin memainkan periode historis dengan informasi sesuai waktunya supaya dapat mengevaluasi keputusan tanpa kebocoran data masa depan. |
| US-17 | Sebagai pemain ponsel, saya ingin menyelesaikan seluruh tindakan dalam portrait supaya dapat bermain dengan nyaman tanpa memutar perangkat. |
| US-18 | Sebagai pemain, saya ingin melanjutkan progres setelah aplikasi ditutup atau diperbarui supaya keputusan dan waktu bermain saya tidak hilang. |

---

## 5. Fitur MVP, v2, dan nanti

### 5.1 Arti MVP pada produk ini

Pemilik menetapkan **semua fitur lama wajib**. Karena itu, MVP dalam PRD berarti **rilis pertama dengan seluruh kelompok fitur wajib yang berfungsi**, bukan pemotongan menjadi game trading dasar.

Pengerjaan boleh bertahap untuk mengurangi risiko. Tahap internal tersebut tidak mengubah syarat rilis pertama.

### 5.2 MVP — seluruhnya wajib

| Kelompok fitur | Cakupan wajib | Requirement |
|---|---|---|
| Mode permainan dan waktu | Pasar internal, replay historis, sesi, pergantian hari, briefing, laporan | FR-01 |
| Katalog dan indeks | Perusahaan nyata, pasar Indonesia dan AS, penjelajah indeks, pencarian, watchlist | FR-02 |
| Pasar hidup | NPC investor, likuiditas, berita, makro, peristiwa emiten, hubungan lintas aset | FR-03 |
| Perdagangan | Grafik, terminal, order book, market/limit, stop-loss, take-profit, partial fill, pembatalan | FR-04 |
| Aset dan pembukuan | Portofolio, kas tersedia/tertahan, valuasi, laba/rugi, biaya, ledger | FR-05 |
| Valuta asing | Dompet IDR/USD, kuotasi, preview, konversi, riwayat | FR-06 |
| Karier | Tutorial, misi, pekerjaan/imbalan, reputasi, promosi, pemulihan setelah rugi | FR-07 |
| Klien dan firma | Penawaran, mandat, akun terpisah, kepercayaan, arus dana, laporan, fee | FR-08 |
| Intelijen | Berita, riset, analis NPC, konsensus, data alternatif, kalender, paket terminal | FR-09 |
| Multi-aset | Saham, obligasi, komoditas, dan FX dengan mekanik instrumen yang jelas | FR-10 |
| Fund builder | Rencana alokasi, batas mandat, evaluasi, hubungan dengan pelaksanaan order | FR-11 |
| Aksi korporasi | Dividen, split/reverse split, rights issue, buyback, dampak merger | FR-12 |
| IPO dan M&A | Penawaran, pemesanan/tender, alokasi, pembayaran, refund, penyelesaian | FR-13 |
| Voting | Hak suara, agenda, keputusan, tenggat, hasil, konsekuensi | FR-14 |
| Risiko dan kepatuhan | Eksposur, drawdown, audit, margin, pinjaman saham, likuidasi, short squeeze | FR-15 |
| Gangguan perdagangan | Peringatan, halt, antrean, pembukaan kembali/lelang | FR-16 |
| Lab dan replay keputusan | What-if, stress test, evaluasi strategi, pemutaran ulang peristiwa | FR-17 |
| Pengalaman portrait | Navigasi, keterbacaan, aksesibilitas, keyboard, tombol Back, state layar | FR-18 |
| Penyimpanan | Autosave/manual, pemulihan, migrasi, lifecycle, perlindungan progres | FR-19 |
| Distribusi Android | APK mandiri, identitas aplikasi, pengujian instalasi dan pembaruan | FR-20 |

### 5.3 v2 — pendalaman setelah semua fitur MVP lulus

Daftar berikut merupakan kandidat pengembangan, bukan komitmen tambahan:

- Penambahan pasar, instrumen, dan periode historis di luar cakupan MVP yang disepakati.
- Indikator teknikal tambahan.
- Variasi tugas, mandat, profil NPC, dan peristiwa.
- Analisis strategi dan perbandingan benchmark yang lebih mendalam.
- Penyempurnaan presentasi dan personalisasi antarmuka.

Tidak ada fitur wajib pada tabel MVP yang dipindahkan ke v2.

### 5.4 Nanti — perlu keputusan produk baru

- Sinkronisasi cloud lintas perangkat.
- Mode kompetitif atau multiplayer.
- Publikasi toko aplikasi dan monetisasi.
- Integrasi layanan eksternal untuk pembaruan konten.

Fitur pada kelompok ini tidak menjadi syarat APK pertama.

---

## 6. Functional requirements per fitur MVP

### FR-01 — Mode permainan, waktu, dan siklus hari

**Perilaku wajib:**

1. Pemain dapat membuat permainan pasar internal atau sesi replay historis.
2. Setiap permainan menyimpan mode, waktu, versi aturan, serta dataset atau seed yang digunakan.
3. Pada pasar internal, perusahaan menggunakan identitas nyata; pergerakan harga berasal dari mekanik pasar game.
4. Pada replay historis, layar menyebut periode, sumber, resolusi data, dan batas mekanik yang dapat didukung dataset.
5. Pergantian mode tidak menimpa progres mode lain.
6. Siklus permainan menghubungkan briefing, perdagangan, penutupan, laporan, dan hari berikutnya.
7. Jam lokal bursa dan waktu game ditampilkan dengan konteks zona waktu yang jelas.
8. Aturan pause, percepatan, dan background harus konsisten dengan satu kebijakan waktu yang dipilih.
9. Pengubahan jam ponsel tidak boleh menggandakan reward, bunga, fee, atau peristiwa.
10. Laporan memisahkan hasil investasi dari setoran, penarikan, hadiah, gaji, dan arus dana lain.

**Ketentuan replay:**

- Informasi masa depan tidak boleh muncul sebagai informasi yang sudah tersedia pada waktu keputusan.
- Jika dataset hanya menyediakan OHLCV, game tidak boleh mengklaim memiliki rekaman antrean atau transaksi individual asli.
- Jika replay menggunakan rekonstruksi likuiditas, hasil eksekusinya harus disebut sebagai hasil model.
- Harga historis yang wajib persis mengikuti sumber tidak dapat sekaligus diklaim berubah karena order pemain. Pilihan aturan ini tercatat pada OQ-03.

**Kriteria penerimaan:** permainan dapat dibuat, dijalankan melewati pergantian hari, disimpan, dan dilanjutkan tanpa perubahan mode atau pengulangan penyelesaian harian. Replay mengikuti kontrak data yang telah diputuskan.

### FR-02 — Katalog, pencarian, watchlist, dan indeks

**Perilaku wajib:**

1. Instrumen memiliki identitas unik yang mencakup bursa; ticker sama pada bursa berbeda tidak boleh tertukar.
2. Katalog mendukung pencarian berdasarkan ticker dan nama serta filter bursa, sektor, dan jenis aset jika datanya tersedia.
3. Detail menampilkan mata uang, unit/lot, jam pasar, status perdagangan, dan kelengkapan data.
4. Watchlist dapat ditambah, dihapus, dan dipulihkan setelah aplikasi dibuka kembali.
5. Instrumen yang belum dapat diperdagangkan diberi status jelas dan tidak memiliki tombol transaksi aktif.
6. Indeks memiliki daftar anggota, metode perhitungan, bobot bila relevan, dan periode berlaku.
7. Indeks internal game dibedakan dari seri indeks historis resmi.
8. Replay memakai keanggotaan dan metadata yang sesuai periode jika tersedia. Ketiadaannya harus dinyatakan.

**Kriteria penerimaan:** pencarian dan pemilihan instrumen tidak salah bursa; indeks konsisten dengan metode yang dinyatakan; katalog pasif tidak dipresentasikan sebagai cakupan perdagangan aktif.

**TBD:** bursa, indeks, jumlah instrumen aktif, dan kelengkapan anggota untuk rilis pertama.

### FR-03 — NPC, likuiditas, berita, dan hubungan pasar

**Perilaku wajib:**

1. NPC memiliki strategi, modal atau kapasitas pendanaan, kepemilikan, dan batas risiko.
2. Pada pasar internal, order pemain, akun klien, dan NPC memakai mekanisme perdagangan bersama.
3. NPC tidak membeli tanpa sumber dana atau menjual tanpa saham/pinjaman yang sah menurut aturan game.
4. Strategi NPC mencakup perbedaan perilaku, seperti fundamental, momentum, konservatif, spekulatif, dan penyedia likuiditas.
5. Peristiwa mempunyai waktu rilis, cakupan emiten/sektor/aset, serta pemicu dan dampak yang tercatat.
6. Respons terhadap berita mempertimbangkan eksposur dan ekspektasi, bukan menggerakkan semua saham secara seragam.
7. Rezim tren naik, turun, datar, dan stres memengaruhi perilaku serta likuiditas melalui aturan yang terdokumentasi.
8. Dampak sementara arus order dibedakan dari perubahan penilaian fundamental.
9. Rangkaian stop, margin call, penjualan paksa, dan short covering memiliki urutan pemrosesan yang terbatas agar tidak berulang tanpa akhir.

**Kriteria penerimaan:** skenario yang sama dapat direproduksi dengan seed, versi aturan, dan urutan input yang sama; perubahan harga dan order dapat ditelusuri ke transaksi atau peristiwa terkait.

### FR-04 — Grafik, terminal, dan order

**Perilaku wajib:**

1. Detail instrumen menampilkan harga, perubahan, volume, grafik, bid/ask, antrean, serta transaksi terakhir sesuai data yang tersedia.
2. Grafik pasar internal berasal dari riwayat game; grafik replay berasal dari dataset dan pemrosesan yang dinyatakan.
3. Pemain dapat memasang order beli/jual market dan limit serta mengatur stop-loss dan take-profit.
4. Sebelum pengiriman, tiket menampilkan akun, instrumen, sisi, jumlah unit/lot, jenis order, harga atau pemicu, mata uang, estimasi biaya, dan dampak kas.
5. Preview menyertakan estimasi VWAP, slippage, dan kemungkinan partial fill jika data likuiditas mendukung.
6. Estimasi diberi konteks waktu dan tidak dijanjikan sama dengan hasil akhir.
7. Sistem memvalidasi saldo tersedia, kepemilikan, pinjaman, tick, lot, sesi, status pasar, mandat, dan margin.
8. Limit tidak dieksekusi lebih buruk daripada batasnya.
9. Pada pasar internal, pencocokan mengikuti prioritas harga dan waktu sesuai aturan yang dipilih.
10. Status order meliputi menunggu pemicu, terbuka, terisi sebagian, terisi penuh, dibatalkan, kedaluwarsa, dan ditolak sesuai kebutuhan jenis order.
11. Pembatalan hanya membatalkan kuantitas yang belum dieksekusi.
12. Stop-loss dan take-profit mengaktifkan order menurut aturan yang ditetapkan; keduanya tidak menjamin eksekusi pada harga pemicu.
13. Pengiriman berulang dengan identitas permintaan yang sama menghasilkan satu tindakan ekonomi.
14. Order tanpa fill tidak mengubah harga transaksi terakhir pada pasar internal.
15. Market impact yang sudah terbentuk melalui pencocokan tidak ditambahkan kembali untuk transaksi yang sama.

**Kriteria penerimaan:** pemain dapat memasang limit, menerima partial fill, membatalkan sisa, menjual posisi, dan merekonsiliasi hasil dengan kas serta ledger. Retry tidak menghasilkan order ganda.

**TBD:** masa berlaku order, perlakuan sisa market order, aturan stop/TP, dan settlement.

### FR-05 — Portofolio dan pembukuan

**Perilaku wajib:**

1. Layar aset membedakan kekayaan pribadi, dana kelolaan klien, dan pendapatan manajer.
2. Kas ditampilkan per mata uang dengan saldo total, tersedia, dan tertahan.
3. Posisi memuat jumlah, biaya perolehan, harga valuasi, nilai pasar, dan laba/rugi.
4. Laba/rugi terealisasi dan belum terealisasi dipisahkan.
5. Setiap perubahan ekonomi memiliki catatan transaksi yang menghubungkan penyebab dan akun.
6. Biaya, dividen, gaji, hadiah, konversi, pinjaman, dan fee manajer dapat diperiksa.
7. Pembaruan fill, kas, posisi, biaya, dan ledger harus konsisten sebagai satu penyelesaian.
8. Rekap lintas mata uang menyebut kurs valuasi dan waktunya.
9. Saldo negatif hanya diperbolehkan jika memang merupakan utang yang sah menurut fasilitas aktif.

**Kriteria penerimaan:** saldo dan posisi dapat direkonsiliasi dari transaksi; akun lain tidak berubah akibat transaksi yang tidak terkait.

### FR-06 — FX IDR/USD

**Perilaku wajib:**

1. Pemain dapat menukar IDR ke USD dan sebaliknya.
2. Preview memuat kurs bid/ask, spread, biaya, jumlah bersih, serta saldo setelah konversi.
3. Kuotasi memiliki waktu berlaku dan diperiksa kembali saat eksekusi.
4. Dana tertahan untuk order tidak boleh digunakan untuk konversi.
5. Debit dan kredit kedua mata uang diselesaikan bersama.
6. Pembulatan tidak boleh menciptakan keuntungan pasti melalui konversi berulang.
7. Riwayat menyimpan kurs dan biaya aktual.

**Kriteria penerimaan:** konversi berhasil atau gagal secara utuh; kuotasi kedaluwarsa tidak dieksekusi tanpa peninjauan ulang.

### FR-07 — Tutorial, misi, dan karier

**Perilaku wajib:**

1. Pemain baru memulai dengan profil dan modal yang didefinisikan secara eksplisit.
2. Tutorial mengajarkan akun, harga, antrean, order, biaya, risiko, dan laporan.
3. Tutorial dapat dibuka kembali; melewatinya tidak memberikan imbalan otomatis.
4. Misi menguji tindakan dan hasil yang tercatat, bukan hanya kunjungan layar.
5. Imbalan hanya dapat diterima sekali untuk penyelesaian yang sama.
6. Perkembangan mencakup trader, analis, fund manager, dan jenjang senior sesuai konsep lama.
7. Promosi mempertimbangkan kualitas keputusan, pengalaman, risiko, kepatuhan, dan kepercayaan sesuai rumus yang ditetapkan.
8. Syarat fitur terkunci dapat dilihat sebelum pemain membukanya.
9. Pemain yang rugi atau kehabisan modal memiliki jalur pemulihan yang dijelaskan.
10. Gaji, tugas berbayar, dan reward tidak menciptakan sumber uang tanpa batas melalui eksploitasi pengulangan.

**Kriteria penerimaan:** perjalanan karier dapat diuji dari profil baru sampai pengelolaan klien, termasuk kegagalan dan pemulihan, tanpa pemberian saldo tersembunyi.

**TBD:** modal awal, jenjang final, ambang promosi, nilai imbalan, dan mekanik pemulihan.

### FR-08 — Klien, mandat, dan firma

**Perilaku wajib:**

1. Pemain dapat meninjau, menerima, atau menolak penawaran mandat.
2. Mandat menjelaskan tujuan, profil risiko, benchmark, horizon, aset yang diizinkan, konsentrasi, kebutuhan kas, batas penurunan, leverage, fee, dan laporan.
3. Penerimaan membuat hubungan mandat dan akun yang terpisah.
4. Akun aktif selalu terlihat pada portofolio, tiket, preview, dan konfirmasi.
5. Hard limit mandat diperiksa sebelum transaksi; peringatan informasional dibedakan dari larangan.
6. Klien dapat menambah dana, menarik dana, meminta penjelasan, bertahan, atau mengakhiri hubungan sesuai aturan.
7. Penarikan tidak boleh menciptakan kas; kekurangan likuiditas ditangani menurut kebijakan yang dinyatakan.
8. Penjualan untuk memenuhi penarikan memakai mekanisme pasar yang sama.
9. Kepercayaan dipengaruhi performa, risiko, pelanggaran, dan komunikasi.
10. Management fee dan performance fee dihitung dari basis yang transparan.
11. Performance fee menggunakan high-water mark yang memperhitungkan arus modal; setoran tidak dianggap keuntungan.
12. Fee yang sama tidak ditagih kembali setelah laporan dibuka ulang atau save dimuat.
13. Laporan klien menampilkan hasil, benchmark, risiko, arus dana, dan kejadian penting.

**Kriteria penerimaan:** satu akun pribadi dan sedikitnya dua akun klien dapat digunakan dalam skenario pengujian tanpa pencampuran dana, fee ganda, atau kebocoran mandat.

### FR-09 — Riset, berita, analis, kalender, dan langganan

**Perilaku wajib:**

1. Berita dapat ditelusuri berdasarkan emiten, sektor, pasar, waktu, dan peristiwa.
2. Briefing menjelaskan perubahan portofolio menggunakan kejadian dan kontribusi yang tercatat.
3. Jika penyebab tidak cukup diketahui, briefing menyatakan keterbatasannya.
4. Analis NPC memiliki laporan, target, waktu penerbitan, opini, dan riwayat akurasi.
5. Prediksi dapat salah dan tidak memakai informasi masa depan.
6. Data alternatif menjelaskan sumber dalam dunia game, waktu observasi, relevansi, dan ketidakpastian.
7. Kalender memuat sesi, libur, earnings, agenda makro, aksi korporasi, IPO/M&A, voting, dan kewajiban pemain.
8. Kalender publik tidak membocorkan hasil peristiwa yang belum diumumkan.
9. Paket terminal memuat manfaat, biaya USD game, periode, status aktif, dan perpanjangan.
10. Akses diperiksa pada layanan data, bukan hanya dengan menyembunyikan tombol.
11. Penagihan, pembatalan, dan kedaluwarsa tidak memungut biaya ganda.
12. Dokumen historis memakai tanggal publikasi yang sesuai, bukan hanya periode laporan.

**Kriteria penerimaan:** paket yang dibeli membuka manfaat yang dijanjikan, biaya tercatat sekali, akses berubah sesuai masa berlaku, dan tidak terdapat kebocoran informasi masa depan.

### FR-10 — Multi-aset

**Perilaku wajib:**

1. Saham, obligasi, komoditas, dan FX mempunyai instrumen yang benar-benar dapat digunakan sesuai aturan masing-masing.
2. Setiap instrumen menetapkan unit, mata uang, harga, biaya, sumber valuasi, dan settlement.
3. Obligasi memiliki ketentuan kupon, jatuh tempo, serta harga dan akrual yang konsisten dengan model yang dipilih.
4. Bentuk komoditas harus ditetapkan sebelum dibangun; label komoditas tidak cukup menggantikan definisi instrumen.
5. FX sebagai pasangan yang diperdagangkan dan FX sebagai penukaran dompet dibedakan jika keduanya digunakan.
6. Portofolio mengonsolidasikan nilai dan eksposur lintas aset tanpa menjumlahkan mata uang secara langsung.
7. Mandat memeriksa jenis aset yang diizinkan.

**Kriteria penerimaan:** setiap kelas aset memiliki alur transaksi, valuasi, dan pencatatan yang dapat diselesaikan. Ringkasan alokasi saja tidak memenuhi fitur ini.

### FR-11 — Fund builder

**Perilaku wajib:**

1. Pemain memilih akun, instrumen, bobot target, dan cadangan kas.
2. Total alokasi divalidasi terhadap kapasitas investasi dan batas leverage.
3. Sistem memeriksa konsentrasi, jenis aset, mata uang, dan mandat.
4. Rencana memperlihatkan selisih dari posisi sekarang, biaya perkiraan, dan kebutuhan likuiditas.
5. Penyimpanan rencana tidak langsung mengubah kepemilikan.
6. Pelaksanaan rencana menggunakan order dan validasi yang sama dengan perdagangan biasa.
7. Hasil parsial ditampilkan; sistem tidak menyatakan target tercapai jika order belum terisi.

**Kriteria penerimaan:** rencana dapat disimpan, ditinjau, dilaksanakan, dan dibandingkan dengan hasil aktual.

### FR-12 — Aksi korporasi

**Perilaku wajib:**

1. Mendukung dividen, split, reverse split, rights issue, buyback, serta dampak merger pada posisi.
2. Setiap aksi memiliki jadwal, ketentuan, status, dan aturan kelayakan kepemilikan.
3. Dividen dibayarkan sekali kepada akun yang berhak.
4. Split/reverse split menyesuaikan unit dan biaya perolehan sesuai ketentuan.
5. Fraksi unit ditangani melalui kebijakan yang dinyatakan.
6. Rights memiliki jumlah hak, harga tebus, tenggat, dan status digunakan/kedaluwarsa.
7. Order terbuka ditangani secara eksplisit saat penyesuaian instrumen.
8. Buyback atau tender tidak menghasilkan kas tanpa penyerahan saham yang sesuai.
9. Efek aksi terhadap harga referensi, grafik, dan indeks diproses konsisten.
10. Riwayat menyediakan penjelasan perubahan unit dan kas.

**Kriteria penerimaan:** aksi berlaku sekali dan tetap konsisten setelah save/load, termasuk kasus fraksi, hak kedaluwarsa, dan order terbuka.

### FR-13 — IPO dan M&A

**Perilaku wajib:**

1. Penawaran menampilkan pihak terkait, ketentuan, harga atau metode penentuan harga, jadwal, serta risiko.
2. Pemesanan IPO memvalidasi jumlah dan dana.
3. Dana pesanan mempunyai status tertahan/terpakai/dikembalikan yang jelas.
4. Alokasi penuh, sebagian, dan gagal ditangani.
5. Sisa dana dikembalikan sekali.
6. Tender M&A memeriksa saham yang tersedia dan ketentuan kelayakan.
7. Penyelesaian pembayaran, saham baru, atau perubahan instrumen mengikuti hasil peristiwa.
8. Penawaran yang berakhir tidak menerima transaksi baru.

**Kriteria penerimaan:** pemesanan sampai alokasi/refund dan tender sampai penyelesaian dapat direkonsiliasi ke akun.

**TBD:** penggunaan skenario fiktif pada perusahaan nyata dan cakupan peristiwa historis.

### FR-14 — Voting pemegang saham

**Perilaku wajib:**

1. Agenda memuat resolusi, pilihan, periode pemungutan, dan dasar hak suara.
2. Hak suara dihitung dari kepemilikan yang memenuhi ketentuan pada waktu acuan.
3. Pemain dapat menyetujui, menolak, atau abstain.
4. Suara yang sama tidak tercatat berulang.
5. Pembelian setelah waktu acuan tidak menambah hak suara untuk agenda tersebut.
6. Hasil dan konsekuensi korporasi/reputasi ditampilkan jika memang ditetapkan.
7. Akun pribadi dan mandat klien tetap dibedakan.

**Kriteria penerimaan:** suara tidak melebihi hak; reload dan retry tidak menggandakan pilihan.

### FR-15 — Risiko, kepatuhan, margin, dan short

**Perilaku wajib:**

1. Menampilkan konsentrasi, eksposur sektor/mata uang/aset, drawdown, serta likuiditas.
2. Pelanggaran menghubungkan aturan, akun, kejadian, dan tindakan yang diperlukan.
3. Margin memiliki modal sendiri, utang, jaminan, initial margin, maintenance margin, biaya, dan batas fasilitas.
4. Short memerlukan inventori pinjaman, biaya, kewajiban pengembalian, serta aturan recall/forced buy-in.
5. Hasil penjualan short tidak diperlakukan sebagai keuntungan bebas tanpa kewajiban.
6. Margin call menjelaskan sebab, kekurangan, tenggat atau kondisi penyelesaian.
7. Likuidasi paksa memakai harga serta likuiditas yang tersedia, dengan memperhitungkan gap dan halt.
8. Jika posisi tidak dapat segera dilikuidasi, kewajiban tetap dicatat dan ditampilkan.
9. Short squeeze berasal dari posisi, pembelian kembali, dan kondisi pasar yang tercatat.
10. Audit mencatat order, pembatalan, pelanggaran, alasan keputusan, dan penanganannya.
11. Fitur margin/short tidak dianggap selesai jika hanya berupa radar atau ilustrasi.

**Kriteria penerimaan:** pinjaman, bunga, jaminan, hasil short, kerugian, repayment, dan likuidasi dapat direkonsiliasi pada skenario normal serta pasar stres.

### FR-16 — Halt dan pembukaan kembali

**Perilaku wajib:**

1. Instrumen/pasar memiliki state yang membedakan perdagangan normal, peringatan, halt, pembukaan kembali, dan tutup.
2. Alasan serta kondisi pembukaan kembali terlihat.
3. Semua jalur order tunduk pada status pasar.
4. Perlakuan order baru, pembatalan, dan order lama saat halt mengikuti aturan bursa game.
5. Lelang pembukaan kembali menentukan harga dan alokasi menurut aturan yang terdokumentasi.
6. Pemulihan dari halt tidak menggandakan fill atau menghapus kewajiban.

**Kriteria penerimaan:** order langsung, order klien, stop, dan likuidasi tidak dapat melewati pembatasan halt melalui jalur lain.

### FR-17 — Stress test, lab strategi, dan replay keputusan

**Perilaku wajib:**

1. Stress test menerima skenario harga, kurs, suku bunga, likuiditas, atau arus dana klien.
2. Hasil menampilkan asumsi, estimasi nilai, kerugian, dan pelanggaran yang mungkin terjadi.
3. Lab memakai salinan keadaan yang terpisah dari permainan utama.
4. Pemain dapat mencoba strategi dan membandingkan hasil serta risiko.
5. Replay keputusan menampilkan urutan peristiwa dan transaksi yang sudah terjadi.
6. Pengujian ulang menyimpan dataset, seed, parameter, serta versi aturan.
7. Hasil lab tidak memberikan reward, fee karier, atau perubahan dana ke permainan utama.
8. Lab dan replay historis dibedakan: satu untuk eksperimen, satu untuk menjalankan periode data nyata.

**Kriteria penerimaan:** state ekonomi permainan utama sebelum dan sesudah eksperimen tetap sama, selain metadata eksperimen yang sengaja disimpan.

### FR-18 — UX 2D portrait

**Struktur navigasi yang diusulkan:**

| Menu | Isi utama |
|---|---|
| Beranda | Ringkasan hari, briefing, agenda, inbox, tugas, perkembangan karier |
| Pasar | Pencarian, watchlist, indeks, detail instrumen, order |
| Aset | Kas, posisi, laba/rugi, transaksi, FX |
| Firma | Karier, klien, mandat, fund builder, fee, kepatuhan |
| Riset | Berita, analis, kalender, data alternatif, paket terminal, lab |

**Perilaku wajib:**

1. Seluruh alur utama dapat diselesaikan dalam portrait.
2. Ruang dan navigasi 3D tidak digunakan.
3. Halaman tidak memaksa grafik, order book, berita, dan formulir tampil bersamaan.
4. Detail saham memakai susunan vertikal dan tab untuk informasi sekunder.
5. Tombol beli/jual mudah dijangkau dan tidak menutupi isi atau keyboard.
6. Target sentuh interaktif minimal setara 48 dp, termasuk pada layar ringkas.
7. Teks mengikuti pembesaran sistem tanpa memotong informasi transaksi penting.
8. Angka memuat mata uang dan unit; perubahan warna selalu disertai tanda, teks, atau ikon.
9. Loading, kosong, terkunci, gagal, diproses, dan berhasil memiliki tampilan yang jelas.
10. Error validasi tidak menghapus isi formulir.
11. Tombol Back menutup lapisan aktif sebelum keluar dari alur.
12. Perpindahan akun tidak boleh meninggalkan tiket yang tampak milik akun sebelumnya.
13. Mode internal dan historis selalu dapat dikenali.
14. Angka dari antarmuka contoh tidak boleh masuk ke permainan produksi.
15. Istilah gameplay mengikuti bahasa game, tetapi asal harga tetap jujur, misalnya “Pasar game” dan “Historis”.

**Kriteria penerimaan:** seluruh cakupan 26 layar konsep dapat diakses dan digunakan melalui susunan portrait, tanpa elemen penting tertutup safe area, keyboard, atau navigasi.

### FR-19 — Save, pemulihan, dan lifecycle

**Perilaku wajib:**

1. Save menyimpan mode, waktu, akun, order, pinjaman, karier, mandat, peristiwa, dan state ekonomi lain yang diperlukan untuk melanjutkan.
2. Save memiliki versi dan identitas dataset/aturan.
3. Penulisan gagal tidak merusak salinan valid terakhir.
4. Backup dan pemulihan tersedia.
5. Save korup tidak otomatis ditimpa permainan baru.
6. Migrasi mempertahankan data asli sampai hasil baru tervalidasi.
7. State yang dinyatakan berhasil tersimpan bertahan setelah aplikasi ditutup paksa.
8. Background/resume tidak menggandakan clock, NPC, penagihan, atau reward.
9. Order dan kewajiban setelah pemulihan mengikuti kebijakan yang dinyatakan; pembatalan diam-diam tidak diperbolehkan.
10. Kekurangan penyimpanan menampilkan kegagalan yang dapat ditindaklanjuti.

**Kriteria penerimaan:** skenario save/load, pembaruan versi, backup recovery, dan force-stop mempertahankan konsistensi ekonomi.

### FR-20 — APK dan kesiapan penggunaan

**Perilaku wajib:**

1. Menghasilkan APK yang dapat dipasang dan dijalankan tanpa lingkungan pengembangan.
2. Aset, font, dan komponen yang diperlukan tersedia secara lokal.
3. Dataset historis yang dibutuhkan tersedia sesuai kebijakan distribusi data yang dipilih.
4. Permainan dengan data yang sudah tersedia dapat berjalan offline sesuai baseline proyek.
5. Identitas `com.tradingglobal3d.mobile`, signing identity, dan data lama dipertahankan jika APK diberikan sebagai pembaruan aplikasi lama.
6. Full trimming atau perubahan runtime berisiko tidak boleh menghilangkan fungsi pemanggilan domain.
7. Paket disertai versi, identitas build, ukuran APK, dan catatan cakupan.
8. Hasil build, instalasi, emulator, dan perangkat fisik dilaporkan terpisah.

**Usulan definisi selesai produk:**

- Seluruh FR wajib memiliki bukti kelulusan.
- Tidak ada placeholder yang dipresentasikan sebagai fitur berfungsi.
- APK dapat dipasang dan dibuka.
- Alur trading dan karier dapat dijalankan.
- Pasar Indonesia/AS, FX, klien, fitur lanjutan, serta replay memiliki skenario penerimaan yang lulus.
- Save/load dan pemulihan tidak merusak progres.
- Tidak ada masalah kritis kehilangan data, transaksi ganda, atau saldo salah.
- Performa diukur terhadap ambang dan perangkat yang disepakati.

Pemilik baru menetapkan hasil akhir **APK**. Keharusan pengujian fisik, perangkat penerimaan, dan ambang performa final masih perlu ditetapkan; build saja tidak membuktikan kenyamanan di HP.

### Batas teknis lintas fitur

- Mesin ekonomi memiliki satu sumber kebenaran.
- Antarmuka tidak menghitung ulang aturan saldo, matching, fee, atau risiko sebagai mesin terpisah.
- Berdasarkan dokumen sebelumnya, baseline adalah domain C# dan host Android MAUI HybridWebView. Kesesuaiannya untuk versi baru belum diaudit melalui source code pada pekerjaan PRD ini.
- Tidak diperlukan Unity atau Three.js untuk target produk 2D.
- Nilai uang dan kuantitas menggunakan presisi serta pembulatan yang eksplisit.
- Instrumen dengan data atau aturan yang belum memadai tidak boleh diisi dengan angka seolah-olah terverifikasi.
- Semua fitur lama tetap wajib, meskipun penguncian berdasarkan karier diperbolehkan.

---

## 7. Sketsa data model

Model berikut bersifat konseptual. Ini bukan keputusan struktur database atau kewajiban membuat kelas terpisah untuk setiap baris.

**Aturan umum:**

- State ekonomi terkait dengan `run_id`.
- Identifier instrumen tidak hanya menggunakan ticker.
- Nilai uang menyertakan mata uang dan presisi tetap.
- Waktu kejadian ekonomi dibedakan dari waktu penyimpanan perangkat.
- Relasi lintas permainan dilarang mengubah state permainan lain.

| Entitas | Field kunci |
|---|---|
| PlayerProfile | `player_id`, nama, preferensi, pengaturan aksesibilitas |
| GameRun | `run_id`, `player_id`, mode, status, waktu_game, versi_aturan, `seed`, `dataset_id` |
| DatasetManifest | `dataset_id`, sumber, versi, periode, zona_waktu, resolusi, cakupan, checksum, catatan_hak_penggunaan, gap_data |
| Exchange | `exchange_id`, nama, mata_uang, zona_waktu, kalender, versi_aturan |
| Company | `company_id`, nama, sektor, metadata_sumber, tanggal_berlaku |
| Instrument | `instrument_id`, `company_id` opsional, bursa, ticker, jenis_aset, mata_uang, unit, lot, tick, status, masa_berlaku |
| InstrumentTerms | `instrument_id`, versi, settlement, biaya, ketentuan_kupon/jatuh_tempo atau kontrak, tanggal_efektif |
| Index | `index_id`, nama, jenis_internal/historis, metode, sumber, versi |
| IndexMembership | `index_id`, `instrument_id`, bobot, berlaku_mulai, berlaku_sampai |
| TradingSession | `session_id`, `run_id`, bursa, tanggal, fase, waktu_buka/tutup, status_halt |
| MarketState | `run_id`, `instrument_id`, harga_terakhir, referensi, volume, status, urutan_event, state_order_book |
| PriceSeries | `run_id/dataset_id`, `instrument_id`, waktu, timeframe, OHLCV, jenis_sumber, status_penyesuaian |
| Account | `account_id`, `run_id`, jenis_pribadi/klien/NPC, pemilik, `mandate_id`, status |
| CashBalance | `account_id`, mata_uang, total, tersedia, tertahan |
| Position | `account_id`, `instrument_id`, jumlah, biaya_perolehan, unit_tertahan, jumlah_short |
| Order | `order_id`, `command_id`, akun, instrumen, sisi, jenis, jumlah, harga_limit, harga_pemicu, masa_berlaku, status, jumlah_terisi, urutan |
| Fill | `fill_id`, `order_id`, lawan/model_eksekusi, waktu, jumlah, harga, biaya, referensi_penyelesaian |
| LedgerTransaction | `transaction_id`, `run_id`, jenis, waktu, sumber_peristiwa, kunci_idempotensi |
| LedgerEntry | `entry_id`, `transaction_id`, akun, mata_uang/aset, perubahan_nilai, kategori |
| FxQuote / FxConversion | pasangan, bid, ask, berlaku_sampai / akun, input, output, kurs_aktual, biaya, status |
| NpcProfile | `npc_id`, peran, strategi, akun, toleransi_risiko, parameter_perilaku, state_keputusan |
| MarketEvent | `event_id`, `run_id`, jenis, waktu_jadwal, waktu_rilis, cakupan, fakta, dampak, status |
| AnalystReport | `report_id`, analis, instrumen, waktu_terbit, rekomendasi, target, asumsi, hasil_evaluasi |
| AlternativeObservation | `observation_id`, sumber_lore/dataset, instrumen/sektor, waktu_observasi, waktu_tersedia, nilai, ketidakpastian |
| ResearchPlan / Subscription | paket, manfaat, harga, mata_uang, periode / akun, paket, awal, akhir, auto_renew, status |
| BillingRecord | `billing_id`, langganan, periode_tagih, nominal, status, `transaction_id` |
| CareerProgress / MissionProgress | pangkat, reputasi, pengalaman, statistik / misi, syarat, progres, periode, status_klaim |
| Client / Mandate | klien, profil, trust / akun, benchmark, batas_aset, konsentrasi, drawdown, leverage, likuiditas, horizon, tarif_fee |
| ClientCashFlow / FeeAssessment | mandat, jenis, jumlah, jadwal, status / periode, basis_fee, HWM, penyesuaian_arus_modal, nominal, transaksi |
| PortfolioReport | akun, periode, NAV_awal/akhir, arus_modal, hasil_investasi, biaya, drawdown, benchmark |
| FundPlan | `plan_id`, akun, versi, target_alokasi, kas_target, hasil_validasi, status_pelaksanaan |
| CorporateAction | `action_id`, instrumen, jenis, jadwal, ketentuan, aturan_kelayakan, status |
| CorporateEntitlement | aksi, akun, kepemilikan_acuan, hak, jumlah_digunakan, kas/unit_diselesaikan, status |
| Offering / Allocation | IPO/tender/M&A, ketentuan, tenggat / akun, pesanan, dana_tertahan, alokasi, refund, status |
| VotingAgenda / Ballot | resolusi, waktu_acuan, tenggat, aturan_hak / akun, hak, pilihan, jumlah_suara, waktu |
| MarginFacility / StockLoan | akun, limit, utang, bunga, margin, jaminan / instrumen, jumlah_pinjam, biaya, jatuh_tempo/recall, status |
| RiskIncident | akun, jenis, aturan, nilai_pemicu, waktu, tindakan, status_penyelesaian |
| ExperimentRun | `experiment_id`, sumber_run, snapshot_acuan, dataset, parameter, seed, hasil, tanpa_akses_tulis_ke_run_utama |
| Watchlist / InboxMessage | pemain/run, instrumen / sumber_event, waktu, isi, target_aksi, status_baca |
| SaveManifest | slot, `run_id`, versi_skema, waktu_checkpoint, checksum, dataset/aturan, lokasi_backup, status_validasi |

**Relasi utama:**

- Satu pemain dapat memiliki beberapa permainan.
- Satu permainan memiliki akun pribadi, akun klien, dan akun NPC.
- Satu mandat menghubungkan klien, akun, dan batas pengelolaannya.
- Satu order memiliki nol atau lebih fill.
- Satu penyelesaian ekonomi menghubungkan perubahan kas/aset melalui catatan transaksi.
- Satu aksi korporasi dapat menghasilkan hak pada banyak akun.
- Eksperimen memakai snapshot acuan dan tidak menulis ke akun permainan utama.

---

## 8. Edge case dan failure state

| Kasus | Perilaku yang diwajibkan |
|---|---|
| Ticker sama pada bursa berbeda | Gunakan identitas bursa dan instrumen; jangan menyatukan riwayat atau order |
| Dataset historis tidak ada, rusak, atau berbeda versi | Blokir sesi terkait dengan alasan jelas; jangan mengganti dengan data rekaan |
| Histori hanya OHLCV | Jangan menampilkan antrean asli atau kepastian eksekusi intrabar; nyatakan model dan batas resolusi |
| Stop dan take-profit sama-sama tersentuh dalam satu candle | Terapkan aturan urutan yang disepakati; jangan mengklaim urutan nyata tanpa data pendukung |
| Delisting, perubahan ticker, atau data masa lalu tidak lengkap | Pertahankan identitas historis; jelaskan cakupan dan pengaruhnya terhadap hasil |
| Harga adjusted bercampur dengan raw | Hindari split/dividen dihitung dua kali; manifest menyatakan basis data |
| Pasar tutup, libur, atau halt | Tolak/tunda tindakan sesuai aturan; jelaskan kapan dan bagaimana dapat diproses |
| Order besar melebihi likuiditas | Tampilkan partial fill dan status sisa; jangan memberi harga tetap untuk seluruh jumlah |
| Pembatalan bersamaan dengan fill | Batalkan hanya sisa yang masih ada setelah urutan kejadian ditetapkan |
| Double tap atau retry | Satu identitas permintaan hanya menghasilkan satu tindakan ekonomi |
| Saldo berubah setelah preview | Validasi ulang dan tampilkan perubahan; jangan memakai preview sebagai izin tanpa pemeriksaan |
| Pemain mengganti akun saat tiket terbuka | Minta peninjauan ulang akun dan preview sebelum submit |
| Stop-loss melewati gap harga | Eksekusi mengikuti aturan dan likuiditas, tanpa menjanjikan harga pemicu |
| Penarikan klien melebihi kas tersedia | Catat kewajiban dan proses pencairan; jangan menciptakan kas atau menjual pada harga fiktif |
| Short recall saat saham tidak likuid/halt | Kewajiban tetap ada; tampilkan proses serta konsekuensi sesuai aturan |
| Kupon, dividen, fee, atau reward diproses ulang | Gunakan identitas penyelesaian; tidak ada pencatatan ganda |
| Split menghasilkan fraksi | Terapkan aturan fraksi yang terlihat dan dapat direkonsiliasi |
| Rights atau penawaran kedaluwarsa ketika aplikasi ditutup | Gunakan kebijakan waktu yang dipilih secara konsisten; tampilkan hasil saat kembali |
| Save gagal karena ruang penuh | Beri status gagal; pertahankan state serta backup valid; jangan menyatakan berhasil |
| Aplikasi ditutup saat penyelesaian transaksi | Setelah pemulihan, transaksi selesai utuh atau belum diterapkan; tidak boleh setengah |
| Save lama tidak kompatibel | Jangan menimpa; sediakan penjelasan dan jalur pemulihan/migrasi |
| Pemain kehabisan modal atau reputasi turun | Tampilkan jalur pemulihan yang ditetapkan tanpa hadiah tersembunyi |
| Pembesaran teks atau keyboard mempersempit layar | Form tetap dapat digulir, dibaca, dan diselesaikan |
| Perangkat lambat saat banyak NPC aktif | Kurangi frekuensi pembaruan visual secara terukur tanpa mengubah aturan ekonomi diam-diam |
| Pembaruan APK memakai signing berbeda | Jangan mengarahkan penghapusan aplikasi lama sebagai langkah rutin; lindungi data dan jelaskan kegagalan pembaruan |

---

## 9. Success metrics

### 9.1 Metrik kelulusan produk

Target angka nol dan 100% berikut merupakan persyaratan integritas pada skenario penerimaan, bukan klaim hasil pengujian yang sudah dilakukan.

| Metrik | Target | Cara pengukuran |
|---|---|---|
| Cakupan fitur wajib | 100% kelompok MVP memiliki alur dan bukti penerimaan | Matriks FR-01–FR-20 dan cakupan 26 layar lama |
| Konsistensi ekonomi | 0 selisih di luar pembulatan yang didefinisikan | Rekonsiliasi kas, posisi, utang, biaya, dan ledger |
| Transaksi/fee/reward ganda | 0 pada skenario retry, reload, dan resume | Uji permintaan berulang dan pemulihan |
| Isolasi akun | 0 perubahan pada akun yang tidak terkait | Perbandingan state akun pribadi dan klien |
| Integritas save | 0 kehilangan state yang telah diakui tersimpan dalam skenario penerimaan | Force-stop, save gagal, backup, migrasi |
| Kebocoran masa depan | 0 pada skenario replay yang diaudit | Pemeriksaan waktu ketersediaan data dan laporan |
| Isolasi lab | 0 perubahan ekonomi pada permainan utama | Perbandingan state sebelum/sesudah eksperimen |
| Kesiapan APK | Build dan instalasi pada target yang disepakati lulus | Bukti paket, instalasi, peluncuran, serta journey |
| Kebenaran klaim data | Semua tampilan menyatakan mode/sumber dengan benar | Audit layar dan manifest dataset |
| Kelengkapan portrait | Semua alur wajib dapat dituntaskan dalam portrait | Pengujian layar, keyboard, Back, dan pembesaran teks |

### 9.2 Metrik UX

| Metrik | Definisi | Status target |
|---|---|---|
| Keberhasilan order pertama | Proporsi peserta yang dapat memilih saham, meninjau, dan mengirim order tanpa bantuan | TBD setelah target pengguna ditetapkan |
| Pemahaman status order | Proporsi peserta yang dapat membedakan pending, partial, filled, dan rejected | TBD |
| Kesalahan akun | Jumlah transaksi yang hampir/sudah dilakukan pada akun yang tidak dimaksud | Ukur; desain harus mencegah kebingungan |
| Penyelesaian siklus hari | Proporsi peserta yang menyelesaikan briefing sampai laporan | TBD |
| Pemahaman hasil | Proporsi peserta yang dapat menjelaskan perubahan portofolio menggunakan laporan | TBD |
| Pemahaman mandat | Proporsi peserta yang dapat menjelaskan batas utama klien sebelum menerima | TBD |
| Kemudahan navigasi | Keberhasilan menemukan portofolio, pembatalan order, laporan, dan mandat | TBD |

Persona dan manfaat belajar belum tervalidasi hanya dengan selesainya fitur. Diperlukan pengujian pengguna untuk menilai metrik ini.

### 9.3 Metrik “lebih ringan”

Wajib dilaporkan:

- Ukuran APK.
- Ruang instalasi dan data historis tambahan.
- Waktu buka sampai halaman dapat digunakan.
- Waktu membuka detail instrumen dan preview order.
- Penggunaan memori selama sesi.
- Kelancaran scroll, perpindahan halaman, dan pembaruan grafik.
- Stabilitas setelah background/resume.
- Konsumsi baterai/panas pada metode uji yang disepakati.

**Ambang kelulusan: TBD.** Angka target tidak ditetapkan hanya dari penghapusan 3D. Jika build lama dapat dipakai sebagai pembanding, pengukuran harus memakai perangkat, data, dan skenario yang setara.

### 9.4 Metrik keterlibatan

Jika produk nantinya dipakai lebih luas, dapat diukur:

- Penyelesaian tutorial.
- Kelanjutan bermain pada hari berikutnya.
- Pencapaian promosi pertama dan klien pertama.
- Penggunaan replay serta lab.
- Titik pemain berhenti karena bingung atau kehilangan tujuan.

Belum menjadi KPI rilis karena target distribusi dan pengguna belum ditentukan. Pengumpulan analitik daring tidak diasumsikan diperlukan; evaluasi dapat memakai catatan lokal dan sesi pengujian.

---

## 10. Open questions

Pertanyaan berikut adalah daftar keputusan yang masih terbuka. Jawaban tidak diisi menggunakan asumsi tersembunyi.

| ID | Keputusan yang belum ditetapkan | Dampak |
|---|---|---|
| OQ-01 | Apakah pengguna hanya pemilik, kelompok terbatas, atau publik? Seberapa paham mereka tentang saham? | Bahasa tutorial, kedalaman penjelasan, pengujian UX, dan distribusi |
| OQ-02 | Bursa, indeks, instrumen, dan jumlah emiten aktif apa saja yang wajib tersedia? Apakah seluruh anggota setiap indeks harus dapat diperdagangkan? | Batas cakupan data, beban pasar, dan penerimaan fitur global |
| OQ-03 | Dalam replay, apakah jalur harga harus persis historis dengan eksekusi pemain sebagai model terpisah, atau histori menjadi awal skenario yang kemudian dapat berubah? | Definisi replay, market impact, dan pembuktian realisme |
| OQ-04 | Dataset historis berasal dari mana, mencakup tahun berapa, dan beresolusi apa? Apakah tersedia transaksi individual, antrean, fundamental, dan berita bertanggal? | Fitur yang dapat direpresentasikan secara jujur |
| OQ-05 | Bagaimana hak penggunaan dan distribusi dataset untuk APK ditetapkan? Apakah data dibundel, diimpor, atau disediakan sebagai paket tambahan? | Ketersediaan offline, ukuran APK, dan distribusi |
| OQ-06 | Apakah waktu mengikuti dunia nyata atau waktu game? Apakah dapat dijeda/dipercepat? Apa yang terjadi saat background atau aplikasi ditutup? | Sesi, billing, bunga, agenda, dan fair play |
| OQ-07 | Berapa modal awal, biaya transaksi, spread FX, pajak game, settlement, serta aturan pembulatan? | Ekonomi dasar dan rekonsiliasi |
| OQ-08 | Jenis serta masa berlaku order apa yang dipilih? Bagaimana stop/TP, sisa market order, antrean saat halt, dan lelang diproses? | Kontrak eksekusi dan kasus ekstrem |
| OQ-09 | Berapa populasi NPC, ragam strategi, frekuensi keputusan, dan aturan pembentukan likuiditas? | Realisme, determinisme, dan performa |
| OQ-10 | Berapa jenjang, syarat promosi, reward, gaji, penalti, serta jalur pemulihan saat kehabisan modal? | Perjalanan karier dan keseimbangan game |
| OQ-11 | Bagaimana tarif fee, high-water mark, kebutuhan kas, kepercayaan, penarikan, dan penghentian mandat ditentukan? | Ekonomi firma dan konsekuensi klien |
| OQ-12 | Apa bentuk obligasi dan komoditas yang diperdagangkan? Bagaimana unit, kupon, jatuh tempo, settlement, dan konversinya? | Definisi multi-aset yang dapat diimplementasikan |
| OQ-13 | Kapan margin/short dibuka, berapa jaminan dan biaya, serta bagaimana recall dan likuidasi ditangani? | Risiko lanjutan dan potensi kerugian |
| OQ-14 | Apakah skenario korporasi, berita, dan IPO fiktif boleh dilekatkan pada perusahaan nyata dalam mode internal? | Konten dan pembedaan fakta historis dari dunia game |
| OQ-15 | Apa paket terminal, manfaat, harga USD game, periode, dan kebijakan perpanjangannya? | Akses informasi serta keseimbangan progres |
| OQ-16 | Perangkat Android mana yang menjadi target minimum dan penerimaan? Berapa batas APK, memori, waktu buka, dan kelancaran? | Definisi terukur “lebih ringan” |
| OQ-17 | Apakah progres APK lama wajib dapat dilanjutkan seluruhnya? Versi save dan APK lama mana yang menjadi acuan migrasi? | Pengujian kompatibilitas dan perlindungan data |
| OQ-18 | Apakah penerimaan akhir cukup APK terbangun, atau wajib terpasang dan lulus journey pada HP pemilik? | Batas serah terima dan bukti selesai |
| OQ-19 | Apakah seluruh fitur dapat dibuka bertahap melalui karier, atau harus ada mode bebas yang menyediakan semuanya sejak awal? | Onboarding, akses fitur, dan pengujian |
| OQ-20 | Apakah struktur lima menu dan arah visual 2D perlu penyesuaian preferensi khusus? | Finalisasi desain antarmuka |

**Keputusan yang menghalangi spesifikasi implementasi final:** cakupan data dan instrumen, perilaku replay, sistem waktu, parameter ekonomi, definisi instrumen multi-aset, aturan margin/short, serta perangkat dan kriteria penerimaan.

**Status akhir dokumen:** PRD mencakup seluruh fitur wajib yang diminta. Produk dan APK belum dibangun melalui pekerjaan ini. Estimasi jadwal, ukuran APK, dan performa belum dapat ditetapkan sebagai komitmen sebelum keputusan terbuka yang memengaruhinya diselesaikan.
