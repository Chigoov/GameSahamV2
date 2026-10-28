# Prompt eksekusi Hermes: UX, motion, realtime dan visual game katalog 100 perusahaan

Saya adalah pemilik proyek. Lanjutkan langsung pekerjaan Game Saham sebagai long task: implementasikan pembaruan otomatis yang konsisten, rapikan beranda serta layar utama mengikuti tiga referensi visual, tambahkan feedback transaksi/pencapaian, dan siapkan integrasi ilustrasi bisnis/koleksi. Setelah itu uji, perbaiki, review, dan bangun APK baru. Jangan berhenti pada pembuatan rencana, mockup, daftar saran, atau laporan audit.

## 1. Root, baseline dan perlindungan pekerjaan

Root produksi yang boleh ditulis:
`C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`

Repo `C:/Users/HYPE AMD/Downloads/VIBE CODING/Game Trading Saham` adalah proyek historis berbeda. Jangan menulisnya, menyalin source/save/build darinya atau memakai checkpoint sesi lamanya sebagai state aktif.

Sebelum perubahan, buktikan cwd/Git root/branch/HEAD, tracked dan untracked changes, fingerprint source, proses/writer aktif, port serta direktori save QA. Satu writer per file/shared state dan output build. Jangan mematikan proses lain berdasarkan dugaan dari log yang diam.

Baseline saat prompt disiapkan: HEAD `8c61169`, branch `codex/owner-audit-portrait-v8`, ditambah perubahan katalog100 yang MASIH LOCAL CHANGES. HEAD saja belum mewakili source terbaru. Pertahankan seluruh perubahan tracked/untracked, termasuk referensi pemilik, catatan UX, prompt dan tes baru. Jangan reset/clean/checkout paksa atau mengembalikan source ke commit lama agar cocok dengan laporan.

Baseline katalog yang wajib dipertahankan:
- 100 perusahaan aktif, 50 IDX + 50 US, 12 kategori, 0 nonaktif.
- Filter kategori/pencarian/bursa/watchlist sudah terintegrasi dan kategori terlihat pada row/detail.
- Migrasi katalog market-state v1/unversioned26 ke v2/100, dengan validasi ketat, pengamanan IPO dan inventory NPC sekali saja.
- Full JVM regression 461 tes PASS. Ini baseline, bukan target jumlah tes atau bukti perubahanmu nanti lulus.
- Browser QA katalog: 14 kombinasi viewport/font, delapan kelompok perilaku; bukan seluruh acceptance produk.
- APK baseline: `Artifacts/Catalog100-2026-10-21/GameSaham-100-Perusahaan.apk`, 877120 byte, SHA-256 `77faf2da9a6bb6b3297cd4cb1db3b875d7a9aceb16e4009ea67baa97c19a1024`.

Save pribadi: `Artifacts/LocalPreview/preview-save/save_game_v2.json`.
Hash terakhir: `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`.
Verifikasi hash awal/akhir; hash berbeda perlu diagnosis, bukan overwrite. Jangan memakai save pribadi/preview port8766 untuk QA mutasi. Semua reset, transaksi, prestige, crash, migrasi dan recovery memakai save QA terisolasi. Jangan menimpa bukti/APK baseline.

## 2. Sumber keputusan dan urutan baca

Baca penuh dokumen kerja yang relevan sebelum mengubah bagian tersebut:
1. `AGENTS.md` root produksi.
2. `Work/CHECKPOINT-CATALOG-100-2026-10-21.md`, `Artifacts/Catalog100-2026-10-21/verification.json`, fingerprint source dan hasil tes yang dirujuk.
3. `Work/NOTE-OWNER-UX-MOTION-REALTIME-2026-10-21.md`.
4. Tiga JPG pemilik pada `Docs/Design/References/Owner-2026-10-21/`; buka gambar secara visual, jangan menilai dari nama file saja.
5. `Docs/Product/01-Konsep-Game-2D-Portrait.md`, PRD dan design brief, lalu `Work/README.md`.
6. Prompt master `Prompts/Hermes-Final-UI-Gameplay-Multi-Agent-2026-10-20.md`, paket `Work/Hermes-Handoff-2026-10-20/` terutama `OWNER-DECISIONS.json`, `PHASE-QUALITY-STANDARDS.md`, `REVIEW-AND-REGRESSION.md`, `TIME-ACCEPTANCE.csv`, `FEATURE-ACCEPTANCE.csv`.
7. Spesifikasi/matriks fitur `Work/Hermes-Feature-Completion-2026-10-20/`, STATUS/REQUIREMENTS/SCREEN-COVERAGE serta acceptance existing.

Keputusan pemilik terbaru menang atas dokumen historis yang bertentangan. Prompt ini menambah pekerjaan UX/motion/realtime; tidak menghapus mandatory requirement atau membangun ulang fitur yang sudah lulus. Snapshot nomor baris/baseline lama harus dicocokkan dengan simbol/source aktual. Path hilang dicari dalam root; jangan mengarang isi.

## 3. Keputusan produk dan batas wewenang

- Produksi tetap Java + native Android WebView + HTML/CSS/JS lokal. Java satu pemilik uang, ledger, akun, harga, NPC, order, karier, waktu dan save. JS untuk input/presentasi.
- Tidak migrasi ke Flutter/Unity/MAUI, menambah framework spekulatif, menggandakan mesin ekonomi/clock atau memakai remote CDN untuk gameplay.
- Portrait, satu token warna/font/spacing/radius, lima tab Beranda/Pasar/Portofolio/Bisnis/Lainnya. Pertahankan fungsi existing, 100 perusahaan aktif, kategori dan kompatibilitas save.
- Foreground1x: 1 hari game =5 menit nyata. Background/ditutup: 1 hari game =1 jam nyata, tidak mewarisi speed foreground. Skip dan offline memakai pipeline domain yang sama. Jangan mengubah rasio atau jadwal settlement untuk memberi kesan realtime.
- Semua uang adalah uang game. Tidak menambah broker, transaksi uang nyata, cloud/backend wajib, iklan, fitur kripto, langganan nyata, multiplayer atau layanan berbayar dari isi referensi.
- Harga dasar/jumlah saham tambahan adalah parameter simulasi; katalog100 bukan perluasan otomatis dataset historis. Jangan mengisi grafik dengan data random/future atau membuat intraday fiktif dari bar harian.
- Diizinkan: edit repo, tests/save QA, browser lokal, build, review, checkpoint dan commit lokal selektif. Jangan push/publish, mengirim pesan pihak lain, membayar layanan, mengubah profil/model/provider/credentials/global Hermes atau membuat scheduler/launcher baru.
- Tidak menjalankan ADB/emulator/instalasi atau mengoperasikan HP pada tugas ini. Native Back/IME/lifecycle, installed APK, FPS/RAM/baterai fisik tetap NOT_TESTED/DEFERRED. Pisahkan dari gate host/browser/APK.

Gunakan pendekatan Ponytail: trace flow/caller terlebih dahulu; reuse source/runner/helper existing, stdlib/native/dependency terpasang, lalu minimum code yang benar. Tanpa abstraksi/event bus/framework baru bila mekanisme yang ada cukup. Jangan mengorbankan validasi, security, accessibility atau perlindungan data loss. Nontrivial logic harus punya satu pemeriksaan runnable yang bermakna pada runner existing.

## 4. Pembagian kerja dan progres

Pakai multi-agent nyata bila tersedia, dengan satu koordinator integrasi. Contoh ownership:
- Koordinator: facade/bridge/save/clock, kontrak, shared state, integrasi dan verdict.
- Worker UI: layout/app.js/CSS sesuai ownership eksplisit.
- Worker QA: reproduksi, tes browser/journey/performa dan matriks bukti.
- Reviewer berbeda dari implementer, dapat berjalan bergiliran bila slot terbatas.

Satu file shared hanya satu writer. Jangan dua agen menulis app.js/facade/bridge/token/status bersamaan. Berikan setiap worker root, scope file/API, acceptance, save/port/output, dependency dan larangan. Build/regresi yang memakai output sama harus bergiliran. Bila alat multi-agent tidak tersedia, gunakan pass implementasi/review terpisah dan nyatakan keterbatasannya.

Laporkan progres tugas ini U1–U6, bukan otomatis mengklaim14/14 produk. Fase produk existing tetap dipetakan, tetapi hanya diluluskan berdasarkan acceptance yang benar-benar diuji.

## U1. Audit awal, reproduksi dan kontrak pembaruan

Telusuri alur input → bridge → domain/ledger → persistence → hasil command → snapshot → komponen layar. Inventaris layar/state yang tampil dan semua mutation path terkait. Reproduksi dengan browser host Java produksi/save QA; mock visual berlabel tidak meluluskan integrasi.

Temuan awal yang harus diperiksa ulang:
- Setelah submit order, UI memanggil receipt dan refreshSnapshot.
- Timer foreground sekitar1detik memperbarui snapshot utama; quote daftar Beranda/Pasar diperbarui saat tidak ada flow/detail.
- refreshSnapshot tidak memperbarui seluruh isi Portofolio/Bisnis/detail yang dibentuk saat layar dibuka.
- Detail instrumen masih mempunyai tombol pembaruan harga/antrean; receipt mempunyai tombol pemeriksaan status.

Definisikan sebelum edit: field sumber/domain, kapan state sah berubah, layar yang terdampak, pemisahan akun/mata uang, kebijakan stale/loading/error, requestId/revisi/identitas kejadian dan metode ukur. Tombol perbarui boleh menjadi recovery tambahan, tetapi tidak boleh menjadi syarat agar pemain melihat hasil terbaru.

Kirim inventaris kebutuhan ilustrasi untuk Codex sedini mungkin pada batas U1; lanjutkan pekerjaan independen tanpa menunggu gambar.

## U2. Pembaruan otomatis yang konsisten

Tujuan: pemain membiarkan layar terbuka; transaksi yang terkonfirmasi langsung tercermin pada saldo/posisi/status/riwayat terkait tanpa refresh, pindah tab, membuka ulang detail atau restart.

Implementasikan melalui jalur sinkronisasi shared yang paling kecil. Reuse hasil command/snapshot/tick existing. Satu snapshot/revisi konsisten untuk kas, reserve, posisi, fee dan wealth. Update komponen yang berubah; jangan mengganti seluruh DOM setiap detik atau membuat timer terpisah pada setiap kartu. Cegah polling/request saling tumpang tindih, timer bocor sesudah halaman ditutup, dan hasil response lama menimpa state baru.

Perhatikan:
- Submitted/open/pending trigger tidak sama dengan fill/settlement. Partial fill hanya mengkredit bagian terisi. Cancel sisa tidak membatalkan fill sebelumnya.
- Penjualan menukar aset menjadi kas; seluruh proceeds bukan profit atau kenaikan wealth. Fee, kurs, dompet IDR/USD dan konteks pribadi/klien/perusahaan tetap benar.
- Reksa dana mengikuti cutoff/settlement; pendapatan/payroll usaha mengikuti kalender game dan akun perusahaan. Tidak mencetak uang per frame atau mengambil keputusan ekonomi dari UI.
- Error/timeout/unknown tidak tampil sebagai sukses. Retry memakai requestId stabil; cek hasil command sebelumnya sebelum mencoba ulang.
- Refresh tidak merusak input, caret, pilihan instrumen/akun/kategori, form draft, scroll, fokus, modal, review quote atau tombol konfirmasi. Harga bergerak tidak boleh mengganti diam-diam ketentuan yang sudah direview. Quote kedaluwarsa perlu status serta jalur review ulang yang jelas.
- Layar hidden/background tidak menjalankan ekonomi JS. Saat kembali aktif, reconcile domain satu kali sesuai kontrak, ambil snapshot baru, lalu tampilkan ringkasan perubahan tanpa duplikasi.

Target pada foreground: perubahan lokal terkonfirmasi terlihat P95≤300ms dari input sampai hasil benar terlihat sesuai standar existing. Untuk settlement tertunda, ukur domain commit → hasil terkait terlihat; update pada siklus commit/tick yang sama, bukan menunggu satu siklus refresh tambahan. Catat delay/domain time/transport/render terpisah. Jangan mengubah frekuensi simulasi demi animasi atau mengklaim timing Android dari host.

## U3. Beranda dan penerapan tiga referensi visual

Buka ketiga gambar berikut dan petakan elemen ke implementasi:

| Referensi | Penggunaan |
|---|---|
| `01-wealth-and-asset-detail.jpg` | Hierarki Beranda, satu angka utama, perubahan periode, pintasan dan detail instrumen |
| `02-market-and-candlestick.jpg` | Pasar/watchlist, kartu instrumen, sparkline/candlestick, filter periode dan Beli/Jual |
| `03-portfolio-and-quick-actions.jpg` | Portofolio, tren kekayaan, daftar kepemilikan dan tindakan cepat |

Gabungkan sebagai satu desain game. Jangan membagi tema per tab atau memasang screenshot sebagai halaman produksi. Kontrol tetap native/interaktif; data tetap domain. Avatar/nama/logo kripto/PRO/verified/angka contoh bukan data atau fitur wajib.

Beranda harus menjawab: kondisi pemain sekarang, perubahan penting terakhir, dan langkah berikutnya. Rapikan duplikasi wealth pada header/kartu besar; pertahankan akses ke detail kas tersedia/tertahan/utang. Tampilkan agenda/misi/promosi, risiko/peristiwa relevan, dan kejadian ekonomi terakhir dengan prioritas yang membantu pemain bertindak.

Pertahankan saldo, wealth, return, realized/unrealized P/L dan arus noninvestasi sebagai konsep berbeda. Periode/metode return jelas; denominator tidak sah/history kosong → penjelasan/empty state. Jika grafik butuh history yang belum tersedia, periksa domain/history existing dahulu; hanya simpan observasi game nyata yang diperlukan dengan persistence/migrasi minimum. Tidak membuat backfill angka fiktif atau riwayat kedua di JS.

Bisnis dan koleksi mendapat hirarki katalog/ownership/locked/upgrade/maintenance yang lebih jelas. Jangan mengubah reward hibah, harga bisnis, balancing atau syarat karier hanya karena layout berubah. Temuan ekonomi di luar kebutuhan UI dicatat dan ditelusuri sebelum perubahan material; keputusan rutin yang sudah didelegasikan tidak perlu ditanyakan kembali.

## U4. Feedback transaksi, progres dan motion

Tambahkan motion yang memberi makna pada kejadian terkonfirmasi:
- Transaksi terisi: pesan hasil, kuantitas, perubahan kas bersih dan akses receipt.
- Perubahan nilai/quote: penekanan singkat berdasarkan perubahan nyata.
- XP/level/badge/promosi: progres terlihat dan perayaan singkat pada kejadian baru.
- Upgrade bisnis/koleksi baru: feedback kepemilikan/perubahan yang jelas.
- Pergantian layar/detail/review: transisi konsisten tanpa memblokir tindakan.

Reuse transisi tombol/halaman existing dan CSS/SVG bila cukup. Durasi baseline dapat sekitar120–250ms untuk interaksi rutin; sesuaikan berdasarkan keterbacaan/hasil ukur. Tidak mewajibkan library animasi, video, partikel/loop terus-menerus, audio atau haptic pada scope ini.

Animasi angka hanya presentasi; nilai akhir dan nilai aksesibel harus sesuai domain. Jangan mengumumkan setiap tick ke screen reader. Dedup feedback memakai identitas kejadian/revisi yang benar: polling, tab switch, retry, load/restart dan restore tidak boleh mengulang perayaan atau memberikan reward dua kali. Initial load adalah baseline tampilan, bukan transaksi baru. Terapkan kebijakan ringkasan offline agar ratusan kejadian tidak membanjiri UI.

`prefers-reduced-motion` dan pengaturan reduced motion game harus bekerja untuk semua motion baru. Mode hemat daya menurunkan efek visual tanpa mengubah ekonomi. Status tetap terbaca lewat teks/ikon saat motion mati.

## U5. Ilustrasi: Codex membuat, Hermes mengintegrasikan

Pemilik secara eksplisit memilih Codex untuk membuat gambar yang dibutuhkan melalui skill imagegen. Jangan menganggap kamu sudah mempunyai output tersebut, mengklaim gambar generated saat hanya ada prompt, menghubungi Codex/layanan lain otomatis, atau memakai screenshot/ilustrasi referensi sebagai aset produksi.

Buat satu inventaris minimum `Work/ASSET-REQUESTS-UX-MOTION-REALTIME-2026-10-21.md` dari katalog bisnis/koleksi aktual: asset ID, benda/karakter, screen/slot pemakaian, rasio/dimensi tampil, transparent/background, gaya/palet konsisten, prioritas, prompt visual siap dipakai Codex, dan acceptance. Monogram/ikon existing cukup untuk identitas saham; tidak perlu membuat100 logo palsu perusahaan nyata.

Ilustrasi dapat mencakup kendaraan/properti/koleksi, jenis usaha, pegawai fiktif atau pencapaian jika diperlukan. Latar/transparansi mengikuti slot UI. Teks/angka dinamis tetap HTML; jangan menanam harga/saldo dalam gambar. Aset final dipasang lokal dalam repo, ukuran dioptimalkan sesuai tampilan tanpa merusak detail/alpha. Tidak ada remote image dependency wajib saat bermain.

Selama aset belum disediakan, lanjutkan layout/sinkronisasi/test serta slot integrasinya menggunakan presentasi existing yang jujur. Catat status REQUESTED/AVAILABLE/REVIEWED/INTEGRATED dan asset ID yang menunggu Codex. Jangan menandai visual penuh COMPLETE bila ilustrasi yang dibutuhkan masih pending. APK checkpoint boleh dibuat dengan batas aset disebut jelas; setelah aset tersedia, integrasikan, review visual dan rebuild sebelum menyebut hasil final.

## U6. Pengujian, review, APK dan penyerahan

Gunakan runner existing, save QA baru, port/output milik tugas. Periksa port/proses aktual; port8791 milik tugas katalog sebelumnya sudah dihentikan saat handoff, tetapi tetap verifikasi. Port8766 pribadi dilarang untuk mutasi; port8784 mungkin milik proses existing. Jangan memakai/menghentikan proses tersebut tanpa ownership yang terbukti.

Acceptance wajib:
1. Full JVM/bridge regression pada source terintegrasi akhir; baseline461 tidak menjadi expected count tetap. Semua failure/error/skipped ditriage; tidak menghapus tes gagal atau menurunkan guard.
2. Tes browser dengan Java domain produksi yang membuktikan perubahan otomatis TANPA reload/navigation/manual refresh: immediate sell, resting limit→fill, partial, cancel, duplicate/retry, koleksi, multi-aset, FX, fund submit/settle, income/payroll bisnis, reward, perubahan hari, mode/account switch dan kembali aktif. Bedakan fixture cabang dari earned journey.
3. Jika uang/order/waktu/save atau event idempotency disentuh, jalankan/perluas critical10×100 existing:10urutan bervariasi×100aksi bermakna, expected rejection diperiksa, invariant setiap mutasi/subaksi, save/restart/fault/retry dan trace/revisi. Catch-all yang menelan error atau menghitung no-op bukan kelulusan.
4. Semua layar/state applicable yang berubah diuji pada320×568,360×640,390×844,412×915,430×932,768×1024,1440×900 × teks100%/200% nyata. Sertakan empty/loading/locked/error/stale/invalid/success/recovery bila relevan. Screenshot dibuka dan diinspeksi; tidak ada overflow tak sengaja, teks/CTA tertutup, fokus/draft/scroll rusak. Target sentuh setara48dp, status selain warna, contrast, keyboard browser dan reduced motion diperiksa.
5. Benchmark sedikitnya30sampel per jenis aksi/navigasi yang ditentukan sebelum pengukuran, raw timing/environment/cache/seed/source. P95 per aksi biasa≤300ms; seluruh sampel navigasi wajib≤1000ms; feedback heavy work benar-benar tergambar≤300ms dan event loop responsif. Jangan menggabungkan aksi cepat untuk menyembunyikan yang lambat, menghitung pending sebagai settlement sukses, atau membuang timeout/outlier agar PASS. Uji efek pembaruan otomatis saat katalog100 aktif.
6. Pastikan 100baseline ID tetap aktif/berkategori; old26→100 migration, authorized IPO, replay/internal slot, inventory sekali dan corrupted save rejection tetap lulus. Tidak menambah hak replay ke99simbol yang belum mempunyai dataset.
7. Reviewer berbeda memeriksa diff/kontrak/test/trace/screenshot/metrics dan temuan awal. Perbaiki semua temuan valid termasuk minor, retest, lalu koordinator memverifikasi hasil gabungan.
8. Build APK baru dari source yang lolos; periksa applicationID,version,SDK,signing certificate, manifest, aset web/gambar/dataset, ZIP integrity dan SHA-256. Tetap `com.tradingglobal3d.mobile`, signing identity/progres. Jangan mengganti key, mengklaim debug sebagai production-signed atau menyamakan build dengan instalasi.
9. Verifikasi hash save pribadi, review diff dan commit lokal selektif jika siap. Jangan commit secrets/save pribadi/binary besar/dataset tanpa hak atau push.

Simpan bukti pada `Artifacts/UX-Motion-Realtime-2026-10-21/<run-id>/`: hasiltes/trace, journey/screenshots, raw metrics, APK/package/signature dan `verification.json`. Reuse file/matriks existing dengan tambahan minimum; jangan membuat framework pelaporan baru.

Checkpoint utama: `Work/CHECKPOINT-UX-MOTION-REALTIME-2026-10-21.md`. Catat timestampWIB, root/branch/HEAD/dirty fingerprint, exact sessionID, ownership/proses/port/saveQA, perubahan, command/exit/revisi/bukti, U1–U6, open findings, asset pending, hash save pribadi, blocker dan next_action konkret. Update bagian tugas terkait dalam STATUS tanpa menyamakan hasil subset dengan seluruh14fase/native.

## 5. Ketekunan dan completion gate

Lanjutkan satu hasil terukur → tes → review → checkpoint → tugas berikutnya. Jangan berhenti karena durasi, jumlah iterasi/giliran, penghematan token, satu tes hijau, mockup selesai atau APK terbentuk. Tidak perlu persetujuan rutin untuk implementasi dalam mandat ini. Tetap patuhi STOP/pause serta batas wewenang.

Sesudah dua kegagalan sama tanpa diagnosis/bukti baru, laporkan PERLU PERHATIAN, bukti dan pendekatan berbeda. Sesudah tiga, pindah ke tugas independen atau catat BLOCKED jika memerlukan tindakan luar. Aset pending tidak menghentikan pekerjaan independen. Jangan memulai writer/launcher kedua, mencoba ulang tanpa diagnosis, atau mengklaim terus bekerja setelah sesi berakhir. Resume memakai exact sessionID dari checkpoint, bukan latest.

Selesai untuk scope ini membutuhkan U1–U6 beserta requirement mandatory terkait yang applicable mempunyai bukti sesuai source akhir, zero bug terbuka diketahui, reviewer menutup temuan, tracker konsisten, aset yang dibutuhkan terintegrasi dan APK baru cocok. Label COMPLETE lama bukan bukti. Jika aset belum tersedia, sebut pekerjaan host yang sudah lulus dan ASSETS_PENDING_CODEX; jika gate lain gagal, PARTIAL/BLOCKED sesuai fakta. Native tetap NOT_TESTED/DEFERRED terpisah.

Laporan akhir harus memberi: perubahan yang benar-benar selesai; cara pembaruan otomatis bekerja dan batas latensinya; daftar fitur/state yang diuji; hasil tes/metrics; screenshot before/after dan tiga referensi; aset generated/integrated/pending dengan pembuatnya; APK/path/ukuran/SHA/source fingerprint; save compatibility; open blockers/native gate; serta checkpoint next_action. Tidak menjanjikan nol bug tersembunyi atau nol kehilangan progres mutlak tanpa batas bukti.

Mulai sekarang dari U1 dan langsung lanjutkan implementasi serta pengujian. Jangan menjalankan ulang pembangunan katalog100 atau kembali ke source lama. Pertahankan hasil yang sudah ada, lalu tuntaskan pengalaman pengguna dan pembaruan otomatis yang diminta.
