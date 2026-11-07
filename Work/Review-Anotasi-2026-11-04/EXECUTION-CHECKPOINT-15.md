# Checkpoint 15 — audit source dan browser pulih

Goal aktif, PROGRESS. Ini mencakup turn yang terinterupsi lalu dilanjutkan. Root dan branch codex/classic-ui-new-assets terverifikasi. Tidak ada commit/push/ADB/perubahan global atau perubahan save pemain.

## Perubahan aplikasi

- navigateToStockMarketDetail sekarang membuka detail IPO untuk saham yang sudah didistribusikan tetapi belum tercatat. Pemilihan memakai canonical instrumentId, sehingga ticker sama pada bursa lain tidak tertukar. Jika caller radar memberi IDX untuk instrumen US, bursa diambil dari id. Kegagalan data kini tampil sebagai pesan. Reproduksi Node sebelum perbaikan tidak membuka tujuan, sesudah perbaikan membuktikan pending IPO, listed stock dan pemisahan bursa.
- Klik harga antrean meneruskan limitPrice ke openOrderTicket. Tiket memilih LIMIT dan nilai harga itu sebelum estimasi pertama. Sebelumnya mode MARKET dari order sebelumnya tetap terpilih sehingga harga diabaikan. Node merah lalu hijau memeriksa handler delegated membuka sekali, nilai tepat, guard disabled serta entry biasa tidak dipaksa mengubah mode.
- getFundamentalRadar hanya mengisi items untuk gapRate positif setelah menghitung jumlah saham eligible. Teaser Basic tetap dua saham diskon. Source sebelumnya memasukkan semua emiten sehingga label radar diskon dan angka tidak selaras. OwnerAnnotationPolicyTest menambah kasus nol saham diskon lalu satu saham murah. Reproduksi merah 1793794650560790400/test.log. UI memakai formatMoney USD/IDR sesuai id dan shared issuer logo. Node merah label USD lalu hijau.
- openInstrumentDetail menunda pemuatan bila clockSyncRunning dan membuka ulang hanya jika panel masih terhubung. Browser awal memperlihatkan Antrean gagal dimuat/Data sedang diselaraskan yang bertahan meski sync selesai. Node mereproduksi jalur tersebut, guard mengikuti pola IPO existing. Browser setelah fix menampilkan book dan berita.
- personalAccount snapshot menambahkan loanDebt dan loanDebtRaw dari margin IDR/USD serta kewajiban pinjaman tenor. Beranda memakai angka snapshot itu. Browser sebelumnya memperlihatkan Rp0 meski fasilitas risiko menunjukkan margin100juta. Tes merah missing loanDebtRaw pada 1793795176774037100. Kasus baru memeriksa 100juta→90juta sesudah repayment dan reconcile. Browser akhir menunjukkan90juta sesudah reload.

## Logo

Metadata exact batch5 dan search batch6/7/8 disimpan dalam Work/Review-Anotasi-2026-11-04/logo-batch*.json. Judul/deskripsi diperiksa sebelum memakai aset. BRI/BTN/Visa lama ditolak, kandidat salah perusahaan tidak dipakai. Ditambahkan AMZN,NKE,MCD,MRK,BBTN,TLKM,V,BBRI,EXCL,JPFA,HD,UNVR,PGAS,BBHI. MAPI gagal pemeriksaan SVG dan tidak dibundel. Kini57SVG resmi +5logo referensi =62emiten. Simbol otomatis masih menangani sisanya dan IPO baru. Ini belum kelengkapan merek resmi semua saham. Semua file baru tetap menjalani XML/active-content/resource checks, hashes dan map checks.

## Bukti host dan APK

- 25tes owner/research lulus sebelum homeDebt, 1793794702458252500/test.log.
- 36tes owner/margin/research/loan accounting akhir lulus, 1793795209741438100/test.log,6,516detik. Source Java akhir termasuk seluruh perbaikan. Run106tes checkpoint14 adalah bukti snapshot sebelumnya, bukan run106tes ulang terhadap snapshot15.
- Keempat check-*.cjs, node syntax dan git diff check lulus. Tes diperluas untuk perilaku yang direproduksi, tidak menghapus assertions lama. Check-research memeriksa USD intrinsic value; check-market memeriksa navigation, book-limit dan sync defer.
- gradlew.bat --offline :app:assembleDebug exit0,3detik. Semua465aset www yang dibundel identik dengan source. Tujuh file preview memang dikecualikan build existing. Hash/API/Java/asset proof di Artifacts/Annotation-Execution/browser-final-20261104/source-apk-parity-15.json.
- APK SHA256351ad7c5c5fb21e4f2f7ed57994f6e14adf917805f794eb703f91926a98b688c. Player SHA tetap59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed.

## Browser — akses pulih

Setelah pemilik menjawab sudah diaktifkan, percobaan pertama masih disabled. Sesudah pause/resume, getTab64224 melaporkan tab hilang, createBrowserTab IAB berhasil membuat tab4. Ini perubahan state akses nyata. Tab5 akun QA risiko dibuat terpisah. Tidak memakai browser alternatif.

- Utama64224 sekarang classes1793795209741438100, save qa-save-staged-ipo-01,session18201. Margin64225 classes sama, save qa-save-final-margin-20261104,session79279. Hanya proses task-owned yang command line-nya cocok persis dihentikan/restart. Server lama64134–64137 bukan source akhir.
- Margin account memakai seedMarginCall fixture sintetis, bukan bukti progres karier. Peringatan nyata di Beranda membuka Fasilitas margin & risiko. Timer120→119menit diamati. Kas25juta dari klaim sponsor game menaikkan equity35→60juta dan warning hilang sesudah sync. Pelunasan10juta melalui UI menurunkan debt100→90juta dan rasio44,44% sehat. Data bertahan setelah server restart/reload. screenshot margin-call-399, margin-recovered-399, margin-repaid-399, home-debt-399.
- Screenshot awal setelah resize sempat terskala stale. margin-call-399 ditimpa hanya setelah DOM width399 dan screenshot baru jelas cocok. Gambar ini bukan bukti perangkat.
- IPO NCLD hari22 didistribusikan, tanggal21allot/22distribution/23listing tampil jelas. UpcomingAGRO bisa dipantau. PastPORT menampilkan aksi Lihat saham yang membuka detailPORT. SVG innerHTML PORT pada quote market dan kartu IPO identik. Screenshot ipo-ncld,ipo-port-listed,ipo-upcoming-watch.
- Di QA margin hari19, ongoing NCLD dapat dipesan10lot via UI, reservasi500ribu tampil pada Pesanan IPO Saya, lalu Detail Pesanan e-IPO memuat ID,lot,harga,refund dan timeline nyata. Screenshot ipo-order-399.
- DetailBBCA berhasil memuat book/berita sesudah guard sync. Market→Close ticket→klikask10350 menghasilkan LIMIT dan estimasi kas1.036.553 untuk100saham, fee1.553. screenshot orderbook-limit-399.
- Indeks LQ45 membuka anggota dengan logo shared rows. screenshot index-lq45-399. Pengujian membership domain masih pada bukti suite/cek sebelumnya.
- Riset Basic menunjukkan13saham di bawah wajar dari35terbuka dan dua teaser diskon. Tidak mengklaim UI Pro/Whale atau US seluruh state telah diperiksa ulang.
- Firma pada akun rankCIO fixture menampilkan AUM→klien→fee. Mandat01 diterima lewat UI dan AUM berubah100juta/1klien aktif. Screenshot firm-aum-399 saat awal0. Bukti kenaikan DOM, bukan screenshot0 sebagai bukti100juta.
- Empat ukuran320×568,360×640,399×615,430×932 untuk detailIPO memiliki documentWidth sama dengan viewport, tanpa overflow horizontal. Data ipo-responsive.json. Matriks ini terbatas detailIPO; tidak meluluskan semua layar/text200.
- Tab4 markDeliverable dan tab5 markHandoff. Viewport reset sesudah pengujian. API press pada role dialog pernah timeout karena div tidak focusable; memakai tombol aksesibel Tutup tiket order berhasil. Tidak melonggarkan verifier karena tool error.

## Remaining / next_action

E03 merek resmi baru62/100base; logo otomatis mencakup semua ticker. Sisa kandidat sumber perlu identitas/versi/hak ditinjau. E04 pinch fisik Android belum ada. Visual/text200/state matriks akhir belum seluruhnya. Riset renewal/expiry dan Firma fee lintas hari memiliki bukti domain lama/akhir relevan, belum seluruh alur UI akhir. E11 Android runtime/perangkat masih NEEDS_VALIDATION. Temuan portfolio QA menampilkan position marketValue150juta dari cached Position meski risk mark135juta; perlu telusuri shared valuation agar snapshot/portfolio tidak tidak selaras. Jangan mengklaim seluruh plan complete. Browser kini berfungsi, lanjutkan task independen dan sisa verifikasi melalui tab4/5, tanpa memulihkan blocker lama.
