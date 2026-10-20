# Independent Design Review: Game Saham 2D Portrait Feature Completion

**Review Date:** October 20, 2026  
**Review Target:** `Work/.agents/tasks/design.md`  
**Reviewer:** Design Review Subagent (Independent Quality Gate)  
**Governing Documents:**
- `AGENTS.md` (Workspace Rules & Mandates)
- `Work/Hermes-Feature-Completion-2026-10-20/SPECIFICATION.md` (Authoritative Functional Specification)
- `Work/Hermes-Handoff-2026-10-20/PHASE-QUALITY-STANDARDS.md` (Quality Gates & Phase Standards)

---

## Executive Summary & Mechanical Verdict

This design review evaluates the technical design document `Work/.agents/tasks/design.md` against the project requirements, architecture boundaries, time acceleration rules, audit finding remediation plans (F1–F9), personal save protection, and the complete coverage of all 26 reference feature groups and D01–D16 designs.

### Findings Breakdown
- **HIGH Severity Findings:** 6
- **MEDIUM Severity Findings:** 3
- **NIT Severity Findings:** 1
- **Total Actionable Findings (HIGH + MEDIUM):** 9

**Mechanical Verdict:** **CHANGES_REQUESTED**  
*(Rule: Count of HIGH + MEDIUM > 0 mechanically requires CHANGES_REQUESTED. Approval is blocked until all HIGH and MEDIUM findings are remedied.)*

---

## Verified Assumptions

1. **Java Domain Single-Ownership:**  
   The design correctly maintains the strict separation of concerns where Java 17 (`com.tradingglobal3d.core`) is the single source of truth for ledgers, portfolios, time, and game state, while the web UI (`assets/www/`) acts strictly as a presentation and user-input layer.
2. **Canonical Time Acceleration Ratios:**  
   The dual-rate ratio rules are mathematically specified in Section 2:
   - Foreground active 1×: 86,400 game seconds per 300 real seconds ($288\times$).
   - Background/closed inactive: 86,400 game seconds per 3,600 real seconds ($24\times$).
   - Skip Day: Exactly +86,400 game seconds with confirmation.
   - Non-inheritance of speed multipliers during background simulation is explicitly mandated.
3. **Double-Entry Value-Neutral Capital Transfers (D02 / F4):**  
   The design correctly specifies that transferring capital between the player's personal account and owned companies is value-neutral and does not deflate consolidated net worth.
4. **Prestige Reset Safety Concept:**  
   The design correctly conceptualizes prestige as an optional, high-tier career choice with non-destructive atomic backups.
5. **Atomic Save Write-Replace Pattern:**  
   The design adheres to the atomic save pattern (`.tmp` $\rightarrow$ `.bak` rotation $\rightarrow$ atomic rename) already verified in `SaveManager.java`.

---

## Unverified & Inaccurate Assumptions in `design.md`

1. **Unverified Assumption on Save Directories:**  
   `design.md` assumes writing `save_game_v2.json` without specifying directory isolation. It fails to identify or protect the player's personal save file at `Artifacts/LocalPreview/preview-save/save_game_v2.json`.
2. **Inaccurate Assumption on Collection Manager Inventory:**  
   `design.md` Section 6.1 claims that `CollectionManager.java` needs to be "expanded to 12 items". In reality, reading `CollectionManager.java` reveals that it **already contains 12 complete items** with prices starting from Rp 15M–Rp 45M for early-game progression. `design.md` invented a new set of 12 items starting at Rp 350M, which violates D10's early-game accessibility rules.
3. **Inaccurate Assumption on Finding F3 Status:**  
   `design.md` Section 4 claims `initializeNewGame()` fails to call `.reset()` on domain child managers. Verification of `TradingFacade.java` (lines 343–353) demonstrates that `.reset()` calls are already implemented for all child managers.
4. **Inaccurate Assumption on Finding F5 Frontend Status:**  
   `design.md` Section 4 lists 7 modals as missing from `app.js`. Inspection of `assets/www/app.js` confirms that 6 of the 7 modals (`openFoundCompanyModal`, `openCompanyPolicyModal`, `openCompanyCapitalModal`, `openCollectionsGalleryModal`, `openPrestigeConfirmationModal`, `openLeagueAndRivalModal`) are already implemented. Only `openCompanyStaffModal()` is truly missing.
5. **Inaccurate Assumption on Existing Loan Contract Terms:**  
   `design.md` claims corporate loans have tenors of 14, 30, and 60 days with 0.04% daily rate and 0.75% fee, whereas existing Java code and SPECIFICATION.md D05 enforce 7, 14, and 30 days at 0.05% daily rate and 0.50% fee.

---

## Numbered Findings List

### Finding 1 [HIGH]: Personal Save Protection Completely Omitted from Design
- **Location:** `Work/.agents/tasks/design.md`, Section 5.5 ("Persistence & Save System Architecture"), lines 494–508.
- **Problem:** `design.md` details file-level atomic serialization (`save_game_v2.json.tmp` $\rightarrow$ `.bak` $\rightarrow$ `save_game_v2.json`), but omits any specification for file path isolation and personal save protection. Both `AGENTS.md` and the user mandate explicitly state:
  *"Save protection (ensuring personal save `Artifacts/LocalPreview/preview-save/save_game_v2.json` is never endangered). Jangan menguji destruktif pada save pribadi."*
  `design.md` provides no rules or guards preventing test runners, automated QA suites, or benchmark scripts from overwriting, clearing, or resetting the personal preview save during test execution.
- **Concrete Fix:** Add Section 5.5.1 ("Save Directory Isolation & Personal Save Safeguards"):
  1. Specify that `SaveManager.forDirectory(File baseDir)` in all unit tests, integration tests, and benchmarks MUST point to a dedicated transient directory (e.g., `java.io.tmpdir/game_saham_test_<timestamp>/` or `Artifacts/QA-Save/`).
  2. Forbid any test suite, script, or automated tool from targeting `Artifacts/LocalPreview/preview-save/`.
  3. Specify that before any preview server run or manual QA session, `Artifacts/LocalPreview/preview-save/save_game_v2.json` must be backed up to a timestamped backup directory (e.g., `Artifacts/LocalPreview/backup/`).

---

### Finding 2 [HIGH]: Discrete Time Advancement Slicing Algorithm Misses 16:00 Cutoff
- **Location:** `Work/.agents/tasks/design.md`, Section 2 ("Canonical Time Architecture & Invariant Step Pipeline"), lines 201–214.
- **Problem:** The proposed algorithmic loop for `advanceWorldSeconds` slices steps using:
  ```java
  long step = Math.min(remainingSeconds, Math.min(60, Math.min(secondsToMidnight, secondsToNextTransition)));
  ```
  and checks cutoff with:
  ```java
  if (clock.getCurrentTime().toSecondOfDay() == 16 * 3600) {
      mutualFundManager.processDailyCutoff(catalog, clock.getCurrentDate());
  }
  ```
  If `step` is 60 seconds (or any step $> 1$ s), the clock can leap over `16 * 3600` (e.g., from 15:59:30 to 16:00:30). The exact equality `toSecondOfDay() == 16 * 3600` evaluates to `false`, completely skipping the daily mutual fund cutoff during large-step time advancements (such as Skip Day or active catch-up).
- **Concrete Fix:** Update the step slicing formula and boundary check to include `secondsToCutoff` and interval boundary crossing:
  ```java
  long currentSecond = clock.getCurrentTime().toSecondOfDay();
  long cutoffSecond = 16 * 3600;
  long secondsToCutoff = (currentSecond < cutoffSecond) ? (cutoffSecond - currentSecond) : Long.MAX_VALUE;
  long step = Math.min(remainingSeconds, Math.min(60, Math.min(secondsToMidnight, Math.min(secondsToNextTransition, secondsToCutoff))));

  clock.advanceGameSeconds(step);
  remainingSeconds -= step;

  // Process mutual fund cutoff if exactly at cutoff or crossed
  if (currentSecond < cutoffSecond && clock.getCurrentTime().toSecondOfDay() >= cutoffSecond) {
      mutualFundManager.processDailyCutoff(catalog, clock.getCurrentDate());
  }
  ```

---

### Finding 3 [HIGH]: Missing Bridge and Facade API Specifications for Company Staff / Employee Management (D11)
- **Location:** `Work/.agents/tasks/design.md`, Section 4 ("Finding F5"), lines 283–295; Section 6.1 & 6.2; Part I FR-04.
- **Problem:** FR-04 and F5 mandate company staff management (hiring, firing, training, assigning roles, and viewing staff rosters via `openCompanyStaffModal()`). However, `design.md` never specifies the Java Facade signatures, Bridge `@JavascriptInterface` endpoints, parameter JSON schemas, or candidate generation rules. `CompanyManager.java` currently has no methods to hire, fire, or train staff, and `TradingWebBridge.java` has no staff endpoints. Without concrete interface definitions, the modal cannot be integrated with the domain.
- **Concrete Fix:** In Section 6.1, specify the required method contracts:
  1. In `CompanyManager.java`:
     ```java
     public synchronized Employee hireEmployee(String companyId, String candidateId, String name, Employee.Role role, Account companyAccount, EconomyLedger ledger, long now);
     public synchronized void fireEmployee(String companyId, String employeeId, Account companyAccount, EconomyLedger ledger, long now);
     public synchronized void trainEmployee(String companyId, String employeeId, Account companyAccount, EconomyLedger ledger, long now);
     public synchronized List<JSONObject> getAvailableCandidates(String companyId, long gameTimestamp);
     ```
  2. In `TradingFacade.java` & `TradingWebBridge.java`:
     - `getCompanyStaff(String companyId)` $\rightarrow$ returns JSON with current employees, total payroll, morale impact, and available candidate pool.
     - `hireCompanyStaff(String jsonRequest)` $\rightarrow$ validates `companyId`, `candidateId`, deducts signing bonus from company account, and updates roster.
     - `fireCompanyStaff(String jsonRequest)` $\rightarrow$ validates severance pay, removes employee, recalculates company morale.
     - `trainCompanyStaff(String jsonRequest)` $\rightarrow$ increments employee level (up to 5), deducts training fee from company cash.

---

### Finding 4 [HIGH]: Inconsistency Between SPECIFICATION.md D05, Existing Code, and `design.md` for Corporate Loan Tenors and Interest Rates
- **Location:** `Work/.agents/tasks/design.md`, Section 5.2 ("Tenor-Based Installment Loans Catalog"), lines 413–418.
- **Problem:** Table 5.2 defines `LOAN-CORP-01` with:
  - Tenors: `14, 30, 60 Hari`
  - Daily Rate: `0.04% / hari`
  - Fee: `0.75%`  
  This conflicts directly with:
  1. SPECIFICATION.md Section 11 (D05): *"minimal dua produk IDR: pinjaman pribadi beragunan dan modal kerja perusahaan, tenor 7/14/30 hari game; principal minimum Rp1 juta... Tarif awal 0,05% per hari pada sisa principal, biaya pencairan 0,5%."*
  2. `InstallmentLoanManager.java` line 70:
     ```java
     if (tenorDays != 7 && tenorDays != 14 && tenorDays != 30) {
         throw new IllegalArgumentException("Pilihan tenor pinjaman yang sah: 7, 14, atau 30 hari");
     }
     ```
  3. `LoanContract.java`, which hardcodes `DISBURSEMENT_FEE_RATE = 0.005` (0.5%) and `DAILY_INTEREST_RATE = 0.0005` (0.05%).
- **Concrete Fix:** Update Table 5.2 in `design.md` to conform to SPECIFICATION.md D05 and `InstallmentLoanManager.java`:
  - `LOAN-CORP-01`: Eligible Borrower: `Operational Company`, Tenor Options: `7, 14, 30 Hari`, Daily Rate: `0.05% / hari`, Origination Fee: `0.50%`, Max Principal: `2× Monthly Revenue`.
  This aligns corporate loans with personal loans on rates and tenors while retaining distinct borrower eligibility and underwriting limits.

---

### Finding 5 [HIGH]: Inconsistency and Accessibility Breakdown in Luxury Collection Catalog (D10)
- **Location:** `Work/.agents/tasks/design.md`, Section 5.4 ("Luxury Collections Catalog (12 Items)"), lines 463–487; Section 6.1, line 538.
- **Problem:**
  1. `design.md` Table 5.4 creates an entirely new set of 12 items (`COL-VEH-01`, `COL-JWL-01`, etc.) where the lowest price is Rp 350,000,000 (and the cheapest vehicle is Rp 850,000,000).
  2. This violates SPECIFICATION.md Section 20 (D10): *"Harga dan upkeep bervariasi agar ada target awal/menengah/panjang; jangan seluruhnya miliaran ketika progres awal Rp50 juta tanpa jalur earned yang realistis."*
  3. It ignores the fact that `CollectionManager.java` **already defines 12 items** with accessible entry-level options (`COL-CAR-00` Vespa at Rp 25M, `COL-PROP-00` Ruko BSD at Rp 45M, `COL-JEW-01` Cincin Safir at Rp 35M, `COL-ART-00` Rolex Vintage at Rp 15M).
  4. Section 6.1 incorrectly claims `CollectionManager.java` must be "expanded to 12 items".
- **Concrete Fix:**
  1. Replace Table 5.4 in `design.md` with the canonical 12 items already implemented in `CollectionManager.java`:
     - Vehicles: `COL-CAR-00` (Vespa Rp 25M), `COL-CAR-01` (Porsche 911 Rp 4.5B), `COL-CAR-02` (Ferrari 296 Rp 8B).
     - Real Estate: `COL-PROP-00` (Ruko BSD Rp 45M), `COL-PROP-01` (Penthouse SCBD Rp 15B), `COL-PROP-02` (Villa Uluwatu Rp 25B).
     - Jewelry: `COL-JEW-01` (Cincin Safir Rp 35M), `COL-JEW-02` (Kalung Berlian Rp 2.5B), `COL-JEW-03` (Tiara Kerajaan Rp 18B).
     - Art & Watches: `COL-ART-00` (Rolex Submariner Rp 15M), `COL-ART-01` (Patek Philippe Rp 5B), `COL-ART-02` (Lukisan Raden Saleh Rp 12B).
  2. Clarify that `CollectionManager.java` already has 12 items, and specify adding the `pledged` boolean flag and lock mechanism for loan collateral.

---

### Finding 6 [HIGH]: Omission of Operational Company Realistic Revenue/Cost Formulas (D11)
- **Location:** `Work/.agents/tasks/design.md`, Part I FR-04, lines 75–83; Part II Section 3 (Bisnis tab), lines 249–258; Section 4 (F5), lines 283–295.
- **Problem:** Currently, `OperatingCompany.java` line 112 hardcodes revenue as:
  `long basePerLevel = 25_000_000L; // 25 jt basis revenue harian per level`
  SPECIFICATION.md Section 21 explicitly warns:
  *"D11: minimal tiga sektor awal yang berbeda (misalnya jasa, ritel, manufaktur); masing-masing mempunyai biaya/kapasitas, pendapatan dari demand/capacity/productivity serta operating cost... Jangan memakai fixed Rp25 juta/hari untuk semua sektor/level tanpa model biaya, demand dan pembuktian balancing."*
  `design.md` provides no model for sector-specific operating costs, market demand fluctuations, capacity constraints, or the economic consequences of failing to meet payroll (payroll arrears).
- **Concrete Fix:** Add Section 5.6 ("Operational Company Sector Model & Economics (D11)") to `design.md`:
  1. **Sector Differentiation:**
     - *Jasa / Konsultan*: Base capacity Rp 8M/day/level, low fixed overhead (Rp 1M/day), highly sensitive to staff morale and skill level ($\pm 40\%$).
     - *Ritel*: Base capacity Rp 20M/day/level, moderate operating overhead (Rp 5M/day), demand scales with overall market sentiment index.
     - *Manufaktur*: Base capacity Rp 50M/day/level, high fixed plant overhead (Rp 15M/day), requires minimum 3 operational staff to run at capacity.
  2. **Daily Cashflow Formulas:**
     $$\text{Daily Revenue} = \text{BaseCapacity} \times \text{Level} \times \text{ProductivityMultiplier} \times \text{DemandFactor}$$
     $$\text{Daily Expenses} = \text{SectorOverhead} \times \text{Level} + \text{DailyPayroll}$$
  3. **Payroll Arrears Handling:**
     If company cash < payroll, record unpaid amount as `payrollArrears`, decrease company morale by 20 points, and reduce operational capacity by 50% until arrears are cleared via capital injection.

---

### Finding 7 [MEDIUM]: Unspecified Queue and Dealing Settlement Mechanics for Mutual Funds (D06)
- **Location:** `Work/.agents/tasks/design.md`, Section 5.1 ("Mutual Funds with Unit/NAV Accounting"), lines 388–410.
- **Problem:** `design.md` states that orders are queued upon submission and settled at 16:00 WIB cutoff. However, the existing code in `MutualFund.java` immediately issues units on current NAV (`Langsung terbitkan unit pada NAB terkini`).
  `design.md` fails to specify:
  1. The pending order queue structure and persistence in `SaveState`.
  2. The cancellation deadline: SPECIFICATION.md Section 13 mandates *"Pending request mempunyai receipt dan cancellation deadline."*
  3. The bridge and facade API for cancelling pending orders or querying pending fund dealing status.
- **Concrete Fix:** In Section 5.1:
  1. Specify that `submitSubscription` reserves cash (`Account.reserveCash(amount)`) and appends a `PendingFundOrder` with status `ACCEPTED`.
  2. Specify that orders placed before 16:00 WIB are scheduled for the current day's 16:00 cutoff; orders placed after 16:00 WIB roll over to the next day's 16:00 cutoff.
  3. Define the cancellation deadline as 15:30 WIB (30 minutes prior to cutoff). Add `cancelFundOrder(String orderId)` to `TradingFacade.java` and `TradingWebBridge.java`, releasing reserved cash/units upon cancellation.
  4. Specify `PendingFundOrder` serialization in `exportState()` and `restoreState()` of `MutualFundManager.java`.

---

### Finding 8 [HIGH]: Incomplete Coverage of 26 Feature Groups and D01–D16 Design Decisions
- **Location:** `Work/.agents/tasks/design.md`, throughout.
- **Problem:** While `design.md` claims complete coverage, several critical design items and feature groups from SPECIFICATION.md are either missing or reduced to passing mentions without technical rules:
  1. **D01 (Deterministic Intraday Event Ordering at Same Timestamp):** Missing specification of the required sequence: Market mutations/due events $\rightarrow$ accruals & obligations $\rightarrow$ end-of-period snapshot $\rightarrow$ mission/stat/league evaluation $\rightarrow$ next period open.
  2. **D03 (Asset Allocation Denominator):** Missing specification for gross asset denominator calculation, handling denominator $\le 0$, and non-negative debt slice presentation.
  3. **D04 (Portfolio Health & Risk Categories):** Missing explicit categorical rules and threshold metrics for diversification, liquidity reserve, and debt service.
  4. **D12 (Bank Deposits Status):** Missing documentation that bank deposits are classified as `REFERENCE_UNCERTAIN` and not implemented as a synthetic product.
  5. **D14 (Optional Prestige Retention Map):** Missing the explicit domain retention map detailing what resets (cash, active positions, company accounts, personal loans) vs what persists (career achievements, badges, lifetime trade stats, prestige multipliers).
  6. **D16 (Player Profile & Local Preferences Persistence):** Missing specification for local persistent preferences (Indonesian language default, font scaling, high-contrast, reduced-motion) in save state.
  7. **Corporate Actions & IPO (REF:ipo, FR-13/14):** Missing concrete lifecycle design for IPO subscriptions, allotment refunds, and shareholder voting agendas.
- **Concrete Fix:** Expand Part II of `design.md` with dedicated subsections for D01, D03, D04, D12, D14, D16, and the complete 26 feature groups, providing explicit algorithmic rules, state machines, and schemas matching SPECIFICATION.md.

---

### Finding 9 [MEDIUM]: NPC Names in League Leaderboard Violate SPECIFICATION.md (D09)
- **Location:** `Work/.agents/tasks/design.md`, Section 5.3 ("20-NPC League System (D09)"), lines 426–442; and `LeagueManager.java`.
- **Problem:** In `LeagueManager.java`, the 20 NPC competitors are initialized with the names of real, living Indonesian billionaires ("Prajogo Pangestu", "Chairul Tanjung", "Low Tuck Kwong", etc.). SPECIFICATION.md Section 19 (D09) explicitly dictates:
  *"20 NPC fiktif dengan profil risiko/strategi beragam dan state tersimpan; dapat memakai/menaut akun NPC pasar existing. **Nama tokoh nyata tidak menjadi default.**"*
  `design.md` fails to identify this compliance issue or specify fictional replacement names.
- **Concrete Fix:** In Section 5.3 of `design.md`, specify a complete roster of 20 fully fictional Indonesian competitor names and archetypes (e.g., "Bambang Wijaya - Konglomerat Energi Nusantara", "Ratna Kusuma - Ratu Ritel Nasional", "Surya Adiputra - Spekulan Momentum"), and add a task in Section 6.1 to update `LeagueManager.java`'s `initializeNpcRoster()` with fictional names.

---

### Finding 10 [MEDIUM]: Missing Input Validation Contracts and Error Codes for Bridge Mutations
- **Location:** `Work/.agents/tasks/design.md`, Section 8 ("Error Handling & Logging Strategy"), lines 561–575.
- **Problem:** Section 8 provides only a generic JSON error envelope without specifying input validation rules, bounds, and machine-readable error codes for critical domain mutations. Developers need concrete validation constraints to prevent unhandled domain exceptions across the bridge.
- **Concrete Fix:** Add an "Input Validation & Error Code Matrix" in Section 8 covering:
  - Loan disbursement: principal $\ge \text{Rp } 1,000,000$, tenor $\in \{7, 14, 30\}$, error: `BELOW_MINIMUM_PRINCIPAL`, `INVALID_TENOR`.
  - Loan prepayment: loan must be active, error: `LOAN_ALREADY_CLOSED`.
  - Mutual fund subscribe: cash $\ge \text{Rp } 10,000$, IDR only, available cash $\ge$ gross, error: `BELOW_MINIMUM_SUBSCRIPTION`, `INSUFFICIENT_FUNDS`.
  - Mutual fund redeem: units $> 0$, units $\le$ held units, error: `INVALID_UNIT_QUANTITY`, `INSUFFICIENT_UNITS`.
  - Company founding: capital $\ge \text{Rp } 25,000,000$, sector $\in \{\text{"Jasa"}, \text{"Ritel"}, \text{"Manufaktur"}\}$, error: `INSUFFICIENT_CAPITAL`, `INVALID_SECTOR`.
  - Collection purchase: item not already owned, cash $\ge$ price, error: `ITEM_ALREADY_OWNED`, `INSUFFICIENT_FUNDS`.
  - Collection sale: item owned, item not pledged as collateral, error: `ITEM_NOT_OWNED`, `ITEM_PLEDGED_AS_COLLATERAL`.

---

### Finding 11 [NIT]: Discrepancy in F3 and F5 Problem Statements vs Existing Repository State
- **Location:** `Work/.agents/tasks/design.md`, Section 4 ("Finding F3" and "Finding F5"), lines 272–295.
- **Problem:**
  - F3 states that `initializeNewGame()` lacks `.reset()` calls on child managers. In the actual codebase (`TradingFacade.java` lines 343–353), these `.reset()` calls are already present.
  - F5 states that modal controllers in `app.js` are missing. In fact, 6 of the 7 listed modals are already implemented in `app.js`; only `openCompanyStaffModal()` is missing.
- **Concrete Fix:** Update the problem descriptions in Section 4 to reflect the true current state of the repository. Position F3 as creating the automated test `CompanyCleanResetTest.java` to prevent regression, and focus F5 on implementing the missing `openCompanyStaffModal()` and its bridge connections.

---

## Conclusion & Next Actions

The design document demonstrates a strong architectural foundation (strict Java domain ownership, canonical time acceleration ratios, double-entry accounting, and atomic save patterns). However, due to the **6 HIGH** and **3 MEDIUM** severity findings—specifically relating to personal save protection, time-step cutoff slicing bugs, missing company staff APIs, corporate loan parameter conflicts, luxury collection catalog mismatches, realistic company economics, and incomplete D01–D16 coverage—the design is not yet ready for immediate coding.

1. Implement the concrete fixes listed in Findings 1 through 10 in `Work/.agents/tasks/design.md`.
2. Update `Work/.agents/tasks/design.md` to Version 1.1.0.
3. Resubmit for quality gate re-evaluation.
