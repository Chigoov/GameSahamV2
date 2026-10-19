# Checkpoint UI portrait dari referensi HP

20 Oktober 2026. Root `GAME SAHAM`, branch `codex/owner-audit-portrait-v8`, baseline `fcc7660`. Pekerjaan overhaul UI dan pemeriksaan host/browser/build selesai untuk lingkup berikut. **Produk keseluruhan tetap PARTIAL, 0/14 paket acceptance penuh.**

## Perubahan yang diselesaikan

Mengikuti keputusan pemilik: portrait, isi scroll, navigasi tetap, graphite–amber–teal, wealth kanan atas, level/XP dan achievement bersama karier. Acuan: PHONE-UI-REFERENCE.md, REA-APK-ANALYSIS.md dan REA-FEATURE-MAP.csv. Ini adaptasi presentasi ke source Java; tidak menyalin source/aset Flutter atau memasukkan seluruh mekanik aplikasi referensi sebagai requirement baru.

- **Shell:** header ringkas, wealth pribadi, waktu/akun dan lima root dengan ikon garis serta penanda aktif. Satu scroller konten; tinggi nav diukur dari ukuran aktual. Root mempertahankan scroll dan filter. Detail baca tetap memiliki navigasi; tiket dan konfirmasi finansial terfokus dengan background inert.
- **Beranda:** hero ekuitas bersih, kas tersedia/tertahan, karier/XP/misi, sesi, laporan, achievement dan pantauan. Nilai berasal dari Java; tidak membuat P/L atau grafik dekoratif.
- **Pasar:** indeks simulasi, status kawasan belum didukung, sorotan daftar, search/filter/watchlist, order/IPO/multi-aset dan baris harga. Pembaruan harga tidak mengganti baris/merebut fokus input. Kegagalan mempertahankan angka terakhir dengan pemberitahuan dan pulih saat bridge kembali tersedia.
- **Detail:** harga/perubahan/waktu, candle/garis/timeframe, tabel OHLCV, antrean dan tape. Bar `isReference` tidak dianggap transaksi. Keadaan kosong dijelaskan. Tombol perbarui mengambil snapshot harga/antrean baru; chart bukan janji streaming.
- **Order:** sisi belum dipilih; state Beli/Jual konsisten. Preview/review/submit/status, command ID dan proteksi submit existing dipertahankan. Focus trap tiket mencakup select. Review membedakan estimasi fill/biaya/kas/masa berlaku dari hasil aktual.
- **Aset:** ekuitas, kas, posisi dan kewajiban disajikan dari bridge Java. Valuasi/komposisi memakai domain existing, tanpa mesin finansial JavaScript kedua. Dompet dapat dibuka; alat lanjutan sesudah kepemilikan. Label detail posisi berbahasa Indonesia; hasil terealisasi sebelum biaya diberi konteks.
- **Firma:** AUM terpisah dari wealth pribadi, career gate, tawaran/mandat, Fund Builder dan laporan memakai komponen shared.
- **Riset:** daftar artikel membuka layar baca dengan waktu rilis dan sumber simulasi. Membuka menu tidak otomatis menandai semua artikel dibaca. Paket, kalender dan konsensus tetap memakai kontrak existing.
- **Karier dan achievement:** hero/progres, status misi, enam badge existing dalam grid dan filter selected. Badge memberi XP; akses tetap jabatan. Completion report badge masih gap produksi yang sudah dicatat.
- **Layar lain:** header/back, baris data, tombol navigasi vs tindakan, form, empty state dan token shared diterapkan pada FX, IPO, multi-aset, corporate/voting, risiko, stres, lab, laporan, entry/tutorial dan pengaturan. Kode S/FR/OQ dihapus dari teks pemain; copy teknis diringkas tanpa menyembunyikan batas data/fitur yang relevan.

Tidak mengubah parameter ekonomi, clock 288×, format save, app ID, signing configuration atau dataset. Tidak menambah framework/dependency/aset remote. CSS lama yang bertentangan disatukan. Tidak mengimpor akun/backend/kredensial/ilustrasi referensi.

## Source dan pemeriksaan

Source: `android/app/src/main/assets/www/index.html`, `style.css`, `app.js`; ringkasan portofolio pada `TradingWebBridge.java`; satu tes regresi Java. Runner browser: `Work/verify-portrait-ui.py` dan `Work/verify-portrait-flows.py`. Semua bukti lokal di `Artifacts/UI-Refresh-2026-10-20/`.

| Lingkup | Hasil aktual |
|---|---|
| JVM | **158 tes / 15 suite**, 0 failure/error/skipped. Tes baru memeriksa kas pinjaman vs ekuitas dan pemisahan klien |
| UI baru | **13 kelompok pemeriksaan PASS**: 35 root/viewport samples, 15 root dengan teks 200%, chart kosong/data, sisi, tiket viewport pendek, quote failure/recovery dan fokus |
| Flow lanjutan | **23 pemeriksaan PASS**: indeks/unavailable, IPO, multi-aset, FX review/receipt, risiko, voting, stres, lab, entry/tutorial, earned manager/mandat dan paket |
| Regresi sebelumnya | **15 pemeriksaan PASS**, 20 sampel portrait: matching fill, skip/retry/save, karier, trust dan tutorial. Label test diselaraskan karena kode screen dihapus |
| Viewport | 320×568, 360×640, 390×844, 412×915, 430×932, 768×1024, 1440×900. Teks 200% pada 320/390/412. Tinggi 390 untuk simulasi viewport pendek, bukan IME Android |
| Visual/runtime | Screenshot lima root dan flow tersimpan; root/chart/review/teks besar diperiksa visual. Tidak ada pageerror pada runner. Preview akhir tidak meminta aset remote |
| Build/lint | `testDebugUnitTest lintDebug assembleDebug` PASS. Lint 0 Error/Fatal, 13 Warning existing. Repack final setelah edit aset PASS; `node --check`, `git diff --check` PASS |
| APK | Signature valid; 7 aset cocok byte-for-byte dengan source final. Bukan bukti instalasi |

Angka kelompok browser bukan hitungan fitur unik atau klaim seluruh PRD lulus. `verification.json` menyimpan hash source, paket, test counts dan rincian runner. XML, log, signature serta screenshot menyertai.

## Kandidat dan preview

- APK: `Artifacts/UI-Refresh-2026-10-20/game-saham-portrait-ui-debug.apk`.
- Ukuran **627.315 byte**; SHA-256 `a08e40ae12e36073a0b39f9004fa7572782f1c6a6bc5a907423923fd5aedf641`.
- App ID existing `com.tradingglobal3d.mobile`, minSdk26/targetSdk36; signing debug existing.
- Preview: `http://127.0.0.1:8766/`, berlabel “PRATINJAU LOKAL — logika Java game; bukan Android”. Domain/bridge produksi, bukan MockBridge. Port QA 8767 dan save uji terpisah.
- Sebelum memperbarui server preview, waktu dijeda dan save dibuat. Sesudah load Java terbaru, personalAccount/career/playerProgress/ledgerEntryCount/clock sama. Bukti: preview-before-restart.json dan preview-preserved.json. Preview tidak direset demi memakai bridge baru.

## Batas dan next action

ADB tidak digunakan atau dibutuhkan pada pekerjaan ini. APK belum dipasang ke HP/emulator. Android Back, IME, TalkBack, insets, sentuh dp, lifecycle dan performa harus diperiksa tersendiri. CSS px dan screenshot browser tidak membuktikan hasil Android.

Gap sebelumnya tetap terbuka: sesi tiap bursa/akhir pekan/libur, manfaat/entitlement premium, laporan periode klien/completion badge, advanced NPC/event/corporate, inbox/data alternatif/perbandingan, serta dataset historis berlisensi. Jangan memakai checkpoint UI untuk meluluskan 14 paket atau 8 gate.

Next action: pemilik meninjau preview; verifikasi kandidat Android ketika perangkat tersedia dan lanjutkan gap domain pada matriks. Tidak perlu mengulang akuisisi/REA untuk melanjutkan UI.
