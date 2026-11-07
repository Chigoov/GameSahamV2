# Plan Eksekusi Perbaikan Lanjutan QC & AT (R01–R07) Game Saham 2D Portrait

**Tanggal:** 3 November 2026, Asia/Jakarta  
**Direktori Proyek:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`  
**Branch Git:** `codex/classic-ui-new-assets`  
**HEAD:** `9653f7a992405b4dfaf28acddbf1caf158d03957` (dengan perubahan lokal aktif dipertahankan)  
**Dokumen Referensi:**
- `AGENTS.md`
- `Work/Perbaikan-Gabungan-QC-AT-2026-11-02/plan.md`
- `Work/Perbaikan-Gabungan-QC-AT-2026-11-02/LAPORAN-HASIL.md`
- `Work/Perbaikan-Audit-Tambahan-2026-11-02/plan.md`

---

## 1. Kebijakan Kepatuhan & Perlindungan Data Pemain
1. **Proteksi Simpanan Pemain Asli:**
   - Berkas `Artifacts/LocalPreview/preview-save/save_game_v2.json` **HANYA BOLEH DI-HASH**:
     `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`.
   - Dilarang membaca, menyalin, memuat ke aplikasi, memigrasikan, atau memakainya sebagai fixture tes.
   - Semua pengujian menggunakan akun QA dan save QA sementara yang diisolasi di `build/tmp-*`.
2. **Kedaulatan Logika Domain Java:**
   - Logika finansial, pasar, waktu 48× (1 hari = 30 menit nyata), matching engine, dan penyimpanan berada di domain Java backend.
   - Transaksi pemain patuh All-Or-None (AON) dan anti-self-trade.
   - Tanpa refactor massal, penambahan dependensi baru, atau perubahan balancing ekonomi sepihak.
3. **Standar Status Bukti:**
   - `PENDING`: Belum dikerjakan.
   - `IN_PROGRESS`: Sedang dalam analisis/perbaikan/pengujian.
   - `DIPERBAIKI_DAN_DIUJI_HOST`: Perbaikan selesai dan diverifikasi lulus pada test suite host Java.
   - `DIVERIFIKASI_BROWSER`: Khusus pengujian komponen web di browser/Playwright.
   - `DIVERIFIKASI_ANDROID`: Khusus bila pengujian perangkat fisik/emulator Android benar-benar dilakukan.
   - `SUDAH_TERTUTUP_OLEH_PERBAIKAN_LAIN`: Terbukti telah ditangani oleh perbaikan terdahulu.
   - `DITOLAK_DENGAN_BUKTI`: Temuan dibuktikan tidak valid berdasarkan logika produksi.
   - `NEEDS_VALIDATION`: Hipotesis yang memerlukan investigasi reproduksi sebelum tindakan.
   - `MENUNGGU_KEPUTUSAN`: Memerlukan keputusan produk/pemilik yang belum disahkan.

---

## 2. Rincian Sasaran Kerja per ID

### R01: Kuota Booster pada Order yang Terisi Belakangan (Resting Order)
- **Tujuan:** Menghubungkan hak pembebasan komisi booster dengan konsumsi kuota yang tepat saat order terisi, baik terisi langsung (*taker*) maupun terisi belakangan dari buku antrean (*maker* oleh NPC/katalis). Order yang antre sesudah kuota habis tidak boleh menikmati diskon liar, dan order batal/kedaluwarsa tidak boleh mengurangi kuota.
- **Lokasi Utama:** `core/facade/TradingFacade.java`, `core/trading/MatchingEngine.java`, `core/trading/Order.java`, `core/trading/OrderManager.java`.

### R02: Keterjangkauan Pembuatan Holding Melalui Jalur Produksi
- **Tujuan:** Memastikan pemain memiliki jalur sah yang lengkap untuk membentuk holding 4 sektor (`FINANCIAL`, `ENERGY`, `CONSUMER`, `TECHNOLOGY`), memeriksa ketersediaan opsi pendirian usaha di antarmuka/domain, serta menyelaraskan pemetaan kategori usaha kanonikal tanpa memalsukan label.
- **Lokasi Utama:** `core/business/CompanyManager.java`, `core/business/OperatingCompany.java`, `assets/www/app.js`.

### R03: Dasar Penghitungan Biaya Sewa Short Setelah Penambahan Tunggakan
- **Tujuan:** Memisahkan nilai pokok pinjaman saham (*borrowed shares liability*) dari tunggakan sewa (*accrued unpaid fee*). Biaya sewa harian hanya boleh dikenakan atas nilai saham yang sedang dipinjam, bukan dikenakan bunga/bunga berbunga di atas tunggakan, serta biaya sewa harus Rp 0 jika sisa pinjaman saham sudah 0 lembar.
- **Lokasi Utama:** `core/risk/ShortPosition.java`, `core/risk/RiskComplianceService.java`, `core/facade/TradingFacade.java`.

### R04: Validasi & Penanganan Risiko Penulisan Save Lama pada Lifecycle (NV01)
- **Tujuan:** Menyusun pengujian deterministik dua penulis dengan snapshot berbeda untuk memvalidasi apakah worker lama yang tertahan dapat menimpa save baru setelah Activity berpindah. Jika terbukti, tetapkan kepemilikan generasi penulisan (*save version/epoch fence*) yang menolak commit dari pemilik usang.
- **Lokasi Utama:** `core/storage/SaveManager.java`, `mobile/TradingWebBridge.java`, `mobile/MainActivity.java`.

### R05: Kelayakan Kejadian Ekonomi untuk Berita Whale
- **Tujuan:** Memastikan warta Whale dan rumor lanjutan hanya terbit jika terdapat peristiwa pasar (`MarketEvent`) yang sah, relevan waktu, terbit, dan tepat sasaran sektor/emitennya tanpa memilih saham acak sembarangan saat sektor tidak cocok.
- **Lokasi Utama:** `core/research/ResearchService.java`, `core/research/MarketEvent.java`.

### R06: Pembuktian Eksekusi Rebalance Fund AT02 Hingga Transaksi Selesai
- **Tujuan:** Membuktikan alur penuh rebalance dana indeks: dari cutoff 16:00 WIB, melewati tengah malam tanpa kedaluwarsa, masuk sesi buka bursa pagi berikutnya, mencocokkan dengan kuotasi likuiditas lawan, hingga saham dan kas pool benar-benar berpindah secara kanonikal.
- **Lokasi Utama:** `core/facade/TradingFacade.java`, `core/funds/MutualFund.java`, `core/trading/MatchingEngine.java`.

### R07: Regresi Lengkap, Build APK Offline, Log & Koreksi Laporan
- **Tujuan:** Menjalankan seluruh test suite regresi yang terdampak, memvalidasi build APK debug Android secara offline, memverifikasi hash SHA-256 save pemain tetap identik, mencatat seluruh log perintah dan hasil nyata, serta menyusun koreksi laporan sebelumnya.
- **Lokasi Utama:** `Work/Perbaikan-Lanjutan-QC-AT-2026-11-03/logs/`, `Work/Perbaikan-Lanjutan-QC-AT-2026-11-03/LAPORAN-HASIL.md`, `Work/Perbaikan-Lanjutan-QC-AT-2026-11-03/KOREKSI-LAPORAN-SEBELUMNYA.md`.

### R08: Kompatibilitas Pembacaan Katalog Koleksi pada Android API 26–32
- **Tujuan:** Mengganti pemanggilan `InputStream.readAllBytes()` (hanya tersedia di API 33+) dengan pembacaan buffer byte stream standar berukuran 8KB yang kompatibel dengan minimum Android API 26, memastikan seluruh 150 item koleksi termuat utuh tanpa `NoSuchMethodError`, serta menutup stream secara aman.
- **Lokasi Utama:** `core/collection/CollectionManager.java`, `core/market/HistoricalDatasetLoader.java`.

### R09: Penuntasan AU06 pada Pergantian Hari Otomatis dan Pemulihan Indeks Save/Reload
- **Tujuan:** Menyelaraskan seluruh pergantian hari bursa (midnight IDX, 04:00 WIB US, dan morning rollover) agar memindahkan acuan indeks (`previousCloseValue`) tepat sekali bersama instrumennya, serta mengekspor dan memulihkan state indeks (`currentValue`, `previousCloseValue`) pada penyimpanan katalog agar nilai indeks tidak melompat kembali ke 1000/basis awal.
- **Lokasi Utama:** `core/market/MarketIndex.java`, `core/market/InstrumentCatalog.java`, `core/facade/TradingFacade.java`.

---

## 3. Matriks Status Eksekusi

| ID | Sasaran Perbaikan | Status | Bukti / Catatan |
|---|---|---|---|
| **M00** | Baseline & Verifikasi Lingkungan | **DIPERBAIKI_DAN_DIUJI_HOST** | Branch & SHA-256 verified |
| **R01** | Kuota booster pada resting order fill | **DIPERBAIKI_DAN_DIUJI_HOST** | `PackageR01ToR06VerificationTest.testR01` PASS |
| **R02** | Keterjangkauan holding jalur produksi | **DIPERBAIKI_DAN_DIUJI_HOST** | `PackageR01ToR06VerificationTest.testR02` PASS, UI Preview updated |
| **R03** | Basis biaya sewa short tanpa bunga atas tunggakan | **DIPERBAIKI_DAN_DIUJI_HOST** | `PackageR01ToR06VerificationTest.testR03` PASS |
| **R04** | Validasi & penanganan urutan tulis save (NV01) | **NEEDS_VALIDATION** (Host: `DIPERBAIKI_DAN_DIUJI_HOST`) | `PackageR01ToR06VerificationTest.testR04` PASS; Android device pending |
| **R05** | Kelayakan peristiwa ekonomi warta Whale | **DIPERBAIKI_DAN_DIUJI_HOST** | `PackageR01ToR06VerificationTest.testR05` PASS |
| **R06** | Pembuktian transaksi rebalance fund pagi | **DIPERBAIKI_DAN_DIUJI_HOST** | `PackageR01ToR06VerificationTest.testR06` PASS |
| **R07** | Regresi penuh, build APK & penyerahan laporan | **DIPERBAIKI_DAN_DIUJI_HOST** | 58/58 tests PASS, APK 15.65 MB built |
| **R08** | Kompatibilitas loader koleksi Android API 26–32 | **DIPERBAIKI_DAN_DIUJI_HOST** | `PackageR08ToR09VerificationTest.testR08` PASS |
| **R09** | Penuntasan AU06 rollover bursa & save/reload indeks | **DIPERBAIKI_DAN_DIUJI_HOST** | `PackageR08ToR09VerificationTest.testR09` PASS |
