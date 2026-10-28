# PLAN-04 — Portofolio dan kepemilikan

**Kerjakan hanya file ini sampai tuntas, serahkan hasil, lalu BERHENTI menunggu pemilik mengirim file berikutnya.**

Kelompok: G04. Paket: N11, N17, N10, N15. Jumlah: 27 subtask. Target akhir proyek 20 paket / 126 subtask; scope satu kiriman hanya ID dalam file ini.

File asli: Work/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/plan.md. Disusun 24 Oktober 2026, Asia/Jakarta. Keberadaan file di disk bukan izin menjalankan tahap lain. Mode kiriman manual menggantikan mandat lanjut otomatis.

## Prasyarat dan batas kiriman

Pemilik mengirim/menunjuk file ini setelah file 03 selesai. Snapshot persiapan 24 Oktober 2026 **belum** menemukan bukti G03 ditutup: tracker masih mencatat 0/19 PASS (10 `IN_PROGRESS`, 9 `PENDING`), `STATUS-REPORT.md` juga mencatat 0/19, dan checkpoint utama masih menggambarkan penutupan G02. Karena itu, persiapan dokumen G04 dapat diselesaikan dan file ini siap ditinjau/dikirim, tetapi **implementasi G04 tetap menunggu** seluruh 19 ID G03 `PASS_WITHIN_SCOPE` pada tracker dan bukti/checkpoint yang konsisten, serta instruksi eksplisit pemilik. Temuan audit yang secara eksplisit ditunda tidak menjadi penghambat administratif; task G03 yang belum lulus adalah ketergantungan kelompok yang nyata, bukan temuan audit tertunda.

Status snapshot persiapan 24 Oktober 2026 dari `TRACKER.csv`: N11 = 6 `PENDING`; N17 = 6 `PENDING`; N10 = 7 `PENDING`; N15 = 6 `IN_PROGRESS` dan 2 `PENDING`; total 27 ID = 6 parsial, 21 PENDING, 0 PASS. Status per ID di bawah dipertahankan sesuai tracker; ini bukan pembaruan status implementasi. Saat file dikirim untuk pelaksanaan, baca ulang tracker/source/bukti G03 terkini; jangan mewarisi status hanya dari teks plan ini.

Master spesifikasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/master plan perbaikan.md. Tracker: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/Master-Plan-Gabungan-2026-10-24/TRACKER.csv. Checkpoint: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/Master-Plan-Gabungan-2026-10-24/CHECKPOINT.md.

Master adalah sumber kontrak produk/referensi relevan. Prompt global seluruh 126 task di sana bukan izin menjalankan semua tahap; scope sekarang hanya file yang dikirim pemilik ini.

## Hasil dan urutan dalam file ini

### G04 — Portofolio dan kepemilikan

Kesesuaian: Kategori, posisi pribadi dan komposisi pemegang saham bergantung pada hasil transaksi G03.

Urutan dalam kelompok: Inventaris/mapping bersama N11/N17 → kategori dan rute Portofolio → N10: posisi akun pribadi → N15: pemegang saham emiten yang terpisah → pemeriksaan semua tujuan, settlement dan restore.

Peta implementasi awal (baca ulang saat G04 benar-benar aktif):
- UI Portofolio berada di `app.js`: `loadAsetView()`, `syncPortfolioLive()`, `openPositionDetailModal()`; navigasi tab mengarah ke `loadAsetView`. Detail emiten mempunyai snapshot posisi akun dan tab pemegang saham yang berbeda. Telusuri seluruh launcher/opener agar N11/N17 tidak membuat tujuan ganda atau orphan.
- `TradingWebBridge.getPortfolio(accountId)` memvalidasi akun pemain, lalu membangun snapshot nilai/kas/reservasi/posisi dari akun Java. `getIssuerOwnership(instrumentId)` meneruskan snapshot issuer berbeda. Verifikasi semua pemakai/DTO, mata uang/lot, empty/error dan identitas account/instrument pada source stabil.
- Java `TradingFacade.getIssuerOwnership()` menggabungkan posisi akun yang tercatat dan shares outstanding, menandai cakupan parsial/tidak konsisten serta tidak mengatribusikan saham unmapped sebagai holder fiktif. Riwayat source mencatat dua tes dasar, tetapi tracker masih menyebut registry, settlement/corporate action, klien dan reload belum tuntas.
- PLAN-03 belum terbukti selesai pada snapshot ini. Sesudah G03 selesai, petakan perubahan aktual `Order`/`TradeFill`/fee/receipt/cancel dan restore; tentukan event fill mana yang memperbarui posisi dan holder agar jumlah, kas, ledger, portfolio receipt dan snapshot kepemilikan tetap sinkron. Jangan menganggap fitur/tes receipt parsial sudah meluluskan N10/N15.

Syarat keluar: Seluruh 27 ID lulus; fitur Portofolio terjangkau, posisi pribadi cocok domain, holder emiten bukan placeholder, supply/akun tidak dihitung ganda, Back/fokus/reload benar.

## Dependency, kepemilikan, dan batas audit

- G04 hanya dimulai setelah seluruh 19 ID G03 berstatus `PASS_WITHIN_SCOPE` dan bukti/checkpoint G03 konsisten; tracker/laporan yang dibaca masih menunjukkan 10 parsial/9 pending dan tidak ditemukan laporan penutupan `RESULTS.md` pada folder deliverable G03. Tracker tetap merujuk bukti parsial terarah (termasuk RESULTS receipt N08 di lokasi lain). Jangan menganggap APK-02 atau catatan browser parsial sebagai pengganti syarat tersebut. Setelah bukti baru muncul, rekonsiliasi tracker serta bukti sebelum kerja.
- Penundaan audit pemilik hanya berlaku pada temuan audit lintas-plan yang ditulis di `DAFTAR-KEKURANGAN-LINTAS-PLAN.md`; jangan jadikan mereka penghambat administratif untuk penyiapan Plan 4. Jangan menaikkannya menjadi PASS. Kegagalan bridge G03 yang masih tertera pada tracker adalah tugas/dependency yang belum selesai, bukan temuan audit yang dianggap ditutup.
- Sebelum menulis, pastikan kepemilikan source bersama dan snapshot/hash terkini. PLAN-03/G03 menyentuh order, fill, fee, status, ledger dan save; pelaksana G04 harus mempertahankan hasilnya serta memeriksa ulang integrasi yang benar-benar berubah, tanpa menimpa delta G03.
- Belum ada temuan baru dari pembacaan ini yang membuktikan transaksi/posisi/save rusak sehingga memerlukan keputusan pemilik sekarang. Bila ketika G03 selesai ditemukan fill tidak terhubung ke posisi/ownership atau settlement tidak dapat direkonsiliasi, dokumentasikan ID terkait, input/status transaksi, dampak ekonomi/save, dan keputusan model konkret yang dibutuhkan. Jangan membuat owner/fraction kosmetik atau memperbaikinya diam-diam.
- Scope tetap N11/N17/N10/N15 saja; bukan implementasi Bisnis, Lainnya, removal N19, atau APK baru.

## Urutan implementasi minimum yang disarankan

1. Setelah dependency G03 dibuktikan selesai, ambil snapshot source baru dan inventaris seluruh fitur Portofolio/caller. Bentuk satu mapping eksplisit N11/N17: keep/move/remove yang telah disetujui, kategori, tujuan kanonis, akun/syarat, Back dan bukti tiap route. Reuse pola/helper yang ada; jangan mulai dengan menyusun ulang semua flow.
2. Bangun/rapikan launcher kategori dan route Portofolio dari mapping tersebut; pindahkan hanya fitur di scope, pertahankan akun/filter/konfirmasi, serta tujuan transaksi sedia ada.
3. Telusuri DTO portfolio dan sumber nilai sebelum menyentuh UI posisi N10. Samakan initial render/live updater dengan `instrumentId` stabil; uji perhitungan berdasarkan mata uang, lot, basis biaya, reserve dan status posisi dari domain.
4. Untuk N15, tetapkan holder-of-record, sumber shares outstanding dan semantik short/borrow/reservasi/akun kelolaan dari model yang ada. Pertahankan state coverage `PARTIAL` bila registry tidak lengkap; hanya tambah representasi domain minimum bila syarat/source membuktikan perlu, terintegrasi settlement dan save.
5. Jalankan pemeriksaan settlement/receipt/fill, portfolio pribadi/klien dan snapshot holder secara terpadu; lalu route, restore, aksesibilitas, screenshot dan performa. Sinkronkan status hanya berdasarkan bukti. Tidak menambah APK deliverable pada Plan 4.

## Pemeriksaan yang dipersiapkan (bukan dijalankan pada sesi ini)

- N11/N17: inventory semua launcher/caller; tujuan valid; kategori koheren; tidak ada orphan/duplikat; setiap kategori via klik/keyboard/Back/direct route/reload; state locked/loading; akun/syarat/review tetap benar; periksa N19 removal tanpa mengambil N19 scope.
- N10: portfolio initial/live selaras per ID setelah harga/fill/cancel/reload; akun pribadi dan klien terpisah; IDR/USD/FX dan lot-size benar; untung/rugi/nol, long/short hanya jika didukung, posisi reserved/closed, stale/error/empty; nilai cocok DTO/fill/receipt/ledger.
- N15: emiten saat pemain nol, satu/multi-holder dan akun klien; settlement fill, no-fill/cancel, split/rights/buyback dan restore yang relevan; quantity conservation, unmapped shares dan denominator nol/invalid, rounding, duplicate holder, short/borrow, reservasi; tidak memaksa total 100% atau mengubah sharesOutstanding.
- Periksa state keyboard/fokus/Back, scroll dan live update; 48 dp, 100%/200%, viewport/kontras/reduced motion/screenshot aktual; simpan bukti visual dan timing yang memenuhi standar plan. Semua QA menggunakan save baru terisolasi, bukan save pemain. Gunakan runner/helper existing; jangan menjalankan test/server/build pada persiapan ini.

## Status/hasil PLAN-03 yang menjadi dasar snapshot

Tracker 24 Oktober 2026 mencatat G03 = 0/19 PASS, 10 `IN_PROGRESS`, 9 `PENDING`. `STATUS-REPORT.md` dan bagian G03 `CHECKPOINT.md` konsisten. Bukti parsial tercatat: receipt domain 7/7 dan subset bridge 5/5 PASS; full `TradingWebBridgeTest` 35 test dengan 4 gagal; browser receipt menunjukkan hasil fase tetapi runner dihentikan `exit -15`; clean full-project compile, expiry lifecycle produksi dan APK fresh belum terbukti dalam checkpoint. Folder deliverable G03 tidak memiliki `RESULTS.md` saat diperiksa. Status itu tidak meluluskan Plan 4 dan tidak membuktikan kerusakan transaksi; G03 tetap prasyarat sampai bukti final tracker/checkpoint diperbarui oleh pelaksanaan yang berizin.

## Konteks pekerjaan yang perlu diperhatikan

N11/N17 memakai satu peta kategori. N10 posisi pribadi terpisah dari N15 holder emiten. Snapshot holder parsial dan dua tes belum memenuhi supply/settlement/corporate action. Jangan membuat Publik/institusi/porsi kosmetik agar total 100%; jelaskan keputusan model nyata bila diperlukan.

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

- [ ] **N11-01 — PENDING**: Inventaris seluruh button/caller asset-tools dan buat mapping keep/move/remove-approved per fitur.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Inventaris seluruh button/caller asset-tools dan buat mapping keep/move/remove-approved per fitur. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N11-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N11-02 — PENDING**: Ganti klasifikasi berbasis awalan teks/tag dengan daftar tujuan eksplisit yang kecil dan terarah.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Ganti klasifikasi berbasis awalan teks/tag dengan daftar tujuan eksplisit yang kecil dan terarah. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N11-02 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N11-03 — PENDING**: Buat launcher kategori terbuka dalam bentuk daftar ringkas dengan normal/pressed/focus/locked/loading yang konsisten.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Buat launcher kategori terbuka dalam bentuk daftar ringkas dengan normal/pressed/focus/locked/loading yang konsisten. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N11-03 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N11-04 — PENDING**: Pertahankan akun, syarat dan review tindakan berisiko; membuka kategori tidak mengeksekusi ekonomi.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Pertahankan akun, syarat dan review tindakan berisiko; membuka kategori tidak mengeksekusi ekonomi. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N11-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N11-05 — PENDING**: Verifikasi seluruh fitur yang dipertahankan punya tujuan; removal lab/uji stres ditelusuri ke N19.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Verifikasi seluruh fitur yang dipertahankan punya tujuan; removal lab/uji stres ditelusuri ke N19. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N11-05 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N11-06 — PENDING**: Uji setiap kategori, klik/keyboard/Back, scroll, 200% dan live refresh tanpa launcher ganda.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Uji setiap kategori, klik/keyboard/Back, scroll, 200% dan live refresh tanpa launcher ganda. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N11-06 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N17-01 — PENDING**: Kelompokkan inventaris Portofolio berdasarkan objek/tindakan/akun; tandai fitur yang salah kelompok dan shortcut duplikat.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Kelompokkan inventaris Portofolio berdasarkan objek/tindakan/akun; tandai fitur yang salah kelompok dan shortcut duplikat. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N17-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N17-02 — PENDING**: Ajukan peta kategori/subkategori serta tujuan kanonis dan rute balik sebagai bagian persetujuan N20.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Ajukan peta kategori/subkategori serta tujuan kanonis dan rute balik sebagai bagian persetujuan N20. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N17-02 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N17-03 — PENDING**: Implementasikan landing ringkasan dan daftar kategori; pindahkan fitur ke halaman relevan tanpa accordion penampung.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Implementasikan landing ringkasan dan daftar kategori; pindahkan fitur ke halaman relevan tanpa accordion penampung. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N17-03 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N17-04 — PENDING**: Pertahankan semua handler, syarat, review, konteks akun, filter dan posisi scroll; cabut hanya removal N19.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Pertahankan semua handler, syarat, review, konteks akun, filter dan posisi scroll; cabut hanya removal N19. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N17-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N17-05 — PENDING**: Verifikasi setiap fitur keep/move/remove-approved dengan klik nyata, Back, direct route dan reload.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Verifikasi setiap fitur keep/move/remove-approved dengan klik nyata, Back, direct route dan reload. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N17-05 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N17-06 — PENDING**: Uji empty/error/locked, banyak posisi/aset, 200%, layar kecil dan live update tanpa lompatan layout.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Uji empty/error/locked, banyak posisi/aset, 200%, layar kecil dan live update tanpa lompatan layout. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N17-06 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N10-01 — PENDING**: Petakan kontrak posisi, katalog/nama emiten, lot, mata uang, biaya dan short/long yang didukung.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Petakan kontrak posisi, katalog/nama emiten, lot, mata uang, biaya dan short/long yang didukung. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N10-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N10-02 — PENDING**: Tentukan field default/rincian dan format nilai yang konsisten.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Tentukan field default/rincian dan format nilai yang konsisten. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N10-02 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N10-03 — PENDING**: Tambahkan field DTO minimum hanya jika sumber numeriknya belum tersedia.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Tambahkan field DTO minimum hanya jika sumber numeriknya belum tersedia. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N10-03 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N10-04 — PENDING**: Implementasikan kartu posisi yang aksesibel serta tindakan ke detail/ticket.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Implementasikan kartu posisi yang aksesibel serta tindakan ke detail/ticket. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N10-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N10-05 — PENDING**: Samakan renderer awal dan live updater berbasis ID stabil.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Samakan renderer awal dan live updater berbasis ID stabil. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N10-05 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N10-06 — PENDING**: Uji untung/rugi/nol, long/short yang tersedia, USD/IDR, posisi tertahan, posisi ditutup, data kosong/error/stale dan perpindahan akun.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Uji untung/rugi/nol, long/short yang tersedia, USD/IDR, posisi tertahan, posisi ditutup, data kosong/error/stale dan perpindahan akun. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N10-06 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N10-07 — PENDING**: Cocokkan semua nilai yang ditampilkan dengan portfolio Java, receipt dan ledger yang relevan.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Cocokkan semua nilai yang ditampilkan dengan portfolio Java, receipt dan ledger yang relevan. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N10-07 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N15-01 — IN_PROGRESS**: Inventaris sumber posisi, NPC/institusi, outstanding/free float, corporate actions dan identitas holder.
  - Implementasi/bukti parsial tercatat: Account/NPC positions and outstanding traced; full holder registry absent
  - Rujukan bukti: android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Lengkapi inventaris pemain/klien/NPC/institusi/treasury/dana, outstanding, corporate action dan holder ID; nyatakan celah registri secara konkret. Tetap belum selesai sampai seluruh kriteria N15-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N15-02 — IN_PROGRESS**: Definisikan holder of record, penyebut porsi, cakupan data, short/reservasi dan akun kelolaan tanpa hitung ganda.
  - Implementasi/bukti parsial tercatat: Partial account positions snapshot basis; short excluded; corporate/borrow model pending
  - Rujukan bukti: android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Tuntaskan definisi holder of record, supply/denominator, short/borrow, reservasi, akun kelolaan dan unit dana tanpa hitung ganda. Tetap belum selesai sampai seluruh kriteria N15-02 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N15-03 — IN_PROGRESS**: Pakai/perluas domain minimum untuk komposisi aktual; jangan mengangkat majorShareholders hardcoded sebagai fakta dinamis.
  - Implementasi/bukti parsial tercatat: Partial domain snapshot added; NOT complete supply registry
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N15-green-02/junit-latest.log
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Lengkapi domain minimum untuk komposisi aktual bila registri belum cukup; snapshot parsial dan dua tes belum memenuhi komposisi lengkap. Tetap belum selesai sampai seluruh kriteria N15-03 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N15-04 — PENDING**: Integrasikan peralihan kepemilikan pada fill/settlement, pembatalan, split/rights/buyback dan restore yang relevan.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Integrasikan peralihan kepemilikan pada fill/settlement, pembatalan, split/rights/buyback dan restore yang relevan. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N15-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N15-05 — IN_PROGRESS**: Sediakan DTO numerik eksplisit dengan instrument/holder ID, jumlah/porsi valid, as-of dan status cakupan.
  - Implementasi/bukti parsial tercatat: Explicit holder IDs/quantity/percent/as-of/partial coverage DTO
  - Rujukan bukti: android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Verifikasi DTO numerik holder/instrumen/jumlah/porsi/as-of/coverage, denominator invalid dan semua identitas akun tanpa angka kosmetik. Tetap belum selesai sampai seluruh kriteria N15-05 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N15-06 — IN_PROGRESS**: Buat panel pemegang saham terpisah dari Posisi saya, termasuk state pemain nol, partial/unknown/loading/error.
  - Implementasi/bukti parsial tercatat: Shareholder tab now reads actual account snapshot; browser/new-host pending
  - Rujukan bukti: android/app/src/main/assets/www/app.js
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Uji panel holder terpisah dari Posisi saya, pemain nol, partial/unknown/loading/error dan host source terbaru; cocokkan setiap angka ke domain. Tetap belum selesai sampai seluruh kriteria N15-06 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N15-07 — IN_PROGRESS**: Uji konservasi, denominator nol/invalid, rounding, banyak holder, akun klien, corporate action dan reload.
  - Implementasi/bukti parsial tercatat: 2 basic holder tests GREEN; test name overstates reservation/short assertions; expand
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N15-green-02/junit-latest.log
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Perluas assertion konservasi, short/reservasi, klien, corporate action dan reload; nama tes lama tidak menggantikan assertion yang belum ada. Tetap belum selesai sampai seluruh kriteria N15-07 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N15-08 — PENDING**: Rekonsiliasi setiap angka ke domain; uji live refresh, focus, 200%, serta tidak mengubah posisi pribadi N10.
  - Pekerjaan/keputusan berikutnya: Hanya saat G04 aktif: Rekonsiliasi setiap angka ke domain; uji live refresh, focus, 200%, serta tidak mengubah posisi pribadi N10. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N15-08 terbukti; catat command, hasil, bukti dan kekurangan tersisa.

## Rincian kontrak dan acceptance paket

Rincian diambil dari master aktif; checklist dipindahkan ke daftar ID di atas. Keputusan revisi 2 mengesampingkan palette/background lama yang bertentangan.

## 15. N11: Launcher Portofolio berkelompok dan mudah ditemukan

Ganti details#asset-tools dengan launcher kategori yang selalu terlihat. Gunakan daftar ringkas berikon, judul, dan keterangan satu baris; kategori membuka halaman fitur relevan. Tidak sekadar membuka accordion secara default, tidak menggantinya dengan daftar panjang semua alat, dan tidak memberi semua fitur kartu hero yang sama.

Kelompok awal: Saham & Order; Dompet & Valuta; Investasi Lain; Utang & Margin; Proteksi & Kepatuhan; Usaha & Dana Kelolaan bila relevan. Rincian pemetaan ada pada N17. Koleksi/properti yang berhubungan dengan kekayaan mempunyai kategori tersendiri jika tidak cocok dengan investasi keuangan. Lab dan uji stres dikecualikan secara eksplisit oleh N19; replay historis dipisahkan dari lab sandbox dan tetap dipertahankan jika mode perdagangan utama.

Dompet dan informasi pasif tidak wajib menjadi menu. Setiap launcher membuka tujuan yang nyata, mempertahankan akun, syarat, Back dan konfirmasi. Fitur tersedia maupun terkunci dapat ditemukan secara wajar, tanpa memalsukan akses.

Penerimaan: tidak ada accordion alat yang menyembunyikan fitur, kategori koheren, setiap tujuan tetap terjangkau atau tercatat dihapus atas keputusan pemilik, dan state finansial terlindungi.

## 27. N17: Subkategori Portofolio yang relevan

### 27.1 Peta awal untuk proposal navigasi

Halaman awal menampilkan kekayaan, kas tersedia dan posisi yang penting. Di bawahnya daftar kategori, bukan seluruh alat. Setiap kategori mempunyai halaman sendiri dan keterangan pendek. Final placement mengikuti persetujuan N20 tanpa kehilangan fitur.

| Subkategori | Isi yang relevan | Batas pemisahan |
|---|---|---|
| Saham & order | Posisi, order aktif, riwayat transaksi saham | Tidak mencampur pinjaman, koleksi atau profil. |
| Dompet & valuta | Saldo IDR/USD, dana tertahan, FX, riwayat kas | Kas pribadi dan klien tetap dibedakan. |
| Reksa dana | Unit/NAB, subscribe/redeem, antrean dan riwayat | Fund builder kelolaan bukan otomatis investasi pribadi. |
| Obligasi & deposito | Posisi, jatuh tempo, kupon/bunga, pencairan | Jika komoditas punya flow berbeda, buat subkategori sendiri. |
| Komoditas | Kepemilikan, quote, beli/jual | Jangan memaksakan satu formulir untuk produk berbeda. |
| Pinjaman & cicilan | Pinjaman tenor, agunan, jadwal, prepay/restrukturisasi | Tidak disamakan dengan margin saham. |
| Margin & proteksi | Margin health, pinjaman saham/short, asuransi yang benar-benar berlaku | Uji stres tidak tampil. |
| Pajak & laporan | Audit, ledger/laporan akun dan kepatuhan relevan | Bukan direktori seluruh fitur lain. |
| Properti & koleksi | Kepemilikan, valuasi, harga dan aksi ekonomi | Tetap ada karena memengaruhi uang/progres. |
| Usaha & dana kelolaan | Shortcut usaha/klien/dana ke tujuan kanonis | Jangan menduplikasi seluruh halaman Bisnis. |

Daftar di atas adalah pemetaan awal, bukan kewajiban membuat sepuluh tab sejajar. Gunakan kategori bertingkat seperlunya dengan judul yang tetap terbaca. Jika satu kategori terlalu luas, pecah berdasarkan tujuan/objek. Jika hanya satu fitur kecil, launcher langsung boleh lebih sederhana daripada halaman kosong satu tombol. Hindari horizontal chip row panjang yang menyembunyikan kategori.

Penerimaan: tidak ada daftar puluhan alat dalam satu bagian, tiap kategori mempunyai alasan pengelompokan yang dapat dijelaskan, fitur yang tidak relevan dipisahkan, dan seluruh jalur uang/progres tetap ada. Jangan menambah pencarian/shortcut favorit baru jika struktur kategori sudah cukup.

## 14. N10: Kepemilikan saham yang ringkas dan informatif

### 14.1 Susunan kartu/baris posisi

Satu baris pendek seperti “IDX:BBCA: 500 saham · Rp …” belum cukup membantu keputusan. Ubah menjadi kartu posisi kompak yang menampilkan:

- **Identitas:** ticker kuat, nama emiten lebih kecil, badge bursa bila diperlukan.
- **Nilai posisi:** angka dominan dalam mata uang posisi, tanpa mencampur IDR dan USD.
- **Kepemilikan:** jumlah saham dan ekuivalen lot berdasarkan lot size instrumen. Saham US tidak diasumsikan 100 saham per lot.
- **Laba/rugi berjalan:** nominal bertanda dan persentase, ditulis “Untung/Rugi” atau tanda yang jelas; warna bukan satu-satunya makna.
- **Harga rata-rata dan harga sekarang:** dua nilai kecil yang sejajar dan satuan per saham jelas.
- **Tindakan:** seluruh kartu membuka rincian posisi. Bila ada Beli/Jual langsung, gunakan tombol terpisah yang tidak nested di dalam button; transaksi tetap melalui ticket dan review.

Rekomendasi hierarki: ticker/nama di kiri, nilai dan laba/rugi di kanan; baris kedua jumlah, harga rata-rata, harga kini. Pada 320 px atau teks 200%, reflow ke beberapa baris. Jangan memperkecil font sampai semua angka muat satu baris.

### 14.2 Perhitungan dan state

1. Gunakan `averageCost`, `currentPrice`, `quantity`, `marketValue`, dan `unrealizedPnL` dari domain. Tambahkan field numerik/basis biaya ke DTO bila persentase tidak dapat dihitung dengan aman dari data existing.
2. Persentase untung/rugi memakai basis biaya yang benar untuk posisi tersebut. Basis nol tidak menghasilkan Infinity/NaN. Posisi short perlu formula arah/basis yang sesuai domain, bukan formula long dipaksakan.
3. Nominal unrealized P/L tidak sama dengan hasil bersih yang sudah terealisasi. Jangan memakai kata “bersih” untuk realized gross. Biaya boleh dijelaskan di rincian yang relevan.
4. Jika menampilkan bobot portofolio, definisikan penyebut: misalnya nilai saham dalam keseluruhan portofolio akun pada valuasi IDR yang sama. Jangan mencampur mata uang mentah. Bobot bukan kewajiban default jika menambah kepadatan tanpa manfaat.
5. Posisi jumlah nol/dahulu ditutup tidak memenuhi daftar kepemilikan aktif. Tampilkan riwayat di tujuan yang sesuai; jangan menghapus catatan ledger atau posisi yang dibutuhkan audit.
6. Saham tertahan/tersedia untuk dijual hanya ditampilkan jika domain menyediakan angka yang benar. Jangan menyamakan held shares dengan saham yang dapat langsung dijual.
7. Peringatan harga stale atau data gagal muncul singkat bila memang diperlukan. Kosong memiliki aksi “Cari saham”; error menyediakan coba lagi dan tidak menyamakan error dengan “Belum punya saham”.
8. `loadAsetView` dan `syncPortfolioLive` harus memakai struktur yang sama. Updater tidak boleh mengembalikan kartu baru menjadi teks satu baris. Cocokkan DOM berdasarkan `instrumentId`, bukan indeks array, agar data tidak tertukar saat urutan berubah.
9. Update harga/PL tidak membuang fokus, selection, atau posisi scroll. Jangan membuat kartu baru setiap detik bila nilainya saja yang berubah.

Penerimaan: pemain dapat melihat apa yang dimiliki, nilainya, harga perolehannya, dan hasil berjalan tanpa membuka setiap posisi. Data tetap benar setelah harga berubah, fill baru, pembatalan reservasi, load, dan akun diganti.

## 25. N15: Komposisi pemegang saham dunia game

### 25.1 Pemisahan dua informasi

Detail emiten memiliki bagian **Pemegang saham** yang tetap berguna ketika pemain belum memiliki saham, serta bagian **Posisi saya** hanya untuk akun terpilih. Jangan mengganti daftar posisi pribadi Portofolio dengan daftar institusi. Istilah stakeholder di komentar ditafsirkan melalui klarifikasi pemilik sebagai pemegang saham, bukan semua pihak berkepentingan seperti pemasok/karyawan.

Komposisi memuat nama pemegang atau kelompok yang benar-benar tercatat, jumlah saham, porsi terhadap saham beredar jika denominator sah, dan waktu state. Tandai kepemilikan pemain secara jelas; akun klien tidak digabung ke milik pribadi. Nama NPC/institusi harus berasal dari identity domain; jangan memakai nama pemegang saham perusahaan asli. Tidak perlu jaringan/data pasar nyata untuk fitur ini.

Telusuri semua akun/registri: pemain, NPC, institusi, issuer/treasury, kelolaan klien dan dana. Definisikan siapa holder of record agar saham dalam dana tidak dihitung ulang sebagai saham milik pemegang unit. Agunan/reservasi order tidak menciptakan pemilik kedua. Short/borrowed shares memerlukan semantik domain yang konsisten; posisi short bukan porsi kepemilikan positif baru.

Porsi memakai sharesOutstanding emiten yang sama dan snapshot yang sama, bukan nilai portofolio atau nilai market cap sektoral. Jangan memakai free float sebagai penyebut kepemilikan total. Jangan paksa total 100% dengan membulatkan baris terakhir atau mengarang Publik. Bila hanya sebagian owner terpetakan, tampilkan cakupan yang benar dan nilai yang tersedia; bagian belum terpetakan bukan nama investor tertentu. State parsial tidak boleh ditandai memenuhi registri lengkap.

Jika source belum memiliki registri lengkap, Hermes menambah representasi domain terkecil yang cukup terintegrasi dengan settlement dan save; dokumentasikan bagaimana baseline legacy dipetakan. Tidak mengubah quantity, kas, outstanding atau ekonomi hanya agar pie chart berjumlah 100%. Bila konservasi supply belum dapat direkonsiliasi, catat temuan konkret dan minta keputusan model yang diperlukan; selesaikan UI dan jalur independen tanpa data palsu.

Penerimaan: emiten tanpa saham pemain masih menampilkan pemegang yang sah bila tersedia; setelah fill jumlah dan porsi berubah sesuai settlement; canceled/unfilled order tidak memindahkan kepemilikan; tidak ada duplikasi pemilik, porsi negatif/lebih dari 100% yang disembunyikan, atau data asli perusahaan yang diada-adakan. Grafik komposisi opsional; daftar angka yang benar cukup.

## Deliverable dan syarat menutup file

Bukti baru disimpan pada Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/04-portofolio-dan-kepemilikan/. Gunakan save QA terisolasi; pertahankan log gagal.

1. Seluruh 27 ID file ini memenuhi requirement dan seluruh acceptance pada snapshot source yang sesuai. Bukti parsial tetap belum selesai.
2. RESULTS.md memuat ID/status, perubahan/asumsi, command/exit code, tes/review/performa, lokasi bukti, source hash, hash save dan temuan terbuka.
3. Tracker existing, STATUS-REPORT.md, Work/STATUS.json dan checkpoint sinkron. Tidak membuat master/tracker tandingan. Saat selesai catat WAITING_FOR_OWNER_PLAN dan jangan mengaktifkan file berikut.
4. Tutup atau catat kepemilikan/status proses QA file ini secara jelas. Jangan menutup preview/proses lain atau meninggalkan proses yang melanjutkan agent ke scope berikut.

File ini menyerahkan checkpoint dan bukti kelompoknya. APK mengikuti titik penyerahan yang sudah ditetapkan; jangan mengambil pekerjaan file lain untuk mengejar kandidat di luar kiriman ini. Plan 4 tidak menambah kewajiban APK kandidat baru; ikuti hanya kewajiban/gerbang APK yang sudah ditetapkan dalam master plan.

## Instruksi berhenti

Setelah seluruh kriteria dan deliverable file ini selesai, jawab dengan PLAN-04, status ID, bukti utama, APK bila diwajibkan, dan temuan tersisa. Kemudian **BERHENTI DAN TUNGGU FILE PLAN BERIKUTNYA DARI PEMILIK**.

Jangan memulai plan berikut dari urutan, membuat reminder/automation untuk lanjut, atau menganggap pemilik diam sebagai persetujuan. Bila ada blocker nyata sebelum selesai, laporkan kekurangan spesifik dan tetap jangan berpindah file.
