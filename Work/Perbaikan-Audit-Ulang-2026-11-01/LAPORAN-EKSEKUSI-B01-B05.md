# LAPORAN EKSEKUSI REMEDIASI BACKLOG REVIEW B01–B05
**Tanggal:** 2 November 2026  
**Target Proyek:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`  
**Referensi:** `AGENTS.md`, `Work/Perbaikan-Audit-Ulang-2026-11-01/` (`plan.md`, `BACKLOG-REVIEW.md`, `MATRIKS-CAKUPAN.md`, `HASIL-VERIFIKASI.md`, `plan-lanjutan-b01-b05.md`)  
**Status Eksekusi:** SELESAI (8/8 Milestone Terpenuhi pada Lingkup Host & Build)

---

## 1. Verifikasi Baseline & Integritas Lingkungan

| Parameter | Nilai / Hash | Status |
|---|---|---|
| **Save Pemain Asli** | `Artifacts/LocalPreview/preview-save/save_game_v2.json` | TERLINDUNGI |
| **SHA-256 Save Pemain** | `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED` | COCOK 100% (UNTOUCHED) |
| **Katalog Bursa (Prod vs QA)** | `android/app/src/main/assets/data/market_universe.json` | IDENTIK (30 instrumen) |
| **Java JDK Runtime** | OpenJDK 21.0.6 (Android Studio JBR: `/c/Program Files/Android/Android Studio/jbr`) | TERVERIFIKASI |
| **Android SDK / Gradle** | Android API 36 / Gradle 8.14.3 / AGP 8.13.0 | TERVERIFIKASI |
| **Paket APK Android Debug** | `android/app/build/outputs/apk/debug/app-debug.apk` (15,47 MB) | BUILD SUCCESSFUL |

---

## 2. Matriks Eksekusi Temuan Backlog Review B01–B05

| ID | Deskripsi Temuan | Status | File & Baris yang Diubah | Hasil Pengujian & Bukti Nyata |
|---|---|---|---|---|
| **B01** | Replenishment bot ritel (`ACC-NPC-RETAIL-01`) menambah saham & kas tanpa event suplai atau ledger mutasi | SELESAI & TERVERIFIKASI | `android/app/src/main/java/com/tradingglobal3d/core/npc/NpcAgent.java:60–100, 226–230`, `TradingFacade.java:1672–1676` | Replenishment retail disalurkan resmi melalui `ensureLiquidity`: tambahan saham dicatat dengan `instrument.applyShareSupplyEvent("RETAIL-SUPPLY-...", ...)` dan modal kas eksternal dicatat di `ledger.recordMutation("RETAIL-CAPITAL-...", ..., LedgerEntryType.LIQUIDITY_CAPITAL, ...)`. Mutasi unrecorded di `generateOrders` dihapus. Diuji via `BacklogReviewB01B05Test.testB01_RetailReplenishmentRecordedViaSupplyEventAndLedger` (**PASS**). |
| **B02** | Kuotasi suku bunga pinjaman (`currentMacroLoanRateAnnual / 365`) berbeda dari rate statis kontrak pinjaman (`DAILY_RATE` 0.05%) | SELESAI & TERVERIFIKASI | `android/app/src/main/java/com/tradingglobal3d/core/loans/LoanContract.java:85–118, 444, 475`, `InstallmentLoanManager.java:224–240` | `LoanContract` menerima `dailyInterestRate` pada konstruktor (dihitung dari rate makro tahunan saat pencairan / 365 / 100) dan menyimpannya pada `exportState`/`restoreFromState`. Bunga kuotasi, estimasi angsuran, dan bunga harian riil cocok 100%. Kompatibilitas mundur save lama terjaga dengan fallback default ke `DAILY_RATE`. Diuji via `BacklogReviewB01B05Test.testB02_LoanQuoteAndContractRateMatchExactly` (**PASS**). |
| **B03** | Rumor lanjutan Whale (`dayIn14 == 9`) memilih emiten kalender acak dan persentase 0.42 tanpa mengaitkan ke rumor Whale asal | SELESAI & TERVERIFIKASI | `android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java:680–740` | Rumor lanjutan menelusuri secara deterministik warta Whale asal sebelumnya (`cand.getPublishedAt() < timestamp`), menggunakan emiten yang sama persis, dan menetapkan persentase target tepat 2× dari rumor asal (`originPct * 2.0`). Tidak memaksakan rumor lanjutan jika warta asal tidak ada. Diuji via `BacklogReviewB01B05Test.testB03_WhaleFollowUpLinksToOriginAndDoublesTarget` (**PASS**). |
| **B04** | Pita kalender pasar (`app.js`) mengunci tanggal statis 11, 12, 13, 14, 15, 18 dan judul statis '12 OKT' | SELESAI & TERVERIFIKASI | `android/app/src/main/assets/www/app.js:4545–4605` | Tanggal pita kalender (-1 s.d. +4 hari) dan judul agenda dihitung dinamis dari `latestSnapshot.clock.timestamp` dalam zona waktu WIB menggunakan format lokal Indonesia (`toLocaleDateString('id-ID', ...)`). Syntax check JS `node --check` (**PASS**). Preview visual disetujui pengguna. |
| **B05** | Teks nominal kas header (`.wealth-chip strong`) terpotong (clipping) pada layar 320/360px dengan setelan zoom font 200% | SELESAI & TERVERIFIKASI | `android/app/src/main/assets/www/style.css:320–345, 2148–2156` | `.wealth-chip` disetel dengan `flex-shrink: 1; min-width: 0;`, ukuran font fluid adaptif `clamp(0.70rem, 3.2vw, 0.88rem)`, dan `overflow: hidden; text-overflow: ellipsis;` sebagai fail-safe. Seluruh digit kas terbaca utuh tanpa meluap keluar container. Preview visual disetujui pengguna. |

---

## 3. Hasil Pengujian Lengkap

1. **Unit Test JUnit Backlog B01–B05 (`BacklogReviewB01B05Test`):**
   - Perintah: `java.exe -cp ... org.junit.runner.JUnitCore com.tradingglobal3d.core.BacklogReviewB01B05Test`
   - Hasil: **`OK (3 tests) in 0.216s`** (100% PASS).
2. **Unit Test JUnit Sintesa 5 Modul (`run_synthesis_tests.py`):**
   - Perintah: `python3 Work/run_synthesis_tests.py`
   - Hasil: **`OK (17 tests) in 0.754s`** (100% PASS).
3. **Kompilasi Sumber Java Android:**
   - Perintah: `./gradlew compileDebugJavaWithJavac --no-daemon --console=plain`
   - Hasil: **`BUILD SUCCESSFUL in 10s`** (0 error, semua kelas Java terkompilasi sempurna).
4. **Build APK Android Debug:**
   - Perintah: `./gradlew assembleDebug --no-daemon --console=plain`
   - Hasil: **`BUILD SUCCESSFUL in 15s`**
   - Berkas: `android/app/build/outputs/apk/debug/app-debug.apk` (15,47 MB, SHA-256: `E5057B5DDA563FEFB9A411927428788E234D87C23B97A0A782E9FFDD535023F5`).
5. **Integritas Save Pemain Asli:**
   - Hash SHA-256: `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED` (Persis sama dengan baseline).

---

## 4. Batasan & Pekerjaan yang Masih Terbuka

1. **Batas Verifikasi Runtime Android & Perangkat Fisik:**
   - Pengujian telah diverifikasi pada lingkungan host Java JVM, Node.js syntax, dan build packaging APK offline.
   - Sesuai instruksi pengguna, aplikasi **tidak dipasang ke perangkat fisik atau emulator** via ADB, dan **tidak dipush/dipublikasikan** ke remote origin. Kelulusan build APK bukan klaim bahwa pengujian pada runtime perangkat fisik Android telah dilakukan.
2. **Kepatuhan Diff Minimum:**
   - Seluruh perubahan dibatasi ketat hanya pada lima butir temuan (B01–B05) tanpa refactor sambilan.
