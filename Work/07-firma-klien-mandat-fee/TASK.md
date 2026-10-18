# Firma klien mandat dan fee

ID: 07. Status awal: PENDING. Cakupan: FR-08.

## Acuan dan dependensi

Baca [PRD](../../Docs/Product/02-PRD-Game-Saham-2D-Portrait.md) dan [aturan repo](../../AGENTS.md). Dependensi: [05](../05-trading-portofolio-fx/TASK.md), [06](../06-karier-siklus-hari-lifecycle/TASK.md). Dependensi adalah urutan logis; setelah kontrak dasar jelas, kerjakan bagian independen tanpa menunggu semua pekerjaan lain selesai. Satu writer repo.

## Pekerjaan

Bangun discovery/accept/reject klien, kontrak, akun terpisah, AUM/benchmark/trust/laporan, deposit/withdrawal, management/performance fee dan HWM. Hard limit divalidasi domain sebelum debit. Penarikan memakai likuiditas pasar.

## Hasil yang harus tersedia

Journey satu akun pribadi dan dua klien, evaluasi mandat, arus modal, fee/HWM idempoten dan respons trust.

## Kriteria penerimaan

Dana tidak tercampur; setoran bukan profit; fee tidak terulang setelah reload; pelanggaran ditolak tanpa mutasi; kekurangan kas dijelaskan.

## Checkpoint dan bukti

Simpan bukti nyata pada Artifacts/07/ saat mulai menguji. Jangan membuat log hasil palsu. Perbarui [STATUS.json](../STATUS.json), [REQUIREMENTS.csv](../REQUIREMENTS.csv), [SCREEN-COVERAGE.csv](../SCREEN-COVERAGE.csv), [DECISIONS.md](../DECISIONS.md), dan [ACCEPTANCE.md](../ACCEPTANCE.md) setelah checkpoint aman. Cantumkan file berubah, command/hasil, lingkungan uji, blocker, dan next_action. Status lulus membutuhkan path bukti; keberadaan file atau output build saja tidak membuktikan gameplay.
