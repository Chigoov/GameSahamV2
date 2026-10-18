# Karier siklus hari dan lifecycle

ID: 06. Status awal: PENDING. Cakupan: FR-01, FR-07, FR-19.

## Acuan dan dependensi

Baca [PRD](../../Docs/Product/02-PRD-Game-Saham-2D-Portrait.md) dan [aturan repo](../../AGENTS.md). Dependensi: [01](../01-domain-akun-waktu-save/TASK.md), [04](../04-ux-portrait-dan-host-android/TASK.md), [05](../05-trading-portofolio-fx/TASK.md). Dependensi adalah urutan logis; setelah kontrak dasar jelas, kerjakan bagian independen tanpa menunggu semua pekerjaan lain selesai. Satu writer repo.

## Pekerjaan

Hubungkan briefing-trading-close-report-hari baru; tutorial, misi, reward/gaji, reputasi, promosi, penalti dan recovery sesuai keputusan ekonomi. Uji autosave, force-stop, background/resume serta migrasi. Fresh run produksi tidak berisi dana/mandat demo.

## Hasil yang harus tersedia

Journey beberapa hari, rugi/recovery, promosi/reward satu klaim dan bukti lifecycle/save.

## Kriteria penerimaan

State yang diakui tersimpan bertahan; reward/laporan tidak terulang; unlock dari syarat nyata. Parameter ekonomi belum final memblokir penerimaan final.

## Checkpoint dan bukti

Simpan bukti nyata pada Artifacts/06/ saat mulai menguji. Jangan membuat log hasil palsu. Perbarui [STATUS.json](../STATUS.json), [REQUIREMENTS.csv](../REQUIREMENTS.csv), [SCREEN-COVERAGE.csv](../SCREEN-COVERAGE.csv), [DECISIONS.md](../DECISIONS.md), dan [ACCEPTANCE.md](../ACCEPTANCE.md) setelah checkpoint aman. Cantumkan file berubah, command/hasil, lingkungan uji, blocker, dan next_action. Status lulus membutuhkan path bukti; keberadaan file atau output build saja tidak membuktikan gameplay.
