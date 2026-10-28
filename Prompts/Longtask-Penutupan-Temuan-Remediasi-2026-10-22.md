# Prompt Hermes: penutupan temuan audit lanjutan Game Saham

Saya pemilik proyek. Lanjutkan implementasi Game Saham berdasarkan audit terbaru Codex atas commit `49bbafb`. Pertahankan perbaikan yang sudah benar, tuntaskan temuan di bawah, uji perilaku nyata, review, perbarui tracker dan buat APK kandidat baru dari source akhir. Jangan berhenti pada rencana atau mengulang klaim bahwa semua temuan sudah ditutup.

## Mandat, root dan baseline

Root produksi: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`. Repo `Game Trading Saham` berbeda dan bukan tempat menulis atau mengimpor source/save/build.

Buktikan cwd, Git root, branch, HEAD, tracked/untracked changes, source fingerprint, writer aktif serta port/save QA sebelum perubahan. Baseline saat prompt disiapkan: `49bbafb`, branch `codex/owner-audit-portrait-v8`. Jika sudah berubah, periksa delta tanpa rollback paksa. Pertahankan semua local changes; jangan reset/clean atau mengikutkan dokumen lain secara otomatis ke commit.

Baca `AGENTS.md`, `Work/README.md`, konsep/PRD/design brief serta mandat dan standar yang dirujuk AGENTS. Baca juga:
- `Prompts/Longtask-UX-Motion-Realtime-100-Perusahaan-2026-10-21.md`.
- `Prompts/Longtask-Remediasi-Audit-UX-Realtime-2026-10-21.md`.
- `Work/CHECKPOINT-REMEDIASI-UX-REALTIME-2026-10-21.md`.
- `Work/AUDIT-REMEDIASI-UX-REALTIME-2026-10-21.md`, `Work/STATUS.json` dan inventaris aset Codex.

Prompt ini memerinci penutupan yang belum lulus; tidak menghapus kriteria mandatory dari mandat terdahulu. Nomor baris adalah petunjuk baseline, cocokkan fungsi dan source aktual.

Save pribadi yang dilindungi: `Artifacts/LocalPreview/preview-save/save_game_v2.json`; SHA256 `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`. Periksa sebelum/sesudah. Jika berbeda, diagnosis tanpa overwrite. Jangan gunakan save ini atau preview8766 untuk tes mutasi. Gunakan save QA terisolasi dan verifikasi metadata/ownership server sebelum transaksi/reset.

APK baseline yang harus disimpan: `Artifacts/Remediasi-UX-Realtime-2026-10-21/run-01/GameSaham-v1.0.0-remediasi-ux-realtime.apk`, 954753 byte, SHA256 `33313c1df9a2f80f11e5d450f66138c766bb3f4ec56b04fa1ae92e44a6f8961a`.

Pertahankan 100 perusahaan aktif (50IDX+50US),12kategori,0nonaktif; filter kategori/pencarian/bursa/watchlist; migrasi katalog/save; pengamanan IPO/inventory sekali saja; application ID `com.tradingglobal3d.mobile` dan signing identity. Baseline461tes/55suite adalah hasil terdahulu, bukan target jumlah atau kelulusan otomatis source baru.

## Aturan pelaksanaan

Produksi tetap Java + Android WebView + HTML/CSS/JS lokal. Java satu pemilik uang, ledger, order, harga, clock dan save. JavaScript hanya presentasi/input. Telusuri caller sebelum perubahan; perbaiki shared root cause. Reuse fungsi, token, dependency dan runner yang tersedia; jangan menambah engine, framework, event bus atau tracking system baru. Logika nontrivial menyisakan runnable regression minimum yang bermakna.

Pekerjaan ini mengizinkan implementasi repo, tes domain/browser, review, build dan commit lokal yang sudah diperiksa. Tidak mengizinkan push/publish, pesan ke pihak lain, pembelian layanan, perubahan profil/provider/model/credentials/global Hermes, scheduler baru, ADB/emulator/perangkat fisik atau perubahan repo/data lain. Tidak perlu persetujuan rutin dalam scope.

Multi-agent sesuai kemampuan nyata: satu koordinator dan satu writer per file/shared state/build output; reviewer berbeda membaca delta dan bukti. Tetapkan ownership sebelum paralel. Jika reviewer terpisah tidak tersedia, catat batasnya; jangan mengarang review atau menjalankan launcher paralel.

## N1. Detail posisi setelah penjualan habis harus tetap benar

Temuan: cabang posisi ditutup pada `syncPortfolioLive`, sekitar app.js464–472, hanya mengganti nilai utama/jumlah/catatan. Unrealized P/L dan realized P/L tetap lama, serta posisi US dipaksa menjadi `Rp 0`. Bridge `getPortfolio` sekitar1390–1395 masih menyediakan metrik terbaru posisi nol.

Perbaiki updater agar seluruh metrik yang ditampilkan konsisten dengan snapshot terbaru, termasuk quantity, mata uang, nilai posisi, harga, average cost, unrealized dan realized gross. Bedakan nol yang sah, data tidak berlaku, serta riwayat posisi yang masih tersedia. Jangan menyembunyikan hasil penjualan dengan menghapus posisi/riwayat domain. Jangan menyebut hasil gross sudah net setelah fee.

Acceptance minimum: posisi IDX dan US dengan P/L nonnol; buka detail lalu jual sebagian dan habiskan posisi melalui alur domain QA yang sah sambil detail tetap terbuka. Bandingkan seluruh angka dan satuan DOM dengan domain setelah tiap perubahan. Hanya assert `0 saham` tidak meluluskan N1. Uji juga identitas posisi berubah dengan jumlah row tetap agar callback dan rincian tidak kembali basi.

## N2. Detail instrumen harus sinkron sampai grafik, tape dan status

Temuan: `updateInstrumentDetailLive` sekitar575–657 sudah memperbarui harga utama/depth/holdings/tombol, tetapi grafik/OHLCV, tape dan teks status pasar masih dari saat layar dibuka. `renderChart` sekitar2020–2163, status2170, tape2194–2202 belum direkonsiliasi otomatis.

Perbarui komponen existing secara terarah ketika revisi/data domain berubah; pertahankan timeframe, jenis grafik, scroll/fokus dan keadaan input. Tidak merender ulang seluruh flow setiap tick. Tangani tape awal kosong lalu mendapat transaksi pertama, pembaruan candle berjalan, candle baru, perubahan halt dan pemulihannya. Grafik dan tape wajib berasal dari riwayat game yang benar. Tombol dan tulisan status harus sepakat dengan domain. Jangan mengubah clock atau membuat grafik sintetis demi efek visual.

Acceptance: detail dibiarkan terbuka, proses transaksi/tick melalui jalur domain yang sah, lalu cocokkan harga, candle terakhir, tape, status dan tombol dengan revisi terbaru tanpa refresh/pindah tab. Uji setiap mode grafik/timeframe yang terdampak, dengan interval yang memang belum memiliki bar baru diperlakukan sesuai kontraknya. Sertakan perubahan status halt melalui fixture domain/test harness terisolasi jika diperlukan; fixture setup bukan bukti perjalanan produksi/native.

## N3. Pisahkan persiapan QA dari bridge produksi

Temuan: `generateNpcLiquidity()` ditambahkan sebagai `@JavascriptInterface` di `TradingWebBridge.java` sekitar1491–1500 untuk dipakai `Work/test-live-sync-browser.py`. Metode langsung memicu pasar/progres, sedangkan jalur normal memiliki gate menit/sesi kalender. Ini bukan fitur UI yang diminta.

Pindahkan kebutuhan setup ke host/test harness yang terisolasi, tanpa meninggalkan metode mutasi khusus tes di bridge produksi. Telusuri semua caller sebelum menghapus/mengganti. Jangan mengganti namanya lalu tetap mengekspos bypass yang sama. Pertahankan jalur domain normal dan safety gate-nya.

Acceptance: periksa daftar metode bridge dan APK/source akhir; helper QA tersebut tidak terekspos. Siapkan state deterministik lewat harness existing. Untuk acceptance live sync/trading, jalankan transaksi dan clock melalui jalur normal yang mematuhi sesi pasar. Assertion mencakup paused/closed/active dan memastikan setup tidak diam-diam dianggap perilaku produksi. Jangan melemahkan aturan kalender agar tes lulus.

## N4. Perbaiki kontras dan keterbacaan teks finansial

Temuan visual yang sudah diverifikasi: `.stock-ticker` dan `.stock-price` pada Beranda memiliki computed color `rgb(0,0,0)` di atas kartu gelap `rgba(18,24,40,0.72)`. CSS sekitar1206/1217 belum menetapkan warna yang sesuai.

Gunakan token warna bersama pada komponen/sumber pewarisan yang benar. Telusuri penggunaan row/tombol yang sama di Beranda/Pasar dan state lain; jangan menambal satu screenshot saja. Target teks biasa minimal kontras4,5:1 terhadap background komposit aktual, termasuk mode normal/high-contrast/power-save, dengan state disabled tetap dapat dipahami. Harga, tanda positif/negatif, mata uang, nama akun dan CTA tidak boleh bergantung pada warna saja.

Acceptance: computed style dan rasio terukur, screenshot before/after yang dibuka dan diperiksa,7viewport × font100%/200% nyata pada state terdampak. Periksa overflow internal, clipping, target sentuh48dp dan fokus keyboard; document.scrollWidth kecil bukan bukti keterbacaan. Pertahankan tema dan token yang sudah disetujui pemilik.

## N5. Buktikan perilaku, satuan dan deduplikasi dengan assertion tepat

Perbaiki runner existing `Work/test-live-sync-browser.py`, jangan membuat framework paralel. Catatan audit: kas IDR/Bisnis masih diuji dengan `!=`; USD hanya diuji bukan nol; deskripsi menyebut cakupan yang belum benar-benar diuji.

Setiap kasus menyimpan nilai awal, hasil domain terkonfirmasi, identitas akun/request/revisi, dan nilai DOM akhir. Bandingkan angka/mata uang/jumlah dengan nilai yang diharapkan, memakai nilai domain/desimal yang benar; jangan membuat mesin hitung finansial kedua atau floating-point uang. Kas tersedia yang berubah karena reserve belum membuktikan settlement selesai.

Minimal tutup N1–N4 serta kontrak remediasi terdahulu yang belum terbukti: BUY/SELL IDX dan US; USD awal nol maupun nonnol; OPEN→beberapa PARTIAL dengan status nama tetap→FILLED; cancel sisa; reject/expired bila applicable; pendapatan/payroll pada pergantian hari; akun pribadi/perusahaan/klien tidak tercampur; resume dan restore save QA; draft/input/fokus/scroll tetap terjaga.

Biarkan layar target terbuka saat domain berubah. Gunakan kondisi/revisi yang teramati dan timeout, bukan sleep sebagai bukti. Ukur commit domain→DOM/paint yang benar; interval polling1detik atau timeout5detik tidak otomatis memenuhi target≤300ms. Untuk aksi lokal terkonfirmasi P95≤300ms sesuai mandat, dan settlement tertunda diukur setelah domain commit, bukan sejak pemain memasang order mengantre. Reuse mekanisme produksi minimal untuk memenuhi kontrak tanpa clock kedua/busy polling.

Deduplikasi: `activeToastKeys` saat ini membuang key setelah5detik dan beberapa kejadian memakai fallback teks. Uji request/kejadian sama setelah lewat TTL, retry, pindah tab, reload/restore dan run baru; kejadian berbeda berteks sama tetap muncul. Gunakan event/revisi/request ID domain yang tersedia dan cakupan akun/run yang tepat; hindari Set global tanpa batas. Jangan menyebut dedup lintas restore lulus hanya karena satu popup terlihat.

Jalankan regresi domain penuh dan browser yang applicable. Pakai runner10×100aksi finansial dengan invariant sesuai standar existing jika jalurnya terdampak; label cakupan. Rekam error browser/unhandled rejection/dialog tak diharapkan dan kondisi gagal. Setiap assertion temuan harus terbukti mendeteksi versi lama atau fixture gagal yang ekuivalen sebelum menunjukkan versi baru PASS. Jangan mengklaim cakupan dari docstring/nama tes saja.

## N6. Benchmark harus memenuhi ukuran sampel dan mengukur hasil terlihat

Kondisi `or len > 0` sudah diperbaiki; pertahankan. Namun `range(5)` dan `len(heavy_samples)>=5` sekitar benchmark179–217 menurunkan mandat minimal30sampel. Pengukuran `page.evaluate(skipDay)` hanya mengukur panggilan domain, belum paint/feedback maupun respons event loop.

- Tetapkan jenis aksi sebelum pengukuran. Minimal30sampel sah untuk SETIAP jenis aksi wajib, termasuk heavy work. Jangan mengganti angka target agar sesuai hasil, menggabungkan kategori cepat untuk menutupi yang lambat atau membuang timeout/outlier.
- P95 per jenis aksi biasa≤300ms; SELURUH sampel navigasi≤1000ms; feedback heavy work benar-benar tergambar≤300ms dan event loop responsif. Ukur feedback awal dan penyelesaian hasil secara terpisah. Ukur jalur UI produksi, bukan bridge-only, untuk klaim UI. Beri label DOM/paint proxy browser beserta keterbatasannya; jangan mengklaim waktu layar Android fisik.
- Simpan rawtiming, jumlah perjenis, maksimum, P95, environment, source/compiled-class fingerprint, warmup/cache, seed dan beban pembaruan otomatis/katalog100. Pastikan server tidak memakai kelas Java lama setelah bridge diperbaiki.
- Sediakan runnable negative check yang menjamin kasus lambat FAIL, sampel kosong FAIL,29sampel FAIL,30sampel sah dapat PASS. Semua summary/verdict/assert/exit code memakai syarat sama. Kegagalan harus menghasilkan hasil FAILED baru, bukan meninggalkan JSON PASS run lama sebagai hasil terbaru.
- Jika batas gagal: diagnosis, perbaiki akar penyebab, uji ulang dan review. Jika pengukuran sah belum tersedia, NOT_TESTED/PARTIAL dengan alasan konkret; jangan menyebut host selesai100%.

## N7. Rekonsiliasi laporan, review dan build APK kandidat

Gunakan direktori run unik di bawah `Artifacts/Penutupan-Remediasi-2026-10-22/`. Parameterkan output runner existing agar bukti tidak lagi menimpa `Artifacts/Final-Verification-2026-10-21/` atau run lama. Jangan menghapus riwayat gagal.

Update tabel temuan existing dengan status OPEN/IN_PROGRESS/VERIFIED/REOPENED, reproduksi, akar masalah, perbaikan, assertion yang mendeteksinya, bukti source akhir dan hasil reviewer. Perbaiki klaim laporan sebelumnya melalui catatan koreksi yang jelas tanpa menghapus riwayat. Tracker `Work/STATUS.json` harus menunjuk checkpoint, APK/SHA, benchmark, jumlah/cakupan tes dan next_action aktual. Jangan membiarkan evidence lama terlihat seolah hasil run baru, atau menghapus pekerjaan lama yang memang sudah terbukti.

Reviewer berbeda meninjau diff DAN ketepatan assertion/pengukuran; temuan ditutup dengan bukti, bukan hanya komentar bahwa sudah dikerjakan. Nol bug terbuka diketahui termasuk minor untuk menyatakan subpekerjaan host selesai. Jika review terpisah tak tersedia, sebutkan batasnya. Kelulusan266flow/70viewport/24journey tetap diberi scope aslinya, bukan seluruh acceptance produk14fase.

Sesudah perubahan stabil dan bukti sesuai source akhir, build APK kandidat baru. Verifikasi ZIP, application ID/version/minSDK/targetSDK, signing certificate terhadap baseline, byte aset web/katalog,100aktif50IDX50US12kategori, tidak ada save/helper/fixture QA/dataset karantina terpaket. Hanya aset data QA yang benar-benar boleh didistribusikan mengikuti aturan existing. Verifikasi hash save pribadi dan migrasi/recovery save QA. Cantumkan path, ukuran byte, SHA256, commit/source fingerprint dan compiled source yang dipakai. Simpan APK baseline. Jangan mengaku APK sudah terpasang atau native tervalidasi.

## Aset dan definisi selesai

Ilustrasi raster tetap dibuat oleh Codex sesuai permintaan pemilik. Hermes memverifikasi asset ID/objek domain/selector/path, menyiapkan slot/fallback dan mengintegrasikan hanya aset yang sudah benar-benar tersedia; tidak perlu menciptakan fitur/objek baru untuk mengejar daftar19gambar. Jangan memakai placeholder sebagai bukti gambar final atau mengarang bahwa gambar sudah generated. Kerjakan semua pekerjaan host yang independen sambil menunggu aset.

Bedakan secara eksplisit:
- N1–N7 host VERIFIED hanya jika seluruh acceptance applicable lulus dan temuan ditutup.
- Keseluruhan mandat UX tetap PARTIAL / ASSETS_PENDING_CODEX jika ilustrasi yang diperlukan masih menunggu. Jika host masih gagal, tulis HOST_PARTIAL dan temuan spesifik; jangan menyalahkan pending gambar untuk menutupi bug host.
- Android native/instalasi/perangkat fisik tetap NOT_TESTED/DEFERRED dalam scope ini. Native di luar scope bukan alasan mengarang kelulusan ataupun berhenti mengerjakan host.
- Kelulusan N1–N7 bukan otomatis14/14 atau penerimaan seluruh produk. Laporkan fase sesuai evidence nyata.

Laporan akhir bahasa Indonesia: status tiapN1–N7 dan temuan; perubahan yang benar-benar selesai; bukti jumlah/P&L/mata uang; cara penjualan dan pendapatan terlihat tanpa refresh serta aturan settlement; cakupan tes dan negative checks; benchmark perjenis beserta rawtiming; screenshot yang diinspeksi; hasil reviewer; aset pending/terintegrasi; perlindungan save; APK/path/ukuran/SHA/source; batas host/native; next_action yang konkret. Jangan menjanjikan nol bug tersembunyi atau nol kehilangan progres mutlak.

Kerjakan hasil terukur→cek→review→checkpoint→lanjut. Jangan berhenti karena durasi, jumlah iterasi/giliran, token atau satu tes hijau. Dua kegagalan sama tanpa diagnosis baru: kirim PERLU PERHATIAN dan ganti pendekatan; tiga: pindah pekerjaan independen atau BLOCKED hanya bila perlu tindakan luar. Hormati STOP/pause dan checkpoint aman.

Mulai dengan verifikasi root/baseline/save, baca sumber keputusan, lalu langsung reproduksi dan perbaiki N1. Lanjutkan N2–N7 sesuai dependensi tanpa meminta persetujuan rutin.
