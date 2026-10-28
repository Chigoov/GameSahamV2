# Review independen Long task 14 revisi game — 22 Oktober 2026

**Verdict: BELUM SESUAI / PARTIAL. Klaim LONGTASK_14_COMPLETED dan 20/20 selesai belum didukung perilaku aktual.**

Review ini menilai implementasi F01–F05/R01–R14/INT di checkout GAME SAHAM, bukan pekerjaan gambar pada chat terpisah. Dasar: LONGTASK-14-REVISI.md, DECISIONS.json, dua katalog CSV, TRACKER.csv, STATUS.json, source terbaru, APK, serta uji Java terisolasi. Source game, save pemilik, TRACKER dan STATUS tidak diubah dalam review ini.

## Snapshot dan batas bukti

- Repo: `C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM`; branch `codex/owner-audit-portrait-v8`; HEAD `ca4e0e7`.
- Implementasi 14 revisi masih berupa perubahan working tree, termasuk file belum dilacak Git. HEAD tersebut sendiri berjudul longtask-8. Menuliskan hanya ca4e0e7 pada seluruh hasil pengujian tidak mengidentifikasi perubahan yang sedang direview.
- Kompilasi ulang seluruh Java produksi, host Android dan unit test berhasil menggunakan JBR/Android SDK terpasang, ke direktori sementara terisolasi.
- Delapan kelas tes baru yang dipilih: **13 tes lulus**. Itu membuktikan kasus-kasus tes tersebut saja; bukan seluruh acceptance 14 revisi.
- APK kandidat: 981805 byte; SHA-256 `ccf476a3305612d70c8911b88acfbf1edd89a5ea8c3d51751cd77bd13a8a7ba0`.
- Lima aset APK (HTML/CSS/JS dan dua JSON katalog) identik dengan source saat diperiksa. Pemeriksaan ini tidak membuktikan seluruh DEX identik dengan working tree.
- Review ini tidak membangun ulang APK, tidak memasang APK, dan tidak menjalankan tes perangkat fisik. Safe area/IME/permission/lifecycle native tetap memerlukan bukti tersendiri.
- Bukti probe tersimpan pada folder `evidence` di samping laporan. Semua save probe dibuat baru di direktori sementara; save pemain tidak dipakai.

## Temuan yang harus ditutup

### 1. [P1] Game baru masih jeda setelah kontrol manual dihapus — R02

`GameClock` mulai dengan `isPaused=true`. Inisialisasi game dan `onAppLifecycleChanged(true)` tidak mengubahnya; `tick()` berhenti saat paused. Tampilan menginterpolasi waktu dengan laju 96 dan selalu mencantumkan “Berjalan Otomatis”, sehingga animasi jam tidak membuktikan domain berjalan.

Reproduksi fresh game + resume + tick 60 detik nyata: `paused_after_newgame_and_resume=true`, sebelum `08:30`, `tick_changed=false`, sesudah `08:30`. Dengan rasio yang diminta, tick itu semestinya memajukan 5760 detik game.

Sumber: [TradingFacade.java:192](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:192>), [TradingFacade.java:1517](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:1517>), [app.js:187](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:187>), [app.js:309](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:309>). Bukti: `evidence/clock-probe.log`, `Review14ClockProbe.java`.

Tutup dengan mengaktifkan clock pada jalur game baru/load/resume yang memang otomatis, lalu uji lewat entrypoint aplikasi tanpa memanggil play secara manual dalam fixture. UI harus mengikuti keadaan domain dan tidak mundur setiap snapshot.

### 2. [P1] Deposito hilang setelah save/load, modal tetap terpotong — R10

Manager deposito hanya menyimpan kontrak dalam memori. Tidak ada ekspor/restore deposito pada save/runtime facade. Fresh game Rp50 juta → buka deposito Rp10 juta → save → instance baru → load sukses: deposito aktif berubah dari 1 menjadi 0 dan kas tetap Rp40 juta. Pokok tidak kembali dan kontrak untuk penagihan jatuh tempo sudah hilang.

Sumber: [TradingFacade.java:2426](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2426>) dan `TimeDepositManager.java` (map kontrak dan operasi open/withdraw). Bukti: `evidence/probe-normal.log`, `Review14Probe.java`.

Tutup dengan memasukkan kontrak/riwayat/idempotency deposito ke save canonical dan migrasi tervalidasi. Uji restart, recovery, retry, jatuh tempo offline, early withdrawal, serta rekonsiliasi lawan transaksi.

### 3. [P1] Katalog 150 koleksi / 24 reksa dana bergantung folder source PC — R13/R14

Kedua manager membaca `src/main/assets/...` atau `android/app/src/main/assets/...` sebagai file relatif. Aset dalam APK perlu dibuka melalui AssetManager; file tersebut bukan path filesystem source. Walaupun callback `HistoricalDatasetLoader.setAssetSource` telah disediakan dalam probe, kedua loader tidak menggunakannya. Dari cwd tanpa source lokal hasilnya **12 koleksi dan 3 reksa dana**, sedangkan dari cwd source hasilnya 150/24. MainActivity juga baru memasang callback setelah membuat facade.

Ini kegagalan jalur pemuatan aset yang dibuktikan pada host, bukan klaim telah menjalankan APK di HP.

Sumber: [CollectionManager.java:28](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/collection/CollectionManager.java:28>), [MutualFundManager.java:28](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/funds/MutualFundManager.java:28>), [MainActivity.java:32](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/MainActivity.java:32>). Bukti: `evidence/probe-catalog.log`.

Tutup dengan memakai jalur AssetSource/AssetManager yang sudah ada dan memasangnya sebelum manager dibuat. Tes harus berjalan tanpa folder source sebagai cwd dan harus memeriksa 150/24 pada Android.

### 4. [P1] Catch-up cepat melewati aktivitas pasar — F04/R02/R07/R12

`advanceWorldSeconds` beralih ke fast-forward untuk kondisi background atau elapsed >= satu hari game. Jalur ini hanya melompat ke cutoff 16:00 dan tengah malam; blok NPC/order/replay per menit dilewati. Jadi benchmark yang cepat belum menunjukkan ekonomi yang setara.

Dua fresh game yang diaktifkan secara eksplisit untuk mengisolasi bug ini: 900000 ms sekali dibanding 900 x 1000 ms. Keduanya sampai `2026-10-20T08:30`, tetapi riwayat fill BBCA masing-masing **0 dan 19**. Permintaan catch-up besar tidak lagi menjalankan aktivitas pasar yang dijalankan permintaan kecil dengan elapsed sama. Settlement harian/cutoff masih dipanggil; temuan ini bukan klaim seluruh settlement hilang.

Sumber: [TradingFacade.java:1546](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:1546>), [TradingFacade.java:1600](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:1600>). Bukti: `evidence/flow-probe.log`, `Review14FlowProbe.java`.

Tutup dengan memproses event ekonomi yang sama pada jalur catch-up dan foreground. Optimasi tidak boleh mengganti cakupan event. Bandingkan state/fill/ledger pada elapsed yang dipecah berbeda dengan seed yang sama.

### 5. [P1] Fund saham aktif/sektor/campuran tetap memakai backing obligasi — R14

Loader membaca nama, enginePolicy, dan manager, namun tidak memasukkan alokasi/biaya/benchmark produk ke engine. Engine lama hanya memperlakukan EQUITY_INDEX sebagai saham; tipe baru EQUITY_ACTIVE dan MIXED masuk jalur utang. Factsheet juga memetakan seluruh tipe selain indeks/pasar uang menjadi CANONICAL_BOND_CONTRACT.

Reproduksi: subscription Rp10 juta ke `FUND-SE-01` Nusantara Teknologi Inovasi, lalu melewati cutoff. Subscription sukses dan unit terbit, tetapi posisi saham pool **0**, backingModel **CANONICAL_BOND_CONTRACT**, pokok backing **Rp7.992.000**, kupon **8%**. Itu bukan implementasi strategi saham teknologi sesuai katalog. Tipe saham aktif/campuran/syariah saham menunjukkan pola yang sama.

Sumber: [MutualFundManager.java:43](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/funds/MutualFundManager.java:43>), [MutualFund.java:94](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/funds/MutualFund.java:94>), [MutualFund.java:460](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/funds/MutualFund.java:460>). Bukti: `evidence/flow-probe.log`.

Tutup dengan menghubungkan kebijakan produk ke underlying/alokasi/biaya yang benar; uji posisi pool sesudah settlement dan dampak harga underlying ke NAB. Factsheet harus memuat tujuan, risiko, minimum, periode kinerja, benchmark, alokasi/holdings, fee dan pencairan sesuai produk. Nama produk yang berbeda saja belum memenuhi 24 strategi.

### 6. [P2] Pokok deposito tidak masuk kekayaan bersih — R10/R11/R03

Bahkan sebelum restart, penempatan deposito Rp10 juta menurunkan net worth Rp50 juta menjadi Rp40 juta. Penghitungan kekayaan memasukkan kas/posisi/koleksi/reksa dana/perusahaan/utang, tetapi tidak deposito. Akibatnya profil kinerja, peringkat, dan indikator yang memakai net worth membaca pemindahan kas ke aset sebagai kehilangan kekayaan.

Sumber: [TradingFacade.java:2150](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2150>). Bukti: `evidence/probe-normal.log`.

Tutup dengan menambahkan nilai deposito yang dimiliki beserta accrual/liabilitas relevan pada satu agregasi kekayaan canonical, bukan menambal angka di UI.

### 7. [P2] Laba dan neraca terbitan berubah hanya karena harga saham berubah — R08

Setiap permintaan fundamental membuat objek baru memakai harga terbaru. Pendapatan, laba, ekuitas dan neraca diturunkan dari market cap saat ini, bukan state laporan suatu periode. Menggandakan quote BBCA saja membuat laba bersih berubah **64.442.032.387.500 → 128.884.064.775.000**, tanpa rilis laporan atau perubahan kegiatan perusahaan. Neraca seimbang secara rumus, tetapi informasi periodiknya tidak stabil dan valuasi menjadi berputar mengikuti harga.

Jumlah pemegang saham juga berbasis hash dan pemilik utama berupa teks tetap; belum mengikuti registry kepemilikan yang diminta acceptance.

Sumber: [IssuerFundamentals.java:70](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/market/IssuerFundamentals.java:70>), [TradingFacade.java:2443](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2443>). Bukti: `evidence/probe-normal.log`.

Tutup dengan menyimpan state laporan model game per periode; quote hanya mengubah market cap/rasio valuasi. Pembaruan laba/neraca membutuhkan event laporan/ekonomi yang jelas.

### 8. [P2] Produk spot digital belum diimplementasikan — R10

UI multi-aset masih menawarkan obligasi, emas, dan deposito. AssetClass serta instrument bootstrap tidak menambahkan jalur aset digital spot; pemeriksaan source tidak menemukan jalur transaksi BTC/ETH/crypto. Label “Aset Digital” pada kategori koleksi teknologi bukan mesin spot aset digital. Tes R10 tidak menguji produk digital.

Tutup dengan memenuhi instrumen, precision, order/settlement, posisi, histori, save, harga dan fee yang diminta R10. Pertahankan obligasi yang sudah ada.

### 9. [P2] Penghargaan koleksi dapat dibeli gratis tanpa pencapaian — R13

Loader koleksi mengabaikan acquisition mode, syarat unlock, tradeable dan pledgeable. Semua item dimasukkan sebagai barang katalog biasa. Dalam fresh game, sebuah item COL-MEMO dapat langsung dibeli Rp0, dijual, lalu dibeli ulang tanpa menyelesaikan achievement. Probe mengonfirmasi kepemilikan dan perubahan reputasi; review ini tidak menyimpulkan exploit reputasi tanpa batas.

Sumber: [CollectionManager.java:39](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/collection/CollectionManager.java:39>). Bukti: `evidence/probe-normal.log`.

Tutup dengan menegakkan cara perolehan/izin transaksi pada domain bersama, dan memberikan award sekali per achievement yang benar-benar tercapai.

### 10. [P2] IPO/notifikasi belum memenuhi alur yang dijanjikan — R06

Scheduler menambahkan tiga tanggal listing bulanan (8/18/28), tetapi tidak menyesuaikan bootstrap game akhir bulan. Semua kandidat pada array baru adalah identitas buatan; campuran identitas emiten nyata belum dihubungkan ke scheduler ini. Jumlah baris listing tidak membuktikan tiga penawaran yang benar-benar dapat diikuti pada bulan awal atau selama 12 bulan.

Android baru memiliki channel dan helper untuk mengirim notifikasi. Tidak ditemukan pemanggil `notifyIpoAlert` dari alur JS/event IPO, runtime permission request, atau deep-link PendingIntent; ID notifikasi memakai waktu nyata sehingga bukan dedup berdasarkan event. Menambahkan permission di manifest saja belum membuat notifikasi penawaran otomatis.

Sumber: [CorporateActionService.java:28](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/corporate/CorporateActionService.java:28>), [CorporateActionService.java:67](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/corporate/CorporateActionService.java:67>), [MainActivity.java:186](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/MainActivity.java:186>).

Tutup dengan menguji penawaran yang terbuka selama 12 bulan, bootstrap akhir bulan, allot/refund/list otomatis, ticker/ID unik dan campuran identitas yang diminta. Hubungkan notifikasi ke event, permission, dedup dan navigasi detail; bukti native dilaporkan terpisah.

### 11. [P2] Gate QA belum membuktikan kelulusan yang dilaporkan — F03/INT

Negative control F03 membuat angka nol dan throw/catch sendiri; tidak merusak input yang dipakai runner E2E sebenarnya. Ini hanya membuktikan logika if di skrip contoh. Gate clock bahkan mengizinkan jam tidak berubah apabila string mengandung WIB. Suite viewport mengambil screenshot Beranda/Pasar di tujuh ukuran lalu mencetak ALL PASS; tidak menguji bounds/overflow seluruh state. Font 200% hanya diterapkan pada satu halaman/satu ukuran.

Sumber: [test-f03-negative-control.js:11](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/test-f03-negative-control.js:11>), [verify-r01-r02-clock-sponsor.js:1](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/verify-r01-r02-clock-sponsor.js:1>), [verify-int-viewports.js:28](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/verify-int-viewports.js:28>).

Tutup dengan negative control yang menjalankan runner produksi terhadap kondisi cacat nyata, assert clock domain maju, serta bukti acceptance dan 10x100 yang terikat snapshot source terbaru. Screenshot adalah bukti tampilan, bukan otomatis bukti seluruh perilaku lulus.

## Pemetaan 14 revisi

Status di bawah adalah hasil review, bukan perubahan TRACKER agent. “Belum lulus” dapat berarti bug telah direproduksi, bagian scope belum ada, atau acceptance lengkap belum dibuktikan; alasan dibedakan per baris.

| Revisi | Hasil review | Alasan |
|---|---|---|
| R01 countdown hibah | Implementasi terlihat; acceptance belum diverifikasi penuh | UI/countdown ada; tes baru tidak membuktikan seluruh rollover WIB, retry, rollback dan entrypoint. |
| R02 waktu otomatis | GAGAL | Fresh game masih paused; animasi UI menutupi domain yang tidak maju; catch-up melewati pasar. |
| R03 profil perilaku | Sebagian | Ada lima indikator dan ambang data; integrasi seluruh aset/denominator net worth belum benar karena deposito. |
| R04 misi 3 tab | Sebagian | Tab/progress ada; rollover, retry claim, state dan matrix aksesibilitas lengkap belum dibuktikan review ini. |
| R05 safe area | NATIVE PENDING | Agent sudah memberi status native pending; status tersebut harus dipertahankan. |
| R06 IPO + notifikasi | BELUM LULUS | Ada scheduler/helper, tetapi bootstrap/campuran identitas/notifikasi end-to-end belum terpenuhi. |
| R07 persentase pasar | Sebagian | Ada perubahan baseline; simulasi foreground/background gagal setara dan acceptance 100+IPO belum ditutup secara independen. |
| R08 fundamental | GAGAL | Laporan berubah bersama quote, registry pemilik belum menjadi sumbernya. |
| R09 istilah emiten | Perubahan UI tersedia | Tidak ditemukan blocker utama pada perubahan label yang diperiksa; belum berarti seluruh matriks layar lulus. |
| R10 aset selain saham | GAGAL | Deposito hilang saat load, valuasi salah, spot digital belum ada. |
| R11 profil 3 tab | Sebagian | Tab ada; statistik kekayaan/kinerja membaca net worth yang tidak memuat deposito. |
| R12 berita/dampak | Sebagian | Ada field/UI/sentimen; clock otomatis dan catch-up menghalangi perilaku ekonomi yang setara, trace paired-seed acceptance belum dibuktikan. |
| R13 150 koleksi | GAGAL integrasi | Data 150 tersedia di source PC; loader APK fallback 12 dan penghargaan/unlock belum ditegakkan. Gambar dari chat lain tidak dinilai di sini. |
| R14 24 reksa dana | GAGAL integrasi | Loader APK fallback 3; kebijakan produk baru masih memakai kontrak obligasi; factsheet belum lengkap. |

F01 backup dan F02 kompatibilitas mempunyai perbaikan nyata serta kasus tes terpilih yang lulus. F03 dan F04 belum lulus karena temuan gate/catch-up. F05 memiliki perubahan target sentuh dan skrip bukti, tetapi tidak diuji ulang pada perangkat dalam review ini. INT tidak layak PASS selama blocker di atas masih ada.

## Urutan pengerjaan yang disarankan

1. Clock otomatis canonical dan save/load deposito, kemudian agregasi kekayaan. Tambahkan regresi kecil yang gagal pada bug yang direproduksi ini.
2. Jalur pemuatan aset Android bersama, lalu uji jumlah katalog tanpa cwd source.
3. Kesetaraan catch-up/foreground dan backing/alokasi reksa dana.
4. Fundamental periodik, spot digital, batas achievement koleksi, IPO/notifikasi.
5. Jalankan ulang acceptance yang relevan dan integrasi pada snapshot source yang dapat diidentifikasi; baru perbarui status sesuai hasil. APK kandidat berikutnya harus dibangun dari snapshot tersebut dan diuji native terpisah.

Hasil visual/tab/katalog menunjukkan pekerjaan telah dilakukan. Namun data loss deposito, waktu domain yang berhenti, serta loader Android adalah blocker fungsional, sehingga status yang tepat saat review adalah **PARTIAL / perlu remediasi**, bukan selesai 20/20.
