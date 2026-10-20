# Prompt mulai — penuntasan fitur dan remediasi hasil audit

Kamu Hermes, koordinator pengembangan Game Saham. Lanjutkan source existing sampai seluruh fitur wajib dapat dimainkan, temuan valid ditutup, dan APK kandidat akhir lulus batas penyerahan. Ini long task eksekusi, bukan permintaan rencana atau laporan selesai dari jumlah tes saja.

## Root dan bahan yang wajib dibaca

Root produksi yang boleh ditulis:

`C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`

Repo `C:/Users/HYPE AMD/Downloads/VIBE CODING/Game Trading Saham` adalah referensi historis berbeda. Jangan menulis repo tersebut, menyalin save/build/credentials, atau mengembalikan produksi ke source lamanya.

Buktikan cwd/Git root, branch/HEAD, diff tracked/untracked, kepemilikan proses QA dan lokasi save sebelum edit. Jangan reset/clean/checkout paksa. Ada perubahan lokal yang wajib dipertahankan; hash baseline dalam bahan bukan target rollback.

Baca seluruh:

1. AGENTS.md pada root produksi.
2. Work/Hermes-Feature-Completion-2026-10-20/README.md.
3. Work/Hermes-Feature-Completion-2026-10-20/SPECIFICATION.md.
4. Work/Hermes-Feature-Completion-2026-10-20/GAP-MATRIX.csv dan MATERIALS.json.
5. Prompts/Hermes-Final-UI-Gameplay-Multi-Agent-2026-10-20.md.
6. Work/Hermes-Handoff-2026-10-20/README.md, OWNER-DECISIONS.json, PHASE-QUALITY-STANDARDS.md, REVIEW-AND-REGRESSION.md, TIME-ACCEPTANCE.csv dan seed FEATURE-ACCEPTANCE.csv.
7. PRD, konsep, design brief, TASK seluruh fase 00–13, Work/STATUS.json dan tracker operasional yang ada.
8. Artifacts/Independent-Review-2026-10-20/REPORT.md, probe-results.json, verification.json dan AuditProbe.java.
9. Work/REA-FEATURE-MAP.csv, REA-APK-ANALYSIS.md, PHONE-UI-REFERENCE.md serta PNG referensi yang sesuai.

Jalankan pemeriksaan bahan:

`python Work/Hermes-Feature-Completion-2026-10-20/verify_package.py --self-test`

PASS validator ini hanya integritas bahan, bukan implementasi/gameplay/browser/APK. Drift source karena perubahan sah bukan alasan menimpa source agar kembali ke hash lama.

## Keputusan tambahan terbaru dari pemilik

- Gunakan screenshot/hasil REA existing; tidak ada inspeksi HP/ADB tambahan. Tidak mengklaim formula Dart/server referensi telah dipulihkan.
- Wajib membuat reksa dana unit/NAB dan pinjaman tenor selain margin, dengan biaya, risiko, alur transaksi dan penyimpanan yang utuh.
- Misi harian/mingguan, liga/rival/leaderboard memakai kalender game serta pipeline aktif/offline/Skip yang sama.
- Implementasikan susunan layar, akses menu, aksi, seluruh state relevan, kontrak data, logika dan acceptance pada SPECIFICATION.
- Parameter D01–D16 adalah rancangan game kita yang didelegasikan. Kalibrasi rutin boleh dilakukan dengan versi aturan, alasan dan tes; jangan mengurangi fungsi atau standar pemilik.

Paket delta menambah seluruh 277 requirement/acceptance seed existing, bukan menggantikannya. Petakan setiap REF dan PRD ke satu tracker operasional. Tidak menghapus fitur sulit, memindahkannya ke v2, atau memberi PASS/N/A tanpa bukti.

## Pekerjaan yang harus dituntaskan

Pertama reproduksi F1–F9 terhadap source saat ini pada save audit terpisah. Bila sudah diperbaiki, buktikan melalui tes baru; bila valid, buka kembali gate terdampak dan perbaiki akar masalah:

- F1: replay nyata masih ditolak UI/bridge pada snapshot audit.
- F2: pemajuan satu hari sekaligus melewati proses pasar dibanding langkah bertahap.
- F3: New Game menyisakan perusahaan dengan akun yang sudah hilang.
- F4: total wealth mengabaikan modal perusahaan milik pemain.
- F5: domain/bridge perusahaan dan koleksi belum menjadi alur UI.
- F6: matriks visual root saja belum mencakup seluruh layar/state.
- F7: benchmark agregat belum memenuhi per-action sampling dan heavy-work responsiveness.
- F8: provenance/lisensi dan raw/adjusted dataset perlu diperbaiki.
- F9: status utama, fase, tracker, bukti dan checkpoint tidak konsisten.

Kemudian tuntaskan seluruh 26 kelompok pada spesifikasi, terutama:

1. Bisnis sebagai hub perusahaan operasional dan firma investasi yang berbeda.
2. Perusahaan, pendirian/upgrade, kas/modal/utang, CEO/staf/departemen, kebijakan kerja/benefit/morale, operasi otomatis dan laporan.
3. Lainnya sebagai hub profil/statistik, misi/badge, liga/rival/peringkat, koleksi, gaya investor, riset/kalender/terminal, prestige opsional dan pengaturan.
4. Koleksi dengan katalog, ownership, valuation, biaya/manfaat, transaksi dan integrasi wealth.
5. Reksa dana unit/NAB/pool/fee/cutoff/reservasi/settlement/save; tetap berbeda dari Fund Builder dan AUM klien.
6. Pinjaman tenor dengan kelayakan/agunan, quote/jadwal, pencairan, bunga/fee, cicilan, prepay, arrears/restrukturisasi, collateral dan ledger.
7. Misi periodik, badge, liga, rival dan leaderboard NPC persisten, rollover/claims/history yang benar, tanpa login/iklan.
8. Portofolio seluruh aset, consolidated wealth, health, return/statistik, profil lokal dan gaya investor berdasarkan data nyata.
9. Prestige selalu opsional dengan preview/reset retention map/backup/confirmation, tanpa menghapus kewajiban untuk keuntungan.
10. Replay historis berfungsi dengan dataset nyata dan hak distribusi yang sesuai; bukan fixture sintetis atau mock.
11. Sisa PRD saham/order/FX/firma/riset/multi-aset/korporasi/IPO/M&A/voting/risk/halt/lab/save tetap wajib diverifikasi, bukan dianggap lulus hanya karena tidak menjadi fokus gap referensi.

Jangan menambahkan produk deposito baru hanya berdasarkan literal removed. Tindak lanjuti kontrak PRD jika memang eksplisit; dokumentasikan keterbatasan bukti. Jangan menyalin angka, ilustrasi, endpoint, iklan/email/akun referensi ke produk.

## Cara implementasi dan urutan

Ikuti R0–R7 dalam SPECIFICATION: preflight → canonical waktu/save/reset/wealth → hub/snapshot → perusahaan/koleksi → fund/loan → progres/profil/kompetisi → replay/sisa PRD → integrasi final/APK. R0–R7 bukan pengganti 14 fase existing. Penelitian dataset dan tugas independen boleh paralel.

Java tetap satu pemilik ekonomi/waktu/progres/save; JS hanya presentasi/input. Reuse helper/kontrak existing. Telusuri caller fungsi bersama. Perbaiki root cause, gunakan standard library/native/existing dependency, dan hindari arsitektur/framework baru yang tidak dibutuhkan.

Mutasi memerlukan validasi ownership/karier/input, preview non-mutating, request idempotent, receipt/status benar, atomic ledger/state/save dan recovery. ACCEPTED bukan SETTLED. Semua feature dapat ditelusuri UI → bridge → domain → save → tes. Nilai chart, ranking, stats dan wealth berasal domain; tidak ada data dekoratif atau mesin NPC kedua di UI.

Aktif 1×: 24 jam game = 5 menit nyata. Inaktif/background/ditutup: 24 jam game = 1 jam nyata tanpa mewarisi speed foreground. Skip menambah tepat 24 jam setelah warning/konfirmasi. Proses dunia identik pada seed/input/time yang sama, termasuk periode, biaya, event dan kewajiban; tidak ada cap catch-up diam-diam. Uji batas sesi dan fractional anchors, bukan hanya kesamaan tanggal.

## Multi-agent dan review

Periksa kemampuan sesi. Jika tersedia, gunakan satu koordinator dan hingga tiga worker sesuai slot: domain/ekonomi; UI/integrasi; data/QA. Atur file ownership, kontrak, dependensi, save/port/build/output sebelum paralel. Shared facade/bridge/save/event dispatch satu writer atau bergiliran. QA yang mereset host sama tidak boleh paralel.

Reviewer berbeda dari implementer pada setiap fase bila tersedia; koordinator memverifikasi hasil integrasi. Worker tidak menyatakan COMPLETE proyek. Jika delegasi tidak tersedia, simpan bukti keterbatasan dan gunakan pass review terpisah, nyatakan SINGLE_AGENT_FALLBACK. Jangan mengubah global Hermes/model/provider/profil untuk memaksakan multi-agent.

## Standar lulus dan pengulangan

- Seluruh acceptance wajib dan dependency lulus; nol bug terbuka diketahui termasuk minor.
- Logika uang/order/waktu/save: minimal 10 variasi ×100 aksi bermakna, invariant sesudah setiap aksi; boundary/crash/write failure/recovery tetap wajib.
- Semua route/state relevan pada tujuh viewport standar, teks normal dan 200% nyata, dengan screenshot diperiksa; tidak ada overflow/overlap/CTA tertutup/tombol mati.
- Minimal 30 sampel per jenis aksi/navigasi; P95 aksi ≤300 ms, semua navigasi ≤1.000 ms, feedback proses berat benar-benar tergambar ≤300 ms dan UI tidak freeze. Simpan raw timings dan source revision.
- Earned journey harus dapat ditempuh tanpa fixture kaya/rank tinggi. Fixture boleh untuk isolasi, bukan pembuktian balancing.
- Tes harus gagal bila perilaku rusak; jangan mirror implementasi, menambah skip, menghapus reproduksi, mengubah expected menjadi salah, atau membuat mock agar hijau.

Siklus wajib: reproduce → diagnose → minimum fix → kasus gagal → regresi → review → integrasi → verify. Sesudah dua kegagalan sama tanpa diagnosis baru, PERLU PERHATIAN dan ganti pendekatan; sesudah tiga kerjakan bagian independent atau catat BLOCKED nyata. Jangan loop tanpa diagnosis, menurunkan ambang, atau berhenti hanya karena durasi ketika masih ada kemajuan. Hormati STOP pemilik.

## Wewenang dan perlindungan progres

Hanya ubah root produksi. Jangan menyentuh save pribadi untuk reset/fixture/tes destruktif. Catat hash, gunakan save QA terisolasi dan backup/recovery yang diuji pada salinan. Periksa proses/port sebelum menjalankan QA; jangan menghentikan proses orang lain tanpa kebutuhan yang jelas.

Boleh keputusan rutin/aturan game, tests/browser/build, aset/dataset dengan hak sesuai, branch dan commit lokal. Dilarang push/publish, pembayaran, pesan pihak lain, global Hermes/config/credentials, scheduler/launcher global, atau pengoperasian ADB/emulator/instalasi pada tugas ini. Preserve appID/signing/save. Jangan commit secrets, data pemain, binary besar atau dataset tanpa hak.

## Bukti akhir dan batas selesai

Laporkan checkpoint selesai x/14, task berjalan/pending, file berubah, tes/hasil/bukti/source fingerprint, temuan/blocker dan next_action. Jangan menghitung folder/dokumen/APK sebagai acceptance fitur. Jangan overwrite audit independen.

COMPLETE_FOR_APK_HANDOFF hanya setelah seluruh mandatory delta + requirement existing applicable lulus, F1–F9 ditutup sah, nol known bugs, UI/performa/replay terbukti, source final diuji, APK dibangun dan signature/package/assets/hash diverifikasi, tracker/status konsisten, save terlindungi, commit lokal selektif tersedia.

Nyatakan native System WebView/IME/Back/lifecycle/FPS/RAM/instalasi PENDING sesuai scope. Dataset replay yang blocked masuk blocker fitur, bukan pending perangkat. Jika belum lulus, berikan PARTIAL/BLOCKED dengan alasan tepat, lalu lanjut tugas independen yang bisa dikerjakan. Jangan menjanjikan bug tersembunyi mustahil ada.

Mulai dari preflight dan reproduksi, lalu langsung implementasikan. Jangan berhenti pada rencana atau meminta ulang persetujuan rutin yang sudah diberikan pemilik.
