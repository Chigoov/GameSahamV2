# Plan lanjutan: ketepatan pembaruan UI dan pembuktian performa

Tanggal: 3 November 2026, Asia/Jakarta.

**Klarifikasi pemilik terbaru mengungguli interpretasi LP09 di bawah:** tanggal/bulan berkedip dan data **Open/Prev/Lot/High/ARA/Val/Low/ARB/Avg pada tabel di bawah chart**. Bukan berita dan bukan running trade. Cakupan berita dikembalikan; daftar transaksi proposal tidak diterapkan. Rujukan [LP09-TABEL-STATISTIK-DAN-TANGGAL.md](LP09-TABEL-STATISTIK-DAN-TANGGAL.md), bukti `logs/lp09-stats-host-verification.json`: **95 tes Java / 27 kelompok browser / 1.000 aksi / 70 layout / 1.350 sampel host**; build dan paket terbaru folder `Artifacts/Codex-LP-Verification/lp09-daily-stats-2026-11-03/` diperiksa. Gate fisik tetap NEEDS_VALIDATION. Jangan memakai APK/news-check snapshot sebelumnya sebagai hasil final klarifikasi ini.

Tambahan instruksi pemilik setelah checkpoint host: **LP09 — glitch jam/tanggal kalender dan informasi emiten terakhir pada hari game tersebut**. Diperbaiki dan diuji host: **93 tes Java, 27 kelompok browser, 1.000 aksi UI dan 70 kasus layout**, serta pasangan APK yang sudah diperiksa. Rincian reproduksi, interpretasi informasi sebagai berita hari berjalan, perubahan tes, hash dan batas bukti: [LP09-JAM-KALENDER-DAN-BERITA.md](LP09-JAM-KALENDER-DAN-BERITA.md), bukti gabungan `logs/lp09-host-verification.json`. Komponen/teks existing dipertahankan; tidak redesain, tidak menampilkan berita masa depan, tidak mengarang berita untuk emiten tanpa berita hari tersebut. LP09/Poco tetap NEEDS_VALIDATION; paket baru belum dipasang dan tidak ada tindakan perangkat baru.

Checkpoint sesuai instruksi terakhir pemilik **lanjutkan host dahulu**: LP07/NV01 diperketat untuk menolak save dari bridge yang sudah digantikan meskipun timestamp panggilannya lebih baru, sambil tetap menerima simpan terakhir tanpa pengganti. Host terbaru: **93 tes / 19 suite terpilih, 23 kelompok browser**, build dan pemeriksaan APK pada `logs/save-owner-host-verification.json`; detail reproduksi/perbaikan pada [NV01-DUA-PENULIS.md](NV01-DUA-PENULIS.md). Paket terbaru di `Artifacts/Codex-LP-Verification/device-save-owner-fix-2026-11-03/` belum dipasang. Tidak ada ADB/input/perubahan ponsel baru pada kelanjutan host ini. LP07 keseluruhan tetap NEEDS_VALIDATION untuk lifecycle/performa dan sesi perangkat 60 menit.

Pembaruan pengujian perangkat: [TEMUAN-PENGUJIAN-PERANGKAT.md](TEMUAN-PENGUJIAN-PERANGKAT.md) menjadi rujukan status terbaru. Dua temuan tambahan storage/menu sudah diperbaiki dan diuji host; sesi fisik 60 menit belum selesai, dan koneksi USB terputus sebelum APK menu terbaru terpasang. Izin pengujian QA tetap berlaku; menunggu perangkat tersambung kembali, bukan persetujuan instalasi ulang yang sama.

Checkpoint host sebelum izin perangkat, 3 November 2026: **host/browser dan build telah diperiksa ulang serta diperbaiki pada pengambilalihan; gate Android/Poco X8 Pro saat itu NEEDS_VALIDATION**. Pada checkpoint ini pemilik memilih menyelesaikan host dahulu, sehingga saat itu belum ada ADB, instalasi atau pengujian perangkat. Bukti checkpoint: [VERIFIKASI-HOST-AKHIR.md](VERIFIKASI-HOST-AKHIR.md). Status setelah instalasi dan temuan perangkat ada di pembaruan di atas; jangan memakai checkpoint ini sebagai keadaan perangkat terkini.

Keputusan pemilik pada pengambilalihan: bila candle pilihan keluar dari rentang, pilih **candle terbaru**, sesuai preview QA `logs/verified-takeover-decision/current-latest.png`. Perilaku existing dipertahankan dan diuji; persetujuan ini tidak mengubah pilihan lain di luar rentang tersebut.

Keputusan tambahan pemilik: tampilkan `coverageNote` pemegang saham sesuai preview kedua di `logs/verified-ownership-review/ownership-proposed.png`. Penerapan dan regression check success/cache/retry telah dilakukan tanpa mengubah desain atau angka kepemilikan.

Revisi pendalaman 3 November 2026: tetap satu plan untuk satu sesi pelaksanaan. Cakupan diperluas ke pembaruan penguat/pelemah pada tab gabungan, scroll halaman detail, siklus hidup cache, dan pembacaan kuotasi berulang ketika overlay terbuka. Seluruh tambahan masih harus diverifikasi pada source yang diambil alih; tidak ada hasil tes baru yang diklaim dari penyusunan dokumen.

## 1. Tujuan dan batas kesimpulan

Menutup regresi pembaruan Beranda, pilihan candle, ketepatan grafik, dan cache pemegang saham yang ditemukan setelah optimasi PF02–PF04. Sesudah perilaku benar, ukur perubahan pada skenario yang setara sebelum menyimpulkan kelancaran pada Poco X8 Pro.

Hasil yang diharapkan: angka pasar terus diperbarui melalui jalur yang tepat, posisi baca pengguna tetap terjaga, grafik mengikuti data domain, kegagalan pemuatan dapat dicoba kembali, dan laporan membedakan pengukuran dari estimasi.

Pemeriksaan penyusunan plan berupa pembacaan source, laporan, runner dan log yang sudah ada. Tidak ada eksekusi aplikasi, browser, build atau perangkat pada tahap penyusunan. Temuan perlu regression check perilaku sebelum implementasi; risiko yang masih berupa hipotesis tidak boleh diberi status bug runtime terkonfirmasi.

## 2. Baseline yang diperiksa

- Root: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`.
- Branch: `codex/classic-ui-new-assets`.
- HEAD: `9653f7a992405b4dfaf28acddbf1caf158d03957` dengan banyak perubahan lokal. HEAD bukan identitas lengkap source yang diuji.
- APK saat penyusunan: `android/app/build/outputs/apk/debug/app-debug.apk`.
- SHA-256 APK: `611CC0ADAD496A9EE137FD7BFF7BEAB9639BB5B6080582A0B198AA47FD5682FA`.
- Log PF terdahulu mencatat 62 tes lulus pada 11 suite terpilih dan build berhasil. Ini bukti tercatat, bukan hasil pengulangan independen atau seluruh tes repo.
- `Work/STATUS.json` dibaca sebagai snapshot historis; klaim denominator lama tidak dipakai untuk kelulusan sekarang.
- Model sasaran dari pemilik: Poco X8 Pro. RAM, Android, WebView, refresh rate aktif dan mode daya belum diverifikasi.
- Pengujian perangkat, pengukuran frame/input perangkat dan sesi 60 menit masih terbuka.

Saat mulai, periksa ulang source/APK dan dokumen. Jika agent lain sudah memperbaiki suatu ID, uji hasilnya dan tandai `SUDAH_TERTUTUP_OLEH_PERBAIKAN_LAIN` tanpa diff tambahan.

## 3. Aturan pelaksanaan

1. Baca AGENTS.md, keputusan produk aktif, dan DESIGN.md. Produksi tetap Java + Android WebView + HTML/CSS/JS existing. Pertahankan 48×, sesi bursa, AON, anti-self-trade, ledger, save dan backup.
2. Jangan mengubah file yang sedang ditulis pelaksana lain. Tidak agent/launcher paralel, pesan lintas chat, reset/clean/stash, penghapusan massal, push/publish, atau konfigurasi global.
3. Gunakan helper dan runner existing setelah memeriksa kompatibilitasnya. Tidak framework, engine, event bus, cache layer atau dependency baru untuk kebutuhan spekulatif.
4. Optimasi internal tidak menjadi izin mengubah desain. Perubahan tampilan, teks, navigasi atau alur memerlukan preview nyata dan persetujuan sesuai aturan proyek. Jika perilaku fallback belum diputuskan, lanjutkan bagian independen.
5. Tidak ADB, instalasi, profiling atau tindakan emulator/perangkat tanpa izin yang berlaku untuk tindakan konkret tersebut. Jangan menganggap penunjukan plan mengizinkan penurunan versi, reset data, atau instalasi ke save pemain.
6. Save pemain `Artifacts/LocalPreview/preview-save/save_game_v2.json` hanya boleh di-hash; jangan dibaca, disalin, dimuat, dimigrasikan atau dijadikan fixture. SHA-256 referensi: `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`. Semua tes memakai save QA baru terisolasi.
7. Gunakan data sintetis berlabel QA. Mock bridge membuktikan perilaku UI terhadap kontrak uji; tidak membuktikan ekonomi Java atau biaya IPC Android. Laporkan lapisan bukti secara terpisah.
8. Setiap perbaikan nontrivial meninggalkan regression check minimum pada perilaku. Source-string check dan syntax check hanya pelengkap.

## 4. Urutan eksekusi

| ID | Hasil wajib | Prioritas/Status awal |
|---|---|---|
| LP00 | Checkpoint, baseline tetap, identitas source/runner dan reproduksi awal | DIPERBAIKI_DAN_DIUJI_HOST |
| LP01 | Kuotasi/indeks Beranda dan penguat/pelemah pada tab gabungan diperbarui tanpa render ganda | DIPERBAIKI_DAN_DIUJI_HOST |
| LP02 | Pilihan candle, readout dan fokus bertahan saat data berubah | DIPERBAIKI_DAN_DIUJI_HOST |
| LP03 | Grafik candle/garis benar pada update, batas periode dan perubahan skala | DIPERBAIKI_DAN_DIUJI_HOST |
| LP04 | Cache pemegang saham diperbarui saat perlu dan dapat pulih dari error | DIPERBAIKI_DAN_DIUJI_HOST |
| LP08 | Audit konteks cache, overlay, dan pembacaan kuotasi ganda; perbaikan hanya yang terbukti | DIPERBAIKI_DAN_DIUJI_HOST |
| LP05 | Tes perilaku gabungan, termasuk scroll utama/detail, resume dan urutan snapshot | DIVERIFIKASI_BROWSER |
| LP06 | Benchmark terukur dan koreksi klaim PF sebelumnya | DIPERBAIKI_DAN_DIUJI_HOST |
| LP07 | Regresi domain, build, gate Poco X8 Pro dan penyerahan | NEEDS_VALIDATION (Host: DIPERBAIKI_DAN_DIUJI_HOST; Device Hardware Test on Poco X8 Pro: OPEN / PENDING by policy) |
| LP09 | Tanggal/bulan berkedip dan sembilan kolom statistik harga harian di bawah chart, sesuai klarifikasi terakhir | DIPERBAIKI_DAN_DIUJI_HOST; PENGUKURAN/BUILD DIVERIFIKASI_HOST; DEVICE NEEDS_VALIDATION |

Urutan satu sesi: LP00 -> LP01 -> LP02 -> LP03 -> LP04 -> LP08 -> LP05 -> LP06 -> LP07. ID LP08 ditambahkan tanpa mengganti identitas tahap lama. Kerjakan setiap perbaikan dengan tes sebelum/sesudah. Jangan menunggu seluruh optimasi selesai untuk menemukan regresi perilaku. LP05 menggabungkan hasilnya sebelum LP06. Persiapan metode ukur dapat dimulai pada LP00; perbandingan akhir dilakukan setelah source stabil.

## 5. LP00: checkpoint dan reuse runner

- Catat root, branch, HEAD, dirty state dan batas file tugas. Bekukan identitas source, aset web, tes dan runner melalui manifest hash, dengan mengecualikan save pemain serta data sensitif.
- Baseline awal plan ini adalah versi PF yang saat ini ada. Jika artefak sebelum PF tidak dipertahankan, nyatakan tidak tersedia; jangan merekonstruksi pengukuran lama dari ingatan atau laporan.
- Simpan baseline QA/artefak yang diizinkan dengan nama berbeda agar build berikutnya tidak menimpanya. Catat hash, jenis build dan konfigurasi setara sebelum/sesudah.
- Inspeksi runner lokal dan alat terpasang sebelum menjalankan; jangan install atau mengunduh dependency. Gunakan isolasi sesuai aturan lingkungan untuk target-controlled code.
- Kandidat reuse yang ditemukan: `Work/test-live-sync-browser.py`, `Work/benchmark-performance-final.py`, `Work/verify-benchmark-performance.js`, dan `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/test-chart-browser.cjs`.
- Runner lama memuat asumsi yang mungkin usang: lima ID tab lama, hook pengaturan clock, kontrol power-save/skip, port QA tetap dan selector historis. Verifikasi semuanya; jangan menjalankan hook lama atau mengubah produk hanya agar runner lulus. Pakai pola yang masih sesuai dan buat adaptasi minimum di folder tugas.
- Satu runner perilaku terfokus untuk LP01–LP05 lebih disukai daripada framework atau sejumlah harness yang terpisah. Jalankan aset aplikasi produksi, bukan HTML tiruan yang menggambar ulang UI sendiri.
- Untuk QA browser yang memerlukan server, gunakan server lokal terisolasi yang sudah tersedia atau adapter existing yang telah diperiksa. Jangan mengekspos layanan keluar atau mengaktifkan endpoint QA pada APK produksi.

## 6. LP01: pembaruan Beranda

### Alur yang diperiksa

`requestClockSync.finish` -> `refreshSnapshot` -> pemilihan subview. Pada source yang diperiksa, `refreshSnapshot` memanggil `updateMarketQuotes` untuk Pasar/subtab pasar, tetapi tidak Beranda. Callback timer yang sebelumnya mencakup Beranda dihapus. `loadHomeMarket` membentuk daftar saat masuk tab/startup/resume, bukan pada tick rutin.

Fungsi `updateMarketQuotes` sudah memperbarui `.stock-list-item` pada daftar Pasar dan Beranda serta kartu indeks. Reuse jalur ini setelah menelusuri caller, bukan membuat timer atau fetch kedua.

Temuan terkait dari pendalaman: `switchTab('pasar')` mengubah `currentTab` menjadi `pasar_porto`, tetapi cabang penghitungan filter dan `renderMarketHighlights` di `updateMarketQuotes` masih memeriksa `currentTab === 'pasar'`. Jadi pembaruan baris harga dapat berjalan sementara peringkat penguat/pelemah tidak dihitung kembali pada rute produksi tab gabungan. Menambahkan Beranda saja belum menutup masalah ini.

### Langkah dan gate

- [ ] Tetap di Beranda, ubah harga/%/indeks melalui data QA lalu jalankan siklus sinkronisasi normal.
- [ ] Buktikan kegagalan awal atau alasan source terbaru sudah benar.
- [ ] Pulihkan cakupan panel terlihat pada satu jalur pembaruan; pertahankan penghapusan callback yang memang ganda.
- [ ] Uji Beranda, Pasar/subtab pasar, Portofolio, detail saham, tab tersembunyi, background dan resume. Kartu yang tidak ada tidak boleh menyebabkan error.
- [ ] Periksa identitas baris dan fokus tidak dibuang untuk memperbarui harga. Keanggotaan watchlist tetap mengikuti kontrak existing.
- [ ] Pada rute produksi Pasar/subtab pasar, ubah peringkat gainer/loser dengan data QA; tiga penguat/pelemah harus mengikuti hasil terbaru dan filter aktif. Uji pencarian, exchange, sektor dan watchlist, termasuk hasil kosong.
- [ ] Periksa cabang nama tab lama pada seluruh caller fungsi yang diperbaiki. Jangan mengganti navigasi atau memulihkan layout historis agar kondisi lama terpenuhi.

Gate: harga, persentase, indeks dan peringkat yang terlihat berubah tanpa pindah tab/reload; filter tetap benar; tidak ada siklus kedua untuk snapshot yang sama; jumlah bridge/render dihitung pada skenario yang dinyatakan, bukan diasumsikan.

## 7. LP02: mempertahankan candle yang dibaca

### Alur yang diperiksa

`openInstrumentDetail` -> `renderChart`. Tombol timeframe/jenis dipertahankan, tetapi selector penghapusan masih mencakup slider. `selectedBar` kemudian selalu diinisialisasi ke bar terakhir. `finally` memulihkan fokus ke kontrol bernama sama, bukan identitas candle yang dipilih. Update live juga menulis ulang readout ke candle terakhir.

### Langkah dan gate

- [ ] Pilih candle historis, pertahankan fokus/operasi slider, lalu kirim update canonical.
- [ ] Pertahankan pilihan berdasarkan identitas candle (`timestamp` dan konteks instrumen/timeframe), bukan indeks array yang bergeser pada rolling window.
- [ ] Bedakan pengguna yang mengikuti candle terbaru dari pengguna yang membaca histori, sesuai perilaku yang disahkan.
- [ ] Readout harus sesuai pilihan. Data live boleh maju tanpa mengganti candle pilihan pengguna secara diam-diam.
- [ ] Hindari pelepasan elemen yang sedang didrag jika pembaruan dapat dilakukan in-place. Uji pointer serta keyboard.
- [ ] Uji penambahan bar, pergeseran awal window, perubahan timeframe/jenis, data kosong dan kembali berisi. Jika candle pilihan keluar cakupan, gunakan fallback yang sudah disahkan; bila belum ada, siapkan keputusan/preview, jangan mengarang histori.

Gate: timestamp candle pilihan dan readout bertahan ketika masih tersedia; fokus, rentang slider dan interaksi tidak rusak. Jangan mengklaim zoom/pan tersedia atau terjaga bila kontrol itu belum ada di produk; uji yang benar-benar tersedia.

## 8. LP03: grafik canonical dan update yang tepat

### Kontrak yang ditemukan

Facade `getChartRange` menyediakan `instrumentId`, `range`, `rangeStart`, `rangeEnd`, `intervalMs`, `totalTrades`, `bars`, `source` dan `coverage`. Setiap bar memiliki `timestamp`, OHLC, volume dan `partial`.

Interval source: R6H = 300.000 ms game; R1D = 900.000; R7D = 14.400.000; R30D = 86.400.000. Verifikasi ulang sebelum implementasi. Pada 48×, lima detik nyata setara 240 detik game; interval refresh tetap dapat melintasi batas candle.

Jalur UI sekarang mengubah close/high/low bar terakhir dari harga kuotasi tanpa memeriksa bucket waktu, mempertahankan skala lama, dan hanya mengubah elemen SVG candle. Mode garis menunggu refresh penuh. `chartSignature` memasukkan seluruh payload termasuk `rangeEnd` yang terus bergerak, sehingga clock saja dapat memicu rebuild meskipun isi bar sama.

### Langkah dan gate

- [ ] Gunakan kontrak domain existing untuk membedakan perubahan bar aktif, bucket baru, tambahan histori dan perubahan rentang harga. Jangan mengarang open/high/low/volume dari tick yang tidak diamati.
- [ ] Jangan menulis harga saat ini ke candle historis yang sudah selesai ketika tidak ada bar aktif yang sah. Tidak ada transaksi berarti tidak menciptakan candle sintetis.
- [ ] Mode garis dan candle sama-sama merepresentasikan state canonical terbaru pada siklus UI yang disahkan.
- [ ] Jika harga keluar rentang plot, perbarui transformasi/axis bersama agar tidak terpotong atau menyesatkan. Periksa volume dan nilai USD/desimal yang sah.
- [ ] Bandingkan isi yang memengaruhi gambar dan metadata yang memang ditampilkan. Jangan memakai hash/cache yang mengabaikan perubahan penting, tetapi hindari rebuild hanya karena metadata clock yang tidak berpengaruh.
- [ ] Tetap pisahkan pengambilan data dari pekerjaan render mahal. Pilih minimum berdasarkan pengukuran; tidak otomatis mengembalikan redraw penuh setiap tick atau membangun pipeline baru.
- [ ] Uji R6H/R1D/R7D/R30D, dua mode grafik, bar baru tepat di batas, gap tanpa trade, extreme high/low, harga tetap, sesi tutup, empty->berisi, serta instrumen/account switching yang relevan.

Gate: setiap bar yang ditampilkan cocok dengan hasil domain untuk timestamp tersebut; mode garis tidak tertinggal akibat jalur candle-only; axis/readout/volume konsisten; pilihan LP02 bertahan; data sama tidak membuang plot/kontrol tanpa sebab. Perubahan cadence yang mengubah pengalaman memerlukan preview/persetujuan.

## 9. LP04: cache struktur pemegang saham

`mountIssuerOwnership` memuat awal melalui frame callback. `updateInstrumentDetailLive` menandai `dataset.loaded` sesudah memanggil updater, sedangkan updater menangani error di dalamnya. Penanda ini tidak membuktikan keberhasilan dan tidak mempunyai invalidasi ketika data pemegang saham berubah.

- [ ] Pisahkan keadaan belum dimuat, sedang dimuat, berhasil dan gagal dengan mekanisme minimum existing; jangan memakai boolean keberhasilan untuk menutupi error.
- [ ] Tandai sukses hanya sesudah payload tervalidasi dan render berhasil.
- [ ] Tetapkan invalidasi berdasarkan kejadian/versi yang sudah tersedia atau pengecekan terukur yang cukup. Jangan menyegarkan seluruh panel setiap frame, tetapi jangan membekukannya selamanya.
- [ ] Error sementara dapat dicoba ulang tanpa membuka ulang layar. Hindari retry rapat tanpa batas dan listener ganda.
- [ ] Hasil instrumen lama tidak boleh masuk panel instrumen baru. Pertahankan validasi identitas dan status panel masih terpasang.
- [ ] Uji gagal lalu sukses, payload identitas salah, transaksi yang mengubah holder, perubahan supply yang sah, data tidak berubah, panel ditutup dan instrumen diganti.

Gate: data berubah ketika sumbernya berubah; kegagalan tidak dikunci sebagai loaded; data sama mempertahankan elemen/fokus; tidak ada request ganda untuk pemuatan yang masih berjalan.

## 10. LP05: tes perilaku gabungan dan antrian snapshot

`verify_app_optimizations.js` lama memeriksa teks source, termasuk komentar. Itu bukan bukti perilaku DOM. Pertahankan syntax/source check jika berguna, tetapi jangan memakainya sebagai acceptance gate utama.

Skenario tambahan yang perlu diverifikasi, belum dinyatakan bug runtime terkonfirmasi: snapshot A tertunda saat scroll; update B yang lebih baru diterapkan melalui aksi/resume; timer scroll kemudian mencoba menerapkan A. Source mempunyai `pendingSnapshot`, tetapi tidak terlihat pembandingan versi atau pembersihan menyeluruh ketika state yang lebih baru diterapkan.

Cakupan scroll yang terlihat dari source: listener dipasang pada `.app-scroll`, sedangkan detail/modal dibuat sebagai `.flow-page` pada `document.body` dan mempunyai `overflow-y: auto` sendiri. Jangan menganggap scroll detail sudah dilindungi oleh flag area utama. Ini kekurangan cakupan listener yang terlihat di source; dampak jank dan solusi akhirnya tetap perlu dibuktikan lewat aksi browser.

- [ ] Buat urutan deterministik A/B, beberapa snapshot selama scroll, scroll selesai, resume, transaksi dan pergantian run. Periksa apakah state lama benar-benar dapat menimpa yang baru melalui jalur produksi.
- [ ] Bila terbukti, perbaiki pemilihan snapshot pada titik bersama yang tepat. Timestamp game saja tidak cukup untuk membedakan dua mutasi keuangan pada waktu game sama; jangan mengarang aturan versi yang membuang transaksi sah.
- [ ] Pertahankan keberhasilan receipt, akun aktif, status recovery dan snapshot baru. Jangan menunda akses pengamanan finansial hanya demi menahan render saat scroll.
- [ ] Cakup data/stale rejection hanya bila diperlukan; tidak membuat framework antrean baru untuk hipotesis yang tidak terkonfirmasi.
- [ ] Uji scroll `.app-scroll` dan `.flow-page` secara terpisah saat data terus datang, termasuk sentuhan/drag slider, pergantian overlay dan close selama timer tertunda. Render yang ditunda harus diterapkan pada konteks yang masih sah dan tidak membuat scroll sendiri berulang tanpa henti.
- [ ] Jika listener scroll detail diperlukan, gunakan pola minimum dengan cleanup yang benar. Buka/tutup detail berulang untuk membuktikan listener/timer tidak bertambah. Jangan menunda hasil transaksi atau menonaktifkan pengamanan domain hanya karena jari sedang menggulir.
- [ ] Uji selector/render produksi, node identity, nilai readout, jumlah bridge calls, console/page errors dan screenshot relevan. Fake clock/bridge berlabel QA boleh mengontrol urutan, bukan menjadi bukti IPC atau device FPS.
- [ ] Verifikasi teks 200% dan viewport 320/360 px pada komponen yang disentuh; pertahankan aksesibilitas.

Gate: LP01–LP04 lulus sebagai aksi UI nyata pada aset produksi; skenario antrian dinyatakan ditolak/terkonfirmasi/masih butuh validasi dengan bukti yang tepat. Tidak ada klaim browser PASS dari pencarian string.

### LP08: konteks cache, overlay dan pekerjaan berulang

Bagian ini dijalankan sebelum LP05. Tujuannya memeriksa hubungan antarperbaikan, bukan memperluas refactor ke seluruh aplikasi.

Temuan source tambahan:

- `refreshSnapshot` dapat menjalankan `updateMarketQuotes` untuk tab pasar yang mendasari overlay dan `updateInstrumentDetailLive` untuk detail saham pada siklus sama. Kedua fungsi membaca seluruh instrumen lewat `getInstruments('', '', null, true)`. Ini pembacaan identik yang terlihat di source; besar biaya, manfaat reuse, dan kebutuhan refresh masing-masing panel perlu diukur.
- `lastMacroCheckDate` memakai tanggal sebagai kunci dan `bridgeReadCache` memakai nama metode/argumen; belum terlihat invalidasi menyeluruh saat pergantian run. Ini hipotesis data cache lintas konteks, bukan klaim kehilangan uang. Reproduksi melalui new game/load/sync yang benar-benar dapat dicapai sebelum memperbaiki.
- Cache highlights memeriksa gabungan dua daftar tanpa pembatas kelompok. Periksa apakah identitas kelompok, konteks instrumen/run, filter, dan metadata yang benar-benar ditampilkan sudah terwakili. Jangan mengubah key jika tidak ada kasus perilaku yang salah.

Langkah:

- [ ] Catat pemanggilan aktual saat detail saham terbuka di atas Beranda, Pasar dan Portofolio. Hitung duplikasi identik dalam satu siklus, bukan hanya jumlah baris pemanggilan di source.
- [ ] Jika terbukti bernilai, reuse hasil pada siklus/context yang sama atau hentikan pekerjaan panel yang benar-benar tidak terlihat sesuai pola existing. Tutup overlay harus menampilkan data terbaru. Jangan membuat cache global baru atau membekukan keadaan domain.
- [ ] Lakukan pergantian run dengan tanggal game sama, restore QA berbeda, perubahan akun, instrumen, timeframe dan filter. Buktikan data macro/kuotasi/profil/pilihan tidak terbawa ke konteks salah. Gunakan keadaan QA sah; bila variasi tidak dapat dicapai pada produksi, catat batas dan jangan memaksakan perubahan produk.
- [ ] Bila stale cache terbukti, invalidasi pada batas konteks yang sesuai. Jangan menghapus seluruh cache setiap frame atau melemahkan pembatasan financial writes selama sinkronisasi.
- [ ] Periksa retry setelah bridge sementara gagal: cache gagal tidak boleh dianggap payload sukses; error harus jujur dan dapat pulih tanpa reload paksa.
- [ ] Uji mode MARKET pada tiket order yang dibiarkan terbuka ketika harga/kas/akun berubah. Periksa apakah estimasi yang ditampilkan konsisten dengan quote terbaru serta review/revalidasi domain. Jangan mengubah input LIMIT yang sengaja ditetapkan pemain dan jangan menambah aturan transaksi baru. Ini skenario verifikasi, bukan bug yang sudah direproduksi.

Gate: pembacaan berulang yang diperbaiki menunjukkan manfaat terukur dan tidak mengorbankan data saat overlay ditutup; hipotesis cache/tiket ditutup dengan hasil tes atau status NEEDS_VALIDATION yang spesifik. Semua perubahan tetap dalam fungsi terkait dan mempunyai satu regression check minimum.

## 11. LP06: pengukuran dan koreksi laporan

### Yang perlu dikoreksi

Benchmark Java existing memanggil bridge secara langsung, bukan melewati IPC WebView. Rumus `baselinePayloadPerTick` dan `optimizedPayloadPerTick` adalah proyeksi dari jumlah panggilan asumsi. Ukuran byte JSON dikali durasi bukan pengukuran heap, alokasi string, atau biaya garbage collector. Klaim 84% IPC, 54,9% payload, 60 FPS mentah dan stabilitas 100% belum ditutup oleh bukti tersebut.

### Metode minimum

- [ ] Koreksi lewat `KOREKSI-LAPORAN-PF.md`, pertahankan laporan lama sebagai riwayat. Bedakan pengukuran Java, pengukuran browser, estimasi dan bukti perangkat yang belum ada.
- [ ] Instrumentasi minimum di harness QA menghitung pemanggilan bridge dan render sebenarnya, per metode dan per skenario, termasuk polling status, chart dan pengambilan portofolio. Jangan menambahkan telemetri global/permanen tanpa kebutuhan.
- [ ] Rekam sampel individual, jumlah aksi, durasi, warm-up, metode percentile dan error. Jalankan tiga pengulangan setara; kumpulkan minimal 30 sampel untuk kategori respons yang dilaporkan p95. Beban dan konfigurasi harus sama.
- [ ] Ukur Beranda, Pasar, detail candle, detail garis, scroll dan resume secara terpisah. Jangan menerapkan angka satu layar sebagai hasil seluruh game.
- [ ] Uji beban QA kecil dan beban yang lebih panjang/ramai dengan jumlah order, posisi dan histori dinyatakan. Gunakan ukuran yang dapat dibenarkan oleh permainan dan sumber daya pengujian; jangan memilih angka ekstrem untuk menampilkan kegagalan yang tidak relevan atau mengekstrapolasi 100 panggilan kecil menjadi performa sesi panjang.
- [ ] Latensi browser dihitung sampai state yang dimaksud diterapkan, bukan hanya handler selesai. JS frame interval/Long Task adalah diagnostik browser, bukan bukti frame layar Android.
- [ ] Ukuran payload dicatat sebagai byte encoding yang disebutkan dan cara penghitungannya. Memori proses, heap Java, WebView dan GC memerlukan alat ukur masing-masing; jika tidak diukur tulis BELUM DIUKUR.
- [ ] Jika baseline awal PF tidak tersedia, bandingkan versi awal LP ini dengan hasil LP; jangan memberi angka before PF yang tidak dapat diulang.

Gate: setiap angka dapat ditelusuri ke sampel dan command/metode. Tidak melaporkan persentase peningkatan tanpa baseline sebanding; tidak menyebut perbaikan 50% render sebagai peningkatan FPS 50%.

## 12. LP07: regresi, Poco X8 Pro dan penyerahan

- [ ] Jalankan regresi domain yang relevan untuk trading, ledger, AON, order receipt, save dan lifecycle sesuai source yang disentuh. Nyatakan suite terpilih; tidak mengklaim seluruh repo dari daftar parsial.
- [ ] Jalankan tes perilaku LP dan simpan log/screenshot/trace minimum. Tidak ada dependency baru atau pengambilan eksternal untuk memaksa tes berjalan.
- [ ] Build APK debug offline setelah check relevan lulus. Catat hash APK, konfigurasi/toolchain dan kecocokan aset web APK dengan source. Build bukan pengujian perangkat.
- [ ] Hash save pemain akhir dan periksa diff. Hanya file lingkup yang ditinjau; tidak commit otomatis.
- [ ] Saat izin tindakan perangkat tersedia, catat model sebenarnya, Android, WebView, refresh rate, mode daya dan jenis build. Perbandingan memakai perangkat/kondisi setara.
- [ ] Target awal pada mode 60 Hz mengikuti plan PF: 60 FPS sebagai sasaran, distribusi waktu frame/p95/p99 dan jank dicatat, respons visual input p95 <=100 ms, serta tidak ada frame beku >700 ms pada interaksi berkelanjutan yang diuji. Startup/catch-up dilaporkan terpisah.
- [ ] Uji ketahanan 60 menit dan cek performa awal/tengah/akhir, memori dan error. Jalankan 10x100 aksi kritis yang berlaku sesuai standar proyek dengan QA terkontrol; jangan mengklaim aksi sintetis mencakup seluruh fitur.
- [ ] Jika belum ada izin/metode perangkat, lanjutkan gate host/browser yang sah dan tandai gate fisik NEEDS_VALIDATION. Jangan menganggap model Poco cukup untuk mengetahui spesifikasi/hasil.

Dokumen saat eksekusi: `BASELINE.md`, `LAPORAN-HASIL.md`, `KOREKSI-LAPORAN-PF.md`, dan `logs/` dengan command/exit code/sampel serta bukti minimum. Tidak perlu membuat placeholder hasil atau angka fiktif sekarang.

Format penyerahan per ID: status, pemicu, sebab, caller, file berubah, tes sebelum/sesudah, lokasi bukti, batas, keputusan terbuka dan next_action. Gunakan PENDING, IN_PROGRESS, DIPERBAIKI_DAN_DIUJI_HOST, DIVERIFIKASI_BROWSER, DIVERIFIKASI_ANDROID, SUDAH_TERTUTUP_OLEH_PERBAIKAN_LAIN, DITOLAK_DENGAN_BUKTI, NEEDS_VALIDATION atau MENUNGGU_KEPUTUSAN sesuai bukti.

Kriteria akhir: regresi Beranda, penguat/pelemah tab gabungan, pilihan candle, ketepatan grafik dan cache holder tertutup dengan tes perilaku; cakupan scroll utama/detail terbukti; hipotesis urutan snapshot/cache/tiket diputuskan dengan bukti atau dinyatakan masih terbuka. Laporan tidak menyamakan estimasi dengan pengukuran; transaksi/save tetap benar. Kelancaran Poco X8 Pro baru dapat dinyatakan untuk APK dan skenario yang benar-benar diuji di perangkat. Setelah penyerahan, berhenti.

## 13. Matriks penerimaan untuk satu sesi

Gunakan satu runner terfokus sejauh pola existing memungkinkan. Nomor T adalah skenario penerimaan, bukan kewajiban membuat satu file/suite baru per baris. Semua status saat penyusunan adalah BELUM DIJALANKAN.

| Kasus | Aksi dan pemeriksaan utama | Pemilik gate |
|---|---|---|
| T01 | Tetap di Beranda; harga/%/indeks berubah tanpa pindah tab dan tanpa refresh ganda | LP01 |
| T02 | Buka Pasar lewat navigasi produksi gabungan; urutan penguat/pelemah berubah sesuai data/filter | LP01 |
| T03 | Filter query/exchange/sektor/watchlist dan keadaan kosong tetap benar setelah update | LP01 |
| T04 | Pilih candle lama; update live tidak mengganti pilihan/readout/fokus | LP02 |
| T05 | Bar/window bergeser; pilihan dipertahankan berdasarkan timestamp yang masih tersedia | LP02/LP03 |
| T06 | Candle aktif berubah; OHLC/volume dan axis sesuai hasil domain | LP03 |
| T07 | Lewati bucket baru dan gap tanpa trade pada seluruh timeframe; tidak mengubah candle historis atau menciptakan volume | LP03 |
| T08 | Mode garis mengikuti data baru; mode candle dan pergantian timeframe tetap benar | LP03 |
| T09 | Harga/payload gambar tetap, metadata clock bergerak; tidak membuang plot/slider tanpa kebutuhan | LP02/LP03 |
| T10 | Holder gagal lalu sukses; retry bekerja, perubahan holdings/supply tercermin | LP04 |
| T11 | Tutup/ganti detail ketika update berlangsung; hasil lama tidak masuk panel baru | LP04/LP05 |
| T12 | Scroll area utama dan halaman detail sambil data berubah; flush satu kali pada konteks yang benar | LP05 |
| T13 | Snapshot A tertunda, B diterapkan, lalu flush; transaksi/run baru tidak ditimpa state lama | LP05 |
| T14 | Overlay dibuka/ditutup berulang; jumlah request/listener/timer tidak bertambah tanpa batas | LP08/LP05 |
| T15 | Pergantian run/tanggal sama/akun/filter; cache tidak membawa state salah; tiket order tetap direvalidasi | LP08 |
| T16 | Pengukuran terulang tiga kali, sampel mentah sebanding, angka estimasi diberi label | LP06 |
| T17 | Regresi ekonomi/save, aset APK, hash pemain, 10x100 aksi relevan dan sesi perangkat 60 menit sesuai izin | LP07 |

Jika satu kasus menghasilkan temuan baru di luar fungsi/lingkup ini, catat lokasi, pemicu dan dampak; jangan langsung melakukan perbaikan sambilan. Jangan memperpanjang audit tanpa batas atau mengklaim matriks ini membuktikan tidak ada bug lain di seluruh repository.

Tambahan LP10 disetujui pemilik setelah preview nyata: pemulihan gagal muat daftar saham dengan pesan dan tombol Coba lagi, query/filter tetap. Status **DIPERBAIKI_DAN_DIUJI_HOST**: 95 tes Java, 27 kelompok browser, 1.000 aksi, 70 layout existing, uji error/retry dan 32 layout tambahan. Pasangan APK dan tanda tangan diperiksa; bukti `logs/lp10-host-verification.json`, rincian `LP10-PEMULIHAN-DAFTAR-SAHAM.md`. Tema Stockbit sendiri tetap preview.

Audit penutupan host: LP06 diulang pada source LP10 yang sama dengan APK terbaru, 4 job exit 0 / 1.350 sampel mentah. Bukti `logs/lp10-performance-summary.json` dan `logs/lp10-performance-jobs.json`. Sesi perangkat lama diperiksa dan semuanya terminal/tidak lengkap; belum ada bukti APK LP10 terpasang atau sesi 60 menit selesai. Checklist awal di badan plan adalah requirement historis, bukan pengganti checkpoint dan bukti terkini.

Next action terkini: saat Poco tersedia, lanjutkan identitas/instalasi **APK QA LP10** (`Artifacts/Codex-LP-Verification/lp10-market-retry-2026-11-03/GameSaham-QA-debug.apk`), performa/lifecycle/sesi 60 menit sesuai izin sebelumnya. Pemeriksaan koneksi terakhir exit 0 tetapi kosong (`poco-recheck-lp09-daily-stats.txt`); pemilik kemudian menyatakan belum tersedia. Jangan meminta izin sama lagi atau memakai APK LP09 sebagai paket LP10. Goal keseluruhan belum selesai; browser bukan bukti perangkat.
