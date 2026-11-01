# Prompt pelaksanaan revisi seluruh plan

Tanggal: 26 Oktober 2026, Asia/Jakarta.

## Mandat pemilik

Seluruh PLAN-01 sampai PLAN-08 sudah dieksekusi. Sekarang kerjakan paket revisi lintas plan berdasarkan hasil audit independen dan daftar K-001 sampai K-021. Penundaan perbaikan sampai semua plan selesai sudah mencapai waktunya. Instruksi ini, ketika saya kirim kepadamu, mengizinkan implementasi dan pengujian seluruh paket revisi ini, termasuk perubahan source Java, WebView, test, dan dokumentasi yang diperlukan untuk menutup temuan. Batas paket adalah 21 ID tersebut beserta regresi langsung akibat perbaikannya.

Kerjakan berurutan sampai paket ini selesai atau ada hambatan nyata yang membutuhkan keputusan saya. Jangan berhenti setelah satu kelompok hanya untuk meminta izin melanjutkan kelompok berikutnya. Instruksi satu plan per kiriman tetap berlaku untuk pekerjaan baru di luar paket revisi ini. Jangan menganggap penyusunan prompt ini sebagai bukti bahwa revisi sudah dikerjakan.

## Root dan bahan wajib

Root produksi yang benar:

`C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`

Checkout `Game Trading Saham` berbeda. Buktikan root Git dan identitas snapshot sebelum mengedit. Baca instruksi aktif di AGENTS.md dan DESIGN.md, dokumen produk yang diwajibkan AGENTS.md, serta file berikut:

1. `Work/Master-Plan-Gabungan-2026-10-24/DAFTAR-KEKURANGAN-LINTAS-PLAN.md` — daftar kanonis 21 temuan, tindakan dan syarat tutup. Gunakan file yang sama untuk memperbarui status.
2. `Artifacts/Audit-Seluruh-Plan-2026-10-26/AUDIT-SELURUH-PLAN.md` — hasil audit delapan fase dan batas kesimpulannya.
3. `Artifacts/Audit-Seluruh-Plan-2026-10-26/CAKUPAN-AUDIT-126-ID.csv` — pemetaan pemeriksaan; bukan tracker penerimaan baru.
4. `Artifacts/Audit-Seluruh-Plan-2026-10-26/full-jvm-result.json` dan `full-jvm/junit-latest.log` — kegagalan regresi final yang harus ditelusuri.
5. Audit PLAN-01 dan PLAN-03 pada folder `independent-audit-20261024` dan `independent-audit-20261025` yang dirujuk daftar revisi.
6. `master plan perbaikan.md`, delapan `Work/Master-Plan-Gabungan-2026-10-24/Plans/*/plan.md`, dan `Work/Hermes-Handoff-2026-10-20/PHASE-QUALITY-STANDARDS.md` untuk kontrak penerimaan yang terkait.

Reproduksi pada source terkini. Temuan historis mungkin sudah berubah. Jika sudah diperbaiki, buktikan dengan pemeriksaan yang relevan, lalu tutup dengan rujukan bukti tanpa mengubah kode lagi. Bedakan bug terkonfirmasi, bukti yang kurang, dan koreksi laporan.

## Batas pelaksanaan

- Pertahankan seluruh perubahan lokal. Jangan reset, clean, stash, atau mengembalikan file orang lain. Jadilah satu writer implementasi; jangan meluncurkan agen lain atau mengirim pesan lintas chat.
- Semua pengujian memakai save QA baru yang terisolasi. Save pemain `Artifacts/LocalPreview/preview-save/save_game_v2.json` hanya boleh dihitung hash; jangan dibaca isinya, dimuat ke facade, ditimpa, dimigrasikan, atau di-reset. Catat hash sebelum dan sesudah pekerjaan. Hash audit terakhir: `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`; bila berbeda sekarang, catat sebagai baseline baru tanpa mengubah datanya.
- Pertahankan preview lokal port18917 dan demo port18918 beserta save-nya. Periksa listener terkini dan pilih port/save lain untuk QA. Hentikan hanya proses QA yang kamu buat sendiri.
- Tidak ada ADB baru, instalasi APK, emulator/perangkat, push, publish, atau perubahan pengaturan global. Native tetap NOT_TESTED_BY_SCOPE sesuai batas ini.
- Domain Java tetap pemilik ekonomi/waktu/save; WebView menyajikan data dan input. Pertahankan waktu otomatis24x, deadline berita persisten, kompatibilitas save, dan desain lima tab yang berlaku.
- Pertahankan rumus valuasi, tarif, hadiah dan aturan ekonomi yang tidak terkait defect. Jangan menyeimbangkan ekonomi berdasarkan demo tiga bulan tanpa mandat terpisah. Demo itu bukan acceptance baru.
- Telusuri fungsi bersama dan semua caller sebelum mengedit. Gunakan helper existing; jangan menambah framework/dependency atau abstraksi spekulatif. Perbaikan logika nontrivial harus meninggalkan pemeriksaan regresi yang dapat dijalankan.

## Urutan kerja dan hasil yang harus dicapai

### A. Pulihkan load histori panjang — K-021; siapkan penutupan K-001

Baseline audit: regresi penuh menjalankan624 tes,623 lulus,1 gagal selama3382.687 detik, dengan heap384MB. Kegagalan `AllFeaturesExtreme10x100CriticalSequenceTest.testMultiYearProgression730Days` adalah OutOfMemoryError melalui `TradingFacade.load` ke `SaveState.fromJsonString`.

Profil alokasi, retensi ledger/history, dan staging load. Source yang diaudit membuat salinan seluruh state melalui `SaveState.fromJsonString(state.toJsonString())` saat memvalidasi mutual fund. Itu titik alokasi pada stack gagal, bukan kesimpulan bahwa seluruh akar masalah sudah diketahui.

Kurangi alokasi yang tidak diperlukan sambil mempertahankan validasi, restore all-or-nothing, akun/pool reksa dana, kas/posisi, ledger, backup, dan kompatibilitas. Buktikan kegagalan load tidak meninggalkan state setengah diterapkan. Pertahankan runnable check histori panjang pada budget384MB sebagai pembanding baseline. Menaikkan heap saja, membuang histori/progres, mengurangi skenario730 hari, atau menelan error tidak menutup temuan.

K-001 baru ditutup setelah regresi penuh pada snapshot akhir stabil lulus dan kandidat APK dikaitkan dengan source yang benar.

### B. Betulkan persetujuan transaksi dan tindakan receipt — K-018, K-008, K-012

- K-018: pakai valuasi/quote domain yang konsisten untuk kemampuan bayar, review, konfirmasi dan receipt koleksi. Harga yang disetujui harus sama dengan debit/kredit; bila quote berubah, wajibkan review ulang. Uji harga naik/turun dan kas di sekitar batas mampu bayar. Pertahankan requestId saat hasil belum pasti dan idempotensi retry; jangan mengubah rumus valuasi agar cocok dengan UI.
- K-008: cancel gagal atau save error tidak boleh diumumkan berhasil. Tentukan feedback dari outcome domain. Bedakan reservasi kas BUY dan saham SELL; klaim pelepasan hanya sesuai hasil aktual.
- K-012: live receipt EXPIRED/REJECTED/FILLED/CANCELLED tidak menawarkan pembatalan yang tidak sah. Gunakan canCancel/status domain dan pertahankan fokus/scroll saat pembaruan.

### C. Betulkan clock, metadata dan notifikasi — K-016, K-017, K-009, K-011

- K-016: gunakan alur sinkronisasi existing, termasuk boolean foreground dan pemantauan hasil async. Tampilkan rate24, bukan clock.speed1. Pending/failure tidak boleh memunculkan sukses; hindari klaim sinkron ke server jika mekanismenya jam lokal.
- K-017: format save, waktu simpan terakhir dan keberadaan backup harus berdasarkan metadata nyata. Jika belum tersedia, nyatakan demikian. Hapus klaim enkripsi/hash valid yang tidak didukung; jangan membangun enkripsi baru hanya untuk membenarkan teks lama.
- K-009: dedupe berdasarkan identitas event/request/status yang stabil sesuai kontrak; event yang sama tidak muncul lagi hanya karena lima detik berlalu. Penanganan burst tidak boleh menghilangkan error penting tanpa jalur pemulihan. Perbaiki helper bersama secukupnya.
- K-011: petakan input -> outcome domain -> feedback untuk kondisi wajib: pending trigger, fill parsial/penuh, cancel, expiry, sesi, bridge/save error, offline sekali, halt/likuiditas, reload dan pergantian akun. Buktikan lewat alur integrasi; tes memanggil toast secara langsung tidak cukup.

### D. Lengkapi matriks dan perbaiki gate — K-010, K-002, K-013, K-015, K-020

- K-010: petakan jenis order, BUY/SELL, IDX/US, akun personal dan klien sah yang diwajibkan plan. Uji saldo, reservasi kas/saham, biaya, isolasi akun, akses/mandat dan idempotensi. Kombinasi tidak didukung harus mempunyai penolakan yang benar dan teruji; jangan menggantikan akun klien sah dengan ID palsu.
- K-020: perbaiki pasangan warna label filter Pasar yang diaudit sekitar2.8226:1. Gunakan token sesuai canvas/panel, lalu ukur warna runtime pada state relevan.
- K-015: gate harus benar-benar gagal dengan exit nonzero ketika kriteria gagal. Pertahankan ambang target sentuh48px, kontras teks normal4.5:1 dan teks besar3:1. Jangan menyatakan PASS dari console.warn, ambang38/44px, atau kontras3.8/4.0. Tambahkan kasus negatif yang membuktikan gate menolak pelanggaran.
- K-002/K-013: petakan seluruh layar/state relevan, termasuk modal/detail; main-screen overflow saja tidak menutup acceptance. Uji empty/error/loading/stale/locked/scheduled/ended dan state lain yang berlaku, dengan alasan bila N/A. Periksa keyboard, fokus, Back/Escape, scroll, reduced motion, kontras, dan screenshot aktual.
- Gunakan10 viewport wajib:320x568,360x640,360x800,390x844,412x915,430x932,768x1024,1440x900,470x658,462x648; teks100% dan200%. Terapkan pada seluruh layar/state relevan, bukan hanya tab terakhir. Fixture UI diberi label dan tidak diklaim sebagai jumlah data domain nyata.
- Ukur minimal30 sampel per jenis aksi/navigasi yang dilaporkan. Simpan data mentah: P95 aksi lokal<=300ms, navigasi<=1000ms, feedback pekerjaan berat<=300ms dan UI responsif. Lima sampel navigasi tidak membuktikan performa aksi lokal. Pisahkan host/browser dari Android.
- Fase kritis yang terdampak tetap memenuhi minimal10 seed x100 aksi dengan invariant yang relevan sesuai standar proyek. Gunakan pemeriksaan existing bila masih tepat. Lengkapi empat blok Delivery Gate persis seperti kontrak aktif, dengan bukti dan batas cakupan yang jujur.

### E. Koreksi laporan dan status — K-003, K-004, K-005, K-006, K-007, K-014, K-019

- K-003: cocokkan durasi berita aktual dengan katalog/source. Default60 menit dan dukungan120 menit eksplisit dijelaskan sesuai bukti; jangan mengubah durasi produk demi mencocokkan laporan.
- K-004: jelaskan field persistence/DTO sebenarnya; nama field keliru tidak memerlukan migrasi baru.
- K-006: jelaskan retry sesuai implementasi, tanpa menyebut exponential backoff bila jedanya tetap.
- K-007: batasi klaim kesetaraan pada data yang benar-benar dibandingkan. Identitas subset ekonomi bukan identitas seluruh save byte-for-byte. Tambahkan assertion hanya jika diperlukan kontrak.
- K-014: koreksi hash historis dari artefak historis yang dirujuk. Kandidat APK baru mempunyai manifest baru; jangan memasukkan hash kandidat baru ke laporan seolah-olah itu hash APK lama. Catat riwayat koreksi.
- K-019: jadikan duplicate/missing/unexpected ID serta status non-PASS sebagai kegagalan penutupan, bukan sekadar hitungan cetak. Uji fixture negatif. Rekonsiliasi126 ID kanonis dan20 paket; milestone UI tidak dihitung sebagai subtask tambahan. Audit menandai rentang N01/N08/N09 yang salah dan total tabel125; verifikasi terhadap plan kanonis.
- K-005/K-019: sinkronkan tracker, Work/STATUS.json, STATUS-REPORT.md, CHECKPOINT.md dan RESULTS yang relevan. Pertahankan histori gagal dengan label historis/superseded dan rujukan penggantinya. Catat branch/HEAD plus manifest perubahan lokal aktual, karena HEAD saja tidak mewakili dirty checkout. Tampilkan status implementasi plan dan status revisi secara terpisah;126 label PASS tidak otomatis berarti21 temuan tertutup.

### F. Penutupan pada snapshot akhir

Jalankan tes target selama implementasi. Setelah source stabil, lakukan satu regresi penuh dari kompilasi fresh memakai runner existing. Catat command, jumlah tes runtime aktual, exit code, durasi, batas heap dan manifest source. Angka624 adalah baseline audit, bukan jumlah yang harus dipaksakan setelah penambahan tes. Ulangi bila ada perubahan berikutnya yang membatalkan hasil atau kegagalan yang perlu diuji kembali.

Validasi kompilasi/pengujian Gradle ketika memungkinkan. Baseline audit terhambat koneksi loopback JDK sebelum compile. Diagnosis secara lokal tanpa mengubah pengaturan global; catat hambatan yang tersisa. Eksperimen classpath android.jar manual bukan bukti Gradle aktual gagal. Bukti host tidak menggantikan bukti Gradle/APK.

Bangun kandidat APK revisi dari source akhir tanpa menimpa APK audit historis. Verifikasi identitas aplikasi, versi, signature yang dipakai, ukuran, SHA256, dan kecocokan aset terpaket dengan source. Jangan mengklaim debug APK sebagai rilis produksi atau terinstal. Jika build terhambat, nyatakan deliverable belum terpenuhi dan jangan menutup paket sebagai100% selesai.

Simpan bukti baru di `Artifacts/Revisi-Lintas-Plan-2026-10-26/`. Pertahankan artefak audit lama. Dalam daftar revisi existing, catat untuk setiap ID: diagnosis, file/perubahan, pemeriksaan dan hasil, lokasi bukti, serta status. Gunakan SELESAI_TERVERIFIKASI hanya jika syarat tutup dipenuhi; temuan yang tidak bisa direproduksi sekarang tetap memerlukan bukti yang menjelaskan hasilnya.

Laporan akhir harus menyebut:

1. Jumlah temuan selesai/terbuka dari21, ID yang belum ditutup dan alasannya.
2. Perubahan konkret dan risiko yang tersisa; hindari klaim seluruh aplikasi bebas bug dari hasil terbatas.
3. Hasil tes target, regresi penuh, matriks UI, performa, dan kasus negatif gate, dengan tautan bukti.
4. Snapshot source dan kandidat APK yang diperiksa, termasuk hash yang dihitung mesin.
5. Hash save pemain sebelum/sesudah, preservasi preview, serta proses QA milik sendiri yang ditutup.
6. Batas host/browser/Gradle/APK/native secara terpisah. NOT_TESTED_BY_SCOPE tetap bukan PASS.

Mulai dari pemeriksaan root dan reproduksi K-021. Lanjutkan paket ini sampai semua tindakan yang terotorisasi selesai. Bila satu jalur terhambat, lanjutkan revisi independen dan laporkan hambatan secara spesifik; jangan membuat seluruh paket tampak lulus dengan menurunkan standar atau menghapus pengujian.
