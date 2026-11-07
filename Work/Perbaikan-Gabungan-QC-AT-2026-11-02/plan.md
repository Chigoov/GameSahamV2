# Plan Induk Perbaikan Gabungan QC (AU01–AU55) dan AT (AT01–AT05, NV01)

**Tanggal:** 2 November 2026, Asia/Jakarta  
**Direktori Proyek:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`  
**Branch:** `codex/classic-ui-new-assets`  
**HEAD:** `9653f7a992405b4dfaf28acddbf1caf158d03957` (dengan perubahan lokal aktif)  
**Dokumen Referensi:**
- `AGENTS.md`
- `Work/Perbaikan-AU01-AU55-2026-11-02/plan.md`
- `Work/Perbaikan-AU01-AU55-2026-11-02/PANDUAN-PERBAIKAN-AU01-AU55.md`
- `Work/Perbaikan-AU01-AU55-2026-11-02/LAPORAN-AKHIR-AU01-AU55.md`
- `Work/Perbaikan-Audit-Tambahan-2026-11-02/plan.md`

---

## 1. Mandat & Kebijakan Perlindungan Data
1. **Proteksi Simpanan Pemain Asli:**
   - Berkas `Artifacts/LocalPreview/preview-save/save_game_v2.json` **HANYA DI-HASH**:
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

## 2. Struktur Milestone & Tahapan Kerja

### Bagian A: Perbaikan Hasil Pemeriksaan AU01–AU55 (QC01–QC06)
- **M01 (QC01):** Pemulihan kuota booster tanpa infinite loop saat booster tidak aktif / habis (`ResearchService.java`, `ResearchBooster.java`).
- **M02 (QC02):** Penegakan filter sesi bursa (IDX/US) dan halt pada pemrosesan katalis berita (`MarketEngine.java`, `TradingFacade.java`).
- **M03 (QC03):** Pelunasan dan preservasi tunggakan short borrow fee hingga lunas (`RiskComplianceService.java`, `ShortPosition.java`, `TradingFacade.java`).
- **M04 (QC04):** Integrasi manfaat booster bebas komisi broker pada transaksi pemain di Matching Engine (`MatchingEngine.java`, `TradingFacade.java`, `Order.java`).
- **M05 (QC05):** Pengaitan berita Whale dan rumor lanjutan ke event ekonomi riil simulasi (`ResearchService.java`, `MarketEvent.java`).
- **M06 (QC06):** Penagihan management fee reksa dana sesuai tarif katalog canonical 24 fund (`MutualFund.java`, `MutualFundManager.java`).

### Bagian B: Plan Tambahan AT01–AT05 dan Investigasi NV01
- **M07 (AT01):** Konsistensi pembulatan jaminan short dan preflight validasi sebelum mutasi saham lender/ledger (`RiskComplianceService.java`).
- **M08 (AT02):** Kesempatan eksekusi order rebalance dana indeks pada sesi buka bursa sah tanpa kedaluwarsa prematur (`TradingFacade.java`, `MutualFund.java`).
- **M09 (AT03):** Kontrol sesi bursa dan halt pada pembukaan serta penutupan posisi short (`TradingFacade.java`, `RiskComplianceService.java`).
- **M10 (AT05):** Idempotensi retry transfer holding dan penolakan perubahan payload pada requestId yang sama (`CompanyManager.java`).
- **M11 (AT04):** Validasi 4 sektor unik holding company berbasis sektor canonical perusahaan (`CompanyManager.java`).
- **M12 (NV01):** Investigasi potensi race condition dua penulis SaveManager pada transisi lifecycle Android (`SaveManager.java`, `MainActivity.java`, `TradingWebBridge.java`).

### Verifikasi Gabungan & Penyerahan
- **M13 (P08-Final):** Suite regresi lengkap (QC + AT + Paket P01-P07 terdahulu), build APK Android debug offline, verifikasi bit-perfect hash save pemain asli, dan penyusunan `LAPORAN-HASIL.md`.

---

## 3. Matriks Status Eksekusi

| ID | Deskripsi Singkat | Lokasi Utama | Status | Bukti / Catatan |
|---|---|---|---|---|
| **M00** | Baseline, Git status, Hash Save Asli, Setup Plan | `Work/Perbaikan-Gabungan-QC-AT-2026-11-02/` | **DIPERBAIKI_DAN_DIUJI_HOST** | Hash `59B41E24...` bit-perfect |
| **QC01** | Booster restore loop fix | `ResearchService.java`, `ResearchBooster.java` | **DIPERBAIKI_DAN_DIUJI_HOST** | `PackageQCFixesTest.testQC01` PASS |
| **QC02** | Katalis berita patuh sesi bursa | `MarketEngine.java`, `TradingFacade.java` | **DIPERBAIKI_DAN_DIUJI_HOST** | `PackageQCFixesTest.testQC02` PASS |
| **QC03** | Preservasi tunggakan fee short sampai lunas | `RiskComplianceService.java`, `ShortPosition.java` | **DIPERBAIKI_DAN_DIUJI_HOST** | `PackageQCFixesTest.testQC03` PASS |
| **QC04** | Booster bebas komisi teraplikasi riil | `MatchingEngine.java`, `TradingFacade.java`, `Order.java` | **DIPERBAIKI_DAN_DIUJI_HOST** | `PackageQCFixesTest.testQC04` PASS |
| **QC05** | Warta Whale terikat event ekonomi riil | `ResearchService.java`, `MarketEvent.java` | **DIPERBAIKI_DAN_DIUJI_HOST** | `PackageQCFixesTest.testQC05` PASS |
| **QC06** | Management fee fund ikuti katalog | `MutualFund.java`, `MutualFundManager.java` | **DIPERBAIKI_DAN_DIUJI_HOST** | `PackageQCFixesTest.testQC06` PASS |
| **AT01** | Pembulatan jaminan short & preflight | `RiskComplianceService.java` | **DIPERBAIKI_DAN_DIUJI_HOST** | `PackageATFixesTest.testAT01` PASS |
| **AT02** | Rebalance fund tidak kedaluwarsa prematur | `TradingFacade.java`, `MutualFund.java` | **DIPERBAIKI_DAN_DIUJI_HOST** | `PackageATFixesTest.testAT02` PASS |
| **AT03** | Buka/tutup short patuh sesi & halt | `TradingFacade.java`, `RiskComplianceService.java` | **DIPERBAIKI_DAN_DIUJI_HOST** | `PackageATFixesTest.testAT03` PASS |
| **AT05** | Idempotensi transfer holding | `CompanyManager.java` | **DIPERBAIKI_DAN_DIUJI_HOST** | `PackageATFixesTest.testAT05` PASS |
| **AT04** | Validasi 4 sektor holding canonical | `CompanyManager.java` | **DIPERBAIKI_DAN_DIUJI_HOST** | `PackageATFixesTest.testAT04` PASS |
| **NV01** | Investigasi race condition SaveManager | `SaveManager.java`, `TradingWebBridge.java` | **DIPERBAIKI_DAN_DIUJI_HOST** | `PackageATFixesTest.testNV01` PASS |
| **M13** | Regresi penuh, build APK & penyerahan laporan | Seluruh modul | **DIPERBAIKI_DAN_DIUJI_HOST** | 47/47 tests PASS, APK 15.63 MB built |
