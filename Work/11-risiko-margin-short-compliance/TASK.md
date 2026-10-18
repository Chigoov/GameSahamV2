# Risiko margin short dan compliance

ID: 11. Status awal: PENDING. Cakupan: FR-04, FR-15, FR-16.

## Acuan dan dependensi

Baca [PRD](../../Docs/Product/02-PRD-Game-Saham-2D-Portrait.md) dan [aturan repo](../../AGENTS.md). Dependensi: [03](../03-pasar-npc-order-halt/TASK.md), [05](../05-trading-portofolio-fx/TASK.md), [07](../07-firma-klien-mandat-fee/TASK.md), [09](../09-multi-aset-fund-builder/TASK.md). Dependensi adalah urutan logis; setelah kontrak dasar jelas, kerjakan bagian independen tanpa menunggu semua pekerjaan lain selesai. Satu writer repo.

## Pekerjaan

Implementasikan exposure/drawdown/audit, stop/TP, debt/collateral/interest, stock borrow/recall/repayment, margin call, forced liquidation/buy-in dan short squeeze. Uji gap, illiquid, halt dan batas urutan pemicu.

## Hasil yang harus tersedia

Journey borrow-short-cover-repay, margin call/liquidation, laporan kewajiban dan check stres.

## Kriteria penerimaan

Utang/jaminan/biaya direkonsiliasi; stop tidak menjamin harga trigger; halt berlaku untuk seluruh caller. Radar/kartu risiko bukan mesin short/margin.

## Checkpoint dan bukti

Simpan bukti nyata pada Artifacts/11/ saat mulai menguji. Jangan membuat log hasil palsu. Perbarui [STATUS.json](../STATUS.json), [REQUIREMENTS.csv](../REQUIREMENTS.csv), [SCREEN-COVERAGE.csv](../SCREEN-COVERAGE.csv), [DECISIONS.md](../DECISIONS.md), dan [ACCEPTANCE.md](../ACCEPTANCE.md) setelah checkpoint aman. Cantumkan file berubah, command/hasil, lingkungan uji, blocker, dan next_action. Status lulus membutuhkan path bukti; keberadaan file atau output build saja tidak membuktikan gameplay.
