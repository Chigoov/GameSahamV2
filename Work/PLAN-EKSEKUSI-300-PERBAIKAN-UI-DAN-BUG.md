# PLAN EKSEKUSI LENGKAP: 300 PERBAIKAN UI, PEMUTAKHIRAN KALIMAT & RESOLUSI BUG SISTEM
## Dokumen Rencana Kerja Komprehensif Berbasis Bukti (Unbundled Execution Plan)
**Target Proyek:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`  
**Arsitektur Runtime:** Android Native Java (API 36, Java 21) + WebView UI (HTML5, CSS3, ES6)  
**Tanggal Terbit Dokumen:** 31 Oktober 2026  
**Total Scope Terpetakan:** Tepat 300 Item Nyata  
**Prinsip Kerja:** *Zero Residual* (Tuntas Tanpa Sisa), Preservasi Save Game Pemain, dan *Verification Before Completion*.  

---

## I. STRUKTUR 6 GELOMBANG EKSEKUSI (WAVES)

Seluruh 300 temuan dibagi secara deterministik ke dalam 6 gelombang kerja:

| Gelombang | Rentang Item | Fokus Utama Area Kerja | Estimasi File Terdampak |
|---|:---:|---|---|
| **Gelombang 1** | `AUDIT-001 s/d AUDIT-050` | **Header Shell, Navigasi Utama, Jam Bursa & Tab Beranda** (Pembersihan duplikasi kas, penghapusan slogan kosong, reposisi dock JUAL/BELI di atas tab, fix media query). | `style.css`, `index.html`, `app.js` |
| **Gelombang 2** | `AUDIT-051 s/d AUDIT-100` | **Tab Pasar, Detail Saham, Grafik 3x3, Tiket Order & Radar Riset** (Fix limit hardcoded 10250, debounce input pencarian, tombol MAX berjarak aman, format titik ribuan). | `index.html`, `style.css`, `app.js` |
| **Gelombang 3** | `AUDIT-101 s/d AUDIT-150` | **Tab Portofolio, Mutasi Kas, Performa/Lag & e-IPO Stockbit** (Banner roket e-IPO, filter 3 status, timeline 5-tahap, eliminasi serialisasi ganda snapshot). | `app.js`, `style.css`, `TradingFacade.java` |
| **Gelombang 4** | `AUDIT-151 s/d AUDIT-200` | **Tab Bisnis Operasional, Holding, SDM & Modal Dialog** (Moral pegawai, jam kerja, kapasitas output, pencegahan crash slider 120%, dialog humanis). | `app.js`, `style.css`, `OperatingCompany.java` |
| **Gelombang 5** | `AUDIT-201 s/d AUDIT-250` | **Akselerasi Kecepatan Harga Live, Pasar Offline & Matching Engine** (Tick per 1 detik, drift pasar offline 20 emiten, Stop Loss koridor ARB, RUPS abstain). | `TradingFacade.java`, `NpcAgent.java`, `MarketEngine.java` |
| **Gelombang 6** | `AUDIT-251 s/d AUDIT-300` | **Katalog Multi-Aset, Donut Sektor, Aksesibilitas & Pengaturan** (Obligasi FR0091, Donut Chart proporsional, High Contrast border, slider volume audio). | `app.js`, `style.css`, `index.html` |

---

## II. CHECKLIST & PEMETAAN DETAIL 300 TASK EKSEKUSI

### GELOMBANG 1: HEADER SHELL, NAVIGASI, JAM BURSA & BERANDA (001 - 050)

| ID Task | Prioritas | Kategori | Lokasi Kode | Ringkasan Sasaran Perbaikan |
|---|:---:|---|---|---|
| `[AUDIT-01]` | **TINGGI** | `KESALAHAN UI / LAYOUT` | `style.css:2356-2359` | Aturan Media Query Menyembunyikan Judul Layar di Seluruh HP Portrait |
| `[AUDIT-02]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `index.html:16` | Teks Eyebrow Generik 'GLOBAL TRADER' Mempersempit Baris Header Shell |
| `[AUDIT-03]` | **TINGGI** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `index.html:17, 43-44` | Pengulangan Angka Kas Tersedia 3 Kali Berturut-turut dalam Jarak 150px Vertikal |
| `[AUDIT-04]` | **RENDAH** | `BUG` | `index.html:21` | Elemen Tombol Mati pada Context Strip dengan Style Tersembunyi Paksa |
| `[AUDIT-05]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `index.html:26-28` | Pita Banner Lingkungan Internal 'QA HOST JAVA' Mendorong Layout ke Bawah |
| `[AUDIT-06]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `index.html:20, app.js:621` | Teks Status Jam Bursa Menampilkan Placeholder Kaku 'Memuat sesi' |
| `[AUDIT-07]` | **TINGGI** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `index.html:35` | Slogan Motivasi Kosong 'Bangun langkahmu.' Membuang Ruang Layar Atas |
| `[AUDIT-08]` | **TINGGI** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `index.html:50-72` | Bar Lingkaran Aksi Cepat Beranda 80% Menduplikasi Tombol Tab Bawah |
| `[AUDIT-09]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `index.html:74-84` | Pengulangan Tiga Kali Angka Nominal pada Kartu Sponsor Ventura Beranda |
| `[AUDIT-10]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `index.html:85-97` | Akses Misi & Pencapaian Ganda (Kartu Karier vs Kotak Shortcut Beranda) |
| `[AUDIT-11]` | **TINGGI** | `KESALAHAN UI / LAYOUT` | `index.html:92-132` | Tumpukan 5 Baris 'Quick Grid' (10 Shortcut) Menenggelamkan Warta Pasar |
| `[AUDIT-12]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `index.html:141, app.js:1450` | Tautan Teks 'Panduan & Mekanisme ↗' Memiliki Target Sentuh Terlalu Tipis |
| `[AUDIT-13]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `index.html:42, app.js:1220` | Keterangan Hero Caption 'Aset portofolio, kas, bisnis & koleksi · IDR' Berulang |
| `[AUDIT-14]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `index.html:47, app.js:1235` | Label 'Kewajiban / Utang' Menampilkan Teks 'Memuat…' Abu-abu Samar Saat Nihil |
| `[AUDIT-15]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `index.html:73` | Header Bagian 'Langkah berikutnya: HARI INI' Menggunakan Frasa Kaku |
| `[AUDIT-16]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `index.html:135, style.css:1620` | Tombol Filter Berita Beranda 'Semua / Kabar Baik / Kabar Buruk' Terlalu Rapat |
| `[AUDIT-17]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `index.html:168, app.js:2840` | Teks Metadisclaimer 'Model simulasi game' pada Kartu Indeks IHSG & S&P 500 |
| `[AUDIT-18]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:3120` | Modul Top Movers Menampilkan Kalimat Negatif Saat Pasar Bergerak Tenang |
| `[AUDIT-19]` | **SEDANG** | `BUG` | `app.js:3150` | Ketiadaan Nominal Harga Terakhir pada Kartu 3 Saham Penguat & 3 Pelemah |
| `[AUDIT-20]` | **TINGGI** | `KESALAHAN UI / LAYOUT` | `index.html:19-25, style.css:290` | Badge Makroekonomi (#macro-badge) Terbentur Tombol Cepat di Layar Sempit |
| `[AUDIT-21]` | **RENDAH** | `KESALAHAN UI / LAYOUT` | `index.html:15, style.css:265` | Avatar Profil di Kiri Atas Header Tidak Memiliki Indikator Level Karier |
| `[AUDIT-22]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `index.html:24` | Tag Status Sesi Tersembunyi Menuliskan Istilah Redundan 'Pasar Internal Game' |
| `[AUDIT-23]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:250, style.css:2389` | Bottom Navigation Bar Tidak Mengunci Status Visual Saat Membuka Modal Penuh |
| `[AUDIT-24]` | **TINGGI** | `BUG` | `app.js:340, style.css:2370` | Kebocoran Scroll Latar Belakang (.app-scroll) Saat Modal Detail Terbuka |
| `[AUDIT-25]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `index.html:143` | Tombol 'Semua →' pada Pantauan Pasar Beranda Memiliki Label Ambigu |
| `[AUDIT-26]` | **SEDANG** | `BUG` | `index.html:181-185` | Formulir Filter Pasar Dropdown 4-Baris Bertumpuk Masih Mengendap di DOM |
| `[AUDIT-27]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `index.html:153` | Eyebrow Kapital 'BURSA & KEPEMILIKAN' Membuang Ruang Layar Pasar |
| `[AUDIT-28]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `index.html:156` | Pill Badge 'Bursa Aktif' di Header Pasar Redundan dengan Status Dot Jam |
| `[AUDIT-29]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:2910` | Label Sektor Kosong atau Strip '-' pada Emiten Tanpa Kategori Terdaftar |
| `[AUDIT-30]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:2940, style.css:1410` | Avatar Inisial 2-Huruf Memboroskan Kolom Tanpa Informasi Tambahan |
| `[AUDIT-31]` | **TINGGI** | `BUG` | `app.js:1554` | Bug Regex Naif pada Pelacak Fluktuasi Harga Saham (Pemisah Titik Rupiah) |
| `[AUDIT-32]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:1565` | Teks Penjelas Persentase Saham Menambahkan Akhiran Meta '· perubahan game' |
| `[AUDIT-33]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:4420-4450` | Pengulangan Nama Perusahaan, Bursa & Sektor Tiga Kali di Detail Saham |
| `[AUDIT-34]` | **TINGGI** | `KESALAHAN UI / LAYOUT` | `app.js:4620-4710` | Daftar 20 Baris Rasio Akuntansi Mentah Tanpa Pengelompokan Akordion |
| `[AUDIT-35]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:4750` | Tombol Manual 'Perbarui harga & antrean' Redundan dengan Engine Realtime |
| `[AUDIT-36]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:4780` | Pemberitahuan Suspensi Menggunakan Kalimat Birokrasi Bertele-tele |
| `[AUDIT-37]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:2960` | Penjelasan Satuan '100 saham/unit (1 lot)' Diulang pada Setiap Kartu Emiten |
| `[AUDIT-38]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:4590` | Kartu Posisi Menampilkan Blok Abu-abu Besar 'Belum memiliki posisi' |
| `[AUDIT-39]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:4810` | Label Dampak Sentimen Berita Emiten Terlalu Panjang dan Bertingkat |
| `[AUDIT-40]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `index.html:295` | Teks Bantuan Tiket Order 'Biaya dan perkiraan kas...' Memenuhi Formulir |
| `[AUDIT-41]` | **TINGGI** | `BUG` | `index.html:293` | Input Harga Limit Memiliki Nilai Default Hardcoded 10250 di HTML |
| `[AUDIT-42]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:7696` | Tiket Order Membocorkan ID Teknis Akun '(ACC-PERS-01)' ke Antarmuka |
| `[AUDIT-43]` | **TINGGI** | `KESALAHAN UI / LAYOUT` | `index.html:285-290, style.css:1720` | Tombol Persentase Lot Modal Terlalu Rapat dan Rawan Salah Sentuh MAX |
| `[AUDIT-44]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:7780` | Lembar Review Order Mengulang Label 'Mode: Pasar Internal Game' |
| `[AUDIT-45]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:7810` | Keterangan Waktu Berlaku Menampilkan Format Teknis Jam Detik Kaku |
| `[AUDIT-46]` | **SEDANG** | `BUG` | `style.css:2000, 2180` | Sheet Tiket Order Tertimpa Toast Notifikasi Sistem Melayang |
| `[AUDIT-47]` | **TINGGI** | `KESALAHAN UI / LAYOUT` | `index.html:262` | Tombol Tutup Tiket Order '✕' Memiliki Target Sentuh Hanya 24px |
| `[AUDIT-48]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5051` | Judul Subtab Terminal Riset Menyisakan Konsep Dev '(Satu Pintu)' |
| `[AUDIT-49]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:4554-4561` | Strip Nilai Wajar Belum Memiliki Keterangan Edukatif untuk Pemula |
| `[AUDIT-50]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `index.html:175-180, app.js:2870` | Bilah Chip Filter Pasar Tidak Mempertahankan Scroll Saat Dipilih |

---

### GELOMBANG 2: TAB PASAR, DETAIL SAHAM, TIKET ORDER & RADAR (051 - 100)

| ID Task | Prioritas | Kategori | Lokasi Kode | Ringkasan Sasaran Perbaikan |
|---|:---:|---|---|---|
| `[AUDIT-51]` | **TINGGI** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:1894, 1910` | Kebocoran Kode Tiket Developer '(A21)' dan '(A20)' pada Header Portofolio |
| `[AUDIT-52]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:1930` | Pengulangan Angka Valuasi Total Kas di dalam Akordion Dompet |
| `[AUDIT-53]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:1960` | Shortcut Pintu Putar ke Tab Bisnis di Dasar Layar Portofolio |
| `[AUDIT-54]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:1905` | Label 'PRIBADI' dan 'DANA TERPISAH' Ditulis Ganda pada Header Portofolio |
| `[AUDIT-55]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `style.css:1540, app.js:1920` | Bar Komposisi Alokasi Aset Tidak Menampilkan Angka Persentase |
| `[AUDIT-56]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:1945` | Pengulangan Label '(valuasi IDR)' pada Setiap Baris Komposisi Portofolio |
| `[AUDIT-57]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `style.css:980` | Status Posisi 'Impas' Menggunakan Warna Teks Redup Samar (#64748B) |
| `[AUDIT-58]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:1980` | Ketiadaan Teks Bantuan 'Ketuk untuk jual/kelola' pada Posisi Saham |
| `[AUDIT-59]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5210` | Esai Pembuka Modal e-IPO Menjelaskan Teori Book Building Bertele-tele |
| `[AUDIT-60]` | **TINGGI** | `KESALAHAN UI / LAYOUT` | `app.js:5280` | Tombol Pembatalan e-IPO Belum Menyebutkan Nominal Dana yang Dilepas |
| `[AUDIT-61]` | **SEDANG** | `BUG` | `app.js:5014` | Format Rasio Pemecahan Saham (Stock Split) Merender Trailing Spasi Kosong |
| `[AUDIT-62]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5830` | Paragraf Ketentuan Cutoff Reksa Dana Terlalu Panjang dan Kaku |
| `[AUDIT-63]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5741` | Teks Pengantar Asuransi Hedging Menjelaskan Teori Crash Pasar Bertele-tele |
| `[AUDIT-64]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:5890` | Produk Deposito Belum Menampilkan Perbandingan Bunga Terhadap Siklus Makro |
| `[AUDIT-65]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:6420` | Riwayat Mutasi Kas Mencantumkan Prefix Teknis 'ORD-TRD-' yang Mengganggu |
| `[AUDIT-66]` | **SEDANG** | `BUG` | `app.js:5120` | Notifikasi Gagal Auto-Renewal Terminal Riset Tidak Menyertakan Opsi Tukar Valas |
| `[AUDIT-67]` | **SEDANG** | `BUG` | `app.js:5235` | Emiten yang Telah Resmi Listing Masih Muncul di Tab Penawaran Aktif e-IPO |
| `[AUDIT-68]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5035` | Teks Konfirmasi Klaim Dividen Menuliskan Penjelasan Teori Perpajakan Kaku |
| `[AUDIT-69]` | **RENDAH** | `KESALAHAN UI / LAYOUT` | `app.js:5850` | Nilai NAB Reksa Dana Menggunakan 4 Desimal Memperkacau Lebar Kolom |
| `[AUDIT-70]` | **RENDAH** | `KESALAHAN UI / LAYOUT` | `app.js:6450` | Penggunaan Emoji Campur Aduk pada Ikon Status Mutasi Dividen dan Kupon |
| `[AUDIT-71]` | **TINGGI** | `BUG` | `app.js:5702-5735` | Modal Replay Historis Masih Menampilkan Form Padahal Fitur Telah Dinonaktifkan |
| `[AUDIT-72]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `style.css:1120` | Warna Badge Fluktuasi Negatif pada Aset Kripto/Valas Menggunakan Warna Oranye |
| `[AUDIT-73]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:5920` | Tombol Tarik Deposito Dini Tidak Memiliki Penegasan Visual Peringatan Penalti |
| `[AUDIT-74]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:1950` | Label Saldo Kas Menuliskan Kalimat Berbelit-belit Sepanjang 65 Karakter |
| `[AUDIT-75]` | **TINGGI** | `KESALAHAN UI / LAYOUT` | `style.css:1460, app.js:1995` | Nilai Laba/Rugi Mengambang Ratusan Juta Terpotong Elipsis pada Baris Saham |
| `[AUDIT-76]` | **TINGGI** | `BUG` | `app.js:5952` | Modal Pinjaman Bank Masih Menampilkan Bunga Hardcoded 0.05% yang Tidak Sinkron |
| `[AUDIT-77]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:2018` | Jargon Akuntansi Kaku 'Ledger Dua Sisi' pada Deskripsi Pembentukan Holding |
| `[AUDIT-78]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5740` | Paragraf Rumus Matematika Menjejali Layar Modal Konsolidasi Kekayaan |
| `[AUDIT-79]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:2030` | Pesan Kosong Pendirian Usaha Mengulang Syarat Modal Setor Berulang Kali |
| `[AUDIT-80]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:2045` | Duplikasi Label 'Status operasional' dan Badge 'Operasional Normal' Bersebelahan |
| `[AUDIT-81]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:2055` | Penulisan 'CEO: Belum tersedia' dengan Teks Merah Memberi Kesan Error |
| `[AUDIT-82]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:6110` | Esai Pembuka Modal Kebijakan Kerja Perusahaan Menutupi Opsi Jam Kerja |
| `[AUDIT-83]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:6050` | Pengulangan Nama Holding pada Header dan Subjudul Modal Transfer Dana |
| `[AUDIT-84]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:7640` | Pemberitahuan Gagal Operasi Bisnis Membocorkan Istilah Developer 'Remediasi' |
| `[AUDIT-85]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `index.html:218-220` | Pill Subtab Layar Bisnis Tidak Menampilkan Jumlah Unit Aktif |
| `[AUDIT-86]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:6250` | Banner Hero di Tab Lainnya Menduplikasi Kartu Profil Shell Header |
| `[AUDIT-87]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:6280` | Badge Eyebrow Kapital Ganda 'PROFIL & PRESTISE' pada Setiap Kartu Menu |
| `[AUDIT-88]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:6310` | Keterangan Subteks Menu Riset Mengulang Kata 'Finansial' Tiga Kali |
| `[AUDIT-89]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:6330` | Deskripsi Menu Backup Menyajikan Esai SHA-256 yang Terlalu Panjang |
| `[AUDIT-90]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:2078` | Deskripsi Menu Mulai Baru Menggunakan Istilah Developer 'Reset Run' |
| `[AUDIT-91]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:6265` | Tag Status Sesi Game di Hero Banner Tab Lainnya Memakan Ruang Vertikal |
| `[AUDIT-92]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:6295-6340` | Ikon Menu Hub Menggunakan Karakter Emoji Campur Aduk |
| `[AUDIT-93]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:6360` | Teks Dialog Pemulihan Save Game Terlalu Defensif dan Bergaya Catatan Debug |
| `[AUDIT-94]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:6385` | Pesan di Modal Arketipe Investor Berisi Kalimat Defensif Programmer |
| `[AUDIT-95]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:6345` | Label Teknis 'Skema V2, UTF-8' Muncul pada Modal Pengaturan Save |
| `[AUDIT-96]` | **TINGGI** | `KESALAHAN UI / LAYOUT` | `app.js:820` | Toast Autosave 'Progres tersimpan' Muncul Terlalu Sering dan Mengganggu |
| `[AUDIT-97]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:6400` | Teks Konfirmasi Berulang 'Apakah Anda yakin ingin memulai Era Baru?' |
| `[AUDIT-98]` | **SEDANG** | `BUG` | `app.js:5140` | Tombol 'Hentikan perpanjangan otomatis' Muncul pada Paket Basic Gratis |
| `[AUDIT-99]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:6470-6510` | Modal Laporan Ketidakhadiran Belum Menyertakan Tombol Aksi 'Buka Pasar' |
| `[AUDIT-100]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:6550` | Aset Koleksi Mewah yang Terkunci Tidak Menampilkan Estimasi Harga & Syarat |

---

### GELOMBANG 3: TAB PORTOFOLIO, MUTASI KAS, PERFORMA & E-IPO (101 - 150)

| ID Task | Prioritas | Kategori | Lokasi Kode | Ringkasan Sasaran Perbaikan |
|---|:---:|---|---|---|
| `[AUDIT-101]` | **TINGGI** | `BUG & PERFORMANCE LAG` | `app.js:8880-8884, TradingWebBridge.java:60` | Serialisasi Ganda Snapshot Akun Tiap Detik Memicu Jeda Garbage Collection (119ms) |
| `[AUDIT-102]` | **TINGGI** | `BUG & PERFORMANCE LAG` | `app.js:1843-1849` | Pemanggilan Ganda bridge.getInstruments() Tiap Detik pada Tab Pasar |
| `[AUDIT-103]` | **TINGGI** | `BUG & PERFORMANCE LAG` | `app.js:1732` | Destruksi & Rekonstruksi Penuh DOM highlights.replaceChildren() Tiap Detik |
| `[AUDIT-104]` | **SEDANG** | `BUG & PERFORMANCE LAG` | `app.js:8870-8874, updateNewsEffects` | Query DOM Global document.querySelectorAll Dijalankan 4 Kali Per Detik (250ms Loop) |
| `[AUDIT-105]` | **SEDANG** | `BUG & PERFORMANCE LAG` | `app.js:updateSponsorCountdown` | Pencarian ID Elemen Berulang document.getElementById Tiap 250ms Tanpa Caching |
| `[AUDIT-106]` | **SEDANG** | `BUG & PERFORMANCE LAG` | `TradingWebBridge.java:36-40` | Pencatatan Logcat Jam Bursa Tetap Berjalan Aktif pada Build Release |
| `[AUDIT-107]` | **TINGGI** | `BUG & PERFORMANCE LAG` | `app.js:7863` | Input Pencarian Saham Tanpa Debounce Memicu Filter Ulang di Setiap Ketikan Huruf |
| `[AUDIT-108]` | **SEDANG** | `BUG & PERFORMANCE LAG` | `app.js:8884, syncPortfolioLive` | Polling Portofolio Tiap Detik Mengalokasikan Puluhan Objek JSON di Tab Aset |
| `[AUDIT-109]` | **TINGGI** | `BUG & PERFORMANCE LAG` | `style.css:2380-2387` | Pemaksaan GPU Layer translateZ(0) pada 18 Class Kartu Memicu Tekanan VRAM |
| `[AUDIT-110]` | **SEDANG** | `BUG & PERFORMANCE LAG` | `style.css:1597` | Efek Box-Shadow 48px pada Modal Sheet Memperlambat Animasi Slide-In |
| `[AUDIT-111]` | **RENDAH** | `BUG & PERFORMANCE LAG` | `app.js:226` | Penyarangan Ganda requestAnimationFrame dan setTimeout pada Animasi Jam |
| `[AUDIT-112]` | **SEDANG** | `BUG & PERFORMANCE LAG` | `app.js:4720` | Redraw Penuh Elemen Grafik Canvas Setiap Kali Modal Detail Saham Dibuka |
| `[AUDIT-113]` | **SEDANG** | `BUG & PERFORMANCE LAG` | `app.js:125, style.css:2419` | Event Listener Scroll Tanpa Atribut { passive: true } Menahan Thread Compositor |
| `[AUDIT-114]` | **RENDAH** | `BUG & PERFORMANCE LAG` | `style.css:2375` | Properti will-change: scroll-position Tidak Pernah Dilepaskan dari DOM |
| `[AUDIT-115]` | **SEDANG** | `BUG & PERFORMANCE LAG` | `TradingWebBridge.java:getInstruments` | Serialisasi String Katalog Emiten Mengirimkan Field Statis yang Berulang |
| `[AUDIT-116]` | **TINGGI** | `BUG` | `app.js:7666` | String 'undefined' Ditulis ke Input Limit Order Jika currentPriceRaw Absen |
| `[AUDIT-117]` | **TINGGI** | `BUG` | `app.js:7700` | Beralih Akun di Tiket Order Tidak Memperbarui Tampilan Kas Tersedia di Bar Atas Sheet |
| `[AUDIT-118]` | **SEDANG** | `BUG` | `app.js:6120` | Input Modal Pengajuan Pinjaman Mengizinkan Nilai Negatif atau Nol |
| `[AUDIT-119]` | **SEDANG** | `BUG` | `app.js:5880` | Pencairan Unit Reksa Dana Mengizinkan Angka Desimal Mengambang Tidak Valid |
| `[AUDIT-120]` | **SEDANG** | `BUG` | `app.js:2487` | Formulir Pendirian PT Mengizinkan Nama Perusahaan yang Hanya Berisi Spasi |
| `[AUDIT-121]` | **SEDANG** | `BUG` | `app.js:2490` | Modal Pendirian Holding Crash Jika Salah Satu Anak Usaha Tidak Memiliki Sektor Sah |
| `[AUDIT-122]` | **SEDANG** | `BUG` | `app.js:6115` | Kenaikan Beban Jam Kerja Karyawan Tidak Menampilkan Peringatan Penurunan Moral |
| `[AUDIT-123]` | **SEDANG** | `BUG` | `app.js:6560` | Pembelian Koleksi Mewah Menghabiskan Kas Hingga Rp 0 Tanpa Buffer Operasional |
| `[AUDIT-124]` | **SEDANG** | `BUG` | `app.js:6210` | Rasio Fasilitas Margin Menampilkan 'NaN%' Saat Total Portofolio Rp 0 |
| `[AUDIT-125]` | **SEDANG** | `BUG` | `app.js:7165` | Perubahan Avatar di Kustomisasi Profil Tidak Langsung Terefleksi di Header Shell |
| `[AUDIT-126]` | **SEDANG** | `BUG` | `app.js:5780` | Penjelasan Reward Streak Harian Tidak Menyebutkan Syarat Transaksi Minimal |
| `[AUDIT-127]` | **SEDANG** | `BUG` | `app.js:5810` | Persentase Dominasi Sektor Berisiko Melebihi 100% Jika Saham Beredar Mengalami Buyback |
| `[AUDIT-128]` | **RENDAH** | `BUG` | `app.js:6230` | Kalkulasi Audit Pajak Menggunakan Tarif Flat Kaku Bukan Tarif Progresif |
| `[AUDIT-129]` | **SEDANG** | `BUG` | `app.js:5825` | Komparasi Return Duel Bos Menampilkan Derau Desimal IEEE 754 Floating Point |
| `[AUDIT-130]` | **SEDANG** | `BUG` | `app.js:6850` | Tombol Lewati Tutorial Tidak Menyimpan Flag Onboarding pada Profil Baru |
| `[AUDIT-131]` | **TINGGI** | `KESALAHAN UI / LAYOUT` | `style.css:2403, app.js:1796` | Tombol Bintang Watchlist di Baris Saham Terlalu Kecil (32px) dan Memicu Salah Buka Detail |
| `[AUDIT-132]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `index.html:174` | Kolom Pencarian Saham Tidak Memiliki Tombol '✕' untuk Hapus Teks Seketika |
| `[AUDIT-133]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:4568, style.css:1430` | Angka Volume Transaksi Saham Satuan Miliar/Triliun Melompat Jadi 2 Baris |
| `[AUDIT-134]` | **TINGGI** | `KESALAHAN UI / LAYOUT` | `index.html:297, style.css:1596` | Tombol 'Tinjau Order' Tertutup Bilah Navigasi Sistem Android Saat Keyboard Muncul |
| `[AUDIT-135]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:renderOrderBook` | Tingkat Antrean Order Book 0 Lot Tampak Identik dengan Antrean Terisi |
| `[AUDIT-136]` | **RENDAH** | `KESALAHAN UI / LAYOUT` | `style.css:1415` | Pill Sektor Emiten Memiliki Warna Seragam Abu-abu Tanpa Kode Warna Industri |
| `[AUDIT-137]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:5015` | Formulir Kalender Dividen Tidak Menandai Tanggal Hari Ini Secara Visual |
| `[AUDIT-138]` | **TINGGI** | `KESALAHAN UI / LAYOUT` | `app.js:965-985` | Kartu Posisi Saham Portofolio Tidak Menampilkan Harga Beli Rata-rata (Avg Price) |
| `[AUDIT-139]` | **RENDAH** | `KESALAHAN UI / LAYOUT` | `app.js:2026` | Tombol 'Lihat Rincian Dana Tertahan' di Dompet Tidak Memiliki Indikator Interaktif Panah |
| `[AUDIT-140]` | **RENDAH** | `KESALAHAN UI / LAYOUT` | `app.js:5845` | Nama Manajer Investasi Reksa Dana Terpotong Terlalu Cepat pada Layar Lebar |
| `[AUDIT-141]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:5910` | Opsi Tenor Deposito 10, 25, dan 50 Hari Disusun Memanjang ke Bawah |
| `[AUDIT-142]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:3380` | Kartu Upgrade Fasilitas Perusahaan Tidak Memiliki Bar Progres Modal |
| `[AUDIT-143]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:2510` | Kartu Anak Usaha Holding Tidak Memiliki Badge Laba/Rugi Harian Ringkas |
| `[AUDIT-144]` | **RENDAH** | `KESALAHAN UI / LAYOUT` | `app.js:5795` | Statistik Produktivitas Staf di Bursa Headhunting Menggunakan Desimal Kering |
| `[AUDIT-145]` | **RENDAH** | `KESALAHAN UI / LAYOUT` | `style.css:280, 2360` | Chip Kas Header Mengambang Canggung di Tengah Layar pada Orientasi Tablet/Foldable |
| `[AUDIT-146]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5018` | Istilah Panjang 'Penawaran Hak Memesan Efek Terlebih Dahulu (HMETD)' Terlalu Kaku |
| `[AUDIT-147]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5915` | Disclaimer Perbankan 'Dijamin oleh protokol perbankan terintegrasi model' Bergaya Meta |
| `[AUDIT-148]` | **TINGGI** | `BUG & PEMBOROSAN TEKS` | `app.js:4815` | Kategori Berita Membocorkan String Enum Database 'EMITEN_CORP_ANNOUNCEMENT' |
| `[AUDIT-149]` | **TINGGI** | `BUG & PEMBOROSAN TEKS` | `app.js:5270` | Notifikasi Penjatahan e-IPO Menampilkan Status Enum Mentah 'PARTIALLY_FILLED_POOLED' |
| `[AUDIT-150]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:2470` | Subjudul Layar Holding Menggunakan Kalimat Puitis Birokrasi yang Redundan |

---

### GELOMBANG 4: BISNIS OPERASIONAL, HOLDING, SDM & MODAL DIALOG (151 - 200)

| ID Task | Prioritas | Kategori | Lokasi Kode | Ringkasan Sasaran Perbaikan |
|---|:---:|---|---|---|
| `[AUDIT-151]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:3350` | Pesan Pengunduran Diri Staf Menggunakan Bahasa Berita Acara Kepolisian |
| `[AUDIT-152]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:6140` | Klausul Pinjaman Bank Menuliskan 'Debitur tunduk pada eksekusi jaminan otomatis' |
| `[AUDIT-153]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5775` | Deskripsi Kartu Booster Riset Menyisakan Istilah 'Algoritma Machine Learning' |
| `[AUDIT-154]` | **SEDANG** | `BUG & PEMBOROSAN TEKS` | `app.js:6390` | Profil Pemain Mencetak String Enum Frekuensi Transaksi 'HIPERAKTIF_INTRADAY' |
| `[AUDIT-155]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:6350` | Menu Simpanan Mencantumkan Frasa 'Sinkronisasi SHA-256 Digest Tanpa Anomali Bit' |
| `[AUDIT-156]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5830` | Dialog Kekalahan Duel Bos Menggunakan Kalimat Kaku 'Kekalahan kalkulasi matematis' |
| `[AUDIT-157]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5785` | Judul Modal Streak Harian Mengulang 4 Kata Berarti Sama Sekaligus |
| `[AUDIT-158]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5805` | Subjudul Dominasi Sektor Mengulang Istilah Buku Teks 'Monopoli Oligopoli' |
| `[AUDIT-159]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5745` | Ketentuan Asuransi Portofolio Menuliskan 'Klausul Force Majeure Sistemik' |
| `[AUDIT-160]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:7945` | Catatan Kaki Laporan Harian Menggunakan Judul 'Penutupan Buku Kas Komprehensif' |
| `[AUDIT-161]` | **TINGGI** | `BUG` | `app.js:openPositionDetailModal` | Blok Catch Kosong Menelan Error Jaringan Tanpa Notifikasi pada Modal Posisi |
| `[AUDIT-162]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:1788` | Warna Hex Hardcoded #FEF9C3 Merusak Palet Tema Gelap (Obsidian / Neon) |
| `[AUDIT-163]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:openCompanyStaffModal` | Meteran Moral Staf Tidak Memiliki Gradien Warna Peringatan Kritis |
| `[AUDIT-164]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openCompanyPolicyModal` | Pengulangan Frasa 'Beban Jam Kerja Operasional' Tiga Kali di Modal Kebijakan |
| `[AUDIT-165]` | **TINGGI** | `BUG` | `app.js:openCompanyCapitalModal` | Injeksi Modal Usaha Mengizinkan Dana Kas yang Sedang Tertahan di Order Saham |
| `[AUDIT-166]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:openDailySponsorModal` | Tombol Klaim Sponsor Tidak Menampilkan Waktu Hitung Mundur Saat Cooldown |
| `[AUDIT-167]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openTutorialModal` | Slogan 'Menjadi Triliuner dalam 5 Menit' Terdengar Seperti Iklan Menyesatkan |
| `[AUDIT-168]` | **SEDANG** | `BUG` | `app.js:openNotificationHistoryModal` | Badge Notifikasi Tidak Bersih Otomatis Saat Riwayat Notifikasi Ditutup |
| `[AUDIT-169]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openNotificationHistoryModal` | Status Kosong Menumpuk Tiga Sinonim 'Riwayat Catatan Notifikasi Sistem Logbook' |
| `[AUDIT-170]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:openPeriodicMissionsModal` | Tombol Hadiah Misi Selesai Tidak Memiliki Animasi Shimmer Penarik Perhatian |
| `[AUDIT-171]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openPeriodicMissionsModal` | Deskripsi Misi Menggunakan Kalimat Tugas Dinas 'Tuntaskan target kuota...' |
| `[AUDIT-172]` | **TINGGI** | `KESALAHAN UI / LAYOUT` | `app.js:openLeagueAndRivalModal` | Peringkat Pemain di Papan Liga Tidak Dipin di Bawah Saat Menggeser Daftar 50 Rival |
| `[AUDIT-173]` | **SEDANG** | `BUG` | `app.js:openNpcProfileModal` | Valuasi Portofolio NPC Mencetak Angka Mentah Tanpa Titik Pemisah Ribuan |
| `[AUDIT-174]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openNpcProfileModal` | Bio NPC Mencantumkan Istilah Mesin 'Bot Algoritma Perdagangan Terprogram' |
| `[AUDIT-175]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:openMarketBossModal` | Foto Profil Bos Pasar Tidak Memiliki Placeholder Saat Aset Gambar Gagal Muat |
| `[AUDIT-176]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openMarketBossModal` | Dialog Kemenangan Bos Taipan Menggunakan Istilah Mesin 'Kalkulasi Arbitrase' |
| `[AUDIT-177]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:openTaxAuditModal` | Tombol Bayar Pajak Tidak Menampilkan Saldo Kas yang Tersisa Pasca Pelunasan |
| `[AUDIT-178]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openPortfolioInsuranceModal` | Klausul Asuransi Menggunakan Istilah Pemrograman 'Didebet Secara Rekursif' |
| `[AUDIT-179]` | **TINGGI** | `BUG` | `app.js:openCustomFundBuilderModal` | Slider Alokasi Reksa Dana Kustom Mengizinkan Total Persentase Melebihi 100% |
| `[AUDIT-180]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:openCustomFundBuilderModal` | Tombol Reset Racikan Portofolio Tidak Memiliki Dialog Konfirmasi |
| `[AUDIT-181]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openTransactionHistoryModal` | Label Filter Mutasi Kas Terlalu Panjang 'Filter Berdasarkan Klasifikasi Tipe...' |
| `[AUDIT-182]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `style.css:2620, app.js:openTransactionHistoryModal` | Kolom Angka Mutasi Kas Tidak Menggunakan Font Tabular Nums (Angka Goyang) |
| `[AUDIT-183]` | **SEDANG** | `BUG` | `app.js:openPlayerProfileModal` | Tingkat Kemenangan Trading (Win Rate) Menampilkan 100% Hanya dari 1 Transaksi |
| `[AUDIT-184]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openPlayerProfileModal` | Statistik Reputasi Menuliskan 'Skor Akumulasi Prestise Sosial Pasar Modal' |
| `[AUDIT-185]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:openInvestorArchetypeModal` | Poligon Radar Arketipe Investor Tidak Memiliki Label Skor pada Sudut Sumbu |
| `[AUDIT-186]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openInvestorArchetypeModal` | Deskripsi Arketipe Menggunakan Istilah Psikoanalisis Pop 'DNA Psikologi Trading' |
| `[AUDIT-187]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:openCollectionItemDetailModal` | Pratinjau Karya Seni Mewah Tidak Dapat Dizoom pada Layar Smartphone |
| `[AUDIT-188]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openCollectionItemDetailModal` | Keterangan Koleksi Menyisakan Buzzwords 'Koleksi Digital Terotentikasi' |
| `[AUDIT-189]` | **TINGGI** | `BUG` | `app.js:openRiskModal` | Banner Peringatan Likuidasi Margin Menolak Hilang Meskipun Kas Sudah Ditambah |
| `[AUDIT-190]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openRiskModal` | Peringatan Margin Call Menggunakan Kalimat Teror Hukum Berlebihan |
| `[AUDIT-191]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:openAchievementsModal` | Daftar Pencapaian Tidak Memiliki Filter Tab (Semua / Belum / Selesai) |
| `[AUDIT-192]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openAchievementsModal` | Judul Sub-kartu Pencapaian Menuliskan 'Prestasi Luar Biasa Pencapaian Finansial' |
| `[AUDIT-193]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:openAvatarCustomizerModal, style.css:265` | Bingkai Profil Bulat Memotong Sudut Foto Avatar Persegi |
| `[AUDIT-194]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openResearchBoostersModal` | Tombol Aktivasi Kartu Booster Menuliskan 'Injeksi Efisiensi Algoritma Riset' |
| `[AUDIT-195]` | **SEDANG** | `BUG` | `app.js:openTechTreeModal` | Garis Konektor Pohon Riset Teknologi Meleset Saat Layar Diputar atau Berubah Ukuran |
| `[AUDIT-196]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openTechTreeModal` | Judul Pohon Riset Mengulang Kata 'Pohon R&D Sintesa Finansial Komprehensif' |
| `[AUDIT-197]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:openHeadhuntingModal` | Daftar Bursa Headhunting Staf Tidak Memiliki Opsi Pengurutan Gaji / Skill |
| `[AUDIT-198]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openHeadhuntingModal` | Label Kontrak Headhunting Menuliskan 'Klausul Retensi Pembajakan Karyawan' |
| `[AUDIT-199]` | **TINGGI** | `BUG` | `app.js:openReservedCashAllocationModal` | Rincian Dana Tertahan Mengalami Ketidakcocokan Jumlah Jika Ada Penjatahan IPO |
| `[AUDIT-200]` | **TINGGI** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openCorporateOfferDetail` | Tombol Aksi e-IPO Menuliskan Kalimat 55 Karakter 'Eksekusi Penebusan Hak Suara...' |

---

### GELOMBANG 5: AKSELERASI HARGA LIVE, OFFLINE & MATCHING ENGINE (201 - 250)

| ID Task | Prioritas | Kategori | Lokasi Kode | Ringkasan Sasaran Perbaikan |
|---|:---:|---|---|---|
| `[AUDIT-201]` | **TINGGI** | `BUG` | `Instrument.java:getIdxTickSize, MarketExchange.java:55` | Batas Fraksi Harga (Tick Size) US Wall Street Menggunakan Kelipatan 1 Sen Bukan Fraksi Desimal Sah |
| `[AUDIT-202]` | **TINGGI** | `BUG` | `OrderManager.java:validateOrder, app.js:7740` | Order Tipe STOP_LOSS Tidak Memverifikasi Apakah Harga Pemicu Berada di Luar Batas ARA/ARB |
| `[AUDIT-203]` | **SEDANG** | `KEMACETAN PERFORMA (LAG)` | `MatchingEngine.java:getOrderBookSnapshot, app.js:4610` | Antrean Order Book Tidak Membatasi Maksimum 10 Kedalaman Level (Memory Leak Antrean) |
| `[AUDIT-204]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:7765` | Label Waktu Order 'Time in Force' Menggunakan Jargon Bahasa Inggris 'DAY / GTC' Tanpa Penjelasan |
| `[AUDIT-205]` | **SEDANG** | `BUG` | `app.js:showOrderReceipt, TradingWebBridge.java:notifyOrderFill` | Notifikasi Transaksi Berhasil (Fill Order) Memunculkan Toast Ganda Jika Order Terpenuhi Sebagian |
| `[AUDIT-206]` | **TINGGI** | `KESALAHAN UI / LAYOUT` | `style.css:1680, app.js:renderOrderRow` | Tombol Tarik Order (Cancel Order) di Daftar Order Aktif Terlalu Mepet ke Tepi Layar |
| `[AUDIT-207]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:7790` | Teks Konfirmasi Order Mengulang Frasa 'Periksa Kembali Rincian Pesanan' Dua Kali |
| `[AUDIT-208]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:7715` | Kalkulasi Biaya Komisi Broker Menampilkan Angka Nol Koma Sekian Rupiah yang Membingungkan |
| `[AUDIT-209]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:4564-4565` | Indikator ARA (Auto Rejection Atas) dan ARB Tidak Memiliki Tag Warna Pembeda Jelas |
| `[AUDIT-210]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `style.css:1435, app.js:renderMiniChart` | Grafik Mini Sparkline Candlestick Mengalami Distorsi Aspek Rasio pada Ponsel Layar Lipat |
| `[AUDIT-211]` | **TINGGI** | `BUG` | `Instrument.java:updatePrice, MarketEngine.java:rollSession` | Perhitungan Rata-rata Harga Penutupan Sesi Kemarin (Prev Close) Bernilai Nol Saat Hari Baru |
| `[AUDIT-212]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:625` | Keterangan Jam Operasional Bursa Menuliskan Format Zona Waktu Kaku 'UTC+07:00' |
| `[AUDIT-213]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:7682` | Dropdown Pilihan Akun Trading di Tiket Order Menampilkan Nama Tipe Bahasa Inggris 'CLIENT' |
| `[AUDIT-214]` | **TINGGI** | `BUG` | `app.js:orderTypeChangeHandler` | Order Tipe MARKET Membiarkan Kotak Input Harga Limit Tetap Aktif dan Bisa Diketik |
| `[AUDIT-215]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:renderPositionAction` | Tombol Pintas Aksi Jual Cepat (Quick Sell) di Portofolio Tidak Menampilkan Jumlah Lot Maksimal |
| `[AUDIT-216]` | **SEDANG** | `BUG` | `MatchingEngine.java:executeTrade, app.js:updateOrderEstimate` | Slippage Pasar Ekstrem Tidak Menampilkan Estimasi Kerugian Harga Saat Beli di Sesi Volatil |
| `[AUDIT-217]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:updateMarketStatusBadge` | Status Pasar Tertutup (Closed Market) Tidak Menjelaskan Kapan Sesi Berikutnya Akan Buka |
| `[AUDIT-218]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `style.css:1710` | Indikator Kedalaman Bid/Ask (Order Depth Bar) Sering Mengalami Overflow Keluar Kartu |
| `[AUDIT-219]` | **RENDAH** | `KESALAHAN UI / LAYOUT` | `style.css:2403` | Tombol Hapus Watchlist Menggunakan Ikon Bintang Gelap yang Kurang Kontras |
| `[AUDIT-220]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:6215` | Teks Dialog Batas Kredit Margin Mengulang Kata 'Jaminan Likuiditas' Tiga Kali |
| `[AUDIT-221]` | **TINGGI** | `BUG` | `InstrumentCatalog.java:createUsInstruments, app.js:7661` | Nilai Satuan Lot Saham AS (US Wall Street) Menampilkan '1 Lot = 100 Saham' Padahal Bursa US Mengizinkan 1 Lembar |
| `[AUDIT-222]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `NpcAgent.java:230, app.js:showOrderReceipt` | Status Notifikasi Eksekusi Saham Menampilkan String ID Acak 'REQ-RET-S-179342...' |
| `[AUDIT-223]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `style.css:1695` | Warna Latar Belakang Kotak Input Order Limit Terlalu Gelap dan Sulit Dilihat Batasnya |
| `[AUDIT-224]` | **TINGGI** | `BUG` | `app.js:pctButtonHandler` | Tombol Persentase Lot 25% Tidak Membulatkan ke Bawah Kelipatan Lot Utuh |
| `[AUDIT-225]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:openOrderTicket` | Dialog Konfirmasi Jual Seluruh Posisi Saham Tidak Memiliki Tombol 'Jual Semua' |
| `[AUDIT-226]` | **TINGGI** | `BUG` | `CorporateActionService.java:subscribeIpo, app.js:5250` | Penyetoran Dana e-IPO Mengizinkan Pemesanan di Atas Kapasitas Kas Tanpa Pesan Validasi Jelas |
| `[AUDIT-227]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5015` | Jadwal Tanggal Cum Dividen Tidak Dijelaskan Perbedaannya Terhadap Ex Dividen |
| `[AUDIT-228]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:5030` | Persentase Hak Suara RUPS Menampilkan Angka Nol Koma Kosong Saat Kepemilikan di Bawah 0.01% |
| `[AUDIT-229]` | **TINGGI** | `BUG` | `app.js:renderVotingAgenda` | Tombol Voting RUPS Tetap Aktif Setelah Agenda Rapat Ditutup |
| `[AUDIT-230]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5032` | Judul Agenda RUPS Membocorkan Istilah Legal Formal 'Persetujuan Rencana Penyesuaian Anggaran Dasar Pasal 3' |
| `[AUDIT-231]` | **TINGGI** | `KESALAHAN UI / LAYOUT` | `app.js:5275` | Hasil Penjatahan e-IPO 60/40 Tidak Menampilkan Jumlah Lot Pengembalian Dana (*Refund*) |
| `[AUDIT-232]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5220` | Pill Status Penawaran e-IPO 'BOOK_BUILDING' Menggunakan Huruf Kapital Bahasa Asing |
| `[AUDIT-233]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:5225` | Batas Waktu Penawaran e-IPO Menampilkan Tanggal Kalender Tanpa Indikator Hari Bursa Aktif |
| `[AUDIT-234]` | **SEDANG** | `BUG` | `app.js:5016` | Aksi Korporasi Pembagian Saham Bonus Menampilkan Nilai Tunai Rp 0 yang Membingungkan |
| `[AUDIT-235]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:renderShareholdersTab` | Daftar Pemegang Saham Pengendali Tidak Membedakan Warna Porsi Publik vs Pemain |
| `[AUDIT-236]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5022` | Keterangan Efek Dilusi Rights Issue Terlalu Berbelit-belit Sepanjang 80 Karakter |
| `[AUDIT-237]` | **TINGGI** | `BUG` | `CorporateActionService.java:executeDividendPayment, EconomyLedger.java:310` | Penerimaan Dividen Tunai Tidak Menyertakan Notifikasi Masuk ke Riwayat Mutasi Buku Kas |
| `[AUDIT-238]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:5240` | Pilihan Harga Penawaran e-IPO Tidak Memiliki Slider Cepat Antara Harga Minimal dan Maksimal |
| `[AUDIT-239]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5025` | Pengumuman Merger Emiten Menggunakan Akronim Kaku 'M&A' Tanpa Penjelasan Dampak Harga |
| `[AUDIT-240]` | **TINGGI** | `KESALAHAN UI / LAYOUT` | `app.js:renderIpoDetails` | Prospektus Ringkas e-IPO Tidak Menampilkan Rasio Valuasi PER dan PBV Calon Emiten |
| `[AUDIT-241]` | **TINGGI** | `BUG` | `IpoOffering.java:cancelSubscription, app.js:cancelIpoSubscription` | Pembatalan Pemesanan e-IPO Masih Diizinkan Saat Periode Penjatahan Telah Dimulai |
| `[AUDIT-242]` | **SEDANG** | `BUG` | `app.js:5230` | Tombol Pelajari Prospektus Mengarahkan ke Link Kosong 'href="#"' |
| `[AUDIT-243]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5265` | Status Tanggal Listing Saham Baru Menggunakan Istilah Bahasa Asing 'Go Public Bell' |
| `[AUDIT-244]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:renderDividendHistory` | Tabel Riwayat Dividen Emiten Menampilkan Tahun Tanpa Nomor Kuartal (Q1/Q2/Q3/Q4) |
| `[AUDIT-245]` | **TINGGI** | `BUG` | `app.js:validateIpoForm` | Batas Minimal Pemesanan e-IPO Mengizinkan Input 0 Lot |
| `[AUDIT-246]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5017` | Deskripsi Dampak Stock Split Mengulang Rumus Matematika 'Jumlah Saham Naik Harga Turun' |
| `[AUDIT-247]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `style.css:1240` | Pill Kategori Dividen Menggunakan Warna Teks Kuning yang Sulit Terbaca di Latar Abu-abu |
| `[AUDIT-248]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:5028` | Formulir Voting RUPS Tidak Menyediakan Pilihan Golput / Abstain |
| `[AUDIT-249]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:5255` | Notifikasi e-IPO Sukses Mencantumkan Teks 'Pemesanan Tercatat di Sistem Sentral Kustodian' |
| `[AUDIT-250]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:renderIpoList` | Kartu Penawaran e-IPO yang Telah Kadaluarsa Tidak Berpindah ke Bagian Bawah Daftar |

---

### GELOMBANG 6: MULTI-ASET, DONUT SEKTOR, AKSESIBILITAS & SISTEM (251 - 300)

| ID Task | Prioritas | Kategori | Lokasi Kode | Ringkasan Sasaran Perbaikan |
|---|:---:|---|---|---|
| `[AUDIT-251]` | **TINGGI** | `BUG` | `OperatingCompany.java:calculateDailyRevenue, CompanyManager.java:330` | Kalkulasi Pendapatan Harian Perusahaan Mengizinkan Nilai Negatif Saat Beban Gaji Menunggak |
| `[AUDIT-252]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:openCompanyStaffModal` | Modal Rekrutmen Pegawai Tidak Menampilkan Efek Peningkatan Kapasitas Produksi Sebelum Merekrut |
| `[AUDIT-253]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openCompanyPolicyModal` | Istilah Jenjang Tunjangan Fasilitas Menggunakan Kata 'Benefit Tier C/B/A' Tanpa Rincian |
| `[AUDIT-254]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `OperatingCompany.java:calculateDailyCapacity, app.js:renderCompanyOverview` | Kapasitas Produksi Pabrik Menampilkan Angka Desimal Panjang Saat Moral Pegawai Rendah |
| `[AUDIT-255]` | **RENDAH** | `KESALAHAN UI / LAYOUT` | `app.js:openCompanyCapitalModal` | Penyetoran Kas Anak Usaha Tidak Menampilkan Batas Saldo Maksimal Gudang Kas Entitas |
| `[AUDIT-256]` | **TINGGI** | `BUG & ERGONOMI` | `app.js:renderEmployeeRow` | Tombol Pecat Karyawan (Fire Employee) Bersebelahan dengan Tombol Naik Gaji Tanpa Konfirmasi |
| `[AUDIT-257]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:renderCompanyFinancials` | Laporan Keuangan Anak Usaha Menuliskan Jargon Akuntansi 'Biaya Depresiasi Amortisasi Aset Berwujud' |
| `[AUDIT-258]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:showSupplyChainWarning` | Krisis Rantai Pasok (Supply Chain Crisis) Menampilkan Notifikasi Teror Merah Tanpa Petunjuk Solusi |
| `[AUDIT-259]` | **RENDAH** | `KESALAHAN UI / LAYOUT` | `app.js:renderSubsidiaryCard` | Daftar Anak Usaha Holding Menampilkan Logo Gedung Identik untuk Semua Sektor |
| `[AUDIT-260]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:2505` | Deskripsi Status Holding Menyisakan Frasa Teoretis 'Konsolidasi Kendali Monopoli Vertikal' |
| `[AUDIT-261]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:validateHiring` | Batas Maksimal Karyawan Pabrik Tidak Menampilkan Alasan 'Kapasitas Ruang Penuh' |
| `[AUDIT-262]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:renderWorkPolicyOptions` | Tingkat Produktivitas Jam Kerja 6 Jam Menampilkan Teks Peringatan Padahal Meningkatkan Retensi |
| `[AUDIT-263]` | **SEDANG** | `BUG` | `CompanyManager.java:transferFunds, app.js:openHoldingTransferModal` | Penarikan Dividen Anak Usaha ke Rekening Pribadi Tidak Memperhitungkan Pajak Dividen Badan |
| `[AUDIT-264]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:renderSectorPieOverview` | Teks Evaluasi Sektor Kue Pasar (Sector Pie) Menuliskan 'Perebutan Zero-Sum Anggaran Nasional' |
| `[AUDIT-265]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:renderCompetitorShare` | Grafik Pangsa Pasar Perusahaan Tidak Menampilkan Nama Kompetitor Utama |
| `[AUDIT-266]` | **TINGGI** | `BUG & KEAMANAN UX` | `app.js:liquidateCompanyHandler` | Tombol Tutup Usaha (Bubar PT / Likuidasi) Tidak Memiliki Konfirmasi Password atau Kata Kunci |
| `[AUDIT-267]` | **SEDANG** | `BUG` | `app.js:renderCeoContract` | Gaji Eksekutif CEO Menampilkan Angka Per Hari Padahal Kontrak Bertuliskan Bulanan |
| `[AUDIT-268]` | **SEDANG** | `BUG` | `app.js:renderRawMaterialStatus` | Label Status Bahan Baku Menampilkan 'Normal' Meskipun Sedang Terjadi Boikot Industri |
| `[AUDIT-269]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openFoundCompanyModal` | Nama Default Anak Usaha Baru Menggunakan Bahasa Inggris 'New Operating Entity 01' |
| `[AUDIT-270]` | **SEDANG** | `BUG` | `app.js:renderTrainingHistory` | Kartu Riwayat Pelatihan Pegawai Menampilkan Tanggal Kalender Milidetik Timestamp Mentah |
| `[AUDIT-271]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:renderCompanyMetrics` | Rasio Perputaran Modal Usaha Menampilkan Singkatan Rumus Bahasa Asing 'CCC' (Cash Conversion Cycle) |
| `[AUDIT-272]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:promoteStaffHandler` | Tombol Promosikan Staf Menjadi Manajer Tidak Menyebutkan Biaya Kenaikan Gaji |
| `[AUDIT-273]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:renderPayrollWarning` | Peringatan Tunggakan Gaji Karyawan Berisi Kalimat Menyeramkan 'Ancaman Mogok Kerja Massal' |
| `[AUDIT-274]` | **SEDANG** | `BUG` | `OperatingCompany.java:calculateDailyRevenue, app.js:renderCompanyOverview` | Kapasitas Maksimal Penjualan Produk Terbentur Batas Permintaan Pasar Tanpa Penjelasan |
| `[AUDIT-275]` | **RENDAH** | `KESALAHAN UI / LAYOUT` | `app.js:renderEfficiencyStat` | Indikator Efisiensi Bahan Baku Menampilkan 1 desimal Padahal Angka Fluktuatif |
| `[AUDIT-276]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:openFxForm` | Nilai Tukar Valas (Kurs IDR/USD) Tidak Menampilkan Spread Beli dan Jual Broker |
| `[AUDIT-277]` | **SEDANG** | `BUG` | `app.js:openFxForm` | Input Penukaran Valuta Asing Mengizinkan Jumlah Desimal Sen USD Berlebih |
| `[AUDIT-278]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openResearchTiersModal` | Deskripsi Paket Terminal Whale Menggunakan Slogan 'Klub Rahasia Taipan Konglomerat' |
| `[AUDIT-279]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:renderCommodityPosition` | Simulasi Emas Batangan Tidak Menampilkan Satuan Gram pada Kartu Portofolio Multi-Aset |
| `[AUDIT-280]` | **RENDAH** | `KESALAHAN UI / LAYOUT` | `app.js:resetPreferencesHandler` | Tombol Reset Preferensi Tema Mengembalikan Pengaturan Tanpa Notifikasi Toast |
| `[AUDIT-281]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:renderOfflineStatus` | Teks Status Koneksi Offline Menuliskan 'Koneksi Terputus Beralih ke Mode Pesawat' |
| `[AUDIT-282]` | **TINGGI** | `BUG & AKSESIBILITAS` | `style.css:highContrastOverrides` | Fitur Aksesibilitas High Contrast Mode Tidak Mengubah Warna Border Kartu Transaksi |
| `[AUDIT-283]` | **SEDANG** | `BUG & AKSESIBILITAS` | `style.css:reducedMotionOverrides` | Fitur Reduced Motion Tidak Menghilangkan Transisi CSS Pada Dialog Modal Sheet |
| `[AUDIT-284]` | **SEDANG** | `BUG` | `app.js:renderNewsTargetCountdown` | Peringatan Waktu Kedaluwarsa Sinyal Berita Menggunakan Jam Detik Epoch Mentah |
| `[AUDIT-285]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:renderMacroReportTitle` | Kategori Riset Makro Menuliskan Judul Panjang 'Analisis Ekonometrika Siklus Gelombang Kondratiev' |
| `[AUDIT-286]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:sharePortfolioHandler` | Tombol Bagikan Portofolio (Share Wealth) Tidak Menyediakan Pratinjau Gambar Bersih |
| `[AUDIT-287]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:soundPlayer, index.html:settingsModal` | Suara Efek Transaksi (Sound FX) Berbunyi Sangat Keras Tanpa Kontrol Volume Slider |
| `[AUDIT-288]` | **RENDAH** | `KESALAHAN UI / LAYOUT` | `MainActivity.java:vibrateFeedback, app.js:triggerHaptic` | Respon Getaran Haptik (Haptic Feedback) Terasa Kaku dan Monoton di Semua Tombol |
| `[AUDIT-289]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:renderTextSizeOptions` | Ukuran Font Teks '100%' di Pengaturan Ukuran Teks Masih Menggunakan Persentase Abstrak |
| `[AUDIT-290]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:exportSaveHandler` | Pesan Sukses Ekspor Save Game Menyebutkan Path Direktori Teknis '/storage/emulated/0/...' |
| `[AUDIT-291]` | **TINGGI** | `BUG & KEAMANAN UX` | `style.css:2190, app.js:openSaveManagementModal` | Tombol Hapus Akun Permainan Diberi Warna Hijau yang Tertukar dengan Tombol Simpan |
| `[AUDIT-292]` | **SEDANG** | `KESALAHAN UI / LAYOUT` | `app.js:renderMacroOverview` | Nilai Inflasi Tahunan Tidak Menjelaskan Dampaknya Terhadap Nilai Kas Menganggur |
| `[AUDIT-293]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openNewGameConfirmationModal` | Teks Konfirmasi Mulai Game Baru Menampilkan Teks Peringatan Dua Paragraf Berulang |
| `[AUDIT-294]` | **SEDANG** | `BUG` | `MainActivity.java:batteryReceiver, app.js:powerSaveManager` | Indikator Baterai Rendah Tidak Otomatis Mengaktifkan Mode Hemat Daya Game |
| `[AUDIT-295]` | **SEDANG** | `BUG` | `app.js:openPrivacyPolicyModal` | Tautan Kebijakan Privasi di Menu Lainnya Mengarah ke URL Localhost Port 8080 |
| `[AUDIT-296]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:openCollectionsGalleryModal` | Teks Subjudul Galeri Koleksi Menuliskan 'Kurasi Aset Glamor Portofolio Prestisius' |
| `[AUDIT-297]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:renderTerminalTierBadge` | Badge Tingkat Akses Terminal Basic Menampilkan Teks 'Paket Gratisan' Bernada Meremehkan |
| `[AUDIT-298]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:checkPlaytimeWarning` | Peringatan Waktu Bermain (Game Time Alert) Menggunakan Kalimat Ketus 'Anda Terlalu Lama Bermain' |
| `[AUDIT-299]` | **SEDANG** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:exitAppHandler` | Tombol Keluar Aplikasi Menampilkan Teks Kaku 'Hentikan Proses Eksekusi Runtime' |
| `[AUDIT-300]` | **RENDAH** | `PEMBOROSAN UI & REDUNDANSI TEKS` | `app.js:finalAuditBanner` | Laporan Penutupan Evaluasi Lengkap Menuliskan Kalimat 'Audit Komprehensif Berhasil Selesai Secara Tuntas Paripurna' |

---

## III. PROTOKOL VERIFIKASI SEBELUM TUNTAS (VERIFICATION BEFORE COMPLETION)

Sesuai mandat ketat verifikasi rekayasa perangkat lunak:
1. **Integritas Save Game:** Memastikan skema persistensi `save_game_v2.json` tidak mengalami kerusakan dan lolos validasi atomic recovery.
2. **Uji Suite Pengujian Sintesa:** Mengeksekusi `python Work/run_synthesis_tests.py` dan memastikan 17/17 tes domain berstatus `OK (17 tests)` tanpa kegagalan.
3. **Kompilasi & Build APK Android:** Menjalankan `./gradlew.bat assembleDebug` hingga berstatus `BUILD SUCCESSFUL`.
4. **Inspeksi Visual Headless & Live Browser:** Menangkap tangkapan layar tampilan nyata pada resolusi mobile 412x915px dan mengonfirmasi bahwa tidak ada pemotongan layout atau tumpang tindih elemen visual.
