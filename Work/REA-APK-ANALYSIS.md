# Analisis APK referensi dengan REA

Tanggal: 20 Oktober 2026, Asia/Jakarta. Root produksi: `GAME SAHAM`; repo referensi `Game Trading Saham` tidak diubah. Status pekerjaan: akuisisi, analisis statis yang tersedia, pemetaan fitur dan laporan selesai; bukan pemulihan lengkap source/logika lama atau acceptance game baru.

## Goal akhir dan bukti pemenuhannya

Mandat pemilik: langsung jalankan pemeriksaan aplikasi lama menggunakan REA, tetapkan goal, beri tahu ketika ADB tidak diperlukan. Goal kerja yang disampaikan: salinan APK terverifikasi di laptop, struktur dan fitur yang bisa dipulihkan dianalisis dengan REA, lalu dipetakan ke game kita dengan bukti dan batas yang jelas.

| Persyaratan | Bukti aktual | Hasil |
|---|---|---|
| Identifikasi aplikasi dan paket terpasang | ADB `pm path`, dump package; REA/JADX mengonfirmasi identitas dari manifest APK | PASS: `com.lydia.tycoongame`, versi 1.4.6, versionCode 33 |
| Salin seluruh paket | `Artifacts/REA/acquisition-2026-10-20/acquisition.json` | PASS: 5/5 APK; SHA-256 lokal sama dengan SHA-256 dari HP; daftar paket stabil setelah penyalinan |
| Pemeriksaan integritas lanjutan | apksigner 36.1.0, 5 log signature; pemeriksaan CRC ZIP | PASS: signature valid dan signer sama; bukan verifikasi identitas penerbit eksternal |
| Jalankan REA yang benar | CLI `rea-agents@6.7.0`, keluaran lengkap 7 operasi dengan Evidence ID | PASS: bukan hanya membaca skill atau hasil ADB yang diberi nama REA |
| Analisis Android dan Flutter | REA inventory, Flutter build/AOT, JADX package/class/method | PASS untuk lingkup metadata/kode yang dilaporkan; cakupan AOT string pool PARTIAL |
| Pemetaan ke game baru | `Work/REA-FEATURE-MAP.csv`, 26 kelompok; anchor URI/literal serta source baru diperiksa | PASS sebagai peta struktur dan gap; bukan bukti seluruh rumus/fitur berjalan |
| Beri tahu ADB tidak diperlukan | Pesan chat segera setelah kelima paket terverifikasi | DONE: HP boleh dicabut untuk analisis statis berikutnya |
| Pemeriksaan ulang tanpa HP | `python Work/verify-rea-evidence.py` → `Artifacts/REA/verification.json` | PASS: 5 APK, 7 record REA, 26 kelompok fitur; tanpa ADB/network |

## Akuisisi dan alat

Semua bahan analisis tetap lokal dalam `Artifacts/REA/` (ignored Git). Total paket **38.819.725 byte**:

| Berkas | Byte | SHA-256 |
|---|---:|---|
| base.apk | 14.651.337 | `11f019fcaf3c1eb23c48745640fca17b55138e9c46fbd76aa462c01b24671e69` |
| split_config.arm64_v8a.apk | 22.459.022 | `54979833350fcd22e8587a045f709af1c3528ff6ef0681679d66f404018c549e` |
| split_config.in.apk | 29.081 | Lihat acquisition.json |
| split_config.ms.apk | 29.081 | Lihat acquisition.json |
| split_config.xxhdpi.apk | 1.651.204 | Lihat acquisition.json |

Akuisisi memakai Android SDK ADB langsung saat unduhan REA masih berlangsung. Manifest akuisisi secara eksplisit menyebutnya bukti ADB, bukan REA Evidence. Tidak mengambil database/progres/akun privat; tidak menghapus atau memasang ulang aplikasi. Tidak mengakses backend, mengirim transaksi, klaim, iklan atau reset. Sesudah akuisisi, seluruh operasi analisis memakai salinan laptop.

REA dijalankan melalui CLI versi pin, dengan cache npm dalam proyek. Tidak mendaftarkan MCP, mengubah profil/model/provider, atau menulis konfigurasi global. Node v24.16.0; JDK existing Android Studio 21.0.10; JADX headless 0.7.1 diunduh lokal dan cocok dengan digest pin resmi `6e5eacf500b64292bfb73c49797c1958f6ee44646e43e868039ae7feb573ff75`. Download alat tidak memasukkan dependency ke APK produksi.

## Temuan yang benar-benar teramati

1. **Flutter ARM64.** REA mendeteksi `libapp.so` dan `libflutter.so` pada split ARM64. Dart snapshot hash `ace654289f5abc240509fc941453ebc5` konsisten dari dua sumber. Tidak memetakan hash ini ke versi Dart SDK; label versi Dart tidak tersedia.
2. **Android shell tipis dalam namespace aplikasi.** Pencarian `com.lydia.tycoongame` pada 6.310 kelas DEX menemukan MainActivity dan R. Itu lingkup pencarian namespace, bukan bukti tidak adanya kode terkait di namespace obfuscated. MainActivity mempunyai empat metode parsed; metode `configureFlutterEngine` berhasil didekompilasi dan memperlihatkan registrasi native ad factories serta MethodChannel. Itu bukan kode matching saham atau rumus keuangan.
3. **Struktur Dart terpisah menurut fitur.** REA AOT memuat kandidat URI `package:finans_oyun/...` untuk home, market, portfolio, empire, tasks, achievements, prestige, archetype, leaderboard, news dan profile. Ada model company, loan, IPO, portfolio, transaction dan OHLCV. Sebanyak 85 URI namespace aplikasi ditemukan pada proyeksi dan pemindaian tambahan; URI adalah string yang tertanam, bukan 85 file source yang berhasil dipulihkan.
4. **Perusahaan lebih luas dari firma investasi kita.** Nama modul meliputi konfigurasi/data perusahaan, pegawai, happiness, investment dan transfer sheet. Literal tertanam memuat `dept_id, happiness, work_hours, benefits_level, salary_bonus_level, employee_level`. Ini bukti keberadaan nama/label/field di paket; formula produktivitas, biaya dan aturan akses belum dipulihkan.
5. **Ada petunjuk integrasi backend.** Pemindaian byte tambahan menemukan URI Supabase/PostgREST/GoTrue/realtime dan literal seperti `take_loan`, `repay_loan`, `execute_ipo_purchase`, `claim_task_reward`, `transfer_to_company`, `upgrade_employee_level` dan `prestige_reset`. Kombinasi ini mendukung inferensi adanya arsitektur berbasis layanan/backend. Literal belum ditelusuri ke callsite atau diamati sebagai request; keberadaan library tidak membuktikan setiap fitur memakai server. Tidak ada endpoint yang dihubungi.
6. **Gambar tersedia sebagai aset Flutter; source Dart tidak ditemukan sebagai file aset biasa.** FontManifest hanya mendaftarkan font ikon MaterialIcons. Ini tidak menentukan keluarga font teks aktual atau mengembalikan ukuran/radius/warna asli. Ilustrasi referensi tidak dimasukkan ke source atau aset produksi.

## Peta fitur dan implikasi produk

Detail seluruh 26 kelompok ada pada [CSV](REA-FEATURE-MAP.csv). Kolom `old_logic_status=NOT_RECOVERED` sengaja dibedakan dari bukti struktur/tampilan. Group adalah kategori analisis, bukan jumlah screen yang sudah dibuka atau jumlah fitur yang lulus uji.

| Kelompok | Bukti pada aplikasi lama | Hubungan dengan game baru |
|---|---|---|
| Wealth, pasar, grafik, order, portofolio | Screenshot + URI model/screen | Domain subset sudah ada; fokus adaptasi hierarki, daftar dan chart |
| Risiko/kesehatan | Screenshot indikator kesehatan | Risiko domain existing berbeda; skor referensi tidak disalin tanpa formula |
| Pinjaman, IPO, reksa dana | Model/literal dan kategori pada screenshot | IPO/margin/Fund Builder existing hanya sebagian sepadan; katalog pinjaman atau unit reksa dana tidak otomatis tersedia |
| Misi dan achievement | Halaman screenshot + URI/literal | Tiga misi dan enam definisi badge existing; achievement baru tetap XP/badge tanpa uang; report badge masih mempunyai gap produksi |
| Rank, liga, rival, leaderboard | Widget URI + sebagian screenshot | Jabatan karier bukan liga mingguan; NPC pasar bukan otomatis pesaing personal |
| Perusahaan, CEO/pegawai, happiness, investasi kas perusahaan | URI dan string statis; dashboard belum dijelajahi runtime | Firma pengelola dana existing berbeda dari simulasi perusahaan operasional |
| Koleksi, prestige, archetype | Screen URI/literal; sebagian banner koleksi | Belum ditemukan padanan dalam source yang diperiksa; fitur baru memerlukan aturan tersendiri |
| Laporan harian, berita, profil | URI; sebagian profil di screenshot | Reuse DailySessionReport, ResearchService dan save lokal; jangan mengimpor akun/backend referensi |
| Deposito bank | Model bank dan literal `Bank deposits (removed)` | Jangan mengklaim fitur deposito masih aktif tanpa runtime; nama/teks bisa merupakan sisa implementasi lama |

Tidak menambahkan seluruh fitur referensi sebagai requirement baru secara otomatis. Persyaratan game kita tetap mengikuti PRD/keputusan pemilik, termasuk simulasi global berlabel, wealth bersih, karier, 24 jam game = 5 menit nyata, serta Java sebagai pemilik ekonomi.

## Apa yang belum dipulihkan

- Source Dart asli, typed class/function graph lengkap, dan implementasi fungsi Dart finansial. JADX menangani DEX Android, bukan mengganti Dart AOT menjadi source aslinya.
- Rumus harga, bunga, penalti keterlambatan, reward, reset harian/mingguan, perhitungan liga/archetype, pendapatan perusahaan dan payroll.
- Fungsi backend, database schema lengkap, task scheduler, atau kode server. File APK berisi klien; hasil ini tidak membuktikan akses ke implementasi server.
- Seluruh fitur runtime, terutama IPO, pinjaman, reksa dana, perusahaan/pegawai, koleksi dan prestige. Screenshot lama tetap 14 tangkapan, bukan 14 fitur atau seluruh journey teruji.
- Validasi source/APK baru di Android: kegiatan ini tidak memasang APK baru dan tidak menutup gate lifecycle/save/performa game baru.

REA menyatakan string pool PARTIAL: 500 URI dari setidaknya 926, serta 2.000 token berbentuk identifier dari setidaknya 4.000. Pemindaian regex tambahan menemukan 925 kandidat URI pada pola yang berbeda; bukan koreksi terhadap jumlah REA atau bukti pemulihan lengkap. Token, route candidate dan label bisa berupa sisa kode, terjemahan atau incidental match. Analisis tidak dieksekusi terhadap aplikasi lama.

## Bukti REA yang tersimpan

Semua JSON di `Artifacts/REA/evidence/`, envelope lengkap dengan `data.evidence_id`, SHA target dan limitations:

| Berkas | Evidence ID | Penggunaan |
|---|---|---|
| flutter-build.json | `ev_bb406e8a5f913efe349de5c92f930a5f9932cfb9585a214e8cd5a1611475576e` | Identitas build Flutter |
| dart-aot.json | `ev_e8eba07a2dfa36fe944131a0132dd8b0aaf0b881d7ea3e83edbbdd092026c344` | Snapshot dan proyeksi string PARTIAL |
| base-artifact.json | `ev_01ae50723def0ed7ef92d0ae5b8db0b8a3b697d6479c22ca4617c7b1455a7ece` | Inventory paket; termasuk evidence inventory nested |
| android-package.json | `ev_7426eae145911c5660f28334808aff5a1c4ff4025408218cc2b11072e2ce4fde` | Manifest, versi, 6.310 kelas DEX dan 560 resource |
| android-app-classes.json | `ev_6a8aa96af8ac6083c8b669a4303023564edbd3bd71044cc93f2d9d4b14ae6677` | Pencarian namespace aplikasi |
| android-main-activity.json | `ev_a580ae5d4c1fb2f5f1413401585c84b85fc447912bdd406700278643e6aa529a` | Parsed member inventory |
| android-engine-method.json | `ev_5997fed1dbb4b7955f64446fb0a7fdbd7d893feee26d06435c3fc93535b30312` | Dekompilasi konfigurasi Flutter/ads/MethodChannel |

Pemindaian ZIP/regex tambahan di `Artifacts/REA/analysis/` adalah hasil Python standard library, tidak diberi klaim sebagai REA Evidence. Beberapa file mengandung istilah internal dari paket; jangan commit/publish binary, seluruh dump, identifier integrasi atau ilustrasi referensi.

## Tahap berikutnya

Analisis statis dapat diteruskan dari salinan lokal tanpa ADB. Jika diperlukan pemetaan visual lebih lengkap, hubungkan HP untuk pemeriksaan terarah: perusahaan/pegawai, produk pinjaman/IPO/reksa dana, koleksi, prestige dan archetype. Membuka halaman aman tidak berarti menyetujui reset/transaksi/claim.

Implementasi UI berikutnya: shared shell portrait dan nav tetap → Beranda/Aset/Pasar → chart/order → karier/misi/achievement → Firma/Riset. Hilangkan kode FR/S/OQ dari teks pemain; gunakan bridge/domain existing. Penerimaan implementasi UI memerlukan pemeriksaan tersendiri; laporan ini belum mengubah source produksi. Tidak perlu pindah ke Flutter untuk mengadaptasi desain referensi.

Pemeriksaan yang dijalankan: SHA lokal/HP, signature kelima APK, ZIP CRC, tujuh hasil REA sukses terikat hash, anchor 26 kelompok cocok, source target ada, dan `git diff --check`. Tidak menjalankan ulang suite game karena source game tidak berubah. Produk tetap PARTIAL dan 0/14 paket acceptance penuh; laporan ini hanya menutup tugas akuisisi/analisis/pemetaan.
