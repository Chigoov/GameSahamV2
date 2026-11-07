# Checkpoint 07

Plan E01–E11 belum selesai. Batas leverage keputusan pemilik tetap maksimum 3x total daya beli dan maintenance margin 30%. Tidak ada perubahan angka ekonomi pada checkpoint ini; OwnerAnnotationPolicyTest.leverageCapMeansTotalBuyingPowerAndNeverExceedsThreeTimesEquity pada bukti checkpoint06 menguji plafon total dan penolakan tambahan utang, serta nilai maintenance 0.30.

## Perubahan dan bukti baru

- Logo lokal Simple Icons: US:ABT, US:AXP, US:CAT, US:KO; commit dan SHA256 tiap aset tercatat di images/stock-logos/SOURCES.json. Status hak logo diperiksa pada halaman Commons terkait. Lisensi koleksi disertakan.
- Logo lokal Commons: IDX:BBCA, IDX:BMRI, IDX:ICBP, IDX:GOTO, US:AAPL. Pilihan tepat diperiksa dari judul/deskripsi dan metadata Public domain. SVG diparse dan ditolak bila memiliki script, foreignObject, image/use, event handler atau href eksternal. Warna asli dipertahankan pada latar putih. Semua digunakan untuk identitas emiten.
- Total 14 emiten punya logo termasuk lima dari referensi pemilik; emiten lain masih inisial. Ini belum membuktikan E03 lengkap. Pencarian logo berikutnya menerima HTTP429; tidak mengulang permintaan massal. Kandidat dan kesalahan tersimpan di logo-candidates.json. US:GOOGL tidak diunduh/diaktifkan.
- createStockListItem memakai gambar lokal yang ditampilkan setelah load berhasil; onerror mempertahankan inisial. check-market-ipo-ui.cjs lulus untuk load/error, warna asli BBCA, hash aset, quote/sparkline dan tindakan IPO. node --check app.js dan git diff --check file aplikasi/QA terkait exit0. Ini kontrak DOM, bukan screenshot/rendered QA.
- WireQaServer melayani MIME SVG/PNG/JPEG/WebP yang benar. HTTP bytes dan MIME SVG BBCA serta JPEG referensi cocok dengan aset source.
- Build session69562 selesai exit0, BUILD SUCCESSFUL10s. APK android/app/build/outputs/apk/debug/app-debug.apk SHA256 9aec719b159993fe2ce8bcfd65811cb37775c6454e242cc51c21935b9dc48ada. Lima belas file web/logo/provenance/license di ZIP identik dengan source. Tidak ada source aplikasi berubah setelah build; perubahan berikutnya hanya check dan checkpoint.
- Save pemain Artifacts/LocalPreview/preview-save/save_game_v2.json tetap SHA256 59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED.

## Preview dan hambatan

Server task-owned15328 dihentikan sesudah identitas CIM diverifikasi, WireQaServer dikompilasi ulang terhadap classes1793748743347910600. Restart pertama gagal karena PowerShell memecah argumen -D tanpa tanda kutip; perbaikan argumen bertanda kutip berhasil. Server aktif session52370, http://127.0.0.1:64134/,159API, save QA terisolasi qa-save-current-preview. Aplikasi/source terbaru dibaca langsung oleh server ini.

open_in_codex untuk preview di panel kanan mengembalikan queued, bukan bukti panel sudah terlihat. cua.getState pada checkpoint ini kembali melaporkan Local browser use is disabled in Work settings dan browsers kosong. Hambatan yang sama sudah tercatat pada checkpoint awal,05,06 dan sekarang07. Permintaan async sebelumnya belum dijawab. Jangan menggunakan browser lain untuk mengakali pengaturan. Tidak ada screenshot/rendered QA baru, bukti Android runtime atau perangkat.

Status goal BLOCKED sesudah hambatan browser berulang dan pekerjaan independen relevan pada snapshot ini diselesaikan. Ini bukan COMPLETE. E03 logo lengkap masih pending akibat HTTP429 dan visual E01/E03–E08 masih perlu diperiksa. Next action setelah akses berubah: lanjut preview64134, verifikasi chart/pasar → IPO → Riset → Firma → Indeks pada viewport sempit, teks besar, keyboard; perbaiki temuan dan lengkapi logo yang belum ada saat sumber kembali tersedia. Build ulang hanya bila source aplikasi berubah. Tidak ada commit/push/ADB/install atau perubahan pengaturan global.
