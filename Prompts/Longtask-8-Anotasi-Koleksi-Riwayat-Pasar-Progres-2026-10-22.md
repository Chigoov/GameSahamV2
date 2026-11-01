# Long task Hermes — anotasi, Bisnis, Profil, Pasar dan hibah harian dunia nyata

Saya pemilik proyek. Implementasikan delapan anotasi berikut dari preview terbaru. Dua anotasi terdahulu adalah pemisahan koleksi berdasarkan kepemilikan dan pemindahan riwayat/layoutPortofolio. Enam anotasi berikutnya membahas tampilan koleksi sesudahdibeli, harga pasar diam, syarat reservasi berulang, penghapusan penjelasan tanpa tamat dari halamanutama, desain status terkunci, dan pemetaan ulang Liga/Peringkat.

Tambahan pemilik setelah delapananotasi: fungsiBisnis dinilai masih jauh dari siapdipakai pemain; wajib dibuat halaman/menuProfil khusus untuk statistik lengkap; desain daftarPasar/saham belum disukai; harga/datarealtime belum berjalan. A4 diperkuat dan A9Bisnis,A10Profil,A11Pasar ditambahkan. Dengan keputusanhibah berikutnya, dokumen sekarang memuat **A1–A12**. Semua delapananotasi awal tetap berlaku. Nama file dipertahankan untuk kontinuitas tugas, bukan membatasi scope menjadi delapanitem.

Pembaruan berikutnya yang sudah dikonfirmasi pemilik: dana hibah hanya satu kali per tanggal dunia nyata, reset pada **00.00 WIB (Asia/Jakarta)**, bukan per hari game dan bukan cooldown24jam. Task sekarang **A1–A12**. Pemilik juga mewajibkan penuntasan long task dengan verifikasi independen dan gerbang bukti di bagian3. A1–A11 tetap berlaku; A12 dan protokol kualitas ini adalah tambahan terbaru.

Instruksi pemilik paling baru: **daftar pekerjaan, subpekerjaan, acceptance dan milestone tidak boleh dipangkas atau diringkas demi mengurangi jumlahnya; kerjakan milestone satu per satu**. A1–A12 adalah induk requirement, bukan batas12milestone. AturanQ8 di bawah mengatur daftar lengkap, ID stabil, satu milestone implementasi aktif, dan gerbang sebelum lanjut. Aturan ini menggantikan izin paralel lintas-milestone/penyingkatan daftar pada dokumen lama yang bertentangan.

Tugas ini adalah implementasi, pengujian perilaku, review dan penyerahan preview/APK kandidat. Jangan berhenti pada audit atau rencana. Pertahankan hasil yang sudah benar dan keputusan pemilik sebelumnya. Prompt master terdahulu tetap berlaku untuk requirement yang tidak bertentangan; dokumen ini memerinci delapan temuan beserta tambahan kebutuhan Bisnis/Profil/Pasar terbaru. Kata siaprilis pada kebutuhanBisnis berarti kelengkapan fungsi dan kualitas yang dapatdibuktikan, bukan izin publikasi atau klaim perangkatAndroid sudahdiverifikasi.

## 1. Root, baseline dan perlindungan

Root produksi: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`. Repo `Game Trading Saham` berbeda dan bukan tempat menulis/mengimpor source/save/build.

Buktikan cwd, Git root, branch, HEAD, tracked/untracked changes, source/compiled fingerprint, proses/writer serta port/saveQA sebelum perubahan. Saat prompt dibuat HEAD masih49bbafb dengan banyak perubahanlokal source/test/QA/checkpoint belumdi-commit. Jangan rollback keHEAD, reset/clean, menghapus output massal atau commit semua perubahan tanpa inspeksi. Satu writer perfile/sharedstate/buildoutput; tidak ada launcher kedua pada sesi sama. Bila agent/proses lain masih aktif, koordinasikan pada batas aman, bukan menghentikannya berdasarkan dugaan.

Baca AGENTS.md, Work/README.md, konsep/PRD/designbrief, standar kualitas dan checkpoint/STATUS terbaru, serta `Prompts/Prompt-Master-Lanjutan-Game-Saham-2026-10-22.md`. Instruksi pemilik terbaru menang atas dokumenlama yang bertentangan. Pertahankan rasioforeground1×:24jamgame=15menitnyata; background/ditutup24jamgame=1jamnyata; kursacuan1USD=Rp15.800; katalogminimal100asliaktif50IDX50US12kategori; migrasi save/IPO/inventory; isolasi akun dan applicationID/signing.

Save pribadi: `Artifacts/LocalPreview/preview-save/save_game_v2.json`, SHA256baseline `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`. Hash sebelum/sesudah; jika berubah, diagnosis tanpa overwrite. Jangan memakai save pribadi atau preview8766/8792/8793 yang sedang dipakai pemilik untuk tes mutasi/reset. Buat saveQA/run/port terisolasi dan pertahankan artefak baseline.

Java tetap pemilik uang, ledger, order, matching, NPC, harga, clock dan save; HTML/CSS/JS hanya presentasi/input. Jangan migrasi engine/runtime atau membuat mesin finansial kedua. Ponytail: telusuri fungsi dan semua caller; reuse komponen/token/helper/runner, perbaiki rootcause shared, minimumdiff yang benar. Tidak ada framework/eventbus/abstraksi spekulatif. Nontriviallogic meninggalkan runnable regression yang bermakna.

Wewenang: perubahan repo, tests/domain/browser, review, previewlokal, buildAPK dan commitlokal yang ditinjau. Tidak ada push/publish/pesan pihaklain/pembayaran/perubahan globalHermes/provider/model/credentials/scheduler/ADB/emulator/perangkatfisik/repo lain. Reviewer berbeda sesuai kemampuan nyata, ownershipjelas; catat bila tidak tersedia, jangan mengarangreview. Native tetapNOT_TESTED/DEFERRED.

## A1. Galeri menampilkan BelumDimiliki dan SudahDimiliki di area yang ditandai

Target adalah isi halaman/modal GaleriKoleksiMewah, bukan sekadar penambahan tombolfilter. Pertahankan kategoriSemua/Kendaraan/Properti/Perhiasan/Seni&JamTangan. Pada tampilanSemuaStatus, tampilkan dua bagian berjudul **Belum Dimiliki** dan **Sudah Dimiliki**, masing-masing dengan jumlah serta kartu yang benar. Jika pemain memilih hanya satu status, tampilkan kelompok tersebut saja. Seluruh aksesKoleksi dariBeranda/Bisnis/Lainnya memakai renderer/filter sama.

Source awal yang perlu ditelusuri: bridge.getCollections mengembalikan `collections` berisi `CollectibleItem.toJsonObject()` dengan field `isOwned`; renderer/filter terbaru masih memakai `item.owned` pada beberapajalur. Audit seluruhcaller untuk kontrak kategori, kepemilikan, akun, collateral dan formatnominal. Jangan mengganti domain dengan dataUI palsu atau menambah fieldberbeda percaller. Perbaiki pada batas shared yang paling kecil dan konsisten; pertahankan kompatibilitas bila kontrak lama memang perlu didukung.

Jangan tampilkan halaman kosong saat katalog ada. Bedakan gagalambil/parsing dari hasilfilter yang benar-benar kosong. Empty state kelompokDimiliki sebelum ada pembelian cukup singkat; barangBelumDimiliki tetap terlihat. Union kedua kelompok harus sama dengan hasilkategori, tanpa hilang/duplikasi.

Acceptance: freshgame menampilkan seluruh katalogbelumdimiliki, pembelian memindahkan tepatitem keDimiliki, penjualan mengembalikannya, count/filter/kategori benar, sama pada semuaentrypoint dan sesudahsave/load. Itemmilikkliendana/akunlain atau diagunkan tidak dianggap bebasdijual jika aturanmelarang. Preserve kategori, scroll, fokus dan posisi UI setelah pembaruan.

## A2. Pindahkan riwayat dan hemat ruang Portofolio

Tombol `Aktivitas / ledger` di `#asset-summary` adalah entrypoint riwayat; pemilik tidak ingin letaknya di sana. Pindahkan ke menu **Lainnya → Riwayat Transaksi**, dengan nama bahasaIndonesia yang jelas. Jangan hanya menyembunyikan atau menghapus fungsi. Hilangkan labelledger dari alur pemain; rincianinternal untuk diagnostikopsional bila memangperlu.

Riwayat menggunakan data ledger/orderexisting, akun yang jelas, urutan/tanggal yang tepat dan detail yang dapatdibuka. Jangan menganggap semuaentriledger adalah transaksiBUY/SELL; bedakan transaksi, biaya, pendapatan, transfer dan reservasi. Tampilkan nominaldengan currency, debit/kredit, hasil/status yang tepat. RequestID tidak memenuhi baris utama. Akunpribadi/perusahaan/klien tetap terpisah. Reuse renderer history existing, jangan membuat riwayatpalsu atau sistem pencatatanbaru.

RapikanPortofolio: prioritas nilaiaset, kas, kepemilikan, dan tindakan utama. Statistik lengkapterbaik/terburuk/winrate/ROI dipusatkan di halamanProfilA10, dengan ringkasan/pintasan kontekstual bila berguna; jangan membiarkan empatkartu kosong menghabiskan layar sebelum kepemilikan. TukarIDR/USD dan aksi sekunder tidak menjadi deretan kartu tombolbesar. Kurangi padding/gap/duplikasi secara terarah menggunakan token bersama, bukan mengecilkan teks atau touch target. Pertahankan fungsi yang dipindah, field/domain dan updaterlive; jangan membuat selector/test patah karena memindah elemen.

Acceptance: entrypointRiwayat tersedia diLainnya, tidak lagi memenuhi ringkasanPortofolio; history menampilkan databenar untukakunaktif; fungsiFX dan detail tetapbekerja; bukti sebelum/sesudah pada438×627 dan viewportstandar menunjukkan kepemilikan lebih mudahdicapaidengan scrolllebihsedikit pada data/fontyang sama, tanpa clipping dan targetkurang48dp.

## A3. Koleksi yang dibeli memiliki tampilan berbeda

Di kartu koleksi, pemilik ingin segera melihat barang sudahdibeli. Kepemilikan bukan hanya perubahan kalimat yang sulitdicari.

Desain dua state konsisten: BelumDimiliki dengan status Tersedia dan CTA Beli; SudahDimiliki dengan badge **✓ Dimiliki**, penanda visual permukaan/border/ikon yang masihselaras tema, dan tindakan LihatDetail/Kelola atauJual yang sesuai. Gunakan teks+ikon, bukan warna saja. Jangan membingungkan badgekelangkaan dengan kepemilikan. Item diagunkan/tunggakan/pembelianpending/ditolak diberi state yang benar dan tidak terlihat sukses sebelum domainkonfirmasi.

SetelahBUYsah, kartu, countkelompok, saldo dan aksi berubah otomatis tanpa reload; setelahSELL kebalikannya. Fokus setelahkartu berpindah menuju targetyang masukakal tanpa melompat keatas. Retry/doubleclick tidak menagih dua kali. Biaya, reputasi, kepemilikanakun dan ledger tetapdomain; efekvisual tidakmengubahnominal.

Acceptance: before→BUY→owned→reload/save-load→SELL→unowned, serta dana kurang/pending/ditolak/collateral. Assert fielddomain, kelompok, badge, CTA, saldo dan count; screenshot kedua state dibuka. Tidak cukup hanya toastpembelian.

## A4. Harga pasar harus hidup melalui engine yang sudah ada

Pemilik melihat jam berjalan pada sesi terbuka tetapi harga/indeks tetap0,00%dan menanyakan perlunya engine lain. Source sudah mempunyai MarketEngine, MatchingEngine, OrderBook, NPC dan pencatatanTradeFill. Jangan menyimpulkan perlu enginegrafis/runtime lain atauAPIhargaluar sebelum diagnosis. Yang diminta adalah pasar game berjalan dan UI mengikuti hasilnya, bukan mandat baru livefeedbursa atau brokeruangnyata.

Telusuri dan rekam satu rantai bukti: elapsednyata→GameClock→gerbangsesi/tick→aktivitasNPC→order sah→matching/fill→lastprice/previousclose→OHLCV/tape/indeks→snapshotbridge→DOMPasar/Beranda/detail. Bedakan tidakadatick, tidakadaorder, ordertidakmatch, filladatanpapriceupdate, domainbergeraktanpaUIupdate, danreturnbaseline salah.

Source awal: `TradingFacade` memanggil aktivitasNPC pada batas15menitgame saat sesi terbuka dan aktivitasliga pada30menitgame. Periksa kebijakanNPC, dana/inventory, offsetspread, pembatalan/pembaruanquote, ordercrossing, settlement, pembaruanpreviousclose/dailychange, dan updaterindices/movers. Mengutip interval bukan bukti bahwa trade memangterbentuk. Jangan mengubah15menitperhari yang disetujui untuk menutupi harga diam.

Perbaiki rootcause dengan strategi/mekanisme existing. Pasar perlu aktivitas masukakal dariNPCterdanai selama jamaktif tanpa menunggu transaksi pemain pertama. Jika perlu kalibrasi aktivitas/likuiditas, tentukan secara terukur, deterministicseed untuktes, tanpa uang/saham tanpaasal, selftrade terlarang, bypassrisk/lot/tick, atau randomprice langsung dariJS. Jangan menjamin setiapemiten bergeraksetiapdetik: tanpafill/perubahanreference yang sah, harga boleh tetap. Pasartutup/halt tidakdipaksa bergerak.

Pergerakanharga yang sah harus tercermin pada persentaseharian, topmovers, indeksgame, chart/tape, posisi/P&L dan valuasi akun terkait. Formulaindeks dan previousclose bersumberdomain; bukan angkaacak atau nilaiindeksbursa nyata yang diklaimlive. Beri keterangan statepasar/lastupdate yang mudahdipahami bila belumada perubahan.

Acceptance wajib: skenariohost terisolasi dengan sesiaktif1×, tanpaorderpemain, rekam banyakinterval aktivitasNPC dan beberapaTradeFill sah beserta perubahanharga yang dapatditelusuri. Skenariodeterministik mencakup transaksi pada hargaberbeda sehingga positif/negatif/unchanged bisa diuji; jangan memaksa semuanya terjadi pada satuemiten tanpa sebab. AssertDOM sesuai domain dan commit→tampilan, chart/tape bertambah, ledger/inventory/cashconservation benar. Uji closed/pause/halt/resume/bothbursa dan save/load, serta scenariotanpafill yang memangtetap. Jika sourcehargamengalami limit aktual, jelaskan buktinya dan solusi minimum, bukan migrasiengine tanpaalasan.

**Gerbang wajib yang diperkuat pemilik:** ini masalah berulang. Sebelum menyebut A4/A11 atau hostselesai, sertakan trace bertimestamp dan sourcefingerprint yang membuktikan harga terakhir/riwayattransaksi benar-benar berubah pada sesi aktif normal1× melalui jalurproduksi, serta UI yang dibiarkan terbuka mengikutinya. Tunjukkan contohIDXdanUS saat masing-masing sesi sah, bukan membukabursa secara paksa. Jangan memakai screenshot angka0,00%, hitunganclock berjalan, mock tick, endpointQAyang menulis harga, getSnapshot yang berubahtimestamp saja, animasiCSS atau dua nilai berbeda hasilseed sebagai bukti realtime. Setupdeterministik boleh tetapi langkah penerimaan pasar berjalan melalui jalurnormal. Jika belumberhasil, catatHOST_PARTIAL dengan akar masalah/hasil yang terukur dan lanjutkan bagianindependen; jangan mengalihkan alasan menjadi menunggu gambar atau menyarankan Unity/Flutter/enginebaru tanpa bukti.

## A5. Perbaiki syarat reservasi yang berulang dan ruang lingkupnya

Pemilik menandai kalimat `Selesaikan seluruh reservasi transaksi` muncul berkali-kali. Ada bug nyata yang harus diperbaiki diakar, bukan hanya difilterUI.

Source awal `TradingFacade.getPrestigeStatus()` sekitar2742–2770: loop `accounts.values()` dan semuaCurrency menambahkan kalimatreservasi/margin perakun/matauang; pengecekanrestingorder juga mengambil seluruhmarket. RendererLainnya menampilkan seluruhblocker apaadanya. Audit apakah akun/orderNPC ikut mengunci perjalanan pemain dan apakah kewajiban benar-benar berada dalam kontrolpemilik. Periksa juga blockerfund/mandat/pinjaman/payroll/pemeliharaan yang berulang atau scope-nya salah. Telusuri callereligibility dan confirmPrestige agar keduanya sama.

Agregasikan hanya kewajiban yang relevan menurut aturanreset/ownership pemain, dengan satu blocker perjenis yang bermakna; rincian akun/currency/jumlah/order dapat dibuka bila dibutuhkan. Dana/orderNPC ataupihaklain yang tidak berada dalam cakupan reset pemain tidak menjadi syarat yang mustahil diselesaikan pemain. Jangan menghapusreservasi, membatalkanNPCmassal, melemahkan validasi atau meluluskanprestige untuk membuat pesan hilang.

Ubah istilah menjadi bahasa pemain, misalnya `Masih ada dana tertahan pada order aktif`, dengan tindakanLihatOrderyang relevan. Jika penyebabnya pendingfunds berbeda, jelaskan sesuai kontrak; jangan otomatis menyatakan semuareservedcashbisa dibatalkan.

Acceptance: banyakakun/banyakcurrency menghasilkan satu ringkasanperjenis dengan rincianakurat; akunpemainbersih+NPCpunyareservasi tidakmemblokir karenaNPC; order/dana/kewajiban pemain yang nyata tetapmemblokir; selesai/cancel/settlement sah menghilangkan blocker; tidak ada duplikasi setelahrefresh/pindahtab/load; confirmPrestige tetap memvalidasi ulang dan idempotent. Semua kasus memakai QAterisolasi.

## A6. Hapus penjelasan PrinsipTanpaTamat dari halamanutama

Hapus baris **Prinsip tanpa tamat** dan paragraf panjang tentang identitas/preferensi/statistiklifetime/riwayatera dari kartu utamaLainnya yang ditandai. Pemilik tidak membutuhkan uraian desaininternal tersebut di menuutama.

Pertahankan fitur perjalananbaruyangopsional. Informasi yang penting untuk keputusan reset tetap berada pada layar tinjauan/konfirmasi: apa yang hilang, dipertahankan, syarat, backup/recovery dan pilihan batal. Jangan menghapus perlindungandata atau menjalankanreset otomatis. Jangan memindahkan paragrafpanjang ke kartutetangga sehingga halaman tetapboros.

Acceptance: kalimat yang ditandai tidak ada pada halamanutama; jalurtinjauan masih lengkap, batal tidakmengubahsave, doubleconfirm/retry tidakmenggandakanrun; seluruhresettest hanya padaQA.

## A7. Desain ulang status terkunci dan persyaratanEraBaru

Ganti baris dua-kolom `Terkunci — Perlu Portfolio Manager` yang terjepit dengan kartu ringkas yang jelas: judul **Era Baru**, badgeikon+teks **Belum Terbuka**, dan syaratprioritas satu-duabaris. Jika jabatan diterjemahkan menjadiManajerPortofolio, tetapcocokkan identitasrankdomain. Jangan hardcodeCIO bila requiredRankdomain adalahPortfolioManager. Currentrank, requiredrank dan statuskelayakan harus konsisten.

Saat terkunci, gunakan tindakan **Lihat Syarat** yang membuka checklist terkelompok/ringkas, menghitung progres dari state nyata tanpa angka rekaan. Tampilkan persyaratan unik hasilA5; kewajiban yang bisa ditindaklanjuti mempunyai navigasi yang benar. Bukan giantdisabledCTA atau daftarblocker panjang diLainnya.

Saat eligible, tindakan yang jelas membuka tinjauan, tidaklangsungreset. Biaya/benefit/reset/retained dijelaskan di tempat relevan, bukan desaininternal dihalamanutama. State locked, partlymet, eligible, loading/error dan prosespermintaan perlu terbaca pada320px/font200%, status tidakbergantungwarna. Thresholdsyarat tetapdomain, bukan diubah agarUIDemo terbuka.

Acceptance: rankbelumcukup, waktubelumcukup, kewajibantersisa, eligible dan retry; konteks tetap setelahkembali; UI tidakmengklaim siap bila confirmdomain menolak. Screenshotsbefore/after dan checklistdata cocokdomain.

## A8. Petakan ulang Liga/Peringkat dan hierarkiLainnya

Pemilik meminta desain LigaKonglomerat/PeringkatFinansial dipikirkan ulang. Lainnya menjadi menu kelompok yang mudahdipindai, bukan dashboard panjang dengan semua tabel/kartuhero terbuka. Pertahankan akses profil, misi, liga/rival/peringkat, kalender/riset, riwayat danpengaturan yang existing; tidak menambah tabutama atau menghilangkan fitur.

Liga/Peringkat diLainnya cukup entrypoint/ringkasan yang proporsional: label **Liga & Peringkat**, posisi pemain, konteks jumlahpeserta dan tindakanLihatPeringkat. Daftarlengkap berada dihalaman/detailkhusus dengan headerjelas, ringkasan pemain, posisi peringkat, nama peserta dan kekayaan yang sejajar. Highlightbarispemain dengan teks/ikon/border, bukan warna saja. Top3bolehditampilkan hanya jika tidakmenduplikasiherobesar; fullranking tetapdapatdiakses dan di-scroll normal. Rival/target/perubahanperingkat hanya bila domainmenyediakan, tidakmengarangtrend.

Gunakan label yang jelas bahwa peringkat adalah di dalam game bila konteks nasional berpotensi disangka peringkat orangnyata. Nilai kekayaan dan sort/rank berasaldomain, tidakmengganti rumus atau mengejarangka contoh. Pisahkan uangpribadi, dana-klien/AUM dan metrik yang benar menurutaturanperingkat. Waktuupdate/periodejelas. Status locked/empty/error diberi alasan danjalanberikutnya.

Tetapkan mappingelemen→lokasi→prioritas→data→state sebelum implementasi, lalu langsung kerjakan. Reuse rowlist dan token, kurangi lapisan kartu besar/padding/gap. Teks/angka/currency tetap terbaca; target48dp tidakdikurangi. Pertahankan navBack, fokus, scroll dan konteksakun.

Acceptance: nilai/rank/sort sesuai domain, barispemain mudahditemukan, aksesdetail/backberfungsi, perubahanrank sah terlihat tanpareloadmanual, stateempty/locked/error benar, halamanLainnya lebihringkas pada datadanfontyang sama. Ukur posisi/scrollreach dan inspeksiscreenshot, jangan menilai dari jumlahbariskodeCSS.

## A9. Tuntaskan fungsi Bisnis sampai dapat digunakan pemain

Pemilik menandai kartuusaha pada `#firma-subtab-content > section.card > article.card` dan menilai fungsi masih jauh dari kesiapanrilis. Ini bukan tugas menambah warna/padding saja. Audit dari daftarusaha→detail→aksi→hasilfinansial→laporan→save/load. Sourceawal pada renderUsaha membuat tombolStatusUtama/DivisiSDM/Fasilitas&Moral/Likuiditas; buktikan setiap tombol mempunyai perilaku dan konten sesuai domain, bukan pilldekoratif/selectedstate tanpafungsi. Tombol yang tidak didukung tidak boleh menyamar sebagaifitur jadi.

PemetaanUI:
- Daftar **Usaha Saya** menampilkan kartu ringkas: nama, jenisusaha dalam bahasapemain, level/statusoperasional, kas dan laba/arus-kasharian yang dihitungbenar. Jangan mengklaim pendapatan dikurangpayroll sebagai laba bersih bila biaya lain belumdiperhitungkan. Data unavailable tidak ditutup dengan CEO/revenue/staff dummy.
- Klikperusahaan membuka halaman/detail khusus dengan headernama dan navigasilokal **Ringkasan, Tim, Fasilitas, Keuangan** atau padanan ringkas yang benar. Tombol dapat disentuh tanpa terpotong kiri/kanan atau bergantungscrollhorizontal yang tidakjelas pada438×627/320px/font200%. Pertahankan lima tab utama; tidak ada dashboard semuaaksi dalam satu karturaksasa.
- Ringkasan berisi keadaan usaha, kinerja terbaru dan tindakan berikutnya. Tim menampilkan pegawai/peran/biaya/status dan aksimanajemen yang tersedia. Fasilitas menampilkan kebijakan/jamkerja/benefit/moral yang benar serta efek yang didukungdomain. Keuangan menampilkan kas, modalmasuk/keluar, pendapatan, biaya, payroll, pinjaman/tunggakan dan laporanperiode yang dapatditelusuri. Gunakan linkdetail untuk informasisekunder, bukan mengulang seluruh baris di semuatab.

Tuntaskan jalurexisting yang applicable: mendirikanusaha dengan modal sah, merekrut/mengelolapegawai dan CEO jika fiturada, mengubahkebijakan/fasilitas, upgrade dengan biayatervalidasi, suntik/tarikmodal atau distribusi yang dibolehkan, pendapatan/biaya/payroll padaperubahanhari, pinjaman/tunggakan sesuaiaturan, dan laporanoperasional. Jangan menambah sistemmanajemen baru yang tidak diminta bila fungsi existing mencukupi. Tiap tindakan memakai review/konfirmasi bila berdampakuang, validasi dana/ownership/prasyarat, statusprocessing, hasil/error dan idempotency.

Tidak boleh ada perpindahan modal yang menciptakan uang, penggunaan uangklien sebagaipribadi, biaya dibukukandua kali, hasilmenyesatkan, tombolmati tanpaalasan, atau laporan yang hanyamenyalin nilai awal. Perusahaanbaru harus langsungmuncul; aksi/pergantianhari memperbarui layar yang tetap terbuka, termasuk detailperusahaan dan ringkasandaftar, tanpa kehilanganpilihan/subtab/fokus/scroll. Namausaha/pegawai berasalinput/domain ditampilkan aman sebagai teks, dibatasi ukuran sesuaivalidasi, tidak disisipkan sebagaiHTML; tidak mengganti namauser secara sepihak.

Acceptance perjenisusahaexisting: permainanQA mulai→dirikan→kelolatim/fasilitas→upgrade/transfer yang sah→pergantianhari→laporan→simpan→muat→lanjut. Assert expected/actual saldo pribadi/perusahaan, danareserve, biaya/pendapatan/payroll/laba sesuai definisi, level/pegawai/moral dan ledger. Cakup danakurang, locked, pending, gagal, retry/doubleclick, tunggakan dan bataswithdrawal. Auditdate/settlement saat waktuberganti/SkipDay dan backgroundcatchup agar proses tepatsekali. Sertakan matriks kontrol→aksi→domainmethod→prasyarat→hasil→regression; tidak ada kontrol yang ditutup hanya dengan screenshot visible. Jika gatehostBisnis gagal, A9 tetapPARTIAL, meskipun desainsudahbagus.

## A10. Buat menu dan halaman Profil khusus dengan statistik lengkap

Pemilik meminta **menuProfil sendiri**, bukan statistik yang tersebar diPortofolio atau sekadar nama padaheader. Buat halaman **Profil Saya** dengan entrypoint jelas dari ikon/nama/avatarheader dan menuLainnya. Pertahankan5tabutamaexisting, tidak otomatis menambah tabkeenam. HalamanProfil mempunyai identitas, judul dan jalurBack sendiri; aksesnya tidak bergantung pada akuntradingaktif atau levelkarier tertentu.

Reuse PlayerProfileManager, getPlayerProfile, career/progress/performance dan datadomainexisting. Audit field/definisi sebelum menampilkan. Susun beberapa bagian yang dapatdipindai atau subtab/detailringkas:
1. **Identitas & Karier:** namapemain, jabatan, level/XP/progres, hariperjalanan dan perjalanansekarang. Editnama/preferensi bila existingfiturmendukung; validasi/persistensinya tetap. Jangan mengarang tanggalmulai/avataridentitas.
2. **Ringkasan Keuangan:** kekayaanbersihpribadi, kasIDR/USD, aset/posisi/bisnis/koleksi dan utang/kewajiban yang relevan. Dana-klien/AUM terpisah dan diberi nama jelas; jangan menggandakan totalaset atau menyamakan kasdengankekayaan/laba. LinkdetailPortofolio/Bisnis/Riwayat tanpa menyalin semuaUI.
3. **Statistik Trading:** transaksi/order/fill/posisiditutup menurut definisinyamasing-masing; profit/loss/breakeven, winrate beserta penyebut, best/worsttrade, P/Lterealisasi dan belumterealisasi, biaya jika tersedia, return/periode. Jangan menghitung satuorderpartial sebagai banyakkeberhasilan tanpa kontrak jelas, menjumlahcurrencymentah, atau menyebutgrosssebagai net. Noltransaksi menampilkan BelumAdaData/N/A yang jujur, bukan0%menyesatkan.
4. **Performa & Pencapaian:** harian/mingguan/bulanan atau periodeexisting yang benar-benar tersedia, rekor/peaknetworth, misi/achievement, posisiLiga dan linkdetail. Jangan membuat grafikhistori/dekoratif/random atau mengarang metrik untuk mengisi bagian.
5. **Perjalanan Sekarang & SepanjangPermainan:** bedakan runmetrics dan lifetime, resetprestige yang sesuai, riwayatera yang sah; tidak mencampur nilairunbaru denganstatistiklama. Informasipanjang/pengaturanreset tetap dalam detail/konfirmasi, bukan memenuhi profilutama.

Statistiklengkap berarti cakupandatatersedia yang benar dan terhubung, bukan kewajiban mengarang field yang belumdidukung. Buat tabel metric→definisi→sumber→currency/periode→kapanupdate→run/lifetime→empty/lockedstate. Jika metricpenting belumdisediakan domain, tambahkan minimal pada existingservice/bridge dengan regression sesuai kebutuhan, bukan mesinanalitikJSbaru. Statistik setelahsettlement/biaya/pergantianhari/save-load harus diperbarui saat Profil tetap terbuka; querytidakmenghasilkan XP/reward/financialsideeffect.

Acceptance: pemainbaru/nohistory, tradeprofit/loss/breakeven/partial/fee, IDRdanUSD, perubahanhari, player/clientcontext, save/load dan erabaru diQA; semuaangkaprofil cocokdomain dan definisi. Menukarakuntransaksi tidakmengganti identitaspemilikprofil atau mencampurdana. Cekaksesheader/Lainnya, Back, namapanjang, font200%, scroll/fokus, serta tidakada duplikasistatistikboros diPortofolio sesudahdipindah.

## A11. Rancang ulang menu Pasar/daftar saham dan integrasikan realtime

Pemilik belum menyukai bentuk daftar saham dan menegaskan datagame belumrealtime. Terapkan redesign menyeluruh pada hierarkiPasar dan row saham dengan data yang sama, bukan sekadar gradient/iconbaru. Buka kembalitiga referensigambar pemilik pada `Docs/Design/References/Owner-2026-10-21/` menggunakanvision; gunakan gambar2untukPasar/detail dan gambar1/3untuk hierarkidaftar. Pertahankan satu token dan5tabutama, jangan membuat tema baru atau mengadopsifiturcrypto.

Susunan yang ditargetkan:
- HeaderPasar ringkas: status sesi yang akurat, lastupdatewaktugame yang benar dan tindakan yang relevan. StatusBelumAdaPerubahan hanya kalimat/konteksringkas, bukan kartuhero kosong yang mendominasi.
- Ringkasanindeks yang proporsional/collapsible dengan labeljenis/metrikyang benar. Jangan menamai indeksmodelsebagai indeksbursa live. Tidak membuang hampir satu layar pada kartuindeksbesar dan scrollhorizontal yang tidakjelas.
- Pencarian ticker/nama, pilihan Semua/Pantauan dan filterbursa/kategori yang ringkas. Jika memakai sheet/filterpanel, selectedstate/jumlahfilter/clear/apply/back harus jelas dan keyboardaccessible. Jangan menghilangkan kemampuanfilter atau menduplikasi banyakselectbesar.
- Daftarsaham menggunakan row/karturingkas dengan identitas ticker/nama/bursa, harga+matauang, perubahanpersen/nilai sah dari domain, status/timestamp relevan dan watchlist. Angka sejajar, nama panjangmembungkus secara wajar, targettouchminimal48dp, tanpa gap/paddingraksasa. Sparkline hanya bila historysahada; bukan dekorasi yang menggeserangka. Buka detail dan CTAtrading secara natural sesuai flowexisting.
- Pengurutan/search/filter/pantauan tetap pada posisi pilihan pemain saat quoteupdate. Jangan menyisipkan/sortulang setiaptick hingga baris melompat saat akan diklik; perpindahankarena data baru diberi perilaku stabil yang sesuai. Bedakan emptyfilter, loading, error, closed, halt, stale dan belumadatrade.

Harga, persentase, topmovers, indeks dan sparkline mengikuti domain dengan acceptanceA4 yang diperkuat. Refreshhalaman, ganti tab, atau mencetPerbarui bukan prasyarat melihat harga hasiltransaksi baru. Updatehanya fieldyangberubah/strategiexisting yang aman, bukan rebuildseluruhlist tiapdetik. Animatedflash bila ada singkat dan respectsreducedmotion; animasiharga bukan buktirealtime.

Acceptance: before/after pada438×627dan7viewport/fontnormal200%; daftar100emiten, query/bursa/kategori/watchlist/gainloss-sort dan nama/harga panjang, callbackinstrumen tetapbenar saat barisupdate. Pada sesisah1×, jalurNPC→fills→domainquote→Pasar/Beranda/detail/chart/Profil semuanya sinkron dengan sourcefingerprint/timestamp yang sama; masih0,00%tanpa penjelasan atau screenshotstatis tidak meluluskan. Kenaikan/penurunan/unchanged diuji dari kejadian domain nyata, bukan randomUI. Performa minimal30sampel peraksi yang relevan dan latencycommit→DOM sesuai standar. Screenshotharusdibuka/dibandingkan referensi; hasilUIterlihatbagus tetapi gateA4gagal berarti A11/hostbelumselesai.

## A12. Hibah harian reset pukul00.00 WIB dunia nyata

Keputusan eksplisit pemilik: **satu klaim hibah per profil pemain pada setiap tanggal dunia nyata di Asia/Jakarta, tersedia lagi pukul00.00 WIB hari berikutnya**. Ini reset kalender, bukan harus menunggu24jam sejak klaim. Contoh: klaim22Oktober23.59WIB sah; klaim23Oktober00.00WIB boleh lagi. Jumlah hibah tetap nominalexisting Rp25.000.000 kecuali ada instruksi baru; jangan sekaligus mengubah balancing.

Source awal `TradingFacade.claimDailySponsor` sekitar2651–2666 memakai `clock.getCurrentDate()` untuk todayKey dan ledger. Karena itu bergantinya tanggalgame dapat membuka klaim baru. Telusuri semua caller, status/tombol, mutasi ledger, save, profile, reset dan prestige; perbaiki shared domain, bukan hanya disablebutton.

### Kontrak waktu dan cakupan

- Tanggal eligibility dihitung dari waktu dunia nyata dengan zona tetap `Asia/Jakarta`, bukan tanggalgame, UTCdefault atau timezone perangkat yang berubah. Percepatan1×/2×/5×, pause, SkipDay, replay, backgroundcatchup dan pergantianharigame tidak membuka klaim lagi pada tanggalWIBnyata yang sama.
- Gunakan sumber waktu nyata yang dapat diuji, reuse mekanisme existing atau `java.time.Clock` dengan perubahan minimum. Jangan mengubah jam OS/global untuk tes, membuat frameworkwaktu baru, atau menyatukan clockhibah dengan clocksimulasi.
- Eligibility berlaku pada profil pemain lokal yang sama, tidak per akuntrading/tab/run. Berpindah pribadi/perusahaan/klien, reload, restart, loadsave atau prestige tidak membuat hak klaim kedua. Hibah masuk hanya ke akun pribadi sesuai domain. Tidak ada mandat baru authcloud/crossdevice.
- NextClaimAt adalah tengahmalamWIB berikutnya. Setelah beberapa hari tidak bermain, pemain tetap memperoleh satu klaim untuk hariini; tidak ada penumpukan/autoklaim hari yang terlewat. Memasuki hari baru mengaktifkan hak, bukan otomatis mengkredit saldo tanpa aksi klaim.
- Tidak boleh melabeli jamperangkat offline sebagai waktu server terpercaya. Catat batas nyata terhadap manipulasi jam atau pemulihan salinanbackup lama; jangan menjanjikan kebalcheat absolut atau menambah backend/layanan baru tanpa izin. Eligibility minimal tidak boleh dibuka ulang hanya karena waktu/tanggaldibaca mundur pada profil yang sama. Penanganannya aman dan tidak menghapusprogres.

### Persistensi dan idempotency finansial

Gunakan identitas klaim perprofil+tanggaldunianyata dan receipt request yang sah. Doubleclick, duapermintaan berbeda pada hari sama, duaentrypoint/tab atau retry saat responsputus hanya menambah uang sekali. Retryrequest sama mengembalikan hasil/keadaan yang konsisten, bukan kredit ulang atau notifikasigagal yang menyamarkan klaim yang sudah sukses.

Saldo, receipt/ledger dan penanda hari klaim dipersistenkan melalui transaksi/saveexisting yang aman. Jika save gagal atau proses terputus, gunakan rollback/recoveryexisting agar penanda dan uang tidak terpisah; jangan mengembalikan sukses sebelum kondisi persistensi yang diwajibkan terpenuhi. Uji crashwindow yang didukung runner. Jangan memakai localStorage/JSflag sebagai otoritas klaim.

Penanda eligibility harus bertahan pada batas normal reset/perjalanan yang relevan tanpa mengubah riwayatuang. Save lama mungkin hanya punya keytanggalgame, bukan tanggalnyata. Audit bukti yang tersedia dan buat migrasi satu kali yang aman dengan backup/policy tercatat; jangan mengarang pemetaan game-date→real-date, menarik kembali hibah lama, menulis ulang ledger atau memberi hibah baru pada setiapload. Jika eligibilitylama tidak dapat ditentukan, pilih kebijakan konservatif yang dijelaskan dan diuji tanpa menghapusdata; jangan menyebut tanggalgame sebagai tanggalWIBnyata.

### UI dan status

Semua entrypoint hibah memakai statusdomain yang sama, termasuk kartuBeranda, quickaction dan Profil jika ditampilkan. Sebelumklaim: `Klaim Hibah Harian`. Sesudahklaim: `Sudah Diklaim Hari Ini` dengan keterangan `Tersedia lagi pukul00.00 WIB` atau tanggal berikutnya jika diperlukan. Nominal notifikasi dari hasildomain, bukan hardcodeUI yang menyimpang.

Jika aplikasi tetap terbuka melewati tengahmalamWIB, status tombol diperbarui tanpa reload/pindahhari game dan tanpa kreditotomatis. Countdown bila ada hanya presentasi berdasarkan nextClaimAtdomain, bukan otoritas eligibility. Jika tidakperlu, cukup tekswaktu berikutnya. Error/pending tidak memberi toast sukses; polling ulang tidak mengumumkan klaim yang sama berkali-kali.

### Acceptance wajib A12

Dengan real-clock terkontrol diQA, buktikan: klaimpertama sah; klaimkedua requestbaru/sama pada tanggalWIBsama tidak menambah uang; retry responsputus; doubleclick/konkurensi; UI dan ledger cocok; save/load/restart tetapmenolak klaim kedua; pergantianhari game/SkipDay/percepatan/replay tidak membuka; prestige/resetalur normal tidak membuka; aplikasi melewati00.00 mengaktifkan tepat satu hak baru.

Uji batas23.59.59WIB→00.00.00WIB, termasuk instantUTC16.59.59→17.00.00 pada tanggalUTCyang sama; tanggalWIB sudah berganti. Cakup pergantianbulan/tahun, timezoneperangkatberbeda, waktu dibaca mundur, beberapa haritidakaktif tanpa backpay, savelegacy dan gagalpersist. Assertion saldoΔ tepatRp25juta satu kali perhari, jumlahentri/receipt dan nextClaimAt sesuai. Tes yang menggunakan clockgame sebagai waktunyata atau hanya memeriksa tomboldisabled tidak meluluskanA12.

## 2. Pengujian, bukti, review dan penyerahan

Urutan kerja mengikuti dependency: reproduksi/kontrakdata dan traceA4+A12; A1+A3; A2+A8+A10; A5+A6+A7; A9 fungsiBisnis; A11 desainPasar dengan A4 sebagai gate; A12 waktu nyata/persistensi/status; integrasi/regresi/review/build. Tanda pengelompokan di sini menunjukkan hubungan requirement, bukan izin menggabungkan seluruh pekerjaan menjadi satu milestone atau mengimplementasikan banyak milestone sekaligus. Turunkan menjadi daftar lengkap terurut lalu kerjakan satu per satu menurutQ8. Diagnosisrealtime tidak ditunda sampai akhir dan tidak diganti sekadarredesign. Multi-agent hanya membantu milestone aktif dengan ownership aman. Jangan menambahframeworktest atau menulis ulang seluruhUI yang sudahbenar. Protokol verifikasi bagian3 wajib dipakai.

Setiaptemuan mempunyai reproduksi, akar masalah, fungsi/caller, perubahan, assertion yang gagal pada kasuslama dan lulus pada sourceakhir, screenshot/bukti serta hasilreviewer. Sourcehint dalam prompt bukan kelulusan otomatis. Jangan menyembunyikan bug dengan fixture/DOMpalsu atau bypass produksi.

Gunakan domainJava nyata dan saveQA terisolasi; fixture untuk setup diberi label, bukan bukti runtimeAndroid. Semua tesmutasi tidakmemakai savepreview pemilik. Jalankan regresi domain/browser relevan dan standar10×100aksi untukjalurfinansialterdampak menggunakan runnerexisting. A5 eligibility/confirm dan A4 settlement memerlukan pemeriksaaninvariant, bukan hanyaUItest.

Visual:7viewport320×568,360×640,390×844,412×915,430×932,768×1024,1440×900 × font100%/200% nyata; tambahkan438×627 sesuai anotasi. Screenshotbefore/after dibuka; periksa clippinginternal, kontras, unitangka, header/stickyCTA/bottomnav, fokus/keyboard/Backbrowser, preservedfilters/scroll, reducedmotion/powersave dan target48dp. Lebardokumen kecil saja tidakmembuktikan rapi. Semua state yang berubah diperiksa, bukan hanya halamanroot.

Performa: minimal30sampel perjenis applicable, P95aksilokal≤300ms perjenis, seluruhnavigasi≤1000ms, feedbackheavywork tergambar≤300ms dan UIresponsif. Ukur commitdomain→tampil sesuai hasilbenar dan bebanmarketaktif; waktuantreanfill bukanlatensirender. Simpanrawtiming/environment/seed/sourcefingerprint dan timeout/outlier. Negativecheck kelulusan tidakboleh meluluskan sampelkosong/kurang/lambat; tidak memakai JSONPASSrunlama sesudahgagal.

Simpan rununik di `Artifacts/8-Anotasi-2026-10-22/`, jangan menimpa bukti/runaktif. Catat keputusanlayout dan statusA1–A12 pada checkpointbaru yang ditautkan daritrackerexisting; pertahankan riwayat. Reviewer berbeda menilai diff, scopeakun, assertion dan buktinya; jangan melabeliCLOSED karena tekslaporan sudahditulis. Nama direktori lama dipertahankan, setiaprun mencantumkanscopeversi12task.

Setelah sourcestabil: previewbaru diportterpisah/savepercobaan yang tidakmengganggupreview pemilik; buildAPKkandidat, periksaZIP/appID/version/minSDK/targetSDK/certificatesigning/byteaset/katalog/sourcecompiledmatch dan tidakadafixture/save/helperQAterpaket. Laporkanpath/ukuranaktual/SHA256/sourcefingerprint setelah berkasfinaldibaca, hash savepribadi dan batasnative. SimpanbaselineAPK. Tidak mengklaim terpasang/terujidiHP.

Laporan akhir Indonesia menjawab A1–A12 satu persatu: koleksi/filter/ownedstyle, lokasiriwayat dan penghematanruang, penyebabpriceflat dan buktifill→price→UI, bugreservasi dan scopeakun/NPC, teksdihapus versuskonfirmasitetapaman, statuslocked/checklist, hierarkiLiga/Peringkat, matriksfungsiBisnis dan perjalananusahautuh, halamanProfil+definisistatistik, pemetaanreferensi/desainPasar dan gateintegrasirealtime, hibahreal-date/00.00WIB/idempotency/migrasi, hasiltes/performa/reviewer, screenshotbefore/after, URLpreview/APK dan next_action. Keputusanrasio15menit/kurs15800/katalog tidakboleh regresi.

READY_FOR_OWNER_PLAYTEST hanya untuk scopehostA1–A12 yang lulus, tidak ada bugterbuka diketahui termasukminor danAPKcocoksource. A4/A9/A10/A11/A12 adalah gatewajib, bukan opsidesain: pasarbelumterbuktiberjalan, Bisnisbelumutuh, Profilbelumada/angka tidakcocok atau hibahmasihmengikuti clockgame/berulang berarti HOST_PARTIAL. Itu bukan seluruh14fase/PRD/Androidfisik atau izinpublikasi. Jika host belumlulus, HOST_PARTIAL dengan bukti/blocker. Asetraster masihCodexyangmembuat; integrasikan hanya yangbenar-benarada, jangan menunggu gambar untukperbaikanindependen. NativeNOT_TESTED/DEFERRED tetapterpisah.

Kerjakan hasilterukur→tes→review→checkpoint→lanjut. Tidak perlu approvalrutin. Jangan berhenti karena durasi/iterasi/giliran/token,satu tes hijau atau buildAPK. Dua kegagalan sama tanpa diagnosisbaru:PERLU PERHATIAN dan pendekatanlain; tiga:kerjakanbagianindependen atauBLOCKED jika butuhtindakanluar. HormatiSTOP/pause pada batasaman. Jangan menjanjikan nolbugtersembunyi atau nolkehilanganprogresmutlak.

## 3. Protokol wajib penuntasan dan verifikasi independen

Pemilik meminta pekerjaan dituntaskan dengan baik dan risiko kesalahan ditekan. Gunakan long task dengan pembagian multi-agent sesuai kemampuan aktual, bukan lebih banyak agent tanpa koordinasi. Prompt ini tidak menjamin nol kesalahan atau memberikan kemampuan/tool yang tidak tersedia; kelulusan ditentukan bukti, bukan keyakinan model.

### Q1. Pembagian peran dan satu integrator

Tetapkan satu koordinator integrasi/penanggungjawab hasil. Jika toolmulti-agent tersedia, pisahkan peran implementasi domain/uang/clock, implementasi UI, dan verifier/reviewer independen **untuk milestone yang sedang aktif**. Reviewer tidak menjadi penulis utama perubahan yang sedang dinilai. Jumlahagent mengikuti slot/kemampuan nyata; tidak membuat thread pengguna atau launcher paralel sebagai pengganti sembarangan. Kehadiran banyakagent tidak mengizinkan banyakmilestone implementasi aktif.

Sebelum pembagian kerja pada milestone aktif, tulis ownership file, kontrak API/data, dependency dan direktori build/test. File bersama sepertiTradingFacade/TradingWebBridge/app.js hanya punya satu writer pada satu waktu; agent lain memberi review/patchusulan melalui koordinator. Hindari bersamaan menjalankan testmutasi pada save/server yang sama. Agent lain tidak memulai implementasi milestone berikutnya sebelum gate milestone aktif lulus. Integrator menggabungkan perubahan dan menjalankan gate integrasi akhir; laporanworker bukan otomatis kelulusanintegrasi.

Jika multi-agent/reviewer berbeda tidak tersedia, tetap tuntaskan implementasi dan tes independen dari keterbatasanitu, catat `REVIEW_INDEPENDEN_PENDING` sesuai fakta. Jangan mengaku telah direviewolehagent lain atau menurunkan kriteria. Gate yang memerlukan reviewtetappending; lanjutkan pekerjaan yang tidakbergantung padanya.

### Q2. Bukti sebelum klaim selesai

Untuk setiap requirement, subpekerjaan, acceptance dan milestone dalam A1–A12, gunakan matriks requirement→reproduksi→akar masalah→source/caller→perbaikan→negative/regressioncheck→expected/actual→bukti→reviewer→verdict. A1–A12 adalah induk, bukan pengganti daftar rinci. Bedakan implementationdone, tested, verified dan pending. Simpan checklist pada trackerexisting atau satu file Markdown/CSV pendukung yang ditautkan, bukan aplikasi/processframework baru. Jangan mereduksi daftar menjadi ringkasan12baris jika masih ada subpekerjaan yang belum tertulis.

Setiap bug yang ditutup punya pemeriksaan yang benar-benar mendeteksi versi gagal atau kasus ekuivalen yang disengaja, lalu lulus pada versi final. Untuk uang/clock/save, assertion menilai invariant dan hasil yang tepat; UIvisible, elapsedwaktu, counttes atau screenshotada bukan pengganti validasi. Reviewer memeriksa assertiondan scope, bukan hanya melihat JSONPASS.

### Q3. Loop perbaikan yang terarah

Untuk satu milestone aktif: implementasi→regresi terarah→review→perbaikan→uji ulang bagianterdampak→bukti→checkpoint; baru mulai milestone berikutnya. Sesudah seluruh milestone implementasi terkait stabil, jalankan seluruh regresi applicable dari source akhir pada milestone integrasi yang tercatat. Jika temuan baru muncul, buka kembali gate terkait dan invalidasi bukti yang sudah tidak mewakili source. Jangan terus mengulang suite yang sudah lulus tanpa perubahan/alasan, dan jangan membiarkan kegagalan diketahui lewat agar cepat menyatakan selesai.

Pertahankan batas yang disetujui: 7viewport×fontnormal/200% untukstateberubah,10×100aksi finansial kritis sesuai jalur, minimal30sampel benchmarkperjenis dan negativechecks kelulusan. Jangan menurunkan jumlahsampel, threshold atau cakupan karena hasil belum hijau. Gunakan runner existing dan minimalkan testyang tidakbermakna.

### Q4. Perjalanan terintegrasi dan reviewer akhir

Verifier/reviewer berbeda memeriksa source akhir dan, bila toolmemungkinkan, menjalankan pemeriksaan terarah sendiri: freshQA→klaimhibah→pasarNPCbergerak→BUY/SELL→profil/riwayat→Bisnis/pergantianhari→beli/jualkoleksi→save/load→clocknyatamelewatitengahmalam. Setupwaktu terkontrol tetap diQA; jangan ubah OS. Semua saldo/status/update UI diperiksa, bukan sekadar navigasi halaman.

Audit ekstra jalur negatif: dana kurang, pemrosesan parsial, requestberulang/konkurensi, responsputus, gagalpersist, reservedfundNPC versuspemain, emptydata, waktu mundur, restorelegacy dan retry. Reuse kasus yang ada; tidak perlu skenario baru yang hanya mencentang kotak tanpa kemungkinan mendeteksi kesalahan.

### Q5. Bekukan source untuk pembuktian paket

Sesudah review dan perbaikan final, catat fingerprint source/compiledclasses. Jalankan checks yang diwajibkan untuk versi tersebut, lalu buildAPK dan cocokkan aset/paket/signing/source. Jika source berubah sesudah verifikasi, catat delta dan ulang gate yang terdampak; jangan menyerahkan hasilrunlama sebagai bukti versibaru.

Laporan verifier menunjuk bukti konkret: command/runner, timestamp, environment/seed/saveQA, jumlahsampel/status, rawtiming, error/failureyang ditemukan, screenshotyang benar-benar dibuka, serta openfindings. Artefak yang kosong/stale, JSONhardcodedPASS, penyalinan metadata salah atau perhitungan suite tanpa membaca hasil aktual menggagalkan gate bukti. Ukuran/SHAAPK dibaca dari berkasfinal sesudah penyalinan.

### Q6. Larangan klaim selesai prematur dan penanganan hambatan

Jangan mengakhiri dengan COMPLETE/READY bila ada requirementhostmandatory belumterbukti, reviewerfinding belumditutup, atau testgagal. Jangan menyebut seluruh tugas selesai hanya karena folder/checkpoint/APK sudah ada. Kekurangan aset atau gate native yang memang di luar scope dipisahkan dari bughost.

Jangan berhenti karena penghematan token, durasi, iterasi/giliran, mockupselesai atau satu tes hijau. Tetap patuhi batas tool/sistem, STOP/pause dan hambatan nyata. Sebelum batas aman atau pergantian konteks, simpan checkpoint exactsession/source/pekerjaan/bukti/next_action agar dapat dilanjutkan tanpa mengulang. Tidak memasang scheduler/globalautomation atau menjanjikan auto-resume jika tidak ada mekanismenya.

Dua kegagalan sama tanpa diagnosisbaru: kirim PERLU PERHATIAN dengan bukti dan pendekatan lain; tiga: pindah pekerjaan independen atau BLOCKED jika benar-benar memerlukan tindakanluar. Jangan memanggilmodel tanpaakhir. Bila blocker belum dapat diatasi, serahkan hasil yang konkret dengan statusparsialdan penyebabnya, bukan hasil kosong atau janji selesai.

### Q7. Format penyerahan akhir

Laporkan A1–A12 beserta seluruh milestone/subpekerjaan yang ditautkan: VERIFIED/PARTIAL/BLOCKED dengan bukti dan reviewer, jumlahtemuan ditutup/masihterbuka, hasilnegativecheck/fullregression/UI/performa, rootcausepasar/harga/hibah, kompatibilitassave, screenshotbefore/after, previewURL, path/ukuran/SHAAPK/sourcefingerprint, serta gateaset/native/review yang pending. Tautkan daftar lengkap; jangan mengganti daftar dengan kalimat `semua sudah selesai`. Beri keputusan readiness yang berasal dari matriks, bukan kalimat optimistis. Catat risiko/batas yang nyata tanpa mengklaim nolbugtersembunyi atau keamanan mutlak.

### Q8. Daftar lengkap tanpa pemangkasan dan eksekusi satu per satu

**Ini instruksi eksplisit pemilik, bukan saran gaya pelaporan:** jangan meringkas, memangkas, menghapus atau menggabungkan daftar pekerjaan/milestone untuk memperpendek rencana, mengurangi denominator, menghemat token atau mempercepat klaim selesai. Bila kebutuhannya memang banyak, tulis semuanya dan kerjakan satu per satu. Tidak ada batas buatan5/10/12/20milestone. A1–A12 adalah kelompok requirement, bukan12langkah yang otomatis cukup untuk seluruh pekerjaan.

1. **Inventaris lengkap sebelum implementasi lanjutan.** Baca seluruh mandat aktif, checkpoint dan temuan; buat daftar eksplisit setiap hasil yang harus dikerjakan serta acceptance/negativecase/bukti terkait. Pecah pekerjaan kompleks menjadi milestone dengan outcome terukur yang dapat ditutup sendiri. Contoh `Bisnis selesai` atau `perbaiki UI+tes+APK` tidak menggantikan pendirianusaha, tiapaksi domain, status/error, pembaruanUI, persistensi dan pemeriksaannya. Jangan pula mengada-adakan milestone kosmetik tanpa requirement hanya agar daftar panjang.
2. **Peta cakupan tidak boleh berlubang.** Setiap requirement dan acceptance diA1–A12 serta mandat lama yang applicable mempunyaiIDmilestone/subpekerjaan dan bukti. Kasus uji rinci boleh menjadi checklist anak, tetap ditulis utuh dan tidak hilang dalam kalimat `dan lain-lain`, `uji semua`, `sisanya sama` atau `selesaikan sisanya`. Tahap besar boleh menjadi heading navigasi, bukan pengganti item rinci. Pekerjaan yang sudah benar tetap direkam dengan bukti; tidak perlu diimplementasikan ulang.
3. **Simpan daftar sebagai sumber kebenaran yang persisten.** Gunakan tracker existing dan satu berkas Markdown/CSV bila diperlukan. Setiap item mempunyaiIDstabil, parentrequirement, judul spesifik, dependency, status, acceptance, owner/reviewer, file/bukti dan next_action. Semua item yang belum selesai tetap terlihat. Percakapan yang dikompresi atau resume bukan izin mengganti daftar lengkap dengan ringkasan; baca kembali file dan lanjutkanIDyang tepat.
4. **Satu milestone implementasi aktif.** Pada satu waktu paling banyak satu milestone berstatusIN_PROGRESS. Worker/reviewer boleh membantu analisis, implementasi sesuaiownership, pengujian dan review pada milestone yang sama. Jangan membagi berbagai milestone ke banyakworker sekaligus atau mulai memperbaiki milestone lain diam-diam. Prasyarat harus sudah memenuhi gate sebelum dependent dimulai.
5. **Gerbang sebelum lanjut.** Milestone aktif baru VERIFIED setelah implementasi yang diperlukan, tes relevan/negativecheck, review yang diwajibkan, penutupan temuan, expected/actual dan bukti sourceyangsesuai tersimpan. Simpan checkpoint milestone itu, baru pilih milestone berikutnya menurutdependency. Kode selesai tetapi belumdites/review bukan VERIFIED. Gagaltes biasa berarti diagnosis/perbaikan pada milestone yang sama, bukan BLOCKED untuk menghindarinya.
6. **Daftar dan denominator stabil.** Jangan menghapus item gagal/pending untuk membuatx/yterlihat lebih baik. Requirement baru ditambahkan denganIDbaru dan alasan; breakdown tambahan mempertahankan parent dan jejaknya. Jika item memang duplikat/tidakapplicable/superseded, pertahankan baris, tautkan penggantinya dan alasan/evidence; jangan menandainya VERIFIED tanpa penerimaan. Pembatalan requirement wajib perlu keputusan pemilik, bukan keputusan sepihak supaya selesai. Catat perubahan daftar/total, jangan diam-diam mereduksi jumlahmilestone di tengah jalan.
7. **Laporan lengkap dan dapat dilanjutkan.** Sesudah tiap milestone, laporkanID/judul/status, bukti/checks/reviewer, temuanterbuka danIDberikutnya; perbarui daftar keseluruhan tanpa menghilangkan item lain. Daftar milestone harus tersedia lengkap dalam artefak yang ditautkan. Jika diminta menampilkan rencana/daftar milestone, tampilkan semuaitem; bila batasoutput mengharuskan beberapa bagian, beri nomorbagian dan lanjutkan dariIDterakhir tanpa melewatkan item. Narasi singkat boleh mendampingi, tidak menggantikan daftar lengkap atau menyebut sebagian pekerjaan mewakili seluruhnya.
8. **Hambatan nyata tidak boleh disembunyikan.** Jika benar-benar butuh tindakanluar, checkpoint item sebagaiBLOCKED dengan alasan/upaya/bukti dan dependency yang terdampak. Hanya sesudah statusaktif itu dicatat, boleh mengaktifkan satu milestone independen berikutnya; itemblocked tetap tercantum dan proyektetapPARTIAL sampai gatewajib tertutup. KetentuanPERLU PERHATIAN/STOP/batas tools tetap berlaku. Jangan memaksa loop takberujung, memalsukan lulus atau menghapus milestone karena konteks/budget/durasi.

Checklist akhir Q8: seluruh requirementterpetakan; seluruhID/bukti masihada; tidak ada pekerjaan wajib yang diserap menjadi headinggenerik; tidak ada dua milestoneimplementasiaktif; tidak ada x/yhasilpemangkasan; semua gatefinal memakai sourceakhir; daftar lengkap dan checkpoint memungkinkan reviewer melanjutkan tanpa menebak pekerjaan yang disembunyikan.

Mulai sekarang dari preflight/reproduksi A1–A12, susun daftar milestone lengkap tanpa ringkasan, ownership dan matriksbukti, lalu langsungtuntaskan satu milestone aktif sesuaidependency. Lanjutkan satu per satu setelah gate dan checkpoint masing-masing lulus. Jangan berhenti setelah menulis rencana.
