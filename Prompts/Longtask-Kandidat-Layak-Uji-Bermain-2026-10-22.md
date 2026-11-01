# Prompt Hermes: verifikasi hasil terbaru dan siapkan kandidat layak uji bermain

Saya pemilik proyek. Lanjutkan Game Saham sampai tersedia APK kandidat yang siap saya uji bermain, dengan alur permainan utuh, angka yang konsisten, penyimpanan/pemulihan progres yang teruji dalam scope host, dan laporan yang cocok dengan artefaknya. Kerjakan implementasi bila masih diperlukan, tes, review dan penyerahan. Jangan berhenti pada rencana atau mengulang pekerjaan yang sudah benar.

## 1. Baseline aktual dan perlindungan pekerjaan

Root produksi yang boleh ditulis: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`. Repo `Game Trading Saham` adalah proyek historis berbeda dan bukan tempat menulis/mengimpor source/save/build.

Saat prompt ini disiapkan, HEAD masih `49bbafb`, branch `codex/owner-audit-portrait-v8`, tetapi source terbaru meliputi perubahan BELUM DI-COMMIT pada `Work/STATUS.json`, `Work/WireQaServer.java`, `Work/benchmark-performance-final.py`, `Work/test-live-sync-browser.py`, `Work/verify-portrait-ui.js`, `app.js`, `style.css`, dan `TradingWebBridge.java`. Ada checkpoint baru `Work/CHECKPOINT-PENUTUPAN-REMEDIASI-2026-10-22.md` serta dokumen untracked dan output `android/app/Artifacts/`. Jangan rollback ke HEAD atau menganggap HEAD mewakili seluruh baseline. Jangan menghapus/commit massal output tersebut.

Sebelum perubahan, buktikan cwd/Git root/branch/HEAD, tracked/untracked changes, source fingerprint, writer aktif, port/save QA. Jika pekerjaan sebelumnya masih berjalan, koordinasikan pada batas aman; jangan menjalankan writer/launcher kedua atau membunuh proses berdasarkan dugaan. Pertahankan seluruh perubahan lokal dan artefak baseline. Satu writer per file/shared state/build output.

Save pribadi: `Artifacts/LocalPreview/preview-save/save_game_v2.json`, SHA256 `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`. Periksa awal/akhir; perbedaan perlu diagnosis tanpa overwrite. Jangan memakai save pribadi atau preview8766 untuk tes mutasi. Semua transaksi/reset/crash/restore memakai save QA terisolasi.

APK terbaru yang ditemukan:
`Artifacts/Penutupan-Remediasi-2026-10-22/run-01/apk/GameSaham-v1.0.0-candidate-remediated.apk`.
Pemeriksaan ringan Codex saat penyusunan prompt: ukuran aktual955886byte, SHA256 `1c375c621244179d44fa0588103fb96f7938323c7217e93707ed891121716ac6`; ZIP valid; app.js/index.html/style.css/katalog cocok source saat itu. Ini bukan audit penerimaan seluruh perubahan baru.

Checkpoint menyebut956416byte, sehingga metadata ukuran perlu dikoreksi. Checkpoint juga menyebut455tes/57suite, sementara XML pada `android/app/build/test-results/testDebugUnitTest` saat diperiksa berisi461tes/55suite dengan0failure/error/skipped. Cari asal perbedaan dan identitas run/source; jangan memilih angka berdasarkan selera atau mengklaim salah satu run telah dijalankan ulang tanpa bukti.

## 2. Mandat dan batas scope

Baca `AGENTS.md`, `Work/README.md`, konsep/PRD/design brief dan mandat kualitas yang dirujuk. Baca checkpoint penutupan terbaru, `Work/STATUS.json`, serta:
- `Prompts/Longtask-Penutupan-Temuan-Remediasi-2026-10-22.md`.
- `Prompts/Longtask-Remediasi-Audit-UX-Realtime-2026-10-21.md`.
- Prompt UX/realtime/katalog100 dan inventaris aset Codex yang dirujuk dokumen tersebut.

Gunakan source dan bukti aktual untuk menilai klaim penutupan N1–N7; jangan membuka ulang pekerjaan yang sudah lulus hanya karena prompt lama masih mencantumkannya. Persyaratan mandatory dan standar existing tetap berlaku. Tugas ini tidak otomatis meluluskan seluruh14fase/PRD.

Produksi tetap Java + Android WebView + HTML/CSS/JS lokal. Java satu pemilik ekonomi, order, ledger, clock dan save. Tidak ada clock finansial/ledger JavaScript. Pertahankan katalog100aktif50IDX50US12kategori, migrasi save/IPO/inventory, isolasi akun, application ID `com.tradingglobal3d.mobile` dan signing identity.

Ponytail: pahami alur/caller; reuse fungsi/token/runner/dependency existing, standard library dan native platform; perbaiki shared root cause dengan diff minimum. Tanpa engine/framework/abstraksi baru yang tidak diperlukan. Logika nontrivial menyisakan runnable regression minimum yang relevan.

Wewenang: perubahan repo, tes domain/browser, review, build dan commit lokal yang ditinjau. Tidak ada push/publish/pesan pihak lain/pembayaran/perubahan konfigurasi global Hermes/provider/model/credentials/scheduler/ADB/emulator/perangkat fisik atau repo/data lain. Native/instalasi fisik tetap NOT_TESTED/DEFERRED. Siapkan instruksi tes manual untuk pemilik tanpa menjalankannya atau meminta pemilik bertindak sebelum pekerjaan host independen selesai.

Multi-agent sesuai kemampuan nyata: satu koordinator, ownership jelas, reviewer berbeda untuk delta dan bukti. Jika tidak tersedia, jelaskan batasnya tanpa mengarang review. Tidak perlu approval rutin dalam scope.

## P1. Verifikasi penutupan terbaru dan bereskan inkonsistensi bukti

Tinjau diff lokal dan assertion N1–N7, bukan hanya laporan CLOSED. Pastikan detail posisi nol/P&L/mata uang, grafik/candle/tape/status/halt, helper QA di luar bridge produksi, kontras aktual, dedup, preservasi draft/fokus/scroll, dan benchmark benar-benar memenuhi kontrak. Jangan memakai endpoint QA sebagai pengganti jalur produksi yang hendak dibuktikan; fixture hanya setup terisolasi.

Prioritaskan tes yang dapat mendeteksi kegagalan, kemudian bukti lulus source akhir. Tinjau metrik benchmark: minimal30sampel per jenis wajib; P95 aksi biasa≤300ms per jenis, seluruh navigasi≤1000ms, feedback pekerjaan berat tergambar≤300ms dan UI responsif. Waktu bridge saja bukan waktu hasil terlihat. Bedakan waktu antrean settlement dengan domain commit→UI. Negative checks harus menolak sampel kosong/kurang/lambat dan memberi exit gagal. Jangan mewariskan JSON PASS run lama.

Rekonsiliasi ukuran/hash APK, suite/test count, fingerprint source/kelas Java QA, timestamp, cakupan viewport/font dan jalur bukti. Update tracker/checkpoint yang sesuai, tanpa menghapus riwayat. Jika temuan nyata tersisa, perbaiki dahulu lalu uji ulang. Seluruh pekerjaan host belum dianggap selesai hanya karena gambar pending.

## P2. Uji satu rangkaian bermain utuh

Gunakan permainan QA baru dengan state/seed tercatat dan jalur UI produksi. Susun aksi menurut prerequisite game, jangan mengarang saldo/karier/fitur yang belum terbuka. Fixture lanjutan boleh untuk state khusus, diberi label terpisah dari perjalanan pemain baru. Uji:

1. Mulai permainan → pahami saldo, status pasar, waktu, tujuan awal dan navigasi5tab. Perbaiki kebingungan yang menghalangi tindakan pertama melalui komponen/copy existing, tanpa membangun sistem tutorial baru yang tidak diperlukan.
2. Cari/filter saham → detail → BUY → review → submit → receipt → portofolio. Cocokkan kuantitas, lot/lembar, currency, biaya, kas tersedia/tertahan/total dan ledger terhadap domain.
3. SELL sebagian dan habiskan posisi → kas/P&L/kepemilikan/detail diperbarui ketika layar tetap terbuka. Bedakan order mengantre, partial dan filled; jangan menyatakan uang penjualan sudah diterima pada tahap reserve/pending.
4. Jalur sahamUS dan FX; order resting→partial bertahap→filled/cancel serta reject/expired yang applicable. Tindakan gagal/retry tidak mendebit/mengkredit dua kali atau menampilkan sukses palsu.
5. Bisnis → modal/pendapatan/biaya/payroll sesuai aturan domain → pergantian hari → uang perusahaan dan pribadi tetap terpisah. Jelaskan kapan pendapatan dibukukan, jangan menjanjikan setiap nominal bergerak terus-menerus jika mekaniknya perhari.
6. Misi/progres/karier yang terbuka dalam perjalanan ini memberikan hasil dan feedback yang konsisten. State locked memberi alasan/tindakan berikutnya yang jelas, tidak berupa tombol mati tanpa informasi.
7. Save → load kembali → lanjutkan langkah permainan dengan state dan input yang sesuai. Setiap layar penting dapat ditutup/kembali tanpa kehilangan alur atau memicu transaksi ulang.

Bandingkan nilai DOM dengan domain terkonfirmasi, bukan sekadar visible/berubah/bukan nol. Gunakan uang desimal/formatted value yang sah, bukan floating-point perhitungan ulang ekonomi. Catat identitas akun/request/order/revisi dan expected/actual. Tangkap error browser/unhandled rejection/dialog tak diharapkan. Semua langkah dan state yang belum teruji disebut apa adanya.

## P3. Uji keberlanjutan permainan dan penyimpanan

Dengan runner existing, uji rangkaian lintas minimal7pergantianharigame yang mencakup trading, biaya/pemasukan bisnis, misi/kalender dan simpan-muat. Hormati kalender/sesi/invariant; jangan memajukan harga lewat bypass QA untuk meluluskan state pasar tertutup. Gunakan standar10×100aksi untuk jalur finansial kritis sesuai ketentuan existing.

Pisahkan bukti foreground dengan waktu nyata, percepatan domain, Skip Day, dan catch-up offline. Cantumkan durasi aktual dan metode;7hari simulasi bukan otomatis35menit bermain nyata. Verifikasi aturan1hari=5menit saat aktif1× dan1hari=1jam saat background/ditutup melalui kontrak domain/lifecycle host yang dapat diuji. Host tidak membuktikan lifecycle Android fisik.

Gunakan save QA untuk memeriksa snapshot sebelum/sesudah load, order tertunda, kas/posisi/ledger/progres/clock, restart host, autosave/backup/recovery, serta gangguan tulis yang didukung runner. Save rusak tidak boleh diam-diam ditimpa permainan baru. Retry/load/catch-up tidak boleh menggandakan reward/biaya/settlement. Pisahkan state volatil UI dari state domain yang wajib persisten. Jangan mengklaim tidak ada kehilangan progres absolut.

Jika ada jeda/proses berat, feedback harus terlihat dan layar tetap responsif. Cari pertumbuhan DOM/listener/polling yang terus meningkat pada perjalanan berulang; perbaiki bila terbukti, tanpa menambah profiler/framework spekulatif.

## P4. Kesiapan UI dan paket untuk pemilik

Pada layar/state yang berubah, jalankan7viewport320×568,360×640,390×844,412×915,430×932,768×1024,1440×900 × font100%/200% nyata. Periksa screenshot secara visual, kontras dan clipping internal, angka/unit/CTA, fokus keyboard, reduced-motion/power-save dan target sentuh48dp. Jangan meluluskan keseluruhan aksesibilitas hanya dari document.scrollWidth.

Ilustrasi raster tetap dibuat oleh Codex. Hermes hanya menginventaris/memetakan slot dan mengintegrasikan aset yang benar-benar tersedia. Fallback rapi boleh untuk kandidat uji; aset diperlukan yang belum tersedia tetap ASSETS_PENDING_CODEX. Jangan menunggu gambar untuk menyelesaikan gameplay/testing yang independen, mengarang gambar sudah dibuat, atau menambah fitur baru agar cocok dengan daftar19gambar.

## P5. Review, build dan serah-terima yang jujur

Simpan artefak pada run baru di bawah `Artifacts/Kandidat-Layak-Uji-Bermain-2026-10-22/`, tidak menimpa baseline/run lama. Gunakan parameter output runner existing. Jalankan regresi domain/browser relevan source akhir; ulang hanya jika ada perubahan/kegagalan yang membenarkannya. Reviewer terpisah menilai diff, perilaku, negative checks dan kecukupan bukti; temuan terbuka tidak diberi CLOSED karena dokumen sudah dibuat.

Setelah source stabil, build APK kandidat dan verifikasi ZIP, appID/version/minSDK/targetSDK, certificate signing, asset byte matching termasuk katalog, serta tidak ada save/fixture/helperQA/dataset karantina di paket. Cocokkan APK dengan source/compiled code aktual, bukan HEAD saja ketika working tree masih berubah. Catat ukuran byte/hash dengan membaca berkas final sesudah penyalinan, kemudian periksa ulang tracker/report menunjuk nilai yang sama. Pertahankan hash save pribadi.

Serahkan:
- APK kandidat dengan path jelas, ukuran byte, SHA256, source fingerprint/commit dan batas uji.
- Checkpoint hasilP1–P5, tabel temuan, expected/actual perjalanan bermain, cakupan regresi, durasi/metode uji keberlanjutan, benchmark perjenis/rawtiming, screenshot yang diinspeksi dan hasil reviewer.
- Panduan uji HP ringkas di `Work/PANDUAN-UJI-BERMAIN-ANDROID-2026-10-22.md`: identifikasi APK, pemasangan/update tanpa menghapus data, bila signature tidak cocok jangan menyuruh uninstall tanpa membahas dampak progres; langkah mulai→BUY/SELL→bisnis/pergantianhari→simpan→tutup/buka→background/resume→Back/keyboard; hasil yang diharapkan; format laporbug dan cara menjaga progres. Jangan menyebut hasil panduan sudah lulus di HP sebelum pemilik mengujinya.
- `Work/STATUS.json` dan checkpoint terbaru yang konsisten, next_action dan gate host/aset/native terpisah. Hindari nama/status yang memberi kesan rilis publik sudah final.

## Kriteria selesai dan persistensi

Tujuan penyerahan ini adalah kandidat untuk uji bermain pemilik. Sebut READY_FOR_OWNER_PLAYTEST hanya jika alur host applicable lulus, data/save konsisten, temuan host diketahui sudah ditutup dan APK cocok source akhir. Status ini bukan penerimaan Android fisik atau seluruhPRD/14fase. Keseluruhan mandat tetap PARTIAL bila ada aset wajib/gate lain pending; tulis status host secara terpisah. Jika host belum lulus, sebut HOST_PARTIAL beserta blocker spesifik.

Jangan memaksa pengguna melakukan tesHP sebelum pekerjaan independen selesai. Jangan berhenti karena durasi/iterasi/token atau satu tes hijau. Kerjakan satu hasil terukur→tes→review→checkpoint→lanjut. Dua kegagalan sama tanpa diagnosis baru: PERLU PERHATIAN dan pendekatan berbeda; tiga: pindah pekerjaan independen atau BLOCKED bila perlu tindakan luar. Hormati STOP/pause pada batas aman.

Laporan akhir bahasa Indonesia harus menjawab: apa yang benar-benar bisa dimainkan; aturan penjualan/pemasukan terlihat dan latensinya; hasil penyimpanan/pemulihan; apa yang belum diuji; bug terbuka; APK yang harus dipakai; serta langkah pemilik berikutnya. Jangan mengklaim semua host selesai100%, tidak ada bug tersembunyi, atau native sudah aman tanpa bukti.

Mulai sekarang dari preflight dan rekonsiliasi buktiP1, lalu lanjutkan perjalanan bermainP2. Jangan berhenti setelah menuliskan rencana.
