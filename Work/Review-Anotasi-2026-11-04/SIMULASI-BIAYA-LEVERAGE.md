# Simulasi biaya leverage untuk keputusan pemilik

4 November 2026. Tahap review, bukan implementasi ekonomi produksi.

## Keputusan diterima

- Pemilik meminta perbandingan beberapa simulasi biaya sebelum menetapkan tarif.
- Tenggat margin call menggunakan waktu nyata agar pemain sempat merespons saat dunia berjalan 48×.
- Batas leverage menggabungkan likuiditas/risiko saham dan kelayakan pemain. Pemetaan spesifik belum dipilih.

## Source yang diperiksa

RiskComplianceService.java:429 mendefinisikan ANNUAL_MARGIN_LOAN_INTEREST_RATE = 0.18. Baris 515 membaginya dengan 360, menghasilkan 0,0005 = 0,05% per hari. Bunga dibulatkan dua desimal. Saat kas tidak mencukupi, implementasi menambahkan bunga ke margin debt. Ini bukti formula source saat dibaca, bukan bukti frekuensi pemanggilan, ketepatan accrual/catch-up, atau kesiapan fitur margin.

## Prototype

Artifacts/Preview-Anotasi-2026-11-04/simulasi-leverage.html; menu Leverage dalam verifikasi.html diarahkan ke halaman ini. Acuan 0,05% dibanding alternatif ilustrasi 0,02% dan 0,10%. Tidak menyatakan tarif alternatif sudah dipilih.

Input: pokok 0–100 juta rupiah (contoh modal sendiri 50 juta, maksimum 3x), durasi 0–30 hari game bulat, perubahan harga, bunga dibayar kas atau dikapitalisasi ke utang. Mode kas mengasumsikan kas pembayaran bunga tersedia di luar posisi investasi. Biaya jual/beli tidak termasuk; hasil diberi judul “setelah bunga”, bukan laba bersih lengkap.

Contoh default: pinjaman Rp40 juta, modal sendiri Rp50 juta, posisi Rp90 juta, durasi tiga hari game = 90 menit nyata, harga naik 2%:

| Tarif/hari game | Bunga dibayar kas | Hasil setelah bunga, sebelum fee transaksi |
|---|---:|---:|
| 0,02% | Rp24.000 | +Rp1.776.000 |
| 0,05% | Rp60.000 | +Rp1.740.000 |
| 0,10% | Rp120.000 | +Rp1.680.000 |

Jika bunga 0,05% ditambahkan ke utang setiap hari, biaya tiga hari menjadi Rp60.030,01. Kasus perbedaan ini sengaja ditampilkan, bukan menyederhanakan seolah seluruh bunga selalu sederhana.

Margin call: contoh 30/60/120 menit nyata setara 1/2/4 hari game. Default 60 menit adalah ilustrasi, belum keputusan. Pada pokok tetap Rp40 juta dengan bunga dibayar kas, tambahan bunga dua hari adalah Rp16.000/Rp40.000/Rp80.000 untuk tiga tarif. Perhitungan tenggat dan biaya mengasumsikan pinjaman/posisi terus dipertahankan selama masa tunggu; belum menyimulasikan perubahan harga, ambang jaminan, atau eksekusi likuidasi.

## Pemeriksaan

- JavaScript syntax check simulasi-leverage dan verifikasi: exit 0; HTTP 200.
- check-simulasi-leverage.cjs: exit 0; sembilan assertion nominal memeriksa tarif, kapitalisasi/pembulatan, tanpa pinjaman, nol hari, biaya tenggat dua hari, dan pokok maksimum.
- Browser: perubahan mode pembayaran menampilkan bunga acuan Rp60.030,01; input -1 menampilkan pesan validasi dan menyembunyikan hasil; setelah input dipulihkan hasil kembali Rp60.000. Pembukaan panel margin call menunjukkan 60 menit nyata = 2 hari game dan bunga Rp40.000 pada tarif 0,05%.
- Screenshot: Artifacts/Preview-Anotasi-2026-11-04/simulasi-biaya-leverage.png.
- Tidak mengubah source Java, save, tarif produksi, atau verifier produksi. Bukti hanya prototype/formula contoh, bukan keberhasilan domain/APK/perangkat.

## Pending

Formulir tiga pertanyaan dikirim untuk memilih tarif, durasi tenggat nyata, dan penanganan bunga saat kas kurang. Ambang jaminan, aturan sebagian hari dan pemetaan maksimum 2x/3x/4x masih perlu dirumuskan sesudah pilihan dasar. Persetujuan keseluruhan UI tetap per layar; jangan menafsirkan jawaban ekonomi sebagai persetujuan seluruh prototype.

## Jawaban terbaru dan simulasi tunggakan

Pemilik memilih 0,02% dari dana pinjaman yang digunakan per hari game dan tenggat margin call 120 menit nyata. Penanganan saat kas kurang belum dipilih; pemilik meminta simulasi lanjutan. Bagian Pending terdahulu digantikan untuk dua angka yang sudah dipilih.

Preview ditambah `simulasi-leverage.html?view=arrears`: membandingkan bunga terpisah tanpa bunga atas bunga versus kapitalisasi harian. Pilihan pokok Rp40 juta/Rp100 juta, horizon 3/7/30 hari game, dan tambahan 4 hari game (120 menit nyata). Tidak ada pembayaran atau perubahan pokok dalam contoh. Tunggakan tetap menjadi kewajiban dan mengurangi kekayaan bersih pada kedua pilihan.

Contoh Rp40 juta: setelah 4 hari bunga terpisah Rp32.000, kapitalisasi Rp32.009,60. Setelah 30 hari bunga terpisah Rp240.000, kapitalisasi Rp240.697,27. Tarif dan tenggat source produksi belum diubah.

Pemeriksaan: syntax exit 0, HTTP 200, runnable check existing diperluas untuk tarif terpilih/4 hari, kedua nominal pokok dan horizon, serta identitas pokok+bunga. Exit 0. Expected value sebelumnya dipertahankan; tidak melonggarkan verifier produksi. Perhitungan juga dibandingkan dengan Decimal Python untuk nominal yang ditampilkan.

Browser tidak bisa dibuka: tool melaporkan local browser use disabled in Work settings. Tidak mengubah pengaturan atau mencoba jalur UI lain. Karena itu revisi tunggakan belum diverifikasi secara visual dan tidak ada screenshot baru. next_action: pemilik membuka tautan bila tersedia dan memilih kebijakan tunggakan; verifikasi visual saat akses browser tersedia.

## Keputusan akhir kebijakan bunga saat kas kurang

Pemilik memilih: bunga ditambahkan ke pokok utang setiap hari game bila kas tidak cukup. Bunga berikutnya dihitung dari utang yang sudah bertambah. Ini menggantikan status pending kebijakan tunggakan di atas; opsi tunggakan terpisah hanya pembanding historis.

Paket keputusan: 0,02%/hari game atas utang yang digunakan, kapitalisasi harian ketika kas kurang, tenggat margin call 120 menit nyata, batas leverage berdasarkan risiko/likuiditas saham dan kelayakan pemain. Preview diperbarui agar mode kapitalisasi terpilih pada awal pembukaan. Tidak mengubah source produksi atau menyatakan UI sudah disetujui.

Masih perlu rincian sebelum implementasi penuh: syarat/ambang jaminan, pemetaan 2x/3x/4x, aturan sebagian hari, serta penanganan pembayaran bunga ketika kas hanya cukup sebagian. Jangan menganggap jawaban kapitalisasi menentukan rincian tersebut secara otomatis. next_action: pemilik memverifikasi preview per bagian dan tinjau rancangan angka/aturan yang belum diputuskan.
