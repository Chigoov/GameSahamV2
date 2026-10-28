# Baseline redesain 23 Oktober 2026

Addendum penutupan: baseline17FAIL di bawah tetap historis. Setelah izin perbaikan terbatas pemilik, integrasi akhir `Artifacts/Redesign-Antislop-2026-10-23/jvm-release/junit-latest.log` **491/491PASS**,66suite,280,432detik. Perubahan aturan hanya mengembalikan mandat aktif5menit/hari; tarif/hadiah/ekonomi lain dipertahankan. Migrasi valuasi kendaraan menjaga nilai save dan menolak manipulasi rumus; short/margin kini langsung tersimpan. Tes baru/yang diperkuat menutup reload langsung dan data snapshot kewajiban; tidak menurunkan assertion agar baseline menjadi hijau.

**FAIL: 66 suite, 488 tes, 471 lulus, 17 gagal dalam 9 suite; 252,094 detik; JUnit exit 1.** Baseline diambil dari HEAD `fc3cdd25c029cc229bf9a24240262930cb362de0` sebelum redesain presentasi. Source Java/domain/assertion tidak diubah. Native Android tetap NOT_TESTED. Angka gagal tidak boleh diubah menjadi PASS melalui penghapusan assertion.

Bukti: [log lengkap](../Artifacts/Redesign-Antislop-2026-10-23/baseline-jvm/junit-latest.log), [ringkasan](../Artifacts/Redesign-Antislop-2026-10-23/baseline-jvm/summary.json), [hash source](../Artifacts/Redesign-Antislop-2026-10-23/baseline-jvm/source-sha256.csv), [environment](../Artifacts/Redesign-Antislop-2026-10-23/baseline-jvm/environment.json), dan [10 trace extreme](../Artifacts/Redesign-Antislop-2026-10-23/baseline-jvm/test-cwd/Artifacts/Final-Verification-2026-10-21/action-traces).

## Runner yang diperbaiki

`Work/run-codex-jvm-check.ps1` sebelumnya gagal compile: `TradingWebBridge` merujuk `MainActivity`, tetapi source itu tidak ada dalam daftar compile. Runner kini memasukkan source asli MainActivity. Setiap eksekusi mempunyai `test-run-<guid>` di OutputDirectory, salinan input `assets/data`, dan tmp save sendiri. Trace relatif yang hardcoded pada tes ikut berada di sandbox. Tidak ada cleanup/save lama/stub/classes build lama. API `-Tests` dan `-OutputDirectory` dipertahankan; semua caller yang ditemukan adalah perintah pada prompt/checkpoint dan dokumentasi.

Jalankan dari root repo menggunakan output baru:

```powershell
& ./Work/run-codex-jvm-check.ps1 -OutputDirectory 'Artifacts/Redesign-Antislop-2026-10-23/runner-check'
```

Runner compile seluruh source/test dan discover seluruh `*Test.java` bila `-Tests` tidak diberikan. Uji ulang dengan runner yang diperbaiki selesai: **488 tes, 17 gagal**, sama dengan baseline. Kegagalan JUnit tetap menghasilkan exception/exit gagal. Bukti: [log verifikasi runner](../Artifacts/Redesign-Antislop-2026-10-23/runner-check/junit-latest.log). Subset WealthConsistencyAcceptanceTest lulus **5/5** dan pemeriksaan input/tmp sandbox lulus: [log subset](../Artifacts/Redesign-Antislop-2026-10-23/runner-isolation-check/junit-latest.log).

Pemeriksaan runnable minimum untuk isolasi dan subset yang lulus:

```powershell
$check = 'Artifacts/Redesign-Antislop-2026-10-23/runner-isolation-check'
& ./Work/run-codex-jvm-check.ps1 -Tests com.tradingglobal3d.core.WealthConsistencyAcceptanceTest -OutputDirectory $check
$run = @(Get-ChildItem -LiteralPath $check -Directory -Filter 'test-run-*')[-1]
if (!(Test-Path -LiteralPath "$($run.FullName)/src/main/assets/data/market_universe.json") -or !(Test-Path -LiteralPath "$($run.FullName)/tmp")) { throw 'Sandbox input/tmp tidak terbentuk' }
```

## Klasifikasi 17 kegagalan

Riwayat fixture di bawah adalah diagnosis awal, bukan persetujuan untuk mengubah tes atau menutup bug. Domain tidak diubah dalam mandat presentasi; konflik kontrak harus diputuskan dan diuji sebelum gate final PASS.

| No. | Suite/metode | Observasi | Klasifikasi awal |
|---:|---|---|---|
| 1 | TradingWebBridgeTest.testCorporateActionsIpoAndVotingViaBridge:147 | IPO-CLOUD vs IPO-2026-10-1 | Fixture ID IPO historis |
| 2 | TradingWebBridgeTest.testClockAndCareerSnapshotCanDriveBriefing:512 | speed2 diminta, snapshot1 | Bug domain terkonfirmasi: setClockState mengubah multiplier, getSnapshotJson hardcode1 |
| 3 | TradingWebBridgeTest.testMutualFundsSubscriptionRedemptionAndValuation:982 | jumlah3 vs24 | Assertion katalog sebelum ekspansi |
| 4 | WireIntegrationDomainTest.ipoRequestRetryPersistsAndListedHoldingCanReload:76 | order IPO-CLOUD gagal | Fixture ID IPO historis |
| 5 | OwnerAuditRegressionTest.readerBadgeIsReachableWithActualGameContent:287 | berita5 vs6 | Assertion jumlah konten historis; hasil badge belum teruji setelah assertion gagal |
| 6 | OwnerAuditRegressionTest.researchUsesGameTimeAndRetainsItsScheduleAfterReload:138 | berita3 vs4 | Assertion jadwal/jumlah historis; perlu kontrak terbaru |
| 7 | FeatureCompletionBusinessTest.collectionValuationIsAnchoredPartitionInvariantAndMigrates:163 | Vespa tidak terdepresiasi | Potensi regresi domain: katalog MOTOR, cabang depresiasi hanya VEHICLE; bukan sekadar mengganti expected |
| 8 | EndToEndGrandIntegrationTest.testFinancialSubsystemsWithUnlockedCareerFixture:134 | IPO-CLOUD tidak ditemukan | Fixture ID IPO historis |
| 9 | CorporateActionIpoVotingTest.testIpoSubscriptionAllotmentAndRefund:124 | IPO-CLOUD tidak ditemukan | Fixture ID IPO historis |
| 10 | CorporateActionIpoVotingTest.testCashDividendDistribution:78 | event2 vs3 | Assertion jumlah event historis; perlu rekonsiliasi event sah |
| 11 | CorporateActionIpoVotingCriticalSequenceTest.testCorporateSequence02 | Masa order IPO ditutup | Urutan fixture belum mengikuti jadwal IPO; invariant tetap wajib diuji |
| 12 | CorporateActionIpoVotingCriticalSequenceTest.testCorporateSequence04 | Masa order IPO ditutup | Konflik jadwal fixture, belum ditutup |
| 13 | CorporateActionIpoVotingCriticalSequenceTest.testCorporateSequence05 | Masa order IPO ditutup | Konflik jadwal fixture, belum ditutup |
| 14 | CorporateActionIpoVotingCriticalSequenceTest.testCorporateSequence08 | Masa order IPO ditutup | Konflik jadwal fixture, belum ditutup |
| 15 | CollectionAccountingRegressionTest.invalidOwnershipPayloadAndValuationCannotMutateRun:89 | valuasi0 diterima | Konflik kontrak: validatePrice eksplisit nonnegatif, tes meminta positif; pemeriksaan payload selanjutnya belum tercapai |
| 16 | CareerAccessTest.lockedCommandsCannotMutateMoneyAccountsOrXp:41 | akun28 vs68 | Assertion jumlah akun sebelum ekspansi fund/NPC |
| 17 | CareerAccessTest.realTradingRewardsAndSalaryReachClientManagementWithoutInjectedState:49 | order pertama belum fill | Fixture harga/likuiditas vs pipeline baru; progres karier end-to-end belum dibuktikan oleh tes ini |

`AllFeaturesExtreme10x100CriticalSequenceTest` menyelesaikan sepuluh seed dan progresi730hari. Hal itu tidak menutup empat failure corporate critical. Speed snapshot dapat diverifikasi di `TradingFacade.java:697/2071` dan `GameClock.setSpeedMultiplier`; kategori koleksi di `katalog_koleksi_150.json` serta `CollectibleItem.updateCalendarValuation`.

## Inventaris browser exact

Inventaris existing: **19 JS dan10 Python** yang memakai Playwright. `test-f03-negative-control.js` adalah self-check4 assertion tanpa browser; tidak dihitung sebagai bukti journey. `verify-redesign-screens.py` ditambahkan dalam sesi redesain ini dan dicatat terpisah.

Kode isolasi:

- **J**: salin JS unchanged ke `<sandbox>/Work`, jalankan langsung dengan `QA_PORT` ke host Java/save baru. Semua output `__dirname/../Artifacts` ikut sandbox; jangan jalankan wrapper lama. Memerlukan runtime/browser terpasang dan NODE_PATH proses bila diperlukan.
- **D**: pakai opsi URL/output existing langsung; tidak perlu salinan.
- **P**: `--output-dir` tersedia, host masih8784; QA_PORT tidak dibaca. Butuh host terisolasi pada8784 setelah port dipastikan aman, atau salinan artifact dengan hanya routing host/output/auto-start yang diubah dan diff dicatat.
- **H**: host/output hardcoded; __dirname/QA_PORT saja tidak cukup. Salinan artifact dan perubahan routing diperlukan, assertion gameplay dipertahankan.

| File di Work/ | Isolasi | Catatan |
|---|---|---|
| verify-portrait-ui.js | D | --url/--port/--output-dir; QA_PORT/UI_VERIFY_OUT_DIR |
| verify-portrait-flows.js | H | port8767 hardcoded; __dirname hanya mengisolasi output |
| verify-a11-market-redesign.js | J | QA_PORT; output relatif |
| verify-a2-a8-a10.js | J | QA_PORT; output relatif |
| verify-a9-business.js | J | QA_PORT; output relatif |
| verify-benchmark-performance.js | J | QA_PORT; output relatif; benchmark30sampel |
| verify-collections-a1-a3.js | J | QA_PORT; output relatif |
| verify-f05-watchlist-touch.js | J | QA_PORT; output relatif |
| verify-grand-journey-e2e.js | J | QA_PORT; output relatif |
| verify-int-viewports.js | J | QA_PORT; output relatif |
| verify-r01-r02-clock-sponsor.js | J | QA_PORT; output relatif |
| verify-r03-r04-r09.js | J | QA_PORT; output relatif |
| verify-r06-ipo.js | J | QA_PORT; output relatif |
| verify-r07-r08-fundamentals.js | J | QA_PORT; output relatif |
| verify-r10-multiasset.js | J | QA_PORT; output relatif |
| verify-r11-profile-tabs.js | J | QA_PORT; output relatif |
| verify-r12-news.js | J | QA_PORT; output relatif |
| verify-r13-collections.js | J | QA_PORT; output relatif; assertion visual lama perlu triage |
| verify-r14-funds.js | J | QA_PORT; output relatif |
| verify-portrait-ui.py | D | --url dan --output |
| verify-portrait-flows.py | H | port8767, output relatif cwd |
| verify-wired-features-2026-10-21.py | H | port8784, OUT relatif __file__; opsi inspect/matrix/journeys/flow-matrix |
| test-live-sync-browser.py | P | Tidak auto-start host |
| test-owner-revisions-2026-10-22.py | P | Auto-start WireQaStart.py jika pemeriksaan netstat/grep kosong |
| test-e2e-playtest-journey.py | P | Auto-start host lama |
| test-p4-viewports-accessibility.py | P | Auto-start host lama |
| test-sustainability-and-storage.py | P + adaptasi | Juga invoke Gradle pada ROOT dan menulis build; output-dir saja belum mengisolasi seluruh efek |
| benchmark-performance-final.py | P | Auto-start host lama |
| test-a1-a3-collections-browser.py | H | Compile/start host8794 sendiri, classes/save/output historis; tidak ada unlink pada skrip ini |
| test-f03-negative-control.js | Langsung | Bukan tes browser atau bukti false-pass mustahil |
| verify-redesign-screens.py | D, baru | --url/--output/--label; screenshot audit saja, bukan journey |

Wrapper di bawah tidak aman dijalankan unchanged terhadap mandat isolasi. Enam wrapper R/INT mempunyai absolute root literal; menyalinnya saja tidak memindahkan root. Wrapper lain memakai __file__, tetapi perlu source/assets/classes dalam sandbox dan selalu memaksa port8794/QA_PORT8794. Lebih kecil menjalankan target JS langsung dengan cara J.

| Wrapper di Work/ | Target | Salin wrapper saja + QA_PORT cukup? | Efek existing |
|---|---|---|---|
| run-grand-journey.py | verify-grand-journey-e2e.js | Tidak | unlink isi QA save; port/classes/output tetap |
| run-benchmark.py | verify-benchmark-performance.js | Tidak | resume QA save tetap; port/classes/output tetap |
| run-verify-a11-market.py | verify-a11-market-redesign.js | Tidak | unlink QA save |
| run-verify-a2-a8-a10.py | verify-a2-a8-a10.js | Tidak | unlink QA save; compile subset domain |
| run-verify-a9-business.py | verify-a9-business.js | Tidak | unlink QA save |
| run-verify-collections-browser.py | verify-collections-a1-a3.js | Tidak | unlink QA save; compile subset |
| run-verify-int.py | verify-int-viewports.js | Tidak | root absolute; unlink QA save |
| run-verify-r06.py | verify-r06-ipo.js | Tidak | root absolute; unlink QA save |
| run-verify-r10.py | verify-r10-multiasset.js | Tidak | root absolute; unlink QA save |
| run-verify-r12.py | verify-r12-news.js | Tidak | root absolute; unlink QA save |
| run-verify-r13.py | verify-r13-collections.js | Tidak | root absolute; unlink QA save |
| run-verify-r14.py | verify-r14-funds.js | Tidak | root absolute; unlink QA save |
| WireQaStart.py | WireQaServer | Tidak | classes/output/save historis, dapat copy classes build lama, default8784 |

Untuk semua suite mutatif gunakan save baru per suite dan eksekusi serial. Periksa `/qa-meta`: `testFixture=true`, `nativeHost=false`. Daftar ini adalah inventaris, bukan klaim semua tes browser telah dijalankan atau lulus. Perubahan empat tab boleh memerlukan pemetaan selector UI; assertion perilaku/domain tidak boleh dihapus untuk menghasilkan PASS.

## Handoff cleanup presentasi app.js

Scope tambahan koordinator: hanya `android/app/src/main/assets/www/app.js`, tanpa Java, dependency, reset save, atau commit. Dead MockBridge beserta saldo/harga/chart contoh dihapus; missing bridge tetap menampilkan state terputus. Emoji dekoratif dan em dash authored dibuang. Arrow transaksi sebelum/sesudah dan arah konversi dipertahankan karena menyampaikan perubahan uang. Badge, kartu, subnav, profil, koleksi, misi, dan kepemilikan menggunakan class/token CSS bersama; tidak ada lagi gradient/rgba/glow inline.

Perubahan yang memengaruhi pembuktian: progres misi default40%, hadiah fallbackRp2,5juta, free-float40%, payout35%, return0%, profil saldo0 dan provenance FY2026/Bursa yang tidak didukung data dihapus. Nilai absen diberi teks belum tersedia. Summary liga pada riset dihapus karena liga berada diKarier. Prestige tetap diKarier; parameter dan callback finansial tetap menuju bridge asli. Nama perusahaan/pemain, blocker prestige dan kartu berita menggunakan textContent; URL profil dibatasi http/https sebelum menjadi link. `flowButton` tetap `button.onclick = action`, sehingga event.currentTarget untuk submit tidak hilang.

Filter koleksi mengambil distinct category dan jumlah dari response `getCollections`, memakai satu peta label Indonesia. Ini menutup filter legacy VEHICLE yang tidak cocok kategori MOTOR/MOBIL katalog150. Jumlah/kategori tidak dibuat dari angka tetap. Scroll tab memakai `.app-scroll`; fokus dan close layer dipertahankan. Polling tick dibungkus catch: error bridge/JSON menjadi peringatan terlihat, tidak menjadi pageerror; interval, elapsed clock dan domain tidak diubah.

Validasi source: `node --check android/app/src/main/assets/www/app.js` PASS; `git diff --check` PASS. Scan source tidak menemukan MockBridge, emoji dekoratif, em dash authored, gradient, atau rgba. Hash saat handoff `29b307168034ebe7047acb4353cadbd2474792acb89aca99889a569fea5c8fba`. Ini belum meluluskan browser/visual: koordinator menjalankan ulang journey dan matriks pada source stabil, termasuk filter seluruh kategori dan respons offline. Baseline JVM tetap488/17 tanpa rerun tambahan setelah perbaikan runner.
