# Gate penerimaan Game Saham 2D Portrait

Status awal seluruh gate: NOT_TESTED. Dokumen bukan bukti runtime. Perbarui status beserta path bukti, lingkungan, metode, build/source revision dan batas hasil.

| Gate | Bukti yang diperlukan | Status awal |
|---|---|---|
| G01 cakupan produk | Seluruh requirement/subrequirement PRD dan 26 cakupan layar memiliki implementasi serta journey/check bermakna | NOT_TESTED |
| G02 ekonomi dan order | Ledger, akun terisolasi, matching/partial/cancel/retry, FX, fee, utang dan aksi korporasi dapat direkonsiliasi | NOT_TESTED |
| G03 karier dan firma | Tutorial sampai promosi/klien, reward idempoten, mandat, cashflow, trust dan recovery | NOT_TESTED |
| G04 historis dan lab | Dataset nyata dengan kontrak terpilih, provenance, anti future leak, raw/adjusted konsisten, lab terisolasi | NOT_TESTED |
| G05 portrait dan aksesibilitas | Viewport portrait, keyboard, Back, font scale, sentuh, state layar dan angka jelas; native diperiksa terpisah | NOT_TESTED |
| G06 save dan lifecycle | Autosave/manual, crash/force-stop, storage failure, corrupt-save recovery dan migrasi sesuai kebijakan | NOT_TESTED |
| G07 APK standalone | Build/versi/checksum, inspeksi paket, aset lokal, fresh install dan update sesuai target penerimaan | NOT_TESTED |
| G08 perangkat dan performa | Target/ambang disepakati; ukuran APK/install/data, startup, respons, memori, lifecycle, baterai bila dipersyaratkan | NOT_TESTED |

## Aturan lulus

- Semua 20 FR adalah scope rilis pertama. Work/REQUIREMENTS.csv merinci butir bernomor, kriteria penerimaan dan syarat lintas fitur; tambahkan bila audit menemukan butir belum terpetakan, jangan menghapusnya demi PASS.
- Work/SCREEN-COVERAGE.csv memetakan 26 layar konsep ke FR; halaman boleh digabung tanpa menghilangkan fungsi.
- Screenshot tidak membuktikan transaksi; static test tidak membuktikan bridge/native; APK tidak membuktikan terpasang atau nyaman di HP.
- Tidak ada blocker kritis/mayor uang/order/save dan tidak ada OQ material belum selesai untuk scope yang dinyatakan complete.
- OQ-18 masih menentukan batas penerimaan perangkat pemilik. Bukti fisik belum tersedia harus dinyatakan pending; jangan menyebut device-tested tanpa pengujian.
- Tidak ada target MB/RAM/FPS yang ditetapkan diam-diam. Catat hasil aktual dan ambang yang disepakati. Angka perangkat 3D lama bukan syarat produk baru.
- Sediakan panduan pemain singkat, catatan batas data, known issues dan pemulihan save bersama APK.
