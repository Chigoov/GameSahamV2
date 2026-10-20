---
title: Konsep Game Trading Global 3D
aliases:
  - Game Trading Global
  - Futuristic Market Game
type: konsep-proyek
status: konsep-belum-diimplementasikan
platform-utama: Android
tags:
  - proyek/game
  - pasar-modal
  - trading
  - memori-agent
tanggal-dibuat: 2026-09-27
---

# Konsep Game Trading Global 3D

> Catatan ini adalah sumber acuan utama untuk desain game. Isinya merangkum keputusan, fitur yang telah dibicarakan, aturan pasar yang harus dijaga, dan hal yang masih perlu diputuskan. Ini dokumen konsep, bukan bukti bahwa aplikasi atau mekaniknya sudah dibuat.

## 1. Visi dan batas proyek

Game ini adalah permainan trading dan manajemen investasi bergaya futuristik dengan tampilan ruang pasar 3D. Pemain memulai dari akun pribadi berskala kecil, mempelajari cara kerja pasar, membangun reputasi, lalu dapat berkembang menjadi analis atau fund manager yang mengelola dana milik banyak klien dengan mandat berbeda.

Arah awalnya adalah Android. Game dibuat untuk dimainkan sendiri dan bersenang-senang; saat ini tidak ada rencana menerbitkannya di Play Store. Seluruh uang, biaya langganan, kontrak, transaksi, dan imbalan di dalam game adalah nilai permainan. Jangan menambahkan koneksi broker, rekening nyata, uang sungguhan, atau transaksi pasar sungguhan tanpa keputusan baru dari pemilik proyek.

Nuansa visual yang diminta: futuristik, berkelas, kaya animasi, tetapi tetap terbaca di layar ponsel. Efek 3D harus membantu pemain memahami keadaan pasar dan perkembangan karier, bukan menutupi angka atau membuat alur trading sulit disentuh.

Target cakupan pasar adalah beberapa indeks saham global, termasuk IHSG, beserta seluruh emiten anggota setiap indeks yang dipilih. Pemain dapat mengakses saham dalam mata uang asal bursa dan menukar rupiah ke dolar AS untuk kebutuhan pasar luar negeri. Untuk daftar saham Indonesia, pemilik proyek memilih halaman Daftar Saham BEI sebagai sumber awal. Daftar indeks global selain IHSG, riwayat keanggotaan dan bobot indeks, serta keputusan memakai harga eksternal atau harga yang dibuat oleh game masih perlu ditentukan.

### 1.1 Engine dan target Android awal

Pilihan awal engine adalah Unity 6.3 LTS dengan Universal Render Pipeline (URP), agar proyek berangkat dari jalur rilis LTS Unity yang masih didukung dan pipeline yang ditujukan untuk grafis lintas perangkat. Unity menyatakan Unity 6.3 LTS didukung sampai Desember 2027. [Rilis Unity 6](https://unity.com/releases/unity-6)

Target kerja minimum game: Android 11 atau lebih baru, ARM64, RAM 6 GB, GPU yang mendukung Vulkan atau OpenGL ES 3.1+, target 720p dan 30 FPS pada setelan grafis rendah-menengah. Target yang disarankan: Android 13 atau lebih baru, RAM 8 GB, GPU kelas menengah ke atas, dan ruang kosong sekitar 12 GB untuk instalasi serta build. 1080p dan 30–60 FPS adalah sasaran uji untuk perangkat lebih kuat, bukan jaminan sebelum profil di perangkat nyata.

Ini batas target yang dipilih untuk game 3D ini, bukan batas minimum Unity. Dokumentasi Unity 6.0 mencantumkan Android API 23+, 1 GB RAM, ARMv7/ARM64, dan OpenGL ES 3.0 atau Vulkan; kemampuan tersebut hanya menjelaskan kompatibilitas engine dan tidak menjamin mutu performa grafis game ini. [Persyaratan sistem Unity 6](https://docs.unity3d.com/6000.0/Documentation/Manual/system-requirements.html)

## 2. Status yang harus dipahami AI agent

- Belum ada proyek kode game yang dibuat dalam pekerjaan percakapan ini.
- Kerangka folder awal sudah dibuat di C:\Users\HYPE AMD\Documents\Codex\2026-09-27\beri\outputs\TradingGlobal3D. README di root menjelaskan susunan folder. Ini belum project Unity lengkap; Unity Hub perlu membuat metadata engine dan project settings.
- Gambar UI yang telah dibuat adalah papan konsep visual, bukan layar dari aplikasi yang sudah berfungsi.
- Jumlah layar konsep yang pernah ditampilkan adalah 26: dashboard, indeks, detail emiten, portofolio, order, berita, watchlist, misi, terminal riset, kalender, FX, fund manager, klien, konstruksi dana, aset lain, IPO/M&A, voting, data alternatif, kepatuhan, stress pasar, penghentian perdagangan, aksi korporasi, dan short squeeze.
- Angka harga, saldo, performa, biaya, dan contoh berita pada papan konsep tidak boleh disebut sebagai data terkini atau data nyata.
- Tidak ada hasil build, ukuran APK, ukuran instalasi, performa di perangkat Android, koneksi data pasar, atau perilaku mekanik yang sudah diverifikasi.
- Setiap AI agent harus membedakan dengan jelas antara konsep, kode yang benar-benar ada, dan hasil yang sudah diuji.

## 3. Prinsip produk dan bahasa antarmuka

1. Gunakan istilah yang jelas dan terasa seperti bagian dari game: pasar, saldo, harga, berita, agenda, portofolio, klien, dan dana.
2. Jangan gunakan kata “simulasi” pada UI, tutorial, berita, briefing, promosi, atau teks gameplay. Pemain sudah tahu bahwa ini game.
3. Jangan menyebut harga buatan game sebagai harga bursa terkini. Jika harga berasal dari aturan internal, label dan penjelasannya harus jujur.
4. Semua angka uang dalam layar harus menunjukkan mata uangnya. Harga terminal dan paket riset memakai dolar AS di dalam game, sementara dompet utama pemain dapat menyimpan rupiah dan dolar AS.
5. Informasi harus tetap mudah dibaca pada layar Android: teks, tombol, grafik, dan konfirmasi order punya ukuran sentuh yang wajar.
6. Animasi, efek ruang trading, berita, dan karakter NPC tidak boleh mengubah saldo atau harga tanpa peristiwa sistem yang tercatat.
7. Mekanik finansial penting harus dapat ditelusuri melalui riwayat transaksi dan catatan peristiwa.

## 4. Pemain, saldo awal, dan perkembangan karier

### 4.1 Awal permainan

Pemain memulai sebagai trader mandiri dengan akun pribadi, saldo awal, dan tutorial. Besar saldo awal belum ditetapkan. Tutorial dapat mengajarkan cara membaca indeks, memahami bid/ask, membuat order limit, memeriksa biaya, menukar IDR ke USD, dan melihat dampak transaksi di portofolio.

Agar pemain memiliki cara memperoleh uang sebelum modalnya berkembang, rancangan awal dapat menyediakan:
- imbalan untuk misi belajar yang selesai, misalnya membaca laporan emiten atau membuat portofolio sesuai batas risiko;
- kontrak tugas harian atau mingguan yang membayar setelah syaratnya terpenuhi;
- pekerjaan awal seperti asisten riset atau analis junior dengan gaji game dan tugas berkala;
- hadiah reputasi, pengalaman, akses fitur, dan bonus kecil yang tidak menciptakan uang tanpa batas.

Sumber imbalan, frekuensi, nilai, dan batasnya perlu diseimbangkan. Hindari satu tugas berulang yang memberi uang tak terbatas atau strategi pasti untung yang membuat trading tidak relevan.

### 4.2 Tahap karier

Arah perkembangan yang telah dibahas:
1. Trader pribadi pemula.
2. Trader pribadi dengan akses riset dan reputasi lebih baik.
3. Analis atau profesional junior yang membuka laporan, tugas, dan relasi klien.
4. Fund manager yang menerima mandat serta dana dari beberapa klien.
5. Manager dengan AUM, tim, reputasi, target, dan tanggung jawab risiko lebih besar.

Kenaikan tahap sebaiknya bergantung pada pengalaman, konsistensi, kepatuhan terhadap mandat, kualitas laporan, dan kepercayaan. Jangan menilai keberhasilan hanya dari keuntungan nominal; risiko dan penurunan nilai juga harus dihitung.

### 4.3 Akun pribadi dan akun klien

Akun pribadi pemain terpisah dari setiap portofolio klien. Masing-masing memiliki saldo kas, kepemilikan saham, nilai portofolio, laba/rugi, biaya, batas risiko, dan buku besar transaksi sendiri. Membeli aset untuk satu mandat tidak boleh mengubah saldo portofolio pribadi atau mandat lain.

Perpindahan dana hanya dapat terjadi jika aturan game memang mengizinkannya, dan harus tercatat sebagai transaksi yang jelas. AUM klien tidak boleh dicampur dengan kekayaan pribadi pemain.

## 5. Klien dan bisnis fund manager

Setiap klien NPC mempunyai tujuan, profil risiko, tolok ukur kinerja, horizon investasi, kebutuhan likuiditas, batas penurunan, dan instrumen yang diizinkan. Profil awal yang pernah dibahas mencakup:
- konservatif: menjaga modal dan kestabilan, toleransi risiko rendah;
- seimbang: membagi tujuan pertumbuhan dan kestabilan;
- pertumbuhan: menerima fluktuasi lebih besar demi pertumbuhan jangka panjang;
- agresif: toleransi volatilitas tinggi dan mandat yang lebih aktif.

Saat menerima klien, pemain menyetujui mandat yang memuat benchmark, batas konsentrasi, batas leverage jika dibuka, daftar aset yang boleh dibeli, batas penurunan, kebutuhan kas, dan jadwal laporan. Order yang melanggar mandat harus diberi peringatan atau ditolak.

Klien bereaksi terhadap performa, risiko, kepatuhan, komunikasi, dan kondisi portofolio. Mereka dapat menambah dana, bertahan, meminta penjelasan, atau menarik sebagian dana. Penarikan besar dapat mengharuskan manager menyediakan kas atau menjual aset; penjualan itu masuk ke buku order pasar dan dapat menimbulkan slippage.

Imbalan fund manager dapat terdiri atas gaji, biaya pengelolaan, atau bagian kinerja. Persentase, waktu penagihan, dan aturan seperti high-water mark belum diputuskan. Jika biaya berbasis hasil dipakai, perhitungan harus transparan agar klien tidak membayar berulang untuk memulihkan kerugian lama.

Dasbor fund manager perlu memisahkan:
- AUM total dan AUM per klien;
- kas dan aset setiap mandat;
- laba/rugi dan kinerja relatif terhadap benchmark;
- volatilitas, penurunan maksimum, konsentrasi, eksposur sektor, dan kebutuhan likuiditas;
- tingkat kepercayaan klien, permintaan penjelasan, arus dana masuk/keluar;
- pelanggaran mandat, peringatan risiko, biaya, dan riwayat keputusan.

## 6. Pasar, indeks, emiten, dan aset

### 6.1 Cakupan instrumen

Pusat pasar menampilkan IHSG dan indeks global yang dipilih. Setiap indeks mempunyai daftar anggota lengkap, bobot, perubahan anggota, serta halaman eksplorasi. Emiten dapat menjadi anggota lebih dari satu indeks jika sesuai dengan struktur data.

Arah multi-aset yang pernah dibahas mencakup:
- saham;
- obligasi;
- komoditas;
- valuta asing.

Instrumen tambahan dapat dibuka bertahap. Jangan menambahkan derivatif atau produk kompleks sebelum kebutuhan, aturan risiko, dan dampak pada order engine jelas.

### 6.2 Informasi setiap emiten

Halaman emiten dapat memuat:
- kode ticker, nama, bursa, mata uang, sektor, dan indeks yang diikuti;
- grafik harga dan volume;
- harga terakhir, perubahan, bid/ask, spread, dan kedalaman order book;
- kapitalisasi, valuasi, laporan keuangan, pendapatan, laba, utang, arus kas, dan panduan perusahaan;
- berita, agenda, pengumuman, aksi korporasi, konsensus analis, dan faktor risiko;
- kepemilikan pemain, biaya transaksi, laba/rugi, dan order yang masih aktif.

Skema instrumen perlu menyimpan ukuran lot, tick size, jam perdagangan, mata uang, biaya, batas harga, likuiditas, volume normal, volatilitas, bobot indeks, serta riwayat aksi korporasi. Ketentuan per negara sebaiknya menjadi parameter game dan tidak diklaim sebagai aturan hukum nyata bila belum diverifikasi.

### 6.3 Perhitungan indeks

Indeks mengikuti pergerakan emiten anggotanya sesuai bobot yang dipakai indeks. Jika ada emiten besar bergerak, pengaruhnya dapat lebih besar daripada emiten kecil. Perubahan harga satu emiten harus tercermin di seluruh indeks yang memilikinya. Aksi seperti stock split atau perubahan anggota perlu menyesuaikan nilai indeks secara konsisten agar tidak terjadi lonjakan palsu.

### 6.4 Sumber daftar saham Indonesia

Sumber awal yang dipilih pemilik proyek adalah [Daftar Saham BEI](https://www.idx.co.id/id/data-pasar/data-saham/daftar-saham). Pengambilan data pada 27 September 2026 menerima 962 baris dari tabel halaman tersebut. Snapshot yang diambil tersimpan di [[Resources/Data/Daftar Saham BEI 2026-09-27.csv|CSV daftar saham BEI 27 September 2026]].

Kolom yang diterima: nomor baris, kode saham, nama perusahaan, tanggal pencatatan, jumlah saham, dan papan pencatatan. Tidak ada kolom sektor pada respons tabel yang diambil, walaupun halaman menyediakan filter sektor. Jika game memerlukan sektor lengkap, ambil dan cocokkan dari sumber resmi BEI yang sesuai. Nilai papan pencatatan disimpan mengikuti respons sumber.

Halaman ini adalah daftar saham tercatat Indonesia; halaman ini bukan daftar konstituen dan bobot untuk semua indeks global. Untuk indeks yang anggota atau bobotnya tidak tersedia di sini, gunakan sumber resmi penyedia indeks masing-masing. Snapshot ini bukan feed harga langsung; gunakan mesin harga internal game sampai pemilik memilih feed eksternal. Refresh snapshot saat memperbarui basis emiten dan simpan tanggal pembaruannya.

BEI memisahkan layanan data pasar dan lisensi indeks. Untuk penggunaan lokal pribadi, catatan ini hanya menyimpan snapshot daftar yang diunduh dari halaman publik. Jangan menganggap hak redistribusi, publikasi, atau pemakaian komersial otomatis termasuk; periksa syarat BEI dan penyedia indeks jika arah penggunaan berubah.

Rujukan resmi terkait produk data: [IDX Data Services](https://www.idx.id/en/products/idx-data-services/) dan [S&P DJI Data & Index Licensing](https://www.spglobal.com/spdji/en/about-us/data-index-licensing/).

## 7. Mesin perdagangan: aturan utama

Mekanik perdagangan adalah bagian inti proyek. Semua order pemain, NPC investor, analis yang berdagang, dan akun klien memakai satu alur eksekusi dan buku pasar bersama. Jangan membuat harga terpisah per layar, akun, atau NPC.

### 7.1 Jenis order

Rancangan order yang dapat dibangun:
- market order: berusaha segera membeli dari ask atau menjual ke bid yang tersedia;
- limit order: hanya dapat dieksekusi pada harga batas atau lebih baik, menunggu jika belum ada lawan yang sesuai;
- stop order atau stop-loss: aktif setelah harga pemicu tercapai, lalu masuk ke mekanisme order yang ditentukan;
- take-profit: memicu penjualan atau pembelian ketika sasaran harga terpenuhi;
- short selling: fitur lanjutan yang hanya dibuka jika mekanik peminjaman saham, margin, biaya pinjam, dan risiko forced buy-in sudah tersedia.

Masa berlaku order, sesi perdagangan, pembatalan, lot minimum, presisi harga, biaya, pajak, dan aturan pasar masing-masing perlu diperiksa sebelum order diterima.

### 7.2 Buku order dan pencocokan

Buku pasar menyimpan bid (permintaan beli) dan ask (penawaran jual) pada beberapa tingkat harga, termasuk jumlah yang tersedia pada tiap tingkat. Order market membeli dengan menghabiskan ask terbaik terlebih dahulu; order market menjual dengan menghabiskan bid terbaik terlebih dahulu.

Jika jumlah yang tersedia tidak cukup, order hanya terisi sebagian. Bagian yang belum terpenuhi mengikuti aturan sisa order, misalnya tetap terbuka, dibatalkan, atau ditolak sesuai jenis order dan sesi. Setiap eksekusi menghasilkan harga, jumlah, biaya, dan pihak lawan yang tercatat.

Harga rata-rata transaksi dihitung dengan volume-weighted average price (VWAP): total nilai seluruh bagian yang terisi dibagi total jumlah yang terisi. Konfirmasi sebelum order dikirim menampilkan perkiraan VWAP, biaya, slippage, jumlah yang mungkin tidak terisi, dan risiko order hanya terisi sebagian.

### 7.3 Dampak order besar dan likuiditas

Dampak transaksi bergantung pada ukuran order dibandingkan kedalaman buku order dan volume normal saham, bukan hanya nominal rupiah atau dolar. Saham dengan likuiditas tinggi dapat menyerap order besar lebih baik; saham tipis dapat bergerak jauh dengan order yang lebih kecil.

Contoh penjualan besar:
1. Order jual market mengambil bid tertinggi yang tersedia.
2. Jika jumlah jual lebih besar daripada kuantitas di bid itu, eksekusi berlanjut di bid yang lebih rendah.
3. Harga rata-rata jual menjadi lebih rendah dari harga bid pertama; selisih ini adalah slippage.
4. Kedalaman sisi bid berkurang. Spread dapat melebar jika pembeli lain tidak segera mengganti likuiditas.
5. Ketidakseimbangan order jual dan kedalaman yang tipis dapat meningkatkan volatilitas.
6. Jika harga melewati pemicu stop-loss, order tambahan dapat muncul. Jika posisi memakai margin, penurunan dapat memicu margin call atau penjualan paksa.
7. Jika tekanan ekstrem, aturan batas harga atau penghentian perdagangan dapat aktif.

Order besar tidak boleh otomatis mengguncang semua saham. Besar dampak mengikuti likuiditas instrumen, kondisi pasar, jenis order, jumlah yang benar-benar dieksekusi, dan peristiwa yang sedang berlangsung.

### 7.4 Harga transaksi, dampak sementara, dan informasi baru

Hanya transaksi yang benar-benar terjadi yang mengubah harga transaksi terakhir. Limit order yang belum terisi dapat mengubah tampilan kedalaman dan peluang eksekusi, tetapi tidak boleh sendirian mengubah harga terakhir.

Tekanan order dapat mendorong harga sementara. Jika tekanan mereda dan tidak ada informasi baru, pasar dapat pulih sebagian secara bertahap. Berita atau perubahan fundamental—misalnya laba, suku bunga, atau kejadian operasional—dapat mengubah perkiraan nilai untuk waktu yang lebih panjang. Pisahkan dampak sementara dari perubahan nilai karena informasi supaya satu order besar tidak menghasilkan lonjakan permanen yang tidak masuk akal.

Jika pencocokan order sudah menggerakkan harga melalui tingkat buku, jangan menambahkan dampak harga kedua secara terpisah atas order yang sama. Hindari menghitung market impact dua kali.

### 7.5 Alur pemrosesan order

Satu siklus order yang dapat dipakai AI agent:
1. Validasi instrumen, sesi, saldo atau kepemilikan, lot, harga, mandat klien, margin, dan status pasar.
2. Catat order dengan status dibuat, ditolak, terbuka, terisi sebagian, terisi penuh, atau dibatalkan.
3. Cocokkan order terhadap sisi lawan berdasarkan aturan harga dan antrean.
4. Catat setiap fill, biaya, pajak game, slippage, dan VWAP.
5. Perbarui kas dan kepemilikan akun secara atomik agar tidak terjadi saldo setengah diperbarui.
6. Perbarui buku order, volume, harga transaksi terakhir, volatilitas, dan indeks yang terpengaruh.
7. Jalankan aturan stop-loss, margin, forced liquidation, short covering, serta penghentian perdagangan bila pemicu benar-benar tercapai.
8. Perbarui nilai portofolio pribadi dan portofolio klien secara terpisah.
9. Simpan jejak audit yang menghubungkan order, transaksi, perubahan saldo, dan peristiwa pasar.

## 8. Fenomena pasar dan hubungan sebab-akibat

Harga perlu bereaksi terhadap gabungan arus order, likuiditas, informasi, kondisi ekonomi, dan perilaku pelaku pasar. Peristiwa dapat menimbulkan efek berantai, tetapi jangan membuat setiap berita menggerakkan semua instrumen secara acak.

### 8.1 Faktor makro dan lintas aset

Peristiwa makro yang dapat memengaruhi beberapa pasar:
- keputusan suku bunga dan pernyataan bank sentral;
- inflasi, pertumbuhan ekonomi, data tenaga kerja, kebijakan fiskal, dan sentimen risiko;
- perubahan kurs, arus modal, harga obligasi, dan imbal hasil;
- komoditas atau gangguan rantai pasok yang mengubah biaya produksi;
- ketegangan geopolitik, sanksi, bencana, atau gangguan logistik.

Contoh hubungan: kenaikan suku bunga dapat mengubah nilai tukar dan imbal hasil obligasi, menekan valuasi sebagian perusahaan, menggeser arus dana antarsektor, lalu memengaruhi indeks dan portofolio klien. Hubungan harus ditentukan menurut eksposur emiten, bukan dipukul rata.

### 8.2 Peristiwa emiten

Kejadian yang dapat memengaruhi perusahaan:
- laporan laba di atas atau di bawah perkiraan;
- perubahan panduan pendapatan, margin, utang, atau belanja modal;
- peluncuran produk, kehilangan kontrak, pergantian pimpinan, atau gangguan produksi;
- dividen, stock split, rights issue, buyback, dan aksi korporasi lain;
- IPO, merger, akuisisi, restrukturisasi, dan perubahan komposisi pemegang saham.

Reaksi dipengaruhi oleh tingkat kejutan dibanding perkiraan. Hasil bagus yang sudah diantisipasi dapat memberi respons kecil; hasil cukup baik tetapi jauh di bawah ekspektasi dapat menekan harga. Narasi “buy the rumor, sell the news” dapat muncul sebagai perilaku NPC, bukan aturan otomatis.

### 8.3 Rezim pasar, korelasi, dan volatilitas

Pasar dapat berada dalam keadaan tren naik, tren turun, bergerak datar, atau stres. Keadaan tersebut mengubah likuiditas, spread, volatilitas, korelasi aset, dan kecenderungan NPC. Saat stres, aset yang biasanya bergerak berbeda dapat turun bersama karena investor mengejar kas.

Volatilitas dapat meningkat dari perubahan harga, order imbalance, kedalaman menipis, berita, gap pembukaan, pemicu stop, margin call, atau short covering. Gunakan batas agar rangkaian reaksi tidak menghasilkan harga negatif, nilai ekstrem tanpa dasar, atau loop pemicu tak berujung.

### 8.4 NPC investor dan analis

NPC mempunyai gaya dan batas perilaku yang berbeda: konservatif, pencari pertumbuhan, penilai fundamental, pengikut tren, penyedia likuiditas, atau spekulan. Mereka menilai berita, valuasi, momentum, dan risiko dengan cara berbeda, sehingga tidak semuanya menjual atau membeli serentak.

Profil NPC dapat menyimpan strategi, aset yang disukai, toleransi risiko, horizon, modal, rekam transaksi, opini, prediksi, dan tingkat keberhasilan. Prediksi dapat salah. Riwayat keberhasilan memengaruhi reputasi NPC dan seberapa besar pemain mempercayai analisisnya.

### 8.5 Reaksi berantai

Contoh runtutan yang perlu didukung:
- laporan buruk → penjualan investor → bid menipis → slippage dan volatilitas naik;
- harga turun ke stop-loss → order jual tambahan → tekanan turun;
- ekuitas akun bermargin turun → margin call → posisi dipaksa dikurangi;
- banyak posisi short menutup bersamaan → pembelian kembali → harga melonjak dan terjadi short squeeze;
- krisis pada komponen penting → biaya atau produksi beberapa emiten berubah → sektor dan indeks terkait ikut bereaksi;
- penarikan dana klien → kebutuhan kas manager → penjualan aset yang masuk ke pasar yang sama.

Setiap reaksi harus punya pemicu, urutan, dan dampak yang bisa diperiksa. Untuk menghindari loop, gunakan batas jumlah aktivasi, urutan waktu yang konsisten, dan status pasar seperti halt saat kondisi ekstrem.

## 9. Pengelolaan risiko, halt, dan kepatuhan

### 9.1 Radar risiko dan stress test

Sebelum pemain melanjutkan hari atau periode perdagangan, radar risiko menampilkan potensi dampak skenario, seperti:
- pasar turun tajam;
- IDR melemah terhadap USD;
- suku bunga naik;
- sektor tertentu terpukul;
- likuiditas satu emiten menipis;
- sebagian klien meminta pencairan;
- beberapa stop-loss atau margin call terpicu.

Skenario what-if berjalan pada salinan keadaan pasar dan portofolio. Hasilnya tidak mengubah saldo, posisi, atau buku order utama. Layar harus menunjukkan asumsi skenario dan batas perkiraan, bukan menjanjikan hasil pasti.

### 9.2 Circuit breaker dan penghentian perdagangan

Jika perubahan harga atau tekanan pasar melewati aturan tertentu, instrumen atau pasar dapat memasuki status peringatan, jeda perdagangan, atau halt. Halaman halt menampilkan alasan, instrumen terdampak, waktu mulai, kondisi pembukaan kembali, dan status antrean order.

Setelah halt, pembukaan dapat memakai proses lelang pembukaan agar order menumpuk dan harga awal ditentukan lebih teratur. Threshold, durasi, dan cara pembukaan harus bisa ditentukan per pasar; nilai final belum dipatok.

### 9.3 Margin, forced liquidation, dan short squeeze

Jika fitur leverage dibuka, akun mempunyai nilai ekuitas, persyaratan margin awal, maintenance margin, biaya pinjaman, dan mekanisme margin call. Ketika jaminan tidak cukup, pemain diberi pemberitahuan dan waktu atau aturan untuk menambah dana; jika tidak, posisi tertentu dilikuidasi sesuai kebijakan game.

Short selling memerlukan inventori pinjaman dan biaya. Jika harga naik atau saham sulit dipinjam, pemain short dapat menutup posisi. Pembelian kembali serentak meningkatkan permintaan dan bisa memicu short squeeze. Fitur short tidak boleh aktif sebelum proses pinjam, risiko kerugian, dan forced buy-in tersedia.

### 9.4 Compliance dan audit

Meja kepatuhan mencatat keputusan besar, transaksi mencurigakan, order yang dibatalkan, konflik mandat, penggunaan informasi, dan batas eksposur. Fitur ini adalah mekanik game untuk mengajarkan tata kelola; jangan menampilkan hasilnya sebagai penilaian hukum nyata.

## 10. Berita, riset, langganan terminal, dan alternatif data

### 10.1 Terminal riset bergaya Bloomberg

Game menyediakan terminal riset premium yang terinspirasi fungsi terminal profesional: berita, harga dan volume, kalender, profil emiten, laporan, konsensus analis, ringkasan sektor, peringatan, dan kedalaman order. Nama, logo, tampilan, konten, dan data milik pihak nyata tidak boleh disalin begitu saja.

Langganan terminal dibayar dengan dolar game. Paket dapat dibedakan sebagai dasar, profesional, dan institusional, dengan perbedaan pada kedalaman data, laporan, jumlah alert, riset, dan akses ke meja tertentu. Harga paket, lama periode, dan manfaat final belum ditetapkan. Langganan hanya membuka informasi dan alat; tidak menjamin keuntungan.

### 10.2 Briefing pasar pintar

Pemain dapat bertanya, misalnya, “Kenapa portofolioku turun hari ini?” Briefing merangkum penyebab yang memang tercatat di dalam game: saham yang bergerak, kontribusi posisi, berita yang cocok, perubahan kurs, order yang dieksekusi, dan kondisi sektor.

Briefing harus menunjuk peristiwa yang menjadi sumbernya dan membedakan fakta dalam game dari dugaan. Jika tidak ada peristiwa yang cukup menjelaskan gerak harga, briefing harus mengatakannya daripada mengarang penyebab.

### 10.3 Konsensus analis dan data alternatif

Meja riset menampilkan perkiraan analis NPC, rentang target, perubahan rekomendasi, serta rekam akurasi. Meja data alternatif dapat menyajikan indikator fiktif yang terhubung ke dunia game, seperti tren permintaan produk, antrean distribusi, sentimen berita, atau perubahan aktivitas sektor.

Setiap indikator harus memiliki sumber di dalam lore game, cakupan, waktu pengamatan, dan tingkat ketidakpastian. Jangan menjadikan indikator sebagai jawaban pasti atau memasukkannya tanpa hubungan yang masuk akal ke emiten.

### 10.4 Kalender pasar dan agenda emiten

Kalender menggabungkan sesi bursa, hari libur, laporan laba, rapat kebijakan, pembagian dividen, IPO, M&A, voting pemegang saham, dan jadwal penting lain. Agenda bisa memicu notifikasi dan persiapan portofolio.

## 11. Fitur strategis dan sosial di dalam game

- Lab strategi untuk mencoba ide pada salinan pasar atau periode sebelumnya, membandingkan hasil dan risiko, lalu menyimpan strategi.
- Putar ulang pasar untuk mempelajari urutan peristiwa, order, dan reaksi harga tanpa mengubah portofolio utama.
- Voting pemegang saham untuk emiten yang memiliki agenda, dengan pilihan yang dapat memengaruhi hasil perusahaan atau reputasi pemain.
- IPO dan M&A room untuk menilai valuasi, membaca dokumen penawaran, menempatkan order, menilai risiko, dan mengikuti hasil transaksi.
- Aksi korporasi untuk melihat dampak dividen, stock split, rights issue, buyback, dan merger pada jumlah unit serta nilai posisi.
- Profil analis dan investor NPC dengan catatan prediksi, transaksi, reputasi, dan gaya.
- Ruang trading 3D yang berubah pencahayaan, tampilan kota, suara, dan suasananya mengikuti keadaan pasar serta reputasi pemain.

Efek visual memberi umpan balik suasana. Harga dan saldo tetap ditentukan oleh mesin pasar dan transaksi yang tercatat.

## 12. Peta layar UI yang telah dibahas

Papan konsep visual sebelumnya mencakup layar-layar berikut. Ini daftar kebutuhan desain, bukan klaim bahwa layar telah tersedia di aplikasi.

### Layar 1–8: alur trading awal

1. Dashboard pasar global: indeks pilihan, perubahan, watchlist, saldo IDR/USD, berita utama, kalender, ringkasan portofolio, dan akses cepat ke order.
2. Penjelajah indeks: daftar indeks global termasuk IHSG, performa, sektor, dan pencarian seluruh anggota.
3. Detail emiten: grafik, harga, volume, bid/ask, kedalaman, berita, laporan, konsensus, agenda, risiko, dan tombol beli/jual.
4. Portofolio pribadi: nilai total, kas per mata uang, aset, laba/rugi, histori, alokasi, dan eksposur.
5. Tiket order: beli/jual, jenis order, jumlah, harga batas, estimasi VWAP, biaya, slippage, dampak order, sisa yang mungkin tidak terisi, dan konfirmasi.
6. News & events: berita berdasarkan emiten, sektor, indeks, tingkat dampak, waktu, serta hubungan dengan aset.
7. Watchlist: harga, perubahan, alert, berita terkait, dan perbandingan beberapa instrumen.
8. Misi dan karier: tugas belajar, imbalan, reputasi, pengalaman, dan syarat karier berikutnya.

### Layar 9–16: terminal, mata uang, dan pengelolaan dana

9. Paket terminal riset: tingkat akses, manfaat, harga dolar game, periode, dan status langganan.
10. Terminal pasar: order book, volume, harga, transaksi terakhir, spread, dan alat charting.
11. Riset emiten: laporan, konsensus, estimasi, target analis, risiko, dan riwayat akurasi.
12. Kalender pasar: jam bursa, hari libur, agenda makro, earnings, aksi korporasi, IPO, dan voting.
13. Ringkasan langganan dalam USD: paket aktif, biaya berikutnya dalam game, fitur yang terbuka, dan riwayat pembayaran game.
14. Penukaran IDR/USD: kurs, spread, biaya, jumlah yang diterima, saldo setelah konversi, dan konfirmasi.
15. Dasbor fund manager: AUM, performa tiap mandat, benchmark, risiko, arus dana, kepercayaan klien, dan tugas.
16. Detail mandat klien: tujuan, risiko, batas aset, benchmark, horizon, likuiditas, biaya, laporan, dan status kepatuhan.

### Layar 17–22: dana dan tata kelola

17. Fund builder: susun alokasi aset, target risiko, kas, sektor, dan benchmark; tampilkan perkiraan risiko.
18. Meja multi-aset: navigasi saham, obligasi, komoditas, dan FX dengan harga serta mata uang masing-masing.
19. IPO & M&A room: penawaran, valuasi, dokumen, jadwal, order, risiko, serta status transaksi.
20. Voting pemegang saham: agenda, pilihan, jumlah hak suara, tenggat, dan hasil.
21. Meja data alternatif: indikator khusus dunia game, waktu, sumber internal, relevansi, dan tingkat keyakinan.
22. Compliance & audit: batas mandat, peringatan, transaksi, keputusan, dan jejak audit.

### Layar 23–26: kondisi pasar ekstrem

23. Market pulse dan stress: keadaan pasar, risiko portofolio, skenario, paparan sektor, kurs, serta akibat yang diperkirakan.
24. Jeda perdagangan: status halt, alasan, hitung mundur, instrumen terdampak, dan proses pembukaan kembali.
25. Aksi korporasi: jenis aksi, tanggal, posisi yang terpengaruh, unit baru, kas, dan dampak nilai.
26. Short squeeze: posisi short, jumlah saham pinjaman, biaya, tekanan pembelian kembali, risiko, dan harga likuidasi.

## 13. Mata uang, biaya, dan pembukuan

Dompet pemain menyimpan IDR dan USD. Penukaran menampilkan kurs yang berlaku di game, arah transaksi, spread, biaya, jumlah sebelum/sesudah, dan waktu kuotasi. Perubahan kurs dapat memengaruhi nilai rupiah dari aset USD ketika portofolio diringkas dalam IDR.

Order asing menggunakan mata uang instrumen. Jika saldo USD tidak cukup, pemain perlu menukar dana terlebih dahulu atau memakai mekanik pembiayaan yang sudah dibuka. Jangan menggunakan kurs saat ini dari dunia nyata kecuali sumber dan cara pembaruannya benar-benar diputuskan.

Semua perubahan uang harus tercatat sebagai ledger: saldo awal, konversi, biaya, hasil transaksi, gaji, reward, dividen, biaya langganan game, imbalan manager, dan arus dana klien. Riwayat ledger menjadi sumber utama untuk menghitung saldo, bukan angka UI yang dapat berubah sendiri.

## 14. Data dan struktur konsep untuk AI agent

Entitas domain yang kemungkinan diperlukan ketika proyek kode dimulai:
- instrumen: ticker, bursa, mata uang, sektor, lot, tick, jam, likuiditas, volatilitas, bobot indeks;
- indeks dan keanggotaan indeks: anggota, bobot, tanggal berlaku;
- order: akun, instrumen, sisi, tipe, jumlah, batas harga, status, waktu;
- fill/transaksi: order asal, harga, jumlah, biaya, waktu, lawan;
- buku order: bid/ask bertingkat, kuantitas, antrean;
- akun/portofolio: mata uang kas, posisi, nilai, ledger;
- klien dan mandat: profil, benchmark, batas risiko, instrumen yang diizinkan, biaya;
- peristiwa pasar: waktu, pemicu, instrumen/sektor terdampak, besaran, durasi;
- berita/riset: sumber internal, fakta, ekspektasi, kejutan, tingkat ketidakpastian;
- NPC: gaya, strategi, posisi, modal, keyakinan, dan riwayat;
- langganan: paket, biaya game, periode, akses;
- kalender, aksi korporasi, dan catatan audit.

Arsitektur awal sebaiknya memisahkan aturan pasar dari visual 3D. Mesin pasar menjadi sumber tunggal untuk harga dan eksekusi; UI hanya membaca keadaan pasar dan mengirim aksi pemain melalui aturan yang sama. Simpan data indeks/emiten sebagai data, bukan layar yang dibuat manual. Perubahan harga, order, dan ledger harus dapat diuji tanpa menjalankan seluruh ruang 3D.

## 15. Invarian yang tidak boleh dilanggar

1. Semua akun dan NPC bertransaksi pada pasar bersama untuk instrumen yang sama.
2. Order yang belum dieksekusi tidak mengubah harga transaksi terakhir.
3. Order besar dinilai relatif terhadap buku order dan volume normal, bukan nominalnya saja.
4. Fill, saldo kas, kepemilikan, biaya, dan ledger diperbarui konsisten dalam satu operasi.
5. Kas pribadi, kas klien, dan portofolio tiap mandat terpisah.
6. Dampak transaksi yang sudah tercermin dari pencocokan buku tidak dihitung lagi sebagai market impact kedua.
7. Harga indeks diturunkan dari anggota dan bobotnya.
8. Perubahan fundamental dan tekanan order sementara adalah dua pengaruh yang berbeda.
9. Skenario lab dan replay tidak mengubah permainan utama.
10. Briefing merujuk peristiwa dan transaksi yang tercatat; jangan membuat sebab tanpa bukti di dunia game.
11. Biaya terminal adalah pembayaran dalam game, bukan tagihan layanan nyata.
12. Jangan klaim data langsung, data resmi, atau harga terkini sebelum sumbernya nyata dan terverifikasi.

## 16. Urutan pengerjaan yang disarankan

Cakupan akhir tetap menargetkan indeks global dan seluruh emitennya. Untuk memvalidasi aturan tanpa membangun semuanya sekaligus, mulai dari potongan kecil yang dapat diuji, lalu perluas datanya:
1. Tetapkan aturan satu bursa, instrumen, jam, tick, lot, biaya, dan contoh akun.
2. Buat pencocokan market/limit order, partial fill, VWAP, slippage, dan ledger.
3. Masukkan NPC dan akun klien ke buku pasar yang sama.
4. Tambahkan spread, kedalaman, volatilitas, market impact, stop-loss, margin, halt, dan mekanisme pemulihan.
5. Tambahkan berita, faktor fundamental, makro, FX, komoditas, obligasi, dan hubungan indeks.
6. Tambahkan dompet IDR/USD, terminal berlangganan, career progression, mandat, fee, laporan, dan reaksi klien.
7. Masukkan daftar indeks dan anggota lengkap dengan metode pembaruan yang jelas.
8. Hubungkan layar-layar utama ke aturan yang sudah ada; baru lanjutkan animasi ruang trading dan aset 3D.
9. Optimalkan kualitas grafis, ukuran paket, dan performa pada perangkat Android yang memang akan dipakai.

Urutan ini adalah cara memvalidasi bagian paling berisiko lebih dulu. Jangan menyebut prototipe kecil sebagai cakupan pasar global lengkap.

## 17. Perkiraan ukuran game

Belum ada build untuk mengukur ukuran nyata. Perkiraan kasar untuk perencanaan:
- prototipe Android dengan beberapa lingkungan dan aset sederhana: sekitar 300 MB–1 GB;
- game dengan kualitas 3D yang lebih matang, banyak layar, lingkungan, efek, dan audio: sekitar 1,5–4 GB;
- aset resolusi sangat tinggi, banyak lokasi, audio/voice, dan sinematik dapat mendorongnya ke sekitar 4–8 GB atau lebih.

Rentang ini bukan hasil pengukuran APK. Aset 3D, tekstur, animasi, audio, dan video biasanya jauh lebih memengaruhi ukuran daripada daftar ticker, angka harga, atau metadata emiten. Ukuran hasil akhir baru dapat dilaporkan setelah ada build Android; bedakan ukuran file unduhan dari ruang instalasi dan data tambahan.

## 18. Keputusan yang masih terbuka

Jangan mengisi hal-hal ini dengan asumsi diam-diam:
- daftar dan urutan indeks global selain IHSG;
- sumber daftar saham Indonesia awal sudah dipilih: halaman Daftar Saham BEI; sektor, histori, pembaruan otomatis, serta hak penggunaan lanjutan masih perlu dicek;
- sumber indeks dan anggota indeks global selain IHSG, termasuk bobot dan sejarah keanggotaannya;
- harga internal dibuat oleh game atau memakai feed eksternal;
- jumlah saldo awal dan rumus hadiah;
- biaya transaksi, pajak game, spread FX, dan aturan settlement;
- durasi hari/periode perdagangan dan kalender tiap bursa;
- konfirmasi target perangkat melalui uji nyata, tingkat grafis final, ukuran unduhan aktual, serta orientasi layar;
- mode offline, kebutuhan koneksi, dan cara penyimpanan/cadangan;
- kapan leverage dan short selling dibuka;
- nominal paket terminal dan apa yang termasuk di setiap tingkat;
- tarif management/performance fee, ambang kenaikan karier, dan tipe klien;
- jenis aset tambahan di luar saham, obligasi, komoditas, dan FX;
- batas circuit breaker, mekanisme lelang pembukaan, dan lama halt;
- jumlah NPC, frekuensi berita, dan seberapa sering kondisi pasar berubah.

## 19. Instruksi untuk AI agent berikutnya

Sebelum mengubah atau membuat proyek:
1. Baca catatan ini dan periksa file proyek yang benar-benar ada.
2. Nyatakan mana yang baru berupa konsep dan mana yang sudah tersedia di kode.
3. Jangan menganggap papan UI sebagai implementasi.
4. Jangan mengklaim APK, ukuran, grafik, data, test, atau fitur telah berhasil sebelum memeriksanya.
5. Pertahankan ruang lingkup Android pribadi dan jangan memublikasikan game.
6. Jangan menghubungkan uang nyata, broker, tagihan, atau akun eksternal.
7. Prioritaskan akurasi ledger, pencocokan order, isolasi akun, dan aturan risiko sebelum dekorasi.
8. Gunakan data contoh secara jelas selama sumber pasar belum ditentukan; jangan memberi label harga contoh sebagai data live.
9. Buat perubahan sekecil mungkin yang tetap menjaga invarian. Periksa pola dan dependency yang sudah ada sebelum menambahkan abstraksi atau library.
10. Untuk logika uang/order, tinggalkan satu pemeriksaan otomatis yang gagal jika aturan inti rusak.
11. Minta keputusan pemilik hanya pada hal-hal terbuka yang benar-benar mengubah arah desain; pilihan implementasi rutin boleh ditentukan secara sederhana dan dicatat.
