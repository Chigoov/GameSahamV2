# Catatan review dan intake 14 revisi pemilik — 22 Oktober 2026

Status: `OWNER_ANSWERS_RECEIVED`. Kelima belas jawaban pemilik sudah diterima pada 22 Oktober 2026. Jawaban verbatim ada di [DECISIONS.json](DECISIONS.json); spesifikasi final ada di [LONGTASK-14-REVISI.md](LONGTASK-14-REVISI.md). Bagian rancangan awal di bawah disimpan sebagai riwayat; jika berbeda, ikuti keputusan dan spesifikasi final. Pencatatan ini bukan bukti implementasi atau kelulusan produk.

## Batas pekerjaan saat ini

- Repo aktif: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`, branch saat pemeriksaan `codex/owner-audit-portrait-v8`, HEAD `49bbafb` dengan perubahan lokal yang wajib dipertahankan.
- Pemilik meminta temuan review disimpan, seluruh 14 revisi dijadikan long task terperinci untuk agent miliknya, dan tepat 15 pertanyaan pilihan sebelum pekerjaan dimulai.
- Tahap ini hanya pencatatan/perencanaan. Jangan menjalankan agent, mengubah source game, mengubah STATUS.json lama, build, instalasi, ADB, atau memakai save pribadi untuk mutasi.
- Long task final, katalog, tracker, dan prompt eksekusi telah disiapkan dari jawaban pemilik. Agent baru mulai implementasi setelah pemilik menjalankan prompt; source game tidak diubah saat persiapan ini.

## Lima temuan review wajib diperbaiki

### F01 — P1: backup valid dapat tertimpa save rusak

Lokasi: `android/app/src/main/java/com/tradingglobal3d/core/storage/SaveManager.java:65`.

Validasi save sebelum rotasi backup diganti syarat ukuran >10 byte; kegagalan copy diabaikan. Reproduksi terpisah: buat primary+backup valid, ganti primary dengan teks rusak >10 byte, save kembali; `backup_preserved=false`. Rusakkan primary berikutnya; load menghasilkan `CORRUPTED_ALL`. Kasus existing hanya memakai `BROKEN` (6 byte), sehingga tidak menguji kondisi yang rusak.

Perbaikan: validasi struktur/checksum/versi primary sebelum menimpa backup, pertahankan backup terakhir yang valid, jangan mengakui keberhasilan jika penulisan/rotasi yang wajib gagal. Tinjau kembali penghapusan disk sync. Gunakan mekanisme existing, bukan sistem save kedua. Acceptance: corrupt primary pendek/panjang/truncated, copy/write gagal, backup recovery dan interrupted write diuji; progres terakhir yang sudah diakui tetap dapat dipulihkan.

### F02 — P1: API profil perusahaan tidak kompatibel dengan minimum Android

Lokasi: `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2343`.

`InputStream.readAllBytes()` baru tersedia API33 menurut metadata SDK lokal; app min Sdk26 dan tidak mengaktifkan library desugaring yang menyediakan metode itu. `NoSuchMethodError` bukan Exception. Temuan statis, belum reproduksi pada perangkat.

Perbaikan: reuse pembaca stream kompatibel min Sdk26 dan tutup resource dengan benar; jangan menaikkan min Sdk hanya untuk menghindari bug. Acceptance: pemeriksaan API/lint plus kontrak bridge; hasil host tidak dianggap bukti Android8–12 berjalan.

### F03 — P1: Grand Journey memberi hasil lulus meski aksi inti dilewati

Lokasi: `Work/verify-grand-journey-e2e.js:81` dan `:181`.

Pengulangan skrip yang sama pada game QA baru (hanya lokasi output bukti diganti) menghasilkan exit0 / 100% PASS, tetapi log tidak menunjukkan pembukaan tiket/submission; portfolio positions=0, dan langkah bisnis terlewati karena perusahaan tidak ada. Setelah run, game tetap paused08:30 dan kas pribadi Rp75juta (modal Rp50juta + hibah Rp25juta).

Perbaikan: kontrol wajib harus ada; ketiadaannya FAIL, bukan skip. Bentuk fixture melalui aksi domain yang sah; verifikasi BUY/SELL/fill/partial/cancel sesuai cakupan, mutasi posisi/kas/ledger, operasi bisnis, koleksi, dan save/load yang benar-benar dilakukan. Uji negatif harus membuktikan gate gagal bila satu aksi pokok sengaja rusak. Jangan menyamakan receipt order dengan fill.

### F04 — P2: catch-up offline panjang memblokir proses

Lokasi: `TradingFacade.reconcileOfflineElapsed` / `advanceWorldSeconds`, caller `MainActivity.onCreate` dan `onResume`.

Run review gabungan dihentikan setelah188 detik saat test `TimeDualRateLifecycleTest.testLifecycleL12_LongOffline30Days` belum selesai. Kasusnya30 hari nyata=720 hari game. Thread dump saat berjalan menunjuk pemindaian ledger melalui `CareerManager.evaluateRecordedTrades`. Jalur Android melakukan catch-up sinkron sebelum/ketika UI resume. Ini bukti kelambatan host dan risiko UI macet, bukan bukti ANR perangkat yang sudah diuji.

Perbaikan: diagnosis biaya event/settlement/evaluasi berulang dengan input deterministik, hilangkan scan berulang yang tidak diperlukan, proses catch-up dengan UI tetap responsif dan feedback cepat; jangan memangkas periode atau menggandakan settlement. Acceptance: offline pendek/panjang, shutdown/resume/interruption, idempotensi, rekonsiliasi keuangan dan batas respons sesuai standar proyek.

### F05 — P2: aksesibilitas tombol watchlist rusak

Lokasi: `android/app/src/main/assets/www/app.js:966` dan `:991`.

Browser360x640: tombol bintang14.671875x19.59375 px. Space ketika tombol fokus membuka detail saham karena keydown bubble ditangani parent baris.

Perbaikan: area sentuh >=48x48CSSpx dalam baseline Web View yang dikalibrasi, label/status aksesibel, handler keyboard hanya mengaktifkan target yang benar. Acceptance: klik/tap/Enter/Space, fokus, teks200%, seluruh viewport; ukuran baris tidak boleh dipakai untuk meluluskan tombol anak.

## Bukti review yang disimpan

Direktori: `Artifacts/Owner-Review-2026-10-22/`.

- `save-probe.log`: hasil reproduksi backup.
- `bounded-junit.log`: kompilasi source domain/bridge terbaru sebelumnya sukses; 36 tes terarah lulus. Ini bukan full-suite acceptance.
- `browser-probe.json`: dimensi watchlist, keyboard, 100 emiten, error Java Script pada run terarah.
- `grand-journey-reproduced.log` dan `grand-journey-report.json`: reproduksi false-positive tes agent.
- `grand-after-snapshot.json`: snapshot QA sesudah run, bukan save pribadi.
- Observasi188 detik/thread dump ada pada percakapan review; jangan menyebutnya laporan benchmark lengkap atau test yang selesai.
- APK longtask8 SHA256 cocok laporan; empat aset (HTML/JS/CSS/company_profiles.json) identik source saat review. Tidak dilakukan build ulang, instalasi, atau pengujian HP oleh reviewer.

## Seluruh 14 revisi yang wajib masuk long task final

| ID pemilik | Permintaan | Arah solusi awal dan acceptance yang harus dirinci setelah jawaban |
|---|---|---|
| R01 | Dana hibah menjadi countdown setelah klaim | Tetap reset00.00 WIB dunia nyata sesuai kontrak existing. Domain mengirim eligibility dan next Claim At; UI menghitung sisa HH:mm:ss, refresh saat resume, kembali aktif hanya berdasarkan domain, tanpa memberi uang otomatis/claim ganda. |
| R02 | Hapus Atur Sesi Pasar; game otomatis; jam berurutan | Hapus kontrol manual dari entry point/tutorial, mulai clock otomatis, satu clock domain authoritative, foreground/background sesuai keputusan. Interpolasi presentasi monoton dari anchor domain; koreksi drift/resume tanpa clock ekonomi JS kedua. Pada percepatan tinggi jangan menjanjikan setiap detik game terlihat di setiap frame. Perbaiki F04 sebelum kelulusan waktu otomatis. |
| R03 | Gaya/profil risiko muncul pertama main | Onboarding ramah, hasil dapat dipahami dan disimpan; profil bukan label medis/kemampuan pasti. Preferensi/rekomendasi dipisahkan dari pembatasan fitur sampai ada keputusan eksplisit. Migrasi pemain lama tidak menghapus progres. |
| R04 | Redesign misi dan periodik | Satu pusat misi dengan tab/progres/claim/status dan countdown kalender game; daily/weekly/career jelas, claim idempoten, empty/locked/error, navigasi balik dan teks200%. |
| R05 | Bagian atas UI tidak menabrak panel HP | Insets native status bar/cutout/navigation/IME sesuai API26+, koordinasikan dengan padding Web View agar tidak dobel; hindari hardcoded tinggi notch. Bukti host dan native dipisah. |
| R06 | IPO minimal 3x setiap bulan game dan notifikasi | Jadwal domain deterministik minimal 3event setiap bulan termasuk panjang bulan berbeda; announce/bookbuilding/penjatahan/listing, reservasi/refund/oversubscription, deduplikasi event/notification, save/load/offline melewati bulan, katalog/portfolio tetap konsisten. Identitas emiten dan kanal notifikasi menunggu jawaban. |
| R07 | Semua emiten memperbarui persentase harian | Audit seluruh 100 emiten dan tambahan IPO; pakai (harga kini-previous close)/previous close dengan baseline harian persistent per bursa, perbarui quote/index/DOM bersama. Bedakan quote stale, tidak ada fill, nol perubahan yang sah dan hari baru; jangan mengarang perubahan supaya semua angka bergerak. |
| R08 | Tambah valuasi, pemegang saham, keuangan dan data lain | Bedakan fakta profil dunia nyata bersumber/tanggal dan fundamental simulasi. Data potensial: market cap, saham beredar, free float, jumlah pemegang saham jika tersedia, struktur pemilik, pendapatan, laba, aset/liabilitas/ekuitas, arus kas, EPS/PER/PBV/ROE/ROA/DER, dividen, periode laporan, sumber dan as-of. Jumlah pemegang saham berbeda dari jumlah saham beredar; unknown tidak diisi angka palsu. |
| R09 | Ganti instrumen menjadi emiten | Audit teks terlihat pada konteks saham, search/filter/detail/error/accessibility. ID/API/save tetap kompatibel. Emiten bukan nama umum untuk deposito/komoditas/aset digital; gunakan istilah tepat per jenis produk agar revisi10 tetap benar. |
| R10 | Perdagangan selain saham | Reksa dana, komoditas, aset digital dan deposito semuanya masuk scope; audit dan reuse implementasi existing. Tentukan unit, kurs, spread/fee, harga, sesi, custody virtual, order/settlement, deposito tenor/bunga/penalti, saldo/ledger/NAV dan isolasi akun. Produk sepenuhnya virtual. |
| R11 | Redesign profil statistik | Ringkasan dan detail terpisah sesuai pilihan pemilik; return metodologi jelas, realized/unrealized, win rate berpenyebut, benchmark/drawdown/exposure sesuai data tersedia, pisahkan setoran/hibah dari profit serta dana klien dari kekayaan pribadi. Tanpa angka default palsu. |
| R12 | Countdown news, clue, estimasi dan dampak ekonomi nyata dalam game | Jadwal domain satu kalender game; future countdown lalu waktu berlalu setelah rilis. Clue sektor/emiten, arah/rentang estimasi dan horizon jelas, tidak menjanjikan return pasti. Event mengubah sinyal/fundamental/keputusan NPC dan order matching secara teruji; jangan UI mengubah harga. Anti-future-leak, priced-in/overlap/expiry, deterministic seed, save/load idempoten. |
| R13 | Tambah galeri, daftar kategori dan rarity existing+baru | Baseline source12 item, 4 kategori: Kendaraan, Properti, Permata, Seni&Jam Tangan; rarity existing Standard/Langka/Legenda. Final wajib inventaris nama item existing, daftar item baru, kategori, rarity, harga/valuasi/biaya/unlock/manfaat, sumber aset/lisensi. Migrasi rarity/kategori tidak menghilangkan kepemilikan/agunan. Volume dan tier menunggu jawaban. |
| R14 | Perbanyak reksa dana dan data investor | Audit unit/NAB/fund existing dahulu. Katalog bervariasi risiko/kebijakan; tampilkan tipe, tujuan, profil risiko, NAB/unit, AUM, return beberapa periode game, benchmark, drawdown, alokasi/top holdings, biaya, minimum, cutoff, settlement/redemption, pengelola dan dokumen simulasi. Tidak menjamin yield atau menyalin data nasabah/produk nyata tanpa dasar. |

## Kerangka long task final

1. Preflight/fingerprint/source/save safety dan penutupan F01–F05 yang relevan sebelum dependency dianggap lulus.
2. Kalender otomatis, jam, countdown, safe area dan quote baseline bersama.
3. Onboarding risiko, misi, profil dan terminologi.
4. Fundamental, news berdampak, IPO bulanan dan notifikasi.
5. Produk multi-aset, perluasan reksa dana dan katalog koleksi lengkap.
6. Integrasi14 revisi, uji kritis10x100 sesuai aturan repo, matriks layar/state7 viewport x2skala teks, performa, review independen, APK dan handoff. Setiap requirement punya ID, evidence dan source revision; jumlah folder/test tidak meluluskan fitur.

Wajib menghasilkan task/dependency/acceptance/evidence per R01–R14, tracker aktual, prompt copy-paste untuk agent pemilik, daftar kategori/rarity/koleksi serta tabel produk reksa dana. Pertanyaan dan jawaban dicatat sebelum finalisasi; rekomendasi bukan jawaban pemilik.

## Lima belas keputusan yang telah dijawab

Q01 tempo hari game; Q02 onboarding risiko; Q03 bentuk pusat misi; Q04 bentuk profil statistik; Q05 gaya UI bersama; Q06 identitas emiten IPO; Q07 kanal notifikasi IPO; Q08 sumber data fundamental; Q09 kedalaman data emiten; Q10 bentuk produk multi-aset; Q11 bentuk clue/estimasi news; Q12 skala katalog koleksi; Q13 jumlah tier rarity; Q14 skala katalog reksa dana; Q15 intensitas pergerakan pasar.

Jawaban: `RECEIVED`, lihat tabel di bawah dan DECISIONS.json. Tidak ada opsi yang dipilih otomatis. Countdown hibah00.00 WIB, safe area yang benar, penghapusan kontrol sesi manual, dan seluruh 14cakupan sudah dicatat sebagai requirement; bukan pertanyaan izin ulang.

### Pilihan yang diajukan kepada pemilik

Huruf A/B/C berikut mengikuti urutan opsi pada formulir percakapan. A adalah rekomendasi awal reviewer; jawaban aktual dicatat pada kolom terakhir. Jawaban khusus Q02, Q06, dan gabungan Q07 menggantikan opsi awal.

| Pertanyaan | A — rekomendasi | B | C | Jawaban |
|---|---|---|---|---|
| Q01 Tempo hari game otomatis saat terbuka | 15 menit nyata/hari game | 30 menit | 60 menit | A — 15 menit |
| Q02 Penentuan profil risiko pertama main | Kuis5pertanyaan, 3 profil, bisa diubah | Pilih langsung3profil dengan contoh | Profil awal dengan saran penyesuaian berdasarkan perilaku | Khusus — sesuai perilaku pemain, tanpa kuis wajib |
| Q03 Pusat misi | Tab Harian/Mingguan/Karier, kartu ringkas, progress bar | Agenda dikelompokkan hampir selesai/hadiah | Peta milestone bertahap | A — tiga tab misi |
| Q04 Profil statistik | Ringkasan+tab Kinerja/Keuangan/Pencapaian | Laporan lengkap dengan bagian lipat | Avatar/level/badge dan halaman statistik terpisah | A — ringkasan dan tiga tab profil |
| Q05 Susunan UI bersama | Pola aplikasi investasi, ringkas, detail saat dibuka | Premium visual, kartu besar/ilustrasi | Progres/level/hadiah dominan | A — UI investasi ringkas |
| Q06 Identitas IPO minimal 3/bulan game | Emiten fiktif baru berlabel simulasi | Emiten nyata dari katalog cadangan, IPO simulasi | Campuran dengan penanda jelas | Khusus — identitas asli dan rekaan, tanpa label simulasi berulang di kartu |
| Q07 Notifikasi IPO | Inbox dalam game+banner singkat | Dalam game+notifikasi HP opsional | Inbox+badge tanpa banner | A+B — inbox/banner dan notifikasi HP opsional |
| Q08 Sumber fundamental | Profil nyata bersumber, keuangan/valuasi simulasi berlabel | Snapshot laporan nyata bertanggal | Fundamental simulasi konsisten | C — fundamental model game |
| Q09 Kedalaman data emiten | Paket lengkap inti valuasi/keuangan/pemilik/dividen/sumber | Inti+tren periode+perbandingan pesaing+rincian | Inti+indikator khusus sektor | A — paket inti lengkap |
| Q10 Kedalaman multi-aset | Spotkomoditas/digital, unit NAB, deposito tenor | A+order book/limitkomoditas&digital | A+obligasi/perbandinganlintasproduk | A — spot/unit-NAB/deposito tenor, preserve obligasi |
| Q11 Penyajian dampak news | Sektor/emiten, arah, rentang, horizon, keyakinan;tidak pasti | Rumorsektor, detail lewat riset uang game | Arah/besaran lebih jelas, skenario simulasi | A — probabilistik dengan clue dan horizon |
| Q12 Skala koleksi | Sekitar60 item/8 kategori | Sekitar100 item/10 kategori | Sekitar150 item/12 kategori | C — total 150 / 12 kategori |
| Q13 Tier rarity | 5:Umum/Tidak Umum/Langka/Epik/Legendaris | Existing3:Standar/Langka/Legenda | 7 tier hingga Mitis/Unik | A — 5 tier |
| Q14 Skala katalog reksa dana | 12 produk, 6 keluarga | 18 produk | 24 produk termasuk sektor/global | C — total 24 produk |
| Q15 Intensitas pasar | Realistis per emiten/likuiditas/sektor/fundamental/news | Lebihaktif, NPClebihsering, volatilitaswajar | Lebihagresif/menantang, dampaknewslebihbesar | C — lebih agresif |

Hal yang tidak menjadi pilihan pengurangan scope: seluruh 14 revisi, 4 kelompokproduk non-saham, validasi uang/save, quote harian yang benar, safe area, dan dampak berita yang benar-benar terhubung ke domain. Background existing1 jam nyata/hari game tetap berlaku kecuali pemilik mengubahnya; tempo Q01 hanya foreground. Memilih rekomendasi Q05 tidak otomatis memberi izin mengganti tema existing tanpa konsistensi.

## Inventaris source existing untuk mencegah pembangunan ulang

Dibaca langsung dari source pada22Oktober2026. Ini inventaris implementasi, bukan acceptance fungsi/ekonomi/perangkat. Harga berikut angka game existing; bukan valuasi/harga pasar nyata yang diverifikasi.

### Seluruh 12 koleksi existing

Sumber: `android/app/src/main/java/com/tradingglobal3d/core/collection/CollectionManager.java`, `initDefaultCatalog()`. Pertahankan ID, pemilik, harga perolehan, agunan dan histori ketika kategori/rarity diperluas.

| ID existing | Nama | Kategori existing | Rarity existing | Harga dasar game IDR | Pemeliharaan/hari game IDR | Bonus reputasi |
|---|---|---|---|---:|---:|---:|
| COL-CAR-00 | Vespa Klasik 1965 Restorasi | Kendaraan | Standar | 25000000 | 20000 | 5 |
| COL-CAR-01 | Porsche 911 GT3 RS | Kendaraan | Langka | 4500000000 | 500000 | 15 |
| COL-CAR-02 | Ferrari 296 GTB | Kendaraan | Legenda | 8000000000 | 1000000 | 25 |
| COL-PROP-00 | Ruko Komersial BSD City | Properti | Standar | 45000000 | 30000 | 8 |
| COL-PROP-01 | Penthouse SCBD Mega Kuningan | Properti | Langka | 15000000000 | 1500000 | 30 |
| COL-PROP-02 | Villa Tebing Uluwatu Bali | Properti | Legenda | 25000000000 | 2500000 | 50 |
| COL-JEW-01 | Cincin Safir Biru Ceylon | Permata | Standar | 35000000 | 15000 | 6 |
| COL-JEW-02 | Kalung Berlian Solitaire Tiffany | Permata | Langka | 2500000000 | 100000 | 18 |
| COL-JEW-03 | Tiara Berlian Kerajaan Langka | Permata | Legenda | 18000000000 | 800000 | 40 |
| COL-ART-00 | Rolex Submariner Vintage 1970 | Seni & Jam Tangan | Standar | 15000000 | 10000 | 4 |
| COL-ART-01 | Patek Philippe Grandmaster Chime | Seni & Jam Tangan | Langka | 5000000000 | 200000 | 20 |
| COL-ART-02 | Lukisan Bersejarah Raden Saleh | Seni & Jam Tangan | Legenda | 12000000000 | 500000 | 35 |

### Reksa dana dan produk selain saham yang sudah ada

Sumber `core/funds/MutualFundManager.java` dan `core/funds/MutualFund.java`:

| ID | Produk existing | Mesin tipe existing | Pengelola yang tertulis dalam game |
|---|---|---|---|
| FUND-MM-01 | Nusantara Kas Lancar | MONEY_MARKET | PT Nusantara Manajemen Investasi |
| FUND-FI-01 | Nusantara Obligasi Mantap | FIXED_INCOME | PT Mandiri Investasi Semesta |
| FUND-EQ-01 | Nusantara Indeks Saham Unggulan | EQUITY_INDEX | PT Bahana Kapital Prima |

Nama/manajer tersebut adalah isi source, bukan pernyataan produk keuangan nyata terdaftar. Existing telah mempunyai pool account, unit/NAB, antrean, cutoff, redemption, fee, bind/restore serta kontrak underlying; perluasan katalog harus menggunakan jalur tersebut. Jangan sekadar menambah nama dana dengan perilaku identik. Tipe campuran/syariah/sektor yang dipilih harus mempunyai kebijakan/alokasi dan underlying yang sesuai, bukan jatuh ke fallback pendapatan tetap karena enum baru.

`core/multiasset/FundBuilderService.java` sudah memuat `BOND:FR0091` (Surat Utang Negara FR0091) dan `COMM:GOLD` (emas, unit gram). Jalur transaksi/valuasi terkait ada di `MultiAssetTradingService`. Obligasi adalah fitur existing yang harus tetap dipertahankan pada semua jawaban Q10; opsi C tidak berarti obligasi boleh dihapus bila pemilik memilih A/B. Pencarian source produksi belum menemukan model transaksi aset digital atau deposito tenor pemain yang terpisah. Kata `DEPOSIT` pada ledger berarti setoran kas, dan kontrak deposito internal underlying reksa dana bukan otomatis fitur deposito bank yang dapat dibuka pemain. Agent wajib membuktikan alur produk dari UI sampai ledger/settlement, bukan mengklaim selesai dari nama enum/kontrak internal.

### Titik integrasi yang harus dipakai dalam spesifikasi final

Path di tabel berikut relatif terhadap `android/app/src/main/`.

| Kelompok revisi | Titik source existing | Konsekuensi untuk pekerjaan agent |
|---|---|---|
| R01 hibah | `java/com/tradingglobal3d/core/facade/TradingFacade.java`: `getDailySponsorStatus`, `claimDailySponsor` | Reuse eligibility/next Claim At/receipt; UI countdown hanya presentasi. |
| R02 waktu | `core/clock/GameClock.java`; facade `advanceRealMs`, `advanceWorldSeconds`, `reconcileOfflineElapsed`; `mobile/MainActivity.java`; `assets/www/app.js` handler Atur Waktu | Trace foreground/background/startup/polling/autosave; hapus entrypoint manual tanpa menghapus kemampuan domain yang masih dipakai pengujian/lifecycle. |
| R03/R11 risiko/profil | `core/player/InvestorArchetypeService.java`, `PlayerProfileManager.java`; facade `getPlayerProfileJson`; UI `openPlayerProfileModal` | Bedakan preferensi risiko onboarding dengan archetype yang dihitung dari perilaku; jelaskan keduanya bila dipertahankan. |
| R04 misi | `core/career/PeriodicMissionManager.java`, `CareerManager.java`; UI `openPeriodicMissionsModal` | Daily/weekly/career tetap satu kalender game; jangan membuat timer reward JS. |
| R05 safe area | `mobile/MainActivity.java` handler Window Insets; `assets/www/style.css` root/safe-area | Existing handling bersyarat SDK>=30; inventaris dukungan SDK26–29 dan edge-to-edge/cutout/IME sebelum perbaikan. |
| R06 IPO | `core/corporate/CorporateActionService.java`: `reset(LocalDate)`, `registerIpoOffering`, `processDate`, `orderIpo`, `allotIpo`; UI `openIpoCenter` | Baseline reset menjadwalkan satu IPO-CLOUD relatif game Base; itu bukan scheduler3IPO/bulan. Tambahkan kalender berulang dan gunakan settlement existing. |
| R07 quote | `core/market/Instrument.java`: `previousClosePrice`, `getDailyChangePercent`, `startNewTradingDay`; UI `loadStocks`, `updateMarketQuotes` | Formula/cadence/reset/save/bursa/NPC perlu ditelusuri bersama. Ada caller reset hari di facade dan Career Manager; buktikan tidak mereset baseline pada saat salah. |
| R08 fundamental | `assets/data/company_profiles.json`; facade `getCompanyProfile`; `core/market/Instrument.java`: `getSharesOutstanding`, `getMarketCap` | Profil teks dan marketcap sudah ada sebagian; neraca/pemegang saham/rasio lengkap bukan otomatis tersedia. Jangan membangun marketcap kedua. |
| R10/R14 multi-aset/dana | `core/funds/MutualFundManager.java`, `MutualFund.java`; `core/multiasset/FundBuilderService.java`, `MultiAssetTradingService.java`; UI `openMultiAssets`, `openMutualFundsMarketModal` | Perluas model/transaksi canonical existing; pertahankan underlying, kas pool, cutoff, fee dan isolasi akun. |
| R12 news | `core/research/NewsArticle.java`, `ResearchService.java`: `getPublishedNews`, `getAlternativeData`; facade getter berita; NPC/matching | `sentimentImpact` existing ditemukan dipakai untuk agregat alternative Data. Keberadaan field sentimen tidak membuktikan dampak ke NPC/order/harga; acceptance harus trace event->sinyal/keputusan->order->fill->harga. |
| R13 koleksi | `core/collection/CollectibleItem.java`, `CollectionManager.java`; UI galeri shared | Gunakan ID existing dan migrasi roster tanpa menghapus ownership/agunan. Rarity/harga/unlock baru ditetapkan setelah jawaban pemilik. |

Catatan path: entri bertuliskan `core/...` adalah singkatan `java/com/tradingglobal3d/core/...`; entri `mobile/...` adalah `java/com/tradingglobal3d/mobile/...`.

### Pemeriksaan yang wajib tetap masuk long task, independen dari pilihan UI

- Setiap penambahan dana/koleksi/IPO mempunyai ID stabil, restore dari save lama, dan perilaku retry yang tidak menggandakan saldo/item/event.
- Rentang waktu news tidak boleh membocorkan isi/hasil aktual sebelum event. Countdown event dan proyeksi probabilistik berbeda dari pengetahuan hasil masa depan.
- Kalender monthly IPO/leapyear/USmelintasi tengah malam serta tutup bursa tidak boleh disamakan dengan tanggal perangkat nyata untuk hibah.
- Mengganti nama UI menjadi emiten tidak mengubah key domain yang tersimpan; komoditas/deposito/aset digital memakai nama produk yang sesuai.
- Data shareholder count/holders/fund holdings mempunyai definisi berbeda; unknown tetap unknown, semua angka simulasi ditandai.
- Pilihan pemilik sudah diterima; implementasi tetap belum dimulai oleh paket ini. Inventaris tidak meluluskan fitur.
