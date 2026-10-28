# Panduan Uji Bermain Android (Kandidat Master Playtest 22 Oktober 2026)
**Paket**: `GameSaham-v1.0.0-candidate-playtest.apk`  
**Lokasi**: `Artifacts/Master-Lanjutan-2026-10-22/apk/GameSaham-v1.0.0-candidate-playtest.apk`  
**Ukuran File**: 961.143 bytes (938 KB)  
**SHA-256**: `c2da7881c536e1927d72bbcd4f6f648c41992bb7fe8b3b6300a69f1f0644f6bb`  
**Status Pengujian Native HP**: `NOT_TESTED` (Disiapkan untuk pengujian langsung oleh pemilik pada perangkat fisik)

---

## 1. Identifikasi APK & Prosedur Pemasangan

### A. Pemasangan Pertama / Update Normal
1. Salin file `GameSaham-v1.0.0-candidate-playtest.apk` ke penyimpanan perangkat Android Anda (melalui kabel USB, Google Drive, atau WhatsApp/Telegram dokumen).
2. Buka file manager di HP, ketuk APK, dan pilih **Install** atau **Update**.
3. Jika muncul dialog *Play Protect* ("Aplikasi dari sumber tidak dikenal"), pilih **Tetap Install (Install anyway)** karena APK ini ditandatangani menggunakan sertifikat debug Android standar.

### B. PERHATIAN: Penanganan Bila Muncul "Aplikasi Tidak Terpasang" / "Signature Mismatch"
- Jika HP Anda sebelumnya sudah memiliki versi terpasang dengan kunci signing berbeda, Android akan menolak update langsung.
- **JANGAN langsung uninstall aplikasi lama jika ingin mempertahankan save progres lokal Anda!**  
  *Penting*: Menghapus aplikasi (Uninstall) pada Android akan **menghapus seluruh file data internal dan save game** yang tersimpan di `/data/data/com.tradingglobal3d.mobile/`.
- Bila progres pada HP lama berharga, cadangkan progres terlebih dahulu jika memungkinkan, atau pastikan siap memulai profil bermain baru sebelum memilih uninstall versi lama.

---

## 2. Fitur Baru yang Siap Diuji (Revisi Pemilik T1–T11)

1. **Waktu Permainan Santai (1 Hari = 15 Menit Nyata)**:
   - Waktu normal 1× kini berlangsung santai: 1 hari game penuh (24 jam) setara 15 menit waktu nyata (900 detik).
   - Di latar belakang / aplikasi ditutup, waktu berjalan stabil 1 hari game per 1 jam nyata (3.600 detik).
2. **Kontrol "Atur Waktu" & Label Akun**:
   - Tombol di Beranda bertuliskan **"Atur Waktu"** (bukan "Buka briefing"). Menampilkan tanggal & sesi pasar, status waktu (berjalan/dijeda), dan opsi *Mulai*, *Jeda*, atau *Lewati 1 Hari*.
   - Pemilih akun di bilah atas menampilkan konteks jelas: **"Akun: Pribadi"** atau **"Akun: Klien"**.
3. **Kabar Pasar Hari Ini di Beranda**:
   - Modul berita di Beranda dilengkapi filter sentimen: **Semua**, **▲ Kabar Baik** (hijau), dan **▼ Kabar Buruk** (merah).
   - Ketuk kartu berita untuk membaca rincian artikel dan melihat emiten/sektor terdampak.
4. **Navigasi Bisnis & Kategori Koleksi**:
   - Tab Bisnis memiliki 3 subnavigasi: **🏢 Usaha Saya**, **💎 Koleksi**, dan **💼 Kelola Dana**.
   - Menu Koleksi memiliki 5 filter kategori: **Semua**, **🚗 Kendaraan**, **🏰 Properti**, **💍 Perhiasan**, dan **⌚ Seni & Jam Tangan**, serta filter kepemilikan (**Semua, Dimiliki, Belum Dimiliki**).
5. **Grafik D1 Langsung Tampil & Profil Emiten**:
   - Buka Detail Saham: grafik harian (**D1**) langsung tampil dengan garis/lilin harga dan skala terbaca (tidak ada lagi kotak kosong *"Belum ada transaksi"*).
   - Tersedia sakelar **Candle** vs **Garis** dan tombol **Tabel OHLCV Alternatif**.
   - Bagian **"Tentang Perusahaan"** menyajikan profil resmi emiten, sektor, deskripsi bisnis faktual 1–3 kalimat, dan tautan situs resmi.
6. **Bahasa Bersih & Terjemahan Status**:
   - Status order di receipt kini bersih dalam bahasa Indonesia: **"Terisi penuh"**, **"Mengantre"**, atau **"Dibatalkan"** tanpa kode raw enum seperti `FILLED` atau `D10`.
7. **Kurs Acuan Tetap Rp 15.800/USD**:
   - Nilai acuan terkunci pada Rp 15.800/USD, dan baris "Kurs valuasi USD/IDR" disembunyikan dari ringkasan Portofolio agar tampilan bersih.

---

## 3. Alur Pengujian Bermain Bertahap (Step-by-Step Playtest)

### Tahap 1: Memulai Permainan & Navigasi 5 Tab
1. Buka aplikasi **Game Saham** pada HP Anda.
2. Pastikan tampilan langsung mengunci orientasi **Portrait (Tegak)** secara kokoh.
3. Periksa bilah navigasi bawah (5 Tab): **Beranda**, **Pasar**, **Aset**, **Firma**, dan **Riset**.
4. Ketuk setiap tab secara bergantian.
   - *Ekspektasi*: Transisi tab instan tanpa lag; bilah navigasi bawah tetap terlihat dan tidak tertutup keyboard atau konten scrolling.

### Tahap 2: Pencarian & Pembelian Saham IDX (BBCA / ASII)
1. Buka tab **Pasar**.
2. Ketuk bilah pencarian saham, ketik `ASII` atau `BBCA`.
   - *Ekspektasi*: Keyboard virtual muncul tanpa memotong daftar saham; daftar menyaring emiten secara real-time.
3. Ketuk kartu emiten untuk membuka **Detail Saham**.
   - *Ekspektasi*: Status bursa menampilkan `NORMAL`, order book 10 tingkat terlihat, dan grafik interaktif dapat diubah jenisnya (Garis / Candle / Tabel).
4. Ketuk tombol hijau **BELI**.
5. Masukkan kuantitas (misal: `1` atau `2` unit; 1 unit = 100 lembar untuk IDX).
6. Ketuk **Tinjau Order** lalu **Kirim Order**.
   - *Ekspektasi*: Toast notifikasi muncul dengan teks akurat (misal: *"Membeli 100 saham IDX:ASII"*, bukan 10.000); receipt menampilkan status `FILLED`.

### Tahap 3: Perdagangan Saham US & Konversi Valuta (FX)
1. Buka tab **Aset (Portofolio)**.
2. Buka bagian rincian dompet, ketuk tombol atau alur **Konversi Valuta (FX)** IDR ke USD (misal tukar Rp 5.000.000 menjadi USD).
   - *Ekspektasi*: Saldo USD bertambah secara live di layar tanpa reload halaman.
3. Buka tab **Pasar**, ubah filter bursa menjadi **US**, pilih `AAPL`.
4. Beli `2` saham AAPL (1 unit = 1 lembar).
   - *Ekspektasi*: Pembelian berhasil diproses dalam denominasi USD; notifikasi toast menyebutkan *"2 saham US:AAPL"*.

### Tahap 4: Penjualan Sebagian & Penjualan Habis (0 Saham)
1. Buka tab **Aset**, ketuk posisi saham Anda untuk membuka **Detail Posisi**.
2. Ketuk **Jual**, lalu jual sebagian lembar saham.
   - *Ekspektasi*: Kuantitas di modal langsung berkurang secara live in-place.
3. Buka kembali Detail Posisi dan jual seluruh sisa lembar saham sampai habis.
   - *Ekspektasi*: Layar Detail Posisi tidak crash; angka kuantitas berubah menjadi `0 saham`; metric hero menampilkan `Rp 0 (Posisi ditutup)` untuk IDX atau `$0.00 (Posisi ditutup)` untuk US; laba/rugi terealisasi kotor tetap ditampilkan utuh.

### Tahap 5: Bisnis (Firma) & Pergantian Hari
1. Buka tab **Firma**. Jika belum memiliki perusahaan, dirikan perusahaan baru (misal modal Rp 25.000.000).
2. Periksa saldo kas perusahaan dan lakukan transfer modal ke rekening pribadi.
   - *Ekspektasi*: Uang pribadi dan uang perusahaan terpisah secara tegas. Uang pribadi bertambah persis sebesar nominal yang ditarik dari kas perusahaan.
3. Lakukan **Skip Hari (Lewati 24 Jam)** via menu kontrol waktu di Beranda / Pengaturan.
   - *Ekspektasi*: Hari kalender game berganti; pendapatan/biaya operasional perusahaan dibukukan harian; laporan laba rugi perusahaan bertambah.

### Tahap 6: Simpan, Tutup Aplikasi, Background, dan Pemulihan (Resume)
1. Buka menu Pengaturan atau ketuk **Simpan Progres**.
2. Tekan tombol **Home** di HP (aplikasi masuk ke background). Tunggu 10–30 detik, lalu buka kembali aplikasi via task switcher.
   - *Ekspektasi*: State permainan pulih instan tanpa reset; waktu dan posisi tetap utuh.
3. Tutup paksa aplikasi (Force close / swipe away dari Recent Apps).
4. Buka kembali aplikasi Game Saham dari app drawer.
   - *Ekspektasi*: Permainan memuat progres terakhir secara otomatis (kas, saldo USD, posisi, perusahaan tetap sama).

### Tahap 7: Uji Tombol Back Fisik & Keyboard
1. Buka modal alur (misal Detail Saham atau Tinjau Order).
2. Tekan tombol **Back fisik/gesture Android** HP Anda.
   - *Ekspektasi*: Modal tertutup dengan rapi dan halaman utama dapat diklik kembali (tidak terkunci / tidak freeze).
3. Buka input pencarian, ketik teks, lalu tekan tombol Back.
   - *Ekspektasi*: Keyboard virtual tertutup tanpa menutup layar aplikasi secara tidak sengaja.

---

## 3. Format Pelaporan Bug / Kendala

Bila menemukan kendala saat bermain di HP, silakan catat dan kirimkan format berikut:
- **Model HP & Versi Android**: (contoh: *Xiaomi Redmi Note 12 / Android 13*)
- **Langkah yang Dilakukan**: (contoh: *Buka tab Pasar -> klik beli ASII -> klik kirim order*)
- **Perilaku yang Terjadi (Bug)**: (contoh: *Tombol beli tidak merespons sentuhan / teks terpotong*)
- **Perilaku yang Diharapkan**: (contoh: *Muncul layar konfirmasi order*)
- **Tangkapan Layar (Screenshot)**: Sertakan tangkapan layar area yang bermasalah.
