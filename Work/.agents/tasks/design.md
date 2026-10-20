# Technical Design & Requirements Clarification: Game Saham 2D Portrait Feature Completion

**Document Version:** 1.1.0  
**Date:** October 20, 2026  
**Status:** REVISED & REMEDIATED FOR IMPLEMENTATION  
**Target Root:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`  
**Execution Authority:** Hermes Feature Completion Mandate (October 20, 2026), PRD v1.0, Design Brief v1.0, Phase Quality Standards, and Independent Design Review Response (October 20, 2026).

---

# PART I: REQUIREMENTS CLARIFICATION

## 1. Summary

The Game Saham 2D Portrait project is an offline Android simulation game coupling realistic stock market trading with an in-depth fund management, enterprise building, and career progression experience. Players begin as an individual retail investor managing Rp 50,000,000, advancing through professional analyst tiers, institutional client asset management (AUM), founding operational enterprises, and collecting high-prestige assets.

This feature completion effort closes all architectural, domain, and frontend gaps identified during the October 20, 2026 audit (Findings F1–F9) and addresses all 11 findings raised in the Independent Design Review (`design-review.md`). It establishes:
1. Strict separation of concerns where the Java 17 core domain (`android/app/src/main/java/com/tradingglobal3d/core/`) is the sole custodian of economic ledgers, accounts, portfolio calculations, market matching, clocks, game state, and save persistence. The Web frontend (`assets/www/`) acts strictly as a presentation and user-input layer.
2. Canonical time synchronization guaranteeing active 1× foreground execution (1 game day = 5 minutes real time, $288\times$ ratio) and inactive/closed background simulation (1 game day = 1 hour real time, $24\times$ ratio, never inheriting foreground speed). Discrete step slicing guarantees mathematical and orderbook equivalence between single large steps and multi-step advances (F2), with exact boundary handling for the 16:00 WIB mutual fund cutoff and midnight rollovers.
3. Complete implementation of operational company mechanics across three distinct sectors (Jasa, Ritel, Manufaktur) with realistic capacity, overhead, demand, and payroll arrears models (D11). Full bridge and UI workflows for hiring, firing, and training company staff (F5 / D11).
4. Tenor-based installment loans (7, 14, 30 days at 0.05%/day, 0.50% origination fee) for personal collateralized borrowing and corporate working capital (D05).
5. Canonical 12-item luxury collection catalog (`COL-CAR-00` through `COL-ART-02`) with early-to-endgame accessibility (starting at Rp 15M) and loan collateral pledging mechanics (D10).
6. Unit/NAV mutual funds with a 16:00 WIB daily cutoff, persistent pending dealing queue, and a 15:30 WIB cancellation deadline (D06).
7. Persistent 20-NPC league competitions with fully fictional Indonesian competitor names and diverse archetypes (D09), weekly rollovers, rival pairings, and rotating periodic missions (D07, D08).
8. Comprehensive coverage of all 26 reference feature groups and design decisions D01–D16, including consolidated net worth accounting (D02 / F4), gross asset allocation denominator (D03), portfolio health risk categories (D04), bank deposits reference uncertain classification (D12), optional prestige retention map (D14), and localized player preferences (D16).
9. Personal save safeguards guaranteeing zero destructive testing or resets against `Artifacts/LocalPreview/preview-save/save_game_v2.json`.

## 2. Functional Requirements (FR)

### FR-01: Architectural Boundary & Bridge Communication
- **FR-01.1**: The Java core domain shall retain exclusive ownership of the economy ledger, account balances, portfolios, order matching, game clock, business entities, and persistence.
- **FR-01.2**: The Web UI (`index.html`, `app.js`, `style.css`) shall not perform economic calculations, simulate prices, or maintain authoritative game clocks.
- **FR-01.3**: Communication across the WebView bridge (`TradingWebBridge.java`) shall be strictly asynchronous, idempotent (via unique `requestId`), and typed using explicit JSON schemas with decimal strings for monetary values.

### FR-02: Canonical Dual-Rate Time & Calendar Engine
- **FR-02.1**: Foreground active clock speed at 1× shall advance at 86,400 game seconds per 300 real seconds ($288\times$ speed). Optional 2× ($576\times$) and 5× ($1,440\times$) speeds shall only function while the application is in active foreground.
- **FR-02.2**: Background/closed lifecycle clock shall advance at 86,400 game seconds per 3,600 real seconds ($24\times$ speed), never inheriting foreground multipliers.
- **FR-02.3**: Skipping a day (`skipDay`) shall advance the simulation by exactly 86,400 game seconds following explicit user confirmation.
- **FR-02.4**: Intraday processing shall partition time into discrete quanta ($\le 60$ seconds, market session transitions, 16:00 WIB cutoff boundary, or midnight). Advancing 300 seconds in a single call must produce identical market depth, resting orders, and ledger entries as 300 sequential 1-second ticks (addressing F2).
- **FR-02.5**: Calendar follows `Asia/Jakarta` (WIB) with day rollover at 00:00 and week rollover on Monday 00:00. Deterministic event sequence at identical timestamps: Market mutations $\rightarrow$ Accruals & obligations $\rightarrow$ End-of-period snapshot $\rightarrow$ Mission/stat/league evaluation $\rightarrow$ Next period open (D01).

### FR-03: Consolidated Wealth & Accounting (D02)
- **FR-03.1**: Consolidated Net Worth shall be calculated as:  
  $$\text{Net Worth} = \text{Personal Assets} + \text{Company Net Equity} + \text{Collection Valuation} - \text{Personal Liabilities}$$  
  where Company Net Equity is $\max(0, \text{Company Assets} - \text{Company Debt})$.
- **FR-03.2**: Capital transfers between personal accounts and 100%-owned operational companies shall be value-neutral (e.g., transferring Rp 25M cash results in Rp 25M personal cash + Rp 25M company equity = Rp 50M total wealth), eliminating net worth deflation (addressing F4).
- **FR-03.3**: Client AUM and mutual fund asset pools shall be strictly isolated from personal net worth.

### FR-04: Operational Companies & Enterprise Management (D11)
- **FR-04.1**: Players may found operational companies across three sectors (Jasa/Konsultan, Ritel, Manufaktur) with initial capital $\ge \text{Rp } 25,000,000$.
- **FR-04.2**: Each company maintains isolated accounts, sector-specific daily revenue and overhead models, staff rosters (CEO + operational staff), and work policies (6/8/10 hours, standard/premium/executive benefits).
- **FR-04.3**: Full bridge and UI workflows for staff management (hiring from candidate pool, assigning roles, training up to level 5, firing with severance pay).
- **FR-04.4**: Inability to meet payroll triggers accrued `payrollArrears`, reducing morale by 20 points and slashing operational capacity by 50% until arrears are settled.

### FR-05: Luxury Collections & Lifestyle Valuation (D10)
- **FR-05.1**: Canonical catalog of 12 luxury items across 4 categories: Vehicles, Real Estate, Jewelry, and Art & Watches, with early-game accessibility starting at Rp 15,000,000.
- **FR-05.2**: Items incur daily maintenance fees debited at midnight and provide career reputation bonuses.
- **FR-05.3**: Items may be purchased, sold at fair appraisal value, or pledged as collateral for installment loans. Pledged items cannot be sold or re-pledged.

### FR-06: Mutual Funds with Unit/NAV Accounting (D06)
- **FR-06.1**: Catalog of three mutual funds (Pasar Uang, Pendapatan Tetap, Indeks Saham) with dedicated asset/liability pools. Initial NAV = Rp 10,000.00; unit precision to 6 decimals, price precision to 8 decimals.
- **FR-06.2**: Orders placed before 16:00 WIB are queued in `pendingOrders` and settled against the 16:00 WIB cutoff NAV. Orders placed after 16:00 WIB roll over to the next trading day's cutoff.
- **FR-06.3**: Pending orders may be cancelled prior to the 15:30 WIB cancellation deadline, releasing reserved cash or units.
- **FR-06.4**: Management fees (0.75% annualized / 365 days) accrue daily into fund liabilities.

### FR-07: Tenor-Based Installment Loans (D05)
- **FR-07.1**: Catalog of two installment loan products: Personal Collateralized Loan and Corporate Working Capital Loan.
- **FR-07.2**: Tenor options of 7, 14, and 30 game days. Daily rate of 0.05% per day on remaining principal. Upfront origination fee of 0.50% deducted from disbursement cash. Minimum principal Rp 1,000,000.
- **FR-07.3**: Daily principal installment equals $\lfloor P / T \rfloor$ with final-day rounding adjustment. Prepayment pays remaining principal plus today's accrued interest with zero future penalty. Late payments incur a 0.05%/day penalty capped at 5% of original principal.

### FR-08: Competitions, League, Rivals & Missions (D07, D08, D09)
- **FR-08.1**: Persistent 3-tier league (Bronze, Silver, Gold) with 20 fictional Indonesian NPC competitors running authentic portfolio simulations.
- **FR-08.2**: Weekly rollover every Monday 00:00 WIB evaluating rankings via Modified Dietz return and total wealth, executing promotions and relegations.
- **FR-08.3**: Weekly rival automatically paired from nearby leaderboard competitors in the same tier.
- **FR-08.4**: Rotating periodic missions (6 daily and 6 weekly templates, presenting 3 active daily and 3 active weekly missions) rewarding cash and XP. Permanent career achievements spanning 18 badges awarding prestige XP only.

### FR-09: Historical Replay & Market Laboratory
- **FR-09.1**: Full UI and bridge integration for Historical Replay mode (`HISTORICAL_REPLAY`) using verified, legally compliant datasets (e.g., IDX BBCA/BBRI/TLKM under CC BY-NC 4.0), resolving F1 and F8.
- **FR-09.2**: Strict prevention of look-ahead bias (future candle leakage). All replay market data standardizes on unadjusted Close prices.
- **FR-09.3**: Sandboxed Strategy Lab and Stress Testing tools allowing isolated portfolio simulations without mutating live career save state.

### FR-10: Optional Prestige & Clean Reset
- **FR-10.1**: Clean session teardown on New Game without orphaned company accounts or manager memory leaks (addressing F3).
- **FR-10.2**: Optional Prestige mode accessible to top career ranks (CIO / Portfolio Manager) with a 2-step confirmation, creating an atomic backup before resetting active runs while preserving lifetime statistics and awarding prestige XP bonuses (capped at 10%).

## 3. Non-Functional Requirements (NFR)

- **NFR-01 (Performance & Latency)**: Local host/browser P95 for standard user actions $\le 300\text{ ms}$; page and tab navigation $\le 1,000\text{ ms}$; heavy background tasks (Skip Day, offline catchup) must render feedback/indicators $\le 300\text{ ms}$ without freezing the main thread (addressing F7). Minimum 30 samples per discrete action type.
- **NFR-02 (Responsiveness & Viewports)**: Seamless visual presentation across 7 standardized viewports (320×568, 360×640, 390×844, 412×915, 430×932, 768×1024, 1440×900) under both normal (100%) and 200% font scaling without horizontal overflow, text clipping, or obscured touch targets (addressing F6).
- **NFR-03 (Accessibility)**: Touch targets $\ge 48\text{ dp}$ equivalent; contrast ratio $\ge 4.5:1$ for normal text and $\ge 3:1$ for graphical UI elements (WCAG 2.1 AA); full keyboard/focus navigability and respect for `prefers-reduced-motion`.
- **NFR-04 (Persistence & Reliability)**: Atomic state serialization using write-replace (`.tmp` to `.json` with `.bak` fallback). Zero data corruption or orphaned accounts on abrupt process termination. Strict directory isolation ensuring personal preview saves are never targeted by automated test suites.
- **NFR-05 (Offline Autonomy)**: Zero dependency on external backend servers, remote CDNs, real money transactions, or online authentication.

## 4. Acceptance Criteria (AC)

1. **AC-01 (F1 Resolution - Replay Integration)**: Selecting `HISTORICAL_REPLAY` via UI/bridge loads verified bars, initializes the simulation at bar 0 date, allows trading, advances bars on day close, and serializes/deserializes without error.
2. **AC-02 (F2 Resolution - Time Step Equivalence)**: Advancing 300,000 ms in a single call vs. 300 sequential calls of 1,000 ms from the same initial timestamp and seed yields identical order book depth ($\pm 0$), resting orders ($\pm 0$), and ledger entries. Slicing algorithm guarantees execution of 16:00 WIB mutual fund cutoff and midnight rollovers.
3. **AC-03 (F3 Resolution - Clean Reset Invariant)**: Automated test `CompanyCleanResetTest.java` verifies that after founding multiple companies and taking loans, calling `initializeNewGame()` results in exactly 0 residual companies, 0 orphaned accounts, and clean child manager states.
4. **AC-04 (F4 Resolution - Consolidated Net Worth)**: Transferring Rp 25,000,000 from personal cash to an owned company results in exactly Rp 50,000,000 displayed consolidated net worth ($25\text{M cash} + 25\text{M equity}$).
5. **AC-05 (F5 Resolution - Complete UI & Staff Journeys)**: Every company, staff, and collection action is accessible via UI: `openFoundCompanyModal()`, `openCompanyStaffModal()` (hire, fire, train), `openCompanyPolicyModal()`, `openCompanyCapitalModal()`, and `openCollectionsGalleryModal()` (buy, sell, pledge).
6. **AC-06 (F6 Resolution - Viewport & Font Scale Matrix)**: Automated Playwright matrix across 7 viewports $\times$ 2 font scales (100% and 200%) records 0 DOM overlaps, 0 horizontal scroll overflows, and 0 obscured CTAs.
7. **AC-07 (F7 Resolution - Latency & Responsiveness Benchmark)**: Benchmark suite records $\ge 30$ samples per action type, proving P95 $\le 300\text{ ms}$, navigation $\le 1,000\text{ ms}$, and heavy process feedback $\le 300\text{ ms}$.
8. **AC-08 (F8 Resolution - Dataset Provenance)**: Replay manifests explicitly declare source licenses (CC BY-NC 4.0 for IDX, Apache 2.0 attribution), verify SHA-256 checksums, and consistently bind to unadjusted Close prices.
9. **AC-09 (F9 Resolution - Reconciled Status)**: `STATUS.json`, `REQUIREMENTS.csv`, and `GAP-MATRIX.csv` reflect 100% verified status backed by real test logs.
10. **AC-10 (Mutual Fund Dealing & Cancellation)**: Subscribing Rp 1,000,000 into a fund with NAB Rp 10,000 and 0.1% entry fee places a pending order, which settles at 16:00 cutoff into exactly 99.900000 units. Cancelling before 15:30 WIB refunds full Rp 1,000,000 cash.
11. **AC-11 (Loan Amortization & Corporate Terms)**: Taking a Rp 1,000,000 corporate or personal loan with 7-day tenor and 0.5% fee yields Rp 995,000 cash and Rp 1,000,000 debt principal. Daily principal installment is Rp 142,857 ($7\times 142,857 = 999,999 + 1$ rounding adjustment on day 7). Prepaying on day 3 pays remaining principal + accrued interest with zero future interest penalty.
12. **AC-12 (League & Missions Rollover)**: Crossing Monday 00:00 WIB game time executes weekly league rankings for all 21 competitors (player + 20 fictional NPCs), updates tiers, archives history, rotates missions, and refreshes the weekly rival.

## 5. Out of Scope

- 3D graphics, camera movement, or Three.js/Unity runtimes.
- Real-money transactions, brokerage APIs, or live real-world trading feeds.
- Online multiplayer, remote matchmaking, or cloud-hosted leaderboards.
- Mandatory forced bankruptcy or game-over hard stops (the game is infinite/endless).
- Synthetic bank deposits product (`REFERENCE_UNCERTAIN`).

---

# PART II: COMPREHENSIVE TECHNICAL DESIGN

## 1. Architectural Overview & Component Boundaries

The Game Saham 2D Portrait architecture follows a strict decoupled pattern:
- **Core Domain (Java 17)**: Resides in `android/app/src/main/java/com/tradingglobal3d/core/`. It contains all business entities, economic ledgers, mathematical pricing formulas, discrete event simulation, and storage. It is completely independent of Android UI and web frameworks, making it 100% unit-testable on standard JVMs.
- **Web Presentation (HTML5/CSS3/ES6)**: Resides in `android/app/src/main/assets/www/`. Built with vanilla web standards without external framework dependencies (React, Vue, Angular are avoided to minimize footprint and build complexity). It is purely a reactive view layer that renders snapshots and forwards user intent.
- **Native Web Bridge**: Encapsulated in `TradingWebBridge.java`. Uses Android's `@JavascriptInterface` mechanism to receive JSON request strings and return JSON response strings.

```
+-----------------------------------------------------------------------------------+
|                            Android WebView Container                              |
|  +-----------------------------------------------------------------------------+  |
|  |                     Web Presentation Layer (assets/www/)                    |  |
|  |   - index.html (5-Tab Shell, Header Chip, Modal Flow Panels)                |  |
|  |   - app.js (Event Listeners, DOM Builders, Snapshot Renderers)              |  |
|  |   - style.css (Design Tokens, Responsive Viewports, 200% Font Scale)        |  |
|  +-----------------------------------------------------------------------------+  |
|                                       | JSON Calls (asynchronous / idempotent)    |
|                                       v                                           |
|  +-----------------------------------------------------------------------------+  |
|  |              TradingWebBridge.java (@JavascriptInterface)                   |  |
|  |   - Input Validation, Type Deserialization, Error Envelope Formatting       |  |
|  +-----------------------------------------------------------------------------+  |
+---------------------------------------|-------------------------------------------+
                                        | Java Method Calls
                                        v
+-----------------------------------------------------------------------------------+
|                        Core Domain Engine (Java 17 JVM)                           |
|  +-----------------------------------------------------------------------------+  |
|  |                             TradingFacade                                   |  |
|  |   - Single Source of Truth, Lifecycle Coordinator, Snapshot Generator       |  |
|  +-----------------------------------------------------------------------------+  |
|     |            |             |              |             |              |      |
|     v            v             v              v             v              v      |
|  +-------+  +----------+  +----------+  +------------+  +----------+  +--------+  |
|  | Game  |  | Economy  |  | Market   |  | Order      |  | Company  |  | Mutual |  |
|  | Clock |  | Ledger   |  | Engine   |  | Manager    |  | & Staff  |  | Funds  |  |
|  +-------+  +----------+  +----------+  +------------+  +----------+  +--------+  |
|     |            |             |              |             |              |      |
|     v            v             v              v             v              v      |
|  +-------+  +----------+  +----------+  +------------+  +----------+  +--------+  |
|  | Loans |  | League   |  | Periodic |  | Collection |  | Replay   |  | Save   |  |
|  | Tenor |  | & Rivals |  | Missions |  | Catalog    |  | Engine   |  | Manager|  |
|  +-------+  +----------+  +----------+  +------------+  +----------+  +--------+  |
+-----------------------------------------------------------------------------------+
```

### Technology Stack (Locked)
- **Language / Runtime**: Java 17 (OpenJDK / Android API 26+ compatible), JavaScript ES6 Vanilla.
- **Frontend Assets**: HTML5 Semantic markup, CSS3 Custom Properties (Design Tokens), System Sans-Serif font with IBM Plex Sans fallback.
- **Testing & Toolchain**: Gradle 8.x, Android Gradle Plugin 8.x, JUnit 4/5, headless Chromium / Playwright for visual matrix, Python 3 for validation scripts.

---

## 2. Canonical Time Architecture & Invariant Step Pipeline

### Dual-Rate Time Configuration
1. **Foreground / Active (1× default)**:
   - $1\text{ Game Day} = 86,400\text{ game seconds} = 300\text{ real seconds} = 5\text{ real minutes}$.
   - Speed ratio: $\frac{86,400}{300} = 288\times$.
   - Speed modifiers ($2\times \rightarrow 576\times$, $5\times \rightarrow 1,440\times$) are ephemeral and apply only while the window is in active foreground.
2. **Background / Inactive / App Closed**:
   - $1\text{ Game Day} = 86,400\text{ game seconds} = 3,600\text{ real seconds} = 1\text{ real hour}$.
   - Speed ratio: $\frac{86,400}{3,600} = 24\times$.
   - **Critical Rule**: When resuming or calculating offline catchup, the clock MUST enforce the $24\times$ ratio regardless of what speed multiplier was active when the app was closed.
3. **Skip Day**:
   - Immediately steps forward by exactly $86,400$ game seconds.
   - Requires explicit modal confirmation warning the player of pending orders, debt installments, and payroll.

### Slicing Algorithm & Exact Boundary Handling (Remediating Finding 2 & F2)
To guarantee step-invariance and ensure critical daily events (such as the 16:00 WIB mutual fund cutoff and 00:00 midnight rollovers) are never skipped during large advancements, `advanceWorldSeconds` slices steps dynamically:

$$\Delta t_{\text{step}} = \min(remaining, \min(60, \min(secondsToMidnight, \min(secondsToNextTransition, secondsToCutoff))))$$

```java
public synchronized boolean advanceWorldSeconds(long remainingSeconds) {
    LocalDate initialDate = clock.getCurrentDate();
    while (remainingSeconds > 0) {
        long currentSecond = clock.getCurrentTime().toSecondOfDay();
        long midnightSecond = 86400;
        long secondsToMidnight = midnightSecond - currentSecond;
        long secondsToNextTransition = getSecondsToNextMarketSessionTransition();
        
        // 16:00 WIB cutoff is at second 57600
        long cutoffSecond = 16 * 3600;
        long secondsToCutoff = (currentSecond < cutoffSecond) ? (cutoffSecond - currentSecond) : Long.MAX_VALUE;
        
        long step = Math.min(remainingSeconds, 
                    Math.min(60, 
                    Math.min(secondsToMidnight, 
                    Math.min(secondsToNextTransition, secondsToCutoff))));
        
        long nextSecond = currentSecond + step;
        
        // Advance clock by discrete step
        if (step == secondsToMidnight) {
            clock.advanceGameSeconds(step - 1);
            settleDay(false);
            clock.advanceGameSeconds(1);
            processCalendarActions(clock.getCurrentDate());
        } else {
            clock.advanceGameSeconds(step);
        }
        remainingSeconds -= step;
        
        // Evaluate market trading & order book matching
        MarketSession session = clock.getCurrentMarketSession();
        if (session.isOpenForTrading()) {
            marketEngine.generateNpcLiquidity(catalog, clock.getCurrentTime());
            orderManager.matchRestingOrders(catalog, marketEngine);
        }
        
        // Evaluate price triggers (stop-loss, take-profit)
        evaluatePriceTriggers();
        
        // Process mutual fund cutoff if reaching or crossing 16:00 WIB
        if (currentSecond < cutoffSecond && nextSecond >= cutoffSecond) {
            mutualFundManager.processDailyCutoff(catalog, clock.getCurrentDate());
        }
    }
    return !initialDate.equals(clock.getCurrentDate());
}
```

This algorithm guarantees:
- Step sizes never exceed 60 seconds.
- Clock steps align exactly to session transitions, 16:00 WIB cutoff, and 00:00 midnight.
- 1 call of 300,000 ms produces identical economic results to 300 calls of 1,000 ms.

---

## 3. Hub & Navigation Architecture (5 Tabs & 40 Screen Goals)

The user interface is organized into five primary tabs hosting all 26 reference groups and 40 PRD screen goals:

```
+-----------------------------------------------------------------------------+
| [Header] Game Saham  |  Net Worth: Rp 50.000.000  |  10:30 WIB  |  1x [Play] |
+-----------------------------------------------------------------------------+
|                                                                             |
|  [TAB 1: BERANDA]                                                           |
|  - Wealth Summary & 24h PnL                                                 |
|  - Career Level & XP Progress                                               |
|  - Quick Skip Day Button & Session Briefing                                 |
|  - Empire Quick Glance (Active Companies, Total Payroll)                    |
|  - Competitions Glance (League Tier, Rival Card)                            |
|  - Market Movers (Top 3 Gainers / Losers)                                   |
|                                                                             |
|  [TAB 2: PASAR]                                                             |
|  - Market Indices Carousel (IHSG, LQ45, S&P 500)                            |
|  - Category Pills: Saham IDX | Saham US | Reksa Dana | Obligasi | IPO | FX  |
|  - Search & Filter Bar (Sector, Active, Watchlist)                          |
|  - Live Quote Instrument Table                                              |
|  - Instrument Detail Drawer: OHLCV Candle/Line, Depth Tape, Trade Form      |
|                                                                             |
|  [TAB 3: PORTOFOLIO]                                                        |
|  - Consolidated Wealth Breakdown (Cash, Stocks, Funds, Empire, Collections) |
|  - Asset Allocation Visualization (Donut / Progress Bars + Exact Table)     |
|  - Portfolio Health & Risk Metrics (Margin ratio, debt service, drawdown)   |
|  - Debt & Loans Sub-Hub (Margin Facilities, Tenor Installment Loans)        |
|  - Audit Ledger & Transaction History                                       |
|  - Financial Tools: Stress Test Sandbox & Strategy Lab                      |
|                                                                             |
|  [TAB 4: BISNIS]                                                            |
|  - Operational Companies (Empire Management):                               |
|    * Company Cards (Cash, Revenue, Payroll, Morale, Level)                  |
|    * + Dirikan Perusahaan Modal (Sector choice, Capital >= Rp 25M)          |
|    * Staff & CEO Management (Hire, Assign, Train, Terminate)                |
|    * Workplace Policies (6h/8h/10h, Benefits: Standard/Premium/Exec)        |
|    * Capital Injections & Dividend Withdrawals                              |
|  - Investment Firm (AUM Management):                                        |
|    * Client Mandates, Isolated Client Portfolios                            |
|    * Management & Performance Fee Claims (High-Water Mark)                  |
|    * Multi-Asset Fund Builder                                               |
|                                                                             |
|  [TAB 5: LAINNYA]                                                           |
|  - Player Profile & Investor Archetype (Conservative/Balanced/Aggressive)   |
|  - Periodic Missions (3 Daily, 3 Weekly, Career Milestones)                 |
|  - Competition Hub: 3-Tier League Leaderboard (20 NPCs) & Weekly Rival      |
|  - Luxury Collections Gallery (12 Items, Appraisals, Maintenance, Upkeep)   |
|  - Research Terminal & News Feed (Tiered subscriptions, Economic Calendar)  |
|  - Optional Prestige (Career CIO/Portfolio Manager, Atomic Backup)          |
|  - Settings: Save/Load Slots, Text Scaling (100%-200%), Audio/Theme         |
|                                                                             |
+-----------------------------------------------------------------------------+
|  [Nav] (1) Beranda  |  (2) Pasar  |  (3) Portofolio  |  (4) Bisnis  | (5) Lainnya |
+-----------------------------------------------------------------------------+
```

---

## 4. Remediation Contracts for Audit Findings F1–F9

### Finding F1: Historical Replay UI & Bridge Flow
- **Audit Problem**: Replay mode was rejected by bridge validation on older snapshots.
- **Contract**:
  1. `TradingWebBridge.startNewGame(jsonConfig)` accepts `{"mode":"HISTORICAL_REPLAY", "datasetId":"IDX:BBCA", "initialCapital":50000000}`.
  2. The bridge invokes `facade.startHistoricalReplay(datasetId, initialCapital)`.
  3. `HistoricalDatasetLoader.loadDataset(datasetId)` reads verified daily CSV files from `android/app/src/main/assets/data/historical/`.
  4. The game clock initializes to the date of the first available bar at 09:00:00 WIB.
  5. The UI modal `openHistoricalReplayModal()` populates verified dataset choices with license attributions, price baselines, and date ranges.

### Finding F2: Invariant Time Advancement Equivalence
- **Audit Problem**: Monolithic time advance skipped session boundaries.
- **Contract**: Enforce discrete step slicing ($\le 60\text{s}$) with cutoff crossing checks in `advanceWorldSeconds()`. Invariant check:
  $$\text{Orders}_{\text{monolithic}} = \text{Orders}_{\text{sliced}}, \quad \text{Books}_{\text{monolithic}} = \text{Books}_{\text{sliced}}, \quad \text{Fills}_{\text{monolithic}} = \text{Fills}_{\text{sliced}}$$

### Finding F3: Clean Reset on New Game
- **Audit Problem**: Ensure no orphaned accounts or dangling company references persist across game restarts.
- **Codebase Reality & Contract**: `TradingFacade.java` already calls `.reset()` on all domain child managers. To guarantee zero regressions, `CompanyCleanResetTest.java` will execute an automated verification verifying that after creating multiple companies and taking loans, calling `initializeNewGame()` leaves exactly 0 residual companies, 0 orphaned accounts, and clean manager instances.

### Finding F4: Consolidated Net Worth Accounting (D02)
- **Audit Problem**: Transferring capital to an owned company debited personal cash without crediting company equity in displayed net worth.
- **Contract**: Implement D02 in `getPersonalNetWorth()`:
  ```java
  Money equity = personalAccount.getTotalPortfolioValue(Currency.IDR, usdRate);
  for (OperatingCompany comp : companyManager.getAllCompanies()) {
      Account compAcc = accounts.get(comp.getAccountId());
      if (compAcc != null) {
          Money compVal = compAcc.getTotalPortfolioValue(Currency.IDR, usdRate);
          Money compDebt = installmentLoanManager.getTotalOutstandingPrincipal(compAcc.getId());
          equity = equity.add(compVal.subtract(compDebt));
      }
  }
  equity = equity.add(collectionManager.calculateTotalValuation());
  equity = equity.add(mutualFundManager.calculateTotalHoldingsValue(personal.getId()));
  equity = equity.subtract(installmentLoanManager.getTotalOutstandingPrincipal(personal.getId()));
  return equity;
  ```

### Finding F5: Complete UI Journeys for Orphaned Features
- **Codebase Reality & Contract**: In `app.js`, 6 of the 7 modals (`openFoundCompanyModal`, `openCompanyPolicyModal`, `openCompanyCapitalModal`, `openCollectionsGalleryModal`, `openPrestigeConfirmationModal`, `openLeagueAndRivalModal`) are already implemented. Finding F5 is refocused on:
  1. Authoring the missing `openCompanyStaffModal(company)` in `app.js`.
  2. Connecting it to the new staff management bridge methods: `getCompanyStaff`, `hireCompanyStaff`, `fireCompanyStaff`, and `trainCompanyStaff`.
  3. Ensuring full UI flow with candidate inspection, role assignment, and severance warnings.

### Finding F6: UI Matrix Across 7 Viewports & 200% Font Scale
- **Contract**: Automated Playwright test runner validates:
  - Viewports: `320x568`, `360x640`, `390x844`, `412x915`, `430x932`, `768x1024`, `1440x900`.
  - Scales: 100% (`font-size: 16px`) and 200% (`font-size: 32px`).
  - Strict assertions: `scrollWidth <= clientWidth`, zero element overlaps, and CTA visibility above bottom navigation.

### Finding F7: Benchmark Methodology & Responsiveness
- **Contract**:
  - Sample size: $\ge 30$ recorded samples per discrete user action.
  - Metrics: P95 action $\le 300\text{ ms}$, Navigation $\le 1,000\text{ ms}$.
  - Heavy tasks (Skip Day, Offline Catchup) must paint a loading indicator $\le 300\text{ ms}$ before executing heavy calculations.

### Finding F8: Dataset Provenance & Price Basis
- **Contract**:
  - Document all datasets in `DATASET-PROVENANCE.md`.
  - IDX datasets (BBCA, BBRI, TLKM) licensed under CC BY-NC 4.0 with full attribution.
  - US sample data explicitly annotated with Apache 2.0 source.
  - Standardize all market calculations to use raw `Close` prices for consistency with orderbook lot matching.

### Finding F9: Consistent Status & Audit Evidence
- **Contract**: Maintain single source of truth in `STATUS.json` and `GAP-MATRIX.csv` where `verification_status` reflects actual test logs.

---

## 5. Domain & Gameplay Specifications

### 5.1 Mutual Funds with Unit/NAV Accounting (D06 & Finding 7)

#### Catalog Definition
| Fund ID | Name | Category | Risk Profile | Initial NAV | Entry/Exit Fee | Annual Mgmt Fee |
|---|---|---|---|---|---|---|
| `FUND-MM-01` | Dana Lancar Likuid | Pasar Uang | Sangat Rendah | Rp 10.000,00 | 0.00% / 0.00% | 0.50% |
| `FUND-FI-01` | Obligasi Prima Stabil | Pendapatan Tetap | Rendah-Sedang | Rp 10.000,00 | 0.10% / 0.10% | 0.75% |
| `FUND-EQ-01` | Nusantara Index Alpha | Saham Campuran | Tinggi | Rp 10.000,00 | 0.20% / 0.20% | 1.25% |

#### Dealing Settlement & Queue Mechanics (Finding 7)
1. **Dealing Queue**:
   - Subscriptions and redemptions submitted during the trading day are queued in `pendingOrders` with status `ACCEPTED`.
   - Subscribing reserves gross cash from the investor's account: `account.reserveCash(grossAmount)`.
   - Redeeming reserves units in the investor's unit register: `holdings.reserveUnits(accountId, units)`.
2. **Cutoff & Settlement (16:00 WIB)**:
   - Orders submitted prior to 16:00 WIB are scheduled for the current day's cutoff. Orders submitted at or after 16:00 WIB roll over to the next trading day's 16:00 cutoff.
   - At 16:00 WIB, `processDailyCutoff()` iterates over eligible pending orders:
     * **Subscription**: Deducts entry fee ($C_{\text{gross}} \times \text{feeRate}$), calculates units issued = $C_{\text{net}} / \text{NAV}_{\text{cutoff}}$, releases reserved cash, debits cash, and credits units to the player.
     * **Redemption**: Calculates gross payout = $U_{\text{redeem}} \times \text{NAV}_{\text{cutoff}}$, deducts exit fee, releases reserved units, burns units, and credits net cash to the player.
3. **Cancellation Deadline (15:30 WIB)**:
   - Investors may cancel pending orders up to 15:30 WIB (30 minutes prior to cutoff).
   - Calling `cancelFundOrder(orderId)` releases reserved cash/units and marks the order `CANCELLED`.
   - Requests submitted after 15:30 WIB for the current day's cutoff are locked and reject cancellation with `CANCELLATION_DEADLINE_PASSED`.
4. **Queue Persistence**:
   - `PendingFundOrder` instances are serialized into `SaveState` under `mutualFunds.pendingOrders` ensuring zero lost orders on application restart.

---

### 5.2 Tenor-Based Installment Loans (D05 & Finding 4)

#### Catalog Definition (Aligned with SPECIFICATION.md D05 & Codebase)
| Product ID | Name | Eligible Borrower | Tenor Options | Daily Rate | Origination Fee | Max Principal |
|---|---|---|---|---|---|---|
| `LOAN-PERS-01` | Pinjaman Pribadi Agunan | Personal Account | 7, 14, 30 Hari | 0.05% / hari | 0.50% | 70% Collateral Value |
| `LOAN-CORP-01` | Modal Kerja Usaha | Operational Company | 7, 14, 30 Hari | 0.05% / hari | 0.50% | 2× Monthly Revenue |

#### Loan Lifecycle & Amortization
1. **Disbursement**:
   - Gross principal $P \ge \text{Rp } 1,000,000$.
   - Origination fee: $Fee = P \times 0.005$ (0.50%).
   - Net cash disbursed: $C_{\text{disbursed}} = P - Fee$.
   - Recorded principal debt: $L = P$.
2. **Repayment Schedule**:
   - Tenor $T \in \{7, 14, 30\}$ game days.
   - Daily Principal Installment: $Instalment_{\text{principal}} = \lfloor P / T \rfloor$.
   - Last day adjustment: $Instalment_T = P - (Instalment_{\text{principal}} \times (T - 1))$.
   - Daily Interest: $Interest_t = PrincipalRemaining_t \times 0.0005$ (0.05%/day).
   - Total daily payment: $Due_t = Instalment_{\text{principal}} + Interest_t$.
3. **Prepayment (Pelunasan Dini)**:
   - Payoff amount = $PrincipalRemaining + AccruedInterestToday$.
   - Zero future interest charged. Zero prepayment penalty. Collateral immediately unpledged.
4. **Arrears & Restructuring**:
   - If cash is insufficient on due date, loan enters `ARREARS`.
   - Late penalty: $0.05\%$ per day on overdue principal, capped at $5\%$ of initial principal.
   - Restructuring extends tenor by $+14$ or $+30$ days, recalculating daily installments without forgiving debt.

---

### 5.3 Competitions, Leagues, Rivals & Missions (D07, D08, D09 & Finding 9)

#### 20-NPC League System (D09) - Fictional Competitor Roster
To comply with SPECIFICATION.md Section 19 ("Nama tokoh nyata tidak menjadi default"), the league initializes with 20 completely fictional Indonesian competitor names and diverse archetypes:

| NPC ID | Name | Professional Title | Strategy Archetype | Base Wealth (IDR) | Target Weekly Return |
|---|---|---|---|---|---|
| `NPC-01` | Bambang Wijaya | Taipan Energi Nusantara | Agresif (Momentum) | Rp 550.000.000.000 | 4.25% |
| `NPC-02` | Ratna Kusuma | Ratu Ritel Nasional | Seimbang (Growth) | Rp 380.000.000.000 | 3.80% |
| `NPC-03` | Surya Adiputra | Investor Nilai Sejati | Konservatif (Deep Value) | Rp 350.000.000.000 | 2.10% |
| `NPC-04` | Hendra Tanuwidjaja | Konglomerat Finansial Terpadu | Konservatif (Dividend) | Rp 330.000.000.000 | 2.05% |
| `NPC-05` | Dewi Anggraini | Pionir Manufaktur Modern | Seimbang (Core/Satellite) | Rp 125.000.000.000 | 3.15% |
| `NPC-06` | Agus Pratama | Raja Media Digital | Seimbang (Growth) | Rp 80.000.000.000 | 2.90% |
| `NPC-07` | Tirta Setiawan | Pengembang Properti Ibu Kota | Seimbang (Value) | Rp 65.000.000.000 | 2.45% |
| `NPC-08` | Maya Indraswari | Pengusaha Agrobisnis Mandiri | Konservatif (Income) | Rp 45.000.000.000 | 1.85% |
| `NPC-09` | Budi Santoso | Pakar Distribusi Logistik | Konservatif (Income) | Rp 35.000.000.000 | 1.95% |
| `NPC-10` | Fajar Nugroho | Taipan Otomotif & Mesin | Seimbang (Industrial) | Rp 30.000.000.000 | 2.60% |
| `NPC-11` | Gunawan Hartanto | Pengusaha Sawit Berkelanjutan | Seimbang (Commodity) | Rp 25.000.000.000 | 2.30% |
| `NPC-12` | Lukman Hakim | Investor Industri Material | Agresif (Cyclical) | Rp 20.000.000.000 | 3.50% |
| `NPC-13` | Rian Firmansyah | Ventura Teknologi Nusantara | Agresif (High Tech) | Rp 15.000.000.000 | 4.10% |
| `NPC-14` | Cindy Prameswari | Pelopor Resor Wisata Bahari | Konservatif (Asset Play) | Rp 10.000.000.000 | 1.70% |
| `NPC-15` | Johannes Wibowo | Pengelola Layanan Kesehatan | Seimbang (Defensive) | Rp 8.000.000.000 | 2.20% |
| `NPC-16` | Dimas Aryasuta | Spekulan Momentum Cepat | Agresif (Breakout) | Rp 5.000.000.000 | 3.05% |
| `NPC-17` | Tommy Subagyo | Pemodal Tambang Transisi | Seimbang (Energy) | Rp 3.000.000.000 | 2.15% |
| `NPC-18` | Joko Wicaksono | Produsen Pangan Tradisional | Konservatif (Consumer) | Rp 1.000.000.000 | 1.65% |
| `NPC-19` | Aditya Wardhana | Trader Cepat Scalping | Agresif (Volatility) | Rp 500.000.000 | 4.80% |
| `NPC-RIV-01` | Hendra Sukanto | Trader Ambisius Rival | Agresif (Direct Rival) | Rp 45.000.000 | 2.75% |

#### League Mechanics
- **Tiers**: Bronze (Rank 15–20), Silver (Rank 8–14), Gold (Rank 1–7).
- **Weekly Rollover (Monday 00:00 WIB)**:
  - Ranked by 7-day Modified Dietz Return.
  - Promotions: Top 2 in Bronze $\rightarrow$ Silver; Top 2 in Silver $\rightarrow$ Gold.
  - Relegations: Bottom 2 in Gold $\rightarrow$ Silver; Bottom 2 in Silver $\rightarrow$ Bronze.
- **Rival Pairing**: Closest NPC in same tier paired at Monday rollover; persistent for the entire week.

#### Periodic Missions & Badges (D07, D08)
- **Daily Missions (3 active)**: Selected from 6 templates (News reader, Portfolio review, Execute limit order, Check enterprise operations, Run stress test, Achieve target daily return).
- **Weekly Missions (3 active)**: Selected from 6 templates (Achieve weekly ROI, Upgrade company/staff, Rebalance portfolio, 5 fills executed, Zero loan arrears, Maintain $\ge 80\%$ morale).
- **Career Badges (18 definitions)**: Spanning trading, learning, risk management, and empire building. Badges award prestige XP only (no cash subsidies).

---

### 5.4 Luxury Collections Catalog (12 Canonical Items) (D10 & Finding 5)

Table 5.4 reflects the canonical 12 items already defined in `CollectionManager.java`, providing early-to-endgame progression starting at Rp 15,000,000:

| ID | Name | Category | Base Valuation | Daily Upkeep | Rep Bonus | Rarity |
|---|---|---|---|---|---|---|
| `COL-CAR-00` | Vespa Klasik 1965 Restorasi | Kendaraan | Rp 25.000.000 | Rp 20.000 | +5 | Standar |
| `COL-CAR-01` | Porsche 911 GT3 RS | Kendaraan | Rp 4.500.000.000 | Rp 500.000 | +15 | Langka |
| `COL-CAR-02` | Ferrari 296 GTB | Kendaraan | Rp 8.000.000.000 | Rp 1.000.000 | +25 | Legenda |
| `COL-PROP-00` | Ruko Komersial BSD City | Properti | Rp 45.000.000 | Rp 30.000 | +8 | Standar |
| `COL-PROP-01` | Penthouse SCBD Mega Kuningan | Properti | Rp 15.000.000.000 | Rp 1.500.000 | +30 | Langka |
| `COL-PROP-02` | Villa Tebing Uluwatu Bali | Properti | Rp 25.000.000.000 | Rp 2.500.000 | +50 | Legenda |
| `COL-JEW-01` | Cincin Safir Biru Ceylon | Permata | Rp 35.000.000 | Rp 15.000 | +6 | Standar |
| `COL-JEW-02` | Kalung Berlian Solitaire Tiffany | Permata | Rp 2.500.000.000 | Rp 100.000 | +18 | Langka |
| `COL-JEW-03` | Tiara Berlian Kerajaan Langka | Permata | Rp 18.000.000.000 | Rp 800.000 | +40 | Legenda |
| `COL-ART-00` | Rolex Submariner Vintage 1970 | Seni & Jam | Rp 15.000.000 | Rp 10.000 | +4 | Standar |
| `COL-ART-01` | Patek Philippe Grandmaster Chime | Seni & Jam | Rp 5.000.000.000 | Rp 200.000 | +20 | Langka |
| `COL-ART-02` | Lukisan Bersejarah Raden Saleh | Seni & Jam | Rp 12.000.000.000 | Rp 500.000 | +35 | Legenda |

#### Collateral Pledging Mechanics
- Items have a `boolean pledged` flag.
- Pledging an item locks it as collateral for a personal installment loan (`LOAN-PERS-01`).
- Pledged items cannot be sold. Attempting to sell returns error `ITEM_PLEDGED_AS_COLLATERAL`.
- Repaying or prepaying the loan clears the `pledged` flag, restoring full liquidity.

---

### 5.5 Persistence & Save System Architecture

#### Atomic Write Pattern
1. Serialize complete game state to `JSONObject`.
2. Compute SHA-256 integrity checksum.
3. Write payload to temporary file: `save_game_v2.json.tmp`.
4. Flush file descriptor to disk: `fos.getFD().sync()`.
5. Rotate existing file to `save_game_v2.json.bak`.
6. Atomic rename: `save_game_v2.json.tmp` $\rightarrow$ `save_game_v2.json`.

#### 5.5.1 Save Directory Isolation & Personal Save Safeguards (Finding 1)
To ensure the player's personal save file at `Artifacts/LocalPreview/preview-save/save_game_v2.json` is never endangered:
1. **Automated Test Isolation**: All unit tests, integration suites, and benchmarks MUST instantiate `SaveManager.forDirectory(File baseDir)` with a transient temporary directory (e.g., `new File(System.getProperty("java.io.tmpdir"), "test_save_" + System.nanoTime())`) or a dedicated QA path (`Artifacts/QA-Save/`).
2. **Absolute Prohibition**: No automated test, benchmark runner, or build script may target or write to `Artifacts/LocalPreview/preview-save/`.
3. **Pre-Run Backup Routine**: Before any local preview server or manual QA session begins, an automated pre-flight hook creates a timestamped copy of the personal save:
   `Artifacts/LocalPreview/preview-save/save_game_v2.json` $\rightarrow$ `Artifacts/LocalPreview/backup/preview-save-backup-<timestamp>/save_game_v2.json`.

---

### 5.6 Operational Company Sector Model & Realistic Economics (D11 & Finding 6)

`OperatingCompany.java` is refactored from fixed Rp 25M revenue to a sector-differentiated economic model:

#### Sector Differentiation Matrix
| Sector | Description | Base Capacity (Daily/Lvl) | Fixed Overhead (Daily/Lvl) | Staff Morale Sensitivity | Market Demand Factor |
|---|---|---|---|---|---|
| **Jasa / Konsultan** | Layanan riset, analisis, dan konsultasi keuangan | Rp 8.000.000 | Rp 1.000.000 | Sangat Tinggi ($\pm 40\%$) | Stabil ($0.95 - 1.05$) |
| **Ritel** | Perdagangan barang konsumsi dan ritel modern | Rp 20.000.000 | Rp 5.000.000 | Sedang ($\pm 20\%$) | Siklikal ($0.80 - 1.25$) |
| **Manufaktur** | Pabrik dan lini perakitan komponen | Rp 50.000.000 | Rp 15.000.000 | Rendah ($\pm 10\%$) | Skala Ekonomi ($0.70 - 1.40$) |

#### Mathematical Formulas
$$\text{ProductivityMultiplier} = 1.0 + \sum_{e \in \text{Staff}} (e.\text{productivityMultiplier} - 1.0) + \text{PolicyHoursFactor}$$
- Policy Hours Factor: 10 hours = $+0.25$, 8 hours = $0.0$, 6 hours = $-0.15$.
- Morale Factor: $\text{Morale} / 100.0$ (scaled by sector sensitivity).
- Plant Operational Efficiency: In Manufacturing, if staff count $< 3$, capacity is penalized by $50\%$.

$$\text{Daily Revenue} = \text{BaseCapacity} \times \text{Level} \times \text{ProductivityMultiplier} \times \text{MoraleFactor} \times \text{MarketDemandFactor}$$
$$\text{Daily Operating Expenses} = (\text{FixedOverhead} \times \text{Level}) + \text{DailyPayroll}$$

#### Payroll Arrears & Morale Penalties
If company cash $< \text{DailyPayroll}$:
1. Available cash is partially disbursed, and the remainder is recorded as `payrollArrears`.
2. Company morale drops by 20 points immediately.
3. Operational capacity is slashed by $50\%$ due to employee disengagement.
4. Capital injections from the player automatically prioritize clearing `payrollArrears` before restoring normal operational capacity.

---

### 5.7 Extended Design Decisions (D01, D03, D04, D12, D14, D15, D16 & Corporate Actions)

#### D01: Deterministic Intraday Event Ordering at Same Timestamp
When multiple events occur at the same timestamp, execution follows this strict sequence:
1. **Market Mutations & Due Events**: Session open/close transitions, orderbook matches, call auctions, trading halts.
2. **Accruals & Obligations**: Loan daily interest, company operating revenue/expenses, company payroll, mutual fund daily management fee, collection maintenance.
3. **End-of-Period Snapshot**: 16:00 WIB NAV cutoff calculation, midnight day close snapshot, Modified Dietz return calculation.
4. **Mission / Stat / League Evaluation**: Mission progress checks, league promotion/relegation (Mondays), rival performance comparison.
5. **Next Period Open**: Advance calendar date, rotate daily/weekly missions, open next market session.

#### D03: Asset Allocation Denominator
- Denominator is **Gross Total Assets**:  
  $$\text{Gross Assets} = \text{Cash} + \text{Stock Portfolio} + \text{Mutual Fund Value} + \text{Company Net Equity} + \text{Collection Valuation}$$
- Total Liabilities (Margin Debt + Outstanding Installment Loans) are displayed as a separate debt card/slice. Debt is never rendered as a negative slice in the asset donut.
- If Gross Assets $\le 0$, the breakdown displays an explanatory alert rather than invalid division-by-zero percentages.

#### D04: Portfolio Health & Risk Categories
Categorical health assessment evaluated against three pillars:
1. **Diversification**: Maximum single-instrument exposure.
   - Healthy: $<25\%$ of gross portfolio.
   - Moderate: $25\% - 50\%$.
   - Concentrated: $>50\%$ (triggers alert).
2. **Liquidity Reserve**: Cash vs. 30-day fixed obligations (loan payments + payroll + collection upkeep).
   - Healthy: $\ge 30$ days of obligations in available cash.
   - Alert: $10 - 30$ days.
   - Critical: $< 10$ days.
3. **Leverage & Debt Service**:
   - Safe: Total debt $< 30\%$ of gross assets, margin ratio $< 1.0\times$.
   - Warning: Debt $30\% - 60\%$, margin ratio $1.0\times - 1.5\times$.
   - Danger: Debt $> 60\%$ or margin ratio $> 1.5\times$ (margin call risk).

#### D12: Reference Uncertain Status for Bank Deposits
- Legacy APK string literals mention `Bank deposits (removed)`.
- In accordance with SPECIFICATION.md Section 14, bank deposits are classified as `REFERENCE_UNCERTAIN`. No synthetic bank deposit product is fabricated. Liquidity is provided through Cash and Money Market Mutual Funds (`FUND-MM-01`).

#### D14: Optional Prestige Retention Map
Prestige is strictly optional and unlocks at CIO or Portfolio Manager career ranks after $\ge 3$ game weeks:
- **Resets**: Personal cash (reverts to Rp 50,000,000), stock holdings, active orders, operating companies, company accounts, personal loans, corporate loans.
- **Persists**: Career rank, unlocked badges (18 items), lifetime trading statistics (all-time volume, best trade, win rate), prestige level counter, permanent prestige XP bonus (+5% per prestige level, capped at 10%).
- **Safety**: Generates an atomic backup (`save_game_prestige_<timestamp>.bak`) before committing the prestige reset.

#### D15: Investor Archetype Classifier
Evaluates trading behavior after $\ge 5$ completed fills and $\ge 7$ game days:
- **Agresif (Aggressive)**: $>60\%$ growth/tech exposure, leverage $>1.2\times$, average holding period $<3$ days.
- **Moderat / Seimbang (Balanced)**: Balanced equity/fund exposure, leverage $\le 1.0\times$, holding period 3–14 days.
- **Konservatif (Conservative)**: $>50\%$ cash/money market funds/blue chips, zero margin debt, holding period $>14$ days.

#### D16: Player Profile & Local Preferences Persistence
- Profile data: Player name, avatar monogram/icon, bio, and assigned archetype.
- Preferences: Locale (Indonesian default), font scaling factor (100% to 200%), high-contrast mode, reduced-motion.
- Stored in `SaveState` under `playerPreferences` and persisted across restarts.

#### Corporate Actions & IPO Lifecycle (REF:ipo, FR-13/14)
- **IPO Offering**: Displays prospectus, issue price, offering window.
- **Subscription**: Cash is reserved upon order placement.
- **Allotment**: If oversubscribed, shares are allocated pro-rata and unallotted cash is refunded.
- **Listing Debut**: Shares become tradable on secondary market at 09:00 WIB listing date.
- **Shareholder Voting (RUPS)**: Periodic corporate agendas (dividend distribution, mergers) allow voting with voting weight proportional to held shares.

---

## 6. Comprehensive Coverage of the 26 Feature Groups

| Ref Group | Feature Name | Primary Tab & Access Route | Java Domain Owner | Bridge APIs | Key UI States |
|---|---|---|---|---|---|
| `REF:shell` | Root 5-Tab Navigation & Header | Global Header & Bottom Nav | `TradingFacade` | `getSnapshot`, `getPlatformInfo` | Active tab, header chips, clock speed |
| `REF:wealth` | Consolidated Net Worth (D02) | Portofolio $\rightarrow$ Wealth Hero | `TradingFacade`, `Account` | `getConsolidatedWealth` | Default, asset breakdown, debt list |
| `REF:market` | Product Discovery & Categories | Pasar $\rightarrow$ Category Pills | `MarketCatalog` | `getInstruments`, `getIndices` | Loading, category selected, empty search |
| `REF:chart` | Chart, Metrics & Order Tape | Pasar $\rightarrow$ Instrument Drawer | `MarketEngine`, `OrderManager` | `getChartData`, `getOrderBook` | Line/Candle, timeframes, empty history |
| `REF:trade` | Trade Order Flow | Drawer $\rightarrow$ Form Beli/Jual | `OrderManager` | `previewOrder`, `submitOrder` | Preview, stale quote, filled, rejected |
| `REF:allocation`| Gross Asset Allocation (D03) | Portofolio $\rightarrow$ Alokasi | `RiskComplianceService` | `getRiskSnapshot` | Donut/bars, gross asset table |
| `REF:health` | Wealth Health & Risk (D04) | Portofolio $\rightarrow$ Kesehatan | `RiskComplianceService` | `getRiskSnapshot` | Diversification, liquidity, debt alerts |
| `REF:loans` | Tenor Installment Loans (D05) | Portofolio $\rightarrow$ Utang & Pinjaman | `InstallmentLoanManager` | `getLoanProducts`, `disburseLoan`, `prepayLoan` | Schedule preview, active, arrears |
| `REF:ipo` | IPO Offerings & Allotment | Pasar $\rightarrow$ IPO | `CorporateActionService` | `getCorporateActions`, `participateCorporateAction` | Prospectus, subscribed, allotted |
| `REF:funds` | Unit/NAV Mutual Funds (D06) | Pasar $\rightarrow$ Reksa Dana | `MutualFundManager` | `getMutualFunds`, `subscribeMutualFund`, `redeemMutualFund`, `cancelFundOrder` | NAV chart, pending cutoff, holdings |
| `REF:deposits` | Bank Deposits Boundary (D12) | Pasar $\rightarrow$ Pasar Uang | `TradingFacade` | N/A (`REFERENCE_UNCERTAIN`) | Explanatory note, directs to Money Market |
| `REF:missions` | Periodic Missions (D07) | Beranda & Lainnya $\rightarrow$ Misi | `PeriodicMissionManager` | `getPeriodicMissions`, `claimPeriodicMission` | Daily/weekly tabs, progress, claimable |
| `REF:achievements`| Career Badges (18 Items) (D08)| Lainnya $\rightarrow$ Pencapaian | `CareerManager`, `ProgressManager` | `getPlayerProgress` | Unlocked, locked, requirements |
| `REF:ranks` | 3-Tier Competitive League (D09)| Lainnya $\rightarrow$ Kompetisi | `LeagueManager` | `getLeagueLeaderboard` | Bronze/Silver/Gold, promotion zone |
| `REF:rivals` | Persistent NPC Rival (D09) | Beranda & Lainnya $\rightarrow$ Rival | `LeagueManager` | `getRivalStatus` | Head-to-head comparison, weekly return |
| `REF:leaderboard`| Offline Leaderboard (20 NPCs) | Lainnya $\rightarrow$ Peringkat | `LeagueManager` | `getLeagueLeaderboard` | Net worth/return tabs, player highlighted |
| `REF:collection`| Luxury Collections (12 Items) | Lainnya $\rightarrow$ Koleksi | `CollectionManager` | `getCollections`, `buyCollection`, `sellCollection` | Gallery, valuation, pledged badge |
| `REF:empire` | Operational Companies (D11) | Bisnis $\rightarrow$ Perusahaan | `CompanyManager` | `getCompanies`, `foundCompany`, `setCompanyPolicy` | Sector choice, operations overview |
| `REF:employees`| Company Staff Management (D11)| Bisnis $\rightarrow$ Perusahaan $\rightarrow$ Pegawai | `CompanyManager` | `getCompanyStaff`, `hireCompanyStaff`, `fireCompanyStaff`, `trainCompanyStaff` | Candidate pool, roster, severance pay |
| `REF:morale` | Workplace Policies (D11) | Bisnis $\rightarrow$ Kebijakan | `CompanyManager` | `setCompanyPolicy` | Hours (6/8/10), benefits tier |
| `REF:empire-invest`| Company Capital & Dividends | Bisnis $\rightarrow$ Kas & Modal | `CompanyManager` | `transferCompanyCapital` | Inflow/outflow, cash reserve |
| `REF:prestige` | Optional Career Prestige (D14) | Lainnya $\rightarrow$ Prestige | `TradingFacade` | `getPrestigeStatus`, `executePrestigeReset` | Eligibility check, backup confirmation |
| `REF:archetype`| Investor Archetype (D15) | Lainnya $\rightarrow$ Profil $\rightarrow$ Gaya | `RiskComplianceService` | `getInvestorArchetype` | Aggressive/Balanced/Conservative |
| `REF:daily-report`| Daily Session Report (D13) | Beranda $\rightarrow$ Laporan | `TradingFacade` | `getDailyReport` | PnL vs cashflow, Modified Dietz |
| `REF:news` | Terminal Riset & Kalender | Lainnya $\rightarrow$ Riset | `ResearchService` | `recordResearchRead` | News articles, economic calendar |
| `REF:profile` | Profile, Stats & Preferences (D16)| Lainnya $\rightarrow$ Profil | `PlayerProfileManager` | `getPlayerProfile`, `updatePlayerProfile` | Avatar monogram, 200% font toggle |

---

## 7. Concrete File Modification Plan & API Contracts

### 7.1 Backend Java Classes to Update / Create
1. `com/tradingglobal3d/core/facade/TradingFacade.java`:
   - Update `advanceWorldSeconds()` with `secondsToCutoff` slicing and interval crossing checks.
   - Implement `cancelFundOrder(String orderId)`.
   - Implement staff management facade methods: `getCompanyStaff(String companyId)`, `hireCompanyStaff(String companyId, String candidateId, String name, String roleStr)`, `fireCompanyStaff(String companyId, String employeeId)`, and `trainCompanyStaff(String companyId, String employeeId)`.
2. `com/tradingglobal3d/core/business/CompanyManager.java`:
   - Add staff management methods:
     ```java
     public synchronized JSONObject getStaffRoster(String companyId);
     public synchronized Employee hireEmployee(String companyId, String candidateId, String name, Employee.Role role, Account companyAccount, EconomyLedger ledger, long now);
     public synchronized void fireEmployee(String companyId, String employeeId, Account companyAccount, EconomyLedger ledger, long now);
     public synchronized void trainEmployee(String companyId, String employeeId, Account companyAccount, EconomyLedger ledger, long now);
     public synchronized List<JSONObject> getAvailableCandidates(String companyId, long now);
     ```
   - Update sector economics, fixed overhead, and payroll arrears handling.
3. `com/tradingglobal3d/core/funds/MutualFundManager.java` & `MutualFund.java`:
   - Refactor `submitSubscription` and `submitRedemption` into dealing queue mechanics.
   - Add `cancelPendingOrder(String orderId, Account account, EconomyLedger ledger)`.
   - Enforce 15:30 WIB cancellation deadline.
4. `com/tradingglobal3d/core/league/LeagueManager.java`:
   - Replace living Indonesian billionaires with the 20 fictional competitor roster.
5. `com/tradingglobal3d/core/collection/CollectionManager.java`:
   - Add `pledged` flag and verify against canonical 12 items.

### 7.2 Web Bridge (`TradingWebBridge.java`) API Contracts
```java
@JavascriptInterface
public String getCompanyStaff(String companyId) { ... }

@JavascriptInterface
public String hireCompanyStaff(String jsonRequest) {
    // Schema: {"companyId":"COMP-01", "candidateId":"CAND-01", "name":"Budi", "role":"HEAD_OF_RESEARCH"}
}

@JavascriptInterface
public String fireCompanyStaff(String jsonRequest) {
    // Schema: {"companyId":"COMP-01", "employeeId":"EMP-01"}
}

@JavascriptInterface
public String trainCompanyStaff(String jsonRequest) {
    // Schema: {"companyId":"COMP-01", "employeeId":"EMP-01"}
}

@JavascriptInterface
public String cancelFundOrder(String orderId) {
    // Cancels pending fund order before 15:30 WIB
}
```

### 7.3 Frontend Web Files to Update
1. `android/app/src/main/assets/www/app.js`:
   - Implement `openCompanyStaffModal(company)`: displays current employee roster, daily salary, productivity contribution, and available candidates for hire.
   - Wire event handlers to `hireCompanyStaff`, `fireCompanyStaff`, and `trainCompanyStaff`.
   - Wire `cancelFundOrder` button in pending mutual fund orders drawer.
2. `android/app/src/main/assets/www/index.html`:
   - Ensure semantic modal containers and accessible buttons for staff management.
3. `android/app/src/main/assets/www/style.css`:
   - Enforce 48dp touch targets and responsive styling for staff roster cards under 200% font scale.

---

## 8. Invariant Enforcement Matrix

| Invariant | Responsible Layer | Enforcement Mechanism | Failure Behavior |
|---|---|---|---|
| Zero money creation from transfers | `TradingFacade` & `EconomyLedger` | Double-entry bookkeeping: debit personal equals credit company equity | Reject transfer with `INSUFFICIENT_FUNDS` or transaction rollback |
| 1x300s vs 300x1s equivalence | `TradingFacade.advanceWorldSeconds` | Discrete time chunk slicing ($\le 60\text{s}$) with exact cutoff boundary crossing | Simulation halts and logs `STEP_DESYNC` |
| Idempotent order & loan execution | `TradingWebBridge` & `OrderManager` | Unique `requestId` tracking in memory and save state | Return cached receipt without re-executing mutations |
| Mutual fund unit conservation | `MutualFundManager` | Outstanding units = sum of all account unit balances | Invariant test fails; reject transaction |
| Loan collateral locking | `InstallmentLoanManager` & `CollectionManager` | Collateral registry flags items as `pledged=true` | Pledged items cannot be sold or re-pledged |
| Single source of truth for time | `GameClock` | UI queries clock state; no client-side `setInterval` time math | Desynchronized UI snaps back to domain time |
| Clean session state | `TradingFacade.initializeNewGame` | Explicit cascading `.reset()` on all domain managers | Residual objects trigger audit probe assertion failure |
| Personal save protection | `SaveManager` & QA Test Harness | Test base directories restricted to temp folders | Tests targeting preview-save fail immediately |

---

## 9. Error Handling, Logging & Input Validation Matrix

### Input Validation & Error Code Matrix (Finding 10)
| Operation | Input Parameters & Validation Rules | Error Code | Behavior on Failure |
|---|---|---|---|
| **Loan Disbursement** | `principal` $\ge 1,000,000$ IDR<br>`tenorDays` $\in \{7, 14, 30\}$<br>Eligible collateral/revenue | `BELOW_MINIMUM_PRINCIPAL`<br>`INVALID_TENOR`<br>`INSUFFICIENT_COLLATERAL` | Reject mutation, return error JSON, display banner |
| **Loan Prepayment** | `loanId` exists & status `ACTIVE` | `LOAN_ALREADY_CLOSED` | Reject mutation, refresh loan list |
| **Fund Subscribe** | `grossAmount` $\ge 10,000$ IDR<br>Available cash $\ge$ gross | `BELOW_MINIMUM_SUBSCRIPTION`<br>`INSUFFICIENT_FUNDS` | Reject mutation, show required balance |
| **Fund Redeem** | `units` $> 0$<br>Held units $\ge$ units | `INVALID_UNIT_QUANTITY`<br>`INSUFFICIENT_UNITS` | Reject mutation, display max redeemable units |
| **Fund Cancel** | Order exists, status `ACCEPTED`<br>Clock time $< 15:30$ WIB | `ORDER_NOT_FOUND`<br>`ORDER_ALREADY_SETTLED`<br>`CANCELLATION_DEADLINE_PASSED` | Reject cancellation, keep order in queue |
| **Company Founding** | `capital` $\ge 25,000,000$ IDR<br>`sector` $\in \{\text{"Jasa"}, \text{"Ritel"}, \text{"Manufaktur"}\}$<br>Personal cash $\ge$ capital | `INSUFFICIENT_CAPITAL`<br>`INVALID_SECTOR`<br>`INSUFFICIENT_FUNDS` | Form highlights invalid field |
| **Staff Hire** | `companyId` exists<br>`candidateId` valid<br>Company cash $\ge$ signing bonus | `COMPANY_NOT_FOUND`<br>`CANDIDATE_NOT_FOUND`<br>`INSUFFICIENT_COMPANY_CASH` | Reject hire, alert required company cash |
| **Staff Fire** | `employeeId` exists<br>Role $\ne$ CEO<br>Company cash $\ge$ severance pay | `EMPLOYEE_NOT_FOUND`<br>`CANNOT_FIRE_CEO`<br>`INSUFFICIENT_COMPANY_CASH` | Reject firing, explain severance obligation |
| **Staff Train** | `employeeId` exists<br>Level $< 5$<br>Company cash $\ge$ training cost | `EMPLOYEE_NOT_FOUND`<br>`MAX_LEVEL_REACHED`<br>`INSUFFICIENT_COMPANY_CASH` | Disable upgrade button, display cost |
| **Company Policy** | `workHours` $\in \{6, 8, 10\}$<br>`benefitTier` $\in \{\text{STANDARD, PREMIUM, EXEC}\}$ | `INVALID_WORK_HOURS`<br>`INVALID_BENEFIT_TIER` | Retain prior policy settings |
| **Capital Transfer** | `amount` $> 0$ IDR<br>Source account cash $\ge$ amount | `INVALID_AMOUNT`<br>`INSUFFICIENT_FUNDS` | Reject transfer, show available balance |
| **Collection Buy** | Item not owned<br>Personal cash $\ge$ price | `ITEM_ALREADY_OWNED`<br>`INSUFFICIENT_FUNDS` | Reject purchase |
| **Collection Sell** | Item owned<br>`pledged == false` | `ITEM_NOT_OWNED`<br>`ITEM_PLEDGED_AS_COLLATERAL` | Reject sale, prompt loan prepayment |

---

## 10. Testing & Quality Assurance Plan

### 10.1 Unit & JVM Test Suites
- `TimeDualRateLifecycleTest.java`: Verify active $288\times$ and background $24\times$ clock speeds.
- `TimeAdvancementEquivalenceTest.java`: Assert exact equality of books, resting orders, and fills between $1\times 300,000\text{ ms}$ and $300\times 1,000\text{ ms}$, including 16:00 cutoff crossing.
- `CompanyCleanResetTest.java`: Prove zero orphaned companies or accounts after `initializeNewGame()`.
- `OperatingCompanyEconomicsTest.java`: Verify sector differentiation, overhead, demand multipliers, and payroll arrears.
- `MutualFundQueueTest.java`: Test subscription queue, 15:30 cancellation deadline, 16:00 cutoff settlement, and fee accounting.
- `InstallmentLoanAmortizationTest.java`: Test 7/14/30-day corporate and personal loan schedules, daily interest, prepayment, and penalty caps.

### 10.2 Viewport & Accessibility Matrix
Automated test script (`verify_viewports.js`) running headless Chromium across all 7 target viewports $\times$ 2 font scales (100% and 200%):
- Viewports: `320x568`, `360x640`, `390x844`, `412x915`, `430x932`, `768x1024`, `1440x900`.
- Verifies zero horizontal overflow (`scrollWidth <= clientWidth`).
- Verifies touch target dimensions ($\ge 48\text{px}$).

### 10.3 Performance Benchmark Harness
Automated benchmark harness (`benchmark_performance.js`) recording $\ge 30$ samples per action:
- Local action latency: P95 $\le 300\text{ ms}$.
- Tab navigation latency: P95 $\le 1,000\text{ ms}$.
- Skip Day heavy execution: Feedback rendered $\le 300\text{ ms}$.

---

# PART III: RESPONSES TO INDEPENDENT DESIGN REVIEW FINDINGS

| Finding ID | Severity | Review Title | Disposition | Specific Response & Remediation in `design.md` v1.1.0 |
|---|---|---|---|---|
| **Finding 1** | **HIGH** | Personal Save Protection Completely Omitted from Design | **ADDRESSED** | Added Section 5.5.1 explicitly mandating that automated tests and benchmarks use temporary directories, prohibiting any writes to `Artifacts/LocalPreview/preview-save/`, and establishing pre-run backup procedures. |
| **Finding 2** | **HIGH** | Discrete Time Advancement Slicing Algorithm Misses 16:00 Cutoff | **ADDRESSED** | Updated Section 2 with an exact mathematical step formula including `secondsToCutoff` and interval boundary crossing evaluation (`currentSecond < cutoffSecond && nextSecond >= cutoffSecond`) for mutual fund cutoffs. |
| **Finding 3** | **HIGH** | Missing Bridge and Facade API Specifications for Company Staff / Employee Management (D11) | **ADDRESSED** | Added explicit method signatures in Section 6.1 and 6.2 for `CompanyManager`, `TradingFacade`, and `TradingWebBridge` covering `getCompanyStaff`, `hireCompanyStaff`, `fireCompanyStaff`, and `trainCompanyStaff`. |
| **Finding 4** | **HIGH** | Inconsistency Between SPECIFICATION.md D05, Existing Code, and design.md for Corporate Loan Tenors and Interest Rates | **ADDRESSED** | Corrected Table 5.2 in Section 5.2 to conform with D05 and `InstallmentLoanManager.java`: 7, 14, 30 days tenor, 0.05%/day daily rate, and 0.50% origination fee for both personal and corporate loans. |
| **Finding 5** | **HIGH** | Inconsistency and Accessibility Breakdown in Luxury Collection Catalog (D10) | **ADDRESSED** | Replaced Table 5.4 with the canonical 12 items implemented in `CollectionManager.java` (starting at Rp 15M for early-game accessibility) and specified the `pledged` collateral flag and lock mechanics in Section 5.4. |
| **Finding 6** | **HIGH** | Omission of Operational Company Realistic Revenue/Cost Formulas (D11) | **ADDRESSED** | Added Section 5.6 defining three differentiated sectors (Jasa, Ritel, Manufaktur), mathematical daily revenue and expense formulas, demand factors, and payroll arrears handling with morale and capacity penalties. |
| **Finding 7** | **MEDIUM**| Unspecified Queue and Dealing Settlement Mechanics for Mutual Funds (D06) | **ADDRESSED** | Expanded Section 5.1 detailing `PendingFundOrder` queue persistence, cash/unit reservation, the 15:30 WIB cancellation deadline, and the `cancelFundOrder` bridge endpoint. |
| **Finding 8** | **HIGH** | Incomplete Coverage of 26 Feature Groups and D01–D16 Design Decisions | **ADDRESSED** | Added Section 5.7 covering D01 (event order), D03 (gross asset denominator), D04 (portfolio health categories), D12 (bank deposits reference uncertain), D14 (prestige retention map), D16 (local preferences), and Corporate Actions/IPO lifecycles. Added Section 6 with complete 26-feature-group mapping. |
| **Finding 9** | **MEDIUM**| NPC Names in League Leaderboard Violate SPECIFICATION.md (D09) | **ADDRESSED** | Replaced living figures with a roster of 20 fully fictional Indonesian competitor names and archetypes in Table 5.3, and added a task in Section 6.1 to update `LeagueManager.java`. |
| **Finding 10**| **MEDIUM**| Missing Input Validation Contracts and Error Codes for Bridge Mutations | **ADDRESSED** | Added an Input Validation & Error Code Matrix in Section 9 detailing parameter bounds, preconditions, error codes, and failure behavior across all bridge mutations. |
| **Finding 11**| **NIT** | Discrepancy in F3 and F5 Problem Statements vs Existing Repository State | **ADDRESSED** | Updated Section 4 problem descriptions to reflect actual repository state: refocused F3 on `CompanyCleanResetTest.java` regression verification, and refocused F5 on implementing the missing `openCompanyStaffModal()` in `app.js`. |

---

*This document serves as the authoritative, binding technical design blueprint for Game Saham 2D Portrait feature completion.*
