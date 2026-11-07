# Checkpoint 08 — browser kembali tersedia, perbaikan hasil UI nyata

Goal aktif, E01–E11 belum dinyatakan complete. Turn sebelumnya menghasilkan progres (aset/APK); turn ini juga menghasilkan perubahan source, reproduksi merah/hijau, browser QA dan APK. Pemilik sempat pause lalu active lagi selama pekerjaan; status terbaru active. Jangan memakai audit blocked dari checkpoint07 sebagai hitungan baru.

## Perbaikan produksi

- Pembukaan dan tutorial masih menyebut jam IDX09–16. Keduanya kini menyebut IDX/US24jam dan pergantian hari00.00WIB. Cutoff reksa dana16.00 dipertahankan karena berbeda produk.
- Aturan tema `.stock-symbol` dengan background!important menimpa logo SVG dan sprite referensi. Selector kini hanya mengenai simbol inisial, sehingga logo asli tampil dengan warna/latar yang sesuai. Browser membuktikan BBCA background putih dan gambar loaded.
- Kontrol chart memakai grid responsif maksimal4kolom, minimum4rem dan target44px, berkurang kolom saat teks diperbesar. Rentang waktu mengikuti pola serupa. Label keluar fullscreen dipersingkat Tutup, aria-label tetap Tutup layar penuh.
- Chart fullscreen sebelumnya berada di stacking context `.flow-page`z250 sehingga navigasi z300 menutupinya. Flow yang memiliki chart-expanded dinaikkan ke z3000. Screenshot sesudah perbaikan menunjukkan navigasi tidak menutup plot; Escape/tombol kembali memulihkan navigasi.
- Header pasar yang sticky menutupi baris BBCA pada320px/teks200%, sehingga klik tidak membuka saham. Sticky dihapus. Baris berhasil dibuka sesudahnya. Header utama dapat membungkus dan judul tidak dielipsis; inline session nowrap dihapus agar status/jam/iklim tidak terpotong pada teks besar.
- Saat clock sync berlangsung, Proxy bridge mengembalikan cache atau{} untuk read. Membuka Firma saat itu menampilkan kegagalan palsu dan IPO menampilkan katalog kosong. Loader Firma/Keuangan menunggu promise sync dan hanya meneruskan bila tab masih sama; pusat IPO menunggu dengan state memuat dan hanya membuka ulang bila panel masih terhubung. Tidak mengeksekusi ulang transaksi pemain. Check DOM menguji deferred read dan tidak membuka halaman yang sudah ditinggalkan.
- Pesanan IPO Saya kini tombol native berlabel Lihat pesanan TICKER. Enter terbukti membuka detail pesanan NCLD.
- Game baru menjadwalkan IPO bulan berjalan termasuk tanggal pencatatan yang sudah lewat, tetapi belum memprosesnya sampai midnight berikutnya. settleDueIpoListings dipakai bersama saat inisialisasi INTERNAL_MARKET, resume INTERNAL_MARKET dan event harian. Penjatahan serta pendaftaran saham tetap melalui allotIpo domain, bukan mengganti label UI. Mode replay tidak menjalankan perubahan ini.
- Pengali iklim pada harga pembukaan IPO sebelumnya menghasilkan harga tidak sah, contoh820×1.1=902 pada fraksi5. allotIpo kini membulatkan harga pembukaan ke tick bursa terdekat sebelum membuat Instrument. Tidak mengubah harga IPO yang sudah tersimpan/terdaftar secara retroaktif.

## Reproduksi dan pengujian

- Check Firma merah: `check-research-firm-ui.cjs` melempar Firma read before clock sync completed pada source lama. Setelah guard, lulus; callback tidak membuka ulang tab setelah pindah ke Beranda. Check IPO juga menguji callback diabaikan setelah panel ditutup.
- Java merah: Artifacts/Annotation-Execution/1793773897921090800/test.log,15tes/1gagal, Past IPO still ongoing: IPO-2026-10-1.
- Java merah resume:1793773966640909900/test.log,15tes/1gagal setelah register/save/resume IPO kedaluwarsa.
- Java merah tick:1793775033879329300/test.log,15tes/1gagal, harga pembukaan tidak memenuhi tick.
- Java terakhir hijau: Artifacts/Annotation-Execution/1793775050382267500/test.log,50tes9.221detik, exit0/session26720terminal. Suite: OwnerAnnotationPolicyTest15, IpoScheduleAndAllotmentTest, CorporateActionIpoVotingTest, OwnerDailyMarketActivityTest, UnifiedWorldClockTest, FirmClientMandateFeeTest, FirmAndPrestigeTahap5Test. SourceJava tidak berubah sesudah run ini.
- Node syntax, empat check chart-density/chart-controls/market-ipo/research-firm dan diff-check relevan exit0. Tiga check terakhir dijalankan ulang sesudah perubahan akhir JS; density tidak berubah. Kontrak DOM tidak disebut rendered proof.

## Bukti browser melalui CUA

Akses browser lokal awal turn tersedia. Server lama sudah tidak ada (CIM), dihidupkan kembali, lalu preview IAB dibuka dan diuji pada source produksi. Semua tindakan menggunakan save QA, bukan save pemain.

Folder bukti: Artifacts/Annotation-Execution/browser-resume-2026-11-04/.

- Chart: pilih7hari, Home→ArrowRight mengubah waktu/harga26Okt23.00Rp10.400→27Okt23.00Rp10.425; zoom, fullscreen, Escape dan kembali dicoba. Tidak membuktikan pinch pada perangkat fisik. Fullscreen screenshot awal chart-fullscreen-399.png menunjukkan perbaikan lapisan.
- Indeks LQ45 hanya10anggota game pada snapshot, pencarian BBCA menyisakan1baris. index-members-399.png. normal/text200 matrix JSON masing-masing8ukuran320×568,360×640,360×800,390×844,412×915,430×932,768×1024,1440×900; seluruh baris fit dan tidak ada luapan flow. Ini cek geometri, bukan visual review semua layar.
- IPO: Past PORT detail hanya membuka jadwal di detail. Upcoming AGRO Pantau→Dipantau. NCLD ongoing dipesan10lot/Rp500.000 pada QA64135, reload tetap ada. Sesudah2hari game QA menjadi10lot100%, nilaiRp500.000/refund0. Screenshot ipo-upcoming-detail-399.png, ipo-order-receipt-399.png, ipo-allocated-320.png. Satu percobaan pesan ditolak karena sedang sync; retry berhasil tanpa pesanan ganda.
- Firma: Novice menampilkan penjelasan dan syarat Portfolio Manager. QA64135 memakai seed karier PM (fixture prasyarat, bukan bukti promosi nyata), modal awalQA100miliar. Menerima Yayasan Dana Sejahtera membuat AUM100juta/1klien. Offset realClockQA+3.600.000ms menghasilkan2hari game: AUM99.991.780 dan laporan fee8.220. Screenshot firm-aum-after-two-days-399.png dan firm-fee-report-399.png. Siklus promosi domain memakai bukti tes checkpoint sebelumnya, bukan fixture UI ini.
- Riset: Basic menampilkan37saham terbuka diQA64134; QA64135 menampilkan35 karena katalog/listing berbeda. Pembelian Whale lewatUI berhasil setelah persiapan FX QA20miliarIDR→USD melalui API domain. Status aktif sampai18Nov15.03.05, toggle off lalu on diuji. Satu on gagal dan UI kembali off dengan pesan sync; setelah sinkronisasi on berhasil. research-whale-renewal-399.png. Bukan transaksi uang nyata.
- Harness `?qaTextScale=200` hanya menginjeksi rootfont200% pada HTML QA, tidak masuk APK. Default URL tidak memakainya. Browser menunjukkan root32px dan menemukan header clipping/sticky bug yang diperbaiki. chart-320-text200.png. Simulasi CSS ini bukan bukti skala font Android.
- Chart normal matrix8ukuran sesudah CSS grid akhir: kontrol muat, tinggi>=42clientpx (border membuat target>=44), maksimal4kolom, tidak ada luapan. chart-normal-matrix.json.

### Batas screenshot terakhir dan akses browser

Setelah resize berulang, screenshot chart-latest-399.png masih menampilkan susunan5kolom lama meskipun pembacaan DOM baru memperlihatkan4kolom dan posisi x12/103.5/195/286.5. Jangan menyebut gambar tersebut bukti visual CSS akhir. Upaya membuat preview terlihat lewat capability visibility kemudian mengembalikan browser.user is unavailable. listTabs masih menemukan tab2 URL64134; getAX/getTab/raw documented tabs API semuanya gagal dengan kondisi sama. Panduan troubleshooting sudah dibaca; jangan menginspeksi source browser atau beralih ke browser lain untuk mengakali batas. Runtime juga sempat timeout300detik saat snapshot sebelumnya; server64135 terbukti tetap hidup dan /qa-meta200, sehingga tidak di-restart hanya karena timeout.

Pada akhir turn akses inspeksi tab masih perlu dipulihkan; ini hambatan baru dalam resumed run, bukan alasan langsung menandai blocked. Browser bindings dapat hilang setelah timeout. Tab2 masih dikenal sebagai IAB, tab1 adalah errorpage dari upaya pertama saat server mati. Tidak berhasil markDeliverable/reset viewport setelah error terakhir; viewport terakhir399×615, URLnormal64134, chart fullscreen.

## APK, source dan server terkini

- Build akhir session8183 terminal exit0, BUILD SUCCESSFUL11detik. APK android/app/build/outputs/apk/debug/app-debug.apk SHA2561680ed96e17f20d9865133190944a6e344593ad7fdc241f35c7f4e88bc05f8c2.
- Lima belas aset JS/index/CSS/JPEG/logo/provenance/license di ZIP identik dengan source. HTTP JS/CSS64134 cocok. Bukti hash tersimpan final-source-apk-parity.json di folder browser evidence. Save pemain tetap SHA25659B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED.
- Server64134 terbaru session16512, classes1793775050382267500, Java finaltickfix+Work harness textScale.159API, qa-save-current-preview. Server task-owned14808 dihentikan setelah identitas diverifikasi; proses pengganti aktif. Semua source aplikasi cocok APK.
- Server64135 session71235 tetap hidup dengan classes1793773980290362900 (sebelum fix tick IPO). Saveqa-save-browser-flow-20261104, artificial realClock offset+3600000ms hanya di memori. Jangan restart tanpa memperhatikan anchor QA masa depan; jangan mengklaim64135 memakaiJavafinal. Aktif PM/Whale/klien/NCLD receipt tersimpan dari UI.
- Tidak ada JUnit/build berjalan; session8183/26720 terminal. Session16512 dan71235 server, bukan tes tertunda. Tidak ada ADB/install/commit/push/publish/perubahan global.

## Next action

Pulihkan observasi IAB dengan API terdokumentasi setelah kondisi akses berubah, lalu pastikan screenshot benar-benar cocok DOM4kolom, simpan dan tampilkan preview terbaru. Lanjutkan matrix chart teks200% setelah CSS akhir, gesture pointer/pan/pinch browser bila tersedia, alur tiket leverage2x/3x UI, serta pemeriksaan layout/state IPO/Riset/Firma yang belum tercakup. E03 logo baru14emiten; sisanya masih inisial dan pengunduhan sebelumnya429. Perlu kelengkapan aset sesuai lingkup, jangan mengarang provenance atau menyatakan lengkap. Jangan mengulang suite hijau tanpa perubahan/gap baru; regression/domain/APK belum membuktikan perangkat Android. Pertahankan goal active dan seluruhscope E01–E11 sampai audit akhir lengkap.
