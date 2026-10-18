# Keputusan dan pertanyaan terbuka

Keputusan terkonfirmasi: trading + karier sama penting; semua fitur lama wajib; perusahaan nyata dengan pasar internal + replay historis; 2D portrait tanpa nuansa 3D; hasil akhir APK.

Semua OQ di bawah berstatus OPEN saat paket disiapkan. PRD lengkap tetap sumber requirement. Menyetujui mulai coding tidak menetapkan jawaban angka ekonomi, sumber data, fidelity replay, target perangkat, atau gate distribusi.

## Kebijakan keputusan

Keputusan teknis rutin yang reversibel boleh dibuat Hermes dan dicatat dengan alasan serta pemeriksaan. Parameter eksperimen pengembangan harus berlabel sementara dan tidak boleh meluluskan acceptance yang memerlukan keputusan produk. Jangan menambah framework untuk mengantisipasi semua jawaban OQ.

Untuk keputusan produk/data yang mengubah perilaku, tulis rekomendasi konkret dan dampaknya, minta jawaban yang diperlukan, lalu kerjakan bagian independen. Jangan menganggap diam sebagai persetujuan dan jangan menghapus fitur wajib untuk menghindari blocker.

## Catatan keputusan baru

Belum ada keputusan implementasi. Tambahkan tanggal, ID OQ/ADR bila relevan, status PROPOSED/CONFIRMED, pemberi keputusan, alasan, serta pengaruh pada requirement. Jangan mengubah OPEN menjadi CONFIRMED tanpa bukti keputusan.

## Daftar keputusan dari PRD

Pertanyaan berikut adalah daftar keputusan yang masih terbuka. Jawaban tidak diisi menggunakan asumsi tersembunyi.

| ID | Keputusan yang belum ditetapkan | Dampak |
|---|---|---|
| OQ-01 | Apakah pengguna hanya pemilik, kelompok terbatas, atau publik? Seberapa paham mereka tentang saham? | Bahasa tutorial, kedalaman penjelasan, pengujian UX, dan distribusi |
| OQ-02 | Bursa, indeks, instrumen, dan jumlah emiten aktif apa saja yang wajib tersedia? Apakah seluruh anggota setiap indeks harus dapat diperdagangkan? | Batas cakupan data, beban pasar, dan penerimaan fitur global |
| OQ-03 | Dalam replay, apakah jalur harga harus persis historis dengan eksekusi pemain sebagai model terpisah, atau histori menjadi awal skenario yang kemudian dapat berubah? | Definisi replay, market impact, dan pembuktian realisme |
| OQ-04 | Dataset historis berasal dari mana, mencakup tahun berapa, dan beresolusi apa? Apakah tersedia transaksi individual, antrean, fundamental, dan berita bertanggal? | Fitur yang dapat direpresentasikan secara jujur |
| OQ-05 | Bagaimana hak penggunaan dan distribusi dataset untuk APK ditetapkan? Apakah data dibundel, diimpor, atau disediakan sebagai paket tambahan? | Ketersediaan offline, ukuran APK, dan distribusi |
| OQ-06 | Apakah waktu mengikuti dunia nyata atau waktu game? Apakah dapat dijeda/dipercepat? Apa yang terjadi saat background atau aplikasi ditutup? | Sesi, billing, bunga, agenda, dan fair play |
| OQ-07 | Berapa modal awal, biaya transaksi, spread FX, pajak game, settlement, serta aturan pembulatan? | Ekonomi dasar dan rekonsiliasi |
| OQ-08 | Jenis serta masa berlaku order apa yang dipilih? Bagaimana stop/TP, sisa market order, antrean saat halt, dan lelang diproses? | Kontrak eksekusi dan kasus ekstrem |
| OQ-09 | Berapa populasi NPC, ragam strategi, frekuensi keputusan, dan aturan pembentukan likuiditas? | Realisme, determinisme, dan performa |
| OQ-10 | Berapa jenjang, syarat promosi, reward, gaji, penalti, serta jalur pemulihan saat kehabisan modal? | Perjalanan karier dan keseimbangan game |
| OQ-11 | Bagaimana tarif fee, high-water mark, kebutuhan kas, kepercayaan, penarikan, dan penghentian mandat ditentukan? | Ekonomi firma dan konsekuensi klien |
| OQ-12 | Apa bentuk obligasi dan komoditas yang diperdagangkan? Bagaimana unit, kupon, jatuh tempo, settlement, dan konversinya? | Definisi multi-aset yang dapat diimplementasikan |
| OQ-13 | Kapan margin/short dibuka, berapa jaminan dan biaya, serta bagaimana recall dan likuidasi ditangani? | Risiko lanjutan dan potensi kerugian |
| OQ-14 | Apakah skenario korporasi, berita, dan IPO fiktif boleh dilekatkan pada perusahaan nyata dalam mode internal? | Konten dan pembedaan fakta historis dari dunia game |
| OQ-15 | Apa paket terminal, manfaat, harga USD game, periode, dan kebijakan perpanjangannya? | Akses informasi serta keseimbangan progres |
| OQ-16 | Perangkat Android mana yang menjadi target minimum dan penerimaan? Berapa batas APK, memori, waktu buka, dan kelancaran? | Definisi terukur “lebih ringan” |
| OQ-17 | Apakah progres APK lama wajib dapat dilanjutkan seluruhnya? Versi save dan APK lama mana yang menjadi acuan migrasi? | Pengujian kompatibilitas dan perlindungan data |
| OQ-18 | Apakah penerimaan akhir cukup APK terbangun, atau wajib terpasang dan lulus journey pada HP pemilik? | Batas serah terima dan bukti selesai |
| OQ-19 | Apakah seluruh fitur dapat dibuka bertahap melalui karier, atau harus ada mode bebas yang menyediakan semuanya sejak awal? | Onboarding, akses fitur, dan pengujian |
| OQ-20 | Apakah struktur lima menu dan arah visual 2D perlu penyesuaian preferensi khusus? | Finalisasi desain antarmuka |

**Keputusan yang menghalangi spesifikasi implementasi final:** cakupan data dan instrumen, perilaku replay, sistem waktu, parameter ekonomi, definisi instrumen multi-aset, aturan margin/short, serta perangkat dan kriteria penerimaan.

**Status akhir dokumen:** PRD mencakup seluruh fitur wajib yang diminta. Produk dan APK belum dibangun melalui pekerjaan ini. Estimasi jadwal, ukuran APK, dan performa belum dapat ditetapkan sebagai komitmen sebelum keputusan terbuka yang memengaruhinya diselesaikan.
