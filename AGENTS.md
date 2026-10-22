# Instruksi aktif Game Saham 2D Portrait

Root yang diizinkan ditulis: C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM.

Mandat redesain pemilik 22 Oktober 2026 (dikonfirmasi 23 Oktober): seluruh keputusan visual dan UX lama, termasuk Design Brief 03, UI handoff Hermes 20 Oktober dan susunan lima tab, digantikan oleh [DESIGN.md](DESIGN.md). Pemilik menyetujui palet arang dengan aksen ungu serta empat tab Ringkasan/Pasar/Aset/Karier; semua fitur tetap terjangkau. Terapkan antislop core/ui/human/layoutmobile/copywriting mode during dan Delivery Gate empat blok. Scope FR-01 sampai FR-20, ekonomi/waktu/save Java dan standar kualitas fase tetap berlaku; hanya presentasi HTML/CSS/JS serta navigasi UI berubah. Referensi adalah mood board, bukan aset untuk disalin; baca folder Reference saja bila mengakses checkout lama. Jangan push atau menulis di luar root yang diizinkan.
Izin lanjutan pemilik 23 Oktober 2026: setelah 17 kegagalan baseline ditemukan, perbaikan Java dan fixture terkait diizinkan secara terbatas untuk memenuhi mandat aktif 1 hari game = 5 menit nyata dan menuntaskan regresi terkonfirmasi; pertahankan rumus, tarif, hadiah, dan batas ekonomi lain. Bedakan koreksi implementasi/fixture dari perubahan aturan ekonomi, dan jalankan kembali seluruh gate.
Tanggal keputusan produk: 18 Oktober 2026. Baca Docs/Product/01-Konsep-Game-2D-Portrait.md, Docs/Product/02-PRD-Game-Saham-2D-Portrait.md, Docs/Design/03-Design-Brief-Game-Saham-2D-Portrait.md, lalu Work/README.md sebelum menulis kode game.

Mandat pemilik terbaru 20 Oktober 2026: [prompt master Hermes](Prompts/Hermes-Final-UI-Gameplay-Multi-Agent-2026-10-20.md) dan [bahan eksekusi lengkap](Work/Hermes-Handoff-2026-10-20/README.md). Baca sebelum melanjutkan. Keputusan di sana menggantikan bagian dokumen historis yang bertentangan: UI referensi HP, lima tab Beranda/Pasar/Portofolio/Bisnis/Lainnya, multi-agent terkoordinasi, aturan gameplay otonom, game tanpa tamat, APK+tes logika/browser untuk penyerahan ini. Aktif 1×: 1 hari game = 5 menit nyata; background/ditutup: 1 hari game = 1 jam nyata tanpa mewarisi speed foreground. Bukan auto-pause atau catch-up 5 menit/hari. Ini requirement implementasi, bukan bukti source sudah memenuhinya.

Standar fase dikonfirmasi pemilik: [PHASE-QUALITY-STANDARDS.md](Work/Hermes-Handoff-2026-10-20/PHASE-QUALITY-STANDARDS.md). Seluruh 14 fase wajib seluruh kriteria lulus dan nol bug terbuka diketahui termasuk minor; UI seluruh layar/state/viewport dan teks 200%; fase kritis minimal 10×100 aksi dengan invariant; P95 aksi lokal ≤300 ms, navigasi ≤1 detik, feedback berat ≤300 ms/UI responsif; review berbeda setiap fase bila tersedia. Hasil di bawah standar → diagnosis/perbaikan/uji ulang/review, bukan penurunan ambang atau COMPLETE. Tugas independen boleh lanjut, dependency gagal tidak dianggap lulus. Penundaan native tetap dinyatakan sesuai scope.

Tambahan keputusan pemilik 20 Oktober 2026: baca [paket penuntasan fitur](Work/Hermes-Feature-Completion-2026-10-20/README.md) dan [prompt mulai](Work/Hermes-Feature-Completion-2026-10-20/START-HERMES.md). Gunakan screenshot/REA existing tanpa inspeksi ADB baru; aturan versi kita ditandai asumsi desain. Reksa dana unit/NAB dan pinjaman tenor selain margin wajib utuh. Misi harian/mingguan, liga/rival/peringkat mengikuti kalender game yang sama pada aktif/offline/Skip. Delta fitur menambah 277 baris bahan existing, tidak menggantikan atau meluluskan requirement tersebut.

## Arah yang berlaku

Bangun Android 2D portrait dengan trading dan karier sama penting. Semua fitur lama dalam FR-01 sampai FR-20 wajib rilis pertama; 14 paket pekerjaan baru adalah urutan implementasi, bukan izin memangkas scope. Seluruh cakupan 26 layar lama harus terlayani. Hapus kewajiban 3D, landscape, Unity, Three.js dan navigasi kamera dari keputusan lama. Hasil akhir APK; bukti build, instalasi, emulator dan perangkat fisik berbeda.

Repo mempunyai source produksi Java + native Android WebView dan UI HTML/CSS/JS lokal. Verifikasi baseline source/test/APK aktual; keberadaannya bukan bukti semua fitur sudah selesai. Repo lama adalah referensi dokumen di C:/Users/HYPE AMD/Downloads/VIBE CODING/Game Trading Saham. Jangan menulis repo lama atau mengimpor source/save/build/credentials secara otomatis. Pembacaan source lama dibatasi sampai pemilik secara eksplisit mengizinkannya; gunakan dokumen dan bukti referensi yang sudah tersedia.

## Cara kerja

- Design brief wajib dibaca sebelum implementasi: tiga prinsip, token bersama, 40 tujuan screen, 20 journey, 28 komponen, state, responsive dan aksesibilitas berlaku pada seluruh UI. Desain telah ditetapkan melalui mandat desain; jangan menggantinya per screen atau menganggap OQ ekonomi/data selesai. Brief bukan bukti UI/native sudah diuji.
- Buktikan cwd dan Git root sebelum edit. Pertahankan semua perubahan lokal. Jangan reset/clean/delete massal.
- Mulai dari Work/STATUS.json dan task aktif. Jangan memakai checkpoint/session proyek lama. Jika butuh resume Hermes, catat exact session ID; jangan mengandalkan latest.
- Multi-agent sesuai kemampuan nyata diizinkan, satu koordinator integrasi dan satu writer per file/shared state; kepemilikan file dan kontrak ditetapkan sebelum kerja paralel. Tidak menjalankan launcher paralel pada sesi sama. Jangan membuat launcher/scheduler/global automation baru. Jangan mengubah profil/provider/model/credentials atau pengaturan global Hermes.
- Kerjakan satu hasil terukur, periksa, checkpoint, lalu lanjut. Jangan berhenti hanya karena durasi/iterasi atau menghemat token. Hormati STOP dan hambatan nyata; jangan loop tanpa diagnosis.
- Sesudah dua kegagalan sama tanpa bukti/diagnosis baru, kirim PERLU PERHATIAN dengan bukti dan pendekatan berbeda. Sesudah tiga, alihkan ke pekerjaan independen atau laporkan BLOCKED jika benar-benar membutuhkan tindakan luar.
- Laporkan selesai x/14, task berjalan/pending, file berubah, command dan bukti, blocker, serta next_action. Ini aturan pelaporan, bukan timer/deteksi loop yang dijamin.
- Dilarang menandai selesai karena folder, dokumen, mock, static check, atau APK saja. Kaitkan setiap requirement dan gate ke bukti yang sesuai.
- Jangan push, publish, mengirim pesan pihak lain, membeli layanan, atau menyentuh repo/data lain. Commit lokal hanya file tugas yang sudah ditinjau dan diperiksa; jangan commit secrets, data pemain, dataset tanpa hak, atau binary besar.
- Keputusan teknis dan aturan simulasi yang belum diputuskan boleh ditetapkan Hermes sesuai mandat terbaru, dengan alasan dan tes. Kewenangan ini tidak memberikan hak dataset pihak ketiga, izin pembayaran/publikasi, atau izin menghapus requirement wajib. Kerjakan bagian independen saat ada hambatan nyata.
- Pertahankan produksi Java + native Android WebView + HTML/CSS/JS lokal. Domain Java satu pemilik ekonomi, waktu dan save; UI hanya presentasi/input. Rekomendasi C#/MAUI historis tidak berlaku. Hindari engine/dependency/frontend framework baru tanpa kebutuhan.
- Dataset historis harus nyata dengan provenance dan hak penggunaan yang ditinjau. Fixture sintetis boleh untuk tests berlabel dan tidak meluluskan replay historis.
- Jika memakai paket aplikasi lama sebagai update, pertahankan application ID com.tradingglobal3d.mobile, signing dan progres. Jangan mengasumsikan key/save lama tersedia di repo baru.

## Ponytail minimum yang benar

Pahami alur lengkap dan telusuri semua caller sebelum mengubah fungsi. Urutan: perlu dibangun -> existing -> standard library -> native platform -> dependency terpasang -> minimum code baru. Tanpa abstraksi/framework/dependency spekulatif. Tandai shortcut disengaja dengan ponytail: dan sebut batas serta upgrade path bila ada.

Jangan memangkas validasi batas input, keamanan uang, penanganan data loss, aksesibilitas atau scope eksplisit. Nontrivial logic menyisakan satu runnable check minimum menggunakan runner yang ada; tanpa framework/fixture baru hanya untuk checklist.
