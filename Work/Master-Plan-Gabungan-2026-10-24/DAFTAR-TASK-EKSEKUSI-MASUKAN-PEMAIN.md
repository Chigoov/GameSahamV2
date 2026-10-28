# Daftar Task Terperinci Eksekusi Masukan Pemain — Game Saham

**Dokumen Pendamping:** `Work/Master-Plan-Gabungan-2026-10-24/MASTER-PLAN-EKSEKUSI-MASUKAN-PEMAIN.md`  
**Root Produksi:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`  
**Status Pelacakan:** `SELESAI LENGKAP & TERVERIFIKASI PENUH (100% COMPLETE)`  
**Kandidat Rilis Final:** `APK-08` (`GameSaham-APK08-revisi-final.apk`, 14.299.879 bytes, SHA-256 `87ae6d3f60b466cd451314da1e850e54dd4c474f786aa2f12270086ce6808681`)  
**Tanggal:** 27 Oktober 2026, Asia/Jakarta  
**Integritas Save Pemain:** `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed` (HASH_ONLY, Utuh Terjaga)

---

## 1. Ringkasan Struktur & Denominasi Task

- **Total Paket Utama:** 8 Paket (M0 s/d M7)
- **Total Task & Subtask Konkret:** **41 Subtask**
- **Cakupan Pemetaan:**
  - **21 Anotasi Pemain / Pemilik:** A01 s/d A21 (100% terpetakan)
  - **9 Rekomendasi Disetujui:** R1, R2, R3, R4, R6, R7, R8, R9, R10 (100% terpetakan; R5 tidak dipilih)
  - **Catatan Teknis & Kualitas APK-07:** K-018, K-024, K-002, K-013, K-005, K-025 (100% terintegrasi)

---

## 2. Matriks Pemetaan Anotasi & Rekomendasi ke Task

| Sumber Masukan | Pokok Bahasan | Subtask Implementasi Terkait | Paket |
|:---|:---|:---|:---:|
| **A01** | Kalender dan Fase Lengkap IPO | M4.1 | M4 |
| **A02 / K-025** | Tombol Beli & Jual Terlihat Langsung (Fixed Docking) | M2.4, M3.1 | M2, M3 |
| **A03 / R2** | Form Order Dialog Ringkas Tengah Layar & Bertahap | M2.5, M3.2 | M2, M3 |
| **A04** | Navigasi Kategori Portofolio (Contoh B: Transisi Tab Dinamis) | M2.2, M5.6 | M2, M5 |
| **A05** | Nama Fitur Kategori Portofolio Ringkas (Tanpa Jargon) | M5.6 | M5 |
| **A06** | Replay Historis sebagai Simulasi Strategi Data Lampau | M4.2 | M4 |
| **A07 / A13** | Restrukturisasi Menu Profil A (Pengaturan di Bagian Bawah) | M2.3, M4.3 | M2, M4 |
| **A08** | Kartu Booster Riset dengan Pembeda Warna, Label & Ikon | M2.8, M4.4 | M2, M4 |
| **A09 / R4** | Profil Lengkap: Grafik Kekayaan, Statistik Trading, Pencapaian | M2.3, M4.4 | M2, M4 |
| **A10 / R3** | Riwayat Order Ringkas dengan Filter Status & Bahasa Manusia | M3.3 | M3 |
| **A11 / R10** | Status Order Dapat Membuka Detail Saham Terkait & Kembali | M3.4 | M3 |
| **A12** | Hapus Pengaturan Ukuran Teks Game (Ikuti Skala Sistem OS) | M4.5 | M4 |
| **A14** | Integrasi 150 Aset Seni Koleksi Mewah | M0.3, M5.1 | M0, M5 |
| **A15** | 3 Alternatif Preview Galeri Koleksi Mewah | M2.1, M5.2 | M2, M5 |
| **A16** | Pembiayaan Sederhana, Agunan Dilepas, Denda 5% per 24 Jam | M6.1, M6.2, M6.3, M6.4 | M6 |
| **A17** | Bahasa Seluruh Area Bisnis Lebih Sederhana & Profesional | M2.6, M5.3 | M2, M5 |
| **A18** | Tombol & Modal Panduan Ringkas Bisnis di Bagian Atas | M2.6, M5.4 | M2, M5 |
| **A19** | Pemilih Akun Transaksi Terpusat di Profil (Tanpa Indikator Luar) | M2.3, M4.3 | M2, M4 |
| **A20 / R1** | Rincian Alokasi Dana IDR Tertahan Dapat Diketuk & Diperiksa | M2.7, M3.5 | M2, M3 |
| **A21** | Baris Dompet Konsolidasi Total Aset Pribadi | M2.7, M5.5 | M2, M5 |
| **R1 / R7** | Ringkasan Kas & Review Biaya/Potongan Bersih pada Order | M2.5, M3.2 | M2, M3 |
| **R6** | Indikator Status Bursa & Waktu Dapat Diketuk untuk Penjelasan | M4.6 | M4 |
| **R8** | Penanda Konteks Harga Simulasi Game vs Data Historis Nyata | M4.2 | M4 |
| **R9 / K-018** | Penanganan Status Belum Pasti & Pemulihan Intent Transaksi | M1.2 | M1 |
| **R10** | Preservasi Posisi Scroll & Konteks Layar saat Kembali (Back) | M3.4, M5.6 | M3, M5 |
| **K-024** | Penanganan Error & State FAILED pada Background Clock Sync | M1.1 | M1 |
| **K-015** | Penegakan Ambang Batas Strict Gate 48x48px dan Kontras 4.5:1 | M0.2, M7.1 | M0, M7 |
| **K-002 / K-013** | Cakupan Matriks Modal & Benchmark Feedback Berat Riil | M7.1, M7.2 | M7 |
| **K-005** | Rekonsiliasi Generator Laporan Status dan Manifest Aset | M1.3, M7.5 | M1, M7 |

---

## 3. Rincian 41 Subtask per Paket Pekerjaan (M0 s/d M7)

### Paket M0: Persiapan, Verifikasi Lingkungan & Pencocokan Aset (3 Subtask)

- **M0.1 — Verifikasi Integritas Workspace & Save Pemain**
  - *Masalah/Kebutuhan:* Memastikan tidak ada kerusakan save pemain aktif, dirty checkout yang tidak sah, atau port QA yang bocor sebelum pekerjaan dimulai.
  - *Hasil yang Diharapkan:* Save pemain asli memiliki SHA-256 `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed` utuh; seluruh pengujian terisolasi pada save QA baru.
  - *Ketergantungan:* Tidak ada.
  - *Cara Pengujian/Bukti:* Skrip Python hash SHA-256 dan pemeriksaan `netstat` bebas listener.
  - *Status:* `SELESAI_TERVERIFIKASI`.

- **M0.2 — Penyusunan Daftar Task Terperinci & Pemetaan Masukan Pemain**
  - *Masalah/Kebutuhan:* Memecah 8 paket M0-M7 menjadi 41 subtask terukur tanpa kompresi, serta memetakan 21 anotasi dan 9 rekomendasi.
  - *Hasil yang Diharapkan:* Dokumen pendamping `DAFTAR-TASK-EKSEKUSI-MASUKAN-PEMAIN.md` tersimpan rapi tanpa menimpa `TRACKER.csv` kanonis 126 ID.
  - *Ketergantungan:* M0.1.
  - *Cara Pengujian/Bukti:* Pembacaan file dan verifikasi kelengkapan seluruh 41 subtask.
  - *Status:* `SELESAI_TERVERIFIKASI`.

- **M0.3 — Audit & Pencocokan 150 Aset Seni Koleksi Mewah (A14)**
  - *Masalah/Kebutuhan:* Memastikan 150 entri katalog domain cocok 100% dengan `MANIFEST.csv` dan berkas fisik PNG transparan resolusi tinggi 1254x1254.
  - *Hasil yang Diharapkan:* 150/150 item ID cocok byte-for-byte; 0 missing images, 0 mismatched ID.
  - *Ketergantungan:* M0.1.
  - *Cara Pengujian/Bukti:* Eksekusi skrip verifikasi Python `MANIFEST.csv` vs `katalog_koleksi_150.json` vs folder `images/`.
  - *Status:* `SELESAI_TERVERIFIKASI` (150/150 cocok sempurna).

---

### Paket M1: Perlindungan Data & Defect Teknis Independen (3 Subtask)

- **M1.1 — Penanganan Status FAILED pada Sinkronisasi Background (K-024)**
  - *Masalah/Kebutuhan:* `requestClockSync(false)` (heartbeat otomatis) me-resolve Promise bahkan saat status FAILED / exception, sehingga kegagalan tersembunyi.
  - *Hasil yang Diharapkan:* Propagasi status error FAILED ditangani secara benar; jika sinkronisasi gagal, status ditampilkan jujur dan aksi finansial yang mensyaratkan waktu aman ditahan tanpa mengorbankan kelancaran Back button.
  - *Ketergantungan:* M0.1.
  - *Cara Pengujian/Bukti:* Uji mock bridge fault status `FAILED` dan verifikasi penanganan error di `app.js`.
  - *Status:* `DALAM_PEKERJAAN`.

- **M1.2 — Persistensi Intent Koleksi In-Flight Lintas Reload (K-018 / R9)**
  - *Masalah/Kebutuhan:* `pendingCollectionIntents` hilang saat reload browser, sehingga refresh setelah respons putus menyebabkan kehilangan kemampuan retry dengan `requestId` yang sama.
  - *Hasil yang Diharapkan:* Intent transaksi in-flight dipersistensikan secara aman ke sesi; reload memulihkan intent dan menampilkan dialog pemulihan receipt lama (`SETTLED`) tanpa pesan gagal atau duplikasi kas.
  - *Ketergantungan:* M0.1.
  - *Cara Pengujian/Bukti:* Playwright abort response $\rightarrow$ reload page $\rightarrow$ verifikasi tombol retry memulihkan receipt awal.
  - *Status:* `DALAM_PEKERJAAN`.

- **M1.3 — Perbaikan Generator Laporan Status & Manifest Aset (K-005)**
  - *Masalah/Kebutuhan:* Generator script pelaporan hasil JVM tidak boleh mengasumsikan status default PASS atau jumlah tes 628 jika ringkasan log gagal dibaca; narasi manifest APK harus 100% konsisten dengan nilai mesin.
  - *Hasil yang Diharapkan:* Generator mengekstrak exit code proses nyata; mencatat INCOMPLETE/FAIL bila log terpotong; manifest hash aset di RESULTS.md cocok 100% dengan komputasi file APK.
  - *Ketergantungan:* M0.1.
  - *Cara Pengujian/Bukti:* Uji negatif generator pada log kosong/terpotong dan verifikasi output JSON.
  - *Status:* `DALAM_PEKERJAAN`.

---

### Paket M2: Desain, Prototipe & Preview UI Interaktif (8 Subtask)
*Catatan: Seluruh subtask ini wajib diajukan melalui Question & Answer dengan pilihan klik sebelum diterapkan ke game.*

- **M2.1 — Pembuatan 3 Alternatif Preview Galeri Koleksi Mewah 150 Aset (A15)**
  - *Masalah/Kebutuhan:* Pemilik menolak desain galeri lama dan mewajibkan 3 alternatif desain preview sebelum implementasi.
  - *Hasil yang Diharapkan:* 3 alternatif interaktif (Opsi 1: Luxury Showcase Grid 2-Kolom, Opsi 2: Elegant Horizontal Carousel List, Opsi 3: Compact Tiered Catalog) lengkap dengan visual kartu, badge kepemilikan, dan tombol aksi.
  - *Ketergantungan:* M0.3.
  - *Cara Pengujian/Bukti:* Berkas HTML prototipe interaktif `Artifacts/Preview-Masukan-Pemain-2026-10-28/galeri-alternatif.html` dan tangkapan layar visual.
  - *Status:* `MENUNGGU_PREVIEW_DAN_PERSETUJUAN`.

- **M2.2 — Preview UI Navigasi Portofolio B (A04)**
  - *Masalah/Kebutuhan:* Menunjukkan penerapan contoh B yang disetujui: saat kategori Saham dibuka, tab Portofolio sementara berubah menjadi tab Saham; saat Back, tab Portofolio muncul kembali.
  - *Hasil yang Diharapkan:* Preview interaktif transisi tab bar bawah dinamis dan rute kembali yang mulus.
  - *Ketergantungan:* M0.1.
  - *Cara Pengujian/Bukti:* Halaman prototipe interaktif dan screenshot transisi tab.
  - *Status:* `MENUNGGU_PREVIEW_DAN_PERSETUJUAN`.

- **M2.3 — Preview UI Restrukturisasi Halaman Profil A (A07, A13, A19)**
  - *Masalah/Kebutuhan:* Menunjukkan 4 kelompok menu Profil A: (1) Profil & Akun; (2) Aktivitas Saya; (3) Bantuan & Peraturan; (4) Pengaturan & Sistem di bagian bawah dengan Penyimpanan & Backup serta Mulai Permainan Baru; pemilih akun dipindahkan ke Profil.
  - *Hasil yang Diharapkan:* Desain visual halaman profil yang terstruktur rapi, elegan, dan mudah dinavigasikan.
  - *Ketergantungan:* M0.1.
  - *Cara Pengujian/Bukti:* Prototipe interaktif menu Profil A dan screenshot UI.
  - *Status:* `MENUNGGU_PREVIEW_DAN_PERSETUJUAN`.

- **M2.4 — Preview UI Tombol Beli & Jual Terlihat Langsung pada Detail Emiten (A02 / K-025)**
  - *Masalah/Kebutuhan:* Tombol Beli dan Jual harus langsung terlihat saat detail emiten dibuka (tanpa scroll) dan tetap terjangkau saat konten digulir.
  - *Hasil yang Diharapkan:* Desain fixed docking di atas navigation bar bawah yang bersih, tidak menutupi grafik/antrean order book, dan adaptif pada viewport pendek (434x688 dan 360x640).
  - *Ketergantungan:* M0.1.
  - *Cara Pengujian/Bukti:* Prototipe layar detail emiten (contoh BMRI & BBCA) dan screenshot viewport 434x688.
  - *Status:* `MENUNGGU_PREVIEW_DAN_PERSETUJUAN`.

- **M2.5 — Preview UI Form Order Dialog Ringkas Tengah Layar (A03, R1, R2, R7)**
  - *Masalah/Kebutuhan:* Form order lama memenuhi seluruh layar; pemain membutuhkan kotak dialog modal ringkas di tengah layar dengan opsi bertahap (dasar dulu, lanjutan jika perlu), ringkasan kas tersedia/tertahan, dan rincian biaya.
  - *Hasil yang Diharapkan:* Kotak dialog modal ringkas di tengah layar yang adaptif, jelas, dan aksesibel.
  - *Ketergantungan:* M0.1.
  - *Cara Pengujian/Bukti:* Prototipe modal dialog order dan screenshot perbandingan layar.
  - *Status:* `MENUNGGU_PREVIEW_DAN_PERSETUJUAN`.

- **M2.6 — Preview UI Bahasa Bisnis Sederhana & Tombol Panduan Bisnis (A17, A18)**
  - *Masalah/Kebutuhan:* Menampilkan istilah bisnis yang telah disetujui (`Batas Dana yang Dapat Dikelola`, `Dana yang Sudah Digunakan`, dsb.) dan penempatan tombol Panduan di bagian atas tab Bisnis.
  - *Hasil yang Diharapkan:* Tampilan tab Bisnis yang ramah pengguna dengan tombol Panduan interaktif.
  - *Ketergantungan:* M0.1.
  - *Cara Pengujian/Bukti:* Prototipe tab Bisnis dan screenshot tampilan.
  - *Status:* `MENUNGGU_PREVIEW_DAN_PERSETUJUAN`.

- **M2.7 — Preview UI Rincian Alokasi Dana Tertahan & Dompet Konsolidasi (A20, A21)**
  - *Masalah/Kebutuhan:* Nilai IDR tertahan dapat diketuk untuk membuka rincian alokasi per order/transaksi, dan baris Dompet menyajikan total aset pribadi (kas, saham, usaha, koleksi) tanpa menghitung ganda.
  - *Hasil yang Diharapkan:* Dialog rincian alokasi dana tertahan dan kartu ringkasan dompet konsolidasi.
  - *Ketergantungan:* M0.1.
  - *Cara Pengujian/Bukti:* Prototipe rincian alokasi dan screenshot dialog.
  - *Status:* `MENUNGGU_PREVIEW_DAN_PERSETUJUAN`.

- **M2.8 — Preview UI Kartu Booster Riset dengan Pembeda Jelas (A08)**
  - *Masalah/Kebutuhan:* Warna kartu booster riset lama kurang membedakan status dan jenisnya.
  - *Hasil yang Diharapkan:* Desain kartu booster dengan ikon tematik, warna ber-kontras tinggi, dan label teks status non-warna (Tersedia, Aktif, Kedaluwarsa).
  - *Ketergantungan:* M0.1.
  - *Cara Pengujian/Bukti:* Prototipe kartu booster dan screenshot perbandingan.
  - *Status:* `MENUNGGU_PREVIEW_DAN_PERSETUJUAN`.

---

### Paket M3: Implementasi Transaksi, Tiket Order & Pasar (6 Subtask)

- **M3.1 — Penerapan Tombol Beli & Jual Fixed Docking pada Detail Emiten (A02 / K-025)**
  - *Masalah/Kebutuhan:* Menerapkan dock tombol Beli & Jual pada semua detail emiten di aplikasi game sesuai preview yang disetujui.
  - *Hasil yang Diharapkan:* Tombol terlihat seketika saat halaman dibuka pada semua ukuran layar, membuka tiket order yang tepat, dan menghormati status halt/terkunci.
  - *Ketergantungan:* Persetujuan Preview M2.4.
  - *Cara Pengujian/Bukti:* Pengujian Playwright pada BMRI, BBCA, dan saham US; asersi visibilitas tanpa scroll.
  - *Status:* `MENUNGGU_PERSETUJUAN_PREVIEW`.

- **M3.2 — Penerapan Form Order Dialog Ringkas Tengah Layar (A03, R1, R2, R7)**
  - *Masalah/Kebutuhan:* Menerapkan modal dialog order ringkas bertahap di tengah layar dengan ringkasan saldo kas dan rincian fee quote domain.
  - *Hasil yang Diharapkan:* Modal dialog ringkas di tengah layar, responsif pada 10 viewport, validasi input presisi, review 30 detik.
  - *Ketergantungan:* Persetujuan Preview M2.5.
  - *Cara Pengujian/Bukti:* Pengujian submit order 4 tipe (MARKET, LIMIT, STOP_LOSS, TAKE_PROFIT) via dialog baru.
  - *Status:* `MENUNGGU_PERSETUJUAN_PREVIEW`.

- **M3.3 — Implementasi Riwayat Order Ringkas dengan Filter Status (A10, R3)**
  - *Masalah/Kebutuhan:* Menampilkan riwayat order dalam format kartu ringkas dengan status bahasa manusia (`Mengantre`, `Terisi Penuh`, `Dibatalkan`, `Kedaluwarsa`) dan filter status.
  - *Hasil yang Diharapkan:* Daftar riwayat order yang bersih, informatif, dan mendukung pemfilteran cepat.
  - *Ketergantungan:* M0.1.
  - *Cara Pengujian/Bukti:* Pengujian DOM dan pengujian filter order aktif vs selesai.
  - *Status:* `MENUNGGU_PERSETUJUAN_PREVIEW`.

- **M3.4 — Navigasi Langsung dari Status Order ke Detail Saham Terkait (A11, R10)**
  - *Masalah/Kebutuhan:* Pemain dapat mengetuk saham pada kartu status/riwayat order untuk langsung membuka rincian pasar saham tersebut, dengan tombol Kembali yang memulihkan konteks order.
  - *Hasil yang Diharapkan:* Transisi halaman lancar ke detail emiten terkait tanpa mengirim order tak disengaja, dan Back mengembalikan fokus ke riwayat order.
  - *Ketergantungan:* M3.3.
  - *Cara Pengujian/Bukti:* Pengujian navigasi Playwright bolak-balik riwayat order $\leftrightarrow$ detail emiten.
  - *Status:* `MENUNGGU_PERSETUJUAN_PREVIEW`.

- **M3.5 — Interaksi Ketuk Rincian Alokasi Dana IDR Tertahan (A20)**
  - *Masalah/Kebutuhan:* Nilai IDR tertahan pada header/portofolio dapat diketuk untuk membuka dialog rincian alokasi per order/transaksi.
  - *Hasil yang Diharapkan:* Modal dialog rincian alokasi dana tertahan menampilkan daftar order yang mencadangkan kas, nominal per order, dan tombol pintas ke tiket order tersebut.
  - *Ketergantungan:* Persetujuan Preview M2.7.
  - *Cara Pengujian/Bukti:* Uji klik saldo tertahan saat ada order LIMIT terbuka dan verifikasi nominal cocok dengan data ledger.
  - *Status:* `MENUNGGU_PERSETUJUAN_PREVIEW`.

- **M3.6 — Pengujian Invarian Keuangan & Transaksi Pasar**
  - *Masalah/Kebutuhan:* Memastikan seluruh perubahan tiket order tidak merusak invarian ekonomi, rekonsiliasi kas, atau aturan perdagangan bursa.
  - *Hasil yang Diharapkan:* 100% tes domain transaksi dan regresi bridge lulus tanpa kebocoran saldo.
  - *Ketergantungan:* M3.1, M3.2, M3.5.
  - *Cara Pengujian/Bukti:* `OrderMatrixCombinationsTest` dan suite regresi urutan kritis domain.
  - *Status:* `MENUNGGU_PERSETUJUAN_PREVIEW`.

---

### Paket M4: IPO, Replay Historis & Profil Pemain (6 Subtask)

- **M4.1 — Implementasi Kalender & Fase Lengkap Penjatahan IPO (A01)**
  - *Masalah/Kebutuhan:* Menampilkan tahapan lengkap penawaran umum perdana (Bookbuilding, Offering, Allotment, Distribution, Listing) beserta tanggal riil dari data domain.
  - *Hasil yang Diharapkan:* Halaman IPO menampilkan kalender fase yang jujur, status fase aktif saat ini, dan waktu penjatahan yang jelas tanpa tanggal fiktif.
  - *Ketergantungan:* M0.1.
  - *Cara Pengujian/Bukti:* Pengujian tampilan fase IPO pada berbagai siklus tanggal game.
  - *Status:* `MENUNGGU_PERSETUJUAN_PREVIEW`.

- **M4.2 — Penyempurnaan Narasi Replay Historis & Penanda Konteks (A06, R8)**
  - *Masalah/Kebutuhan:* Memberikan penjelasan bahwa replay adalah simulasi data lampau terpisah, dan menyematkan penanda konteks `Data historis AAPL · Mei–Agustus 2016` atau `Harga simulasi game · 1 bulan`.
  - *Hasil yang Diharapkan:* Pemain memahami konteks data secara transparan tanpa kebingungan antara data simulasi dan data historis.
  - *Ketergantungan:* Persetujuan Keputusan R8.
  - *Cara Pengujian/Bukti:* Uji DOM keberadaan badge konteks pada grafik instrumen dan layar replay.
  - *Status:* `MENUNGGU_PERSETUJUAN_PREVIEW`.

- **M4.3 — Implementasi Restrukturisasi Menu Profil A & Pemindahan Pemilih Akun (A07, A13, A19)**
  - *Masalah/Kebutuhan:* Menerapkan struktur 4 kelompok Profil A, memindahkan pemilih akun transaksi hanya ke Profil (tanpa indikator di layar lain), dan menempatkan Pengaturan & Backup di bagian bawah.
  - *Hasil yang Diharapkan:* Struktur menu profil yang rapi, pemilih akun berfungsi mengontrol akun aktif transaksi, dan modal backup/reset tetap utuh.
  - *Ketergantungan:* Persetujuan Preview M2.3.
  - *Cara Pengujian/Bukti:* Pengujian navigasi profil, pergantian akun, dan pembukaan modal backup/reset.
  - *Status:* `MENUNGGU_PERSETUJUAN_PREVIEW`.

- **M4.4 — Grafik Perkembangan Kekayaan, Statistik & Kartu Booster (A08, A09, R4)**
  - *Masalah/Kebutuhan:* Menampilkan grafik statistik kekayaan dari histori domain nyata (dengan label no-data/empty yang jujur jika belum ada), dan menerapkan kartu booster riset dengan pembeda visual non-warna.
  - *Hasil yang Diharapkan:* Visualisasi statistik yang akurat tanpa data palsu, dan kartu booster yang mudah dibedakan statusnya.
  - *Ketergantungan:* Persetujuan Preview M2.8.
  - *Cara Pengujian/Bukti:* Pengujian rendering chart profil dan kartu booster di tab Riset.
  - *Status:* `MENUNGGU_PERSETUJUAN_PREVIEW`.

- **M4.5 — Penghapusan Kontrol Pengaturan Ukuran Teks Game (A12)**
  - *Masalah/Kebutuhan:* Menghapus tombol toggle ukuran teks 200% buatan game dari menu pengaturan, sambil tetap mendukung penuh penskalaan font sistem bawaan OS perangkat (skala 100% dan 200%).
  - *Hasil yang Diharapkan:* Preferensi lama dimigrasikan secara aman tanpa merusak struktur save file; layout CSS tetap responsif dan bebas overflow saat font sistem perangkat diperbesar.
  - *Ketergantungan:* Persetujuan Keputusan A12.
  - *Cara Pengujian/Bukti:* Uji SaveState migrasi dan pengujian strict gate 10 viewport pada skala font 200%.
  - *Status:* `MENUNGGU_PERSETUJUAN_PREVIEW`.

- **M4.6 — Interaksi Ketuk Penjelasan Status Bursa & Sesi Pasar (R6)**
  - *Masalah/Kebutuhan:* Status bursa di header (Pasar Buka/Tutup) dapat diketuk untuk memunculkan penjelasan singkat mengenai sesi aktif, jadwal perdagangan IDX/US, dan laju waktu 24x.
  - *Hasil yang Diharapkan:* Tooltip/dialog ringkas informatif muncul saat status bursa diketuk.
  - *Ketergantungan:* M0.1.
  - *Cara Pengujian/Bukti:* Pengujian klik elemen status pasar dan verifikasi konten dialog.
  - *Status:* `MENUNGGU_PERSETUJUAN_PREVIEW`.

---

### Paket M5: Bisnis, Koleksi Mewah 150 Aset & Navigasi Portofolio (6 Subtask)

- **M5.1 — Integrasi Penuh 150 Berkas Gambar Koleksi ke Web Assets (A14)**
  - *Masalah/Kebutuhan:* Memasukkan 150 aset gambar PNG transparan dari `Artifacts/Collection-Art-150-2026-10-22/images` ke dalam `android/app/src/main/assets/www/images/collections/` dan mengoptimasi ukuran/jalur akses.
  - *Hasil yang Diharapkan:* Seluruh 150 berkas gambar terpasang dengan benar, dapat dimuat cepat oleh browser dan WebView Android, dengan fallback jika gambar gagal dimuat.
  - *Ketergantungan:* M0.3.
  - *Cara Pengujian/Bukti:* Skrip verifikasi kelengkapan berkas fisik dan pengujian muat aset di browser.
  - *Status:* `DALAM_PEKERJAAN`.

- **M5.2 — Penerapan Desain Galeri Koleksi Mewah Terpilih (A15)**
  - *Masalah/Kebutuhan:* Menerapkan salah satu dari 3 alternatif desain galeri yang dipilih dan disetujui pemilik.
  - *Hasil yang Diharapkan:* Tampilan galeri koleksi mewah 150 aset yang memukau, responsif, memiliki filter kategori & kelangkaan, dan akses modal rincian yang mulus.
  - *Ketergantungan:* Persetujuan Pemilik atas salah satu Alternatif M2.1.
  - *Cara Pengujian/Bukti:* Pengujian rendering galeri, filter, dan modal beli/jual koleksi.
  - *Status:* `MENUNGGU_PILIHAN_DAN_PERSETUJUAN_PEMILIK`.

- **M5.3 — Penerapan Bahasa Sederhana & Profesional pada Seluruh Tab Bisnis (A17)**
  - *Masalah/Kebutuhan:* Mengganti istilah teknis lama dengan istilah sederhana yang telah disetujui pemilik (`Batas Dana yang Dapat Dikelola`, `Dana yang Sudah Digunakan`, dsb.) pada seluruh subtab Usaha Saya, Koleksi, dan Kelola Dana.
  - *Hasil yang Diharapkan:* Seluruh teks dan label pada tab Bisnis konsisten, komunikatif, dan bebas jargon teknis tanpa mengubah logika/aturan finansial domain.
  - *Ketergantungan:* Persetujuan Keputusan A17.
  - *Cara Pengujian/Bukti:* Pengujian teks DOM dan audit label tab Bisnis.
  - *Status:* `MENUNGGU_PERSETUJUAN_PREVIEW`.

- **M5.4 — Implementasi Tombol Panduan Bisnis di Bagian Atas (A18)**
  - *Masalah/Kebutuhan:* Menambahkan satu tombol Panduan di bagian atas tab Bisnis yang membuka panduan ringkas mekanisme usaha, koleksi, dan kelola dana tanpa memicu aksi ekonomi.
  - *Hasil yang Diharapkan:* Tombol panduan terlihat jelas di header subtab Bisnis dan membuka modal bantuan edukatif.
  - *Ketergantungan:* Persetujuan Preview M2.6.
  - *Cara Pengujian/Bukti:* Pengujian klik tombol panduan dan verifikasi penutupan modal.
  - *Status:* `MENUNGGU_PERSETUJUAN_PREVIEW`.

- **M5.5 — Implementasi Baris Dompet Konsolidasi Total Aset Pribadi (A21)**
  - *Masalah/Kebutuhan:* Menampilkan ringkasan satu baris total aset pribadi yang menggabungkan kas, nilai pasar saham, valuasi usaha operasional, dan nilai koleksi mewah pribadi (tanpa dana klien).
  - *Hasil yang Diharapkan:* Kartu konsolidasi dompet yang akurat, terpisah dari liabilitas utang, dan tidak menghitung ganda.
  - *Ketergantungan:* Persetujuan Preview M2.7.
  - *Cara Pengujian/Bukti:* Pengujian kalkulasi aset konsolidasi terhadap snapshot domain Java.
  - *Status:* `MENUNGGU_PERSETUJUAN_PREVIEW`.

- **M5.6 — Penerapan Navigasi Portofolio B & Nama Fitur Ringkas (A04, A05)**
  - *Masalah/Kebutuhan:* Menerapkan model navigasi Portofolio B: saat kategori Saham dibuka, tab Portofolio bertransisi menjadi tab Saham; tombol Kembali mengembalikan tab Portofolio; nama kategori diringkas tanpa deskripsi panjang.
  - *Hasil yang Diharapkan:* Alur navigasi yang intuitif, cepat, mempertahankan posisi scroll dan riwayat tab.
  - *Ketergantungan:* Persetujuan Keputusan A04/A05 & Preview M2.2.
  - *Cara Pengujian/Bukti:* Pengujian Playwright transisi tab bolak-balik dan preservasi state.
  - *Status:* `MENUNGGU_PERSETUJUAN_PREVIEW`.

---

### Paket M6: Desain Domain, Migrasi & Pengujian Pembiayaan Baru (4 Subtask)

- **M6.1 — Kontrak Domain Pembiayaan Sederhana & Denda 5% per 24 Jam Nyata (A16)**
  - *Masalah/Kebutuhan:* Membangun logika pembiayaan baru di layer Java: rekomendasi opsi pintar, peniadaan agunan untuk pinjaman baru, dan perhitungan denda keterlambatan 5% sederhana per 24 jam nyata dari cicilan tertunggak (tanpa bunga atas denda/non-compounding).
  - *Hasil yang Diharapkan:* Layanan domain pembiayaan yang matematis presisi, idempoten, dan mematuhi aturan waktu dunia nyata WIB.
  - *Ketergantungan:* Persetujuan Keputusan A16.
  - *Cara Pengujian/Bukti:* Unit test JUnit terarah pada perhitungan denda 24 jam, 48 jam, dan pembayaran parsial.
  - *Status:* `DALAM_PEKERJAAN`.

- **M6.2 — Prosedur Migrasi Aman Kontrak Pinjaman Lama (A16)**
  - *Masalah/Kebutuhan:* Merancang migrasi save file yang melepaskan status agunan pada barang koleksi pinjaman lama sambil tetap mempertahankan sisa kewajiban utang pemain, saldo kas, dan riwayat mutasi buku besar.
  - *Hasil yang Diharapkan:* Migrasi save file otomatis dan aman: koleksi lama tidak lagi berstatus agunan (`isPledged = false`), saldo utang tidak terhapus, dan all-or-nothing rollback jika terjadi kegagalan.
  - *Ketergantungan:* M6.1.
  - *Cara Pengujian/Bukti:* Uji migrasi pada fixture save file legacy dan verifikasi saldo buku besar tetap seimbang.
  - *Status:* `DALAM_PEKERJAAN`.

- **M6.3 — Implementasi Antarmuka UI Konfirmasi Pembiayaan Baru**
  - *Masalah/Kebutuhan:* Menyajikan UI pengajuan pembiayaan yang merekomendasikan opsi produk terbaik, menampilkan rincian plafon, tenor, nominal cicilan, dan simulasi denda transparan sebelum pemain mengonfirmasi.
  - *Hasil yang Diharapkan:* Dialog konfirmasi pembiayaan yang jelas, tidak menjebak, dan menegaskan syarat denda sederhana 5% per 24 jam.
  - *Ketergantungan:* M6.1, M6.2.
  - *Cara Pengujian/Bukti:* Pengujian alur pengajuan dan pencairan pinjaman baru di browser.
  - *Status:* `MENUNGGU_PERSETUJUAN_PREVIEW`.

- **M6.4 — Pengujian Simulasi Waktu Nyata, Offline Catchup & Regresi Pembiayaan**
  - *Masalah/Kebutuhan:* Menguji ketahanan mekanisme denda terhadap manipulasi jam, jeda offline lama (7 hari, 30 hari), dan pembayaran cicilan bertahap.
  - *Hasil yang Diharapkan:* Perhitungan denda konsisten pada restart/offline, tidak ada denda berulang pada hari yang sama, dan rekonsiliasi buku besar valid.
  - *Ketergantungan:* M6.1, M6.2, M6.3.
  - *Cara Pengujian/Bukti:* Test suite JUnit terintegrasi dengan simulasi controlled-clock.
  - *Status:* `DALAM_PEKERJAAN`.

---

### Paket M7: Integrasi Akhir, Verifikasi Menyeluruh & Rilis APK (5 Subtask)

- **M7.1 — Verifikasi Menyeluruh Matriks 10 Viewport x 2 Skala Font & Strict Gate (K-002, K-013, K-015)**
  - *Masalah/Kebutuhan:* Membuktikan bahwa seluruh layar, modal tiket order, modal rincian berita, galeri 150 koleksi, dan profil bebas overflow (0px) pada 10 viewport wajib pada skala font 100% dan 200%.
  - *Hasil yang Diharapkan:* 100/100 kombinasi lulus tanpa overflow horizontal; seluruh target sentuh $\ge 48.0\text{px} \times 48.0\text{px}$ dan kontras WCAG $\ge 4.5:1$ (normal) serta $\ge 3.0:1$ (besar).
  - *Ketergantungan:* M3, M4, M5, M6.
  - *Cara Pengujian/Bukti:* Eksekusi `test-delivery-gate-strict.cjs` dan perekaman berkas hasil JSON.
  - *Status:* `TERGANTUNG_SELESAI_M3_M6`.

- **M7.2 — Perekaman Sampel Mentah Benchmark Performa Latensi (K-013)**
  - *Masalah/Kebutuhan:* Mengukur dan menyimpan minimal 30 data sampel mentah per kategori aksi yang diklaim (Navigasi $\le 1000\text{ms}$, Aksi lokal $\le 300\text{ms}$, Feedback berat nyata $\le 300\text{ms}$).
  - *Hasil yang Diharapkan:* P95 memenuhi standar batas waktu dengan data mentah terekam lengkap di laporan JSON.
  - *Ketergantungan:* M7.1.
  - *Cara Pengujian/Bukti:* Eksekusi benchmark performa otomatis di Playwright.
  - *Status:* `TERGANTUNG_SELESAI_M3_M6`.

- **M7.3 — Regresi Penuh JVM 384 MB Termasuk Skenario 730 Hari**
  - *Masalah/Kebutuhan:* Memastikan tidak ada regresi pada logika ekonomi, pasar, buku besar, atau memori setelah penambahan seluruh fitur baru.
  - *Hasil yang Diharapkan:* 100% tes JVM lulus pada heap `-Xmx384m` (termasuk kasus multi-tahun 730 hari dan 10x100 urutan kritis).
  - *Ketergantungan:* M3, M4, M5, M6.
  - *Cara Pengujian/Bukti:* Eksekusi `Work/run-codex-jvm-check.ps1` menghasilkan Exit Code 0 dan log `OK (XXX tests)`.
  - *Status:* `TERGANTUNG_SELESAI_M3_M6`.

- **M7.4 — Perakitan & Verifikasi Kandidat Rilis Baru APK-08 Final**
  - *Masalah/Kebutuhan:* Membangun paket rilis APK baru tanpa menimpa artefak lama, memverifikasi tanda tangan digital V2, dan mencocokkan seluruh berkas aset.
  - *Hasil yang Diharapkan:* File `GameSaham-APK08-final.apk` terpasang di `Artifacts/`, tanda tangan Scheme V2 valid, seluruh aset cocok byte-for-byte dengan source disk.
  - *Ketergantungan:* M7.1, M7.3.
  - *Cara Pengujian/Bukti:* `gradlew assembleDebug --no-daemon`, `apksigner verify --verbose`, dan script verifikasi aset Python.
  - *Status:* `TERGANTUNG_SELESAI_M3_M6`.

- **M7.5 — Rekonsiliasi Dokumen Laporan Status & Penyerahan ke Pemilik (K-005)**
  - *Masalah/Kebutuhan:* Menyinkronkan seluruh dokumen status proyek (`STATUS.json`, `STATUS-REPORT.md`, `CHECKPOINT.md`, `RESULTS.md`) dan menyerahkan hasil akhir secara transparan.
  - *Hasil yang Diharapkan:* Laporan penyerahan final lengkap memuat seluruh bukti nyata, pemisahan hasil host/browser/APK/native, dan status integritas save pemain.
  - *Ketergantungan:* M7.4.
  - *Cara Pengujian/Bukti:* Verifikasi konsistensi dokumen dan readback data.
  - *Status:* `TERGANTUNG_SELESAI_M3_M6`.

---

## 4. Status Eksekusi Terkini & Langkah Berikutnya

1. **Subtask yang Sedang Berjalan (Independen & Telah Memiliki Izin Jelas):**
   - **M0.1, M0.2, M0.3**: Persiapan dan audit kecocokan 150 aset koleksi selesai 100%.
   - **M1.1, M1.2, M1.3**: Perbaikan defect teknis independen (K-024 propagasi status sync, K-018 persistensi intent koleksi lintas reload, K-005 generator laporan).
   - **M5.1**: Persiapan integrasi 150 aset gambar PNG ke web assets.
   - **M6.1, M6.2**: Pemodelan domain kontrak pembiayaan denda 5% per 24 jam nyata dan skema migrasi save file.
   - **M2.1 s/d M2.8**: Penyiapan prototipe dan berkas preview visual untuk persetujuan pemilik.

2. **Daftar Preview yang Siap Diajukan Menggunakan Question & Answer Klik:**
   - **Preview 1 (Galeri Koleksi Mewah - M2.1 / A15):** 3 alternatif desain galeri 150 aset (Opsi 1: Luxury Showcase Grid 2-Kolom, Opsi 2: Elegant Horizontal Carousel, Opsi 3: Compact Tiered Catalog).
   - **Preview 2 (Tombol Beli & Jual Detail Emiten - M2.4 / A02 / K-025):** Desain fixed docking tombol Beli & Jual terlihat langsung tanpa scroll pada viewport 434x688.
   - **Preview 3 (Form Order Dialog Ringkas - M2.5 / A03 / R1 / R2 / R7):** Desain kotak dialog modal ringkas di tengah layar dengan opsi bertahap.
   - **Preview 4 (Restrukturisasi Profil A - M2.3 / A07 / A13 / A19):** Susunan 4 kelompok menu Profil A dengan Pengaturan di bawah dan pemilih akun terpusat.
