# LAPORAN AUDIT LENGKAP 100 TEMUAN GAME SAHAM
## Evaluasi Menyeluruh: Bug Sistem, Kesalahan UI / Layout, dan Pemborosan Kalimat / Teks
**Proyek:** Game Saham 2D Portrait Android Native Java + WebView
**Lokasi Repositori:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`
**Tanggal Audit:** 31 Oktober 2026
**Status Audit:** SELESAI (100 Temuan Nyata Terverifikasi)

---

## I. Ringkasan Eksekutif & Statistik 100 Temuan

Audit ini mencakup seluruh 5 tab navigasi root, 55 modal dialog interaktif, lembar tiket order, antrean order book, formula fundamental ekonomi biner Sintesa 5 Masterpiece, dan sistem penyimpanan cadangan ganda.

### 1. Rekapitulasi Berdasarkan Kategori Masalah
| Kategori Masalah | Jumlah Temuan | Persentase | Fokus Utama |
|---|:---:|:---:|---|
| **BUG & LOGIKA SISTEM** | **15 Temuan** | 15% | Nilai hardcoded 10250 di HTML tiket order, parsing regex naif pemisah ribuan Rupiah, sinkronisasi bunga pinjaman makroekonomi, modal replay historis mati, z-index toast vs modal order, auto-renewal paket gratis, dll. |
| **KESALAHAN UI, LAYOUT & ERGONOMI** | **35 Temuan** | 35% | Judul layar disembunyikan media query mobile portrait, tombol persentase modal 75% vs MAX terlalu rapat, tombol tutup modal 24px, pemotongan PnL ratusan juta, status impas redup samar, emoji campur aduk, overflow chip filter, dll. |
| **PEMBOROSAN UI, REDUNDANSI & JARGON** | **50 Temuan** | 50% | Slogan kosong di atas lipatan layar, pengulangan nominal kas 3x dalam 150px, esai pembuka panjang di 20+ modal, kebocoran istilah tiket developer `(A20)`/`(A21)`, jargon akuntansi kaku `Ledger Dua Sisi`, bar tombol duplikat navigasi bawah, dll. |
| **TOTAL INVESTIGASI** | **100 Temuan** | **100%** | **Seluruh 100 butir memiliki file, baris, dan rekomendasi konkrit yang siap dieksekusi.** |

### 2. Rekapitulasi Berdasarkan Tingkat Prioritas
- **TINGGI (Kritis UX & Integritas Permainan):** 22 Temuan
- **SEDANG (Redundansi, Layout & Clutter Layar):** 62 Temuan
- **RENDAH (Polesan Estetika & Micro-copy):** 16 Temuan

---

## II. Pembagian 100 Temuan Berdasarkan Milestone Area
- **Milestone 1 (Temuan 01 s/d 25):** Global Header Shell, Navigasi Utama, Context Strip, Jam Bursa & Tab Beranda
- **Milestone 2 (Temuan 26 s/d 50):** Tab Pasar, Detail Emiten, Grafik, Antrean Order Book, Tiket Order & Radar Riset
- **Milestone 3 (Temuan 51 s/d 75):** Tab Portofolio, Buku Kas/Mutasi, e-IPO, Dividen, Aksi Korporasi & Reksa Dana/Deposito
- **Milestone 4 (Temuan 76 s/d 100):** Tab Bisnis Operasional, Holding Usaha, Pinjaman Bank, Tab Lainnya, Profil, Misi & Dialog Modal

---

## III. Inventaris Terstruktur Lengkap 100 Temuan

### [AUDIT-01] Aturan Media Query Menyembunyikan Judul Layar di Seluruh HP Portrait
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Kode:** `style.css:2356-2359`
- **Kondisi Eksisting (Before):**
  > `@media (max-width: 599px) { .header-identity { display: none; } }`
- **Akar Masalah:** Aturan media query CSS menyembunyikan kontainer .header-identity pada layar di bawah 599px, sehingga judul layar aktif (Beranda, Pasar, Portofolio, Bisnis, Lainnya) lenyap sama sekali di HP pengguna.
- **Dampak terhadap Pemain:** Pemain di ponsel pintar tidak dapat mengetahui nama layar atau sub-halaman yang sedang dibuka melalui header.
- **Usulan Solusi Konkret (After):** Hapus display:none pada .header-identity; izinkan judul layar (#header-title) tetap tampil dominan dengan font-size:1.05rem dan flex:1.

---

### [AUDIT-02] Teks Eyebrow Generik 'GLOBAL TRADER' Mempersempit Baris Header Shell
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `index.html:16`
- **Kondisi Eksisting (Before):**
  > `<span class="eyebrow">GLOBAL TRADER</span><strong id="header-title">Beranda</strong>`
- **Akar Masalah:** Teks dekoratif statis 'GLOBAL TRADER' memakan ruang horizontal header ponsel di antara tombol profil dan chip kas.
- **Dampak terhadap Pemain:** Mempersempit ruang judul layar dan chip kas pada layar beresolusi 390px-412px, memicu pemotongan teks.
- **Usulan Solusi Konkret (After):** Eliminasi teks 'GLOBAL TRADER'; tampilkan judul layar secara tegas: <strong id="header-title" style="font-size:1.15rem;">Beranda</strong>.

---

### [AUDIT-03] Pengulangan Angka Kas Tersedia 3 Kali Berturut-turut dalam Jarak 150px Vertikal
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **TINGGI**
- **Lokasi Kode:** `index.html:17, 43-44`
- **Kondisi Eksisting (Before):**
  > `Header: Kas Rp 75 Jt | Hero: Kas Total Rp 75 Jt | Hero Sub: Tersedia Rp 75 Jt`
- **Akar Masalah:** Nominal uang tunai yang persis sama diulang 3 kali di area pandang paling atas layar tanpa memberikan wawasan operasional baru.
- **Dampak terhadap Pemain:** Membuang area pandang atas layar portrait dan membingungkan pemain saat mulai memiliki portofolio saham.
- **Usulan Solusi Konkret (After):** Header: Kas Tersedia ringkas | Hero: Total Kekayaan Bersih sebagai angka primer, dengan Kas Tersedia sebagai subteks rapi.

---

### [AUDIT-04] Elemen Tombol Mati pada Context Strip dengan Style Tersembunyi Paksa
- **Kategori:** `BUG` | **Prioritas:** **RENDAH**
- **Lokasi Kode:** `index.html:21`
- **Kondisi Eksisting (Before):**
  > `<button id="context-account-btn" class="context-account" style="display:none !important;">Akun: Pribadi</button>`
- **Akar Masalah:** Tombol pemilih akun yang sudah dinonaktifkan duduk sebagai elemen sampah di DOM dengan inline style display:none !important.
- **Dampak terhadap Pemain:** Menambah beban parsing DOM seluler dan membingungkan penelusuran hierarki fokus aksesibilitas.
- **Usulan Solusi Konkret (After):** Hapus elemen tombol mati ini dari template berkas index.html produksi.

---

### [AUDIT-05] Pita Banner Lingkungan Internal 'QA HOST JAVA' Mendorong Layout ke Bawah
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `index.html:26-28`
- **Kondisi Eksisting (Before):**
  > `<div id="mock-banner" class="mock-banner">QA HOST JAVA · SAVE TERISOLASI · BUKAN ANDROID</div>`
- **Akar Masalah:** Pita banner pengujian setebal ~32px mendorong seluruh konten layar portrait ke bawah pada sesi permainan.
- **Dampak terhadap Pemain:** Merusak proporsi vertikal layar HP dan memotong elemen kartu finansial yang seharusnya berada di atas lipatan layar (above the fold).
- **Usulan Solusi Konkret (After):** Setel display:none !important pada berkas produksi atau sembunyikan secara default saat berjalan di WebView runtime.

---

### [AUDIT-06] Teks Status Jam Bursa Menampilkan Placeholder Kaku 'Memuat sesi'
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `index.html:20, app.js:621`
- **Kondisi Eksisting (Before):**
  > `<span id="session-label">Memuat sesi</span>`
- **Akar Masalah:** Teks placeholder 'Memuat sesi' sering tampak statis beberapa detik sebelum sinkronisasi jembatan jam bursa selesai.
- **Dampak terhadap Pemain:** Memberikan kesan aplikasi lambat atau macet sesaat saat transisi antar layar.
- **Usulan Solusi Konkret (After):** Gunakan label default yang natural seperti 'Sesi Pasar Reguler' sebelum sinkronisasi jam selesai.

---

### [AUDIT-07] Slogan Motivasi Kosong 'Bangun langkahmu.' Membuang Ruang Layar Atas
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **TINGGI**
- **Lokasi Kode:** `index.html:35`
- **Kondisi Eksisting (Before):**
  > `<span class="eyebrow" id="home-date">HARI PERDAGANGAN</span><h1>Bangun langkahmu.</h1>`
- **Akar Masalah:** Slogan motivasi kosong memakan tinggi vertikal ~50px tepat di area paling berharga layar Beranda.
- **Dampak terhadap Pemain:** Kartu finansial utama terdorong ke bawah; tanggal game justru ditaruh di font kecil yang sulit terbaca.
- **Usulan Solusi Konkret (After):** Ganti slogan dengan Hari dan Tanggal Bursa Game yang tegas dan informatif: <h1 id="home-date" style="font-size:1.25rem;">Hari 1 · 24x Otomatis</h1>.

---

### [AUDIT-08] Bar Lingkaran Aksi Cepat Beranda 80% Menduplikasi Tombol Tab Bawah
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **TINGGI**
- **Lokasi Kode:** `index.html:50-72`
- **Kondisi Eksisting (Before):**
  > `Bar Lingkaran: [🛒 Beli Saham] [💼 Portofolio] [🏢 Bisnis] [🎁 Klaim Hibah] [💳 Pinjaman]`
- **Akar Masalah:** Tiga tombol menduplikasi bilah navigasi bawah, satu tombol menduplikasi kartu sponsor tepat di bawahnya.
- **Dampak terhadap Pemain:** Memboroskan 90px tinggi layar portrait untuk deretan tombol yang memiliki fungsi identik.
- **Usulan Solusi Konkret (After):** Eliminasi bar lingkaran duplikat ini sepenuhnya; hemat 90px ruang vertikal agar konten pasar langsung terlihat.

---

### [AUDIT-09] Pengulangan Tiga Kali Angka Nominal pada Kartu Sponsor Ventura Beranda
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `index.html:74-84`
- **Kondisi Eksisting (Before):**
  > `Pill: +Rp 25.000.000 | Teks: '...ekspansi portofolio.' | Tombol: 'Klaim Hibah Rp 25 Juta'`
- **Akar Masalah:** Nominal Rp 25 Juta disebut 3 kali dalam kartu setebal 160px yang disertai esai panjang di Beranda.
- **Dampak terhadap Pemain:** Memakan area vertikal secara berlebihan dengan pengulangan kata yang tidak perlu.
- **Usulan Solusi Konkret (After):** Rampingkan menjadi kartu aksi satu baris (tinggi ~50px): 'Dana Hibah Ventura | +Rp 25.000.000 (1x/hari) | [Klaim]'.

---

### [AUDIT-10] Akses Misi & Pencapaian Ganda (Kartu Karier vs Kotak Shortcut Beranda)
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `index.html:85-97`
- **Kondisi Eksisting (Before):**
  > `Kartu Karier: 'Lihat misi →' | Quick Grid: 'Misi Periodik 📋' & 'Pencapaian ◇'`
- **Akar Masalah:** Pemain disajikan dua pintu masuk terpisah untuk alur misi yang sama di layar Beranda.
- **Dampak terhadap Pemain:** Membingungkan pemain seolah-olah terdapat dua sistem misi yang berbeda dalam game.
- **Usulan Solusi Konkret (After):** Satukan kontrol misi di dalam kartu karier; hapus tombol duplikat dari kisi pintasan.

---

### [AUDIT-11] Tumpukan 5 Baris 'Quick Grid' (10 Shortcut) Menenggelamkan Warta Pasar
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Kode:** `index.html:92-132`
- **Kondisi Eksisting (Before):**
  > `10 Tombol: Laporan, Pencapaian, Misi, Liga, Duel Bos, Galeri, DNA, Streak, Dominasi, Asuransi`
- **Akar Masalah:** Menciptakan tembok tautan vertikal yang mendorong modul warta pasar dan sentimen harian hingga 400px ke bawah.
- **Dampak terhadap Pemain:** Pemain tidak melihat sentimen berita hari ini saat membuka game karena terhalang 10 kotak tombol.
- **Usulan Solusi Konkret (After):** Pindahkan fitur sekunder ke Tab Lainnya; biarkan Beranda fokus pada Laporan Sesi, Liga, dan Pantauan Pasar.

---

### [AUDIT-12] Tautan Teks 'Panduan & Mekanisme ↗' Memiliki Target Sentuh Terlalu Tipis
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `index.html:141, app.js:1450`
- **Kondisi Eksisting (Before):**
  > `<button id="btn-home-guide" class="text-button">Panduan & Mekanisme ↗</button>`
- **Akar Masalah:** Tautan teks terpencil di bawah pantauan pasar memiliki tinggi target sentuh hanya ~24px dan label yang membingungkan.
- **Dampak terhadap Pemain:** Pemain sering salah sentuh atau mengabaikan panduan karena target sentuh tidak memenuhi standar ergonomi 44px.
- **Usulan Solusi Konkret (After):** Gunakan tombol bergaya chip rapi dengan tinggi minimal 44px atau integrasikan ke dalam menu Pengaturan di Tab Lainnya.

---

### [AUDIT-13] Keterangan Hero Caption 'Aset portofolio, kas, bisnis & koleksi · IDR' Berulang
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Kode:** `index.html:42, app.js:1220`
- **Kondisi Eksisting (Before):**
  > `<span class="hero-caption">Aset portofolio, kas, bisnis & koleksi · IDR</span>`
- **Akar Masalah:** Keterangan teks panjang yang menjelaskan komponen kekayaan bersih diulang terus di bawah kartu total aset.
- **Dampak terhadap Pemain:** Menambah kepadatan visual tanpa menambah informasi angka keuangan baru bagi pemain.
- **Usulan Solusi Konkret (After):** Sederhanakan menjadi label mata uang ringkas: <span class="hero-caption">Konsolidasi Aset Bersih (IDR)</span>.

---

### [AUDIT-14] Label 'Kewajiban / Utang' Menampilkan Teks 'Memuat…' Abu-abu Samar Saat Nihil
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `index.html:47, app.js:1235`
- **Kondisi Eksisting (Before):**
  > `Utang: <strong id="home-liabilities">Memuat…</strong>`
- **Akar Masalah:** Saat pemain tidak memiliki utang sama sekali (Rp 0), status kewajiban menampilkan teks 'Memuat…' berwarna redup.
- **Dampak terhadap Pemain:** Pemain mengira data keuangan aplikasi gagal terhubung ke server atau sedang error.
- **Usulan Solusi Konkret (After):** Tampilkan secara tegas nilai 'Rp 0' dengan warna netral saat tidak ada fasilitas pinjaman aktif.

---

### [AUDIT-15] Header Bagian 'Langkah berikutnya: HARI INI' Menggunakan Frasa Kaku
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `index.html:73`
- **Kondisi Eksisting (Before):**
  > `<div class="section-heading"><h2>Langkah berikutnya: HARI INI</h2></div>`
- **Akar Masalah:** Frasa 'Langkah berikutnya: HARI INI' terdengar seperti to-do list tugas kantor daripada game simulasi pasar modal.
- **Dampak terhadap Pemain:** Mengurangi nuansa profesional dan atmosfer trading floor modern.
- **Usulan Solusi Konkret (After):** Ubah menjadi subjudul kontekstual: <h2>Peluang & Agenda Hari Ini</h2>.

---

### [AUDIT-16] Tombol Filter Berita Beranda 'Semua / Kabar Baik / Kabar Buruk' Terlalu Rapat
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `index.html:135, style.css:1620`
- **Kondisi Eksisting (Before):**
  > `<div class="news-filters"><button>Semua</button><button>Kabar Baik</button><button>Kabar Buruk</button></div>`
- **Akar Masalah:** Baris tombol filter berita berdesakan pada layar HP sempit (360px), memotong teks dan mempersempit kartu berita.
- **Dampak terhadap Pemain:** Pemain sulit menyaring warta pasar penting dan tombol filter rawan salah pencet.
- **Usulan Solusi Konkret (After):** Terapkan overflow-x:auto dengan padding-bottom:4px dan flex-shrink:0 pada masing-masing pill filter.

---

### [AUDIT-17] Teks Metadisclaimer 'Model simulasi game' pada Kartu Indeks IHSG & S&P 500
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `index.html:168, app.js:2840`
- **Kondisi Eksisting (Before):**
  > `<small class="meta-tag">Model simulasi game</small>`
- **Akar Masalah:** Pemberian label disclaimer 'Model simulasi game' pada setiap kartu indeks merusak kesan imersif.
- **Dampak terhadap Pemain:** Menciptakan kesan game murahan padahal mesin pasar digerakkan oleh order book dan matching engine nyata.
- **Usulan Solusi Konkret (After):** Ganti dengan status sesi bursa sesungguhnya seperti 'IDX Composite · Sesi Reguler' dan 'S&P 500 · US Market'.

---

### [AUDIT-18] Modul Top Movers Menampilkan Kalimat Negatif Saat Pasar Bergerak Tenang
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:3120`
- **Kondisi Eksisting (Before):**
  > `topMoversContainer.innerHTML = '<p class="empty">Tidak ada pergerakan harga signifikan hari ini.</p>'`
- **Akar Masalah:** Saat pembukaan sesi atau pasar tenang, modul menampilkan pesan kosong bernada seolah pasar mati atau rusak.
- **Dampak terhadap Pemain:** Membingungkan pemain dan membuat layar Beranda tampak hampa.
- **Usulan Solusi Konkret (After):** Tampilkan daftar 3 emiten dengan volume tertinggi atau spread antrean terpadat sebagai pengganti pesan kosong.

---

### [AUDIT-19] Ketiadaan Nominal Harga Terakhir pada Kartu 3 Saham Penguat & 3 Pelemah
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:3150`
- **Kondisi Eksisting (Before):**
  > `Card Top Mover: [BBCA] [+2.45%] (Tanpa angka nominal harga saham)`
- **Akar Masalah:** Kartu mover hanya menampilkan kode saham dan persentase perubahan tanpa menyebutkan harga saham terkini.
- **Dampak terhadap Pemain:** Pemain tidak tahu apakah emiten tersebut bernilai Rp 500 atau Rp 10.000 tanpa harus membuka detail instrumen satu per satu.
- **Usulan Solusi Konkret (After):** Sertakan nominal harga terakhir di samping persentase: 'BBCA · Rp 10.250 (+2.45%)'.

---

### [AUDIT-20] Badge Makroekonomi (#macro-badge) Terbentur Tombol Cepat di Layar Sempit
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Kode:** `index.html:19-25, style.css:290`
- **Kondisi Eksisting (Before):**
  > `<span id="macro-badge" class="m-tag neutral">Normal (Lv.5)</span> <button id="btn-skip-break">⏩ Lanjut</button>`
- **Akar Masalah:** Pada layar portrait 360px-390px, penambahan badge siklus makro dan tombol ⏩ Lanjut mendesak waktu jam bursa hingga membentur tepi layar.
- **Dampak terhadap Pemain:** Angka jam bursa atau teks badge makro terpotong elipsis pada ponsel kecil.
- **Usulan Solusi Konkret (After):** Beri min-width:0 pada kontainer jam bursa dan jadikan tombol ⏩ Lanjut sebagai ikon ringkas tanpa teks berlebih pada layar sempit.

---

### [AUDIT-21] Avatar Profil di Kiri Atas Header Tidak Memiliki Indikator Level Karier
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **RENDAH**
- **Lokasi Kode:** `index.html:15, style.css:265`
- **Kondisi Eksisting (Before):**
  > `<button id="btn-settings" class="brand-button"><span class="brand-mark"><svg ...>...</svg></span></button>`
- **Akar Masalah:** Tombol avatar profil hanya menampilkan SVG figur generik abu-abu tanpa badge angka level karier pemain saat ini.
- **Dampak terhadap Pemain:** Pemain tidak merasakan kepuasan progres status dan pangkat finansialnya secara instan di layar utama.
- **Usulan Solusi Konkret (After):** Tambahkan badge kecil melayang di sudut avatar yang menunjukkan level pemain saat ini (misal: 'Lv.3').

---

### [AUDIT-22] Tag Status Sesi Tersembunyi Menuliskan Istilah Redundan 'Pasar Internal Game'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Kode:** `index.html:24`
- **Kondisi Eksisting (Before):**
  > `<span id="mode-label" class="sr-only">Pasar Internal Game</span>`
- **Akar Masalah:** Screen-reader text 'Pasar Internal Game' redundan dengan label sesi bursa di sampingnya.
- **Dampak terhadap Pemain:** Membacakan istilah developer yang tidak berguna bagi pengguna teknologi asistif.
- **Usulan Solusi Konkret (After):** Ganti dengan teks deskriptif standar: <span id="mode-label" class="sr-only">Bursa Saham Utama</span>.

---

### [AUDIT-23] Bottom Navigation Bar Tidak Mengunci Status Visual Saat Membuka Modal Penuh
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:250, style.css:2389`
- **Kondisi Eksisting (Before):**
  > `Nav-item tetap menerima klik dan mempertahankan highlight saat flow-page modal aktif.`
- **Akar Masalah:** Saat lembar flow terbuka di atas layar utama, navigasi bawah kadang tampak aktif ganda.
- **Dampak terhadap Pemain:** Pemain bingung apakah tombol bawah akan menutup modal atau membuka tab baru di latar belakang.
- **Usulan Solusi Konkret (After):** Redupkan opacity navigasi bawah (opacity: 0.4; pointer-events: none) saat modal flow-page penuh sedang aktif.

---

### [AUDIT-24] Kebocoran Scroll Latar Belakang (.app-scroll) Saat Modal Detail Terbuka
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Kode:** `app.js:340, style.css:2370`
- **Kondisi Eksisting (Before):**
  > `.app-scroll tetap dapat di-scroll saat .flow-page aktif di beberapa WebView Android.`
- **Akar Masalah:** Sentuhan jari pengguna pada latar modal kadang menggeser kontainer utama .app-scroll di belakang modal.
- **Dampak terhadap Pemain:** Saat modal ditutup, posisi scroll halaman Beranda atau Pasar melompat tidak terduga.
- **Usulan Solusi Konkret (After):** Tambahkan class modal-open pada document.body dengan overflow:hidden dan overscroll-behavior:none saat modal terbuka.

---

### [AUDIT-25] Tombol 'Semua →' pada Pantauan Pasar Beranda Memiliki Label Ambigu
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `index.html:143`
- **Kondisi Eksisting (Before):**
  > `<button id="btn-home-market" class="text-button">Semua <span aria-hidden="true">→</span></button>`
- **Akar Masalah:** Label tombol 'Semua →' tidak menjelaskan apakah akan membuka semua berita atau membuka seluruh katalog saham.
- **Dampak terhadap Pemain:** Membuat interaksi navigasi menjadi tebak-tebakan bagi pemain baru.
- **Usulan Solusi Konkret (After):** Ubah label menjadi lebih eksplisit dan intuitif: 'Buka Pasar ↗' atau 'Daftar Lengkap ↗'.

---

### [AUDIT-26] Formulir Filter Pasar Dropdown 4-Baris Bertumpuk Masih Mengendap di DOM
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `index.html:181-185`
- **Kondisi Eksisting (Before):**
  > `<div style="display:none !important;"><select id="filter-exchange-select">...</select><select id="filter-sector-select">...</select>...</div>`
- **Akar Masalah:** Deretan elemen select lama yang sudah digantikan oleh chip filter modern masih ditinggalkan di DOM dengan inline style tersembunyi.
- **Dampak terhadap Pemain:** Menghambat kebersihan source code dan rawan memicu event listener ganda jika terpanggil di script.
- **Usulan Solusi Konkret (After):** Hapus blok select filter tersembunyi ini sepenuhnya dari berkas index.html.

---

### [AUDIT-27] Eyebrow Kapital 'BURSA & KEPEMILIKAN' Membuang Ruang Layar Pasar
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `index.html:153`
- **Kondisi Eksisting (Before):**
  > `<span class="eyebrow" id="pasar-porto-eyebrow">BURSA & KEPEMILIKAN</span>`
- **Akar Masalah:** Eyebrow kapital statis memakan tinggi vertikal di atas judul layar Pasar & Portofolio.
- **Dampak terhadap Pemain:** Mendorong tombol subtab Pasar dan Portofolio lebih ke bawah pada layar HP portrait.
- **Usulan Solusi Konkret (After):** Hapus teks eyebrow ini; biarkan judul dan subtab pil menjadi navigasi utama yang bersih.

---

### [AUDIT-28] Pill Badge 'Bursa Aktif' di Header Pasar Redundan dengan Status Dot Jam
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Kode:** `index.html:156`
- **Kondisi Eksisting (Before):**
  > `<span id="pasar-porto-status-badge" class="m-tag pos">Bursa Aktif</span>`
- **Akar Masalah:** Tag status bursa aktif diulang kembali tepat di bawah status dot hijau jam bursa yang ada di context strip.
- **Dampak terhadap Pemain:** Redundansi visual ganda pada rentang jarak kurang dari 50px.
- **Usulan Solusi Konkret (After):** Hapus badge status duplikat ini untuk memperlebar area judul subtab.

---

### [AUDIT-29] Label Sektor Kosong atau Strip '-' pada Emiten Tanpa Kategori Terdaftar
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:2910`
- **Kondisi Eksisting (Before):**
  > `<span class="sector-tag">-</span>`
- **Akar Masalah:** Jika emiten tidak memiliki data sektor terdaftar, sistem merender tanda strip '-' atau teks kosong.
- **Dampak terhadap Pemain:** Tampak seperti bug database atau data cacat di mata pemain.
- **Usulan Solusi Konkret (After):** Gunakan kategori default yang aman seperti 'Diversified / Konglomerasi' atau sembunyikan tag jika kosong.

---

### [AUDIT-30] Avatar Inisial 2-Huruf Memboroskan Kolom Tanpa Informasi Tambahan
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Kode:** `app.js:2940, style.css:1410`
- **Kondisi Eksisting (Before):**
  > `<div class="stock-logo-circle">BB</div>`
- **Akar Masalah:** Lingkaran inisial 2 huruf (seperti 'BB' untuk BBCA) memakan lebar 40px tepat di samping ticker 'BBCA'.
- **Dampak terhadap Pemain:** Mengambil ruang horizontal berharga yang seharusnya digunakan untuk nama lengkap perusahaan dan volume lot.
- **Usulan Solusi Konkret (After):** Hapus lingkaran inisial redundan; ratakan teks ticker ke kiri dengan font tebal dan nama emiten di bawahnya.

---

### [AUDIT-31] Bug Regex Naif pada Pelacak Fluktuasi Harga Saham (Pemisah Titik Rupiah)
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Kode:** `app.js:1554`
- **Kondisi Eksisting (Before):**
  > `const newVal = parseFloat(fresh.currentPrice.replace(/[^0-9.-]+/g,"")) || 0;`
- **Akar Masalah:** Regex membuang karakter non-angka tetapi membiarkan titik desimal. Pada format IDR 'Rp 10.500', titik ribuan dianggap desimal sehingga menghasilkan nilai 10.5 bukannya 10500.
- **Dampak terhadap Pemain:** Animasi flash harga naik/turun (price-flash-up / down) sering salah arah saat harga saham melintasi batas ribuan.
- **Usulan Solusi Konkret (After):** Gunakan helper terstandarisasi parseCleanPrice(fresh.currentPriceRaw || fresh.currentPrice) yang aman dari format ribuan IDR.

---

### [AUDIT-32] Teks Penjelas Persentase Saham Menambahkan Akhiran Meta '· perubahan game'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:1565`
- **Kondisi Eksisting (Before):**
  > `pill.textContent = `${isPos ? '+' : ''}${fresh.dailyChangePercent}% · perubahan game`;`
- **Akar Masalah:** String '· perubahan game' ditambahkan di setiap pill fluktuasi harga detail saham.
- **Dampak terhadap Pemain:** Kebocoran istilah developer yang merusak suasana simulasi bursa profesional.
- **Usulan Solusi Konkret (After):** Cukup tampilkan persentase fluktuasi bersih: `${isPos ? '+' : ''}${fresh.dailyChangePercent}% (Hari Ini)`.

---

### [AUDIT-33] Pengulangan Nama Perusahaan, Bursa & Sektor Tiga Kali di Detail Saham
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:4420-4450`
- **Kondisi Eksisting (Before):**
  > `Header Modal: BBCA (IDX) | Sub-header: Bank Central Asia Tbk · IDX · Keuangan | Info Bar: IDX:BBCA · Sektor Keuangan`
- **Akar Masalah:** Data identitas emiten yang sama diulang 3 kali berturut-turut dalam lembar detail instrumen.
- **Dampak terhadap Pemain:** Memakan sepertiga layar awal detail saham dan menunda pemain melihat grafik serta order book.
- **Usulan Solusi Konkret (After):** Satukan identitas emiten dalam satu blok header ringkas: 'BBCA · Bank Central Asia' dengan pill 'IDX · Keuangan'.

---

### [AUDIT-34] Daftar 20 Baris Rasio Akuntansi Mentah Tanpa Pengelompokan Akordion
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Kode:** `app.js:4620-4710`
- **Kondisi Eksisting (Before):**
  > `Daftar teks mentah 20 rasio: ROE, ROA, DER, PBV, PER, DY, EV/EBITDA, Current Ratio, Quick Ratio, dst.`
- **Akar Masalah:** Rasio finansial dicetak memanjang ke bawah tanpa tab atau menu lipat (accordion).
- **Dampak terhadap Pemain:** Pemain kasual kewalahan membaca data mentah dan harus menggeser layar sangat jauh untuk mencapai tombol order.
- **Usulan Solusi Konkret (After):** Bungkus metrik sekunder ke dalam detail akordion lipat ('Rasio Keuangan Lengkap ▾'); tampilkan hanya 4 rasio utama (PER, PBV, ROE, DY) di layar depan.

---

### [AUDIT-35] Tombol Manual 'Perbarui harga & antrean' Redundan dengan Engine Realtime
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:4750`
- **Kondisi Eksisting (Before):**
  > `<button class="btn-secondary">Perbarui harga & antrean</button>`
- **Akar Masalah:** Disediakan tombol refresh manual pada detail saham, padahal WebSocket/jembatan jam game sudah melakukan polling otomatis tiap detik.
- **Dampak terhadap Pemain:** Memboroskan ruang layar dan memberi kesan seolah sistem game tidak otomatis memperbarui data.
- **Usulan Solusi Konkret (After):** Hapus tombol manual ini; ganti dengan indikator denyut sinkronisasi kecil (pulsing dot) di sudut grafik.

---

### [AUDIT-36] Pemberitahuan Suspensi Menggunakan Kalimat Birokrasi Bertele-tele
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:4780`
- **Kondisi Eksisting (Before):**
  > `'Pemberitahuan: Perdagangan instrumen ini sedang di-halt sementara oleh otoritas bursa terkait pemenuhan regulasi keterbukaan informasi...'`
- **Akar Masalah:** Paragraf peringatan pembekuan saham (halt) memuat 3 baris kalimat formal yang berbelit-belit.
- **Dampak terhadap Pemain:** Mengaburkan status utama instrumen dan memakan area order book.
- **Usulan Solusi Konkret (After):** Ringkaskan menjadi banner peringatan tegas: '⛔ Perdagangan Dihentikan Sementara (Suspended oleh Bursa)'.

---

### [AUDIT-37] Penjelasan Satuan '100 saham/unit (1 lot)' Diulang pada Setiap Kartu Emiten
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Kode:** `app.js:2960`
- **Kondisi Eksisting (Before):**
  > `<small class="text-muted">100 saham/unit (1 lot)</small>`
- **Akar Masalah:** Setiap baris saham di pasar mencantumkan keterangan satuan 100 saham.
- **Dampak terhadap Pemain:** Menumpuk teks pada daftar scroll panjang yang memperlambat pemindaian harga secara visual.
- **Usulan Solusi Konkret (After):** Cukup tampilkan harga per lembar dan satuan lot pada tiket order saat transaksi berlangsung.

---

### [AUDIT-38] Kartu Posisi Menampilkan Blok Abu-abu Besar 'Belum memiliki posisi'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:4590`
- **Kondisi Eksisting (Before):**
  > `<div class="card"><h3>Posisi Anda</h3><p>Belum memiliki posisi pada instrumen ini.</p></div>`
- **Akar Masalah:** Di halaman detail saham, sistem membuat kartu tebal 80px hanya untuk mengabarkan bahwa pemain belum punya saham tersebut.
- **Dampak terhadap Pemain:** Memboroskan ruang layar dan menenggelamkan antrean Bid/Ask ke bawah.
- **Usulan Solusi Konkret (After):** Jika pemain belum memiliki posisi, sembunyikan kartu atau tampilkan sebagai teks satu baris ramping: 'Portofolio: 0 lot'.

---

### [AUDIT-39] Label Dampak Sentimen Berita Emiten Terlalu Panjang dan Bertingkat
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:4810`
- **Kondisi Eksisting (Before):**
  > `<span class="news-impact">Perkiraan dampak: naik · +2.0% - 5.0%</span>`
- **Akar Masalah:** Label sentimen warta mencantumkan frasa pengantar panjang 'Perkiraan dampak: naik ·'.
- **Dampak terhadap Pemain:** Teks meluap dan terpotong pada layar beresolusi sempit.
- **Usulan Solusi Konkret (After):** Ganti dengan pill ringkas berwarna hijau: '▲ Target +2.0% s/d +5.0%'.

---

### [AUDIT-40] Teks Bantuan Tiket Order 'Biaya dan perkiraan kas...' Memenuhi Formulir
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `index.html:295`
- **Kondisi Eksisting (Before):**
  > `<p id="order-est-total">Biaya dan perkiraan kas ditampilkan sebelum order dikirim.</p>`
- **Akar Masalah:** Kalimat penjelas statis memakan baris tepat di atas tombol Tinjau Order.
- **Dampak terhadap Pemain:** Mengurangi ruang dan mendorong tombol aksi utama terpotong keyboard virtual ponsel.
- **Usulan Solusi Konkret (After):** Tampilkan angka estimasi total secara dinamis: 'Estimasi Total: Rp 0 (Termasuk Komisi 0.15%)'.

---

### [AUDIT-41] Input Harga Limit Memiliki Nilai Default Hardcoded 10250 di HTML
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Kode:** `index.html:293`
- **Kondisi Eksisting (Before):**
  > `<input id="order-limit-price" class="input-field" type="number" value="10250">`
- **Akar Masalah:** Nilai default '10250' tertulis mati di file HTML. Jika pemain membuka tiket order saham berharga Rp 200 atau US$ 150 sebelum JS mengisi harga, angka 10250 tetap menempel.
- **Dampak terhadap Pemain:** Berisiko fatal jika pemain tergesa-gesa menekan order sehingga order terpasang pada harga yang salah 50x lipat.
- **Usulan Solusi Konkret (After):** Kosongkan atribut value di HTML (value="") dan berikan placeholder="Masukkan harga limit".

---

### [AUDIT-42] Tiket Order Membocorkan ID Teknis Akun '(ACC-PERS-01)' ke Antarmuka
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:7696`
- **Kondisi Eksisting (Before):**
  > `accContext.textContent = `${currentAccount.name} (${currentAccount.id})`;`
- **Akar Masalah:** Pemain disajikan string teknis internal database seperti 'Akun Pribadi (ACC-PERS-01)'.
- **Dampak terhadap Pemain:** Membingungkan pemain dengan istilah id entitas developer yang tidak estetis.
- **Usulan Solusi Konkret (After):** Cukup tampilkan nama akun secara ramah: 'Akun Pribadi' atau 'Akun Firma PT Indojaya'.

---

### [AUDIT-43] Tombol Persentase Lot Modal Terlalu Rapat dan Rawan Salah Sentuh MAX
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Kode:** `index.html:285-290, style.css:1720`
- **Kondisi Eksisting (Before):**
  > `<div id="order-pct-buttons" style="display:flex; gap:8px;"><button>25%</button><button>50%</button><button>75%</button><button>MAX</button></div>`
- **Akar Masalah:** Tombol alokasi modal 75% dan MAX diletakkan bersebelahan tanpa pemisah warna yang tegas.
- **Dampak terhadap Pemain:** Pemain berisiko tinggi salah menekan tombol MAX (menghabiskan seluruh kas portofolio 100%) padahal bermaksud memilih 75%.
- **Usulan Solusi Konkret (After):** Beri tombol MAX warna aksen peringatan (amber/gold border) dan jarak pemisah visual yang lebih aman.

---

### [AUDIT-44] Lembar Review Order Mengulang Label 'Mode: Pasar Internal Game'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:7780`
- **Kondisi Eksisting (Before):**
  > `flowText(panel, 'Mode', 'Pasar Internal Game');`
- **Akar Masalah:** Pada dialog konfirmasi sebelum eksekusi pesanan saham, sistem masih mencantumkan label mode game.
- **Dampak terhadap Pemain:** Menambah panjang dialog konfirmasi dan menunda pemain menekan tombol konfirmasi final.
- **Usulan Solusi Konkret (After):** Hapus baris mode ini; fokuskan review pada: Emiten, Tipe Order, Jumlah Lot, Harga, dan Estimasi Kas.

---

### [AUDIT-45] Keterangan Waktu Berlaku Menampilkan Format Teknis Jam Detik Kaku
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Kode:** `app.js:7810`
- **Kondisi Eksisting (Before):**
  > `flowText(panel, 'Estimasi berlaku hingga', '15:45:00 WIB');`
- **Akar Masalah:** Penyebutan jam detik presisi '15:45:00 WIB' terkesan kaku untuk pesanan harian biasa.
- **Dampak terhadap Pemain:** Membingungkan pemain seolah order akan hangus dalam hitungan detik tertentu.
- **Usulan Solusi Konkret (After):** Gunakan bahasa bursa yang umum dan alami: 'Berlaku Hingga: Akhir Sesi Bursa Hari Ini'.

---

### [AUDIT-46] Sheet Tiket Order Tertimpa Toast Notifikasi Sistem Melayang
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `style.css:2000, 2180`
- **Kondisi Eksisting (Before):**
  > `#order-modal { z-index: 1050; } | #toast-container { z-index: 2000; }`
- **Akar Masalah:** Container toast memiliki z-index 2000 sedangkan modal order 1050. Ketika toast autosave muncul, toast menutupi tombol tutup tiket order.
- **Dampak terhadap Pemain:** Pemain tidak bisa menutup tiket order sampai notifikasi toast hilang dengan sendirinya.
- **Usulan Solusi Konkret (After):** Setel z-index modal dialog (#order-modal dan .flow-page) ke 2500 sehingga berada di atas notifikasi background.

---

### [AUDIT-47] Tombol Tutup Tiket Order '✕' Memiliki Target Sentuh Hanya 24px
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Kode:** `index.html:262`
- **Kondisi Eksisting (Before):**
  > `<button id="btn-close-modal" style="font-size:1.4rem; padding:4px 8px;">✕</button>`
- **Akar Masalah:** Tombol silang untuk membatalkan tiket order terlalu kecil dan mepet ke sudut kanan atas.
- **Dampak terhadap Pemain:** Pemain di layar sentuh ponsel sering salah menyentuh area background daripada tombol silang.
- **Usulan Solusi Konkret (After):** Beri padding dan ukuran minimum 48px × 48px (min-height: 48px; min-width: 48px; display: flex; align-items: center; justify-content: center;).

---

### [AUDIT-48] Judul Subtab Terminal Riset Menyisakan Konsep Dev '(Satu Pintu)'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:5051`
- **Kondisi Eksisting (Before):**
  > `showFlow('Terminal Riset Pasar (Satu Pintu)');`
- **Akar Masalah:** Istilah sisa diskusi arsitektur developer '(Satu Pintu)' lolos ke antarmuka pengguna pada judul modal riset.
- **Dampak terhadap Pemain:** Tampak aneh dan membingungkan pemain mengenai arti kurung tersebut.
- **Usulan Solusi Konkret (After):** Bersihkan judul menjadi: showFlow('Terminal Riset & Warta Pasar');.

---

### [AUDIT-49] Strip Nilai Wajar Belum Memiliki Keterangan Edukatif untuk Pemula
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:4554-4561`
- **Kondisi Eksisting (Before):**
  > `fundStrip hanya menampilkan Nilai Wajar dan Tag Strong Buy tanpa aksi sentuh/penjelasan.`
- **Akar Masalah:** Pemain pemula yang melihat badge 'Strong Buy' atau 'Diskon 18%' tidak tahu dasar formula perhitungannya.
- **Dampak terhadap Pemain:** Pemain merasa angka tersebut seperti tebak-tebakan tanpa dasar valuasi fundamental.
- **Usulan Solusi Konkret (After):** Tambahkan ikon info kecil 'ⓘ' yang ketika diketuk menampilkan popover singkat: 'Berdasarkan rasio EPS emiten dikalikan rata-rata PER sektor industri'.

---

### [AUDIT-50] Bilah Chip Filter Pasar Tidak Mempertahankan Scroll Saat Dipilih
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `index.html:175-180, app.js:2870`
- **Kondisi Eksisting (Before):**
  > `.filter-chips-bar tanpa scrollIntoView saat chip kanan diklik.`
- **Akar Masalah:** Saat memilih chip 'Watchlist ★' di ujung kanan baris pada layar ponsel 360px, bilah filter melompat kembali ke kiri.
- **Dampak terhadap Pemain:** Mengganggu kenyamanan navigasi dan membingungkan orientasi visual pemain.
- **Usulan Solusi Konkret (After):** Tambahkan event click handler yang memanggil chip.scrollIntoView({ behavior: 'smooth', inline: 'center', block: 'nearest' });.

---

### [AUDIT-51] Kebocoran Kode Tiket Developer '(A21)' dan '(A20)' pada Header Portofolio
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **TINGGI**
- **Lokasi Kode:** `app.js:1894, 1910`
- **Kondisi Eksisting (Before):**
  > `<h3>Portofolio Gabungan (A21)</h3> <p>Pemisahan dana transaksi aktif (A20)</p>`
- **Akar Masalah:** Kode penomoran sprint pengembang (A21 dan A20) bocor ke teks UI judul dan paragraf portofolio.
- **Dampak terhadap Pemain:** Mengurangi kredibilitas visual game dan membingungkan pemain dengan kode asing.
- **Usulan Solusi Konkret (After):** Hapus kode tiket sepenuhnya: 'Portofolio Gabungan' dan 'Pemisahan dana kas operasional'.

---

### [AUDIT-52] Pengulangan Angka Valuasi Total Kas di dalam Akordion Dompet
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:1930`
- **Kondisi Eksisting (Before):**
  > `Header Dompet: Total Kas Rp 50 Jt | Baris 1: Kas Rekening Rp 50 Jt | Sub-baris: Dana Tersedia Rp 50 Jt`
- **Akar Masalah:** Pemain yang hanya memiliki satu rekening kas disajikan pengulangan nominal saldo 3 kali berturut-turut di dalam akordion.
- **Dampak terhadap Pemain:** Menciptakan kesan berbelit-belit pada struktur laporan kas dompet.
- **Usulan Solusi Konkret (After):** Jika rekening hanya satu dan dana tertahan Rp 0, satukan rincian menjadi satu baris: 'Kas Rekening Utama: Rp 50.000.000'.

---

### [AUDIT-53] Shortcut Pintu Putar ke Tab Bisnis di Dasar Layar Portofolio
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:1960`
- **Kondisi Eksisting (Before):**
  > `<div class="card"><h3>Aset Bisnis & Firma</h3><p>Kelola perusahaan...</p><button>Buka Tab Bisnis →</button></div>`
- **Akar Masalah:** Menempatkan kartu promosi pintu putar ke Tab Bisnis di dalam layar Portofolio saham.
- **Dampak terhadap Pemain:** Menduplikasi navigasi tab bawah dan membingungkan batas tanggung jawab layar Portofolio.
- **Usulan Solusi Konkret (After):** Hapus kartu pintu putar ini; biarkan pemain berpindah tab melalui bilah navigasi bawah secara konsisten.

---

### [AUDIT-54] Label 'PRIBADI' dan 'DANA TERPISAH' Ditulis Ganda pada Header Portofolio
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Kode:** `app.js:1905`
- **Kondisi Eksisting (Before):**
  > `<span class="badge">PRIBADI</span> <span class="badge">DANA TERPISAH</span>`
- **Akar Masalah:** Dua badge dengan makna yang tumpang tindih dipasang berdampingan di atas ringkasan aset.
- **Dampak terhadap Pemain:** Memperberat tampilan header portofolio tanpa menambah informasi fungsional.
- **Usulan Solusi Konkret (After):** Cukup gunakan satu badge bersih: <span class="m-tag neutral">Rekening Pribadi</span>.

---

### [AUDIT-55] Bar Komposisi Alokasi Aset Tidak Menampilkan Angka Persentase
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `style.css:1540, app.js:1920`
- **Kondisi Eksisting (Before):**
  > `<div class="allocation-bar"><div class="seg-stock" style="width:60%"></div><div class="seg-cash" style="width:40%"></div></div>`
- **Akar Masalah:** Bilah grafik alokasi aset hanya berupa garis warna-warni tanpa label teks persentase di sampingnya.
- **Dampak terhadap Pemain:** Pemain tidak tahu berapa persisnya bobot saham vs kas tanpa harus menghitung manual.
- **Usulan Solusi Konkret (After):** Tampilkan legenda ringkas di bawah bar: 'Saham: 60% · Kas: 40% · Deposito: 0%'.

---

### [AUDIT-56] Pengulangan Label '(valuasi IDR)' pada Setiap Baris Komposisi Portofolio
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Kode:** `app.js:1945`
- **Kondisi Eksisting (Before):**
  > `Saham (valuasi IDR): Rp 30 Jt | Deposito (valuasi IDR): Rp 0 | Reksa Dana (valuasi IDR): Rp 0`
- **Akar Masalah:** Teks '(valuasi IDR)' diulang pada setiap kategori instrumen di kartu ringkasan.
- **Dampak terhadap Pemain:** Menimbulkan clutter teks pada tampilan vertikal portofolio.
- **Usulan Solusi Konkret (After):** Cantumkan mata uang sekali pada header kartu: 'Komposisi Aset (dalam IDR)'.

---

### [AUDIT-57] Status Posisi 'Impas' Menggunakan Warna Teks Redup Samar (#64748B)
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `style.css:980`
- **Kondisi Eksisting (Before):**
  > `.status-breakeven { color: #64748B; font-weight: normal; }`
- **Akar Masalah:** Ketika harga saham persis sama dengan modal beli (PnL 0.00%), teks status impas berwarna abu-abu sangat gelap.
- **Dampak terhadap Pemain:** Hampir tidak terbaca pada layar HP di bawah sinar matahari atau tema kontras tinggi.
- **Usulan Solusi Konkret (After):** Gunakan warna abu-abu netral terang (#94A3B8) dengan indikator pill ringkas: 'Impas (0.00%)'.

---

### [AUDIT-58] Ketiadaan Teks Bantuan 'Ketuk untuk jual/kelola' pada Posisi Saham
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:1980`
- **Kondisi Eksisting (Before):**
  > `Kartu posisi saham hanya dapat diklik tanpa affordance tanda panah atau keterangan aksi.`
- **Akar Masalah:** Tidak ada indikator visual bahwa kartu posisi saham di portofolio dapat diketuk untuk membuka menu jual atau pasang cut-loss.
- **Dampak terhadap Pemain:** Banyak pemain baru mengira portofolio hanya tabel pasif yang tidak interaktif.
- **Usulan Solusi Konkret (After):** Tambahkan ikon panah navigasi '›' atau chip mikro 'Kelola ↗' di ujung kanan baris saham.

---

### [AUDIT-59] Esai Pembuka Modal e-IPO Menjelaskan Teori Book Building Bertele-tele
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:5210`
- **Kondisi Eksisting (Before):**
  > `flowText(panel, 'Pusat e-IPO', 'Sistem penawaran umum perdana saham baru dengan alokasi penjatahan pasti 60% dan pooling 40% sesuai jadwal...');`
- **Akar Masalah:** Paragraf pengantar e-IPO memuat esai teoritis 150 karakter yang memakan ruang vertikal layar.
- **Dampak terhadap Pemain:** Daftar emiten baru yang sedang menggelar IPO terdorong keluar layar pertama.
- **Usulan Solusi Konkret (After):** Ganti dengan subjudul instruktif padat: 'Pesan saham perdana sebelum tercatat di bursa'.

---

### [AUDIT-60] Tombol Pembatalan e-IPO Belum Menyebutkan Nominal Dana yang Dilepas
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Kode:** `app.js:5280`
- **Kondisi Eksisting (Before):**
  > `button.textContent = 'Batalkan Pemesanan';`
- **Akar Masalah:** Dialog konfirmasi pembatalan pemesanan IPO tidak merinci berapa dana tertahan yang akan langsung cair kembali ke kas pribadi.
- **Dampak terhadap Pemain:** Pemain ragu-ragu membatalkan pesanan karena takut ada biaya penalti atau dana hangus.
- **Usulan Solusi Konkret (After):** Sertakan nominal pemulihan dana pada label: 'Batalkan Pemesanan & Cairkan Kembali Rp 15.000.000'.

---

### [AUDIT-61] Format Rasio Pemecahan Saham (Stock Split) Merender Trailing Spasi Kosong
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:5014`
- **Kondisi Eksisting (Before):**
  > `flowText(row, `${act.type} · ${act.ticker}`, `Nilai/rasio: ${act.valueOrRatio} ${act.currency || ''}`);`
- **Akar Masalah:** Ketika aksi korporasi berupa STOCK_SPLIT, field currency bernilai kosong/null sehingga menghasilkan teks 'Nilai/rasio: 1:5 ' dengan trailing spasi.
- **Dampak terhadap Pemain:** Tampilan teks terlihat canggung dan tidak profesional.
- **Usulan Solusi Konkret (After):** Beri kondisi percabangan: jika tipe STOCK_SPLIT, tampilkan `Rasio Pemecahan: ${act.valueOrRatio}`; jika DIVIDEN, tampilkan `Dividen: ${act.valueOrRatio} ${act.currency}`.

---

### [AUDIT-62] Paragraf Ketentuan Cutoff Reksa Dana Terlalu Panjang dan Kaku
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:5830`
- **Kondisi Eksisting (Before):**
  > `flowText(panel, 'Ketentuan Transaksi', 'Pemesanan unit penyertaan diproses berdasarkan NAB hari bursa bersangkutan jika diselesaikan sebelum batas waktu cutoff 15:30 WIB...');`
- **Akar Masalah:** Esai perbankan kaku 3 baris dipasang di atas pilihan manajer investasi.
- **Dampak terhadap Pemain:** Mengurangi kenyamanan eksplorasi produk reksa dana.
- **Usulan Solusi Konkret (After):** Ringkaskan menjadi poin instruktif: 'Cutoff harian 15:30 WIB · Penetapan NAB di akhir hari bursa'.

---

### [AUDIT-63] Teks Pengantar Asuransi Hedging Menjelaskan Teori Crash Pasar Bertele-tele
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:5741`
- **Kondisi Eksisting (Before):**
  > `flowText(panel, 'Proteksi Hedging', 'Perlindungan 80% ganti rugi saat terjadi crash pasar ekstrem (> 5%).');`
- **Akar Masalah:** Menggunakan penjelasan teoritis di header kartu alih-alih langsung menampilkan status proteksi polis aktif pemain.
- **Dampak terhadap Pemain:** Membuang ruang di atas pilihan opsi durasi polis asuransi.
- **Usulan Solusi Konkret (After):** Fokuskan pada status aktif: 'Status: Belum Terlindungi | Lindungi portofolio dari anomali crash pasar'.

---

### [AUDIT-64] Produk Deposito Belum Menampilkan Perbandingan Bunga Terhadap Siklus Makro
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:5890`
- **Kondisi Eksisting (Before):**
  > `card.innerHTML = `<h3>${prod.name}</h3><p>Bunga: ${(prod.rate * 100).toFixed(2)}% p.a.</p>`;`
- **Akar Masalah:** Tidak menampilkan keterangan bahwa suku bunga deposito berjangka saat ini sedang menguat/melemah akibat posisi Siklus Makroekonomi.
- **Dampak terhadap Pemain:** Pemain tidak menyadari hubungan strategis antara menabung deposito saat masa resesi makro (bunga 9.5%) vs ekspansi (bunga 4.0%).
- **Usulan Solusi Konkret (After):** Tambahkan tag makro: 'Bunga Efektif: 8.50% p.a. (Level Makro: Resesi Moderat)'.

---

### [AUDIT-65] Riwayat Mutasi Kas Mencantumkan Prefix Teknis 'ORD-TRD-' yang Mengganggu
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Kode:** `app.js:6420`
- **Kondisi Eksisting (Before):**
  > `<h3>ORD-TRD-1793402842109 · Pembelian BBCA</h3>`
- **Akar Masalah:** ID unik transaksi database dicantumkan sebagai judul utama riwayat mutasi kas.
- **Dampak terhadap Pemain:** Membuat buku kas akun pemain tampak seperti log debug programmer.
- **Usulan Solusi Konkret (After):** Jadikan judul ramah: 'Beli 10 Lot BBCA', dan taruh ID transaksi sebagai teks kecil abu-abu di sudut bawah.

---

### [AUDIT-66] Notifikasi Gagal Auto-Renewal Terminal Riset Tidak Menyertakan Opsi Tukar Valas
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:5120`
- **Kondisi Eksisting (Before):**
  > `showGameToast('warning', 'Langganan Berakhir', 'Saldo USD tidak cukup untuk perpanjangan otomatis.');`
- **Akar Masalah:** Saat langganan Pro/Whale turun ke Basic karena saldo USD kosong, pemain hanya mendapat toast peringatan tanpa tautan cepat.
- **Dampak terhadap Pemain:** Pemain bingung cara mengisi saldo USD padahal memiliki saldo IDR berlimpah di kas pribadi.
- **Usulan Solusi Konkret (After):** Sertakan tombol aksi cepat pada notifikasi: '[Tukar IDR ke USD Sekarang]'.

---

### [AUDIT-67] Emiten yang Telah Resmi Listing Masih Muncul di Tab Penawaran Aktif e-IPO
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:5235`
- **Kondisi Eksisting (Before):**
  > `if (ipo.status === 'ACTIVE' || ipo.status === 'CLOSED') renderIpoCard(ipo);`
- **Akar Masalah:** Emiten yang sudah menyelesaikan tanggal listing perdana masih ditampilkan di bagian atas tab pemesanan penawaran umum.
- **Dampak terhadap Pemain:** Pemain mencoba memesan saham yang sebenarnya sudah resmi melantai di pasar reguler.
- **Usulan Solusi Konkret (After):** Pisahkan penawaran aktif ke dalam subtab 'Sedang Berlangsung' dan 'Selesai / Sudah Melantai di Bursa'.

---

### [AUDIT-68] Teks Konfirmasi Klaim Dividen Menuliskan Penjelasan Teori Perpajakan Kaku
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:5035`
- **Kondisi Eksisting (Before):**
  > `flowText(panel, 'Pajak Dividen', 'Dividen tunai dipotong pajak final 10% sesuai regulasi perpajakan pasar modal yang berlaku.');`
- **Akar Masalah:** Penjelasan legalitas pajak dividen diulang setiap kali pemain membuka jadwal dividen emiten.
- **Dampak terhadap Pemain:** Membuat pemain jenuh membaca disclaimer yang sama berulang kali.
- **Usulan Solusi Konkret (After):** Rampingkan rincian kas langsung: 'Dividen Bersih: Rp 450.000 (Setelah Pajak Final 10%)'.

---

### [AUDIT-69] Nilai NAB Reksa Dana Menggunakan 4 Desimal Memperkacau Lebar Kolom
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **RENDAH**
- **Lokasi Kode:** `app.js:5850`
- **Kondisi Eksisting (Before):**
  > `NAB: Rp 1.452,8924 / unit`
- **Akar Masalah:** Menampilkan 4 angka desimal pada harga NAB reksa dana di layar portrait ponsel.
- **Dampak terhadap Pemain:** Angka saling menumpuk dan mendesak kolom return persentase ke luar layar.
- **Usulan Solusi Konkret (After):** Bulatkan menjadi 2 desimal standar: 'NAB: Rp 1.452,89 / unit'.

---

### [AUDIT-70] Penggunaan Emoji Campur Aduk pada Ikon Status Mutasi Dividen dan Kupon
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **RENDAH**
- **Lokasi Kode:** `app.js:6450`
- **Kondisi Eksisting (Before):**
  > `span.textContent = item.type === 'DIVIDEND' ? '💰 Dividen' : item.type === 'INTEREST' ? '💵 Bunga' : '📈 Trading';`
- **Akar Masalah:** Pencampuran karakter emoji berwarna-warni membuat antarmuka tabel transaksi kehilangan kesan profesional.
- **Dampak terhadap Pemain:** Tampilan tidak konsisten dengan ikon SVG monokrom pada navigasi utama.
- **Usulan Solusi Konkret (After):** Gunakan badge SVG monokrom atau chip teks elegan: '[DIVIDEN]', '[BUNGA DEPOSITO]', '[TRADING SAHAM]'.

---

### [AUDIT-71] Modal Replay Historis Masih Menampilkan Form Padahal Fitur Telah Dinonaktifkan
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Kode:** `app.js:5702-5735`
- **Kondisi Eksisting (Before):**
  > `function openHistoricalReplayModal() merender form dataset, modal replay, dan tombol 'Mulai replay baru'.`
- **Akar Masalah:** Backend TradingWebBridge.java telah resmi menolak pemanggilan HISTORICAL_REPLAY dengan pesan error dinonaktifkan, namun modal UI masih ada dan tampak fungsional.
- **Dampak terhadap Pemain:** Pemain yang mengisi form dan menekan tombol akan menemui pesan error kegagalan yang membingungkan.
- **Usulan Solusi Konkret (After):** Hapus fungsi openHistoricalReplayModal atau ganti isi dialog dengan pengumuman transparan bahwa mode difokuskan ke Pasar Realtime.

---

### [AUDIT-72] Warna Badge Fluktuasi Negatif pada Aset Kripto/Valas Menggunakan Warna Oranye
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `style.css:1120`
- **Kondisi Eksisting (Before):**
  > `.crypto-negative { background: rgba(245, 158, 11, 0.15); color: #F59E0B; }`
- **Akar Masalah:** Kerugian harga pada kartu multi-aset diberi warna oranye (amber peringatan) alih-alih warna merah standar (--negative / #EF4444).
- **Dampak terhadap Pemain:** Pemain salah mengartikan status rugi sebagai peringatan sistem biasa.
- **Usulan Solusi Konkret (After):** Gunakan warna merah negatif standar konsisten: color: var(--negative); background: var(--negative-bg);

---

### [AUDIT-73] Tombol Tarik Deposito Dini Tidak Memiliki Penegasan Visual Peringatan Penalti
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:5920`
- **Kondisi Eksisting (Before):**
  > `<button class="btn-secondary">Tarik Deposito Sebelum Jatuh Tempo</button>`
- **Akar Masalah:** Tombol penarikan deposito sebelum jatuh tempo menggunakan gaya tombol abu-abu sekunder biasa.
- **Dampak terhadap Pemain:** Pemain tidak menyadari bahwa penarikan dini akan membatalkan seluruh akumulasi bunga berjalan.
- **Usulan Solusi Konkret (After):** Beri gaya tombol peringatan tegas (border: 1px solid var(--negative); color: var(--negative);) disertai dialog konfirmasi penalti bunga.

---

### [AUDIT-74] Label Saldo Kas Menuliskan Kalimat Berbelit-belit Sepanjang 65 Karakter
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:1950`
- **Kondisi Eksisting (Before):**
  > `<p class="text-muted">Saldo Kas Belum Ditempatkan ke Aset Saham Maupun Deposito</p>`
- **Akar Masalah:** Kalimat deskriptif sepanjang 65 karakter digunakan hanya untuk menjelaskan saldo kas menganggur (*idle cash*).
- **Dampak terhadap Pemain:** Memperpanjang tinggi kartu ringkasan kas tanpa menambah kegunaan.
- **Usulan Solusi Konkret (After):** Persingkat menjadi label finansial standar: 'Kas Menganggur (Siap Investasi)'.

---

### [AUDIT-75] Nilai Laba/Rugi Mengambang Ratusan Juta Terpotong Elipsis pada Baris Saham
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Kode:** `style.css:1460, app.js:1995`
- **Kondisi Eksisting (Before):**
  > `.position-pnl { max-width: 90px; text-overflow: ellipsis; overflow: hidden; white-space: nowrap; }`
- **Akar Masalah:** Kontainer PnL dibatasi max-width: 90px. Saat keuntungan pemain mencapai ratusan juta (misal: '+Rp 125.400.000'), teks terpotong menjadi '+Rp 125...'.
- **Dampak terhadap Pemain:** Pemain tidak dapat membaca hasil keuntungan portofolionya secara utuh pada daftar posisi.
- **Usulan Solusi Konkret (After):** Hapus batasan max-width kaku; gunakan flex: 0 0 auto dan font-size: clamp(0.78rem, 2.5vw, 0.9rem) agar nominal pas di layar.

---

### [AUDIT-76] Modal Pinjaman Bank Masih Menampilkan Bunga Hardcoded 0.05% yang Tidak Sinkron
- **Kategori:** `BUG` | **Prioritas:** **TINGGI**
- **Lokasi Kode:** `app.js:5952`
- **Kondisi Eksisting (Before):**
  > `flowText(panel, 'Ketentuan Fasilitas', 'Tenor fleksibel 7, 14, atau 30 hari game dengan bunga harian 0.05%.');`
- **Akar Masalah:** Teks bunga harian 0.05% tertulis mati di app.js. Padahal di TradingFacade.java bunga pinjaman kini dinamis mengikuti Siklus Makroekonomi Level 1..9 (getEffectiveLoanRate()).
- **Dampak terhadap Pemain:** Pemain disajikan informasi bunga pinjaman yang salah dan bertolak belakang dengan beban bunga riil yang didebet ledger.
- **Usulan Solusi Konkret (After):** Ambil suku bunga pinjaman dinamis dari bridge getMacroAndSynthesisState() dan tampilkan angka harian/tahunan yang akurat.

---

### [AUDIT-77] Jargon Akuntansi Kaku 'Ledger Dua Sisi' pada Deskripsi Pembentukan Holding
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:2018`
- **Kondisi Eksisting (Before):**
  > `flowText(panel, 'Struktur Korporasi', 'Konsolidasi entitas anak usaha dengan prinsip ledger dua sisi dan pemisahan liabilitas hukum...');`
- **Akar Masalah:** Penggunaan bahasa buku teks akuntansi yang kaku dan tidak ramah pemain awam pada alur pendirian holding usaha.
- **Dampak terhadap Pemain:** Membuat fitur grup bisnis terasa rumit dan membosankan.
- **Usulan Solusi Konkret (After):** Gunakan bahasa bisnis yang menggugah: 'Satukan anak usaha di bawah satu grup konglomerasi untuk efisiensi modal dan dividen gabungan'.

---

### [AUDIT-78] Paragraf Rumus Matematika Menjejali Layar Modal Konsolidasi Kekayaan
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:5740`
- **Kondisi Eksisting (Before):**
  > `flowText(panel, 'Formula', 'Total kekayaan = Aset pribadi + Nilai ekuitas perusahaan operasional - Total kewajiban utang perbankan.');`
- **Akar Masalah:** Menuliskan persamaan matematika panjang dalam bentuk kalimat paragraf di awal modal dialog.
- **Dampak terhadap Pemain:** Memakan ruang di atas grafik pie konsolidasi kekayaan.
- **Usulan Solusi Konkret (After):** Tampilkan langsung kartu angka ringkas: Aset Pribadi + Ekuitas Bisnis - Total Utang = Total Kekayaan Bersih.

---

### [AUDIT-79] Pesan Kosong Pendirian Usaha Mengulang Syarat Modal Setor Berulang Kali
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:2030`
- **Kondisi Eksisting (Before):**
  > `<p>Belum ada perusahaan. Siapkan modal Rp 100.000.000 untuk mendirikan PT baru sesuai syarat modal minimal Rp 100 Juta.</p>`
- **Akar Masalah:** Nominal syarat Rp 100 Juta diulang dua kali dalam satu kalimat pendek yang bernada kaku.
- **Dampak terhadap Pemain:** Tampilan kosong tampak tidak rapi.
- **Usulan Solusi Konkret (After):** Sederhanakan menjadi panggilan aksi elegan: 'Mulai bangun bisnis pertama Anda (Modal pendirian minimal Rp 100.000.000)'.

---

### [AUDIT-80] Duplikasi Label 'Status operasional' dan Badge 'Operasional Normal' Bersebelahan
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Kode:** `app.js:2045`
- **Kondisi Eksisting (Before):**
  > `<span>Status operasional:</span> <span class="badge pos">Operasional Normal</span>`
- **Akar Masalah:** Kata 'operasional' diulang dua kali dalam jarak 10px pada kartu profil anak usaha.
- **Dampak terhadap Pemain:** Pemborosan teks visual yang canggung dibaca.
- **Usulan Solusi Konkret (After):** Cukup gunakan label bersih: <span>Status:</span> <span class="m-tag pos">Normal / Lancar</span>.

---

### [AUDIT-81] Penulisan 'CEO: Belum tersedia' dengan Teks Merah Memberi Kesan Error
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:2055`
- **Kondisi Eksisting (Before):**
  > `<p class="text-danger">CEO: Belum tersedia</p>`
- **Akar Masalah:** Perusahaan tanpa CEO diberi teks merah peringatan berbahaya, seolah-olah pabrik sedang mogok kerja.
- **Dampak terhadap Pemain:** Pemain panik mengira bisnisnya rusak padahal posisi CEO eksekutif bersifat opsional tambahan.
- **Usulan Solusi Konkret (After):** Gunakan teks netral yang menenangkan: 'CEO: Belum Ditunjuk (Dikelola Pemilik)' dengan tombol kecil '[Rekrut]'.

---

### [AUDIT-82] Esai Pembuka Modal Kebijakan Kerja Perusahaan Menutupi Opsi Jam Kerja
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:6110`
- **Kondisi Eksisting (Before):**
  > `flowText(panel, 'Kebijakan Kerja', 'Atur beban jam kerja harian karyawan antara efisiensi biaya dan retensi moral jangka panjang...');`
- **Akar Masalah:** Paragraf pengantar mengambil 80px ruang vertikal modal dialog kebijakan SDM.
- **Dampak terhadap Pemain:** Pemain harus scroll ke bawah untuk melihat tombol opsi 6 Jam, 8 Jam, dan 10 Jam.
- **Usulan Solusi Konkret (After):** Pangkas menjadi subjudul padat: 'Pilih ritme jam kerja harian karyawan'.

---

### [AUDIT-83] Pengulangan Nama Holding pada Header dan Subjudul Modal Transfer Dana
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Kode:** `app.js:6050`
- **Kondisi Eksisting (Before):**
  > `Header: Transfer Kas Holding Nusantara | Subjudul: Transfer dana kas dari/ke rekening induk Holding Nusantara`
- **Akar Masalah:** Nama badan usaha induk diulang secara kaku di judul dan subjudul berturut-turut.
- **Dampak terhadap Pemain:** Memperpanjang teks modal tanpa menambah kejelasan alur.
- **Usulan Solusi Konkret (After):** Header: 'Transfer Dana Perusahaan' | Subjudul: 'Pindahkan kas antara rekening pribadi dan PT Indojaya'.

---

### [AUDIT-84] Pemberitahuan Gagal Operasi Bisnis Membocorkan Istilah Developer 'Remediasi'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:7640`
- **Kondisi Eksisting (Before):**
  > `flowText(panel, 'Status belum terkonfirmasi', 'Jangan membuat permintaan baru. Pertahankan ID REQ-01; pemulihan status FX masih dalam remediasi.');`
- **Akar Masalah:** Pesan error kegagalan transaksi sistem membocorkan istilah teknis 'remediasi' dan 'ID permintaan'.
- **Dampak terhadap Pemain:** Pemain merasa bingung dan takut progres gamenya rusak.
- **Usulan Solusi Konkret (After):** Gunakan instruksi yang menenangkan: 'Sistem sedang memproses transaksi Anda. Mohon tunggu beberapa detik sebelum mencoba lagi'.

---

### [AUDIT-85] Pill Subtab Layar Bisnis Tidak Menampilkan Jumlah Unit Aktif
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `index.html:218-220`
- **Kondisi Eksisting (Before):**
  > `<button id="firma-subtab-usaha">🏢 Usaha Saya</button><button id="firma-subtab-koleksi">💎 Koleksi</button><button id="firma-subtab-dana">💼 Kelola Dana</button>`
- **Akar Masalah:** Subtab tidak menampilkan angka indikator berapa perusahaan atau aset koleksi yang sudah dimiliki pemain.
- **Dampak terhadap Pemain:** Pemain harus membuka tab satu per satu hanya untuk memeriksa apakah ada bisnis yang aktif.
- **Usulan Solusi Konkret (After):** Tampilkan counter badge kecil: '🏢 Usaha (2)' · '💎 Koleksi (5)' · '💼 Kelola Dana (1)'.

---

### [AUDIT-86] Banner Hero di Tab Lainnya Menduplikasi Kartu Profil Shell Header
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:6250`
- **Kondisi Eksisting (Before):**
  > `<div class="hero-banner"><h2>Profil & Prestise</h2><p>Nama: Investor Muda · Level 3</p></div>`
- **Akar Masalah:** Di bagian atas Tab Lainnya, sistem merender ulang banner profil pemain yang isinya sama persis dengan kartu profil di header.
- **Dampak terhadap Pemain:** Membuang 120px ruang layar vertikal dan menunda menu pengaturan terlihat.
- **Usulan Solusi Konkret (After):** Ganti hero duplikat dengan navigasi grid 2 kolom yang langsung menyajikan menu utama.

---

### [AUDIT-87] Badge Eyebrow Kapital Ganda 'PROFIL & PRESTISE' pada Setiap Kartu Menu
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Kode:** `app.js:6280`
- **Kondisi Eksisting (Before):**
  > `<span class="eyebrow">PROFIL & PRESTISE</span> <h3>Pencapaian</h3>`
- **Akar Masalah:** Kata 'PROFIL & PRESTISE' dicetak kapital di atas setiap tombol kartu menu di Tab Lainnya.
- **Dampak terhadap Pemain:** Menimbulkan polusi visual teks kapital yang berulang-ulang.
- **Usulan Solusi Konkret (After):** Hapus teks eyebrow statis tersebut; cukup tampilkan judul menu dan ikon representatifnya.

---

### [AUDIT-88] Keterangan Subteks Menu Riset Mengulang Kata 'Finansial' Tiga Kali
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:6310`
- **Kondisi Eksisting (Before):**
  > `<h3>Pohon Riset Finansial Tech Tree</h3><p>Riset teknologi finansial untuk membuka efisiensi finansial pasar modal.</p>`
- **Akar Masalah:** Pengulangan kata 'finansial' 3 kali dalam judul dan subjudul kartu berukuran 80px.
- **Dampak terhadap Pemain:** Teks terasa canggung dan redundan.
- **Usulan Solusi Konkret (After):** Rampingkan judul dan deskripsi: 'Pohon Teknologi R&D' | 'Buka analisis canggih dan diskon biaya transaksi bursa'.

---

### [AUDIT-89] Deskripsi Menu Backup Menyajikan Esai SHA-256 yang Terlalu Panjang
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:6330`
- **Kondisi Eksisting (Before):**
  > `flowText(panel, 'Integritas Berkas Save', 'Sistem menggunakan mekanisme penyimpanan ganda (save utama dan backup otomatis) dengan validasi checksum SHA-256...');`
- **Akar Masalah:** Pemain disajikan kuliah singkat mengenai integritas cryptographic hash pada menu simpanan biasa.
- **Dampak terhadap Pemain:** Menghabiskan ruang di atas tombol penting Simpan Sekarang dan Ekspor Cadangan.
- **Usulan Solusi Konkret (After):** Gunakan jaminan keamanan yang ramah: 'Progres tersimpan aman dengan proteksi pencadangan ganda otomatis'.

---

### [AUDIT-90] Deskripsi Menu Mulai Baru Menggunakan Istilah Developer 'Reset Run'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:2078`
- **Kondisi Eksisting (Before):**
  > `<h3>Mulai Permainan Baru (Reset Run)</h3><p>Reset seluruh data sesi dan kembali ke awal.</p>`
- **Akar Masalah:** Penyisipan istilah roguelike gaming / developer '(Reset Run)' ke dalam teks menu game simulasi ekonomi.
- **Dampak terhadap Pemain:** Mengurangi nuansa profesional dan membingungkan pemain bisnis kasual.
- **Usulan Solusi Konkret (After):** Ubah menjadi: 'Mulai Karir Baru' | 'Ulangi perjalanan finansial dari modal awal'.

---

### [AUDIT-91] Tag Status Sesi Game di Hero Banner Tab Lainnya Memakan Ruang Vertikal
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Kode:** `app.js:6265`
- **Kondisi Eksisting (Before):**
  > `<span class="tag">Internal Market · Realtime Engine</span>`
- **Akar Masalah:** Tag teknis internal dipasang mengambang di bawah banner profil tanpa ada fungsi interaktif.
- **Dampak terhadap Pemain:** Menambah tinggi header dan menunda pemain mengakses tombol pengaturan.
- **Usulan Solusi Konkret (After):** Hapus tag teknis ini dari antarmuka visual pemain.

---

### [AUDIT-92] Ikon Menu Hub Menggunakan Karakter Emoji Campur Aduk
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:6295-6340`
- **Kondisi Eksisting (Before):**
  > `Menu: [📋 Misi] [👑 Prestise] [💎 Koleksi] [💾 Backup] [⚙️ Pengaturan] [💀 Reset]`
- **Akar Masalah:** Penggunaan emoji sistem operasi bawaan yang warnanya tidak konsisten dan tidak mengikuti tema elegan navy-gold game.
- **Dampak terhadap Pemain:** Kualitas visual tampak tidak seragam di berbagai merk ponsel (Samsung, Xiaomi, Vivo merender emoji berbeda).
- **Usulan Solusi Konkret (After):** Ganti seluruh emoji dengan set ikon SVG monokrom vektor yang seragam dan elegan.

---

### [AUDIT-93] Teks Dialog Pemulihan Save Game Terlalu Defensif dan Bergaya Catatan Debug
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:6360`
- **Kondisi Eksisting (Before):**
  > `'Sistem mendeteksi inkonsistensi atomic save setelah kegagalan IO sebelumnya. Pulihkan cadangan slot B?'`
- **Akar Masalah:** Pemain disajikan istilah kegagalan I/O dan atomic save ketika aplikasi menawarkan pemulihan cadangan data.
- **Dampak terhadap Pemain:** Pemain mengira HP atau sistemnya rusak secara fatal.
- **Usulan Solusi Konkret (After):** Gunakan dialog penjelas yang tenang: 'Ditemukan berkas cadangan otomatis terbaru. Apakah Anda ingin memulihkan data tersebut?'

---

### [AUDIT-94] Pesan di Modal Arketipe Investor Berisi Kalimat Defensif Programmer
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:6385`
- **Kondisi Eksisting (Before):**
  > `flowText(panel, 'Metode', 'Gaya investasi sedang dievaluasi otomatis tanpa kuis buatan.');`
- **Akar Masalah:** Pernyataan 'tanpa kuis buatan' merupakan argumen desain fitur developer yang tidak seharusnya menjadi teks antarmuka.
- **Dampak terhadap Pemain:** Tampak aneh dan defensif di hadapan pemain.
- **Usulan Solusi Konkret (After):** Ganti dengan deskripsi elegan: 'Gaya trading dianalisis secara cerdas dari riwayat transaksi riil Anda'.

---

### [AUDIT-95] Label Teknis 'Skema V2, UTF-8' Muncul pada Modal Pengaturan Save
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **RENDAH**
- **Lokasi Kode:** `app.js:6345`
- **Kondisi Eksisting (Before):**
  > `<small class="text-muted">Format data: Skema V2, UTF-8</small>`
- **Akar Masalah:** Penyebutan format encoding UTF-8 dan versi skema JSON pada antarmuka save game pemain.
- **Dampak terhadap Pemain:** Detail teknis yang tidak relevan bagi pemain game mobile.
- **Usulan Solusi Konkret (After):** Hapus rincian encoding; cukup tampilkan tanggal dan jam terakhir kali game disimpan.

---

### [AUDIT-96] Toast Autosave 'Progres tersimpan' Muncul Terlalu Sering dan Mengganggu
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **TINGGI**
- **Lokasi Kode:** `app.js:820`
- **Kondisi Eksisting (Before):**
  > `showGameToast('success', 'Tersimpan', 'Progres permainan berhasil disimpan secara atomik.');`
- **Akar Masalah:** Setiap kali timer sinkronisasi jam atau aksi kecil berjalan di latar belakang, toast notifikasi sukses muncul menutupi layar.
- **Dampak terhadap Pemain:** Sangat mengganggu fokus membaca grafik saham dan menutupi header bursa.
- **Usulan Solusi Konkret (After):** Filter notifikasi autosave rutin (Zero-Delta Filtering); simpan secara senyap di background tanpa menampilkan banner popup jika tidak ada error.

---

### [AUDIT-97] Teks Konfirmasi Berulang 'Apakah Anda yakin ingin memulai Era Baru?'
- **Kategori:** `PEMBOROSAN UI & REDUNDANSI TEKS` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:6400`
- **Kondisi Eksisting (Before):**
  > `Judul: Memulai Era Baru? | Paragraf: Apakah Anda yakin ingin memulai Era Baru dan mereset portofolio?`
- **Akar Masalah:** Pertanyaan yang persis sama diulang pada judul modal dan baris pertama deskripsi konfirmasi prestise.
- **Dampak terhadap Pemain:** Redundansi kata yang membuat dialog terasa membosankan.
- **Usulan Solusi Konkret (After):** Judul: 'Konfirmasi Era Baru' | Paragraf: 'Reset perjalanan aktif saat ini untuk mengklaim gelar prestise dan bonus multiplier permanen'.

---

### [AUDIT-98] Tombol 'Hentikan perpanjangan otomatis' Muncul pada Paket Basic Gratis
- **Kategori:** `BUG` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:5140`
- **Kondisi Eksisting (Before):**
  > `if (tier.autoRenewal) renderCancelAutoRenewalButton();`
- **Akar Masalah:** Tombol berhenti langganan otomatis sempat muncul pada paket Basic yang bersifat gratis permanen seumur hidup.
- **Dampak terhadap Pemain:** Membingungkan pemain seolah-olah paket gratis pun sedang menyedot biaya langganan diam-diam.
- **Usulan Solusi Konkret (After):** Tampilkan tombol berhenti perpanjangan otomatis hanya jika pemain sedang aktif berlangganan paket PRO atau WHALE.

---

### [AUDIT-99] Modal Laporan Ketidakhadiran Belum Menyertakan Tombol Aksi 'Buka Pasar'
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:6470-6510`
- **Kondisi Eksisting (Before):**
  > `openAbsenceReportModal hanya memiliki tombol [Tutup] di bagian dasar ringkasan.`
- **Akar Masalah:** Setelah pemain membaca laba bisnis yang terkumpul selama pemain offline, pemain hanya disajikan tombol tutup dan kembali ke Beranda.
- **Dampak terhadap Pemain:** Memutus momentum bermain; pemain harus mencari tab Pasar sendiri untuk membelanjakan keuntungan offlinenya.
- **Usulan Solusi Konkret (After):** Tambahkan tombol aksi primer [Lihat Pasar & Investasikan Laba ↗] di samping tombol [Tutup].

---

### [AUDIT-100] Aset Koleksi Mewah yang Terkunci Tidak Menampilkan Estimasi Harga & Syarat
- **Kategori:** `KESALAHAN UI / LAYOUT` | **Prioritas:** **SEDANG**
- **Lokasi Kode:** `app.js:6550`
- **Kondisi Eksisting (Before):**
  > `<div class="collection-item locked"><h3>???</h3><p>Aset Terkunci</p></div>`
- **Akar Masalah:** Aset mewah yang belum terbuka hanya menampilkan tanda tanya tanpa informasi harga atau level reputasi yang dibutuhkan.
- **Dampak terhadap Pemain:** Pemain kehilangan tujuan jangka panjang (*aspirational goal*) karena tidak tahu berapa modal yang harus dikumpulkan untuk membeli jet pribadi atau vila mewah.
- **Usulan Solusi Konkret (After):** Tampilkan siluet elegan beserta target jelas: 'Superyacht Samudra · Terbuka pada Kekayaan Rp 50 Miliar (Harga: Rp 25 Miliar)'.

---
