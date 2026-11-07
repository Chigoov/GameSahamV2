# Koreksi chart berdasarkan screenshot

Goal E01–E11 tetap aktif. Steering terbaru: rapikan bentuk chart jika masalah juga ada di produksi.

Produksi ditemukan memakai body/volume width 8 konstan meski step antarbar bisa lebih kecil. app.js kini memakai fitChartBars untuk mengelompokkan rentang menurut lebar plot (~7px/bar) dengan open pertama, high/low ekstrem, close terakhir, total volume dan status partial yang dipertahankan. Input tidak dimutasi; waktu menampilkan rentang gabungan. Body dan volume mengikuti min(10, step*0.65). Signature termasuk lebar plot.

style.css sudah mendapat grid timeframe empat kolom minmax(0,1fr), kontrol/chart min-width 0 dan plot overflow hidden. Upaya opsional menghapus deklarasi display:flex lama yang sudah ditimpa display:grid gagal tulis; perubahan utama sebelumnya tetap ada. File tidak readonly dan ruang disk 89GB; belum diketahui penyebab write failure. Tidak menimpa paksa source.

Prototype: toolbar dua kolom dan chart constrained; grafik padat dikelompokkan, badan candle mengikuti jarak, fixture sinus diganti deterministic walk. Harga fixture bukan harga domain nyata.

Pemeriksaan: check-chart-density.cjs exit 0 (enam lebar, jumlah bar, OHLC, volume, partial, input tidak dimutasi, empty/single dan body<step); node --check app.js exit 0; JS prototype syntax exit 0. Tidak mengubah expected tes lama. Visual NEEDS_VALIDATION karena tool menyatakan browser lokal disabled di Work settings. Tidak ada screenshot baru/bukti Android.

next_action: verifikasi responsif/teks besar dan interaksi produksi saat akses tersedia. E04 belum seluruhnya selesai; lanjut cutoff midnight dan E02–E11 sesuai checkpoint. Preview: verifikasi.html?scene=chart, muat ulang agar mengambil revisi.
