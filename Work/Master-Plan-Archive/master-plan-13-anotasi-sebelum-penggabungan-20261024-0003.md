# Master Plan Perbaikan Game Saham: 13 Anotasi Pemilik

Tanggal: 23 Oktober 2026, Asia/Jakarta.  
Tujuan: bahan eksekusi lengkap untuk AI agent yang mengerjakan proyek.  
Status: **RENCANA EKSEKUSI; bukan laporan bahwa seluruh perbaikan sudah selesai.**  
Root produksi: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`.  
Preview yang menjadi dasar anotasi: `http://127.0.0.1:18917/?v=automatic-world-20261023`.  
Branch saat pemeriksaan: `codex/classic-ui-new-assets`.  
HEAD saat pemeriksaan: `afd8b23581d0b760f96132dae29ad488849880c4`, 23 Oktober 2026 pukul 16.31.48 WIB.  
Source mempunyai perubahan lokal setelah HEAD, termasuk perubahan waktu otomatis dan pekerjaan agen lain. **HEAD tersebut saja tidak mengidentifikasi kode yang sedang dipreview.** Agen pelaksana wajib mencatat hash source aktual pada setiap checkpoint.

## 1. Mandat, keputusan terbaru, dan batas pekerjaan

Kerjakan **seluruh 13 anotasi** dalam dokumen ini. Setiap anotasi memiliki nomor sendiri, tugas, kriteria penerimaan, dan bukti tersendiri. Jangan menggabungkan beberapa anotasi lalu menyatakan semuanya selesai berdasarkan satu screenshot atau satu tes.

Dokumen ini disusun dari 13 komentar dan screenshot yang dikirim pemilik serta pemeriksaan source lokal. Pada tahap penyusunan dokumen, tidak dilakukan implementasi baru terhadap 13 anotasi ini. Beberapa fondasi waktu otomatis sudah diubah pada pekerjaan sebelumnya; agen berikutnya harus memeriksa, mempertahankan yang benar, dan menuntaskan integrasinya.

### 1.1 Keputusan pemilik yang mengikat

1. **Keputusan tempo terbaru:** pemilik menjawab, **“Pertahankan laju lama; haluskan tampilannya saja.”** Karena itu anotasi N01 tidak memberikan izin untuk mengubah game ke waktu nyata 1:1.
2. Laju dasar saat aktif tetap **1 hari game = 5 menit nyata**, yaitu 288 detik game per detik nyata pada 1×. Laju saat tidak aktif tetap **1 hari game = 1 jam nyata**, yaitu 24 detik game per detik nyata. Jangan membawa pengali aktif ke periode tidak aktif.
3. Dunia mulai otomatis dan tetap berkembang selama aplikasi ditinggalkan. Kartu “Sesi pasar otomatis / Jadwal Sesi” yang sebelumnya diminta dihapus tidak boleh muncul kembali. Pemain tidak perlu menekan play, membuka briefing, atau menutup hari secara manual agar dunia berlanjut.
4. Efek berita pada N02 harus memiliki durasi yang mengikuti **waktu nyata**, misalnya satu atau dua jam nyata. Ini kontrak waktu yang berbeda dari tempo dunia. Keputusan nomor 1 tidak membatalkan N02.
5. Pertahankan UI klasik yang dipilih pemilik dan lima tab **Beranda, Pasar, Portofolio, Bisnis, Lainnya**. Perbaiki susunan, hirarki, kontrol, dan keterbacaan; jangan mengulang redesain total ke empat tab.
6. Pertahankan isi Aset terbaru di Portofolio dan seluruh fitur yang sudah tersedia. Mengubah dropdown menjadi launcher fitur tidak boleh menghilangkan tindakan di dalamnya.
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
| N01 | `time#clock-display` | “buat berjalan lebih normal, waktunya bergerak pelan jangan skip skip, apakah kamu paham maksudku?” | Jam terasa mengalir dan tidak melompat saat refresh; laju lama dipertahankan sesuai jawaban terbaru. |
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

Pertahankan token, font lokal, lima tab, dan arah aktif `DESIGN.md`: ENERGY 3 / RHYTHM 2 / MOTION 2. Antislop core/ui/human/layoutmobile/copywriting berperan sebagai filter kualitas. Jangan mengganti pilihan visual klasik pemilik atas nama filter.

### 4.2 Data domain dan angka uang

- Java tetap otoritas saldo, biaya, order, waktu, dan save. JS menyajikan serta menerima input.
- Nilai uang gunakan objek/angka desimal yang sudah dipakai domain. Jangan mengubah precision uang menjadi `double` untuk akuntansi atau menghitung ulang biaya aktual di UI.
- Jangan mem-parse string rupiah terformat untuk memperoleh angka mentah. Jika kontrak belum membawa nilai numerik yang diperlukan, tambahkan field DTO sekecil mungkin dengan makna dan mata uang jelas.
- Pertahankan isolasi akun pribadi dan klien pada semua kartu, pemilihan akun, order, serta notifikasi.
- Nama emiten dapat diambil dari katalog berdasarkan ID. Jangan membuang prefix bursa dari identitas internal karena ticker dapat bertabrakan antar pasar; cukup rapikan label yang terlihat.
- Data tidak tersedia ditampilkan sebagai “—” atau keadaan kosong yang jelas, bukan nol palsu. Harga fill tidak boleh diganti harga terakhir; laba bersih tidak boleh memakai nilai sebelum biaya tanpa label yang tepat.
- Semua kontrol baru harus memakai helper/pola existing bila memungkinkan. Jangan membuat framework, service generik, registry rumit, dependency baru, atau event bus hanya untuk perubahan ini.

## 5. N01: Jam mengalir tanpa mengubah laju

### 5.1 Masalah dan keputusan

Pemilik melihat jam seperti melompat. Source mengubah tampilan setiap 250 ms sementara laju aktif dasar 288×. Secara matematis, 250 ms nyata setara **72 detik game**. Jadi interval itu secara alami melewati angka menit/detik, meskipun mekanisme waktu domain tepat.

**WAJIB:** laju lama dipertahankan. Jangan memperlambat domain menjadi 1:1, mengubah bunga/gaji, atau menyembunyikan drift dengan jam dekoratif yang tidak mewakili dunia.

### 5.2 Solusi dan pertimbangan

1. Gunakan `requestAnimationFrame` hanya untuk presentasi jam pada halaman terlihat. Domain tidak boleh maju berdasarkan jumlah frame.
2. Ambil timestamp, laju efektif, foreground/background, dan status dari snapshot Java. Buang fallback laju visual `96` jika tidak memiliki kontrak sah; saat snapshot belum tersedia tampilkan placeholder singkat.
3. Pada 60 frame/detik, laju 288× masih maju sekitar 4,8 detik game per frame. Tidak mungkin semua detik terlihat satu per satu tanpa mengubah tempo. Karena itu rekomendasi tampilan utama adalah **HH:mm** dengan transisi perubahan digit yang ringan; jangan menonjolkan detik yang berkedip cepat. Jam tetap mencerminkan timestamp domain.
4. Bila detik tetap diperlukan pada layar tertentu, tampilkan hanya pada detail waktu yang memang berguna. Jangan membuat waktu detail berbeda dari header.
5. Saat snapshot diterima, pertahankan continuity anchor. Hindari menimpa teks jam dengan waktu snapshot lama lalu langsung menggambar interpolasi baru sehingga terlihat mundur/maju dua kali.
6. Koreksi drift kecil secara bertahap dengan batas yang terukur. Koreksi besar karena resume/offline adalah sinkronisasi yang sah; jangan menganimasikan berjam-jam game seolah waktu sedang berjalan lambat. Segera tampilkan state yang benar dan, bila perlu, feedback singkat bahwa aktivitas saat ditinggalkan telah diperbarui.
7. Hentikan pekerjaan render saat hidden atau halaman dilepas. Saat kembali, minta sinkronisasi domain dahulu, lalu buat satu anchor tampilan baru. Jangan memutar banyak loop rAF setelah navigasi.
8. `prefers-reduced-motion` menonaktifkan animasi digit; pergantian angka tetap benar. Tidak membuat clock sebagai live region yang dibacakan setiap frame.
9. Penutupan bursa bukan jeda dunia. Status “Pasar tutup” tetap boleh tampil karena berguna untuk order; jangan menghapus aturan sesi agar jam tampak aktif.

### 5.3 Tugas atomik

- [ ] N01-01 Telusuri semua penulis `#clock-display`, termasuk refresh, interpolasi, recovery, dan updater modal.
- [ ] N01-02 Rekam baseline timestamp domain dan rekaman tampilan pada laju 1×, CPU lambat, refresh, serta reentry.
- [ ] N01-03 Perbaiki presenter jam dan lifecycle render tanpa menggandakan penghitung ekonomi.
- [ ] N01-04 Pertahankan `syncClock` berbasis waktu Java; pastikan callback ganda, timer terlambat, load, pergantian mode, dan page exit tidak kehilangan waktu.
- [ ] N01-05 Pertahankan auto-start, penghapusan kartu sesi, dan penanganan gagal save/batas replay yang sudah benar.
- [ ] N01-06 Jalankan regresi controlled clock, drift presenter, dan rekaman visual yang dapat dibandingkan.

### 5.4 Penerimaan dan bukti

- Controlled clock: 300.000 ms aktif 1× menghasilkan tepat 86.400 detik game; 3.600.000 ms tidak aktif menghasilkan tepat satu hari game, dengan remainder dipertahankan.
- Callback hidden/resume ganda, first tick terlambat 1.900 ms lalu hidden pada 2.000 ms, serta restart/load mode tidak kehilangan atau menghitung waktu dua kali.
- Tidak ada jeda manual/kartu sesi kembali. Setelah rollover, dunia tetap berjalan.
- Tidak ada kemunduran tampilan saat refresh biasa tanpa kemunduran waktu domain; tetapkan toleransi drift kecil dan catat pengukurannya.
- Lampirkan rekaman pendek pada beberapa FPS, screenshot normal/reduced motion, serta angka perbandingan domain. Screenshot diam saja tidak membuktikan kelancaran.

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

Event berdurasi 60 menit dimulai pukul 14.00 WIB nyata: pukul 14.30 sisa 30 menit, pukul 15.00 berakhir. Event 120 menit: pukul 15.00 masih sisa 60 menit, pukul 16.00 berakhir. Nilai tersebut tidak berubah karena tempo 288× atau 24×.

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
| Beli sekarang / Jual sekarang | `MARKET` | Jumlah, referensi harga beli/jual yang tersedia, estimasi biaya; field harga limit tidak aktif. |
| Tentukan harga | `LIMIT` | “Harga beli maksimal” atau “Harga jual minimal”, jumlah, estimasi dan status menunggu bila belum match. |
| Batasi rugi | `STOP_LOSS` | Harga pemicu, jumlah, arah kondisi sesuai sisi; tidak menyebut harga eksekusi terjamin. |
| Ambil untung | `TAKE_PROFIT` | Harga pemicu, jumlah, kondisi sesuai sisi; setelah terpancing mengikuti jenis eksekusi yang didukung domain. |

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

## 15. N11: Launcher fitur Portofolio terlihat langsung

### 15.1 Solusi

Ganti `details#asset-tools` menjadi bagian fitur terbuka dengan kartu/tombol seperti bahasa interaksi Beranda. Jangan sekadar menambahkan atribut `open` pada dropdown; pemilik menolak bentuk dropdown sebagai wadah fitur.

Rekomendasi kelompok berikut menggunakan fitur yang ditemukan pada `loadAsetView`; daftar final wajib direkonsiliasi dengan source terbaru:

| Kelompok | Tujuan yang harus tetap terjangkau |
|---|---|
| Aset & investasi | Rincian kekayaan konsolidasi, reksa dana berbasis NAB, multi-aset, koleksi/properti, bisnis operasional. |
| Kelola transaksi | Tukar IDR/USD, riwayat transaksi akun, aksi korporasi dan voting. |
| Risiko & kewajiban | Utang/pinjaman tenor, margin/risiko, audit pajak, asuransi portofolio, restrukturisasi darurat. |
| Kelola & uji strategi | Racik reksa dana kustom, uji stres, lab strategi, replay historis. |

Setiap tujuan mempunyai judul singkat, ikon relevan existing bila ada, dan satu penanda tindakan. Gunakan grid dua kolom yang reflow menjadi satu kolom bila perlu. Jika halaman menjadi panjang, pengelompokan dengan heading cukup; jangan memasukkan semuanya kembali ke accordion atau menu “lainnya” baru yang menyembunyikan fitur.

Dompet IDR/USD adalah rincian informasi dan tidak otomatis termasuk larangan dropdown fitur N11. Namun periksa apakah ringkasan saldo tersedia cukup terlihat tanpa membuka rincian. Jangan mengubah bentuk dompet hanya karena selector `details` sama.

### 15.2 Tugas atomik

- [ ] N11-01 Buat daftar lengkap button/caller yang sekarang dipindahkan ke `asset-tools`, termasuk FX yang dikecualikan oleh awalan teks.
- [ ] N11-02 Ganti pengelompokan berdasarkan tag/awalan teks dengan daftar tujuan eksplisit yang kecil dan mudah ditinjau.
- [ ] N11-03 Buat launcher terbuka dengan state normal/pressed/focus/locked/loading yang konsisten.
- [ ] N11-04 Pertahankan akun, syarat fitur dan konfirmasi untuk tindakan berisiko; kartu pembuka tidak langsung melakukan bailout/transaksi tanpa review.
- [ ] N11-05 Verifikasi setiap fitur sebelum/sesudah memiliki tujuan yang sama dan tidak ada fitur yatim.
- [ ] N11-06 Uji klik, keyboard, Back, scroll paling bawah, 200% dan refresh Portofolio tanpa duplikasi launcher.

Penerimaan: tidak ada `summary` yang menyembunyikan alat Portofolio; semua tujuan dalam inventaris dapat ditemukan dan dibuka. Fitur destruktif/ekonomi tetap terlindungi konfirmasi yang sesuai.

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

## 17. N13: Lainnya & Riset sebagai pusat fitur yang jelas

### 17.1 Solusi pengelompokan

Susun launcher terbuka dengan kelompok yang sesuai tujuan:

| Kelompok | Fitur existing yang harus diperiksa |
|---|---|
| Profil & progres | Profil Saya, misi periodik, liga/rival, level/pencapaian, Era Baru. |
| Riwayat & aset | Riwayat transaksi, galeri koleksi, hasil investasi/arus kas. |
| Riset & strategi | Terminal analis/konsensus/perbandingan, paket terminal, berita/riset, booster R&D, pohon riset, uji strategi, replay. |

Daftar ini bukan izin memindahkan seluruh struktur tab utama atau mengurangi fitur. Cocokkan dengan `loadRisetView`, `appendPrestigeSection` dan opener aktual. Gunakan satu representasi launcher yang sama dengan N11/N12 sejauh cocok, bukan menyalin komponen baru untuk setiap tombol.

### 17.2 Era Baru dan fitur terkunci

- Ringkasan syarat/progres dapat berupa panel pasif; tindakan “Lihat syarat & progres” harus jelas tombol yang membuka rincian.
- Jika belum terbuka, tampilkan status terkunci dan syarat aktual secara ringkas. Jangan memberi kesan tidak bisa melakukan apa pun jika rincian progres masih dapat dibuka.
- Jika sudah memenuhi syarat, tampilkan tindakan utama yang jelas dan pertahankan review/konfirmasi dampak terhadap run, aset, atau progres.
- Jangan menghilangkan guard hanya untuk membuat tombol tampak interaktif. Reset/run baru harus mengikuti aturan domain dan receipt/idempotency yang sudah ada.

### 17.3 Riset dan paket

- Ringkasan paket aktif adalah informasi. Tombol “Lihat paket” atau “Kelola paket” adalah tindakan yang terpisah.
- Kartu berita punya affordance buka dan detail; N02 mengatur durasi efek, N04 membersihkan label simulasi.
- Perbandingan/terminal/tech tree/booster ditampilkan sebagai tujuan fitur dengan nama sederhana. Jangan menggantinya dengan paragraf kemampuan yang tidak bisa diklik.
- Empty, unavailable, locked dan error dibedakan; jangan menyamakan belum ada data dengan fitur tidak dapat dipakai.

### 17.4 Tugas atomik

- [ ] N13-01 Inventaris semua launcher, kartu pasif, paket, berita dan state Era Baru.
- [ ] N13-02 Terapkan pengelompokan serta affordance yang sama dengan Portofolio/Bisnis.
- [ ] N13-03 Perbaiki syarat/progres/konfirmasi Era Baru tanpa mengubah aturan reset.
- [ ] N13-04 Perbaiki launcher terminal, hasil, booster, pohon riset dan replay.
- [ ] N13-05 Verifikasi setiap tujuan tetap bekerja, termasuk Back, fokus kembali dan state terkunci.
- [ ] N13-06 Uji journey profil, misi, liga, paket riset, fitur strategi dan Era Baru pada akun/save terisolasi.

Penerimaan: semua fitur yang dapat digunakan terlihat sebagai tindakan, panel informasi tetap pasif, dan syarat terkunci dapat dimengerti. Tidak ada fitur lama hilang, tombol mati, atau reset yang terjadi hanya karena membuka kartu.

## 18. Urutan eksekusi dan checkpoint

Urutan ini mengatur dependency, **tidak menggabungkan atau mengurangi task N01–N13**. Setiap ID tetap ditutup secara terpisah dengan buktinya.

| Fase kerja | Isi | Syarat sebelum lanjut |
|---|---|---|
| P00: Snapshot dan kepemilikan | Preflight, hash save/source, baseline compile, inventaris route/string/fitur, writer per file | Root benar, pekerjaan lokal terjaga, konflik penulisan ditangani dan baseline tercatat. |
| P01: Kontrak waktu | N01 dan fondasi waktu nyata N02 | Laju lama tepat, presenter halus terverifikasi, deadline berita/migrasi/catchup lulus tes domain. |
| P02: Pasar dan kebersihan UI | N03, N04, N05, N06 serta presentasi berita N02 | Ranking benar, sapuan string lengkap, grafik tetap utuh dan kontrol yang diminta dihapus. |
| P03: Transaksi | N07, N08, N09 | Payload dan akun benar, review/confirm aman, status ringkas, notifikasi sesuai outcome dan idempotency. |
| P04: Portofolio | N10, N11 | Angka posisi cocok dengan domain, updater tidak merusak kartu, seluruh launcher terjangkau. |
| P05: Bisnis dan Lainnya | N12, N13 | Journey tiap subarea, locked/empty/error, Back/fokus dan seluruh fitur lulus. |
| P06: Regresi dan penyerahan | Integrasi semua ID, matriks layar, stress 10×100, performa, paket APK, audit bukti | 13/13 memiliki kriteria lengkap dan bukti yang sesuai; tidak ada temuan wajib disembunyikan. |

Tidak menjalankan build yang sama bersamaan atau mengompilasi saat writer sedang mengganti kontrak DTO. Jika satu tahap gagal, diagnosis penyebab, perbaiki, dan uji ulang. Bagian independen boleh lanjut, tetapi dependency gagal tidak dianggap lulus.

### 18.1 Format checkpoint minimum

Pada setiap checkpoint, catat:

1. Root, branch, HEAD, daftar perubahan lokal yang relevan, hash snapshot, dan pemilik file.
2. ID anotasi dan subtask yang selesai/berjalan/pending, bukan hanya “UI selesai”.
3. Perilaku sebelum/sesudah, keputusan kecil/asumsi yang dipakai, dan jalur yang masih belum terbukti.
4. File berubah, command yang benar-benar dijalankan, exit code, total assertion/tes dan lokasi log.
5. Screenshot/rekaman aktual dari source yang sama, dengan route, viewport dan skala teks.
6. Temuan terbuka, severity, reproduksi, dependency, serta next_action.
7. Hash save pemain sesudah tes dan bukti bahwa save terisolasi yang dipakai.

Status yang diperbolehkan: `PENDING`, `IN_PROGRESS`, `FAIL`, `BLOCKED`, `PASS_WITHIN_SCOPE`. Jangan memakai `COMPLETE` jika suatu kriteria wajib belum diperiksa. Keterbatasan perangkat tetap `NOT_TESTED` secara eksplisit.

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
- Perubahan benchmark harus tetap mengukur pekerjaan berat yang nyata. Jangan mengganti skip/catchup satu hari yang lambat menjadi tick satu detik hanya agar angka PASS. Catat operasi berat yang diukur dan tuntaskan feedback/responsiveness yang memang dipersyaratkan.

### 19.3 Matriks layar dan state

Uji viewport standar proyek: **320×568, 360×640, 390×844, 412×915, 430×932, 768×1024, 1440×900**, pada **100% dan 200% teks**. Tambahkan **470×656** sesuai anotasi terbaru, dan **462×648** untuk reproduksi komentar sebelumnya.

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
| Jam tidak bisa memperlihatkan setiap detik pada 288× | Pertahankan tempo sesuai jawaban; gunakan representasi HH:mm dan presenter halus. Jangan memperlambat ekonomi atau membikin jam palsu. |
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

Seluruh file bukti baru direkomendasikan di `Artifacts/Annotation-13-2026-10-23/` atau direktori checkpoint baru yang teridentifikasi jelas. Jangan membuat duplikasi laporan besar bila tracker existing sudah memadai.

Penyerahan minimum:

1. **Tracker 13 anotasi**, dengan subtask seluruh ID pada dokumen ini, status, dependency, implementasi, tes, bukti, reviewer dan next_action.
2. **Source terintegrasi** yang mempertahankan laju lama serta perubahan UI/logika yang diperlukan semua anotasi.
3. **Perbandingan sebelum/sesudah** setiap N01–N13. N01 menggunakan rekaman gerak; N02 menggunakan controlled clock dan dampak engine, bukan screenshot countdown saja.
4. **Matriks field dan fitur:** jenis order → enum/payload, status order → tampilan/aksi, fitur Portofolio/Bisnis/Lainnya → opener/route, string lama → penggantian/penghapusan.
5. **Log pengujian** beserta exit code, source hash dan snapshot; kegagalan awal tetap tersimpan dengan diagnosis serta rerun.
6. **Hasil sequence 10×100** untuk jalur kritis yang disentuh, beserta invariant sesudah setiap aksi dan seed.
7. **Hasil layout/aksesibilitas/performa** dengan batas cakupan dan screenshot aktual yang dibuka reviewer.
8. **APK kandidat terbaru** yang cocok dengan source, hash, signature, metadata dan bukti isi paket; status native ditulis terpisah.
9. **Bukti save pemain utuh**, serta daftar save/port terisolasi yang dipakai untuk QA.
10. **Laporan akhir yang jujur:** selesai x/13, sisa ID, kegagalan terbuka, pertanyaan keputusan yang masih ada, dan next_action konkret. Jangan menulis “semua selesai” jika satu ID hanya selesai visual tetapi logikanya belum diverifikasi.

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

Gate visual tidak menggantikan tes ekonomi. Kode yang lulus tes tidak otomatis lulus penilaian visual. Semua ID membutuhkan keduanya sesuai sifat perubahan. Tidak membuat klaim PASS pada layar/state yang tidak diperiksa atau pada perangkat yang tidak diuji.

## 22. Instruksi mulai untuk agen pelaksana

Baca dokumen ini sampai selesai, lalu:

1. Jalankan preflight P00 dan pastikan tidak bertabrakan dengan writer/verifier yang sedang aktif.
2. Buat tracker yang mempertahankan **seluruh 77 subtask bernomor pada N01–N13**, ditambah langkah integrasi dan pengujian lintas bagian; jangan menghapus atau mengecilkan daftar. Batas nomor tiap anotasi berbeda, sehingga jangan mengasumsikan semuanya hanya enam subtask.
3. Catat keputusan tempo pemilik secara konsisten pada checkpoint/aturan proyek yang relevan: **tempo lama tetap, hanya tampilan jam dihaluskan; efek berita menggunakan satu–dua jam nyata.**
4. Verifikasi fondasi waktu yang sudah dikerjakan sebelum menggantinya. Jangan memulai game dari nol atau mengimpor save/source checkout lama.
5. Kerjakan satu checkpoint terukur menurut dependency bagian 18, tunjukkan progres x/13, lalu lanjut. Jangan berhenti pada rencana baru karena rencana eksekusinya sudah ada di file ini.
6. Perubahan visual kecil yang jelas putuskan sendiri sesuai `DESIGN.md`; catat asumsi. Konflik ekonomi nyata, aturan replay yang belum didukung, atau perluasan native/layanan baru harus dinyatakan secara spesifik.
7. Tutup setiap ID hanya setelah bukti penerimaannya ada. Simpan temuan yang belum lulus secara terbuka dan pertahankan batas host/browser/APK/perangkat.

**Dokumen ini lengkap mencakup 13 anotasi pemilik. Tidak ada anotasi yang dibatalkan hanya karena belum cocok dengan implementasi existing.**
