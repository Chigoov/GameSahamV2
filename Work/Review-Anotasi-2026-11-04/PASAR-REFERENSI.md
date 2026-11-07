# Preview Pasar mengikuti gambar pemilik

Referensi: C:/Users/HYPE AMD/Downloads/WhatsApp Image 2026-11-04 at 00.27.02.jpeg (926×1600). Mandat: samakan bentuk daftar Pasar dan tanyakan bagian yang belum jelas, tanpa pemborosan UI/teks.

Preview terpisah: Artifacts/Preview-Anotasi-2026-11-04/pasar-referensi.html. Pola: daftar datar hitam, logo bulat, ticker/nama, sparkline tengah, harga dan perubahan nominal/persen rata kanan. Tidak memakai kartu bertumpuk atau keterangan per baris.

Jawaban lanjutan pemilik:
- Badge 2x/3x/4x dipertahankan sebagai informasi leverage; mekanikanya harus dibahas dahulu. Ini belum mengizinkan implementasi pinjaman/transaksi leverage.
- Grafik mini memakai pergerakan sejak 00.00 WIB sampai waktu game saat ini.

Pertanyaan lanjutan diajukan: apakah 3x berarti total daya beli tiga kali modal dengan pembiayaan broker, leverage intrahari, atau mekanika dibahas sesudah persetujuan UI. Masih menunggu jawaban pada penyerahan preview.

Source produksi saat dibaca loadStocks app.js:2028 dst memakai inisial dua huruf, badge bursa/level/pantauan; belum menampilkan logo/sparkline referensi ini. Pencarian source menemukan infrastruktur margin/risk, tetapi tidak membuktikan pemetaan batas leverage per emiten. Jangan menganggap badge preview fitur domain sudah berjalan.

Batas: semua angka dan lintasan chart adalah contoh untuk membandingkan UI; logo memakai CSS clipping dari screenshot pemilik yang disalin ke direktori preview. Ini bukan aset logo produksi atau data harga terkini. Tidak mengubah source produksi, katalog, save, atau mekanika uang. Tidak menggunakan image generation/dependency baru.

Pemeriksaan: JavaScript node --check exit 0 pada versi awal; perubahan berikutnya hanya default badge dan label aksesibilitas. Browser sesudah perubahan memperlihatkan 11 baris, logo, sparkline dan badge; screenshot visual ditinjau. Bukti final: Artifacts/Preview-Anotasi-2026-11-04/pasar-referensi.png.

next_action: persetujuan eksplisit bentuk daftar Pasar, lalu perumusan leverage dan validasi data grafik harian sebelum implementasi sesuai scope yang ditunjuk pemilik.
