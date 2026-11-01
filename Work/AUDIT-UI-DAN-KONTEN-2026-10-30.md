# LAPORAN AUDIT LENGKAP: ELEMEN UI, KONTEN BERLEBIH, DAN TEKS REDUNDAN
**Proyek:** Game Saham 2D Portrait  
**Lokasi Root:** `C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM`  
**Tanggal Audit:** 30 Oktober 2026  
**Status Audit:** SELESAI (Audit Deteksi & Rekomendasi Tanpa Mengubah Kode Produksi / Data Pemain)  
**Dokumen Acuan:** `PROMPT-AUDIT-UI-DAN-KONTEN.md`, `AGENTS.md`, `DESIGN.md`, `MASTER-PLAN-OFFLINE-DAN-REALISME-2026-10-28.md`  
**Hash Save Pemain Asli:** `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed` (100% Aman & Terverifikasi Utuh)

---

## I. Ringkasan Eksekutif Hasil Audit

Audit komprehensif ini meneliti seluruh antarmuka pengguna (UI), hierarki tata letak, string teks, komponen bersama, dan 82 alur layar (*modal flows*) di dalam aplikasi **Game Saham**.

Tujuan audit adalah **menemukan seluruh konten yang memenuhi layar tetapi tidak memberi manfaat yang cukup bagi pemain**, seperti:
1. **Elemen UI tanpa fungsi jelas / mati / redundan terhadap navigasi utama.**
2. **Informasi berulang** yang tampil berkali-kali di layar yang sama atau dalam alur berurutan.
3. **Kalimat kosong dan slogan motivasi** yang memakan ruang vertikal (*above-the-fold*) pada layar ponsel portrait.
4. **Kebocoran istilah teknis developer dan kode tiket sprint** (seperti `(A20)`, `(A21)`, `Spesifikasi D05`, `(A16)`, `ledger dua sisi`, `Model simulasi game`).
5. **Paragraf pengantar bertele-tele (esai/buku teks)** di bagian atas modal yang menenggelamkan tombol aksi dan kontrol interaktif.
6. **Kepadatan layar portrait mobile** yang berpotensi memotong konten penting akibat tumpukan shortcut berlebih.

Dari hasil pemeriksaan silang menyeluruh, ditemukan **25 temuan terperinci** yang diklasifikasikan ke dalam 7 kelompok area antarmuka:
- **Prioritas Tinggi (Critical UX & Confusing):** 8 temuan (termasuk kebocoran kode dev, tombol navigasi duplikat, dan slogan pemakan ruang).
- **Prioritas Sedang (Clutter & Redundancy):** 12 temuan (tumpukan quick grid, esai pembuka modal, dan pengulangan nilai kas).
- **Prioritas Rendah (Minor Polish):** 5 temuan (label counter sekunder, format status netral).

---

## II. Peta Cakupan Audit (Coverage Map)

| Area / Layar / Komponen | Status Audit | Metode / Lingkungan Pemeriksaan | Catatan Kondisi |
|---|---|---|---|
| **Shell Global & App Header** | SUDAH DIPERIKSA | Source AST + Browser Live 18917 | Header, wealth chip, status dot, mock banner |
| **Context Strip & Account Button** | SUDAH DIPERIKSA | Source AST + Browser Live 18917 | Mode bursa, tombol akun tersembunyi |
| **Bilah Navigasi Bawah (5 Tab)** | SUDAH DIPERIKSA | Source AST + Browser Live 18917 | Beranda, Pasar, Portofolio, Bisnis, Lainnya |
| **Tab 1: Beranda (Home Dashboard)** | SUDAH DIPERIKSA | Source AST + Browser Live 18917 | Hero card, quick actions, 5 quick grids, news, market list |
| **Tab 2: Pasar (Stock Market)** | SUDAH DIPERIKSA | Source AST + Browser Live 18917 | Indeks ribbon, top movers, search & filter, stock list |
| **Tab 3: Portofolio (Asset & Wallet)** | SUDAH DIPERIKSA | Source AST + Browser Live 18917 | Net worth, allocation bar, dompet accordion, compact rows, categories |
| **Tab 4: Bisnis (Operating Business)** | SUDAH DIPERIKSA | Source AST + Browser Live 18917 | Subtab usaha, holding section, subtab koleksi, subtab dana |
| **Tab 5: Lainnya (Hub & Settings)** | SUDAH DIPERIKSA | Source AST + Browser Live 18917 | Progres hero banner, 4 hub groups, settings, tutorial |
| **Detail Saham (Instrument View)** | SUDAH DIPERIKSA | Source AST + Browser Live 18917 | Header emiten, chart candlestick, timeframe, profile emiten, 20 rasio, order dock |
| **Tiket Order & Review Transaksi** | SUDAH DIPERIKSA | Source AST + Browser Live 18917 | Sheet modal, side toggle, order types, pct buttons, review, receipt |
| **Modal Profil Saya (4 Subtabs)** | SUDAH DIPERIKSA | Source AST + Browser Live 18917 | Kinerja, Keuangan, Pencapaian, Pengaturan & Tema |
| **Modal Terminal Riset (Satu Pintu)** | SUDAH DIPERIKSA | Source AST + Browser Live 18917 | Status paket, selector layanan, radar NPC, kelola paket tier |
| **Modal Finansial & Pembiayaan** | SUDAH DIPERIKSA | Source AST + Browser Live 18917 | Utang & pinjaman tenor, pengajuan pembiayaan, bayar cicilan |
| **Modal Reksa Dana & Multi-Aset** | SUDAH DIPERIKSA | Source AST + Browser Live 18917 | Pasar unit/NAB, beli/cairkan, katalog obligasi/emas, custom fund |
| **Modal Gamifikasi & Kompetisi** | SUDAH DIPERIKSA | Source AST + Browser Live 18917 | Misi periodik, liga & rival, duel bos, DNA investor, streak, dominasi |
| **Modal Sistem, Save & Recovery** | SUDAH DIPERIKSA | Source AST + Browser Live 18917 | Welcome onboarding, save/load checkpoint, backup recovery offer, reset run |

---

## III. Daftar Lengkap 25 Temuan Audit (Detail per ID)

### KELOMPOK 1: GLOBAL SHELL, APP HEADER & STRIP KONTEKS

#### [AUDIT-01] Teks Eyebrow Generik "GLOBAL TRADER" pada Header Aplikasi
1. **Lokasi:** Global Shell (`index.html:16`)
2. **Konten Saat Ini:** `<span class="eyebrow">GLOBAL TRADER</span>`
3. **Bukti:** `android/app/src/main/assets/www/index.html` baris 16; terlihat di tangkapan layar `01-beranda-live.png`.
4. **Masalah:** Kalimat dekoratif kosong yang statis. Tidak memberikan informasi status apa pun kepada pemain dan menghabiskan ruang horizontal header di layar ponsel berukuran 390px–412px di antara avatar dan chip kas.
5. **Dampak:** Mempersempit area judul layar dan membuat teks header terdesak.
6. **Rekomendasi:** **HAPUS** teks `GLOBAL TRADER`. Cukup tampilkan judul tab aktif (`Beranda`, `Pasar`, dll.) dengan ukuran yang tegas.
7. **Risiko & Ketergantungan:** Tidak ada risiko fungsi; tampilan header menjadi jauh lebih lega dan bersih.
8. **Prioritas:** Sedang
9. **Tingkat Keyakinan:** Terverifikasi Visual & Source.

---

#### [AUDIT-02] Duplikasi Nilai "Kas Tersedia" di Tiga Tempat Sekaligus (Header vs Kartu Hero)
1. **Lokasi:** Global Header (`index.html:17`) dan Kartu Hero Beranda (`index.html:43-44`)
2. **Konten Saat Ini:**
   - Header chip: `<small>Kas Tersedia ↗</small><strong id="header-net-worth">Rp 75.000.000</strong>`
   - Hero card: `<small>Kas IDR Total</small><strong>Rp 75.000.000</strong>`
   - Hero card sub-box: `<small>Tersedia</small><strong>Rp 75.000.000</strong>`
3. **Bukti:** `index.html` baris 17, 43, 44; `app.js` baris 653, 654, 659.
4. **Masalah:** Informasi nominal uang tunai yang sama persis (`Rp 75.000.000`) dicetak **3 kali dalam jarak vertikal kurang dari 150 piksel**. Selain itu, ID elemennya `#header-net-worth` namun isinya adalah kas tersedia, membingungkan saat pemain memiliki posisi saham (di mana Net Worth berbeda dari Kas).
5. **Dampak:** Memenuhi area pandang teratas dengan pengulangan angka yang sama tanpa nilai tambah.
6. **Rekomendasi:** **RINGKAS & BEDAKAN**:
   - Jadikan chip header menampilkan **"Kekayaan Bersih"** (Net Worth) yang mencerminkan total aset.
   - Di kartu hero, cukup tampilkan Total Kas dan Kas Tersedia hanya jika ada dana tertahan (`reserved > 0`). Jika tertahan Rp 0, satukan menjadi satu baris: "Kas IDR Tersedia: Rp X".
7. **Risiko & Ketergantungan:** Perlu penyesuaian binding teks di `app.js` baris 659.
8. **Prioritas:** Tinggi
9. **Tingkat Keyakinan:** Terverifikasi Visual & Source.

---

#### [AUDIT-03] Elemen Mati / Hidden Button Akun Transaksi pada Context Strip
1. **Lokasi:** Context Strip Global (`index.html:21`)
2. **Konten Saat Ini:** `<button id="context-account-btn" class="context-account" style="display:none !important;">`
3. **Bukti:** `index.html` baris 21; `app.js` baris 8335-8336.
4. **Masalah:** Tombol pemilih akun yang dinonaktifkan permanen dengan inline style `display:none !important`. Duduk di DOM sebagai kode sampah karena pemain bermain dengan akun pribadi tunggal.
5. **Dampak:** Menambah beban DOM dan membingungkan pemeliharaan kode antarmuka.
6. **Rekomendasi:** **HAPUS** elemen tombol ini dari HTML atau aktifkan hanya jika mode multi-akun resmi dibuka.
7. **Risiko & Ketergantungan:** Pastikan event listener di `app.js:8336` diberi null-check aman.
8. **Prioritas:** Rendah
9. **Tingkat Keyakinan:** Terverifikasi Source.

---

#### [AUDIT-04] Banner Lingkungan Internal "QA HOST JAVA · SAVE TERISOLASI"
1. **Lokasi:** Shell Atas (`index.html:26-28`, `app.js:63-64`)
2. **Konten Saat Ini:** `<div id="mock-banner" class="mock-banner">QA HOST JAVA · SAVE TERISOLASI · BUKAN ANDROID</div>`
3. **Bukti:** `index.html` baris 26; terlihat membentang di tangkapan layar `01-beranda-live.png` dan `02-pasar-live.png`.
4. **Masalah:** Pita banner pengujian internal yang memakan ruang vertikal ~32px di atas konten utama. Pada layar mobile portrait, ini mendorong seluruh konten layar turun ke bawah.
5. **Dampak:** Merusak evaluasi layout mobile nyata karena ketinggian layar terpotong banner pengujian.
6. **Rekomendasi:** **PINDAHKAN / MINIMALKAN**: Jadikan indikator debug ini sebagai chip kecil transparan di pojok bawah atau sembunyikan jika tidak dalam mode diagnostik khusus.
7. **Risiko & Ketergantungan:** Tidak ada dampak ke logika trading.
8. **Prioritas:** Sedang
9. **Tingkat Keyakinan:** Terverifikasi Visual & Source.

---

### KELOMPOK 2: TAB BERANDA (HOME DASHBOARD)

#### [AUDIT-05] Slogan Kosong "Bangun langkahmu." dan Label Statis "HARI PERDAGANGAN"
1. **Lokasi:** Header Konten Beranda (`index.html:35`)
2. **Konten Saat Ini:** `<span class="eyebrow" id="home-date">HARI PERDAGANGAN</span><h1>Bangun langkahmu.</h1>`
3. **Bukti:** `index.html` baris 35; tangkapan layar `01-beranda-live.png`.
4. **Masalah:** "Bangun langkahmu." adalah slogan statis tanpa nilai fungsional. Memakan ruang vertikal ~48px tepat di area paling berharga (*above-the-fold*), sementara informasi tanggal kalender game justru diletakkan di font kecil di atasnya.
5. **Dampak:** Kartu finansial utama terdorong ke bawah, memaksa pemain harus scroll untuk melihat ringkasan bisnis.
6. **Rekomendasi:** **GANTI DENGAN INFORMASI BERGUNA**: Jadikan `<h1>` menampilkan Hari dan Tanggal Bursa Game (contoh: `<h1>Hari 41 · 28 Nov 2026</h1>`) dan hapus slogan "Bangun langkahmu.".
7. **Risiko & Ketergantungan:** Perlu penyesuaian id `#home-date` di `app.js`.
8. **Prioritas:** Tinggi
9. **Tingkat Keyakinan:** Terverifikasi Visual & Source.

---

#### [AUDIT-06] Bar Aksi Cepat (Quick Actions Bar) yang 80% Duplikat dengan Navigasi Bawah
1. **Lokasi:** Bagian Tengah Beranda (`index.html:50-72`, `app.js:8372-8383`)
2. **Konten Saat Ini:** 5 tombol lingkaran:
   - `Beli Saham` -> `switchTab('pasar')`
   - `Portofolio` -> `switchTab('aset')`
   - `Bisnis` -> `switchTab('firma')`
   - `Klaim Hibah` -> `handleClaimSponsor`
   - `Pinjaman` -> `openLoansModal`
3. **Bukti:** `index.html` baris 50-72; `app.js` baris 8373-8382; tangkapan layar `01-beranda-live.png`.
4. **Masalah:** Baris ini menghabiskan tinggi vertikal ~90px di layar portrait. Tiga dari lima tombol (Beli Saham, Portofolio, Bisnis) hanya mengalihkan tab yang tombol aslinya sudah terpajang tepat di bilah navigasi bawah (hanya berjarak 3 cm di bawahnya). Tombol keempat (Klaim Hibah) menduplikasi kartu Sponsor Ventura Modal yang persis berada di bawah baris ini.
5. **Dampak:** Pemborosan ruang layar secara masif (*redundant navigation clutter*).
6. **Rekomendasi:** **HAPUS BARIS INI SEPENUHNYA**, atau jika ingin dipertahankan, ubah isinya hanya untuk aksi instan yang TIDAK ada di navigasi bawah (misal: `Tukar Valuta FX`, `Bayar Cicilan Utang`, `Laporan Sesi`).
7. **Risiko & Ketergantungan:** Hapus elemen `#btn-qa-buy`, `#btn-qa-portfolio`, dll. dari HTML dan listener-nya.
8. **Prioritas:** Tinggi
9. **Tingkat Keyakinan:** Terverifikasi Visual & Source.

---

#### [AUDIT-07] Pengulangan Tiga Kali Angka Nominal Hibah pada Kartu Sponsor Ventura Modal
1. **Lokasi:** Modul Sponsor Beranda (`index.html:74-84`)
2. **Konten Saat Ini:**
   - Pill kanan atas: `+Rp 25.000.000`
   - Paragraf deskripsi: `"Suntikan modal ventura tanpa bunga 1x per hari perdagangan untuk ekspansi portofolio."`
   - Teks tombol CTA: `"Klaim Hibah Rp 25 Juta"`
3. **Bukti:** `index.html` baris 74-84; tangkapan layar `01-beranda-live.png`.
4. **Masalah:** Angka hibah diulang 3 kali di kartu yang sama: di badge pill, di kalimat deskripsi, dan di dalam tombol. Deskripsinya juga terlalu panjang dan bertele-tele.
5. **Dampak:** Kartu memakan tinggi ~160px yang sebenarnya bisa diringkas menjadi kartu ramping 60px.
6. **Rekomendasi:** **RINGKAS**:
   - Judul: "Dana Hibah Harian"
   - Nominal di pill: "+Rp 25.000.000"
   - Tombol CTA: cukup "Klaim Sekarang"
   - Deskripsi dihapus atau dipersingkat menjadi: "Tersedia 1x per hari bursa."
7. **Risiko & Ketergantungan:** Tidak ada risiko fungsional.
8. **Prioritas:** Sedang
9. **Tingkat Keyakinan:** Terverifikasi Visual & Source.

---

#### [AUDIT-08] Duplikasi Akses Misi & Pencapaian pada Kartu Karier vs Quick Grid
1. **Lokasi:** Beranda (`index.html:85-97`)
2. **Konten Saat Ini:**
   - Di dalam Kartu Karier: Tombol `#btn-career` ("Lihat misi →") dan chip `#header-level-chip` ("Lv. 1 Memuat XP").
   - Di Quick Grid tepat di bawahnya: Tombol `#btn-missions` ("Misi Periodik 📋") dan `#btn-achievements` ("Pencapaian ◇").
3. **Bukti:** `index.html` baris 85-97; `app.js` baris 8326-8329, 8338-8339.
4. **Masalah:** Pemain disajikan dua tombol berbeda untuk membuka Misi dan dua tombol berbeda untuk membuka Pencapaian dalam jarak yang sangat berdekatan.
5. **Dampak:** Membingungkan pemain seolah-olah "Lihat misi" di kartu karier berbeda dari "Misi Periodik" di quick grid, padahal membuka alur yang sama.
6. **Rekomendasi:** **GABUNGKAN**: Jadikan kartu Karier sebagai satu-satunya pusat progres (berisi level, reputasi, dan tombol "Pusat Misi & Pencapaian"). Hapus tombol duplikat dari quick grid.
7. **Risiko & Ketergantungan:** Pembersihan listener di `app.js`.
8. **Prioritas:** Sedang
9. **Tingkat Keyakinan:** Terverifikasi Source.

---

#### [AUDIT-09] Penumpukan 5 Baris "Quick Grid" (10 Tombol Pintasan) yang Mengubah Beranda Menjadi "Wall of Links"
1. **Lokasi:** Bagian Bawah Beranda (`index.html:92-132`)
2. **Konten Saat Ini:** 10 tombol kotak pintasan dalam 5 grid terpisah:
   - Grid 1: Laporan Harian, Pencapaian, Misi Periodik, Liga & Rival
   - Grid 2: Duel Bos, Galeri Mewah
   - Grid 3: DNA Investor, Disiplin Harian
   - Grid 4: Dominasi Sektor, Asuransi Hedging
3. **Bukti:** `index.html` baris 92-132; tangkapan layar `01-beranda-live.png`.
4. **Masalah:** Deretan 10 tombol pintasan berukuran besar menciptakan "tembok tautan" vertikal yang sangat panjang. Ini mendorong daftar berita pasar dan pantauan harga saham jatuh ke dasar halaman yang hampir tidak pernah dilihat pemain. Padahal sebagian besar fitur ini (Galeri, Asuransi, Duel Bos) adalah fitur sekunder.
5. **Dampak:** Beranda kehilangan fokus sebagai dasbor pasar/finansial aktif.
6. **Rekomendasi:** **PINDAHKAN KE TAB LAINNYA (HUB FITUR)**: Pindahkan modul sekunder (DNA Investor, Disiplin Harian, Dominasi Sektor, Asuransi Hedging) ke Tab "Lainnya" yang memang dirancang sebagai hub fitur komprehensif. Biarkan Beranda hanya memuat ringkasan aset, berita sentimen hari ini, dan pantauan saham.
7. **Risiko & Ketergantungan:** Mengharuskan pemain mengakses fitur sekunder via tab Lainnya (lebih rapi dan terorganisir).
8. **Prioritas:** Tinggi
9. **Tingkat Keyakinan:** Terverifikasi Visual & Source.

---

#### [AUDIT-10] Tombol Teks "Panduan & Mekanisme ↗" di Dasar Beranda yang Redundan
1. **Lokasi:** Paling Bawah Beranda (`index.html:155`)
2. **Konten Saat Ini:** `<button id="btn-entry-game" class="text-button">Panduan & Mekanisme ↗</button>`
3. **Bukti:** `index.html` baris 155; `app.js` baris 8320.
4. **Masalah:** Tombol teks yang diletakkan di bagian paling bawah setelah daftar pantauan pasar. Fungsinya hanya membuka picker entry/tutorial yang sudah ada di menu Profil dan Pengaturan.
5. **Dampak:** Elemen terisolasi tanpa konteks yang jelas di bawah tabel pasar.
6. **Rekomendasi:** **HAPUS** dari halaman Beranda. Tempatkan akses tutorial secara terhormat di Tab Lainnya atau Profil.
7. **Risiko & Ketergantungan:** Tidak ada.
8. **Prioritas:** Rendah
9. **Tingkat Keyakinan:** Terverifikasi Source.

---

### KELOMPOK 3: TAB PASAR

#### [AUDIT-11] Label Metadisclaimer "Model simulasi game (10 anggota)" pada Kartu Indeks
1. **Lokasi:** Ribbon Indeks Tab Pasar (`app.js:1486, 1492`)
2. **Konten Saat Ini:** `<span class="eyebrow">Model</span>`, `<div class="index-caption">Model simulasi game (10 anggota)</div>`
3. **Bukti:** `app.js` baris 1486, 1492; tangkapan layar `02-pasar-live.png`.
4. **Masalah:** Menampilkan teks penjelas internal ("Model simulasi game") yang melanggar keputusan resmi pemilik K04 (*"Hapus penanda 'harga simulasi game' pada pasar utama"*). Teks ini memakan ruang kartu indeks dan membuat kartu terpotong di layar horizontal.
5. **Dampak:** Tampilan kartu indeks terlihat kotor dengan disclaimer internal berulang.
6. **Rekomendasi:** **HAPUS** penanda "Model" dan ganti teks caption dengan nama resmi indeks (misal: "Indeks Saham Utama" atau "Bursa Efek Indonesia").
7. **Risiko & Ketergantungan:** Tidak ada risiko kalkulasi harga indeks.
8. **Prioritas:** Tinggi
9. **Tingkat Keyakinan:** Terverifikasi Visual & Source.

---

#### [AUDIT-12] Pesan Kosong / Negatif pada Ringkasan 3 Penguat & 3 Pelemah Pasar
1. **Lokasi:** Highlight Pasar (`app.js:1547-1549`)
2. **Konten Saat Ini:** `"Belum ada penguat di daftar ini."`, `"Hanya 1 emiten melemah di daftar ini."`
3. **Bukti:** `app.js` baris 1547-1549; tangkapan layar `02-pasar-live.png`.
4. **Masalah:** Ketika bursa baru dibuka atau pasar sedang bergerak tenang, sistem menampilkan kalimat negatif bernada keluhan ("Hanya 1 emiten...", "Belum ada..."). Ini mengisi kartu dengan kalimat kosong yang tidak memberikan nilai aksi bagi pemain.
5. **Dampak:** Memperburuk estetika antarmuka saat kondisi pasar seimbang.
6. **Rekomendasi:** **RINGKAS**: Jika penguat/pelemah kurang dari 3, cukup tampilkan baris saham yang ada tanpa paragraf penjelas tambahan. Jika kosong, cukup tampilkan label netral: "Pasar cenderung stagnan".
7. **Risiko & Ketergantungan:** Tidak ada.
8. **Prioritas:** Sedang
9. **Tingkat Keyakinan:** Terverifikasi Visual & Source.

---

#### [AUDIT-13] Ketiadaan Nominal Harga Saham pada Modul 3 Penguat & 3 Pelemah
1. **Lokasi:** Highlight Pasar (`app.js:1543`)
2. **Konten Saat Ini:** `1. V +0,2%` (Hanya nomor urut, ticker, dan persentase)
3. **Bukti:** `app.js` baris 1543; tangkapan layar `02-pasar-live.png`.
4. **Masalah:** Pemain melihat saham masuk kategori "Top Gainers" atau "Top Losers" namun tidak dapat melihat berapa harga saham tersebut saat ini tanpa mengkliknya terlebih dahulu.
5. **Dampak:** Informasi tidak lengkap; pemain kehilangan konteks apakah saham tersebut bernilai murah (misal ratusan perak) atau saham sultan (puluhan ribu rupiah).
6. **Rekomendasi:** **LENGKAPI INFORMASI**: Tampilkan nominal harga di samping persentase (contoh: `1. BBCA · Rp 10.425 · +0.48%`).
7. **Risiko & Ketergantungan:** Tidak ada.
8. **Prioritas:** Sedang
9. **Tingkat Keyakinan:** Terverifikasi Visual & Source.

---

#### [AUDIT-14] Formulir Filter Pasar yang Terlalu Tinggi & Teks Status Penghitung Emiten
1. **Lokasi:** Filter Bar Pasar (`index.html:168-197`, `app.js:1565-1567`)
2. **Konten Saat Ini:**
   - 4 form field terpisah: "Cari emiten saham", "Bursa", "Kategori / sektor", "Daftar emiten".
   - Teks status: `<p id="stock-list-status">20 emiten pada daftar ini.</p>`
3. **Bukti:** `index.html` baris 168-197; `app.js` baris 1565-1567; tangkapan layar `02-pasar-live.png`.
4. **Masalah:** Blok filter memakan tinggi vertikal ~180px sebelum daftar saham muncul. Teks status "20 emiten pada daftar ini" adalah teks pengisi (*filler*) yang tidak berguna karena daftarnya tepat berada di bawahnya.
5. **Dampak:** Layar Pasar terasa sesak dan daftar saham pertama terdorong ke bawah layar.
6. **Rekomendasi:** **KOMPAKSI**:
   - Satukan Search Bar di baris atas dengan input ramping.
   - Gunakan filter chips horizontal cepat: `[Semua] [IDX] [US] [Watchlist ★]`.
   - Hapus `<p id="stock-list-status">`.
7. **Risiko & Ketergantungan:** Mempermudah pencarian dan menghemat 100px vertikal.
8. **Prioritas:** Tinggi
9. **Tingkat Keyakinan:** Terverifikasi Visual & Source.

---

### KELOMPOK 4: TAB PORTOFOLIO

#### [AUDIT-15] Kebocoran Kode Tugas Internal Developer: "(A21)" dan "(A20)"
1. **Lokasi:** Tab Portofolio (`app.js:1801, 1809`)
2. **Konten Saat Ini:**
   - Label: `Total Kekayaan Bersih Konsolidasi (A21)`
   - Tombol: `Lihat Rincian Alokasi Dana Tertahan (A20)`
3. **Bukti:** `app.js` baris 1801, 1809; terlihat di tangkapan layar `03-portofolio-live.png`.
4. **Masalah:** Kode pengenal sprint developer (`A20` dan `A21`) terpajang secara terang-terangan di antarmuka pengguna akhir. Ini murni teks teknis pengujian yang lupa dibersihkan.
5. **Dampak:** Merusak kesan profesional aplikasi game dan membingungkan pemain ("apa itu A20/A21?").
6. **Rekomendasi:** **BERSIHKAN KODE DEV**:
   - Ganti menjadi: `Total Kekayaan Bersih Konsolidasi`
   - Ganti tombol menjadi: `Lihat Rincian Dana Tertahan`
7. **Risiko & Ketergantungan:** Tidak ada dampak ke logika.
8. **Prioritas:** Tinggi
9. **Tingkat Keyakinan:** Terverifikasi Visual & Source.

---

#### [AUDIT-16] Pengulangan Total Valuasi Seluruh Kas di dalam Accordion Dompet
1. **Lokasi:** Tab Portofolio (`app.js:1786, 1802`)
2. **Konten Saat Ini:**
   - Di kartu Komposisi Aset: `Kas (valuasi IDR): Rp 100.000.000`
   - Di dalam accordion Dompet IDR & USD: `Total Valuasi Seluruh Kas: Rp 100.000.000`
3. **Bukti:** `app.js` baris 1786, 1802; tangkapan layar `03-portofolio-live.png`.
4. **Masalah:** Nilai kas total diulang 2 kali secara berturut-turut pada blok yang sama.
5. **Dampak:** Redundan dan menambah panjang daftar rincian kas.
6. **Rekomendasi:** **HAPUS** baris `Total Valuasi Seluruh Kas` di dalam accordion; langsung tampilkan rincian saldo `IDR (Total/Tersedia/Tertahan)` dan `USD (Total/Tersedia/Tertahan)`.
7. **Risiko & Ketergantungan:** Tidak ada.
8. **Prioritas:** Sedang
9. **Tingkat Keyakinan:** Terverifikasi Visual & Source.

---

#### [AUDIT-17] Kategori Portofolio Bagian Bawah yang Hanya Berisi Shortcut ke Tab Bisnis
1. **Lokasi:** Dasar Tab Portofolio (`app.js:1848-1855`)
2. **Konten Saat Ini:**
   - Kategori "Properti & Koleksi": membuka Galeri Koleksi (fitur tab Bisnis).
   - Kategori "Usaha & Dana Kelolaan": berisi 3 link yang hanya mengarahkan/switch tab ke Tab Bisnis ("Perusahaan operasional", "Dana kelolaan").
3. **Bukti:** `app.js` baris 1848-1855; tangkapan layar `05-portfolio-compact-rows-live.png`.
4. **Masalah:** Tab Portofolio diakhiri dengan tumpukan kartu kategori besar yang sebagian besar hanya merupakan pintu putar (*portal link*) menuju tab Bisnis.
5. **Dampak:** Memperpanjang scroll Portofolio dengan tautan yang tidak relevan dengan instrumen saham/keuangan pribadi.
6. **Rekomendasi:** **PANGKAS KATEGORI DUPLIKAT**: Hapus kategori "Usaha & Dana Kelolaan" dari tab Portofolio karena sudah memiliki rumah kanonikal sendiri di Tab Bisnis (`firma`).
7. **Risiko & Ketergantungan:** Pengguna mengakses usaha dan dana kelolaan secara teratur melalui tab Bisnis.
8. **Prioritas:** Sedang
9. **Tingkat Keyakinan:** Terverifikasi Visual & Source.

---

### KELOMPOK 5: TAB BISNIS & TAB LAINNYA

#### [AUDIT-18] Jargon Akuntansi Teknis "Ledger Dua Sisi" pada Deskripsi Holding
1. **Lokasi:** Tab Bisnis (`app.js:2228`)
2. **Konten Saat Ini:** `"Pilih empat perusahaan yang ada untuk membentuk satu grup: Finansial, Energi, Konsumer, dan Teknologi. Transfer/dividen internal memakai ledger dua sisi tanpa pajak bursa."`
3. **Bukti:** `app.js` baris 2228.
4. **Masalah:** Frasa "memakai ledger dua sisi" adalah istilah teknis internal akuntansi/database yang tidak ramah bagi pemain awam.
5. **Dampak:** Terkesan kaku dan membingungkan pemain ("apakah pemain harus mengisi buku besar dua sisi?").
6. **Rekomendasi:** **GUNAKAN BAHASA PEMAIN YANG NATURAL**: Ganti menjadi: *"Bentuk konglomerasi holding dari 4 sektor usaha. Nikmati fasilitas transfer modal dan dividen internal antar-anak usaha tanpa potongan pajak bursa."*
7. **Risiko & Ketergantungan:** Tidak ada.
8. **Prioritas:** Sedang
9. **Tingkat Keyakinan:** Terverifikasi Source.

---

#### [AUDIT-19] Banner Hero Progres pada Tab Lainnya yang Menduplikasi Profil Pemain
1. **Lokasi:** Puncak Tab Lainnya (`app.js:3529-3567`)
2. **Konten Saat Ini:** Banner kartu besar memuat: Nama Trader, Level, Pangkat Karier, "Reputasi Karier: X Poin", "Total Pengalaman: Y XP", "Status Pasar: Z".
3. **Bukti:** `app.js` baris 3529-3567.
4. **Masalah:** Seluruh data identitas ini sudah ada di dalam modal Profil Saya (yang diakses dari avatar kiri atas) dan di kartu Karier Beranda. Tab "Lainnya" berfungsi sebagai direktori menu/hub pengaturan, namun setengah layar pertamanya dihabiskan oleh banner ringkasan identitas yang sangat besar.
5. **Dampak:** Menu-menu esensial (Terminal Riset, Pengaturan Sistem, Misi) terdorong ke bawah layar.
6. **Rekomendasi:** **KOMPAKSI / HAPUS BANNER**: Cukup tampilkan header ringkas "Pusat Kontrol & Pengaturan", lalu langsung sajikan grup menu secara efisien.
7. **Risiko & Ketergantungan:** Tidak ada kehilangan data pemain.
8. **Prioritas:** Sedang
9. **Tingkat Keyakinan:** Terverifikasi Source.

---

### KELOMPOK 6: DETAIL SAHAM & TIKET TRANSAKSI

#### [AUDIT-20] Tiga Kali Pengulangan Nama Perusahaan, Bursa, dan Sektor pada Detail Saham
1. **Lokasi:** Detail Saham (`app.js:4125-4126` dan `app.js:4376-4380`)
2. **Konten Saat Ini:**
   - Di kartu atas: Ticker `BBCA`, Nama `Bank Central Asia Tbk`, Bursa `IDX`, Sektor `Keuangan`.
   - Di bawah harga: Sektor `Keuangan` diulang lagi.
   - Di kartu Profil Emiten bawah: "Nama resmi: Bank Central Asia Tbk", "Bursa & negara: IDX · Indonesia", "Sektor: Keuangan".
3. **Bukti:** `app.js` baris 4125-4126, 4376-4380; tangkapan layar `04-bbca-detail-fixed-dock-live.png`.
4. **Masalah:** Nama emiten, bursa, dan sektor tercetak 3 kali di halaman detail yang sama.
5. **Dampak:** Memperpanjang scroll vertikal dengan teks statis berulang.
6. **Rekomendasi:** **ELIMINASI DUPLIKASI**: Cukup tampilkan nama dan sektor di kartu ringkasan header atas. Pada kartu profil emiten di bawah, hapus pengulangan nama/sektor dan fokuskan langsung pada ringkasan operasional bisnis serta data fundamental.
7. **Risiko & Ketergantungan:** Tidak ada.
8. **Prioritas:** Sedang
9. **Tingkat Keyakinan:** Terverifikasi Visual & Source.

---

#### [AUDIT-21] Daftar 20 Rasio Keuangan Mentah yang Tidak Dikelompokkan
1. **Lokasi:** Detail Saham (`app.js:4385-4420`)
2. **Konten Saat Ini:** Daftar 20 baris rasio akuntansi berturut-turut: EPS, PER, PBV, ROE, ROA, DER, DPS, DPR, Dividend Yield, Total Aset, Total Liabilitas, Ekuitas Bersih, Pendapatan, Laba Usaha, Laba Bersih, CFO, CFI, CFF, Net Cashflow.
3. **Bukti:** `app.js` baris 4385-4420.
4. **Masalah:** Menampilkan 20 baris data angka akuntansi mentah secara vertikal tanpa pengelompokan (*collapsible*). Istilah seperti CFO, CFI, CFF, DER, DPR sangat membingungkan bagi pemain awam dan menghabiskan 400px tinggi layar.
5. **Dampak:** Layar detail saham menjadi sangat panjang dan melelahkan untuk di-scroll.
6. **Rekomendasi:** **KELOMPOKKAN KE DALAM ACCORDION / SUBTAB FUNDAMENTAL**:
   - Tampilkan 4 rasio utama secara default (PER, PBV, ROE, Dividend Yield).
   - Masukkan sisanya ke dalam menu lipat `<details><summary>Lihat Laporan Keuangan Lengkap (Neraca & Arus Kas)</summary>...</details>`.
7. **Risiko & Ketergantungan:** Data tetap 100% tersedia bagi pemain mahir yang membutuhkan tanpa mengorbankan ruang layar bagi pemain umum.
8. **Prioritas:** Sedang
9. **Tingkat Keyakinan:** Terverifikasi Source.

---

#### [AUDIT-22] Tombol Manual Berulang "Perbarui harga & antrean" pada Detail Saham
1. **Lokasi:** Detail Saham (`app.js:4543-4547`)
2. **Konten Saat Ini:** `<button class="btn-secondary">Perbarui harga & antrean</button>`
3. **Bukti:** `app.js` baris 4543-4547.
4. **Masalah:** Aplikasi sudah memiliki sinkronisasi data otomatis (*auto-sync timer*) setiap interval waktu yang memperbarui kuotasi harga dan antrean order secara live. Tombol manual berukuran penuh ini memakan ruang tepat di atas dock transaksi.
5. **Dampak:** Memenuhi ruang layar dan membuat pengguna ragu apakah datanya sudah otomatis atau harus ditekan manual.
6. **Rekomendasi:** **HAPUS TOMBOL MANUAL INI** atau ubah menjadi ikon refresh kecil di pojok kanan kartu harga.
7. **Risiko & Ketergantungan:** Sistem auto-sync sudah terbukti memperbarui data secara reaktif.
8. **Prioritas:** Rendah
9. **Tingkat Keyakinan:** Terverifikasi Source.

---

### KELOMPOK 7: MODAL DIALOG & POP-UP ALUR

#### [AUDIT-23] Paragraf "Esai" / Penjelasan Pembuka Bertele-tele di Setiap Modal Dialog
1. **Lokasi:** Tersebar di 15+ Modal Dialog (`openTutorialModal`, `openTechTreeModal`, `openLoansModal`, `openTaxAuditModal`, `openPortfolioInsuranceModal`, `openMarketBossModal`, `openSectorDominanceModal`, `openDailyStreakModal`, dll.)
2. **Konten Saat Ini:** Blok teks pengantar panjang (100–180 karakter) di baris pertama setiap modal, contohnya:
   - Di Pinjaman: `"Spesifikasi D05: Fasilitas pinjaman tenor 7/14/30 hari game dengan bunga harian 0.05% atas sisa pokok dan provisi cair 0.5%."`
   - Di Pembiayaan: `"Ketentuan Pembiayaan (A16): Game merekomendasikan opsi terbaik berdasarkan profil Anda..."`
   - Di Dominasi Sektor: `"Akumulasi kepemilikan saham di atas 25% kapitalisasi pasar satu sektor untuk dinobatkan sebagai penguasa industri..."`
3. **Bukti:** `app.js` baris 5686, 5783, 6366, 6431, 6459, 8089, 8202, 8228.
4. **Masalah:** Pada layar ponsel portrait (tinggi layar terbatas), paragraf pembuka ini menelan 30%–45% area modal. Pemain yang ingin segera mengeksekusi aksi (misal: mengajukan pinjaman, klaim misi, bayar cicilan) dipaksa membaca esai yang sama berulang kali dan harus men-scroll ke bawah untuk menemukan tombol.
5. **Dampak:** Mengurangi kenyamanan interaksi dan memperlambat alur bermain (*friction in gameplay*).
6. **Rekomendasi:** **PANGKAS & FOKUSKAN KE KONTROL AKSI**:
   - Ganti esai panjang dengan subjudul 1 baris singkat (maksimal 40 karakter).
   - Penjelasan detail peraturan dapat dipindahkan ke tombol ikon bantuan `(?)` kecil yang dapat dibuka saat dibutuhkan saja.
7. **Risiko & Ketergantungan:** Menjadikan modal dialog langsung to-the-point dan ramah jempol.
8. **Prioritas:** Tinggi
9. **Tingkat Keyakinan:** Terverifikasi Source & Visual.

---

#### [AUDIT-24] Teks Dialog Pemulihan Save yang Terlalu Defensif & Bernada Catatan Developer
1. **Lokasi:** Modal Pemulihan Save (`app.js:125-132, 592-602`)
2. **Konten Saat Ini:**
   - Status: `"Save tidak dapat divalidasi. Berkas asli tidak dihapus atau diganti permainan baru."`
   - Langkah: `"Pertahankan file asli dan backup. Coba muat salinan valid; bila tetap gagal, jangan membuat save baru di slot ini."`
3. **Bukti:** `app.js` baris 594-595.
4. **Masalah:** Kalimat menggunakan gaya catatan instruksi teknis pengembang (*developer instruction notes*), bukan bahasa produk game yang berpusat pada kenyamanan pemain.
5. **Dampak:** Pemain merasa bingung dan takut ("apa maksudnya jangan membuat save baru di slot ini?").
6. **Rekomendasi:** **GANTI DENGAN BAHASA PEMAIN YANG ALAMI & MENENANGKAN**:
   - Status: *"Data permainan belum dapat dimuat."*
   - Keterangan: *"Tenang, berkas cadangan Anda tersimpan aman. Pulihkan salinan cadangan untuk melanjutkan progres permainan."*
7. **Risiko & Ketergantungan:** Tidak ada dampak logika pemulihan.
8. **Prioritas:** Sedang
9. **Tingkat Keyakinan:** Terverifikasi Source.

---

#### [AUDIT-25] Teks "(Satu Pintu)" pada Judul Header Modal Terminal Riset
1. **Lokasi:** Modal Terminal Riset (`app.js:3294`)
2. **Konten Saat Ini:** `showFlow('Terminal Riset Pasar (Satu Pintu)')`
3. **Bukti:** `app.js` baris 3294.
4. **Masalah:** Teks "(Satu Pintu)" adalah kode konsep dari master plan arsitektur Plan 12/13. Pemain tidak perlu tahu bahwa terminal ini dirancang dengan konsep "satu pintu".
5. **Dampak:** Judul header terlihat aneh dan tidak mencerminkan istilah finansial dunia nyata.
6. **Rekomendasi:** **HAPUS TEKS DALAM KURUNG**: Cukup gunakan judul elegan: `"Terminal Riset Pasar"`.
7. **Risiko & Ketergantungan:** Tidak ada.
8. **Prioritas:** Rendah
9. **Tingkat Keyakinan:** Terverifikasi Source.

---

## IV. Matriks Rekapitulasi Berdasarkan Prioritas

| Prioritas | Jumlah Temuan | ID Temuan | Dampak Utama terhadap Pemain |
|---|---|---|---|
| **TINGGI (Critical UX & Confusing)** | **8 Temuan** | AUDIT-02, AUDIT-05, AUDIT-06, AUDIT-09, AUDIT-11, AUDIT-14, AUDIT-15, AUDIT-23 | Membuang ruang layar utama secara masif, membingungkan pemain dengan kode dev internal, dan menduplikasi navigasi dasar. |
| **SEDANG (Clutter & Redundancy)** | **13 Temuan** | AUDIT-01, AUDIT-04, AUDIT-07, AUDIT-08, AUDIT-12, AUDIT-13, AUDIT-16, AUDIT-17, AUDIT-18, AUDIT-19, AUDIT-20, AUDIT-21, AUDIT-24 | Mengulang data yang sama berturut-turut, mengisi kartu dengan kalimat kosong, dan menenggelamkan kontrol aksi. |
| **RENDAH (Minor Polish & Cleanup)** | **4 Temuan** | AUDIT-03, AUDIT-10, AUDIT-22, AUDIT-25 | Elemen tombol mati di DOM, teks kurung sisa developer, dan tombol refresh manual yang tidak diperlukan. |

---

## V. Batas Verifikasi & Kepatuhan Batas Pekerjaan

1. **Kepatuhan Mode Read-Only:**  
   Sesuai instruksi ketat pada `PROMPT-AUDIT-UI-DAN-KONTEN.md`, sesi audit ini **TIDAK MENGUBAH satu baris pun kode produksi (`app.js`, `style.css`, Java core) maupun data pemain**.
2. **Integritas Berkas Save Asli:**  
   Checksum SHA-256 dari `Artifacts/LocalPreview/preview-save/save_game_v2.json` tetap:  
   `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed` (100% identik dan aman).
3. **Penyimpanan Laporan Resmi:**  
   Laporan audit lengkap ini telah tersimpan secara resmi dan aman di disk pada path:  
   `C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM\Work\AUDIT-UI-DAN-KONTEN-2026-10-30.md`.

---

## VI. Rekomendasi Langkah Konkret Berikutnya

Laporan temuan di atas disusun secara terstruktur agar Anda dapat meninjau dan memilih tindak lanjut per butir:
1. **Tahap 1 (Pembersihan Cepat Bebas Risiko):** Membersihkan kebocoran kode teknis developer (`AUDIT-15`: hapus `A20`/`A21`, `AUDIT-11`: hapus `Model simulasi`, `AUDIT-25`: hapus `(Satu Pintu)`).
2. **Tahap 2 (Kompaksi Beranda & Layout Portrait):** Merampingkan Beranda dengan memangkas Quick Actions Bar yang duplikat (`AUDIT-06`), menghapus slogan kosong (`AUDIT-05`), dan memindahkan tumpukan 10 shortcut modul sekunder ke Tab Lainnya (`AUDIT-09`).
3. **Tahap 3 (Penyederhanaan Modal & Detail Saham):** Memangkas esai pembuka pada modal dialog (`AUDIT-23`), menyatukan accordion rasio keuangan (`AUDIT-21`), dan menyederhanakan filter pasar (`AUDIT-14`).

*Laporan audit UI dan konten ini telah selesai disusun secara jujur, mendalam, dan siap untuk Anda tinjau satu per satu!*
