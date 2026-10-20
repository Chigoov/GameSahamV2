# Paket kerja lengkap Hermes — Game Saham

20 Oktober 2026, Asia/Jakarta. Paket ini menyiapkan eksekusi long task; bukan laporan implementasi baru, bukan source Dart yang berhasil dipulihkan, dan bukan bukti game sudah lulus mandat final.

## Mulai dari sini

Root produksi: `C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM`.

Prompt master: [Hermes-Final-UI-Gameplay-Multi-Agent-2026-10-20.md](../../Prompts/Hermes-Final-UI-Gameplay-Multi-Agent-2026-10-20.md). Baca seluruh isi, kemudian spesifikasi yang dirujuk. Jangan menjalankan prompt lama `Hermes-Mulai-Game-Saham-2D.md` sebagai mandat terbaru.

```powershell
python Work/Hermes-Handoff-2026-10-20/verify_materials.py --self-test
```

Perintah ini memeriksa integritas bahan, kelengkapan matriks dan aritmetika kontrak waktu. Hasilnya **bukan PASS gameplay/domain/browser/APK**. Hash mismatch setelah perubahan sah harus diinspeksi, bukan diperbaiki dengan menimpa source memakai snapshot lama.

Pemeriksaan ini untuk snapshot awal sebelum perubahan. `FEATURE-ACCEPTANCE.csv` tetap sebagai seed; pilih matriks operasional yang ditautkan untuk status implementasi. Jangan mengubah seed menjadi PASS atau memaksa hash dokumen tetap sama setelah edit sah. Simpan bukti pemeriksaan awal dan bukti kode/game final secara terpisah.

## Dokumen dan kegunaannya

| Bahan | Isi dan penggunaan |
|---|---|
| Prompt master | Mandat lengkap: keputusan, cakupan, arsitektur, multi-agent, clock dual-rate, UI, perulangan perbaikan/tes dan completion gate. |
| OWNER-DECISIONS.json | Keputusan yang dapat dibaca mesin. Hanya kontrak spesifikasi; bukan konfigurasi runtime yang sudah terpasang. |
| FEATURE-ACCEPTANCE.csv | 228 requirement existing dengan teks utuh, 26 kelompok referensi, dan 23 acceptance pemilik/lintas fitur, total 277 baris. Semua status mandat baru dimulai NOT_TESTED; prior_status adalah riwayat. |
| TIME-ACCEPTANCE.csv | Contoh numerik waktu foreground/background, speed, interval panjang, fraksi dan kombinasi. Harus diwujudkan sebagai tes domain produksi, bukan hanya dihitung ulang oleh validator bahan. |
| REVIEW-AND-REGRESSION.md | Protokol loop otonom, fault injection, review independen, UI journeys dan bukti akhir. |
| PHASE-QUALITY-STANDARDS.md | Standar minimum tinggi pada seluruh 14 fase: nol bug diketahui termasuk minor, seluruh UI/state/viewport/teks 200%, 10×100 aksi kritis, performa terukur dan review setiap fase. |
| References/Original-Game-Concept.md | Salinan **utuh** dokumen konsep repo lama. Hash dan asal disimpan; instruksi engine/3D/landscape historis sudah digantikan. |
| MATERIALS.json | Inventaris root-relative, ukuran dan SHA-256 bahan; tanggal/baseline dan klasifikasi bukti. Tidak berisi save pribadi atau signing key. |
| verify_materials.py | Validator bahan dan kontrol negatif. Tidak menjalankan game atau mengubah progres. |

Matriks baru adalah bahan awal audit gabungan, bukan kewajiban mempertahankan dua sumber status. Hermes memilih satu matriks operasional, menghubungkan ID ke matriks existing dan memperbarui evidence secara konsisten. Jangan menghapus requirement yang sulit agar denominator tampak kecil.

## Spesifikasi utuh yang tetap wajib dibaca

- `Docs/Product/01-Konsep-Game-2D-Portrait.md`.
- `Docs/Product/02-PRD-Game-Saham-2D-Portrait.md` — seluruh FR/subrequirement, bukan hanya tabel daftar fitur.
- `Docs/Design/03-Design-Brief-Game-Saham-2D-Portrait.md` — 40 tujuan screen, 20 journey, 28 komponen/state; keputusan pemilik terbaru menang bila berbeda.
- `Work/README.md` dan 14 folder `Work/00-*` sampai `Work/13-*`.
- `Work/REQUIREMENTS.csv`, `SCREEN-COVERAGE.csv`, `OWNER-UI-LOGIC-MATRIX.csv`.
- `Work/DECISIONS.md`, `STATUS.json`, `ACCEPTANCE.md`, `HANDOFF-NEXT-SESSION.md`.
- Audit owner, checkpoint karier, checkpoint UI, acuan HP, analisis REA dan seluruh baris peta fitur REA. Lokasi persis dicantumkan prompt dan MATERIALS.json.

## Bukti visual dan analisis yang tersedia

14 PNG referensi ada di `Artifacts/Phone-UI-Reference/`. Gunakan isi PNG dengan pemetaan pada `Work/PHONE-UI-REFERENCE.md`: `10-missions.png` adalah bagian bawah Beranda; `11-missions-view.png` benar-benar misi. XML 07 tidak serentak dengan PNG dan tidak menjadi bukti posisi elemen.

Hasil analisis REA ada di `Artifacts/REA/evidence/` dan `Artifacts/REA/analysis/`. Itu bukti metadata/struktur/string statis; logika keuangan Dart/backend belum dipulihkan. Lima APK referensi tersedia lokal bila inspeksi tambahan diperlukan, tetapi tidak diperlukan untuk memulai implementasi dan tidak disalin ke produk.

Baseline source `196a49d` mempunyai 158 tes JVM menurut laporan saat itu, runner browser dan APK kandidat debug. Angka lama tidak meluluskan fitur baru atau clock offline. Native runtime kandidat belum terbukti. Verifikasi ulang current state sebelum mengklaim angka terbaru.

## QA lokal dan perlindungan save

- Preview pribadi: `http://127.0.0.1:8766/`, save `Artifacts/LocalPreview/preview-save`. Periksa kepemilikan proses; jangan reset/save fixture di sana.
- QA: port 8767, host Java terpisah, wajib `testFixture: true` dan label `QA HOST JAVA`. Tidak ada klaim Android.
- Launcher existing: `Artifacts/UI-Refresh-2026-10-20/start_qa.py` dengan `UiQaDomainServer.java` dan `bridge-init.js`. Baca ketiganya, verifikasi JDK/build classes/jar dan direktori save sebelum menjalankan.
- Runner: `Work/verify-portrait-ui.py` dan `Work/verify-portrait-flows.py`. Keduanya melakukan mutasi/reset **save QA**. Jangan melemahkan guard agar bisa diarahkan ke preview pribadi.
- Source/build toolchain tetap pada checkout produksi; paket bahan/ZIP tidak menjadi checkout kedua atau izin rebuild dari source lama.

## Multi-agent sudah menjadi opsi eksekusi utama

Mode AUTO mengutamakan multi-agent bila kemampuan nyata tersedia: satu koordinator dan hingga tiga worker, atau satu/dua worker sesuai slot. Periksa kemampuan dan catat hasilnya sebelum memilih fallback single-agent. Tidak mengganti model/provider/profil untuk memaksakan fitur ini.

Setiap worker menerima work order, baseline/root, scope file, kontrak, acceptance, save/port/output tes dan seluruh batas wewenang. Koordinator menguasai shared changes, integrasi serta keputusan selesai. Tidak ada dua writer pada file/save/port/output yang sama; bila isolasi tidak tersedia, worker read-only atau bergiliran. Detail operasional ada di bagian 7 prompt master.

## Kontrak waktu final

Foreground 1×: 1 hari game = 5 menit nyata; 2×/5× berlaku saat aktif.

Tidak aktif/background/ditutup: 1 hari game = 1 jam nyata, tetap termasuk biaya dan risiko. Laju ini tidak mewarisi speed foreground. Contoh 8 jam tidak aktif = 8 hari game, **bukan 96 hari**. Tidak boleh auto-pause background atau memotong durasi catch-up diam-diam.

Definisi lifecycle, fractional remainder, migration anchor, idempotency, crash/retry dan Skip tersedia lengkap di prompt/protokol. Saat persiapan paket, source `MainActivity.onPause()` masih menjeda clock: ini gap implementasi yang wajib diperbaiki Hermes.

## Paket ZIP bahan

Jika tersedia `Artifacts/Hermes-Handoff-2026-10-20.zip`, isinya adalah bahan yang tercantum MATERIALS.json ditambah manifest tersebut. Struktur relatif dipertahankan. Tidak menyertakan seluruh source checkout, APK referensi, cache alat, keys atau save pribadi. Untuk menjalankan implementasi, Hermes tetap harus mempunyai akses checkout produksi/source yang benar. Jangan mengekstrak ZIP di atas perubahan baru tanpa memeriksa konflik.

## Batas selesai

Penyerahan `COMPLETE_FOR_APK_HANDOFF` membutuhkan implementasi utuh dan bukti sesuai prompt, bukan keberhasilan validator bahan. Instalasi/Android native/perangkat menyusul dan tetap NOT_TESTED bila belum dijalankan. Produk tidak dipush/dipublish dan tidak ada launcher/agent yang dijalankan hanya karena paket ini dibuat.

Setiap fase harus memenuhi seluruh standar pada `PHASE-QUALITY-STANDARDS.md` dan menutup semua bug terbuka yang diketahui termasuk minor. Jika belum, Hermes memperbaiki/menguji ulang dan tidak menganggap dependency gagal sudah lulus. P95 aksi lokal ≤300 ms, navigasi ≤1 detik, feedback pekerjaan berat ≤300 ms dengan UI responsif. Review agen berbeda dilakukan setiap fase bila tersedia dan koordinator memverifikasi integrasi.
