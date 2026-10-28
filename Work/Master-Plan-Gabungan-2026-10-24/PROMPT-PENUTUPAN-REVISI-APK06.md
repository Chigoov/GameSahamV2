# Prompt penuntasan enam catatan review APK-06

Tanggal:27 Oktober2026, Asia/Jakarta.

## Mandat pemilik

Kerjakan revisi lanjutan berdasarkan review APK-06. Root produksi yang benar adalah:

`C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`

Saya mengizinkan perubahan source, pengujian dan dokumentasi yang diperlukan untuk menuntaskan **K-018, K-009, K-015, K-002, K-013 dan K-005**, beserta regresi langsung perbaikannya. Kerjakan seluruh paket ini sampai selesai atau ada hambatan nyata yang membutuhkan keputusan saya. Tidak perlu meminta izin ulang untuk berpindah ID dalam paket ini. Pekerjaan baru di luar paket tetap mengikuti batas AGENTS.md.

Baca sampai selesai:

1. AGENTS.md dan DESIGN.md beserta kontrak produk/standar penerimaan yang dirujuk.
2. `Artifacts/Review-Hermes-2026-10-27/REVIEW-REPORT-HERMES.md`.
3. `Work/Master-Plan-Gabungan-2026-10-24/DAFTAR-KEKURANGAN-LINTAS-PLAN.md`, terutama bagian review independen APK-06 paling akhir. Label23/23 milik penyerahan sebelumnya telah dikoreksi oleh review ini.
4. Bukti terkait di `Artifacts/Review-Hermes-2026-10-27/` dan penyerahanmu `Artifacts/Revisi-Pasca-Review-2026-10-27/`.

Baseline yang sudah terbukti: log penuh628/628 PASS pada384MB;253 class hasil kompilasi fresh reviewer cocok dengan class full-run; uji target independen11/11 PASS. Backup, persetujuan harga domain, sync berulang, metadata, tracker dan integritas APK mempunyai bukti positif. Pertahankan perbaikan itu. Hasil tersebut berlaku pada snapshot review, bukan otomatis pada source setelah diubah lagi.

## 1. K-018 — pulihkan transaksi yang responsnya terputus

Reproduksi bukti `browser-review.json` dan `lost-response-retry.png`: pembelian sudah committed di domain, tetapi respons diputus sebelum diterima UI. UI menyebut Pembelian Gagal. Klik ulang membuat requestId baru dan menghasilkan Barang koleksi sudah dimiliki, bukan receipt sukses awal. Tidak ada bukti debit ganda pada kasus ini; masalahnya pemulihan outcome.

Pertahankan requestId dan payload untuk satu intent yang statusnya belum pasti. Bedakan penolakan definitif, hasil belum diketahui, dan transaksi selesai. Retry/pemeriksaan hasil harus memulihkan receipt intent yang sama. Jangan membentuk ID baru pada setiap klik ulang. Jika quote berubah dan pengguna menyetujui ulang harga, perlakukan sebagai intent baru hanya setelah intent sebelumnya diselesaikan atau dipastikan tidak committed.

Telusuri seluruh caller koleksi, termasuk galeri/detail BUY/SELL. Pakai ulang penyimpanan receipt dan pola pemulihan existing. Pertahankan expectedPrice atomik dan angka hasil dari receipt. Uji: respons hilang setelah commit, gagal sebelum commit, callback duplikat, retry/reload yang disyaratkan, dan harga berubah. Buktikan kas/kepemilikan/ledger tidak berubah dua kali serta UI akhirnya menunjukkan hasil yang benar.

## 2. K-009 — sediakan pemulihan pesan penting yang bisa dipakai pemain

Tiga error kini terlindungi dari info, tetapi error keempat masih menggusur error pertama. Riwayat50 item melalui window.getToastHistory() belum mempunyai jalur UI bagi pemain dan seluruh toast tetap auto-dismiss.

Gunakan mekanisme minimum yang menjaga akses pesan penting: antrean/dismiss yang tepat atau akses riwayat pada UI existing. Fungsi console/internal bukan bukti pemulihan pengguna. Jangan membangun pusat notifikasi besar tanpa kebutuhan. Uji beberapa error berurutan, campuran info/error, auto-dismiss, keyboard/fokus, callback duplikat dan transisi status. Pertahankan identitas operasi sync unik yang telah memperbaiki K-023.

## 3. K-015 — tegakkan ambang gate sesuai kontrak

Jalankan ulang `gate-borderline-probe.cjs` sebagai diagnosis pada save QA baru. Helper saat ini meloloskan teks normal #777777 di atas putih (4.478089:1) serta tombol47.59375x48px, karena memakai threshold-0.05 dan47.5px.

Terapkan ambang48x48px, kontras teks normal4.5:1 dan teks besar3:1. Toleransi floating point harus sekecil yang diperlukan, tidak menurunkan standar0.05 rasio atau0.5px. Tambahkan tes tepat di bawah, sama dengan, dan di atas ambang. Tes positif dan negatif harus memanggil fungsi gate utama yang sama; pelanggaran yang dibiarkan pada halaman harus menghasilkan kegagalan gate/exit nonzero. Jangan menghapus elemen pelanggar sebelum pemeriksaan yang hendak dibuktikan.

## 4. K-002 — pastikan berita dan tujuan wajib benar-benar diuji

Perbaiki selector runner: ia mencari Berita & Sentimen Pasar, sedangkan UI berlabel Berita Pasar & Sentimen. Tujuan wajib yang tidak ditemukan harus menggagalkan pengujian, bukan dilewati oleh if(count>0) lalu dilaporkan PASS.

Petakan state berita yang diwajibkan, termasuk state publikasi/aktif/berakhir, detail, empty/loading/error/stale/locked yang berlaku. Jelaskan N/A berdasarkan kontrak, bukan karena selector tidak menemukan elemen. Buktikan rute, fokus, keyboard/Back dan pembaruan in-place. Periksa screenshot aktual dan pastikan laporan memuat setiap state yang benar-benar diuji.

## 5. K-013 — lengkapi acceptance order, diagnosis Back dan ukur aksi yang benar

Matriks100 sebelumnya hanya overflow lima tab; modal diperiksa pada360x800. Bagian issuer_and_ticket hanya membuka detail emiten. Lengkapi modal/state berita serta tiket/receipt order yang diwajibkan pada10 viewport dan skala100%/200%, termasuk fokus/keyboard/Back/scroll/reduced motion. Jangan menganggap detail emiten sebagai pengujian seluruh tiket order.

Diagnosis reproduksi `modal-back-probe.json`, `modal-back-timeout.png` dan `strict-gate-rerun.log`: setelah buka/tutup detail berulang, klik Back menyisakan modal BBCA. Runner penuh reviewer timeout30detik; probe terpisah mereproduksi pada iterasi ke21. Ini hasil host/browser, akar masalah belum ditetapkan. Telusuri fungsi Back bersama dan interaksi polling/clock/lifecycle dengan bukti. Jangan meluluskan dengan menghapus wait/assert, menambah sleep tetap, memperpanjang timeout saja, atau memaksa reload halaman. Jika ternyata masalah runner/adapter, buktikan penyebabnya dan perbaiki pengujian tanpa menyembunyikan kegagalan produk.

Benchmark heavy_feedback harus menekan aksi yang benar-benar memulai sinkronisasi/pekerjaan async dan mengukur feedback pertamanya. Membuka dan menutup modal jam tidak membuktikan feedback proses sync. Pisahkan waktu feedback dari waktu pekerjaan selesai.

Simpan minimal30 sampel mentah per jenis aksi/rute yang dilaporkan. Jika menyimpulkan per-tab, kumpulkan30 per tab. P95 aksi lokal<=300ms, navigasi<=1000ms, feedback berat<=300ms dengan UI responsif. Pertahankan cakupan dan ambang standar proyek. Klaim harus sesuai aksi, state dan perangkat/host yang diukur.

## 6. K-005 — rekonsiliasi status setelah hasil akhir tersedia

STATUS.json masih aktif dengan next_action menguji/build; STATUS-REPORT masih menunjuk APK-04/05 dan21 catatan; tabel penutupan menyebut626 walaupun bukti terbaru628. Folder penyerahan belum mempunyai RESULTS.md final yang menyatukan snapshot, tes, APK dan batas pengujian.

Perbarui daftar revisi existing, Work/STATUS.json, STATUS-REPORT.md, CHECKPOINT.md dan laporan akhir. Bedakan histori dari status aktif. Jangan menghapus hasil gagal atau mengklaim23/23 selama enam syarat ini belum ditutup. Ambil jumlah tes, exit code dan durasi dari run yang sebenarnya. Generator laporan tidak boleh mengisi PASS/angka fallback jika hasil akhir tidak ditemukan.

## Batas pelaksanaan dan penyerahan

- Buktikan root Git/snapshot sebelum edit. Pertahankan dirty checkout dan perubahan orang lain. Telusuri caller fungsi bersama sebelum memilih perubahan terkecil. Pakai helper, stdlib dan dependency existing; tanpa framework atau abstraksi spekulatif. Logika nontrivial meninggalkan runnable regression check.
- Domain Java tetap pemilik ekonomi/waktu/save. Pertahankan24x, deadline berita, kompatibilitas save, rumus ekonomi dan desain yang berlaku. Tidak ada tuning ekonomi dari demo tiga bulan.
- Save pemain `Artifacts/LocalPreview/preview-save/save_game_v2.json` hanya boleh di-hash sebelum/sesudah. Jangan membaca isinya, memuatnya ke QA, menimpa atau meresetnya. Hash review terakhir `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`; bila berbeda saat mulai, catat keadaan aktual tanpa mengubah data.
- Semua test memakai save dan proses QA terpisah. Periksa listener sebelum memilih port; jangan mengganggu preview18917/demo18918 atau save-nya. Hentikan hanya proses QA milikmu.
- Jangan reset/clean/stash, mengubah pengaturan global, meluncurkan agen lain, mengirim pesan lintas chat, push/publish, menjalankan ADB atau menginstal APK. Native tetap NOT_TESTED_BY_SCOPE sampai ada mandat terpisah.
- Jalankan tes target selama perbaikan. Setelah source stabil, jalankan regresi akhir sesuai standar, termasuk suite penuh fresh pada384MB dengan skenario730 hari tetap utuh. Jangan mengurangi durasi, menghapus histori, menaikkan heap saja atau menelan error untuk meluluskan.
- Bangun kandidat APK baru setelah verifikasi source akhir, dengan nama baru tanpa menimpa APK-06. Catat manifest source, command/exit build, jumlah tes aktual, raw sample, identitas aplikasi, signature, ukuran, SHA256 dan kecocokan seluruh aset.
- Simpan bukti baru pada folder terpisah dari audit dan penyerahan historis. Perbarui status per ID hanya sesudah syarat penutupan terpenuhi. Jika ada hambatan, lanjutkan pekerjaan independen dan laporkan batasnya secara spesifik.

Penyerahan akhir harus memuat hasil enam ID satu per satu, perubahan konkret, tes dan bukti, status seluruh23 ID yang konsisten, kandidat APK baru, integritas save pemain, dan pemisahan bukti host/browser/Gradle/APK/native. Mulai dari reproduksi K-018 dan lanjutkan seluruh paket ini sampai tuntas.
