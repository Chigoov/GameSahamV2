# Checkpoint: 100 perusahaan aktif dan kategori

Tanggal: 21 Oktober 2026, WIB. Root: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`.
Baseline Git: `8c61169`; pekerjaan tersimpan sebagai local changes, tidak dipush. Satu subpekerjaan fase 02 katalog selesai untuk scope source/domain/browser/APK; ini bukan audit ulang seluruh 14 fase atau kelulusan perangkat fisik.

## Permintaan dan hasil

Pemilik meminta minimal 100 perusahaan berkategori dan seluruh perusahaan aktif. Hasil katalog internal: **100 perusahaan unik, 50 IDX + 50 US, 0 nonaktif, 12 kategori**. Enam entri yang sebelumnya pasif tetap ada dan sudah aktif. ID serta parameter pasar 26 entri sebelumnya dipertahankan; nama ADRO diperbarui mengikuti identitas Alamtri pada sumber primer.

| Kategori game | Jumlah |
|---|---:|
| Keuangan | 22 |
| Konsumen Primer | 14 |
| Kesehatan | 11 |
| Teknologi | 10 |
| Energi | 10 |
| Konsumen Nonprimer | 8 |
| Industri | 7 |
| Bahan Baku | 7 |
| Komunikasi & Media | 4 |
| Infrastruktur & Telekomunikasi | 3 |
| Properti & Real Estat | 3 |
| Utilitas | 1 |

Kategori berbahasa Indonesia dikurasi untuk gameplay, bukan klaim klasifikasi resmi IDX-IC/GICS. Status aktif berlaku untuk pasar internal game. Aturan sesi/halt/karier dan keterbatasan dataset replay tetap berlaku. Tidak menambah data historis sintetis atau feed pasar nyata.

Identitas ticker/nama diperiksa terhadap sumber primer:
- KSEI: https://web.ksei.co.id/services/registered-securities/shares?setLocale=en-US
- Nasdaq: https://www.nasdaqtrader.com/dynamic/SymDir/nasdaqlisted.txt
- Direktori bursa AS lain dari Nasdaq Trader: https://www.nasdaqtrader.com/dynamic/SymDir/otherlisted.txt

Pilihan identitas dan hasil pengambilan sumber disimpan di `Artifacts/Catalog100-2026-10-21/source-evidence/`. Path KSEI tanpa parameter bahasa sempat mengembalikan HTTP500; jalur resmi en-US berhasil. Halaman utama sempat berisi 979 kode; hanya 50 dipilih untuk katalog ini.

Harga dasar dan jumlah saham adalah parameter simulasi internal, bukan harga live/fundamental terverifikasi. Seed 26 entri lama tetap dipertahankan; 74 tambahan memakai harga awal IDR5000/USD100 dan 1 miliar saham simulasi. Kebijakan ini dicatat dalam `market_universe.json`. Indeks game tetap basket existing, tidak mengklaim cakupan indeks resmi penuh.

## Perubahan

- `android/app/src/main/assets/data/market_universe.json`: 100 entri aktif, kategori, versi katalog dan kebijakan sumber/parameter simulasi.
- `android/app/src/main/assets/data/dataset_manifest.json`: jumlah universe 50/50/0; metadata historis lama di luar count tidak ditinjau ulang pada tugas ini. Otoritas dataset replay tetap `historical/replay_provenance.json` dan loader, dengan dua dataset karantina tetap dikeluarkan dari APK.
- `InstrumentCatalog.java`: versi market state 2; validasi cakupan exact tetap ada.
- `TradingFacade.java`: migrasi khusus save unversioned/v1 yang mempunyai tepat baseline lama 26 ID, termasuk IPO yang dibuktikan allotment. Entry awal yang hilang pada save v2, duplikat, ID tak dikenal atau versi invalid tetap ditolak. Aktivasi/penambahan inventory market maker hanya pada baseline baru/diaktifkan saat migrasi; IPO tidak diberi inventory baru. Reload biasa tidak mengisi kembali posisi yang sudah dijual.
- `app.js`/`index.html`: filter kategori native berasal dari katalog domain, dapat digabung dengan query/bursa/watchlist; kategori tampil di row dan detail. Quote timer tetap memperbarui row di tempat.
- `style.css`: judul dapat wrap, teks highlight dapat patah baris, shortcut Pasar mengikuti lebar/font; menutup overflow yang ditemukan pada 320px teks200%.
- Tes katalog/bridge diperbarui sesuai requirement baru; `CatalogExpansionRegressionTest.java` menguji migrasi dan jalur penolakan.

## Bukti dan pemeriksaan

1. Targeted awal: 11 tes PASS. Targeted akhir migrasi + bridge: 41 tes PASS.
2. Full regression final: `Work/run-codex-jvm-check.ps1 -OutputDirectory Artifacts/Catalog100-2026-10-21/regression-final` → **461 tes PASS**, 0 failure, 209,446 detik. Output: `regression-final/junit-latest.log` dan `regression-final-console.log`.
3. Full regression awal 458 tes mempunyai satu assertion lama yang masih mengharapkan 20 entry. Assertion diselaraskan dengan requirement100 dan flag aktif diperiksa satu per satu; kemudian seluruh suite dijalankan ulang. Tidak menghapus tes kegagalan save/unknown ID.
4. Migrasi: saldo/posisi/harga/volume/watchlist/date/runID bertahan; load tidak menimpa file asli; save berikutnya mempertahankan byte lama pada `.bak`. Sold-out inventory tetap habis sesudah reload. Legacy IPO menjadi101 tanpa inventory IPO baru; internal/replay slot mempertahankan identitas; satu NEE yang hilang pada v2 ditolak. Null/string/fractional/future version, unknown dan duplicate ID juga ditolak.
5. Browser host Java terisolasi pada port8791/save baru (bukan Android; port8784 existing tidak disentuh). Delapan kelompok pemeriksaan:100 terlihat; 50/50 dan12kategori; seluruh kategori/filter; BBNI aktif; persistensi filter/fokus/row identity; watchlist/detail;14 viewport-font samples; performa30sampel. **PASS**, 0 pageerror.
6. Tujuh viewport × teks100/200%, tidak ada overflow horizontal halaman/main. Screenshot 320 dan390 diperiksa. Raw timing: kategori P95 sekitar18,78ms; tick katalog100 P95 sekitar11,23ms. Ini benchmark host/browser, bukan FPS perangkat.
7. `node --check app.js` PASS. Build `assembleDebug --no-daemon --offline` PASS dalam16detik. Gradle mengeluarkan peringatan deprecation existing; tidak ada error build. Android Studio JBR dengan workaround Unix-domain socket pendek/nonexistent per invocation dipakai; tidak mengubah environment global.
8. APK ZIP CRC PASS; katalog dalam APK identik dengan source100; kelima aset www cocok byte-for-byte. Dua CSV karantina tidak ada dalam APK. Application ID tetap `com.tradingglobal3d.mobile`, minSDK26/target36.
9. `apksigner verify --verbose --print-certs` PASS v2; sertifikat debug sama dengan APK baseline. Tidak mengklaim production release signing.
10. Reviewer berbeda melakukan review read-only terhadap source/migrasi/test/UI, temuan versi/IPO/kategori ditutup, verdict akhir tidak ada correctness blocker pada diff yang direview.

Hasil terstruktur: `Artifacts/Catalog100-2026-10-21/verification.json`.
Daftar perusahaan: `Artifacts/Catalog100-2026-10-21/companies-100.csv`.

## APK baru

- `Artifacts/Catalog100-2026-10-21/GameSaham-100-Perusahaan.apk`
- Ukuran: **877120 byte**.
- SHA-256: `77faf2da9a6bb6b3297cd4cb1db3b875d7a9aceb16e4009ea67baa97c19a1024`.
- APK baseline sebelumnya tetap disimpan; tidak di-overwrite.

Hash save pribadi sebelum/sesudah tetap:
`59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`.

## Batas dan next action

Scope request katalog100 sudah dipenuhi. Instalasi/runtime Android fisik belum diuji; tidak menjalankan ADB/emulator. Pekerjaan UX/motion/realtime pada `NOTE-OWNER-UX-MOTION-REALTIME-2026-10-21.md` tetap catatan untuk tugas terpisah, belum diimplementasikan oleh perubahan katalog ini. Tidak menjalankan Hermes, tidak push, tidak menambah dependency produksi atau mengubah save pribadi.
