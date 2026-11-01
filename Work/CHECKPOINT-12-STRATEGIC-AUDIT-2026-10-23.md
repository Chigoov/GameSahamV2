# Checkpoint Audit Masterplan 12 Fitur Strategis — 2026-10-23

## Status

**Belum selesai.** Masterplan mengandung gap acceptance yang nyata. Jangan menggunakan status historis `ALL_4_TAHAP_COMPLETED_AND_VERIFIED` sebagai kesimpulan; status telah dikoreksi di `Work/STATUS.json` menjadi `PARTIAL_REAUDIT_GAPS_OPEN`.

- Repo: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`
- Branch: `codex/classic-ui-new-assets`
- HEAD terakhir yang diperiksa sebelum audit: `afd8b23581d0b760f96132dae29ad488849880c4` (perubahan audit belum dikomit)
- Audit milestone: **3/5 selesai**; M2 acceptance audit & perbaikan masih aktif; M4 review/checkpoint terakhir berjalan.
- Save pemain terlindungi: `Artifacts/LocalPreview/preview-save/save_game_v2.json`
  - SHA-256 awal/akhir: `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`
  - Tidak dipakai untuk test, tidak diubah.
- QA memakai save terisolasi `Artifacts/Owner-Revision-14-2026-10-22/run-01/qa-save/` dan port 8794; tidak menyentuh port 8766, 8792 atau 8793.

## Perbaikan dan bukti yang telah diverifikasi

1. **F01 Deposito:** bonus produk 10/25/50% ditambahkan ke bunga efektif, UI menyebut tenor sesi dan total bunga hangus saat pencairan dini. Manager deposito menyimpan dan memulihkan state melalui save game JSON.
   - `Work/run-s1-test.py`: 10/10 JUnit PASS.
   - Bukti mencakup tenor/bonus, bunga ekstra, bunga terakru yang hangus, pokok kembali, dan state aktif melewati save/load.
2. **F02 Asuransi:** pembelian ditolak bila salah satu pasar sudah buka; klaim dibayar hanya untuk crash **>5%**; event Black Swan mengumpulkan kerugian posisi pemain dan memicu klaim otomatis. Polis tersimpan/muat kembali.
   - S1: 10/10 PASS, termasuk batas 5% vs 5,01%, pembelian waktu pasar buka, klaim 80%, event shock serta persistensi.
3. **F03 Turnaround — masih parsial:** endpoint gratis Rp25 juta diganti dengan gate insolvensi (utang aktif, kas IDR < Rp500.000, kekayaan bersih ≤0), preview daftar koleksi/valuasi, lelang koleksi non-pledged, pelunasan utang margin dari hasil lelang, restrukturisasi utang dan receipt idempoten. UI menunjukkan preview dan menolak pemain sehat tanpa melelang.
   - S1: 10/10 PASS untuk penolakan akun sehat, rescue margin, lelang, tidak ada cash grant, dan retry idempoten; Browser QA PASS untuk penolakan akun sehat.
   - **Belum tuntas:** alur pinjaman tenor melalui konsultan belum diuji dengan kontrak pinjaman nyata; extension 30 hari merupakan asumsi implementasi yang harus dikonfirmasi/ditetapkan. Penalti reputasi/struktur rescue perlu review domain terpisah.
4. **F04 Holding — domain + UI host selesai diuji:** empat bucket sektor wajib, anggota grup yang unik, transfer/dividen internal hanya antar anggota, reserve operasi dipertahankan, dua mutasi ledger tanpa pajak bursa, idempotensi, dan persistensi group.
   - S2: 5/5 PASS; Browser QA menyelesaikan pembuatan empat anak usaha, membentuk holding, menampilkan grup dan melakukan transfer dividen internal.
   - State holding berada di save JSON runtime, **bukan** tabel SQLite `HoldingSubsidiaries`.
5. **F08 Streak — multiplier telah berdampak pada reward sponsor:** bonus klaim harian dihitung sebagai Rp25 juta × multiplier; status/receipt/ledger memakai nilai yang sama. Streak tersimpan.
   - S4: 7/7 PASS, termasuk klaim 2× = Rp50 juta, save/load, progres, broken-streak, dan matched BUY fee discount untuk F09.
   - **Masih perlu audit:** definisi hari streak terhadap kalender dan login setelah offline, termasuk whether current session increments once/day, belum diuji melintasi inactive catch-up.
6. **F09 dominasi sektor:** ambang tegas `>25%`; diskon fee 25% diterapkan pada fee beli/jual normal, preview/reservasi dan call auction melalui fee policy yang disimpan pada order. Tech-tree fee reduction ikut ditambahkan ke rate.
   - S4: tes ambang (25% vs 26%) dan matched BUY commission discount PASS.
   - **Belum lengkap:** hak veto belum mengubah outcome kebijakan.
7. **F01/F02/F03 visual:** `Work/verify-s1-deposits-insurance.js` Browser QA PASS untuk status polis, gate bailout akun sehat, 3 produk tenor dan pesan pencairan dini. S4 Browser QA PASS menampilkan 12 sektor dan mencapai kartu terakhir dengan scroll; screenshot sudah diperiksa visual.
8. **F04 visual:** `Work/run-verify-s2.py` Browser QA PASS; screenshot akhir `Artifacts/Overall-Preview-2026-10-23/TahapS2-holding-custom-funds.png` menampilkan alur transfer internal dan receipt.
9. **SQLite:** database statis dibuka read-only; terdapat 22 tabel, termasuk tabel strategi. Namun ini **bukan** bukti table-table strategi membaca/menulis state runtime.
10. **APK kandidat terbaru:** `android/app/build/outputs/apk/debug/app-debug.apk`
    - 1,184,201 byte
    - SHA-256 `7e1a93f4307ed3487914cac5bc1a5b09ee6a2bdd03defe58dd902b1280fe94c7`
    - `assembleDebug` dan `compileDebugJavaWithJavac` PASS.
    - `apksigner verify`: V2 valid (Android Debug signer).
    - `assets/www/index.html`, `assets/www/app.js`, `assets/data/gamesaham.db` cocok byte-for-byte dengan source.
    - Tidak diinstal ke emulator/perangkat.
11. **Total targeted runner akhir:** S1 10 + S2 5 + S3 3 + S4 7 = **25 JUnit PASS**; 3 browser QA tasks terakhir lulus sesuai cakupan di atas. Full `testDebugUnitTest` masih gagal startup loopback seperti dijelaskan di bawah.
12. `testDebugUnitTest` Gradle **BLOCKED/FAIL** pada startup worker karena `java.net.SocketException: Invalid argument: connect` / `Unable to establish loopback connection`; suite penuh belum berjalan. Percobaan awal tanpa `JAVA_HOME` juga gagal sebelum test startup. Jangan laporkan sebagai PASS.

## Gap acceptance tersisa (belum boleh ditandai selesai)

- **F05 Black Swan:** model shock saat ini sudah menjaga kas, memberi kenaikan emas 5%, menghitung offset pada eksposur emas, dan mengetes satu jalur tersebut di S4. Belum ada model kedalaman likuiditas/market impact safe haven, dan belum ada bukti menyeluruh lintas jenis aset/save-load.
- **F06 Supply Chain:** quote sektor berubah dan daily operating cost manufaktur/ritel serta revenue energi berubah; S4 mengetes angka perubahan. Belum terbukti settlement siklus bisnis memakai modifier itu dan mem-posting hasil ke ledger end-to-end.
- **F07 Tech tree:** signal threshold, diskon fee komisi dan kapasitas AUM +50% kini memiliki efek domain terukur serta UI; targeted JVM/browser lulus. Baseline AUM dan interpretasi bonus sensitivitas adalah asumsi simulasi terdokumentasi, bukan angka literal yang diberi masterplan. Lihat M2A/F07 di bawah.
- **F09 Dominasi sektor:** ambang dan diskon komisi transaksi telah diuji; hak veto belum mengubah outcome kebijakan.
- **F10 Headhunting:** perekrutan pemain tersedia, tetapi rival tidak membajak staf pemain saat moral rendah; staf headhunt belum terintegrasi ke ekonomi firma.
- **F11 Reksa Dana Kustom:** definisi alokasi dan validasi 100% ada serta dipersistenkan; unit, subscription client AUM, ledger fee berkala, performance fee belum berjalan.
- **F12 Kripto:** katalog BTC/ETH tersedia; alur perdagangan 24 jam dan bebas suspension belum dibuktikan atau dimodelkan lengkap.
- **F03/F08:** batas di atas pada bagian verifikasi perlu dilengkapi.
- Acceptance eksplisit mensyaratkan C# Domain, tetapi source proyek yang berlaku adalah Java + Android WebView; tidak dibuat C# paralel. Ini konflik requirement dari prompt historis dengan instruksi arsitektur produk yang aktif.

## Perubahan lokal dan batas integrasi

Perubahan audit belum dikomit. Pertahankan perubahan pengguna/untracked yang telah ada. Jangan reset/clean/stash. File source utama yang disentuh audit:
- `android/app/src/main/java/com/tradingglobal3d/core/multiasset/TimeDeposit.java`
- `android/app/src/main/java/com/tradingglobal3d/core/multiasset/TimeDepositManager.java`
- `android/app/src/main/java/com/tradingglobal3d/core/risk/PortfolioInsuranceService.java`
- `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java`
- `android/app/src/main/java/com/tradingglobal3d/core/player/DailyStreakService.java`
- `android/app/src/main/java/com/tradingglobal3d/core/market/SectorDominanceService.java`
- `android/app/src/main/java/com/tradingglobal3d/core/research/ResearchTechTreeService.java`
- `android/app/src/main/java/com/tradingglobal3d/core/firm/HeadhuntingService.java`
- `android/app/src/main/java/com/tradingglobal3d/core/business/CompanyManager.java`
- `android/app/src/main/java/com/tradingglobal3d/core/trading/Order.java`, `OrderManager.java`, `MatchingEngine.java`
- `android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java`
- `android/app/src/main/assets/www/app.js`
- test runner/test files `Work/run-s1-test.py`, `Work/run-s2-test.py`, `Work/run-s3-test.py`, `Work/run-s4-test.py`, `Work/verify-s1-deposits-insurance.js`, `Work/verify-s2-holding-custom-funds.js`, `android/app/src/test/java/com/tradingglobal3d/core/LiquidityInsuranceAndBailoutTahapS1Test.java`, `HoldingAndCustomFundsTahapS2Test.java`, `TechTreeAndHeadhuntingTahapS3Test.java`, `DailyStreakAndMarketShockTahapS4Test.java`.

`Work/WireQaServer.java` and `Work/run-verify-s2.py` contain a QA-only initial-capital option used to provision the isolated fixture for four companies. It is local test support only and not exposed in APK.

## Lanjutkan dari sini

1. Jangan mengulang mutation yang sudah terverifikasi tanpa alasan. Baca status/Git diff dan checkpoint ini.
2. Tutup F03 installment-loan restructuring acceptance; review atomic/idempotent edge cases.
3. Tuntaskan F05/F06/F07/F09/F10/F11/F12 satu per satu dengan test-first, jangan menggabungkan.
4. Jalankan browser matrix yang relevan, targeted/full host tests (full Gradle loopback blocker masih terbuka), build APK terbaru dan hash/asset/signature check.
5. Sinkronkan `Work/STATUS.json` hanya berdasarkan bukti baru; jangan push/install tanpa scope pemilik.

## Checkpoint progres lanjutan — F07 dan pemulihan gate (2026-10-23)

### F07 — implementasi kandidat; targeted host/browser sudah lulus

- Kapasitas AUM dihitung dari jumlah setoran awal IDR-equivalent seluruh kontrak mandat yang belum TERMINATED; nilai FX memakai mid-rate domain saat dihitung. `TECH-SCALE-1` menambah 50% dari baseline kontrak tersebut. Ini **asumsi simulasi**, bukan angka yang dinyatakan masterplan; dipilih agar basis kapasitas dapat diturunkan dari kewajiban mandat nyata dan tidak bergantung angka arbitrer.
- `acceptClientMandate` dan `clientDeposit` menolak setoran yang membuat AUM melampaui batas; mandat/akun lama tetap bisa dibaca/dipulihkan dan tidak dihapus. AUM berjalan tetap dibaca dari akun aktif dan state fitur tetap memakai save JSON yang ada, bukan migrasi/penulisan SQLite.
- `getFirmSnapshot`/UI Kelola Dana menampilkan kapasitas, utilisasi, dan bonus Scale.
- `getAlgorithmicMarketSignal` memakai perubahan harga sesi berjalan. Bonus `SIGNAL_ACCURACY` menurunkan ambang sinyal dari `1.0 - bonus/100` (minimum 0.10%); output menandai bahwa itu sinyal simulasi dan tidak memakai data masa depan. UI Tech Tree menyediakan tombol BBCA. Ini mengubah sensitivitas deteksi, **bukan bukti akurasi prediktif**.
- Tes `TechTreeAndHeadhuntingTahapS3Test`: 5/5 lulus pada `python Work/run-s3-test.py` (batas sebelum/sesudah Scale dan perubahan hasil sinyal); browser `python Work/run-verify-s3.py` lulus dan memverifikasi tombol sinyal, kapasitas firma serta alur lain yang tercakup runner.
- Test AUM diperluas setelah run terakhir agar membuktikan save/load kapasitas dan AUM aktual. Ekstensi tersebut belum ada bukti run tersendiri pada saat checkpoint ini dibuat.

### Runner dan suite penuh

- `Work/run-s1-test.py` s.d. `run-s4-test.py` kini mengembalikan exit code JUnit melalui `SystemExit(res.returncode)`. Uji simulasi non-mutatif terhadap keempat skrip memaksa hasil JUnit exit 1 dan membuktikan masing-masing skrip keluar 1; javac tetap memakai `check=True`.
- Direktori root `.socket-qa` dibuat untuk konfigurasi socket khusus proses uji; Gradle dijalankan dengan `JAVA_TOOL_OPTIONS` scoped, `--no-daemon --no-parallel --max-workers=1 --offline`.
- Full `testDebugUnitTest` berhasil memasuki task test (jadi masalah startup loopback awal teratasi), tetapi saat checkpoint masih berjalan. Log menunjukkan kegagalan pada `AllFeaturesExtreme10x100CriticalSequenceTest` (sequences 01,03,04,05,07; assertion submit BUY di baris 177) dan `CareerAccessTest.realTradingRewardsAndSalaryReachClientManagementWithoutInjectedState` baris 73. Belum ada hasil final suite atau assertion message lengkap; keduanya **jangan ditandai PASS** dan perlu diinvestigasi dari report final.
- Build APK dan test Android/native belum dijalankan ulang sesudah F07; APK lama di bagian atas bukan bukti memuat perubahan ini.

### Perubahan yang dilakukan pada putaran ini

- Domain: `TradingFacade.java`, `ResearchTechTreeService.java`, `TradingWebBridge.java`.
- UI/test: `android/app/src/main/assets/www/app.js`, `TechTreeAndHeadhuntingTahapS3Test.java`, `Work/verify-s3-techtree-headhunt.js`.
- Gate/test support: `run-s1-test.py` s.d. `run-s4-test.py`; kompatibilitas assertion JUnit `JSONArray.length()` di `LiquidityInsuranceAndBailoutTahapS1Test.java`.
- Perubahan lokal pengguna di file lain tetap dipertahankan.

### Status sesi checkpoint

F07 implementasi + targeted JVM/browser terverifikasi; full Gradle suite sedang dijalankan ulang setelah koreksi fixture test, fokus Gradle 43/43 lulus; seluruh regresi S1–S4 setelah perubahan lulus 29/29, build APK/paket belum diperbarui. Save pemain tidak digunakan; hash baseline tetap perlu dicek sebelum laporan final. Teruskan dari polling Gradle aktif dan parse XML final, lalu jalankan review/rebuild/paket serta lanjutkan item lain sesuai urutan aktif.

## Pembaruan verifikasi sesudah checkpoint (2026-10-23)

- F07 AUM save/load assertion sekarang dijalankan oleh `python Work/run-s3-test.py`; S3 lulus 5/5.
- Keempat targeted runners dijalankan ulang pada source terbaru: S1 11/11, S2 5/5, S3 5/5, S4 8/8 (**29/29**). `node --check android/app/src/main/assets/www/app.js`, `python -m py_compile Work/run-s1-test.py Work/run-s2-test.py Work/run-s3-test.py Work/run-s4-test.py`, simulasi propagation JUnit exit 1 untuk keempat runner, dan `git diff --check` lulus.
- Perbaikan fixture/regresi ditambahkan setelah suite awal 548/17: fixture stress menambah XP misi `TEST-IDX-LEVEL-4` hanya untuk tes non-karier yang memang memakai TLKM/industri; `CareerAccessTest` memakai IDX:UNVR (sektor yang terbuka di rank awal) sambil assert akses; tes OwnerAudit `skipDay` kini assert clock tetap berjalan sesuai AGENTS.md.
- Gradle targeted untuk empat class yang sebelumnya gagal lulus **43/43** dengan `--tests AllFeaturesExtreme10x100CriticalSequenceTest`, `MarketMatchingOrderHaltCriticalSequenceTest`, `CareerAccessTest`, dan `OwnerAuditRegressionTest`. Fokus report: 4 XML, 43 test, 0 failures. Ini bukti Gradle, bukan hanya runner JVM.
- Full Gradle `testDebugUnitTest` sempat dimulai ulang setelah fixture fix dengan `.socket-qa` scoped pada proses tes, lalu dihentikan atas instruksi pengguna sebelum selesai. Run itu **tidak menghasilkan hasil penuh** dan tidak boleh dianggap lulus; XML pada direktori build masih dapat berasal dari run sebelumnya.
- Run sebelumnya sebelum perbaikan fixture: Gradle benar-benar selesai **548 tests, 17 failures**. 15 BUY failures berasal dari gate sektor Level 4 pada fixture non-karier yang hanya memberi rank manajer; 1 gagal karena `CareerAccessTest` memakai TLKM level 4 saat rank awal; 1 karena assertion mengharapkan auto-pause pada `skipDay`. Tidak ada kegagalan dari F07.
- Save pribadi tetap tidak dibuka/digunakan untuk QA; pemeriksaan hash terakhir sama baseline `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`.

## M2A — Peta bukti 12 fitur (source, UI, SQLite, tes, status)

Kontrak yang dipakai untuk audit: seluruh fitur MVP wajib menurut `Docs/Product/02-PRD-Game-Saham-2D-Portrait.md`; mandat ekonomi/domain Java dan UI HTML/JS sebagai input/presentasi menurut `AGENTS.md`. Design Brief memetakan S19/S21/S22/S23/S24/S25/S27/S28/S31/S32/S33/S34/S35/S36 dan journey J08–J18, tetapi tidak menetapkan angka ekonomi atau membuktikan implementasi. Skema SQLite berisi tabel fitur; pencarian source menunjukkan `GameDatabaseHelper` ada, tetapi belum ditemukan repository/DAO pemakai tabel strategis dari runtime; state layanan ini terlihat melewati `TradingFacade.save()`/JSON. Karena itu “tabel ada” bukan “fitur tersimpan di SQLite”.

| Fitur | Jejak domain & UI saat ini | Tabel / persistensi yang diamati | Tes/bukti yang benar-benar ada | Audit/status acceptance |
|---|---|---|---|---|
| F01 Deposito | `TimeDepositManager`, `TradingFacade`, `TradingWebBridge`, `app.js` produk tenor/pencairan | DDL `TimeDeposits`; service state lewat save JSON | S1 11/11 terbaru: tenor/bonus, bunga, penalti, persistence | PASS targeted; SQLite runtime integration tidak terbukti |
| F02 Asuransi | `PortfolioInsuranceService`, trigger Black Swan, bridge/UI polis | DDL `PortfolioInsurancePolicy`; runtime JSON | S1 11/11 termasuk batas crash/coverage/persistence; browser S1 lulus cakupan pembelian + reject sehat | PASS targeted pada cakupan itu; gate Android/device tidak diuji |
| F03 Turnaround | preview insolvensi, lelang collection, pelunasan margin/restrukturisasi, receipt idempoten, UI preview/konfirmasi | Loan/collateral state JSON; bukan bukti settlement SQLite | S1 11/11: insolvensi, lelang, pelunasan/receipt sesuai test; browser reject akun sehat | PARTIAL: konsultan atas pinjaman installment nyata/jadwal dan konsekuensi terms belum acceptance-tested; 30 hari asumsi |
| F04 Holding | `CompanyManager` parent/subsidiary + internal ledger transfer, bridge, UI empat sektor/transfer | `HoldingSubsidiaries` DDL; holding disimpan via JSON | S2 5/5 + browser create group/transfer | PASS pada coverage tersebut; belum persistence SQLite |
| F05 Black Swan | `triggerMarketShockEvent`: broad price drop, cash snapshot/tetap, gold +5%, hitung gross/net exposure, insurance | Komoditas/state/portofolio JSON; schema statis | S4 method `testMarketShockAndSupplyChainCrisis`: kas tidak berubah, emas naik, eksposur emas offset gross loss; runner S4 8/8 | Efek kas/safe-haven kini terbukti pada satu test path; batas model: simulasi shock sederhana, bukan model liquidity/market depth, perlu review keseragaman dan save/load |
| F06 Supply chain | shock quotes + `CompanyManager.applySupplyChainCrisis` modifier biaya dan revenue bisnis | perusahaan/runtime JSON | test S4 membuktikan biaya manufaktur/ritel naik dan revenue energi naik; runner S4 8/8 | PARTIAL: belum bukti siklus settlement harian memakai cost/revenue modifiers dan posting ledger end-to-end |
| F07 Tech tree | Algo threshold sinyal; EFF diskon fee trading; SCALE +50% kontrak-derived AUM capacity; bridge & UI | `ResearchTechTreeProgress` DDL, state lewat JSON save/load | S3 5/5 termasuk cap reject/accept + save/load dan sinyal threshold; S4 8/8 matched BUY komisi; browser S3 sinyal/AUM display PASS | Targeted domain+UI PASS; baseline capacity adalah asumsi yang didokumentasikan; sinyal = sensitivitas, bukan akurasi prediktif; belum run full suite final |
| F08 Streak | `DailyStreakService`, sponsor reward multiplier/UI | `DailyStreakTracker` DDL; runtime JSON | S4 8/8: streak, reset/broken, reward, persistence | PASS targeted; kalender/offline boundary perlu review lanjut bila acceptance mensyaratkan lintas offline |
| F09 Dominasi | `SectorDominanceService`: strict >25%; fee policy di order/matching; status veto flag | `SectorDominanceStatus` DDL; runtime JSON | S4 8/8: 25% vs 26%, matched BUY fee discount, research fee stack | PARTIAL: hak veto belum mengubah keputusan/outcome kebijakan, meski flag status tersedia |
| F10 Headhunting | kandidat direkrut lewat `HeadhuntingService`; UI bursa staf | `StaffHeadhuntingMarket` DDL; runtime JSON | S3 5/5 workflow kandidat dan save/load; browser S3 rekrut Budi | PARTIAL: belum integrasi performa/AUM dari staf ke firma dan rival poaching staf pemain saat moral rendah |
| F11 Reksa Dana Kustom | `createCustomMutualFund` memvalidasi bobot total 100%, definition listing/UI | `CustomMutualFunds` DDL; definisi runtime JSON | S2 5/5 validasi dan persistence definisi | PARTIAL: tidak ada unit/NAB produktif, subscription client, AUM aktual, management/performance fee per custom product |
| F12 Kripto | BTC/ETH tersedia sebagai spot `CommodityInstrument` berlabel 24 jam; belum ada alur order khas yang terverifikasi | Tidak ditemukan persistence transaksi crypto khusus di tabel | S2 menguji katalog BTC/ETH; belum ada tes order 24 jam/no-suspension | PARTIAL: katalog/spot bukan bukti perdagangan 24 jam atau bebas halt saham |

### Ruang lingkup bukti M2A

- Targeted JVM runners terbaru: S1 11, S2 5, S3 5, S4 8 = **29/29**.
- Browser S1/S2/S3/S4 yang diklaim di STATUS harus dibaca sebagai cakupan terpisah; Browser S3 untuk signal/AUM baru dijalankan dan lulus. Browser bukan bukti APK, emulator atau perangkat.
- SQLite DDL memuat 8 tabel strategis dan 5 tambahan ekspansi (termasuk semua nama di atas); belum ditemukan penggunaan tabel-tabel itu dari jalur runtime produk. State aktual dilacak via JSON SaveState.
- Full Gradle pertama gagal 17/548; setelah koreksi fixture/kontrak `skipDay`, Gradle targeted 4 class lulus 43/43. Full Gradle rerun berikutnya dihentikan pengguna saat masih berjalan; hasil akhir tidak diketahui.
