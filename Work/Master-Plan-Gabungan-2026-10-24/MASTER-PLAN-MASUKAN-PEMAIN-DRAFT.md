# Draft master plan masukan pemain — MENUNGGU JAWABAN PEMILIK

Tanggal:27 Oktober2026, Asia/Jakarta. Dokumen persiapan untuk agent implementasi; **belum final dan belum merupakan perintah menjalankan perubahan**. Simpan jawaban aktual dan keputusan pemilik sebelum menetapkan kriteria final. Jangan mengganti keputusan yang belum dijawab dengan asumsi.

Root produksi: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`. Sumber:21 komentar browser pemilik pada preview lokal dan masukan sebelumnya tentang tombol Beli. Masukan sebelumnya sama dengan komentar2 dan dipetakan menjadi **satu butir**, K-025, bukan task duplikat. Lima temuan teknis dari review APK-07 tetap tercatat dalam `DAFTAR-KEKURANGAN-LINTAS-PLAN.md`; rencanakan integrasinya setelah pemilik menentukan urutan.

## Catatan batas dan persetujuan

- Pemilik meminta galeri kategori direvisi melalui **preview UI yang harus disetujui sebelum implementasi/penetapan**. Jika ditolak, revisi preview sampai disetujui. Jangan menyatakan mockup sebagai implementasi.
- Pemilik ingin aturan pembiayaan berubah: agunan dihapus, bunga keterlambatan5% berdasarkan jam dunia nyata. Interval5%, basis perhitungan, compounding, jatuh tempo, proses saat game ditutup dan migrasi kontrak lama **menunggu jawaban**. Ini perubahan aturan ekonomi yang diminta pemilik, bukan sekadar desain; jangan mengarang nilainya atau menghapus hak/saldo/agunan kontrak lama sebelum migrasi disepakati dan aman.
- Penghapusan kontrol ukuran teks200% mengikuti jawaban pemilik mengenai scope; tetap perhatikan aksesibilitas, skala sistem, data preferensi lama dan fungsi lain yang terkait.
- Keinginan bar/menu baru, pindah profil/panduan/peraturan, dan pemindahan pemilih akun harus direkonsiliasi bersama supaya tidak ada rute hilang atau duplikasi. Jumlah/jenis tab baru menunggu jawaban pemilik.
- Save pemain asli hanya di-hash; pengujian memakai save QA terpisah. Pertahankan source, aset, kompatibilitas save dan aturan yang tidak diminta berubah. Jangan reset/clean/publish atau melakukan tes perangkat baru di luar mandat.

## Daftar 21 anotasi: kebutuhan dan keputusan yang menunggu jawaban

| No | Permasalahan / harapan pemilik | Arah awal untuk rencana; keputusan belum dikunci |
|---|---|---|
| A01 | Status penjatahan IPO tidak menunjukkan fase dan kalender. | Tampilkan urutan fase dan tanggal riil dari domain; jangan mengarang jadwal yang belum ada. Pemilik memilih seluruh fase atau ringkasan. |
| A02 | Tombol Beli pada detail emiten tidak terlihat sebelum scroll. | K-025: tampil langsung saat detail dibuka; pemilik menentukan lokasi serta perlakuan tombol Jual. |
| A03 | Form order terlalu memenuhi layar. | Rancang modal lebih ringkas tanpa menghapus informasi harga, biaya, risiko, aksesibilitas atau review. Pemilik menentukan bentuk. |
| A04 | Fitur kategori Portofolio muncul di bawah dan membingungkan. | Buka tujuan pada tampilan terpisah dalam aplikasi; definisi tab baru menunggu jawaban. Pertahankan Back dan akun. |
| A05 | Informasi kategori panjang dan kurang informatif. | Tampilkan ringkasan yang jelas dan buka detail jika perlu; pilihan ringkas/detail menunggu jawaban. |
| A06 | Pemilik bertanya fungsi Replay historis. | Jelaskan replay adalah simulasi strategi pada data harga historis yang tersedia, terpisah dari permainan utama; bar harian, intraday tidak direkonstruksi. Keputusan pertahankan/sembunyikan/hapus menunggu jawaban. |
| A07 | Pengaturan & Sistem diminta jadi bar baru. | Petakan lokasi yang dimaksud dan dampaknya pada lima tab; jangan menebak posisi. Bergantung A13/A19. |
| A08 | Warna kartu Booster Riset kurang membedakan dan jelas. | Bedakan jenis/status dengan warna yang memenuhi kontras serta label nonwarna. Pemilik memilih arah/preview. |
| A09 | Profil membutuhkan data/grafik statistik. | Gunakan data aktual yang tersedia; jelaskan jika histori belum ada, tanpa angka palsu. Grafik yang diprioritaskan menunggu pilihan. |
| A10 | UI order aktif/riwayat belum ramah pengguna. | Rancang daftar ringkas, status jelas, filter dan detail; gaya menunggu pilihan. Pertahankan identitas request untuk audit di detail bila perlu. |
| A11 | Dari status order ingin langsung melihat saham yang terkait. | Tambahkan tujuan detail saham terkait dan jalan kembali ke order jika disetujui; jangan mengirim order otomatis. |
| A12 | Pemilik meminta menghapus fitur ukuran teks200%. | Tentukan apakah hanya tombol pada layar tersebut atau semua kontrol preferensi; jangan menyimpulkan penghapusan semua dukungan aksesibilitas. |
| A13 | Pengaturan/save diminta menjadi menu profil, peraturan, dan panduan, memindahkan yang disebut dari Lainnya. | Buat peta lokasi sumber/tujuan dan rute balik; nasib save/reset lama menunggu jawaban, tidak dihapus diam-diam. |
| A14 | Desain yang sudah dibuat belum tampil pada galeri koleksi. | Cari dan tunjukkan desain/aset existing serta kecocokannya; rujukan tepat menunggu pemilik. Jangan mengklaim semua aset tersedia. |
| A15 | UI kategori galeri tidak disukai; preview harus diajukan sebelum perubahan ditetapkan. | Siapkan alternatif sesuai pilihan, beri kesempatan pemilik menolak/meminta revisi; implementasi setelah persetujuan desain. |
| A16 | Pembiayaan ingin disederhanakan, agunan dihapus, denda bunga5% keterlambatan berbasis waktu nyata. | Perjelas apakah sistem memilih otomatis atau merekomendasikan produk, interval/basis denda, batas dan migrasi; desain domain serta simulasi/restart diuji setelah jawaban. |
| A17 | Bahasa tab Bisnis terlalu sulit; fungsi tetap. | Buat inventaris istilah dan padanan sederhana profesional tanpa mengubah aturan/fitur; batas layar menunggu pilihan. |
| A18 | Tambahkan menu informasi kecil tentang fitur Bisnis. | Ikon informasi/panduan/teks inline menunggu pilihan; isi ringkas dari fungsi yang benar. |
| A19 | Pemilih akun transaksi dipindah ke profil. | Tetapkan cara mengganti akun dan indikator akun aktif di layar lain berdasarkan jawaban; pastikan review order tetap menampilkan akun yang digunakan. |
| A20 | IDR tertahan harus dapat diketuk untuk rincian alokasi dana. | Perlihatkan sumber reservasi, nominal/status dan navigasi detail sesuai data domain; bedakan akun, sumber dana dan nilai yang belum tersedia. |
| A21 | Dompet IDR & USD membutuhkan satu baris nilai aset gabungan. | Pemilik menentukan komponen, basis IDR/kurs, bruto atau bersih; gunakan DTO domain dan label jelas agar tidak menghitung ganda. |

## Sepuluh rekomendasi reviewer — BELUM DISETUJUI

Masukkan hanya nomor yang pemilik setujui; setiap rekomendasi harus dinyatakan sebagai usulan, bukan temuan bug otomatis.

| ID | Rekomendasi | Dasar pengamatan dan tujuan |
|---|---|---|
| R01 | Ringkasan kas yang bisa dipakai, tertahan, dan tujuan dana tertahan ditampilkan dekat tombol order. | Form order menuntut keputusan jumlah; pemain perlu tahu kemampuan transaksi sebelum mengetik. Berhubungan A20. |
| R02 | Susun form order bertahap: pilihan dasar dahulu, pengaturan lanjutan terbuka bila dibutuhkan. | Tiket memenuhi layar434×688 dan total/perkiraan berada jauh di bawah; berkaitan A03. |
| R03 | Setiap status order menggunakan bahasa pemain dan langkah berikutnya, misalnya Mengantre → lihat saham/batalkan sisa. | Contoh order memperlihatkan kode BUY/OPEN dan ID panjang; berkaitan A10/A11. |
| R04 | Beri penjelasan singkat untuk aset/angka yang belum tersedia, bukan angka nol yang bisa dibaca sebagai hasil nyata. | Preview awal mempunyai data kosong dan status belum tersedia di beberapa layar; bedakan kosong dari gagal. |
| R05 | Kelompokkan panduan menurut tujuan pemain seperti beli saham, kelola bisnis dan pahami risiko. | Lainnya memuat19 launcher pada sesi uji; mengurangi waktu mencari tanpa menghapus fungsi. |
| R06 | Tampilkan indikator status bursa dan waktu game dengan informasi tambahan saat diketuk. | Header menampilkan Pasar Tutup tanpa penjelasan berikutnya pada screenshot; gunakan jadwal domain nyata dan status belum tersedia bila tak ada. |
| R07 | Tampilkan biaya, potongan dan hasil bersih pada langkah konfirmasi transaksi sebelum pemain menyetujui. | Kejelasan dampak finansial penting pada order, pembelian koleksi, dan pembiayaan; gunakan quote domain. |
| R08 | Tambahkan penanda data game/model dan rentang waktu grafik dekat angka yang ditampilkan. | Kartu indeks memakai label Model dan replay menggunakan data historis; bantu pemain memahami angka dan konteks. |
| R09 | Ketika transaksi atau sinkronisasi belum pasti, sediakan status dan cara memeriksa ulang yang jelas. | Review APK-07 menemukan K-018/K-024; bedakan gagal, sedang diproses dan hasil belum terkonfirmasi. |
| R10 | Pertahankan posisi layar dan konteks saat kembali dari detail/berita/order. | Perjalanan Pasar/Portofolio melibatkan halaman detail dan Back; membantu pemain melanjutkan tugas tanpa mencari ulang. |

## Lima pertanyaan tambahan — MENUNGGU JAWABAN

1. Bar Pengaturan baru ditambah sebagai tab keenam, menggantikan Lainnya, atau menjadi tombol atas agar lima tab tetap?
2. Denda5% dihitung sekali, setiap24 jam nyata, setiap jam nyata, atau interval lain?
3. Dasar5%: cicilan tertunggak, sisa pokok, atau total yang sudah termasuk denda? Apakah denda berbunga/majemuk?
4. Kontrak lama dan agunannya: tetap berlaku sampai selesai, migrasi aman, atau tunggu rancangan migrasi untuk disetujui?
5. Selain galeri, apakah preview wajib untuk semua perubahan UI besar, galeri + navigasi, atau galeri saja?

## Hasil observasi reviewer dan keterbatasannya

Preview uji: server Java-domain terpisah port18947, save UX QA baru, viewport434×688. Reviewer membuka Beranda, Pasar, detail emiten, tiket order tanpa submit, Portofolio/kategori, Bisnis/koleksi, Lainnya, profil, replay dan panduan. Tombol Beli pada detail tidak terlihat pada viewport awal. Screenshot dan teks tersimpan di `Artifacts/Analisis-UX-Preview-2026-10-27/`.

Observasi ini adalah uji pendek host/browser dengan data baru; tidak membuktikan seluruh transaksi, keamanan, perilaku Android fisik, atau seluruh ukuran layar. Screenshot pasar/portofolio yang terlihat gelap selama sinkronisasi adalah state saat ditangkap, bukan kesimpulan bahwa layar selalu gelap. Uji lebih luas ditetapkan pada plan final.

## Format plan final setelah jawaban

1. Rekonsiliasi tiap jawaban dengan A01–A21 serta rekomendasi R yang disetujui; tandai konflik/kebutuhan klarifikasi tersisa.
2. Buat peta navigasi dan alur pemain, daftar tugas yang dapat ditelusuri ke anotasi, prioritas/dependensi dan kriteria penerimaan. Integrasikan temuan teknis sebelumnya tanpa menganggap telah dikerjakan.
3. Masukkan urutan preview galeri → persetujuan pemilik → implementasi; jika pemilik meminta preview UI lain, tambahkan gate yang sesuai.
4. Rinci kontrak pembiayaan nyata, migrasi save/kontrak, idempotensi, clock mundur/restart dan uji batas berdasarkan jawaban, bukan asumsi.
5. Definisikan gate fungsi, visual, aksesibilitas, bukti host/browser/APK dan batas native; hitung task aktual setelah rincian disetujui.
6. Pertahankan seluruh pertanyaan yang belum dijawab sebagai keputusan terbuka dan jangan menyajikan plan final siap eksekusi sampai keputusan yang diperlukan diperoleh.
