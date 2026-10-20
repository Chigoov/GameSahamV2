# Mandat lanjutan: perbaikan integrasi, penuntasan fitur, dan verifikasi APK

Anda melanjutkan pekerjaan yang sudah ada. Bertindak sebagai koordinator implementasi dan QA. Eksekusi pekerjaan, bukan hanya menulis rencana. Jangan membangun ulang game, mengganti desain yang sudah dipilih, atau mengulang workflow desain historis tanpa kebutuhan nyata. Baca source dan bukti terbaru, lalu perbaiki bagian yang masih gagal.

## 1. Root dan sumber keputusan

Satu-satunya root yang boleh ditulis:
`C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`

Repo `C:/Users/HYPE AMD/Downloads/VIBE CODING/Game Trading Saham` adalah referensi historis yang berbeda. Jangan menulisnya atau mengimpor source/save/build darinya. Buktikan cwd, Git root, branch, HEAD dan working tree sebelum perubahan. Pertahankan semua perubahan agen sebelumnya. Jangan reset, clean, checkout paksa, atau menghapus pekerjaan lokal.

Baca berurutan:

1. `AGENTS.md` di root produksi.
2. `Artifacts/Independent-Review-2026-10-21/REPORT.md` dan `ui-contract-probe.log` di direktori yang sama.
3. `Work/CHECKPOINT-HERMES-SESSION2-2026-10-20.md`.
4. `Work/CHECKPOINT-CODEX-2026-10-20.md` dan `Work/STATUS.json`.
5. `Work/Hermes-Feature-Completion-2026-10-20/README.md`, `SPECIFICATION.md`, `GAP-MATRIX.csv`, dan `START-HERMES.md`.
6. `Work/Hermes-Handoff-2026-10-20/README.md`, `PHASE-QUALITY-STANDARDS.md`, dan `Prompts/Hermes-Final-UI-Gameplay-Multi-Agent-2026-10-20.md`.
7. Dokumen produk/desain dan task fase yang diwajibkan oleh AGENTS.md untuk bagian yang disentuh.

Jika path tidak ditemukan, cari nama file di root ini. Jangan mengarang isinya. Tanda baca/nama fase dan angka bukti lama harus dicocokkan dengan kondisi aktual. Prompt ini menetapkan urutan remediasi dan bukti; scope produk lengkap tetap mengacu dokumen tersebut. Jangan menghidupkan sesi Codex/Kiro/Hermes lama secara otomatis atau menjalankan dua koordinator/writer pada checkout yang sama.

## 2. Baseline yang harus diverifikasi, bukan diasumsikan

Audit independen 21 Oktober sekitar 00:24 WIB menemukan:

- Branch `codex/owner-audit-portrait-v8`, HEAD `f467607c9c2fc68d16cbea135351ba9e6bc74796`; perubahan implementasi masih lokal.
- Uji ulang Gradle: 37 suite, 361 tes, nol gagal/error/skipped. Kelulusan ini belum mencakup seluruh perjalanan UI.
- APK debug lama: `android/app/build/outputs/apk/debug/app-debug.apk`, 805534 byte, signature v2 valid, SHA-256 `131c476af773adcaf5d73ec88e8a2ce379e0bfece73cdf228b2cda6618bac2e1`.
- Save pribadi `Artifacts/LocalPreview/preview-save/save_game_v2.json`, SHA-256 `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`.
- Snapshot tracker: REQUIREMENTS 225 NOT_TESTED dan 3 PASS, SCREEN-COVERAGE 26 NOT_TESTED, GAP-MATRIX 148 NOT_TESTED. Ini status bukti, bukan penghitungan fitur yang pasti tidak ada.

Jika kondisi sudah berubah, catat baseline baru dan penyebabnya sebelum melanjutkan. Jangan menimpa laporan audit independen atau log agen sebelumnya.

## 3. Aturan implementasi dan perlindungan

- Produksi tetap Java + native Android WebView + HTML/CSS/JS lokal. Java pemilik tunggal saldo, ledger, harga, order, karier, XP, kalender, progres, dan save. JavaScript hanya input/presentasi.
- Pertahankan portrait, pola UI aplikasi referensi yang sudah disukai, konten dapat scroll dan navigasi bawah tetap. Lima tab: Beranda, Pasar, Portofolio, Bisnis, Lainnya. Gunakan token desain bersama; jangan redesign seluruh aplikasi.
- Aktif 1×: 24 jam game = 5 menit nyata. Background/ditutup: 24 jam game = 1 jam nyata, tidak mewarisi speed foreground. Aktif, offline, dan Skip Hari memakai aturan ekonomi/kalender yang sama.
- Game berlanjut tanpa tamat. Prestige opsional, dampak reset dan data yang dipertahankan wajib ditampilkan sebelum konfirmasi. XP/badge tidak memberikan kas atau melewati kunci karier.
- Pahami alur dan semua caller, gunakan helper/pola yang sudah ada, perbaiki akar masalah di tempat bersama. Jangan menambah engine, framework, dependency, atau abstraksi spekulatif.
- Jangan melonggarkan validasi untuk membuat UI lolos. Jangan menghapus tes gagal, mengubah expected menjadi perilaku salah, atau mengganti produksi dengan mock.
- Buat save/akun QA terisolasi. Jangan menjalankan mutasi, replay, catch-up, reset, atau prestige pada save pribadi. Verifikasi hash save pribadi sebelum dan setelah pekerjaan. Jika berubah tanpa sengaja, hentikan writer dan pertahankan bukti serta backup; jangan menimpa lagi secara membabi buta.
- Pertahankan application ID `com.tradingglobal3d.mobile`. Jangan mengganti signing key atau mengklaim cocok identitas rilis lama tanpa pembuktian sertifikat.
- Dilarang push/publish, membayar layanan, mengirim pesan pihak lain, mengubah profil/provider/model/credentials atau konfigurasi global agen, serta membuat scheduler/launcher global.
- Untuk tugas ini jangan ADB, emulator, instalasi, atau mengoperasikan HP. WebView/IME/Back/lifecycle/performa native tetap gate perangkat terpisah. Build APK dan tes browser bukan bukti perangkat.

## 4. Pembagian multi-agent

Gunakan multi-agent bila benar-benar tersedia. Koordinator menentukan kontrak dan kepemilikan file sebelum kerja paralel; satu writer per file dan per save QA. Pilihan pembagian:

- Agen A: UI prestige/pinjaman, aksesibilitas, dan perjalanan browser.
- Agen B: domain return liga/rival, backing reksa dana, dan regresi keuangan.
- Agen C: reviewer/QA independen, audit kontrak, matriks acceptance, dan pemeriksaan bukti.
- Koordinator: file shared seperti TradingFacade/TradingWebBridge, integrasi, regresi akhir, tracker, commit dan APK.

Pembagian dapat disesuaikan kemampuan alat. Jangan membiarkan beberapa agen mengedit app.js/TradingFacade bersamaan. Agen mengusulkan kontrak lebih dahulu bila dependensi belum siap. Reviewer tiap fase berbeda dari implementor bila tersedia; semua temuan harus ditutup dan integrasi diperiksa koordinator. Jika sub-agent tidak tersedia, lakukan implementasi dan review sebagai pass terpisah yang dicatat dengan jujur. Jangan mengklaim review independen yang tidak terjadi.

## 5. Urutan eksekusi dan acceptance

Gunakan ID `AUD21-*` di bawah agar tidak tertukar dengan F1–F9 dari audit historis. Nomor baris hanyalah petunjuk snapshot, cari simbol aktual.

### R0 — Preflight terukur

Verifikasi baseline, simpan hash save pribadi, petakan alur UI → bridge → facade/domain → ledger/save. Reproduksi AUD21-01/02 menggunakan source saat ini dan QA terisolasi. Reuse runner yang ada; jangan membuat infrastruktur baru hanya untuk memulai. Rekonsiliasi status sesi aktif tanpa meluluskan requirement yang belum diuji. Setelah konteks cukup, segera implementasikan perbaikan; dokumen desain panjang bukan keluaran utama fase ini.

### R1 — AUD21-01: Prestige dari UI

Source audit: refreshSnapshot mengisi `latestSnapshot`, tetapi tombol prestige membaca `window._currentSnapshot` yang tidak diisi; runId kosong ditolak domain.

- Ambil runId dari preview/snapshot domain yang benar dan terkait layar konfirmasi yang sedang ditinjau. Jika perjalanan berubah, tampilkan penolakan dan minta review ulang; jangan mengambil runId baru diam-diam untuk menyetujui reset yang belum direview.
- Tampilkan prasyarat, blockers, reset, retained, bonus, dan backup dari kontrak domain. Hindari klaim mutlak bahwa data tidak mungkin hilang.
- Satu aksi konfirmasi mempunyai requestId stabil untuk retry. Tahan klik ganda dan tangani error dengan pesan/kode yang sesuai konteks, bukan kode error staf perusahaan.
- Acceptance: eligible success, ineligible, cancel tanpa mutasi, stale runId, klik ganda/retry, backup gagal, pemulihan, dan reload. Tidak boleh ada reset ganda atau keberhasilan palsu. Gunakan fixture QA untuk cabang ekstrem, lalu buktikan akses UI melalui perjalanan progres yang sah sesuai standar produk.

### R2 — AUD21-02: Pinjaman pribadi beragunan

Source audit: UI membaca `panel._selectedCollateralId` tanpa pemilih/assignment, sedangkan manager mewajibkan agunan.

- Sediakan pemilih koleksi canonical milik peminjam yang eligible dan belum dijaminkan. Tampilkan empty/locked/error state, nilai bersih, batas pinjaman, dan alasan ketidaklayakan.
- Gunakan quote domain untuk principal, biaya, dana bersih, tenor, eligibility dan quoteVersion. Ganti input membatalkan review sebelumnya; konfirmasi hanya berlaku untuk quote yang dilihat pemain.
- Konfirmasi memakai requestId stabil untuk retry dan expectedQuoteVersion. Penolakan stale quote tidak boleh berakhir sebagai pencairan diam-diam.
- Jangan menggunakan fallback pencairan legacy yang melewati review/agunan. Telusuri seluruh entrypoint produksi sehingga jalur lama tidak menjadi bypass; fixture/import tepercaya hanya sesuai kebutuhan test yang sah.
- Acceptance: beli koleksi → pilih agunan → quote → review → pencairan → ledger/save/reload → cicilan/pelunasan → agunan terlepas. Uji tanpa agunan, pemilik salah, sudah dijaminkan, perubahan valuasi/tunggakan, pokok/tenor batas, stale quote, retry dan save gagal. Uji produk perusahaan secara terpisah dengan batas arus kas aktual dan kewajiban berjalan. Kas dan utang tidak boleh mengganda atau menjadi uang gratis.

### R3 — AUD21-03: Return liga dan rival

Source audit: rollover, leaderboard WEEKLY_RETURN dan rival memasok return pemain konstan nol.

- Telusuri seluruh caller dan gunakan satu perhitungan canonical berbasis progres aktual serta baseline periode yang tersimpan. Jangan memindahkan perhitungan ke JS.
- Tetapkan/dokumentasikan perlakuan arus kas, utang, transfer, aset, fees, zero/negative equity, pekan parsial dan batas kalender; reuse aturan konsisten yang sudah ada.
- Rollover terjadi tepat sekali walau melewati beberapa pekan melalui offline/skip. Achievement promosi hanya muncul bila promosi benar-benar terjadi. NPC memakai nama fiktif dan hasil mengikuti aturan game yang terdokumentasi.
- Acceptance: untung, rugi, netral, transfer/utang tidak memalsukan return, seri/ranking stabil, baseline reload, batas pekan, multiweek offline/skip, rollover ulang, dan angka konsisten pada semua UI/API. Jangan mengganti nol dengan persentase acak.

### R4 — AUD21-04: Reksa dana dan kegagalan rebalance

Verifikasi implementasi yang sudah ada lebih dahulu. Audit sebelumnya baru membuktikan pemanggilan rebalance EQUITY_INDEX dan menemukan exception ditelan; backing/pemasukan MONEY_MARKET dan FIXED_INCOME belum terbukti lengkap.

- Buktikan backing aset, kas pool, kepemilikan unit, NAV/NAB, fee, utang/pending settlement dan reserved cash untuk ketiga strategi. Bila belum ada, implementasikan minimum yang sesuai spesifikasi dengan instrumen/kontrak canonical.
- Hindari keuntungan dari rumus tambahan yang tidak mempunyai aset/arus pendapatan dasar. NAV harus dapat direkonsiliasi terhadap aset dan kewajiban pool.
- Kegagalan order rebalance harus punya status/alasan yang tersimpan dan dapat diperiksa. Kegagalan yang bisa dipulihkan tidak boleh merusak cutoff, tetapi tidak boleh menghilang dalam catch kosong atau dilaporkan sukses.
- Acceptance: subscribe/redeem/cancel, cutoff batas waktu, reservasi, biaya, backing/NAB, kondisi likuiditas kurang, order ditolak/partial, retry, reload, advance aktif/offline/skip. Buktikan tidak ada unit/kas/aset tercipta tanpa sumber yang sah.

### R5 — Gap fitur dan replay yang tersisa

Cocokkan source terbaru dengan PRD dan GAP-MATRIX. Jangan mengulang temuan audit lama sebagai fakta tanpa reproduksi dan jangan menulis ulang fungsi yang sudah benar. Tuntaskan requirement wajib yang belum lolos sesuai dependency fase, termasuk perusahaan/pegawai/koleksi/misi/profil dan fitur lain pada paket yang relevan.

Replay historis tetap wajib berfungsi sesuai mandat: cari dataset nyata yang boleh digunakan tanpa membeli layanan baru. Catat sumber, cakupan, hak penggunaan/distribusi, atribusi dan bukti peninjauan; repositori publik sendiri bukan bukti hak redistribusi. Jangan mengarang izin. Dataset harian tidak boleh menyamar menjadi intraday. Uji tanggal/instrumen, resolusi, histori chart, pencegahan future leak, eksekusi, save/reload dan loader asset Android secara tepat. Jika hak/data benar-benar belum terpenuhi, pertahankan gate BLOCKED dengan alasan terukur dan lanjutkan pekerjaan independen; jangan meluluskan replay dengan fixture sintetis.

### R6 — Acceptance terintegrasi dan review

Setiap fase hanya lulus bila seluruh kriteria yang berlaku terpenuhi dan nol bug terbuka yang diketahui, termasuk minor. Jika gagal: reproduksi → diagnosis akar masalah → perbaikan → regresi relevan → uji integrasi ulang → review. Jangan menjanjikan nihil bug tersembunyi.

- Regresi lama tetap lulus, ditambah test yang menangkap bug baru. Jangan mengejar angka test sebagai ukuran kelengkapan.
- UI: semua layar/state relevan × viewport 320×568, 360×640, 390×844, 412×915, 430×932, 768×1024, 1440×900 × teks normal/200%. Pertahankan matriks applicable; N/A wajib beralasan. Inspeksi screenshot, overflow, keterbacaan angka, target sentuh setara 48 dp, fokus, reduced motion, kontras, konten terbawah dan nav tetap. Tidak perlu memaksa seluruh halaman bebas scroll.
- Uji browser memakai backend/domain Java sungguhan dengan save QA. Mock/fixture berlabel boleh untuk kebutuhan khusus tetapi tidak menggantikan bukti produksi. Buktikan klik UI → bridge → perubahan domain/ledger → save → reload; jangan berhenti pada HTTP 200 atau tombol terlihat.
- Uang/order/waktu/save: minimal 10 variasi urutan ×100 aksi bermakna dengan seed dan invariant setelah setiap aksi, ditambah kasus batas, proses terputus dan kegagalan save. Klik/no-op berulang bukan pengganti aksi bermakna. Pertahankan bukti penyebab dan reproduksi jika gagal.
- Performa browser/host: minimal 30 sampel per jenis aksi sesuai standar; p95 aksi biasa ≤300 ms, navigasi ≤1 detik, feedback proses berat tampak ≤300 ms dan UI tetap responsif. Ukur feedback yang benar-benar dirender, catat lingkungan dan cold/warm secara jelas. Jangan menyebutnya performa Android fisik.
- Recheck source terintegrasi yang sama setelah hasil agent digabung. Tes lama boleh direuse bila cakupan dan revisinya masih berlaku, tetapi perubahan baru wajib punya bukti baru.

### R7 — Tracker, checkpoint, commit dan APK baru

- Perbarui STATUS.json, REQUIREMENTS.csv, SCREEN-COVERAGE.csv dan GAP-MATRIX.csv sesuai bukti. Isi path bukti, revisi source, hasil review dan alasan pending. Status fase remediasi R0–R7 berbeda dari 14 fase produk; jangan menjumlahkannya sebagai persentase selesai produk.
- Simpan bukti baru di `Artifacts/Hermes-Remediation-2026-10-21/` tanpa menimpa `Artifacts/Independent-Review-2026-10-21/`.
- Setelah review/regresi lulus, buat commit lokal selektif atas perubahan yang siap; jangan memasukkan save pribadi, secrets, data tanpa hak atau binary besar. Perubahan terdahulu yang belum siap dipisahkan dan dijelaskan, bukan dibuang.
- Build APK kandidat baru dari source yang diuji. Catat command, exit code, waktu, appID/version, signing certificate digest, ukuran, SHA-256, Git revision dan status dirty. Cocokkan asset paket dengan source, periksa seluruh perubahan produksi sudah masuk build, dan salin APK ke path kandidat yang jelas. Jangan menyebut APK lama sebagai hasil baru.
- Jangan instal atau klaim lulus perangkat. Bila seluruh scope source/browser/build lulus, nyatakan batas penyerahan secara spesifik dan gate native pending. Jika requirement produk wajib masih blocked/pending, status produk tetap PARTIAL.

## 6. Ketekunan, pelaporan dan checkpoint

Lanjutkan otomatis dari hasil terukur ke hasil berikutnya tanpa meminta izin rutin untuk bugfix, desain detail yang sudah didelegasikan, tes, atau commit lokal. Jangan berhenti hanya karena tes hijau, APK terbentuk, durasi, jumlah iterasi, atau ingin menghemat token. Jangan menurunkan standar untuk mempercepat.

Sesudah dua kegagalan sama tanpa diagnosis/bukti baru, laporkan `PERLU PERHATIAN`, sertakan bukti dan ubah pendekatan. Sesudah tiga, kerjakan bagian independen atau nyatakan BLOCKED bila butuh perubahan eksternal. Jangan mengulang command yang sama tanpa alasan baru. STOP pemilik selalu dihormati: berhenti di batas aman dan simpan checkpoint.

Gunakan checkpoint ringkas dan terbarui pada `Work/CHECKPOINT-REMEDIATION-2026-10-21.md`: baseline/revisi, fase R berjalan, status 14 fase produk, temuan terbuka, file berubah, tes yang benar-benar dijalankan dan hasilnya, save QA, hash save pribadi, path bukti, blocker, next_action konkret dan cara mengulang. Jika sesi terputus, tandai WIP yang belum dikompilasi/diuji; jangan mengubahnya menjadi selesai.

Laporan akhir wajib menyebut:

1. Perbaikan yang benar-benar sudah berfungsi beserta alur pengguna.
2. Mapping AUD21-01 sampai AUD21-05 dan requirement lain ke bukti, bukan checklist tanpa tautan.
3. Tes/domain/browser/performa yang dijalankan, cakupan, hasil, dan keterbatasan.
4. Bug yang ditemukan, perbaikannya, dan sisa bug/gate tanpa menyembunyikan minor.
5. APK baru dan metadata, commit lokal serta perubahan yang belum di-commit.
6. Bukti save pribadi dipertahankan, gate perangkat dan dataset yang belum terpenuhi.

Mulai sekarang: preflight singkat, reproduksi dua kontrak UI yang dilaporkan, perbaiki R1/R2/R3 terlebih dahulu, lanjutkan R4–R7 berdasarkan dependency dan standar di atas. Jangan berhenti pada rencana atau menunggu approval desain historis yang sudah digantikan mandat pemilik.
