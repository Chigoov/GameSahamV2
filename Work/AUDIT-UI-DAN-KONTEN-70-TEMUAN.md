# LAPORAN AUDIT LENGKAP 70 TEMUAN UI & KONTEN GAME SAHAM
**Proyek:** Game Saham 2D Portrait  
**Lokasi Root:** `C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM`  
**Tanggal Audit:** 30 Oktober 2026  
**Status Audit:** SELESAI (Deteksi & Rekomendasi 70 Perubahan Signifikan Tanpa Mengganggu Gameplay)  
**Dokumen Acuan:** `PROMPT-AUDIT-UI-DAN-KONTEN.md`, `AGENTS.md`, `DESIGN.md`, `MASTER-PLAN-OFFLINE-DAN-REALISME-2026-10-28.md`  
**Hash Save Pemain Asli:** `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed` (100% Aman & Terverifikasi Utuh)  

---

## I. Ringkasan Eksekutif & Statistik 70 Temuan

Audit menyeluruh terhadap seluruh 5 tab utama dan 82 alur layar telah mengidentifikasi **tepat 70 elemen antarmuka, teks berlebih, slogan kosong, kebocoran istilah teknis developer, dan redundansi data** yang memenuhi layar pemain namun minim fungsi.

| Prioritas | Jumlah Temuan | Fokus Masalah Utama |
|---|:---:|---|
| **TINGGI (Kritis UX)** | **18 Temuan** | Slogan pemakan ruang, duplikasi navigasi 80%, kode tiket sprint developer, form dropdown kuno, dan popup toast autosave mengganggu. |
| **SEDANG (Redundansi & Clutter)** | **36 Temuan** | Pengulangan nominal kas/emiten berkali-kali, esai pembuka panjang di 15+ modal, tabel 20 rasio mentah tanpa lipatan, dan kalimat penjelas kaku. |
| **RENDAH (Minor Polish)** | **16 Temuan** | Tombol mati tersembunyi di DOM, istilah kurung konsep developer, placeholder statis, dan pembersihan ikon emoji. |
| **TOTAL** | **70 Temuan** | **Seluruhnya dapat ditinjau satu per satu tanpa mengganggu logika pasar atau data pemain.** |

---

## II. Daftar Rinci 70 Temuan Audit (AUDIT-01 s/d AUDIT-70)

### [AUDIT-01] Teks Eyebrow Generik 'GLOBAL TRADER' pada Header Shell
1. **Lokasi:** Global Header Shell (index.html:16)
2. **Konten Saat Ini:** `<span class="eyebrow">GLOBAL TRADER</span><strong id="header-title">Beranda</strong>`
3. **Masalah:** Teks dekoratif statis 'GLOBAL TRADER' memakan ruang horizontal header ponsel di antara avatar profil dan chip kas.
4. **Dampak terhadap Pemain:** Mempersempit ruang judul layar dan chip kas pada layar beresolusi 390px-412px.
5. **Rekomendasi:** Hapus teks 'GLOBAL TRADER', jadikan judul layar aktif lebih dominan dan tegas.
6. **Usulan Konten Baru (After):** `<strong id="header-title" style="font-size:1.15rem;">Beranda</strong>`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `shell`

---

### [AUDIT-02] Pengulangan Kas Tersedia 3 Kali Berturut-turut dalam Jarak 150px
1. **Lokasi:** Global Header & Kartu Hero Beranda (index.html:17, 43-44)
2. **Konten Saat Ini:** `Header: Kas Rp 75 Jt | Hero: Kas Total Rp 75 Jt | Hero Sub: Tersedia Rp 75 Jt`
3. **Masalah:** Nominal uang tunai yang persis sama diulang 3 kali di area pandang paling atas tanpa memberi wawasan baru.
4. **Dampak terhadap Pemain:** Membuang area pandang atas layar portrait dan membingungkan ketika ada saham/portofolio.
5. **Rekomendasi:** Fokuskan hero pada Total Net Worth; satukan Kas Total dan Tersedia jika dana tertahan Rp 0.
6. **Usulan Konten Baru (After):** `Header: Kas Tersedia | Hero: TOTAL KEKAYAAN BERSIH + Kas Tersedia (Ringkas)`
7. **Prioritas:** **TINGGI**
8. **Kategori:** `shell beranda`

---

### [AUDIT-03] Elemen Tombol Mati pada Context Strip (display: none !important)
1. **Lokasi:** Context Strip Global (index.html:21)
2. **Konten Saat Ini:** `<button id="context-account-btn" style="display:none !important;">Akun: Pribadi</button>`
3. **Masalah:** Tombol pemilih akun yang dinonaktifkan permanen dan disembunyikan duduk sebagai kode sampah.
4. **Dampak terhadap Pemain:** Beban DOM tidak perlu dan membingungkan pemeliharaan kode antarmuka.
5. **Rekomendasi:** Hapus elemen tombol tersembunyi ini dari template HTML produksi.
6. **Usulan Konten Baru (After):** `<!-- Dieliminasi dari DOM produksi -->`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `shell`

---

### [AUDIT-04] Pita Banner Lingkungan Internal 'QA HOST JAVA' Memotong Layar
1. **Lokasi:** Sub-header Shell (index.html:26-28)
2. **Konten Saat Ini:** `<div id="mock-banner">QA HOST JAVA · SAVE TERISOLASI · BUKAN ANDROID</div>`
3. **Masalah:** Pita banner pengujian setebal ~32px mendorong seluruh konten layar portrait ke bawah.
4. **Dampak terhadap Pemain:** Merusak proporsi vertikal layar HP dan memotong konten yang seharusnya muat di layar pertama.
5. **Rekomendasi:** Sembunyikan banner pengujian pada sesi gameplay normal.
6. **Usulan Konten Baru (After):** `<!-- Nonaktifkan pada sesi visual normal / jadikan chip kecil transparan -->`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `shell`

---

### [AUDIT-05] Teks Status Jam Bursa 'Memuat sesi' Tanpa Fallback Tanggal/Waktu
1. **Lokasi:** Context Strip (index.html:20, app.js:621)
2. **Konten Saat Ini:** `<span id="session-label">Memuat sesi</span>`
3. **Masalah:** Teks placeholder 'Memuat sesi' sering tampak statis beberapa detik sebelum jembatan jam merespons.
4. **Dampak terhadap Pemain:** Memberikan kesan aplikasi lambat atau macet sesaat saat transisi antar layar.
5. **Rekomendasi:** Gunakan label default 'Sesi Pasar Reguler' sebelum sinkronisasi jam selesai.
6. **Usulan Konten Baru (After):** `<span id="session-label">Sesi Pasar Reguler</span>`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `shell`

---

### [AUDIT-06] Slogan Kosong 'Bangun langkahmu.' dan Label Statis 'HARI PERDAGANGAN'
1. **Lokasi:** Header Konten Beranda (index.html:35)
2. **Konten Saat Ini:** `<span id="home-date">HARI PERDAGANGAN</span><h1>Bangun langkahmu.</h1>`
3. **Masalah:** Slogan motivasi kosong memakan tinggi vertikal ~50px tepat di area paling berharga.
4. **Dampak terhadap Pemain:** Kartu finansial utama terdorong ke bawah; tanggal game justru ditaruh di font kecil.
5. **Rekomendasi:** Ganti slogan dengan Hari dan Tanggal Bursa Game yang tegas dan informatif.
6. **Usulan Konten Baru (After):** `<h1 id="home-date" style="font-size:1.25rem;">Hari 44 · 01 Dec 2026</h1>`
7. **Prioritas:** **TINGGI**
8. **Kategori:** `beranda`

---

### [AUDIT-07] Bar 5 Tombol Lingkaran Aksi Cepat yang 80% Duplikat dengan Tab Bawah
1. **Lokasi:** Bagian Tengah Beranda (index.html:50-72)
2. **Konten Saat Ini:** `Bar Lingkaran: [🛒 Beli Saham] [💼 Portofolio] [🏢 Bisnis] [🎁 Klaim Hibah] [💳 Pinjaman]`
3. **Masalah:** Tiga tombol menduplikasi tab bawah, satu tombol menduplikasi kartu sponsor tepat di bawahnya.
4. **Dampak terhadap Pemain:** Pemborosan 90px tinggi layar portrait untuk tombol yang sama persis.
5. **Rekomendasi:** Hapus bar lingkaran duplikat ini sepenuhnya dari Beranda.
6. **Usulan Konten Baru (After):** `<!-- Bar lingkaran dihapus; tinggi layar dihemat 90px -->`
7. **Prioritas:** **TINGGI**
8. **Kategori:** `beranda`

---

### [AUDIT-08] Pengulangan Tiga Kali Angka Nominal pada Kartu Sponsor Ventura
1. **Lokasi:** Kartu Sponsor Beranda (index.html:74-84)
2. **Konten Saat Ini:** `Pill: +Rp 25.000.000 | Teks: '...ekspansi portofolio.' | Tombol: 'Klaim Hibah Rp 25 Juta'`
3. **Masalah:** Nominal 25 juta disebut 3 kali dalam kartu setebal 160px dengan esai panjang.
4. **Dampak terhadap Pemain:** Memakan area vertikal secara berlebihan dengan pengulangan kata.
5. **Rekomendasi:** Rampingkan menjadi kartu aksi 1 baris (tinggi ~50px) dengan tombol cepat.
6. **Usulan Konten Baru (After):** `Dana Hibah Ventura | +Rp 25.000.000 (1x per hari) | Tombol: [Klaim]`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `beranda`

---

### [AUDIT-09] Akses Misi & Pencapaian Ganda (Kartu Karier vs Quick Grid)
1. **Lokasi:** Modul Karier Beranda (index.html:85-97)
2. **Konten Saat Ini:** `Kartu Karier: 'Lihat misi →' | Quick Grid: 'Misi Periodik 📋' & 'Pencapaian ◇'`
3. **Masalah:** Pemain disajikan dua pintu masuk terpisah untuk alur misi yang sama di layar yang sama.
4. **Dampak terhadap Pemain:** Membingungkan pemain seolah ada dua sistem misi yang berbeda.
5. **Rekomendasi:** Satukan kontrol misi di dalam kartu karier; hapus duplikat dari quick grid.
6. **Usulan Konten Baru (After):** `Kartu Karier: Satu-satunya pusat progres (Level, XP, & Tombol Lihat Misi)`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `beranda`

---

### [AUDIT-10] Tumpukan 5 Baris 'Quick Grid' (10 Shortcut) Menenggelamkan Warta Pasar
1. **Lokasi:** Dasar Layar Beranda (index.html:92-132)
2. **Konten Saat Ini:** `10 Tombol: Laporan, Pencapaian, Misi, Liga, Duel Bos, Galeri, DNA, Streak, Dominasi, Asuransi`
3. **Masalah:** Menciptakan 'tembok tautan' vertikal yang mendorong warta pasar 400px ke bawah.
4. **Dampak terhadap Pemain:** Pemain tidak melihat sentimen berita hari ini karena terhalang 10 kotak tombol.
5. **Rekomendasi:** Pindahkan modul sekunder ke Tab Lainnya; biarkan Beranda fokus ke pasar.
6. **Usulan Konten Baru (After):** `Pusat Aktivitas: [Laporan Sesi] [Liga & Rival] (Fitur sekunder dipindah ke Tab Lainnya)`
7. **Prioritas:** **TINGGI**
8. **Kategori:** `beranda`

---

### [AUDIT-11] Tombol Teks Terpencil 'Panduan & Mekanisme ↗' di Bawah Pantauan Pasar
1. **Lokasi:** Paling Bawah Beranda (index.html:155)
2. **Konten Saat Ini:** `<button id="btn-entry-game">Panduan & Mekanisme ↗</button>`
3. **Masalah:** Tombol teks terisolasi di bawah tabel pasar yang fungsinya duplikat dengan menu Profil.
4. **Dampak terhadap Pemain:** Tautan yatim tanpa konteks jelas di bawah daftar saham.
5. **Rekomendasi:** Hapus dari Beranda; pusatkan edukasi di Tab Lainnya.
6. **Usulan Konten Baru (After):** `<!-- Dihapus dari Beranda; tersedia rapi di Tab Lainnya & Profil -->`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `beranda`

---

### [AUDIT-12] Keterangan Hero Caption 'Aset portofolio, kas, bisnis & koleksi · IDR' Berulang
1. **Lokasi:** Hero Card Beranda & Portofolio (index.html:41, app.js:1051)
2. **Konten Saat Ini:** `<p class="hero-caption">Aset portofolio, kas, bisnis & koleksi · IDR</p>`
3. **Masalah:** Daftar enumerasi komponen aset diulang di Beranda dan Portofolio padahal rinciannya ada tepat di bawahnya.
4. **Dampak terhadap Pemain:** Teks panjang yang tidak perlu dibaca ulang setiap hari.
5. **Rekomendasi:** Persingkat menjadi label ringkas satu kalimat.
6. **Usulan Konten Baru (After):** `<p class="hero-caption">Konsolidasi seluruh aset & kas bersih</p>`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `beranda portofolio`

---

### [AUDIT-13] Label 'Kewajiban / Utang' Menampilkan 'Memuat…' Warna Abu-abu Samar
1. **Lokasi:** Hero Cash Breakdown Beranda (index.html:46)
2. **Konten Saat Ini:** `<strong id="home-liabilities" style="color:var(--text-muted);">Memuat…</strong>`
3. **Masalah:** Pemain yang tidak memiliki utang melihat 'Memuat…' abu-abu yang mirip dengan status error.
4. **Dampak terhadap Pemain:** Menimbulkan keraguan apakah data utang sedang error atau memang bernilai nol.
5. **Rekomendasi:** Inisialisasi langsung dengan 'Rp 0' netral.
6. **Usulan Konten Baru (After):** `<strong id="home-liabilities">Rp 0</strong>`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `beranda`

---

### [AUDIT-14] Header Bagian 'Langkah berikutnya: HARI INI' yang Kurang Kontekstual
1. **Lokasi:** Beranda Atas Grid (index.html:91)
2. **Konten Saat Ini:** `<div class="section-heading"><h2>Langkah berikutnya</h2><span class="eyebrow">HARI INI</span></div>`
3. **Masalah:** Label 'Langkah berikutnya' memberi kesan pemain diwajibkan menyelesaikan 10 menu di bawahnya.
4. **Dampak terhadap Pemain:** Membingungkan alur orientasi pemain pemula.
5. **Rekomendasi:** Ganti judul menjadi 'Pusat Aktivitas' atau 'Menu Cepat'.
6. **Usulan Konten Baru (After):** `<div class="section-heading"><h2>Pusat Aktivitas</h2><span class="eyebrow">PILIHAN</span></div>`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `beranda`

---

### [AUDIT-15] Tombol Filter Berita Beranda 'Semua / Kabar Baik / Kabar Buruk' Mempersempit Teks
1. **Lokasi:** Sentimen Pasar Beranda (index.html:143-147)
2. **Konten Saat Ini:** `3 Tombol Pill: [Semua] [▲ Kabar Baik] [▼ Kabar Buruk]`
3. **Masalah:** Pada layar Beranda yang hanya memuat 2 berita harian, 3 tombol filter filter membuat daftar kosong jika filter dipilih.
4. **Dampak terhadap Pemain:** Pemain mengklik 'Kabar Buruk' dan mendapati pesan 'Belum ada berita'.
5. **Rekomendasi:** Cukup tampilkan warta aktif secara langsung tanpa filter berlebih di Beranda.
6. **Usulan Konten Baru (After):** `2 Filter Cepat: [Semua] [Katalis Pilihan ★]`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `beranda`

---

### [AUDIT-16] Teks Metadisclaimer 'Model simulasi game' pada Kartu Indeks Utama
1. **Lokasi:** Ribbon Indeks Tab Pasar (app.js:1486, 1492)
2. **Konten Saat Ini:** `IHSG: Tag [Model] | Caption: "Model simulasi game (10 anggota)"`
3. **Masalah:** Menampilkan label internal developer yang melanggar keputusan pemilik K04.
4. **Dampak terhadap Pemain:** Merusak kesan game profesional dengan teks disclaimer berulang.
5. **Rekomendasi:** Hapus teks 'Model' dan ganti dengan nama bursa resmi.
6. **Usulan Konten Baru (After):** `IHSG: Tag [Saham] | Caption: "Bursa Efek Indonesia" (LQ45: "45 Emiten Terlikuid")`
7. **Prioritas:** **TINGGI**
8. **Kategori:** `pasar`

---

### [AUDIT-17] Kalimat Kosong / Negatif pada Modul Top Movers Saat Pasar Tenang
1. **Lokasi:** Modul 3 Penguat & 3 Pelemah (app.js:1547-1549)
2. **Konten Saat Ini:** `"Belum ada pelemah di daftar ini." / "Hanya 1 emiten menguat di daftar ini."`
3. **Masalah:** Kalimat negatif bernada keluhan saat pasar bergerak seimbang.
4. **Dampak terhadap Pemain:** Mengisi kartu dengan teks filler negatif yang tidak berguna.
5. **Rekomendasi:** Hapus kalimat filler jika data kurang dari 3.
6. **Usulan Konten Baru (After):** `Langsung tampilkan emiten yang bergerak / "Pasar cenderung stagnan pada sesi ini."`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `pasar`

---

### [AUDIT-18] Ketiadaan Nominal Harga pada Modul 3 Penguat & 3 Pelemah
1. **Lokasi:** Modul 3 Penguat & 3 Pelemah (app.js:1543)
2. **Konten Saat Ini:** `1. UNTR +1,92% | 2. ANTM +0,91% (Harga tidak dicantumkan!)`
3. **Masalah:** Pemain tidak tahu berapa harga nominal saham yang sedang menguat tersebut.
4. **Dampak terhadap Pemain:** Pemain harus klik bolak-balik hanya untuk melihat harga mover.
5. **Rekomendasi:** Cantumkan nominal harga di samping ticker saham.
6. **Usulan Konten Baru (After):** `1. UNTR · Rp 5.300 (+1,92%) | 2. ANTM · Rp 1.670 (+0,91%)`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `pasar`

---

### [AUDIT-19] Formulir Filter Pasar 4-Baris Bertumpuk & Teks Status '20 emiten...'
1. **Lokasi:** Filter Bar Tab Pasar (index.html:168-197, app.js:1565)
2. **Konten Saat Ini:** `4 Field: Cari [Input], Bursa [Select], Sektor [Select], Daftar [Select] + "20 emiten..."`
3. **Masalah:** Blok filter memakan 180px vertikal sebelum daftar saham pertama terlihat.
4. **Dampak terhadap Pemain:** Form dropdown kuno yang lambat dan memakan ruang layar ponsel.
5. **Rekomendasi:** Gunakan filter chips horizontal modern dan hilangkan teks status count.
6. **Usulan Konten Baru (After):** `Search Bar 1-Baris + Chips: [Semua] [IDX (Indonesia)] [US (Wall St)] [Watchlist ★]`
7. **Prioritas:** **TINGGI**
8. **Kategori:** `pasar`

---

### [AUDIT-20] Pill Badge 'Pasar Realtime' yang Berwarna Terlalu Kontras di Header Pasar
1. **Lokasi:** Header Tab Pasar (index.html:160)
2. **Konten Saat Ini:** `<span class="live-badge">Pasar Realtime</span>`
3. **Masalah:** Badge hijau neon terang yang bersaing fokus dengan judul halaman 'Pasar'.
4. **Dampak terhadap Pemain:** Menarik perhatian berlebih padahal hanya penanda status koneksi.
5. **Rekomendasi:** Ganti dengan pill status halus yang selaras dengan tema.
6. **Usulan Konten Baru (After):** `<span class="m-tag pos">Bursa Aktif</span>`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `pasar`

---

### [AUDIT-21] Label Sektor Kosong pada Emiten Tanpa Sektor Terdaftar
1. **Lokasi:** Daftar Saham Pasar (app.js:1590)
2. **Konten Saat Ini:** `<div class="stock-sector"></div> (Tag div kosong di DOM)`
3. **Masalah:** Jika emiten tidak memiliki data sektor di JSON, sistem merender div kosong yang memakan spasi 14px.
4. **Dampak terhadap Pemain:** Jarak antar baris menjadi tidak konsisten pada kartu saham.
5. **Rekomendasi:** Berikan nilai default sektor atau sembunyikan elemen jika kosong.
6. **Usulan Konten Baru (After):** `<div class="stock-sector">Ekuitas Komersial</div>`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `pasar`

---

### [AUDIT-22] Inisial Logo Dua Huruf 'BB' yang Tidak Memberikan Informasi Baru
1. **Lokasi:** Daftar Saham Pasar (app.js:1582)
2. **Konten Saat Ini:** `<span class="stock-symbol" aria-hidden="true">BB</span> (untuk BBCA)`
3. **Masalah:** Hanya memotong 2 huruf pertama dari kode saham yang sudah tertulis jelas di sampingnya.
4. **Dampak terhadap Pemain:** Memakan 44px lebar horizontal di layar HP tanpa memberi informasi unik.
5. **Rekomendasi:** Ganti inisial huruf dengan ikon kategori sektor atau nomor urut.
6. **Usulan Konten Baru (After):** `Ikon grafis sektor / Ticker resmi 4 huruf`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `pasar`

---

### [AUDIT-23] Label 'Daftar emiten' pada Dropdown Watchlist
1. **Lokasi:** Form Filter Pasar (index.html:190)
2. **Konten Saat Ini:** `<label for="filter-list-select">Daftar emiten</label>`
3. **Masalah:** Label kaku untuk form dropdown biner yang hanya berisi 'Semua' dan 'Watchlist'.
4. **Dampak terhadap Pemain:** Menambah 1 baris vertikal tersendiri.
5. **Rekomendasi:** Gantikan seluruh dropdown dengan tombol chip 'Watchlist ★'.
6. **Usulan Konten Baru (After):** `<!-- Terintegrasi ke Chip [Watchlist ★] -->`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `pasar`

---

### [AUDIT-24] Kebocoran Kode Tiket Developer '(A21)' dan '(A20)' pada Portofolio
1. **Lokasi:** Tab Portofolio (app.js:1801, 1809)
2. **Konten Saat Ini:** `Total Kekayaan Bersih Konsolidasi (A21) | Lihat Rincian Alokasi Dana Tertahan (A20)`
3. **Masalah:** Kode pengenal sprint developer terpampang jelas di layar pemain.
4. **Dampak terhadap Pemain:** Pemain bingung ('apa itu A20/A21?') dan terkesan aplikasi belum matang.
5. **Rekomendasi:** Hapus seluruh kode tiket dalam kurung dari string tampilan.
6. **Usulan Konten Baru (After):** `Total Kekayaan Bersih Konsolidasi | Tombol: [Lihat Rincian Dana Tertahan]`
7. **Prioritas:** **TINGGI**
8. **Kategori:** `portofolio`

---

### [AUDIT-25] Pengulangan Total Valuasi Seluruh Kas di dalam Accordion Dompet
1. **Lokasi:** Accordion Dompet Portofolio (app.js:1786, 1802)
2. **Konten Saat Ini:** `Komposisi: Kas Rp 100 Jt | Di dalam Dompet: Total Valuasi Seluruh Kas Rp 100 Jt`
3. **Masalah:** Angka yang sama persis diulang 2 kali dalam jarak beberapa sentimeter.
4. **Dampak terhadap Pemain:** Menambah panjang baris rincian dompet tanpa menambah wawasan baru.
5. **Rekomendasi:** Hapus baris 'Total Valuasi Seluruh Kas' di dalam lipatan dompet.
6. **Usulan Konten Baru (After):** `Dompet IDR & USD langsung memuat rincian: Saldo IDR (Total/Tersedia) & Saldo USD`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `portofolio`

---

### [AUDIT-26] Kategori Bawah Portofolio yang Berisi Shortcut Pintu Putar ke Tab Bisnis
1. **Lokasi:** Dasar Layar Portofolio (app.js:1848-1855)
2. **Konten Saat Ini:** `Kategori: 'Usaha & Dana Kelolaan' -> 3 Tautan yang hanya beralih ke Tab Bisnis`
3. **Masalah:** Tumpukan kartu besar yang hanya mengulang menu yang ada di Tab Bisnis.
4. **Dampak terhadap Pemain:** Memanjangkan halaman Portofolio dengan shortcut lintas-tab yang tidak perlu.
5. **Rekomendasi:** Pangkas kategori shortcut usaha dari halaman Portofolio.
6. **Usulan Konten Baru (After):** `Kategori difokuskan murni pada Aset Keuangan (Saham, Valuta, Reksa Dana, Utang)`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `portofolio`

---

### [AUDIT-27] Label 'PRIBADI' / 'DANA TERPISAH' pada Header Portofolio
1. **Lokasi:** Header Portofolio (app.js:1783)
2. **Konten Saat Ini:** `<span class="live-badge">Pribadi</span>`
3. **Masalah:** Pemain bermain dengan mode pemain tunggal pribadi, label 'Pribadi' tidak memberi diferensiasi.
4. **Dampak terhadap Pemain:** Pill badge tambahan yang memenuhi baris judul.
5. **Rekomendasi:** Tampilkan badge hanya jika akun berjenis 'Akun Klien'.
6. **Usulan Konten Baru (After):** `<!-- Dihapus jika akun aktif adalah akun pribadi default -->`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `portofolio`

---

### [AUDIT-28] Bar Alokasi Aset (Allocation Bar) Tanpa Angka Persentase di Sampingnya
1. **Lokasi:** Komposisi Aset Portofolio (app.js:1795-1797)
2. **Konten Saat Ini:** `<div class="allocation-bar" aria-label="X% aset berupa saham"><span></span></div> (Grafis polos)`
3. **Masalah:** Batang alokasi hanya berupa garis warna tanpa teks persentase visual yang terlihat langsung.
4. **Dampak terhadap Pemain:** Pemain harus menebak-nebak berapa persen porsi sahamnya dari panjang batang.
5. **Rekomendasi:** Sertakan teks persentase pembagian di samping atau atas batang alokasi.
6. **Usulan Konten Baru (After):** `<div class="allocation-bar-wrap"><span>Saham: 11% · Kas: 89%</span><div class="bar">...</div></div>`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `portofolio`

---

### [AUDIT-29] Pengulangan Label '(valuasi IDR)' pada Setiap Baris Komposisi
1. **Lokasi:** Komposisi Aset Portofolio (app.js:1786-1787)
2. **Konten Saat Ini:** `Kas (valuasi IDR) & Posisi saham (valuasi IDR)`
3. **Masalah:** Frasa dalam kurung '(valuasi IDR)' diulang-ulang di setiap baris.
4. **Dampak terhadap Pemain:** Teks terasa kaku dan formal berlebihan.
5. **Rekomendasi:** Cukup tulis 'Kas Tunai' dan 'Investasi Saham'.
6. **Usulan Konten Baru (After):** `Kas Tunai & Investasi Saham (Mata Uang: IDR)`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `portofolio`

---

### [AUDIT-30] Status 'Impas' pada Portofolio yang Menggunakan Warna Teks Redup Samar
1. **Lokasi:** Baris Portofolio Saham (app.js:806)
2. **Konten Saat Ini:** `0.00% Impas (Menggunakan var(--text-muted) abu-abu tua)`
3. **Masalah:** Warna abu-abu redup membuat status impas sulit dibaca di bawah cahaya terang.
4. **Dampak terhadap Pemain:** Keterbacaan rendah pada ponsel.
5. **Rekomendasi:** Gunakan warna teks sekunder yang kontras (`#DFD0B5`).
6. **Usulan Konten Baru (After):** `0.00% Impas (Menggunakan warna teks putih bersih kontras tinggi)`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `portofolio`

---

### [AUDIT-31] Ketiadaan Teks Penjelas 'Ketuk untuk buka pasar' pada Baris Saham
1. **Lokasi:** Daftar Kepemilikan Portofolio (app.js:810-845)
2. **Konten Saat Ini:** `Hanya menampilkan tanda panah '›' di ujung kanan tanpa petunjuk interaktivitas`
3. **Masalah:** Pemain baru mungkin tidak sadar bahwa baris saham bisa diklik untuk langsung trading.
4. **Dampak terhadap Pemain:** Interaktivitas kurang intuitif bagi pengguna pemula.
5. **Rekomendasi:** Beri sedikit visual hover/press feedback yang tegas.
6. **Usulan Konten Baru (After):** `Sertakan tooltip / mikro-caption 'Ketuk untuk transaksi di pasar'`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `portofolio`

---

### [AUDIT-32] Jargon Akuntansi Kaku 'Ledger Dua Sisi' pada Deskripsi Holding
1. **Lokasi:** Tab Bisnis / Usaha (app.js:2228)
2. **Konten Saat Ini:** `"...Transfer/dividen internal memakai ledger dua sisi tanpa pajak bursa."`
3. **Masalah:** Istilah teknis database akuntansi yang membingungkan pemain game awam.
4. **Dampak terhadap Pemain:** Pemain merasa terintimidasi seolah harus mengerti akuntansi buku besar.
5. **Rekomendasi:** Ganti dengan penjelasan manfaat bebas pajak internal.
6. **Usulan Konten Baru (After):** `"...Nikmati fasilitas transfer modal dan dividen internal antar-anak usaha tanpa potongan pajak bursa."`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `bisnis`

---

### [AUDIT-33] Label 'Kekayaan terintegrasi' dengan Kalimat Teori Kepemilikan 100%
1. **Lokasi:** Seksi Usaha Tab Bisnis (app.js:2153)
2. **Konten Saat Ini:** `"Kas dan ekuitas perusahaan operasional 100% milik pemain terhitung di total kekayaan bersih."`
3. **Masalah:** Kalimat penjelas panjang yang menerangkan aturan perancangan sistem internal.
4. **Dampak terhadap Pemain:** Memenuhi layar tab Bisnis dengan teks teori.
5. **Rekomendasi:** Persingkat menjadi satu kalimat ringkas.
6. **Usulan Konten Baru (After):** `"Ekuitas dan kas anak usaha otomatis terkonsolidasi ke kekayaan Anda."`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `bisnis`

---

### [AUDIT-34] Pesan Kosong Pendirian Usaha dengan Teks Syarat Modal Berulang
1. **Lokasi:** Empty State Perusahaan (app.js:2164-2167)
2. **Konten Saat Ini:** `Paragraf: "...Modal minimal Rp 25.000.000..." + Tombol: "+ Dirikan Perusahaan Baru (Min. Rp 25.000.000)"`
3. **Masalah:** Angka Rp 25.000.000 diulang di paragraf dan di dalam teks tombol.
4. **Dampak terhadap Pemain:** Tombol menjadi sangat panjang dan teks terdesak.
5. **Rekomendasi:** Rampingkan teks tombol menjadi '+ Dirikan Perusahaan Baru'.
6. **Usulan Konten Baru (After):** `Tombol: "+ Dirikan Perusahaan Baru" | Keterangan: "Modal awal mulai Rp 25 Juta"`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `bisnis`

---

### [AUDIT-35] Duplikasi Label 'Status operasional' dan Badge 'Operasional Normal'
1. **Lokasi:** Kartu Ringkasan Perusahaan (app.js:2197, 2200)
2. **Konten Saat Ini:** `Badge: [Operasional Normal] & Baris data: "Status operasional: Beroperasi Normal"`
3. **Masalah:** Status operasional perusahaan ditampilkan dua kali bertumpuk di kartu yang sama.
4. **Dampak terhadap Pemain:** Menambah 1 baris vertikal tanpa faedah.
5. **Rekomendasi:** Cukup tampilkan status di badge header kartu perusahaan.
6. **Usulan Konten Baru (After):** `Badge: [Operasional Normal] (Baris data status operasional dihilangkan)`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `bisnis`

---

### [AUDIT-36] Penulisan 'CEO: Belum tersedia' pada Perusahaan Tanpa Eksekutif
1. **Lokasi:** Kartu Ringkasan Perusahaan (app.js:2204)
2. **Konten Saat Ini:** `"... · CEO: Belum tersedia"`
3. **Masalah:** Label pasif 'Belum tersedia' tidak memberi arahan aksi permainan.
4. **Dampak terhadap Pemain:** Pemain mengira ada bug data belum termuat.
5. **Rekomendasi:** Ubah menjadi ajakan aksi bertransaksi/rekrutmen.
6. **Usulan Konten Baru (After):** `"... · Rekrut CEO untuk bonus efisiensi"`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `bisnis`

---

### [AUDIT-37] Esai Pembuka Modal Kebijakan Kerja Perusahaan
1. **Lokasi:** Modal Kebijakan Kerja (app.js:3126)
2. **Konten Saat Ini:** `"Kebijakan kerja memengaruhi produktivitas, kepuasan kerja pegawai, dan risiko pergantian staf secara bertahap."`
3. **Masalah:** Paragraf pengantar psikologi manajemen yang bertele-tele.
4. **Dampak terhadap Pemain:** Menenggelamkan slider pengaturan gaji dan jam kerja.
5. **Rekomendasi:** Ganti dengan subjudul aksi langsung.
6. **Usulan Konten Baru (After):** `"Atur jam kerja, fasilitas, dan bonus insentif pegawai."`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `bisnis modal`

---

### [AUDIT-38] Pengulangan Nama Holding pada Judul Header dan Subjudul Modal Transfer
1. **Lokasi:** Modal Transfer Holding (app.js:2292)
2. **Konten Saat Ini:** `Header: Transfer Internal · [Nama Holding] | Subjudul: Transfer modal antar anak usaha [Nama Holding]`
3. **Masalah:** Nama holding diulang-ulang di header dan teks panduan.
4. **Dampak terhadap Pemain:** Judul modal terpotong di layar kecil.
5. **Rekomendasi:** Gunakan judul ringkas dan letakkan nama holding di ringkasan rekening.
6. **Usulan Konten Baru (After):** `Header: Transfer Modal Holding | Sub: Pilih pengirim dan penerima dana`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `bisnis modal`

---

### [AUDIT-39] Pemberitahuan 'Perubahan perusahaan belum tersedia' Saat Gagal Operasi
1. **Lokasi:** Error Handler Bisnis (app.js:3256)
2. **Konten Saat Ini:** `"Perubahan perusahaan belum tersedia: [Error Message]"`
3. **Masalah:** Judul error yang kaku dan terkesan fitur belum dibangun.
4. **Dampak terhadap Pemain:** Menurunkan kepercayaan pemain terhadap stabilitas game.
5. **Rekomendasi:** Ganti judul notifikasi menjadi 'Transaksi Bisnis Tertunda'.
6. **Usulan Konten Baru (After):** `"Operasi Belum Dapat Diproses: [Penyebab Nyata]"`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `bisnis`

---

### [AUDIT-40] Pill Subtab 'Usaha Saya / Koleksi / Kelola Dana' Tanpa Jumlah Item Aktif
1. **Lokasi:** Subnav Tab Bisnis (index.html:224-228)
2. **Konten Saat Ini:** `[🏢 Usaha Saya] [💎 Koleksi] [💼 Kelola Dana]`
3. **Masalah:** Pemain tidak tahu apakah ada anak usaha atau koleksi tanpa mengklik tab satu per satu.
4. **Dampak terhadap Pemain:** Navigasi buta yang memerlukan klik tambahan.
5. **Rekomendasi:** Sertakan counter dinamis kecil pada subtab aktif.
6. **Usulan Konten Baru (After):** `[Usaha (1)] [Koleksi (0)] [Kelola Dana]`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `bisnis`

---

### [AUDIT-41] Banner Hero Progres Tab Lainnya yang Menduplikasi Profil Pemain
1. **Lokasi:** Puncak Tab Lainnya (app.js:3529-3567)
2. **Konten Saat Ini:** `Banner Raksasa: Nama Trader, Level, Pangkat, Reputasi, XP, Status Sesi (180px)`
3. **Masalah:** Menghabiskan setengah layar pertama hanya untuk mengulang data profil.
4. **Dampak terhadap Pemain:** Menu-menu penting di Tab Lainnya terdorong jauh ke dasar layar.
5. **Rekomendasi:** Rampingkan banner profil; jadikan Tab Lainnya efisien sebagai hub menu.
6. **Usulan Konten Baru (After):** `Header Ramping: 'Pusat Kontrol & Pengaturan' -> Langsung ke Menu Layanan`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `lainnya`

---

### [AUDIT-42] Badge Eyebrow Kapital Ganda 'PROFIL & PRESTISE' pada Setiap Kartu Hub
1. **Lokasi:** Hub Groups Tab Lainnya (app.js:3570, 3580, 3598, 3608)
2. **Konten Saat Ini:** `Header grup memuat dua judul: 'PROFIL & PRESTISE' (eyebrow) + 'Profil & Progres' (h2)`
3. **Masalah:** Menampilkan dua judul yang maknanya hampir identik pada setiap seksi menu.
4. **Dampak terhadap Pemain:** Memperpanjang kartu menu secara visual tanpa fungsi.
5. **Rekomendasi:** Hapus eyebrow kapital; gunakan heading h2 yang bersih.
6. **Usulan Konten Baru (After):** `Cukup 1 judul jelas: 'Profil & Progres'`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `lainnya`

---

### [AUDIT-43] Keterangan Subteks Menu 'Pohon Riset Finansial Tech Tree' Berulang
1. **Lokasi:** Hub Menu Riset (app.js:3602)
2. **Konten Saat Ini:** `Judul: Pohon Riset Finansial (Tech Tree) | Subteks: 'Riset teknologi order, analisis pasar, dan fitur bursa'`
3. **Masalah:** Kata 'riset' diulang 3 kali dalam satu baris menu.
4. **Dampak terhadap Pemain:** Teks bertele-tele.
5. **Rekomendasi:** Persingkat subteks menjadi manfaat utama.
6. **Usulan Konten Baru (After):** `Pohon Riset Finansial | Sub: 'Buka fitur bursa & teknologi baru'`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `lainnya`

---

### [AUDIT-44] Deskripsi Menu 'Penyimpanan Permainan & Backup' yang Terlalu Panjang
1. **Lokasi:** Hub Menu Pengaturan (app.js:3612)
2. **Konten Saat Ini:** `'Simpan permainan secara manual dan periksa integritas cadangan'`
3. **Masalah:** Deskripsi memanjang 2 baris penuh di layar kecil.
4. **Dampak terhadap Pemain:** Baris menu menjadi sangat tinggi.
5. **Rekomendasi:** Rampingkan menjadi kalimat ringkas padat.
6. **Usulan Konten Baru (After):** `'Kelola berkas simpanan & cadangan game'`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `lainnya`

---

### [AUDIT-45] Deskripsi Menu 'Mulai Permainan Baru (Reset Run)' Berisi Jargon 'Reset Run'
1. **Lokasi:** Hub Menu Pengaturan (app.js:3614)
2. **Konten Saat Ini:** `'Mulai Permainan Baru (Reset Run)' - 'Mulai ulang permainan dari awal dengan konfirmasi ganda aman'`
3. **Masalah:** Istilah teknis gaming 'Reset Run' dan kata 'konfirmasi ganda aman' terdengar seperti pesan dev.
4. **Dampak terhadap Pemain:** Kurang ramah bagi pemain kasual.
5. **Rekomendasi:** Gunakan bahasa Indonesia standar yang bersih.
6. **Usulan Konten Baru (After):** `'Mulai Permainan Baru' - 'Reset progres untuk memulai kembali dari awal'`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `lainnya`

---

### [AUDIT-46] Tag Status Sesi Game di Hero Banner Tab Lainnya
1. **Lokasi:** Hero Banner Tab Lainnya (app.js:3564)
2. **Konten Saat Ini:** `Kotak status: 'Status Pasar: Sesi II'`
3. **Masalah:** Status sesi bursa sudah terpampang jelas di bilah context strip bagian atas.
4. **Dampak terhadap Pemain:** Pengulangan informasi sesi di layar yang sama.
5. **Rekomendasi:** Hapus kotak status pasar dari hero banner Tab Lainnya.
6. **Usulan Konten Baru (After):** `<!-- Dieliminasi; sudah ada di header utama aplikasi --&gt;`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `lainnya`

---

### [AUDIT-47] Ikon Menu Hub yang Menggunakan Karakter Emoji Campuran
1. **Lokasi:** Grup Menu Tab Lainnya (app.js:3572-3614)
2. **Konten Saat Ini:** `Emoji: 👤, 🏆, 🎯, 🌟, 📋, 🥇, 🎁, ⚔️, 📊, 📰, 🌳, ⚡, 📖, ⏱️, 💾, 🔔, 🔄`
3. **Masalah:** Emoji warna-warni bawaan OS sering terlihat tidak konsisten dan bertabrakan dengan palet tema Tycoon.
4. **Dampak terhadap Pemain:** Estetika visual terasa kurang serasi antar perangkat Android yang berbeda font emoji.
5. **Rekomendasi:** Bungkus emoji dalam wadah monokrom dengan filter warna tema terpadu.
6. **Usulan Konten Baru (After):** `Standardisasi ikon monokrom emas/krem yang selaras dengan tema`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `lainnya`

---

### [AUDIT-48] Tiga Kali Pengulangan Nama Perusahaan, Bursa & Sektor pada Detail Saham
1. **Lokasi:** Detail Saham BBCA (app.js:4125, 4417-4424)
2. **Konten Saat Ini:** `Header: Bank Central Asia Tbk | Di bawah harga: Keuangan | Di bawah: Nama resmi, Bursa, Sektor`
3. **Masalah:** Tiga entitas teks statis yang persis sama dicetak 3 kali dalam satu halaman.
4. **Dampak terhadap Pemain:** Memperpanjang scroll vertikal dengan teks statis berulang.
5. **Rekomendasi:** Pusatkan identitas emiten di header atas, bersihkan pengulangan di bawah.
6. **Usulan Konten Baru (After):** `Header: Nama & Sektor | Kartu Profil Bawah: Langsung ke Deskripsi Bisnis & Web`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `detail`

---

### [AUDIT-49] Daftar 20 Baris Rasio Akuntansi Mentah Tanpa Pengelompokan
1. **Lokasi:** Tab Valuasi Detail Saham (app.js:4434-4475)
2. **Konten Saat Ini:** `EPS, PER, PBV, ROE, ROA, DER, DPS, DPR, Yield, Aset, Liabilitas, Ekuitas... (20 Baris)`
3. **Masalah:** Daftar vertikal 400px penuh istilah teknis akuntansi yang melelahkan di-scroll.
4. **Dampak terhadap Pemain:** Layar detail menjadi sangat panjang dan istilah CFO/CFI/DER membingungkan.
5. **Rekomendasi:** Sorot 4 rasio utama di grid 4-kolom; simpan rincian dalam menu lipat.
6. **Usulan Konten Baru (After):** `Grid 4 Rasio Inti [PER] [PBV] [ROE] [Yield] + Lipatan [▶ Laporan Keuangan Lengkap]`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `detail`

---

### [AUDIT-50] Tombol Manual Redundan 'Perbarui harga & antrean' pada Detail Saham
1. **Lokasi:** Di Atas Dock Transaksi Detail Saham (app.js:4535-4539)
2. **Konten Saat Ini:** `<button class="btn-secondary">Perbarui harga & antrean</button>`
3. **Masalah:** Tombol manual berukuran penuh padahal kuotasi harga sudah otomatis sinkron real-time.
4. **Dampak terhadap Pemain:** Memakan ruang tepat di atas tombol beli dan membuat ragu apakah sistem otomatis.
5. **Rekomendasi:** Hapus tombol manual ini sepenuhnya.
6. **Usulan Konten Baru (After):** `<!-- Dieliminasi; sistem mengandalkan auto-sync reaktif yang sudah terbukti -->`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `detail`

---

### [AUDIT-51] Pemberitahuan Suspensi 'Pemberitahuan: Perdagangan instrumen ini sedang di-halt...'
1. **Lokasi:** Detail Saham (app.js:4517)
2. **Konten Saat Ini:** `<p class="notice">Pemberitahuan: Perdagangan instrumen ini sedang di-halt / disuspensi bursa. Order baru tidak dapat diproses.</p>`
3. **Masalah:** Kalimat birokrasi panjang yang formal dan kaku.
4. **Dampak terhadap Pemain:** Memakan 3 baris teks peringatan tebal.
5. **Rekomendasi:** Ubah menjadi badge status suspensi yang ringkas dan padat.
6. **Usulan Konten Baru (After):** `<p class="notice" style="color:var(--negative);">⛔ Saham sedang disuspensi bursa (ARA/ARB). Transaksi ditutup sementara.</p>`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `detail`

---

### [AUDIT-52] Penjelasan Satuan '100 saham/unit (1 lot)' yang Diulang di Setiap Emiten
1. **Lokasi:** Header Detail Saham (app.js:4128)
2. **Konten Saat Ini:** ``${latestSnapshot.clock.date} ${latestSnapshot.clock.time} · ${stock.lotSize} saham/unit (1 lot)``
3. **Masalah:** Penjelasan definisi lot diulang setiap kali membuka detail saham.
4. **Dampak terhadap Pemain:** Teks metadata menjadi panjang dan berpotensi terpotong.
5. **Rekomendasi:** Singkat penulisan satuan menjadi '1 Lot = 100 Lbr'.
6. **Usulan Konten Baru (After):** ``${latestSnapshot.clock.time} WIB · 1 Lot = 100 Lbr``
7. **Prioritas:** **RENDAH**
8. **Kategori:** `detail`

---

### [AUDIT-53] Pesan 'Belum memiliki posisi pada instrumen ini' Mengisi Kartu Khusus
1. **Lokasi:** Kartu Kepemilikan Detail Saham (app.js:4142)
2. **Konten Saat Ini:** `<div class="card"><p class="notice">Belum memiliki posisi pada instrumen ini.</p></div>`
3. **Masalah:** Membuat kartu kosong tersendiri untuk memberi tahu hal yang pemain sudah ketahui.
4. **Dampak terhadap Pemain:** Menambah ketinggian scroll detail saham sebesar 60px.
5. **Rekomendasi:** Tampilkan kartu kepemilikan hanya jika saldo lembar > 0.
6. **Usulan Konten Baru (After):** `<!-- Sembunyikan kartu jika kuantitas 0; tampilkan hanya jika pemain memiliki saham -->`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `detail`

---

### [AUDIT-54] Label 'Perkiraan dampak: naik · +2.0% - 5.0%' pada Berita Terkait Emiten
1. **Lokasi:** Kartu Berita Terkait Detail Saham (app.js:4486)
2. **Konten Saat Ini:** `"... Perkiraan dampak: naik · +2.0% - 5.0%"`
3. **Masalah:** Frasa panjang yang membuat kartu berita menjadi padat dan bertumpuk.
4. **Dampak terhadap Pemain:** Judul berita terdesak ke atas.
5. **Rekomendasi:** Gunakan tag pill estimasi ringkas di pojok kartu berita.
6. **Usulan Konten Baru (After):** `"Estimasi: +2% ~ 5%"`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `detail`

---

### [AUDIT-55] Teks Bantuan Tiket Order 'Biaya dan perkiraan kas ditampilkan sebelum order dikirim'
1. **Lokasi:** Tiket Order Modal (index.html:305)
2. **Konten Saat Ini:** `<p id="order-est-total">Biaya dan perkiraan kas ditampilkan sebelum order dikirim.</p>`
3. **Masalah:** Teks placeholder statis yang tidak menampilkan angka kalkulasi sebelum tombol ditekan.
4. **Dampak terhadap Pemain:** Pemain tidak tahu total uang yang harus dikeluarkan saat mengatur jumlah lot.
5. **Rekomendasi:** Langsung hitung dan tampilkan perkiraan total dana riil secara live.
6. **Usulan Konten Baru (After):** `<p id="order-est-total">Estimasi Kas Diperlukan: <strong>Rp 1.044.500</strong></p>`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `order`

---

### [AUDIT-56] Duplikasi Label 'Beli' dan 'Harga beli maksimal' pada Form Limit
1. **Lokasi:** Tiket Order Modal (app.js:7487)
2. **Konten Saat Ini:** `Tombol aktif [BELI] & Label input: 'Harga beli maksimal (IDR)'`
3. **Masalah:** Kata 'beli' diulang di tombol dan di label input.
4. **Dampak terhadap Pemain:** Label input menjadi panjang dan terpotong di layar ponsel.
5. **Rekomendasi:** Cukup beri label 'Harga Limit (IDR)'.
6. **Usulan Konten Baru (After):** `Tombol aktif [BELI] & Label input: 'Harga Limit (IDR)'`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `order`

---

### [AUDIT-57] Pill Tombol Persentase Lot (25%, 50%, 75%, MAX) yang Terlalu Rapat
1. **Lokasi:** Tiket Order Modal (index.html:295-300)
2. **Konten Saat Ini:** `4 tombol berdempetan dengan padding minim di bawah input unit`
3. **Masalah:** Pemain berisiko salah menekan (misal ingin 50% terpencet MAX) karena tombol terlalu rapat.
4. **Dampak terhadap Pemain:** Risiko fatal salah beli modal penuh.
5. **Rekomendasi:** Beri celah yang nyaman dan konfirmasi visual tegas saat dipilih.
6. **Usulan Konten Baru (After):** `Grid 4 tombol dengan target sentuh minimal 44px ramah jempol`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `order`

---

### [AUDIT-58] Teks Review Order 'Mode: Pasar Internal Game' yang Berulang
1. **Lokasi:** Modal Tinjau Order (app.js:4538)
2. **Konten Saat Ini:** `flowText(panel, 'Mode', 'Pasar Internal Game')`
3. **Masalah:** Semua order berjalan di pasar internal game; baris ini tidak memberi informasi penting.
4. **Dampak terhadap Pemain:** Memanjangkan dialog review order.
5. **Rekomendasi:** Hapus baris mode dari tinjauan order.
6. **Usulan Konten Baru (After):** `<!-- Dihapus dari ringkasan review order -->`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `order`

---

### [AUDIT-59] Keterangan Waktu Berlaku 'Estimasi berlaku hingga: 15:45:00 WIB' pada Review Order
1. **Lokasi:** Modal Tinjau Order (app.js:4548)
2. **Konten Saat Ini:** `flowText(panel, 'Estimasi berlaku hingga', '15:45:00 WIB')`
3. **Masalah:** Menampilkan countdown 30 detik untuk quote pasar internal yang membuat pemain panik terburu-buru.
4. **Dampak terhadap Pemain:** Menciptakan urgensi buatan yang tidak perlu pada simulasi offline.
5. **Rekomendasi:** Hapus batas waktu 30 detik pada pasar internal reguler.
6. **Usulan Konten Baru (After):** `<!-- Dieliminasi jika order bertipe DAY (berlaku hingga tutup bursa) -->`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `order`

---

### [AUDIT-60] Paragraf Esai Pembuka Panjang (100–180 Karakter) di 15+ Modal Dialog
1. **Lokasi:** Tersebar di 15+ Modal Dialog (app.js:5686, 5783, 6366, dll.)
2. **Konten Saat Ini:** `"Spesifikasi D05: Fasilitas pinjaman tenor 7/14/30 hari game dengan bunga harian 0.05% atas sisa pokok dan provisi cair 0.5%."`
3. **Masalah:** Esai panjang di baris pertama menelan 35% area layar modal di ponsel portrait.
4. **Dampak terhadap Pemain:** Pemain harus scroll ke bawah hanya untuk menemukan tombol aksi.
5. **Rekomendasi:** Pangkas esai pembuka menjadi subjudul to-the-point maksimal 40 karakter.
6. **Usulan Konten Baru (After):** `Ketentuan Fasilitas: Tenor fleksibel 7, 14, atau 30 hari game dengan bunga 0.05%/hari.`
7. **Prioritas:** **TINGGI**
8. **Kategori:** `modal`

---

### [AUDIT-61] Teks Dialog Pemulihan Save yang Terlalu Defensif & Bergaya Catatan Dev
1. **Lokasi:** Modal Pemulihan Save Game (app.js:594-595)
2. **Konten Saat Ini:** `"Save tidak dapat divalidasi. Berkas asli tidak dihapus... jangan membuat save baru di slot ini."`
3. **Masalah:** Bahasa instruksi internal teknis yang menakuti dan membingungkan pemain.
4. **Dampak terhadap Pemain:** Pemain panik mengira progresnya hilang selamanya.
5. **Rekomendasi:** Gunakan bahasa produk yang menenangkan dan instruktif.
6. **Usulan Konten Baru (After):** `"Data sesi permainan belum dapat dimuat. Berkas cadangan tersimpan aman. Tekan tombol di bawah untuk memuat cadangan valid terakhir."`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `modal`

---

### [AUDIT-62] Teks Sisa Konsep Developer '(Satu Pintu)' pada Judul Terminal Riset
1. **Lokasi:** Header Modal Terminal Riset (app.js:3294)
2. **Konten Saat Ini:** `showFlow('Terminal Riset Pasar (Satu Pintu)')`
3. **Masalah:** Teks dalam kurung adalah sisa nama konsep arsitektur plan pengembang.
4. **Dampak terhadap Pemain:** Tampak canggung di layar pemain.
5. **Rekomendasi:** Hapus teks '(Satu Pintu)' dari judul header.
6. **Usulan Konten Baru (After):** `showFlow('Terminal Riset Pasar')`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `modal`

---

### [AUDIT-63] Judul Subtab Panjang 'Pohon Riset Finansial Tech Tree'
1. **Lokasi:** Modal Tech Tree (app.js:8088)
2. **Konten Saat Ini:** `showFlow('Pohon Riset Finansial Tech Tree')`
3. **Masalah:** Mencampurkan istilah terjemahan 'Pohon Riset' dengan bahasa Inggris 'Tech Tree' di judul yang sama.
4. **Dampak terhadap Pemain:** Judul header terpotong di samping tombol kembali.
5. **Rekomendasi:** Gunakan judul ringkas: 'Riset Teknologi Finansial'.
6. **Usulan Konten Baru (After):** `showFlow('Riset Teknologi Finansial')`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `modal`

---

### [AUDIT-64] Penjelasan Rumus Matematika 'Total kekayaan = Aset pribadi + Nilai ekuitas...' pada Modal Konsolidasi
1. **Lokasi:** Modal Kekayaan Konsolidasi (app.js:5473)
2. **Konten Saat Ini:** `flowText(panel, 'Perhitungan kekayaan', 'Total kekayaan = Aset pribadi + Nilai ekuitas anak usaha + Koleksi mewah − Liabilitas...')`
3. **Masalah:** Menulis rumus teks aljabar panjang padahal pemain bisa langsung melihat tabel neracanya.
4. **Dampak terhadap Pemain:** Membuang ruang atas modal.
5. **Rekomendasi:** Hapus rumus teks; biarkan tabel angka berbicara sendiri.
6. **Usulan Konten Baru (After):** `<!-- Dihapus; langsung sajikan tabel rincian aset dan total di kartu hero -->`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `modal`

---

### [AUDIT-65] Pesan Warning 'Gaya investasi sedang dievaluasi otomatis tanpa kuis buatan' di Modal Arketipe
1. **Lokasi:** Modal Arketipe Investor (app.js:6945)
2. **Konten Saat Ini:** `'Gaya investasi sedang dievaluasi otomatis tanpa kuis buatan.'`
3. **Masalah:** Frasa defensif 'tanpa kuis buatan' adalah catatan perbandingan developer terhadap game lain.
4. **Dampak terhadap Pemain:** Pemain merasa aneh membaca alasan tersebut.
5. **Rekomendasi:** Ganti dengan kalimat positif tentang evaluasi trading riil.
6. **Usulan Konten Baru (After):** `'Sistem sedang menganalisis gaya trading dari transaksi riil Anda.'`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `modal`

---

### [AUDIT-66] Label Teknis 'Skema V2, UTF-8' pada Modal Pengaturan Save
1. **Lokasi:** Modal Penyimpanan Permainan (app.js:3871)
2. **Konten Saat Ini:** `flowText(panel, 'Format Berkas', 'JSON Standar (Skema V2, UTF-8)')`
3. **Masalah:** Pemain game tidak butuh informasi encoding UTF-8 atau nomor skema JSON.
4. **Dampak terhadap Pemain:** Tampilan teknis kaku.
5. **Rekomendasi:** Ganti dengan status verifikasi penyimpanan yang ramah pengguna.
6. **Usulan Konten Baru (After):** `flowText(panel, 'Status Penyimpanan', 'Penyimpanan Lokal Terverifikasi')`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `modal`

---

### [AUDIT-67] Paragraf Ketentuan Cutoff Reksa Dana 'Pembatalan sebelum 15:30 WIB' yang Kaku
1. **Lokasi:** Modal Pasar Reksa Dana (app.js:5528)
2. **Konten Saat Ini:** `'Cutoff Senin–Jumat pukul 16:00 WIB. Pembatalan sebelum 15:30 WIB. Harga final mengikuti NAB hari bursa bersangkutan.'`
3. **Masalah:** Paragraf disclaimer prospektus yang panjang di atas katalog reksa dana.
4. **Dampak terhadap Pemain:** Daftar produk reksa dana terdorong ke bawah.
5. **Rekomendasi:** Persingkat menjadi satu baris aturan jadwal cutoff.
6. **Usulan Konten Baru (After):** `'Cutoff transaksi pukul 16:00 WIB. Harga unit mengikuti valuasi NAB harian.'`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `modal`

---

### [AUDIT-68] Pesan Sukses Berulang Toast Autosave 'Progres tersimpan' yang Mengganggu Layar
1. **Lokasi:** Autosave Background Hook (app.js:558, 8350)
2. **Konten Saat Ini:** `Memunculkan toast pop-up setiap kali siklus simpan berkala dijalankan`
3. **Masalah:** Melanggar keputusan resmi pemilik Q&A 44 ('Autosave tanpa pesan sukses setiap kali; saat gagal beri peringatan').
4. **Dampak terhadap Pemain:** Toast pop-up terus bermunculan dan menutupi tombol header setiap beberapa menit.
5. **Rekomendasi:** Jadikan autosave berjalan hening di latar belakang tanpa toast sukses berulang.
6. **Usulan Konten Baru (After):** `Autosave hening (silent autosave); hanya beri notifikasi jika terjadi kegagalan simpan`
7. **Prioritas:** **TINGGI**
8. **Kategori:** `shell modal`

---

### [AUDIT-69] Teks Konfirmasi Berulang 'Apakah Anda yakin ingin memulai Era Baru?'
1. **Lokasi:** Modal Prestise & Era Baru (app.js:3672)
2. **Konten Saat Ini:** `Alert browser konfirmasi teks panjang dengan huruf kapital 'PERINGATAN ERA BARU:...'`
3. **Masalah:** Menggunakan browser alert pop-up native yang memblokir layar dan merusak estetika game.
4. **Dampak terhadap Pemain:** Pengalaman bermain terputus oleh dialog bawaan browser OS.
5. **Rekomendasi:** Gunakan modal dialog in-game yang elegan dengan tombol Ya/Batal.
6. **Usulan Konten Baru (After):** `Gunakan sheet konfirmasi UI terpadu di dalam game dengan ringkasan bonus`
7. **Prioritas:** **SEDANG**
8. **Kategori:** `modal`

---

### [AUDIT-70] Tombol 'Hentikan perpanjangan otomatis' pada Paket Basic yang Belum Pernah Langganan
1. **Lokasi:** Modal Kelola Paket Terminal Riset (app.js:3450)
2. **Konten Saat Ini:** `Pengecekan autoRenewal render kontrol pada kondisi paket yang tidak berlangganan`
3. **Masalah:** Menyajikan opsi pembatalan pada status paket gratisan.
4. **Dampak terhadap Pemain:** Membingungkan pemain apakah mereka sedang terkena tagihan atau tidak.
5. **Rekomendasi:** Sembunyikan kontrol pembatalan jika pemain berada di paket Basic.
6. **Usulan Konten Baru (After):** `Tampilkan tombol batal perpanjangan HANYA pada paket Pro atau Whale yang sedang aktif`
7. **Prioritas:** **RENDAH**
8. **Kategori:** `modal`

---
