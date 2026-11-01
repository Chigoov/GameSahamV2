# Riset sumber terbuka untuk Game Saham — 28 Oktober 2026

**Status: pemilik menyetujui R01–R16 sebagai dasar rencana setelah melihat preview; detail aturan dan tampilan yang belum diputuskan tetap terbuka.** Persetujuan ini bukan bukti fitur telah diimplementasikan atau izin otomatis melanjutkan di luar lingkup yang ditunjuk. Rujukan bisa dibaca publik; akses terbuka tidak otomatis berarti data, gambar, atau kode boleh dikemas dan didistribusikan. Keputusan pemilik di master plan, preview UI, operasi offline, ekonomi 24×, dan batas `AGENTS.md` tetap berlaku.

## Ringkasan sumber dan kondisi proyek

Pemeriksaan source terbatas pada pembacaan file, bukan pengujian runtime: `CorporateActionService.java` menyimpan aksi korporasi, jadwal IPO, penawaran, dan agenda RUPS contoh; `TradingFacade.java` memiliki jalur dividen dan aksi korporasi; `LeagueManager.java` memuat 20 NPC bawaan dengan akun, order periodik dan peringkat; `PeriodicMissionManager.java`, `CompanyManager.java`, `InstallmentLoanManager.java`, `EconomyLedger.java` dan `SaveManager.java` juga ada. Keberadaan class/metode tidak membuktikan fitur lengkap, terhubung UI, seimbang atau berjalan pada APK.

Data awal `android/app/src/main/assets/data/market_universe.json` berisi 50 IDX dan 50 US aktif. Dari 50 IDX, 37 mempunyai kapitalisasi awal Rp5 triliun; rata-rata kira-kira Rp82,71 triliun dan median Rp5 triliun (harga awal × saham beredar). Harga saat permainan berlangsung dapat berubah; valuasi usaha pemain/IPO baru belum otomatis sama. Dengan asumsi ilustratif emiten Rp5 triliun menawarkan 20% saham, ambang pemesan besar 20% dari saham IPO berarti 4% perusahaan atau Rp200 miliar sebelum biaya. Ini risiko aksesibilitas, bukan bukti aturan harus diganti.

## Usulan produk dan pengujian

Pemilik telah menyetujui seluruh rekomendasi di bawah sebagai dasar rencana. Detail yang belum dipilih, preview lanjutan dan persetujuan perubahan UI tetap diperlukan; jangan menyimpulkan setiap rumus atau angka telah disetujui.

### R01 — Perjalanan IPO dengan nilai yang masuk akal untuk pemain baru
- **Ide:** dalam layar IPO yang sudah dipilih, jelaskan total saham ditawarkan, ukuran satu lot, rentang biaya dan status pesanan. Buat simulasi pengujian untuk pemain dengan modal kecil dan NPC besar, sebelum menentukan batas, harga, atau hadiah.
- **Alasan:** aturan 20% dari penawaran dapat berarti ratusan miliar rupiah; pemain tetap dapat memesan sebagai kelompok lain. Penjatahan proporsional, 60:40 dan kriteria 20% telah dipilih, jangan ubah diam-diam.
- **Dasar:** [SEOJK 25/SEOJK.04/2025](https://ojk.go.id/id/regulasi/Pages/SEOJK-25-SEOJK04-2025-Verifikasi-Pesanan-dan-Dana%2C-Alokasi-Penjatahan-dan-Penyelesaian-Pemesanan-Efek-dalam-Penawaran-Umum.aspx) membahas pesanan dan penjatahan; angka 20%/60:40 adalah aturan game, bukan klaim aturan OJK.
- **Uji:** beberapa ukuran emiten/penawaran; pemain dengan modal awal; oversubscription; biaya dan refund; hasil penuh, sebagian atau nihil pada IPO; total tidak melebihi saham dan perbedaan dengan aturan transaksi pasar. **Perlu:** UI dan ukuran IPO baru disetujui.

### R02 — Agenda dividen dan hak kepemilikan
- **Ide:** manfaatkan aksi korporasi existing untuk kalender sederhana dengan tanggal hak, tanggal bayar, status pemegang dan rincian dividen yang benar-benar tercatat.
- **Dasar:** [Investor.gov: tanggal dividen](https://www.investor.gov/introduction-investing/investing-basics/glossary/ex-dividend-dates-when-are-you-entitled-stock-and) menjelaskan record date dan ex-date untuk pasar AS. Bukan otomatis aturan tanggal BEI.
- **Uji:** beli/jual sekitar tanggal yang ditetapkan game; snapshot hak; offline/restart; pembayaran tepat sekali; posisi klien dipisah; keterbacaan waktu game. **Perlu:** preview dan aturan kalender game; jangan menjanjikan dividen selalu ada.

### R03 — Dampak aksi korporasi yang bisa dipahami
- **Ide:** jelaskan split, reverse split, dividen, rights issue dan buyback dalam ringkasan sebelum/sesudah, serta tawarkan keputusan hanya pada peristiwa yang domain sungguh dukung.
- **Dasar:** [BEI — materi aksi korporasi](https://rdis.idx.co.id/id/events/corporate-action-discussion-2). `CorporateActionService` dan tipe aksi sudah ada, tetapi cakupan end-to-end belum diaudit.
- **Uji:** jumlah saham, harga, pembulatan, order tertahan, ledger, peristiwa duplikat, saham beredar dan save. **Perlu:** pilihan aksi yang diprioritaskan dan preview; jangan mengklaim harga otomatis untung.

### R04 — Peta kepemilikan dan pengaruh secara bertahap
- **Ide:** perkuat rencana daftar ≥1%, total publik, kepemilikan pemain, serta hak suara dan agenda berdasarkan data akun. Sajikan secara ringkas lalu rincian bila dibuka.
- **Dasar:** [OJK Buku Saku Pasar Modal](https://www.ojk.go.id/id/berita-dan-kegiatan/info-terkini/Documents/Pages/Buku-Saku-Pasar-Modal/BUKU%20SAKU%20PSR%20MODAL%20OJK%202023.pdf) membahas kepemilikan, dividen dan RUPS. Kode agenda RUPS ada; daftar pemegang menyeluruh dan hak bertingkat belum dibuktikan.
- **Uji:** 0,99%/1%, total saham, snapshot, dividen, akun klien, penurunan hak, penerbitan IPO dan profil NPC. **Perlu:** ambang hak, aturan kendali, preview; bedakan pemilik tidak diketahui dari publik.

### R05 — Perbandingan kondisi IPO, bukan sekadar harga
- **Ide:** dalam tab IPO yang sudah dipilih, gunakan data perusahaan dan rencana penerbitan untuk perbandingan kas, laba, risiko dan rencana penggunaan dana; data belum ada ditandai belum tersedia.
- **Dasar:** [SEC EDGAR APIs](https://www.sec.gov/search-filings/edgar-application-programming-interfaces) menyediakan riwayat pengajuan dan XBRL perusahaan AS, bukan harga saham; tidak mencakup keseluruhan perusahaan IDX.
- **Uji:** data nol/kosong/rugi, unit uang, timestamp, daftar IPO tanpa data; tidak mengubah aturan offline. **Perlu:** preview serta audit sumber dan hak jika data asli dikemas; tidak menghidupkan kembali replay historis.

### R06 — Ringkasan konsentrasi portofolio yang tidak memaksa
- **Ide:** tampilkan komposisi berdasarkan perusahaan/sektor dengan penjelasan singkat bahwa konsentrasi dapat menambah risiko; jangan memaksa pembelian atau mengklaim kepastian untung.
- **Dasar:** [Investor.gov — asset allocation/diversification](https://www.investor.gov/introduction-investing/getting-started/asset-allocation) menjelaskan penyebaran aset; tidak menjamin terhindar rugi. `app.js` sudah memuat data posisi/ringkasan; pembagian sektor dan kelengkapannya perlu audit.
- **Uji:** portofolio kosong, satu saham, beberapa sektor, kas IDR/USD, nilai nol dan kurs, grafik dapat dibaca tanpa warna saja. **Perlu:** preview dan pilihan pemilik.

### R07 — Bukti transaksi yang mudah dibaca
- **Ide:** di ringkasan transaksi tunjukkan jumlah, harga aktual, nilai, biaya, perubahan kas, waktu dan status, dengan rincian historis yang konsisten dengan ledger.
- **Dasar:** [Investor.gov — dampak biaya](https://www.investor.gov/introduction-investing/general-resources/news-alerts/alerts-bulletins/investor-bulletins/updated). Kode sudah memiliki ledger dan posisi; audit output transaksi sebenarnya terlebih dahulu.
- **Uji:** beli/jual, fee, order gagal, harga bergerak, restart dan duplikasi. Pertahankan full-fill atau tidak terisi untuk transaksi pemain. **Perlu:** preview, bukan perubahan tarif.

### R08 — Hindari nama Limit yang menyesatkan
- **Ide:** audit label dan penjelasan order yang sebelumnya disebut Limit; tunjukkan apa yang terjadi jika harga berubah dan saldo tidak cukup, tanpa meminta pemain memilih harga referensi jika keputusan itu tetap berlaku.
- **Dasar:** [SEC — memahami jenis order](https://www.investor.gov/introduction-investing/general-resources/news-alerts/alerts-bulletins/investor-bulletins-14) menjelaskan limit sebagai batas harga; ini rujukan AS, bukan aturan game/BEI yang otomatis berlaku.
- **Uji:** harga cocok, harga berubah, sesi tutup, dana kurang, reserve dan pembatalan. **Perlu:** definisi harga cocok dan preview; pemilik menolak fraksi/hambatan tambahan dan tidak meminta lawan transaksi baru.

### R09 — Rival dekat posisi pemain
- **Ide:** selain daftar 1.000 NPC berdasarkan hasil nyata, tampilkan posisi pemain dan beberapa rival di sekitarnya serta perubahan posisi yang benar-benar terjadi, bukan peringkat acak.
- **Dasar:** [ABIDES](https://github.com/abides-sim/abides) adalah contoh riset simulasi agen pasar; `LeagueManager` sudah memiliki rival dan ranking. ABIDES bukan bukti 1.000 NPC ini sudah bekerja di Android.
- **Uji:** urutan tie stabil, nilai real dari akun, pemain baru, 1.000 NPC, restart/offline, waktu buka daftar. **Perlu:** preview dan pilihan; jangan impor framework Python atau menjalankan model bahasa untuk tiap NPC.

### R10 — Target pribadi yang dapat dipilih
- **Ide:** padukan tujuan singkat bebas pilihan dengan kemajuan pribadi, di samping leaderboard. Contohnya menyelesaikan pemahaman bisnis atau diversifikasi tanpa harus selalu unggul dari NPC kaya.
- **Dasar:** [ulasan terbuka tentang motivasi dan gamifikasi](https://pmc.ncbi.nlm.nih.gov/articles/PMC10448467/) menemukan hasil bergantung konteks dan potensi motivasi menurun seiring waktu; populasinya pelajar, bukan bukti angka retensi game ini. Misi dan karier sudah memiliki class di source.
- **Uji:** pilih/abaikan, hadiah hanya untuk misi selesai, klaim otomatis tepat sekali, progres 1–3 hari nyata dengan tiga gaya bermain. **Perlu:** target/hadiah dan preview bila berubah.

### R11 — Aktivitas berbeda saat pasar tutup
- **Ide:** gunakan pilihan yang sudah disetujui: kelompok riset/pantauan dan bisnis/aset dapat ditemukan ketika pasar tutup, sementara order yang dibuat menunggu sesi buka. Berikan konteks kapan sesi berikutnya setelah jadwal diputuskan.
- **Dasar:** [SEC — pelaksanaan order](https://www.investor.gov/introduction-investing/investing-basics/how-stock-markets-work/executing-order) menerangkan order dapat menghadapi harga yang berubah. Itu rujukan edukasi AS, bukan jaminan/aturan bursa game.
- **Uji:** tutup/buka, suspend, simpan, offline, waktu game 24×, saldo/penolakan dan tombol kembali. **Perlu:** jam bursa final dan preview gabungan; tidak menambahkan kontrol skip/pause.

### R12 — Ringkasan kembali bermain yang dapat ditelusuri
- **Ide:** implementasikan pilihan pemilik untuk daftar seluruh kejadian yang memang tercatat, dengan akses ke order, berita, dividen, bisnis dan tagihan; jika histori parsial, jelaskan cakupannya.
- **Dasar:** [Android — offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first) membahas penyimpanan dan pemrosesan yang tahan kondisi offline; bukan bukti sistem game sudah deterministik.
- **Uji:** tidak ada kejadian, banyak kejadian, kejadian identik, data gagal dibaca, waktu mundur, restart, daftar tidak mengubah transaksi. **Perlu:** preview state tambahan; jangan merekayasa hasil selama proses mati.

### R13 — Notifikasi per kategori dengan batas OS yang jujur
- **Ide:** tetapkan lima kategori yang sudah dipilih pemilik—order, berita, IPO, bisnis, tagihan—dapat diatur pemain; utamakan peristiwa yang benar-benar terjadi atau jadwal yang pasti.
- **Dasar:** [izin notifikasi Android](https://developer.android.com/develop/ui/compose/notifications/notification-permission) dan [penjadwalan pekerjaan](https://developer.android.com/develop/background-work/background-tasks/persistent). Android 13+ memerlukan izin untuk notifikasi non-exempt; pekerjaan persisten tidak berarti eksekusi tepat waktu atau simulasi berjalan terus.
- **Uji:** izin ditolak/diterima, kanal, deep link, baterai, restart, jam berubah, rangkap dan force-stop sesuai batas OS; perangkat fisik jika diizinkan. **Perlu:** preview kontrol; jangan menjanjikan fenomena yang belum dihitung saat aplikasi mati.

### R14 — NPC sebagai pelaku ekonomi, bukan angka kosmetik
- **Ide:** kembangkan 20 rival existing menuju 1.000 akun unik dengan perilaku heterogen, frekuensi bervariasi, posisi, kekayaan dan order yang dilacak. Gunakan kembali mesin ekonomi existing.
- **Dasar:** [ABIDES](https://github.com/abides-sim/abides) mendemonstrasikan simulasi multiagen berbasis peristiwa pada konteks riset; ini bukan benchmark ponsel game kita. Source `LeagueManager` saat ini berisi 20 bawaan dan menyimpan posisi akun canonical.
- **Uji:** 1.000 ID unik, partisipasi, saham beredar, saldo, portofolio dan ranking; replay identik berdasarkan seed yang disimpan; logout/restart; waktu catch-up, RAM dan baterai pada perangkat. **Perlu:** kriteria aktif, strategi, alokasi awal, migrasi dan preview profil; jangan mengubah aturan jual-beli pemain.

### R15 — Karier dan bisnis dengan konsekuensi yang terbaca
- **Ide:** tampilkan syarat karier yang telah dipilih, serta hasil keputusan bisnis berupa penerimaan, biaya dan proyeksi laba. Hubungkan penjelasan hasil ke kejadian yang benar-benar tercatat.
- **Dasar:** [Investor.gov — sumber risiko dan perubahan nilai](https://www.investor.gov/introduction-investing) menjelaskan harga dipengaruhi berbagai faktor; hubungan bisnis terhadap harga di game perlu model dan uji sendiri. `CompanyManager` dan data fundamental ada; hubungan menyeluruh belum terbukti.
- **Uji:** proyeksi rugi/untung, kekurangan dana, pajak/biaya yang memang ada, pemrosesan harian dan skenario offline; jangan menduplikasi uang pribadi dengan kas bisnis. **Perlu:** preview dan persetujuan rumus baru.

### R16 — Kualitas visual dan aksesibilitas yang tetap terasa game
- **Ide:** gunakan avatar anime yang dipilih sebagai dasar, ikon dan state ringkas, kontras memadai, serta tombol yang mudah ditekan; tampilkan semua preview UI sebelum penerapan.
- **Dasar:** [Android core app quality](https://developer.android.com/develop/adaptive-apps/quality-guidelines/core-app-quality) merekomendasikan target sentuh 48 dp serta kontras yang dapat diukur. Menghapus pengaturan ukuran teks dalam game tidak berarti mengabaikan pengaturan perangkat.
- **Uji:** beberapa ukuran layar, ukuran teks perangkat, TalkBack/label, Back, kontras dan state error/offline. **Perlu:** preview per perubahan; tidak menyatakan 100 avatar sudah menjadi file terpisah atau terpasang.

## Sumber terbuka: hak dan keterbatasan

| Sumber | Kegunaan potensial | Batas |
|---|---|---|
| [World Bank data licensing](https://datacatalog.worldbank.org/public-licenses) | Parameter ekonomi makro untuk menginspirasi skenario; data World Bank tertentu CC BY 4.0. | Periksa lisensi dan metadata dataset satu per satu, sumber, atribusi dan perubahan. Data makro tidak otomatis data harga saham real-time; jangan jadikan game online. |
| [SEC EDGAR APIs](https://www.sec.gov/search-filings/edgar-application-programming-interfaces) | Dokumen/fundamental perusahaan AS sebagai bahan analisis atau snapshot. | Akses API bukan izin otomatis atas setiap isi yang diunggah pihak ketiga; cek kebijakan akses dan hak sebelum mendistribusikan. Tidak menyediakan cakupan pasar IDX. |
| [ABIDES project](https://github.com/abides-sim/abides) | Referensi desain simulasi NPC. | Python untuk riset; tidak terbukti cocok sebagai dependency Android. Periksa lisensi repositori dan hak dataset bila benar-benar menggunakan kode/data. |
| [Kenney support](https://kenney.nl/support) | Referensi aset permainan berlisensi CC0 yang relevan jika dipilih. | Periksa lisensi file dan kecocokan visual masing-masing paket; tidak menggantikan avatar anime yang dipilih atau otomatis menyetujui perubahan UI. |
| [London Stock Exchange data licensing](https://www.londonstockexchange.com/en/markets-products/market-data/market-data-licensing) | Contoh mengapa data pasar harus ditinjau haknya. | Sumber tersebut menyatakan penggunaan/redistribusi data tertentu butuh lisensi; tidak berarti semua sumber pasar memiliki syarat yang sama. |

## Pengujian dan prioritas yang diusulkan

1. Setelah P00, prioritaskan integritas save dan ekonomi, konsistensi waktu offline, serta aksesibilitas IPO skala realistis. Tinjau laporan Hermes sebelum memberi instruksi implementasi baru.
2. Bandingkan pemain baru, pemain fokus bisnis, dan pemain santai/offline: waktu nyata, waktu aktif, keputusan bermakna, kekayaan, akses IPO dan kesempatan naik level. Ukur target 1–3 hari nyata; jangan mengklaim sudah tercapai.
3. Buat preview hanya untuk pilihan UI yang pemilik ingin tinjau. Jangan menganggap satu persetujuan gambar sebagai persetujuan semua state/layout.
4. Untuk setiap usulan yang dipilih, catat source, hasil sebelum/sesudah, biaya implementasi, kondisi ekstrem, pengujian dan status. Tes browser/host/APK/Android fisik dilaporkan terpisah.

## Belum ditetapkan

R01–R16 disetujui sebagai dasar rencana; detail yang masih terbuka dicatat pada plan revisi. Keputusan lama tentang tidak adanya replay historis, bursa buka setiap hari game, 24×, notifikasi terpilih, order penuh/tidak terisi, IPO, warna dan preview tetap berlaku. Riset ini tidak menyatakan bahwa fitur telah diimplementasikan atau perangkat sudah diuji.
