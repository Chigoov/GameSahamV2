# Plan revisi APK-10 dan persiapan rekomendasi riset

**Status: SIAP DITUNJUK PEMILIK; implementasi menunggu penugasan.** Referensi `Artifacts/Review-Indep-APK10-2026-10-28/LAPORAN-REVIEW.md`, `Work/RISET-OPEN-ACCESS-FITUR-GAME-2026-10-28.md`, dan `MASTER-PLAN-OFFLINE-DAN-REALISME-2026-10-28.md`. Tinjau source, status dan keputusan terbaru lagi sebelum memulai; bukti review merupakan snapshot, bukan jaminan masalah masih ada atau telah diperbaiki. Pemilik telah menyetujui R01–R16 sebagai dasar setelah melihat contoh; keputusan aturan yang belum dipilih dan detail UI yang belum dipreview tetap terbuka. Pekerjaan terdahulu yang sudah dipilih tetap berlaku menurut master plan.

## Batas kerja

- Hermes adalah writer implementasi; ikuti `AGENTS.md`, pertahankan seluruh perubahan lokal dan gunakan save QA terisolasi. Jangan mengubah, menghapus, reset atau menimpa save asli; dilarang ADB baru, instalasi perangkat, push dan publikasi sesuai aturan proyek.
- Setiap perubahan UI, teks atau alur baru memerlukan preview serta persetujuan eksplisit pemilik sebelum diterapkan. Saat menunggu lanjutkan pekerjaan domain dan pengujian independen yang telah ditunjuk; jangan mengganti isi dengan asumsi sendiri.
- Semua kondisi berubah dan hasil uji harus dilaporkan jujur. Jangan klaim 100% bila sebagian masih menunggu keputusan, approval, Android, pengujian atau terbukti gagal. Perbandingan APK dan source harus pada kandidat yang sama.

## A. Perbaikan temuan review

1. **REV-01 — Full-fill pemain.** Reproduksi order beli 300 saham mengantre lalu lawan menjual 100; pastikan tidak ada eksekusi parsial baik ketika order pemain masuk maupun ketika order pemain menjadi antrean. Uji sisi beli/jual, beberapa lawan yang bersama mencukupi/tidak mencukupi, pembatalan, order saat bursa tutup, saldo, fee, ledger dan restart. Pelihara histori partial lama; definisi harga cocok yang belum disepakati tetap terbuka.
2. **REV-02 — Migrasi 20 menjadi 1.000 NPC.** Reproduksi save lama `canonicalNpcAccounts=true`, termasuk akun dan ledger, melalui restore serta bind/load produksi; jangan menyamaratakan semua save gagal. Perbaiki migrasi sambil menjaga identitas, saldo dan posisi, dan mencatat seed tepat sekali. Uji save baru/lama, corrupt, pengulangan dan restart menggunakan data QA; jangan utak-atik save pemain.
3. **REV-03 — IPO kurang peminat.** Reproduksi penawaran 1.000 lot dan pesanan 10 lot yang dipangkas menjadi 5 akibat rasio 0,5. Pemilik telah memilih jika total pesanan sah tidak melebihi saham yang ditawarkan dan dana cukup, seluruh pesanan dipenuhi; misalnya 20 dan 30 lot mendapat 20 dan 30 lot. Perbaiki alur produksi, uji di bawah batas, tepat penuh, oversubscribed, dana dan refund tanpa menggandakan mutasi.
4. **REV-04 — Pembulatan IPO.** Reproduksi 11 lot dengan permintaan kelompok besar 8 dan lain 6; bagian awal sesuai keputusan adalah 7:4, bukan 6:5. Uji ambang 20%, pembagian 60:40, proporsi dua kelompok, pemindahan sisa, jumlah tidak melebihi pesanan/saham, seri diundi, refund, restart dan idempotensi. Jelaskan penanganan seri dan simpan hasil undian agar tidak berubah saat retry.
5. **REV-05 — Profil dan avatar NPC.** Audit 1.000 NPC yang nyata aktif serta profil/tab, leaderboard dan 100 avatar; source sebelumnya hanya menampilkan inisial. Siapkan preview berdasarkan profil B dan avatar anime yang disetujui sebagai dasar; tunggu persetujuan penerapan. Uji data aktual, akun, posisi, ranking, penggunaan ulang avatar dan performa.
6. **REV-06 — Pembatalan IPO.** Audit tombol dan boundary masa penawaran serta penjatahan, pengembalian dana dan pemanggilan bridge. Siapkan preview sebelum mengubah UI; uji alur nyata dengan QA.
7. **REV-07 — Pelaporan dan bukti.** Validasi klaim P00–P09, keputusan dan persetujuan, tes host/browser/APK/perangkat dan playtest 1–3 hari; perbarui laporan yang perlu direvisi berdasarkan bukti. Verifikasi ukuran, SHA, signing dan aset pada APK yang sama. APK-10 16.139.325 byte, debug V2 pada pemeriksaan sebelumnya; jangan menyatakan siap rilis hanya karena build atau tes host.

## B. 16 rekomendasi riset — disetujui sebagai dasar rencana

Gunakan `Work/RISET-OPEN-ACCESS-FITUR-GAME-2026-10-28.md` sebagai rincian sumber dan usulan, namun perbarui inventaris source saat ini: riset awal menyebut 20 NPC dan avatar belum dipisah, sedangkan pemeriksaan APK-10 menemukan 1.000 entri serta 100 file WebP, tanpa bukti seluruhnya aktif atau tampil di UI. Pisahkan ide baru dari fitur existing dan keputusan yang telah dipilih; jangan menghitung ulang sebagai fitur baru atau mengubah aturan 24×, ekonomi, IPO atau biaya diam-diam.

| ID | Usulan | Fokus persiapan dan keputusan |
|---|---|---|
| R01 | IPO berskala realistis | Preview penawaran, dana dan hasil; uji skala/modal awal serta 20% dan 60:40 tanpa mengganti aturan. |
| R02 | Agenda dividen dan hak | Preview tanggal hak dan pembayaran; audit domain, tentukan kalender game. |
| R03 | Aksi korporasi sebelum/sesudah | Preview dampak berdasarkan data aktual; audit jenis yang sudah ada. |
| R04 | Kepemilikan dan pengaruh | Preview pemegang ≥1%, publik dan hak; ambang hak belum diputuskan. |
| R05 | Informasi perusahaan IPO | Preview tab Profil/Keuangan/Risiko/Rencana; data tersedia atau state belum tersedia. |
| R06 | Komposisi portofolio | Preview proporsi aset/sektor tanpa saran transaksi paksa. |
| R07 | Bukti transaksi | Preview jumlah, harga aktual, biaya, status dan dampak kas sesuai ledger. |
| R08 | Istilah order jelas | Preview informasi harga dan order; definisi harga cocok masih terbuka. |
| R09 | Rival sekitar pemain | Preview ranking berdasarkan hasil sebenarnya, bukan angka acak. |
| R10 | Tujuan pribadi | Preview tujuan opsional tanpa tutorial wajib; target/hadiah baru belum disetujui. |
| R11 | Kegiatan pasar tutup | Preview riset/pantauan dan bisnis/aset; jam bursa belum diputuskan. |
| R12 | Ringkasan kembali | Preview daftar kejadian aktual, termasuk state kosong/data terbatas. |
| R13 | Notifikasi | Preview kontrol lima jenis; uji izin dan batas Android saat diperbolehkan. |
| R14 | Aktivitas 1.000 NPC | Audit transaksi, portofolio, offline, kekayaan dan performa; jangan menganggap banyak nama sebagai bukti aktif. |
| R15 | Dampak keputusan bisnis | Preview perbandingan pendapatan, biaya dan proyeksi; bedakan hasil aktual. |
| R16 | Visual dan aksesibilitas | Preview anime, tombol, state dan skala teks perangkat, uji kontras serta navigasi. |

Preview awal 16 gagasan: `Artifacts/Preview-Saran-2026-10-28/prototype-riset-16.html` dan lembar PNG. Data adalah ilustrasi, bukan kondisi game. Pemilik menyetujui semua sebagai dasar; detail desain, state, aturan dan formula yang belum dipilih tidak otomatis disetujui. Sajikan keputusan yang masih terbuka dengan pertanyaan pilihan dan contoh visual. Jangan mengemas data pihak ketiga tanpa memeriksa lisensi.

## Penerimaan dan laporan

- Setiap REV memiliki status, penyebab, reproduksi sebelum/sesudah, uji negatif/batas, bukti, file terkait dan kendala. Jalan uji regresi relevan dan laporkan perbedaan host, browser, APK, emulator dan ponsel.
- Catat R01–R16 sebagai dasar yang telah disetujui, dan pisahkan kebutuhan rinci yang masih menunggu keputusan atau preview. Petakan ke tugas existing dan lingkup yang ditunjuk; jangan mengklaim telah diterapkan.
- Catat klaim APK/debug, ukuran, hash, aset dan pengujian; target 1–3 hari nyata merupakan sasaran, bukan hasil tanpa playtest.
- Setelah selesai pada lingkup yang ditunjuk pemilik, berikan laporan dan berhenti menunggu instruksi berikutnya.
