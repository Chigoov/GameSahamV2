# PLAN-03 — Order, receipt dan notifikasi

**Kerjakan hanya file ini sampai tuntas, serahkan hasil, lalu BERHENTI menunggu pemilik mengirim file berikutnya.**

Kelompok: G03. Paket: N07, N08, N09. Jumlah: 19 subtask. Target akhir proyek 20 paket / 126 subtask; scope satu kiriman hanya ID dalam file ini.

File asli: Work/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/plan.md. Disusun 24 Oktober 2026, Asia/Jakarta. Keberadaan file di disk bukan izin menjalankan tahap lain. Mode kiriman manual menggantikan mandat lanjut otomatis.

## Prasyarat dan batas kiriman

Pemilik mengirim/menunjuk file ini setelah file 02 selesai. Snapshot persiapan 24 Oktober 2026 mengamati tracker 22/22 ID PLAN-02 berstatus `PASS_WITHIN_SCOPE`; checkpoint, STATUS-REPORT.md, dan `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/RESULTS.md` juga melaporkan G02 selesai. Untuk aktivasi implementasi, baca status/checkpoint dan source terbaru lagi; bila ada ketidaksesuaian baru yang membuat alur order/uang/save tidak aman atau tidak dapat dijalankan, laporkan bukti serta dampaknya sebelum tindakan terkait. Temuan audit yang sudah diputuskan ditunda dicatat sebagai tindak lanjut audit, bukan penghambat administratif.

Status snapshot persiapan 24 Oktober 2026 dari `TRACKER.csv`: N07 = 4 `IN_PROGRESS`, 3 `PENDING`; N08 = 6 `IN_PROGRESS`; N09 = 6 `PENDING`; total PLAN-03 = 10 parsial, 9 PENDING, 0 PASS. Status per ID di bawah dipertahankan sama dengan tracker yang dibaca. Ini bukan pembaruan status pelaksanaan. Ketika plan dikirim untuk implementasi, baca tracker/source terbaru dan pertahankan bukti parsial yang masih cocok; jangan menganggap status parsial atau bukti subset sebagai selesai.

Master spesifikasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/master plan perbaikan.md. Tracker: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/Master-Plan-Gabungan-2026-10-24/TRACKER.csv. Checkpoint: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/Master-Plan-Gabungan-2026-10-24/CHECKPOINT.md.

Master adalah sumber kontrak produk/referensi relevan. Prompt global seluruh 126 task di sana bukan izin menjalankan semua tahap; scope sekarang hanya file yang dikirim pemilik ini.

## Hasil dan urutan dalam file ini

### G03 — Transaksi dari tiket sampai notifikasi

Kesesuaian: Pilihan order, payload, fill, receipt dan feedback harus mengikuti transaksi yang sama.

Urutan dalam kelompok: N07: pilih jenis/side/akun, quote dan review → N08: status, fill/biaya/kas, cancel dan reload → N09: feedback seluruh outcome → perjalanan transaksi lengkap.

Urutan praktis berdasarkan alur source: (1) petakan kontrak enum, field dan seluruh caller tiket; (2) verifikasi preview/capacity serta payload review terhadap bridge/facade; (3) ikuti submit ke matching, fill, fee/ledger dan save; (4) verifikasi getOrderStatus, history, live refresh, pembatalan dan restart; (5) petakan semua outcome ke inline/toast, dedupe dan aksesibilitas; (6) jalankan journey integrasi dan APK-02.

Peta source awal untuk diperiksa ulang pada snapshot implementasi:
- UI `app.js`: `openOrderTicket`, `updateOrderEstimate`, handler review/submit, `showOrderReview`, `showOrderReceipt`, `updateReceiptLive`, `renderReceiptAmounts`, `getOrders` history dan `showGameToast`.
- Bridge `TradingWebBridge.java`: `previewOrder`, `getOrderCapacity`, `submitOrder`, `getOrderStatus`, `cancelOrder`, `getOrders`; validasi akun pribadi/klien harus tetap terpusat di Java.
- Domain: `TradingFacade.previewOrder/getOrderCapacity/submitOrder/cancelOrder`, `MarketEngine`/`MatchingEngine`, `OrderManager`, `TradeFill`, `OrderBook`, ledger dan `SaveManager` export/restore.
- Kontrak source saat persiapan: enum `OrderType` berisi tepat `MARKET`, `LIMIT`, `STOP_LOSS`, `TAKE_PROFIT`. `requirePlayerAccount` menerima PERSONAL/CLIENT dan menerapkan syarat karier untuk akun klien. Cocokkan ulang enum/akses pada implementasi.
- Snapshot source baca-saja: ticket menghitung preview dari domain, membuat request ID baru untuk payload submit setelah preview berhasil, lalu mengirimnya ke `submitOrder`; receipt live membaca status berdasarkan request ID dan history dibangun ulang dari `getOrders`. Batalkan sisa melalui bridge/facade lalu save. DTO status menjumlahkan nilai dan biaya fill aktual; fee legacy yang tak ada ditandai unavailable. Ini peta alur untuk validasi, bukan jaminan semua state/caller telah diuji.
- Area pemeriksaan tanpa diagnosis bug: UI toast memakai satu container yang diganti (`replaceChildren`); bridge `getOrderStatus` punya catch yang mengembalikan JSON minimal; exception submit UI masuk ke state `UNKNOWN`. Pastikan antrean/dedupe, field status failure dan status request lama benar-benar menjaga outcome/domain serta tidak mengirim ulang order. Tidak ada reproduksi kegagalan yang dijalankan dalam persiapan ini.

Syarat keluar: Seluruh 19 ID lulus; review/confirm, akun, idempotency, seluruh status, ledger, notifikasi dan jalur gagal benar; tes parsial lama diulang sesuai acceptance.

## Dependensi, kepemilikan dan batas audit

- G03 mengikuti G01 dan G02. PLAN-02 telah dinyatakan selesai dalam tracker/checkpoint dan memiliki laporan `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/RESULTS.md`; source bersama tetap harus dibaca ulang karena hash source report berbeda dari snapshot saat persiapan ini. Pertahankan filter pasar/detail, bridge, rentang chart, source OHLCV dan delta PLAN-01 yang telah dikerjakan; jangan reset atau mengambil tugas plan lain.
- Tetapkan satu writer untuk source bersama dan periksa ownership sebelum edit `app.js`, `TradingWebBridge.java`, `TradingFacade.java`, kelas matching/trading atau save. Perubahan lintas-file boleh hanya untuk acceptance G03; catat dampak.
- `DAFTAR-KEKURANGAN-LINTAS-PLAN.md` memutuskan temuan audit ditunda sampai seluruh plan selesai; bukti tidak lengkap tetap tidak lengkap. Temuan historis 4 kegagalan full bridge/runner browser timeout dan catatan audit PLAN-01 tidak otomatis menghalangi administrasi G03. Periksa kembali bila menyentuh alur yang sama; bila ada reproduksi yang menunjukkan order/ledger/save tidak aman atau alur tidak dapat dijalankan, hentikan hanya tindakan terdampak, catat bukti/dampak dan minta keputusan spesifik—jangan memperbaikinya diam-diam.
- Tidak ada temuan baru persiapan ini yang terbukti sebagai bug transaksi/integritas uang. Jangan menambahkan ID kekurangan baru hanya untuk risiko yang belum direproduksi; bila pelaksana menemukan temuan kemudian, tambahkan ID berikutnya ke daftar kekurangan sesuai prosedur pemilik.
- Prasyarat implementasi tetap menunggu pengiriman/izin eksplisit pemilik atas file PLAN-03; file plan yang ada atau laporan completion PLAN-02 sendiri bukan izin mulai otomatis.

## Urutan kerja minimum yang disarankan

1. N07-01: pemetaan caller, enum dan field; verifikasi empat pilihan visual/native radio, side/akun dan nilai draft.
2. N07-02—N07-07: jalankan preview/quote/capacity domain, review, validasi dan submit untuk matriks sisi × jenis × bursa × akun; pertahankan nilai/tarif dan payload benar.
3. N08-01—N08-06: ikuti fill aktual sampai fee/gross/net/kas dan status; rapikan receipt dari DTO; cek live/history, pembatalan, request unknown, retry/idempotency, reload/save dan expiry produksi.
4. N09-01—N09-06: petakan seluruh transisi dari domain; baru kemudian samakan pesan inline/toast, dedupe/prioritas, live region, fokus, reconnect/offline/reload dan perpindahan akun.
5. Tutup dengan journey integrasi pada akun/save QA terisolasi, regresi sesuai source terkini, review screenshot/aksesibilitas/performa, sinkronkan status hanya dengan bukti, lalu build dan verifikasi APK-02.

Jangan melemahkan requirement asli pada bagian N07/N08/N09 atau mengganti enum/ekonomi demi menyederhanakan matriks.

## Batas bukti awal PLAN-02

Laporan `RESULTS.md` dan checkpoint mencatat 22/22 PASS, hasil browser/domain dan visual; persiapan ini membaca dokumen dan source, bukan menjalankan ulang pemeriksaan itu. Laporan mencatat domain chart 7/7, browser/movers, chart browser, UI matrix 20/20, G02 suite 106/106 dan critical sequence 10×100. Bukti tersebut mendukung penyelesaian G02 dalam status tracker, tetapi bukan acceptance PLAN-03. Daftar kekurangan lintas-plan yang dibaca memuat 7 temuan PLAN-01 berstatus `DITUNDA_SESUAI_PEMILIK`; tidak ada entri PLAN-02 pada snapshotnya. Ketiadaan entri bukan klaim audit PLAN-02 bebas temuan dan tidak menutup kemungkinan temuan baru saat G03 dijalankan.

APK-02 tetap wajib pada penutupan file ini sebagaimana bagian deliverable di bawah. Build, APK, signing, instalasi dan pengujian native adalah tahap berbeda; tidak ada yang dijalankan dalam persiapan dokumen ini.

## Pemeriksaan minimum saat pelaksanaan

- N07: assertion payload/outcome domain untuk semua enum yang didukung; side/akun, saldo nol, capacity/MAX, quote stale/gagal, lot IDX vs US, sesi tutup/halt/terkunci, quantity invalid, trigger, partial dan request ID berulang; keyboard/fokus/selected/200%.
- N08: cocokkan quantity, multi-fill VWAP, gross, fee aktual, kas bersih, reserve dan ledger; OPEN/PENDING_TRIGGER/PARTIAL/FILLED/CANCELLED/REJECTED/EXPIRED/UNKNOWN; order baru, reload, legacy fee unavailable, cancel sisa, duplikasi callback/retry, save failure/recovery. Uji expiry produksi secara controlled clock, bukan hanya DTO terminal.
- N09: satu input → outcome domain → satu feedback benar untuk pasar tutup/buka, saldo/posisi tidak cukup, locked/halt, antrean, partial/full fill, trigger, cancel/expiry, bridge error/unknown, save gagal dan offline catchup. Pastikan toast penting tidak saling menimpa, dedupe mengikuti request/status, serta reload tidak memunculkan sukses historis.
- Semua pemeriksaan ekonomi memakai fixture/save QA baru terisolasi, clock terkendali, source/classpath tercatat dan tidak mengubah tarif. Periksa akun pribadi/klien tidak tercampur; selaraskan deadline berita/time 24x dari Java jika jalur lifecycle disentuh.
- Gunakan state normal, kosong, loading, error, stale, locked, selected, pending, sukses/gagal; refresh/reload, keyboard/Back/fokus, target sentuh ≥48 dp, 100%/200%, reduced motion, screenshot aktual dan performa sesuai standar plan. Native tetap dilabeli `NOT_TESTED_BY_SCOPE` jika tidak diuji.
- Pertahankan log gagal; catat command, exit code, assertion, source hash, save QA hash, classpath dan hasil. APK-02 harus fresh dari snapshot stabil dan diverifikasi ID aplikasi/version/signature, SHA-256/ukuran serta kecocokan aset source. Jangan instal/ADB/perangkat.

## Temuan audit ditunda

Kekurangan yang eksplisit diputuskan pemilik untuk ditinjau setelah seluruh plan selesai tetap berstatus tertunda dan tidak mengurangi denominator 19 ID atau berubah menjadi `PASS`. Pada penutupan PLAN-03, tandai tindak lanjut audit/temuan yang tetap terbuka di laporan hasil; jangan memindahkan statusnya secara diam-diam. Bug yang tereproduksi dan langsung mengancam uang/order/save tetap harus ditangani atau dilaporkan sebagai blocker substantif sesuai bukti dan keputusan pemilik.

## Konteks pekerjaan yang perlu diperhatikan

Snapshot tracker 24 Oktober 2026 menunjukkan N08-01 s.d. N08-06 masih `IN_PROGRESS`; N07-01 s.d. N07-04 `IN_PROGRESS`, N07-05 s.d. N07-07 `PENDING`; keenam N09 `PENDING`. Bukti historis PLAN-03 yang tercantum tracker/plan: receipt terarah 7/7 dan subset bridge order 5/5 PASS; full bridge 35 tes mencatat 4 gagal; browser receipt mengeluarkan data status/readback tetapi runner/teardown tidak berakhir clean; EXPIRED pernah diuji sebagai DTO terminal fixture, bukan lifecycle expiry produksi. Perlakukan setiap hasil sebagai bukti parsial pada snapshotnya, bukan PASS acceptance. Pada implementasi G03, temukan RESULTS/log/source snapshot terbaru dan ulangi hanya pemeriksaan yang masih diperlukan pada source terbaru.

`Plans/02-pasar-dan-grafik/RESULTS.md` melaporkan source SHA-256 `82643f9046fb6098b2fc81c14a8bb2b61ce2a69c297fda76cb2466fdbb55eb00`; saat persiapan ini, source yang sama telah berubah dari laporan itu. Verifikasi manifest/hash terbaru sebelum menggunakan bukti lintas-plan. PLAN-02 mengubah `app.js`, `style.css`, dan `ChartHistoryRangeTest.java`; perubahan `app.js` menyentuh detail instrumen dan refresh yang berdekatan dengan tombol order. Periksa kembali seluruh caller ticket/detail, filter akun, review, receipt dan updater terhadap source stabil. Jangan menafsirkan status PASS PLAN-02 sebagai hasil uji G03.

## Kontrak produk yang tetap berlaku

- Waktu Java otomatis tunggal 24x: satu hari game = satu jam nyata pada aktif/background/ditutup. Tanpa play/pause pemain, speed atau skip. Pertahankan anchor, remainder, migrasi, kalender dan save.
- Efek berita 60/120 menit nyata memakai deadline persisten. Grafik memiliki empat rentang waktu game 6 jam/1 hari/7 hari/30 hari, default 1 hari; data berasal dari domain.
- Revisi visual 2 sudah disetujui: canvas navy #101824, panel #FFFFFF/#E8EEF5, teks #162332/#526175, aksi #165DCC, untung #087F5B, rugi #C22B43, bisnis #6D40AF, progres #FFF5D9 dengan teks #684300. Lima tab tetap Beranda/Pasar/Portofolio/Bisnis/Lainnya. Frasa tema terang pada ID lama mengikuti revisi 2; tidak kembali ke background terang menyeluruh atau meminta ulang persetujuan yang sama.
- Java adalah otoritas uang, waktu, order, posisi dan persistence. UI hanya input/presentasi. Gunakan precision serta angka domain yang sah; tanpa saldo, fill, biaya, holder atau reward palsu. Pertahankan isolasi akun pribadi/klien.
- Hapus UI lab sandbox/uji stres, lindungi data save serta shared risk dan replay perdagangan utama. Posisi pribadi berbeda dari komposisi pemegang saham emiten.
- Pertahankan seluruh delta source termasuk N08 parsial; jangan reset/clean/stash atau kembali ke archive jeda lama. Root produksi berbeda dari checkout lama Game Trading Saham.
- Save pemain Artifacts/LocalPreview/preview-save/save_game_v2.json hanya di-hash. Semua QA/transaksi/manipulasi waktu memakai save baru terisolasi.
- Hermes satu writer; tanpa delegasi/pesan lintas chat, perubahan provider/model/credentials/pengaturan global, push/publish, ADB, instalasi atau pengujian perangkat baru. Native tetap NOT_TESTED_BY_SCOPE.

## Cara mengerjakan file aktif

1. Verifikasi root/Git/branch/HEAD/delta dan hash source/save. Baca AGENTS.md, keputusan aktif DESIGN.md, tracker dan checkpoint terkini. Jangan menjalankan preflight.py/capture-pause.py historis untuk menimpa tracker.
2. Aktifkan hanya file dan kelompok ini. Telusuri alur serta semua caller, gunakan helper/runner existing, implementasikan minimum yang benar. Jangan mengubah tarif/hadiah/formula ekonomi demi tes.
3. Tuntaskan seluruh requirement dan acceptance setiap ID file ini. Bukti parsial hanya referensi; ulangi penelusuran serta pengujian yang diperlukan pada source terbaru. Tes sebagian lulus belum menutup ID yang masih punya kriteria belum terbukti.
4. Shared helper/style/bridge boleh disentuh hanya untuk kebutuhan alur ini. Catat dampak; jangan mengambil fitur plan lain. Dependency rusak yang sebelumnya lulus harus dilaporkan dan diperbaiki minimum atau dinyatakan blocker yang perlu keputusan.
5. Tes, review source/visual, perbaiki dan tes ulang. Setelah dua kegagalan sama tanpa diagnosis baru, laporkan PERLU PERHATIAN dan ubah pendekatan. Bagian independen yang dikerjakan tetap dalam file ini.
6. Sinkronkan tracker, laporan, STATUS dan checkpoint dengan bukti. Pertahankan seluruh ID/denominator. Perubahan shared source tidak otomatis meluluskan task di luar file.

## Standar pemeriksaan untuk cakupan file ini

- Seluruh acceptance paket di bawah dan standar proyek/master bagian 19 tetap berlaku; pengiriman bertahap tidak menurunkan kriteria.
- Jalur kritis waktu/berita/order/save/lifecycle yang disentuh wajib minimal 10 urutan × 100 aksi bermakna dengan seed dan invariant per aksi. No-op/read berulang dan exception yang ditelan tidak memenuhi urutan kritis.
- Periksa ledger, kas/reservasi/saham/utang, idempotency, fill/pembayaran tepat sekali, elapsed serta deadline. Gunakan controlled clock untuk boundary; jangan menunggu 1–2 jam sungguhan.
- Periksa semua state yang berlaku: normal/empty/loading/error/stale/locked/selected/pending/sukses/gagal; akun, refresh/reload, keyboard/fokus/Back, target sentuh 48, kontras, reduced motion dan teks 100%/200%. Viewport proyek: 320×568, 360×640, 390×844, 412×915, 430×932, 768×1024, 1440×900, 470×658, 462×648; sertakan 360×800 pada audit terkait.
- Buka screenshot aktual; geometri/console bersih saja tidak meluluskan visual. Jalankan empat blok Delivery Gate Antislop pada cakupan visual yang ditutup dan nyatakan batasnya.
- P95 aksi lokal ≤300 ms, navigasi ≤1 detik, feedback kerja berat ≤300 ms dan UI responsif. Minimal 30 sampel untuk P95. Catchup panjang/riwayat 30 hari mengikuti acceptance terkait; jangan mengecilkan beban agar lulus.
- Catat command/exit code/assertion/log, source hash, classpath dan save/output. Incremental compile/subset bukan clean full-project compile. Source berubah membatalkan bukti bagian terdampak sampai diperiksa ulang.

## Daftar ID yang ditugaskan

Setiap ID mempertahankan requirement dan status awal tracker. Parsial tetap belum selesai; tes lama belum menggantikan seluruh acceptance.

- [ ] **N07-01 — IN_PROGRESS**: Telusuri seluruh caller `#order-type`, side, limit/trigger, percent buttons, review dan submit; inventaris enum yang benar.
  - Implementasi/bukti parsial tercatat: Ticket type/side/quote/MAX/review/submit traced
  - Rujukan bukti: android/app/src/main/assets/www/app.js
  - Pekerjaan/keputusan berikutnya: Hanya saat G03 aktif: Periksa seluruh caller type/side/limit/trigger/persentase/review/submit dan enum aktual; catat pemetaan field ke payload. Tetap belum selesai sampai seluruh kriteria N07-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N07-02 — IN_PROGRESS**: Implementasikan pilihan visual dengan native radio atau kontrol setara yang mendukung keyboard dan state selected.
  - Implementasi/bukti parsial tercatat: Native radio options replace dropdown; browser matrix pending
  - Rujukan bukti: android/app/src/main/assets/www/index.html
  - Pekerjaan/keputusan berikutnya: Hanya saat G03 aktif: Uji seluruh native radio melalui klik/keyboard, selected/focus/disabled dan 200%; memilih jenis tidak boleh mengirim transaksi. Tetap belum selesai sampai seluruh kriteria N07-02 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N07-03 — IN_PROGRESS**: Hubungkan pilihan ke field/payload yang tepat dan pertahankan nilai draft yang masih relevan.
  - Implementasi/bukti parsial tercatat: Conditional field/payload connected; caller matrix pending
  - Rujukan bukti: android/app/src/main/assets/www/app.js
  - Pekerjaan/keputusan berikutnya: Hanya saat G03 aktif: Uji pergantian jenis/side/akun dan draft; hanya field relevan masuk payload, tanpa limit/trigger lama atau quantity berubah diam-diam. Tetap belum selesai sampai seluruh kriteria N07-03 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N07-04 — IN_PROGRESS**: Samakan estimasi dan MAX dengan akun serta quote Java; hilangkan fallback angka palsu pada jalur tersebut.
  - Implementasi/bukti parsial tercatat: Java capacity and quote replace fake balance/fee fallback; edge cases pending
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N07-green-01/junit-latest.log
  - Pekerjaan/keputusan berikutnya: Hanya saat G03 aktif: Tuntaskan quote/MAX untuk akun pribadi/klien, IDX/US, saldo nol/reservasi, data gagal dan harga stale; tanpa fallback saldo atau biaya rekaan. Tetap belum selesai sampai seluruh kriteria N07-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N07-05 — PENDING**: Pertahankan review/confirm, validasi, idempotency dan handling pemicu.
  - Pekerjaan/keputusan berikutnya: Hanya saat G03 aktif: Pertahankan review/confirm, validasi, idempotency dan handling pemicu. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N07-05 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N07-06 — PENDING**: Perbarui selector tes yang masih `select_option` menjadi aksi radio yang benar, tanpa melemahkan assertion payload/outcome.
  - Pekerjaan/keputusan berikutnya: Hanya saat G03 aktif: Perbarui selector tes yang masih `select_option` menjadi aksi radio yang benar, tanpa melemahkan assertion payload/outcome. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N07-06 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N07-07 — PENDING**: Uji Beli/Jual × empat jenis × IDX/US × akun pribadi/klien yang tersedia, termasuk kasus tidak didukung.
  - Pekerjaan/keputusan berikutnya: Hanya saat G03 aktif: Uji Beli/Jual × empat jenis × IDX/US × akun pribadi/klien yang tersedia, termasuk kasus tidak didukung. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N07-07 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N08-01 — IN_PROGRESS**: Tetapkan field utama per status dan field detail; petakan semua status existing.
  - Implementasi/bukti parsial tercatat: Receipt fields mapped for OPEN/PARTIAL/FILLED/CANCELLED/REJECTED/EXPIRED and unknown; EXPIRED tested as terminal DTO fixture only; production expiry lifecycle unverified
  - Tes tercatat: 7 targeted receipt tests PASS
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N08-resume-targeted/receipt-7.log
  - Pekerjaan/keputusan berikutnya: Hanya saat G03 aktif: Lengkapi seluruh status dan tindakan yang tersedia; telusuri expiry produksi karena fixture EXPIRED terminal belum membuktikan lifecycle otomatis. Tetap belum selesai sampai seluruh kriteria N08-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N08-02 — IN_PROGRESS**: Lengkapi DTO harga/nilai/biaya dari receipt/ledger bila dibutuhkan; dokumentasikan gross/net.
  - Implementasi/bukti parsial tercatat: Actual buyer/seller fees captured in all three matching paths; optional save fields preserve legacy fills without inventing fee; bridge computes fill gross, VWAP, actual fee and cash impact
  - Tes tercatat: 7 receipt-specific tests PASS; 5 order bridge subset PASS; incremental compile only, not clean full build
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N08-resume-targeted/receipt-7.log; bridge-subset-5.log; RESULTS.md
  - Pekerjaan/keputusan berikutnya: Hanya saat G03 aktif: Kompilasi bersih source kini; ulangi fill berbobot, fee buyer/seller, gross/net/kas dan legacy save, kemudian regresi terkait tanpa menganggap subset sebagai full PASS. Tetap belum selesai sampai seluruh kriteria N08-02 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N08-03 — IN_PROGRESS**: Bangun ulang ringkasan dan tindakan, sembunyikan ID mentah dari default.
  - Implementasi/bukti parsial tercatat: Receipt UI omits mode/account/raw UUID rows; displays localized actual gross/VWAP/fee/net cash and cancelled quantity. QA host page, ticket, review, filled/cancelled receipt visually exercised at 470x658
  - Tes tercatat: Node/Chromium output shows no page/console errors through displayed interactions; combined runner teardown timed out, not recorded as overall PASS
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N08-resume-targeted/ui-before-order.png; ui-detail-inspect.png; ui-ticket-ready.png; ui-order-review.png; ui-receipt.png; ui-cancelled-receipt.png
  - Pekerjaan/keputusan berikutnya: Hanya saat G03 aktif: Ulangi receipt dan aksi per status dengan browser yang keluar normal; tuntaskan keyboard/fokus, 200%, visual dan pastikan ID/mode mentah tidak kembali. Tetap belum selesai sampai seluruh kriteria N08-03 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N08-04 — IN_PROGRESS**: Perbarui updater live dan riwayat agar memakai representasi yang konsisten.
  - Implementasi/bukti parsial tercatat: Live updater refreshes receipt monetary rows and cancelled remainder; browser output shows fresh-page history/status readback matches receipt values; full runner teardown incomplete
  - Tes tercatat: Filled and cancelled status values printed after reload; overall browser runner was stopped after output and must not be counted clean PASS
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N08-resume-targeted/verify-receipt-ui.cjs; ui-receipt-bottom.png; ui-cancelled-receipt.png
  - Pekerjaan/keputusan berikutnya: Hanya saat G03 aktif: Buktikan live transition, riwayat dan reload dari state tersimpan memakai angka sama tanpa mereset fokus/scroll; simpan hasil runner dengan clean exit. Tetap belum selesai sampai seluruh kriteria N08-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N08-05 — IN_PROGRESS**: Uji order fresh, reload, partial fill, batal sisa, expired, rejected dan response belum diketahui.
  - Implementasi/bukti parsial tercatat: Receipt tests cover matched/reload, partial/cancel/reload, legacy fee absent, OPEN no fill, unknown request, natural REJECTED and EXPIRED terminal DTO fixture; browser filled/cancel status output observed
  - Tes tercatat: 7 targeted receipt tests PASS; browser test command timed out in teardown after status/readback output
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N08-resume-targeted/receipt-7.log; ui-receipt.png; ui-cancelled-receipt.png; RESULTS.md
  - Pekerjaan/keputusan berikutnya: Hanya saat G03 aktif: Ulangi fresh/reload/partial/cancel/rejected/unknown dan expiry produksi; simpan bukti fase lengkap serta runner browser yang selesai normal. Tetap belum selesai sampai seluruh kriteria N08-05 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N08-06 — IN_PROGRESS**: Verifikasi pembatalan, retry, dan laporan kas tetap sesuai ledger.
  - Implementasi/bukti parsial tercatat: Partial cancellation releases remaining reservation; exact cash, zero residual reserve, duplicate cancel idempotency, ledger reconciliation and reload tested; browser cancel/reload status printed
  - Tes tercatat: 7 receipt tests PASS; 5 order bridge tests PASS; full bridge suite has 4 unresolved failures unrelated to selected order methods
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N08-resume-targeted/receipt-7.log; bridge-subset-5.log; bridge-full-35.log; ui-cancelled-receipt.png
  - Pekerjaan/keputusan berikutnya: Hanya saat G03 aktif: Ulangi cancel/idempotency/kas/reservasi/reconcile/reload; diagnosis empat kegagalan bridge lalu jalankan ulang sesuai kontrak baru sebelum penutupan. Tetap belum selesai sampai seluruh kriteria N08-06 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N09-01 — PENDING**: Inventaris seluruh jalur submit, gagal preview, gagal confirm, cancel, partial fill, trigger dan perubahan sesi.
  - Pekerjaan/keputusan berikutnya: Hanya saat G03 aktif: Inventaris seluruh jalur submit, gagal preview, gagal confirm, cancel, partial fill, trigger dan perubahan sesi. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N09-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N09-02 — PENDING**: Samakan kode outcome/error domain dan bahasa pesan; jangan mengubah status ekonomi.
  - Pekerjaan/keputusan berikutnya: Hanya saat G03 aktif: Samakan kode outcome/error domain dan bahasa pesan; jangan mengubah status ekonomi. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N09-02 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N09-03 — PENDING**: Hubungkan feedback inline dan toast pada titik transisi yang benar.
  - Pekerjaan/keputusan berikutnya: Hanya saat G03 aktif: Hubungkan feedback inline dan toast pada titik transisi yang benar. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N09-03 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N09-04 — PENDING**: Perbaiki dedupe, prioritas pesan, masa tampil, dan recovery status belum pasti.
  - Pekerjaan/keputusan berikutnya: Hanya saat G03 aktif: Perbaiki dedupe, prioritas pesan, masa tampil, dan recovery status belum pasti. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N09-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N09-05 — PENDING**: Verifikasi queue, focus, live region, teks panjang dan keyboard pada layar kecil.
  - Pekerjaan/keputusan berikutnya: Hanya saat G03 aktif: Verifikasi queue, focus, live region, teks panjang dan keyboard pada layar kecil. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N09-05 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N09-06 — PENDING**: Uji beberapa event berdekatan, callback duplikat, reconnect/reload, offline catchup serta perubahan akun.
  - Pekerjaan/keputusan berikutnya: Hanya saat G03 aktif: Uji beberapa event berdekatan, callback duplikat, reconnect/reload, offline catchup serta perubahan akun. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N09-06 terbukti; catat command, hasil, bukti dan kekurangan tersisa.

## Rincian kontrak dan acceptance paket

Rincian diambil dari master aktif; checklist dipindahkan ke daftar ID di atas. Keputusan revisi 2 mengesampingkan palette/background lama yang bertentangan.

## 11. N07: Pemilihan jenis order melalui antarmuka yang mudah dipahami

### 11.1 Bentuk interaksi yang direkomendasikan

Ganti dropdown tunggal dengan empat pilihan berbentuk **radio cards** atau segmented control yang tetap terlihat. Dua kolom pada layar cukup; satu kolom bila teks 200% atau ruang tidak cukup. Satu pilihan hanya memilih jenis order, tidak langsung mengirim transaksi.

| Label utama pengguna | Enum domain | Field/kondisi yang ditampilkan |
|---|---|---|
| Market | `MARKET` | Jumlah, referensi harga beli/jual yang tersedia, estimasi biaya; field harga limit tidak aktif. |
| Limit | `LIMIT` | “Harga beli maksimal” atau “Harga jual minimal”, jumlah, estimasi dan status menunggu bila belum match. |
| Stop Loss | `STOP_LOSS` | Harga pemicu, jumlah, arah kondisi sesuai sisi; tidak menyebut harga eksekusi terjamin. |
| Take Profit | `TAKE_PROFIT` | Harga pemicu, jumlah, kondisi sesuai sisi; setelah terpancing mengikuti jenis eksekusi yang didukung domain. |

Enum di tabel harus dicocokkan dengan `OrderType` aktual sebelum implementasi; jangan mengirim enum tebakan bila source memakai nama lain. Bentuk visual harus membantu membedakan “sekarang”, “harga target”, dan “pemicu”. Boleh menggunakan satu baris pendek atau label field dinamis untuk kondisi, bukan paragraf tutorial atau dialog belajar tambahan.

### 11.2 Perilaku yang harus tetap benar

1. Sisi Beli/Jual tetap keputusan terpisah. Warna hijau/merah saja tidak menentukan sisi. Saat sisi berganti, label dan validasi pemicu harus ikut berganti tanpa mengganti quantity diam-diam.
2. UI merepresentasikan kemampuan domain. Jika stop/take-profit untuk sisi tertentu tidak didukung, tampilkan alasan singkat dan nonaktifkan pilihan itu; jangan menjanjikan short selling, proteksi, atau tipe eksekusi baru.
3. Menekan jenis order memperbarui field relevan, estimasi, dan tombol tinjau. Field yang tidak relevan tidak ikut payload sebagai harga pemicu/limit lama.
4. `MARKET` tidak menjamin filled atau harga terakhir. Sesi tutup, halt, kekurangan likuiditas, saldo/posisi kurang, dan sektor terkunci tetap diperiksa Java.
5. `LIMIT` dapat mengantre atau terisi sebagian. Stop-loss dan take-profit menggunakan pemicu yang benar; jangan menyatakan order sudah terisi ketika baru dipasang.
6. Pertahankan alur preview → review → confirm, request ID idempotent, isolasi akun klien, dan perhitungan lot per instrumen.
7. Gunakan data akun terpilih untuk tombol 25/50/75/MAX. Source yang diperiksa masih memiliki fallback saldo `50.000.000`, asumsi komisi `0,15%`, dan pembulatan minimum satu lot. Pada jalur yang tersentuh, ganti estimasi/fallback yang dapat memunculkan jumlah tidak mampu dibeli dengan quote domain; jangan mengubah tarif ekonominya.
8. Jika data saldo/harga gagal dimuat, nonaktifkan komitmen dan jelaskan singkat. Jangan mengasumsikan modal awal atau membuat kuantitas minimum satu lot saat saldo nol.

### 11.4 Penerimaan dan bukti

Pengguna dapat melihat jenis yang dipilih, field yang harus diisi, dan langkah “Tinjau beli/jual” tanpa membaca tutorial. Keyboard dapat memilih radio dan mengakses field; focus terlihat. Switching type tidak mengirim transaksi atau memakai harga stale. Payload terverifikasi sesuai enum dan angka yang ditinjau. Tes mencakup saldo nol, quantity invalid, lot US versus IDX, sesi tutup, order tertunda, fill sebagian, pemicu aktif, dan retry request yang sama.

## 12. N08: Ringkas halaman status order

### 12.1 Susunan informasi

Gunakan ringkasan hasil dengan urutan:

1. Status utama: **Terisi penuh / Terisi sebagian / Menunggu harga / Menunggu pemicu / Ditolak / Dibatalkan / Kedaluwarsa**, mengikuti status domain yang benar.
2. Identitas transaksi: **Beli BBCA** atau **Jual BBCA**, nama akun manusiawi bila diperlukan untuk membedakan pribadi dan klien.
3. Jumlah: saham/lot yang sudah terisi dan sisa hanya jika masih relevan.
4. Harga rata-rata terisi, nilai transaksi, biaya, dan total kas keluar/masuk **jika data aktual tersedia**. Pada order menunggu, tampilkan harga limit/pemicu, bukan harga fill buatan.
5. Satu tindakan utama yang masuk akal: “Lihat kepemilikan”, “Lihat order aktif”, “Perbaiki order”, atau “Kembali ke pasar”. “Batalkan sisa” hanya untuk status yang benar-benar bisa dibatalkan.

Hapus baris Mode, kode akun mentah, pasangan UUID, pesan generik berulang, dan “Periksa status” yang redundan bila updater sudah berjalan. ID lengkap tetap berada dalam riwayat/detail teknis transaksi yang memang dibutuhkan untuk penelusuran, bukan memenuhi halaman utama.

### 12.2 Data dan konsistensi

- Telusuri `getOrderStatus`, `Order`, `TradeFill`, ledger, dan receipt yang ada. DTO saat diperiksa belum menjamin harga fill rata-rata, biaya, dan total aktual. Tambahkan nilai dari domain hanya setelah sumbernya jelas.
- Untuk beberapa fill, gunakan harga rata-rata tertimbang berdasarkan quantity fill; jangan memakai rata-rata sederhana atau harga quote terakhir.
- Bedakan jumlah diminta, terisi, dan tersisa. Pembatalan sisa tidak membatalkan fill yang sudah terjadi.
- Setelah restart, `orderDraft` mungkin tidak ada. Halaman harus dapat dibangun dari order/receipt tersimpan, tanpa bergantung pada variabel sementara.
- `updateReceiptLive` harus memperbarui komponen ringkas yang sama, bukan menambahkan ulang baris lama. Tidak mencuri fokus atau melompat scroll saat status berubah.
- Untuk request dengan outcome belum diketahui karena koneksi/bridge error, tampilkan “Status belum dapat dipastikan” dan cek request lama; jangan otomatis mengirim request ID baru.

Penerimaan: layar awal dapat menjawab “apa yang terjadi, berapa terisi, berapa nilainya, dan apa yang bisa dilakukan berikutnya” tanpa UUID dan teks mode. Informasi kritis tidak dihapus demi ringkas. Bukti mencocokkan ringkasan dengan fill serta ledger nyata.

## 13. N09: Notifikasi order dan kondisi pasar

### 13.1 Gunakan fondasi existing

Pakai `showGameToast` dan status inline yang sudah ada. Jangan menambah library notifikasi. Toast adalah feedback cepat; error yang memerlukan perbaikan input tetap berada dekat field dan tidak hilang setelah 3,2 detik.

`container.replaceChildren(toast)` dapat menimpa notifikasi penting. Gunakan antrean kecil atau prioritas yang cukup untuk alur ini. Hindari sistem notifikasi generik dengan banyak layer. Dedupe berdasarkan event/request/status yang stabil, bukan hanya lima detik sejak pesan terakhir.

### 13.2 Matriks kondisi wajib

| Kondisi | Pesan/tindakan yang disarankan | Ketentuan |
|---|---|---|
| Bursa tutup ketika hendak kirim | “Pasar sedang tutup” + waktu buka berikutnya jika domain menyediakannya | Jelaskan apakah order ditolak atau mengantre sesuai aturan jenis; jangan menyamakan semua jenis. |
| Sesi berubah menjadi buka/tutup | Status kecil dan satu notifikasi bila relevan | Hanya saat transisi, bukan tiap polling. Sertakan bursa yang benar. |
| Saldo tidak cukup | “Saldo tersedia belum cukup” + angka kurang jika tersedia | Inline di ticket dan feedback singkat; jangan bocorkan akun lain. |
| Saham tersedia tidak cukup | “Saham yang bisa dijual belum cukup” | Hitung reservasi order lain, bukan quantity total saja. |
| Sektor/fitur terkunci | “Terbuka di level …” atau syarat jabatan | Gunakan syarat domain aktual. |
| Order diterima dan mengantre | “Order dipasang” | Jangan memakai “Pembelian berhasil” jika belum fill. |
| Fill sebagian | “Terisi … dari … saham” | Nilai terisi dan sisa cocok dengan order; tidak mengulang event lama. |
| Fill penuh | “Beli/Jual … selesai” | Gunakan jumlah/harga aktual bila tersedia. |
| Pemicu terpasang/aktif | “Pemicu dipasang” / “Pemicu aktif” | Terpisah dari notifikasi fill. |
| Dibatalkan/kedaluwarsa | “Sisa order dibatalkan” / “Order berakhir” | Kas/saham reservasi dilepas sesuai domain; fill lama tetap ada. |
| Likuiditas kurang atau halt | Alasan ringkas dan langkah berikutnya | Bedakan dari saldo kurang; jangan menjanjikan kapan pasti fill. |
| Bridge error / hasil belum pasti | “Status belum dapat dipastikan” + “Periksa order” | Tidak memicu duplikasi transaksi. |
| Save gagal | “Progres terakhir belum tersimpan” + coba simpan ulang | Pesan penting tetap dapat ditemukan; jangan menyatakan tersimpan. |
| Aktivitas offline selesai diproses | Ringkasan sekali saat kembali bila ada perubahan bermakna | Tidak mengirim puluhan toast untuk semua event historis. |

### 13.3 Perilaku dan aksesibilitas

- Notifikasi informasional memakai pengumuman sopan (`status`); error penting menggunakan `alert` secara terukur. Jangan membuat puluhan live region yang saling memotong.
- Pesan memiliki tombol tutup, target sentuh memadai, tidak menutupi submit/order review, serta dapat dibaca pada 200%.
- Jika judul dan body cukup, jangan menambahkan jargon internal. Alasan singkat seperti “Harga pemicu harus di bawah harga sekarang” adalah validasi UI, bukan tutorial yang dilarang N07.
- Transisi status setelah reload tidak boleh menghasilkan toast sukses palsu untuk semua order lama. Inisialisasi baseline pengamatan kemudian kirim hanya perubahan baru; ringkasan offline satu kali menggunakan receipt/event ID tersimpan bila diperlukan.
- Notifikasi native saat aplikasi ditutup belum otomatis tercakup oleh toast. Untuk rencana ini, wajibkan in-app + ringkasan saat kembali. Jika pemilik menghendaki push/notifikasi OS ketika tertutup, jadikan perluasan eksplisit dengan izin/perilaku Android dan bukti perangkat; jangan mengklaimnya sudah selesai dari uji browser.

Penerimaan: setiap kondisi pada matriks memiliki bukti input → outcome domain → pesan yang benar. Tidak ada banjir toast, transaksi ganda, atau pesan “berhasil” saat order sekadar diterima.

## Deliverable dan syarat menutup file

Bukti baru disimpan pada Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/. Gunakan save QA terisolasi; pertahankan log gagal.

1. Seluruh 19 ID file ini memenuhi requirement dan seluruh acceptance pada snapshot source yang sesuai. Bukti parsial tetap belum selesai.
2. RESULTS.md memuat ID/status, perubahan/asumsi, command/exit code, tes/review/performa, lokasi bukti, source hash, hash save dan temuan terbuka.
3. Tracker existing, STATUS-REPORT.md, Work/STATUS.json dan checkpoint sinkron. Tidak membuat master/tracker tandingan. Saat selesai catat WAITING_FOR_OWNER_PLAN dan jangan mengaktifkan file berikut.
4. Tutup atau catat kepemilikan/status proses QA file ini secara jelas. Jangan menutup preview/proses lain atau meninggalkan proses yang melanjutkan agent ke scope berikut.

### APK-02 wajib diserahkan dalam file ini

Bangun kandidat APK fresh dari snapshot stabil setelah pemeriksaan yang diwajibkan. Verifikasi application ID com.tradingglobal3d.mobile, version metadata, signature yang berlaku, SHA-256/ukuran dan kecocokan index.html/app.js/CSS/font/aset/data dengan source. Pertahankan identitas update/signing yang sah. Sertakan ID selesai/terbuka, hasil gagal serta batas host/browser/APK/native. Tidak instalasi/ADB/perangkat/push.

File belum ditutup sebelum APK-02 dan bukti paket diserahkan. APK awal tidak meluluskan alur lain yang masih parsial. APK final file 08 menunggu seluruh 126 ID dan gate akhir.

## Instruksi berhenti

Setelah seluruh kriteria dan deliverable file ini selesai, jawab dengan PLAN-03, status ID, bukti utama, APK bila diwajibkan, dan temuan tersisa. Kemudian **BERHENTI DAN TUNGGU FILE PLAN BERIKUTNYA DARI PEMILIK**.

Jangan memulai plan berikut dari urutan, membuat reminder/automation untuk lanjut, atau menganggap pemilik diam sebagai persetujuan. Bila ada blocker nyata sebelum selesai, laporkan kekurangan spesifik dan tetap jangan berpindah file.
