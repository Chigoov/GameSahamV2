# Preview gabungan untuk verifikasi pemilik

Mandat terbaru: analisis seluruh anotasi dan tampilkan bentuk perubahan terlebih dahulu; pemilik memverifikasi sebelum penerapan source produksi. Ajukan pertanyaan terkait hasil akhir.

## Keputusan lanjutan yang sudah dijawab

- Leverage 3x = total daya beli tiga kali modal sendiri; sisanya pinjaman broker game.
- Bentuk daftar referensi berlaku pada IDX, US, pantauan, dan anggota indeks.
- Saham terkunci tetap terlihat dengan kunci/syarat level; transaksi dinonaktifkan.
- Badge menunjukkan maksimum leverage per saham, bukan leverage yang sedang dipakai.
- Biaya pinjaman per hari game atas dana pinjaman yang benar-benar digunakan.
- Margin call → kesempatan tambah jaminan → likuidasi bila tetap kurang. Tarif, ambang, tenggat dan pemetaan leverage per saham belum dipilih; jangan mengarang angka.
- Jumlah saham Riset: pemilik meminta contoh preview. Contoh saat ini “33 peluang dari 42 saham terbuka”, dengan penjelasan ringkas yang bisa dibuka. Belum merupakan persetujuan teks final.
- Firma: AUM → daftar klien → kinerja dan fee.
- Kewajiban harga penutupan berubah berlaku pada saham yang aktif; saham yang dihentikan sementara dikecualikan.

## Artefak

Artifacts/Preview-Anotasi-2026-11-04/verifikasi.html menyediakan satu pemilih review, di luar UI game. Memuat Beranda, Pasar IDX, Pasar US, Pantauan, Chart, IPO, Riset, Firma, Indeks, dan rancangan leverage. Memakai halaman prototype existing agar tidak menggandakan implementasi layar. Embed menyembunyikan alat review lama, narasi layout, label data contoh berulang dan pemilih variasi. Prototype lama tetap dapat dibuka secara terpisah.

Tambahan ringkasan-review.html hanya memuat perubahan Beranda, ringkasan Firma dan contoh hitungan leverage. Ini bukan replika seluruh fitur Beranda atau bukti domain. Halaman data tidak menyentuh save. State contoh hilang bila layar dimuat ulang.

Daftar logo IDX berasal dari gambar pemilik untuk review. US memakai inisial sebagai fallback, belum aset logo produksi. Keanggotaan indeks/level/batas leverage/jadwal/harga/volume adalah fixture berlabel, bukan data terkini atau keputusan ekonomi baru. Seluruh chart masih contoh sintetis. Referensi video chart dan IPO tambahan masih menunggu pemilik.

## Pemeriksaan terbatas

- node --check: lima halaman JavaScript (verifikasi, ringkasan-review, pasar-referensi, preview-ringkas, prototype), semuanya exit 0 pada pemeriksaan sebelum koreksi label aksesibilitas US.
- HTTP verifikasi.html: 200.
- Browser: semua tujuan pemilih dimuat. US menampilkan Apple/Microsoft/Tesla; Pantauan subset fixture; Chart tanpa narasi review lama; IPO/rise/firma/indeks/leverage memiliki konten sesuai tujuan.
- Pergantian hari contoh: Hari 1 · 19 Oktober 2026 23.59 → Hari 2 · 20 Oktober 2026 00.01, pasar tetap buka.
- Riset menampilkan contoh 33/42 dan penjelasan yang dapat dibuka.
- Saham ASRI fixture terkunci menunjukkan tombol Beli disabled di detail. Ini demo UI, bukan pembuktian guard domain.
- Firma setelah simulasi penerimaan mandat memperlihatkan AUM Rp100 juta, klien aktif, hasil/fee Rp0. Tidak mengklaim siklus fee/progres/save produksi teruji.
- Beberapa pencarian locator awal gagal karena kontrol di details tertutup dan ticker mengandung badge; setelah observasi, pemeriksaan diarahkan ke kontrol aktual. Tidak melonggarkan assertion produksi atau quality gate.

Tidak menjalankan regresi produksi/APK/Android untuk pembuatan prototype. Tidak ada perubahan pada android/app/src atau save pemain. Source produksi tetap menunggu verifikasi tampilan dan rencana implementasi yang disetujui.

next_action: pemilik meninjau tiap layar melalui satu preview, memberi anotasi/persetujuan per bagian. Selesaikan angka ekonomi leverage dan referensi visual yang masih pending sebelum fitur terkait diterapkan.
