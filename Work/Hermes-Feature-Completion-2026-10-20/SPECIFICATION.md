# Spesifikasi penuntasan fitur dan integrasi

Versi 20 Oktober 2026. Seluruh angka/aturan yang diberi Dxx adalah **rancangan game kita yang diotorisasi melalui delegasi keputusan pemilik**, bukan rumus hasil dekompilasi aplikasi referensi. Ini dokumen implementasi yang harus diuji, bukan laporan kelulusan.

## 1. Cakupan, sumber dan prioritas otoritas

Urutan otoritas: jawaban langsung pemilik terbaru → paket ini untuk keputusan tambahannya → prompt master dan OWNER-DECISIONS existing → PRD/design brief/TASK → laporan historis. Bagian lama yang menyebut C#/MAUI, 3D/landscape, tidak ada source, larangan leaderboard, offline auto-pause, atau lima tab lama sudah digantikan keputusan pemilik.

Seluruh 228 requirement PRD, 26 kelompok referensi, dan 23 acceptance pemilik existing tetap diperiksa. Delta di GAP-MATRIX menambah rincian dan fitur baru. Satu baris kelompok bukan pengganti seluruh subrequirement.

Bukti referensi dibedakan:

- **VISUAL:** PNG HP membuktikan isi yang terlihat, bukan transaksi/rumus sudah diuji. Lihat indeks PHONE-UI-REFERENCE; 10-missions.png adalah Beranda bawah, 11-missions-view.png benar-benar halaman misi.
- **STATIC:** URI/literal/model hasil REA membuktikan struktur atau nama. Bukan source Dart/server atau formula yang berhasil dipulihkan.
- **PRODUCTION:** source Java/bridge/UI membuktikan implementasi tertentu. Keberadaan metode belum membuktikan dapat dijangkau pemain.
- **RUNTIME:** tes produksi dengan expected/actual, source revision, seed, dan save terpisah diperlukan untuk PASS.
- **DESIGN Dxx:** pilihan aturan versi kita; dapat dikalibrasi Hermes melalui bukti balancing dan dicatat versinya, tanpa mengurangi fitur/standar pemilik.

Tidak ada inspeksi HP/ADB baru pada penyusunan atau pelaksanaan paket ini. Jangan menyalin ilustrasi, backend, akun, kredensial, atau save referensi ke produk.

## 2. Kontrak yang berlaku pada SEMUA fitur

### 2.1 Otoritas dan konsistensi

Java adalah satu pemilik akun, ledger, order, market/NPC, waktu, progres, statistik, dan save. UI hanya input/presentasi. Reuse fungsi/runner existing sebelum menulis fungsi baru. Telusuri semua caller fungsi bersama sebelum perbaikan. Tidak membangun framework, mesin ekonomi JS, clock kedua, atau dependency spekulatif.

Satu snapshot konsisten menyertakan `schemaVersion`, `rulesVersion`, `runId`, `mode`, `snapshotVersion`, `gameTime`, `timezone`, dan data yang relevan. Uang/kuantitas presisi dikirim sebagai decimal string dengan currency/unit terpisah; jangan menghitung saldo menggunakan floating point JS. Persentase selalu jelas sebagai rasio atau persen. Nominal display mempunyai raw value yang dapat direkonsiliasi.

Kontrak baru berikut adalah **usulan bentuk kontrak**, bukan pernyataan metode sudah ada. Bila metode existing setara, perluas/reuse tanpa menduplikasi API. Query tidak memutasi ekonomi. Mutasi menerima `requestId`, `runId`, akun/entitas pemilik, input tervalidasi, serta `expectedVersion`/quote bila relevan. Respons mempunyai `success`, `status`, `code`, `message`, `receiptId`, `snapshotVersion`, dan field domain. Bedakan ACCEPTED/PENDING dari SETTLED/FAILED/CANCELLED/PARTIAL. Tidak mengandalkan menonaktifkan tombol UI sebagai satu-satunya idempotency.

Preview harus non-mutating, kedaluwarsa dan mempunyai rincian biaya/risiko. Commit memvalidasi ulang harga/syarat/akses/kas/reservasi/versi. Stale quote → jelaskan perubahan dan minta review ulang; jangan mengubah transaksi diam-diam. Duplicate request mengembalikan receipt sama tanpa mutasi kedua; request ID sama dengan payload berbeda ditolak. Semua entry point menegakkan karier, kepemilikan dan isolasi akun.

Mutasi ekonomi, event receipt, cursor/clock anchor, serta save berkomit secara konsisten. Setelah write gagal/crash/retry, tidak ada setengah transfer, unit/utang/asset yatim, charge ganda, atau silent reset. Gunakan backup/migrasi versi. Jangan menguji destruktif pada save pribadi.

### 2.2 Kalender dan event

Foreground 1×: 86.400 detik game/300 detik nyata = 288×; opsi 2×/5× hanya foreground. Background/ditutup: 86.400/3.600 = 24×, tidak mewarisi speed. Contoh delapan jam tidak aktif = delapan hari game. Tidak ada pemotongan catch-up tersembunyi.

**D01:** kalender periode pemain memakai Asia/Jakarta: hari berganti 00:00, minggu mulai Senin 00:00. Ini kalender misi/liga; bursa/instrumen tetap memakai timezone dan holiday/session masing-masing. Pemrosesan pada timestamp sama mempunyai urutan deterministik yang didokumentasikan: mutasi pasar/event jatuh tempo → akrual dan kewajiban → snapshot akhir periode → evaluasi misi/statistik/liga → buka periode berikut. Event harus mempertahankan eligibility dan urutan intraday; jangan menumpuk seluruh operasi di tengah malam jika kontraknya intraday.

Normal, Skip tepat 24 jam, dan catch-up memakai pipeline yang sama. Split langkah pada market open/close, auction/halt, NAV cutoff, jatuh tempo, payroll, corporate action, expiry, pergantian hari/minggu, serta batas relevan lain. Identitas event misalnya `(runId,entityId,eventType,periodId)` persisten. Batas interval [awal,akhir) atau padanannya harus konsisten agar tepat sekali.

RNG dan proses NPC harus bergantung tick/event kanonis serta seed tersimpan, bukan jumlah callback atau chunk yang dikirim browser. Proses panjang boleh dipartisi/checkpoint, tetapi seluruh interval diproses, event tidak hilang, progres/feedback tersedia, dan resume tidak mengulang committed segment. Jam sistem mundur/maju/reboot serta process death tanpa callback mengikuti kontrak waktu master dan diuji; jangan mengklaim jam perangkat offline anti-cheat tepercaya.

### 2.3 Pola layar, state dan aksesibilitas

Portrait; konten scroll; root navigation tetap Beranda/Pasar/Portofolio/Bisnis/Lainnya. Header ringkas dengan kekayaan kanan atas. Graphite/amber/teal dan font/token shared existing. Ikuti hierarchy/spacing referensi tanpa menyalin asetnya atau membuat tema baru per halaman. Detail bertingkat, bukan paragraf implementasi panjang. Jangan menampilkan kode FR/OQ atau nama metode pada UI pemain.

Setiap route di bagian 4–29 wajib menginventaris state: loading, default, empty, selected/filter, locked karena karier, invalid form, insufficient funds, stale quote/version, submitting, accepted/pending, settled/partial, error/recovery, archived/expired, dan offline-catch-up bila berlaku. Yang tidak berlaku diberi alasan konkret, bukan karena implementasinya belum dibuat. Selected/locked/disabled disertai label/ikon, kontras cukup; disabled tidak dapat mengeksekusi command.

Setiap kartu angka menunjukkan konteks akun/currency/periode. Satu CTA utama per langkah; tindakan sekunder berbeda bobot. Tekan/transition singkat mengikuti token, reduced motion dihormati, tanpa flash harga/gerak terus-menerus. Form tidak kehilangan draft/fokus saat snapshot diperbarui. Kembali/close mempertahankan state yang aman, memperingatkan draft berisiko dan mengembalikan fokus. Review transaksi boleh terisolasi sementara, tetapi tidak menutup CTA dengan nav/keyboard.

Uji semua layar/state relevan pada 320×568, 360×640, 390×844, 412×915, 430×932, 768×1024, 1440×900, masing-masing teks normal dan font scale 200% nyata. Angka/label/CTA tidak terpotong atau overlap; ujung konten tercapai; tidak ada overflow horizontal halaman yang tak disengaja. Target sentuh setara 48 dp, keyboard/focus browser, safe-area CSS, kontras, dan reduced motion diperiksa. Pembuktian dp/IME/Back native tetap tahap Android, bukan PASS browser.

### 2.4 Paket minimum bukti per fitur

Tiap baris requirement wajib terhubung ke route/state → UI handler → bridge → domain → save → tes/hasil. Minimal satu journey pengguna dapat ditempuh lewat UI/bridge produksi; fixture rank/saldo untuk isolasi tidak menggantikan earned journey. Uji input kosong, negatif, nol, sangat besar, format salah, akun salah, double submit, stale data dan retry sesuai kontrak.

Fase uang/order/waktu/save: minimal 10 variasi urutan ×100 aksi bermakna, invariant setiap aksi; sertakan boundary, crash/write failure/recovery. No-op/read berulang bukan pemenuhan jumlah. Gunakan runner existing, bukan framework baru. Snapshot lama yang tidak berubah boleh dirujuk dengan alasan, tetapi integrasi akhir harus diuji ulang setelah perubahan relevan.

P95 per jenis aksi biasa ≤300 ms; seluruh sampel navigasi yang disyaratkan ≤1.000 ms; feedback proses berat sempat tergambar ≤300 ms dan event loop responsif. Minimal 30 sampel per jenis aksi, raw timings, environment, source revision, cache dan metode tersedia. Jangan agregasi untuk menyembunyikan aksi lambat atau membuang outlier. Nol bug terbuka diketahui termasuk minor; review berbeda per fase bila tersedia. Hasil di bawah standar → perbaiki/retest/review, bukan turunkan ambang.

## 3. Peta menu dan route

Nama route di dokumen ini adalah tujuan navigasi; tidak mewajibkan router framework atau URL baru. Reuse flow/panel existing bila cocok.

| Root | Isi dan akses detail wajib |
|---|---|
| Beranda | Wealth hero, hasil hari ini, karier/XP, target berikutnya, briefing/Skip/laporan, ringkasan perusahaan/koleksi/liga/rival, market movers. Semua ringkasan menaut ke detail nyata. |
| Pasar | Saham/watchlist/indeks; pintasan obligasi/komoditas/FX/reksa dana/IPO; detail/chart/order. Tidak menaruh produk pinjaman sebagai saham. |
| Portofolio | Kekayaan gabungan, arus dan hasil, alokasi, kesehatan, kepemilikan per kelas, utang/pinjaman, transaksi/ledger, tools/stress/lab/replay bertingkat. |
| Bisnis | Dashboard perusahaan operasional dan subbagian Firma investasi yang berbeda; perusahaan → overview/kas/pegawai/kebijakan/upgrade/laporan; firma → klien/mandat/fee/fund builder. |
| Lainnya | Profil/statistik, misi/pencapaian, liga/rival/peringkat, koleksi, gaya investor, riset/terminal/kalender, prestige opsional, pengaturan/save/tutorial. Tidak berhenti pada halaman riset yang diganti label. |

Semua route existing tetap dapat dicapai. Layar detail dapat mempunyai lebih dari satu pintu masuk tanpa menggandakan state. Pasar hanya memuat produk yang dapat dibaca dan ditransaksikan sesuai career; locked card menjelaskan syarat, bukan tombol mati.

## 4. REF:shell — kerangka dan penemuan fitur

Baseline: kerangka lima tab ada; alias `bisnis→firma` dan `lainnya→riset` belum memperluas kontennya. Bukti: app.js switchTab, index.html, screenshot 01/02/07. Fase 04/13.

Buat root Bisnis dan Lainnya sebagai hub pada peta menu. Susunan tiap hub: judul → ringkasan relevan → kelompok menu berikon/label → detail. Gunakan `navigationSections`/state domain hanya jika data dinamis dibutuhkan; jangan menduplikasi registry besar jika daftar statis existing cukup.

Aksi: membuka semua fitur dari root, kembali, ganti akun/filter, mempertahankan scroll. Acceptance: setiap route punya entry terjangkau, locked state jujur, tidak ada alias menu yang menyesatkan, tidak ada flow tersangkut atau nav/CTA tertutup pada matriks viewport/state lengkap.

## 5. REF:wealth — kekayaan bersih konsolidasi

Baseline: header/hero tersedia; audit F4 menunjukkan transfer modal perusahaan mengurangi angka wealth walaupun uang masih dimiliki. Fase 01/05/07/09.

Layar: header → ringkasan Kekayaan → rincian kas, surat berharga, unit fund, bisnis, koleksi, utang/collateral. Query snapshot harus mempunyai raw/display per komponen, `asOf`, FX, ownership share dan reconciliation total.

**D02:** total wealth pemain = nilai aset pribadi + nilai bersih ekuitas perusahaan miliknya + nilai koleksi yang dimiliki − liabilitas pribadi; utang perusahaan sudah mengurangi ekuitas perusahaan. Kas/posisi di dalam perusahaan tidak dijumlahkan ulang di luar equity. AUM klien bukan aset pribadi. Nilai perusahaan awal konservatif memakai net assets; goodwill nol sampai model valuasi dengan aturan/tes sah ditambahkan. Fund unit dihitung sebagai unit×NAB, tidak sekaligus menambah underlying fund.

Transfer pribadi↔perusahaan tidak menghasilkan profit/loss, kecuali biaya riil yang terdokumentasi. Loan principal menaikkan cash dan debt sama besar; fee/interest tetap biaya. Harga/FX dan snapshot timestamp sama untuk seluruh komponen. Nilai negatif tidak dipotong menjadi nol agar terlihat sehat. Acceptance: rekonsiliasi seluruh jenis kepemilikan, FX, fee, utang, transfer/reload, dan audit Rp50 juta→modal Rp25 juta tanpa kehilangan wealth karena perpindahan uang.

## 6. REF:market — kategori dan discovery produk

Baseline: saham IDX/US, search/filter/watchlist/movers ada. Fase 02/03/04/09.

Layar Pasar: ringkasan indeks → kategori → movers → search/filter → baris instrumen. Kategori saham, obligasi, komoditas, reksa dana, FX, IPO mengarah ke katalog produk yang benar. Semua baris memuat nama, currency/unit, nilai, perubahan/periode, session/eligibility. Query existing getInstruments/getIndices diperluas sesuai kebutuhan; produk non-saham tidak dipaksa menjadi lot saham.

State kosong membedakan filter tidak cocok, produk locked, pasar tutup, dan belum ada history. Movers hanya berasal perubahan domain pada periode jelas; tie nol tidak diberi label pemenang palsu. Acceptance: search/filter tetap setelah kembali, counter sesuai hasil, quote tidak mencuri fokus, unit dan order sesuai produk, riwayat konsisten saat aktif/Skip/offline.

## 7. REF:chart — chart, metrik dan konteks instrumen

Baseline: line/candle M1/M5/M15/H1/D1 dan order book tersedia. Referensi 03/04/05 memperlihatkan timeframe, metrik, rasio, dan posisi. Fase 03/04/05.

Urutan detail: ticker/harga/perubahan/asOf → chart dan kontrol → OHLCV/statistik periode → order book/depth/tape → posisi pemain → konteks/risk → CTA transaksi. Query getChartData/getOrderBook/getPortfolio berbagi timestamp/identitas instrumen. Tambahkan metrik high/low/open/close/volume yang benar-benar dapat dihitung dari history. Rasio beli/jual harus menyebut metodenya dan hanya tampil jika datanya tersedia; jangan mengarang jumlah investor.

Empty history adalah state sah tanpa candle dekoratif. Resolusi data membatasi timeframe replay: data harian tidak boleh dibuat menjadi candle intraday seolah nyata. Acceptance: agregasi OHLCV boundary, indikator tanpa future leak, format unit/volume, live quote tidak menggeser sumbu secara mengganggu, chart+data table dapat dibaca pada 200%.

## 8. REF:trade — perjalanan transaksi utuh

Baseline: preview/review/submit/status/cancel existing. Referensi 06 hanya membuktikan form, bukan transaksi lama dieksekusi. Fase 03/05/11.

Form: akun, sisi, jumlah lot/unit, tipe order/harga, expiry/stop/TP bila berlaku → review nilai/fee/slippage/risiko → submit → receipt dengan filled/remaining/status → portfolio/ledger. Gunakan canonical OrderManager dan bridge existing; jangan menambah jalur settlement UI.

State wajib meliputi market closed/halt, stale quote, insufficient cash/position/collateral, partial fill, cancel pending/retry, trigger/expiry, account lock. Acceptance: semua order path menegakkan kalender/risk/career, reservasi dilepas benar, receipt bukan klaim filled sebelum fill, retry setelah reload tidak menggandakan transaksi, perubahan harga hanya dari transaksi/event domain yang sah.

## 9. REF:allocation — komposisi seluruh kepemilikan

Baseline: cash/shares/bar sederhana. Referensi 07 memperlihatkan alokasi. Fase 05/07/09.

Portofolio: ringkasan → komposisi per kelas → rincian → akun/aset. `assetBreakdown` berisi gross assets, liabilities terpisah, weight dan valuation method; debt tidak menjadi irisan negatif yang menyesatkan. **D03:** denominator alokasi adalah total gross assets terhitung; net worth ditampilkan terpisah. Jika denominator nol/negatif atau data unavailable, jelaskan dan tampilkan daftar, bukan persen palsu.

Gunakan daftar/bar dan donat hanya jika membantu seperti referensi; semua seri berlabel dan ada tabel angka. Acceptance: bobot menjumlah 100% dalam toleransi pembulatan display, USD terkonversi satu kali, perusahaan/underlying fund tidak double count, akun klien terpisah, grafik dan detail sama setelah mutasi/reload.

## 10. REF:health — kesehatan kekayaan dan risiko

Baseline: margin ratio/risk service bukan skor diversifikasi referensi. Fase 05/11.

Portofolio → Kesehatan: indikator, penyebab, ambang dan tindakan detail. Tampilkan konsentrasi, liquid reserve, debt service, drawdown dan status margin secara terpisah. **D04:** default gunakan kategori terjelaskan (terdiversifikasi/terkonsentrasi, kas cukup/perlu perhatian) dengan formula/threshold rulesVersion; jangan memaksakan angka 30/100 dari screenshot. Jika skor gabungan ditambahkan, bobot/formula/komponen dan missing-data policy wajib terbuka serta diuji.

Query health menyebut exposure basis, periode return/drawdown, liabilitas, dan coverage. Acceptance: risiko tinggi tidak berarti margin call jika kontraknya berbeda; urutan severity/pesan benar, kewajiban tenor masuk debt service, threshold boundary tepat, indikator bukan klaim rekomendasi investasi dunia nyata.

## 11. REF:loans — pinjaman tenor di luar margin

Baseline: take/repay margin dan restructuring tersedia; katalog pinjaman tenor belum ditemukan. Pemilik mengonfirmasi produk tenor wajib. Fase 01/05/11.

Layar Portofolio → Utang & Pinjaman: total outstanding/angsuran terdekat → produk → detail syarat → simulasi jadwal → review → pencairan → kontrak/jadwal → bayar/prepay/restrukturisasi → receipt/ledger. Margin dan pinjaman tenor tetap produk berbeda.

**D05, baseline balancing yang boleh dikalibrasi:** minimal dua produk IDR: pinjaman pribadi beragunan dan modal kerja perusahaan, tenor 7/14/30 hari game; principal minimum Rp1 juta, batas menurut collateral/cashflow yang tersedia. Tarif awal 0,05% per hari pada sisa principal, biaya pencairan 0,5%, angsuran pokok sama besar per hari dan sisa rounding diselesaikan pada angsuran terakhir. Bunga tidak berbunga otomatis. Fee dipotong/ditagih sesuai preview yang jelas. Seluruh angka adalah aturan game, bukan tarif bank nyata.

Contoh kontrak numerik: pinjaman Rp1 juta, 7 hari, fee Rp5.000, pencairan bersih Rp995.000, debt principal Rp1 juta. Bunga hari pertama Rp500 jika saldo principal awal hari Rp1 juta; tidak menjadikan Rp995.000 sebagai principal. Schedule menyimpan principal/interest/fee/due/paid/remaining terpisah. Pelunasan awal membayar principal tersisa+bunga accrued+fee sah, tanpa bunga masa depan atau penalti prepay default. Pembayaran tidak melebihi jumlah terutang; quote harus membedakan partial payment dan lunas.

**D05 lanjutan:** keterlambatan menimbulkan status arrears, penalti sederhana 0,05%/hari atas principal overdue dengan total penalti kontrak maksimum 5% principal awal; tidak menghapus debt atau mencetak rescue cash. Collection/pengambilan collateral hanya melalui aturan, deadline, peringatan dan receipt. Restrukturisasi membuat jadwal baru, mempertahankan principal/fee/accrual sah dan audit history; tidak menghilangkan utang. Jangan membekukan seluruh UI sehingga pemain tidak dapat mengambil pekerjaan/recovery.

Kontrak usulan: loan products/quote/schedule/contract, disburse/pay/prepay/restructure dengan accountId, ownerType, productId, collateralIds, principal, rate, duePeriod, scheduleVersion. Akun peminjam dan liability sama; kas pribadi tidak menerima loan perusahaan secara otomatis. Agunan punya registry/reservation sehingga aset yang sama tidak dapat dijaminkan ulang, dijual atau dihitung bebas dua kali. Tenor mengikuti hari game melewati offline/Skip.

Acceptance: perhitungan contoh, jumlah principal installments tepat, jadwal akhir lunas, boundary due date/late cap, kelayakan/collateral, preview non-mutating, double disburse/pay, insufficient cash, save/crash, prepay/restructure, saldo dan wealth benar, dan simulasi multi-periode tidak menggandakan bunga. Tidak ada koneksi kredit atau uang nyata.

## 12. REF:ipo — penawaran dan settlement

Baseline: UI/bridge IPO existing; bukan hasil runtime lama. Fase 10.

Pasar → IPO → daftar penawaran/filter fase → prospektus game ringkas → pemesanan/review → reserve → status/allotment/refund → posisi/ledger. Sertakan issuer, price range/final price, unit, window, allocation method, lockup bila berlaku, risk dan tanggal.

Acceptance: oversubscription/partial allocation, cancel deadline, missing funds, reserve/refund/fee tepat sekali setelah Skip/offline/reload, timestamp eligibility, akun benar, posisi/order tidak terbentuk sebelum settlement. Audit pula M&A/tender/voting existing sesuai PRD; IPO saja tidak meluluskan seluruh FR-13/14.

## 13. REF:funds — reksa dana unit/NAB

Baseline: Fund Builder adalah target alokasi/order, bukan produk unit reksa dana. Fase 02/05/09.

Pasar → Reksa dana → daftar profil/risiko → detail NAB/history/holdings/fee → beli atau jual unit → review → pending dealing → settlement → receipt → kepemilikan unit/ledger. Reksa dana, dana kelolaan klien, dan Fund Builder mempunyai identitas serta akuntansi berbeda.

**D06:** minimal tiga produk IDR dengan strategi pasar uang, obligasi, dan indeks saham game. Masing-masing memiliki pool aset/kewajiban dan total units canonical. Seed awal pool berasal dari akun sponsor/NPC yang dibukukan saat inisialisasi; tidak mencetak keuntungan ketika pemain subscribe. Aset backing memakai instrumen/domain game, bukan NAV acak UI. Instrumen kas/pasar uang yang dipakai backing harus mempunyai accrual/liability counterparty yang jelas. Boleh memulai katalog kecil namun ketiga mekanik produk dan risiko wajib berfungsi.

NAB total = fair value aset pool + receivables sah − liabilities/accrued fee; NAB/unit = NAB total/units outstanding. Initial issue price baseline Rp10.000, unit precision enam desimal, price precision minimal delapan desimal internal sesuai aturan rounding yang didokumentasi. Minimum subscribe Rp10.000, fee masuk/keluar baseline 0,1%, management fee 0,75% tahunan/365 hari kalender game diakru sesuai interval, tanpa fee duplikat pada callback. Angka D06 boleh dikalibrasi tanpa menghilangkan biaya/risiko/transaksi.

**D06 dealing:** satu cutoff 16:00 WIB pada hari dealing yang didefinisikan kalender produk. Permintaan sesudah cutoff/hari tutup masuk cutoff valid berikutnya. Preview menyebut estimasi dan bahwa harga final memakai NAB cutoff; cash/unit direservasi saat ACCEPTED. Status settled hanya setelah NAV final dan pembukuan. Liquidity shortage tidak memalsukan cash tersedia; gunakan antrean/partial settlement sesuai aturan jelas, batalkan/refund sisa dengan benar. Pending request mempunyai receipt dan cancellation deadline. Jangan menyatakan T+0/cair instan jika engine belum menyelesaikannya.

Subscribe menambah cash pool dan menerbitkan unit sesuai amount net fee/NAB, tanpa perubahan NAV/unit akibat setoran itu sendiri selain rounding/fee yang sah. Redeem membakar unit setelah settlement dan membayar cash dari pool; unit pending tidak dapat dijual dua kali. Valuasi pemain = unit×NAB latest dengan asOf/stale state; jangan menjumlah underlying pool lagi pada wealth pemain.

Fixture numerik acceptance tanpa perubahan pasar/accrual antara dua dealing: gross subscribe Rp1.000.000, entry fee 0,1%=Rp1.000, net Rp999.000, NAB/unit Rp10.000 → 99,900000 unit. Redeem seluruh unit pada NAB sama menghasilkan gross Rp999.000, exit fee Rp999, net Rp998.001. Selisih Rp1.999 terhadap cash awal adalah total fee sah, bukan profit/kerugian harga. Sesuaikan fixture hanya jika aturan fee berubah secara sah dan dicatat, jangan mengubah expected untuk mengikuti bug.

Kontrak usulan fund catalog/detail/history/quote/submit/cancel/order/holdings dengan fundId, dealingPeriodId, navVersion, units precision, fee breakdown, expected settlement, reserved cash/units. Save memuat pool holdings, unit register, accrual anchors, pending requests dan receipts. Acceptance: contoh subscribe/redeem round-trip termasuk fee, NAV sebelum/sesudah external flow, negative/zero NAV guard, total unit reconciliation, cutoff boundary/offline, failed save/retry, no future NAV leakage, dan Fund Builder tetap dapat dipakai secara terpisah.

## 14. REF:deposits — batas bukti deposito

STATIC menunjukkan model bank dan literal `Bank deposits (removed)`; runtime aktifnya belum dibuktikan. **D12:** jangan menciptakan produk deposito baru hanya untuk mengejar kesetaraan dengan sisa string APK. Catat sebagai REFERENCE_UNCERTAIN, pertahankan referensi, dan jelaskan keputusan di tracker. Jika PRD existing ternyata memiliki requirement deposito eksplisit, requirement tersebut tetap berlaku dan Hermes harus menanganinya; paket ini tidak membatalkannya. Status ini tidak menunda produk reksa dana pasar uang atau pinjaman tenor yang sudah diwajibkan pemilik.

## 15. REF:missions — misi harian/mingguan dan karier

Baseline: tiga misi karier permanen, UI klaim dan promosi. Screenshot 11 memperlihatkan tab harian/mingguan, progress dan target. Fase 06.

Beranda → Misi, juga Lainnya → Misi: tab Harian/Mingguan/Karier → ringkasan selesai/claimable → kartu target/progress/reward/deadline → review klaim → receipt. Tampilkan deadline kalender game dan sisa durasi game, bukan countdown dunia nyata yang salah.

**D07:** harian minimal enam template dan mingguan minimal enam template, rotasi deterministik memilih minimal tiga target harian dan tiga mingguan sesuai career/fitur terbuka. Target mencakup belajar/riset, diversifikasi/risk review, laporan, kegiatan perusahaan, dan produk relevan; jangan seluruhnya mendorong churn transaksi atau profit terjamin. Misi karier existing tetap permanen dan berbeda dari reset periodik.

Event progress mempunyai `(runId,periodId,missionId,eventId)`; hanya event dalam periode aktif yang dihitung. Aktivitas offline otomatis boleh menghitung payroll/operasi yang benar-benar terjadi, tidak memalsukan membaca artikel atau menekan tombol. Completed reward dapat diklaim sesudah periode tutup lewat riwayat; tidak auto-claim, tidak diberikan untuk misi gagal, dan tidak diam-diam dihapus ketika offline lama. Simpan klaim dan progress final yang diperlukan walau UI mengarsipkan periode lama.

Reward misi earned boleh kas/XP sesuai rules; achievement tetap tidak memberi kas. Angka reward dikalibrasi terhadap risiko/pendapatan, bukan subsidi kebangkrutan. Acceptance: periode boundary, multi-day/week Skip/offline, seed rotation, target locked tidak dipilih, claim idempotent/save, misi selesai tidak bocor periode lain dan UI riwayat dapat menjelaskan hasil.

## 16. REF:achievements — level, badge dan peristiwa

Baseline: enam badge dan XP, filter status/grid tersedia. Fase 06/07/09.

Lainnya/Beranda → Pencapaian: level/XP → filter kategori dan status → kartu progress/locked/unlocked → detail syarat/asOf/earned date. **D08:** minimal 18 definisi badge yang mencakup trading, belajar, risiko, firma/laporan, perusahaan/pegawai, koleksi, fund/loan yang dikelola sehat, dan progres jangka panjang. Ini target katalog versi kita, bukan klaim jumlah badge referensi.

Semua badge berdasarkan domain event yang dapat dibuktikan, tidak memberi kas, dan tidak membuka fitur yang seharusnya dikunci karier. Event ulang/reload tidak menggandakan XP. Target panjang realistis dengan game endless; jangan memberikan badge hanya karena membuka menu daftar. Acceptance: completion tepat sekali, negative/boundary progress, migrasi katalog lama, badge report terhubung completion produksi, earned journey tanpa fixture rank, filter/category/focus benar pada 200%.

## 17. REF:ranks — liga periodik yang berbeda dari karier

Baseline: CareerRank/XP bukan liga. Referensi widget rank/league sebagian terlihat di Beranda bawah. Fase 06/13.

Beranda ringkasan → Liga: tier, periodId, waktu tutup, metrik/rules, posisi, tren, pesaing, histori hasil. Karier dan XP tetap ditampilkan dengan identitas berbeda. **D09:** minimal tiga tier liga; evaluasi/promosi/degradasi tiap minggu game; tidak ada reset aset/karier pada rollover. Eligibility dan grace untuk pemain baru/wealth negatif jelas, tidak menyebut mereka peringkat ROI valid jika denominator tak sah.

Metrik default kompetisi adalah return mingguan yang dikoreksi arus noninvestasi; leaderboard juga punya tab kekayaan. Gunakan return methodology di bagian 27. Hasil periode dibekukan pada boundary dan tersimpan, lalu periode baru dibuka sekali. Rewards liga default badge/XP, tanpa rescue cash. Acceptance: fairness external flow, tie, rollover normal/Skip/offline, period history/reload, late command, dan tidak ada formula ROI berbeda antara pemain/NPC.

## 18. REF:rivals — rival NPC persisten

Baseline: NpcAgent pasar bukan rival personal. Fase 03/06.

Lainnya → Rival, serta kartu Beranda: nama fiktif/avatar lokal, gaya/risiko, wealth/return asOf, perbandingan periode, aktivitas domain ringkas. **D09 lanjutan:** pilih satu rival dekat performa dari roster leaderboard; pemilihan di awal minggu tersimpan dan tidak diacak ulang saat membuka layar. Rival tidak diganti diam-diam untuk memaksa pemain kalah/menang.

Roster memakai identitas akun/portfolio/NPC domain yang persisten. Boleh mengembangkan NPC existing; jangan membuat angka ranking acak terpisah dari ekonomi. Semua rival menjalankan kalender dan aturan biaya/valuation yang sama. Acceptance: identitas stabil setelah reload, tidak ada future leakage, rekening/ledger relevan dapat direkonsiliasi, tidak ada aktivitas fiktif dari handler UI, dan akhir minggu memberi hasil yang dapat diperiksa.

## 19. REF:leaderboard — peringkat offline

Baseline: route/kontrak leaderboard belum ditemukan; screenshot 14 memperlihatkan pemain dan tetangga peringkat. Fase 03/06/13.

Lainnya → Peringkat: tab kekayaan/return liga → pemain disorot → atas/bawah → detail peserta → filter periode/tier → metode. Beranda menampilkan ringkasan yang menaut ke daftar ini.

**D09 roster minimum:** 20 NPC fiktif dengan profil risiko/strategi beragam dan state tersimpan; dapat memakai/menaut akun NPC pasar existing. Nama tokoh nyata tidak menjadi default. Tidak menampilkan label simulasi pada leaderboard sesuai permintaan pemilik; konteks bantuan boleh menjelaskan peserta NPC. Tidak ada login, jaringan, klaim pemain online atau kekayaan nyata.

Urut deterministik: nilai utama turun, metrik tie-break terdokumentasi, ID stabil untuk tie terakhir. Tidak menampilkan #1624 jika hanya ada 21 peserta domain. Display jumlah peserta/eligibility jujur. Wealth berasal aturan D02, ROI D13. Acceptance: ranking berubah dari aksi ekonomi yang sah, tie/zero/negative cases, neighbor list boundary, periode arsip, no arbitrary refresh, dan input save yang corrupt tidak menghasilkan peringkat palsu.

## 20. REF:collection — katalog dan kepemilikan koleksi

Baseline: enam item domain/bridge dan biaya, belum UI. Referensi banner menunjukkan kendaraan/properti/permata/seni/legenda, bukan bukti seluruh transaksi koleksi. Fase 09.

Beranda/Lainnya → Galeri: kategori/rarity/owned → kartu item/harga/maintenance/status → detail valuasi/biaya/manfaat/riwayat → review beli/jual → receipt → kepemilikan dan wealth. Jangan menyalin gambar referensi. Gunakan aset lokal legal atau ikon existing.

**D10:** katalog awal minimal 12 item dalam kendaraan, properti, permata, seni; minimal tiga per kategori. Legenda menjadi filter rarity lintas kategori, minimal dua item, bukan aset terduplikasi. Model jam existing dapat dipetakan kategori yang jelas dengan keputusan terdokumentasi. Harga dan upkeep bervariasi agar ada target awal/menengah/panjang; jangan seluruhnya miliaran ketika progres awal Rp50 juta tanpa jalur earned yang realistis.

Valuation deterministic berdasarkan aturan tersimpan (misalnya depresiasi/indeks per kategori), asOf, buy/sell spread/fee eksplisit dan tidak menjamin arbitrage. Upkeep berlaku pada hari game, hanya kepemilikan eligible, tepat sekali; bila cash tidak cukup, debt/arrears atau penghentian manfaat yang dijelaskan, bukan mengabaikan tagihan. Reputation benefit reversibel dan tidak dapat dieksploitasi buy/sell loop; tidak menjadi pengganti career gate.

Acceptance: ownership unik, buy/sell/collateral restrictions, biaya tidak hilang saat offline, valuation+wealth reconcile, double tap/retry, New Game reset, migration enam item, seluruh filter dan state locked/insufficient funds/owned ditangani.

## 21. REF:empire — perusahaan operasional

Baseline: CompanyManager/OperatingCompany dan bridge tersedia; UI Bisnis masih firma investasi. Fase 07.

Bisnis → Perusahaan: ringkasan seluruh bisnis → pilih sektor → rencana pendirian/nama/modal/biaya → review → receipt → detail Overview/Kas/Pegawai/Kebijakan/Upgrade/Laporan. Firma investasi tetap subbagian terpisah dan tidak dihitung sebagai perusahaan operasional yang sama.

**D11:** minimal tiga sektor awal yang berbeda (misalnya jasa, ritel, manufaktur); masing-masing mempunyai biaya/kapasitas, pendapatan dari demand/capacity/productivity serta operating cost. Angka D11 harus dikalibrasi melalui simulasi earned progression 15–30 menit/sesi, tanpa jaminan profit atau pertumbuhan eksponensial bebas biaya. Jangan memakai fixed Rp25 juta/hari untuk semua sektor/level tanpa model biaya, demand dan pembuktian balancing.

Setiap perusahaan punya accountId, owner/share, sector, level/capacity, cash, liabilities, staff/policy, operating status, reports, event anchors. Pendapatan harus memakai counterparty/budget/aturan demand domain yang jelas; transfer modal bukan pendapatan. Biaya pendirian sungguhan berbeda dari modal yang tetap menjadi aset perusahaan. Upgrade butuh quote cost/time/benefit, dana tersedia dan receipt; tidak tersedia hanya karena public method bisa dipanggil.

Default operasi mengikuti kebijakan pemain saat aktif/offline/Skip. Keputusan besar pendirian, investasi/modal besar, hutang, ekspansi, perubahan organisasi tetap manual. Darurat kas: policy prioritas payroll/utang/operasional, accrual kewajiban atau shutdown dijelaskan; tidak melanjutkan produksi penuh tanpa membayar biaya yang hilang. Tidak auto-mendirikan bisnis atau mengambil utang atas nama pemain.

Acceptance: founding/upgrade/cashflow journey, modal vs expense, demand/kapasitas, payroll dan missed obligation, day/week report, downtime/recovery, seed/partition invariance, New Game/reset dan save/migration, serta equity pada net worth tidak double count.

## 22. REF:employees — CEO, staf dan organisasi

Baseline: model CEO/research/trader/HR/compliance ada; CEO dibuat saat founding; workflow manajemen belum lengkap. Fase 07.

Perusahaan → Pegawai: organisasi/departemen → daftar staff/lowongan → kandidat → detail peran/biaya/skill → review hire/assign/upgrade/dismiss → receipt. Tampilkan payroll sekarang/setelah perubahan dan dampak kapasitas/productivity.

**D11 staffing:** minimal CEO dan tiga fungsi yang sesuai sektor, bukan otomatis jabatan trading untuk seluruh jenis perusahaan. Satu pegawai tidak bekerja penuh pada dua perusahaan tanpa aturan. Candidate generation deterministik, biaya hire/training/termination jelas; upgrade punya batas/biaya/waktu. Penempatan CEO otomatis awal wajib dicantumkan dalam founding quote agar bukan biaya tersembunyi.

Query company staff menyertakan employeeId, role/department, level, salary, morale/productivity, assignment, accrual. Command memvalidasi ownership, vacancy/capacity, career dan cash. Acceptance: hire/assign/fire/train nyata memengaruhi payroll/output, tidak dapat upgrade gratis lewat bridge, payroll prorata mengikuti waktu efektif yang dipilih, offline/save/retry benar, utang gaji tidak terhapus saat dismiss/reset yang salah.

## 23. REF:morale — jam kerja, benefit dan kebahagiaan

Baseline: field workHours/benefitTier/morale serta formula ada; UI belum tersambung. Fase 07.

Perusahaan → Kebijakan: jam kerja 6/8/10, benefit, bonus gaji, morale saat ini/tren, estimasi biaya/produktivitas → preview dampak → konfirmasi → policy history. Jika bonus gaji belum ada dalam model, implementasikan secara konsisten atau gunakan kompensasi setara yang memenuhi fungsi, bukan label kosmetik.

**D11 policy:** perubahan efektif pada timestamp tercatat; perpindahan policy berulang tidak memberikan manfaat harian penuh berkali-kali. Morale berada rentang jelas, dipengaruhi workload/kompensasi/tunggakan, pulih/menurun menurut waktu game, tidak hanya reset instan ketika modal bertambah. Benefit meningkatkan biaya nyata dan dampak output dapat diperiksa.

Acceptance: boundary jam/tier/input invalid, before/after quote, accrual/prorata atau effective-next-day yang eksplisit, upgrade HR berpengaruh sah, tunggakan menurunkan morale/operasi, toggle policy exploit, normal/offline/Skip equivalent, save/reload.

## 24. REF:empire-invest — kas dan transfer perusahaan

Baseline: transferCompanyCapital bridge ada, UI belum; audit F3/F4 terkait. Fase 05/07.

Perusahaan → Kas & Pendanaan: saldo tersedia/reservasi, operating reserve, receivables/debt, owner investment → transfer masuk/penarikan → review → receipt/ledger. Pisahkan injeksi modal, dividen pemilik, revenue dan loan. Jangan mengizinkan penarikan kas terkunci untuk payroll/loan tanpa aturan solvency yang jelas.

Query/command menaut account personal dan company pemilik yang sama. Transfer dua sisi atomic, request idempotent, currency/FX eksplisit. Investasi perusahaan pada aset game bila didukung PRD memakai account company dan risk rules; jangan diam-diam memakai personal account. Acceptance: failed second leg/failed save, duplicate/mismatched owner, reserved cash, statement laporan, wealth transfer invariance, New Game membersihkan referensi orphan.

## 25. REF:prestige — pilihan perjalanan baru yang aman

Baseline: belum ditemukan. Fase implementasi 01/06/13, dengan kontrak penyimpanan FR-19 tetap berlaku.

Lainnya → Prestige: syarat → preview perbedaan state yang dipertahankan/reset → backup/checkpoint → konfirmasi eksplisit dua langkah → hasil perjalanan baru. Tautan tidak otomatis mereset. Game tetap endless jika tidak pernah menggunakan prestige.

**D14:** tersedia setelah career Portfolio Manager dan minimal tiga minggu game, dapat dikalibrasi. Bonus permanen default kosmetik/badge dan bonus belajar XP paling tinggi 10%; tidak berupa rescue cash atau pembuka fitur yang melewati karier. Tampilkan semua efek. Save menyimpan lifetime statistics/prestige history, sementara run baru mempunyai runId/seed/anchors/mission period baru.

Kontrak reset mempunyai retention map untuk setiap domain: cash/positions/orders/reservations, company/staff/collection, debt/collateral, client mandate/AUM, history/XP/career. Semua external obligations harus diselesaikan melalui aturan sebelum prestige; tidak menghapus pinjaman/uang klien untuk keuntungan. Backup atomik yang dapat dipulihkan diperlukan sebelum commit. Acceptance: cancel tidak mutasi, syarat benar, liability/mandate block, failed backup/write tidak reset, resume crash tidak dua kali, New Game dan prestige berbeda dari load slot, lifetime/history tetap dapat diperiksa.

## 26. REF:archetype — gaya investor berdasarkan perilaku

Baseline: belum ditemukan; bukti referensi STATIC. Fase 05/06.

Portofolio/Lainnya → Gaya Investor: profil saat ini, periode sampel, indikator/bobot, perubahan historis, penjelasan. **D15:** klasifikasi awal konservatif/seimbang/agresif berdasarkan eksposur saham/risiko, diversifikasi, leverage, holding period dan turnover; bukan kuis psikologi/diagnosis atau saran investasi nyata. Minimal lima fill dan tujuh hari game data sebelum profil final; sebelumnya Belum cukup data.

Gunakan ledger/fill/valuation history canonical; tidak menghitung order belum filled sebagai investasi. Hindari hadiah kas/bonus return dari label, akses tetap karier. Acceptance: boundary/sample count, zero/negative equity, long offline tidak memalsukan trade, deposit/loan tidak dianggap profit, riwayat setelah reload sama, pengguna dapat menelusuri alasan label.

## 27. REF:daily-report — laporan, return dan statistik

Baseline: DailySessionReport dan layar laporan ada; belum konsolidasi seluruh fitur. Referensi 07/08 memperlihatkan hasil harian/mingguan/keseluruhan, trade terbaik/terburuk. Fase 05/06/07/09.

Beranda → Laporan, Profil → Statistik, dan detail Bisnis: pilih hari/minggu/run → ringkasan perubahan wealth → rincian investasi/operasi/noninvestasi → transaksi/biaya/utang/event → ledger. Angka awal/akhir/asOf dan harga/FX konsisten.

**D13:** simpan perubahan equity dan external/noninvestment cashflow per event. Metode return utama boleh time-weighted jika history mendukung; baseline sederhana yang sah adalah Modified Dietz: `(Vakhir−Vawal−ΣF)/(Vawal+Σ(w×F))`, dengan `w=(akhir−waktuFlow)/durasiPeriode`. Didefinisikan pada wealth D02; transfer internal tidak menjadi F, pinjaman net principal tidak menjadi profit, misi/gaji pemulihan noninvestasi dicatat sesuai sumber. Fee/bunga kerugian investasi/usaha dipisahkan dengan klasifikasi jelas. Jika denominator ≤0 atau data tidak cukup, ROI tidak tersedia, bukan 0% atau infinity. Gunakan metode yang sama untuk NPC, liga dan statistik.

Trade terbaik/terburuk default realized net P/L per transaksi/lot tertutup setelah alokasi fee, bukan unrealized winner atau harga buy dibanding sell tanpa basis. Win rate menyebut denominator dan interval; tidak tersedia jika belum ada posisi tertutup. Cashflow reconciliation selalu mungkin walaupun ROI tidak tersedia. Acceptance: fixture numerik return positif/negatif/flow dekat awal/akhir, biaya/salary/capital transfer/loan, multi-currency, holding fund/business/collection, rollover banyak periode, dan laporan konsisten dengan ranking/profil.

## 28. REF:news — riset, kalender dan terminal

Baseline: ResearchService/UI berita/paket/kalender ada. Fase 08/12.

Lainnya → Riset/Terminal: default ringkas → artikel/analis/konsensus/data alternatif/kalender/inbox/perbandingan sesuai FR-09 → detail sumber/waktu → entitlement. Jangan menghilangkan fungsi karena root Lainnya menjadi hub; riset tetap dapat dicapai dari instrumen/briefing.

Data internal game berlabel konteks yang tepat; tidak mengklaim Bloomberg/feed dunia nyata atau prediksi pasti. Entitlement paket uang game harus mengubah akses/data/manfaat nyata dan expiry/charge melalui domain, bukan badge premium. Opening daftar tidak memberi XP membaca semua artikel. Kalender memuat kewajiban fund/loan/company bila relevan tanpa future leakage pada replay. Acceptance: waktu terbit, read event idempotent, subscribe/renew/expiry/reload, restricted benefits, account fee correct, stale/empty states dan terminal tidak memenuhi layar dengan teks implementasi.

## 29. REF:profile — identitas, statistik dan preferensi

Baseline: save/load/text-size settings ada; profil pemain lengkap belum ditemukan. Referensi 08 membuktikan avatar/nama dan statistik, tidak memberi izin cloud login. Fase implementasi 01/04/06/13, termasuk kontrak penyimpanan FR-19.

Lainnya → Profil: avatar lokal/monogram dan nama → ringkasan career/XP/league → kas/portfolio/wealth → tab harian/mingguan/run/lifetime → detail statistik → edit identitas/preferensi. Avatar ringkas pada header boleh menaut profil; jangan menambah avatar besar berulang di semua layar.

**D16:** nama/identitas pemain lokal tersimpan; aset avatar lokal legal, tidak menyalin ilustrasi referensi. Default bahasa Indonesia lengkap; pilihan bahasa Inggris hanya disediakan jika seluruh string/jumlah/tanggal/error yang disentuh dilokalkan benar. Tema tetap keluarga graphite/amber/teal; preferensi kontras tinggi/density/teks dan reduced motion boleh memperluas token bersama, bukan mengganti tema tiap layar. Jangan menampilkan pilihan bahasa/tema yang tidak berfungsi. Preferensi yang ditawarkan harus persisten setelah save/reload.

Kontrak profile/stats/preferences memisahkan identitas save dari account finansial, period stats, locale/format, access settings. Tidak meminta email/password, tidak ada cloud/iklan, tidak menghubungi avatar remote. Acceptance: input nama panjang/emoji/HTML tidak menjadi injection, null stats/empty history, persisted preferences, angka sama dengan D13/league/wealth, data run/lifetime setelah prestige jelas, 200% dan focus benar.

## 30. Penutupan audit F1–F9

Pertahankan reproduksi asli dalam `Artifacts/Independent-Review-2026-10-20/`. Buat bukti remediasi baru; jangan menimpa audit. Line number audit adalah lokasi snapshot, cari fungsi aktual sesudah perubahan.

| ID | Reproduksi/masalah | Syarat penutupan |
|---|---|---|
| F1 | UI/bridge menolak HISTORICAL_REPLAY | Dataset legal → pilih run → chart/order/report → save/resume end-to-end; bukan enum/helper sintetis. |
| F2 | 1×300.000 ms aktif vs 300×1.000 ms menghasilkan book 0 vs 20/order 0 vs 40 pada gameTime sama | State ekonomi/event/RNG setara untuk seed/input terjadwal sama; batas sesi diproses, bukan menyamakan tanggal saja. |
| F3 | New Game meninggalkan satu perusahaan dengan akun hilang | Reset seluruh state sesi dan referensi, termasuk sibling manager; save/reload dan prestige diuji pada QA. |
| F4 | Rp50 juta→modal company Rp25 juta menurunkan displayed wealth Rp25 juta | D02 dan seluruh pemakai net worth konsisten; transfer tidak dianggap rugi, biaya sah tetap dihitung. |
| F5 | Metode company/collection belum dipanggil UI | Seluruh journey domain/bridge/UI/save terjangkau dan diuji; bukan sekadar label/menu baru. |
| F6 | Bukti 35 root/viewport dan 15 font cases terlalu sempit | Matriks seluruh layar/state applicable ×7 viewport×2 text scale dengan screenshot diperiksa. |
| F7 | Performa agregat/tab/picker belum memenuhi metode penuh | ≥30 per jenis, per-action p95, max nav, heavy paint/heartbeat dan raw timings. |
| F8 | Manifest lisensi dan adjusted basis tidak sesuai bukti | Sumber/rights/attribution/column basis aktual ditinjau dan fixture data nyata tervalidasi; legal blocker tetap blocker fitur. |
| F9 | Status top selesai vs tasks PARTIAL/matriks belum teruji | Satu status operasional dengan mapping bukti source final; tidak memalsukan PASS/N/A. |

Replay data memakai manifest versi, checksum, source/terms/access date/attribution, symbol/timezone/currency/resolution, raw/adjusted, coverage/gaps. Gratis download bukan hak redistribusi; repository code license bukan otomatis lisensi data pihak ketiga. Cari sumber yang memenuhi kebutuhan tanpa membeli layanan. Jangan mempublikasikan dataset/key atau mengunduh sumber tanpa hak jelas. Data harian tidak memberi bukti tick intraday.

## 31. Batas yang sengaja dipertahankan

- Offline, tanpa login/cloud/iklan/multiplayer/feed broker atau uang nyata.
- Leaderboard NPC fiktif tanpa label simulasi pada layar leaderboard; tidak mengklaim peserta online.
- Tidak ada tamat, forced bankruptcy reset, rescue cash otomatis, atau prestige wajib.
- Pembukaan fitur mengikuti karier; XP/badge bukan pembuka akses finansial pengganti.
- Deposito tidak dijadikan produk aktif hanya dari removed literal; putuskan berdasarkan kontrak yang benar-benar ada.
- Tidak ada kewajiban menyalin 60 item/angka/ranking/mata uang referensi. Minimum katalog Dxx adalah rancangan kita, dan seluruh kelompok fungsional tetap harus terlayani.
- Produk tenor/fund bukan layanan finansial nyata. Tarif hanya parameter simulasi yang harus dibuktikan seimbang.
- Native/installed/device bukan PASS dari browser/APK. Scope handoff sekarang berhenti pada APK+domain/browser; tidak memulai ADB/emulator.

## 32. Pelaksanaan dan urutan integrasi

| Urutan | Hasil terukur | Dependency dan pemetaan fase |
|---|---|---|
| R0 | Preflight/diff/protection/reproduksi/ownership | Fase 00; penelitian dataset boleh paralel. |
| R1 | Canonical event clock, transaction/save, reset, wealth/return | Fase 01/03/05/06/11; mendasari seluruh fitur keuangan. |
| R2 | Hub Bisnis/Lainnya, shared UI state/snapshot/entry | Fase 04; kontrak disepakati setelah R1, mock hanya berlabel untuk desain sementara. |
| R3 | Perusahaan/staff/policy/cash/report/koleksi utuh | Fase 07/09; uji sebelum angka masuk wealth/ranking. |
| R4 | Reksa dana unit/NAB dan loan tenor | Fase 02/05/09/11; schedule/event dan accounting R1 wajib sah. |
| R5 | Misi/badge/league/rival/leaderboard/archetype/profile/prestige | Fase 03/04/06; statistik dari event nyata R1–R4, bukan angka UI. |
| R6 | Replay nyata, sisa produk/riset/korporasi/PRD | Fase 02/08/10/11/12; sumber data bukan dependency untuk fitur independen yang bisa lanjut. |
| R7 | Regresi lintas fitur, matriks UX/performa, paket APK/laporan | Fase 13 dan review ulang fase terdampak. |

Setiap fase existing 00–13 tetap mempunyai gate sendiri. R0–R7 tidak memperkecil scope atau membuat fase baru pengganti. Pekerjaan independent boleh paralel setelah ownership jelas; shared facade/bridge/save/catalog/event dispatch diintegrasikan satu writer. Tidak menjalankan runner/reset/build pada save/port/output sama dari dua agen.

P0/P1/P2 pada matriks menyatakan prioritas dan urutan, bukan izin menunda requirement mandatory ke rilis berikutnya. Baris REFERENCE_GROUP adalah parent/rollup; jumlah baris matriks bukan jumlah fitur selesai atau persentase kelengkapan.

Worker menerima root, snapshot, file ownership, contracts, requirement IDs, acceptance, save/port/output dan larangan global/publikasi. Worker tidak boleh memberi COMPLETE proyek. Koordinator meninjau hasil, mengintegrasikan, menjalankan ulang pemeriksaan terdampak dan memutuskan gate. Jika delegasi tidak tersedia, catat bukti dan review pass terpisah, jangan menyebutnya independent-agent review.

## 33. Journey integrasi wajib

1. New game QA → tutorial → fill sah → misi/XP → promosi earned → mandat → firma fee/report → company founding. Tidak menggunakan fixture rank sebagai bukti earned progression.
2. Company founding → staff → kebijakan → payroll/revenue → transfer/withdraw → report → offline/Skip → wealth/stats sesuai.
3. Fund subscribe → reserve → cutoff/settle → unit/NAB berubah → redeem → fee/receipt → wealth/save; Fund Builder tetap terpisah.
4. Loan tenor → quote → disburse → installments/interest → late/restructure/prepay → debt/collateral bebas saat lunas tanpa menggandakan uang.
5. Koleksi → buy → maintenance/valuation → collateral restriction bila berlaku → sell → reputation/wealth benar; tidak ada free reputation loop.
6. Hari/minggu rollover → misi/claims → badge → rival/league/ranking → arsip → next period; ulangi dengan elapsed besar dan reload tengah proses.
7. Profil/stats/archetype menampilkan hasil ledger/wealth sama, termasuk denominator return invalid dan empty history.
8. Stress/lab isolated → replay dataset nyata → no future leak → save/resume/akhir dataset tanpa menimpa run utama.
9. Kerugian berat/debt/payroll → pekerjaan earned/restrukturisasi → recovery; tidak ada rescue cash/game over, UI tetap dapat dipakai.
10. Prestige opsional → preview/backup/cancel atau commit → run baru konsisten/lifetime terjaga; gagal backup/write tidak merusak save.

Untuk setiap journey: positive, boundary, denied access, stale, duplicate, crash/save failure sesuai risiko. Reproduksi yang gagal disimpan, diperbaiki dan diuji ulang sebelum gate.

## 34. Gate akhir dan keluaran Hermes

COMPLETE_FOR_APK_HANDOFF hanya bila semua mandatory delta dan requirement existing yang applicable lulus; seluruh known bug termasuk minor ditutup dengan review; seluruh fase 00–13 memenuhi standar; replay nyata/rights sah; UI/performa lengkap; progres pribadi terlindungi; APK dibangun dari source final dan package/signature/aset/hash diperiksa; commit lokal selektif serta laporan/matriks konsisten.

Laporan akhir memuat fase lulus x/14, requirement PASS/FAIL/NOT_TESTED/BLOCKED dan alasan, daftar F1–F9 beserta reproduksi setelah fix, file/commit, source fingerprint, command/exit/XML/raw timings/screenshot, APK path/size/SHA-256/signature, save protection, mode multi-agent/review dan batas native. Jangan mengklaim build ulang jika hanya memeriksa APK existing; jangan menyebut APK debug sebagai store release.

Jika dataset/lingkungan menghalangi requirement wajib, catat blocker fitur spesifik; kerjakan bagian independent. Instalasi/IME/Back/lifecycle/FPS/RAM Android tetap PENDING tersendiri sesuai scope. Jangan menyembunyikan replay di tabel pending perangkat. Validator paket ini hanya memeriksa bahan; hasilnya tidak meluluskan kode, UX, balancing atau APK.
