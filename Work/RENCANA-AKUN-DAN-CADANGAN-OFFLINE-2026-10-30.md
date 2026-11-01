# Rencana akun dan cadangan progres untuk game offline

Tanggal: 30 Oktober 2026. Status: DRAFT untuk ditinjau pemilik. Tujuan: satu pemain dan NPC bermain offline pada aplikasi Android dan menggunakan akun untuk memulihkan progres lintas perangkat. Pemilik membatalkan rencana versi web; tidak ada tugas versi web dalam rencana ini. Pemain boleh bermain sebagai tamu sebelum menghubungkan akun; login Google dan email keduanya dipilih pemilik. Pembuatan rencana bukan perintah menjalankan implementasi, membuat akun layanan, biaya, atau publikasi. Ikuti instruksi proyek satu plan pada satu waktu; pemilik menunjuk plan sebelum eksekusi.

## Keputusan yang sudah disampaikan dan batas

- Permainan satu pemain dengan NPC; game tetap dapat dimainkan offline. Pemilik menginginkan progres tersimpan di akun dan dapat disinkronkan saat internet tersedia. Pertahankan fitur, ekonomi, waktu dan penyimpanan yang tidak bertentangan. Jangan menyatakan server menjalankan dunia sebagai fakta atau menambahkan multiplayer.
- Game hanya memiliki cadangan akun yang aman setelah upload selesai dan dikonfirmasi; perubahan lokal yang belum terunggah bisa hilang bila aplikasi atau perangkat hilang. Proses tidak diwajibkan hidup ketika perangkat tertutup sesuai keputusan terbaru. Jangan mengurangi, memotong, atau mengubah hasil catch-up dan aturan 24× tanpa keputusan pemilik.
- Akun dan cadangan adalah tambahan; engine Java tetap pemilik logika ekonomi lokal, UI hanya presentasi/input. Tidak menukar engine atau mengonversi dunia menjadi server secara diam-diam. Backend boleh menyimpan cadangan dan mengautentikasi, tidak memproses transaksi pasar pada tahap ini.
- Arah tahap awal: satu slot cloud per akun dan satu perangkat aktif; pemilik memilih perbandingan dan pilihan pemain jika progres lokal dan cloud berbeda. Mekanisme perpindahan perangkat serta perilaku saat perangkat lama offline belum diputuskan. Jangan merge transaksi/saldo secara otomatis atau memakai waktu HP sebagai penentu kemenangan.
- Lindungi semua perubahan lokal dan pekerjaan agen lain. Root C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM; verifikasi branch, HEAD, dirty tree, source hash, versi kandidat, bukti terbaru dan aktivitas agen. Jaga save pemain Artifacts/LocalPreview/preview-save/save_game_v2.json hanya di-hash; gunakan data QA sintetis dan direktori terisolasi. Jangan baca, timpa, migrasi, hapus save pemain atau replay lama tanpa kebijakan dan izin berlaku. Tidak ADB, instalasi perangkat, unggah data pemain, perubahan akun/cloud, pembelian, deploy, publikasi, push atau pengaturan global hanya karena rencana ini dibuat.
- Preview nyata dan persetujuan eksplisit untuk setiap perubahan tampilan, teks, navigasi dan alur, termasuk login, konflik, pemulihan dan status; keputusan produk yang belum dipilih tidak ditetapkan sepihak. Jangan mengirim pesan atau kode akun kepada pihak lain.

## Keputusan terbaru pemilik dan urutan pekerjaan — 30 Oktober

Pemilik membatalkan rencana game yang dimainkan melalui web. Jangan mengerjakan port, prototipe, hosting, pengujian Safari atau publikasi web. Keputusan ini menggantikan seluruh arahan versi web sebelumnya; layanan akun dan penyimpanan untuk aplikasi Android tetap dalam rencana, bukan berarti menyediakan permainan lewat browser.

Arah yang berlaku: (1) satu pemain dengan NPC; (2) aplikasi Android dapat dimainkan offline; (3) sinkronisasi akun saat internet tersedia untuk cadangan dan pemulihan lintas perangkat; (4) tidak ada server yang wajib terus menjalankan simulasi; (5) pemilik menginginkan tanpa biaya layanan. Ini keputusan produk, bukan bukti implementasi. Jangan menganggap multiplayer atau pasar bersama telah disetujui. Pemilik memilih login Google dan email serta mengizinkan bermain sebagai tamu sebelum menghubungkan akun.

Keputusan ini menggantikan syarat lama bahwa proses simulasi harus benar-benar hidup ketika aplikasi ditutup. Durasi 24× dan jam bursa 09.00–16.00 tetap berlaku kecuali diubah pemilik. Pemilik memilih: satu jam nyata saat aplikasi ditutup dianggap sekitar satu hari game dan perkembangannya dihitung secara lokal ketika dibuka kembali. Detail teknis harus mempertahankan kebenaran ekonomi dan jangan diam-diam memangkas waktu/kejadian.

Urutan satu plan pada satu waktu: pertama pemilik dapat menunjuk `Work/Master-Plan-Gabungan-2026-10-24/Plans/15-perbaikan-portofolio-beli-dan-save/plan.md` untuk diagnosis dan perbaikan save lokal serta dua bug UI. Setelah penyerahan dan tinjauan, pemilik dapat menunjuk rencana akun ini. Jangan memulai akun atau cloud saat Plan 15 ditunjuk. Tiap tahap menunggu penunjukan dan persetujuan yang berlaku.

Tidak ada jaminan bebas gagal save atau akses online 100%. Ukur keandalan dengan uji yang bisa diulang, jangan menghilangkan peringatan kegagalan. Backend akun berarti layanan autentikasi dan penyimpanan cadangan yang tersedia saat diakses, bukan mesin dunia yang wajib hidup 24 jam. Kehilangan jaringan atau layanan tidak boleh menghalangi akses ke progres lokal Android yang valid. Risiko save belum tersinkron harus dijelaskan kepada pemain.

### Batas biaya nol

Bandingkan minimal dua opsi aktual dengan sumber resmi bertanggal: biaya, kuota, kapasitas ukuran progres, keamanan, export/backup, autentikasi dan persistensi. Periksa apakah provider memerlukan kartu, apakah batas gratis menghentikan layanan atau dapat menimbulkan tagihan, dan perilaku saat kuota habis. Sasaran Rp0 adalah kendala; tanpa persetujuan pemilik jangan menautkan metode pembayaran, membuat layanan, mengaktifkan auto-upgrade atau menerima biaya. Jika layanan dapat mengenakan biaya meski gratis, jelaskan dan jangan pilih secara otomatis. Jika tidak ada opsi yang memenuhi sasaran dan kebutuhan, laporkan tradeoff tanpa janji.

## Jawaban pemilik dan contoh konflik — 30 Oktober

Pemilik memilih kedua metode login (Google dan email), dan pemain boleh bermain sebagai tamu sebelum menghubungkan akun. Pemilik mengatakan game berjalan langsung di lokal: logika dunia di perangkat, tidak ada server simulasi selalu hidup. Jawaban Q13 terbaru menetapkan waktu game dianggap berlalu saat ditutup dan perkembangannya dihitung saat dibuka kembali. Jangan mengklaim OS menjamin proses hidup ketika ditutup.

Pemilik meminta contoh UI saat progres lokal dan akun berbeda. Preview konsep QA: `Work/preview-konflik-progres-prototype.html`, data simulasi. Preview bukan persetujuan implementasi, bukan operasi pada save, dan bukan keputusan final pemilihan/retensi cadangan. Pertahankan opsi Batal dan konsekuensi progres yang belum tersinkron; jangan menimpa otomatis. Minta persetujuan spesifik terhadap layar/alur dan kebijakan cadangan sebelum implementasi.

## Keputusan Q&A 15 butir — 30 Oktober, terbaru

Q01. Pemilik memilih preview konflik A: dua kartu terpisah. Angka/waktu pada prototype hanyalah simulasi. Pilihan ini menyetujui konsep konflik A secara spesifik, bukan seluruh detail UI, alur konfirmasi atau layar lain; presentasikan detail pelaksanaan dan catat approval jika berubah.
Q02. Pada awal game tampilkan pilihan masuk akun atau main sebagai tamu. Preview dan persetujuan layar yang sebenarnya diperlukan.
Q03. Progres tamu dihubungkan ke akun baru setelah konfirmasi. Jika akun sudah berisi progres, jalankan pemeriksaan konflik dan pilihan Q10; jangan overwrite.
Q04. Google dan email sama dapat menuju akun game yang sama hanya setelah kedua metode terbukti dimiliki pemain dan pengaitan aman; kesamaan teks alamat email saja tidak cukup.
Q05. Sinkronisasi cadangan otomatis saja ketika koneksi tersedia; tidak menambahkan tombol sinkron manual umum. Q08 secara khusus tetap mengizinkan tombol coba lagi ketika gagal.
Q06. Pemilik memilih tidak menampilkan keterangan offline bahwa progres baru tersimpan di HP dan menunggu dikirim ke akun. Jangan menambah indikator offline terus-menerus; ini tidak menghapus kebutuhan peringatan kegagalan lokal pada Q09 atau kegagalan sinkronisasi pada Q08. Waktu dan kondisi peringatan disajikan dalam preview untuk disetujui.
Q07. Tidak perlu notifikasi sukses berulang; pemilik ingin sinkronisasi terus dijaga. Ini tujuan best-effort: offline, koneksi terganggu, proses mati atau server gagal bisa menunda; jangan mengklaim selalu tersinkron atau sukses tanpa konfirmasi server. Simpan lokal lebih dahulu, antre revisi belum diakui, coba lagi dan tampilkan kegagalan sesuai Q08/Q09.
Q08. Jika sinkronisasi gagal, coba otomatis dan sediakan tombol coba lagi. Perubahan tampilan perlu preview dan persetujuan.
Q09. Jika save HP gagal, tampilkan peringatan dan tombol coba lagi, lindungi progres sedapat mungkin. Tidak menyatakan bahwa error tidak bisa terjadi.
Q10. Saat progres HP dan akun berbeda, tampilkan perbandingan dan biarkan pemain memilih; tidak ada overwrite otomatis atau merge saldo/transaksi.
Q11. Sebelum mengganti progres HP dengan cadangan akun, pemilik memilih minta konfirmasi saja, bukan menetapkan kewajiban backup lokal baru. Jangan menyebutnya izin menghapus/mengganti save saat ini atau mengabaikan perlindungan save: validasi salinan sebelum penggantian, jaga backup yang sudah ada dan jangan rusakkan data saat gagal. Kebijakan backup tambahan opsional bukan keputusan yang sudah disetujui; jelaskan risiko bila relevan.
Q12. Pemilik memilih game hanya bisa dibuka pada satu perangkat. Terapkan sebagai satu perangkat aktif per akun; perpindahan perangkat perlu rancangan dan persetujuan. Perangkat aktif yang sebelumnya sudah terdaftar dapat dimainkan offline sesuai kebutuhan. Batas teknis: server tidak dapat langsung mencabut izin pada HP lama yang benar-benar offline; jangan mengklaim eksklusivitas mutlak saat dua HP terputus. Rancang protokol aktivasi/perpindahan, deteksi konflik dan pengamanan progres; demonstrasikan tradeoff kepada pemilik dan jangan mengorbankan mode offline secara sepihak.
Q13. Bila aplikasi ditutup satu jam nyata, waktu game dianggap berlalu sekitar satu hari dan perkembangannya dihitung ketika game dibuka kembali. Ini catch-up lokal, bukan jaminan proses berjalan ketika aplikasi ditutup. Tetap 24× dan bursa 09.00–16.00 sesuai aturan. Jangan memotong atau mengulang kejadian, atau membuat UI membeku; ukur performa dan simpan dengan benar.
Q14. Saat keluar dari akun, progres tetap tersimpan di HP. Jangan unggah perubahan akun lama ke akun berbeda; aturan akses identitas/konflik saat login lain tetap perlu dirancang.
Q15. Jika layanan gratis penuh atau tidak tersedia, game tetap dapat dimainkan offline, sinkronisasi menunggu dan pemilik diberi tahu. Ini tidak menjamin pemberitahuan sampai jika semua jalur jaringan gagal; rancang status dan retry yang jujur. Tidak ada biaya otomatis.

## Baseline yang perlu diverifikasi saat mulai

Source saat perencanaan memiliki SaveManager.forDirectory untuk save_game_v2.json, rotasi backup .bak, dan domain Java di TradingFacade; manifest dan Gradle yang diperiksa belum menunjukkan layanan akun sebagai dependensi. Ini pembacaan source terbatas, bukan bukti semua save/restore berfungsi di HP. `android:allowBackup=true` tidak membuktikan adanya akun game atau pemulihan yang pasti. Periksa ukuran save realistis serta versi skema pada QA, kumpulkan hasil startup fix terbaru dari pemilik/artefak dan pisahkan bukti perangkat dari host. Pernyataan pemilik bahwa game sudah bisa dimainkan dihormati; jangan menimpa dengan laporan lama, tetapi verifikasi kandidat dan batasnya saat eksekusi.

## Keputusan pemilik sebelum memilih rancangan atau layanan

1. Dipilih pemilik: login Google dan email; pemain boleh bermain sebagai tamu sebelum menghubungkan akun. Detail keamanan, keterkaitan metode login pada akun yang sama, akses dan pemulihan tetap perlu ditentukan; jangan menganggap email Google dan email/password identik secara otomatis.
2. Ketika lokal dan cloud berbeda, tampilkan informasi waktu, perangkat/versi bila tersedia, dan risiko. Pemilik memilih perbandingan dua kartu, pemain memilih dan konfirmasi sebelum mengganti; backup lokal tambahan bukan pilihan yang ditetapkan. Pertahankan backup yang sudah ada dan proteksi terhadap data loss; tidak merge saldo/order otomatis.
3. Perangkat kedua: pemilik memilih satu perangkat aktif. Rancangan perpindahan, akses offline pada perangkat lama dan penanganan konflik masih perlu contoh dan keputusan; jangan menjanjikan pencabutan jarak jauh saat perangkat lama offline.
4. Batas versi cadangan, pemulihan progres lama, pergantian akun, penghapusan akun dan masa simpan: butuh pilihan pemilik serta mekanisme yang dapat diuji. Jangan menyatakan satu cadangan menjamin pemulihan semua keadaan.
5. Penyedia layanan dan anggaran: bandingkan opsi yang sesuai setelah pengguna menentukan akun/anggaran. Verifikasi harga, kuota, batas ukuran dokumen/objek, keamanan dan portabilitas dari dokumentasi resmi pada saat memilih; jangan menyimpan save penuh pada produk dengan batas ukuran yang tidak cukup. Jangan membuat layanan atau membeli tanpa izin yang berlaku.

## A0 — Persiapan dan inventaris

Baca AGENTS.md, DESIGN.md, master plan aktif, keputusan terakhir, laporan dan source terbaru. Petakan alur start/load/save/recovery dan setiap caller, event otomatis/penutupan, error handling, versi save, lifecycle, mekanisme backup dan restore. Ukur ukuran file, durasi serialize/hash dan memori pada profil QA kecil/menengah/besar tanpa menyentuh save pemain. Identifikasi isi sensitif untuk meminimalkan log dan informasi yang dikirim. Tetapkan matriks baseline dan ownership file; jangan mengganggu pekerjaan agen lain.

## A1 — Rancangan akun dan pengalaman offline

Tampilkan preview untuk layar/status: tamu, hubungkan akun, sedang menyinkronkan, tersimpan di akun (dengan waktu server), menunggu koneksi, gagal dan coba lagi, login ulang, pilihan konflik, cadangan tersedia/tidak tersedia, restore dan konfirmasi dampak. Penanda sukses harus berdasarkan pengakuan server, bukan hanya antrean lokal. Tindakan login, unlink, ganti akun, sign out, penghapusan akun dan restore memerlukan skenario dan konsekuensi jelas. Hormati pilihan aksesibilitas dan persetujuan spesifik per layar/alur. Status game offline tetap dapat dipakai, tanpa pesan sukses autosave berulang.

## A2 — Kontrak cadangan aman

- Snapshot hanya dari state lokal yang sudah tersimpan dan divalidasi, diambil melalui jalur yang konsisten dengan SaveManager dan domain, tanpa membekukan UI lebih lama dari pengukuran. Sertakan versi skema, ukuran, checksum, identitas akun, ID perangkat acak, revisi lokal dan revisi cloud, metadata waktu dari server serta ID operasi. Jangan menyimpan kata sandi/token di save atau log.
- Backend memverifikasi identitas dan akses hanya pemilik; enkripsi transport, aturan akses server dan batas penyimpanan yang sesuai. Jangan menaruh kunci administratif dalam APK. Batasi ukuran, validasi format/versi dan lindungi terhadap file tidak lengkap/berbahaya.
- Upload melalui antrean lokal persisten, retry dengan backoff dan idempotency; checksum harus dihitung dari snapshot yang sama yang diunggah. Buat snapshot baru bila data berubah saat upload; never mark perubahan lebih baru sudah tersinkron hanya karena snapshot lama sukses.
- Gunakan pembaruan bersyarat berdasarkan revisi cloud yang dibaca sebelumnya (compare-and-swap/ETag sesuai penyedia), agar dua perangkat tidak saling menimpa diam-diam. Jika ada konflik, hentikan penggantian dan tawarkan preview/pilihan yang disetujui; jangan memakai last-write-wins otomatis untuk seluruh save keuangan.
- Tangani proses berhenti, permintaan hilang, jawaban sukses hilang, retry, logout dan pergantian akun tanpa mengirim cadangan ke akun yang salah. Lakukan semua sinkronisasi jaringan di luar thread UI dan jangan membuat bermain offline bergantung pada login/jaringan.
- Cadangan cloud tidak menggantikan backup lokal yang ada. Restore staging terisolasi, verifikasi integritas/skema dan kompatibilitas sebelum pilihan, pertahankan backup yang sudah ada dan gunakan pemulihan aman yang sudah ada; pembuatan cadangan lokal tambahan bukan syarat yang dipilih pemilik dan tidak boleh diklaim sudah disetujui. Jika tahap gagal, progres lama tetap tersedia dan jangan menimpa diam-diam. Kebijakan save replay lama tetap terpisah.
- Resolusi versi skema lama/baru: jangan otomatis migrasi jika kebijakan atau dukungan belum terbukti; tolak dengan pesan jelas dan pertahankan salinan. Jangan mengunggah dataset atau aset yang tidak perlu.

## A3 — Pemilihan penyedia dan batas operasional

Buat perbandingan minimum dua opsi yang benar-benar sesuai dengan pilihan pemilik: autentikasi, penyimpanan snapshot realistis, operasi revisi bersyarat, aturan akses, backup, lokasi data, kuota/batas dan perkiraan biaya berdasarkan asumsi penggunaan eksplisit. Sumber resmi dan tanggal harus dicantumkan. Ukuran file bukan satu-satunya pertimbangan; data transaksi keuangan game harus terlindung dan pemulihan dapat diandalkan. Jangan berjanji gratis atau memilih penyedia semata karena familiar. Pisahkan lingkungan QA dari produksi dan minimalkan data pribadi.

## A4 — Implementasi setelah plan ditunjuk dan keputusan tersedia

Gunakan mekanisme lokal existing dan dependency minimum; buat fitur akun/cadangan hanya untuk ruang lingkup disetujui. Uji dengan server atau layanan uji yang terisolasi dan konfigurasi aman; tidak mengakses layanan produksi tanpa izin. Buat batas kepercayaan untuk respons jaringan dan file yang dipulihkan. Simpan akses akun secara aman sesuai platform; tidak membuat mekanisme kriptografi atau password sendiri. Pisahkan perubahan UI yang menunggu persetujuan. Tambahkan tes yang gagal jika invarians kritis rusak, tanpa menambah framework spekulatif.

## A5 — Uji dan bukti wajib

1. Offline: mulai sebagai tamu dan bermain, simpan, tutup/buka, lalu sinkronisasi setelah online; putus internet sebelum/selama/sesudah upload dan verifikasi retry.
2. Dua perangkat QA: update bersamaan dari revisi sama; hanya satu commit diterima, konflik terlihat dan tidak ada saldo/transaksi yang hilang atau berganda. Jangan menggabungkan file secara naif.
3. Snapshot race: lakukan mutasi saat upload berlangsung; status cloud hanya mencakup versi snapshot yang benar, perubahan baru tetap menunggu upload.
4. Restore: cloud valid/rusak/kosong, save lokal valid/rusak/kosong, format dan versi tidak kompatibel; batalkan atau gagal pada tiap tahap, pastikan backup lokal tetap valid dan tidak ada overwrite diam-diam.
5. Akun A/B: beralih akun, logout saat upload pending, sesi expired, login ulang, izin ditolak dan akses silang. Pastikan data tidak bocor/terunggah ke akun lain, termasuk melalui log.
6. Gangguan: app ditutup/proses dihentikan pada setiap batas operasi terkontrol, restart dan jaringan tidak stabil; ulangi permintaan idempoten dan validasi autentikasi. Tidak memodifikasi OS atau perangkat secara merusak.
7. Kinerja: ukur ukuran save QA realistis, serialisasi, hash, penggunaan memori dan waktu upload/restore pada lingkungan yang dicatat; jangan menjanjikan angka tanpa benchmark. Uji error kuota dan ruang penyimpanan.
8. Perangkat: verifikasi login, airplane mode, sinkronisasi, pergantian dan pemulihan pada HP QA hanya setelah izin tindakan dan pelindungan data berlaku. Host, browser, build dan HP dilaporkan terpisah. Uji awal masalah startup yang sudah diperbaiki sebagai regresi, tanpa menyatakan status dari laporan lama.

## A6 — Penyerahan

Serahkan matriks keputusan dan setiap syarat beserta bukti, status LULUS/GAGAL/BELUM_DIUJI/MENUNGGU_KEPUTUSAN, cakupan host/HP dan risiko. Sertakan demonstrasi QA, biaya berbasis asumsi, pemulihan, perlindungan akun dan langkah selanjutnya. Jika fitur belum diuji di perangkat atau layanan produksi belum disetujui, nyatakan itu. Jangan mengklaim sinkronisasi aktif hanya karena desain, akun, atau build tersedia. Hentikan setelah penyerahan dan tunggu penunjukan berikutnya.
