# PLAN-02 — Pasar, detail saham dan grafik

**Kerjakan hanya file ini sampai tuntas, serahkan hasil, lalu BERHENTI menunggu pemilik mengirim file berikutnya.**

Kelompok: G02. Paket: N03, N05, N06, N14. Jumlah: 22 subtask. Target akhir proyek 20 paket / 126 subtask; scope satu kiriman hanya ID dalam file ini.

File asli: Work/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/plan.md. Disusun 24 Oktober 2026, Asia/Jakarta. Keberadaan file di disk bukan izin menjalankan tahap lain. Mode kiriman manual menggantikan mandat lanjut otomatis.

## Prasyarat dan batas kiriman

Pemilik mengirim/menunjuk file ini setelah file 01 selesai. Verifikasi seluruh ID serta checkpoint/bukti file sebelumnya memenuhi kriterianya. Jika belum, laporkan ID penghambat dan tunggu keputusan tanpa mengambil pekerjaan file lain.

Status saat disusun: 0 PASS_WITHIN_SCOPE, 18 parsial, 4 PENDING. Ini snapshot awal. Saat file diterima, baca tracker/source terbaru, pertahankan bukti valid dan ulangi pemeriksaan yang diperlukan task belum tuntas. Jangan mulai dari nol atau menurunkan status hanya karena snapshot file lebih lama.

Master spesifikasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/master plan perbaikan.md. Tracker: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/Master-Plan-Gabungan-2026-10-24/TRACKER.csv. Checkpoint: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/Master-Plan-Gabungan-2026-10-24/CHECKPOINT.md.

Master adalah sumber kontrak produk/referensi relevan. Prompt global seluruh 126 task di sana bukan izin menjalankan semua tahap; scope sekarang hanya file yang dikirim pemilik ini.

## Hasil dan urutan dalam file ini

### G02 — Pasar, detail saham dan grafik

Kesesuaian: Daftar pasar dan halaman detail memakai renderer, updater dan data grafik yang sama.

Urutan dalam kelompok: N03: ranking/filter → satu pekerjaan grafik terpadu untuk N05/N06/N14: hapus UI yang ditolak, empat rentang, data dan aksesibilitas → pemeriksaan refresh/reload.

Syarat keluar: Seluruh 22 ID lulus; tiga movers sesuai data/filter, empat rentang memakai OHLCV nyata, tabel/blok teknis hilang, grafik dan fokus tetap benar.

## Konteks pekerjaan yang perlu diperhatikan

Kerjakan tabel/blok teknis dan empat rentang sebagai satu perubahan grafik. Tiga tes chart yang pernah lulus belum meliputi seluruh boundary/reload/replay/performa/browser. N15 berada pada file 04 dan tidak diimplementasikan dalam file ini.

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

- [ ] **N03-01 — IN_PROGRESS**: Samakan sumber ranking dengan pencarian, bursa, sektor, active-only dan watchlist.
  - Implementasi/bukti parsial tercatat: Ranking follows filtered list including watchlist
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/ui-initial-01/results.json
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Buktikan sumber ranking mengikuti pencarian, bursa, sektor, active-only dan watchlist yang benar-benar berisi data; jangan hanya fixture watchlist kosong. Tetap belum selesai sampai seluruh kriteria N03-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N03-02 — IN_PROGRESS**: Perbaiki renderer awal dan updater realtime agar keduanya memakai aturan yang sama.
  - Implementasi/bukti parsial tercatat: Shared ranking initial/live renderer
  - Rujukan bukti: android/app/src/main/assets/www/app.js
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Periksa renderer awal dan updater memakai helper ranking yang sama; uji refresh berulang tanpa menggandakan item atau mengubah filter. Tetap belum selesai sampai seluruh kriteria N03-02 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N03-03 — IN_PROGRESS**: Tambahkan navigasi item ke detail emiten, focus state, serta label perubahan bertanda.
  - Implementasi/bukti parsial tercatat: Clickable ranking rows and focus restoration implemented; deep interaction pending
  - Rujukan bukti: android/app/src/main/assets/www/app.js
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Uji klik setiap mover ke detail, keyboard, tanda perubahan dan pemulihan fokus pada mobile/200%. Tetap belum selesai sampai seluruh kriteria N03-03 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N03-04 — IN_PROGRESS**: Uji tiga/kurang dari tiga, semua nol, semua positif, semua negatif, seri, nilai invalid dan ticker lintas bursa.
  - Implementasi/bukti parsial tercatat: Logic RED/GREEN: sign/zero/invalid/ties/unique/cross-exchange
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/test-market-movers.cjs
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Ulangi ranking tiga/kurang dari tiga, nol, semua positif/negatif, tie, invalid dan ticker lintas bursa; cocokkan expected order serta ID unik. Tetap belum selesai sampai seluruh kriteria N03-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N03-05 — IN_PROGRESS**: Verifikasi refresh tidak menggandakan item, mengacak fokus, atau mengubah filter pengguna.
  - Implementasi/bukti parsial tercatat: Live refresh implementation; focus/order/layout acceptance pending
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/ui-initial-01/results.json
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Buktikan refresh mempertahankan urutan stabil, filter, fokus dan scroll tanpa item ganda pada data yang berubah. Tetap belum selesai sampai seluruh kriteria N03-05 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N05-01 — IN_PROGRESS**: Telusuri seluruh state, node, event, CSS dan tes tabel alternatif.
  - Implementasi/bukti parsial tercatat: Chart table state/node/listener traced
  - Rujukan bukti: android/app/src/main/assets/www/app.js
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Periksa ulang seluruh state/node/event/CSS/selector tes tabel alternatif; bedakan dependency mati dari data OHLCV yang masih dipakai. Tetap belum selesai sampai seluruh kriteria N05-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N05-02 — IN_PROGRESS**: Hapus UI dan cleanup dependency yang tidak memiliki pemakai lain.
  - Implementasi/bukti parsial tercatat: Alternative table/toggle/state removed; dead CSS/test audit pending
  - Rujukan bukti: android/app/src/main/assets/www/app.js
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Tuntaskan cleanup tombol/tabel/state/listener/CSS yang tidak punya caller lain; uji tidak ada akses tersembunyi atau tab stop kosong. Tetap belum selesai sampai seluruh kriteria N05-02 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N05-03 — IN_PROGRESS**: Pertahankan render candle/line/volume, data OHLCV domain, pemilihan rentang, serta focus control lain.
  - Implementasi/bukti parsial tercatat: Chart kept; new domain ranges connected; browser verification pending
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N14-green-01/junit-latest.log
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Buktikan candle/garis/volume dan empat rentang tetap memakai data domain yang benar setelah penghapusan tabel, termasuk live refresh. Tetap belum selesai sampai seluruh kriteria N05-03 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N05-04 — IN_PROGRESS**: Verifikasi grafik tetap dapat dipahami tanpa warna saja, dan nilai penting dapat diakses tanpa hover mouse.
  - Implementasi/bukti parsial tercatat: Selected candle range input added; keyboard/touch gate pending
  - Rujukan bukti: android/app/src/main/assets/www/app.js
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Tuntaskan pembacaan candle terpilih melalui keyboard/sentuh dan makna nonwarna; uji nilai dapat diakses tanpa hover. Tetap belum selesai sampai seluruh kriteria N05-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N05-05 — PENDING**: Uji tidak ada tombol, tabel tersembunyi, tab stop kosong, atau error saat refresh dan pergantian timeframe.
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Uji tidak ada tombol, tabel tersembunyi, tab stop kosong, atau error saat refresh dan pergantian timeframe. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N05-05 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N06-01 — IN_PROGRESS**: Telusuri penciptaan `provNotice` dan pemakainya.
  - Implementasi/bukti parsial tercatat: Technical chart notice traced
  - Rujukan bukti: android/app/src/main/assets/www/app.js
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Periksa seluruh pencipta/caller provNotice serta fallback sehingga blok teknis tidak muncul kembali dari jalur lain. Tetap belum selesai sampai seluruh kriteria N06-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N06-02 — IN_PROGRESS**: Hapus blok sumber/frame duplikat, rapikan jarak bawah grafik tanpa ruang kosong bekas elemen.
  - Implementasi/bukti parsial tercatat: Chart source/timeframe duplicate block removed
  - Rujukan bukti: android/app/src/main/assets/www/app.js
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Verifikasi blok sumber/timeframe duplikat dan ruang bekasnya hilang pada seluruh jenis grafik, sementara kontrol dan data tetap utuh. Tetap belum selesai sampai seluruh kriteria N06-02 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N06-03 — PENDING**: Pertahankan filter rentang dan status error/stale yang relevan.
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Pertahankan filter rentang dan status error/stale yang relevan. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N06-03 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N06-04 — PENDING**: Uji semua jenis grafik/timeframe serta riwayat kosong agar notice tidak muncul kembali dari fallback.
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Uji semua jenis grafik/timeframe serta riwayat kosong agar notice tidak muncul kembali dari fallback. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N06-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N14-01 — IN_PROGRESS**: Telusuri kontrak getChartData, fill timestamps, replay, retensi history dan seluruh caller grafik.
  - Implementasi/bukti parsial tercatat: Chart fill/replay/save history traced
  - Rujukan bukti: android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Lengkapi jejak chart data, timestamp fill, replay, retensi dan restore serta seluruh caller; pastikan pembacaan grafik tidak memajukan ekonomi. Tetap belum selesai sampai seluruh kriteria N14-01 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N14-02 — IN_PROGRESS**: Pisahkan konsep rentang dan interval; tentukan timestamp, zona, batas dan resolusi otomatis.
  - Implementasi/bukti parsial tercatat: Four range codes with separate automatic candle intervals
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N14-green-01/junit-latest.log
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Verifikasi kontrak rentang/interval/zona/batas dan resolusi otomatis pada source kini; pertahankan tepat empat kontrol/default satu hari. Tetap belum selesai sampai seluruh kriteria N14-02 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N14-03 — IN_PROGRESS**: Implementasikan filter/agregasi empat rentang pada domain/shared helper yang sesuai, tanpa future leak.
  - Implementasi/bukti parsial tercatat: Timestamp filter/sort/dedupe/OHLCV aggregation implemented; broader gate pending
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N14-green-01/junit-latest.log
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Ulangi filter/agregasi OHLCV dan volume dari fill terkontrol; uji no future leak, input tak terurut, dedupe serta batas bucket. Tetap belum selesai sampai seluruh kriteria N14-03 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N14-04 — IN_PROGRESS**: Hubungkan empat kontrol saja, default 1 hari, dan pertahankan pilihan saat refresh/Candle-Garis.
  - Implementasi/bukti parsial tercatat: Four UI ranges default R1D connected; new host/browser pending
  - Rujukan bukti: android/app/src/main/assets/www/app.js
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Uji empat kontrol pada host terbaru, default satu hari, refresh dan Candle/Garis; pilihan pengguna tidak boleh direset. Tetap belum selesai sampai seluruh kriteria N14-04 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N14-05 — IN_PROGRESS**: Pastikan retensi/restore cukup atau state kurang data jujur; hapus fallback yang memalsukan history pada jalur baru.
  - Implementasi/bukti parsial tercatat: Existing saved fills reused; no fake fallback in new path; reload coverage pending
  - Rujukan bukti: android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Buktikan riwayat 30 hari atau state kurang data jujur setelah reload; periksa seluruh jalur baru tanpa fallback history palsu. Tetap belum selesai sampai seluruh kriteria N14-05 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N14-06 — IN_PROGRESS**: Perbaiki sumbu tanggal/jam, ringkasan candle terpilih, gap/partial/empty dan akses keyboard/sentuh.
  - Implementasi/bukti parsial tercatat: Timestamp labels/partial marker/selected-candle input; visual/keyboard pending
  - Rujukan bukti: android/app/src/main/assets/www/app.js
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Uji label tanggal/jam, gap/partial/empty, candle terpilih, keyboard dan sentuh di layar kecil/200%. Tetap belum selesai sampai seluruh kriteria N14-06 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N14-07 — IN_PROGRESS**: Uji bucket boundaries, OHLCV/volume, weekend/sesi, lintas hari, instrumen, mode, reload dan data tidak berurutan.
  - Implementasi/bukti parsial tercatat: 3 tests GREEN for ranges/OHLCV/empty/future; replay/reload/boundary coverage pending
  - Rujukan bukti: Artifacts/Master-Plan-Gabungan-2026-10-24/N14-green-01/junit-latest.log
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Perluas dan ulangi bucket boundaries, weekend/sesi, lintas hari/instrumen/mode/reload, OHLCV/volume dan data tak terurut; tiga tes lama belum meliputi semuanya. Tetap belum selesai sampai seluruh kriteria N14-07 terbukti; catat command, hasil, bukti dan kekurangan tersisa.
- [ ] **N14-08 — PENDING**: Verifikasi browser 100%/200%, performa 30 hari, source/DTO/visual yang sama dan N05/N06 tidak kembali.
  - Pekerjaan/keputusan berikutnya: Hanya saat G02 aktif: Verifikasi browser 100%/200%, performa 30 hari, source/DTO/visual yang sama dan N05/N06 tidak kembali. Periksa kondisi source dan implementasi existing, lalu tuntaskan seluruh acceptance pada snapshot terbaru. Tetap belum selesai sampai seluruh kriteria N14-08 terbukti; catat command, hasil, bukti dan kekurangan tersisa.

## Rincian kontrak dan acceptance paket

Rincian diambil dari master aktif; checklist dipindahkan ke daftar ID di atas. Keputusan revisi 2 mengesampingkan palette/background lama yang bertentangan.

## 7. N03: Tiga penguat dan tiga pelemah

### 7.1 Solusi

Ganti dua highlight tunggal menjadi dua kelompok ringkas: **“3 Penguat”** dan **“3 Pelemah”**. Setiap baris berisi peringkat, ticker, perubahan persen, serta harga bila tidak membuat layar sempit. Baris merupakan tombol yang membuka detail emiten.

Gunakan basis daftar yang konsisten dengan filter pasar aktif. Jika yang ditampilkan berasal dari seluruh pasar alih-alih hasil filter, beri label “Seluruh pasar”; jangan menulis “di daftar” ketika sumbernya berbeda. Rekomendasi default: gunakan hasil filter yang sedang terlihat, termasuk watchlist, lalu kelompokkan.

Aturan data:

- Penguat hanya nilai perubahan > 0, urut menurun. Pelemah hanya < 0, urut menaik dari yang paling turun.
- Ambil tiga masing-masing bila tersedia. Jika hanya dua emiten menguat, tampilkan dua dan keterangan ringkas; jangan memasukkan saham turun sebagai penguat hanya untuk memenuhi angka tiga.
- Nilai nol netral dan tidak mengisi dua kategori sekaligus. Semua nol menghasilkan state “Belum ada penguat/pelemah”.
- Angka tidak valid/NaN atau emiten tanpa baseline perubahan tidak ikut ranking; jangan dipaksa menjadi 0.
- Tie diputus dengan aturan stabil, misalnya ticker lalu ID bursa. Instrumen tidak boleh muncul ganda karena ticker sama.
- Jangan memberi label paling kuat berdasarkan pembulatan persen yang kehilangan urutan; urutkan nilai numerik sebelum format.
- Pada ponsel, dua kelompok dapat bertumpuk; pada ruang yang cukup gunakan dua kolom. Tidak mengandalkan carousel tersembunyi untuk tiga item utama.

Penerimaan: dataset cukup menghasilkan enam item dengan ID unik dan urutan benar; dataset tidak cukup menampilkan jumlah yang benar dengan state jujur. Bukti berupa perbandingan input terkontrol, hasil ranking, klik detail, dan screenshot mobile normal/200%.

## 9. N05: Hapus tombol dan tabel OHLCV alternatif

### 9.1 Solusi

Hapus tombol “Tabel OHLCV Alternatif / Tutup Tabel OHLCV” beserta tabel alternatif yang dimaksud dari detail emiten. Jangan sekadar menyembunyikan tombol sementara tabel masih menerima fokus atau dapat dibuka melalui state lama.

Telusuri `chartTableOpen`, `#chart-table`, `toggle-table`, pemulihan scroll tabel, dataset fokus, serta selector pengujian. Hapus cabang yang benar-benar tidak dipakai. `getChartData`, array bar OHLCV, volume, timeframe, dan data riwayat tetap diperlukan untuk grafik, jadi tidak ikut dihapus.

Aksesibilitas grafik tetap wajib. Pilih implementasi kecil: beri grafik nama/ringkasan yang tepat dan sediakan ringkasan candle terpilih melalui fokus/keyboard atau pembacaan nilai yang sudah ada. Jangan membuat “tabel alternatif baru” yang diam-diam mengembalikan fitur yang ditolak pemilik.

Penerimaan: fitur yang ditandai benar-benar hilang, grafik tidak kehilangan data, dan tidak ada caller mati atau regresi aksesibilitas.

## 10. N06: Hapus blok sumber teknis di bawah grafik

### 10.1 Solusi

Hapus blok `provNotice` yang menampilkan “Sumber data: Matched trades domain engine internal” dan “Timeframe: D1”. Keduanya merupakan satu blok teknis yang dipilih pemilik.

Pilihan rentang/timeframe grafik tetap ada karena merupakan kontrol, bukan disclaimer. Label kontrol boleh menggunakan bahasa pengguna seperti “Harian” bila memang sesuai arti D1. Jangan menyebut D1 sebagai “1 hari terakhir” jika sebenarnya resolusi bar harian dengan rentang beberapa hari.

Sumber data tetap ada di domain/metadata/laporan audit. Hapus presentasinya pada lokasi yang diminta; jangan membuang field yang dipakai validator atau membuat chart tak bisa ditelusuri secara internal. Saat data benar-benar gagal/kurang, tetap tampilkan error singkat yang bisa ditindaklanjuti.

Penerimaan: dua teks yang ditandai tidak muncul, kontrol rentang tetap bekerja, dan hasil OHLCV sama sebelum/sesudah perubahan presentasi.

## 24. N14: Empat rentang grafik yang benar-benar memfilter riwayat

### 24.1 Kontrak data dan tampilan

Tampilkan tepat **6 jam | 1 hari | 7 hari | 1 bulan**. Default tiap pembukaan detail baru adalah 1 hari; refresh pada detail yang sama tidak mengubah pilihan pengguna. Seluruh rentang dihitung dalam kalender dunia game, bukan waktu nyata. Bulan = rolling 30 hari, bukan bulan kalender berjalan. Pilihan Candle/Garis tetap berfungsi; tabel OHLCV dan blok source/frame tidak kembali.

Rentang berakhir pada timestamp dunia yang diketahui; gunakan satu kebijakan konsisten untuk batas awal/akhir, misalnya start <= tradeTime <= observedNow, tanpa menggandakan fill antar bucket. Bar terakhir boleh parsial dan ditandai sewajarnya. Jangan memperlihatkan harga atau high/low/close masa depan pada replay.

Resolusi otomatis yang disarankan: 6 jam → 5 menit/candle; 1 hari → 15 menit/candle; 7 hari → 4 jam/candle; 30 hari → 1 hari/candle. Ini rekomendasi teknis, bukan empat tombol tambahan. Hermes boleh memilih granularitas setara berdasarkan data dan ukuran layar, mencatat alasannya, dan tidak mengubah arti rentang. Bila resolusi 4 jam belum didukung, perluas agregasi existing minimum; jangan membuat chart engine kedua.

Java menyediakan timestamp bucket mentah serta OHLCV. Gunakan epoch/zone kontrak existing; jangan memfilter dengan mem-parse string Sesi 1 atau jam tanpa tanggal. Agregasi memakai fill terurut, open pertama, close terakhir, high maksimum, low minimum dan volume jumlah transaksi yang sah. Double hitung sisi beli/jual dari fill yang sama dilarang.

Riwayat kosong/kurang dari rentang ditampilkan jujur. Gap pasar tutup tidak berarti error; jangan membuat perdagangan atau volume fiktif. Data harian tidak boleh diubah menjadi candle intraday palsu. Harga acuan yang tersedia harus dibedakan dari aktivitas yang benar-benar terjadi tanpa menambah disclaimer simulasi panjang.

Jika history in-memory terlalu pendek, simpan agregat domain minimum yang diperlukan untuk horizon 30 hari dan restore konsisten. Jangan menyimpan semua tick selamanya, menambah database/service baru, atau mengganti format save tanpa kebutuhan. Periksa apakah data existing sudah memadai sebelum menambah struktur. Operasi membaca chart tidak boleh memajukan waktu atau mengubah uang.

Penerimaan: dua rentang berbeda menghasilkan himpunan waktu yang berbeda bila data tersedia; OHLCV direkonsiliasi ke fill input; tidak ada candle masa depan atau volume palsu; seluruh rentang tetap dapat dipilih saat data kurang dengan penjelasan singkat. Simpan input terkontrol, expected buckets, output, screenshot dan timing. Konversi konseptual pada tempo baru: 6 jam game = 15 menit nyata; 1 hari = 1 jam nyata; 7 hari = 7 jam nyata; 30 hari = 30 jam nyata. Label kontrol tetap waktu game.

## Deliverable dan syarat menutup file

Bukti baru disimpan pada Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/. Gunakan save QA terisolasi; pertahankan log gagal.

1. Seluruh 22 ID file ini memenuhi requirement dan seluruh acceptance pada snapshot source yang sesuai. Bukti parsial tetap belum selesai.
2. RESULTS.md memuat ID/status, perubahan/asumsi, command/exit code, tes/review/performa, lokasi bukti, source hash, hash save dan temuan terbuka.
3. Tracker existing, STATUS-REPORT.md, Work/STATUS.json dan checkpoint sinkron. Tidak membuat master/tracker tandingan. Saat selesai catat WAITING_FOR_OWNER_PLAN dan jangan mengaktifkan file berikut.
4. Tutup atau catat kepemilikan/status proses QA file ini secara jelas. Jangan menutup preview/proses lain atau meninggalkan proses yang melanjutkan agent ke scope berikut.

File ini menyerahkan checkpoint dan bukti kelompoknya. APK mengikuti titik penyerahan yang sudah ditetapkan; jangan mengambil pekerjaan file lain untuk mengejar kandidat di luar kiriman ini.

## Instruksi berhenti

Setelah seluruh kriteria dan deliverable file ini selesai, jawab dengan PLAN-02, status ID, bukti utama, APK bila diwajibkan, dan temuan tersisa. Kemudian **BERHENTI DAN TUNGGU FILE PLAN BERIKUTNYA DARI PEMILIK**.

Jangan memulai plan berikut dari urutan, membuat reminder/automation untuk lanjut, atau menganggap pemilik diam sebagai persetujuan. Bila ada blocker nyata sebelum selesai, laporkan kekurangan spesifik dan tetap jangan berpindah file.
