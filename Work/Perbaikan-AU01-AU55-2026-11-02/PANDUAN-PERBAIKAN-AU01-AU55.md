# Panduan perbaikan Game Saham — AU01–AU55

Tanggal: 2 November 2026, Asia/Jakarta.

**Status: dokumen panduan; perbaikan source BELUM dijalankan melalui dokumen ini.** Permintaan pemilik pada tahap ini hanya membuat file Markdown. Dokumen ini bukan penunjukan otomatis `plan.md` aktif, bukan izin menerapkan seluruh perubahan sekaligus, dan bukan persetujuan desain baru.

## 1. Tujuan, baseline dan cara memakai dokumen

Tuntaskan 55 temuan audit dengan perubahan minimum pada akar masalah. Lampiran A menyalin rincian temuan, lokasi kode, potongan source, reproduksi dan arah perbaikannya ke dalam dokumen ini. Agent tidak perlu bergantung pada folder audit sementara untuk mengetahui masalah dan batasnya.

| Baseline audit | Nilai |
|---|---|
| Root produksi | `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM` |
| Branch snapshot | `codex/classic-ui-new-assets` |
| HEAD snapshot | `9653f7a992405b4dfaf28acddbf1caf158d03957` |
| Perubahan lokal | Sudah ada sebelum audit; wajib dipertahankan |
| Temuan utama | 55: 2 Fatal bersyarat, 9 Tinggi, 42 Sedang, 2 Rendah |
| Cakupan pembacaan audit | 105 berkas Java/web produksi; bukan persentase fitur yang telah lulus pengujian |
| Pengujian historis audit | 112 tes regresi terpilih lulus, ditambah probe khusus yang membuktikan bug |
| Bukti Android fisik/build baru | Belum tersedia dari audit ini |

Langkah menggunakan panduan:

1. Pemilik menunjuk satu `plan.md` atau paket/ID yang akan dieksekusi. Jika penunjukan sudah ada dalam sesi, gunakan penunjukan tersebut tanpa meminta ulang.
2. Baca bagian 2–4, lalu rincian ID yang ditunjuk pada Lampiran A. Nomor baris adalah snapshot audit: verifikasi fungsi dan seluruh caller pada source terkini sebelum mengedit.
3. Reproduksi masalah memakai data QA baru. Catat jika kode sudah berubah atau temuan tidak lagi dapat direproduksi; jangan membuat perubahan kosmetik hanya agar daftar terlihat selesai.
4. Perbaiki satu akar masalah, jalankan pemeriksaan yang sesuai, lalu lanjutkan paket terkait. Pisahkan hasil domain, browser, build dan perangkat.
5. Gunakan kriteria selesai bagian 7 dan format penyerahan bagian 8. Memiliki file, screenshot atau APK saja tidak meluluskan fitur.

Bukti audit lama merupakan titik awal, bukan bukti kelulusan setelah source diperbaiki. Contoh kode dalam Lampiran A adalah ilustrasi; beberapa ditandai pseudocode. Jangan menyalinnya seolah seluruh nama variabel atau API tersebut sudah tersedia.

## 2. Instruksi wajib untuk agent pelaksana

### 2.1 Ubah hanya bagian yang diinstruksikan

**Hanya ubah fungsi, caller, regression check dan dokumentasi yang diperlukan untuk ID/paket yang ditunjuk pemilik.** Setiap file dan bagian diff harus dapat dikaitkan dengan ID tersebut.

- Jangan memperbaiki bagian lain, merombak arsitektur, mengganti tema/navigasi, memformat seluruh file, mengganti nama massal, memperbarui dependency, atau mengubah balancing sebagai pekerjaan sambilan.
- Jika akar masalah mempunyai beberapa caller, telusuri semuanya dan perbaiki shared helper yang tepat. Perubahan caller yang diperlukan untuk menutup akar masalah termasuk lingkup ID; jelaskan hubungannya pada laporan.
- Jika menemukan masalah baru yang tidak diperlukan untuk perbaikan ID aktif, catat lokasi, bukti, dampak dan saran. Jangan langsung mengeditnya di luar lingkup.
- Pertahankan semua perubahan lokal milik pemilik/agent lain. Jangan reset, clean, stash, revert massal atau menghapus file supaya tes lulus.
- Jangan mengubah `AGENTS.md`, master plan atau keputusan produk untuk menyesuaikannya dengan implementasi yang salah. Perubahan dokumen aturan membutuhkan instruksi pemilik yang berlaku.
- Tidak ada framework, engine, dependency atau abstraksi spekulatif baru. Reuse helper dan runner existing; gunakan perubahan paling kecil yang benar setelah alur lengkap dipahami.

### 2.2 Lindungi data pemain

Save pemain berikut **hanya boleh di-hash**:

`C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Artifacts/LocalPreview/preview-save/save_game_v2.json`

SHA256 pada audit:

`59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`

Jangan parse, menyalin, memuat ke game QA, memigrasikan, memperbaiki atau memakai save pemain sebagai fixture. Jika hash berubah, laporkan; jangan mengembalikan atau menimpanya secara paksa karena pemilik mungkin sedang bermain.

- Gunakan save QA baru dan direktori output yang jelas terpisah. Periksa tujuan tulis runner sebelum menjalankannya.
- Jangan memakai new game/reset untuk menyamarkan masalah load atau state korup.
- Tidak boleh menghapus ID aset, posisi atau data lama yang belum dikenali agar validasi save lolos. Kebijakan kompatibilitas/migrasi harus disahkan sebelum langkah yang memerlukannya.
- Jelaskan maksud compile/test/build/run terlebih dahulu dan pastikan source serta save asli aman. Izin pengujian lokal terpisah yang sudah diberikan tidak berarti izin instalasi atau perubahan global.
- Jangan ADB, memasang APK, mengubah signing/application ID, push/publish, mengirim pesan eksternal atau mengubah konfigurasi global tanpa izin yang berlaku. Jangan meluncurkan agent lain berdasarkan mandat lama.

### 2.3 Pertahankan aturan produk yang sudah disahkan

Rujukan utama adalah instruksi pemilik terbaru, `AGENTS.md`, bagian aktif master plan dan plan yang ditunjuk. Jangan meminta ulang keputusan yang sudah jelas berlaku.

- Java domain tetap pemilik uang, aset, waktu dan save; UI hanya presentasi/input. Produksi tetap Java + native Android WebView + HTML/CSS/JavaScript lokal.
- Dunia otomatis 48×: satu hari game sekitar 30 menit nyata pada kondisi aktif, background dan offline. Tidak ada kontrol pause/speed/skip untuk pemain. Pemulihan kegagalan internal bukan fitur manipulasi waktu.
- IDX buka 09.00–16.00 WIB game, istirahat 12.00–13.30; US 21.30–04.00 WIB. Terapkan aturan sesuai bursa instrumen.
- Transaksi pemain AON. LIMIT tetap antre utuh bila pemenuhan penuh belum tersedia. MARKET yang diproses dengan likuiditas tidak cukup dibatalkan utuh dan reservasi dilepas. MARKET saat bursa tutup mengikuti antrean sesi berikutnya yang sudah disepakati; harga estimasi tidak dijamin.
- Dukungan order besar yang valid tidak boleh dipangkas menjadi maksimum 2.000 lot hanya untuk menghindari bug. Cadangan 2.000 lot adalah cadangan dasar market maker; likuiditas sesuai kebutuhan tetap melalui kejadian ekonomi tercatat dan idempotent.
- Jangan menambah kas/saham NPC diam-diam. Pertahankan keputusan suntikan modal tanpa utang/bunga serta penerbitan saham baru yang tercatat; jangan membuat cooldown atau batas ekonomi baru sepihak.
- Pertahankan mekanika IPO 3 hari dan penjatahan yang berlaku, anti-self-trade, serta pemisahan dana pribadi/klien.
- Pokok deposito dihitung sebagai kekayaan; bunga yang belum menjadi hak pemain tetap estimasi terpisah. Jangan mengubah kontrak aktif ketika memperbaiki quote produk.
- PRO US$250.000 dan Whale US$1.000.000 uang game per 30 hari game; penghentian auto-renew mempertahankan akses sampai masa berbayar habis.
- Konflik berita dijumlahkan bertanda; reference yang disepakati dipertahankan selama rangkaian aktif. Net nol menonaktifkan dorongan target. Pembulatan searah target pada fraksi sah. Efek 60/120 menit nyata mempunyai deadline persisten.
- Sinkronisasi memakai jam/tanggal existing yang mengejar cepat, tanpa panel tunggu baru. Persetujuan gerakan jam existing dan pesan gagal pembatalan yang sudah diberikan tidak perlu diminta ulang untuk lingkup identik.
- Perubahan tampilan, teks, navigasi dan alur lain tetap melalui preview nyata dan persetujuan eksplisit sesuai aturan proyek. Kerjakan bagian domain yang independen selama menunggu.

## 3. P00 — Baseline sebelum implementasi

P00 adalah pemeriksaan awal, bukan perbaikan source.

- [ ] Buktikan cwd dan Git root, branch/HEAD, serta staged/unstaged/untracked pada checkout yang benar.
- [ ] Catat plan aktif dan ID yang ditunjuk. Bedakan dari mandat/dokumen historis.
- [ ] Hash save pemain tanpa memuat isinya; catat perubahan lokal yang harus dipertahankan.
- [ ] Reproduksi ID aktif dengan fixture QA minimal. Periksa test lama terhadap keputusan terbaru; jangan mengubah ekspektasi untuk menutupi bug.
- [ ] Petakan shared helper, caller, state yang dimutasi, persistensi dan efek sampingnya.
- [ ] Tetapkan target hasil dan pemeriksaan penolakan tanpa mutasi sebelum mengedit.
- [ ] Untuk perbaikan UI, identifikasi persetujuan yang sudah ada dan perubahan mana yang masih memerlukan preview.

Dua kegagalan yang sama tanpa diagnosis baru memerlukan laporan PERLU PERHATIAN beserta bukti dan pendekatan berbeda. Jangan mengulang runner tanpa memahami penyebabnya. Hambatan pada satu keputusan tidak menghentikan pekerjaan independen yang masih diizinkan.

## 4. Paket pekerjaan dan dependensi

Paket berikut adalah urutan rekomendasi, **bukan izin menjalankan semuanya**. Setiap AU ditempatkan sekali pada tabel agar tidak dihitung ganda. Dependensi antarpaket tetap harus diperiksa pada caller aktual.

| Paket | Tujuan | ID |
|---|---|---|
| P01 | Dunia macet, integritas save dan endpoint portofolio | [AU04](#au04), [AU05](#au05), [AU09](#au09), [AU23](#au23), [AU27](#au27), [AU37](#au37) |
| P02 | Integritas order, validasi jumlah dan expiry NPC | [AU01](#au01), [AU14](#au14), [AU28](#au28), [AU29](#au29), [AU50](#au50) |
| P03 | Perhitungan ekonomi, kebijakan produk dan konsistensi laporan | [AU06](#au06), [AU07](#au07), [AU10](#au10), [AU12](#au12), [AU13](#au13), [AU30](#au30), [AU38](#au38), [AU39](#au39) |
| P04 | Berita, target, efek, reset dan perpanjangan paket | [AU24](#au24), [AU25](#au25), [AU26](#au26), [AU33](#au33), [AU34](#au34), [AU35](#au35), [AU36](#au36), [AU54](#au54) |
| P05 | Katalog, progres fitur dan persistensi profil | [AU11](#au11), [AU15](#au15), [AU16](#au16), [AU17](#au17), [AU18](#au18), [AU19](#au19) |
| P06 | Kontrak UI, navigasi, interaksi dan aksesibilitas | [AU02](#au02), [AU03](#au03), [AU08](#au08), [AU20](#au20), [AU21](#au21), [AU22](#au22), [AU40](#au40), [AU41](#au41), [AU42](#au42), [AU43](#au43), [AU44](#au44), [AU45](#au45), [AU46](#au46), [AU47](#au47), [AU48](#au48), [AU49](#au49), [AU51](#au51), [AU52](#au52), [AU53](#au53), [AU55](#au55) |
| P07 | Keamanan jalur replay yang masih tersimpan | [AU31](#au31), [AU32](#au32) |

### P01 — Gerbang keselamatan progres dan dunia

Kerjakan AU05/AU09 terlebih dahulu untuk menutup penyebab clock berhenti. Jangan sekadar menelan exception atau memanggil resume tanpa memulihkan invariant ekonomi. AU27 harus membedakan save struktural valid dari save yang bertentangan dengan ledger; cadangan valid terakhir tidak boleh ikut ditimpa kandidat rusak. AU37 memvalidasi konfigurasi sebelum reset. AU23 memisahkan resolusi aset saham dari obligasi/komoditas. AU04 memakai serializer error existing agar kegagalan tidak berubah menjadi JSON rusak.

Gate: reproduksi awal berubah menjadi penolakan/pemulihan yang benar, tanpa mutasi separuh, tanpa kupon ganda, tanpa short dari saham terikat, tanpa overwrite backup valid, dan dunia tetap dapat berjalan setelah hambatan pulih. Simpan bukti sebelum/sesudah, termasuk save/reload QA.

### P02 — Order dan trust boundary

Gunakan pemeriksaan sesi dan AON pada seluruh tipe/side, termasuk trigger dan queued market. Hitung seluruh eksposur pending akun klien ketika memeriksa batas. Quantity/lot pecahan atau di luar rentang tidak boleh dipotong diam-diam menjadi integer. Pastikan expiry menemukan quote NPC yang tidak terdaftar pada order manager, tanpa menghapus GTC pemain atau menimbun riwayat NPC final selamanya.

Gate: likuiditas kurang/tepat/lebih; reservasi kembali penuh pada pembatalan utuh; posisi/uang tidak berubah pada penolakan; beberapa order klien bersamaan tidak melewati batas; integer valid tetap diterima; expiry dan reload konsisten.

### P03 — Ekonomi dan laporan

Satukan rumus preview dengan eksekusi bisnis, konversi utang USD, parameter fund dari katalog dan penerapan macro setelah reset produk. Tutup validasi bobot negatif. Selesaikan keputusan biaya short tidak terbayar dan identitas/emisi IPO berulang sebelum perubahan yang bergantung padanya. Jangan menciptakan uang, aset, bunga atau saham tanpa ledger/supply yang benar.

Gate: komponen laporan menjumlah ke total kanonis, rollover indeks tidak melompat saat harga tetap, quote tidak berubah hanya karena reload, kontrak aktif tidak direpricing, serta saldo dan saham beredar dapat direkonsiliasi.

### P04 — Kewajiban berulang dan berita

Pisahkan quote NPC rutin dari kewajiban ekonomi esensial. Target berita berbayar tidak boleh dilewatkan pada fast-forward; jangan menyatakan target tercapai hanya dengan mengganti flag. Renewal diproses tepat pada expiry, bukan baru settlement berikutnya. Reset cache per run, entitlement Basic, penggunaan/persistensi booster dan hubungan berita dengan kejadian ekonomi harus diperbaiki di domain.

Gate: aktif bertahap/background/offline memberi pemenuhan kewajiban yang benar pada batas relevan; konflik/net nol/pembulatan/reference/deadline tetap sesuai keputusan; save/reload tidak menggandakan biaya atau kejadian. Bandingkan kasus 14 hari Whale dan generator 90 hari dari audit tanpa mengklaim keduanya membuktikan run tanpa batas.

### P05 — Konten dan progres tersimpan

Perbaiki alur AssetSource yang benar-benar dipakai Android; jangan menganggap keberadaan katalog 150 item berarti runtime sudah memuatnya. Progress bos harus dipicu kalender, disimpan dan direset bersama run. Achievement membutuhkan event sah; arsip liga harus mencari pemain yang benar. Avatar/frame yang terpasang perlu ikut persistensi sesuai kontrak profil.

Gate: startup dengan aset Android dari CWD netral, save/reload dan new run; progres tidak hilang atau terbawa run lama; koleksi lama tidak dihapus. Validasi kompatibilitas ID koleksi sebelum mengganti fallback katalog.

### P06 — UI dan jalur fitur

Siapkan preview untuk AU51 sejak awal sambil P01/P02 berjalan; akses pembatalan langganan adalah temuan Tinggi dan tidak boleh tertinggal sampai kosmetik selesai. Gunakan fungsi yang sudah ada untuk memulihkan jalur fitur. Perbaiki kontrak field akun/avatar/kalender, guard tab gabungan, event listener order book dan respons successfalse. Hapus histori, pemilik saham, promo atau statistik rekaan dari UI.

Gate: Basic/Pro/Whale dapat mencapai alur yang berhak diakses, termasuk kelola/cancel paket; akun personal/klien tidak tercampur; data live diperbarui; klik tetap bekerja setelah tick; penolakan domain tidak mengabarkan sukses; teks sempit/200% dan high contrast dapat digunakan. Cek keyboard, fokus dan reduced motion pada layar yang berubah.

### P07 — Replay tersimpan

Batasi remediasi pada jalur replay/save lama yang masih dapat digunakan. Perbaiki penggunaan data masa depan dan kredit USD ganda. Jangan mengaktifkan pembuatan replay baru atau menghapus/migrasikan save replay tanpa kebijakan yang disahkan.

Gate: fill hanya pada harga yang dapat diamati pada waktu keputusan, modal dikredit sekali dan ledger cocok; pasar utama tidak berubah akibat perbaikan legacy replay.

## 5. Keputusan yang perlu diverifikasi sebelum perubahan terkait

Cari dahulu keputusan terbaru pada master plan/plan aktif dan percakapan yang relevan. Tabel ini bukan perintah menanyakan ulang keputusan yang sudah disahkan. Jika memang belum ada jawaban, ajukan pertanyaan ringkas kepada pemilik dan lanjutkan pekerjaan independen.

| ID | Hal yang perlu dipastikan | Yang tidak boleh dipilih sepihak |
|---|---|---|
| AU05 | Sumber/kebijakan pembayaran kupon ketika penerbit kekurangan kas | Mencetak dana diam-diam, menghapus hak kupon, atau memberi utang/bunga baru |
| AU10 | Pencatatan dan pelunasan biaya short yang tidak terbayar | Menghapus biaya, menarik collateral, atau menambah bunga baru tanpa aturan |
| AU13 | Pool IPO berulang: emiten baru atau penerbitan tambahan yang sah pada ticker sama | Mengubah hak kepemilikan atau reset posisi pemegang lama |
| AU17 | Peristiwa yang sah untuk progres achievement terkait liga/riset | Memberi achievement hanya karena membuka layar bila itu belum disahkan |
| AU26/AU54 | Hubungan kejadian ekonomi, entitlement/deadline dan jaminan target ketika ada konflik nyata dengan band harga | Mengarang merger/akuisisi, mengubah persentase/rate, atau mengklaim target sukses tanpa harga/ekonomi sesuai |
| AU30 | Pemilihan instrumen pendukung fund bila katalog hanya mendefinisikan bobot | Strategi investasi/balancing baru tanpa keputusan |
| AU11/AU27/P07 | Kompatibilitas ID/skema save lama bila remediasi memerlukannya | Migrasi atau penghapusan data pemain tanpa kebijakan |

## 6. Rencana pengujian minimum

Gunakan runner dan helper yang tersedia. Satu pemeriksaan kecil yang benar-benar gagal pada bug lebih bernilai daripada test yang hanya meniru implementasi. Jangan menambah framework/fixture besar untuk checklist.

| Kelompok | Pemeriksaan wajib pada perubahan terkait |
|---|---|
| Uang/aset | Saldo tersedia/tertahan, kepemilikan, utang, ledger dan supply sebelum/sesudah; penolakan tidak memutasi |
| Matching | BUY/SELL, LIMIT/MARKET/trigger, bursa buka/tutup/istirahat, AON, self-trade, order besar, akun klien |
| Clock/settlement | Batas 16.00/24.00/04.00 yang relevan, expiry, aktif/background/offline, kegagalan dan retry idempotent |
| Save | Primer valid/rusak struktural/rusak semantik, backup valid, write gagal, reload dan versi/ID lama yang disepakati |
| Progres | New run, save/reload, rollover harian/mingguan, hadiah tidak ganda, state per-run/lifetime tidak tertukar |
| UI | Sukses, kosong, gagal domain, status belum terkonfirmasi, data terakhir/stale, transaksi terkunci saat sinkronisasi/save gagal |
| Aksesibilitas | 320/360/390 px, teks 100/200%, high contrast, reduced motion, keyboard dan fokus; bedakan simulasi browser dari skala OS |
| Kritis berulang | 10×100 aksi bila relevan sesuai standar proyek; catat alur dan seed, bukan mengklaim semua fitur diuji |
| Performa | Catat heap/latensi pada skenario relevan; offline panjang tidak menimbun quote NPC atau melonggarkan validasi save |

Audit sebelumnya mencatat 51 + 44 + 17 tes regresi terpilih lulus. Seluruh suite besar belum dijalankan ulang dan APK/perangkat belum dibuktikan. Jalankan pemeriksaan sesuai perubahan baru; jangan memakai hasil lama sebagai status sekarang. Jangan memperbaiki test dengan melemahkan invariant atau menghapus reproduksi yang gagal.

Jika probe lama di folder sementara sudah tidak tersedia, bangun ulang reproduksi minimal dari Lampiran A menggunakan source terkini dan save QA baru. Catat bahwa reproduksi baru yang menjadi bukti, bukan nama file/log lama.

## 7. P08 — Kriteria selesai dan gate rilis

Untuk setiap ID, gunakan status `BELUM DIMULAI`, `DIREPRODUKSI`, `DIPERBAIKI—BELUM DIVERIFIKASI`, `TERVERIFIKASI`, atau `MENUNGGU KEPUTUSAN` dengan alasan konkret. Jangan menandai TERVERIFIKASI hanya karena kode sudah berubah.

- [ ] Akar masalah ditutup pada semua caller yang relevan dalam lingkup, dengan diff minimum yang dapat dijelaskan.
- [ ] Reproduksi awal berubah menjadi hasil yang benar; kasus batas dan jalur saudara tidak rusak.
- [ ] Pemeriksaan nontrivial yang diperlukan ditinggalkan pada runner existing.
- [ ] Domain uang/ledger/supply dan state setelah save/reload sesuai; tidak ada data pemain digunakan.
- [ ] Persetujuan UI/produk yang diperlukan tercatat untuk perubahan yang benar-benar diterapkan.
- [ ] Semua perubahan lokal pihak lain tetap ada; tidak ada cleanup/refactor/dependency/global change di luar tugas.
- [ ] Hasil host, browser, build APK dan perangkat dipisahkan. Gate yang belum diuji tetap ditulis belum terverifikasi.
- [ ] Hash save pemain akhir dibandingkan dengan baseline tanpa pemulihan paksa bila berubah.
- [ ] Temuan Fatal/Tinggi aktif tidak disembunyikan di balik tes lulus atau checklist dokumen.
- [ ] Pemilik menerima handoff plan/ID yang ditunjuk; penyelesaian satu paket tidak dianggap izin mengerjakan scope baru.

## 8. Format penyerahan hasil oleh agent

Gunakan Markdown biasa dan isi dengan bukti aktual:

```text
Plan/paket aktif:
ID dan subbutir yang dikerjakan:
Baseline branch/HEAD + perubahan lokal yang dipertahankan:
Reproduksi sebelum perbaikan:
Akar masalah dan perubahan:
File/baris yang diubah, dipetakan ke ID:
Hasil setelah perbaikan dan pemeriksaan kasus batas:
Perintah/check + log/screenshot/output:
Status per ID dan gate yang belum terverifikasi:
Hash save pemain awal/akhir:
Keputusan/persetujuan UI yang dipakai atau masih dibutuhkan:
Temuan tambahan di luar scope (dicatat, tidak diam-diam diperbaiki):
Blocker nyata dan pekerjaan independen yang telah diselesaikan:
Next action:
```

## Lampiran A — Rincian seluruh temuan AU01–AU55

Bagian berikut disalin dari laporan audit final agar lokasi kode, batas bukti, reproduksi dan rekomendasi tetap tersedia di repo. **Ini bukti baseline audit, bukan hasil perbaikan.** Label Terbukti mengikuti lingkup yang disebutkan; test host/browser tidak menjadi bukti Android fisik. Contoh ilustrasi/pseudocode wajib disesuaikan dengan API existing dan diperiksa sebelum dipakai.

### Indeks dan bukti baseline

Urutan berdasarkan tingkat keparahan. NomorAU adalah identitas temuan, bukan urutan prioritas. Penilaian finalAU05/AU09 memakai kategoriFatal karena reproduksi menghentikan dunia; sebagian catatan checkpoint sebelumnya menamainyaTinggi. Potongan source di bawah diambil dari file saat penutupan audit. Contoh perbaikan adalah usulan, belum diterapkan/diuji; pseudocode ditandai dan tidak boleh diperlakukan sebagai API yang sudah tersedia.

| ID | Keparahan | Temuan | Lokasi utama |
|---|---|---|---|
| [AU05](#au05) | Fatal | Kegagalan pembayaran kupon obligasi menghentikan jam dunia | [MultiAssetTradingService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/multiasset/MultiAssetTradingService.java:130>) |
| [AU09](#au09) | Fatal | Short meminjam saham NPC yang masih terikat order jual | [RiskComplianceService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/risk/RiskComplianceService.java:226>) |
| [AU01](#au01) | Tinggi | Trigger dieksekusi ketika bursa tutup | [TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2459>) |
| [AU11](#au11) | Tinggi | Katalog150koleksi hanya terbaca dari filesystem proyek | [CollectionManager.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/collection/CollectionManager.java:309>) |
| [AU14](#au14) | Tinggi | Beberapa order beli klien bisa melewati hard limit konsentrasi | [FirmManager.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/firm/FirmManager.java:150>) |
| [AU23](#au23) | Tinggi | Memiliki obligasi membuat endpoint portofolio gagal, bahkan setelah posisi dijual habis | [TradingWebBridge.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java:2119>) |
| [AU27](#au27) | Tinggi | Save dengan saldo yang bertentangan dengan ledger dianggap valid dan menggantikan cadangan baik | [TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2853>) |
| [AU28](#au28) | Tinggi | Stop-loss dan take-profit dapat terisi sebagian, melanggar AON pemain | [MatchingEngine.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/trading/MatchingEngine.java:243>) |
| [AU37](#au37) | Tinggi | Permintaan game baru dengan nama tidak valid tetap mereset progres | [TradingWebBridge.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java:2425>) |
| [AU51](#au51) | Tinggi | Menu aktif kehilangan akses terminal, pengelolaan paket, lab dan beberapa fitur existing | [app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:4710>) |
| [AU54](#au54) | Tinggi | Fast-forward melewati pemenuhan target berita Whale | [TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2621>) |
| [AU02](#au02) | Sedang | Pemilih perusahaan tersembunyi pada pembukaan awal | [app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:7390>) |
| [AU03](#au03) | Sedang | Tanggal pilihan kalender tidak menyaring daftar | [app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:4606>) |
| [AU04](#au04) | Sedang | JSON error save tidak di-escape | [TradingWebBridge.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java:2305>) |
| [AU06](#au06) | Sedang | Nilai indeks melompat kembali ke basis pada pergantian hari | [MarketIndex.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/market/MarketIndex.java:77>) |
| [AU07](#au07) | Sedang | Alokasi reksa dana kustom menerima bobot negatif | [TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:3895>) |
| [AU08](#au08) | Sedang | UI mengabarkan sukses ketika domain menolak tindakan | [app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:8401>) |
| [AU10](#au10) | Sedang | Biaya pinjaman short hilang ketika kas tidak cukup | [RiskComplianceService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/risk/RiskComplianceService.java:399>) |
| [AU12](#au12) | Sedang | Proyeksi pendapatan dan laba bisnis memakai kapasitas, bukan rumus pendapatan | [CompanyManager.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/business/CompanyManager.java:570>) |
| [AU13](#au13) | Sedang | IPO tahunan dengan ticker sama menambah posisi tanpa menambah saham beredar | [CorporateActionService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/corporate/CorporateActionService.java:937>) |
| [AU15](#au15) | Sedang | Duel bos tidak mendapat kemajuan dari waktu dunia | [MarketBossService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/boss/MarketBossService.java:60>) |
| [AU16](#au16) | Sedang | State duel bos hilang setelah load dan tertinggal setelah newgame | [MarketBossService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/boss/MarketBossService.java:81>) |
| [AU17](#au17) | Sedang | Dua achievement tidak memiliki jalur progres produksi | [PlayerProgressManager.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/player/PlayerProgressManager.java:396>) |
| [AU18](#au18) | Sedang | Arsip liga mengambil peserta terakhir sebagai pengganti baris pemain | [LeagueManager.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/league/LeagueManager.java:453>) |
| [AU19](#au19) | Sedang | Avatar dan bingkai terpasang tidak masuk save profil | [PlayerProfileManager.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/player/PlayerProfileManager.java:252>) |
| [AU20](#au20) | Sedang | Picker dan foto profil mengabaikan equippedAvatar dari domain | [app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:8520>) |
| [AU21](#au21) | Sedang | Nama pemain dirender sebagai HTML dan dapat menjalankan JavaScript | [app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:8597>) |
| [AU22](#au22) | Sedang | Pengaturan offline menjanjikan pause padahal hanya mengatur laporan | [app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:8770>) |
| [AU24](#au24) | Sedang | Cache generator berita umum tidak dibersihkan saat mulai run baru | [ResearchService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java:74>) |
| [AU25](#au25) | Sedang | Evaluasi kuartal membaca return yang sudah direset | [TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2558>) |
| [AU26](#au26) | Sedang | Warta Whale terbit berdasarkan tanggal tanpa penghubung kejadian ekonomi | [ResearchService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java:682>) |
| [AU29](#au29) | Sedang | Order generator NPC tidak mendapat kedaluwarsa harian | [TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:1535>) |
| [AU30](#au30) | Sedang | Kebijakan katalog reksa dana tidak diterapkan pada backing dan parameter transaksi | [MutualFundManager.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/funds/MutualFundManager.java:40>) |
| [AU31](#au31) | Sedang | Jalur order replay memakai low/high harian sebelum penutupan | [TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:1279>) |
| [AU32](#au32) | Sedang | Modal USD dikredit dua kali saat metode domain memulai replay | [TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:931>) |
| [AU33](#au33) | Sedang | Variasi sentimen generator terikat kategori sehingga cabang tidak pernah tercapai | [ResearchService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java:308>) |
| [AU34](#au34) | Sedang | Booster hanya mengubah flag, tanpa manfaat transaksi, pemakaian, atau persistensi | [ResearchService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java:415>) |
| [AU35](#au35) | Sedang | Feed Basic membocorkan isi target berita yang belum terbit | [ResearchService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java:444>) |
| [AU36](#au36) | Sedang | Perpanjangan terminal terlambat sampai settlement harian, menyebabkan jeda akses berbayar | [ResearchService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java:951>) |
| [AU38](#au38) | Sedang | Tarif produk deposito berubah hanya karena save/load | [TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:786>) |
| [AU39](#au39) | Sedang | Laporan risiko dan ringkasan utang mengabaikan margin USD | [TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2177>) |
| [AU40](#au40) | Sedang | Judul tidak terlihat ketika mode kontras tinggi aktif | [index.html](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/index.html:173>) |
| [AU41](#au41) | Sedang | Label subtab Pusat Keuangan bertumpuk dan terpotong pada layar sempit | [style.css](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/style.css:2897>) |
| [AU42](#au42) | Sedang | Portofolio tab gabungan tidak diperbarui live; label baru tidak cocok dengan updater | [app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:1221>) |
| [AU43](#au43) | Sedang | UI mengarang pengendali dan persentase ownership melebihi 100% | [app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:1528>) |
| [AU44](#au44) | Sedang | Tombol promo PRO/WHALE tidak membuka langganan | [app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:4330>) |
| [AU45](#au45) | Sedang | Promo Whale menjanjikan US$500 ribu, domain menagih US$1 juta | [app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:4337>) |
| [AU46](#au46) | Sedang | Grafik histori kosong diisi30 candle sintetis dengan NaN | [app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:5494>) |
| [AU47](#au47) | Sedang | Statistik pasar di detail saham adalah angka perkiraan tanpa label | [app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:5611>) |
| [AU48](#au48) | Sedang | Klik bid/ask hilang setelah pembaruan live order book | [app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:1751>) |
| [AU49](#au49) | Sedang | Filter tahap e-IPO bergantung tahun2027 dan menganggap semua belum allotted sebagai ongoing | [app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:6436>) |
| [AU50](#au50) | Sedang | Quantity pecahan dipotong diam-diam menjadi long sebelum validasi domain | [TradingWebBridge.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java:1647>) |
| [AU53](#au53) | Sedang | Tiket dan rincian dana tertahan menampilkan saldo pribadi saat akun klien aktif | [app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:9408>) |
| [AU52](#au52) | Rendah | Welcome dan panduan masih menjelaskan24x/IDX24jam | [app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:9837>) |
| [AU55](#au55) | Rendah | Jumlah usaha menampilkan undefined | [app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:2527>) |

<a id="au05"></a>

### AU05 — Kegagalan pembayaran kupon obligasi menghentikan jam dunia

**Keparahan final: Fatal. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/multiasset/MultiAssetTradingService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/multiasset/MultiAssetTradingService.java:130>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
130:     public void settleCoupon(Account owner, Account dealer, EconomyLedger ledger, long now) {
131:         List<CouponSettlement> plan = couponPlan(owner, dealer, now);
132:         Money total = Money.zero(Currency.IDR);
133:         for (CouponSettlement settlement : plan) total = total.add(settlement.due).add(settlement.principal);
134:         if (dealer.getAvailableCash(Currency.IDR).isLessThan(total)) throw new IllegalStateException("Kupon/pelunasan tertunda: likuiditas penerbit kurang");
```

Keparahan Fatal bersyarat. Status Terbukti pada domainJavaQA; frekuensi tercapainya kondisi dealer kekurangan kas melalui permainan normal belum diukur.
Lokasi C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/multiasset/MultiAssetTradingService.java:130–134; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2322,2497,2503–2505.
Masalah: pembayaran kupon memakai mutasi keuangan yang dapat melempar saat kas dealer kurang; exception naik ke penggerak dunia yang memanggil clock.pause. Pemain tidak mempunyai kontrol resume yang sah.
ReproduksiQA: miliki10unit obligasi, buat availablecashdealer0 padafixtureQA, lewati settlement tengahmalam. Probe mencatat clockpausedtrue23:59:59. Setelah kasdealer dipulihkan, advanceforeground tidak memajukan waktu. Tidak mengklaim berkas save terhapus.
Perbaikan: validasi/persiapkan sumber pembayaran kupon sebelum settlement, dengan kejadian ekonomi/funding tercatat sesuai aturan jika memang fasilitas likuiditas menanggungnya; jangan menandai pembayaran sukses bila belumdibayar. Settlement perlu atomic/idempotent dan retry setelah hambatan pulih tanpa menggandakan kupon. Hindari berhenti permanen pada pause tanpa mekanisme pemulihan domain.
Contoh pola (pseudocode, perlu disesuaikan helper existing):
```text
preflight sumber dana seluruh kupon yang jatuh tempo
jika sumber belum siap: simpan kewajiban outstanding, jangan tandai paid
jika siap: mutasi ledger sekali berdasarkan couponId
setelah retry berhasil: lanjutkan boundary waktu yang sama
```
Verifikasi: kasdealer cukup/kurang/tertahan, dua obligasi pada hari sama, retry dan save/reload sebelum/sesudah batas; kupon tidak hilang/ganda dan clock dapat melanjutkan.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
couponId sudah dibayar -> jangan bayar ulang
kas penerbit belum cukup -> kewajiban pending dan retry, jangan pause permanen
```

Catatan bukti terperinci: [FINDINGS-AU05-AU08.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU05-AU08.md>).

<a id="au09"></a>

### AU09 — Short meminjam saham NPC yang masih terikat order jual

**Keparahan final: Fatal. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/risk/RiskComplianceService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/risk/RiskComplianceService.java:226>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
226:         // 0. Validasi ketersediaan inventori pinjaman pada lender pool
227:         if (lenderPool != null) {
228:             Position lenderPos = lenderPool.getPosition(instrument.getId());
229:             if (lenderPos == null) lenderPos = lenderPool.getPosition(instrument.getTicker());
230:             long availableLendable = (lenderPos != null) ? lenderPos.getQuantity() : 0;
```

Keparahan final: Fatal bersyarat (dunia pause pada reproduksi). Status: Terbukti pada domain Java; jam dunia pause setelah pembaruan NPC.

Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/risk/RiskComplianceService.java:226-235; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2096-2105,2503-2505,2621-2622; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/npc/NpcAgent.java:58-70; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:9191-9203.

Kode terkait:
```java
long availableLendable = (lenderPos != null) ? lenderPos.getQuantity() : 0;
lenderPos.borrowShares(sharesToShort);
```

Reproduksi: QA instrumen IDX:QA harga10000 lot100; MMmemiliki20000saham, orderjual100@10025 tetapOPEN. Buka short20000. MM0saham tetapi ask100tetapOPEN. Advance18750msnyata=15menitgame menuju09:15.

Hasil/dampak: Short diterima, generateNpcLiquidity dan tick09:15 melempar Komitmen saham MM melebihi persediaan; clock.isPaused=true.

Bukti: MarginProbe.java, margin-probe.log di folder dokumen ini.

Rekomendasi: Preflight stok bebas memakai getCommittedShares existing SEBELUM borrow/credit/reserve. Domain juga perlu menerima/menghitung stok bebas. Jangan menyembunyikan komitmen rusak dengan pengisian tanpa event ekonomi. Setelah sumber error pulih, sediakan retry dunia yang tidak melewatkan kewajiban.

Contoh inti (sesuaikan API/konteks dan periksa seluruhcaller):
```java
long free = Math.subtractExact(held, getCommittedShares(mm.getId(), inst.getId()));
if (shares > free) throw new IllegalStateException("Stok pinjaman bebas tidak cukup");
```

Verifikasi perbaikan: free,free+1; snapshot tak berubah setelah penolakan; retry/save-load; tick15menit. Tidak mengklaim semua pembukaan short bermasalah.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
free = held - committedShares;
if (requestedShortShares > free) rejectBeforeMutation();
```

Catatan bukti terperinci: [FINDINGS-AU09-AU16.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU09-AU16.md>).

<a id="au01"></a>

### AU01 — Trigger dieksekusi ketika bursa tutup

**Keparahan final: Tinggi. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2459>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
2459:     public synchronized void evaluateTriggers() {
2460:         if (!orderManager.hasActiveTriggers()) {
2461:             if (clock != null && clock.isForeground()) evaluateRecordedCareerProgress();
2462:             return;
2463:         }
```

##### Lokasi dan bukti

- `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2459–2471`, `evaluateTriggers()`; pemanggilan aktivasi pada baris 2469.
- `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/trading/OrderManager.java:117`, `checkAndActivateTriggers()`.
- Pencarian caller produksi saat audit menemukan aktivasi tersebut dipanggil dari facade. Cari ulang seluruh caller sebelum edit.

Potongan relevan:

```java
for (Instrument inst : catalog.getActiveUniverse()) {
    // Pemeriksaan sesi yang ada di sini hanya membatasi HISTORICAL_REPLAY.
    orderManager.checkAndActivateTriggers(inst, marketEngine, ledger, accounts, now);
}
```

Komentar di atas adalah penjelasan dokumen, bukan salinan komentar source. Pada mode INTERNAL_MARKET belum ada pemeriksaan sesi per exchange di loop tersebut. OrderManager mengevaluasi harga trigger dan mengirim order ke MarketEngine; MarketEngine.submitOrder tidak menerima clock/sesi.

Probe audit menghasilkan `marketOpen=false`, waktu `08:30`, Stop-Loss SELL `FILLED`, quantity `100`. Akun, posisi dan bid adalah data QA sintetis. Tersedianya bid lawan merupakan prasyarat reproduksi; tidak semua trigger pasti terisi ketika tutup.

##### Reproduksi

1. Buat game QA INTERNAL_MARKET dan satu instrumen IDX dengan lot sah.
2. Pada 08:30, siapkan posisi pemain 100 saham dan bid lawan yang sah pada order book. Ini merepresentasikan antrean yang tersedia sebelum buka.
3. Pasang STOP_LOSS SELL 100 saham dengan kondisi harga yang sudah terpenuhi.
4. Panggil jalur evaluasi trigger domain.
5. Sebelum perbaikan: order dapat FILLED walaupun bursa IDX tutup.

##### Pembetulan yang disarankan

Gunakan pemeriksaan sesi existing milik clock pada boundary aktivasi facade. Jangan mengubah seluruh aturan MatchingEngine atau membatalkan order hanya karena sesi sedang tutup. Contoh arah perubahan, sesuaikan dengan flow replay yang sudah ada:

```java
if (gameMode != GameMode.HISTORICAL_REPLAY
        && !clock.isTradingOpenFor(inst.getExchange())) continue;
```

Tempatkan sebelum aktivasi trigger, pertahankan validasi replay existing, dan pastikan clock null ditangani sesuai invariant facade. Ketika tutup, order tetap PENDING_TRIGGER dan klaim saham yang valid tetap dipertahankan. Jangan memilih sesi IDX untuk seluruh instrumen; US memiliki sesi berbeda.

##### Acceptance

- STOP_LOSS dan TAKE_PROFIT tidak mengubah uang/saham/fill/ledger saat IDX sebelum 09:00, istirahat 12:00–13:30, atau mulai 16:00; pending tetap utuh.
- US diperiksa terpisah: buka 21:30–04:00 WIB; tutup mulai 04:00. Gunakan timestamp game/fixed clock.
- Ketika sesi relevan buka dan harga masih memenuhi, order diproses sesuai AON dan likuiditas, tepat sekali. Ketika harga tidak memenuhi, tetap pending.
- Halt, pembatalan, komitmen saham, save/reload dan evaluasi berulang tidak menghasilkan fill ganda atau pelepasan klaim keliru.
- Uji caller dunia aktif dan catch-up; jangan hanya mengetes fungsi clock secara terpisah.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
if (!clock.isTradingOpenFor(instrument.getExchange())) continue;
```

Catatan bukti terperinci: [PANDUAN-PERBAIKAN-AU01-AU04.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/PANDUAN-PERBAIKAN-AU01-AU04.md>).

<a id="au11"></a>

### AU11 — Katalog150koleksi hanya terbaca dari filesystem proyek

**Keparahan final: Tinggi. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/collection/CollectionManager.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/collection/CollectionManager.java:309>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
309:     public void restoreState(JSONArray arr) throws JSONException {
310:         CollectionManager staged = new CollectionManager();
311:         Set<String> seen = new HashSet<>(), requests = new HashSet<>();
312:         for (int i = 0; arr != null && i < arr.length(); i++) {
313:             JSONObject obj = arr.getJSONObject(i);
```

Keparahan: Tinggi. Status: Terbukti pada loader host netral; akibat APK didukung source, runtime APK perlu verifikasi.

Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/collection/CollectionManager.java:29-58,309-329; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/MainActivity.java:37-39; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/market/HistoricalDatasetLoader.java:30-38.

Kode terkait:
```java
new File("src/main/assets/data/katalog_koleksi_150.json");
new File("C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/data/katalog_koleksi_150.json");
```

Reproduksi: Jalankan AdditionalProbe dari folderQA netral; pasang AssetSource pembaca aset produksi. Tidak ada kedua folder relatif proyek.

Hasil/dampak: Asset JSON150barang tetapi CollectionManager hanya12fallback. CollectionManager tidak menggunakan AssetSource. MainActivity membuatfacade sebelum memasang AssetSource. Restore membangun katalogcanonical dari loader yang sama sehingga dapat menolak barang nonlegacy dari save.

Bukti: AdditionalProbe.java, additional-probe.log di folder dokumen ini.

Rekomendasi: Gunakan HistoricalDatasetLoader.openAssetStream existing; pasang AssetSource Android sebelum konstruksi facade. Laporkan aset rusak dengan jelas. Jangan fallbackdiam-diam ke12barang pada rilis.

Contoh inti (sesuaikan API/konteks dan periksa seluruhcaller):
```java
HistoricalDatasetLoader.setAssetSource(path -> getAssets().open(path));
facade = new TradingFacade(saveManager);
// di CollectionManager:
try (InputStream in = HistoricalDatasetLoader.openAssetStream("data/katalog_koleksi_150.json")) { /* parse */ }
```

Verifikasi perbaikan: cwdnetral+AssetSource; jumlah150; missing/corrupt; roundtrip itemnonlegacy; periksa jumlah pada APK saat uji Android diizinkan. Tidak mengklaim sudah diuji di perangkat.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
CollectionManager: baca lewat AssetSource domain existing; fallback hanya setelah kegagalan terlaporkan
```

Catatan bukti terperinci: [FINDINGS-AU09-AU16.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU09-AU16.md>).

<a id="au14"></a>

### AU14 — Beberapa order beli klien bisa melewati hard limit konsentrasi

**Keparahan final: Tinggi. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/firm/FirmManager.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/firm/FirmManager.java:150>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
150:         Position currentPos = clientAccount.getPosition(instrument.getId());
151:         long currentQty = currentPos != null ? currentPos.getQuantity() : 0;
152:         long totalQtyAfter = currentQty + order.getTotalQuantity();
153:         BigDecimal postValue = inBase(estPrice.multiply(BigDecimal.valueOf(totalQtyAfter)), instrument.getCurrency(), baseCur, fxRate);
154:         BigDecimal maxConcentrationAllowed = totalAum.getAmount().multiply(BigDecimal.valueOf(mandate.getRiskProfile().getMaxConcentrationPerStock()));
```

Keparahan: Tinggi. Status: Terbukti lewat facade produksi.

Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/firm/FirmManager.java:150-156; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:1260-1270; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/trading/MatchingEngine.java:480.

Kode terkait:
```java
long currentQty = currentPos != null ? currentPos.getQuantity() : 0;
long totalQtyAfter = currentQty + order.getTotalQuantity();
```

Reproduksi: MANDATE-01 AUM100juta batas15%.08:30dua orderbeli1000saham@9900, masing9,9juta.09:00MMjual2000@9900.

Hasil/dampak: DuaorderOPEN kemudianFILLED; posisi19800000/AUM99970300≈19,81%; isWithinLimits=false. Kas tidak negatif, harga tak perlu dimanipulasi.

Bukti: FirmProbe.java, firm-probe.log di folder dokumen ini.

Rekomendasi: Hitung pendingbuy saham yang sama, termasukqueuedmarket/trigger yang relevan, dalam proyeksi sebelum menerima order. Jangan menghitung order sendiri dua kali. Revalidasi pemenuhan pada harga aktual sambil mempertahankanAON.

Contoh inti (sesuaikan API/konteks dan periksa seluruhcaller):
```java
long projected = Math.addExact(held, Math.addExact(pendingBuyShares, incomingShares));
```

Verifikasi perbaikan: Dua order masinglolos gabungangagal; cancel; trigger; antrebuka; lintasmata uang. Proyeksi harus memakai notional/currency/riskpolicy yang konsisten.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
projectedExposure = posisi + seluruh order beli aktif + order baru;
validateMandate(projectedExposure, projectedCash);
```

Catatan bukti terperinci: [FINDINGS-AU09-AU16.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU09-AU16.md>).

<a id="au23"></a>

### AU23 — Memiliki obligasi membuat endpoint portofolio gagal, bahkan setelah posisi dijual habis

**Keparahan final: Tinggi. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java:2119>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
2119:             for (Position p : account.getPositions().values()) {
2120:                 Instrument inst = facade.getCatalog().getById(p.getExchange() + ":" + p.getTicker());
2121:                 long lotSize = inst != null ? inst.getLotSize() : ("US".equalsIgnoreCase(p.getExchange()) ? 1 : 100);
2122:                 String name = inst != null ? inst.getName() : p.getTicker();
2123:                 BigDecimal cost = p.getAverageCost();
```

Keparahan:Tinggi. Status:Terbukti lewat transaksi dan endpoint produksi pada host Java QA.

Lokasi:C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java:2119-2131,2153; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:1059-1062; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:2170.

```java
String instrumentId = p.getExchange() + ":" + p.getTicker();
long committedShares = facade.getCommittedShares(account.getId(), instrumentId);
// helper memanggil requireInstrument(instrumentId), yang hanya mengenali katalog saham
```

Reproduksi:GameQA50juta,pangkatPortfolioManager. Bridge.getPortfolio(personal) sukses. Quote dan eksekusi multiAssetOrder membeli1unitBOND:FR0091, lalu minta portofolio lagi. Jual1unit sampai posisi0, ulangpermintaan.

Hasil/dampak:Sebelumbeli success=true; sesudahbeli {"success":false}; sesudahjualhabis masih {"success":false}. Akun.positions mencakupBOND, tetapi katalog saham tidak mengenaliBOND:FR0091. Exception ditelan menjadi gagal tanpa pesan.

Bukti:BridgeAssetProbe.java, bridge-asset-probe.log.

Rekomendasi:Endpoint membedakan assetclass memakai registry existing. Hanya posisi sahamIDX/US yang memerlukan komitmen order saham; obligasi/komoditas memakai registry dan ukuranunitnya sendiri, atau difilter eksplisit bila daftar ini khusus saham. Jangan mengubah validasi requireInstrument agar instrumen tidak dikenal diterima sembarang. Tangani posisi0 secara konsisten; angka alokasi/total harus mengikuti cakupan aset yang benar. Gunakan bridgeError agar kegagalan bisa didiagnosis.

```java
// Setelah mengenali assetclass dari registry canonical:
long committed = stockInstrument != null ? facade.getCommittedShares(accountId, instrumentId) : 0L;
// untuk multiasset: nama/unit/aksi dari registry multiasset, bukan tombol order saham
```

Verifikasi:Saham saja,bondsaja,saham+bond,komoditas,zeroquantity,setelahsellall,reload; getPortfolio serta tomboljual/UIalokasi tidak gagal.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
if (assetClass != STOCK) jangan panggil requireInstrument khusus saham;
gunakan komitmen sesuai jenis aset
```

Catatan bukti terperinci: [FINDINGS-AU22-AU23.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU22-AU23.md>).

<a id="au27"></a>

### AU27 — Save dengan saldo yang bertentangan dengan ledger dianggap valid dan menggantikan cadangan baik

**Keparahan final: Tinggi. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2853>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
2853:             for (Account account : state.getAccounts()) {
2854:                 if (account.getId().trim().isEmpty() || !accountIds.add(account.getId())) throw new IllegalArgumentException("ID akun save duplikat atau kosong");
2855:                 if (account.getType() == AccountType.PERSONAL) personalAccounts++;
2856:             }
2857:             if (personalAccounts != 1) throw new IllegalArgumentException("Save harus memiliki satu akun personal");
```

Keparahan: **Tinggi**. Status: **Terbukti pada fault injection save QA**. Belum terbukti bahwa aksi permainan normal menghasilkan ketidaksesuaian ini; temuan menyangkut deteksi kerusakan semantik dan perlindungan backup.

Root: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM.
Lokasi:
- C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2853–2858 dan 3037: validasi graph tidak merekonsiliasi kas akun dengan ledger sebelum menyatakan valid.
- C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/storage/SaveManager.java:33–46,99–124,178–182: pemuatan dan rotasi backup mempercayai validator tersebut.
- C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2675–2688: pemeriksa rekonsiliasi tersedia, tetapi tidak dipakai dalam validasi save.

Kode terkait:
```java
new EconomyLedger().restoreEntries(state.getLedgerEntries());
// ... validasi domain lain ...
if (validationOnly) return true;
```

#### Reproduksi dan dampak

1. Buat game QA baru dengan kas pemain Rp50.000.000, lalu simpan sehingga primer dan backup valid tersedia.
2. Pada file primer QA saja, ubah kas IDR pemain menjadi Rp50.000.001; ledger tidak diubah dan JSON tetap valid.
3. Buat facade baru dan load. Hasil: load=true, recoveryOffered=false, tetapi reconcileAll=false.
4. Simpan kembali melalui facade produksi.
5. Kas primer dan backup sama-sama Rp50.000.001. Salinan cadangan baik sebelumnya Rp50.000.000 sudah tergantikan oleh state yang tidak cocok dengan ledger.

Bukti: SaveSemanticProbe.java dan save-semantic-probe.log di folder dokumen ini. Tidak ada akses isi save pemain produksi.

Ini bukan klaim file JSON terpotong tidak ditangani: jalur parser rusak/struktur tidak valid sudah memiliki fallback. Kelemahannya adalah angka yang secara bentuk valid tetapi tidak konsisten dengan pembukuan tetap dianggap last-good, lalu masuk rotasi backup.

#### Rekomendasi

Gunakan ledger hasil restore pada validasi detached, sebelum live state atau file lama dinyatakan valid. Helper reconcile sudah tersedia. Pemeriksaan minimal untuk personal/client:
```java
EconomyLedger stagedLedger = new EconomyLedger();
stagedLedger.restoreEntries(state.getLedgerEntries());
for (Account account : state.getAccounts()) {
    if (account.getType() != AccountType.PERSONAL && account.getType() != AccountType.CLIENT) continue;
    for (Currency currency : Currency.values()) {
        if (!stagedLedger.reconcile(account, currency))
            throw new IllegalArgumentException("Kas save tidak cocok dengan ledger");
    }
}
```

Telusuri juga akun perusahaan dan konsistensi saldo-after setiap entry. Jangan mengubah saldo agar cocok secara diam-diam; data legacy yang memakai saldo pembukaan tanpa ledger membutuhkan kebijakan migrasi yang jelas. Hindari menghitung seluruh ledger berulang untuk1000NPC bila pemeriksaan dapat dikelompokkan sekali.

#### Verifikasi perbaikan

- Save valid tetap roundtrip.
- Selisih kas satu rupiah/satu sen, ledger hilang/duplikat, dan ketidaksesuaian saldo-after ditolak sesuai kontrak.
- Ketika primer tidak konsisten dan backup valid, tawarkan pemulihan dari backup.
- Ketika menyimpan state live valid di atas primer tidak konsisten, pertahankan backup baik.
- Kegagalan validasi tidak mengganti state live dan tidak merusak save.
- Uji biaya validasi pada riwayat panjang, bukan hanya fixture kecil.

Source tidak diperbaiki dalam audit ini.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
jika cash != saldo ledger yang tervalidasi -> tolak kandidat save dan pertahankan backup valid
```

Catatan bukti terperinci: [FINDING-AU27.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDING-AU27.md>).

<a id="au28"></a>

### AU28 — Stop-loss dan take-profit dapat terisi sebagian, melanggar AON pemain

**Keparahan final: Tinggi. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/trading/MatchingEngine.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/trading/MatchingEngine.java:243>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
243:         if (isPlayerAccount) {
244:             if (order.getType() == OrderType.MARKET) {
245:                 // Preserve the existing market-order cancellation policy without partial settlement.
246:                 if (!canFillPlayerGreedily(order, instrument, book, allAccounts)) {
247:                     order.cancel();
```

- Keparahan: Tinggi. Status: Terbukti melalui domain produksi dengan fixture QA sintetis.
- Lokasi akar: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/trading/MatchingEngine.java:243–274,314–328. Pemicu: OrderManager.java:155–162. Facade: TradingFacade.java:1274–1276,2459–2469.
- Bukti kode: `if (order.getType() == OrderType.MARKET)` memeriksa ketersediaan penuh; cabang lain hanya memeriksa penuh di `if (order.getType() == OrderType.LIMIT)`. STOP_LOSS/TAKE_PROFIT langsung memanggil `matchSellOrder`/`matchBuyOrder`, kemudian membatalkan sisa.
- Reproduksi: game QA pada 09:00, pemain memegang 200 saham QA, satu bid MM 100 saham @Rp10.000. Kirim SELL STOP_LOSS 200 dengan trigger Rp10.000 lalu `evaluateTriggers()`. Ulangi TAKE_PROFIT pada game QA baru.
- Hasil masing-masing: accepted=true, filled=100, status=CANCELLED, saham tersisa100, kas Rp50.997.500. Jadi order bukan batal utuh dan bukan terisi penuh. Berbeda dengan AU01: ini terjadi saat bursa BUKA; menambah guard sesi saja tidak memperbaikinya.
- Dampak: posisi perlindungan risiko hanya tertutup sebagian sementara sisa order berhenti aktif; aturan transaksi pemain dan ekspektasi penjualan utuh dilanggar.
- Saran minimal: setelah pemicu aktif, perlakukan sebagai market-style execution untuk pemeriksaan penuh dan pembatalan utuh, tanpa mengubah jenis/riwayat order. Gunakan helper `canFillPlayerGreedily` yang sama sebelum mutasi settlement. Contoh kondisi bersama: `boolean marketStyle = order.getType() != OrderType.LIMIT;` lalu gunakan pada cabang AON market-style yang sudah ada. Tetap validasi sesi bursa terpisah (AU01). Periksa semua caller trigger, preview, buy, sell, dan restore.
- Check minimum: untuk kedua jenis trigger, kedua sisi, likuiditas kurang => filled0 dan tidak ada mutasi kas/posisi; likuiditas cukup => filled seluruhnya; tidak ada saldo tertahan tersisa pada pembatalan. Jangan mengubah trigger menjadi limit antre tanpa keputusan produk.
- Bukti runnable: OrderAuditProbe.java; order-audit-probe.log.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
boolean marketStyle = order.getType() != OrderType.LIMIT;
// gunakan preflight AON market-style juga untuk trigger yang aktif
```

Catatan bukti terperinci: [FINDINGS-AU28-AU29.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU28-AU29.md>).

<a id="au37"></a>

### AU37 — Permintaan game baru dengan nama tidak valid tetap mereset progres

**Keparahan final: Tinggi. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java:2425>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
2425:             facade.startInternalGame(BigDecimal.valueOf(capital));
2426:             String playerName = cfg.optString("playerName", "").trim();
2427:             if (!playerName.isEmpty() && facade.getPlayerProfileManager() != null) {
2428:                 facade.getPlayerProfileManager().setPlayerName(playerName);
2429:             }
```

- Keparahan Tinggi; status Terbukti pada bridge/domain produksi. Keterjangkauan form dikonfirmasi dengan fungsi UI produksi pada Chromium QA.
- Lokasi akar: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java:2425–2430. Reset: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:986–991,775–812. Validasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/player/PlayerProfileManager.java:116–119. UI: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:5102–5107,5168–5190.
- Kode: `facade.startInternalGame(...)` dipanggil sebelum `setPlayerName(playerName)`. Exception nama mengembalikan successfalse setelah reset terjadi. Form tidak membatasi panjang nama.
- Repro: gameQA mempunyai kasRp55juta (50juta + fixturedeposit5juta via ledger), namaQA Existing Run, sudahsave. Kirim bridge.startNewGame modal50juta dan nama49karakter. Respons successfalse dengan pesan validasi, tetapi kasmemori menjadi50juta dan nama default. Save2kali berikutnya lalu load primer/backupQA: keduanya50juta.
- Browser: fungsi openNewGameConfirmationModal verbatim, helperDOMminimal, bridgefake merekamrequest/mengembalikan responsgagal. Input49karakter terkirim, toastGagalMemulaiBaru muncul, tombolaktifkembali. Ini bukti keterjangkauanform, bukan fullAndroidE2E.
- Dampak: request yang dilaporkan gagal tetap mengganti permainan; autosave dapat menggulung progres lama keluar dari primer danbackup. Konfirmasi reset pemain tidak membenarkan mutasi untuk requestinvalid yang dinyatakan gagal.
- Perbaikan minimum: validasi seluruh config sebelum startInternalGame dengan aturan nama existing. Contoh `new PlayerProfileManager().setPlayerName(playerName);` sebelum reset bila nama nonkosong, lalu terapkan nama tervalidasi setelah initialize. Gunakan helper validasi bersama bila tersedia. Tambahkan validasi UI sebelum konfirmasi; domain tetap wajib. Jangan menutupi exception sesudah reset.
- Check: nama49karakter/kontrolinvalid => responsgagal dan runId/kas/posisi/nama/saveprimer/backup tidakberubah; autosave berikutnya tetapmempertahankanrunlama. Nama valid meresetlengkap.
- Bukti: FacadeBoundaryProbe.java; facade-boundary-probe.log; ui-newgame-probe.py/log.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
validasi seluruh konfigurasi dan nama sebelum startInternalGame/reset apa pun
```

Catatan bukti terperinci: [FINDINGS-AU37-AU38-FACADE.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU37-AU38-FACADE.md>).

<a id="au51"></a>

### AU51 — Menu aktif kehilangan akses terminal, pengelolaan paket, lab dan beberapa fitur existing

**Keparahan final: Tinggi. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:4710>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
4710:     function loadRisetView() {
4711:         refreshSnapshot();
4712:         const view = views.riset;
4713:         view.replaceChildren();
4714: 
```

- Keparahan Tinggi (akses fitur dan pembatalan auto-renew). Status Terbukti pada penelusuran seluruh source produksi dan navigasi halaman produksi di browser QA; belum uji Android.
- Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:4710–4779(loadRisetView),3782(openResearchTerminal),3811(KelolaPaket),3918/3939–3951(pembatalan),4237–4347(Radar),6901(openStressTestModal),6948(openStrategyLabModal),8274(openCustomFundBuilderModal). Semua di android/app/src/main/assets/www.
- Bukti: pencarian caller pada app.js/index.html/nativeJava hanya menemukan definisi openResearchTerminal/openStressTestModal, serta selfcallback untuk lab/customfund. Tidak ada launcher aktif dari navigasi. openResearchTiersModal diacu oleh terminal yang juga tidak terjangkau, selfcallback, dan dua inlineCTA rusak AU44. Pada Radar ketika Pro aktif, cabang4237–4264 hanya menampilkan banner, tidak menyediakan tombol kelola/cancel.
- Dampak: pemain Pro tidak memiliki alur UI aktif untuk menghentikan auto-renew meski fungsi domain/layar pembatalan ada; terminal intelijen, stress test dan lab yang masih ada source-nya juga tidak dapat dicapai dari menu. Ini bukan bukti kehilangan uang di luar kebijakan biaya langganan; masalahnya pemain tidak bisa memakai kontrol pembatalan yang dijanjikan.
- Reproduksi: buka halaman produksi dengan profilQAPro aktif(SeniorAnalyst). Tab Lainnya menampilkan15launcher yang terekam pada ui-full-probe.log, tanpa Terminal Riset/KelolaPaket/Lab/StressTest. PusatKeuangan->Radar menampilkan PRO aktif namun radarButtons=[]. Pada Basic hanya ada2CTAyangkeduanya gagal AU44. Tidak ada booterror/missingasset/unsupportednativecall pada kedua skenario. ScreenshotproRadar mengonfirmasi tidak ada tombol kelola.
- Saran: pulihkan jalur masuk menuju fungsi existing, khususnya kelola paket yang selalu tersedia bagi pelanggan aktif; ikuti gate karier masing-masing untuk fitur lain. Jangan menulis ulang domain, menghapus fitur atau menambah navigasi baru tanpa preview/persetujuan. Contoh arah minimal di lokasi menu yang disetujui:
```js
flowButton(section, 'Kelola paket riset', openResearchTiersModal);
flowButton(section, 'Terminal riset', openResearchTerminal);
```
  Tombol pembatalan harus tetap bisa diakses setelah pemain turun pangkat atau akses berbayar belum habis; aturan pembukaan fitur baru tetap mengikuti domain.
- Verifikasi: Basic,Pro,Whale,autoRenewon/off,masaaktif/expired; pemain dapatmembuka kelola->cancel->statusakseshinggaexpiry. Lab/stres/customfund dapatdicapaidengan gateyangbenar. Tidak perlu mengaktifkan kembali replayhistoris yang pembuatannya memang dinonaktifkan; fungsi replay bukan bagianremediasiini.
- Koreksi cakupanAU44: dokumen sebelum fullnavigation sempat menyebut jalurKelolaPaket sebagai alternatiftersedia. Yang tersedia baru fungsi/callback-nya; hasil penelusuran terkini menunjukkan entry terminalnya tidak terjangkau. Gunakan AU51 ini saat menyusun laporanfinal; jangan mengulangi klaim alternatifUItersedia.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
flowButton(section, 'Kelola paket riset', openResearchTiersModal);
```

Catatan bukti terperinci: [FINDINGS-AU51-AU52-NAVIGATION.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU51-AU52-NAVIGATION.md>).

<a id="au54"></a>

### AU54 — Fast-forward melewati pemenuhan target berita Whale

**Keparahan final: Tinggi. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2621>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
2621:                         if (!fastForward && clock.getCurrentTime().getMinute() % 15 == 0) {
2622:                             generateNpcLiquidity();
2623:                         }
2624:                         if (!fastForward && clock.getCurrentTime().getMinute() % 30 == 0 && clock.isTradingOpenFor(com.tradingglobal3d.core.market.MarketExchange.IDX)) {
2625:                             for (Order npcOrder : leagueManager.getNpcOrders(clock.getGameTimestamp())) submitOrder(npcOrder);
```

Keparahan Tinggi. Status Terbukti pada generator dan katalog produksi di JavaQA terisolasi; bukan ujiAndroid/perangkat atau bukti semua targetforegroundpasti gagal.
Root: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM

#### Lokasi dan kode
- C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2531,2545–2550,2621–2625: fast-forwardketikabackgroundatau>3600detikgame, generateNpcLiquidity hanyauntuk!fastForward.
- C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/npc/MarketEngine.java:248–288: satujalurdoronganhargatargetberitaberadadipemrosesanNPCtersebut.
- C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java:583–606: evaluasihanyamengukurpencapaiandanmenandaiexpired;682–697/700–735menggenerateWhalebase/followup.
```java
boolean fastForward = !clock.isForeground() || total > 3600;
// ...
if (!fastForward && clock.getCurrentTime().getMinute() % 15 == 0) {
    generateNpcLiquidity();
}
```

#### Bukti dan dampak
NewsOfflineProbe.java/logmengikutiurutanloadernative,darikatalogAndroidproduksi100 instrumen,rankQASeniorAnalyst,USDQA1juta yangtercatatledger,berlanggananENTERPRISEdandomaindynamicDailyNewsEnabled=true. MelaluiadvanceRealMs14harigame(background)tanpamengeditfileharga:
- clockberakhir2026-11-02,paused=false,tierENTERPRISE.
- Generatorberitaproduksimembuat NEWS-WHALE-2026-10-24 untukBRPT: reference5000,target6250(+25%),deadline2026-10-27; setelahdeadline statusEXPIRED_UNACHIEVED,progress0,currentPrice5000.
- Rumorlanjutan2026-10-31untukBRPTbenar+50%target7500,reference5000; padaNov2masihprogress0. BelummelewatideadlineNov3padasampelini, jadi jangansebutrumorlanjutanini sudahexpired.

Targetberbayar dapat kedaluwarsa tanpa dorongan pasar saatpemainoffline atauketika foregroundmenerimaelapsedbesar. Aturan48xdanpenghindaranantreanNPCmasa lalutetapbenar; masalahnya janji targetpenutupan tidakdiproses sebagai kejadiannesensial. Ada data/targetbaru, jadi inibukanmasalahgeneratorberhentimembuatberita(AU24/26berbeda).

#### Reproduksi
1. GameQAbaru denganassetAndroidproduksi;beliWhalesahmenggunakandanaseedQA.
2. AktifkangeneratorsepertisetupMainActivity.
3. Jalankan14hari melaluiadvanceRealMs(14*1800000,false) padaheap384MB.
4. PeriksagetNewsPriceTargets/priceBRPTdanstatusdeadline. Probeberjalan1–2detik,lulusdarisegiselesaidantidakpause,namunmembuktikanpelanggaranperilakutarget.

#### Rekomendasi
Pisahkanpemenuhan kewajiban targetberita yangdisahkan dari pembuatanquoteNPCrutin: jalankansatuperhitunganbataspenutupan yangidempotent, menggunakantargetgabungan,pinnedreference,hakefek/deadlinepersisten,fraksihargasah,pricebanddankelangsunganjumlahsaham/kas. Reusealgoritmagabungan/pembulatanexisting, janganmemutarmilyaranticklama atauassignhargaUIsecara langsung. Jika pemenuhan memerlukan transaksiNPC, gunakan matching/ledgerdanfasilitaslikuiditasekonomitercatatyangsudahada.
```text
pada batas penutupan bursa instrumen:
    ambil target gabungan yang benar-benar aktif/berhak pada batas itu
    proses kewajiban target penutupan sekali dengan jalur ekonomi domain
    baru evaluasi pencapaian/expiry dan tutup sesi
```
Contohdiataspseudocodearahperbaikan, bukanAPIbaruyangtelahditambahkan. Pemilikperlumengesahkansemantikjaminan bilaadakonfliknyatadenganband,hakberitasudahhabis,atauintervensipemain; janganmemilihperubahanekonomibaruuntukmeluluskantes.

#### Verifikasi
Bandingkanpemrosesanaktifbertahap,background,offlinelama;ambilbatas16:00IDX/04:00USyangrelevan,konflik+5%-3%,net0,fraksiharga,referenceyangdipin,hakberita60/120menitnyata,save/reloadtepatdisekitarpenutupan. Pastikankas/supplyrekonsiliasi,memorytetapterbatasdantidakdoubleprocesstarget. JanganmembuktikanpemenuhantargetsekadardenganmengubahstatusmenjadiACHIEVED.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
batas penutupan -> proses kewajiban target aktif sekali -> evaluasi pencapaian -> settlement sesi
```

Catatan bukti terperinci: [FINDING-AU54-NEWS-OFFLINE.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDING-AU54-NEWS-OFFLINE.md>).

<a id="au02"></a>

### AU02 — Pemilih perusahaan tersembunyi pada pembukaan awal

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:7390>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
7390:         companySection.style.display = 'none';
7391: 
7392:         const collateralSection = document.createElement('div');
7393:         collateralSection.style.display = 'none'; // Agunan dilepas (A16)
7394:         const collateralSelect = document.createElement('select');
```

##### Lokasi dan bukti

`C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js`:

- Baris 2880: tombol perusahaan membuka `openDisburseLoanModal('LOAN-CORP-01', c.id)`.
- Baris 7347: fungsi `openDisburseLoanModal()`.
- Baris 7390: `companySection.style.display = 'none'`.
- Baris 7399–7401: handler onchange mengatur display, tetapi tidak dijalankan saat inisialisasi.
- Baris 7486–7487: updater lain hanya mengatur hidden, serta menganggap personal hanya LOAN-PERS-01.

```js
companySection.style.display = 'none';
// ...
const updateLoanKind = () => {
    const personal = pSelect.value === 'LOAN-PERS-01';
    collateralSection.hidden = !personal;
    companySection.hidden = personal;
};
```

Probe fungsi produksi: produk LOAN-CORP-01 dan perusahaan CO-B sudah terpilih, tetapi pemilih tidak terlihat. Ganti produk ke personal lalu kembali ke perusahaan membuatnya terlihat. Belum ada bukti dana masuk ke perusahaan yang salah.

##### Pembetulan yang disarankan

Satukan updater visibilitas existing, panggil saat inisialisasi dan onchange, dan jangan mencampur inline display yang tidak direset dengan hidden. Arah minimum:

```js
const updateLoanKind = () => {
    const corporate = pSelect.value === 'LOAN-CORP-01';
    companySection.hidden = !corporate;
};
```

Hapus atau selaraskan `display:none`/handler lama untuk bagian perusahaan agar hidden benar-benar mengontrolnya. Pertahankan keputusan produk tanpa agunan existing; jangan menghidupkan produk/agunan lain hanya karena kode historis masih ada. Produk personal yang tampil sekarang LOAN-PERS-02 juga harus dianggap personal.

##### Acceptance

- Pembukaan langsung dari perusahaan A/B menampilkan perusahaan yang benar sejak frame awal, tanpa perlu mengganti produk.
- Pemain dapat memilih perusahaan lain yang sah; quote dan konfirmasi memakai pilihan itu.
- Produk personal menyembunyikan bagian perusahaan tanpa memblokir pengajuan personal.
- Berganti jenis produk berulang tidak menghasilkan hidden/display yang bertentangan atau handler ganda.
- Tidak ada perusahaan: beri state yang dapat dipahami tanpa mengirim companyId kosong. Perubahan teks/alur diperlihatkan melalui preview.
- Input berubah setelah quote tetap meminta review ulang; data penerima tidak berubah diam-diam pada konfirmasi.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
companySection.style.display = isCorp ? '' : 'none';
```

Catatan bukti terperinci: [PANDUAN-PERBAIKAN-AU01-AU04.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/PANDUAN-PERBAIKAN-AU01-AU04.md>).

<a id="au03"></a>

### AU03 — Tanggal pilihan kalender tidak menyaring daftar

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:4606>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
4606:             const currentSimMs = Number(latestSnapshot?.clock?.timestamp) || anchorGameTimestamp || Date.now();
4607:             const activeDate = new Date(currentSimMs + currentKalenderDayOffset * 86400000);
4608:             const dateStr = activeDate.toLocaleDateString('id-ID', { timeZone: 'Asia/Jakarta', day: 'numeric', month: 'short', year: 'numeric' }).toUpperCase();
4609:             headerTitle.innerHTML = `<span class="eyebrow" style="color:var(--gold);">AGENDA · ${currentKalenderDayOffset === 0 ? 'HARI INI (' + dateStr + ')' : 'TANGGAL ' + dateStr}</span>`;
4610:         }
```

##### Lokasi dan bukti

`C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js`:

- Baris 4023: `currentKalenderDayOffset`.
- Baris 4529: `renderKeuanganKalender()`.
- Baris 4571: memilih offset tanggal dan merender ulang.
- Baris 4607–4609: tanggal pilihan dipakai pada judul.
- Baris 4647–4654: loop agenda riset memfilter kategori, belum tanggal.

```js
rf.calendar.forEach(ev => {
    // Filter kategori DIV/EARN/MACRO yang ada belum memeriksa ev.date.
});
```

Probe fungsi produksi dengan tanggal game 2 November, agenda 1 November dan 3 November: kedua agenda muncul pada HARI INI dan tetap sama pada BESOK. Perbaikan tanggal ribbon sebelumnya tetap benar; masalah ada pada isi daftar.

##### Pembetulan yang disarankan

- Turunkan satu tanggal pilihan dari clock domain + offset dalam WIB, memakai helper existing bila ada. Hindari tanggal komputer sebagai sumber utama.
- Pada BY_DAY, cocokkan tanggal kejadian dengan tanggal pilihan sebelum membuat kartu.
- Pada ALL_DAYS, jangan gunakan filter tanggal pilihan; urutkan agenda berdasarkan tanggal, dengan urutan stabil untuk tanggal sama.
- Contoh filter berikut hanya arah untuk agenda yang ev.date-nya sudah diverifikasi sebagai ISO YYYY-MM-DD; jangan menebak format data:

```js
if (currentKalenderViewMode === 'BY_DAY' && ev.date !== selectedDateKey) return;
```

- Telusuri juga kartu IPO dan deposito dalam renderer yang sama. Untuk kejadian satu tanggal, gunakan tanggal domain yang relevan. IPO bisa berupa rentang penawaran; deposito perlu field jatuh tempo yang sah. Jika makna tanggal/rentang belum ditetapkan, ajukan pilihan konkret kepada pemilik, jangan mengarang aturan baru.
- Penanda titik dan jumlah kategori harus berasal dari kejadian yang benar atau diberi status sesuai data; jangan menambah angka hardcoded baru.
- Gunakan empty state existing ketika tanggal/kategori tidak memiliki agenda. Preview menampilkan contoh hari kosong, hari berisi agenda, dan ALL_DAYS sebelum penerapan.

##### Acceptance

- Agenda kemarin tidak muncul sebagai agenda hari ini/besok pada BY_DAY; agenda besok muncul hanya pada tanggal yang benar.
- ALL_DAYS menampilkan kejadian yang sama dalam urutan tanggal yang benar, tanpa kehilangan item atau duplikasi.
- Filter kategori dan tanggal berlaku bersamaan.
- Crossing tengah malam WIB, bulan/tahun, offset negatif/positif, dan perbedaan timezone host tidak menggeser tanggal.
- Uji fixture lintas 31 Desember–1 Januari serta data tanggal invalid/kosong; jangan diam-diam menganggapnya agenda hari ini.
- Terapkan aturan tanggal konsisten pada seluruh sumber agenda yang sudah mempunyai keputusan jelas; catat yang memerlukan keputusan.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
if (event.date !== selectedDate) return; // gunakan tanggal WIB dari domain
```

Catatan bukti terperinci: [PANDUAN-PERBAIKAN-AU01-AU04.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/PANDUAN-PERBAIKAN-AU01-AU04.md>).

Tambahan bukti kalenderIPO: renderer memeriksa ipoRes.offerings padahal bridge mengembalikanJSONArray;10IPO dari domain menghasilkan0baris kalender. Perbaiki kontrak array sekaligus tanggal dan pengurutan, bukan salah satunya saja. Lihat [FINDINGS-AU49-AU50-IPO.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU49-AU50-IPO.md>).

<a id="au04"></a>

### AU04 — JSON error save tidak di-escape

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java:2305>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
2305:             return "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}";
2306:         }
2307:     }
2308: 
2309:     @JavascriptInterface
```

##### Lokasi dan bukti

`C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java:2296–2305`, `saveGame()`:

```java
return "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}";
```

Pesan exception dimasukkan langsung ke JSON. Probe fault write pada save QA menghasilkan pesan berisi path Windows; `new JSONObject(response)` gagal. Efek konkretnya: UI gagal memparse respons dan pesan penyebab save gagal tidak tersampaikan dengan benar. Ini tidak membuktikan progres pemain hilang. Reproduksi perangkat Android belum dilakukan.

##### Reproduksi

1. Buat save QA baru yang valid.
2. Buat direktori penghalang pada path temporary save QA, sehingga penulisan berikutnya gagal.
3. Panggil bridge.saveGame(), kemudian parse hasilnya sebagai JSON.
4. Tambahkan check pesan berisi backslash, kutip dan newline tanpa memakai data pengguna.

##### Pembetulan yang disarankan

Gunakan serializer JSON existing, jangan membuat fungsi escaping sendiri. Contoh ekspresi di dalam flow error yang menangani JSONException sesuai API proyek:

```java
new JSONObject().put("success", false)
    .put("message", e.getMessage() == null ? "Penyimpanan gagal" : e.getMessage())
    .toString();
```

Pertahankan notifySaveFailed(true), blokir transaksi pada domain, serta proteksi primary/backup. Jika helper error serializer yang sesuai sudah ada, gunakan helper itu. Jangan memasukkan isi save atau detail sensitif ke pesan/log. Pesan produksi baru yang mengubah UI mengikuti persetujuan yang berlaku.

##### Acceptance

- Respons gagal parseable oleh JSONObject dan JSON.parse untuk pesan dengan `\`, `"`, newline, teks biasa, dan pesan null.
- `success` tetap false; karakter pesan yang diizinkan bertahan round-trip.
- Save gagal tidak dianggap sukses; primary/backup valid dipertahankan dan transaksi tetap diblokir sesuai kebijakan existing.
- Sesudah blocker QA dihapus, retry berhasil dan alur pemulihan existing tetap bekerja.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
return new JSONObject().put("success", false).put("message", e.getMessage()).toString();
```

Catatan bukti terperinci: [PANDUAN-PERBAIKAN-AU01-AU04.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/PANDUAN-PERBAIKAN-AU01-AU04.md>).

<a id="au06"></a>

### AU06 — Nilai indeks melompat kembali ke basis pada pergantian hari

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/market/MarketIndex.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/market/MarketIndex.java:77>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
77:                 BigDecimal baseCap = inst.getPreviousClosePrice().getAmount().multiply(BigDecimal.valueOf(effectiveShares));
78:                 totalCurrentCap = totalCurrentCap.add(curCap);
79:                 totalBaseCap = totalBaseCap.add(baseCap);
80:             }
81:         }
```

Keparahan Sedang. Status Terbukti pada domainJavaQA.
Lokasi C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/market/MarketIndex.java:77; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/market/Instrument.java:197.
Masalah: indeks dihitung fixedbase dikali rasio harga current terhadap previousClose, kemudian previousClose diganti current setiap hari. Ini menghapus perubahan kumulatif.
Reproduksi: anggota10000->11000 memberiindeks1100; startNewTradingDay tanpa harga berubah mengembalikanindeks1000. Laba/rugi portofolio tidak otomatis berubah karena nilaiindeksini.
Saran: gunakan divisor/baseline indeks yang persisten atau chain nilai penutupan sesuai metodologi game yang disahkan. Jangan mengubah bobot ekonomi sepihak. Jika desainnya indikator perubahan harian, label bukan levelindeks kumulatif.
```text
nilai indeks = nilai penutupan indeks sebelumnya * rasio perubahan anggota sesi ini
```
Simpan penutupan/divisor bersamaan dengan save; uji harga tetap saat rollover, beberapa hari naik/turun dan reload.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
indexCloseSebelumnya * perubahanHargaTertimbangSesiIni
```

Catatan bukti terperinci: [FINDINGS-AU05-AU08.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU05-AU08.md>).

<a id="au07"></a>

### AU07 — Alokasi reksa dana kustom menerima bobot negatif

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:3895>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
3895:         double totalPct = eqPct + commPct + cryptoPct + cashPct;
3896:         if (Math.abs(totalPct - 100.0) > 0.01) throw new IllegalArgumentException("Total alokasi aset harus 100%");
3897: 
3898:         String fundId = "FUND-CUST-" + (customMutualFunds.size() + 1);
3899:         JSONObject obj = new JSONObject();
```

Keparahan Sedang. Status Terbukti pada domainJavaQA.
Lokasi C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:3895 dan validasi jumlah bobot di sekitarnya; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:8391–8401.
Masalah: pemeriksaan hanya jumlah100%, bukan batasmasingmasingbobot. UIpreviewclampke0, tetapi payloadsubmitmemakainilaiaslinegatif.
ReproduksiQA: equity=-50,commodity=150,crypto=0,cash=0;jumlah100diterima. Belumdibuktikaneksploituang/asettakterbatas; temuanadalahstatealokasitidaksah.
Saran: validasi seluruhbobot finite dan0..100 sebelummenciptakanfund, baruperiksajumlah; gunakan batas yangsama padaUI.
```java
for (double weight : weights) {
    if (!Double.isFinite(weight) || weight < 0 || weight > 100)
        throw new IllegalArgumentException("Bobot harus 0–100 persen");
}
```
Contohmerupakanpola,variabelarraydisesuaikandenganparameterexisting. Uji negatif,>100,NaN/Infinity,jumlah100/99.8daninputkosongtanpamutasi.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
if (!Double.isFinite(weight) || weight < 0 || weight > 100) throw new IllegalArgumentException();
```

Catatan bukti terperinci: [FINDINGS-AU05-AU08.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU05-AU08.md>).

<a id="au08"></a>

### AU08 — UI mengabarkan sukses ketika domain menolak tindakan

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:8401>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
8401:                         const res = JSON.parse(bridge.createCustomMutualFund(JSON.stringify(payload)));
8402:                         alert(`Reksa dana kustom ${res.fundName} berhasil diterbitkan dengan dana kelolaan awal ${res.totalAum}!`);
8403:                         refreshSnapshot();
8404:                         openCustomFundBuilderModal();
8405:                     } catch(err) { alert('Gagal menerbitkan: ' + err.message); }
```

Keparahan Sedang. Status Terbukti pada fungsiUI danresponsdomainQA; cakupanditambahpadadokumenAU53.
Lokasi C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:8401–8404(customfund),10148–10151(teknologi),10198–10201(rekrutmen).
Masalah: JSONparseberhasil dianggaptransaksiberhasil, meskipunsuccess=false. Contohresponspenolakanfundtetapmemunculkanalertfundberhasilditerbitkan; teknologi/rekrutmenmenghasilkanpesanundefinedberhasildenganerrorinsufficientcashyangsebenarnyatersedia.
Reproduksi: kembalikanresponsepenolakandomainyangvalidbukanexceptionJSON. Untukteknologi/rekrutmen,UiAccountFailureProbedenganpersonal0menghasilkanresponsnyatafalse25jt/15jt;ui-account-failure-probemenunjukkanfeedbacksukses.
Saran: reusebridgeResult padaalurtersebut,barutampilkansuksesdanrefreshsetelahhasilvalid. Janganmengubahaturanbiayaatau membuatfallbackdomain.
```js
const res = bridgeResult('createCustomMutualFund', JSON.stringify(payload));
```
Verifikasi: sukses,gagaldomain,gagaltransport/JSON,savegagal;feedbackmembedakanpenolakandaristatusbelumterkonfirmasidantidakmengklaimmutasiberhasil.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
const res = bridgeResult('createCustomMutualFund', JSON.stringify(payload));
```

Catatan bukti terperinci: [FINDINGS-AU05-AU08.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU05-AU08.md>).

Perluasan runtime terkonfirmasi teknologi/rekrutmen beserta respons penolakan kas nyata: [FINDING-AU53-AND-AU08-EXTENSION.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDING-AU53-AND-AU08-EXTENSION.md>). Ini tetap satu temuan pola feedback; tidak dihitung ulang sebagaiAUbaru.

<a id="au10"></a>

### AU10 — Biaya pinjaman short hilang ketika kas tidak cukup

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/risk/RiskComplianceService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/risk/RiskComplianceService.java:399>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
399:         Map<String, ShortPosition> userShorts = shortPositions.get(account.getId());
400:         if (userShorts != null) {
401:             for (ShortPosition sp : userShorts.values()) {
402:                 Instrument inst = catalog.getById("IDX:" + sp.getTicker());
403:                 if (inst == null) inst = catalog.getById("US:" + sp.getTicker());
```

Keparahan: Sedang. Status: Terbukti.

Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/risk/RiskComplianceService.java:399-423.

Kode terkait:
```java
if (dailyFee.isPositive() && available.isGreaterThanOrEqual(dailyFee)) { /* debit fee */ }
// tidak ada pencatatan kewajiban bila kas kurang
```

Reproduksi: Kas awal0, short100@10000 menghasilkan997500yang seluruhnya menjadi collateral. Jalankan akrualharian.

Hasil/dampak: dailyFee69, available0, ledgerbaru0, exportState.debts kosong; fee tidak masuk tunggakan.

Bukti: MarginProbe.java, margin-probe.log di folder dokumen ini.

Rekomendasi: Persistensikan biaya belum dibayar dan periode akrual idempoten; debit kas tersedia melalui ledger dan pertahankan sisa sebagai liability. Kebijakan pemotongan collateral atau bunga atas tunggakan harus disahkan pemilik.

Contoh inti (sesuaikan API/konteks dan periksa seluruhcaller):
```java
Money paid = min(available, feeDue);
Money unpaid = feeDue.subtract(paid);
// debit paid melalui ledger; persist unpaid sebagai biaya terutang
```

Verifikasi perbaikan: Kas0/kurang/cukup; retrytanggal sama; reload; cover parsial/penuh. Jangan otomatis mengubahnya menjadi margin debt berbunga18%.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
dailyFee - feePaid -> tambah kewajiban biaya belum dibayar dengan ID settlement persisten
```

Catatan bukti terperinci: [FINDINGS-AU09-AU16.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU09-AU16.md>).

<a id="au12"></a>

### AU12 — Proyeksi pendapatan dan laba bisnis memakai kapasitas, bukan rumus pendapatan

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/business/CompanyManager.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/business/CompanyManager.java:570>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
570:                 .put("projectedDailyRevenueBefore", company.calculateDailyCapacity().getAmount().toPlainString())
571:                 .put("projectedDailyRevenueAfter", projected.calculateDailyCapacity().getAmount().toPlainString())
572:                 .put("projectedDailyCostBefore", company.calculateDailyPayroll().add(company.calculateDailyOperatingCost()).getAmount().toPlainString())
573:                 .put("projectedDailyCostAfter", projected.calculateDailyPayroll().add(projected.calculateDailyOperatingCost()).getAmount().toPlainString())
574:                 .put("projectedDailyProfitBefore", company.calculateDailyCapacity().subtract(company.calculateDailyPayroll().add(company.calculateDailyOperatingCost())).getAmount().toPlainString())
```

Keparahan: Sedang. Status: Terbukti pada payload preview.

Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/business/CompanyManager.java:570-575; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/business/OperatingCompany.java:212-225.

Kode terkait:
```java
.put("projectedDailyRevenueAfter", projected.calculateDailyCapacity().getAmount().toPlainString())
```

Reproduksi: Jasa level1 CEOlevel1 jam8 STANDARD,02Nov2026 macro1; previewPOLICY tanpa perubahan.

Hasil/dampak: Payload revenue9000000 sementara calculateDailyRevenue(day)=3090000. Profit proyeksi juga memakai kapasitas.

Bukti: AdditionalProbe.java, additional-probe.log di folder dokumen ini.

Rekomendasi: Preview menghitung dengan fungsi settlement yang sama, hari dan macro jelas, kemudian mengurangi payroll/operating cost yang sama. Bila ingin menunjukkan kapasitas maksimum, label harus kapasitas; perubahan label/tampilan mengikuti persetujuanUI.

Contoh inti (sesuaikan API/konteks dan periksa seluruhcaller):
```java
Money estimate = projected.calculateDailyRevenue(day, macroMultiplier);
Money profit = estimate.subtract(projected.calculateDailyPayroll()).subtract(projected.calculateDailyOperatingCost());
```

Verifikasi perbaikan: Sektor/level/benefit/morale/krisis/macro; bandingkan preview dengan settlement fixture deterministik.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
previewRevenue = rumusRevenueOperasionalYangSama(company, macro, staff, hours, morale);
```

Catatan bukti terperinci: [FINDINGS-AU09-AU16.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU09-AU16.md>).

<a id="au13"></a>

### AU13 — IPO tahunan dengan ticker sama menambah posisi tanpa menambah saham beredar

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/corporate/CorporateActionService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/corporate/CorporateActionService.java:937>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
937:                 String exchange = ipo.getCurrency() == Currency.USD ? "US" : "IDX";
938:                 Position p = acc.getPosition(exchange + ":" + ipo.getTicker());
939:                 if (p == null) {
940:                     p = new Position(ipo.getTicker(), exchange, ipo.getCurrency(), allottedShares, ipo.getOfferingPrice().getAmount(), ipo.getOfferingPrice().getAmount());
941:                     acc.addPosition(p);
```

Keparahan: Sedang. Status: Terbukti.

Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/corporate/CorporateActionService.java:123-142,937-944; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:1893-1907.

Kode terkait:
```java
candidateIdx = Math.floorMod((year - 2026) * 36 + (month - 1) * 3 + (slot - 1), IPO_CATALOG_CANDIDATES.length);
if (catalog.getById(exchange + ":" + ipo.getTicker()) == null) { /* register supply */ }
```

Reproduksi: JadwalkanJan2026danJan2027, slot1keduanyaNCLD. Pesan1lot masing-masing dan jalankan allotment.

Hasil/dampak: Pemain100->200saham; sharesOutstanding tetap20000000. Tidak ada transfer stok treasury/dealer yang menjelaskan tambahan. Persentasekepemilikan dan kapitalisasi dapat salah. Tidak mengklaim contoh2lot sudah melebihi100%.

Bukti: AdditionalProbe.java, additional-probe.log di folder dokumen ini.

Rekomendasi: Pemilik menentukan emiten baru/tickerunik atau penerbitan lanjutan. Untuk emiten sama gunakan event supply persisten dan idempoten; jangan mengabaikan supply karena katalog sudah memiliki ticker. Jangan migrasi ticker save lama sepihak.

Contoh inti (sesuaikan API/konteks dan periksa seluruhcaller):
```java
// Untuk penerbitan lanjutan yang disahkan:
instrument.applyShareSupplyEvent(eventId, BigDecimal.ONE, newlyIssuedShares, 0);
```

Verifikasi perbaikan: Lintas tahun, retry allotment, reload dan konsistensi issued/treasury/outstanding/posisi; jumlah yang diterbitkan perlu sesuai kebijakan penawaran.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
IPO ticker sudah ada -> penerbitan baru wajib menambah issuedShares dan outstanding secara atomik
```

Catatan bukti terperinci: [FINDINGS-AU09-AU16.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU09-AU16.md>).

<a id="au15"></a>

### AU15 — Duel bos tidak mendapat kemajuan dari waktu dunia

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/boss/MarketBossService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/boss/MarketBossService.java:60>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
60:     public synchronized void processSessionUpdate(double playerSessionReturn, Account personalAccount, EconomyLedger ledger, long now) {
61:         for (MarketBoss b : bosses.values()) {
62:             if (b.isActive()) {
63:                 boolean won = b.updateProgress(playerSessionReturn);
64:                 if (won && personalAccount != null && ledger != null) {
```

Keparahan: Sedang. Status: Terbukti; soft-lock fitur duel, bukan seluruhgame.

Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/boss/MarketBossService.java:60-78; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2337-2355,3742-3745; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:8142-8155.

Kode terkait:
```java
public synchronized void processSessionUpdate(...) { ... }
// pencarian semua source tidak menemukan caller produksi
```

Reproduksi: MulaiBOSS-01 lalu advanceRealMs(5*1800000,false), lima hari game diproses.

Hasil/dampak: active=true,elapsedSessions=0. Satu-satunya caller processSessionUpdate ditemukan di testBossAndTaxAuditTahapE2Test.java:84.

Bukti: BossProbe.java, boss-probe.log di folder dokumen ini.

Rekomendasi: Hubungkan kemajuan ke settlement sesi/hari sesuai aturan produk, satu kali perperiode, memakai return investasi terukur dari performance/report existing, mengecualikan setoran/pinjaman/reward.

Contoh inti (sesuaikan API/konteks dan periksa seluruhcaller):
```java
// Pada settlement periode yang disahkan dan belum diproses:
marketBossService.processSessionUpdate(investmentReturnPercent, personal, ledger, timestamp);
```

Verifikasi perbaikan: Melalui facade advanceWorld: menang/kalah/retry/offline dan midnightretry, bukan memanggil service langsung saja.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
pada settlement sesi yang sukses: advanceSession sekali per bossId+sessionId
```

Catatan bukti terperinci: [FINDINGS-AU09-AU16.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU09-AU16.md>).

<a id="au16"></a>

### AU16 — State duel bos hilang setelah load dan tertinggal setelah newgame

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/boss/MarketBossService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/boss/MarketBossService.java:81>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
81:     public synchronized JSONArray exportBosses() throws JSONException {
82:         JSONArray arr = new JSONArray();
83:         for (MarketBoss b : bosses.values()) {
84:             arr.put(b.toJsonObject());
85:         }
```

Keparahan: Sedang. Status: Terbukti.

Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/boss/MarketBossService.java:32-34,81-87; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:178,3742-3745; runtime save/load/reset tidak memuatbos.

Kode terkait:
```java
private final MarketBossService marketBossService = new MarketBossService();
// state service tidak disertakan dalam pipeline save/load/reset
```

Reproduksi: Mulai duel;save();facadebaru.load() sukses. Pada facade semula jalankan initializeNewGame().

Hasil/dampak: Setelah load activefalse; setelah newgame diinstance lama justru activetrue.

Bukti: BossProbe.java, boss-probe.log di folder dokumen ini.

Rekomendasi: Ekspor/restore state bos tervalidasi dan panggil reset pada newgame/reset. Simpan active/defeated/elapsed/return serta receipt reward, bukan stringdisplay. Save lama defaultbelumduel; jangan menghapus state lain.

Contoh inti (sesuaikan API/konteks dan periksa seluruhcaller):
```java
marketBossService.reset(); // pada reset domain
// sertakan state bos dalam snapshot dan validasi/restore seperti service lain
```

Verifikasi perbaikan: Roundtripaktif/menang/kalah; newrun; rewardtakganda setelahretry/reload.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
save.bossState = marketBossService.exportBosses();
load/reset wajib memulihkan/mengosongkan state yang sama
```

Catatan bukti terperinci: [FINDINGS-AU09-AU16.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU09-AU16.md>).

<a id="au17"></a>

### AU17 — Dua achievement tidak memiliki jalur progres produksi

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/player/PlayerProgressManager.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/player/PlayerProgressManager.java:396>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
396:     public synchronized boolean onLeaguePromoted(long timestamp) {
397:         Achievement a = achievements.get("league_promoted");
398:         if (a != null && !a.isUnlocked()) {
399:             a.unlock(timestamp);
400:             currentXp += a.getXpReward();
```

Keparahan: Sedang. Status: Terbukti dari source; arti partisipasi aktif liga perlu keputusan pemilik untuk remediasi.

Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/player/PlayerProgressManager.java:153-157,178-182,273-287,396-403.

```java
achievements.put("league_competitor", ...);
achievements.put("research_elite", ...);
// onResearchRead hanya mengubah reader, onLeaguePromoted hanya league_promoted
```

Reproduksi/bukti: ProgressProbe.java dan progress-probe.log:10artikelunik=>readertrue,research_elite0/10. Delapanhari dan weekrollover=>league_competitor0. Pencarian seluruhsource tidak menemukan pengubah duaachievement ini selain pemulihan save.

Dampak: Badge dan XP yang ditawarkan tidak dapat diperoleh melalui permainan normal. Delapanhari pasif sendiri bukan bukti memenuhi syarat partisipasi aktif; ketidakadaan semua caller progres menjadi bukti utama badge liga tidak tersambung.

Rekomendasi: Perbarui research_elite di event onResearchRead yang sama dan hitung artikel unik, satu kali XP. Tetapkan bersama pemilik pemicu partisipasi aktif liga, lalu kaitkan event liga existing; jangan memilih kriteria ekonomi/progression sendiri.

Contoh inti:
```java
Achievement elite = achievements.get("research_elite");
elite.setProgress(readArticleIds.size());
// bila mencapai target dan elite.unlock(now) berhasil, tambahkan XP tepat satu kali
```

Verifikasi: 10artikelunik+duplikat,reload,reader danelite keduanya; ulangperiode liga,partisipasi valid/tidakvalid,idempoten.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
event READ_RESEARCH / LEAGUE_PARTICIPATION -> update achievement terkait secara idempotent
```

Catatan bukti terperinci: [FINDINGS-AU17-AU20.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU17-AU20.md>).

<a id="au18"></a>

### AU18 — Arsip liga mengambil peserta terakhir sebagai pengganti baris pemain

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/league/LeagueManager.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/league/LeagueManager.java:453>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
453:             // Rampingkan arsip riwayat liga ke top 20 + player agar tidak membengkak 16MB pada 1.000 NPC
454:             JSONArray fullBoard = archived.getJSONArray("leaderboard");
455:             JSONArray trimmedBoard = new JSONArray();
456:             boolean hasPlayer = false;
457:             for (int i = 0; i < Math.min(20, fullBoard.length()); i++) {
```

Keparahan: Sedang. Status: Terbukti pada service dengan fixture ranking valid.

Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/league/LeagueManager.java:453-465.

```java
if (!hasPlayer && fullBoard.length() > 0) {
    trimmedBoard.put(fullBoard.getJSONObject(fullBoard.length() - 1));
}
```

Reproduksi/bukti: ProgressProbe.java/progress-probe.log:30NPCreturn2%,sisanya0%,pemain1%=>ranking31. Setelah rollover,arsip21baris tidakmemuatpemain; baristambahanNPC-1000.

Dampak: Riwayat periode menyimpan peserta yang salah bagi pemain di posisi21sampaiposisi kedua terakhir; source tidak menjamin pemain selalu terakhir.

Rekomendasi: Cari objek isPlayer dari fullBoard ketika memang belum masuktop20, lalu tambahkan objek itu; pertahankan nomor ranking asli.

Contoh inti:
```java
for (int i = 20; !hasPlayer && i < fullBoard.length(); i++) {
    JSONObject row = fullBoard.getJSONObject(i);
    if (row.optBoolean("isPlayer")) { trimmedBoard.put(row); break; }
}
```

Verifikasi: Pemainposisi1,20,21,31,1001; returnbelumtersedia; save/loadarsip.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
cari baris isPlayer; jangan memakai rows[rows.length - 1] sebagai pengganti pemain
```

Catatan bukti terperinci: [FINDINGS-AU17-AU20.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU17-AU20.md>).

<a id="au19"></a>

### AU19 — Avatar dan bingkai terpasang tidak masuk save profil

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/player/PlayerProfileManager.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/player/PlayerProfileManager.java:252>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
252:     public synchronized JSONObject exportState() throws JSONException {
253:         JSONObject obj = new JSONObject();
254:         obj.put("playerName", playerName);
255:         obj.put("avatarMonogram", avatarMonogram);
256:         obj.put("totalTradesExecuted", totalTradesExecuted);
```

Keparahan: Sedang. Status: Terbukti melalui facade save/load.

Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/player/PlayerProfileManager.java:252-277,280-349; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:4469-4478.

```java
// toJsonObject mengeluarkan equippedAvatar/frame,
// exportState dan restoreState tidak menyimpan/memulihkan keduanya
```

Reproduksi/bukti: ProgressProbe.java/progress-probe.log: equipAVATAR-002+frame_gold_tycoon.png; save; loadberhasil=>avatar_trader_muda.png+frame_steel.png.

Dampak: Pemain menerima sukses pemasangan dan penyimpanan, tetapi preferensi kosmetik hilang ketika aplikasi dibuka kembali. Ini tidak membuktikan hilangnya saham/uang.

Rekomendasi: Simpan keduaID dalam exportState dan pulihkan setelah validasi terhadap katalog kosmetik canonical. Gunakan default hanya untuk save lama tanpafield. Keputusan reset/prestige perlu konsisten dengan aturan identitas yang disahkan.

Contoh inti:
```java
obj.put("equippedAvatar", equippedAvatar).put("equippedFrame", equippedFrame);
// restore ID yang tervalidasi; jangan menerima path/file sebarang
```

Verifikasi: Avatar/frameyangsah,IDinvalid,legacytanpafield,roundtrip,reset/prestige.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
export/import profil menyertakan equippedAvatar dan equippedFrame dengan default versi lama
```

Catatan bukti terperinci: [FINDINGS-AU17-AU20.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU17-AU20.md>).

<a id="au20"></a>

### AU20 — Picker dan foto profil mengabaikan equippedAvatar dari domain

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:8520>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
8520:         let curEquipped = 'AVATAR-001';
8521:         try {
8522:             if (bridge.getPlayerProfile) {
8523:                 const prof = JSON.parse(bridge.getPlayerProfile());
8524:                 if (prof.avatarId) curEquipped = 'AVATAR-' + String(prof.avatarId).padStart(3, '0');
```

Keparahan: Sedang. Status: Terbukti dari fungsi renderer produksi di browser QA dan payload domain aktual; bukan full-app/perangkat.

Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:8520-8525,8556-8564,8597-8600; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:4526; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/player/PlayerProfileManager.java:220-221.

```java
let curEquipped = "AVATAR-001";
if (prof.avatarId) curEquipped = ...;
// profile memakai p.avatarPath || avatar_001.webp
// domain menyediakan equippedAvatar, bukan avatarId/avatarPath
```

Reproduksi/bukti: ProgressProbe: getPlayerProfileJson setelah equipAVATAR-002 memuat equippedAvatar=AVATAR-002, avatarIdPresentfalse,avatarPathPresentfalse. Tidak ada pembacaan equippedAvatar dalam C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js; picker dibuka ulang langsung setelah pemasangan.

Dampak: Sebelum reload pun pilihan yang ditandai kembali keAVATAR-001 dan foto profil memakaifallback001; toast sukses dapat menyesatkan. Perbaikan persistensi AU19 saja tidak memperbaiki kontrakUI ini.

Rekomendasi: Gunakan ID canonical yang sama pada kedua pihak; map equippedAvatar tervalidasi ke asetavatar existing dan status pilihan. Cocokkan format IDlegacy filename dengan AVATAR-xxx melalui migrasi eksplisit yang menjaga preferensi. Periksa response success sebelumtoast.

Contoh inti:
```java
const currentId = profile.equippedAvatar;
const selected = avatars.find(avatar => avatar.id === currentId);
// pakai selected.file untuk foto profil dan selected.id untuk badge
```

Verifikasi: Pilih002/009,close/reopenpicker,profil,save/load,invalidresponse; preview/persetujuanUIsebelumperbaikan produksi.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
const equipped = profile.equippedAvatar; // gunakan kontrak domain, bukan avatarId rekaan
```

Catatan bukti terperinci: [FINDINGS-AU17-AU20.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU17-AU20.md>).

<a id="au21"></a>

### AU21 — Nama pemain dirender sebagai HTML dan dapat menjalankan JavaScript

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:8597>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
8597:             avRow.innerHTML = `
8598:                 <div style="display:flex; align-items:center; gap:12px;">
8599:                     <img src="${p.avatarPath || 'img/avatars/avatar_001.webp'}" onerror="this.src='img/avatars/avatar_001.webp'" style="width:54px; height:54px; border-radius:50%; object-fit:cover; border:2px solid var(--gold); background:var(--surface-raised);">
8600:                     <div>
8601:                         <strong style="font-size:1.05rem; color:#FFF; display:block;">${p.playerName || 'Trader Nusantara'}</strong>
```

Keparahan: Sedang. Status: Terbukti dengan penanda lokal yang tidak berbahaya; jalur serangan eksternal belum terbukti.

Root: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM. Snapshot sesuai coverage.json.

Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/player/PlayerProfileManager.java:116-129; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:8597-8604.

```javascript
avRow.innerHTML = `...<strong>${p.playerName || "Trader Nusantara"}</strong>...`;
```

Reproduksi: Set nama melalui validator domain menjadi <img src=x onerror=window.qa=1> (panjang31 dan tanpa karakterkontrol). Ambil getPlayerProfileJson aktual, render dengan fungsi openPlayerProfileModal dari source produksi dalam browserQA. window.qa berubah1 saat gambar gagal dimuat.

Bukti: ProgressProbe.java, tmp/profile-name.json, ui-profile-probe.py, ui-profile-probe.log di folder ini.

Dampak dan batas: Nama yang seharusnya teks diperlakukan sebagai markup dan event handler. Input tersimpan dapat merusak tampilan atau menjalankan aksi JavaScript di konteks halaman. Belum membuktikan penyerang jarak jauh, eksfiltrasi, atau akses OS; pengujian tidak memakai bridge asli.

Rekomendasi: Bangun nama dan teks dinamis dengan node()/textContent existing; jangan menjadikan pembatasan48karakter sebagai escaping. Telusuri semua sink innerHTML untuk data pemain/perusahaan/pesan. Pertahankan markup statis seperlunya.

```javascript
const name = node("strong", "", p.playerName || "Trader Nusantara");
// append node ke wadah; jangan interpolasi nama ke innerHTML
```

Uji perbaikan: Nama literal dengan<>&quotes/emoji; reload; pastikan tidak ada elemen HTML/event dari input dan nama biasa tetap benar.

Bukti tambahan AU20: fungsi picker produksi yang dijalankan pada browser QA menandai Trader Muda/AVATAR-001 sebelum dan sesudah memilih Analis Riset, sementara domain mengembalikan AVATAR-002 dan toast menyatakan sukses. Foto profil tetap img/avatars/avatar_001.webp. Log ui-profile-probe.log. Ini menguatkan status AU20 menjadi TERBUKTI pada fungsi renderer produksi dengan payload domain aktual; bukan pengujian seluruh aplikasi atau perangkat.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
nameElement.textContent = p.playerName;
```

Catatan bukti terperinci: [FINDING-AU21.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDING-AU21.md>).

<a id="au22"></a>

### AU22 — Pengaturan offline menjanjikan pause padahal hanya mengatur laporan

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:8770>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
8770:                 const offlineProgressLabel = node('label', 'check-row');
8771:                 const offlineProgressCheck = node('input'); offlineProgressCheck.type = 'checkbox';
8772:                 offlineProgressCheck.checked = Boolean(p.preferences ? p.preferences.offlineProgressEnabled !== false : true);
8773:                 offlineProgressLabel.append(offlineProgressCheck, node('span', '', 'Waktu pasar tetap berjalan saat offline (48× Otomatis)'));
8774:                 const offlineNotice = node('p', 'notice', 'Jika dimatikan, seluruh simulasi bursa dan operasional anak usaha membeku (pause) saat game ditutup.');
```

Keparahan:Sedang. Status:Terbukti.

Lokasi:C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:8770-8774,8783-8790; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:316-340.

```java
offlineNotice = node("p", "notice", "Jika dimatikan, seluruh simulasi bursa dan operasional anak usaha membeku (pause) saat game ditutup.");
// domain tetap menjalankan clock.advanceRealMs(elapsedRealMs, false)
```

Reproduksi:Set offlineProgressEnabled=false lalu reconcileOfflineElapsed(1800000) pada fixtureQA.

Hasil/dampak:Dunia tetap maju86400detik=1harigame; hasReport=false. Ini sesuai kebijakan48x, tetapi bertentangan dengan janji kontrol dan teksUI.

Bukti:BridgeAssetProbe.java, bridge-asset-probe.log.

Rekomendasi:Pertahankan48x dan jangan mengimplementasikanpause. Hilangkan atau beri nama kontrol sesuai fungsi sebenarnya (laporan saat kembali). Pertahankan kompatibilitas save untuk boolean lama bila nama internal berubah. Preview/persetujuanUI wajib sebelum mengubah teks/tampilan.

```java
// Pilihan yang disahkan harus menjelaskan laporan, bukan kecepatan dunia:
// enabled => tampilkan laporan; disabled => laporan hening; dunia tetap48x.
```

Verifikasi:True/false; offlinependek/panjang; waktu/settlement setara; hanya laporan berbeda; teks tidak menjanjikanpause.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
ubah kontrol/teks menjadi pengaturan laporan offline; laju dunia tetap 48x
```

Catatan bukti terperinci: [FINDINGS-AU22-AU23.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU22-AU23.md>).

<a id="au24"></a>

### AU24 — Cache generator berita umum tidak dibersihkan saat mulai run baru

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java:74>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
74:     public synchronized void resetForGame(long baseTime) {
75:         contentEpoch = baseTime;
76:         observedGameAt = -1; observedRealAt = -1; replayClock = false;
77:         newsList.clear();
78:         analystReports.clear();
```

Keparahan:Sedang. Status:Terbukti.
Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java:74–84,295–306; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:163,818.

Kode terkait:
```java
private final Set<String> proceduralNewsDates = new HashSet<>();
if (proceduralNewsDates.contains(dateKey)) return;
proceduralNewsDates.add(dateKey);
// resetForGame membersihkan newsList dan scheduledDates, tetapi bukan proceduralNewsDates
```

Reproduksi: jalankan90hari QA sehingga NEWS-AUTO90; initializeNewGame pada facade yangsama, lalu majukan satuhari. Source menghasilkan Pro2 tetapi NEWS-AUTO0. Tanggal yang sudah dipakai runlama tetap menghalangi runbaru.

Perbaikan inti: proceduralNewsDates.clear() pada resetForGame, setelah menelusuri callerreset/load. Karena set tidak ada di exportState, tentukan cara membangun ulang deduplikasi dari IDartikel tersimpan agar reload/retry tetap idempoten. Hindari setkedua yang mengulang state artikel bila pengecekanIDexisting cukup.

Uji: newgameulang/eraBaru/save-load; hariyangsama tidakdobel; tanggal pada runbaru tidakterhalangriwayatrunlama.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
proceduralNewsDates.clear();
```

Catatan bukti terperinci: [AUDIT-KEBERULANGAN-KEJADIAN.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/AUDIT-KEBERULANGAN-KEJADIAN.md>).

<a id="au25"></a>

### AU25 — Evaluasi kuartal membaca return yang sudah direset

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2558>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
2558:             if (step == midnight) {
2559:                 clock.advanceGameSeconds(step - 1);
2560:                 settleDay(false);
2561:                 clock.advanceGameSeconds(1);
2562:                 processCalendarActions(clock.getCurrentDate());
```

Keparahan:Sedang. Status:Terbukti pada boundarykuartal di facadeJava.
Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2356–2363,2558–2562,2388–2390,255–265.

```java
// settleDay(false): inst.startNewTradingDay();
// lalu processCalendarActions(newDate): evaluateQuarterlyMacroShift();
double chg = bbca.getDailyChangePercent();
```

Reproduksi: fixture clock31Des2026, BBCA+2% dibandingpreviousclose, makro5. Majukan satuharimelewati1Jan2027. Sebelumboundary dailyChange2%; setelahboundary0%; makrotetap5 padahal delta+2 pada sinyal sebelumreset memenuhi kondisi stepQuarterlyMacro untukkenaikan.

Perbaikan: capture sinyal kuartal dari snapshot penutupan sebelum startNewTradingDay, atau jalankan evaluasi pada urutan boundary yang mempertahankan harga acuan tersebut. Gunakan periodekuartal yangdipersistenkan untuk idempotensi retry/load; jangan menambahkan randomshift untukmenutupi hilangnyasinyal.

Uji:+2%,−2%,diambang/tanpasinyal; aktif/offline; retrykuartalsama; reload; level1dan9tetapdibatasi.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
rekam harga acuan/return kuartal sebelum startNewTradingDay; evaluasi memakai snapshot itu
```

Catatan bukti terperinci: [AUDIT-KEBERULANGAN-KEJADIAN.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/AUDIT-KEBERULANGAN-KEJADIAN.md>).

<a id="au26"></a>

### AU26 — Warta Whale terbit berdasarkan tanggal tanpa penghubung kejadian ekonomi

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java:682>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
682:         // 2. Warta Eksklusif Whale (1-2 per 7 hari game, kenaikan > 20%)
683:         int dayIn14 = Math.floorMod(date.toEpochDay(), 14);
684:         if (dayIn14 == 2) {
685:             Instrument whaleInst = idxUniverse.get((dayOfYear + 1) % idxUniverse.size());
686:             double whalePct = 0.25; // +25% (> 20%)
```

Keparahan:Sedang. Status:Terbukti dari scheduler source dan hasil90hari; ketidaksesuaian aturan yang menyatakan beritaWhale mengikuti kejadian.
Lokasi: ResearchService.java:682–698,700–735. Aturan rootAGENTS.md bagianpaketriset: beritaWhale mengikuti kejadian, jangan mengarang kejadian untukmengisikuota.

```java
if (dayIn14 == 2) {
    Instrument whaleInst = idxUniverse.get((dayOfYear + 1) % idxUniverse.size());
    String whaleHeadline = "Katalis Mega Taipan: Akuisisi Strategis Konglomerat pada " + whaleInst.getTicker();
    // artikel dan target25% dibuat tanpa mengecek transaksi/kejadian akuisisi
}
```

Follow-up juga memakai tanggaldayIn14==9 dan narasi finalisasimerger; ia menautkan beritaawal, bukan bukti penyelesaian merger dari enginecorporate. Run90hari menambah13wartaWhale/follow-up sedangkan daftarcorporate action/RUPS/tender tetap seedawal.

Rekomendasi: consumerberitaWhale membaca kejadianekonomi existing yangsudahtercatat dan mempunyai ID/timestamp/provenance; deduplikasiIDkejadian/jenisberita; hanya terbitkanwarta yangmemenuhisyarat. Tidak perlu mengganti semuanya dengan eventframeworkbaru. Saat kejadianlangka belumada, jangan membuat akuisisi/merger fiktif semata mengisijadwal. Pemicu, frekuensi dan syarat perjenis tetap keputusanproduk pemilik.

Uji: tidakadakejadian=>tidakadaheadlineakuisisi; kejadianvalid=>satuwarta; follow-upasalsah; retry/reload; emiten/tanggal cocok.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
berita Whale merujuk economicEventId yang benar-benar tercatat; proses event sekali
```

Catatan bukti terperinci: [AUDIT-KEBERULANGAN-KEJADIAN.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/AUDIT-KEBERULANGAN-KEJADIAN.md>).

<a id="au29"></a>

### AU29 — Order generator NPC tidak mendapat kedaluwarsa harian

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:1535>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
1535:     public synchronized int expireDayOrders() {
1536:         int count = 0;
1537:         for (Order order : orderManager.getAllOrders()) {
1538:             if (!order.getStatus().isFinal()) {
1539:                 Account account = accounts.get(order.getAccountId());
```

- Keparahan: Sedang. Status: Terbukti pada alur generator dan pergantian hari produksi.
- Lokasi akar: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:1535–1551 (`orderManager.getAllOrders()` saja). Caller generator: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/npc/MarketEngine.java:304–306 dan TradingFacade.java:1677–1679, keduanya langsung submit ke matching/market tanpa registerOrder. Caller penutupan: TradingFacade.java:2298–2299.
- Reproduksi: game QA satu instrumen, 09:00, panggil `generateNpcLiquidity()`. Catat `marketEngine.getRestingOrders()` lalu majukan 1.800.000 ms nyata melalui `advanceRealMs(...,false)` (satu hari game48x).
- Hasil: initialResting4, initialTracked0, stillActive4, sampleStatusOPEN, clock tidak pause. Jadi generator berhasil menyediakan order, tetapi penutupan hari tidak menemukannya untuk expired.
- Dampak terbukti: quote NPC dan reservasi yang semestinya harian bertahan melewati settlement; perlakuan order manual/terdaftar dan order generator tidak konsisten. Pertumbuhan memori tak terbatas/freeze belum dibuktikan oleh probe ini dan tidak diklaim.
- Saran minimal: gunakan union orderManager orders dengan marketEngine.getRestingOrders saat expiry, deduplikasi berdasarkan ID, lalu panggil expireOrder untuk order non-pemain saja. Alternatif registrasi bersama harus menghindari menimbun semua riwayat NPC final selamanya. Gunakan helper existing, bukan pengelola antrean baru.
- Contoh inti: `Map<String,Order> daily = new LinkedHashMap<>();` isi dari dua daftar berdasarkan ID; iterasi `daily.values()`, pertahankan skip PERSONAL/CLIENT GTC. Pertahankan cleanup order final pada buku yang relevan.
- Check minimum: quote dari generateNpcLiquidity dan simulateNextTickPrice kedaluwarsa dan reserved cash kembali; order pemain GTC tidak ikut hilang; save/reload tidak mengubah aturan expiry.
- Bukti runnable: OrderAuditProbe.java; order-audit-probe.log.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
gabungkan tracked orders dan resting orders berdasarkan orderId sebelum expiry NPC harian
```

Catatan bukti terperinci: [FINDINGS-AU28-AU29.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU28-AU29.md>).

<a id="au30"></a>

### AU30 — Kebijakan katalog reksa dana tidak diterapkan pada backing dan parameter transaksi

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/funds/MutualFundManager.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/funds/MutualFundManager.java:40>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
40:                 for (int i = 0; i < arr.length(); i++) {
41:                     JSONObject obj = arr.getJSONObject(i);
42:                     String id = obj.getString("id");
43:                     String name = obj.getString("name");
44:                     MutualFund.FundType type = MutualFund.FundType.fromPolicy(obj.getString("enginePolicy"));
```

Keparahan: Sedang. Status: Terbukti lewat source dan domain produksi dengan save QA baru.

Lokasi:
- C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/funds/MutualFundManager.java:40–46 hanya membaca id/name/enginePolicy/manager.
- C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/funds/MutualFund.java:94–104,133–138,230–283: hanya EQUITY_INDEX mendapat saham; EQUITY_ACTIVE dan MIXED menggunakan kontrak utang, coupon8%, tanpa rebalance saham; perubahan faktor harga hanya FIXED_INCOME.
- C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/funds/MutualFund.java:323–324,468: minimum seragamRp10.000, biaya pengelolaan0,75%/tahun.
- C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/data/katalog_reksadana_24.json:173–187 FUND-SH-01 menetapkan saham90%, obligasi0%, kas10%, managementFeeBps150, minBuyIdr100000. FUND-MX-01:122–136 menetapkan saham45%, obligasi40%, kas15%.

Bukti kode: `if (type != FundType.EQUITY_INDEX)` membuat issuer; `if (type != FundType.EQUITY_INDEX || poolAccount == null) return null;` menolak rebalance seluruh jenis lain. `new MutualFund(id,name,type,manager)` tidak menerima stockPct/bondPct/cashPct atau minimum/biaya dari katalog.

Reproduksi: game QA memakai market_universe dan katalog24 produksi; subscriptionRp1juta pada FUND-SH-01 dan FUND-MX-01; majukan jam8:30 ke cutoff16:00 melalui48x. Probe: SH-01 debtPrincipalRp807.192, MX-01Rp799.200; keduanya poolStockPositions0, backingModelCANONICAL_BOND_CONTRACT. Naikkan BBCA20% pada waktu yang sama: NAB keduanya tetap10.000. SubscriptionSH-01Rp10.000 juga SETTLED meskipun katalog minimumRp100.000; API biaya keduanya0,75%.

Dampak: pilihan produk saham/campuran tidak memberi eksposur/risiko yang dicatat dalam katalog; perbedaan produk dan biaya/minimum kehilangan arti. Ini bukan crash atau bukti glitch uang tak terbatas.

Saran: gunakan parameter katalog yang sudah disetujui sebagai sumber tunggal; bawa minimum, biaya, dan bobot ke MutualFund dan rutekan alokasi saham lewat jalur rebalance canonical yang sudah ada. Contoh validasi minimum: `grossCash.compareTo(minimumSubscription) < 0` dengan minimum dari katalog, bukan angka global. Hilangkan asumsi `type != EQUITY_INDEX => seluruh backing utang`. Komposisi saham lintas emiten dan metode seleksi untuk fund aktif masih memerlukan keputusan pemilik jika belum ditentukan; jangan menyamakan semua dengan satu saham atau memilih strategi baru sendiri. Jangan sekadar mengganti label produk untuk menutupi perilaku.

Check minimum: produk saham memiliki aset saham canonical sesuai kebijakan/toleransi dan bereaksi pada harganya; fund campuran menilai saham+utang+kas; minimum/biaya mengikuti katalog; save/load menjaga backing dan NAB; subscription/redemption tetap idempoten.

Bukti: FundPolicyProbe.java; fund-policy-probe.log. Pengujian merekayasa kenaikan20% pada instrumen QA untuk mengisolasi respons valuasi, bukan klaim kejadian pasar aktual.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
alokasi actual fund mengikuti bobot/minimum/fee katalog yang tervalidasi; bukan uniform default
```

Catatan bukti terperinci: [FINDING-AU30.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDING-AU30.md>).

<a id="au31"></a>

### AU31 — Jalur order replay memakai low/high harian sebelum penutupan

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:1279>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
1279:         if (gameMode == GameMode.HISTORICAL_REPLAY) {
1280:             if (!replayEngine.supportsInstrument(inst.getId())) throw new IllegalArgumentException("Instrumen tidak termasuk dataset replay aktif");
1281:             var replayBar = replayEngine.getCurrentBar();
1282:             if (replayBar != null && replayEngine.canFillOrderOnHistoricalBar(order, replayBar)) {
1283:                 Account dealer = accounts.get("ACC-NPC-MM-01");
```

- Keparahan: Sedang, khusus replay lama/retained path. Status: Terbukti pada domain produksi.
- Lokasi akar: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:1279–1291. Helper yang dipakai: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/replay/HistoricalReplayEngine.java:259–270. Helper yang benar tetapi belum dipakai: executablePrice116–125.
- Kode: `canFillOrderOnHistoricalBar(order,replayBar)` memeriksa low/high lalu memasang counterorder pada `order.getLimitPrice()`, bukan harga pembukaan yang sudah teramati.
- Repro: replay QA datasetbundledAAPL; tanggal2016-05-09 jam09:00, openUSD93, lowhariUSD92,59. Kirim limitBUY1@92,59. Hasil FILLED1@92,59, sementara executablePrice pada jam yang sama mengembalikan null (opening93 di atas batas beli). Low harian belum diketahui sebelum close.
- Dampak: keuntungan/hasil strategi replay bisa bergantung pada data masa depan; helper visible-bars yang aman tidak melindungi jalur eksekusi ini.
- Saran minimal: gunakan `BigDecimal executable = replayEngine.executablePrice(order,clock.getCurrentDate(),clock.getCurrentTime());`; buat counterorder hanya bila executable!=null dan gunakan executable sebagai harga. Validasi ukuran lot, fee, ketersediaan dealer dan AON tetap lewat matching bersama. Jangan memaksa semua order terisi atau mengaktifkan kembali menu replay.
- Check: limit di bawah open tidak terisi di09:00 meski lowhari menembusnya; harga eksekusi berdasarkan data teramati; save lama tetap terpisah dari pasar utama.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
gunakan executablePrice pada timestamp kini; jangan fill dari low/high yang belum teramati
```

Catatan bukti terperinci: [FINDINGS-AU31-AU32-REPLAY.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU31-AU32-REPLAY.md>).

<a id="au32"></a>

### AU32 — Modal USD dikredit dua kali saat metode domain memulai replay

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:931>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
931:             if (inst.getCurrency() == Currency.USD && personalAcc != null && personalAcc.getTotalCash(Currency.USD).isZero()) {
932:                 BigDecimal usdRate = fxService != null ? fxService.getMidRate() : BigDecimal.valueOf(15800);
933:                 BigDecimal usdAmount = (initialCapital != null ? initialCapital : BigDecimal.valueOf(50_000_000))
934:                         .divide(usdRate, 2, java.math.RoundingMode.HALF_UP);
935:                 Money usdMoney = new Money(usdAmount, Currency.USD);
```

- Keparahan: Sedang; jalur pembuatan replay baru sudah diblokir pada UI/bridge produksi. Status: Terbukti pada metode domain, keterjangkauan melalui UI saat ini tidak terbukti dan tidak diklaim.
- Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:931–938. EconomyLedger.java:51–55 membuktikan recordMutation turut mengubah kas.
- Kode: `personalAcc.creditCash(usdMoney);` diikuti `ledger.recordMutation(...usdMoney,true,...)`.
- Repro: startHistoricalReplay('US:AAPL',50000000) pada facade QA, kurs15800. Seharusnya satu kreditUSD3164,56 sesuai rumus method, tetapi kasUSD6329,12 dan reconcileAllfalse. KasIDR50juta juga tetap ada; apakah modal IDR perlu dikonversi atau hanya pemberian USD tambahan memerlukan keputusan produk, tidak diputuskan audit.
- Saran minimum bug pembukuan: hapus kredit fisik langsung dan biarkan ledger menjadi pemilik mutasi sekali. Jangan menghapus data replay lama atau menerapkan koreksi saldo pemain tanpa kebijakan migrasi disetujui. Bila replay tidak akan dibuka kembali, prioritaskan proteksi jalur save lama dan tandai metode pembuatan sebagai nonaktif; tidak perlu membangun fitur replay baru.
- Check: satu ledger kredit menghasilkan satu penambahan kas dan reconcileAlltrue; pengulangan/resume tidak menambah modal.

Bukti runnable kedua kasus: ReplayAuditProbe.java; replay-audit-probe.log. Dataset61barbundleddenganchecksum diperiksa loader produksi; tidak ada akses save pemain.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
kredit modal USD cukup melalui ledger.recordMutation; hapus kredit fisik duplikat
```

Catatan bukti terperinci: [FINDINGS-AU31-AU32-REPLAY.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU31-AU32-REPLAY.md>).

<a id="au33"></a>

### AU33 — Variasi sentimen generator terikat kategori sehingga cabang tidak pernah tercapai

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java:308>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
308:         int daySeed = Math.abs(Objects.hash(date.getYear(), date.getDayOfYear()));
309:         int typeMod = daySeed % 4; // 0: Emiten Laba, 1: Kebijakan Makro, 2: Sektor Industri, 3: Katalis Global
310: 
311:         String newsId = "NEWS-AUTO-" + date.toString() + "-" + (daySeed % 100);
312:         NewsArticle article = null;
```

Keparahan Sedang; Terbukti.
Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java:308–319 dan365–378.
Kode: `typeMod=daySeed%4`; untuk kategori COMPANY, typeMod==0 sehingga `daySeed%2==0` selalu true. Untuk kategori global, typeMod==3 sehingga kondisi yang sama selalu false.
Repro: panggil generator produksi untuk365hari2026 dengan katalog QA valid. Hasil COMPANY positif92/negatif0; Global positif0/negatif91. Ini bukan kesimpulan dari sampel kecil: hubungan modulo membuktikan cabang lawannya mustahil.
Dampak: kabar emiten tidak pernah menghasilkan variasi laba turun dari cabang yang sudah disediakan; kabar global selalu negatif. Menambah durasi bermain/update data tidak memperbaiki variasi tersebut.
Saran: pisahkan bit/hash kategori dan arah. Contoh minimum dengan seed yang sama: `boolean isBull = ((daySeed >>> 2) & 1) == 0;`, bukan memeriksa bit yang sudah ditentukan oleh kategori. Persentase/probabilitas produk jangan diubah sepihak. Check: rentang tanggal deterministik menghasilkan kedua arah pada setiap kategori yang mendukungnya dan reload tetap deterministik.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
pakai bagian hash yang berbeda untuk kategori dan arah; pertahankan peluang produk
```

Catatan bukti terperinci: [FINDINGS-AU33-AU36-RESEARCH.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU33-AU36-RESEARCH.md>).

<a id="au34"></a>

### AU34 — Booster hanya mengubah flag, tanpa manfaat transaksi, pemakaian, atau persistensi

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java:415>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
415:     public synchronized JSONObject activateBooster(String boosterId) throws JSONException {
416:         for (ResearchBooster b : boosters) {
417:             if (b.getBoosterId().equalsIgnoreCase(boosterId)) {
418:                 b.activate();
419:                 return b.toJsonObject();
```

Keparahan Sedang; Terbukti untuk fee booster dan save/load. Dua tipe lain tidak memiliki caller efek di source, tetapi besaran manfaatnya belum diprobe secara terpisah.
Lokasi: ResearchService.java:90–93,175–202,415–429; ResearchBooster.java:59–71; TradingFacade.java:1180–1190,3763–3766; MatchingEngine.java:426–427,609–610.
Kode aktivasi hanya `b.activate()`; pencarian seluruh produksi tidak menemukan caller `hasActiveBooster`, `consumeUse`, atau `getMultiplierValue`. applySectorFeeDiscount hanya memakai dominasi sektor dan tech tree. exportState tidak menyimpan booster.
Repro: gameQA09:00, aktifkanBOOST-02 yang menjanjikan bebas komisi15order, beli100saham@Rp10.000. Hasil FILLED, feeRp1.500 tetap dipungut, remainingUses15, isActivetrue. Save lalu load QA berhasil tetapi isActivefalse dan uses15 kembali default.
Dampak: pemain mendapat konfirmasi aktivasi dan indikator aktif tanpa manfaat; batas pemakaian tidak berjalan dan aktivasi hilang saat reload.
Saran: sambungkan booster pada titik perhitungan bersama yang sudah ada, konsumsi pada kejadian eksekusi yang disetujui, dan simpan state booster di runtime research. Jangan mengurangi penggunaan ketika order ditolak atau hanya mengantre. Keputusan yang perlu dipastikan sebelum implementasi: apakah bebas komisi mencakup seluruh fee jual atau hanya komponen broker (saat ini rate jual juga menyebut pajak game); apakah pemakaian dihitung perorder utuh atau tiapfill. Jangan melepas batas fee/aturan pajak secara sepihak. Minimal test aktifkan->eksekusi->usesberkurang sesuai aturan->save/loadtetapsama; boosterhabis menghentikan manfaat.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
aktivasi -> manfaat pada transaksi yang relevan -> consumeUse -> persist state yang sama
```

Catatan bukti terperinci: [FINDINGS-AU33-AU36-RESEARCH.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU33-AU36-RESEARCH.md>).

<a id="au35"></a>

### AU35 — Feed Basic membocorkan isi target berita yang belum terbit

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java:444>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
444:     public synchronized JSONArray getNewsFeed(long currentTimestamp, long realNow) throws JSONException {
445:         JSONArray arr = new JSONArray();
446:         for (NewsArticle a : newsList) {
447:             if (a.isAnnounced(currentTimestamp) || a.isPublished(currentTimestamp)) {
448:                 JSONObject obj = a.toJsonObject(currentTimestamp, Math.max(realNow, observedRealAt), replayClock);
```

Keparahan Sedang; Terbukti pada payload domain produksi yang dikembalikan ke NativeBridge.
Lokasi akar: ResearchService.java:444–453; NewsArticle.java:139–143. Caller: TradingFacade.java:1698–1702; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java:677–679.
Kode: `obj.put("targetInfo",target.toJsonObject())` tanpa pemeriksaan paket atau publikasi. Serializer overloaded juga menimpa headline yang sudah dimasking dengan affectedSector dan direction asli sebelum publish.
Repro1: gameBasicawal8:30, getNewsFeedJson mengembalikan NEWS-FUTURE-01 isPublishedfalse tetapi headline`[Jadwal Rilis] Energi: POSITIF`.
Repro2: aktifkan generator harian sepertihostproduksi lalu majukan world ke00:01. Basic getNewsFeedJson mengembalikan NEWS-PRO-2026-10-20-2 belum terbit (rilis00:30) dengan targetInfo: tickerQA, headlineakumulasiinstitusi, target3,7%, hargaacuan10000, target10375, tierPRO.
Dampak: batas informasi paket dan anti-future leak bisa dilewati pada feed umum; masking field relatedTicker/content tidak cukup karena targetInfo/headline menyimpan kembali informasi aslinya. Ini kebocoran informasi dalam game lokal, bukan akses jaringan atau data rahasia luar.
Saran: terapkan pemeriksaan entitlement dan waktu pada mapper feed bersama SEBELUM serialize rincian target; jadwal umum hanya metadata generik yang disetujui. Contoh: `if (mayReadTarget(target,currentTimestamp)) obj.put("targetInfo",target.toJsonObject());`, dengan mayReadTarget memakai hasAccess dan kebijakan rilis resmi/early-access yang sudah disetujui. Jangan hanya menyembunyikan DOM atau menghapus tombol. Hapus penimpaan headline asli pada cabang umum belum publish. Check Basic/Pro/Whale sebelum/ketika/sesudahrilis serta setelah masa berbayar habis.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
if (!published || !entitled) jangan serialisasi targetInfo dan headline berisi arah/target rahasia
```

Catatan bukti terperinci: [FINDINGS-AU33-AU36-RESEARCH.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU33-AU36-RESEARCH.md>).

<a id="au36"></a>

### AU36 — Perpanjangan terminal terlambat sampai settlement harian, menyebabkan jeda akses berbayar

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java:951>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
951:     public synchronized void processDailyAutoRenewal(Account personalAccount, EconomyLedger ledger, long currentTimestamp) {
952:         TerminalTier tierToRenew = enrolledTier;
953:         if (tierToRenew == null || tierToRenew == TerminalTier.BASIC) return;
954:         if (currentTimestamp >= subscriptionExpiresAt) {
955:             Money fee = tierToRenew.getMonthlyFeeUsd();
```

Keparahan Sedang; Terbukti melalui clock/facade produksi.
Lokasi: TradingFacade.java:2331–2334 hanya memanggil processDailyAutoRenewal saat settleDay; ResearchService.java:895–900 menurunkan tier tepat expiry,951–969 baru memperpanjang saat caller harian berjalan.
Repro: fixtureQA memberikanUSD1juta lewatledger, rankSENIOR_ANALYST, berlanggananPro08:30 biayagameUSD250ribu. Majukan30harigame48x. Tepat expiry08:30, saldoUSD750ribu dan autoRenewaltrue tetapi hasAccessPROfalse serta tierBASIC. Majukan sampai settlementberikutnya:USD500ribu, tierPRO, periodebaru dihitung dari23:59:59.
Dampak: pemain dengan saldo cukup kehilangan akses selama sisa hari (contoh15jam29menit59detikgame, sekitar19menit22detiknyata48x), padahal perpanjangan otomatis aktif. Hasil juga bergantung kapan fungsi akses dipanggil.
Saran: proses deadline langganan sebagai kejadian waktu ekonomi pada clock progression, bukan hanya penutupan hari; panggil helper perpanjangan existing sebelum entitlement dievaluasi pada deadline. Gunakan expiry lama sebagai anchor periode berikutnya; saat catch-up panjang, proses masing-masing periode jatuh tempo tepat sekali, hentikan jika saldo kurang, dan hormati cancelAutoRenewal. Jangan menambah pembayaran di getter UI. Check deadline-1ms/deadline/deadline+1ms, offline beberapa periode, saldo cukup/tidak, dan pembatalan dengan akses tersisa.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
expiry langganan merupakan batas ekonomi: renewal sekali pada expiry, bukan tengahmalam berikutnya
```

Catatan bukti terperinci: [FINDINGS-AU33-AU36-RESEARCH.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU33-AU36-RESEARCH.md>).

<a id="au38"></a>

### AU38 — Tarif produk deposito berubah hanya karena save/load

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:786>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
786:         setMacroIndex(5);
787:         this.firmManager.reset();
788:         this.corporateService.reset(clock.getCurrentDate());
789:         this.riskService.reset();
790:         this.fundBuilder.reset();
```

- Keparahan Sedang; status Terbukti.
- Lokasi akar: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:786 (`setMacroIndex(5)`) sebelum804 (`timeDepositManager.reset()`). Pengaturan tarif198–202. Resetproduk C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/multiasset/TimeDepositManager.java:63–77. LoadTradingFacade3107–3110 menerapkan makro setelahrestore.
- Repro: gameQAbaru macro5; save tanpa memajukan waktu/mengubahmakro; loadfacadeQAbaru. ProdukDEP-BCA-1M sebelum3,50%/tahun, sesudah5,40%; macro5keduanya.
- Dampak: penawaran bunga berbeda hanya karena reload, bukan kejadianekonomi. Kontrak aktif belumterbukti berubah; jangan mengklaimnya.
- Saran: terapkan makro setelah seluruh managerproduk selesaireset, sebelum penawaran. Contoh `timeDepositManager.reset(); ... setMacroIndex(5);`. Gunakan rumus makroexisting, jangan pilih bunga baru/merepricekontraklama.
- Check: semuaenamproduk identik sebelum/sesudahsave-load padamakrosama; perubahanmakro mengubahpenawaranbaru tetapi ratekontrakaktif tetap.
- Bukti: FacadeBoundaryProbe.java; facade-boundary-probe.log.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
reset semua produk -> baru terapkan setMacroIndex; load tidak mengubah rate tanpa event baru
```

Catatan bukti terperinci: [FINDINGS-AU37-AU38-FACADE.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU37-AU38-FACADE.md>).

<a id="au39"></a>

### AU39 — Laporan risiko dan ringkasan utang mengabaikan margin USD

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2177>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
2177:     public synchronized StressTestResult runStressTest(StressTestScenario scenario) {
2178:         Account personal = getPersonalAccount();
2179:         Money marginDebt = riskService.getMarginDebt(personal.getId(), Currency.IDR);
2180:         return replayEngine.runStressTest(personal, scenario, catalog, fxService.getMidRate(), marginDebt);
2181:     }
```

Keparahan Sedang. Status Terbukti pada domain produksi, tanpa perubahan kode dan memakai saveQA baru.

Lokasi dan jalur:
- C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2177–2180 memasok hanya marginIDR ke runStressTest, padahal mesin replay menguranginya dari totalaset IDR pada C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/core/replay/HistoricalReplayEngine.java:217–219.
- TradingFacade.java:3748–3756 mengabaikan USDmargin untuk debt/assets yang diberikan keTaxAuditService.
- TradingFacade.java:4513–4523 mengirim hanya marginIDR keInvestorArchetypeService (efek klasifikasi belum diprobe, jangan diklaim terjadi pada fixture).
- TradingFacade.java:4554–4555 mengisi financialSummary.liabilitiesIdr dengan marginIDRsaja; installmentpun tidaktermasuk meskipun labelgenericliabilities.
- TradingFacade.java:4751–4757,4777–4783: totalLiabilities dihitungulang dari gross-net sehingga totalbenar, tetapi liabObj.marginDebt tetapIDRsaja dan isReconciledtrue walau komponen tidakmenjumlahketotal.

Repro: gameQA modalRp50juta, rankPORTFOLIO_MANAGER fixture, takeMarginLoanUSD1000 (jalurcanonical dengan ledger dan save). Kurs15800. PanggilgetPersonalNetWorth,runStressTest(defaultMarketCrash),getPlayerProfileJson,getConsolidatedWealthBreakdown,getTaxAuditStatus.
Hasil: USDdebt1000, cashIDR-ekuivalen65,8juta, canonicalnetworth50juta. Stressbaseline65,8juta (utang15,8juta tidakdikurangi). ProfileliabilitiesRp0. ConsolidatedtotalLiabilities15,8juta tetapi semuakomponenliabilities0 dan isReconciledtrue. Taxratio0%, ratingA; dengan utang15,8juta/totalaset65,8juta rasionya sekitar24,01% dan evaluator yang sama masuk kategoriB, bukanA.

Dampak: informasi risiko, hasil stress test, dan rincian kekayaan saling bertentangan; pemain dapat mengambil keputusan berdasar utang yang tersembunyi. Ini bukan bukti canonical networth bertambah atau utang benar-benar dihapus. Canonical getPersonalNetWorth/evaluateMarginHealth sudah mengonversi USDdebt dengan benar pada fixtureini.

Perbaikan minimal: gunakan data debtcanonical dan kurs sama seperti getPersonalNetWorth, jangan hanya membaca satu currency. Contoh inti `Money debtIdr = riskService.getMarginDebt(id,Currency.IDR).add(new Money(riskService.getMarginDebt(id,Currency.USD).getAmount().multiply(fxService.getMidRate()),Currency.IDR));`. Reuse laporan marginexisting jika menyediakan totalIDRsetara; jangan membuat perhitungan kurs baru di UI. Untuk ringkasan yang berlabel seluruhkewajiban, tambahkan installment/maintenance/shortliability sesuai definisi laporan; untuk stress yang hanya mencakup margin, labelcakupan harus jelas. Pastikan jumlah komponen total sebelum isReconciledtrue, bukan menetapkantrue tanpa cek. Pertahankan personalnetworth yang saatini benar.

Check minimum: IDR-only,USD-only,campuran; stressbaseline cocok dengan ekuitas dalam cakupannya; komponenutang konsolidasi menjumlahketotal; TaxAudit mendapat utang dan aset yang konsisten. PerubahanFX mengubah valuasi USDdebt, bukan kuantitasutangUSD.

Bukti: FacadeIntegrationProbe.java; facade-integration-probe.log. Probe lain padafileyangsama juga mengonfirmasi getPrestigeStatusmenolak shortterbuka karena collateralreserved; calonbugprestigedibatalkan.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
marginDebtIdr + marginDebtUsd * valuationFx -> dipakai konsisten pada semua laporan
```

Catatan bukti terperinci: [FINDING-AU39-DEBT-REPORTS.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDING-AU39-DEBT-REPORTS.md>).

<a id="au40"></a>

### AU40 — Judul tidak terlihat ketika mode kontras tinggi aktif

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/index.html](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/index.html:173>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
173:                     <h1 style="font-size:1.25rem; font-weight:700; color:#FFF; margin:0 0 10px 0;">Pusat Keuangan</h1>
174:                 </div>
175:             </div>
176:             <div class="keuangan-subnav">
177:                 <button class="subtab-pill active" id="keuangan-subtab-makro" data-keuangan-subtab="makro">🌐 Iklim & Warta</button>
```

- Keparahan: Sedang. Status: Terbukti pada Chromium QA yang merender markup/CSS produksi; belum diuji pada perangkat Android.
- Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/index.html:156 dan 173; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/style.css:2585–2588; applyPlayerPreferences C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:766–776.
- Kode: h1 Bisnis Saya dan Pusat Keuangan memakai inline `color:#FFF`. `.page-heading` memakai `background:var(--surface)`; `html.high-contrast` mengubah --surface menjadi #FFFFFF.
- Dampak: judul putih di atas putih, rasio kontras hasil render 1:1. Pengaturan aksesibilitas membuat judul justru hilang. Ini masalah keterbacaan, bukan kehilangan data atau crash.
- Reproduksi: aktifkan preferensi highContrast melalui fungsi produksi applyPlayerPreferences, buka Pusat Keuangan. Pada fixture markup/CSS asli di viewport 390 px, computed color dan background keduanya rgb(255,255,255); screenshot mengonfirmasi judul hilang. Bisnis memiliki pola kode identik; screenshot untuk Bisnis belum diambil.
- Perbaikan: gunakan token warna existing pada kedua judul, misalnya `color:var(--text-primary)` menggantikan warna literal, lalu periksa judul lain yang hardcoded. Jangan menambah tema/dependency baru. Semua perubahan tampilan tetap memerlukan preview/persetujuan sesuai AGENTS.md.
- Verifikasi: kedua judul terlihat di tema Emas Klasik/Midnight Neon dengan highContrast on/off; ukur warna hasil render, tidak cukup mengecek string CSS. Perlu pemeriksaan layar lain untuk warna literal yang sama.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
color: var(--text-primary);
```

Catatan bukti terperinci: [FINDINGS-AU40-AU41-CSS.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU40-AU41-CSS.md>).

<a id="au41"></a>

### AU41 — Label subtab Pusat Keuangan bertumpuk dan terpotong pada layar sempit

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/style.css](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/style.css:2897>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
2897: .keuangan-subnav {
2898:     display: flex !important;
2899:     gap: 4px !important;
2900:     margin-bottom: 12px !important;
2901:     background: rgba(0, 0, 0, 0.4) !important;
```

- Keparahan: Sedang. Status: Terbukti pada Chromium QA menggunakan markup/CSS produksi, 320 px pada skala teks 100% dan 200%. Skala teks browser bukan bukti font scaling Android fisik.
- Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/style.css:2897–2918; C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/index.html:176–180.
- Kode: container `flex-wrap:nowrap !important; overflow-x:hidden !important`; tombol `flex:1 1 0 !important; min-width:0 !important; white-space:nowrap !important`.
- Dampak: empat label dipaksa masuk ke empat tombol terlalu sempit; label meluber ke tombol sebelah, kemudian dipotong container. Pada 320 px/100%, setiap tombol clientWidth 68 px sedangkan konten sampai 99 px; pada 200% sampai 196 px. Tidak ada scrolling horizontal untuk membaca bagian tersembunyi. Tombol masih ada; masalah adalah identifikasi pilihan dan aksesibilitas, bukan soft-lock trading.
- Reproduksi: buka Pusat Keuangan pada viewport 320 px; baca Iklim & Warta, Radar Saham, Deposito Bank, Kalender. Ulangi dengan ukuran font root 200%. Screenshot css-keuangan-320-2-hcFalse.png memperlihatkan label saling terpotong.
- Saran minimum: gunakan reflow existing dengan baris/kolom yang muat dan izinkan teks membungkus. Contoh arah untuk dipreview, bukan perubahan yang sudah diterapkan:
```css
.keuangan-subnav { flex-wrap:wrap !important; }
.keuangan-subnav .subtab-pill {
  flex:1 1 8rem !important;
  white-space:normal !important;
  overflow-wrap:anywhere;
}
```
- Verifikasi: 320/360/390 px, teks 100/200%, nama lengkap dapat dibaca, fokus terlihat dan target sentuh tetap memadai. Uji juga Android WebView; minta persetujuan preview sebelum mengubah source.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
.keuangan-subnav { flex-wrap: wrap !important; }
.keuangan-subnav .subtab-pill { white-space: normal !important; }
```

Catatan bukti terperinci: [FINDINGS-AU40-AU41-CSS.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU40-AU41-CSS.md>).

<a id="au42"></a>

### AU42 — Portofolio tab gabungan tidak diperbarui live; label baru tidak cocok dengan updater

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:1221>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
1221:     function syncPortfolioLive() {
1222:         if (currentTab !== 'aset') return;
1223:         const posContainer = document.getElementById('portfolio-positions-list');
1224:         const summary = document.getElementById('asset-summary');
1225:         if (!posContainer || !summary || !currentAccount || !bridge.getPortfolio) return;
```

- Keparahan Sedang; Terbukti pada fungsi produksi di browser QA.
- Lokasi C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:157–163 (routing mengubah aset menjadi pasar_porto), 947 dan 10573/10592 (caller), 1221–1222 (guard), 1252–1266 (label lookup), 2183–2189 dan 2211–2213 (label yang dirender).
- Bukti kode:
```js
function syncPortfolioLive() {
    if (currentTab !== 'aset') return;
```
  Namun pembuka normal menetapkan currentTab='pasar_porto', currentPasarPortoSubtab='porto'. Updater tidak pernah membaca getPortfolio pada keadaan ini. Selain itu, updater mencari 'IDR total'/'IDR tersedia' dan 'Laba/Rugi Mengambang (Unrealized)', sedangkan renderer membuat 'IDR Total'/'IDR Tersedia' dan 'Laba/Rugi Mengambang'.
- Dampak: nilai posisi, kas, dan laba rugi di portofolio dapat tertinggal selama pemain tetap di layar itu. Pindah tab/membuka ulang dapat memuat ulang data; uang domain tidak hilang karena masalah tampilan ini.
- Reproduksi: buka Portofolio melalui tab gabungan, lalu jalankan refresh live setelah payload portofolio berubah. Probe mencatat readCount=0 dan semua label tetap OLD. Diagnostik tambahan memaksa nama tab lama aset (bukan navigasi normal): net worth berhasil diperbarui, tetapi cash/pnl tetap OLD karena lookup label tidak cocok.
- Perbaikan: izinkan kedua konteks yang benar pada shared updater dan selaraskan penanda baris. Contoh guard minimum:
```js
if (!(currentTab === 'aset' || (currentTab === 'pasar_porto' && currentPasarPortoSubtab === 'porto'))) return;
```
  Untuk baris, gunakan penanda data existing bila tersedia; alternatif diff minimum cocokkan teks yang benar, jangan menambah framework. Semua caller termasuk tombol retry dan lifecycle harus menuju updater yang sama.
- Verifikasi: ubah harga, kas tersedia/tertahan dan posisi pada domain QA selagi Portofolio tetap terbuka; pastikan nilai berubah tanpa pindah tab. Uji akun klien juga. Guard serupa C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:2028 membuat market highlights tidak masuk cabang refresh pada tab gabungan; perlu uji khusus daftar penguat/pelemah sebelum menutup perbaikan migrasi tab.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
if (!(currentTab === 'aset' || (currentTab === 'pasar_porto' && currentPasarPortoSubtab === 'porto'))) return;
```

Catatan bukti terperinci: [FINDINGS-AU42-AU48-UI.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU42-AU48-UI.md>).

<a id="au43"></a>

### AU43 — UI mengarang pengendali dan persentase ownership melebihi 100%

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:1528>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
1528:             // Tentukan Pemegang Pengendali & Publik Resmi
1529:             let ctrlName = 'PT Dwimuria Investama (Pengendali)';
1530:             let ctrlPct = 54.94;
1531:             if (stockId.includes('BMRI') || stockId.includes('BBRI') || stockId.includes('TLKM')) {
1532:                 ctrlName = 'Negara Republik Indonesia (Pengendali)';
```

- Keparahan Sedang; Terbukti pada renderer dengan payload Java aktual QA.
- Lokasi C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:1528–1555, 1570–1599, 1602–1627. Kontrak otoritatif TradingFacade.java:3411–3450, khususnya coverageNote:3450.
- Bukti kode: `let ctrlName='PT Dwimuria Investama (Pengendali)'; let ctrlPct=54.94;`, lalu `publicPct=100-ctrlPct` meskipun publicShares sudah dikurangi mappedTotal; setelah itu daftar menambahkan seluruh pemegang tercatat lagi. Default tersebut juga dipakai untuk ticker yang tidak ada pada daftar khusus, termasuk emiten US.
- Dampak: pemain melihat pengendali yang tidak ada pada registri, persentase publik tidak cocok dengan jumlah saham, dan peringatan data partial/inconsistent disembunyikan. Pembelian/penerbitan baru tidak memperbarui proporsi pengendali sesuai kepemilikan nyata domain.
- Reproduksi: payload domain QA PARTIAL, sharesOutstanding=1.000.000.000, mappedShares=118.117.500, tidak memiliki pemegang Dwimuria. Renderer tetap menampilkan Dwimuria 549,40 juta saham/54,94%, publik 45,06%, lalu pemegang lain sekitar11,81% lagi; total sekitar111,81%. publicShares yang dihitung332.482.500 sebenarnya33,24825%, bukan45,06%. Status coverage disembunyikan (`display:none`). Tidak mengklaim perusahaan nyata memiliki data ini; ini membuktikan renderer menambahkan informasi yang absen dari payload.
- Perbaikan: hapus atribusi pemilik/persentase hardcoded dari UI; render holders[].percent, unmappedShares/unknownLegacyShares, treasury dan coverage dari domain. Contoh prinsip:
```js
const holders = snapshot.holders || [];
status.textContent = snapshot.coverageNote || '';
// Gunakan holder.percent dari domain; saham tak terpetakan tetap dilabeli tak terpetakan.
```
  Jangan menamai saham tak terpetakan sebagai investor nyata. Pertahankan keputusan filter pemegang <1%/pengecualian pemain; jumlah agregat tetap harus rekonsiliasi. Perubahan tampilan wajib preview/persetujuan.
- Verifikasi: registri PARTIAL/COMPLETE/INCONSISTENT, emiten IDX/US, treasury, short dan penerbitan saham; jumlah dan persentase berasal dari denominator yang sama.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
tampilkan snapshot.holders[].percent dan unmappedShares; jangan menambah pemilik rekaan
```

Catatan bukti terperinci: [FINDINGS-AU42-AU48-UI.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU42-AU48-UI.md>).

<a id="au44"></a>

### AU44 — Tombol promo PRO/WHALE tidak membuka langganan

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:4330>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
4330:                         <button class="btn-primary" style="width:100%; min-height:36px; padding:6px 0; font-size:0.75rem; font-weight:700;" onclick="openResearchTiersModal()">Beli PRO ($250k)</button>
4331:                     </div>
4332: 
4333:                     <div style="background:var(--surface-raised); border:1px solid var(--gold); border-radius:10px; padding:10px; display:flex; flex-direction:column; justify-content:space-between;">
4334:                         <div>
```

- Keparahan Sedang; Terbukti pada Chromium QA dalam scope IIFE yang sama dengan produksi.
- Lokasi C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:7 (IIFE), 3918 (fungsi lokal), 4330 dan 4342 (inline onclick). Pencarian seluruh C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js tidak menemukan window.openResearchTiersModal.
- Bukti: kedua tombol memakai `onclick="openResearchTiersModal()"`, tetapi fungsi lokal hanya tersedia di closure IIFE, tidak pada global scope handler HTML.
- Dampak: tombol promo yang ditawarkan pada Radar tidak berfungsi. Koreksi setelah fullnavigation: callback Kelola Paket Langganan memang ada di terminal, tetapi terminal tidak memiliki entry aktif; lihat AU51. Jangan menyimpulkan ada jalur alternatif UI yang terjangkau.
- Reproduksi: akun Basic, buka Pusat Keuangan -> Radar Saham -> klik Beli PRO dan Beli WHALE. Probe melaporkan dua `openResearchTiersModal is not defined`, openedCount=0.
- Perbaikan: gunakan callback DOM sebagaimana pola existing, bukan mengekspos semua fungsi ke window:
```js
promoSection.querySelectorAll('button').forEach(button => button.onclick = openResearchTiersModal);
```
  Pasang setelah markup promo dibuat dan hapus inline handler yang lama, atau gunakan tombol node existing. Tidak mengubah produk/harga ekonomi.
- Verifikasi: kedua CTA membuka modal yang sama dengan Kelola Paket Langganan tanpa pageerror dan tanpa langsung menarik uang.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
promoSection.querySelectorAll('button').forEach(button => button.onclick = openResearchTiersModal);
```

Catatan bukti terperinci: [FINDINGS-AU42-AU48-UI.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU42-AU48-UI.md>).

<a id="au45"></a>

### AU45 — Promo Whale menjanjikan US$500 ribu, domain menagih US$1 juta

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:4337>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
4337:                                 <small style="color:var(--text-muted); text-decoration:line-through; font-size:0.68rem;">$1.000.000</small>
4338:                                 <strong style="display:block; font-size:1rem; color:var(--gold);">$500.000 <span style="font-size:0.65rem; color:var(--text-secondary);">/ 30 hr</span></strong>
4339:                             </div>
4340:                             <small style="font-size:0.66rem; color:var(--text-secondary); display:block; margin-bottom:8px; line-height:1.3;">Data alternatif eksklusif & katalis mega kenaikan >20%.</small>
4341:                         </div>
```

- Keparahan Sedang; Terbukti dari markup yang dirender dan DTO Java.
- Lokasi C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:4271–4276 dan 4333–4342; TerminalTier.java:10–12; TradingWebBridge.java:1973–1988, 1995–2007.
- Bukti: UI menulis `PROMO PERDANA DISKON 50%`, Whale `$500.000 /30 hr` dan `Beli WHALE ($500k)`, sedangkan TerminalTier.ENTERPRISE/getResearchTiers.feeUsdRaw='1000000.00'. Tidak ada diskon perdana pada subscribeResearchTier; harga resmi pemilik adalah1juta.
- Dampak: ekspektasi saldo/harga salah; setelah CTA AU44 diperbaiki, pemain dengan500ribu tetap tidak memenuhi harga resmi. Tidak terbukti pemotongan uang melampaui harga review; modal langganan lain menampilkan harga domain yang benar.
- Reproduksi: akun Basic, baca kartu Whale Radar, bandingkan dengan getResearchTiers atau modal Kelola Paket Langganan. Probe ui-radar-probe mencatat500k vs1000000.
- Perbaikan: tampilkan feeUsd/feeUsdRaw dari getResearchTiers sebagai satu sumber harga; hapus janji diskon yang tidak didukung domain. Jangan menurunkan biaya domain atau menciptakan promo baru karena keputusan harga pemilik sudah jelas.
- Verifikasi: seluruh launcher, review, transaksi dan renewal memakai250kPRO/1jtWhale; saldo kurang ditolak tanpa potongan. Preview teks baru tetap memerlukan persetujuan.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
hargaPromo = getResearchTiers().feeUsd; // hapus diskon yang tidak disahkan domain
```

Catatan bukti terperinci: [FINDINGS-AU42-AU48-UI.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU42-AU48-UI.md>).

<a id="au46"></a>

### AU46 — Grafik histori kosong diisi30 candle sintetis dengan NaN

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:5494>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
5494:                 let bars = chartData.bars || [];
5495:                 if (!bars.length) {
5496:                     // Fallback Day 1: Bangkitkan lilin historis acuan langsung di klien jika backend belum terisi
5497:                     const currentP = Number(stock.currentPrice || 1000);
5498:                     bars = [];
```

- Keparahan Sedang; Terbukti pada fungsi UI produksi dengan chart EMPTY dari domain.
- Lokasi C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:5494–5514, 5530–5579; domain TradingFacade.java:3671–3727 (EMPTY adalah keadaan valid).
- Bukti:
```js
if (!bars.length) {
    const currentP = Number(stock.currentPrice || 1000);
    // selanjutnya sin/cos membuat30bar
}
```
  currentPrice dari bridge berupa 'Rp 10.000', bukan numeric; currentPriceRaw='10000.00' ada, tetapi diabaikan fallback.
- Dampak: chart saat belum ada transaksi menampilkan lilin/volume rekaan, readout NaN dan koordinat SVG NaN; label aksesibilitas malah mengklaim grafik berasal dari transaksi game. Tidak mengubah harga domain atau uang.
- Reproduksi: new game QA08:30, getChartData R1D success=true, coverage=EMPTY, totalTrades=0, bars=[]; buka detail saham. Probe mendapatkan uiBarCount=30, nonFiniteUiCloses=30, SVG mengandungNaN, readout volume432 rekaan.
- Perbaikan akar masalah: hapus pembuatan histori di UI, tampilkan keadaan kosong sesuai domain dan pertahankan pilihan timeframe. Sekadar mengganti currentPrice dengan currentPriceRaw hanya menghilangkanNaN tetapi tetap membuat histori fiktif.
```js
if (!bars.length) {
    emptyState(chartWrapper, 'Belum ada transaksi', 'Grafik muncul setelah transaksi tercatat.');
    return;
}
```
  Pastikan kontrol timeframe tetap dirender sebelum return, seperti alur existing.
- Verifikasi: fresh game, instrumen sepi/tidak ditransaksikan pada range terpilih, lalu fill pertama menghasilkan candle dari domain; tidak ada data OHLC/volume rekaan atau NaN.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
if (!bars.length) { emptyState(chartWrapper, 'Belum ada transaksi', 'Grafik muncul setelah transaksi tercatat.'); return; }
```

Catatan bukti terperinci: [FINDINGS-AU42-AU48-UI.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU42-AU48-UI.md>).

<a id="au47"></a>

### AU47 — Statistik pasar di detail saham adalah angka perkiraan tanpa label

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:5611>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
5611:         const rawCurrent = parseCleanPrice(stock.currentPriceRaw || stock.currentPrice);
5612:         const rawPrev = parseCleanPrice(stock.previousCloseRaw || stock.previousClose) || rawCurrent;
5613:         const rawOpen = parseCleanPrice(stock.openPriceRaw || stock.openPrice) || rawPrev;
5614:         const rawHigh = stock.highPriceRaw ? parseCleanPrice(stock.highPriceRaw) : Math.round(Math.max(rawCurrent, rawOpen) * 1.015);
5615:         const rawLow = stock.lowPriceRaw ? parseCleanPrice(stock.lowPriceRaw) : Math.round(Math.min(rawCurrent, rawOpen) * 0.985);
```

- Keparahan Sedang; Terbukti pada render dengan instrumen dari bridge produksi.
- Lokasi C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:5611–5615, 5641–5650, 5655–5698; TradingWebBridge.java:207–222 (DTO getInstruments tidak membawa previousCloseRaw/openPriceRaw/highPriceRaw/lowPriceRaw/volumeLots).
- Bukti: High/Low = max/min harga*1.015/0.985, Lot=round(15000000/currentPrice), Val=Lot*lotSize*price; Prev/Open jatuh ke currentPrice; ARA/ARB selalu +/-25%; Avg ditampilkan sebagai harga terakhir. UI menyajikannya sebagai statistik sesi, bukan estimasi.
- Dampak: pembacaan aktivitas, rata-rata dan rentang pasar menyesatkan; tidak sinkron dengan transaksi/limit harga yang benar di domain. Khusus aturan US/fraksi harga, angka batas +/-25% tidak otomatis benar.
- Reproduksi: instrumen QA harga10000, pada chart08:30 belum ada trade. UI tetap menampilkan Lot1,50K, High10150, Low9850 dan Val1,50B; kelima field statistik tidak ada dalam DTO. Angka tersebut bukan hasil riwayat trade yang dikirim domain. Ini terpisah dari fallback30candle AU46: statistik rekaan juga berjalan ketika chart tidak kosong.
- Perbaikan: kirim statistik sesi dari getter/agregasi transaksi dan price-band domain yang sudah ada, atau tampilkan 'Belum tersedia' untuk field yang belum disediakan. Jangan mengarang data pengganti di JavaScript. Gunakan currency dan unit volume yang benar (lembar vs lot) untuk IDX/US.
- Verifikasi: sesi0trade, beberapafill pada harga berbeda, pergantian hari, USD/IDR dan batas fraksi; hasilUI sama dengan data domain, bukan formula fallback.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
statistik tidak tersedia -> tampilkan Belum tersedia, bukan angka formula perkiraan
```

Catatan bukti terperinci: [FINDINGS-AU42-AU48-UI.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU42-AU48-UI.md>).

<a id="au48"></a>

### AU48 — Klik bid/ask hilang setelah pembaruan live order book

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:1751>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
1751:             if (depthTable && book && book.bids && book.asks) {
1752:                 const tbody = depthTable.querySelector('tbody');
1753:                 if (tbody) {
1754:                     const rowCount = Math.max(book.bids.length, book.asks.length);
1755:                     tbody.innerHTML = Array.from({ length: rowCount }).map((_, i) => {
```

- Keparahan Sedang; Terbukti pada fungsi UI produksi/browser QA dengan book dari Java.
- Lokasi C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:5716–5739 (kelas/data-side/data-price/listener awal) dan 1751–1764 (penggantian tbody tanpa kontrak interaksi).
- Bukti: render awal memberi `.clickable-price` dan addEventListener pada tiap cell; updater mengganti `tbody.innerHTML` dengan td kelas positive/negative biasa tanpa data dan listener.
- Dampak: harga yang awalnya bisa dipilih berhenti membuka tiket sesudah tick live pertama. Tombol BELI/JUAL dock masih berfungsi; bukan kegagalan matching/domain.
- Reproduksi: buka detail saham dengan book QA09:00, klik ask membuka tiket (probe memakai callback pencatat, tidak mengeksekusi uang). Jalankan updateInstrumentDetailLive, klik ask lagi: tickets tetap1, remainingClickableCells=0, tanpa pageerror.
- Perbaikan: pertahankan class/data pada renderer shared dan gunakan satu delegated listener di tabel yang tidak diganti, atau perbarui cell existing tanpa membuang handler. Telusuri kedua caller initial/live, jangan hanya mengikat ulang sekali di pembuka layar.
```js
// Pasang sekali pada table yang tetap hidup; kedua renderer mempertahankan dataset.
table.addEventListener('click', event => {
    const cell = event.target.closest('[data-price][data-side]');
    if (!cell || cell.dataset.price === '-') return;
    // gunakan alur buka tiket dan pengisian harga existing
});
```
- Verifikasi: tick berulang dan jumlah level berubah, klik bid/ask tetap mengisi sisi dan harga yang tepat, tidak menggandakan callback. Uji keyboard jika cell diubah menjadi kontrol interaktif.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
pasang satu delegated listener pada tabel yang tetap hidup; pertahankan data-price/data-side saat refresh
```

Catatan bukti terperinci: [FINDINGS-AU42-AU48-UI.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU42-AU48-UI.md>).

<a id="au49"></a>

### AU49 — Filter tahap e-IPO bergantung tahun2027 dan menganggap semua belum allotted sebagai ongoing

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:6436>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
6436:                 const filtered = ipos.filter(ipo => {
6437:                     if (activeIpoFilter === 'past') return ipo.isAllotted;
6438:                     if (activeIpoFilter === 'upcoming') return !ipo.isAllotted && (ipo.offeringStartDate && ipo.offeringStartDate.includes('2027'));
6439:                     // Default Ongoing: penawaran yang sedang dibuka
6440:                     return !ipo.isAllotted;
```

- Keparahan Sedang. Status Terbukti pada Java+ChromiumQA.
- Lokasi C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:6436–6440, 6451–6458. Kontrak tanggal domain: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java:1623–1638; IpoOffering.java:67–69.
- Potongan kode:
```js
if (activeIpoFilter === 'past') return ipo.isAllotted;
if (activeIpoFilter === 'upcoming') return !ipo.isAllotted && (ipo.offeringStartDate && ipo.offeringStartDate.includes('2027'));
return !ipo.isAllotted;
```
- Dampak: penawaran masa depan masuk Ongoing, Upcoming kosong untuk penawaran mendatang pada2026/2028/dst; status tidak mengikuti tiga hari penawaran. Pemain dapat membuka formulir untuk jendela yang belum aktif, lalu ditolak domain. Tidak membuktikan transaksi di luar tanggal diterima domain.
- Reproduksi: newgameQA tanggal2026-10-19, bridge menyediakan10IPO, domain isOfferingPeriod memberi1aktif dan7mendatang. UI Ongoing10, Upcoming0. Semua tanggal berasal dari generator produksi; tidak perlu memajukan ke2028 untuk membuktikan bug2026.
- Perbaikan: gunakan status/date yang ditentukan domain pada timestamp snapshot yang sama. Tambahkan field status/canSubscribe hasil isOfferingPeriod jika belum tersedia; UI hanya merender/memfilter. Tidak hardcode tahun. Tampilkan detail/read-only untuk upcoming/past, aktifkan pesan hanya saat window berlaku. Jangan mengubah panjang periode yang sudah disetujui.
```java
// Arah DTO, gunakan helper domain existing:
.put("canSubscribe", ipo.isOfferingPeriod(facade.getClock().getCurrentDate()))
```
- Verifikasi: tanggal sebelum pembukaan, setiap hari window, setelahpenutupan/allotment/listing, lintas2026/2027/2028; penawaran masuk tepat satu kategori sesuai status yang disepakati (detail status produk bila belum didefinisikan jangan dipilih sepihak).

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
canSubscribe = ipo.isOfferingPeriod(clock.getCurrentDate()); // DTO domain, tanpa literal tahun2027
```

Catatan bukti terperinci: [FINDINGS-AU49-AU50-IPO.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU49-AU50-IPO.md>).

<a id="au50"></a>

### AU50 — Quantity pecahan dipotong diam-diam menjadi long sebelum validasi domain

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java:1647>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
1647:             JSONObject req = new JSONObject(jsonRequest);
1648:             String ipoId = req.getString("ipoId");
1649:             long lots = req.getLong("lots");
1650:             if (lots <= 0) return new JSONObject().put("success", false).put("message", "Jumlah lot harus positif").toString();
1651:             com.tradingglobal3d.core.corporate.IpoSubscription sub = facade.orderIpo(ipoId, lots, req.getString("requestId"));
```

- Keparahan Sedang. Status Terbukti pada bridge produksi; UI e-IPO juga terbukti mengirim pecahan.
- Lokasi TradingWebBridge.java:1647–1651(orderIpo), 740–751(parseOrder shared),1089(multiAssetOrder),1586/1604(shortshares), serta TradingFacade.java:1848(corporateOfferAction). Bukti runtime langsung dalam audit ini untuk IPO dan submitOrder; sibling yang lain baru kesamaan parser/source, perlu test kasus permethod.
- Source UI e-IPO C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:6318–6327 hanya memeriksa finite dan >0, bukan integer; 6330 menggunakan input mentah untuk toast.
- Kode: `long lots=req.getLong("lots")`; `obj.getLong("quantity")`. org.json mengonversi numeric BigDecimal pecahan dengan longValue, bukan longValueExact.
- Dampak: tindakan yang dijalankan berbeda dari input/review; misalnya1,5lot menjadi1lot. Ini bug validasi kuantitas, bukan bukti saldo negatif/uang tak terbatas.
- Reproduksi: IpoUiProbe newgameQA, naikkan pangkat QA agar akses sah, pilih IPO-CLOUD yang periodenya aktif. b.orderIpo dengan lots=1.5 berhasil, orderedLots=1 dan reservedMoneyRp50.000. Pada bridge submitOrder quantity100.9 untuk IDX:QA, order diterimaOPEN dengan remainingQuantity100. BrowserUI menerima1.5, mengirim1.5 dan toast 'Pemesanan1.5lot...berhasil'.
- Perbaikan shared trust boundary: gunakan parser integer exact yang sudah ada jika tersedia; bila tidak, BigDecimal.longValueExact pada token JSON number/string dengan error ramah. Lalu pertahankan validasi >0, batas aman, lotSize dan kas di domain. Validasi UI Number.isSafeInteger membantu, tetapi tidak menggantikan bridge.
```java
long lots = new java.math.BigDecimal(req.get("lots").toString()).longValueExact();
```
```js
if (!Number.isSafeInteger(lots) || lots <= 0) {
    errText.textContent = 'Jumlah lot harus bilangan bulat positif.';
    return;
}
```
- Gunakan orderedLots/quantity dari receipt untuk feedback. Telusuri semua pembacaan jumlah/tenor getLong/getInt yang menerima input UI; jangan memodifikasi field waktu/ID yang tidak relevan tanpa alasan. Angka di luar range long, pecahan, NaN/Infinity, string kosong dan perkalian lot overflow harus ditolak sebelum mutasi.
- Verifikasi: pecahan1.5/100.9 ditolak tanpa mutasi kas/order, integer sah tetap diterima, request ulang idempotent. Form tiket saham existing C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:9425 dan9484 sudah memeriksa integer lot; ini aman untuk input normalnya, tetapi tidak menutup boundary bridge maupun formIPO.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
long lots = new BigDecimal(req.get("lots").toString()).longValueExact();
```

Catatan bukti terperinci: [FINDINGS-AU49-AU50-IPO.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU49-AU50-IPO.md>).

<a id="au53"></a>

### AU53 — Tiket dan rincian dana tertahan menampilkan saldo pribadi saat akun klien aktif

**Keparahan final: Sedang. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:9408>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
9408:         if (currentAccount && latestSnapshot?.personalAccount?.cash && selectedInstrument) {
9409:             const cur = selectedInstrument.currency || 'IDR';
9410:             const cashObj = latestSnapshot.personalAccount.cash[cur];
9411:             if (availEl && cashObj) availEl.textContent = cashObj.available || '-';
9412:             if (reservedEl && cashObj) reservedEl.textContent = cashObj.reserved || '-';
```

- Keparahan Sedang; Terbukti pada Java+browserQA.
- Lokasi C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:9408–9412, 10295–10303 (android/app/src/main/assets/www).
- Source:
```js
const cashObj = latestSnapshot.personalAccount.cash[cur];
const reservedTotal = latestSnapshot?.personalAccount?.cash?.IDR?.reserved || 'Rp 0';
const orders = JSON.parse(bridge.getOrders(currentAccount.id, true));
```
- Dampak: satu layar mencampur dompet pribadi dan order akun klien; pemain dapatmenyangkakas/tertahan klien jauhlebihkecil. Domain/quote yangdiuji tetap memakaiakunbenar; tidakada bukti uangklien masukrekeningpribadi.
- Reproduksi: QA rankPortfolioManager,acceptMANDATE-01=>kas100jt; orderLIMIT100sahamQA@9900menahan991.485. Personal50jt,tertahan0. currentAccountklien. updateOrderEstimate menampilkanavailable50jt/reserved0, padahalgetPortfolio(klien)available99.008.515/reserved991.485; previewdomain dibawahnya benar99.008.515. ModalrincianmenulistotaltertahanRp0tetapimemuatorderklien100saham.
- Saran: ambilsnapshotportofolio currentAccount.id sebagai sumberdompet/rincian, sebagaimanasumberquote; pertahankanheaderkaspribadijikaitumemanglabelyangdisetujui. Janganubahotorisasiaccountatauledger.
```js
const portfolio = bridgeResult('getPortfolio', currentAccount.id);
const cashObj = portfolio.cash[cur];
```
  Hindarifetchduplikatsetiapkeystroke jika quote/snapshotakun yangbenar sudahtersedia; reusepayloadexisting bilaada.
- Verifikasi: personal/klien danIDR/USD, switchakunditiket,tertahanorderIPO/fund/corporate,gangguanbridge menampilkanstale/unavailableyangjelas; quote/receipt tetapakunyangdipilih.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
const portfolio = bridgeResult('getPortfolio', currentAccount.id);
const cashObj = portfolio.cash[cur];
```

Catatan bukti terperinci: [FINDING-AU53-AND-AU08-EXTENSION.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDING-AU53-AND-AU08-EXTENSION.md>).

<a id="au52"></a>

### AU52 — Welcome dan panduan masih menjelaskan24x/IDX24jam

**Keparahan final: Rendah. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:9837>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
9837:                     <strong style="color:#FFDF80; font-size:1rem;">24× Otomatis</strong>
9838:                 </div>
9839:             </div>
9840:             <p style="font-size:0.82rem; color:var(--text-muted); text-align:left; line-height:1.4; margin:0 0 16px 0;">
9841:                 • 1 hari game berlangsung sekitar 1 jam waktu nyata.<br>
```

- Keparahan Rendah. Status Terbukti pada teks source; welcome default belum diambil screenshot terpisah dalam auditini.
- Lokasi C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:4919,4954/4960,9837–9842,9860–9861,9901. Aturan aktif AGENTS.md menyebut48x,1harigame30menitnyata,IDX09–16denganistirahat12–13:30.
- Bukti: welcome9837 `24× Otomatis`,9841 `1 hari game berlangsung sekitar 1 jam waktu nyata`,9842 `Bursa IDX buka 24 jam`; tutorial9861mengulangaturanini; label clock modal4954menyebut24xwalauactualrate bisa48x.
- Dampak: onboarding memberi jadwal/laju yang salah, membingungkan pemain ketika order menunggu sesi berikutnya. Domain clock tidak dibuktikanberubahke24x; justru tesclocklulus48x. Ini ketidaksesuaian informasi, bukan crash.
- Reproduksi: profil browser tanpa game_welcome_shown dan tanpa no_welcome memanggil openWelcomeModal pada10488–10490. Buka JadwalSesiBursa diLainnya untukteks24x; bandingkanrateclock48. Jalur tutorialinteraktif9854 tidakmemilikicalleraktifsehingga bukanbuktisemua pemainmelihatnya, tetapi harusdiselaraskansebelumdipulihkan.
- Saran: ubahteksmengikutikeputusan48xdanjamIDXyangbenar; gunakan clock/rules sebagai sumberangka, hindari literal berbeda di beberapa panduan. Tidak menambah kontrolpause/speed/skip.
```js
// Contohteksuntukpreview, belumditerapkan:
'48× Otomatis · 1 hari game sekitar 30 menit nyata'
```
- Verifikasi: welcome,panduansingkat,modaljadwal,danpanduaninteraktif(ketikadiakses)konsisten; pastikanreviewteksUIoleh pemilik sesuaiAGENTS. Ini menggantikanOBS-UI-02 di catatansebelumnya agar tidakdihitungduakali.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
48× Otomatis · 1 hari game sekitar 30 menit nyata · IDX 09.00–16.00 dengan istirahat sesi
```

Catatan bukti terperinci: [FINDINGS-AU51-AU52-NAVIGATION.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDINGS-AU51-AU52-NAVIGATION.md>).

<a id="au55"></a>

### AU55 — Jumlah usaha menampilkan undefined

**Keparahan final: Rendah. Status: Terbukti pada lingkup bukti yang dijelaskan.**

Lokasi utama: [C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js](<C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:2527>). Lokasi akar/caller tambahan tercantum di rincian.

Potongan source snapshot:
```text
2527:                     const cList = JSON.parse(bridge.getCompanies() || '[]');
2528:                     btnUsaha.textContent = `🏢 Usaha (${cList.length})`;
2529:                 } catch(e) {}
2530:             }
2531:         }
```

Keparahan Rendah. Status Terbukti pada halaman produksi utuh di Chromium QA untuk profil Basic dan Pro.
Root: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM.
Lokasi: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/www/app.js:2527–2528. Kontrak: TradingFacade.java:482–494, TradingWebBridge.java:1015–1017.
```js
const cList = JSON.parse(bridge.getCompanies() || '[]');
btnUsaha.textContent = `🏢 Usaha (${cList.length})`;
```
getCompanies menghasilkan object {success:true,companies:[]}, bukan array. Property length pada object itu undefined.
Dampak: label jumlah usaha salah dan menampilkan teks teknis yang membingungkan. Ini tidak membuktikan usaha hilang atau transaksi gagal; isi daftar memakai data.companies pada jalur lainnya.
Reproduksi: buka Bisnis dari navigasi halaman produksi dengan DTO read-only dari domain QA. ui-full-probe.log untuk Basic dan Pro sama-sama mencatat businessLabel='🏢 Usaha (undefined)',0unsupportedNativeCalls,0booterror. Probe awal belum menangkap getCompanies sehingga menampilkan label lama; fixture dilengkapi dengan respons Java asli dan diulang. Tidak menganggap kegagalan fixture sebagai bug produksi.
Saran minimum:
```js
const data = bridgeResult('getCompanies');
btnUsaha.textContent = `🏢 Usaha (${(data.companies || []).length})`;
```
Gunakan bentuk payload domain yang sama di seluruh caller. Jika successfalse, tampilkan kegagalan/data terakhir yang jelas. Perubahan teks mengikuti preview/persetujuan yang berlaku.
Verifikasi:0,1,beberapa usaha,refresh setelah pendirian,dan error bridge. OBS-UI-01 dari catatan sebelumnya kini menjadi AU55; jangan menghitung keduanya.

Contoh arah perbaikan minimum (ilustrasi/pseudocode bila nama variabel/API belum ada; sesuaikan alur existing dan uji sebelum dipakai):
```text
const data = bridgeResult('getCompanies');
btnUsaha.textContent = 'Usaha (' + (data.companies || []).length + ')';
```

Catatan bukti terperinci: [FINDING-AU55-COMPANY-COUNT.md](<C:/Users/HYPEAM~1/AppData/Local/Temp/GameSaham-RemainingAudit-a65m3mzp/FINDING-AU55-COMPANY-COUNT.md>).


## Lampiran B — Batas audit yang tetap harus diingat

- Ada dua reproduksi yang menghentikan jam dunia; tidak berarti seluruh run/pemain pasti mengalami freeze.
- Save semantik AU27 dibuktikan melalui fault injection QA; belum membuktikan aksi permainan biasa selalu menciptakannya.
- Domain net worth dan pemisahan akun pada kasus AU39/AU53 dapat benar ketika laporan/UI salah. Jangan mengganti perhitungan kanonis yang sudah benar untuk menyesuaikan tampilan salah.
- Tidak semua fenomena berulang tanpa menambah scheduler: berita/misi/IPO mempunyai generator, sementara beberapa aksi korporasi/RUPS/kalender/laporan analis tetap berupa seed. Kejadian sekali per kontrak/pencapaian tidak otomatis bug; siklus konten baru memerlukan keputusan produk.
- Tidak ada bukti kelulusan seluruh suite, run tanpa batas, fresh APK, emulator/perangkat fisik, semua failure filesystem Android, atau audit lisensi/CVE transitif penuh.
- Kandidat yang belum dibuktikan tidak boleh dipromosikan menjadi bug Terbukti atau langsung diedit. Telusuri caller dan reproduksi dahulu jika pemilik menunjuknya.

Selesai membuat panduan tidak sama dengan selesai memperbaiki AU01–AU55.
