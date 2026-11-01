Kamu bertugas mengaudit seluruh fitur, UI, dan teks yang ditampilkan kepada pemain dalam proyek **Game Saham**.

**Tujuan:** temukan seluruh konten yang memenuhi layar tetapi tidak memberi manfaat yang cukup bagi pemain, seperti elemen UI yang tidak memiliki fungsi jelas, informasi berulang, kalimat kosong, label yang membingungkan, dan penjelasan yang terlalu panjang. Hasil akhir berupa laporan temuan yang bisa saya tinjau satu per satu.

Lokasi proyek: `C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM`.

## Batas pekerjaan

- Audit dan dokumentasikan temuan. **Jangan mengubah UI, teks, navigasi, alur, logika permainan, atau data pemain.**
- Perubahan tampilan, teks, navigasi, dan alur harus mengikuti aturan proyek: siapkan preview nyata dan tunggu persetujuan eksplisit pemilik sebelum penerapan. Tugas ini hanya sampai tahap deteksi dan rekomendasi.
- Baca dan ikuti `AGENTS.md`, `DESIGN.md`, design brief yang relevan, master plan terbaru, dan aturan proyek yang berlaku. Bedakan keputusan terbaru dari dokumen lama.
- Pastikan direktori dan Git root benar. Pertahankan seluruh perubahan lokal.
- Jangan membaca isi atau mengubah save pemain `Artifacts/LocalPreview/preview-save/save_game_v2.json`; jika perlu memeriksa integritasnya, gunakan hash saja. Gunakan save QA baru yang terisolasi untuk pengujian.
- Jangan melakukan ADB, instalasi perangkat, reset, clean, stash, push, publish, pengiriman pesan eksternal, perubahan pengaturan global, atau mengakses source repo lama tanpa izin yang berlaku.
- Jangan meluncurkan agen lain, launcher paralel, scheduler, atau automasi baru.
- Jangan memilih sendiri aturan produk, ekonomi, atau desain yang masih menunggu keputusan pemilik.

## Langkah 1: buat peta cakupan

Telusuri source produksi, navigasi, komponen bersama, resource teks, dan dokumen fitur yang relevan. Susun daftar seluruh area yang perlu diperiksa berdasarkan bukti aktual, termasuk lima tab utama **Beranda, Pasar, Portofolio, Bisnis, dan Lainnya**, serta seluruh layar, submenu, dan alur yang dapat ditemukan.

Jangan menganggap angka atau daftar layar historis sebagai bukti bahwa semuanya sudah tersedia. Bandingkan dengan cakupan fitur dan layar yang diwajibkan proyek; tandai fitur yang diwajibkan tetapi belum ditemukan atau belum dapat diverifikasi.

Untuk setiap area, buat daftar status:

- Belum diperiksa.
- Sedang diperiksa.
- Sudah diperiksa melalui source.
- Sudah diperiksa secara visual atau interaktif.
- Terhalang, beserta penyebabnya.

Catat juga bagian yang muncul berdasarkan kondisi: modal, drawer, tooltip, menu, formulir, pesan kesalahan, konfirmasi, notifikasi, keadaan kosong, loading, sukses, gagal, akun atau level berbeda, serta perubahan saat data atau status pasar berbeda.

## Langkah 2: audit setiap area

Untuk setiap layar dan keadaan yang dapat dijangkau, periksa teks dan elemen yang terlihat pemain, termasuk:

- Judul, subjudul, deskripsi, label, tombol, kartu, badge, ikon, dekorasi, dan angka.
- Informasi yang menyampaikan hal yang sama lebih dari sekali, baik dalam satu layar maupun pada langkah berurutan.
- Kalimat generik, slogan, penjelasan bertele-tele, teks teknis internal, placeholder, atau klaim yang tidak membantu pemain memahami atau mengambil tindakan.
- Elemen yang tidak memiliki fungsi yang jelas, aksi yang tidak dapat digunakan, informasi yang tidak relevan pada konteksnya, dan konten yang mengganggu prioritas utama layar.
- Istilah yang ambigu, terlalu teknis, tidak konsisten, atau sulit dimengerti pemain.
- Kepadatan layar portrait, hierarki informasi, keterbacaan, ukuran teks besar, dan potensi konten terpotong atau bertumpuk.

Untuk setiap kandidat, tanyakan:

1. Apa tujuan elemen atau kalimat ini bagi pemain pada saat itu?
2. Apakah informasi yang sama sudah tersedia di tempat lain yang mudah terlihat?
3. Jika dihapus atau dipindahkan, apakah pemain kehilangan informasi, konteks, navigasi, atau kemampuan mengambil keputusan?
4. Apakah ada bentuk yang lebih singkat dan tetap mempertahankan makna?
5. Apakah keberadaannya diperlukan untuk aksesibilitas, pencegahan kesalahan, konsekuensi transaksi, status, atau pemulihan masalah?

Jangan menandai konten sebagai tidak penting hanya karena panjang, dekoratif, atau tidak interaktif. Pertimbangkan konteks permainan, identitas visual, aksesibilitas, dan fungsi sebenarnya. Jika manfaatnya belum dapat dipastikan, tandai **perlu keputusan pemilik** atau **perlu verifikasi**, beserta alasannya.

## Langkah 3: lakukan perulangan audit

Kerjakan secara bertahap sampai seluruh area dalam peta cakupan memiliki status akhir yang jujur:

1. Pilih area yang belum diperiksa.
2. Telusuri source dan alur terkait, termasuk komponen bersama dan kondisi yang membuat konten muncul.
3. Jalankan aplikasi atau preview yang tersedia jika memungkinkan; periksa secara visual dan interaktif dengan data QA terisolasi.
4. Catat seluruh temuan dan bukti, termasuk kondisi yang tidak dapat direproduksi.
5. Perbarui daftar cakupan dan lanjutkan ke area berikutnya.
6. Setelah seluruh area yang bisa diakses diperiksa, lakukan putaran silang: periksa kembali komponen bersama, variasi kondisi, temuan duplikat, dan area yang awalnya terlewat.
7. Jika putaran silang menemukan area baru atau kekurangan cakupan, tambahkan ke daftar dan ulangi pemeriksaan pada area tersebut.

Jangan berhenti hanya karena sudah menemukan beberapa contoh, mencapai jumlah iterasi tertentu, atau satu putaran tidak menghasilkan temuan baru. Selesai hanya ketika seluruh cakupan yang berhasil diidentifikasi sudah diperiksa atau memiliki status terhalang yang disertai alasan dan langkah lanjutan. Tidak perlu membuat loop tanpa kemajuan: setelah kegagalan berulang, diagnosis penyebabnya dan ikuti aturan proyek untuk penanganan hambatan.

Jika waktu, akses, atau lingkungan tidak memungkinkan pemeriksaan tertentu, simpan progres dan laporkan bagian tersebut sebagai **belum terverifikasi**. Jangan mengklaim seluruh UI sudah diperiksa atau semua masalah sudah terdeteksi bila masih ada celah cakupan.

## Format temuan

Berikan ID unik untuk setiap temuan dan catat:

1. **Lokasi:** tab, layar, bagian, dan kondisi kemunculan.
2. **Konten saat ini:** kutip teks persis atau jelaskan elemen UI dengan spesifik.
3. **Bukti:** file dan baris yang relevan; sertakan bukti visual atau langkah reproduksi jika tersedia.
4. **Masalah:** jelaskan mengapa konten berpotensi tidak penting, berulang, membingungkan, atau terlalu memenuhi layar.
5. **Dampak:** apa yang menjadi sulit dilihat atau dipahami pemain.
6. **Rekomendasi:** hapus, ringkas, gabungkan, pindahkan, pertahankan dengan alasan, atau perlu keputusan pemilik. Berikan usulan teks pengganti jika memang membantu.
7. **Risiko dan ketergantungan:** informasi atau fungsi apa yang mungkin hilang jika usulan diterapkan.
8. **Prioritas:** tinggi, sedang, atau rendah, berdasarkan dampaknya pada pemain.
9. **Tingkat keyakinan dan bukti:** terverifikasi visual/interaktif, ditemukan melalui source saja, atau perlu verifikasi.

Kelompokkan temuan yang berasal dari komponen bersama tanpa menyembunyikan daftar layar yang terdampak. Jangan mengarang isi layar, lokasi file, perilaku, atau hasil pengujian.

## Laporan akhir

Sampaikan:

- Ringkasan temuan paling berdampak dan alasan prioritasnya.
- Daftar lengkap temuan sesuai format di atas.
- Matriks cakupan per tab, layar, alur, dan keadaan, dengan status serta jenis bukti.
- Daftar fitur atau keadaan yang belum dapat diperiksa, penyebab, dan langkah konkret untuk melanjutkannya.
- Hasil pemeriksaan silang dan area baru yang ditemukan selama perulangan.
- File laporan yang dibuat, perintah atau metode pemeriksaan penting, serta batas bukti host, browser, build APK, emulator, dan perangkat.

Simpan laporan audit dan daftar progres di dalam folder kerja proyek yang sesuai, tanpa mengubah source aplikasi atau data pemain. Berikan tautan ke laporan saat menyerahkan hasil. Jika seluruh cakupan belum bisa diperiksa, nyatakan dengan jelas apa yang masih tersisa.

