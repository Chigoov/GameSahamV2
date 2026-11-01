# PROMPT MASTER — Lanjutan Game Saham, revisi pemilik dan kesiapan uji bermain

Saya pemilik proyek. Lanjutkan Game Saham sebagai long task: implementasikan seluruh revisi di bawah, pertahankan perbaikan yang sudah benar, tuntaskan bug yang relevan, uji perilaku nyata, lakukan review, lalu serahkan preview dan APK kandidat untuk saya uji bermain. Jangan berhenti pada rencana, mockup, daftar saran, atau klaim tes hijau yang tidak membuktikan perilaku.

Dokumen ini menggabungkan mandat remediasi, kesiapan bermain, enam revisi awal pemilik, dan enam penandaan browser berikutnya. Ini menjadi ringkasan operasional terbaru untuk lingkup tersebut; requirement produk/standar existing yang tidak bertentangan tetap berlaku. Keputusan eksplisit terbaru: foreground1× menjadi15menit nyata per24jamgame dan kurs acuanUSD/IDR tetapRp15.800. Jangan kembali memakai aturan5menit dari dokumen lama.

## 1. Root, baseline dan perlindungan

Root produksi yang boleh ditulis:
`C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`

`Game Trading Saham` adalah repo historis berbeda. Jangan menulis atau mengimpor source/save/build darinya.

Saat prompt disiapkan HEAD masih49bbafb, tetapi ada banyak perubahan lokal pada source Java, UI, QA, tracker, checkpoint dan tes yang belum di-commit. Buktikan cwd/Git root/branch/HEAD, tracked/untracked changes, source fingerprint, writer aktif serta port/saveQA. Working tree terbaru adalah bagian baseline; jangan rollback keHEAD, reset/clean, menghapus output massal, atau commit semua file secara membabi buta. Jika pekerjaan masih aktif, koordinasikan pada batas aman; satu writer perfile/sharedstate/buildoutput, satu launcher per sesi.

Baca AGENTS.md, Work/README.md, konsep/PRD/designbrief, standar fase dan keputusan pemilik yang dirujuk, STATUS dan checkpoint terbaru. Gunakan prompt remediasi/penutupan/kandidat serta `Longtask-Revisi-Pemilik-Waktu-Briefing-Bisnis-Berita-Referensi-2026-10-22.md` sebagai rincian pendukung. Cocokkan klaim CLOSED/PASS dengan source dan bukti aktual; jangan mengulang bagian yang sudah terbukti benar.

Save pribadi yang dilindungi:
`Artifacts/LocalPreview/preview-save/save_game_v2.json`
SHA256 baseline: `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`.
Periksa awal/akhir. Jika berubah, diagnosis tanpa overwrite. Semua reset/transaksi/crash/recovery memakai saveQA terisolasi. Jangan memutasi preview8766 atau preview8792 yang sedang dilihat pemilik. Simpan APK dan bukti baseline.

## 2. Arsitektur, wewenang dan cara kerja

Produksi tetap Java + AndroidWebView + HTML/CSS/JS lokal. Java satu pemilik uang, ledger, harga, order, NPC, clock dan save; JavaScript hanya input/presentasi. Pertahankan isolasi akun pribadi/perusahaan/klien, katalog100aktif50IDX50US12kategori, migrasi save/katalog/IPO/inventory, applicationID com.tradingglobal3d.mobile, signing dan progres.

Ponytail: pahami alur dan caller, reuse fungsi/token/komponen/runner/dependency existing, kemudian standardlibrary/nativeplatform, lalu perubahan minimum yang benar. Jangan menambah engine/framework/eventbus/abstraksi spekulatif. Jangan memangkas validasi, keamanan uang, aksesibilitas atau recovery. Logika nontrivial mempunyai runnable regression minimum yang bermakna.

Saya mengizinkan perubahan repo, tes domain/browser, preview lokal, review, build dan commit lokal yang ditinjau. Tidak ada push/publish, pesan pihak lain, pembayaran, perubahan global Hermes/provider/model/credentials, scheduler baru, ADB/emulator/perangkat fisik atau repo/data lain. Reviewer berbeda bila tersedia, ownership jelas; bila tidak tersedia, catat batas tanpa mengarang review. Native tetap NOT_TESTED/DEFERRED.

## 3. Waktu permainan:15menit untuk satu hari penuh

Keputusan saya:24jamgame dari00.00 sampai pergantian hari sesudah23.59 =15menit nyata pada foreground normal1×. Ini bukan15menit hanya untuk sesi pasar buka. Background/ditutup tetap1jam nyata per1harigame, mengabaikan multiplier foreground.

Perbaiki konversi clock domain shared dan semua caller:900detik nyata→86400detikgame, yaitu96detikgame perdetiknyata pada1×. Jangan memperlambat tulisan jam/polling saja atau membuat clockJS kedua. Default1× harus jelas;2×/5× bila dipertahankan adalah percepatan yang dipilih pemain, bukan stateQA yang diwariskan diam-diam.

Pertahankan sesi bursa, pause/resume, remainder, expiry, kalender, biaya/pendapatan/misi serta pemrosesan tepatsekali. Update seluruh teks/dokumen/test yang masih menyebut5menit atau keliru menyebut backgroundselalu dijeda. Save lama tidak dinilai ulang retroaktif dengan menggandakan hari/biaya.

Uji900detik→1hari,300detik→8jam, tengahmalam dari00.00 dan waktu lain, pecahanelapsed, pause/resume, load, dan background3600detik→1hari meski sebelumnya2×/5×. Bedakan detiknyata dengan detikgame di setiap caller.

## 4. AturWaktu dan pemilih akun yang jelas

Tombol BukaBriefing saat ini berisi kontrol waktu. Ubah menjadi **Atur Waktu**, berisi tanggal/sesi, berjalan/jeda, kecepatan, penjelasan1hari=15menit, serta Mulai/Lanjutkan/Jeda/Lewati1Hari sesuai keadaan. Skip tetap punya tinjauan/konfirmasi konsekuensi dan idempotency. Update semua entrypoint/tutorial yang memanggilnya.

Pemilih PortofolioPribadi diberi konteks jelas, misalnya **Akun Transaksi: Pribadi**. Pilihan klien mengikuti akses domain. Bedakan memilih akun transaksi dengan membuka tabPortofolio. Jangan menjumlahkan dana klien sebagai kas pribadi.

## 5. Bisnis rapi dan Koleksi berkategori

Pertahankan lima tab utama. TabBisnis memakai subnavigasi yang jelas: **Usaha Saya**, **Koleksi**, serta **Kelola Dana** bila memang relevan/tersedia. Jangan mencampurkan semua koleksi ke daftar panjang di bawah perusahaan atau menambah submenu kosong.

UsahaSaya menampilkan ringkasan dan kartu perusahaan yang ringkas; operasional/pegawai/fasilitas/modal berada dalam detail yang rapi, tanpa fungsi existing hilang.

Koleksi memiliki menu **Semua, Kendaraan, Properti, Perhiasan, Seni & Jam Tangan**, sesuai kategori domain; filterDimiliki/BelumDimiliki bila sesuai. Satu renderer/filter dipakai seluruh pintasan dariBeranda/Bisnis/Lainnya. Kartu menonjolkan nama,gambar/fallback,harga/valuasi,status; detail memuat biaya,reputasi,kelangkaan dan Beli/Jual. Jangan menyamakan harga beli/valuasi/hasil jual bila domain berbeda.

Uji setiap item berada pada kategori benar, Semua tanpa duplikasi, filter setelahbeli/jual, empty state, saldo/ledger, preservasi kategori/fokus/scroll dan layar320px/font200%.

## 6. Bahasa pemain dan badge yang dihapus

Hapus badge **SIMULASI** pada judulBeranda yang ditandai pemilik; rapikan layout tanpa badgepengganti yang tidak berguna. PreviewQA tetap diberi penanda singkat data percobaan terpisah; jangan menyamarkan mock atau fixture sebagai aplikasi/data bursa nyata. Konteks produk/sumber data dijelaskan pada tempat yang relevan, bukan berulang pada kartu.

Audit bahasa di seluruh layar/state/detail/tiket/review/receipt/misi/pinjaman/danaklien/notifikasi/error. Jangan tampilkan MANDATE-01,D10,kodeinternal atau rawenum kepada pemain. Gunakan nama manusia dan bahasaIndonesia: FILLED→TerisiPenuh, PARTIALLY_FILLED→TerisiSebagian, OPEN→Mengantre, dan padanan tepat lainnya. Error menjelaskan alasan dan langkah berikutnya.

ID domain, APIenum, requestId, savekey dan ledger tetap utuh secara internal. Reuse mappingpresentation shared; jangan mengubah identitas data hanya untuk membersihkan teks. Recovery dan konfirmasi penting tetap jelas. Rincian teknis masuk log/diagnostik relevan, bukan kartu utama. Ticker/bursa sebagai identitas sah tetap boleh.

## 7. KabarBaik dan KabarBuruk diBeranda

Buat **Kabar Pasar Hari Ini** dengan filterSemua/KabarBaik/KabarBuruk; berita netral dapat muncul diSemua. Kartu ringkas berisi judul,waktugame,perusahaan/sektor,ringkasan dampak, aksesdetail dan LihatSemua menuju daftar existing.

Reuse getResearchFeed/kontrakdomain yang aktual. Klasifikasi berdasarkan sentimentImpact yang diverifikasi, bukan tebakanjudul. Hanya berita yang sudah terbit menurut clockgame, urutan terbaru, tidak mengisi kategori kosong dengan berita palsu atau membocorkan eventfuture. Dampak positif bagi suatuperusahaan tidak menjamin semuaharga naik.

Perbarui saat hari/revisi berubah tanpa reload/duplikasi dan tanpa hilangfilter/fokus/scroll. Memuat kartu bukan membaca artikel otomatis atau menggandakanreward. Berita tetap peristiwa game, bukan livefeed dunia nyata. Tidak menambah API/langganan/layananberbayar.

## 8. Ketiga gambar referensi wajib dilihat dan diterapkan

Saya melampirkan kembali tiga gambar; salinan lokal juga ada:
1. `Docs/Design/References/Owner-2026-10-21/01-wealth-and-asset-detail.jpg`.
2. `Docs/Design/References/Owner-2026-10-21/02-market-and-candlestick.jpg`.
3. `Docs/Design/References/Owner-2026-10-21/03-portfolio-and-quick-actions.jpg`.

Buka ketiganya dengan vision. Membaca filename/deskripsi bukan inspeksi visual. Jika tidak dapat dilihat, jelaskan batas dan kerjakan bagian independen; jangan mengaku sesuai referensi.

Pemetaan: gambar1 untuk hierarkiBeranda/detailaset, gambar2 untukPasar/grafik/detailBeliJual, gambar3 untukPortofolio/kepemilikan/aksicepat. Terapkan susunan informasi, proporsi, spacing, kartu dan CTA dengan satu token warna/font/radius/state yang konsisten, bukan tiga tema. Pertahankan identitas game dan lima tab.

Buat tabel elemenreferensi→komponen→hasil→screenshotbefore/after, lalu buka dan bandingkan hasilnya. Grafik/angka memakai data game yang sah. Jangan memasang screenshot sebagai UI, menyalin fiturcrypto/akunPRO/identitasorang/transaksiuangnyata, atau membuat grafik dekoratif.

Kontrol/chart/kartu memakaiHTML/CSS/SVG existing. Ilustrasi raster tambahan tetap dibuatCodex; Hermes menyiapkan slot/inventaris dan mengintegrasikan berkas yang benar-benar tersedia. Jangan mengarang asetgenerated atau berhenti mengerjakan layout karena asetpending.

## 9. Grafik harus benar-benar disajikan

Dua penandaan pemilik pada tombolM1/M5/M15/H1/D1 dan pesanBelumAdaTransaksi adalah satu masalah: grafik tidak terlihat. Menghapus pesan kosong atau hanya menampilkan kontrol bukan perbaikan.

Telusuri domain getChartData→bridge→timeframe→dataOHLCV→renderer→ukuran/visibility→liveupdate; periksa normalgame versus replay, IDinstrumen,timestamp/timezone,resolusi,save/load dan kesesuaian compiledclass server.

Semuaemiten yang dinyatakan lengkap harus punya sumbergrafikawal sah sehingga pemain tidak harus bertransaksi dahulu. Periksa data benar-benar dimuat, bukan hanya manifest. Jika memakai histori bursa nyata, verifikasi source/periode/resolusi/pricebasis/hakpakai dan keterkaitan tanggal; pisahkan dari riwayat perdagangan game yang berkembang setelahnya.

Jangan memecah candleD1 menjadiM1/M5/M15/H1 rekaan. Intraday memakai data intraday sah atau tradehistory game yang benar-benar tercatat. Default pada periode yang punya data; periode belumtersedia diberi keterangan dan tindakan yang jelas. Marketclosed tidak menghilangkan histori. Tidak ada randomwalk/demo yang disebut dataasli.

Candle/garis mempunyai area nonnol, skala harga/waktu terbaca dan data yang valid; pembaruanbar/tape otomatis menjaga pilihanperiode/jenis/fokus/scroll. Uji dataada/kosong/error,mode,line/candle,timeframe,closed,replay,barbaru,save/load dan geometri renderer. Screenshot dibuka. Jika sumberdata belum ada, laporkan gap dan kerjakan penyelesaiannya; jangan klaimgrafikselesai100%.

## 10. Profil perusahaan nyata dan audit100/101emiten

Tambahkan **Tentang Perusahaan**: namaresmi,ticker,bursa,negara,sektor/industri,matauang,deskripsi spesifik1–3kalimat,situsresmi bila terverifikasi. Sumber/tanggalverifikasi dapat dibuka melaluiSumberData. Jangan mengarang CEO,jumlahpegawai,marketcap,PER,EPS/dividen atau memakai seedharga/sharesgame sebagai fundamental nyata.

Audit minimal100emiten asli aktif baseline50IDX50US12kategori. Gunakan sumberprimer perusahaan/bursa/regulator; catat URL/tanggal. Dataoffline lokal yang boleh didistribusikan diutamakan. License repository tidak otomatis mencakup data bursa pihak ketiga. Jangan mengaktifkan data karantina, membeli layanan atau mengarang lisensi untuk mengejarcoverage.

Preview menampilkan101instrumen: rekonsiliasi IDunik,katalog,save,IPO,replay/fixture/filter. Jika tambahan itu IPO fiktif, jangan menyebutnya emitenasli atau memasukkannya ke daftarutama emitenasli; hentikan penambahan fiktif baru pada daftar tersebut. Pertahankan cara mengelola posisi/order/riwayat lama tanpa merusaksave. Jangan menghapus posisi untuk memaksajumlah100 atau mengganti nama fiktif dengan perusahaan nyata tanpa migrasi sah. Jika tambahan nyata, buktikan.

Tabelcoverage peremiten memuat identitas/profil/source, histori/periode/resolusi/source, kategorisasi, serta hasilBUY/SELL/ledger/save. Satu datasetAAPL/BBCA tidak membuktikan semua100punya histori. Validasi parsing/OHLCV/volume/waktu/currency/duplikasi/file sebenarnya.

Dengan QAterisolasi dan sesi sah, buktikanBUYdanSELL semuaemiten, lot/currency/fee, matching/fill, posisi/kas/ledger/persistensi. Flagaktif atau ordermengantre saja bukan bukti settlement. Halt/likuiditas mengikuti aturan domain tanpa bypass atau sukses palsu. Identitas asli tidak berarti seluruh hargagame merupakan harga live bursa; jelaskan jenisdata pada tempat relevan.

## 11. Kurs acuan tetapRp15.800 dan bariskurs disembunyikan

Keputusan saya: **kurs acuan1USD=Rp15.800**, tetap sepanjanggame. Sembunyikan barisKursValuasiUSD/IDR pada ringkasanPortofolio dan ringkasan lain yang tidak diperlukan. Saldo,harga saham,kepemilikan dan hasiltransaksi tetap dinamis; tidak dibekukan dan tidak disamakan antarvaluta.

Reuse satu konstanta desimalotoritatif domain; telusuri FxExchangeService,restore/reset/setMidRate serta valuasi,FX,margin,risiko,AUM,multiaset dan save. Jangan ada rateJS kedua. Jika memang sudah tetap, tegaskan kontrak dan ubah hanya jalur yang bertentangan.

Fee/spread existing tetap biaya yang dijelaskan terpisah pada reviewtukar; jangan mengubahnya diam-diam. Kurs dapat dilihat pada detailpenukaran yang memerlukan informasi itu, bukan memenuhi halamanutama. Receipt/ledger lama tidak ditulisulang. Save dengan ratelama berbeda membutuhkan backup/migrasi dan invalidasiquote aman tanpa menghapus uang/receiptidempotency.

Uji15800tetap sesudahtick/hari/berita/resume/load; valuasibruto dan konversi sesuai presisi, net setelahbiaya benar, barisUI hilang, saldo berubah sah padaFX/BUY/SELL dan akun tetap terpisah.

## 12. Pertahankan dan buktikan pembaruan otomatis/feedback

Perubahan terkonfirmasi domain harus terlihat pada layar terkait yang tetap terbuka: Beranda,kasIDR/USD,posisi/detailposisi,Bisnis,receipt,detailinstrumen/grafik/tape dan berita. Reuse update mekanisme minimal; jangan renderulang seluruhhalaman setiapdetik atau membuat ekonomikedua.

Pastikan buglama tidak kembali: toastquantity sudahlembar tanpa pengali lotkedua; posisi nol memperbarui seluruhP/L/currency; callback berdasarkan identitas/data terbaru; receiptOPEN→beberapaPARTIAL→FILLED/cancel/reject/expired; statuspasar/tombol sepakat. Pending/unknown tidak tampil sukses. Retry memakai identitas sama tanpa debit/kreditganda.

Deduplikasi berdasarkan request/event/revisi serta akun/run yang tepat; uji melewatiTTL,pindahtab,retry,reload/restore. Dua kejadian sah berteks sama tetap diumumkan. Live-region toast tidak ikut inert saat modal; backgroundinteraktif tetap terlindungi. Reducedmotion/powersave tidak mengubah hasil ekonomi atau menjadi satu-satunya cara memahami status.

## 13. Pengujian, performa dan review

Jalankan regresi applicable pada sourceakhir menggunakanrunner existing. Nontrivialchange mempunyai assertion yang dapat mendeteksi bug, bukan cerminimplementasi. Cocokkan DOM dengan hasil domain/expectedamount,currency,quantity dan revision; sekadar visible,berubah,tidaknol bukan kelulusan.

Uji7viewport320×568,360×640,390×844,412×915,430×932,768×1024,1440×900 × font100%/200% nyata untuklayar/stateberubah. Buka screenshot; periksa kontras aktual, clippinginternal, angka/unit/CTA, keyboard/fokus/scroll,target48dp,reducedmotion/powersave. Documentwidth kecil saja bukan bukti UIbaik.

Benchmark minimal30sampel perjenis wajib termasukheavywork. P95aksi lokal≤300ms perjenis; seluruhsamplenavigasi≤1000ms; feedbackheavywork benar-benar tergambar≤300ms dan eventloopresponsif. Pisahkan feedbackawal/hasilakhir dan domaincommit→UI dari waktu tunggu antrean. Waktupanggilanbridge saja bukan paint. Jangan menutup aksi lambat dengan agregasi aksi cepat atau membuang timeout/outlier.

Simpan rawtiming,environment,seed,warmup/cache,source/compiledfingerprint dan beban100emiten/liveupdate. Negativechecks harus menolak sampelkosong/kurang30/lambat dan memberi exitgagal. JSONPASSlama tidak boleh menjadi hasil terbaru setelahrun gagal.

Pakai standar10×100aksi finansial dengan invariant sesuai ketentuan existing untukjalurterdampak. Reviewer berbeda menilai diff DAN kecukupan bukti; nolbugterbuka diketahui termasukminor untukmenyatakan subpekerjaan selesai. Jangan mewariskan PASS dari jumlahtes lama. Laporkan keterbatasanreviewer bila tidak tersedia.

## 14. Perjalanan bermain dan penyimpanan progres

Uji satu perjalanan pemain baru dengan state/seed tercatat melaluiUIproduksi: mulai→cari/detail→BUY→review/receipt→portofolio→SELLsebagian/habis→FX/US→Bisnis/pergantianhari→misi/progres→save→load→lanjut. Hormati prerequisite; fixture statekhusus diberi label terpisah dari perjalanan pemainbaru.

Uji rangkaian minimal7pergantianharigame dengan pendapatan,biaya,payroll,kalender,ordertertunda dan save/load. Bedakan waktunyata,percepatan,SkipDay dan catch-up; jangan mengklaim7harisimulasi sebagai durasiforeground nyata yang tidak dijalankan.

Dengan saveQA, periksa restart/load,backup/recovery dan gangguantulis yang didukungrunner. Save rusak tidak otomatis ditimpa permainanbaru; retry/load/catchup tidak menggandakan hadiah/biaya/settlement. Hostlifecycle/domain bukan bukti Androidfisik. Jangan mengklaim mustahil kehilanganprogres.

## 15. Penyerahan, status dan persistensi

Simpan bukti rununik di `Artifacts/Master-Lanjutan-2026-10-22/`, tanpa menimpa baseline/runaktif. Update tracker/checkpoint/sourcecoverage yang existing, jangan membuat sistemtracking paralel. Perbaiki perbedaan metadata tes/APK dengan membaca artefak, bukan memilihangka yang menguntungkan.

Setelah sourcestabil dan review, serahkan:
1. Preview lokal terbaru memakai save terpisah dengan labelpreview. Jangan mereset previewyang sedang dilihat pemilik; berikanportbaru jika perlu.
2. APKkandidat baru; verifikasiZIP,appID/version/minSDK/targetSDK,certificatesigning,byteaset/katalog dan tidak ada save/helper/fixtureQA/datasetkarantina terpaket. Path,ukuranbyte,SHA256,commit/sourcefingerprint dan compiledsource cocok dengan berkasfinal. Pertahankan baseline dan hash savepribadi.
3. Laporan bahasaIndonesia: requirement→perubahan→expected/actual→test/reviewer→bukti; gambarbefore/after dan pemetaan3referensi; seluruh100/101coverage; grafik/profil/source; clock15menit; kurs15800; save/perjalananbermain; benchmarkperjenis/rawtiming; asetpending/nativeNOT_TESTED dan next_action.
4. Panduan ujiHP singkat yang menjaga progres: identifikasiAPK,pasang/update tanpa menghapusdata,transaksi,bisnis,save,tutup/buka,background/resume,Back/keyboard,hasilharapan dan formatlaporbug. Signaturemismatch tidak diselesaikan dengan menyuruhuninstall tanpa membahas dampakprogres. Jangan mengklaim langkahHP sudah lulus sebelum dijalankan pemilik.

READY_FOR_OWNER_PLAYTEST hanya jika alurhost applicable lulus, temuanhost tertutup danAPK cocok sourceakhir. Itu bukan penerimaan Androidfisik/seluruhPRD14fase. Jika host/data belum lengkap, HOST_PARTIAL beserta temuan/coverage; asetwajibpending tetapASSETS_PENDING_CODEX. Kerjakan semua bagian independen sebelum meminta tindakan pemilik. Jangan menutupi bughost dengan alasanmenunggu gambar atau mengklaim100%tanpabukti.

Kerjakan hasilterukur→tes→review→checkpoint→lanjut. Jangan berhenti karena durasi/iterasi/giliran/token,satu tes hijau atauAPK terbentuk. Dua kegagalan sama tanpa diagnosisbaru: PERLU PERHATIAN dan pendekatanberbeda; tiga: pindah pekerjaanindependen atau BLOCKED jika benar-benar memerlukan tindakanluar. HormatiSTOP/pause dan checkpointaman.

Mulai sekarang dengan preflight, baca sumberkeputusan dan ketigagambar, simpan kondisi before, lalu implementasikan sesuai dependensi. Tidak perlu persetujuan rutin dalam scope dan tidak berhenti setelah menuliskanrencana.
