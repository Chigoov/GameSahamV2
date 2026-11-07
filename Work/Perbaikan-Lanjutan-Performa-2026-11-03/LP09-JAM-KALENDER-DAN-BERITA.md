# LP09: jam, kalender WIB dan informasi emiten hari berjalan

**DIKOREKSI OLEH KLARIFIKASI PEMILIK:** informasi yang dimaksud adalah tabel harga harian di bawah chart, bukan berita. Pembatasan berita dibatalkan; hasil dan APK pada dokumen ini merupakan riwayat snapshot sebelumnya. Rujukan aktual: [LP09-TABEL-STATISTIK-DAN-TANGGAL.md](LP09-TABEL-STATISTIK-DAN-TANGGAL.md). Jangan memakai paket berita ini sebagai hasil akhir permintaan yang sudah diklarifikasi.

3 November 2026. Permintaan pemilik: memperbaiki glitch waktu/kalender dan menampilkan informasi terakhir tiap emiten pada hari tersebut. Informasi diartikan sebagai berita emiten pada bagian existing **Berita terkait**; klarifikasi kepada pemilik masih dapat memperinci maksud ini. Tidak mengubah desain, label, navigasi, aturan ekonomi, skema save atau data pemain. Pelaksanaan tetap host dahulu, tanpa ADB atau tindakan ponsel baru.

## Temuan dan perbaikan yang dibuktikan

| Masalah | Bukti sebelum | Perilaku sesudah |
|---|---|---|
| Jam mundur pada awal/akhir sinkronisasi | Waktu sudah `00:01 WIB`, tetapi kembali `23:59 WIB` ketika snapshot lama diselaraskan. | Awal/progres/hasil memakai waktu presentasi yang sama; hasil terlambat pada run sama tidak memundurkan jam. Hasil tes `00:01` sebelum, selama dan sesudah. |
| Tanggal tertinggal dan format berganti | Setelah melintasi tengah malam, masih `Hari 12 · 2026-11-03`, bukan `Hari 13 · 04 November 2026`. | Tanggal hanya ditulis oleh presenter jam, memakai format WIB yang sama pada kondisi normal dan sinkronisasi. Teks yang tidak berubah tidak mengganti node DOM. |
| Agenda memakai tanggal UTC sementara label memakai WIB | Pada 4 November 01:00 WIB, pilihan bertuliskan hari 4 tetapi agenda kemarin/tanggal 3 dimuat. | Satu fungsi kunci tanggal WIB dipakai untuk filter; agenda tanggal 4 muncul, tanggal 3 tidak. Ketika hari beralih ke tanggal 5, kalender yang tetap terbuka diperbarui. |
| Berita emiten tidak mengikuti hari/urutan terbaru dan tidak diperbarui saat tetap terbuka | Daftar berupa `QA kemarin`, `QA pagi`, `QA terbaru`, `QA belum terbit`. | Hanya berita yang sudah terbit pada hari game berjalan dan berkaitan dengan ticker/sektor existing; urutan terbaru di atas. Berita baru muncul tanpa membuka ulang rincian, dan berita kemarin hilang pada pergantian hari WIB. |

Komponen berita existing dipakai ulang dalam `updateIssuerNews` untuk pemuatan awal dan pembaruan rincian. Data yang sama mempertahankan node; saat ada berita baru, fokus tombol artikel yang masih tersedia dipulihkan. Pembaruan Beranda/rincian yang bersamaan berbagi satu pembacaan feed. Jika tidak ada berita terkait yang sudah terbit pada hari itu, empty state existing tetap tampil; tidak mengarang berita untuk semua emiten.

## Bukti dan batas tes

- Baseline sah: `logs/verified-clock-issuer-baseline-final/results.json`, empat pemeriksaan LP09 gagal terhadap salinan app.js sebelum perbaikan. Percobaan kalender terdahulu sempat memakai snapshot Java aktual, bukan tanggal fixture; kegagalan setup itu dipertahankan sebagai riwayat dan tidak dipakai sebagai bukti bug tanggal. Baseline final memasok snapshot tanggal yang sama melalui jalur refresh sebenarnya.
- Gate terbaru `logs/gate-1793709030419947100/gate.json`: `quality_gate.py run` exit 0; **93 tes Java / 19 suite terpilih dan 27 kelompok browser lulus**. Empat kelompok LP09 ditambahkan ke daftar wajib; 23 kelompok lama tidak dihapus atau dilonggarkan. Tes QA memakai emiten/data sintetis berlabel serta aset produksi, bukan save pemain.
- `check` regression exit **0**; `check --claim full` exit **2**, sesuai bukti gabungan `logs/lp09-host-verification.json`. Tidak menyatakan seluruh plan atau kelancaran Android selesai.
- `logs/verified-lp09-sequence/sequences.json`: 10 × 100 aksi UI terkontrol lulus. `logs/verified-lp09-layout/layouts.json`: 70 kasus layout lulus pada cakupan lima layar existing; ini bukan cakupan semua layar atau ukuran teks Android fisik.
- Dua build offline berhasil. Setiap APK diperiksa: 405 aset web cocok, 13 DEX diperiksa, tidak ditemukan kelas server QA/source test. Seluruh 427 anggota assets/DEX QA identik dengan produksi, tanda tangan dan application ID sesuai. Rincian perintah/hash pada `logs/lp09-host-verification.json`.
- Save pemain hanya di-hash dan tetap `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`.

## Artefak terbaru dan pekerjaan tersisa

Folder `Artifacts/Codex-LP-Verification/lp09-calendar-news-2026-11-03/`:

- `GameSaham-debug.apk`: SHA-256 `1EAB445A5DC5F0EAD5987E39E9072FC48B817A1FA296512309A3D1CDD6E019D7`.
- `GameSaham-QA-debug.apk`: SHA-256 `C08942B170596AF9F1D2D223F079C4404B65E2D9331EB7666EF2AACEE89D2CEA`.

File produksi pada tahap ini: `android/app/src/main/assets/www/app.js`. Tambahan pemeriksaan pada `verify_ui.py` dan `quality_gate.py`; plan/laporan diperbarui. Perubahan lokal lain dipertahankan. Tidak commit, push, publish atau memperbarui game utama.

**LP09 diperbaiki dan diuji host; perangkat NEEDS_VALIDATION.** APK baru belum dipasang. Perbaikan belum akan terlihat pada APK lama di ponsel. Saat Poco tersambung, lanjutkan QA yang sudah diizinkan dengan artefak ini: verifikasi identitas, kalender/pergantian hari dan berita, respons/frame, lifecycle/progres, lalu sesi 60 menit pada snapshot stabil. Bukan klaim bahwa semua bug yang mungkin terlihat oleh pemilik telah tertutup.
