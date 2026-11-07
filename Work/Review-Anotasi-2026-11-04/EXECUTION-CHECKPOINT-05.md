# Checkpoint 05

Goal E01–E11 masih aktif. Ini pembaruan atas checkpoint04 dan seluruh turn sesudahnya. Root GAME SAHAM dan branch codex/classic-ui-new-assets. Save pemain hanya di-hash, masih 59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED. Turn ini menghasilkan source, test, build, dan server preview baru.

## Source sekarang

E02 completeDailyActivity sudah terhubung ke settleDay saat 23.59.59 sebelum reset00.00. Batch NPC terdanai dengan ensureLiquidity dan matching standar memenuhi sisa volume sesuai macro dan harga penutupan minimal satu fraksi berbeda. Harga floor/ceiling dan halt dipatuhi. simulateDailyMarketMovement lama yang membuat fill tanpa ledger dibuang. Lot RETAIL_NOISE kini20–200. Volume target peremiten ribuan–puluhan ribu berdasarkan macro. Pasangan NPC bergantian tiap hari. Test30hari catchup+reload berjalan dengan heap384MB. Ini belum pengukuran puncak memori/performa Android.

E09 getStockLeverageLimit memperhitungkan akses karier/level, tradable/halt, volume dan range harian. Parameter awal yang dipilih implementasi sudah diberitahukan di chat. Minimal1000lot, range<=10% untuk2x. CIO dengan5000lot dan range<=5% dapat3x. Di bawahnya1x. maxLeverage dikirim getInstruments, dipakai badge2x/3x serta tiketBUY MARKET PERSONAL. Tiket awalTunai, pilihan2x/3x muncul sesuai batas. Review menunjukkan modal/pinjaman/tarif, payload menyimpan multiplier dan marginPrincipalRaw yang disetujui. Estimasi tiket terbaru juga menampilkan modal sendiri dan pokok pinjaman.

previewLeveragedBuy membaca fill dan biaya matching, menghitung modal=ceil(total/multiplier), utang=sisa biaya, memeriksa plafon yang sama dengan confirm. Full-cash affordability pada matching preview tidak dipakai sebagai syarat leverage. confirmLeveragedBuy memeriksa ulang pokok disetujui, mencatat loan+order dalam lock facade, mensyaratkan FILLED dan totalbiaya yang identik, kemudian persist. Id order menyimpan multiplier/principal/requestId untuk pemeriksaan retry. Snapshot rollback memerlukan Account.copy karena SaveState.capture semula menyimpan referensi akun. save memakai captureRuntimeState(false), rollback memakai true. Kegagalan restore mengunci recovery. Setelah rollback berhasil, saveWriteFailure dibersihkan agar retry sah bisa berjalan. Bridge parse multiplier bilangan bulat dan principal presisi mata uang. Namespace MARGIN-LIQ tetap dicadangkan.

E06 bindResearchInstrumentAccess dipanggil setiap katalog diganti, termasuk initialize new game dan restore, agar akses tidak membaca katalog stale. E07 getFirmSnapshot kini menambahkan currentValue serta feesPaid/feesPaidRaw dari debit MANAGEMENT_FEE rekening klien. Laporan klien menampilkan dua nilai itu bila akun klien ada. Belum menambahkan return persentase karena harus menghitung arus setoran/penarikan dengan benar.

E03 daftarBeranda memakai createStockListItem bersamaPasar/pantauan/index. ContainerCSS juga mencakup home-market-list. Container rule2kolom diletakkan setelah media350px agar tidak tertimpa. Lima logo (ADRO/AMRT/ANTM/ARTO/ASII) memakai CSS window pada gambar referensi pemilik yang disalin utuh ke assets/www/images/referensi-logo-emiten.jpeg. Ukuran mengikuti30/34px. Logo lain tetapinisial. Tidak mengklaim seluruhlogo lengkap. Katalog belum punya ticker lain dari11logo referensi.

getLivePriceFeed target dan compactlist diperbaiki dari volumelembar menjadi volumelot sesuai lotSize. getInstruments memang sudah lot sebelumnya.

## Bukti terbaru

| Lingkup | Bukti |
|---|---|
| Regresi gabungan53tes sebelum laporanfee terbaru | Artifacts/Annotation-Execution/1793747898850145200/test.log exit0 |
| AntreanGTC pemain tetapOPEN/reservasi utuh pada penutupan+save/load, serta testpasar lain | Artifacts/Annotation-Execution/1793748051928528400/test.log,19tes exit0 |
| Risiko10x100+Firma10x100+Billing10x100 dan ownerpolicy pada source sebelum tambahan laporanfee | Artifacts/Annotation-Execution/1793748080463554800/test.log,44tes210,196detik exit0, session4810 terminal |
| Laporanfee sama dengan debitledger dan nilaiakun, plus ownerpolicy | Artifacts/Annotation-Execution/1793748182633096700/test.log,23tes exit0 |
| 2x dan3x lewat bridge, principalberubah ditolak, savefailure rollback/retry/reload | OwnerAnnotationPolicyTest.verifyLeveragedOrder pada bukti di atas |
| 30harigame dari15jamnyata, tepat30report, save/load | OwnerDailyMarketActivityTest pada bukti19tes dan regresigabungan |
| Kontrolchart/density, pasarIPO, risetfirma | Empat check-*.cjs exit0, DOMcontract dan perhitungan |

Build sebelumlogo terbaru sukses18detik,34tasks. APKsha dd12999b52976186edfe67c833ce5a9e605f8a137d236fa39f68e37d0d59fc80. App.js/index/style identik dalamZIP saat dicek. Setelahlogo ditambahkan, buildulang sedang dijalankan, poll session dari toolcall terbaru sebelum menyebut APK terkini. Seluruh build menggunakan JAVA_HOME JBR AndroidStudio dan JAVA_TOOL_OPTIONS -Djdk.net.unixdomain.tmpdir=.socket-qa terbatas proses, cwd android. Tanpa pathsocketpendek, Gradle mengalami Unable to establish loopback connection. Tidak mengubah konfigurasi global.

## Preview dan batas akses

Server baru aktif session31801, URL http://127.0.0.1:64134/. WireQaServer dikompilasi terhadap classes1793748182633096700, Javafee/current source. Assetweb dibaca langsung dari produksi. Save QA Artifacts/Annotation-Execution/qa-save-current-preview,159API. HTTPmeta dan halaman200 sudah diperiksa. Server64133 lama memakaiJava lama, jangan gunakan sebagai bukti terbaru. Tool CUA palingakhir melaporkan Local browser use is disabled in Work settings. Tidak ada screenshot atau perilaku rendered baru. Aktifkan browserlokal dari sisi pemilik diperlukan untuk visual QA melalui tool yang tersedia.

## Next action

Selesaikan poll buildlogo dan verifikasi asetJPEG+JS/CSS dalamZIP. Verifikasi5logo secara visual masihpending. E01/E03/E04/E05/E06/E07/E08 masih membutuhkan browser nyata, teksbesar/narrowkeyboard; E03seluruhlogo masihbelumlengkap. Kalibrasi parameterleverage awal dan fee/margin sudah diimplementasi, jangan menyatakan seluruhproduk complete dari regresisempit. E07 siklusfirma punya bukti10x100 domain, tampilanhasil aktual baru punya kontrak/23tes. Periksa requirementfirmprogress dan anggotaindeks terhadap sourceaktif. E10 validasiboundary/currency/recovery masih bisa ditambah bila gap ditemukan. E11APKdibangun berulang, perangkat membutuhkan izin yang berlaku diAGENTS. Jangan install/ADB otomatis.

Buildlogo session58854 terminal exit0, BUILD SUCCESSFUL11detik. APKsha256 6ca25a6fdb862c21d967019a8d58e6c0a1b3024d2c3ac11e4852f95d3a3b0de4. ZIP app.js/index.html/style.css/images/referensi-logo-emiten.jpeg identik dengan source setelah build. Tidak ada proses tes tersisa. Server31801 tetap disediakan untuk preview.
