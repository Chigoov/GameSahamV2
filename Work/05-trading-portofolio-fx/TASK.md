# Trading portofolio dan FX

ID: 05. Status awal: PENDING. Cakupan: FR-04, FR-05, FR-06.

## Acuan dan dependensi

Baca [PRD](../../Docs/Product/02-PRD-Game-Saham-2D-Portrait.md) dan [aturan repo](../../AGENTS.md). Dependensi: [02](../02-data-katalog-indeks/TASK.md), [03](../03-pasar-npc-order-halt/TASK.md), [04](../04-ux-portrait-dan-host-android/TASK.md). Dependensi adalah urutan logis; setelah kontrak dasar jelas, kerjakan bagian independen tanpa menunggu semua pekerjaan lain selesai. Satu writer repo.

## Pekerjaan

Hubungkan detail/chart/book/tiket/order-result dengan domain. Akun aktif, biaya, preview, partial/cancel, riwayat, valuation dan available/reserved jelas. FX IDR/USD memakai kuotasi berumur dan rounding eksplisit. Grafik berasal histori domain/dataset, bukan seri dekoratif.

## Hasil yang harus tersedia

Journey beli-partial-cancel-jual, kas/ledger, FX preview/confirm/expiry dan screenshot UI dengan data domain.

## Kriteria penerimaan

Unit/mata uang/VWAP/fee direkonsiliasi; retry satu transaksi; perubahan akun memaksa preview ulang; FX tidak menciptakan arbitrase rounding.

## Checkpoint dan bukti

Simpan bukti nyata pada Artifacts/05/ saat mulai menguji. Jangan membuat log hasil palsu. Perbarui [STATUS.json](../STATUS.json), [REQUIREMENTS.csv](../REQUIREMENTS.csv), [SCREEN-COVERAGE.csv](../SCREEN-COVERAGE.csv), [DECISIONS.md](../DECISIONS.md), dan [ACCEPTANCE.md](../ACCEPTANCE.md) setelah checkpoint aman. Cantumkan file berubah, command/hasil, lingkungan uji, blocker, dan next_action. Status lulus membutuhkan path bukti; keberadaan file atau output build saja tidak membuktikan gameplay.
