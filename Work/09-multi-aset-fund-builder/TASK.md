# Multi aset dan fund builder

ID: 09. Status awal: PENDING. Cakupan: FR-10, FR-11.

## Acuan dan dependensi

Baca [PRD](../../Docs/Product/02-PRD-Game-Saham-2D-Portrait.md) dan [aturan repo](../../AGENTS.md). Dependensi: [02](../02-data-katalog-indeks/TASK.md), [05](../05-trading-portofolio-fx/TASK.md), [07](../07-firma-klien-mandat-fee/TASK.md). Dependensi adalah urutan logis; setelah kontrak dasar jelas, kerjakan bagian independen tanpa menunggu semua pekerjaan lain selesai. Satu writer repo.

## Pekerjaan

Tentukan obligasi/komoditas/FX sesuai keputusan produk, lalu unit, valuation, coupon/maturity, settlement dan biaya. Fund plan menyimpan target/kas, memeriksa mandat dan melaksanakan lewat order shared. Menyimpan rencana tidak mengubah posisi.

## Hasil yang harus tersedia

Transaksi per kelas aset, valuasi lintas mata uang, alokasi dan status eksekusi parsial, check rekonsiliasi.

## Kriteria penerimaan

Setiap kelas aset berfungsi; tabel ekuitas bukan multi-aset. Target bobot belum tercapai sebelum fill. OQ definisi instrumen tetap gate.

## Checkpoint dan bukti

Simpan bukti nyata pada Artifacts/09/ saat mulai menguji. Jangan membuat log hasil palsu. Perbarui [STATUS.json](../STATUS.json), [REQUIREMENTS.csv](../REQUIREMENTS.csv), [SCREEN-COVERAGE.csv](../SCREEN-COVERAGE.csv), [DECISIONS.md](../DECISIONS.md), dan [ACCEPTANCE.md](../ACCEPTANCE.md) setelah checkpoint aman. Cantumkan file berubah, command/hasil, lingkungan uji, blocker, dan next_action. Status lulus membutuhkan path bukti; keberadaan file atau output build saja tidak membuktikan gameplay.
