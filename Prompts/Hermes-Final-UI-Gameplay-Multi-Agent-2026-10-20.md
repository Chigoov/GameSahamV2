# PROMPT MASTER HERMES — FINALISASI GAME SAHAM, UI REFERENSI, LOGIKA, QA, DAN APK

Versi mandat: 20 Oktober 2026, Asia/Jakarta. Ini instruksi eksekusi lengkap dari pemilik. Baca seluruh prompt, keputusan pemilik, bahan handoff, PRD dan cakupan requirement sebelum mengubah kode. Jangan menganggap pembacaan prompt saja menggantikan pembacaan spesifikasi lengkap.

## 1. Tugas dan hasil yang harus kamu wujudkan

Kamu adalah Hermes, koordinator engineer yang bertanggung jawab menuntaskan game Android ini sebagai long task. Gunakan kemampuan multi-agent yang benar-benar tersedia, dengan integrasi satu koordinator. Hasil yang diminta adalah UI final yang mengikuti aplikasi referensi yang sudah diinspeksi pemilik, seluruh fitur wajib yang berfungsi melalui domain produksi, serta APK terverifikasi dengan bukti pengujian yang sesuai.

Lanjutkan source existing. Jangan memulai ulang produk, mengganti runtime, membuat prototipe sebagai pengganti aplikasi, atau berhenti pada rencana/audit/checklist. Pekerjaanmu mencakup membaca, merencanakan dependensi, mengimplementasikan, mengintegrasikan, menjalankan pemeriksaan, memeriksa hasil, memperbaiki akar masalah, menguji ulang, dan membangun artefak akhir.

Tuntaskan gabungan:
1. Seluruh FR-01 sampai FR-20 dan subrequirement dokumentasi produk.
2. Seluruh cakupan 26 layar konsep serta tujuan screen/journey/komponen design brief yang masih berlaku.
3. Fitur aplikasi referensi yang sudah teridentifikasi dan sekarang diminta pemilik, termasuk perusahaan operasional, pegawai, koleksi, liga/rival, dan prestige opsional.
4. Perubahan keputusan pemilik yang ditulis di sini, termasuk navigasi baru, permainan tanpa tamat, serta laju waktu aktif dan tidak aktif yang berbeda.
5. Penambahan minimum yang diperlukan agar fitur di atas utuh, dapat dimainkan dan dapat dipulihkan. Jangan menambah fitur spekulatif tanpa kebutuhan.

Jangan menjanjikan tidak mungkin ada bug sama sekali. Target yang wajib dibuktikan adalah kode valid, alur wajib lulus, invariant keuangan/save terjaga, **nol bug terbuka yang diketahui termasuk minor pada cakupan fase dan penyerahan**, serta batas verifikasi dilaporkan dengan jujur. Fase tidak lulus jika satu kriteria wajib belum terbukti.

## 2. Root produksi, baseline, dan aturan otoritas

Satu-satunya checkout produksi yang boleh ditulis:
C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM

Repo lama hanya untuk referensi dokumen secara read-only:
C:\Users\HYPE AMD\Downloads\VIBE CODING\Game Trading Saham

Sebelum edit:
- Buktikan lokasi kerja dan Git root absolut.
- Catat branch, HEAD, perubahan tracked dan untracked.
- Inventaris pekerjaan existing; jangan reset, clean atau menimpa perubahan lokal.
- Saat bahan ini disiapkan, baseline source HEAD adalah 196a49d, branch codex/owner-audit-portrait-v8. Ada perubahan dokumen handoff yang belum tentu di-commit. Verifikasi keadaan aktual, jangan mengembalikan checkout ke hash ini bila sudah ada pekerjaan baru.
- Source produksi adalah Android Java + WebView. Pernyataan awal bahwa repo belum mempunyai kode dan rekomendasi awal C#/MAUI sudah kedaluwarsa.

Instruksi langsung pemilik dalam prompt ini menggantikan bagian dokumen lama yang bertentangan. Tetap ikuti keamanan data, batas repo, identitas aplikasi, dan larangan publikasi. Baca AGENTS.md; selaraskan catatan aktif secara minimal agar agen lain tidak kembali memakai aturan lama. Jangan menghapus histori keputusan untuk menyembunyikan perubahan scope.

Batas wewenang:
- Boleh mengubah source, UI, tests, data/aset yang haknya sesuai, dokumentasi, branch/commit lokal dan artefak build dalam root produksi.
- Boleh menentukan aturan simulasi yang belum diputuskan, termasuk angka ekonomi, syarat karier, sesi bursa dan manfaat paket, lalu mendokumentasikan alasan dan mengujinya.
- Boleh menggunakan akun/save uji terpisah, transaksi ekstrem dan clock yang dikendalikan untuk tes.
- Tidak boleh mengubah profil/provider/model/credentials atau konfigurasi global Hermes, memasang scheduler/launcher global, membeli layanan, mengirim pesan pihak lain, push, publish atau mengubah repo/data lain.
- Jangan menjalankan dua launcher/writer untuk sesi Hermes yang sama. Resume hanya exact session ID yang sudah diverifikasi; jangan menebak lewat latest.
- Boleh mencari/mengunduh dataset dan dependency/aset yang memang diperlukan dengan sumber serta lisensi yang sesuai. Tidak menggunakan backend/kredensial aplikasi referensi.
- Progres pribadi tidak boleh dijadikan fixture, direset, ditimpa atau dipakai untuk tes destruktif.

## 3. Semua keputusan pemilik yang berlaku

Tujuh jawaban awal:
1. UI final mengikuti tampilan dan layout aplikasi yang sudah diinspeksi/reverse engineered dari HP. Pemilik menyukai layout aplikasi referensi. Preview kita merupakan baseline implementasi, bukan persetujuan final terhadap semua detailnya.
2. Seluruh fitur referensi yang relevan, fitur dokumentasi game lama dan tambahan yang diperlukan harus dituntaskan; jangan diam-diam memindahkan fitur wajib ke versi berikut.
3. Batas penyerahan saat ini: APK terbangun dan terverifikasi, tes logika serta browser lulus. Instalasi APK menyusul.
4. Pengujian bebas menggunakan save uji terpisah dan transaksi ekstrem; progres pribadi harus dipertahankan.
5. Hermes boleh menyelesaikan aturan gameplay yang belum diputuskan tanpa meminta persetujuan rutin lagi, dengan keputusan terdokumentasi dan tes.
6. Replay historis nyata wajib berfungsi; cari dataset dengan hak yang terverifikasi tanpa membeli layanan baru.
7. Multi-agent diizinkan sesuai kemampuan aktual Hermes, integrasi satu koordinator, commit lokal; tanpa push/publikasi.

Sepuluh konfirmasi tambahan dan koreksi waktu terakhir:

| No. | Keputusan final pemilik |
|---|---|
| 1 | Menu utama: Beranda, Pasar, Portofolio, Bisnis, Lainnya. |
| 2 | Game offline tanpa login/cloud/iklan. Leaderboard memakai NPC bernama fiktif tanpa label "simulasi". Tokoh kaya yang nanti disarankan pemilik boleh dipertimbangkan sebagai karakter NPC game, bukan akun atau kekayaan nyata tokoh tersebut. |
| 3 | Progres seimbang: ada kemajuan bermakna setiap sesi aktif 15–30 menit; bukan jaminan profit trading. |
| 4 | Operasional perusahaan dan pegawai otomatis mengikuti kebijakan pemain; keputusan penting tetap manual. |
| 5 | Skip Hari memberi peringatan sebelum konfirmasi, kemudian menyelesaikan 24 jam game dan memberikan laporan. |
| 6 | Kebangkrutan dapat dipulihkan melalui pekerjaan serta restrukturisasi utang, tanpa pemberian uang otomatis. |
| 7 | Koleksi bisa dibeli/dijual, mempunyai valuasi serta biaya/manfaat wajar. |
| 8 | Tidak ada tamat; game dapat terus dimainkan dan akan diperbarui berkala. Prestige bukan akhir permainan atau reset wajib. |
| 9 | Terminal riset ringkas ketika dibuka; detail dan grafik lanjutan bisa diperluas. |
| 10 | Dunia tetap berjalan saat tidak aktif, termasuk biaya dan risiko, tetapi LEBIH LAMBAT: 1 hari game = 1 jam nyata ketika aplikasi ditutup/background. |

Koreksi terakhir nomor 10 menggantikan kebijakan offline sama cepat dengan foreground maupun kebijakan auto-pause lama. Rasio foreground tetap 24 jam game = 5 menit nyata pada 1×. Detail transisi dan tes wajib ada di bagian waktu dan bahan TIME-ACCEPTANCE.csv.

Lima keputusan quality gate berikut juga sudah dikonfirmasi langsung oleh pemilik dan wajib berlaku pada setiap fase:
1. Semua layar dan state fase diperiksa pada seluruh ukuran layar yang disepakati, termasuk teks 200%, dengan layout/tombol/navigasi sesuai kriteria.
2. Nol bug terbuka yang diketahui, termasuk bug minor yang ditemukan, dalam cakupan fase serta dependency yang diperlukan. Tidak ada toleransi kosmetik sebagai alasan meluluskan bug yang masih terbuka.
3. Fase uang/order/waktu/save memiliki minimal 10 variasi urutan yang dapat diulang, masing-masing minimal 100 aksi bermakna, invariant diperiksa setiap langkah, ditambah kasus batas/proses terputus/save gagal.
4. Browser/host lokal: P95 aksi biasa ≤300 ms; pergantian halaman ≤1.000 ms; pekerjaan berat menampilkan indikator ≤300 ms dan tidak membekukan UI. Ini bukan bukti performa native Android.
5. Setiap fase direview agen berbeda bila tersedia; seluruh temuan ditutup dan koordinator memverifikasi hasil integrasi sebelum meluluskan. Fallback hanya dengan bukti kemampuan tidak tersedia dan pass review terpisah.

Aturan lengkap dan minimum khusus **seluruh 14 fase** ada di Work/Hermes-Handoff-2026-10-20/PHASE-QUALITY-STANDARDS.md. Baca penuh dan gunakan sebagai tambahan acceptance TASK.md. Kegagalan standar wajib memicu diagnosis → perbaikan → uji ulang → review, bukan penurunan standar atau deklarasi selesai.

Keputusan sebelumnya yang tetap berlaku:
- Android 2D portrait, konten boleh scroll vertikal, navigasi utama tetap.
- Palet graphite + amber + teal; font lokal dan token CSS bersama.
- Total kekayaan di kanan atas serta wealth hero pada halaman yang sesuai.
- Jabatan karier ditampilkan bersama level/XP. Achievement memberi progres/XP/badge tanpa hadiah uang achievement. Pembukaan fitur mengikuti karier.
- Pasar Indonesia/AS dan indeks dari anggota game harus memenuhi PRD; angka game tidak boleh diklaim feed bursa nyata.
- Target penggunaan pemilik Poco X8 Pro 12/512. Browser dan APK build bukan bukti hasil di perangkat.
- Pemilik belum memasang APK game ini; penyerahan saat ini untuk instalasi pertama nanti, dengan identitas aplikasi existing tetap dipertahankan.
- Pembaruan berkala berarti produk dapat dikembangkan tanpa merusak save, bukan mandat memasang scheduler/publikasi otomatis.

## 4. Bahan wajib dan cara membacanya

Mulai dari:
1. AGENTS.md.
2. Work/Hermes-Handoff-2026-10-20/README.md.
3. Work/Hermes-Handoff-2026-10-20/OWNER-DECISIONS.json.
4. Work/Hermes-Handoff-2026-10-20/REVIEW-AND-REGRESSION.md.
5. Work/Hermes-Handoff-2026-10-20/TIME-ACCEPTANCE.csv.
6. Work/Hermes-Handoff-2026-10-20/FEATURE-ACCEPTANCE.csv.
7. Work/Hermes-Handoff-2026-10-20/MATERIALS.json; verifikasi menggunakan verify_materials.py yang disertakan.
8. Work/Hermes-Handoff-2026-10-20/PHASE-QUALITY-STANDARDS.md — standar bersama, 14 gate fase, metode ukur dan laporan kelulusan.

Baca penuh spesifikasi produk existing:
- Docs/Product/01-Konsep-Game-2D-Portrait.md.
- Docs/Product/02-PRD-Game-Saham-2D-Portrait.md.
- Docs/Design/03-Design-Brief-Game-Saham-2D-Portrait.md.
- Work/README.md dan seluruh Work/00-* sampai Work/13-*/TASK.md sesuai dependensi.
- Work/REQUIREMENTS.csv, Work/SCREEN-COVERAGE.csv, Work/OWNER-UI-LOGIC-MATRIX.csv.
- Work/DECISIONS.md, Work/ACCEPTANCE.md, Work/STATUS.json, Work/HANDOFF-NEXT-SESSION.md.
- Work/OWNER-AUDIT-2026-10-19.md dan Work/CAREER-ACCESS-CHECKPOINT.md.
- Work/PORTRAIT-UI-CHECKPOINT-2026-10-20.md.
- Work/PHONE-UI-REFERENCE.md, Work/REA-APK-ANALYSIS.md dan Work/REA-FEATURE-MAP.csv.
- Work/Hermes-Handoff-2026-10-20/References/Original-Game-Concept.md, salinan utuh dokumen konsep lama; sumbernya dicatat pada manifest. Teknologi/landscape/3D lama sudah digantikan oleh keputusan produk terbaru.

Bukti visual dan analisis:
- Artifacts/Phone-UI-Reference/*.png: 14 screenshot referensi yang benar-benar diperoleh. Gunakan pemetaan isi di PHONE-UI-REFERENCE.md; nama beberapa file tidak sesuai isi.
- Artifacts/REA/evidence/: hasil analisis statis; Work/REA-APK-ANALYSIS.md menjelaskan cakupannya.
- Artifacts/UI-Refresh-2026-10-20/verification.json dan screenshot: baseline hasil UI kita, bukan acuan visual tertinggi atau bukti fitur penuh.
- Salinan APK referensi di Artifacts/REA/acquisition-2026-10-20/ tersedia lokal bila analisis tambahan diperlukan. APK referensi tidak perlu dibundel ke produk atau ke paket dokumen eksekusi.
- Main Dart financial logic dan backend aplikasi referensi BELUM dipulihkan. Nama modul/string AOT bukan source yang bisa disalin atau bukti rumus telah diketahui.
- XML referensi 07-portfolio tidak serentak dengan PNG. Screenshot 10-missions sebenarnya bagian bawah Beranda; 11-missions-view adalah halaman misi.

Bahan harus digunakan secara utuh. Daftar FR di prompt ini adalah instruksi implementasi tambahan, tidak menggantikan 228 baris requirement existing. FEATURE-ACCEPTANCE.csv menyimpan seluruh baris tersebut tanpa menghapus teks aslinya, ditambah 26 kelompok referensi dan keputusan lintas fitur. Status awalnya NOT_TESTED untuk mandat ini; prior_status hanya riwayat.

Jika bahan hilang atau hash berbeda, inspeksi kondisi nyata dan sumber perubahannya. Jangan otomatis menimpa dengan snapshot lama. Verifier bahan hanya memeriksa integritas/cakupan paket; itu bukan tes game, bukan gate fitur PASS.

Verifier bahan dijalankan sebelum perubahan sebagai pemeriksaan snapshot awal. FEATURE-ACCEPTANCE.csv adalah seed acceptance yang dibekukan; jangan mengubah status seed menjadi PASS. Pilih matriks operasional existing atau salinan kerja yang ditautkan untuk mencatat kemajuan. Setelah perubahan sah pada dokumen/matriks operasional, hash snapshot awal boleh berbeda: inspeksi dan catat perubahan, jangan memulihkan file lama, menghapus hasil kerja, atau mengubah hash hanya untuk memperoleh status hijau. Verifikasi kode/test/APK final mempunyai evidence tersendiri, tidak bergantung pada semua hash dokumen tetap sama seperti hari persiapan paket.

## 5. Arsitektur existing dan perlindungan data

Runtime produksi:
- Android native Java + WebView.
- Domain: android/app/src/main/java/com/tradingglobal3d/core/.
- Facade: core/facade/TradingFacade.java.
- Waktu: core/clock/GameClock.java.
- Save: core/storage/SaveState.java dan SaveManager.java.
- Bridge: android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java.
- Lifecycle: android/app/src/main/java/com/tradingglobal3d/mobile/MainActivity.java.
- UI lokal: android/app/src/main/assets/www/index.html, style.css, app.js.
- Katalog/data lokal: android/app/src/main/assets/data/.

Alur yang wajib ditelusuri dan dijaga:
input UI → TradingNativeBridge/TradingWebBridge → facade/domain → validasi/mutasi ledger/save → response/snapshot → UI.

Java memiliki saldo, ledger, posisi, order/matching, harga, NPC, utang, biaya, karier, XP, mandat, clock, perusahaan dan save. JavaScript hanya presentasi/input. Tidak membuat kalkulator ekonomi/matching/clock pasar kedua di JS. Estimasi finansial UI mengambil kontrak preview domain; jangan menanam fee duplikat dalam handler UI.

Pertahankan application ID com.tradingglobal3d.mobile dan signing existing. Jangan membuat keystore pengganti diam-diam atau mengklaim debug APK sebagai production-signed. Jangan mengaktifkan trimming/obfuscation atau mengganti bridge tanpa pemeriksaan kontrak yang sesuai.

Sebelum mengubah schema/state:
- Identifikasi lokasi save pribadi dan save QA secara eksplisit.
- Backup sebelum migrasi; catat checksum dan metode recovery pada salinan.
- Gunakan schema/version dan converter minimum sesuai kebutuhan, bukan sistem migrasi spekulatif.
- Tangani write gagal, ruang penyimpanan tidak cukup, JSON rusak, versi tidak dikenal dan crash saat commit.
- Load gagal tidak boleh diam-diam memulai permainan baru atau menimpa satu-satunya save.
- Save lama yang belum punya anchor offline harus diberi anchor migrasi saat aktivasi aturan baru. Jangan menghitung seluruh waktu sejak instalasi/save lama sebagai utang offline retroaktif tanpa batas aturan yang jelas.
- Jangan memasukkan save pribadi, credentials atau signing key ke Git, log atau paket handoff.

## 6. Prinsip implementasi minimum yang benar

Ponytail berlaku:
1. Pahami masalah, trace alur end-to-end dan seluruh caller.
2. Periksa apakah kebutuhan sudah dipenuhi source existing.
3. Reuse helper/pattern/domain existing.
4. Gunakan standard library dan kemampuan native.
5. Gunakan dependency existing bila perlu.
6. Tulis minimum kode baru yang benar.

Tidak ada framework frontend baru, engine baru, dependency spekulatif, boilerplate atau abstraksi satu pemakai tanpa kebutuhan. Tidak beralih ke Flutter hanya karena referensi Flutter. Tidak menghidupkan Unity, Three.js atau MAUI.

Perbaiki akar masalah pada lapisan shared. Jangan menambal setiap tombol bila invariant seharusnya ditegakkan di domain. Validasi input, akses akun, ketahanan save, keamanan uang dan aksesibilitas tidak boleh dikurangi demi diff pendek.

Setiap logika nontrivial mempunyai runnable check yang akan gagal bila kontraknya rusak. Gunakan JUnit/runner existing, tanpa framework tes tambahan hanya untuk checklist. Jangan menguji implementasi dengan menyalin rumus salah yang sama ke expected result. Gunakan invariant, contoh hasil independen dan alur silang.

Tandai simplifikasi disengaja dengan komentar ponytail:; sebut batas/upgrade path jika mempunyai ceiling. Jangan menyebut penghilangan requirement wajib sebagai simplifikasi.

## 7. Organisasi multi-agent dan pencegahan konflik

Mode default adalah AUTO: utamakan multi-agent ketika kemampuan dan slot nyata tersedia. Periksa tool/sub-agent Hermes melalui fasilitas lokal yang benar-benar didukung; catat nama kemampuan, jumlah slot yang dapat dipakai dan hasil pemeriksaan. Jangan mengarang kemampuan atau menyebut agent semu sebagai eksekusi paralel. Pemeriksaan tidak mengizinkan perubahan profil/model/provider/global Hermes atau peluncuran koordinator kedua.

Pilih mode berdasarkan hasil pemeriksaan:
- MULTI_AGENT: satu koordinator dan hingga tiga agen pelaksana, disesuaikan kapasitas aktual. Tidak wajib empat proses jika slot atau isolasi tidak cukup.
- LIMITED_MULTI_AGENT: satu koordinator dan satu/dua agen; gabungkan peran, jalankan dependensi berurutan dan pertahankan review terpisah.
- SINGLE_AGENT_FALLBACK: hanya jika sub-agent benar-benar tidak tersedia atau gagal digunakan setelah diagnosis yang wajar. Catat bukti keterbatasannya dan lanjutkan dengan pass implementasi/review terpisah. Jangan memilih fallback hanya karena belum mengecek kemampuan.

Pembagian kerja yang dianjurkan:
- Koordinator/integrator: baseline, keputusan, kontrak data, dependensi, save/clock shared, merge, build final, completion audit.
- UX/UI: perbandingan screenshot, arsitektur navigasi lima tab, komponen shared, implementasi presentasi, responsive/state/accessibility.
- Gameplay/domain: transaksi, progres, perusahaan, pegawai, koleksi, risiko, karier dan integrasi antarfitur.
- Data/replay dan QA: sumber/lisensi dataset, replay deterministik, invariant, skenario ekstrem, pengujian integrasi serta review independen.

Jika slot terbatas, jalankan beberapa peran berurutan. Bila sub-agent tidak tersedia, koordinator mengerjakan semua dengan review terpisah; ini bukan alasan berhenti. Laporkan kemampuan yang benar-benar digunakan.

Sebelum edit paralel, catat:
- Agent, tugas konkret, file yang dimiliki, kontrak input/output, dependensi, tes, dan kriteria penerimaan.
- Shared files yang hanya boleh diubah koordinator: facade/clock/save/bridge/app.js/token atau matriks saat ada pekerjaan terkait lain.
- Worktree/patch terisolasi bila tersedia dalam root yang diizinkan; jangan menulis worktree di repo lain tanpa otorisasi.
- Larangan dua agent menulis file/shared state yang sama bersamaan.
- Larangan sub-agent menandai matriks utama PASS tanpa integrasi/review koordinator.

UI dapat memakai fixture berlabel selama kontrak baru belum tersedia; hasil final wajib memakai domain. Integrasikan patch kecil teruji. Jalankan pemeriksaan sesudah merge; hasil tes sebelum merge tidak otomatis berlaku sesudahnya.

Minta agen/reviewer yang berbeda memeriksa **setiap fase**, termasuk UI/data/dokumentasi preflight, bukan hanya uang/save/waktu. Tutup semua temuan yang valid dan verifikasi ulang setelah integrasi. Bila hanya satu agent, lakukan pass review terpisah dengan membaca final diff dan hasil uji secara kritis, bukan sekadar mengulang pernyataan author; catat keterbatasan aktual, jangan menyebutnya independent-agent review.

### 7.1 Kontrak tugas setiap agen
Koordinator memberi work order tertulis minimum: ID task/requirement, root dan baseline, tujuan/acceptance, file yang boleh diubah, file terlarang/shared, dependensi/kontrak data, save/port/output tes yang dimiliki, command pemeriksaan, batas wewenang pemilik, serta format hasil. Setiap agen menerima aturan waktu final, perlindungan progres dan larangan push/global changes; jangan berasumsi semua child otomatis mewarisi konteks lengkap.

Hasil agen memuat diff/commit lokal atau patch, file berubah, perilaku yang sudah/belum selesai, tes yang benar-benar dijalankan, log/evidence, asumsi dan isu terbuka. Laporan agen bukan bukti otomatis PASS. Jika kontrak berubah, koordinator menyelaraskan caller dan memberi tahu agen terdampak sebelum integrasi.

### 7.2 Kepemilikan sumber daya dan integrasi
Single writer berlaku pada file, save, port, build output dan matriks shared. Jangan menjalankan dua runner yang mereset host QA 8767/save yang sama secara paralel. Gunakan save/direktori output terpisah untuk worker atau jalankan bergiliran. Build/regresi final yang memakai checkout/output sama dijadwalkan koordinator secara berurutan; worker boleh memakai lingkungan terisolasi yang benar-benar terpisah.

Koordinator memeriksa patch terhadap baseline dan perubahan yang sudah masuk. Jangan menggabungkan output stale tanpa rebase/adaptasi yang tepat, menyelesaikan konflik dengan membuang kode agen lain, atau cherry-pick/merge membabi buta. Uji ulang perubahan terintegrasi. Jika isolasi tidak dapat dibuktikan, batasi worker pada analisis/review read-only sampai giliran edit diberikan. Cukup catatan ownership/checkpoint existing; tidak perlu membangun layanan lock/orchestrator baru.

### 7.3 Keputusan selesai tetap di koordinator
Worker tidak boleh mengurangi scope, menghapus tes gagal, menandai seluruh requirement selesai, mengganti clock/ekonomi bersama, atau menyatakan APK final tanpa integrasi dan bukti. Koordinator membaca hasil sebenarnya, menutup temuan review, dan memeriksa artefak pada source akhir. Hanya koordinator mengeluarkan status penyerahan setelah completion audit.

## 8. Matriks cakupan dan fase kerja

Gunakan 14 paket existing sebagai urutan dependensi, bukan batas scope atau alasan mengulang yang sudah selesai:
00 preflight/keputusan; 01 domain-akun-waktu-save; 02 data-katalog-indeks; 03 pasar-NPC-order-halt; 04 UX/host; 05 trading-portofolio-FX; 06 karier/hari/lifecycle; 07 firma-klien; 08 riset/terminal; 09 multi-aset/fund builder; 10 corporate/IPO/M&A/voting; 11 risiko/margin/short; 12 histori/lab; 13 QA/APK.

Tambahkan fitur referensi ke paket yang paling sesuai. Jangan membuat 14 aplikasi atau ledger/mesin ekonomi per paket.

Urutan eksekusi:
A. Inventaris actual state, backup, baseline test, baca requirement, bentuk agent ownership. Mulai riset lisensi data sejak awal.
B. Selaraskan kontrak akun/clock/save, aturan yang belum ada dan matriks cakupan. Buat acceptance sebelum implementasi kompleks.
C. Implementasikan domain dan UI bertahap, mengikuti dependensi; tiap irisan harus punya jalur UI-domain-save yang dapat diuji.
D. Integrasikan perusahaan/pegawai/koleksi/rival serta seluruh fitur lanjutan PRD. Pastikan waktu aktif/offline/skip melewati jalur yang sama.
E. Jalankan earned journey, invariant, data historis nyata, review visual/aksesibilitas dan review independen. Perbaiki seluruh blocker wajib.
F. Rebuild final, verifikasi paket/signature/assets, audit seluruh requirement dan serahkan laporan/APK.

Setiap baris matriks menyimpan ID requirement, sumber, perilaku, prior_status, status pengujian terbaru, source/bridge/UI, evidence, lingkungan, revision dan gap. Jangan menandai PASS hanya dari keberadaan file/metode/enum. Bukti tiap requirement harus sesuai luas klaimnya. Kandidat statis reference tetap diberi konteks bukti; adaptasi offline dicatat terbuka.

### 8.1 Gate fase wajib, tidak boleh diluluskan dengan rata-rata skor
Sebelum mengerjakan fase, tulis acceptance, metode pembuktian dan dependency. Terapkan seluruh standar pada PHASE-QUALITY-STANDARDS.md untuk Phase 00 sampai Phase 13. Seluruh kriteria wajib harus lulus, seluruh bug diketahui ditutup, dan review serta verifikasi integrasi selesai. FAIL/NOT_TESTED/evidence lama bukan PASS. Jangan menutupi satu kriteria gagal dengan banyak kriteria lain yang hijau atau menurunkan severity bug untuk meloloskan gate.

Jika gate belum lulus, tandai status yang tepat, simpan reproduksi, diagnosis akar masalah, perbaiki dan jalankan ulang kasus gagal beserta regresi terdampak, lalu review kembali. Jangan mengerjakan dependency berikut seolah fase gagal sudah lengkap. Tugas independen tetap dapat dilanjutkan; prototipe/fixture diberi label dan tidak meluluskan integrasi final.

### 8.2 Definisi pengujian dan performa minimum
Pada fase kritis, gunakan minimal 10 variasi urutan × 100 aksi bermakna pada domain produksi, seed/urutan tercatat dan invariant diperiksa sesudah setiap aksi. Bukan 1.000 no-op atau method yang hanya mengembalikan sukses. Sertakan kasus batas, restart/crash, write/save failure dan retry. Reuse runner existing; tidak perlu framework baru atau 1.000 test method.

UI mencakup semua pasangan layar/state yang berlaku pada tujuh viewport (320×568, 360×640, 390×844, 412×915, 430×932, 768×1024, 1440×900), teks normal dan 200%; screenshot diperiksa. P95 diukur per aksi biasa dari input hingga hasil benar tergambar, minimal 30 sampel per aksi. Navigasi ≤1.000 ms pada seluruh sampel yang disyaratkan; feedback heavy work harus tergambar ≤300 ms dan UI tetap responsif. Catat raw timings, environment, data/save dan kondisi cache. Jangan membuang sampel lambat atau menghitung error sebagai respons cepat. Metode lengkap ada di standar fase; ambang pemilik tidak boleh dilonggarkan.

### 8.3 Bukti gate dan batas penyerahan
Checkpoint fase memuat criterion expected/observed/status/evidence, revision, bug IDs beserta closure/retest, seeds/jumlah aksi/assertions, matriks layar/state/viewport/text scale, raw performance, author/reviewer dan keputusan koordinator. Runner keluar nonzero ketika assertion wajib gagal; report bertuliskan PASS tanpa proses/inspeksi nyata bukan bukti.

Gate fase pada scope host/browser/APK dipisahkan dari acceptance native yang ditangguhkan. Jangan menandai Android penuh COMPLETE jika belum diuji, tetapi jangan memakai penundaan native untuk melewati bug source/host yang sudah diketahui. Standar ini tidak menghapus scope fitur atau mengizinkan mengubah baseline pribadi demi tes.

## 9. Cakupan FR-01 sampai FR-20 — wajib lengkap

### FR-01 — Mode, waktu dan siklus hari
Implementasikan permainan internal dan replay historis nyata, identitas run/mode/seed/dataset/rules version, isolasi progres, briefing, sesi perdagangan, penutupan, laporan, dan hari berikut. Clock mengikuti kontrak aktif/offline di bagian 11. Waktu bursa/timezone dan kalender konsisten. Pergantian mode tidak menimpa progres lain, tidak mengubah waktu masa depan menjadi informasi yang sudah tersedia. Setoran/penarikan/gaji/reward dibedakan dari hasil investasi.

### FR-02 — Katalog, pencarian, watchlist dan indeks
Penuhi universe Indonesia/AS dan cakupan dokumentasi. Identitas perusahaan nyata tidak berarti harga live. Search/filter/kategori/watchlist bekerja dan tersimpan. Indeks mempunyai anggota, basis, metode dan timestamp yang dapat diperiksa. Jangan menamai basket kecil sebagai IHSG resmi tanpa menjelaskan cakupannya. Data unavailable tidak diisi angka dekoratif atau tombol mati. Tampilkan detail indeks dan akses anggotanya.

### FR-03 — Dunia pasar, NPC dan hubungan lintas aset
NPC mempunyai aturan, posisi/modal atau model yang sah, order dan pengaruh likuiditas, bukan angka harga acak dari JS. Berita/makro/event dan hubungan lintas aset mengikuti waktu game serta aturan terdokumentasi. Hasil dapat diuji dengan seed/state yang sesuai. Likuiditas tidak boleh menciptakan fill tanpa lawan/model eksekusi yang dijelaskan. Jangan mengaku telah memulihkan AI/rumus referensi yang tidak tersedia.

### FR-04 — Chart, terminal, order dan matching
Implementasikan market/limit/stop-loss/take-profit, validasi, expiry, preview/review/submit/receipt/status, partial fill, reservasi, cancel/retry, antrean/halt/lelang sesuai aturan. Chart garis/candle/timeframe/OHLCV, order book dan tape berasal dari data game. High/low/volume/change punya definisi waktu, bukan nilai dummy. Penggantian harga sesudah preview mempunyai aturan validitas/konfirmasi. Submit ganda tidak menggandakan order/fill.

### FR-05 — Portofolio dan pembukuan
Sediakan kas tersedia/tertahan, posisi, cost basis, valuasi, P/L terealisasi/belum, fee, liabilitas, kekayaan bersih, alokasi, ledger dan rekonsiliasi. Hindari double counting kas pinjaman dan nilai bisnis/koleksi. Wealth header/hero harus cocok domain. Bedakan transaksi gagal, pending, partial dan selesai. Setiap perubahan ekonomi memiliki sebab yang bisa ditelusuri.

### FR-06 — FX
Dompet IDR/USD, quote, spread/fee, masa berlaku, preview, konfirmasi dan riwayat harus bekerja. Gunakan presisi/rounding eksplisit dan lindungi retry/duplikasi. FX untuk valuasi berbeda dari transaksi konversi aktual; jangan menyembunyikan sumber kurs/timestamp.

### FR-07 — Karier, tutorial, misi dan XP
Tutorial dapat diselesaikan melalui aksi sebenarnya. Misi terikat event domain, progres/reward idempoten, reset harian/mingguan berdasarkan waktu game, dan akses ditegakkan domain. Jabatan dan XP merupakan progres berbeda. Achievement tidak memberi uang; hadiah kerja/misi yang disetujui dicatat terpisah. Uji promosi earned, bukan hanya fixture rank tinggi. Pemulihan rugi/utang tidak memberi uang otomatis. Jabatan tertinggi tidak menamatkan permainan.

### FR-08 — Klien, firma, mandat, trust dan fee
Implementasikan penawaran/penerimaan mandat, akun klien terisolasi, batas investasi, deposit/withdrawal, reserve, trust, termination, management/performance fee, high-water mark dan laporan periode. AUM bukan wealth pribadi. Fee tidak berulang saat retry, reload, close day atau catch-up. Laporan dan badge terkait punya event completion sebenarnya. Firma investasi berbeda dari perusahaan operasional.

### FR-09 — Riset, berita, terminal dan paket
Berita/riset/analis NPC/konsensus/alt-data/kalender/inbox/perbandingan yang disyaratkan mempunyai alur nyata dan waktu rilis. Membuka menu bukan otomatis membaca semua artikel. Paket dibeli dengan uang game, manfaat/entitlement benar-benar memengaruhi akses/data, expiry/renewal konsisten, dan tidak menggandakan charge. Terminal ringkas dengan progressive disclosure; kode FR/OQ dan penjelasan implementasi tidak ditaruh di layar pemain.

### FR-10 — Multi-aset
Saham, obligasi, komoditas dan FX mengikuti unit, harga, kupon, jatuh tempo, settlement serta risiko yang jelas. Jangan sekadar menambah kartu untuk aset yang tidak bisa diperdagangkan/dibukukan. Integrasikan valuasi, ledger, kalender, laporan, dan save.

### FR-11 — Fund builder
Rencana alokasi, constraints mandat, evaluasi risiko, preview/eksekusi rebalancing, biaya dan hasil harus terhubung ke order sebenarnya. Target bobot bukan posisi yang otomatis sudah dimiliki. Partial fill dan sisa dana ditangani. Produk reksa dana/unit tersendiri dari referensi bukan pengganti fund builder ini.

### FR-12 — Aksi korporasi
Dividen, split/reverse split, rights, buyback dan dampak merger mengikuti jadwal, eligibility, posisi, order, kas dan ledger. Jelaskan fraksi, pembulatan dan penyesuaian harga/chart. Uji lintas akun serta offline/skip; event terjadi sekali. Peristiwa game untuk perusahaan nyata tidak diklaim fakta historis.

### FR-13 — IPO dan M&A
Penawaran, eligibility, pemesanan/tender, reservasi, alokasi, pembayaran, refund dan settlement punya status serta receipt. Tenggat dan halt terintegrasi. Dana tidak hilang karena penawaran batal/expired atau save/load. Tombol booking tidak cukup membuktikan seluruh IPO/M&A selesai.

### FR-14 — Voting
Hak suara berasal dari posisi yang memenuhi syarat, agenda punya deadline, jumlah suara valid, hasil serta konsekuensi domain. Tolak vote ganda/tidak berhak. Pergantian hari, offline dan reload tidak mengulang efek. Sediakan layar status/hak/deadline/hasil yang terbaca.

### FR-15 — Risiko, compliance, margin dan short
Hitung eksposur/drawdown/rasio margin dengan definisi; tegakkan loan, bunga, collateral, borrow availability/fee, maintenance, recall, liquidation dan short squeeze. Tolak akses atau akun yang tidak berwenang di domain. Skor health referensi hanya diadaptasi jika formula jelas; jangan menyalin angka screenshot. Loan produk konsumtif/operasional dan margin trading bukan produk sama.

### FR-16 — Gangguan perdagangan
Peringatan/halt, antrean, expiry, cancel yang diizinkan dan reopening/call auction diterapkan seluruh jalur. Berita UI saja tidak boleh membiarkan matching/trigger/NPC mengeksekusi saat terlarang. Uji sesi tertutup/weekend/hari libur/reopen dan partial order.

### FR-17 — Historis, lab, stress dan replay keputusan
Replay memakai dataset nyata yang haknya sesuai, sementara stress/what-if/lab mempunyai state terisolasi. Hasil lab tidak menambah uang/progres produksi. Tampilkan input, asumsi, risiko dan hasil; jangan menganggap grafik contoh sebagai simulasi strategi selesai. Hindari look-ahead/future leakage dan uji determinisme.

### FR-18 — UX portrait dan aksesibilitas
Seluruh halaman/state mengikuti UI referensi, shared token, navigasi baru, keterbacaan, fokus, reduced motion dan feedback. Konten scroll, root nav tetap. Browser tests membuktikan scope browser; keyboard/Back/safe-area/TalkBack/dp native belum PASS tanpa runtime Android. Perbaiki source native yang diketahui salah meskipun instalasi ditunda.

### FR-19 — Save, migrasi, recovery dan lifecycle
Autosave/manual/save-load, backup, corrupt recovery, versioning, transaksi atomic, pending commands, RNG/events, offline anchors dan laporan harus konsisten. Restart/crash/storage failure tidak menimbulkan duplikasi atau silent reset. Migrasi diuji dari save lama pada salinan. Semua data pemain baru, klien, bisnis/pegawai, koleksi, prestige dan leaderboard disimpan sesuai kontrak.

### FR-20 — Distribusi Android
APK standalone dengan aset lokal, identitas/versi/signature/checksum serta sumber build yang diketahui. Verifikasi isi paket dan kecocokan aset final. Hindari CDN atau fallback mock. Debug disebut debug. Instalasi/update/runtime device ditangguhkan untuk penyerahan ini, tetap NOT_TESTED; jangan menghapus requirement native dari produk atau mengaku seluruh gate penuh lulus.

## 10. Seluruh kelompok referensi dan adaptasinya

Baca semua 26 baris REA-FEATURE-MAP.csv, termasuk anchor/batas bukti. Tuntaskan:
1. Shell/nav/scroll sesuai referensi, diadaptasi ke lima tab pilihan pemilik.
2. Wealth/kas/net worth yang terhubung domain.
3. Kategori pasar, daftar, sorotan, pencarian dan filter.
4. Chart garis/candle dan OHLCV sebenarnya.
5. Beli/jual dengan flow preview-review-submit-receipt.
6. Alokasi portofolio dengan total yang dapat direkonsiliasi.
7. Health/risk score dengan formula dan konteks.
8. Produk pinjaman, bunga, jatuh tempo, pembayaran/pelunasan dan restrukturisasi.
9. IPO lengkap.
10. Reksa dana/unit: NAV, pembelian/penjualan, alokasi, fee, eligibility serta settlement yang jelas.
11. Kandidat deposito: ada literal "Bank deposits (removed)", bukan bukti aktif. Verifikasi atau putuskan eksplisit sebagai tambahan bila perlu; jangan mengaku sudah diobservasi. Catat keputusan berbasis bukti.
12. Misi harian/mingguan.
13. Achievement/badge/XP.
14. Rank/liga/ROI periode yang berbeda dari jabatan karier.
15. Rival NPC personal, berbeda dari market-making NPC.
16. Leaderboard/neighbor rank lokal berbasis NPC dan hasil domain tersimpan.
17. Koleksi kendaraan, properti, seni: katalog, kepemilikan, transaksi, valuasi dan biaya/manfaat.
18. Empire/perusahaan operasional: pendirian, biaya, pendapatan, kas, profit dan perkembangan.
19. CEO/pegawai/departemen: rekrut, peran, upgrade, biaya dan pengaruh.
20. Happiness/morale/jam kerja/benefit/payroll: aturan biaya-produktivitas, batas dan dampak.
21. Investasi/transfer kas perusahaan dengan akun dan otorisasi jelas, terpisah dari AUM klien.
22. Prestige opsional dengan konfirmasi/preview/backup dan progres permanen yang ditentukan.
23. Tipe investor/archetype dari perilaku yang cukup, bukan klasifikasi acak.
24. Laporan harian dengan atribusi P/L serta arus dana yang benar.
25. News/market event yang mengikuti waktu dan model game.
26. Profil lokal/statistik/pengaturan/bahasa/tema sesuai fitur yang benar-benar dipilih, tanpa login cloud.

Nama NPC fiktif konsisten. Tidak ada label "simulasi" berulang pada leaderboard. Tidak memakai online badge, jumlah pemain online palsu atau identitas seolah akun tokoh asli. Peringkat dihitung dari nilai/ROI/metrik yang didefinisikan; jangan menyisipkan angka acak di JS. Saran nama tokoh kaya dari pemilik bisa dipertimbangkan nanti sebagai karakter NPC; tidak perlu menunggu daftar nama untuk menyelesaikan implementasi.

Fitur cloud/iklan/backend referensi diadaptasi ke fungsi lokal yang sesuai dan dicatat ketidaksetaraannya. Tidak menambah tombol login/iklan mati. Kandidat fitur dari string statis tidak boleh otomatis dianggap fitur terobservasi atau dipakai untuk menghapus scope lain.

## 11. Kontrak waktu aktif, background, offline dan Skip Hari

### 11.1 Nilai final yang tidak boleh ditebak ulang
- Foreground normal 1×: 300 detik nyata = 86.400 detik game. Rasio 288 detik game/detik nyata.
- Foreground 2×: 150 detik nyata = 1 hari game. Foreground 5×: 60 detik nyata = 1 hari game.
- Tidak aktif: 3.600 detik nyata = 86.400 detik game. Rasio 24 detik game/detik nyata.
- Background/ditutup selalu menggunakan laju tidak aktif tersebut; pilihan foreground 2×/5× tidak dibawa ke offline.
- "Tidak aktif" berarti aplikasi tidak terlihat/interaktif, background, layar terkunci atau proses ditutup. Layar bantuan/pengaturan yang masih aktif di dalam aplikasi bukan alasan mengganti laju tanpa aturan eksplisit.
- UI dan domain membedakan status foreground/background, speed pilihan dan laporan offline. Jangan membuat clock kedua di JS.

Contoh hasil:
| Interval | Kemajuan game |
|---|---|
| Aktif 5 menit pada 1× | 1 hari |
| Aktif 1 menit pada 5× | 1 hari |
| Tidak aktif 30 menit | 12 jam |
| Tidak aktif 1 jam | 1 hari |
| Tidak aktif 8 jam | 8 hari |
| Tidak aktif 24 jam | 24 hari |
| Aktif 5 menit pada 1× lalu tidak aktif 1 jam | 2 hari |
| Aktif 5 menit pada 5× lalu tidak aktif 1 jam | 6 hari |

### 11.2 Implementasi dan batas transisi
Trace GameClock, TradingFacade.tick/advanceWorldSeconds/skipDay, bridge tickClock/setClockState, JS timer dan MainActivity lifecycle serta semua caller. Saat persiapan mandat, MainActivity.onPause masih melakukan clock.pause(); kebijakan ini perlu diubah, bukan dianggap sudah sesuai.

Ubah laju pada boundary lifecycle dengan memproses elapsed sampai boundary memakai laju lama tepat sekali, menyimpan anchor, lalu memproses interval berikut dengan laju baru. Ketika resume/load, selesaikan interval offline dahulu, kemudian gunakan laju foreground. Cegah hitung ganda dari onPause/onStop/onDestroy/resume/reload maupun callback yang datang berulang.

Jangan mengandalkan onPause/onStop/onDestroy selalu sempat dikirim. Bila proses mati tanpa boundary yang tercatat, saat load perlakukan interval belum committed sejak anchor terakhir sebagai interval tidak aktif pada rasio 24×, bukan rasio foreground 288×/2×/5× hanya karena flag terakhir tersimpan ACTIVE. Catat kebijakan interval yang tidak teramati ini dan uji. Segmen foreground yang sudah committed tidak diproses ulang. Save legacy tanpa anchor tetap mengikuti aturan migrasi, bukan estimasi downtime dari tanggal arbitrer.

Gunakan domain untuk konversi elapsed, fractional remainder dan event processing. Pertahankan fraksi kecil; banyak langkah pendek harus setara satu langkah panjang. Jangan floor setiap tick sehingga waktu hilang. Gunakan presisi/rentang yang aman dan checked arithmetic pada batas yang relevan.

Waktu dunia boleh direkonsiliasi saat kembali tanpa proses Android/JS tetap hidup. Tidak perlu foreground service, scheduler global atau backend. Jangan mengaku proses terus hidup ketika yang dilakukan adalah catch-up; hasil ekonominya harus tetap sesuai kontrak.

Jika Pause manual dipertahankan, dokumentasikan perilakunya sebagai keputusan gameplay dan uji. Jangan mencampur pause manual dengan lifecycle atau menggunakan pause tersisa sebagai alasan dunia tidak berjalan saat ditutup. Default mandat ini: penutupan/background tetap memajukan dunia pada laju offline, apa pun pilihan speed foreground sebelumnya. Jelaskan konsekuensi Pause yang relevan kepada pemain.

### 11.3 State dan ketahanan catch-up
Simpan schema/rules version, last committed real-time anchor, game timestamp, mode/speed yang relevan, fractional remainder, pending progress dan receipt/event identity minimum yang diperlukan. Gunakan state existing dan perubahan sekecil yang benar; tidak memaksakan event-sourcing/framework baru.

Pada crash/retry, lanjutkan dari checkpoint committed. Jangan replay mutasi yang sudah committed atau melewati sisa interval yang belum selesai. Mutasi ekonomi dan anchor commit harus konsisten. UI tidak boleh mengizinkan order pada snapshot separuh rekonsiliasi.

Tangani save lama tanpa anchor, jam mundur/maju, timezone perangkat berubah, reboot, integer overflow dan input elapsed ilegal. Foreground dapat memakai monotonic elapsed yang sesuai; persistensi lintas proses perlu anchor yang bisa direkonsiliasi. Jangan menjanjikan trusted anti-cheat time tanpa server pada produk offline.

Tidak memotong catch-up pada batas beberapa jam/hari secara diam-diam. Bila interval panjang, gunakan batching/boundary event/checkpoint dan indikator kemajuan yang menjaga hasil domain. Jangan melompati fee, risiko, fill, event, expiry atau progres karena perhitungan lama. Pilih optimasi setelah mengukur jalur existing; jangan membangun scheduler rumit tanpa kebutuhan.

### 11.4 Ekonomi selama pemain tidak hadir
Pasar/NPC/order aktif, stop/TP, settlement, biaya, bunga, borrow fee, payroll, pendapatan bisnis, expiry paket, aksi korporasi, voting deadline, misi/liga dan laporan mengikuti waktu game yang maju. Pengaruh kalender bursa tetap dihormati. Aturan default darurat harus terdokumentasi; tidak memilih keputusan besar pemain tanpa kebijakan yang sah.

Operasional rutin perusahaan mengikuti kebijakan pemain; keputusan besar seperti pendirian, modal, rekrut utama dan pembelian tetap manual. Jangan membekukan seluruh dunia sambil menunggu tombol keputusan. Saat kembali, sediakan laporan ringkas sebelum/sesudah, P/L, arus kas, utang, biaya, transaksi, event dan tindakan berikutnya; detail bisa dibuka.

### 11.5 Skip Hari
Sebelum konfirmasi, tampilkan peringatan relevan berdasarkan order, risiko, fee/tagihan dan deadline yang diketahui. Ini bukan prediksi profit/hasil pasti.
Setelah konfirmasi:
- Rekonsiliasi elapsed yang belum diproses.
- Tambah tepat 86.400 detik game secara eksplisit lewat jalur dunia yang sama.
- Gunakan kebijakan default/darurat yang terdokumentasi.
- Jangan berhenti di tengah hanya untuk meminta persetujuan rutin.
- Tampilkan receipt serta laporan perubahan.
- Cegah duplikasi double tap/retry/load lewat request ID dan state commit.
- Reset anchor agar 24 jam lompatan tidak dihitung kembali sebagai offline elapsed.
- Catat terpisah game time yang berasal dari elapsed dan dari skip.

### 11.6 Tes yang wajib
Gunakan seluruh contoh numerik TIME-ACCEPTANCE.csv dan skenario fault di REVIEW-AND-REGRESSION.md. Tes numerik bahan hanya expected contract, bukan bukti runtime. Buktikan di domain produksi:
- Foreground 1×/2×/5×.
- Background 24× yang tidak mewarisi speed foreground.
- Perpindahan foreground/background berulang.
- Fraksi dan chunk invariance.
- Offline 5 menit, 30 menit, 1 jam, 8 jam, 24 jam dan 30 hari nyata.
- Restart saat catch-up, write failure dan reload receipt.
- Skip + elapsed tidak overlap.
- Fee/payroll/order/event tepat sekali, misi/league berperiode, dan save lama.
- Perbandingan ekonomi pada kemajuan game yang sama dan seed/input yang setara, bukan membandingkan timestamp saja.
- Long run tanpa overflow, game tamat, softlock atau truncation durasi.

## 12. UX final dan fidelity terhadap referensi

UI harus terasa seperti aplikasi referensi, dengan ekonomi game kita. Gunakan screenshot sebagai dasar hierarki dan proporsi, bukan hanya palet. Jangan mengklaim pixel-perfect untuk layar yang tidak pernah diinspeksi.

Terapkan:
- Portrait, konten scroll, root nav tetap, safe padding dan konten terakhir tidak tertutup nav.
- Lima tab pilihan pemilik; Bisnis mencakup perusahaan operasional dan firma investasi. Lainnya menampung fungsi pendukung dengan pintasan yang mudah ditemukan.
- Wealth hero dominan; total kekayaan ringkas di kanan atas, kas/utang/akun jelas.
- Karier, level/XP, misi dan badge yang terbaca tanpa menumpuk penjelasan.
- Market rows mudah dipindai, kategori/filter/search/watchlist, price change dan status yang tidak hanya mengandalkan warna.
- Chart luas, timeframe/jenis chart, data detail bertingkat.
- Alokasi dan risk/health dengan angka yang dapat direkonsiliasi.
- Achievement grid serta misi/progres sesuai pola referensi.
- Riset ringkas saat masuk, detail/grafik lanjutan dapat diperluas dan state tidak hilang saat refresh.
- Satu CTA utama per langkah; secondary/tonal/link dipisahkan. Selected, pressed, loading, disabled/locked dengan alasan, success/error/empty/stale state jelas.
- Animasi singkat untuk feedback, bukan hiasan berlebihan; reduced motion dihormati.
- Data terakhir boleh dipertahankan saat error dengan status stale/retry yang jelas; jangan mengganti angka menjadi palsu atau merampas fokus search/form.
- Review transaksi terfokus, draft/sisi/akun benar, focus trap, close/back aman, dan submit ganda dicegah.

Gunakan token shared existing di style.css, termasuk graphite #101214, surface #191D21, raised #252B31, amber #FFBD59 dan teal #66E4D5 sebagai baseline aktual, serta font IBM Plex lokal dengan OFL. Perbaikan token aksesibilitas boleh dilakukan konsisten dengan alasan, bukan tema baru per halaman.

Kurangi teks yang tidak membantu keputusan pemain. Pindahkan metodologi/detail panjang ke bantuan/detail. Pertahankan biaya, risiko, eligibility, sumber/batas data dan konsekuensi reset yang material. Jangan menampilkan kode FR/OQ, test fixture atau uraian implementation di APK pemain.

Periksa semua root, route existing, fitur baru dan state yang berlaku pada 320×568, 360×640, 390×844, 412×915, 430×932, 768×1024 dan 1440×900, masing-masing dengan teks normal dan 200%. Viewport pendek, input/fokus/modal, reduced motion dan contrast juga diperiksa. Catat matriks serta screenshot yang benar-benar ditinjau. Bug visual/interaksi minor yang ditemukan tetap wajib ditutup sebelum fase lulus. Browser CSS px tidak membuktikan dp/touch/IME/TalkBack Android. Jangan mengunci rasio sehingga HP berbeda terpotong atau memaksa landscape.

## 13. Invariant keuangan, balancing dan permainan tanpa tamat

Tentukan precision, rounding, currency, unit, sign, valuation time dan aturan fee. Gunakan tipe existing yang aman; input NaN/Infinity/negative/overflow ditolak pada trust boundary.

Buktikan:
- Kas tersedia/tertahan, posisi/collateral/liabilitas sesuai ledger dan status order.
- Uang klien, firma, perusahaan, pribadi dan koleksi terisolasi dan tidak double counted.
- Preview read-only; submit/confirm tervalidasi ulang.
- Retry/double tap tidak menggandakan transaksi, reward, fee atau perubahan waktu.
- P/L tidak memasukkan setoran/gaji/hadiah sebagai return investasi.
- Utang/bunga/pelunasan, margin/short dan likuidasi mempunyai rekonsiliasi.
- Corporate action/IPO/refund/FX/rebalancing/payroll/transfer memberi efek tepat sekali.
- Nilai wealth header/portfolio/report konsisten pada timestamp dan kurs yang sama.

Sasaran balancing: sesi aktif 15–30 menit menawarkan kemajuan sah melalui tindakan pemain, kompetensi, karier/misi atau bisnis. Bukan profit pasti atau subsidi otomatis. Rekam skenario earned dan durasi setara; jangan hanya membuka semua melalui fixture.

Game tidak memiliki tamat. Tidak memaksa prestige/reset atau mengakhiri save karena karier tertinggi, kekayaan, hari, liga atau bangkrut. Pertumbuhan dan event tetap bisa berlangsung. Pemulihan melalui pekerjaan dan restrukturisasi utang harus benar-benar dapat ditempuh pada kas nol/ekuitas negatif. Tidak ada uang penyelamat otomatis. Biaya offline tetap mengikuti aturan; jangan menghilangkannya untuk menyembunyikan balancing yang salah.

Prestige merupakan pilihan dengan preview, syarat, efek permanen, daftar yang reset/bertahan, backup dan konfirmasi kuat. Tidak pernah terjadi otomatis. Pembaruan konten memakai struktur data/migrasi minimum existing; jangan membuat framework konten tak terbatas atau auto-update service tanpa kebutuhan.

## 14. Data historis nyata, provenance dan hak

Cari sejak awal, menggunakan sumber penerbit/primer dan ketentuan yang berlaku. Gratis dibaca/diunduh tidak sama dengan boleh dikemas ulang. Robots.txt atau lisensi library pembaca bukan lisensi dataset.

Untuk setiap dataset catat:
- Penerbit, URL sumber/data/terms dan tanggal akses.
- Lisensi/ketentuan konkret serta izin penggunaan/redistribusi yang dibutuhkan.
- Atribusi wajib dan batas penggunaan.
- Simbol/bursa/periode/resolusi/timezone/mata uang.
- Raw/adjusted, split/dividend handling, missing/duplicate bars, volume/unit.
- Cara mengambil dan validasi, versi/checksum, lokasi penyimpanan.
- Bagian replay yang benar-benar didukung dan bagian yang dimodelkan.

Gunakan dataset terkecil yang memenuhi requirement. Cakupan Indonesia/AS/periodenya tidak boleh dipangkas diam-diam bila wajib. Jangan mengemas data dengan hak tidak jelas. Data sintetis hanya fixture berlabel, bukan mode historis lulus.

Journey wajib: pilih dataset/periode/instrumen → validasi → mulai → maju/pause/skip/offline sesuai kontrak → chart/trading/report → save/load → ulang deterministik. Hindari future leakage saat intrabar, rilis berita dan corporate adjustments. Cegah campur state internal/historis dan order yang memakai informasi masa depan.

Akhir periode dataset tidak menamatkan game utama. Jelaskan akhir cakupan replay dan pilihan periode/mode berikutnya tanpa mengarang candle historis masa depan. Uji anchor waktu saat perpindahan mode.

Jika sumber legal tidak ditemukan, dokumentasikan upaya berbeda dan gap hak/cakupan yang spesifik. Teruskan tugas independen. Replay wajib belum terpenuhi berarti penyerahan belum COMPLETE; jangan membeli layanan atau mengubah requirement menjadi sintetis untuk mendapatkan status hijau.

## 15. Loop otonom implementasi, review dan pengujian

Untuk setiap subtask terukur:
1. Pilih requirement/gap dan acceptance yang konkret.
2. Trace implementasi dan semua caller; baca kode yang akan diubah.
3. Rekam baseline/reproduksi bila memperbaiki bug.
4. Tentukan minimum perubahan yang menyelesaikan akar masalah.
5. Implementasikan dengan kepemilikan file yang jelas.
6. Jalankan compile/syntax serta tes yang mencakup perilaku.
7. Periksa hasil aktual, log, assertions, source dan UI, bukan hanya exit code.
8. Bila gagal, diagnosis akar masalah dan perbaiki. Tambahkan regression check bermakna untuk bug nontrivial.
9. Uji ulang kasus gagal dan jalur sibling yang memakai fungsi shared.
10. Setelah patch terintegrasi, jalankan integrasi/regresi yang relevan.
11. Review terpisah setiap fase oleh agen berbeda bila tersedia, termasuk review visual untuk UI; tutup seluruh temuan dan verifikasi integrasi sebelum meluluskan.
12. Simpan checkpoint/bukti dan lanjut gap berikut.

Jangan memperbaiki tests agar menerima perilaku salah, menghapus assertion, melonggarkan tolerance tanpa dasar, skip tes gagal, memalsukan output atau memberi PASS manual. Tes baru untuk kontrak baru boleh mengganti expected behavior lama bila perubahan keputusan jelas dan alasannya dicatat.

Pengulangan harus menghasilkan diagnosis, perubahan atau bukti baru. Tidak mengulang semua suite hijau terus-menerus tanpa perubahan. Setelah dua kegagalan sama tanpa kemajuan, tulis PERLU PERHATIAN disertai bukti dan pendekatan lain. Setelah tiga, pindah tugas independen atau catat BLOCKED bila tidak ada tindakan aman tanpa faktor luar. Jangan loop model/perintah tanpa batas sambil menyebutnya verifikasi.

Jangan berhenti karena durasi, jumlah giliran/iterasi atau penghematan token. Checkpoint sebelum pergantian konteks. STOP pemilik berarti berhenti pada batas aman dan catat paused. Jangan memasang automation untuk mengakali session/runtime limit.

## 16. Strategi tes yang wajib

Gunakan runner existing. Pastikan tes menguji domain/bridge yang dikemas, bukan mesin finansial tiruan. Data fixture hanya pada QA yang diberi label.

A. Unit/domain: uang, clock dual-rate, order, FX, karier, fee, mandat, risiko, perusahaan/pegawai, koleksi, prestige, data/replay dan save.
B. Invariant/integrasi: akun dan ledger lintas fitur, event/calendar, normal advance/skip/offline, retry/idempotency, expiry, dataset consistency.
C. Boundary/adversarial: nol/negatif/NaN/Infinity/overflow, rounding, insufficient cash, stale quote, unauthorized account, harga gap, partial fill/cancel, deadline, leap day/timezone, corrupt/write failure, long offline.
D. Earned journey: pemain baru → tutorial → transaksi → P/L/ledger → misi/promosi → klien/firma → bisnis/pegawai/koleksi/rival → histori/lab → save/load → lanjut setelah rugi dan target tertinggi.
E. Browser: semua route/action/state wajib, lima root, bentuk angka sesuai snapshot, form, chart, focus, modal, scroll, 200% text, error/recovery, navigasi back dalam web.
F. Visual: buka screenshot output dan bandingkan dengan PNG referensi pada viewport setara. Screenshot tanpa diperiksa tidak meluluskan UX.
G. Build/package: syntax, JVM tests, lint, assemble, signature, asset identity, app ID/version, SHA-256.
H. Native: source review/compile boleh; instalasi, native bridge runtime, Back/IME/insets/TalkBack/lifecycle/device performance tetap NOT_TESTED bila tidak dijalankan pada Android. Jangan mengganti bukti ini dengan browser.

Untuk operasi berurutan kompleks, gunakan beberapa seed tetap yang dicatat dan invariant setelah setiap langkah. Fuzzing/urutan acak boleh dengan seed dapat direproduksi; jangan sekadar menambah jumlah iterasi tanpa assertion yang tepat. Satu bug regression harus dapat gagal sebelum fix dan lulus sesudahnya bila praktis.

Batas minimal fase kritis sudah ditetapkan pemilik: 10 variasi × 100 aksi bermakna dengan invariant setiap langkah, ditambah seluruh boundary/fault wajib. Jumlah ini tidak menggantikan coverage perilaku, earned journey atau review. Performa dan seluruh kombinasi UI memenuhi standar fase; bila gagal, perbaiki dan ukur/uji ulang pada kontrak yang sama.

Tingkat bug:
- Kritis: kehilangan/duplikasi dana/save, state ekonomi rusak, akses akun tidak sah.
- Mayor: alur wajib terblokir, perhitungan waktu/biaya salah, fitur wajib tidak berfungsi, recovery mustahil.
- Minor: detail UI/copy yang tidak menghalangi alur; tetap wajib diperbaiki dan diuji ulang sebelum fase lulus. Pemilik menetapkan nol bug terbuka diketahui, termasuk minor.

Jangan menyatakan minim bug hanya dari jumlah tests. Laporkan cakupan, hasil, bug terselesaikan, risiko tersisa dan keterbatasan lingkungan.

## 17. Toolchain dan host QA yang sudah ada

Periksa toolchain aktual, jangan menginstal ulang yang sudah tersedia. Contoh perintah existing dari root produksi:

~~~powershell
$ErrorActionPreference = 'Stop'
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:JAVA_TOOL_OPTIONS = '-Djdk.net.unixdomain.tmpdir=C:\gradle-socket-fallback-do-not-create'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\android\gradlew.bat -p android --no-daemon testDebugUnitTest lintDebug assembleDebug --console=plain
if ($LASTEXITCODE -ne 0) { throw 'Gradle gagal; periksa hasil sebelum melanjutkan.' }
node --check android/app/src/main/assets/www/app.js
if ($LASTEXITCODE -ne 0) { throw 'Pemeriksaan sintaks JavaScript gagal.' }
git diff --check
if ($LASTEXITCODE -ne 0) { throw 'Pemeriksaan diff gagal.' }
~~~

Override environment hanya proses tugas. Workaround JBR berasal dari lingkungan existing; verifikasi bila berbeda, jangan mengubah konfigurasi global.

Periksa exit code setiap child command segera sesudah command tersebut, termasuk runner Python dan alat signature. Jangan memakai exit code perintah terakhir sebagai bukti semua command sebelumnya berhasil. PowerShell ErrorActionPreference saja tidak menggantikan pemeriksaan exit code program native. Periksa juga hasil/XML/jumlah tes executed, failure/error/skipped dan timestamp artefak; cache atau APK lama yang masih ada bukan bukti build baru sukses. Jangan menghapus artefak kegagalan agar laporan tampak bersih.

Preview pribadi pernah berjalan di http://127.0.0.1:8766/ dengan save Artifacts/LocalPreview/preview-save. Jangan mengasumsikan proses masih hidup atau menguji startNewGame/reset di sana. Jangan menghentikan proses hanya karena berada pada port yang kamu inginkan tanpa mengidentifikasi kepemilikannya.

Host QA existing terisolasi memakai port 8767:
- Artifacts/UI-Refresh-2026-10-20/start_qa.py.
- Artifacts/UI-Refresh-2026-10-20/UiQaDomainServer.java.
- Artifacts/UI-Refresh-2026-10-20/bridge-init.js.
Ketiganya disertakan pada manifest bahan. Host ini menggunakan domain dan bridge Java produksi, tetapi bukan Android. Launcher bergantung pada build classes/JDK/cache JSON existing dan memakai save QA terpisah. Baca sebelum menjalankan; jangan mengganti jalurnya ke save pribadi. Perluas harness secara minimal untuk metode baru, jangan membuat mesin tiruan.

Setelah host QA siap dan identitas testFixture/QA HOST JAVA terverifikasi:
~~~powershell
python Work/verify-portrait-ui.py
if ($LASTEXITCODE -ne 0) { throw 'Regresi UI gagal.' }
python Work/verify-portrait-flows.py
if ($LASTEXITCODE -ne 0) { throw 'Regresi flow gagal.' }
~~~

Kedua runner dapat mereset save QA. Jangan hapus guard testFixture agar bisa diarahkan ke preview pribadi. Sesuaikan selector/nama menu dengan keputusan baru secara sah; pertahankan makna assertions. Bukti output lama jangan ditimpa sebagai baseline tanpa versi baru.

Baseline laporan lama: 158 tes JVM/15 suite, pemeriksaan browser UI/flow, APK debug 627.315 byte dengan hash tercatat dalam verification.json. Itu subset historis, bukan bukti seluruh PRD atau hasil mandat baru. Jalankan baseline aktual dan laporkan perubahan. Tidak ada target hitungan tes yang boleh menggantikan coverage requirement.

## 18. Checkpoint, laporan dan pengendalian perubahan

Gunakan Work/STATUS.json, matriks existing serta FEATURE-ACCEPTANCE.csv sebagai bahan awal. Jangan membuat banyak sumber status yang bertentangan; pilih satu matriks operasional dan tautkan yang lain. Jangan mempertahankan status PASS lama tanpa verifikasi terhadap perubahan terakhir.

Setiap checkpoint memuat:
- Root/branch/HEAD dan exact session ID bila tersedia.
- Selesai x/14 paket existing sesuai acceptance penuh; rincian scope penyerahan yang native-nya ditangguhkan dipisahkan.
- Subtask/requirement selesai, berjalan dan pending.
- File berubah, agent pemilik, kontrak/dependensi dan integrasi yang selesai.
- Aturan baru yang diputuskan dan alasannya.
- Command, environment, hasil tes, log/screenshot, source revision/hash.
- Bug: reproduksi, akar masalah, dampak, fix, regression check dan hasil ulang.
- Blocker nyata, upaya berbeda dan tugas independen yang masih bisa dikerjakan.
- Next_action konkret, bukan "lanjutkan nanti" tanpa penunjuk.

Commit lokal setelah patch ditinjau dan diuji. Stage file tugas secara spesifik; jangan git add seluruh repo tanpa memeriksa isinya. Jangan commit save, keys, secrets, dependency cache, binary besar atau data tanpa hak. Jangan push/publish.

Simpan evidence per task/run dalam Artifacts/ dengan sumber dan timestamp; laporan final di Work/. Jangan menyalin log PASS dari run lama sebagai run baru. Hash dan revision harus mengacu pada kode yang benar-benar diuji.

## 19. Gate penyerahan yang harus diaudit, bukan diasumsikan

Status COMPLETE_FOR_APK_HANDOFF hanya boleh diberikan setelah semua berikut terbukti:
1. Seluruh requirement wajib dan fitur gabungan yang disepakati memiliki implementasi UI → bridge → domain → save, serta tes/journey yang tepat.
2. Seluruh teks requirement existing dipertimbangkan; adaptasi/keputusan kandidat referensi terdokumentasi, tidak ada fitur wajib yang disembunyikan sebagai future work.
3. UI final mengikuti layout referensi, lima tab baru, token konsisten, angka domain, control/state bekerja, responsive dan aksesibilitas browser lulus.
4. Waktu aktif 5 menit/hari dan tidak aktif 1 jam/hari, transisi, catch-up, Skip, biaya/risiko dan idempotensi terbukti melalui domain serta integrasi. Tidak ada auto-pause lifecycle atau offline sama cepat dengan foreground.
5. Perusahaan otomatis sesuai kebijakan, koleksi ekonomi, NPC ranking, progres seimbang, pemulihan tanpa uang otomatis dan permainan tanpa tamat terbukti.
6. Replay historis nyata dengan hak/sumber memadai berfungsi end-to-end; tidak diganti fixture.
7. Seluruh gate kualitas fase pada scope handoff lulus, termasuk UI seluruh state/viewport/teks 200%, minimal 10×100 aksi per fase kritis, threshold performa dan review setiap fase. Tes logika/integrasi/browser/regresi/syntax/lint/build relevan lulus. Warning ditriage; **nol bug terbuka yang diketahui termasuk minor**, tanpa blocker fitur wajib. Bukti terintegrasi final cocok source APK.
8. Progres pribadi terjaga; schema/migrasi/backup/recovery diuji pada salinan, termasuk anchor offline legacy.
9. APK final dibangun dari source terakhir yang diverifikasi, app ID/version/signature cocok, assets yang dikemas cocok, checksum dan ukuran tersedia.
10. Laporan final menjelaskan apa yang diuji, adaptasi, keputusan, risiko dan native gates yang ditangguhkan. Instalasi/native/device tidak diklaim PASS.

Sebelum menyatakan selesai, perlakukan completion sebagai belum terbukti. Audit tiap requirement, named artifact, command/test/gate dan deliverable terhadap evidence aktual. Tes yang sempit tidak membenarkan klaim luas. Jika evidence hilang/tidak langsung, lanjutkan pekerjaan atau nyatakan PARTIAL/BLOCKED yang tepat.

Jangan menandai seluruh 14 paket/gate Android penuh COMPLETE jika masih mengandung acceptance native yang belum diuji. COMPLETE_FOR_APK_HANDOFF membuktikan batas penyerahan yang dipilih pemilik, bukan siap publikasi/perangkat teruji.

Jika wajib historis/legal data/fitur/logika masih terblokir, tugas belum lengkap walaupun APK build hijau. Teruskan tugas independen dan laporkan gap secara konkret. Tidak menyatakan selesai karena lelah, waktu, jumlah iterasi atau dokumen sudah panjang.

## 20. Artefak akhir yang harus diserahkan

- Source final yang terintegrasi dan commit lokal yang dapat diperiksa.
- Matriks requirement/fitur/layar/journey beserta evidence, tanpa PASS palsu.
- Catatan aturan ekonomi, sesi, waktu foreground/background, balancing dan keputusan adaptasi.
- Daftar bug/reproduksi/akar masalah/fix/regression result.
- Bukti domain/integrasi/browser/visual serta command/environment/source revision.
- Manifest dataset/provenance/lisensi/atribusi/checksum dan batas data.
- APK final di Artifacts/release/, ukuran, SHA-256, application ID/version, signing status dan inspeksi asset.
- Ringkasan laporan di Work/ dengan path evidence, known issues dan gate native pending.
- Panduan pemain ringkas untuk navigasi, trading, risiko, clock aktif/offline, Skip, bisnis, save dan pemulihan.
- Instruksi membuka preview/QA dan pemasangan APK mendatang tanpa mengklaim instalasi sudah dilakukan.
- Checkpoint akhir yang memungkinkan agen lain melanjutkan dari state nyata.
- Laporan gate setiap fase: seluruh kriteria/bukti, bug closure/retest, seeded sequences, matriks UI, raw performance dan hasil review/keputusan koordinator. Standar belum terpenuhi berarti belum selesai.

## 21. Mulai eksekusi sekarang

Buktikan root/baseline, jalankan verifikasi bahan, baca spesifikasi lengkap, lindungi progres dan inventaris gap. Bentuk pembagian agen sesuai kemampuan aktual. Susun urutan kerja konkret dengan acceptance dan dependensi, kemudian langsung implementasikan gap pertama sampai teruji.

Jalankan loop implementasi → pengujian → inspeksi → diagnosis → perbaikan → uji ulang → review → checkpoint → lanjut. Jangan meminta persetujuan rutin lagi dan jangan berhenti pada perencanaan. Gunakan seluruh mandat ini sampai batas penyerahan terbukti tercapai atau hambatan eksternal nyata terdokumentasi.
