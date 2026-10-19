# Gate penerimaan Game Saham 2D Portrait

Status awal seluruh gate: NOT_TESTED. Dokumen bukan bukti runtime. Perbarui status beserta path bukti, lingkungan, metode, build/source revision dan batas hasil.

| Gate | Bukti yang diperlukan | Status | Catatan / Path Bukti |
|---|---|---|---|
| G01 cakupan produk | Seluruh requirement/subrequirement PRD dan 26 cakupan layar memiliki implementasi serta journey/check bermakna | PARTIAL | R01: UI/domain parsial; 228 butir diaudit individual di Artifacts/Remediation/R01/requirement-audit.csv. 58 XML lama bukan bukti seluruh journey. |
| G02 ekonomi dan order | Ledger, akun terisolasi, matching/partial/cancel/retry, FX, fee, utang dan aksi korporasi dapat direkonsiliasi | PARTIAL | R01: tes JVM subset; risiko retry/reservasi/fee/shared settlement perlu reproduksi R02. reconcileAll hanya kas, bukan pembukuan double-entry penuh. |
| G03 karier dan firma | Tutorial sampai promosi/klien, reward idempoten, mandat, cashflow, trust dan recovery | PARTIAL | R01: tes misi manual/akun/fee subset; journey karier-klien dan persistensi belum tersedia penuh. |
| G04 historis dan lab | Dataset nyata dengan kontrak terpilih, provenance, anti future leak, raw/adjusted konsisten, lab terisolasi | BLOCKED | R01: tidak ada dataset nyata/provenance/hak terverifikasi; candle tes sintetis. OQ-03–05 OPEN. |
| G05 portrait dan aksesibilitas | Viewport portrait, keyboard, Back, font scale, sentuh, state layar dan angka jelas; native diperiksa terpisah | PARTIAL | R03/integrity-111: 31 responsive/10 scroll samples GREEN setelah label dan viewport pendek diperbaiki; screenshot subset dibaca. Seluruh screen/state belum diuji; CSS px bukan dp; native tetap PENDING. |
| G06 save dan lifecycle | Autosave/manual, crash/force-stop, storage failure, corrupt-save recovery dan migrasi sesuai kebijakan | PARTIAL | R06 dan R02/STEP-INTEGRITY.md: order/utang/collateral/karier/mandat/report/FX/book/fund/PnL tersimpan pada subset tes; backup/writefail/corrupt/recovery diuji host. State events/run/RNG dan migrasi lengkap belum; native lifecycle PENDING. |
| G07 APK standalone | Build/versi/checksum, inspeksi paket, aset lokal, fresh install dan update sesuai target penerimaan | PARTIAL | R07/stress-121: build/121 tes EXECUTED, 204.459 byte, 5 aset exact, debug signature valid, lint 0 error 9 warning; hash/source pada REPORT.md. Instalasi/update tetap PENDING tanpa ADB; source berikutnya wajib rebuild. |
| G08 perangkat dan performa | Target/ambang disepakati; ukuran APK/install/data, startup, respons, memori, lifecycle, baterai bila dipersyaratkan | BLOCKED | OQ-16/18 OPEN. Tanpa ADB/perangkat pada fase ini; tidak membuat klaim keadaan perangkat saat ini. Pengukuran tetap PENDING. |

## Aturan lulus

- Design brief Docs/Design/03-Design-Brief-Game-Saham-2D-Portrait.md adalah acuan UI sebelum kode game. Periksa token/state aktual, semua 40 tujuan screen, 20 journey dan 28 peran komponen; implementasi boleh reuse markup/native tanpa satu class per ID. Dokumen lengkap/rasio token yang benar tidak membuktikan aksesibilitas pada runtime.
- Semua 20 FR adalah scope rilis pertama. Work/REQUIREMENTS.csv merinci butir bernomor, kriteria penerimaan dan syarat lintas fitur; tambahkan bila audit menemukan butir belum terpetakan, jangan menghapusnya demi PASS.
- Work/SCREEN-COVERAGE.csv memetakan 26 layar konsep ke FR; halaman boleh digabung tanpa menghilangkan fungsi.
- Screenshot tidak membuktikan transaksi; static test tidak membuktikan bridge/native; APK tidak membuktikan terpasang atau nyaman di HP.
- Tidak ada blocker kritis/mayor uang/order/save dan tidak ada OQ material belum selesai untuk scope yang dinyatakan complete.
- OQ-18 masih menentukan batas penerimaan perangkat pemilik. Bukti fisik belum tersedia harus dinyatakan pending; jangan menyebut device-tested tanpa pengujian.
- Tidak ada target MB/RAM/FPS yang ditetapkan diam-diam. Catat hasil aktual dan ambang yang disepakati. Angka perangkat 3D lama bukan syarat produk baru.
- Sediakan panduan pemain singkat, catatan batas data, known issues dan pemulihan save bersama APK.


## Checkpoint audit owner 19 Oktober 2026

Status G01–G03/G05–G07 tetap PARTIAL; G04 dataset BLOCKED; G08 runtime/performa BLOCKED oleh ketiadaan emulator/perangkat. Bukti baru tidak meluluskan seluruh PRD.

- 153 tes/14 suite/18 regresi baru; 13 browser checks pada host Java; 20 sampel portrait; 0 overflow horizontal. Scroll vertikal masih ada dan V8 fixed-page belum diintegrasikan.
- Build APK debug dan signature PASS, 7 aset paket exact; lint 0 error/13 warning. Instalasi/native Back/keyboard/background/force-stop/performa NOT_TESTED.
- G02/G03 masih memerlukan penutupan gate karier/sesi/entitlement, laporan klien dan acceptance fitur lanjutan. G06 native masih belum terverifikasi walau autosave source ditambahkan.
- Target utama Poco X8 Pro 12/512, instalasi pertama; emulator diizinkan tetapi emulator binary/system image/AVD tidak tersedia saat audit.
- Bukti terbaru dan daftar gap: [audit owner](OWNER-AUDIT-2026-10-19.md), [matrix UI](OWNER-UI-LOGIC-MATRIX.csv), `Artifacts/OwnerAudit-2026-10-19/verification.json`. Hash/ukuran APK lama di tabel historis di atas tidak mewakili kandidat terbaru.
