# Master plan eksekusi masukan pemain — Game Saham

**Versi:** keputusan navigasi dan Profil diperbarui, 28 Oktober 2026.  
**Status:** `PERSIAPAN_AKTIF — SEBAGIAN KEPUTUSAN MENUNGGU PEMILIK`.  
**Root produksi:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`.

Dokumen ini menggabungkan 21 anotasi browser pemilik, masukan tombol Beli sebelumnya dan rekomendasi yang telah disetujui. Masukan Beli sebelumnya sama dengan A02/K-025 dan hanya dihitung sekali. Rencana boleh dipakai untuk persiapan, inventaris, tes, perbaikan domain yang independen dan pembuatan preview. **Penerapan UI yang berubah hanya dilakukan setelah pemilik menyetujui preview perubahan tersebut.** Beberapa keputusan bertanda `MENUNGGU_JAWABAN` belum boleh diasumsikan.

## Instruksi utama pemilik: persetujuan seluruh perubahan UI

1. **Setiap perubahan UI/tampilan** wajib ditunjukkan dahulu kepada pemilik sebagai preview yang bisa dinilai, termasuk copy/warna/navigasi/form/grafik dan revisi atas preview sebelumnya. Galeri wajib memiliki **tiga alternatif**; cakupan preview bukan hanya galeri.
2. Minta jawaban **Question & Answer dengan pilihan klik**, misalnya `Setujui` / `Revisi` / `Lihat lagi`; sediakan isian opsional bila platform mendukung, tetapi jangan mewajibkan pemilik mengetik untuk memberi keputusan. Hasil pilihan harus eksplisit; pilihan default atau lewatnya waktu bukan persetujuan. Jika tidak tersedia alat pilihan, jelaskan keterbatasannya dan beri alternatif; jangan menganggap telah disetujui.
3. Setelah preview diajukan, **agent harus melanjutkan pekerjaan lain yang independen** sambil menunggu. Jangan berhenti seluruh proyek hanya karena satu preview belum disetujui. Tugas yang mengimplementasikan atau menetapkan UI tersebut tetap tertahan sampai disetujui. Apabila ditolak, revisi preview dan minta keputusan lagi; ulangi sampai disetujui.
4. Kelola status per item/preview dan catat tautan, versi, tanggapan pemilik serta dampaknya pada implementasi. Jangan mengubah source UI produksi lebih dahulu lalu meminta persetujuan belakangan; fixture/prototipe terpisah boleh dipakai untuk preview.
5. Jika ada ketentuan lama yang meminta berhenti menunggu plan berikutnya, instruksi pemilik terbaru ini memberi mandat menyiapkan/menjalankan **paket yang diberikan** dengan pekerjaan independen berlanjut; tidak memberi izin memulai pekerjaan lain di luar cakupan paket.

## Baseline dan sumber

- `Work/Master-Plan-Gabungan-2026-10-24/MASTER-PLAN-MASUKAN-PEMAIN-DRAFT.md`: anotasi awal, pertanyaan, rekomendasi dan observasi.
- `Work/Master-Plan-Gabungan-2026-10-24/DAFTAR-KEKURANGAN-LINTAS-PLAN.md`: temuan audit, termasuk K-024/K-018/K-002/K-013/K-005 dan K-025. Periksa status terkini sebelum mengerjakan; jangan menutup berdasarkan label lama.
- `Artifacts/Review-APK07-2026-10-27/REVIEW-REPORT.md`: bukti audit yang mendasari lima catatan teknis. Reproduksi pada snapshot terbaru, bukan menganggap bug pasti masih sama.
- `Artifacts/Analisis-UX-Preview-2026-10-27/ux-observations.json` dan screenshot: observasi 434×688 menggunakan host Java dan save QA baru. Bukan bukti Android fisik.
- `Artifacts/Collection-Art-150-2026-10-22/MANIFEST.csv` dan `images/`: pemeriksaan persiapan menemukan150 entri manifest dan150 PNG. Cocokkan ID, hash, atribusi dan kebutuhan performa dengan katalog produk; belum membuktikan integrasi tampil di APK.
- 628/628 regresi host dan APK-07 signature/aset yang sudah diperiksa adalah **hasil snapshot terdahulu**, bukan bukti perubahan berikutnya lulus.

## Peta keputusan pemilik per anotasi

| ID | Masalah dan keputusan pemilik | Hasil yang diperlukan dan batas |
|---|---|---|
| A01 | IPO: tampilkan **semua fase beserta kalender dan penjelasan singkat**. | Baca jadwal/phase dari domain, tampilkan waktu dan status saat ini dengan jelas; jika data fase tidak ada, beri label belum tersedia dan rencanakan domain yang diperlukan, jangan mengarang tanggal. |
| A02 / K-025 | Detail emiten: tombol **Beli dan Jual tetap terlihat di bawah layar**. | Keduanya langsung terlihat saat detail dibuka dan tetap terjangkau saat scroll; tidak menutupi bottom navigation/konten. Pertahankan alasan disable, eligibility dan review. Contoh BMRI, berlaku untuk semua detail emiten. |
| A03 | Form order: **kotak dialog ringkas di tengah layar**. | Preview dialog, adaptif pada layar pendek tanpa memotong kontrol penting; lihat R1/R2/R7. Tetap tersedia jenis order, harga, jumlah, validasi dan konfirmasi. |
| A04 | Tiap kategori Portofolio dibuka sebagai **tab navigasi baru di bar bawah**, tidak muncul menyempil di bawah. | Pemilik memilih **contoh B** sebagai dasar: saat kategori Saham & Order dibuka, tab Portofolio sementara berubah menjadi Saham; kembali ke Portofolio mengembalikan tab semula. Terapkan prinsip yang sama pada kategori lain setelah preview masing-masing; pertahankan navigasi kembali dan tab lain. |
| A05 | Informasi kategori terlalu panjang; **hapus penjelasan dan tampilkan nama fitur saja**. | Terapkan pada daftar kategori yang dimaksud; pertahankan informasi penting pada layar tujuan/konfirmasi jika diperlukan, jangan memuat jargon teknis mentah. |
| A06 | Replay historis **dipertahankan dengan penjelasan sederhana**. | Jelaskan sebagai simulasi dari data lampau, terpisah dari permainan utama; bar harian tidak merekonstruksi harga intraday. Gunakan provenance nyata, jangan menjanjikan data yang tidak ada. |
| A07 | Pengaturan & Sistem dipindah menjadi **menu tersendiri di dalam halaman Profil, bagian bawah**. | Bukan menambah bar Pengaturan baru. Padukan dengan A13 dan jangan kehilangan save/reset/panduan. Ikuti susunan Profil A yang dipilih pemilik; Pengaturan berada di bagian bawah. |
| A08 | Booster: **bedakan tiap jenis dan status**. | Pakai kombinasi warna/label/ikon yang dapat dibedakan tanpa warna saja, kontras sesuai standar. |
| A09 | Profil: pemilik menginginkan **semua** pilihan yang ditawarkan: grafik perkembangan kekayaan, hasil/statistik trading dan pencapaian. | Tentukan seri aktual dari domain/history; beri label waktu dan no-data/coverage bila tidak tersedia. Jangan menciptakan histori atau angka palsu; ajukan layout preview. |
| A10 | Riwayat order: **kartu ringkas dengan filter status**. | Tampilkan identitas saham, sisi, status, jumlah dan harga yang berguna; ID mentah diletakkan di detail bila perlu. Pertahankan filter, state kosong/error dan live update. |
| A11 | Status order harus dapat membuka **halaman Pasar dengan saham tersebut dipilih**. | Bawa identitas saham/akun yang benar, tidak mengirim order, dan rancang jalan kembali ke status order. |
| A12 | Pemilik menyetujui **hapus seluruh pengaturan ukuran teks buatan game**, tetap mengikuti ukuran teks bawaan perangkat. | Hapus kontrol/fitur pengaturan ukuran teks game dan migrasikan preferensi lama dengan aman; jangan menghapus skala sistem perangkat atau merusak save. Preview semua perubahan UI tetap wajib. |
| A13 | Ubah akses Pengaturan/save menjadi menu **Profil, Peraturan dan Panduan** dan pindahkan item terkait dari Lainnya. | Pemilik menolak susunan pertama dan memilih **Profil A** sebagai dasar: Profil/akun, Aktivitas Saya, Bantuan & Peraturan, lalu Pengaturan di bawah dengan Penyimpanan & Backup serta Mulai Permainan Baru. Pertahankan fungsi existing dan ajukan preview spesifik untuk perubahan lain. |
| A14 | Gunakan **150 aset koleksi yang sudah dibuat**. | Cocokkan150 ID katalog ↔ manifest ↔150 gambar, verifikasi file/hak/atribusi yang relevan dan integrasi build; periksa performa, fallback dan proporsi. Gambar bukan persetujuan layout. |
| A15 | UI kategori galeri tidak disukai; minta **tiga alternatif preview**. | Tampilkan tiga opsi yang menggunakan arah desain dan aset existing; Q&A `Setujui` / `Revisi`. Jika tidak disukai, ulangi. Belum boleh menetapkan/implementasi sebelum pilihan. |
| A16 | Pembiayaan disederhanakan: **game merekomendasikan opsi, pemain mengonfirmasi**, agunan dihapus, biaya keterlambatan **5% sederhana setiap24 jam nyata** dari **cicilan yang belum dibayar**. | Pemilik memilih denda sederhana dan memilih **utang lama tetap ada saat agunan dilepas**. Tetapkan prosedur aman dan aturan waktu rinci untuk preview/review; jangan menghapus saldo/utang/riwayat. Pilih produk dari kondisi aktual dan tampilkan alasan, jumlah bersih/biaya/tenor/denda sebelum konfirmasi. |
| A17 | Bahasa seluruh area Bisnis dibuat sederhana/profesional tanpa mengubah fitur. | Pemilik **menyetujui gaya contoh**: `Kapasitas AUM firma` → `Batas Dana yang Dapat Dikelola`; `Pemakaian kapasitas AUM` → `Dana yang Sudah Digunakan`; `Dana Kelolaan Klien` dipertahankan. Inventaris label terkait dan ajukan preview untuk tampilan lain. |
| A18 | Tambahkan **satu tombol Panduan di bagian atas Bisnis**. | Isi panduan ringkas untuk Usaha Saya, Koleksi dan Kelola Dana, sesuai domain. Tidak mengeksekusi aksi ekonomi. |
| A19 | Pemilih akun pindah ke Profil; pemilik meminta **akun hanya tampil di Profil**, tidak ada indikator di halaman lain. | Hormati permintaan, namun susun peta alur/preview dan uji bahwa setiap transaksi menggunakan akun terpilih, isolasi mandat tetap benar. Jangan diam-diam menambah indikator di halaman lain; risiko salah akun dijelaskan sebagai pertimbangan pemilik. |
| A20 | Nilai IDR tertahan dapat diketuk; pemilik memilih **rincian per order/transaksi beserta nominal/status dan tombol menuju detailnya**. | Hubungkan reservasi yang sesungguhnya dengan akun dan sumbernya; cek jumlah agregat dan state kosong, released, unknown; jangan menghitung/menebak tanpa bukti domain. |
| A21 | Dompet: satu baris **seluruh aset pribadi termasuk kas, saham, bisnis dan koleksi**. | Gunakan definisi domain, kurs dan waktu valuasi yang jelas; sertakan jenis aset lain yang termasuk aset pribadi, hindari hitung ganda dan pisahkan kewajiban/kekayaan bersih. Jangan menyertakan dana klien sebagai milik pribadi. |

## Rekomendasi reviewer dan keputusan pemilik

| ID | Keputusan | Isi yang direncanakan |
|---|---|---|
| R1 | DISETUJUI | Kas tersedia/tertahan dekat form order, dengan akses rincian alokasi (A20). |
| R2 | DISETUJUI | Form order bertahap: pilihan dasar dulu, pengaturan lanjutan dibuka bila perlu (A03). |
| R3 | DISETUJUI | Status order memakai bahasa pemain dan aksi selanjutnya (A10/A11). |
| R4 | DISETUJUI | Bedakan tidak ada data, nilai nol, dan gagal memuat. |
| R5 | TIDAK_DIPILIH | Usulan panduan berbasis tujuan tidak diotorisasi secara terpisah; A18/A13 tetap berlaku. |
| R6 | DISETUJUI | Status bursa dapat dijelaskan saat diketuk, berdasarkan data domain. |
| R7 | DISETUJUI | Review harga, biaya, potongan dan hasil bersih yang bersumber dari quote domain. |
| R8 | DISETUJUI | Pemilik menyetujui penanda konteks contoh `Harga simulasi game · 1 bulan` / `Data historis AAPL · Mei–Agustus 2016`; sumber dan periode harus nyata. |
| R9 | DISETUJUI | Hasil transaksi/sync belum pasti memiliki status dan cara memeriksa ulang; selaras K-018/K-024. |
| R10 | DISETUJUI | Pertahankan posisi/konteks saat kembali dari detail, order dan berita. |

## Keputusan yang masih diperlukan

Keputusan yang sudah dijawab pemilik (jangan memperluas persetujuan melampaui contoh yang dipilih):

1. R8 **disetujui** berdasarkan preview.
2. Susunan Profil pertama ditolak; pemilik kemudian memilih **Profil A**, dengan empat kelompok dan Penyimpanan & Backup serta Mulai Permainan Baru dalam Pengaturan di bagian bawah.
3. Gaya penyederhanaan istilah Bisnis A17 **disetujui** berdasarkan preview.
4. Pemilik memilih **utang lama tetap ada saat agunan dilepas**, dengan aturan baru; migrasi harus menjaga saldo dan tidak menghapus hak/progres. Tunjukkan rancangan sebelum perubahan UI.
5. Denda5% tiap24 jam nyata dari cicilan tertunggak dipilih **sederhana**, tanpa bunga atas denda sebelumnya.
6. A04: pemilik memilih **Portofolio B**, yaitu tab Portofolio sementara berubah menjadi tab kategori Saham saat dibuka dan kembali seperti semula ketika kembali.
7. A12 sudah dijawab: hapus pengaturan ukuran teks buatan game dan tetap mengikuti ukuran teks sistem perangkat.

Pada28 Oktober pemilik menjelaskan bahwa pertanyaan R8, susunan Profil, istilah Bisnis dan pilihan migrasi belum dapat diputuskan sebelum **melihat contoh UI**. Pemilik juga meminta contoh hitungan denda dan penjelasan visual untuk tab Portofolio. Jangan mencatat permintaan contoh tersebut sebagai jawaban persetujuan/penolakan. Preview dan pertanyaan lanjutan diberikan setelah contoh siap; pekerjaan independen tetap boleh berjalan.

Contoh terpisah sudah disiapkan di `Artifacts/Preview-Keputusan-Pemilik-2026-10-28/prototype.html`, preview lokal `http://127.0.0.1:18948/prototype.html`. Lima bagian: data/grafik R8, menu Profil, bahasa Bisnis, tiga ilustrasi tab Portofolio, dan contoh denda serta dua perlakuan pinjaman lama. Seluruh angka contoh berlabel ilustrasi, bukan data pemain; prototipe tidak mengubah game/save. Pemeriksaan browser434×850: lima tab contoh berfungsi, tanpa overflow horizontal dan pageerror; bukti `check-result.json` dan screenshot folder tersebut. Respons pemilik atas enam pertanyaan tersebut telah dicatat pada bagian keputusan di atas. Persetujuan contoh penjelas ini bukan otomatis persetujuan semua desain final; tiga alternatif galeri tetap harus dibuat dan disetujui secara terpisah.

Keputusan lain yang belum dijelaskan, seperti tanggal mulai denda, pecahan24 jam, cicilan parsial, cap dan perbedaan UTC/jam mundur, dirinci dalam desain untuk diperlihatkan; jangan diam-diam membuat tarif baru atau mengasumsikan pengguna menerima compounding.

## Masukan tambahan preview 28 Oktober 2026

- Dana tertahan di Beranda: pemilik memilih **A — tulisan “Tertahan” tanpa panah**. Seluruh area tetap bisa diketuk untuk membuka rincian dana; jangan menghapus konteks aksesibilitasnya.
- Tombol kiri atas: pemilik memilih **A — avatar bulat** sebagai akses Profil, menggantikan tombol G/Pengaturan. Ini persetujuan atas contoh yang ditampilkan, bukan semua perubahan terkait Profil.
- Detail indeks: pemilik memilih **A — kartu ringkas** sebagai dasar. Informasi yang belum tersedia tidak boleh direka; informasi tambahan tetap dapat diakses.
- **Warna dasar: D — Biru Samudra**, dipilih pemilik melalui Question & Answer setelah melihat preview halaman Beranda. Tiga contoh pertama (hijau terang, biru gelap, navy hangat) ditolak. Acuan prototipe `Artifacts/Preview-Warna-Dan-Navigasi-2026-10-28/prototype-controls.js`, parameter `?variant=samudra`, dan gambar `pilihan-warna-baru.png`. Pilihan ini menyetujui arah warna contoh yang ditampilkan, bukan otomatis menyetujui seluruh layar dan state; tunjukkan preview untuk perubahan UI lain atau penyesuaian desain yang belum ditinjau. Pastikan kontras, status, grafik, fokus dan keterbacaan pada seluruh layar sebelum menerapkan tiap preview yang telah disetujui.

## Paket kerja dan dependensi untuk agent

**M0 — Persiapan dan inventaris.** Pastikan root/branch/delta, baca AGENTS.md/DESIGN.md, seluruh plan awal dan ledger. Petakan 21 anotasi → file, fungsi domain/UI, caller, data dan pengujian. Peta kerja disimpan dalam output paket, bukan membuat tracker yang menimpa tracker126 task. Tandai kebutuhan yang belum diputuskan.

**M1 — Perlindungan data dan defect independen.** Reproduksi K-024 sync failure dan K-018 transaksi unknown/reload, serta K-005 generator/laporan. Susun perbaikan domain/tes yang tidak bergantung pada preview; feedback UI yang berubah tetap perlu preview. Pertahankan K-022 backup, quote atomic dan 24x. Hasil baru wajib diverifikasi pada source terkini.

Contoh interaktif lanjutan tersedia di `Artifacts/Preview-Keputusan-Pemilik-2026-10-28/navigasi-interaktif.html` (lokal: `http://127.0.0.1:18948/navigasi-interaktif.html`): tiga alternatif navigasi Portofolio dan tiga susunan Profil. Melalui Question & Answer, pemilik memilih Portofolio B dan Profil A sebagai dasar. Pilihan ini menyetujui arah contoh yang ditampilkan; preview spesifik tetap wajib untuk setiap perubahan UI lain atau revisi desain. Simulasi tidak terhubung ke data game. Enam alternatif dan navigasi dasar telah diperiksa pada lebar 320 dan 434 piksel.

**M2 — Preview desain dan navigasi.** Buat tiga alternatif galeri dengan aset 150 dan preview setiap perubahan UI lain yang terpengaruh. Sertakan state normal/locked/empty/error serta viewport kecil. Gunakan Portofolio B dan Profil A sebagai dasar peta navigasi sesuai keputusan A04/A07/A13; tunjukkan preview untuk kategori dan perubahan UI lain serta pergerakan akun A19. Perubahan UI per komponen baru boleh diterapkan setelah preview spesifik disetujui. Saat menunggu, kerjakan M1, inventaris, model domain/tes yang independen, dan dokumentasi.

**M3 — Transaksi & Pasar.** Implementasikan UI A02/A03/A10/A11/A20 dan R1/R2/R3/R7/R9/R10 sesuai preview. Pertahankan ID intent yang stabil, quote/capacity domain, idempotensi, account isolation, arus Back dan lifecycle; uji tanpa transaksi pada save pemain. Tampilkan rencana sumber alokasi dana tertahan berdasarkan ledger/order/reservasi aktual.

**M4 — IPO, replay dan profil.** Implementasikan A01/A06/A08/A09 dan R4/R6/R8 hanya jika disetujui. Gunakan kalender fase/data histori aktual, grafik hanya dari data tersedia, pengujian cakupan dan state kosong; jangan mengisi tanggal/histori dengan contoh palsu.

**M5 — Bisnis & koleksi.** Terapkan aset150 setelah mapping jelas dan UI disetujui; selesaikan A14/A15/A17/A18, serta A21. Penyederhanaan teks mengikuti contoh yang pemilik setujui; ubah presentasi tanpa mengubah fitur/aturan bisnis. Uji grafik/aset, loading/fallback, file/APK dan performa.

**M6 — Pembiayaan.** Tetapkan kontrak domain dan migrasi sesuai pilihan pemilik: agunan dilepas, sisa utang lama tetap ada, aturan baru diterapkan; denda sederhana5% tiap24 jam nyata dari cicilan tertunggak. Rinci pecahan24 jam, pembayaran parsial, anchor dan komponen lain untuk review jika belum ditentukan. Uji batas24 jam, cicilan parsial, waktu mundur, restart/offline, idempotensi, data lama, pelepasan agunan, saldo dan ledger. Jangan menghapus utang atau riwayat.

**M7 — Integrasi dan penyerahan.** Setelah seluruh UI masing-masing disetujui dan source stabil, selesaikan K-002/K-013 bukti state/modal/viewport dan K-005 dokumentasi. Jalankan tes relevan untuk semua tugas, regresi penuh fresh384MB termasuk730 hari, lalu kandidat APK baru jika source berubah. Cocokkan source/asset/identitas/signature/hash, dokumentasikan jumlah tes aktual dan hasil/exit. Kegagalan atau bukti kurang tetap terbuka; pertahankan histori audit.

Urutan di atas adalah dependensi, bukan larangan kerja independen secara berurutan. Jangan mengubah ekonomi/navigasi yang keputusannya belum diperoleh; jangan mengerjakan UI produk sebelum preview masing-masing disetujui.

## Penerimaan dan perlindungan

- Per item: tautan anotasi, keputusan pemilik, preview dan persetujuan, perubahan konkret, uji positif/negatif, state, screenshot serta bukti output. Laporkan status `MENUNGGU_JAWABAN`, `MENUNGGU_PREVIEW`, `MENUNGGU_PERSETUJUAN`, `DALAM_PEKERJAAN`, `TERUJI` atau `TERHAMBAT` dengan alasan.
- UI:10 viewport wajib320×568,360×640,360×800,390×844,412×915,430×932,768×1024,1440×900,470×658,462×648 dan 434×688 contoh pemilik. Uji skala sistem 100%/200% yang relevan walau kontrol game A12 dihapus, sentuh48×48, kontras teks normal4.5:1/teks besar3:1, keyboard/fokus/Back/scroll dan reduced motion. Pastikan fixed Beli/Jual tidak menutupi konten atau bar bawah.
- Kinerja: P95 aksi lokal<=300ms, navigasi<=1000ms, feedback kerja berat<=300ms, minimal30 sampel mentah per jenis/rute yang diklaim; ukur operasi aktual dan feedback baru. Beri label host, bukan benchmark Android.
- Finansial: quote review=transaksi atau minta review ulang, requestID stabil untuk hasil tak pasti, retry idempoten, isolasi akun, saldo/reservasi/ledger sesuai domain. Denda dan migrasi baru diimplementasikan hanya setelah keputusan terbuka dipastikan.
- Lindungi save pemain `Artifacts/LocalPreview/preview-save/save_game_v2.json`: hash-only, semua percobaan pakai save QA baru. Pertahankan preview pengguna, dirty checkout, histori dan aset. Jangan reset/clean/stash, ADB/instalasi, push/publish, menjalankan agen lain, mengirim pesan lintas chat, atau mengubah pengaturan global tanpa mandat baru.
- APK debug, pengujian host/browser, Gradle dan native/Android fisik dilaporkan terpisah. Tidak menyatakan fisik lulus dari preview; tidak menyatakan proyek100% selesai saat keputusan/UI/gate belum selesai.

## Penyampaian kepada pemilik

Sebelum menugaskan agent untuk penerapan, perbarui keputusan `MENUNGGU_JAWABAN` dari respons pemilik yang sebenarnya, tandai versi rencana, dan sertakan ringkasan pekerjaan independen yang boleh lanjut sementara approval UI menunggu. Pemilik tidak perlu mengetik untuk menyetujui/menolak preview jika Question & Answer tersedia.
