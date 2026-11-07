# Hasil pengambilalihan dan pemeriksaan ulang plan LP

> **Status terbaru:** gunakan [VERIFIKASI-HOST-AKHIR.md](VERIFIKASI-HOST-AKHIR.md). Tambahan sesudah laporan ini menutup bug akun/restore, menerapkan catatan cakupan yang disetujui pemilik, membuktikan reservasi order nyata pada timestamp sama, dan menghapus kelas QA yang tertinggal pada output build. Snapshot terbaru memiliki 22 kelompok browser dan 70 kombinasi layout; hash APK di bawah adalah riwayat dan telah digantikan.

Tanggal: 3 November 2026, Asia/Jakarta. Status: **bagian host diserahkan; Android/Poco X8 Pro NEEDS_VALIDATION**. Pemilik meminta bagian host diselesaikan dahulu. Tidak dilakukan ADB, instalasi, reset data, commit atau publish.

## Identitas dan batas pekerjaan

- Root: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`.
- Branch: `codex/classic-ui-new-assets`; HEAD `9653f7a992405b4dfaf28acddbf1caf158d03957`. Checkout memiliki banyak perubahan lokal milik pekerjaan sebelumnya; seluruhnya dipertahankan.
- Plan aktif: `Work/Perbaikan-Lanjutan-Performa-2026-11-03/plan.md`, LP00–LP08 sesuai urutan yang tercantum di plan.
- Delta produksi pengambilalihan hanya `android/app/src/main/assets/www/app.js`, dibandingkan snapshot hasil agent sebelumnya. Patch terpisah: `logs/takeover-app.patch`. Diff Git terhadap HEAD juga memuat pekerjaan agent sebelumnya dan tidak seluruhnya dibuat pada pengambilalihan.
- Runner terfokus: `verify_ui.py`; memakai Playwright, Chromium, JDK, Android stubs, JSON dan JUnit yang telah terpasang. Tidak ada dependency atau framework produksi baru.
- Hook fungsi privat dan perubahan data QA hanya disuntikkan ke respons browser uji. Tidak dimasukkan ke source produksi atau APK.

## Temuan yang benar-benar direproduksi dan diperbaiki

Hasil sebelum perbaikan ada di `logs/verified-takeover-before/results.json`; app.js awal dibekukan pada direktori yang sama. Empat kegagalan ini muncul melalui aset produksi, bukan pencarian string source:

| Masalah hasil agent sebelumnya | Sebab dan perbaikan pengambilalihan | Bukti setelah perbaikan |
|---|---|---|
| Candle tertutup close 100 berubah menjadi kuotasi 105 | Updater menulis quote ke candle terakhir tanpa kepemilikan bucket yang benar. Hapus mutasi sintetis tersebut; gunakan bar canonical dari Java melalui renderer existing. | T07: close candle tertutup tetap 100. |
| SVG garis tidak berubah saat close canonical berubah | Jalur live hanya mengubah elemen candle. Reuse `refreshChart()` untuk candle/garis; signature mencakup identitas waktu, OHLCV dan partial. | T08: path garis benar-benar berubah; T06: axis, volume dan pemulihan data kosong sesuai canonical. |
| Snapshot tertunda menimpa saldo baru pada timestamp game sama | Queue menyimpan snapshot A; flush kemudian menerapkannya setelah B. Queue kini menandai kebutuhan refresh, membaca keadaan terkini saat flush dan dibersihkan ketika snapshot sah diterima. | T13: saldo B Rp987.654 tidak kembali menjadi saldo A Rp1 miliar; T12 memeriksa area utama dan detail. |
| Kartu holder dibangun ulang meski payload sama; perubahan nonpemain/supply bisa tidak terpantau | Status sudah diubah menjadi loading sebelum diuji success; invalidasi mengandalkan kuantitas pribadi. Simpan status sukses sebelumnya, bandingkan payload holder/supply/coverage dan periksa sumber saat panel relevan diperbarui. | T10: node sama dipertahankan, payload identitas salah ditolak, error/retry pulih, perubahan supply/nonpemain tercermin. |

Perbaikan sebelumnya untuk kuotasi Beranda, rute Pasar gabungan, filter dan pemeliharaan pilihan candle dipertahankan setelah uji perilaku, tanpa menulis ulang desain. Keputusan pemilik: **candle pilihan yang keluar rentang diganti candle terbaru**. Preview `logs/verified-takeover-decision/` dan regression check menyimpan keputusan tersebut.

## Bukti uji host dan browser

| Lapisan | Hasil aktual | Bukti |
|---|---|---|
| Java domain terpilih | **90 tes pada 18 suite lulus**. Mencakup tujuh suite tambahan yang relevan dan semua sebelas `Package*Test` yang dipilih runner. Bukan seluruh suite repository. | `logs/verified-takeover-domain/host-results.json` dan log per suite. |
| Perilaku UI produksi | **18 kelompok pemeriksaan lulus**, tanpa page error. Mencakup chart, filter, cache, snapshot, keyboard/mouse, owner, tiket order, run baru pada tanggal sama dan pembatasan request. | `logs/verified-takeover-final-accepted/results.json`. |
| Urutan aksi | **10 seed × 100 = 1.000 aksi UI terkontrol lulus**, dengan assert per aksi. Bukan 1.000 transaksi atau sentuhan fisik. | `logs/verified-takeover-sequence/sequences.json`. |
| Buka/tutup detail | 100 siklus; saat satu refresh aktif, `getInstruments=1` dan `getChartData=1`. Tidak ada overlay tersisa. Jenis/jumlah listener pada window, document, body dan app-scroll tetap sama. | Kelompok T11/T14 dan T14 persistent listener pada hasil final. |
| Reflow | **56 kombinasi**: empat layar × tujuh viewport × teks normal/200%; tanpa page error atau overflow horizontal pada container yang diperiksa. Skala root aktual 16/32 px. | `logs/verified-takeover-layout-final/layouts.json` dan screenshot. |
| APK | Build debug offline berhasil; 405 aset web produksi cocok byte-for-byte dengan source. Tujuh file audit/preview dikecualikan sesuai aturan existing build.gradle. | `logs/takeover-apk-build.log`, `logs/takeover-artifact-verification.json`. |
| Save pemain | SHA-256 akhir sama dengan referensi; hanya dihitung hash, tidak dimuat, disalin atau dipakai untuk QA. Semua domain uji memakai save QA baru. | `logs/takeover-artifact-verification.json`. |

Viewport: 320×568, 360×640, 390×844, 412×915, 430×932, 768×1024, 1440×900. Pemeriksaan reflow mengukur batas app-scroll, flow-page, plot dan slider, serta menyediakan screenshot; tidak meluluskan semua teks/route/kontras aplikasi. Mouse Chromium bukan bukti sentuhan Android. Listener stabil dan node yang hilang bukan bukti heap bebas kebocoran.

Kartu indeks terdapat pada panel **Pasar**, bukan Beranda pada UI produksi saat ini. Uji final memeriksa indeks di panel yang benar, serta kuotasi Beranda. Tidak menambah kartu indeks baru ke Beranda demi menyesuaikan asumsi plan lama.

## Pengukuran yang dapat ditelusuri

Rincian agregat: `logs/takeover-measurements-summary.json`; tiap entri menunjuk JSON sampel mentah. Setiap kategori/versi mempunyai tiga pengulangan × 30 sampel; lima warm-up per pengulangan tidak dihitung. p95 memakai nearest rank dari 90 sampel gabungan. Waktu diukur dengan `performance.now()` di browser sampai dua callback `requestAnimationFrame` setelah update, bukan waktu RPC Python/selector.

Sumber before adalah app.js hasil agent yang dibekukan sebelum perbaikan tambahan. Java dan aset lain sama pada pasangan tersebut. Ini perbandingan **awal LP dengan hasil pengambilalihan**, bukan before PF, dan bukan perbandingan APK Android.

| Beban / layar | p95 before, ms | p95 after, ms |
|---|---:|---:|
| Kecil, Beranda | 35,5 | 34,6 |
| Kecil, Pasar | 34,1 | 34,2 |
| Kecil, candle | 51,1 | 50,6 |
| Kecil, garis | 51,3 | 50,6 |
| 96 bar, Beranda | 34,3 | 34,5 |
| 96 bar, Pasar | 34,2 | 34,4 |
| 96 bar, candle | 34,2 | 50,1 |
| 96 bar, garis | 34,6 | 34,5 |
| 5 posisi + 24 order OPEN + 96 bar, Beranda | 34,2 | 36,4 |
| 5 posisi + 24 order OPEN + 96 bar, Pasar | 34,5 | 34,3 |
| 5 posisi + 24 order OPEN + 96 bar, candle | 34,7 | 67,2 |
| 5 posisi + 24 order OPEN + 96 bar, garis | 34,6 | 58,8 |
| Flush setelah scroll utama | 184,5 | 184,5 |
| Flush setelah scroll detail | 201,4 | 200,7 |
| Event resume browser | 50,6 | 34,3 |

Pada beban ramai, lima posisi dihasilkan oleh lima order MARKET yang benar-benar FILLED di Java dan 24 order LIMIT benar-benar OPEN. Daftar instrumen dan order tersimpan dalam `workload.json` pada kedua direktori busy. Real clock QA digeser lalu diselaraskan lewat jalur produksi 48×; helper legacy `advanceToTradingSession` tidak dipakai untuk hasil final. Grafik diberi 96 bar canonical sintetis berlabel QA; pengukuran ini tidak membuktikan biaya membangun 96 bar dari data trade Java atau sesi ekonomi panjang.

**Tidak ada dasar menyimpulkan peningkatan kecepatan besar.** Pada beban ramai, detail candle/garis justru lebih mahal setelah data dibaca dan digambar dengan benar. Versi before dapat tampak cepat karena gagal memperbarui grafik. Pada skenario terukur, `getInstruments` satu kali per update pada kedua versi; `getChartData` menjadi satu kali per update pada versi after, menggantikan jalur before yang melewatkan pembaruan dalam jendela sampel. Biaya itu dicatat, bukan disembunyikan sebagai optimasi FPS.

Angka scroll mencakup debounce produksi **150 ms** dan dua frame browser; bukan latensi respons sentuhan. Caption beberapa log interaksi awal menyebut 180 ms secara keliru; nilai source yang diuji adalah 150 ms. Sampel final yang dipakai adalah `verified-takeover-interactions-baseline-valid` dan `verified-takeover-interactions-final-valid`. Log eksperimen lain disimpan sebagai riwayat, bukan digabung ke perbandingan akhir.

Heap Java, heap WebView, GC, suhu, FPS layar, input-to-photon dan IPC Android **belum diukur**. Peningkatan 84% IPC, penghematan payload per detik, penghematan RAM per jam, 60 FPS dan stabilitas 100% tidak diluluskan oleh bukti ini. Hitungan panggilan adalah per update pada skenario terkontrol, bukan klaim frekuensi panggilan per detik seluruh game.

## Matriks penyerahan plan

| ID | Status dan batas |
|---|---|
| LP00 | DIPERBAIKI_DAN_DIUJI_HOST: before JS, manifest source, reproduksi dan patch sendiri tersedia. APK sebelum PF tidak direkonstruksi. |
| LP01 | DIVERIFIKASI_BROWSER: kuotasi Beranda, indeks Pasar, movers dan query/exchange/sektor/watchlist diuji di rute produksi. Tidak ada indeks Beranda yang baru ditambahkan. |
| LP02 | DIVERIFIKASI_BROWSER: timestamp, readout, node, fokus, keyboard, mouse dan rolling window diuji; fallback latest telah disetujui pemilik. Sentuhan fisik belum diuji. |
| LP03 | DIVERIFIKASI_BROWSER + tes domain terpilih: OHLCV canonical, bucket, garis, axis, partial, kosong/pulih dan signature diuji. |
| LP04 | DIVERIFIKASI_BROWSER: cache node, supply/nonpemain, identitas, retry dan panel terlepas diuji. Kasus transaksi/penyimpanan Java ditopang suite domain terpilih, bukan klaim seluruh ownership dunia. |
| LP08 | DIVERIFIKASI_BROWSER: request terbatas, macro/profil dimuat ulang pada run baru bertanggal sama, cache stabil tidak dibaca ulang; preview MARKET direvalidasi dan input LIMIT dipertahankan. Variasi restore/akun finansial seluruh fitur tidak diluluskan oleh fixture ini. |
| LP05 | DIVERIFIKASI_BROWSER: scroll kedua area, same-clock A/B, run baru, event resume, pointer dan listener diuji. Process death Android tetap terbuka. |
| LP06 | DIPERBAIKI_DAN_DIUJI_HOST: tiga pengulangan, sampel individual, beban kecil/96 bar/beban order-posisi, scroll dan event resume tersedia; batas Android dan memori eksplisit. |
| LP07 | NEEDS_VALIDATION: bagian host, 10×100 UI dan APK selesai; lifecycle Android/NV01, Poco X8 Pro dan sesi perangkat 60 menit masih terbuka. |

T01–T16 mempunyai bukti sesuai lapisan di atas, bukan PASS menyeluruh untuk semua arti baris matriks. Secara khusus, T07 menggunakan tes domain chart dan uji UI canonical bersama-sama; T14 membuktikan request/listener/DOM terukur, bukan heap; T15 tidak membuktikan semua variasi pergantian akun/restore. **T17 belum lulus penuh karena gate perangkat belum dijalankan.**

## Artefak dan perintah pengulangan

APK: `android/app/build/outputs/apk/debug/app-debug.apk`, debug, 16.408.211 byte, SHA-256:

`C11726F6CAE65D34A352F57932DEB8947BB665E992CAB4B87ECD3758B6AA4E3C`

Save pemain, SHA-256 tidak berubah:

`59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`

Jalankan dari root repository. Gunakan label baru agar bukti lama tidak tertimpa:

```powershell
python Work/Perbaikan-Lanjutan-Performa-2026-11-03/verify_ui.py --label review-domain --host
python Work/Perbaikan-Lanjutan-Performa-2026-11-03/verify_ui.py --label review-ui --skip-compile
python Work/Perbaikan-Lanjutan-Performa-2026-11-03/verify_ui.py --label review-sequence --skip-compile --sequence
python Work/Perbaikan-Lanjutan-Performa-2026-11-03/verify_ui.py --label review-layout --skip-compile --layout
python Work/Perbaikan-Lanjutan-Performa-2026-11-03/verify_ui.py --label review-busy --skip-compile --benchmark --dense --busy
python Work/Perbaikan-Lanjutan-Performa-2026-11-03/verify_ui.py --label review-interactions --skip-compile --interaction-benchmark
```

Untuk before, tambahkan `--asset-source Work/Perbaikan-Lanjutan-Performa-2026-11-03/logs/verified-takeover-before/app.js`. `--skip-compile` hanya dipakai setelah kompilasi source Java snapshot yang sama. Server selalu localhost dan save QA baru; runner menghentikan hanya server yang dimulainya.

Build dijalankan offline dengan JBR Android Studio dan `-Djdk.net.unixdomain.tmpdir` pada folder `.socket-qa` di repo, terbatas pada proses command. Kegagalan loopback JDK awal dicatat di riwayat; retry dengan lokasi socket pendek berhasil. Tidak mengubah konfigurasi global.

## Yang tetap terbuka dan next_action

1. **Poco X8 Pro / NV01:** perlu izin tindakan perangkat yang konkret, identitas perangkat sebenarnya, Android/WebView/refresh rate/mode daya, kebijakan instalasi yang melindungi save, lifecycle/force-close/recovery dan sesi 60 menit beserta metrik awal/tengah/akhir. Pilihan pemilik saat ini: selesaikan host dahulu. Belum melakukan pemeriksaan koneksi sekalipun.
2. **Batas bukti:** belum ada profil heap/GC atau pembuktian semua variasi akun/restore pada browser. Tidak menganggap 18 kelompok UI atau 90 tes domain sebagai jaminan seluruh game bebas bug.
3. **Catatan UI untuk peninjauan terpisah:** `updateIssuerOwnership` mengisi `coverageNote`, tetapi menyembunyikan `.ownership-status` lagi pada success/cache hit. Source domain menyediakan catatan cakupan registri; metadata saham belum terpetakan tetap ditampilkan. Maksud tampilan catatan perlu ditinjau sebelum mengubah UI/teks; ini tidak diperbaiki diam-diam di luar hasil yang ditunjuk.

Next action: pemilik meninjau hasil host ini. Gate fisik tetap NEEDS_VALIDATION; pekerjaan keseluruhan belum dinyatakan selesai atau dijamin smooth di Poco X8 Pro.
