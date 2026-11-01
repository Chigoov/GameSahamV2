# MASTER PLAN PERBAIKAN 500 MILESTONE BUG GAME SAHAM (1 BUG = 1 MILESTONE)

**Basis Kode:** Android Native Java (API 36, Java 21) + WebView HTML/CSS/JS (Asset Lokal)  
**Total Target Milestone:** **Tepat 500 Milestone Mandiri**  
**Standar Pelaksanaan:** Strict Unbundled Execution & `/verification-before-completion`  
**Tanggal Penerbitan:** 1 November 2026  

---

## 1. PETA ARSITEKTUR & PEMBAGIAN 3 BATCH (500 MILESTONE)

Semua 500 bug dari 3 batch audit telah dipetakan secara deterministik ke dalam sistem pelacakan:

```
[BATCH 1: AUDIT_100]  Milestone #001 s.d. #100 (100 Bug Mobile Runtime, Freezes & UI)
[BATCH 2: AUDIT_300]  Milestone #101 s.d. #400 (300 Bug Akuntansi, Math & Trading Engine)
[BATCH 3: BATCH_G3]   Milestone #401 s.d. #500 (100 Bug Ekspansi, Boss, Mandat, Bank & RUPS)
========================================================================================
TOTAL KESELURUHAN   = 500 MILESTONE LENGKAP TERDAFTAR (1 BUG = 1 MILESTONE)
```

---

## 2. DETAIL SISTEMATIKA & STRATEGI PERBAIKAN PER BATCH

### A. BATCH 1: MOBILE RUNTIME, TOMBOL MACET & GC FREEZE (Milestone #001 s.d. #100)
* **Sumber Data:** `Artifacts/AUDIT_100_BUG_GAME_SAHAM.md`
* **Cakupan File:** `MainActivity.java`, `TradingWebBridge.java`, `app.js`, `style.css`, `index.html`
* **Langkah Kunci:**
  1. Helper `safeUUID()` di `app.js` menggantikan `crypto.randomUUID()` yang menyebabkan crash order di WebView.
  2. Endpoint mikro `getLivePriceFeed()` (< 2.5 KB) di `TradingWebBridge.java` memangkas jeda GC 119ms.
  3. Penggantian `LAYER_TYPE_HARDWARE` ke `LAYER_TYPE_NONE` di `MainActivity.java` menangkal crash GPU RenderThread.
  4. Penanganan tombol Back fisik Android (`handleBack()`) yang menutup modal flow tanpa keluar aplikasi.
  5. Penggunaan `WeakReference<MainActivity>` di `TradingWebBridge` mengeliminasi kebocoran memori Activity.

### B. BATCH 2: AKUNTANSI, ORDER BOOK, MATH & MODEL KEUANGAN (Milestone #101 s.d. #400)
* **Sumber Data:** `Artifacts/AUDIT_300_BUG_BARU_NON_DUPLIKAT.md` (6 Modul, #1 - #300)
* **Cakupan File:** `Money.java`, `Account.java`, `Position.java`, `MatchingEngine.java`, `OrderBook.java`, `Instrument.java`, `MarketIndex.java`, `EconomyLedger.java`
* **Langkah Kunci:**
  1. Pola `#,##0` pada DecimalFormat `Money.java` mencetak saldo nol `"Rp 0"` dengan benar.
  2. Deep copy pada `Account.copy()` dan `Position.copy()` melindungi keutuhan data akun portofolio.
  3. Reset otomatis `averageCost = 0` di `Position.java` saat seluruh lembar saham habis terjual.
  4. Pembobotan *Free-Float Adjusted Market Cap* di `MarketIndex.java` untuk kalkulasi IHSG & S&P 500.
  5. Auto-replenishment kas & stok Market Maker di `NpcAgent.java` agar buku order selalu memiliki kuotasi beli dan jual.
  6. Dukungan *partial fill* pada order beli/jual market di `MatchingEngine.java`.
  7. Pemotongan pajak PPh Final 20% otomatis pada pencairan bunga deposito di `TimeDepositManager.java`.
  8. Plafon fasilitas pinjaman margin bertingkat berdasarkan jenjang karier di `TradingFacade.java`.

### C. BATCH 3: FITUR EKSPANSI, BOSS, MANDAT, BANK & RUPS (Milestone #401 s.d. #500)
* **Sumber Data:** `Artifacts/AUDIT_100_BUG_BARU_GELOMBANG_3.md` (5 Kluster, #301 - #400)
* **Cakupan File:** `MarketBossService.java`, `MarketBoss.java`, `PortfolioInsuranceService.java`, `FirmManager.java`, `InstallmentLoanManager.java`, `ShareholderMeetingAgenda.java`
* **Langkah Kunci:**
  1. **Kluster 1 (Boss Duel #301 - #320):** Implementasi penonaktifan duel bos lama saat bos baru ditantang (`other.setActive(false)`), kalkulasi *compounded return* pemain `(1 + r1) * (1 + r2) - 1`, dan status tantang ulang (*retry*).
  2. **Kluster 2 (Tech Tree & Asuransi #321 - #340):** Perbaikan batas waktu polis asuransi portofolio dan pembukaan cooldown 24 jam.
  3. **Kluster 3 (Mandat Klien AUM #341 - #360):** Penyesuaian ambang batas penerimaan mandat awal dan kalkulasi High-Water Mark (HWM) fee performa yang presisi.
  4. **Kluster 4 (Perbankan & Deposito #361 - #380):** Penggantian dialog `prompt` bawaan WebView dengan modal kustom interaktif, serta validasi pembulatan nominal penempatan deposito.
  5. **Kluster 5 (RUPS & Database SQLite #381 - #400):** Resolusi ticker multi-bursa pada hak suara RUPS saham US dan pembulatan stock split non-pecahan.

---

## 3. BERKAS MASTER TRACKER & VERIFIKASI SEBELUM SELESAI

Setiap nomor milestone dapat dipantau langsung pada berkas artefak:
* **Tracker CSV (500 Baris Riil):** `Work/MASTER-TRACKER-500-BUGS.csv`
* **Tracker Database JSON:** `Work/MASTER-TRACKER-500-BUGS.json`
* **Suite Uji Simulasi JUnit:** `Work/run_synthesis_tests.py` (17 Tests)
* **Uji Transaksi Realistis Bridge:** `Work/verify_comprehensive_fixes.py`
