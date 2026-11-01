# Long task: revisi APK-11 dan penguatan Game Saham

**Status rencana:** SIAP DITUGASKAN, belum ada bukti eksekusi. **Dibuat:** 28 Oktober 2026. Satu paket berisi **8 tahap dan 32 task** `LT-01`–`LT-32`; jumlah ini adalah cakupan rencana baru, bukan jumlah task yang telah selesai. Gunakan file ini sebagai plan yang ditunjuk pemilik, bukan instruksi untuk melanjutkan plan lain otomatis.

## Tujuan dan sumber

Perbaiki temuan review APK-11, serta lakukan tujuh penguatan yang disetujui pemilik: konsistensi transaksi, penanganan aksi ganda/gangguan, data dan aktivitas 1.000 NPC, pengukuran serta optimisasi berbasis bukti, IPO end-to-end, tes dan pelaporan yang tuntas, serta preview UI dan persetujuan. Pemetaan kebocoran informasi terminal adalah pemeriksaan dan persiapan; bukan mandat mengimplementasikan terminal premium secara penuh atau menentukan sendiri aturannya.

Sebelum mulai, baca `AGENTS.md`, laporan `Artifacts/Review-Indep-APK11-2026-10-28/LAPORAN-REVIEW.md`, kode probe `ProbeAon.java` dan `ProbeClosedMarket.java` di folder review itu, `Plans/10-revisi-apk10-dan-riset/plan.md`, `MASTER-PLAN-OFFLINE-DAN-REALISME-2026-10-28.md`, serta `Work/MASTER-PLAN-PERUSAHAAN-DAN-TERMINAL-2026-10-28.md`. Periksa perubahan dan hasil agent terbaru; laporan review adalah snapshot, bukan jaminan bug masih sama. R01–R16 dan plan perusahaan adalah referensi/arah yang sudah disetujui sebagai dasar, bukan izin untuk mengerjakan seluruh fitur tambahannya di luar 32 task ini.

## Batas dan keputusan pemilik

- Root produksi `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`; folder `Game Trading Saham` adalah proyek berbeda. Pertahankan perubahan lokal dan hasil pekerjaan agent; tidak reset/clean/menghapus secara massal. Hindari menimpa pekerjaan agent lain, cek status aktif dan koordinasi checkpoint bila masih ada writer.
- Save pemain `Artifacts/LocalPreview/preview-save/save_game_v2.json` hanya diperiksa hash; seluruh tes memakai save QA baru yang terisolasi di `Artifacts/Longtask-APK11-2026-10-28/`. Tidak melakukan reset progres pemain, instalasi, ADB baru, publish/push, atau perubahan konfigurasi global; gunakan bukti perangkat yang sudah ada bila relevan dan jelaskan batasnya. Otorisasi user untuk rilis awal tidak otomatis menjadi izin untuk menghapus file save saat audit ini.
- User memilih transaksi pemain terisi **seluruhnya atau tidak terisi**, harga sesuai keputusan terbaru; jangan mengubah aturan lain secara diam-diam. Audit kedua arah serta order yang mengantre. Order saat pasar tutup disimpan untuk sesi buka, reservasi berlaku, dapat dibatalkan; jika saldo tak cukup ketika diproses, order gagal dan reservasi dilepas. Jangan menambahkan klaim jaminan harga atau aturan produk yang belum diputuskan.
- Waktu otomatis 24× pada aktif/offline; 1 hari game = 1 jam nyata. Efek berita 60/120 menit nyata tetap. Akses awal terminal 1 hari game sebelum rilis; konten informasi benar namun harga tidak pasti. Formula, harga layanan, cakupan berita dan dampak belum diputuskan.
- IPO lain: jika saham mencukupi, seluruh pesanan sah dipenuhi; jika kelebihan pesanan, ambang ≥20%, kuota awal 60:40, pembagian proporsional, sisa dipindahkan, lot dengan pecahan terbesar, dan seri diundi. Jaga jumlah saham dan uang, hasil undian konsisten pada retry/restore. Ini aturan game, bukan klaim aturan resmi BEI/OJK. Masa pembatalan hanya selama penawaran sebelum penjatahan; tanggal/jam fase yang belum jelas perlu keputusan pemilik, jangan disamakan diam-diam dengan listing.
- Setiap perubahan UI, teks, navigasi, alur, atau tampilan wajib preview yang benar-benar terlihat dan persetujuan eksplisit pemilik sebelum diterapkan. Buat pilihan tanya-jawab otomatis jika tersedia (termasuk 'belum cocok/buat ulang'); jika tidak tersedia, berikan pilihan jelas tanpa mengaku sudah disetujui. Saat menunggu, lanjutkan pekerjaan domain/test/penelitian independen dan laporkan bagian yang tertahan. Persetujuan contoh sebagai dasar bukan persetujuan seluruh detail implementasi.
- Tidak mengarang aset, saldo, portofolio, bisnis, riwayat, performa, rekomendasi, durasi, approval atau hasil tes; tampilkan keadaan tidak tersedia bila sumber belum ada. Gunakan solusi yang sudah ada dan perubahan sekecil mungkin yang benar; tidak menambah dependency tanpa kebutuhan.

## Tahap 1 — Baseline dan reproduksi (LT-01–04)

- **LT-01 — Identitas & snapshot.** Catat root, branch/HEAD, perubahan tracked/untracked, waktu, status agent, file APK kandidat dan SHA, serta hash save yang dilindungi. Jangan reset/memperbaiki keadaan Git.
- **LT-02 — Baca review dan keputusan.** Petakan tujuh temuan APK-11, aturan terbaru dan status persetujuan ke source/bridge/UI/save/test; bedakan sudah diperbaiki dari belum terbukti.
- **LT-03 — Reproduksi terisolasi.** Jalankan atau adaptasi dua probe audit pada source terkini dengan save QA. Catat hasil sebelum perbaikan, including exit code; jika tidak terulang, jelaskan perubahan dan uji ekuivalen.
- **LT-04 — Rencana kerja dan status.** Buat tracker 32 ID dan checkpoint di folder artefak task; tiap ID `PENDING`, `IN_PROGRESS`, `PASS`, `BLOCKED` atau `MENUNGGU_PERSETUJUAN`, dengan bukti, dependensi dan alasan. Jangan mengklaim completion hanya dari checklist.

## Tahap 2 — Transaksi penuh atau tidak sama sekali (LT-05–08)

- **LT-05 — Telaah semua jalur matching.** Telusuri pemain sebagai pembeli/penjual masuk dan antre, beberapa harga/lawan, akun pribadi/klien, lelang/pemicu, kondisi likuiditas kurang, validasi dan reservasi.
- **LT-06 — Perbaiki akar penyebab.** Implementasikan full-fill atau nol secara atomik sesuai keputusan pemilik; jangan menghapus histori lama atau mengubah status lama secara palsu. Tidak boleh ada settlement sebagian sebelum keputusan kelayakan keseluruhan.
- **LT-07 — Regresi kasus review.** Uji gabungan NPC 100 + akun klien 300 lalu pembeli pemain 300, versi sisi jual, maker pemain, beberapa level/urutan, dana atau volume kurang, tanpa mutasi kas/saham parsial. Gunakan probe sebagai bukti awal dan tambahkan cek yang dapat dijalankan ulang.
- **LT-08 — Integritas transaksi.** Uji fee, limit/market sesuai keputusan yang berlaku, konsistensi order, posisi, kas, rezervasi, ledger, pembatalan, retry dan restart; kasus tak disepakati ditandai menunggu keputusan.

## Tahap 3 — Order saat pasar tutup (LT-09–12)

- **LT-09 — Satu identitas order.** Telusuri order Market dan Limit dari facade sampai book, riwayat dan save/restore; pastikan hasil matching memperbarui order yang ditampilkan pemain.
- **LT-10 — Pemrosesan ketika buka.** Perbaiki reproduksi order beli 100 saham saat tutup yang menghasilkan posisi 100 tetapi status masih OPEN/0. Uji beli/jual serta saat tidak ada lawan; jangan mendefinisikan ulang harga atau batas yang belum disetujui.
- **LT-11 — Reservasi, batal dan suspend.** Uji reservasi saat dibuat, batal sebelum buka, emiten disuspensi, harga berubah, dana tidak cukup, dan pelepasan kas/saham tepat sekali sesuai keputusan pemilik.
- **LT-12 — Offline dan restart.** Uji simpan/muat saat menunggu dan setelah proses, catch-up lintas buka/tutup, tanpa order hilang, fill palsu atau duplikasi; cocokkan buku order, status, posisi dan ledger.

## Tahap 4 — Ketahanan aksi dan informasi finansial (LT-13–16)

- **LT-13 — Konsistensi lintas tampilan.** Uji jalur domain → bridge → bukti order/riwayat → portofolio/kas untuk beli/jual/ditolak/dibatalkan dan biaya aktual. Jika UI perlu diubah, siapkan preview dan tunggu persetujuan.
- **LT-14 — Aksi berulang.** Uji idempotensi tombol ganda, retry request ID, respons terlambat/terputus dan pengulangan setelah restart. Tidak boleh menciptakan transaksi, dana, saham, atau fee ganda.
- **LT-15 — Gangguan dan pemulihan.** Uji penghentian sebelum/sesudah mutasi dan save QA, kerusakan save serta restore yang didukung; catat kondisi yang tidak bisa dijamin. Jangan merusak save pemain.
- **LT-16 — Laporan perbaikan finansial.** Catat kasus awal/akhir, hasil tes, file sumber, invariant kas/saham/ledger dan celah belum terselesaikan. Kegagalan tanpa bukti penyebab tetap terbuka.

## Tahap 5 — NPC dan leaderboard (LT-17–20)

- **LT-17 — Peringkat profil benar.** Perbaiki contoh leaderboard #2 menjadi profil #1; uji mode kekayaan/return, beberapa rank, pemain dan status tidak tersedia.
- **LT-18 — Data profil terpercaya.** Telusuri 1.000 identitas/akun, posisi, kas, aktivitas, peringkat, 100 avatar dan profil tab. Hilangkan informasi default yang menyesatkan; tampilkan hanya data domain yang benar atau status belum tersedia. Perubahan tampilan mengikuti gate preview.
- **LT-19 — Aktivitas dan migrasi.** Uji keragaman aktivitas/strategi, sebagian NPC dapat tidak transaksi setiap saat, portofolio terkait kekayaan dan hasil, migrasi canonical 20→1.000, seed tepat sekali, save lama/baru dan reload. Jangan menyimpulkan semua aktif hanya karena 1.000 nama tersedia.
- **LT-20 — Peringkat dan permainan.** Buktikan ranking mengikuti hasil nyata, bukan pengacakan ranking; uji perubahan kekayaan/return, akun klien tidak tercampur kekayaan pribadi, data kosong dan persaingan pada beberapa periode. Laporkan celah yang memerlukan keputusan pemain.

## Tahap 6 — IPO dan perlindungan dana (LT-21–24)

- **LT-21 — Alur IPO aktual.** Audit penawaran, batas fase, pemesanan, reservasi, penjatahan, refund, pencatatan, dan pembatalan melalui facade/bridge/UI, termasuk tanggal yang benar-benar tersedia. Buat contoh jika syarat fase butuh keputusan.
- **LT-22 — Alokasi disetujui.** Uji kurang peminat, tepat penuh, kelebihan peminat, batas 20%, 60:40, transfer sisa, proporsi, pembulatan 11 lot menjadi 7:4 bila demand mencukupi, dan undian seri yang stabil. Perbaiki regresi yang dibuktikan tanpa mengubah angka keputusan.
- **LT-23 — Batal dan refund.** Uji sebelum/saat/sesudah penutupan penawaran dan penjatahan jika aturan fase telah ditetapkan, serta dana dikembalikan tepat sekali, duplikat, gagal, restart dan saham terbatas. Jika tanggal belum diputuskan, kerjakan bagian independen, ajukan pertanyaan, tandai dependent task menunggu.
- **LT-24 — Pengujian IPO end-to-end.** Bandingkan pemesanan, hasil penjatahan, kas, saham, ledger, status, daftar dan restore; UI baru hanya sesudah preview disetujui. Jangan klaim IPO perusahaan milik pemain selesai bila di luar cakupan atau syarat belum ada.

## Tahap 7 — UX, preview dan performa (LT-25–28)

- **LT-25 — Preview perubahan UI.** Buat dan tampilkan contoh yang benar-benar bisa dilihat untuk profil NPC, leaderboard dan pembatalan IPO; sertakan empty/error/aktif dan perbedaan yang dibutuhkan. Minta persetujuan per perubahan melalui pilihan otomatis bila alat tersedia; catat pilihan/tanggal/bukti.
- **LT-26 — UI yang telah disetujui.** Setelah persetujuan spesifik, perbaiki tab terpotong, angka, alur balik, fokus/keyboard, target sentuh dan kemudahan menemukan rank pemain; gunakan data aktual, pertahankan nuansa game, jangan menambah kontrol yang tidak berfungsi.
- **LT-27 — Ukur sebelum/sesudah.** Dengan 1.000 NPC dan riwayat representatif, ukur waktu buka leaderboard/profil, memory bila tersedia, waktu simpan/muat, ukuran save dan alur pasar. Nyatakan mesin/lingkungan, ukuran input, sampel dan hasil; bandingkan standar proyek yang berlaku, tanpa menyebut browser sebagai Android.
- **LT-28 — Optimisasi berbasis bukti.** Atasi bottleneck yang benar-benar ditemukan tanpa mengorbankan akurasi, urutan, aksesibilitas, data atau offline; ukur ulang. Uji viewport kecil dan ukuran teks perangkat termasuk 200% jika tersedia, tanpa mengembalikan fitur pengaturan ukuran teks game yang diminta dihapus. Untuk uji perangkat/ADB hormati batas otorisasi yang masih berlaku.

## Tahap 8 — Terminal, regresi, APK dan laporan (LT-29–32)

- **LT-29 — Audit kebocoran terminal.** Petakan berita belum rilis melalui feed, detail, bridge dan notifikasi; catat siapa dapat melihat ticker/sektor/arah/rentang sebelum publik. Usulkan perbaikan sesuai keputusan premium dan daftar hal belum dipilih, beserta preview jika mengubah UI; jangan mengklaim implementasi terminal selesai atau menentukan formula/biaya sendiri.
- **LT-30 — Tes tuntas dan diagnosis.** Jalankan tes baru dan regresi relevan dari source terkini. Jalankan suite luas sampai menghasilkan hasil lengkap; boleh dipecah per kelas/kelompok untuk diagnosis dan runtime, tetapi laporkan total, pemilihan/pengecualian, log dan exit code. Review sebelumnya berhenti dengan tanda `E` tanpa nama tes/diagnosis; cari penyebab dari run selesai, jangan menganggap E sudah lulus/gagal semua. Jalankan stress kritis sesuai standar proyek yang berlaku dan catat keterbatasan.
- **LT-31 — Kandidat & integritas.** Jika build memungkinkan, hasilkan APK kandidat dari snapshot source yang sama; catat nama, bytes, SHA-256, signing dan perbandingan aset. Bedakan debug vs release dan build vs instalasi/perangkat; jangan publish atau menyebut siap rilis tanpa seluruh gate. Jika terhalang, simpan log dan status BLOCKED, lanjutkan kerja independen.
- **LT-32 — Handoff dan checkpoint.** Buat laporan akhir per ID dengan bukti/lokasi/hasil, daftar temuan terbuka, approval UI, pertanyaan dan pekerjaan tertunda, diff/source yang benar-benar diubah, hash save sebelum/sesudah, hasil tes, performa, APK serta keterbatasan native/perangkat. Berhenti setelah tugas ini atau laporkan tunggu keputusan; jangan otomatis memulai plan lain.

## Gate penerimaan

Satu task `PASS` hanya jika implementasi sesuai scope telah terbukti dengan hasil uji dan risiko/keputusan relevan tertutup. Preview yang belum disetujui berstatus `MENUNGGU_PERSETUJUAN`; pertanyaan aturan yang belum dijawab menahan hanya pekerjaan yang bergantung padanya. Gunakan `BLOCKED` bila hambatan nyata dicatat, bukan mengganti dengan PASS atau 100%. Dua probe kritis harus berubah dari gagal menjadi lulus, disertai uji negatif agar tidak sekadar memodifikasi probe. Regresi, bukti UI disetujui, performa dan integritas APK harus sesuai cakupan; setiap yang tidak diuji dinyatakan terbuka. Jangan mengklaim game siap rilis sebelum seluruh persyaratan rilis lain juga diverifikasi.
