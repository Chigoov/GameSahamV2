# Rencana uji kritis dan stress Game Saham

Status: DRAFT untuk ditinjau pemilik, 29 Oktober 2026. Pembuatan rencana bukan izin menjalankan pengujian atau mengubah produk. Plan 14 sedang dikerjakan; rencana ini tidak menggantikan, mengubah, atau menjalankan Plan 14. Pelaksanaan hanya setelah Plan 14 diserahkan dan pemilik menunjuk rencana ini sesuai AGENTS.md. Rekonsiliasi keputusan dan bukti terbaru sebelum mulai.

## Tujuan dan batas

Temukan, reproduksi, dan prioritaskan bug pada ekonomi, progres, simulasi dan pengalaman Android melalui uji berulang, beban, gangguan, dan durasi panjang. Tidak ada uji yang dapat menjamin seluruh bug ditemukan. Gunakan hasil aktual, bukan asumsi atau status historis. Jangan ubah aturan produk yang belum diputuskan; tandai MENUNGGU_KEPUTUSAN.

Lindungi semua perubahan lokal. Catat root, branch, HEAD, status, waktu, hash snapshot dan kandidat, lingkungan, serta hasil Plan 14 aktual. Jangan mengubah file yang sedang dikerjakan agen lain. Save pemain Artifacts/LocalPreview/preview-save/save_game_v2.json hanya di-hash sebelum/sesudah; gunakan save QA baru terisolasi untuk setiap percobaan dan backup QA. Jangan membaca isi, migrasi, menimpa atau menghapus save pemain atau replay lama. Jangan reset, clean, stash, instal, ADB, emulator, perangkat, unggah, publish, ubah pengaturan global atau mengirim pesan tanpa izin yang berlaku. UI, teks, navigasi, dan alur memerlukan preview dan persetujuan spesifik sebelum penerapan.

## Tahapan setelah ditunjuk

### S0 — Inventaris dan baseline
- Petakan hasil Plan 14 dan tes yang sudah ada, cakupan, runner, waktu eksekusi, dependensi, serta celah yang benar-benar belum diuji. Gunakan sarana yang ada sebelum menambah alat. Verifikasi snapshot dan pastikan save QA terisolasi; hentikan jika isolasi tidak terbukti.
- Tetapkan kondisi awal, seed, durasi, sumber daya dan batas penghentian untuk tiap percobaan. Hentikan jika berisiko pada data pemain atau terjadi kegagalan berulang tanpa diagnosis baru. Catat hasil baseline, termasuk tes gagal dan tidak dapat dijalankan.

### S1 — Ekonomi dan transaksi
- Ulangi rangkaian beli/jual, antre dan batal saat tutup, batas saldo/saham, biaya, order penuh atau tidak terisi, IPO, aksi korporasi, pinjaman, bisnis, pembayaran serta perpanjangan riset dan pemulihan. Uji batas, urutan dan percobaan ganda.
- Periksa invarians berdasarkan aturan yang sudah diputuskan: tidak ada saldo atau saham ilegal, debit/refund tidak ganda, dan ledger, portofolio, order serta save konsisten. Reproduksi dengan seed dan urutan tindakan. Jalankan standar 10 seed × 100 aksi pada alur kritis yang berlaku; tambahkan durasi/seed bila hasil mengindikasikan risiko, tanpa menyatakan jumlah itu menjamin bebas bug.

### S2 — Progres dan kegagalan
- Pada QA terisolasi, uji save/reload berulang, backup, pemulihan dengan konfirmasi sesuai desain yang disetujui, data QA terpotong/rusak, kegagalan tulis dan percobaan ulang, serta restart di titik kritis transaksi dan perpanjangan. Periksa apakah progres terakhir aman dan apakah mutasi terulang atau hilang.
- Simulasikan kesalahan hanya lewat sarana uji terkendali; jangan memenuhi disk atau merusak data sistem. Bedakan simulasi host dari gangguan perangkat nyata.

### S3 — Waktu, berita, NPC dan durasi
- Uji batas 09.00/16.00 dan pergantian hari, deadline 60/120 menit nyata, interval langganan, jadwal berita, 1.000 NPC, dan interaksi pasar/bisnis. Uji seed berbeda serta simulasi jangka panjang terukur; catat waktu nyata, waktu game, jumlah kejadian, konsumsi memori dan tren.
- Bandingkan hasil maju bertahap dengan catch-up untuk skenario yang semestinya setara, dengan memperhatikan randomness dan keputusan yang berlaku. Jangan menyamakan catch-up dengan bukti proses tetap hidup ketika aplikasi ditutup. Periksa masalah konflik/pembulatan hanya sesuai keputusan terbaru; jangan mengarang oracle untuk aturan yang belum dipilih.

### S4 — Alur UI dan Android, bertahap
- Uji browser/host terlebih dahulu pada lingkungan dan data QA yang aman: navigasi lima tab, state kosong/error, input batas, scroll dan Back jika relevan. Pengujian browser tidak membuktikan WebView Android.
- Siapkan skenario untuk emulator atau HP hanya jika pemilik memberi izin tindakan terkait: lifecycle, gangguan, restart, orientasi/ukuran teks yang didukung, sentuhan berulang, notifikasi dan pemulihan. Jangan instal atau menggunakan ADB sebelum izin. Uji acak hanya pada instalasi dan save QA yang dipastikan terisolasi; hentikan jika isolasi tidak terjamin.
- Catat model, Android, build/hash, kondisi, durasi, langkah dan hasil. Skenario otomatis dan acak tidak menggantikan pengujian manual pengguna.

### S5 — Performa dan batas praktis
- Ukur baseline dan puncak pada skenario identik: waktu mulai, waktu aksi/catch-up, memori, respons UI dan sumber daya yang tersedia. Catat median, nilai terburuk, jumlah ulangan, perangkat dan kondisi; pisahkan host, browser, emulator dan HP.
- Tentukan target realistis setelah baseline dan kesepakatan pemilik bila berdampak pada produk. Jangan menetapkan angka lulus fiktif. Investigasi tren memori naik terus, macet, crash dan regresi yang dapat direproduksi.

### S6 — Triase, perbaikan dan regresi
- Untuk setiap temuan buat ID, dampak/prioritas, snapshot, lingkungan, seed, save QA, langkah minimum, hasil diharapkan dan aktual, log/bukti serta status. Bedakan bug terkonfirmasi, dugaan, keputusan tertunda dan keterbatasan pengujian. Hindari menyimpan informasi sensitif dalam laporan.
- Dalam kampanye ini, telusuri caller, dokumentasikan akar penyebab serta usulan perbaikan dan pemeriksaan regresi. Perubahan produk menunggu penunjukan terpisah; perubahan UI tetap memerlukan persetujuan preview. Jangan menetapkan aturan produk yang masih menunggu keputusan.
- Setelah dua kegagalan sama tanpa diagnosis baru, laporkan PERLU PERHATIAN dan ubah pendekatan; setelah tiga, kerjakan bagian independen atau laporkan TERHALANG jika membutuhkan tindakan luar.

## Alat yang dipertimbangkan

1. Runner Java/Gradle dan tes proyek yang sudah ada: pilihan pertama untuk ekonomi dan simulasi; inventaris aktual sebelum menambah apa pun.
2. Android Monkey: alat Android untuk stress input acak, dapat diulang dengan seed; memerlukan lingkungan Android/ADB dan izin yang berlaku. Input acak bukan pemeriksaan kebenaran ekonomi dan dapat memicu tindakan destruktif; hanya gunakan instalasi serta data QA terisolasi. https://developer.android.com/studio/test/other-testing-tools
3. AndroidX UI Automator: opsi untuk skenario Android yang terarah jika alat yang sudah ada tidak cukup; cek kecocokan WebView dan biaya penambahan dependensi. https://developer.android.com/training/testing/other-components/ui-automator
4. Android Macrobenchmark: opsi lanjutan untuk mengukur startup dan interaksi pada perangkat; memerlukan modul dan konfigurasi khusus, jadi tambahkan hanya jika pengukuran yang ada tidak memadai dan lingkungan diizinkan. https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview
5. Google Play pre-launch report: opsi setelah tersedia akun dan jalur unggah yang diizinkan; bukan pengganti tes lokal atau izin publikasi. https://developer.android.com/guide/app-bundle/test

Tidak menginstal plugin, repo, dependency, atau alat global hanya karena tercantum. Tinjau lisensi, pemeliharaan, keamanan, kecocokan dan kebutuhan sebelum mengusulkan tambahan.

## Bukti dan penyerahan

Serahkan matriks S0–S6 beserta tiap skenario, status LULUS/GAGAL/BELUM_DIUJI/MENUNGGU_KEPUTUSAN/TERHALANG, bukti dan batas; daftar bug dan prioritas; seed dan perintah reproduksi; hasil regresi; ukuran performa; hash save pemain sebelum/sesudah; file berubah dan next_action. Pisahkan hasil host, browser, APK, emulator dan HP. Jika ada bug kritis terbuka atau gate belum terbukti, jangan menyebut siap rilis. Laporan dan plan bukan bukti bahwa tes dijalankan.

# Revisi intensif: kampanye pengujian ekstrem untuk agen pelaksana

Revisi 29 Oktober 2026 atas permintaan pemilik. Bagian ini memperluas dan menggantikan target pengujian umum S0–S6 jika bertentangan. Ini tugas perencanaan, bukan eksekusi. Fokus eksekusi berikutnya adalah pengujian, diagnosis, dan reproduksi; perbaikan produk dipisahkan sebagai usulan dan tidak otomatis dilaksanakan. Tidak boleh berhenti setelah bug pertama. Tidak ada kewajiban menemukan bug jika bukti tidak menunjukkan bug, dan tidak ada klaim bebas bug mutlak.

## E00 — Kontrak pelaksanaan long task

1. Mulai hanya setelah pemilik menunjuk rencana ini, mengonfirmasi kondisi serah terima Plan 14, dan snapshot stabil tersedia. Pernyataan pemilik bahwa Plan 14 sedang berjalan lebih baru daripada status DRAFT di dokumen lamanya. Jangan mengganggu agen tersebut atau mengirim pesan lintas chat tanpa izin.
2. Keputusan terbaru pemilik 29 Oktober 2026: gunakan agen utama dan sub-agent untuk mempercepat kampanye ini sesuai E10. Izin terbatas pada delegasi internal pengujian setelah rencana ditunjuk; menggantikan larangan satu pelaksana dalam versi lama rencana ini, bukan izin mengganggu Plan 14, mengirim pesan lintas chat, menjalankan launcher/scheduler, atau mengubah pengaturan global. Maksimal satu agen utama dan tiga sub-agent, mengikuti slot nyata yang tersedia. Persiapan dan analisis boleh paralel; beban berat dan pengukuran performa mengikuti antrian eksklusif E10. Durasi panjang diperbolehkan, bukan alasan mengulang tanpa informasi baru.
3. Baca AGENTS.md, keputusan terbaru master plan, Plan 14 beserta bukti penyerahannya, serta semua caller jalur yang diuji. Jangan menebak API, perintah, format save, nama test atau aturan produk.
4. Bangun manifest source dari HEAD, dirty diff dan hash file relevan; HEAD saja tidak mengidentifikasi perubahan lokal. Simpan identitas kandidat build. Periksa drift pada setiap batch. Jika source berubah, pisahkan hasil snapshot dan hentikan perbandingan lintas versi sampai baseline diulang.
5. Gunakan runner yang sudah ada. Penambahan kode uji minimum diperbolehkan ketika pelaksanaan ditunjuk; tidak mengubah produk untuk membuat pengujian lulus. Jika diperlukan seam/instrumentasi produksi, dokumentasikan kebutuhan dan pisahkan persetujuan sesuai lingkup. Jangan menambahkan framework tanpa alasan yang terbukti.
6. Tidak memakai save pemain sebagai fixture. Data sintetis QA diberi label, asal, seed, versi skema dan tujuan. Direktori QA per run harus diverifikasi absolut sebelum operasi file. Dilarang mematikan proses sembarang, mengubah jam OS, memenuhi disk host, merusak izin global, atau menimbulkan kehabisan memori host.
7. Semua angka di bawah adalah anggaran dan target kampanye yang diusulkan, bukan benchmark yang sudah terbukti layak. Kalibrasi throughput dan kapasitas mesin dahulu. Bila angka tak tercapai, catat aktual/target, alasan dan dampak; jangan mengurangi target diam-diam atau mengklaim selesai.
8. Setelah setiap batch simpan checkpoint yang bisa diteruskan: snapshot, skenario selesai/gagal/pending, seed berikut, command tervalidasi, log, bug, sumber daya dan next_action. Beri pembaruan manusiawi saat aktif. Hindari polling cepat atau menjalankan ulang hang tanpa diagnosis.

## E01 — Instrumen kebenaran dan cakupan

Buat matriks setiap requirement dan keputusan aktual: ID sumber, aturan, jalur domain/bridge/UI/save, oracle, test, seed, lingkungan dan bukti. Inventaris seluruh fitur rilis dan layar yang memang diwajibkan, jangan berhenti pada daftar contoh ini.

Oracle harus berasal dari aturan yang disetujui atau model acuan sederhana yang independen dari fungsi produksi. Jangan menghitung expected dengan fungsi yang sedang diuji. Gunakan satuan uang dan aturan pembulatan resmi; jangan menetapkan toleransi floating point sembarangan. Bila keputusan belum tersedia, uji invarians teknis yang tetap sah dan tandai pemeriksaan produk MENUNGGU_KEPUTUSAN.

Invarians minimum yang berlaku diperiksa setelah setiap aksi dan save/reload:
- Kas tersedia, kas tertahan, saham tersedia dan tertahan tidak melanggar aturan; bedakan pinjaman sah dari saldo ilegal.
- Perubahan saldo/saham/biaya dapat direkonsiliasi dengan ledger dan kejadian domain. Keuntungan pasar bukan bukti uang diciptakan secara salah; periksa aliran transaksi sesuai model ekonomi.
- Pesanan penuh atau tidak terisi sesuai aturan; pembatalan/expiry tidak menghasilkan refund ganda; order terminal tidak diproses kembali.
- ID dan kejadian tidak terduplikasi; jumlah objek/history/queue tidak bertambah tanpa sebab; nilai tidak NaN/Infinity atau overflow diam-diam.
- Tagihan, hadiah, allotment, aksi korporasi dan efek berita tepat sesuai frekuensi aturan, termasuk lintas restore.
- Data yang ditampilkan bridge/UI konsisten dengan keadaan domain pada versi state yang sama.
- Pemulihan gagal tidak menimpa salinan QA valid secara diam-diam; pengakuan berhasil simpan harus sesuai keadaan tersimpan.

Periksa kekuatan harness dengan kontrol negatif QA: masukkan ketidaksesuaian sintetis yang diketahui dan pastikan pemeriksa gagal. Ini menguji oracle, bukan membuat bug produk palsu. Laporkan hasilnya terpisah.

## E02 — Tangga beban dan target minimum

| Level | Target | Tujuan |
|---|---|---|
| L0 | 10 seed × 100 aksi | Baseline cepat dan validasi harness |
| L1 | 100 seed × 1.000 aksi | Variasi alur dan urutan |
| L2 | 1.000 seed × 1.000 aksi | Satu juta aksi domain terverifikasi |
| L3 | 100 seed × 10.000 aksi | Satu juta aksi lain dengan rantai state panjang |
| L4 | 20 seed × 365 hari game | Interaksi ekonomi, berita, NPC dan penagihan tahunan |
| L5 | 5 seed × 3.650 hari game | Penuaan state, sejarah panjang dan batas waktu |
| L6 | soak host 8 jam, lanjut 24 jam bila stabil dan sumber daya aman | Kebocoran atau degradasi menurut waktu nyata |

Angka aksi dan hari tidak dicampur menjadi satu denominator. L4/L5 boleh memakai clock QA terkontrol melalui titik yang tersedia; percepatan ini tidak membuktikan Android berjalan saat tertutup. L6 benar-benar waktu nyata, bukan label pada loop cepat. Catat durasi aktual bila terputus. L2/L3 tidak boleh berisi jutaan aksi no-op yang sama: laporkan distribusi aksi, keberhasilan, penolakan, transisi dan state terjangkau.

Gunakan campuran aksi awal 70% valid, 20% batas, 10% invalid melalui batas input yang semestinya; sesuaikan secara transparan untuk cakupan. Jangan menyisipkan state mustahil langsung ke domain lalu melaporkannya sebagai bug perjalanan pemain. Bedakan valid, reachable boundary, corrupt-data robustness dan beyond-spec stress.

## E03 — Keluarga skenario kritis

### A. Trading, uang dan pesanan
- Setiap jenis order yang benar-benar tersedia, tiap pasar/mata uang yang didukung, beli/jual, akun/level terkunci dan terbuka.
- Nilai 0, satu unit terkecil, tepat batas dana setelah biaya, satu unit kurang/lebih, batas kuantitas/representasi dan input malformed pada bridge. Gunakan max legal aktual; beyond-spec diberi label.
- Antre saat tutup, batal sebelum buka, expiry jika didukung, buka bursa, kondisi tidak cukup likuiditas dan perubahan harga sebelum eksekusi.
- Tombol ganda dan input identik berulang; ekspektasi mengikuti semantik API: dua order sah tidak otomatis duplikasi. Uji retry untuk operasi yang menjanjikan idempotensi.
- 10.000 rangkaian order–batal–save–reload pada QA dan 10.000 rangkaian beli–jual dengan rekonsiliasi setiap putaran; angka menjadi target terpisah dari L2/L3 jika belum terpetakan eksplisit.

### B. IPO, aksi korporasi, bisnis dan pinjaman
- Hari batas IPO, dana tertahan, pembatalan bila didukung, alokasi kurang/lebih permintaan, listing, kepemilikan dan refund.
- Aksi korporasi dekat save/reload, perubahan portofolio dan harga; cek konsistensi jumlah saham dan biaya sesuai desain.
- Pendapatan/biaya bisnis, peningkatan, pembelian dan penjualan aset yang didukung; utang, jatuh tempo dan pembayaran jika tersedia. Jangan mengasumsikan dividen/stock split tertentu ada tanpa inventaris.
- Ulangi kejadian yang sama setelah restore untuk mendeteksi pembayaran/pendapatan ganda.

### C. Riset, berita dan waktu
- Basic/Pro/Whale; beli, ganti, berhenti perpanjangan, habis periode, saldo tepat cukup dan kurang, restore serta catch-up melewati beberapa periode.
- Batas 09.00 dan 16.00, pergantian hari, 7/14/30 hari game, deadline 60/120 menit nyata: tepat sebelum, tepat pada dan tepat sesudah sesuai resolusi clock aktual.
- Dua berita Pro dan rangkaian Whale/lanjutan sesuai keputusan terbaru. Verifikasi event pemicu, target/acuan, distribusi jadwal, konflik dan pembulatan hanya jika sudah diputuskan.
- Clock QA maju/mundur, delta nol, delta besar dan replay tick yang sama. Jangan mengubah waktu perangkat/OS. Nilai ekstrem di luar kontrak harus ditolak atau ditangani sesuai kontrak, bukan dipaksa dianggap skenario pemain normal.

### D. NPC, ranking dan progres
- 1.000 NPC sebagai beban produk; peringkat, transaksi, bisnis dan IPO sesuai fitur aktual. Uji kedudukan seri dan data kosong tanpa mengarang tie-breaker.
- Profil QA baru, awal, menengah, kaya, banyak aset/order/history dan semua fitur terbuka secara sah.
- 2× dan 5× jumlah objek sebagai uji di luar spesifikasi hanya bila fixture mendukung dan aman; kegagalan di sana adalah temuan kapasitas, bukan otomatis bug syarat 1.000 NPC.
- Periksa misi, hadiah, level dan karier yang tersedia, khususnya auto-claim dan pemrosesan ulang setelah restore.

## E04 — Fault injection dan pemulihan

Petakan titik transaksi nyata dahulu: sebelum mutasi, setelah mutasi sebelum persist, saat tulis, setelah tulis sebelum rename/commit bila ada, setelah commit sebelum respons, dan ketika retry. Jangan menganggap implementasi memiliki atomic rename sebelum dibaca.

Untuk setiap titik yang dapat diuji, jalankan minimal 100 seed/varian dan catat keadaan sebelum, sesudah gangguan serta setelah restart QA. Gangguan mencakup exception terkendali, short write, file QA terpotong, invalid JSON/skema, field hilang, backup QA rusak, dan kegagalan akses terbatas direktori QA. Hanya gunakan mekanisme yang benar-benar aman dan tersedia; titik yang tidak dapat diuji dicatat BELUM_DIUJI.

Matriks pemulihan: primary valid/invalid/missing × backup valid/invalid/missing, dengan variasi umur/skema yang didukung. Periksa pilihan konfirmasi, informasi waktu dan risiko sesuai desain yang disetujui; jangan membuat kebijakan recovery sendiri. Lakukan 1.000 siklus save–reload pada masing-masing profil kecil, menengah dan besar. Periksa hash logis/canonical state yang mengecualikan field volatil dengan alasan terdokumentasi.

Process kill hanya terhadap proses QA yang PID dan jalur datanya terverifikasi, serta izin lingkungan yang berlaku. Host kill bukan bukti lifecycle Android. Gangguan daya nyata dan disk host penuh tidak dilakukan.

## E05 — Kombinasi adversarial dan eksplorasi state

Gunakan tabel dimensi: fase bursa, tier riset, saldo, holding, order, status save, batas waktu, umur game dan status lifecycle. Turunkan nilai dari kontrak aktual. Bangun pairwise untuk seluruh pasangan yang valid, lalu kombinasi tiga arah untuk waktu–transaksi–persistensi dan waktu–tagihan–berita. Catat pasangan yang tercakup, invalid menurut model, dan belum tercakup; daftar panjang tanpa ukuran cakupan tidak cukup.

Skenario wajib panjang: 20 perjalanan × sedikitnya 500 langkah, mencampur fitur dari awal permainan sampai kondisi lanjut, dengan checkpoint reload pada batas kejadian. Contoh: antre order → berhenti auto-renew → bursa buka → IPO alokasi → save gagal → retry → reload → periode habis → berita lanjutan → jual saham → rekonsiliasi ledger. Sesuaikan ke fitur aktual dan aturan sah.

Bandingkan continuous run dengan save/reload dan catch-up pada seed dan clock terkendali untuk properti yang dijanjikan setara. Jika RNG/event ordering berbeda secara sah, bandingkan invarians yang relevan, bukan menuntut byte-identical tanpa dasar.

## E06 — UI, bridge dan Android

Browser: target 10 seed × 1.000 aksi terarah di seluruh layar/state aktual, dengan distribusi coverage, error console, screenshot kegagalan dan state domain pembanding bila tersedia. Browser dengan bridge mock hanya bukti presentasi; tidak boleh meluluskan integrasi Java.

Bridge: uji input kosong, jenis salah, angka batas, string panjang terbatas, pengulangan, operasi tidak tersedia dan data stale melalui API nyata. Fokus pada validasi dan respons konsisten. Tidak perlu membangun fuzzing framework baru bila parameterisasi runner cukup.

Android bersyarat izin: minimal 20 perjalanan kritis terarah, 100 siklus background/foreground, 100 restart QA yang terkontrol, dan Monkey 10 seed × 10.000 event; naik ke 10 × 100.000 hanya setelah tidak ada blocker dan isolasi QA terbukti. Batasi target package dan event yang dapat meninggalkan lingkungan QA. APK dengan application ID produksi tidak boleh dipasang menimpa game pemain. Jika instalasi QA aman belum tersedia, tandai tahap belum diuji.

Monkey menemukan crash/hang, bukan bukti kebenaran ekonomi. Sertakan pemeriksaan state sebelum/sesudah. Jangan menyebut browser, emulator, atau build sebagai bukti HP. Android soak 2 jam lalu 8 jam hanya jika izin perangkat, batas temperatur/baterai dan pengawasan disepakati; jangan mengubah batas thermal OS.

## E07 — Kapasitas dan performa

Kalibrasi tiga profil state dan sekurangnya 5 pengulangan per pengukuran terarah. Catat startup, latency aksi, catch-up, memori, jumlah objek/history, ukuran save serta durasi save/load. Untuk latency dengan sampel cukup, sertakan median/p95/p99/max dan jumlah sampel; jangan membuat p99 bermakna dari 5 sampel saja.

Bandingkan baseline dan akhir soak pada snapshot/perangkat/kondisi sama. Memori sesaat tinggi tidak otomatis leak; cari tren retained state dan reproduksi. Sebelum batch tentukan batas RAM/disk/durasi berdasarkan kapasitas tersedia. Timeout adalah temuan yang harus didiagnosis, bukan alasan meningkatkan timeout terus.

Klasifikasi: crash, ANR, corrupt state, kehilangan progres, pelanggaran ekonomi, pertumbuhan tak terbatas, regresi terukur, kapasitas di luar spesifikasi, atau hasil inconclusive. Target performa produk yang belum disetujui tetap terbuka; angka beban kampanye bukan SLA.

## E08 — Reproduksi dan laporan bug

Pada kegagalan: simpan seed, trace aksi, clock QA, input, snapshot, state QA, stack/log dan command tervalidasi. Ulangi seed hingga 3 kali untuk klasifikasi deterministik/intermiten; jangan hilangkan kegagalan hanya karena retry lulus. Perkecil urutan secara bertahap menjadi reproduksi minimum, menjaga kondisi pemicu. Batasi proses minimisasi per temuan agar tidak menghentikan seluruh kampanye; simpan checkpoint jika belum minimal.

Setiap bug memiliki ID ST-xxx, skenario, aturan sumber, severity, langkah, expected/actual, frekuensi, bukti, snapshot, lingkungan, dampak data, dan status. P0: kehilangan/kerusakan progres berat atau kegagalan kritis sebanding; P1: transaksi/aturan inti salah atau alur utama terblokir; P2: masalah fungsional terbatas; P3: minor. Beri alasan, jangan menilai hanya dari label.

Deduplikasi berdasarkan akar penyebab yang terbukti; jangan menyatukan semua crash dengan gejala sama. Setelah bug pertama lanjutkan keluarga skenario independen. Jika state telah korup, mulai QA bersih untuk batch berikut agar temuan lanjutan tidak hanya efek domino.

Perbaikan source produk bukan bagian otomatis kampanye ini. Serahkan usulan minimal dan test regresi. Jika pemilik kemudian menunjuk perbaikan, ulangi reproduksi, keluarga terkait dan baseline pada snapshot baru; hasil snapshot lama tetap historis.

## E09 — Keluaran, gate selesai dan handoff

Buat direktori bukti unik per run di bawah Work/Stress-Test-Evidence/. Isi minimum: manifest, matriks cakupan CSV, log batch, seed/action traces yang cukup untuk reproduksi, daftar bug, checkpoint, ringkasan performa dan laporan akhir. Hindari dump state setiap aksi jutaan kali; gunakan checkpoint periodik serta buffer aksi yang di-flush saat gagal, dengan strategi reproduksi lengkap. Batasi ukuran log/disk secara eksplisit. Jangan commit binary besar, save pemain atau secrets.

Status per skenario: LULUS, GAGAL, BELUM_DIUJI, MENUNGGU_KEPUTUSAN, TERHALANG; hasil inconclusive tidak dilabeli lulus. Laporkan jumlah aksi direncanakan/dijalankan, seed, game days, wall time, pasangan tercakup, kegagalan, abort dan retry. Jangan mencampur jumlah test, aksi, hari dan layar menjadi persentase selesai tunggal.

Kampanye lengkap hanya jika semua tahap yang berlaku memiliki bukti dan target tercapai; tahap bersyarat tidak tersedia tetap ditulis gap, bukan N/A untuk menaikkan kelulusan. Boleh menyerahkan laporan parsial jujur dengan sisa konkret. Tidak berhenti karena satu juta aksi tercapai jika fitur wajib belum terjangkau. Tidak memperpanjang tanpa batas hanya untuk memaksa ditemukan bug.

Laporan akhir harus menyatakan secara terpisah: cakupan yang terbukti, bug terbuka, keputusan belum selesai, batas lingkungan, kesiapan uji pribadi, dan bagian yang memerlukan tindakan pemilik. Pengujian selesai tidak sama dengan produk siap rilis. Setelah penyerahan, berhenti sesuai aturan proyek; tidak otomatis mulai perbaikan atau publikasi.

## E10 — Pelaksanaan dengan sub-agent untuk mempercepat pekerjaan

Keputusan pemilik: “okee, perbarui file nya agar pekerjaan dapat selesai lebiih cepat”, mengikuti pembahasan penggunaan sub-agent. Ini izin khusus menyiapkan dan memakai pembagian internal untuk kampanye ini setelah ditunjuk; tidak memulai kampanye sekarang. Tidak perlu mengubah mandat global AGENTS.md. Batas lain tetap berlaku. Jika lingkungan pelaksana tidak menyediakan sub-agent, jalankan pembagian yang sama secara serial dan laporkan keterbatasannya; jangan memasang launcher atau mengubah konfigurasi global untuk mengakalinya.

### Kepemilikan pekerjaan

| Pelaksana | Tanggung jawab utama | Keluaran |
|---|---|---|
| Agen utama | E00/E01, baseline, manifest, penjadwalan sumber daya, UI/bridge E06, pengukuran E07, konsolidasi E08/E09 | Matriks induk, antrian batch, laporan akhir |
| Sub-agent A — ekonomi | E03 A/B, bagian D yang terkait aset/progres, rangkaian transaksi L1–L3 | Test dan bukti transaksi, rekonsiliasi, temuan ekonomi |
| Sub-agent B — waktu dan simulasi | E03 C/D untuk berita/NPC/waktu, L4/L5, kombinasi E05 terkait waktu | Test dan bukti simulasi, kejadian, ketepatan jadwal |
| Sub-agent C — persistensi | E04, kombinasi E05 terkait persistensi, save/reload dan recovery | Test dan bukti kegagalan/pemulihan, reproduksi minimal |

Agen utama menetapkan satu pemilik setiap skenario sebelum delegasi. Tabel ini membagi keahlian, bukan menggandakan jumlah tes. E05 lintas bidang dirancang bersama lewat agen utama, lalu diberikan kepada satu pelaksana. E08 berlaku untuk semua temuan; E09 hanya dikonsolidasikan agen utama. Tidak ada delegasi bertingkat tanpa kebutuhan atau menambah jumlah agen melebihi batas.

### Gelombang kerja dan dependensi

1. Agen utama menyelesaikan inventaris, isolasi QA, snapshot, batas resource dan L0. Bagikan keputusan terbaru, manifest dan lokasi bukti; jangan mengandalkan konteks percakapan yang mungkin tidak diwariskan.
2. Tiga sub-agent dapat secara paralel membaca jalur masing-masing, menyiapkan oracle, matriks, test minimum serta daftar celah. Agen utama menyiapkan UI/bridge dan meninjau cakupan lintas bidang.
3. Integrasikan kode uji yang sudah ditinjau secara serial, kompilasi satu baseline bersama, lalu bekukan manifest test untuk batch. Tidak boleh mengedit source/test yang sedang dikompilasi atau dipakai runner lain. Jika perlu perubahan, gunakan batas batch dan identitas revisi baru.
4. Jalankan smoke/tes fungsional ringan bersamaan hanya setelah terbukti masing-masing punya output build, temp, save, port dan proses terpisah. Mulai maksimal dua worker eksekusi ringan, bukan otomatis tiga. Jika runner memakai output bersama atau batas isolasi belum terbukti, eksekusinya serial sementara analisis tetap paralel.
5. L2/L3/L4/L5, fault injection proses, build bersama dan pengukuran berat mendapat giliran eksklusif dari agen utama. Saat satu batch berat berjalan, agen lain mengerjakan analisis log yang sudah selesai, reproduksi tertulis, pemeriksaan source, dan triase tanpa menjalankan beban lain.
6. Soak test dan benchmark untuk angka performa hanya berjalan ketika tidak ada build atau tes pesaing. Batasi kegiatan agen lain pada pekerjaan ringan; catat proses latar dan gangguan. Jika soak sengaja berbarengan beban lain, labeli sebagai stress contention terpisah, bukan baseline performa. Ulangi pengukuran bersih yang terpengaruh.
7. Tutup batch, periksa hash snapshot dan hash save pemain oleh agen utama, konsolidasikan temuan, lalu jadwalkan batch berikut. Setelah semua tahap tersedia selesai, agen utama memeriksa bukti tiap pemilik sebelum menyatakan hasil.

### Isolasi, perubahan file dan pembagian seed

- Direktori: Work/Stress-Test-Evidence/<run-id>/main, /economy, /simulation, /recovery; setiap pemilik menulis hanya bagiannya. Source produksi bersama hanya dibaca. Agen utama satu-satunya penulis matriks induk dan laporan konsolidasi.
- Tetapkan daftar file test per agen sebelum edit. Helper bersama dan runner hanya diubah oleh agen utama pada batas batch. Hindari worktree yang hanya memuat HEAD karena perubahan lokal produksi bisa hilang dari snapshot. Jangan membuat salinan source berbeda tanpa manifest yang membuktikan kesamaan input.
- Setiap penugasan mencantumkan: skenario, ruang file, snapshot, seed range, profil QA, oracle, target batch, larangan, output, batas resource dan syarat penyerahan. Gunakan koordinasi internal sub-agent yang tersedia; ini bukan izin mengirim pesan ke chat lain atau pihak luar.
- Alokasi seed dicatat sebelum menjalankan batch. L2 memiliki tepat 1.000 seed unik, L3 100 seed unik dengan rantai panjang; jangan memberi seluruh target yang sama ke setiap agen lalu menghitung duplikasi sebagai cakupan baru. Seed yang sengaja diulang untuk reproduksi/regresi diberi label dan dihitung terpisah.
- Tidak ada dua worker menggunakan save, backup, temp directory, port, build directory atau file log yang sama. Masing-masing memastikan proses yang dihentikan miliknya sendiri. Jika isolasi alat tidak didukung, serialkan alat tersebut, bukan membangun infrastruktur besar.
- Snapshot source sama tidak berarti state mutable boleh dibagi. Input QA identik boleh disalin secara terkendali; setiap worker memiliki salinan runtime sendiri. Pemain tidak menjadi sumber salinan.

### Penjadwalan ringan dan pemulihan kegagalan

Agen utama mengelola tabel pekerjaan sederhana (ID, pemilik, dependensi, snapshot, seed, status, slot resource, bukti), tanpa scheduler/daemon baru. Hanya agen utama memberikan satu slot kerja berat. Jangan menggunakan marker file sebagai lock lintas proses yang tidak aman; orkestrasi dilakukan melalui dispatch dan konfirmasi proses selesai. Worker tidak memulai pekerjaan berat tanpa slot tersebut.

Jika agen berhenti atau konteks habis, simpan checkpoint lalu konfirmasi prosesnya benar-benar selesai sebelum slot dipindahkan. Jangan menganggap status chat selesai berarti proses pengujian anak telah berhenti. Temuan atau crash worker bukan otomatis bug game: pisahkan kegagalan harness, resource host, dan produk. Setelah dua kegagalan sama ikuti aturan diagnosis proyek.

Setelah baseline, agen utama memberikan estimasi berdasarkan throughput dan jalur pekerjaan terpanjang, bukan membagi estimasi lama dengan jumlah agen. Laporkan waktu persiapan/analisis yang dihemat, waktu eksekusi nyata, dan tahap yang wajib serial. Target 8 jam lalu 24 jam soak tidak dipendekkan oleh jumlah sub-agent. Anggaran test dan standar bukti E00–E09 tetap utuh.

## E11 — Milestone, progres terlihat dan estimasi waktu wajib

Permintaan terbaru pemilik: agen wajib menampilkan milestone, progres, estimasi waktu setiap milestone, dan benar-benar menggunakan multi-agent. Dalam lingkungan yang mendukung, satu agen utama wajib mendelegasikan pekerjaan ke tiga sub-agent ekonomi, simulasi dan persistensi sesuai E10. Gunakan slot aktual; jika kurang, gunakan yang tersedia dan rotasikan pembagian. Jangan hanya menuliskan rencana delegasi. Tampilkan ID/nama sub-agent yang benar-benar dibuat, tugas, status dan hasilnya. Jangan mengklaim multi-agent jika tidak tersedia. Jika sama sekali tidak tersedia, laporkan hambatan ini dan mintakan arahan pemilik mengenai syarat multi-agent; lanjutkan inventaris independen yang aman, tetapi jangan diam-diam menganggap eksekusi serial memenuhi permintaan. Ini menggantikan fallback serial otomatis pada E10. Tidak perlu membuat launcher atau mengubah pengaturan global.

Sebelum batch panjang, tampilkan tabel milestone berikut dengan cakupan, pemilik, status, target, perkiraan durasi kerja, perkiraan sisa, ketergantungan, dan bukti. Tahap boleh bertumpang tindih hanya sesuai E10.

| Milestone | Cakupan | Syarat penyerahan |
|---|---|---|
| M0 | Serah terima Plan 14, E00 dan inventaris E01 | Snapshot, isolasi QA, batas dan daftar pekerjaan |
| M1 | Aktivasi sub-agent, oracle, harness dan L0 | ID agen nyata, cakupan, kontrol negatif dan baseline |
| M2 | Ekonomi dan transaksi, L1–L3, E03 A/B | Target aktual, rekonsiliasi, bug dan reproduksi |
| M3 | Waktu/NPC/berita dan L4/L5 | Hari game, seed, kejadian dan bukti |
| M4 | Fault injection dan recovery E04 | Matriks gangguan, siklus save/reload dan hasil |
| M5 | Kombinasi E05, UI/bridge E06 | Cakupan pasangan, perjalanan, lingkungan dan gap |
| M6 | Soak L6 dan performa E07 | Durasi nyata, metrik, kondisi dan batas |
| M7 | Android bersyarat E06 | Izin, lingkungan QA dan bukti; jika tidak tersedia tetap BELUM_DIUJI |
| M8 | Reproduksi akhir E08, konsolidasi E09 | Laporan, bukti, bug terbuka dan tindak lanjut |

Estimasi setiap milestone wajib berupa rentang, dasar hitung dan tingkat keyakinan. Sebelum kalibrasi tulis perkiraan awal belum terukur; jika belum layak memberi angka, tulis belum dapat diestimasi dan sebut pengukuran yang diperlukan. Setelah L0, hitung ulang dari throughput nyata, jumlah kerja tersisa, pengulangan dan dependensi. Pisahkan jam kerja aktif, waktu tunggu izin dan durasi nyata soak. Jangan membagi estimasi total dengan jumlah agen; milestone paralel memakai jalur ketergantungan terpanjang, batch eksklusif harus masuk antrian. Estimasi bukan janji. Perbarui jika hasil ukur atau bug mengubahnya; jangan mengarang jam selesai.

Kewajiban komunikasi:
- Tampilkan tabel awal sebelum eksekusi panjang dan ringkasan delegasi setelah sub-agent benar-benar dibuat.
- Selama agen utama aktif, berikan update singkat paling lambat sekitar 60 detik atau saat ada perubahan penting; jangan memakai panggilan blocking panjang yang menghalangi komunikasi bila tersedia polling terbatas. Update hanya memakai bukti terkini. Jika proses tidak mengekspos counter, tampilkan durasi dan status masih berjalan, jangan mengarang persentase.
- Pada awal/akhir milestone, bug P0/P1, hang atau perubahan estimasi besar, tampilkan laporan segera. Ambang perubahan estimasi besar: lebih dari 25% atau lebih dari satu jam dari rentang sebelumnya; bukan alasan menyembunyikan perubahan lain.
- Format ringkas: milestone/sub-agent aktif; aktual/target beserta unit; lulus/gagal/belum diuji; temuan baru; waktu berlalu; estimasi sisa dan keyakinan; hambatan; langkah berikut; tautan bukti/checkpoint.
- Jangan menghitung aksi, seed, hari game, milestone dan layar sebagai satu persentase. Tampilkan progres per ukuran; persentase per milestone hanya bila denominator jelas dan mencerminkan kerja nyata. Terhalang atau belum diuji tidak sama dengan selesai.
- Simpan tabel dan update milestone dalam PROGRESS.md di direktori run; agen utama satu-satunya penulis. Setiap sub-agent memberi counter dan checkpoint di direktori miliknya setelah batch, bukan menulis laporan induk bersama.
- Tidak mengirim notifikasi eksternal atau membuat automation. Ketika sesi berhenti, jangan menjanjikan pembaruan otomatis; simpan checkpoint dan berikan cara melanjutkan. Saat resume baca checkpoint, periksa proses dan snapshot, lalu tampilkan progres/estimasi terbaru.

## Prompt penyerahan untuk agen

Kerjakan rencana ini hanya setelah pemilik menunjuknya dan Plan 14 diserahkan. Fokus pada kampanye E00–E11, temuan nyata, reproduksi dan bukti. Sebagai agen utama, wajib gunakan multi-agent sesuai E10/E11 dengan tiga sub-agent internal jika slot tersedia untuk ekonomi, simulasi dan persistensi; mandat khusus pemilik ini menggantikan ketentuan satu pelaksana versi lama rencana. Pisahkan data QA, file test dan bukti; lakukan persiapan/analisis paralel, tetapi antrekan build bersama, pengujian berat dan pengukuran performa secara eksklusif. Jangan mengurangi target atau menghitung seed duplikat sebagai cakupan baru. Baca batas keselamatan dan instruksi proyek sebelum eksekusi. Inventaris tes dan runner aktual, identifikasi snapshot termasuk perubahan lokal, gunakan save QA terisolasi, dan kerjakan bertahap dengan checkpoint. Jangan mengubah source produk atau aturan untuk meluluskan test. Jangan menyentuh save pemain selain hash, jangan menjalankan ADB/instalasi/perangkat tanpa izin tindakan yang berlaku, dan jangan mengganggu agen lain. Tuntaskan tahap independen yang dapat dijalankan; laporkan hambatan dan gap tanpa mengarang PASS. Durasi panjang diperbolehkan, tetapi setiap batch harus menambah cakupan atau bukti. Serahkan bug dengan reproduksi, hasil aktual terhadap target, dan langkah berikut, lalu berhenti.

Wajib tampilkan milestone M0–M8, ID/nama sub-agent nyata dan pembagian tugas, aktual/target, bukti, serta estimasi durasi dan sisa waktu setiap milestone. Beri update berkala selama sesi aktif dan setiap perubahan penting sesuai E11; hitung ulang estimasi setelah baseline dan jangan mengarang progres. Simpan PROGRESS.md serta checkpoint agar bisa dilanjutkan. Jika multi-agent tidak tersedia, jelaskan hambatannya dan jangan mengklaim telah menggunakan multi-agent.
