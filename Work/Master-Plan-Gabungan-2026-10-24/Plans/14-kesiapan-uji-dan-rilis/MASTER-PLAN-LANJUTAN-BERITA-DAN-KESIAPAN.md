# Master Plan Tindak Lanjut: Berita, Batas Harga, dan Kesiapan Uji

Tanggal: 29 Oktober 2026  
Status: DRAF UNTUK DITINJAU — BUKAN IZIN EKSEKUSI

## 1. Tujuan dan prioritas

Susun pekerjaan lanjutan untuk memperbaiki temuan Plan 14 dan menerapkan keputusan terbaru pemilik tentang harga saham dan berita berbayar. Pembuatan dokumen ini tidak memulai perubahan kode, pembuatan APK, instalasi atau pengujian perangkat. Pemilik akan menentukan kapan pekerjaan dimulai dan plan mana yang ditunjuk.

Root produksi: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`. Baca `AGENTS.md`, Plan 14, master plan sebelumnya, kode aktual, dan bukti pengujian; instruksi terbaru pemilik serta keputusan dalam dokumen ini mengungguli aturan lama yang bertentangan. Rujukan lama tetap menjadi riwayat, bukan alasan menjalankan batas +80%/-40%, menerobos batas bursa, menggunakan acuan harga buka, atau menjamin target harus selesai pada penutupan bursa berikutnya.

Jangan reset, clean, stash, hapus pekerjaan lokal atau ubah save pemain. Save `Artifacts/LocalPreview/preview-save/save_game_v2.json` hanya boleh di-hash; seluruh tes menggunakan save QA terisolasi. Jangan ADB, instalasi, penghapusan aplikasi, migrasi save, distribusi atau publikasi tanpa izin yang berlaku; tindakan pada ponsel memerlukan izin per tindakan. Setiap perubahan layar/teks/alur memerlukan preview dan persetujuan spesifik, kecuali yang sudah disetujui secara khusus.

## 2. Keputusan produk terbaru

1. Semua saham memiliki batas kenaikan dan penurunan harian masing-masing 25%, dihitung dari harga penutupan hari bursa sebelumnya. Tidak ada pengecualian otomatis bagi berita Whale. Fraksi harga dan aksi korporasi perlu ditangani tanpa melewati batas; mekanisme kasus tanpa harga valid harus ditentukan sebelum implementasi terkait.
2. Berita Pro dan Whale yang memenuhi aturan paket menjanjikan **kenaikan minimum yang wajib tercapai**. Acuan adalah harga penutupan sebelum berita terbit. Rumor lanjutan menggunakan harga penutupan sebelum rumor lanjutan. Berita umum tidak otomatis dijamin. Target harga bukan jaminan laba pemain.
3. Target yang secara matematis bisa dicapai dalam tiga hari bursa wajib tercapai paling lambat dalam tiga hari bursa setelah berita. Jika tidak bisa akibat batas 25%, tenggat diperpanjang ke jumlah hari bursa minimum yang memungkinkan, dengan perhitungan bertahap dan fraksi harga yang valid. Angka berita adalah minimum, bukan batas atas. Jangan menyatakan angka pasti jika fraksi harga membuat perhitungan berbeda.
4. Harga target yang berada di antara fraksi valid dibulatkan ke atas agar minimum terpenuhi. Batas harian juga harus dibulatkan ke harga valid di dalam batas; jangan membulatkan batas atas ke luar 25% atau batas bawah ke bawah 25%.
5. Dua berita Pro per hari bursa, masing-masing minimal 2–5%; emiten yang sama boleh muncul jika memiliki alasan game. Whale eksklusif 1–2 per tujuh hari game dengan kenaikan di atas 20%; jika perlu, siapkan kejadian simulasi masuk akal agar ada sedikitnya satu berita. Rumor lanjutan minimal sekali per 14 hari game, besaran minimum dua kali persentase rumor awal. Frekuensi ini diukur pada waktu game dan tetap ditangani saat catch-up.
6. Berita bertentangan untuk emiten sama menunggu target pertama selesai; berita untuk emiten berbeda berjalan tanpa saling menunggu. Rumor lanjutan Whale yang berhubungan adalah pengecualian dan memakai acuannya sendiri. Pelaksanaan harus menjaga kedua target tetap tercatat dan tidak menggandakan transaksi atau melanggar batas 25%.
7. Pemilik meminta tambahan catatan pada berita untuk target yang membutuhkan lebih dari tiga hari; teks, waktu muncul dan tampilan final masih menunggu persetujuan. Usulan: “Target kenaikan minimal [persentase]%. Karena batas kenaikan harian 25%, target membutuhkan sedikitnya [jumlah] hari bursa.” Jumlah harus hasil perhitungan game, bukan teks statis.
8. Dunia 24×, jam bursa 09.00–16.00 setiap hari game, paket dan biaya, autosave, persetujuan UI, perlindungan progres, serta fitur lain yang tidak diubah pemilik tetap mengikuti keputusan terbaru dan aturan proyek. Durasi efek berita 60/120 menit nyata yang sebelumnya disetujui perlu direkonsiliasi dengan jaminan target lintas hari; jangan menyamakan berakhirnya efek dengan tercapainya target tanpa bukti.

## 3. Baseline dan status pekerjaan terdahulu

- Catatan sesi: perbaikan pembatalan perpanjangan dan alur konfirmasi pemulihan sudah diterapkan di source; rangkaian terpilih menghasilkan `OK (52 tests)` pada log `Artifacts/Codex-Fix-Recovery-Verified-2026-10-29/junit-latest.log`. Ini bukti host untuk snapshot tersebut, bukan bukti menyeluruh, tampilan ponsel, atau bukti APK baru. Verifikasi ulang pada snapshot eksekusi.
- Pemilik menyetujui secara khusus teks dan alur pemulihan cadangan serta tombol penghentian perpanjangan dengan konfirmasi; periksa kesesuaian implementasi dan uji, tanpa menganggap semua layar lain disetujui.
- Jadwal/target berita Whale, target lintas hari, batas harga terbaru, dan penanganan konflik belum terbukti selesai. Cek kode dan tes, bukan hanya laporan.
- APK-15 pada pemeriksaan 29 Oktober berukuran 16.404.438 byte dengan SHA-256 `9b5adc56fedfa05a7da1c00b46a2e1223bef52cda28be139f1100e63adf16b0c`, berbeda dari nilai dalam laporan lama. Ini perbedaan bukti, bukan bukti penyebab. Audit identitas berkas dan catat snapshot; jangan timpa kandidat lama.
- Hash save pemain pada pemeriksaan penyusunan plan: `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`. Bandingkan kembali sebelum dan sesudah eksekusi tanpa membuka isi.

## 4. Paket kerja dan kriteria penerimaan

### T01 — Baseline dan pelacakan

Verifikasi root, branch/HEAD, perubahan lokal, aturan aktif, hash save, hasil tes dan identitas APK yang tersedia. Petakan setiap butir T02–T08 ke kode aktual, caller, tes dan status. Bedakan pekerjaan yang sudah diterapkan dari yang hanya direncanakan. Jangan menimpa perubahan yang sudah ada.

**Terima jika:** baseline terdokumentasi, perbedaan APK tercatat dengan hash dan ukuran aktual, serta semua pengujian memakai data QA terpisah.

### T02 — Perpanjangan dan pemulihan

Telusuri urutan penghentian auto-renewal, jatuh tempo, save dan reload untuk Pro dan Whale. Pastikan akses berbayar bertahan sampai masa berakhir, tak ada debit setelah pembatalan, dan saldo tidak negatif. Uji save utama rusak, tawaran cadangan yang valid, penolakan, persetujuan, kegagalan pemulihan dan larangan timpa diam-diam. Tampilkan waktu cadangan dan risiko kehilangan progres; verifikasi bahwa game tidak berjalan atau menyimpan di atas data yang membutuhkan keputusan.

**Terima jika:** tes regresi reproduksi lulus, tidak ada debit/pemulihan tanpa persetujuan, dan hasil UI yang disetujui dibuktikan sesuai permukaan pengujian yang tersedia.

### T03 — Batas harga 25%

Telusuri semua jalur pembentukan harga, order, eksekusi, NPC, penutupan/pembukaan, split dan pemulihan save. Terapkan aturan pada tempat bersama yang mencegah transaksi atau pembaruan harga melewati batas dan tetap konsisten dengan fraksi. Jangan membatasi hanya tampilan atau mengubah harga sesudah ledger/transaksi dieksekusi. Pastikan order lama/antrean saat hari berganti tidak mengeksekusi di luar batas baru.

**Terima jika:** uji harga tepat batas, satu fraksi di luar batas, beli/jual, order antre, pergantian hari, beberapa kelas harga, aksi korporasi dan restart; ledger serta harga transaksi tetap konsisten. Kasus tanpa harga valid tidak diputuskan sepihak.

### T04 — Target berita yang tersimpan dan terjamin

Rancang penyimpanan acuan harga, target minimum, berita/emiten, tenggat, progres dan hasil; pakai pola save yang ada dan jaga kompatibilitas versi lama. Hitung target dengan angka desimal dan fraksi valid. Hitung tenggat tiga hari atau hari minimum yang realistis berdasarkan batas dan fraksi. Pastikan target dapat tercapai melalui mekanisme pasar yang konsisten dengan order, ledger dan posisi; jangan sekadar mengubah angka penutupan atau melaporkan hasil rekaan. Jelaskan bila keterbatasan likuiditas membuat jaminan tidak mungkin dan selesaikan mekanisme sebelum menyatakan lulus.

**Terima jika:** pada acuan Rp10.000 target +30% minimal Rp13.000, dengan pergerakan harian maksimal 25%; target +100% butuh sedikitnya empat hari sebelum penyesuaian fraksi. Uji batas, pembulatan, tidak terpenuhi, persistensi, restart, catch-up berulang, tanggal tenggat, dan tidak ada eksekusi ganda; uji kedua paket.

### T05 — Frekuensi berita dan konflik

Hubungkan berita yang diterbitkan ke kejadian game yang nyata dalam simulasi, target dan status. Jadwalkan dua Pro harian, 1–2 Whale tiap tujuh hari, dan rumor lanjutan minimal sekali tiap 14 hari game. Hindari duplikasi setelah restart/catch-up; berikan alasan game bila emiten Pro sama. Antrekan berita bertentangan pada emiten sama sampai target pertama selesai; emiten berbeda tetap independen. Rumor lanjutan terkait adalah pengecualian; terapkan dua kali persentase awal dengan acuan penutupan sebelum rumor.

**Terima jika:** simulasi beberapa jendela 7/14 hari beserta batas jendela, restart/offline, target aktif bersamaan, antrean dan pengecualian terbukti dengan data QA; jumlah berita bukan sekadar angka yang ditampilkan.

### T06 — UI, riwayat dan informasi

Siapkan preview catatan target lebih dari tiga hari serta contoh informasi dan riwayat berita (terbit, target minimum, acuan, tenggat, progres, tercapai/menunggu) berdasarkan data domain aktual. Minta persetujuan pemilik per layar dan teks sebelum menerapkan detail baru. Jangan menjadikan contoh yang ditulis dalam dokumen ini sebagai persetujuan. Pertahankan tombol kelola paket dan pemulihan yang telah disetujui; uji keadaan kosong, gagal, teks besar dan layar ponsel.

**Terima jika:** preview dan persetujuan dicatat per layar, informasi berasal dari status sebenarnya, kontrol dapat diakses, dan bukti browser/perangkat diberi label sesuai lingkungan.

### T07 — Regresi, offline, keamanan progres dan performa

Jalankan tes yang relevan untuk ekonomi, order, waktu, berita, save, bridge dan UI serta pemeriksaan sintaks. Uji ulang sesuai perubahan dan catat perintah, hasil, kegagalan dan keterbatasan. Gunakan data QA untuk simulasi hari bergulir dan catch-up, termasuk 1.000 NPC bila berdampak. Verifikasi hash save pemain tetap sama. Uji pada perangkat hanya jika ada izin tindakan; proses background nyata dan catch-up bukan bukti yang saling menggantikan.

**Terima jika:** setiap syarat yang diklaim memiliki bukti relevan, tidak ada bug diketahui yang belum selesai untuk kandidat pribadi, dan setiap pengujian yang belum dilakukan diberi status jelas.

### T08 — Identitas APK dan penyerahan kandidat

Inventaris APK terdahulu: hash, ukuran, application ID, versionCode, signer, waktu dan bukti asal yang tersedia; jangan menganggap nama menjamin isi atau mengarang riwayat. Setelah syarat dan izin dipenuhi, buat APK kandidat dari snapshot yang diperiksa, cek identitas/signature dan kecocokan pembaruan, simpan tanpa menimpa kandidat lama. Jika signature berbeda, siapkan opsi penyelamatan progres untuk ditinjau; jangan uninstall. Sertakan panduan instalasi dan checklist, hasil uji, perubahan, risiko dan keputusan yang belum dibuat.

**Terima jika:** bukti build, signature, host, browser, instalasi, emulator dan ponsel dilaporkan terpisah; jika belum diuji, tulis belum diuji. Kandidat debug berlabel uji pribadi; bukan pernyataan siap rilis publik.

## 5. Ketergantungan dan pertanyaan yang masih terbuka

- T01 mendahului perubahan; T03 dan T04 saling terkait, lalu T05; T06 membutuhkan persetujuan preview; T07 memverifikasi integrasi; T08 setelah syarat kandidat terpenuhi. T02 dapat dikerjakan secara independen setelah baseline.
- Pemilik belum menyetujui isi, kondisi tampil, dan desain catatan di berita. Siapkan preview dan minta persetujuan.
- Tentukan bersama pemilik jika ditemukan ketidakmungkinan karena fraksi, suspensi, aksi korporasi, likuiditas atau beberapa target serentak; jangan mengarang solusi ekonomi atau melanggar 25%. Rumor lanjutan adalah pengecualian antrean tetapi bukan izin mengabaikan target terdahulu.
- Pastikan dengan pemilik bagaimana menghitung hari pertama tenggat jika berita terbit setelah bursa tutup, serta apakah target dianggap tercapai saat harga menyentuhnya atau harus pada harga penutupan. Keputusan sebelumnya mengarah ke harga penutupan; jangan ubah asumsi tanpa konfirmasi.
- Tentukan cara menggabungkan deadline efek berita 60/120 menit nyata dan kewajiban target lintas hari tanpa menghilangkan aturan sebelumnya. Mintakan keputusan jika keduanya tidak bisa berjalan bersamaan.
- Keputusan visual lain yang masih menunggu preview, penanganan save replay lama, dan izin perangkat tetap terbuka; jangan memakainya untuk menghentikan tugas lain yang independen.

## 6. Format laporan pelaksanaan

Untuk setiap T01–T08: status (selesai/sebagian/menunggu/tidak diuji), perubahan aktual, bukti tes dan lingkungan, keputusan/izin tertunda, risiko, dan langkah berikutnya. Lampirkan daftar file, hash save sebelum/sesudah, identitas APK serta hasil setiap gate. Jangan menyebut plan selesai hanya karena dokumen, kode, tes host atau APK tersedia.
