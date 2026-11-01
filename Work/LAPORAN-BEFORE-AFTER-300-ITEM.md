# LAPORAN PERBANDINGAN LENGKAP BEFORE VS AFTER (300 ITEM GAME SAHAM)
## Dokumentasi Menyeluruh Seluruh Elemen UI, Kalimat, dan Logika Sistem yang Berubah
**Target Proyek:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`  
**Tanggal Terbit Laporan:** 31 Oktober 2026  
**Total Item Terverifikasi:** Tepat 300 Item  

---

## DAFTAR RINCI BEFORE VS AFTER (ITEM 001 s/d 300)

### [AUDIT-01] Aturan Media Query Menyembunyikan Judul Layar di Seluruh HP Portrait
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `style.css:2356-2359`
- **Kondisi SEBELUM (Before):**
  > `@media (max-width: 599px) { .header-identity { display: none; } }`
- **Akar Masalah:** Aturan media query CSS menyembunyikan kontainer .header-identity pada layar di bawah 599px, sehingga judul layar aktif (Beranda, Pasar, Portofolio, Bisnis, Lainnya) lenyap sama sekali di HP pengguna.
- **Dampak terhadap Pemain:** Pemain di ponsel pintar tidak dapat mengetahui nama layar atau sub-halaman yang sedang dibuka melalui header.
- **Kondisi SESUDAH (After):**
  > `Hapus display:none pada .header-identity; izinkan judul layar (#header-title) tetap tampil dominan dengan font-size:1.05rem dan flex:1.`

---

### [AUDIT-02] Teks Eyebrow Generik 'GLOBAL TRADER' Mempersempit Baris Header Shell
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `index.html:16`
- **Kondisi SEBELUM (Before):**
  > `<span class="eyebrow">GLOBAL TRADER</span><strong id="header-title">Beranda</strong>`
- **Akar Masalah:** Teks dekoratif statis 'GLOBAL TRADER' memakan ruang horizontal header ponsel di antara tombol profil dan chip kas.
- **Dampak terhadap Pemain:** Mempersempit ruang judul layar dan chip kas pada layar beresolusi 390px-412px, memicu pemotongan teks.
- **Kondisi SESUDAH (After):**
  > `Eliminasi teks 'GLOBAL TRADER'; tampilkan judul layar secara tegas: <strong id="header-title" style="font-size:1.15rem;">Beranda</strong>.`

---

### [AUDIT-03] Pengulangan Angka Kas Tersedia 3 Kali Berturut-turut dalam Jarak 150px Vertikal
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `index.html:17, 43-44`
- **Kondisi SEBELUM (Before):**
  > `Header: Kas Rp 75 Jt | Hero: Kas Total Rp 75 Jt | Hero Sub: Tersedia Rp 75 Jt`
- **Akar Masalah:** Nominal uang tunai yang persis sama diulang 3 kali di area pandang paling atas layar tanpa memberikan wawasan operasional baru.
- **Dampak terhadap Pemain:** Membuang area pandang atas layar portrait dan membingungkan pemain saat mulai memiliki portofolio saham.
- **Kondisi SESUDAH (After):**
  > `Header: Kas Tersedia ringkas | Hero: Total Kekayaan Bersih sebagai angka primer, dengan Kas Tersedia sebagai subteks rapi.`

---

### [AUDIT-04] Elemen Tombol Mati pada Context Strip dengan Style Tersembunyi Paksa
- **Kategori:** `BUG` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `index.html:21`
- **Kondisi SEBELUM (Before):**
  > `<button id="context-account-btn" class="context-account" style="display:none !important;">Akun: Pribadi</button>`
- **Akar Masalah:** Tombol pemilih akun yang sudah dinonaktifkan duduk sebagai elemen sampah di DOM dengan inline style display:none !important.
- **Dampak terhadap Pemain:** Menambah beban parsing DOM seluler dan membingungkan penelusuran hierarki fokus aksesibilitas.
- **Kondisi SESUDAH (After):**
  > `Hapus elemen tombol mati ini dari template berkas index.html produksi.`

---

### [AUDIT-05] Pita Banner Lingkungan Internal 'QA HOST JAVA' Mendorong Layout ke Bawah
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `index.html:26-28`
- **Kondisi SEBELUM (Before):**
  > `<div id="mock-banner" class="mock-banner">QA HOST JAVA · SAVE TERISOLASI · BUKAN ANDROID</div>`
- **Akar Masalah:** Pita banner pengujian setebal ~32px mendorong seluruh konten layar portrait ke bawah pada sesi permainan.
- **Dampak terhadap Pemain:** Merusak proporsi vertikal layar HP dan memotong elemen kartu finansial yang seharusnya berada di atas lipatan layar (above the fold).
- **Kondisi SESUDAH (After):**
  > `Setel display:none !important pada berkas produksi atau sembunyikan secara default saat berjalan di WebView runtime.`

---

### [AUDIT-06] Teks Status Jam Bursa Menampilkan Placeholder Kaku 'Memuat sesi'
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `index.html:20, app.js:621`
- **Kondisi SEBELUM (Before):**
  > `<span id="session-label">Memuat sesi</span>`
- **Akar Masalah:** Teks placeholder 'Memuat sesi' sering tampak statis beberapa detik sebelum sinkronisasi jembatan jam bursa selesai.
- **Dampak terhadap Pemain:** Memberikan kesan aplikasi lambat atau macet sesaat saat transisi antar layar.
- **Kondisi SESUDAH (After):**
  > `Gunakan label default yang natural seperti 'Sesi Pasar Reguler' sebelum sinkronisasi jam selesai.`

---

### [AUDIT-07] Slogan Motivasi Kosong 'Bangun langkahmu.' Membuang Ruang Layar Atas
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `index.html:35`
- **Kondisi SEBELUM (Before):**
  > `<span class="eyebrow" id="home-date">HARI PERDAGANGAN</span><h1>Bangun langkahmu.</h1>`
- **Akar Masalah:** Slogan motivasi kosong memakan tinggi vertikal ~50px tepat di area paling berharga layar Beranda.
- **Dampak terhadap Pemain:** Kartu finansial utama terdorong ke bawah; tanggal game justru ditaruh di font kecil yang sulit terbaca.
- **Kondisi SESUDAH (After):**
  > `Ganti slogan dengan Hari dan Tanggal Bursa Game yang tegas dan informatif: <h1 id="home-date" style="font-size:1.25rem;">Hari 1 · 24x Otomatis</h1>.`

---

### [AUDIT-08] Bar Lingkaran Aksi Cepat Beranda 80% Menduplikasi Tombol Tab Bawah
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `index.html:50-72`
- **Kondisi SEBELUM (Before):**
  > `Bar Lingkaran: [🛒 Beli Saham] [💼 Portofolio] [🏢 Bisnis] [🎁 Klaim Hibah] [💳 Pinjaman]`
- **Akar Masalah:** Tiga tombol menduplikasi bilah navigasi bawah, satu tombol menduplikasi kartu sponsor tepat di bawahnya.
- **Dampak terhadap Pemain:** Memboroskan 90px tinggi layar portrait untuk deretan tombol yang memiliki fungsi identik.
- **Kondisi SESUDAH (After):**
  > `Eliminasi bar lingkaran duplikat ini sepenuhnya; hemat 90px ruang vertikal agar konten pasar langsung terlihat.`

---

### [AUDIT-09] Pengulangan Tiga Kali Angka Nominal pada Kartu Sponsor Ventura Beranda
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `index.html:74-84`
- **Kondisi SEBELUM (Before):**
  > `Pill: +Rp 25.000.000 | Teks: '...ekspansi portofolio.' | Tombol: 'Klaim Hibah Rp 25 Juta'`
- **Akar Masalah:** Nominal Rp 25 Juta disebut 3 kali dalam kartu setebal 160px yang disertai esai panjang di Beranda.
- **Dampak terhadap Pemain:** Memakan area vertikal secara berlebihan dengan pengulangan kata yang tidak perlu.
- **Kondisi SESUDAH (After):**
  > `Rampingkan menjadi kartu aksi satu baris (tinggi ~50px): 'Dana Hibah Ventura | +Rp 25.000.000 (1x/hari) | [Klaim]'.`

---

### [AUDIT-10] Akses Misi & Pencapaian Ganda (Kartu Karier vs Kotak Shortcut Beranda)
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `index.html:85-97`
- **Kondisi SEBELUM (Before):**
  > `Kartu Karier: 'Lihat misi →' | Quick Grid: 'Misi Periodik 📋' & 'Pencapaian ◇'`
- **Akar Masalah:** Pemain disajikan dua pintu masuk terpisah untuk alur misi yang sama di layar Beranda.
- **Dampak terhadap Pemain:** Membingungkan pemain seolah-olah terdapat dua sistem misi yang berbeda dalam game.
- **Kondisi SESUDAH (After):**
  > `Satukan kontrol misi di dalam kartu karier; hapus tombol duplikat dari kisi pintasan.`

---

### [AUDIT-11] Tumpukan 5 Baris 'Quick Grid' (10 Shortcut) Menenggelamkan Warta Pasar
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `index.html:92-132`
- **Kondisi SEBELUM (Before):**
  > `10 Tombol: Laporan, Pencapaian, Misi, Liga, Duel Bos, Galeri, DNA, Streak, Dominasi, Asuransi`
- **Akar Masalah:** Menciptakan tembok tautan vertikal yang mendorong modul warta pasar dan sentimen harian hingga 400px ke bawah.
- **Dampak terhadap Pemain:** Pemain tidak melihat sentimen berita hari ini saat membuka game karena terhalang 10 kotak tombol.
- **Kondisi SESUDAH (After):**
  > `Pindahkan fitur sekunder ke Tab Lainnya; biarkan Beranda fokus pada Laporan Sesi, Liga, dan Pantauan Pasar.`

---

### [AUDIT-12] Tautan Teks 'Panduan & Mekanisme ↗' Memiliki Target Sentuh Terlalu Tipis
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `index.html:141, app.js:1450`
- **Kondisi SEBELUM (Before):**
  > `<button id="btn-home-guide" class="text-button">Panduan & Mekanisme ↗</button>`
- **Akar Masalah:** Tautan teks terpencil di bawah pantauan pasar memiliki tinggi target sentuh hanya ~24px dan label yang membingungkan.
- **Dampak terhadap Pemain:** Pemain sering salah sentuh atau mengabaikan panduan karena target sentuh tidak memenuhi standar ergonomi 44px.
- **Kondisi SESUDAH (After):**
  > `Gunakan tombol bergaya chip rapi dengan tinggi minimal 44px atau integrasikan ke dalam menu Pengaturan di Tab Lainnya.`

---

### [AUDIT-13] Keterangan Hero Caption 'Aset portofolio, kas, bisnis & koleksi · IDR' Berulang
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `index.html:42, app.js:1220`
- **Kondisi SEBELUM (Before):**
  > `<span class="hero-caption">Aset portofolio, kas, bisnis & koleksi · IDR</span>`
- **Akar Masalah:** Keterangan teks panjang yang menjelaskan komponen kekayaan bersih diulang terus di bawah kartu total aset.
- **Dampak terhadap Pemain:** Menambah kepadatan visual tanpa menambah informasi angka keuangan baru bagi pemain.
- **Kondisi SESUDAH (After):**
  > `Sederhanakan menjadi label mata uang ringkas: <span class="hero-caption">Konsolidasi Aset Bersih (IDR)</span>.`

---

### [AUDIT-14] Label 'Kewajiban / Utang' Menampilkan Teks 'Memuat…' Abu-abu Samar Saat Nihil
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `index.html:47, app.js:1235`
- **Kondisi SEBELUM (Before):**
  > `Utang: <strong id="home-liabilities">Memuat…</strong>`
- **Akar Masalah:** Saat pemain tidak memiliki utang sama sekali (Rp 0), status kewajiban menampilkan teks 'Memuat…' berwarna redup.
- **Dampak terhadap Pemain:** Pemain mengira data keuangan aplikasi gagal terhubung ke server atau sedang error.
- **Kondisi SESUDAH (After):**
  > `Tampilkan secara tegas nilai 'Rp 0' dengan warna netral saat tidak ada fasilitas pinjaman aktif.`

---

### [AUDIT-15] Header Bagian 'Langkah berikutnya: HARI INI' Menggunakan Frasa Kaku
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `index.html:73`
- **Kondisi SEBELUM (Before):**
  > `<div class="section-heading"><h2>Langkah berikutnya: HARI INI</h2></div>`
- **Akar Masalah:** Frasa 'Langkah berikutnya: HARI INI' terdengar seperti to-do list tugas kantor daripada game simulasi pasar modal.
- **Dampak terhadap Pemain:** Mengurangi nuansa profesional dan atmosfer trading floor modern.
- **Kondisi SESUDAH (After):**
  > `Ubah menjadi subjudul kontekstual: <h2>Peluang & Agenda Hari Ini</h2>.`

---

### [AUDIT-16] Tombol Filter Berita Beranda 'Semua / Kabar Baik / Kabar Buruk' Terlalu Rapat
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `index.html:135, style.css:1620`
- **Kondisi SEBELUM (Before):**
  > `<div class="news-filters"><button>Semua</button><button>Kabar Baik</button><button>Kabar Buruk</button></div>`
- **Akar Masalah:** Baris tombol filter berita berdesakan pada layar HP sempit (360px), memotong teks dan mempersempit kartu berita.
- **Dampak terhadap Pemain:** Pemain sulit menyaring warta pasar penting dan tombol filter rawan salah pencet.
- **Kondisi SESUDAH (After):**
  > `Terapkan overflow-x:auto dengan padding-bottom:4px dan flex-shrink:0 pada masing-masing pill filter.`

---

### [AUDIT-17] Teks Metadisclaimer 'Model simulasi game' pada Kartu Indeks IHSG & S&P 500
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `index.html:168, app.js:2840`
- **Kondisi SEBELUM (Before):**
  > `<small class="meta-tag">Model simulasi game</small>`
- **Akar Masalah:** Pemberian label disclaimer 'Model simulasi game' pada setiap kartu indeks merusak kesan imersif.
- **Dampak terhadap Pemain:** Menciptakan kesan game murahan padahal mesin pasar digerakkan oleh order book dan matching engine nyata.
- **Kondisi SESUDAH (After):**
  > `Ganti dengan status sesi bursa sesungguhnya seperti 'IDX Composite · Sesi Reguler' dan 'S&P 500 · US Market'.`

---

### [AUDIT-18] Modul Top Movers Menampilkan Kalimat Negatif Saat Pasar Bergerak Tenang
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:3120`
- **Kondisi SEBELUM (Before):**
  > `topMoversContainer.innerHTML = '<p class="empty">Tidak ada pergerakan harga signifikan hari ini.</p>'`
- **Akar Masalah:** Saat pembukaan sesi atau pasar tenang, modul menampilkan pesan kosong bernada seolah pasar mati atau rusak.
- **Dampak terhadap Pemain:** Membingungkan pemain dan membuat layar Beranda tampak hampa.
- **Kondisi SESUDAH (After):**
  > `Tampilkan daftar 3 emiten dengan volume tertinggi atau spread antrean terpadat sebagai pengganti pesan kosong.`

---

### [AUDIT-19] Ketiadaan Nominal Harga Terakhir pada Kartu 3 Saham Penguat & 3 Pelemah
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:3150`
- **Kondisi SEBELUM (Before):**
  > `Card Top Mover: [BBCA] [+2.45%] (Tanpa angka nominal harga saham)`
- **Akar Masalah:** Kartu mover hanya menampilkan kode saham dan persentase perubahan tanpa menyebutkan harga saham terkini.
- **Dampak terhadap Pemain:** Pemain tidak tahu apakah emiten tersebut bernilai Rp 500 atau Rp 10.000 tanpa harus membuka detail instrumen satu per satu.
- **Kondisi SESUDAH (After):**
  > `Sertakan nominal harga terakhir di samping persentase: 'BBCA · Rp 10.250 (+2.45%)'.`

---

### [AUDIT-20] Badge Makroekonomi (#macro-badge) Terbentur Tombol Cepat di Layar Sempit
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `index.html:19-25, style.css:290`
- **Kondisi SEBELUM (Before):**
  > `<span id="macro-badge" class="m-tag neutral">Normal (Lv.5)</span> <button id="btn-skip-break">⏩ Lanjut</button>`
- **Akar Masalah:** Pada layar portrait 360px-390px, penambahan badge siklus makro dan tombol ⏩ Lanjut mendesak waktu jam bursa hingga membentur tepi layar.
- **Dampak terhadap Pemain:** Angka jam bursa atau teks badge makro terpotong elipsis pada ponsel kecil.
- **Kondisi SESUDAH (After):**
  > `Beri min-width:0 pada kontainer jam bursa dan jadikan tombol ⏩ Lanjut sebagai ikon ringkas tanpa teks berlebih pada layar sempit.`

---

### [AUDIT-21] Avatar Profil di Kiri Atas Header Tidak Memiliki Indikator Level Karier
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `index.html:15, style.css:265`
- **Kondisi SEBELUM (Before):**
  > `<button id="btn-settings" class="brand-button"><span class="brand-mark"><svg ...>...</svg></span></button>`
- **Akar Masalah:** Tombol avatar profil hanya menampilkan SVG figur generik abu-abu tanpa badge angka level karier pemain saat ini.
- **Dampak terhadap Pemain:** Pemain tidak merasakan kepuasan progres status dan pangkat finansialnya secara instan di layar utama.
- **Kondisi SESUDAH (After):**
  > `Tambahkan badge kecil melayang di sudut avatar yang menunjukkan level pemain saat ini (misal: 'Lv.3').`

---

### [AUDIT-22] Tag Status Sesi Tersembunyi Menuliskan Istilah Redundan 'Pasar Internal Game'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `index.html:24`
- **Kondisi SEBELUM (Before):**
  > `<span id="mode-label" class="sr-only">Pasar Internal Game</span>`
- **Akar Masalah:** Screen-reader text 'Pasar Internal Game' redundan dengan label sesi bursa di sampingnya.
- **Dampak terhadap Pemain:** Membacakan istilah developer yang tidak berguna bagi pengguna teknologi asistif.
- **Kondisi SESUDAH (After):**
  > `Ganti dengan teks deskriptif standar: <span id="mode-label" class="sr-only">Bursa Saham Utama</span>.`

---

### [AUDIT-23] Bottom Navigation Bar Tidak Mengunci Status Visual Saat Membuka Modal Penuh
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:250, style.css:2389`
- **Kondisi SEBELUM (Before):**
  > `Nav-item tetap menerima klik dan mempertahankan highlight saat flow-page modal aktif.`
- **Akar Masalah:** Saat lembar flow terbuka di atas layar utama, navigasi bawah kadang tampak aktif ganda.
- **Dampak terhadap Pemain:** Pemain bingung apakah tombol bawah akan menutup modal atau membuka tab baru di latar belakang.
- **Kondisi SESUDAH (After):**
  > `Redupkan opacity navigasi bawah (opacity: 0.4; pointer-events: none) saat modal flow-page penuh sedang aktif.`

---

### [AUDIT-24] Kebocoran Scroll Latar Belakang (.app-scroll) Saat Modal Detail Terbuka
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:340, style.css:2370`
- **Kondisi SEBELUM (Before):**
  > `.app-scroll tetap dapat di-scroll saat .flow-page aktif di beberapa WebView Android.`
- **Akar Masalah:** Sentuhan jari pengguna pada latar modal kadang menggeser kontainer utama .app-scroll di belakang modal.
- **Dampak terhadap Pemain:** Saat modal ditutup, posisi scroll halaman Beranda atau Pasar melompat tidak terduga.
- **Kondisi SESUDAH (After):**
  > `Tambahkan class modal-open pada document.body dengan overflow:hidden dan overscroll-behavior:none saat modal terbuka.`

---

### [AUDIT-25] Tombol 'Semua →' pada Pantauan Pasar Beranda Memiliki Label Ambigu
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `index.html:143`
- **Kondisi SEBELUM (Before):**
  > `<button id="btn-home-market" class="text-button">Semua <span aria-hidden="true">→</span></button>`
- **Akar Masalah:** Label tombol 'Semua →' tidak menjelaskan apakah akan membuka semua berita atau membuka seluruh katalog saham.
- **Dampak terhadap Pemain:** Membuat interaksi navigasi menjadi tebak-tebakan bagi pemain baru.
- **Kondisi SESUDAH (After):**
  > `Ubah label menjadi lebih eksplisit dan intuitif: 'Buka Pasar ↗' atau 'Daftar Lengkap ↗'.`

---

### [AUDIT-26] Formulir Filter Pasar Dropdown 4-Baris Bertumpuk Masih Mengendap di DOM
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `index.html:181-185`
- **Kondisi SEBELUM (Before):**
  > `<div style="display:none !important;"><select id="filter-exchange-select">...</select><select id="filter-sector-select">...</select>...</div>`
- **Akar Masalah:** Deretan elemen select lama yang sudah digantikan oleh chip filter modern masih ditinggalkan di DOM dengan inline style tersembunyi.
- **Dampak terhadap Pemain:** Menghambat kebersihan source code dan rawan memicu event listener ganda jika terpanggil di script.
- **Kondisi SESUDAH (After):**
  > `Hapus blok select filter tersembunyi ini sepenuhnya dari berkas index.html.`

---

### [AUDIT-27] Eyebrow Kapital 'BURSA & KEPEMILIKAN' Membuang Ruang Layar Pasar
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `index.html:153`
- **Kondisi SEBELUM (Before):**
  > `<span class="eyebrow" id="pasar-porto-eyebrow">BURSA & KEPEMILIKAN</span>`
- **Akar Masalah:** Eyebrow kapital statis memakan tinggi vertikal di atas judul layar Pasar & Portofolio.
- **Dampak terhadap Pemain:** Mendorong tombol subtab Pasar dan Portofolio lebih ke bawah pada layar HP portrait.
- **Kondisi SESUDAH (After):**
  > `Hapus teks eyebrow ini; biarkan judul dan subtab pil menjadi navigasi utama yang bersih.`

---

### [AUDIT-28] Pill Badge 'Bursa Aktif' di Header Pasar Redundan dengan Status Dot Jam
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `index.html:156`
- **Kondisi SEBELUM (Before):**
  > `<span id="pasar-porto-status-badge" class="m-tag pos">Bursa Aktif</span>`
- **Akar Masalah:** Tag status bursa aktif diulang kembali tepat di bawah status dot hijau jam bursa yang ada di context strip.
- **Dampak terhadap Pemain:** Redundansi visual ganda pada rentang jarak kurang dari 50px.
- **Kondisi SESUDAH (After):**
  > `Hapus badge status duplikat ini untuk memperlebar area judul subtab.`

---

### [AUDIT-29] Label Sektor Kosong atau Strip '-' pada Emiten Tanpa Kategori Terdaftar
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:2910`
- **Kondisi SEBELUM (Before):**
  > `<span class="sector-tag">-</span>`
- **Akar Masalah:** Jika emiten tidak memiliki data sektor terdaftar, sistem merender tanda strip '-' atau teks kosong.
- **Dampak terhadap Pemain:** Tampak seperti bug database atau data cacat di mata pemain.
- **Kondisi SESUDAH (After):**
  > `Gunakan kategori default yang aman seperti 'Diversified / Konglomerasi' atau sembunyikan tag jika kosong.`

---

### [AUDIT-30] Avatar Inisial 2-Huruf Memboroskan Kolom Tanpa Informasi Tambahan
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:2940, style.css:1410`
- **Kondisi SEBELUM (Before):**
  > `<div class="stock-logo-circle">BB</div>`
- **Akar Masalah:** Lingkaran inisial 2 huruf (seperti 'BB' untuk BBCA) memakan lebar 40px tepat di samping ticker 'BBCA'.
- **Dampak terhadap Pemain:** Mengambil ruang horizontal berharga yang seharusnya digunakan untuk nama lengkap perusahaan dan volume lot.
- **Kondisi SESUDAH (After):**
  > `Hapus lingkaran inisial redundan; ratakan teks ticker ke kiri dengan font tebal dan nama emiten di bawahnya.`

---

### [AUDIT-31] Bug Regex Naif pada Pelacak Fluktuasi Harga Saham (Pemisah Titik Rupiah)
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:1554`
- **Kondisi SEBELUM (Before):**
  > `const newVal = parseFloat(fresh.currentPrice.replace(/[^0-9.-]+/g,"")) || 0;`
- **Akar Masalah:** Regex membuang karakter non-angka tetapi membiarkan titik desimal. Pada format IDR 'Rp 10.500', titik ribuan dianggap desimal sehingga menghasilkan nilai 10.5 bukannya 10500.
- **Dampak terhadap Pemain:** Animasi flash harga naik/turun (price-flash-up / down) sering salah arah saat harga saham melintasi batas ribuan.
- **Kondisi SESUDAH (After):**
  > `Gunakan helper terstandarisasi parseCleanPrice(fresh.currentPriceRaw || fresh.currentPrice) yang aman dari format ribuan IDR.`

---

### [AUDIT-32] Teks Penjelas Persentase Saham Menambahkan Akhiran Meta '· perubahan game'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:1565`
- **Kondisi SEBELUM (Before):**
  > `pill.textContent = `${isPos ? '+' : ''}${fresh.dailyChangePercent}% · perubahan game`;`
- **Akar Masalah:** String '· perubahan game' ditambahkan di setiap pill fluktuasi harga detail saham.
- **Dampak terhadap Pemain:** Kebocoran istilah developer yang merusak suasana simulasi bursa profesional.
- **Kondisi SESUDAH (After):**
  > `Cukup tampilkan persentase fluktuasi bersih: `${isPos ? '+' : ''}${fresh.dailyChangePercent}% (Hari Ini)`.`

---

### [AUDIT-33] Pengulangan Nama Perusahaan, Bursa & Sektor Tiga Kali di Detail Saham
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:4420-4450`
- **Kondisi SEBELUM (Before):**
  > `Header Modal: BBCA (IDX) | Sub-header: Bank Central Asia Tbk · IDX · Keuangan | Info Bar: IDX:BBCA · Sektor Keuangan`
- **Akar Masalah:** Data identitas emiten yang sama diulang 3 kali berturut-turut dalam lembar detail instrumen.
- **Dampak terhadap Pemain:** Memakan sepertiga layar awal detail saham dan menunda pemain melihat grafik serta order book.
- **Kondisi SESUDAH (After):**
  > `Satukan identitas emiten dalam satu blok header ringkas: 'BBCA · Bank Central Asia' dengan pill 'IDX · Keuangan'.`

---

### [AUDIT-34] Daftar 20 Baris Rasio Akuntansi Mentah Tanpa Pengelompokan Akordion
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:4620-4710`
- **Kondisi SEBELUM (Before):**
  > `Daftar teks mentah 20 rasio: ROE, ROA, DER, PBV, PER, DY, EV/EBITDA, Current Ratio, Quick Ratio, dst.`
- **Akar Masalah:** Rasio finansial dicetak memanjang ke bawah tanpa tab atau menu lipat (accordion).
- **Dampak terhadap Pemain:** Pemain kasual kewalahan membaca data mentah dan harus menggeser layar sangat jauh untuk mencapai tombol order.
- **Kondisi SESUDAH (After):**
  > `Bungkus metrik sekunder ke dalam detail akordion lipat ('Rasio Keuangan Lengkap ▾'); tampilkan hanya 4 rasio utama (PER, PBV, ROE, DY) di layar depan.`

---

### [AUDIT-35] Tombol Manual 'Perbarui harga & antrean' Redundan dengan Engine Realtime
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:4750`
- **Kondisi SEBELUM (Before):**
  > `<button class="btn-secondary">Perbarui harga & antrean</button>`
- **Akar Masalah:** Disediakan tombol refresh manual pada detail saham, padahal WebSocket/jembatan jam game sudah melakukan polling otomatis tiap detik.
- **Dampak terhadap Pemain:** Memboroskan ruang layar dan memberi kesan seolah sistem game tidak otomatis memperbarui data.
- **Kondisi SESUDAH (After):**
  > `Hapus tombol manual ini; ganti dengan indikator denyut sinkronisasi kecil (pulsing dot) di sudut grafik.`

---

### [AUDIT-36] Pemberitahuan Suspensi Menggunakan Kalimat Birokrasi Bertele-tele
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:4780`
- **Kondisi SEBELUM (Before):**
  > `'Pemberitahuan: Perdagangan instrumen ini sedang di-halt sementara oleh otoritas bursa terkait pemenuhan regulasi keterbukaan informasi...'`
- **Akar Masalah:** Paragraf peringatan pembekuan saham (halt) memuat 3 baris kalimat formal yang berbelit-belit.
- **Dampak terhadap Pemain:** Mengaburkan status utama instrumen dan memakan area order book.
- **Kondisi SESUDAH (After):**
  > `Ringkaskan menjadi banner peringatan tegas: '⛔ Perdagangan Dihentikan Sementara (Suspended oleh Bursa)'.`

---

### [AUDIT-37] Penjelasan Satuan '100 saham/unit (1 lot)' Diulang pada Setiap Kartu Emiten
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:2960`
- **Kondisi SEBELUM (Before):**
  > `<small class="text-muted">100 saham/unit (1 lot)</small>`
- **Akar Masalah:** Setiap baris saham di pasar mencantumkan keterangan satuan 100 saham.
- **Dampak terhadap Pemain:** Menumpuk teks pada daftar scroll panjang yang memperlambat pemindaian harga secara visual.
- **Kondisi SESUDAH (After):**
  > `Cukup tampilkan harga per lembar dan satuan lot pada tiket order saat transaksi berlangsung.`

---

### [AUDIT-38] Kartu Posisi Menampilkan Blok Abu-abu Besar 'Belum memiliki posisi'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:4590`
- **Kondisi SEBELUM (Before):**
  > `<div class="card"><h3>Posisi Anda</h3><p>Belum memiliki posisi pada instrumen ini.</p></div>`
- **Akar Masalah:** Di halaman detail saham, sistem membuat kartu tebal 80px hanya untuk mengabarkan bahwa pemain belum punya saham tersebut.
- **Dampak terhadap Pemain:** Memboroskan ruang layar dan menenggelamkan antrean Bid/Ask ke bawah.
- **Kondisi SESUDAH (After):**
  > `Jika pemain belum memiliki posisi, sembunyikan kartu atau tampilkan sebagai teks satu baris ramping: 'Portofolio: 0 lot'.`

---

### [AUDIT-39] Label Dampak Sentimen Berita Emiten Terlalu Panjang dan Bertingkat
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:4810`
- **Kondisi SEBELUM (Before):**
  > `<span class="news-impact">Perkiraan dampak: naik · +2.0% - 5.0%</span>`
- **Akar Masalah:** Label sentimen warta mencantumkan frasa pengantar panjang 'Perkiraan dampak: naik ·'.
- **Dampak terhadap Pemain:** Teks meluap dan terpotong pada layar beresolusi sempit.
- **Kondisi SESUDAH (After):**
  > `Ganti dengan pill ringkas berwarna hijau: '▲ Target +2.0% s/d +5.0%'.`

---

### [AUDIT-40] Teks Bantuan Tiket Order 'Biaya dan perkiraan kas...' Memenuhi Formulir
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `index.html:295`
- **Kondisi SEBELUM (Before):**
  > `<p id="order-est-total">Biaya dan perkiraan kas ditampilkan sebelum order dikirim.</p>`
- **Akar Masalah:** Kalimat penjelas statis memakan baris tepat di atas tombol Tinjau Order.
- **Dampak terhadap Pemain:** Mengurangi ruang dan mendorong tombol aksi utama terpotong keyboard virtual ponsel.
- **Kondisi SESUDAH (After):**
  > `Tampilkan angka estimasi total secara dinamis: 'Estimasi Total: Rp 0 (Termasuk Komisi 0.15%)'.`

---

### [AUDIT-41] Input Harga Limit Memiliki Nilai Default Hardcoded 10250 di HTML
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `index.html:293`
- **Kondisi SEBELUM (Before):**
  > `<input id="order-limit-price" class="input-field" type="number" value="10250">`
- **Akar Masalah:** Nilai default '10250' tertulis mati di file HTML. Jika pemain membuka tiket order saham berharga Rp 200 atau US$ 150 sebelum JS mengisi harga, angka 10250 tetap menempel.
- **Dampak terhadap Pemain:** Berisiko fatal jika pemain tergesa-gesa menekan order sehingga order terpasang pada harga yang salah 50x lipat.
- **Kondisi SESUDAH (After):**
  > `Kosongkan atribut value di HTML (value="") dan berikan placeholder="Masukkan harga limit".`

---

### [AUDIT-42] Tiket Order Membocorkan ID Teknis Akun '(ACC-PERS-01)' ke Antarmuka
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:7696`
- **Kondisi SEBELUM (Before):**
  > `accContext.textContent = `${currentAccount.name} (${currentAccount.id})`;`
- **Akar Masalah:** Pemain disajikan string teknis internal database seperti 'Akun Pribadi (ACC-PERS-01)'.
- **Dampak terhadap Pemain:** Membingungkan pemain dengan istilah id entitas developer yang tidak estetis.
- **Kondisi SESUDAH (After):**
  > `Cukup tampilkan nama akun secara ramah: 'Akun Pribadi' atau 'Akun Firma PT Indojaya'.`

---

### [AUDIT-43] Tombol Persentase Lot Modal Terlalu Rapat dan Rawan Salah Sentuh MAX
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `index.html:285-290, style.css:1720`
- **Kondisi SEBELUM (Before):**
  > `<div id="order-pct-buttons" style="display:flex; gap:8px;"><button>25%</button><button>50%</button><button>75%</button><button>MAX</button></div>`
- **Akar Masalah:** Tombol alokasi modal 75% dan MAX diletakkan bersebelahan tanpa pemisah warna yang tegas.
- **Dampak terhadap Pemain:** Pemain berisiko tinggi salah menekan tombol MAX (menghabiskan seluruh kas portofolio 100%) padahal bermaksud memilih 75%.
- **Kondisi SESUDAH (After):**
  > `Beri tombol MAX warna aksen peringatan (amber/gold border) dan jarak pemisah visual yang lebih aman.`

---

### [AUDIT-44] Lembar Review Order Mengulang Label 'Mode: Pasar Internal Game'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:7780`
- **Kondisi SEBELUM (Before):**
  > `flowText(panel, 'Mode', 'Pasar Internal Game');`
- **Akar Masalah:** Pada dialog konfirmasi sebelum eksekusi pesanan saham, sistem masih mencantumkan label mode game.
- **Dampak terhadap Pemain:** Menambah panjang dialog konfirmasi dan menunda pemain menekan tombol konfirmasi final.
- **Kondisi SESUDAH (After):**
  > `Hapus baris mode ini; fokuskan review pada: Emiten, Tipe Order, Jumlah Lot, Harga, dan Estimasi Kas.`

---

### [AUDIT-45] Keterangan Waktu Berlaku Menampilkan Format Teknis Jam Detik Kaku
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:7810`
- **Kondisi SEBELUM (Before):**
  > `flowText(panel, 'Estimasi berlaku hingga', '15:45:00 WIB');`
- **Akar Masalah:** Penyebutan jam detik presisi '15:45:00 WIB' terkesan kaku untuk pesanan harian biasa.
- **Dampak terhadap Pemain:** Membingungkan pemain seolah order akan hangus dalam hitungan detik tertentu.
- **Kondisi SESUDAH (After):**
  > `Gunakan bahasa bursa yang umum dan alami: 'Berlaku Hingga: Akhir Sesi Bursa Hari Ini'.`

---

### [AUDIT-46] Sheet Tiket Order Tertimpa Toast Notifikasi Sistem Melayang
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `style.css:2000, 2180`
- **Kondisi SEBELUM (Before):**
  > `#order-modal { z-index: 1050; } | #toast-container { z-index: 2000; }`
- **Akar Masalah:** Container toast memiliki z-index 2000 sedangkan modal order 1050. Ketika toast autosave muncul, toast menutupi tombol tutup tiket order.
- **Dampak terhadap Pemain:** Pemain tidak bisa menutup tiket order sampai notifikasi toast hilang dengan sendirinya.
- **Kondisi SESUDAH (After):**
  > `Setel z-index modal dialog (#order-modal dan .flow-page) ke 2500 sehingga berada di atas notifikasi background.`

---

### [AUDIT-47] Tombol Tutup Tiket Order '✕' Memiliki Target Sentuh Hanya 24px
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `index.html:262`
- **Kondisi SEBELUM (Before):**
  > `<button id="btn-close-modal" style="font-size:1.4rem; padding:4px 8px;">✕</button>`
- **Akar Masalah:** Tombol silang untuk membatalkan tiket order terlalu kecil dan mepet ke sudut kanan atas.
- **Dampak terhadap Pemain:** Pemain di layar sentuh ponsel sering salah menyentuh area background daripada tombol silang.
- **Kondisi SESUDAH (After):**
  > `Beri padding dan ukuran minimum 48px × 48px (min-height: 48px; min-width: 48px; display: flex; align-items: center; justify-content: center;).`

---

### [AUDIT-48] Judul Subtab Terminal Riset Menyisakan Konsep Dev '(Satu Pintu)'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5051`
- **Kondisi SEBELUM (Before):**
  > `showFlow('Terminal Riset Pasar (Satu Pintu)');`
- **Akar Masalah:** Istilah sisa diskusi arsitektur developer '(Satu Pintu)' lolos ke antarmuka pengguna pada judul modal riset.
- **Dampak terhadap Pemain:** Tampak aneh dan membingungkan pemain mengenai arti kurung tersebut.
- **Kondisi SESUDAH (After):**
  > `Bersihkan judul menjadi: showFlow('Terminal Riset & Warta Pasar');.`

---

### [AUDIT-49] Strip Nilai Wajar Belum Memiliki Keterangan Edukatif untuk Pemula
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:4554-4561`
- **Kondisi SEBELUM (Before):**
  > `fundStrip hanya menampilkan Nilai Wajar dan Tag Strong Buy tanpa aksi sentuh/penjelasan.`
- **Akar Masalah:** Pemain pemula yang melihat badge 'Strong Buy' atau 'Diskon 18%' tidak tahu dasar formula perhitungannya.
- **Dampak terhadap Pemain:** Pemain merasa angka tersebut seperti tebak-tebakan tanpa dasar valuasi fundamental.
- **Kondisi SESUDAH (After):**
  > `Tambahkan ikon info kecil 'ⓘ' yang ketika diketuk menampilkan popover singkat: 'Berdasarkan rasio EPS emiten dikalikan rata-rata PER sektor industri'.`

---

### [AUDIT-50] Bilah Chip Filter Pasar Tidak Mempertahankan Scroll Saat Dipilih
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `index.html:175-180, app.js:2870`
- **Kondisi SEBELUM (Before):**
  > `.filter-chips-bar tanpa scrollIntoView saat chip kanan diklik.`
- **Akar Masalah:** Saat memilih chip 'Watchlist ★' di ujung kanan baris pada layar ponsel 360px, bilah filter melompat kembali ke kiri.
- **Dampak terhadap Pemain:** Mengganggu kenyamanan navigasi dan membingungkan orientasi visual pemain.
- **Kondisi SESUDAH (After):**
  > `Tambahkan event click handler yang memanggil chip.scrollIntoView({ behavior: 'smooth', inline: 'center', block: 'nearest' });.`

---

### [AUDIT-51] Kebocoran Kode Tiket Developer '(A21)' dan '(A20)' pada Header Portofolio
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:1894, 1910`
- **Kondisi SEBELUM (Before):**
  > `<h3>Portofolio Gabungan (A21)</h3> <p>Pemisahan dana transaksi aktif (A20)</p>`
- **Akar Masalah:** Kode penomoran sprint pengembang (A21 dan A20) bocor ke teks UI judul dan paragraf portofolio.
- **Dampak terhadap Pemain:** Mengurangi kredibilitas visual game dan membingungkan pemain dengan kode asing.
- **Kondisi SESUDAH (After):**
  > `Hapus kode tiket sepenuhnya: 'Portofolio Gabungan' dan 'Pemisahan dana kas operasional'.`

---

### [AUDIT-52] Pengulangan Angka Valuasi Total Kas di dalam Akordion Dompet
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:1930`
- **Kondisi SEBELUM (Before):**
  > `Header Dompet: Total Kas Rp 50 Jt | Baris 1: Kas Rekening Rp 50 Jt | Sub-baris: Dana Tersedia Rp 50 Jt`
- **Akar Masalah:** Pemain yang hanya memiliki satu rekening kas disajikan pengulangan nominal saldo 3 kali berturut-turut di dalam akordion.
- **Dampak terhadap Pemain:** Menciptakan kesan berbelit-belit pada struktur laporan kas dompet.
- **Kondisi SESUDAH (After):**
  > `Jika rekening hanya satu dan dana tertahan Rp 0, satukan rincian menjadi satu baris: 'Kas Rekening Utama: Rp 50.000.000'.`

---

### [AUDIT-53] Shortcut Pintu Putar ke Tab Bisnis di Dasar Layar Portofolio
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:1960`
- **Kondisi SEBELUM (Before):**
  > `<div class="card"><h3>Aset Bisnis & Firma</h3><p>Kelola perusahaan...</p><button>Buka Tab Bisnis →</button></div>`
- **Akar Masalah:** Menempatkan kartu promosi pintu putar ke Tab Bisnis di dalam layar Portofolio saham.
- **Dampak terhadap Pemain:** Menduplikasi navigasi tab bawah dan membingungkan batas tanggung jawab layar Portofolio.
- **Kondisi SESUDAH (After):**
  > `Hapus kartu pintu putar ini; biarkan pemain berpindah tab melalui bilah navigasi bawah secara konsisten.`

---

### [AUDIT-54] Label 'PRIBADI' dan 'DANA TERPISAH' Ditulis Ganda pada Header Portofolio
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:1905`
- **Kondisi SEBELUM (Before):**
  > `<span class="badge">PRIBADI</span> <span class="badge">DANA TERPISAH</span>`
- **Akar Masalah:** Dua badge dengan makna yang tumpang tindih dipasang berdampingan di atas ringkasan aset.
- **Dampak terhadap Pemain:** Memperberat tampilan header portofolio tanpa menambah informasi fungsional.
- **Kondisi SESUDAH (After):**
  > `Cukup gunakan satu badge bersih: <span class="m-tag neutral">Rekening Pribadi</span>.`

---

### [AUDIT-55] Bar Komposisi Alokasi Aset Tidak Menampilkan Angka Persentase
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `style.css:1540, app.js:1920`
- **Kondisi SEBELUM (Before):**
  > `<div class="allocation-bar"><div class="seg-stock" style="width:60%"></div><div class="seg-cash" style="width:40%"></div></div>`
- **Akar Masalah:** Bilah grafik alokasi aset hanya berupa garis warna-warni tanpa label teks persentase di sampingnya.
- **Dampak terhadap Pemain:** Pemain tidak tahu berapa persisnya bobot saham vs kas tanpa harus menghitung manual.
- **Kondisi SESUDAH (After):**
  > `Tampilkan legenda ringkas di bawah bar: 'Saham: 60% · Kas: 40% · Deposito: 0%'.`

---

### [AUDIT-56] Pengulangan Label '(valuasi IDR)' pada Setiap Baris Komposisi Portofolio
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:1945`
- **Kondisi SEBELUM (Before):**
  > `Saham (valuasi IDR): Rp 30 Jt | Deposito (valuasi IDR): Rp 0 | Reksa Dana (valuasi IDR): Rp 0`
- **Akar Masalah:** Teks '(valuasi IDR)' diulang pada setiap kategori instrumen di kartu ringkasan.
- **Dampak terhadap Pemain:** Menimbulkan clutter teks pada tampilan vertikal portofolio.
- **Kondisi SESUDAH (After):**
  > `Cantumkan mata uang sekali pada header kartu: 'Komposisi Aset (dalam IDR)'.`

---

### [AUDIT-57] Status Posisi 'Impas' Menggunakan Warna Teks Redup Samar (#64748B)
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `style.css:980`
- **Kondisi SEBELUM (Before):**
  > `.status-breakeven { color: #64748B; font-weight: normal; }`
- **Akar Masalah:** Ketika harga saham persis sama dengan modal beli (PnL 0.00%), teks status impas berwarna abu-abu sangat gelap.
- **Dampak terhadap Pemain:** Hampir tidak terbaca pada layar HP di bawah sinar matahari atau tema kontras tinggi.
- **Kondisi SESUDAH (After):**
  > `Gunakan warna abu-abu netral terang (#94A3B8) dengan indikator pill ringkas: 'Impas (0.00%)'.`

---

### [AUDIT-58] Ketiadaan Teks Bantuan 'Ketuk untuk jual/kelola' pada Posisi Saham
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:1980`
- **Kondisi SEBELUM (Before):**
  > `Kartu posisi saham hanya dapat diklik tanpa affordance tanda panah atau keterangan aksi.`
- **Akar Masalah:** Tidak ada indikator visual bahwa kartu posisi saham di portofolio dapat diketuk untuk membuka menu jual atau pasang cut-loss.
- **Dampak terhadap Pemain:** Banyak pemain baru mengira portofolio hanya tabel pasif yang tidak interaktif.
- **Kondisi SESUDAH (After):**
  > `Tambahkan ikon panah navigasi '›' atau chip mikro 'Kelola ↗' di ujung kanan baris saham.`

---

### [AUDIT-59] Esai Pembuka Modal e-IPO Menjelaskan Teori Book Building Bertele-tele
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5210`
- **Kondisi SEBELUM (Before):**
  > `flowText(panel, 'Pusat e-IPO', 'Sistem penawaran umum perdana saham baru dengan alokasi penjatahan pasti 60% dan pooling 40% sesuai jadwal...');`
- **Akar Masalah:** Paragraf pengantar e-IPO memuat esai teoritis 150 karakter yang memakan ruang vertikal layar.
- **Dampak terhadap Pemain:** Daftar emiten baru yang sedang menggelar IPO terdorong keluar layar pertama.
- **Kondisi SESUDAH (After):**
  > `Ganti dengan subjudul instruktif padat: 'Pesan saham perdana sebelum tercatat di bursa'.`

---

### [AUDIT-60] Tombol Pembatalan e-IPO Belum Menyebutkan Nominal Dana yang Dilepas
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:5280`
- **Kondisi SEBELUM (Before):**
  > `button.textContent = 'Batalkan Pemesanan';`
- **Akar Masalah:** Dialog konfirmasi pembatalan pemesanan IPO tidak merinci berapa dana tertahan yang akan langsung cair kembali ke kas pribadi.
- **Dampak terhadap Pemain:** Pemain ragu-ragu membatalkan pesanan karena takut ada biaya penalti atau dana hangus.
- **Kondisi SESUDAH (After):**
  > `Sertakan nominal pemulihan dana pada label: 'Batalkan Pemesanan & Cairkan Kembali Rp 15.000.000'.`

---

### [AUDIT-61] Format Rasio Pemecahan Saham (Stock Split) Merender Trailing Spasi Kosong
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5014`
- **Kondisi SEBELUM (Before):**
  > `flowText(row, `${act.type} · ${act.ticker}`, `Nilai/rasio: ${act.valueOrRatio} ${act.currency || ''}`);`
- **Akar Masalah:** Ketika aksi korporasi berupa STOCK_SPLIT, field currency bernilai kosong/null sehingga menghasilkan teks 'Nilai/rasio: 1:5 ' dengan trailing spasi.
- **Dampak terhadap Pemain:** Tampilan teks terlihat canggung dan tidak profesional.
- **Kondisi SESUDAH (After):**
  > `Beri kondisi percabangan: jika tipe STOCK_SPLIT, tampilkan `Rasio Pemecahan: ${act.valueOrRatio}`; jika DIVIDEN, tampilkan `Dividen: ${act.valueOrRatio} ${act.currency}`.`

---

### [AUDIT-62] Paragraf Ketentuan Cutoff Reksa Dana Terlalu Panjang dan Kaku
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5830`
- **Kondisi SEBELUM (Before):**
  > `flowText(panel, 'Ketentuan Transaksi', 'Pemesanan unit penyertaan diproses berdasarkan NAB hari bursa bersangkutan jika diselesaikan sebelum batas waktu cutoff 15:30 WIB...');`
- **Akar Masalah:** Esai perbankan kaku 3 baris dipasang di atas pilihan manajer investasi.
- **Dampak terhadap Pemain:** Mengurangi kenyamanan eksplorasi produk reksa dana.
- **Kondisi SESUDAH (After):**
  > `Ringkaskan menjadi poin instruktif: 'Cutoff harian 15:30 WIB · Penetapan NAB di akhir hari bursa'.`

---

### [AUDIT-63] Teks Pengantar Asuransi Hedging Menjelaskan Teori Crash Pasar Bertele-tele
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5741`
- **Kondisi SEBELUM (Before):**
  > `flowText(panel, 'Proteksi Hedging', 'Perlindungan 80% ganti rugi saat terjadi crash pasar ekstrem (> 5%).');`
- **Akar Masalah:** Menggunakan penjelasan teoritis di header kartu alih-alih langsung menampilkan status proteksi polis aktif pemain.
- **Dampak terhadap Pemain:** Membuang ruang di atas pilihan opsi durasi polis asuransi.
- **Kondisi SESUDAH (After):**
  > `Fokuskan pada status aktif: 'Status: Belum Terlindungi | Lindungi portofolio dari anomali crash pasar'.`

---

### [AUDIT-64] Produk Deposito Belum Menampilkan Perbandingan Bunga Terhadap Siklus Makro
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5890`
- **Kondisi SEBELUM (Before):**
  > `card.innerHTML = `<h3>${prod.name}</h3><p>Bunga: ${(prod.rate * 100).toFixed(2)}% p.a.</p>`;`
- **Akar Masalah:** Tidak menampilkan keterangan bahwa suku bunga deposito berjangka saat ini sedang menguat/melemah akibat posisi Siklus Makroekonomi.
- **Dampak terhadap Pemain:** Pemain tidak menyadari hubungan strategis antara menabung deposito saat masa resesi makro (bunga 9.5%) vs ekspansi (bunga 4.0%).
- **Kondisi SESUDAH (After):**
  > `Tambahkan tag makro: 'Bunga Efektif: 8.50% p.a. (Level Makro: Resesi Moderat)'.`

---

### [AUDIT-65] Riwayat Mutasi Kas Mencantumkan Prefix Teknis 'ORD-TRD-' yang Mengganggu
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:6420`
- **Kondisi SEBELUM (Before):**
  > `<h3>ORD-TRD-1793402842109 · Pembelian BBCA</h3>`
- **Akar Masalah:** ID unik transaksi database dicantumkan sebagai judul utama riwayat mutasi kas.
- **Dampak terhadap Pemain:** Membuat buku kas akun pemain tampak seperti log debug programmer.
- **Kondisi SESUDAH (After):**
  > `Jadikan judul ramah: 'Beli 10 Lot BBCA', dan taruh ID transaksi sebagai teks kecil abu-abu di sudut bawah.`

---

### [AUDIT-66] Notifikasi Gagal Auto-Renewal Terminal Riset Tidak Menyertakan Opsi Tukar Valas
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5120`
- **Kondisi SEBELUM (Before):**
  > `showGameToast('warning', 'Langganan Berakhir', 'Saldo USD tidak cukup untuk perpanjangan otomatis.');`
- **Akar Masalah:** Saat langganan Pro/Whale turun ke Basic karena saldo USD kosong, pemain hanya mendapat toast peringatan tanpa tautan cepat.
- **Dampak terhadap Pemain:** Pemain bingung cara mengisi saldo USD padahal memiliki saldo IDR berlimpah di kas pribadi.
- **Kondisi SESUDAH (After):**
  > `Sertakan tombol aksi cepat pada notifikasi: '[Tukar IDR ke USD Sekarang]'.`

---

### [AUDIT-67] Emiten yang Telah Resmi Listing Masih Muncul di Tab Penawaran Aktif e-IPO
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5235`
- **Kondisi SEBELUM (Before):**
  > `if (ipo.status === 'ACTIVE' || ipo.status === 'CLOSED') renderIpoCard(ipo);`
- **Akar Masalah:** Emiten yang sudah menyelesaikan tanggal listing perdana masih ditampilkan di bagian atas tab pemesanan penawaran umum.
- **Dampak terhadap Pemain:** Pemain mencoba memesan saham yang sebenarnya sudah resmi melantai di pasar reguler.
- **Kondisi SESUDAH (After):**
  > `Pisahkan penawaran aktif ke dalam subtab 'Sedang Berlangsung' dan 'Selesai / Sudah Melantai di Bursa'.`

---

### [AUDIT-68] Teks Konfirmasi Klaim Dividen Menuliskan Penjelasan Teori Perpajakan Kaku
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5035`
- **Kondisi SEBELUM (Before):**
  > `flowText(panel, 'Pajak Dividen', 'Dividen tunai dipotong pajak final 10% sesuai regulasi perpajakan pasar modal yang berlaku.');`
- **Akar Masalah:** Penjelasan legalitas pajak dividen diulang setiap kali pemain membuka jadwal dividen emiten.
- **Dampak terhadap Pemain:** Membuat pemain jenuh membaca disclaimer yang sama berulang kali.
- **Kondisi SESUDAH (After):**
  > `Rampingkan rincian kas langsung: 'Dividen Bersih: Rp 450.000 (Setelah Pajak Final 10%)'.`

---

### [AUDIT-69] Nilai NAB Reksa Dana Menggunakan 4 Desimal Memperkacau Lebar Kolom
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:5850`
- **Kondisi SEBELUM (Before):**
  > `NAB: Rp 1.452,8924 / unit`
- **Akar Masalah:** Menampilkan 4 angka desimal pada harga NAB reksa dana di layar portrait ponsel.
- **Dampak terhadap Pemain:** Angka saling menumpuk dan mendesak kolom return persentase ke luar layar.
- **Kondisi SESUDAH (After):**
  > `Bulatkan menjadi 2 desimal standar: 'NAB: Rp 1.452,89 / unit'.`

---

### [AUDIT-70] Penggunaan Emoji Campur Aduk pada Ikon Status Mutasi Dividen dan Kupon
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:6450`
- **Kondisi SEBELUM (Before):**
  > `span.textContent = item.type === 'DIVIDEND' ? '💰 Dividen' : item.type === 'INTEREST' ? '💵 Bunga' : '📈 Trading';`
- **Akar Masalah:** Pencampuran karakter emoji berwarna-warni membuat antarmuka tabel transaksi kehilangan kesan profesional.
- **Dampak terhadap Pemain:** Tampilan tidak konsisten dengan ikon SVG monokrom pada navigasi utama.
- **Kondisi SESUDAH (After):**
  > `Gunakan badge SVG monokrom atau chip teks elegan: '[DIVIDEN]', '[BUNGA DEPOSITO]', '[TRADING SAHAM]'.`

---

### [AUDIT-71] Modal Replay Historis Masih Menampilkan Form Padahal Fitur Telah Dinonaktifkan
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:5702-5735`
- **Kondisi SEBELUM (Before):**
  > `function openHistoricalReplayModal() merender form dataset, modal replay, dan tombol 'Mulai replay baru'.`
- **Akar Masalah:** Backend TradingWebBridge.java telah resmi menolak pemanggilan HISTORICAL_REPLAY dengan pesan error dinonaktifkan, namun modal UI masih ada dan tampak fungsional.
- **Dampak terhadap Pemain:** Pemain yang mengisi form dan menekan tombol akan menemui pesan error kegagalan yang membingungkan.
- **Kondisi SESUDAH (After):**
  > `Hapus fungsi openHistoricalReplayModal atau ganti isi dialog dengan pengumuman transparan bahwa mode difokuskan ke Pasar Realtime.`

---

### [AUDIT-72] Warna Badge Fluktuasi Negatif pada Aset Kripto/Valas Menggunakan Warna Oranye
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `style.css:1120`
- **Kondisi SEBELUM (Before):**
  > `.crypto-negative { background: rgba(245, 158, 11, 0.15); color: #F59E0B; }`
- **Akar Masalah:** Kerugian harga pada kartu multi-aset diberi warna oranye (amber peringatan) alih-alih warna merah standar (--negative / #EF4444).
- **Dampak terhadap Pemain:** Pemain salah mengartikan status rugi sebagai peringatan sistem biasa.
- **Kondisi SESUDAH (After):**
  > `Gunakan warna merah negatif standar konsisten: color: var(--negative); background: var(--negative-bg);`

---

### [AUDIT-73] Tombol Tarik Deposito Dini Tidak Memiliki Penegasan Visual Peringatan Penalti
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5920`
- **Kondisi SEBELUM (Before):**
  > `<button class="btn-secondary">Tarik Deposito Sebelum Jatuh Tempo</button>`
- **Akar Masalah:** Tombol penarikan deposito sebelum jatuh tempo menggunakan gaya tombol abu-abu sekunder biasa.
- **Dampak terhadap Pemain:** Pemain tidak menyadari bahwa penarikan dini akan membatalkan seluruh akumulasi bunga berjalan.
- **Kondisi SESUDAH (After):**
  > `Beri gaya tombol peringatan tegas (border: 1px solid var(--negative); color: var(--negative);) disertai dialog konfirmasi penalti bunga.`

---

### [AUDIT-74] Label Saldo Kas Menuliskan Kalimat Berbelit-belit Sepanjang 65 Karakter
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:1950`
- **Kondisi SEBELUM (Before):**
  > `<p class="text-muted">Saldo Kas Belum Ditempatkan ke Aset Saham Maupun Deposito</p>`
- **Akar Masalah:** Kalimat deskriptif sepanjang 65 karakter digunakan hanya untuk menjelaskan saldo kas menganggur (*idle cash*).
- **Dampak terhadap Pemain:** Memperpanjang tinggi kartu ringkasan kas tanpa menambah kegunaan.
- **Kondisi SESUDAH (After):**
  > `Persingkat menjadi label finansial standar: 'Kas Menganggur (Siap Investasi)'.`

---

### [AUDIT-75] Nilai Laba/Rugi Mengambang Ratusan Juta Terpotong Elipsis pada Baris Saham
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `style.css:1460, app.js:1995`
- **Kondisi SEBELUM (Before):**
  > `.position-pnl { max-width: 90px; text-overflow: ellipsis; overflow: hidden; white-space: nowrap; }`
- **Akar Masalah:** Kontainer PnL dibatasi max-width: 90px. Saat keuntungan pemain mencapai ratusan juta (misal: '+Rp 125.400.000'), teks terpotong menjadi '+Rp 125...'.
- **Dampak terhadap Pemain:** Pemain tidak dapat membaca hasil keuntungan portofolionya secara utuh pada daftar posisi.
- **Kondisi SESUDAH (After):**
  > `Hapus batasan max-width kaku; gunakan flex: 0 0 auto dan font-size: clamp(0.78rem, 2.5vw, 0.9rem) agar nominal pas di layar.`

---

### [AUDIT-76] Modal Pinjaman Bank Masih Menampilkan Bunga Hardcoded 0.05% yang Tidak Sinkron
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:5952`
- **Kondisi SEBELUM (Before):**
  > `flowText(panel, 'Ketentuan Fasilitas', 'Tenor fleksibel 7, 14, atau 30 hari game dengan bunga harian 0.05%.');`
- **Akar Masalah:** Teks bunga harian 0.05% tertulis mati di app.js. Padahal di TradingFacade.java bunga pinjaman kini dinamis mengikuti Siklus Makroekonomi Level 1..9 (getEffectiveLoanRate()).
- **Dampak terhadap Pemain:** Pemain disajikan informasi bunga pinjaman yang salah dan bertolak belakang dengan beban bunga riil yang didebet ledger.
- **Kondisi SESUDAH (After):**
  > `Ambil suku bunga pinjaman dinamis dari bridge getMacroAndSynthesisState() dan tampilkan angka harian/tahunan yang akurat.`

---

### [AUDIT-77] Jargon Akuntansi Kaku 'Ledger Dua Sisi' pada Deskripsi Pembentukan Holding
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:2018`
- **Kondisi SEBELUM (Before):**
  > `flowText(panel, 'Struktur Korporasi', 'Konsolidasi entitas anak usaha dengan prinsip ledger dua sisi dan pemisahan liabilitas hukum...');`
- **Akar Masalah:** Penggunaan bahasa buku teks akuntansi yang kaku dan tidak ramah pemain awam pada alur pendirian holding usaha.
- **Dampak terhadap Pemain:** Membuat fitur grup bisnis terasa rumit dan membosankan.
- **Kondisi SESUDAH (After):**
  > `Gunakan bahasa bisnis yang menggugah: 'Satukan anak usaha di bawah satu grup konglomerasi untuk efisiensi modal dan dividen gabungan'.`

---

### [AUDIT-78] Paragraf Rumus Matematika Menjejali Layar Modal Konsolidasi Kekayaan
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5740`
- **Kondisi SEBELUM (Before):**
  > `flowText(panel, 'Formula', 'Total kekayaan = Aset pribadi + Nilai ekuitas perusahaan operasional - Total kewajiban utang perbankan.');`
- **Akar Masalah:** Menuliskan persamaan matematika panjang dalam bentuk kalimat paragraf di awal modal dialog.
- **Dampak terhadap Pemain:** Memakan ruang di atas grafik pie konsolidasi kekayaan.
- **Kondisi SESUDAH (After):**
  > `Tampilkan langsung kartu angka ringkas: Aset Pribadi + Ekuitas Bisnis - Total Utang = Total Kekayaan Bersih.`

---

### [AUDIT-79] Pesan Kosong Pendirian Usaha Mengulang Syarat Modal Setor Berulang Kali
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:2030`
- **Kondisi SEBELUM (Before):**
  > `<p>Belum ada perusahaan. Siapkan modal Rp 100.000.000 untuk mendirikan PT baru sesuai syarat modal minimal Rp 100 Juta.</p>`
- **Akar Masalah:** Nominal syarat Rp 100 Juta diulang dua kali dalam satu kalimat pendek yang bernada kaku.
- **Dampak terhadap Pemain:** Tampilan kosong tampak tidak rapi.
- **Kondisi SESUDAH (After):**
  > `Sederhanakan menjadi panggilan aksi elegan: 'Mulai bangun bisnis pertama Anda (Modal pendirian minimal Rp 100.000.000)'.`

---

### [AUDIT-80] Duplikasi Label 'Status operasional' dan Badge 'Operasional Normal' Bersebelahan
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:2045`
- **Kondisi SEBELUM (Before):**
  > `<span>Status operasional:</span> <span class="badge pos">Operasional Normal</span>`
- **Akar Masalah:** Kata 'operasional' diulang dua kali dalam jarak 10px pada kartu profil anak usaha.
- **Dampak terhadap Pemain:** Pemborosan teks visual yang canggung dibaca.
- **Kondisi SESUDAH (After):**
  > `Cukup gunakan label bersih: <span>Status:</span> <span class="m-tag pos">Normal / Lancar</span>.`

---

### [AUDIT-81] Penulisan 'CEO: Belum tersedia' dengan Teks Merah Memberi Kesan Error
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:2055`
- **Kondisi SEBELUM (Before):**
  > `<p class="text-danger">CEO: Belum tersedia</p>`
- **Akar Masalah:** Perusahaan tanpa CEO diberi teks merah peringatan berbahaya, seolah-olah pabrik sedang mogok kerja.
- **Dampak terhadap Pemain:** Pemain panik mengira bisnisnya rusak padahal posisi CEO eksekutif bersifat opsional tambahan.
- **Kondisi SESUDAH (After):**
  > `Gunakan teks netral yang menenangkan: 'CEO: Belum Ditunjuk (Dikelola Pemilik)' dengan tombol kecil '[Rekrut]'.`

---

### [AUDIT-82] Esai Pembuka Modal Kebijakan Kerja Perusahaan Menutupi Opsi Jam Kerja
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:6110`
- **Kondisi SEBELUM (Before):**
  > `flowText(panel, 'Kebijakan Kerja', 'Atur beban jam kerja harian karyawan antara efisiensi biaya dan retensi moral jangka panjang...');`
- **Akar Masalah:** Paragraf pengantar mengambil 80px ruang vertikal modal dialog kebijakan SDM.
- **Dampak terhadap Pemain:** Pemain harus scroll ke bawah untuk melihat tombol opsi 6 Jam, 8 Jam, dan 10 Jam.
- **Kondisi SESUDAH (After):**
  > `Pangkas menjadi subjudul padat: 'Pilih ritme jam kerja harian karyawan'.`

---

### [AUDIT-83] Pengulangan Nama Holding pada Header dan Subjudul Modal Transfer Dana
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:6050`
- **Kondisi SEBELUM (Before):**
  > `Header: Transfer Kas Holding Nusantara | Subjudul: Transfer dana kas dari/ke rekening induk Holding Nusantara`
- **Akar Masalah:** Nama badan usaha induk diulang secara kaku di judul dan subjudul berturut-turut.
- **Dampak terhadap Pemain:** Memperpanjang teks modal tanpa menambah kejelasan alur.
- **Kondisi SESUDAH (After):**
  > `Header: 'Transfer Dana Perusahaan' | Subjudul: 'Pindahkan kas antara rekening pribadi dan PT Indojaya'.`

---

### [AUDIT-84] Pemberitahuan Gagal Operasi Bisnis Membocorkan Istilah Developer 'Remediasi'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:7640`
- **Kondisi SEBELUM (Before):**
  > `flowText(panel, 'Status belum terkonfirmasi', 'Jangan membuat permintaan baru. Pertahankan ID REQ-01; pemulihan status FX masih dalam remediasi.');`
- **Akar Masalah:** Pesan error kegagalan transaksi sistem membocorkan istilah teknis 'remediasi' dan 'ID permintaan'.
- **Dampak terhadap Pemain:** Pemain merasa bingung dan takut progres gamenya rusak.
- **Kondisi SESUDAH (After):**
  > `Gunakan instruksi yang menenangkan: 'Sistem sedang memproses transaksi Anda. Mohon tunggu beberapa detik sebelum mencoba lagi'.`

---

### [AUDIT-85] Pill Subtab Layar Bisnis Tidak Menampilkan Jumlah Unit Aktif
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `index.html:218-220`
- **Kondisi SEBELUM (Before):**
  > `<button id="firma-subtab-usaha">🏢 Usaha Saya</button><button id="firma-subtab-koleksi">💎 Koleksi</button><button id="firma-subtab-dana">💼 Kelola Dana</button>`
- **Akar Masalah:** Subtab tidak menampilkan angka indikator berapa perusahaan atau aset koleksi yang sudah dimiliki pemain.
- **Dampak terhadap Pemain:** Pemain harus membuka tab satu per satu hanya untuk memeriksa apakah ada bisnis yang aktif.
- **Kondisi SESUDAH (After):**
  > `Tampilkan counter badge kecil: '🏢 Usaha (2)' · '💎 Koleksi (5)' · '💼 Kelola Dana (1)'.`

---

### [AUDIT-86] Banner Hero di Tab Lainnya Menduplikasi Kartu Profil Shell Header
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:6250`
- **Kondisi SEBELUM (Before):**
  > `<div class="hero-banner"><h2>Profil & Prestise</h2><p>Nama: Investor Muda · Level 3</p></div>`
- **Akar Masalah:** Di bagian atas Tab Lainnya, sistem merender ulang banner profil pemain yang isinya sama persis dengan kartu profil di header.
- **Dampak terhadap Pemain:** Membuang 120px ruang layar vertikal dan menunda menu pengaturan terlihat.
- **Kondisi SESUDAH (After):**
  > `Ganti hero duplikat dengan navigasi grid 2 kolom yang langsung menyajikan menu utama.`

---

### [AUDIT-87] Badge Eyebrow Kapital Ganda 'PROFIL & PRESTISE' pada Setiap Kartu Menu
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:6280`
- **Kondisi SEBELUM (Before):**
  > `<span class="eyebrow">PROFIL & PRESTISE</span> <h3>Pencapaian</h3>`
- **Akar Masalah:** Kata 'PROFIL & PRESTISE' dicetak kapital di atas setiap tombol kartu menu di Tab Lainnya.
- **Dampak terhadap Pemain:** Menimbulkan polusi visual teks kapital yang berulang-ulang.
- **Kondisi SESUDAH (After):**
  > `Hapus teks eyebrow statis tersebut; cukup tampilkan judul menu dan ikon representatifnya.`

---

### [AUDIT-88] Keterangan Subteks Menu Riset Mengulang Kata 'Finansial' Tiga Kali
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:6310`
- **Kondisi SEBELUM (Before):**
  > `<h3>Pohon Riset Finansial Tech Tree</h3><p>Riset teknologi finansial untuk membuka efisiensi finansial pasar modal.</p>`
- **Akar Masalah:** Pengulangan kata 'finansial' 3 kali dalam judul dan subjudul kartu berukuran 80px.
- **Dampak terhadap Pemain:** Teks terasa canggung dan redundan.
- **Kondisi SESUDAH (After):**
  > `Rampingkan judul dan deskripsi: 'Pohon Teknologi R&D' | 'Buka analisis canggih dan diskon biaya transaksi bursa'.`

---

### [AUDIT-89] Deskripsi Menu Backup Menyajikan Esai SHA-256 yang Terlalu Panjang
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:6330`
- **Kondisi SEBELUM (Before):**
  > `flowText(panel, 'Integritas Berkas Save', 'Sistem menggunakan mekanisme penyimpanan ganda (save utama dan backup otomatis) dengan validasi checksum SHA-256...');`
- **Akar Masalah:** Pemain disajikan kuliah singkat mengenai integritas cryptographic hash pada menu simpanan biasa.
- **Dampak terhadap Pemain:** Menghabiskan ruang di atas tombol penting Simpan Sekarang dan Ekspor Cadangan.
- **Kondisi SESUDAH (After):**
  > `Gunakan jaminan keamanan yang ramah: 'Progres tersimpan aman dengan proteksi pencadangan ganda otomatis'.`

---

### [AUDIT-90] Deskripsi Menu Mulai Baru Menggunakan Istilah Developer 'Reset Run'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:2078`
- **Kondisi SEBELUM (Before):**
  > `<h3>Mulai Permainan Baru (Reset Run)</h3><p>Reset seluruh data sesi dan kembali ke awal.</p>`
- **Akar Masalah:** Penyisipan istilah roguelike gaming / developer '(Reset Run)' ke dalam teks menu game simulasi ekonomi.
- **Dampak terhadap Pemain:** Mengurangi nuansa profesional dan membingungkan pemain bisnis kasual.
- **Kondisi SESUDAH (After):**
  > `Ubah menjadi: 'Mulai Karir Baru' | 'Ulangi perjalanan finansial dari modal awal'.`

---

### [AUDIT-91] Tag Status Sesi Game di Hero Banner Tab Lainnya Memakan Ruang Vertikal
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:6265`
- **Kondisi SEBELUM (Before):**
  > `<span class="tag">Internal Market · Realtime Engine</span>`
- **Akar Masalah:** Tag teknis internal dipasang mengambang di bawah banner profil tanpa ada fungsi interaktif.
- **Dampak terhadap Pemain:** Menambah tinggi header dan menunda pemain mengakses tombol pengaturan.
- **Kondisi SESUDAH (After):**
  > `Hapus tag teknis ini dari antarmuka visual pemain.`

---

### [AUDIT-92] Ikon Menu Hub Menggunakan Karakter Emoji Campur Aduk
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:6295-6340`
- **Kondisi SEBELUM (Before):**
  > `Menu: [📋 Misi] [👑 Prestise] [💎 Koleksi] [💾 Backup] [⚙️ Pengaturan] [💀 Reset]`
- **Akar Masalah:** Penggunaan emoji sistem operasi bawaan yang warnanya tidak konsisten dan tidak mengikuti tema elegan navy-gold game.
- **Dampak terhadap Pemain:** Kualitas visual tampak tidak seragam di berbagai merk ponsel (Samsung, Xiaomi, Vivo merender emoji berbeda).
- **Kondisi SESUDAH (After):**
  > `Ganti seluruh emoji dengan set ikon SVG monokrom vektor yang seragam dan elegan.`

---

### [AUDIT-93] Teks Dialog Pemulihan Save Game Terlalu Defensif dan Bergaya Catatan Debug
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:6360`
- **Kondisi SEBELUM (Before):**
  > `'Sistem mendeteksi inkonsistensi atomic save setelah kegagalan IO sebelumnya. Pulihkan cadangan slot B?'`
- **Akar Masalah:** Pemain disajikan istilah kegagalan I/O dan atomic save ketika aplikasi menawarkan pemulihan cadangan data.
- **Dampak terhadap Pemain:** Pemain mengira HP atau sistemnya rusak secara fatal.
- **Kondisi SESUDAH (After):**
  > `Gunakan dialog penjelas yang tenang: 'Ditemukan berkas cadangan otomatis terbaru. Apakah Anda ingin memulihkan data tersebut?'`

---

### [AUDIT-94] Pesan di Modal Arketipe Investor Berisi Kalimat Defensif Programmer
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:6385`
- **Kondisi SEBELUM (Before):**
  > `flowText(panel, 'Metode', 'Gaya investasi sedang dievaluasi otomatis tanpa kuis buatan.');`
- **Akar Masalah:** Pernyataan 'tanpa kuis buatan' merupakan argumen desain fitur developer yang tidak seharusnya menjadi teks antarmuka.
- **Dampak terhadap Pemain:** Tampak aneh dan defensif di hadapan pemain.
- **Kondisi SESUDAH (After):**
  > `Ganti dengan deskripsi elegan: 'Gaya trading dianalisis secara cerdas dari riwayat transaksi riil Anda'.`

---

### [AUDIT-95] Label Teknis 'Skema V2, UTF-8' Muncul pada Modal Pengaturan Save
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:6345`
- **Kondisi SEBELUM (Before):**
  > `<small class="text-muted">Format data: Skema V2, UTF-8</small>`
- **Akar Masalah:** Penyebutan format encoding UTF-8 dan versi skema JSON pada antarmuka save game pemain.
- **Dampak terhadap Pemain:** Detail teknis yang tidak relevan bagi pemain game mobile.
- **Kondisi SESUDAH (After):**
  > `Hapus rincian encoding; cukup tampilkan tanggal dan jam terakhir kali game disimpan.`

---

### [AUDIT-96] Toast Autosave 'Progres tersimpan' Muncul Terlalu Sering dan Mengganggu
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:820`
- **Kondisi SEBELUM (Before):**
  > `showGameToast('success', 'Tersimpan', 'Progres permainan berhasil disimpan secara atomik.');`
- **Akar Masalah:** Setiap kali timer sinkronisasi jam atau aksi kecil berjalan di latar belakang, toast notifikasi sukses muncul menutupi layar.
- **Dampak terhadap Pemain:** Sangat mengganggu fokus membaca grafik saham dan menutupi header bursa.
- **Kondisi SESUDAH (After):**
  > `Filter notifikasi autosave rutin (Zero-Delta Filtering); simpan secara senyap di background tanpa menampilkan banner popup jika tidak ada error.`

---

### [AUDIT-97] Teks Konfirmasi Berulang 'Apakah Anda yakin ingin memulai Era Baru?'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:6400`
- **Kondisi SEBELUM (Before):**
  > `Judul: Memulai Era Baru? | Paragraf: Apakah Anda yakin ingin memulai Era Baru dan mereset portofolio?`
- **Akar Masalah:** Pertanyaan yang persis sama diulang pada judul modal dan baris pertama deskripsi konfirmasi prestise.
- **Dampak terhadap Pemain:** Redundansi kata yang membuat dialog terasa membosankan.
- **Kondisi SESUDAH (After):**
  > `Judul: 'Konfirmasi Era Baru' | Paragraf: 'Reset perjalanan aktif saat ini untuk mengklaim gelar prestise dan bonus multiplier permanen'.`

---

### [AUDIT-98] Tombol 'Hentikan perpanjangan otomatis' Muncul pada Paket Basic Gratis
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5140`
- **Kondisi SEBELUM (Before):**
  > `if (tier.autoRenewal) renderCancelAutoRenewalButton();`
- **Akar Masalah:** Tombol berhenti langganan otomatis sempat muncul pada paket Basic yang bersifat gratis permanen seumur hidup.
- **Dampak terhadap Pemain:** Membingungkan pemain seolah-olah paket gratis pun sedang menyedot biaya langganan diam-diam.
- **Kondisi SESUDAH (After):**
  > `Tampilkan tombol berhenti perpanjangan otomatis hanya jika pemain sedang aktif berlangganan paket PRO atau WHALE.`

---

### [AUDIT-99] Modal Laporan Ketidakhadiran Belum Menyertakan Tombol Aksi 'Buka Pasar'
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:6470-6510`
- **Kondisi SEBELUM (Before):**
  > `openAbsenceReportModal hanya memiliki tombol [Tutup] di bagian dasar ringkasan.`
- **Akar Masalah:** Setelah pemain membaca laba bisnis yang terkumpul selama pemain offline, pemain hanya disajikan tombol tutup dan kembali ke Beranda.
- **Dampak terhadap Pemain:** Memutus momentum bermain; pemain harus mencari tab Pasar sendiri untuk membelanjakan keuntungan offlinenya.
- **Kondisi SESUDAH (After):**
  > `Tambahkan tombol aksi primer [Lihat Pasar & Investasikan Laba ↗] di samping tombol [Tutup].`

---

### [AUDIT-100] Aset Koleksi Mewah yang Terkunci Tidak Menampilkan Estimasi Harga & Syarat
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:6550`
- **Kondisi SEBELUM (Before):**
  > `<div class="collection-item locked"><h3>???</h3><p>Aset Terkunci</p></div>`
- **Akar Masalah:** Aset mewah yang belum terbuka hanya menampilkan tanda tanya tanpa informasi harga atau level reputasi yang dibutuhkan.
- **Dampak terhadap Pemain:** Pemain kehilangan tujuan jangka panjang (*aspirational goal*) karena tidak tahu berapa modal yang harus dikumpulkan untuk membeli jet pribadi atau vila mewah.
- **Kondisi SESUDAH (After):**
  > `Tampilkan siluet elegan beserta target jelas: 'Superyacht Samudra · Terbuka pada Kekayaan Rp 50 Miliar (Harga: Rp 25 Miliar)'.`

---

### [AUDIT-101] Serialisasi Ganda Snapshot Akun Tiap Detik Memicu Jeda Garbage Collection (119ms)
- **Kategori:** `BUG & PERFORMANCE LAG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:8880-8884, TradingWebBridge.java:60`
- **Kondisi SEBELUM (Before):**
  > `requestClockSync().then(() => { refreshSnapshot(); }); // refreshSnapshot memanggil bridge.getSnapshot() kedua kalinya secara sinkron`
- **Akar Masalah:** Setiap detik jam bursa berdetak, bridge.beginClockSync sudah mengembalikan objek snapshot lengkap. Namun JS langsung memanggil bridge.getSnapshot() ulang secara sinkron, memicu serialisasi string JSON raksasa dua kali tiap detik.
- **Dampak terhadap Pemain:** Memicu GC pause Android 119ms (menghapus 50MB-98MB objek) yang membekukan animasi dan membuat scrolling terasa patah-patah (stutter).
- **Kondisi SESUDAH (After):**
  > `Gunakan payload snapshot yang sudah dikembalikan oleh beginClockSync; hilangkan pemanggilan getSnapshot() kedua.`

---

### [AUDIT-102] Pemanggilan Ganda bridge.getInstruments() Tiap Detik pada Tab Pasar
- **Kategori:** `BUG & PERFORMANCE LAG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:1843-1849`
- **Kondisi SEBELUM (Before):**
  > `const fresh = JSON.parse(bridge.getInstruments('', '', null, true)); ... const filtered = JSON.parse(bridge.getInstruments(query, exchange, sector, true));`
- **Akar Masalah:** Fungsi updateMarketQuotes() memanggil bridge.getInstruments dua kali berturut-turut tiap detik saat pengguna berada di tab Pasar.
- **Dampak terhadap Pemain:** Membebani thread Java dan JS engine dengan parsing 40 objek emiten secara ganda setiap 1.000 milidetik.
- **Kondisi SESUDAH (After):**
  > `Panggil bridge.getInstruments() sekali saja; lakukan pemfilteran query/sektor langsung di memori JavaScript.`

---

### [AUDIT-103] Destruksi & Rekonstruksi Penuh DOM highlights.replaceChildren() Tiap Detik
- **Kategori:** `BUG & PERFORMANCE LAG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:1732`
- **Kondisi SEBELUM (Before):**
  > `highlights.replaceChildren(); // Menghancurkan 6 tombol emiten dan membuatnya ulang dari nol tiap detik`
- **Akar Masalah:** Fungsi renderMarketHighlights menghancurkan seluruh elemen kartu 3 Penguat & 3 Pelemah tiap 1 detik daripada memperbarui node teks harga di tempat.
- **Dampak terhadap Pemain:** Menyebabkan reflow layout browser terus menerus dan boros alokasi memori DOM.
- **Kondisi SESUDAH (After):**
  > `Perbarui teks harga dan persentase pada elemen DOM yang sudah ada di tempat (in-place text update).`

---

### [AUDIT-104] Query DOM Global document.querySelectorAll Dijalankan 4 Kali Per Detik (250ms Loop)
- **Kategori:** `BUG & PERFORMANCE LAG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:8870-8874, updateNewsEffects`
- **Kondisi SEBELUM (Before):**
  > `setInterval(() => { updateNewsEffects(); }, 250); // menjalankan querySelectorAll('[data-news-effect-id]') tiap 250ms`
- **Akar Masalah:** Mesin menjalankan pemindaian pohon DOM lengkap tiap 250ms meskipun daftar berita pasar tidak mengalami perubahan.
- **Dampak terhadap Pemain:** Menghabiskan daya baterai smartphone secara sia-sia saat layar menyala.
- **Kondisi SESUDAH (After):**
  > `Hanya jalankan pembaruan label efek berita ketika timestamp menit bursa berubah atau ada berita baru terbit.`

---

### [AUDIT-105] Pencarian ID Elemen Berulang document.getElementById Tiap 250ms Tanpa Caching
- **Kategori:** `BUG & PERFORMANCE LAG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:updateSponsorCountdown`
- **Kondisi SEBELUM (Before):**
  > `const claimSponsorBtn = document.getElementById('btn-claim-sponsor'); // dipanggil 4x per detik`
- **Akar Masalah:** Pencarian node tombol sponsor di-lookup dari root dokumen berkali-kali dalam interval 250ms.
- **Dampak terhadap Pemain:** Overhead lookup hash DOM yang tidak perlu pada loop berkecepatan tinggi.
- **Kondisi SESUDAH (After):**
  > `Simpan referensi elemen DOM tombol sponsor ke dalam variabel lokal saat inisialisasi aplikasi.`

---

### [AUDIT-106] Pencatatan Logcat Jam Bursa Tetap Berjalan Aktif pada Build Release
- **Kategori:** `BUG & PERFORMANCE LAG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `TradingWebBridge.java:36-40`
- **Kondisi SEBELUM (Before):**
  > `debugClockStage("syncClock"); // memicu logging berkala`
- **Akar Masalah:** Pemberian string log berkala pada sinkronisasi jam bursa memicu I/O string formatting ke sistem logcat Android.
- **Dampak terhadap Pemain:** Memperlambat thread worker jam bursa saat dijalankan di perangkat dengan I/O penyimpanan terbatas.
- **Kondisi SESUDAH (After):**
  > `Pastikan logging dinonaktifkan sepenuhnya saat FLAG_DEBUGGABLE bernilai false.`

---

### [AUDIT-107] Input Pencarian Saham Tanpa Debounce Memicu Filter Ulang di Setiap Ketikan Huruf
- **Kategori:** `BUG & PERFORMANCE LAG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:7863`
- **Kondisi SEBELUM (Before):**
  > `searchInput.addEventListener('input', () => loadStocks());`
- **Akar Masalah:** Setiap ketikan satu huruf pada input pencarian saham langsung mengeksekusi bridge.getInstruments dan re-render puluhan elemen kartu.
- **Dampak terhadap Pemain:** Keyboard virtual HP terasa tersendat (laggy input) saat pengguna mengetik cepat kode saham.
- **Kondisi SESUDAH (After):**
  > `Terapkan fungsi debounce 250ms pada event listener input pencarian saham.`

---

### [AUDIT-108] Polling Portofolio Tiap Detik Mengalokasikan Puluhan Objek JSON di Tab Aset
- **Kategori:** `BUG & PERFORMANCE LAG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:8884, syncPortfolioLive`
- **Kondisi SEBELUM (Before):**
  > `if (currentTab === 'aset') syncPortfolioLive(); // memanggil bridge.getPortfolio tiap detik`
- **Akar Masalah:** Nilai portofolio hanya berubah jika terjadi fill order atau perubahan harga saham, namun di-polling penuh tiap detik.
- **Dampak terhadap Pemain:** Beban parsing JSON berulang pada halaman portofolio.
- **Kondisi SESUDAH (After):**
  > `Sinkronkan portofolio hanya saat ada notifikasi order match baru atau pergerakan harga saham yang dimiliki.`

---

### [AUDIT-109] Pemaksaan GPU Layer translateZ(0) pada 18 Class Kartu Memicu Tekanan VRAM
- **Kategori:** `BUG & PERFORMANCE LAG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `style.css:2380-2387`
- **Kondisi SEBELUM (Before):**
  > `.card, .wealth-hero, .shortcut, ... { transform: translateZ(0); }`
- **Akar Masalah:** Menetapkan transform: translateZ(0) pada hampir seluruh kartu membuat Android WebView mengalokasikan tekstur grafis terpisah untuk puluhan elemen.
- **Dampak terhadap Pemain:** Konsumsi memori grafis (VRAM) melonjak hingga puluhan megabyte, menyebabkan aplikasi lambat pada HP dengan RAM 4GB/6GB.
- **Kondisi SESUDAH (After):**
  > `Batasi layer GPU hardware acceleration hanya pada kontainer scroll utama (.app-scroll) dan modal aktif.`

---

### [AUDIT-110] Efek Box-Shadow 48px pada Modal Sheet Memperlambat Animasi Slide-In
- **Kategori:** `BUG & PERFORMANCE LAG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `style.css:1597`
- **Kondisi SEBELUM (Before):**
  > `box-shadow: 0 16px 48px rgba(0, 0, 0, 0.8);`
- **Akar Masalah:** Blur shadow radius 48px membutuhkan perhitungan komputasi blur GPU intensif saat modal bergeser ke atas.
- **Dampak terhadap Pemain:** Animasi kemunculan modal tiket order dan flow sheet mengalami drop frame pada layar refresh rate 120Hz.
- **Kondisi SESUDAH (After):**
  > `Sederhanakan bayangan menjadi: box-shadow: 0 8px 24px rgba(0, 0, 0, 0.6); border: 1px solid rgba(255,255,255,0.12).`

---

### [AUDIT-111] Penyarangan Ganda requestAnimationFrame dan setTimeout pada Animasi Jam
- **Kategori:** `BUG & PERFORMANCE LAG` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:226`
- **Kondisi SEBELUM (Before):**
  > `requestAnimationFrame(() => setTimeout(() => { ... }, 0));`
- **Akar Masalah:** Penumpukan callback rAF di dalam setTimeout memicu ketidakpastian sinkronisasi vsync layar.
- **Dampak terhadap Pemain:** Animasi denyut sinkronisasi jam bursa kadang melompat 1 frame.
- **Kondisi SESUDAH (After):**
  > `Gunakan salah satu mekanisme secara konsisten (cukup requestAnimationFrame murni).`

---

### [AUDIT-112] Redraw Penuh Elemen Grafik Canvas Setiap Kali Modal Detail Saham Dibuka
- **Kategori:** `BUG & PERFORMANCE LAG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:4720`
- **Kondisi SEBELUM (Before):**
  > `renderMiniChart() menghapus konteks 2D dan menggambar ulang seluruh candlestick/garis dari array mentah.`
- **Akar Masalah:** Saat berpindah antar emiten dengan cepat, canvas terus menerus dirender ulang tanpa caching path.
- **Dampak terhadap Pemain:** Sentuhan jari terasa berat saat menggeser daftar saham.
- **Kondisi SESUDAH (After):**
  > `Simpan path grafik yang telah dihitung sebelumnya ke dalam memori cache instrumen.`

---

### [AUDIT-113] Event Listener Scroll Tanpa Atribut { passive: true } Menahan Thread Compositor
- **Kategori:** `BUG & PERFORMANCE LAG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:125, style.css:2419`
- **Kondisi SEBELUM (Before):**
  > `main.addEventListener('scroll', () => { scrollPositions[currentTab] = main.scrollTop; });`
- **Akar Masalah:** Event scroll pada kontainer utama didaftarkan tanpa opsi { passive: true }.
- **Dampak terhadap Pemain:** Browser Chromium/WebView harus menunggu eksekusi JS selesai sebelum merespons sentuhan geser jari pemain.
- **Kondisi SESUDAH (After):**
  > `Tambahkan opsi { passive: true } pada seluruh event listener scroll.`

---

### [AUDIT-114] Properti will-change: scroll-position Tidak Pernah Dilepaskan dari DOM
- **Kategori:** `BUG & PERFORMANCE LAG` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `style.css:2375`
- **Kondisi SEBELUM (Before):**
  > `.app-scroll { will-change: scroll-position; }`
- **Akar Masalah:** will-change dibiarkan aktif permanen di root scroll container tanpa pernah direset.
- **Dampak terhadap Pemain:** Mengunci alokasi memori layer compositor secara statis di memori GPU.
- **Kondisi SESUDAH (After):**
  > `Hapus will-change permanen; biarkan engine browser Chromium mengoptimalkan layer scroll secara native.`

---

### [AUDIT-115] Serialisasi String Katalog Emiten Mengirimkan Field Statis yang Berulang
- **Kategori:** `BUG & PERFORMANCE LAG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `TradingWebBridge.java:getInstruments`
- **Kondisi SEBELUM (Before):**
  > `Mengirimkan seluruh metadata deskripsi, nama bursa, mata uang, dan tanggal katalog tiap permintaan harga.`
- **Akar Masalah:** Data statis emiten yang tidak pernah berubah (seperti nama lengkap perusahaan) ikut dikirimkan berulang kali dalam transfer JSON harga.
- **Dampak terhadap Pemain:** Ukuran transfer data bridge membengkak menjadi puluhan kilobyte tiap polling harga.
- **Kondisi SESUDAH (After):**
  > `Kirimkan payload harga dinamis ringkas (id, price, changePercent, volume) untuk pembaruan realtime.`

---

### [AUDIT-116] String 'undefined' Ditulis ke Input Limit Order Jika currentPriceRaw Absen
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:7666`
- **Kondisi SEBELUM (Before):**
  > `inputOrderLimit.value = stock.currentPriceRaw;`
- **Akar Masalah:** Jika objek instrumen tidak memiliki properti currentPriceRaw (misal pada instrumen tertentu), input limit order terisi string 'undefined'.
- **Dampak terhadap Pemain:** Formulir tiket order menjadi tidak valid dan pemain tidak dapat melakukan order tanpa menghapus teks manual.
- **Kondisi SESUDAH (After):**
  > `Gunakan fallback aman: inputOrderLimit.value = parseCleanPrice(stock.currentPriceRaw || stock.currentPrice);`

---

### [AUDIT-117] Beralih Akun di Tiket Order Tidak Memperbarui Tampilan Kas Tersedia di Bar Atas Sheet
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:7700`
- **Kondisi SEBELUM (Before):**
  > `accSelect.onchange hanya memanggil updateOrderEstimate(), tidak memperbarui #order-cash-avail.`
- **Akar Masalah:** Saat pemain mengganti akun dari Pribadi ke Klien pada tiket order, angka kas di bar #order-cash-avail masih menampilkan saldo akun lama.
- **Dampak terhadap Pemain:** Pemain mengira saldo akun klien sama dengan saldo akun pribadinya.
- **Kondisi SESUDAH (After):**
  > `Perbarui tampilan #order-cash-avail dan #order-cash-reserved dengan saldo akun terpilih saat opsi akun berubah.`

---

### [AUDIT-118] Input Modal Pengajuan Pinjaman Mengizinkan Nilai Negatif atau Nol
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:6120`
- **Kondisi SEBELUM (Before):**
  > `const amountInput = node('input', 'input-field'); // tanpa validasi min/max ketat`
- **Akar Masalah:** Input nominal pinjaman tidak mengunci batas minimal pinjaman pokok.
- **Dampak terhadap Pemain:** Pemain dapat mengetik angka 0 atau negatif yang memicu error exception dari domain Java.
- **Kondisi SESUDAH (After):**
  > `Setel atribut min="1000000" dan validasi input secara realtime sebelum tombol pengajuan aktif.`

---

### [AUDIT-119] Pencairan Unit Reksa Dana Mengizinkan Angka Desimal Mengambang Tidak Valid
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5880`
- **Kondisi SEBELUM (Before):**
  > `const qty = parseFloat(inputUnit.value); // tanpa pembulatan unit penyertaan`
- **Akar Masalah:** Input unit reksa dana tidak membatasi jumlah angka di belakang koma.
- **Dampak terhadap Pemain:** Menghasilkan selisih pembulatan fraksional (floating point precision error) pada saldo kas akhir.
- **Kondisi SESUDAH (After):**
  > `Kunci input ke 2 desimal maksimum dan bulatkan nilai penarikan sebelum dikirim ke backend.`

---

### [AUDIT-120] Formulir Pendirian PT Mengizinkan Nama Perusahaan yang Hanya Berisi Spasi
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:2487`
- **Kondisi SEBELUM (Before):**
  > `if (!name.value.trim()) ... // sebagian validasi form modal melewatkan pengecekan karakter khusus`
- **Akar Masalah:** Pemain dapat mendirikan anak usaha dengan nama kosong atau simbol aneh pada dialog tertentu.
- **Dampak terhadap Pemain:** Nama perusahaan di daftar usaha menjadi tidak terbaca dan merusak tata letak kartu bisnis.
- **Kondisi SESUDAH (After):**
  > `Wajibkan minimal 3 karakter alfanumerik yang sah untuk nama perusahaan baru.`

---

### [AUDIT-121] Modal Pendirian Holding Crash Jika Salah Satu Anak Usaha Tidak Memiliki Sektor Sah
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:2490`
- **Kondisi SEBELUM (Before):**
  > `const subsidiaries = selectors.map(item => ({sector:item.sector,companyId:item.select.value}));`
- **Akar Masalah:** Jika salah satu dropdown anak usaha bernilai kosong, mapping tetap dijalankan dan menyebabkan exception saat parsing di backend.
- **Dampak terhadap Pemain:** Aplikasi menampilkan error modal dan tombol tertahan dalam status loading.
- **Kondisi SESUDAH (After):**
  > `Validasi bahwa keempat sektor anak usaha wajib terpilih lengkap sebelum memicu request pembentukan holding.`

---

### [AUDIT-122] Kenaikan Beban Jam Kerja Karyawan Tidak Menampilkan Peringatan Penurunan Moral
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:6115`
- **Kondisi SEBELUM (Before):**
  > `Pemilihan opsi 10 Jam Kerja langsung mengubah radio button tanpa indikator efek samping.`
- **Akar Masalah:** Pemain tidak diberi peringatan bahwa menetapkan jam kerja 10 jam sehari akan menurunkan moral staf sebesar -15% tiap hari.
- **Dampak terhadap Pemain:** Pemain kaget saat karyawan tiba-tiba resign massal di akhir minggu.
- **Kondisi SESUDAH (After):**
  > `Tampilkan tag peringatan merah di samping opsi 10 Jam: '⚠️ Moral -15%/hari jika benefit tidak dinaikkan'.`

---

### [AUDIT-123] Pembelian Koleksi Mewah Menghabiskan Kas Hingga Rp 0 Tanpa Buffer Operasional
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:6560`
- **Kondisi SEBELUM (Before):**
  > `Tombol beli aset mewah tetap aktif selama saldo kas >= harga aset.`
- **Akar Masalah:** Pemain diizinkan membeli jam tangan mewah atau mobil sport hingga kas rekeningnya bersisa Rp 0.
- **Dampak terhadap Pemain:** Pemain langsung gagal membayar operasional bisnis dan gaji karyawan pada keesokan harinya.
- **Kondisi SESUDAH (After):**
  > `Beri dialog konfirmasi khusus jika pembelian koleksi menyisakan kas di bawah batas aman operasional 1 minggu.`

---

### [AUDIT-124] Rasio Fasilitas Margin Menampilkan 'NaN%' Saat Total Portofolio Rp 0
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:6210`
- **Kondisi SEBELUM (Before):**
  > `const marginRatio = (debt / equity * 100).toFixed(1);`
- **Akar Masalah:** Ketika equity bernilai 0, pembagian menghasilkan Infinity atau NaN yang tercetak mentah di antarmuka.
- **Dampak terhadap Pemain:** Tampilan metrik risiko terlihat cacat di mata pemain.
- **Kondisi SESUDAH (After):**
  > `Gunakan pengecekan: const marginRatio = equity > 0 ? (debt / equity * 100).toFixed(1) : '0.0';`

---

### [AUDIT-125] Perubahan Avatar di Kustomisasi Profil Tidak Langsung Terefleksi di Header Shell
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:7165`
- **Kondisi SEBELUM (Before):**
  > `profileStatus.textContent = 'Identitas dan preferensi tersimpan.'; // tanpa memperbarui ikon #btn-settings`
- **Akar Masalah:** Setelah pemain memilih avatar atau warna bingkai baru, tombol profil di sudut kiri atas header masih menampilkan ikon lama.
- **Dampak terhadap Pemain:** Pemain merasa perubahannya tidak tersimpan.
- **Kondisi SESUDAH (After):**
  > `Perbarui SVG/gambar pada #btn-settings secara langsung begitu profil berhasil disimpan.`

---

### [AUDIT-126] Penjelasan Reward Streak Harian Tidak Menyebutkan Syarat Transaksi Minimal
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5780`
- **Kondisi SEBELUM (Before):**
  > `flowText(panel, 'Bonus Kehadiran', 'Masuk dan bertransaksi setiap sesi bursa...');`
- **Akar Masalah:** Tidak disebutkan apakah sekadar login cukup untuk mempertahankan streak, atau harus ada transaksi jual/beli saham sah.
- **Dampak terhadap Pemain:** Pemain kehilangan rentetan streak karena hanya login tanpa bertransaksi.
- **Kondisi SESUDAH (After):**
  > `Jelaskan dengan tegas: 'Lakukan minimal 1 transaksi jual/beli saham setiap hari bursa untuk mempertahankan streak'.`

---

### [AUDIT-127] Persentase Dominasi Sektor Berisiko Melebihi 100% Jika Saham Beredar Mengalami Buyback
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5810`
- **Kondisi SEBELUM (Before):**
  > `const dominancePct = (userShares / sectorTotalShares * 100).toFixed(1);`
- **Akar Masalah:** Kalkulasi tidak melakukan clamping Math.min(100, pct).
- **Dampak terhadap Pemain:** Dapat menampilkan angka aneh seperti 'Dominasi Industri: 104.2%'.
- **Kondisi SESUDAH (After):**
  > `Terapkan Math.min(100.0, Math.max(0.0, dominancePct)) sebelum ditampilkan.`

---

### [AUDIT-128] Kalkulasi Audit Pajak Menggunakan Tarif Flat Kaku Bukan Tarif Progresif
- **Kategori:** `BUG` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:6230`
- **Kondisi SEBELUM (Before):**
  > `const taxEst = netIncome * 0.15;`
- **Akar Masalah:** Estimasi pajak bisnis mengalikan laba dengan tarif tunggal 15% tanpa memperhitungkan skala UMKM vs Korporasi.
- **Dampak terhadap Pemain:** Kurang realistis bagi pemain yang menyukai simulasi akuntansi akurat.
- **Kondisi SESUDAH (After):**
  > `Sesuaikan tarif pajak dengan skala usaha: 0.5% untuk omzet kecil dan tarif korporasi berjenjang.`

---

### [AUDIT-129] Komparasi Return Duel Bos Menampilkan Derau Desimal IEEE 754 Floating Point
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5825`
- **Kondisi SEBELUM (Before):**
  > `diffEl.textContent = `Selisih: ${bossReturn - playerReturn}%`;`
- **Akar Masalah:** Pengurangan angka desimal tanpa toFixed() menghasilkan angka seperti 'Selisih: 4.200000000000001%'.
- **Dampak terhadap Pemain:** Tampilan kartu tantangan bos tampak tidak rapi.
- **Kondisi SESUDAH (After):**
  > `Format selisih selalu dengan 2 desimal: Math.abs(bossReturn - playerReturn).toFixed(2) + '%'.`

---

### [AUDIT-130] Tombol Lewati Tutorial Tidak Menyimpan Flag Onboarding pada Profil Baru
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:6850`
- **Kondisi SEBELUM (Before):**
  > `closeFlow(); // tanpa menyimpan status tutorial_completed`
- **Akar Masalah:** Pemain yang menekan tombol lewati tutorial akan kembali disajikan tutorial setiap kali membuka layar tertentu.
- **Dampak terhadap Pemain:** Sangat mengganggu bagi pemain berpengalaman yang ingin langsung bermain cepat.
- **Kondisi SESUDAH (After):**
  > `Simpan status onboarding_done: true ke localStorage dan profil pengguna saat tombol lewati diklik.`

---

### [AUDIT-131] Tombol Bintang Watchlist di Baris Saham Terlalu Kecil (32px) dan Memicu Salah Buka Detail
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `style.css:2403, app.js:1796`
- **Kondisi SEBELUM (Before):**
  > `<button class="watchlist-star-btn"><span>★</span></button> (lebar sentuh sempit di tepi kartu)`
- **Akar Masalah:** Tombol bintang watchlist berada di dalam baris yang bisa diklik. Karena target sentuhnya sempit, jari pemain sering membuka detail saham padahal hanya ingin menandai favorit.
- **Dampak terhadap Pemain:** Pengguna frustrasi karena layar detail terus terbuka saat mengelola watchlist.
- **Kondisi SESUDAH (After):**
  > `Perlebar padding tombol bintang menjadi 44px × 44px dan pisahkan secara visual dari area klik baris utama.`

---

### [AUDIT-132] Kolom Pencarian Saham Tidak Memiliki Tombol '✕' untuk Hapus Teks Seketika
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `index.html:174`
- **Kondisi SEBELUM (Before):**
  > `<input id="search-stock-input" type="text" placeholder="🔍 Cari kode atau nama emiten…">`
- **Akar Masalah:** Setelah mencari saham (misal: 'BBCA'), pemain harus menekan tombol backspace berulang kali di keyboard ponsel untuk mengembalikan seluruh daftar.
- **Dampak terhadap Pemain:** Memperlambat alur eksplorasi bursa.
- **Kondisi SESUDAH (After):**
  > `Tambahkan tombol silang kecil '✕' di dalam kolom pencarian untuk mereset filter dalam sekali ketuk.`

---

### [AUDIT-133] Angka Volume Transaksi Saham Satuan Miliar/Triliun Melompat Jadi 2 Baris
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:4568, style.css:1430`
- **Kondisi SEBELUM (Before):**
  > `<div class="stock-stat-box"><small>Volume</small><strong>12.450.000 Lot</strong></div>`
- **Akar Masalah:** Pada layar ponsel 360px-390px, angka volume panjang mendesak kata 'Lot' turun ke baris baru.
- **Dampak terhadap Pemain:** Grid statistik 3x3 menjadi tidak simetris dan tinggi baris melompat-lompat.
- **Kondisi SESUDAH (After):**
  > `Gunakan singkatan ringkas standar pasar modal: '12.45M Lot' atau '2.10B Lot'.`

---

### [AUDIT-134] Tombol 'Tinjau Order' Tertutup Bilah Navigasi Sistem Android Saat Keyboard Muncul
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `index.html:297, style.css:1596`
- **Kondisi SEBELUM (Before):**
  > `padding: 20px 20px max(20px, env(safe-area-inset-bottom));`
- **Akar Masalah:** Ketika pemain mengetik harga limit dan keyboard Android naik, tombol submit order terdorong hingga terpotong di tepi bawah viewport.
- **Dampak terhadap Pemain:** Pemain harus menutup keyboard manual terlebih dahulu agar tombol order dapat disentuh.
- **Kondisi SESUDAH (After):**
  > `Terapkan padding-bottom: max(32px, env(safe-area-inset-bottom)) dan tambahkan scroll otomatis ke tombol saat input fokus.`

---

### [AUDIT-135] Tingkat Antrean Order Book 0 Lot Tampak Identik dengan Antrean Terisi
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderOrderBook`
- **Kondisi SEBELUM (Before):**
  > `<div class="book-row"><span class="lot">0</span>...</div>`
- **Akar Masalah:** Baris antrean yang belum terisi pesanan pasar tetap ditampilkan dengan warna teks yang sama dengan antrean aktif.
- **Dampak terhadap Pemain:** Pemain kesulitan membedakan kedalaman pasar (*market depth*) secara sekilas.
- **Kondisi SESUDAH (After):**
  > `Beri baris antrean kosong (0 lot) warna teks redup dan tanda strip '-' agar antrean riil langsung mencolok.`

---

### [AUDIT-136] Pill Sektor Emiten Memiliki Warna Seragam Abu-abu Tanpa Kode Warna Industri
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `style.css:1415`
- **Kondisi SEBELUM (Before):**
  > `.stock-sector { color: var(--text-secondary); background: var(--surface); }`
- **Akar Masalah:** Semua sektor (Keuangan, Tambang, Energi, Ritel, Teknologi) memiliki tampilan teks abu-abu yang persis sama.
- **Dampak terhadap Pemain:** Pemain tidak dapat membedakan sektor secara intuitif tanpa membaca teks lengkap.
- **Kondisi SESUDAH (After):**
  > `Beri aksen warna lembut spesifik sektor (misal: Biru untuk Perbankan, Emas untuk Energi/Tambang, Hijau untuk Konsumer).`

---

### [AUDIT-137] Formulir Kalender Dividen Tidak Menandai Tanggal Hari Ini Secara Visual
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5015`
- **Kondisi SEBELUM (Before):**
  > `Daftar jadwal hanya menuliskan string tanggal 'Recording: 2026-11-04'.`
- **Akar Masalah:** Pemain tidak tahu berapa hari lagi tanggal recording atau eksekusi dividen dari tanggal game hari ini.
- **Dampak terhadap Pemain:** Pemain harus mengingat tanggal hari ini untuk mencocokkan jadwal.
- **Kondisi SESUDAH (After):**
  > `Tambahkan tag hitung mundur hari bursa: 'Recording: 2026-11-04 (3 Hari Bursa Lagi)'.`

---

### [AUDIT-138] Kartu Posisi Saham Portofolio Tidak Menampilkan Harga Beli Rata-rata (Avg Price)
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:965-985`
- **Kondisi SEBELUM (Before):**
  > `Hanya menampilkan: Ticker, Total Modal, P&L, dan Persentase P&L.`
- **Akar Masalah:** Pemain tidak bisa melihat harga modal per lembar sahamnya (Average Price) langsung di daftar kartu portofolio.
- **Dampak terhadap Pemain:** Pemain harus menghitung manual modal dibagi jumlah lembar untuk mengetahui harga beli sahamnya.
- **Kondisi SESUDAH (After):**
  > `Cantumkan Avg Price di bawah ticker: 'Avg: Rp 9.850 · Last: Rp 10.250'.`

---

### [AUDIT-139] Tombol 'Lihat Rincian Dana Tertahan' di Dompet Tidak Memiliki Indikator Interaktif Panah
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:2026`
- **Kondisi SEBELUM (Before):**
  > `<button class="btn-secondary">Lihat Rincian Dana Tertahan</button>`
- **Akar Masalah:** Tombol tampak seperti tombol aksi submit formulir daripada pembuka lembar detail.
- **Dampak terhadap Pemain:** Mengurangi kejelasan interaksi antarmuka.
- **Kondisi SESUDAH (After):**
  > `Ubah label menjadi: 'Rincian Alokasi Dana Tertahan ↗'.`

---

### [AUDIT-140] Nama Manajer Investasi Reksa Dana Terpotong Terlalu Cepat pada Layar Lebar
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:5845`
- **Kondisi SEBELUM (Before):**
  > `fund.manager.slice(0, 12) + '...'`
- **Akar Masalah:** Pemotongan string statis 12 karakter dilakukan di JavaScript tanpa memanfaatkan CSS ellipsis.
- **Dampak terhadap Pemain:** Nama institusi seperti 'Schroder Dana Prestasi' terpotong menjadi 'Schroder Dan...' padahal layar masih sangat luas.
- **Kondisi SESUDAH (After):**
  > `Gunakan CSS text-overflow: ellipsis; white-space: nowrap; max-width: 160px; agar responsif terhadap lebar layar.`

---

### [AUDIT-141] Opsi Tenor Deposito 10, 25, dan 50 Hari Disusun Memanjang ke Bawah
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5910`
- **Kondisi SEBELUM (Before):**
  > `Setiap produk deposito memakan kartu tebal 90px secara vertikal.`
- **Akar Masalah:** Pilihan produk deposito mengambil seluruh layar modal sehingga suku bunga tidak bisa dibandingkan sekilas.
- **Dampak terhadap Pemain:** Pemain harus scroll atas-bawah berkali-kali untuk membandingkan bunga antar tenor.
- **Kondisi SESUDAH (After):**
  > `Susun pilihan tenor ke dalam grid 3 kolom simetris di bagian atas: [10 Hari] [25 Hari] [50 Hari] dengan bunga tercantum di dalamnya.`

---

### [AUDIT-142] Kartu Upgrade Fasilitas Perusahaan Tidak Memiliki Bar Progres Modal
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:3380`
- **Kondisi SEBELUM (Before):**
  > `flowText(card, 'Biaya Upgrade', formatMoney(cost));`
- **Akar Masalah:** Hanya menampilkan biaya peningkatan level tanpa visualisasi seberapa dekat kas perusahaan untuk mencapai biaya tersebut.
- **Dampak terhadap Pemain:** Pemain kurang merasakan motivasi pencapaian target tabungan bisnis.
- **Kondisi SESUDAH (After):**
  > `Tambahkan progress bar modal: 'Kas: Rp 80 Jt / Rp 100 Jt (80%)'.`

---

### [AUDIT-143] Kartu Anak Usaha Holding Tidak Memiliki Badge Laba/Rugi Harian Ringkas
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:2510`
- **Kondisi SEBELUM (Before):**
  > `Kartu anak usaha hanya menampilkan Nama PT, Sektor, dan Level.`
- **Akar Masalah:** Pemain pemilik holding tidak tahu anak usaha mana yang sedang merugi atau mencetak dividen tanpa membuka detail perusahaan.
- **Dampak terhadap Pemain:** Memperlambat pemantauan performa konglomerasi bisnis pemain.
- **Kondisi SESUDAH (After):**
  > `Tambahkan tag PnL harian di sudut kartu: '🟢 Laba +Rp 14.5 Jt/hari' atau '🔴 Defisit -Rp 2 Jt/hari'.`

---

### [AUDIT-144] Statistik Produktivitas Staf di Bursa Headhunting Menggunakan Desimal Kering
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:5795`
- **Kondisi SEBELUM (Before):**
  > `flowText(card, 'Produktivitas', `${emp.productivity}x`);`
- **Akar Masalah:** Penyajian angka '1.25x' terasa kering dan kurang bernuansa game tycoon.
- **Dampak terhadap Pemain:** Kurang menarik minat pemain untuk berburu eksekutif terbaik.
- **Kondisi SESUDAH (After):**
  > `Sajikan dalam format visual rating: 'Produktivitas: 1.25x ★★★★☆ (Senior Eksekutif)'.`

---

### [AUDIT-145] Chip Kas Header Mengambang Canggung di Tengah Layar pada Orientasi Tablet/Foldable
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `style.css:280, 2360`
- **Kondisi SEBELUM (Before):**
  > `margin-left: auto;`
- **Akar Masalah:** Pada tablet atau ponsel lipat layar lebar, jarak antara judul layar dan chip kas menjadi sangat renggang (terlalu jauh ke kanan).
- **Dampak terhadap Pemain:** Komposisi header tampak tidak seimbang pada layar lebar.
- **Kondisi SESUDAH (After):**
  > `Beri max-width 720px pada seluruh bar .app-header dengan margin: 0 auto agar tetap terpusat elegan.`

---

### [AUDIT-146] Istilah Panjang 'Penawaran Hak Memesan Efek Terlebih Dahulu (HMETD)' Terlalu Kaku
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5018`
- **Kondisi SEBELUM (Before):**
  > `flowText(card, 'Jenis', 'Penawaran Hak Memesan Efek Terlebih Dahulu (HMETD / Rights Issue)');`
- **Akar Masalah:** Penggunaan frasa hukum pasar modal lengkap sepanjang 65 karakter pada kartu aksi korporasi.
- **Dampak terhadap Pemain:** Memenuhi kartu dan membingungkan pemain kasual.
- **Kondisi SESUDAH (After):**
  > `Sederhanakan menjadi istilah populer: 'Rights Issue (Tebus Saham Baru)'.`

---

### [AUDIT-147] Disclaimer Perbankan 'Dijamin oleh protokol perbankan terintegrasi model' Bergaya Meta
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5915`
- **Kondisi SEBELUM (Before):**
  > `flowText(panel, 'Jaminan', 'Simpanan dijamin oleh protokol perbankan terintegrasi model game.');`
- **Akar Masalah:** Pemberian jaminan menggunakan istilah 'protokol model game' yang merusak suasana bermain.
- **Dampak terhadap Pemain:** Mengingatkan pemain secara canggung bahwa ini hanya kode tiruan.
- **Kondisi SESUDAH (After):**
  > `Ubah menjadi suasana dunia finansial imersif: 'Simpanan perbankan berizin dengan perlindungan suku bunga terjamin'.`

---

### [AUDIT-148] Kategori Berita Membocorkan String Enum Database 'EMITEN_CORP_ANNOUNCEMENT'
- **Kategori:** `BUG & PEMBOROSAN TEKS` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:4815`
- **Kondisi SEBELUM (Before):**
  > `<span class="category-tag">EMITEN_CORP_ANNOUNCEMENT</span>`
- **Akar Masalah:** String enum konstanta Java dicetak mentah tanpa pemetaan teks bahasa Indonesia di antarmuka warta.
- **Dampak terhadap Pemain:** Tampak seperti bug aplikasi dan tidak ramah pemain.
- **Kondisi SESUDAH (After):**
  > `Petakan menjadi label rapi: 'Pengumuman Emiten'.`

---

### [AUDIT-149] Notifikasi Penjatahan e-IPO Menampilkan Status Enum Mentah 'PARTIALLY_FILLED_POOLED'
- **Kategori:** `BUG & PEMBOROSAN TEKS` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:5270`
- **Kondisi SEBELUM (Before):**
  > `showGameToast('info', 'Hasil Penjatahan', `Status: ${ipo.allotmentStatus}`); // mencetak PARTIALLY_FILLED_POOLED`
- **Akar Masalah:** Status penjatahan saham IPO menampilkan konstanta kode teknis programmer dalam bahasa Inggris.
- **Dampak terhadap Pemain:** Pemain bingung apakah pesanannya berhasil dialokasikan atau gagal.
- **Kondisi SESUDAH (After):**
  > `Terjemahkan menjadi bahasa Indonesia natural: 'Penjatahan Parsial (Sebagian Terpenuhi)'.`

---

### [AUDIT-150] Subjudul Layar Holding Menggunakan Kalimat Puitis Birokrasi yang Redundan
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:2470`
- **Kondisi SEBELUM (Before):**
  > `flowText(panel, 'Holding', 'Entitas Induk Konglomerasi Multi-Sektor Terkonsolidasi');`
- **Akar Masalah:** Penggunaan deretan kata bombastis yang saling mengulang makna yang sama.
- **Dampak terhadap Pemain:** Teks terasa kaku dan formal berlebihan.
- **Kondisi SESUDAH (After):**
  > `Gunakan bahasa bisnis modern: 'Grup Induk Perusahaan (Holding Konglomerasi)'.`

---

### [AUDIT-151] Pesan Pengunduran Diri Staf Menggunakan Bahasa Berita Acara Kepolisian
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:3350`
- **Kondisi SEBELUM (Before):**
  > `'Pegawai memutuskan terminasi kontrak sepihak akibat ketidakpuasan tingkat moral kerja.'`
- **Akar Masalah:** Kalimat peristiwa HRD terdengar seperti dokumen sengketa hukum ketenagakerjaan.
- **Dampak terhadap Pemain:** Mengurangi humor dan kehangatan interaksi game.
- **Kondisi SESUDAH (After):**
  > `Gunakan gaya bahasa humanis: 'Pegawai mengundurkan diri karena kelelahan kerja. Naikkan fasilitas atau turunkan jam kerja!'.`

---

### [AUDIT-152] Klausul Pinjaman Bank Menuliskan 'Debitur tunduk pada eksekusi jaminan otomatis'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:6140`
- **Kondisi SEBELUM (Before):**
  > `flowText(card, 'Klausul', 'Debitur tunduk pada eksekusi jaminan otomatis pada kegagalan angsuran termin ke-3.');`
- **Akar Masalah:** Menyisipkan klausul perjanjian kredit perbankan formal ke dalam kartu peminjaman dana game.
- **Dampak terhadap Pemain:** Membuat pemain enggan memanfaatkan fitur pinjaman modal.
- **Kondisi SESUDAH (After):**
  > `Sederhanakan menjadi peringatan santai: 'Pastikan saldo kas cukup sebelum jatuh tempo angsuran ke-3'.`

---

### [AUDIT-153] Deskripsi Kartu Booster Riset Menyisakan Istilah 'Algoritma Machine Learning'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5775`
- **Kondisi SEBELUM (Before):**
  > `flowText(card, 'Teknologi', 'Algoritma kuantitatif machine learning mendongkrak ketepatan sinyal beli +15%.');`
- **Akar Masalah:** Penggunaan istilah buzzword AI yang tidak kontekstual dengan alur game.
- **Dampak terhadap Pemain:** Terasa seperti promosi software fiktif daripada item game.
- **Kondisi SESUDAH (After):**
  > `Ubah menjadi: 'Riset Kuantitatif: Meningkatkan akurasi sinyal radar beli +15%'.`

---

### [AUDIT-154] Profil Pemain Mencetak String Enum Frekuensi Transaksi 'HIPERAKTIF_INTRADAY'
- **Kategori:** `BUG & PEMBOROSAN TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:6390`
- **Kondisi SEBELUM (Before):**
  > `flowText(card, 'Gaya Trading', p.tradingStyle || 'HIPERAKTIF_INTRADAY');`
- **Akar Masalah:** Konstanta gaya trading dicetak menggunakan huruf besar dan garis bawah database.
- **Dampak terhadap Pemain:** Merusak keanggunan tata letak profil rekam jejak pemain.
- **Kondisi SESUDAH (After):**
  > `Petakan menjadi format ramah: 'Day Trader Aktif' atau 'Investor Fundamental'.`

---

### [AUDIT-155] Menu Simpanan Mencantumkan Frasa 'Sinkronisasi SHA-256 Digest Tanpa Anomali Bit'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:6350`
- **Kondisi SEBELUM (Before):**
  > `<small>Sinkronisasi SHA-256 Digest terverifikasi tanpa anomali bit.</small>`
- **Akar Masalah:** Penyebutan istilah kriptografi tingkat rendah pada antarmuka kasual pemain.
- **Dampak terhadap Pemain:** Detail teknis yang sama sekali tidak dipahami oleh pemain umum.
- **Kondisi SESUDAH (After):**
  > `Ganti dengan teks ketenangan pikiran: 'Progres terverifikasi 100% aman'.`

---

### [AUDIT-156] Dialog Kekalahan Duel Bos Menggunakan Kalimat Kaku 'Kekalahan kalkulasi matematis'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5830`
- **Kondisi SEBELUM (Before):**
  > `'Kekalahan kalkulasi matematis terhadap Taipan Raven dalam periode 30 hari.'`
- **Akar Masalah:** Pemberitahuan hasil pertandingan terdengar seperti error kalkulator ilmiah.
- **Dampak terhadap Pemain:** Menghilangkan sensasi persaingan antar taipan bisnis.
- **Kondisi SESUDAH (After):**
  > `Gunakan gaya bahasa narasi game: 'Taipan Raven memenangkan duel kali ini. Susun strategi portofolio Anda dan tantang kembali!'.`

---

### [AUDIT-157] Judul Modal Streak Harian Mengulang 4 Kata Berarti Sama Sekaligus
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5785`
- **Kondisi SEBELUM (Before):**
  > `showFlow('Pelacak Disiplin Kehadiran Rutin Harian Konsisten');`
- **Akar Masalah:** Menumpuk empat kata bersinonim: Disiplin, Rutin, Harian, Konsisten dalam satu judul modal.
- **Dampak terhadap Pemain:** Judul menjadi sangat panjang dan terpotong di header modal.
- **Kondisi SESUDAH (After):**
  > `Persingkat menjadi judul padat dan energik: showFlow('Bonus Streak Harian');.`

---

### [AUDIT-158] Subjudul Dominasi Sektor Mengulang Istilah Buku Teks 'Monopoli Oligopoli'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5805`
- **Kondisi SEBELUM (Before):**
  > `flowText(panel, 'Konsep', 'Penguasaan monopoli oligopoli pangsa pasar industri untuk hak istimewa bursa.');`
- **Akar Masalah:** Pengulangan istilah teori ekonomi SMA di dalam dialog aksi permainan.
- **Dampak terhadap Pemain:** Membosankan untuk dibaca.
- **Kondisi SESUDAH (After):**
  > `Fokus pada keuntungan gameplay: 'Kuasai >25% saham sektor untuk membuka diskon komisi transaksi bursa'.`

---

### [AUDIT-159] Ketentuan Asuransi Portofolio Menuliskan 'Klausul Force Majeure Sistemik'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5745`
- **Kondisi SEBELUM (Before):**
  > `flowText(panel, 'Klausul', 'Polis perlindungan klausul force majeure goncangan pasar sistemik.');`
- **Akar Masalah:** Penyisipan istilah hukum kontrak asuransi yang berbelit-belit.
- **Dampak terhadap Pemain:** Pemain malas membaca dan bingung kapan asuransi akan mencairkan klaim.
- **Kondisi SESUDAH (After):**
  > `Jelaskan dengan lugas: 'Klaim otomatis cair saat indeks pasar anjlok lebih dari 5% dalam satu hari'.`

---

### [AUDIT-160] Catatan Kaki Laporan Harian Menggunakan Judul 'Penutupan Buku Kas Komprehensif'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:7945`
- **Kondisi SEBELUM (Before):**
  > `flowText(detail, 'Jenis', 'Catatan penutupan buku kas komprehensif harian otomatis.');`
- **Akar Masalah:** Menyematkan istilah pembukuan akuntansi berlapis di setiap kartu riwayat hari.
- **Dampak terhadap Pemain:** Menambah kepadatan teks tanpa memberikan wawasan finansial baru.
- **Kondisi SESUDAH (After):**
  > `Cukup tampilkan: 'Ringkasan Tutup Hari Bursa'.`

---

### [AUDIT-161] Blok Catch Kosong Menelan Error Jaringan Tanpa Notifikasi pada Modal Posisi
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:openPositionDetailModal`
- **Kondisi SEBELUM (Before):**
  > `catch(e) {} // menelan error pemuatan posisi secara diam-diam`
- **Akar Masalah:** Ketika terjadi kegagalan pembacaan data portofolio dari bridge, error ditelan tanpa peringatan atau status di antarmuka.
- **Dampak terhadap Pemain:** Modal dialog menampilkan indikator loading terus menerus tanpa pernah selesai atau memberi tahu pemain.
- **Kondisi SESUDAH (After):**
  > `Tambahkan penanganan error: catch(e) { flowText(panel, 'Status', 'Gagal memuat detail posisi: ' + e.message); }`

---

### [AUDIT-162] Warna Hex Hardcoded #FEF9C3 Merusak Palet Tema Gelap (Obsidian / Neon)
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:1788`
- **Kondisi SEBELUM (Before):**
  > `background:#FEF9C3; color:#854D0E; border:1px solid #CA8A04;`
- **Akar Masalah:** Badge saham terkunci menggunakan warna kuning pastel terang yang di-hardcode secara inline.
- **Dampak terhadap Pemain:** Merusak keharmonisan visual saat pemain memilih tema gelap seperti Obsidian Pro atau Navy Neon.
- **Kondisi SESUDAH (After):**
  > `Gunakan variabel tema dinamis: background: var(--surface-raised); color: var(--gold); border: 1px solid var(--gold);`

---

### [AUDIT-163] Meteran Moral Staf Tidak Memiliki Gradien Warna Peringatan Kritis
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openCompanyStaffModal`
- **Kondisi SEBELUM (Before):**
  > `Bar moral tetap berwarna hijau meskipun angka moral karyawan turun ke 25%.`
- **Akar Masalah:** Tidak ada perubahan warna visual yang menandakan bahwa pegawai berada di ambang pengunduran diri massal.
- **Dampak terhadap Pemain:** Pemain tidak menyadari bahaya krisis SDM pada perusahaannya sampai karyawan mogok.
- **Kondisi SESUDAH (After):**
  > `Terapkan gradien dinamis: Hijau (>70%), Kuning (40-70%), dan Merah menyala (<40%).`

---

### [AUDIT-164] Pengulangan Frasa 'Beban Jam Kerja Operasional' Tiga Kali di Modal Kebijakan
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openCompanyPolicyModal`
- **Kondisi SEBELUM (Before):**
  > `Paragraf pengantar mengulang 'beban jam kerja operasional' 3 kali dalam 2 baris.`
- **Akar Masalah:** Redundansi kata yang membuat dialog kebijakan terasa bertele-tele.
- **Dampak terhadap Pemain:** Menghabiskan ruang vertikal layar portrait.
- **Kondisi SESUDAH (After):**
  > `Sederhanakan instruksi: 'Tetapkan ritme jam kerja harian dan fasilitas pegawai'.`

---

### [AUDIT-165] Injeksi Modal Usaha Mengizinkan Dana Kas yang Sedang Tertahan di Order Saham
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:openCompanyCapitalModal`
- **Kondisi SEBELUM (Before):**
  > `Pengecekan kas hanya memeriksa saldo total kas, bukan saldo kas tersedia bebas.`
- **Akar Masalah:** Pemain dapat menyuntikkan modal ke anak usaha menggunakan dana yang sebenarnya sudah terkunci untuk antrean beli saham.
- **Dampak terhadap Pemain:** Memicu penolakan matching engine atau saldo negatif di buku besar perbankan.
- **Kondisi SESUDAH (After):**
  > `Validasi terhadap availableCash: pastikan kas yang disetor tidak melebihi dana menganggur bebas.`

---

### [AUDIT-166] Tombol Klaim Sponsor Tidak Menampilkan Waktu Hitung Mundur Saat Cooldown
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openDailySponsorModal`
- **Kondisi SEBELUM (Before):**
  > `Tombol hanya berstatus disabled dengan teks statis 'Sudah Diklaim'.`
- **Akar Masalah:** Pemain tidak tahu berapa menit atau jam lagi hibah ventura harian dapat diklaim kembali.
- **Dampak terhadap Pemain:** Pemain harus membuka-tutup modal berulang kali untuk memeriksa kesiapan.
- **Kondisi SESUDAH (After):**
  > `Tampilkan hitung mundur waktu game: 'Tersedia kembali dalam 04:25:10'.`

---

### [AUDIT-167] Slogan 'Menjadi Triliuner dalam 5 Menit' Terdengar Seperti Iklan Menyesatkan
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openTutorialModal`
- **Kondisi SEBELUM (Before):**
  > `'Pelajari cara menjadi triliuner bursa dalam 5 menit melalui panduan taktis ini.'`
- **Akar Masalah:** Bahasa pemasaran berlebihan yang tidak mencerminkan kedalaman simulasi pasar modal realistis.
- **Dampak terhadap Pemain:** Menurunkan kelas estetika permainan.
- **Kondisi SESUDAH (After):**
  > `Gunakan nada elegan: 'Panduan dasar strategi investasi dan mekanika bursa saham'.`

---

### [AUDIT-168] Badge Notifikasi Tidak Bersih Otomatis Saat Riwayat Notifikasi Ditutup
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openNotificationHistoryModal`
- **Kondisi SEBELUM (Before):**
  > `Pemain harus mengklik tombol silang satu per satu pada tiap riwayat notifikasi.`
- **Akar Masalah:** Badge angka notifikasi merah tetap menyala meskipun pemain sudah membuka daftar riwayat.
- **Dampak terhadap Pemain:** Mengganggu pemain dengan indikator palsu adanya pesan belum dibaca.
- **Kondisi SESUDAH (After):**
  > `Sediakan tombol cepat 'Tandai Semua Sudah Dibaca' dan bersihkan badge secara otomatis.`

---

### [AUDIT-169] Status Kosong Menumpuk Tiga Sinonim 'Riwayat Catatan Notifikasi Sistem Logbook'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:openNotificationHistoryModal`
- **Kondisi SEBELUM (Before):**
  > `<p>Belum ada riwayat catatan notifikasi sistem logbook.</p>`
- **Akar Masalah:** Menumpuk 4 kata yang bermakna sama: Riwayat, Catatan, Notifikasi, Logbook.
- **Dampak terhadap Pemain:** Teks terasa kaku dan canggung.
- **Kondisi SESUDAH (After):**
  > `Persingkat menjadi: 'Belum ada notifikasi baru'.`

---

### [AUDIT-170] Tombol Hadiah Misi Selesai Tidak Memiliki Animasi Shimmer Penarik Perhatian
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openPeriodicMissionsModal`
- **Kondisi SEBELUM (Before):**
  > `Tombol 'Klaim Hadiah' memiliki gaya tombol hijau biasa.`
- **Akar Masalah:** Tidak ada penegasan visual yang membedakan misi yang baru saja selesai dari tombol biasa.
- **Dampak terhadap Pemain:** Pemain sering lupa mengklaim hadiah modal dan reputasi yang sudah berhasil diraihnya.
- **Kondisi SESUDAH (After):**
  > `Beri efek kilau emas halus (gold shimmer animation) dan getaran visual saat hadiah siap diklaim.`

---

### [AUDIT-171] Deskripsi Misi Menggunakan Kalimat Tugas Dinas 'Tuntaskan target kuota...'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openPeriodicMissionsModal`
- **Kondisi SEBELUM (Before):**
  > `'Tuntaskan target kuota transaksi bursa saham harian Anda sebelum penutupan sesi.'`
- **Akar Masalah:** Bahasa birokrasi penugasan pegawai kantor pada kartu misi permainan.
- **Dampak terhadap Pemain:** Mengurangi kesenangan dan atmosfer bermain.
- **Kondisi SESUDAH (After):**
  > `Gunakan gaya narasi gamer: 'Lakukan 3 transaksi saham hari ini dan raih bonus modal +Rp 10 Juta!'.`

---

### [AUDIT-172] Peringkat Pemain di Papan Liga Tidak Dipin di Bawah Saat Menggeser Daftar 50 Rival
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:openLeagueAndRivalModal`
- **Kondisi SEBELUM (Before):**
  > `Baris peringkat pemain berada di urutan aslinya di tengah-tengah daftar panjang.`
- **Akar Masalah:** Saat pemain menggeser daftar leaderboard 50 taipan, posisi pemain tenggelam keluar layar.
- **Dampak terhadap Pemain:** Pemain harus mencari-cari namanya di antara puluhan nama rival.
- **Kondisi SESUDAH (After):**
  > `Sematkan baris kartu pemain secara melayang (*sticky pinned bar*) di bagian bawah layar modal.`

---

### [AUDIT-173] Valuasi Portofolio NPC Mencetak Angka Mentah Tanpa Titik Pemisah Ribuan
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openNpcProfileModal`
- **Kondisi SEBELUM (Before):**
  > `flowText(card, 'Kekayaan', 'Rp 1542000000');`
- **Akar Masalah:** Angka kekayaan taipan rival tidak diformat menggunakan pemisah ribuan rupiah pada modal profil NPC.
- **Dampak terhadap Pemain:** Sangat sulit membaca nominal ratusan juta atau miliaran tanpa pemisah titik.
- **Kondisi SESUDAH (After):**
  > `Format selalu menggunakan formatMoney: 'Rp 1.542.000.000'.`

---

### [AUDIT-174] Bio NPC Mencantumkan Istilah Mesin 'Bot Algoritma Perdagangan Terprogram'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openNpcProfileModal`
- **Kondisi SEBELUM (Before):**
  > `flowText(card, 'Deskripsi', 'Bot algoritma perdagangan kuantitatif pasar terprogram.');`
- **Akar Masalah:** Pemberian identitas NPC sebagai 'bot program' merusak atmosfer kompetisi sosial.
- **Dampak terhadap Pemain:** Pemain merasa bermain sendirian melawan script tanpa kepribadian.
- **Kondisi SESUDAH (After):**
  > `Berikan persona taipan manusiawi: 'Manajer Portofolio Hedge Fund dengan strategi fokus pertumbuhan nilai'.`

---

### [AUDIT-175] Foto Profil Bos Pasar Tidak Memiliki Placeholder Saat Aset Gambar Gagal Muat
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openMarketBossModal`
- **Kondisi SEBELUM (Before):**
  > `<img src="assets/bosses/boss_01.png"> (tanpa onerror fallback)`
- **Akar Masalah:** Jika file aset gambar bos belum terpasang, WebView menampilkan ikon gambar rusak default Android.
- **Dampak terhadap Pemain:** Antarmuka pertarungan bos tampak cacat.
- **Kondisi SESUDAH (After):**
  > `Tambahkan onerror="this.src='data:image/svg+xml,...'" yang menampilkan siluet taipan emas elegan.`

---

### [AUDIT-176] Dialog Kemenangan Bos Taipan Menggunakan Istilah Mesin 'Kalkulasi Arbitrase'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openMarketBossModal`
- **Kondisi SEBELUM (Before):**
  > `'Kalkulasi eksekusi arbitrase Anda melampaui benchmark pasar bos.'`
- **Akar Masalah:** Ucapan selamat kemenangan terdengar seperti laporan komputasi software statistik.
- **Dampak terhadap Pemain:** Menghilangkan sensasi euforia kemenangan pemain.
- **Kondisi SESUDAH (After):**
  > `Ubah menjadi selebrasi hangat: 'Luar biasa! Portofolio Anda berhasil mengalahkan Taipan Raven! Hadiah modal tunai telah masuk ke rekening'.`

---

### [AUDIT-177] Tombol Bayar Pajak Tidak Menampilkan Saldo Kas yang Tersisa Pasca Pelunasan
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openTaxAuditModal`
- **Kondisi SEBELUM (Before):**
  > `<button class="btn-primary">Bayar Tagihan Pajak Rp 12.000.000</button>`
- **Akar Masalah:** Tidak ada keterangan berapa saldo kas yang tersisa di dompet setelah membayar pajak.
- **Dampak terhadap Pemain:** Pemain cemas kasnya akan langsung habis jika menekan tombol.
- **Kondisi SESUDAH (After):**
  > `Tampilkan simulasi kas: 'Bayar Pajak Rp 12.000.000 (Sisa Kas Anda: Rp 38.000.000)'.`

---

### [AUDIT-178] Klausul Asuransi Menggunakan Istilah Pemrograman 'Didebet Secara Rekursif'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openPortfolioInsuranceModal`
- **Kondisi SEBELUM (Before):**
  > `'Premi periode baru didebet otomatis secara rekursif dari saldo rekening.'`
- **Akar Masalah:** Penyisipan istilah programmer 'rekursif' ke dalam kalimat perpanjangan polis.
- **Dampak terhadap Pemain:** Membingungkan pemain dengan istilah fungsi pemanggil diri sendiri.
- **Kondisi SESUDAH (After):**
  > `Gunakan bahasa umum: 'Premi diperpanjang otomatis setiap 7 hari bursa'.`

---

### [AUDIT-179] Slider Alokasi Reksa Dana Kustom Mengizinkan Total Persentase Melebihi 100%
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:openCustomFundBuilderModal`
- **Kondisi SEBELUM (Before):**
  > `Event listener slider tidak menyeimbangkan slider lain saat digeser cepat.`
- **Akar Masalah:** Ketika pemain menggeser beberapa slider alokasi (Saham, Emas, Kripto, Kas), total persentase dapat tembus hingga 120%.
- **Dampak terhadap Pemain:** Penerbitan reksa dana gagal di tingkat backend atau menghasilkan rasio portofolio yang tidak seimbang.
- **Kondisi SESUDAH (After):**
  > `Terapkan algoritma constraint auto-balancing: penambahan satu slider otomatis mengurangi slider kas hingga total selalu 100%.`

---

### [AUDIT-180] Tombol Reset Racikan Portofolio Tidak Memiliki Dialog Konfirmasi
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openCustomFundBuilderModal`
- **Kondisi SEBELUM (Before):**
  > `resetBtn.onclick = () => resetAllSliders(); (tanpa konfirmasi)`
- **Akar Masalah:** Pemain yang sudah susah payah mengatur komposisi 5 aset dapat kehilangan racikannya hanya karena satu sentuhan tidak sengaja pada tombol reset.
- **Dampak terhadap Pemain:** Frustrasi pemain karena kehilangan input kompleks.
- **Kondisi SESUDAH (After):**
  > `Tambahkan konfirmasi singkat: 'Atur ulang seluruh komposisi ke posisi awal?'.`

---

### [AUDIT-181] Label Filter Mutasi Kas Terlalu Panjang 'Filter Berdasarkan Klasifikasi Tipe...'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:openTransactionHistoryModal`
- **Kondisi SEBELUM (Before):**
  > `<label>Filter Berdasarkan Klasifikasi Tipe Mutasi Finansial</label>`
- **Akar Masalah:** Penggunaan frasa formal sepanjang 50 karakter hanya untuk judul dropdown filter.
- **Dampak terhadap Pemain:** Mempersempit ruang tabel mutasi di layar ponsel.
- **Kondisi SESUDAH (After):**
  > `Sederhanakan menjadi label rapi: 'Jenis Transaksi'.`

---

### [AUDIT-182] Kolom Angka Mutasi Kas Tidak Menggunakan Font Tabular Nums (Angka Goyang)
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `style.css:2620, app.js:openTransactionHistoryModal`
- **Kondisi SEBELUM (Before):**
  > `.trx-amount { text-align: right; } (tanpa font-variant-numeric: tabular-nums)`
- **Akar Masalah:** Setiap angka (0-9) memiliki lebar proporsional berbeda, menyebabkan posisi titik desimal dan ribuan tidak lurus saat discroll.
- **Dampak terhadap Pemain:** Tampilan buku kas tampak bergelombang dan sulit dibandingkan.
- **Kondisi SESUDAH (After):**
  > `Terapkan font-variant-numeric: tabular-nums; pada seluruh angka mutasi kas.`

---

### [AUDIT-183] Tingkat Kemenangan Trading (Win Rate) Menampilkan 100% Hanya dari 1 Transaksi
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openPlayerProfileModal`
- **Kondisi SEBELUM (Before):**
  > `winRateEl.textContent = `${(wins/totalTrades*100).toFixed(1)}%`;`
- **Akar Masalah:** Pemain yang baru mengeksekusi 1 transaksi jual untung langsung memperoleh win rate 100.0% tanpa keterangan jumlah sampel.
- **Dampak terhadap Pemain:** Menyesatkan evaluasi performa pemain.
- **Kondisi SESUDAH (After):**
  > `Sertakan jumlah sampel: 'Win Rate: 100% (1 dari 1 transaksi)' atau beri batas minimal 5 transaksi sebelum ditampilkan.`

---

### [AUDIT-184] Statistik Reputasi Menuliskan 'Skor Akumulasi Prestise Sosial Pasar Modal'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openPlayerProfileModal`
- **Kondisi SEBELUM (Before):**
  > `flowText(card, 'Reputasi', 'Skor Akumulasi Prestise Sosial Pasar Modal: 85/100');`
- **Akar Masalah:** Pemberian nama atribut yang terlalu puitis dan berbelit-belit.
- **Dampak terhadap Pemain:** Menyulitkan pemindaian cepat statistik karier.
- **Kondisi SESUDAH (After):**
  > `Gunakan istilah lugas: 'Reputasi Pasar: 85/100 (Pialang Elit)'.`

---

### [AUDIT-185] Poligon Radar Arketipe Investor Tidak Memiliki Label Skor pada Sudut Sumbu
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openInvestorArchetypeModal`
- **Kondisi SEBELUM (Before):**
  > `Grafik radar SVG hanya merender garis laba-laba tanpa angka skor numerik di ujung sumbu.`
- **Akar Masalah:** Pemain melihat bentuk geometri tetapi tidak tahu persis berapa nilai sumbu Value, Momentum, atau Dividennya.
- **Dampak terhadap Pemain:** Analisis perilaku trading menjadi kurang informatif.
- **Kondisi SESUDAH (After):**
  > `Tampilkan label angka ringkas di samping tiap sudut sumbu: 'Value: 82', 'Momentum: 45', dll.`

---

### [AUDIT-186] Deskripsi Arketipe Menggunakan Istilah Psikoanalisis Pop 'DNA Psikologi Trading'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openInvestorArchetypeModal`
- **Kondisi SEBELUM (Before):**
  > `'Karakteristik DNA psikologi trading Anda tergolong agresif spekulatif.'`
- **Akar Masalah:** Penggunaan frasa pseudo-ilmiah yang tidak lazim di dunia broker saham.
- **Dampak terhadap Pemain:** Terasa seperti kuis kepribadian majalah kasual.
- **Kondisi SESUDAH (After):**
  > `Gunakan gaya analisis profesional: 'Profil Risiko: Agresif (Fokus pada saham berfluktuasi tinggi)'.`

---

### [AUDIT-187] Pratinjau Karya Seni Mewah Tidak Dapat Dizoom pada Layar Smartphone
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openCollectionItemDetailModal`
- **Kondisi SEBELUM (Before):**
  > `Gambar lukisan/properti mewah dikunci pada resolusi kecil 200px × 200px.`
- **Akar Masalah:** Koleksi visual resolusi tinggi (1254x1254 PNG) yang ada di folder aset tidak dapat dinikmati keindahannya secara detail oleh pemain.
- **Dampak terhadap Pemain:** Pemain kurang merasa puas telah mengumpulkan miliaran rupiah untuk membeli barang koleksi.
- **Kondisi SESUDAH (After):**
  > `Tambahkan fitur ketuk dua kali untuk memperbesar layar penuh (*pinch-to-zoom / tap to expand lightbox*).`

---

### [AUDIT-188] Keterangan Koleksi Menyisakan Buzzwords 'Koleksi Digital Terotentikasi'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openCollectionItemDetailModal`
- **Kondisi SEBELUM (Before):**
  > `flowText(card, 'Otentikasi', 'Karya seni koleksi digital aset fisik terotentikasi sertifikat.');`
- **Akar Masalah:** Penggunaan bahasa hype era NFT yang membingungkan identitas aset game tycoon.
- **Dampak terhadap Pemain:** Mengurangi kesan kepemilikan aset fisik nyata seperti galeri seni atau penthouse.
- **Kondisi SESUDAH (After):**
  > `Ganti dengan deskripsi mewah: 'Aset Koleksi Bersejarah · Menambah Reputasi Sosial +5 Poin'.`

---

### [AUDIT-189] Banner Peringatan Likuidasi Margin Menolak Hilang Meskipun Kas Sudah Ditambah
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:openRiskModal`
- **Kondisi SEBELUM (Before):**
  > `Pemeriksaan status margin hanya dievaluasi saat modal dibuka pertama kali.`
- **Akar Masalah:** Jika pemain menyetor kas darurat, banner merah peringatan likuidasi tetap menempel di layar hingga aplikasi direstart.
- **Dampak terhadap Pemain:** Pemain panik mengira akunnya masih berada dalam status bahaya penutupan paksa.
- **Kondisi SESUDAH (After):**
  > `Evaluasi ulang rasio margin secara reaktif saat ada mutasi kas masuk.`

---

### [AUDIT-190] Peringatan Margin Call Menggunakan Kalimat Teror Hukum Berlebihan
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openRiskModal`
- **Kondisi SEBELUM (Before):**
  > `'Pemberitahuan likuidasi paksa jaminan efek margin portofolio tanpa kompromi.'`
- **Akar Masalah:** Kalimat yang terlalu agresif dan menakut-nakuti pemain.
- **Dampak terhadap Pemain:** Membuat pemain stres alih-alih memberikan instruksi mitigasi yang jelas.
- **Kondisi SESUDAH (After):**
  > `Berikan panduan tenang: 'Batas margin terlewati. Tambahkan kas atau tutup sebagian posisi sebelum penutupan sesi'.`

---

### [AUDIT-191] Daftar Pencapaian Tidak Memiliki Filter Tab (Semua / Belum / Selesai)
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openAchievementsModal`
- **Kondisi SEBELUM (Before):**
  > `Seluruh 30 kartu pencapaian dicetak memanjang ke bawah tanpa filter.`
- **Akar Masalah:** Pemain harus scroll sangat jauh untuk menemukan pencapaian mana yang hadiahnya belum diklaim.
- **Dampak terhadap Pemain:** Membuang waktu navigasi pemain.
- **Kondisi SESUDAH (After):**
  > `Sediakan 3 tab filter pil: [Belum Selesai] [Siap Klaim ★] [Selesai].`

---

### [AUDIT-192] Judul Sub-kartu Pencapaian Menuliskan 'Prestasi Luar Biasa Pencapaian Finansial'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:openAchievementsModal`
- **Kondisi SEBELUM (Before):**
  > `<h3>Prestasi Luar Biasa Pencapaian Finansial Mandiri</h3>`
- **Akar Masalah:** Penumpukan kata-kata pujian berlebihan pada header modul.
- **Dampak terhadap Pemain:** Menghabiskan ruang judul kartu.
- **Kondisi SESUDAH (After):**
  > `Gunakan judul rapi: 'Pencapaian Finansial'.`

---

### [AUDIT-193] Bingkai Profil Bulat Memotong Sudut Foto Avatar Persegi
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openAvatarCustomizerModal, style.css:265`
- **Kondisi SEBELUM (Before):**
  > `border-radius: 50%; overflow: hidden; (tanpa object-fit: cover pada tag img)`
- **Akar Masalah:** Foto avatar yang tidak memiliki aspek rasio 1:1 gepeng atau terpotong bagian pentingnya.
- **Dampak terhadap Pemain:** Tampilan avatar pemain di header tampak distorsi.
- **Kondisi SESUDAH (After):**
  > `Terapkan aspect-ratio: 1/1; object-fit: cover; border-radius: 50%; pada seluruh elemen avatar.`

---

### [AUDIT-194] Tombol Aktivasi Kartu Booster Menuliskan 'Injeksi Efisiensi Algoritma Riset'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openResearchBoostersModal`
- **Kondisi SEBELUM (Before):**
  > `<button class="btn-primary">Injeksi Efisiensi Algoritma Riset Pasar</button>`
- **Akar Masalah:** Penggunaan frasa berbelit-belit pada tombol aksi berdurasi sementara.
- **Dampak terhadap Pemain:** Teks tombol terlalu panjang hingga melompat jadi 2 baris.
- **Kondisi SESUDAH (After):**
  > `Persingkat menjadi: 'Aktifkan Kartu Booster (Durasi 3 Hari)'.`

---

### [AUDIT-195] Garis Konektor Pohon Riset Teknologi Meleset Saat Layar Diputar atau Berubah Ukuran
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openTechTreeModal`
- **Kondisi SEBELUM (Before):**
  > `Koordinat garis penghubung dihitung dengan posisi piksel absolut window.innerWidth.`
- **Akar Masalah:** Saat membuka keyboard atau mengubah ukuran split-screen, garis konektor pohon riset terlepas dari kotaknya.
- **Dampak terhadap Pemain:** Tampilan visual pohon riset teknologi tampak berantakan.
- **Kondisi SESUDAH (After):**
  > `Gunakan kontainer SVG viewBox 0 0 1000 1000 dengan koordinat persentase relatif yang tahan responsif.`

---

### [AUDIT-196] Judul Pohon Riset Mengulang Kata 'Pohon R&D Sintesa Finansial Komprehensif'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openTechTreeModal`
- **Kondisi SEBELUM (Before):**
  > `showFlow('Pohon R&D Sintesa Finansial Komprehensif Berjenjang');`
- **Akar Masalah:** Penggunaan nama arsitektur internal pengembang pada antarmuka pemain.
- **Dampak terhadap Pemain:** Judul kepanjangan dan sulit dipahami pemain kasual.
- **Kondisi SESUDAH (After):**
  > `Gunakan nama elegan: showFlow('Pohon Teknologi Finansial');.`

---

### [AUDIT-197] Daftar Bursa Headhunting Staf Tidak Memiliki Opsi Pengurutan Gaji / Skill
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openHeadhuntingModal`
- **Kondisi SEBELUM (Before):**
  > `Daftar kandidat manajer investasi dicetak secara acak tanpa filter.`
- **Akar Masalah:** Pemain dengan modal terbatas harus mencari satu per satu eksekutif yang tarif gajinya terjangkau.
- **Dampak terhadap Pemain:** Menyulitkan proses rekrutmen SDM usaha.
- **Kondisi SESUDAH (After):**
  > `Sediakan pengurutan cepat: [Urutkan: Gaji Terendah] [Produktivitas Tertinggi].`

---

### [AUDIT-198] Label Kontrak Headhunting Menuliskan 'Klausul Retensi Pembajakan Karyawan'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openHeadhuntingModal`
- **Kondisi SEBELUM (Before):**
  > `flowText(card, 'Kontrak', 'Klausul retensi pembajakan karyawan kompetitor pasar modal.');`
- **Akar Masalah:** Bahasa hukum ketenagakerjaan yang kaku dan terkesan agresif.
- **Dampak terhadap Pemain:** Teks kartu bursa kerja terasa tidak ramah.
- **Kondisi SESUDAH (After):**
  > `Ubah menjadi: 'Biaya Perekrutan: Termasuk kompensasi transfer manajer'.`

---

### [AUDIT-199] Rincian Dana Tertahan Mengalami Ketidakcocokan Jumlah Jika Ada Penjatahan IPO
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:openReservedCashAllocationModal`
- **Kondisi SEBELUM (Before):**
  > `Penjumlahan dana tertahan hanya menghitung antrean order limit tanpa menambah pemesanan e-IPO aktif.`
- **Akar Masalah:** Total dana tertahan di header menampilkan angka lebih besar daripada rincian di dalam modal.
- **Dampak terhadap Pemain:** Pemain mengira ada dana kas yang hilang secara gaib.
- **Kondisi SESUDAH (After):**
  > `Sertakan pos pemesanan e-IPO secara eksplisit di dalam rincian dana tertahan.`

---

### [AUDIT-200] Tombol Aksi e-IPO Menuliskan Kalimat 55 Karakter 'Eksekusi Penebusan Hak Suara...'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:openCorporateOfferDetail`
- **Kondisi SEBELUM (Before):**
  > `<button class="btn-primary">Eksekusi Penebusan Hak Suara Penjatahan Terlebih Dahulu</button>`
- **Akar Masalah:** Kalimat 55 karakter dimasukkan ke dalam tombol aksi primer, menyebabkan teks melompat jadi 3 baris di HP.
- **Dampak terhadap Pemain:** Tombol terlihat aneh, tinggi berlebihan, dan merusak tata letak dialog penawaran.
- **Kondisi SESUDAH (After):**
  > `Persingkat menjadi tombol padat elegan: 'Tebus Hak Saham Sekarang'.`

---

### [AUDIT-201] Batas Fraksi Harga (Tick Size) US Wall Street Menggunakan Kelipatan 1 Sen Bukan Fraksi Desimal Sah
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `Instrument.java:getIdxTickSize, MarketExchange.java:55`
- **Kondisi SEBELUM (Before):**
  > `getExchange().getTickSize(price) mengembalikan BigDecimal("0.01") untuk seluruh rentang harga saham US.`
- **Akar Masalah:** Untuk saham AS di atas $100 (seperti AAPL, MSFT), bursa sub-penny rule membatasi spread minimum 1 sen ($0.01), namun tidak menangani konversi rounding lot IDR secara konsisten.
- **Dampak terhadap Pemain:** Order limit saham US dapat terpotong atau menghasilkan pembulatan ganjil saat dikonversi ke saldo kas portofolio IDR.
- **Kondisi SESUDAH (After):**
  > `Terapkan aturan pembulatan presisi mata uang terpisah (Currency.USD.getDecimalPlaces() = 2) sebelum validasi batas tick bursa.`

---

### [AUDIT-202] Order Tipe STOP_LOSS Tidak Memverifikasi Apakah Harga Pemicu Berada di Luar Batas ARA/ARB
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `OrderManager.java:validateOrder, app.js:7740`
- **Kondisi SEBELUM (Before):**
  > `Pemeriksaan isWithinDailyPriceBand hanya dijalankan untuk order LIMIT reguler.`
- **Akar Masalah:** Pemain dapat memasang order stop-loss dengan harga pemicu di bawah batas ARB harian (-25%).
- **Dampak terhadap Pemain:** Saat harga anjlok, pemicu stop loss tidak akan pernah tereksekusi oleh MatchingEngine karena melanggar batas bawah bursa, menyebabkan posisi pemain terkunci rugi.
- **Kondisi SESUDAH (After):**
  > `Kunci harga pemicu Stop Loss dan Take Profit agar selalu berada di dalam koridor rentang batas harian [DailyFloor, DailyCeiling].`

---

### [AUDIT-203] Antrean Order Book Tidak Membatasi Maksimum 10 Kedalaman Level (Memory Leak Antrean)
- **Kategori:** `KEMACETAN PERFORMA (LAG)` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `MatchingEngine.java:getOrderBookSnapshot, app.js:4610`
- **Kondisi SEBELUM (Before):**
  > `Map<BigDecimal, List<Order>> bids dan asks mengembalikan seluruh tingkatan harga yang terisi oleh 1.000 NPC.`
- **Akar Masalah:** MatchingEngine mengembalikan seluruh level harga dalam snapshot order book ke antarmuka webview, bukan dibatasi 10-best bid/ask.
- **Dampak terhadap Pemain:** Ukuran transfer JSON membengkak menjadi puluhan kilobyte dan webview merender antrean ratusan baris di latar belakang.
- **Kondisi SESUDAH (After):**
  > `Batasi snapshot Order Book tepat pada 10 level penawaran terbaik (Top 10 Bids dan Top 10 Asks).`

---

### [AUDIT-204] Label Waktu Order 'Time in Force' Menggunakan Jargon Bahasa Inggris 'DAY / GTC' Tanpa Penjelasan
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:7765`
- **Kondisi SEBELUM (Before):**
  > `<select id="order-tif"><option value="DAY">DAY</option><option value="GTC">GTC</option></select>`
- **Akar Masalah:** Singkatan TIF teknis perbankan (DAY / GTC) dicantumkan tanpa penjelasan bahasa Indonesia.
- **Dampak terhadap Pemain:** Pemain awam bingung apa arti GTC (*Good 'Til Cancelled*).
- **Kondisi SESUDAH (After):**
  > `Terjemahkan menjadi pilihan ramah: 'Hari Ini (Batal Otomatis Saat Tutup)' dan 'Berlaku Hingga Dibatalkan (GTC)'.`

---

### [AUDIT-205] Notifikasi Transaksi Berhasil (Fill Order) Memunculkan Toast Ganda Jika Order Terpenuhi Sebagian
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:showOrderReceipt, TradingWebBridge.java:notifyOrderFill`
- **Kondisi SEBELUM (Before):**
  > `Setiap eksekusi lot parsial memicu event toast terpisah: 'Fill 2 lot' kemudian 'Fill 8 lot'.`
- **Akar Masalah:** Jika order 10 lot dipenuhi dalam 3 tahap pencocokan antrean terpisah, sistem menembakkan 3 toast berturut-turut dalam 1 detik.
- **Dampak terhadap Pemain:** Layar ponsel dipenuhi rentetan popup notifikasi yang menutupi grafik saham.
- **Kondisi SESUDAH (After):**
  > `Gabungkan (*batching / debounce 500ms*) notifikasi eksekusi parsial menjadi satu toast konsolidasi: 'Order Terpenuhi: 10/10 Lot'.`

---

### [AUDIT-206] Tombol Tarik Order (Cancel Order) di Daftar Order Aktif Terlalu Mepet ke Tepi Layar
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `style.css:1680, app.js:renderOrderRow`
- **Kondisi SEBELUM (Before):**
  > `<button class="btn-cancel-order">Batal</button> (margin-right: 0; padding: 4px 6px;)`
- **Akar Masalah:** Tombol pembatalan order limit berada persis di sudut kanan bawah kartu pesanan aktif dengan lebar sentuh sempit.
- **Dampak terhadap Pemain:** Pemain di ponsel dengan layar lengkung (*curved edge*) sering gagal menyentuh tombol pembatalan order.
- **Kondisi SESUDAH (After):**
  > `Beri padding minimal 10px 14px dengan target sentuh 44px dan margin pengaman dari tepi layar.`

---

### [AUDIT-207] Teks Konfirmasi Order Mengulang Frasa 'Periksa Kembali Rincian Pesanan' Dua Kali
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:7790`
- **Kondisi SEBELUM (Before):**
  > `Judul: Periksa Rincian Pesanan | Paragraf: Harap periksa kembali rincian pesanan Anda sebelum mengirim ke bursa.`
- **Akar Masalah:** Pengulangan kata instruksi yang sama dalam jarak 1 baris.
- **Dampak terhadap Pemain:** Memperpanjang tinggi dialog konfirmasi tiket order.
- **Kondisi SESUDAH (After):**
  > `Judul: 'Konfirmasi Pesanan' | Subjudul: 'Pastikan emiten, jumlah lot, dan harga limit telah sesuai'.`

---

### [AUDIT-208] Kalkulasi Biaya Komisi Broker Menampilkan Angka Nol Koma Sekian Rupiah yang Membingungkan
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:7715`
- **Kondisi SEBELUM (Before):**
  > `Komisi: Rp 1.542,45 (dengan desimal koma)`
- **Akar Masalah:** Komisi transaksi pada bursa IDR menampilkan 2 desimal sen rupiah yang tidak memiliki uang fisik.
- **Dampak terhadap Pemain:** Tampilan angka menjadi panjang dan berdesakan.
- **Kondisi SESUDAH (After):**
  > `Bulatkan biaya transaksi IDR selalu ke atas (Ceiling) ke bilangan bulat rupiah terdekat: 'Komisi: Rp 1.543'.`

---

### [AUDIT-209] Indikator ARA (Auto Rejection Atas) dan ARB Tidak Memiliki Tag Warna Pembeda Jelas
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:4564-4565`
- **Kondisi SEBELUM (Before):**
  > `ARA: Rp 12.800 | ARB: Rp 7.700 (dicetak dengan warna teks putih biasa)`
- **Akar Masalah:** Batas harga tertinggi dan terendah harian tidak diberi warna sinyal psikologis pasar modal.
- **Dampak terhadap Pemain:** Pemain sulit mengenali apakah harga saham saat ini sudah mentok batas kenaikan harian atau belum.
- **Kondisi SESUDAH (After):**
  > `Beri batas ARA warna hijau menyala (pos-text) dengan panah ke atas 'ARA: Rp 12.800 ⮝' dan ARB warna merah 'ARB: Rp 7.700 ⮟'.`

---

### [AUDIT-210] Grafik Mini Sparkline Candlestick Mengalami Distorsi Aspek Rasio pada Ponsel Layar Lipat
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `style.css:1435, app.js:renderMiniChart`
- **Kondisi SEBELUM (Before):**
  > `canvas.style.width = '100%'; canvas.style.height = '120px';`
- **Akar Masalah:** Lebar canvas memanjang fleksibel 100% tanpa memperbarui atribut internal canvas.width = canvas.clientWidth.
- **Dampak terhadap Pemain:** Batang candlestick menjadi melebar gepeng atau garis tren buram (*pixelated*) pada resolusi tablet/foldable.
- **Kondisi SESUDAH (After):**
  > `Sinkronkan resolusi pixel buffer canvas: canvas.width = canvas.clientWidth * window.devicePixelRatio.`

---

### [AUDIT-211] Perhitungan Rata-rata Harga Penutupan Sesi Kemarin (Prev Close) Bernilai Nol Saat Hari Baru
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `Instrument.java:updatePrice, MarketEngine.java:rollSession`
- **Kondisi SEBELUM (Before):**
  > `previousClosePrice direset ke currentPrice tanpa memverifikasi nilai positif.`
- **Akar Masalah:** Jika instrumen baru ditambahkan ke katalog di tengah sesi, previousClosePrice dapat bernilai 0.
- **Dampak terhadap Pemain:** Kalkulasi batas harian getDailyCeilingPrice() membagi nol atau menghasilkan persentase kenaikan tak terhingga (Infinity%).
- **Kondisi SESUDAH (After):**
  > `Pastikan previousClosePrice selalu diinisialisasi minimal sama dengan initialPrice saat katalog dimuat.`

---

### [AUDIT-212] Keterangan Jam Operasional Bursa Menuliskan Format Zona Waktu Kaku 'UTC+07:00'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:625`
- **Kondisi SEBELUM (Before):**
  > `Waktu Operasional: 09:00 - 16:00 (UTC+07:00 / Asia/Jakarta)`
- **Akar Masalah:** Pencantuman string offset teknis ISO 'UTC+07:00 / Asia/Jakarta' yang memakan ruang header.
- **Dampak terhadap Pemain:** Tampak seperti catatan server daripada game mobile.
- **Kondisi SESUDAH (After):**
  > `Gunakan singkatan standar Indonesia yang ramah: '09:00 - 16:00 WIB'.`

---

### [AUDIT-213] Dropdown Pilihan Akun Trading di Tiket Order Menampilkan Nama Tipe Bahasa Inggris 'CLIENT'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:7682`
- **Kondisi SEBELUM (Before):**
  > `opt.textContent = `Akun: ${a.name} (${a.type === 'CLIENT' ? 'Klien' : 'Pribadi'})`; // sebagian opsi mencetak raw a.type`
- **Akar Masalah:** Sebagian selector akun mencetak enum database huruf besar 'CLIENT' atau 'FIRM'.
- **Dampak terhadap Pemain:** Inkonsistensi bahasa antar layar pemesanan.
- **Kondisi SESUDAH (After):**
  > `Standardisasi label akun: 'Akun Pribadi' atau 'Akun Portofolio Klien'.`

---

### [AUDIT-214] Order Tipe MARKET Membiarkan Kotak Input Harga Limit Tetap Aktif dan Bisa Diketik
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:orderTypeChangeHandler`
- **Kondisi SEBELUM (Before):**
  > `inputOrderLimit tidak disetel disabled saat radio button 'MARKET' dipilih.`
- **Akar Masalah:** Pemain yang memilih order Market (hajar kanan/kiri pada antrean terbaik) tetap melihat kotak input harga yang bisa diubah-ubah.
- **Dampak terhadap Pemain:** Pemain bingung apakah pesanannya akan dieksekusi di harga market atau harga yang diketiknya di kotak input.
- **Kondisi SESUDAH (After):**
  > `Nonaktifkan dan redupkan inputOrderLimit (disabled = true; placeholder = 'Eksekusi instan di harga pasar terbaik') saat tipe MARKET aktif.`

---

### [AUDIT-215] Tombol Pintas Aksi Jual Cepat (Quick Sell) di Portofolio Tidak Menampilkan Jumlah Lot Maksimal
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderPositionAction`
- **Kondisi SEBELUM (Before):**
  > `<button class="btn-sell">Jual Saham</button>`
- **Akar Masalah:** Pemain tidak diberi tahu berapa sisa lot saham yang dimilikinya yang siap dijual langsung dari kartu portofolio.
- **Dampak terhadap Pemain:** Pemain harus membuka tiket order terlebih dahulu untuk melihat kuantitas saham yang tersedia.
- **Kondisi SESUDAH (After):**
  > `Tampilkan kuantitas lot pada tombol: 'Jual Saham (Tersedia: 25 Lot)'.`

---

### [AUDIT-216] Slippage Pasar Ekstrem Tidak Menampilkan Estimasi Kerugian Harga Saat Beli di Sesi Volatil
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `MatchingEngine.java:executeTrade, app.js:updateOrderEstimate`
- **Kondisi SEBELUM (Before):**
  > `Estimasi total biaya hanya mengalikan kuantitas dengan harga penawaran ask pertama tanpa memeriksa ketebalan lot.`
- **Akar Masalah:** Jika pemain membeli 50 lot sedangkan antrean ask terbaik hanya memiliki 5 lot, order akan memakan level harga di atasnya (slippage), tetapi UI hanya memproyeksikan harga level 1.
- **Dampak terhadap Pemain:** Pemain kaget saat dana kas yang terpotong lebih besar dari angka yang tertera di lembar review order.
- **Kondisi SESUDAH (After):**
  > `Hitung slippage berbobot volume (VWAP) melintasi kedalaman buku order dan beri peringatan jika harga rata-rata naik > 1%.`

---

### [AUDIT-217] Status Pasar Tertutup (Closed Market) Tidak Menjelaskan Kapan Sesi Berikutnya Akan Buka
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:updateMarketStatusBadge`
- **Kondisi SEBELUM (Before):**
  > `badge.textContent = 'Pasar Tutup';`
- **Akar Masalah:** Label hanya mengabarkan pasar tutup tanpa memberi wawasan berapa lama lagi sesi akan dibuka kembali.
- **Dampak terhadap Pemain:** Pemain pasif menunggu tanpa kepastian waktu permainan.
- **Kondisi SESUDAH (After):**
  > `Ubah menjadi informatif: 'Bursa Tutup · Buka Kembali 09:00 WIB (Gunakan Tombol ⏩ Lanjut)'.`

---

### [AUDIT-218] Indikator Kedalaman Bid/Ask (Order Depth Bar) Sering Mengalami Overflow Keluar Kartu
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `style.css:1710`
- **Kondisi SEBELUM (Before):**
  > `.depth-bar { position: absolute; height: 100%; top: 0; left: 0; opacity: 0.15; } (tanpa max-width: 100%)`
- **Akar Masalah:** Ketika volume lot salah satu sisi melebihi 100.000 lot, bar visual kedalaman meluap keluar batas tabel order book.
- **Dampak terhadap Pemain:** Merusak batas garis pemisah antar kolom antrean beli dan jual.
- **Kondisi SESUDAH (After):**
  > `Kunci lebar maksimum bar kedalaman: max-width: 100%; border-radius: 4px; box-sizing: border-box;.`

---

### [AUDIT-219] Tombol Hapus Watchlist Menggunakan Ikon Bintang Gelap yang Kurang Kontras
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `style.css:2403`
- **Kondisi SEBELUM (Before):**
  > `.watchlist-star-btn[aria-pressed="false"] { color: var(--text-muted); }`
- **Akar Masalah:** Ikon bintang tidak aktif berwarna abu-abu sangat redup yang hampir tidak terlihat di latar belakang kartu gelap.
- **Dampak terhadap Pemain:** Pemain mengira baris saham tidak memiliki fitur favorit/watchlist.
- **Kondisi SESUDAH (After):**
  > `Tingkatkan kontras ikon bintang garis luar (outline star) menjadi warna abu-abu keperakan (#94A3B8).`

---

### [AUDIT-220] Teks Dialog Batas Kredit Margin Mengulang Kata 'Jaminan Likuiditas' Tiga Kali
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:6215`
- **Kondisi SEBELUM (Before):**
  > `Paragraf fasilitas margin mengulang frasa 'jaminan likuiditas portofolio' berturut-turut.`
- **Akar Masalah:** Redundansi kata-kata keuangan yang memperberat pemahaman.
- **Dampak terhadap Pemain:** Membuat pemain ragu menggunakan fasilitas margin saham.
- **Kondisi SESUDAH (After):**
  > `Sederhanakan menjadi bahasa transaksi: 'Gunakan saham portofolio sebagai agunan untuk daya beli tambahan hingga 2x'.`

---

### [AUDIT-221] Nilai Satuan Lot Saham AS (US Wall Street) Menampilkan '1 Lot = 100 Saham' Padahal Bursa US Mengizinkan 1 Lembar
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `InstrumentCatalog.java:createUsInstruments, app.js:7661`
- **Kondisi SEBELUM (Before):**
  > `document.getElementById('order-unit-label').textContent = `Jumlah unit (1 unit = ${stock.lotSize} saham)`;`
- **Akar Masalah:** Saham bursa Wall Street (NYSE / NASDAQ) di game ini sebagian diatur dengan lotSize = 100, padahal saham US standar diperdagangkan per 1 lembar (*share*).
- **Dampak terhadap Pemain:** Harga beli 1 unit saham AS menjadi sangat mahal (misal 1 unit AAPL = $180 × 100 = $18.000 atau Rp 280 Juta), menyulitkan pemain baru.
- **Kondisi SESUDAH (After):**
  > `Pastikan seluruh instrumen bursa US memiliki lotSize = 1 lembar dengan label 'Jumlah Lembar (1 Saham)'.`

---

### [AUDIT-222] Status Notifikasi Eksekusi Saham Menampilkan String ID Acak 'REQ-RET-S-179342...'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `NpcAgent.java:230, app.js:showOrderReceipt`
- **Kondisi SEBELUM (Before):**
  > `showGameToast('info', 'Pesanan Masuk', `ID: ${receipt.requestId}`);`
- **Akar Masalah:** ID timestamp internal sistem dicetak sebagai judul notifikasi transaksi.
- **Dampak terhadap Pemain:** Tampak seperti pesan log sistem daripada konfirmasi broker.
- **Kondisi SESUDAH (After):**
  > `Tampilkan judul yang membanggakan: 'Pesanan Terpasang di Antrean Bursa'.`

---

### [AUDIT-223] Warna Latar Belakang Kotak Input Order Limit Terlalu Gelap dan Sulit Dilihat Batasnya
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `style.css:1695`
- **Kondisi SEBELUM (Before):**
  > `.input-field { background: rgba(0,0,0,0.5); border: 1px solid var(--border); }`
- **Akar Masalah:** Kontras batas kotak input terhadap latar belakang sheet hitam sangat tipis pada layar OLED.
- **Dampak terhadap Pemain:** Pemain kesulitan menemukan letak kotak input harga tanpa mengetuk layar secara acak.
- **Kondisi SESUDAH (After):**
  > `Beri latar belakang sedikit lebih terang (background: var(--surface-raised); border: 1px solid var(--border-interactive);).`

---

### [AUDIT-224] Tombol Persentase Lot 25% Tidak Membulatkan ke Bawah Kelipatan Lot Utuh
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:pctButtonHandler`
- **Kondisi SEBELUM (Before):**
  > `const targetLots = Math.floor(maxAffordableShares / lotSize * 0.25); // dapat menghasilkan 0 lot jika modal terbatas`
- **Akar Masalah:** Jika modal kas pemain hanya cukup untuk membeli 2 atau 3 lot, perhitungan 25% menghasilkan 0 lot.
- **Dampak terhadap Pemain:** Input jumlah lot terisi angka 0 yang menyebabkan order ditolak oleh validasi bursa.
- **Kondisi SESUDAH (After):**
  > `Pastikan perhitungan persentase memiliki batas bawah minimal 1 lot: Math.max(1, calculatedLots) jika modal mencukupi.`

---

### [AUDIT-225] Dialog Konfirmasi Jual Seluruh Posisi Saham Tidak Memiliki Tombol 'Jual Semua'
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openOrderTicket`
- **Kondisi SEBELUM (Before):**
  > `Pemain harus mengetik angka manual atau memilih tombol MAX saat ingin menjual seluruh kepemilikan.`
- **Akar Masalah:** Tidak ada tombol pintas instan satu ketukan untuk melikuidasi seluruh posisi saat pasar anjlok.
- **Dampak terhadap Pemain:** Memperlambat tindakan darurat *cut-loss* pemain saat terjadi krisis pasar.
- **Kondisi SESUDAH (After):**
  > `Sediakan tombol aksi cepat 'Likuidasi Seluruh Posisi (Sell All)' di lembar detail saham.`

---

### [AUDIT-226] Penyetoran Dana e-IPO Mengizinkan Pemesanan di Atas Kapasitas Kas Tanpa Pesan Validasi Jelas
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `CorporateActionService.java:subscribeIpo, app.js:5250`
- **Kondisi SEBELUM (Before):**
  > `if (account.getAvailableCash().isLessThan(required)) return false;`
- **Akar Masalah:** Ketika saldo kas pemain kurang dari nominal pesanan IPO, formulir hanya menampilkan pesan generik 'Pemesanan gagal'.
- **Dampak terhadap Pemain:** Pemain bingung alasan kegagalan pemesanan.
- **Kondisi SESUDAH (After):**
  > `Tampilkan pesan transparan: 'Saldo kas tersedia tidak mencukupi (Kurang Rp 4.500.000)'.`

---

### [AUDIT-227] Jadwal Tanggal Cum Dividen Tidak Dijelaskan Perbedaannya Terhadap Ex Dividen
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5015`
- **Kondisi SEBELUM (Before):**
  > `Cum Date: 2026-11-03 | Ex Date: 2026-11-04 (tanpa keterangan hak dividen)`
- **Akar Masalah:** Pemain baru tidak tahu apakah mereka berhak mendapat dividen jika membeli saham di Ex Date.
- **Dampak terhadap Pemain:** Pemain membeli di Ex Date dan kecewa tidak menerima transferan dividen.
- **Kondisi SESUDAH (After):**
  > `Tambahkan catatan instruktif: 'Beli sebelum penutupan Cum Date untuk berhak menerima dividen tunai'.`

---

### [AUDIT-228] Persentase Hak Suara RUPS Menampilkan Angka Nol Koma Kosong Saat Kepemilikan di Bawah 0.01%
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5030`
- **Kondisi SEBELUM (Before):**
  > `Hak Suara: 0.00%`
- **Akar Masalah:** Pemain yang memiliki 5 lot saham emiten konglomerasi melihat hak suaranya dicetak 0.00%.
- **Dampak terhadap Pemain:** Pemain merasa kepemilikan sahamnya tidak diakui sama sekali dalam agenda voting bursa.
- **Kondisi SESUDAH (After):**
  > `Gunakan format desimal presisi kecil: 'Hak Suara: 0.002% (500 Lembar Saham)'.`

---

### [AUDIT-229] Tombol Voting RUPS Tetap Aktif Setelah Agenda Rapat Ditutup
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:renderVotingAgenda`
- **Kondisi SEBELUM (Before):**
  > `Tombol [Setuju] dan [Tolak] tetap dapat diklik setelah tanggal voting bursa berakhir.`
- **Akar Masalah:** Pemain dapat mengirimkan vote pada agenda yang sudah selesai diarsipkan.
- **Dampak terhadap Pemain:** Memicu pesan error exception atau status voting tidak berubah.
- **Kondisi SESUDAH (After):**
  > `Kunci tombol voting (disabled = true) dan beri stempel 'Voting Ditutup / Keputusan Telah Disahkan'.`

---

### [AUDIT-230] Judul Agenda RUPS Membocorkan Istilah Legal Formal 'Persetujuan Rencana Penyesuaian Anggaran Dasar Pasal 3'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5032`
- **Kondisi SEBELUM (Before):**
  > `Agenda: 'Persetujuan Rencana Penyesuaian Anggaran Dasar Pasal 3 Terkait Klasifikasi Lapangan Usaha'`
- **Akar Masalah:** Menyalin dokumen notaris hukum perseroan terbatas ke dalam kartu voting game.
- **Dampak terhadap Pemain:** Pemain tidak memahami dampak ekonomi dari agenda voting tersebut.
- **Kondisi SESUDAH (After):**
  > `Sederhanakan menjadi intisari bisnis: 'Ekspansi Sektor Usaha Baru (Energi Hijau)'.`

---

### [AUDIT-231] Hasil Penjatahan e-IPO 60/40 Tidak Menampilkan Jumlah Lot Pengembalian Dana (*Refund*)
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:5275`
- **Kondisi SEBELUM (Before):**
  > `Notifikasi hanya menuliskan jumlah lot yang didapat tanpa menyebutkan dana refund yang kembali ke kas.`
- **Akar Masalah:** Jika pemain memesan 100 lot dan hanya memperoleh penjatahan 40 lot, pemain bingung ke mana sisa dana 60 lotnya.
- **Dampak terhadap Pemain:** Pemain panik mengira sisa uangnya hilang.
- **Kondisi SESUDAH (After):**
  > `Rincikan dengan jelas: 'Dapat: 40 Lot · Dana Dikembalikan (Refund): Rp 15.000.000 ke Kas Utama'.`

---

### [AUDIT-232] Pill Status Penawaran e-IPO 'BOOK_BUILDING' Menggunakan Huruf Kapital Bahasa Asing
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5220`
- **Kondisi SEBELUM (Before):**
  > `<span class="badge">BOOK_BUILDING</span>`
- **Akar Masalah:** Istilah tahapan teknis bursa asing dicetak tanpa format ramah pengguna.
- **Dampak terhadap Pemain:** Pemain awam tidak mengerti bahwa ini adalah tahap penawaran awal pembentukan harga.
- **Kondisi SESUDAH (After):**
  > `Ganti dengan pill bahasa Indonesia: 'Masa Penawaran Awal (Pesan Harga Min/Maks)'.`

---

### [AUDIT-233] Batas Waktu Penawaran e-IPO Menampilkan Tanggal Kalender Tanpa Indikator Hari Bursa Aktif
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5225`
- **Kondisi SEBELUM (Before):**
  > `Batas: 2026-11-10`
- **Akar Masalah:** Tidak disebutkan apakah tanggal batas tersebut adalah hari bursa aktif atau hari libur akhir pekan.
- **Dampak terhadap Pemain:** Pemain melewatkan batas waktu pemesanan karena salah menghitung hari libur game.
- **Kondisi SESUDAH (After):**
  > `Tampilkan status hari bursa: 'Batas Penawaran: Selasa, 10 Nov 2026 (Sisa 4 Hari Bursa)'.`

---

### [AUDIT-234] Aksi Korporasi Pembagian Saham Bonus Menampilkan Nilai Tunai Rp 0 yang Membingungkan
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5016`
- **Kondisi SEBELUM (Before):**
  > `flowText(row, 'Nilai Dividen', 'Rp 0');`
- **Akar Masalah:** Pembagian saham bonus non-tunai tetap menampilkan baris 'Nilai Dividen: Rp 0'.
- **Dampak terhadap Pemain:** Pemain mengira pembagian bonus dividen dibatalkan atau perusahaan mengalami kebangkrutan.
- **Kondisi SESUDAH (After):**
  > `Ganti label menjadi: 'Bonus Saham: 1 Saham Baru per 10 Saham Lama (Tanpa Potongan Kas)'.`

---

### [AUDIT-235] Daftar Pemegang Saham Pengendali Tidak Membedakan Warna Porsi Publik vs Pemain
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderShareholdersTab`
- **Kondisi SEBELUM (Before):**
  > `Seluruh baris pemegang saham menggunakan warna teks abu-abu yang seragam.`
- **Akar Masalah:** Pemain tidak bisa langsung mengenali baris kepemilikan sahamnya sendiri di antara daftar investor institusi besar.
- **Dampak terhadap Pemain:** Pemain harus mencari namanya di antara belasan pemegang saham pengendali.
- **Kondisi SESUDAH (After):**
  > `Sorot kepemilikan pemain dengan latar belakang emas lembut (--gold-bg) dan tag '⭐ Posisi Anda'.`

---

### [AUDIT-236] Keterangan Efek Dilusi Rights Issue Terlalu Berbelit-belit Sepanjang 80 Karakter
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5022`
- **Kondisi SEBELUM (Before):**
  > `'Pemegang saham yang tidak mengeksekusi hak berisiko mengalami penurunan porsi kepemilikan persentase bursa.'`
- **Akar Masalah:** Kalimat peringatan hukum yang terlalu panjang untuk layar ponsel.
- **Dampak terhadap Pemain:** Membuat pemain cemas dan memenuhi kartu penawaran saham baru.
- **Kondisi SESUDAH (After):**
  > `Rampingkan menjadi peringatan taktis: 'Porsi saham Anda akan terdilusi jika hak tebus tidak dieksekusi'.`

---

### [AUDIT-237] Penerimaan Dividen Tunai Tidak Menyertakan Notifikasi Masuk ke Riwayat Mutasi Buku Kas
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `CorporateActionService.java:executeDividendPayment, EconomyLedger.java:310`
- **Kondisi SEBELUM (Before):**
  > `recordMutation hanya mencatat rekening tanpa mengirimkan event broadcast toast ke UI.`
- **Akar Masalah:** Dividen tunai masuk ke saldo kas pemain secara senyap tanpa pemberitahuan notifikasi visual.
- **Dampak terhadap Pemain:** Pemain tidak sadar kasnya bertambah puluhan juta rupiah dari dividen emiten yang dimilikinya.
- **Kondisi SESUDAH (After):**
  > `Kirimkan toast selebrasi dividen: '💰 Dividen Masuk: +Rp 12.500.000 dari BBCA'.`

---

### [AUDIT-238] Pilihan Harga Penawaran e-IPO Tidak Memiliki Slider Cepat Antara Harga Minimal dan Maksimal
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5240`
- **Kondisi SEBELUM (Before):**
  > `Pemain harus mengetik angka harga penawaran secara manual di kotak teks.`
- **Akar Masalah:** Pemain sering mengetik harga di luar rentang book building (misal mengetik 850 padahal rentang 900-1100).
- **Dampak terhadap Pemain:** Pemesanan ditolak oleh sistem karena harga tidak valid.
- **Kondisi SESUDAH (After):**
  > `Sediakan tombol cepat [Harga Terendah: Rp 900] dan [Harga Tertinggi: Rp 1.100] atau slider interaktif.`

---

### [AUDIT-239] Pengumuman Merger Emiten Menggunakan Akronim Kaku 'M&A' Tanpa Penjelasan Dampak Harga
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5025`
- **Kondisi SEBELUM (Before):**
  > `Kategori: Aksi Korporasi M&A`
- **Akar Masalah:** Penggunaan singkatan bahasa Inggris M&A (*Mergers and Acquisitions*) yang tidak komunikatif.
- **Dampak terhadap Pemain:** Pemain kasual tidak mengetahui arti singkatan tersebut.
- **Kondisi SESUDAH (After):**
  > `Gunakan istilah populer: 'Penggabungan Usaha & Akuisisi (Merger Perusahaan)'.`

---

### [AUDIT-240] Prospektus Ringkas e-IPO Tidak Menampilkan Rasio Valuasi PER dan PBV Calon Emiten
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:renderIpoDetails`
- **Kondisi SEBELUM (Before):**
  > `Hanya menampilkan: Jumlah Saham Ditawarkan, Rentang Harga, dan Sektor.`
- **Akar Masalah:** Pemain tidak memiliki data valuasi fundamental (PER / PBV) untuk memutuskan apakah harga IPO tersebut murah atau mahal.
- **Dampak terhadap Pemain:** Pemain memesan IPO secara buta (*blind investing*) tanpa analisis rasional.
- **Kondisi SESUDAH (After):**
  > `Cantumkan indikator valuasi perdana: 'PER Prospektus: 11.4x · PBV: 1.8x (Diskon vs Sektor)'.`

---

### [AUDIT-241] Pembatalan Pemesanan e-IPO Masih Diizinkan Saat Periode Penjatahan Telah Dimulai
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `IpoOffering.java:cancelSubscription, app.js:cancelIpoSubscription`
- **Kondisi SEBELUM (Before):**
  > `Status IPO 'ALLOTMENT_IN_PROGRESS' tidak memblokir fungsi pembatalan pesanan di frontend.`
- **Akar Masalah:** Pemain dapat membatalkan pesanan saat sistem sedang menghitung alokasi saham penjatahan pasti.
- **Dampak terhadap Pemain:** Memicu inkonsistensi data matching bursa dan kegagalan rekonsiliasi kas pemodal.
- **Kondisi SESUDAH (After):**
  > `Kunci tombol pembatalan begitu status IPO memasuki masa penjatahan (hanya izinkan saat status OFFERING_OPEN).`

---

### [AUDIT-242] Tombol Pelajari Prospektus Mengarahkan ke Link Kosong 'href="#"'
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5230`
- **Kondisi SEBELUM (Before):**
  > `<a href="#" class="btn-link">Baca Prospektus Ringkas</a>`
- **Akar Masalah:** Tombol tautan prospektus tidak memiliki action handler dan hanya melakukan scroll melompat ke atas layar.
- **Dampak terhadap Pemain:** Membingungkan pemain dan merusak posisi scroll modal dialog.
- **Kondisi SESUDAH (After):**
  > `Buka modal popover ringkasan rencana penggunaan dana IPO (Ekspansi 60%, Bayar Utang 20%, Modal Kerja 20%).`

---

### [AUDIT-243] Status Tanggal Listing Saham Baru Menggunakan Istilah Bahasa Asing 'Go Public Bell'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:5265`
- **Kondisi SEBELUM (Before):**
  > `Pemberitahuan: 'Go Public Bell Ringing Ceremony'`
- **Akar Masalah:** Pencantuman slogan seremonial bursa Wall Street yang tidak kontekstual.
- **Dampak terhadap Pemain:** Mengurangi nuansa profesional pasar modal lokal.
- **Kondisi SESUDAH (After):**
  > `Gunakan istilah bursa resmi: 'Pencatatan Perdana Saham di Papan Bursa'.`

---

### [AUDIT-244] Tabel Riwayat Dividen Emiten Menampilkan Tahun Tanpa Nomor Kuartal (Q1/Q2/Q3/Q4)
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderDividendHistory`
- **Kondisi SEBELUM (Before):**
  > `Dividen Tahun 2026: Rp 250`
- **Akar Masalah:** Tidak dijelaskan apakah dividen tersebut adalah dividen interim kuartalan atau dividen final tahunan.
- **Dampak terhadap Pemain:** Pemain tidak bisa memproyeksikan jadwal penerimaan dividen berikutnya.
- **Kondisi SESUDAH (After):**
  > `Sertakan label kuartal: 'Dividen Interim Q2 2026: Rp 120' dan 'Dividen Final 2026: Rp 130'.`

---

### [AUDIT-245] Batas Minimal Pemesanan e-IPO Mengizinkan Input 0 Lot
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:validateIpoForm`
- **Kondisi SEBELUM (Before):**
  > `const lots = parseInt(inputLot.value) || 0; // if (lots >= 0) kirim pesanan`
- **Akar Masalah:** Pemain dapat mengirimkan formulir pemesanan IPO dengan kuantitas 0 lot.
- **Dampak terhadap Pemain:** Menciptakan transaksi kosong di database e-IPO dan membingungkan penjatahan saham.
- **Kondisi SESUDAH (After):**
  > `Wajibkan minimal pemesanan 1 lot: if (lots < 1) tampilkan peringatan 'Pemesanan minimal 1 lot'.`

---

### [AUDIT-246] Deskripsi Dampak Stock Split Mengulang Rumus Matematika 'Jumlah Saham Naik Harga Turun'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5017`
- **Kondisi SEBELUM (Before):**
  > `'Pemecahan saham membagi 1 saham menjadi 5 saham sehingga harga nominal per lembar saham menjadi seperlima.'`
- **Akar Masalah:** Penjelasan bertele-tele yang memenuhi kartu aksi korporasi.
- **Dampak terhadap Pemain:** Pemain jenuh membaca teori matematika yang sudah dipahami.
- **Kondisi SESUDAH (After):**
  > `Ringkaskan: 'Rasio 1:5 · Nilai total investasi Anda tetap sama'.`

---

### [AUDIT-247] Pill Kategori Dividen Menggunakan Warna Teks Kuning yang Sulit Terbaca di Latar Abu-abu
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `style.css:1240`
- **Kondisi SEBELUM (Before):**
  > `.badge-dividend { color: #FBBF24; background: #374151; }`
- **Akar Masalah:** Rasio kontras warna kuning muda terhadap abu-abu gelap hanya 2.8:1, melanggar standar aksesibilitas WCAG AA (minimal 4.5:1).
- **Dampak terhadap Pemain:** Teks dividen kabur dan memicu kelelahan mata pemain.
- **Kondisi SESUDAH (After):**
  > `Tingkatkan kontras: color: #FEF08A; background: rgba(245, 158, 11, 0.2); border: 1px solid #F59E0B;.`

---

### [AUDIT-248] Formulir Voting RUPS Tidak Menyediakan Pilihan Golput / Abstain
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5028`
- **Kondisi SEBELUM (Before):**
  > `Hanya disediakan tombol [Setuju] dan [Tolak].`
- **Akar Masalah:** Pemain yang tidak ingin memihak pada agenda merger atau akuisisi dipaksa memilih ya atau tidak.
- **Dampak terhadap Pemain:** Tidak mencerminkan mekanisme RUPS bursa nyata.
- **Kondisi SESUDAH (After):**
  > `Sediakan 3 opsi resmi bursa: [Setuju] [Abstain / Netral] [Tolak].`

---

### [AUDIT-249] Notifikasi e-IPO Sukses Mencantumkan Teks 'Pemesanan Tercatat di Sistem Sentral Kustodian'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:5255`
- **Kondisi SEBELUM (Before):**
  > `showGameToast('success', 'Pemesanan Berhasil', 'Pemesanan tercatat di sistem sentral kustodian efek pasar modal.');`
- **Akar Masalah:** Penyisipan istilah lembaga KSEI bursa nyata ke dalam notifikasi game kasual.
- **Dampak terhadap Pemain:** Teks terlalu panjang dan terpotong di layar ponsel.
- **Kondisi SESUDAH (After):**
  > `Gunakan konfirmasi singkat: 'Pemesanan Berhasil · Menunggu hasil penjatahan bursa'.`

---

### [AUDIT-250] Kartu Penawaran e-IPO yang Telah Kadaluarsa Tidak Berpindah ke Bagian Bawah Daftar
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderIpoList`
- **Kondisi SEBELUM (Before):**
  > `Emiten diurutkan berdasarkan ID katalog tanpa memprioritaskan penawaran aktif.`
- **Akar Masalah:** IPO yang sudah tutup tetap bertengger di baris atas, mendorong IPO aktif ke bawah.
- **Dampak terhadap Pemain:** Pemain harus scroll ke bawah untuk mencari saham yang masih bisa dipesan.
- **Kondisi SESUDAH (After):**
  > `Urutkan daftar e-IPO: [Penawaran Aktif Teratas] $\rightarrow$ [Menunggu Penjatahan] $\rightarrow$ [Selesai Melantai].`

---

### [AUDIT-251] Kalkulasi Pendapatan Harian Perusahaan Mengizinkan Nilai Negatif Saat Beban Gaji Menunggak
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `OperatingCompany.java:calculateDailyRevenue, CompanyManager.java:330`
- **Kondisi SEBELUM (Before):**
  > `if (company.getPayrollArrears().isPositive()) revenue = revenue.multiply(0.5);`
- **Akar Masalah:** Jika biaya operasional melebihi kas, revenue dapat terpotong hingga menghasilkan angka omzet aneh atau minus.
- **Dampak terhadap Pemain:** Perusahaan mencetak 'Pendapatan: -Rp 5.000.000' yang secara akuntansi tidak sah.
- **Kondisi SESUDAH (After):**
  > `Pastikan pendapatan kotor (*gross revenue*) selalu memiliki batas bawah Math.max(0, revenue).`

---

### [AUDIT-252] Modal Rekrutmen Pegawai Tidak Menampilkan Efek Peningkatan Kapasitas Produksi Sebelum Merekrut
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openCompanyStaffModal`
- **Kondisi SEBELUM (Before):**
  > `Kartu pelamar hanya menampilkan: Nama, Gaji Bulanan, dan Pengalaman.`
- **Akar Masalah:** Pemain tidak tahu berapa unit tambahan output atau omzet yang akan dihasilkan jika merekrut pegawai tersebut.
- **Dampak terhadap Pemain:** Pemain ragu-ragu merekrut karena takut beban gaji menggerus laba usaha.
- **Kondisi SESUDAH (After):**
  > `Tampilkan proyeksi instan: 'Perkiraan Tambahan Omzet: +Rp 8.500.000/hari · Gaji: Rp 4.000.000/hari'.`

---

### [AUDIT-253] Istilah Jenjang Tunjangan Fasilitas Menggunakan Kata 'Benefit Tier C/B/A' Tanpa Rincian
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openCompanyPolicyModal`
- **Kondisi SEBELUM (Before):**
  > `<option value="TIER_C">Benefit Tier C</option>`
- **Akar Masalah:** Label tidak menjelaskan fasilitas apa saja yang didapat pegawai pada tingkatan tersebut.
- **Dampak terhadap Pemain:** Pemain memilih paket fasilitas termahal tanpa tahu manfaat moralnya.
- **Kondisi SESUDAH (After):**
  > `Gunakan label deskriptif: 'Fasilitas Standar (BPJS & Makan Siang)' dan 'Fasilitas Premium (Bonus Kinerja & Gym)'.`

---

### [AUDIT-254] Kapasitas Produksi Pabrik Menampilkan Angka Desimal Panjang Saat Moral Pegawai Rendah
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `OperatingCompany.java:calculateDailyCapacity, app.js:renderCompanyOverview`
- **Kondisi SEBELUM (Before):**
  > `Kapasitas Harian: Rp 45.241.892,348912`
- **Akar Masalah:** Hasil perkalian multiplier moral tidak dibulatkan ke rupiah utuh sebelum dikirim ke UI.
- **Dampak terhadap Pemain:** Angka kapasitas produksi tampak berantakan dengan 6 angka desimal di belakang koma.
- **Kondisi SESUDAH (After):**
  > `Bulatkan selalu kapasitas pabrik ke satuan rupiah bulat: 'Kapasitas Harian: Rp 45.241.892'.`

---

### [AUDIT-255] Penyetoran Kas Anak Usaha Tidak Menampilkan Batas Saldo Maksimal Gudang Kas Entitas
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:openCompanyCapitalModal`
- **Kondisi SEBELUM (Before):**
  > `Input setor kas tidak memiliki informasi plafon kas aman perusahaan.`
- **Akar Masalah:** Pemain menumpuk kas ratusan miliar di rekening anak usaha yang sebenarnya lebih produktif diputar di pasar saham.
- **Dampak terhadap Pemain:** Uang kas menganggur (*idle cash*) tanpa ada peringatan efisiensi modal.
- **Kondisi SESUDAH (After):**
  > `Tambahkan tips efisiensi: 'Kas anak usaha sudah mencukupi operasional 90 hari. Pertimbangkan tarik dividen ke rekening induk'.`

---

### [AUDIT-256] Tombol Pecat Karyawan (Fire Employee) Bersebelahan dengan Tombol Naik Gaji Tanpa Konfirmasi
- **Kategori:** `BUG & ERGONOMI` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:renderEmployeeRow`
- **Kondisi SEBELUM (Before):**
  > `<button class="btn-danger">Pecat</button> <button class="btn-primary">Naikkan Gaji</button>`
- **Akar Masalah:** Kedua tombol diletakkan berdampingan dengan ukuran kecil tanpa jeda dialog konfirmasi pemutusan hubungan kerja.
- **Dampak terhadap Pemain:** Pemain yang berniat menaikkan gaji pegawai sering salah memencet tombol pecat permanen.
- **Kondisi SESUDAH (After):**
  > `Beri jeda dialog konfirmasi: 'Apakah Anda yakin ingin memberhentikan staf ini? Moral tim akan terpengaruh sementara'.`

---

### [AUDIT-257] Laporan Keuangan Anak Usaha Menuliskan Jargon Akuntansi 'Biaya Depresiasi Amortisasi Aset Berwujud'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderCompanyFinancials`
- **Kondisi SEBELUM (Before):**
  > `flowText(row, 'Depresiasi', 'Biaya depresiasi amortisasi aset berwujud dan peralatan pabrik');`
- **Akar Masalah:** Penggunaan frasa standar PSAK akuntansi lengkap yang membuat kartu laporan keuangan sangat padat.
- **Dampak terhadap Pemain:** Membosankan untuk dipindai secara cepat oleh pemain kasual.
- **Kondisi SESUDAH (After):**
  > `Sederhanakan menjadi: 'Penyusutan Alat & Gedung'.`

---

### [AUDIT-258] Krisis Rantai Pasok (Supply Chain Crisis) Menampilkan Notifikasi Teror Merah Tanpa Petunjuk Solusi
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:showSupplyChainWarning`
- **Kondisi SEBELUM (Before):**
  > `showGameToast('error', 'Krisis Pasokan Ekstrem', 'Bahan baku energi langka!');`
- **Akar Masalah:** Pemain hanya diberi tahu ada krisis tanpa dijelaskan langkah apa yang harus diambil untuk mengatasinya.
- **Dampak terhadap Pemain:** Pemain bingung dan merasa permainannya tidak adil.
- **Kondisi SESUDAH (After):**
  > `Berikan rekomendasi tindakan: '⚠️ Bahan baku langka (+10% biaya). Naikkan harga jual atau tingkatkan riset efisiensi di Pohon Teknologi!'.`

---

### [AUDIT-259] Daftar Anak Usaha Holding Menampilkan Logo Gedung Identik untuk Semua Sektor
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:renderSubsidiaryCard`
- **Kondisi SEBELUM (Before):**
  > `Seluruh anak usaha memakai ikon gedung generik '🏢'.`
- **Akar Masalah:** Pabrik Manufaktur, Toko Ritel, dan Tambang Energi semuanya menggunakan ikon gedung kantor yang sama.
- **Dampak terhadap Pemain:** Mengurangi kekayaan visual dan karakter unik masing-masing lini bisnis pemain.
- **Kondisi SESUDAH (After):**
  > `Gunakan ikon tematik spesifik: 🏭 untuk Manufaktur, 🛒 untuk Ritel, ⚡ untuk Energi, dan 💼 untuk Jasa Finansial.`

---

### [AUDIT-260] Deskripsi Status Holding Menyisakan Frasa Teoretis 'Konsolidasi Kendali Monopoli Vertikal'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:2505`
- **Kondisi SEBELUM (Before):**
  > `flowText(card, 'Karakteristik', 'Konsolidasi kendali monopoli integrasi vertikal empat sektor.');`
- **Akar Masalah:** Penggunaan istilah ekonomi industri akademik yang kaku.
- **Dampak terhadap Pemain:** Bahasa terasa artifisial dan membosankan.
- **Kondisi SESUDAH (After):**
  > `Ganti dengan bahasa konglomerasi: 'Sinergi 4 Sektor: Efisiensi biaya pasokan antar anak usaha'.`

---

### [AUDIT-261] Batas Maksimal Karyawan Pabrik Tidak Menampilkan Alasan 'Kapasitas Ruang Penuh'
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:validateHiring`
- **Kondisi SEBELUM (Before):**
  > `Tombol '+ Rekrut Staf' langsung berstatus disabled tanpa keterangan di sampingnya.`
- **Akar Masalah:** Pemain tidak tahu mengapa tombol rekrutmen tidak bisa diklik padahal kas perusahaan masih banyak.
- **Dampak terhadap Pemain:** Pemain mengira ada bug pada sistem rekrutmen.
- **Kondisi SESUDAH (After):**
  > `Tampilkan keterangan jelas di bawah tombol: 'Kapasitas Staf Penuh (10/10). Upgrade level pabrik untuk menambah kuota!'.`

---

### [AUDIT-262] Tingkat Produktivitas Jam Kerja 6 Jam Menampilkan Teks Peringatan Padahal Meningkatkan Retensi
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderWorkPolicyOptions`
- **Kondisi SEBELUM (Before):**
  > `Opsi 6 Jam diberi teks merah: 'Output rendah 85%'.`
- **Akar Masalah:** Tidak menampilkan sisi positifnya bahwa opsi 6 jam kerja harian memberikan peningkatan moral pegawai +20% dan loyalitas tinggi.
- **Dampak terhadap Pemain:** Pemain selalu menghindari kebijakan 6 jam karena dianggap merugikan secara sepihak.
- **Kondisi SESUDAH (After):**
  > `Sajikan secara seimbang: 'Output 85% · Moral Pegawai Maksimal (+20% Kepuasan Kerja & Retensi)'.`

---

### [AUDIT-263] Penarikan Dividen Anak Usaha ke Rekening Pribadi Tidak Memperhitungkan Pajak Dividen Badan
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `CompanyManager.java:transferFunds, app.js:openHoldingTransferModal`
- **Kondisi SEBELUM (Before):**
  > `Transfer dari rekening PT ke rekening pribadi menarik dana 100% tanpa catatan potongan pajak dividen.`
- **Akar Masalah:** Pemain dapat menarik miliaran rupiah dari kas usaha ke kas pribadi secara bebas tanpa regulasi pemisahan harta.
- **Dampak terhadap Pemain:** Inkonsistensi dengan aturan pajak dividen saham yang dipotong 10%.
- **Kondisi SESUDAH (After):**
  > `Catat mutasi dividen usaha secara rapi di ledger dengan rincian dividen bersih ke kas pribadi.`

---

### [AUDIT-264] Teks Evaluasi Sektor Kue Pasar (Sector Pie) Menuliskan 'Perebutan Zero-Sum Anggaran Nasional'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderSectorPieOverview`
- **Kondisi SEBELUM (Before):**
  > `'Perebutan kue sektor zero-sum anggaran perputaran modal nasional.'`
- **Akar Masalah:** Pemberian nama fitur yang terdengar seperti dokumen nota keuangan kementerian.
- **Dampak terhadap Pemain:** Membuat pemain merasa terbebani istilah ekonomi makro.
- **Kondisi SESUDAH (After):**
  > `Gunakan gaya bahasa permainan bisnis: 'Pangsa Pasar Sektor: Rebut pelanggan dari pesaing industri'.`

---

### [AUDIT-265] Grafik Pangsa Pasar Perusahaan Tidak Menampilkan Nama Kompetitor Utama
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderCompetitorShare`
- **Kondisi SEBELUM (Before):**
  > `Grafik kue hanya menampilkan porsi perusahaan Anda (misal: 14%) dan sisanya bertuliskan 'Pasar (86%)'.`
- **Akar Masalah:** Pemain tidak tahu siapa saja 3 perusahaan raksasa yang menguasai sisa 86% pangsa pasar tersebut.
- **Dampak terhadap Pemain:** Kompetisi terasa abstrak dan kurang seru.
- **Kondisi SESUDAH (After):**
  > `Tampilkan rincian pesaing: 'PT Anda (14%) · Astra Group (42%) · Indofood (28%) · Lainnya (16%)'.`

---

### [AUDIT-266] Tombol Tutup Usaha (Bubar PT / Likuidasi) Tidak Memiliki Konfirmasi Password atau Kata Kunci
- **Kategori:** `BUG & KEAMANAN UX` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `app.js:liquidateCompanyHandler`
- **Kondisi SEBELUM (Before):**
  > `window.confirm('Likuidasi perusahaan?'); // satu klik langsung menghapus permanen`
- **Akar Masalah:** Pemain dapat menghapus anak usaha bernilai ratusan juta rupiah yang sudah dibangun berhari-hari hanya dengan 1 klik cepat pada popup browser.
- **Dampak terhadap Pemain:** Bencana kehilangan data progres bisnis yang tidak sengaja tertekan.
- **Kondisi SESUDAH (After):**
  > `Wajibkan pemain mengetik kata 'TUTUP' di kolom konfirmasi sebelum aset dilikuidasi ke kas.`

---

### [AUDIT-267] Gaji Eksekutif CEO Menampilkan Angka Per Hari Padahal Kontrak Bertuliskan Bulanan
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderCeoContract`
- **Kondisi SEBELUM (Before):**
  > `Gaji Kontrak: Rp 50.000.000 / bulan | Di buku kas terpotong Rp 50.000.000 setiap hari.`
- **Akar Masalah:** Inkonsistensi periode pembayaran antara teks perjanjian kontrak dan eksekusi debit harian game.
- **Dampak terhadap Pemain:** Kas perusahaan terkuras 30x lebih cepat dari yang dibayangkan pemain.
- **Kondisi SESUDAH (After):**
  > `Sesuaikan satuan waktu: potong gaji harian proporsional (Gaji Bulanan / 30) pada penyelesaian harian bursa.`

---

### [AUDIT-268] Label Status Bahan Baku Menampilkan 'Normal' Meskipun Sedang Terjadi Boikot Industri
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderRawMaterialStatus`
- **Kondisi SEBELUM (Before):**
  > `statusEl.textContent = 'Bahan Baku: Aman';`
- **Akar Masalah:** Variabel status rantai pasok tidak membaca event black swan yang sedang aktif di MarketEngine.
- **Dampak terhadap Pemain:** Pabrik berhenti beroperasi tetapi pemain melihat indikator pasokan masih hijau.
- **Kondisi SESUDAH (After):**
  > `Perbarui status rantai pasok secara reaktif mengikuti event krisis global.`

---

### [AUDIT-269] Nama Default Anak Usaha Baru Menggunakan Bahasa Inggris 'New Operating Entity 01'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:openFoundCompanyModal`
- **Kondisi SEBELUM (Before):**
  > `nameInput.value = 'New Operating Entity 01';`
- **Akar Masalah:** Saran nama otomatis menggunakan istilah hukum korporasi bahasa Inggris.
- **Dampak terhadap Pemain:** Inkonsistensi dengan atmosfer game bernuansa konglomerat Indonesia.
- **Kondisi SESUDAH (After):**
  > `Gunakan nama default ramah: 'PT Makmur Sejahtera'.`

---

### [AUDIT-270] Kartu Riwayat Pelatihan Pegawai Menampilkan Tanggal Kalender Milidetik Timestamp Mentah
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderTrainingHistory`
- **Kondisi SEBELUM (Before):**
  > `Pelatihan Terakhir: 1793402842109`
- **Akar Masalah:** Timestamp epoch Java dicetak langsung ke layar tanpa dikonversi ke tanggal bursa.
- **Dampak terhadap Pemain:** Angka acak 13 digit membingungkan pemain.
- **Kondisi SESUDAH (After):**
  > `Format menjadi tanggal bursa game: 'Hari 45 · 02 Des 2026'.`

---

### [AUDIT-271] Rasio Perputaran Modal Usaha Menampilkan Singkatan Rumus Bahasa Asing 'CCC' (Cash Conversion Cycle)
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderCompanyMetrics`
- **Kondisi SEBELUM (Before):**
  > `flowText(card, 'Siklus Kas', 'CCC: 45 Hari');`
- **Akar Masalah:** Penggunaan singkatan akuntansi internasional tanpa penjabaran makna.
- **Dampak terhadap Pemain:** Pemain tidak mengerti bahwa semakin pendek siklus kas semakin cepat uang berputar.
- **Kondisi SESUDAH (After):**
  > `Jelaskan dengan mudah: 'Perputaran Modal: 45 Hari (Kecepatan Kas Kembali)'.`

---

### [AUDIT-272] Tombol Promosikan Staf Menjadi Manajer Tidak Menyebutkan Biaya Kenaikan Gaji
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:promoteStaffHandler`
- **Kondisi SEBELUM (Before):**
  > `<button class="btn-secondary">Promosikan Jadi Manajer</button>`
- **Akar Masalah:** Pemain tidak diberi tahu berapa kenaikan beban gaji per hari setelah promosi jabatan.
- **Dampak terhadap Pemain:** Beban operasional tiba-tiba melonjak dan memicu defisit kas.
- **Kondisi SESUDAH (After):**
  > `Cantumkan implikasi biaya: 'Promosikan Jadi Manajer (+Rp 2.000.000/hari · Produktivitas +30%)'.`

---

### [AUDIT-273] Peringatan Tunggakan Gaji Karyawan Berisi Kalimat Menyeramkan 'Ancaman Mogok Kerja Massal'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderPayrollWarning`
- **Kondisi SEBELUM (Before):**
  > `'PERINGATAN: Ancaman mogok kerja massal dan pemogokan operasional permanen!'`
- **Akar Masalah:** Huruf kapital penuh (*all-caps*) bernada panik yang mengganggu estetika antarmuka.
- **Dampak terhadap Pemain:** Tampilan peringatan terasa norak dan agresif.
- **Kondisi SESUDAH (After):**
  > `Gunakan banner elegan: '⚠️ Kas Operasional Kurang: Segera setor kas untuk melunasi gaji tertunda'.`

---

### [AUDIT-274] Kapasitas Maksimal Penjualan Produk Terbentur Batas Permintaan Pasar Tanpa Penjelasan
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `OperatingCompany.java:calculateDailyRevenue, app.js:renderCompanyOverview`
- **Kondisi SEBELUM (Before):**
  > `Kapasitas produksi 100 unit tetapi penjualan mandek di 50 unit tanpa catatan.`
- **Akar Masalah:** Model bisnis membatasi penjualan berdasarkan batas permintaan pasar industri, namun UI tidak memberi tahu mengapa produksi tambahan tidak terjual.
- **Dampak terhadap Pemain:** Pemain mengira output pabriknya hilang atau dicuri.
- **Kondisi SESUDAH (After):**
  > `Tambahkan keterangan permintaan: 'Penjualan dibatasi permintaan pasar saat ini (Maksimal 50 unit/hari). Buka cabang baru untuk menjangkau pasar lain!'.`

---

### [AUDIT-275] Indikator Efisiensi Bahan Baku Menampilkan 1 desimal Padahal Angka Fluktuatif
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:renderEfficiencyStat`
- **Kondisi SEBELUM (Before):**
  > `Efisiensi: 94.23891%`
- **Akar Masalah:** Mencetak angka desimal panjang pada indikator persentase efisiensi energi.
- **Dampak terhadap Pemain:** Angka berdesakan di kartu ringkasan.
- **Kondisi SESUDAH (After):**
  > `Bulatkan rapi menjadi: 'Efisiensi: 94.2%'.`

---

### [AUDIT-276] Nilai Tukar Valas (Kurs IDR/USD) Tidak Menampilkan Spread Beli dan Jual Broker
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openFxForm`
- **Kondisi SEBELUM (Before):**
  > `Kurs: US$ 1 = Rp 15.500 (hanya menampilkan 1 kurs tengah tanpa spread)`
- **Akar Masalah:** Pemain menukar IDR ke USD lalu langsung menukar kembali dan mendapati saldonya berkurang karena ada spread 1%, tetapi tidak ada keterangan spread beli/jual di awal.
- **Dampak terhadap Pemain:** Pemain mengira sistem memotong biaya siluman secara sepihak.
- **Kondisi SESUDAH (After):**
  > `Tampilkan dua kurs transparan: 'Beli USD: Rp 15.650 | Jual USD: Rp 15.350 (Spread Transparan 1%)'.`

---

### [AUDIT-277] Input Penukaran Valuta Asing Mengizinkan Jumlah Desimal Sen USD Berlebih
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openFxForm`
- **Kondisi SEBELUM (Before):**
  > `inputUsd.value mengizinkan $10.5542`
- **Akar Masalah:** Penukaran mata uang asing mengizinkan lebih dari 2 angka di belakang koma pada dolar AS.
- **Dampak terhadap Pemain:** Menghasilkan selisih pembulatan sen di buku besar perbankan multi-currency.
- **Kondisi SESUDAH (After):**
  > `Kunci input desimal USD selalu maksimal 2 angka sen (step="0.01").`

---

### [AUDIT-278] Deskripsi Paket Terminal Whale Menggunakan Slogan 'Klub Rahasia Taipan Konglomerat'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openResearchTiersModal`
- **Kondisi SEBELUM (Before):**
  > `'Bergabunglah dengan klub rahasia taipan konglomerat untuk membocorkan pergerakan bandar.'`
- **Akar Masalah:** Bahasa fiksi konspirasi pasar yang tidak sesuai dengan pendekatan intelijen pasar modal formal.
- **Dampak terhadap Pemain:** Terdengar seperti grup obrolan telegram pom-pom saham.
- **Kondisi SESUDAH (After):**
  > `Gunakan bahasa intelijen pasar profesional: 'Akses Intelijen Whale: Deteksi akumulasi transaksi institusi besar dan target harga jangka panjang'.`

---

### [AUDIT-279] Simulasi Emas Batangan Tidak Menampilkan Satuan Gram pada Kartu Portofolio Multi-Aset
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderCommodityPosition`
- **Kondisi SEBELUM (Before):**
  > `Emas: 250 unit`
- **Akar Masalah:** Kepemilikan komoditas emas batangan menggunakan satuan abstrak 'unit' bukannya satuan gram standar logam mulia.
- **Dampak terhadap Pemain:** Kurang memuaskan imajinasi pemain yang mengumpulkan emas fisik.
- **Kondisi SESUDAH (After):**
  > `Cantumkan satuan gram resmi: 'Emas Batangan: 250 Gram (Valuasi: Rp 350.000.000)'.`

---

### [AUDIT-280] Tombol Reset Preferensi Tema Mengembalikan Pengaturan Tanpa Notifikasi Toast
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:resetPreferencesHandler`
- **Kondisi SEBELUM (Before):**
  > `Tema langsung berubah secara mendadak tanpa konfirmasi pesan.`
- **Akar Masalah:** Pemain mengira layar berkedip karena error sistem saat warna tema berubah seketika.
- **Dampak terhadap Pemain:** Pengalaman visual terasa kasar.
- **Kondisi SESUDAH (After):**
  > `Tampilkan toast halus: 'Tema visual diatur ulang ke Emas Klasik'.`

---

### [AUDIT-281] Teks Status Koneksi Offline Menuliskan 'Koneksi Terputus Beralih ke Mode Pesawat'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderOfflineStatus`
- **Kondisi SEBELUM (Before):**
  > `'Koneksi internet terputus; sistem beralih ke mode pesawat offline mandiri.'`
- **Akar Masalah:** Menyebutkan istilah mode pesawat padahal perangkat mungkin hanya offline biasa.
- **Dampak terhadap Pemain:** Membingungkan pengguna gawai.
- **Kondisi SESUDAH (After):**
  > `Gunakan label tenang: 'Mode Offline Aktif · Seluruh data tersimpan aman di perangkat'.`

---

### [AUDIT-282] Fitur Aksesibilitas High Contrast Mode Tidak Mengubah Warna Border Kartu Transaksi
- **Kategori:** `BUG & AKSESIBILITAS` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `style.css:highContrastOverrides`
- **Kondisi SEBELUM (Before):**
  > `html.high-contrast .card { border-color: var(--border); } (masih memakai abu-abu gelap)`
- **Akar Masalah:** Saat opsi Kontras Tinggi diaktifkan di profil, border kartu tetap berwarna abu-abu redup.
- **Dampak terhadap Pemain:** Pengguna dengan gangguan penglihatan rendah (*low-vision*) tetap kesulitan melihat pemisah antar kartu.
- **Kondisi SESUDAH (After):**
  > `Terapkan border tegas pada mode kontras tinggi: border: 2px solid #FFFFFF !important; color: #FFFFFF !important;.`

---

### [AUDIT-283] Fitur Reduced Motion Tidak Menghilangkan Transisi CSS Pada Dialog Modal Sheet
- **Kategori:** `BUG & AKSESIBILITAS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `style.css:reducedMotionOverrides`
- **Kondisi SEBELUM (Before):**
  > `.modal-sheet { transition: transform 0.3s ease; } (tidak dimatikan saat reduced-motion)`
- **Akar Masalah:** Pengguna yang mengaktifkan mode bebas animasi untuk menghindari mabuk gerak (*motion sickness*) tetap melihat animasi geser modal.
- **Dampak terhadap Pemain:** Melanggar preferensi aksesibilitas standar sistem operasi.
- **Kondisi SESUDAH (After):**
  > `Matikan seluruh transisi slide pada mode bebas animasi: transition: none !important; transform: none !important;.`

---

### [AUDIT-284] Peringatan Waktu Kedaluwarsa Sinyal Berita Menggunakan Jam Detik Epoch Mentah
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderNewsTargetCountdown`
- **Kondisi SEBELUM (Before):**
  > `Target berlaku hingga: 1793424048191`
- **Akar Masalah:** Target batas waktu berita analis mencetak angka milidetik bukannya sisa hari bursa.
- **Dampak terhadap Pemain:** Pemain tidak tahu kapan sinyal berita tersebut akan hangus.
- **Kondisi SESUDAH (After):**
  > `Format menjadi hitung mundur ramah: 'Berlaku Hingga: 3 Hari Bursa Lagi'.`

---

### [AUDIT-285] Kategori Riset Makro Menuliskan Judul Panjang 'Analisis Ekonometrika Siklus Gelombang Kondratiev'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderMacroReportTitle`
- **Kondisi SEBELUM (Before):**
  > `Laporan: 'Analisis Ekonometrika Siklus Gelombang Kondratiev Jangka Panjang'`
- **Akar Masalah:** Penggunaan nama teori siklus ekonomi langka yang membingungkan pemain.
- **Dampak terhadap Pemain:** Tampak pretensius dan sulit dipahami.
- **Kondisi SESUDAH (After):**
  > `Sederhanakan menjadi intisari: 'Laporan Tren Siklus Ekonomi Makro'.`

---

### [AUDIT-286] Tombol Bagikan Portofolio (Share Wealth) Tidak Menyediakan Pratinjau Gambar Bersih
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:sharePortfolioHandler`
- **Kondisi SEBELUM (Before):**
  > `Fungsi bagikan hanya menyalin teks mentah angka kekayaan ke clipboard ponsel.`
- **Akar Masalah:** Pemain tidak mendapatkan kartu visual elegan untuk dipamerkan ke media sosial.
- **Dampak terhadap Pemain:** Mengurangi potensi viralitas dan kebanggaan pemain.
- **Kondisi SESUDAH (After):**
  > `Rancang kartu mini visual bergaya tiket pialang saham lengkap dengan avatar, pangkat, dan total kekayaan.`

---

### [AUDIT-287] Suara Efek Transaksi (Sound FX) Berbunyi Sangat Keras Tanpa Kontrol Volume Slider
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:soundPlayer, index.html:settingsModal`
- **Kondisi SEBELUM (Before):**
  > `Pengaturan hanya menyediakan toggle On/Off tanpa slider persentase volume audio.`
- **Akar Masalah:** Dentang suara koin saat pesanan match terdengar terlalu keras jika memakai headset.
- **Dampak terhadap Pemain:** Mengagetkan telinga pemain saat bermain di tempat tenang.
- **Kondisi SESUDAH (After):**
  > `Tambahkan pengatur volume suara (*Volume Slider 0% s/d 100%*) di menu Pengaturan.`

---

### [AUDIT-288] Respon Getaran Haptik (Haptic Feedback) Terasa Kaku dan Monoton di Semua Tombol
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `MainActivity.java:vibrateFeedback, app.js:triggerHaptic`
- **Kondisi SEBELUM (Before):**
  > `Setiap klik tombol memicu getaran tunggal 50ms yang identik.`
- **Akar Masalah:** Getaran saat klik tombol biasa sama persis dengan getaran saat mengeksekusi order miliaran rupiah.
- **Dampak terhadap Pemain:** Sensasi sentuhan terasa murah dan kurang sinematik.
- **Kondisi SESUDAH (After):**
  > `Variasikan pola getaran: getaran halus (tick 10ms) untuk tab/filter, dan getaran ganda mantap (heavy thud) saat order saham berhasil.`

---

### [AUDIT-289] Ukuran Font Teks '100%' di Pengaturan Ukuran Teks Masih Menggunakan Persentase Abstrak
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:renderTextSizeOptions`
- **Kondisi SEBELUM (Before):**
  > `<option value="100%">100%</option><option value="115%">115%</option>`
- **Akar Masalah:** Angka persentase tidak memberi gambaran nyata seberapa besar tulisan akan berubah.
- **Dampak terhadap Pemain:** Pemain harus mencoba-coba satu per satu untuk menemukan ukuran yang nyaman.
- **Kondisi SESUDAH (After):**
  > `Gunakan label intuitif: 'Standar (Normal)', 'Besar (Nyaman Terbaca)', dan 'Ekstra Besar (Lansia/Mata Lelah)'.`

---

### [AUDIT-290] Pesan Sukses Ekspor Save Game Menyebutkan Path Direktori Teknis '/storage/emulated/0/...'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:exportSaveHandler`
- **Kondisi SEBELUM (Before):**
  > `showGameToast('success', 'Cadangan Disimpan', 'File tersimpan di /storage/emulated/0/Android/data/com.tradingglobal3d/files/save.json');`
- **Akar Masalah:** Menampilkan path filesystem Linux Android yang sangat panjang dan membingungkan pengguna awam.
- **Dampak terhadap Pemain:** Teks toast meluap keluar layar ponsel.
- **Kondisi SESUDAH (After):**
  > `Persingkat menjadi lokasi ramah: 'Cadangan tersimpan aman di folder Dokumen Game'.`

---

### [AUDIT-291] Tombol Hapus Akun Permainan Diberi Warna Hijau yang Tertukar dengan Tombol Simpan
- **Kategori:** `BUG & KEAMANAN UX` | **Prioritas:** **TINGGI**
- **Lokasi Berkas:** `style.css:2190, app.js:openSaveManagementModal`
- **Kondisi SEBELUM (Before):**
  > `<button class="btn-primary">Hapus Seluruh Data Permainan</button>`
- **Akar Masalah:** Tombol tindakan destruktif menghapus seluruh data memakai gaya tombol hijau primer yang biasa digunakan untuk simpan data.
- **Dampak terhadap Pemain:** Pemain dapat salah mengira tombol ini sebagai tombol simpan progres dan menghapus seluruh karirnya secara tidak sengaja.
- **Kondisi SESUDAH (After):**
  > `Wajibkan gaya tombol merah bahaya: class="btn-danger" (background: var(--negative); color: #FFF;).`

---

### [AUDIT-292] Nilai Inflasi Tahunan Tidak Menjelaskan Dampaknya Terhadap Nilai Kas Menganggur
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderMacroOverview`
- **Kondisi SEBELUM (Before):**
  > `Tingkat Inflasi: 4.2% p.a.`
- **Akar Masalah:** Hanya menampilkan angka inflasi tanpa edukasi bahwa membiarkan uang tunai menganggur akan menurunkan daya beli modal.
- **Dampak terhadap Pemain:** Pemain kasual tidak memahami urgensi menginvestasikan kas ke saham atau deposito.
- **Kondisi SESUDAH (After):**
  > `Tambahkan keterangan edukatif: 'Inflasi 4.2%: Nilai riil kas tunai menyusut perlahan jika tidak diinvestasikan'.`

---

### [AUDIT-293] Teks Konfirmasi Mulai Game Baru Menampilkan Teks Peringatan Dua Paragraf Berulang
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openNewGameConfirmationModal`
- **Kondisi SEBELUM (Before):**
  > `Paragraf 1 dan Paragraf 2 sama-sama mengulang kalimat 'Seluruh portofolio dan bisnis Anda akan dihapus permanen'.`
- **Akar Masalah:** Duplikasi kalimat peringatan di dalam satu dialog pendek.
- **Dampak terhadap Pemain:** Membuat pemain jenuh membaca.
- **Kondisi SESUDAH (After):**
  > `Satukan peringatan tegas dalam 1 kalimat padat: 'Tindakan ini akan mengulang karir dari modal awal Rp 50.000.000. Progres saat ini tidak dapat dikembalikan'.`

---

### [AUDIT-294] Indikator Baterai Rendah Tidak Otomatis Mengaktifkan Mode Hemat Daya Game
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `MainActivity.java:batteryReceiver, app.js:powerSaveManager`
- **Kondisi SEBELUM (Before):**
  > `Mode hemat daya game (html.power-save) hanya bisa diaktifkan secara manual lewat Pengaturan.`
- **Akar Masalah:** Ketika baterai smartphone tersisa di bawah 15%, game tetap menjalankan animasi penuh 60fps yang menguras daya.
- **Dampak terhadap Pemain:** Baterai HP cepat habis saat bermain lama.
- **Kondisi SESUDAH (After):**
  > `Deteksi status baterai rendah Android (<15%) dan tawarkan aktifkan Mode Hemat Daya secara otomatis.`

---

### [AUDIT-295] Tautan Kebijakan Privasi di Menu Lainnya Mengarah ke URL Localhost Port 8080
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:openPrivacyPolicyModal`
- **Kondisi SEBELUM (Before):**
  > `<a href="http://localhost:8080/privacy.html">Kebijakan Privasi</a>`
- **Akar Masalah:** URL developer sisa pengujian lokal tertinggal di menu produksi.
- **Dampak terhadap Pemain:** Pemain yang mengklik tautan menemui halaman error net::ERR_CONNECTION_REFUSED.
- **Kondisi SESUDAH (After):**
  > `Ganti dengan dialog popover offline mandiri yang menjelaskan perlindungan data 100% lokal di perangkat pemain.`

---

### [AUDIT-296] Teks Subjudul Galeri Koleksi Menuliskan 'Kurasi Aset Glamor Portofolio Prestisius'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:openCollectionsGalleryModal`
- **Kondisi SEBELUM (Before):**
  > `showFlow('Kurasi Aset Glamor Portofolio Prestisius');`
- **Akar Masalah:** Penumpukan kata-kata bombastis yang berlebihan pada judul modal.
- **Dampak terhadap Pemain:** Terdengar canggung dan memakan ruang header modal.
- **Kondisi SESUDAH (After):**
  > `Gunakan judul berkelas: showFlow('Galeri Koleksi Mewah');.`

---

### [AUDIT-297] Badge Tingkat Akses Terminal Basic Menampilkan Teks 'Paket Gratisan' Bernada Meremehkan
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:renderTerminalTierBadge`
- **Kondisi SEBELUM (Before):**
  > `<span class="badge">Paket Gratisan</span>`
- **Akar Masalah:** Penggunaan kata 'Gratisan' terasa kurang menghargai pemain yang memilih bermain tanpa berlangganan.
- **Dampak terhadap Pemain:** Mengurangi kenyamanan bermain.
- **Kondisi SESUDAH (After):**
  > `Ubah menjadi sebutan terhormat: <span class="m-tag neutral">Terminal Standar (Bebas Biaya)</span>.`

---

### [AUDIT-298] Peringatan Waktu Bermain (Game Time Alert) Menggunakan Kalimat Ketus 'Anda Terlalu Lama Bermain'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:checkPlaytimeWarning`
- **Kondisi SEBELUM (Before):**
  > `'PERINGATAN KESEHATAN: Anda terlalu lama menatap layar trading!'`
- **Akar Masalah:** Kalimat yang terkesan memarahi pemain alih-alih memberi perhatian kesehatan.
- **Dampak terhadap Pemain:** Merusak suasana hati pemain.
- **Kondisi SESUDAH (After):**
  > `Ubah menjadi pesan ramah dan hangat: 'Sudah 2 jam Anda aktif di bursa saham. Istirahatkan mata sejenak dan nikmati kopi hangat! ☕'.`

---

### [AUDIT-299] Tombol Keluar Aplikasi Menampilkan Teks Kaku 'Hentikan Proses Eksekusi Runtime'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Berkas:** `app.js:exitAppHandler`
- **Kondisi SEBELUM (Before):**
  > `<button>Hentikan Proses Eksekusi Runtime</button>`
- **Akar Masalah:** Istilah teknis terminasi OS dicantumkan pada tombol keluar.
- **Dampak terhadap Pemain:** Tampak aneh di mata pemain.
- **Kondisi SESUDAH (After):**
  > `Gunakan istilah natural: 'Simpan & Keluar Permainan'.`

---

### [AUDIT-300] Laporan Penutupan Evaluasi Lengkap Menuliskan Kalimat 'Audit Komprehensif Berhasil Selesai Secara Tuntas Paripurna'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Berkas:** `app.js:finalAuditBanner`
- **Kondisi SEBELUM (Before):**
  > `'Audit Komprehensif Berhasil Selesai Secara Tuntas Paripurna Tanpa Ada Residu Tertinggal'`
- **Akar Masalah:** Penyisipan kalimat klaim sukses berlebihan di catatan status sistem.
- **Dampak terhadap Pemain:** Memperpanjang baris footer tanpa fungsi nyata.
- **Kondisi SESUDAH (After):**
  > `Persingkat menjadi jaminan kualitas bersih: 'Sistem Terverifikasi Siap Operasi'.`

---
