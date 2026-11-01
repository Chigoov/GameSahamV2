# Plan Lanjutan Remediasi Backlog Review B01–B05
**Tanggal:** 2 November 2026  
**Target Proyek:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`  
**Referensi:** `AGENTS.md` & `Work/Perbaikan-Audit-Ulang-2026-11-01/` (`plan.md`, `BACKLOG-REVIEW.md`, `MATRIKS-CAKUPAN.md`, `HASIL-VERIFIKASI.md`)  
**Status:** AKTIF — EKSEKUSI MANDAT LANJUTAN B01–B05

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
- **Lokasi Kode:** `android/app/src/main/java/com/tradingglobal3d/core/loans/InstallmentLoanManager.java:239`, `LoanContract.java:81,97,215,235`.
- **Akar Masalah:** Kuotasi pinjaman menampilkan teks `currentMacroLoanRateAnnual / 365.0` (misal 10.75% / 365 = ~0.029% / hari), sedangkan `LoanContract` secara baku mengunci `dailyInterestRate = DAILY_RATE` (0.05% / hari atau 18.25% p.a.).
- **Rencana Perbaikan:**
  - `LoanContract` menerima `dailyInterestRate` pada konstruktor atau pencairan pinjaman, sehingga bunga yang dihitung dan diakrualkan sama persis dengan angka yang disajikan pada kuotasi/receipt.
  - Format quote dan estimasi harian dihitung konsisten dari rate yang sama.
- **Acceptance:** Suku bunga pada tanda terima pencairan sama dengan bunga yang diakrualkan pada `accrueThrough`; estimasi angsuran harian cocok 100%.

### B03 — Pengikatan Rumor Lanjutan Whale ke Rumor Asal
- **Lokasi Kode:** `android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java:700–716`.
- **Akar Masalah:** Rumor lanjutan pada `dayIn14 == 9` memilih emiten acak `idxUniverse.get((dayOfYear + 2) % size)` dengan persentase `0.42` tanpa merujuk ke rumor Whale asal.
- **Rencana Perbaikan:**
  - Simpan referensi rumor Whale asal yang aktif/terakhir (`lastWhaleTicker`, `lastWhalePct`, `lastWhaleNewsId`).
  - Saat giliran rumor lanjutan tiba (`dayIn14 == 9`), periksa apakah ada rumor asal yang valid.
  - Jika ada, buat rumor lanjutan untuk emiten yang sama dengan persentase `2.0 * lastWhalePct` dan headline yang merujuk pada perkembangan katalis awal. Jika tidak ada rumor awal yang sah, jangan memaksakan rumor lanjutan yatim.
- **Acceptance:** Rumor lanjutan selalu memiliki emiten yang sama dengan rumor Whale asal, persentase tepat 2× dari rumor asal, dan mempertahankan rantai katalis.

### B04 — Kalender Ribbon Pasar Dinamis (UI)
- **Lokasi Kode:** `android/app/src/main/assets/www/app.js:4547–4554, 4596`.
- **Akar Masalah:** Array tanggal ribbon `days` dikodekan mati dengan angka 11, 12, 13, 14, 15, 18 dan '12 OKT'.
- **Rencana Perbaikan:**
  - Hitung tanggal untuk setiap hari offset (-1 s.d. +4) secara dinamis dari `latestSnapshot.clock.timestamp` (atau anchor game time) dalam zona waktu WIB.
  - Format angka tanggal (`day`), nama hari (`label`), dan penanda hari ini secara otomatis.
- **Acceptance:** Tanggal ribbon selalu mencerminkan tanggal kalender dunia game; perpindahan bulan dan tahun tertangani secara mulus.

### B05 — Penataan Responsif Header Kas 200% Font (UI/CSS)
- **Lokasi Kode:** `android/app/src/main/assets/www/style.css:314–340`, `index.html:17`.
- **Akar Masalah:** `.wealth-chip strong` memiliki `white-space: nowrap` dan chip memiliki `flex-shrink: 0`. Pada viewport 320/360px dengan pembesaran teks 200%, nominal kas terpotong oleh batas header container.
- **Rencana Perbaikan:**
  - Sesuaikan penataan CSS `.wealth-chip` agar dapat menyusut secara proporsional (`min-width: 0`), mengizinkan reflow teks atau ukuran font yang adaptif (`clamp()`), dan mencegah pemotongan glyph kas.
- **Acceptance:** Pada resolusi 320px dan 360px dengan teks 200%, seluruh digit nominal kas tetap terbaca penuh tanpa clipping atau overflow container.

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
