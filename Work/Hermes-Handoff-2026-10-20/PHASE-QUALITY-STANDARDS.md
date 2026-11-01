# Standar minimum tinggi dan gerbang kelulusan setiap fase

## Delta kontrak pemilik 24 Oktober 2026

Untuk mandat `master plan perbaikan.md`, tempo aktif/inactive menjadi tunggal 24x (1 hari game = 1 jam nyata), tanpa kontrol pause/speed/skip pemain. Berita memakai deadline 60/120 menit nyata. UI mengikuti contoh revisi 2 yang disetujui: background gelap dan panel terang, tab lama tetap; lab/uji stres dihapus dari UI dengan kompatibilitas data/shared domain tetap dilindungi. Pernyataan dual-rate/Skip dan kewajiban launcher eksperimen historis di bawah digantikan hanya pada kontrak tersebut, bukan dihapus dari sejarah. Seluruh standar integritas, 10x100 aksi bermakna, viewport/200%, performa, bukti dan nol bug terbuka tetap berlaku. Tidak ADB/instalasi/perangkat baru; native NOT_TESTED_BY_SCOPE. Review mandiri terpisah, bukan independent-agent review karena delegasi tidak diizinkan.

Keputusan pemilik, 20 Oktober 2026. Seluruh lima jawaban quality gate telah dikonfirmasi. Dokumen ini wajib dibaca bersama prompt master dan TASK.md masing-masing fase. Standar berikut menambah acceptance, tidak menggantikan atau mengurangi 228 requirement, fitur referensi, maupun keputusan produk sebelumnya.

**Status saat dokumen disiapkan: spesifikasi; belum ada fase yang diluluskan oleh dokumen ini.** Target penyerahan tetap host/domain/browser/APK. Instalasi serta pembuktian runtime Android ditangguhkan, dicatat NOT_TESTED, dan tidak dianggap bug yang boleh disembunyikan sebagai PASS.

## A. Standar bersama yang mengikat semua fase

### A1. Seluruh kriteria wajib lulus

- Tetapkan cakupan, dependensi, input/output, acceptance dan metode pembuktian sebelum fase dikerjakan.
- Petakan 100% requirement fase ke implementasi serta bukti yang tepat. Tidak menggunakan rata-rata nilai untuk menutup satu kegagalan wajib.
- PASS membutuhkan tes/inspeksi terhadap source terintegrasi yang sama dengan revision/hash bukti. Stub, enum, folder, screenshot atau build saja tidak meluluskan perilaku end-to-end.
- FAIL, NOT_TESTED, bukti kedaluwarsa atau bukti tidak sesuai scope berarti gerbang belum lulus. Tidak mengubah requirement menjadi opsional agar mendapat PASS.
- Kriteria benar-benar tidak berlaku boleh diberi N/A dengan alasan konkret yang direview koordinator. N/A tidak boleh dipakai untuk fitur yang belum dibuat, alat yang belum tersedia, tes yang gagal, atau target yang sulit. Native ditangguhkan secara eksplisit pada scope, bukan dipalsukan sebagai N/A/PASS.

### A2. Nol bug terbuka yang diketahui

- Sebelum fase lulus, **0 bug kritis, 0 bug mayor dan 0 bug minor yang diketahui** dalam cakupan fase beserta dependensi yang diperlukan.
- Setiap temuan harus diperbaiki dan diuji ulang. Jangan menurunkan severity, mengganti nama bug menjadi enhancement, menghapus reproduksi atau memindahkannya ke backlog hanya untuk meluluskan fase.
- Bila sebuah temuan ternyata bukan bug, simpan alasan, kontrak/requirement yang mendasari, bukti dan persetujuan reviewer. Mengganti label tanpa pemeriksaan tidak menutup temuan.
- Fitur fase mendatang yang memang belum dijadwalkan tidak otomatis menjadi bug fase saat ini; tetap tercatat pending dan tidak boleh diklaim sudah berfungsi. Bug pada dependency yang membuat hasil fase tidak sah tetap memblokir kelulusan.
- Warning compiler/lint bukan otomatis bug, tetapi wajib ditriage. Warning yang mengungkap cacat nyata harus diperbaiki; jangan menambah suppression tanpa alasan dan pemeriksaan dampaknya.
- Nol bug diketahui bukan janji bahwa tidak mungkin ada bug tersembunyi. Laporan harus menyebut apa yang diperiksa dan batas bukti.

### A3. Minimal 10 urutan × 100 aksi untuk logika kritis

Berlaku pada fase yang mengubah atau mengintegrasikan uang, order, waktu atau save, termasuk mutasi melalui bridge/UI:

- Minimal **10 variasi urutan yang dapat direproduksi, masing-masing sekurangnya 100 aksi bermakna**. Catat seed, input, urutan, hasil dan source revision.
- Periksa invariant setelah **setiap aksi**: kas/reservasi/posisi/utang/ledger, event idempotency, clock/anchor, serta state/save yang relevan.
- Campur operasi sah, boundary serta kegagalan yang diharapkan sesuai cakupan. Read-only/no-op berulang dan mengganti seed tanpa mengubah urutan tidak boleh dipakai untuk memenuhi jumlah.
- Setiap keluarga perilaku yang disentuh harus terwakili. Contoh trading mencakup fill parsial/cancel/retry, bukan 100 submit identik; save mencakup load/restart/recovery, bukan 100 pembacaan JSON yang sama.
- Tes batas, proses terputus, save/write gagal dan regression kasus wajib tetap harus ada di luar atau sebagai bagian urutan dengan cakupan yang jelas; angka 1.000 aksi tidak menggantikan kasus tersebut.
- Gunakan runner/domain existing. Tidak wajib menulis 1.000 test method atau framework baru. Satu runner terparameterisasi dengan assertion yang tepat dapat mencakup urutan tersebut.
- Setelah integrasi yang mengubah hasil, jalankan ulang suite terdampak pada source baru. Hasil lama boleh dirujuk untuk bagian yang terbukti tidak berubah, tetapi bukan pengganti pengujian integrasi akhir.
- Kegagalan terencana lulus hanya bila error yang benar dan state konsisten teramati; exception tak terduga, invariant pecah atau silent reset adalah FAIL.

### A4. Semua layar dan state pada semua viewport

Untuk setiap fase yang mempunyai UI, inventarisasi seluruh layar/state yang benar-benar berlaku, termasuk default, empty, loading, selected, locked, error, stale/recovery, sukses, form invalid dan konfirmasi bila relevan.

Uji setiap pasangan layar/state tersebut pada viewport: **320×568, 360×640, 390×844, 412×915, 430×932, 768×1024 dan 1440×900**, masing-masing dengan teks normal serta pembesaran teks **200%**. Penyesuaian metode font-scale browser harus nyata dan dicatat; screenshot diperbesar saja bukan pengujian teks 200%.

Kelulusan membutuhkan:

- Tidak ada teks/angka/form terpotong, horizontal overflow tak sengaja, tumpang tindih atau CTA tertutup nav.
- Konten scroll dan root nav tetap, isi terakhir dapat dicapai, detail/form/modal mempunyai fokus serta back/close yang benar.
- Seluruh tombol/interaksi yang ditampilkan bekerja atau mempunyai disabled/locked state yang jelas, bukan tombol mati.
- Angka dan chart sesuai snapshot/history domain; perubahan data tidak menghilangkan input, pilihan atau fokus.
- Token, jarak, hierarki, card dan navigasi mengikuti referensi HP serta keputusan lima tab. Perbedaan konten game diperbolehkan; perbedaan besar layout/interaksi harus diselesaikan.
- Reviewer membuka dan memeriksa hasil screenshot pada matriks yang dinyatakan diperiksa. Tidak menandai seluruh kombinasi PASS hanya karena lima root terlihat baik.
- Status visual memakai teks/ikon, kontras dan reduced motion diperiksa, serta label/fokus dapat digunakan melalui keyboard pada browser.

Automation layout/assertions dapat mempercepat pemeriksaan, tetapi tidak menggantikan inspeksi visual. Komponen shared boleh menggunakan pemeriksaan yang direuse dengan bukti konteks setiap layar; jangan menghapus variasi layar yang nyata.

### A5. Ambang performa browser/host lokal

Keputusan pemilik:

- **P95 aksi biasa ≤300 ms.**
- **Pergantian halaman ≤1.000 ms.**
- **Pekerjaan berat menampilkan indikator/umpan balik ≤300 ms dan tidak membekukan UI.**

Metode pengukuran operasional:

1. Tentukan daftar aksi biasa dan navigasi relevan sebelum benchmark; jangan mengeluarkan aksi yang lambat setelah melihat hasil.
2. Ambil minimal 30 sampel per aksi/navigasi yang diuji, catat environment, browser, viewport, dataset/save, seed dan kondisi cache. Pisahkan startup/initial load dari navigasi di dalam aplikasi; jangan mengklaim cold startup Android dari hasil ini.
3. Ukur dari input pengguna sampai hasil yang benar terlihat/tergambar dan state terkait telah konsisten, bukan sekadar handler dimulai atau spinner muncul. Untuk submit, penerimaan command dan final settlement boleh berbeda bila statusnya jelas; jangan memakai pending sebagai sukses transaksi.
4. Hitung P95 per jenis aksi dengan nearest-rank pada sampel terurut: indeks ceil(0,95 × n). Jangan menggabungkan banyak aksi cepat agar satu aksi lambat tersembunyi. Seluruh sampel navigasi yang diwajibkan harus ≤1.000 ms; tampilkan nilai maksimum serta distribusinya.
5. Pekerjaan berat diuji terpisah: indikator harus benar-benar sempat tergambar ≤300 ms, event loop tetap bergerak, dan interaksi nonfinansial yang aman tetap responsif. Kontrol finansial boleh dikunci saat state belum konsisten. Spinner statis sebelum synchronous blocking lama tidak lulus.
6. Simpan raw timing, hasil per aksi dan metode. Error/timeout bukan sampel cepat yang boleh dihitung sebagai sukses. Jangan membuang outlier diam-diam atau mengulang sampai kebetulan hijau.
7. Jika hasil di bawah standar, diagnosis bottleneck, perbaiki jalur minimum, lalu ukur ulang skenario yang sama beserta regresi terdampak. Ambang tidak boleh dilonggarkan tanpa keputusan pemilik baru.

Ini target host/browser lokal, bukan janji performa HP. Performa native tetap membutuhkan lingkungan Android dan bukti tersendiri. Bila lingkungan lokal rusak/tidak siap, catat blocker dan lanjutkan tugas independen; jangan memberi PASS asumsi.

### A6. Review terpisah pada setiap fase

- Setiap fase direview agen berbeda dari implementer bila kemampuan multi-agent tersedia, bukan hanya fase keuangan.
- Reviewer memeriksa kontrak, diff, tes, bukti, screenshot/performa yang relevan dan temuan sebelumnya. Laporan author tidak otomatis diterima.
- Seluruh temuan yang valid ditutup melalui perbaikan dan uji ulang. Koordinator memverifikasi hasil setelah integrasi dan memutuskan kelulusan.
- Jika slot terbatas, review dijalankan bergiliran. Jika sub-agent benar-benar tidak tersedia, gunakan pass review terpisah, simpan bukti keterbatasan kemampuan dan nyatakan bahwa itu bukan independent-agent review.
- Bahan awal, template atau validator material tidak pernah meluluskan review implementasi.

### A7. Aturan pengulangan dan dependency

Jika satu kriteria wajib belum lulus, tandai gate FAIL/NOT_TESTED/BLOCKED sesuai bukti, simpan reproduksi, perbaiki akar masalah, uji ulang kasus gagal dan regresi terdampak, lalu review kembali. Tidak meneruskan pekerjaan yang bergantung pada hasil gagal seolah dependency sudah lulus. Bagian independen tetap boleh dikerjakan atau prototipe kontrak berlabel boleh dipakai; integrasi final tetap menunggu dependency nyata.

Setelah dua kegagalan sama tanpa diagnosis/bukti baru, kirim PERLU PERHATIAN dan ubah pendekatan. Setelah tiga, pindah pekerjaan independen atau catat BLOCKED bila membutuhkan faktor luar. Ini tidak mengizinkan menurunkan standar atau menandai fase selesai. Tidak ada batas iterasi buatan untuk menghentikan perbaikan yang masih menghasilkan kemajuan.

## Phase 00 — Preflight, scope, baseline dan perlindungan progres

- Root/Git/branch/HEAD serta perubahan lokal tercatat; seluruh keputusan terakhir dan 228+26+acceptance pemilik dipetakan.
- Java/WebView existing diverifikasi; tidak mengulang feasibility engine lama atau mengimpor source/save repo lain.
- Toolchain dan command baseline nyata dicatat, beserta exit code/XML hasil; baseline gagal dicatat sebagai temuan, bukan ditimpa log lama.
- Lokasi save pribadi/QA, backup dan prosedur pemulihan pada salinan jelas. Save pribadi tidak direset.
- Kemampuan multi-agent, ownership dan dependensi terverifikasi; review terpisah pada hasil preflight.
- Tiap blocker data/perangkat dinyatakan dengan scope yang tepat. Fase ini tidak meluluskan fitur ekonomi/UI yang belum diimplementasikan.

## Phase 01 — Akun, ledger, clock dual-rate dan save

- Satu domain Java menguasai akun/ledger/clock/save; validasi input, presisi, rounding, isolasi akun dan invariant dasar terbukti.
- Foreground 300 detik nyata/hari game pada 1×, background 3.600 detik/hari game, tanpa pewarisan speed; seluruh TIME-ACCEPTANCE dan kasus lifecycle/fault relevan lulus.
- Anchor/fraksi/idempotency, proses mati tanpa callback, write failure, migrasi legacy dan recovery terbukti; tidak ada charge/delta ganda atau silent reset.
- Minimal 10×100 aksi dengan invariant per langkah untuk jalur clock/akun/save. Review agen berbeda dan nol bug terbuka.
- Jika mempunyai UI/status/error, A4/A5 berlaku pada cakupannya; gate ini tidak memberi PASS native lifecycle tanpa Android.

## Phase 02 — Katalog, indeks, kontrak dan sumber data

- Universe dan metadata instrumen Indonesia/AS memenuhi PRD; simbol, unit, currency, timezone, sumber dan active/unavailable status konsisten.
- Anggota/metode/basis indeks dapat direkonsiliasi; bukan angka dekoratif atau klaim feed resmi yang tidak dimiliki.
- Validasi dataset menolak bar ilegal/duplikat/urut salah dan menjelaskan raw/adjusted/missing data.
- Provenance, sumber primer dan hak penggunaan/redistribusi yang diperlukan diperiksa dan terdokumentasi. Gratis akses bukan bukti lisensi.
- Jika dataset wajib belum sah/tersedia, bagian terkait tetap BLOCKED dan tidak boleh diluluskan untuk mengamankan angka progres; tugas independen dapat dilanjutkan.
- Search/filter/watchlist/indeks yang termasuk UI fase memenuhi A4/A5; persistence/ekonomi yang disentuh mengikuti A3.

## Phase 03 — NPC, matching, order book, sesi dan halt

- Market/limit, partial fill, price-time/aturan antrean yang dipilih, reservasi, cancel, trigger/expiry dan retry teruji pada order/ledger yang sama.
- NPC membuat likuiditas/pergerakan melalui model domain yang terdokumentasi, bukan kalkulator UI; seed/state dapat direproduksi.
- Seluruh execution path mematuhi bursa/session/weekend/libur/halt/reopen dan kalender lintas wilayah.
- Minimal 10×100 aksi dengan kombinasi fill/cancel/retry/halt/sesi, invariant setelah tiap aksi dan fault yang relevan.
- Tidak ada order/fill ganda, harga/quantity tidak valid atau dana tertahan hilang; review independen menutup seluruh temuan.

## Phase 04 — UI portrait, shell, navigasi dan host

- Lima root pilihan pemilik, layout referensi, token/font lokal, wealth header, scroll konten dan nav tetap konsisten.
- Inventaris semua route/state fase; seluruh kombinasi viewport dan teks normal/200% lulus A4, screenshot diperiksa.
- CTA/state, focus modal/form, reduced motion, loading/error/stale/recovery dan navigasi diuji; tidak ada tombol mati atau copy implementasi mengganggu pemain.
- P95 aksi, navigasi dan feedback heavy work lulus A5 pada host lokal; semua bug visual/interaksi minor yang ditemukan ditutup.
- Host/source Android dibangun dan bridge contract diperiksa; runtime Android tidak diklaim lulus melalui browser. Jalur finansial/save yang diintegrasikan mengikuti A3.

## Phase 05 — Trading, portfolio, ledger dan FX

- Journey preview → review → submit → receipt/status, partial/cancel/retry, stale quote dan akun benar lulus dengan domain produksi.
- Kas/reservasi/posisi/cost basis/P&L/fees/liabilities/net worth, header dan laporan direkonsiliasi pada timestamp/kurs yang sama.
- FX mempunyai quote expiry/spread/fee/rounding dan tidak menimbulkan free-money loop atau duplikasi konfirmasi.
- Minimal 10×100 aksi transaksi/FX/save, boundary/insufficient funds serta restart/write failure relevan.
- Seluruh UI/state fase lulus A4/A5; review terpisah menutup semua temuan.

## Phase 06 — Waktu dunia, karier, misi, XP dan recovery

- Normal advance, Skip dan offline setara pada game time/seed/input yang sama, termasuk kalender, fee, laporan dan event tepat sekali.
- Skip memberi peringatan sebelum konfirmasi, menambah tepat 24 jam game dan memberikan receipt/laporan tanpa hitung ulang elapsed.
- Tutorial/misi/promosi ditempuh melalui aksi sah; reward/reset periodik idempotent; akses domain mengikuti karier, bukan XP saja.
- Pemulihan kas nol/utang melalui kerja earned dan restrukturisasi terbukti, tanpa subsidi otomatis, game over atau prestige wajib.
- Minimal 10×100 aksi serta interval offline panjang, rollover misi/liga, crash/reload; UI dan performa memenuhi A4/A5.

## Phase 07 — Firma, klien, perusahaan, pegawai dan otomasi

- Akun pribadi/klien/firma/perusahaan terisolasi; mandat, trust, deposit/withdrawal, fee/HWM serta laporan periode benar dan idempotent.
- Perusahaan operasional, CEO/pegawai/departemen, payroll, happiness/jam kerja/benefit/produktivitas benar-benar memengaruhi ledger dan state.
- Operasional otomatis mematuhi kebijakan pemain saat aktif/offline; keputusan besar manual dan default darurat jelas.
- Minimal 10×100 aksi mencakup investasi/transfer, biaya/fee/payroll dan save/reload; tidak menganggap AUM sebagai wealth pribadi.
- Journey earned klien/bisnis dan seluruh layar/state memenuhi A4/A5; review berbeda menutup seluruh temuan.

## Phase 08 — Berita, riset, terminal dan entitlement

- Artikel/berita/analis/konsensus/alt-data/kalender/inbox/perbandingan yang diwajibkan mempunyai data, release time dan alur nyata.
- Membuka menu tidak otomatis memberi XP seluruh artikel; baca ulang/retry idempotent dan tidak ada future leakage.
- Paket uang game mempunyai charge/expiry/renewal serta manfaat akses/data yang benar, bukan label premium kosmetik.
- Terminal default ringkas, expandable details stabil; seluruh state/viewport dan A5 lulus.
- Jalur billing/waktu/save mengikuti minimal 10×100 aksi relevan; review terpisah menutup semua temuan.

## Phase 09 — Multi-aset, unit fund, fund builder dan koleksi

- Obligasi/komoditas/FX/saham mengikuti unit/valuasi/kupon/maturity/settlement dan pembukuan yang dipilih.
- Unit reksa dana/NAV/fee/pembelian/penjualan berbeda dari rencana fund builder; alokasi tidak menciptakan posisi fiktif.
- Rebalance nyata menangani partial fill, batas mandat dan sisa dana; koleksi mempunyai kepemilikan/beli/jual/valuasi serta biaya/manfaat.
- Minimal 10×100 aksi lintas produk, kalender dan save, dengan rekonsiliasi serta fault yang relevan.
- Seluruh layar/state serta A5 lulus; reviewer berbeda menutup semua bug.

## Phase 10 — Corporate action, IPO, M&A dan voting

- Dividen/split/reverse split/rights/buyback/merger mempunyai jadwal, eligibility, fraksi/rounding dan efek posisi/order/kas yang benar.
- IPO/M&A menangani reserve/allocation/payment/refund/cancel/deadline tanpa dana hilang atau duplikasi.
- Voting menegakkan hak/jumlah/deadline/hasil dan efek tepat sekali; aksi game tidak diklaim fakta historis.
- Minimal 10×100 aksi termasuk offline/Skip/reload melintasi event, serta failure/retry.
- Seluruh UI/status/error/receipt lulus A4/A5 dan review independen menutup semua temuan.

## Phase 11 — Risiko, loan, margin, short dan compliance

- Formula health/exposure/drawdown/margin dijelaskan dan direkonsiliasi; loan produk dan margin tidak disamakan tanpa mekanik.
- Bunga/collateral/repayment/borrow/recall/liquidation/short squeeze/limit mandat ditegakkan domain pada semua jalur.
- Minimal 10×100 aksi mencakup gap harga, sesi/halt, insufficient funds, offline lama dan save/recovery.
- Tidak ada liabilitas hilang, free-money loop, kas pinjaman dianggap profit, atau akses akun tak sah. Jalur pemulihan tetap dapat dimainkan.
- Warning/risiko/review/UI dan A5 lulus, nol bug terbuka setelah review terpisah.

## Phase 12 — Replay nyata, lab, stress dan progres panjang

- Dataset nyata dan haknya sah, manifest/cakupan/checksum jelas; journey pilihan periode → chart/trade → laporan → save/replay deterministik lulus.
- Anti future leakage, raw/adjusted, intrabar, mode/state/anchor terisolasi dan akhir periode bukan tamat game.
- Lab/stress/what-if tidak memutasi uang/XP/save utama; asumsi dan hasil dapat diperiksa.
- Minimal 10×100 aksi replay/lab/time/save dengan fault, urutan normal/Skip/offline dan interval panjang; RNG/event hasil konsisten menurut kontrak.
- Semua UI/state dan A5 lulus; legal/data blocker tidak boleh dilewati. Review berbeda menutup semua temuan.

## Phase 13 — Integrasi final, performa dan APK

- Seluruh gate fase pada scope handoff serta requirement wajib lengkap; integrasi final tidak memakai PASS sebelum perubahan terakhir.
- Jalankan suite relevan/domain/bridge/browser dan minimal 10×100 aksi integrasi lintas fitur pada source final, plus seluruh regression/fault wajib. Hitungan aksi tidak menggantikan earned journey.
- Seluruh layar/state/viewport/teks 200%, p95/navigasi/feedback heavy work dan review visual final terbukti. Nol bug terbuka yang diketahui termasuk minor.
- Reviewer berbeda memeriksa hasil final bila tersedia; koordinator menutup temuan, memastikan save pribadi terjaga dan menyetujui gate.
- APK dibangun dari source final teruji, app ID/version/signature/asset identity/hash/ukuran diverifikasi; debug disebut debug.
- Laporan/manifest evidence/panduan/recovery tersedia. Native/install/device tetap NOT_TESTED jika ditangguhkan; tidak menyebut seluruh acceptance Android selesai.

## B. Laporan gerbang fase yang wajib tersedia

Gunakan checkpoint existing dan format minimum berikut; boleh JSON/Markdown/CSV, tanpa membuat framework status baru:

- phase_id, scope, input/dependency dan seluruh ID requirement.
- revision/hash source terintegrasi, environment dan waktu pemeriksaan.
- Setiap criterion: expected, observed, PASS/FAIL/NOT_TESTED/BLOCKED, command/metode dan path evidence.
- Jumlah temuan terbuka kritis/mayor/minor, beserta bug IDs dan bukti penutupan/retest.
- Critical sequence: seed/variasi, jumlah aksi bermakna, assertions/invariant per langkah, failure dan reproduksi.
- UI: daftar layar/state × viewport × text-scale, screenshot serta hasil review, bukan satu checkbox umum.
- Performa: daftar aksi, raw samples, p95/max navigation/feedback dan responsivitas heavy work.
- Author, reviewer, hasil review, tindak lanjut dan verifikasi koordinator; fallback review diberi alasan nyata.
- gate_result, batas scope native, blocker dan next_action.

Runner pemeriksaan fase harus keluar nonzero ketika assertion wajib gagal. Jika reuse beberapa runner, koordinator memeriksa exit code masing-masing serta output aktual. Dokumen atau report JSON dengan field PASS tidak otomatis menjadi bukti bahwa proses tersebut dijalankan.

Tidak ada scoring 90%/95% yang meluluskan kriteria wajib gagal. Angka 95% di kontrak performa hanya berarti persentil latensi, bukan izin 5% fitur rusak. Gate PASS membutuhkan seluruh kriteria wajib yang berlaku lulus dan tidak ada bug terbuka yang diketahui pada cakupannya.
