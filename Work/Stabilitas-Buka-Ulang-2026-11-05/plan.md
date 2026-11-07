# Stabilitas buka ulang dan progres offline Game Saham

**Checkpoint eksekusi 5 November 2026.** Implementasi SU01–SU04 telah dikerjakan dengan bukti host/browser. SU05 memiliki bukti host dan APK pada `Artifacts/Stabilitas-Buka-Ulang-2026-11-05/pnl-precision-current/su-evidence.json`. Pengujian empat restart pada 30 hari juga meluluskan harga, volume, kas dan posisi seluruh akun setelah perbaikan presisi PnL. Gerbang Poco, ambang performa perangkat, dan sesi 60 menit tetap **NEEDS_VALIDATION**. Pemilik menyatakan Poco belum tersedia dan memilih melanjutkan bukti host. Rincian serta batas klaim ada di [LAPORAN-EKSEKUSI.md](LAPORAN-EKSEKUSI.md). Paragraf status review di bawah mempertahankan konteks penyerahan reviewer sebelum implementasi.

Status 5 November 2026 — review independen selesai; plan direvisi, implementasi **BELUM DIMULAI** oleh reviewer. Status runtime/perangkat: **NEEDS_VALIDATION**. Penugasan ini hanya mengubah dokumen ini; tidak mengakses save pemain, ADB, instalasi, build rilis, push, atau file lain.

## Kesimpulan dan lingkup

Plan menjawab risiko buka ulang setelah offline, tetapi belum membuktikan penyebab aplikasi menutup di HP. Urutan yang didukung: klasifikasi dan ukur → kurangi puncak alokasi tanpa perubahan semantik → kendalikan pertumbuhan yang terbukti → checkpoint catch-up → buktikan startup dan perangkat. Pembagian batch saja tidak membatasi ledger yang terus membesar; kompresi berkas saja tidak mengurangi object graph saat load. Jangan menambah database, engine, scheduler, atau worker baru sebelum pendekatan existing terbukti tidak cukup.

Pertahankan 48× untuk seluruh waktu aktif, background dan offline. Aturan IDX/US 24 jam serta rollover 00.00 WIB berasal dari [keputusan pemilik 4 November, Q1–Q2](../Review-Anotasi-2026-11-04/KEPUTUSAN-QA.md), lebih baru daripada aturan sesi lama. Cutoff reksa dana 16.00 tetap; tidak mengembalikan sesi lama sebagai optimasi. Tema dan perluasan berita US Pro/Whale di luar lingkup. Persetujuan plan ini bukan persetujuan perubahan UI atau kebijakan retensi.

## Bukti dan batas diagnosis

Checkout ditinjau pada `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`, branch `codex/classic-ui-new-assets`, HEAD `9653f7a992405b4dfaf28acddbf1caf158d03957`. Working tree berisi banyak perubahan lokal; HEAD saja tidak mengidentifikasi source yang dijalankan. Saat SU01, rekam hash source/test/runner/fixture dan APK yang dipakai. `Work/STATUS.json` dibaca sebagai riwayat, bukan acceptance kini.

- Pemilik melaporkan aplikasi menutup setelah lama didiamkan. Belum ada diagnosis perangkat yang mengikat kejadian tersebut ke APK, timestamp, proses dan stack tertentu.
- Draft awal melaporkan domain tujuh hari nyata melewati pengamatan 75 detik sebelum optimasi berita, kemudian sekitar 6,3 detik; startup bridge dengan heap Java 384 MB masih `Java heap space`. Ukuran save sekitar 58 MB, ledger 44 MB, buku pasar 12 MB; dua hari bridge dan 14 tes berita disebut lulus. Angka ini **bukti awal yang perlu dilengkapi command, log mentah, hash dan fixture**, bukan pengukuran ulang reviewer atau gerbang PASS. Timeout 75 detik bukan pengukuran durasi selesai.
- `Artifacts/Annotation-Execution/cold-resume-qa/Probe.java` yang dibaca memang membuat fixture baru dan membuka domain pada jam tetap +7 hari, tetapi tidak menguji bridge atau Android, tidak memberi assertion rinci ekonomi, dan tidak menetapkan heap sendiri. Jangan memakai probe ini sebagai bukti startup penuh.
- `Artifacts/Annotation-Execution/browser-resume-2026-11-04/large-save-memory-proof.json` mencatat heap 384 MB, dua respons save sukses dan ukuran sekitar 39 MB. Itu bukan bukti catch-up tujuh/30 hari atau keselamatan perangkat. Reviewer hanya membaca metadata ini, bukan isi save yang disebutnya.
- Tes simulasi write gagal disebut gagal juga pada baseline. SU01 harus mereproduksi dan mengklasifikasi assertion/fixture sebelum menjadikannya gate; kegagalan baseline bukan izin mengabaikan perlindungan data.

| Kelas kegagalan | Bukti yang harus dicari pada fase eksekusi berizin | Batas inferensi |
|---|---|---|
| Java OOM / exception | Stack lengkap, tahap load/advance/validate/serialize/save/snapshot, heap limit dan puncak alokasi | OOM host membuktikan risiko jalur host tersebut, bukan penyebab penutupan HP |
| ANR | Rekaman ANR/main-thread stack dan lock yang ditunggu saat kejadian | Worker existing tidak menjamin UI responsif jika pembaca UI menunggu monitor facade |
| Native crash | Tombstone/signal dan identitas proses/library | Jangan menyebutnya Java OOM tanpa bukti |
| Proses dihentikan sistem | Alasan exit yang tersedia, PID, tekanan memori, lifecycle | Hilangnya proses tanpa stack bukan otomatis crash; force-stop QA bukan reproduksi low-memory kill |
| Renderer WebView mati | Kejadian renderer, PID renderer, versi WebView dan kondisi proses aplikasi | Recovery renderer tidak menyelesaikan OOM domain Java; uji kehilangan renderer terpisah |

Jika bukti Android tidak tersedia, status penyebab pengguna tetap **NEEDS_VALIDATION**. Penanganan `catch (Throwable)` pada bridge bukan bukti proses bisa pulih dari OOM; hasil error pun memerlukan alokasi. Jangan meluluskan retry atas state ekonomi yang mungkin sudah termutasi sebagian.

## Temuan source yang mengubah rencana

Rujukan berikut relatif terhadap `android/app/src/main/java/com/tradingglobal3d/`; nomor baris adalah snapshot review, bukan kontrak permanen.

| Source yang dibaca | Temuan statis | Implikasi |
|---|---|---|
| `mobile/MainActivity.java` onCreate/onPause/onStop/onResume; `mobile/TradingWebBridge.java:64–146` | Lifecycle sudah memakai `beginClockSync`, satu worker, pending dan epoch; worker melakukan load/resume, lifecycle, sync, save lalu snapshot | Reuse jalur ini. Uji pembatalan pemilik lama, callback beruntun dan semua pembaca UI; jangan menambah worker kedua |
| `core/facade/TradingFacade.java:113–134,340–382,2847` | Catch-up memproses durasi penuh; anchor kemudian diisi waktu nyata yang dibaca lagi. Loop existing melompat ke cutoff 16.00/midnight dan memperhitungkan deadline margin | Tetapkan target waktu sekali per siklus, lalu buktikan waktu yang berlalu selama komputasi tidak hilang. Checkpoint tidak boleh mendahulukan anchor sebelum ekonomi selesai |
| `core/economy/EconomyLedger.java` | Entries, indeks akun dan processedRequests dipelihara; restore membangun deduplikasi dari entries; rekonsiliasi menjumlah mutasi dari nol | Ledger bukan sekadar log. Menghapus entry dapat menghapus receipt/idempotensi atau basis rekonsiliasi |
| `core/career/CareerManager.java:284`; caller ledger di facade/business/risk | Ada indeks posisi ledger, hitungan entry dalam ID serta pembaca receipt | Bahkan pemangkasan NPC dapat menggeser indeks laporan pemain atau mengganti ID. Petakan seluruh caller sebelum memilih compaction |
| `core/trading/OrderBook.java:68–73`; `core/npc/MarketEngine.java:48–68,233` | Fills sudah dibatasi 500 per instrumen; export memuat fills dan referensi antrean aktif; aktivitas harian menghasilkan transaksi melalui matching | Pisahkan jumlah instrumen × fills, order aktif dan histori final. Ukuran books besar belum membuktikan pertumbuhan tak terbatas. Jangan menghapus aktivitas harian/volume untuk mengecilkan save |
| `core/storage/SaveState.java:105–113,116`; `SaveManager.java:47–54,89 dst` | Copy ledger memakai referensi immutable, tetapi runtime JSON disalin lewat string; save memvalidasi salinan, membuat JSON penuh, membaca primary lama dan mungkin memvalidasinya | Ukur salinan hidup bersamaan pada load/validation/save. Jangan mengulang optimasi copy ledger yang sudah ada atau melepas validator untuk menghemat heap |
| `SaveManager.java` atomic move/robustMove; facade capture/restore | Ada temp+sync, backup, write-authority dan timestamp fence; fallback move biasa tersedia | Reuse mekanisme ini, tetapi uji fallback dan crash consistency. Label atomic dan timestamp saja bukan pembuktian commit state+cursor atau urutan writer saat timestamp sama |

Pemetaan caller `getTradeHistory` mencakup facade, bridge dan export; ledger dibaca oleh domain karier, bisnis, risiko, penyimpanan dan facade. SU02 wajib menelusuri fungsi pembaca secara lengkap sebelum perubahan; pencarian nama pada review ini belum membuktikan semua semantik aman dipangkas.

## Pelajaran REA dan batas penggunaannya

- [Capitalism, bagian 6](<C:/Users/HYPE AMD/Downloads/REA/com.SyGame.Capitalism/LAPORAN_DEKOMPILASI_RUMUS_MATEMATIKA.md:166>) melaporkan analisis biner `savetime/daytimer`, jalur minimize dan cold start, serta pembagian tiga gelombang untuk >100 langkah. Laporan juga menyebut cap 240/720 unit, 20 detik per unit, dan cold start pendek yang dilewati. Ini bukti analisis statis yang dibaca, **bukan profiling runtime atau bukti bebas ANR**. Ambil pemisahan lifecycle dan kerja bertahap; jangan menyalin cap, pengabaian waktu, angka tiga, server jam atau Unity. Tidak ada bukti pada bagian ini bahwa checkpoint antar-gelombang atomik atau exactly-once.
- [Tycoon Game](../REA-APK-ANALYSIS.md) menunjukkan URI/model pasar, portofolio dan transaksi serta petunjuk library/backend dari analisis Flutter AOT yang PARTIAL. Implementasi catch-up, scheduler server, rumus dan perilaku offline belum dipulihkan. Pemisahan snapshot awal/detail adalah pilihan Game Saham untuk diukur, bukan teknik runtime Tycoon yang sudah terbukti. Jangan membawa rasio waktu lama dari laporan tersebut ke plan ini.

## Tahapan dan gerbang

| ID | Pekerjaan yang disarankan | Bukti untuk lanjut |
|---|---|---|
| SU01 | Reproduksi sebelum perbaikan lewat domain dan worker/bridge pada fixture QA baru/bertransaksi. Bekukan identitas, jam dan input; ukur tahap load, validasi, catch-up, capture, serialize, backup/commit dan snapshot. Lengkapi provenance angka awal; pisahkan klasifikasi Android. | Command, exit code, timeout, stack, hash dan profil pertumbuhan 1/2/7/30 hari. Kasus merah ber-assertion atau pernyataan jujur belum direproduksi; jangan memaksakan OOM jika tidak muncul |
| SU02a | Utamakan pengurangan salinan/alokasi berulang yang terbukti mahal tanpa mengubah format/semantik atau mengurangi validasi. Pertahankan lock/immutability yang diperlukan saat reuse data. Ukur load save besar juga, bukan hanya save baru kecil. | Peak live heap dan waktu per tahap sebelum/sesudah; rekonsiliasi dan round-trip setara; tidak ada alias state mutable atau stale snapshot |
| SU02b | Jika ledger tetap tumbuh melewati budget, petakan retensi per tipe/akun, receipt, indeks dan seluruh caller. Bandingkan ringkasan NPC final yang terbukti tak diperlukan dengan arsip detail terpisah. Books dinilai sendiri karena fills sudah bounded. | Kontrak data yang dipertahankan, dampak format/retensi, ukuran dan heap terukur; opsi yang mengubah histori pemain menunggu keputusan. Deduplikasi, biaya/PnL, saldo dan laporan tidak berubah |
| SU03 | Reuse satu worker dan batas ekonomi existing; checkpoint hanya pada state lengkap yang konsisten, dengan target waktu tetap dan cursor/remainder persisten. Pilih batch berdasarkan biaya terukur; batas waktu adalah soft budget, tidak memutus settlement di tengah mutasi. | Uji commit/restart/gagal/retry di bawah, tanpa kewajiban ganda atau waktu hilang; setiap batch punya rentang yang jelas dan semua sisa tetap terutang |
| SU04 | Reuse status sinkronisasi/snapshot yang tersedia. Ukur UI yang menunggu lock dan payload startup; rincian ditunda hanya jika diperlukan. Aksi ekonomi ditolak di domain selama pending/failed dan sampai commit valid. Pemulihan renderer hanya jika diagnosis mendukung. | Browser membuktikan state dan respons interaksi; Android membuktikan main thread/renderer. Preview dan persetujuan wajib untuk teks/tampilan/alur baru |
| SU05 | Jalankan matriks waktu, kompatibilitas, gangguan dan perangkat pada snapshot yang sama. Pisahkan domain JVM, bridge host, browser, APK dan Poco. | 7/30 hari lolos startup penuh host 384 MB beserta assertion ekonomi, kemudian gate perangkat; source berubah berarti bukti relevan diulang |

### Pilihan minimal untuk data dan kompatibilitas (SU02)

1. Pisahkan biaya permanen (jumlah entries, indeks, request ID, order/fill, histori emiten) dari biaya sementara (JSON tree/string/byte array, copy validator, primary lama). Catat byte UTF-8 dan MiB heap secara berbeda. Jangan menyimpulkan slope 30 hari dengan mengekstrapolasi satu fixture tujuh hari.
2. Coba perbaikan representasi/alokasi yang mempertahankan data terlebih dahulu. Streaming atau arsip terpisah hanya dipilih bila profiling menuntutnya; reuse pustaka yang tersedia. Gzip dapat menghemat disk, tetapi tetap gagal bila semua data dibuka menjadi object graph yang sama.
3. Compaction hanya untuk data yang kontrak pembacanya terbukti dipenuhi. Ringkasan saldo saja tidak menggantikan receipt asli, request ID, biaya/PnL, urutan kejadian, baseline indeks karier atau histori pemain. Pertahankan kas/posisi/issued shares, reservation kas/saham, antrean harga-waktu, trigger, AON, anti-self trade, kewajiban dan OHLCV/volume dari transaksi nyata. Jangan mempertahankan set ID yang terus tumbuh lalu mengklaim memori sudah bounded.
4. Jika mengubah format, sediakan reader save lama dan transisi tervalidasi dari salinan QA: load → rekonsiliasi → tulis kandidat → reload → bandingkan domain → baru commit. Primary/backup lama tetap bisa dipulihkan saat gagal. Save lama besar harus dapat dibaca dalam budget sebelum manfaat compaction berlaku. Kompatibilitas membaca save lama berbeda dari kemampuan APK lama membaca format baru; kebutuhan downgrade harus diputuskan, bukan diasumsikan.
5. Arsip terpisah menambah risiko commit lintas-file: snapshot harus merujuk generasi arsip yang lengkap dan tervalidasi; arsip belum committed tidak menjadi sumber kebenaran. Hindari opsi ini jika perbaikan lebih kecil sudah lolos gate. Jangan menerapkan migrasi ke save pemain dalam pekerjaan ini.

### Kontrak checkpoint tanpa duplikasi (SU03)

- Di awal siklus, ambil target waktu nyata `T` sekali. State committed menyatakan waktu ekonomi yang **sudah selesai**, anchor nyata yang ekuivalen dan sisa subdetiknya. Remainder konversi 48× dibawa tepat satu kali; jangan menandai seluruh interval telah diproses saat baru sebagian selesai.
- Commit mencakup clock/cursor bersama akun, ledger/receipt, order/reservation, settlement marker, RNG/urutan deterministik yang diperlukan dan domain kewajiban lain. Jangan menulis cursor lebih dulu atau menyimpan settlement setengah jadi. Existing marker dipakai sebelum menambah jurnal baru.
- Waktu komputasi setelah `T` ditangani siklus berikutnya; jangan memajukan anchor ke jam terbaru sambil membuang selisih tersebut. Uji juga deadline berbasis waktu nyata (berita, margin, bunga), bukan hanya midnight game. Jangan membuat snapshot masa depan atau membuka transaksi di state setengah catch-up.
- Setelah kegagalan advance/save, state memori bisa lebih maju dari disk. Retry harus memakai state committed valid atau protokol yang terbukti idempoten; sekadar memanggil ulang loop pada memori parsial tidak diterima. Commit sukses tetapi callback hilang harus dikenali saat reload tanpa mengulang kewajiban.
- Fault injection pada sebelum settlement, setelah mutasi sebelum commit, saat temp ditulis, sesudah temp sync, saat rotasi backup, sebelum/sesudah replace dan sebelum acknowledgement. Cakup write penuh/disk gagal, primary rusak, backup valid, keduanya rusak, fallback non-atomic, writer superseded dan timestamp sama. Gunakan fixture QA; jangan menangkap exception saja lalu menyebutnya simulasi process death.
- Bandingkan proses tanpa gangguan dengan beberapa restart di batas dan satu gangguan di tengah mutasi: total kewajiban sama, tidak ada debit/refund/dividen/IPO/bunga/fee/reward ganda, reservation tidak hilang, semua waktu terselesaikan. Keduanya rusak harus berhenti dalam recovery, bukan otomatis membuat progres kosong.

### Matriks pembuktian (SU05)

| Durasi nyata fixture | Durasi game 48× | Assertion utama |
|---|---|---|
| 1 hari | 48 hari | Jam akhir + remainder, cutoff dan rollover tepat |
| 2 hari | 96 hari | Regresi jalur bridge yang sebelumnya dilaporkan lulus |
| 7 hari | 336 hari | Reproduksi risiko awal, startup load → catch-up → commit → snapshot → reload |
| 30 hari | 1.440 hari | Pertumbuhan data, puncak heap, seluruh kewajiban dan keberlanjutan setelah reopen berikutnya |

- Gunakan katalog representatif, profil baru dan profil bertransaksi: order aktif/trigger, dana tertahan, IPO, reksa dana, utang/margin/short, bisnis/firma dan langganan. Fixture sintetis diberi label. Variasikan cold start, minimize, onPause/onResume beruntun, jam mundur, remainder subdetik, dan deadline tepat sebelum/sesudah cutoff. Tidak perlu menunggu 30 hari nyata atau mengubah jam sistem global.
- Oracle: interval pendek dapat dibandingkan dengan jalur referensi pada seed/input/jam tetap dan kontrak determinisme yang dinyatakan. Rentang panjang dibandingkan juga dengan batch/restart, rekonsiliasi serta jumlah/tanggal kewajiban yang diharapkan. Kesamaan dua run yang menjalankan bug sama tidak cukup. Bandingkan nilai ekonomi, bukan byte JSON/ID berbasis waktu yang memang nondeterministik; dokumentasikan pengecualian tanpa mengabaikan perbedaan harga atau volume.
- Gate host: jalur bridge lengkap pada `-Xmx384m`, 7 dan 30 hari selesai tanpa OOM, timeout, status FAILED/PENDING tersangkut, kehilangan waktu atau duplikasi; reload hasil dan reopen kedua lulus. Rekam waktu tiap tahap, peak heap/GC, ukuran per bagian dan slope data. Budget heap ini bukan budget total proses Android. Mulai sedikitnya tiga cold start terpisah untuk rentang 7/30 hari, laporkan semua hasil, bukan hanya run terbaik.
- Gate UI: waktu ke shell merespons, waktu sinkronisasi, waktu sampai transaksi aman dan respons selama catch-up dicatat terpisah. Tidak menerima screenshot/keberadaan indikator sebagai bukti respons. Test retry harus membuktikan nilai/status berubah dan snapshot stale ditolak.
- Gate perangkat **NEEDS_VALIDATION** sampai pengujian berizin: Poco X8 Pro, identitas/hash APK, versi Android/WebView, kondisi RAM, log exit/ANR/renderer, memori proses aplikasi dan renderer serta waktu UI/transaksi. Jalankan 7/30 hari dengan jam QA terisolasi, cold start/minimize/reopen berulang dan sesi 60 menit setelah catch-up. Jangan klaim crash pengguna tertutup hanya karena build, browser atau host lulus.
- Tetapkan target durasi dan margin memori perangkat setelah baseline, sebelum menyatakan kinerja diterima; catat angka serta alasan. Tanpa ambang yang disepakati, laporkan pengukuran dan status performa **NEEDS_VALIDATION**, bukan “lancar”. Gate integritas tetap wajib dan tidak menunggu angka performa.
- Saat implementasi nanti, reuse runner regresi yang relevan termasuk quality gate plan performa jika hasilnya diklaim: `python Work/Perbaikan-Lanjutan-Performa-2026-11-03/quality_gate.py run`, lalu `check <gate.json>` dan `check <gate.json> --claim full` untuk klaim penuh. Simpan exit code; exit 2 bukan PASS. Gate existing bukan pengganti matriks SU05 yang belum tercakup. Review perubahan assertion/verifier bersama diff aplikasi.

## Keputusan yang masih memerlukan pemilik

1. **MENUNGGU_KEPUTUSAN bila SU02b diperlukan:** retensi/detail histori pemain atau ringkasan yang mengubah informasi yang dapat dilihat; kebijakan migrasi dan kebutuhan downgrade APK. Rekomendasi default: pertahankan semantik dan format existing selama perbaikan alokasi mencukupi.
2. **MENUNGGU_KEPUTUSAN bila SU04 mengubah pengalaman:** preview indikator, teks gagal/retry, navigasi saat pending atau alur recovery. Pengukuran dan perbaikan domain independen dapat dipersiapkan setelah penugasan implementasi.
3. Target waktu startup/transaksi dan margin sumber daya HP ditetapkan setelah baseline; izin perangkat diperiksa pada fase eksekusi. Review ini tidak memberikan izin ADB atau instalasi.

## Handoff review

Perubahan konkret: memisahkan OOM/ANR/native/kill/renderer; menurunkan angka awal menjadi bukti yang perlu provenance; mencatat batas 500 fills existing dan ketergantungan ledger; menambahkan SU02a sebelum compaction; memperjelas anchor/remainder/target tetap dan commit gagal; memperluas gate 48×, migrasi, restart dan perangkat; mengikat aturan 24 jam ke keputusan terbaru; membatasi klaim REA pada bukti statis.

Verifikasi review: baca source dan caller yang disebut, keputusan pemilik, laporan REA Capitalism/Tycoon, probe QA dan metadata save-memory; baca ulang dokumen akhir serta periksa whitespace. Tidak menjalankan suite aplikasi, probe, quality gate atau APK karena perubahan hanya plan. Tidak membaca maupun meng-hash save pemain. Semua SU01–SU05 masih **PENDING eksekusi**, penyebab penutupan HP **NEEDS_VALIDATION**.

`next_action`: setelah plan ini ditunjuk untuk implementasi, mulai SU01 dengan mengikat log/fixture awal ke hash working tree dan mereproduksi startup bridge tujuh hari pada heap 384 MB; pisahkan tahap gagal sebelum memilih optimasi. Hentikan pekerjaan review setelah penyerahan ini.
