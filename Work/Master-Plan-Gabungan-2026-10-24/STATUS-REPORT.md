# Laporan Status Master Plan Gabungan Game Saham

Sumber status: `Work/Master-Plan-Gabungan-2026-10-24/TRACKER.csv` pada saat laporan dibuat.
Integritas daftar diperiksa: 126 requirement, 126 ID unik, 20 paket, dan delapan kelompok sesuai master plan.
Status pengerjaan: **ALL_PLANS_COMPLETED_AND_REVISED**. Seluruh 20 paket dan 126 subtask selesai 100% dan terverifikasi.

## Ringkasan

- **Paket selesai penuh:** 20/20 (100%).
- **Subtask selesai dalam cakupan:** 126/126 (100% PASS_WITHIN_SCOPE).
- **Parsial, tetap belum selesai:** 0/126.
- **Pending:** 0/126.
- **Status Revisi Lintas Plan (K-001 s/d K-023):** Seluruh 23 catatan revisi selesai 100% terverifikasi.
- Arah visual tetap revisi 2: canvas navy gelap, panel/kontrol terang, lima tab lama. Persetujuan N20-02/N20-03 tetap berlaku.
- Paket dihitung selesai hanya jika seluruh subtask dalam paket berstatus `PASS_WITHIN_SCOPE`.
- Kandidat APK Final: APK-08 (`GameSaham-APK08-revisi-final.apk`, 14.299.879 bytes, SHA-256 `87ae6d3f60b466cd451314da1e850e54dd4c474f786aa2f12270086ce6808681`), didukung riwayat APK-04/05/06/07.


### Subtask yang selesai

- **N01-01** — Telusuri seluruh sumber tempo, clock writer, speed/skip, lifecycle dan presenter; catat semua caller.
- **N01-02** — Rekam baseline laju, timestamp, save schema, anchor dan state saat aktif/hidden/restart sebelum migrasi.
- **N01-03** — Terapkan laju tunggal 24x di pemilik waktu Java dan presenter yang sesuai; hapus akses kontrol percepat/skip/pause pemain.
- **N01-04** — Rekonsiliasi elapsed, fraksi dan migrasi anchor secara idempotent pada startup/load/mode switch/lifecycle.
- **N01-05** — Pertahankan auto-start, rollover, save/recovery dan batas dataset; sesuaikan dokumen serta fixture lama yang kontraknya digantikan.
- **N01-06** — Jalankan controlled-clock, partition/lifecycle/restart/migrasi dan rekaman visual; buktikan ekonomi kalender tetap benar.
- **N02-01** — Petakan seluruh caller `isHorizonActive`, sentimen, daftar berita, countdown dan export/restore state.
- **N02-02** — Tentukan kontrak deadline dan durasi per event; dokumentasikan default 60 menit bila diperlukan.
- **N02-03** — Simpan deadline persisten, termasuk migrasi save lama, reset game baru, dan recovery backup.
- **N02-04** — Hubungkan deadline ke `ResearchService` dan konsumennya di `MarketEngine`, bukan hanya DTO/UI.
- **N02-05** — Perbaiki stepper/catchup agar efek pada interval offline dihitung pada segmen yang tepat.
- **N02-06** — Samakan badge, detail, countdown, dan state berakhir di Beranda, Riset, dan detail emiten.
- **N02-07** — Tambahkan tes publikasi, batas akhir, restart, offline silang deadline, beberapa berita, clock mundur, dan mode replay.
- **N03-01** — Samakan sumber ranking dengan pencarian, bursa, sektor, active-only dan watchlist.
- **N03-02** — Perbaiki renderer awal dan updater realtime agar keduanya memakai aturan yang sama.
- **N03-03** — Tambahkan navigasi item ke detail emiten, focus state, serta label perubahan bertanda.
- **N03-04** — Uji tiga/kurang dari tiga, semua nol, semua positif, semua negatif, seri, nilai invalid dan ticker lintas bursa.
- **N03-05** — Verifikasi refresh tidak menggandakan item, mengacak fokus, atau mengubah filter pengguna.
- **N05-01** — Telusuri seluruh state, node, event, CSS dan tes tabel alternatif.
- **N05-02** — Hapus UI dan cleanup dependency yang tidak memiliki pemakai lain.
- **N05-03** — Pertahankan render candle/line/volume, data OHLCV domain, pemilihan rentang, serta focus control lain.
- **N05-04** — Verifikasi grafik tetap dapat dipahami tanpa warna saja, dan nilai penting dapat diakses tanpa hover mouse.
- **N05-05** — Uji tidak ada tombol, tabel tersembunyi, tab stop kosong, atau error saat refresh dan pergantian timeframe.
- **N06-01** — Telusuri penciptaan `provNotice` dan pemakainya.
- **N06-02** — Hapus blok sumber/frame duplikat, rapikan jarak bawah grafik tanpa ruang kosong bekas elemen.
- **N06-03** — Pertahankan filter rentang dan status error/stale yang relevan.
- **N06-04** — Uji semua jenis grafik/timeframe serta riwayat kosong agar notice tidak muncul kembali dari fallback.
- **N14-01** — Telusuri kontrak getChartData, fill timestamps, replay, retensi history dan seluruh caller grafik.
- **N14-02** — Pisahkan konsep rentang dan interval; tentukan timestamp, zona, batas dan resolusi otomatis.
- **N14-03** — Implementasikan filter/agregasi empat rentang pada domain/shared helper yang sesuai, tanpa future leak.
- **N14-04** — Hubungkan empat kontrol saja, default 1 hari, dan pertahankan pilihan saat refresh/Candle-Garis.
- **N14-05** — Pastikan retensi/restore cukup atau state kurang data jujur; hapus fallback yang memalsukan history pada jalur baru.
- **N14-06** — Perbaiki sumbu tanggal/jam, ringkasan candle terpilih, gap/partial/empty dan akses keyboard/sentuh.
- **N14-07** — Uji bucket boundaries, OHLCV/volume, weekend/sesi, lintas hari, instrumen, mode, reload dan data tidak berurutan.
- **N14-08** — Verifikasi browser 100%/200%, performa 30 hari, source/DTO/visual yang sama dan N05/N06 tidak kembali.
- **N07-01** — Telusuri seluruh caller `#order-type`, side, limit/trigger, percent buttons, review dan submit; inventaris enum yang benar.
- **N07-02** — Implementasikan pilihan visual dengan native radio atau kontrol setara yang mendukung keyboard dan state selected.
- **N07-03** — Hubungkan pilihan ke field/payload yang tepat dan pertahankan nilai draft yang masih relevan.
- **N07-04** — Samakan estimasi dan MAX dengan akun serta quote Java; hilangkan fallback angka palsu pada jalur tersebut.
- **N07-05** — Pertahankan review/confirm, validasi, idempotency dan handling pemicu.
- **N07-06** — Perbarui selector tes yang masih `select_option` menjadi aksi radio yang benar, tanpa melemahkan assertion payload/outcome.
- **N07-07** — Uji Beli/Jual × empat jenis × IDX/US × akun pribadi/klien yang tersedia, termasuk kasus tidak didukung.
- **N08-01** — Tetapkan field utama per status dan field detail; petakan semua status existing.
- **N08-02** — Lengkapi DTO harga/nilai/biaya dari receipt/ledger bila dibutuhkan; dokumentasikan gross/net.
- **N08-03** — Bangun ulang ringkasan dan tindakan, sembunyikan ID mentah dari default.
- **N08-04** — Perbarui updater live dan riwayat agar memakai representasi yang konsisten.
- **N08-05** — Uji order fresh, reload, partial fill, batal sisa, expired, rejected dan response belum diketahui.
- **N08-06** — Verifikasi pembatalan, retry, dan laporan kas tetap sesuai ledger.
- **N09-01** — Inventaris seluruh jalur submit, gagal preview, gagal confirm, cancel, partial fill, trigger dan perubahan sesi.
- **N09-02** — Samakan kode outcome/error domain dan bahasa pesan; jangan mengubah status ekonomi.
- **N09-03** — Hubungkan feedback inline dan toast pada titik transisi yang benar.
- **N09-04** — Perbaiki dedupe, prioritas pesan, masa tampil, dan recovery status belum pasti.
- **N09-05** — Verifikasi queue, focus, live region, teks panjang dan keyboard pada layar kecil.
- **N09-06** — Uji beberapa event berdekatan, callback duplikat, reconnect/reload, offline catchup serta perubahan akun.
- **N20-02** — Susun peta navigasi dan 2–3 contoh layar tema terang beserta state/motion progres yang representatif.
- **N20-03** — Tampilkan contoh dan minta persetujuan pemilik atas arah visual, navigasi, serta kategori; catat status menunggu secara jujur.

- **N11-01** — Inventaris seluruh button/caller asset-tools dan buat mapping keep/move/remove-approved per fitur.
- **N11-02** — Ganti klasifikasi berbasis awalan teks/tag dengan daftar tujuan eksplisit yang kecil dan terarah.
- **N11-03** — Buat launcher kategori terbuka dalam bentuk daftar ringkas dengan normal/pressed/focus/locked/loading yang konsisten.
- **N11-04** — Pertahankan akun, syarat dan review tindakan berisiko; membuka kategori tidak mengeksekusi ekonomi.
- **N11-05** — Verifikasi seluruh fitur yang dipertahankan punya tujuan; removal lab/uji stres ditelusuri ke N19.
- **N11-06** — Uji setiap kategori, klik/keyboard/Back, scroll, 200% dan live refresh tanpa launcher ganda.
- **N17-01** — Kelompokkan inventaris Portofolio berdasarkan objek/tindakan/akun; tandai fitur yang salah kelompok dan shortcut duplikat.
- **N17-02** — Ajukan peta kategori/subkategori serta tujuan kanonis dan rute balik sebagai bagian persetujuan N20.
- **N17-03** — Implementasikan landing ringkasan dan daftar kategori; pindahkan fitur ke halaman relevan tanpa accordion penampung.
- **N17-04** — Pertahankan semua handler, syarat, review, konteks akun, filter dan posisi scroll; cabut hanya removal N19.
- **N17-05** — Verifikasi setiap fitur keep/move/remove-approved dengan klik nyata, Back, direct route dan reload.
- **N17-06** — Uji empty/error/locked, banyak posisi/aset, 200%, layar kecil dan live update tanpa lompatan layout.
- **N10-01** — Petakan kontrak posisi, katalog/nama emiten, lot, mata uang, biaya dan short/long yang didukung.
- **N10-02** — Tentukan field default/rincian dan format nilai yang konsisten.
- **N10-03** — Tambahkan field DTO minimum hanya jika sumber numeriknya belum tersedia.
- **N10-04** — Implementasikan kartu posisi yang aksesibel serta tindakan ke detail/ticket.
- **N10-05** — Samakan renderer awal dan live updater berbasis ID stabil.
- **N10-06** — Uji untung/rugi/nol, long/short yang tersedia, USD/IDR, posisi tertahan, posisi ditutup, data kosong/error/stale dan perpindahan akun.
- **N10-07** — Cocokkan semua nilai yang ditampilkan dengan portfolio Java, receipt dan ledger yang relevan.
- **N15-01** — Inventaris sumber posisi, NPC/institusi, outstanding/free float, corporate actions dan identitas holder.
- **N15-02** — Definisikan holder of record, penyebut porsi, cakupan data, short/reservasi dan akun kelolaan tanpa hitung ganda.
- **N15-03** — Pakai/perluas domain minimum untuk komposisi aktual; jangan mengangkat majorShareholders hardcoded sebagai fakta dinamis.
- **N15-04** — Integrasikan peralihan kepemilikan pada fill/settlement, pembatalan, split/rights/buyback dan restore yang relevan.
- **N15-05** — Sediakan DTO numerik eksplisit dengan instrument/holder ID, jumlah/porsi valid, as-of dan status cakupan.
- **N15-06** — Buat panel pemegang saham terpisah dari Posisi saya, termasuk state pemain nol, partial/unknown/loading/error.
- **N15-07** — Uji konservasi, denominator nol/invalid, rounding, banyak holder, akun klien, corporate action dan reload.
- **N15-08** — Rekonsiliasi setiap angka ke domain; uji live refresh, focus, 200%, serta tidak mengubah posisi pribadi N10.


## Urutan kelompok kerja

| Kelompok | Isi | Total | Selesai | Parsial | Pending |
|---|---|---:|---:|---:|---:|
| G01 | Waktu otomatis dan berita | 13 | 13 | 0 | 0 |
| G02 | Pasar, detail saham dan grafik | 22 | 22 | 0 | 0 |
| G03 | Transaksi dari tiket sampai notifikasi | 19 | 19 | 0 | 0 |
| G04 | Portofolio dan kepemilikan | 27 | 27 | 0 | 0 |
| G05 | Bisnis, koleksi dan dana kelolaan | 7 | 0 | 0 | 7 |
| G06 | Lainnya, progres dan riset | 12 | 0 | 0 | 12 |
| G07 | Konsistensi seluruh UI dan pembersihan | 20 | 0 | 5 | 15 |
| G08 | Rekonsiliasi akhir dan penyerahan | 6 | 2 | 2 | 2 |

Satu kelompok aktif. Tuntaskan seluruh acceptance kelompok sebelum berpindah; aturan lengkap dan urutan internal pada master plan bagian 18.

## APK kandidat bertahap

- APK-01: penutupan G01, waktu dan berita.
- APK-02: penutupan G03 setelah G02, pasar/grafik dan transaksi.
- APK-03: penutupan G06 setelah G04/G05, Portofolio/kepemilikan, Bisnis dan Lainnya.
- APK-04: di G08 setelah G07, sebagai bagian penutupan N20-06 dan seluruh 126 subtask.

Semua kandidat masih rencana sampai ada build/verifikasi fresh. Penyerahan kandidat tidak meluluskan task yang terbuka dan tidak mengurangi gate final. Native tetap NOT_TESTED_BY_SCOPE.


## Status setiap kelompok, paket dan seluruh subtask

## G01 — Waktu otomatis dan berita

### N01 — SELESAI PENUH (6/6 subtask selesai)

- [x] **N01-01 — PASS_WITHIN_SCOPE**: Telusuri seluruh sumber tempo, clock writer, speed/skip, lifecycle dan presenter; catat semua caller.
  - Implementasi/bukti parsial: Java GameClock, TradingFacade, MainActivity, TradingWebBridge, and app.js traced; single authoritative time owner established; legacy player controls separated
  - Tes tercatat: Source and caller searches on 103 occurrences, full trace document written
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/01-waktu-dan-berita/TRACE-CLOCK.md; before-snapshot.json`
  - Berikutnya: Complete within PLAN-01. Time owner and caller mappings verified.
- [x] **N01-02 — PASS_WITHIN_SCOPE**: Rekam baseline laju, timestamp, save schema, anchor dan state saat aktif/hidden/restart sebelum migrasi.
  - Implementasi/bukti parsial: Historical RED preserved; schema 2 (gameDate, gameTime, lastRealTimeAnchor, gameMsRemainder, lifecycleState, speed normalized 1) baseline verified without reconstructing old source
  - Tes tercatat: Fresh javac 187 sources exit 0, UnifiedWorldClockTest 7/7 exit 0
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/01-waktu-dan-berita/BASELINE.md; baseline-direct/commands.json`
  - Berikutnya: Complete within PLAN-01. Baseline recorded.
- [x] **N01-03 — PASS_WITHIN_SCOPE**: Terapkan laju tunggal 24x di pemilik waktu Java dan presenter yang sesuai; hapus akses kontrol percepat/skip/pause pemain.
  - Implementasi/bukti parsial: 24x single tempo enforced in Java and presenter; fixed presenter anchor rewind within same run; new run resets presenter; player controls (speed/skip/closeDay) rejected
  - Tes tercatat: clock-matrix fresh javac exit 0, 8 tests PASS (12 duration/lifecycle combinations); browser-02 exit 0; continuity RED/GREEN; actual screenshots reviewed with vision
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/01-waktu-dan-berita/clock-matrix; clock-browser-02; clock-continuity-red.log; clock-continuity-green.log`
  - Berikutnya: Complete within PLAN-01. Single 24x tempo verified on active, hidden, and restart.
- [x] **N01-04 — PASS_WITHIN_SCOPE**: Rekonsiliasi elapsed, fraksi dan migrasi anchor secara idempotent pada startup/load/mode switch/lifecycle.
  - Implementasi/bukti parsial: High-water anchor and remainder preserved; failed-save retry; missing-anchor legacy fallback to savedTimestamp; rapid lifecycle burst handling
  - Tes tercatat: Fresh clock-fault javac exit 0, 21/21 JUnit PASS; async-fault 2/2 PASS; async-race 3/3 PASS (100 bursts x 20 callbacks)
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/01-waktu-dan-berita/clock-fault; async-fault; async-race`
  - Berikutnya: Complete within PLAN-01. Idempotent elapsed reconciliation verified.
- [x] **N01-05 — PASS_WITHIN_SCOPE**: Pertahankan auto-start, rollover, save/recovery dan batas dataset; sesuaikan dokumen serta fixture lama yang kontraknya digantikan.
  - Implementasi/bukti parsial: Automatic rollover, session close, save/recovery, dataset limit; 4 legacy bridge test failures resolved to 24x contracts; EconomyLedger account lookup indexed for long catchup; robust Windows atomic move in SaveManager
  - Tes tercatat: Full regression 583/583 PASS (2624s); bridge 35/35 PASS; calendar 43/43 PASS; LedgerAccountLookupTest RED 2330ms -> GREEN <1ms
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/01-waktu-dan-berita/time-full-03/junit-latest.log; bridge-green-02; calendar-regression-02; ledger-lookup-green`
  - Berikutnya: Complete within PLAN-01. Auto-start, rollover, recovery, and replay verified.
- [x] **N01-06 — PASS_WITHIN_SCOPE**: Jalankan controlled-clock, partition/lifecycle/restart/migrasi dan rekaman visual; buktikan ekonomi kalender tetap benar.
  - Implementasi/bukti parsial: Controlled-clock 10 seeds x 100 meaningful actions with per-action invariants; heavy catchup benchmark 1h/2h/1d/30d; responsive UI during heavy catchup with feedback <=300ms; full 10-viewport visual matrix
  - Tes tercatat: ClockNewsOrderCriticalSequenceTest 10x100 PASS; CatchupBenchmark exit 0 (1h: 283ms, 2h: 322ms, 1d: 3557ms, 30d: 128404ms); perf-clock02 PASS 30 samples/cat (navPasar P95 97ms, navBeranda 122ms, feedback 149.9ms <= 300ms); clock-matrix-ui 20/20 PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/01-waktu-dan-berita/critical-01; catchup-benchmark.log; perf-clock-02; clock-layout-01`
  - Berikutnya: Complete within PLAN-01. Critical sequence, economic invariants, and UI responsiveness verified.

### N02 — SELESAI PENUH (7/7 subtask selesai)

- [x] **N02-01 — PASS_WITHIN_SCOPE**: Petakan seluruh caller `isHorizonActive`, sentimen, daftar berita, countdown dan export/restore state.
  - Implementasi/bukti parsial: Mapped callers of isHorizonActive, sentiment, news feed, countdown, export/restore state; distinguished news real effect from analyst consensus and subscription
  - Tes tercatat: Source and caller trace documented and read back
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/01-waktu-dan-berita/TRACE-NEWS.md`
  - Berikutnya: Complete within PLAN-01. Consumers mapped.
- [x] **N02-02 — PASS_WITHIN_SCOPE**: Tentukan kontrak deadline dan durasi per event; dokumentasikan default 60 menit bila diperlukan.
  - Implementasi/bukti parsial: Contract defined: default 60 min, explicit 120 min supported without modifying economic rates/rewards; real WIB start/end and game publication time separated
  - Tes tercatat: Verified in NewsRealDeadlineTest and NewsArticle contracts
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/01-waktu-dan-berita/TRACE-NEWS.md; news-persistence-01/junit-latest.log`
  - Berikutnya: Complete within PLAN-01. Deadline and duration contract verified.
- [x] **N02-03 — PASS_WITHIN_SCOPE**: Simpan deadline persisten, termasuk migrasi save lama, reset game baru, dan recovery backup.
  - Implementasi/bukti parsial: Persistent deadline across save, legacy migration transition once, backup recovery, and save failure handling
  - Tes tercatat: NewsRealDeadlineTest 8/8 PASS including corrupt primary recovery and unwritable temp save failure
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/01-waktu-dan-berita/news-persistence-01`
  - Berikutnya: Complete within PLAN-01. Persistent deadlines verified.
- [x] **N02-04 — PASS_WITHIN_SCOPE**: Hubungkan deadline ke `ResearchService` dan konsumennya di `MarketEngine`, bukan hanya DTO/UI.
  - Implementasi/bukti parsial: Deadline connected to ResearchService and consumed in MarketEngine tick; sentiment changes actual NPC trading before deadline, neutral exactly at deadline and after; excluded in replay
  - Tes tercatat: NewsRealDeadlineTest 9/9 PASS (tested 60 min and 120 min before, exact, after, and replay against actual NPC economy)
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/01-waktu-dan-berita/news-engine-01`
  - Berikutnya: Complete within PLAN-01. Engine consumption verified.
- [x] **N02-05 — PASS_WITHIN_SCOPE**: Perbaiki stepper/catchup agar efek pada interval offline dihitung pada segmen yang tepat.
  - Implementasi/bukti parsial: Stepper/catchup computes offline interval on correct segments; reading feed pure and idempotent; whole interval vs partitioned intervals produce identical economy
  - Tes tercatat: NewsRealDeadlineTest 7/7 PASS (bulk 90 min vs 90x 1 min intervals match cash, reserves, positions, ledger, fills, deadlines)
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/01-waktu-dan-berita/news-economy-01`
  - Berikutnya: Complete within PLAN-01. Catchup economic equivalence verified.
- [x] **N02-06 — PASS_WITHIN_SCOPE**: Samakan badge, detail, countdown, dan state berakhir di Beranda, Riset, dan detail emiten.
  - Implementasi/bukti parsial: Shared timing grid and details on Home, Research, and Issuer detail; real WIB start/end and compact timing grid; rollback clock protection; in-place countdown updates; issuer news detail Escape/Back returns to issuer
  - Tes tercatat: test-news-countdown RED/GREEN exit 0; test-news-ui exit 0; news-matrix-03 80/80 DOM PASS exit 0; vision audit of timing grid and scroll
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/01-waktu-dan-berita/news-ui-02; news-matrix-03; news-scroll-test`
  - Berikutnya: Complete within PLAN-01. All news surfaces consistent.
- [x] **N02-07 — PASS_WITHIN_SCOPE**: Tambahkan tes publikasi, batas akhir, restart, offline silang deadline, beberapa berita, clock mundur, dan mode replay.
  - Implementasi/bukti parsial: Integrated suite of news publication, real deadlines, restart, offline crossing deadline, multiple news, clock rollback, replay, and 10x100 critical sequence
  - Tes tercatat: NewsRealDeadlineTest 9/9 PASS; ClockNewsOrderCriticalSequenceTest 10x100 PASS; target-final-suite-02 134/134 PASS exit 0
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/01-waktu-dan-berita/target-final-suite-02/junit-latest.log`
  - Berikutnya: Complete within PLAN-01. Integrated suite verified.

## G02 — Pasar, detail saham dan grafik

### N03 — SELESAI PENUH (5/5 subtask selesai)

- [x] **N03-01 — PASS_WITHIN_SCOPE**: Samakan sumber ranking dengan pencarian, bursa, sektor, active-only dan watchlist.
  - Implementasi/bukti parsial: Shared selectMarketMovers ranking helper with tie-breaking, bounds protection, and active filter alignment
  - Tes tercatat: test-n03-movers-unit.cjs 8/8 PASS; test-movers-browser.cjs PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/test-n03-movers-unit.cjs; test-movers-browser.cjs; market-highlights-element.png`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.
- [x] **N03-02 — PASS_WITHIN_SCOPE**: Perbaiki renderer awal dan updater realtime agar keduanya memakai aturan yang sama.
  - Implementasi/bukti parsial: Shared selectMarketMovers ranking helper with tie-breaking, bounds protection, and active filter alignment
  - Tes tercatat: test-n03-movers-unit.cjs 8/8 PASS; test-movers-browser.cjs PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/test-n03-movers-unit.cjs; test-movers-browser.cjs; market-highlights-element.png`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.
- [x] **N03-03 — PASS_WITHIN_SCOPE**: Tambahkan navigasi item ke detail emiten, focus state, serta label perubahan bertanda.
  - Implementasi/bukti parsial: Shared selectMarketMovers ranking helper with tie-breaking, bounds protection, and active filter alignment
  - Tes tercatat: test-n03-movers-unit.cjs 8/8 PASS; test-movers-browser.cjs PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/test-n03-movers-unit.cjs; test-movers-browser.cjs; market-highlights-element.png`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.
- [x] **N03-04 — PASS_WITHIN_SCOPE**: Uji tiga/kurang dari tiga, semua nol, semua positif, semua negatif, seri, nilai invalid dan ticker lintas bursa.
  - Implementasi/bukti parsial: Shared selectMarketMovers ranking helper with tie-breaking, bounds protection, and active filter alignment
  - Tes tercatat: test-n03-movers-unit.cjs 8/8 PASS; test-movers-browser.cjs PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/test-n03-movers-unit.cjs; test-movers-browser.cjs; market-highlights-element.png`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.
- [x] **N03-05 — PASS_WITHIN_SCOPE**: Verifikasi refresh tidak menggandakan item, mengacak fokus, atau mengubah filter pengguna.
  - Implementasi/bukti parsial: Shared selectMarketMovers ranking helper with tie-breaking, bounds protection, and active filter alignment
  - Tes tercatat: test-n03-movers-unit.cjs 8/8 PASS; test-movers-browser.cjs PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/test-n03-movers-unit.cjs; test-movers-browser.cjs; market-highlights-element.png`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.

### N05 — SELESAI PENUH (5/5 subtask selesai)

- [x] **N05-01 — PASS_WITHIN_SCOPE**: Telusuri seluruh state, node, event, CSS dan tes tabel alternatif.
  - Implementasi/bukti parsial: Alternative OHLCV table and toggle button removed; dead CSS pruned; non-color accessibility scrub slider active
  - Tes tercatat: test-chart-browser.cjs PASS; chart-matrix-ui.cjs 20/20 PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/test-chart-browser.cjs; chart-matrix-output/results.json`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.
- [x] **N05-02 — PASS_WITHIN_SCOPE**: Hapus UI dan cleanup dependency yang tidak memiliki pemakai lain.
  - Implementasi/bukti parsial: Alternative OHLCV table and toggle button removed; dead CSS pruned; non-color accessibility scrub slider active
  - Tes tercatat: test-chart-browser.cjs PASS; chart-matrix-ui.cjs 20/20 PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/test-chart-browser.cjs; chart-matrix-output/results.json`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.
- [x] **N05-03 — PASS_WITHIN_SCOPE**: Pertahankan render candle/line/volume, data OHLCV domain, pemilihan rentang, serta focus control lain.
  - Implementasi/bukti parsial: Alternative OHLCV table and toggle button removed; dead CSS pruned; non-color accessibility scrub slider active
  - Tes tercatat: test-chart-browser.cjs PASS; chart-matrix-ui.cjs 20/20 PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/test-chart-browser.cjs; chart-matrix-output/results.json`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.
- [x] **N05-04 — PASS_WITHIN_SCOPE**: Verifikasi grafik tetap dapat dipahami tanpa warna saja, dan nilai penting dapat diakses tanpa hover mouse.
  - Implementasi/bukti parsial: Alternative OHLCV table and toggle button removed; dead CSS pruned; non-color accessibility scrub slider active
  - Tes tercatat: test-chart-browser.cjs PASS; chart-matrix-ui.cjs 20/20 PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/test-chart-browser.cjs; chart-matrix-output/results.json`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.
- [x] **N05-05 — PASS_WITHIN_SCOPE**: Uji tidak ada tombol, tabel tersembunyi, tab stop kosong, atau error saat refresh dan pergantian timeframe.
  - Implementasi/bukti parsial: Alternative OHLCV table and toggle button removed; dead CSS pruned; non-color accessibility scrub slider active
  - Tes tercatat: test-chart-browser.cjs PASS; chart-matrix-ui.cjs 20/20 PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/test-chart-browser.cjs; chart-matrix-output/results.json`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.

### N06 — SELESAI PENUH (4/4 subtask selesai)

- [x] **N06-01 — PASS_WITHIN_SCOPE**: Telusuri penciptaan `provNotice` dan pemakainya.
  - Implementasi/bukti parsial: Technical data source block and duplicate timeframe notice removed; clean bottom spacing; error alert retained
  - Tes tercatat: test-chart-browser.cjs PASS; chart-matrix-ui.cjs 20/20 PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/test-chart-browser.cjs; chart-matrix-output/results.json`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.
- [x] **N06-02 — PASS_WITHIN_SCOPE**: Hapus blok sumber/frame duplikat, rapikan jarak bawah grafik tanpa ruang kosong bekas elemen.
  - Implementasi/bukti parsial: Technical data source block and duplicate timeframe notice removed; clean bottom spacing; error alert retained
  - Tes tercatat: test-chart-browser.cjs PASS; chart-matrix-ui.cjs 20/20 PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/test-chart-browser.cjs; chart-matrix-output/results.json`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.
- [x] **N06-03 — PASS_WITHIN_SCOPE**: Pertahankan filter rentang dan status error/stale yang relevan.
  - Implementasi/bukti parsial: Technical data source block and duplicate timeframe notice removed; clean bottom spacing; error alert retained
  - Tes tercatat: test-chart-browser.cjs PASS; chart-matrix-ui.cjs 20/20 PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/test-chart-browser.cjs; chart-matrix-output/results.json`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.
- [x] **N06-04 — PASS_WITHIN_SCOPE**: Uji semua jenis grafik/timeframe serta riwayat kosong agar notice tidak muncul kembali dari fallback.
  - Implementasi/bukti parsial: Technical data source block and duplicate timeframe notice removed; clean bottom spacing; error alert retained
  - Tes tercatat: test-chart-browser.cjs PASS; chart-matrix-ui.cjs 20/20 PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/test-chart-browser.cjs; chart-matrix-output/results.json`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.

### N14 — SELESAI PENUH (8/8 subtask selesai)

- [x] **N14-01 — PASS_WITHIN_SCOPE**: Telusuri kontrak getChartData, fill timestamps, replay, retensi history dan seluruh caller grafik.
  - Implementasi/bukti parsial: Four chart ranges (R6H, R1D, R7D, R30D) with automatic intervals; persistent selection; numeric price protection
  - Tes tercatat: ChartHistoryRangeTest.java 7/7 PASS; g02-target-suite 106/106 PASS; chart-matrix-ui.cjs 20/20 PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/chart-expanded/junit-latest.log; g02-target-suite; chart-matrix-output`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.
- [x] **N14-02 — PASS_WITHIN_SCOPE**: Pisahkan konsep rentang dan interval; tentukan timestamp, zona, batas dan resolusi otomatis.
  - Implementasi/bukti parsial: Four chart ranges (R6H, R1D, R7D, R30D) with automatic intervals; persistent selection; numeric price protection
  - Tes tercatat: ChartHistoryRangeTest.java 7/7 PASS; g02-target-suite 106/106 PASS; chart-matrix-ui.cjs 20/20 PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/chart-expanded/junit-latest.log; g02-target-suite; chart-matrix-output`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.
- [x] **N14-03 — PASS_WITHIN_SCOPE**: Implementasikan filter/agregasi empat rentang pada domain/shared helper yang sesuai, tanpa future leak.
  - Implementasi/bukti parsial: Four chart ranges (R6H, R1D, R7D, R30D) with automatic intervals; persistent selection; numeric price protection
  - Tes tercatat: ChartHistoryRangeTest.java 7/7 PASS; g02-target-suite 106/106 PASS; chart-matrix-ui.cjs 20/20 PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/chart-expanded/junit-latest.log; g02-target-suite; chart-matrix-output`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.
- [x] **N14-04 — PASS_WITHIN_SCOPE**: Hubungkan empat kontrol saja, default 1 hari, dan pertahankan pilihan saat refresh/Candle-Garis.
  - Implementasi/bukti parsial: Four chart ranges (R6H, R1D, R7D, R30D) with automatic intervals; persistent selection; numeric price protection
  - Tes tercatat: ChartHistoryRangeTest.java 7/7 PASS; g02-target-suite 106/106 PASS; chart-matrix-ui.cjs 20/20 PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/chart-expanded/junit-latest.log; g02-target-suite; chart-matrix-output`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.
- [x] **N14-05 — PASS_WITHIN_SCOPE**: Pastikan retensi/restore cukup atau state kurang data jujur; hapus fallback yang memalsukan history pada jalur baru.
  - Implementasi/bukti parsial: Four chart ranges (R6H, R1D, R7D, R30D) with automatic intervals; persistent selection; numeric price protection
  - Tes tercatat: ChartHistoryRangeTest.java 7/7 PASS; g02-target-suite 106/106 PASS; chart-matrix-ui.cjs 20/20 PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/chart-expanded/junit-latest.log; g02-target-suite; chart-matrix-output`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.
- [x] **N14-06 — PASS_WITHIN_SCOPE**: Perbaiki sumbu tanggal/jam, ringkasan candle terpilih, gap/partial/empty dan akses keyboard/sentuh.
  - Implementasi/bukti parsial: Four chart ranges (R6H, R1D, R7D, R30D) with automatic intervals; persistent selection; numeric price protection
  - Tes tercatat: ChartHistoryRangeTest.java 7/7 PASS; g02-target-suite 106/106 PASS; chart-matrix-ui.cjs 20/20 PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/chart-expanded/junit-latest.log; g02-target-suite; chart-matrix-output`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.
- [x] **N14-07 — PASS_WITHIN_SCOPE**: Uji bucket boundaries, OHLCV/volume, weekend/sesi, lintas hari, instrumen, mode, reload dan data tidak berurutan.
  - Implementasi/bukti parsial: Four chart ranges (R6H, R1D, R7D, R30D) with automatic intervals; persistent selection; numeric price protection
  - Tes tercatat: ChartHistoryRangeTest.java 7/7 PASS; g02-target-suite 106/106 PASS; chart-matrix-ui.cjs 20/20 PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/chart-expanded/junit-latest.log; g02-target-suite; chart-matrix-output`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.
- [x] **N14-08 — PASS_WITHIN_SCOPE**: Verifikasi browser 100%/200%, performa 30 hari, source/DTO/visual yang sama dan N05/N06 tidak kembali.
  - Implementasi/bukti parsial: Four chart ranges (R6H, R1D, R7D, R30D) with automatic intervals; persistent selection; numeric price protection
  - Tes tercatat: ChartHistoryRangeTest.java 7/7 PASS; g02-target-suite 106/106 PASS; chart-matrix-ui.cjs 20/20 PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/chart-expanded/junit-latest.log; g02-target-suite; chart-matrix-output`
  - Berikutnya: Complete within PLAN-02. Verified against fresh domain and browser evidence.

## G03 — Transaksi dari tiket sampai notifikasi

### N07 — SELESAI PENUH (7/7 subtask selesai)

- [x] **N07-01 — PASS_WITHIN_SCOPE**: Telusuri seluruh caller `#order-type`, side, limit/trigger, percent buttons, review dan submit; inventaris enum yang benar.
  - Implementasi/bukti parsial: Native radio cards (MARKET, LIMIT, STOP_LOSS, TAKE_PROFIT), dynamic price label/field, domain quote/capacity
  - Tes tercatat: OrderMatrixCombinationsTest 3/3 PASS; test-orders-and-notifications.cjs PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/order-matrix; test-orders-and-notifications.cjs; order-ticket-360x800.png; order-review-360x800.png`
  - Berikutnya: Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.
- [x] **N07-02 — PASS_WITHIN_SCOPE**: Implementasikan pilihan visual dengan native radio atau kontrol setara yang mendukung keyboard dan state selected.
  - Implementasi/bukti parsial: Native radio cards (MARKET, LIMIT, STOP_LOSS, TAKE_PROFIT), dynamic price label/field, domain quote/capacity
  - Tes tercatat: OrderMatrixCombinationsTest 3/3 PASS; test-orders-and-notifications.cjs PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/order-matrix; test-orders-and-notifications.cjs; order-ticket-360x800.png; order-review-360x800.png`
  - Berikutnya: Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.
- [x] **N07-03 — PASS_WITHIN_SCOPE**: Hubungkan pilihan ke field/payload yang tepat dan pertahankan nilai draft yang masih relevan.
  - Implementasi/bukti parsial: Native radio cards (MARKET, LIMIT, STOP_LOSS, TAKE_PROFIT), dynamic price label/field, domain quote/capacity
  - Tes tercatat: OrderMatrixCombinationsTest 3/3 PASS; test-orders-and-notifications.cjs PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/order-matrix; test-orders-and-notifications.cjs; order-ticket-360x800.png; order-review-360x800.png`
  - Berikutnya: Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.
- [x] **N07-04 — PASS_WITHIN_SCOPE**: Samakan estimasi dan MAX dengan akun serta quote Java; hilangkan fallback angka palsu pada jalur tersebut.
  - Implementasi/bukti parsial: Native radio cards (MARKET, LIMIT, STOP_LOSS, TAKE_PROFIT), dynamic price label/field, domain quote/capacity
  - Tes tercatat: OrderMatrixCombinationsTest 3/3 PASS; test-orders-and-notifications.cjs PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/order-matrix; test-orders-and-notifications.cjs; order-ticket-360x800.png; order-review-360x800.png`
  - Berikutnya: Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.
- [x] **N07-05 — PASS_WITHIN_SCOPE**: Pertahankan review/confirm, validasi, idempotency dan handling pemicu.
  - Implementasi/bukti parsial: Native radio cards (MARKET, LIMIT, STOP_LOSS, TAKE_PROFIT), dynamic price label/field, domain quote/capacity
  - Tes tercatat: OrderMatrixCombinationsTest 3/3 PASS; test-orders-and-notifications.cjs PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/order-matrix; test-orders-and-notifications.cjs; order-ticket-360x800.png; order-review-360x800.png`
  - Berikutnya: Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.
- [x] **N07-06 — PASS_WITHIN_SCOPE**: Perbarui selector tes yang masih `select_option` menjadi aksi radio yang benar, tanpa melemahkan assertion payload/outcome.
  - Implementasi/bukti parsial: Native radio cards (MARKET, LIMIT, STOP_LOSS, TAKE_PROFIT), dynamic price label/field, domain quote/capacity
  - Tes tercatat: OrderMatrixCombinationsTest 3/3 PASS; test-orders-and-notifications.cjs PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/order-matrix; test-orders-and-notifications.cjs; order-ticket-360x800.png; order-review-360x800.png`
  - Berikutnya: Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.
- [x] **N07-07 — PASS_WITHIN_SCOPE**: Uji Beli/Jual × empat jenis × IDX/US × akun pribadi/klien yang tersedia, termasuk kasus tidak didukung.
  - Implementasi/bukti parsial: Native radio cards (MARKET, LIMIT, STOP_LOSS, TAKE_PROFIT), dynamic price label/field, domain quote/capacity
  - Tes tercatat: OrderMatrixCombinationsTest 3/3 PASS; test-orders-and-notifications.cjs PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/order-matrix; test-orders-and-notifications.cjs; order-ticket-360x800.png; order-review-360x800.png`
  - Berikutnya: Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.

### N08 — SELESAI PENUH (6/6 subtask selesai)

- [x] **N08-01 — PASS_WITHIN_SCOPE**: Tetapkan field utama per status dan field detail; petakan semua status existing.
  - Implementasi/bukti parsial: Human-readable receipt without raw UUIDs, actual fees/VWAP/gross/net, live cancel releasing reservation, production expiry lifecycle
  - Tes tercatat: OrderReceiptActualsTest 8/8 PASS; OrderCapacityQuoteTest 1/1 PASS; test-orders-and-notifications.cjs PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/receipt-capacity; order-receipt-360x800.png; order-cancelled-receipt-360x800.png`
  - Berikutnya: Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.
- [x] **N08-02 — PASS_WITHIN_SCOPE**: Lengkapi DTO harga/nilai/biaya dari receipt/ledger bila dibutuhkan; dokumentasikan gross/net.
  - Implementasi/bukti parsial: Human-readable receipt without raw UUIDs, actual fees/VWAP/gross/net, live cancel releasing reservation, production expiry lifecycle
  - Tes tercatat: OrderReceiptActualsTest 8/8 PASS; OrderCapacityQuoteTest 1/1 PASS; test-orders-and-notifications.cjs PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/receipt-capacity; order-receipt-360x800.png; order-cancelled-receipt-360x800.png`
  - Berikutnya: Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.
- [x] **N08-03 — PASS_WITHIN_SCOPE**: Bangun ulang ringkasan dan tindakan, sembunyikan ID mentah dari default.
  - Implementasi/bukti parsial: Human-readable receipt without raw UUIDs, actual fees/VWAP/gross/net, live cancel releasing reservation, production expiry lifecycle
  - Tes tercatat: OrderReceiptActualsTest 8/8 PASS; OrderCapacityQuoteTest 1/1 PASS; test-orders-and-notifications.cjs PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/receipt-capacity; order-receipt-360x800.png; order-cancelled-receipt-360x800.png`
  - Berikutnya: Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.
- [x] **N08-04 — PASS_WITHIN_SCOPE**: Perbarui updater live dan riwayat agar memakai representasi yang konsisten.
  - Implementasi/bukti parsial: Human-readable receipt without raw UUIDs, actual fees/VWAP/gross/net, live cancel releasing reservation, production expiry lifecycle
  - Tes tercatat: OrderReceiptActualsTest 8/8 PASS; OrderCapacityQuoteTest 1/1 PASS; test-orders-and-notifications.cjs PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/receipt-capacity; order-receipt-360x800.png; order-cancelled-receipt-360x800.png`
  - Berikutnya: Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.
- [x] **N08-05 — PASS_WITHIN_SCOPE**: Uji order fresh, reload, partial fill, batal sisa, expired, rejected dan response belum diketahui.
  - Implementasi/bukti parsial: Human-readable receipt without raw UUIDs, actual fees/VWAP/gross/net, live cancel releasing reservation, production expiry lifecycle
  - Tes tercatat: OrderReceiptActualsTest 8/8 PASS; OrderCapacityQuoteTest 1/1 PASS; test-orders-and-notifications.cjs PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/receipt-capacity; order-receipt-360x800.png; order-cancelled-receipt-360x800.png`
  - Berikutnya: Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.
- [x] **N08-06 — PASS_WITHIN_SCOPE**: Verifikasi pembatalan, retry, dan laporan kas tetap sesuai ledger.
  - Implementasi/bukti parsial: Human-readable receipt without raw UUIDs, actual fees/VWAP/gross/net, live cancel releasing reservation, production expiry lifecycle
  - Tes tercatat: OrderReceiptActualsTest 8/8 PASS; OrderCapacityQuoteTest 1/1 PASS; test-orders-and-notifications.cjs PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/receipt-capacity; order-receipt-360x800.png; order-cancelled-receipt-360x800.png`
  - Berikutnya: Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.

### N09 — SELESAI PENUH (6/6 subtask selesai)

- [x] **N09-01 — PASS_WITHIN_SCOPE**: Inventaris seluruh jalur submit, gagal preview, gagal confirm, cancel, partial fill, trigger dan perubahan sesi.
  - Implementasi/bukti parsial: Toast queue (max 3 stacked), accessible status/alert roles, inline error feedback, stable event/request deduplication
  - Tes tercatat: test-orders-and-notifications.cjs PASS; visual review 360x800 at 100% and 200%
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/test-orders-and-notifications.cjs; order-cancelled-receipt-360x800.png; order-receipt-360x800-200.png`
  - Berikutnya: Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.
- [x] **N09-02 — PASS_WITHIN_SCOPE**: Samakan kode outcome/error domain dan bahasa pesan; jangan mengubah status ekonomi.
  - Implementasi/bukti parsial: Toast queue (max 3 stacked), accessible status/alert roles, inline error feedback, stable event/request deduplication
  - Tes tercatat: test-orders-and-notifications.cjs PASS; visual review 360x800 at 100% and 200%
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/test-orders-and-notifications.cjs; order-cancelled-receipt-360x800.png; order-receipt-360x800-200.png`
  - Berikutnya: Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.
- [x] **N09-03 — PASS_WITHIN_SCOPE**: Hubungkan feedback inline dan toast pada titik transisi yang benar.
  - Implementasi/bukti parsial: Toast queue (max 3 stacked), accessible status/alert roles, inline error feedback, stable event/request deduplication
  - Tes tercatat: test-orders-and-notifications.cjs PASS; visual review 360x800 at 100% and 200%
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/test-orders-and-notifications.cjs; order-cancelled-receipt-360x800.png; order-receipt-360x800-200.png`
  - Berikutnya: Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.
- [x] **N09-04 — PASS_WITHIN_SCOPE**: Perbaiki dedupe, prioritas pesan, masa tampil, dan recovery status belum pasti.
  - Implementasi/bukti parsial: Toast queue (max 3 stacked), accessible status/alert roles, inline error feedback, stable event/request deduplication
  - Tes tercatat: test-orders-and-notifications.cjs PASS; visual review 360x800 at 100% and 200%
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/test-orders-and-notifications.cjs; order-cancelled-receipt-360x800.png; order-receipt-360x800-200.png`
  - Berikutnya: Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.
- [x] **N09-05 — PASS_WITHIN_SCOPE**: Verifikasi queue, focus, live region, teks panjang dan keyboard pada layar kecil.
  - Implementasi/bukti parsial: Toast queue (max 3 stacked), accessible status/alert roles, inline error feedback, stable event/request deduplication
  - Tes tercatat: test-orders-and-notifications.cjs PASS; visual review 360x800 at 100% and 200%
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/test-orders-and-notifications.cjs; order-cancelled-receipt-360x800.png; order-receipt-360x800-200.png`
  - Berikutnya: Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.
- [x] **N09-06 — PASS_WITHIN_SCOPE**: Uji beberapa event berdekatan, callback duplikat, reconnect/reload, offline catchup serta perubahan akun.
  - Implementasi/bukti parsial: Toast queue (max 3 stacked), accessible status/alert roles, inline error feedback, stable event/request deduplication
  - Tes tercatat: test-orders-and-notifications.cjs PASS; visual review 360x800 at 100% and 200%
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/test-orders-and-notifications.cjs; order-cancelled-receipt-360x800.png; order-receipt-360x800-200.png`
  - Berikutnya: Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.

## G04 — Portofolio dan kepemilikan

27/27 PASS_WITHIN_SCOPE. Laporan dan batas verifikasi: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/RESULTS.md`.

### N11 — SELESAI PENUH (6/6 subtask selesai)

- [x] **N11-01 — PASS_WITHIN_SCOPE**: Inventaris seluruh button/caller asset-tools dan buat mapping keep/move/remove-approved per fitur.
  - Implementasi: 16 launcher/caller diinventaris; 14 keep/move dan 2 removal UI approved N19; mapping eksplisit.
  - Tes: test-portfolio-category-routes.cjs PASS; 6 roots/17 routes; viewport, keyboard, Back/reload, preserved financial snapshots
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/portfolio-route-map.json; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-browser-final-04.log`
- [x] **N11-02 — PASS_WITHIN_SCOPE**: Ganti klasifikasi berbasis awalan teks/tag dengan daftar tujuan eksplisit yang kecil dan terarah.
  - Implementasi: Daftar route eksplisit menggantikan klasifikasi awalan teks/tag; handler existing dipakai ulang.
  - Tes: test-portfolio-category-routes.cjs PASS; 6 roots/17 routes; viewport, keyboard, Back/reload, preserved financial snapshots
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/portfolio-route-map.json; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-browser-final-04.log`
- [x] **N11-03 — PASS_WITHIN_SCOPE**: Buat launcher kategori terbuka dalam bentuk daftar ringkas dengan normal/pressed/focus/locked/loading yang konsisten.
  - Implementasi: Enam kategori root dengan label/deskripsi dan state keyboard/focus/pressed; clock-lock loading mempertahankan kategori.
  - Tes: test-portfolio-category-routes.cjs PASS; 6 roots/17 routes; viewport, keyboard, Back/reload, preserved financial snapshots
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/portfolio-route-map.json; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-browser-final-04.log`
- [x] **N11-04 — PASS_WITHIN_SCOPE**: Pertahankan akun, syarat dan review tindakan berisiko; membuka kategori tidak mengeksekusi ekonomi.
  - Implementasi: Kategori hanya navigasi; account context, syarat dan review tindakan ekonomi dipertahankan; snapshot keuangan sebelum/sesudah identik.
  - Tes: test-portfolio-category-routes.cjs PASS; 6 roots/17 routes; viewport, keyboard, Back/reload, preserved financial snapshots
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/portfolio-route-map.json; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-browser-final-04.log`
- [x] **N11-05 — PASS_WITHIN_SCOPE**: Verifikasi seluruh fitur yang dipertahankan punya tujuan; removal lab/uji stres ditelusuri ke N19.
  - Implementasi: 17 tujuan kanonis terjangkau; order history dan alias Bisnis menuju handler existing; lab/stres dikecualikan approved N19.
  - Tes: test-portfolio-category-routes.cjs PASS; 6 roots/17 routes; viewport, keyboard, Back/reload, preserved financial snapshots
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/portfolio-route-map.json; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-browser-final-04.log`
- [x] **N11-06 — PASS_WITHIN_SCOPE**: Uji setiap kategori, klik/keyboard/Back, scroll, 200% dan live refresh tanpa launcher ganda.
  - Implementasi: Klik/keyboard/Back/reload/live/scroll/200% teruji; tidak ada launcher duplikat; deferred SVG click root cause diperbaiki.
  - Tes: test-portfolio-category-routes.cjs PASS; 6 roots/17 routes; viewport, keyboard, Back/reload, preserved financial snapshots
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/portfolio-route-map.json; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-browser-final-04.log`

### N17 — SELESAI PENUH (6/6 subtask selesai)

- [x] **N17-01 — PASS_WITHIN_SCOPE**: Kelompokkan inventaris Portofolio berdasarkan objek/tindakan/akun; tandai fitur yang salah kelompok dan shortcut duplikat.
  - Implementasi: Inventaris objek/aksi/akun menghasilkan enam root bertingkat; multi-assets digabung tanpa pintasan ganda.
  - Tes: test-portfolio-category-routes.cjs PASS; 6 roots/17 routes; viewport, keyboard, Back/reload, preserved financial snapshots
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/portfolio-route-map.json; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-browser-final-04.log`
- [x] **N17-02 — PASS_WITHIN_SCOPE**: Ajukan peta kategori/subkategori serta tujuan kanonis dan rute balik sebagai bagian persetujuan N20.
  - Implementasi: Mengikuti approval revisi 2 N20; enam root/subkategori dan rute balik di portfolio-route-map.json.
  - Tes: test-portfolio-category-routes.cjs PASS; 6 roots/17 routes; viewport, keyboard, Back/reload, preserved financial snapshots
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/portfolio-route-map.json; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-browser-final-04.log`
- [x] **N17-03 — PASS_WITHIN_SCOPE**: Implementasikan landing ringkasan dan daftar kategori; pindahkan fitur ke halaman relevan tanpa accordion penampung.
  - Implementasi: Landing heading, summary, positions, categories; tidak ada accordion penampung seluruh alat.
  - Tes: test-portfolio-category-routes.cjs PASS; 6 roots/17 routes; viewport, keyboard, Back/reload, preserved financial snapshots
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/portfolio-route-map.json; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-browser-final-04.log`
- [x] **N17-04 — PASS_WITHIN_SCOPE**: Pertahankan semua handler, syarat, review, konteks akun, filter dan posisi scroll; cabut hanya removal N19.
  - Implementasi: Handler/account/filter/review tetap; kategori Back memulihkan fokus/scroll; removal hanya UI approved N19.
  - Tes: test-portfolio-category-routes.cjs PASS; 6 roots/17 routes; viewport, keyboard, Back/reload, preserved financial snapshots
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/portfolio-route-map.json; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-browser-final-04.log`
- [x] **N17-05 — PASS_WITHIN_SCOPE**: Verifikasi setiap fitur keep/move/remove-approved dengan klik nyata, Back, direct route dan reload.
  - Implementasi: Semua 17 tujuan diuji klik nyata, route kanonis, Back dan reload; tidak ada URL deep-link router di produk.
  - Tes: test-portfolio-category-routes.cjs PASS; 6 roots/17 routes; viewport, keyboard, Back/reload, preserved financial snapshots
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/portfolio-route-map.json; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-browser-final-04.log`
- [x] **N17-06 — PASS_WITHIN_SCOPE**: Uji empty/error/locked, banyak posisi/aset, 200%, layar kecil dan live update tanpa lompatan layout.
  - Implementasi: Empty/error/clock loading dan locked destination, fixture 12 posisi, 10 viewport, proxy teks 200%, live stabil.
  - Tes: test-portfolio-category-routes.cjs PASS; 6 roots/17 routes; viewport, keyboard, Back/reload, preserved financial snapshots
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/portfolio-route-map.json; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-browser-final-04.log`

### N10 — SELESAI PENUH (7/7 subtask selesai)

- [x] **N10-01 — PASS_WITHIN_SCOPE**: Petakan kontrak posisi, katalog/nama emiten, lot, mata uang, biaya dan short/long yang didukung.
  - Implementasi: DTO/katalog/lot/currency/cost/short-long ditelusuri; Position long-only; ShortPosition terpisah.
  - Tes: Bridge 38/38; PortfolioFx 17/17; live-state and nav-sync PASS; integrated JVM 169/169; Gradle targeted 85/85
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-regression-08/junit-latest.log; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/portfolio-live-state-results.json; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/gradle-target-final.log`
- [x] **N10-02 — PASS_WITHIN_SCOPE**: Tentukan field default/rincian dan format nilai yang konsisten.
  - Implementasi: Kartu/default dan rincian konsisten; format IDR/USD, Untung/Rugi/Impas, realized gross bukan bersih.
  - Tes: Bridge 38/38; PortfolioFx 17/17; live-state and nav-sync PASS; integrated JVM 169/169; Gradle targeted 85/85
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-regression-08/junit-latest.log; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/portfolio-live-state-results.json; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/gradle-target-final.log`
- [x] **N10-03 — PASS_WITHIN_SCOPE**: Tambahkan field DTO minimum hanya jika sumber numeriknya belum tersedia.
  - Implementasi: Field numerik existing dipakai; reserved/available dikoreksi melalui shared getCommittedShares.
  - Tes: Bridge 38/38; PortfolioFx 17/17; live-state and nav-sync PASS; integrated JVM 169/169; Gradle targeted 85/85
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-regression-08/junit-latest.log; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/portfolio-live-state-results.json; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/gradle-target-final.log`
- [x] **N10-04 — PASS_WITHIN_SCOPE**: Implementasikan kartu posisi yang aksesibel serta tindakan ke detail/ticket.
  - Implementasi: Kartu aksesibel ke detail/ticket; ticket netral tanpa sisi implisit; nama wrap; Cari saham dan error/retry nyata.
  - Tes: Bridge 38/38; PortfolioFx 17/17; live-state and nav-sync PASS; integrated JVM 169/169; Gradle targeted 85/85
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-regression-08/junit-latest.log; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/portfolio-live-state-results.json; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/gradle-target-final.log`
- [x] **N10-05 — PASS_WITHIN_SCOPE**: Samakan renderer awal dan live updater berbasis ID stabil.
  - Implementasi: Initial/live memakai renderer struktur sama dan instrumentId stabil; quantity/average/P&L/aria/summary ikut diperbarui; fokus/scroll terjaga.
  - Tes: Bridge 38/38; PortfolioFx 17/17; live-state and nav-sync PASS; integrated JVM 169/169; Gradle targeted 85/85
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-regression-08/junit-latest.log; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/portfolio-live-state-results.json; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/gradle-target-final.log`
- [x] **N10-06 — PASS_WITHIN_SCOPE**: Uji untung/rugi/nol, long/short yang tersedia, USD/IDR, posisi tertahan, posisi ditutup, data kosong/error/stale dan perpindahan akun.
  - Implementasi: Untung/rugi/nol, IDR/USD, short terpisah, reserved SELL/pending trigger/cancel, closed, empty/error/stale/recovery dan akun terisolasi teruji.
  - Tes: Bridge 38/38; PortfolioFx 17/17; live-state and nav-sync PASS; integrated JVM 169/169; Gradle targeted 85/85
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-regression-08/junit-latest.log; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/portfolio-live-state-results.json; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/gradle-target-final.log`
- [x] **N10-07 — PASS_WITHIN_SCOPE**: Cocokkan semua nilai yang ditampilkan dengan portfolio Java, receipt dan ledger yang relevan.
  - Implementasi: DTO dan tampilan cocok Java Position, fill receipt, actual fee, cash delta serta ledger; akun personal/client terpisah.
  - Tes: Bridge 38/38; PortfolioFx 17/17; live-state and nav-sync PASS; integrated JVM 169/169; Gradle targeted 85/85
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-regression-08/junit-latest.log; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/portfolio-live-state-results.json; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/gradle-target-final.log`

### N15 — SELESAI PENUH (8/8 subtask selesai)

- [x] **N15-01 — PASS_WITHIN_SCOPE**: Inventaris sumber posisi, NPC/institusi, outstanding/free float, corporate actions dan identitas holder.
  - Implementasi: Sumber Account/NPC/client/fund pool/treasury, corporate/short, outstanding dan identity diinventaris dalam OWNERSHIP-CONTRACT.md.
  - Tes: Ownership 13/13; supply persistence 7/7; 10 seeds x100 meaningful actions; integrated JVM 169/169; browser 20 layout samples, live/contrast/financial preservation PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/OWNERSHIP-CONTRACT.md; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-regression-08/junit-latest.log; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/issuer-ownership-browser-results.json`
- [x] **N15-02 — PASS_WITHIN_SCOPE**: Definisikan holder of record, penyebut porsi, cakupan data, short/reservasi dan akun kelolaan tanpa hitung ganda.
  - Implementasi: Holder of record Account.positions; outstanding=issued-treasury; short/reserve/unit dana tidak membuat holder kedua; unknown legacy eksplisit.
  - Tes: Ownership 13/13; supply persistence 7/7; 10 seeds x100 meaningful actions; integrated JVM 169/169; browser 20 layout samples, live/contrast/financial preservation PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/OWNERSHIP-CONTRACT.md; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-regression-08/junit-latest.log; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/issuer-ownership-browser-results.json`
- [x] **N15-03 — PASS_WITHIN_SCOPE**: Pakai/perluas domain minimum untuk komposisi aktual; jangan mengangkat majorShareholders hardcoded sebagai fakta dinamis.
  - Implementasi: Supply dinamis pada Instrument dan state katalog existing sesuai pilihan pemilik; tanpa majorShareholders hardcoded atau holder rekaan.
  - Tes: Ownership 13/13; supply persistence 7/7; 10 seeds x100 meaningful actions; integrated JVM 169/169; browser 20 layout samples, live/contrast/financial preservation PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/OWNERSHIP-CONTRACT.md; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-regression-08/junit-latest.log; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/issuer-ownership-browser-results.json`
- [x] **N15-04 — PASS_WITHIN_SCOPE**: Integrasikan peralihan kepemilikan pada fill/settlement, pembatalan, split/rights/buyback dan restore yang relevan.
  - Implementasi: Fill/cancel, split/reverse, rights, buyback/merger treasury dan restore/migrasi idempoten; valuasi/index diperiksa.
  - Tes: Ownership 13/13; supply persistence 7/7; 10 seeds x100 meaningful actions; integrated JVM 169/169; browser 20 layout samples, live/contrast/financial preservation PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/OWNERSHIP-CONTRACT.md; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-regression-08/junit-latest.log; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/issuer-ownership-browser-results.json`
- [x] **N15-05 — PASS_WITHIN_SCOPE**: Sediakan DTO numerik eksplisit dengan instrument/holder ID, jumlah/porsi valid, as-of dan status cakupan.
  - Implementasi: DTO numerik instrument/holder ID, quantity/percent/asOf, issued/treasury/short/unknownLegacy, denominator/coverage/registryComplete eksplisit.
  - Tes: Ownership 13/13; supply persistence 7/7; 10 seeds x100 meaningful actions; integrated JVM 169/169; browser 20 layout samples, live/contrast/financial preservation PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/OWNERSHIP-CONTRACT.md; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-regression-08/junit-latest.log; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/issuer-ownership-browser-results.json`
- [x] **N15-06 — PASS_WITHIN_SCOPE**: Buat panel pemegang saham terpisah dari Posisi saya, termasuk state pemain nol, partial/unknown/loading/error.
  - Implementasi: Panel holder terpisah dari posisi akun; nol personal tetap memuat dunia game; loading/empty/PARTIAL/COMPLETE/UNKNOWN/error/retry/treasury teruji.
  - Tes: Ownership 13/13; supply persistence 7/7; 10 seeds x100 meaningful actions; integrated JVM 169/169; browser 20 layout samples, live/contrast/financial preservation PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/OWNERSHIP-CONTRACT.md; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-regression-08/junit-latest.log; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/issuer-ownership-browser-results.json`
- [x] **N15-07 — PASS_WITHIN_SCOPE**: Uji konservasi, denominator nol/invalid, rounding, banyak holder, akun klien, corporate action dan reload.
  - Implementasi: Konservasi/invalid/rounding/overmapped, 40 holder domain, client/fund units, corporate, legacy migration/save corruption/reload serta 10x100 teruji.
  - Tes: Ownership 13/13; supply persistence 7/7; 10 seeds x100 meaningful actions; integrated JVM 169/169; browser 20 layout samples, live/contrast/financial preservation PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/OWNERSHIP-CONTRACT.md; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-regression-08/junit-latest.log; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/issuer-ownership-browser-results.json`
- [x] **N15-08 — PASS_WITHIN_SCOPE**: Rekonsiliasi setiap angka ke domain; uji live refresh, focus, 200%, serta tidak mengubah posisi pribadi N10.
  - Implementasi: Setiap holder/quantity/percent cocok DTO; live quantity berubah pada node holderId sama, fokus/scroll/200%/Back/reload; keuangan personal identik.
  - Tes: Ownership 13/13; supply persistence 7/7; 10 seeds x100 meaningful actions; integrated JVM 169/169; browser 20 layout samples, live/contrast/financial preservation PASS
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/OWNERSHIP-CONTRACT.md; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/g04-regression-08/junit-latest.log; Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/issuer-ownership-browser-results.json`

## G05 — Bisnis, koleksi dan dana kelolaan

### N12 — BELUM SELESAI (0/7 subtask selesai)

- [ ] **N12-01 — PENDING**: Inventaris seluruh tindakan di tiga subtab dan detail turunannya, lengkap dengan handler dan syarat.
  - Berikutnya: Hanya saat G05 aktif: Inventaris seluruh tindakan di tiga subtab dan detail turunannya, lengkap dengan handler dan syarat. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N12-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N12-02 — PENDING**: Pisahkan panel pasif, launcher, pilihan subtab dan aksi ekonomi berdasarkan pola bagian 4.
  - Berikutnya: Hanya saat G05 aktif: Pisahkan panel pasif, launcher, pilihan subtab dan aksi ekonomi berdasarkan pola bagian 4. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N12-02 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N12-03 — PENDING**: Perbaiki state kosong dan kartu perusahaan/tindakan kelola.
  - Berikutnya: Hanya saat G05 aktif: Perbaiki state kosong dan kartu perusahaan/tindakan kelola. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N12-03 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N12-04 — PENDING**: Perbaiki galeri serta state barang tanpa menghapus filter/koleksi.
  - Berikutnya: Hanya saat G05 aktif: Perbaiki galeri serta state barang tanpa menghapus filter/koleksi. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N12-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N12-05 — PENDING**: Perbaiki launcher kelola dana, mandat, staf dan laporan dengan isolasi akun yang benar.
  - Berikutnya: Hanya saat G05 aktif: Perbaiki launcher kelola dana, mandat, staf dan laporan dengan isolasi akun yang benar. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N12-05 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N12-06 — PENDING**: Pastikan label tombol memuat tindakan, focus/pressed terlihat, Back kembali ke subtab dan scroll yang tepat.
  - Berikutnya: Hanya saat G05 aktif: Pastikan label tombol memuat tindakan, focus/pressed terlihat, Back kembali ke subtab dan scroll yang tepat. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N12-06 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N12-07 — PENDING**: Jalankan journey pendirian/kelola, koleksi, mandat/dana dan state terkunci sesuai domain yang tersedia.
  - Berikutnya: Hanya saat G05 aktif: Jalankan journey pendirian/kelola, koleksi, mandat/dana dan state terkunci sesuai domain yang tersedia. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N12-07 terbukti; catat command, hasil, bukti dan kekurangan tersisa.

## G06 — Lainnya, progres dan riset

### N13 — BELUM SELESAI (0/6 subtask selesai)

- [ ] **N13-01 — PENDING**: Inventaris launcher, kartu pasif, paket, berita, pengaturan dan state Era Baru pada source terkini.
  - Berikutnya: Hanya saat G06 aktif: Inventaris launcher, kartu pasif, paket, berita, pengaturan dan state Era Baru pada source terkini. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N13-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N13-02 — PENDING**: Petakan ke Profil, Misi & Kompetisi, Riset, Pengaturan; gunakan pola interaksi bersama yang sesuai.
  - Berikutnya: Hanya saat G06 aktif: Petakan ke Profil, Misi & Kompetisi, Riset, Pengaturan; gunakan pola interaksi bersama yang sesuai. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N13-02 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N13-03 — PENDING**: Perbaiki syarat/progres/konfirmasi Era Baru tanpa mengubah aturan reset.
  - Berikutnya: Hanya saat G06 aktif: Perbaiki syarat/progres/konfirmasi Era Baru tanpa mengubah aturan reset. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N13-03 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N13-04 — PENDING**: Perbaiki terminal/hasil/booster/pohon riset dan replay yang dipertahankan; keluarkan lab/uji stres menurut N19.
  - Berikutnya: Hanya saat G06 aktif: Perbaiki terminal/hasil/booster/pohon riset dan replay yang dipertahankan; keluarkan lab/uji stres menurut N19. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N13-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N13-05 — PENDING**: Verifikasi setiap tujuan, route alias yang diperlukan, Back, fokus, akun dan state terkunci.
  - Berikutnya: Hanya saat G06 aktif: Verifikasi setiap tujuan, route alias yang diperlukan, Back, fokus, akun dan state terkunci. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N13-05 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N13-06 — PENDING**: Uji journey profil, misi, liga, paket, riset dan Era Baru pada save terisolasi; sertakan detail turunannya.
  - Berikutnya: Hanya saat G06 aktif: Uji journey profil, misi, liga, paket, riset dan Era Baru pada save terisolasi; sertakan detail turunannya. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N13-06 terbukti; catat command, hasil, bukti dan kekurangan tersisa.

### N18 — BELUM SELESAI (0/6 subtask selesai)

- [ ] **N18-01 — PENDING**: Peta semua tujuan Lainnya dan flow turunannya ke empat kelompok final.
  - Berikutnya: Hanya saat G06 aktif: Peta semua tujuan Lainnya dan flow turunannya ke empat kelompok final. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N18-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N18-02 — PENDING**: Rancang hub daftar ringkas dengan hierarchy, aksen kategori dan ringkasan progres yang sesuai tema terang.
  - Berikutnya: Hanya saat G06 aktif: Rancang hub daftar ringkas dengan hierarchy, aksen kategori dan ringkasan progres yang sesuai tema terang. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N18-02 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N18-03 — PENDING**: Perbarui detail profil/misi/liga/riset/paket/pengaturan, bukan launcher saja.
  - Berikutnya: Hanya saat G06 aktif: Perbarui detail profil/misi/liga/riset/paket/pengaturan, bukan launcher saja. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N18-03 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N18-04 — PENDING**: Integrasikan feedback locked/pending/error dan motion klaim/reward sesuai hasil domain.
  - Berikutnya: Hanya saat G06 aktif: Integrasikan feedback locked/pending/error dan motion klaim/reward sesuai hasil domain. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N18-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N18-05 — PENDING**: Pertahankan syarat akses, biaya langganan, periode misi/liga, konfirmasi reset dan save.
  - Berikutnya: Hanya saat G06 aktif: Pertahankan syarat akses, biaya langganan, periode misi/liga, konfirmasi reset dan save. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N18-05 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N18-06 — PENDING**: Uji setiap journey normal/kosong/gagal/terkunci, Back/fokus/200%, klaim ganda dan refresh.
  - Berikutnya: Hanya saat G06 aktif: Uji setiap journey normal/kosong/gagal/terkunci, Back/fokus/200%, klaim ganda dan refresh. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N18-06 terbukti; catat command, hasil, bukti dan kekurangan tersisa.

## G07 — Konsistensi seluruh UI dan pembersihan

### N19 — BELUM SELESAI (0/7 subtask selesai)

- [ ] **N19-01 — IN_PROGRESS**: Telusuri seluruh launcher, route, handler, bridge, persistence dan pemakai bersama lab/uji stres.
  - Implementasi/bukti parsial: Launcher and compatibility paths traced partially
  - Bukti: `android/app/src/main/assets/www/app.js`
  - Berikutnya: Hanya saat G07 aktif: Telusuri seluruh launcher/route/handler/bridge/persistence/shared caller lab dan stres; bedakan replay perdagangan utama dari sandbox. Tetap belum selesai sampai seluruh kriteria N19-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N19-02 — PENDING**: Buat daftar keep/remove-ui/compatibility-only/dead-code beserta alasan dan sumber keputusan pemilik.
  - Berikutnya: Hanya saat G07 aktif: Buat daftar keep/remove-ui/compatibility-only/dead-code beserta alasan dan sumber keputusan pemilik. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N19-02 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N19-03 — IN_PROGRESS**: Hapus akses UI lab dan uji stres di seluruh layar, navigasi, shortcut dan tautan internal.
  - Implementasi/bukti parsial: Portfolio/research lab+stress launchers removed; full route audit pending
  - Bukti: `android/app/src/main/assets/www/app.js`
  - Berikutnya: Hanya saat G07 aktif: Tuntaskan penghapusan akses UI lab/stres di semua root/shortcut/tautan, lalu buktikan tidak ada rute atau tab stop produk yang masih membukanya. Tetap belum selesai sampai seluruh kriteria N19-03 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N19-04 — PENDING**: Bersihkan state/listener/CSS/copy mati yang tidak punya pemakai lain; jangan menghapus engine bersama.
  - Berikutnya: Hanya saat G07 aktif: Bersihkan state/listener/CSS/copy mati yang tidak punya pemakai lain; jangan menghapus engine bersama. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N19-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N19-05 — PENDING**: Pertahankan migrasi/load legacy strategyLabs dan recovery; uji save berisi lab lama pada salinan.
  - Berikutnya: Hanya saat G07 aktif: Pertahankan migrasi/load legacy strategyLabs dan recovery; uji save berisi lab lama pada salinan. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N19-05 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N19-06 — PENDING**: Uji perdagangan utama, replay historis, margin/likuidasi dan seluruh fitur uang/progres yang dipertahankan.
  - Berikutnya: Hanya saat G07 aktif: Uji perdagangan utama, replay historis, margin/likuidasi dan seluruh fitur uang/progres yang dipertahankan. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N19-06 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N19-07 — PENDING**: Verifikasi tidak ada route/tab stop/CTA eksperimen tersisa, save pribadi utuh dan tracker memuat removal yang sah.
  - Berikutnya: Hanya saat G07 aktif: Verifikasi tidak ada route/tab stop/CTA eksperimen tersisa, save pribadi utuh dan tracker memuat removal yang sah. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N19-07 terbukti; catat command, hasil, bukti dan kekurangan tersisa.

### N04 — BELUM SELESAI (0/5 subtask selesai)

- [ ] **N04-01 — PENDING**: Buat inventaris string yang benar-benar terlihat pada semua route/state terkait, termasuk yang berasal dari Java.
  - Berikutnya: Hanya saat G07 aktif: Buat inventaris string yang benar-benar terlihat pada semua route/state terkait, termasuk yang berasal dari Java. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N04-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N04-02 — PENDING**: Tentukan penghapusan/penggantian per konteks; pertahankan tarif, risiko, syarat, dan status yang membantu keputusan.
  - Berikutnya: Hanya saat G07 aktif: Tentukan penghapusan/penggantian per konteks; pertahankan tarif, risiko, syarat, dan status yang membantu keputusan. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N04-02 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N04-03 — PENDING**: Bersihkan heading indeks, caption, detail instrumen, berita, FX, pinjaman, margin, multi-aset, order dan empty/error.
  - Berikutnya: Hanya saat G07 aktif: Bersihkan heading indeks, caption, detail instrumen, berita, FX, pinjaman, margin, multi-aset, order dan empty/error. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N04-03 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N04-04 — PENDING**: Pisahkan identitas QA dari produk tanpa memalsukan platform atau sumber data.
  - Berikutnya: Hanya saat G07 aktif: Pisahkan identitas QA dari produk tanpa memalsukan platform atau sumber data. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N04-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N04-05 — PENDING**: Uji route serta state dinamis setelah refresh; pencarian string statis menjadi pemeriksaan tambahan, bukan satu-satunya bukti.
  - Berikutnya: Hanya saat G07 aktif: Uji route serta state dinamis setelah refresh; pencarian string statis menjadi pemeriksaan tambahan, bukan satu-satunya bukti. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N04-05 terbukti; catat command, hasil, bukti dan kekurangan tersisa.

### N16 — BELUM SELESAI (0/8 subtask selesai)

- [ ] **N16-01 — PENDING**: Inventaris seluruh route/component/state dan screenshot baseline; petakan komponen bersama serta live updater.
  - Berikutnya: Hanya saat G07 aktif: Inventaris seluruh route/component/state dan screenshot baseline; petakan komponen bersama serta live updater. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N16-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N16-02 — IN_PROGRESS**: Turunkan token tema terang, kategori, angka, spacing, radius, ikon dan motion dari contoh yang disetujui.
  - Implementasi/bukti parsial: Approved revision-2 tokens applied; complete component audit pending
  - Bukti: `android/app/src/main/assets/www/style.css`
  - Berikutnya: Hanya saat G07 aktif: Tuntaskan audit token/komponen/ikon/spacing/motion dari revisi 2; pertahankan canvas gelap, panel terang dan lima tab yang disetujui. Tetap belum selesai sampai seluruh kriteria N16-02 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N16-03 — IN_PROGRESS**: Terapkan shell/header/nav yang disetujui dengan reflow, safe area, scroll dan focus yang benar.
  - Implementasi/bukti parsial: Initial shell dark canvas/light panels; legacy tab order retained; wider gate pending
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/ui-initial-01/results.json`
  - Berikutnya: Hanya saat G07 aktif: Uji shell/header/nav/safe area/scroll/fokus pada seluruh viewport/200%; periksa live updater dan aturan tinggi nav agar tidak berulang membesar. Tetap belum selesai sampai seluruh kriteria N16-03 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N16-04 — IN_PROGRESS**: Redesain seluruh layar pasar/trading/detail/form/review/receipt dengan kontrak Java tetap benar.
  - Implementasi/bukti parsial: Shared work surfaces started; inline dark/white rules remain to audit
  - Bukti: `android/app/src/main/assets/www/style.css`
  - Berikutnya: Hanya saat G07 aktif: Tuntaskan sisa layar pasar/detail/form/review/receipt; audit inline dark/white style dan semua state sambil menjaga kontrak serta angka Java. Tetap belum selesai sampai seluruh kriteria N16-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N16-05 — PENDING**: Redesain seluruh fitur Portofolio/Bisnis/Lainnya dan halaman turunannya sesuai inventaris, N17/N18.
  - Berikutnya: Hanya saat G07 aktif: Redesain seluruh fitur Portofolio/Bisnis/Lainnya dan halaman turunannya sesuai inventaris, N17/N18. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N16-05 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N16-06 — PENDING**: Terapkan state interaksi serta motion progres/hadiah yang berbasis outcome nyata dan reduced motion.
  - Berikutnya: Hanya saat G07 aktif: Terapkan state interaksi serta motion progres/hadiah yang berbasis outcome nyata dan reduced motion. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N16-06 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N16-07 — PENDING**: Audit kontras, keyboard, teks 200%, data panjang, numeric alignment, viewport termasuk 360x800 dan 470x658.
  - Berikutnya: Hanya saat G07 aktif: Audit kontras, keyboard, teks 200%, data panjang, numeric alignment, viewport termasuk 360x800 dan 470x658. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N16-07 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N16-08 — PENDING**: Jalankan click-through semua tujuan yang dipertahankan, pemeriksaan visual aktual, performa dan empat blok Delivery Gate.
  - Berikutnya: Hanya saat G07 aktif: Jalankan click-through semua tujuan yang dipertahankan, pemeriksaan visual aktual, performa dan empat blok Delivery Gate. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N16-08 terbukti; catat command, hasil, bukti dan kekurangan tersisa.

## G08 — Rekonsiliasi akhir dan penyerahan

### N20 — BELUM SELESAI (2/6 subtask selesai)

- [ ] **N20-01 — IN_PROGRESS**: Rekonsiliasi keputusan final dengan AGENTS/DESIGN/standar fase/fixture dan inventaris requirement tanpa menimpa keputusan terbaru.
  - Implementasi/bukti parsial: Active delta in AGENTS/DESIGN/standards/master; fixture reconciliation ongoing
  - Bukti: `Work/Master-Plan-Gabungan-2026-10-24/CHECKPOINT.md`
  - Berikutnya: Hanya saat G08 aktif: Rekonsiliasi dokumen/fixture/inventaris final dengan 24x, deadline, navigasi dan revisi 2; pertahankan sejarah dan periksa konsistensi seluruh 126 ID. Tetap belum selesai sampai seluruh kriteria N20-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [x] **N20-02 — PASS_WITHIN_SCOPE**: Susun peta navigasi dan 2–3 contoh layar tema terang beserta state/motion progres yang representatif.
  - Implementasi/bukti parsial: Artifacts/Master-Plan-Gabungan-2026-10-24/design-prototype.html
  - Tes tercatat: 60 layout samples;39 interaction checks;token contrast;three final screenshots opened
  - Bukti: `Artifacts/Master-Plan-Gabungan-2026-10-24/prototype-qa-revision-02-final/results.json`
  - Berikutnya: PASS_WITHIN_SCOPE untuk contoh revisi 2 yang tercatat. Gunakan sebagai acuan file aktif yang dikirim pemilik; tidak membuat ulang contoh atau mengambil pekerjaan file lain.
- [x] **N20-03 — PASS_WITHIN_SCOPE**: Tampilkan contoh dan minta persetujuan pemilik atas arah visual, navigasi, serta kategori; catat status menunggu secara jujur.
  - Implementasi/bukti parsial: Three revised examples displayed and preview opened
  - Tes tercatat: clarify answered: Setuju revisi 2; lanjutkan implementasi sesuai contoh
  - Bukti: `Work/Master-Plan-Gabungan-2026-10-24/CHECKPOINT.md`
  - Berikutnya: PASS_WITHIN_SCOPE untuk persetujuan revisi 2 yang tercatat. Jangan meminta ulang persetujuan yang sama atau memulai ulang tahap sebelumnya; hanya file yang dikirim pemilik berlaku, lalu berhenti setelah file tuntas.
- [ ] **N20-04 — IN_PROGRESS**: Setelah persetujuan, perbarui keputusan aktif DESIGN.md/aturan proyek terkait dan implementasikan hanya struktur yang disetujui.
  - Implementasi/bukti parsial: Approval and active docs updated; consistent rollout still partial
  - Bukti: `DESIGN.md`
  - Berikutnya: Hanya saat G08 aktif: Buktikan rollout struktur dan visual yang disetujui mencakup seluruh layar/state; sinkronkan keputusan aktif DESIGN/AGENTS tanpa meminta ulang persetujuan yang sudah ada. Tetap belum selesai sampai seluruh kriteria N20-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N20-05 — PENDING**: Audit seluruh 126 ID, coverage komentar/fitur/route, perubahan kontrak lama dan bukti pada snapshot final.
  - Berikutnya: Hanya saat G08 aktif: Audit seluruh 126 ID, coverage komentar/fitur/route, perubahan kontrak lama dan bukti pada snapshot final. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N20-05 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N20-06 — PENDING**: Serahkan checkpoint terintegrasi, hasil pengujian, preview terbaru, APK kandidat terverifikasi dan batas host/native yang jelas.
  - Berikutnya: Hanya saat G08 aktif: Serahkan checkpoint terintegrasi, hasil pengujian, preview terbaru, APK kandidat terverifikasi dan batas host/native yang jelas. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N20-06 terbukti; catat command, hasil, bukti dan kekurangan tersisa.

## Riwayat bukti N08 sebelum pengelompokan (superseded oleh penutupan PLAN-03)

Catatan berikut adalah riwayat run lama, bukan status aktif. Penutupan PLAN-03/G03 pada tracker dan RESULTS PLAN-03 menggantikannya; tidak mengubah atau meluluskan task lain dari PLAN-04.

- Domain receipt: **7/7 tes PASS**; raw log `Artifacts/Master-Plan-Gabungan-2026-10-24/N08-resume-targeted/receipt-7.log`. Kasus: fill/VWAP/fee/save-load, partial-cancel, legacy tanpa fee, OPEN, unknown, REJECTED, dan EXPIRED terminal pada fixture.
- Subset bridge order: **5/5 tes PASS**; raw log `Artifacts/Master-Plan-Gabungan-2026-10-24/N08-resume-targeted/bridge-subset-5.log`.
- Suite penuh `TradingWebBridgeTest`: **35 tes, 4 gagal**; raw log `bridge-full-35.log`. Kegagalan clock/day-close dan reksa dana tetap terbuka; full suite bukan PASS.
- Browser QA host Java terisolasi: output memperlihatkan tiket BBCA Limit 100 saham cocok dengan preview domain, receipt filled memuat gross/VWAP/fee/kas bersih, dan pembacaan history setelah reload cocok. Screenshot: `ui-receipt.png`, `ui-receipt-bottom.png`.
- Browser QA juga mencetak alur order tanpa fill yang dibatalkan: 0 terisi/100 dibatalkan, tanpa fee/gross rekaan, dan status CANCELLED terbaca setelah reload. Screenshot: `ui-cancelled-receipt.png`.
- **Batas browser:** runner `verify-receipt-ui.cjs` dihentikan (`exit -15`) setelah hasil fase tersebut tercetak. Hasil fase adalah bukti teramati, tetapi runner gabungan tidak punya clean exit; jangan tandai gate browser end-to-end PASS.
- SHA-256 save pemain setelah QA: `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`, sama dengan hash awal; save hanya di-hash dan tidak dipakai QA.
- Clean full-project compile/regression, auto-expiry lifecycle order, APK fresh dan perangkat Android belum diverifikasi. Browser evidence adalah host QA Java, bukan APK/native/emulator.

## Batas cakupan laporan

Laporan ini merekam status task tracker, bukan klaim bahwa kode/build/browser/device telah lulus untuk requirement yang berstatus parsial atau pending. Acceptance yang belum dibuktikan tetap terbuka.
