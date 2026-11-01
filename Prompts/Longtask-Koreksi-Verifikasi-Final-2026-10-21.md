# Long task Hermes: koreksi verifikasi akhir, perbaiki temuan, dan bangun ulang APK

Saya meminta kamu langsung mengeksekusi pekerjaan ini secara otonom sampai seluruh gate dalam scope terpenuhi atau terdapat hambatan nyata. Ini perintah implementasi, pengujian, review, dan penyerahan; jangan berhenti setelah membuat rencana atau laporan audit saja.

## 1. Tujuan dan batas pekerjaan

Lanjutkan Game Saham 2D Portrait yang sudah tersedia. Perbaiki cacat serta kelemahan pembuktian yang ditemukan audit, pertahankan fitur dan desain terbaru, lalu hasilkan APK baru dari source akhir yang diuji. Jangan membangun ulang game, mengganti engine/tema, atau menambah fitur spekulatif.

Root produksi yang boleh ditulis:
`C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`

`C:/Users/HYPE AMD/Downloads/VIBE CODING/Game Trading Saham` adalah checkout historis yang berbeda. Jangan menulisnya, mengambil checkpoint-nya sebagai state produksi, atau mengimpor source/save/build/credentials darinya.

Java domain tetap satu pemilik ekonomi, clock, akun, ledger dan save. Android WebView + HTML/CSS/JS lokal tetap digunakan. JS hanya input/presentasi melalui bridge. Pertahankan application ID `com.tradingglobal3d.mobile`, signing identity, kompatibilitas save, lima tab dan desain terbaru. Jangan membatalkan keputusan produk terbaru hanya karena prompt historis berbeda; cari bukti keputusan tersebut dan catat konflik yang material.

Diizinkan: perbaikan source/test/docs dalam root, QA terisolasi, browser lokal, build, review dan commit lokal selektif. Jangan push/publish, mengirim pesan pihak lain, membayar layanan, mengubah profil/model/provider/credentials/global Hermes, memasang scheduler/launcher baru atau menyentuh repo/data lain.

Scope penyerahan tetap domain/host/browser/APK. Jangan menjalankan ADB, emulator, instalasi atau mengoperasikan HP. Runtime Android, Back/IME/lifecycle fisik, installed APK, FPS/RAM/baterai perangkat tetap NOT_TESTED sampai tersedia bukti pada tugas perangkat tersendiri. Jangan memakai batas ini sebagai alasan menunda pekerjaan host yang independen.

## 2. Preflight dan urutan baca

Sebelum edit, buktikan cwd/Git root/branch/HEAD, tracked/untracked changes, proses aktif, ownership writer, port dan save QA. Hanya satu writer per file/shared state dan satu writer untuk output build. Jangan menghentikan proses pengguna atau writer lain tanpa dasar kewenangan.

Pertahankan local changes. Jangan reset/clean/checkout paksa atau rollback untuk menyamakan laporan lama. Snapshot audit terakhir: branch `codex/owner-audit-portrait-v8`, HEAD `013fc24`, working tree bersih. Ini informasi baseline, bukan target reset; verifikasi ulang karena source dapat berubah.

Save pribadi yang dilindungi:
`Artifacts/LocalPreview/preview-save/save_game_v2.json`
SHA-256 terakhir: `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`.

Hitung hash aktual sebelum/sesudah. Semua transaksi, reset, crash, migrasi dan recovery memakai direktori save QA baru. Jangan memakai save pribadi atau preview pribadi port 8766. Hash berbeda memerlukan diagnosis, bukan overwrite dengan salinan lama. Kecocokan hash hanya membuktikan kecocokan isi pada titik pemeriksaan, bukan sejarah bahwa berkas tidak pernah disentuh.

Baca bertahap sesuai pekerjaan:
1. `AGENTS.md`, `PLAN.md`, `RELEASE-DOSSIER.md`, `Work/STATUS.json`.
2. `Work/Hermes-Handoff-2026-10-20/PHASE-QUALITY-STANDARDS.md` dan keputusan pemilik terbaru yang relevan.
3. `Prompts/Longtask-Penuntasan-Fitur-Pengujian-2026-10-21.md` sebagai cakupan fitur dan acceptance; baseline WIP di dalamnya bersifat historis.
4. Konsep/PRD/design brief, requirement, screen coverage, matriks fitur, TASK 00–13 dan acceptance yang dirujuk dokumen tersebut.
5. Source aktual dan bukti audit pada bagian berikut.

Jika dokumen tidak ada, cari dalam root. Jangan mengarang isinya. Seluruh requirement mandatory tetap berlaku; jangan mengurangi denominator, mengganti FAIL menjadi opsional, atau menyamakan jumlah tes dengan persentase produk.

## 3. Temuan baseline yang wajib direproduksi

Audit 21 Oktober 2026 menemukan:
- APK `Artifacts/Release-Final-2026-10-21/GameSaham-v1.0.0-final.apk` berukuran 836637 byte, SHA-256 `e7b1021928fa3fc37b58b9e4fc49359e02f577cd3425239f942b3232d2f7d360`. Kelima aset www cocok dengan source saat audit. Ini bukan hash target APK baru.
- XML `android/app/build/test-results/testDebugUnitTest/`: 454 tes, 0 failure/error/skipped, 54 suite. Angka 45 suite dalam laporan tidak sesuai XML.
- `Artifacts/Wire-Integration-2026-10-21/root-matrix.json`: 70 sampel root, tanpa errors.
- `Artifacts/Wire-Integration-2026-10-21/flow-matrix.json`: 266 sampel, satu gagal pada `flow-company-upgrade-review-320x568-text200`; scrollWidth 339 pada viewport 320. Scope laporan: 19 named wired feature states, bukan seluruh state produk.
- `AllFeaturesExtreme10x100CriticalSequenceTest.java`: `Random rnd` tidak dipakai untuk urutan; pilihan aksi memakai `step % 8`; beberapa exception ditelan dan aksi tetap dihitung. Klaim seluruh fitur/10 variasi/multi-tahun belum dibuktikan oleh tes itu.
- 228 requirement, 148 delta dan 26 layar diberi PASS, tetapi banyak bukti generik; seluruh 26 layar merujuk showcase/root matrix yang sama.
- `Work/STATUS.json` mencampur COMPLETE/PRODUCTION_VERIFIED_COMPLETE dengan catatan lama SPECIFIED_NOT_IMPLEMENTED, native NOT_TESTED dan PAUSED_BY_OWNER. Jangan menganggap salah satu label sebagai bukti current source.
- Autosave callback dan mode hemat daya ada di source; jaminan tidak mungkin kehilangan progres serta klaim 60/120 FPS perangkat tidak terbukti oleh keberadaan kode/toggle.
- Dataset legacy yang dinyatakan quarantine masih berada dalam ZIP APK. Loader exclusion dan package exclusion adalah pemeriksaan berbeda; cocokkan dengan kebijakan distribusi existing.

Audit ini membaca hasil tes tersimpan, bukan rerun seluruh suite. Reproduksi memakai source sekarang, simpan output baru, dan catat jika temuan sudah diperbaiki oleh perubahan lain.

## 4. Cara kerja dan pembagian

Pakai helper, runner, dependency dan pola existing. Telusuri seluruh caller sebelum memilih root fix. Stdlib/native lebih dahulu; minimum diff yang benar, tanpa framework/abstraksi baru hanya untuk QA. Jangan menyederhanakan validasi uang, data loss, security atau aksesibilitas.

Gunakan multi-agent nyata bila tersedia: satu koordinator, worker tes/domain, worker UI/browser dan reviewer. Tetapkan ownership file, kontrak, save/port/output dan dependency sebelum paralel. Shared facade/bridge/save/app.js hanya satu writer. Review terpisah dari implementer; bila kemampuan tidak tersedia, lakukan pass review terpisah dan nyatakan keterbatasannya. Jangan mengklaim review independen yang tidak dilakukan.

## 5. Eksekusi A — benarkan tes ekstrem dan perbaiki bug yang terungkap

Review tes kritis existing sebelum menambahnya. Perbaiki tes shared atau runner existing; jangan sekadar mengganti nama, menambah jumlah assertion atau membuat framework baru.

Kriteria minimum:
- Sepuluh urutan reproducible yang benar-benar bervariasi, masing-masing minimal 100 aksi bermakna. Seed memengaruhi urutan/input; simpan action trace, seed, hasil dan fingerprint source. Hindari random yang membuat seluruh jalur keberhasilan bergantung kebetulan; pastikan coverage wajib tercapai.
- Setiap operasi memeriksa hasil dan perubahan state yang seharusnya. Cabang penolakan memeriksa jenis error/hasil yang diharapkan dan state tetap konsisten. Exception tak terduga harus menggagalkan tes; tidak ada catch-all yang meluluskan aksi.
- Hitung operasi yang tervalidasi. Query/no-op, percobaan gagal tanpa assertion, atau mengganti seed pada urutan identik tidak memenuhi 100 aksi. Retry idempotent merupakan kasus sah bila memeriksa mutasi tepat sekali.
- Invariant diperiksa sesudah setiap mutasi ekonomi, termasuk suboperasi dalam satu blok. Periksa cash/reserve/posisi/utang/ledger/fee/akun pribadi-vs-klien serta clock/save yang relevan. `netWorth != null` bukan bukti konservasi nilai.
- Cakup saham market/limit/trigger, partial fill/cancel/retry/stale; margin/short/interest/liquidation; pinjaman tenor/agunan/late/restructure/prepay; ketiga reksa dana subscribe/redeem/cancel/cutoff/backing; bisnis/payroll/modal/dividen; corporate/IPO/allotment/refund/listing/voting; multi-aset/FX; kalender/misi/liga; replay/lab; save/load/recovery sesuai requirement.
- Buktikan skenario multi-tahun pada rentang game time yang sungguh mencapainya, bukan sekadar komentar atau beberapa skip. Aktif/offline/Skip memakai kontrak waktu existing dan event tepat sekali. Gunakan clock/fixture QA existing agar tes tidak menunggu waktu nyata.
- Untuk cakupan yang lebih tepat diuji terpisah, reuse tes deterministik existing dan petakan bukti; jangan memaksa semua fitur ke satu switch besar. Minimal 10×100 untuk logika kritis tetap dipenuhi sesuai standar fase.
- Fixture kaya/unlocked boleh untuk cabang terisolasi, tetapi bukan bukti earned progression atau keseimbangan ekonomi. Pertahankan journey new game → trading → promosi → mandat yang sah.

Buktikan tes yang diperbaiki akan gagal pada cacat yang ditargetkan. Perbaiki bug produksi yang ditemukan di fungsi shared, jalankan tes terarah lalu regresi integrasi. Simpan reproduksi dan hasil review sebelum menutup temuan.

## 6. Eksekusi B — UI, journey dan aksesibilitas

Reproduksi overflow company-upgrade-review 320×568 teks 200%. Perbaiki layout shared penyebabnya dengan tetap mempertahankan isi, font scale, scroll dan CTA. Jangan meluluskan dengan menyembunyikan overflow, mengecilkan teks wajib, menghapus konten atau melemahkan assertion. Overflow horizontal hanya boleh pada komponen yang memang didesain demikian dan tetap dapat digunakan.

Susun inventaris layar/state applicable dari requirement dan route source aktual. Hubungkan 26 konsep layar, tujuan design brief, nested forms/review/receipt dan journey; jangan menganggap jumlah tersebut identik atau menjumlah parent-child sebagai fitur unik.

Uji semua pasangan layar/state applicable pada 320×568, 360×640, 390×844, 412×915, 430×932, 768×1024 dan 1440×900, teks 100%/200% nyata. Gunakan host Java produksi dengan save QA; mock visual berlabel tidak meluluskan integrasi produksi.

Periksa default/empty/loading/locked/error/invalid/stale/retry/success sesuai applicability, fokus dan back/close, input tidak hilang saat refresh, kontras, reduced motion, target sentuh setara 48 dp, scroll terakhir serta CTA/nav. Jalankan transaksi melalui UI sampai domain/save/reload, bukan berhenti di form terbuka atau layar terkunci. Buka dan inspeksi screenshot yang diklaim direview; simpan bukti dan cakupan yang sebenarnya.

## 7. Eksekusi C — save, recovery dan performa

Telusuri jalur mutasi → persistence → acknowledgement/receipt, termasuk facade, bridge, background callback dan error UI. Jangan menambah save berulang pada semua caller bila satu root fix cukup.

Gunakan save QA terisolasi untuk menguji mutasi tersimpan, write failure, temporary file/backup, file utama korup, interrupted write, restart/load, migrasi yang berlaku dan retry idempotent. Reuse fault injection/runner existing; bila perlu, host subprocess boleh dipakai untuk memutus proses QA pada batas yang dapat direproduksi. Jangan mengklaim itu sebagai Android force-stop. Periksa tidak ada silent reset, receipt sukses palsu, mutasi ganda atau recovery ke state yang tidak dijelaskan. Jika hanya callback yang diuji, nyatakan batas tersebut.

Ukur performa browser/host sesuai standar existing: sedikitnya 30 sampel per jenis aksi/navigasi yang ditentukan sebelum pengukuran, raw timing/environment/cache/seed/source hash. Input sampai hasil yang benar terlihat dan state konsisten; spinner saja bukan settlement sukses.

Ambang: P95 per aksi biasa ≤300 ms dengan nearest-rank; seluruh sampel navigasi wajib ≤1000 ms; feedback pekerjaan berat benar-benar tergambar ≤300 ms dan event loop responsif. Catat failure/timeout, jangan digabung dengan aksi cepat atau membuang outlier untuk meluluskan. Periksa mode hemat daya ON/OFF dan persistensinya. Toggle blur bukan bukti FPS atau penghematan baterai HP; hapus/kualifikasikan klaim yang belum diukur.

## 8. Eksekusi D — rekonsiliasi bukti dan dataset

Periksa seluruh 228 requirement, 148 delta dan 26 konsep layar beserta parent/acceptance existing. Gunakan tracker existing; tambahkan kolom/file minimal hanya bila hubungan bukti tidak dapat dicatat di sana.

Setiap PASS perlu requirement ID → implementasi/route → test case atau inspeksi spesifik → hasil → revisi/hash source → reviewer. Satu test boleh mendukung beberapa requirement bila hubungan jelas; direktori test atau showcase umum saja tidak meluluskan seluruh kebutuhan. Bedakan source implemented, subset tested, end-to-end passed, FAIL, NOT_TESTED dan BLOCKED. Jangan menandai ulang massal menjadi PASS/PARTIAL tanpa evaluasi per butir.

Validasi replay/provenance/checksum/loader dan kemasan APK sesuai ketentuan existing. Dataset yang dilarang didistribusikan harus dikeluarkan dari paket final walaupun loader menolaknya; pertahankan bukti audit di lokasi non-distribusi dalam root. Verifikasi sumber primer/hak bila perlu, jangan menyimpulkan dari lisensi kode mirror. Jangan membeli layanan atau memperluas dataset tanpa kebutuhan. Laporkan cakupan AAPL 61 bar harian secara tepat bila itu masih cakupan aktual.

Selaraskan STATUS, requirement/screen/gap tracker, PLAN, release dossier, checkpoint dan ringkasan remediation yang masih dijadikan rujukan aktif. Arsip historis diberi konteks, bukan dipalsukan menjadi laporan baru. Hitung jumlah suite/test dari output nyata; jangan hardcode 454/54 sebagai target.

## 9. Eksekusi E — regresi final, review dan APK

Setelah integrasi stabil:
1. Jalankan full JVM/bridge regression pada source final; catat command, exit code, tests/suites/failure/error/skipped dan source fingerprint.
2. Jalankan matriks UI/journey/performa final sesuai cakupan. Perubahan setelah tes memerlukan rerun terdampak; jangan merujuk hasil sebelum root fix sebagai final.
3. Reviewer memeriksa diff, test harness, action traces, screenshot, metrics, requirement mapping dan temuan lama. Perbaiki semua temuan valid termasuk minor, lalu koordinator memverifikasi hasil gabungan.
4. Bangun APK baru dari source yang lolos. Periksa manifest/application ID/SDK/version, signing certificate dan scheme, paket aset/dataset serta kecocokan hash aset dengan source akhir. Nyatakan debug/release signing sesuai fakta; jangan mengganti key hanya untuk label rilis.
5. Simpan APK, byte size, SHA-256, source revision/fingerprint, build command/hasil dan batas bukti. Jangan overwrite APK lama untuk menyamarkan perubahan; gunakan nama/path kandidat yang jelas.
6. Periksa hash save pribadi dan local changes, lalu commit lokal selektif atas pekerjaan yang siap. Jangan commit secrets/save pribadi/dataset tanpa hak atau push. Bedakan tracking ref lokal dengan remote yang benar-benar diverifikasi.

## 10. Ketekunan, pelaporan dan completion

Kerjakan satu hasil terukur → uji → review → checkpoint → lanjut otomatis. Jangan meminta persetujuan rutin atau berhenti karena durasi, jumlah giliran/iterasi, penghematan token, satu tes hijau atau APK terbentuk. Jangan berjanji bekerja di latar belakang setelah sesi berhenti; checkpoint/resume adalah mekanisme kesinambungan, bukan scheduler.

Sesudah dua kegagalan sama tanpa diagnosis/bukti baru, kirim PERLU PERHATIAN beserta bukti dan pendekatan berbeda. Sesudah tiga, alihkan ke pekerjaan independen atau catat blocker nyata bila tidak ada kemajuan tanpa tindakan luar. Hormati STOP/pause pemilik pada batas aman. Jangan memulai writer/launcher kedua untuk sesi yang masih hidup.

Gunakan evidence directory baru `Artifacts/Final-Verification-2026-10-21/` dan checkpoint `Work/CHECKPOINT-FINAL-VERIFICATION-2026-10-21.md`; bila sudah ada, baca lalu lanjutkan dengan run subfolder baru agar hasil sebelumnya tidak tertimpa. Catat timestamp WIB, root/branch/HEAD/dirty fingerprint, exact Hermes session ID aktual, ownership/proses/port/save QA, perubahan, command/exit/hasil/revisi, temuan terbuka, status gate, blocker dan next_action konkret. Sesi audit sebelumnya `20261020_230515_5c4e96` adalah referensi; jangan menganggap sesi baru mempunyai ID tersebut. Resume exact ID, bukan latest.

Laporan chat pada batas subpekerjaan: progres verifikasi A–E; x/14 fase produk hanya jika acceptance fase dibuktikan; selesai/berjalan/pending; file berubah; hasil dan path bukti; blocker; next_action. Persentase jumlah file/tes bukan persentase produk selesai.

COMPLETE untuk penyerahan domain/browser/APK hanya boleh bila seluruh mandatory gate dalam scope mempunyai bukti sesuai source akhir, tidak ada kegagalan wajib atau bug terbuka yang diketahui, reviewer sudah memeriksa hasil, tracker konsisten dan APK cocok. Status COMPLETE sebelumnya tidak membebaskan pemeriksaan ini. Jangan melonggarkan standar agar label tetap COMPLETE.

Laporkan gate Android fisik secara terpisah sebagai NOT_TESTED/DEFERRED sesuai scope, walaupun penyerahan host/browser/APK sudah lulus. Jangan menyebut full production/hardware verified, 60/120 FPS perangkat atau jaminan progres tidak mungkin hilang tanpa bukti yang sesuai. Jika masih ada gap dalam scope, tulis PARTIAL/BLOCKED sesuai fakta dan sebabnya.

Keluaran akhir: perbaikan source/test, matriks bukti yang dapat ditelusuri, action trace dan hasil full regression, screenshot/journey dan raw metrics, bukti save/recovery, audit paket, APK baru + SHA-256, daftar temuan beserta penutupannya, verdict per gate serta checkpoint next action. Laporan bukan pengganti perbaikan yang masih dapat dikerjakan.

Mulai sekarang dari preflight dan reproduksi temuan. Setelah itu langsung lanjutkan perbaikan, pengujian dan review sampai completion gate tercapai atau tersisa hambatan nyata di luar wewenang.
