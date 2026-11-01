# Plan perbaikan hasil audit ulang source Game Saham

Tanggal: 1 November 2026  
ID: REMEDIASI-AUDIT-ULANG-2026-11-01  
Status: AKTIF — EKSEKUSI LANGSUNG DISETUJUI PEMILIK; P01–P04 MEMILIKI BUKTI HOST SUBSET; P05 MENUNGGU PERSETUJUAN PREVIEW; P06 BERJALAN

## 1. Mandat dan hasil yang dituju

Pemilik telah memerintahkan eksekusi langsung oleh Codex, pembaruan dokumen ini dan laporan milestone berkala. Hanya plan ini yang dikerjakan; jangan mengirim tugas ke Hermes atau chat lain secara otomatis.

Tutup 12 temuan audit ulang R01–R12, buktikan regresi A01 settlement/AON yang belum dinyatakan tuntas, dan koreksi status penyerahan berdasarkan bukti. Jangan mengulang semua pekerjaan plan lama atau mengubah fitur di luar akar masalah tersebut.

Root produksi: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`.

Sumber keputusan:

1. Instruksi langsung pemilik terbaru dan AGENTS.md yang berlaku.
2. Keputusan Q&A dalam `Work/HANDOFF-AUDIT-LOGIKA-DAN-RESPON-UI-2026-11-01.md`, terutama bagian 11–12.
3. `Work/Eksekusi-Audit-Logika-UI-2026-11-01/plan.md` untuk acceptance yang masih relevan, bukan klaim sudah selesai.
4. Master plan bagian 2A, DESIGN.md dan standar `Work/Hermes-Handoff-2026-10-20/PHASE-QUALITY-STANDARDS.md` sesuai lingkup.
5. `Work/Eksekusi-Audit-Logika-UI-2026-11-01/LAPORAN_EKSEKUSI_PARIPURNA.md` adalah klaim pelaksana sebelumnya yang perlu direkonsiliasi, bukan bukti kelulusan independen.

Lokasi di bagian 4 memakai nomor baris snapshot audit ulang. Periksa kembali sebelum edit. Temuan source bukan bukti crash atau exploit telah dijalankan pada perangkat. R11 adalah risiko penggunaan class lama; belum terbukti class yang tersedia benar-benar kedaluwarsa.

## 2. Batas perubahan dan perlindungan data

- Hanya ubah kode, test dan dokumentasi yang diperlukan untuk R01–R12 dan regresi A01. Petakan setiap hunk baru ke ID. Tidak ada refactor umum, rename massal, reformat file, konversi line ending, dependency/framework baru atau perubahan balancing sambilan.
- Buktikan cwd, Git root, branch, HEAD, status staged/unstaged/untracked dan hash file terkait sebelum mengedit. Repo memiliki perubahan lokal lama; jangan menganggap seluruh diff sebagai pekerjaan sendiri. Simpan baseline di lokasi QA yang disetujui dan bandingkan perubahan sendiri secara terarah.
- Pertahankan perubahan pihak lain. Tidak reset/clean/stash/revert massal, push/publish, pesan eksternal, launcher paralel, perubahan global, ADB atau instalasi perangkat.
- Save pemain `Artifacts/LocalPreview/preview-save/save_game_v2.json` HANYA di-hash. Jangan baca isi, salin, jadikan fixture, ubah atau migrasikan. Hash audit ulang: `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`. Jika baseline berbeda, laporkan perubahan eksternal; jangan pulihkan paksa.
- Semua mutasi QA memakai save baru terisolasi. Periksa tujuan tulis runner, katalog, caches dan log sebelum menjalankannya. Jangan menjalankan launcher lama secara buta.
- Produksi tetap Java + Android WebView + HTML/CSS/JS lokal. Domain Java memiliki ekonomi/waktu/save. Telusuri seluruh caller dan gunakan helper/runner existing sebelum menambah kode.
- Pertahankan source yang sudah benar: helper harga, deposito, mark posisi, plafon margin gabungan, reset US, deduplikasi fill, penolakan skip dan teks prestige. Uji regresi bila tersentuh.
- Perubahan tampilan, teks, navigasi atau alur memerlukan preview dan persetujuan eksplisit. Pilihan data terakhir + Kalender dan jam sudah disahkan sebagai arah komponen; jangan anggap preview itu menyetujui semua detail baru. Selagi menunggu persetujuan, kerjakan domain/test yang independen.
- Temuan baru di luar lingkup dicatat dengan lokasi dan dampak; perluasan yang benar-benar diperlukan diajukan sebelum diterapkan.

## 3. Aturan produk yang tidak boleh berubah

- Dunia otomatis 48× pada aktif/background/offline; seluruh durasi diproses. Tidak ada pause/speed/skip pemain. Deadline berita 60/120 menit nyata tetap persisten.
- Sesi IDX 09.00–12.00 dan 13.30–16.00; US 21.30–04.00 WIB waktu game. Optimisasi offline tidak boleh melewatkan kewajiban pembukaan/penutupan, cutoff dan settlement yang relevan.
- Pemain PERSONAL/CLIENT tetap AON. LIMIT menunggu OPEN bila pemenuhan utuh belum tersedia; MARKET dibatalkan utuh bila likuiditas tidak cukup saat diproses, dengan reservasi dilepas. MARKET saat tutup menunggu sesi berikutnya dan memakai harga saat eksekusi, bukan harga estimasi lama.
- Save gagal total: blokir mutasi finansial pemain baru, tetap izinkan melihat data dan retry, pertahankan progres memori serta salinan valid terakhir, dunia tetap 48×. Jangan rollback progres diam-diam. Buka transaksi hanya setelah save benar-benar sukses.
- Cadangan bebas MM utama: IDR <Rp5 miliar diisi hingga Rp10 miliar; USD <US$500 ribu hingga US$1 juta; saham bebas <50 lot hingga 2.000 lot per instrumen. Tambah hanya selisih. Pemeriksaan identik tanpa perubahan tidak menambah lagi; tidak ada cooldown minimum.
- Target cadangan adalah dasar, bukan maksimum. Order besar valid 10.000–50.000 lot didukung adaptif tanpa menjadikan 50.000 lot batas input baru. Dana pemain, fee, sesi, halt, band, harga limit dan AON tetap berlaku.
- Kas tambahan adalah modal eksternal fasilitas likuiditas tanpa utang/bunga. Saham tambahan selalu penerbitan baru dan mendilusi persentase pemilik lama tanpa mengubah jumlah saham mereka. Setiap event tercatat persisten dan tepat sekali.
- Penambahan kapasitas likuiditas tidak memberi pemain hak menentukan harga quote NPC. Gunakan aturan harga/spread yang sudah berlaku; jangan memilih spread, subsidi atau kompensasi baru.
- Berita aktif dijumlahkan bertanda pada acuan sama (+5%−3%=+2%), kemudian dibulatkan sekali searah target pada fraksi sah. Jangan menebak kebijakan acuan lintas hari atau konflik deadline yang belum diputuskan.

## 4. Pekerjaan per temuan

Semua path berikut relatif terhadap root produksi. Status eksekusi aktual dan batas bukti ada pada bagian 9 serta LAPORAN-PROGRES.md; uraian butir adalah requirement, bukan klaim kelulusan.

### R01 — Validasi lengkap sebelum memilih atau merotasi save (Tinggi)

Lokasi awal: `android/app/src/main/java/com/tradingglobal3d/core/storage/SaveManager.java:65,132,241`; `core/facade/TradingFacade.java:2666,2701,2870` di bawah direktori Java yang sama.

Bukti: rotasi memakai `validateSaveIntegrity()` yang memparse SaveState, sedangkan validasi graph domain lengkap ada pada facade. Backup offer juga belum memakai validasi lengkap yang sama.

Perbaikan: pisahkan validasi detached yang sudah ada agar dapat digunakan konsisten pada primary, backup, recovery offer dan rotasi tanpa mengubah live state. Pertahankan save format dan API publik bila memungkinkan. Jangan merotasi primary yang hanya valid syntax/struktur dasar. Pertahankan backup sehat bila kandidat gagal. Jangan memulai game baru diam-diam.

Acceptance: primary syntax-invalid/domain-invalid + backup valid; primary valid + backup invalid; keduanya invalid; recovery diterima/ditolak; primary merujuk order/instrumen tidak dikenal; write/move gagal. Setelah recovery lalu save, setidaknya satu salinan valid tetap terlindungi. Uji validator tidak memutasi live state.

### R02 — Pengunci mutasi pemain setelah save gagal (Tinggi)

Lokasi awal: `mobile/MainActivity.java:237`; `mobile/TradingWebBridge.java:767,2282`; `core/facade/TradingFacade.java:4253`.

Bukti: flag saveFailed ada di Activity untuk warning, tetapi boundary transaksi domain belum memeriksanya.

Perbaikan: letakkan status persistensi pada pemilik domain dan periksa sebelum setiap mutasi finansial pemain. Telusuri order/cancel, FX, deposito, dana, koleksi, bisnis, pinjaman, langganan, klaim dan prestige. Bedakan mutasi pemain baru dari kewajiban dunia otomatis. Gagal save pertama tidak boleh menyebabkan transaksi yang sudah diterapkan diulang tanpa idempotency. Save sukses melepas blokir; error serialisasi juga ditangani. Hindari warning/toast berulang.

Acceptance: fault injection write ditolak; satu operasi yang sudah diterapkan tetap di memori dengan receipt; operasi pemain berikutnya ditolak tanpa mutasi; baca data dan dunia 48× tetap berjalan; retry dengan ID sama tidak menggandakan uang/aset; save sukses membuka transaksi. Verifikasi entry domain langsung, bukan tombol saja.

### R03 — Quote adaptif mengikuti aturan pasar, bukan limit pemain (Tinggi)

Lokasi awal: `core/facade/TradingFacade.java:1288,1307`.

Bukti: askP/bidP quote tambahan mengambil harga limit pemain.

Perbaikan: gunakan quote/spread existing yang sah, priority dan band. Limit hanya menjadi batas penerimaan harga pemain. Jangan menciptakan quote yang menguntungkan pemain semata untuk membuat test fill lulus. Gunakan rencana pemenuhan utuh yang sama untuk preview/preflight/settlement sejauh diperlukan.

Acceptance: buku tipis/kosong; limit beli di bawah ask dan jual di atas bid tidak otomatis dipenuhi pada harga buatan; limit marketable dan MARKET valid tetap dapat terlayani; uji dua arah 10.000/50.000 lot dan multi-level, tanpa partial pemain.

### R04 — Jangan menerbitkan saham atau mendanai order tidak sah (Tinggi)

Lokasi awal: `core/facade/TradingFacade.java:1253,1263`; `core/trading/MatchingEngine.java:39,163`.

Bukti: helper likuiditas memutasi suplai/inventory sebelum matching memeriksa kemampuan bayar.

Perbaikan: preflight tanpa mutasi menghitung dana/fee/reservasi, kepemilikan/komitmen, sesi/halt/band dan rencana likuiditas. Terapkan penerbitan, kas, quote dan settlement hanya sesudah rencana valid; cegah keadaan setengah jadi. Gunakan event/request ID persisten dan aritmetika aman. Jangan bypass AON, validasi atau self-trade.

Acceptance: saldo kurang, saham kurang, lot/fraksi salah, sektor terkunci, halt, overflow dan request duplikat. Penolakan tidak mengubah suplai, persentase kepemilikan, kas MM, inventory atau quote ekonomi. Registrasi receipt/status penolakan boleh terjadi. Retry tidak menggandakan event.

### R05 — Modal eksternal masuk ledger dan receipt (Tinggi)

Lokasi awal: `core/npc/NpcAgent.java:75`; `core/facade/TradingFacade.java:1311,1330`; `core/economy/EconomyLedger.java:122`.

Bukti: creditCash langsung menambah saldo tanpa entri modal eksternal.

Perbaikan: alirkan tambahan melalui mutasi ledger existing; tambah jenis entri hanya bila tipe existing tidak mewakili modal eksternal. Jangan mengkredit dua kali. Hubungkan event dengan mata uang, sebab, nominal selisih dan receipt persisten. Rencana likuiditas harus menjaga kas/posisi/issued/ledger/receipt konsisten saat gagal.

Acceptance: IDR/USD terpisah; ambang dan defisit tepat; rekonsiliasi sesudah top-up dan fill; ulang event identik; save/reload; fault sebelum/sesudah penerapan. Tidak ada utang/bunga atau pengurangan saldo pemain.

### R06 — Antrean MARKET dan batas sesi instrumen (Tinggi)

Lokasi awal: `core/facade/TradingFacade.java:1218,2402`; `core/npc/MarketEngine.java:229,283`; `core/trading/OrderManager.java:53`; `core/trading/MatchingEngine.java`.

Bukti: MARKET saat tutup diberi limitPrice harga lama dan dimasukkan ke buku limit; pemeriksaan sesi tick hanya membatasi RETAIL_NOISE.

Perbaikan: telusuri seluruh jalur matching/NPC/trigger sebelum memilih perubahan minimum. Simpan antrean pembukaan secara persisten tanpa memperlakukannya sebagai maker limit biasa. Saat sesi sah buka, validasi ulang dana/fee/saham dan AON pada harga eksekusi. Hindari double reserve. Semua jalur eksekusi harus mematuhi sesi instrumen. Pertahankan data save lama; format lama memerlukan compatibility terarah bila tersentuh, bukan migrasi save pemain.

Acceptance: IDX tutup saat US buka dan sebaliknya; jam 09.00/12.00/13.30/16.00/21.30/04.00; offline melintasi pembukaan; harga pembukaan berbeda dari estimasi; dana tidak cukup; likuiditas kurang; cancel/reload/retry. Tidak ada fill saat sesi instrumen tutup; reservasi terlepas utuh saat batal.

### R07 — Agregasi target berita bertanda (Tinggi)

Lokasi awal: `core/research/ResearchService.java:431,465,513`; `core/npc/MarketEngine.java:241`; `core/research/NewsPriceTarget.java`.

Bukti: target berikutnya menjadi WAITING_PREVIOUS_TARGET dan getter mengambil satu target. Penjumlahan sentimen bukan agregasi target harga.

Perbaikan: gunakan seluruh efek target aktif yang relevan pada acuan sama, jumlah bertanda lalu pembulatan sekali. Sinkronkan mekanisme target, katalis pasar, status dan presentasi yang terdampak tanpa redesign. Pertahankan identitas efek, deadline nyata persisten, paid access dan follow-up. Jangan menggabungkan acuan berbeda secara diam-diam.

Acceptance: +5%−3%=+2%; +3%+2%=+5%; urutan publikasi tidak mengubah target; efek kedaluwarsa keluar dari agregasi; reload identik; target tepat fraksi tidak bergeser. Jika zero-net, acuan lintas hari, target yang sudah tercapai atau konflik deadline membutuhkan keputusan produk yang belum tercatat, ajukan kasus konkret kepada pemilik dan lanjutkan pekerjaan independen.

### R08 — Pencapaian target turun memakai arah yang benar (Sedang)

Lokasi awal: `core/research/ResearchService.java:533`; `core/npc/MarketEngine.java:249`.

Bukti: pencapaian selalu current >= target; katalis pasar yang diperiksa juga hanya mendorong kenaikan.

Perbaikan: gunakan tanda target efektif untuk pemeriksaan tercapai dan telusuri caller katalis/progress/expiry agar target negatif tidak selesai dini. Jangan hanya mengganti satu comparator sementara jalur pasar tetap salah arah. Perilaku target netral mengikuti keputusan R07 yang disahkan.

Acceptance: referensi 9.500 target turun -3% dibulatkan 9.200; harga 9.500 belum tercapai, 9.200 tercapai, 9.175 tetap memenuhi. Uji naik, expiry, reload dan agregasi tanpa status ganda.

### R09 — Jam dan tanggal existing bergerak cepat mengikuti penyelarasan (Sedang)

Lokasi awal: `android/app/src/main/assets/www/app.js:221,10451,10524`; `mobile/TradingWebBridge.java:30,58,127`; `core/facade/TradingFacade.java:2402`.

Bukti: kartu statis hanya pada isManual; startup/resume memakai jalur default; finish(error) melepas blokir.

Keputusan terbaru pemilik, 2 November 2026: panel tunggu/kalender tambahan pada preview ditolak. Gunakan tampilan jam dan tanggal yang SUDAH ADA di game. Saat startup/resume, waktu pada tampilan existing mengejar dengan cepat dari waktu terakhir menuju waktu dunia sekarang sesuai lamanya pemain meninggalkan game; selama waktu itu bergerak, domain sedang menyelaraskan dunia. Jangan menambah kartu, overlay, progress bar, kalender atau layar tunggu baru.

Perbaikan: expose snapshot progres immutable/volatile yang dapat dibaca tanpa menunggu monitor panjang facade. Gerakkan jam/tanggal existing mengikuti waktu game yang benar-benar telah diproses. Interpolasi presentasi boleh memperhalus langkah yang sudah terkonfirmasi, tetapi tidak boleh mendahului progres domain, mengubah clock ekonomi, mempercepat rasio dunia di luar 48×, atau memperpanjang tunggu demi animasi. Seluruh durasi yang ditinggalkan tetap diproses. Ketika tersusul, tampilan kembali bergerak normal 48× tanpa layar sukses tambahan. Data terakhir tetap dapat dilihat dan hanya aksi mutasi dikunci hingga sinkronisasi DAN save sukses. Saat save gagal, gunakan jalur peringatan/retry existing sesuai R02, bukan panel tunggu baru. Hormati reduced-motion dan aksesibilitas.

Gate UI: tampilkan preview menggunakan komponen jam/tanggal existing sesuai keputusan terbaru, lalu minta persetujuan eksplisit sebelum penerapan tampilan yang memerlukannya. Preview panel sebelumnya adalah riwayat yang ditolak; jangan digunakan sebagai persetujuan. R10 tetap komponen terpisah dan belum otomatis disetujui oleh keputusan R09.

Acceptance: cold start, resume singkat/panjang, jam/tanggal existing melintasi tengah malam/bulan/tahun sesuai durasi absen, request tumpang tindih, data lama dapat dilihat, transaksi terkunci, save gagal/retry, selesai tanpa reload, navigasi/Back, 320px, teks 200%, reduced-motion. Tidak ada UI tunggu baru. Ukur respons pembacaan status selama catch-up; tidak ada deadlock atau klaim progres fiktif. Tanggal/jam berhenti mengejar saat mencapai hasil domain dan kembali ke laju normal tanpa jeda animasi buatan.

### R10 — Error pembatalan auto-renewal terlihat (Sedang)

Lokasi awal: `android/app/src/main/assets/www/app.js:3936,5229,5281`.

Bukti: catch masih flowText(card,...) padahal showConfirmModal mengganti flow asal.

Perbaikan: pakai toast/error container existing yang masih terhubung ke DOM, pertahankan pesan domain dan akses langganan. Tampilkan contoh keadaan gagal untuk persetujuan teks/alur sebelum penerapan bila belum tercakup persetujuan sebelumnya.

Acceptance: response gagal dan exception setelah konfirmasi terlihat, fokus tetap masuk akal, cancel tidak menggandakan permintaan, jalur sukses/expiry akses tetap benar.

### R11 — Test menggunakan kompilasi source terkini (Sedang, potensial)

Lokasi awal: `Work/run_synthesis_tests.py:16,22,41`; laporan eksekusi lama bagian pengujian.

Bukti: runner memakai Artifacts/Test-Plan14-Full/classes dan hanya mengompilasi test synthesis. Ini membuktikan ketergantungan artefak historis, bukan membuktikan class pasti stale.

Perbaikan: pastikan produksi dan semua regression test yang ditunjuk dikompilasi dari snapshot sama di output QA baru. Gunakan task Gradle/runner existing paling sederhana yang memenuhi batas eksekusi. Jangan install dependency atau membuat framework baru. Rekonsiliasi jumlah suites/test dengan output nyata, dan sediakan runnable regression untuk perbaikan nontrivial.

Acceptance: kegagalan kompilasi menghentikan tes; test baru tidak terlewat; tidak memakai class produksi historis diam-diam; catat source hash, command, toolchain, jumlah test dan exit code. Hasil lama tidak dihitung sebagai PASS snapshot baru.

### R12 — Cadangan saham dihitung dari inventory bebas (Sedang)

Lokasi awal: `core/npc/NpcAgent.java:62`; helper komitmen pada facade dan order book.

Bukti: threshold memakai pos.getQuantity() tanpa komitmen order jual.

Perbaikan: gunakan posisi dikurangi kewajiban yang memang belum dikurangi dari posisi, per instrumen dan ukuran lot sah. Telusuri cancel/fill/corporate lock agar tidak menghitung kewajiban dua kali. Integrasikan dengan rencana likuiditas R03–R05 dan penerbitan idempotent.

Acceptance: 2.000 lot total dengan 1.980 lot terikat berarti 20 lot bebas; tambahan tepat 1.980 lot untuk mencapai 2.000 lot bebas. Uji batas 49/50 lot, cancel, fill, reload, beberapa order dan pemeriksaan identik berulang.

## 5. Urutan eksekusi dan gate

| Tahap | Lingkup | Syarat selesai |
|---|---|---|
| P00 | Baseline dan R11 | Instruksi/source/runner dan tujuan tulis terpetakan; hash save; daftar reproduksi serta kemampuan eksekusi jelas. |
| P01 | R01–R02 | Validasi save bersama dan proteksi write gagal lolos fault/recovery tests. |
| P02 | R03–R05, R12, A01 | Rencana likuiditas/settlement konsisten; tidak ada mint dari order invalid; ledger cocok; AON dan self-trade terlindungi. |
| P03 | R06 | Antrean MARKET persisten, sesi lintas bursa, harga dan reservasi benar pada buka/cancel/reload. |
| P04 | R07–R08 | Agregasi, pembulatan, arah target dan persistensi sesuai keputusan; kasus yang belum diputuskan tidak diasumsikan. |
| P05 | R09–R10 | Domain progres dapat diuji dahulu; preview disetujui; browser/UI state dan aksesibilitas diperiksa. |
| P06 | Regresi dan penyerahan | Matriks R01–R12 + A01, bukti per lingkungan, diff terarah dan hash save akhir. |

Jangan menunggu UI untuk mengerjakan domain independen. Setelah dua kegagalan sama tanpa diagnosis baru, laporkan PERLU PERHATIAN dan ganti pendekatan berbasis bukti; jangan loop. Status belum diuji tetap PERLU_VERIFIKASI.

## 6. Pengujian dan batas eksekusi

Sebelum install/build/test/run, jelaskan command, tujuan tulis dan perlindungan data. Jangan menjalankan install/fetch tanpa kebutuhan/izin yang berlaku. Gunakan dependency lokal. Candidate task dari salinan QA yang benar: `gradlew.bat --offline --no-daemon testDebugUnitTest assembleDebug`; ini rute kandidat, belum dijalankan dan bukan izin perangkat.

Panduan security-audit yang dipakai pada audit mensyaratkan isolasi OS untuk target-controlled execution: jaringan eksternal mati, environment allowlist dengan HOME/tmp/cache scratch, target/toolchain read-only, hanya scratch writable serta batas resource/waktu. Kemampuan itu belum dibuktikan tersedia. Jangan menganggap lokasi save QA sendiri memenuhi isolasi tersebut atau menganggap permintaan membuat plan mengesahkan pengecualian. Saat eksekusi, buktikan kontrol yang tersedia; jika tidak memadai, siapkan perbaikan/review/check yang aman lalu jelaskan hambatan dan minta pengecualian spesifik bila diperlukan. Jangan mengubah kebijakan/pengaturan global untuk melewatinya.

Minimum regresi bermakna:

- A01: multi-maker dengan fee/reservasi berbeda, maker pemain yang harus dilewati, self-trade, kegagalan preflight dan tidak ada mutasi setengah jadi. Bandingkan estimator dengan rencana settlement aktual.
- Order besar: BUY/SELL 10.000 dan 50.000 lot IDX/US, MARKET/LIMIT, multi-level dan dana/inventory kurang; cek hasil settlement, bukan order accepted saja.
- Save: domain-invalid primer dengan backup sehat, fault penulisan/rotasi, retry dan reload; jangan baca save pemain.
- Waktu: fixed clock, durasi gabungan vs terbagi, offline 24 jam nyata setara 48 hari game; crossing pembukaan, 04.00, 16.00, midnight; kewajiban tepat sekali.
- Regresi perbaikan awal: helper tiket/IPO, pokok/bunga deposito, mark posisi, margin gabungan, bailout seluruh aset, prestige, koleksi, nominal invalid dan fill ke-501+.
- Terapkan 10×100 aksi pada alur kritis yang relevan sesuai standar proyek, dengan seed, batas resource dan hasil dicatat. Ukur catch-up/ledger/save panjang sebelum klaim optimisasi; tidak ada target performa baru yang dikarang.
- Pisahkan hasil static review, host JVM, browser, build APK, emulator dan perangkat. APK tidak membuktikan runtime Android atau kesiapan rilis.

## 7. Penyerahan dan definisi selesai

Pada eksekusi nanti, tulis satu laporan ringkas di direktori plan ini. Jangan menimpa laporan lama sehingga riwayat klaim hilang; rujuk koreksinya secara eksplisit.

Matriks wajib:

| ID | Status | File/fungsi/baris akhir | Check/input | Bukti/log dan snapshot | Batas/next_action |
|---|---|---|---|---|---|
| R01–R12 dan A01 | BELUM_DIEKSEKUSI / DIPERBAIKI_BELUM_DIUJI / DIPERBAIKI_DAN_DIUJI / PERLU_VERIFIKASI / MENUNGGU_KEPUTUSAN / TIDAK_TERBUKTI_DENGAN_ALASAN | Lokasi tepat | Runnable check | Hasil sebenarnya | Pekerjaan tersisa |

Sertakan file berubah dan alasan per ID, pemeriksaan scope setiap hunk, bukti persetujuan UI, command/exit code, hash save sebelum/sesudah dan pemisahan lingkungan pengujian. Jika source berubah dari snapshot audit, revalidasi dan koreksi temuan dengan bukti; jangan memaksakan bug yang sudah tidak ada.

Tidak boleh menyatakan 100% hanya karena source dikompilasi, test subset lulus, screenshot tersedia atau APK terbentuk. Selesai hanya untuk requirement yang mempunyai bukti sesuai; gate yang belum lolos harus tetap terlihat. Setelah penyerahan, laporkan dan berhenti sesuai lingkup.

## 8. Instruksi eksekusi singkat untuk pemilik

“Saya menunjuk `Work/Perbaikan-Audit-Ulang-2026-11-01/plan.md` sebagai satu plan aktif. Codex, kerjakan R01–R12 dan regresi A01 sesuai plan, hanya perubahan yang diperlukan. Pertahankan perubahan lokal dan save pemain, patuhi preview UI serta batas eksekusi, lalu laporkan bukti dan pekerjaan yang masih terbuka. Jangan meluncurkan agent lain, push, publish atau memasang aplikasi ke perangkat.”

Kalimat ini adalah rujukan mandat. Pemilik sudah memerintahkan eksekusi langsung; status aktual berada pada bagian 9 dan laporan progres.

## 9. Milestone berjalan — diperbarui saat bukti berubah

| Milestone | Status aktual | Bukti / pekerjaan berikutnya |
|---|---|---|
| P00 Baseline | SELESAI untuk persiapan host | Root/branch/HEAD serta baseline 220 file dicatat; save pemain hanya di-hash. Pengujian lokal terpisah diizinkan pemilik. |
| P01 Save dan pengunci transaksi | SOURCE DIPERBAIKI; HOST REGRESSION LULUS | Run current-regression2: 61 tests mencakup recovery, save, migration dan async; offline-recheck2: 41 tests pada snapshot masing-masing. Integrasi Android belum dibuktikan. |
| P02 Likuiditas/settlement | SOURCE DIPERBAIKI; HOST LULUS | Matriks besar 16 skenario IDX/US, MARKET/LIMIT, BUY/SELL 10k/50k serta reload sudah masuk regression. Indeks ledger permanen untuk R05 diuji pada final-targeted (43 tests lulus), termasuk >5.000 mutasi dan restore. |
| P03 MARKET dan sesi | HOST REGRESSION LULUS | Antrean pembukaan, cancel/reload, sesi lintas bursa dan offline player order diperiksa pada offline-recheck2; source produksi saat ini menunggu suite luas. |
| P04 Target berita | HOST REGRESSION LULUS | Agregasi, acuan episode, zero-net, arah negatif, artikel/deadline persisten dan compatibility legacy diperiksa. Jangan menganggap seluruh balancing/news readiness di luar plan telah selesai. |
| P05 UI | R09/R10 KOMPONEN HOST LULUS; NATIVE NOT_TESTED_BY_SCOPE | Pemilik menyetujui jam existing tanpa panel dan toast gagal pembatalan secara terpisah. app.js diubah. check-existing-clock.py lulus memakai fungsi produksi dengan worker/bridge sintetis: navigation/cache read, lock/failure/retry, reduced-motion, dan error toast DOM/fokus. Host Java/browser terpadu lulus 390/320px untuk catch-up empat hari game dan navigasi; belum Android/perangkat. |
| P06 Regresi/penyerahan | HOST/BUILD SELESAI; SIAP REVIEW | Full suite 98846 terminal: 725 tests, dua asumsi tes lama diperbaiki dan final-targeted 43 tests lulus. Run akhir session 97726 terminal: 726 tests PASS, 681,754 detik, termasuk 730 hari pada heap maksimum 384 MB. final-acceptance 45 PASS; Android build offline PASS. Lihat HASIL-VERIFIKASI.md dan batas pada MATRIKS-CAKUPAN.md; belum bukti perangkat. |

Izin pemilik pada sesi ini: pengujian lokal terpisah tanpa isolasi OS lengkap diizinkan; tidak mencakup perubahan global, instalasi perangkat, akses save pemain atau publikasi. Kompilasi memakai runner existing, output/save QA di Artifacts/Codex-Audit-Ulang-2026-11-01 dan direktori test-run miliknya. Run awal menemukan assertion tes yang memasukkan waktu presentasi monotonicAnchor dan rotasi kedua yang memang valid; tes diperbaiki untuk memeriksa invariant yang relevan. Satu compile gagal karena JSONException runtime pada JSON host; catch diperbaiki agar kompatibel dengan tipe exception, lalu 27 tes lulus.

### Keputusan tambahan pemilik saat P04

- Jika efek berita emiten sama bertumpuk lintas hari, gunakan harga acuan berita pertama selama rangkaian efek itu tetap aktif. Contoh +5% dari 10.000 lalu -3% ketika acuan hari baru 10.500 menghasilkan target +2% dari 10.000. Acuan episode harus bertahan save/reload dan tidak berganti hanya karena efek pertama berakhir sementara efek penerus masih aktif.
- Jika jumlah efek tepat nol, nonaktifkan dorongan target dan biarkan harga mengikuti pasar biasa; jangan memaksa harga kembali ke acuan.

### Checkpoint aktif dan cara melanjutkan

Pembaruan 2 November 2026: status handle lama pada daftar riwayat di bawah sudah digantikan oleh checkpoint terbaru `LAPORAN-PROGRES.md`. Full suite awal 3199 terminal (718 tests, 15 failures); run regression terbaru lulus 61 tests dan run offline-recheck2 lulus 41 tests pada snapshot masing-masing. Full suite source terkini sedang berjalan pada session **98846** (`Artifacts/Codex-Audit-Ulang-2026-11-01/full-suite-current`). Preview baru **jam/tanggal existing tanpa panel tambahan** sudah dibuat dan diperiksa pada 320/390/736px, reduced-motion dan teks besar; file `preview-jam-existing-mengejar.html` di direktori visualisasi chat. R09/R10 telah disetujui dan diterapkan; bukti host/browser serta regression toast dicatat pada checkpoint terbaru LAPORAN-PROGRES.md. Jangan menghitung run tumpang tindih sebagai jumlah test unik atau menandai seluruh plan selesai.

- Handle full-suite: unified exec session `3199`; proses Java yang diverifikasi hidup `12704`; runner sudah mengompilasi source dan tes pada direktori `Artifacts/Codex-Audit-Ulang-2026-11-01/full-suite`. Jangan restart hanya karena polling timeout. Poll handle yang sama dan baca hasil; jika terminal, catat exit code serta failures.
- Diagnostik read-only thread JVM menunjukkan RUNNABLE pada SaveManager.readValidatedState saat MarketMatchingOrderHaltCriticalSequenceTest seed 01, bukan bukti deadlock. CPU dan trace menunjukkan stress sedang bekerja. Batas heap runner -Xmx384m; working set bukan angka heap dan tidak boleh disamakan.
- Setelah run selesai, jalankan regression terbaru untuk legacy-effect dan backend reconciliation yang diedit setelah snapshot run, lalu perbaiki failure berbasis diagnosis. Jangan menyebut full suite lulus sebelum exit code/output tersedia.
- Preview interaktif: `C:/Users/HYPE AMD/.codex/visualizations/2026/11/01/01a1964c-3cf3-7302-9ed3-74392fa65ea2/preview-remediasi-save-dan-sinkronisasi.html`. Bukti checks: `Artifacts/Codex-Audit-Ulang-2026-11-01/ui-preview/check.txt`. Ini contoh data simulasi, bukan runtime game.
- next_action UI: siapkan preview jam/tanggal existing yang bergerak cepat sesuai progres domain tanpa komponen tunggu tambahan; minta persetujuan terkait. R10 tetap terpisah. Lanjutkan verifikasi backend independen. Setelah persetujuan, terapkan alur UI produksi dan uji integrasi/cached-data navigation, fokus, error, retry, reduced-motion dan 200% teks.

### Pembaruan eksekusi P05 — 2 November 2026

Pemilik telah menyetujui preview jam existing dan meminta penerapan; kemudian secara terpisah menyetujui perbaikan pesan gagal pembatalan langganan. Kedua approval berlaku untuk komponen/alur terkait, bukan semua layar. Panel tambahan dihapus dari requestClockSync; presentasi jam/tanggal mengikuti snapshot progres yang sudah diproses. Cache baca bridge menghindari native getter yang menunggu monitor panjang selama sync, sedangkan command finansial diblokir. Tidak ada autoplay tunggu palsu; cache presentasi disimpan paling sering 30 detik, bukan setiap heartbeat. R10 memakai toast existing sesudah flow konfirmasi dilepas. Detail bukti dan gap ada di MATRIKS-CAKUPAN.md.

R05 dependency: cap indeks processedRequests 5.000 dapat menggagalkan idempotensi event modal eksternal. Diubah menjadi indeks seluruh ledger (HashMap existing stdlib); regression baru memeriksa retry sesudah 5.001 mutasi dan restore. Perubahan ini menutup requirement R05 tepat-sekali, bukan izin mengerjakan temuan lain di luar plan. Regresi >5.000 mutasi dan restore lulus pada full suite dan completion-final.

### Penutupan verifikasi lokal — 2 November 2026

Full regression 726 PASS; acceptance terbaru 51/25/26 PASS pada snapshot masing-masing (overlap). Java pada completion-final identik dengan source akhir, APK memuat UI produksi aktual. R09 diperiksa pada tujuh viewport × dua skala; R10 42 kasus toast. Matriks wajib bagian 7 sudah diisi dengan lokasi baris akhir dan batas. Lima backlog di luar mandat dicatat; native NOT_TESTED_BY_SCOPE, bukan PASS. Lihat HASIL-VERIFIKASI.md dan LAPORAN-PROGRES.md untuk metrik, command/exit code dan bukti. next_action: review penyerahan; berhenti setelah penyerahan sesuai aturan scope, jangan memulai backlog tanpa plan pemilik.
