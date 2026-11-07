# Checkpoint 09

Goal aktif dan belum complete. Turn08 adalah progres; turn09 juga progres: browser pulih, drag/seleksi chart dan pembelian3x diuji nyata, perbaikan receipt/pesan sinkronisasi/gembok saham, empat logo tambahan, tes dan APK. Tidak ada alasan menandai blocked pada snapshot ini.

## Browser dan bukti baru

IAB baru tab3 menggantikan tab2 yang bermasalah. getState/getTab3 berhasil. Preview normal64134 dan tab3 berhasil markDeliverable. Override viewport399×615 dipakai untuk bukti lalu reset berhasil; URL akhir normal64134 tanpa qaTextScale, chart fullscreen. Jangan mewarisi error browser.user is unavailable sebagai kondisi kini.

Folder bukti masih Artifacts/Annotation-Execution/browser-resume-2026-11-04/.

- Screenshot chart kini cocok DOM4kolom. chart-verified-399.png dan chart-final-399.png menggantikan klaim visual yang belum sah pada chart-latest-399.png. Gambar akhir memperlihatkan29Okt23.00/Rp10.475 dan kontrol4kolom.
- Drag nyata CUA pada chart30hari setelah zoom mengubah rentang27Okt07.00–02Nov07.00 menjadi23Okt07.00–29Okt07.00. Memilih titik secara langsung menghasilkan25Okt07.00/Rp10.375. Ini bukti pointer browser, bukan dua sentuhan perangkat.
- chart-text200-matrix.json sekarang memuat delapan ukuran sesudah CSS grid akhir; root32px, semua kontrol muat dan tidak ada luapan. Bersama chart-normal-matrix.json dan dua matrix indeks dari08, ada bukti geometri normal/200%; tidak mengubahnya menjadi klaim visual semua layar/perangkat.
- QA64136 dibuat dengan save baru qa-save-leverage-ui-20261104. Work/WireQaServer menambah action seedLeverageEligibility: rankCIO dan volume BBCA5000lot sintetis untuk membuka prasyarat UI, generate likuiditas lalu save. Fixture diberi label eksplisit; bukan bukti volume transaksi nyata/kemajuan karier.
- Tiket market menampilkan Tunai/2x/3x dan tidak4x. Estimasi2x untuk1000saham menampilkan modal sendiri5.145.207/pinjaman5.145.206. Tinjauan3x kemudian modal sendiri3.430.138/pinjaman6.860.275, total pembelian+fee10.290.413. Order3x terisi1000/0 pada rata-rata10.275, fee15.413. Riwayat sesudah restart server/reload menyimpan funding yang sama; leverage-3x-receipt-restored-399.png. Snapshot review leverage-3x-review-399.png juga memuat toast penolakan lama dari percobaan saat sinkronisasi; jangan menyebutnya gambar review bersih.
- AAPL yang terkunci terlihat dan dapat dibuka untuk membaca data. Browser menemukan JUAL semula enabled sementara BELI disabled; live refresh juga dapat mengaktifkan BELI kembali. Sesudah perbaikan, kedua tombol disabled pada pembukaan dan sesudah refresh; locked-aapl-399.png.
- Empat SVG baru Google/Microsoft/Meta/Netflix loaded/naturalWidth>0 pada daftarUS. market-us-logos-399.png. Semua sembilan SVGUS yang dicoba loaded. Total aset logo kini13SVG+5sprite referensi=18emiten; lainnya masih inisial.

## Perubahan produksi

1. Bridge Proxy tidak lagi mengembalikan{} untuk cache miss saat clock sync. Cache yang tersedia tetap dibaca tanpa memanggil native; cache miss melempar pesan Data sedang diselaraskan. Coba lagi sebentar. Write tetap ditolak saat sync. Check proxy pada check-research-firm-ui.cjs merah sebelum perubahan (missing expected exception), hijau sesudahnya. Loader yang ditunda pada08 tetap dipertahankan.
2. Tinjauan dan estimasi leverage memakai formatMoney existing. Nilai total trading diberi label Total pembelian & fee; modal sendiri dan pinjaman terpisah, tidak menyebut seluruh biaya sebagai dampak kas pribadi.
3. getOrderStatus mengurai metadata pendanaan dari ID durable yang dibuat confirmLeveragedBuy, format LEVERAGE-multiplier-principal-requestId; hanya multiplier2/3 dan requestId yang cocok. Menambahkan leverageMultiplier/marginPrincipalRaw/ownCashRaw; ownCash hanya tersedia jika fill+fee aktual lengkap. submitOrder menyertakan data status aktual bersama hasil submission. UI receipt menunjukkan total pembelian+fee, modal sendiri, pokok pinjaman. Receipt tunai mempertahankan label kas bersih. Tidak mengarang fee/cash bila histori lama tidak cukup. OwnerAnnotationPolicyTest memeriksa nilai terhadap perubahan kas/debt2x/3x dan sesudah reload, bukan sekadar field.
4. updateInstrumentTradeAccess dipakai oleh initial detail dan live refresh. Kedua tombol diblokir jika tidak aktif/halt/terkunci; label locked/level ikut diperbarui. activeDetailStock diisi fresh supaya handler tidak memakai boolean lock lama. Opacity/alert dengan lock yang tertangkap saat pembukaan dihapus.
5. orderSectorAccessError menyatukan gate preview dan submit personal. BUY/SELL manual pada sektor terkunci ditolak sesuai pilihan pemilik; replay tetap memakai aturannya sendiri. Namespace internal SELL MARGIN-LIQ dikecualikan untuk risiko otomatis; bridge masih menolak pemain memalsukan namespace itu. Gate akun klien berdasarkan karier/mandat existing tidak diubah.

## Tes dan penyesuaian fixture

- Red receipt metadata: Artifacts/Annotation-Execution/1793776322207762700/test.log,15tes/1gagal (leverageMultiplier belum ada).
- Red lock preview:1793777012022487700/test.log,16tes/1gagal; preview BUY AAPL terkunci sebelumnya sukses. Owner test baru memeriksa preview+submit BUY/SELL manual, saldo/posisi tidak berubah, dan order namespace likuidasi internal tidak ditolak oleh gate level. Pengujian deadline/counterparty ada pada OwnerMarginDeadlineTest, terpisah dari order internal fixture ini.
- Node red lock state: check-market-ipo-ui.cjs pada source lama menghasilkan false!==true dari live button-state block. Sesudah shared helper, menguji terkunci→terbuka→halt. Receipt UI menguji pemisahan funding dan receipt tunai tetap memakai kas bersih.
- Core terakhir:1793777230147182300/test.log,20tes2.529detik, exit0. OwnerAnnotationPolicyTest16 + DividendAndSectorLockTest + OrderCapacityQuoteTest. SourceJava sama dengan run35tes sebelumnya1793777043376683400/test.log,13.434detik, mencakup OwnerMarginDeadlineTest, RiskMarginShortComplianceTest, CareerAccessTest.
- TradingWebBridgeTest38tes awal dijalankan terhadap source baru dan menghasilkan3failure: katalog masih mengharapkan100 meskipun startup kini mencatatPORT/POWR; fixture USD creditCash melewati ledger; resting ask berdasarkan fallback bid9000 justru marketable.
- Test diperbaiki tanpa melonggarkan tujuan: jumlah102 dengan assertPORT/POWR ada; USD dimasukkan via ledger DEPOSIT dan assert250ktersisa/reconcile sesudahPro; resting sell memakai ceiling valid dan overbooked memakai harga sama, lalu assert penolakan Saham telah direservasi. Assertion reason pertama memakai teks tidak tepat; dibetulkan setelah membaca source guard reservasi. Log awal bridge-test.log/bridge-test-after.log/bridge-test-final.log di1793776349034686300 mempertahankan jejak tersebut.
- Bridge38tes terakhir dikompilasi/dijalankan ulang terhadap classes1793777230147182300, exit0,5.61detik. Bukti1793777230147182300/bridge-test-final.log. Tidak perlu mengubah runner untuk suite mobile; memakai JUnit existing dan tmp/asset terisolasi yang sama.
- Node syntax dan check-chart-controls/check-market-ipo/check-research-firm terakhir exit0. Tes density tidak disentuh oleh perubahan funding/gate. Test baru/expected values di atas bagian dari diff yang wajib direview, bukan verifier yang dilemahkan.

## Logo

logo-batch2.json berisi metadata Commons satu batch. Memilih GOOGL/META/MSFT/NFLX, berstatus Public domain dan judul/deskripsi sesuai perusahaan. Amazon/Visa dalam batch ditandai versi lama sehingga tidak dipakai. NVDA/TSLA belum terunduh: HTTP429 setelah empat unduhan; tidak mengaktifkan map untuk file gagal. SVG diparse, menolak tag aktif/resource eksternal/event handler/@import/url eksternal. Provenance URL/status/hash ada pada SOURCES.json per aset. Berikutnya gunakan batch kecil dengan jeda wajar (jangan mengulang burst paralel seperti percobaan awal) dan periksa identitas/versi/logo, bukan memilih hasil pencarian pertama secara otomatis.

## APK, server dan batas

Build37386 terminal exit0, BUILD SUCCESSFUL15detik. APKsha256 f60680f8d39d45d4d4b11d516f7773e4add77d7052b720495143cc30593fd088. Sebanyak19aset dalamZIP identik dengan source. HTTP JS/CSS64134 juga cocok. source-apk-parity-09.json menyimpan hash aplikasi/test/APK dan jalur bukti tes. Save pemain tetap SHA25659B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED.

Server64134 terbaru session70009 dan64136 session11011 memakai classes1793777230147182300 + Work harness terbaru. Proses task-owned17160/3144 lama dihentikan sesudah identitas CIM diverifikasi. Dua server pengganti berhasil159API/saveQAmasing-masing. 64135/session71235 tidak disentuh turn ini; verifikasi hidup/classes/offset sebelum memakainya. Jangan menganggapnya sourcefinal (lihat08). Tidak ada build/JUnit yang masih berjalan pada akhir turn. Tidak ada ADB/install/commit/push/global settings/perubahan save pemain.

## Sisa dan next action

Goal tetap mencakup E01–E11; tidak complete dari58tes terakhir, screenshot atau APK saja. Prioritaskan logo yang belum lengkap dengan request sopan dan provenance, lalu audit matriks requirement/bukti untuk gap nyata yang tersisa (terutama tampilan margin call/deadline, state Riset/Firma lebih lanjut bila belum terlayani). Chart pinch punya kontrak DOM, bukan bukti multi-touch native; jangan mengklaim perangkat. Gunakan tab3 yang berhasil markDeliverable, URL normal64134. Viewport override sudah reset. Jangan mengulang pemeriksaan hijau tanpa source/gap baru atau menambah fitur di luar keputusan pemilik.
