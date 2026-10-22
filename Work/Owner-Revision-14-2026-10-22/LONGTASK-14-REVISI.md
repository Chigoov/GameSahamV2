# Long task 14 revisi Game Saham — keputusan pemilik 22 Oktober 2026

Status paket: **READY_FOR_AGENT_SETUP**. Status implementasi seluruh paket: **NOT_STARTED_BY_THIS_PACKAGE**. Pemilik sudah menjawab tepat 15pertanyaan; jawaban verbatim dan interpretasinya disimpan di [DECISIONS.json](DECISIONS.json). Dokumen ini adalah spesifikasi pekerjaan agent berikutnya, bukan laporan fitur selesai.

## 1. Mandat dan hasil akhir

Kerjakan seluruh R01–R14 beserta penutupan F01–F05 dari [review](REVIEW-AND-INTAKE.md). Pertahankan trading, karier, bisnis, mandat klien, obligasi, replay dan fitur lama yang tidak dicabut. Lima tab utama tetap Beranda/Pasar/Portofolio/Bisnis/Lainnya. Hasil yang dituju: game2Dportrait otomatis, UI investasi ringkas, 150 koleksi/12 kategori, 24 reksa dana, ekonomi agresif yang tetap dapat direkonsiliasi, dan APK kandidat yang identik source tervalidasi.

Root satu-satunya yang boleh ditulis: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`. Saat paket disusun branch `codex/owner-audit-portrait-v8`, HEAD `49bbafb` dan banyak perubahan lokal belum di-commit. Verifikasi ulang sebelum bekerja; jangan checkout/reset/clean atau menimpa perubahan agent lain. Repo lama Game Trading Saham hanya referensi dokumen.

Produksi tetap Java + Android Web View + HTML/CSS/JS lokal. Java satu pemilik waktu, uang, harga, order, fundamental, berita, event, katalog, ledger dan save. JS hanya presentasi/input/interpolasi tampilan. Pakai source/helper/test/native API existing dahulu. Tanpa engine, frontend framework, backend, AI berbayar, cloudpush, scheduler Hermes atau dependency baru yang tidak diperlukan.

Tugas persiapan ini tidak menjalankan implementasi. Agent yang menerima prompt setelah disetup pemilik mendapat mandat perubahan repo/test/browser/build/commit lokal terarah sesuai scope existing. Tidak ada push/publish, pembayaran, perubahan profil/provider/credentials/global Hermes, atau akses repo/data lain. ADB/emulator/HP belum diotorisasi ulang; jangan mengarang bukti native. Penambahan penjadwalan notifikasi **di aplikasi Android ini** diizinkan sebagai fitur produk, bukan scheduler global komputer.

## 2. Keputusan yang mengikat

- Foreground otomatis1 hari game=15 menit nyata (96x); inactive1 hari game=1 jam nyata (24x), mengabaikan multiplier foreground. Tidak ada UIplay/pause/speed/Skip Day/Atur Sesi Pasar. Kalender dan jam bursa tetap berjalan oleh sistem, bukan bursa buka24 jam.
- Profil risiko mengikuti perilaku pemain. Tidak ada kuis wajib/pilihan label di awal; tampil sejak pertama main sebagai belum cukup aktivitas dan jelaskan cara membentuknya.
- Misi: tab Harian/Mingguan/Karier. Profil: ringkasan dengan tab Kinerja/Keuangan/Pencapaian. Tema/token existing dipakai konsisten, informasi bertingkat, bukan tumpukan kartu besar.
- IPO: identitas emiten asli dan sesekali fiktif. Jangan beri badge/tulisan “simulasi” berulang di kartu/IPO. Beri konteks terpusat sekali saat pengenalan dan dapat diakses di Tentang: ini game uang virtual, memuat perusahaan nyata dan rekaan, serta peristiwa/angka berjalan dalam dunia game. Tidak boleh mengklaim IPO/fundamental sebagai pengumuman/data resmi dunia nyata. Metadata asal identitas tetap tersedia di detail informasi, bukan banner mengganggu.
- Fundamental seluruh emiten adalah model ekonomi game yang konsisten. Identitas perusahaan nyata harus sesuai sumber identitas; sumber angka finansial adalah model game dan periode/as-of, bukan URL laporan yang tidak dipakai. Data replay nyata yang sudah ada tetap terpisah dan tidak dipalsukan.
- Notifikasi IPO: inbox+banner dalam game dan notifikasi HP opsional. Penolakan izin HP tidak menghilangkan inbox atau membatasi gameplay.
- Multi-aset: komoditas/aset digital spot, reksa dana unit/NAB, deposito tenor/bunga/penalti. Obligasi existing dipertahankan.
- News: countdown jadwal, waktu berlalu sesudah rilis, clue sektor/emiten, arah/rentang/horizon/keyakinan. Angka estimasi adalah probabilistik, bukan hasil harga yang dipaksa.
- Target katalog total 150 koleksi (12 existing+138 baru) dalam 12 kategori dan 5rarity; total 24 reksa dana (3 existing+21 baru). CSV paket adalah daftar kerja konkret, bukan daftaropsional.
- Pasar lebih agresif dan menantang. Harga tetap berasal dari fill yang sah pada internal market; saham tanpa fill tidak boleh diberi harga transaksi palsu.

## 3. Preflight dan perbaikan wajib F01–F05

Simpan baseline Git/diff, source hashes, toolchain, versi APK, daftar suite dan hash save pribadi. Cari lokasi save hanya melalui checkpoint yang relevan; jangan broadscan data pribadi. Gunakan run terpisah `Artifacts/Owner-Revision-14-2026-10-22/run-NN/` dan `qa-save` terisolasi, port kosong milik run, tanpa reset/mutasi preview yang sedang dibuka pemilik. Bukti review lama ada di `Artifacts/Owner-Review-2026-10-22/`; pertahankan utuh.

| Temuan | Perbaikan minimal yang benar | Bukti wajib |
|---|---|---|
| F01 backup rusak | Kembalikan validasi struktur/checksum/versi primary sebelum overwrite backup; preserve last-good; rotasi/write gagal tidak diabaikan; pertahankan durability yang diperlukan. | Primary rusak6 byte/>10 byte/truncated, backup masihvalid; recovery lanjut save lalu corrupt lagi masihrecoverable; copy/write gagal; interrupted write; tidak mengakui save yang hilang. |
| F02 min Android | Ganti `InputStream.readAllBytes()` pada `getCompanyProfile` dengan reader kompatibel API26 dan try-with-resources. Jangan menaikkan min Sdk untuk menghindari masalah. | Kompilasi, lint API, contractbridge; API26–32 native test bila diotorisasi/tersedia. Catch Exception bukan penanganan No Such Method Error. |
| F03 false PASS | Kontrol/aksi inti wajib menghasilkan FAIL jika tidak ada. Fixture dibuat sah; tidak ada kondisi opsional yang tetap mengklaim sukses. | Negative control sengaja meniadakan tombol/fill/bisnis harus membuat gate gagal; runnormal BUY/SELL/fill/posisi/kas/ledger/bisnis/koleksi/save-load benar-benar terjadi. |
| F04 offline lambat | Diagnosis scanledger/evaluasikarier/valuasi berulang; perbaiki sharedpath; catch-up tidak memblokir main UI. Tidak memotong periode atau settlement yang wajib. | Offline1 jam/1 hari/7 hari/30 hari nyata; 30 hari=720 hari game. Feedback<=300 ms, UI responsif; rekonsiliasi, progress/cancel-interruption aman, restart tidak double settlement. Catat durasi selesai aktual. |
| F05 watchlist | Tombol>=48x48CSSpx pada baseline Web View terkalibrasi; rowkeydown tidak mengambil Enter/Space tombol anak; aria-pressed/label/status benar. | Pointer/Enter/Space mengubah watchlist saja; klikbaris membuka detail; fokus, font200%, seluruh viewport. |

Run existing yang lulus36 tes tidak menutup F01: test lama memakai string BROKEN6 byte sehingga melewati syarat>10. Bukti native/build/host/source adalah gerbang terpisah. Jangan memakai laporan lama462/70 tes untuk source yang berubah.

## 4. R01 — countdown hibah dan kontrak waktu bersama

**Sumber:** facade `getDailySponsorStatus`, `claimDailySponsor`, bridge dan render Beranda.

- Pisahkan real clock WIB untuk hibah dari game clock untuk seluruh event ekonomi. Gunakan `nextClaimAt`/eligibility domain sebagai kontrak; bukan membandingkan tanggal game.
- Sebelum klaim: CTA `Klaim Hibah Rp25Juta`. Saat proses: disabled/Memproses. Berhasil: disabled `Klaim berikutnya 12:34:56`, subteks `Tersedia pukul00.00WIB`. Retry request sama tidak menggandakan kas.
- Hitung countdown dari absolute deadline dan anchorreal; jangan mengurangi counter1setiapcallback karena backgroundtimer terlambat. Pada resume minta status baru. Jam perangkat mundur mengikuti proteksi domain existing, jangan membuka eligibility dari UI.
- Tepat nol: verifikasi domain, CTAaktif bila eligible; jangan kredit otomatis. Angka tidak negatif; dua entrypoint Beranda/shortcut selalu sama. Screenreader tidak diumumkan setiap detik; status penting saja.
- **AC-R01:**23:59:59→00:00:00 WIB, foreground/background/resume/reload, offlineclock rollback, doubletap/retry, startnew/legacy-save, dua entrypoint, klaim tetap1/tanggalnyata. Perubahan hari game tidak membuka hibah.

## 5. R02 — game otomatis dan jam mengalir

**Sumber:** `GameClock`, facade `advanceRealMs`/`advanceWorldSeconds`/lifecycle, `MainActivity`, polling UI dan semua caller handler Atur Waktu.

- Sesudah game siap dimainkan, clock berjalan otomatis tanpa tombol Mulai. Hapusentrypoint Atur Waktu/Atur Sesi, play/pause, 2x/5x/Skip Day dari UI/tutorial/shortcut, termasuk deep-linklama. Helpers domain untuk tests/lifecycle boleh tetapinternal.
- Aturan sesi IDX/US existing tetap menentukan kapan order/fill sah. Otomatis berarti sistem membuka/menutup sesi sesuai kalender; tidak berarti bursa selalu buka. Tidak ada clock pasar kedua pada JS.
- Gunakan monotonic elapsed untuk foreground, remainderinteger untuk pecahanms dan persistedwall anchor untuk inactive.900000 msreal→86400000 msgame;300000 ms→28800000 ms;3600000 msinactive→86400000 ms. Delaycallback tidak mengurangi/menggandakan elapsed.
- Publish anchor `{gameTimestamp, observedMonotonicAt, rate, lifecycle, revision}` dari domain. UIinterpolasi sebatas presentasi dari anchor dan menjadwalkan renderhalus; koreksi drift saat snapshot, visibility/resume. Displayutama HH:mm dan progressmenit; detikboleh di detail bila masihterbaca. Pada96x, tidak setiap detikgame bisa terlihat di60fps; jangan menjanjikan hal itu. Hindari loncatan96 detik sekaligus setiap poll1 detik.
- Saatdomain error/stale, tampilkan status recovery dan jangan terus memajukan jamvisual seolah ekonomi berhasil. Orderpreview punya kedaluwarsa/validasirequote jika pasar berubah; modal baca/order tidak mem-pause pasar.
- Migrasi sekali legacy-paused/speed2x/5x menjadi automatic1x dengan versiaturan+backup; tidak menilai ulang elapsedlama menggunakan rasio baru. Jelaskan migrasi pada catatan teknis, preserve posisi/kas/progres.
- Catch-up F04: satu writer domain, proses dalam unit/event boundary yang bisa di-checkpoint, UI tidak diblokir; hindari menciptakan worker yang menulis savebersamaan. Uang/payroll/misi/kupon/IPOmaturity diproses tepatsekali, termasuk saat proses mati dan resume.
- **AC-R02:** elapsedpecahan/jitter/delay, date/month/leapyear, USovernight, 30 hariinactive, automaticnew/legacy/resume, nobuttonmanual, no JSfinancialclock, determinism state sebelum/sesudah save, UI tidak backward selama clock valid. Benchmark feedback/catch-up nyata.

## 6. R03 — profil risiko sejak awal, mengikuti gaya pemain

**Sumber:** `InvestorArchetypeService`, `PlayerProfileManager`, facadeprofile dan UIonboarding/home/profil e. Source sudah mempunyai klasifikasi exposure/leverage/diversifikasi/holding/turnover; reuse.

- Pada pertama main tampilkan kartu kecil `Gaya investasimu sedang terbentuk`, limaindikator yang akan diamati, dan `Belum cukup aktivitas`. Tanpa memaksa kuis atau meminta pemain memilih Konservatif/Agresif. Identitasnama opsional dapat diisi tanpa memengaruhi risiko.
- Pertahankan baseline minimum5fill milikpemain dan 7 hari game untuk kesimpulan awal; perlihatkan progresdata (`3/5transaksi;4/7hari`), bukan label risiko palsu. Setelahcukup data gunakan alasan/scoreyangdapatdijelaskan dari serviceexisting.
- Hitung hanya akunpribadi, dedupfill, ignorefutureevents, FIFOholding bilahistoriada. Mandat/NPC/transfer/hibah bukan tradingstyle. Asetbaru masuk eksposur: deposito/kas, obligasi/fundlookthrough, komoditas/digital sesuai kebijakan transparan; unknownunderlying menurunkan confidence, bukan asumsiaman.
- Tampilkan label, alasan2–3 poin, periode/as-of, confidence dan perubahan dibandingperiode sebelumnya. Update saat fill/valuasi/periodereview yang relevan; tidak berkedipsetiaprender. Stabilkanpergantian label dengan jendela/hysteresis sederhana bila perlu dan uji, bukan MLbaru.
- Profil memberi insight/rekomendasi; tidak otomatis mengunci trading/karier atau mengubah saldo. Preferensi aksesibilitas bukan faktor risiko.
- **AC-R03:** day0/underthreshold, konservatif/seimbang/agresif dengan fixtures meaningful, changingstyle, mixedasset, legacyhistoryincomplete, clientisolation, restart/retry, evidence alasan cocokdata.

## 7. R04 — pusat misi Harian/Mingguan/Karier

- Satu entrypoint shared dari Beranda/Lainnya/shortcut. Header ringkas dengan progresagregat; tigatab Harian/Mingguan/Karier. Baris misi: judul, deskripsi1–2 baris, progresnumerik+bar, hadiah, CTAklaim bilaeligible.
- Urutan dapatdiklaim→hampir selesai→lainnya. Jangan menyembunyikan syarat; detaildibuka untuk petunjuk. Stateaktif/terkunci/selesai/sudah diklaim/expired/empty/loading/error jelas; tidak hanya warna.
- Countdown Harian menuju resetkalender game, Mingguan menuju boundaryminggugame yang didefinisikan; Karier tidak diberi timerpalsu. Semua entrypointmenampilkan objekmisi yang sama. Minggulewat/boot/catchup tidak menghapusreward yangmasih berhak menurut kebijakan tersimpan.
- Claim validasidomain, disableinflight, stableidrequest, updatekas/XP/rep/progres tanpa reloadtab; jadwalnotifs opsional bukan syaratreward. Definisikan expiredunclaimed agar tidak merugikan karena appditutup; proposal: rewardcompletedtersimpan dapatdiklaim setelah periodelewat, targetbaru tetapterbit.
- **AC-R04:** tiapstate/tab, resetmidnight/minggu/offline, tindakanlintasproduk, klaimulang/tapcepat, filter/fokusscroll, font200% dan 7 viewport.

## 8. R05 — batas aman layar HP

- Tangani `systemBars`, displaycutout dan IME pada nativecontainer dengan APIyang sesuaimin Sdk26. Pelihara jalur26–27/28–29/30+; target36harus memperhitungkanedge-to-edge. Jangan hardcode notch24/32 px atau menebak HPpemilik.
- Pilihsatu pemilik paddinginset: nativecontaineraman dengan CSSsisa yang diperlukan, atau koordinasi eksplisit melalui CSSvariable. Jangan menambahkan nativepadding dan envsafearea dua kali. Ukur bounds elemen pertama terhadap area aman.
- Header, badge, toast, dialog/fullscreenflow, rootnav, order CTA dan keyboardform semua mengikuti areaaman; backgroundbolehmengisi layar, teks/tombol tidak masuk status bar/notch. Portrait dengan kameratengah/kiri, status barbesar, gesturenav/3 buttonnav, teksbesar, keyboardopen/close.
- Clock dan tombol header tetapterbaca; tidak dipotong dengan overflowhidden supaya tesgeometri lulus. Jangan menyembunyikan status bar untuk menutupi bug.
- **AC-R05:** hostsimulatedinsets untuk regresi; native API26/28/30/33/35–36 bila lingkungandiotorisasi. Sertakan screenshot/boundsdanversi Web View/device. Tanpa nativeevidence status R05=`IMPLEMENTED_HOST_VERIFIED_NATIVE_PENDING`, bukan full PASS.

## 9. R06 — minimal 3 IPO setiap bulan game dan notifikasi

**Sumber:** `CorporateActionService`, `IpoOffering`, `IpoSubscription`, catalog/market/ledger, inboxdan nativebridge.

- Buat scheduler kalender domain untuk minimal 3penawaran IPO berbeda yang benar-benar membuka pemesanan dalam setiap bulan game; daftar event3 baris kosong tidak cukup. Defaultbulanpenuh: tanggal6/15/24, pengumuman H-2 hari, bookbuilding2 hari game lalu penjatahan/listing. Ini parameterdesainawal agent, bukan jadwal dunia nyata.
- Bootstrapgame/migrasi pertengahanbulan menempatkan3 slot valid pada sisa waktubulan; bila tersisa<3 hari gunakan slotintraday yangmasukakal, bukan event dimasalalu. Jika waktu tersisa sangat pendek, tiga penawaran boleh membuka bersamaan dan masa pemesanan tetap berlangsung sampai bulan berikutnya; jangan memaksa pemain bertransaksi dalam satu detik nyata. Hitung per bulan pembukaan penawaran, bukan tanggal listing. Simpan jadwal, issuer ID, stagerun ID, processedmarkers; leapyear/28/29/30/31 hari dan offlinelewatbulan wajib tercakup.
- Target komposisiawal2identitas nyata+1 rekaan/bulan selama cadangan nyata eligible ada. Identitasnyata: cadangantersumber, belum aktif di katalog game; IPOemiten yang sudah listed bukan IPOkedua. Bila cadangan habis, bangkitkanrekaan deterministikunik untuk memenuhi3/bulan dan catatalasan. Tidak membuat saham nyata barudengan tickeryangtabrakan.
- Minimalrealidentity: nama, ticker, bursa, sektor, sumber identitas; fundamental/harga/penawaran tetapmodel game. UIdesain tanpa badge simulasi berulang sesuai pemilik; informasi umum konteks game tersedia tanpa berpura-pura harga/IPOresmi.
- Tahapannormal diumumkan→pemesanan→ditutup→penjatahan/refund→listing/failed. Pesanan danareserved, oversubscriptionpro-rata/aturantersimpan, roundingunit, canceldeadline, fee, refundutuhsisa dan sharesissued konsisten. Kasemisi masukpenerbit; saham listingmasukkatalog, quote, indekseligible, profil e, news, watchlist/portfolio tanpa refreshpenuh.
- Inbox+banner untuk pengumuman, pemesananpenting, penjatahan/listing. Uniquekeyrun Id/event Id/stage/channel; reload/resume/receiverulang tidak spam. Offlinebacklog diringkas, detailtetapdiinbox. Deep-linkke IPOyang tepat atau riwayatjikaexpired.
- Android: channel API26+, izin POST_NOTIFICATIONS API33+ pada konteks usermengaktifkan; denial/revocation tetapinbox. Gunakan native scheduling lokal yang tepat kebutuhan tanpa server/FCM/dependencybaru. Jadwal realdeadline dihitungdari game anchor+rateinactive24x ketika appditutup; reschedulepada foreground/resume/resetpermainan/reboot/time change bila didukung. Receiver tidak menjalankan mesinekonomi kedua atau mengklaimsettlementbelumterjadi. Android Doze/force-stopdapatmenunda; jangan menjanjikan notifikasitepatdetik tanpa bukti. Prefer tanpa exact-alarmpermission tambahan.
- **AC-R06:**>=3 open IPO/masing-masing12 bulan, fresh game akhirbulan, 30 hariinactive, realreserveexhaustion, duplicate IDs, no doubleallot/refund, insufficientcash, beragamukuranorder, Androidpermissionstates/deep-link/dedup, hidupmatiproses. Nativepending dilaporkan terpisah.

## 10. R07 — persentase harian seluruh emiten

- Trace setiap100emitenexisting+IPO baru: sumberprice, lastfilltime, previousclose, sessiondate, exchange, currency, tradability, halt, NPCbudget, bridgepayload, DOM. Jangan menambal10ticker yangkelihatan saja.
- Formula `(lastTradedPrice - previousSessionClose) / previousSessionClose * 100`. Gunakan precisiondesimal cukup, bulatkan terakhir untuk display; harga acuan invalid→N/A dengan alasan, bukan0palsu. Perubahanpersen kecil yangterbulat0.00 boleh diberi detaillebihpresisi.
- Simpan previousclose saat sesi bursa masing-masingbenar-benarselesai, termasuk USovernight; jangan reset00:00 saat USmasihbuka. Audit caller `startNewTradingDay` facade+Career Manager untuk mencegah resetganda. Save/load tidak mengubahbaseline.
- Bid/ask/NPCorder tidak otomatis mengubah last trade. Tidakadafill→last trade/stamptetap; UI jelaskanbelumadatransaksi/tutup/halt, dan data freshness terpisah dari perubahan0%. Orderbook last trade dan fundamental marketcap harus satuprice.
- NPCtakers mencakup seluruh universeeligible dengan budget, inventori, lot, tick, sessionvalid; agresiftidak berarti setiap emiten harus bergeraksetiapdetik. Testcoverageinstrumen100% bukan menuntut100%emiten naik/turun bersamaan.
- UIdiffupdate harga/%/volume/freshness, indexdanringkasan secara in-place; filter/pencarian/scroll/fokus/watchlist/order input tidak reset. Perubahan status emiten baru/lostsymbol gracefully.
- **AC-R07:** positive/negative/zero/sub0.01/previouscloseinvalid, IDX/USsessionboundary, roundtrip, all100+IPO, stalevsnofill, haltduringnews, filterlists+detail+home agree. Assertpercentdari domainformula untuk seluruhuniverse, bukan regexnilai0.

## 11. R08 — fundamental emiten lengkap dan konsisten

**Data inti wajib:** identitas/bursa/sektor/mata uang; saham diterbitkan/beredar/treasury/freefloat; marketcap; jumlah pemegang saham internal dan strukturbigowners; pendapatan/labaoperasi/lababersih; aset/liabilitas/ekuitas; CFO/CFI/CFF/cash; EPS/PER/PBV/ROE/ROA/DER/margin; dividen/yield/payout; period type/as-of/published At/source/provenance.

- Reuse shares Outstanding/current Price/marketcapexisting. Marketcap=harga*jumlahsaham beredar, bukan diketikmanual. Freefloat<=beredar; jumlahholders bukan jumlah lembar. Definisikan pemilik benefisial perentitas agar akun NPCganda tidak menggandakan jumlah orang; pemegang nihil tidak dihitung.
- Seluruhangka dibuat seeded/deterministik sebagai keadaan perusahaan dalamgame. Boleh seedlaporansebelumstart sebagai **laporan model game**, bukan performatradinghistoris yang tidak pernahterjadi. Pisahkan `identitySource` (bilaidentitas nyata) dan `financialSource=GAME_ECONOMY_MODEL`, version/as Of. Jangan mengisikan nomor izin/sumberlaporanresmi palsu.
- Neracaaset=liabilitas+ekuitas; endingcash=startingcash+CFO+CFI+CFF; labaditahan/netincome/dividen konsisten. Pembayaran IPO/rights/dividen/split/buyback mengubahstateperusahaanyang relevan tepatsekali. Transaksisahamsekunder tidak langsung menambahpendapatanperusahaan.
- Rasiofinancial disusun dari periode yang sama: EPS dengan weightedsharesdilusi, PERhanyaearningspositif, PBVjikaekuitaspositif; ROE/ROApakaiaverageequity/assets bilaada, DERdefinisiliabilitasatauinterestbearingdebt jelas. Laba/ekuitasnegatif/riwayat tidak cukup→N/A+alasan; tidak dividezero atau ratioabsolutmenyesatkan. TTMhanya jika4kuartal sudah tersedia; annualized diberilabel.
- Timeline laporan kuartal/annual mengikuti game clock dan waktu publikasi; data futurehidden. News corporateimpact masukforecast/laporan saatwaktunya, bukan merombak historis yang sudah dipublikasi. Corporateactions punya event records untuk audit.
- UI4subtabdi detail: Ringkasan/Valuasi/Keuangan/Pemegang Saham; metricscorediatas lalu expandabletables/tren. Sumbermodel/periode diperlihatkan ringan di detailinfo, tanpa badge simulasi berulang. Format IDR/USD/lembar/orang/% jelas.
- **AC-R08:**100 emiten+new IPO seluruh required fields; balancesheet/cashflow/ratioasserts; owncounttransfer/exit/duplicatebeneficialowner;split/rights/IPO/dividend;missingnegativevalues;futureleak;repeat load identical; profil e API26fix F02.

## 12. R09 — istilah emiten pada konteks saham

- Audit visibletext pada root/detail/order/receipt/riwayat/filter/error/tooltip/aria/tutorial/news/factsheet/career. Ganti “instrumen” menjadi “emiten” untuk saham/perusahaan: Cari Emiten, Daftar Emiten, Detail Emiten.
- Jangan mengganti field `instrumentId`, class/method/savekey/enum/URLinternal hanya untukteks. Migrasikeytakperlu meningkatkanrisiko.
- Reksa Dana=produk/dana, Komoditas=komoditas, Aset Digital=aset, Deposito=deposito, Obligasi=obligasi/penerbit. “Instrumen” yangmerujukgabunganproduk jangan dipaksamenjadiemiten; pakai “Produk Investasi”.
- **AC-R09:** auditvisible DOM lintasscreen/state, status/error s/highcontrast/font200%, no domain IDschema changes; jangan testgreprawsource yangmengharuskan semua `Instrument` hilang.

## 13. R10 — produk selain saham, utuh sampai settlement

Navigasi di Pasar: pemilihjenis Saham/Reksa Dana/Komoditas/Aset Digital/Deposito/Obligasi, atau daftar pintasnativeexisting yangringkas; satuentrypoint ke produkshared dari Portofolio. Jenisproduk jelas; total/kas/risiko memberi valuasilintasproduk dengan FXcanonical. Jangan merombak5tabutama.

| Produk | Cakupan minimum kerja | Kontrak ekonomi |
|---|---|---|
| Komoditas | Pertahankanemas; tambahkanperak, minyak, kopi sebagai unitvirtual yang jelas. | Spot, bid/ask/spread, fee, unitdanprecision, kas/inventoricounterpartyfunded; tidak claimpengirimanfisik. Harga canonical berkaitan skenariomakro/news, tidak random UI. |
| Aset Digital | BTC, ETH, SOL, ADA sebagai pasarvirtual awal; identitas umum, harga awal game terpisah darilivefeed. | Spot tanpa leveragebaru; fractionalquantities dengan Big Decimal/scaleperaset, minnotional, spread/fee, marketvenue/liquidityyangsah, 24 jam game menurut aturan produk, bid/askdeterministik; tanpa wallet/blockchain/APIbroker. |
| Deposito | Tenor7/30/90/180 hari game; tiapproduk rate fixed yang ditawarkan, estimasibunga/fee/penalti, maturity, auto-rollopsionaloff default. | Principalditransfer keissuercontractfunded, bukan uangmenghilang. Accrual rateannual*day count/365 dikurangi potongan yang didefinisikan; maturity creditprincipal+interestsekali; earlybreakreview+penaltikontrak. Tarif awal desain4/4.5/5/5.5%/tahun GAME untuk kalibrasi, bukan tarifbanknyata. Tidakmengaku perlindungan LPS/fasilitasbankresmi. |
| Obligasi | Pertahankan FR0091/kupon/penerbit/valuasi; perluasunderlying dana sesuai kebutuhan. | Jangan menggunakan helperdeprecatedcoupon yangthrow Unsupported Operation Exception; reuse Multi Asset Trading Service canonicalsettlement. |
| Reksa Dana | Unit/NAB, cutoff, poolaccounts, subscription/redemption/cancel; rincian R14. | Bukan spotorderbook/return flat; valuasi underlyingdanunitdilution benar. |

- Reviewtransaksi selalu menunjukkan akun, matauang, quantityunit, fee/spread, kas before/after, tenor/risiko maturity. Requestidstable, rejectinvalidnegative/Na N/precision/overbudget, no doubledebit.
- Posisi tiapproduk tampildi Portofolio, statistik dan profil risiko; reksa danatidakdihitungdua kali underlying, pinjaman/deposito jangan salingsalahlabel. Uangklienisolated dan entitlement sesuai domain existing.
- **AC-R10:**buy/sell/fractionalrounding/insufficientcash/retry/cancel/failure; maturity lewatbeberapahari/month/offline; earlybreak/autoroll/restore; seluruh saldo+assetissuer+ledgerreconcile aftereveryaction; no client/personalmix.

## 14. R11 — profil dan statistik yang enak dibaca

- Header: nama/avatarinisial, pangkat, level, gayaactual, networthpribadi; tidak menumpuk20 angka dihalaman awal. CTAeditidentitas/preferensiterpisahdari analitik. Tab Kinerja/Keuangan/Pencapaian konsistenback/fokus.
- Kinerja: real/unrealized P&L, return1/7/30/90 hari game/sinceinception sesuaihistori, benchmark periodesejajar, drawdown, winrate `xmenang/ysellclosed` danavgduration. Chart dari historiobserved; belum cukupperiode→Belum Cukup Data, bukan0/fiktif.
- Keuangan: aset/kewajiban/networth, kas IDR/USD, reserved, deposito/reksa dana/saham/obligasi/komoditas/digital/koleksi/bisnis, utangmargin+tenor+short sesuai kontrak. AUMklien terpisah dan tidak ditambahkan ke net worth pribadi. Nonfinancial trophyvaluasi0.
- Returndisesuaikan cashflow; hibah/setor/tarik/reward bukan investmentprofit. Beri definisiringkas metodologi; jangan membandingkan rawnetworthgrowth dengan benchmark price return tanpa penjelasan. Dailyreturn/benchmark/datewindow/FXbasis sama.
- Pencapaian: milestones, liga, karier, galeri, 5 rarityprogress, unclaimedachievementreward; progressnumerik, syarat tercapai, lockedpreviewtanpa menyembunyikanrule. Jangan membentuk infinite XP/rep dengan jual-belibarang yang sama.
- **AC-R11:**0 fills/1 buy/nosells/profit/loss/partialcloses/breakeven; externalgrantvs P&L; FX/utangsemua; client AUMisolation;7/30/90 lookbackpartialhistory; tabstate/font200%; arbitrarybigmoneyformat tidak overflow.

## 15. R12 — news berjalan dan benar-benar berdampak

- Satu event ID dengan announced At, published At, starts At, expires At, affectedsectors/instruments, kind, magnitude Distribution, confidence, horizon, processedmarkers, runseed. Persiststate/notifications; `sentimentImpact` field existing dan textberita saja tidak meluluskanfitur.
- Future card: event publikyangbolehdiketahui, contoh `Rilis kinerja sektorenergi —01j00m lagi (waktu game)`. Selisihabsolute published At-current Game Time; pada96x 1 jam game=37.5 detik nyataforeground dan 150 detik nyatainactive. Sesudahrilis `terbit2menitgame lalu`, timerhitungkeatas. Tidak berubah ke negatif atau “besok” selamanya.
- Jadwalpublik dapatmenunjukkan prakiraan/clue; isi confidential/hasilkeuangan/trueimpactseed tetap tersembunyi sampairilis. Prediksi dari data as-of, bukan membacatrue future impact. `expectedrange` bukan perintah setlastprice.
- Setiapnews card memuatcluejenisbisnis/sector, emiten eligible, arahpositif/negatif/campuran, rentangperkiraanmisal+2–6%,horizon2 hari game dan confidence Rendah/Sedang/Tinggi yang didefinisikan. Ini contoh format, bukan nilai untuksemua berita. Jelaskan alasan, priced-in/delay/risiko reversal singkat.
- Domainrilis: shock/sinyal mengubah expectation, fundamental sector bila relevan, riskappetite/order budget/limitprice NPCmelaluijalurexisting; marketmatchingmembentuk fill/price/tape/OHLC. Jangan mutateharga langsung dari UI/news, mendouble shock saat reload, atau mengisi chart dengan harga fiktif agar bergerak.
- Agresif: kalibrasiparameter perlikuiditas/sector, misaljendela shocknews2–12%selama1–3 hari game dengan peluangreversal; basevolatilityberbedabesar/kecil. Angka ini kandidatbalancing, bukan jaminanreturn/rangeclip. Pertahankan tick/lot/halt/liquidity/riskrulesexisting; noise tidak menghabiskan seluruh kas NPC secara takterkendali.
- Berita makro (sukubunga, kurs, energi),sektor, corporateearnings, dividen/IPO, rumor/invalidation wajib variasi. Dampaklintassektor dapatberlawanan: minyaknaik membantuenergi tapi menekantransportasi; bank bereaksi bergantungmargin/kredit. Berita sama tidak mendorongsemuatickerpersissama.
- Jalankan paired seed tests dengan/withoutnews: buktikan eligible NPCorders/sentiment berubah dan agregatfill/harga beda bila likuiditas memungkinkan. Skenariotutupbursa/haltnofill harus membuktikan no boguslastprice; sinyal tetaptersimpan untuk nextsession.
- **AC-R12:**countdown/crossrelease/expiry, futureleak, replayeventidempotent, seedrestart, overlapnews, sectorpositive/negativeunaffectedcontrol, quote/detail/chart/fundamental sync, offlinecutoff multi-event, no direct JSpricewrite. Sertakaneventtrace dari asalhingga DOM.

## 16. R13 — 150 koleksi, 12 kategori, 5 rarity

Daftar lengkap150 item ada di [KATALOG-KOLEKSI-150.csv](KATALOG-KOLEKSI-150.csv). Seluruh 12 item existing dicatat dengan ID, harga dasar, pemeliharaan danrep persissource.138 itembaru punya IDunik, nama, kategori, rarity, harga game awal, biaya harian, unlock, cara perolehan sertaartbrief. Harga/ambang baru adalah **usulanbalancinguntukagent**, bukan harga dunia nyata atau hasilplaytest.

| Kategori | Totalitem |
|---|---:|
| Motor & Kendaraan Roda Dua | 12 |
| Mobil & Kendaraan Kolektor | 20 |
| Hunian & Properti Ikonik | 16 |
| Properti Komersial | 10 |
| Jam Tangan | 15 |
| Perhiasan & Permata | 15 |
| Seni Rupa & Patung | 12 |
| Barang Antik & Warisan | 10 |
| Kapal & Yacht | 10 |
| Pesawat & Helikopter | 8 |
| Teknologi & Hobi Premium | 12 |
| Trofi & Memorabilia Prestasi | 10 |
| **Total** | **150** |

- Rarity5:Umum/Tidak Umum/Langka/Epik/Legendaris. Preserve internallegacy STANDARD/RARE/LEGENDARY bila palingaman, ganti hanya label serta tambahtier; mapping STANDARD→Umum, RARE→Langka, LEGENDARY→Legendaris. Jangan meresetowned/pledge saat enum/categoriberubah. Kategori UIboleh metadata yangmaplegacykelompokbarujika itu migrationpalingkecil.
- Galerifilter kategori, rarity, owned/unowned/pledged/unlock, search dan sortprice/progress; jumlahunion150 tanpa duplikasi, pagination/lazyrendernative bila dibutuhkan bukan librarybaru. Kartucompact gambar/nama/rarity/status/harga, detailmaintenance/rep/source acquisition/action. Angkaowned count/from150 benar, bukan totalbarisrendered.
- Sebanyak10trofi didapat dari achievements: transaksi pertama;5 sektor;30 haritanpalikuidasi;mandatpertama;juaraliga Bronze/Silver/Gold;memegang6jenisproduk;100 koleksi;menyelesaikan Era Baru. IDrewardstable/run+lifetimepolicy, 0 purchaseprice, nontradable/nonpledgeable, 0 marketvalue; jangan membuattrofi dari buy0yangditolakvalidation. Gunakan rewardpath existing/extensionminimal.
- Barang koleksi beli bisa punya milestone/auctionopsional hanya bila diperlukan; jangan membangun sistemlelangbaru di luar kebutuhan150 item. Pembelian/penjualan/beban/valuasi/agunan/rep masing-masing lewat domain existing, biaya ownerssaja. Tidakada bonus rep/XPulangfarm saat buy/sell/retry.
- Seluruh 150 item mempunyai visual lokal sah atau fallbackikon/ilustrasi distinct yangrapi, altlabel dan rasio gambarseragam. Lisensi/asalassetdicatat; tanpa CDN/remoteimage atau mengunduhbrandphoto tanpa hak. Jangan menandai kelulusanvisual darifileplaceholderrusak.
- **AC-R13:**exact150/12/5, 12 legacyowned/pledgedroundtrip, filterunion, dedup, tier unlocks, purchaseinsufficient/rapidretry, sellpledgedreject, 10 achievementawardexactlyonce, maintenanceofflinereconcile, perf150items/font200%/allviewports.

## 17. R14 — 24 reksa dana dan lembar informasi investor

Daftar24 produk di [KATALOG-REKSA-DANA-24.csv](KATALOG-REKSA-DANA-24.csv):3 pasar uang, 4 pendapatan tetap, 3 campuran, 3 saham aktif, 3 indeks, 3 syariah, 3 sektor, 2 global. Tiga ID legacy dipertahankan. Tiapbaris mempunyai jenisrisiko, alokasi100%,strategy, benchmark, biaya/minimumawalusulan dan kontrakcutoff/settlement.

- Reuse `MutualFundManager`/`MutualFund`, poolcanonical, unitprecision, cutoff dan underlying. Existingenum3tipe tidak cukup untuk semuastrategi; perluas kebijakan holding/valuation yang jelas, bukan mengasumsikan setiap nonindex=fixedincome. Dana yangbernama25tapi holdings/biaya/risknyasama bukan24produkberfungsi.
- Dana campuran benar-benar memiliki saham+surat utang+cash; sektor terpilih punya eligibleholdingssektor; global memakai USunderlying+FXcanonical; indextrackingmengikutibasketmodel yang didefinisikanbukan mengakuindeks dunia nyata lengkap bila coveragebelumada. Syariah menerapkan screeningsector/rasio/contract di game (sukuk dan kontrak bagi hasil dunia game, bukan bunga deposito konvensional yang hanya diganti nama), bukan sekadar kata Amanah atau claimsertifikasi OJK/DSN yang tidak ada.
- Persist/poolledgerfunded, investorunitissuance/redemption, dividend/coupon/feeaccrual, cashbuffer/rebalance, pendingliquidity semua konsisten. AUM=netassetfund;NABperunit=(aset-liabilitas)/unitberedar, dengan bootstrapuntuk0unit yang didefinisikan. Investorbaru tidak mendapatprofitmasa lalu/fundlain.
- Katalog ringkas bisa filterkeluarga/risiko/currency/minimum, sortbyrisk/AUM/return yang tersedia. Bandingkan2–3 danapada periode yang sama. Detail4bagian Ringkasan/Kinerja/Portofolio/Biaya&Transaksi, tombol Beli/Jual, unitdimiliki dan pendingorders.
- Factsheetsetiapproduk wajib: nama/ID/inception/currency/managergame/custodiangame; tujuan/kebijakan;risiko utama+skala 1–7 game;NAB/unit, total NAB/AUM, totalunits, as Of;allocationriil/top10holdings+dollarweight;return1/7/30/90/365 hari game/sinceinception bila observedhistorycukup;benchmark periode;maxdrawdown/volatilitasbila sampel cukup;fee management/custody/entry/exit dan basis/tahunan;minimumbeli/tambah/jual;cutoff/jam/hari/settlement/cancel/liquidityrisk;dokumenringkasanproduk dari data yang sama dengan UI.
- Jangan membuat graph3tahun/past return untuk fund baru yang belum lived3tahun. Gunakan N/A+tanggalpeluncuran. NAVhistorybaruappendobservedcutoff, backtestlabterpisah danberlabel; tidak masukreportedactualreturn.
- Rate/fee CSV adalah tuningawal (basispoints/tahun GAME), bukan tarifproduk nyata. Existingcontractsowned/pendingtidakboleh direprice diam-diam; grandfatheratau migrasiversioning yang jelas. Takada garantibunga reksa dana; deposito ratecontract berbeda dariexpectedfundreturn.
- **AC-R14:**24 distinct/8 keluarga/3 legacy, allocation100, holdingsmatchpolicy, feesfrom NAVonce, subscription/cancel/redemptionpartialliquidity/retry, marketnewsimpactunderlying→NAV, FX, existingunitmigration, compareperiods, N/Ahistory, cutoff clock auto, allfactsheetfields UI/APIconsistent.

## 18. Urutan eksekusi dan dependency

Trackerterpisah: [TRACKER.csv](TRACKER.csv), jangan menimpa status longtasklama hanya agar tampakselesai.

1. P00 preflight+fingerprint+tes baseline fresh; tutup F01/F02/F03 dan regression palingkritis. F05masukshared UI; F04haruslulus sebelum kalenderotomatis dianggap selesai.
2. R02waktu/F04→R01hibah, R05safearea/F05, R07quotebaseline danregimagresifawal. Bolehdesain UIindependen, tidak meluluskan dependencyyanggagal.
3. R03profilperilaku, R04misi, R09terminologi, R11strukturprofil; analitikfinalmenungguaset/fundseconomyterintegrasi.
4. R08fundamental→R12news agresif→R06IPOdaninbox/nativecontracts. Ujipenawaranlongcalendar sebelum generateeventbesar.
5. R10spot/deposito→R14fundcatalog; R13 koleksi dapatdikerjakanindependen sesudah save/migrasi aman. Integrasikan kembali R03/R04/R11 untuk new assets.
6. INT regresi lintas14 revisi, negative controlgate, 10x100 meaningfulactions, UIstate/viewportmatrix, performance, independentreview, APKbuild+inspection+handoff.

Multi-agent sesuai kemampuan yang benar-benar tersedia: koordinator menetapkan ownership file/contract, reviewerberbeda dariimplementer bila ada. Satu writerperfile/sharedstate; `TradingFacade.java`, bridge, app.js dan save adalah hotspot yang tidak boleh ditulisbersamaan. Tidak wajib menciptakanagen/launcher baru jika tidak ada; catatketerbatasan review secara jujur.

## 19. Gerbang bukti dan pengujian

- Setiap F/R/INT punya status NOT_STARTED/IN_PROGRESS/FAIL/PASS_HOST/NATIVE_PENDING/BLOCKED, source fingerprint, input/seed, command, exitcode, assertions, evidence path, reviewer, knownissues. Tidak ada PASS karena folder ada atau dokumenselesai.
- Runnerreuse `Work/run-codex-jvm-check.ps1` dengan output directoryrun baru; verifikasialltestsdiscovered=executed, 0 failure/error/unexpectedskip. Jangan copyclasseslama yang tidak dikompilasiterhadapsource baru. Baselinequerycurrentclasses sebelum memberi angka “semua tes”.
- Reuse Wire Qa Server tapicompilefresh againstdomain/bridgeactual, isolatedqa-save/port. Runnerexisting bisa hardcodeoutputpath, port/save, overrideclock atau menyalinbuildclasseslama; auditsebelumrun. Tidak mutasi savepreview8766/8792 atau writer8794. Jika server JBRloopbackgagal, gunakan workaroundlokalperinvocation yang terverifikasi, bukan pengaturan Javaglobal.
- JVMfinancial/time/save:10 seedx100aksibermakna minimum untuk keluargaperilakukritis; invariantsetiap aksi (cash/reserved/positions/debt/units/ledger/issuerassets/time/event idempotency). Seedberbeda tetapi100no-op bukan cakupan. Tambahkanboundary/faultinjection khusus F01–F05dan Rbaru.
- Browserjourneywajibmelakukan: fresh entry→automaticclock→hibah/countdown→quotedomain→buy/fill/sell/partial/cancel→portfolio/history→style/missionclaim→fundbuy/redeem→depositmaturity→spotdigital/commodity→IPOreserve/allot/refund/list→news release/actualimpact→collectionbuy/achievement→profil e/restore. Fixtureprogress/seedboleh melalui QA-onlydomaincontrol yang jelas; jangan membypass productionvalidation untuk mengklaimjourneypemain.
- Negative controls: hilangkanbutton BUY, balikkansign%atau freezequote, blokirstagematurity, corruptbackup>10 byte, duplicateeventnews/IPO, putarcaptionfundtanpaunderlying; gate harus FAIL. Simpanhasilnegative sebagai PASS_EXPECTED_FAILURE, terpisah darirunnormal. Jangan mengubahsource produksiuseruntukfaultinjection tanpa restore/hashcheck.
- Seluruhscreen/state relevan:7 viewport320x568, 360x640, 390x844, 412x915, 430x932, 768x1024, 1440x900 masing-masing100%/200%text. Inventorymatrix includesnewtabs, locked/empty/error/loading/claimable, permissiondenied, legacyrecovery, keyboard, modal, longmoney/internationalnames150catalog. Testroot5tabx14kombinasi saja tidak membuktikansemuadetailstate.
- Targettouch>=48 dpterkalibrasi CSS/device; status adaikon/teks;keyboard/Back/fokustrappedcorrect, reducedmotion/highcontrast, charttablefallback. Visualscreenshots inspectedbukan hanya geometryassert.
- Perf:>=30 sampleper aksi nyata, nav P95<=1 detik, ordinarylocal P95<=300 ms, heavyfeedback<=300 ms+UI responsif; ukurcompletionbarusebutdurasi. Tunggu hasilrender/quote/filteryang benar, bukan `.active` yang sudah adasebelumclick. Ukurheavynews/IPOmonthly/catchup/150 catalog/24 fund/AUMbig; RAM/growth30month/GC dan ledgergrowth. Tidak melonggarkan ambang atau menghapuslongtest yanggagal.
- APK:buildfresh `android/gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` pada rootandroid dengan toolchain existing yangdiverifikasi; jika menjalankanbertahapcatatexitmasing-masing. Periksa min Sdk26, target36, application Id`com.tradingglobal3d.mobile`,version/signing updateidentity, manifestpermissionreceiver, offlineassets, 4/semua aset hashes, APK hashdan size. Debugcandidate bukan rilisstore. Jangan memasang APK/push tanpa otorisasi.
- Save pribadi:before/afterhashidentik untuk pengujian QA; semua migrationujicopyisolated. Jangan bakeplayerdata, keys, rawdatasetunlicensed, secrets/binarybesar kedalamcommit.
- Nativegates:insets API26–36, Android Back/IME, profil ecompat, permissionnotifications, localnotificationdelivery/deeplink/doze/lifecycle. Bila belum diotorisasi/tersedia, catat NATIVE_PENDING. Hostacceptance/buildbukan buktiperangkat; hasil akhir dapat diserahkan sebagai **HOST_VERIFIED_CANDIDATE_NATIVE_PENDING**, bukan “semua fitur100%selesai”.

## 20. Pelaporan, stop, dan handoff

Sesudahhasilsubtaskterukur: laporkan Fselesai/5 dan Rselesai/14, active/pending, changedfiles, tes+hasil+scope/evidence, temuanbaru, blocker, next_action. Statusphasebaru boleh PASS setelah seluruhacceptanceyang sesuai lingkup lulus dan 0bugknownterbuka termasukminor. Reviewterpisah wajib sesuai aturanrepo.

Setelah2kegagalansama tanpa diagnosis/bukti baru kirim PERLU_PERHATIAN dan ubahpendekatan; setelah3 alihkekerjaindependen atau BLOCKED bila butuhfaktorluar. Tidak berhenti hanya karena durasi/iterasi/token. STOP pemilik→checkpointaman, writeinflightdiselesaikan/diabortaman, laporkanpaused; resumeexactsession ID bukan latest.

Handofffinalwajib: keputusan pemilik15/15; coverage F01–F05/R01–R14/INT; changelog; daftar150 koleksi/24 fundactualdibanding CSV; migrations+recoverycontract; commandreproducefresh; positive+negativeevidence; remainingnativegates; APK+hash+metadata+source fingerprint; screenshotrepresentatif; panduanujipemilik; Gitcheckpointterarah danstatusdirtyworkyangdipertahankan. Jangan mengirim pesan kepemilik lain/push/publish otomatis.

## 21. Rujukan primer untuk implementer

Rujukan ini mendasari kontrak platform/konsep, bukan sertifikasi produkgame atau pernyataan semua kebutuhan hukum dunia nyata terpenuhi. Parameter harga/fee/rarity/volatilitas dalam paket adalah rancangan gameplay yangharuskalibrasi, bukan rekomendasi investasi uang nyata.

- Penangananinsets Web View harus memperhatikan nativecontainer dan viewport web agar padding tidak dihitungganda: [Android: window insets in Web View](https://developer.android.com/develop/ui/views/layout/webapps/understand-window-insets).
- Perilakuedge-to-edge pada target Androidbaru: [Android: edge-to-edge views](https://developer.android.com/develop/ui/views/layout/edge-to-edge).
- Channelnotifikasi pada API26+ dan izinnotifikasi API33+ mengikuti sistem: [Android notification channels](https://developer.android.com/develop/ui/compose/notifications/channels), [Android notification permission](https://developer.android.com/develop/ui/compose/notifications/notification-permission).
- Definisi NAV=aset-liabilitas dan basis perunit dipakai pada desainfund: [Investor.gov: Net Asset Value](https://www.investor.gov/introduction-investing/investing-basics/glossary/net-asset-value).
- Tujuan/risiko/biaya/kinerja adalah informasi produk yangperlu dibaca; pastperformance bukan jaminan hasil: [Investor.gov: Mutual Funds](https://www.investor.gov/introduction-investing/investing-basics/investment-products/mutual-funds-and-exchange-traded-funds-etfs/mutual-funds).

Tanggalpemeriksaanrujukan22Oktober2026. Tidak ada claim lookupdata fundamental100 emiten dunia nyata pada tugas persiapan ini; pemilik memilih fundamental model game.
