# Koreksi pemilik — preview ringkas

4 November 2026. Enam anotasi baru memperbarui keputusan sebelumnya; belum ada persetujuan desain penuh atau izin penerapan UI produksi.

1. Jadwal IPO hanya dalam detail emiten setelah kartu diketuk. Berlaku untuk Upcoming/Ongoing/Past. Daftar utama tidak memuat timeline.
2. Hapus kartu “Bersiap sebelum penawaran” dan keterangan pengantar yang tidak diperlukan. Ringkasan pesanan tampil bila memang ada pesanan, bukan filler kosong.
3. Pemilik menyetujui kontrol perpanjangan/tidak, tetapi menolak tampilan Riset terdahulu. Persetujuan kontrol bukan persetujuan seluruh layar.
4. Hapus semua usulan manfaat riset yang ditolak; jangan menganggap daftar alternatif berikutnya otomatis disetujui. Scope manfaat baru belum diputuskan.
5. Keputusan terbaru indeks: Pasar → pilih indeks → daftar saham anggota indeks itu. Ini menggantikan jawaban Q19 sebelumnya yang meminta semua saham bursa dengan filter.
6. Hindari pemborosan teks/UI. Alat review tidak menjadi UI produksi: judul review, narasi layout, menu lima layar dan pemilih alternatif dihapus dari prototype ringkas. Satu strip kecil “Preview · data contoh” dengan pemilih layar tetap berada di luar area app untuk review.

Artefak: Artifacts/Preview-Anotasi-2026-11-04/preview-ringkas.html. Tidak mengganti source produksi. Prototype lama dipertahankan agar anotasi dan bukti tetap dapat dirujuk.

Pemeriksaan: node --check exit 0; HTTP 200; browser membuktikan daftar IPO tanpa Jadwal untuk tiga status dan Jadwal muncul saat detail dibuka; sakelar perpanjangan berubah ke nonaktif dengan keterangan akses tersisa; LQ45 contoh membuka dua baris anggota, pencarian BMRI menyaring dengan benar. Screenshot Riset: Artifacts/Preview-Anotasi-2026-11-04/riset-ringkas.png. Keanggotaan indeks, harga, jadwal dan berita seluruhnya fixture UI, bukan data domain atau daftar anggota bursa nyata.

next_action: pemilik meninjau preview ringkas; tunggu referensi chart/IPO yang dijanjikan dan persetujuan eksplisit per layar sebelum implementasi source produksi.
