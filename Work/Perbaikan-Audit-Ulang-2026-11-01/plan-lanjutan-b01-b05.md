# Plan Lanjutan Remediasi Backlog Review B01–B05
**Tanggal:** 2 November 2026  
**Target Proyek:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`  
**Referensi:** `AGENTS.md` & `Work/Perbaikan-Audit-Ulang-2026-11-01/` (`plan.md`, `BACKLOG-REVIEW.md`, `MATRIKS-CAKUPAN.md`, `HASIL-VERIFIKASI.md`)  
**Status:** PENYERAHAN LOKAL — sisa B05 diperbaiki dan diperiksa; 42 kasus browser lulus, APK offline berhasil. Native runtime belum diuji. Riwayat domain dan batas bukti ada di laporan.

---

## 1. Tujuan dan Latar Belakang

Setelah 12 temuan audit ulang R01–R12 dan regresi A01 selesai diperbaiki dan diverifikasi pada host JVM, lima butir backlog review (B01–B05) yang telah terbukti secara source dan visual diinventarisasi untuk diselesaikan:
1. **B01:** Pencatatan replenishment NPC Retail (`ACC-NPC-RETAIL-01`) melalui penerbitan saham sah (`applyShareSupplyEvent`) dan modal eksternal fasilitas pasar tercatat pada `EconomyLedger`.
2. **B02:** Penyelarasan suku bunga pinjaman antara kuotasi `InstallmentLoanManager` dan kalkulasi bunga riil `LoanContract`.
3. **B03:** Pengikatan rumor lanjutan Whale (`ResearchService`) ke emiten dan persentase rumor Whale asal (2× dari target awal), bukan indeks acak tanpa asal.
4. **B04:** Tanggal dan nama hari pada ribbon kalender pasar (`app.js`) diturunkan secara dinamis dari clock domain permainan, bukan nilai statis.
5. **B05:** Reflow dan penataan responsif teks kas header (`style.css` / `index.html`) agar tidak terpotong pada viewport sempit 320/360px dengan font root 200%.

---

## 2. Batas Kerja dan Perlindungan Data

- **Ubah hanya kode yang diperlukan:** Tidak ada refactor massal, reformatting, atau modifikasi fitur yang tidak bersangkutan.
- **Save Pemain Terlindungi:** Berkas `Artifacts/LocalPreview/preview-save/save_game_v2.json` HANYA di-hash (SHA-256 baseline: `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`).
- **Pengujian Terisolasi:** Semua pengujian unit dan probe menggunakan direktori QA terpisah (`build/tmp-test-b*`).
- **Gate Preview UI:** Perubahan visual pada B04 (ribbon kalender) dan B05 (header kas 200%) disajikan melalui berkas preview tersendiri untuk ditinjau pengguna sebelum diterapkan ke kode produksi.
- **Larangan Tindakan Luar Scope:** Dilarang commit/push ke remote, memakai ADB, atau memasang aplikasi ke perangkat fisik.

---

## 3. Rincian Teknis Pekerjaan per Butir

### B01 — Pencatatan Replenishment NPC Retail (Domain Finansial)
- **Lokasi Kode:** `android/app/src/main/java/com/tradingglobal3d/core/npc/NpcAgent.java:194–208`, `MarketEngine.java:302–306`, `TradingFacade.java:1670–1680`.
- **Akar Masalah:** `ACC-NPC-RETAIL-01` saat kehabisan saham atau kas menginjeksi saldo via `pos.addBuy(replenish, currentPrice)` dan `account.creditCash(minCash)` tanpa event penerbitan suplai saham atau mutasi kas ledger.
- **Rencana Perbaikan:**
  - Tambahkan metode `ensureRetailLiquidity(Instrument, OrderBook, EconomyLedger, long, String)` atau perluas `ensureLiquidity` agar mendukung `ACC-NPC-RETAIL-01`.
  - Tambahan saham dicatat via `instrument.applyShareSupplyEvent("RETAIL-SUPPLY-...", ...)` dengan id event persisten dan rekonsiliasi kepemilikan.
  - Tambahan kas dicatat via `ledger.recordMutation("RETAIL-CAPITAL-...", account, LedgerEntryType.LIQUIDITY_CAPITAL, deficit, true, ...)` hanya sebesar defisit menuju target (IDR 2 Miliar / USD 200.000), bukan menambahkan kelipatan berulang.
- **Acceptance:** Top-up retail tercatat di ledger dan supply event; tidak ada mutasi saldo kas/saham diam-diam; idempotensi terjamin saat reload/retry.

### B02 — Keselarasan Suku Bunga Quote Pinjaman vs Kontrak
- **Lokasi Kode:** `android/app/src/main/java/com/tradingglobal3d/core/loans/InstallmentLoanManager.java:95–130, 224–245`, `LoanContract.java:85–118, 444, 475`, `TradingFacade.java:4266–4290`, `app.js:7349, 7443`.
- **Akar Masalah:**
  1. Kuotasi pinjaman menampilkan teks `currentMacroLoanRateAnnual / 365.0`, sedangkan kontrak sebelumnya mengunci `DAILY_RATE = 0.05%`.
  2. `quoteVersion` belum menyertakan suku bunga dalam fingerprint hash SHA-256, sehingga perubahan suku bunga makro saat quote terbuka tidak memicu `STALE_QUOTE`.
  3. UI pada `app.js:7443` dan `TradingFacade.java:4271` masih memuat teks hardcoded bunga tetap 0.05%.
- **Rencana Perbaikan & Hasil Implementasi:**
  - `LoanContract` menerima `dailyInterestRate` pada konstruktor (dihitung dari rate makro tahunan saat pencairan / 365 / 100) dan menyimpannya pada `exportState`/`restoreFromState`. Bunga kuotasi, estimasi angsuran, dan bunga harian riil cocok 100%. Kompatibilitas mundur save lama terjaga (fallback ke `DAILY_RATE`).
  - Masukkan `dailyRate.toPlainString()` ke dalam fingerprint SHA-256 `quoteVersion` dan sertakan `dailyRateRaw`, `dailyRate`, `annualRate`, `dailyEstimateRaw`, dan `dailyEstimate` pada payload quote. Jika bunga berubah saat quote terbuka, pemanggilan `disburseReviewedLoan` otomatis melempar `IllegalStateException("STALE_QUOTE: tinjau ulang syarat pinjaman")`.
  - Hapus teks statis 0.05% pada `app.js:7349, 7443` dan `TradingFacade.java:4266–4290`, ganti dengan pembacaan dinamis `quoteRes.dailyRate` dan `quoteRes.dailyEstimate`.
  - Selaraskan oracle pada `LoanScheduleRegressionTest.java:67` dan `LoanAccountingRegressionTest.java:115,152` agar mengacu pada `loan.getDailyInterestRate()`.
- **Acceptance:** Suku bunga pada tanda terima pencairan sama dengan bunga yang diakrualkan pada `accrueThrough`; perubahan suku bunga saat quote terbuka sukses ditolak dengan `STALE_QUOTE`; kontrak legacy tetap dapat dimuat dengan rate default; seluruh tes regresi pinjaman (17 tests) PASS 100%.

### B03 — Pengikatan Rumor Lanjutan Whale ke Rumor Asal
- **Lokasi Kode:** `android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java:680–740`.
- **Akar Masalah:** Rumor lanjutan pada `dayIn14 == 9` memilih emiten acak `idxUniverse.get((dayOfYear + 2) % size)` dengan persentase `0.42` tanpa merujuk ke rumor Whale asal.
- **Rencana Perbaikan & Hasil Implementasi:**
  - Rumor lanjutan menelusuri secara deterministik warta Whale asal sebelumnya (`cand.getPublishedAt() < timestamp`), menggunakan emiten yang sama persis, dan menetapkan persentase target tepat 2× dari rumor asal (`originPct * 2.0`). Jika warta asal belum ada, rumor lanjutan tidak dipaksakan.
- **Acceptance:** Rumor lanjutan selalu memiliki emiten yang sama dengan rumor Whale asal, persentase tepat 2× dari rumor asal, dan mempertahankan rantai katalis. Diuji pada `BacklogReviewB01B05Test.testB03_WhaleFollowUpLinksToOriginAndDoublesTarget` (PASS).

### B04 — Kalender Ribbon Pasar Dinamis (UI)
- **Lokasi Kode:** `android/app/src/main/assets/www/app.js:4545–4605`.
- **Akar Masalah:** Array tanggal ribbon `days` dikodekan mati dengan angka 11, 12, 13, 14, 15, 18 dan '12 OKT'.
- **Rencana Perbaikan & Hasil Implementasi:**
  - Hitung tanggal untuk setiap hari offset (-1 s.d. +4) secara dinamis dari `latestSnapshot.clock.timestamp` dalam zona waktu WIB.
  - Format angka tanggal (`day`), nama hari (`label`), dan penanda hari ini secara otomatis menggunakan `toLocaleDateString('id-ID', ...)`.
- **Acceptance:** Tanggal ribbon selalu mencerminkan tanggal kalender dunia game; perpindahan bulan dan tahun tertangani secara mulus. Syntax check `node --check` PASS.

### B05 — Penataan Responsif Header Kas 200% Font (UI/CSS)
- **Lokasi Kode:** `android/app/src/main/assets/www/style.css:320–345, 2148–2156`, `index.html:17`.
- **Akar Masalah:** `.wealth-chip strong` memiliki `white-space: nowrap` dan chip memiliki `flex-shrink: 0`. Pada viewport 320/360px dengan pembesaran teks 200%, nominal kas terpotong oleh batas header container. Review terbaru menegaskan agar menggunakan reflow penuh dan TIDAK memakai ellipsis.
- **Rencana Perbaikan & Hasil Implementasi:**
  - Sesuaikan CSS `.wealth-chip`: `flex-shrink: 1; min-width: 0; padding: 4px 8px;`
  - Sesuaikan CSS `.wealth-chip strong`: `white-space: normal; word-break: break-word; overflow: visible; text-overflow: clip; text-align: right; font-size: clamp(0.68rem, 2.8vw, 0.88rem); line-height: 1.15;`.
  - Berkas preview interaktif dibuat di `Artifacts/Preview-B05-Reflow-2026-11-02/preview-header-reflow.html` dan diverifikasi dengan screenshot `preview-capture.png` & `vision_analyze`.
- **Acceptance:** Pada resolusi 320px dan 360px dengan teks 200%, seluruh digit nominal kas Rp 50.000.000 (8 digit) dan Rp 50.000.000.000 (11 digit) terbaca 100% utuh tanpa pemotongan dan tanpa tanda ellipsis (...). Reflow telah diterapkan oleh agent sebelumnya. Pemilik kemudian memerintahkan Codex mengambil alih sisa perbaikan pada 2 November 2026; override akhir `style.css:2533` yang membatalkan word-break dihapus tanpa menambah komponen/layout baru.

---

## 4. Urutan Eksekusi dan Verifikasi

1. **M00:** Validasi baseline, hash save pemain, dan verifikasi temuan di codebase.
2. **M01:** Dokumentasi plan lanjutan ini.
3. **M02 (B01):** Implementasi replenishment retail berbasis supply event & ledger.
4. **M03 (B03):** Implementasi pengikatan rumor lanjutan Whale dengan rumor asal.
5. **M04 (B04 & B05):** Pembuatan berkas mockup/preview UI untuk verifikasi visual pengguna.
6. **M05 (B02):** Klarifikasi & penyelarasan suku bunga pinjaman quote vs kontrak.
7. **M06:** Penerapan UI B04 dan B05 setelah persetujuan preview.
8. **M07:** Uji regresi menyeluruh, build APK offline, hash save akhir, dan laporan penutupan.

## Penutupan B05 oleh Codex — 2 November 2026

- Produksi: hapus hanya `overflow-wrap: normal; word-break: normal;` pada override akhir `.wealth-chip strong`. Aturan reflow existing sekarang berlaku.
- Runnable check `check-header-reflow.py` memakai HTML, seluruh CSS dan font Plex produksi; tanpa bridge/ekonomi/save. 42 kasus: viewport 320/360/390/412/430/768/1440, font root 100%/200%, nominal Rp50 juta/Rp50 miliar/Rp9.999.999.999.999. Seluruh glyph nominal berada dalam area teks horizontal, chip vertikal dan viewport, tanpa ellipsis.
- Bukti: `Artifacts/B05-Final-Reflow-2026-11-02/checks.json`, screenshot header-320-text200.png/header-360-text200.png. Pemeriksaan ini khusus nominal kas B05, bukan seluruh header/navigasi atau runtime Android.
- Satu check awal terlalu ketat membandingkan tinggi glyph terhadap line box; dikoreksi ke batas chip vertikal karena font dapat melampaui line box tanpa terpotong. Check horizontal tetap ketat terhadap area teks dan viewport.
- Java/domain tidak diubah pada penutupan B05; hasil 34 tes pada review source sebelumnya tetap riwayat yang terpisah, bukan tes yang diklaim dijalankan ulang sekarang.

Build akhir 27 detik, exit 0; APK SHA-256 `CBA7DCDE716FA55F470CB1E8D5C895C0E6D9E279DD99AA8D0532B37207B0D181`. Isi app.js/style.css APK sama dengan source. Save pemain tetap hash baseline. Tidak ada commit/push/instalasi.
