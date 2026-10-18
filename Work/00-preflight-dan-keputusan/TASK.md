# Preflight dan keputusan

ID: 00. Status awal: PENDING. Cakupan: FR-01, FR-18, FR-19, FR-20.

## Acuan dan dependensi

Baca [PRD](../../Docs/Product/02-PRD-Game-Saham-2D-Portrait.md) dan [aturan repo](../../AGENTS.md). Dependensi: Tidak ada. Dependensi adalah urutan logis; setelah kontrak dasar jelas, kerjakan bagian independen tanpa menunggu semua pekerjaan lain selesai. Satu writer repo.

## Pekerjaan

Baca [design brief](../../Docs/Design/03-Design-Brief-Game-Saham-2D-Portrait.md) sebelum kode game. Pertahankan pemetaan 40 screen/26 cakupan lama, 20 journey dan 28 komponen. Keputusan token/UX telah ditetapkan; OQ ekonomi/data/perangkat tetap terbuka. Verifikasi kesiapan spesifikasi terlebih dahulu, tanpa mengklaim visual/runtime sudah lulus.

Buktikan path absolut dan Git root; inventaris perubahan lokal dan toolchain aktual. Baca seluruh PRD dan OQ. Verifikasi baseline C# + MAUI HybridWebView + HTML/CSS/JS lokal atau pilih alternatif minimum dengan alasan. Rencanakan APK smoke awal. Triage setiap OQ tanpa menganggap rekomendasi sebagai jawaban pemilik. Petakan seluruh requirement dan 26 layar.

## Hasil yang harus tersedia

Catatan toolchain dengan command nyata, keputusan arsitektur minimum, dependensi/lisensi, triage OQ, dan urutan pekerjaan independen.

## Kriteria penerimaan

Root tepat dan file awal terlindungi; 20 FR serta 26 layar terpetakan. Preflight boleh selesai setelah triage terverifikasi; OQ yang belum dijawab tetap memblokir acceptance fitur terkait.

## Checkpoint dan bukti

Simpan bukti nyata pada Artifacts/00/ saat mulai menguji. Jangan membuat log hasil palsu. Perbarui [STATUS.json](../STATUS.json), [REQUIREMENTS.csv](../REQUIREMENTS.csv), [SCREEN-COVERAGE.csv](../SCREEN-COVERAGE.csv), [DECISIONS.md](../DECISIONS.md), dan [ACCEPTANCE.md](../ACCEPTANCE.md) setelah checkpoint aman. Cantumkan file berubah, command/hasil, lingkungan uji, blocker, dan next_action. Status lulus membutuhkan path bukti; keberadaan file atau output build saja tidak membuktikan gameplay.
