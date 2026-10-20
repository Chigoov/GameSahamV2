# CHECKPOINT: Hermes Session 3 — Remediation AUD21-01 s/d AUD21-04
**Waktu:** 2026-10-21 (Hermes Session 3, model ag/claude-sonnet-4-6)
**Branch:** `codex/owner-audit-portrait-v8`
**HEAD sebelum:** `f467607c9c2fc68d16cbea135351ba9e6bc74796`
**HEAD sesudah:** `68bbd2c` (commit lokal, belum push)

---

## Ringkasan Perbaikan

### R0 — Preflight ✅
- Git root: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`
- Branch: `codex/owner-audit-portrait-v8`
- Save pribadi SHA-256: `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed` — **TIDAK BERUBAH**

### R1 — AUD21-01: Prestige UI ✅
- **Bug:** Tombol prestige membaca `window._currentSnapshot.runId` yang tidak pernah diisi
- **Fix:** Baca `presRes.runId` dari `bridge.getPrestigeStatus()` secara eksplisit
- **Tambahan:** `bridgeError()` helper di TradingWebBridge; `confirmPrestige/transactCollection/cancelFundOrder/reviewPortfolioHealth` dari `staffError` → `bridgeError`
- **Tes baru:** `PrestigeIntegrationTest.java` (5 tes: runId dari domain, blockers, stale runId)

### R2 — AUD21-02: Pinjaman Beragunan ✅
- **Bug:** UI membaca `panel._selectedCollateralId` tanpa pemilih
- **Fix:** Pemilih koleksi (`select`) diisi dari `bridge.getCollectionStatus()`, filter owned & tidak dijaminkan
- **Fix:** Alur quote → review → confirm dengan `quoteVersion` untuk anti-stale
- **Fix:** Legacy `disburseLoan` bypass dihapus — semua pinjaman harus melalui quote
- **Tes baru:** `LoanCollateralIntegrationTest.java` (5 tes: tanpa agunan, quote data, flow lengkap, stale quote, pinjaman perusahaan)

### R3 — AUD21-03: Return Liga/Rival ✅
- **Bug:** `weeklyRet = 0.0` konstan di rollover, leaderboard, dan rival
- **Fix:** `weeklyBaselineNetWorth` field baru; `computeWeeklyReturnPercent()` = (now-baseline)/baseline×100
- **Fix:** Baseline disimpan di JSON state dan dipulihkan saat load; reset setiap Senin rollover

### R4 — AUD21-04: Reksa Dana Rebalance ✅
- **Bug:** `advanceDay()` inert (body kosong) untuk MM/FI; exception rebalance EQUITY_INDEX ditelan
- **Fix:** `advanceDay(double, EconomyLedger, long)` — akruasi yield harian via ledger (tidak langsung creditCash)
- **Fix:** `LedgerEntryType.YIELD` ditambahkan — reconcile tetap konsisten
- **Fix:** `recordRebalanceFailure(reason, timestamp)` di MutualFundManager — kegagalan tersimpan, tidak hilang
- **Fix:** `backingModel` diperbarui: `CASH_ACCRUAL_DAILY_YIELD` untuk MM/FI (sebelumnya `CASH_ONLY_MM_FI_INSTRUMENTS_PENDING`)
- **Tes baru:** `MutualFundRebalanceTest.java` (6 tes: NAB MM tumbuh, NAB FI tumbuh, EQ canonical, backing model, failure recorded, NAB survive save/reload)
- **Regresi fix:** `FundDealingRegressionTest.java` diperbarui untuk signature baru

### R5 — Gap Fitur ✅ (Replay BLOCKED)
- **Gap:wealth acceptance:** `WealthConsistencyAcceptanceTest.java` (5 tes: net worth awal, transfer netral, AUM tidak double-count, reconcile konsisten, save/reload)
- **Gap:trade acceptance:** `TradeIdempotencyAcceptanceTest.java` (5 tes: idempotency, preview, kas berkurang, cancel restore, save/reload)
- **Replay historis:** BLOCKED — hak dataset tidak terpenuhi, tidak ada dataset intraday yang boleh didistribusikan

### R6 — Acceptance & Review ✅
- 387 tes, 0 failures (baseline: 361)
- Save pribadi tidak berubah: SHA-256 `59b41e24...`
- Reconcile lulus di semua tes wealth
- APK berhasil dibangun

### R7 — Tracker, Commit, APK ✅
- **Commit lokal:** `68bbd2c` — 13 file berubah, 2367 insertions, 349 deletions
- **APK baru:** `Artifacts/Hermes-Remediation-2026-10-21/app-debug-remediation-2026-10-21.apk`
- **APK size:** 815,303 bytes
- **APK SHA-256:** `ceccdf68a8f53d7606ba21f926aaa4763ff70f5605f12c0ef4e5e62b4498b059`

---

## File yang Diubah

**Domain (production):**
- `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java`
- `android/app/src/main/java/com/tradingglobal3d/core/funds/MutualFund.java`
- `android/app/src/main/java/com/tradingglobal3d/core/funds/MutualFundManager.java`
- `android/app/src/main/java/com/tradingglobal3d/core/economy/LedgerEntryType.java`
- `android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java`
- `android/app/src/main/java/com/tradingglobal3d/core/business/OperatingCompany.java`
- `android/app/src/main/assets/www/app.js`

**Tes baru:**
- `PrestigeIntegrationTest.java` — 5 tes
- `LoanCollateralIntegrationTest.java` — 5 tes
- `MutualFundRebalanceTest.java` — 6 tes
- `WealthConsistencyAcceptanceTest.java` — 5 tes
- `TradeIdempotencyAcceptanceTest.java` — 5 tes

**Tes diupdate:**
- `FundDealingRegressionTest.java` — signature advanceDay

---

## Status Produk

| Milestone | Status |
|-----------|--------|
| R0 Preflight | ✅ DONE |
| R1 AUD21-01 Prestige | ✅ DONE |
| R2 AUD21-02 Loan Collateral | ✅ DONE |
| R3 AUD21-03 Liga Return | ✅ DONE |
| R4 AUD21-04 Fund Rebalance | ✅ DONE |
| R5 Gap Fitur | ✅ DONE (Replay BLOCKED) |
| R6 Acceptance | ✅ 387/0 |
| R7 Commit + APK | ✅ DONE |

**Gate terbuka:**
- Replay historis: BLOCKED — butuh dataset intraday berlisensi
- UI browser acceptance: NOT_TESTED — butuh Playwright/Chrome terinstall
- Native device: PENDING — butuh perangkat fisik atau emulator
- Status produk: **PARTIAL** (semua requirement domain source selesai; UI native dan perangkat belum)

## Next Action
Jika dilanjutkan:
1. Install Playwright untuk UI browser acceptance test
2. Hubungkan perangkat/emulator untuk native test
3. Cari dataset historis berlisensi untuk replay
