# Prompt Eksekusi Lengkap: Sintesa 5 Fitur Masterpiece (Game Saham 2D Portrait Android)
## Proyek: Game Saham Android (Repo: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`)

Kamu adalah AI Principal Systems Engineer yang ditugaskan untuk mengeksekusi secara penuh dan menyeluruh rancangan pada dokumen:  
`Docs/PLAN_EKSEKUSI_SINTESA_5_MASTERPIECE.md` ke dalam basis kode produksi Java Android `com.tradingglobal3d`.

Tugasmu adalah membangun, mengintegrasikan, dan memverifikasi artefak kode produksi nyata sampai selesai 100%, bukan sekadar menulis ringkasan, membuat rencana ulang, atau menghasilkan kode pura-pura (*stubs/mocking*).

---

### ATURAN UTAMA & LARANGAN KERAS (HARAP DIPATUHI SECARA MUTLAK):
1. **DILARANG MERINGKAS ATAU MENGGABUNGKAN TASK SECARA ILEGAL:**  
   Jangan memangkas, meringkas, atau melompati subtask apa pun. Setiap modul dari ke-5 fitur Masterpiece, fitur kenyamanan waktu (*Session Fast-Forward*), jembatan bridge, dan UI WebView wajib diimplementasikan secara utuh (*unbundled*).
2. **KEDAULATAN LOGIKA TUNGGAL (JAVA DOMAIN):**  
   Seluruh aturan bursa, formula nilai wajar (*VirCal*), naluri bot NPC, perebutan kue sektor *zero-sum*, *supply squeeze*, dan siklus makro 1..9 dikelola 100% di Java Domain (`com.tradingglobal3d.core`). JavaScript di WebView (`app.js`) murni sebagai penampil data dan penangkap aksi sentuh.
3. **PROTEKSI FITUR EXISTING (SINTESA KOLABORASI, BUKAN MENIMPA):**  
   Dilarang menghapus atau merusak arsitektur yang sudah berjalan: sistem *Order Book*, *Matching Engine*, antrean limit/market order, pembagian 36 IPO bulanan, fasilitas pinjaman bank, dan penyimpanan SQLite/SaveState JSON wajib tetap utuh dan lulus pengujian regresi.
4. **PERTAHANKAN RITME WAKTU 24× & KALENDER 365 HARI:**  
   Pertahankan rasio waktu 24× (1 detik nyata = 24 detik game) pada `GameClock.java`. Jangan mengubah angka rasio ini. Kenyamanan waktu diatasi dengan tombol pintas *"Lompati Jeda Sesi"* (Skip Break 11:31 $\rightarrow$ 13:30 dan Skip Sore 16:01 $\rightarrow$ 21:30).
5. **DILARANG MENGAKHIRI TURN DENGAN JANJI TINDAKAN MASA DEPAN:**  
   Jika suatu langkah dapat dieksekusi, jalankan langsung dengan alatmu. Setiap klaim selesai wajib didukung bukti eksekusi perintah riil (exit code 0).

---

### DOKUMEN & KODE RUJUKAN WAJIB (BACA SEBELUM CODING):
1. `Docs/PLAN_EKSEKUSI_SINTESA_5_MASTERPIECE.md` — Rencana eksekusi master plan lengkap per fase.
2. `android/app/src/test/java/com/tradingglobal3d/core/synthesis/SynthesisMarketSimulationEngine.java` — Sumber model matematika murni yang sudah teruji 100% lulus (VirCal, TakerBias, SectorPie, SupplySqueeze, MacroCycle).
3. `android/app/src/main/java/com/tradingglobal3d/core/market/Instrument.java` — Entitas instrumen saham.
4. `android/app/src/main/java/com/tradingglobal3d/core/npc/NpcAgent.java` — Agen bot Market Maker & Retail Noise Taker.
5. `android/app/src/main/java/com/tradingglobal3d/core/business/OperatingCompany.java` & `CompanyManager.java` — Model perusahaan operasional di tab Bisnis.
6. `android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java` — Layanan riset, konsensus analis, dan berita.
7. `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java` — Fasade orkestrator domain bursa.
8. `android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java` & `android/app/src/main/assets/www/app.js` — Jembatan native WebView & tampilan antarmuka portrait.

---

### CAKUPAN MODUL YANG WAJIB DISELESAIKAN:

#### MODUL 1: NILAI INTRINSIK & NALURI BOT TAKER (`Instrument.java` & `NpcAgent.java`)
- [ ] Tambahkan field `intrinsicValue`, `trailingEps`, dan `sectorAvgPer` ke `Instrument.java`.
- [ ] Terapkan metode `calculateIntrinsicValue()` dengan formula *VirCal*:
  $$\text{RawFair} = \text{EPS} \times \text{PER}_{\text{sektor}}$$
  Dibatasi koridor $[0.60\times .. 1.95\times \text{Harga Pasar}]$, redaman $> 300.000$ ($\times 0.8$) dan $> 500.000$ ($\times 0.7$), serta batas bawah nominal bursa.
- [ ] Ubah strategi `RETAIL_NOISE` di `NpcAgent.java`: saat berita netral, bot pembeli mengevaluasi 3 rezim gravitasi:
  - $P \ge V \rightarrow 40\%$ Beli (60% Jual).
  - $P < V \le 1.5P \rightarrow 63\%$ Beli.
  - $1.5P < V \rightarrow 68\%$ Beli dengan volume lot digandakan ($2.0\times$).
- [ ] Sertakan data `intrinsicValue` dan `trailingEps` dalam serialisasi `exportMarketState()` dan `restoreMarketState()`.

#### MODUL 2: RADAR DISKON SAHAM & SINYAL ANALIS FUNDAMENTAL (`ResearchService.java`)
- [ ] Implementasikan kalkulasi $\text{GapRate} = \frac{\text{Fair} - \text{Harga}}{\text{Harga}} \times 100\%$ pada `ResearchService.java`.
- [ ] Tentukan sinyal rekomendasi analis: `STRONG_BUY` ($> 15\%$), `BUY` ($> 5\%$), `HOLD` ($-5\%..+5\%$), `SELL` ($< -5\%$).
- [ ] Ekspos `intrinsicValue`, `gapRatePercent`, dan `fundamentalSignal` ke endpoint JSON `researchQuery(kind="consensus")`.

#### MODUL 3: SINERGI PEREBUTAN KUE SEKTOR ZERO-SUM (`OperatingCompany.java` & `CompanyManager.java`)
- [ ] Definisikan `SectorPie` bulanan per sektor (Energi Rp 25T, Manufaktur Rp 20T, Ritel Rp 15T, Jasa Rp 30T).
- [ ] Implementasikan `getMarketCompetitivenessScore()` pada `OperatingCompany.java` berbasis moral pegawai, tunjangan fasilitas kantor, dan jam kerja.
- [ ] Pada `CompanyManager.processDailyOperations()`, bagikan porsi kue harian secara *zero-sum* antara perusahaan pemain dan emiten bursa sejenis. Pendapatan emiten kompetitor yang tersedot berkurang secara logis $\rightarrow$ laba turun $\rightarrow$ nilai intrinsik emiten turun.

#### MODUL 4: EFEK KELANGKAAN SAHAM / SUPPLY SQUEEZE (`Instrument.java` & `NpcAgent.java`)
- [ ] Hitung rasio saham yang terkunci di luar Market Maker (`lockedFloatRatio` dari akumulasi treasury + akun pribadi pemain + mandat klien AUM + reksa dana).
- [ ] Pada strategi `MARKET_MAKER` di `NpcAgent.java`: saat `lockedFloatRatio >= 0.40`, tebalkan spread ask dan pasang pengali step kenaikan harga ($1.35\times$ di $> 30\%$, $1.80\times$ di $> 60\%$, $2.50\times$ di $> 80\%$) dengan tetap terkunci aman di bawah batas ARA harian.

#### MODUL 5: JANTUNG SIKLUS MAKROEKONOMI TERPADU (LEVEL 1..9) (`TradingFacade.java`)
- [ ] Tambahkan variabel state `macroIndex` (Level 1 Resesi s.d. Level 9 Booming, default 5) di `TradingFacade.java` dan simpan ke `SaveState.java`.
- [ ] Evaluasi pergeseran siklus makro setiap kuartal bursa di `TradingFacade.processCalendarActions()`.
- [ ] Ikat serentak ke:
  - Bunga deposito di `TimeDepositManager.java`: $\text{Bunga} = 9.5\% - (\text{macroIndex} - 1) \times \frac{5.5\%}{8}$.
  - Bunga pinjaman di `InstallmentLoanManager.java`: bunga deposito + spread 4.0%.
  - Pengali kue sektor: $1.0 + (\text{macroIndex} - 5) \times 0.075$.
  - Pengali gairah listing 36 jadwal IPO: $0.80 + (\text{macroIndex} - 1) \times \frac{0.60}{8}$.

#### PACING & KENYAMANAN WAKTU: TOMBOL LOMPATI JEDA SESI (`TradingWebBridge.java` & `app.js`)
- [ ] Tambahkan fungsi bridge `@JavascriptInterface public String skipToNextMarketSession()`:
  - Jika waktu game di jam `11:31 – 13:29` (Istirahat IDX), majukan waktu game langsung ke `13:30:00`.
  - Jika waktu game di jam `16:01 – 21:29` (Pasca Penutupan IDX), majukan waktu game langsung ke `21:30:00` (Pembukaan US).
- [ ] Munculkan tombol pintas *"Lanjut ke Sesi II"* dan *"Lanjut ke Pembukaan US"* di antarmuka UI saat jam jeda tersebut berlangsung.

#### MODUL 6: INTEGRASI ANTARMUKA UI 2D PORTRAIT (`TradingWebBridge.java`, `app.js`, `style.css`)
- [ ] Tambahkan method bridge `@JavascriptInterface public String getMacroAndSynthesisState()`.
- [ ] Tampilkan badge Nilai Wajar dan Diskon (*GapRate*) di bawah grafik *candlestick* pada layar detail saham.
- [ ] Tampilkan pil status siklus makro (Level 1..9) di header konteks.
- [ ] Verifikasi kepatuhan ergonomi portrait: touch target $\ge 48\text{ dp}$, dark theme navy/amber, dan *zero horizontal overflow*.

---

### TAHAPAN PELAKSANAAN SISTEMATIS:

1. **TAHAP 1: PRE-FLIGHT & VERIFIKASI ENVIRONMENT**
   - Atur environment `JAVA_HOME` ke `/c/Program Files/Android/Android Studio/jbr`.
   - Jalankan `python3 Work/run_synthesis_tests.py` untuk mengonfirmasi baseline 13/13 PASS.
2. **TAHAP 2: INTI DOMAIN (MODUL 1 & 4 — INSTRUMENT & NPC AGENT)**
   - Edit `Instrument.java` dan `NpcAgent.java`.
   - Jalankan kompilasi javac dan pengujian unit `IntrinsicValueGravityAndRadarTest`.
3. **TAHAP 3: RISET & KONSENSUS ANALIS (MODUL 2)**
   - Edit `ResearchService.java`.
   - Verifikasi output JSON pada endpoint `researchQuery(kind="consensus")`.
4. **TAHAP 4: OPERASI BISNIS & SIKLUS MAKRO (MODUL 3 & 5)**
   - Edit `OperatingCompany.java`, `CompanyManager.java`, dan `TradingFacade.java`.
   - Jalankan pengujian unit `SectorPieAndSupplySqueezeTest` dan `MacroCycleEngineTest`.
5. **TAHAP 5: JEMBATAN BRIDGE, PACING WAKTU & UI WEBVIEW (MODUL 6)**
   - Edit `TradingWebBridge.java`, `android/app/src/main/assets/www/app.js`, dan `style.css`.
   - Implementasikan tombol pintas lompati jeda sesi dan badge fundamental.
6. **TAHAP 6: UJI REGRESI PURNALENGKAP & BUILD APK ANDROID**
   - Jalankan seluruh tes domain dan tes sintesa.
   - Lakukan build APK Android resmi via Gradle:
     ```bash
     cd "C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android"
     export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
     export PATH="$JAVA_HOME/bin:$PATH"
     ./gradlew assembleDebug --no-daemon --console=plain
     ```
   - Verifikasi keberadaan dan SHA256 dari `android/app/build/outputs/apk/debug/app-debug.apk`.

---

### FORMAT LAPORAN KEMAJUAN (SETIAP SELESAI TAHAP/MILESTONE):
- **Progres:** X dari 6 Tahap Selesai (Tahap Aktif: ...)
- **Keluaran Nyata:** Berkas yang dibuat/dimodifikasi beserta baris perubahannya.
- **Pemeriksaan & Bukti:** Perintah uji riil yang dijalankan beserta outputnya (exit code 0 / PASS).
- **Hambatan:** Catat jika ada kendala nyata, atau "Tidak ada".
- **Langkah Berikutnya:** Tugas terdekat yang akan langsung dikerjakan.

---

MULAI SEKARANG DARI TAHAP 1 & 2: Buka `Docs/PLAN_EKSEKUSI_SINTESA_5_MASTERPIECE.md`, periksa `Instrument.java` serta `NpcAgent.java`, dan laksanakan implementasi kode secara mandiri hingga selesai tuntas!
