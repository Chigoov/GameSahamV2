# Master Plan Game Saham Offline dan Realisme

**Status: DIPERBARUI SETELAH TINJAUAN PEMILIK — PAKET DAN KEPUTUSAN MASIH PERLU DITUNJUK; BELUM ADA IZIN EKSEKUSI OTOMATIS**  
**Tanggal:** 28 Oktober 2026  
**Proyek:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`

## Prioritas pelaksanaan terbaru setelah review APK-14

**Rencana pekerjaan yang masih perlu dibuktikan atau direvisi:** `Work/Master-Plan-Gabungan-2026-10-24/Plans/12-revisi-apk14-dan-keputusan-terbaru/plan.md`. Rencana itu memetakan **8 prioritas review dan 16 rekomendasi** menjadi 24 butir audit/penyelesaian, bukan 24 fitur baru. Gunakan hanya saat ditunjuk pemilik. Pekerjaan sebelumnya yang terbukti benar tetap menjadi baseline; setiap butir mulai dengan memeriksa bukti terkini, dan hanya gap yang diperbaiki. Semua butir harus menyatakan status dan bukti aktual; tidak mengklaim seluruhnya sudah dikerjakan atau belum ada.

**Aturan pembacaan:** keputusan pemilik paling baru di bagian 2A dan plan 12 mengungguli kalimat lama yang bertentangan. Bagian P00–P09, checklist, tinjauan dan pertanyaan di bawah mempertahankan riwayat/rincian, bukan instruksi mengulang paket yang sudah benar atau menyatakan status terbaru. Khususnya, 09.00–16.00 tiap hari game sudah dipilih; untuk kabar Pro dan Whale yang memenuhi syarat, kenaikan harga penutupan dijamin sesuai keputusan terbaru, sedangkan berita umum tidak otomatis dijamin dan laba bersih pemain tidak dijamin. Bagian lama yang menyebut jam belum dipilih atau semua dampak harga tidak dijamin adalah **catatan historis yang digantikan secara terbatas**.

**Jawaban tindak lanjut terbaru:** order IPO baru ditolak sesudah penawaran tutup; profil menunjukkan ranking kekayaan dan return dengan label masing-masing; contoh order 300 saham boleh melewati antrean 100 dan terisi dari penjual 300 pada harga cocok; pemilik menyetujui preview terminal satu pintu sebagai dasar desain, dengan Kelola Paket di dalam Terminal Riset (`Artifacts/Preview-Pro-Tycoon-2026-10-28/terminal-satu-pintu.png` dan `terminal-satu-pintu-paket.png`); approval detail implementasi tetap diperlukan; Pro dan Whale dihitung dari harga bursa buka dan harus sesuai persentase pada penutupan (Whale >20%); urutan preview Tycoon Beranda, Pasar, Portofolio, Bisnis, Lainnya; hasil komputer, browser, APK, dan ponsel dilaporkan terpisah. Keputusan ini tidak otomatis menyetujui detail UI atau jawaban untuk kasus lain. 16 rekomendasi R01–R16 dipetakan satu per satu dalam plan 12 dengan instruksi, uji, ketergantungan dan status yang perlu dibuktikan.

**Lima keputusan terakhir pemilik (28 Oktober 2026):** kabar frekuensinya sedang dan ada setiap pembukaan pasar; paket diperpanjang otomatis jika saldo cukup; penawaran IPO tiga hari game; selama menunggu persetujuan UI agent melanjutkan tugas independen; agent melaporkan setiap layar atau perubahan UI yang siap ditinjau. Angka jumlah kabar, aturan saldo kurang, jadwal fase mendetail dan persetujuan UI tetap memerlukan rincian yang belum diberikan.

## 1. Tujuan

Menyiapkan game saham Android offline yang stabil untuk diuji, mempertahankan progres pemain, dan meningkatkan hubungan antara pasar, berita, keputusan bisnis, dan ekonomi. Game harus tetap nyaman dimainkan tanpa pemain menunggu lama tanpa kegiatan yang berarti.

Dokumen ini adalah bahan tinjauan dan pengujian rencana. Jangan mengubah game hanya karena file ini ada. Pelaksanaan mengikuti instruksi pemilik dan aturan pengiriman satu plan pada satu waktu dalam `AGENTS.md`. Minta keputusan pemilik untuk bagian yang masih berupa pilihan. Jangan mengubah tracker lama atau menyatakan rencana lama selesai berdasarkan dokumen ini.

## 2. Keputusan dan batas yang sudah diketahui

| ID | Keputusan / batas | Status |
|---|---|---|
| K01 | Game ditujukan untuk Android dan dapat dimainkan offline. | Arah produk |
| K02 | Hapus fitur replay historis karena tidak memengaruhi pasar utama. | Diputuskan pemilik pada 28 Oktober 2026 melalui Question & Answer setelah perbandingan; menggantikan keputusan lama mempertahankan replay. Penanganan save replay lama tetap menunggu D06; pertahankan grafik pasar utama dan komponen bersama, dan ajukan preview perubahan UI. |
| K03 | Tutorial wajib pembelian saham pertama tidak ditambahkan. | Usulan tutorial wajib dicatat ditolak; panduan sukarela existing tetap ada. |
| K04 | Hapus penanda “harga simulasi game” pada pasar utama. | Dipilih pemilik setelah pertanyaan lanjutan, menggantikan persetujuan sebelumnya. Ajukan preview perubahan UI; jangan menghapus informasi lain di luar cakupan. |
| K05 | Daftar persiapan rilis, pengujian offline/perangkat, dan perbaikan peringatan save disetujui secara prinsip. | Disetujui; ikuti batas akses, pelaksanaan, dan pengujian perangkat di bawah. |
| K06 | Satu hari kalender game = satu jam nyata, rasio 24x sama pada aktif, background dan saat aplikasi ditutup; dunia otomatis tanpa kontrol pause, speed atau skip oleh pemain. | Pertahankan keputusan aktif dalam `AGENTS.md`. |
| K07 | Efek berita 60/120 menit nyata dan tenggatnya harus persisten. | Pertahankan; jangan mengubah tanpa persetujuan pemilik. |
| K08 | Tarif, hadiah, formula ekonomi, desain visual dan navigasi yang berlaku tidak diubah diam-diam. | Setiap perubahan aturan atau tampilan memerlukan keputusan sesuai instruksi proyek. |

## 2A. Penyelarasan keputusan, hasil review dan persiapan rilis

**Arahan batas bursa terbaru 29 Oktober, menunggu klarifikasi:** pemilik menyatakan “untuk kenaikan 80% dan penuruan harian buat aturan maksimal 40%, sehingga kejadian 20% ini masih normal”. Ditanyakan apakah ini berarti +80%/−40% per hari game dari harga penutupan sebelumnya untuk semua saham atau hanya saham dengan berita Whale. Jangan tetapkan cakupan, acuan, atau angka di kode sebelum jawaban.

**Keputusan batas harga lanjutan, 29 Oktober:** pada contoh rumor lanjutan 42% dengan acuan Rp10.000 dan batas Rp12.000, pemilik memilih mengizinkan batas bursa dilampaui khusus untuk kejadian tersebut agar target bisa tercapai. Cakupan pengecualian untuk berita Whale biasa sedang diklarifikasi; jangan perluas otomatis. Konflik dua target yang sama ditangani dengan menjadwalkan berita kedua ke sesi lain. Ini keputusan, bukan bukti implementasi.

**Keputusan baru dalam chat 29 Oktober 2026:** jika dua berita berbayar untuk emiten yang sama memiliki target berbeda pada penutupan yang sama, jangan jadwalkan target yang bertentangan; berita kedua menunggu sesi lain. Batas bursa yang membuat target Whale tidak dapat terpenuhi masih menunggu pilihan pemilik setelah melihat contoh. Ini keputusan produk, bukan bukti implementasi.

**Keputusan 20 pertanyaan akhir, 29 Oktober 2026:** autosave tanpa pesan sukses setiap kali; saat gagal beri peringatan dan tombol coba lagi, dengan perlindungan progres. Uji keandalan dan penanganan kegagalan tanpa menjanjikan keberhasilan mutlak. Jika kejadian Whale kurang dari satu per tujuh hari game, siapkan kejadian simulasi masuk akal agar minimal satu warta. Rumor lanjutan langka minimal sekali per 14 hari game; detail jendela, konflik dan pembulatan menunggu rancangan atau keputusan. Kartu galeri memuat gambar, nama, harga, status kepemilikan; detail UI dan fallback menunggu preview. Plan 14 dimulai setelah pemilik meninjau dan menunjuknya. Jawaban J01–J20 lengkap di Plan 14.

**Keputusan lanjutan Q&A 21–70, 29 Oktober 2026:** dua berita Pro harian dapat membahas emiten yang sama jika ada alasan dalam game, menggantikan syarat dua emiten berbeda; emiten juga boleh muncul lagi hari berikutnya dengan alasan jelas. Jumlah dan waktu warta Whale mengikuti kejadian nyata dalam game, dalam arah 1–2 per tujuh hari game; jika kejadian tidak mencukupi, jangan mengarang dan ajukan contoh untuk menyelesaikan hubungan dengan minimum. Kejadian lanjutan langka jarang tetapi mungkin terlihat saat uji bermain; persentase dua kali awal dijamin pada penutupan bursa yang relevan dibanding harga penutupan hari sebelumnya. Konflik berita dan pembulatan menunggu contoh serta keputusan. Penghentian auto-renewal melalui Kelola Paket dengan konfirmasi; akses bertahan sampai akhir periode berbayar, pemberitahuan mengikuti kategori tagihan, dan riwayat biaya tersedia. Rincian lengkap, batas, preview yang diminta dan keputusan lain ada pada Q&A 21–70 dalam Plan 14; ini bukan bukti implementasi.

**Keputusan warta lanjutan 29 Oktober:** dua berita Pro per hari bursa, masing-masing tentang satu dari dua emiten dan kenaikan 2–5%; menggantikan kuota lama 2–3. Warta eksklusif Whale 1–2 kali per tujuh hari game, dengan kenaikan lebih dari 20% sesuai keputusan sebelumnya. Pemilik meminta kejadian langka rumor lanjutan hari berikutnya dengan persentase kenaikan dua kali rumor awal, bukan harga saham dua kali lipat; contoh 21% menjadi 42%. Frekuensi kejadian langka, waktu tepat, harga acuan lanjutan, jaminan pada rumor lanjutan, konflik dan pembulatan belum dipilih; sajikan contoh dan minta keputusan. Pertahankan keputusan harga/akses terdahulu sejauh tidak bertentangan. Rincian di Plan 14.

**Keputusan terbaru 29 Oktober 2026, mengungguli rincian lama yang bertentangan:** sasaran awal uji pribadi lewat APK; semua fitur yang direncanakan harus lengkap dan semua bug diketahui diperbaiki sebelum rilis. Jika saldo kurang saat auto-renewal, langsung turun ke Basic; pemain dapat menghentikan auto-renewal Pro/Whale. Pro mendapat 2–3 warta acak per hari bursa; kuota Whale menunggu contoh dan pilihan pemilik. Jika save rusak, tawarkan pemulihan cadangan setelah konfirmasi; game baru melindungi progres lama dan meminta konfirmasi untuk tindakan penghapusan, menggantikan pilihan lama menghapus progres tanpa cadangan. Penanganan save replay menunggu pemilik melihat kondisinya, dengan perlindungan hash-only dan larangan mengakali penolakan tetap berlaku. Preview galeri grid dua kolom bergambar ditampilkan terlebih dahulu, bukan persetujuan final atau penghapusan tiga alternatif. Layar dapat dipreview per kelompok dengan persetujuan tiap layar. Lima kategori notifikasi dapat diatur. Ponsel pemilik digunakan dengan izin per tindakan; skenario offline disiapkan sebelum meminta izin. Rincian 20 jawaban di `Work/Master-Plan-Gabungan-2026-10-24/Plans/14-kesiapan-uji-dan-rilis/plan.md`; plan itu belum mulai otomatis.

- **Rujukan master plan masukan pemain:** `Work/Master-Plan-Gabungan-2026-10-24/MASTER-PLAN-EKSEKUSI-MASUKAN-PEMAIN.md`; pelacakan awal 41 subtask ada di `DAFTAR-TASK-EKSEKUSI-MASUKAN-PEMAIN.md`. Rujukan rilis awal dan ikon: `Artifacts/Persiapan-Aplikasi-2026-10-28/PANDUAN-RILIS-AWAL-DAN-GAME-BARU.md` dan `PANDUAN-PERSIAPAN-DAN-OPTIMISASI.md`. Gunakan status terkini, bukan klaim selesai historis.
- **Arah tampilan terbaru — Tycoon Sinematik:** pada 28 Oktober 2026 pemilik memilih contoh Tycoon Sinematik sebagai dasar untuk seluruh game, menggantikan Biru Samudra sebagai arah warna umum. Referensi adalah preview interaktif `C:/Users/HYPE AMD/.codex/visualizations/2026/10/28/01a1811c-8996-7d01-a89c-f58163685f4a/gaya-game-saham.html`; data dan aksi pada contoh bersifat simulasi. Pilihan arah dasar bukan persetujuan otomatis atas detail atau implementasi setiap layar: tampilkan preview setiap perubahan UI, teks dan alur untuk persetujuan eksplisit, gunakan Q&A yang dapat diklik bila tersedia, dan kerjakan tugas independen selama menunggu. Pertahankan keputusan lain yang tidak bertentangan, termasuk avatar bulat menuju Profil, label Tertahan tanpa panah yang dapat diketuk, kartu indeks ringkas, Portofolio B, Profil A, ikon launcher dan tiga alternatif preview galeri. Jangan menyatakan Tycoon Sinematik sudah diterapkan di source/APK hanya karena preview tersedia.
- **Paket terminal — keputusan terbaru 28 Oktober 2026:** Pro berharga USD 250.000 dan Whale USD 1.000.000 dalam uang game, masing-masing aktif 30 hari game (sekitar 30 jam nyata pada 24×). Pemilik memilih kabar Pro tentang kenaikan harga 2–5% dan kabar Whale tentang kenaikan harga di atas 20%; Whale juga menerima kabar Pro 2–5%. Pemilik memilih bahwa harga saham harus benar-benar naik sesuai angka kategori tersebut: keputusan ini menggantikan keputusan lama bahwa kenaikan 2–5% atau di atas 20% pada kabar paket tersebut hanya perkiraan/tidak dijamin. Ini bukan jaminan laba bersih pemain: biaya, waktu order, harga eksekusi, ketersediaan, dan kondisi beli/jual tetap berpengaruh. Pemilik memilih kenaikan Pro 2–5% dan kenaikan Whale lebih dari 20% harus tercermin pada harga penutupan bursa berikutnya. Pemilik memilih harga acuan Pro adalah harga saat bursa berikutnya dibuka, bukan harga saat kabar diterima. Pada saat bursa tersebut tutup, harga harus 2–5% di atas harga pembukaan itu. Contoh: harga buka Rp10.200, maka penutupan Rp10.404–Rp10.710 (sebelum aturan pembulatan/tick yang belum diputuskan). Pemilik juga memilih harga pembukaan bursa berikutnya sebagai acuan Whale dan harga harus naik lebih dari 20% saat bursa tutup: jika harga buka Rp10.000 maka harga tutup harus lebih dari Rp12.000. Mekanisme, kasus tidak terpenuhi dan interaksi pasar/berita lain masih harus diputuskan. Pemilik memilih waktu informasi Pro dan kabar eksklusif Whale sama: satu hari game sebelum bursa berikutnya dibuka (sekitar satu jam nyata pada 24×); keterkaitannya dengan waktu publikasi dan kondisi penutupan/pembukaan masih perlu dirancang. Berita umum lain tidak otomatis dijamin naik. Tetap pertahankan tenggat efek berita 60/120 menit nyata yang sudah disetujui. Pemilik memilih kabar tersedia pada setiap pembukaan pasar dengan frekuensi sedang; jumlah pasti dan pembagian tiap tier masih perlu contoh. Pro dan Whale diperpanjang otomatis setelah 30 hari game jika saldo cukup; rincian saldo kurang dan pembatalan perpanjangan belum dipilih. Preview setiap perubahan UI wajib sebelum penerapan.
- **Manfaat tambahan Pro — pilihan setelah preview 28 Oktober 2026:** pemilik menyetujui ringkasan aktivitas investor besar NPC yang sudah terjadi sebagai manfaat Pro dan menyetujui arah contoh tampilannya `Artifacts/Preview-Pro-Tycoon-2026-10-28/pro-tambahan-npc.png`; gunakan data transaksi yang benar-benar tercatat, bukan angka rekaan. Pemilik menyatakan hanya setuju pada investor NPC di antara tiga pilihan dan menolak misi Pro serta pengingat harga/waktu sebagai manfaat Pro; keputusan ini tidak otomatis menghapus misi atau notifikasi umum game. Pemilik sebelumnya menolak perbandingan saham sebagai manfaat Pro dan bonus tambahan saat menjual untung, tanpa meminta penghapusan fitur dari seluruh game. Diskon transaksi dan alternatif bonus dividen belum dipilih. Persetujuan arah contoh bukan bukti implementasi, uji, atau persetujuan seluruh detail UI.
- **Keputusan tindak lanjut review APK-14 (28 Oktober 2026):** pesanan IPO baru ditolak setelah masa penawaran tutup, bahkan sebelum listing; pemilik memilih masa penawaran tiga hari game (sekitar tiga jam nyata), sedangkan jam spesifik, penjatahan dan migrasi jadwal belum ditetapkan. Profil NPC menampilkan peringkat kekayaan dan return dengan label terpisah, dengan preview tampilan sebelum penerapan. Untuk order beli 300 saham yang menghadapi penjual NPC 100 saham lalu penjual lain 300 saham pada harga cocok, pemilik memilih melewati penjual 100 agar seluruh 300 dibeli dari penjual yang cukup; audit semua sisi, efek prioritas dan atomicity tanpa menyimpulkan seluruh skenario dari satu contoh. Pemilik menyetujui contoh terminal satu pintu sebagai dasar desain; detail implementasi masih perlu preview dan persetujuan. Urutan preview Tycoon: Beranda, Pasar, Portofolio, Bisnis, Lainnya, dilanjut layar terkait. Hasil tes komputer, browser, APK dan ponsel dilaporkan terpisah; sebut belum diuji bila belum. Keputusan terbaru mengungguli catatan lama yang bertentangan.
- **Rilis awal dan save:** pemain baru harus memperoleh progres awal bersih; pengujian reset dan restore dilakukan pada QA terisolasi. Pemilik memilih menghapus progres lama tanpa cadangan, namun penghapusan `Artifacts/LocalPreview/preview-save/save_game_v2.json` pernah ditolak peninjauan otomatis `blocked by policy` dan belum terverifikasi terhapus. Jangan mengakali penolakan atau mengklaim penghapusan berhasil. Fitur penyimpanan untuk progres pemain baru tetap diperlukan; penanganan save replay lama D06 belum diputuskan.
- **Temuan review APK-09:** baca `Artifacts/Review-Kandidat-APK09-2026-10-28/LAPORAN-REVIEW-INDEPENDEN.md` dan verifikasi pada source terkini. Denda lama 0,05% per hari game tercatat masih berjalan bersama denda baru 5% per 24 jam nyata; tetapkan perbaikan dan uji melalui alur produksi pada P06. Reset lengkap, restore backup, persetujuan galeri, akses produk beragunan, dan benchmark transaksi/reload/chart yang benar juga belum terbukti selesai. Jangan menyebut APK-09 siap rilis atau 100% berdasarkan tes lulus saja.
- **Kebijakan pembiayaan yang telah dipilih:** denda sederhana 5% setiap 24 jam nyata atas cicilan yang belum dibayar, tanpa bunga atas denda sebelumnya; agunan dilepas dan utang lama tetap ada. Rincian yang belum diputuskan—dasar komponen cicilan, pembayaran parsial, batas, anchor, dan jam mundur—harus disajikan untuk keputusan sebelum implementasi asumsi.

### Peta dependensi lintas rencana

| Rencana ini | Kaitan master plan masukan pemain | Hal yang harus direkonsiliasi |
|---|---|---|
| P00/P02 | M0/M1/M7 | Baseline source, integritas save, reset/restore dan laporan; uji aktual QA. |
| P01 | A06/M4 | Hapus replay sesuai keputusan terbaru; jangan hapus grafik pasar utama atau migrasi save replay otomatis. |
| P03/P04 | M3/M4, A01/A02/R6 | Jadwal dan efek berita harus menjaga 24×, deadline 60/120 menit dan alur order. |
| P05/P06 | M5/M6, A16/A17/A20/A21 | Keputusan bisnis, utang, valuasi dan denda harus konsisten; perbaiki temuan APK-09. |
| P07/P08/P09 | M2/M7, A15 | Preview/approval, playtest, pengukuran, ikon dan kandidat dari source yang sama. |

Peta ini menunjukkan keterkaitan, bukan task baru atau penjumlahan jumlah task. P00 lalu keselamatan save/temuan kritis P02/P06 direkomendasikan sebagai urutan awal; paket tetap menunggu penunjukan atau mandat pemilik sesuai instruksi.

### Pilihan lanjutan keseimbangan progres

- Order Market setelah contoh dana: pemilik memilih **game menyarankan batas dana maksimum dan pemain mengonfirmasi**. Formula usulan, fee, validasi, perubahan harga, durasi/sisa tidak terisi, serta perlakuan saat kebutuhan melebihi batas masih perlu keputusan dan uji; jangan menganggap saran sebagai persetujuan otomatis.

- Keputusan lanjutan order dan kalender: pemilik memilih **Limit dan Market** saat pasar tutup; order disimpan lalu diteruskan saat buka, bukan langsung terisi. Jika saham disuspensi saat bursa buka, order ditolak dan dana/saham dilepas; hasil ditampilkan dalam ringkasan dengan akses riwayat. Pemilik memilih **A — Tahapan order** setelah melihat tiga alternatif; pastikan tiap status sesuai domain dan preview untuk detail lain. Order aktif sampai dibatalkan adalah pilihan sebelumnya; untuk Market perlu keputusan eksplisit tentang batas dana/biaya, harga bergerak, likuiditas dan sisa tidak terisi karena durasi tersebut belum tentu kompatibel.
- Kalender terbaru: bursa buka setiap hari game pukul **09.00–16.00 waktu game**, dipilih pemilik pada 28 Oktober 2026. Pada 24×, durasi pembukaan 17 menit 30 detik nyata tanpa istirahat tambahan; pengaturan istirahat, pengecualian, hari libur khusus dan perbedaan bursa belum diputuskan. Order saat tutup menunggu sesi sesuai aturan; jangan menafsirkan pilihan jam sebagai persetujuan istirahat.
- Pembiayaan: denda berikutnya 5% dari sisa cicilan belum dibayar setelah pembayaran parsial (contoh Rp100.000 dikurangi Rp40.000 = dasar Rp60.000); penanganan komponen bunga/utang dan denda sebelumnya tetap menunggu desain dan keputusan.
- Hadiah misi: klaim otomatis **langsung saat misi selesai**; offline diproses saat rekonsiliasi dengan bukti idempoten, tanpa klaim untuk misi belum selesai.
- Penanda “harga simulasi game” pasar utama: **hapus** menurut keputusan terbaru, menggantikan persetujuan lama; tampilkan preview perubahan UI dan minta persetujuan sebelum menerapkan.

- KP09: contoh notifikasi ponsel untuk order, berita, IPO, bisnis dan tagihan ada di `Artifacts/Preview-Saran-2026-10-28/contoh-notifikasi-ponsel.png`; setelah preview, pemilik memilih semua lima jenis (order, berita, IPO, bisnis, tagihan) aktif secara awal dan pemain dapat mengatur jenisnya. Contoh bukan peristiwa nyata. Uji batas offline, izin sistem, pembatasan proses Android, jadwal, duplikasi dan jam nyata/game; jangan menjanjikan notifikasi real-time saat proses mati tanpa bukti perangkat.

- KP10: pemilik menyetujui rencana uji dengan tiga gaya: aktif trading, fokus bisnis, dan santai/offline. Laporkan waktu nyata, waktu aktif, hambatan, hasil kuantitatif serta observasi playtest sebelum usul perubahan aturan. Sasaran 1–3 hari nyata bukan hasil yang sudah terbukti.

- KP07: setelah menolak contoh awal target kekayaan dan melihat alternatif, pemilik memilih **keduanya**: jalur pencapaian berdasarkan minat dan ide kegiatan opsional. Usulkan tampilan gabungan melalui preview; pemain bebas memilih dan tidak wajib membeli saham; isi target dan hadiah baru belum diputuskan.
- KP08/D05: setelah membandingkan dua contoh terbaru, pemilik memilih **B — daftar seluruh kejadian** ketika kembali. Keputusan ini menggantikan preferensi tampilan ringkasan singkat jika berbeda; tampilkan hanya kejadian yang dicatat dan jelaskan keterbatasan data, sediakan urutan/filter atau pemuatan bertahap sesuai preview, jangan mengarang kejadian.

- KP06: pemilik memilih contoh perbandingan pemasukan, biaya dan **perkiraan laba** sebelum/sesudah keputusan bisnis sebagai dasar. Tandai proyeksi bukan jaminan dan gunakan hanya data domain; hasil aktual ditampilkan terpisah setelah terjadi.

- KP04: setelah melihat dua contoh pasar tutup, pemilik memilih **menampilkan kedua kelompok**, yaitu riset/pantauan dan bisnis/aset, serta tetap dapat membuat order yang menunggu bursa buka. Susunan gabungan yang belum diperlihatkan tetap perlu preview dan persetujuan.

- KP02: setelah melihat preview, pemilik memilih **A — semua syarat dan progres karier ditampilkan**, sebagai dasar. Gunakan syarat dan progres domain yang aktual, serta jelaskan state belum tersedia.

- KP02/KP04/KP06/KP07/KP08/KP10: pemilik meminta contoh visual atau contoh rencana pengujian sebelum memilih. Contoh terpisah `Artifacts/Preview-Saran-2026-10-28/progres.html` meliputi dua alternatif syarat karier, dua susunan kegiatan pasar tutup (order tetap menunggu buka), hasil bisnis, tahapan dan dua tampilan ringkasan offline. Angka ilustrasi, bukan data game; KP02/KP04/KP06/KP07/KP08 telah dipilih sebagai dasar, KP10 telah disetujui, dan KP09 memilih semua contoh notifikasi dengan kontrol per jenis. Rencana uji yang diusulkan membandingkan trading, bisnis dan santai/offline, memisahkan waktu nyata dari waktu bermain aktif serta tes otomatis dari playtest; target besar 1–3 hari nyata bukan hasil terukur.

- KP09: pemilik meminta notifikasi berbagai fenomena dan peristiwa untuk mengingatkan pemain; pemilik menginginkan notifikasi di ponsel ketika tidak bermain/game tidak dibuka; pemilik memilih lima jenis contoh aktif secara awal dan pemain dapat mengatur masing-masing; perilaku saat aplikasi tertutup dan rancangan kontrolnya perlu diuji serta dipreview. Jangan menganggap izin perangkat, push, sumber berita eksternal atau perilaku background sudah disetujui/tersedia. Berita dan notifikasi berdasarkan peristiwa game nyata dan sesuai keputusan 24×; preview UI dan uji izin bila relevan.

- KP05: pemilik memilih **mempertahankan nominal sponsor saat ini**. Jangan mengubah nominal atau formula tanpa keputusan baru; keseimbangan dapat diukur dalam playtest dan hasil dilaporkan, bukan diubah diam-diam.

- KP03: pemilik memilih hadiah misi yang telah selesai **diklaim otomatis langsung saat misi selesai**, termasuk saat offline direkonsiliasi; gunakan idempotensi dan ledger agar hadiah tidak ganda; misi belum selesai tidak mendapat hadiah. Perubahan hadiah/tampilan dan migrasi perlu dipreview dan diuji.

- KP01: pemilik memilih beberapa tujuan singkat yang **bebas dipilih pemain** pada awal permainan; tidak menambahkan tutorial pembelian wajib, tidak memaksa transaksi. Gunakan atau perluas misi existing setelah inventaris; tampilan baru tetap memerlukan preview dan persetujuan.

### Keputusan terbaru: penyederhanaan transaksi dan fraksi

**Klarifikasi terbaru pemain:** tidak perlu fitur tambahan karakter lawan transaksi dan tidak perlu fitur menyarankan jumlah lebih kecil saat saldo tidak cukup. Pemilik ingin order langsung diproses **ketika harga cocok**, seluruh jumlah atau tidak terisi, sesuai sesi. Jika harga berubah saat order beli akan diproses dan saldo tidak cukup untuk membeli seluruh jumlah, order gagal dan dana tertahan dikembalikan. Jangan menghapus otomatis NPC atau likuiditas pasar yang sudah menjadi bagian domain; audit mekanisme existing. Definisi kecocokan harga dan aturan perubahan harga yang lain masih perlu diputuskan tanpa mengasumsikan order pasti terisi. Pertahankan audit ledger, cadangan dana/saham, tidak double-fill dan preview UI.

**Klarifikasi terbaru pemain — harga terlihat dan terisi penuh:** saat harga saham terlihat Rp7.100 dan pemain memilih Beli pada harga tersebut, pemain mengharapkan memperoleh seluruh jumlah yang dibeli langsung pada transaksi yang dapat dijalankan, bukan sebagian. Pemilik memilih pemain tidak perlu memilih harga referensi; tampilkan perkiraan biaya. Untuk order dibuat saat bursa tutup tetap menunggu sesi buka. Perubahan harga antara tampilan, konfirmasi, pemrosesan dan pembukaan bursa masih perlu diputuskan; jangan mengarang jaminan harga lama atau melewati biaya/saldo tanpa aturan jelas. Pemilik memilih order gagal dan dana tertahan dikembalikan jika harga berubah dan saldo tidak cukup untuk jumlah penuh; definisi harga cocok yang terukur tetap perlu dipastikan.

Preview transaksi `Artifacts/Preview-Saran-2026-10-28/transaksi-sederhana.png` dan contoh saldo/lawan `contoh-saldo-lawan.png` telah ditampilkan. Pemilik tidak memerlukan karakter lawan tambahan maupun saran jumlah lebih kecil; penanganan saldo kurang akibat harga berubah telah dipilih di atas, sementara definisi harga cocok masih memerlukan keputusan. Contoh hanya ilustrasi.

Pemilik menyetujui rekomendasi mekanisme lainnya dalam penjelasan sebelumnya (lot saham IDX, perbedaan Limit/Market, prioritas harga/waktu, suspensi, dan IPO/penjatahan) sebagai arah; persetujuan prinsip ini tidak mengesahkan perubahan UI atau detail aturan yang belum dipreview/diputuskan. Keputusan berikut menggantikan pilihan lama bila bertentangan:

- **Tidak ada transaksi/order terisi sebagian untuk pemain.** Pemilik menginginkan hanya terisi penuh atau tidak terisi, serta jual/beli lebih mudah dan segera. Hapus tampilan status terisi sebagian dan jangan melakukan mutasi ekonomi sebagian lalu menyebutnya terisi penuh; order hanya selesai jika seluruh jumlah terlaksana, jika tidak maka tetap tidak terisi dengan alasan/status yang jelas. Status menunggu, gagal dan batal tetap dapat dipakai untuk menjelaskan proses, tetapi bukan status terisi sebagian. Preview UI wajib. Tentukan penanganan legacy partial fills dan data/ledger tanpa menghapus riwayat.
- Keputusan sebelumnya yang meminta sisa order parsial terus antre **digantikan**. Pilihan lama untuk memasukkan angka referensi juga digantikan: pemilik kemudian memilih tidak perlu memilih angka harga dan menjelaskan bahwa order diproses penuh ketika harga cocok. Nama dan aturan jenis order, definisi harga cocok, biaya, perubahan harga dan perilaku ketika dana/likuiditas kurang masih perlu keputusan. Jangan menjanjikan transaksi pasti tanpa syarat atau menganggap batas harga yang tidak dipilih pemain.
- Pemilik **tidak menyetujui rekomendasi fraksi harga dan batas perubahan**; jangan menambah atau mengubah aturan tersebut atas dasar rekomendasi ini. Apakah fitur fraksi atau auto-rejection existing harus dihapus/diubah masih menunggu klarifikasi spesifik; jangan diam-diam menghapus sistem existing atau menganggap harga bebas sudah diputuskan. Pertimbangkan perbedaan instrumen IDX/US dan dampak save/tes.
- Pemilik memilih pembelian seluruh jumlah jika saldo tersedia meski biaya nyata melewati angka dana yang semula dikonfirmasi; bila harga berubah dan saldo tidak cukup, order gagal dan dana tertahan dilepas. Angka tersebut bukan batas maksimum; istilah, feedback, cara menjaga ketersediaan dana/fee dan perubahan harga lain perlu preview dan aturan yang jelas. Pemilik memilih tidak menambah karakter lawan transaksi maupun fitur saran jumlah lebih kecil; gunakan mekanisme pasar existing setelah audit dan klarifikasi kecocokan harga.
- Jika order saat tutup: tetap disimpan dan baru diteruskan saat bursa buka, berlaku keputusan lain (reservasi, pembatalan dan 24×) sampai ada perubahan. Penuh/tidak terisi tidak otomatis berarti order dikirim sebelum buka, menyebut estimasi dana sebagai batas maksimum yang dijamin atau pasti dipenuhi tanpa likuiditas. Penamaan dan perilaku order yang sebelumnya disebut Limit perlu diklarifikasi berdasarkan keputusan terbaru; revisi label/preview sebelum penerapan.

## 2A-1. Keputusan pemegang saham, IPO dan 1.000 NPC

**Status: keputusan produk yang telah dipilih; belum merupakan bukti implementasi, preview final, atau izin memulai seluruh paket otomatis.** Rincian jawaban dan inventaris awal ada di `Work/Master-Plan-Gabungan-2026-10-24/DRAFT-PLAN-PEMEGANG-SAHAM-IPO-PEMAIN.md`. Keputusan terbaru dalam bagian ini mengungguli riwayat yang lebih lama bila bertentangan. Terapkan hanya task yang ditunjuk pemilik sesuai `AGENTS.md`; setiap tampilan baru/perubahan tetap memerlukan preview dan persetujuan.

| ID | Keputusan pemilik | Kriteria dan batas untuk agent |
|---|---|---|
| SH01 | Tampilkan pemegang saham dengan kepemilikan **1% atau lebih**, beserta kepemilikan publik. | Nama, jumlah saham dan persentase, urutan terbesar ke terkecil; perbarui ketika halaman dibuka/diperbarui. Gunakan kepemilikan domain aktual dan hitung ambang sebelum pembulatan; jangan menyulap saham yang belum diketahui menjadi publik. Preview tampilan diperlukan. |
| SH02 | NPC memiliki saham beragam sesuai kekayaan. | Gunakan portofolio, kas dan transaksi yang tercatat; total saham konsisten dengan saham beredar. Jangan mengarang pemilik legacy yang belum terpetakan. |
| SH03 | Hak suara, usulan agenda, dan kendali bertahap menurut kepemilikan. | Jumlah suara berdasarkan saham yang berhak; hak menyesuaikan jika kepemilikan turun. Saham akun klien bukan kepemilikan pribadi untuk kendali. Ambang dan rincian hak, termasuk tanggal pencatatan, belum disetujui. |
| IPO01 | IPO emiten lain: informasi Profil, Keuangan, Risiko, dan Rencana IPO; pemesanan dapat dibatalkan selama penawaran sebelum penjatahan. | Gunakan dasar preview informasi bertab yang dipilih; tampilkan data tersedia secara jujur dan uji tanggal, pembatalan dan pelepasan dana. |
| IPO02 | Dana IPO yang tidak terpakai dikembalikan otomatis; setelah IPO saham tercatat dan dapat diperdagangkan. | Uji penjatahan tidak melebihi saham yang ditawarkan, refund tepat sekali, listing dan perdagangan. |
| IPO03 | Pesanan besar IPO adalah pesanan **minimal 20% dari seluruh saham yang ditawarkan**. Saat kelebihan pesanan, pemilik memilih pembagian awal **60% kelompok besar dan 40% pemesan lain**; jika jatah salah satu kelompok tidak habis, sisa dialihkan ke kelompok lain sesuai pesanan belum terpenuhi. | Kedua kelompok membagi jatah masing-masing sebanding besar pesanan. Perhitungan contoh 300:100 menjadi 150:50 dari jatah 200 hanya ilustrasi proporsi; kelompok dan jumlah sebenarnya harus konsisten dengan ambang 20%, penawaran dan lot. Pecahan terbesar mendapat lot sisa; bila pemesan seri, game mengundi. Uji pembulatan lot, sisa, batas pesanan, kelompok kosong dan penggunaan dana; jangan menjanjikan pesanan penuh atau melebihi saham tersedia. Ambang dan rasio ini aturan game, bukan ketentuan BEI/OJK. |
| IPO04 | Perusahaan milik pemain boleh IPO jika memenuhi syarat usia, keuangan dan kesiapan; dana penerbitan saham baru masuk ke kas perusahaan. Pemain memilih jumlah saham baru sendiri tanpa saran game. | Syarat angka, valuasi dan batas penawaran masih terbuka. Penjualan saham existing berbeda dari penerbitan baru; jangan mencampur arus dana. |
| IPO05 | Pemain tetap memiliki kendali penuh atas perusahaan miliknya setelah IPO. | Pilihan ini sudah diputuskan; rancangkan mekanisme yang menjaga keputusan tersebut serta menjelaskan hak pemegang lain dan suara berbasis saham. Struktur kendali/rincian voting belum dipilih dan perlu preview serta persetujuan. |
| NPC01 | Tepat **1.000 NPC unik** aktif sebagai peserta pasar dan mengisi leaderboard; pemain peserta terpisah. | Inventarisasi dan gunakan sistem liga existing; cegah ID/akun ganda, jangan menambah 1.000 di atas peserta lama secara diam-diam. Kebijakan migrasi save dan peran agen pasar lama perlu audit. |
| NPC02 | Peringkat mengikuti hasil nyata; strategi dan keputusan NPC beragam. Aktivitas berbeda dan tidak semua transaksi terjadi setiap saat. | Kekayaan dan peringkat berasal dari data game; urutan tie harus konsisten. Tetapkan kriteria pengamatan dan buktikan aktivitas melalui pengujian, jangan sekadar membuat 1.000 nama. |
| NPC03 | Profil ringkas NPC dapat dilihat; pemilik memilih contoh **B — informasi dibagi dalam tab**. | Gunakan contoh `Artifacts/Preview-Saran-2026-10-28/profil-npc-pilihan.png` sebagai dasar; nama dan nilai di contoh ilustrasi. Daftar final informasi dan desain detail masih menunggu pilihan/preview; gunakan data aktual, tandai yang belum tersedia. |
| NPC04 | Pemilik mengganti arah avatar menjadi **anime** dan menyetujui lima lembar contoh 100 avatar sebagai dasar; nama campuran Indonesia dan internasional. Satu avatar boleh digunakan ulang untuk NPC yang memiliki nama dan profil berbeda. | Siapkan aset per karakter sebelum integrasi dan uji keterbacaan, penempatan dan kinerja. Persetujuan contoh sebagai dasar bukan bukti integrasi UI telah disetujui atau selesai; preview penerapan dan state tetap diperlukan. |

**Keputusan pembulatan IPO:** setelah pembagian proporsional, alokasikan lot utuh dan berikan lot sisa kepada pemesan yang memiliki sisa pecahan terbesar; contoh 2,6 lot dan 2,4 lot menjadi 3 dan 2 lot. Jika pecahan sama dan lot tidak cukup bagi semua pemesan yang seri, pemenang diundi oleh game. Pembagian awal 60:40 antarkelompok menggunakan pecahan terbesar juga: 11 lot menjadi 7 lot dan 4 lot. Undian harus dapat dipulihkan setelah restart dan tidak berubah karena pemrosesan ulang, agar tidak terjadi pembagian atau refund ganda. Jangan melebihi pesanan atau jumlah saham yang tersedia; uji jatah, sisa, seri, refund dan kasus kelompok kosong.

**IPO saat pesanan tidak melebihi penawaran:** jika saham yang ditawarkan mencukupi untuk seluruh pesanan yang sah dan dana setiap pemesan cukup, masing-masing mendapat seluruh jumlah pesanannya. Contoh penawaran 10.000 lot, pesanan sah 20 dan 30 lot: keduanya memperoleh 20 dan 30 lot. Kuota 60:40 mengatur kasus kelebihan pesanan; jangan menggunakan pemotongan tetap 50% saat penawaran cukup. Uji batas tepat penuh, dana tidak cukup, status dan refund dalam alur penjatahan produksi.

**Keputusan setelah membaca SEOJK 25/SEOJK.04/2025:** pemilik memilih mempertahankan pembagian proporsional yang sudah diputuskan, tanpa menambahkan tahap awal 1 lot bagi setiap pemesan. Ini adalah pilihan gameplay; prosedur penjatahan terpusat OJK berbeda dan tidak boleh diklaim diterapkan secara identik. Penjatahan IPO dapat menghasilkan jumlah lebih sedikit dari pesanan sesuai keputusan IPO, sedangkan aturan transaksi pasar pemain tetap terisi penuh atau tidak terisi.

**Pengujian dan ketergantungan:** petakan seluruh task ini ke source dan caller, saldo/ledger/posisi, saham beredar, IPO, leaderboard, save dan 24×/offline. Uji pembulatan 0,99%/1%, perubahan kepemilikan, pemisahan akun, penawaran berlebih, pembatalan dan refund, restart/catch-up tanpa transaksi ganda serta migrasi. Ukur performa dan ukuran save pada 1.000 NPC, termasuk target perangkat bila tersedia; jangan menyamakan tes desktop dan Android. Gunakan save QA dan jangan mengubah progres pemain tanpa penanganan yang disetujui. Lakukan uji keseimbangan terhadap tujuan pencapaian besar 1–3 hari nyata, tanpa menganggap target sudah terbukti.

**Masih menunggu keputusan:** rincian profil NPC; kriteria aktif dan ukuran jendela pembuktian; pembagian saham awal/publik dan migrasi data legacy; ambang/hak kepemilikan; persentase penawaran dan syarat rinci IPO sendiri; rancangan kendali setelah IPO; serta semua preview UI yang belum disetujui. Detail yang belum dipilih jangan dinyatakan disetujui atau selesai.

**Rujukan penjatahan IPO (28 Oktober 2026):** [BEI — How to IPO](https://gopublic.idx.co.id/how-to-ipo/?slide=1) menjelaskan perlunya penjatahan jika pesanan melebihi saham. [OJK — SEOJK 25/SEOJK.04/2025](https://ojk.go.id/id/regulasi/Pages/SEOJK-25-SEOJK04-2025-Verifikasi-Pesanan-dan-Dana%2C-Alokasi-Penjatahan-dan-Penyelesaian-Pemesanan-Efek-dalam-Penawaran-Umum.aspx) mengatur verifikasi, penggolongan penawaran dan penyesuaian alokasi, serta menggantikan SEOJK 15/2020. [FAQ e-IPO](https://www.e-ipo.co.id/id/faq) membedakan penjatahan terpusat dan pasti, serta menjelaskan bahwa memesan lebih banyak tidak menjamin memperoleh seluruh pesanan. Rincian alokasi yang disederhanakan untuk game merupakan desain game, bukan salinan hukum atau klaim kesetaraan dengan aturan tersebut.

### Prinsip pengalaman bermain: aturan kompleks tetap terasa seperti game

Pemilik meminta setiap detail dan aturan yang kompleks mempertahankan nuansa visual bermain game. Ini berlaku lintas pasar, saham, IPO, NPC, bisnis, misi, portofolio, notifikasi dan profil; tidak mengizinkan penyederhanaan yang menghapus aturan atau mengubah hasil ekonomi yang sudah disetujui.

- Sajikan keputusan dan hasil secara mudah dipahami, bertahap sesuai kebutuhan pemain; tampilkan informasi penting pada saat memilih, dengan rincian yang bisa diakses tanpa memaksa pemain membaca seluruh aturan setiap saat.
- Tampilkan status, alasan, dampak dan langkah yang tersedia sesuai data game yang benar. Bedakan perkiraan dari hasil aktual; jangan sembunyikan biaya, risiko, waktu atau batas yang menentukan keputusan pemain. Hindari istilah teknis mentah dan angka yang tidak jelas.
- Pertahankan identitas visual game dan keputusan UI yang telah dipilih, termasuk permintaan avatar NPC anime; visual, istilah dan interaksi harus konsisten antarhalaman dan tetap terbaca pada layar ponsel serta state kosong, menunggu, gagal dan offline. Arah Biru Samudra pada versi lama digantikan Tycoon Sinematik sesuai bagian 2A; detail tiap layar tetap menunggu persetujuan.
- Buat preview setiap perubahan UI, teks atau alur dan mintakan persetujuan melalui pilihan otomatis sebelum penerapan. Jika belum disetujui, lanjutkan pekerjaan independen yang tidak bergantung pada keputusan tersebut; jangan menandai pekerjaan UI selesai hanya karena mockup tersedia.
- Uji keterbacaan, navigasi/kembali, aksesibilitas, respons di perangkat, konsistensi hasil dan integritas ekonomi, serta apakah interaksi mendukung target progres dan kesenangan bermain. Pilihan detail visual, animasi, suara, contoh edukasi dan hasil playtest belum otomatis disetujui.

Contoh 100 avatar anime tersimpan dalam lima lembar `Artifacts/Preview-Saran-2026-10-28/avatar-anime-01-20.png` sampai `avatar-anime-81-100.png`; pemilik menyetujuinya sebagai dasar, namun belum berupa aset terpisah atau terpasang di game. Preview penerapan tetap diperlukan.

### Persetujuan 16 rekomendasi riset

Pemilik menyetujui **R01–R16 sebagai dasar rencana** setelah melihat `Artifacts/Preview-Saran-2026-10-28/riset-ui-1.png` sampai `riset-ui-4.png` dan `prototype-riset-16.html`. Rincian sumber, ide dan risiko ada di `Work/RISET-OPEN-ACCESS-FITUR-GAME-2026-10-28.md`; tugas dan perbaikan terhubung di `Work/Master-Plan-Gabungan-2026-10-24/Plans/10-revisi-apk10-dan-riset/plan.md`. R01–R16 pada riset adalah ID terpisah dari R01 reset di tabel 2B.

Persetujuan sebagai dasar tidak menetapkan formula, hadiah, jam bursa, ambang hak atau detail ekonomi yang belum dipilih; jangan ubah keputusan sebelumnya dengan asumsi. Rincian tampilan, teks, alur dan state yang belum dipreview/disetujui masih melalui preview serta pilihan eksplisit pemilik. Petakan rekomendasi ke fungsi existing dan uji; persetujuan bukan bukti implementasi, pengujian atau izin otomatis melanjutkan di luar lingkup yang ditunjuk.

## 2B. Daftar periksa terperinci dan bukti penerimaan

**Aturan pelaksanaan:** daftar berikut adalah kebutuhan dan kriteria, bukan klaim sudah selesai atau perintah memulai semua paket. Agent memetakan setiap ID ke task paket yang ditunjuk, source/caller, keputusan, preview, test, bukti dan status. Catat sublangkah yang gagal, terlewat, belum diuji, belum disetujui, atau tidak berlaku beserta alasannya; jangan menghilangkan item kecil, memadatkan beberapa task menjadi satu PASS, atau menganggap mock, screenshot atau build sebagai bukti alur domain lengkap. Tugas hanya ditutup bila seluruh kriteria, keputusan dan bukti yang berlaku dipenuhi. Uji pada save QA; pertahankan keputusan 24× dan jangan mengubah aturan yang belum disetujui.

| ID | Hal yang wajib dipastikan | Bukti minimum dan kondisi selesai | Status awal |
|---|---|---|---|
| C01 | Tujuan singkat awal bebas dipilih, tidak wajib beli saham; petakan ke misi existing. | Preview disetujui; uji pilih/abaikan, keluar-masuk dan tidak ada transaksi paksa. | PERLU_INVENTARIS_PREVIEW_UJI |
| C02 | Tampilkan semua syarat/progres karier dari data aktual; state belum tersedia dibedakan. | Preview sesuai contoh A; periksa tiap syarat terpenuhi/belum, batas level dan refresh. | PERLU_UJI |
| C03 | Hadiah misi selesai otomatis diklaim, langsung ketika selesai, termasuk saat rekonsiliasi offline; misi tak selesai tidak dibayar. | Uji selesai, pergantian, restart/offline, klaim manual yang mungkin ada, retry dan ledger tepat sekali; gunakan waktu klaim langsung saat selesai dan uji offline. | PERLU_KEPUTUSAN_UJI |
| C04 | Dua kelompok kegiatan pasar tutup—riset/pantauan dan bisnis/aset—mudah diakses bersama order menunggu buka. | Preview gabungan disetujui; semua tautan dan Back diuji; tanpa order terkirim otomatis sebelum sesi. | PERLU_PREVIEW_UJI |
| C05 | Pertahankan nominal/formula sponsor saat ini, uji keseimbangannya. | Catat nilai baseline source dan hasil tiga gaya bermain tanpa mengubah nominal; jika ada usul, minta persetujuan. | PERLU_UJI |
| C06 | Tampilkan pemasukan, biaya dan proyeksi laba sebelum/sesudah keputusan bisnis; bedakan hasil aktual. | Preview sesuai contoh; uji sumber domain, kekurangan data, biaya dan nilai rugi; proyeksi tidak dijamin. | PERLU_UJI |
| C07 | Sediakan pencapaian menurut minat serta ide kegiatan opsional, sesuai pilihan kedua alternatif. | Inventaris fitur existing; preview gabungan disetujui; uji bebas memilih/abaikan; target/hadiah baru memerlukan keputusan. | PERLU_PREVIEW_KEPUTUSAN_UJI |
| C08 | Daftar seluruh kejadian yang benar-benar tercatat selama tidak bermain; nyatakan cakupan jika histori terbatas. | Uji banyak/kosong/sebagian/gagal muat, restart, urutan dan akses rincian; bila perlu pemuatan bertahap harus sesuai preview. | PERLU_PREVIEW_UJI |
| C09 | Notifikasi ponsel untuk order, berita, IPO, bisnis dan tagihan; pemain dapat mengatur tiap jenis. | Preview kontrol disetujui; uji izin diterima/ditolak, toggle, peristiwa sah, duplikasi, waktu dan batas proses/background pada lingkungan yang diizinkan. Jangan janjikan ketepatan saat aplikasi mati tanpa bukti perangkat. | PERLU_PREVIEW_UJI |
| C10 | Bandingkan tiga gaya: aktif trading, bisnis, dan santai/offline; sasaran pencapaian besar pertama 1–3 hari nyata. | Laporan waktu nyata dan aktif, asumsi, jalur, hambatan, risiko dan bukti playtest; tes otomatis dibedakan dari penilaian manusia. Usul perubahan ekonomi perlu keputusan. | PERLU_UJI |
| O01 | Saat pasar tutup, pemain melihat pasar dan membuat order; baru diteruskan pada sesi yang relevan. | Preview disetujui; uji waktu tutup/buka, per bursa, restart/catch-up dan idempotensi; diteruskan bukan jaminan terisi. | PERLU_PREVIEW_UJI |
| O02 | Dana order beli ditahan saat order dibuat; pembatalan sebelum buka melepas dana/saham; order baru harus terisi penuh atau tidak terisi; harga yang sebelumnya disebut Limit tidak dijamin menurut pilihan terbaru; nama/preview masih ditanyakan. | Uji ledger, dana dan saham tersedia/tertahan, cancel ganda, full-fill atau tidak terisi, legacy partial fill, status tidak valid, save/restart dan anti-duplicate. Besaran termasuk fee, reserve saham dan penanganan luar biasa perlu keputusan. | PERLU_KEPUTUSAN_UJI |
| O03 | Harga terakhir disertai waktu pembaruan; offline order diproses sesuai waktu game. | Uji timestamp/zona, harga lama, berjam-jam offline, kondisi jam mundur, determinisme dan tiada pengiriman ganda. | PERLU_UJI |
| O04 | Limit dan Market dipilih; pada saham disuspensi ketika buka, tolak order dan lepaskan dana/saham; hasil order muncul dalam ringkasan dengan akses riwayat. Preview A dipilih sebagai dasar tetapi harus direvisi untuk menghapus terisi sebagian dan disetujui ulang. | Gunakan preview A sebagai dasar; untuk Market terapkan keputusan pemain bahwa estimasi dapat terlampaui jika saldo cukup, dengan label yang jujur; minta keputusan fee, risiko perubahan harga dan durasi saat tidak terisi; jangan menetapkan asumsi sendiri. | MENUNGGU_JAWABAN |
| I01 | Halaman IPO memuat fase/kalender/status pesanan dan informasi perusahaan bertab Profil, Keuangan, Risiko, Rencana IPO. | Gunakan data aktual dan state belum tersedia; uji fase/tanggal, sumber, perubahan fase dan akses kembali; preview yang belum diperlihatkan harus disetujui. | PERLU_PREVIEW_UJI |
| G01 | Koleksi: 150 aset, pencarian, kategori dan status kepemilikan; tiga alternatif desain galeri wajib. | Mapping 150 ID/aset, filter dan loading/fallback/performa diuji; sajikan tiga alternatif dan catat pilihan eksplisit sebelum menetapkan desain. | MENUNGGU_PREVIEW_PERSETUJUAN |
| F01 | Denda sederhana 5% per 24 jam nyata dan pelepasan agunan, tanpa denda harian lama, utang lama tetap ada. | Reproduksi audit APK-09 pada source terbaru; tentukan komponen cicilan/parsial/anchor; uji alur produksi offline/restart/clock mundur, ledger dan migrasi. | PERLU_KEPUTUSAN_UJI |
| R01 | Reset rilis awal, simpan, backup dan pemulihan tanpa bocor progres lama pada QA. | Uji order, bisnis, misi, karier, koleksi, utang, histori, reset berulang dan corrupt/restore; keputusan save replay terpisah. Save lama pemilik belum terhapus karena review otomatis menolak; jangan mengakali. | PERLU_KEPUTUSAN_UJI |
| V01 | Sumber/ APK/ikon/performa/persetujuan dan laporan sinkron. | Uji kandidat yang sama; data mentah benchmark transaksi/load/chart, hash/aset/signing, UI approval, tes host/browser dan Android fisik dilaporkan terpisah. | PERLU_UJI |

**Keputusan terbuka yang tidak boleh diisi sendiri:** D01 rincian kalender game lebih sering buka; D03 besar respons berita; D04 formula bisnis; D06 save replay lama; O04 rincian Market; rincian cadangan dana/biaya dan ketentuan order luar biasa; rincian denda/parsial; target/hadiah untuk pencapaian; desain UI gabungan dan tiga alternatif galeri. D02 arah utama, D05 daftar kejadian, D07 jenis notifikasi, dan keputusan KP01–KP10 yang sudah diberikan tidak boleh ditandai belum dijawab secara menyeluruh. Jika Q&A masih menunggu, catat tepat pertanyaan dan lanjutkan pekerjaan independen.

## 3. Pilihan yang harus ditinjau pemilik

Rencana ini **tidak memilih otomatis** alternatif berikut. Prototipe atau simulasi untuk membandingkan boleh diusulkan; penerapan yang mengubah aturan menunggu keputusan pemilik.

| ID | Pertanyaan | Pilihan untuk dipertimbangkan | Dampak |
|---|---|---|---|
| D01 | Bagaimana jadwal pasar dan pengecualian? | Pemilik memilih bursa **buka setiap hari game pukul 09.00–16.00 waktu game** pada 28 Oktober 2026. | Ini setara 17 menit 30 detik nyata tanpa istirahat tambahan pada 24×; istirahat, libur khusus dan perbedaan bursa masih perlu diputuskan. |
| D02 | Aktivitas dan order saat pasar tutup. | Pemilik memilih tetap bisa melihat pasar dan membuat order untuk masuk pada sesi bursa berikutnya; pemilik meminta rujukan perilaku dunia nyata. | Arah utama, dana beli ditahan sejak pembuatan, aktif sampai dibatalkan, pembatalan sebelum buka dan pelepasan dana/saham, harga terakhir beserta waktu, pemrosesan offline telah dipilih; aturan sisa terisi sebagian digantikan oleh keputusan terbaru hanya terisi penuh atau tidak terisi. Jenis Limit dan Market sudah dipilih; rincian dana/biaya Market dan penanganan kondisi khusus lain masih perlu diputuskan. Order masuk tidak berarti langsung terisi; jangan ubah rasio 24×. |
| D03 | Bagaimana reaksi berita yang ingin dimainkan? | Kabar Pro dan Whale khusus mengikuti jaminan kenaikan harga yang dipilih pemilik di 2A; berita umum lainnya belum otomatis dijamin. | Baseline harga, waktu pengukuran, probabilitas kejadian dan konflik dengan pergerakan lain masih harus diputuskan; jaminan harga bukan jaminan untung bersih. |
| D04 | Seberapa besar pengaruh keputusan bisnis terhadap pendapatan, biaya, laba dan nilai perusahaan? | Tentukan setelah inventaris perilaku sekarang dan contoh simulasi. | Mengubah formula dapat mengubah keseimbangan dan save lama. |
| D05 | Aktivitas ketika kembali bermain. | Pemilik memilih contoh B, daftar seluruh kejadian yang tercatat, sebagai dasar setelah melihat preview. | Gunakan data domain aktual, jelaskan data yang belum tersedia, dan ajukan preview tambahan sesuai cakupan persetujuan. |
| D06 | Bagaimana menangani save lama yang sedang berada di replay? | Pilih setelah bukti format dan opsi pemulihan disajikan. | Jangan menghapus, menimpa, atau memigrasikan progres secara otomatis tanpa kebijakan yang aman dan disetujui. |
| D07 | Notifikasi IPO di ponsel saat tidak bermain. | Pemilik menginginkan notifikasi contoh IPO bersama order, berita, bisnis dan tagihan, dengan jenis yang dapat diatur pemain. | Perlu desain pengaturan, izin runtime bila relevan, pengujian platform dan batas proses offline; pilihan produk bukan izin ADB atau instalasi perangkat. |

## 4. Fakta awal yang harus diverifikasi ulang

- Pada pemeriksaan awal, APK-08 dan source saat itu berbeda pada `app.js`, `index.html`, dan `style.css`; `market_universe.json` yang diperiksa cocok. Cocokkan kembali pada saat pelaksanaan, termasuk source Java dan konfigurasi build, sebelum menganggap sebuah APK merepresentasikan source.
- APK-08 dilaporkan bertanda debug. Status tersebut bukan bukti kesiapan paket rilis.
- `MainActivity.java` mempunyai variabel `saveFailed` untuk peringatan native yang pada pemeriksaan awal tidak ditemukan perubahan nilainya. Periksa semua jalur pemanggil dan alur UI sebelum menyimpulkan dampaknya; UI juga memiliki penanganan kegagalan sinkronisasi.
- `GameClock.getMarketSession` yang diperiksa mengatur sesi IDX yang sama untuk semua hari dan jam AS tetap dalam WIB. Periksa pula pemanggil dan aturan tanggal/hari libur di tempat lain sebelum menilai perilaku runtime.
- Berita dan order NPC sudah memiliki hubungan sentimen. Fundamental emiten memiliki komponen yang dibentuk dari harga dan parameter simulasi. Keberadaan kode belum membuktikan hubungan sebab-akibat seluruh alur permainan.
- Laporan terdahulu mencatat tes host/JVM dan browser, tetapi itu bukan bukti pengujian baru atau pengujian fisik untuk source terbaru.

## 5. Urutan paket pekerjaan

Setiap paket di bawah adalah usulan cakupan, bukan izin bagi agent untuk melanjutkan otomatis. Buat atau kirim plan kerja yang ditunjuk pemilik satu per satu. Setiap paket wajib memiliki bukti, batas verifikasi, dan status yang jujur.

### P00 — Pemeriksaan awal dan perlindungan progres

- Buktikan root, branch, commit, status Git, dan inventaris pekerjaan yang sudah ada. Jangan reset, clean, atau menghapus perubahan pengguna.
- Identifikasi source kandidat, APK terdahulu, pengujian yang tersedia, dan status setiap keputusan D01–D07.
- Untuk QA gunakan save terisolasi; save pemain `Artifacts/LocalPreview/preview-save/save_game_v2.json` hanya boleh di-hash sesuai aturan proyek.
- Catat bagian yang belum diuji. Jangan menyimpulkan seluruh game lulus dari laporan historis.

**Selesai jika:** identitas source dan batas kerja tercatat; save QA terpisah; tidak ada perubahan pada save pemain.

### P01 — Hapus replay historis dengan aman

- Inventaris pilihan UI, bridge, mode game, loader, dataset, dokumentasi, tes, dan semua pemanggil replay.
- Hapus akses replay dan jalur yang benar-benar khusus replay. Pertahankan grafik, pasar, data, penyimpanan, atau komponen bersama yang masih dipakai fitur lain.
- Periksa save replay lama. Sajikan opsi pemulihan untuk keputusan D06; jangan menghapus, menimpa, atau memigrasikan save pemain tanpa dasar yang aman.
- Sesuaikan tes dan dokumentasi yang berkaitan, tanpa menghapus riwayat audit yang diperlukan.

**Selesai jika:** replay tidak lagi bisa dimulai dari game; pasar internal dan save non-replay tetap berfungsi; save replay lama ditangani sesuai kebijakan yang disetujui atau ditandai sebagai keputusan tertunda. Uji grafik, berita, waktu, beli/jual, portofolio, dan save/load dengan data terisolasi.

### P02 — Perbaiki keamanan penyimpanan dan sinkronisasi

- Telusuri seluruh jalur autosave, lifecycle, save manual, pemulihan backup, dan pelaporan kesalahan; perbaiki akar masalah, bukan sekadar menampilkan pesan baru.
- Gunakan tes kegagalan tulis, data utama rusak, backup valid/rusak, penutupan proses dan pemulihan dengan save QA.
- Pastikan kegagalan yang relevan dilaporkan dengan jujur dan tindakan tidak menampilkan keberhasilan palsu. Buktikan apakah transaksi perlu dibatasi berdasarkan kondisi sinkronisasi yang gagal.
- Uji agar perhitungan waktu dan kejadian tidak digandakan setelah retry, restart, atau recovery.
- Uji reset game baru melalui bridge dan save QA yang berisi order, bisnis, misi, level dan histori; uji corrupt/restore agar progres lama tidak muncul kembali tanpa kebijakan yang diputuskan. Jangan menjadikan penghapusan save asli sebagai jalan pintas.

**Selesai jika:** skenario sukses dan gagal mempunyai bukti; progres QA dapat dipulihkan sesuai kontrak; tidak ada klaim berhasil pada operasi gagal. Pengujian host tidak menggantikan pengujian Android.

### P03 — Audit dan rancangan jadwal pasar

- Petakan sesi IDX/AS, tanggal, akhir pekan, hari libur, perbedaan Jumat, DST, pembukaan/penutupan, penolakan order, dan seluruh caller jam pasar.
- Bandingkan aturan produksi dengan sumber BEI/NYSE yang masih berlaku saat implementasi; catat perbedaan antara ketentuan nyata dan adaptasi game.
- Hitung waktu tunggu nyata pada rasio 24x, termasuk istirahat, semalam, akhir pekan dan libur. Buat contoh alternatif untuk D01–D02 sebelum mengubah aturan.
- Setelah pemilik memilih, terapkan kontrak terpilih dan uji batas sesi, kalender, order saat pasar tutup, dan lintas hari pada aktif/offline.

**Selesai jika:** keputusan pemilik tercatat dan tes batas sesuai aturan terpilih. Jangan menganggap rekomendasi sesi atau kalender sebagai persetujuan otomatis.

### P04 — Berita dan reaksi pasar

- Petakan sumber dan masa berlaku berita, target perusahaan/sektor/makro, sentimen, dampak pada order NPC, dan efek yang sudah berlaku.
- Sajikan contoh D03 yang memanfaatkan mekanisme existing: dampak berbeda menurut cakupan dan ekspektasi; hasil harga tidak dijamin.
- Pertahankan tenggat nyata 60/120 menit. Uji berita bertumpuk, lintas sesi, save/load, offline, expiry dan retry tanpa menerapkan efek dua kali.
- Setelah disetujui, uji bahwa berita yang relevan dapat memengaruhi perilaku pasar melalui mekanisme yang sah, tanpa mengarang transaksi atau melanggar batas uang/saham NPC.

**Selesai jika:** perubahan sesuai keputusan pemilik, ada tes skenario positif/negatif/netral dan batas waktu, serta tidak ada janji harga pasti.

### P05 — Hubungan bisnis, fundamental dan keputusan pemain

- Telusuri hubungan keputusan operasional, perekrutan, ekspansi, pinjaman dan dividen dengan kas, pendapatan, biaya, laba, utang dan laporan.
- Periksa apakah fundamental emiten yang dapat dipengaruhi pemain berasal dari kondisi bisnis yang benar dan tidak membentuk hubungan melingkar yang menyesatkan.
- Buat contoh dampak dan trade-off D04 sebelum mengubah formula atau nilai lama. Lindungi data dan migrasi save.
- Uji keputusan, pembayaran, penyelesaian harian, save/load, kondisi dana tidak cukup, serta laporan dan valuasi yang saling konsisten.

**Selesai jika:** hubungan yang dipilih terbukti melalui contoh dan pengujian; perubahan rumus mendapat persetujuan pemilik.

### P06 — Konsistensi ekonomi dan aktivitas offline

- Daftarkan order, biaya, dividen, gaji, pinjaman, misi, liga, IPO dan kejadian lain yang mengikuti kalender game atau waktu nyata.
- Uji beberapa lama absen, pergantian hari, restart, transaksi tertunda, kegagalan save dan perubahan jam perangkat. Bedakan asumsi jam dan batas keamanan yang memang tersedia.
- Pastikan tiap kejadian yang berhak terjadi diproses tepat sekali dan saldo/posisi/ledger konsisten; jangan membuat penghasilan offline selalu untung.
- Tutup temuan denda ganda APK-09: denda 0,05% per hari game tidak boleh tetap menumpuk bersama aturan 5% sederhana per 24 jam nyata. Uji jatuh tempo, pembayaran parsial, 24 jam, offline, restart, jam mundur, restore dan repeat melalui alur produksi. Rincian dasar cicilan yang belum disetujui tetap terbuka.
- Siapkan contoh ringkasan saat kembali untuk D05 berdasarkan data domain. Perubahan tampilan mengikuti proses preview dan persetujuan yang berlaku.

**Selesai jika:** bukti menunjukkan tidak ada penggandaan pada skenario yang diuji dan risiko yang belum teruji dicatat. Jangan menyatakan semua durasi aman dari satu skenario.

### P07 — Kemudahan bermain dan keseimbangan

- Petakan kegiatan yang tersedia ketika pasar buka, istirahat dan tutup; ukur kapan pemain dapat mengambil keputusan yang berarti.
- Uji pemain baru dan pemain berpengalaman menggunakan kandidat yang sama. Catat waktu tunggu tanpa kegiatan yang dianggap menarik, waktu memahami hasil keputusan, dan hambatan istilah atau navigasi.
- Target pengalaman yang dipilih pemilik: pemain merasakan pencapaian besar pertama, seperti membuka jenjang karier atau membangun bisnis, dalam **1–3 hari nyata**. Ini sasaran playtest, bukan angka yang sudah terbukti atau instruksi mengunci semua fitur selama 1–3 hari; pemain tetap mendapat kegiatan dan kemajuan kecil sebelumnya. Ukur waktu nyata dan waktu bermain aktif secara terpisah pada beberapa gaya bermain, termasuk offline; catat rentang dan hambatan. Pertahankan jam dunia 24× dan ajukan perubahan ekonomi/aturan serta preview UI untuk persetujuan sebelum diterapkan.
- Susun usulan perubahan durasi misi, peluang, biaya atau progres hanya berdasarkan hasil yang dicatat. Jangan mengubah rasio 24x, menambah skip/pause, atau mengubah hadiah/tarif tanpa keputusan pemilik.
- Tidak membuat tutorial pembelian wajib atau penanda simulasi baru yang telah ditolak pemilik.

**Selesai jika:** hasil playtest dan usulan yang dapat ditinjau tersedia; keseimbangan tidak diklaim sempurna dari tes otomatis.

### P08 — Notifikasi dan uji Android offline

- Audit apakah notifikasi IPO memang dipicu, izin Android diminta dengan benar, dan hasil penolakan ditangani. Keputusan fitur mengikuti D07.
- Dengan izin perangkat yang berlaku dan save QA, uji instalasi/pembukaan pertama dalam mode pesawat; transaksi, karier, bisnis, portofolio, pengembalian dari background, save/restart, keyboard, Back, ukuran teks, ukuran layar, performa, baterai dan ruang penyimpanan.
- Bedakan pengujian host, browser, emulator dan ponsel fisik; catat perangkat, versi Android, kandidat dan hasil.
- **Batas akses saat ini:** `AGENTS.md` melarang ADB baru dan instalasi/perangkat. Persetujuan umum untuk daftar rilis bukan bukti bahwa batas ini sudah dicabut. Siapkan skenario dan kandidat terlebih dahulu; minta instruksi eksplisit sebelum tindakan perangkat yang melanggar batas tersebut.

**Selesai jika:** seluruh uji dalam cakupan yang diizinkan tercatat; uji yang belum diizinkan ditandai belum diuji, bukan lulus.

### P09 — Kandidat rilis dan keputusan publikasi

- Buat kandidat dari source yang disepakati; catat commit, perubahan lokal, versi, hash, hasil build, dan kesesuaian aset yang dikemas. Cocokkan juga kode Java/config dan hasil test dengan kandidat.
- Jalankan regresi sesuai perubahan dan uji kandidat yang sama; catat kegagalan, skip, dan lingkungan. Bila source berubah setelah build, jangan anggap bukti APK lama otomatis berlaku.
- Koreksi benchmark APK-09: ukur awal hingga data benar-benar siap, render chart, transaksi dan rekonsiliasi nyata, scroll/filter yang terverifikasi; simpan sampel mentah dan angka ringkasan konsisten serta bandingkan sebelum/sesudah. Jangan menyebut host sebagai bukti performa Android.
- Siapkan paket rilis, penandatanganan, identitas aplikasi, versi, hak aset/data, materi toko dan persyaratan Play Console sesuai jalur distribusi. Jangan menganggap kunci signing atau akses akun tersedia.
- Sajikan laporan kesiapan dan hambatan; publikasi/push hanya dengan instruksi eksplisit pemilik.

**Selesai jika:** kandidat dapat diidentifikasi, bukti sesuai kandidat, masalah rilis terdaftar dan pemilik dapat meninjau hasil. Build saja tidak membuktikan siap dipublikasikan.

## 6. Matriks pengujian untuk pemilik

Gunakan salinan/save QA, bukan progres asli. Hasil awal tiap baris **BELUM DIUJI** sampai ada bukti.

| ID | Skenario yang perlu dilihat | Hasil yang diharapkan | Status |
|---|---|---|---|
| T01 | Mulai game pasar internal dan beli/jual saat pasar buka | Saldo, biaya, saham dan riwayat sesuai hasil order | BELUM DIUJI |
| T02 | Tutup dan buka game setelah menyimpan | Kas, posisi dan progres tetap konsisten | BELUM DIUJI |
| T03 | Gagalkan penyimpanan pada lingkungan QA | Pemain mendapat informasi yang sesuai; tidak ada keberhasilan palsu | BELUM DIUJI |
| T04 | Tinggalkan game beberapa lama dan kembali | Waktu dan kejadian diproses sesuai aturan, tanpa duplikasi | BELUM DIUJI |
| T05 | Berita positif/negatif dan berita berakhir | Respons sesuai aturan yang disetujui; tidak ada jaminan harga | BELUM DIUJI |
| T06 | Keputusan bisnis dan pergantian hari | Kas, biaya dan laporan mencerminkan tindakan sesuai aturan | BELUM DIUJI |
| T07 | Pasar tutup, istirahat, Jumat, akhir pekan | Status/order mengikuti kalender terpilih dan waktu tunggu terlihat | BELUM DIUJI |
| T08 | Mainkan dengan mode pesawat di Android | Alur yang dijanjikan dapat dipakai tanpa jaringan | BELUM DIUJI |
| T09 | Buka ulang kandidat dari save QA yang lama | Pemulihan sesuai kebijakan, tanpa menimpa progres tanpa izin | BELUM DIUJI |
| T10 | Perjalanan pemain: saham, firma, bisnis, pinjaman, IPO | Alur dapat selesai dan uang/posisi tetap konsisten | BELUM DIUJI |
| T11 | Lihat pilihan permainan baru dan menu terkait | Replay tidak tersedia; pasar internal tetap tersedia | BELUM DIUJI |
| T12 | Notifikasi dengan izin diterima dan ditolak | Perilaku sesuai pilihan D07 dan kebijakan Android | BELUM DIUJI |

## 7. Referensi untuk peninjauan

- BEI, jam dan mekanisme perdagangan: https://www.idx.co.id/id/produk/mekanisme-dan-jam-perdagangan
- NYSE, jam dan kalender perdagangan: https://www.nyse.com/trade/hours-calendars
- Investor.gov, jenis order: https://www.investor.gov/introduction-investing/general-resources/news-alerts/alerts-bulletins/investor-bulletins-14
- Federal Reserve, suku bunga dan ekonomi: https://www.federalreserve.gov/monetarypolicy/monetary-policy-what-are-its-goals-how-does-it-work.htm
- CFA Institute, laba dan ekspektasi: https://rpc.cfainstitute.org/research/financial-analysts-journal/1986/on-the-relation-between-earnings-changes-analysts-forecasts-and-stock-price-fluctuations
- Studi perilaku pemain idle game: https://ray.yorksj.ac.uk/id/eprint/7953/1/BusyDoingNothingWhatDoPlayersDoInIdleGames.pdf
- Android, izin notifikasi: https://developer.android.com/develop/ui/compose/notifications/notification-permission
- Google Play, persyaratan uji akun personal: https://support.google.com/googleplay/android-developer/answer/14151465

Sumber pasar adalah rujukan untuk diadaptasi, bukan lisensi data historis dan bukan jaminan bahwa semua detail game harus meniru bursa. Periksa kembali peraturan dan kebijakan distribusi saat dikerjakan.

## 8. Hasil tinjauan pemilik

- Status rencana: **DIPERBARUI SESUAI TINJAUAN**; pilihan terbuka, penunjukan paket dan izin eksekusi/perangkat tetap berlaku.
- Keputusan terbaru 28 Oktober 2026: pemilik memilih **hapus replay historis** setelah melihat perbandingan. Keputusan sebelumnya untuk mempertahankan replay telah digantikan. Penanganan save replay lama (D06) masih menunggu keputusan terpisah; perubahan tampilan wajib mengikuti proses preview.
- Penanda harga simulasi pasar utama: pemilik memilih **hapus** setelah pertanyaan terpisah; penghapusan UI harus dipreview sebelum diterapkan.
- Pemilik menyetujui penyelarasan plan, penambahan rujukan keputusan desain/rilis, serta penanganan temuan review APK-09. Persetujuan pembaruan dokumen bukan izin otomatis menjalankan semua paket.
- Sepuluh saran tambahan: S01 ringkasan ketika kembali; S02 kegiatan dan order ketika pasar tutup; S03 pantau order; S04 berita dan sisa efek; S05 informasi pinjaman; S06 ringkasan bisnis; S07 grafik kekayaan; S08 pengelompokan misi; S09 pencarian koleksi; S10 IPO. Hanya respons eksplisit berikut dicatat; preview saja bukan persetujuan implementasi.
- S04: pemilik memilih tampilkan pihak terdampak dan sisa waktu efek berita. Pertahankan durasi 60/120 menit nyata dan ajukan preview UI.
- S05: pemilik memilih informasi ringkas cicilan dan jatuh tempo; contoh denda tidak dipilih sebagai isi utama. Tetap tampilkan atau buat mudah diakses ketentuan denda yang sebelumnya diwajibkan sebelum konfirmasi; jangan diam-diam mengubah aturan 5%. Ajukan preview UI.
- S06: pemilik memilih ringkasan pemasukan, biaya dan laba bisnis di satu tempat, memakai data aktual. Ajukan preview.
- S08: pemilik memilih kelompok misi harian dan mingguan. Ajukan preview, jaga hadiah dan mekanisme.
- S01/KP08: setelah memilih contoh ringkasan awal, pemilik kemudian memilih contoh B **daftar seluruh kejadian** sebagai dasar setelah membandingkan preview terbaru. Tampilkan waktu pergi dan hanya data aktivitas yang tercatat, termasuk perubahan kekayaan, order, bisnis, berita atau tagihan jika tersedia; jelaskan data belum tersedia, jangan mengarang angka. Preview contoh tidak otomatis menyetujui semua detail implementasi.
- S02/D02: keputusan terbaru pemilik adalah tetap melihat pasar dan membuat order ketika pasar tutup; order diteruskan/masuk ketika bursa kembali buka, seperti layanan antrean broker yang ia inginkan. Ini menggantikan contoh E (selalu buka) dan F (sesi tambahan). Masuk antrean tidak menjamin terisi. Status sebelum diteruskan, harga, cadangan dana/saham, validasi, masa berlaku, pembatalan, kegagalan dan penanganan offline harus ditetapkan dan diuji; jangan mengasumsikan keputusan rinci. Rujukan BEI sesi pra-pembukaan https://www.idx.co.id/id/produk/mekanisme-dan-jam-perdagangan, penjelasan order broker Stockbit https://snips.stockbit.com/investasi/ dan jenis order Investor.gov https://www.investor.gov/introduction-investing/general-resources/news-alerts/alerts-bulletins/investor-bulletins-14 . Perilaku broker bervariasi; antrean aplikasi berbeda dari order yang sudah masuk sistem bursa. Buat preview UI dan minta persetujuan; pertahankan jam dunia 24×.
- S02 rincian 2: pemilik memilih **dana order beli langsung ditahan saat order dibuat ketika pasar tutup**. Besaran termasuk biaya dan pelepasan saat batal/expiry/terisi, serta cadangan saham untuk order jual, masih harus dipastikan dan diuji; gunakan mekanisme reservasi domain tanpa menghitung dana dua kali.
- S02 rincian 4: pemilik memilih order yang belum terisi **tetap aktif sampai dibatalkan**. Mekanisme pembatalan, perilaku status emiten, penolakan validasi dan kondisi luar biasa perlu dijelaskan; bukan jaminan order pasti terisi.
- S02 rincian 5: pemilik menyetujui pembatalan sebelum pasar buka dan pelepasan dana atau saham yang tertahan; uji idempoten dan konsistensi ledger.
- S02 rincian 1 dan 3: pemilik memilih Limit dan Market setelah melihat contoh; pemilik memilih alternatif A tahapan untuk tampilan status sebagai dasar.
- S02 rincian 6: pemilik memilih menampilkan harga terakhir beserta waktu pembaruan ketika bursa tutup; gunakan sumber dan zona waktu yang jelas, jangan menyebut harga lama sebagai live.
- S02 rincian 7: pemilik memilih order diproses otomatis pada simulasi offline sesuai waktu game 24× saat game ditutup; persistensi, catch-up, batas waktu, transaksi tepat sekali serta determinisme diuji. Tidak menjanjikan bahwa aplikasi yang tertutup akan berjalan sebagai proses hidup terus-menerus.
- S02 rincian 9: keputusan lama mengenai sisa terisi sebagian **digantikan**: pemilik kini meminta hanya terisi penuh atau tidak terisi. Pemilik memilih order yang sebelumnya disebut Limit tetap terisi pada harga pasar walau melampaui angka yang dimasukkan; penamaan/preview dan penanganan data lama partial fill menunggu klarifikasi/analisis; tidak menghapus riwayat atau merusak ledger.
- S02 rincian 8: pemilik memilih tolak order saham yang disuspensi ketika bursa buka dan lepaskan dana/saham. Situasi invalid lain masih memerlukan kebijakan.
- S02 rincian 10: pemilik memilih ringkasan hasil order dalam game dengan akses riwayat setelah melihat contoh; ikuti KP08 untuk daftar seluruh kejadian ketika kembali. Contoh di `Artifacts/Preview-Saran-2026-10-28/` memakai data ilustrasi. Tiga contoh alternatif status order ada di `Artifacts/Preview-Saran-2026-10-28/status-order-alternatif.png`; pemilik memilih A tahapan. Batas dana Market disarankan game dan dikonfirmasi pemain; rumus dan penanganan ketika tidak mencukupi masih menunggu keputusan.
- S03: pemilik memilih contoh halaman pantau order sebagai dasar; status mengantre, terisi, dibatalkan dan bermasalah dapat difilter berdasarkan data order asli. Integrasikan dengan A10/A11, bukan fitur duplikat.
- S07: pemilik memilih contoh grafik kekayaan dengan pilihan periode sebagai dasar; gunakan hanya histori nyata, tampilkan state belum cukup data dan gabungkan dengan A09.
- S09: pemilik memilih pencarian, kategori dan filter kepemilikan sebagai dasar; ini hanya persetujuan fitur filter, bukan pilihan desain galeri. Tiga alternatif galeri tetap wajib.
- S10: pemilik menyukai contoh halaman IPO yang menyatukan fase, kalender dan status pesanan serta meminta menu untuk informasi tambahan perusahaan. Pemilik memilih contoh **B — informasi bertab** sebagai dasar: Profil, Keuangan, Risiko dan Rencana IPO, berdasarkan preview `Artifacts/Preview-Saran-2026-10-28/info-perusahaan-ipo.png`. Gunakan data domain yang benar-benar tersedia dan tampilkan “Data belum tersedia” untuk data yang belum ada; jangan mengarang profil, angka, risiko atau rencana. Preview perubahan lain dan state terkait tetap perlu persetujuan pemilik. Contoh ilustrasi terpisah di `Artifacts/Preview-Saran-2026-10-28/index.html`; belum disetujui, tidak mengubah game/save. S02 opsi A riset/kelola vs B persiapan order adalah pilihan D02. S09 hanya contoh filter dan tidak menggantikan kewajiban tiga alternatif desain galeri. S10 memakai tanggal contoh; hanya gunakan data domain nyata dalam game.
- Setiap perubahan tampilan tetap menunggu preview dan jawaban eksplisit. Keputusan D01 rincian kalender/D03/D04/D06 masih terbuka; penanda harga simulasi dipilih dihapus setelah preview; D07 sudah dipilih secara produk tetapi implementasi izin/perangkat belum teruji.
- Keputusan D05: pemilik memilih daftar seluruh kejadian sebagai dasar setelah melihat dua alternatif, menggunakan data aktual sesuai yang tersedia. D02 arah dasar telah dipilih: order saat tutup diteruskan ketika buka; rincian perilaku masih menunggu keputusan. D07 memilih notifikasi IPO di ponsel dengan kontrol per jenis; detail izin dan keandalan Android masih perlu diuji. D01 prinsip kalender lebih sering buka dipilih, rincian jadwal menunggu keputusan; D03, D04 dan D06 masih menunggu keputusan.
- Paket pertama yang ditunjuk pemilik: belum diisi.
- Izin perangkat/publikasi: mengikuti batas `AGENTS.md` sampai ada instruksi eksplisit yang relevan.
