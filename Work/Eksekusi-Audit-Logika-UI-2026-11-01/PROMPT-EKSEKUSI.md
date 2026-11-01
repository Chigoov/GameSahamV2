Saya menunjuk file ini sebagai satu plan aktif untuk perbaikan audit Game Saham:

C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/Eksekusi-Audit-Logika-UI-2026-11-01/plan.md

Baca plan tersebut sampai selesai, termasuk bagian 2A tentang batas perubahan kode, serta handoff berikut:

C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/HANDOFF-AUDIT-LOGIKA-DAN-RESPON-UI-2026-11-01.md

Kerjakan lingkup 17 temuan utama, 12 catatan tambahan sesuai bukti dan keputusan, serta seluruh keputusan Q&A terbaru yang dirujuk plan. Bagian 12 handoff menetapkan dukungan order besar 10.000-50.000 lot dengan likuiditas sesuai kebutuhan dan tetap All-Or-None; 2.000 lot adalah cadangan dasar, bukan batas maksimum order.

Aturan perubahan kode wajib:

1. HANYA ubah kode yang diperlukan untuk butir yang diinstruksikan dalam plan. Setiap perubahan harus mempunyai ID temuan atau requirement yang jelas. Jika saya mempersempit lingkup, ikuti lingkup terbaru.
2. Sebelum mengedit, pahami alur lengkap, telusuri semua caller, lalu catat singkat ID, akar masalah dan file/fungsi yang perlu disentuh. Perbaiki pada tempat yang tepat dengan diff minimum. Helper/caller terkait boleh diubah hanya jika memang diperlukan untuk memperbaiki akar masalah atau mencegah regresi langsung.
3. Jangan melakukan perubahan sambilan: refactor umum, rename massal, format seluruh file, konversi line ending, mengganti framework, menambah dependency, mengubah versi, menghapus fitur, atau merombak layar yang tidak ditugaskan. Optimisasi yang tercantum dalam plan harus mengikuti bukti dan batasnya.
4. Pertahankan perilaku di luar perubahan yang sengaja diminta. Jangan menimpa perubahan lokal pihak lain, meskipun berada pada file yang sama.
5. Temuan baru di luar lingkup cukup dicatat dengan lokasi kode, bukti, dampak dan rekomendasi. Jangan langsung diperbaiki. Jika mutlak menjadi prasyarat dan tidak dapat dipisahkan, minta persetujuan perluasan lingkup; lanjutkan bagian independen yang sudah diizinkan.
6. Setelah setiap hasil terukur, periksa diff terhadap baseline pekerjaan. Setiap hunk perubahan sendiri harus dapat dipetakan ke ID/requirement. Jangan memakai reset/clean/stash atau revert massal untuk membersihkan diff.

Mulai dengan memverifikasi cwd, root Git, branch, HEAD dan perubahan lokal. Gunakan hanya repo produksi C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM. Revalidasi temuan terhadap source terkini; jangan menganggap laporan lama atau angka kelulusan historis sebagai bukti saat ini.

Sinkronkan bagian aktif AGENTS.md dengan keputusan yang sudah disahkan sesuai T00, tanpa menulis ulang seluruh file, menghapus perubahan lokal, melonggarkan pembatasan, atau memperluas izin sendiri.

Save pemain Artifacts/LocalPreview/preview-save/save_game_v2.json hanya boleh di-hash. Jangan baca isinya, mengubahnya, memigrasikannya, atau menjadikannya fixture. Gunakan save QA baru terisolasi. Jelaskan command build/test/run dan tujuan tulisnya sebelum dijalankan serta pastikan data asli terlindungi.

Patuhi keputusan ekonomi, waktu 48x, AON, kebijakan darurat save dan seluruh aturan terbaru dalam plan. Jangan memilih ambang, fee, spread, harga atau manfaat baru sendiri. Pilihan UI penyelarasan adalah Kalender dan jam sesuai preview yang dipilih; perubahan tampilan/teks/navigasi/alur lain tetap membutuhkan preview dan persetujuan eksplisit.

Kerjakan bertahap sesuai urutan plan, periksa, checkpoint, lalu lanjutkan pekerjaan yang sudah diizinkan. Jangan berhenti pada penyusunan rencana saja. Jangan meluncurkan agent lain, menjalankan plan lama, mengubah pengaturan global, melakukan ADB/instalasi/tindakan perangkat, commit, push atau publikasi tanpa instruksi/izin yang berlaku.

Tinggalkan regression check yang relevan untuk perbaikan nontrivial dengan runner yang ada. Pisahkan bukti host, browser, build APK, emulator dan perangkat. Jangan menyatakan temuan potensial terbukti tanpa reproduksi; jangan menyatakan perbaikan lulus runtime hanya karena sintaks/compile/preview/APK berhasil.

Serahkan laporan setiap ID: status awal dan akhir, file/fungsi yang berubah, alasan tiap perubahan, command dan input QA, hasil/bukti, keterbatasan, pekerjaan tertunda dan next_action. Sertakan pemetaan ID/requirement ke perubahan dan pengujiannya serta hasil pemeriksaan bahwa perubahan sendiri tetap dalam lingkup. Bila ada blocker nyata atau keputusan UI yang masih diperlukan, jelaskan dan lanjutkan bagian independen yang diizinkan; jangan mengklaim seluruh pekerjaan selesai.
