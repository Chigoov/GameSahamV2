# Plan perbaikan audit tambahan Game Saham

Tanggal: 2 November 2026, Asia/Jakarta.

**Status: SIAP DITINJAU, BELUM DIEKSEKUSI.** Dokumen ini memuat lima temuan tambahan dan satu risiko yang perlu reproduksi. Pembuatan dokumen tidak mengganti plan AU01–AU55 yang sedang dikerjakan agent lain dan tidak memulai perubahan source.

## 1. Baseline dan batas bukti

- Root: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`.
- Branch: `codex/classic-ui-new-assets`.
- HEAD saat penyusunan: `9653f7a992405b4dfaf28acddbf1caf158d03957`.
- Working tree memiliki perubahan lokal aktif. HEAD saja tidak mewakili source yang diperiksa. Verifikasi ulang fungsi dan caller sebelum implementasi; nomor baris dapat bergeser.
- Acuan deduplikasi utama: `Work/Perbaikan-AU01-AU55-2026-11-02/PANDUAN-PERBAIKAN-AU01-AU55.md`, `Work/Perbaikan-Audit-Ulang-2026-11-01/`, dan laporan audit historis terkait di `Work/` serta `Artifacts/`.
- Bukti audit tambahan: penelusuran source dan pemeriksaan aritmetika pembulatan AT01. Belum ada reproduksi melalui runner aplikasi, build APK, emulator, atau perangkat untuk temuan tambahan ini. Jangan mengubah bukti source menjadi klaim runtime PASS.
- Saat penyusunan, pola kode AT01–AT05 dan NV01 masih ditemukan. File reksa dana sedang berubah dalam pekerjaan agent lain; AT02 wajib diperiksa kembali setelah paket terkait diserahkan.

## 2. Aturan pelaksanaan

1. Jalankan hanya ID/paket yang ditunjuk pemilik. Jangan menjalankan dua pelaksana yang menulis fungsi yang sama bersamaan. Jika perbaikan AU sebelumnya sudah menutup temuan tambahan, verifikasi buktinya dan tandai `SUDAH_TERTUTUP_OLEH_PERBAIKAN_LAIN`; jangan membuat diff tambahan.
2. Ikuti `AGENTS.md`, keputusan terbaru pemilik dan master plan aktif. Pertahankan 48×, AON pemain, anti-self-trade, Java sebagai pemilik ekonomi/waktu/save, serta produksi Android WebView existing.
3. Perubahan minimum pada akar masalah dan seluruh caller yang relevan. Tanpa framework/dependency baru, refactor massal, atau penyesuaian balancing sepihak. Gunakan runner dan helper existing.
4. Setiap ID harus dimulai dengan reproduksi terisolasi atau penjelasan bukti yang masih kurang. Nontrivial logic wajib mempunyai regression check yang benar-benar gagal jika bug kembali.
5. Source dan perubahan lokal pihak lain dipertahankan. Tidak reset/clean/stash, commit file di luar tugas, push/publish, pesan lintas chat, peluncuran agent lain, atau perubahan global.
6. Save pemain `Artifacts/LocalPreview/preview-save/save_game_v2.json` hanya boleh di-hash. Jangan membaca, menyalin, memuat, atau memigrasikannya. Hash audit terakhir: `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`. Semua reproduksi memakai save QA baru di lokasi terisolasi. Perubahan hash tidak mengizinkan pemulihan paksa.
7. Tidak ADB, instalasi, atau tindakan perangkat tanpa izin yang berlaku. Pisahkan bukti domain host, browser, build APK, emulator dan perangkat fisik.
8. Perubahan tampilan, teks, navigasi, atau alur memerlukan preview nyata dan persetujuan eksplisit sesuai aturan proyek. Persetujuan identik yang sudah ada tidak perlu diminta ulang. Lanjutkan bagian domain independen bila ada keputusan yang menunggu.

## 3. Urutan dan status

| Tahap | ID | Hasil yang harus dicapai | Status |
|---|---|---|---|
| P00 | Baseline | Periksa source terbaru, pekerjaan agent lain, deduplikasi, hash save dan tujuan tulis QA | PENDING |
| P01 | AT01 | Pembukaan short konsisten terhadap pembulatan dan tidak menyisakan mutasi separuh | PENDING |
| P02 | AT02 | Kebutuhan transaksi dana indeks mendapat kesempatan eksekusi pada sesi sah | PENDING |
| P03 | AT03 | Buka/tutup short mematuhi sesi serta halt sebelum mutasi | PENDING |
| P04 | AT05 | Retry transfer holding mengembalikan receipt asli atau menolak payload berbeda | PENDING |
| P05 | AT04 | Empat sektor holding berasal dari identitas perusahaan yang benar | PENDING; pemetaan sektor perlu diverifikasi |
| P06 | NV01 | Reproduksi overlap lifecycle/save; remediasi hanya bila risiko terkonfirmasi | NEEDS_VALIDATION |
| P07 | Penyerahan | Bukti per ID, regresi terkait, diff, hash dan batas pengujian | PENDING |

Urutan prioritas perbaikan: AT01 → AT02 → AT03 → AT05 → AT04. NV01 dapat direproduksi lebih awal setelah baseline source stabil karena menyangkut progres. Tidak ada pekerjaan yang dianggap selesai hanya karena dokumen atau APK tersedia.

## 4. AT01: pembulatan jaminan short dapat gagal setelah mutasi

**Prioritas: tinggi. Bukti: alur source dan aritmetika; reproduksi aplikasi masih diperlukan.**

Lokasi: `android/app/src/main/java/com/tradingglobal3d/core/risk/RiskComplianceService.java`, `openShortPosition`; `core/model/Money.java`; caller `TradingFacade.openShortPosition` dan bridge dengan nama yang sama.

Pemberi pinjaman kehilangan saham melalui `borrowShares` sebelum seluruh operasi berhasil. `grossProceeds`, biaya, dan hasil bersih dihitung mentah, kemudian biaya dan jaminan dibulatkan terpisah. Kas dikredit, fee didebit, lalu `reserveCash(proceedsMoney)` dipanggil. Kewajiban `ShortPosition` baru dimasukkan sesudah reservasi berhasil.

Contoh: 100 saham × Rp202 = Rp20.200. Fee mentah Rp50,50 menjadi Rp51. Kas bersih aktual Rp20.149, tetapi jaminan dari Rp20.149,50 menjadi Rp20.150. Jika kas tersedia awal nol, reservasi kurang Rp1 sesudah mutasi saham dan ledger terjadi. Ini berbeda dari AU09 tentang inventori pemberi pinjaman yang sudah terikat order jual.

- [ ] Reproduksi dengan akun QA berhak short, inventori bebas cukup, harga Rp202, 100 saham, dan kas tersedia nol. Simpan snapshot saldo, reservasi, posisi lender, kewajiban short, dan ledger sebelum/sesudah.
- [ ] Turunkan jaminan dari nilai bruto dikurangi fee yang telah dikuantisasi ke mata uang yang sama; jangan membulatkan kembali komponen dari basis berbeda.
- [ ] Lakukan preflight seluruh syarat yang dapat gagal sebelum `borrowShares` atau mutasi kas. Telusuri caller dan overflow; jangan menutupi kegagalan dengan tambahan kas atau penangkapan exception saja.
- [ ] Regression check mencakup kasus pembulatan IDR di atas, USD dengan pecahan setengah sen, kasus normal, inventori kurang, dan kondisi gagal. Reuse fixture existing jika tersedia.

**Gate:** kegagalan tidak mengubah saldo/ledger/saham/reservasi/kewajiban; keberhasilan menghasilkan jaminan tepat sebesar hasil bersih yang dibukukan, saham pinjaman tercatat sekali, dan save/reload QA konsisten.

## 5. AT02: order dana indeks berakhir sebelum pembukaan berikutnya

**Prioritas: tinggi. Bukti: alur kalender source; reproduksi aplikasi masih diperlukan.**

Lokasi: `core/facade/TradingFacade.java`, `advanceWorldSeconds`, `submitOrder`, `expireDayOrders`, `settleDay`; `core/funds/MutualFund.java`, `getEquityRebalanceOrder`; `MutualFundManager.getEquityRebalanceOrders`.

Caller kalender membentuk order rebalance pada 16.00. IDX sudah tutup sehingga LIMIT masuk buku antrean. Akun pool bertipe `FIRM`, sedangkan expiry harian hanya mengecualikan `PERSONAL`/`CLIENT`. Order dana dapat di-expire saat penutupan hari sebelum IDX buka lagi. Ini berbeda dari AU30 tentang strategi/parameter katalog dan AU29 tentang quote NPC yang tidak ditemukan expiry.

- [ ] Setelah perubahan AU30/AU29 stabil, buat subscription QA dana `EQUITY_INDEX` dengan dana cukup untuk sekurangnya satu lot underlying.
- [ ] Lewati cutoff, midnight, lalu sesi berikutnya dengan likuiditas lawan yang tersedia. Periksa status order, ledger pool, posisi saham, NAV dan reservasi. Jangan memakai perubahan label sebagai bukti investasi terjadi.
- [ ] Pilih perubahan minimum: jadwalkan kebutuhan rebalance pada sesi sah atau pertahankan intent/order dana secara terarah sampai sesi tersebut. Cegah order berganda, reservasi berulang, dan penggunaan harga yang tidak lagi sah.
- [ ] Jangan membuat seluruh akun `FIRM` menjadi GTC; quote NPC tetap mengikuti expiry yang berlaku.
- [ ] Uji pembelian dan penjualan untuk memenuhi redemption, likuiditas kurang/cukup, hari dealing/non-dealing sesuai kontrak dana, serta save/reload di antara cutoff dan pembukaan.

**Gate:** dana memperoleh kesempatan eksekusi pada pasar sah; transaksi dengan likuiditas cukup tercermin di uang dan posisi canonical; permintaan tanpa likuiditas tetap jujur sebagai pending; tidak ada double order atau double reserve. Aturan AON pemain dan expiry NPC tetap lulus.

## 6. AT03: jalur short melewati sesi dan halt

**Prioritas: tinggi. Bukti: alur UI → bridge → facade → layanan risiko; reproduksi aplikasi masih diperlukan.**

Lokasi: `core/facade/TradingFacade.java`, `openShortPosition`/`closeShortPosition`; `core/risk/RiskComplianceService.java`; `mobile/TradingWebBridge.java`; form short pada `assets/www/app.js`.

Jalur short melakukan mutasi langsung melalui layanan risiko, tanpa pemeriksaan sesi/halt yang dipakai jalur order biasa. Pembukaan memeriksa pangkat dan persediaan; penutupan memeriksa kewajiban serta kas. Kedua pemeriksaan itu tidak membuktikan pasar sedang boleh mengeksekusi transaksi. Ini bukan duplikat trigger biasa pada AU01.

- [ ] Reproduksi pembukaan dan penutupan dengan syarat keuangan valid saat IDX/US buka, istirahat/tutup, dan instrumen halt.
- [ ] Terapkan kontrol domain sebelum mutasi menggunakan helper sesi/halt existing pada titik keputusan yang dipakai semua caller produksi.
- [ ] Verifikasi keputusan produk mengenai penolakan versus antrean short di luar sesi. Aturan antrean MARKET biasa tidak otomatis menjadi persetujuan format antrean short baru. Jika belum diputuskan, kerjakan reproduksi/preflight independen dan tandai keputusan yang tepat.
- [ ] Pertahankan akses untuk meminta pengurangan kewajiban. Bedakan kemampuan mengirim permintaan dari izin mengeksekusi perdagangan saat pasar tidak sah.
- [ ] Uji dana dan saham tidak berubah pada penolakan/antrean; cover sah tetap mengurangi kewajiban serta melepaskan jaminan dengan benar.

**Gate:** tidak ada eksekusi short yang melewati sesi atau halt; perilaku permintaan tertunda mengikuti kebijakan yang disahkan; tidak ada UI/alur baru tanpa preview dan persetujuan.

## 7. AT04: sektor holding dipercaya dari input

**Prioritas: sedang. Bukti: validasi source tidak mencocokkan sektor input dengan perusahaan.**

Lokasi: `core/business/CompanyManager.java`, `createHoldingCompany`/`restoreHoldingState`; `OperatingCompany.getSector`/`getSectorModel`; `app.js`, `openHoldingCreationModal`.

Domain memeriksa empat label sektor unik dan empat ID perusahaan unik, lalu menyimpan pasangan input tersebut tanpa membandingkan sektor aktual. UI memasukkan seluruh perusahaan ke setiap pilihan sektor. Empat perusahaan satu sektor dapat dilabeli sebagai empat sektor berbeda.

- [ ] Verifikasi arti empat sektor holding dan pemetaan ke kategori perusahaan existing dari keputusan produk. Bila mapping belum ada, tandai sebagai keputusan pemilik; jangan mengarang mapping.
- [ ] Reproduksi empat perusahaan satu sektor dengan empat label input berbeda. Periksa source terbaru jika AU lain menyentuh bisnis.
- [ ] Domain menentukan atau memvalidasi sektor berdasarkan perusahaan canonical. Periksa restore agar invariant yang sama tidak dilanggar saat load.
- [ ] Jangan langsung menolak/menghapus holding lama yang sebelumnya diterima. Tentukan kompatibilitas dan kebijakan penanganan sebelum memberlakukan aturan restore yang memblokir save.
- [ ] Setelah mapping disahkan, uji sektor sama, label palsu, ID duplikat, perusahaan yang sudah menjadi anggota, kombinasi sah, dan save/reload QA. Filter UI hanya diterapkan setelah preview disetujui.

**Gate:** klaim empat sektor didukung data perusahaan; tidak ada penghilangan holding/progres lama tanpa kebijakan yang disetujui.

## 8. AT05: retry transfer holding memberi receipt dari input baru

**Prioritas: sedang. Bukti: cabang duplicate source.**

Lokasi: `core/business/CompanyManager.java`, `transferHoldingCapital`; `core/facade/TradingFacade.java`, overload dengan `requestId`.

Jika dua key ledger `HOLDING-<requestId>-DEBIT/CREDIT` ada, fungsi langsung mengembalikan sukses dengan nominal/jenis transfer dari permintaan sekarang. Asal, tujuan, nominal dan konteks transaksi pertama tidak dibandingkan. Retry Rp1 juta sebagai Rp10 juta bisa menghasilkan respons Rp10 juta tanpa perpindahan uang tambahan. Ini masalah integritas receipt, bukan bukti uang tercetak ganda.

- [ ] Reproduksi transfer sah, retry identik, lalu retry ID sama dengan nominal, asal/tujuan, jenis, dan holding berbeda yang masing-masing valid.
- [ ] Gunakan informasi ledger/receipt existing untuk mengikat identitas operasi. Periksa pola FX dan pinjaman yang sudah menolak payload berbeda; jangan menambah framework idempotensi.
- [ ] Retry identik mengembalikan hasil asli. Payload berbeda ditolak sebelum mutasi dan tidak mengarang hasil dari input baru. Jika field identitas yang dibutuhkan belum disimpan, tambahkan data minimum dan tetapkan penanganan receipt lama secara eksplisit.
- [ ] Pertahankan penolakan bila baru satu sisi ledger tersedia; jangan otomatis mengirim uang sisi lain tanpa rekonsiliasi.
- [ ] Uji sesudah save/reload. Seluruh retry mempertahankan jumlah mutasi kas awal tanpa duplikasi.

**Gate:** respons cocok dengan transaksi yang benar-benar terjadi, tepat satu debit/kredit untuk satu transfer sah, dan perubahan payload tidak dianggap sukses.

## 9. NV01: potensi dua pemilik penulisan save saat lifecycle bertumpang tindih

**Status: NEEDS_VALIDATION. Belum ada reproduksi kehilangan progres. Tidak diberi tingkat keparahan sebagai bug terkonfirmasi.**

Lokasi: `mobile/MainActivity.java`, `onCreate`/`onDestroy`; `mobile/TradingWebBridge.java`, `destroy`/worker `beginClockSync`; `core/storage/SaveManager.java`, `save`/`getTempFile`.

Activity membuat facade dan SaveManager baru pada direktori yang sama. `destroy()` memanggil `clockWorker.shutdown()`, yang mengizinkan tugas yang sudah diajukan selesai; worker tidak memeriksa status destroyed sebelum setiap penulisan. SaveManager menggunakan file `.tmp` tetap dan sinkronisasi per instance. Hipotesis: instance lama yang belum selesai dapat menulis bersamaan atau sesudah snapshot instance baru.

Referensi perilaku API: [Android ExecutorService](https://developer.android.com/reference/java/util/concurrent/ExecutorService). Ini mendukung perilaku shutdown, bukan membuktikan race terjadi di game.

- [ ] Susun check deterministik dengan dua pemilik save QA, pengaturan urutan kerja terkontrol dan snapshot berbeda. Buktikan apakah overlap dapat dicapai dari lifecycle produksi, bukan sekadar memanggil dua SaveManager secara artifisial.
- [ ] Periksa skenario tutup/buka cepat ketika catch-up/save berjalan, kegagalan tulis, serta pemulihan backup. Pengujian emulator/perangkat mengikuti izin yang berlaku.
- [ ] Jika terkonfirmasi, tetapkan satu pemilik ekonomi/penulisan per save dengan penyerahan kepemilikan lifecycle yang jelas. Lock tulis saja tidak cukup jika snapshot lama masih boleh menimpa snapshot baru.
- [ ] Jangan mengganti `shutdown()` dengan penghentian paksa sebagai satu-satunya solusi; interupsi pada mutasi/penulisan dapat menghasilkan masalah baru.
- [ ] Jika tidak terkonfirmasi, catat skenario, source yang diperiksa, dan alasan penolakannya. Jangan membuat refactor spekulatif.

**Gate:** satu urutan snapshot yang konsisten, tidak ada penimpaan oleh pemilik lama atau benturan `.tmp`, backup tetap dapat dimuat. Nyatakan terpisah hasil host dan hasil lifecycle Android; bukti host saja tidak menutup gate Android.

## 10. Verifikasi dan penyerahan

- [ ] P00: catat root/branch/HEAD/dirty state, plan yang ditunjuk, file yang sedang dimiliki pelaksana lain, hash save serta tujuan tulis QA.
- [ ] Kaitkan setiap bagian diff ke AT/NV yang ditunjuk. Temuan baru di luar paket dicatat tanpa perbaikan sambilan.
- [ ] Jalankan check minimum per akar masalah dan regresi existing untuk short, matching/expiry, fund cutoff, holding, serta save bila tersentuh. Periksa runner sebelum menjalankan; jangan memasang dependensi atau menjalankan target tanpa isolasi yang sesuai.
- [ ] Build APK setelah perbaikan source disahkan dan pemeriksaan relevan lulus, bila termasuk penunjukan pekerjaan. Build bukan pengganti verifikasi domain atau Android.
- [ ] Hash save pemain akhir. Laporkan perubahan hash bila ada tanpa menimpa data.
- [ ] Laporkan per ID: status, pemicu, sebab, caller, file berubah, command, hasil sebelum/sesudah, bukti dan batasnya, keputusan terbuka serta `next_action`.

Status yang dipakai: `PENDING`, `IN_PROGRESS`, `NEEDS_VALIDATION`, `MENUNGGU_KEPUTUSAN`, `DIPERBAIKI_DAN_DIUJI_HOST`, `DIVERIFIKASI_ANDROID`, `SUDAH_TERTUTUP_OLEH_PERBAIKAN_LAIN`, atau `DITOLAK_DENGAN_BUKTI`. Jangan menyamakan hasil source, host, browser dan perangkat. Setelah penyerahan, berhenti dan jangan mengaktifkan plan lain otomatis.
