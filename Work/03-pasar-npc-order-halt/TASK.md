# Pasar NPC order dan halt

ID: 03. Status awal: PENDING. Cakupan: FR-03, FR-04, FR-16.

## Acuan dan dependensi

Baca [PRD](../../Docs/Product/02-PRD-Game-Saham-2D-Portrait.md) dan [aturan repo](../../AGENTS.md). Dependensi: [01](../01-domain-akun-waktu-save/TASK.md), [02](../02-data-katalog-indeks/TASK.md). Dependensi adalah urutan logis; setelah kontrak dasar jelas, kerjakan bagian independen tanpa menunggu semua pekerjaan lain selesai. Satu writer repo.

## Pekerjaan

Implementasikan pasar bersama, NPC terdanai, price/time priority, market/limit, partial fill, cancel/expiry, reservasi, biaya dan slippage. Event makro/emiten dan reaksi NPC dapat ditelusuri. Tambahkan halt/reopening dan batas rantai pemicu sesuai aturan terpilih. Telusuri seluruh caller aturan shared.

## Hasil yang harus tersedia

Mesin pasar/order, log sebab-akibat, preview/submit konsisten dan check deterministik untuk fill/cancel/retry/reservasi/halt.

## Kriteria penerimaan

Order tanpa fill tidak mengubah last trade; market impact tidak ganda; tidak ada dana/saham siluman; NPC/klien/pemain memakai validasi sama. Angka aturan belum final tetap gate terbuka.

## Checkpoint dan bukti

Simpan bukti nyata pada Artifacts/03/ saat mulai menguji. Jangan membuat log hasil palsu. Perbarui [STATUS.json](../STATUS.json), [REQUIREMENTS.csv](../REQUIREMENTS.csv), [SCREEN-COVERAGE.csv](../SCREEN-COVERAGE.csv), [DECISIONS.md](../DECISIONS.md), dan [ACCEPTANCE.md](../ACCEPTANCE.md) setelah checkpoint aman. Cantumkan file berubah, command/hasil, lingkungan uji, blocker, dan next_action. Status lulus membutuhkan path bukti; keberadaan file atau output build saja tidak membuktikan gameplay.
