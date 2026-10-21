# Prompt eksekusi lengkap — lanjutkan integrasi UI/save sampai pengujian dan APK

Saya pemilik proyek. Lanjutkan pekerjaan existing secara otonom. Saya meminta fitur yang belum masuk dilengkapi, seluruh fitur disambungkan ke tombol/layar/domain/save, kemudian langsung dilakukan pengujian menyeluruh dan perbaikan temuan. **Jangan berhenti pada rencana atau meminta saya mengulangi keputusan yang sudah tertulis.** Gunakan multi-agent bila kemampuan tersebut tersedia, dengan satu koordinator integrasi.

## A. Root, keadaan awal dan batas wewenang

Root produksi:

`C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`

Repo `C:/Users/HYPE AMD/Downloads/VIBE CODING/Game Trading Saham` adalah referensi historis berbeda. Jangan menulisnya, memakai source/save/build-nya sebagai produksi, atau rollback ke source lama.

Checkpoint terbaru dan paling penting:

`Work/CHECKPOINT-WIRING-HANDOFF-2026-10-21.md`

Jangan mulai dari checkpoint 07:06 saja karena setelah itu banyak UI/domain sudah disambungkan. Baseline saat handoff sekitar 07:28 WIB: branch `codex/owner-audit-portrait-v8`, HEAD `7dd0526dd1f9cc0ba7730de3025c54f69f6b2d6d`, banyak dirty changes dan source/test baru. Ini bukan target reset. Buktikan cwd/Git root/HEAD/diff dan proses aktual sebelum edit.

Pertahankan seluruh local changes. Dilarang reset/clean/checkout paksa, menghapus kerja agen sebelumnya atau menimpa agar hash dokumen lama cocok. Gunakan snapshot `Artifacts/Wire-Handoff-2026-10-21/` untuk membandingkan drift secara sadar.

Save pribadi yang wajib dipertahankan:

`Artifacts/LocalPreview/preview-save/save_game_v2.json`

SHA-256 terakhir: `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`.

Jangan menjalankan test/reset/prestige/catch-up/mode switch pada save pribadi atau preview 8766. Semua pengujian memakai save QA terpisah. Hash berbeda harus diinspeksi, bukan ditimpa diam-diam. Pertahankan appID `com.tradingglobal3d.mobile` dan signing identity; perubahan save/schema memerlukan backup/recovery/migrasi yang teruji.

Diizinkan: implementasi repo, pengujian terisolasi, browser lokal, build, review, commit lokal selektif dan artefak/laporan. Jangan push/publish, mengirim pesan pihak lain, membayar layanan, mengubah global profil/model/provider/credentials, membuat scheduler/global launcher, atau menyentuh repo/data lain. **Tidak menjalankan ADB, emulator, instalasi atau mengoperasikan HP pada tugas ini.** Gate native tetap PENDING terpisah.

## B. Sumber keputusan yang wajib dibaca

1. `AGENTS.md` root produksi.
2. `Work/CHECKPOINT-WIRING-HANDOFF-2026-10-21.md` dan bukti/snapshot `Artifacts/Wire-Handoff-2026-10-21/`.
3. `Prompts/Longtask-Penuntasan-Fitur-Pengujian-2026-10-21.md` — mandat lengkap 15 bagian, seluruh fitur/14 fase/invariant/journey/gate. **Prompt resume ini menambah kondisi terbaru; tidak mengganti atau memperkecil scope dokumen lengkap itu.**
4. `Work/Hermes-Feature-Completion-2026-10-20/SPECIFICATION.md`, `GAP-MATRIX.csv`, `README.md` dan `MATERIALS.json`.
5. `Prompts/Hermes-Final-UI-Gameplay-Multi-Agent-2026-10-20.md` dan `Work/Hermes-Handoff-2026-10-20/README.md`, `OWNER-DECISIONS.json`, `PHASE-QUALITY-STANDARDS.md`, `REVIEW-AND-REGRESSION.md`, `FEATURE-ACCEPTANCE.csv`, `TIME-ACCEPTANCE.csv`.
6. Dokumen konsep/PRD/design brief pada `Docs/Product/` dan `Docs/Design/`, `Work/README.md`, TASK 00–13, STATUS/REQUIREMENTS/SCREEN-COVERAGE/OWNER-UI-LOGIC-MATRIX/DECISIONS/ACCEPTANCE.
7. Screenshot/REA/acuan HP existing, audit independen tanggal 20–21 Oktober dan bukti domain/browser terbaru. Baca dokumen historical sesuai konteks; source dan keputusan terbaru mengungguli klaim status lama.

Jika path hilang, cari di root ini; jangan mengarang isi. Seed 277 baris dan delta 148 baris mengandung parent/acceptance, bukan jumlah fitur unik atau persentase selesai. Petakan seluruh ID ke satu tracker operasional; jangan menghapus requirement sulit.

## C. Keputusan produk yang tidak perlu ditanyakan kembali

- Java + native Android WebView + HTML/CSS/JS lokal. Domain Java satu pemilik ekonomi, waktu dan save; JS hanya input/presentasi. Tidak kembali ke Unity/MAUI/3D/landscape atau menambah framework spekulatif.
- Portrait, layout/hierarchy aplikasi referensi HP, konten boleh scroll, navigasi bawah tetap: Beranda/Pasar/Portofolio/Bisnis/Lainnya. Graphite/amber/teal dan token/font shared; total kekayaan kanan atas; karier/XP/badge konsisten. Hindari teks implementasi panjang dan tombol semu.
- Aktif normal: 24 jam game = 5 menit nyata; 2×/5× hanya foreground. Background/ditutup: 24 jam game = 1 jam nyata, termasuk biaya/risiko, tidak mewarisi speed aktif dan tidak auto-pause. Skip tepat24jam lewat pipeline yang sama, peringatan sebelum dan laporan setelah; enddataset exception harus jujur.
- Offline, uang game, tanpa broker/cloud/iklan/layanan wajib berbayar. Nama NPC fiktif, tidak mengklaim online. Karier membuka fitur; XP/badge tidak mencetak kas atau mengganti syarat jabatan.
- Game tanpa tamat; prestige opsional dengan preview/backup/confirmation; pemulihan melalui pekerjaan/restrukturisasi, tidak ada rescuecash otomatis. Perusahaan berjalan menurut kebijakan; keputusan besar manual. Progres seimbang sesi15–30menit.
- Saham Indonesia/AS, indeks dari data anggota game, FX, firma/klien, perusahaan/staff, koleksi, tiga reksa dana unit/NAB, loan tenor/agunan, corporate/IPO/tender/vote, riset premium, liga/rival/profile/archetype, lab/replay keputusan/historis dan seluruh FR01–20 tetap wajib sesuai spesifikasi.
- Aturan simulasi yang belum ditentukan boleh diputuskan konsisten, didokumentasikan/rulesVersion/dites; bukan alasan menghapus scope. Dataset harus nyata dan mempunyai bukti hak; data harian tidak boleh menyamar intraday.

## D. Apa yang sudah tersedia dan tidak perlu ditulis ulang

Facade/bridge baru memiliki `researchQuery`, `corporateOfferAction`, `strategyLab`, company review/execute/capital, multiAssetOrder, performance report, dataset catalog dan resume mode. Labs tersimpan di runtime save per mode. Listing IPO tersimpan dan dapat direload, voting mempunyai snapshot/deadline/result, tender/corporate melindungi saham, akun internal ditolak dari API pemain, replay slot terpisah dan berhenti di akhir dataset.

UI baru sudah menyediakan terminal4jenis, performance report, companyupgrade/policy/capital/report, offerdetail/review, labhistory/reset/replay, replaycatalog/consent/resume, leaderboardmode/nullableROI, persistentpreferences dan company/personalloan selector. Sharedformatter yang hilang sudah diperbaiki setelah browser menemukan error. Perubahan fee split/multi-aset sudah ada.

Tapi integrasi dan acceptance **belum selesai**. Jangan menyatakan seluruh fitur lengkap dari daftar di atas. Periksa API/contract/result nyata dan reuse implementasi yang ada.

Bukti yang boleh dirujuk sesuai cakupan:

- `Artifacts/Wire-Domain-2026-10-21/junit-latest.log`: compile +20tes terarah PASS, termasuk7integrationtests.
- `Artifacts/Wire-Review-2026-10-21/domain-final/junit-latest.log`: compile +31tes terarah PASS. Ada overlap, jangan dijumlah.
- `Artifacts/Wire-Integration-2026-10-21/journey-result.json`:24pemeriksaan journey bernama PASS, bukan semua fitur/branch. Paidresearch/tender/loan successfulflow belum terbukti dari lockedscreen.
- `root-matrix.json` di direktori sama:70samples=5root×7viewport×2textscale, bukan seluruh nestedstate.
- `compile/junit-latest.log`:35bridgebaseline,2gagal pada fixture loan/corporate. Ini FAIL yang belum ditutup, bukan diabaikan.
- Belum ada fullregression akhir, buildMainActivity/Android atau APK baru dari seluruh WIP ini. APK lama tidak meluluskan source baru.

## E. Pembagian multi-agent

Periksa kemampuan alat yang tersedia. Bila memungkinkan, gunakan satu koordinator dan hingga tiga worker:

1. **Domain/integrasi:** pemisahan clock FX, mode/save, kontrak finansial, fixture dan persistence.
2. **UI/journey:** alur form, review, receipt, state, akses menu dan aksesibilitas.
3. **QA/reviewer:** host browser, bukti pengujian, urutan kritis, performa dan review independen.

Koordinator menetapkan satu writer untuk setiap shared file seperti facade, bridge, app.js dan save schema. Setiap worker menerima root, scope file/API, ID requirement, seed, save, port, output, command pengujian dan larangan. Jangan mengedit file, output build atau save yang sama secara bersamaan. Jangan menghidupkan workflow/launcher lama atau membuat konfigurasi global baru. Bila multi-agent tidak tersedia, gunakan pass implementasi dan review terpisah, lalu catat keterbatasannya secara jujur.

## F. Urutan kerja wajib

### Fase 1 — Preflight dan reproduksi

Verifikasi Git, fingerprint source, save pribadi dan proses aktual. Host QA 8784 sebelumnya sudah dihentikan; periksa ulang listener sebelum menjalankan host baru. Jangan menganggap lock/log sebagai bukti proses hidup atau mati.

Baca checkpoint terbaru dan spesifikasi, petakan celah yang masih ada, kemudian segera lanjutkan eksekusi. Jangan membuat desain baru untuk menggantikan desain yang sudah disetujui. Kompilasi WIP dan jalankan tes relevan dengan output baru. Bedakan fixture yang kedaluwarsa dari bug produksi; jangan melemahkan agunan, review, requestId atau jendela voting agar tes hijau.

### Fase 2 — Tuntaskan integrasi yang masih terbuka

1. **Pisahkan waktu quote FX dan waktu ledger.** TTL quote memakai waktu nyata, sedangkan settlement ekonomi memakai waktu game. Telusuri seluruh caller FxExchangeService/FxQuote/facade/bridge. Uji expiry, retry, perubahan kurs, saldo/reserve, timestamp ledger dan return. Jangan mengganti semua timestamp dengan salah satu clock tanpa memahami kontraknya.
2. **Perbaiki dua bridge regression fixtures.** Loan memakai quote, agunan dan konfirmasi; jalur pencairan legacy harus ditolak. IPO mempunyai requestId stabil, voting mengikuti tanggal game dan hak record-date. Tambahkan keberhasilan serta penolakan; jangan menghapus cakupan lama atau membuka kembali bypass produksi.
3. **Periksa payload IPO dari UI.** Pastikan requestId tetap sama ketika retry. Uji reserve, partial allotment/refund, listing, penjualan posisi setelah listing dan reload.
4. **Lengkapi tampilan kepemilikan multi-aset.** API sudah mempunyai quantity, marketValue, positionId dan unitPriceRaw. Pemain harus bisa memeriksa unit, nilai, kupon, jatuh tempo, receipt dan hubungan dengan wealth. Uji beli/jual, fee dan kekurangan likuiditas.
5. **Uji ulang UI terakhir.** Periksa arah modal dua sisi, pemilih perusahaan peminjam, wrapper koleksi/agunan, invalidasi quote ketika input berubah, retry ID loan, persistensi preferensi, reduced motion/kontras, ID lab setelah pergantian mode, dan status penawaran/pembatalan.
6. **Buktikan keberhasilan alur yang sebelumnya hanya terkunci.** Riset berbayar/expiry, pinjaman perusahaan, rights/tender/settlement/voting/IPO dan transaksi multi-aset harus diuji lewat UI. Layar terkunci atau form terbuka tidak meluluskan settlement. Fixture terisolasi boleh untuk cabang tertentu; earned journey tetap perlu bukti sesuai standar.
7. **Periksa mode/replay dan kegagalan save.** Akhir dataset menampilkan alasan Skip yang hanya berjalan sebagian dan tetap menyediakan jalan kembali ke permainan utama. Jangan bocorkan future OHLC, fundamental modern, kalender atau berita masa depan. Posisi/reserve/pending order harus tetap benar ketika tanggal, sesi atau simbol ditolak.
8. **Periksa fee dan statistik.** Actual acquisition fees harus bertahan melalui split, reverse split, merger, lab dan save. Net P/L, dedup fill, FX dan period return harus konsisten setelah reload. Dietz memisahkan arus noninvestasi, debt dan fee; denominator tidak valid ditampilkan tidak tersedia.
9. **Cocokkan seluruh fitur wajib.** Telusuri layar → handler → bridge → domain → save → bukti. Tuntaskan alur lain yang terbukti masih hilang. Reuse implementasi yang ada; jangan menganggap satu perbaikan sempit membuktikan seluruh produk lengkap.

Standar setiap fase: seluruh kontrak yang berlaku berfungsi, tidak ada bug terbuka yang diketahui termasuk minor, regresi bermakna menangkap akar masalah, dan hasil integrasi lolos review serta tes ulang. Kegagalan dependency tidak dianggap lulus; tugas independen boleh dilanjutkan.

### Fase 3 — Pengujian integrasi menyeluruh

- Jalankan **full JVM/bridge suite** dari source akhir, bukan hanya subset. Simpan command, exit code, XML, jumlah tes, failures/errors/skipped dan revision.
- Minimal **10 variasi × 100 aksi bermakna** untuk uang/order/waktu/save. Simpan seed dan urutan aksi, periksa invariant setelah setiap aksi, sertakan kasus batas, proses terputus dan save gagal. Query atau no-op berulang bukan pengganti aksi.
- Browser harus memakai domain Java nyata dan save QA. Uji positive, boundary, locked, empty, invalid, stale, submitting, pending, partial, settled, error/recovery dan archived yang applicable. Draft, fokus, navigasi, Back dan CTA harus aman.
- Periksa seluruh route/state applicable pada **320×568, 360×640, 390×844, 412×915, 430×932, 768×1024, 1440×900**, masing-masing teks normal dan 200% nyata. Root 70 samples hanya boleh dirujuk bila masih berlaku; tidak meluluskan nested states. Inspeksi screenshot, geometry, target sentuh setara48dp, kontras, reduced motion, overflow, scroll dan CTA.
- Matriks detail sebelumnya berstatus **INVALID_HARNESS_NOT_PRODUCT_FAILURE** karena helper Python tertimpa variabel loop. Nama variabel sudah diperbaiki, tetapi belum dites ulang. Jalankan kembali dan periksa kualitas harness; jangan menyebut error harness sebagai bug aplikasi atau mengubahnya menjadi PASS tanpa run.
- Performa: minimal **30 sampel per jenis aksi**, p95 aksi biasa ≤300ms, navigasi ≤1detik, feedback proses berat benar-benar ter-render ≤300ms dan UI responsif. Catat raw timings, cache, lingkungan dan revision. Jangan menghapus outlier atau mengagregasi untuk menyembunyikan aksi lambat. Browser bukan bukti FPS/RAM Android.
- Gunakan reviewer berbeda bila tersedia. Tutup semua temuan dan uji source gabungan; perubahan sesudah tes memerlukan pemeriksaan ulang yang terdampak. Jangan menjanjikan bahwa bug tersembunyi mustahil ada.

### Fase 4 — Tracker, data paket, commit dan APK

Rekonsiliasi STATUS.json, REQUIREMENTS, SCREEN-COVERAGE dan GAP-MATRIX dengan mapping seed. PASS membutuhkan bukti dan revisi; jangan meluluskan semua baris secara otomatis. Bedakan empat langkah resume ini dari 14 fase produk yang tetap berlaku.

Selaraskan manifest dataset dan isi APK dengan provenance. CSV legacy yang ditolak loader masih dapat ikut paket; quarantine atau exclude dengan path terverifikasi dan bukti/backup yang dipertahankan. Lisensi kode repositori tidak otomatis menjadi hak redistribusi data upstream. Jangan mengarang izin atau membeli layanan baru.

Setelah review/regresi lulus, buat commit lokal selektif hanya untuk source/test/docs yang siap. Jangan commit secrets, save pribadi, binary besar atau data tanpa hak. Pertahankan dan jelaskan local changes lain yang belum siap.

Bangun APK baru dari source yang diuji. Periksa appID, version, minSdk/targetSdk, status debug/release, signing certificate digest, signature, asset/data/source correspondence, ukuran, SHA-256, Git revision dan dirty fingerprint. APK lama yang disalin/diubah nama bukan hasil build baru; APK debug bukan store release. Tidak ada ADB atau instalasi pada tugas ini.

## G. Runner yang tersedia

Dari root produksi melalui PowerShell, gunakan output baru agar bukti lama tidak tertimpa:

```powershell
node --check android/app/src/main/assets/www/app.js
./Work/run-codex-jvm-check.ps1 -Tests com.tradingglobal3d.core.WireIntegrationDomainTest,com.tradingglobal3d.core.PositionAcquisitionFeeRegressionTest,com.tradingglobal3d.core.FeatureCompletionCorporateTest,com.tradingglobal3d.core.FeatureCompletionResearchTest,com.tradingglobal3d.core.FeatureCompletionLabTest,com.tradingglobal3d.core.FeatureCompletionMultiAssetTest -OutputDirectory Artifacts/Wire-Resume-2026-10-21/targeted
./Work/run-codex-jvm-check.ps1 -OutputDirectory Artifacts/Wire-Resume-2026-10-21/full
```

QA browser tersedia pada `Work/WireQaServer.java`, `Work/WireQaStart.py` dan `Work/verify-wired-features-2026-10-21.py`. Baca guard/argumen dahulu. Compile classes ke lokasi yang diharapkan helper, jalankan host dengan save QA port8784, lalu gunakan `--inspect`, `--journeys` dan `--matrix`. Periksa juga opsi matriks detail yang ditambahkan ke script aktual.

Script mempunyai output tetap. Backup bukti atau pilih output baru secara eksplisit; jangan hapus log lama. Python sistem sudah mempunyai Playwright/Chromium yang berhasil dipakai. Periksa runtime tersebut sebelum menginstal ulang; jangan mengakali pembatasan lingkungan atau mengarahkan runner reset ke preview pribadi.

Build Android memakai JBR/Gradle existing; workaround per proses bila error Windows selector yang sama muncul:

```powershell
$env:JAVA_HOME='C:/Program Files/Android/Android Studio/jbr'
$env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=C:/gradle-socket-fallback-do-not-create'
./android/gradlew.bat -p android --no-daemon testDebugUnitTest assembleDebug --console=plain
```

## H. Ketekunan, checkpoint dan completion audit

Lanjut otomatis tanpa approval rutin untuk kode, tes, detail aturan yang sudah didelegasikan dan commit lokal. Jangan berhenti karena durasi, iterasi, penghematan token, satu tes hijau, rencana selesai atau APK terbentuk. Hormati STOP pemilik, batas wewenang dan hambatan nyata.

Setelah dua kegagalan sama tanpa diagnosis atau bukti baru, laporkan **PERLU PERHATIAN**, bukti dan pendekatan berbeda. Setelah tiga, kerjakan tugas independen atau catat BLOCKED bila memerlukan tindakan eksternal. Jangan mengulang command identik tanpa alasan atau memulai proses kedua karena polling timeout; periksa handle yang masih hidup.

Checkpoint sesudah batas aman dan sebelum context/sesi habis: root, branch/HEAD/dirty fingerprint, waktu, ownership/proses/port/save QA, file berubah, test command/exit/revision/bukti, temuan terbuka, status fase, hash save pribadi, blocker dan next_action konkret. Gunakan `Work/CHECKPOINT-WIRING-RESUME-2026-10-21.md`. Source yang belum diuji harus disebut WIP. Setelah STOP, hentikan hanya server/browser milik pekerjaan ini dan catat status aktual; jangan mematikan proses lain.

Laporan akhir wajib memuat:

1. Fitur dan alur yang benar-benar berfungsi, dengan pemetaan requirement/14 fase ke bukti.
2. Bug yang ditemukan, diperbaiki dan masih terbuka; full tests, browser, urutan kritis dan performa beserta batasnya.
3. Dataset/provenance, rulesVersion dan asumsi yang diputuskan.
4. Commit lokal, dirty source tersisa dan fingerprint yang diuji.
5. APK baru, metadata, signature, checksum dan petunjuk pemulihan save.
6. Hash save pribadi sebelum/sesudah, checkpoint lanjutan dan gate native PENDING terpisah.

Sebelum menyatakan selesai, anggap completion belum terbukti dan audit setiap requirement terhadap source/runtime/bukti aktual. Bukti lemah atau hilang berarti belum lulus. Jangan mempersempit tujuan menjadi bagian yang sudah ditulis. Bila requirement wajib masih blocked atau bug diketahui masih terbuka, produk tetap PARTIAL; mock, jumlah tes dan APK saja tidak meluluskan produk.

Mulai sekarang: **preflight/checkpoint terbaru → reproduksi FX/fixture/UI tertunda → tutup integrasi → full pengujian/perbaikan/review → tracker/commit/APK baru.** Eksekusi langsung, jangan berhenti pada balasan rencana.
