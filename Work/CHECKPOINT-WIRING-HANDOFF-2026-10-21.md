# Checkpoint pergantian agen — integrasi tombol, layar, dan save

Tanggal: 21 Oktober 2026, Asia/Jakarta, sekitar 07:28 WIB. **Status: PARTIAL / HANDOFF REQUESTED BY OWNER.** Ini checkpoint lanjutan; jangan memakai status checkpoint 07:06 sebagai source terakhir.

Pemilik meminta pekerjaan dilanjutkan oleh model/agen berikutnya dan meminta prompt lengkap serta checkpoint. Koordinator menghentikan penambahan fitur baru pada batas aman, mempertahankan semua perubahan lokal, dan menyiapkan serah-terima ini. Status produk bukan COMPLETE.

## 1. Root, Git dan perlindungan data

- Root produksi: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`.
- Checkout `C:/Users/HYPE AMD/Downloads/VIBE CODING/Game Trading Saham` adalah repo referensi berbeda. Jangan ditulis/dijadikan checkout aktif.
- Branch: `codex/owner-audit-portrait-v8`.
- HEAD saat checkpoint: `7dd0526dd1f9cc0ba7730de3025c54f69f6b2d6d`.
- Banyak tracked changes dan untracked source/test/helper/doc masih lokal. Koordinator tidak membuat commit baru atau push pada putaran wiring ini. Jangan reset/clean/checkout paksa atau menganggap HEAD mencakup seluruh WIP.
- Save pribadi: `Artifacts/LocalPreview/preview-save/save_game_v2.json`.
- SHA-256 sebelum/sesudah yang diperiksa: `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed` — tidak berubah.
- Semua pengujian memakai save QA terisolasi. Tidak ada ADB, emulator, instalasi, publikasi, pembelian layanan atau perubahan konfigurasi global agen.
- Snapshot status/diff/fingerprint berada di `Artifacts/Wire-Handoff-2026-10-21/`. Perubahan source tetap berada di working tree, bukan hanya di dokumen ini.

## 2. Pekerjaan domain/bridge yang sudah ditulis

`TradingFacade.java`, `TradingWebBridge.java` dan `WireIntegrationDomainTest.java`:

1. `researchQuery(JSON)`: `kind=inbox|consensus|alternative|comparison`, `instrumentId` atau `instrumentIds`. Tier dan published-time ditegakkan domain.
2. `corporateOfferAction(JSON)`: `action=list|preview|participate|cancel`. List berisi `offers`; setiap offer mempunyai `participations` dan alias `requests`. Participate memakai `offerId`, `shares`, `requestId`; cancel memakai requestId partisipasi.
3. `strategyLab(JSON)`: `action=list|create|history|trade|reset|replay`. Facade memiliki labs, tersimpan sebagai runtime `strategyLabs`; list mengembalikan `sandboxes`, history `state`/`decisions`. State mencakup kas, posisi, trade count dan realized P/L. Wrapper lama memakai service persisten ini.
4. IPO bridge mewajibkan requestId stabil; listing membuat instrumen yang tervalidasi dan dapat disimpan/dimuat. New game membersihkan metadata listing lama.
5. Voting mempunyai entitlement record-date, deadline, result, consequence dan canVote. Penjadwalan corporate memakai game time; split ledger overload menjaga cash-in-lieu, membatalkan resting/trigger orders dan menunda saat short terbuka. Tender juga melindungi saham tersimpan.
6. Akun internal NPC/pool/issuer ditolak pada API pemilih akun/order/FX/portfolio/ledger/fund plan. Kekayaan tidak boleh memasukkan akun tersebut.
7. Profile fill yang sudah direkam dilewati sebelum konversi ulang FX; receipt multi-aset ikut statistik dengan dedup.
8. Multi-aset memperbarui valuasi dalam pipeline dan mengembalikan `quantity`, `marketValue`, `positionId`, `unitPriceRaw`.
9. Replay: validasi modal; epoch dataset sebelum init; target mode divalidasi pada facade/katalog terpisah sebelum switch; labs per mode terisolasi; quote spread tidak churn tiap menit; trigger/simbol/sesi dibatasi; indeks lain/riset/corporate sintetis disembunyikan.
10. Akhir dataset menghentikan waktu pada close terakhir. Skip yang menyentuh akhir dapat mengembalikan `success:false,status:DATASET_COMPLETE,gameSeconds:<86400`; jangan mengaku maju satu hari penuh.
11. Prevalidation load memeriksa labs, collateral dan fund bindings memakai salinan akun/ledger agar gagal validasi tidak mengubah state aktif.

## 3. Pekerjaan UI yang sudah ditulis

`android/app/src/main/assets/www/app.js` dan `style.css`:

- Shared `bridgeResult`, formatter nominal, review/confirmation command dengan requestId stabil.
- Arah transfer modal sudah menggunakan `TO_COMPANY`/`TO_PERSONAL`, bukan perbandingan salah `INJECT`.
- Upgrade berbayar, policy review, transfer review, operating report dan pintasan pinjaman modal kerja perusahaan.
- Terminal riset dengan inbox, consensus, comparison dan alternative data, pilihan instrumen dari domain, empty/locked/error/source/asOf states.
- Laporan hasil investasi weekly/run, noninvestment flows, return availability dan weekly history.
- Rights/tender list/detail/preview/participate/cancel dan status hak/tenggat; voting deadline/entitlement/result/consequence.
- Lab persisten: daftar sesi, create, trade, history, reset, decision replay. ID lab usang dibuang ketika mode baru tidak memilikinya.
- Replay catalog dari `getHistoricalDatasets()`, sumber/resolusi/coverage domain, persetujuan mengganti replay slot, resume replay dan kembali internal. Klaim dataset IDX/Yahoo hardcoded lama dihapus dari flow ini.
- Pilihan leaderboard kekayaan/return, nullable ranking, rival dan akses riwayat hasil. Nama NPC/player dirender memakai textContent, bukan HTML interpolasi.
- Profil/preferensi memiliki label form terkait, kontras tinggi/reduced motion, validasi respons dan persistensi. Preferensi diterapkan ketika run dimuat; tombol ukuran teks pengaturan menyimpan lewat domain.
- Loan confirmation memakai requestId stabil per review dan menolak perubahan input sebelum confirm. Error tidak menghapus tombol retry. Agunan membaca wrapper `{collections:[]}` yang benar, memfilter owner personal; produk perusahaan mempunyai pemilih perusahaan.

Perubahan UI paling akhir (pemilih perusahaan/agunan dan beberapa error/availability guards) belum memperoleh seluruh pengujian browser ulang sebelum pemilik meminta handoff. Jangan menyatakan semua layar/state lulus.

## 4. Root-cause fixes reviewer yang sudah ditulis

- `CorporateActionService`: split/reverse split mempertahankan actual acquisition-fee balance/quantity dan gross realized P/L; alokasi fraksi cash-in-lieu serta overflow prevalidation.
- `Position`: return saham overflow-safe dan kompatibel save legacy yang tidak menyimpan fee quantity saham dipinjam, tanpa fee fiktif.
- `MultiAssetTradingService`: actual buy fee pemain masuk basis; dealer tidak menerima basis fee palsu. Sale receipt menyimpan realizedNetPnL setelah actual allocated fee dan fee jual.
- Lima `PositionAcquisitionFeeRegressionTest` dan penyesuaian fixture corporate voting/split mengikuti canonical clock/catalog. Test multi-aset membuktikan round-trip emas rugi fee Rp5.800 dan fee basis habis.

## 5. Bukti nyata yang tersedia

| Bukti | Hasil dan batas |
|---|---|
| `Artifacts/Wire-Domain-2026-10-21/junit-latest.log` | Compile core+bridge+tests; **20 tes terarah PASS**, termasuk 7 WireIntegrationDomainTest. Bukan full suite/APK/native. |
| `Artifacts/Wire-Review-2026-10-21/domain-final/junit-latest.log` | Compile source dan **31 tes terarah PASS** untuk fee/multi-aset/corporate/replay/lab. Ada overlap; jangan dijumlah sebagai 51 tes unik. |
| `Artifacts/Wire-Integration-2026-10-21/journey-result.json` | **24 pemeriksaan journey bernama PASS**, pageerrors 0 pada snapshot pengujian. Bukan seluruh produk atau semua positive/negative branch. |
| `Artifacts/Wire-Integration-2026-10-21/root-matrix.json` | **70 root samples** = 5 tab × 7 viewport × normal/200%; 0 error geometrik pada sampel itu. Bukan matriks seluruh nested screen/state. |
| `Artifacts/Wire-Integration-2026-10-21/dom-inventory.json` | Inventaris DOM/label host QA nyata, tanpa error halaman saat discovery. |
| `Artifacts/Wire-Integration-2026-10-21/compile/junit-latest.log` | Baseline **35 bridge tests, 2 gagal** pada legacy loan/corporate fixtures. Belum diluluskan ulang; bukti kegagalan dipertahankan. |
| `Artifacts/Wire-Integration-2026-10-21/flow-matrix.json` | **INVALID_HARNESS_NOT_PRODUCT_FAILURE**. Matriks 266 detail samples terhenti karena helper tab tertimpa variabel loop. Script telah diperbaiki, tetapi belum dijalankan ulang setelah STOP. Tidak boleh dihitung PASS atau bug produk. |
| `Artifacts/Wire-Integration-2026-10-21/QA-HANDOFF.json` | Ringkasan final QA, ruang lingkup bukti, batas pengujian dan status proses. |

Journey browser yang sudah diamati meliputi company create dan upgrade review/confirm receipt; pembelian koleksi → pemilihan agunan → quote pinjaman → pencairan → prepay → pelepasan agunan → save/load; terminal inbox dan premium locked; laporan performa; offer/detail locked; lab trade/replay/save-load tanpa mutasi primary; replay consent/start/return dengan primary slot semantic tetap. Form terbuka atau locked bukan bukti successful financial settlement. Jangan mengklaim premium paid, tender successful, pinjaman perusahaan atau semua nested states sudah diuji dari bukti tersebut. Keberhasilan personal loan bukan bukti seluruh skenario late/restructure/stale/save-failure lulus.

## 6. Masalah dan pekerjaan lanjutan yang belum ditutup

### P0/P1 — periksa pertama

1. **FX timestamps:** quote TTL memakai waktu nyata; settlement ledger masih perlu timestamp game terpisah. Overload belum dikonfirmasi dibuat. Hubungkan hanya setelah membaca FxExchangeService dan caller; jangan membuat quote langsung expired karena epoch game. Tambahkan test expiry/idempotency/ledger epoch/return.
2. **Dua baseline bridge tests gagal:** `testInstallmentLoansDisbursementPaymentAndPrepay` dan `testCorporateActionsIpoAndVotingViaBridge`. Reproduksi source terbaru. Fixture harus memakai quote+agunan, IPO requestId dan voting game window; jangan membuka kembali bypass produksi atau melemahkan assertions.
3. **Perubahan UI paling akhir belum diuji ulang lengkap:** pastikan kedua arah modal benar, company selector memilih owner yang dimaksud, collection wrapper mengisi agunan, quote invalidation/retry tidak menggandakan loan, preferences bertahan reload/mode, availability/cancellation benar.
4. **IPO UI payload:** bridge sekarang mewajibkan requestId. Periksa openIpoCenter masih membuat payload tanpa ID; bila ada, buat ID stabil per reviewed request dan uji reserve/retry/listing/refund.
5. **Lengkapi tampilan holdings multi-aset:** API quantity/marketValue sudah ada; kartu UI mungkin baru menampilkan katalog serta tombol buy/sell. Pastikan unit dimiliki, nilai, kupon/maturity, receipt dan wealth bisa diperiksa.

### Verifikasi integrasi dan syarat penerimaan berikutnya

- Paid terminal success/expiry, successful rights/tender/cancel/settlement/vote/IPO dan loan personal/company harus diuji lewat UI, bukan hanya domain tests atau locked screens.
- Selesaikan pemeriksaan end-of-dataset UI, skip partial status, mode resume failed-target/save failure, profile/fee/FX/period reconciliation, tender/split dengan resting orders/shorts.
- Lanjutkan full regression, seluruh nested route/state ×7 viewport×2scale, 10×100 critical sequences dan performa 30 sampel/jenis sesuai standar. Root 70 samples tidak meluluskan semua gate itu.
- Build Android/MainActivity, APK/package/signature/hash/source correspondence belum dijalankan untuk WIP ini. APK 815303 byte adalah baseline lama, bukan hasil integrasi saat ini.
- Manifest dataset global serta isi paket legacy belum dituntaskan. Loader reject tidak otomatis mengeluarkan CSV hak tidak terverifikasi dari APK; reconcile provenance dan quarantine/exclude packaging sesuai aturan.
- STATUS.json/REQUIREMENTS/SCREEN-COVERAGE/GAP-MATRIX belum disinkronkan ke bukti baru; tetap PARTIAL, bukan semua PASS.

## 7. Menjalankan ulang pemeriksaan dengan aman

Dari root produksi, PowerShell:

```powershell
node --check android/app/src/main/assets/www/app.js
./Work/run-codex-jvm-check.ps1 -Tests com.tradingglobal3d.core.WireIntegrationDomainTest,com.tradingglobal3d.core.PositionAcquisitionFeeRegressionTest,com.tradingglobal3d.core.FeatureCompletionCorporateTest,com.tradingglobal3d.core.FeatureCompletionResearchTest,com.tradingglobal3d.core.FeatureCompletionLabTest,com.tradingglobal3d.core.FeatureCompletionMultiAssetTest -OutputDirectory Artifacts/Wire-Resume-2026-10-21/targeted
./Work/run-codex-jvm-check.ps1 -OutputDirectory Artifacts/Wire-Resume-2026-10-21/full
```

Jangan menimpa output lama. Untuk QA host, baca helper dan guard dahulu, lalu compile production classes ke output yang diharapkan helper:

```powershell
./Work/run-codex-jvm-check.ps1 -Tests com.tradingglobal3d.core.WireIntegrationDomainTest -OutputDirectory Artifacts/Wire-Integration-2026-10-21/compile
python Work/WireQaStart.py
```

Host QA ini **127.0.0.1:8784**, save `Artifacts/Wire-Integration-2026-10-21/qa-save`, platform `testFixture:true`, label QA HOST JAVA. Tidak memakai preview pribadi 8766. Jalankan browser runner di terminal berbeda setelah host siap:

```powershell
python Work/verify-wired-features-2026-10-21.py --inspect
python Work/verify-wired-features-2026-10-21.py --journeys
python Work/verify-wired-features-2026-10-21.py --matrix
```

Runner dapat mereset/memutasi QA. Pilih direktori bukti baru/backup bukti jika perlu karena script existing mempunyai output tetap. Jangan memindahkan guard ke slot personal demi memudahkan test. Python sistem sudah mempunyai Playwright/Chromium yang berhasil dipakai; jangan menginstal ulang tanpa memeriksa runtime tersebut.

Untuk build Android, gunakan JBR existing dan workaround socket lokal bila error yang sama muncul:

```powershell
$env:JAVA_HOME='C:/Program Files/Android/Android Studio/jbr'
$env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=C:/gradle-socket-fallback-do-not-create'
./android/gradlew.bat -p android --no-daemon testDebugUnitTest assembleDebug --console=plain
```

Ini perubahan environment proses, bukan konfigurasi global. Tidak memerlukan ADB/instalasi.

## 8. Status proses ketika handoff

Ketiga worker telah mengirim final. Worker domain dan reviewer menyatakan tidak mempunyai pekerjaan aktif. QA menghentikan host port8784 PID21588 melalui session66279; pemeriksaan berikutnya tidak menemukan listener/PID tersebut dan seluruh browser Playwright telah ditutup. Snapshot pemeriksaan koordinator dicatat pada `Artifacts/Wire-Handoff-2026-10-21/process-state.json`. Jangan menganggap lock/log sebagai proses hidup atau mati: periksa PID/command/port aktual sebelum menjalankan host baru.

Tidak ada worker yang diberi mandat untuk terus mengedit setelah handoff. Agen penerus wajib memeriksa proses aktual karena command yang sebelumnya diinterupsi dapat tetap hidup.

## 9. Instruksi mulai berikutnya

Baca `Prompts/Resume-Wiring-Longtask-2026-10-21.md` dan sumber keputusan yang dirujuk. Mulai dari pemeriksaan FX/fixture/API UI di atas, kompilasi ulang snapshot aktual, tutup integrasi dan test seluruh alur, baru buat APK/commit lokal/laporan. Jangan mendesain ulang atau membangun ulang game dari kosong.
