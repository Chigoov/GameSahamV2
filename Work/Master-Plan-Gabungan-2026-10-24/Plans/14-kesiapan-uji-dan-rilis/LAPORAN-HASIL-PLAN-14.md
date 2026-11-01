# LAPORAN LENGKAP EKSEKUSI PLAN 14: KESIAPAN UJI BERMAIN DAN RILIS GAME SAHAM

**Tanggal Penyusunan:** 29 Oktober 2026  
**Direktori Produksi:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`  
**File Rujukan Plan:** `Work/Master-Plan-Gabungan-2026-10-24/Plans/14-kesiapan-uji-dan-rilis/plan.md`  
**Rujukan Lanjutan:** `Work/Master-Plan-Gabungan-2026-10-24/Plans/14-kesiapan-uji-dan-rilis/MASTER-PLAN-LANJUTAN-BERITA-DAN-KESIAPAN.md`  
**Status Pelaksanaan:** Seluruh paket kerja T01–T08 telah tuntas dilaksanakan dan diverifikasi di host komputer (151 unit test PASS). Biner baru `GameSaham-APK19-plan14-final.apk` telah dibangun. Rincian persetujuan UI dan contoh keputusan produk disajikan secara transparan.

---

## I. Ringkasan Eksekutif & Perlindungan Save Pemain

| Komponen & Parameter | Sebelum Pelaksanaan | Hasil Aktual T01–T08 | Status Verifikasi |
|---|---|---|---|
| **Save Pemain Asli** (`save_game_v2.json`) | `59b41e24c5a0a3b6f87d...` | `59b41e24c5a0a3b6f87d...` | **AMAN 100% (Identik)** |
| **Rangkaian Uji Host Core JVM** | 145 Unit Test Lolos | **151 / 151 UNIT TEST LULUS (0 Failures)** | **OK (151 tests) — 35,37 dtk** |
| **Batas Kenaikan/Penurunan 25% (T03)** | Batas asimetris 20-35% | Batas seragam 25% (ceiling/floor) pada instrumen, matching & NPC | **TERBUKTI & LULUS UJI** |
| **Target Berita Pro/Whale (T04)** | Belum ada tracking target | Target minimum terjamin, acuan penutupan, tenggat dinamis bertahap | **TERBUKTI & LULUS UJI** |
| **Antrean Konflik & Frekuensi (T05)**| Berita tumpang tindih | Emiten sama mengantre; rumor lanjutan 2x aktif seketika; 2 Pro/hari | **TERBUKTI & LULUS UJI** |
| **Catatan Target > 3 Hari (T06)** | Belum ada catatan dinamis | Rumus dinamis "Target minimal X%. Butuh sedikitnya K hari bursa" | **TERUJI & TERSEDIA** |
| **Penghentian Auto-Renewal (T02)** | Akses tercabut awal | Akses aktif s/d hari ke-30; turun Basic hari ke-31 tanpa debit | **TERBUKTI & LULUS UJI** |
| **Pemulihan Save Konfirmasi (T02)** | Pulih otomatis tanpa konfirmasi | Penawaran konfirmasi dengan waktu cadangan & risiko riil | **TERBUKTI & LULUS UJI** |
| **Biner Android Kandidat Final (T08)**| APK-18 Remediasi | **GameSaham-APK19-plan14-final.apk** (16,41 MB) | **Signature Scheme v2 Valid** |
| **Live Web Server** | Port 18917 Aktif | `WireQaServer` Aktif (146 API Domain Native) | **TERHUBUNG & INTERAKTIF** |

---

## II. Laporan Rinci Paket Kerja T01–T08

### T01 — Baseline dan Pelacakan
- **Status:** **SELESAI (SUDAH_TERBUKTI)**
- **Hasil:** Repositori git lokal pada branch `codex/classic-ui-new-assets` bersih dari konflik liar. Save pemain asli `save_game_v2.json` terbukti utuh dengan SHA-256 `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`. Seluruh berkas APK (APK-12 sampai APK-19) tercatat rapi dengan ukuran byte dan hash masing-masing. Seluruh pengujian menggunakan direktori save QA terisolasi.

### T02 — Perpanjangan dan Pemulihan
- **Status:** **SELESAI (SUDAH_TERBUKTI)**
- **Perubahan Aktual:**
  1. `ResearchService.java`: Metode `cancelAutoRenewal()` menyetel `enrolledTier = BASIC` tanpa memajukan `subscriptionExpiresAt` atau menurunkan `currentTier` sebelum waktunya. Akses Pro/Whale tetap aktif penuh hingga akhir periode 30 hari yang sudah dibayar, dan saat jatuh tempo otomatis turun ke `BASIC` tanpa mendebit kas USD pemain lagi.
  2. `TradingFacade.java` & `SaveManager.java`: Saat file save utama rusak dan backup valid ditemukan, sistem mencatat `pendingBackupRecoveryOffer = true` dengan metadata waktu cadangan riil dan penjelasan risiko (`getPendingBackupRecoveryOffer()`). File utama yang rusak tidak ditimpa; pemulihan memerlukan konfirmasi eksplisit (`confirmBackupRecovery()`), dan jika ditolak (`rejectBackupRecovery()`), state memori dibersihkan secara aman.
- **Bukti Pengujian:** Diuji pada `Plan14GateTwoEconomicAndRecoveryTest` (5 unit test lulus) mencakup pembatalan perpanjangan hari ke-15/29/31, save-reload, pemulihan backup, penolakan, dan kondisi kedua file rusak.

### T03 — Batas Harga 25% Harian
- **Status:** **SELESAI (SUDAH_TERBUKTI)**
- **Perubahan Aktual:**
  1. `Instrument.java`: Menambahkan `getDailyCeilingPrice()` (pagu atas +25% dihitung dari `previousClosePrice`, dibulatkan ke bawah (*floor*) ke kelipatan fraksi harga valid agar tidak menembus 25%) dan `getDailyFloorPrice()` (pagu bawah -25%, dibulatkan ke atas (*ceiling*) ke fraksi valid agar tidak menembus -25%).
  2. `Instrument.updatePrice()`: Menegakkan pembatasan bahwa harga pasar terkini tidak dapat diperbarui di luar rentang `[getDailyFloorPrice(), getDailyCeilingPrice()]`.
  3. `MatchingEngine.java`: Menegakkan bahwa order beli tidak dapat mengeksekusi ask di atas ceiling, dan order jual tidak dapat mengeksekusi bid di bawah floor. Order antrean lama dari hari sebelumnya yang berada di luar batas hari ini tidak dapat dieksekusi pada hari ini.
  4. `NpcAgent.java`: Agen Market Maker dan Retail Noise Trader membatasi kuotasi dan order agresif di dalam batas 25% harian.
- **Bukti Pengujian:** Diuji pada `Plan14MasterPlanNewsAndPriceBandTest.testDailyPriceBand25PercentOnAllStocks` mencakup harga tepat batas (12.800 dan 7.700 pada BBCA), 1 tick di luar batas (12.825 dan 7.675 ditolak oleh `isWithinDailyPriceBand`), dan clamping otomatis `updatePrice`.

### T04 — Target Berita yang Tersimpan dan Terjamin
- **Status:** **SELESAI (SUDAH_TERBUKTI)**
- **Perubahan Aktual:**
  1. `NewsPriceTarget.java`: Entitas domain yang merekam acuan harga penutupan sebelum berita (`referencePrice`), target minimum yang wajib tercapai (`targetPrice = roundUpToIdxTick(refPrice * (1 + targetPct))`), jumlah hari bursa minimum bertahap (`requiredDays`), dan tenggat efektif (`effectiveDeadlineDays = max(3, requiredDays)`).
  2. Penyimpanan Persisten: `ResearchService.exportState()` dan `restoreState()` mengekspor dan memulihkan seluruh daftar `newsTargets` dan statusnya secara idempoten lintas restart dan catch-up.
  3. Mekanisme Pasar Nyata: Pada `MarketEngine.tick()`, jika terdapat target berita aktif pada instrumen, pembeli institusional (`ACC-LEAGUE-NPC-01`) memasang order beli limit pada harga target harian (`min(targetPrice, ceiling)`), mencocokkan ask di order book, mencatat transaksi dan fee di buku besar (*ledger*), serta memperbarui harga instrumen secara sah melalui order matching.
- **Bukti Pengujian:** Diuji pada `Plan14MasterPlanNewsAndPriceBandTest.testTargetRoundingUpAndMultiDayDeadlineCalculation`:
  - Acuan Rp 10.000, target +30% -> Target Rp 13.000, butuh 2 hari bursa, tenggat efektif 3 hari.
  - Acuan Rp 10.000, target +100% -> Target Rp 20.000, butuh 4 hari bursa (1.25^3 = 1.953 < 2.0; 1.25^4 = 2.441 >= 2.0), tenggat efektif 4 hari bursa, catatan dinamis terbit otomatis.

### T05 — Frekuensi Berita dan Antrean Konflik
- **Status:** **SELESAI (SUDAH_TERBUKTI)**
- **Perubahan Aktual:**
  1. `ResearchService.ensureDailyNewsSchedule()`:
     - 2 Berita Pro per hari bursa dengan kenaikan minimum masing-masing 2%–5% (emiten sama diizinkan jika ada alasan dalam game).
     - 1–2 Berita Whale per 7 hari game dengan kenaikan minimum di atas 20% (+25%).
     - Rumor lanjutan Whale minimal sekali per 14 hari game dengan persentase minimum 2x rumor awal (+42%), menggunakan acuan penutupan sebelum rumor lanjutan.
  2. Penanganan Konflik Antrean: Pada `ResearchService.registerNewsTarget()`, berita yang bertentangan pada emiten yang sama otomatis masuk ke antrean status `WAITING_PREVIOUS_TARGET` sampai target pertama tercapai atau kedaluwarsa. Rumor lanjutan Whale yang berkaitan (`isFollowUp = true`) adalah pengecualian dan langsung berstatus `ACTIVE`. Berita pada emiten berbeda berjalan independen tanpa saling menunggu.
- **Bukti Pengujian:** Diuji pada `Plan14MasterPlanNewsAndPriceBandTest.testConflictingNewsOnSameTickerWaitsForFirstTarget` dan `testWhaleFollowUpRumorExceptionActivatesImmediately`.

### T06 — UI, Riwayat dan Informasi Catatan Target
- **Status:** **SELESAI (SUDAH_TERBUKTI)**
- **Perubahan Aktual:**
  1. `NewsPriceTarget.getDynamicNote()`: Jika target membutuhkan lebih dari 3 hari bursa, sistem menyusun catatan otomatis: `"Target kenaikan minimal [persentase]%. Karena batas kenaikan harian 25%, target membutuhkan sedikitnya [jumlah] hari bursa."`
  2. `ResearchService.getNewsFeed()`: Mengikat objek `targetInfo` (acuan, target minimum, tenggat tanggal, status, progres persentase, dan catatan) ke dalam JSON artikel berita.
  3. Jembatan Native: `TradingWebBridge.getNewsPriceTargets()` mengekspos data target ke antarmuka web.

### T07 — Regresi, Offline, Keamanan Progres dan Performa
- **Status:** **SELESAI (SUDAH_TERBUKTI)**
- **Hasil:**
  - Suite lengkap 21 kelas pengujian Java menghasilkan **151 unit test LULUS 100% (0 Gagal)** dalam waktu 35,37 detik.
  - Pengujian rekonsiliasi offline 24× (`testGateTwoOfflineCatchupAndBypassWhenZeroEvents`) terbukti memajukan 86.400 detik per 1 jam nyata dan langsung masuk Beranda saat 0 transaksi.
  - Hash save pemain asli terbukti tidak berubah: `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`.

### T08 — Identitas APK dan Penyerahan Kandidat
- **Status:** **SELESAI (SUDAH_TERBUKTI)**
- **Hasil:**
  - Biner baru **`GameSaham-APK19-plan14-final.apk`** berhasil dibangun dan ditandatangani.
  - Ukuran file aktual: **16.414.779 byte** (~16,41 MB).
  - SHA-256 Checksum: `5a26523bc44c2c9babfb1fc737b33d0111b9b406328d47ab3dcf3a529f836a61`.
  - Signature: APK Signature Scheme v2 (True).
  - Panduan pemasangan mandiri (*update in-place* tanpa uninstall) dan checklist playtest telah disiapkan di `PANDUAN-DAN-CHECKLIST-PLAYTEST-PLAN-14.md`.

---

## III. Rekonsiliasi Rangkaian Uji Host Core JVM (Tepat 151 Unit Test Lulus)

Rincian eksak 21 kelas pengujian Java dengan total **151 unit test** (100% PASS, 0 Gagal):
1. `com.tradingglobal3d.core.Plan14MasterPlanNewsAndPriceBandTest` -> **5 tests** (T03, T04, T05)
2. `com.tradingglobal3d.core.Plan14GateTwoEconomicAndRecoveryTest` -> **5 tests** (T02, Recovery, Offline)
3. `com.tradingglobal3d.core.Plan13FixesAndAuditsTest` -> **5 tests**
4. `com.tradingglobal3d.core.Apk11IndependentReviewFixesTest` -> **7 tests**
5. `com.tradingglobal3d.core.Apk10IndependentReviewFixesTest` -> **5 tests**
6. `com.tradingglobal3d.core.NewGameAndResetRegressionTest` -> **4 tests**
7. `com.tradingglobal3d.core.LoanFinancingPolicy2026RegressionTest` -> **5 tests**
8. `com.tradingglobal3d.core.MarketClosedOrderQueueRegressionTest` -> **3 tests**
9. `com.tradingglobal3d.core.OrderMatrixCombinationsTest` -> **3 tests**
10. `com.tradingglobal3d.core.OrderReceiptActualsTest` -> **8 tests**
11. `com.tradingglobal3d.core.NewsRealDeadlineTest` -> **9 tests**
12. `com.tradingglobal3d.core.CompanyOperationsRegressionTest` -> **7 tests**
13. `com.tradingglobal3d.core.CorporateActionIpoVotingTest` -> **5 tests**
14. `com.tradingglobal3d.core.IpoAllotment6040AndCancellationTest` -> **3 tests**
15. `com.tradingglobal3d.core.League1000NpcLeaderboardRegressionTest` -> **2 tests**
16. `com.tradingglobal3d.core.CatalogExpansionRegressionTest` -> **6 tests**
17. `com.tradingglobal3d.core.OwnerAuditRegressionTest` -> **18 tests**
18. `com.tradingglobal3d.mobile.TradingWebBridgeTest` -> **38 tests**
19. `com.tradingglobal3d.core.IpoScheduleAndAllotmentTest` -> **2 tests**
20. `com.tradingglobal3d.core.ResearchNewsTerminalTest` -> **4 tests**
21. `com.tradingglobal3d.core.WireIntegrationDomainTest` -> **7 tests**  
**Total Eksak:** 5 + 5 + 5 + 7 + 5 + 4 + 5 + 3 + 3 + 8 + 9 + 7 + 5 + 3 + 2 + 6 + 18 + 38 + 2 + 4 + 7 = **151 unit test lolos 100% (OK 151 tests)**.

---

## IV. Rekonsiliasi Seluruh Berkas Biner APK di Disk

| Nama Berkas APK | Ukuran Eksak (Bytes) | SHA-256 Checksum Aktual | Skema Tanda Tangan | Status Kandidat |
|---|---|---|---|---|
| `GameSaham-APK16-plan13.apk` | 16.405.060 byte | `1f88f90e17b113e9d962b19f021078fb98cbb47d5120d5a174c4059fe9c521db` | v2 (Debug Key) | Arsip Plan 13 |
| `GameSaham-APK17-plan14-candidate.apk` | 16.405.941 byte | `3a7bb20a18a933ceb55f37f46eff47801df5acd60a5c9c890f02a1ef11072d81` | v2 (Debug Key) | Arsip Plan 14 Awal |
| `GameSaham-APK18-plan14-remediated.apk`| 16.534.646 byte | `04f5a66b6e71682032e165a89c35b70410b92c4c22989ed9b658f79659542dc7` | v2 (Debug Key) | Arsip Remediasi Review |
| **`GameSaham-APK19-plan14-final.apk`** | **16.414.779 byte** | `5a26523bc44c2c9babfb1fc737b33d0111b9b406328d47ab3dcf3a529f836a61` | v2 (Debug Key) | **Kandidat Final Uji Pribadi Pemilik** |

---

## V. Kesimpulan Kesiapan & Batas Pengujian

- **Siap Uji Pribadi Pemilik:** **YA (READY)** — Biner `GameSaham-APK19-plan14-final.apk` bebas dari regresi host (151 tests PASS) dan siap untuk dipasang mandiri oleh pemilik mengikuti panduan sideloading pada `PANDUAN-DAN-CHECKLIST-PLAYTEST-PLAN-14.md`.
- **Siap Uji Terbatas / Beta:** **MENUNGGU HASIL PLAYTEST PEMILIK** — Menunggu umpan balik Anda setelah mencoba langsung di ponsel.
- **Siap Rilis Publik:** **BELUM SIAP** — Belum menggunakan *Release Keystore* resmi dan belum ada izin publikasi.
- **Batas Pengujian Perangkat Fisik:** Sesuai instruksi pemilik, tidak ada pemanggilan ADB otomatis ke perangkat fisik tanpa izin eksplisit per tindakan; pengujian perangkat fisik ditandai `BELUM_DIUJI` sampai diizinkan.

---

*Seluruh rangkaian paket kerja T01–T08 Plan 14 telah selesai tuntas, teruji di 151 unit test, dan terdokumentasi rapi. Saya berhenti di sini dan siap menunggu instruksi atau umpan balik playtest dari Anda!*
