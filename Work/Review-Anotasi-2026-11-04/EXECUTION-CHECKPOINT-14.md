# Checkpoint 14 — logo IPO otomatis dan audit lanjutan

Goal aktif. Turn ini PROGRESS. Rujukan implementation-plan.md E01–E11 dan permintaan logo IPO tanpa pembaruan aplikasi. Root serta branch codex/classic-ui-new-assets diverifikasi. Save pemain hanya di-hash.

Pemilik menolak pola piksel pada preview pertama, kemudian menyetujui revisi simbol perusahaan melalui jawaban “Setuju, gunakan bentuk revisi ini”. Preview Artifacts/Preview-Anotasi-2026-11-04/logo-otomatis.html, HTTP 200 pada port 60551. open_in_codex mengembalikan queued. CUA getState kembali mencatat Local browser use is disabled in Work settings. Ini audit blocker pertama untuk goal baru ini, bukan alasan memakai hitungan goal lama.

## Perubahan yang diterapkan

- app.js memakai issuerLogoSvg dan createIssuerLogo bersama untuk kartu/form/detail IPO, detail pesanan, daftar saham, detail emiten, dan portofolio. Identitas bursa+ticker dinormalisasi. Versi simbol/palet tetap agar reload serta listing memakai gambar yang sama. Simbol SVG dari 12 motif dan delapan palet tersedia offline untuk ticker baru. Enam emiten preview memiliki motif yang disetujui. Motif/palet dapat berulang, sehingga ini tidak menjanjikan logo unik tanpa batas atau merek resmi baru. Logo resmi lokal tetap diprioritaskan dan kegagalan gambar mempertahankan simbol otomatis. Setelah APK ini dipasang sekali, emiten IPO berikutnya tidak membutuhkan aset atau pembaruan aplikasi tersendiri untuk memperoleh simbol.
- style.css menyesuaikan simbol dan ukuran logo referensi pada IPO serta portofolio. createIssuerLogo memakai id instrumen dengan prefix IDX/US, sehingga id pesanan IPO tidak menjadi seed. getMyIpoSubscriptions mengirim currency untuk konsistensi IPO US.
- Delapan SVG resmi HON, MS, ORCL, PEP, PG, DIS, UNH, UPS diunduh dari metadata batch4 yang sudah ditinjau dan lolos pemeriksaan SVG. SHA, asal, dan status hak dicatat di SOURCES.json. JPM/PFE/WFC gagal pemeriksaan dan tidak dibundel. Kini 43 SVG resmi dan lima logo referensi, total 48 emiten. Simbol otomatis mencakup sisanya, tetapi kelengkapan merek resmi E03 masih belum penuh.
- Ditemukan regresi pemisahan IPO pada checkpoint13. allocateIpo memotong uang sehari sebelum saham masuk, sehingga wealth 50 juta terbaca 49,9 juta untuk order 100 ribu. Reproduksi merah pada 1793792841331584300/test.log. Perbaikan menahan dana jatah sampai distribusi, melepas refund pada penjatahan, kemudian memproses pembayaran dan saham bersama. Ledger IPO-PAY dipakai untuk mengenali save bertahap lama yang sudah membayar, agar tidak didebit ulang. Distribusi memvalidasi reservasi agregat sebelum mutasi.
- getMyIpoSubscriptions mengirim heldMoney aktual, refund pembatalan dan nilai nol untuk penjatahan tanpa jatah. UI memakai heldMoney pada daftar pesanan dan menampilkan 0 lot untuk REFUNDED.

## Bukti dan perubahan pemeriksaan

- OwnerAnnotationPolicyTest menambah reproduksi wealth/reservasi, pembayaran/distribusi sekali, save/reload dan kompatibilitas pembayaran bertahap lama. Tahap penjatahan/distribusi/listing tetap diperiksa lewat world clock.
- CorporateActionIpoVotingTest semula menganggap default listing besok masih menerima order hari ini. Run 1793793058634430300 gagal pada batas masa pemesanan. Fixture kini memakai default jadwal baru dengan listing hari+3, menambah assertion hari+1 belum ada saham dan dana masih tertahan, kemudian memeriksa hari+2 distribusi. Assertion dividen, jatah penuh, saham, refund dan rekonsiliasi dipertahankan.
- check-market-ipo-ui.cjs memakai helper produksi bersama dan memeriksa logo normalisasi, id pesanan/IDX/US sebelum-sesudah listing, kestabilan pada konteks JS baru, ticker yang belum dikenal, input menyerupai markup, fallback saat aset gagal dan hash seluruh SVG resmi. Adapter elemen diperluas dengan replaceChildren/querySelector sesuai operasi DOM baru, tanpa menghapus pemeriksaan alur lama.
- Run gabungan akhir 1793793286369763300/test.log lulus 106 tes, 134,888 detik, exit 0. Command: python Work/Review-Anotasi-2026-11-04/run-policy-check.py IpoScheduleAndAllotmentTest IpoAllotment6040AndCancellationTest CorporateActionIpoVotingTest FeatureCompletionCorporateTest OwnerDailyMarketActivityTest OwnerMarginDeadlineTest RiskMarginShortComplianceTest FirmCompanyMandateCriticalSequenceTest FirmClientMandateFeeTest UnifiedWorldClockTest ResearchNewsTerminalTest ChartHistoryRangeTest CareerAccessTest.
- Keempat check-*.cjs, node --check dan git diff --check lulus. Pemeriksaan Node mencakup kontrak interaksi, bukan hasil render WebView.
- gradlew.bat --offline :app:assembleDebug akhir exit 0. SHA256 APK 48cd6231a4e064b1b4edbf1fcc7b772b741c3e907413b389535ac5cd7175b074. Pemeriksaan seluruh www semula menemukan tujuh file preview yang memang dikecualikan app/build.gradle. Sesudah mengikuti pengecualian yang sudah ada, pemeriksaan menangkap style.css lebih baru daripada APK, sehingga APK dibangun ulang. Bukti akhir 451 aset www identik dengan APK dan JS/CSS HTTP identik dengan source ada di 1793793286369763300/source-apk-parity-14.json. Tidak ada verifier produksi yang dilonggarkan.
- SHA256 save pemain tetap 59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed.

## Audit penyelesaian E01–E11

| Requirement | Bukti terkini dan sisa |
|---|---|
| E01 | Kebijakan 24 jam/midnight diuji OwnerAnnotationPolicyTest dan UnifiedWorldClockTest. Source home-date hanya mempunyai formatter bulan panjang. Verifikasi rendered akhir masih terbuka. |
| E02 | OwnerDailyMarketActivityTest lulus, termasuk konservasi order/reservasi, matched volume dan penutupan, serta 30 hari catch-up/save. Performa Android masih perlu bukti perangkat. |
| E03 | Semua ticker mempunyai simbol otomatis; 48 emiten mempunyai logo dari aset resmi/referensi. Uji identitas lintas layar lulus. Kelengkapan merek resmi dan visual akhir masih terbuka. |
| E04 | ChartHistoryRangeTest dan check-chart-controls/density lulus. Bukti browser lama ada di checkpoint09. Pinch fisik dan pemeriksaan akhir masih terbuka. |
| E05 | Tahap, pembayaran/reservasi/refund/distribusi dan save/reload diuji pada source akhir. Visual timeline/logo/status baru masih perlu inspeksi browser. |
| E06 | ResearchNewsTerminalTest, owner policy dan check-research-firm-ui lulus. Bukti visual sebelumnya tidak membuktikan seluruh state akhir. |
| E07 | Promosi membuka Firma, mandat, fee dan isolasi diuji owner policy serta suite Firma. UI terbaru masih perlu pemeriksaan. |
| E08 | Source openIndexDetail memfilter instrumentId anggota dan memakai shared row. Matriks visual sebelumnya ada di checkpoint08/09; logo akhir belum rendered QA. |
| E09 | Uji owner policy, plafon dan short/risk lulus pada source akhir. Bukti UI 2x/3x lama ada di checkpoint09; visual akhir masih terbuka. |
| E10 | OwnerMarginDeadlineTest menguji deadline/persistensi/likuidasi. Warning/countdown/repayment via UI masih perlu pemeriksaan browser. |
| E11 | APK dan parity source lulus. Android runtime/perangkat tetap NEEDS_VALIDATION. |

## Preview dan next_action

Java host terbaru aktif session18508 di http://127.0.0.1:64224/, classes1793793286369763300, save QA qa-save-staged-ipo-01. Task-owned PID24664 sebelumnya diidentifikasi melalui command line lengkap lalu diganti. HTTP 200 serta data jadwal diperiksa. Server60551 untuk preview logo aktif dari turn ini. Server lama64134/64135/64136/64137 memakai classes lama dan bukan bukti source akhir.

Lanjutkan review visual saat akses browser tersedia, terutama logo akhir, IPO semua status, warning→risiko→deadline→repayment, ukuran layar/teks besar dan navigasi. Lanjutkan logo resmi yang aman bila sumber tersedia. Pengujian sentuh Android memerlukan izin perangkat yang berlaku. Goal tetap aktif karena gate tersebut belum terpenuhi. Tidak ada commit/push/publish/ADB/perubahan pengaturan global.
