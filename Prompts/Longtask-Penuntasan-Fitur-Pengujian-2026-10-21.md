# Prompt long task: tuntaskan seluruh fitur, integrasikan UI, uji, perbaiki, dan hasilkan APK

Saya adalah pemilik proyek. Saya mengizinkan kamu melanjutkan pekerjaan existing secara otonom sampai seluruh fitur wajib di bawah dapat dimainkan dan memenuhi standar penyerahan. **Lengkapi fitur yang belum masuk dahulu, kemudian langsung jalankan pengujian menyeluruh dan perbaikan temuan.** Tes developer tetap dilakukan selama implementasi agar bug tidak menumpuk. Ini mandat eksekusi, bukan permintaan rencana saja.

## 1. Hasil akhir yang diminta

Hasil akhir adalah game Android 2D portrait dengan UI sesuai aplikasi referensi HP yang sudah saya sukai, fitur aplikasi referensi serta dokumentasi game yang berlaku tersedia secara fungsional, logika Java konsisten, hasil transaksi tersimpan, dan APK kandidat baru dibangun dari source final yang telah diuji.

Selesai berarti: seluruh requirement mandatory yang applicable mempunyai bukti sesuai cakupannya, tidak ada bug terbuka yang diketahui termasuk minor, seluruh fase yang disyaratkan lulus, UI dapat dipakai melalui alur pengguna nyata, save pribadi terlindungi, laporan/trackers konsisten, dan APK diverifikasi. Tidak berarti menjanjikan bahwa bug tersembunyi mustahil ada.

Keberadaan kelas, metode, kartu UI, rencana, test fixture, jumlah tes, atau APK lama tidak membuktikan hasil akhir. Jangan mengganti tujuan ini menjadi subset yang lebih mudah diluluskan.

## 2. Lokasi kerja dan preflight wajib

Root produksi yang boleh ditulis:

`C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`

Checkout historis `C:/Users/HYPE AMD/Downloads/VIBE CODING/Game Trading Saham` adalah referensi berbeda. Jangan menulisnya, memakai checkpoint-nya sebagai state aktif, atau menyalin source/save/build/credentials otomatis. Gunakan dokumen dan screenshot/REA referensi yang sudah tersedia di root produksi.

Sebelum perubahan:

1. Buktikan cwd dan Git root, branch, HEAD, tracked/untracked changes serta proses/port/save QA yang aktif.
2. Pastikan hanya satu koordinator dan satu writer per file/shared state. Jika ada pekerjaan aktif lain, jangan mengedit file yang sama atau menjalankan reset/build pada output yang sama. Koordinasikan berdasarkan kemampuan yang benar-benar tersedia; jangan menebak proses sudah berhenti hanya karena log tidak bergerak.
3. Pertahankan semua local changes. Dilarang reset/clean/checkout paksa, menghapus source baru, atau rollback agar hash cocok laporan lama.
4. Verifikasi hash save pribadi dan simpan baseline audit. Semua transaksi ekstrem, reset, prestige, mode replay, crash dan restore menggunakan save QA terisolasi.
5. Baca checkpoint terbaru dan source aktual. Rujukan baris adalah snapshot, cari simbol saat ini.

Snapshot pemeriksaan 21 Oktober sekitar 07:06 WIB: branch `codex/owner-audit-portrait-v8`, HEAD `7dd0526dd1f9cc0ba7730de3025c54f69f6b2d6d`, banyak local changes. HEAD ini bukan target reset. Commit dapat berubah; verifikasi kembali. Beberapa commit muncul selama integrasi sebelumnya sehingga kepemilikan writer harus diperiksa.

Save pribadi: `Artifacts/LocalPreview/preview-save/save_game_v2.json`, hash terakhir `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`. Jangan menjalankan QA mutasi pada save ini atau preview pribadi port 8766. Hash berbeda adalah alasan inspeksi, bukan izin menimpa dengan salinan lama.

## 3. Sumber keputusan dan urutan baca

Baca isi lengkap dokumen berikut secara bertahap; jangan hanya membaca judul atau mengandalkan ringkasan chat:

1. `AGENTS.md` di root produksi.
2. `Work/CHECKPOINT-LONGTASK-HANDOFF-2026-10-21.md` — keadaan WIP dan daftar integrasi yang belum selesai.
3. `Docs/Product/01-Konsep-Game-2D-Portrait.md`, `Docs/Product/02-PRD-Game-Saham-2D-Portrait.md`, `Docs/Design/03-Design-Brief-Game-Saham-2D-Portrait.md`.
4. `Prompts/Hermes-Final-UI-Gameplay-Multi-Agent-2026-10-20.md` dan `Work/Hermes-Handoff-2026-10-20/README.md`, `OWNER-DECISIONS.json`, `PHASE-QUALITY-STANDARDS.md`, `REVIEW-AND-REGRESSION.md`, `TIME-ACCEPTANCE.csv`, `FEATURE-ACCEPTANCE.csv`.
5. `Work/Hermes-Feature-Completion-2026-10-20/README.md`, `SPECIFICATION.md`, `GAP-MATRIX.csv`, `MATERIALS.json` dan `START-HERMES.md`.
6. `Work/README.md`, `STATUS.json`, `REQUIREMENTS.csv`, `SCREEN-COVERAGE.csv`, `OWNER-UI-LOGIC-MATRIX.csv`, `DECISIONS.md`, `ACCEPTANCE.md` dan TASK fase 00–13.
7. `Work/PHONE-UI-REFERENCE.md`, `REA-FEATURE-MAP.csv`, `REA-APK-ANALYSIS.md` dan screenshot terkait dalam `Artifacts/Phone-UI-Reference/`.
8. `Artifacts/Independent-Review-2026-10-20/REPORT.md`, `Artifacts/Independent-Review-2026-10-21/REPORT.md`, checkpoint Codex/Hermes Session 2/Session 3, dan bukti worker pada direktori FeatureCompletion/Feature-Completion tanggal 21 Oktober.

Keputusan pemilik terbaru mengungguli dokumen historis yang bertentangan: portrait, source Java/WebView existing, UI referensi, lima tab baru, dual-rate clock, seluruh fitur wajib, tanpa instalasi perangkat pada scope ini. Jangan kembali ke 3D/Unity/MAUI/landscape dari rencana lama. Jangan menganggap status lama COMPLETE atau NOT_TESTED sebagai pembuktian current source.

Seed 277 baris dan delta 148 baris adalah bahan requirement/acceptance yang memiliki parent/rollup, **bukan 425 fitur unik atau persentase produk selesai**. Pertahankan seluruh ID dan dependency; satu tracker operasional menghubungkan semuanya. Tidak boleh menghapus requirement sulit atau memindahkannya ke versi berikut agar denominator mengecil.

Jika path tidak ada, cari dalam root produksi. Jangan mengarang isi file. Validator bahan existing boleh digunakan setelah membaca bantuan; PASS validator hanya integritas dokumen, bukan kelulusan game.

## 4. Keputusan produk yang tetap berlaku

- Game offline, tanpa akun cloud, broker/uang nyata, iklan atau layanan berbayar wajib. Gunakan aset lokal; tidak ada remote CDN saat bermain.
- Portrait; konten halaman boleh scroll dan navigasi bawah tetap. Lima tab: **Beranda · Pasar · Portofolio · Bisnis · Lainnya**.
- Ikuti layout, hierarchy, spacing dan pola navigasi aplikasi referensi yang disukai. Graphite/amber/teal dan satu set token/font bersama. Tidak membuat tema baru per layar, tidak mendesain ulang dari nol, tidak memadatkan dashboard landscape ke ponsel.
- Header menampilkan total kekayaan di kanan atas, konteks akun, karier, level/XP dan akses achievement yang jelas. Hindari teks implementasi, kode FR/OQ, paragraf berulang, atau CTA yang tidak dapat dijalankan.
- Trading pribadi dan karier fund manager sama penting. Fitur dibuka oleh karier; XP/badge menunjukkan progres dan tidak memberikan kas otomatis atau melewati syarat jabatan.
- NPC dan nama fiktif pada leaderboard; jangan memakai tokoh nyata sebagai default atau mengklaim peserta online. Tidak perlu label simulasi pada leaderboard, tetapi model pasar/data di layar yang membutuhkan penjelasan tetap harus jujur.
- Kemajuan seimbang pada sesi 15–30 menit. Perusahaan mengikuti kebijakan pemain secara otomatis; keputusan penting tetap manual.
- Tidak ada tamat, prestige wajib, reset paksa saat bangkrut atau rescue cash gratis. Pemulihan melalui pekerjaan dan restrukturisasi yang dapat ditelusuri. Prestige opsional dan menjelaskan reset/retained/bonus sebelum konfirmasi.
- Kalender game mengatur misi, liga, rival, biaya, payroll dan tenggat. Skip tepat 24 jam game: beri peringatan kejadian penting sebelum konfirmasi, lalu proses seluruh interval dan berikan laporan.
- Foreground normal 1×: 24 jam game = **5 menit nyata**. Opsi 2×/5× hanya foreground. Background/ditutup: 24 jam game = **1 jam nyata**, tetap berjalan dengan biaya/risiko, tidak mewarisi speed foreground dan tidak auto-pause.
- Koleksi dapat dibeli/dijual dengan valuasi dan biaya/manfaat masuk akal. Riset default ringkas, detail/chart lanjutan dapat diperluas.
- Replay data nyata wajib; resolusi harian boleh bila disajikan sebagai harian. Tidak membuat jalur intraday dari OHLC harian atau menampilkan harga masa depan.

## 5. Arsitektur dan batas wewenang

Java domain adalah satu pemilik uang, ledger, akun, harga, NPC, order, biaya, FX, kewajiban, clock, karier, XP, statistik dan save. UI HTML/CSS/JS hanya input/presentasi melalui `TradingWebBridge` → `TradingFacade` → domain. Jangan membangun mesin finansial/clock kedua di JavaScript.

Pahami alur dan seluruh caller sebelum edit. Pilih existing helper/pola → stdlib → native platform → dependency terpasang → minimum code baru. Tidak menambah framework, engine, abstraksi spekulatif atau dependency yang tidak diperlukan. Perbaiki akar masalah bersama, bukan patch per tombol. Simplifikasi tidak boleh menghapus validasi input, penanganan data loss, security, aksesibilitas, atau scope eksplisit.

Diizinkan: perubahan source/repo, tes terisolasi, browser lokal, build, review, dependency/aset yang memang diizinkan proyek, branch/commit lokal selektif dan laporan. Jangan push/publish, membayar layanan, mengirim pesan pihak lain, mengubah konfigurasi global agen/model/provider/credentials, membuat scheduler, atau menyentuh repo/data lain.

Scope penyerahan ini **tidak menjalankan ADB, emulator, instalasi atau pengoperasian HP**. Native WebView/Back/IME/lifecycle/FPS/RAM/installed APK harus dicatat PENDING sebagai gate perangkat terpisah. Jangan menggunakan browser sebagai bukti native. Pertahankan appID `com.tradingglobal3d.mobile`, signing identity dan progres; jangan mengganti key atau mengklaim update-compatible tanpa pembuktian.

## 6. Multi-agent yang terkoordinasi

Gunakan kemampuan multi-agent yang benar-benar tersedia, idealnya satu koordinator dan hingga tiga worker. Jangan mengganti model/provider global untuk memaksakannya. Contoh pembagian:

| Peran | Tanggung jawab |
|---|---|
| Koordinator | Shared facade/bridge/save/event pipeline, kontrak, integrasi, status, regresi final, commit dan APK |
| Worker domain | Fitur ekonomi/bisnis/produk/korporasi yang belum lengkap, invariant dan unit/integration tests |
| Worker UI/UX | Handler dan layar sesuai kontrak, akses menu, seluruh state dan aksesibilitas |
| Worker QA/data/review | Dataset/provenance, reproduksi independen, browser/performa dan review temuan |

Tetapkan ownership file, API, ID requirement, seed/save/port/output dan dependency sebelum paralel. Satu writer per shared file; jangan membuat dua agen mengubah app.js, TradingFacade, save schema atau output build yang sama. Worker boleh menyiapkan patch/usulan kontrak bila koordinator sedang menulis shared file.

Reviewer fase berbeda dari implementor bila tersedia. Semua temuannya ditutup dan koordinator menguji hasil gabungan. Jika multi-agent tidak tersedia, gunakan pass implementasi dan review terpisah, catat sebagai fallback; jangan mengklaim review independen. Jangan menjalankan sesi/launcher lama secara otomatis.

## 7. Baseline WIP yang harus dilanjutkan

Checkpoint terbaru memuat domain baru perusahaan/pegawai/kebijakan/transfer/laporan, valuasi koleksi, profil dan gaya investor, backing reksa dana schema 4, NPC liga canonical, transaksi multi-aset, riset premium, lab decision replay, replay legal harian, rights/tender/voting/IPO, PerformancePeriod dan actual acquisition-fee basis.

Sebagian sudah memiliki test subset PASS, sebagian baru tersambung ke facade/bridge/UI. Pemeriksaan terakhir mengompilasi core+bridge+test source lalu meluluskan **15 tes terpilih**. Angka itu tidak menyatakan full regression, browser, MainActivity atau APK WIP lulus. Baseline 387 tes dan APK 815303 byte berasal dari sebelum integrasi terbaru. Jangan mengklaim APK tersebut dibangun dari WIP sekarang.

Tugas pertama setelah preflight: baca daftar temuan pada checkpoint dan periksa source aktual. Prioritas konkret:

1. Benarkan arah transfer UI `TO_COMPANY`/`TO_PERSONAL`; snapshot pernah membandingkan `INJECT`.
2. Semua transaksi review mempunyai requestId stabil untuk retry; input berubah membatalkan review/quote lama. Tidak ada fallback pencairan loan/policy/coupon yang melewati validasi.
3. Hubungkan UI replay ke `getHistoricalDatasets()`, metadata/provenance aktual, pemisahan save dan resume mode. Hapus pilihan/klaim lisensi hardcoded yang tidak didukung loader.
4. Selesaikan callback statistik dari fill canonical; jangan memproses ulang fill USD dengan FX baru. Simpan nilai/kurs pada kejadian atau dedup sebelum konversi. Jangan menghitung buy fill sebagai transaksi tertutup bernilai nol.
5. Actual buy-fee basis harus tersimpan, dialokasikan tepat pada partial/final sale, dan bertahan melalui save/split/merger/lab. Seeded/legacy holdings tidak diberi fee fiktif.
6. Dietz/cashflow harus membedakan gaji, reward, transfer internal, margin/loan principal, fee dan investasi. Jangan laporkan debt sebagai loss/profit. Statistik harian/mingguan/run, profil, liga dan NPC harus konsisten; denominator tidak sah → tidak tersedia.
7. Tutup integrasi quote/execute company upgrade/policy/capital, daily report history, collection anchors, loan schedule/collateral, fund counterparty/NAV dan nullable league ROI/history.
8. Selesaikan facade/bridge/UI untuk research consensus/altdata/inbox/comparison, lab decisions/history/reset/replay, multi-aset holdings/quote/coupon/maturity, corporate offers/reservation/cancel/settlement/voting dan IPO listing.
9. Beri game timestamp pada mutasi ekonomi; audit caller yang masih memakai wall clock, bukan mengganti semua timer UI secara membabi buta.
10. Jangan menghitung akun pool, issuer, dealer atau 20 NPC liga sebagai aset pribadi atau menawarkannya di account picker pemain.

Periksa kembali karena daftar ini adalah snapshot, bukan instruksi menambahkan duplikat ketika source sudah diperbaiki.

## 8. Inventaris seluruh fitur wajib

Buat satu matriks operasional yang menghubungkan setiap requirement existing/delta ke route/state, handler, bridge, domain, persistence, test, hasil dan revisi. Untuk setiap fitur bedakan **belum dibuat**, **sebagian**, **terintegrasi belum diuji**, **lulus**, **gagal**, **blocked**. N/A hanya dengan alasan applicability yang sah, bukan karena sulit.

Seluruh kelompok berikut tetap wajib sesuai PRD/SPEC:

1. Mode internal dan replay nyata, slot terpisah, clock/session/briefing/close/report/skip/offline.
2. Katalog saham Indonesia dan AS, perusahaan nyata, pencarian/filter/watchlist, IHSG/indeks yang dihitung dari anggota game dan global index explorer sesuai cakupan nyata.
3. NPC investor/analis, order book bersama, likuiditas, makro/berita/peristiwa dan hubungan lintas aset.
4. Chart/timeframe/data table, volume, book/depth/tape, detail instrumen, market/limit/stop/take-profit, partial fill, cancel dan receipt.
5. Kas tersedia/reserved, posisi, valuation, gross/net P/L, fees, ledger, allocation dan kesehatan kekayaan/risiko.
6. Dompet IDR/USD, FX quote/review/expiry/convert/receipt/history.
7. Tutorial, misi karier dan harian/mingguan, pekerjaan/pemulihan, reputasi/promosi, level/XP/achievement.
8. Firma, klien/mandat, akun terpisah, trust/constraints, subscriptions/redemptions, laporan, management/performance fee dan high-water mark.
9. Berita/riset, analis/konsensus, altdata, calendar/inbox/comparison, terminal entitlement dan expiry/manfaat nyata.
10. Obligasi dan komoditas yang dapat dibeli/dijual, harga/kupon/maturity/counterparty/risiko, ditambah saham dan FX.
11. Fund Builder: alokasi, validasi mandat, rebalance dan hubungan status rencana dengan order/fill sebenarnya.
12. Reksa dana unit/NAB tiga strategi, pool backing/kewajiban, subscribe/redeem/reserve/cutoff/fee/cancel/settle/receipt/history.
13. Dividen, split/reverse split, rights issue, buyback dan konsekuensi merger.
14. IPO dan tender/M&A: eligibility, review, reserve, deadline, allocation/refund/cancel/settlement/listing.
15. Voting: record-date entitlement, vote/duplicate/deadline/result/consequence.
16. Risiko/compliance/audit, drawdown/exposure, margin/interest, short borrow/fee/close/squeeze/liquidation, halt/reopening auction.
17. Perusahaan operasional, CEO/pegawai, hire/train/assign/dismiss, gaji/tunggakan, morale/work hours/benefit, paid upgrade, transfer kas dan laporan.
18. Koleksi/properti/kendaraan/seni sesuai katalog minimum, buy/sell, valuation/maintenance/arrears/collateral serta wealth tanpa double count.
19. Liga/rival/leaderboard NPC fiktif, rank/tie/return eligibility, periode/calendar/archive serta actual canonical holdings.
20. Profil/identitas/preferensi persisten, statistik daily/weekly/run/lifetime, gaya investor yang dapat dijelaskan dan optional prestige aman.
21. Stress test, lab isolated, what-if dan replay keputusan dengan riwayat yang dapat diulang tanpa mengubah run utama.
22. Autosave/manual/load/backup/migrasi/recovery, schema/slot identity, lifecycle dan crash/write failure.
23. Seluruh 26 kelompok referensi: shell, wealth, market, chart, trade, allocation, health, loans, ipo, funds, deposits sesuai keputusan D12, missions, achievements, ranks, rivals, leaderboard, collection, empire, employees, morale, empire-invest, prestige, archetype, daily-report, news, profile.
24. Seluruh cakupan FR-01 sampai FR-20, 26 kelompok screen dan tujuan/journey/state desain yang applicable, serta Android packaging sesuai batas penyerahan.

Deposito tidak otomatis menjadi produk baru hanya karena literal APK yang dihapus; ikuti D12 dan requirement yang benar-benar berlaku. Wilayah/indeks dengan data anggota tidak tersedia harus jujur; jangan menampilkan angka statis seolah data indeks aktif. Minimum katalog/balancing mengikuti D01–D16. Aturan yang belum diputuskan boleh ditetapkan secara konsisten, diberi rulesVersion/alasan dan tes, tanpa mengurangi scope.

## 9. Urutan pelaksanaan dan standar minimum per fase

Rencana remediasi R0–R7 tidak menggantikan 14 fase produk. Gunakan dependency existing dan fase berikut untuk memutuskan kelulusan:

| Fase | Keluaran wajib sebelum lulus |
|---|---|
| 00 Preflight | Root/writer/toolchain/baseline/save protection terbukti; bahan dan keputusan dipahami; requirement map mencakup scope, tanpa klaim selesai dari validator bahan |
| 01 Akun, waktu, save | Canonical ownership; uang/reserve/ledger konsisten; dual-rate/pecahan/chunk/lifecycle/migrasi/recovery dan crash retry benar |
| 02 Data/katalog/indeks | Identitas/currency/lot/timezone tepat; indeks dari anggota; dataset/provenance/hash/resolution/rights aktual tervalidasi |
| 03 Pasar/order/halt | Satu matching engine pemain/NPC/klien; price-time/partial/reserve/cancel/trigger/liquidity/auction konsisten, tanpa price move dari unfilledorder |
| 04 UI portrait | Hierarchy referensi/tokens/lima tab, semua route/state applicable dan 7 viewport × normal/200% lolos inspeksi/focus/overflow/CTA |
| 05 Trading/wealth/FX | Review→execute→receipt→save/reload, gross/net P/L/fee basis/FX/ROI/alokasi konsisten seluruh kelas tanpa AUM double count |
| 06 Karier/progres | Earned journey tutorial/fill/misi/promosi/pekerjaan; XP event idempotent; daily/weekly/offline/skip tepat sekali; prestige aman |
| 07 Firma/bisnis | Mandat/AUM/fee/HWM/limits serta perusahaan/staff/policy/cash/operations nyata dan ledger/wealth benar |
| 08 Intelijen | Published-time/entitlement/expiry yang ditegakkan domain; consensus/altdata/inbox/comparison berfungsi dan tidak bocor masa depan |
| 09 Multi-aset/fund | Buy/sell/kupon/maturity/risiko; unit/NAB/backing/counterparty/fee/cutoff/settlement/likuiditas; Fund Builder tetap terpisah |
| 10 Korporasi | Record date/window/reservation/deadline/allocation/refund/approval/settlement/vote/split/listing tepat dan persisten |
| 11 Risiko | Utang/borrow/fee/liquidation/short/compliance/health, ekstrem dan recovery tanpa rescuecash atau penjualan saham terkunci |
| 12 Replay/lab | Dataset nyata legal; anti-future; date/symbol/price model; isolated decisions/stress/replay; save/resume/enddataset benar |
| 13 Integrasi/APK | Seluruh dependency lulus, full regression/browser/performa/review; tracker/source fingerprint/commit/APK/signature/hash cocok, native pending jujur |

Mulai dari WIP yang ada dan kebutuhan dependency, bukan 14 desain baru. Berikan rencana awal ringkas, kemudian segera implementasikan hasil terukur. Kerja independen boleh paralel; dependency gagal tidak dianggap lulus.

## 10. Invariant ekonomi, waktu dan penyimpanan

- Saldo/ledger/reconciliation, cash reserved/available dan posisi konsisten setelah setiap aksi; uang tidak tercipta tanpa sumber ekonomi yang dibukukan. Kredit counterpart/liability harus sah; menambah entry YIELD saja bukan backing.
- Total wealth menggabungkan aset pribadi, net equity perusahaan milik pemain, unit fund dan koleksi, dikurangi liability relevan; tidak menjumlah AUM klien, underlying fund atau company cash dua kali.
- Loan principal menambah cash dan debt sama besar; fee/interest biaya, bukan principal. Collateral canonical tidak dapat dijaminkan/dijual dua kali; pelepasan/agunan lewat aturan dan receipt.
- Subscribe/redeem tidak mengubah NAB/unit melalui external flow selain fee/rounding sah. Semua unit/register/pool/counterparty/receivable/liability dapat direkonsiliasi. Liquidity shortage tidak dilabeli settled tanpa dana.
- Preview tidak memutasi. Konfirmasi memakai account/owner/requestId/version/asOf yang benar; stale/double-submit/retry/crash tidak menggandakan mutasi. ID dipakai input berbeda ditolak.
- Harga order dan chart berasal game fills/riwayat yang sah, tanpa candle dekoratif. Fee/slippage/marketimpact tidak dihitung dua kali.
- Kalender aktif/offline/Skip melalui pipeline/event ordering sama; RNG bergantung canonical tick/seed tersimpan, bukan jumlah browser callback. Pecah pada batas relevan dan proses seluruh interval tanpa silent cap.
- Event tepat sekali dengan ID persisten. Save di tengah batch dan resume tidak melewati atau mengulang committed segment. Jam perangkat offline punya keterbatasan; jangan mengklaim anti-cheat terpercaya tanpa sumber tepercaya.
- Error save tidak boleh menghasilkan receipt sukses palsu atau state setengah jalan. Gunakan atomic persistence/backup/recovery atau transaksi in-memory yang dapat dibuktikan rollback/retry sesuai desain existing; jangan menambah transaction framework spekulatif.

## 11. Replay dan data historis

Periksa provenance `android/app/src/main/assets/data/historical/replay_provenance.json`, checksum dan sumber primer yang ditautkan. Feed Nasdaq WIKI dan mirror pinned adalah kandidat bukti yang sudah didokumentasi, bukan izin menyalin dataset lain. Gratis diunduh, lisensi kode repositori dan lisensi data upstream adalah hal berbeda. Verifikasi primary rights/attribution, raw/adjusted basis, symbol/currency/timezone/resolution, coverage/gaps, checksum dan transformasi. Tidak membeli layanan.

Loader baru menyediakan US:AAPL 61 daily sessions; jangan mengklaim seluruh IHSG/global/intraday sudah dicakup. Dataset IDX/Yahoo legacy tidak boleh masuk paket distribusi hanya karena loader menolaknya. Quarantine/archive/exclude dari APK dengan manifest jelas setelah memastikan path/backup, tanpa menghapus bukti audit lama.

Inisialisasi semua domain memakai epoch dataset sebelum ledger/career/collection/fund/league anchors. Reset seluruh reference price agar high/low/previous close tidak memakai data modern. Tidak menampilkan fundamental modern sebagai historis. Publish daily OHLCV hanya pada model close; jelaskan opening-reference/spread/volume capacity dan bahwa jam model bukan rekonstruksi tick bursa nyata. Tidak memperkirakan high/low path atau memakai bar lain untuk mengisi simbol yang salah.

Semua order/pending trigger/resting order harus mematuhi tanggal bar, instrumen, waktu sesi/weekend/gap/enddataset sebelum registration/mutation. Refresh quotes tidak self-cross, tidak memakai modal/stock tak terbatas dan tidak menduplikasi order tiap callback. Suppress synthetic future news/calendar/corporate events di mode historis.

Pisahkan slot primary/replay/lab, switch/resume aman, validasi target sebelum mengganti state aktif, error/backup recovery dan hasil akhir dataset yang jelas. Jangan membekukan permainan tanpa cara kembali ke run utama atau menimpa progres mode lain.

## 12. Pengujian developer, pengujian akhir dan pengulangan

Sebelum menyatakan fitur terintegrasi, telusuri UI→handler→bridge→domain→ledger/save→reload dan uji positive, denied access, empty, boundary, stale, duplicate, insufficient funds, failed save/recovery sesuai risiko. Buat test yang gagal pada perilaku lama dan lulus sesudah root fix. Reuse runner/framework existing; tidak membuat banyak tests yang hanya mencerminkan implementation atau mengulang no-op.

Urutan perbaikan: reproduksi dengan evidence → diagnosis caller/shared state → root fix minimum → test relevan → regression integrasi → review → tutup temuan. Jangan menghapus test gagal, mengubah expected agar menerima bug, melemahkan guard, memalsukan status atau mengganti domain dengan mock. Fixture extreme/rank boleh untuk isolasi cabang, tetapi tidak menggantikan earned progression.

Setelah fitur lengkap, jalankan:

1. **Full JVM/bridge regression** pada source akhir, bukan hasil subset lama. Catat compile/task/exit, kelas/tes/failure/error/skipped dan revision. Tes yang sengaja skipped tetap belum memberikan bukti requirement.
2. **Urutan kritis minimal 10 variasi ×100 aksi bermakna** untuk uang/order/waktu/save, seed dan aksi dapat diulang, invariant sesudah tiap aksi. Sertakan callback besar vs kecil, multi-period, proses putus dan failed write/reload. Jangan mengganti 100 aksi dengan 100 query/no-op atau menyatakan test harness lulus sebagai lulus game.
3. **Browser dengan host Java nyata** dan save QA baru. Baca runner existing, guard QA dan bantuan sebelum pakai. Synchronous stub/mock visual berlabel tidak meluluskan fitur produksi. Jangan menjalankan runner reset terhadap preview pribadi.
4. **Semua layar/state applicable × tujuh viewport** 320×568, 360×640, 390×844, 412×915, 430×932, 768×1024, 1440×900 × teks normal dan font scale 200% nyata. Inventaris N/A beralasan, screenshot dan inspeksi visual; bukan root-tab screenshot saja.
5. **UX/accessibility:** label/angka tidak terpotong/overlap, konten terbawah tercapai, scroll wajar, tidak ada horizontal overflow halaman tak sengaja, nav/CTA tetap dapat diakses, target sentuh setara48dp, form/focus/draft aman saat snapshot refresh, keyboard browser, modal/back/focus restore, contrast/status selain warna, reduced motion, selected/disabled/locked/error/loading yang jujur.
6. **Performa:** ≥30 sampel setiap jenis aksi, raw timings/environment/cache/source revision; p95 aksi biasa ≤300ms, semua sampel navigasi yang disyaratkan ≤1detik, feedback proses berat benar-benar ter-render ≤300ms dan event loop responsif. Pisahkan query ringan, submit, mode switch/catchup; jangan agregasi untuk menutup aksi lambat atau membuang outlier tanpa alasan sah. Target ini bukti host/browser, bukan FPS/RAM perangkat.
7. **Review fase dan final:** reviewer berbeda bila tersedia; seluruh temuan termasuk minor ditutup, lalu koordinator menguji hasil gabungan. Perubahan sesudah tes memerlukan pemeriksaan ulang yang terdampak.

Bila alat browser dikatakan tidak tersedia, periksa runtime/dependency terpasang dan jalur yang diizinkan dahulu. Jangan menginstal ulang besar-besaran atau mengakali larangan lingkungan. Jika tetap terhalang, simpan diagnosis dan lanjutkan bagian independen; gate browser tetap NOT_TESTED/BLOCKED dengan alasan, bukan PASS.

## 13. Journey penerimaan wajib

1. Newgame QA → tutorial → fill sah → misi/XP → promosi earned → mandat → laporan/fee → firma/perusahaan. Tidak memakai fixture kaya untuk membuktikan keseimbangan progres.
2. Perusahaan → staff/CEO/hire/train/assign/dismiss → policy review → paid expansion → payroll/revenue/arrears → transfer/withdraw → report → offline/skip → wealth/save benar.
3. Beli koleksi → maintenance/valuasi → pilih agunan → loan quote/review → cair → accrual/late/restructure/prepay → lepas agunan → jual koleksi; tidak ada uang/reputasi gratis dari retry.
4. Ketiga fund → subscribe/reserve → cutoff/NAB/settle → unit/fee/receipt → redeem/cancel/shortage → reload; backing/counterparty sama dengan angka wealth.
5. Bond/gold → quote/review/buy → mark/kupon/maturity atau sell → fee/receipt/wealth/save; FX dan akun tujuan benar.
6. Rights/buyback/merger/IPO → record/window/review/reserve → cancel atau allocate/settle/refund/listing; frozen vote rights → deadline/result/consequence; split/reverse order/reserve/basis aman.
7. Kalender melewati hari/minggu → misi/claim/badge/liga/rival/archive → reload; hasil setara aktif/offline/skip, return tidak salah karena gaji/loan/transfer.
8. Terminal → subscribe gamecurrency → consensus/inbox/compare/altdata → locked/expiry/renew/error; published timestamps dan benefits nyata, tidak bocor masa depan.
9. Lab isolated → keputusan/history/reset/replay → hasil deterministik, mainrun unchanged. Stress/kerugian/debt → pekerjaan/restrukturisasi → recovery, tanpa gameover/rescuecash otomatis.
10. Replay nyata → modal/mode terpisah → FX/historical symbol → chart/trade/report/no-future → gap/weekend/enddataset → save/resume → kembali mainrun tanpa kehilangan progres.
11. Profil/statistik/archetype menampilkan hasil yang sesuai ledger/wealth dengan sample/denominator/confidence/period benar, termasuk empty/no-closed-trade/nonpositiveequity.
12. Prestige opsional → preview reset/retained/bonus/blockers → cancel atau backup+confirm → era baru/run/lifetime konsisten; duplicate/crash/backupfailed tidak reset dua kali.

## 14. Ketekunan, checkpoint dan pelaporan

Lanjut otomatis dari satu hasil terukur ke berikutnya. Jangan meminta approval rutin untuk bugfix, detail aturan yang sudah didelegasikan, tes atau commit lokal. Jangan berhenti karena durasi, jumlah iterasi/giliran, penghematan token, satu tes hijau, APK terbentuk atau rencana selesai. Tetap hormati STOP dan batas wewenang.

Setelah dua kegagalan sama tanpa bukti/diagnosis baru, laporkan `PERLU PERHATIAN`, bukti dan pendekatan berbeda. Setelah tiga, pindah ke tugas independen atau catat blocker nyata jika memerlukan input/perubahan eksternal. Jangan menjalankan command sama tanpa alasan baru atau menganggap timeout observasi sebagai proses mati. Re-poll handle yang hidup; jangan memulai writer kedua.

Checkpoint setelah batas kerja aman dan sebelum context habis/interupsi: waktu/root/branch/HEAD/dirty fingerprint, ownership/proses/port/save QA, perubahan, test yang benar-benar dijalankan dan revision, temuan terbuka, status fase, hash save pribadi, bukti, blocker serta **next_action dan command konkret**. Source yang belum dikompilasi/diuji wajib disebut WIP. Jangan menamai file 'FINAL' sebagai bukti completion.

Gunakan `Work/CHECKPOINT-LONGTASK-EXECUTION-2026-10-21.md` dan direktori evidence baru yang jelas; jangan menimpa audit independen/log lama. Rekonsiliasi STATUS.json/matriks sesuai bukti. Laporan chat ringkas: lulus x/14, fitur selesai/berjalan/pending, hasil tes, temuan, next_action. Jumlah file atau test bukan persentase produk.

Jika goal API tersedia dan pemilik mengaktifkan long task, pertahankan objective penuh. Mark complete hanya setelah audit completion, bukan karena checkpoint dibuat; paused hanya atas STOP/pause pemilik; blocked setelah threshold hambatan nyata sesuai alat. Jangan membuat automation global sebagai pengganti sesi.

## 15. Commit, APK dan keluaran akhir

Setelah review/regresi relevan lulus, commit lokal selektif hanya source/test/docs tugas yang siap. Pertahankan local changes lain, jangan commit secrets, save pribadi, dataset tanpa hak atau binary besar. Pastikan build memakai keseluruhan source final yang diuji; dirty changes wajib dilaporkan. Jangan menganggap commit lama otomatis mencakup WIP.

Build APK baru melalui Gradle existing. Catat perintah/exit/log, waktu, revision/fingerprint, appID/version/minSdk/targetSdk, debug/release status, signing certificate digest, ukuran dan SHA-256. Periksa signature, packaged assets/data/provenance dan source correspondence. Jangan memasukkan file data legacy tak terverifikasi; jangan menyebut debug APK sebagai store release. APK lama tidak menjadi hasil baru hanya karena disalin/rename.

Keluaran akhir wajib:

- Peta fitur seluruh mandatory: status, route, kontrak, persistence, test/evidence/reviewer/source revision dan alasan N/A/blocked.
- Daftar perbaikan dan fitur baru beserta alur yang dapat dimainkan.
- Fase lulus x/14 dan temuan audit yang ditutup dengan reproduksi setelah fix.
- Full test results, raw browser/performance data, screenshot/state matrix, journey earned, crash/save/critical sequence proof.
- Laporan rulesVersion/asumsi yang diputuskan, dataset rights/provenance/basis/gaps dan batas model.
- Checkpoint akhir yang dapat dipakai agen lain, commit lokal dan daftar dirty changes tersisa.
- APK baru beserta metadata/checksum/signature, panduan menjalankan dan pemulihan save.
- Hash save pribadi sebelum/sesudah dan gate Android native PENDING yang tidak dicampur dengan replay/data blocker.

**Audit completion:** turunkan seluruh requirement dari sumber keputusan; periksa evidence sesuai luas klaim satu per satu. Evidence lemah/tidak langsung/hilang berarti belum lulus. Seluruh bug diketahui termasuk minor harus ditutup dan source integrasi akhir diuji. Jika dataset/lingkungan menghalangi requirement mandatory, produk tetap PARTIAL/BLOCKED; jangan mengecilkan scope atau menyembunyikan blocker di tabel perangkat. Jika seluruh scope domain/browser/build yang diminta terpenuhi sementara perangkat di luar scope belum diuji, nyatakan batas penyerahan APK secara tepat tanpa mengklaim native lulus.

Mulai sekarang: verifikasi checkout/writer/save, baca checkpoint dan spesifikasi, kompilasi WIP, petakan celah, tuntaskan fitur dan integrasi shared terlebih dahulu, lalu langsung jalankan pengujian menyeluruh, perbaiki semua temuan dan hasilkan APK beserta bukti akhir. Jangan berhenti pada balasan rencana.
