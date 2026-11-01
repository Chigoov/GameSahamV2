# Instruksi aktif Game Saham 2D Portrait

**Keputusan Q&A dan Hasil Audit/Perbaikan Terkini (01 November 2026):**
1. **Kecepatan Waktu 48×:** 1 hari game = 30 menit dunia nyata (86.400 detik game = 1.800 detik nyata) berlaku seragam pada kondisi aktif, background, dan offline. Bursa setiap hari game 09:00–16:00 WIB (Sesi I 09:00–12:00, Istirahat 12:00–13:30, Sesi II 13:30–16:00) dan bursa US 21:30–04:00 WIB.
2. **Kebijakan Transaksi Pemain All-Or-None (AON):** Hapus transaksi terisi sebagian (partial fill) untuk pemain. Order LIMIT pemain antre utuh (`OPEN`) bila likuiditas pemenuhan penuh belum tersedia; order MARKET pemain dibatalkan utuh (`CANCELLED`) seketika jika likuiditas tidak cukup, dan seluruh dana tertahan dilepas.
3. **Mekanika Penjatahan IPO:** Jika kurang peminat (*undersubscribed*), seluruh pemesan memperoleh 100% pesanan (refund Rp 0). Jika kelebihan peminat (*oversubscribed*), kuota awal 60% pemesan besar (>= 20% saham ditawarkan) dan 40% pemesan lain dibagikan menggunakan Metode Pecahan Terbesar (Hamilton Method) dengan undian seri deterministik berbasis hash. Penawaran IPO berlangsung 3 hari game sebelum tanggal pencatatan.
4. **Proteksi Transaksi Sendiri (Anti-Self Trade):** Order matching menolak mempertemukan maker dan taker dari akun yang sama.
5. **Fast-Forward Batas Harian Esensial:** Pada lompatan offline atau jeda waktu besar (>1 jam game), sistem melompat langsung ke batas jam kejadian ekonomi esensial (16:00 cutoff reksa dana & 24:00 settlement kalender) untuk mengeksekusi kewajiban harian tanpa menimbun antrean bising NPC masa lalu, menjaga heap memori tetap aman (<384 MB).
6. **Autosave & Proteksi Cadangan:** Autosave hening tanpa notifikasi berulang; jika terjadi kegagalan/korupsi primer, rotasi cadangan `.bak` otomatis melindungi progres pemain.

## Aturan aktif dan prioritas keputusan (01 November 2026)

- Pemilik menunjuk satu `plan.md` pada satu waktu. Kerjakan hanya lingkup yang ditunjuk, lanjutkan pekerjaan independen selama menunggu keputusan, laporkan status dan berhenti setelah penyerahan. Pembuatan Plan 13 bukan perintah memulai eksekusi.
- Ikuti instruksi terbaru pemilik, lalu keputusan terbaru yang tercatat di `MASTER-PLAN-OFFLINE-DAN-REALISME-2026-10-28.md` bagian 2A dan plan yang ditunjuk. Dokumen desain, laporan, checkpoint, `Work/STATUS.json`, angka 20/126 dan mandat lama adalah sumber konteks atau bukti historis; jangan menganggapnya status kini atau memakai aturan lama yang bertentangan. Verifikasi source dan bukti terkini.
- Dunia berjalan otomatis 48× pada kondisi aktif, background dan aplikasi ditutup: satu hari game sekitar 30 menit nyata. Bursa setiap hari game 09.00–16.00 waktu game. Efek berita 60/120 menit nyata mempertahankan deadline persisten. Tidak ada kontrol pause, speed atau skip untuk pemain. Catch-up saat aplikasi dibuka kembali tidak membuktikan proses tetap hidup saat aplikasi tertutup.
- Arah visual terbaru adalah Tycoon Sinematik sebagai dasar, dengan lima tab Beranda, Pasar, Portofolio, Bisnis, Lainnya. Contoh dasar bukan persetujuan setiap layar. Tampilkan preview nyata dan minta persetujuan eksplisit per perubahan tampilan, teks, navigasi dan alur sebelum penerapan; lanjutkan pekerjaan independen selagi menunggu. Pertahankan keputusan lain yang tidak bertentangan sebagaimana master plan terbaru, termasuk Portofolio B, Profil A, avatar dan tiga pilihan galeri. Lihat bagian aktif terbaru `DESIGN.md`.
- Paket riset: Pro USD 250.000 dan Whale USD 1.000.000 uang game per 30 hari game; perpanjang otomatis jika saldo cukup, jika tidak langsung turun ke Basic. Pemain dapat menghentikan perpanjangan dengan akses tersisa sampai masa berbayar berakhir. Dua berita Pro 2–5% tiap hari bursa; emiten boleh sama jika ada alasan game. Whale 1–2 berita eksklusif per tujuh hari game mengikuti kejadian; jangan mengarang kejadian jika jumlah tidak cukup. Rumor lanjutan langka menggandakan persentase awal dan dijamin pada penutupan yang relevan terhadap penutupan hari sebelumnya; konflik dan pembulatan mengikuti Q09 (+5% dan -3% dijumlahkan bertanda menjadi +2%) dan Q10 (pembulatan searah target pada fraksi harga sah) sesuai keputusan disahkan pada `Work/Eksekusi-Audit-Logika-UI-2026-11-01/plan.md` dan handoff. Aturan lengkap dan keputusan terbaru di bagian 2A master plan dan Plan 14; jangan menganggap source sebagai bukti sudah sesuai.
- Keputusan menghapus replay historis tidak mengizinkan merusak pasar utama atau menghapus/migrasi save replay lama tanpa kebijakan yang disetujui. Perubahan UI dan penanganan save ikuti master plan terbaru.
- Root penulisan `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`. Pertahankan semua perubahan lokal; save pemain `Artifacts/LocalPreview/preview-save/save_game_v2.json` hanya di-hash dan uji memakai save QA baru terisolasi. Tidak ADB, instalasi perangkat, reset/clean/stash, push/publish, pesan eksternal atau perubahan global tanpa izin yang berlaku.
- Hermes adalah pelaksana untuk plan yang ditunjuk; jangan meluncurkan agent lain atau mengirim pesan lintas chat berdasarkan mandat lama. Terapkan standar kualitas relevan, tetapi jangan mengklaim 100% atau menghitung denominator lama tanpa dasar untuk plan yang ditunjuk. Pisahkan bukti host, browser, build APK, emulator dan perangkat.

## Rujukan dan standar kualitas

Keputusan dan mandat 20–24 Oktober yang digantikan adalah riwayat; gunakan dokumen sumber hanya jika diperlukan untuk melacak bukti. Pertahankan fitur dan syarat yang tidak bertentangan. Standar kualitas relevan ada di `Work/Hermes-Handoff-2026-10-20/PHASE-QUALITY-STANDARDS.md`: uji keadaan UI dan teks besar, jalankan 10×100 aksi untuk alur kritis bila berlaku, serta catat batas dan hasil performa. Bukti lama bukan bukti pada snapshot baru.

## Arah yang berlaku

Bangun Android 2D portrait dengan trading dan karier sama penting. Semua fitur lama dalam FR-01 sampai FR-20 wajib rilis pertama; 14 paket pekerjaan baru adalah urutan implementasi, bukan izin memangkas scope. Seluruh cakupan 26 layar lama harus terlayani. Hapus kewajiban 3D, landscape, Unity, Three.js dan navigasi kamera dari keputusan lama. Hasil akhir APK; bukti build, instalasi, emulator dan perangkat fisik berbeda.

Repo mempunyai source produksi Java + native Android WebView dan UI HTML/CSS/JS lokal. Verifikasi baseline source/test/APK aktual; keberadaannya bukan bukti semua fitur sudah selesai. Repo lama adalah referensi dokumen di C:/Users/HYPE AMD/Downloads/VIBE CODING/Game Trading Saham. Jangan menulis repo lama atau mengimpor source/save/build/credentials secara otomatis. Pembacaan source lama dibatasi sampai pemilik secara eksplisit mengizinkannya; gunakan dokumen dan bukti referensi yang sudah tersedia.

## Cara kerja

- Baca design brief dan DESIGN.md sesuai tugas; keputusan terbaru mengungguli palet dan arahan lama yang bertentangan. Pertahankan kebutuhan state, responsive dan aksesibilitas; preview dan persetujuan tiap perubahan UI tetap wajib. Brief bukan bukti UI/native sudah diuji.
- Buktikan cwd dan Git root sebelum edit. Pertahankan semua perubahan lokal. Jangan reset/clean/delete massal.
- Periksa Work/STATUS.json sebagai snapshot historis bersama task yang ditunjuk, lalu verifikasi source dan bukti terbaru. Jika butuh resume Hermes, catat exact session ID; jangan mengandalkan latest.
- Jangan meluncurkan agent lain atau mengirim pesan lintas chat berdasarkan mandat lama. Jangan menjalankan launcher paralel, membuat launcher/scheduler/global automation baru, atau mengubah profil/provider/model/credentials maupun pengaturan global Hermes.
- Kerjakan satu hasil terukur, periksa, checkpoint, lalu lanjut. Jangan berhenti hanya karena durasi/iterasi atau menghemat token. Hormati STOP dan hambatan nyata; jangan loop tanpa diagnosis.
- Sesudah dua kegagalan sama tanpa bukti/diagnosis baru, kirim PERLU PERHATIAN dengan bukti dan pendekatan berbeda. Sesudah tiga, alihkan ke pekerjaan independen atau laporkan BLOCKED jika benar-benar membutuhkan tindakan luar.
- Laporkan ID dan subbutir plan yang ditunjuk, task berjalan/pending, file berubah, command dan bukti, blocker, serta next_action. Angka x/14 adalah denominator historis dan tidak otomatis berlaku.
- Dilarang menandai selesai karena folder, dokumen, mock, static check, atau APK saja. Kaitkan setiap requirement dan gate ke bukti yang sesuai.
- Jangan push, publish, mengirim pesan pihak lain, membeli layanan, atau menyentuh repo/data lain. Commit lokal hanya file tugas yang sudah ditinjau dan diperiksa; jangan commit secrets, data pemain, dataset tanpa hak, atau binary besar.
- Keputusan teknis rutin dapat ditetapkan sesuai plan yang ditunjuk, dengan alasan dan tes. Aturan produk, ekonomi dan tampilan yang menurut keputusan terbaru masih menunggu pemilik tidak boleh dipilih sepihak. Kerjakan bagian independen saat ada hambatan nyata.
- Pertahankan produksi Java + native Android WebView + HTML/CSS/JS lokal. Domain Java satu pemilik ekonomi, waktu dan save; UI hanya presentasi/input. Rekomendasi C#/MAUI historis tidak berlaku. Hindari engine/dependency/frontend framework baru tanpa kebutuhan.
- Dataset historis harus nyata dengan provenance dan hak penggunaan yang ditinjau. Fixture sintetis boleh untuk tests berlabel dan tidak meluluskan replay historis.
- Jika memakai paket aplikasi lama sebagai update, pertahankan application ID com.tradingglobal3d.mobile, signing dan progres. Jangan mengasumsikan key/save lama tersedia di repo baru.

## Ponytail minimum yang benar

Pahami alur lengkap dan telusuri semua caller sebelum mengubah fungsi. Urutan: perlu dibangun -> existing -> standard library -> native platform -> dependency terpasang -> minimum code baru. Tanpa abstraksi/framework/dependency spekulatif. Tandai shortcut disengaja dengan ponytail: dan sebut batas serta upgrade path bila ada.

Jangan memangkas validasi batas input, keamanan uang, penanganan data loss, aksesibilitas atau scope eksplisit. Nontrivial logic menyisakan satu runnable check minimum menggunakan runner yang ada; tanpa framework/fixture baru hanya untuk checklist.
