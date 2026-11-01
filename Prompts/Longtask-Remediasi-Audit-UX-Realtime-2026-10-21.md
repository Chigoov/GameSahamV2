# Prompt Hermes: tuntaskan koreksi audit UX, realtime, pengujian dan APK

Saya pemilik proyek. Lanjutkan Game Saham sebagai long task untuk memperbaiki temuan audit atas commit `6d3a44e`. Kerjakan implementasi, pengujian perilaku, review, koreksi tracker dan build APK baru. Jangan berhenti pada analisis, rencana, daftar saran atau tes yang hanya memastikan komponen terlihat. Pertahankan hasil pekerjaan yang sudah benar.

## 1. Root, mandat dan perlindungan baseline

- Root produksi yang boleh ditulis: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`. `Game Trading Saham` adalah repo historis berbeda; jangan menulis atau mengimpor source/save/build darinya.
- Buktikan cwd, Git root, branch, HEAD, tracked/untracked changes, source fingerprint, proses/writer aktif, port dan save QA sebelum perubahan. Baseline saat prompt dibuat adalah `6d3a44e`; jika HEAD sudah berubah, audit delta terlebih dahulu. Jangan mengembalikan source agar cocok dengan nomor baris lama.
- Baca `AGENTS.md`, `Work/README.md`, konsep/PRD/design brief, prompt master serta paket standar kualitas yang dirujuk AGENTS. Lalu baca `Prompts/Longtask-UX-Motion-Realtime-100-Perusahaan-2026-10-21.md`, `Work/CHECKPOINT-UX-MOTION-REALTIME-2026-10-21.md`, `Work/STATUS.json`, dan `Work/ASSET-REQUESTS-UX-MOTION-REALTIME-2026-10-21.md`.
- Pertahankan semua local changes, termasuk empat dokumen prompt/checkpoint/catatan yang belum dilacak Git. Jangan reset, clean, delete massal, atau memasukkan file di luar tugas ke commit.
- Save pribadi: `Artifacts/LocalPreview/preview-save/save_game_v2.json`. SHA-256 baseline: `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`. Verifikasi sebelum/sesudah. Jika berubah, diagnosis; jangan overwrite. Jangan memakai save ini atau preview port8766 untuk tes mutasi.
- Baseline APK: `Artifacts/UX-Motion-Realtime-2026-10-21/apk/GameSaham-v1.0.0-ux-motion-realtime.apk`, 952643 byte, SHA-256 `382f0ff200942aafb173a31114a722084a2d4ba042d1fc6b2be73fa387a57ade`. Pertahankan sebagai bukti sebelum perbaikan.
- Pertahankan katalog 100 perusahaan aktif, 50 IDX + 50 US, 12 kategori, nol nonaktif; pencarian/filter/watchlist; migrasi save katalog v1/unversioned ke v2; validasi IPO dan inventory NPC sekali saja. Baseline 461 tes/55 suite adalah catatan hasil terdahulu, bukan target jumlah tes.

## 2. Batas implementasi dan cara bekerja

Produksi tetap Java + Android WebView + HTML/CSS/JS lokal. Domain Java satu pemilik uang, order, ledger, waktu dan save. JavaScript hanya presentasi/input. Gunakan fungsi/runner/dependency existing, lalu standard library/native platform; jangan menambah engine, framework, event bus, lapisan abstraksi atau dependency baru tanpa kebutuhan nyata. Telusuri caller dan perbaiki akar masalah di fungsi bersama. Satu runnable regression check yang bermakna untuk logika nontrivial; gunakan runner existing.

Pertahankan application ID `com.tradingglobal3d.mobile`, signing identity dan kompatibilitas save. Pertahankan aturan waktu aktif/background dari mandat produk. Jangan mengubah frekuensi simulasi untuk mengejar benchmark atau membuat gerakan UI.

Multi-agent sesuai kemampuan nyata dengan satu koordinator dan satu writer per file/shared state/output build. Reviewer berbeda melakukan review baca-saja atas delta dan bukti; jangan menjalankan writer/launcher paralel pada sesi sama. Jika kemampuan tersebut tidak tersedia, catat batasnya tanpa mengarang review.

Wewenang mencakup perubahan repo, tes domain/browser, build APK, laporan dan commit lokal yang ditinjau. Jangan push/publish, mengirim pesan pihak lain, membeli layanan, mengubah profil/provider/model/credentials/global Hermes, menambah scheduler, memakai ADB/emulator/perangkat fisik atau menyentuh data/repo lain. Native tetap NOT_TESTED/DEFERRED untuk penyerahan ini.

## R1. Verifikasi temuan dan koreksi status

Buat tabel temuan ID, reproduksi, akar masalah, fungsi/caller, perbaikan, regression check, bukti dan hasil reviewer. Nomor baris berikut adalah petunjuk baseline; cocokkan simbol/source aktual. Temuan yang ternyata tidak berlaku harus dibantah dengan bukti perilaku.

Tracker kini menyebut `COMPLETE / PRODUCTION_VERIFIED_COMPLETE` sementara aset pending dan temuan masih terbuka. Koreksi ringkasan status/next_action secara konsisten tanpa menghapus riwayat atau menurunkan pekerjaan lama yang memang sudah terbukti. Bedakan selesai untuk subpekerjaan host, selesai untuk mandat UX ini, dan penerimaan seluruh produk/14 fase. Jangan mewariskan PASS dari laporan lama sebagai bukti perubahan baru.

## R2. Jumlah saham dan notifikasi transaksi

Temuan: `draft.quantity` sudah dalam lembar saham (`totalShares`, sekitar app.js3894–3901). `showOrderReview` menutup tiket, `closeOrderTicket` mengosongkan `selectedInstrument`, kemudian toast FILLED/OPEN mengalikan jumlah itu lagi dengan lotSize/fallback100 (sekitar2151/2155).

- Perbaiki satuan pada seluruh jalur notifikasi BUY/SELL, OPEN/PARTIAL/FILLED dan pembaruan receipt. Jangan mengubah kuantitas transaksi domain untuk menyesuaikan teks toast.
- Uji IDX satu lot=100 saham dan saham US sesuai lotSize domain. Cocokkan tiket, review, hasil domain, receipt dan notifikasi untuk jumlah order/terisi/sisa. Jangan menyamakan jumlah dipesan dengan jumlah terisi.
- Review deduplikasi `lastToastKey`: satu teks global selama3,5detik belum memenuhi identitas kejadian. Reuse request/order/event/revision ID yang tersedia. Refresh, retry, pergantian tab dan restore tidak boleh mengulang kejadian sama; dua kejadian sah dengan teks sama harus tetap diumumkan. Hindari subsistem event baru bila kontrak existing cukup.

## R3. Pembaruan otomatis yang lengkap dan akurat

Makna realtime: setelah perubahan dikonfirmasi domain, semua tampilan terkait yang sedang terbuka memperlihatkan hasil benar tanpa refresh, pindah tab atau buka ulang. Polling UI boleh mengikuti mekanisme existing; tidak membuat clock/ledger kedua. Target perubahan lokal terkonfirmasi P95≤300ms sesuai standar; settlement tertunda diukur dari domain commit ke tampilan, dengan fase domain/transport/render dibedakan. Jangan mengklaim waktu tunggu antrean sebagai render lambat atau dana tertahan sebagai hasil penjualan.

1. **Portofolio:** `syncPortfolioLive` sekitar346–421 hanya memperbarui IDR meski layar merender IDR dan USD. Sinkronkan kedua mata uang serta valuasi, posisi dan kewajiban sesuai akun aktif. Callback detail posisi masih menangkap objek lama ketika jumlah row tetap; gunakan identitas posisi/data terbaru sehingga angka daftar dan rincian selalu konsisten. Periksa pergantian identitas posisi dengan jumlah row sama. Detail posisi yang dibiarkan terbuka juga harus mengikuti data terbaru.
2. **Bisnis:** nilai kas sekitar885 dibentuk saat load; refreshSnapshot/polling belum menyinkronkan Bisnis. Perbarui tampilan bisnis/perusahaan yang sedang terbuka setelah penjualan/pendapatan, biaya, payroll dan pergantian hari sesuai mekanisme domain. Jangan menciptakan penjualan sintetis atau menjanjikan semua pemasukan seketika jika aturan settlement berbeda. Bedakan uang pribadi/perusahaan/klien.
3. **Detail instrumen:** `updateInstrumentDetailLive` sekitar463–529 belum mengosongkan holdings jika posisi hilang. Tangani posisi nol/habis terjual. Sinkronkan harga, perubahan, chart/OHLCV, tape, order book, halt/status dan enable/disable Beli/Jual berdasarkan state domain terbaru. Grafik/tape berasal riwayat game, bukan data dekoratif.
4. **Receipt:** verifikasi OPEN→PARTIAL→FILLED, beberapa partial fill tanpa perubahan nama status, cancel, reject/expired dan retry. Perbarui field yang tersedia dalam kontrak domain, termasuk terisi/sisa/biaya/hasil, serta tindakan yang masih sah. Tidak boleh menampilkan sukses sebelum domain mengonfirmasinya.
5. **Konsistensi UI:** cek Beranda, daftar order, detail posisi dan layar keuangan terdampak; pertahankan draft, input, fokus, scroll, filter dan quote review. Hindari render ulang seluruh layar setiap tick. Audit pemanggilan updater ganda setelah refreshSnapshot dan hapus yang redundan bila kontraknya sama.
6. **Resume/recovery:** uji kembali dari background, load/restore save QA, pergantian akun dan run baru. Tidak boleh mencampur state, posisi, progres atau notifikasi antar-akun/run. Periksa penanganan preview expiry lokal30detik terhadap kontrak quote/domain; koreksi jika tidak sesuai, tanpa menghidupkan clock finansial JavaScript.

## R4. Aksesibilitas, motion dan visual

- `showFlow` sekitar1778 membuat body children inert, termasuk `#toast-container`; jalur tiket sudah mengecualikannya. Perbaiki shared modal/flow handling agar live region dapat diumumkan saat receipt/detail terbuka, sementara background interaktif tetap inert. Uji fokus masuk/keluar, Tab/Shift+Tab, Escape/Back browser yang applicable. Pemeriksaan DOM/aksesibilitas browser diberi label sesuai cakupannya; jangan mengklaim TalkBack Android sudah diuji.
- Uji prefers-reduced-motion sistem, saklar pengaturan dan power-save; tidak ada animasi wajib untuk memahami transaksi. Motion tidak mengubah uang/clock maupun memicu pengumuman berulang.
- Periksa klaim semua alert telah dihapus; masih ada jalur loan/mission memakai alert pada baseline. Gunakan pola feedback existing yang sesuai tanpa menghapus konfirmasi/tindakan penting. Jika sengaja dipertahankan, laporkan alasannya dengan akurat.
- Periksa CSS `.hero-cash strong`/label yang memakai ellipsis. Angka keuangan, unit, nama akun dan CTA harus dapat dibaca; document.scrollWidth yang kecil saja tidak membuktikan tidak ada clipping. Uji juga overflow internal, kontras dan target sentuh48dp.
- Pertahankan token dan tiga referensi pemilik; jangan mengganti tema atau menambah fitur crypto/uang nyata dari gambar referensi.

## R5. Ganti bukti tes yang lemah dengan pengujian perilaku

`Work/test-live-sync-browser.py` menyimpan kas UI awal tetapi tidak membandingkannya; pemeriksaan utama membaca snapshot domain lalu hanya menguji kas berbeda. Navigasi ke Portofolio membangun ulang layar, sehingga tidak membuktikan pembaruan saat layar tetap terbuka.

Gunakan save QA terisolasi dengan seed/state deterministik, server/port yang kepemilikannya terbukti, domain Java nyata dan aset produksi. Label host-browser QA secara jelas; fixture hanya untuk setup, bukan mesin finansial palsu. Jangan menyentuh save pribadi atau memakai bypass mutasi produksi.

Tambahkan/perbaiki regresi minimum yang membuktikan temuan R2–R4 sebelum dan setelah perbaikan. Assertion harus membandingkan nilai DOM dengan nilai domain yang benar, bukan sekadar visible, saldo berbeda atau screenshot ada. Minimal cakup BUY/SELL IDX dan US, USD, antrean/partial/fill/cancel, posisi menjadi nol, rincian posisi terbaru, pergantian akun, pendapatan/biaya bisnis pada pergantian hari, resume, dedup dan draft/fokus/scroll. Biarkan layar target tetap terbuka saat domain berubah. Simpan bukti sebelum/sesudah dan identitas order/revisi/waktu. Tunggu kondisi bermakna dengan timeout, bukan sleep sebagai bukti sinkronisasi.

Jalankan regresi domain penuh yang applicable; angka461 adalah baseline. Jalankan pemeriksaan browser existing yang relevan dan seluruh state berubah pada7viewport (320×568,360×640,390×844,412×915,430×932,768×1024,1440×900) × font100%/200% nyata. Buka dan inspeksi screenshot, jangan hanya menghasilkan berkas. Rekam pageerror/unhandled rejection/dialog tak diharapkan. Ikuti10×100aksi untuk jalur finansial kritis sesuai standar existing dengan invariant yang bermakna.

Jangan menyebut266flow/70viewport/24journey sebagai seluruh penerimaan produk: laporkan cakupan state dan batas yang benar-benar diperiksa.

## R6. Perbaiki benchmark dan logika kelulusannya

Di `Work/benchmark-performance-final.py` sekitar220 terdapat `p95 <= 300 or len(samples) > 0`. Ini bisa meluluskan pekerjaan lambat hanya karena sampel ada. Perbaiki akar masalah serta seluruh summary/verdict/assert/exit code yang membaca hasilnya.

- Cek minimum runnable: sampel lambat harus FAIL; sampel kosong/kurang tidak boleh PASS; hasil yang memenuhi seluruh syarat boleh PASS. Verdict tidak boleh hardcoded.
- Tetapkan jenis aksi sebelum mengukur. Minimal30sampel per jenis yang diwajibkan. Jangan menggabungkan aksi cepat untuk menutupi jenis lambat; laporkan P95 per jenis, maksimum dan rawtiming. Simpan timeout/outlier dan penyebabnya.
- Aksi biasa P95≤300ms per jenis; seluruh sampel navigasi≤1000ms; feedback pekerjaan berat benar-benar tergambar≤300ms dan event loop tetap responsif. Pisahkan feedback awal dari hasil akhir; ukur keduanya. Waktu page.evaluate/bridge saja tidak membuktikan paint atau UI responsif.
- Rekam environment, source fingerprint, seed, cache/warmup, beban pembaruan otomatis dan katalog100 aktif. Jika setup host tidak dapat mengukur suatu kontrak secara sah, laporkan NOT_TESTED/PARTIAL dan alasannya, jangan mengarang angka/PASS.

## R7. Aset Codex, bukti, APK dan penutupan

Pembuatan ilustrasi raster tetap oleh Codex sesuai permintaan pemilik. Hermes mengaudit kebutuhan nyata, memperbaiki mapping asset ID→objek domain→selector/path yang salah, mempertahankan slot/fallback dan mengintegrasikan aset yang benar-benar sudah tersedia. Jangan mengaku19gambar sudah dibuat, memakai placeholder sebagai aset final, menambah objek gameplay agar cocok dengan daftar gambar, atau menghentikan pekerjaan independen sambil menunggu gambar. Gunakan status REQUESTED/AVAILABLE/REVIEWED/INTEGRATED dengan bukti file. Jika aset diperlukan masih belum ada, status keseluruhan mandat UX tetap PARTIAL / ASSETS_PENDING_CODEX; jelaskan subpekerjaan host yang sudah lulus.

Simpan bukti run baru dalam direktori unik di bawah `Artifacts/Remediasi-UX-Realtime-2026-10-21/` agar baseline tidak ditimpa. Buat checkpoint/handoff baru yang ditautkan dari tracker. Reuse tracker existing, jangan membuat sistem pelacakan paralel.

Sesudah perbaikan dan review lulus, build APK baru dari source akhir. Verifikasi ZIP, signing certificate/identity terhadap baseline, application ID/version, kecocokan byte aset produksi/katalog,100aktif50IDX50US12kategori, tidak ada fixture/save QA masuk paket, kompatibilitas save dan SHA pribadi awal/akhir. Beri path, ukuran byte, SHA256, source fingerprint/commit dan batas pengujian. APK disebut kandidat bila gate/aset masih pending. Jangan menimpa baseline atau menyebut sudah terpasang/berjalan di HP tanpa bukti.

Reviewer meninjau diff dan kecukupan assertion/bukti serta membuka kembali temuan jika belum tertutup. Target nol bug terbuka diketahui termasuk minor; jangan mempersempit acceptance agar hijau. Keterbatasan native yang memang di luar scope dicatat terpisah.

## Penyelesaian dan laporan akhir

Kerjakan satu hasil terukur→tes→review→checkpoint→lanjut. Tidak perlu persetujuan rutin dalam scope. Jangan berhenti karena durasi, iterasi, penghematan token atau satu tes hijau. Setelah dua kegagalan sama tanpa diagnosis baru, kirim PERLU PERHATIAN dan ganti pendekatan; setelah tiga, kerjakan bagian independen atau BLOCKED jika benar-benar membutuhkan tindakan luar. Hormati STOP/pause dan simpan checkpoint aman.

Laporan akhir bahasa Indonesia harus menjelaskan: R1–R7 selesai/parsial/pending; temuan dan perbaikan dengan bukti; apakah uang hasil penjualan benar-benar terlihat bertambah saat layar tetap terbuka serta aturan settlementnya; hasil tes dan benchmark per jenis; keterbatasan host/native; screenshot yang diinspeksi; aset tersedia/terintegrasi/pending; save compatibility; APK/path/ukuran/SHA/source; hasil reviewer dan next_action. Laporkan x/14 hanya berdasarkan acceptance yang terbukti; selesainya R1–R7 bukan otomatis14/14.

COMPLETE untuk mandat ini hanya bila seluruh kriteria applicable terbukti pada source akhir, reviewer menutup temuan, tracker konsisten dan aset yang diperlukan terintegrasi. Jika gambar masih menunggu Codex, serahkan semua hasil independen yang sudah selesai dengan PARTIAL / ASSETS_PENDING_CODEX. Jika gate lain gagal, sebutkan terpisah. Jangan menjanjikan tidak ada bug tersembunyi atau nol kehilangan progres secara mutlak.

Mulai sekarang dengan memverifikasi root/baseline/save dan membaca sumber keputusan, lalu langsung lakukan R1 dan perbaikan pertama. Jangan berhenti setelah menuliskan rencana.
