# Draf plan pemegang saham dan IPO perusahaan pemain

**Status: PERSIAPAN — 20 JAWABAN DITERIMA, SEBAGIAN RINCIAN MASIH MENUNGGU.** Keputusan yang dipilih dirangkum di master plan offline bagian 2A-1. Ini draf kebutuhan, bukan izin implementasi atau klaim fitur sudah lengkap. Hindari duplikasi; audit source/caller, save dan fitur existing sebelum merancang perubahan. Lakukan pengujian dengan save QA terpisah.

## Empat area yang diminta

1. **Pemegang saham perusahaan.** Tinjau dan tingkatkan daftar pemegang tercatat pada detail emiten: jumlah saham, persentase, waktu, pemain dan status cakupan. Data yang belum diketahui tidak boleh direkayasa; data game tidak diklaim sebagai data pasar nyata. Tentukan ruang lingkup pemegang dan desain berdasarkan jawaban pemilik.
2. **Dampak kepemilikan besar.** Petakan RUPS satu saham satu suara dan dividen existing, bedakan persentase satu perusahaan dari dominasi sektor >25%. Tentukan ambang/hak/biaya/risiko setelah keputusan pemilik; jangan menjanjikan kendali tanpa mekanisme yang terbukti. Lengkapi batas, perubahan saat saham berkurang, pemisahan akun pribadi/dana klien dan validasi jumlah saham.
3. **IPO perusahaan lain.** Audit pusat IPO, jadwal, pemesanan, dana tertahan, penjatahan dan refund. Aturan fixed 50% pada jalur yang ditemukan perlu diuji dan dinilai. Gunakan halaman fase/kalender/status dan informasi perusahaan bertab Profil/Keuangan/Risiko/Rencana IPO yang sudah dipilih; jawaban pemilik menentukan perilaku. Semua tampilan baru memerlukan preview.
4. **IPO usaha sendiri.** Audit apakah ada alur existing; sambungkan usaha milik pemain ke penawaran/listing dan perdagangan hanya setelah syarat, valuasi, penawaran, struktur modal, kontrol, waktu, biaya, arus dana, pemegang saham dan interaksi pemain diputuskan. Modelkan dana hasil ke akun usaha atau pihak yang berhak sesuai transaksi; jangan menghitung uang/saham ganda.

## Perlindungan, kesiapan dan bukti

- Pertahankan dunia 24×, order saat pasar tutup menunggu sesi buka, keputusan transaksi penuh atau tidak terisi dan persetujuan preview UI; pertanyaan rinci yang belum dijawab tetap terbuka. Jangan menambah atau mengubah tarif/hadiah tanpa persetujuan.
- Pemegang lama/riwayat parsial harus ditandai apa adanya. Tidak mengarang nilai, tanggal, kepemilikan atau pendapatan.
- Preview setiap perubahan tampilan sebelum penerapan, minta keputusan melalui pilihan klik; saat menunggu tetap kerjakan task independen. Rancangan ini belum menjadi persetujuan atas tampilan.
- Per area buat ID subtask, skenario positif/negatif, boundary ambang, penjualan di bawah ambang, transaksi ditolak, simpan/restore, offline/catch-up/idempotensi, saldo/reservasi/ledger, saham beredar/dilusi, konflik dan rollback; uji integrasi UI/bridge/domain serta regresi terkait. Jangan klaim lulus jika pengujian, approval, Android fisik atau data tak tersedia.
- Target progres pemain dipertahankan sebagai sasaran playtest 1–3 hari nyata untuk pencapaian besar pertama, bukan instruksi mengunci setiap fitur dan bukan hasil terukur. Periksa dampak keputusan IPO pada keseimbangan.
- Hubungkan draf ini ke master plan offline dan masukan pemain setelah jawaban; hindari menimpa tracker atau menganggap semua paket diberi mandat.

## Keputusan pemilik

Dua puluh jawaban telah diterima; Q02/Q06/Q11/Q18 meminta contoh atau rekomendasi sebelum keputusan final. Keputusan lanjutan belum otomatis disetujui. Nomor 1–20 harus dicatat satu per satu berdasarkan jawaban eksplisit; jika meminta contoh, buat preview/ilustrasi dulu dan minta pilihan ulang. Jangan menganggap opsi awal sebagai persetujuan.

- **Q01 — Dipilih pemilik:** tampilkan pemegang dengan kepemilikan **1% atau lebih**, serta tampilkan kepemilikan publik. Pembulatan tidak boleh mengubah ambang. Definisi publik, penanganan individu di bawah 1%, treasury, kepemilikan pemain, dan saham belum terpetakan harus ditentukan dari data domain; jangan otomatis menyamakan saham yang belum diketahui dengan publik atau menciptakan pemilik. Preview UI dan uji batas 0,99%/1% tetap wajib.

## Jawaban pemilik Q01–Q20 (28 Oktober 2026)

| No | Jawaban pemilik | Status dan batas |
|---|---|---|
| Q01 | Pemegang 1% atau lebih dan kepemilikan publik | Dipilih; pisahkan saham belum terpetakan dari publik. |
| Q02 | NPC memiliki beragam saham sesuai kekayaannya | Arah dipilih; rincian alokasi dan data legacy masih perlu keputusan. |
| Q03 | Urutkan persentase terbesar ke terkecil | Dipilih; aturan ambang gunakan presisi sebelum pembulatan. |
| Q04 | Nama, jumlah saham dan persentase | Dipilih; tampilan harus dipreview. |
| Q05 | Perbarui saat halaman dibuka atau diperbarui | Dipilih; tunjukkan waktu pembaruan dan status kesalahan. |
| Q06 | Hak suara, usulan agenda dan kendali bertahap | Dipilih; ambang dan rincian hak masih terbuka. |
| Q07 | Beberapa tingkatan kepemilikan bertahap | Dipilih; ambang dan hak rinci belum ditetapkan. |
| Q08 | Hak langsung menyesuaikan ketika kepemilikan menurun | Dipilih; hak yang telah dipotret pada tanggal pencatatan rapat perlu kebijakan khusus, jangan merusak keputusan historis. |
| Q09 | Suara berdasarkan jumlah saham yang berhak | Dipilih; gunakan snapshot hak yang valid. |
| Q10 | Saham akun klien terpisah dari milik pribadi untuk kendali | Dipilih; uji isolasi akun. |
| Q11 | Pesanan minimal 20% disebut besar; kelebihan pesanan menggunakan alokasi awal 60% kelompok besar dan 40% kelompok lain; sisa kelompok dipindahkan sesuai kebutuhan. | Kedua kelompok membagi proporsional; sisa lot kepada pemesan dengan pecahan terbesar dan seri diundi. Pembulatan 60:40 antarkelompok memakai pecahan terbesar. |
| Q12 | Dana tidak terpakai dikembalikan otomatis ke saldo tersedia | Dipilih; uji akurasi dan idempotensi. |
| Q13 | Profil, keuangan, risiko dan rencana IPO | Dipilih; konsisten dengan tab IPO B, data aktual saja. |
| Q14 | Pembatalan selama penawaran sebelum penjatahan | Dipilih; validasi periode dan pelepasan dana. |
| Q15 | Saham IPO perusahaan lain tercatat di pasar game dan dapat diperdagangkan | Dipilih; uji waktu pencatatan dan status. |
| Q16 | Perusahaan pemain bisa IPO | Dipilih; belum terbukti implementasi existing. |
| Q17 | IPO sendiri setelah syarat usia, keuangan dan kesiapan | Dipilih; rincian angka/aturan belum ditentukan. |
| Q18 | Pemain memilih sendiri jumlah saham baru yang ditawarkan tanpa saran game. | Dipilih; batas penawaran dan rincian aturan masih perlu diputuskan. |
| Q19 | Dana hasil penerbitan saham baru masuk kas perusahaan | Dipilih untuk penerbitan saham baru; penjualan saham existing adalah kasus berbeda, belum dipilih. |
| Q20 | Pemain selalu memiliki kendali penuh setelah perusahaan sendiri IPO | Dipilih; aturan hak suara/kendali ketika kepemilikan terdilusi dan konsistensi Q09 perlu dirancang dan ditunjukkan untuk keputusan, tanpa mengasumsikan pemegang lain tidak punya hak. |

**Arah Q20:** kendali penuh meski kepemilikan berkurang adalah keputusan game pemilik, bukan otomatis gambaran hukum/realitas semua perusahaan; tampilkan alternatif desain yang konsisten agar voting pemegang lain tetap bermakna. Tidak mengambil keputusan formula, persentase atau manfaat tanpa jawaban lanjut.

Preview yang ditunjukkan kepada pemilik: `Artifacts/Preview-Saran-2026-10-28/pemegang-alternatif.png`, `ipo-perbandingan.png`; alternatif rekomendasi kejadian pemegang saham telah dijelaskan. Empat pertanyaan lanjutan Q02/Q06/Q11/Q18 dikirim; pilihan masih menunggu. Angka dan nama dalam preview adalah ilustrasi.

## Jawaban lanjutan

- Q02: pemilik menolak contoh A/B bagian belum terpetakan dan meminta contoh baru. Tidak boleh mengubah yang belum diketahui menjadi publik tanpa bukti.
- Q06: pemilik memilih hak suara, usulan agenda dan kendali bertahap; ambang, konsekuensi, syarat dan alur voting masih perlu diputuskan.
- Q11: pemilik telah memilih ambang 20%, alokasi awal 60:40 dan pengalihan jatah yang tidak terpakai; kedua kelompok membagi jatah sesuai besar pesanan. Sisa lot diberikan berdasarkan pecahan terbesar, seri pemesan diundi; alokasi tidak boleh melebihi jumlah saham tersedia dan sisa dana dikembalikan otomatis sesuai Q12.
- Q18: pemilik memilih menentukan jumlah saham baru sendiri tanpa saran game; batas penawaran dan detailnya masih terbuka.

Preview lanjutan Q02: `Artifacts/Preview-Saran-2026-10-28/pemegang-baru.png`; Q11/Q18: `ipo-aturan-baru.png`. Semua angka/nama ilustrasi. Pertanyaan lanjutan tentang pilihan pemilik telah dikirim dan menunggu jawaban. Q06 sudah dipilih: hak suara, usulan agenda dan kendali bertahap, rincian ambang tetap terbuka.

- **Q02 arah baru:** pemilik menginginkan NPC yang telah diciptakan memiliki beragam saham sesuai kekayaannya; bukan meminta mengganti label unknown dengan nama rekaan. Inventaris source 28 Oktober: 20 NPC roster liga dengan akun canonical yang didanai, serta 2 NPC agen pasar terpisah (market maker dan ritel). NPC liga memiliki order periodik melalui mesin matching, tetapi belum dibuktikan masing-masing sudah punya posisi pada semua saham atau dapat menutupi seluruh saham beredar. Rancang alokasi saham/saldo/ledger yang konsisten dan migrasi tanpa mengarang kepemilikan untuk data legacy; detail distribusi dan preview menunggu keputusan pemilik. Angka ini hasil pembacaan source, bukan jumlah runtime yang diukur.

## Tambahan permintaan: 1.000 NPC pasar dan leaderboard

**Status: kebutuhan baru, belum diimplementasikan.** Pemilik meminta tepat **1.000 NPC unik** yang aktif bermain di pasar, mengisi leaderboard yang sama dan masing-masing mempunyai profil ringkas yang dapat dibuka dengan gambaran keseluruhan NPC. Pemain tetap peserta terpisah; jangan menafsirkan permintaan ini sebagai 1.000 NPC tambahan di atas roster lama. Pemilik memilih peringkat mengikuti hasil nyata, sedangkan strategi dan keputusan NPC beragam; aktivitas bervariasi dan tidak semua NPC bertransaksi setiap saat. Isi dan desain profil masih menunggu pilihan pemilik setelah melihat contoh. Semua perubahan UI wajib ditunjukkan dalam preview dan disetujui sebelum diterapkan; pekerjaan independen tetap dapat dilanjutkan.

### Kondisi source yang perlu diperhatikan

- `LeagueManager` membuat 20 peserta bawaan, menghubungkan akun NPC ke ledger, menilai kas dan posisi IDR, dan menghasilkan usulan order berkala lewat mekanisme pasar. Ini bukti pembacaan source, bukan bukti bahwa 1.000 NPC sudah berjalan.
- Pemulihan liga saat ini memeriksa jumlah roster terhadap `DEFAULT_NAMES.length` dan daftar ID yang telah dikenal. Perlu rencana perubahan dan uji save lama agar penambahan peserta tidak membuat restore gagal atau melipatgandakan modal.
- Agen pasar yang ada harus diinventarisasi dan perannya dibedakan dari peserta liga; jangan menghitung satu akun dua kali atau diam-diam membuat NPC tambahan. Jangan menganggap kepemilikan saham legacy yang tidak terpetakan sebagai milik NPC tertentu.

### Cakupan pekerjaan untuk agent

1. **Inventaris alur:** petakan peserta, akun, seed modal, transaksi, harga, pemegang saham, peringkat, histori, save, bridge, UI dan rekonsiliasi offline existing; gunakan alur yang sudah ada bila memenuhi kebutuhan.
2. **Populasi:** buat 1.000 identitas NPC unik yang tetap sama setelah save/restart, termasuk kebijakan untuk 20 NPC lama; pastikan pemain dan setiap NPC muncul hanya sekali di peringkat.
3. **Kekayaan dan portofolio:** tetapkan nilai awal dan distribusi yang masuk akal, dengan kas dan saham tercatat pada sumber data ekonomi game; jumlah saham tidak boleh melebihi saham beredar. Hitung kekayaan dari data yang benar, bukan angka profil rekaan.
4. **Aktivitas pasar:** rancang aktivitas beragam untuk seluruh populasi dalam waktu game sesuai jawaban pemilik, dengan validasi saldo, saham, sesi pasar dan aturan order yang berlaku. Catat bukti aktivitas nyata per NPC selama jendela pengujian yang disepakati; jangan mengklaim semua aktif hanya karena terdaftar.
5. **Kompetisi:** kaitkan peringkat dengan aturan kompetisi yang disetujui, tentukan urutan saat nilai sama, serta jelaskan bagaimana unsur acak memengaruhi perilaku tanpa menciptakan hasil transaksi atau kekayaan palsu.
6. **Profil NPC:** siapkan preview profil ringkas yang dapat dibuka dari leaderboard dan pemegang saham. Tampilkan informasi yang pemilik pilih dari data yang benar-benar tersedia; bila suatu jenis data belum dimodelkan, nyatakan belum tersedia, jangan mengarang.
7. **Pemegang saham:** gunakan posisi NPC yang benar untuk daftar pemegang dan ambang 1%; tampilkan publik dan data belum terpetakan secara konsisten. Rincian klasifikasi dan kepemilikan awal tetap memerlukan desain dan keputusan pemilik.
8. **Kinerja:** ukur respons layar, waktu pemrosesan pasar/catch-up, ukuran save dan memori dengan 1.000 NPC dan perangkat/lingkungan uji yang disebutkan; optimalkan bagian yang terbukti lambat. Jangan menganggap pengujian desktop sebagai bukti performa perangkat Android.
9. **Offline dan ketahanan:** uji aktif, aplikasi ditutup dan kembali, save/restart, pemrosesan ulang, transaksi ganda, perubahan waktu dan pemulihan; pastikan saldo, posisi, order, peringkat, dan hasil acak yang dipakai tetap konsisten serta tidak ada data hilang.
10. **Regresi dan keseimbangan:** uji harga/saham/saldo, order pemain, IPO, pemegang saham, liga dan ekonomi; lakukan playtest progres aktif, bisnis dan santai, termasuk sasaran pencapaian besar pertama 1–3 hari nyata. Laporkan hasil dan penyimpangan, bukan sekadar menulis bahwa target terpenuhi.
11. **Bukti penyerahan:** cantumkan daftar task dan status, data uji terisolasi, jumlah NPC unik serta yang benar-benar melakukan aktivitas, hasil uji, batasan, preview dan keputusan pemilik, serta bedakan build APK dari pengujian Android fisik.

### Keputusan yang masih terbuka

- **Diputuskan:** strategi dan keputusan NPC beragam; peringkat mengikuti hasil nyata, bukan diacak.
- Pemilik memilih **B — informasi dibagi dalam tab** pada preview `Artifacts/Preview-Saran-2026-10-28/profil-npc-pilihan.png`. Informasi wajib dan detail tampilan masih menunggu pilihan/preview; contoh berisi data ilustrasi.
- **Diputuskan:** aktivitas bervariasi; tidak semua NPC bertransaksi setiap saat. Jendela pengamatan dan kriteria pembuktian aktivitas masih perlu dirancang; jangan menjanjikan 1.000 transaksi serentak.
- Tentukan secara eksplisit migrasi populasi dan saldo untuk save lama, ukuran periode pembuktian aktivitas, serta dampak ekonomi setelah audit. Hindari reset atau perubahan progres otomatis.
