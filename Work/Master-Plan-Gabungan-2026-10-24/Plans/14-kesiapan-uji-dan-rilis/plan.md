# Plan 14: Kesiapan uji bermain dan rilis Game Saham

**Keputusan baru 29 Oktober:** untuk contoh rumor lanjutan +42% ketika batas bursa hanya +20%, pemilik memilih mengizinkan batas dilampaui khusus untuk kejadian itu. Apakah berlaku juga untuk berita Whale biasa masih ditanyakan. Untuk dua target berita berbayar berbeda pada emiten dan penutupan yang sama, jadwalkan berita kedua ke sesi lain. Keputusan bukan bukti implementasi.

**Keputusan konflik terbaru 29 Oktober:** dua berita berbayar dengan target berbeda untuk emiten dan penutupan yang sama tidak boleh dijadwalkan bertentangan; berita kedua menunggu sesi lain. Batas bursa yang menghalangi target Whale masih menunggu pilihan setelah contoh. Keputusan ini menggantikan bagian lama yang mengatakan konflik sepenuhnya belum dipilih; belum menjadi bukti implementasi.

**Status: DRAFT, MENUNGGU PENYELESAIAN PLAN 13 DAN BEBERAPA KEPUTUSAN.** Diperbarui 29 Oktober 2026 dari jawaban pemilik atas 20 pertanyaan. Bukan izin memulai pelaksanaan, instalasi, distribusi atau publikasi. Setelah Plan 13 diperbaiki dan diperiksa, pemilik dapat menunjuk plan ini.

## Syarat masuk dan perlindungan

- Periksa bukti Plan 13 terbaru, termasuk masalah auto-renewal setelah restore, status terminal, tutorial, UI, NPC/IPO/order, laporan dan pengujian. Plan 13 belum boleh diasumsikan lulus dari laporan lama. Jika ada item menunggu keputusan atau approval, catat dampaknya terhadap pekerjaan ini; jangan menyebutnya selesai.
- Root produksi `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`. Baca `AGENTS.md`, `DESIGN.md`, master plan terbaru dan bukti relevan. Periksa branch, HEAD, perubahan lokal serta versi kandidat. Pertahankan seluruh pekerjaan yang ada; jangan reset, clean, stash atau hapus data.
- Save pemain `Artifacts/LocalPreview/preview-save/save_game_v2.json` hanya di-hash; gunakan save QA terisolasi. Jangan migrasi, reset atau hapus save pemain atau save replay lama tanpa kebijakan dan izin yang sesuai.
- ADB, emulator, instalasi/perangkat, keystore, distribusi dan publikasi belum diizinkan hanya karena plan ini dibuat. Ikuti jawaban pemilik dan batas akses yang berlaku; siapkan pekerjaan independen dan tandai `BELUM_DIUJI` jika izin atau perangkat tidak tersedia. Jangan mengakali penolakan.
- Pertahankan 24× aktif/offline, bursa 09.00–16.00 setiap hari game, Pro USD 250.000, Whale USD 1.000.000 per 30 hari game, dan keputusan lain yang tidak diganti pemilik. Tampilan Tycoon Sinematik adalah dasar; setiap perubahan layar/teks/alur memerlukan preview dan persetujuan spesifik. Tidak menganggap pilihan produk sebagai izin menerapkan seluruh detail UI.

## Pertanyaan pemilik yang menentukan lingkup

Jawaban diterima melalui Q&A percakapan pada 29 Oktober 2026. Ini keputusan produk, bukan bukti fitur telah diterapkan atau pengujian telah dilakukan.

**Keputusan lanjutan pemilik mengenai warta, 29 Oktober:** setiap hari bursa ada **dua berita Pro 2–5%**; sesuai jawaban 40, keduanya boleh membahas emiten sama jika ada alasan dalam game. Ini menggantikan angka lama 2–3 berita dan syarat dua emiten berbeda. Warta eksklusif Whale **1–2 kali per tujuh hari game**, dengan kenaikan **lebih dari 20%** sesuai keputusan sebelumnya. Untuk kejadian langka lanjutan pada hari berikutnya, persentase kenaikan dua kali rumor awal dan dijamin pada penutupan relevan terhadap harga penutupan hari sebelumnya. Contoh awal 21% menjadi 42%; Rp10.000 menjadi Rp14.200 sebelum pembulatan. Frekuensi minimal sekali per 14 hari game sudah dipilih; timing, konflik dengan berita lain dan pembulatan masih memerlukan rincian; sajikan contoh untuk keputusan. Kenaikan harga bukan jaminan laba bersih pemain.

1. Rilis awal untuk **uji coba pribadi** di perangkat pemilik.
2. Format awal **APK untuk dipasang langsung**; tidak menyiratkan persetujuan publikasi.
3. Seluruh fitur yang sudah direncanakan wajib lengkap.
4. Stabilitas/save/transaksi dan tampilan/kenyamanan sama penting.
5. Ponsel Android pemilik; **izin diminta per tindakan**, bukan izin ADB/instalasi otomatis.
6. Pemilik bersedia menguji sendiri setelah kandidat siap.
7. Durasi uji bermain ditentukan berdasarkan hasil awal; belum ada angka pasti.
8. Perbaiki semua bug yang diketahui sebelum rilis.
9. Siapkan skenario offline dahulu; tunggu izin tindakan pada perangkat.
10. Jika save rusak, tawarkan pemulihan dari cadangan setelah konfirmasi pemain; verifikasi cadangan dan jangan timpa data diam-diam.
11. Penanganan progres replay lama **menunggu pemilik melihat kondisi save**. Save pemain yang dilindungi tetap hanya di-hash; minta izin yang berlaku sebelum inspeksi lain dan jangan mengakali penolakan terdahulu.
12. Lindungi progres lama ketika memulai game baru; minta konfirmasi untuk tindakan yang menghapus. Ini menggantikan secara terbatas pilihan lama untuk menghapus progres tanpa cadangan.
13. Tampilkan preview **grid dua kolom dengan gambar aset terlebih dahulu**. Ini prioritas preview, bukan persetujuan final; tiga alternatif sebelumnya tetap disiapkan.
14. Preview beberapa layar boleh dikelompokkan; persetujuan tetap spesifik untuk masing-masing layar.
15. Jika saldo kurang saat perpanjangan, paket **langsung turun ke Basic**; tanpa saldo negatif. Uji urutan/debit dan status.
16. Pro: dua berita setiap hari bursa, masing-masing 2–5%; emiten boleh sama jika ada alasan dalam game sesuai jawaban 40.
17. Whale: 1–2 warta eksklusif per tujuh hari game; rumor lanjutan langka menggandakan persentase awal. Rincian kejadian langka yang tercantum di atas masih menunggu keputusan.
18. Pemain harus dapat menghentikan perpanjangan otomatis untuk Pro dan Whale; detail alur dan tampilan memerlukan preview dan persetujuan.
19. Lima kategori notifikasi tersedia dan dapat diatur pemain; kemampuan perangkat dan izin belum terbukti.
20. Serahkan laporan temuan, bukti uji, APK kandidat, dan daftar keputusan.

## Jawaban 50 pertanyaan lanjutan (21–70)

Jawaban diterima melalui Q&A pada 29 Oktober 2026. Pilihan yang meminta contoh belum menjadi persetujuan desain; tampilkan preview nyata dan minta pilihan spesifik. Tidak ada hasil uji atau implementasi yang disimpulkan dari jawaban.

21. APK debug boleh untuk uji pribadi jika diberi label jelas.
22. Pertahankan APK sebelumnya sampai versi baru terbukti aman.
23. Jika signature tidak kompatibel, siapkan opsi penyelamatan progres untuk ditinjau; jangan uninstall atau menimpa progres secara otomatis.
24. Sediakan panduan pemasangan singkat yang menjaga progres.
25. Hasil uji awal dilaporkan melalui daftar pemeriksaan singkat.
26. Sarankan durasi lanjutan setelah beberapa sesi dan temuannya ditinjau.
27. Informasi awal game: MENUNGGU beberapa contoh.
28. Bentuk panduan awal: MENUNGGU beberapa contoh; jangan menambahkan pembelian saham wajib.
29. Portofolio tanpa saham: tonjolkan tombol menuju pasar; preview perlu persetujuan.
30. Informasi saat bursa tutup: MENUNGGU beberapa contoh.
31. Hasil offline: ringkasan singkat dengan rincian yang dapat dibuka, berbasis kejadian yang tercatat.
32. Jika tidak ada kejadian offline, langsung masuk Beranda.
33. Pemilik menanyakan apakah game autosave. Source memiliki pemanggilan simpan otomatis pada sejumlah mutasi dan upaya saat lifecycle berubah, serta tombol simpan manual; perilaku menyeluruh dan perangkat belum terbukti. Jangan anggap pemilik memilih notifikasi penyimpanan; MENUNGGU keputusan bila diperlukan.
34. Jika simpan gagal, tampilkan pilihan penyelamatan progres; detail perlu preview.
35. Sebelum pemulihan, tampilkan waktu cadangan dan risiko hilangnya progres terbaru.
36. Untuk game baru, jelaskan dampak dan minta konfirmasi sebelum penghapusan progres.
37. Informasi save replay lama disampaikan ketika pemilik meminta; jangan lakukan inspeksi lain pada save pemain yang hash-only.
38. Ketika diminta, siapkan lokasi, ukuran, hash dan cara peninjauan aman tanpa membuka isi; batas akses dan penolakan terdahulu tetap berlaku.
39. Waktu berita Pro tidak harus sama setiap hari, selama sesuai keputusan waktu kabar sebelumnya.
40. Dua berita Pro boleh membahas emiten yang sama jika ada alasan dalam game; jawaban terbaru ini menggantikan secara terbatas ketentuan dua emiten berbeda sebelumnya. Pertahankan dua berita per hari bursa.
41. Emiten boleh muncul lagi hari berikutnya jika alasannya jelas.
42. Jumlah 1–2 berita Whale mengikuti kejadian game. Jika kejadian kurang, siapkan kejadian simulasi yang masuk akal agar ada minimal satu warta; kejadian dan warta harus benar-benar terhubung, bukan angka rekaan pada UI.
43. Waktu warta Whale mengikuti kejadian dalam game dan keputusan waktu kabar yang berlaku.
44. Kejadian lanjutan Whale minimal satu kali dalam 14 hari game; detail jendela, timing, offline dan retry perlu rancangan dan uji.
45. Harga acuan kenaikan lanjutan: harga penutupan hari sebelumnya.
46. Kenaikan persentase rumor lanjutan dijamin pada penutupan bursa yang relevan berdasarkan acuan yang dipilih pada 45. Contoh: rumor awal 21% → lanjutan 42%; penutupan sebelumnya Rp10.000 → target penutupan Rp14.200 sebelum pembulatan. Bukan jaminan keuntungan bersih.
47. Konflik dua berita berbayar pada emiten yang sama: MENUNGGU beberapa contoh aturan dan pilihan pemilik.
48. Pembulatan/batas perdagangan yang menyulitkan target: MENUNGGU contoh dan keputusan pemilik.
49. Dua berita Pro hari yang sama boleh berbeda persentase dalam rentang 2–5%.
50. Informasi pada warta berbayar: MENUNGGU beberapa contoh.
51. Riwayat warta diperlukan, dengan status hasil jika tersedia dari data nyata.
52. Keadaan warta berbayar belum tersedia: MENUNGGU beberapa contoh tanpa membuat data rekaan.
53. Kontrol berhenti auto-renewal: tombol di Kelola Paket dengan konfirmasi; preview diperlukan.
54. Setelah auto-renewal dihentikan, akses tetap aktif sampai akhir periode yang sudah dibayar.
55. Pemberitahuan sebelum perpanjangan jika kategori notifikasi tagihan aktif; ketepatan Android belum terbukti.
56. Informasi saat saldo kurang/turun Basic: MENUNGGU beberapa contoh.
57. Riwayat tagihan menunjukkan tanggal, paket, biaya yang tercatat.
58. Tampilkan biaya dan durasi sebelum konfirmasi pembelian atau perubahan paket.
59. Tiket order menonjolkan akun, jumlah, harga dan biaya sebelum konfirmasi.
60. Hasil order merangkum status, jumlah, harga dan biaya yang benar-benar terjadi.
61. Order antrean saat bursa tutup boleh dibatalkan sebelum buka, dengan pelepasan dana atau saham tertahan.
62. Informasi utama daftar IPO: MENUNGGU beberapa contoh.
63. Tampilan data emiten IPO belum tersedia: MENUNGGU contoh, jangan mengarang data.
64. Urutan informasi profil NPC: MENUNGGU beberapa contoh; dua peringkat berlabel tetap dipertahankan.
65. Tampilan bisnis NPC tidak ada/data belum tersedia: MENUNGGU contoh, jangan mengarang fakta.
66. Isi kartu galeri dua kolom: MENUNGGU beberapa contoh.
67. Fallback gambar koleksi: MENUNGGU contoh.
68. Pengaturan lima kategori notifikasi: MENUNGGU contoh; pilihan awal lima kategori aktif tetap berlaku.
69. Jika izin notifikasi Android ditolak, jelaskan notifikasi perangkat tidak aktif dan beri akses pengaturan jika tersedia.
70. Serahkan APK kandidat, panduan instalasi dan daftar pemeriksaan singkat; laporan temuan dan bukti tetap disertakan sesuai jawaban 20.

## Jawaban 20 pertanyaan akhir (J01–J20), 29 Oktober 2026

J01–J03. Pemilik meminta beberapa preview untuk informasi awal, panduan awal dan kondisi pasar tutup; belum memilih desain.
J04. Autosave tanpa pesan sukses setiap kali; targetkan keandalan dan uji penyimpanan/retry/pemulihan, tanpa menjanjikan keberhasilan mutlak pada setiap gangguan perangkat.
J05. Saat autosave gagal tampilkan peringatan dan tombol coba lagi; pertahankan pilihan penyelamatan progres yang dipilih sebelumnya bila relevan. J06. Pemilik meminta contoh informasi cadangan; waktu dan risiko kehilangan progres tetap perlu dijelaskan.
J07. Durasi uji pribadi ditentukan setelah hasil awal. J08. Preview boleh berkelompok dengan persetujuan spesifik per layar. J09. Grid galeri dua kolom bergambar ditampilkan dahulu, bukan otomatis disetujui. J10. Kartu menampilkan gambar, nama, harga dan status kepemilikan; preview detail tetap butuh persetujuan.
J11–J15. Pemilik meminta beberapa preview untuk fallback gambar, daftar IPO, data IPO kosong, informasi awal profil NPC dan bisnis NPC jika data tidak tersedia; jangan mengarang data.
J16. Jika kejadian Whale kurang dari satu per tujuh hari game, siapkan kejadian simulasi masuk akal untuk menghasilkan minimal satu warta. J17–J18. Konflik berita dan pembulatan/batas bursa menunggu contoh dan keputusan. J19. Rumor lanjutan langka minimal satu kali dalam 14 hari game; pemilik menegaskan satuan game. Rincian jendela, waktu, offline/retry dan konflik memerlukan rancangan dan keputusan bila memengaruhi aturan. J20. Plan 14 baru dimulai setelah pemilik meninjau dan menunjuknya. Ini keputusan produk, bukan bukti implementasi atau izin tindakan perangkat.

## Pelaksanaan setelah ditunjuk

### Cara mengerjakan dan melacak tanpa melewatkan butir

Sebelum mengubah produk, buat matriks per butir dari keputusan 1–70 dan subbutir G1–G5: ID, syarat, sumber/keputusan terbaru, status awal, ketergantungan, source/bridge/UI/save terkait, metode dan bukti, hasil, persetujuan, serta tindakan berikut. Bedakan jawaban pemilik, contoh yang diminta, pekerjaan implementasi dan pengujian; satu baris PASS tidak menutup beberapa syarat. Labeli `SUDAH_TERBUKTI`, `PERLU_REVISI`, `MENUNGGU_CONTOH`, `MENUNGGU_PERSETUJUAN`, `MENUNGGU_KEPUTUSAN`, `BELUM_DIUJI`, `TERHALANG` atau `LULUS`. Sertakan alasan dan bukti konkret; kode lama atau laporan saja bukan bukti hasil. Tandai tugas yang sudah terpenuhi secara spesifik, jangan ulangi implementasi tanpa kebutuhan.

Pemetaan minimum: G1 = 1–8, 21–26, 70; G2 = 9–12, 15–19, 31–38, 39–49, 51, 54–55, 57–61, 63–65, 69; G3 = 13–14, 27–36, 50–53, 56–60, 62–69; G4 = 1–2, 5–6, 9, 19–24, 68–70; G5 = 1, 3–8, 20, 25–26, 70. Pemetaan ini referensi silang, bukan batas daftar: periksa 1–70 seluruhnya dan turunkan syarat yang terlewat. Untuk butir 33 jangan menafsirkan pertanyaan autosave sebagai pilihan notifikasi; jika perlu pilihan, tampilkan contoh. Butir 37–38 hanya setelah pemilik meminta dan tetap mengikuti pembatasan akses.

Siapkan contoh yang diminta untuk 27, 28, 30, 47, 48, 50, 52, 56, 62–68. Untuk 47 dan 48, berikan skenario angka dan konsekuensi tanpa menetapkan kebijakan; untuk UI berikan preview yang terlihat, data simulasi berlabel, keadaan relevan dan pilihan Setuju/Setuju dengan perubahan/Belum cocok/Tunda. Galeri grid dua kolom ditampilkan dahulu, dua alternatif lain tetap disiapkan. Tampilkan beberapa layar per kelompok sesuai keinginan pemilik, tetapi simpan persetujuan untuk masing-masing. Jangan menganggap diam atau penerimaan contoh sebagai approval implementasi.

Jika sumber keputusan saling bertentangan, utamakan jawaban terbaru yang eksplisit. Contoh: Pro dua berita per hari, dapat untuk emiten sama bila ada alasan; Whale 1–2 per tujuh hari mengikuti kejadian, dan jika kejadian kurang siapkan kejadian simulasi masuk akal agar minimal satu warta terbit. Rumor lanjutan langka memakai penutupan hari sebelumnya sebagai acuan dan target penutupan yang relevan sebesar dua kali persentase awal; frekuensi minimum sekali per 14 hari game sudah dipilih; waktu tepat, konflik dan pembulatan masih terbuka. Pemilik meminta semua fitur direncanakan lengkap dan semua bug diketahui diperbaiki sebelum rilis; jika tidak dapat dipenuhi, catat kondisi konkret dan jangan menurunkan standar sepihak.

### G1. Audit kandidat dan inventaris gap

Identifikasi source dan kandidat terbaru, hash, konfigurasi, signature, perubahan lokal, daftar fitur/layar/journey dan bukti setiap syarat. Rekonsiliasi Plan 13 dengan pengujian pada snapshot aktual. Tandai bug terkonfirmasi, keputusan tertunda, dan pengujian belum dilakukan; jangan hitung artefak atau laporan sebagai kelulusan. Jika ditemukan bug, perbaiki dalam cakupan yang disetujui, jalankan regresi, dan catat dampak pada save, ekonomi serta keputusan sebelumnya.

### G2. Uji ekonomi, progres dan pemulihan pada QA

Gunakan save baru dan save QA lama buatan yang terisolasi. Uji game baru, save/reload/backup/recovery, penanganan data rusak, langganan lintas restore/offline, order, IPO, biaya, ledger, bisnis, NPC, berita, progres dan perubahan hari. Uji idempotensi dan kegagalan tanpa mengubah data pemain. Jalankan suite relevan serta standar kritis proyek, termasuk 10×100 bila berlaku. Catat seed, lingkungan, versi, log, exit dan hasil; uji host bukan bukti perangkat.

### G3. UI dan aksesibilitas

Inventaris seluruh layar, alur dan state; periksa akses fitur, data aktual, error/kosong/offline, lima tab, ukuran teks 200% bila tersedia, kontras, sentuhan, fokus, Back, scroll dan safe area. Tampilkan preview nyata per perubahan dan catat persetujuan sebelum implementasi. Sertakan alternatif galeri sesuai pilihan pemilik. Uji ulang hasil implementasi yang disetujui; jangan menganggap preview sebagai implementasi atau bukti Android.

### G4. Kandidat dan uji Android

Siapkan kandidat teridentifikasi dari source yang diuji; catat hash, ukuran, perbandingan aset/kelas/config yang relevan, jenis signature dan build log. Debug hanya kandidat uji, bukan release signing. Jika pemilik memberikan izin yang diperlukan, uji pemasangan, peluncuran, sentuh/Back, gangguan lifecycle, offline/restart, notifikasi, performa dan perjalanan pemain pada lingkungan yang disetujui. Gunakan akun/save QA; jangan menimpa progres pemain. Catat perangkat, versi OS, hasil dan batas. Jika tidak diizinkan, siapkan panduan dan tandai belum diuji.

### G5. Uji bermain dan keputusan rilis

Siapkan skenario uji bermain untuk sasaran dan durasi yang dipilih, termasuk progres awal, pasar, portofolio, karier/bisnis, riset, offline dan pemulihan. Catat umpan balik dan bug tanpa mengarang hasil atau menganggap waktu berlalu sebagai bukti pengujian. Kelompokkan dampak dan terapkan kebijakan bug yang dipilih pemilik.

Pisahkan rekomendasi **siap uji pribadi**, **siap uji terbatas**, dan **siap rilis publik**. Periksa kebutuhan release signing dan jalur distribusi yang dipilih tanpa membuat keystore baru, membeli layanan, mengunggah atau mempublikasikan tanpa izin yang berlaku. Jangan menjanjikan kesiapan toko atau instalasi hanya dari APK debug.

## Penyerahan

Buat laporan hasil dengan status per gate dan subbutir, versi source/kandidat, bukti dan lingkungan, hash save sebelum/sesudah, uji lulus/gagal/belum dijalankan, approval UI, risiko terbuka dan keputusan yang masih dibutuhkan. Sertakan daftar prioritas perbaikan dan panduan uji bermain yang sesuai. Pisahkan host, browser, APK, emulator dan ponsel fisik. Sebut siap hanya untuk sasaran yang seluruh kriterianya telah terbukti; jika ada gap nyatakan `BELUM_SIAP` atau `MENUNGGU_KEPUTUSAN`. Berhenti setelah menyerahkan hasil, tanpa otomatis memulai tahap berikut atau merilis.
