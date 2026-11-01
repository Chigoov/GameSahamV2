# Plan 15 — Portofolio ringkas, posisi tombol beli/jual, dan pemulihan progres

Tanggal 30 Oktober 2026. Status: DRAFT untuk ditinjau pemilik. Pembuatan plan bukan perintah menjalankan perbaikan atau menyatakan siap rilis. Satu plan aktif pada satu waktu; pemilik menunjuk plan ini setelah menyerahkan pekerjaan lain yang sedang aktif. Lingkup adalah tiga masalah yang dilaporkan serta pilihan dua tema yang diminta pemilik; akun/cloud dan fitur pemberitahuan update APK dibahas terpisah dan tidak otomatis diimplementasikan.

## Arah terbaru dan hubungan dengan pekerjaan berikut

Pemilik memilih aplikasi Android yang tetap bisa offline, sinkronisasi akun saat online untuk pemulihan lintas perangkat, satu pemain dengan NPC, tanpa server simulasi yang harus berjalan terus, dan sasaran tanpa biaya layanan. Pemilik membatalkan versi game web; jangan menjadikannya tugas. Keputusan ini menggantikan usulan server dunia bersama yang selalu berjalan. Pemilik memilih satu jam nyata saat aplikasi ditutup dianggap sekitar satu hari game dan dihitung secara lokal saat dibuka kembali; jangan memangkas waktu atau mengubah aturan ekonomi sepihak.

Dalam plan ini, prioritaskan B03 sebagai fondasi sebelum sinkronisasi akun: identifikasi penyebab dan buktikan perlindungan save lokal. B01 dan B02 tetap lingkup yang diminta; kerjakan diagnosis independen dan preview sambil menunggu keputusan. Akun tidak boleh diimplementasikan hanya karena plan ini ditunjuk; versi web telah dibatalkan. Setelah Plan 15 diserahkan, pemilik dapat menunjuk `Work/RENCANA-AKUN-DAN-CADANGAN-OFFLINE-2026-10-30.md` untuk fase berikut.

## Situasi dan perlindungan segera

Foto HP 02:09 menampilkan “Progres terakhir belum tersimpan” dan “Penyelarasan waktu bursa otomatis terhenti: Progres membutuhkan pemulihan”. Pemilik menjelaskan bahwa peringatan muncul saat membuka kembali game. Foto tidak membuktikan file rusak, save hilang atau penyebab tertentu; pesan bukan bukti data terakhir berhasil tampil (angka pada foto masih “Memuat…”).

Jangan menyuruh pemilik menghapus aplikasi/data, mereset game, menekan mulai baru, memulihkan backup, atau mencoba menyimpan berulang tanpa diagnosis dan kebijakan perlindungan. Tindakan ini dapat menimpa bukti atau progres. Jangan membaca, menarik, menyalin, memodifikasi, merestore, migrasi atau menghapus save di HP tanpa izin yang secara spesifik mencakup tindakan tersebut; tidak menganggap izin lama otomatis berlaku untuk isi data sekarang. Save pemain di `Artifacts/LocalPreview/preview-save/save_game_v2.json` tetap hanya di-hash. Gunakan save QA sintetis dan terisolasi untuk seluruh reproduksi. Data pribadi jangan dicatat di log. Periksa waktu, ruang, versi dan status dengan metode yang diizinkan saja. Jika bukti perangkat dibutuhkan, jelaskan prosedur dan risiko; jangan mengakali penolakan atau mengklaim solusi tanpa bukti.

Root produksi: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`. Baca AGENTS.md, DESIGN.md, keputusan terbaru master plan, rencana/temuan startup terkini serta bukti kandidat. Periksa cwd/Git root, branch, HEAD, dirty tree, source dan hash kandidat. Pertahankan seluruh perubahan lokal; jangan reset/clean/stash, mass delete, menjalankan launcher paralel, ubah pengaturan global, push/publish, mengirim pesan, instal/uninstall atau memakai ADB di luar izin yang berlaku. Koordinasi lintas chat tidak dilakukan tanpa instruksi langsung pemilik. Kode dan hasil sebelumnya bukan bukti current device lulus.

## B01 — Portofolio terlalu boros ruang

Bukti pemilik: `C:/Users/HYPE AMD/Downloads/WhatsApp Image 2026-10-30 at 02.06.35 (1).jpeg` menampilkan kartu kepemilikan tinggi dengan kode, perusahaan, nilai, P&L, jumlah, rata-rata, harga pasar. Foto referensi latar hitam: `C:/Users/HYPE AMD/Downloads/WhatsApp Image 2026-10-30 at 02.06.35.jpeg` menampilkan ringkasan dan baris saham yang lebih padat, dengan kode, nilai dan P&L nominal/persen. Ini referensi hierarki dan kepadatan, bukan izin menyalin merek, logo, aset, atau mengubah keseluruhan tema Tycoon Sinematik.

- Siapkan preview nyata layar portofolio menggunakan data simulasi berlabel: baris ringkas dibanding kartu saat ini; tunjukkan daftar beberapa saham sekaligus, kondisi untung/rugi/impas, dan detail yang tetap dapat dibuka. Pertahankan informasi penting jumlah, harga rata-rata, harga pasar, P&L, akun, transaksi, dan fungsi yang sudah disetujui, dengan detail tersedia tanpa mengarang data. Nilai “diinvestasikan” vs nilai pasar harus diberi label tepat dan memakai data domain aktual; jangan mengasumsikan angka referensi berarti nilai pasar.
- Bandingkan dua cara menempatkan detail bila diperlukan, tetap mobile portrait, teks besar, kontras, tombol/fokus, scroll, layar kosong dan error. Jangan memutuskan sepihak perubahan teks/navigasi/alur. Preview dan pilihan Setuju / Setuju dengan perubahan / Belum cocok / Tunda; terapkan hanya untuk layar dan state yang pemilik setujui. Lanjutkan diagnosis B02/B03 saat menunggu.
- Telusuri loadAsetView, syncPortfolioLive dan semua caller sebelum mengubah; uji pembaruan real-time, identitas posisi, jumlah, rupiah/USD bila didukung, pembulatan, akses detail dan state kosong/error. Lakukan pemeriksaan visual perangkat setelah izin berlaku; browser atau preview bukan bukti HP.

## B02 — Posisi tombol beli/jual tidak sesuai

Bukti pemilik: `C:/Users/HYPE AMD/Downloads/WhatsApp Image 2026-10-30 at 02.06.35 (2).jpeg`. Pemilik mengklarifikasi “tombol belinya tidak sesuai tempatnya”. Foto menunjukkan bar JUAL/BELI di atas bagian grafik dan konten detail; jangan mengasumsikan transaksi gagal atau jumlah saham salah. Temuan gejala tata letak; penyebab dan perilaku ketika scroll belum terbukti.

- Reproduksi detail saham dengan data QA dan ukuran/versi HP yang sama bila tersedia dan diizinkan: awal halaman, scroll tengah/bawah, status bar, gesture/3-button navigation, keyboard, modal/tiket dan teks besar. Catat apakah tombol hanya tampak menutupi konten atau konten benar-benar tidak bisa diakses.
- Telusuri container, sticky/fixed, dock, scroll, z-index, safe area dan semua caller/detail flow. Buat preview nyata solusi yang menjaga tombol tidak menutupi grafik/konten dan tetap mudah dijangkau. Ajukan pilihan penempatan dengan contoh jika ada tradeoff; butuh persetujuan eksplisit sebelum perubahan tampilan/navigasi/alur.
- Uji bahwa beli dan jual tetap membuka tiket yang benar dan tidak menghasilkan transaksi duplikat. Periksa order, saldo dan fee dengan QA jika memungkinkan; laporan posisi tombol tidak boleh ditulis sebagai bug ekonomi yang sudah terbukti.

## B03 — Gagal menyimpan / pemulihan saat membuka kembali

Bukti `C:/Users/HYPE AMD/Downloads/WhatsApp Image 2026-10-30 at 02.09.54.jpeg`; pemilik menyatakan muncul saat membuka kembali. Pisahkan setidaknya: penyimpanan gagal, primary/backup invalid, validasi/restore gagal, exception startup, penyelarasan offline gagal/terhenti, UI salah melaporkan status, dan proses recovery yang perlu konfirmasi. Ini hipotesis; ukur sebelum menetapkan akar penyebab.

- Inventaris jalur startup, clock worker, onPause, transaksi, save atomic/backup, load, verifikasi format, offer recovery, retry, dan semua caller. Jejak gejala, versi APK/source, exception dan stage menggunakan log yang tidak membocorkan isi save. Jangan menyebut save korup hanya berdasarkan teks di layar.
- Susun reproduksi QA dari kondisi baru dan progres yang tersimpan; ulangi tutup/buka dan transaksi-lalu-save, gangguan tulis terkontrol, retry, backup valid/rusak, versi skema, penyimpanan terbatas terisolasi, dan restart. Cakup null/exception/incomplete transaction serta kesesuaian saldo/saham sebelum dan setelah reload; hindari mengisi disk host dan mengubah izin sistem.
- Perbaiki akar penyebab pada jalur bersama setelah terbukti. Pertahankan integritas transaksi, atomicity yang memang didukung, validasi backup dan penanganan error. Gagal save harus terdeteksi, progres dipertahankan sedapat mungkin, pemilik mendapat peringatan dan dapat mencoba lagi; sukses autosave tanpa notifikasi berulang. Jangan menyembunyikan error, mereset slot, menandai sukses palsu, atau memaksakan pemulihan cadangan tanpa persetujuan pemain.
- Untuk progres HP pemilik: dokumentasikan opsi aman berbasis bukti, termasuk kapan pengguna perlu melihat konsekuensi dan menyetujui prosedur. Akses atau perubahan data perangkat memerlukan izin tindakan yang relevan. Hanya uji QA yang diizinkan tanpa izin tambahan; tak ada asumsi bahwa kasus pemilik identik dengan QA.

## B05 — Dua tema Tycoon Sinematik dalam Pengaturan

### Keputusan pemilik 30 Oktober

Pemilik memilih dua tema untuk game dan meminta keduanya tersedia di menu Pengaturan, dapat diganti kapan saja, tersimpan dan tetap dipakai setelah game dibuka kembali. Tema awal **Emas Klasik** jika belum ada pilihan sah. Keputusan terbaru ini menggantikan secara terbatas arahan historis yang melarang toggle dua tema; tidak menghapus persetujuan/fitur lain. Pilihan dua tema dan referensi warna sudah disetujui pada tingkat arah; preview ini tidak otomatis menyetujui tiap layar, teks, navigasi atau alur. Jangan menyalin aset, merek atau data contoh dari preview.

| Token yang dipilih | Emas Klasik | Midnight Neon |
|---|---|---|
| Latar | `#151515` | `#0A1023` |
| Panel | `#29251F` | `#151F3A` |
| Latar hero | `#57442C` | `#253B70` |
| Aksen | `#E3BD7F` | `#76DBFF` |
| Teks utama | `#FFF4E1` | `#F2F8FF` |
| Teks sekunder | `#DFD0B5` | `#C4D3E8` |

Referensi pemilik: `C:/Users/HYPE AMD/.codex/visualizations/2026/10/28/01a1811c-8996-7d01-a89c-f58163685f4a/bandingkan-empat-tema.html`. Bandingkan dengan source dan keputusan terbaru; preview memakai data simulasi, bukan bukti implementasi. Pilihan lama Laut Safir/Tembaga adalah konteks sebelumnya, bukan tugas menambah tema ketiga atau keempat dalam permintaan ini. Nilai enam token yang diberikan adalah acuan persis; warna pendukung untuk semantik, batas, disabled dan fokus dipilih berdasarkan keterbacaan, tidak mengganti enam warna yang dipilih tanpa keputusan pemilik.

### B05.1 — Baseline dan cakupan

- Verifikasi branch/HEAD, source/dirty changes, perubahan yang sedang dikerjakan, dan hasil Plan 15 B01–B03 pada snapshot aktual. Pertahankan prioritas diagnosis B03, portofolio B01 dan tombol B02; kerjakan inventaris tema serta preview independen saat menunggu persetujuan. Jangan menandai tiga bug selesai karena fitur tema ditambahkan.
- Audit CSS variables dan warna hardcoded di `android/app/src/main/assets/www/style.css`, HTML, app.js termasuk gaya inline/dinamis, ikon/SVG, canvas/grafik, modal dan toast; identifikasi area Android native yang terlihat seperti status bar. Catat seluruh layar/komponen/state, bukan hanya lima tab yang terlihat pada preview. Jangan mengubah layout, fitur, angka/domain, isi save pemain atau keputusan desain yang tidak terkait.
- Audit jalur pengaturan yang sudah ada: `openPlayerProfileModal`, `applyPlayerPreferences`/`persistPlayerPreferences`, `TradingWebBridge.updatePlayerProfile`, `TradingFacade.updatePlayerProfile`, `PlayerProfileManager` export/restore serta `SaveState`/`SaveManager` dan semua caller. Saat perencanaan jalur profil mencakup textSize/highContrast/reducedMotion, CSS sudah memiliki beberapa token, dan bridge belum menerima pilihan tema; periksa ulang saat implementasi. Gunakan jalur yang sesuai dan existing tanpa menambah penyimpanan kedua hanya demi toggle; pastikan perubahan satu preferensi tidak mereset yang lain.

### B05.2 — Preview dan persetujuan

- Buat preview nyata dua tema pada keseluruhan kelompok layar dan state yang berubah, memakai data QA berlabel simulasi. Tampilkan Pengaturan dan alur penggantian, Beranda, Pasar dan detail/grafik/tiket, Portofolio, Bisnis, Lainnya, dialog, toast, error, kosong/loading, navigasi, status dan teks besar. Cocokkan portofolio dan dock dengan hasil B01/B02 yang telah disetujui; jangan menerapkan desain lama secara diam-diam.
- Berikan pilihan Setuju / Setuju dengan perubahan / Belum cocok / Tunda. Catat persetujuan eksplisit per layar/perubahan tampilan, teks, navigasi dan alur sebelum menerapkan pada produk; boleh mengelompokkan preview namun keputusan tiap layar tetap spesifik. Pilihan palet bukan persetujuan otomatis seluruh tampilan. Kerjakan diagnosis dan tes independen selama menunggu.

### B05.3 — Perilaku dan penyimpanan

- Tampilkan pilihan `Emas Klasik` dan `Midnight Neon` di Pengaturan sesuai preview yang disetujui. Pergantian harus menerapkan warna segera ke layar aktif dan seluruh layar yang dibuka sesudahnya, tanpa reload progres atau aksi perdagangan. Status aktif jelas, dapat dioperasikan dengan sentuhan/fokus, dan tidak bergantung hanya pada warna.
- Simpan pilihan melalui pola preferensi profil/save yang sudah digunakan bila audit menunjukkan cocok; validasi nilai di batas bridge/domain. Jaga identitas, preferensi ukuran teks, kontras tinggi, reduced motion, saldo, saham dan progres. Propagasi kegagalan simpan secara jujur sesuai aturan B03: jangan mengklaim pilihan tersimpan setelah write gagal; upaya ulang sesuai alur yang disetujui. Tentukan kapan perubahan visual diterapkan versus konfirmasi simpan tanpa mengorbankan progres.
- Save lama tanpa field tema atau nilai tidak dikenal: pilih Emas Klasik secara aman tanpa menghapus/menimpa file lama diam-diam. Apabila audit menemukan pilihan tema sah yang sudah tersimpan, pertahankan atau rencanakan pemetaan kompatibilitas berbasis bukti; jangan memutuskan migrasi destruktif sepihak. Periksa ekspor/restore profil, lifecycle, mode/game baru dan pilihan pengaturan yang tidak terkait. Kebijakan save replay lama tetap menunggu pemilik.
- Tema bertahan saat game ditutup/dibuka ulang setelah simpan berhasil. Pengaturan lokal bukan otomatis sinkronisasi akun; integrasi akun tetap pada rencana terpisah. Jangan memakai file save pemain untuk percobaan; gunakan save QA terisolasi.

### B05.4 — Aksesibilitas dan uji

- Uji dua tema pada semua kelompok layar/states yang diinventarisasi; termasuk dialog/modal/sheet, tombol normal/tekan/disabled, fokus, validasi dan error, toast, grafik/candle/garis/legenda, untung/rugi/impas, navigasi bawah, safe area dan sistem bar. Untung/rugi tetap memiliki tanda/label, bukan hanya warna; jangan membuat angka atau tren contoh dalam produk.
- Ukur kontras kombinasi aktual foreground/background termasuk atas hero/gradien di titik terburuk: target minimal WCAG AA 4,5:1 untuk teks normal, 3:1 untuk teks besar dan komponen visual yang diperlukan; catat kombinasi, metode dan pengecualian yang benar-benar berlaku. Jika enam warna pilihan tidak memenuhi kebutuhan tertentu, ajukan solusi pendukung melalui preview dan minta keputusan, jangan mengubah palet sepihak.
- Periksa portrait lebar sempit, sentuhan, fokus/Back, teks hingga 200% bila tersedia, scroll, pembacaan angka, mode kontras tinggi dan gerakan dikurangi pada kedua tema. Pastikan pilihan tema dan aksesibilitas tidak saling menimpa; verifikasi kontras pada komposisi tema + high-contrast, bukan dua fitur terpisah saja.
- Runnable check bermakna: pilih Emas → Midnight, simpan → rekonstruksi/load → Midnight, ganti kembali → Emas, save lama tanpa tema → Emas; invalid input tidak merusak preferensi dan perubahan profil/aksesibilitas lain tetap ada. Uji kegagalan simpan dan tampilannya sesuai approval, tanpa menyentuh save pemain. Verifikasi layar awal setelah restart, termasuk bila profil belum selesai dimuat: jangan memamerkan tema tersimpan palsu atau menimpa save saat startup.
- Rekam bukti terpisah untuk host, browser/preview, build dan Android fisik, dengan environment, snapshot, hash kandidat, perintah/hasil dan persetujuan. Build/preview saja bukan bukti pengujian perangkat. ADB, instalasi atau akses data perangkat hanya sesuai izin berlaku; no otomatis publish atau perubahan global.

### B05.5 — Penyerahan dan kriteria

Serahkan matriks B05.1–B05.4 per layar/komponen/state dan per tema, hasil kontras, kompatibilitas save lama, failure cases, screenshot preview, approval, tes dan keterbatasan. Status harus membedakan MENUNGGU_PERSETUJUAN/BELUM_DIUJI/GAGAL/LULUS. Selesai hanya bila pilihan, persistensi dan seluruh area yang diwajibkan terbukti pada lingkungan yang relevan; jangan mengklaim selesai dari demo atau APK saja.

## B04 — Bukti, pengujian dan gate

1. Buat matriks B01/B02/B03/B05 dengan ID, gejala, snapshot source/APK/HP, status awal, bukti, diagnosis dan langkah berikut. Gunakan LULUS/GAGAL/BELUM_DIUJI/MENUNGGU_PERSETUJUAN/TERHALANG; jangan menyamakan laporan, build, preview atau kode dengan uji perangkat.
2. Pertahankan foto sebagai bukti referensi tanpa menyalin ke APK. Ambil baseline lewat browser/host dengan data QA, lalu source trace dan root cause. Setiap perubahan nontrivial memerlukan setidaknya satu runnable check yang bermakna menggunakan runner existing; jalankan regresi save/order dan lint/build yang relevan, catat perintah, log dan exit.
3. Bukti perangkat: jika izin relevan diberikan, uji kandidat pada perangkat serta save QA yang dapat dipastikan terisolasi tanpa mengganti progres pemain. Jika isolasi tidak memungkinkan, jangan instal kandidat di atas data; laporkan batas dan opsi. Uji viewport/safe area/scroll, portofolio, posisi tombol, beli dan jual, save dan restart; catat Android, versi, tanda tangan, hash dan keadaan. Jangan menghapus/mengganti progres untuk kemudahan tes.
4. Jalankan siklus realistis berulang dan injeksi kesalahan yang terkendali untuk save/transaksi. Selain jalur normal, buktikan tidak terjadi transaksi ganda, hilang, saldo negatif ilegal atau pemulihan diam-diam. Uji perangkat dan restart bukan bukti semua kemungkinan kehilangan data sudah dihilangkan.
5. Siap uji pribadi dan siap rilis publik adalah keputusan berbeda. Jangan menjanjikan tidak ada bug atau kegagalan save selamanya. Untuk rekomendasi rilis, ketiga gejala dan B05 perlu terselesaikan dengan bukti relevan, preview disetujui, risiko dan bug terbuka dilaporkan, serta seluruh gate rilis lain diperiksa secara terpisah. Bila belum, nyatakan BELUM TERBUKTI.

## Penyerahan

Serahkan ringkasan tiap ID: masalah terkonfirmasi vs dugaan, akar penyebab (jika ditemukan), perubahan file, keputusan/preview/approval, pengujian dan hasil per lingkungan, bukti dan hash save yang diizinkan, risiko, hambatan dan next_action. Tidak membuat update akun, perubahan sistem distribusi atau publikasi dari plan ini. Berhenti setelah penyerahan, tunggu arahan pemilik.
