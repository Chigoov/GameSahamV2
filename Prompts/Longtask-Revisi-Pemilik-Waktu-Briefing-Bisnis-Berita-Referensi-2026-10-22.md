# Prompt Hermes: revisi pemilik setelah melihat preview — waktu, bahasa, Bisnis, berita dan tiga referensi

Saya pemilik proyek. Setelah mencoba preview saya meminta enam revisi: jam terasa terlalu cepat; fungsi briefing harus jelas; menu Bisnis/Koleksi harus rapi dan mempunyai kategori sendiri; bahasa/kode sistem seperti MANDATE-01 dihilangkan dari tampilan pemain; kabar baik/buruk ditampilkan di Beranda; dan tiga gambar referensi benar-benar diterapkan. Implementasikan, uji, review dan serahkan preview serta APK kandidat baru. Ini mandat implementasi, bukan permintaan rencana saja.

Tambahan pemilik dari enam penandaan browser tanggal22Oktober2026: hapus badgeSIMULASI; perbaiki grafik yang kosong (dua penandaan pada kontrol dan empty state); tambahkan profil perusahaan; verifikasi seluruh emiten nyata dan dapat ditransaksikan termasuk asal jumlah101instrumen; sembunyikan baris kurs dan kunci kurs acuanUSD/IDR padaRp15.800 perUS$1. InstruksiT7–T11 di bawah menambahT1–T6, tidak menggantikannya. Pemilik sudah mengonfirmasi kurs tetap; saldo tetap bergerak mengikuti transaksi, bukan dibekukan.

## 1. Root, baseline, sumber dan batas wewenang

Root produksi: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`. `Game Trading Saham` adalah repo historis berbeda; jangan menulis atau menyalin source/save/build darinya.

Baca `AGENTS.md`, `Work/README.md`, konsep/PRD/design brief, mandat kualitas yang dirujuk, checkpoint terbaru dan working tree. Baca prompt penutupan remediasi serta kandidat layak uji bermain yang sudah ada; pertahankan hasil yang benar dan tutup temuan relevan yang tersisa tanpa mengulang pekerjaan lulus. Instruksi pemilik terbaru pada prompt ini mengubah rasio waktu foreground5menit menjadi15menit; dokumen lama yang bertentangan harus diperbarui konsisten. Requirement lain tidak otomatis dihapus.

Saat prompt disiapkan HEAD `49bbafb` dengan banyak perubahan lokal Java/UI/QA, checkpoint dan tes baru yang belum di-commit. Buktikan cwd/Git root/branch/HEAD, tracked/untracked changes, fingerprint source, writer aktif dan port/save QA. Jangan reset/clean/rollback atau commit massal semua perubahan. Satu writer per file/shared state/output; jangan menjalankan launcher kedua pada sesi sama. Jika agent sebelumnya masih bekerja, koordinasikan pada batas aman tanpa mematikan proses berdasarkan dugaan.

Save pribadi: `Artifacts/LocalPreview/preview-save/save_game_v2.json`; SHA256 baseline `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`. Verifikasi sebelum/sesudah, diagnosis perubahan tanpa overwrite. Jangan memakai save pribadi/preview8766 untuk tes mutasi. Preview sementara Codex pada8792 memakai save terpisah di `Artifacts/TemporaryPreview-2026-10-22/preview-save`; jangan memakai server/save preview yang sedang dilihat pemilik untuk tes reset/transaksi otomatis. Buat run QA terisolasi.

Produksi tetap Java + Android WebView + HTML/CSS/JS lokal. Java satu pemilik uang/order/ledger/harga/clock/save. Reuse fungsi, token, komponen, dependency dan runner existing; perbaiki akar masalah shared. Tanpa engine/framework/eventbus baru yang tidak perlu. Pertahankan100perusahaanaktif50IDX50US12kategori, filter, migrasi save/IPO/inventory, isolasi akun, application ID `com.tradingglobal3d.mobile`, signing dan progres.

Scope mengizinkan implementasi repo, tests/browser, preview lokal, build dan commit lokal yang sudah diperiksa. Tidak ada push/publish/pesan pihak lain/pembayaran/perubahan global Hermes/provider/model/credentials/scheduler/ADB/emulator/perangkat fisik atau repo/data lain. Reviewer berbeda bila tersedia, ownership jelas, tanpa mengarang review. Native tetap NOT_TESTED/DEFERRED. Tidak perlu approval rutin dalam scope.

## T1. Perubahan waktu yang sudah diputuskan pemilik

Keputusan eksplisit pemilik: **1hari game penuh dari00.00 sampai pergantian hari sesudah23.59 =15menit nyata pada foreground1×**. Ini24jamkalender game, bukan15menit hanya untuk sesi perdagangan. Aturan sebelumnya5menit diganti. Background/ditutup tetap1jamnyata per1harigame sesuai keputusan sebelumnya, mengabaikan multiplier foreground.

Source saat diperiksa `GameClock.FOREGROUND_GAME_SECONDS_PER_REAL_SECOND=288`, inactive24. Rasio baru foreground1× adalah96detikgame per1detiknyata:900detiknyata→86400detikgame. Implementasikan pada clock domain shared dan seluruh caller yang relevan; jangan memperlambat tulisan jam saja, menurunkan polling UI, mengubah sesi bursa atau membuat clock JS kedua.

- Profil/default normal memakai1×. Multiplier existing2×/5× bila tetap tersedia harus berlabel jelas sebagai percepatan; tidak boleh aktif diam-diam karena state QA. Tampilkan kecepatan yang benar saat bermain. Jangan mengubah preferensi save lama tanpa alasan/migrasi yang jelas.
- Pertahankan ketepatan elapsed/remainder, pause/resume, catch-up dan pergantian hari. Pendapatan, biaya, payroll, misi, expiry order dan kalender mengikuti waktu game yang sama. Jangan mengalikan atau memproses satu hari dua kali saat konversi/Skip Day/load.
- Update seluruh petunjuk UI/dokumen/test yang menyebut5menit. Ada teks briefing yang keliru mengatakan background dijeda; koreksi sesuai kontrak inactive1jam, bukan menyalin teks lama.
- Acceptance dengan clock deterministik:900detiknyata foreground1× memajukan tepat1harigame;300detik hanya8jamgame; dari00.00 mencapai tengah malam berikutnya setelah900detik; pengujian pecahan elapsed/remainder dan pergantian hari dari waktu selain00.00 juga benar. Background3600detik memajukan1harigame tanpa mewarisi2×/5×. Uji pause/resume/save/load tanpa retroaktif menilai ulang waktu historis. Bedakan unit detik nyata/detik game di setiap caller.

## T2. Jelaskan dan rapikan fungsi briefing

Saat ini tombol `Buka briefing` pada Beranda membuka kontrol waktu (handler app.js sekitar4166): waktu/akun, mulai-lanjut, jeda, speed1/2/5 dan skip24jam. Nama tersebut membingungkan karena belum berupa briefing berita.

- Ubah tombol/halamannya menjadi **Atur Waktu**. Tampilkan tanggal, sesi pasar, keadaan berjalan/jeda, kecepatan dan keterangan: `Kecepatan normal: 1 hari game = 15 menit nyata`.
- Gunakan tombol yang lugas: `Mulai`, `Lanjutkan`, `Jeda`, dan `Lewati 1 Hari` sesuai keadaan nyata. Skip tetap punya tinjauan/konfirmasi konsekuensi; tidak menggandakan proses ketika diklik ulang/retry.
- Ringkasan berita/agenda hari ini ditempatkan di Beranda dan dijelaskan padaT5. Jangan menambahkan layar briefing lain yang menduplikasi kontrol atau seluruh daftar berita.
- Pemilih akun atas diberi konteks yang jelas, misalnya `Akun Transaksi: Pribadi`, serta label klien saat relevan. Jangan mencampurkan kas pribadi dengan dana klien hanya agar ringkasan lebih pendek.
- Uji tombol Atur Waktu dari semua entry point/tutorial yang memanggil handler lama. Perubahan label tidak boleh memutus flow, ID internal yang masih dipakai tes, atau aksesibilitas.

## T3. Rapikan Bisnis dan berikan menu kategori Koleksi sendiri

Pertahankan lima tab utama. Di tab **Bisnis**, gunakan subnavigasi yang jelas dengan pola native existing, misalnya **Usaha Saya**, **Koleksi**, dan **Kelola Dana** bila fitur dana klien memang sudah tersedia. Gunakan nama yang sesuai domain; jangan membuat kategori/fitur kosong. Daftar semua koleksi tidak lagi bercampur panjang di bawah kartu perusahaan pada halaman awal Bisnis.

- Usaha Saya: ringkasan yang ringkas, daftar perusahaan, kas/pendapatan/biaya/status utama, lalu tindakan operasional dalam detail perusahaan yang rapi. Hindari duplikasi aksi dan deretan kartu terlalu panjang.
- Koleksi membuka halaman/submenu khusus. Berikan menu kategori **Semua**, **Kendaraan**, **Properti**, **Perhiasan**, **Seni & Jam Tangan**, sesuai kategori domain yang benar. Tambahkan filter **Dimiliki** dan **Belum Dimiliki** bila memadai dengan kontrol existing, bukan menciptakan kategori ekonomi baru.
- Gunakan satu renderer/filter kategori bersama untuk akses Koleksi dari Bisnis, Beranda dan Lainnya. Jangan menyisakan galeri berbeda yang salah satunya tidak punya kategori atau data basi.
- Kartu koleksi menonjolkan nama, gambar/fallback, harga/valuasi dan status kepemilikan. Detail memuat biaya pemeliharaan, reputasi, kelangkaan dan aksiBeli/Jual. Validasi, dana kurang, konfirmasi dan requestId tetap aman. Harga beli, valuasi dan hasil jual jangan disamakan bila aturan domain berbeda.
- Label kategori memakai bahasa pemain, bukan enum `ART_AND_WATCHES`, `VEHICLE` atau kode `COL-...`. Jumlah kategori/hasil dan empty state harus akurat. Membeli/menjual memperbarui kepemilikan, saldo, hitungan dan filter tanpa merusak kategori/scroll/fokus.
- Acceptance: seluruh item muncul tepat pada kategori yang sesuai, Semua mencakup union tanpa duplikasi; Dimiliki/Belum Dimiliki benar setelah transaksi; UI tetap rapi pada320px/font200%; tidak ada fungsi bisnis/klien yang hilang karena pemisahan navigasi.

## T4. Hilangkan bahasa dan kode sistem dari alur pemain

Audit teks dinamis/static pada Beranda, Pasar, Portofolio, Bisnis, Lainnya, detail, tiket, review, receipt, misi, pinjaman, dana klien, tooltip, empty/error/locked dan notifikasi. Pemilik tidak ingin melihat kode seperti `MANDATE-01`, `D10`, rawenum/status dan istilah pengembangan.

- Ganti ID tampilan dengan nama/deskripsi manusia, misalnya nama mandat yang tersedia atau `Pengelolaan Dana Klien`, bukan memamerkanID. `FILLED`→`Terisi penuh`, `PARTIALLY_FILLED`→`Terisi sebagian`, `OPEN`→`Mengantre`, dan padanan status lain yang tetap akurat. `Snapshot`, `Bridge`, `facade`, `requestId`, kode QA/debug bukan penjelasan untuk pemain.
- ID domain, key save, enum API, requestId dan ledger **tetap dipertahankan secara internal**. Jangan mengubah identitas atau merusak rekonsiliasi transaksi demi menghapus teks. Formatter/mapping presentation shared digunakan bila sudah ada; audit caller agar tidak membuat satu mapping perhalaman.
- Error rutin memberi alasan dan langkah berikutnya dalam bahasa Indonesia. Rincian teknis dapat masuk log/diagnostik opsional yang relevan untuk dukungan, tidak menjadi isi kartu utama. Recovery/data-loss safeguards tetap jelas dan tidak disembunyikan.
- Istilah game lazim tetap boleh bila dipahami, disertai penjelasan pendek bila perlu. Jangan sekadar mengganti satu kata kode dengan jargon Inggris lain. Label frontend jangan dipakai sebagai identitas filter/status domain.
- Preview/browser harus tetap diberi penanda ringkas **Pratinjau — data percobaan terpisah**; jangan menghapus kejujuran QA untuk membersihkan bahasa. APK produksi tidak menampilkan banner tes. Menyembunyikan label mock tanpa mengubah sifatnya dilarang.
- Acceptance: pencarian pola kode ditambah inspeksi DOM semua state relevan. Assertion berdasarkan visible text, bukan mengharuskan semua ID internal hilang dari source. Cocokkan output transaksi dengan status domain setelah diterjemahkan.

## T5. Kabar baik dan kabar buruk di Beranda

Tambahkan bagian **Kabar Pasar Hari Ini** di Beranda, dengan kontrol **Semua**, **Kabar Baik**, **Kabar Buruk**; netral tetap dapat muncul diSemua dengan penjelasan netral. Gunakan tata letak kartu ringkas agar tidak menenggelamkan ringkasan keuangan/tindakan utama. Setiap kartu dapat dibuka untuk membaca rincian, dan `Lihat Semua Berita` mengarah ke daftar existing yang lengkap.

- Reuse berita domain yang sudah ada (`getResearchFeed`/kontrak aktual), termasuk publishedAt, headline, content, relatedTicker, kategori dan sentimentImpact. Sentiment positif/negatif diklasifikasi dari kontrak domain yang diverifikasi, bukan menebak dari kata judul atau membuat berita palsu. Perjelas terkait perusahaan/sektor yang mana; berita positif bagi satu pihak tidak otomatis menjamin seluruh harga naik.
- Tampilkan label/ikon positif-negatif dan kalimat dampak yang bersumber mekanik nyata. Berita hanya ditampilkan setelah waktu publikasi game; jangan membocorkan event mendatang. Urutan terbaru, jumlah terbatas diBeranda, empty state wajar ketika salah satu kategori belum ada. Jangan membuat berita tambahan hanya untuk mengisi kedua sisi.
- Refresh saat hari/revisi berita berubah tanpa reload/pindah tab, tanpa duplikasi kartu dan tanpa menghilangkan pilihan filter/fokus/scroll. `recordResearchRead` mengikuti aksi membaca existing; memuat daftar otomatis bukan izin mengklaim reward/membaca berulang.
- Berita tetap **simulasi game**, bukan berita pasar dunia nyata. Tidak menambah API berita/langganan/jaringan/layanan baru.
- Acceptance: berita positif, negatif dan netral; tidak ada berita future; publikasi pada pergantian hari muncul tepat sekali; filter benar, detail/aksi kembali benar, akun dan saldo tidak berubah hanya karena daftar direfresh.

## T6. Terapkan tiga gambar referensi secara nyata

Pemilik akan melampirkan kembali ketiga gambar kepada agent. Salinan lokal juga SUDAH tersedia:
1. `Docs/Design/References/Owner-2026-10-21/01-wealth-and-asset-detail.jpg` — original `5f4aaea7dd0d88d3add1425e932f2bfc.jpg`.
2. `Docs/Design/References/Owner-2026-10-21/02-market-and-candlestick.jpg` — original `6b348949d8ddc79ab85e4ae66d1e5648.jpg`.
3. `Docs/Design/References/Owner-2026-10-21/03-portfolio-and-quick-actions.jpg` — original `886f8f1179a97e3dcc926ef2ab7e9a3f.jpg`.

**Buka dan inspeksi ketiga file gambar dengan kemampuan vision**, bukan sekadar membaca nama/path/deskripsi. Jika lampiran sama dengan salinan lokal, tidak perlu menggandakan file. Jika benar-benar tidak dapat melihat file, laporkan batas tepat dan kerjakan bagian independen; jangan mengklaim desain sudah sesuai referensi tanpa melihatnya.

Pemetaan implementasi:
- Gambar1→Beranda dan detail saham: hierarki satu angka utama, perubahan periode yang jelas, ruang antarbagian, kartu daftar yang rapi dan tindakan ringkas. Sesuaikan posisi berita baru tanpa memenuhi layar dengan ringkasan berulang.
- Gambar2→Pasar/detail: kartu instrumen yang mudah dibandingkan, grafik candle/garis dan filter periode yang terbaca, susunan harga/perubahan/kepemilikan serta CTA Beli/Jual yang jelas.
- Gambar3→Portofolio dan aksi cepat: ringkasan aset, tren kekayaan dari history, daftar kepemilikan dan kelompok aksi dengan hierarki yang seragam.

Ketiga referensi menjadi satu sistem visual gelap yang konsisten melalui token warna/font/spacing/radius/state bersama. Pertahankan identitas game, navigasi5tab dan keterbacaan. Tidak membuat3tema terpisah. Tabelkan elemen penting setiap referensi→layar/komponen→hasil aktual→bukti before/after. Perubahan yang sudah sesuai boleh dipertahankan; perbaiki gap yang membuat pemilik belum merasakan penerapannya.

Angka dan grafik bersumber domain/history game, bukan angka/screenshot referensi. Jangan menyalin fitur crypto, alamat wallet, akunPRO, identitas/avatar/nama atau transaksi uang nyata dari mockup. Grafik yang history-nya belum tersedia menunjukkan empty state yang jujur. Screenshot referensi tidak dipasang sebagai UI interaktif. Tata letak/control/chart/gradient menggunakan HTML/CSS/SVG existing. Ilustrasi raster tambahan tetap dibuat Codex sesuai keputusan pemilik; Hermes menyiapkan slot/inventaris lalu mengintegrasikan aset yang benar-benar tersedia, tanpa mengklaim sudah generated atau menunggu aset untuk menyelesaikan layout independen.

Acceptance visual: screenshot sebelum/sesudah padaBeranda, Pasar/detail, Portofolio dan Bisnis/Koleksi; dibuka dan dibandingkan langsung dengan ketiga gambar. Uji7viewport320×568,360×640,390×844,412×915,430×932,768×1024,1440×900 × font100%/200% nyata pada semua state berubah. Periksa kontras aktual, angka/unit tak terpotong, CTA, scroll/fokus, keyboard, target48dp, reduced-motion/power-save. Nol overflow dokumen saja tidak meluluskan visual.

## T7. Hapus badge SIMULASI yang ditandai pemilik (komentar browser1)

Target pemilik adalah `section#view-beranda > .page-heading > span.live-badge` yang bertuliskanSIMULASI. Hapus badge dekoratif tersebut dan rapikan alignment/jarak judulBeranda. Tidak perlu menggantinya dengan badge lain yang sama-sama tidak berguna. Telusuri replikasi badge serupa agar konsisten pada komponen yang sama.

Ini perubahan teks/dekorasi, bukan perubahan sifat produk menjadi broker/pasar uang nyata. Penanda singkat preview dengan save percobaan tetap ada pada lingkungan QA, terpisah dari badge yang dihapus. Konteks game dan sumber/jenis data cukup dijelaskan pada tempat yang relevan sepertiTentang/Data, tanpa mengulang kataSIMULASI pada setiap kartu. Jangan menyamarkan fixture sebagai data bursa asli. Kontrak dataT8–T10 dan aturan beritaT5 tetap berlaku.

Acceptance: badge pada selector yang ditandai tidak terlihat pada source akhir/preview/APK; judul tidak meninggalkan slot kosong; penanda preview QA tetap jujur dan tidak masuk UIAPKproduksi; tidak ada perubahan saldo/clock akibat perubahan dekorasi.

## T8. Grafik harus benar-benar tampil dan bergerak (komentar browser2dan3)

Pemilik menandai kontrolM1/M5/M15/H1/D1 serta kotak `Belum ada transaksi tercatat`. Dua penandaan ini adalah satu masalah perilaku: grafik belum disajikan, bukan sekadar teks empty state yang tidak disukai. Jangan menutup temuan dengan menghapus kotaknya, menambah tombol periode atau screenshot grafik contoh.

Telusuri alur lengkap dan semua caller: `TradingFacade.getChartData`→bridge→handlertimeframe→dataOHLCV→rendererSVG/canvas→ukuran/visibilitycontainer→pembaruan live. Periksa state mode permainan normal versusHistoricalReplay, timestamp/timezone, resolusi, data memangkosong versus gagalrender, mappingIDinstrumen, serta data tersimpan setelahsave/load. Serverpreview lama dapat memakai kelasJava yang berbeda; pastikan source/compiled fingerprint.

- Semua emiten dalam cakupanT10 harus mempunyai sumber grafik awal yang sah, sehingga pemain tidak harus membeli/menjual dulu untuk melihat data. Telusuri pemuatan histori yang sebenarnya, bukan hanya keberadaan manifest atau file CSV. Audit apakah data yang dirujuk benar-benar dibaca dan boleh didistribusikan. Rencanakan cakupan data bersamaT10.
- Identitas/profil perusahaan nyata tidak otomatis berarti seed harga, hargaNPC atau seluruh bar game adalah harga bursa asli. Jika grafik awal memakai histori bursa nyata, sertakan sumber/periode/resolusi/pricebasis/hakpakai dan keterkaitan tanggal yang jelas. Pisahkan dari riwayat transaksi permainan yang berkembang sesudahnya; jangan menyambung dua seri dengan timestamp/harga berbeda tanpa kontrak yang dijelaskan.
- Dataharian asli dapat digunakan untukD1dan timeframe lebih tinggi yang didukung. Jangan memecah satu candleharian menjadi candleM1/M5/M15/H1 rekaan. Timeframe intraday memakai intraday nyata yang sah atau trade history game yang betul-betul tercatat. Gunakan default periode yang memang memiliki data dan jelaskan periode belum tersedia dengan tindakan berikutnya; jangan menampilkan seluruh halaman grafik kosong hanya karena defaultM5belum berisi transaksi.
- Jika data acuan eksternal belum tersedia untuk suatu emiten, catat sebagai gap cakupan dan selesaikan sumber/datanya; jangan mengklaim100%terpenuhi. Riwayat hasil transaksiNPC/player di mesin game boleh menjadi grafik permainan dengan identitas jenisdata yang benar, bukan dilabeli historis bursa asli. Jangan memakai randomwalk atau angka demo sebagai bukti harga asli.
- Candle dan garis harus menunjukkan harga/waktu, label/skala terbaca, keadaan periode aktif dan data volume yang sah bila tersedia. Tampilkan rentang waktu/source yang dapat dibuka. Area grafik mempunyai tinggi/ukuran nyata yang tidak nol pada ponsel. Jangan biarkanCTAatau bottomnav menutupi grafik.
- Grafik serta tape diperbarui otomatis setelah commit transaksi atau terbentukbarbaru, tanpa pindahtab/reload, dengan timeframe/jenis/scroll/fokus tetap. Marketclosed tidak boleh menghapus histori yang sudah tersedia; datafuture tidak boleh bocor dalam replay.

Acceptance: tabel seluruh emiten→ketersediaanhistori/periode/resolusi/source→rendererline/candle→hasilbefore/after. Tes pilihanM1/M5/M15/H1/D1, kondisi dataada/benar-benarkosong/gagalmuat/closed/replay, barbaru dan save/load. Assertion memeriksa data valid DAN elemen grafik yang ter-render dengan geometri nonnol serta perubahanrevisi; screenshot dibuka. Koleksi tombol dan empty state tidak dihitung sebagai grafik berhasil.

## T9. Tambahkan ringkasan perusahaan pada detail saham (komentar browser4)

Pemilik menandai areaStatuspasar dan meminta sedikit data perusahaan. Tambahkan bagian **Tentang Perusahaan** yang ringkas pada detail instrumen, di lokasi yang mudah ditemukan tanpa menenggelamkan harga/grafik/CTA:
- Nama resmi, ticker, bursa, negara, sektor/industri dan mata uang perdagangan.
- Deskripsi usaha singkat1–3kalimat yang spesifik dan terverifikasi, serta situs resmi bila tersedia.
- Sumber dan tanggal verifikasi dapat dibuka melalui `Sumber Data` yang ringkas, bukan kode internal yang memenuhi layar.

Gunakan sumber primer seperti halaman investor/perusahaan/bursa/regulator yang sesuai; catat URL dan tanggal verifikasi. Jangan mengarang tahunberdiri/CEO/jumlahpegawai/marketcap/PER/EPS/dividen atau angka fundamental lain untuk mengisi kartu. Field belum terverifikasi boleh tidak ditampilkan dengan status cakupan pada laporan. Bila fundamental memang ditambahkan, sertakan as-ofdate, unit dan sumber, tidak menggunakan shares/basePrice game sebagai statistik perusahaan nyata.

Profil berlaku bagi seluruh emiten yang dinyatakan asli padaT10. Cache/bundel data yang sah untukoffline; jangan membuat aplikasi bergantung padaAPIjaringan baru atau scrapingwebviewketika pemain membuka detail. UIyang ditampilkan dalam bahasaIndonesia dengan nama resmi tetap akurat. Pertahankan statuspasar yang operasional, diterjemahkan sesuaiT4, bukan menggantinya dengan profilperusahaan.

Acceptance: bukaprofil setiapemiten→nama/ticker/bursa/sektor/deskripsi sesuai rekaman verifikasi; ticker mirip di bursa berbeda tidak tertukar; belum tersedia tidak menjadi teks dummy; akses profil tidak mengubah keadaanfinansial.

## T10. Seluruh emiten harus nyata, terverifikasi dan bisa ditransaksikan (komentar browser5)

Preview pemilik menampilkan **101instrumen**, sementara baseline katalog100aktif50IDX50US. Jangan menganggap angka101kesalahanhitung atau perusahaanasli tambahan sebelum menelusuri sumbernya: hitungIDunik, katalogawal, save, IPO, replay/fixture dan filter aktif. Buat rekonsiliasi yang menjelaskan instrumenke101beserta jenis/sumbernya. Tidak ada reset save atau penghapusan posisi/order untuk memaksa jumlah100.

Makna dataasli pada tugas ini: identitas/profil emiten nyata dan datahistoris yang dinyatakan bursa-asli harus punya sumber yang benar. Ini tidak otomatis memberi mandat livefeed bursa/rekeningbroker/transaksi uangnyata. Harga dan eksekusi game tetap lewat domain sesuai aturan permainan; semua seed/asumsisimulasi dibedakan dalam metadata data yang ringkas. Jangan mengklaim harga live atau fundamental real dari namaemiten asli saja.

- Audit minimal100emiten asli aktif sesuai baseline50IDX50US, kategori/ID/ticker/nama/currency/lotSize yang sah. Gunakan sumber primer, tanggalverifikasi, dan daftar provenance yang dapat diperiksa. Pilih sumber data yang hak penggunaan/redistribusinya benar-benar sesuai; license repository tidak otomatis mencakup data bursa pihak ketiga di dalamnya. Jangan mengaktifkan data karantina karena ingin menutup coverage atau membayar layanan tanpa izin.
- Lengkapi profilT9 dan cakupan data grafikT8. Tabel cakupan harus peremiten, bukan memakai satu datasetAAPL/BBCA untuk mengklaim seluruh100memiliki histori. Periksa hasil parsing/duplikasi/OHLCV/volume/timestamp/currency/resolusi/pricebasis dan file sebenarnya, bukan hanya manifest `historicalDataAvailable:true`.
- Jika instrumenke101adalah IPOgeneratif/fiktif, jangan menyebutnya emiten dunia nyata. Sesuai permintaan semuaemitenasli, daftar pasarutama hanya memuat emiten asli yang tervalidasi. Hentikan penambahan emitenfiktif baru ke daftar tersebut; pertahankan posisi/order/riwayat/save lama secara kompatibel dan sediakan cara mengelolanya tanpa menyamar sebagai emitenasli. Catat keputusan penyajian/migrasi. Jangan mengganti nama fiktif menjadi nama perusahaan nyata tanpa basisdata dan migrasi yang sah. Jika memang emitenasli tambahan, buktikan dan jelaskan jumlahnya.
- Flagaktif saja tidak membuktikan dapatditransaksikan. Untuk setiapemiten, dengan saveQA/capitalQAterisolasi dan sesi pasaryang sah, buktikanBUYdanSELL, lot/currency/fee, hasilfill/partial/resting, updateposisi/kas/ledger dan persistensi. Keterbatasanlikuiditas atauhaltsah diberi status yang tepat, bukan bypass aturan atau menampilkan sukses palsu. Jika order hanya mengantre, belum terbukti fill; sediakan skenariomatchingproduksi yang deterministik dan lawful menurut kontrakgame untuk membuktikan settlement.
- Pemain tidak perlu bertransaksi dulu untuk memunculkan seluruh profil/histori. Tidak adaemiten nonaktif, tombol mati tanpa penjelasan, data dummy atau itemfixture dalam daftarasli yang diklaimsiap.

Acceptance: laporan peremiten berisiID/ticker/bursa/kategori/realcompany-source/profile-source/historicalcoverage/tradeBUY/SELL/ledger/save/checkresults serta rekonsiliasi100versus101. Lulus hanya bagi emiten yang benar-benar diperiksa. Jika ada sumber/hakpakai/datahistoris yang belum tersedia, kerjakan bagian independen lalu laporkan spesifik PARTIAL/BLOCKED, jangan menyembunyikan kekurangan dengan labelsemuaasliatauPASS100%.

## T11. Sembunyikan baris kurs dan tetapkan USD/IDR Rp15.800 (komentar browser6)

Keputusan eksplisit pemilik: **kurs acuan tetap Rp15.800 perUS$1**. Hapus baris `Kurs valuasi USD/IDR` yang menampilkan15800.00 dari ringkasanPortofolio dan ringkasanserupa yang tidak diperlukan. Saldo, harga saham, jumlahkepemilikan dan hasil transaksi tetapdinamis. Ini bukan permintaan membekukan nominalsaldo, menyamakan nilaiIDR/USD atau mengganti semuaaset menjadiRp15.800.

Implementasikan/reuse satu konstantaotoritatif domain dengan desimal eksak. Source saat diperiksa `FxExchangeService` memakai currentMidRate15800.00, reset, restoreState dan setMidRate; telusuri seluruhcaller serta FX/valuasi/margin/risiko/AUM/multiaset/save/replay. Jika rate sebenarnya sudah tetap, cukup tegaskan kontrak dan hapus jalur perubahan yang bertentangan tanpa membangun ulang servis. Tidak ada tabelkurskedua diJS atau networkratefeed.

- Kurs acuan tidak bergerak karena tick/hari/berita/background/save-load atau UI. Gunakan kurs yang sama pada konversi, valuasi dan semua akun sesuai kontraknya.
- Fee/spreadexisting tetap diperlakukan sebagai biaya terpisah yang dijelaskan pada tinjauan tukar uang; jangan mengubahnya diam-diam atau menjanjikan nominalnet selalu identik dengan konversibruto. Harga sahamUS masih dalamUSDdan bergerak mengikuti pasar game. Nominal rupiah dari valuasi mengikuti jumlahaset/harga terbaru padarateacuan tetap.
- Riwayatreceipt/ledger/biayalama tidak ditulis ulang ketika mengunci rate. Untuk save lama yang menyimpan rateberbeda, buat backup/migrasi yang aman dan batalkan quote usang secara tepat; jelaskan perubahanvaluasi nonkas tanpa menciptakan/menghapus uang. Jangan menghilangkan receiptidempotency atau memaksa resetpemain.
- Informasi kurs tidak perlu mengisi halamanutama, tetapi jika dibutuhkan untuk keputusan tukarIDR/USD tetap dapat dilihat pada review/detail perhitungan dengan bahasa pemain (`Kurs tetap permainan`), beserta biaya/net. Jangan menyembunyikan biaya atau hasil tukar.

Acceptance: kurs acuan selalu15800sebelum/sesudahhari/berita/resume/load; nilai valuasibruto1USD=15800IDR dan sebaliknya dengan presisipembulatan yang benar; hasilnet cocok setelahbiayaexisting; UIbariskurs yang ditandai hilang; saldo tetap berubah karenaBUY/SELL/FX; pribadi/perusahaan/klien tidak tercampur; save lama dan retry tidak menggandakan perubahan.

## 3. Integrasi, pengujian dan hasil penyerahan

Kerjakan satu hasil terukur→tes→review→checkpoint→lanjut. Nontrivial logic meninggalkan runnable regression minimum yang mendeteksi kegagalan sebelumperbaikan. Gunakan runner existing, bukan framework baru. Source/test/QA perlu konsisten dengan rasio15menit; jangan mengubah expectation agar bug konversi tersembunyi. Regresi katalog, ekonomi, clock, save dan bridge yang terdampak wajib dijalankan; standar10×100aksi finansial dan benchmark perjenis pada mandat existing tetap berlaku sesuai jalur terdampak.

Review terpisah menilai delta DAN kecukupan buktiT1–T11, termasuk keenam komentarbrowser yang dipetakan padaT7–T11. Reviewer tidak menutup temuan hanya dari tulisan `sudah selesai`. Jika kemampuan reviewer terpisah tidak tersedia, jelaskan batasnya. Jangan mengklaim source/teshost/browser sebagai pengujian Android fisik.

Simpan bukti run baru di `Artifacts/Revisi-Pemilik-UX-2026-10-22/<run-unik>/`, tanpa menimpa baseline/pengujian yang sedang berjalan. Update tracker/checkpoint berdasarkan bukti terbaru, catat keputusan waktu15menit sebagai perubahan pemilik, dan pertahankan riwayat temuan. Penyerahan:
1. Preview lokal source terbaru yang bisa dibuka pemilik, memakai save terpisah dan labelpratinjau. Jika harus mengganti server8792 yang sedang dipakai pemilik, tawarkan URL/port baru dan jelaskan; jangan diam-diam mereset savepreview atau menabrak proses.
2. APK kandidat baru sesudah source stabil: verifikasi ZIP/appID/version/signing/byte aset/katalog/QA excluded; path, ukuran aktual, SHA256 dan source/compiled fingerprint konsisten. Pertahankan APK baseline dan save pribadi.
3. Laporan Indonesia dengan statusT1–T11, perubahan nyata, expected/actual clock, kategori Koleksi, contoh penggantian bahasa, berita/sentiment dan timing publikasi, tabel ketiga referensi beserta screenshot sebelum/sesudah, grafik yang benar-benarterrender/provenance/profil dan tradecoverageperemiten/rekonsiliasi101/kurs tetap dan migrasi, hasiltes/performa/reviewer, asetpending/nativeNOT_TESTED dan next_action.

Selesai untuk scope ini hanya jika kriteriaT1–T11 yang applicable terbukti dan tidak ada bug terbuka diketahui. Jika ilustrasi wajib masih menunggu Codex atau cakupandataasli belum terbukti, bedakan subpekerjaan yang lulus dengan keseluruhanmandatPARTIAL dan blocker spesifik. Jangan menyebut14/14 atau finalproduk hanya karena11task ini lulus.

Jangan berhenti karena durasi/iterasi/giliran/token atau satu tes hijau. Dua kegagalan sama tanpa diagnosis baru: kirimPERLU PERHATIAN dan ganti pendekatan. Tiga: kerjakan bagian independen atau BLOCKED bila membutuhkan tindakan luar. HormatiSTOP/pause dan checkpoint aman.

Mulai sekarang dengan memverifikasi baseline/sumber, membuka ketiga gambar, dan menyimpan kondisi before; lalu implementasikanT1–T11 sesuai dependensi. Pertahankan bagian yang sudah terbukti selesai, jangan mengulangnya hanya karena nomor tugas lama. Tidak berhenti pada pembuatan rencana.
