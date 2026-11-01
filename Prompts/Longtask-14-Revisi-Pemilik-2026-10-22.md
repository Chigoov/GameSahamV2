# Long task otonom: 14 revisi pemilik Game Saham

Tanggal keputusan: 22 Oktober 2026. Root kerja: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`.

Kamu diminta mengerjakan perubahan sampai hasilnya dapat ditinjau dan diuji, bukan hanya mengembalikan rencana. Pemilik sudah menjawab 15 pertanyaan. Jangan meminta pilihan yang sama lagi atau memangkas cakupan menjadi MVP baru.

## Baca sebelum bekerja

1. `AGENTS.md` dan standar mutu `Work/Hermes-Handoff-2026-10-20/PHASE-QUALITY-STANDARDS.md`.
2. `Work/Owner-Revision-14-2026-10-22/README.md`.
3. Seluruh `DECISIONS.json` dan `LONGTASK-14-REVISI.md` di folder paket tersebut.
4. `REVIEW-AND-INTAKE.md`, `TRACKER.csv`, `KATALOG-KOLEKSI-150.csv`, dan `KATALOG-REKSA-DANA-24.csv`.

Keputusan terbaru pemilik di DECISIONS menggantikan rekomendasi lama yang bertentangan. Spesifikasi lengkap memuat kontrak, dependency, dan acceptance yang wajib dijalankan; ringkasan prompt ini tidak menggantikannya.

## Hasil yang wajib terwujud

1. Hibah setelah klaim berubah menjadi countdown HH:mm:ss menuju pukul 00.00 WIB dunia nyata; tidak mengkredit uang otomatis.
2. Hapus Atur Sesi/Atur Waktu, play/pause, speed, dan Skip Day dari UI. Game berjalan otomatis, 1 hari game = 15 menit nyata saat terbuka dan 1 jam nyata saat inactive. Tampilkan jam mengalir dari satu clock domain, bukan lompatan setiap polling.
3. Profil risiko mengikuti perilaku pemain. Sejak awal tampilkan bahwa gaya sedang terbentuk; tanpa kuis wajib. Reuse `InvestorArchetypeService`, dengan data cukup dan alasan yang transparan.
4. Pusat misi memakai tab Harian/Mingguan/Karier, kartu ringkas, progress bar, hadiah, dan status klaim yang jelas.
5. Safe area atas, cutout, keyboard, dan navigasi benar pada Android yang didukung, tanpa padding ganda.
6. Minimal 3 IPO per bulan game. Pakai identitas emiten asli dan sesekali rekaan, tanpa label simulasi berulang di kartu. Konteks uang virtual/perusahaan rekaan tersedia terpusat; jangan mengklaim jadwal IPO atau angka keuangan sebagai data resmi dunia nyata. Sediakan inbox, banner, dan notifikasi Android opsional.
7. Seluruh 100 emiten existing serta IPO baru memperbarui persentase terhadap penutupan sesi sebelumnya. Bedakan data stale, tidak ada fill, dan perubahan 0% yang sah. Jangan membuat harga/fill palsu agar angka bergerak.
8. Fundamental setiap emiten lengkap dan konsisten sebagai model ekonomi game: valuasi, keuangan, pemilik, saham, rasio, dividen, periode, dan sumber. Pisahkan sumber identitas dari sumber angka.
9. Teks pada konteks saham menggunakan istilah emiten. Jangan mengubah `instrumentId`, key save, atau API hanya demi label. Produk non-saham memakai istilah yang tepat.
10. Komoditas dan aset digital spot, reksa dana unit/NAB, serta deposito tenor/bunga/penalti berfungsi sampai ledger dan settlement. Obligasi existing tetap dipertahankan.
11. Profil berisi ringkasan dan tab Kinerja/Keuangan/Pencapaian. Statistik mencakup semua aset/utang, memisahkan dana klien, hibah, setoran, dan profit investasi.
12. News memiliki countdown dan waktu berlalu, clue emiten/sektor, arah, rentang, horizon, dan confidence. Dampaknya benar-benar melewati keputusan NPC, order, matching, dan harga. Hindari future leak dan hasil yang dijamin.
13. Total 150 koleksi dalam 12 kategori dan 5 rarity, termasuk 12 item lama. Kepemilikan, agunan, dan harga perolehan lama aman. Sepuluh trofi didapat lewat prestasi; semua item punya visual lokal yang sah.
14. Total 24 reksa dana termasuk 3 produk lama. Strategi dan underlying benar-benar sesuai, bukan hanya nama berbeda. Lengkapi factsheet, risiko, biaya, kinerja as-of, cutoff, unit, subscription, redemption, dan likuiditas.

## Langkah pertama dan perlindungan progres

Buktikan cwd, Git root, branch, HEAD, dan perubahan lokal. Catat fingerprint source dan hash save pribadi dari checkpoint yang relevan. Jangan reset/clean, mengganti branch secara paksa, atau menimpa pekerjaan agent lain.

Jalankan baseline baru memakai runner existing dan direktori run terpisah. Audit runner sebelum dipakai: source harus dikompilasi ulang; save QA dan port harus terisolasi. Jangan menggunakan save pribadi atau preview yang sedang dilihat pemilik untuk tes transaksi/reset. Bukti review tersimpan di `Artifacts/Owner-Review-2026-10-22/`; pertahankan utuh.

Tutup F01–F05 sesuai spesifikasi: backup valid tertimpa primary rusak >10 byte; API profil tidak kompatibel minSdk26; Grand Journey false-pass; catch-up 30 hari nyata memblokir; tombol watchlist terlalu kecil dan keyboard salah. Dependency yang gagal tidak boleh dianggap lulus. Bagian independen boleh dikerjakan sambil diagnosis.

## Cara implementasi dan pembuktian

Java adalah satu pemilik ekonomi, waktu, dan save; JavaScript hanya input/presentasi. Reuse source, helper, native API, dan dependency existing. Jangan menambah engine, framework, backend, cloud service, atau abstraksi spekulatif.

Harga, biaya, dan syarat baru pada CSV adalah rancangan awal untuk kalibrasi. Boleh menyesuaikan berdasar hasil tes dan mencatat alasan, tetapi tidak mengurangi jumlah produk, mengubah kontrak owned/pending diam-diam, atau melonggarkan standar mutu.

Gunakan koordinator integrasi dan reviewer berbeda bila kemampuan multi-agent tersedia. Tetapkan ownership file dan kontrak lebih dahulu. Satu writer per file/shared state; facade, bridge, app.js, dan save tidak ditulis bersamaan.

Setiap hasil terukur diperiksa, direview, dicatat, lalu pekerjaan dilanjutkan. Pengujian meliputi regression kasus F01–F05, suite fresh yang relevan, minimal 10x100 aksi kritis bermakna dengan invariant setiap aksi, negative control gate, seluruh layar/state pada 7 viewport dan teks 100%/200%, inspeksi visual, serta benchmark yang benar-benar menunggu hasil aksi.

Hasil lama tidak meluluskan source baru. Receipt bukan bukti fill. Tidak adanya tombol wajib harus FAIL, bukan dilewati lalu dilaporkan PASS. Semua acceptance rinci pada LONGTASK tetap wajib.

Laporkan F selesai/5, R selesai/14, pekerjaan berjalan/pending, file berubah, command dan hasil, scope bukti, blocker, serta next_action pada setiap milestone. Setelah dua kegagalan sama tanpa diagnosis/bukti baru, kirim PERLU PERHATIAN dan ubah pendekatan; setelah tiga, pindah ke tugas independen atau BLOCKED jika membutuhkan tindakan luar. Jangan berhenti hanya karena durasi, iterasi, atau penghematan token. STOP pemilik berarti checkpoint aman dan paused; resume exact session ID.

## Penyerahan dan batas wewenang

Build APK baru setelah integrasi dan verifikasi applicationId/signing/minSDK/manifest/aset/hash/source fingerprint. Serahkan tracker F/R/INT, daftar katalog aktual, migrasi dan recovery, bukti tes positif/negatif, screenshot, command reproduksi, panduan uji pemilik, APK beserta hash, dan checkpoint Git lokal terarah.

Tidak ada push, publikasi, pembayaran, ADB, emulator, atau instalasi otomatis tanpa otorisasi baru. Jangan mengubah provider/model/credentials/global Hermes, membuat launcher/scheduler komputer, atau menyentuh repo lama/data lain. Penjadwalan notifikasi lokal di dalam aplikasi merupakan bagian fitur ini.

Perangkat yang belum diuji tetap NATIVE_PENDING. Host dan build tidak membuktikan safe area/permission/notifikasi/lifecycle di HP. Jika seluruh host lulus tetapi perangkat belum diuji, laporkan HOST_VERIFIED_CANDIDATE_NATIVE_PENDING beserta gate yang tersisa, bukan seluruh produk 100% selesai.
