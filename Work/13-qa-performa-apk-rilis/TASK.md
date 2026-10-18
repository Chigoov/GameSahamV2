# QA performa dan APK rilis

ID: 13. Status awal: PENDING. Cakupan: FR-01, FR-02, FR-03, FR-04, FR-05, FR-06, FR-07, FR-08, FR-09, FR-10, FR-11, FR-12, FR-13, FR-14, FR-15, FR-16, FR-17, FR-18, FR-19, FR-20.

## Acuan dan dependensi

Baca [PRD](../../Docs/Product/02-PRD-Game-Saham-2D-Portrait.md) dan [aturan repo](../../AGENTS.md). Dependensi: [00](../00-preflight-dan-keputusan/TASK.md), [01](../01-domain-akun-waktu-save/TASK.md), [02](../02-data-katalog-indeks/TASK.md), [03](../03-pasar-npc-order-halt/TASK.md), [04](../04-ux-portrait-dan-host-android/TASK.md), [05](../05-trading-portofolio-fx/TASK.md), [06](../06-karier-siklus-hari-lifecycle/TASK.md), [07](../07-firma-klien-mandat-fee/TASK.md), [08](../08-berita-riset-kalender-terminal/TASK.md), [09](../09-multi-aset-fund-builder/TASK.md), [10](../10-aksi-korporasi-ipo-ma-voting/TASK.md), [11](../11-risiko-margin-short-compliance/TASK.md), [12](../12-replay-historis-lab-stress/TASK.md). Dependensi adalah urutan logis; setelah kontrak dasar jelas, kerjakan bagian independen tanpa menunggu semua pekerjaan lain selesai. Satu writer repo.

## Pekerjaan

Audit tiap butir PRD terhadap bukti, bukan folder/checklist. Jalankan regresi domain/bridge/UI/native, portrait/accessibility, save/recovery/upgrade. Build APK standalone, periksa paket, hash/versi, install/journey sesuai target. Ukur APK/install/data, startup, responsiveness, memory dan lifecycle dengan perangkat/metode jelas.

## Hasil yang harus tersedia

APK/checksum, matriks penerimaan, bukti build/install/device terpisah, manual, known issues, recovery dan hasil performa.

## Kriteria penerimaan

Semua FR/26 layar/gate akhir berbukti; tanpa blocker kritis/mayor uang/order/save; OQ material selesai. APK build bukan installed/device-tested. Jika perangkat/data nyata belum ada, status PARTIAL/BLOCKED, jangan COMPLETE.

## Checkpoint dan bukti

Simpan bukti nyata pada Artifacts/13/ saat mulai menguji. Jangan membuat log hasil palsu. Perbarui [STATUS.json](../STATUS.json), [REQUIREMENTS.csv](../REQUIREMENTS.csv), [SCREEN-COVERAGE.csv](../SCREEN-COVERAGE.csv), [DECISIONS.md](../DECISIONS.md), dan [ACCEPTANCE.md](../ACCEPTANCE.md) setelah checkpoint aman. Cantumkan file berubah, command/hasil, lingkungan uji, blocker, dan next_action. Status lulus membutuhkan path bukti; keberadaan file atau output build saja tidak membuktikan gameplay.
