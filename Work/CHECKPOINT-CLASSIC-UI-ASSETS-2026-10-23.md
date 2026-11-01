# Checkpoint: UI klasik dengan isi Aset terbaru

Keputusan pemilik 23 Oktober 2026: kembali ke tampilan lama, mempertahankan isi Aset yang disukai. Branch lokal `codex/classic-ui-new-assets`; fallback redesain tetap di `codex/ui-redesign-antislop` pada `1c821af`, desain klasik awal pada `fc3cdd2`. Tidak push, reset save atau membaca source checkout lama.

## Perubahan yang sudah diterapkan

- HTML/CSS memakai shell klasik: navy, ungu, hijau dan emas; Beranda/Pasar/Portofolio/Bisnis/Lainnya.
- Portofolio mempertahankan fungsi `loadAsetView` dari `1c821af`, identik setelah normalisasi CRLF/LF: nilai bersih, komposisi, enam nilai dompet IDR/USD, posisi, FX, koleksi dan 12 alat aset.
- Controller terbaru tetap digunakan. Java, ekonomi, waktu 5 menit dan perbaikan save sama dengan `1c821af`; pemeriksaan Git tidak menemukan perubahan Java.
- Lainnya kembali memuat profil, misi, liga, galeri, riwayat, terminal riset dan era baru. Root Lainnya tetap dipilih saat native Back tidak memiliki lapisan untuk ditutup.
- Galeri merender ke wadah anak sehingga pergantian filter tidak menghapus judul dan tombol Kembali.
- Koreksi aksesibilitas: target sentuh 48 px, fokus jelas, indeks bisa digeser dengan label/fokus, reflow teks 200%, tautan terbaca dan warna kerugian diperjelas. Label loading tidak menampilkan angka contoh.
- Tes navigasi disesuaikan dengan lima tab yang dipilih pemilik. Pemeriksaan saldo, fitur, persistensi dan ambang performa tetap; rasio ukuran khas desain empat tab diganti pemeriksaan batas, tumpang tindih dan target sentuh aktual.

## Peta fitur yang dipertahankan

| Tujuan | Isi dan jalur |
|---|---|
| Beranda | Kekayaan, hibah, karier, jadwal sesi, laporan, pencapaian, misi, liga, berita, pantauan dan panduan |
| Pasar | Indeks, 100 emiten, pencarian/filter/pantauan, detail/grafik/fundamental, order, IPO, reksa dana dan multi-aset |
| Portofolio | Seluruh isi Aset terbaru: dompet, komposisi, posisi, FX, kekayaan gabungan, koleksi, multi-aset, reksa dana, pinjaman, margin/short, aksi korporasi dan riwayat |
| Bisnis | Pendirian/operasi perusahaan, staf/fasilitas/kebijakan/modal/laporan, koleksi, dana kelolaan, klien dan mandat |
| Lainnya | Profil dan preferensi, misi, liga/rival, galeri, riwayat, era baru, riset, analisis, lab strategi, stress test dan replay |
| Logo/header | Pengaturan/simpan, akun aktif dan akses Portofolio dari kas |

## Bukti checkpoint

Semua hasil berada di `Artifacts/Classic-UI-2026-10-23`, kecuali kontrak awal reviewer di `Artifacts/Classic-UI-Assets-2026-10-23`.

| Pemeriksaan | Command / bukti | Hasil |
|---|---|---|
| Logika Java | `Work/run-codex-jvm-check.ps1 -OutputDirectory Artifacts/Classic-UI-2026-10-23/jvm` | 491/491 PASS; `jvm/junit-latest.log` |
| Browser | `python Work/run-redesign-browser-regressions.py --assets android/app/src/main/assets --output <folder bukti> --classpath Artifacts/Redesign-Antislop-2026-10-23/release-classpath.txt --language js\|python` | 35/35 suite memiliki hasil final PASS (19 JS, 16 Python); `browser-checkpoint-results.json`; kegagalan awal tetap disimpan untuk audit |
| Kontrak klasik/Aset | `Work/test-classic-ui-assets.py` melalui runner terisolasi | PASS 56 pemeriksaan, termasuk 40 sampel lima tab × empat viewport × dua skala; pembelian BBCA nyata, saldo/posisi, 12 alat, FX, filter galeri dan Back |
| Layar | `python Work/verify-redesign-screens.py --url http://127.0.0.1:18918 --output <screens-final-a atau b> --label after --only <setengah inventaris>` | 84/84 route × delapan viewport × teks 100%/200% = 1.344/1.344 PASS, 3.005 screenshot; `checkpoint-verification.json` |
| Kontras | `python Artifacts/Classic-UI-2026-10-23/contrast-all-routes.py` | PASS 504 sampel / 52.133 pengukuran, nol kegagalan ambang 4,5:1 normal / 3:1 besar; pendekatan konservatif titik warna gradien, bukan kalibrasi layar fisik |
| Performa | `python-nav-final/benchmark-performance-final/evidence/performance-benchmark.json` | 160 navigasi P95 112,95 ms; 128 aksi biasa P95 139,98 ms; 32 feedback berat P95 88,85 ms, dalam batas host uji |
| Build | `JAVA_HOME=<bundled JBR>; JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=<root>/.socket-qa; android/gradlew.bat --project-dir android --offline :app:assembleDebug --no-daemon --console=plain` | PASS; `build-checkpoint.log` |
| Paket | SHA dan seluruh `assets/www` dibandingkan source; `apksigner verify --verbose` | PASS, tanda tangan debug V2 valid; `package-verification.json`, `apk-signature.log` |
| Pemeriksaan source | `node --check android/app/src/main/assets/www/app.js`, `git diff --check` | PASS |

APK checkpoint: `Artifacts/Classic-UI-2026-10-23/GameSaham-classic-ui-new-assets.apk`, 977.927 byte, SHA-256 `0917ca0712355fe2cbc961ee7c778630681846f8f60053f04f8fbf8ca6d422b9`.

## Batas checkpoint dan tindak lanjut

Delivery Gate antislop dijalankan oleh koordinator untuk perubahan presentasi pada checkpoint ini. Bagian native/perangkat dan sertifikasi ulang seluruh fase tetap di luar klaim PASS berikut.

| Blok | Hasil dan bukti |
|---|---|
| Hard Gate | PASS pada scope browser yang diuji: tidak ada em dash pada source UI; data dari bridge Java nyata; lima tujuan navigasi ada; tidak membuat aset/dependency baru; pengujian route dan reflow serta 504 sampel kontras; state kosong/loading/error/offline dan fokus/keyboard diperiksa oleh suite state/journey; build dan rekaman klik alat/aset/riset/korporasi tersedia. |
| Purpose-Gate | PASS dengan keputusan pemilik sebagai arah: gradien/kaca/glow mempertahankan identitas klasik; emas untuk aksi, hijau/merah untuk nilai, ungu untuk bagian aktif. Ikon kategori dan panah tujuan, kapital untuk kelompok data, Plex untuk keterbacaan, motion singkat untuk respons. Alasan aktif tercatat di DESIGN.md, bukan arah baru dari filter. |
| Liveliness | PASS: ENERGY 3 / RHYTHM 2 / MOTION 2 telah dinyatakan; nilai kekayaan/grafik/tujuan halaman menjadi fokus, ruang memisahkan kelompok data, aksen aksi emas dan motif kartu klasik konsisten. Screenshot aktual Portofolio 390×844/100% dan Beranda 360×800/200% dibuka untuk review visual koordinator. |
| Craftsmanship & Quality Locks | PASS pada perubahan yang diuji: isi Aset dipakai ulang, tanpa data dekoratif baru; bentuk kontrol/kartu klasik konsisten, CTA menyebut tindakan, seluruh 35 suite browser memiliki hasil PASS final. Galeri kehilangan Back diperbaiki pada wadah bersama; ukuran target, fokus, teks 200%, loading, error, offline, persistensi dan hubungan saldo diverifikasi dengan bukti runner. |

Laporan mesin `checkpoint-verification.json` menyatukan angka final; rekaman awal yang gagal tidak dihapus. Pengujian screenshot memakai pembuka route khusus QA, sehingga bukan pengganti perjalanan klik. Pengujian klik membuktikan jalur yang direkam, bukan setiap kombinasi kemungkinan transaksi atau state karier.

Ini checkpoint pemulihan UI, bukan sertifikasi ulang semua 14 fase produk atau hasil uji Android fisik. Tidak ada instalasi/ADB baru. Pengujian browser memakai state Java nyata pada save QA terisolasi, bukan save pemain. Screenshot membuktikan route yang dirender; suite perjalanan/klik memberi bukti interaksi tambahan, tanpa klaim semua kemungkinan status ekonomi sudah dicoba.

Percobaan build pertama gagal membuat koneksi loopback, lalu lulus dengan direktori socket lokal yang sudah dipakai proyek. Percobaan storage pertama kehabisan memori native JVM pada host uji; rerun terisolasi lulus seluruh empat pemeriksaan, termasuk tujuh settlement harian, waktu 5 menit, save/load dan suite Extreme 10×100/Dual-Rate. Bukti ada di `storage-rerun`; kegagalan lingkungan awal tetap direkam.

Preview produksi lokal berada di `http://127.0.0.1:18917/` dan sudah menampilkan shell klasik pada inspeksi visual. Akses browser lokal kemudian dinonaktifkan oleh Work settings sehingga reload akhir tidak bisa dilakukan melalui alat UI; source dan screenshot akhir diperiksa melalui runner yang sudah ada. Pengaturan Work tidak diubah.

Next action sesudah checkpoint: tinjauan pemilik atas UI klasik/isi Portofolio dan, bila diminta, uji bermain APK melalui cara instalasi pilihan pemilik. Tidak ada perubahan visual tambahan yang direncanakan pada checkpoint ini.
