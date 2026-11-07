# Eksekusi stabilitas buka ulang Game Saham

Status penyerahan setelah pembuktian host lanjutan. SU01–SU04 memiliki implementasi dan bukti host/browser sesuai rincian berikut. SU05 memiliki bukti host dan APK, dengan gerbang perangkat **NEEDS_VALIDATION**. Pemilik menyatakan Poco belum tersedia dan memilih melanjutkan bukti host. Gerbang wajib yang masih terbuka memerlukan Poco. Goal **BLOCKED** menunggu ketersediaan perangkat, setelah hambatan yang sama bertahan dalam tiga giliran goal berturut-turut.

Checkout `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`, branch `codex/classic-ui-new-assets`, HEAD `9653f7a992405b4dfaf28acddbf1caf158d03957`. Working tree memuat perubahan lokal sebelumnya. Bukti kini di `Artifacts/Stabilitas-Buka-Ulang-2026-11-05/pnl-precision-current/su-evidence.json` menautkan 244 input source/test serta 346 hash kelas, katalog, dan runner. Path bukti di bawah relatif terhadap `Artifacts/Stabilitas-Buka-Ulang-2026-11-05`.

## SU01 dan SU02a

Baseline `baseline/cold-resume-20261105-222651-586` mereproduksi domain tujuh hari selesai dalam 9,58 detik, tetapi worker bridge berakhir FAILED dengan pesan Java heap space pada heap 384 MiB setelah primary 58.549.235 byte terbentuk. Sampel JFR memperlihatkan worker membaca ulang primary saat save kedua. Domain 30 hari juga kehabisan heap saat capture buku pasar. Diagnosis kejadian penutupan aplikasi pada Poco masih memerlukan bukti Android.

Source sekarang memakai JsonReader dan JsonWriter bawaan Android untuk berkas save. Ledger diparse per baris, checksum dihitung dari aliran byte, dan penulisan mengalir langsung ke temp. Validator tetap memeriksa graph domain. Data hasil parse merupakan milik pemuat, sehingga validator dapat memakai objek itu langsung. Validasi snapshot dari state hidup tetap menerima salinan terpisah. Parser berbagi maksimal 4.096 string label selama parse untuk mengurangi duplikasi akun/deskripsi. Host mengekstrak delapan kelas pembaca/penulis dari layoutlib Android Studio yang sudah terpasang. Hash kelas dan helper tersimpan. Build Android memakai API platform.

Profil fase terbaru menggunakan salinan kelas QA bertanda waktu di `pnl-precision-current/phase-profile`. Dalam satu run tujuh hari dengan fixture gabungan, load/restore 420 ms, parse 105 ms, validasi load 158 ms, catch-up 5.146 ms, capture 94 ms, validasi save 304 ms, serialize/tulis temp/sync 700 ms, backup/commit 10 ms, dan snapshot 20 ms. Parse dan validasi merupakan bagian dari load. Serialize, tulis dan sync dilaporkan bersama karena penulisan kini bertahap. Log dan input hasil instrumentasi tersedia terpisah dari run penerimaan.

## SU02b dan kompatibilitas save

Pemilik menyetujui ringkasan detail internal yang tidak tampil, dengan riwayat yang tampil dan receipt ekonomi dipertahankan. Ringkasan mingguan hanya memilih entri matching internal dari akun NPC yang dikenali. Sepuluh entri terakhir tiap NPC, transaksi besar yang tampil di Terminal, seluruh entri personal/klien/perusahaan, serta receipt dari jalur ekonomi lain tetap tersedia. Ringkasan menyimpan total mutasi kas bertanda per akun/mata uang dan hitungan logis. Caller yang memakai jumlah entri untuk ID memakai hitungan logis. Indeks awal hari karier disesuaikan pada batas ringkasan atau saat migrasi.

Save yang diringkas menggunakan versi 3. Pemilik menyetujui bahwa format ini cukup dibuka dengan APK baru. Reader tetap menerima versi 2. Pada load legacy, graph asli divalidasi sebelum diringkas di memori. Saat save migrasi, kandidat temp dibaca ulang, divalidasi, diserialisasi ulang ke checksum, dan dibandingkan sebelum backup/replace.

`pnl-precision-current/legacy-30-verified` membuktikan transisi fixture QA versi 2 berukuran 237.065.936 byte pada heap 384 MiB. Bridge memuat, memigrasikan dan menyimpan hasil 90.402.692 byte dalam 11,08 detik. Reload 2,72 detik dan buka ulang kedua 5,18 detik. Hash backup setelah commit pertama sama dengan hash berkas lama `340369CBBAEB13A9F1D0388FC9ABD2006AE034B769BDA62CF4E74B7C4A607308`. Berkas sumber QA tetap tersedia. Assertion terpisah memeriksa migrasi di memori mempertahankan byte primary sebelum save serta batas ledger laporan karier.

Pembandingan fixture yang sama sebelum/sesudah ringkasan di `verification-load/compare-visible-economy-matched.json` memeriksa 225.245 transaksi besar, sepuluh riwayat terakhir per akun, fill, kas, dan posisi. Nilai dan urutannya cocok setelah ID berbasis waktu antar-run dikecualikan. Detail NPC yang tetap tampil terus bertumbuh. Implementasi ini belum membuktikan penggunaan heap terbatas untuk durasi tanpa batas.

## Presisi PnL setelah beberapa restart

Pembuktian lanjutan memperlihatkan pemeriksaan kas dan ledger personal sebelumnya belum menangkap perubahan PnL tiap posisi NPC. Setelah empat restart pada rentang 30 hari, 57 posisi USD menunjukkan selisih pembulatan. Penyimpanan dan pembentukan salinan posisi memakai getter Money yang membulatkan nilai PnL akumulatif. Tes merah di `pnl-precision-red/junit-latest.log` mereproduksi USD0,03 berubah menjadi USD0,00 setelah sepuluh save/reload.

Position sekarang mengekspos nilai BigDecimal akumulatif untuk save dan salinan domain. SaveState, StrategyLabSandbox dan kedua jalur split memakai nilai presisi tersebut. Getter Money untuk penyajian tetap menjalankan pembulatan mata uang. Reader save lama mempertahankan nilai yang sudah tersimpan. Pecahan yang hilang sebelum perbaikan tidak tersedia untuk direkonstruksi.

`pnl-precision-current/repeated-checkpoint-30-20261105-235347-311` merekam empat penghentian JVM berurutan dengan exit 71, lalu proses kelima selesai. Pembanding penuh lulus untuk 216.619 transaksi besar, riwayat terbaru, harga/quantity/timestamp dan biaya fill, kas, reserved cash, kuantitas posisi, average cost, acquisition fees, current price serta realized PnL seluruh akun. Semua checksum ekonomi cocok dengan run tanpa gangguan setelah ID berbasis waktu dikecualikan. Runner `Work/Stabilitas-Buka-Ulang-2026-11-05/run-checkpoint-restarts.ps1` dapat mengulang skenario dari direktori run referensi.

86 tes terpilih lulus, termasuk regresi PnL positif dan negatif. Sebanyak 16 tes tambahan untuk lab, aksi korporasi, dan persistensi supply juga lulus. Bukti source sebelum perbaikan PnL merupakan riwayat. Manifest dan APK penyerahan memakai snapshot setelah perbaikan.

## SU03 dan SU05 host

Worker existing menangkap target waktu per siklus. Interval panjang membuat checkpoint setelah mencapai batas tengah malam WIB, dengan anggaran tujuh hari nyata per batch. Anchor dan remainder ikut commit. Waktu yang berlalu setelah target tetap terutang pada siklus berikutnya. Worker yang gagal memuat state committed saat retry. Perhitungan kewajiban real-time memakai waktu batas kejadian terkait.

Fixture gabungan sintetis memakai transaksi saham terisi, order terbuka, trigger, dana tertahan, pemesanan dan distribusi IPO katalog, reksa dana, mandat klien, perusahaan, utang margin, posisi short, dan langganan Pro. Modal dan peringkat QA dinyatakan sintetis. Pengujian tidak memakai save pemain.

| Offline nyata | Bridge, detik | Reload, detik | Buka ulang kedua, detik |
| --- | --- | --- | --- |
| 7 hari, tiga proses | 4.56 / 4.54 / 5.87 | 0.95 / 0.96 / 1.55 | 1.84 / 1.91 / 2.65 |
| 30 hari, tiga proses | 36.99 / 35.57 / 31.63 | 3.92 / 2.56 / 3.28 | 8.24 / 4.58 / 4.77 |

Tiga run memakai katalog, seed, input dan jam yang diikat ke manifest. Setiap proses memakai `-Xmx384m`. Assertion memeriksa target 48× dan snapshot, rekonsiliasi, checksum ledger berurutan untuk round-trip, serta checksum mutasi ekonomi personal/klien/perusahaan yang mengecualikan ID nondeterministik untuk pembandingan antar-run. Pada 30 hari, masing-masing mencatat 1.440 pembayaran gaji dan 48 perpanjangan Pro. Distribusi IPO 200 saham, 100 saham BBCA, unit reksa dana 9,990000, operasi perusahaan, serta margin dan short tersimpan. Primary awal hasil catch-up berukuran 91.656.023 byte dan ledger memiliki 786.106 entri logis.

Run awal berlangsung bersamaan dengan pengujian/build lain, sehingga angka durasi di atas menunjukkan variasi host. Okupansi maksimum yang terekam saat GC untuk tujuh hari 180 MiB, dan untuk 30 hari 383 MiB. Angka tersebut memakai heap JVM. Margin memori 30 hari masih tipis dan penerimaan performa Poco menunggu pengukuran perangkat.

`pnl-precision-current/junit-latest.log` mencatat 86 tes terpilih lulus. Uji mencakup remainder 624 ms game, jam bergerak selama catch-up, commit gagal lalu retry, jam mundur, superseded writer, primary/backup rusak, serta aturan pasar 24 jam dan batas margin. `process-death-matrix/result.json` mencatat tujuh penghentian JVM dengan exit 72, lalu pemulihan dalam proses baru yang cocok dengan referensi ekonomi. Titiknya sebelum settlement, setelah mutasi sebelum commit, saat temp ditulis, setelah temp sync, setelah rotasi backup, setelah replace, dan sebelum acknowledgement. Uji delapan hari juga menghentikan proses pada commit kedua dan melanjutkan dari checkpoint pertama.

`disk-full-injection/result.json` membuktikan simulasi IOException saat tulis temp mempertahankan hash primary, kemudian proses baru pulih ke referensi. `commit-edge-cases` membuktikan fallback move non-atomic untuk backup dan primary, serta penolakan writer yang sudah digantikan pada savedTimestamp yang sama. Injeksi memakai salinan source QA dengan hash dan command tersimpan. Bukti ini mengukur penghentian proses JVM dan fault buatan.

## SU04 browser

Tambahan pemeriksaan kontrak waktu nyata di `pnl-precision-current/real-deadline-tests.log` mencatat 21 tes lulus pada source yang sama, exit 0. Cakupannya deadline berita 60/120 menit dan akhir eksklusif, restore/jam mundur, publikasi selama offline, kesetaraan catch-up utuh dan terbagi, serta akrual bunga/penalti, receipt pembayaran, amortisasi, dan persistensi pinjaman. Suite pinjaman juga menjalankan rangkaian 10 seed × 100 aksi. Bukti ini melengkapi kontrak deadline SU03.

`pnl-precision-current/browser-resume-1793897543675614100/result.json` menggunakan Chromium dengan domain Java dan save QA baru. Setelah simulasi offline tujuh hari, pending terlihat dalam 27,9 ms, navigasi selama pending merespons dalam 134,4 ms, dan sinkronisasi selesai dalam 5,50 detik. Aksi order saat pending ditolak. Fault tulis mempertahankan hash primary, retry lalu order sungguhan mengubah reservasi Rp0 menjadi Rp10.015, dan snapshot lebih lama ditolak. Nol page error tercatat.

Runner browser diperbaiki agar menunggu callback UI selesai membuka transaksi setelah worker menyelesaikan commit. Skenario legacy lama juga dikenali terpisah karena fixture tersebut hanya memiliki order terbuka. Fixture gabungan pertama ditolak oleh validator supply akibat IPO sintetis yang memakai ticker NCLD existing. Fixture diperbaiki untuk memakai IPO-CLOUD katalog. Assertion batas karier pada tes baru dikoreksi dari 2 menjadi 1 berdasarkan satu baris modal yang tersisa sebelum batas. Semua kegagalan percobaan tetap ada pada log. Runner regresi grafik lama masih berhenti pada `#live-candle-body` pada baseline dan kandidat. Gerbang plan performa lama belum diklaim lulus.

## SU05 APK dan perangkat

APK terkini berada di `pnl-apk-current` dan `pnl-apk-current-qa`. Build dan pemeriksaan tanda tangan keduanya exit 0. Paket QA `com.tradingglobal3d.mobile.qa.su05`. Pemeriksaan `pnl-precision-current/apk-parity.json` menemukan 491 anggota assets/DEX identik. Pemeriksa mencocokkan 469 aset web dengan source dan memeriksa 13 DEX per APK, termasuk penolakan kelas server QA/test. Manifest source build diperiksa ulang terhadap checkout.

- `Artifacts\Stabilitas-Buka-Ulang-2026-11-05\pnl-apk-current\app\outputs\apk\debug\app-debug.apk` memiliki SHA-256 `5B84D1176619D2326E9683D1C473BC058F5CFEC706F42B64B5F821E4226A64D6`.
- `Artifacts\Stabilitas-Buka-Ulang-2026-11-05\pnl-apk-current-qa\qa-device-build\outputs\apk\debug\app-debug.apk` memiliki SHA-256 `A0C2F9095976B90B4B0293446B7765E7E8DA2A5FD1EFC9B590639BEB02815A24`.

Save pemain hanya di-hash. SHA-256 tetap `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`.

Gerbang terbuka tetap **NEEDS_VALIDATION** untuk identitas APK terpasang dan Android/WebView, diagnosis exit/ANR/renderer, cold start dan lifecycle perangkat 7/30 hari, memori aplikasi/renderer serta respons input, ambang durasi/margin memori yang disepakati, dan sesi ketahanan 60 menit setelah catch-up. Pemilik menyatakan Poco belum tersedia dan memilih bukti host. Goal belum selesai.

`next_action` memakai hasil host terkini sebagai kandidat untuk gerbang Poco saat perangkat tersedia. Pemeriksaan tambahan harus membaca cakupan tes dan menautkan hasil ke hash APK yang benar. Seluruh perubahan lokal disertakan sebagai working tree untuk ditinjau.
