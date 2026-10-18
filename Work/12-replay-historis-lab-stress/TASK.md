# Replay historis lab dan stress test

ID: 12. Status awal: PENDING. Cakupan: FR-01, FR-17.

## Acuan dan dependensi

Baca [PRD](../../Docs/Product/02-PRD-Game-Saham-2D-Portrait.md) dan [aturan repo](../../AGENTS.md). Dependensi: [02](../02-data-katalog-indeks/TASK.md), [03](../03-pasar-npc-order-halt/TASK.md), [05](../05-trading-portofolio-fx/TASK.md), [08](../08-berita-riset-kalender-terminal/TASK.md), [11](../11-risiko-margin-short-compliance/TASK.md). Dependensi adalah urutan logis; setelah kontrak dasar jelas, kerjakan bagian independen tanpa menunggu semua pekerjaan lain selesai. Satu writer repo.

## Pekerjaan

Implementasikan replay dataset historis nyata sesuai OQ-03, manifest dan model fill yang jujur. Lab/what-if/stress/replay keputusan pada clone terisolasi dengan perbandingan hasil/risiko, seed dan versi. Tidak memberi reward atau perubahan dana ke run utama.

## Hasil yang harus tersedia

Journey replay periode nyata, gap/split/delisting/resolution, stress dan perbandingan state utama sebelum/sesudah eksperimen.

## Kriteria penerimaan

Tanpa future leak/antrean asli palsu; lab tidak memutasi ekonomi utama. Fixture sintetis tidak meluluskan histori. Jika kontrak/data belum ada, gate terkait BLOCKED/PENDING dan pekerjaan independen dilanjutkan.

## Checkpoint dan bukti

Simpan bukti nyata pada Artifacts/12/ saat mulai menguji. Jangan membuat log hasil palsu. Perbarui [STATUS.json](../STATUS.json), [REQUIREMENTS.csv](../REQUIREMENTS.csv), [SCREEN-COVERAGE.csv](../SCREEN-COVERAGE.csv), [DECISIONS.md](../DECISIONS.md), dan [ACCEPTANCE.md](../ACCEPTANCE.md) setelah checkpoint aman. Cantumkan file berubah, command/hasil, lingkungan uji, blocker, dan next_action. Status lulus membutuhkan path bukti; keberadaan file atau output build saja tidak membuktikan gameplay.
