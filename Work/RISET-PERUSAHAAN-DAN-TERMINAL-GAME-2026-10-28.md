# Riset game pembanding: perusahaan dan terminal

**Tanggal:** 28 Oktober 2026. **Status:** penelitian dan usulan; belum persetujuan tambahan atau bukti implementasi. Sumber diperiksa melalui halaman resmi yang dapat dibaca umum, bukan hasil memainkan langsung game pembanding. Sebagian contoh berasal dari situs pengembang/handbook, sebagian dari halaman penerbit; uraian produk adalah klaim sumber yang dapat berubah. Jangan menyalin gambar, merek atau data yang belum jelas haknya. Game kita Android portrait/offline; mayoritas referensi adalah game desktop atau online sehingga perlu adaptasi, bukan pemindahan utuh.

## Keputusan yang sudah berlaku

- Terminal adalah layanan khusus di menu game; pemain membeli akses untuk mendapat informasi yang benar 1 hari game sebelum diumumkan, setara 1 jam nyata pada kecepatan 24×. Berita yang benar tidak menjamin arah atau besarnya perubahan harga. Sumber harus data simulasi game, bukan langganan resmi Bloomberg.
- Durasi efek berita spesifik/makro 60/120 menit nyata tetap terpisah dari periode akses awal. Pemilik memilih tampilan perkiraan sebelum rilis dan perubahan harga yang diamati sesudah rilis, serta menyetujui preview terbaru sebagai dasar desain: `Artifacts/Preview-Saran-2026-10-28/terminal-berita-perkiraan-dan-hasil-disetujui-dasar.png`. Angka dan cerita adalah ilustrasi. Cara menghitung perkiraan, periode hasil, cakupan berita, biaya, tier, perilaku NPC, waktu pasar tutup dan pengalaman saat offline belum dipilih.
- Perusahaan pemain bisa IPO setelah syarat kesiapan, usia dan keuangan; pemain memilih jumlah saham baru; hasil penerbitan masuk kas perusahaan. Hak khusus pendiri dipilih sebagai arah agar pemain tetap memegang kendali walau persentase turun; rincian suara dan keputusan yang dilindungi masih terbuka. Hak perusahaan lain memakai contoh B bertahap sebagai **dasar diskusi**, bukan angka final.
- R01–R16 telah disetujui sebagai dasar rencana; sebagian isi di bawah memperdalam keputusan tersebut. Setiap UI, teks dan alur baru perlu preview serta persetujuan eksplisit; perbaikan independen yang ditugaskan dapat berjalan saat menunggu. Game tidak menambahkan replay historis.

## I. Pembanding game: mekanisme yang didukung sumber

### 1. Capitalism Lab — perusahaan, anak usaha, dan pembagian saham

[Panduan resmi subsidiary financial management](https://www.capitalismlab.com/subsidiary-dlc/subsidiary-financial-management/) menjelaskan pendanaan anak usaha melalui injeksi modal, penerbitan saham dan pengaturan dividen. Saham baru kepada publik dapat mengurangi persentase induk; akses aksi tertentu bergantung kepemilikan dan status perusahaan. [Materi resmi baru](https://www.capitalismlab.com/new-content/) menyebut berita finansial dan pemantauan performa saham.

**Adaptasi usulan:** halaman usaha memperlihatkan arus dana usaha, kepemilikan sebelum/sesudah penerbitan, status perusahaan privat/tercatat, serta keputusan dividen dan alasan keputusannya. Gunakan `CompanyManager`/ledger yang ada jika memadai. Jangan menyalin syarat 50%/100% atau batas harga dari game pembanding karena pemilik memilih kendali pendiri khusus. Perubahan emisi bukan uang pribadi otomatis. Perlu preview, aturan valuasi, batas penerbitan dan perlakuan laba/utang.

### 2. Wall Street Raider — pasar dan perusahaan pada satu ruang kerja

[Situs resmi](https://www.wallstreetraider.com/) menampilkan dan mendeskripsikan dashboard harga, berita, grafik pendapatan, laporan riset, peta sektor, hubungan kepemilikan dan penyaring saham. [Halaman Steam](https://store.steampowered.com/app/3525620/Wall_Street_Raider/) menjelaskan analisis pendapatan, arus kas dan strategi perusahaan. Ini deskripsi pengembang/penerbit, bukan hasil uji game oleh peneliti.

**Adaptasi usulan:** jadikan terminal pintu masuk riset dan informasi awal yang dapat ditindaklanjuti pemain: detail berita → pihak terdampak → konteks saham → keputusan. Opsi penyaring, grafik dan peta kepemilikan perlu pilihan pemilik; tidak menjadikan informasi awal sebagai jaminan hasil. Akses saham tidak otomatis mengeksekusi order.

### 3. Offworld Trading Company — hubungan bisnis dan pasar

[Wiki resmi gameplay](https://wiki.offworldgame.com/index.php/Gameplay) menjelaskan keputusan produksi, pembelian input di pasar, peningkatan markas, lelang dan pembelian saham sebagai bagian dari kompetisi. Harga dan sumber daya pada game tersebut memiliki konteks strategi real-time yang berbeda dari game kita.

**Adaptasi usulan:** contoh skenario bisnis: bahan baku naik → biaya usaha meningkat → proyeksi laba berubah → pemain memilih bertahan atau mengubah operasi. Harga/rumus/risiko perlu dipilih pemilik dan dimainkan melalui model existing yang konsisten; jangan otomatis menambahkan sabotase, lelang, komoditas baru atau mengubah harga saham secara pasti.

### 4. Prosperous Universe — perusahaan, laporan dan terminal yang disesuaikan

[Situs resmi](https://prosperousuniverse.com/about) menggambarkan perusahaan memproduksi dan menjual pada pasar, serta menekankan bermain dengan ritme sendiri. [Panduan APEX](https://handbook.apex.prosperousuniverse.com/tutorials/current-tutorials/07-interface-guide/index.html) menggambarkan layar perusahaan, pemegang saham dan ringkasan keuangan; [catatan UI mobile](https://com.prosperousuniverse.com/t/release-notes-mobile-ui/3898) menyebut pendekatan layar/kartu. Game ini online dan berbasis interaksi pemain; halaman roadmap lama menandai pasar saham sebagai **future**, sehingga tidak dijadikan bukti fitur pasar saham sudah hadir.

**Adaptasi usulan:** pilih tampilan ringkas per topik pada ponsel dan masuk ke rincian ketika dibutuhkan; jangan memaksa pemain mengatur desktop bertile atau mengharuskan koneksi online. Rekomendasi ini tidak meniadakan jam bursa atau rekonsiliasi offline yang sudah dipilih.

## II. Rekomendasi berdasarkan alur game kita

| ID | Usulan yang bisa ditinjau | Terkait existing / keputusan | Bukti dan risiko sebelum diterapkan |
|---|---|---|---|
| GT-01 | Kartu berita awal berisi emiten, waktu publik, fakta dan akses ke saham. | Terminal awal satu hari game sudah dipilih; `ResearchService` dan UI terminal ada. | Fakta benar dan tersimpan; akses premium tidak bocor; tanggal relevan; preview. |
| GT-02 | Urutan jelas: bocoran → publik → efek berita → selesai; faktor lain tetap memengaruhi harga. | Efek 60/120 menit nyata sudah dipilih. | Uji offline melewati semua batas, berita bersamaan, reload, jam mundur, tidak ganda. |
| GT-03 | Perbandingan harga dan informasi emiten di satu alur. | Terminal existing memiliki jenis perbandingan dan konsensus, keberhasilan runtime belum dibuktikan. | Data nyata dalam dunia game, IDR/USD tidak dijumlahkan asal; preview mobile. |
| GT-04 | Tampilkan perkiraan dampak berita sebelum rilis dan perubahan harga setelah rilis. | Pemilik memilih keduanya dan menyetujui preview terbaru sebagai dasar desain; angka dan formula belum dipilih. | Jangan menyebut perubahan harga total sebagai sebab tunggal berita; atribusi perlu pemisahan yang benar, atau tandai tidak tersedia. |
| GT-05 | Perusahaan: keputusan usaha → proyeksi pemasukan, biaya dan laba → hasil aktual. | Owner menyukai arah proyeksi; `CompanyManager` dan `OperatingCompany` ada. | Integrasi, ledger, dana pribadi/usaha, kas minus, offline; tidak menjanjikan keuntungan. |
| GT-06 | Informasi modal usaha: transfer/injeksi, kas dan histori. | Metode transfer modal dan holding sudah ada. | Identitas transaksi, biaya, penggandaan, kondisi gagal serta save; UI perlu preview. |
| GT-07 | Kesiapan IPO berupa daftar syarat dan kemajuan yang bisa ditinjau. | Syarat usia/keuangan/kesiapan dipilih; angka belum dipilih. | Simulasi 3 ukuran usaha dengan uang, waktu 24× dan progress 1–3 hari nyata; syarat/valuasi/biaya tunggu pilihan. |
| GT-08 | Kalkulasi IPO perusahaan sendiri sebelum konfirmasi: saham lama, baru, nilai dan porsi sesudah. | Pemain pilih jumlah sendiri; hasil emisi masuk kas perusahaan. | Hak pendiri, jumlah saham, pembulatan, skenario kurang/penuh/kelebihan peminat, rollback. Tidak mendesak pemain pilih jumlah tertentu. |
| GT-09 | RUPS dan hak bertingkat yang mudah dipahami. | Pemilik memilih B sebagai dasar diskusi, E untuk kendali pendiri. | B bukan persetujuan angka; uraikan keputusan dilindungi, investor voting dan snapshot. |
| GT-10 | Aksi perusahaan memengaruhi kondisi dan terlihat di terminal, bila peristiwa sudah dibuktikan. | Dividend/split/RUPS existing di source; kelengkapan runtime belum dibuktikan. | Tanggal hak, jumlah saham, ledger, apakah pasar terpengaruh, no replay. |
| GT-11 | Rival dan 1.000 NPC berprofil, beragam strategi dan transaksi sah. | Peringkat berdasar hasil; avatar anime dasar dipilih. | Review APK-10 menemukan migrasi dan sambungan UI bermasalah; uji data aktual, activity dan performa Android, tidak hanya entri. |
| GT-12 | Jalur tujuan pendek: riset → keputusan → lihat hasil, opsional. | Misi pilihan dan tidak wajib beli sudah dipilih. | Ukur kesenangan/progres lewat playtest, bukan janji retensi; target/hadiah baru tunggu keputusan. |

## III. Perbedaan yang perlu diputuskan dan usulan uji

1. **Akses awal terhadap waktu pasar:** bila informasi keluar ketika pasar tutup, tampilkan status dan simpan sampai kapan informasi publik; aturan transaksi tetap mengikuti sesi yang akhirnya dipilih. Apakah keunggulan diperpanjang jika semua jendela jatuh saat tutup masih terbuka; jangan mengubahnya sendiri.
2. **Offline:** untuk berita terjadwal, tentukan waktu informasi yang layak dilihat ketika pemain kembali, tanpa menciptakan peluang transaksi yang tidak sempat dilakukan. Jika 1 hari game bocoran dan 60/120 menit nyata efek saling bertumpang tindih dengan offline panjang, data publikasi dan efek harus dijadwalkan konsisten. Jadwalkan notifikasi sesuai kemampuan Android, tanpa janji real-time saat proses mati.
3. **NPC dan keadilan:** informasi valid lebih dulu untuk pemain adalah pilihan, tetapi kapan NPC bereaksi, apakah mereka memiliki terminal dan perbedaan tier belum diputuskan. Uji kapitalisasi Rp5 triliun dan modal awal, ukuran IPO dan persentase pemesan besar.
4. **Kendali pendiri:** identifikasi ruang keputusan yang boleh ditentukan pemain meski saham <50%, hak investor di luar itu, dan konsekuensi penjualan seluruh saham pendiri. Hak istimewa game bukan otomatis interpretasi aturan BEI.
5. **Biaya dan informasi:** nama paket, nominal, masa langganan, status kadaluarsa, akses umum setelah publikasi dan penanganan pemotongan saat permainan offline perlu dipilih setelah audit fitur langganan existing. Tidak ada angka baru disetujui.
6. **Persentase dan risiko:** tentukan apakah rentang perkiraan adalah besaran dampak model atau prediksi harga, dan bagaimana hasil diatribusikan. Buat contoh positif, negatif, netral, berita tumpang tindih dan pengaruh lain; jika tidak bisa membuktikan, tampilkan perubahan total saja.

## IV. Batasan data dan verifikasi

- Hak akses halaman publik adalah untuk membaca riset, bukan otomatis lisensi mengunduh/menyebarkan kode, gambar, logo atau data harga; pakai informasi sebagai inspirasi dan verifikasi lisensi untuk setiap aset yang benar-benar dipakai.
- Tidak mengubah source, save, APK atau status agent. Keberadaan metode belum bukti UI/runtime. Audit APK-10 `Artifacts/Review-Indep-APK10-2026-10-28/LAPORAN-REVIEW.md` menandai masalah transaksi, migrasi dan IPO; cek ulang terhadap source saat pekerjaan benar-benar dimulai.
- Rujukan utama proyek: `Work/MASTER-PLAN-PERUSAHAAN-DAN-TERMINAL-2026-10-28.md`, master plan offline, plan `Plans/10-revisi-apk10-dan-riset/plan.md`. Buat preview baru dan minta persetujuan pemilik sebelum perubahan UI/flow; tidak mengklaim rekomendasi riset sudah dipilih.
