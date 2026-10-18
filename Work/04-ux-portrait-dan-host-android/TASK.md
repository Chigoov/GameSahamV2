# UX portrait dan host Android

ID: 04. Status awal: PENDING. Cakupan: FR-18, FR-20.

## Acuan dan dependensi

Baca [PRD](../../Docs/Product/02-PRD-Game-Saham-2D-Portrait.md) dan [aturan repo](../../AGENTS.md). Dependensi: [00](../00-preflight-dan-keputusan/TASK.md), [01](../01-domain-akun-waktu-save/TASK.md). Dependensi adalah urutan logis; setelah kontrak dasar jelas, kerjakan bagian independen tanpa menunggu semua pekerjaan lain selesai. Satu writer repo.

## Pekerjaan

Terapkan [design brief](../../Docs/Design/03-Design-Brief-Game-Saham-2D-Portrait.md) secara penuh: token navy/putih hangat/emas, IBM Plex Sans lokal, shell dan layout S01-S40, journey J01-J20, komponen C01-C28, state per screen dan aksesibilitas. Jangan membuat warna/font sendiri per screen. Form transaksi panjang adalah halaman penuh dan footer tidak menumpuk root navigation. Gambar/diagram spesifikasi tidak menggantikan screenshot implementasi.

Bangun token visual bersama dan shell lima menu 2D portrait, detail/tab, form, loading/empty/error/locked, safe area, keyboard, Back, fokus dan font scale. Jika hybrid dipilih, gunakan bridge terbatas ke domain. Buktikan packaging lokal dan APK smoke sedini mungkin. Mock desain terpisah dan berlabel.

## Hasil yang harus tersedia

Shell portrait, host/bridge, screenshot viewport ringkas/lebar/keyboard dan bukti APK smoke jika toolchain memungkinkan.

## Kriteria penerimaan

Tidak ada renderer/aset/navigasi 3D. Target sentuh setara 48 dp diuji dengan lingkungan disebut; UI menerima domain nyata; bridge hilang gagal jelas. Browser bukan bukti native.

## Checkpoint dan bukti

Simpan bukti nyata pada Artifacts/04/ saat mulai menguji. Jangan membuat log hasil palsu. Perbarui [STATUS.json](../STATUS.json), [REQUIREMENTS.csv](../REQUIREMENTS.csv), [SCREEN-COVERAGE.csv](../SCREEN-COVERAGE.csv), [DECISIONS.md](../DECISIONS.md), dan [ACCEPTANCE.md](../ACCEPTANCE.md) setelah checkpoint aman. Cantumkan file berubah, command/hasil, lingkungan uji, blocker, dan next_action. Status lulus membutuhkan path bukti; keberadaan file atau output build saja tidak membuktikan gameplay.
