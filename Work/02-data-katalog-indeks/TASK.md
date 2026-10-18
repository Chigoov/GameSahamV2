# Data katalog indeks dan kontrak historis

ID: 02. Status awal: PENDING. Cakupan: FR-02, FR-01.

## Acuan dan dependensi

Baca [PRD](../../Docs/Product/02-PRD-Game-Saham-2D-Portrait.md) dan [aturan repo](../../AGENTS.md). Dependensi: [00](../00-preflight-dan-keputusan/TASK.md), [01](../01-domain-akun-waktu-save/TASK.md). Dependensi adalah urutan logis; setelah kontrak dasar jelas, kerjakan bagian independen tanpa menunggu semua pekerjaan lain selesai. Satu writer repo.

## Pekerjaan

Bangun manifest data dengan sumber, hak penggunaan, versi, checksum, timezone, resolution, gap serta basis raw/adjusted. Katalog memakai identitas lintas bursa, pencarian, watchlist, status aktif/pasif dan indeks berkeanggotaan berperiode. Siapkan import histori sesuai kontrak yang disepakati; fixture sintetis hanya untuk tests berlabel.

## Hasil yang harus tersedia

Katalog/importer, manifest sumber, perhitungan indeks, check duplicate ticker dan kualitas dataset.

## Kriteria penerimaan

Ticker tidak tertukar; instrumen pasif tidak ditransaksikan; indeks konsisten dengan metode. Jangan mengaku dataset/962 emiten tersedia dari dokumen lama saja. Gate historis membutuhkan dataset nyata.

## Checkpoint dan bukti

Simpan bukti nyata pada Artifacts/02/ saat mulai menguji. Jangan membuat log hasil palsu. Perbarui [STATUS.json](../STATUS.json), [REQUIREMENTS.csv](../REQUIREMENTS.csv), [SCREEN-COVERAGE.csv](../SCREEN-COVERAGE.csv), [DECISIONS.md](../DECISIONS.md), dan [ACCEPTANCE.md](../ACCEPTANCE.md) setelah checkpoint aman. Cantumkan file berubah, command/hasil, lingkungan uji, blocker, dan next_action. Status lulus membutuhkan path bukti; keberadaan file atau output build saja tidak membuktikan gameplay.
