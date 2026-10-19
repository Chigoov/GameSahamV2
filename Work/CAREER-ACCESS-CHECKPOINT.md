# Checkpoint: alur karier dan akses fitur

Tanggal: 2026-10-19T23:35:47.854304+07:00. Root GAME SAHAM, branch `codex/owner-audit-portrait-v8`, baseline sesi `a5c42d7`. Produk tetap PARTIAL; seluruh 14 paket penerimaan belum dapat ditandai COMPLETE.

## Hasil subpekerjaan

- Domain memeriksa jabatan sebelum membuat akun klien, preview/submit order klien, menyimpan/mengeksekusi Fund Builder, meminjam margin, membuka short baru, membeli terminal premium, dan mengambil kalender. Akses dasar: Senior Analyst untuk kalender/terminal premium; Portfolio Manager untuk klien/Fund Builder/margin/short. Memakai jenjang existing; angka ekonomi tetap parameter sementara OQ-10.
- Penolakan fitur terkunci tidak mengubah saldo, akun, ledger atau XP. Pelunasan utang dan penutupan short tetap dapat dilakukan untuk mengurangi risiko.
- Snapshot mengirim status akses dan jabatan yang dibutuhkan. Tombol UI terkunci menampilkan syaratnya. Kalender terkunci dibedakan dari kalender kosong. Syarat promosi ditampilkan dari data threshold yang sama dengan evaluator.
- Misi diversifikasi menilai kepemilikan positif di dua sektor berbeda; misi kekayaan menilai ekuitas bersih. Misi transaksi memakai ledger fill existing. Membuka halaman tidak memberi reward.
- Promosi diperiksa setelah klaim dan penutupan hari. Threshold lama dipusatkan di CareerRank tanpa mengubah angka: Analyst hari2/reputasi30; Manager hari4/reputasi50/kekayaan75juta; CIO hari7/reputasi80/kekayaan200juta.
- Uang pinjaman tidak menambah kekayaan untuk misi/promosi. Nilai memakai perhitungan ekuitas yang sama dengan header, termasuk kewajiban margin/short. XP dan badge tidak menentukan akses jabatan.
- Pengujian alur pemain memakai permainan baru, order yang benar-benar fill, klaim misi dan gaji, sampai Portfolio Manager menerima mandat; save/load mempertahankan akses dan newgame menguncinya kembali. Tidak ada injeksi saldo/rank dalam alur tersebut.

## Bukti

- 157 tes JVM / 15 suite, 0 failure/error/skipped; 4 tes baru di `CareerAccessTest.java`: penolakan tanpa mutasi, earned player journey sampai klien/save/reset, repayment setelah kehilangan akses, dan pinjaman tidak menaikkan misi/promosi.
- Tes finansial existing menggunakan helper fixture eksplisit `GameTestSupport.unlockManager`; helper itu hanya source test dan tidak masuk APK. Tes integrasi finansial lama diganti nama agar tidak dianggap bukti earned career journey.
- Browser: 15 pemeriksaan PASS pada host Java fresh, termasuk tombol terkunci dan earned journey sampai Manager tanpa injeksi fixture. 20 sampel portrait tanpa overflow horizontal; scroll vertikal/fixed-page V8 masih belum selesai. Tidak ada pageerror.
- Build/lint: `gradlew.bat -p android --no-daemon testDebugUnitTest lintDebug assembleDebug --console=plain` PASS. Lint 0 error/13 warning. JBR + workaround loopback hanya per proses.
- APK debug tanda tangan verified; 7 aset source cocok byte-for-byte. App ID tetap `com.tradingglobal3d.mobile`, minSdk26/targetSdk36. Ukuran 623140 byte, SHA256 `5e0629e200846fe2fa9718865b2cfb23baccefee322d48b83107c71d36497be9`. File: `Artifacts/CareerAccess-2026-10-19/game-saham-career-access-debug.apk`. Bukan APK final V8 dan belum bukti instalasi/native.
- Bukti lengkap lokal: `Artifacts/CareerAccess-2026-10-19/verification.json`, XML, gradle-final.log, browser-check.py/browser-result.json, career-locked.png, career-earned.png, dan apk-signature.txt.

## Batas dan pekerjaan berikutnya

Checkpoint ini menyelesaikan akses dasar karier serta jalan pemain untuk memperolehnya. Aturan sesi per bursa/akhir pekan/libur belum berubah dan menjadi subpekerjaan berikutnya; jangan menyebutnya sudah teratasi. Manfaat/entitlement data premium, laporan klien/completion badge report, NPC/events/fitur lanjutan dan port UI V8 masih belum penuh. Native lifecycle/performa/perangkat serta dataset historis tetap gate tersendiri.
