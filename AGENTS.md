# Instruksi aktif Game Saham 2D Portrait

Root yang diizinkan ditulis: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM.
Tanggal keputusan produk: 18 Oktober 2026. Baca Docs/Product/01-Konsep-Game-2D-Portrait.md, Docs/Product/02-PRD-Game-Saham-2D-Portrait.md, lalu Work/README.md sebelum implementasi.

## Arah yang berlaku

Bangun Android 2D portrait dengan trading dan karier sama penting. Semua fitur lama dalam FR-01 sampai FR-20 wajib rilis pertama; 14 paket pekerjaan baru adalah urutan implementasi, bukan izin memangkas scope. Seluruh cakupan 26 layar lama harus terlayani. Hapus kewajiban 3D, landscape, Unity, Three.js dan navigasi kamera dari keputusan lama. Hasil akhir APK; bukti build, instalasi, emulator dan perangkat fisik berbeda.

Repo awal ini hanya paket dokumen dan tugas, bukan source game. Jangan mengklaim test, dataset, APK atau gameplay sudah tersedia. Repo lama adalah referensi dokumen di C:/Users/HYPE AMD/Downloads/VIBE CODING/Game Trading Saham. Jangan menulis repo lama atau mengimpor source/save/build/credentials secara otomatis. Pembacaan source lama dibatasi sampai pemilik secara eksplisit mengizinkannya; tugas awal ini menyiapkan rebuild berdasarkan dokumen.

## Cara kerja

- Buktikan cwd dan Git root sebelum edit. Pertahankan semua perubahan lokal. Jangan reset/clean/delete massal.
- Mulai dari Work/STATUS.json dan task aktif. Jangan memakai checkpoint/session proyek lama. Jika butuh resume Hermes, catat exact session ID; jangan mengandalkan latest.
- Satu writer. Jangan membuat launcher/scheduler/global automation baru. Jangan mengubah profil/provider/model/credentials atau pengaturan global Hermes.
- Kerjakan satu hasil terukur, periksa, checkpoint, lalu lanjut. Jangan berhenti hanya karena durasi/iterasi atau menghemat token. Hormati STOP dan hambatan nyata; jangan loop tanpa diagnosis.
- Sesudah dua kegagalan sama tanpa bukti/diagnosis baru, kirim PERLU PERHATIAN dengan bukti dan pendekatan berbeda. Sesudah tiga, alihkan ke pekerjaan independen atau laporkan BLOCKED jika benar-benar membutuhkan tindakan luar.
- Laporkan selesai x/14, task berjalan/pending, file berubah, command dan bukti, blocker, serta next_action. Ini aturan pelaporan, bukan timer/deteksi loop yang dijamin.
- Dilarang menandai selesai karena folder, dokumen, mock, static check, atau APK saja. Kaitkan setiap requirement dan gate ke bukti yang sesuai.
- Jangan push, publish, mengirim pesan pihak lain, membeli layanan, atau menyentuh repo/data lain. Commit lokal hanya file tugas yang sudah ditinjau dan diperiksa; jangan commit secrets, data pemain, dataset tanpa hak, atau binary besar.
- Keputusan teknis rutin yang reversibel boleh ditetapkan dengan alasan. Keputusan produk/data yang masih OQ tidak dianggap disetujui hanya karena pemilik meminta mulai coding. Kerjakan bagian independen sambil meminta klarifikasi yang benar-benar diperlukan.
- Baseline C# + MAUI HybridWebView + HTML/CSS/JS lokal perlu feasibility check. Hindari engine/dependency/frontend framework baru tanpa kebutuhan. Pilih satu pemilik ekonomi; UI hanya presentasi/input.
- Dataset historis harus nyata dengan provenance dan hak penggunaan yang ditinjau. Fixture sintetis boleh untuk tests berlabel dan tidak meluluskan replay historis.
- Jika memakai paket aplikasi lama sebagai update, pertahankan application ID com.tradingglobal3d.mobile, signing dan progres. Jangan mengasumsikan key/save lama tersedia di repo baru.

## Ponytail minimum yang benar

Pahami alur lengkap dan telusuri semua caller sebelum mengubah fungsi. Urutan: perlu dibangun -> existing -> standard library -> native platform -> dependency terpasang -> minimum code baru. Tanpa abstraksi/framework/dependency spekulatif. Tandai shortcut disengaja dengan ponytail: dan sebut batas serta upgrade path bila ada.

Jangan memangkas validasi batas input, keamanan uang, penanganan data loss, aksesibilitas atau scope eksplisit. Nontrivial logic menyisakan satu runnable check minimum menggunakan runner yang ada; tanpa framework/fixture baru hanya untuk checklist.
