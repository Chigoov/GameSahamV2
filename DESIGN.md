# Game Saham: arah UI/UX aktif

Mandat pemilik 22 Oktober 2026, pilihan warna dan navigasi dikonfirmasi di chat 23 Oktober 2026. Menggantikan keputusan visual/UX historis, bukan FR-01 sampai FR-20 atau aturan domain. Status dokumen: arah disetujui; implementasi dan bukti diverifikasi terpisah.

Design Read: game trading dan karier untuk pemain ponsel, bahasa visual terminal arang dengan aksen ungu tenang. ENERGY 2 / RHYTHM 2 / MOTION 2. Angka utama menjadi titik fokus, baris data membentuk ritme, transisi hanya memperjelas pergantian konteks.

## Referensi yang benar-benar dilihat

Delapan JPG dibuka satu per satu dari `Reference/` di root produksi. Path `Game Trading Saham/Reference` dalam mandat tidak ditemukan; tidak membaca source/save/build/credentials checkout lama. Hex dan ukuran berikut adalah interpretasi visual mendekati gambar JPEG, bukan token desain asli. Font dan motion tidak dapat dibuktikan dari screenshot: semua identifikasi keluarga font/motion diberi [ASUMSI]. Tidak menyalin logo, avatar, ilustrasi, merek, atau aset referensi.

| Referensi | Bedah visual | Diambil dan alasan | Sengaja tidak diambil dan alasan |
|---|---|---|---|
| `081b6b0ba1b6e8c60bcff6e7bcf7f8e5.jpg` | Navy hampir hitam sekitar #080E19; panel #111B2A; violet #6850F5; putih #F2F5FA. Sans netral [ASUMSI], judul lebih berat; jarak desktop sekitar 12-20px, radius 8-12px, elevasi tipis. Kartu ringkasan, daftar aktivitas, grafik luas; ikon garis kecil. Kepadatan tinggi. Motion tidak terlihat. | Hierarki nilai → grafik → rincian untuk trading; pengelompokan yang konsisten. | Sidebar desktop, banner promosi, tumpukan metrik dan ungu-biru glow: menghabiskan layar ponsel dan mengalahkan data. Logo/avatar tidak disalin. |
| `34c98bfef84f3db4a1b34bacefbbec6e.jpg` | Latar #081127; panel #252C48; cyan #70D4E6; hijau #87D282. Sans agak padat [ASUMSI]; nilai tebal, header besar. Spasi sekitar 16-24px; radius 6-12px; bayangan lebar. Susunan KPI → tren → tabel, ikon grafik kecil, kepadatan tinggi. Motion tidak diketahui. | Urutan ikhtisar → pola → detail; pemisahan angka dan label untuk laporan. | KPI/rating 4,6 dan angka contoh, panel 3D, grafik yang tidak disokong domain; bukan informasi game. |
| `5d34605b2a78921ea28ef63abeb55cda.jpg` | Slate #252B3A; panel #323848; aksen lilac #A48AEA dan pink #CB75D4. Sans kompak [ASUMSI], teks kecil pada lembar semua layar. Spasi rapat sekitar 8-16px; radius field membulat 12-24px, kartu 8-12px; elevasi rendah. Tab kategori, daftar berulang, header kembali, aksi bawah. Ikon garis; kepadatan tinggi. Motion tidak diketahui. | Konsistensi antar layar, kontrol kategori dan halaman detail yang bertahap. | Branding taruhan, pembayaran nyata, gamifikasi judi, gradien pink/ungu pada semua tombol, teks sekecil referensi. |
| `5f4aaea7dd0d88d3add1425e932f2bfc.jpg` | Hitam #080A0B; panel #171A1A; hijau mint #0EDDB1; violet #A56BE2 sekunder. Sans [ASUMSI]; saldo besar, daftar label/nilai. Spasi sekitar 12-20px; panel radius 12-16px, tombol bulat; elevasi datar. Grafik garis, selector waktu, daftar kepemilikan. Motion tidak diketahui. | Satu nilai dominan dan daftar kepemilikan terstruktur; grafik tidak ditimpa dekorasi. | Lima aksi hanya ikon, neon pada semua elemen, logo koin, saldo/return contoh, ruang grafik kosong yang menyerupai data. |
| `6382d992a4fd2afff82de7c2ea109a90.jpg` | Navy #04121B; panel #081C28; turquoise #00C9A8; teks #F3F7F8. Sans [ASUMSI], label kecil; spasi 16-24px, radius 12-16px, border tipis/elevasi rendah. Grafik candle+volume, tabel, alokasi. Ikon garis, informasi sangat padat. Motion tidak diketahui. | Fokus grafik, tanda laba/rugi, pemisahan volume dan harga, data rinci yang punya sumber. | AI signal/confidence/gauge palsu, logo kepala AI, sidebar lebar, banyak chart mini serentak dan glow. |
| `a74637bbfd8343d2009ff27249c779b1.jpg` | Navy #030D19; panel #081522; hijau #23B974; biru #216FE0; teks #F1F4F7. Sans [ASUMSI] dengan nilai tabular; spasi 12-20px; radius kecil 4-8px, permukaan datar. Grafik dominan, tabel performa, distribusi. Ikon garis. Kepadatan tinggi. Motion tidak diketahui. | Angka keuangan rapat tetapi sejajar; laba/rugi selalu bertanda; laporan mengikuti sumber. | Klaim trader pro/top 13%, jumlah trade/win rate tanpa metrik domain, komposisi landscape dan legenda pelangi. |
| `download (1).jpg` | Hitam plum #120F1B; panel #292330; ungu #873DB5; mint #50D2A2; kuning #F8E64D. Sans bulat [ASUMSI], saldo sangat besar; jarak 16-24px; radius 20-32px, tombol lingkaran; elevasi lembut/blur nav. Daftar aset, grafik garis dan dua aksi utama; kepadatan sedang. Motion tidak diketahui. | Ruang tenang di sekitar saldo, detail→grafik→aksi, aksen ungu sebagai penanda keputusan. | Background gradien, profil orang, logo kripto, lingkaran tanpa label dan angka desimal tidak konsisten. |
| `download.jpg` | Arang violet #14141F; panel #232332; violet #7041EF; mint #70E6BA; teks #F5F5F7. Sans geometris [ASUMSI]; saldo tebal, label lembut. Spasi 12-20px; kartu radius 18-24px, field 12px; elevasi rendah. Candle besar, filter waktu, dock beli/jual. Ikon garis geometris. Kepadatan sedang-tinggi. Motion tidak diketahui. | Arang+ungu, grafik dominan, aksi beli/jual berdampingan, pemisahan label/harga. | Badge Verified/Pro, avatar, yield contoh, bubble chart palsu, nav hanya ikon. |

## Tiga prinsip

1. **Angka terlebih dahulu.** Nilai, mata uang, tanda perubahan dan akun harus terbaca sebelum dekorasi; angka tidak boleh terpotong atau diganti demi komposisi.
2. **Satu keputusan per lapisan.** Daftar → detail → formulir → tinjauan → hasil; data sekunder berada dalam tab/details yang berlabel.
3. **Konteks tetap jelas.** Nama akun, waktu game, status pasar, asal simulasi dan konsekuensi tindakan tetap dapat dibaca; uang pribadi dan AUM tidak dicampur.

## Token dan alasan

| Token | Nilai | Alasan |
|---|---|---|
| Canvas | #101318 | Arang mengikat mood board terminal dan dompet tanpa pantulan glow. |
| Surface | #1A2028 | Permukaan solid memisahkan blok tanpa blur dan tumpukan bayangan. |
| Raised | #252D38 | Menandai kontrol/selected dan dialog, bukan semua kartu. |
| Teks utama | #F2F5F7 | Angka penting jelas pada tiga tingkat permukaan. |
| Teks sekunder/muted | #B6C0CE | Label kecil tetap kontras; bukan abu-abu samar. |
| Aksen | #B6A0FF | Ungu khusus pilihan aktif dan aksi utama, dipilih pemilik. |
| Teks pada aksen | #171225 | Aksi berisi ungu terang memerlukan teks gelap. |
| Positif | #65D6AD | Status untung/terbuka dengan tanda dan teks; bukan dekorasi bisnis. |
| Negatif | #FF8C99 | Rugi/gagal/jual dengan label; tidak bergantung hue saja. |
| Peringatan | #E8C27A | Tenggat/locked/stale; dibatasi pada status yang nyata. |
| Border kontrol/fokus | #8592A3 / #C7B8FF | Batas interaktif dan fokus dibedakan dari divider dekoratif. |
| Divider | #394350 | Pemisah baris internal yang tidak disalahartikan sebagai kontrol. |
| Typeface | IBM Plex Sans lokal, `assets/IBMPlexSans.ttf` | Sudah terpasang dengan `assets/OFL.txt`; detail huruf dan angka cocok untuk tabel keuangan. Tidak menambah font/dependency/CDN. |
| Skala type | .8125rem label; .9375rem isi; 1.125rem bagian; 1.5rem judul; 2.125rem nilai utama | Membentuk hierarki dalam portrait; rem mengikuti pengaturan teks 200%. |
| Angka | tabular-nums; nilai dan unit terpisah secara logis | Kolom harga tidak bergeser ketika quote berubah. |
| Spasi | 4, 8, 12, 16, 24, 32px | Jarak kecil menghubungkan label/nilai, jarak besar memisahkan keputusan. |
| Radius | 6px status; 10px input/tombol; 16px panel; 20px sheet | Bentuk membedakan kontrol dari data; bukan semua pill. |
| Elevasi | Tidak ada pada data; shadow hanya sheet/toast | Hanya lapisan yang benar-benar melayang perlu bayangan. |
| Motion | 120-180ms state/control; 180ms layer entry | Memperjelas interaksi; tanpa loop/pulse, tanpa animasi angka yang mengaburkan data. Reduced-motion dan hemat daya tetap berlaku. |

Seluruh pasangan warna harus dihitung saat verifikasi runtime, termasuk surface selected dan elemen inline. Tabel token bukan bukti kontras seluruh aplikasi.

## Komponen dan pola layar

Formulir memakai label yang terasosiasi dengan ID unik, termasuk beberapa penawaran IPO pada satu layar. Risiko menampilkan kewajiban short, jaminan, laba/rugi serta biaya dari snapshot Java; tombol membuka/menutup menggunakan bridge existing dan konfirmasi. Hasil yang belum terkonfirmasi menahan kedua tindakan sampai pemain memeriksa state ulang. Notifikasi hanya satu, maksimum40% tinggi viewport, dapat digulir dan ditutup; hasil finansial tetap tersedia di layar/ledger agar toast tidak menjadi satu-satunya catatan.

- Header: nama produk berupa teks, tombol Profil berlabel, kas akun nyata; tidak menciptakan logo/avatar baru. Konteks akun dan waktu di strip ringkas.
- Navigasi: empat tujuan berlabel dengan mark geometris sederhana dari SVG lokal existing yang disesuaikan; tidak impor icon pack. Label tetap makna utama.
- Ringkasan: kekayaan bersih dominan, kas/kewajiban ringkas, lalu misi berikutnya, jadwal dan berita/pantauan. Hibah adalah aksi sekunder, bukan iklan hero.
- Pasar: pencarian dan filter sebelum daftar; saham/reksa dana/produk lain serta berita/riset bisa dicapai dari kelompok yang berlabel. Baris ticker/nama di kiri, harga/perubahan di kanan, daftar wrap pada teks besar.
- Aset: akun dan nilai bersih, posisi, kas, komposisi, risiko, FX, pinjaman dan koleksi. Daftar dan data-row, tidak memaksa semua angka menjadi kartu KPI.
- Karier: pangkat/misi, bisnis operasional, tim, klien dan kelola dana. Detail perusahaan memakai tab yang wrap; staf dan modal tetap alur tinjauan.
- Detail instrumen: harga + status → grafik/timeframe → rincian/data sekunder → beli/jual; angka chart berasal dari history domain. Label sumbu dan tabel tersedia.
- Formulir keuangan: label terlihat, keyboard sesuai tipe, akun/unit/fee/risiko sebelum konfirmasi; kesalahan tidak menghapus input. Kontrol kirim tidak dipalsukan sebagai hasil final.
- Tinjauan/hasil: baris ringkas label/nilai; request/status aktual; bila pending/partial, tampilkan demikian. Back/fokus/inert/native close hook dipertahankan.
- Berita/riset: waktu publikasi domain, sumber skenario game dan emiten; tidak menampilkan timer atau badge live literal.
- Kosong: sebab dan tindakan pengisi yang nyata. Loading: nama data yang dimuat. Error: penyebab tersedia dan langkah coba lagi/kembali. Bridge tak tersedia menampilkan keterbatasan, bukan data keuangan contoh.
- Offline: WebView dan aset lokal tetap digunakan; putus jaringan bukan alasan memalsukan bridge/domain. Putus host QA hanya pengujian gangguan bridge, bukan bukti Android offline.
- Preferensi teks/kontras/motion/hemat daya tetap tersedia. Panel/CTA tumbuh mengikuti teks; minimal target 48px; long tables boleh scroll di container berlabel, halaman tidak overflow.

## Navigasi dan pemetaan seluruh cakupan

Pemilik menyetujui empat tab: **Ringkasan, Pasar, Aset, Karier**. ID internal `beranda`, `pasar`, `aset`, `firma` boleh dipertahankan untuk kontrak, `riset` menjadi halaman sekunder Pasar. Ini tidak menghapus riset atau bisnis. Profil/pengaturan di header. Alias rute lama diteruskan ke tujuan baru. Perubahan root harus diverifikasi bersama navigasi native Back/fokus.

| Cakupan lama / FR | Tujuan baru | Alur/layer yang tetap tersedia |
|---|---|---|
| 1 Dashboard global, FR-01/02/18 | Ringkasan | Kekayaan, waktu, pantauan, sesi, jadwal, laporan harian |
| 2 Indeks, FR-02 | Pasar | Indeks → anggota → detail emiten |
| 3 Detail emiten, FR-02/04/09 | Pasar → emiten | Harga, grafik/line/candle/tabel, timeframe, order book/tape, profil, fundamental |
| 4 Portofolio, FR-05 | Aset | Akun pribadi/klien, kas per mata uang, posisi → detail |
| 5 Tiket order, FR-04 | Pasar → emiten → Beli/Jual | Form → tinjauan → hasil; partial/cancel/retry/riwayat |
| 6 Berita/peristiwa, FR-03/09 | Pasar → Berita & riset | Feed → catatan; ringkasan berita tetap di Ringkasan |
| 7 Watchlist, FR-02 | Pasar | Filter pantauan, toggle emiten, simpan preferensi existing |
| 8 Misi/karier, FR-07 | Karier | Misi harian/mingguan/karier, klaim, pangkat, pemulihan |
| 9 Paket terminal, FR-09 | Pasar → Berita & riset | Paket → syarat/biaya USD → review → langganan |
| 10 Terminal pasar, FR-04 | Pasar → emiten | Grafik, antrean, tape dan order |
| 11 Riset emiten, FR-09 | Pasar → terminal | Inbox, konsensus, perbandingan, target dan akses karier |
| 12 Kalender, FR-01/09 | Pasar → Berita & riset | Agenda bursa/makro; jadwal sesi dari Ringkasan |
| 13 Langganan USD, FR-09 | Pasar → paket | Status berlaku/perpanjangan/biaya; dompet USD di Aset |
| 14 Penukaran IDR/USD, FR-06 | Aset → Tukar mata uang | Input → kuotasi → review → hasil |
| 15 Fund manager, FR-08 | Karier → Kelola dana | Firma, AUM, klien, fee dan akses jabatan |
| 16 Mandat, FR-08 | Karier → Kelola dana → klien | Tawaran/detail/terima, akun klien dan laporan |
| 17 Fund builder, FR-11 | Karier → Kelola dana | Alokasi → rencana → simpan/rebalance → hasil parsial |
| 18 Multi-aset, FR-10 | Aset → Multi-aset; pintasan Pasar | Obligasi/komoditas/deposito, quote beli/jual/cairkan |
| 19 IPO/M&A, FR-13 | Pasar → IPO / aksi korporasi | Jadwal/pemesanan/alokasi/refund dan tender |
| 20 Voting, FR-14 | Pasar → aksi korporasi | Agenda/hak suara → pilih → hasil |
| 21 Data alternatif, FR-09 | Pasar → terminal | Observasi/sumber/waktu/locked |
| 22 Compliance/audit, FR-08/15 | Aset → Risiko; Karier → laporan | Mandat, pelanggaran, ledger/riwayat transaksi |
| 23 Pulse/stress, FR-03/15/17 | Aset → Risiko & lab | Skenario stress → hasil; eksperimen lab terpisah |
| 24 Halt, FR-16 | Pasar → emiten/order | Status/alasan/buka kembali/larangan order dari domain |
| 25 Aksi korporasi, FR-12 | Pasar → aksi korporasi | Dividen/split/rights/buyback/riwayat |
| 26 Short squeeze, FR-15 | Aset → Risiko; Pasar → order | Margin/pinjaman saham/short, coverage/likuidasi/risiko |
| Reksa dana unit/NAB | Pasar/Aset → Reksa dana | Katalog → subscribe/redeem → antrean/cancel |
| Pinjaman tenor | Aset → Pinjaman; Karier → perusahaan | Produk/kolateral → quote/review/cair; cicilan/prepay/restrukturisasi |
| Bisnis/tim/modal | Karier → Usaha | Dirikan → detail ringkasan/tim/fasilitas/keuangan; review hire/train/assign/dismiss/upgrade/policy/capital |
| Koleksi/properti | Aset → Koleksi | Kategori/status/kepemilikan → beli/jual; valuasi dari domain |
| Liga/rival/peringkat | Karier | Kekayaan/return mingguan, peringkat aktual, rival |
| Profil/archetype/XP | Header → Profil; Karier | Kinerja/keuangan/pencapaian, gaya investor, nama/preferences |
| Era Baru | Karier → progres | Syarat → tinjauan → konfirmasi; aturan domain dipertahankan |
| Replay historis, FR-01/17 | Pasar → Riset & lab | Katalog provenance → mode/modal awal → resume; unavailable ditampilkan jujur |
| Tutorial/game baru, FR-07/19 | Profil/pengaturan; Ringkasan → panduan | Tutorial → konfigurasi → konfirmasi; tidak reset diam-diam |
| Save/load/recovery, FR-19 | Profil/pengaturan | Save/checkpoint/backup/recovery, dialog isolated wajib |
| Aset lokal/APK, FR-20 | Seluruh UI | Font/icon/CSS/JS lokal; domain/host Android dipertahankan |

Peta ini adalah spesifikasi jangkauan, bukan bukti gate. Denominator verifikasi di runner merinci root, flow, subtab, form, review, receipt dan state; tidak menganggap 26 konsep = seluruh layar.

## Asumsi dan batas

[ASUMSI] Pada teks 200%, header detail ditumpuk dan empat tujuan navigasi memakai dua baris jika label tidak muat. Nilai hero memakai ukuran berbasis lebar kontainer dengan minimum 1rem agar angka tetap utuh; label/isi tetap mengikuti pembesaran akar 200%. Unit dapat berada di baris tersendiri. Profil tidak menumpuk kartu nilai di dalam kartu identitas karena padding ganda mempersempit angka.

- [ASUMSI] Gaya font/motion referensi hanya dapat diinterpretasi dari JPG; memakai font existing dan transisi singkat beralasan, bukan mengklaim meniru animasi Pinterest.
- [ASUMSI] Dark tetap sebagai tema dasar karena delapan referensi dan sesi trading padat grafik; high-contrast existing tetap berfungsi. Tidak menambah tema terang yang belum diminta.
- [ASUMSI] Mark SVG existing boleh disederhanakan sebagai ikon fungsional, tanpa aset merek baru. Teks tujuan selalu terlihat.
- [ASUMSI] Label data-row yang merupakan hook live-sync dipertahankan sampai caller diperbarui bersama. Domain Java dan angka ekonomi tidak disentuh.
- Gambar referensi hanya dibaca; tidak dimasukkan APK atau commit. Bukti before/after dan log tes di `Artifacts/Redesign-Antislop-2026-10-23/`, laporan di `Work/`.
- Matriks wajib: 320×568, 360×640, 360×800, 390×844, 412×915, 430×932, 768×1024, 1440×900; teks normal/200%, reduced motion dan high contrast. Screenshot viewport plus konten scroll bila perlu; visual review tidak digantikan assertion root.
- Empat blok Delivery Gate: Hard Gate; Purpose-Gate; Liveliness; Craftsmanship & Quality Locks. Tidak menyatakan selesai apabila layar/state, tes, review atau bukti masih kurang.
