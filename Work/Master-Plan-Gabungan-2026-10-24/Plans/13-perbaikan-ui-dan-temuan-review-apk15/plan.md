# Plan 13: Perbaikan UI dan temuan review APK-15

**Keputusan Q&A terbaru 29 Oktober:** Autosave tanpa notifikasi sukses berulang; jika gagal tampilkan peringatan dan coba lagi serta lindungi progres. Whale minimal satu berita per tujuh hari game dengan menyiapkan kejadian simulasi masuk akal bila diperlukan; rumor lanjutan langka minimal sekali per 14 hari game. Konflik berita dan pembulatan tetap menunggu contoh/keputusan. Detail dan J01–J20 di Plan 14. Aturan terbaru menggantikan bagian lama yang bertentangan. Plan 14 dimulai hanya setelah pemilik meninjau dan menunjuknya.


Status: **rencana untuk ditunjuk pemilik kepada agent; belum dilaksanakan**. Tanggal penyusunan 29 Oktober 2026. Rencana ini menindaklanjuti analisis laporan Plan 12 dan UI dalam percakapan, bukan menyatakan seluruh Plan 12 telah diaudit atau seluruh bug telah ditemukan. Kerjakan hanya plan ini setelah pemilik menunjuknya. Jangan mulai plan berikutnya secara otomatis.

**Prioritas keputusan terbaru:** baca jawaban Q&A 21–70 di Plan 14 dan bagian 2A master plan. Dua berita Pro boleh mengenai emiten yang sama bila ada alasan; rumor Whale lanjutan dijamin dengan acuan penutupan hari sebelumnya; penghentian auto-renewal tidak mengakhiri akses sebelum periode berbayar habis. Konflik/pembulatan masih menunggu contoh. Keputusan terbaru menggantikan uraian lama di bawah hanya sejauh bertentangan; tidak memulai Plan 14.

**Keputusan pemilik 29 Oktober 2026 yang menggantikan bagian lama di bawah jika bertentangan:** saldo tidak cukup saat perpanjangan langsung turun ke Basic; pemain bisa menghentikan perpanjangan otomatis Pro/Whale. Pro dua berita 2–5% per hari bursa untuk dua emiten; Whale 1–2 berita eksklusif per tujuh hari game. Kejadian langka rumor lanjutan menggandakan persentase awal, dengan acuan, frekuensi dan jaminan lanjutan yang masih menunggu keputusan sebagaimana Plan 14. Tampilkan grid galeri dua kolom bergambar terlebih dahulu, tanpa menganggap desain final disetujui atau menghilangkan dua alternatif lain. Preview boleh dikelompokkan, persetujuan tetap per layar. Pemulihan save rusak ditawarkan setelah konfirmasi; game baru melindungi progres lama dan meminta konfirmasi sebelum penghapusan. Perlindungan save pemain dan izin perangkat tetap berlaku. Rincian selengkapnya di Plan 14; keberadaannya bukan instruksi memulai Plan 14.

## 1. Sumber dan batas kerja

- Root produksi: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`. `Game Trading Saham` adalah checkout berbeda. Verifikasi root, branch, HEAD, status kerja dan instruksi yang berlaku sebelum edit; kondisi kerja bisa berubah.
- Baca `AGENTS.md`, `DESIGN.md`, `MASTER-PLAN-OFFLINE-DAN-REALISME-2026-10-28.md` bagian keputusan terbaru, `Work/STATUS.json`, dan seluruh `Work/Master-Plan-Gabungan-2026-10-24/Plans/12-revisi-apk14-dan-keputusan-terbaru/plan.md`, `LAPORAN-HASIL-EKSEKUSI.md`, serta `MATRIKS-CAKUPAN.csv`. Telusuri sumber dan caller yang benar-benar terkait sebelum memilih perubahan.
- Prioritaskan keputusan pemilik terbaru yang terdokumentasi. Arah Tycoon Sinematik menggantikan arah warna lama sebagai dasar; keputusan rinci dan persetujuan implementasi per layar tetap berlaku. Jika dokumen berkonflik, catat sumber, tanggal dan keputusan yang berlaku. Jangan jadikan keputusan lama sebagai keputusan baru.
- Pertahankan seluruh perubahan lokal pengguna dan agent sebelumnya; jangan reset, clean, stash, hapus massal, overwrite, atau mengklaim delta kotor semata milik tugas ini. Save pemain `Artifacts/LocalPreview/preview-save/save_game_v2.json` hanya di-hash sebelum dan sesudah, jika ada; pengujian menggunakan save QA baru di lokasi terisolasi. Jangan membaca/menulis save pemain melalui aplikasi, mengubah konfigurasi global, menggunakan ADB, instalasi perangkat, push, publish, atau mengirim pesan eksternal. Jika dibutuhkan akses yang dibatasi, catat dan lanjutkan pekerjaan independen.
- Pertahankan Java sebagai pemilik ekonomi, waktu dan save, serta WebView dan aset lokal. Jangan mengubah biaya, saldo, rumus, jadwal atau fitur di luar keputusan dan cakupan ini. Jangan menambah dependency atau abstraksi spekulatif. Ikuti pendekatan Ponytail dan tinggalkan pemeriksaan runnable untuk logika nontrivial.

## 2. Status bukti awal dan cara memperlakukan temuan

Ini petunjuk audit pada snapshot saat penyusunan, bukan bukti bahwa kondisi masih sama saat agent mulai. Uji ulang sebelum memperbaiki. Tandai masing-masing subbutir `TERBUKTI`, `PERLU_REVISI`, `SEDANG_DIKERJAKAN`, `LULUS`, `BELUM_DIUJI`, `MENUNGGU_PREVIEW_PERSETUJUAN`, `MENUNGGU_KEPUTUSAN`, atau `TERHALANG`, dengan file, versi, bukti dan alasan. Buat inventaris subbutir dalam folder plan ini; bedakan bug terkonfirmasi, risiko yang perlu reproduksi, ketidakcocokan laporan, dan bukti yang belum ada. Jangan menganggap perintah ini sebagai persetujuan UI atau aturan produk yang belum dipilih.

- Terminal menggunakan `currentTier` pada `openResearchTerminal`, sedangkan deklarasi yang terlihat berada dalam fungsi hub lain; risiko `ReferenceError` harus direproduksi pada jalur sebenarnya.
- `TerminalTier` pada snapshot menetapkan Pro USD 500; keputusan terbaru mencatat Pro USD 250.000 dan Whale USD 1.000.000 uang game selama 30 hari game. Preview menampilkan USD 250.000. Verifikasi source, bridge, UI dan debit sebelum menyelaraskan dengan keputusan.
- `hasAccess` dapat mengubah tier menjadi Basic saat kedaluwarsa, sedangkan auto-renewal berjalan saat penyelesaian hari. Risiko akses terjadi sebelum perpanjangan perlu dibuktikan melalui urutan nyata, termasuk offline/restore/retry.
- Tutorial pada snapshot menyatakan aktif 5 menit/hari dan offline 1 jam/hari; keputusan terbaru menyatakan 24× saat aktif dan offline. Verifikasi alur waktu terpisah dari koreksi teks.
- Preview yang ada memuat pemilih layar, grafik placeholder, contoh tombol yang belum terhubung dan galeri dengan emoji; ini mockup, bukan bukti UI game lengkap atau alur produksi berjalan.
- Laporan Plan 12 mencampur ID R, memiliki rincian jumlah tes per kelas yang berjumlah 146 meskipun log terpisah menyatakan `OK (136 tests)`, dan mencantumkan ukuran APK berbeda dari pemeriksaan file. Verifikasi ulang angka dan bukti, jangan menganggap hasil log sebagai bukti daftar kelas.
- Hash APK-15 dan tiga aset web pernah cocok pada pemeriksaan sebelumnya, dan hash save saat itu cocok dengan angka laporan. Itu bukan bukti kelas Java, seluruh APK, perangkat, atau keadaan save sebelum pengerjaan terdahulu. Benchmark JSON yang ada adalah pengukuran host, bukan perangkat.

## 3. Pekerjaan kode dan integrasi

### K01. Terminal Riset satu pintu

Telusuri dari menu Lainnya hingga handler, data paket, halaman terminal, paket dan bridge. Reproduksi risiko variabel di luar cakupan. Perbaiki akar masalah dengan sumber status paket yang sudah ada; tampilkan Basic/Pro/Whale berdasarkan data aktual dan pesan jujur ketika data gagal dimuat. Jangan mengasumsikan Basic jika status tidak diketahui. Pastikan tindakan kelola paket, kembali, berita, akses terkunci, radar NPC dan layanan existing tetap terjangkau. Uji status Basic, Pro dan Whale, kegagalan bridge, perubahan paket, reload dan navigasi; sertakan minimal satu tes runnable yang gagal untuk bug yang direproduksi. Perubahan presentasi/teks/navigasi memerlukan alur preview dan persetujuan bagian 4.

### K02. Harga, manfaat dan auto-renewal

Pastikan Pro USD 250.000, Whale USD 1.000.000 dan periode 30 hari game sesuai keputusan terbaru, pada domain, bridge, teks UI dan debit. Harga ini bukan opsi $500 atau usulan IDR yang perlu dipilih lagi. Gunakan mata uang dan saldo game yang benar; jangan ubah ekonomi lain. Telaah semua caller `hasAccess`, proses harian, langganan, penyimpanan dan pemulihan. Reproduksi lalu perbaiki jika akses lebih dulu menyebabkan paket yang seharusnya diperpanjang menjadi Basic. Uji cukup/tidak cukup, satu debit per periode, akses sebelum/pada/sesudah jatuh tempo, urutan akses versus settlement, lintas hari, retry, restart, offline dan tidak ada saldo negatif/duplikasi. Saldo kurang langsung turun Basic; pemain bisa menghentikan perpanjangan. Detail pergantian paket dan biaya offline yang belum dipilih tetap memerlukan keputusan; preview dan approval diperlukan untuk alur UI.

Audit juga aturan kabar Pro 2–5% dan Whale >20% terhadap harga buka dan tutup bursa berikutnya, waktu informasi dan akses tier sebagaimana A05 Plan 12. Buktikan dengan uji kejadian nyata dan harga, bukan teks saja. Pertahankan deadline efek berita 60/120 menit nyata yang berlaku. Mekanisme konflik, pembulatan dan kasus belum dipilih jangan ditentukan sepihak; jika keputusan diperlukan, tandai dan kerjakan bagian lain. Jangan menjanjikan laba bersih pemain.

### K03. Waktu dan teks tutorial

Audit clock aktif/offline dan semua teks terkait. Pertahankan 24×, 1 hari game sekitar 1 jam nyata, dan bursa setiap hari game 09.00–16.00. Teks tutorial yang bertentangan diperbaiki setelah preview/approval khusus; perubahan mekanisme hanya jika pengujian membuktikan gap terhadap keputusan dan cakupan yang berlaku. Uji batas sesi, transisi aktif/offline, reload dan efek berita tanpa mengubah save pemain. Istirahat/pengecualian bursa masih terbuka.

### K04. NPC, IPO dan order: validasi temuan sebelumnya

- NPC: uji save QA baru dan save QA lama yang sudah memiliki akun NPC; bedakan seed akun baru dari migrasi existing. Buktikan sampel portofolio dan saldo nyata, ledger, kepemilikan/saham beredar, idempotensi dan save/reload. Jika migrasi berpotensi mengubah ekonomi atau progres pemain tanpa aturan yang disetujui, jangan memaksakannya: jelaskan dan ajukan pilihan.
- IPO: uji sebelum, selama dan sesudah masa penawaran serta batas listing, alokasi, dana/reserve dan save lama. Tiga hari game adalah keputusan; detail fase dan migrasi jadwal lama belum otomatis diputuskan.
- AON dan antrean pasar tutup: uji contoh lawan 100 lalu 300, sisi beli dan jual, maker/taker, kecukupan dana/volume, posisi, fee, ledger, reserve, retry/reload dan prioritas yang relevan. Jangan menyamakan lembar dengan lot. Hindari menyatakan semua kasus aman dari satu probe. Pertahankan order tutup menunggu sesi berikutnya; saat buka order tidak otomatis dijamin terisi.

### K05. Integritas profil NPC dan isi UI

Gunakan dua peringkat berlabel yang konsisten dengan leaderboard, termasuk tie dan data tidak tersedia. Uji beberapa NPC, pemain dan save/reload. Pastikan status bisnis/portofolio berasal dari data domain; jika tidak ada, tampilkan belum tersedia alih-alih klaim generik yang tidak terbukti. Cek nilai panjang dan layar kecil. Preview dan persetujuan dibutuhkan untuk perubahan tampilan/teks.

## 4. Preview, persetujuan dan penerapan UI

Urutan review: **Beranda → Pasar → Portofolio → Bisnis → Lainnya**, kemudian terminal/paket, profil NPC, IPO, leaderboard, galeri dan dialog terkait. Jangan menganggap pilihan gaya dasar sebagai persetujuan seluruh layar. Pertahankan lima tab, avatar bulat menuju Profil, label Tertahan tanpa panah dan dapat diketuk, kartu indeks ringkas, Portofolio B, Profil A, ikon launcher, avatar NPC, serta akses semua fitur yang masih berlaku. Tampilkan sumber persetujuan relevan; bila ada konflik dengan pilihan terbaru, catat dan ajukan, jangan menebak.

Untuk setiap layar/komponen/teks/alur yang ingin diubah: tampilkan preview yang benar-benar terlihat atau tautan preview lokal yang dapat dibuka, jelaskan perubahan dan alasan, beri ID tetap, tampilkan data contoh berlabel serta state kosong/error/offline yang relevan. Tawarkan `Setuju`, `Setuju dengan perubahan`, `Belum cocok/buat ulang`, `Tunda` melalui Q&A bila tersedia. **Jangan menerapkan perubahan itu sebelum persetujuan eksplisit untuk item tersebut.** Laporkan setiap preview siap ditinjau dan setiap perubahan yang sudah disetujui dan diterapkan. Sambil menunggu, lanjutkan kode dan verifikasi independen; jangan meminta persetujuan ulang untuk hal yang sudah spesifik disetujui.

Preview harus memperlihatkan shell ponsel portrait dan navigasi bawah lima tab, hierarki data dan akses fitur yang ada. Bedakan tombol contoh dengan interaksi yang benar-benar berfungsi; jangan menyatakan mockup sebagai fitur terpasang. Grafik contoh harus dapat dinilai dan data simulasi diberi label. Jangan mengganti perilaku domain dengan data palsu.

**Galeri:** siapkan tiga alternatif yang dapat dibandingkan (grid dua kolom, daftar horizontal, katalog berjenjang) menggunakan aset yang tersedia dan memiliki izin, contoh nama/harga/status panjang dan singkat, dimiliki/belum dimiliki, pencarian, kategori, filter kepemilikan, serta fallback bila gambar tidak tersedia. Validasi jumlah dan identitas aset dari sumber game; jangan mengklaim 150 aset bergambar telah siap sebelum dibuktikan. Pilihan desain masih menunggu pemilik.

Verifikasi yang sesuai setelah persetujuan: viewport ponsel sempit dan umum, teks sistem besar/200% bila lingkungan memungkinkan, keterbacaan, kontras, target sentuh, keyboard/fokus/Back, scroll, safe area dan state kosong/error/offline. Catat apa yang benar-benar diuji. Tes browser tidak membuktikan WebView Android atau ponsel fisik. Jika akses visual atau browser ditolak oleh sistem, jangan mengakalinya; laporkan pembatasan dan minta tindakan pengguna hanya bila benar-benar diperlukan.

## 5. Bukti, laporan dan penutupan

1. Rekonsiliasi laporan Plan 12 terhadap ID sebenarnya pada `plan.md`, termasuk R06 portofolio, R08 nama order, R13 notifikasi, R15 karier/bisnis dan R16 visual/aksesibilitas. Perbaiki pemetaan dan status hanya berdasarkan bukti; jangan menghapus catatan historis. Perbaiki struktur CSV agar setiap kolom terbaca, pertahankan isi yang relevan dan lengkapi bukti per subbutir. Lingkup Plan 13 tidak otomatis menutup semua kriteria Plan 12.
2. Verifikasi log dan daftar tes yang benar, catat kelas yang dijalankan, jumlah, pengecualian, perintah, exit code dan snapshot. Rekonsiliasi angka 146 vs 136 tanpa menebak. Jalankan tes terarah untuk perubahan dan suite relevan; tulis log baru dalam folder QA tugas ini, jangan timpa bukti lama. Bila syarat stress proyek berlaku, jalankan 10×100 secara terisolasi atau jelaskan keterbatasannya.
3. Periksa benchmark 1.000 NPC dengan input, lingkungan, ukuran sampel dan batas yang jelas. Pisahkan host dari render browser dan perangkat. Jangan gunakan benchmark host untuk mengklaim performa ponsel.
4. Jika APK baru diperlukan setelah pekerjaan independen, bangun kandidat debug dengan nama baru dan catat sumber/snapshot, hash, ukuran aktual, tanda tangan serta perbandingan aset dan kelas/config sesuai yang dapat diverifikasi. Tidak menginstal atau mempublikasikan. Kandidat yang masih memiliki UI menunggu persetujuan tidak boleh disebut final lengkap.
5. Buat `LAPORAN-HASIL-PLAN-13.md`: hasil setiap ID dan subbutir, masalah yang ditemukan, perubahan, uji dan log, hash save sebelum/sesudah, status persetujuan, kekurangan dan langkah berikut. Pisahkan bukti source, host, browser, build APK, emulator dan ponsel. Akhiri dengan hal yang benar-benar masih terbuka, termasuk persetujuan UI dan kuota Whale. Jangan menanyakan lagi harga, jam bursa, saldo kurang atau kuota Pro seolah belum dipilih.
6. Nyatakan selesai hanya untuk bagian yang memenuhi syarat. Jika approval atau bukti perangkat belum ada, tuliskan `MENUNGGU_PREVIEW_PERSETUJUAN` atau `BELUM_DIUJI`; jangan mengklaim 100% atau 0 terlewat. Hentikan setelah hasil diserahkan; jangan otomatis memulai plan lain.
