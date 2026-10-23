# Rekonsiliasi bukti layar redesain

Tanggal: 23 Oktober 2026, Asia/Jakarta. Cakupan bukti adalah browser Chromium dengan domain Java produksi dan save QA terisolasi. Ini bukan bukti Android, clickable journey, seluruh state gameplay, atau Delivery Gate visual.

## Baseline sebelum perubahan

Host `http://127.0.0.1:8791` melayani snapshot stabil `Artifacts/Redesign-Antislop-2026-10-23/baseline-assets`. SHA256 app.js: `fb363be46e7f0cef3b4e776e1547133c2057d4de98278ccec3b071da6e32f90a`. Produksi boleh berubah sesudah snapshot; seluruh pengambilan baseline tetap menggunakan snapshot ini.

Matriks wajib: 320×568, 360×640, 360×800, 390×844, 412×915, 430×932, 768×1024 dan 1440×900; masing-masing teks 100% dan 200%. Runner menambahkan ekspor fungsi hanya pada respons app.js yang diintersepsi browser. Source aplikasi tidak disisipi hook pengujian. Angka dan records berasal dari API Java host.

| Laporan relatif terhadap `Artifacts/Redesign-Antislop-2026-10-23/before/` | Sampel | PASS geometri | FAIL geometri/capture | NOT_TESTED | Gambar tercatat | Peran |
|---|---:|---:|---:|---:|---:|---|
| `screen-audit.json` | 1.232 | 865 | 319 | 48 | 2.346 | Run utama 77 route; baseline awal |
| `supplement/screen-audit.json` | 112 | 80 | 0 | 32 | 160 | Tujuh flow inline yang tidak ada di run utama |
| `oversize-retry/screen-audit.json` | 32 | 2 | 30 | 0 | 306 | Menggantikan seluruh sampel collections/business-koleksi; expanded-scroll ditile 12.000px dan posisi scroll diverifikasi |
| `earned-records/screen-audit.json` | 80 | 52 | 12 | 16 | 128 | Lima route dengan record nyata; order-status masih belum terpilih karena filter default order aktif |
| `earned-order/screen-audit.json` | 16 | 13 | 3 | 0 | 32 | Menggantikan order-status setelah filter riwayat dibuka |

Urutan rekonsiliasi berdasarkan key route+viewport+scale: utama → supplement → oversize-retry → earned-records → earned-order. Hasil unik: **1.344 sampel / 84 route**, 1.011 PASS pemeriksaan otomatis dan **333 FAIL**; 2.930 gambar terkait sampel final; nol pageerror atau exception capture pada sampel final. Seluruh 84 route berhasil dirender pada state yang disebut dalam laporan, tetapi angka ini **tidak berarti 84 layar/state lulus**. FAIL tetap terbuka dan setiap gambar memerlukan review visual.

Rekonsiliasi machine-readable: `before/reconciled-screen-audit.json`. Setiap sampel menyertakan `report` asal agar path screenshot relatif dapat diselesaikan dengan benar. Screenshot smoke tidak menimpa sampel final ini.

`smoke/` dan `smoke-final/` adalah pemeriksaan runner tambahan, tidak ditambahkan ke jumlah unik. Smoke final menggunakan pemeriksaan focusability role button yang lebih lengkap; 16/16 FAIL pada root baseline. Versi pemeriksaan runner berkembang selama baseline; hasil PASS/FAIL berbeda versi tidak menjadi pembandingan kuantitatif identik. Gunakan gambar untuk perbandingan visual dan ulang pemeriksaan versi akhir pada UI sesudah perubahan.

## Records yang sebelumnya tidak tersedia

Eksplorasi menggunakan host terpisah `http://127.0.0.1:18897` dengan snapshot baseline dan save baru `Artifacts/Redesign-Antislop-2026-10-23/qa-save-coverage`. Host baseline/after dan save pemain tidak diubah. Provenance setiap permintaan dan respons: `qa-save-coverage/coverage-preparation.json`.

1. `setClockState(true,1)` → `tickClock(1800)` → pause melalui API domain QA membuka sesi. Order pasar `COVERAGE-BBCA` dan `COVERAGE-TLKM` masing-masing 100 saham menghasilkan FILLED, posisi nyata, dan misi MIS-01/MIS-02 completed tetapi belum claimed.
2. `skipDay('COVERAGE-DAY-1')` menyelesaikan hari secara domain dan menghasilkan session reports. Klaim tidak dijalankan, sehingga review klaim masih tersedia.
3. Beli koleksi canonical `COL-MOBIL-003` melalui `transactCollection`. `getLoanQuote` Rp1.000.000, tenor14hari, agunan koleksi tersebut menghasilkan eligible quote. `disburseReviewedLoan` dengan quoteVersion yang sama menghasilkan SETTLED, loan `LN-1792632600000-1` dan receipt `LOAN:COVERAGE-LOAN-2`.
4. Runner memilih posisi/order/loan/report/misi dari respons domain aktual. Halaman status order dibuka melalui riwayat UI setelah checkbox active-only dimatikan. Tidak membuat object posisi/loan/report palsu dan tidak mengubah source/save pemain.

## Batas pemeriksaan

- Route inventory, ekspresi pembuka, panggilan bridge, record terpilih, computed font size, root/flow width, target sentuh, fokus satu Tab dan path screenshot tersimpan dalam JSON masing-masing run. Versi akhir runner mencatat SHA256 runner dan source.
- Screenshot viewport mempertahankan layout sebenarnya. Gambar `expanded-scroll` khusus bukti isi panjang memperluas scroll container sementara, lalu mengembalikan style; pada galeri panjang gambar ditile. Gambar expanded bukan bukti bahwa seluruh isi muat dalam satu viewport.
- Pembuka fungsi testing membuktikan hasil render, bukan bahwa seluruh tombol bisa dicapai pengguna. Tes clickable journey, performa, domain logic, lifecycle dan Android harus dilaporkan terpisah.
- Belum mencakup seluruh variasi empty/loading/error/offline, recovery native, seluruh hasil transaksi/retry/cancel/partial/stale quote, seluruh career unlock, variasi panjang data dan seluruh lifecycle. `uncoveredStates` pada report tetap berlaku.
- Status baseline keseluruhan **FAIL** karena 333 sampel final memiliki temuan otomatis. Tidak ada klaim Delivery Gate PASS atau seluruh gambar telah direview.

## Perintah pemeriksaan

Runner: `python Work/verify-redesign-screens.py --url <host-QA> --output <folder-dalam-Artifacts> --label before|after`. Run utama baseline menggunakan `--prepare-domain` untuk mendirikan satu perusahaan nyata pada save QA baseline. Run tambahan memakai `--only` dengan route yang tertulis dalam laporan. `--self-check` lulus untuk ekspor IIFE, nama route unik dan delapan viewport wajib; pemeriksaan sintaks Python juga lulus. Return code1 pada run dengan FAIL/NOT_TESTED disengaja dan dipertahankan.

## Sesudah perubahan

Final: `after/release-stable/screen-audit.json` **1.344/1.344 PASS**, 84 tujuan × delapan viewport × teks100%/200%, **3.200 gambar**, nol page error, nol field terlihat tanpa nama aksesibel. Source final stabil `release-assets`, SHA256 app.js `6fadfe59ba1a3d5258fd95a1997888bb2b495dfb9e58fe383da4b892c32a3d71`; snapshot tersebut sama dengan source produksi dan isi web APK. Seluruh opener pada inventaris memiliki route (`openersWithoutRoute=[]`).

`after/handoff-screen-audit.json` dan `BEFORE-AFTER.html` kini menunjuk run final penuh ini, tanpa mencampur supplement label lama. Setiap path gambar sebelum/sesudah diperiksa. Indeks dibuka dengan browser; pilihan84tujuan/delapanviewport ada, gambar perbandingan risiko200% berhasil dimuat dan tidak ada page error.

Run `after/release` sebelumnya terhenti setelah host Java kehabisan memori; partial250sampel dan log crash dipertahankan sebagai bukti gagal, tidak masuk jumlah final. Host ulang dibatasi256MiB heap/64MiB code cache. `after/delivery` juga merupakan run antara sebelum ID IPO dibetulkan; tidak dipakai sebagai bukti akhir.

## Adaptasi tes browser existing

Baseline 19suite asli dijalankan koordinator melalui `Work/run-redesign-browser-regressions.py`: 13PASS/6FAIL, log `Artifacts/Redesign-Antislop-2026-10-23/browser-before-run2/results.json`. Angka tersebut berasal dari tes sebelum adaptasi selector dan bukan hasil redesain.

Sepuluh file tes yang memakai presentasi lama dimigrasi: `verify-a2-a8-a10.js`, `verify-benchmark-performance.js`, `verify-collections-a1-a3.js`, `verify-grand-journey-e2e.js`, `verify-portrait-flows.js`, `verify-portrait-ui.js`, `verify-r01-r02-clock-sponsor.js`, `verify-r03-r04-r09.js`, `verify-r11-profile-tabs.js`, `verify-r13-collections.js`. Sembilan suite lain tidak memerlukan perubahan yang ditemukan pada pemeriksaan ini.

Perubahan terbatas pada rute Berita melalui Pasar, Profil melalui tombol header, riwayat melalui Aset, Liga/Era Baru melalui Karier, scroller `.app-scroll`, copy rasio waktu yang disetujui, struktur tiga tab Profil, badge/metric DOM dan katalog kategori dari data Java. Kelima area informasi Profil lama tetap diperiksa dengan mengeklik seluruh tiga tab dan mempertahankan label metrik trading, keuangan, pencapaian serta identitas/preferensi. Asersi neon procedural frame diganti identitas seluruh item canonical karena ilustrasi dekoratif ditiadakan; jumlah katalog/filter berasal dari domain, tidak diganti hardcode150. Assertion uang, status, idempotensi, quantity, jumlah sampel serta threshold performa tidak diturunkan.

Pemeriksaan sintaks semua19suite lulus. Runtime final: `browser-release`18PASS/1timeout dan `browser-release-retry`1/1PASS untuk suite yang sama, sehingga **19/19 suite final memiliki run PASS**. Timeout nav pada run serentak tidak dihapus dari log. Python existing10/10 serta lima suite tambahan mempunyai run PASS pada source final; `--deep` wired benar-benar menjalankan24checkpoint,266flowmatrix dan70rootmatrix. Bukti klik, state terkontrol, settlement serta final-review terpisah dirangkum di `REDESIGN-DELIVERY-2026-10-23.md`.
