# Audit pemilik: UI portrait, logika game, dan kesiapan Android

Tanggal pemeriksaan: 2026-10-19T23:16:01.670776+07:00. Root aktif: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`. Baseline `0807e43ca7ac2cd827626446dab8c20b68caa3de`; branch `codex/owner-audit-portrait-v8`. Agen lain dinyatakan tidak bekerja oleh pemilik.

**Hasil: produk tetap PARTIAL. Seluruh logika UI V8 belum tersambung. Kandidat APK debug berhasil dibangun; belum diinstal atau diuji pada Android.** Tidak ada klaim bebas bug atau selesai seluruh PRD. Dari 14 paket penerimaan produk, 0 berstatus COMPLETE pada STATUS.json; seluruhnya tetap PARTIAL.

## Mandat dan keputusan pemilik

Audit sekaligus perbaiki bug pada source dan uji ulang. Target utama Poco X8 Pro 12/512; versi Android belum diketahui. Instalasi pertama, belum perlu membuktikan migrasi APK lama. Emulator yang tersedia boleh digunakan. Pemeriksaan lokal menemukan `emulator.exe` tidak ada, direktori system-images tidak ada, folder AVD kosong, dan `adb devices -l` tidak menampilkan perangkat. Gate native belum dapat dijalankan. Tidak menginstal emulator/image baru, tidak push, dan tidak mengubah repo lama.

Acuan produk: portrait, graphite/amber/teal, kekayaan pribadi terpisah dari AUM, XP/badge tanpa uang hadiah; pembukaan fitur mengikuti karier; indeks simulasi transparan. V8 adalah acuan UI terpisah di folder visualizations, bukan source ekonomi produksi. UI produksi masih turunan integrasi revisi 2.

## Perbaikan yang diterapkan

| Masalah | Perubahan / batas bukti |
|---|---|
| Close hari sebelum batas waktu masih memotong bunga | Validasi dilakukan sebelum mutasi financing/ledger. Direproduksi gagal sebelum perbaikan, lalu regresi lulus. |
| Clock kehilangan tanggal pada tepat 1 atau beberapa hari | Gunakan LocalDateTime dan perhitungan long; tanggal/waktu tidak terpotong oleh wrap LocalTime. |
| Rasio waktu UI tidak sesuai keputusan pemilik | Facade mengonversi 1 detik nyata menjadi 288 detik game; UI hanya mengukur elapsed aktif. 300 detik pada 1x, 150 pada 2x, 60 pada 5x = 24 jam game. |
| Skip hari belum tersambung | Tambah skip tepat 24 jam, review dan konfirmasi, request ID, receipt tersimpan, retry/reload tidak memajukan dua kali. Daily settlement/calendar/NPC/trigger memakai jalur advance yang sama. Resolusi pasar saat advance: langkah maksimum satu menit game. Kalender bursa global belum lengkap. |
| Preview mandat mengurangi trust | Validator dibuat murni; penalti hanya pada submit yang benar-benar ditolak, sekali per request. |
| Batas mandat mencampur IDR/USD dan mengabaikan biaya beli | Kas dan nilai posisi dikonversi ke mata uang dasar; estimasi biaya beli menggunakan tarif matching engine. Bukan bukti seluruh kebijakan mandat sudah lengkap. |
| XP dari ID artikel sembarang | Facade hanya menerima artikel terbit yang benar-benar tersedia; UI mengirim ID artikel. |
| Badge pembaca tidak bisa dicapai dari konten game | Tambah dua catatan edukasi simulasi terjadwal agar lima artikel benar-benar tersedia; uji badge memakai konten game, tanpa menyuntik artikel fixture. |
| Level pada ambang 1750 XP salah | Koreksi rumus ambang level 8 dan seterusnya. |
| Fill resting/trigger tidak selalu memberi progres | Evaluasi progres bersama memeriksa order personal yang benar-benar memiliki fill. |
| Badge risiko diperoleh dari hari tanpa mandat | Hanya sesi dengan mandat aktif dan batas kas/konsentrasi/drawdown terjaga yang dihitung; jalur close manual dan otomatis memakai evaluasi bersama. |
| New game mewarisi status lama | Reset karier, riset/langganan, FX receipt/quote, lab, katalog/watchlist dan progres. Modal awal invalid ditolak sebelum menghapus state live. |
| Riset dan subscription memakai waktu perangkat | Jadwal riset/kalender diikat pada epoch game dan disimpan; expiry dievaluasi pada waktu game termasuk tepat batasnya. Manfaat premium masih gap terpisah. |
| Progress save invalid berpotensi memengaruhi live state | Validasi progress pada manager detached; tolak XP negatif/tidak cocok, ID duplikat dan progress di luar target sebelum restore live. |
| Tutorial tidak membuka tiket | Perbaiki jumlah/jenis parameter getInstruments sesuai kontrak bridge. Uji klik membuka tiket produksi. |
| Laporan klien selalu menampilkan trust 70 | Baca field `trust` yang benar; hilangkan fallback angka yang mengarang nilai. UI diuji menampilkan 65 setelah penolakan order. |
| Mode historis bisa diberi label tanpa dataset | Bridge produksi menolak start historis yang belum didukung dengan pesan alasan; domain fixture replay tetap terpisah untuk pengujian. |
| Error clock dapat berulang | Clock dijeda saat advance melempar error; UI menampilkan pesan. Regresi memeriksa error range waktu, bukan simulasi kegagalan disk Android. |
| Batas native dan lifecycle | Baca aset sampai EOF, gagal katalog menghentikan startup dengan pesan, autosave setelah pause, peringatan gagal simpan, pembatasan navigasi WebView, insets API30+ untuk system bars/cutout/IME. Terkompilasi/lint; perilaku native belum diuji. |

Empat regresi pertama disimpan sebagai bukti FAIL sebelum perbaikan di `Artifacts/OwnerAudit-2026-10-19/regression-before-fix.xml`. Test regresi source: `android/app/src/test/java/com/tradingglobal3d/core/OwnerAuditRegressionTest.java`.

## Bukti terbaru

| Pemeriksaan | Hasil | Batas |
|---|---|---|
| JUnit host | 153 tes / 14 suite, 0 failure/error/skipped; 18 regresi audit baru | Subset perilaku domain, bukan seluruh requirement/native |
| Browser dengan Java QA host fresh | 13 pemeriksaan lulus; tidak ada pageerror | Java domain/bridge asli lewat HTTP lokal, bukan Android JavascriptInterface |
| Layout | 20 sampel: 4 ukuran portrait x 5 tab; tanpa overflow horizontal | Scroll vertikal masih ada: dokumen 10/20, main container 9/20. Bukan fixed-page V8 |
| Teks 200% | Isi/tindakan tetap dapat dijangkau dengan layout scroll saat ini | Bukan bukti seluruh layar/ukuran sistem Android |
| Lint | 0 error, 13 warning | Termasuk locale, icon aplikasi, orientasi dan pengingat WebView/dependency; bukan 0 warning |
| APK | Build debug, tanda tangan diverifikasi, 7 aset paket sama byte-for-byte dengan source | Tidak membuktikan instalasi, lifecycle atau performa HP |
| Emulator / perangkat | NOT_TESTED / BLOCKED lingkungan | Tidak ada emulator runnable/image atau perangkat ADB |

Perintah validasi: dari `android`, `gradlew.bat --no-daemon testDebugUnitTest lintDebug assembleDebug --console=plain`, memakai JBR Android Studio dan workaround loopback per proses. Tidak ada perubahan pengaturan global.

Bukti tersimpan di `Artifacts/OwnerAudit-2026-10-19/`: `verification.json`, XML seluruh suite, `gradle-final.log`, `browser-result.json`, `browser-check.py`, screenshot review/skip/teks, `apk-signature.txt`, `apk-badging.txt`, serta kandidat APK.

APK: `Artifacts/OwnerAudit-2026-10-19/game-saham-owner-audit-debug.apk`; ukuran 622,716 byte. SHA-256: `1f99218c636d8795cb11f4ce804516feaaf3a4a13fd141299487d102695d991a`. App ID tetap `com.tradingglobal3d.mobile`, minSdk26, targetSdk36, versionCode1. Ini kandidat QA dengan UI produksi saat ini, bukan APK final V8.

## Celah yang masih terbuka dan urutan selanjutnya

| Prioritas | Gap / bukti source | Tindakan berikut |
|---|---|---|
| P1 | Gate fitur karier belum ditegakkan utuh: acceptClientMandate/takeMarginLoan/openShortPosition masih dapat dipanggil tanpa pemeriksaan rank yang menyeluruh | Tetapkan kontrak akses dari karier yang existing, tegakkan di domain pada seluruh caller, lalu uji journey profil baru sampai klien. XP tidak boleh membuka fitur. |
| P1 | Sesi individual bursa/libur/global belum lengkap. Clock masih satu tabel sesi WIB; submitOrder dan trigger tidak memakai kebijakan per bursa yang lengkap | Definisikan queued/rejected/expired di luar sesi dan tegakkan pada shared execution path. Tambahkan regresi closed/weekend/US/trigger. |
| P1 | Manfaat paket premium belum benar-benar diperiksa di semua layanan. hasAccess saat ini tidak melindungi seluruh data/order-book/alternatif yang dijanjikan | Implementasikan manfaat dan entitlement pada layanan data; jangan meluluskan pembelian saja sebagai paket lengkap. |
| P1 | Cakupan UI V8 belum terintegrasi: inbox, data alternatif, perbandingan dan peta fitur belum memiliki jalur produksi. Fixed-page V8 belum dipasang | Port shell/interaksi presentasi V8 bertahap, pertahankan Java sebagai pemilik ekonomi, ganti semua fixture UI dengan data/empty/unavailable domain. |
| P1 | Laporan klien hanya snapshot mandat. onClientReportCompleted tidak punya caller, badge report belum dapat diraih | Buat laporan periode yang terlacak, benchmark/risiko/flow, status completion idempoten dan persistensi, lalu sambungkan badge. Jangan beri XP hanya karena membuka layar. |
| P1 | Pasar hidup belum memenuhi seluruh FR03: registrasi runtime baru market maker; berita/kalender masih seed, belum rezim/reaksi NPC dan causal attribution penuh | Sambungkan strategi NPC existing dan event terjadwal dengan sumber/seed/run identity serta riwayat harga nyata game. Tidak membuat candle dekoratif. |
| P1 | Dataset historis/provenance/hak belum tersedia | Tetapkan sumber/periode/model replay/hak; pertahankan UNAVAILABLE sampai lolos gate data. |
| P2 | Multi-aset, rights/reverse split/buyback, tender M&A, voting record-date, margin recall/liquidation/squeeze dan audit mandat semua jalur belum acceptance lengkap | Selesaikan per journey PRD dengan ledger/reservasi/save/retry, bukan menyimpulkan lengkap dari keberadaan kelas/tombol. |
| P1 sebelum final | Native Back/keyboard/insets/background/force-stop/storage-failure dan performa Poco belum diuji | Sediakan emulator runnable, lakukan fresh-install dan native journey; pengukuran di HP pemilik tetap bukti tersendiri. |

Pemetaan rinci 47 route referensi V8 ada di `Work/OWNER-UI-LOGIC-MATRIX.csv`: 42 PARTIAL, 4 MISSING, 1 BLOCKED_DATA. Ini hitungan route pemetaan, **bukan persentase selesai code/game**. Tidak ada route yang diberi COMPLETE dari pengujian browser saja.

Checkpoint selanjutnya: R04 integrasi UI V8 + kontrak akses karier/sesi, kemudian laporan klien/entitlement dan acceptance fitur lanjutan; native dan dataset tetap gate terpisah. Audit ini memperbaiki bug yang direproduksi/ditelusuri pada jalur yang diperiksa, bukan menyelesaikan semua gap fitur PRD secara diam-diam.
