# Fondasi domain akun waktu dan save

ID: 01. Status awal: PENDING. Cakupan: FR-01, FR-05, FR-19.

## Acuan dan dependensi

Baca [PRD](../../Docs/Product/02-PRD-Game-Saham-2D-Portrait.md) dan [aturan repo](../../AGENTS.md). Dependensi: [00](../00-preflight-dan-keputusan/TASK.md). Dependensi adalah urutan logis; setelah kontrak dasar jelas, kerjakan bagian independen tanpa menunggu semua pekerjaan lain selesai. Satu writer repo.

## Pekerjaan

Bangun fondasi minimum untuk run/mode terpisah, uang presisi tetap, identitas akun/instrumen, ledger, clock tunggal, command idempoten dan save berversi. Satu sumber kebenaran ekonomi; jangan menduplikasi di UI. Parameter pengembangan sementara wajib berlabel dan tidak menggantikan keputusan produk.

## Hasil yang harus tersedia

Domain minimum, penyimpanan atomik/backup, dan check runnable untuk konsistensi akun, retry dan recovery.

## Kriteria penerimaan

Kas/posisi/ledger konsisten; run dan akun terisolasi; save gagal mempertahankan salinan valid; waktu/resume tidak menggandakan efek. Acceptance yang bergantung OQ tetap pending.

## Checkpoint dan bukti

Simpan bukti nyata pada Artifacts/01/ saat mulai menguji. Jangan membuat log hasil palsu. Perbarui [STATUS.json](../STATUS.json), [REQUIREMENTS.csv](../REQUIREMENTS.csv), [SCREEN-COVERAGE.csv](../SCREEN-COVERAGE.csv), [DECISIONS.md](../DECISIONS.md), dan [ACCEPTANCE.md](../ACCEPTANCE.md) setelah checkpoint aman. Cantumkan file berubah, command/hasil, lingkungan uji, blocker, dan next_action. Status lulus membutuhkan path bukti; keberadaan file atau output build saja tidak membuktikan gameplay.
