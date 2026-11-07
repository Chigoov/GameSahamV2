# Checkpoint 06

Goal E01–E11 aktif. Turn ini menghasilkan dua reproduksi gagal, perbaikan fungsi bersama, bukti uji, APK dan refresh server preview. Root GAME SAHAM, branch codex/classic-ui-new-assets. Save pemain masih SHA256 59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED.

## Perbaikan

Chart sebelumnya dapat mempertahankan selectedTime di luar jendela setelah pan. Readout kemudian membaca harga candle yang sudah tidak tampak dan crosshair berada pada candle lain. check-chart-controls.cjs menambah assert jendela tampak dan gagal pada source lama. render membatasi selectedTime ke timestamp awal/akhir bar tampak sebelum menentukan readout/crosshair. Pilihan di dalam jendela tetap stabil selama refresh. Kontrol chart dan density lulus setelah perubahan.

RiskComplianceService.accrueDailyFinancingCosts sebelumnya mengapitalisasi kembali bunga ketika tanggal yang sama dipanggil ulang dengan timestamp berbeda, termasuk setelah restore. Reproduksi OwnerAnnotationPolicyTest menghasilkan utang40.016.002 padahal seharusnya tetap40.008.000 setelah charge pertama. Bukti merah Artifacts/Annotation-Execution/1793748515564593700/test.log. Method kini menyimpan epochDay WIB terakhir perakun dalam financingDays dan melewati tanggal sama/lebih lama. Reset menghapusnya. Export/restore menyimpan array accountId/day. Restore memeriksa tanggal sah, akun tidak kosong/duplikat, dan facade memeriksa tautan akun serta tanggal tidak melampaui waktu save. Field opsional untuk save lama. Caller produksi hanya settleDay, sudah ditelusuri.

Test kapitalisasi menguji IDR dan USD selama3hari serta restore/retry tiap hari. Expected IDR40.024.005 dan USD10.006,00 dipertahankan. Test tambahan memastikan bunga yang dibayar dari kas juga hanya sekali pertanggal setelah restore dan tanggal berikutnya tetap ditagih. Keduanya memeriksa efek saldo/utang, bukan sekadar field.

Label Data alternatif di Riset memakai Whale agar sesuai nama paket yang ditampilkan.

## Bukti

- 38tes margin/pembiayaan/worldclock setelah perbaikan awal, exit0. Artifacts/Annotation-Execution/1793748559866051800/test.log.
- 63tes regresi gabungan, exit0. Artifacts/Annotation-Execution/1793748610187635700/test.log. Mencakup OwnerDailyMarketActivityTest, OwnerMarginDeadlineTest, RiskMarginShortComplianceTest, UnifiedWorldClockTest, ResearchNewsTerminalTest, IpoScheduleAndAllotmentTest, ChartHistoryRangeTest, CareerAccessTest. Run selesai sebelum perubahan kurung pada loop validasi, yang menjaga perilakunya sama.
- 30tes terbaru termasuk test bunga dibayar tunai dan10x100 RiskMarginShortComplianceCriticalSequenceTest, exit0,105,017detik. Artifacts/Annotation-Execution/1793748743347910600/test.log. Session13180 terminal. SourceJava pada run ini sudah termasuk loop validasi akhir.
- Empat check-*.cjs dan node --check app.js exit0. Kontrak DOM/perhitungan, visual masih pending.
- Build APK terbaru session56497 terminal exit0,10detik. BuildJava perbaikan sebelumnya session55763 sukses16detik. APKsha256 93e57ebfeea00e8d92cb65fc74b149598075325305d77dd2861a01870c9fb1bb. Empat aset app.js/index/style/JPEGlogo identik di ZIP.

## Server preview

Server64134 lama telah diganti dengan classes1793748743347910600 yang memuat perbaikan pembiayaan. Pertama restart gagal bind karena Get-NetTCPConnection tidak menemukan listener meski server lama masih hidup. Diagnosis CIM menemukan proses task-owned14132 dengan WireQaServer dan qa-save-current-preview64134. Proses itu dihentikan setelah identitas diverifikasi, lalu server baru berhasil.

Server saat ini session76535, http://127.0.0.1:64134/,159API, save QA terisolasi yang sama. HTTPmeta diperiksa setelah restart. Session31801 sudah dihentikan, bukan handle aktif. Tidak ada uji/build lain yang masih berjalan.

## Status dan pekerjaan tersisa

E01/E02/E05/E06/E08/E09/E10 mempunyai perubahan domain dan regresi sebagaimana checkpoint05/06. E03 lima logo referensi tersedia, emiten lain masih inisial. E04 perilaku chart punya kontrak DOM, belum rendered QA. E07 firma mempunyai uji siklus10x100 dan laporan nilai/fee aktual, tetapi hasil UI/progres perlu verifikasi visual. E11 APK terbangun dan aset cocok, tanpa bukti Android runtime/perangkat.

Permintaan async sudah dikirim agar pemilik mengaktifkan akses browser lokal di Work. Tool terakhir menyatakan Local browser use is disabled in Work settings. Jawaban belum diterima pada checkpoint ini. Jangan menganggap waktu berlalu sebagai izin atau browser sudah aktif. Tunggu jawaban sambil melakukan pekerjaan independen yang mempunyai gap nyata. Jangan menambah tes/fitur semata untuk menghasilkan aktivitas atau menyatakan goal complete dari snapshot ini.
