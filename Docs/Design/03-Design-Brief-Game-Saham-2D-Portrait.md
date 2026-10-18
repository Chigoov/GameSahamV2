# Design Brief Game Saham 2D Portrait

Versi 1.0. Tanggal 18 Oktober 2026. Acuan produk: [PRD](../Product/02-PRD-Game-Saham-2D-Portrait.md). Acuan pekerjaan: [rencana Hermes](../../Work/README.md).

Brief ini menetapkan keputusan desain sebelum kode game ditulis. Seluruh fitur MVP tetap wajib. Arah desain adalah jurnal investasi profesional: informasi finansial presisi, agenda karier yang hidup, serta konsekuensi keputusan yang mudah diperiksa. Antarmuka sepenuhnya 2D portrait.

Keputusan visual dan interaksi pada brief ini merupakan keputusan desain kerja yang diminta pemilik. Angka ekonomi, sumber data, perilaku harga replay, dan syarat perangkat yang masih terbuka dalam PRD tidak ditetapkan lewat desain. Semua contoh isi saat eksplorasi harus diberi label contoh; nominal reward, tarif, harga, profil klien, serta hak transaksi produksi berasal dari domain.

## 1. Design principles

Tepat tiga aturan berikut berlaku untuk seluruh layar.

| Prinsip | Aturan wajib | Alasan dan cara memeriksa |
|---|---|---|
| Konteks sebelum komitmen | Akun, mode, instrumen, mata uang, unit, biaya dan akibat harus terlihat sebelum tindakan ekonomi. Sisi beli/jual tidak dipilih otomatis dari warna grafik. | Kesalahan akun dan asumsi harga lebih merugikan daripada satu langkah review. Tinjauan order/FX/mandat wajib dapat dipahami tanpa kembali ke layar sebelumnya. |
| Bukti sebelum ornamen | Setiap harga, grafik, laporan dan status menunjuk waktu serta asal data. Outcome dipisahkan dari estimasi. Tidak ada grafik acak, saldo dekoratif, atau klaim future knowledge. | Realisme ditentukan keterlacakan. Audit satu angka sampai transaksi/event/dataset; jika tidak tersedia tampilkan ketiadaan, bukan angka pengganti. |
| Satu keputusan per langkah | Satu halaman mempunyai satu prioritas; informasi sekunder berada dalam tab/detail. Trading dan karier mendapat hierarki setara pada Beranda. | Semua fitur wajib tidak berarti semua fitur tampil sekaligus. Pemain harus dapat menyebut tujuan halaman dan tindakan berikutnya tanpa memindai dashboard penuh panel. |

## 2. Visual direction

### Mood dan bahasa visual

Tenang, teliti, berwibawa, dengan kesan buku kerja seorang pengelola investasi. Permainan terasa melalui agenda, percakapan klien, target, pangkat, dan konsekuensi keputusan; bukan lewat ruang kantor, hologram atau dekorasi mesin trading.

Gunakan navy berlapis dengan teks putih hangat dan aksen emas kusam. Navy mempertahankan identitas finansial proyek lama, sedangkan putih hangat dan emas menggeser suasana menuju laporan investasi yang bersifat personal. Emas menunjukkan tindakan utama dan progres penting, tidak menyatakan rekomendasi saham. Hijau dan koral hanya untuk makna ekonomi/status yang dijelaskan teks.

Tampilan gelap adalah satu tema awal yang ditetapkan. Tema terang belum menjadi requirement. Hindari menambah dua tema sebelum satu tema ini konsisten dan aksesibel.

### Referensi dan batas penerapannya

- Dokumen konsep milik proyek: ambil hubungan trading-karier-klien dan lima area navigasi; tinggalkan komposisi landscape serta dekorasi 3D.
- [IBM Plex](https://github.com/IBM/plex): pilih satu keluarga Sans untuk identitas yang teknis tetapi tetap nyaman untuk narasi. IBM menyediakan Plex dengan OFL dan menyebut penggunaan UI sebagai salah satu tujuan desainnya. Pilihan font ini tidak mewajibkan library komponen IBM.
- [Carbon data visualization](https://www.carbondesignsystem.com/building-blocks/data-visualization/color-palettes): ambil disiplin membedakan warna kategori dan warna peringatan. Palet produk di bawah merupakan pilihan sendiri; tidak menyalin dashboard atau mengadopsi framework Carbon.
- [WAI APG](https://www.w3.org/WAI/ARIA/apg/): referensi perilaku tab, dialog, dan navigasi keyboard. Bentuk visual tetap mengikuti brief ini.

### Yang dihindari

- Neon cyan/ungu, glow, glass blur, latar kota, koin terbang, avatar besar dan badge prestasi yang memenuhi layar.
- Semua informasi dibungkus kartu kecil dengan bobot yang sama. Gunakan daftar editorial dan pembatas; kartu hanya untuk ringkasan atau satu keputusan penting.
- Hijau sebagai tombol beli dan merah sebagai tombol jual secara otomatis. Menjual tidak selalu berarti rugi; membeli tidak selalu keputusan baik.
- Donat/gradient sebagai hiasan ketika daftar alokasi lebih jelas. Tidak ada leaderboard, countdown semu, confetti laba, atau dorongan transaksi impulsif.
- Teks kecil untuk membuat semua informasi muat; tabel penuh diperkecil ke lebar ponsel; angka penting dipotong dengan elipsis.
- Logo atau tampilan khas terminal pihak lain. Identitas perusahaan cukup ticker/nama; logo bukan dependensi untuk memahami instrumen.

### Bahasa, ikon dan gerak

Bahasa utama Indonesia, kalimat singkat dan faktual: “Order diterima”, “Terisi sebagian”, “Estimasi kedaluwarsa”, “Data historis belum tersedia”. Kata “berhasil” selalu menyebut tindakan yang selesai. Jangan menyebut order filled ketika domain baru menerima permintaan.

Gunakan satu gaya ikon garis 24 unit dengan ketebalan optik setara 2; ikon dekoratif tidak dibacakan screen reader. Label teks selalu menyertai lima menu utama, tindakan ekonomi, dan status. Tidak perlu memasang paket ikon besar: implementasi mengambil hanya ikon yang benar-benar dipakai dengan lisensi yang dicatat.

Feedback tekan 120 ms, transisi tab/halaman 180 ms, batas maksimum animasi 240 ms. Reduced motion membuat perpindahan seketika atau opacity singkat tanpa translasi. Harga berubah tanpa flip digit, kilatan terus-menerus atau pergeseran baris. Berita baru ditawarkan lewat “Ada berita baru”, bukan menyisipkan baris saat pemain sedang membaca.

## 3. Design tokens

### Warna

Semua nilai adalah token bersama, bukan izin membuat variasi warna per layar. Surface tidak memakai transparansi di belakang teks.

| Token | Hex | Peran |
|---|---|---|
| background | #0F1720 | Latar halaman |
| surface | #172330 | Panel, daftar terkelompok, form |
| surface-raised | #203140 | Dialog, panel terpilih, header tabel |
| text-primary | #F5F1E8 | Judul, nilai, isi utama |
| text-secondary | #B6C2CC | Label dan deskripsi |
| text-muted | #94A5B5 | Timestamp, konteks sekunder; tetap terbaca |
| action | #E0BD7A | CTA utama, indikator lokasi aktif |
| on-action | #111A24 | Teks/ikon pada tombol emas |
| action-hover | #EACB91 | Hover desktop pada tombol utama |
| action-pressed | #CFAA68 | Feedback tekan; teks tetap on-action |
| positive | #79D3B0 | Kenaikan, laba, hasil valid, dengan tanda/teks |
| negative | #FF9A91 | Penurunan, kerugian, tindakan berisiko, dengan tanda/teks |
| warning | #F0C477 | Halt, estimasi kedaluwarsa, mendekati batas |
| information | #9CC9EF | Mode historis, data, penjelasan netral |
| positive-surface | #19392F | Banner positif |
| negative-surface | #462B31 | Banner error atau risiko |
| warning-surface | #3A3326 | Banner peringatan |
| information-surface | #1D3345 | Banner data/mode |
| border-interactive | #7B8FA1 | Batas input dan kontrol yang harus dikenali |
| divider | #334655 | Pembatas dekoratif; bukan satu-satunya penanda kontrol |
| focus | #E0BD7A | Ring fokus 3 unit dan offset 3 unit |
| scrim | #000000 pada 64% | Belakang dialog; tidak dipakai untuk mengukur kontras teks dialog |

Disabled memakai surface-raised, text-muted, label keadaan dan tanpa CTA aktif; jangan mengandalkan opacity seluruh komponen. Alasan tidak tersedia tetap berada di teks yang dapat dibaca. Selected memakai ring/border action dan penanda teks/ikon, tidak hanya perubahan fill.

Untuk grafik: harga utama information; rata-rata/benchmark action dengan garis putus-putus; gain positive; loss negative. Kategori alokasi menggunakan information, positive, action, dan #C6ACED dengan label langsung serta tabel. Bentuk/garis/pola membedakan seri; tidak ada arti wajib yang hanya disampaikan warna.

### Kontras palet

Rasio dihitung dari pasangan hex opak menggunakan luminans relatif sRGB. Angka tampilan dibulatkan, tetapi evaluasi ambang menggunakan nilai penuh. Ini pemeriksaan token, belum pengujian layar hasil implementasi.

| Pasangan | Rasio |
|---|---:|
| text-primary / background | 16,01:1 |
| text-primary / surface-raised | 11,83:1 |
| text-secondary / surface-raised | 7,35:1 |
| text-muted / surface-raised | 5,28:1 |
| on-action / action | 9,80:1 |
| positive / positive-surface | 7,05:1 |
| negative / negative-surface | 6,23:1 |
| warning / warning-surface | 7,65:1 |
| information / information-surface | 7,45:1 |
| border-interactive / surface-raised | 3,99:1 |
| focus / surface-raised | 7,45:1 |

Teks biasa minimal 4,5:1; komponen/batas informatif dan grafik yang diperlukan untuk memahami konten minimal 3:1 terhadap warna yang bersebelahan. Ambang teks mengikuti [WCAG contrast minimum](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html) dan elemen nonteks mengikuti [non-text contrast](https://www.w3.org/WAI/WCAG22/Understanding/non-text-contrast.html). Tidak memakai ambang teks besar untuk membenarkan label kecil yang pucat.

### Tipografi

Keluarga utama: IBM Plex Sans, bobot 400 untuk isi, 500 untuk label/aksi, 600 untuk judul dan nominal penting. Gunakan hanya ketiga bobot tersebut, berkas lokal berlisensi OFL; tanpa CDN atau font request saat bermain. Fallback: sans sistem perangkat. Catat versi dan ukuran aset sebelum packaging. Jangan menambahkan keluarga Mono atau Serif hanya untuk memberi kesan terminal.

Alasan: satu keluarga menjaga ritme antara paragraf riset, tabel angka dan label singkat; bentuk karakter teknis sesuai ketelitian investasi, sementara bobot reguler tetap cocok untuk pesan klien. Keputusan ini tentang karakter dan konsistensi, bukan klaim hasil uji keterbacaan pada pengguna.

| Peran | Ukuran dasar / line height | Bobot | Penggunaan |
|---|---|---|---|
| caption | 12 / 16 | 400 | Timestamp/sumber sekunder; bukan label form/CTA |
| label | 14 / 20 | 500 | Tab, label, status |
| body | 16 / 24 | 400 | Isi, penjelasan risiko, form |
| body-strong | 16 / 24 | 600 | Total/baris penting |
| section | 18 / 24 | 600 | Judul bagian |
| title | 24 / 32 | 600 | Judul halaman |
| metric | 32 / 40 | 600 | Harga/NAV utama |
| display | 40 / 48 | 600 | Maksimal satu ringkasan di layar luas; turun ke metric pada mobile |

Ukuran adalah baseline logis: sp pada native dan rem berbasis ukuran default pengguna pada web. Tidak mengunci skala untuk mengejar pixel-perfect. Angka memakai lining/tabular figures bila tersedia pada aset font terpilih; periksa glyph angka, minus, persen, IDR/USD dan lebar digit pada native. Jika aset tidak mendukung alignment angka, gunakan fallback sistem untuk kolom numerik, tanpa menambah keluarga baru.

Nominal rupiah: “Rp 1.250.000”; USD: “US$ 1.250,50”; mata uang tetap ada pada ringkasan padat. Presisi berasal dari domain/instrumen. Tanda minus, jumlah unit, tanggal, dan persentase tidak dihapus saat ruang sempit. Pembulatan ringkasan seperti “juta” hanya untuk overview, dengan nominal penuh tersedia pada detail serta review transaksi.

### Spacing, radius, elevation dan ukuran

Spacing, radius, border, offset dan blur dinyatakan dalam unit layout logis: dp pada native atau CSS px pada web dengan pemetaan density yang diverifikasi. Shadow ditulis offset-x, offset-y, blur, lalu warna/alpha. Ini tidak menjadikan CSS px otomatis sama dengan dp untuk hitbox Android.

| Kelompok | Token dan nilai | Aturan |
|---|---|---|
| Spacing | 4, 8, 12, 16, 20, 24, 32, 40, 48, 64 | 4 untuk hubungan label; 8 antar-elemen terkait; 16 isi panel; 24 antar-section; 32 antar-blok naratif |
| Gutter halaman | 16 mobile; 12 hanya pada lebar 320–359; 24 tablet; 32 desktop | Konten tidak menyentuh tepi/safe area |
| Radius | 0 tabel; 4 badge; 8 input/tombol/panel; 16 dialog/sheet; pill hanya mode/status pendek | Hindari semua kartu menjadi pill atau radius berlebihan |
| Border | 1 normal; 2 selected; focus 3 + offset 3 | Focus tidak mengubah ukuran komponen |
| Shadow | level-0 none; level-1 0 2 8, hitam 16%; level-2 0 12 32, hitam 32% | Panel biasa level-0; sticky bar level-1 bila perlu; overlay level-2. Tanpa blur latar |
| Kontrol | target minimum setara 48 dp; tombol/form minimum tinggi 48; baris interaktif minimum 56 | Tinggi minimum boleh tumbuh karena font scale |
| Header | minimum 56; context strip minimum 40 | Membesar jika teks membungkus |
| Root navigation | minimum 80 + safe area | Lima label tetap terbaca |
| Action footer | minimum 72 + safe area | Hanya di layar detail/form, tanpa menumpuk root navigation |

## 4. Screen inventory

S01–S40 adalah cakupan desain. Satu screen berarti satu tujuan yang dapat dinavigasi; detail tertentu boleh berupa panel pada viewport luas. Layar tambahan memecah alur lama untuk mengurangi kepadatan, bukan menambah fitur produk di luar PRD.

| ID | Screen dan tujuan | Lokasi | FR |
|---|---|---|---|
| S01 | Mulai/lanjut: memilih progres yang akan dimainkan | Entry | FR-01, FR-19 |
| S02 | Permainan baru: memilih mode, dataset/periode dan konfigurasi yang sudah diputuskan | Entry | FR-01, FR-02 |
| S03 | Tutorial: mengerjakan satu pelajaran nyata | Beranda/detail | FR-07 |
| S04 | Beranda: menentukan prioritas trading dan karier hari ini | Root Beranda | FR-01, FR-07, FR-08 |
| S05 | Briefing: memahami konteks dan agenda sebelum sesi | Beranda/detail | FR-01, FR-09 |
| S06 | Pasar/watchlist: mencari serta memilih instrumen | Root Pasar | FR-02, FR-04 |
| S07 | Indeks: menelusuri indeks, metode dan anggotanya | Pasar/detail | FR-02 |
| S08 | Detail instrumen/terminal: memeriksa harga, grafik, antrean dan konteks | Pasar/detail | FR-02, FR-04, FR-10 |
| S09 | Tiket order: menyusun sisi, jumlah dan ketentuan order | Flow transaksi | FR-04, FR-15 |
| S10 | Review order: memeriksa estimasi dan akibat sebelum kirim | Flow transaksi | FR-04 |
| S11 | Hasil/detail order: mengetahui status, fill dan sisa yang dapat dibatalkan | Flow transaksi | FR-04 |
| S12 | Order aktif/riwayat: menemukan order berdasarkan akun dan status | Pasar/detail | FR-04 |
| S13 | Aset: memahami kas dan posisi akun yang dipilih | Root Aset | FR-05 |
| S14 | Detail posisi: memeriksa basis biaya, hasil dan kewajiban instrumen | Aset/detail | FR-05, FR-15 |
| S15 | Aktivitas/ledger: menelusuri perubahan uang dan aset | Aset/detail | FR-05 |
| S16 | FX: menukar IDR/USD dengan preview dan konfirmasi | Aset/detail | FR-06 |
| S17 | Laporan hari: memahami hasil, biaya dan langkah berikutnya | Beranda/detail | FR-01, FR-05, FR-07 |
| S18 | Karier/misi: mengetahui progres, syarat dan tugas berikutnya | Beranda/Firma/detail | FR-07 |
| S19 | Firma: menilai AUM, kewajiban klien dan pendapatan manajer | Root Firma | FR-08 |
| S20 | Klien/tawaran: meninjau hubungan aktif dan peluang mandat | Firma/detail | FR-08 |
| S21 | Mandat: memahami kontrak, batas dan status akun klien | Firma/detail | FR-08 |
| S22 | Laporan klien/arus dana/fee: menjelaskan hasil dan kewajiban mandat | Firma/detail | FR-08 |
| S23 | Fund builder: menyusun lalu meninjau rencana alokasi | Firma/detail | FR-11 |
| S24 | Multi-aset: memilih saham/obligasi/komoditas/FX beserta unitnya | Pasar/Aset/detail | FR-10 |
| S25 | Berita: memilih informasi yang relevan dan sudah dirilis | Root Riset | FR-03, FR-09 |
| S26 | Dokumen berita/riset: membaca laporan dan sumber serta menuju instrumen terkait | Riset/detail | FR-09 |
| S27 | Analis/data alternatif: menilai opini, rekam akurasi dan indikator | Riset/detail | FR-09 |
| S28 | Kalender: memahami agenda bursa, perusahaan dan kewajiban pribadi | Riset/detail | FR-01, FR-09 |
| S29 | Paket riset: membandingkan manfaat dan meninjau pembelian | Riset/detail | FR-09 |
| S30 | Langganan/billing: memahami periode, perpanjangan dan transaksi biaya | Riset/detail | FR-09 |
| S31 | Aksi korporasi: memahami hak dan menindaklanjuti aksi | Aset/detail | FR-12 |
| S32 | IPO/M&A: meninjau penawaran dan status pemesanan/tender | Pasar/detail | FR-13 |
| S33 | Voting: membaca resolusi, hak dan menyampaikan pilihan | Aset/Firma/detail | FR-14 |
| S34 | Risiko/compliance: memprioritaskan eksposur dan kejadian yang perlu tindakan | Firma/Aset/detail | FR-15 |
| S35 | Margin/pinjaman/short squeeze: memahami fasilitas, utang dan penanganan risiko | Aset/detail | FR-15 |
| S36 | Status halt/pembukaan: memahami pembatasan dan order terdampak | Pasar/detail/banner | FR-16 |
| S37 | Stress test: menguji skenario dan membaca dampaknya | Riset/detail | FR-17 |
| S38 | Lab/replay keputusan: membandingkan eksperimen terisolasi dan urutan peristiwa | Riset/detail | FR-17 |
| S39 | Replay historis: mengontrol periode dan menilai keputusan pada waktu data | Run historis/detail | FR-01, FR-17 |
| S40 | Pengaturan/save/pemulihan: menjaga aksesibilitas, data dan keberlanjutan progres | Header/detail | FR-18, FR-19, FR-20 |

Pemetaan 26 layar lama tersimpan di [SCREEN-COVERAGE.csv](../../Work/SCREEN-COVERAGE.csv). Profil analis berupa panel detail S27; inbox berupa daftar keputusan di S04 dengan detail pesan menuju screen tindakan. Tidak menambah login atau akun online.

## 5. User flow

### Aturan lintas journey

Root memakai lima menu: Beranda, Pasar, Aset, Firma, Riset. Setiap root mempertahankan posisi scroll, filter dan akun saat kembali. Detail memakai Back dan judul; footer aksi menggantikan bottom navigation. Context strip memuat mode, waktu game dan akun. Nama klien panjang boleh membungkus; nominal penting tidak dipotong.

Flow uang memakai susun -> review -> kirim -> status. Saat kirim, label menjadi “Memproses”; input dikunci sementara dan command ID dipertahankan. Jika hasil belum diketahui, tampilkan “Status belum terkonfirmasi” dan “Periksa status”, bukan membuat order baru lewat tombol Coba lagi. Bila gagal sebelum diterima domain, draft tetap dapat diperbaiki.

Back/Close tidak membatalkan command yang sudah diterima domain. Jika pemain meninggalkan screen saat hasil belum diketahui, status tetap dapat dibuka kembali dari riwayat menggunakan ID yang sama; tidak ada prompt yang mengklaim uang telah kembali hanya karena dialog ditutup.

Switch akun pada flow transaksi membatalkan preview dan mengembalikan ke tahap susun dengan pemberitahuan. Review tidak mengasumsikan akun baru punya hak yang sama. Mengubah mode berarti membuka run lain, bukan menukar label pada portofolio yang sama. Lab selalu memiliki label “Eksperimen — tidak mengubah permainan”.

| Journey | Langkah dan cabang utama |
|---|---|
| J01 mulai karier | S01 Baru -> S02 Pasar game -> konfigurasi domain yang tersedia -> review awal -> buat run -> S03 tutorial -> S05 briefing -> S04. Jika konfigurasi wajib belum ditetapkan, jelaskan bagian belum tersedia; jangan isi angka ekonomi dari mock. |
| J02 lanjut/pulih | S01 pilih run -> validasi save -> S04 sesuai checkpoint. Jika rusak -> S40 bandingkan backup/tanggal -> pratinjau pemulihan -> konfirmasi tanpa menghapus asli -> lanjut. Tidak otomatis membuat run baru. |
| J03 beli/jual | S06 cari/filter -> S08 periksa -> Buat order -> S09 pilih sisi/jenis/jumlah -> Tinjau order -> S10 baca akun/biaya/risiko -> Kirim -> S11. Jika pasar halt menuju S36; jika preview kedaluwarsa, perbarui estimasi dan ulang review. |
| J04 partial/cancel/stop | S12 pilih -> S11 lihat filled/remaining -> Batalkan sisa -> review jumlah yang masih bisa dibatalkan -> hasil domain. Atur stop/TP lewat S09 dengan trigger dan jenis lanjutan terlihat; gap tidak dijanjikan terisi pada trigger. |
| J05 aset dan rekonsiliasi | S13 pilih akun -> S14 posisi atau S15 aktivitas -> pilih fill/ledger -> S11/peristiwa terkait. Nominal kas/reserved/utang dijelaskan; kembali mempertahankan filter. |
| J06 FX | S13 -> S16 pilih akun/mata uang/jumlah -> hitung preview -> baca bid/ask/biaya/bersih -> konfirmasi -> receipt dan tautan S15. Quote habis -> perbarui; jangan otomatis mengirim dengan kurs berbeda. |
| J07 akhir hari | S04 lihat kewajiban -> Tinjau penutupan -> rangkum order/kewajiban yang dipengaruhi menurut aturan domain -> tutup sesi -> S17 hasil -> lanjut hari -> S05. Bukaan ulang laporan tidak mengulang reward/fee. |
| J08 karier dan pemulihan | S04 tugas -> S18 detail syarat -> S03 atau screen tugas -> hasil tercatat -> S18 klaim bila tersedia -> evaluasi promosi. Jika modal habis, S18 menjelaskan opsi recovery yang ditetapkan domain; bukan top-up tersembunyi. |
| J09 menerima klien | S19 -> S20 tawaran -> S21 baca tujuan/batas/tarif/likuiditas -> review -> terima -> pilih akun baru -> S13/S23. Jika belum memenuhi pangkat, tampilkan syarat dan tautan S18. |
| J10 kelola mandat | S19 pilih klien -> S21 status -> S13/S09 berlabel klien -> order sesuai mandat -> S22 performa/arus dana/fee -> tinjau dan kirim laporan. Penarikan membuka kebutuhan kas dan order terkait, bukan tombol penyelesaian instan. |
| J11 riset dan paket | S25 berita -> S26 sumber/waktu -> S08 instrumen. Riset terkunci -> S29 manfaat/harga -> review akun USD/periode -> langganan -> S30 receipt -> kembali ke dokumen. Pembatalan auto-renew menyebut konsekuensi sesuai aturan domain. |
| J12 fund plan dan multi-aset | S24 pilih kelas -> S08 ketentuan -> S23 pilih akun/target/kas -> evaluasi mandat/biaya -> tinjau order yang diusulkan -> konfirmasi pelaksanaan -> status per order. Simpan rencana bukan eksekusi; sebagian terisi tetap ditandai. |
| J13 aksi korporasi | S28/S13 hak -> S31 detail/eligibility -> untuk aksi otomatis baca receipt; untuk rights/buyback isi pilihan -> review akun/unit/biaya/tenggat -> konfirmasi -> status -> S15. Hak kedaluwarsa tidak menawarkan submit. |
| J14 IPO dan M&A | S32 pilih penawaran -> detail ketentuan -> isi pesanan/tender -> review dana/saham tertahan -> kirim -> status pemesanan -> alokasi/refund/settlement -> S15/S14. Tidak menampilkan alokasi pasti sebelum hasil domain. |
| J15 voting | S33 pilih agenda -> baca resolusi/hak/tenggat -> pilih setuju/tolak/abstain -> review jumlah hak dan akun -> kirim -> surat suara. Aturan mengubah suara belum diputuskan; UI tidak menawarkan perubahan jika domain tidak mengizinkan. |
| J16 margin/short dan risiko | S34 kejadian -> S35 rincian utang/jaminan/borrow -> untuk buka fasilitas atau pinjam, review kewajiban terlebih dahulu -> S09 order terkait -> pantau call/recall -> pilih tambah dana/tutup posisi jika diizinkan -> S11 dan S15. Saat halt, jelaskan kewajiban tetap ada. |
| J17 halt dan reopening | Banner S08/S04 -> S36 alasan/order terdampak -> tindakan yang diizinkan saja -> fase reopening -> hasil/antrean S11. Tidak ada countdown presisi jika domain hanya menentukan kondisi pembukaan. |
| J18 stress dan lab | S34/S27 -> S37 pilih snapshot/skenario -> jalankan -> dampak + asumsi -> S38 simpan/bandingkan eksperimen -> kembali ke run utama. Aksi transaksi eksperimen tidak tersedia pada akun produksi. |
| J19 historis | S01/S02 pilih Historis -> baca sumber/resolution/cakupan -> periode valid -> buat run terpisah -> S39 -> S06/S08/S09 sesuai kontrak replay -> S17 hasil -> lanjut atau keluar. Data belum ada menahan awal replay dan menawarkan kembali ke run pasar game, tidak menggantinya diam-diam. |
| J20 pengaturan dan save | Header -> S40 ubah aksesibilitas/mute/reduced motion atau simpan -> feedback terkonfirmasi. Impor/pemulihan -> pilih -> validasi -> ringkas dampak -> konfirmasi -> receipt. Back saat draft kotor menampilkan simpan draft/buang/tetap, tanpa menambah konfirmasi pada navigasi biasa. |

Untuk obyek otomatis seperti dividen, fee terjadwal, atau forced liquidation, CTA adalah melihat detail dan menangani tindakan yang memang tersedia; antarmuka tidak membuat tombol “klaim” yang mengubah aturan ekonomi.

## 6. Layout per screen

### Shell dan hierarki bersama

Root: header identitas halaman -> context strip -> prioritas/angka utama -> section kerja -> navigasi lima menu. Detail: Back + judul -> konteks -> isi vertikal -> action footer jika ada. Header minimum 56 dan context minimum 40; keduanya boleh tumbuh. Action footer minimum 72 menggantikan root navigation minimum 80. Jangan menumpuk keduanya. Isi memiliki ruang bawah setinggi footer aktual dan safe area.

Pada 360 x 800 logis, Beranda mengutamakan judul/waktu, akun/mode, satu ringkasan keuangan, satu agenda trading dan satu agenda karier sebelum daftar sekunder. Jika ada margin call, risiko menggantikan slot agenda teratas tanpa menghapus informasi akun. Saat ruang atau font scale membatasi, konten menggulir; tidak menjanjikan semua bagian selalu above the fold.

| ID | Urutan section dan hierarki | Komponen | Primary action |
|---|---|---|---|
| S01 | Judul -> run terakhir dengan mode/checkpoint -> run lain -> buat baru -> pemulihan | C02 C04 C05 C27 | Lanjutkan run terpilih; jika belum ada: Mulai permainan |
| S02 | Pilih mode setara -> sumber/periode/data readiness -> opsi yang sudah tersedia -> ringkasan | C04 C06 C08 C13 C27 | Buat permainan; disabled dengan alasan bila syarat belum tersedia |
| S03 | Satu tujuan -> penjelasan singkat -> tindakan/latihan -> bukti selesai -> langkah berikut | C16 C20 C05 C22 | Kerjakan langkah / Lanjut |
| S04 | Status hari -> konteks -> kekayaan pribadi; AUM dan fee terpisah -> dua agenda trading/karier setara -> inbox -> indeks ringkas | C01 C02 C03 C04 C10 C15 C16 | Lanjutkan prioritas hari ini, label spesifik menurut fase |
| S05 | Fase/tanggal -> ringkasan pasar -> peristiwa sudah publik -> tugas/risiko -> jadwal | C02 C04 C15 C19 C20 | Mulai sesi; syarat/ketidaktersediaan terlihat |
| S06 | Konteks -> pencarian -> tab Watchlist/Semua/Indeks -> filter bursa -> daftar harga berunit -> akses order aktif | C01 C03 C08 C09 C26 | Buka instrumen yang dipilih; tanpa CTA beli massal |
| S07 | Nama/metode/periode indeks -> nilai/perubahan -> grafik -> anggota/bobot -> catatan sumber | C10 C11 C12 C20 | Buka anggota; tombol kembali ke daftar indeks |
| S08 | Ticker/nama/bursa -> mode/akun -> harga/timestamp/status -> grafik -> tab Ringkasan/Antrean/Berita/Ketentuan -> posisi akun | C02 C03 C04 C10 C11 C12 C08 C19 | Buat order; watchlist sekunder |
| S09 | Akun/instrumen read-only -> sisi belum dipilih -> jenis -> jumlah/unit -> harga/trigger -> masa berlaku -> alasan jika perlu -> kas/risiko ringkas | C03 C06 C07 C08 C19 | Tinjau order |
| S10 | Judul tindakan -> akun/mode -> sisi/jumlah/harga -> estimasi fill/VWAP/fee -> kas/reserved/risiko -> waktu berlaku -> konsekuensi | C04 C13 C19 C05 | Kirim order; jika expired: Perbarui estimasi |
| S11 | Status domain sebagai judul -> order ID/akun -> filled/remaining/VWAP/fee -> timeline -> detail fill -> tindakan sisa | C14 C12 C15 C22 | Lihat posisi bila filled; Batalkan sisa bila masih terbuka |
| S12 | Akun -> status Aktif/Riwayat -> filter -> daftar sisi/status/jumlah/tanggal -> detail | C03 C08 C12 C26 | Buka order; tidak ada repeat order satu ketuk |
| S13 | Akun -> NAV dan timestamp -> kas available/reserved/utang -> posisi -> aktivitas -> pintu FX/aksi korporasi | C01 C03 C10 C12 C09 | Lihat posisi/aktivitas; FX sebagai pintu sekunder |
| S14 | Akun/instrumen -> jumlah dan nilai -> biaya perolehan -> realized/unrealized -> kewajiban -> histori -> riset | C03 C10 C12 C19 | Kelola posisi -> S09; sisi ditentukan pemain |
| S15 | Akun/periode -> ringkasan arus -> filter kategori -> ledger bertanggal -> hubungan fill/event | C03 C08 C12 C26 | Buka detail transaksi |
| S16 | Akun -> mata uang asal/tujuan -> jumlah -> kurs/timestamp -> preview fee/bersih -> review -> receipt | C06 C07 C10 C13 C14 | Tinjau konversi lalu Konfirmasi konversi |
| S17 | Hari dan akun -> NAV awal/akhir -> hasil investasi terpisah arus modal -> biaya -> kontribusi/risiko -> misi/klien -> agenda | C10 C12 C15 C16 C20 | Lanjut hari setelah kewajiban diperiksa |
| S18 | Pangkat/reputasi -> syarat berikutnya -> tab Tugas/Riwayat -> misi yang dapat dikerjakan -> penalti/recovery bila relevan | C16 C12 C22 | Kerjakan tugas prioritas; klaim hanya jika domain mengharuskan |
| S19 | Firma -> AUM terpisah kekayaan pribadi -> fee aktual -> kewajiban mendesak -> klien -> peluang -> pintu fund/compliance | C01 C10 C17 C15 C19 | Tinjau kewajiban; jika kosong: Lihat tawaran |
| S20 | Tab Aktif/Tawaran -> filter profil/status -> daftar trust/tenggat -> detail kontrak | C08 C17 C26 | Tinjau mandat, bukan Terima langsung |
| S21 | Klien/status -> tujuan/benchmark -> hard limit -> likuiditas/horizon -> tarif -> ketentuan -> review penerimaan atau akun aktif | C17 C19 C13 C20 | Tinjau penerimaan untuk tawaran; Kelola akun untuk aktif |
| S22 | Klien/periode -> tab Laporan/Arus dana/Fee -> NAV/benchmark -> kontribusi -> trust/compliance -> rincian kewajiban | C10 C12 C19 C20 | Tinjau/kirim laporan; tab lain memberi tindakan sesuai status |
| S23 | Akun/mandat -> target instrumen/kas -> input bobot -> total/selisih -> validasi -> estimasi biaya -> daftar order usulan | C07 C18 C19 C13 | Tinjau pelaksanaan; Simpan rencana sekunder |
| S24 | Kelas aset -> filter bursa/mata uang -> instrumen dengan unit/status -> ringkasan ketentuan -> akses detail S08 | C08 C09 C12 C20 | Buka instrumen, bukan memakai satu formulir semua kelas |
| S25 | Konteks waktu -> filter relevansi -> berita berjudul + waktu + sumber -> agenda pendek -> pintu analis/kalender/lab/paket | C01 C08 C15 C20 | Baca berita terpilih |
| S26 | Jenis dokumen -> judul/sumber/waktu tersedia -> ringkasan -> isi -> fakta/asumsi/ketidakpastian -> instrumen terkait | C20 C04 C19 | Buka instrumen; jika terkunci: Lihat akses |
| S27 | Tab Analis/Data alternatif -> cakupan/waktu -> daftar profil atau observasi -> detail/rekam prediksi -> metode | C08 C12 C20 C19 | Buka laporan/observasi; hindari rating bintang tanpa makna |
| S28 | Tanggal/bursa -> agenda harian -> jenis/status -> detail waktu/eligibility -> screen tindakan | C08 C15 C26 | Buka agenda; month grid hanya sekunder |
| S29 | Paket aktif -> perbandingan manfaat/baris -> harga/periode USD game -> saldo -> review renew/batas -> konfirmasi | C21 C12 C13 C19 | Tinjau paket; jangan memilih paket mahal otomatis |
| S30 | Status/berakhir/renew -> manfaat -> biaya berikut -> daftar billing/receipt -> pengaturan renew/cancel | C21 C12 C14 C24 | Kelola perpanjangan; batal dengan konsekuensi eksplisit |
| S31 | Akun -> tab Perlu tindakan/Otomatis/Riwayat -> aksi/eligibility/tenggat -> dampak unit/kas -> form jika perlu -> receipt | C15 C12 C07 C13 C14 | Tinjau tindakan; aksi otomatis: Lihat rincian |
| S32 | Jenis penawaran -> jadwal/ketentuan -> risiko -> akun/dana/saham eligible -> pesanan -> dana tertahan -> status/refund | C15 C20 C07 C13 C14 | Tinjau pesanan/tender; setelah kirim: Periksa status |
| S33 | Agenda -> resolusi -> akun/hak/tenggat -> pilihan tanpa default -> review -> surat suara/hasil | C20 C06 C13 C14 | Tinjau suara lalu Kirim suara |
| S34 | Akun -> kejadian prioritas -> eksposur/drawdown -> tab Risiko/Audit -> aturan/pemicu/bukti -> tindak lanjut | C19 C10 C12 C15 | Tangani kejadian prioritas; uji skenario sekunder |
| S35 | Akun/status fasilitas -> ekuitas/utang/jaminan -> call/recall -> tab Fasilitas/Pinjaman/Squeeze -> biaya dan posisi -> review tindakan | C19 C10 C12 C13 | Tinjau penanganan kewajiban; buka fasilitas hanya jika eligible |
| S36 | Instrumen/fase -> alasan halt -> waktu/kondisi pembukaan -> order/utang terdampak -> tindakan yang diizinkan | C19 C15 C12 C22 | Lihat order terdampak; tidak ada tombol paksa buka |
| S37 | Label eksperimen -> snapshot akun/waktu -> skenario + input -> asumsi -> hasil delta/nilai/risk -> perbandingan | C04 C07 C19 C10 C12 | Jalankan stress test; sesudah hasil: Simpan eksperimen |
| S38 | Label eksperimen -> daftar/snapshot -> parameter strategi -> hasil/risiko -> timeline replay keputusan -> bandingkan | C04 C12 C15 C26 | Jalankan/bandingkan eksperimen terpilih |
| S39 | Label Historis/periode -> manifest ringkas -> waktu berjalan -> kontrol sesuai kontrak waktu -> peristiwa tersedia -> portofolio/order run ini | C04 C27 C10 C15 C26 | Lanjutkan replay; tidak menambahkan kontrol waktu yang belum diputuskan |
| S40 | Tab Permainan/Aksesibilitas/Data -> checkpoint/save status -> opsi teks/motion/audio -> dataset -> validasi dan pemulihan | C28 C27 C14 C24 C22 | Simpan atau tinjau pemulihan pada section aktif |

### Susunan portrait untuk dua keputusan penting

Beranda: identitas hari -> mode dan akun -> kekayaan pribadi -> AUM/fee yang dilabeli terpisah -> prioritas trading -> prioritas karier -> inbox -> ringkasan indeks -> navigasi. Kedua agenda menggunakan ukuran judul dan bobot yang sama; keadaan mendesak menentukan urutan, bukan selalu trading.

Review order: “Tinjau pembelian/penjualan” -> akun dan mode -> ticker/unit -> jumlah dan harga -> estimasi fill serta biaya -> perubahan kas/posisi -> peringatan konkret -> “Estimasi pada [waktu]” -> tombol Kirim. Scroll tetap tersedia dan footer tidak menutupi rincian terakhir. Tidak perlu checkbox persetujuan generik yang tidak menambah pemahaman.

## 7. Component library

Library adalah kosakata desain reusable. Implementasi tidak wajib membuat satu class/component untuk setiap ID jika markup native dan token bersama sudah cukup.

State interaktif dasar: default, hover untuk pointer, focus-visible, pressed, selected bila sesuai, disabled dengan alasan, dan pending untuk command. State yang tidak relevan tidak dibuat hanya untuk melengkapi daftar.

| ID | Komponen | Variant | State dan perilaku |
|---|---|---|---|
| C01 | Navigasi utama | bottom mobile; rail tablet; sidebar desktop | current ditandai indikator + label; focus terlihat; badge hanya kebutuhan nyata; root tidak berubah saat konten loading |
| C02 | Header/context | root; detail dengan Back; flow transaksi | mode/waktu/akun tetap terbaca; judul panjang membungkus; tanpa akun sebelum run dipilih |
| C03 | Account selector | pribadi; klien; eksperimen; terkunci saat submit | expanded/selected/loading/error; menampilkan nama, jenis dan mata uang; perubahan membatalkan preview |
| C04 | Mode badge | Pasar game; Historis + tanggal; Eksperimen | state data tersedia/tidak tersedia; teks bukan warna saja; badge dapat membuka penjelasan sumber |
| C05 | Button | primary emas; secondary outline; tertiary teks; destructive outline negatif; icon berlabel aksesibel | default/hover/focus/pressed/disabled/pending; pending mempertahankan lebar dan command; tindakan ekonomi selalu label verba spesifik |
| C06 | Form field | teks; pilihan; radio group; tanggal; read-only | label persisten, helper, required, validasi inline, error terkait field; placeholder bukan label |
| C07 | Numeric field | uang; unit/lot; persen; trigger; stepper | locale-aware, unit tetap, nol beda kosong, min/max sesuai domain; validasi setelah input tanpa merusak draft; tersedia input langsung |
| C08 | Tabs/filter/search | tab konten; chip filter; search | tab selected/focus; filter aktif dapat dihapus; no results bukan error; chip 48 target walau bentuk visual kecil |
| C09 | Instrument row | watchlist; katalog; posisi terkait; hasil pencarian | aktif/pasif/halt/stale; ticker/bursa/unit dan harga bertimestamp; favorite tombol terpisah dari buka detail |
| C10 | Metric | harga; NAV; cash available/reserved; delta; utang | normal/gain/loss/stale/unavailable; nilai nol tidak dipakai untuk loading; delta bertanda dan basis waktu |
| C11 | Chart | line; candlestick; volume; benchmark | loading/empty/error/stale; tooltip dapat diakses keyboard; zoom/pan/reset berupa tombol; alternatif tabel OHLC/riwayat |
| C12 | Data list/table | book; posisi; ledger; laporan; histori | sortable jika relevan; selected/detail; empty/loading/error; header/unit eksplisit; kolom numeric rata kanan |
| C13 | Review summary | order; FX; mandat; paket; IPO; aksi; pinjaman; voting | ready/expired/blocked/submitting/unknown; selalu akun dan akibat, alasan blokir dekat CTA; tidak mengirim saat membuka review |
| C14 | Receipt/status timeline | order; conversion; entitlement; ballot; billing; save | accepted/pending/partial/filled/rejected/cancelled/recovered; ID/waktu/correlation tersedia; receipt bukan toast yang hilang |
| C15 | Event/inbox row | berita; agenda; permintaan klien; audit; peristiwa | unread/read/due/expired; waktu dan sumber; no future leak; aksi menuju konteks yang benar |
| C16 | Mission/career progress | tutorial; tugas; promosi; recovery | locked/available/in progress/completed/claimed; syarat eksplisit; tidak menampilkan progress persen palsu jika target belum ada |
| C17 | Client/mandate summary | tawaran; aktif; berakhir; withdrawal | trust disertai sebab/rentang; hard limit berbeda warning; detail kontrak sebelum acceptance |
| C18 | Allocation editor | target; aktual; perbandingan | sum valid/invalid, breach, draft/saved/executing/partial; angka input sebagai alternatif drag; tidak menggambar target seolah aktual |
| C19 | Risk/status panel | info; warning; violation; margin call; recall; halt | severity berlabel, sebab dan tindakan; tidak berubah jadi success hanya karena panel ditutup |
| C20 | Document/article | news; analyst; mandate; resolution; report | public/locked/missing/available; judul, byline, release time, fakta/asumsi/sumber; lebar baca dibatasi |
| C21 | Research plan/billing | comparison; active; renewal; cancelled | available/insufficient USD/pending/expired; biaya uang game, periode dan manfaat; tanpa rekomendasi upsell otomatis |
| C22 | Feedback/empty panel | empty; no results; info; error; success; offline-data-missing | satu judul spesifik, penjelasan singkat dan tindakan pemulihan; tidak mengganti seluruh app jika hanya satu section gagal |
| C23 | Loading/snapshot | skeleton statis; inline progress; refreshing existing | tidak ada shimmer kontinu; pakai layout mendekati isi; angka lama diberi waktu jika masih valid; aksesibel busy |
| C24 | Dialog/panel | modal pilihan singkat; detail sheet; confirmation; recovery | fokus terkelola, background inert, Close jelas; form transaksi panjang memakai screen penuh; tanpa swipe-only dismiss |
| C25 | Toast/noncritical status | watchlist; preferensi tersimpan; aksi ringan | pesan singkat dan live polite; tidak untuk error uang/save yang perlu keputusan; tidak mencuri fokus |
| C26 | History/time controls | pagination; agenda harian; replay; viewport chart | disabled dengan alasan di batas; tombol alternatif drag; langkah sesuai kontrak data/waktu; tidak memicu ekonomi di UI |
| C27 | Run/dataset card | existing run; internal; historical; manifest | valid/missing/corrupt/incompatible/loading; sumber/periode/resolution/gap terlihat; import tidak mengklaim valid sebelum pemeriksaan |
| C28 | Preferences/save setting | toggle; choice; save status; recovery | checked/unchecked/disabled/pending/error; konfirmasi hanya tindakan yang mengganti/menghapus state; perubahan aksesibilitas langsung terlihat |

Beli/jual dipilih lewat radio/segmented control berlabel. Keduanya setara sampai pemain memilih. Primary “Buat order” pada detail tidak membiasakan membeli. “Kirim order” adalah aksi eksplisit hanya pada review. Destructive style untuk membuang data/menutup fasilitas yang memiliki konsekuensi, bukan otomatis untuk jual saham.

## 8. State per screen

### Kontrak state bersama

Empty berarti tidak ada obyek yang sah; loading belum mengetahui isi; error gagal mendapatkan/menjalankan sesuatu; success adalah keadaan terkonfirmasi atau konten valid; offline hanya ketiadaan jaringan. Offline tidak sama dengan bridge mati, save corrupt, pasar tutup atau data historis tidak ada.

Game lokal yang datanya tersedia tetap dapat digunakan offline. Tidak menampilkan banner “sambungkan internet” di setiap layar. Jika ada kebutuhan data eksternal yang belum lokal, jelaskan “Data belum tersedia di perangkat” dan tahan hanya tindakan yang membutuhkan data tersebut. Tidak membuat antrean transaksi daring atau fallback mock.

Untuk command dengan outcome belum diketahui, tampilkan state terpisah “Status belum terkonfirmasi”; pertahankan ID dan sediakan Periksa status. Error yang pasti ditolak boleh dikoreksi dan diajukan sebagai permintaan baru secara sadar. Save gagal adalah banner persisten dengan status salinan terakhir, tidak hanya toast.

| Screen | Empty | Loading | Error | Success/valid | Offline |
|---|---|---|---|---|---|
| S01 | Belum ada progres; Mulai permainan | Memeriksa daftar checkpoint | Run rusak; Buka pemulihan | Run/tanggal/mode valid; Lanjutkan | Run lokal tetap tersedia |
| S02 | Histori belum ada; pilih sumber/periode yang tersedia | Validasi dataset/config | Jelaskan gap/format/aturan yang tidak cocok | Ringkasan valid; Buat permainan | Data lokal dapat dipilih; sumber belum lokal ditahan |
| S03 | Pelajaran belum tersedia untuk tahap ini | Membuka langkah dan progres | Langkah gagal tercatat; pertahankan latihan | Tujuan tercapai dari domain; Lanjut | Tutorial lokal berfungsi |
| S04 | Hari belum dimulai; buka briefing | Snapshot section, tanpa saldo nol palsu | Satu section gagal dengan Coba muat ulang | Prioritas dan angka terkonfirmasi | Beranda lokal tetap berjalan |
| S05 | Tidak ada berita rilis; agenda tetap terlihat | Mengambil briefing hari ini | Ringkasan gagal; sumber tersedia tetap dapat dibaca | Briefing dan syarat sesi valid | Briefing lokal tetap tersedia |
| S06 | Watchlist kosong: Cari saham; hasil nol: Ubah filter | Skeleton daftar; filter tetap | Daftar gagal; jangan isi harga contoh | Hasil aktif/pasif sesuai filter | Katalog lokal berjalan; data belum lokal diberi status |
| S07 | Indeks/anggota belum tersedia; jelaskan cakupan | Hitung/ambil seri dan anggota | Metode/data gagal dimuat | Metode, periode dan anggota terlihat | Indeks lokal berfungsi |
| S08 | Belum ada transaksi/riwayat; bukan harga nol | Placeholder grafik/quote terpisah | Harga/ketentuan tak tersedia; tahan order terkait | Harga/timestamp/source valid | Lokal tetap bisa; quote basi diberi waktu, bukan disebut live |
| S09 | Sisi/jumlah belum diisi; helper unit | Membaca aturan/reservasi | Inline lot/tick/saldo/mandat; draft utuh | Input valid; Tinjau order | Lokal berfungsi; bridge unavailable adalah error terpisah |
| S10 | Tidak ada preview sah; kembali ke tiket | Menghitung estimasi; Kirim terkunci | Preview invalid; jelaskan field/aturan | Ringkasan siap; Kirim order | Preview lokal valid bisa dipakai; usia quote tetap diperiksa |
| S11 | ID tidak ditemukan; kembali riwayat | Periksa status ID yang sama | Outcome unknown tidak disamakan rejected | Diterima/partial/filled/cancelled sesuai domain | Status lokal tetap dapat diperiksa |
| S12 | Belum ada order; Jelajahi pasar | Daftar akun terpilih | Riwayat gagal; tidak hilangkan data lama yang valid | Order/status tersedia | Riwayat lokal tersedia |
| S13 | Belum ada posisi; kas tetap terlihat | Hitung snapshot akun | NAV gagal; jangan tampil 0 atau gabung akun | Posisi/kas/reserved konsisten | Valuasi lokal bertimestamp tersedia |
| S14 | Posisi sudah tidak ada; buka aktivitas | Memuat basis biaya/kewajiban | Rincian tak cocok; tahan aksi dan muat ulang | Detail posisi dan histori valid | Detail lokal tersedia |
| S15 | Belum ada aktivitas untuk filter ini | Memuat halaman ledger | Ledger belum termuat; jangan membuat baris pengganti | Entri dapat ditelusuri | Ledger lokal tersedia |
| S16 | Jumlah belum diisi atau saldo sumber kosong | Preview kurs/fee | Quote expired/saldo kurang; draft tetap | Receipt dengan debit/kredit aktual | Konversi lokal berfungsi sesuai quote game |
| S17 | Sesi belum ditutup; menuju sesi | Menutup/menyusun hasil; cegah submit ulang | Status penutupan perlu diperiksa | Laporan final terpisah cashflow/PnL | Laporan lokal tersedia |
| S18 | Tidak ada tugas aktif; jelaskan syarat berikut | Membaca progres/reward | Klaim gagal; periksa status, bukan ulang bonus | Syarat/progres/pangkat sah | Progres lokal berfungsi |
| S19 | Belum ada klien; Lihat syarat/tawaran | Mengambil AUM/kewajiban | Jangan menyebut AUM 0 jika snapshot gagal | AUM/fee dan prioritas valid | Firma lokal tersedia |
| S20 | Tidak ada tawaran saat ini; syarat/agenda | Daftar aktif/tawaran | Daftar gagal; pertahankan pilihan | Tawaran/klien bertenggat | Hubungan lokal tersedia |
| S21 | Tawaran telah dicabut; alasan/domain | Memuat kontrak/hak | Mandat berubah; review ulang | Kontrak diterima/aktif dengan ID akun | Kontrak lokal dapat ditinjau |
| S22 | Belum ada periode laporan/fee | Menghitung report/settlement | Outcome pengiriman/fee diperiksa per ID | Laporan/fee terkonfirmasi sekali | Report lokal tersedia |
| S23 | Rencana baru kosong; Tambah instrumen | Evaluasi bobot/biaya/mandat | Total salah/breach; bobot tidak hilang | Rencana tersimpan atau eksekusi parsial eksplisit | Rencana lokal tetap dapat dikerjakan |
| S24 | Tidak ada instrumen kelas ini pada cakupan | Memuat katalog/ketentuan | Data unit tidak ada; tahan perdagangan | Instrumen dan unit tersedia | Katalog lokal sesuai data tersedia |
| S25 | Tidak ada berita relevan/rilis; ubah filter | Skeleton judul, bukan berita palsu | Feed gagal; dokumen tersimpan tetap ada | Artikel tersedia dengan waktu publik | Berita game/dokumen lokal tetap dibaca |
| S26 | Dokumen tidak tersedia pada waktu ini | Memuat isi dan sumber | Gagal/akses kedaluwarsa; kembali atau lihat paket | Isi sesuai entitlement dan release time | Dokumen lokal tersedia; dokumen belum lokal dijelaskan |
| S27 | Belum ada prediksi/observasi rilis | Memuat laporan/rekam akurasi | Jangan mengganti target dengan angka contoh | Observasi/akurasi dengan basis terlihat | Data lokal tersedia |
| S28 | Hari tanpa agenda; pilih tanggal lain | Memuat sesi/agenda tanggal | Kalender tak tercakup; tanggal tidak dianggap hari buka | Agenda publik dan kewajiban tersedia | Kalender lokal sesuai cakupan |
| S29 | Katalog paket belum tersedia; jangan buat harga | Memuat manfaat dan saldo | Saldo kurang/kontrak berubah; review ulang | Langganan diterima setelah settlement | Paket lokal dibeli dengan uang game |
| S30 | Belum pernah berlangganan; Lihat paket | Memuat tagihan/status | Pembatalan/billing unknown; Periksa status | Periode dan receipt sah | Billing lokal tetap tersedia |
| S31 | Tidak ada hak/aksi dalam periode | Memuat eligibility/settlement | Hak habis/dana kurang; jelaskan | Hak terselesaikan/otomatis dengan receipt | Ketentuan waktu game tetap berlaku offline |
| S32 | Tidak ada penawaran aktif | Memuat ketentuan/status alokasi | Pesanan berubah/berakhir; dana jangan dianggap terpotong | Pesanan/alokasi/refund bertahap jelas | Penawaran lokal berjalan sesuai waktu game |
| S33 | Tidak ada agenda/hak; alasan eligibility | Memuat snapshot hak | Hak berubah/tenggat lewat/duplikasi | Ballot terkonfirmasi; jumlah hak terlihat | Voting lokal sesuai aturan tenggat |
| S34 | Tidak ada pelanggaran; tampilkan basis pemeriksaan | Menghitung risiko/audit | Risiko belum terhitung bukan aman | Incident dan exposure terkonfirmasi | Pemeriksaan lokal tetap berjalan |
| S35 | Tidak ada pinjaman; jelaskan eligibility fasilitas | Memuat utang/jaminan/borrow | Borrow habis/call unresolved; tampilkan kewajiban | Pinjaman/repayment/likuidasi sesuai receipt | Offline tidak menghapus bunga/kewajiban sesuai clock |
| S36 | Tidak ada halt aktif; status pasar normal | Memeriksa fase terbaru | Fase belum diketahui; jangan membuka order otomatis | Status/reopening dengan aturan nyata | Fase lokal tetap berlaku |
| S37 | Belum ada snapshot/skenario dipilih | Menguji snapshot; label eksperimen tetap | Input invalid/perhitungan gagal; run utama aman | Hasil/asumsi/delta eksplisit | Stress lokal tersedia |
| S38 | Belum ada eksperimen; Buat eksperimen | Menjalankan/membandingkan | Eksperimen gagal tanpa mengubah run | Hasil dan versi parameter tersedia | Lab lokal tersedia |
| S39 | Dataset/periode tidak tersedia | Membaca manifest/bar berikut | Gap/incompatible; tahan progres terkait, jelaskan | Replay sesuai kontrak dan waktunya | Dataset lokal berjalan; kebutuhan data belum lokal ditahan |
| S40 | Belum ada backup/checkpoint; jelaskan | Validasi save/import/restore | Persisten: salinan valid terakhir dan pemulihan | Tersimpan/pulih dengan waktu/slot | Save/settings lokal tetap berfungsi |

Locked dan disabled berbeda dari error: tampilkan syarat pangkat, hak, paket, atau aturan yang belum terpenuhi dan tautan menuju penjelasan. Success finansial tidak menghilangkan receipt. Tidak ada UI loading tak terbatas tanpa jalan kembali/status yang dapat diperiksa.

## 9. Responsive behaviour

Android portrait adalah target rilis. Tablet dan desktop pada bagian ini mengatur layout lebar/preview/keyboard; tidak menambah kewajiban aplikasi Windows atau publikasi web.

| Lebar viewport logis | Tata letak | Navigasi dan detail |
|---|---|---|
| 320–599 | Satu kolom. Gutter 12 pada 320–359, kemudian 16. Form/detail penuh; grafik minimum tinggi 192 sebelum skala teks; dapat bertambah. | Root bottom nav 5 item; detail memakai Back dan action footer; drawer pilihan singkat, form uang penuh |
| 600–839 | Konten utama satu kolom maksimum 720 dengan gutter 24; ringkasan/agenda dapat dua kolom jika setiap kolom minimal 264 | Rail 80 hanya jika sisa konten memadai; bila tidak tetap bottom nav; detail finansial tetap satu alur |
| 840–1199 | Dua pane daftar/detail hanya untuk katalog, klien dan dokumen; pane minimum 280/400, gap 24 | Rail 80, judul/context di area isi; review transaksi di halaman/panel terfokus maksimum 560 |
| 1200 ke atas | Sidebar 216; area kerja maksimum 1280 dengan gutter 32; daftar/detail 320 + fleksibel; dokumen maksimum 680; form maksimum 560 | Sidebar berlabel; satu tiket/review aktif. Tidak ada beberapa panel order berdampingan |

Breakpoint adalah ruang konten, bukan deteksi merek perangkat. Jika font scale/zoom membuat pane tidak memenuhi minimum, kembali ke satu kolom. Tinggi tidak dipatok. Lebar desktop tidak memperbesar teks nominal tanpa batas atau membuat banyak kartu kosong.

Table prioritas mobile: instrumen -> jumlah/nilai -> delta/status, sisanya pada detail. Ledger menampilkan waktu/jenis/nominal lalu membuka rincian. Order book mempertahankan hubungan bid/ask, header dan unit; pada 320 atau font scale besar, tampilkan sisi Beli/Jual sebagai tab atau daftar vertikal, bukan angka yang dipadatkan. Tabel dua dimensi dapat memiliki scroll horizontal lokal bila memang esensial, dengan label/petunjuk dan alternatif detail; seluruh halaman tidak ikut bergeser.

Keyboard terbuka: field aktif dan error terlihat; footer mengikuti viewport yang benar atau masuk aliran halaman, bukan terjebak di bawah keyboard. Jangan mengecilkan font atau menonaktifkan zoom. Saat viewport sangat pendek, header sekunder dan footer boleh tidak sticky; konteks serta aksi tetap tersedia lewat scroll.

Pada foldable/window resize, pertahankan draft, akun, order ID dan posisi logis. Perubahan layout tidak mengirim perintah, mengulang preview tanpa alasan, atau mereset run. Tidak memaksa pengguna memutar HP untuk order book/chart.

Viewport desain/verifikasi: 320 x 640, 360 x 800, 412 x 915, 768 x 1024, 1024 x 1366, 1440 x 900; tambah kondisi keyboard dan font 200%. Ini ukuran logis untuk QA, bukan resolusi aset yang di-hardcode. Pemetaan ukuran sentuh ke dp tetap harus diuji pada Android.

## 10. Accessibility dan handoff

### Target dan kontras

Target desain adalah perilaku yang mendukung WCAG 2.2 AA, ditambah target sentuh produk minimum 48 dp. WCAG 2.5.8 menetapkan dasar minimum 24 x 24 CSS px dengan pengecualian; target 48 dp berasal dari requirement produk dan tidak disamakan otomatis dengan CSS px. [Sumber target size](https://www.w3.org/WAI/WCAG22/Understanding/target-size-minimum.html).

Gunakan rasio pasangan pada bagian 3, lalu verifikasi state hover/pressed/selected/error serta background nyata saat implementasi. Focus ring 3 unit dengan offset 3 wajib tetap jelas di atas panel dan tombol. Warna gain/loss disertai plus/minus, label serta nilai; chart mempunyai penjelasan dan alternatif data. Teks tidak diletakkan di atas gradient/foto.

### Navigasi dan focus order

Root: tautan Lewati ke konten untuk keyboard -> header/akun -> mode/waktu jika interaktif -> judul konten -> filter -> daftar/tindakan -> navigasi utama. Urutan DOM mengikuti urutan baca visual, tidak diubah hanya dengan posisi CSS. Satu H1 per screen, H2 untuk section. Perubahan halaman memindahkan fokus ke judul yang dapat difokuskan secara programatis dan mengumumkan judul singkat; jangan setiap snapshot pasar.

Detail: Back -> judul/konteks -> isi -> review/aksi. Form: akun -> sisi -> jenis -> jumlah -> harga/trigger -> masa berlaku/alasan -> helper/estimasi -> Tinjau. Pada error submit, fokus ke ringkasan error yang menaut ke field, atau field pertama jika hanya satu error. Setelah dialog ditutup, fokus kembali ke pemicu yang masih ada. Focus tidak boleh tertutup footer, sticky header, keyboard atau toast; desain menargetkan seluruh kontrol terlihat, lebih kuat dari minimum sebagian terlihat pada [Focus Not Obscured](https://www.w3.org/WAI/WCAG22/Understanding/focus-not-obscured-minimum.html).

### Semantik dan kebutuhan ARIA

- Gunakan elemen native button, link, input, label, fieldset/legend dan table terlebih dahulu. Jangan membuat seluruh screen menjadi role application.
- Navigasi memiliki nama “Navigasi utama”; item aktif memakai aria-current=page. Tabs memakai tablist/tab/tabpanel, selected dan relasi label yang benar; bukan sekadar lima tombol tanpa state.
- Account selector, filter dan disclosure mengumumkan aria-expanded serta relasi aria-controls bila menggunakan pola custom; gunakan select native jika mencukupi. Header tabel yang mengurutkan data mengumumkan aria-sort dan arah aktualnya.
- Setiap field memiliki label programatis dan unit; aria-describedby menghubungkan helper/error, aria-invalid hanya saat invalid. Required disampaikan dalam teks, bukan asterisk saja.
- Dialog memiliki accessible name, aria-modal, fokus awal yang tepat, background inert dan penahanan fokus. Dialog finansial panjang mulai pada judul/ringkasan; tombol berisiko bukan fokus/default Enter. Pola mengikuti [APG dialog](https://www.w3.org/WAI/ARIA/apg/patterns/dialog-modal/).
- Area sedang dimuat memakai aria-busy. Notifikasi ringan memakai status/live polite; rejection/error transaksi yang baru terjadi dapat memakai alert tanpa dibacakan berulang. Harga, jam dan ticker tidak menjadi live region per tick.
- Badge status mempunyai teks; ikon dekoratif aria-hidden. Icon-only Close/Back/Watchlist mempunyai nama spesifik, termasuk apakah saham sudah dipantau.
- Tabel memakai caption, header dan relasi sel. Jangan memakai ARIA grid untuk daftar biasa yang tidak membutuhkan navigasi spreadsheet. Book mengumumkan sisi, harga, unit dan jumlah.
- Grafik memiliki judul/ringkasan periode; tabel data dan kontrol navigasi tersedia. Tidak membuat ratusan candle masing-masing menjadi stop Tab.
- Angka dibacakan bersama mata uang/unit dan tanda untung/rugi. Label accessible tidak bertentangan dengan teks yang tampak.
- Jika host native dipilih, gunakan padanan accessibility API dan verifikasi TalkBack; ARIA pada dokumen web saja tidak membuktikan hasil native.

### Keyboard dan input alternatif

Tab/Shift+Tab berpindah antar kontrol; Enter/Space mengaktifkan tombol yang memang terfokus. Tabs menggunakan panah dan Home/End sesuai [APG tabs](https://www.w3.org/WAI/ARIA/apg/patterns/tabs/); aktivasi otomatis hanya jika panel sudah tersedia tanpa latensi. Radio menggunakan panah. Escape/Android Back menutup lapisan teratas; jika tidak ada lapisan kembali ke induk dengan draft terlindungi.

Tidak ada shortcut huruf tunggal default, Enter global untuk mengirim order, atau drag-only interaction. Chart punya zoom/pan/reset berbentuk tombol; alokasi bisa diketik; tooltip tersedia lewat fokus/tap. Hover bukan satu-satunya cara mengetahui biaya atau risiko. Kursor maupun keyboard tidak mengubah harga secara tidak sengaja melalui wheel pada input numerik.

### Teks, gerak dan pembacaan

Teks 200% tetap dapat digunakan tanpa kehilangan informasi/transaksi. Reflow diuji pada lebar 320 CSS px untuk halaman umum; tabel/grafik esensial ditangani dengan detail alternatif, bukan seluruh halaman melebar. Referensi: [Resize Text](https://www.w3.org/WAI/WCAG22/Understanding/resize-text.html) dan [Reflow](https://www.w3.org/WAI/WCAG22/Understanding/reflow.html). Tidak mengunci maximum-scale/user-scalable.

Hormati reduced motion dan preferensi teks sistem. Tidak ada flash harga atau animasi wajib. Audio bersifat opsional dengan mute; tidak ada informasi eksklusif suara. Isi pesan menjelaskan kesalahan dan pemulihan, misalnya “Dana tersedia tidak cukup setelah memperhitungkan order terbuka”, bukan “Error 400”. Bahasa dokumen disetel Indonesia; istilah asing singkat tetap dapat dijelaskan.

### Batas handoff dan pemeriksaan sebelum implementasi

Brief ini menetapkan desain, bukan mengonfirmasi hasil TalkBack, keyboard native, usability, render font atau APK. Pemeriksaan token dan cakupan dapat dilakukan sekarang; screenshot/browser/native dilakukan setelah implementasi dan dicatat terpisah.

Pada pemeriksaan akhir repo, folder android dan artefak smoke APK sudah muncul di luar pekerjaan penulisan brief ini. Tidak ada kode game yang dibuat atau diubah untuk menghasilkan brief. Urutan waktu seluruh pekerjaan lain tidak dapat dijamin; implementasi yang sudah ada harus diperiksa ulang terhadap brief sebelum dikembangkan lebih lanjut. Artefak tersebut tidak dianggap bukti kepatuhan desain.

Sebelum kode game: baca brief penuh, pastikan 40 tujuan screen terpetakan ke 26 cakupan lama dan 20 FR, pahami 20 journey serta state di bagian 8, dan catat pertanyaan produk yang memengaruhi isi. Tidak membutuhkan approval ulang untuk spacing/komponen rutin yang sudah ditetapkan di sini. OQ-20 tentang preferensi visual memiliki arah kerja pada brief ini; jangan menandai seluruh OQ sebagai terjawab.

Saat implementasi: terapkan token bersama; rekam screenshot Beranda, detail instrumen, tiket/review, firma/mandat dan laporan pada viewport wajib; lakukan pengujian transaksi, keyboard, TalkBack, font scale dan lifecycle secara nyata. Penyelesaian brief tidak meluluskan task implementasi; status Work mengikuti bukti pekerjaan agent yang menjalankannya. Tidak mengurangi scope demi menyamakan jumlah screen atau membuat semua komponen tampak selesai.
