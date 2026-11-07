# LP09: tabel statistik di bawah chart dan tanggal/bulan berkedip

Instruksi pemilik terbaru: yang dimaksud adalah **Open, Prev, Lot, High, ARA, Val, Low, ARB, Avg** pada tabel existing di bawah chart. Tanggal dan bulan berkedip. Bukan berita dan bukan running trade. Interpretasi sebelumnya dikoreksi; pembatasan berita ke hari berjalan dikembalikan, dan preview daftar transaksi tidak diterapkan. Artefak/log kedua interpretasi lama tetap riwayat, bukan hasil final permintaan ini.

## Perbaikan source

- Presenter jam/tanggal memakai satu format WIB, tidak mundur ketika sinkronisasi menerima snapshot terlambat pada run sama, dan tidak mengganti node tanggal/bulan bila teksnya sama. Tes juga melintasi 31 Oktober ke 1 November. Filter agenda memakai hari WIB yang sama dengan label.
- `TradingWebBridge.getInstruments` mengirim data sembilan kolom yang sebelumnya tidak lengkap. UI memakai formatter yang sama pada pemuatan awal dan pembaruan, tanpa mengganti bentuk tabel, label, navigasi atau menambah daftar transaksi.
- Open dicatat pada transaksi pertama hari berjalan; High/Low tidak menganggap harga referensi kemarin sebagai transaksi pertama hari ini. Prev mengikuti domain. Lot berasal dari jumlah lembar dibagi ukuran lot, bukan menganggap jumlah lembar sebagai lot.
- ARA/ARB memakai batas serta fraksi harga dari `Instrument`, menggantikan perkiraan 25% dan pembulatan bebas di JavaScript.
- Val adalah penjumlahan harga × jumlah setiap transaksi dalam domain, bukan harga terakhir × seluruh volume. Avg memakai Val/volume dengan BigDecimal. Nilai tetap diperbarui saat halaman rincian terbuka.
- Open dan nilai transaksi kumulatif disimpan sebagai dua field opsional di state katalog. Pemuatan baru mempertahankannya; pergantian hari mereset pencatatan. Update volume negatif/overflow ditolak, dan Open save di luar rentang High/Low ditolak sebelum state diterapkan.
- Pada save lama, nilai yang belum pernah dicatat hanya dipulihkan dari riwayat bila total volumenya benar-benar mencakup seluruh volume hari itu. Bila riwayat yang dibatasi 500 fill tidak lengkap, nilai yang tidak dapat dibuktikan tetap tidak tersedia sampai pencatatan hari baru; tidak memakai perkiraan yang tampak presisi. Tidak membaca/mengubah save pemain host.
- Drift harga harian memakai kembali harga canonical setelah pembatasan domain untuk posisi dan fill, agar harga yang dicatat konsisten dengan statistik.

## Bukti

**Checkpoint akhir host:** `logs/lp09-stats-host-verification.json` mengikat bukti berikut pada source/artefak yang diperiksa: **95 tes Java pada 19 suite terpilih, 27 kelompok browser, 1.000 aksi UI, 70 kasus layout dan 1.350 sampel pengukuran host**. Dua build dan pemeriksaan paket lulus. `check` regression exit 0, `check --claim full` exit 2; kelulusan penuh/perangkat tidak diklaim.

- `logs/verified-lp09-price-history-before/ChartHistoryRangeTest.log`: reproduksi harga harian tidak dikirim (`highPriceRaw` hilang). Pengujian daftar transaksi yang tercatat di log sama adalah riwayat interpretasi yang dibatalkan.
- `logs/gate-1793711727023609300/gate.json`: regresi source terbaru lulus. Suite terpilih mencakup persistensi Open/Val/Avg/volume, penolakan metadata salah tanpa mengubah state, kompatibilitas save lama, pemulihan hanya jika volume lengkap, reset hari, pergantian bulan, identitas node tanggal dan pembaruan semua sembilan kolom.
- `logs/verified-lp09-stats-proof-final/daily-stats.png` dan JSON: tabel existing pada source final dengan transaksi Java QA sebenarnya, bukan mock tabel. Contoh: 300 lembar = 3 lot, Val 3.072.500, Avg 10.241,6667; harga Open/High/Low dan fraksi ARA/ARB mengikuti domain. `verified-lp09-correct-daily-stats` adalah screenshot checkpoint sebelumnya.
- `logs/lp09-stats-performance-jobs.json`: empat job selesai exit 0. Ringkasan/sampel pada `logs/lp09-stats-performance-summary.json`: tiga pengulangan × 30 sampel, lima warm-up dikeluarkan; 90 sampel per kategori, p95 nearest rank. Beban kecil/96 bar/5 posisi nyata + 24 order OPEN diuji. p95 pembaruan Beranda/Pasar sekitar 34–35 ms, candle ramai 50,5 ms, garis ramai 49,9 ms. Flush scroll utama 184,6 ms dan detail 201,2 ms termasuk debounce 150 ms; event resume browser 50,1 ms. Ini bukan FPS, IPC Android, sentuhan fisik, atau bukti peningkatan dibanding versi lain. Pengukuran lama `lp09-performance-jobs.json` mengacu snapshot sebelum klarifikasi terakhir.
- Paket, hasil pengukuran, jumlah tes, hash dan exit check terakhir dicatat di `logs/lp09-stats-host-verification.json` setelah seluruh pemeriksaan selesai. Gate penuh tetap terpisah dari regresi host.

## Perubahan tes dan batas

Kasus `LP09_issuer_news_latest_today_and_live` dikeluarkan karena pemilik menegaskan bahwa berita bukan target, bukan untuk meluluskan implementasi yang gagal. Verifier sebelum klarifikasi diarsipkan pada `logs/lp09-pre-clarification-verify_ui.py` dan `logs/lp09-pre-clarification-quality_gate.py`. Penggantinya `LP09_detail_latest_daily_prices` memeriksa semua sembilan kolom, perubahan nilai dan node yang tetap. Seluruh 23 kelompok lama dipertahankan. Tes Java baru berada pada suite existing `ChartHistoryRangeTest`; tidak menambah framework/dependency.

Preview daftar transaksi pada `logs/verified-lp09-trade-preview*` tidak disetujui dan tidak diterapkan. Kode proposal berada sebagai arsip `logs/lp09-rejected-trade-preview.js`; runner aktif hanya mempunyai screenshot tabel statistik existing (`--stats-preview`), bukan pemasangan daftar baru. API getOrderBook dikembalikan ke cakupan semula; bukan sumber sembilan kolom ini.

Source produksi terkait: `app.js`, `TradingWebBridge.java`, `Instrument.java`, `InstrumentCatalog.java`, dan jalur pemulihan/drift di `TradingFacade.java`. Semua perubahan lokal lain dipertahankan. Save pemain hanya di-hash. Tidak commit/push/publish, tidak ada ADB atau instalasi baru selama tahap host ini. Pengujian Poco, lifecycle dan sesi 60 menit tetap membutuhkan bukti aktual.

## APK akhir host

Folder `Artifacts/Codex-LP-Verification/lp09-daily-stats-2026-11-03/`:

- Produksi `GameSaham-debug.apk`: `54E8CBF1985CC76E7F4EE6501910019C98436B2B6EE27CBAB84E1B0E156F2954`.
- QA `GameSaham-QA-debug.apk`: `74DD94CE27CF5B9E6EF872C67D17EF9145E5E19B232EF09A2FE21D52E6940315`.
- 427 anggota assets/DEX kedua paket identik; masing-masing 405 aset web dan 13 DEX diperiksa, tanpa kelas server QA/source test; tanda tangan valid. Bukti ada pada JSON gabungan.

Belum dipasang pada tahap host ini. Bukti APK sebelumnya untuk berita atau preview running trade bukan paket yang dipilih untuk permintaan pemilik yang telah diklarifikasi.

Sesudah host selesai, pemeriksaan koneksi yang sudah diizinkan dijalankan satu kali: `logs/poco-recheck-lp09-daily-stats.txt`, exit 0, daftar perangkat kosong. Tidak ada instalasi, peluncuran, input, screenshot atau profiling ponsel baru. Izin QA tetap berlaku; pengujian fisik belum dapat dilanjutkan sampai Poco tersedia.
