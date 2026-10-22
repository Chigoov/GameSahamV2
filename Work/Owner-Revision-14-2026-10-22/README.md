# Paket long task 14 revisi — siap untuk setup agent

Tanggal keputusan: 22 Oktober 2026. Pemilik sudah menjawab 15 dari 15 pertanyaan. Paket ini berisi spesifikasi, katalog, tracker, dan prompt untuk agent berikutnya. Source game belum diubah dan implementasi belum dimulai oleh paket ini.

## Urutan penggunaan

1. Baca [keputusan pemilik](DECISIONS.json), kemudian [spesifikasi lengkap](LONGTASK-14-REVISI.md).
2. Lihat [review dan inventaris source existing](REVIEW-AND-INTAKE.md). Bukti review asli tersimpan di `../../Artifacts/Owner-Review-2026-10-22/`.
3. Agent mengerjakan [tracker](TRACKER.csv): lima perbaikan F01–F05, 14 revisi R01–R14, dan integrasi INT. Semua status awal adalah NOT_STARTED.
4. Gunakan [katalog 150 koleksi](KATALOG-KOLEKSI-150.csv) dan [katalog 24 reksa dana](KATALOG-REKSA-DANA-24.csv). ID, kepemilikan, dan kontrak lama wajib dipertahankan. Harga, biaya, serta syarat baru merupakan rancangan awal ekonomi game yang perlu dikalibrasi, bukan harga dunia nyata atau hasil pengujian yang sudah lulus.
5. Salin [prompt lengkap](../../Prompts/Longtask-14-Revisi-Pemilik-2026-10-22.md) ke agent yang akan disetup pemilik.

## Keputusan inti

- Game otomatis: satu hari game berlangsung 15 menit nyata saat aplikasi terbuka, atau satu jam nyata saat inactive. Kontrol sesi manual dihapus.
- Profil risiko mengikuti perilaku. Misi memakai tab Harian/Mingguan/Karier; profil memakai Kinerja/Keuangan/Pencapaian. UI mengikuti pola aplikasi investasi yang ringkas.
- Minimal tiga IPO setiap bulan game, memakai identitas perusahaan asli dan rekaan. Tidak ada badge simulasi berulang pada kartu; konteks dunia game disampaikan terpusat. Sediakan inbox, banner, dan notifikasi HP opsional.
- Seluruh fundamental berasal dari ekonomi game. Sumber identitas perusahaan dipisahkan dari sumber angka finansial.
- Pasar lebih agresif. Berita memengaruhi keputusan NPC, order, transaksi, dan harga; tidak boleh menghasilkan harga palsu atau jaminan prediksi.
- Total 150 koleksi dalam 12 kategori dan lima rarity. Total 24 reksa dana dalam delapan keluarga. Saham, obligasi, komoditas, aset digital, deposito, dan reksa dana harus terintegrasi.

## Pemeriksaan paket

Dari folder paket, jalankan `python validate-package.py`. Laporan [PACKET-VALIDATION.json](PACKET-VALIDATION.json) memeriksa jumlah, ID, data lama, alokasi dana, jawaban, dependency, dan tautan dokumen. Hasilnya khusus validasi paket perencanaan; tidak meluluskan domain, browser, APK, atau pengujian HP.

Source produksi tetap Java dan Android WebView lokal. Perubahan agent lain, save pribadi, applicationId, signing, serta fitur lama harus dipertahankan. Tidak ada push, publikasi, instalasi, ADB, atau perubahan global Hermes secara otomatis. Bukti host, build, dan perangkat harus dilaporkan terpisah.
