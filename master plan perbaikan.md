# Master Plan Gabungan Game Saham: Waktu, Trading, dan Redesain Seluruh UI

## Delta eksekusi yang disetujui pemilik

24 Oktober 2026: pemilik mengoreksi cara pengiriman. Pekerjaan dibagi menjadi delapan file plan.md terpisah di Work/Master-Plan-Gabungan-2026-10-24/Plans/. Pemilik mengirim SATU file, Hermes menuntaskan file tersebut, menyerahkan hasil, lalu BERHENTI menunggu file berikutnya dari pemilik. Daftar urutan dan master 126 task tidak memberi izin melanjutkan file lain secara otomatis.

24 Oktober 2026: setelah tiga contoh awal ditinjau, pemilik meminta “warnanya background buat gelap, warna ui nya buat terang, lalu untuk susunan tabnya tetap samakan dengan yang lama”, kemudian menyetujui revisi 2 secara eksplisit. Arah visual aktif: background navy gelap dengan panel/kontrol terang, tab Beranda/Pasar/Portofolio/Bisnis/Lainnya tetap. Ini menggantikan usulan background terang menyeluruh dalam bagian 0/N16; seluruh 126 ID dan requirement nonvisual tetap. Bukti persetujuan/contoh dan progres eksekusi di `Work/Master-Plan-Gabungan-2026-10-24/CHECKPOINT.md` dan `TRACKER.csv`, bukan master spesifikasi kedua. Dokumen rencana di bawah belum membuktikan implementasi selesai.

24 Oktober 2026: urutan pelaksanaan diperinci menjadi delapan kelompok G01–G08 pada bagian 18, dengan APK kandidat bertahap. Seluruh 126 requirement dan status penyelesaian dipertahankan. Pengelompokan tidak mencabut jeda pemilik; Hermes mulai G01 hanya setelah diminta lanjut.

Revisi: 24 Oktober 2026, Asia/Jakarta. Dokumen tunggal untuk eksekusi Hermes. **RENCANA, BELUM DIIMPLEMENTASIKAN.**

## 0. Keputusan final dan cara memakai dokumen

Dokumen ini menggabungkan master plan 13 anotasi sebelumnya, tujuh anotasi browser tambahan, 20 jawaban pemilik, tiga klarifikasi final, dan mandat eksekusi yang ditempel pemilik. Codex hanya menyusun dokumen; **Hermes yang melaksanakan kode, pengujian, dan APK lokal**. Arsip versi sebelumnya: `Work/Master-Plan-Archive/master-plan-13-anotasi-sebelum-penggabungan-20261024-0003.md`, SHA-256 `0A4972C7685CA580E4B0D9CD19E491F5BF3F03FF9E42B7FFE95249C82DC42DA9`.

### 0.1 Kontrak pemilik yang berlaku

| Topik | Keputusan final |
|---|---|
| Tempo dunia | **1 hari kalender game = 1 jam nyata** saat aktif, background, maupun aplikasi ditutup. Laju 24 detik game/detik nyata. |
| Kontrol waktu | Tetap otomatis, tanpa play/pause pemain, percepat, atau skip waktu. Tidak mengembalikan kartu Jadwal Sesi. |
| Berita | Efek berlangsung 60/120 menit nyata, deadline persisten, sisa waktu terlihat. Tidak mengikuti durasi candle atau rendering. |
| Grafik | Tepat empat rentang riwayat: **6 jam, 1 hari, 7 hari, 1 bulan** dalam waktu game; default 1 hari; 1 bulan berarti 30 hari terakhir. Resolusi tiap candle menyesuaikan. |
| Bentuk visual | Trading menyerupai kerapian aplikasi broker; progres/hadiah tetap terasa seperti game taipan. Referensi Stockbit dan Ajaib hanya pola informasi/interaksi. |
| Tema | **Terang**, latar lembut, panel putih, teks gelap, kontras jelas. Tidak perlu membangun tema gelap baru. |
| Aksen | Tiap kelompok fitur punya aksen konsisten. Hijau/merah tetap bermakna untung/rugi; warna bukan satu-satunya pembeda. |
| Navigasi utama | Hermes boleh **mengusulkan** struktur baru, tetapi wajib memperoleh persetujuan sebelum mengimplementasikannya. Lima tab lama menjadi baseline sementara. |
| Menu | Daftar ringkas berkelompok, ikon fungsional, judul, dan satu baris keterangan. Tidak semua fitur diberi kartu besar seragam. |
| Portofolio | Pecah ke subkategori yang relevan; jangan memaksa fitur berbeda masuk satu kategori. Jangan membuat tumpukan alat dalam satu accordion. |
| Lainnya | Kelompok **Profil, Misi & Kompetisi, Riset, Pengaturan**. Detail dan state turunannya ikut diperbarui. |
| Bahasa | Gunakan istilah trading umum. Bantuan singkat hanya bila diperlukan; tanpa tutorial panjang. |
| Pemegang saham | Tampilkan pemegang saham **dunia game**, termasuk pemain/NPC/institusi yang benar-benar tercatat pada domain. Bukan daftar pemegang saham perusahaan asli. |
| Posisi pribadi | Tetap terpisah dari komposisi pemegang saham emiten. Saat pemain belum punya saham, panel emiten tetap dapat menampilkan pemegang saham lain. |
| Pemangkasan | Hapus lab strategi dan uji stres portofolio dari UI. Pertahankan bisnis, karier, misi, liga, dan aset yang memengaruhi uang atau progres. |
| Kompatibilitas | Penghapusan UI tidak otomatis menghapus data save/kelas bersama. Tidak reset progres, saldo, koleksi, atau posisi. |
| Persetujuan visual | Hermes menunjukkan **2–3 contoh layar utama**, palet, dan peta navigasi, lalu meminta persetujuan sebelum menyebarkan redesain. |
| Motion | Lebih ekspresif pada progres dan hadiah, terkendali pada perdagangan, tunduk reduced motion dan performa. |
| File | Perbarui file ini sebagai satu master aktif, arsipkan versi lama. Tidak membuat master eksekusi kedua yang saling bertentangan. |

Keputusan di atas menggantikan instruksi lama tentang tempo aktif 5 menit, larangan redesain menyeluruh, tema gelap klasik wajib, lima tab yang tidak boleh berubah sama sekali, dan kewajiban mempertahankan launcher lab/uji stres. Instruksi keselamatan data, arsitektur, transaksi, bukti, dan fitur ekonomi lain tetap berlaku. Jika `AGENTS.md`, `DESIGN.md`, fixture, atau standar fase masih menyebut kontrak lama, Hermes merekonsiliasinya secara terarah dalam tugas yang sesuai; jangan menganggap dokumen lama membatalkan keputusan pemilik terbaru ini.

### 0.2 Pelacakan pekerjaan dan konflik

- Pertahankan seluruh **77 ID subtask N01–N13** dari rencana lama. Arti N01/N11/N13 dan penerimaan terkait direvisi di dokumen ini sesuai keputusan terbaru; jangan menjalankan kontrak lama dari arsip.
- Tambahkan **49 subtask N14–N20**. Total **126 subtask bernomor dalam 20 paket kerja**. Ini bukan pernyataan ada 20 anotasi unik; beberapa komentar baru mengulang atau memperluas anotasi lama.
- Pada penyusunan awal rencana, seluruh 126 tugas dimulai PENDING/PENDING_VERIFICATION. Status terkini mengikuti tracker existing; jangan mereset task yang sudah memiliki status/bukti. Pembuatan atau pemecahan file plan tidak meluluskan implementasi.
- Catat riwayat perubahan requirement di tracker. Bila tugas lama secara eksplisit dibatalkan pemilik, tandai `SUPERSEDED` dengan rujukan, jangan menghilangkannya dari denominator tanpa penjelasan. Tugas revisi di sini tetap harus dikerjakan.
- Ketika menunggu persetujuan contoh visual, Hermes boleh mengerjakan preflight, penelusuran, kontrak waktu, tes domain, serta inventaris fitur yang independen. **Jangan menerapkan navigasi atau menyebarkan desain yang belum disetujui.**

### 0.3 Pemetaan tujuh anotasi tambahan

| Komentar baru | Target | Analisis dan tugas |
|---|---|---|
| C01: hilangkan Tabel OHLCV Alternatif | `#instrument-chart-section > button.btn-secondary` | Pengulangan N05; satu implementasi, tetap dilacak ke dua sumber komentar. |
| C02: empat waktu grafik | `#instrument-chart-section` | N14; pilihan adalah rentang terlihat, bukan durasi satu candle. |
| C03: arti belum memiliki posisi | `#detail-holdings-card` | Pesan lama membahas posisi akun terpilih. Permintaan terbaru menambah komposisi pemegang saham emiten di N15, tanpa menghapus kebutuhan posisi pribadi N10. |
| C04: seluruh fitur Lainnya membingungkan | `#main-content` pada tab Lainnya | N13, N16, N18; seluruh flow, detail, review, receipt, empty/error/locked ikut dalam audit. |
| C05: alat Portofolio menumpuk | `details#asset-tools` | N11 dan N17; kategori koheren dengan halaman masing-masing, bukan menambah puluhan kartu di halaman yang sama. |
| C06: jelaskan uji stres | Header flow uji stres | Penjelasan source pada bagian 23; keputusan final menghapus akses UI melalui N19. |
| C07: hapus lab bila tidak berpengaruh | Header lab strategi | Source memakai akun klon; hapus UI N19 dan pertahankan kompatibilitas data yang diperlukan. |

### 0.4 Kondisi saat penyusunan revisi

Root yang diperiksa: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`. Branch `codex/classic-ui-new-assets`, HEAD `afd8b23581d0b760f96132dae29ad488849880c4`; ditemukan 84 entri perubahan/untracked pada snapshot pemeriksaan. Nilai itu tidak membuktikan kepemilikan writer atau keadaan setelah dokumen disimpan. Hash save pribadi yang dibaca saat penyusunan: `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`. Hermes wajib memeriksa ulang keadaan aktual, bukan mengembalikan checkout ke snapshot ini.

Bagian berikut mempertahankan rincian operasional rencana sebelumnya dengan kontrak yang telah diperbarui. Angka pemeriksaan historis tetap berlabel historis. Bagian 23–32 menambah analisis source, tugas baru, referensi, dan prompt eksekusi gabungan.

Tanggal penyusunan awal: 23 Oktober 2026, Asia/Jakarta; keputusan aktif mengikuti revisi bagian 0.  
Tujuan: bahan eksekusi lengkap untuk AI agent yang mengerjakan proyek.  
Status: **RENCANA EKSEKUSI; bukan laporan bahwa seluruh perbaikan sudah selesai.**  
Root produksi: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`.  
Preview yang menjadi dasar anotasi: `http://127.0.0.1:18917/?v=automatic-world-20261023`.  
Branch saat pemeriksaan: `codex/classic-ui-new-assets`.  
HEAD saat pemeriksaan: `afd8b23581d0b760f96132dae29ad488849880c4`, 23 Oktober 2026 pukul 16.31.48 WIB.  
Source mempunyai perubahan lokal setelah HEAD, termasuk perubahan waktu otomatis dan pekerjaan agen lain. **HEAD tersebut saja tidak mengidentifikasi kode yang sedang dipreview.** Agen pelaksana wajib mencatat hash source aktual pada setiap checkpoint.

## 1. Mandat, keputusan terbaru, dan batas pekerjaan

Target akhir proyek adalah **seluruh 20 paket N01–N20 dan 126 subtask** dalam dokumen ini; pelaksanaan satu kiriman dibatasi pada file plan.md yang dikirim pemilik. Setiap paket memiliki tugas, kriteria penerimaan, dan bukti tersendiri. Jangan menyatakan beberapa paket selesai berdasarkan satu screenshot atau satu tes.

Dokumen ini disusun dari 13 komentar dan screenshot yang dikirim pemilik serta pemeriksaan source lokal. Pada tahap penyusunan dokumen, tidak dilakukan implementasi baru terhadap 13 anotasi ini. Beberapa fondasi waktu otomatis sudah diubah pada pekerjaan sebelumnya; agen berikutnya harus memeriksa, mempertahankan yang benar, dan menuntaskan integrasinya.

### 1.1 Keputusan pemilik yang mengikat

1. **Keputusan tempo terbaru: 1 hari game = 1 jam nyata, aktif maupun ditutup.** Ini menggantikan kontrak aktif sebelumnya. Bukan waktu nyata 1:1.
2. Laju tunggal **24 detik game per detik nyata**; 3.600.000 ms nyata menghasilkan 86.400 detik game. Tanpa percepat/skip. Sisa fraksi, anchor, dan lifecycle tetap dipertahankan.
3. Dunia mulai otomatis dan tetap berkembang selama aplikasi ditinggalkan. Kartu “Sesi pasar otomatis / Jadwal Sesi” yang sebelumnya diminta dihapus tidak boleh muncul kembali. Pemain tidak perlu menekan play, membuka briefing, atau menutup hari secara manual agar dunia berlanjut.
4. Efek berita pada N02 harus memiliki durasi yang mengikuti **waktu nyata**, misalnya satu atau dua jam nyata. Ini kontrak waktu yang berbeda dari tempo dunia. Keputusan nomor 1 tidak membatalkan N02.
5. Redesain seluruh UI monoton/membingungkan menjadi tema terang dengan karakter broker pada trading dan game pada progres. Lima tab lama menjadi baseline sampai usulan navigasi dan 2–3 contoh layar mendapat persetujuan pemilik; jangan menetapkan empat/lima tab baru secara sepihak.
6. Pertahankan isi Aset terbaru serta fitur yang memengaruhi perdagangan, uang, dan progres. Lab strategi dan uji stres dihapus dari UI sesuai N19; fungsi bersama dan data lama dilindungi.
7. Tidak membuat layar tutorial panjang untuk menjelaskan jenis order. N07 meminta antarmuka yang membantu pengguna memahami melalui pilihan, susunan field, label tindakan, dan feedback singkat.
8. Hilangkan label yang menjelaskan bahwa permainan “hanya simulasi” dari alur bermain sesuai N04. Jangan menggantinya dengan klaim harga live, harga resmi bursa, rekomendasi investasi, atau data pasar nyata yang tidak tersedia.
9. Gunakan data domain Java yang sebenarnya. Jangan membuat saldo, laba/rugi, durasi, harga fill, peringkat, status order, atau indikator aktif palsu demi tampilan.
10. Seluruh instruksi keselamatan data proyek tetap berlaku: simpanan pemain dilindungi, tidak reset/clean, tidak push/publish, tidak mengubah credentials/pengaturan global, dan tidak melakukan inspeksi ADB atau instalasi perangkat baru tanpa mandat terpisah.

### 1.2 Cara membaca keputusan dan usulan

- **WAJIB:** langsung berasal dari anotasi, jawaban tempo terbaru, atau aturan proyek yang masih berlaku.
- **REKOMENDASI IMPLEMENTASI:** cara paling kecil dan masuk akal untuk memenuhi kebutuhan; agen boleh memilih implementasi setara setelah menelusuri source dan mencatat alasannya.
- **ASUMSI TERBATAS:** detail yang belum disebutkan pemilik. Gunakan default yang dinyatakan dalam dokumen dan catat; jangan mengubah tarif, hadiah, formula harga, durasi utang, atau aturan ekonomi lain secara diam-diam.
- **PERLU KEPUTUSAN:** digunakan hanya jika penelusuran menemukan konflik nyata yang berdampak besar. Kerjakan bagian independen terlebih dahulu. Jangan memakai label ini untuk menunda keputusan kecil yang sudah jelas.

### 1.3 Penyimpanan dan koordinasi agen

Pemilik menyatakan agen lain sedang mencocokkan dan memverifikasi. Source sempat berubah ketika pemeriksaan berjalan. Sebelum menulis:

1. Buktikan cwd dan Git root produksi. Folder `Game Trading Saham` adalah checkout lama yang berbeda; jangan mengerjakan source di sana.
2. Baca `AGENTS.md`, keputusan aktif `DESIGN.md`, `Work/STATUS.json`, serta `Work/Hermes-Handoff-2026-10-20/PHASE-QUALITY-STANDARDS.md`.
3. Catat perubahan lokal dan siapa yang memegang `app.js`, `style.css`, `index.html`, `TradingFacade.java`, `TradingWebBridge.java`, dan file save. **Satu writer untuk satu file bersama.** Delegasi dapat dipakai untuk pemeriksaan read-only atau file independen setelah batasnya jelas.
4. Jangan memerintahkan agen lain melalui chat atau memulai ulang sesi lain tanpa instruksi pemilik. Koordinasikan kepemilikan melalui mekanisme kerja yang memang tersedia dan diizinkan.
5. Rekam SHA-256 save pemain sebelum dan setelah pengujian. Lokasi yang perlu diperiksa: `Artifacts/LocalPreview/preview-save/save_game_v2.json`. Hash yang tercatat pada pemeriksaan sebelumnya adalah `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`; verifikasi ulang, jangan menganggapnya tetap benar tanpa membaca file.
6. Semua skenario uji menggunakan save terisolasi di bawah `Artifacts/`, dengan nama yang jelas. Jangan memakai preview pemain untuk fixture, tes transaksi, manipulasi tanggal, atau reset.
7. Sebelum menyatakan hasil sesuai source, cocokkan hash semua file produksi yang dipakai, kelas Java yang dikompilasi, file web yang dilayani, dan aset di APK. Jika source berubah saat tes berjalan, hasil itu berstatus snapshot lama sampai bagian terdampak diuji ulang.

## 2. Inventaris lengkap anotasi

Nomor berikut merujuk komentar terbaru 1–13. Dua anotasi yang pernah masuk lebih dahulu tetap ditelusuri: permintaan waktu otomatis menjadi prasyarat N01, dan permintaan kartu kepemilikan yang informatif dilanjutkan oleh N10. Keduanya tidak dihitung sebagai anotasi baru ke-14 dan ke-15.

| ID | Target dari anotasi | Permintaan pemilik | Hasil yang harus terlihat |
|---|---|---|---|
| N01 | `time#clock-display` | “buat berjalan lebih normal, waktunya bergerak pelan jangan skip skip, apakah kamu paham maksudku?” | Jam terasa mengalir dan tidak melompat saat refresh; laju tunggal 24x sesuai keputusan terbaru. |
| N02 | Bagian arah/efek berita di `#home-news-list` | “tambhakan waktu yang sesuai dengan in real life, misal 1 jam -2 jam efeknya akan berlangsung di dunia nyata” | Durasi dan sisa pengaruh berita mengikuti waktu nyata, berlaku juga setelah aplikasi ditutup. |
| N03 | “TERKUAT DI DAFTAR” pada `#market-highlights` | “mengapa hanya ada 1 terkuat dan terelamahnya, coba buat minimal 3” | Tiga penguat dan tiga pelemah jika jumlah data yang memenuhi kategori cukup; aturan kekurangan data jelas. |
| N04 | “Model simulasi game (10 anggota)” pada indeks | “hapus semua kata yang memiliki label simulasi atau kalimat apa aja yang intinya menjelaskan bahwa ini hanya simulasi” | Alur bermain bebas label/disclaimer simulasi dan jargon internal yang tidak membantu tindakan pengguna. |
| N05 | “Tabel OHLCV Alternatif” | “hilangkan ini, gada gunanya” | Tombol serta tabel alternatif tersebut tidak muncul di layar; grafik tetap berfungsi dan aksesibel. |
| N06 | “Sumber data: Matched trades domain engine internal / Timeframe: D1” | “hilangkan ini” | Blok teknis sumber dan timeframe duplikat hilang; pilihan rentang grafik tetap tersedia. |
| N07 | `select#order-type` | “pada bagian jenis order masih membingungkan, buat lebh mudah dalam bentuk ui agar user langsung memahminya, jangan dibat tulisantutorial atau apapu tu” | Jenis order dipilih lewat UI yang jelas, field mengikuti pilihan, tanpa tutorial panjang. |
| N08 | Halaman “Status order” | “terlalu banyak infromasi yang tdak dibutuhkan oleh user, ringkas lag, tampilkan yang dapat berguna untuk user saja” | Ringkasan hasil transaksi berfokus pada status, saham, jumlah, nilai, dan tindakan berikutnya. |
| N09 | Halaman “Status order” | “tambahkan juga notifikasi misal ketika order gagal, market tutup, atau kondisi lainya” | Pemberitahuan yang tepat waktu, tidak berulang, dan sesuai outcome domain. |
| N10 | “Kepemilikan saham” | “buat lebih enak dilihat ui nya, fokus tampilkan informasi yanng dapat membantu user” | Daftar posisi ringkas dengan identitas, jumlah, nilai, harga rata-rata, dan laba/rugi yang benar. |
| N11 | `details#asset-tools > summary` | “jika ini memang sebuah fitur, aku kurang suka kalo malah dibuat dopdown, coba dibuat seperti fitur di beranda, karena kalo seperti ini jatuhnya seperti fitur tutorial atau disclaimer, jadi user mengira tidak ada yang bisa di lakukan ketika melihat ini” | Launcher fitur Portofolio terlihat langsung sebagai tombol/kartu tindakan, bukan dropdown tersembunyi. |
| N12 | Area Bisnis / Usaha Saya / Koleksi / Kelola Dana | “pada bagian ini juga, jika meman ada sebuah fitur, bedakan uinya dari sebuah teks atau papan kotak yang tidak bisa di interaksi” | Bagian informasi dan tindakan di Bisnis memiliki bentuk yang jelas berbeda dan seluruh fitur dapat ditemukan. |
| N13 | Area Lainnya & Riset | “pada bagian ini juga jika memang fitur itu bisa digunakan dan interaktf, bedakan bentuk ui nya agar user memahaminya” | Menu fitur Lainnya, riset, dan progres jelas dapat dipakai; state terkunci tidak menyerupai tombol mati. |

## 3. Dasar source dan temuan awal

Semua path pada tabel ini relatif terhadap root produksi di bagian awal. Nomor baris merupakan penunjuk pemeriksaan, bukan kontrak permanen; cari nama fungsi lagi setelah source berubah.

| Area | Source yang diperiksa | Temuan dan implikasi |
|---|---|---|
| Jam layar | `android/app/src/main/assets/www/app.js`: `renderFlowingClock`, `refreshSnapshot`, interval 250 ms dan sinkronisasi 1 detik | Tampilan menginterpolasi dari timestamp dan anchor. Laju tinggi membuat tampilan detik pada interval 250 ms meloncat besar meski domain benar. Perbaikan harus menangani frekuensi render, anchor, dan representasi jam. |
| Jam dunia | `android/app/src/main/java/com/tradingglobal3d/core/clock/GameClock.java`, `TradingFacade.java`: `syncClock`, `advanceRealMs`, `onAppLifecycleChanged`, `resumeWorld`, `advanceWorldSeconds` | Fondasi otomatis dan perhitungan waktu berlalu sudah ada dalam perubahan lokal. Pertahankan satu pemilik waktu di Java dan uji semua jalur masuk kembali. |
| Jembatan waktu | `android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java`: `syncClock`, `onAppLifecycleChanged`, `loadGame` | UI sudah dapat meminta sinkronisasi domain. Hindari membuat penghitung ekonomi kedua di JS. |
| Berita | `core/research/NewsArticle.java`, `ResearchService.java`, `core/npc/MarketEngine.java` | `NewsArticle.isHorizonActive` memakai `publishedAt + horizonDays × 86.400.000` dalam waktu game. `ResearchService` menggunakan status horizon untuk dampak, dan `MarketEngine` memakai sentimen ketika membuat aktivitas NPC. N02 membutuhkan perubahan domain, bukan label saja. |
| Penguat/pelemah | `www/app.js`: `renderStockList` dan `updateMarketQuotes`; `#market-highlights` | Renderer yang diperiksa mengambil elemen pertama dan terakhir dari daftar terurut. Telusuri updater juga agar perbaikan tiga item tidak kembali menjadi satu saat refresh. |
| Label teknis | `www/index.html`, `www/app.js`, DTO di `TradingWebBridge.java` | Ada “Indeks simulasi game”, “Harga simulasi”, label riset, disclaimer aturan, dan teks sumber dari Java. Mengganti satu string di HTML tidak menuntaskan N04. |
| Grafik | `www/app.js`: `openInstrumentDetail`, fungsi lokal `renderChart` | Tombol tabel, state `chartTableOpen`, DOM `#chart-table`, pemulihan scroll/fokus, dan `provNotice` dibuat di fungsi yang sama. Hapus dependency yang benar-benar tidak dipakai tanpa menghapus data OHLCV. |
| Jenis order | `www/index.html`: `#order-type`; `www/app.js`: `openOrderTicket`, `updateOrderEstimate`, listener dan submit/review | Dropdown ikut menentukan jenis, field harga/pemicu, dan payload. Perubahan visual wajib mempertahankan enum dan validasi domain. |
| Status order | `www/app.js`: `showOrderReceipt`, `updateReceiptLive`; `TradingWebBridge.getOrderStatus` | Halaman menampilkan ID akun dan pasangan UUID permintaan/order. DTO status saat diperiksa memiliki jumlah dan status, tetapi belum menampilkan harga fill rata-rata, biaya aktual, atau nilai kas sebagai field tersendiri. Data itu perlu ditelusuri sebelum UI baru menjanjikannya. |
| Pemberitahuan | `www/app.js`: `showGameToast`, `activeToastKeys`; `www/style.css`: `.game-toast`; `MainActivity.java`: notifikasi yang sudah ada | Toast memakai dedupe 5 detik dan `replaceChildren`, sehingga notifikasi baru dapat menggantikan sebelumnya. Gunakan fondasi ini, tetapi perbaiki outcome, antrean prioritas, dan durasi untuk pesan penting. Notifikasi Android saat aplikasi ditutup adalah cakupan terpisah. |
| Kepemilikan | `www/app.js`: `loadAsetView`, `syncPortfolioLive`, `openPositionDetailModal`; `TradingWebBridge.getPortfolio` | Posisi tampil dalam satu baris teks. DTO sudah membawa `instrumentId`, jumlah, mata uang, biaya rata-rata, harga sekarang, nilai, unrealized P/L dan realized P/L gross. Persentase, lot, saham tersedia, dan bobot harus dihitung dari data numerik yang sah atau ditambahkan ke DTO. |
| Launcher Portofolio | `www/app.js`: `loadAsetView` | Banyak tombol dipindahkan ke `details#asset-tools` menggunakan pemilihan elemen berdasarkan tag dan awalan teks. Ganti pengelompokan berbasis string tersebut dengan daftar fitur yang eksplisit dan kecil. |
| Bisnis | `www/app.js`: `loadFirmaView`, `renderFirmaUsahaSection`, `renderFirmaDanaSection`, renderer koleksi | Terdapat subtab dan kartu berisi teks serta tindakan. Perbaikan mencakup state kosong, perusahaan yang sudah ada, modal tindakan, serta dana/mandat, bukan tiga tombol tab saja. |
| Lainnya | `www/app.js`: `loadRisetView`, `appendPrestigeSection` | Hub sudah menggunakan beberapa tombol, tetapi bentuk dan pengelompokan bercampur dengan panel informasi. Seluruh launcher, paket riset, dan Era Baru harus diinventarisasi. |

### 3.1 Bukti terdahulu yang boleh dipakai sebagai referensi terbatas

- `Artifacts/Automatic-World-2026-10-23/verified-jvm/junit-latest.log` pada pemeriksaan dokumen berisi **OK (90 tests)**. Bukti itu berasal dari snapshot pekerjaan waktu sebelumnya, bukan sertifikasi source yang terus berubah atau seluruh 13 anotasi.
- `Artifacts/Automatic-World-2026-10-23/browser-final/results.json` mencatat automatic startup, penolakan pause lama, percobaan page exit/reentry, serta 16 sampel layout. Bukti ini adalah browser dengan Java host, bukan Android fisik.
- `Work/STATUS.json` mempunyai klaim checkpoint dan hasil masterplan lama. Cocokkan dengan source, log, dan artefak terbaru; jangan meneruskan label selesai tanpa verifikasi.
- Source sempat gagal dikompilasi selama agen lain menambah ekspor state. Jangan menerapkan perbaikan lama berdasarkan pesan error yang mungkin sudah kedaluwarsa; kompilasi ulang snapshot stabil terlebih dahulu.

### 3.2 Batas bukti REA

Hasil existing di `Artifacts/REA/analysis/feature-string-candidates.json` memuat teks “Your portfolio keeps moving. Check back in anytime!”. Pemeriksaan biner sebelumnya menemukan token `ticks_per_day`, `get:daysElapsed`, `DateTime.now`, `Timer.periodic`, dan `didChangeAppLifecycleState` dalam `libapp.so` pada split APK ARM64. Ini menunjukkan keberadaan teks/token terkait waktu, **belum membuktikan algoritme offline atau layanan backend yang dipakai**.

`Artifacts/REA/evidence/dart-aot.json` membedakan observasi string dari source Dart yang benar-benar dipulihkan. Source Java yang tersedia pada `android-engine-method.json` berkaitan dengan konfigurasi Flutter, iklan, dan MethodChannel; bukan implementasi jam ekonomi. Laporan akuisisi juga menyatakan lokasi pemrosesan pasar lokal versus backend belum dipastikan.

Agen boleh memakai bukti tersebut sebagai konteks perilaku, tetapi tidak boleh menulis bahwa mekanisme game contoh sudah berhasil disalin/dipulihkan. Tidak perlu memulai akuisisi APK, ADB, atau reverse engineering baru untuk menuntaskan 13 anotasi ini.

## 4. Prinsip desain bersama dan kontrak data

### 4.1 Informasi versus tindakan

Gunakan bahasa visual bersama untuk N07, N10, N11, N12, dan N13:

| Jenis elemen | Bentuk yang disarankan | Perilaku |
|---|---|---|
| Ringkasan informasi | Panel tenang, label dan nilai, tanpa chevron atau efek tekan | Tidak menerima klik/fokus jika memang tidak punya tindakan. |
| Tujuan fitur | Tombol/kartu dengan judul tindakan, ikon relevan, batas jelas, dan penanda buka | Seluruh area kartu dapat diklik; memiliki pressed/focus; membuka satu tujuan. |
| Pilihan kategori | Segmented control/radio cards dengan state terpilih | Pilihan tetap terlihat dan memengaruhi field/isi terkait. |
| Aksi ekonomi | Tombol kata kerja yang spesifik | Masuk tahap tinjau/konfirmasi yang sudah ada; tidak mengeksekusi diam-diam saat membuka kartu. |
| Fitur terkunci | State terkunci dengan satu alasan dan syarat progres | Jika dapat dibuka untuk melihat syarat, tampilkan “Lihat syarat”; jika tidak dapat digunakan, jangan membuatnya tampak aktif. |
| Proses tertunda | Status singkat dan feedback langsung | Cegah double submit tanpa menghilangkan riwayat atau menganggap proses gagal sebelum domain menjawab. |

Perbarui token dan arah aktif DESIGN.md ke tema terang sesuai bagian 0 dan N16 setelah persetujuan contoh N20; pakai font lokal yang masih cocok. Antislop core/UI/human/layoutmobile/copywriting mode during menjadi filter. Desain trading tenang dan progres ekspresif mempunyai alasan per konteks; tidak kembali ke glow/ungu seragam.

### 4.2 Data domain dan angka uang

- Java tetap otoritas saldo, biaya, order, waktu, dan save. JS menyajikan serta menerima input.
- Nilai uang gunakan objek/angka desimal yang sudah dipakai domain. Jangan mengubah precision uang menjadi `double` untuk akuntansi atau menghitung ulang biaya aktual di UI.
- Jangan mem-parse string rupiah terformat untuk memperoleh angka mentah. Jika kontrak belum membawa nilai numerik yang diperlukan, tambahkan field DTO sekecil mungkin dengan makna dan mata uang jelas.
- Pertahankan isolasi akun pribadi dan klien pada semua kartu, pemilihan akun, order, serta notifikasi.
- Nama emiten dapat diambil dari katalog berdasarkan ID. Jangan membuang prefix bursa dari identitas internal karena ticker dapat bertabrakan antar pasar; cukup rapikan label yang terlihat.
- Data tidak tersedia ditampilkan sebagai “—” atau keadaan kosong yang jelas, bukan nol palsu. Harga fill tidak boleh diganti harga terakhir; laba bersih tidak boleh memakai nilai sebelum biaya tanpa label yang tepat.
- Semua kontrol baru harus memakai helper/pola existing bila memungkinkan. Jangan membuat framework, service generik, registry rumit, dependency baru, atau event bus hanya untuk perubahan ini.

## 5. N01: Tempo tunggal satu hari game per satu jam nyata

### 5.1 Kontrak dan solusi

Keputusan terbaru menggantikan tempo aktif lama. Satu hari kalender game adalah 86.400 detik, bukan hanya jam bursa. Setiap satu detik nyata memajukan 24 detik game, pada foreground/background/closed. Dunia auto-start, hari berganti tanpa jeda pemain, tidak ada tombol speed/skip/play/pause. Bursa tetap memiliki sesi yang sah; pasar tutup tidak menghentikan kalender dunia.

Java tetap satu pemilik clock. Telusuri GameClock, syncClock, advanceRealMs, resume/load, lifecycle, MainActivity, bridge, presenter JS, speed/skip lama dan seluruh callernya. Pakai aturan elapsed yang sama di startup, resume, mode switch, dan catchup; jangan memperlambat lewat CSS atau menambah penghitung ekonomi JS.

Saat migrasi, pertahankan timestamp dunia dan seluruh saldo/posisi. Simpan versi aturan/anchor minimum bila dibutuhkan. Selesaikan elapsed yang belum direkonsiliasi menurut kebijakan transisi yang dicatat, lalu anchor baru memakai 24x; jangan mengubah elapsed yang sama dua kali, mengubah tanggal secara retroaktif, atau menghitung seluruh masa lalu dengan multiplier baru. Kebijakan fallback untuk save lama yang tidak punya informasi cukup harus eksplisit dan diuji; jangan mengarang timestamp historis.

Presenter memakai timestamp domain dan anchor monoton untuk interpolation, tidak per-frame meminta Java. Utamakan HH:mm; detail detik hanya jika berguna. Pada 24x, satu detik game tetap bukan satu detik nyata. Refresh biasa tidak membuat jam mundur. Hidden menghentikan pekerjaan visual, saat kembali sinkronisasi dahulu. Reduced motion menonaktifkan animasi digit tanpa mengubah waktu.

Bunga, gaji, dividen, misi, liga, jatuh tempo dan sesi tetap mengikuti kalender/rumus yang sah. Durasi nyata sampai event tersebut secara alami berubah karena tempo baru; jangan diam-diam mengubah tarif atau hadiah. Berita 60/120 menit nyata mempunyai kontrak tersendiri di N02.

### 5.2 Tugas atomik (identitas lama dipertahankan)

- [ ] N01-01 Telusuri seluruh sumber tempo, clock writer, speed/skip, lifecycle dan presenter; catat semua caller.
- [ ] N01-02 Rekam baseline laju, timestamp, save schema, anchor dan state saat aktif/hidden/restart sebelum migrasi.
- [ ] N01-03 Terapkan laju tunggal 24x di pemilik waktu Java dan presenter yang sesuai; hapus akses kontrol percepat/skip/pause pemain.
- [ ] N01-04 Rekonsiliasi elapsed, fraksi dan migrasi anchor secara idempotent pada startup/load/mode switch/lifecycle.
- [ ] N01-05 Pertahankan auto-start, rollover, save/recovery dan batas dataset; sesuaikan dokumen serta fixture lama yang kontraknya digantikan.
- [ ] N01-06 Jalankan controlled-clock, partition/lifecycle/restart/migrasi dan rekaman visual; buktikan ekonomi kalender tetap benar.

### 5.3 Penerimaan dan bukti

- 300.000 ms nyata menghasilkan 7.200 detik game; 3.600.000 ms menghasilkan 86.400 detik game; 7.200.000 ms menghasilkan dua hari game. Uji aktif, hidden, tertutup/restart dan campuran dengan total elapsed sama.
- Interval utuh sama dengan interval terpartisi, termasuk remainder; callback ganda dan retry tidak menambah waktu dua kali. Uji 1.900 ms lalu hidden pada 2.000 ms, clock mundur, crash tanpa callback, save failure, dan offline panjang.
- Migrasi tidak mereset waktu, progres atau posisi. Tidak ada kompensasi uang/hadiah tanpa aturan domain. Clock bergerak otomatis tanpa kontrol speed/skip; API lama yang mungkin terpanggil UI tidak boleh diam-diam mempercepat dunia.
- Session close, rollover misi/liga, jatuh tempo, pembayaran dan corporate actions tepat sekali pada kalender baru. Replay tetap anti-future-leak dan berhenti pada batas dataset yang sah.
- Lampirkan rekaman jam normal/reduced motion, source hash, angka domain, snapshot save sebelum/sesudah, serta sequence kritis. Update tes lama dengan perilaku baru; jangan hanya menghapus assertion.

## 6. N02: Efek berita berdurasi satu sampai dua jam nyata

### 6.1 Masalah dan kontrak baru

Horizon yang diperiksa masih menggunakan hari game. Mengubah teks “hari permainan” menjadi “jam nyata” tanpa mengubah perhitungan dampak akan menyesatkan dan tidak memenuhi anotasi.

**WAJIB:** countdown yang terlihat dan batas aktif yang dipakai engine berasal dari deadline waktu nyata yang sama. Menutup aplikasi, mengubah kecepatan aktif, memajukan hari, atau masuk replay tidak boleh memperpanjang deadline tersebut.

### 6.2 Solusi domain yang direkomendasikan

1. Tambahkan kontrak waktu nyata pada event berita yang relevan, misalnya `publishedRealAt`, `effectStartsRealAt`, `effectDurationRealMs`, dan `effectEndsRealAt`. Nama final boleh mengikuti pola repo; maknanya harus tetap eksplisit.
2. Tetapkan deadline sekali saat event benar-benar terbit/aktif, bukan setiap kali kartu dirender atau dibaca. Membaca berita tidak memulai ulang efek.
3. Gunakan clock waktu nyata yang dapat diinjeksi dan dipakai bersama secara konsisten. Countdown JS boleh menginterpolasi sisa waktu, tetapi Java menentukan apakah dampak masih berlaku.
4. **ASUMSI TERBATAS untuk durasi:** dukung dua durasi nyata, 60 dan 120 menit. Jika katalog belum menentukan durasi, gunakan 60 menit sebagai default tercatat; event yang memang diberi 120 menit mengikuti durasi tersebut. Ini bukan izin membuat reward/rentang harga baru atau menetapkan semua berita selalu dua jam.
5. Aktif pada interval `startsAt <= now < endsAt`. Tepat pada deadline, efek berhenti; riwayat berita tetap tersedia dan diberi status berakhir.
6. Pada catchup offline, evaluasi kejadian berdasarkan posisi waktu nyata di dalam interval yang dilewati. Jangan memakai satu `now` saat login untuk menentukan seluruh pengaruh selama dua jam sebelumnya: itu bisa menghapus efek yang sebelumnya aktif atau menerapkannya setelah berakhir.
7. Jika dunia berjalan dengan beberapa laju, petakan elapsed nyata ke batas event dunia dalam segmen yang sesuai. Potong segmen pada publikasi/deadline berita, rollover/cutoff, dan transisi mode yang memang relevan. Pakai ulang scheduler/stepper existing; jangan membuat engine paralel.
8. Aktivitas yang terjadi sebelum deadline tetap menjadi transaksi/riwayat yang sah. Saat efek berakhir, hentikan dorongan sentimen berikutnya; jangan mengembalikan harga secara paksa atau membatalkan transaksi yang sudah terisi.
9. Telusuri pemakai horizon lain seperti konsensus, riset terminal, booster, atau berita replay. Jangan mengganti semua `horizonDays` di repo secara global. Bedakan horizon analisis, masa langganan, dan durasi pengaruh harga.
10. Mode replay tetap mengikuti batas dataset dan anti-future-leak. Untuk efek yang tidak berlaku pada replay, definisikan state yang jelas; jangan mengubah bar historis untuk memaksakan pengaruh berita internal.

### 6.3 Tampilan yang diinginkan

- Di kartu: kategori/sektor, arah dampak, lalu **“Aktif · sisa 1j 24m”** atau **“Berakhir”**.
- Di rincian: waktu mulai/berakhir nyata dalam WIB dan durasi total yang ringkas. Jangan mencampur timestamp game dengan waktu nyata tanpa penamaan.
- Persentase rentang tetap berlabel **perkiraan dampak**, bukan hasil pasti. Masa aktif dua jam tidak berarti harga pasti naik sekian persen.
- Saat belum terbit, gunakan countdown rilis yang sesuai kontraknya dan jangan bocorkan isi terlarang.
- Tidak perlu animasi atau pembacaan screen reader tiap detik. Pembaruan per menit memadai untuk kartu satu–dua jam; status batas harus tetap diperbarui tepat.

### 6.4 Tugas atomik

- [ ] N02-01 Petakan seluruh caller `isHorizonActive`, sentimen, daftar berita, countdown dan export/restore state.
- [ ] N02-02 Tentukan kontrak deadline dan durasi per event; dokumentasikan default 60 menit bila diperlukan.
- [ ] N02-03 Simpan deadline persisten, termasuk migrasi save lama, reset game baru, dan recovery backup.
- [ ] N02-04 Hubungkan deadline ke `ResearchService` dan konsumennya di `MarketEngine`, bukan hanya DTO/UI.
- [ ] N02-05 Perbaiki stepper/catchup agar efek pada interval offline dihitung pada segmen yang tepat.
- [ ] N02-06 Samakan badge, detail, countdown, dan state berakhir di Beranda, Riset, dan detail emiten.
- [ ] N02-07 Tambahkan tes publikasi, batas akhir, restart, offline silang deadline, beberapa berita, clock mundur, dan mode replay.

### 6.5 Kasus penerimaan wajib

Event berdurasi 60 menit dimulai pukul 14.00 WIB nyata: pukul 14.30 sisa 30 menit, pukul 15.00 berakhir. Event 120 menit: pukul 15.00 masih sisa 60 menit, pukul 16.00 berakhir. Nilai tersebut mengikuti deadline nyata dan tidak berubah karena rendering atau perpindahan foreground/background pada laju 24x.

Jika aplikasi ditutup pukul 14.15 dan dibuka 15.30, engine harus merekonsiliasi aktivitas 14.15–15.00 saat efek aktif dan 15.00–15.30 saat tidak aktif. Membuka ulang lagi pada waktu yang sama tidak mengulang efek/hasil.

Uji interval panjang sekaligus dan interval kecil terpartisi pada input yang sama. Tanggal perangkat mundur tidak memperpanjang efek atau menggandakan kejadian. Jelaskan batas proteksi clock lokal; jangan mengklaim otoritas waktu server yang tidak ada.

Migrasi: jangan memperlakukan save lama seolah memiliki timestamp nyata historis yang tidak pernah disimpan. Rekomendasi untuk event lama yang sudah aktif adalah menetapkan satu masa transisi sekali pada migrasi dan menyimpannya; jangan mengulang masa transisi pada setiap load. Catat kebijakan itu secara eksplisit dan verifikasi tidak memberi kredit/keuntungan retroaktif yang tidak didukung data.

## 7. N03: Tiga penguat dan tiga pelemah

### 7.1 Solusi

Ganti dua highlight tunggal menjadi dua kelompok ringkas: **“3 Penguat”** dan **“3 Pelemah”**. Setiap baris berisi peringkat, ticker, perubahan persen, serta harga bila tidak membuat layar sempit. Baris merupakan tombol yang membuka detail emiten.

Gunakan basis daftar yang konsisten dengan filter pasar aktif. Jika yang ditampilkan berasal dari seluruh pasar alih-alih hasil filter, beri label “Seluruh pasar”; jangan menulis “di daftar” ketika sumbernya berbeda. Rekomendasi default: gunakan hasil filter yang sedang terlihat, termasuk watchlist, lalu kelompokkan.

Aturan data:

- Penguat hanya nilai perubahan > 0, urut menurun. Pelemah hanya < 0, urut menaik dari yang paling turun.
- Ambil tiga masing-masing bila tersedia. Jika hanya dua emiten menguat, tampilkan dua dan keterangan ringkas; jangan memasukkan saham turun sebagai penguat hanya untuk memenuhi angka tiga.
- Nilai nol netral dan tidak mengisi dua kategori sekaligus. Semua nol menghasilkan state “Belum ada penguat/pelemah”.
- Angka tidak valid/NaN atau emiten tanpa baseline perubahan tidak ikut ranking; jangan dipaksa menjadi 0.
- Tie diputus dengan aturan stabil, misalnya ticker lalu ID bursa. Instrumen tidak boleh muncul ganda karena ticker sama.
- Jangan memberi label paling kuat berdasarkan pembulatan persen yang kehilangan urutan; urutkan nilai numerik sebelum format.
- Pada ponsel, dua kelompok dapat bertumpuk; pada ruang yang cukup gunakan dua kolom. Tidak mengandalkan carousel tersembunyi untuk tiga item utama.

### 7.2 Tugas atomik

- [ ] N03-01 Samakan sumber ranking dengan pencarian, bursa, sektor, active-only dan watchlist.
- [ ] N03-02 Perbaiki renderer awal dan updater realtime agar keduanya memakai aturan yang sama.
- [ ] N03-03 Tambahkan navigasi item ke detail emiten, focus state, serta label perubahan bertanda.
- [ ] N03-04 Uji tiga/kurang dari tiga, semua nol, semua positif, semua negatif, seri, nilai invalid dan ticker lintas bursa.
- [ ] N03-05 Verifikasi refresh tidak menggandakan item, mengacak fokus, atau mengubah filter pengguna.

Penerimaan: dataset cukup menghasilkan enam item dengan ID unik dan urutan benar; dataset tidak cukup menampilkan jumlah yang benar dengan state jujur. Bukti berupa perbandingan input terkontrol, hasil ranking, klik detail, dan screenshot mobile normal/200%.

## 8. N04: Bersihkan label simulasi dan jargon internal dari UI bermain

### 8.1 Cakupan yang wajib disapu

Jangan membatasi pencarian pada kata `simulasi` dalam `index.html`. Inventarisasi HTML, JS template, DTO Java, nama mode yang disajikan, feed berita, chart, modal, status order, bantuan, fitur multi-aset, FX, pinjaman, risiko, dan state error/empty yang terlihat pengguna.

Contoh yang teramati: “Indeks simulasi game”, “Model simulasi game (10 anggota)”, “Harga simulasi”, “Skenario simulasi internal game”, “Bridge riwayat harga domain belum tersedia”, dan “Portofolio domain belum tersedia pada pratinjau ini”.

### 8.2 Penggantian berbasis makna

| Teks/jenis lama | Bentuk baru yang disarankan | Data yang harus dipertahankan |
|---|---|---|
| Indeks simulasi game | Indeks pasar | Nama indeks dan nilai yang benar. |
| Model simulasi game (10 anggota) | 10 emiten | Jumlah anggota dari domain, tanpa klaim indeks resmi. |
| Harga simulasi | Harga terakhir | Waktu harga dan mata uang bila dibutuhkan untuk keputusan. |
| Aturan simulasi pinjaman | Ketentuan pinjaman | Bunga, provisi, tenor, agunan dan konsekuensi tetap terlihat. |
| Penukaran memakai kurs dan biaya simulasi | Tampilkan kurs, biaya dan jumlah diterima | Angka quote aktual dan batas berlaku. |
| Sumber internal/domain/engine | Hilangkan dari alur utama | Provenance tetap tersimpan dalam data dan bukti pengujian. |
| Bridge/domain gagal | Harga/portofolio belum dapat dimuat | Pesan aksi berikutnya dan data terakhir yang valid. |

Hilangkan “mode” dari status order bila tidak memengaruhi tindakan pengguna. Untuk pilihan permainan seperti replay, nama “Replay historis” tetap dibutuhkan karena membedakan alur sebenarnya; jangan membuat replay tampak seperti pasar hari ini.

Label QA host pada preview adalah konteks alat pengujian, bukan bagian permainan Android. Pindahkan ke bingkai/metadata alat preview bila perlu membersihkan UI bermain, sambil tetap menyatakan cakupan host dalam bukti. Jangan mengubah `nativeHost=false` menjadi `true` atau menyatakan screenshot browser berasal dari Android.

Tidak perlu menghapus istilah teknis dari nama class, log, source comment, dokumen internal, atau test fixture yang tidak dilihat pemain. Jangan melakukan replace global yang merusak enum, identity source, legal provenance dataset, atau pesan validasi finansial.

### 8.3 Tugas atomik

- [ ] N04-01 Buat inventaris string yang benar-benar terlihat pada semua route/state terkait, termasuk yang berasal dari Java.
- [ ] N04-02 Tentukan penghapusan/penggantian per konteks; pertahankan tarif, risiko, syarat, dan status yang membantu keputusan.
- [ ] N04-03 Bersihkan heading indeks, caption, detail instrumen, berita, FX, pinjaman, margin, multi-aset, order dan empty/error.
- [ ] N04-04 Pisahkan identitas QA dari produk tanpa memalsukan platform atau sumber data.
- [ ] N04-05 Uji route serta state dinamis setelah refresh; pencarian string statis menjadi pemeriksaan tambahan, bukan satu-satunya bukti.

Penerimaan: tidak ada label “hanya simulasi” atau sinonim disclaimer tersebut pada UI bermain. Tidak ada klaim “live/resmi/real-time bursa” yang dibuat sebagai pengganti. Data dan risiko yang penting tetap terbaca.

## 9. N05: Hapus tombol dan tabel OHLCV alternatif

### 9.1 Solusi

Hapus tombol “Tabel OHLCV Alternatif / Tutup Tabel OHLCV” beserta tabel alternatif yang dimaksud dari detail emiten. Jangan sekadar menyembunyikan tombol sementara tabel masih menerima fokus atau dapat dibuka melalui state lama.

Telusuri `chartTableOpen`, `#chart-table`, `toggle-table`, pemulihan scroll tabel, dataset fokus, serta selector pengujian. Hapus cabang yang benar-benar tidak dipakai. `getChartData`, array bar OHLCV, volume, timeframe, dan data riwayat tetap diperlukan untuk grafik, jadi tidak ikut dihapus.

Aksesibilitas grafik tetap wajib. Pilih implementasi kecil: beri grafik nama/ringkasan yang tepat dan sediakan ringkasan candle terpilih melalui fokus/keyboard atau pembacaan nilai yang sudah ada. Jangan membuat “tabel alternatif baru” yang diam-diam mengembalikan fitur yang ditolak pemilik.

### 9.2 Tugas atomik

- [ ] N05-01 Telusuri seluruh state, node, event, CSS dan tes tabel alternatif.
- [ ] N05-02 Hapus UI dan cleanup dependency yang tidak memiliki pemakai lain.
- [ ] N05-03 Pertahankan render candle/line/volume, data OHLCV domain, pemilihan rentang, serta focus control lain.
- [ ] N05-04 Verifikasi grafik tetap dapat dipahami tanpa warna saja, dan nilai penting dapat diakses tanpa hover mouse.
- [ ] N05-05 Uji tidak ada tombol, tabel tersembunyi, tab stop kosong, atau error saat refresh dan pergantian timeframe.

Penerimaan: fitur yang ditandai benar-benar hilang, grafik tidak kehilangan data, dan tidak ada caller mati atau regresi aksesibilitas.

## 10. N06: Hapus blok sumber teknis di bawah grafik

### 10.1 Solusi

Hapus blok `provNotice` yang menampilkan “Sumber data: Matched trades domain engine internal” dan “Timeframe: D1”. Keduanya merupakan satu blok teknis yang dipilih pemilik.

Pilihan rentang/timeframe grafik tetap ada karena merupakan kontrol, bukan disclaimer. Label kontrol boleh menggunakan bahasa pengguna seperti “Harian” bila memang sesuai arti D1. Jangan menyebut D1 sebagai “1 hari terakhir” jika sebenarnya resolusi bar harian dengan rentang beberapa hari.

Sumber data tetap ada di domain/metadata/laporan audit. Hapus presentasinya pada lokasi yang diminta; jangan membuang field yang dipakai validator atau membuat chart tak bisa ditelusuri secara internal. Saat data benar-benar gagal/kurang, tetap tampilkan error singkat yang bisa ditindaklanjuti.

### 10.2 Tugas atomik

- [ ] N06-01 Telusuri penciptaan `provNotice` dan pemakainya.
- [ ] N06-02 Hapus blok sumber/frame duplikat, rapikan jarak bawah grafik tanpa ruang kosong bekas elemen.
- [ ] N06-03 Pertahankan filter rentang dan status error/stale yang relevan.
- [ ] N06-04 Uji semua jenis grafik/timeframe serta riwayat kosong agar notice tidak muncul kembali dari fallback.

Penerimaan: dua teks yang ditandai tidak muncul, kontrol rentang tetap bekerja, dan hasil OHLCV sama sebelum/sesudah perubahan presentasi.

## 11. N07: Pemilihan jenis order melalui antarmuka yang mudah dipahami

### 11.1 Bentuk interaksi yang direkomendasikan

Ganti dropdown tunggal dengan empat pilihan berbentuk **radio cards** atau segmented control yang tetap terlihat. Dua kolom pada layar cukup; satu kolom bila teks 200% atau ruang tidak cukup. Satu pilihan hanya memilih jenis order, tidak langsung mengirim transaksi.

| Label utama pengguna | Enum domain | Field/kondisi yang ditampilkan |
|---|---|---|
| Market | `MARKET` | Jumlah, referensi harga beli/jual yang tersedia, estimasi biaya; field harga limit tidak aktif. |
| Limit | `LIMIT` | “Harga beli maksimal” atau “Harga jual minimal”, jumlah, estimasi dan status menunggu bila belum match. |
| Stop Loss | `STOP_LOSS` | Harga pemicu, jumlah, arah kondisi sesuai sisi; tidak menyebut harga eksekusi terjamin. |
| Take Profit | `TAKE_PROFIT` | Harga pemicu, jumlah, kondisi sesuai sisi; setelah terpancing mengikuti jenis eksekusi yang didukung domain. |

Enum di tabel harus dicocokkan dengan `OrderType` aktual sebelum implementasi; jangan mengirim enum tebakan bila source memakai nama lain. Bentuk visual harus membantu membedakan “sekarang”, “harga target”, dan “pemicu”. Boleh menggunakan satu baris pendek atau label field dinamis untuk kondisi, bukan paragraf tutorial atau dialog belajar tambahan.

### 11.2 Perilaku yang harus tetap benar

1. Sisi Beli/Jual tetap keputusan terpisah. Warna hijau/merah saja tidak menentukan sisi. Saat sisi berganti, label dan validasi pemicu harus ikut berganti tanpa mengganti quantity diam-diam.
2. UI merepresentasikan kemampuan domain. Jika stop/take-profit untuk sisi tertentu tidak didukung, tampilkan alasan singkat dan nonaktifkan pilihan itu; jangan menjanjikan short selling, proteksi, atau tipe eksekusi baru.
3. Menekan jenis order memperbarui field relevan, estimasi, dan tombol tinjau. Field yang tidak relevan tidak ikut payload sebagai harga pemicu/limit lama.
4. `MARKET` tidak menjamin filled atau harga terakhir. Sesi tutup, halt, kekurangan likuiditas, saldo/posisi kurang, dan sektor terkunci tetap diperiksa Java.
5. `LIMIT` dapat mengantre atau terisi sebagian. Stop-loss dan take-profit menggunakan pemicu yang benar; jangan menyatakan order sudah terisi ketika baru dipasang.
6. Pertahankan alur preview → review → confirm, request ID idempotent, isolasi akun klien, dan perhitungan lot per instrumen.
7. Gunakan data akun terpilih untuk tombol 25/50/75/MAX. Source yang diperiksa masih memiliki fallback saldo `50.000.000`, asumsi komisi `0,15%`, dan pembulatan minimum satu lot. Pada jalur yang tersentuh, ganti estimasi/fallback yang dapat memunculkan jumlah tidak mampu dibeli dengan quote domain; jangan mengubah tarif ekonominya.
8. Jika data saldo/harga gagal dimuat, nonaktifkan komitmen dan jelaskan singkat. Jangan mengasumsikan modal awal atau membuat kuantitas minimum satu lot saat saldo nol.

### 11.3 Tugas atomik

- [ ] N07-01 Telusuri seluruh caller `#order-type`, side, limit/trigger, percent buttons, review dan submit; inventaris enum yang benar.
- [ ] N07-02 Implementasikan pilihan visual dengan native radio atau kontrol setara yang mendukung keyboard dan state selected.
- [ ] N07-03 Hubungkan pilihan ke field/payload yang tepat dan pertahankan nilai draft yang masih relevan.
- [ ] N07-04 Samakan estimasi dan MAX dengan akun serta quote Java; hilangkan fallback angka palsu pada jalur tersebut.
- [ ] N07-05 Pertahankan review/confirm, validasi, idempotency dan handling pemicu.
- [ ] N07-06 Perbarui selector tes yang masih `select_option` menjadi aksi radio yang benar, tanpa melemahkan assertion payload/outcome.
- [ ] N07-07 Uji Beli/Jual × empat jenis × IDX/US × akun pribadi/klien yang tersedia, termasuk kasus tidak didukung.

### 11.4 Penerimaan dan bukti

Pengguna dapat melihat jenis yang dipilih, field yang harus diisi, dan langkah “Tinjau beli/jual” tanpa membaca tutorial. Keyboard dapat memilih radio dan mengakses field; focus terlihat. Switching type tidak mengirim transaksi atau memakai harga stale. Payload terverifikasi sesuai enum dan angka yang ditinjau. Tes mencakup saldo nol, quantity invalid, lot US versus IDX, sesi tutup, order tertunda, fill sebagian, pemicu aktif, dan retry request yang sama.

## 12. N08: Ringkas halaman status order

### 12.1 Susunan informasi

Gunakan ringkasan hasil dengan urutan:

1. Status utama: **Terisi penuh / Terisi sebagian / Menunggu harga / Menunggu pemicu / Ditolak / Dibatalkan / Kedaluwarsa**, mengikuti status domain yang benar.
2. Identitas transaksi: **Beli BBCA** atau **Jual BBCA**, nama akun manusiawi bila diperlukan untuk membedakan pribadi dan klien.
3. Jumlah: saham/lot yang sudah terisi dan sisa hanya jika masih relevan.
4. Harga rata-rata terisi, nilai transaksi, biaya, dan total kas keluar/masuk **jika data aktual tersedia**. Pada order menunggu, tampilkan harga limit/pemicu, bukan harga fill buatan.
5. Satu tindakan utama yang masuk akal: “Lihat kepemilikan”, “Lihat order aktif”, “Perbaiki order”, atau “Kembali ke pasar”. “Batalkan sisa” hanya untuk status yang benar-benar bisa dibatalkan.

Hapus baris Mode, kode akun mentah, pasangan UUID, pesan generik berulang, dan “Periksa status” yang redundan bila updater sudah berjalan. ID lengkap tetap berada dalam riwayat/detail teknis transaksi yang memang dibutuhkan untuk penelusuran, bukan memenuhi halaman utama.

### 12.2 Data dan konsistensi

- Telusuri `getOrderStatus`, `Order`, `TradeFill`, ledger, dan receipt yang ada. DTO saat diperiksa belum menjamin harga fill rata-rata, biaya, dan total aktual. Tambahkan nilai dari domain hanya setelah sumbernya jelas.
- Untuk beberapa fill, gunakan harga rata-rata tertimbang berdasarkan quantity fill; jangan memakai rata-rata sederhana atau harga quote terakhir.
- Bedakan jumlah diminta, terisi, dan tersisa. Pembatalan sisa tidak membatalkan fill yang sudah terjadi.
- Setelah restart, `orderDraft` mungkin tidak ada. Halaman harus dapat dibangun dari order/receipt tersimpan, tanpa bergantung pada variabel sementara.
- `updateReceiptLive` harus memperbarui komponen ringkas yang sama, bukan menambahkan ulang baris lama. Tidak mencuri fokus atau melompat scroll saat status berubah.
- Untuk request dengan outcome belum diketahui karena koneksi/bridge error, tampilkan “Status belum dapat dipastikan” dan cek request lama; jangan otomatis mengirim request ID baru.

### 12.3 Tugas atomik

- [ ] N08-01 Tetapkan field utama per status dan field detail; petakan semua status existing.
- [ ] N08-02 Lengkapi DTO harga/nilai/biaya dari receipt/ledger bila dibutuhkan; dokumentasikan gross/net.
- [ ] N08-03 Bangun ulang ringkasan dan tindakan, sembunyikan ID mentah dari default.
- [ ] N08-04 Perbarui updater live dan riwayat agar memakai representasi yang konsisten.
- [ ] N08-05 Uji order fresh, reload, partial fill, batal sisa, expired, rejected dan response belum diketahui.
- [ ] N08-06 Verifikasi pembatalan, retry, dan laporan kas tetap sesuai ledger.

Penerimaan: layar awal dapat menjawab “apa yang terjadi, berapa terisi, berapa nilainya, dan apa yang bisa dilakukan berikutnya” tanpa UUID dan teks mode. Informasi kritis tidak dihapus demi ringkas. Bukti mencocokkan ringkasan dengan fill serta ledger nyata.

## 13. N09: Notifikasi order dan kondisi pasar

### 13.1 Gunakan fondasi existing

Pakai `showGameToast` dan status inline yang sudah ada. Jangan menambah library notifikasi. Toast adalah feedback cepat; error yang memerlukan perbaikan input tetap berada dekat field dan tidak hilang setelah 3,2 detik.

`container.replaceChildren(toast)` dapat menimpa notifikasi penting. Gunakan antrean kecil atau prioritas yang cukup untuk alur ini. Hindari sistem notifikasi generik dengan banyak layer. Dedupe berdasarkan event/request/status yang stabil, bukan hanya lima detik sejak pesan terakhir.

### 13.2 Matriks kondisi wajib

| Kondisi | Pesan/tindakan yang disarankan | Ketentuan |
|---|---|---|
| Bursa tutup ketika hendak kirim | “Pasar sedang tutup” + waktu buka berikutnya jika domain menyediakannya | Jelaskan apakah order ditolak atau mengantre sesuai aturan jenis; jangan menyamakan semua jenis. |
| Sesi berubah menjadi buka/tutup | Status kecil dan satu notifikasi bila relevan | Hanya saat transisi, bukan tiap polling. Sertakan bursa yang benar. |
| Saldo tidak cukup | “Saldo tersedia belum cukup” + angka kurang jika tersedia | Inline di ticket dan feedback singkat; jangan bocorkan akun lain. |
| Saham tersedia tidak cukup | “Saham yang bisa dijual belum cukup” | Hitung reservasi order lain, bukan quantity total saja. |
| Sektor/fitur terkunci | “Terbuka di level …” atau syarat jabatan | Gunakan syarat domain aktual. |
| Order diterima dan mengantre | “Order dipasang” | Jangan memakai “Pembelian berhasil” jika belum fill. |
| Fill sebagian | “Terisi … dari … saham” | Nilai terisi dan sisa cocok dengan order; tidak mengulang event lama. |
| Fill penuh | “Beli/Jual … selesai” | Gunakan jumlah/harga aktual bila tersedia. |
| Pemicu terpasang/aktif | “Pemicu dipasang” / “Pemicu aktif” | Terpisah dari notifikasi fill. |
| Dibatalkan/kedaluwarsa | “Sisa order dibatalkan” / “Order berakhir” | Kas/saham reservasi dilepas sesuai domain; fill lama tetap ada. |
| Likuiditas kurang atau halt | Alasan ringkas dan langkah berikutnya | Bedakan dari saldo kurang; jangan menjanjikan kapan pasti fill. |
| Bridge error / hasil belum pasti | “Status belum dapat dipastikan” + “Periksa order” | Tidak memicu duplikasi transaksi. |
| Save gagal | “Progres terakhir belum tersimpan” + coba simpan ulang | Pesan penting tetap dapat ditemukan; jangan menyatakan tersimpan. |
| Aktivitas offline selesai diproses | Ringkasan sekali saat kembali bila ada perubahan bermakna | Tidak mengirim puluhan toast untuk semua event historis. |

### 13.3 Perilaku dan aksesibilitas

- Notifikasi informasional memakai pengumuman sopan (`status`); error penting menggunakan `alert` secara terukur. Jangan membuat puluhan live region yang saling memotong.
- Pesan memiliki tombol tutup, target sentuh memadai, tidak menutupi submit/order review, serta dapat dibaca pada 200%.
- Jika judul dan body cukup, jangan menambahkan jargon internal. Alasan singkat seperti “Harga pemicu harus di bawah harga sekarang” adalah validasi UI, bukan tutorial yang dilarang N07.
- Transisi status setelah reload tidak boleh menghasilkan toast sukses palsu untuk semua order lama. Inisialisasi baseline pengamatan kemudian kirim hanya perubahan baru; ringkasan offline satu kali menggunakan receipt/event ID tersimpan bila diperlukan.
- Notifikasi native saat aplikasi ditutup belum otomatis tercakup oleh toast. Untuk rencana ini, wajibkan in-app + ringkasan saat kembali. Jika pemilik menghendaki push/notifikasi OS ketika tertutup, jadikan perluasan eksplisit dengan izin/perilaku Android dan bukti perangkat; jangan mengklaimnya sudah selesai dari uji browser.

### 13.4 Tugas atomik

- [ ] N09-01 Inventaris seluruh jalur submit, gagal preview, gagal confirm, cancel, partial fill, trigger dan perubahan sesi.
- [ ] N09-02 Samakan kode outcome/error domain dan bahasa pesan; jangan mengubah status ekonomi.
- [ ] N09-03 Hubungkan feedback inline dan toast pada titik transisi yang benar.
- [ ] N09-04 Perbaiki dedupe, prioritas pesan, masa tampil, dan recovery status belum pasti.
- [ ] N09-05 Verifikasi queue, focus, live region, teks panjang dan keyboard pada layar kecil.
- [ ] N09-06 Uji beberapa event berdekatan, callback duplikat, reconnect/reload, offline catchup serta perubahan akun.

Penerimaan: setiap kondisi pada matriks memiliki bukti input → outcome domain → pesan yang benar. Tidak ada banjir toast, transaksi ganda, atau pesan “berhasil” saat order sekadar diterima.

## 14. N10: Kepemilikan saham yang ringkas dan informatif

### 14.1 Susunan kartu/baris posisi

Satu baris pendek seperti “IDX:BBCA: 500 saham · Rp …” belum cukup membantu keputusan. Ubah menjadi kartu posisi kompak yang menampilkan:

- **Identitas:** ticker kuat, nama emiten lebih kecil, badge bursa bila diperlukan.
- **Nilai posisi:** angka dominan dalam mata uang posisi, tanpa mencampur IDR dan USD.
- **Kepemilikan:** jumlah saham dan ekuivalen lot berdasarkan lot size instrumen. Saham US tidak diasumsikan 100 saham per lot.
- **Laba/rugi berjalan:** nominal bertanda dan persentase, ditulis “Untung/Rugi” atau tanda yang jelas; warna bukan satu-satunya makna.
- **Harga rata-rata dan harga sekarang:** dua nilai kecil yang sejajar dan satuan per saham jelas.
- **Tindakan:** seluruh kartu membuka rincian posisi. Bila ada Beli/Jual langsung, gunakan tombol terpisah yang tidak nested di dalam button; transaksi tetap melalui ticket dan review.

Rekomendasi hierarki: ticker/nama di kiri, nilai dan laba/rugi di kanan; baris kedua jumlah, harga rata-rata, harga kini. Pada 320 px atau teks 200%, reflow ke beberapa baris. Jangan memperkecil font sampai semua angka muat satu baris.

### 14.2 Perhitungan dan state

1. Gunakan `averageCost`, `currentPrice`, `quantity`, `marketValue`, dan `unrealizedPnL` dari domain. Tambahkan field numerik/basis biaya ke DTO bila persentase tidak dapat dihitung dengan aman dari data existing.
2. Persentase untung/rugi memakai basis biaya yang benar untuk posisi tersebut. Basis nol tidak menghasilkan Infinity/NaN. Posisi short perlu formula arah/basis yang sesuai domain, bukan formula long dipaksakan.
3. Nominal unrealized P/L tidak sama dengan hasil bersih yang sudah terealisasi. Jangan memakai kata “bersih” untuk realized gross. Biaya boleh dijelaskan di rincian yang relevan.
4. Jika menampilkan bobot portofolio, definisikan penyebut: misalnya nilai saham dalam keseluruhan portofolio akun pada valuasi IDR yang sama. Jangan mencampur mata uang mentah. Bobot bukan kewajiban default jika menambah kepadatan tanpa manfaat.
5. Posisi jumlah nol/dahulu ditutup tidak memenuhi daftar kepemilikan aktif. Tampilkan riwayat di tujuan yang sesuai; jangan menghapus catatan ledger atau posisi yang dibutuhkan audit.
6. Saham tertahan/tersedia untuk dijual hanya ditampilkan jika domain menyediakan angka yang benar. Jangan menyamakan held shares dengan saham yang dapat langsung dijual.
7. Peringatan harga stale atau data gagal muncul singkat bila memang diperlukan. Kosong memiliki aksi “Cari saham”; error menyediakan coba lagi dan tidak menyamakan error dengan “Belum punya saham”.
8. `loadAsetView` dan `syncPortfolioLive` harus memakai struktur yang sama. Updater tidak boleh mengembalikan kartu baru menjadi teks satu baris. Cocokkan DOM berdasarkan `instrumentId`, bukan indeks array, agar data tidak tertukar saat urutan berubah.
9. Update harga/PL tidak membuang fokus, selection, atau posisi scroll. Jangan membuat kartu baru setiap detik bila nilainya saja yang berubah.

### 14.3 Tugas atomik

- [ ] N10-01 Petakan kontrak posisi, katalog/nama emiten, lot, mata uang, biaya dan short/long yang didukung.
- [ ] N10-02 Tentukan field default/rincian dan format nilai yang konsisten.
- [ ] N10-03 Tambahkan field DTO minimum hanya jika sumber numeriknya belum tersedia.
- [ ] N10-04 Implementasikan kartu posisi yang aksesibel serta tindakan ke detail/ticket.
- [ ] N10-05 Samakan renderer awal dan live updater berbasis ID stabil.
- [ ] N10-06 Uji untung/rugi/nol, long/short yang tersedia, USD/IDR, posisi tertahan, posisi ditutup, data kosong/error/stale dan perpindahan akun.
- [ ] N10-07 Cocokkan semua nilai yang ditampilkan dengan portfolio Java, receipt dan ledger yang relevan.

Penerimaan: pemain dapat melihat apa yang dimiliki, nilainya, harga perolehannya, dan hasil berjalan tanpa membuka setiap posisi. Data tetap benar setelah harga berubah, fill baru, pembatalan reservasi, load, dan akun diganti.

## 15. N11: Launcher Portofolio berkelompok dan mudah ditemukan

Ganti details#asset-tools dengan launcher kategori yang selalu terlihat. Gunakan daftar ringkas berikon, judul, dan keterangan satu baris; kategori membuka halaman fitur relevan. Tidak sekadar membuka accordion secara default, tidak menggantinya dengan daftar panjang semua alat, dan tidak memberi semua fitur kartu hero yang sama.

Kelompok awal: Saham & Order; Dompet & Valuta; Investasi Lain; Utang & Margin; Proteksi & Kepatuhan; Usaha & Dana Kelolaan bila relevan. Rincian pemetaan ada pada N17. Koleksi/properti yang berhubungan dengan kekayaan mempunyai kategori tersendiri jika tidak cocok dengan investasi keuangan. Lab dan uji stres dikecualikan secara eksplisit oleh N19; replay historis dipisahkan dari lab sandbox dan tetap dipertahankan jika mode perdagangan utama.

Dompet dan informasi pasif tidak wajib menjadi menu. Setiap launcher membuka tujuan yang nyata, mempertahankan akun, syarat, Back dan konfirmasi. Fitur tersedia maupun terkunci dapat ditemukan secara wajar, tanpa memalsukan akses.

- [ ] N11-01 Inventaris seluruh button/caller asset-tools dan buat mapping keep/move/remove-approved per fitur.
- [ ] N11-02 Ganti klasifikasi berbasis awalan teks/tag dengan daftar tujuan eksplisit yang kecil dan terarah.
- [ ] N11-03 Buat launcher kategori terbuka dalam bentuk daftar ringkas dengan normal/pressed/focus/locked/loading yang konsisten.
- [ ] N11-04 Pertahankan akun, syarat dan review tindakan berisiko; membuka kategori tidak mengeksekusi ekonomi.
- [ ] N11-05 Verifikasi seluruh fitur yang dipertahankan punya tujuan; removal lab/uji stres ditelusuri ke N19.
- [ ] N11-06 Uji setiap kategori, klik/keyboard/Back, scroll, 200% dan live refresh tanpa launcher ganda.

Penerimaan: tidak ada accordion alat yang menyembunyikan fitur, kategori koheren, setiap tujuan tetap terjangkau atau tercatat dihapus atas keputusan pemilik, dan state finansial terlindungi.

## 16. N12: Bisnis harus jelas interaktif

### 16.1 Cakupan layar

Kerjakan seluruh area **Usaha Saya**, **Koleksi**, dan **Kelola Dana**, termasuk keadaan kosong, perusahaan yang sudah ada, syarat pangkat, modal tindakan dan rincian. Jangan hanya mengubah warna tab atau menambahkan chevron ke semua panel.

### 16.2 Solusi per subarea

**Usaha Saya:**

- Pisahkan ringkasan kekayaan/perusahaan yang pasif dari tombol kelola.
- Perusahaan tampil sebagai kartu dengan nama, status operasi, angka utama yang tersedia dari domain, dan “Kelola usaha”. Angka utama dipilih dari data yang ada; jangan mengarang revenue/profit.
- Tindakan tim, fasilitas, keuangan, kebijakan, modal dan riwayat memiliki button yang jelas di halaman detail. Panel informasi tidak diberi hover/chevron jika tidak bisa dibuka.
- State belum punya usaha langsung menawarkan “Dirikan perusahaan” beserta modal/syarat aktual. Jangan menenggelamkan CTA di antara paragraf penjelasan kepemilikan.

**Koleksi:**

- Kartu barang membedakan melihat rincian, dimiliki, dapat dibeli, terkunci, dan sedang diproses.
- Membuka kartu tidak berarti langsung membeli. Harga, kelangkaan, syarat, dan tindakan beli/jual tetap berasal dari katalog/state.
- Gambar 150 koleksi hanya dipakai jika integrasi asetnya sudah terbukti; jangan mengklaim paket gambar selesai berarti seluruh galeri dalam APK otomatis terintegrasi.

**Kelola Dana:**

- Pisahkan informasi status karier/AUM dari launcher mandat klien, dana kelolaan, staf, fund builder, dan laporan yang benar-benar ada.
- Akun klien harus jelas sebelum tindakan keuangan; jangan menukar ringkasan kekayaan pribadi dengan dana klien.
- Jika fitur terkunci karena jabatan, kartu menampilkan “Terbuka di …” dan “Lihat syarat”, bukan angka palsu atau tombol aktif yang kemudian tidak berfungsi.
- Laporan/mandat kosong memiliki aksi berikutnya yang benar; jangan membuat kartu generik yang tampak dapat dipilih tetapi tidak punya handler.

### 16.3 Tugas atomik

- [ ] N12-01 Inventaris seluruh tindakan di tiga subtab dan detail turunannya, lengkap dengan handler dan syarat.
- [ ] N12-02 Pisahkan panel pasif, launcher, pilihan subtab dan aksi ekonomi berdasarkan pola bagian 4.
- [ ] N12-03 Perbaiki state kosong dan kartu perusahaan/tindakan kelola.
- [ ] N12-04 Perbaiki galeri serta state barang tanpa menghapus filter/koleksi.
- [ ] N12-05 Perbaiki launcher kelola dana, mandat, staf dan laporan dengan isolasi akun yang benar.
- [ ] N12-06 Pastikan label tombol memuat tindakan, focus/pressed terlihat, Back kembali ke subtab dan scroll yang tepat.
- [ ] N12-07 Jalankan journey pendirian/kelola, koleksi, mandat/dana dan state terkunci sesuai domain yang tersedia.

Penerimaan: pengguna dapat membedakan mana informasi dan mana tindakan melalui bentuk, label dan state. Semua tombol punya tujuan nyata; tidak ada bidang pasif yang menipu seolah interaktif. Seluruh fitur Bisnis tetap terjangkau setelah perbaikan.

## 17. N13: Lainnya dan Riset jelas, terkelompok, serta konsisten

Gunakan kelompok Profil, Misi & Kompetisi, Riset, Pengaturan sesuai keputusan pemilik. Tautan transaksi/galeri dipindah atau menjadi shortcut yang jelas dengan satu tujuan kanonis. Seluruh isi flow, detail, form, review, receipt, empty/error/locked ikut diperbarui melalui N16/N18; jangan hanya mengganti launcher.

Profil/progres/level tidak dicampur dengan subscription/terminal. Paket aktif adalah informasi dengan tindakan kelola yang terpisah. Riset yang memengaruhi trading, berita, konsensus, tech tree dan booster tetap ada. Lab/uji stres dihapus dari UI N19. Replay historis tidak otomatis dianggap lab; pertahankan mode yang benar-benar menjalankan perdagangan dan progresnya sendiri.

Era Baru tetap mempunyai syarat domain, rincian dampak, review dan konfirmasi. Membuka kartu tidak mereset run. Fitur terkunci menjelaskan syarat aktual dan boleh membuka rincian bila memang didukung. Jangan memberi tombol mati atau promosi unlock palsu.

- [ ] N13-01 Inventaris launcher, kartu pasif, paket, berita, pengaturan dan state Era Baru pada source terkini.
- [ ] N13-02 Petakan ke Profil, Misi & Kompetisi, Riset, Pengaturan; gunakan pola interaksi bersama yang sesuai.
- [ ] N13-03 Perbaiki syarat/progres/konfirmasi Era Baru tanpa mengubah aturan reset.
- [ ] N13-04 Perbaiki terminal/hasil/booster/pohon riset dan replay yang dipertahankan; keluarkan lab/uji stres menurut N19.
- [ ] N13-05 Verifikasi setiap tujuan, route alias yang diperlukan, Back, fokus, akun dan state terkunci.
- [ ] N13-06 Uji journey profil, misi, liga, paket, riset dan Era Baru pada save terisolasi; sertakan detail turunannya.

Penerimaan: semua fitur yang dipertahankan terlihat sebagai tindakan yang jelas; panel pasif tidak meniru tombol; navigasi tidak menyisakan orphan, dan data/progres tidak hilang.

## 18. Urutan eksekusi dan checkpoint

Revisi alur kerja 24 Oktober 2026: pemilik meminta pengelompokan berdasarkan kesesuaian agar Hermes menuntaskan satu alur sebelum berpindah. Target tetap 20 paket / 126 subtask. Delapan kelompok G01–G08 adalah urutan pelaksanaan; setiap ID asli, requirement, acceptance dan status tetap dipertahankan.

Pemilik memilih APK kandidat bertahap dan menegaskan task yang belum tuntas tetap belum selesai walau sebagian tes telah lulus. Bukti parsial boleh menjadi referensi; Hermes mengulang penelusuran dan pengujian yang diperlukan sampai seluruh kriteria ID terpenuhi. Pengelompokan ini tidak memulai ulang Hermes: keadaan saat revisi tetap PAUSED_BY_OWNER.

### 18.0 Satu file plan per kiriman pemilik

Gunakan delapan file terpisah dengan nama plan.md dalam folder berurutan. Indeks pengiriman: Work/Master-Plan-Gabungan-2026-10-24/Plans/README.md. Setiap file membawa daftar ID, acceptance, konteks parsial, langkah kerja dan syarat selesai untuk SATU kelompok.

Pemilik mengirim file 01, menunggu sampai selesai, lalu mengirim file 02, dan seterusnya. Hermes hanya mengaktifkan file yang dikirim atau ditunjuk secara eksplisit oleh pemilik. Sesudah seluruh ID serta deliverable file aktif terpenuhi, simpan checkpoint/laporan, berhenti, dan tunggu kiriman file berikutnya. Dilarang lanjut otomatis, mengambil sendiri plan berikut, atau menganggap daftar G01–G08 sebagai izin menjalankan semua kelompok.

Target akhir tetap 20 paket / 126 subtask. Dua PASS yang sudah tercatat tetap dipertahankan; task yang belum tuntas tetap terbuka meski ada tes lulus. Pengiriman file, bukan keberadaan file di disk, yang memberikan scope pelaksanaan. Penyusunan file saat ini tidak mencabut PAUSED_BY_OWNER.

### 18.1 Preflight saat pemilik mengirim file plan

Verifikasi root/Git/branch/delta source terkini, kepemilikan file dan hash save pemain. Pertahankan semua perubahan termasuk implementasi N08 setelah checkpoint jeda lama; jangan reset ke source-at-owner-pause.zip atau memulai dari N08 RED historis. Baca ringkasan terbaru dan execution_group dalam tracker. QA selalu memakai save terisolasi.

N20-02 dan N20-03 sudah PASS_WITHIN_SCOPE. Persetujuan revisi 2 tetap berlaku: canvas navy gelap, panel/kontrol terang, lima tab lama. Jangan meminta persetujuan visual yang sama lagi. Tugas N20 lain tetap terbuka di G08; pekerjaan persiapan yang diperlukan kelompok aktif tetap dilakukan tanpa meluluskan tugas akhir tersebut.

### 18.2 Kelompok yang wajib dikerjakan berurutan

| Urutan | Kelompok | Paket asal | Jumlah subtask | Prasyarat |
|---|---|---|---:|---|
| G01 | Waktu otomatis dan berita | N01, N02 | 13 | P00; persetujuan visual N20-03 sudah tercatat |
| G02 | Pasar, detail saham dan grafik | N03, N05, N06, N14 | 22 | Seluruh ID G01 PASS_WITHIN_SCOPE |
| G03 | Transaksi dari tiket sampai notifikasi | N07, N08, N09 | 19 | Seluruh ID G02 PASS_WITHIN_SCOPE |
| G04 | Portofolio dan kepemilikan | N11, N17, N10, N15 | 27 | Seluruh ID G03 PASS_WITHIN_SCOPE |
| G05 | Bisnis, koleksi dan dana kelolaan | N12 | 7 | Seluruh ID G04 PASS_WITHIN_SCOPE |
| G06 | Lainnya, progres dan riset | N13, N18 | 12 | Seluruh ID G05 PASS_WITHIN_SCOPE |
| G07 | Konsistensi seluruh UI dan pembersihan | N19, N04, N16 | 20 | Seluruh ID G06 PASS_WITHIN_SCOPE |
| G08 | Rekonsiliasi akhir dan penyerahan | N20 | 6 | Seluruh ID G07 PASS_WITHIN_SCOPE |

File 01/G01 mulai hanya setelah pemilik mengirim atau menunjuk file tersebut. Kelompok selanjutnya mulai hanya setelah pemilik mengirim file plan berikutnya. IN_PROGRESS pada kelompok lain berarti pekerjaan parsial yang tersimpan, bukan beberapa kelompok sedang aktif sekaligus.

#### G01 — Waktu otomatis dan berita

Kesesuaian: Satu pemilik waktu, lifecycle, deadline berita, catchup dan save.

Urutan dalam kelompok: N01: tempo, migrasi dan lifecycle → N02: deadline, engine, persistence dan tampilan berita → uji gabungan waktu/berita/save.

Syarat keluar: Seluruh 13 ID lulus kriterianya; tempo 24x dan efek 60/120 menit nyata benar pada aktif, offline, restart dan retry; kalender serta uang tetap konsisten.

#### G02 — Pasar, detail saham dan grafik

Kesesuaian: Daftar pasar dan halaman detail memakai renderer, updater dan data grafik yang sama.

Urutan dalam kelompok: N03: ranking/filter → satu pekerjaan grafik terpadu untuk N05/N06/N14: hapus UI yang ditolak, empat rentang, data dan aksesibilitas → pemeriksaan refresh/reload.

Syarat keluar: Seluruh 22 ID lulus; tiga movers sesuai data/filter, empat rentang memakai OHLCV nyata, tabel/blok teknis hilang, grafik dan fokus tetap benar.

#### G03 — Transaksi dari tiket sampai notifikasi

Kesesuaian: Pilihan order, payload, fill, receipt dan feedback harus mengikuti transaksi yang sama.

Urutan dalam kelompok: N07: pilih jenis/side/akun, quote dan review → N08: status, fill/biaya/kas, cancel dan reload → N09: feedback seluruh outcome → perjalanan transaksi lengkap.

Syarat keluar: Seluruh 19 ID lulus; review/confirm, akun, idempotency, seluruh status, ledger, notifikasi dan jalur gagal benar; tes parsial lama diulang sesuai acceptance.

#### G04 — Portofolio dan kepemilikan

Kesesuaian: Kategori, posisi pribadi dan komposisi pemegang saham bergantung pada hasil transaksi G03.

Urutan dalam kelompok: Inventaris/mapping bersama N11/N17 → kategori dan rute Portofolio → N10: posisi akun pribadi → N15: pemegang saham emiten yang terpisah → pemeriksaan semua tujuan, settlement dan restore.

Syarat keluar: Seluruh 27 ID lulus; fitur Portofolio terjangkau, posisi pribadi cocok domain, holder emiten bukan placeholder, supply/akun tidak dihitung ganda, Back/fokus/reload benar.

#### G05 — Bisnis, koleksi dan dana kelolaan

Kesesuaian: Ketiga subtab berada dalam satu alur Bisnis dengan syarat serta akun yang saling terkait.

Urutan dalam kelompok: Usaha Saya → Koleksi → Kelola Dana → journey normal/kosong/terkunci/gagal, review tindakan dan rute balik.

Syarat keluar: Seluruh 7 ID lulus; informasi dan tindakan jelas berbeda, seluruh fitur/detail dapat dipakai sesuai syarat, angka dan isolasi akun benar.

#### G06 — Lainnya, progres dan riset

Kesesuaian: Kedua paket mengatur hub empat kelompok beserta layar turunannya.

Urutan dalam kelompok: Satu inventaris/mapping N13/N18 → Profil → Misi & Kompetisi → Riset → Pengaturan → seluruh detail/state, reward dan konfirmasi Era Baru.

Syarat keluar: Seluruh 12 ID lulus; empat kelompok serta detailnya konsisten, reward/langganan/progres/save benar dan tidak ada reset tanpa konfirmasi.

#### G07 — Konsistensi seluruh UI dan pembersihan

Kesesuaian: Audit lintas layar baru lengkap setelah seluruh alur tujuan G02–G06 tersedia.

Urutan dalam kelompok: N19: audit removal lab/uji stres dan kompatibilitas → N04: sapu label/jargon di seluruh route/state → N16: tuntaskan bagian visual yang tersisa dan audit lintas layar/state/viewport.

Syarat keluar: Seluruh 20 ID lulus; dua UI eksperimen tidak terjangkau, save/shared domain aman, teks produk bersih, visual/keyboard/200%/kontras/motion/performa serta Delivery Gate terbukti.

#### G08 — Rekonsiliasi akhir dan penyerahan

Kesesuaian: Dokumen, semua bukti, source dan APK akhir harus menunjukkan snapshot yang sama.

Urutan dalam kelompok: Pertahankan PASS N20-02/N20-03 → N20-01/N20-04: rekonsiliasi kontrak dan rollout → N20-05: audit seluruh 126 ID → N20-06: source, bukti, preview dan APK final.

Syarat keluar: N20-01/04/05/06 dan seluruh ID sebelumnya lulus; total 20/20 paket dan 126/126 subtask, APK final cocok source, seluruh temuan wajib ditangani dan batas native tetap dinyatakan.

### 18.3 Siklus satu kelompok

1. Nyatakan tepat satu kelompok aktif dan daftar ID yang belum PASS_WITHIN_SCOPE di kelompok itu. Catat source/snapshot yang dipakai.
2. Telusuri alur menyeluruh dan pakai ulang implementasi, helper serta runner existing. Kerjakan subtask yang saling terkait dalam urutan internal kelompok, lalu tes, review, perbaiki dan tes ulang.
3. Isi next_action per ID dengan kekurangan konkret. Jangan menggantinya dengan kalimat umum yang tidak menyebut pekerjaan tersisa. Tes yang lulus sebagian tidak menutup ID dengan acceptance lain yang belum terpenuhi.
4. Setiap ID hanya PASS_WITHIN_SCOPE setelah seluruh requirement dan acceptance yang berlaku terbukti. Satu bukti yang relevan boleh dirujuk beberapa ID, tetapi cakupannya harus jelas dan tidak meluluskan kondisi yang tidak diuji.
5. Setelah seluruh ID file aktif PASS_WITHIN_SCOPE, checkpoint/bukti dan APK kandidat yang diwajibkan selesai, BERHENTI dan tunggu pemilik mengirim file plan berikutnya. Kelompok sebelumnya lulus adalah prasyarat, bukan izin melanjutkan otomatis.
6. Jika gagal, diagnosis dan perbaiki pada kelompok aktif. Sesudah dua kegagalan sama tanpa bukti/diagnosis baru, laporkan PERLU PERHATIAN dan ubah pendekatan. Bila ada blocker nyata, kerjakan bagian independen dalam kelompok yang sama, catat ID/blocker dan minta keputusan yang memang diperlukan; jangan memilih fitur dari kelompok lain karena terasa lebih mudah.
7. Jika perubahan mengungkap dependency rusak di kelompok lain, catat hubungan dan lakukan perbaikan minimum untuk alur aktif, tanpa memperluasnya menjadi pekerjaan kelompok baru. Bila perubahan membatalkan bukti sebelumnya, buka kembali ID terdampak dan perbaiki dependency sebelum meneruskan urutan.
8. Satu writer source tetap Hermes. Tidak menjalankan build yang sama bersamaan, mengompilasi saat kontrak DTO sedang ditulis, meluncurkan agen lain, atau mengirim pesan lintas chat.

### 18.4 Aturan untuk requirement lintas layar

N16 menjadi arah visual yang diterapkan ketika tiap alur G02–G06 disentuh. Perubahan CSS/token/shared renderer dibatasi pada kebutuhan alur aktif. G07 menuntaskan kekurangan dan melakukan audit menyeluruh; jangan sengaja menunda semua desain sampai G07 lalu membangun layar dua kali.

Penggantian teks N04 dan removal UI N19 boleh dilakukan ketika menyentuh alur aktif, dengan bukti dicatat. Semua entry lab/uji stres yang telah diputuskan dihapus dikecualikan dari rancangan kategori sejak awal. Penutupan seluruh ID N04/N16/N19 tetap milik G07 setelah cakupan lintas route/state dan kompatibilitas terbukti.

N11/N17 memakai satu inventaris kategori dan rute. N13/N18 memakai satu inventaris empat kelompok Lainnya dan detailnya. N05/N06/N14 memakai satu perubahan grafik yang terintegrasi. Setiap ID tetap memiliki kriteria sendiri; tidak membuat ulang implementasi atau dokumen yang sama untuk setiap nama paket.

### 18.5 Empat penyerahan APK kandidat

| Kandidat | Titik penyerahan | Cakupan perubahan yang dinilai |
|---|---|---|
| APK-01 | Penutupan G01 | Fondasi waktu 24x, berita nyata, lifecycle dan save. |
| APK-02 | Penutupan G03, setelah G02 | Pasar/grafik dan transaksi dari tiket sampai notifikasi. |
| APK-03 | Penutupan G06, setelah G04/G05 | Portofolio/kepemilikan, Bisnis, dan seluruh Lainnya. |
| APK-04 | Di G08 setelah G07 dan sebelum N20-06 ditutup | Seluruh 126 subtask, konsistensi UI, regresi dan penyerahan akhir. |

Setiap kandidat dibangun fresh dari snapshot stabil; verifikasi metadata/signature/hash dan kecocokan aset, jalankan pemeriksaan sesuai perubahan, sertakan ID selesai/terbuka, hasil gagal serta batas cakupan. APK awal dapat memuat alur lain yang masih parsial; keberadaan alur tersebut di dalam paket tidak meluluskannya. Tidak menyerahkan APK lama sebagai kandidat baru.

Kandidat bertahap tidak mengurangi seluruh gate pada bagian 19/21/30. APK-04 menjadi kandidat final hanya setelah 126/126 ID memenuhi seluruh kriterianya. N20-06 mencakup pembuatan/verifikasi/penyerahan APK-04, sehingga APK final dibuat di dalam G08, bukan menunggu setelah tugas penyerahan selesai.

Setiap file plan selesai merupakan titik BERHENTI yang diwajibkan pemilik, baik file itu menyerahkan APK maupun hanya checkpoint/bukti. Hermes menunggu kiriman file plan berikutnya dan tidak menjadwalkan kelanjutan sendiri. Instruksi STOP/jeda di tengah file juga selalu dihormati. Tidak ADB/instalasi/emulator/perangkat baru; native tetap NOT_TESTED_BY_SCOPE.

### 18.6 Checkpoint minimum

Catat kelompok aktif/selesai/berikutnya; ID beserta status dan kekurangan per ID; root/branch/HEAD/hash source; file berubah; command/exit code/log/assertion; screenshot/viewport/skala teks; review/performa; temuan/blocker; hash save pemain; serta kandidat APK dan scope yang benar-benar diverifikasi. Tracker menjadi sumber status, ringkasan report/STATUS dan next_action checkpoint harus sinkron.

Status yang diperbolehkan tetap PENDING, IN_PROGRESS, FAIL, BLOCKED dan PASS_WITHIN_SCOPE. Kelompok tidak menambah denominator task. Angka laporan tetap x/20 paket dan y/126 subtask; tambahkan progres kelompok untuk menunjukkan fokus kerja.

## 19. Rencana verifikasi menyeluruh

### 19.1 Domain, bridge dan uang

Pakai runner existing. Jalankan suite terdampak lebih dahulu, kemudian regresi integrasi yang diwajibkan standar proyek. Jangan membuat framework baru hanya untuk checklist.

Contoh alat existing yang perlu diperiksa sebelum dipakai:

- `Work/run-codex-jvm-check.ps1` menerima `-Tests` dan `-OutputDirectory`, mengompilasi source/test dan membuat sandbox terisolasi.
- `AutomaticWorldTimeTest`, `TimeDualRateLifecycleTest`, `CanonicalWorldRegressionTest`, `WireIntegrationDomainTest`, `TradingWebBridgeTest` adalah titik awal waktu/bridge; ini bukan pengganti seluruh suite akhir.
- Telusuri suite order, funds, risiko/margin, corporate actions, akun/save dan replay yang menggunakan fungsi yang diubah. Jangan memilih suite hanya karena cepat lulus.
- Uji real deadline berita dengan controlled clock; jangan menunggu satu–dua jam sungguhan untuk boundary tests.
- Untuk N01/N02/N07/N08/N09 dan save/lifecycle terkait, jalankan minimal **10 urutan × 100 aksi bermakna**, seed tercatat, assertion setelah setiap aksi. Gunakan runner kritis existing dan perluas hanya bagian yang diperlukan.
- Invariant: ledger rekonsiliasi, kas/reservasi/saham/utang konsisten, tidak ada pembayaran/fill ganda, request idempotent, interval waktu tidak hilang/dihitung ganda, deadline tidak reset, serta save tidak merusak state valid ketika gagal.

Urutan campuran harus mencakup aktif → hidden → kembali → fill/partial → cancel → save → reload → batas berita → mode switch → error yang diharapkan. Jangan memenuhi 1.000 aksi dengan no-op/read berulang atau menelan exception tak terduga.

### 19.2 Browser dan perjalanan pengguna

Runner yang ditemukan dan relevan:

| Area | Runner existing yang dapat dipakai setelah diaudit |
|---|---|
| Waktu/lifecycle | `Work/test-automatic-world-browser.py`, `Work/verify-r01-r02-clock-sponsor.js` |
| Pasar/ranking/grafik | `Work/verify-tahap2-market.js`, `Work/verify-a11-market-redesign.js`, `Work/verify-chart-emiten.js` |
| Berita | `Work/verify-r12-news.js`, suite research/terminal yang memakai horizon |
| Tiket/status/notifikasi | `Work/test-e2e-playtest-journey.py`, `Work/test-live-sync-browser.py`, `Work/verify-portrait-flows.js`, `Work/verify-portrait-flows.py` |
| Portofolio | `Work/test-classic-ui-assets.py`, `Work/verify-tahap3-assets.js`, `Work/test-redesign-control-gaps.py` |
| Bisnis/Lainnya | `Work/verify-a9-business.js`, `Work/test-redesign-journeys.py`, suite research/corporate/fund controls |
| Layout dan aksesibilitas | `Work/verify-redesign-screens.py`, `Work/verify-int-viewports.js`, `Work/test-p4-viewports-accessibility.py` |
| Performa | `Work/verify-benchmark-performance.js`, `Work/benchmark-performance-final.py` |
| Integrasi suite | `Work/run-redesign-browser-regressions.py` dengan classpath/source/save yang sudah dipastikan sesuai snapshot |

Sebelum menjalankan wrapper, periksa apakah ia menghapus save, memakai port pribadi, menyalin kelas lama, menulis artefak historis, atau hardcode selector lama. Jalankan help bila tersedia. Gunakan output baru dan save terisolasi; jangan menimpa bukti gagal.

Perubahan tes yang sah harus dikaitkan ke requirement yang berubah:

- Tes yang meminta dropdown order diganti aksi memilih kartu dan assertion enum/payload.
- Tes yang meminta menu sesi dihapus/diganti assertion bahwa menu tidak ada dan dunia otomatis.
- Tes yang mengharuskan jam sama persis sesudah reload harus mengikuti kontrak waktu berjalan; tetap periksa tidak mundur dan tidak menggandakan aktivitas.
- Selector BUY/SELL harus dibatasi pada ticket/komponen yang benar jika ada beberapa tombol sejenis. Jangan menggunakan `.first()` tanpa memastikan target semantik.
- Fixture trading harus memenuhi level/sektor/akun yang berlaku. Jangan menonaktifkan guard produksi agar fixture TLKM atau US bisa diperdagangkan.
- Perubahan benchmark harus tetap mengukur pekerjaan berat yang nyata. Jangan mengganti catchup satu hari yang lambat menjadi tick satu detik hanya agar angka PASS. Catat operasi berat yang diukur dan tuntaskan feedback/responsiveness yang memang dipersyaratkan.

### 19.3 Matriks layar dan state

Uji viewport standar proyek: **320×568, 360×640, 390×844, 412×915, 430×932, 768×1024, 1440×900**, pada **100% dan 200% teks**. Tambahkan **470×658** sesuai anotasi terbaru, dan **462×648** untuk reproduksi komentar sebelumnya.

Skala teks benar-benar mengubah ukuran font/reflow, bukan memperbesar file screenshot. Untuk tiap layar yang berubah, inventaris state normal, kosong, loading, error/stale, selected, locked, pending, sukses dan gagal yang berlaku.

Periksa:

- Tidak ada horizontal overflow tak sengaja, angka terpotong, tombol tertutup nav, atau scroll terakhir yang tidak bisa dijangkau.
- Grid tiga movers, pilihan order, kartu posisi, launcher serta pesan panjang tetap terbaca.
- Tidak ada feedback loop ukuran nav: nilai tinggi hasil `ResizeObserver` yang dipakai sebagai `min-height` elemen yang sama dapat membesar berulang setelah skala teks berganti. Audit perbaikan yang sudah ada sebelum memperluas CSS.
- Focus terlihat, urutan Tab masuk akal, Enter/Space bekerja, modal Escape/Back sesuai konteks, fokus kembali ke pembuka, tidak ada nested button.
- Target sentuh minimal sesuai standar proyek 48 dp/ukuran CSS ekuivalen yang diuji, kontras teks dan batas kontrol sesuai kebutuhan, reduced motion bekerja.
- Pembaruan realtime tidak menghapus draft, filter, pilihan jenis order, status radio, posisi scroll, atau fokus.
- Penilaian visual membuka screenshot aktual. Assertion geometri saja tidak cukup untuk menyatakan semua layar enak dilihat.

### 19.4 Performa dan operasi berat

Tetap gunakan batas proyek: P95 tindakan biasa ≤300 ms, perpindahan halaman ≤1 detik, pekerjaan berat memberikan feedback ≤300 ms dan UI tetap responsif. Ukur setidaknya 30 sampel untuk metrik P95 yang dilaporkan.

Jam rAF tidak boleh memicu request Java setiap frame. Render presentasi ringan; sinkronisasi domain mengikuti interval yang diperlukan. Pada hidden tidak ada loop visual yang terus menghabiskan CPU.

Untuk catchup offline satu jam, dua jam, satu hari, dan periode panjang yang didukung, ukur durasi dan respons UI. Jika bridge sinkron menyebabkan freeze, pakai pola kerja asynchronous yang sesuai stack existing pada titik berat tersebut; jangan mengganti engine, menghilangkan aktivitas, atau menurunkan ambang tanpa mandat. Progress/indikator harus tampil sebelum kerja berat, bukan setelah freeze selesai.

Jika diperlukan chunking, state ekonomi dan cursor catchup harus konsisten terhadap interruption/retry. Jangan menandai seluruh rentang selesai sebelum kejadian diproses dan disimpan. Tampilan tidak boleh mengizinkan transaksi memakai saldo/timestamp separuh catchup tanpa aturan yang jelas.

### 19.5 Build, APK, dan scope native

1. Kompilasi fresh source yang stabil, lalu jalankan tes yang berlaku pada snapshot tersebut.
2. Bangun APK dengan tooling existing dan pengaturan lokal per proses yang sudah terbukti; jangan mengubah pengaturan global untuk mengatasi masalah host.
3. Verifikasi application ID, version metadata, signature yang berlaku, hash APK, ukuran, dan byte aset web terhadap source. Jangan menyebut APK lama terbaru jika source berubah setelah build.
4. Bandingkan semua aset relevan, bukan hanya `app.js`; periksa `index.html`, CSS, font/aset lokal dan data yang dipakai.
5. Browser/host Java lulus, APK berhasil dibangun, APK terpasang, emulator lulus, dan perangkat fisik lulus adalah status berbeda.
6. Tidak melakukan ADB/instalasi/perangkat baru dalam mandat ini. Native yang belum diuji tetap ditulis `NOT_TESTED_BY_SCOPE`; jangan memberi PASS palsu atau menutupnya dengan screenshot browser.

## 20. Risiko, keputusan kecil, dan solusi yang harus dihindari

| Risiko/pertimbangan | Penanganan yang wajib |
|---|---|
| Jam pada laju 24x tetap bergerak lebih cepat dari detik nyata | Gunakan timestamp domain, HH:mm dan presenter halus; domain memakai 24x yang telah disetujui. Jangan memperlambat ekonomi atau membikin jam palsu. |
| Efek berita nyata bercampur hari game | Deadline nyata disimpan dan dipakai engine; pecah catchup di batas event. Jangan sekadar mengonversi label UI. |
| “Semua label simulasi” diterapkan sebagai replace global | Audit teks yang dilihat pemain. Jangan merusak enum, source metadata, legal provenance, error finansial atau bukti QA. |
| Minimal tiga movers saat data kurang | Tampilkan sampai tiga item valid; empty/kurang data jujur. Tidak mengarang data atau mengulang saham. |
| Receipt ringkas kehilangan bukti biaya/fill | Jadikan data aktual tetap tersedia dan cocokkan ledger; pisahkan detail ID dari ringkasan. |
| Notifikasi menutup error sebelumnya | Prioritas/antrean kecil, dedupe event, error penting juga inline. Tidak menambah sistem besar tanpa kebutuhan. |
| MAX order memakai modal awal hardcoded | Quote akun terpilih dari domain; data gagal tidak menjadi saldo palsu. |
| Portofolio berkilat/terurut ulang saat refresh | Update per ID stabil dan node nilai; pertahankan fokus/scroll. |
| Dropdown dihilangkan tetapi fiturnya ikut hilang | Inventaris launcher sebelum/sesudah dan klik tiap tujuan; tidak cukup menghitung jumlah kartu. |
| Semua kartu diberi chevron walau pasif | Chevron/pressed hanya untuk tindakan nyata; ringkasan pasif tetap informasi. |
| Source berubah selama uji/build | Satu writer per file, snapshot hash, re-run terdampak. Hasil snapshot lama tidak dilabeli current. |
| Save gagal ketika startup/mode switch | Tampilkan gagal simpan secara aman, jangan reset atau membekukan dunia yang masih valid. Uji penyimpanan tidak dapat ditulis. |
| Replay mencapai akhir data | Pertahankan batas dataset dan pesan yang benar; jangan membuat bar baru agar tampak tidak pernah berhenti. Dunia internal tetap punya kontrak otomatis sendiri. |
| Source referensi REA tidak tersedia lengkap | Nyatakan hanya temuan yang benar-benar ada; bangun perilaku yang diminta dari kontrak produk, jangan mengaku source referensi sudah dipulihkan. |

## 21. Hasil akhir yang harus diserahkan agen

Seluruh bukti baru direkomendasikan di `Artifacts/Master-Plan-Gabungan-2026-10-24/` atau direktori checkpoint baru yang teridentifikasi jelas. Jangan membuat duplikasi laporan besar bila tracker existing sudah memadai.

Penyerahan minimum:

1. **Tracker 20 paket / 126 subtask**, dengan subtask seluruh ID pada dokumen ini, status, dependency, implementasi, tes, bukti, reviewer dan next_action.
2. **Source terintegrasi** yang menerapkan laju tunggal 24x serta perubahan UI/logika yang diperlukan semua anotasi.
3. **Perbandingan sebelum/sesudah** setiap N01–N13. N01 menggunakan rekaman gerak; N02 menggunakan controlled clock dan dampak engine, bukan screenshot countdown saja.
4. **Matriks field dan fitur:** jenis order → enum/payload, status order → tampilan/aksi, fitur Portofolio/Bisnis/Lainnya → opener/route, string lama → penggantian/penghapusan.
5. **Log pengujian** beserta exit code, source hash dan snapshot; kegagalan awal tetap tersimpan dengan diagnosis serta rerun.
6. **Hasil sequence 10×100** untuk jalur kritis yang disentuh, beserta invariant sesudah setiap aksi dan seed.
7. **Hasil layout/aksesibilitas/performa** dengan batas cakupan dan screenshot aktual yang dibuka reviewer.
8. **APK kandidat terbaru** yang cocok dengan source, hash, signature, metadata dan bukti isi paket; status native ditulis terpisah.
9. **Bukti save pemain utuh**, serta daftar save/port terisolasi yang dipakai untuk QA.
10. **Laporan akhir yang jujur:** selesai x/20 paket dan y/126 subtask, sisa ID, kegagalan terbuka, pertanyaan keputusan yang masih ada, dan next_action konkret. Jangan menulis “semua selesai” jika satu ID hanya selesai visual tetapi logikanya belum diverifikasi.

### 21.1 Matriks penutupan per anotasi

| ID | Implementasi | Bukti utama yang diperlukan | Status awal dokumen |
|---|---|---|---|
| N01 | Presenter jam + continuity + fondasi otomatis terjaga | Rekaman, angka laju controlled clock, lifecycle/restart/mode tests | PENDING_VERIFICATION |
| N02 | Deadline nyata + efek engine + save/migrasi + UI | Batas 60/120 menit, offline silang deadline, tidak reset/ganda | PENDING |
| N03 | Tiga penguat dan tiga pelemah | Data ranking terkontrol, filter, click detail, realtime | PENDING |
| N04 | Sapuan label/disclaimer bermain | Inventaris string/route dan audit state dinamis | PENDING |
| N05 | Hapus tabel alternatif | DOM/focus bersih, grafik tetap benar/aksesibel | PENDING |
| N06 | Hapus blok sumber teknis | Screenshot semua chart/range dan kesamaan data | PENDING |
| N07 | Pilihan order visual + payload benar | Radio/keyboard, field kondisional, preview/confirm/domain | PENDING |
| N08 | Receipt ringkas + detail aktual | Semua status, fill/fee/net ledger, reload/live update | PENDING |
| N09 | Feedback kondisi dan outcome | Matriks event, dedupe, queue, inline, aksesibilitas | PENDING |
| N10 | Kartu kepemilikan informatif | Nilai domain, akun, short/long, USD/IDR, updater stabil | PENDING |
| N11 | Launcher Portofolio terbuka | Pemetaan seluruh fitur dan klik masing-masing | PENDING |
| N12 | Affordance Bisnis | Journey ketiga subtab dan state normal/empty/locked/error | PENDING |
| N13 | Affordance Lainnya & Riset | Launcher, paket, progres/konfirmasi Era Baru, Back/fokus | PENDING |

### 21.2 Delivery Gate Antislop dan kualitas proyek

Evaluasi empat blok Antislop dari skill yang benar-benar terpasang: **Block 1: Hard Gate (absolute)**, **Block 2: Purpose-Gate (technique allowed, reason required)**, **Block 3: Liveliness (required to be alive, not just clean)**, serta **Block 4: Craftsmanship & Quality Locks**. Setiap hasil memiliki bukti, cakupan, serta pengecualian dari pilihan visual pemilik yang sudah dicatat dalam `DESIGN.md`. Ikuti pertanyaan dan arah jawaban PASS/FAIL dalam skill; jangan menggantinya dengan penilaian umum “desain sudah bagus”.

Gate visual tidak menggantikan tes ekonomi. Kode yang lulus tes tidak otomatis lulus penilaian visual. Semua ID membutuhkan bukti sesuai sifat perubahan. Gate implementasi visual belum dijalankan pada tahap penulisan rencana ini; jangan melabeli rencana sebagai UI lulus. Tidak membuat klaim PASS pada layar/state yang tidak diperiksa atau pada perangkat yang tidak diuji.

## 22. Instruksi mulai untuk agen pelaksana

Baca dokumen ini sampai selesai, lalu:

1. Jalankan preflight P00 dan pastikan tidak bertabrakan dengan writer/verifier yang sedang aktif.
2. Gunakan dan perbarui tracker existing yang mempertahankan **seluruh 126 subtask: 77 pada N01–N13 dan 49 pada N14–N20**, ditambah langkah integrasi dan pengujian lintas bagian; jangan menghapus atau mengecilkan daftar. Batas nomor tiap anotasi berbeda, sehingga jangan mengasumsikan semuanya hanya enam subtask.
3. Catat keputusan tempo pemilik secara konsisten pada checkpoint/aturan proyek yang relevan: **tempo tunggal 1 hari game = 1 jam nyata saat aktif/tertutup, tanpa speed/skip; efek berita tetap satu–dua jam nyata.**
4. Verifikasi fondasi waktu yang sudah dikerjakan sebelum menggantinya. Jangan memulai game dari nol atau mengimpor save/source checkout lama.
5. Kerjakan checkpoint terukur untuk ID dalam file plan.md yang dikirim pemilik menurut bagian 18. Tunjukkan progres x/20 paket dan y/126 subtask. Setelah seluruh ID/deliverable file itu tuntas, BERHENTI dan tunggu kiriman file berikutnya; jangan menjalankan seluruh master otomatis.
6. Perubahan visual kecil yang jelas putuskan sendiri sesuai `DESIGN.md`; catat asumsi. Konflik ekonomi nyata, aturan replay yang belum didukung, atau perluasan native/layanan baru harus dinyatakan secara spesifik.
7. Tutup setiap ID hanya setelah bukti penerimaannya ada. Simpan temuan yang belum lulus secara terbuka dan pertahankan batas host/browser/APK/perangkat.

**Baca juga bagian 23–32. Kontrak final bagian 0, revisi N01/N11/N13 dan N14–N20 menggantikan instruksi lama yang bertentangan; pembatalan eksplisit tetap ditelusuri di tracker.**

## 23. Analisis tambahan dari anotasi, source, dan referensi

### 23.1 Uji stres: penjelasan dan keputusan akhir

Source yang dibaca: `www/app.js: openStressTestModal`, `TradingWebBridge.getStressTestScenarios/runStressTest`, `TradingFacade.runStressTest`, `core/replay/HistoricalReplayEngine.runStressTest`, `StressTestScenario`.

Fitur menghitung proyeksi nilai akun pribadi di bawah skenario: harga saham turun 30%, atau kurs USD/IDR naik 35% bersamaan harga aset turun 10%. Kas IDR tidak dikalikan penurunan harga saham; kas USD dan posisi USD dinilai dengan kurs skenario; utang margin dikurangkan. Implementasi memeriksa rasio ekuitas stres terhadap total aset untuk indikasi margin call. Tidak ada perintah jual/beli atau pemaksaan perubahan pasar dari jalur perhitungan yang dibaca.

Contoh penjelasan konseptual: kas Rp5 juta + saham Rp10 juta, tanpa utang, menjadi proyeksi Rp12 juta pada skenario saham -30%, sehingga potensi penurunan Rp3 juta. Ini contoh aritmetika fitur, bukan data akun pemain atau prediksi pasar nyata. Nama skenario 2008 tidak membuktikan rekonstruksi historis; deskripsi likuiditas terbatas belum tercermin dalam perhitungan antrean/slippage yang dibaca. Menyebut hasil tanpa margin call sebagai pasti solven juga terlalu luas untuk model yang belum mencakup seluruh kewajiban/aset.

**Keputusan final: hapus akses uji stres dari UI N19**, karena alat ini hanya perhitungan terpisah dan pemilik menghendaki fokus perdagangan. Jangan menghapus riskService, kesehatan margin, audit, asuransi atau likuidasi sungguhan yang memengaruhi perdagangan utama. Jangan mengembangkan kalkulator stres baru sebagai pengganti.

### 23.2 Lab strategi: tidak mengubah perdagangan utama

`TradingFacade.createStrategySandbox/strategyLab` memakai `StrategyLabSandbox`, akun klon, ledger sandbox dan riwayat keputusan terpisah. Namun daftar `strategyLabs` ikut export/restore state dan beberapa tindakan memanggil persistence. Jadi “tidak memengaruhi perdagangan utama” tidak berarti “tidak punya dependency save”.

**Keputusan final: hapus UI/launcher lab.** Audit callback, bridge dan restore sebelum membuang kode. Pertahankan pembacaan state lama bila dibutuhkan; hapus kode mati hanya bila seluruh caller dan kompatibilitas telah diverifikasi. Tidak menghapus replay historis perdagangan utama, corporate actions, data OHLCV atau suite pengujian hanya karena folder/class bernama replay.

### 23.3 Pemegang saham: field existing belum cukup

`core/market/IssuerFundamentals.java` memiliki `sharesOutstanding`, `freeFloatShares`, `freeFloatPercent`, `totalShareholdersCount`, dan `majorShareholders`. Pada source yang diperiksa, free float diisi 40%, jumlah pemegang dibangkitkan dari seed, dan `majorShareholders` berisi teks tetap Pendiri 55% / Lembaga 25% / Publik 20%. **Itu tidak membuktikan siapa yang memiliki saham dalam state game saat ini.** Jangan menyalin teks tersebut ke kartu baru lalu mengklaim data pemegang saham telah selesai.

`Instrument.getSharesOutstanding()` dan posisi akun adalah titik penelusuran awal, bukan bukti tersedia cap table lengkap. N15 harus menghubungkan komposisi ke kepemilikan domain yang aktual, membedakan pemilik legal/akun kelolaan, memperhatikan settlement, aksi korporasi, dan cakupan data. Jangan mengisi nama institusi nyata atau persentase kosmetik dari UI.

### 23.4 Grafik: rentang bukan resolusi candle

`www/app.js: openInstrumentDetail/renderChart` saat diperiksa memakai M1/M5/M15/H1/D1 sebagai interval bar. `TradingFacade.getChartData` mengelompokkan fill menurut interval; beberapa jalur D1 mengambil 30 bar dataset AAPL/BBCA, dan saat tanpa fill bisa membuat bar acuan. Label seperti Sesi 1 tidak cukup untuk memfilter riwayat 6 jam/7 hari secara akurat.

N14 memerlukan timestamp bar yang nyata, filter relatif terhadap clock dunia, resolusi otomatis dan persistensi riwayat yang memadai. Jangan sekadar mengganti tulisan M1 menjadi 6 jam. Audit pemisahan dataset historis dan pasar internal; jangan mencampur keduanya agar grafik tampak ramai. Informasi source boleh disimpan untuk audit tanpa mengembalikan blok teknis yang ditolak N06.

### 23.5 Stockbit, Ajaib, dan REA

Stockbit: gambar detail emiten resmi dalam bantuan Chartbit benar-benar dilihat. Tampilan contoh menekankan ticker/harga, chart, rentang, aksi Buy, dan tab informasi. Ajaib: halaman saham resmi dan contoh produk dalam hero benar-benar dilihat; dokumen resminya menjelaskan order book, eksekusi, serta informasi pemegang saham. Gambar pemegang saham yang dibuka terpisah diblokir browser, sehingga rincian layar tersebut tidak diklaim telah diinspeksi. Ini pemeriksaan referensi publik, bukan audit aplikasi setelah login.

Prinsip yang diambil: hierarki angka, konteks instrumen/akun, pemisahan data ke tab/kategori, dan kedekatan keputusan dengan tindakan. Jangan menyalin merek, ikon, saldo, biaya, janji kecepatan, status legal, atau persentase pemegang saham broker ke game. Palet terang di N16 adalah usul untuk game sendiri, bukan warna resmi Stockbit/Ajaib. Referensi broker bukan instruksi menghapus karakter game.

REA existing tetap referensi terbatas. `Artifacts/REA/evidence/dart-aot.json` membedakan ekstraksi token dari source yang dipulihkan. Gunakan screenshot/analisis yang sudah tersedia bila relevan; tidak mengakuisisi APK/perangkat baru, tidak mengklaim algoritme waktu atau ekonomi game lain telah berhasil dipulihkan. Daftar pustaka desain terbuka ada di bagian 31; akses publik tidak otomatis lisensi redistribusi aset.

## 24. N14: Empat rentang grafik yang benar-benar memfilter riwayat

### 24.1 Kontrak data dan tampilan

Tampilkan tepat **6 jam | 1 hari | 7 hari | 1 bulan**. Default tiap pembukaan detail baru adalah 1 hari; refresh pada detail yang sama tidak mengubah pilihan pengguna. Seluruh rentang dihitung dalam kalender dunia game, bukan waktu nyata. Bulan = rolling 30 hari, bukan bulan kalender berjalan. Pilihan Candle/Garis tetap berfungsi; tabel OHLCV dan blok source/frame tidak kembali.

Rentang berakhir pada timestamp dunia yang diketahui; gunakan satu kebijakan konsisten untuk batas awal/akhir, misalnya start <= tradeTime <= observedNow, tanpa menggandakan fill antar bucket. Bar terakhir boleh parsial dan ditandai sewajarnya. Jangan memperlihatkan harga atau high/low/close masa depan pada replay.

Resolusi otomatis yang disarankan: 6 jam → 5 menit/candle; 1 hari → 15 menit/candle; 7 hari → 4 jam/candle; 30 hari → 1 hari/candle. Ini rekomendasi teknis, bukan empat tombol tambahan. Hermes boleh memilih granularitas setara berdasarkan data dan ukuran layar, mencatat alasannya, dan tidak mengubah arti rentang. Bila resolusi 4 jam belum didukung, perluas agregasi existing minimum; jangan membuat chart engine kedua.

Java menyediakan timestamp bucket mentah serta OHLCV. Gunakan epoch/zone kontrak existing; jangan memfilter dengan mem-parse string Sesi 1 atau jam tanpa tanggal. Agregasi memakai fill terurut, open pertama, close terakhir, high maksimum, low minimum dan volume jumlah transaksi yang sah. Double hitung sisi beli/jual dari fill yang sama dilarang.

Riwayat kosong/kurang dari rentang ditampilkan jujur. Gap pasar tutup tidak berarti error; jangan membuat perdagangan atau volume fiktif. Data harian tidak boleh diubah menjadi candle intraday palsu. Harga acuan yang tersedia harus dibedakan dari aktivitas yang benar-benar terjadi tanpa menambah disclaimer simulasi panjang.

Jika history in-memory terlalu pendek, simpan agregat domain minimum yang diperlukan untuk horizon 30 hari dan restore konsisten. Jangan menyimpan semua tick selamanya, menambah database/service baru, atau mengganti format save tanpa kebutuhan. Periksa apakah data existing sudah memadai sebelum menambah struktur. Operasi membaca chart tidak boleh memajukan waktu atau mengubah uang.

### 24.2 Tugas

- [ ] N14-01 Telusuri kontrak getChartData, fill timestamps, replay, retensi history dan seluruh caller grafik.
- [ ] N14-02 Pisahkan konsep rentang dan interval; tentukan timestamp, zona, batas dan resolusi otomatis.
- [ ] N14-03 Implementasikan filter/agregasi empat rentang pada domain/shared helper yang sesuai, tanpa future leak.
- [ ] N14-04 Hubungkan empat kontrol saja, default 1 hari, dan pertahankan pilihan saat refresh/Candle-Garis.
- [ ] N14-05 Pastikan retensi/restore cukup atau state kurang data jujur; hapus fallback yang memalsukan history pada jalur baru.
- [ ] N14-06 Perbaiki sumbu tanggal/jam, ringkasan candle terpilih, gap/partial/empty dan akses keyboard/sentuh.
- [ ] N14-07 Uji bucket boundaries, OHLCV/volume, weekend/sesi, lintas hari, instrumen, mode, reload dan data tidak berurutan.
- [ ] N14-08 Verifikasi browser 100%/200%, performa 30 hari, source/DTO/visual yang sama dan N05/N06 tidak kembali.

Penerimaan: dua rentang berbeda menghasilkan himpunan waktu yang berbeda bila data tersedia; OHLCV direkonsiliasi ke fill input; tidak ada candle masa depan atau volume palsu; seluruh rentang tetap dapat dipilih saat data kurang dengan penjelasan singkat. Simpan input terkontrol, expected buckets, output, screenshot dan timing. Konversi konseptual pada tempo baru: 6 jam game = 15 menit nyata; 1 hari = 1 jam nyata; 7 hari = 7 jam nyata; 30 hari = 30 jam nyata. Label kontrol tetap waktu game.

## 25. N15: Komposisi pemegang saham dunia game

### 25.1 Pemisahan dua informasi

Detail emiten memiliki bagian **Pemegang saham** yang tetap berguna ketika pemain belum memiliki saham, serta bagian **Posisi saya** hanya untuk akun terpilih. Jangan mengganti daftar posisi pribadi Portofolio dengan daftar institusi. Istilah stakeholder di komentar ditafsirkan melalui klarifikasi pemilik sebagai pemegang saham, bukan semua pihak berkepentingan seperti pemasok/karyawan.

Komposisi memuat nama pemegang atau kelompok yang benar-benar tercatat, jumlah saham, porsi terhadap saham beredar jika denominator sah, dan waktu state. Tandai kepemilikan pemain secara jelas; akun klien tidak digabung ke milik pribadi. Nama NPC/institusi harus berasal dari identity domain; jangan memakai nama pemegang saham perusahaan asli. Tidak perlu jaringan/data pasar nyata untuk fitur ini.

Telusuri semua akun/registri: pemain, NPC, institusi, issuer/treasury, kelolaan klien dan dana. Definisikan siapa holder of record agar saham dalam dana tidak dihitung ulang sebagai saham milik pemegang unit. Agunan/reservasi order tidak menciptakan pemilik kedua. Short/borrowed shares memerlukan semantik domain yang konsisten; posisi short bukan porsi kepemilikan positif baru.

Porsi memakai sharesOutstanding emiten yang sama dan snapshot yang sama, bukan nilai portofolio atau nilai market cap sektoral. Jangan memakai free float sebagai penyebut kepemilikan total. Jangan paksa total 100% dengan membulatkan baris terakhir atau mengarang Publik. Bila hanya sebagian owner terpetakan, tampilkan cakupan yang benar dan nilai yang tersedia; bagian belum terpetakan bukan nama investor tertentu. State parsial tidak boleh ditandai memenuhi registri lengkap.

Jika source belum memiliki registri lengkap, Hermes menambah representasi domain terkecil yang cukup terintegrasi dengan settlement dan save; dokumentasikan bagaimana baseline legacy dipetakan. Tidak mengubah quantity, kas, outstanding atau ekonomi hanya agar pie chart berjumlah 100%. Bila konservasi supply belum dapat direkonsiliasi, catat temuan konkret dan minta keputusan model yang diperlukan; selesaikan UI dan jalur independen tanpa data palsu.

### 25.2 Tugas

- [ ] N15-01 Inventaris sumber posisi, NPC/institusi, outstanding/free float, corporate actions dan identitas holder.
- [ ] N15-02 Definisikan holder of record, penyebut porsi, cakupan data, short/reservasi dan akun kelolaan tanpa hitung ganda.
- [ ] N15-03 Pakai/perluas domain minimum untuk komposisi aktual; jangan mengangkat majorShareholders hardcoded sebagai fakta dinamis.
- [ ] N15-04 Integrasikan peralihan kepemilikan pada fill/settlement, pembatalan, split/rights/buyback dan restore yang relevan.
- [ ] N15-05 Sediakan DTO numerik eksplisit dengan instrument/holder ID, jumlah/porsi valid, as-of dan status cakupan.
- [ ] N15-06 Buat panel pemegang saham terpisah dari Posisi saya, termasuk state pemain nol, partial/unknown/loading/error.
- [ ] N15-07 Uji konservasi, denominator nol/invalid, rounding, banyak holder, akun klien, corporate action dan reload.
- [ ] N15-08 Rekonsiliasi setiap angka ke domain; uji live refresh, focus, 200%, serta tidak mengubah posisi pribadi N10.

Penerimaan: emiten tanpa saham pemain masih menampilkan pemegang yang sah bila tersedia; setelah fill jumlah dan porsi berubah sesuai settlement; canceled/unfilled order tidak memindahkan kepemilikan; tidak ada duplikasi pemilik, porsi negatif/lebih dari 100% yang disembunyikan, atau data asli perusahaan yang diada-adakan. Grafik komposisi opsional; daftar angka yang benar cukup.

## 26. N16: Redesain menyeluruh dengan tema terang

### 26.1 Arah visual dan palet

Gunakan Antislop core/UI/human/layoutmobile/copywriting mode during sebagai filter, serta prinsip Ponytail pada implementasi. Arah yang dibaca dari jawaban pemilik: aplikasi broker terang, terstruktur dan mudah dipindai pada trading; karakter taipan hidup pada karier/progres/hadiah. Dials usulan untuk disetujui bersama contoh: trading ENERGY 2 / RHYTHM 2 / MOTION 1; progres ENERGY 3 / RHYTHM 2 / MOTION 3, dengan satu identitas dan token dasar yang sama.

| Peran | Warna usulan | Pemakaian |
|---|---|---|
| Canvas | `#F3F6FA` | Latar halaman lembut, bukan putih menyilaukan pada semua permukaan. |
| Surface | `#FFFFFF` | Konten utama, tabel, formulir dan sheet. |
| Surface sekunder | `#E8EEF5` | Pemisahan kelompok, kontrol nonaktif yang masih jelas dan lapisan konteks. |
| Teks utama | `#162332` | Judul, nilai, identitas saham. |
| Teks sekunder | `#526175` | Nama emiten, label dan informasi pendukung. |
| Aksi trading/pasar | `#165DCC` | CTA utama dan pilihan aktif; teks putih bila kontras telah diuji. |
| Positif | `#087F5B` | Untung/naik/sukses dengan tanda dan teks; bukan dekorasi semua tombol. |
| Negatif | `#C22B43` | Rugi/turun/error dengan tanda, label dan alasan. |
| Bisnis/dana | `#6D40AF` | Aksen kecil pada kategori bisnis/dana, bukan hero ungu di semua halaman. |
| Karier/hadiah | `#8A5A00` di `#FFF5D9` | Progres, milestone dan hadiah sah. Warna emas pucat tidak dipakai sebagai teks di putih. |
| Panel pasar | `#EAF2FF` | Konteks kategori, dipasangkan teks gelap/biru yang lulus kontras. |
| Panel bisnis | `#F3EDFC` | Pengelompokan bisnis/dana; tidak berarti untung/rugi. |

Warna ini usulan konkret untuk 2–3 contoh, bukan klaim palet resmi broker atau persetujuan screenshot final. Hermes memeriksa seluruh pasangan teks/permukaan, tombol, selected/disabled, grafik dan focus. Target proyek: teks normal >=4,5:1, teks besar >=3:1, kontrol/indikator penting >=3:1 jika berlaku. Target sentuh minimal 48 sesuai standar proyek. Makna untung/rugi tidak berubah antar halaman. Satu konteks layar tidak memakai semua aksen sekaligus.

### 26.2 Cakupan wajib

Audit seluruh root, detail, subtab, form, tinjauan, hasil, sheet/modal, notifikasi, pengaturan, serta state loading/empty/error/stale/locked/pending. Cakupan termasuk Beranda, daftar pasar, detail/chart/order book, ticket/receipt, posisi, dompet/FX, investasi lain, utang/margin, corporate actions/voting, usaha/tim, mandat/fund builder, koleksi, profil/karier/misi/liga, riset/paket dan save/recovery. Inventaris aktual lebih penting daripada jumlah contoh layar.

Daftar saham menekankan ticker/nama, harga dan perubahan yang sejajar. Form menekankan akun, jenis/side, input relevan, estimasi dan review. Tabel memakai alignment numerik dan header jelas. Detail menyajikan ringkasan → data utama → aksi; informasi sekunder di tujuan berlabel. Kurangi card-in-card, judul kapital berulang, chevron pada panel pasif, padding besar dan glow seragam. Jangan mengganti monoton gelap menjadi monoton putih dengan semua kartu identik.

Pakai font lokal yang terbaca dan angka tabular; jangan menambah font jaringan/library UI. Aset existing yang punya fungsi dipakai ulang. Ikon harus konsisten dan berlabel; jangan memakai emoji sebagai substitusi ikon seluruh kategori. Ukuran uang tidak dikecilkan agar muat, melainkan reflow.

Motion progres/hadiah boleh merayakan perubahan status nyata, misalnya progress fill, pencapaian muncul, atau hadiah telah diterima. Jangan merayakan order pending seolah filled. Animasi tidak menunda input/review atau menyembunyikan angka. Tidak ada loop confetti/glow, getaran/suara baru yang tidak diminta, atau animasi setiap tick. Reduced motion mempertahankan hasil yang sama tanpa gerakan nonesensial.

### 26.3 Tugas

- [ ] N16-01 Inventaris seluruh route/component/state dan screenshot baseline; petakan komponen bersama serta live updater.
- [ ] N16-02 Turunkan token tema terang, kategori, angka, spacing, radius, ikon dan motion dari contoh yang disetujui.
- [ ] N16-03 Terapkan shell/header/nav yang disetujui dengan reflow, safe area, scroll dan focus yang benar.
- [ ] N16-04 Redesain seluruh layar pasar/trading/detail/form/review/receipt dengan kontrak Java tetap benar.
- [ ] N16-05 Redesain seluruh fitur Portofolio/Bisnis/Lainnya dan halaman turunannya sesuai inventaris, N17/N18.
- [ ] N16-06 Terapkan state interaksi serta motion progres/hadiah yang berbasis outcome nyata dan reduced motion.
- [ ] N16-07 Audit kontras, keyboard, teks 200%, data panjang, numeric alignment, viewport termasuk 360x800 dan 470x658.
- [ ] N16-08 Jalankan click-through semua tujuan yang dipertahankan, pemeriksaan visual aktual, performa dan empat blok Delivery Gate.

Penerimaan: setiap layar/state mempunyai hierarki dan bukti, kategori dapat dibedakan tanpa warna saja, tidak ada dark CSS/inline style yang membuat teks hilang pada tema terang, tidak ada navigasi mati atau perubahan keuangan demi visual. Pengaturan high contrast/reduced motion existing tetap berfungsi atau dimigrasikan secara eksplisit. Tidak membangun dual-theme baru karena pemilik memilih terang.

## 27. N17: Subkategori Portofolio yang relevan

### 27.1 Peta awal untuk proposal navigasi

Halaman awal menampilkan kekayaan, kas tersedia dan posisi yang penting. Di bawahnya daftar kategori, bukan seluruh alat. Setiap kategori mempunyai halaman sendiri dan keterangan pendek. Final placement mengikuti persetujuan N20 tanpa kehilangan fitur.

| Subkategori | Isi yang relevan | Batas pemisahan |
|---|---|---|
| Saham & order | Posisi, order aktif, riwayat transaksi saham | Tidak mencampur pinjaman, koleksi atau profil. |
| Dompet & valuta | Saldo IDR/USD, dana tertahan, FX, riwayat kas | Kas pribadi dan klien tetap dibedakan. |
| Reksa dana | Unit/NAB, subscribe/redeem, antrean dan riwayat | Fund builder kelolaan bukan otomatis investasi pribadi. |
| Obligasi & deposito | Posisi, jatuh tempo, kupon/bunga, pencairan | Jika komoditas punya flow berbeda, buat subkategori sendiri. |
| Komoditas | Kepemilikan, quote, beli/jual | Jangan memaksakan satu formulir untuk produk berbeda. |
| Pinjaman & cicilan | Pinjaman tenor, agunan, jadwal, prepay/restrukturisasi | Tidak disamakan dengan margin saham. |
| Margin & proteksi | Margin health, pinjaman saham/short, asuransi yang benar-benar berlaku | Uji stres tidak tampil. |
| Pajak & laporan | Audit, ledger/laporan akun dan kepatuhan relevan | Bukan direktori seluruh fitur lain. |
| Properti & koleksi | Kepemilikan, valuasi, harga dan aksi ekonomi | Tetap ada karena memengaruhi uang/progres. |
| Usaha & dana kelolaan | Shortcut usaha/klien/dana ke tujuan kanonis | Jangan menduplikasi seluruh halaman Bisnis. |

Daftar di atas adalah pemetaan awal, bukan kewajiban membuat sepuluh tab sejajar. Gunakan kategori bertingkat seperlunya dengan judul yang tetap terbaca. Jika satu kategori terlalu luas, pecah berdasarkan tujuan/objek. Jika hanya satu fitur kecil, launcher langsung boleh lebih sederhana daripada halaman kosong satu tombol. Hindari horizontal chip row panjang yang menyembunyikan kategori.

### 27.2 Tugas

- [ ] N17-01 Kelompokkan inventaris Portofolio berdasarkan objek/tindakan/akun; tandai fitur yang salah kelompok dan shortcut duplikat.
- [ ] N17-02 Ajukan peta kategori/subkategori serta tujuan kanonis dan rute balik sebagai bagian persetujuan N20.
- [ ] N17-03 Implementasikan landing ringkasan dan daftar kategori; pindahkan fitur ke halaman relevan tanpa accordion penampung.
- [ ] N17-04 Pertahankan semua handler, syarat, review, konteks akun, filter dan posisi scroll; cabut hanya removal N19.
- [ ] N17-05 Verifikasi setiap fitur keep/move/remove-approved dengan klik nyata, Back, direct route dan reload.
- [ ] N17-06 Uji empty/error/locked, banyak posisi/aset, 200%, layar kecil dan live update tanpa lompatan layout.

Penerimaan: tidak ada daftar puluhan alat dalam satu bagian, tiap kategori mempunyai alasan pengelompokan yang dapat dijelaskan, fitur yang tidak relevan dipisahkan, dan seluruh jalur uang/progres tetap ada. Jangan menambah pencarian/shortcut favorit baru jika struktur kategori sudah cukup.

## 28. N18: Lainnya dan seluruh fiturnya diperbarui

Kelompok Profil memuat identitas, statistik/gaya investasi, level/pencapaian dan progres yang relevan. Misi & Kompetisi memuat misi harian/mingguan, liga, rival/peringkat dan klaim sah. Riset memuat terminal, berita/analisis, paket dan pengembangan yang memengaruhi perdagangan. Pengaturan memuat preferensi, panduan singkat, simpan/muat/recovery serta tindakan run baru yang dilindungi konfirmasi. Penempatan Era Baru mengikuti usulan progres, bukan tercampur dengan subscription.

Gunakan daftar ringkas, ikon fungsional, judul dan satu baris keterangan. Satu atau dua ringkasan progres boleh mendapat penekanan visual; semua menu tidak menjadi kotak gelap/berwarna besar yang sama. Kelompok riset tidak perlu memakai istilah internal/domain, dan istilah trading umum tetap dipakai tanpa paragraf tutorial.

Langganan menunjukkan masa aktif dan biaya nyata domain; misi menunjukkan target/progres/hadiah actual; liga menjelaskan periode dan posisi yang valid; profil tidak memamerkan angka palsu untuk mengisi ruang. State terkunci bisa membuka syarat, tidak menyamar sebagai fitur rusak. Era Baru/reset tidak dipicu saat membuka menu. Animasi hadiah hanya setelah reward domain sukses dan tidak menggandakan klaim.

- [ ] N18-01 Peta semua tujuan Lainnya dan flow turunannya ke empat kelompok final.
- [ ] N18-02 Rancang hub daftar ringkas dengan hierarchy, aksen kategori dan ringkasan progres yang sesuai tema terang.
- [ ] N18-03 Perbarui detail profil/misi/liga/riset/paket/pengaturan, bukan launcher saja.
- [ ] N18-04 Integrasikan feedback locked/pending/error dan motion klaim/reward sesuai hasil domain.
- [ ] N18-05 Pertahankan syarat akses, biaya langganan, periode misi/liga, konfirmasi reset dan save.
- [ ] N18-06 Uji setiap journey normal/kosong/gagal/terkunci, Back/fokus/200%, klaim ganda dan refresh.

Penerimaan: pengguna dapat membedakan profil, kompetisi, riset dan pengaturan; seluruh tujuan serta rincian menggunakan bahasa visual baru, angka sesuai domain, dan tidak ada akses lab/uji stres terselip di hub lain.

## 29. N19: Hapus UI eksperimen tanpa merusak fitur inti atau save

Removal yang telah disetujui: lab strategi & replay keputusan sandbox, serta uji stres portofolio makro. Hapus launcher di seluruh root/shortcut, opener/rute akses produk, empty-state prompt dan copy yang mempromosikannya. Menghapus dua tombol di Portofolio saja belum cukup jika Beranda/Riset masih bisa membuka flow.

Tetap pertahankan replay historis yang menjadi mode perdagangan, akun/ledger/clock, risiko margin, pinjaman, asuransi, likuidasi, bisnis, karier, misi, liga, koleksi/properti dan investasi yang memengaruhi uang/progres. Kata stress dalam test runner atau regression scenario bukan fitur uji stres pemain. Kata replay pada API bisa mempunyai caller fitur inti; telusuri semantik, bukan hapus berdasarkan nama folder.

Backend lab/stres boleh tetap sebagai kompatibilitas/internal bila dipakai restore atau shared dependency; dokumentasikan alasan. Kode yang terbukti mati boleh dibersihkan secara terarah, tanpa membersihkan data pemain secara destruktif. Tidak menambah tugas penghapusan fitur lain atas tafsir pribadi. Temuan fitur di luar dua removal ini masuk proposal terpisah bila benar-benar perlu keputusan.

- [ ] N19-01 Telusuri seluruh launcher, route, handler, bridge, persistence dan pemakai bersama lab/uji stres.
- [ ] N19-02 Buat daftar keep/remove-ui/compatibility-only/dead-code beserta alasan dan sumber keputusan pemilik.
- [ ] N19-03 Hapus akses UI lab dan uji stres di seluruh layar, navigasi, shortcut dan tautan internal.
- [ ] N19-04 Bersihkan state/listener/CSS/copy mati yang tidak punya pemakai lain; jangan menghapus engine bersama.
- [ ] N19-05 Pertahankan migrasi/load legacy strategyLabs dan recovery; uji save berisi lab lama pada salinan.
- [ ] N19-06 Uji perdagangan utama, replay historis, margin/likuidasi dan seluruh fitur uang/progres yang dipertahankan.
- [ ] N19-07 Verifikasi tidak ada route/tab stop/CTA eksperimen tersisa, save pribadi utuh dan tracker memuat removal yang sah.

Penerimaan: tidak ada cara membuka kedua flow dari UI produk; data save lama masih dapat dibaca tanpa reset; fitur inti tidak berubah hanya karena berbagi package/class; tidak menghapus warning risiko transaksi yang masih penting.

## 30. N20: Persetujuan contoh, rekonsiliasi spesifikasi, dan rollout

### 30.1 Titik persetujuan yang diwajibkan pemilik

Sesudah preflight, Hermes membuat **2–3 contoh layar utama** dari arah terang, misalnya Beranda, detail saham/order, dan Portofolio berkelompok. Sertakan peta navigasi utama/subkategori, token palet, satu contoh progres/hadiah, serta alasan yang menghubungkan perubahan dengan anotasi. Ini contoh layar, bukan kewajiban membuat tiga desain produk yang berbeda. Boleh memakai data dummy yang jelas ditandai untuk contoh; tidak mengaku hasil domain produksi.

Contoh ditempatkan pada output desain terisolasi atau branch/file yang sesuai dan dapat ditinjau; tidak mengganggu save pribadi atau diam-diam merombak seluruh production UI. Tampilkan hasil yang konkret sebelum meminta persetujuan. Pemilik menyetujui arah dan struktur terlebih dahulu. Tidak ada jawaban, timeout, atau terpilihnya opsi default yang dianggap persetujuan.

Jika diminta revisi, perbaiki contoh dan minta persetujuan pada perubahan material. Sesudah disetujui, terapkan ke seluruh inventaris UI secara konsisten; tidak meminta izin ulang untuk tiap spacing/label kecil. Bila navigasi berubah material lagi, ajukan delta yang jelas.

### 30.2 Tugas

- [ ] N20-01 Rekonsiliasi keputusan final dengan AGENTS/DESIGN/standar fase/fixture dan inventaris requirement tanpa menimpa keputusan terbaru.
- [ ] N20-02 Susun peta navigasi dan 2–3 contoh layar tema terang beserta state/motion progres yang representatif.
- [ ] N20-03 Tampilkan contoh dan minta persetujuan pemilik atas arah visual, navigasi, serta kategori; catat status menunggu secara jujur.
- [ ] N20-04 Setelah persetujuan, perbarui keputusan aktif DESIGN.md/aturan proyek terkait dan implementasikan hanya struktur yang disetujui.
- [ ] N20-05 Audit seluruh 126 ID, coverage komentar/fitur/route, perubahan kontrak lama dan bukti pada snapshot final.
- [ ] N20-06 Serahkan checkpoint terintegrasi, hasil pengujian, preview terbaru, APK kandidat terverifikasi dan batas host/native yang jelas.

### 30.3 Matriks penerimaan tambahan

| ID | Bukti penutupan |
|---|---|
| N14 | Empat rentang, default, timestamp/buckets, OHLCV/volume, no future leak, retensi/reload, browser dan performa. |
| N15 | Sumber holder/denominator, konservasi dan akun, settlement/corporate actions, DTO/UI, data parsial jujur. |
| N16 | Coverage seluruh route/state, token terang, visual review, 200%, keyboard/kontras/motion, click-through dan Delivery Gate. |
| N17 | Map setiap fitur ke kategori kanonis, tidak ada fitur keep yang hilang, akun/Back/scroll/live update terjaga. |
| N18 | Empat kelompok dan seluruh detail, lock/progres/paket/reward/reset benar, tampilan dan interaksi konsisten. |
| N19 | Kedua flow tidak terjangkau, kompatibilitas legacy lulus, fitur inti/shared domain tidak terhapus. |
| N20 | Persetujuan nyata tercatat, dokumen kontrak konsisten, tracker lengkap, hasil final cocok source. |

Regresi tambahan: tempo 24x untuk semua lifecycle; tidak ada kontrol skip yang tersisa; efek berita 1–2 jam nyata; seluruh rentang grafik memakai waktu game; komposisi pemegang saham tidak memakai placeholder; tema terang tidak membuat chart/dialog/form gelap tak terbaca. Tes legacy tentang dual-rate, skip dan launcher eksperimen disesuaikan secara eksplisit ke requirement baru tanpa menghapus uji keamanan/save yang masih berlaku.

## 31. Referensi primer dan penerapannya

Sumber publik diperiksa 24 Oktober 2026 WIB. Ini referensi UI dan dokumentasi, bukan rekomendasi memilih broker atau meniru aturan ekonominya. Tangkapan pemasaran/bantuan dapat mewakili versi tertentu; jangan menyebutnya bukti semua state aplikasi terbaru setelah login.

1. [Stockbit: penggunaan Chartbit](https://help.stockbit.com/id/article/chartbit-bagaimana-cara-menggunakan-dan-apa-fungsinya-1n4gg87/). Dipakai untuk alur emiten → chart, fokus harga, kontrol rentang dan pemisahan tab; gambar mobile resmi dilihat. Tidak mengimpor layanan cloud/data live.
2. [Stockbit: Portfolio Performance](https://help.stockbit.com/id/article/performance-apa-itu-portfolio-performance-dan-bagaimana-cara-melihatnya-1m46xja/). Rujukan pemisahan posisi/performa/alokasi; rumus dan data game tetap dari Java.
3. [Ajaib: halaman produk saham](https://ajaib.co.id/saham). Rujukan kedekatan chart/order book/aksi dan pemisahan informasi emiten/pemegang saham; bukan sumber data pemegang saham dunia game.
4. [Ajaib: Portfolio Quick Trade](https://ajaib.co.id/belajar/fitur-update/cara-eksekusi-order-saham-lebih-cepat). Rujukan hubungan daftar posisi dengan tindakan transaksi yang tetap menampilkan konteks posisi. Desktop perlu diadaptasi untuk ponsel, bukan disalin.
5. [Material Design 3: color roles](https://m3.material.io/m3/pages/color-roles?s=m3). Dasar perbedaan surface/container/foreground dan aksen berdasarkan kepentingan. Tidak perlu menambah dependency Material.
6. [Material: navigation](https://m2.material.io/design/navigation/). Dasar membedakan tujuan utama dengan tujuan bertingkat; struktur final tetap menunggu persetujuan.
7. [W3C: Contrast Minimum](https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html) dan [WCAG 2.2](https://www.w3.org/TR/WCAG22/). Dasar pengujian kontras, keyboard, focus tak tertutup dan reflow. Standar sentuh proyek 48 tetap dipakai, tidak diklaim sebagai minimum WCAG 2.2 universal.
8. [TradingView: time intervals](https://www.tradingview.com/support/solutions/43000747934-time-intervals-how-much-market-activity-each-candle-shows/). Menjelaskan durasi per candle. Keputusan pemilik di N14 menggunakan rentang terlihat, sehingga kedua konsep wajib dipisahkan.

Referensi berlisensi terbuka hanya dipakai sesuai lisensinya. Jangan mengunduh screenshot/merek broker untuk dipaketkan dalam APK, menambah framework, koneksi jaringan wajib, atau sistem autentikasi broker demi meniru tampilan.

## 32. Prompt eksekusi Hermes: salin bagian ini

```text
# MANDAT PEMILIK: EKSEKUSI MASTER PLAN GABUNGAN GAME SAHAM

Baca seluruh file ini sebagai spesifikasi tunggal:
C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM\master plan perbaikan.md

Target akhir adalah seluruh 20 paket N01–N20 dan 126 subtask: 77 ID lama yang
sudah direvisi dan 49 ID baru. Pelaksanaan pada satu kiriman dibatasi hanya pada
file plan.md yang saya kirim atau tunjuk secara eksplisit. Tuntaskan kode, tes,
bukti dan deliverable file itu, lalu BERHENTI menunggu file berikutnya dari saya.
Jangan menjalankan semua 126 task otomatis dari mandat global ini. Codex menyusun
rencana; Hermes menjalankan source sesuai file aktif.

Root tulis hanya C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM.
Game Trading Saham adalah checkout lama yang berbeda. Buktikan cwd/Git root,
branch/HEAD dan dirty state. Pertahankan perubahan lokal dan pemilik tiap file;
jangan reset/clean atau menimpa pekerjaan yang sedang berlangsung.

Keputusan terbaru menggantikan instruksi historis yang bertentangan:
1. Tempo tunggal 1 hari kalender game = 1 jam nyata saat aktif/background/ditutup,
   laju 24x, otomatis, tanpa play/pause pemain, speed atau skip. Preserve anchor,
   fraksi, migrasi, lifecycle, catchup dan save; Java satu otoritas waktu.
2. Efek berita tetap 60/120 menit nyata, deadline persisten, countdown benar,
   termasuk catchup melewati awal/akhir efek.
3. Grafik tepat empat rentang waktu game: 6 jam, 1 hari, 7 hari, 1 bulan=30 hari.
   Default 1 hari. Ukuran candle menyesuaikan. Jangan hanya mengganti label
   M1/M5 atau mengarang OHLCV. Hapus tabel alternatif dan blok source teknis.
4. Tema terang, trading rapi seperti pola aplikasi broker Stockbit/Ajaib,
   progres/hadiah terasa seperti game. Kategori punya aksen konsisten; untung/
   rugi tetap semantik. Gunakan istilah trading umum tanpa tutorial panjang.
5. Portofolio dipecah menjadi subkategori yang relevan. Lainnya berkelompok
   Profil, Misi & Kompetisi, Riset, Pengaturan. Seluruh layar turunannya ikut
   diperbarui, bukan hanya launcher atau warna shell.
6. Detail emiten menampilkan pemegang saham dunia game dari domain: pemain,
   NPC/institusi yang benar-benar tercatat. Posisi akun pribadi terpisah.
   Jangan memakai teks majorShareholders hardcoded sebagai distribusi aktual.
7. Hapus UI lab sandbox dan uji stres. Pertahankan bisnis/karier/misi/liga/aset
   yang memengaruhi uang/progres, risiko transaksi dan replay perdagangan utama.
   Jangan merusak save lama atau shared domain saat menghapus fitur UI.

SEBELUM MENYEBARKAN REDESAIN:
Siapkan 2–3 contoh layar utama tema terang dan usulan peta navigasi/kategori
yang konkret untuk saya tinjau. Minta persetujuan arah visual dan struktur.
Jangan menganggap diam/default/timeout sebagai persetujuan. Tunggu jawaban
untuk implementasi navigasi dan penerapan luas. Preflight, analisis serta
domain/test yang independen boleh lanjut ketika menunggu. Sesudah disetujui,
terapkan konsisten ke seluruh UI dan perbarui keputusan aktif DESIGN/AGENTS
yang bertentangan secara terarah, tanpa menghapus sejarah dan aturan data.

Mulai P00: baca AGENTS.md, DESIGN.md, Work/STATUS.json, standar kualitas fase,
dan seluruh master ini. Buat tracker lengkap 126 ID dengan dependency,
status, kriteria, implementasi, bukti, reviewer dan next_action. Periksa source
yang benar-benar ada; kondisi/hitungan historis bukan hasil verifikasi baru.
Rekonsiliasi seluruh caller/updater/bridge/save, pakai helper dan runner existing,
tanpa framework/dependency/abstraksi baru yang spekulatif.

Lindungi save pemain Artifacts/LocalPreview/preview-save/save_game_v2.json:
rekam hash sebelum/sesudah. Semua QA memakai save terisolasi. Tidak transaksi,
reset atau manipulasi waktu pada save pribadi. Tidak push/publish, mengubah
credentials/profil/model/provider/global settings, mengirim pesan ke chat lain,
ADB baru, instalasi APK atau pengujian perangkat baru dalam mandat ini.

Kerjakan preflight P00 lalu hanya kelompok dalam file plan.md yang saya kirim.
Urutan G01–G08 pada bagian 18 menjadi peta pengiriman manual. Persetujuan revisi 2
sudah tercatat. Sesudah file aktif selesai, kirim hasil dan BERHENTI; tunggu file
berikutnya dari saya. APK kandidat mengikuti kewajiban file aktif. Pakai Antislop core/UI/
human/layoutmobile/copywriting mode during. Buktikan semua route/state, teks
100%/200%, kontras, keyboard, Back/fokus, reduced motion, outcome transaksi,
idempotency dan kompatibilitas save. Jalur kritis wajib 10x100 aksi bermakna
dengan invariant per aksi. Controlled clock untuk tempo/deadline/migrasi.
Jangan menurunkan standar atau menelan exception agar hitungan lulus naik.

Setelah source stabil: regresi relevan/integrasi, review visual aktual,
performa sesuai standar proyek, APK fresh, metadata/signature/SHA256 dan
kecocokan byte aset dengan source. Source berubah saat tes berarti ulangi
cek terdampak. Pisahkan hasil domain/host, browser, APK build, instalasi,
emulator dan fisik. Native di luar scope tetap NOT_TESTED_BY_SCOPE.

Laporkan progres x/20 paket dan y/126 subtask pada checkpoint bermakna:
ID berjalan, file berubah, tes dan bukti, temuan, approval/blocker serta langkah
berikutnya. Simpan kegagalan awal dan rerun. Jangan menyatakan selesai dari
screenshot, tombol, dokumen atau APK saja. Bila terhambat, checkpoint lengkap
dan lanjutkan pekerjaan independen yang masih berada dalam file aktif. Selama
file belum tuntas, jangan berhenti hanya karena waktu/hemat token. Saat file telah
tuntas, BERHENTI menunggu file berikutnya dari pemilik sesuai mode pengiriman.

Penyerahan: source terintegrasi, tracker lengkap, before/after, bukti seluruh
gate termasuk empat blok Antislop, preview terbaru, APK kandidat terverifikasi,
hash save tetap utuh, batas native, temuan terbuka dan next_action. Jangan
membalas hanya rencana pengganti karena spesifikasi lengkap sudah tersedia.
```
