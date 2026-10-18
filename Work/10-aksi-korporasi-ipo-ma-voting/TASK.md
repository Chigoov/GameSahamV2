# Aksi korporasi IPO M&A dan voting

ID: 10. Status awal: PENDING. Cakupan: FR-12, FR-13, FR-14.

## Acuan dan dependensi

Baca [PRD](../../Docs/Product/02-PRD-Game-Saham-2D-Portrait.md) dan [aturan repo](../../AGENTS.md). Dependensi: [02](../02-data-katalog-indeks/TASK.md), [05](../05-trading-portofolio-fx/TASK.md), [07](../07-firma-klien-mandat-fee/TASK.md). Dependensi adalah urutan logis; setelah kontrak dasar jelas, kerjakan bagian independen tanpa menunggu semua pekerjaan lain selesai. Satu writer repo.

## Pekerjaan

Implementasikan dividend/split/reverse/rights/buyback/merger, eligibility, fraksi, penyesuaian order/indeks; IPO allotment/refund/tender. Voting memakai snapshot, tenggat, hak dan ballot unik. Bedakan skenario game dan historis.

## Hasil yang harus tersedia

Journey tiap keluarga aksi, ledger kas/unit, refund parsial/gagal, voting idempoten dan save/load saat kewajiban terbuka.

## Kriteria penerimaan

Tidak ada pembayaran/hak/suara ganda; basis biaya dan fraksi tepat; raw/adjusted tidak dihitung dua kali; eligibility dan deadline ditegakkan.

## Checkpoint dan bukti

Simpan bukti nyata pada Artifacts/10/ saat mulai menguji. Jangan membuat log hasil palsu. Perbarui [STATUS.json](../STATUS.json), [REQUIREMENTS.csv](../REQUIREMENTS.csv), [SCREEN-COVERAGE.csv](../SCREEN-COVERAGE.csv), [DECISIONS.md](../DECISIONS.md), dan [ACCEPTANCE.md](../ACCEPTANCE.md) setelah checkpoint aman. Cantumkan file berubah, command/hasil, lingkungan uji, blocker, dan next_action. Status lulus membutuhkan path bukti; keberadaan file atau output build saja tidak membuktikan gameplay.
