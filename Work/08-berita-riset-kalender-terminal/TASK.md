# Berita riset kalender dan terminal

ID: 08. Status awal: PENDING. Cakupan: FR-09.

## Acuan dan dependensi

Baca [PRD](../../Docs/Product/02-PRD-Game-Saham-2D-Portrait.md) dan [aturan repo](../../AGENTS.md). Dependensi: [02](../02-data-katalog-indeks/TASK.md), [03](../03-pasar-npc-order-halt/TASK.md), [04](../04-ux-portrait-dan-host-android/TASK.md), [06](../06-karier-siklus-hari-lifecycle/TASK.md). Dependensi adalah urutan logis; setelah kontrak dasar jelas, kerjakan bagian independen tanpa menunggu semua pekerjaan lain selesai. Satu writer repo.

## Pekerjaan

Bangun berita makro/emiten, briefing berbasis kontribusi nyata, analis/konsensus/rekam akurasi, data alternatif, kalender dan paket terminal USD game. Entitlement di layanan; timestamp publikasi dan billing konsisten. Tanpa dependensi LLM berbayar wajib.

## Hasil yang harus tersedia

Journey berita/agenda/riset, subscribe/cancel/expire/bill, check anti future leak dan double billing.

## Kriteria penerimaan

Informasi belum rilis tidak bocor; sebab tanpa bukti diakui tidak diketahui; manfaat paket benar dan biaya dibukukan sekali.

## Checkpoint dan bukti

Simpan bukti nyata pada Artifacts/08/ saat mulai menguji. Jangan membuat log hasil palsu. Perbarui [STATUS.json](../STATUS.json), [REQUIREMENTS.csv](../REQUIREMENTS.csv), [SCREEN-COVERAGE.csv](../SCREEN-COVERAGE.csv), [DECISIONS.md](../DECISIONS.md), dan [ACCEPTANCE.md](../ACCEPTANCE.md) setelah checkpoint aman. Cantumkan file berubah, command/hasil, lingkungan uji, blocker, dan next_action. Status lulus membutuhkan path bukti; keberadaan file atau output build saja tidak membuktikan gameplay.
