# Plan eksekusi perbaikan audit logika, UI dan likuiditas Game Saham

ID: AUDIT-LOGIKA-UI-2026-11-01.

Status dokumen: SIAP DISERAHKAN KEPADA AGENT. Belum dieksekusi. Pembuatan file ini tidak menjalankan perbaikan, memulai plan lama, meluncurkan agent, atau mengizinkan tindakan perangkat/publikasi. Pemilik menunjuk file ini ketika memberi tugas eksekusi kepada pelaksana.

## 1. Tujuan dan sumber wajib

Perbaiki akar masalah 17 temuan utama A01-A10/B01-B07, tindak lanjuti C01-C12 sesuai bukti dan keputusan, serta terapkan keputusan Q01-Q10 dan lanjutannya yang sudah disahkan. Dukungan order besar 10.000-50.000 lot adalah bagian lingkup, tetap All-Or-None. Jangan mengubah temuan potensial menjadi bug pasti tanpa reproduksi atau klaim perbaikan hanya dari pemeriksaan statis.

Root produksi satu-satunya:

`C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`

Baca sebelum bekerja:

1. `AGENTS.md` di root tersebut, instruksi langsung pemilik terbaru, dan instruksi yang berlaku pada direktori source.
2. `Work/HANDOFF-AUDIT-LOGIKA-DAN-RESPON-UI-2026-11-01.md` di root tersebut. Ini sumber detail lokasi kode, cuplikan bukti, reproduksi, rekomendasi dan acceptance setiap ID. Bagian 11 memuat keputusan; bagian 12 memuat keputusan terbaru dukungan order besar.
3. `DESIGN.md`, design brief yang dirujuknya, dan `Work/Hermes-Handoff-2026-10-20/PHASE-QUALITY-STANDARDS.md` sesuai pekerjaan.
4. `Work/STATUS.json` hanya sebagai snapshot historis; verifikasi source dan bukti terkini.

Urutan keputusan: instruksi terbaru pemilik, keputusan Q&A terbaru yang terdokumentasi dalam handoff, lalu instruksi/dokumen proyek yang tidak bertentangan. Nomor baris dan hash handoff adalah bukti snapshot audit, bukan jaminan source belum berubah saat pelaksana mulai. Jangan membaca source repo lama atau mengimpor data/build/credentials dari sana.

## 2. Batas kerja

- Pertahankan perubahan lokal pihak lain. Jangan reset, clean, stash, menghapus massal, mengubah pengaturan global, atau menjalankan launcher/agent lain.
- Produksi tetap Java + Android native WebView + HTML/CSS/JS lokal. Domain Java memiliki ekonomi/waktu/save; UI hanya input dan presentasi. Gunakan helper, stdlib, API native dan runner existing; hindari dependency/framework baru.
- Save pemain `Artifacts/LocalPreview/preview-save/save_game_v2.json` HANYA di-hash; jangan baca isi, menyalin sebagai fixture, mengubah atau memigrasikannya. Hash audit: `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`. Jika berbeda saat baseline, catat perubahan eksternal; jangan memulihkan paksa ke hash lama.
- Gunakan save QA baru terisolasi. Periksa tujuan tulis setiap runner sebelum menjalankannya; jangan menganggap runner berlabel QA otomatis aman.
- Tidak ADB, instalasi, tindakan perangkat, push/publish, pesan pihak lain, pembelian layanan atau perubahan global tanpa izin yang berlaku. Build APK tidak membuktikan instalasi/pengujian perangkat. Commit bukan deliverable wajib; jangan commit tanpa instruksi yang berlaku dan peninjauan file tugas.
- Perubahan UI/teks/navigasi/alur memerlukan preview dan persetujuan per perubahan. Pilihan Q08 `Kalender dan jam` hanya berlaku bagi komponen dan alur yang diperlihatkan; bukan persetujuan semua layar. Perbaikan internal helper tidak boleh disertai redesign tersembunyi.
- Jika ada rule produk yang belum dijawab, telusuri keputusan terakhir; jangan menebak atau menanyakan ulang keputusan yang sudah jelas. Tanyakan hanya detail yang benar-benar menghambat, sambil mengerjakan bagian independen.
- Nontrivial fix harus memiliki runnable regression check minimum memakai runner yang ada. Lakukan root-cause fix dan telusuri semua caller.

### 2A. Batas perubahan kode — wajib, sesuai instruksi pemilik terbaru

1. **Hanya ubah yang diinstruksikan.** Setiap perubahan harus terhubung ke ID temuan A01-A10/B01-B07, catatan C01-C12 yang memenuhi syarat, keputusan Q&A yang sudah disahkan, atau gate pengujian/penyerahan dalam plan ini. Bila pemilik menunjuk subset, hanya subset itu dan dependensi langsung yang diperlukan yang boleh dikerjakan.
2. Sebelum mengedit suatu kelompok pekerjaan, catat singkat ID, akar masalah, file/fungsi yang diperlukan dan hasil yang diharapkan. Daftar ini untuk menjaga lingkup; keputusan teknis rutin yang sudah tercakup tidak perlu meminta izin ulang.
3. Perubahan helper bersama/caller terkait diperbolehkan hanya bila diperlukan untuk menutup akar masalah atau mencegah regresi langsung pada alur yang ditugaskan. Telusuri caller dan buktikan relevansinya; jangan memakai istilah root-cause fix sebagai alasan merombak modul lain.
4. **Dilarang perubahan sambilan:** refactor umum, rename massal, format seluruh file, konversi line ending, migrasi framework/dependency, perubahan palet/layout/navigasi, penghapusan fitur/kode/data, perubahan aturan ekonomi atau pembaruan versi yang tidak diperlukan oleh butir yang sedang dikerjakan. Optimisasi/penghapusan yang memang tercantum C03-C07 hanya dilakukan pada lokasi yang relevan, dengan bukti yang disyaratkan dan keputusan/persetujuan yang berlaku.
5. Pertahankan perilaku di luar requirement yang sengaja direvisi. Menyentuh file yang sama tidak mengizinkan membersihkan bagian lain di file itu. Jangan menimpa, mengembalikan atau memasukkan perubahan lokal pihak lain ke dalam hasil pekerjaan sendiri.
6. Jika menemukan bug baru di luar lingkup, catat lokasi, bukti, dampak dan rekomendasinya dalam laporan; **jangan langsung memperbaikinya**. Jika perbaikan luar lingkup benar-benar prasyarat yang tidak dapat dipisahkan, jelaskan perluasan yang dibutuhkan dan minta persetujuan sebelum mengerjakan bagian tersebut. Lanjutkan pekerjaan independen yang sudah diizinkan.
7. Jangan membuat aksi finansial baru, mengubah ambang/fee/harga/spread, menambah layar atau menyederhanakan validasi hanya untuk memudahkan test. Detail produk belum disahkan tetap menunggu pemilik. Preview terpilih tidak mengizinkan perubahan layar lain.
8. Setelah setiap hasil terukur, periksa diff dibanding baseline sebelum pekerjaan. Tiap hunk baru harus bisa dijelaskan kaitannya dengan ID/requirement. Koreksi perubahan sambilan milik sendiri secara terarah; jangan memakai reset/clean/revert massal yang dapat menghapus pekerjaan pihak lain.
9. Penyerahan menyertakan pemetaan **ID/requirement -> file/fungsi/hunk perubahan -> pengujian/bukti**, serta daftar file yang berubah dan alasannya. Pemeriksaan lingkup harus memastikan tidak ada perubahan di luar instruksi, tanpa menyatakan diff lama pihak lain sebagai perubahan agent.
10. Sinkronisasi AGENTS.md pada T00 terbatas pada keputusan pemilik yang sudah disahkan dan rujukan plan/handoff. Ini tidak mengizinkan agent melonggarkan aturan kerja, mengubah otoritas, memperluas izin, atau menghapus pembatasan save/UI/perangkat.

## 3. Keputusan produk yang wajib dipertahankan

| Topik | Ketetapan |
|---|---|
| Waktu | Otomatis 48x pada aktif/background/offline; tidak ada pause/speed/skip pemain. Seluruh durasi diproses; optimisasi batch tidak boleh membuang waktu. |
| Sesi | IDX 09.00-12.00 dan 13.30-16.00; US 21.30-04.00 waktu game WIB, mengikuti instruksi aktif. |
| Transaksi | AON pemain: seluruh kuantitas terisi atau tidak sama sekali. Limit antre OPEN bila belum dapat terisi penuh; market dibatalkan utuh bila tidak cukup saat diproses, reservasi dilepas. |
| Market saat tutup | Diterima sebagai antrean sesi berikutnya, tetap MARKET; eksekusi harga saat sesi sah dibuka, harga estimasi tidak dijamin. Validasi ulang dana/saham/fee dan AON. |
| Save | Autosave stabil dan hening. Jika penulisan benar-benar ditolak setelah pemulihan otomatis dicoba: blokir transaksi baru pada boundary domain/native; data tetap dapat dilihat, warning/coba simpan ulang tersedia; lindungi state memori dan salinan valid terakhir. Dunia tetap 48x. Buka transaksi setelah save benar-benar sukses. |
| Deposito | Pokok aktif tetap aset. Bunga belum jatuh tempo hanya estimasi; akui saat menjadi hak pemain di jatuh tempo, sekali saja. |
| Bailout | Sekali pakai, hanya net worth tidak positif DAN kas tidak mencukupi; perhitungkan seluruh aset/utang. Jangan memilih angka hibah/ambang baru. |
| Prestige | Badge kosmetik dan kas reset Rp50 juta; hilangkan janji bonus reputasi/XP/modal tambahan yang tidak berlaku. Backup dan proteksi reset tetap ada. |
| Berita | Efek aktif bertanda dijumlah terhadap acuan yang sama: +5% -3% = +2%. Bukan perkalian berurutan. Deadline nyata 60/120 menit tetap persisten. |
| Fraksi berita | Naik dibulatkan ke atas, turun ke bawah pada harga sah; target tepat fraksi tidak digeser. Band harian tetap berlaku. Acuan lintas hari yang belum diputuskan tidak boleh ditebak. |
| Penyelarasan UI | Data terakhir dapat dilihat dengan label sedang diperbarui; transaksi terkunci sementara. Pilihan desain `Kalender dan jam`; mengikuti progres domain sebenarnya. Tahap menyimpan tetap terkunci; panel hilang setelah pemrosesan DAN save sukses. Tidak memperpanjang tunggu untuk animasi; reduced-motion dihormati. |

### 3A. Likuiditas dasar dan dukungan order besar

- NPC utama: IDR tersedia < Rp5 miliar -> isi hingga tersedia Rp10 miliar; USD tersedia < US$500 ribu -> isi hingga tersedia US$1 juta. Nilai tersedia tidak mencakup kas tertahan order.
- Per instrumen: saham bebas <50 lot -> isi hingga 2.000 lot bebas; ukuran lot sesuai instrumen. Bebas mengecualikan komitmen yang benar-benar belum dikurangi pada posisi; jangan menghitung kewajiban dua kali.
- Jumlah tambahan hanya kekurangan menuju target, bukan nominal target penuh. Mata uang/instrumen diperiksa terpisah. Pengisian boleh berulang ketika kebutuhan kembali memenuhi syarat tanpa cooldown minimum; pemeriksaan identik tanpa perubahan tidak menambah lagi.
- Sumber kas: fasilitas likuiditas pasar simulasi sebagai arus dana eksternal, tanpa utang/bunga/pengembalian, tanpa mengambil dana pemain atau mengubah kepemilikan pemain.
- Sumber saham: selalu penerbitan baru sebesar kekurangan. Jumlah saham beredar dan posisi penerima harus konsisten; pemegang lama tetap memiliki lembar yang sama tetapi persentase terdilusi. Pemilik menerima dilusi ini. Jangan menyamarkannya sebagai transfer saham lama atau stock split.
- **Aturan terbaru:** 2.000 lot, Rp10 miliar dan US$1 juta adalah target dasar, BUKAN batas maksimum dukungan. Order besar valid memicu tambahan likuiditas sesuai kekurangan kebutuhan tanpa harus lebih dahulu menurunkan stok di bawah ambang dasar.
- Dukung skala 10.000-50.000 lot melalui quote, inventory, kas, fee, reservasi dan settlement yang konsisten. Jangan menetapkan 50.000 lot sebagai maksimum input baru tanpa instruksi. Gunakan likuiditas sah yang tersedia dan hitung kekurangannya menurut aturan matching/priority yang berlaku.
- AON, kemampuan bayar pemain, kepemilikan penjual, sesi/halt, harga limit, band dan proteksi save tidak boleh dilewati. Jangan mint kas/saham dari input tidak sah. Jangan membuat 25 pengisian parsial 2.000 lot untuk menyamarkan satu order 50.000 lot yang seharusnya AON.
- Semua event memiliki identitas persisten dan diterapkan tepat sekali. Issued/outstanding, posisi, ledger, receipt dan state restore harus sesuai. Tidak ada harga/spread/bonus kompensasi atau repricing baru yang disahkan oleh keputusan likuiditas ini.

## 4. Tahapan kerja dan cakupan

Semua baris di bawah berstatus BELUM_DIEKSEKUSI pada saat dokumen disiapkan.

| Tahap | ID / cakupan | Hasil wajib dan gate |
|---|---|---|
| T00 | Baseline, instruksi, C01/C02 | Buktikan root/branch/HEAD/diff, hash source/save, catat perubahan eksternal. Pastikan semua test baru dikompilasi dan katalog fixture sama dengan produksi yang diuji. |
| T01 | B01 | Perbaiki scope `parseCleanPrice` satu kali untuk seluruh caller; tiket beli/jual, formulir IPO dan live detail dapat dipanggil tanpa ReferenceError. |
| T02 | A01 | Reproduksi estimator maker vs eksekusi; preflight seluruh settlement sebelum mutasi. Kas/posisi/reservasi/order/fee tetap konsisten pada gagal, AON dan self-trade terjaga. |
| T03 | A02/A03, Q02, C08 | Validasi save lengkap bersama untuk pemilihan primary/backup dan rotasi; tawarkan recovery sesuai persetujuan. State gagal save tidak mengizinkan mutasi pemain baru; retry tidak menggandakan transaksi; warning hilang saat sukses tanpa spam toast. |
| T04 | A04/A05/A10, Q03/Q05/C10 | Pokok deposito masuk kekayaan; bunga mengikuti keputusan; harga instrumen/posisi selaras; debt lintas mata uang mengikuti satu plafon; bailout hanya insolvency sesuai keputusan. |
| T05 | A06/A07/B02/B03/A08 | Reset sesi US tepat sekali; seluruh elapsed diproses; resume tidak menambah drift per klik; toggle tidak mem-pause dunia; endpoint skip pemain ditolak. Uji crossing pembukaan, cutoff, penutupan dan midnight. |
| T06 | Q04/C09 + bagian 12 handoff | Likuiditas dasar serta adaptif order besar, issued/ownership dan arus dana eksternal konsisten. QA beli/jual 10.000/50.000 lot, market/limit dan multi-maker tanpa partial player settlement. |
| T07 | Q07/Q09/Q10 | Antrean MARKET saat tutup aman save/load dan diproses saat sesi sah buka. Agregasi berita serta pembulatan arah benar, deadline persisten; jangan menebak acuan lintas hari bila belum disahkan. |
| T08 | Q08 | Komponen `Kalender dan jam` berdasarkan preview pilihan, progres domain nyata aman antar-thread, data lama berlabel, transaksi terkunci hingga save sukses, panel kemudian hilang. Ukur respons UI selama catch-up. |
| T09 | B04/B05/B06/B07/A09, Q06 | Error pada elemen UI yang terlihat; endpoint koleksi benar; nominal invalid mendapat feedback; teks prestige sesuai manfaat; statistik fill ke-501+ tercatat tepat sekali. Preview perubahan teks/komponen yang belum disetujui. |
| T10 | C03-C07/C11/C12, regresi | Ukur bottleneck sebelum optimisasi; packaging aset audit tanpa menghapus bukti; audit kode mati; profil lifecycle sebelum klaim leak; rekonsiliasi navigasi melalui bukti persetujuan; verifikasi toolchain/dependensi. |
| T11 | Penyerahan | Laporan status setiap ID, file berubah, command/bukti, ketidakpastian, hasil QA terpisah per lingkungan, hash save akhir dan next_action. Tidak ada klaim rilis hanya dari build/host. |

### 4A. Sinkronisasi AGENTS.md

AGENTS.md pada saat penyiapan masih memuat status konflik/pembulatan berita menunggu keputusan dan belum memuat seluruh keputusan baru. File sudah mempunyai perubahan lokal sebelum auditor bekerja. Handoff bukan bukti file itu sudah tersinkron.

Ketika pelaksana menerima tugas menjalankan plan ini, lakukan sinkronisasi minimum pada bagian aktif AGENTS.md sebagai bagian T00: rujuk handoff bagian 11-12, perbarui keputusan yang jelas kedaluwarsa, dan pertahankan semua aturan/perubahan lokal yang tidak bertentangan. Jangan menulis ulang seluruh file atau menghapus riwayat sembarang. Jangan mengubah AGENTS.md sebagai cara mengizinkan tindakan yang dilarang. Auditor tidak melakukan sinkronisasi atau perubahan source saat hanya menyiapkan file eksekusi ini.

## 5. Pengujian penerimaan

Gunakan acceptance rinci pada masing-masing ID di handoff. Minimal:

1. **Trading:** limit/market/trigger, beli/jual, AON, self-trade, several maker, band, fee, kas tidak cukup, saham terikat, retry, pembatalan dan kegagalan di tengah operasi. Reproduksi A01 harus ada sebelum klaim penutupan.
2. **Order besar:** 10.000 dan 50.000 lot, IDX/US, kedalaman kecil/besar, harga multi-level, valid/tidak valid. Periksa UI unit lot vs lembar, preview/MAX, persediaan bebas, pendanaan NPC dan hasil settlement. Tidak mengukur keberhasilan hanya dari order diterima.
3. **Save:** primary syntax-invalid/domain-invalid, backup valid/invalid, keduanya invalid, tawaran ditolak/diterima, write gagal/retry, save-reload, dan progres memori tidak dirollback diam-diam. Hash save pemain tetap dibandingkan; semua mutasi QA memakai save baru.
4. **Waktu:** fixed clock, aktif/background/reopen, durasi gabungan vs terbagi, offline 24 jam nyata setara 48 hari game, resume pendek tanpa drift tambahan, kalender per bursa, event/biaya/cutoff tepat sekali, deadline berita nyata.
5. **Ekonomi:** konservasi kekayaan saat penempatan/pencairan pokok deposito, bunga tidak diakui dini, valuation mark benar, debt gabungan benar, bailout valid. Pengisian NPC secara eksplisit arus eksternal/penerbitan baru; cek dilusi dan ledger, bukan klaim ekonomi tertutup.
6. **UI:** tombol kritis, keadaan loading/error/empty/recovery, fokus/Back, 320px dan teks 200%, reduced-motion, catch-up sukses/save gagal, pesan benar-benar berada pada DOM aktif. Animasi tidak menjadi bukti proses domain sudah selesai.
7. **Statistik:** fill 500/501+, deduplikasi dan reload, hanya fill pemain yang relevan dihitung.
8. Terapkan standar 10x100 aksi alur kritis bila berlaku, dengan seed/scenario dan batas runtime/resource yang dicatat. Jangan mengganti test bermakna dengan assertion yang meniru implementasi.

Sebelum menjalankan install/build/test/run, jelaskan command, tujuan tulis dan perlindungan data. Gunakan dependency yang sudah tersedia; jangan fetch/install tanpa kebutuhan/izin yang berlaku. Audit sebelumnya tidak menjalankan target runtime karena isolasi OS yang diwajibkan panduan Security Audit tidak tersedia; itu bukan bukti target aman atau tes telah lulus. Jika pelaksana memakai panduan itu, patuhi batas isolasinya dan laporkan blocker yang nyata; source review/preview mandiri masih dapat dilanjutkan.

Candidate command build/test Android offline: `gradlew.bat --offline --no-daemon testDebugUnitTest assembleDebug` dari salinan kerja QA yang benar. Ini contoh rute, bukan command yang telah dijalankan atau izin perangkat. Periksa dahulu write path runner/Gradle, dependency dan JDK yang digunakan.

## 6. Bukti dan status penyerahan

Gunakan format minimum berikut untuk setiap ID (boleh satu laporan ringkas berisi tabel, tanpa generator laporan baru):

| ID | Status | File/fungsi diubah | Check yang dijalankan | Bukti/output | Keterbatasan/next_action |
|---|---|---|---|---|---|
| A01 ... | BELUM_DIEKSEKUSI / TERKONFIRMASI / DIPERBAIKI_DAN_DIUJI / PERLU_VERIFIKASI / MENUNGGU_KEPUTUSAN / TIDAK_TERBUKTI_DENGAN_ALASAN | Path tepat | Command dan input QA | Lokasi log/hasil | Pekerjaan tersisa |

- Pisahkan pemenuhan aturan produk, perbaikan source, host test, browser, build/signature APK, emulator dan perangkat fisik.
- Sertakan scope ID yang benar: 17 temuan utama, 12 catatan, keputusan Q&A dan dukungan order besar. Catatan performa/keputusan tidak otomatis dihitung sebagai bug.
- Sesudah dua kegagalan sama tanpa diagnosis baru, laporkan PERLU PERHATIAN dengan bukti dan pendekatan lain; jangan loop tanpa diagnosis.
- Jika gated UI menunggu keputusan, lanjutkan bagian independen; jangan menandai seluruh plan selesai. Jika ketiadaan akses/izin menghambat bukti perangkat, laporkan batas itu tanpa klaim telah lolos.
- Selesai berarti requirement yang relevan terhubung ke bukti yang tepat. Keberadaan file, screenshot preview, static parse, compile atau APK tidak cukup membuktikan seluruh game benar.

## 7. Instruksi penugasan yang dapat diberikan pemilik

Prompt siap salin tersedia di `PROMPT-EKSEKUSI.md` pada direktori yang sama. Batas perubahan bagian 2A tetap berlaku meskipun prompt dibaca terpisah dari percakapan.

"Saya menunjuk `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/Eksekusi-Audit-Logika-UI-2026-11-01/plan.md` sebagai satu plan aktif untuk perbaikan audit ini. Baca plan dan handoff yang dirujuk, revalidasi source, lalu kerjakan lingkupnya secara bertahap. Hanya ubah kode yang diperlukan untuk butir yang ditugaskan; patuhi bagian 2A dan jangan melakukan refactor/perubahan sambilan. Pertahankan semua perubahan lokal dan save pemain; patuhi gate preview UI dan izin perangkat yang berlaku. Laporkan setiap ID beserta bukti dan batas pengujiannya. Jangan menjalankan plan lama, agent lain, push atau publikasi."

Kalimat di atas disediakan untuk disampaikan pemilik kepada agent. Auditor tidak mengirimkannya ke agent/chat lain dan tidak memulai pelaksanaan dengan membuat dokumen ini.
