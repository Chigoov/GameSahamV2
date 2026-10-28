# PLAN-01 — Waktu otomatis dan berita

**Kerjakan hanya file ini sampai tuntas, serahkan hasil, lalu BERHENTI menunggu pemilik mengirim file berikutnya.**

Kelompok: G01. Paket: N01, N02. Jumlah: 13 subtask. Target akhir proyek 20 paket / 126 subtask; scope satu kiriman hanya ID dalam file ini.

File asli: Work/Master-Plan-Gabungan-2026-10-24/Plans/01-waktu-dan-berita/plan.md. Disusun 24 Oktober 2026, Asia/Jakarta. Keberadaan file di disk bukan izin menjalankan tahap lain. Mode kiriman manual menggantikan mandat lanjut otomatis.

## Prasyarat dan batas kiriman

Pemilik mengirim/menunjuk file ini. Persetujuan revisi 2 sudah tercatat; source dan save aktual harus dipertahankan.

Status saat disusun: 0 PASS_WITHIN_SCOPE, 13 parsial, 0 PENDING. Ini snapshot awal. Saat file diterima, baca tracker/source terbaru, pertahankan bukti valid dan ulangi pemeriksaan yang diperlukan task belum tuntas. Jangan mulai dari nol atau menurunkan status hanya karena snapshot file lebih lama.

Master spesifikasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/master plan perbaikan.md. Tracker: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/Master-Plan-Gabungan-2026-10-24/TRACKER.csv. Checkpoint: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/Master-Plan-Gabungan-2026-10-24/CHECKPOINT.md.

Master adalah sumber kontrak produk/referensi relevan. Prompt global seluruh 126 task di sana bukan izin menjalankan semua tahap; scope sekarang hanya file yang dikirim pemilik ini.

## Hasil dan urutan dalam file ini

### G01 — Waktu otomatis dan berita

Kesesuaian: Satu pemilik waktu, lifecycle, deadline berita, catchup dan save.

Urutan dalam kelompok: N01: tempo, migrasi dan lifecycle → N02: deadline, engine, persistence dan tampilan berita → uji gabungan waktu/berita/save.

Syarat keluar: Seluruh 13 ID lulus kriterianya; tempo 24x dan efek 60/120 menit nyata benar pada aktif, offline, restart dan retry; kalender serta uang tetap konsisten.

## Konteks pekerjaan yang perlu diperhatikan

Empat FAIL TradingWebBridgeTest perlu diagnosis/rerun: speed 2, closeDay manual pada dua tes, dan elapsed 93.750 ms yang masih memakai laju 288x untuk settlement reksa dana. Ini temuan statis, bukan hasil rerun atau izin menghapus assertion. Sesuaikan fixture ke 24x sambil mempertahankan idempotency/settlement/ledger. Pertahankan N08 parsial di source; jangan kembali ke RED historis atau mengambil pekerjaan N08 dalam file ini.

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

- [ ] **N01-01 — IN_PROGRESS**: Telusuri seluruh sumber tempo, clock writer, speed/skip, lifecycle dan presenter; catat semua caller.
  - Implementasi/bukti parsial tercatat: Clock/facade/bridge/save/lifecycle/presenter traced
  - Rujukan bukti: Work/Master-Plan-Gabungan-2026-10-24/N01-N02-TRACE.md
  - Pekerjaan/keputusan berikutnya: Hanya saat G01 aktif: Telusuri ulang semua clock writer/caller facade, bridge, native lifecycle, DOM dan presenter; lengkapi jejak source serta pastikan tidak ada pemilik waktu kedua. Tetap belum selesai sampai seluruh kriteria N01-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N01-02 — IN_PROGRESS**: Rekam baseline laju, timestamp, save schema, anchor dan state saat aktif/hidden/restart sebelum migrasi.
  - Implementasi/bukti parsial tercatat: Baseline and controlled-clock RED recorded
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N01-red/junit-latest.log
  - Pekerjaan/keputusan berikutnya: Hanya saat G01 aktif: Periksa kelengkapan baseline awal laju/timestamp/schema/anchor/lifecycle dan rekam baseline source kini; pertahankan bukti historis tanpa merekayasa kondisi sebelum migrasi. Tetap belum selesai sampai seluruh kriteria N01-02 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N01-03 — IN_PROGRESS**: Terapkan laju tunggal 24x di pemilik waktu Java dan presenter yang sesuai; hapus akses kontrol percepat/skip/pause pemain.
  - Implementasi/bukti parsial tercatat: 24x conversion; product bridge controls blocked; HH:mm presenter
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N01-browser-03/results.json
  - Pekerjaan/keputusan berikutnya: Hanya saat G01 aktif: Buktikan 24x pada aktif/hidden/restart, HH:mm dari domain dan penolakan kontrol speed/skip/pause; periksa reduced motion serta continuity tampilan. Tetap belum selesai sampai seluruh kriteria N01-03 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N01-04 — IN_PROGRESS**: Rekonsiliasi elapsed, fraksi dan migrasi anchor secara idempotent pada startup/load/mode switch/lifecycle.
  - Implementasi/bukti parsial tercatat: High-water anchor; speed migration; remainder preserved; wider gate pending
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N02-facade-regression/junit-latest.log
  - Pekerjaan/keputusan berikutnya: Hanya saat G01 aktif: Ulangi migrasi dan interval terpartisi, remainder, callback ganda, jam mundur, crash serta retry; pastikan elapsed tidak hilang atau dihitung dua kali. Tetap belum selesai sampai seluruh kriteria N01-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N01-05 — IN_PROGRESS**: Pertahankan auto-start, rollover, save/recovery dan batas dataset; sesuaikan dokumen serta fixture lama yang kontraknya digantikan.
  - Implementasi/bukti parsial tercatat: Docs and selected fixtures reconciled; full regression incomplete
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/full-jvm-02/junit-latest.log
  - Pekerjaan/keputusan berikutnya: Hanya saat G01 aktif: Rekonsiliasi seluruh fixture waktu lama dan uji auto-start/rollover/save/recovery/replay. Diagnosis empat kegagalan bridge yang masih memakai speed/manual close atau durasi 288x; pertahankan invariant dan jalankan ulang. Tetap belum selesai sampai seluruh kriteria N01-05 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N01-06 — IN_PROGRESS**: Jalankan controlled-clock, partition/lifecycle/restart/migrasi dan rekaman visual; buktikan ekonomi kalender tetap benar.
  - Implementasi/bukti parsial tercatat: Targeted clock/save sequence and browser recorded; full gate pending
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N01-browser-03
  - Pekerjaan/keputusan berikutnya: Hanya saat G01 aktif: Tuntaskan controlled-clock dan sequence kritis lifecycle/order/berita/save sesuai acceptance, rekam gerak jam, serta buktikan kalender dan ekonomi tepat sekali. Tetap belum selesai sampai seluruh kriteria N01-06 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N02-01 — IN_PROGRESS**: Petakan seluruh caller `isHorizonActive`, sentimen, daftar berita, countdown dan export/restore state.
  - Implementasi/bukti parsial tercatat: Research/news/engine/feed/persistence callers traced
  - Rujukan bukti: Work/Master-Plan-Gabungan-2026-10-24/N01-N02-TRACE.md
  - Pekerjaan/keputusan berikutnya: Hanya saat G01 aktif: Lengkapi peta semua pemakai horizon/deadline, engine, feed, countdown dan export/restore; bedakan efek berita dari horizon analisis/langganan. Tetap belum selesai sampai seluruh kriteria N02-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N02-02 — IN_PROGRESS**: Tentukan kontrak deadline dan durasi per event; dokumentasikan default 60 menit bila diperlukan.
  - Implementasi/bukti parsial tercatat: 60-minute default and explicit 120-minute event support
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N02-facade-regression/junit-latest.log
  - Pekerjaan/keputusan berikutnya: Hanya saat G01 aktif: Verifikasi kontrak 60/120 menit dan pemetaan durasi event/default 60 menit pada katalog/source kini; dokumentasikan tanpa mengubah tarif atau hadiah. Tetap belum selesai sampai seluruh kriteria N02-02 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N02-03 — IN_PROGRESS**: Simpan deadline persisten, termasuk migrasi save lama, reset game baru, dan recovery backup.
  - Implementasi/bukti parsial tercatat: Deadline export/restore and legacy transition added; fault/legacy coverage pending
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N02-facade-regression/junit-latest.log
  - Pekerjaan/keputusan berikutnya: Hanya saat G01 aktif: Uji migrasi legacy, load berulang, backup/recovery dan save gagal; buktikan deadline dipersistenkan sekali dan tidak diperpanjang setelah restart. Tetap belum selesai sampai seluruh kriteria N02-03 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N02-04 — IN_PROGRESS**: Hubungkan deadline ke `ResearchService` dan konsumennya di `MarketEngine`, bukan hanya DTO/UI.
  - Implementasi/bukti parsial tercatat: Shared research impact consumes observed real deadline; deep engine gate pending
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N02-facade-regression/junit-latest.log
  - Pekerjaan/keputusan berikutnya: Hanya saat G01 aktif: Buktikan MarketEngine memakai deadline yang sama untuk dampak sebelum/tepat/sesudah batas, termasuk replay; DTO/countdown saja belum cukup. Tetap belum selesai sampai seluruh kriteria N02-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N02-05 — IN_PROGRESS**: Perbaiki stepper/catchup agar efek pada interval offline dihitung pada segmen yang tepat.
  - Implementasi/bukti parsial tercatat: Event real-time mapping and partition tests; full economic equivalence pending
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N02-facade-regression/junit-latest.log
  - Pekerjaan/keputusan berikutnya: Hanya saat G01 aktif: Bandingkan ekonomi interval offline utuh dan terpartisi melewati publikasi/deadline; reading feed tidak boleh menghapus pengaruh historis atau menggandakan hasil. Tetap belum selesai sampai seluruh kriteria N02-05 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N02-06 — IN_PROGRESS**: Samakan badge, detail, countdown, dan state berakhir di Beranda, Riset, dan detail emiten.
  - Implementasi/bukti parsial tercatat: Home/research presentation started; countdown updates/detail coverage pending
  - Rujukan bukti: android/app/src/main/assets/www/app.js
  - Pekerjaan/keputusan berikutnya: Hanya saat G01 aktif: Samakan Beranda/Riset/detail emiten, timestamp nyata WIB, countdown dan transisi berakhir; uji refresh serta kembali dari background. Tetap belum selesai sampai seluruh kriteria N02-06 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N02-07 — IN_PROGRESS**: Tambahkan tes publikasi, batas akhir, restart, offline silang deadline, beberapa berita, clock mundur, dan mode replay.
  - Implementasi/bukti parsial tercatat: Six deadline tests in targeted 29-test run; wider critical sequence pending
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N02-facade-regression/junit-latest.log
  - Pekerjaan/keputusan berikutnya: Hanya saat G01 aktif: Ulangi publikasi, batas 60/120 menit, beberapa berita, restart, clock mundur, replay dan sequence kritis sesuai seluruh acceptance dengan controlled clock. Tetap belum selesai sampai seluruh kriteria N02-07 terbukti; catat command, hasil, bukti dan kekurangan tersisa.

## Rincian kontrak dan acceptance paket

Rincian diambil dari master aktif; checklist dipindahkan ke daftar ID di atas. Keputusan revisi 2 mengesampingkan palette/background lama yang bertentangan.

## 5. N01: Tempo tunggal satu hari game per satu jam nyata

### 5.1 Kontrak dan solusi

Keputusan terbaru menggantikan tempo aktif lama. Satu hari kalender game adalah 86.400 detik, bukan hanya jam bursa. Setiap satu detik nyata memajukan 24 detik game, pada foreground/background/closed. Dunia auto-start, hari berganti tanpa jeda pemain, tidak ada tombol speed/skip/play/pause. Bursa tetap memiliki sesi yang sah; pasar tutup tidak menghentikan kalender dunia.

Java tetap satu pemilik clock. Telusuri GameClock, syncClock, advanceRealMs, resume/load, lifecycle, MainActivity, bridge, presenter JS, speed/skip lama dan seluruh callernya. Pakai aturan elapsed yang sama di startup, resume, mode switch, dan catchup; jangan memperlambat lewat CSS atau menambah penghitung ekonomi JS.

Saat migrasi, pertahankan timestamp dunia dan seluruh saldo/posisi. Simpan versi aturan/anchor minimum bila dibutuhkan. Selesaikan elapsed yang belum direkonsiliasi menurut kebijakan transisi yang dicatat, lalu anchor baru memakai 24x; jangan mengubah elapsed yang sama dua kali, mengubah tanggal secara retroaktif, atau menghitung seluruh masa lalu dengan multiplier baru. Kebijakan fallback untuk save lama yang tidak punya informasi cukup harus eksplisit dan diuji; jangan mengarang timestamp historis.

Presenter memakai timestamp domain dan anchor monoton untuk interpolation, tidak per-frame meminta Java. Utamakan HH:mm; detail detik hanya jika berguna. Pada 24x, satu detik game tetap bukan satu detik nyata. Refresh biasa tidak membuat jam mundur. Hidden menghentikan pekerjaan visual, saat kembali sinkronisasi dahulu. Reduced motion menonaktifkan animasi digit tanpa mengubah waktu.

Bunga, gaji, dividen, misi, liga, jatuh tempo dan sesi tetap mengikuti kalender/rumus yang sah. Durasi nyata sampai event tersebut secara alami berubah karena tempo baru; jangan diam-diam mengubah tarif atau hadiah. Berita 60/120 menit nyata mempunyai kontrak tersendiri di N02.

### 5.3 Penerimaan dan bukti

- 300.000 ms nyata menghasilkan 7.200 detik game; 3.600.000 ms menghasilkan 86.400 detik game; 7.200.000 ms menghasilkan dua hari game. Uji aktif, hidden, tertutup/restart dan campuran dengan total elapsed sama.
- Interval utuh sama dengan interval terpartisi, termasuk remainder; callback ganda dan retry tidak menambah waktu dua kali. Uji 1.900 ms lalu hidden pada 2.000 ms, clock mundur, crash tanpa callback, save failure, dan offline panjang.
- Migrasi tidak mereset waktu, progres atau posisi. Tidak ada kompensasi uang/hadiah tanpa aturan domain. Clock bergerak otomatis tanpa kontrol speed/skip; API lama yang mungkin terpanggil UI tidak boleh diam-diam mempercepat dunia.
- Session close, rollover misi/liga, jatuh tempo, pembayaran dan corporate actions tepat sekali pada kalender baru. Replay tetap anti-future-leak dan berhenti pada batas dataset yang sah.
- Lampirkan rekaman jam normal/reduced motion, source hash, angka domain, snapshot save sebelum/sesudah, serta sequence kritis. Update tes lama dengan perilaku baru; jangan hanya menghapus assertion.

## 6. N02: Efek berita berdurasi satu sampai dua jam nyata

### 6.1 Masalah dan kontrak baru

Horizon yang diperiksa masih menggunakan hari game. Mengubah teks “hari permainan” menjadi “jam nyata” tanpa mengubah perhitungan dampak akan menyesatkan dan tidak memenuhi anotasi.

**WAJIB:** countdown yang terlihat dan batas aktif yang dipakai engine berasal dari deadline waktu nyata yang sama. Menutup aplikasi, mengubah kecepatan aktif, memajukan hari, atau masuk replay tidak boleh memperpanjang deadline tersebut.

### 6.2 Solusi domain yang direkomendasikan

1. Tambahkan kontrak waktu nyata pada event berita yang relevan, misalnya `publishedRealAt`, `effectStartsRealAt`, `effectDurationRealMs`, dan `effectEndsRealAt`. Nama final boleh mengikuti pola repo; maknanya harus tetap eksplisit.
2. Tetapkan deadline sekali saat event benar-benar terbit/aktif, bukan setiap kali kartu dirender atau dibaca. Membaca berita tidak memulai ulang efek.
3. Gunakan clock waktu nyata yang dapat diinjeksi dan dipakai bersama secara konsisten. Countdown JS boleh menginterpolasi sisa waktu, tetapi Java menentukan apakah dampak masih berlaku.
4. **ASUMSI TERBATAS untuk durasi:** dukung dua durasi nyata, 60 dan 120 menit. Jika katalog belum menentukan durasi, gunakan 60 menit sebagai default tercatat; event yang memang diberi 120 menit mengikuti durasi tersebut. Ini bukan izin membuat reward/rentang harga baru atau menetapkan semua berita selalu dua jam.
5. Aktif pada interval `startsAt <= now < endsAt`. Tepat pada deadline, efek berhenti; riwayat berita tetap tersedia dan diberi status berakhir.
6. Pada catchup offline, evaluasi kejadian berdasarkan posisi waktu nyata di dalam interval yang dilewati. Jangan memakai satu `now` saat login untuk menentukan seluruh pengaruh selama dua jam sebelumnya: itu bisa menghapus efek yang sebelumnya aktif atau menerapkannya setelah berakhir.
7. Jika dunia berjalan dengan beberapa laju, petakan elapsed nyata ke batas event dunia dalam segmen yang sesuai. Potong segmen pada publikasi/deadline berita, rollover/cutoff, dan transisi mode yang memang relevan. Pakai ulang scheduler/stepper existing; jangan membuat engine paralel.
8. Aktivitas yang terjadi sebelum deadline tetap menjadi transaksi/riwayat yang sah. Saat efek berakhir, hentikan dorongan sentimen berikutnya; jangan mengembalikan harga secara paksa atau membatalkan transaksi yang sudah terisi.
9. Telusuri pemakai horizon lain seperti konsensus, riset terminal, booster, atau berita replay. Jangan mengganti semua `horizonDays` di repo secara global. Bedakan horizon analisis, masa langganan, dan durasi pengaruh harga.
10. Mode replay tetap mengikuti batas dataset dan anti-future-leak. Untuk efek yang tidak berlaku pada replay, definisikan state yang jelas; jangan mengubah bar historis untuk memaksakan pengaruh berita internal.

### 6.3 Tampilan yang diinginkan

- Di kartu: kategori/sektor, arah dampak, lalu **“Aktif · sisa 1j 24m”** atau **“Berakhir”**.
- Di rincian: waktu mulai/berakhir nyata dalam WIB dan durasi total yang ringkas. Jangan mencampur timestamp game dengan waktu nyata tanpa penamaan.
- Persentase rentang tetap berlabel **perkiraan dampak**, bukan hasil pasti. Masa aktif dua jam tidak berarti harga pasti naik sekian persen.
- Saat belum terbit, gunakan countdown rilis yang sesuai kontraknya dan jangan bocorkan isi terlarang.
- Tidak perlu animasi atau pembacaan screen reader tiap detik. Pembaruan per menit memadai untuk kartu satu–dua jam; status batas harus tetap diperbarui tepat.

### 6.5 Kasus penerimaan wajib

Event berdurasi 60 menit dimulai pukul 14.00 WIB nyata: pukul 14.30 sisa 30 menit, pukul 15.00 berakhir. Event 120 menit: pukul 15.00 masih sisa 60 menit, pukul 16.00 berakhir. Nilai tersebut mengikuti deadline nyata dan tidak berubah karena rendering atau perpindahan foreground/background pada laju 24x.

Jika aplikasi ditutup pukul 14.15 dan dibuka 15.30, engine harus merekonsiliasi aktivitas 14.15–15.00 saat efek aktif dan 15.00–15.30 saat tidak aktif. Membuka ulang lagi pada waktu yang sama tidak mengulang efek/hasil.

Uji interval panjang sekaligus dan interval kecil terpartisi pada input yang sama. Tanggal perangkat mundur tidak memperpanjang efek atau menggandakan kejadian. Jelaskan batas proteksi clock lokal; jangan mengklaim otoritas waktu server yang tidak ada.

Migrasi: jangan memperlakukan save lama seolah memiliki timestamp nyata historis yang tidak pernah disimpan. Rekomendasi untuk event lama yang sudah aktif adalah menetapkan satu masa transisi sekali pada migrasi dan menyimpannya; jangan mengulang masa transisi pada setiap load. Catat kebijakan itu secara eksplisit dan verifikasi tidak memberi kredit/keuntungan retroaktif yang tidak didukung data.

## Deliverable dan syarat menutup file

Bukti baru disimpan pada Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/01-waktu-dan-berita/. Gunakan save QA terisolasi; pertahankan log gagal.

1. Seluruh 13 ID file ini memenuhi requirement dan seluruh acceptance pada snapshot source yang sesuai. Bukti parsial tetap belum selesai.
2. RESULTS.md memuat ID/status, perubahan/asumsi, command/exit code, tes/review/performa, lokasi bukti, source hash, hash save dan temuan terbuka.
3. Tracker existing, STATUS-REPORT.md, Work/STATUS.json dan checkpoint sinkron. Tidak membuat master/tracker tandingan. Saat selesai catat WAITING_FOR_OWNER_PLAN dan jangan mengaktifkan file berikut.
4. Tutup atau catat kepemilikan/status proses QA file ini secara jelas. Jangan menutup preview/proses lain atau meninggalkan proses yang melanjutkan agent ke scope berikut.

### APK-01 wajib diserahkan dalam file ini

Bangun kandidat APK fresh dari snapshot stabil setelah pemeriksaan yang diwajibkan. Verifikasi application ID com.tradingglobal3d.mobile, version metadata, signature yang berlaku, SHA-256/ukuran dan kecocokan index.html/app.js/CSS/font/aset/data dengan source. Pertahankan identitas update/signing yang sah. Sertakan ID selesai/terbuka, hasil gagal serta batas host/browser/APK/native. Tidak instalasi/ADB/perangkat/push.

File belum ditutup sebelum APK-01 dan bukti paket diserahkan. APK awal tidak meluluskan alur lain yang masih parsial. APK final file 08 menunggu seluruh 126 ID dan gate akhir.

## Instruksi berhenti

Setelah seluruh kriteria dan deliverable file ini selesai, jawab dengan PLAN-01, status ID, bukti utama, APK bila diwajibkan, dan temuan tersisa. Kemudian **BERHENTI DAN TUNGGU FILE PLAN BERIKUTNYA DARI PEMILIK**.

Jangan memulai plan berikut dari urutan, membuat reminder/automation untuk lanjut, atau menganggap pemilik diam sebagai persetujuan. Bila ada blocker nyata sebelum selesai, laporkan kekurangan spesifik dan tetap jangan berpindah file.
