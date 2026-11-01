# PLAN EKSEKUSI LENGKAP: SINTESA 5 FITUR MASTERPIECE
## Integrasi Formula Ekonomi Biner ke dalam Game Saham 2D Portrait Android
### Target Proyek: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`
- **Tanggal Dokumen:** 31 Oktober 2026
- **Status Dokumen:** Master Plan Eksekusi Siap Jalankan (Unbundled & Detail)
- **Arsitektur Produksi:** Android Native Java (API 36, Java 21) + WebView UI 2D Portrait
- **Prinsip Utama:** Kolaborasi Sintesa — **TIDAK MENIMPA** fondasi `OrderBook`, `MatchingEngine`, `OperatingCompany`, dan `CorporateActionService` yang sudah ada, melainkan memberikan mesin penggerak nilai wajar, perebutan kue pasar riil, dan siklus makro terpadu.

---

## DAFTAR ISI MASTER PLAN
1. [Ringkasan Arsitektur & Filosofi Sintesa](#1-ringkasan-arsitektur--filosofi-sintesa)
2. [Fase 0: Pre-Flight, Lingkungan Toolchain & Proteksi Baseline](#fase-0-pre-flight-lingkungan-toolchain--proteksi-baseline)
3. [Fase 1: Implementasi Modul 1 — Nilai Intrinsik & Gravitasi Order Book](#fase-1-implementasi-modul-1--nilai-intrinsik--gravitasi-order-book)
4. [Fase 2: Implementasi Modul 2 — Radar Diskon Saham & Sinyal Analis Fundamental](#fase-2-implementasi-modul-2--radar-diskon-saham--sinyal-analis-fundamental)
5. [Fase 3: Implementasi Modul 3 — Sinergi Kue Sektor Zero-Sum Perusahaan Operasional](#fase-3-implementasi-modul-3--sinergi-kue-sektor-zero-sum-perusahaan-operasional)
6. [Fase 4: Implementasi Modul 4 — Efek Kelangkaan Saham / Supply Squeeze](#fase-4-implementasi-modul-4--efek-kelangkaan-saham--supply-squeeze)
7. [Fase 5: Implementasi Modul 5 — Jantung Siklus Makroekonomi Terpadu (Level 1..9)](#fase-5-implementasi-modul-5--jantung-siklus-makroekonomi-terpadu-level-19)
8. [Fase 6: Integrasi Antarmuka UI WebView 2D Portrait & Native Bridge](#fase-6-integrasi-antarmuka-ui-webview-2d-portrait--native-bridge)
9. [Fase 7: Uji Regresi Menyeluruh, Verifikasi Memori & Build APK Android](#fase-7-uji-regresi-menyeluruh-verifikasi-memori--build-apk-android)
10. [Matriks Checklist Verifikasi & Kriteria Penerimaan](#10-matriks-checklist-verifikasi--kriteria-penerimaan)

---

## 1. Ringkasan Arsitektur & Filosofi Sintesa

Di game **Capitalism (`com.SyGame.Capitalism`)**, harga saham digerakkan oleh rumus internal berbasis *Virtual Stock Value* tanpa adanya antrean buku pesanan (*Order Book*). Sebaliknya, di **Game Saham 2D Portrait** milik Anda, Anda telah memiliki sistem bursa saham modern berbasis **Order Book Bid/Ask**, **Matching Engine**, dan antrean lot nyata.

Sintesa ini mengawinkan keunggulan kedua game dengan arsitektur berikut:

```
┌────────────────────────────────────────────────────────────────────────┐
│             ARSITEKTUR SINTESA 5 MASTERPIECE PADA GAME SAHAM           │
├────────────────────────────────────────────────────────────────────────┤
│                                                                        │
│   [ SIKLUS MAKROEKONOMI TERPADU ] (MacroCycleEngine Level 1..9)        │
│          │                               │                             │
│          ▼                               ▼                             │
│   [ KUE SEKTOR ZERO-SUM ]         [ BANK & PERBANKAN ]                 │
│   (SectorPie Rp 15-20 Triliun)     - Bunga Deposito (4.0%..9.5%)       │
│          │                         - Bunga Pinjaman (+4.0% spread)     │
│     ┌────┴─────────────────┐       - Minat & Listing Gain 36 IPO       │
│     ▼                      ▼                                           │
│  [ EMITEN BURSA ]     [ OPERATING COMPANY ]                            │
│  - Revaluasi EPS      - Moral Staf & Fasilitas                         │
│  - Nilai Intrinsik    - R&D & Kapasitas Produksi                       │
│  - P/E Sektor         - Rebut Pangsa Pasar Emiten                      │
│     │                                                                  │
│     ▼                                                                  │
│  [ GRAVITASI NILAI INTRINSIK ]                                         │
│  - Rumus VirCal: Fair = EPS * PER                                      │
│  - Radar Analis: GapRate = (Fair - Price) / Price                      │
│     │                                                                  │
│     ▼                                                                  │
│  [ NALURI BOT NPC TAKER ] ────► [ ORDER BOOK & MATCHING ENGINE ]       │
│  - 3 Rezim Peluang Beli:         - Antrean Bid & Ask Nyata             │
│    * Overvalued: 40% Beli        - Eksekusi Price-Time Priority        │
│    * Undervalued: 63% Beli       - Efek Supply Squeeze (Ask Melebar)   │
│    * Deeply Undervalued: 68%     - Pembatasan ARA / ARB Aman           │
│      (dengan 2.0x volume lot)                                          │
└────────────────────────────────────────────────────────────────────────┘
```

### Keputusan Pacing & Ritme Waktu Permainan (Hasil Analisis Komparatif)
1. **Mempertahankan Rasio Waktu 24× (`GameClock.java`):**
   - **Alasan:** Berbeda dengan Capitalism yang tidak memiliki Order Book, game Anda membutuhkan waktu yang cukup bagi pemain untuk menganalisis antrean Bid/Ask dan memasukkan order limit. Rasio 24× (1 sesi IDX = ~17,5 menit nyata) adalah ritme paling pas untuk gameplay pro-trader mobile.
2. **Mempertahankan Kalender Bursa 365 Hari:**
   - Kalender asli dengan 52 minggu, libur akhir pekan, dan jam bursa IDX (09:00–16:00 WIB) & US (21:30–04:00 WIB) tetap dipertahankan utuh.
3. **Penambahan Fitur Tombol "Lompati Jeda Sesi" (*Session Fast-Forward*):**
   - Mengeliminasi momen mati (*dead-time*) saat istirahat siang IDX (11:31 – 13:29 WIB / ~5 menit nyata) dan jeda sore (16:01 – 21:29 WIB / ~13 menit nyata) melalui tombol cepat di samping jam: *"Lanjut ke Sesi II (13:30)"* dan *"Lanjut ke Pembukaan US (21:30)"*.

---

## FASE 0: PRE-FLIGHT, LINGKUNGAN TOOLCHAIN & PROTEKSI BASELINE

Sebelum satu baris kode produksi pun diubah, agen pelaksana wajib memvalidasi toolchain lokal dan membackup status kerja:

### Task 0.1: Verifikasi Environment Java 21 & Android SDK
- **Deskripsi:** Memastikan compiler dan runner menggunakan runtime Java 21 Android Studio yang terpasang.
- **Lokasi Eksekusi:** Terminal Windows MSYS/Bash.
- **Perintah Verifikasi:**
  ```bash
  export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
  export PATH="$JAVA_HOME/bin:$PATH"
  java -version
  javac -version
  ```
- **Kriteria Lolos:** Output menampilkan `openjdk version "21.0.10"`.

### Task 0.2: Pencatatan Status Git & Isolasi Modifikasi
- **Deskripsi:** Memastikan repositori kerja berada di direktori aktif `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM` dan mencatat status diff awal pengguna agar tidak tertimpa secara tidak sengaja.
- **Perintah Verifikasi:**
  ```bash
  cd "C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM"
  git status --short
  ```
- **Kriteria Lolos:** Tidak ada stash atau checkout yang dipaksa; perubahan lokal pengguna tetap terjaga.

### Task 0.3: Verifikasi Suite Pengujian Sintesa Mandiri
- **Deskripsi:** Menjalankan script uji sintesa yang sudah dibuat (`run_synthesis_tests.py`) sebagai baseline bahwa 13 unit test simulasi berstatus `OK (13 tests)`.
- **Perintah Verifikasi:**
  ```bash
  python3 "C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/run_synthesis_tests.py"
  ```
- **Kriteria Lolos:** Exit code `0` dan laporan `OK (13 tests)`.

---

## FASE 1: IMPLEMENTASI MODUL 1 — NILAI INTRINSIK & GRAVITASI ORDER BOOK

Mengintegrasikan perhitungan harga wajar fundamental ke dalam `Instrument.java` dan menyuntikkan 3 rezim naluri bot pembeli ke `NpcAgent.java`.

### Task 1.1: Penambahan Atribut Fundamental pada `Instrument.java`
- **Berkas Target:** `android/app/src/main/java/com/tradingglobal3d/core/market/Instrument.java`
- **Langkah Spesifik:**
  1. Tambahkan field instance:
     ```java
     private BigDecimal intrinsicValue; // Nilai wajar / harga intrinsik saham
     private BigDecimal trailingEps;    // EPS 12 bulan terakhir
     private double sectorAvgPer;       // Rata-rata P/E ratio sektor
     ```
  2. Di konstruktor `Instrument(...)`, inisialisasi default:
     ```java
     this.trailingEps = initialPrice.getAmount().divide(new BigDecimal("15.0"), 2, RoundingMode.HALF_UP);
     this.sectorAvgPer = 15.0;
     this.intrinsicValue = initialPrice.getAmount();
     ```
  3. Tambahkan metode setter dan getter publik:
     - `public synchronized BigDecimal getIntrinsicValue()`
     - `public synchronized void setFundamentalMetrics(BigDecimal eps, double sectorPer)`
  4. Perbarui metode serialisasi save/restore: `exportMarketState()` dan `restoreMarketState()` agar `intrinsicValue` dan `trailingEps` tersimpan permanen di file save JSON.

### Task 1.2: Implementasi Rumus Nilai Intrinsik (*VirCal*) di `Instrument.java`
- **Berkas Target:** `android/app/src/main/java/com/tradingglobal3d/core/market/Instrument.java`
- **Formula Matematika Spesifik:**
  ```java
  public synchronized BigDecimal calculateIntrinsicValue() {
      if (trailingEps == null || trailingEps.signum() <= 0) {
          return currentPrice.getAmount();
      }
      // 1. Raw Fair Value = EPS * P/E Sektor
      BigDecimal rawFair = trailingEps.multiply(BigDecimal.valueOf(sectorAvgPer)).setScale(2, RoundingMode.HALF_UP);
      
      // 2. Koridor Clamping Asli Capitalism [0.60x .. 1.95x dari currentPrice]
      BigDecimal minBound = currentPrice.getAmount().multiply(new BigDecimal("0.60")).setScale(2, RoundingMode.HALF_UP);
      BigDecimal maxBound = currentPrice.getAmount().multiply(new BigDecimal("1.95")).setScale(2, RoundingMode.HALF_UP);
      BigDecimal clamped = rawFair.max(minBound).min(maxBound);
      
      // 3. Redaman Harga Tinggi (> 300.000 x0.80 | > 500.000 x0.70)
      if (clamped.compareTo(new BigDecimal("300000")) > 0) {
          clamped = clamped.multiply(new BigDecimal("0.80")).setScale(2, RoundingMode.HALF_UP);
      }
      if (clamped.compareTo(new BigDecimal("500000")) > 0) {
          clamped = clamped.multiply(new BigDecimal("0.70")).setScale(2, RoundingMode.HALF_UP);
      }
      
      // 4. Batas Bawah Harga Nominal Bursa
      BigDecimal floor = getDailyFloorPrice() != null ? getDailyFloorPrice().getAmount() : new BigDecimal("50.00");
      this.intrinsicValue = clamped.max(floor);
      return this.intrinsicValue;
  }
  ```

### Task 1.3: Modifikasi Naluri Bot Pembeli (*Taker Bias*) pada `NpcAgent.java`
- **Berkas Target:** `android/app/src/main/java/com/tradingglobal3d/core/npc/NpcAgent.java`
- **Lokasi Kode:** Metode `generateOrders(...)` pada blok `strategy == NpcStrategyType.RETAIL_NOISE` (baris 152–209).
- **Langkah Spesifik:**
  1. Ambil nilai `intrinsicValue = instrument.getIntrinsicValue()`. Jika null, fallback ke `instrument.getCurrentPrice().getAmount()`.
  2. Hitung probabilitas dasar beli (`buyProbability`) dan pengali lot (`lotMultiplier`):
     - **Jika $P \ge V$ (*Overvalued*):** `buyProbability = 0.40`, `lotMultiplier = 1.0`.
     - **Jika $P \times 1.5 < V$ (*Deeply Undervalued*):** `buyProbability = 0.68`, `lotMultiplier = 2.0`.
     - **Lainnya ($P < V \le 1.5P$ / *Undervalued*):** `buyProbability = 0.63`, `lotMultiplier = 1.0`.
  3. Terapkan sentimen berita `newsSentimentImpact`:
     - Jika berita positif ($> 0.1$): naikkan `buyProbability += 0.15` (maksimal 0.90).
     - Jika berita negatif ($< -0.1$): turunkan `buyProbability -= 0.25` (minimal 0.15).
  4. Tentukan arah order:
     ```java
     boolean isBuy = (rng.nextDouble() < buyProbability);
     ```
  5. Kalikan volume lot yang dibeli bot dengan `lotMultiplier` saat saham *deeply undervalued*, sehingga likuiditas beli di *Order Book* melimpah.

### Task 1.4: Unit Test & Verifikasi Fase 1
- **Berkas Uji:** `android/app/src/test/java/com/tradingglobal3d/core/synthesis/IntrinsicValueGravityAndRadarTest.java`
- **Pemeriksaan Runnable:** Pastikan 4 test case pada file uji ini lulus 100%.

---

## FASE 2: IMPLEMENTASI MODUL 2 — RADAR DISKON SAHAM & SINYAL ANALIS FUNDAMENTAL

Menyediakan sinyal analisis fundamental di tab Riset berdasarkan persentase diskon harga terhadap nilai intrinsik.

### Task 2.1: Implementasi Perhitungan GapRate pada `ResearchService.java`
- **Berkas Target:** `android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java`
- **Langkah Spesifik:**
  1. Buat metode kalkulasi diskon:
     ```java
     public double calculateGapRate(Instrument instrument) {
         BigDecimal fair = instrument.getIntrinsicValue();
         BigDecimal current = instrument.getCurrentPrice().getAmount();
         if (fair == null || current.signum() <= 0) return 0.0;
         return fair.subtract(current).divide(current, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
     }
     ```
  2. Buat metode penentuan sinyal konsensus analis:
     ```java
     public String getFundamentalSignal(double gapRate) {
         if (gapRate >= 15.0) return "STRONG_BUY"; // Diskon > 15%
         if (gapRate >= 5.0)  return "BUY";        // Diskon 5% - 15%
         if (gapRate >= -5.0) return "HOLD";       // Harga wajar (-5% s.d. +5%)
         return "SELL";                            // Kemahalan (Premium > 5%)
     }
     ```

### Task 2.2: Ekspos Data Fundamental ke Endpoint Konsensus JSON
- **Berkas Target:** `android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java`
- **Lokasi Kode:** Metode `getConsensus(Instrument instrument, long timestamp)` (sekitar baris 320–380).
- **Langkah Spesifik:**
  Tambahkan field berikut ke objek `JSONObject` konsensus:
  ```java
  obj.put("intrinsicValue", instrument.getIntrinsicValue().toPlainString());
  obj.put("gapRatePercent", String.format(Locale.US, "%.2f", calculateGapRate(instrument)));
  obj.put("fundamentalSignal", getFundamentalSignal(calculateGapRate(instrument)));
  obj.put("trailingEps", instrument.getTrailingEps().toPlainString());
  obj.put("sectorPer", instrument.getSectorAvgPer());
  ```

### Task 2.3: Unit Test & Verifikasi Fase 2
- **Kriteria Lolos:** Panggilan `researchQuery(kind="consensus")` melalui `TradingFacade` menyertakan key `intrinsicValue`, `gapRatePercent`, dan `fundamentalSignal` yang valid secara numerik.

---

## FASE 3: IMPLEMENTASI MODUL 3 — SINERGI KUE SEKTOR ZERO-SUM PERUSAHAAN OPERASIONAL

Menghubungkan tab Bisnis (`OperatingCompany`) dengan emiten di tab Pasar sehingga keduanya bersaing memperebutkan kue pendapatan riil.

### Task 3.1: Definisi Model Kue Sektor Bulanan
- **Berkas Target:** `android/app/src/main/java/com/tradingglobal3d/core/business/CompanyManager.java` & `TradingFacade.java`
- **Konfigurasi Ukuran Kue Dasar per Sektor:**
  - Sektor Energi: **Rp 25.000.000.000.000** (Rp 25 Triliun)
  - Sektor Manufaktur: **Rp 20.000.000.000.000** (Rp 20 Triliun)
  - Sektor Ritel / Konsumer: **Rp 15.000.000.000.000** (Rp 15 Triliun)
  - Sektor Jasa / Finansial: **Rp 30.000.000.000.000** (Rp 30 Triliun)

### Task 3.2: Perhitungan Skor Daya Saing Perusahaan Pemain pada `OperatingCompany.java`
- **Berkas Target:** `android/app/src/main/java/com/tradingglobal3d/core/business/OperatingCompany.java`
- **Formula Skor Daya Saing Perusahaan:**
  ```java
  public double getMarketCompetitivenessScore() {
      // Bobot Moral Pegawai (0..100) + Fasilitas Kantor + Jam Kerja
      double moraleWeight = this.morale / 100.0;
      double benefitBonus = this.benefitTier.getMoraleBonus() * 0.5;
      double productivity = (this.workHours == 10) ? 1.25 : (this.workHours == 6) ? 0.85 : 1.0;
      for (Employee e : employees.values()) {
          productivity += (e.getProductivityMultiplier() - 1.0);
      }
      double baseScore = (this.level * 20.0) + benefitBonus;
      return Math.max(5.0, baseScore * moraleWeight * productivity);
  }
  ```

### Task 3.3: Eksekusi Alokasi Pendapatan Zero-Sum di `CompanyManager.java`
- **Berkas Target:** `android/app/src/main/java/com/tradingglobal3d/core/business/CompanyManager.java`
- **Lokasi Kode:** Metode `processDailyOperations(...)` (baris 180–240).
- **Langkah Spesifik:**
  1. Kumpulkan seluruh emiten bursa yang berada di sektor yang sama dari `catalog.getInstrumentsBySector(company.getSector())`.
  2. Hitung total skor daya saing kompetitor emiten $\sum \text{Score}_{\text{emiten}}$ dan skor perusahaan pemain $\text{Score}_{\text{player}}$.
  3. Bagikan porsi kue harian:
     $$\text{DailyRevenue}_{\text{player}} = \frac{\text{SectorPie}_{\text{daily}} \times \text{Score}_{\text{player}}}{\sum \text{Score} + \text{Score}_{\text{player}}}$$
  4. Emiten kompetitor yang kehilangan pangsa pasar mengalami penurunan pendapatan harian secara proporsional.

### Task 3.4: Dampak Balik ke Laba & Nilai Intrinsik Emiten Bursa
- **Langkah Spesifik:**
  Setiap akhir bulan (tanggal 30/31), laba bersih emiten yang tersedot oleh perusahaan pemain dihitung ulang $\rightarrow$ `trailingEps` emiten turun $\rightarrow$ `calculateIntrinsicValue()` emiten tersebut ikut terkoreksi di `OrderBook`.

### Task 3.5: Unit Test & Verifikasi Fase 3
- **Berkas Uji:** `android/app/src/test/java/com/tradingglobal3d/core/synthesis/SectorPieAndSupplySqueezeTest.java`
- **Kriteria Lolos:** Tes `testPlayerResearchAndMoraleTakesMarketShareFromCompetitors` lulus dan total alokasi terbukti 100% *zero-sum*.

---

## FASE 4: IMPLEMENTASI MODUL 4 — EFEK KELANGKAAN SAHAM / SUPPLY SQUEEZE

Mewujudkan efek kelangkaan saham di mana harga melonjak tajam saat mayoritas lembar saham diborong oleh Pemain, Reksa Dana, dan Akun Mandat Klien.

### Task 4.1: Penambahan Metode `getLockedFloatRatio()` di `Instrument.java`
- **Berkas Target:** `android/app/src/main/java/com/tradingglobal3d/core/market/Instrument.java`
- **Rumus Perhitungan:**
  ```java
  public synchronized double getLockedFloatRatio(long totalLockedShares) {
      long total = getIssuedShares();
      if (total <= 0) return 0.0;
      double ratio = (double) totalLockedShares / (double) total;
      return Math.min(0.95, Math.max(0.0, ratio));
  }
  ```

### Task 4.2: Integrasi Pengali Kenaikan Harga di `NpcAgent.java` (Market Maker)
- **Berkas Target:** `android/app/src/main/java/com/tradingglobal3d/core/npc/NpcAgent.java`
- **Lokasi Kode:** Blok `strategy == NpcStrategyType.MARKET_MAKER` (baris 57–120).
- **Langkah Spesifik:**
  1. Periksa sisa stok saham yang dipegang akun Market Maker (`ACC-NPC-MM-01`).
  2. Hitung rasio saham yang terkunci di luar Market Maker:
     ```java
     double lockedRatio = 1.0 - ((double) mmHoldingShares / (double) instrument.getIssuedShares());
     ```
  3. Tetapkan pengali spread dan step harga ask:
     - Jika `lockedRatio < 0.30`: normal (pengali 1.0x).
     - Jika `0.30 <= lockedRatio < 0.60`: pengali **1.35x**.
     - Jika `0.60 <= lockedRatio < 0.80`: pengali **1.80x**.
     - Jika `lockedRatio >= 0.80`: pengali **2.50x** (*Extreme Squeeze*).
  4. Ketika pemain atau bot melakukan transaksi beli, harga Ask melompat lebih tinggi mengikuti pengali kelangkaan, namun tetap di bawah batas ARA harian (`getDailyCeilingPrice()`).

### Task 4.3: Unit Test & Verifikasi Fase 4
- **Berkas Uji:** `android/app/src/test/java/com/tradingglobal3d/core/synthesis/SectorPieAndSupplySqueezeTest.java`
- **Kriteria Lolos:** Tes `testSupplySqueezeMultiplierCurve` dan `testCumulativeFloatLockingFromPlayerAndFunds` lulus 100%.

---

## FASE 5: IMPLEMENTASI MODUL 5 — JANTUNG SIKLUS MAKROEKONOMI TERPADU (LEVEL 1..9)

Membangun satu indikator makroekonomi yang secara serentak mengendalikan bunga deposito, bunga pinjaman bank, ukuran pasar industri, dan antusiasme IPO.

### Task 5.1: Penambahan State `macroIndex` di `TradingFacade.java`
- **Berkas Target:** `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java`
- **Langkah Spesifik:**
  1. Tambahkan field:
     ```java
     private int macroIndex = 5; // Skala 1 (Resesi Parah) s.d. 9 (Booming), default 5
     ```
  2. Tambahkan metode publik:
     - `public synchronized int getMacroIndex()`
     - `public synchronized void setMacroIndex(int index)`
  3. Simpan dan pulihkan nilai `macroIndex` pada `SaveState.java` / `getSaveMetadata()` agar tidak hilang saat aplikasi ditutup.

### Task 5.2: Evaluasi Siklus Kuartalan pada `TradingFacade.processCalendarActions()`
- **Berkas Target:** `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java`
- **Lokasi Kode:** Metode `processCalendarActions(LocalDate nextDate)` (baris 1970–2021).
- **Langkah Spesifik:**
  Setiap pergantian kuartal bursa (akhir bulan 3, 6, 9, dan 12):
  ```java
  if (nextDate.getDayOfMonth() == 1 && (nextDate.getMonthValue() == 1 || nextDate.getMonthValue() == 4 || nextDate.getMonthValue() == 7 || nextDate.getMonthValue() == 10)) {
      // Evaluasi tren pasar kuartal lalu
      int sentimentShift = evaluateQuarterlyMacroTrend();
      if (sentimentShift > 0 && macroIndex < 9) macroIndex++;
      else if (sentimentShift < 0 && macroIndex > 1) macroIndex--;
  }
  ```

### Task 5.3: Pengikatan Siklus Makro ke Seluruh Sub-Sistem
1. **Ke `TimeDepositManager.java`:**
   - Suku bunga deposito berjangka dihitung dengan rumus:
     $$\text{BungaDeposito} = 9.5\% - (\text{macroIndex} - 1) \times \frac{5.5\%}{8}$$
     *(Resesi = 9.5% p.a., Normal = 6.75% p.a., Booming = 4.0% p.a.).*
2. **Ke `InstallmentLoanManager.java`:**
   - Suku bunga pinjaman bank mengikuti bunga deposito + spread risiko 4.0%.
3. **Ke Ukuran Kue Sektor (`CompanyManager.java`):**
   - Kue sektor dikalikan faktor:
     $$\text{Multiplier} = 1.0 + (\text{macroIndex} - 5) \times 0.075$$
     *(Level 1 = 0.75x, Level 5 = 1.00x, Level 9 = 1.30x).*
4. **Ke Penjatahan IPO (`CorporateActionService.java`):**
   - Minat investor dan persentase listing gain IPO dikalikan faktor:
     $$\text{IpoGainMult} = 0.80 + (\text{macroIndex} - 1) \times \frac{0.60}{8}$$

### Task 5.4: Unit Test & Verifikasi Fase 5
- **Berkas Uji:** `android/app/src/test/java/com/tradingglobal3d/core/synthesis/MacroCycleEngineTest.java`
- **Kriteria Lolos:** 4 tes pengujian makro lulus 100%.

---

## FASE 6: INTEGRASI ANTARMUKA UI WEBVIEW 2D PORTRAIT & NATIVE BRIDGE

Menampilkan metrik baru secara elegan dan ramah satu tangan pada layar ponsel tanpa menambah kepadatan (*clutter*) yang berlebihan.

### Task 6.1: Penambahan API Bridge pada `TradingWebBridge.java`
- **Berkas Target:** `android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java`
- **Metode Baru yang Diekspos ke Javascript:**
  ```java
  @JavascriptInterface
  public String getMacroAndSynthesisState() {
      try {
          JSONObject json = new JSONObject();
          json.put("macroIndex", facade.getMacroIndex());
          json.put("macroLabel", getMacroLabel(facade.getMacroIndex()));
          json.put("depositRateAnnual", facade.getEffectiveDepositRate());
          json.put("loanRateAnnual", facade.getEffectiveLoanRate());
          return json.toString();
      } catch (Exception e) {
          return "{}";
      }
  }
  ```

### Task 6.2: Penambahan Informasi Nilai Intrinsik & GapRate di Tab Pasar (`app.js`)
- **Berkas Target:** `android/app/src/main/assets/www/app.js`
- **Langkah UI:**
  1. Pada halaman detail saham (`renderStockDetail`), tambahkan baris metrik ringkas di bawah grafik *candlestick*:
     ```html
     <div class="fundamental-strip">
       <span class="badge-fair">Nilai Wajar: Rp 11.800</span>
       <span class="badge-gap discount">Diskon 24.2% (Strong Buy)</span>
     </div>
     ```
  2. Gunakan warna hijau lembut untuk diskon (*undervalued*) dan warna merah kusam untuk kemahalan (*overvalued*).

### Task 6.3: Tampilan Indikator Siklus Makro di Header Konteks
- **Berkas Target:** `android/app/src/main/assets/www/index.html` & `app.js`
- **Langkah UI:**
  Di samping indikator sesi bursa (WIB), tampilkan pil status makro:
  - Level 1–3: `Krisis / Resesi` (Aksen Merah/Muted Rose)
  - Level 4–6: `Ekonomi Normal` (Aksen Navy/Amber)
  - Level 7–9: `Pasar Booming` (Aksen Emerald/Hijau)

### Task 6.4: Audit Ergonomi Portrait 2D
- **Aturan Wajib:**
  - Seluruh tombol dan baris interaktif memiliki tinggi sentuh minimal **$\ge 48\text{ dp}$**.
  - Tetap menggunakan Dark Theme asli (Navy `#0f172a`, Slate, Amber `#fbbf24`, teks `#f8fafc`).
  - Tidak ada komponen yang meluap horizontal (*zero horizontal overflow*).

---

## FASE 7: UJI REGRESI MENYELURUH, VERIFIKASI MEMORI & BUILD APK ANDROID

Memastikan seluruh sistem terintegrasi sempurna tanpa bug, tanpa kebocoran memori, dan berhasil dibangun menjadi file APK Android.

### Task 7.1: Eksekusi Seluruh Suite Pengujian JUnit
- **Perintah Eksekusi:**
  ```bash
  python3 "C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/run_synthesis_tests.py"
  ```
- **Kriteria Lolos:** 13/13 tes sintesa lulus (0 failure, 0 error).

### Task 7.2: Uji Persistensi Save/Load SQLite
- **Langkah Verifikasi:**
  1. Mulai game baru, lakukan transaksi hingga `intrinsicValue`, `macroIndex`, dan kue sektor bergerak.
  2. Simpan permainan (`facade.save()`).
  3. Tutup dan muat kembali save data (`facade.load()`).
  4. Verifikasi bahwa `macroIndex`, status kue sektor, dan posisi portofolio pulih 100% identik tanpa corrupt.

### Task 7.3: Build APK Debug Android Resmi
- **Perintah Eksekusi:**
  ```bash
  cd "C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android"
  export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
  export PATH="$JAVA_HOME/bin:$PATH"
  ./gradlew assembleDebug --no-daemon --console=plain
  ```
- **Kriteria Lolos:** Build menghasilkan `android/app/build/outputs/apk/debug/app-debug.apk` dengan status `BUILD SUCCESSFUL`.

### Task 7.4: Dokumentasi Laporan Final
- Mengarsipkan seluruh log pengujian dan SHA256 APK ke dalam folder `Artifacts/`.

---

## 10. Matriks Checklist Verifikasi & Kriteria Penerimaan

| ID Tiket | Komponen / File | Kriteria Selesai (*Definition of Done*) | Status Verifikasi |
|---|---|---|---|
| **T-01** | `SynthesisMarketSimulationEngine.java` | 4 modul matematika (VirCal, SectorPie, SupplySqueeze, MacroCycle) terimplementasi bersih. | ✅ Selesai & Lulus Unit Test |
| **T-02** | `Instrument.java` | Memiliki field `intrinsicValue` & rumus `calculateIntrinsicValue()`. Tersimpan di Save JSON. | ⏳ Siap Diintegrasikan |
| **T-03** | `NpcAgent.java` | Bot pembeli mengevaluasi 3 rezim peluang (40%, 63%, 68%) terhadap harga wajar. | ⏳ Siap Diintegrasikan |
| **T-04** | `ResearchService.java` | Rumus `GapRate` aktif dan sinyal konsensus analis (*Strong Buy*, *Buy*, *Hold*, *Sell*) tampil di JSON. | ⏳ Siap Diintegrasikan |
| **T-05** | `OperatingCompany.java` | Perusahaan pemain memiliki skor daya saing R&D/Moral dan memperebutkan kue sektor secara zero-sum. | ⏳ Siap Diintegrasikan |
| **T-06** | `Instrument.java` (Squeeze) | Pengali spread ask Market Maker naik $1.35\times$ s.d. $2.50\times$ saat float publik dikunci $> 40\%$. | ⏳ Siap Diintegrasikan |
| **T-07** | `TradingFacade.java` (Makro) | State `macroIndex` (1..9) bergeser tiap kuartal dan menggerakkan bunga bank & listing gain IPO. | ⏳ Siap Diintegrasikan |
| **T-08** | `TradingWebBridge.java` | API bridge mengekspos metrik makro & fundamental ke antarmuka WebView. | ⏳ Siap Diintegrasikan |
| **T-09** | `www/app.js` & `style.css` | Tampilan nilai wajar, gap rate, dan indikator makro tampil rapi dengan standar portrait $\ge 48\text{ dp}$. | ⏳ Siap Diintegrasikan |
| **T-10** | JUnit Suite & Gradle Build | 13/13 unit test lulus 100% dan APK Android debug berhasil di-compile tanpa error. | ⏳ Siap Diintegrasikan |

---

*Dokumen ini dirancang sebagai panduan eksekusi mandiri tanpa asumsi tersembunyi. Setiap langkah memiliki lokasi file absolut, potongan kode acuan, rumus matematis pasti, dan perintah verifikasi yang dapat langsung dijalankan.*
