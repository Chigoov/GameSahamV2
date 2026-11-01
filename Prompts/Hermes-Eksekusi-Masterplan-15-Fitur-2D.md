# Prompt Eksekusi Lengkap: Masterplan 15 Fitur (2D Portrait Android)
## Proyek: Game Saham Android (Repo: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`)

Kamu adalah AI Principal Engineer yang ditugaskan untuk mengeksekusi secara penuh seluruh rancangan dalam **Masterplan 15 Fitur Game Saham 2D Portrait**. Tugasmu adalah membangun dan menguji artefak kode nyata sampai selesai, bukan sekadar menulis ringkasan, membuat rencana ulang, atau menghasilkan kode pura-pura (*stubs/mocking*).

---

### ATURAN UTAMA & LARANGAN KERAS:
1. **DILARANG MERINGKAS ATAU MENGGABUNGKAN TASK SECARA ILEGAL:**
   Jangan memangkas, meringkas, atau melompati subtask apa pun. Setiap item dari ke-15 fitur wajib diimplementasikan secara utuh (*unbundled*), memiliki kode domain nyata, antarmuka nyata, dan pemeriksaan yang lulus verifikasi.
2. **DILARANG MENGAKHIRI TURN DENGAN JANJI TINDAKAN MASA DEPAN:**
   Jika suatu fitur atau langkah dapat dikerjakan, eksekusi sekarang menggunakan alatmu. Selesaikan tahap demi tahap secara mandiri hingga seluruh acceptance terpenuhi.
3. **KEDAULATAN LOGIKA TUNGGAL (C# DOMAIN):**
   Seluruh aturan bursa, formula harga, matching order, buku kas (*ledger*), saldo portofolio, utang bank, dan pendapatan pasif dikelola 100% di C# Domain. Kode JavaScript di UI murni sebagai penampil data dan penangkap gestur sentuh.
4. **BATAS REPO & PERTAHANKAN PERUBAHAN LOKAL:**
   Gunakan root absolut: `C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM` sebagai satu-satunya repo kerja. Jangan menyentuh repo lain. Jangan menghapus atau mengubah konfigurasi lokal yang sudah ada tanpa alasan terverifikasi.

---

### DOKUMEN RUJUKAN WAJIB (BACA SEBELUM CODING):
1. `Docs/MASTERPLAN_15_FITUR_2D_PORTRAIT.md` — Rujukan arsitektur, 15 fitur, dan skema SQLite.
2. `Docs/Product/01-Konsep-Game-2D-Portrait.md` & `Docs/Product/02-PRD-Game-Saham-2D-Portrait.md` — Konsep produk dan PRD.
3. `Docs/Design/03-Design-Brief-Game-Saham-2D-Portrait.md` — Panduan visual 2D Portrait, palet warna, dan aturan aksesibilitas.
4. `Artifacts/Collection-Art-150-2026-10-22/MANIFEST.csv` — Sumber metadata 150 aset koleksi galeri.

---

### CAKUPAN 15 FITUR YANG WAJIB SELESAI SECARA UTUH:

#### PILAR A: SISTEM PASAR & EKSEKUSI TRADING (Tab PASAR)
- [ ] **Fitur 1: Data Candlestick OHLCV (`MarketCandleDto`):** Riwayat lilin harga bursa (Open, High, Low, Close, Volume) terhitung di domain dan dirender menggunakan HTML5 Canvas ringan di UI vertikal.
- [ ] **Fitur 2: Sentimen Berita Makro (`MarketEvent`):** Event berita per sesi yang memiliki bobot pengali tren per sektor saham secara logis.
- [ ] **Fitur 3: Ledger Transaksi & Kalkulasi Portofolio:** Pencatatan immutable ledger beli/jual serta evaluasi transparan Realized P/L dan Unrealized P/L.
- [ ] **Fitur 4: Layar Perdagangan Vertikal (Order Book & Quick Ticket):** Tampilan order book bid/ask interaktif (klik harga antrean otomatis mengisi input) dan tombol porsi modal (25%, 50%, 75%, MAX).
- [ ] **Fitur 5: Penawaran Saham Perdana (IPO):** Alokasi pemesanan saham baru sebelum listing resmi di bursa.
- [ ] **Fitur 6: Aksi Korporasi Stock Split:** Mekanisme pemecahan nominal saham yang secara presisi mengalikan lot pemain dan membagi harga per lembar.

#### PILAR B: KEUANGAN, RISIKO & PROGRES PRIBADI (Tab ASET)
- [ ] **Fitur 7: Pinjaman Bank & Margin Trading:** Plafon utang modal (maks 50% net worth), bunga sesi, dan sistem peringatan Margin Call.
- [ ] **Fitur 8: Galeri 150 Aset Mewah Sultan:** Katalog lengkap 150 barang mewah (12 kategori, 5 kelangkaan) yang terhubung ke aset PNG $1254 \times 1254$ di `Artifacts/Collection-Art-150-2026-10-22/images`.
- [ ] **Fitur 9: Pilihan Realisme Pasar (*Market Difficulty*):** Mode Santai (ramah pemula) vs Mode Realistis Pro (biaya komisi, pajak, inflasi, halt).

#### PILAR C: IDENTITAS INVESTOR, MISI & PERSAINGAN (Tab BERANDA)
- [ ] **Fitur 10: Diagnosis DNA Investor (*Investor Archetype*):** Analisis statistik trading untuk menentukan titel profil (Scalper, Value Investor, dll.).
- [ ] **Fitur 11: Checklist Misi Harian (*Daily Quests*):** Sistem misi harian berbasis target yang memberikan reward kas modal dan reputasi.
- [ ] **Fitur 12: Sistem Rival NPC & Promosi Liga:** Klasemen liga mingguan dan komparasi persentase return terhadap Rival NPC.

#### PILAR D: KARIER PENGELOLA DANA, KANTOR & WARISAN (Tab FIRMA)
- [ ] **Fitur 13: Kerajaan Firma Investasi & Staf Kantor:** Upgrade kantor, rekrutmen staf (analis/marketing), dan skor kebahagiaan karyawan.
- [ ] **Fitur 14: Akun Klien Terpisah (AUM) & Biaya Pengelolaan:** Pemisahan saldo pribadi dan dana nasabah, fee manajemen, dan fee performa.
- [ ] **Fitur 15: Sistem Prestise Warisan (*Legacy Rebirth*):** Fitur pensiun konglomerat, reset aset dengan imbalan Poin Warisan Permanen.

---

### TAHAPAN PELAKSANAAN (ROADMAP EKSEKUSI):

#### TAHAP 1: INISIALISASI DATABASE SQLITE LOKAL & MODEL DATA
1. Buat koneksi SQLite lokal di proyek Android (.NET MAUI) menggunakan path penyimpanan lokal aman (`FileSystem.AppDataDirectory`).
2. Terapkan 8 skema tabel: `PlayerProfile`, `StockLedger`, `ActivePortfolio`, `MarketCandleHistory`, `BankLoan`, `InvestmentFirm`, `GalleryCatalog`, `PlayerGalleryOwnership`, dan `DailyQuests`.
3. Buat skrip seeder otomatis untuk memuat seluruh 150 metadata galeri dari `MANIFEST.csv` ke dalam tabel `GalleryCatalog`.
4. Jalankan pengujian baca/tulis SQLite lokal dan pastikan seluruh operasi database lulus tanpa galat.

#### TAHAP 2: INTI PERDAGANGAN, CANDLESTICK & EVENT PASAR (Tab PASAR)
1. Implementasikan `MarketCandleDto` di C# domain dan buat generator candlestick dari simulasi harga sesi.
2. Hubungkan jembatan `TradingWebBridge` untuk menyuplai data OHLCV, Order Book Bid/Ask, dan ticker aktif ke UI.
3. Bangun komponen Canvas grafik lilin responsif 2D Portrait di file web UI.
4. Bangun tiket order cepat dengan tombol persentase (25%, 50%, 75%, MAX) dan validasi saldo sebelum eksekusi.
5. Implementasikan event berita makro (`MarketEvent`), pemesanan saham IPO, dan aksi korporasi Stock Split.

#### TAHAP 3: PORTOFOLIO, PINJAMAN BANK & GALERI 150 ITEM (Tab ASET)
1. Tampilkan kartu ringkasan portofolio: kas tunai, total nilai saham, laba/rugi belum direalisasi (*unrealized*), dan laba bersih cair (*realized*).
2. Bangun modal pinjaman bank: pengajuan kredit, perhitungan akumulasi bunga per sesi, dan deteksi otomatis Margin Call.
3. Bangun layar Galeri Sultan yang menampilkan 150 item koleksi berdasarkan kategori (mobil, properti, jam, perhiasan, dll.) lengkap dengan gambar PNG resolusi tinggi dan tombol pembelian menggunakan saldo kas pemain.

#### TAHAP 4: AGENDA, MISI HARIAN, PROFIL DNA & LIGA (Tab BERANDA)
1. Bangun komponen checklist misi harian (3 misi acak per hari bursa) yang melacak progres otomatis dan tombol klaim hadiah.
2. Bangun algoritma analisis gaya transaksi pemain untuk menetapkan kartu DNA Investor di halaman profil.
3. Bangun papan peringkat liga mingguan dengan perbandingan performa pemain vs Rival NPC.

#### TAHAP 5: FIRMA DANA KELOLAAN, PRESTISE REBIRTH & VERIFIKASI AKHIR
1. Bangun sistem firma investasi: rekrut staf analis dan marketing, serta kelola dana klien terpisah (AUM).
2. Bangun sistem Rebirth Prestise: kalkulasi nilai pensiun, konversi aset menjadi Poin Warisan, dan pemilihan perk permanen.
3. Lakukan pengujian end-to-end pada orientasi 2D Portrait ponsel (Android): periksa fluiditas sentuhan ($\ge 48\text{ dp}$), kontras tema gelap, integritas database lokal, dan build APK Android.

---

### FORMAT LAPORAN KEMAJUAN (SETIAP SELESAI SUBTASK/TAHAP):
Laporankan secara ringkas dan faktual:
- **Progres:** X dari 5 Tahap Selesai (Fitur Aktif: ...)
- **Keluaran Nyata:** Berkas yang dibuat/dimodifikasi (sebutkan path absolutnya).
- **Pemeriksaan & Bukti:** Perintah uji yang dijalankan beserta hasil output riil (exit code 0 / PASS).
- **Hambatan:** Catat jika ada kendala teknis nyata, atau nyatakan "Tidak ada".
- **Langkah Berikutnya:** Tugas terdekat yang akan langsung dikerjakan.

---

MULAI SEKARANG DARI TAHAP 1: Siapkan database SQLite lokal, buat tabel, dan impor 150 katalog galeri ke dalam database. Jalankan eksekusi secara mandiri sampai tuntas!
