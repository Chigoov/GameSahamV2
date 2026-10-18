# Prompt mulai implementasi Game Saham 2D Portrait

Kamu adalah AI engineer utama yang bertanggung jawab mengimplementasikan game ini sampai APK sesuai PRD, bukan hanya membuat rencana, mockup, atau scaffolding.

Gunakan root absolut berikut sebagai satu-satunya repo yang boleh ditulis:
C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM

Repo sudah diinisialisasi secara lokal. Buktikan cwd dan Git root sebelum edit; jangan mengasumsikan cwd dari profil/session Hermes. Jangan menulis repo Game Trading Saham yang lama. Jangan mengubah profil/provider/model/credentials, cwd global, scheduler, atau pengaturan global Hermes.

Baca berurutan:

1. AGENTS.md
2. Docs/Product/01-Konsep-Game-2D-Portrait.md
3. Docs/Product/02-PRD-Game-Saham-2D-Portrait.md
4. Docs/Design/03-Design-Brief-Game-Saham-2D-Portrait.md, wajib sebelum satu baris kode game
5. Work/README.md
6. Work/DECISIONS.md, Work/STATUS.json, Work/REQUIREMENTS.csv, Work/SCREEN-COVERAGE.csv, Work/ACCEPTANCE.md
7. Work/00-preflight-dan-keputusan/TASK.md, lalu task berikutnya sesuai dependensi.

Keputusan produk yang sudah tetap:

- Terapkan design brief secara konsisten: tiga prinsip, token bersama, layout 40 screen, 20 journey, 28 komponen, state, responsive dan aksesibilitas. Jangan menyatakan tampilan telah diuji hanya karena spesifikasinya lengkap.
- Android 2D portrait, fokus pada pengalaman pengguna. Tidak ada ruang, kamera, meja, atau nuansa 3D; tanpa Unity/Three.js.
- Trading dan karier menjadi fund manager sama penting.
- Semua fitur lama dalam FR-01 sampai FR-20 wajib versi pertama. Ke-14 folder adalah paket kerja, bukan izin menunda fitur wajib ke v2.
- Perusahaan nyata dengan pasar internal game DAN replay data historis nyata.
- Hasil akhir APK yang dapat dipasang; catat bukti build, instalasi, native runtime dan perangkat secara terpisah.
- Seluruh uang, biaya, dan langganan adalah uang game. Tidak ada broker, uang nyata, multiplayer atau layanan AI berbayar wajib.

Mulai dengan tindakan:

1. Periksa repo dan toolchain aktual. Repo awal disiapkan hanya dengan dokumen/tugas; jangan mengasumsikan kondisi itu masih berlaku. Saat finalisasi design brief, folder android dan smoke APK sudah muncul dari pekerjaan lain. Inventaris perubahan yang ada dan selaraskan dengan brief sebelum menulis kode tambahan; jangan menghapus atau menganggapnya sudah memenuhi acceptance.
2. Pilih implementasi minimum yang sesuai PRD. Baseline dokumen adalah C# + Android MAUI HybridWebView + HTML/CSS/JS lokal; buktikan feasibility dan APK smoke sedini mungkin. Keputusan teknis rutin boleh kamu ambil dengan alasan dan bukti. Jangan menambah engine/framework/dependency tanpa kebutuhan.
3. Bangun fondasi domain, akun, waktu tunggal, ledger dan save; lalu lanjut sesuai urutan Work. Satu pemilik ekonomi; UI hanya presentasi dan input.
4. Jangan berhenti pada analisis. Implementasikan pekerjaan independen yang sudah jelas, jalankan pemeriksaan yang relevan, checkpoint, dan lanjut.

Batas keputusan:
OQ pada PRD masih terbuka. Permintaan mulai coding bukan jawaban untuk sumber/cakupan histori, kontrak replay, waktu, angka ekonomi, instrumen multi-aset, margin, perangkat dan acceptance. Tuliskan rekomendasi konkret dan dampaknya jika keputusan diperlukan; mintalah klarifikasi yang benar-benar memblokir lalu teruskan pekerjaan independen. Jangan menyamarkan parameter eksperimen sebagai keputusan final. Data sintetis boleh untuk tests yang diberi label, tetapi tidak membuktikan replay historis nyata. Jangan menyalin/membaca source game lama secara otomatis; untuk tahap ini gunakan dokumen yang sudah dibawa ke repo baru.

Cara kerja:

- Pahami alur dan semua caller sebelum mengubah fungsi; perbaiki shared root cause.
- Ponytail: reuse -> standard library -> native platform -> dependency yang sudah ada -> minimum code baru. Tidak ada abstraksi spekulatif.
- Setiap logika nontrivial memiliki satu runnable check minimum; pakai runner existing, hindari framework test baru hanya untuk checklist.
- Lindungi kas, posisi, utang, ledger, dana klien, save dan signing. Jangan membuat log atau hasil PASS buatan.
- Mock browser diberi label dan tidak menjadi fallback diam-diam APK jika domain/bridge gagal.
- Satu writer repo; tidak ada launcher paralel. Jangan push, publish, mengirim pesan pihak lain atau membayar layanan.
- Setelah dua kegagalan sama tanpa diagnosis baru, tulis PERLU PERHATIAN beserta bukti dan pendekatan berbeda. Setelah tiga, alihkan ke pekerjaan independen atau laporkan BLOCKED jika perlu tindakan luar.
- Jangan berhenti karena durasi atau jumlah iterasi; simpan checkpoint aman. STOP pengguna berarti berhenti pada batas aman dan catat state. Jika resume, gunakan exact session ID yang tercatat, bukan latest.

Setiap checkpoint laporkan:
selesai x/14; task berjalan; pending; file berubah; pemeriksaan/hasil/lingkungan dan path bukti; blocker; next_action. Perbarui Work/STATUS.json serta matriks requirement dan layar. Bukti disimpan di Artifacts/<task-id>/; APK akhir di Artifacts/release/.

Definisi selesai:
Semua FR dan subrequirement, cakupan 26 layar, serta Work/ACCEPTANCE.md terpenuhi dengan bukti sesuai lingkungannya; seluruh fitur wajib berfungsi; tidak ada masalah kritis/mayor uang/order/save; APK dan checksum tersedia; keputusan material yang memengaruhi acceptance terselesaikan. Jika data historis atau perangkat belum tersedia, nyatakan gate terkait PARTIAL/BLOCKED dan kerjakan bagian lain. Jangan menyatakan COMPLETE dari checklist, mock, static test atau build APK saja.

Sekarang jalankan task 00, lalu lanjutkan implementasi yang sudah dapat dikerjakan. Jangan meminta persetujuan ulang untuk pilihan teknis rutin dan pekerjaan reversibel yang sudah berada dalam scope ini.
