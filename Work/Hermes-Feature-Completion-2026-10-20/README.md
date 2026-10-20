# Serah-terima penuntasan fitur Game Saham

Tanggal: 20 Oktober 2026, Asia/Jakarta. Status paket: **SPESIFIKASI DAN HASIL PEMETAAN, BUKAN BUKTI IMPLEMENTASI SELESAI**.

## Keputusan pemilik untuk paket ini

Lima jawaban terbaru pemilik menjadi otoritas tambahan:

1. Hasil berupa Markdown, matriks CSV, dan prompt mulai, disimpan pada repo produksi.
2. Gunakan screenshot dan hasil REA existing. Tidak melakukan inspeksi ADB tambahan. Aturan versi kita boleh ditetapkan mandiri, tetapi harus ditandai sebagai keputusan desain, bukan rumus aplikasi referensi yang berhasil dipulihkan.
3. Wajib menuntaskan reksa dana berbasis unit/NAB dan pinjaman bert tenor selain margin, masing-masing dengan biaya, risiko, serta alur lengkap.
4. Misi harian/mingguan, liga, rival, dan leaderboard memakai kalender game; aktif, offline, dan Skip melewati pipeline dunia yang sama.
5. Spesifikasi mencakup susunan layar, akses menu, aksi, state, kontrak data, logika, serta acceptance test.

Keputusan ini menambah mandat master. Tidak menghapus FR-01 sampai FR-20, 228 subrequirement PRD, 26 kelompok referensi, atau 23 acceptance pemilik dalam seed 277 baris.

## Mulai dari sini

Root produksi: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`.

Repo `C:/Users/HYPE AMD/Downloads/VIBE CODING/Game Trading Saham` adalah referensi historis berbeda; jangan ditulis, direset, atau dipakai sebagai checkout produksi.

1. Baca [prompt mulai](START-HERMES.md), lalu README ini dan [spesifikasi lengkap](SPECIFICATION.md).
2. Baca [prompt master](../../Prompts/Hermes-Final-UI-Gameplay-Multi-Agent-2026-10-20.md), [bahan awal](../Hermes-Handoff-2026-10-20/README.md), dan [standar fase](../Hermes-Handoff-2026-10-20/PHASE-QUALITY-STANDARDS.md).
3. Jalankan `python Work/Hermes-Feature-Completion-2026-10-20/verify_package.py --self-test` dari root produksi. Ini hanya validasi bahan, tidak meluluskan gameplay.
4. Inventaris current root/Git/diff/save/proses. Reproduksi audit pada save uji, perbarui status operasional berdasarkan bukti, lalu implementasikan.

## Isi dan penggunaan

| Berkas | Fungsi |
|---|---|
| SPECIFICATION.md | Kontrak bersama, seluruh 26 kelompok, layar/aksi/state/data, rancangan ekonomi baru, audit F1–F9, urutan, QA dan gate. |
| GAP-MATRIX.csv | Daftar kerja tambahan dengan parent requirement, baseline, prioritas, fase, dependensi, tindakan, acceptance dan kolom bukti. |
| START-HERMES.md | Instruksi siap diberikan kepada Hermes untuk eksekusi otonom/multi-agent. |
| MATERIALS.json | Keputusan terbaru, identitas snapshot, inventaris/hash paket dan sumber yang dirujuk. |
| verify_package.py | Pemeriksaan ID, cakupan 26 kelompok/F1–F9, dependensi, link, referensi seed dan integritas snapshot bahan; bukan validator game. |

CSV merupakan **matriks delta** terhadap seed 277 baris. Ia tidak menggantikan atau mengecilkan seed. Pilih satu tracker operasional dengan seluruh ID existing dan ID baru dipetakan; jangan menjalankan dua sumber status yang saling bertentangan. Kolom `baseline_status` adalah hasil inspeksi saat paket dibuat; `verification_status=NOT_TESTED` berarti acceptance baru belum dijalankan.

Jangan memberi PASS pada baris kelompok sebelum seluruh anak wajib dan dependency lulus. `REFERENCE_UNCERTAIN` pada deposito bukan N/A karena sulit dikerjakan: ada bukti literal removed sehingga keberadaan fitur aktif pada referensi memang belum terbukti. Keputusan tidak memasukkan produk deposito baru pada paket ini dicatat sebagai asumsi desain D12; bukan penghapusan pinjaman/reksa dana atau requirement deposito yang benar-benar ditemukan pada PRD.

## Baseline dan batas bukti

- Snapshot Git saat persiapan: branch `codex/owner-audit-portrait-v8`, HEAD `196a49d4f574fcd2c57c792797913541aa126f9b`, dengan banyak perubahan lokal. Hash ini bukan target rollback.
- Snapshot source dan rujukan beserta SHA-256 ada dalam MATERIALS.json. Jika Hermes mengubah source setelah paket dibuat, inspeksi ulang; jangan menimpa pekerjaan baru agar hash kembali sama.
- Audit independen 20 Oktober ada di [REPORT.md](../../Artifacts/Independent-Review-2026-10-20/REPORT.md), [hasil probe](../../Artifacts/Independent-Review-2026-10-20/probe-results.json), dan AuditProbe.java dalam folder yang sama.
- Pada audit tersebut 293 tes JVM lulus, tetapi ada reproduksi kegagalan replay, pemajuan waktu besar, reset perusahaan, dan cakupan net worth. Jumlah tes tidak meluluskan fitur yang belum tersambung ke UI.
- [Peta REA](../REA-FEATURE-MAP.csv), [analisis APK](../REA-APK-ANALYSIS.md), dan [acuan HP](../PHONE-UI-REFERENCE.md) merupakan sumber bukti. Status lama pada peta REA tidak otomatis mencerminkan source yang lebih baru.
- Empat belas PNG di `Artifacts/Phone-UI-Reference/` membuktikan layar tertentu. Perusahaan/pegawai/morale/prestige/archetype juga mempunyai bukti URI/literal APK, tetapi seluruh runtime dan formula lama belum dipulihkan.
- Tidak mengklaim seluruh fitur referensi pernah dibuka, source Dart/server telah didapatkan, atau rumus baru di bawah identik dengan aplikasi lama.
- Paket ini tidak mengubah source game, tidak membangun/memasang APK, dan tidak menjalankan Hermes. Semua hasil implementasi harus diperoleh pada eksekusi berikutnya.

## Ringkasan 26 kelompok

| REF | Kelompok | Baseline yang harus ditindaklanjuti |
|---|---|---|
| shell | Navigasi/scroll | Ada kerangka; Bisnis masih firma dan Lainnya masih riset. |
| wealth | Kekayaan/kas | Ada header/hero, tetapi perusahaan/koleksi belum terkonsolidasi benar. |
| market | Daftar/kategori pasar | Ada saham/search/filter; navigasi produk belum utuh. |
| chart | Chart/detail | Ada riwayat game dan timeframe; metrik/detail belum lengkap. |
| trade | Order/transaksi | Ada flow dasar; audit end-to-end dan regresi tetap wajib. |
| allocation | Alokasi | Kas/saham/bar sederhana; belum seluruh kelas kepemilikan. |
| health | Kesehatan | Risiko margin tersedia; kesehatan kekayaan luas berbeda. |
| loans | Pinjaman | Margin ada; produk tenor baru wajib dibangun. |
| ipo | IPO | Flow dasar ada; audit kalender/dana/refund. |
| funds | Reksa dana | Fund Builder bukan kepemilikan unit/NAB. |
| deposits | Deposito | Keaktifan referensi tidak terbukti; literal removed. |
| missions | Misi | Tiga misi karier; belum sistem periodik lengkap. |
| achievements | Badge/XP | Enam badge; perlu cakupan kegiatan baru dan validasi event. |
| ranks | Rank/liga | Karier/XP bukan liga periodik. |
| rivals | Rival | NPC pasar bukan rival personal persisten. |
| leaderboard | Peringkat | UI/kontrak peringkat NPC belum ditemukan. |
| collection | Koleksi | Domain/bridge enam item; UI belum tersambung. |
| empire | Perusahaan | Domain/bridge ada; dashboard operasional belum tersambung. |
| employees | Pegawai | Model peran/gaji/level; workflow manajemen belum utuh. |
| morale | Kebijakan kerja | Model ada; UI dan seluruh konsekuensi perlu integrasi. |
| empire-invest | Kas perusahaan | Transfer ada; UI/valuasi/reset perlu perbaikan. |
| prestige | Prestige | Implementasi belum ditemukan; invocation selalu opsional. |
| archetype | Gaya investor | Implementasi belum ditemukan. |
| daily-report | Laporan | Ada sesi; perlu lintas bisnis/produk dan statistik. |
| news | Berita/riset | Ada flow; manfaat entitlement/data dan konteks perlu audit. |
| profile | Profil | Pengaturan save ada; identitas/statistik/preferensi belum utuh. |

## Urutan pekerjaan dan wewenang

Kerjakan R0 preflight → R1 waktu/save/reset/net worth → R2 navigasi dan snapshot → R3 perusahaan/pegawai/koleksi → R4 reksa dana/pinjaman → R5 misi/liga/rival/profil → R6 replay dan sisa PRD → R7 integrasi/UX/performa/APK. Penelitian dataset boleh dimulai paralel sejak R0. R0–R7 adalah urutan remediasi, **bukan delapan fase pengganti 14 fase existing**; CSV memetakan fase 00–13.

Multi-agent: satu koordinator, hingga tiga worker menurut kemampuan nyata; file/port/save/output shared satu writer. Worker domain, UI, dan QA/data tidak boleh bersamaan mengedit facade/bridge/save schema atau mereset host yang sama. Review agen berbeda per fase bila tersedia; fallback review terpisah harus dinyatakan apa adanya.

Tidak mengubah global Hermes/profil/model/provider/credentials, tidak membuat scheduler, tidak push/publish, tidak membayar, tidak mengirim pesan pihak lain. Commit lokal selektif boleh setelah verifikasi. Instalasikan/periksa Android hanya pada tahap pemilik berikutnya; sekarang berikan APK + bukti domain/browser, dengan native PENDING terpisah.

Setelah dua kegagalan sama tanpa diagnosis baru: PERLU PERHATIAN dan ubah pendekatan. Setelah tiga: kerjakan bagian independen atau catat blocker nyata. Jangan menurunkan standar, membuat loop tanpa diagnosis, atau berhenti pada rencana ketika implementasi masih dapat dilanjutkan.
