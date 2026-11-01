# Hasil verifikasi remediasi R01–R12 dan A01

Tanggal: 2 November 2026. Root produksi: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`. Status: implementasi dan verifikasi host/build siap direview. Full regression akhir 726 tests PASS; bukan klaim siap rilis/perangkat.

## Perubahan dan bukti

- R01/R02: validasi detached domain pada pemilihan/rotasi/recovery save; hash byte primer yang sudah divalidasi; UTF-8 ketat; pengunci transaksi setelah kegagalan save tanpa membuang progres memori; status worker tetap terminal ketika logger native tidak tersedia pada JVM.
- R03/R04/R05/R12: quote MM memakai harga pasar existing; preview/MAX memperhitungkan likuiditas adaptif tanpa menerbitkan aset; preflight kemampuan bayar/kepemilikan/counterparty; penerbitan dan modal eksternal dengan ID persisten; inventory bebas setelah komitmen order; deduplikasi ledger tidak lagi hilang setelah 5.000 mutasi.
- R06: MARKET saat tutup persisten sebagai antrean pembukaan; tidak berubah menjadi LIMIT pada harga lama; reservasi/cancel dan pembukaan per bursa. Fast-forward melewati noise historis sesuai keputusan, tetap memproses kewajiban esensial dan order pembukaan.
- R07/R08: agregasi bertanda, referensi episode pertama tetap lintas hari/expiry/reload; zero-net menonaktifkan dorongan; artikel dan deadline real persisten; comparator/katalis berita negatif mengikuti arah penurunan.
- R09 disetujui pemilik: hanya jam/tanggal existing bergerak mengejar progres domain terkonfirmasi; tanpa panel tunggu baru. UI memakai cache baca selama sinkronisasi dan mengunci mutasi sampai selesai/save berhasil. `can...` diperiksa sebagai prefix camelCase agar `cancel...` tidak salah dianggap pembacaan.
- R10 disetujui pemilik: pesan gagal pembatalan perpanjangan memakai toast existing yang masih terhubung DOM. Pesan domain tetap tampil; fallback “Perpanjangan otomatis belum dihentikan. Silakan coba lagi.” Fokus kembali ke pemicu.
- R11/A01: runner mengompilasi source/test aktual dan mencatat hash; preflight menggunakan maker yang benar-benar dapat dipakai (termasuk melewati AON pemain/self); nilai settlement memakai BigDecimal untuk quantity yang tidak presisi dalam double.

Lokasi perbaikan dan acceptance yang belum dibuktikan seluruhnya tersedia pada `plan.md` dan `MATRIKS-CAKUPAN.md`. Lima temuan review tambahan berada di `BACKLOG-REVIEW.md`, belum menjadi perbaikan dalam mandat ini.

## Hasil run

Semua path bukti di tabel berikut berada di `Artifacts/Codex-Audit-Ulang-2026-11-01/`.

| Pemeriksaan | Hasil | Bukti dan batas |
|---|---|---|
| full-suite awal | 718 tests, 15 failures | `full-suite/junit-latest.log`; riwayat awal, bukan hasil akhir. |
| full-suite-current | 725 tests, 2 failures; 681,521 detik | `full-suite-current/junit-latest.log`; asumsi tes crossing dan partition diperbaiki, tidak disembunyikan sebagai PASS. |
| final-targeted | 43 tests PASS; 6,55 detik | `final-targeted/junit-latest.log`; termasuk dua tes di atas dan deduplikasi >5.000 mutasi. |
| final-acceptance | 45 tests PASS; 8,139 detik | `final-acceptance/junit-latest.log`; termasuk boundary 49/50 lot + cancel, urutan berita terbalik/episode baru dan katalis turun dengan settlement dua akun NPC. |
| Full regression akhir | 726 tests PASS, 681,754 detik; session 97726 terminal exit 0 | `final-full-suite/junit-latest.log`; 111 suites, termasuk simulasi 730 hari tanpa OOM pada -Xmx384m. Dua check baru sesudah kompilasi suite luas dibuktikan pada final-acceptance. Jangan menghitung jumlah run yang tumpang tindih sebagai total test unik. |
| completion-acceptance | 51 tests PASS; 16,31 detik | `completion-acceptance/junit-latest.log`; termasuk lifecycle pembatalan langganan nyata dan benchmark ledger. |
| completion-fault-check | 25 tests PASS; 14,259 detik | `completion-fault-check/junit-latest.log`; blocker rotasi backup/primary move, byte valid tetap utuh, retry pulih. |
| completion-final | 26 tests PASS; 14,457 detik | `completion-final/junit-latest.log`; dua maker dengan fee/reserve berbeda, estimator BUY/SELL vs settlement serta semua acceptance baru. Hash seluruh Java identik dengan kompilasi terakhir. |
| Benchmark ledger/save panjang | Diukur, bukan klaim perangkat | `completion-final/host-metrics.json`: 11.026 entri, 3.557.028 byte; 30 sampel, P95 save 181,033 ms/max 335,351 ms, P95 load 202,021 ms/max 234,920 ms; catch-up 48 hari satu sampel 625,425 ms. Raw samples dipertahankan, cache warm/cold tidak dipisahkan, input sintetis. |
| UI fungsi produksi | PASS | `ui-clock-production/check.txt`; bridge/worker sintetis: navigation/cache, lock/failure/retry, reduced-motion, toast gagal response/exception/tanpa pesan, fokus. Script runnable: `check-existing-clock.py`; tahun baru/reduced-motion serta 42 kasus toast (3 failure × 7 viewport × 2 skala) memakai fungsi dan CSS produksi lulus. |
| UI produksi + Java host | PASS komponen jam/navigasi: 7 viewport × 2 skala | `ui-clock-production/java-host-check.json`, screenshot java-host-390.png/java-host-320.png; dua jam nyata = empat hari game diproses, PENDING → COMPLETED, jam bergerak, navigasi, zero page errors, tanpa panel baru. Save QA `qa-save-ui-final` dan `qa-save-ui-acceptance`, kedua host sudah dihentikan. ACK ditahan hanya oleh harness pada 14 kombinasi agar navigasi lima tab selama PENDING dapat diuji; clock tetap dalam viewport. Glyph kas header existing terpotong pada 320/360px teks 200%, dicatat B05; bukan kelulusan seluruh layout. Script `check-java-clock.py` perlu WireQaServer aktif. |
| Syntax JavaScript | PASS | `node --check android/app/src/main/assets/www/app.js`. |
| Build Android offline | PASS, 26 detik, 34 tasks | `android-build-retry.log`; compiler/Gradle memberi peringatan deprecated API/features. Percobaan pertama gagal path socket JVM, kemudian properti socket lokal existing menyelesaikannya tanpa perubahan global. |
| Isi UI dalam APK | PASS | `ui-clock-production/source-and-apk-check.json`; bytes assets/www/app.js sama dengan source produksi. APK debug `android/app/build/outputs/apk/debug/app-debug.apk`, SHA256 `CD8B2CD60820B72270B01EB723A3ED2642D9CAF07F55BDD31B0D118A26F760A8`. Belum instalasi/runtime Android. |
| Heap satu sampel | sekitar 156 MB used | `final-full-suite/heap-observation.txt`; runner membatasi heap -Xmx384m. Ini bukan peak memory/RSS maksimum seluruh run. |

## Perlindungan data dan scope

- Save pemain hanya di-hash, tidak dibaca/disalin/diubah. SHA256 tetap `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED` pada pemeriksaan terakhir.
- Baseline 220 file serta Git status awal dipertahankan. `changed-source-files.json` dan `task-final.diff` membandingkan pekerjaan dengan baseline tugas, bukan mengklaim seluruh staged/unstaged/untracked repo sebagai pekerjaan ini.
- Pemeriksaan whitespace terhadap diff tugas: tidak ada tambahan trailing whitespace. `git diff --check` terhadap seluruh perubahan branch menunjukkan whitespace lama di app.js; tidak dilakukan pembersihan di luar lingkup. Line ending SaveManager dan plan dikembalikan mengikuti baseline, tanpa perubahan logika. Hash SaveManager setelah normalisasi CRLF ke LF identik dengan snapshot yang diuji; receipt snapshot-final-check.json mencatat ini.
- Hanya satu subagent review read-only yang sebelumnya diminta pemilik; tidak ada agent implementasi, commit, push, publikasi, instalasi/ADB atau perubahan global pada penyerahan ini.

## Belum dibuktikan

Android WebView/emulator/perangkat fisik, perilaku force-kill/penyimpanan OS pada perangkat, seluruh variasi fault rotasi/move, seluruh kombinasi multi-maker/limit/trigger/halt, UI 200% teks pada semua flow dan peak frame/memory seluruh run belum dibuktikan menyeluruh. Matriks cakupan menjelaskan gap per ID. Kelulusan JVM/browser/APK tidak menutup gap tersebut atau lima backlog review.

## Command dan exit code penutupan

Dijalankan dari root produksi, kecuali Gradle dari `android/`. Semua regression memakai runner existing dan output/save QA baru; tidak install/fetch dependency.

- `Work/run-codex-jvm-check.ps1 -OutputDirectory Artifacts/Codex-Audit-Ulang-2026-11-01/final-full-suite`: exit 0, 726 tests. Selection 111 suites dan hash tersedia dalam direktori run.
- Runner yang sama, `-Tests com.tradingglobal3d.core.AuditRemediationRegressionTest` dan `-OutputDirectory Artifacts/Codex-Audit-Ulang-2026-11-01/completion-final`: exit 0, 26 tests pada source/test terkini; seluruh source tetap identik sesudah kompilasi/run.
- `python Work/Perbaikan-Audit-Ulang-2026-11-01/check-existing-clock.py`: exit 0; fungsi/CSS produksi dengan worker/response sintetis. Terakhir termasuk lintas tahun dan 42 kombinasi toast.
- `python Work/Perbaikan-Audit-Ulang-2026-11-01/check-java-clock.py`: exit 0; host WireQaServer pada 127.0.0.1:8798 dengan save baru qa-save-ui-acceptance, kemudian host dihentikan. Dua viewport catch-up nyata dan 14 kombinasi state PENDING; ACK ditahan hanya dalam harness.
- `gradlew.bat --offline --no-daemon -Dorg.gradle.jvmargs="-Xmx512m -Dfile.encoding=UTF-8" --project-cache-dir ../Artifacts/Codex-Audit-Ulang-2026-11-01/gradle-project-cache :app:assembleDebug`: exit 0 pada retry. JAVA_HOME hanya pada proses shell menuju JBR Android Studio; JAVA_TOOL_OPTIONS menunjuk jdk.net.unixdomain.tmpdir ke direktori .socket-qa existing. Tidak mengubah environment global.
- `node --check android/app/src/main/assets/www/app.js`: exit 0. Isi UI dalam APK diverifikasi byte-for-byte dengan source.

Scope tags per file ada di `task-file-scope.json` sebagai alat bantu review; `task-final.diff` tetap hunk authoritative. Tag A01 pada tes bailout/waktu lama berarti regresi perbaikan awal yang diwajibkan plan, bukan perubahan balancing baru. Penyerahan ini menyelesaikan implementasi/check lokal yang diizinkan dan mencatat batas eksplisit; bukan persetujuan mulai plan lain, memasang APK atau klaim seluruh game siap rilis.
