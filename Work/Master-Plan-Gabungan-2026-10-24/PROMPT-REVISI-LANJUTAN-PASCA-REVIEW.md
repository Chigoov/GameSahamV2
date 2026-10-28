# Paket revisi lanjutan setelah review APK-05

Tanggal:26 Oktober2026, Asia/Jakarta.

Kerjakan tindak lanjut review independen revisi pertama. Root produksi:
`C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`.

Baca sebelum implementasi:

- `Artifacts/Audit-Revisi-2026-10-26/REVIEW-REVISI.md`, termasuk hasil regresi penuh final dan pemetaan23 ID.
- `Work/Master-Plan-Gabungan-2026-10-24/DAFTAR-KEKURANGAN-LINTAS-PLAN.md`, terutama pembaruan review terbaru. Ringkasan agent21/21 sebelumnya merupakan klaim penutupan sebelum review ulang.
- `Work/Master-Plan-Gabungan-2026-10-24/PROMPT-REVISI-SELURUH-PLAN.md`, AGENTS.md, DESIGN.md, plan terkait dan standar penerimaan aktif. Mandat revisi lintas plan pada prompt pertama tetap menjadi batas pelaksanaan.

Saya menginstruksikan kamu menuntaskan temuan terbuka yang dikonfirmasi review ini, beserta regresi langsung perbaikannya. Reproduksi dari bukti, telusuri fungsi bersama dan semua caller, perbaiki secukupnya, lalu buktikan penutupannya. Pertahankan perbaikan yang sudah benar. Jangan mengulang implementasi temuan yang telah dibuktikan tertutup pada snapshot review, tetapi jalankan regresi yang relevan setelah perubahan berikutnya.

Hasil akhir review independen: **626/626 tes JVM PASS pada384MB**, termasuk kasus730 hari lama; JUnit1813.931 detik. K-001/K-021 sudah mempunyai bukti independen pada snapshot tersebut. Dari23 ID setelah dua temuan baru,13 masih terbuka/parsial: K-002/K-005/K-009/K-010/K-011/K-013/K-014/K-015/K-017/K-018/K-019/K-022/K-023. Fokus perubahan pada daftar ini dan regresi langsungnya; hasil tes lama tidak otomatis berlaku setelah kamu mengubah source.

## Urutan dan syarat penutupan

### 1. K-022 — perlindungan backup, prioritasP1

`SaveManager.save` sekarang menerima primary dengan schemaVersion2 plus key accounts/ledger walaupun isi field tersebut tidak valid. Jalankan ulang `Artifacts/Audit-Revisi-2026-10-26/BackupValidationProbe.java` pada save QA baru; probe sengaja menunjukkan defect pada revisi pertama. Hasil pembanding sebelum revisi berada di backup-baseline-result.json.

Pulihkan perlindungan backup terakhir yang benar-benar dapat dimuat. Jangan menyalin primary yang rusak secara struktur/semantik menjadi pengganti backup baik. Pertahankan atomic save, recovery, kompatibilitas schema dan perbaikan memori; jangan hanya mengembalikan alokasi besar tanpa mengukurnya.

Tutup setelah ada pemeriksaan regresi yang gagal pada kode bermasalah dan lulus setelah perbaikan: backup awalvalid -> primary strukturinvalid -> save state livevalid -> primary baru rusak -> recovery backup tetap berhasil. Tambahkan kasus schema lama yang masih didukung dan tipe/isi akun atau ledger yang ditolak parser. Gunakan save QA, bukan save pemain.

### 2. K-018 — persetujuan harga koleksi, prioritasP1

Reproduksi `revision-ui-probes.json` dan stale-sell-confirmation.png: detail menjanjikan Rp877.000, harga berubah, domain mengkredit Rp876.912, toast masih mengatakan Rp877.000. BUY yang memakai valuasi baru belum menutup SELL yang stale.

Ikat persetujuan harga/versi pada eksekusi domain: harga cocok atau respons menuntut review ulang tanpa mutasi ekonomi. Berlakukan pada BUY/SELL dan seluruh caller, termasuk perubahan tanggal di antara review dan confirm. Pertahankan requestId untuk satu intent yang statusnya belum pasti, gunakan receipt aktual untuk angka hasil, dan uji retry idempoten serta batas kas. Perbandingan fresh di JavaScript saja tidak menjamin harga pada saat eksekusi. Jangan mengubah aturan valuasi.

### 3. K-009/K-023/K-011 — pesan penting, dedupe dan outcome

- K-009: tiga error/alert lalu satu info tidak boleh menghapus error penting tanpa jalur pemulihan. Reproduksi errorBurst pada revision-ui-probes.json. Pertahankan dedupe sesuai kontrak; dokumentasikan batas cache sesi dengan jujur, bukan mengklaim dedupe permanen lintas reload.
- K-023: dua operasi sync yang sama-sama berhasil harus sama-sama memberi feedback. Jangan gunakan CLOCK_SYNC_OK/CLOCK_SYNC_ERR tetap sebagai identitas permanen semua operasi pengguna. Bedakan operasi baru dari callback duplikat untuk operasi yang sama; uji sukses -> sukses, gagal -> retry -> sukses, serta gagal berulang.
- K-011: lengkapi matriks input -> outcome domain -> feedback sesuai plan. Klaim "Sesi Bursa Berganti" belum ada pada source yang direview. Buktikan perubahan sesi, trigger/expiry, bridge/save error, offline sekali, halt/likuiditas, reload dan pergantian akun yang berlaku. Uji alur integrasi, jangan menggantinya dengan pemanggilan toast langsung atau sekadar branch string.

### 4. K-015/K-002/K-013/K-010/K-017 — gate, cakupan dan metadata

- Jalankan satu fungsi gate yang sama untuk positif dan negatif. `gate-negative-probe.cjs` membuktikan runner saat ini tetap PASS dengan tombol20x48 dan span kontras1.0479:1. Perbaikan harus membuat counterexample tersebut ditolak oleh runner utama dengan exit nonzero, tanpa menghapus elemen pelanggar sebelum pemeriksaan.
- Periksa lebar dan tinggi48px sesuai kontrak, warna runtime dengan ambang teks yang tepat, state/modal yang relevan, keyboard/fokus/Back/scroll/reduced motion,10 viewport wajib dan skala100%/200%. Seratus pemeriksaan overflow tab utama bukan bukti seluruh state berita/order lulus.
- Simpan sampel mentah minimal30 per kategori aksi/navigasi yang diklaim, bukan35 sampel gabungan lima tab atau hanya jumlah/P95. P95 aksi lokal<=300ms, navigasi<=1000ms dan feedback berat<=300ms. Panduan Bermain tidak mewakili latency aksi order/berita.
- Lengkapi matriks K-010 untuk akun klien sah dan tipe order yang diwajibkan, termasuk invariant kas/reservasi/fee, isolasi akun, retry dan penolakan yang sah. BUY/SELL LIMIT akun klien belum mewakili empat tipe pada dua bursa. Fixture diberi label; jangan mengubah syarat produk demi membuat fixture lulus.
- K-017: tampilkan metadata yang tersedia atau Belum tersedia. Bedakan mekanisme rotasi aktif dari file backup yang benar-benar ada/valid. Jangan mengarang savedTimestamp; cek kontrak DTO aktual. Jangan menambah enkripsi yang tidak diminta.
- Penuhi empat blok Delivery Gate sesuai instruksi aktif, dengan screenshot yang benar-benar diperiksa dan batas cakupan yang jelas.

### 5. K-019/K-005/K-014 — alat penutupan dan laporan

`audit_tracker.py` masih exit0 pada duplicate+PENDING. Validasi set126 ID kanonis, missing/unexpected/duplicate dan status non-PASS dengan exit nonzero. Kasus negatif harus menjalankan runner utama; jangan menguji logika terpisah yang tidak dipakai runner tersebut.

Sinkronkan status terkini di daftar revisi, Work/STATUS.json, STATUS-REPORT.md dan CHECKPOINT.md; pisahkan histori penutupan pertama dari hasil review ulang. Jangan menyatakan23 temuan tertutup sebelum semua syarat teruji. Hasil review penuh yang diterima pada snapshot ini harus mempunyai rujukan bukti independen, lalu diperbarui jika source diubah lagi.

Koreksi tujuh hash APK-02 pada laporan PLAN-03 berdasarkan APK-02 historis, dengan catatan errata. Hash APK-05 atau kandidat berikutnya tidak menggantikan hash artefak lama. Hitung identitas/signature dari alat yang benar-benar berjalan; jangan mengisi nilai verifikasi sebagai literal lalu mengklaim sudah diperiksa.

## Pengujian akhir dan penyerahan

Gunakan tes target selama implementasi, lalu satu regresi penuh fresh setelah source stabil. Kasus730 hari yang sebelumnya gagal harus tetap dijalankan pada budget384MB; tes60 hari tidak menggantikannya. Jangan mengurangi durasi, menghapus histori, menaikkan heap saja, atau menelan error untuk menutup temuan. Jumlah kasus runtime mengikuti source terbaru, bukan angka yang dipaksakan.

Baca full-jvm-result.json dan log dari review ini. Hasilnya berlaku pada snapshot review, bukan otomatis pada source setelah kamu mengedit lagi. Pertahankan pemeriksaan load all-or-nothing dan seluruh invariant ekonomi.

Diagnosis hambatan Gradle secara lokal. Review mencoba JBR eksplisit dan terhenti sebelum compile karena loopback; bukti host JVM tidak menggantikan compile/build Gradle. Jangan mengubah pengaturan global. Bila build masih terhambat, laporkan deliverable belum terpenuhi dan lanjutkan revisi independen.

Sesudah source stabil dan pemeriksaan selesai, buat kandidat APK baru dengan nama baru tanpa menimpa APK-05. Catat command/exit build, manifest source, identitas, signature, ukuran, SHA256 dan seluruh aset terpaket. Kandidat debug, hasil host/browser dan Android fisik tetap dibedakan.

Semua save/proses QA harus terisolasi. Save pemain Artifacts/LocalPreview/preview-save/save_game_v2.json hanya di-hash sebelum/sesudah. Pertahankan preview18917 dan demo18918 beserta datanya; periksa listener sebelum memilih port QA. Jangan reset/clean/stash, mengedit delta di luar scope, meluncurkan agen lain, mengirim pesan lintas chat, push/publish, ADB, instalasi atau mengubah pengaturan global.

Simpan bukti baru di folder terpisah yang tidak menimpa artefak audit maupun revisi pertama. Perbarui daftar revisi existing per ID: perubahan, pemeriksaan, hasil, lokasi bukti dan status. Laporkan apa yang benar-benar sudah selesai, apa yang masih terbuka, dan batas pengujiannya. Mulai dari K-022, lanjut seluruh paket ini tanpa meminta izin ulang per kelompok.
