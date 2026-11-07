# LAPORAN EKSEKUSI REMEDIASI BACKLOG REVIEW B01–B05
**Tanggal:** 2 November 2026  
**Target Proyek:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`  
**Referensi:** `AGENTS.md`, `Work/Perbaikan-Audit-Ulang-2026-11-01/` (`plan.md`, `BACKLOG-REVIEW.md`, `MATRIKS-CAKUPAN.md`, `HASIL-VERIFIKASI.md`, `plan-lanjutan-b01-b05.md`)  
**Status Eksekusi:** PENYERAHAN LOKAL B01–B05; sisa B05 ditutup Codex pada 2 November 2026. Bukti domain agent sebelumnya dan verifikasi akhir B05 dipisahkan di bawah. Tidak mengklaim seluruh game/perangkat lulus.

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
| **B02** | Kuotasi suku bunga pinjaman (`currentMacroLoanRateAnnual / 365`) berbeda dari rate statis kontrak pinjaman (`DAILY_RATE` 0.05%), fingerprint quoteVersion belum menyertakan rate bunga, dan teks UI hardcoded 0.05% | SELESAI & TERVERIFIKASI | `android/app/src/main/java/com/tradingglobal3d/core/loans/LoanContract.java:85–118, 444, 475`<br>`InstallmentLoanManager.java:95–130, 224–240`<br>`TradingFacade.java:4266–4290`<br>`app.js:7349, 7443`<br>`LoanScheduleRegressionTest.java:67`<br>`LoanAccountingRegressionTest.java:115, 152` | 1. Suku bunga harian `dailyRate` dimasukkan ke dalam fingerprint SHA-256 `quoteVersion` dan data kuotasi (`dailyRateRaw`, `dailyRate`, `annualRate`, `dailyEstimate`). Jika suku bunga berubah sebelum konfirmasi, `disburseReviewedLoan` otomatis melempar `IllegalStateException("STALE_QUOTE: tinjau ulang syarat pinjaman")`.<br>2. Hapus teks statis 0.05% pada `app.js:7349, 7443` dan `TradingFacade.java:4266–4290`; tampilan dan estimasi angsuran harian mengambil langsung dari domain quote.<br>3. `LoanContract` menerima `dailyRate` dan menyimpannya di `exportState`/`restoreFromState` (fallback aman ke `DAILY_RATE` untuk save lama).<br>4. Oracle tes regresi diselaraskan dengan rate dinamis (`loan.getDailyInterestRate()`). Seluruh 17 tes suite pinjaman PASS (8 LoanSchedule + 4 LoanAccounting + 5 BacklogReview). |
| **B03** | Rumor lanjutan Whale (`dayIn14 == 9`) memilih emiten kalender acak dan persentase 0.42 tanpa mengaitkan ke rumor Whale asal | SELESAI & TERVERIFIKASI | `android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java:680–740` | Rumor lanjutan menelusuri secara deterministik warta Whale asal sebelumnya (`cand.getPublishedAt() < timestamp`), menggunakan emiten yang sama persis, dan menetapkan persentase target tepat 2× dari rumor asal (`originPct * 2.0`). Tidak memaksakan rumor lanjutan jika warta asal tidak ada. Diuji via `BacklogReviewB01B05Test.testB03_WhaleFollowUpLinksToOriginAndDoublesTarget` (**PASS**). |
| **B04** | Pita kalender pasar (`app.js`) mengunci tanggal statis 11, 12, 13, 14, 15, 18 dan judul statis '12 OKT' | SELESAI & TERVERIFIKASI | `android/app/src/main/assets/www/app.js:4545–4605` | Tanggal pita kalender (-1 s.d. +4 hari) dan judul agenda dihitung dinamis dari `latestSnapshot.clock.timestamp` dalam zona waktu WIB menggunakan format lokal Indonesia (`toLocaleDateString('id-ID', ...)`). Syntax check JS `node --check` (**PASS**). Preview visual disetujui pengguna. |
| **B05** | Teks nominal kas header (`.wealth-chip strong`) terpotong (clipping) pada layar 320/360px dengan setelan zoom font 200%; review menegaskan memakai reflow penuh tanpa ellipsis | SELESAI & TERVERIFIKASI | `android/app/src/main/assets/www/style.css:336–346, 2154–2164` | Penataan reflow penuh tanpa ellipsis diterapkan: `white-space: normal; word-break: break-word; overflow: visible; text-overflow: clip; text-align: right; font-size: clamp(0.68rem, 2.8vw, 0.88rem); line-height: 1.15;`. Preview agent merupakan riwayat. Override akhir pada style.css:2533 yang membatalkan word-break dihapus Codex setelah pemilik meminta mengambil alih sisa pekerjaan. Pemeriksaan produksi lengkap check-header-reflow.py lulus 42 kasus hingga Rp9.999.999.999.999, tujuh viewport dan skala teks 100%/200%; bukti checks.json dan screenshot pada Artifacts/B05-Final-Reflow-2026-11-02. Tidak menyimpulkan kelulusan dari preview saja. |


---

## 3. Hasil Pengujian Lengkap

1. **Unit Test JUnit Backlog B01–B05 (`BacklogReviewB01B05Test`):**
   - Perintah: `java.exe -cp ... org.junit.runner.JUnitCore com.tradingglobal3d.core.BacklogReviewB01B05Test`
   - Hasil: **`OK (5 tests) in 0.213s`** (100% PASS: B01 supply event & ledger, B02 rate match, B02 stale quote rejection, B02 legacy fallback, B03 Whale 2x follow-up).
2. **Suite Tes Regresi Pinjaman Lengkap:**
   - `LoanScheduleRegressionTest`: **`OK (8 tests) in 4.18s`** (100% PASS).
   - `LoanAccountingRegressionTest`: **`OK (4 tests) in 0.638s`** (100% PASS).
3. **Suite Tes Regresi Remediasi Audit (`AuditRemediationRegressionTest`):**
   - Hasil: **`OK (26 tests) in 13.404s`** (100% PASS: R01–R12 & A01).
4. **Unit Test JUnit Sintesa 5 Modul (`run_synthesis_tests.py`):**
   - Perintah: `python3 Work/run_synthesis_tests.py`
   - Hasil: **`OK (17 tests) in 0.653s`** (100% PASS).
3. **Kompilasi Sumber Java Android:**
   - Perintah: `./gradlew compileDebugJavaWithJavac --no-daemon --console=plain`
   - Hasil: **`BUILD SUCCESSFUL in 10s`** (0 error, semua kelas Java terkompilasi sempurna).
4. **Build APK Android Debug:**
   - Perintah: `./gradlew assembleDebug --no-daemon --console=plain`
   - Hasil: **`BUILD SUCCESSFUL in 27s`** pada build akhir Codex (offline), 34 tasks; log `Artifacts/B05-Final-Reflow-2026-11-02/android-build.log`. Hasil 15 detik sebelumnya adalah riwayat agent.
   - Berkas: `android/app/build/outputs/apk/debug/app-debug.apk` (15,47 MB, SHA-256: `CBA7DCDE716FA55F470CB1E8D5C895C0E6D9E279DD99AA8D0532B37207B0D181`).
5. **Integritas Save Pemain Asli:**
   - Hash SHA-256: `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED` (Persis sama dengan baseline).

---

## 4. Batasan & Pekerjaan yang Masih Terbuka

1. **Batas Verifikasi Runtime Android & Perangkat Fisik:**
   - Pengujian telah diverifikasi pada lingkungan host Java JVM, Node.js syntax, dan build packaging APK offline.
   - Sesuai instruksi pengguna, aplikasi **tidak dipasang ke perangkat fisik atau emulator** via ADB, dan **tidak dipush/dipublikasikan** ke remote origin. Kelulusan build APK bukan klaim bahwa pengujian pada runtime perangkat fisik Android telah dilakukan.
2. **Kepatuhan Diff Minimum:**
   - Seluruh perubahan dibatasi ketat hanya pada lima butir temuan (B01–B05) tanpa refactor sambilan.

## Penyerahan akhir sisa B05 — Codex, 2 November 2026

| Milestone sisa | Status | Bukti |
|---|---|---|
| Override CSS | SELESAI | style.css:2533: hapus dua properti override; perubahan kode produksi satu baris, tanpa perubahan Java/ekonomi. |
| Browser produksi | LULUS | 42 kasus melalui `python Work/Perbaikan-Audit-Ulang-2026-11-01/check-header-reflow.py`, exit 0. HTML/CSS/font produksi, normal/200%, nominal panjang; batas glyph diperiksa terhadap chip dan viewport. |
| Syntax JS | LULUS | `node --check android/app/src/main/assets/www/app.js`, exit 0; JS tidak diubah pada penutupan ini. |
| APK offline | LULUS | assembleDebug offline, exit 0, 27 detik/34 tasks. app.js dan style.css APK identik byte dengan source. Peringatan deprecated Gradle features tetap ada. |
| Save pemain | IDENTIK | Hanya di-hash, tidak dibaca sebagai data permainan/disalin/dimigrasikan; hash baseline tetap sama. |
| Dokumen/hash | DIPERBARUI | Hash APK pada bagian 3 dan final-verification.json di direktori bukti akhir. |

Command build dari android/: `gradlew.bat --offline --no-daemon -Dorg.gradle.jvmargs="-Xmx512m -Dfile.encoding=UTF-8" --project-cache-dir ../Artifacts/B05-Final-Reflow-2026-11-02/gradle-cache :app:assembleDebug`. JAVA_HOME/JAVA_TOOL_OPTIONS hanya di proses shell; tidak ada perubahan global atau fetch dependency.

Satu check awal gagal akibat perbandingan tinggi glyph dengan line box, kemudian batas vertikal diperbaiki menjadi batas chip; batas horizontal tetap area teks dan viewport. Tidak ada penghapusan kasus yang gagal. Hasil terakhir 42/42 lulus. Screenshot tidak berisi save pemain; nominal/data tampilan sintetis. Headless Chrome digunakan, bukan Android WebView. Tidak menjalankan ulang tes domain hanya untuk perubahan CSS; 34 tests lulus pada review source sebelum penutupan tetap bukti terpisah.

Tidak ada commit/push/publikasi, ADB atau instalasi. Batas di luar komponen B05 (misalnya pemenggalan label navigasi pada font sangat besar), seluruh flow lain serta runtime Android tetap tidak diluluskan oleh check nominal kas ini. next_action: review penyerahan lalu uji runtime Android bila pemilik mengizinkan; tidak memulai scope baru secara otomatis.
