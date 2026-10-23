# Redesain Game Saham, bukti penyerahan 23 Oktober 2026

Status: **implementasi 84/84 tujuan UI dan verifikasi browser selesai**. Gate empat blok di bawah berlaku untuk sumber final yang dibangun dan diuji; pengujian Android fisik/native tetap NOT_TESTED. Ini tidak otomatis meluluskan ulang seluruh 14 fase produk historis. Root dan Git root: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`. Remote tetap `https://github.com/Chigoov/GameSahamV2.git`; branch lokal `codex/ui-redesign-antislop`, tidak push. Perubahan/untracked sebelum tugas dipertahankan.

## Keputusan dan hasil

Delapan gambar mood board dibuka secara visual sebelum `DESIGN.md` dibuat. Pemilik menyetujui arang `#101318`, panel `#1A2028`, teks `#F2F5F7`, aksen ungu `#B6A0FF`, serta empat tab Ringkasan/Pasar/Aset/Karier. IBM Plex Sans dan OFL yang sudah dibundel dipakai kembali. Tidak menambah framework, dependency, CDN, ilustrasi/logo/foto, atau statistik dekoratif. Antislop core/ui/human/layoutmobile/copywriting aktif mode during; Ponytail memandu pemakaian helper existing dan komponen native.

Semua 26 cakupan lama serta FR-01 sampai FR-20 dipetakan di `DESIGN.md`; 84 tujuan pengujian mencakup subtab, detail, form, review dan status. Berita/riset berada di Pasar; investasi, FX, utang dan alat risiko di Aset; usaha, mandat klien, misi, liga dan era baru di Karier; profil/pengaturan tersedia dari header. ID/hook lama yang masih dipakai dipertahankan atau diberi alias. Caller bridge, tombol berparameter dan event `currentTarget` ditelusuri sebelum perubahan.

Tiga prinsip: angka terlebih dahulu; satu keputusan per lapisan; konteks akun/waktu/simulasi tetap jelas. Semua nilai berasal dari Java. Bridge yang tidak ada menampilkan keadaan terputus, tanpa saldo/harga/chart contoh. Data gagal diperbarui mempertahankan isi terakhir dan fokus. Header/konten bergulir bersama, navigasi tetap dapat dijangkau; teks 200% memakai reflow header dan dua baris navigasi bila perlu. Harga/kekayaan menyesuaikan lebar kontainer agar token angka tetap utuh. Tabel lebar memakai gulir lokal berlabel dan dapat difokuskan.

## Izin perbaikan domain dan batasnya

Awalnya hanya presentasi yang diubah. Setelah baseline menunjukkan 17 kegagalan, pemilik memberi izin eksplisit: “Izinkan perbaikan terbatas sesuai mandat 5 menit, tanpa mengubah ekonomi lain”. Izin dicatat di `AGENTS.md` dan commit domain dipisahkan dari UI.

- `GameClock`: rasio aktif menjadi 288 detik game per detik nyata; 300 detik nyata menjadi tepat satu hari. Background tetap 24, satu jam nyata per hari game, tanpa mewarisi speed foreground.
- `TradingFacade`: alias konstanta mengacu ke clock; snapshot speed membaca multiplier sebenarnya. UI interpolasi mengikuti pause dan multiplier tersebut. Tidak menggandakan konversi waktu.
- `CollectibleItem`/`CollectionManager`: MOTOR/MOBIL memakai aturan kendaraan existing 1 basis poin/hari dengan batas bawah 90%. Kategori lain, tarif, biaya, hadiah dan batas ekonomi tidak diganti. Save V1 diverifikasi terhadap rumus lama lalu dipindahkan anchor dengan mempertahankan nilai tersimpan; V2 memvalidasi rumus aktif. Kasus formula lama/baru bertepatan dan V1 yang dimanipulasi menjadi hasil V2 diuji.
- Fixture usang IPO/berita/fund/akun diperbarui dari katalog dan jadwal nyata. Order karier menggunakan order jual sah dari saham akun existing, lalu mewajibkan fill nyata sebelum misi/promosi. Valuasi nol tetap sah sesuai kontrak nonnegatif, disertai tes sale/reload/retry yang tidak menciptakan kas.
- Audit FR-15 menemukan short sudah ada di domain, tetapi belum punya UI. Aset → Risiko kini menyediakan buka/cover dan kewajiban aktual. `TradingWebBridge.getRiskSnapshot` menambah data baca saja: jumlah saham, jaminan, kewajiban, laba/rugi dan biaya harian. Facade memanggil helper persistensi existing setelah margin borrow/repay serta short open/cover. Tidak mengganti rumus atau tarif. Reload tanpa save eksplisit diuji. Jika acknowledgement/penyimpanan belum pasti, kedua tindakan dalam bagian tersebut ditahan dan pemain diminta memeriksa state sebelum mencoba lagi.

## Perintah dan hasil

Seluruh perintah dijalankan dari root yang diizinkan, memakai runtime/browser/dependency yang sudah ada. Save dan output tes berada di sandbox terpisah; tidak memakai save pemain atau inspeksi ADB baru.

| Pemeriksaan | Perintah utama | Hasil dan bukti |
|---|---|---|
| Baseline Java | `./Work/run-codex-jvm-check.ps1 -OutputDirectory Artifacts/Redesign-Antislop-2026-10-23/runner-check` | 488 tes, 471 PASS/17 FAIL; `Work/REDESIGN-BASELINE-2026-10-23.md` menjelaskan setiap kegagalan. |
| Java integrasi akhir | `./Work/run-codex-jvm-check.ps1 -OutputDirectory Artifacts/Redesign-Antislop-2026-10-23/jvm-release` | **491/491 PASS**, 66 suite, 280,432 detik; `jvm-release/junit-latest.log`. Termasuk urutan kritis 10×100, lifecycle/offline/save/uang/agunan/korporasi/progresi. |
| Browser JavaScript | Harness `--language js --assets .../release-assets --output .../browser-release --classpath .../release-classpath.txt` | **19/19 suite mempunyai run PASS**: 18 pada run utama, `verify-portrait-ui.js` PASS pada `browser-release-retry` setelah timeout saat run serentak. Log gagal tetap ada. |
| Browser Python | Harness `--language python --deep`, sumber `release-assets`, output `python-release` | **14/14 PASS**: sepuluh existing dan empat tambahan. Riset menambah satu suite tersendiri, sehingga total Python **15/15**. |
| Klik mendalam | `--only test-redesign-control-gaps.py test-redesign-corporate-fund-controls.py`, termasuk pada `python-release` | 22 checks/95 klik usaha, pinjaman, margin dan rebalance; 12 checks/80 klik korporasi, tiga voting, IPO berhasil, subscribe/cancel/settlement/redeem sah. |
| Journey final | `--only test-redesign-journeys.py`, `release-assets`, output `final-control-ledger` | Navigasi, profil/aria, pause, misi/liga, Back/fokus, validasi, 200%, gangguan/pemulihan quote, hemat daya/offline, save/load, lima aksi tutorial dan resume. |
| Riset dan risiko | `--only test-redesign-research-controls.py`, `release-notification-assets`, output `notification-final` | **23 checks PASS**: pangkat dari fill/misi/hari nyata; PRO/ENTERPRISE dan empat query, short open/partial/full/ditolak/cancel, acknowledgement gagal setelah mutasi nyata, toast 200%, obligasi/emas beli-jual, deposito dini/jatuh tempo, tinjauan dan konfirmasi era baru. |
| State terkontrol | `--only test-redesign-states.py`, output `python-release` | **30/30 sampel PASS**, 36 screenshot, 0 page error: loading, missing bridge, snapshot gagal/pulih, callback save-warning, kontras tinggi/kurangi gerak. |
| Layar | `python Work/verify-redesign-screens.py --url http://127.0.0.1:18915 --output Artifacts/Redesign-Antislop-2026-10-23/after/release-stable --label after` | **1.344/1.344 PASS**: 84 tujuan × 8 viewport × 100%/200%, **3.200 gambar**, nol page error, nol field tanpa nama aksesibel. |
| Kontras runtime | `python Artifacts/Redesign-Antislop-2026-10-23/contrast-all-routes.py` | **504/504 sampel PASS**, **52.007** pengukuran teks, minimum **6,274:1**; tiga ukuran HP × dua ukuran teks × 84 tujuan. |
| Performa | `benchmark-performance-final.py` melalui harness `python-release` | P95 navigasi **160,32 ms** (160 sampel); aksi biasa **83,50 ms** (128); feedback berat **50,87 ms** (32). Ambang 1000/300/300 ms dipertahankan. Browser laptop, bukan FPS/baterai Android. |
| Build | Java 21 bundled → Gradle wrapper `--project-dir android --offline :app:assembleDebug --no-daemon --console=plain` | Build lokal lulus; log final dan SHA APK direkam dalam `package-verification.json` setelah build akhir. |
| Paket | `python Artifacts/Redesign-Antislop-2026-10-23/verify-package.py`; `apksigner verify --verbose --print-certs` | Isi web APK cocok byte-for-byte sumber final, perubahan produksi Java dibatasi lima file (empat koreksi dan satu snapshot bridge), tanda tangan debug v2 valid. APK **977.793 byte**, SHA256 `32093ac46a3941113b574692f6f913a7ea889999e3f298f3190c30f5ef50033e`. |
| Statik | `node --check android/app/src/main/assets/www/app.js`; `git diff --check`; syntax Python | PASS. Negative-control F03 tetap 4/4 memicu kegagalan yang diharapkan; bukan bukti perjalanan browser. |

## Bukti layar dan kecocokan referensi

Indeks lokal: `Artifacts/Redesign-Antislop-2026-10-23/BEFORE-AFTER.html`. Pilih satu dari 84 tujuan, delapan viewport dan teks 100%/200%. File viewport mempertahankan layout nyata; `expanded-scroll` memperluas kontainer sementara, ditile bila panjang, dan berlabel berbeda. Rekonsiliasi `after/handoff-screen-audit.json` memverifikasi setiap path dan seluruh hasil akhir berasal dari satu run penuh `release-stable`. Source app.js SHA256 `6fadfe59ba1a3d5258fd95a1997888bb2b495dfb9e58fe383da4b892c32a3d71` cocok dengan produksi. Indeks dibuka melalui browser dan gambar perbandingan berhasil dimuat tanpa page error. Baseline memakai data QA nyata; hasil sebelum bukan hasil akhir.

| Referensi | Keputusan yang diterapkan | Layar hasil pada indeks bukti | Selisih yang disengaja |
|---|---|---|---|
| `081b6b0ba1b6e8c60bcff6e7bcf7f8e5.jpg` | Hirarki nilai, grafik, rincian dan aktivitas | `root-beranda`, `performance`, `transactions` | Sidebar desktop dan banner promosi ditiadakan, angka hanya dari domain. |
| `34c98bfef84f3db4a1b34bacefbbec6e.jpg` | Ikhtisar → pola → rincian laporan | `performance`, `inline-daily-report`, `profile-kinerja` | Tidak memakai rating/KPI contoh, panel 3D atau grafik dekoratif. |
| `5d34605b2a78921ea28ef63abeb55cda.jpg` | Komponen kategori dan konsistensi lintas layar | `collections`, `missions-daily`, `company-tim` | Teks diperbesar, target48px, tanpa merek taruhan/gradien serentak. |
| `5f4aaea7dd0d88d3add1425e932f2bfc.jpg` | Nilai dominan dan daftar kepemilikan | `root-aset`, `position`, `wealth` | Aksi berlabel; warna semantik hanya untuk status/untung-rugi. |
| `6382d992a4fd2afff82de7c2ea109a90.jpg` | Candle, volume, antrean dan konteks angka | `instrument-idx`, `instrument-us` | Tidak menambah signal/confidence buatan; tabel horizontal memiliki nama dan fokus. |
| `a74637bbfd8343d2009ff27249c779b1.jpg` | Nilai tabular dan tanda performa | `performance`, `profile-kinerja`, `league-weekly_return` | Tanpa klaim top-trader/win rate yang tidak tersedia. |
| `download (1).jpg` | Ruang saldo tenang, aksen ungu untuk keputusan | `root-beranda`, `root-aset`, `fx-review` | Latar solid, tanpa foto/logo koin/blur atau aksi lingkaran tanpa label. |
| `download.jpg` | Arang-violet, grafik dan dua aksi beli/jual | `instrument-idx`, `order-ticket-buy`, `order-review` | Tanpa Verified/Pro dekoratif, angka/token tetap terbaca pada200%. |

Palet per referensi, tipografi, jarak/radius/elevasi, ikon, motion yang bisa/tidak bisa disimpulkan dan kepadatan dicatat lengkap di `DESIGN.md`. Motion tidak ditebak dari gambar diam.

## Review dan batas bukti

Koordinator membuka 14 contact sheet berisi seluruh 84 tujuan pada320×568/100% dan200% (168 screenshot viewport), kemudian membuka gambar detail harga/tiket/nilai Profil secara penuh. Reviewer terpisah menutup empat temuan: tabel/fokus chart, aria subtab, jam pause, angka terbelah; reviewer domain terpisah menutup validasi migrasi V1. Review root akhir mencatat80kombinasi dengan source stabil, tanpa overflow/targetkecil/kontrasgagal. Lima state200% juga dibuka reviewer state.

Tes gangguan menggunakan fault injection berlabel pada host QA nyata; itu bukan bukti save Android rusak, offline native, instalasi APK, hardware FPS, baterai, termal, atau perangkat fisik. Tidak ada inspeksi ADB baru. Semua kemungkinan outcome transaksi/karier/lifecycle tidak diklaim tercakup hanya karena matriks render lulus.

Supplement terakhir hanya memperlebar isi toast: tiga selector `.game-toast`, `.toast-icon`, `.toast-body`. `notification-css-delta.json` membuktikan seluruh CSS lain dan app.js identik dengan sumber matriks 1.344 sampel. Screenshot matriks tetap dilabeli sumber aslinya; screenshot notifikasi final berada di `notification-final`. Riset/risiko 23 checks dan live-sync diuji ulang setelah perubahan tersebut, keduanya PASS. Teks toast pada 320×568/200% dibuka kembali: judul utuh, satu toast, tinggi 227,1875px ≤40dvh, tombol tutup berfungsi. Tidak mengklaim matriks lama sebagai recapture CSS baru.

Build pertama tanpa properti socket lokal gagal membuka loopback. Build ulang memakai konfigurasi socket QA existing dan lulus. Dua log crash Java karena alokasi memori serta run screenshot yang terputus disimpan; run final memakai heap256MiB/code cache64MiB. Tidak mengubah pengaturan global. Tidak ada kegagalan produk terbuka yang diketahui dalam scope verifikasi browser ini.

## Ledger kontrol

Kontrol pada record berulang mengikuti handler yang sama; tes memeriksa binding parameter ke data domain dan contoh transaksi nyata. Ini bukan klaim setiap tombol pada setiap emiten/koleksi/pegawai diklik satu per satu, atau semua kombinasi gameplay telah dieksplorasi. Geometri dan nama aksesibel mencakup seluruh record yang ter-render pada matriks. Pemeriksaan unik berikut menggunakan klik, bukan ekspor opener screenshot:

| Area | Bukti klik dan hasil |
|---|---|
| Navigasi, header, subtab, filter, fokus/Back/Escape | 19 suite JS, Python portrait, `final-control-ledger/test-redesign-journeys` 12 checks. Termasuk save/load, ukuran teks menu, lima tombol tutorial dan resume. |
| Saham, grafik, fundamental, watchlist, akun dan order | Suite market, live-sync, watchlist, grand-journey, portrait, INT; parameter instrumen/akun, native form validation, review, submit, receipt/cancel, update chart dan fokus diperiksa. |
| Perusahaan, staf dan modal | `python-release/test-redesign-control-gaps`: POLICY, dua arah modal, HIRE/ASSIGN/TRAIN/DISMISS, quote/receipt, laporan. Wired deep menjalankan upgrade dan review. |
| Pinjaman dan margin | Control gaps: cair/restrukturisasi/cicilan, principal turun tepat Rp1 juta; wired deep: agunan personal/cair/prepay; riset final: short partial/full, overcover/cancel, margin dan short acknowledgement hilang, kedua tindakan ditahan. |
| Reksa dana, fund plan | Corporate-fund: subscribe/cancel/rebuy/settle/redeem/settle; control gaps: bobot invalid, simpan rencana, rebalance dengan status actual EXECUTING. Tidak menyamakan EXECUTING dengan seluruh order FILLED. |
| IPO, rights/tender dan RUPS | `python-release/test-redesign-corporate-fund-controls`: 12 checks/80 klik, IPO aktif reserve Rp53.000, tiga jenis penawaran review/confirm/cancel dan tiga pilihan voting pada run terpisah. |
| Riset, obligasi/emas, deposito, era baru | `notification-final/test-redesign-research-controls`: 23 checks; PRO USD500, ENTERPRISE USD2.000 sesuai debit; empat query; empat beli/jual multi-aset; deposito dini dan setelah tenor nyata; konfirmasi era mengubah runId. |
| Lab/replay, kinerja, misi/liga, berita/koleksi | Wired deep 24 checkpoint, suite domain/browser terkait; keputusan lab tidak mengubah akun utama, replay consent/reload/slot terpisah; klaim memakai misi terpenuhi dan berita sudah terbit. |
| Loading, kosong, error, offline, preferensi | `python-release/test-redesign-states`: 30 sampel/36 screenshot; journey gangguan/pulih dan state quote terakhir; missing bridge tidak membuat saldo atau chart contoh. |

## Delivery Gate antislop

Koordinator menjalankan gate setelah implementasi, build, review dan uji ulang. **PASS empat blok untuk scope UI browser yang dinyatakan di laporan ini**. Batas native di atas tetap berlaku; gate ini tidak merupakan sertifikasi setiap kemungkinan state atau seluruh 14 fase produk.

### 1. Hard Gate: PASS

- R-02 PASS: scan tiga sumber www tidak menemukan em dash authored; `flowText` menormalisasi separator data ke titik tengah.
- R-03 PASS: 1.344 sampel final tanpa overflow horizontal, field tanpa nama atau target terlihat <48px; tabel lebar memakai gulir lokal berlabel. Screenshot 200% diperiksa.
- R-17 PASS: angka berasal dari snapshot/bridge, tanpa statistik dekoratif; fixture browser tidak mengganti nilai game.
- R-18 PASS: tidak ada testimoni atau avatar orang rekaan di UI.
- R-23 PASS: empat tab/palet disetujui pemilik; aset lokal existing, tanpa menyalin mood board atau mengunduh aset baru.
- R-24 PASS: empat tujuan nav diklik pada journey; seluruh opener inventaris mempunyai route.
- R-25 PASS: 504 sampel runtime/52.007 pengukuran, minimum6,274:1; token batas kontrol/fokus dihitung terpisah pada review.
- R-26 PASS: ledger kontrol di atas memetakan tindakan ke perubahan domain/hasil terlihat; short yang sebelumnya yatim kini punya UI dan tes buka/cover.
- R-27 PASS: loading, kosong, missing bridge, error/pemulihan dan gangguan host diuji pada suite state/journey.
- R-28 PASS: tidak menambahkan FAQ generik.
- R-32 PASS: fokus terlihat, Tab/Enter/Escape, inert layer, nama form dan subtab aria diuji; nol unnamed field pada matriks.
- R-33 PASS: fitur ditulis pada sumber www/Java; intersepsi IIFE hanya untuk runner screenshot dan tidak masuk APK. Paket diperiksa byte-for-byte.
- R-34 PASS: tema arang disetujui pemilik; tidak ada toggle tema setengah berfungsi. High contrast/reduced motion benar-benar bekerja pada suite state.
- R-35 PASS pada inventory kontrol/handler UI: aplikasi dibangun, 34 suite browser mempunyai run PASS, klik mendalam dan konfirmasi nyata tercatat dalam ledger. Record berulang dan batas state dibedakan di atas, bukan dianggap seluruhnya diklik dari matriks render.
- R-36 PASS: performa dilabeli host browser; tidak ada klaim hardware, keamanan atau pelanggan yang dibuat-buat.
- R-37 PASS: Design Read/dial ditulis sebelum implementasi, arah dan navigasi dikonfirmasi pemilik.
- R-38 PASS: NPC, kabar, harga dan laporan berasal dari simulasi domain dengan label mode, tanpa mengganti isinya dengan data buatan UI.

### 2. Purpose-Gate: PASS

- R-01 PASS: tanpa gradient/glow pada sumber UI; permukaan solid mengikuti DESIGN.
- R-04 PASS: ikon nav geometris menjelaskan chart/pasar/aset/usaha dan ditemani label; tanpa pustaka ikon/dekorasi baru.
- R-06 PASS: IBM Plex Sans lokal dipilih untuk angka/data, OFL existing; tabular-nums menjaga kolom stabil.
- R-07 PASS: tidak ada grid/dot/blueprint background.
- R-08 PASS: panah perubahan uang menunjukkan arah; chevron menandai drill-down, bukan semua CTA.
- R-09 PASS: badge berisi status/akses domain; tanpa Verified/Pro atau angka contoh dekoratif.
- R-10 PASS: tidak memakai backdrop-filter/glassmorphism.
- R-12 PASS: shadow hanya sheet/toast yang melayang; alasan elevasi tercatat di DESIGN.
- R-13 PASS: tidak memakai glow bertumpuk.
- R-14 PASS: ikhtisar, baris data, tabel, form dan receipt berbeda menurut kebutuhan; screenshot seluruh84tujuan direview melalui contact sheet.
- R-19 PASS: transisi120–180ms memperjelas konteks; high-contrast/reduced-motion dan hemat daya diuji.
- R-22 PASS: tidak menambahkan ilustrasi generik atau menyalin aset referensi.

### 3. Liveliness: PASS

- Dial PASS: ENERGY2/RHYTHM2/MOTION2 eksplisit di DESIGN.
- Konsistensi dial PASS: satu struktur data tenang dengan variasi grafik, tabel, form dan laporan; motion berfungsi sebagai umpan balik.
- Titik fokus PASS: nilai utama pada ikhtisar, grafik/detail instrumen, judul keputusan pada form/review; 168 viewport diperiksa manusia.
- Whitespace PASS: token4/8/12/16/24/32 memisahkan label, kelompok dan keputusan; tidak membesarkan ruang kosong demi template.
- Aksen PASS: ungu pada aksi/selected; hijau/merah/amber menyampaikan status berlabel.
- Motif PASS: angka tabular, konteks akun/waktu dan urutan daftar→detail→tinjauan konsisten.
- Design Read PASS: deklarasi awal dan penerapan mood board tercatat, bukan arah default tanpa persetujuan.

### 4. Craftsmanship & Quality Locks: PASS

- C-1 PASS: alasan palet/type/spacing/radius/ikon/elevasi/motion tertulis di DESIGN dan tabel referensi.
- C-2 PASS pada inventory UI: klik mendalam memeriksa transaksi/validasi/hasil nyata; tidak ada kontrol mati yang diketahui. Entitlement terkunci menampilkan syarat domain.
- C-3 PASS: semua bagian melayani fitur/data FR-01–FR-20; peta26cakupan lama tersedia.
- C-4 PASS dalam scope yang diuji: viewport320–1440, teks200%, keyboard/fault/recovery/preferensi lulus; batas Android dan state tak terenumerasi tetap dinyatakan.
- C-5 PASS: tidak ada statistik/testimoni/klaim buatan untuk mengisi layout.
- R-05 PASS: komposisi mengikuti isi game, bukan hero/feature-grid/FAQ/footer template.
- R-11 PASS: radius6/10/16/20 membedakan status/kontrol/panel/sheet; bukan semua pill.
- R-15 PASS: CTA menyebut tindakan seperti Tinjau order, Konfirmasi, Bayar Cicilan dan Beli kembali saham short.
- R-16 PASS: copy tidak menambahkan jargon pemasaran AI/hype.
- R-20 PASS: konteks pasar Indonesia, angka IDR/USD, waktu game dan karier membentuk identitas spesifik.
- R-21 PASS: arang dipilih pemilik setelah membaca referensi; high contrast tersedia dan diuji.
- R-29 PASS: neutral arang/putih, satu aksen ungu, warna semantik dibatasi pada status.
- R-30 PASS: delapan referensi menjadi mood board; logo, ilustrasi dan layout merek tidak disalin.
- R-31 PASS: alasan satu baris setiap keputusan utama tersedia pada tabel token/komponen DESIGN.

## Commit dan next action

File produksi UI: `android/app/src/main/assets/www/index.html`, `style.css`, `app.js`. Produksi Java yang berubah: `core/clock/GameClock.java`, `core/facade/TradingFacade.java`, `core/collection/CollectibleItem.java`, `core/collection/CollectionManager.java`, `mobile/TradingWebBridge.java`. File produk/dokumentasi: `AGENTS.md`, `DESIGN.md`, `Work/STATUS.json`, tiga laporan redesain dan baseline ini. Runner existing diperbaiki, selector/fixture tes existing dimigrasi, lima pemeriksaan browser tambahan serta runner screenshot ditambahkan. Font/OFL, dataset, ID aplikasi dan signing tidak diganti.

Perintah runtime final yang dapat diulang dari root (gunakan output baru agar bukti final tetap tersimpan):

```powershell
./Work/run-codex-jvm-check.ps1 -OutputDirectory Artifacts/Redesign-Antislop-2026-10-23/repeat-jvm
python Work/run-redesign-browser-regressions.py --language js --assets Artifacts/Redesign-Antislop-2026-10-23/release-notification-assets --output Artifacts/Redesign-Antislop-2026-10-23/repeat-js --classpath Artifacts/Redesign-Antislop-2026-10-23/release-classpath.txt
python Work/run-redesign-browser-regressions.py --language python --deep --assets Artifacts/Redesign-Antislop-2026-10-23/release-notification-assets --output Artifacts/Redesign-Antislop-2026-10-23/repeat-python --classpath Artifacts/Redesign-Antislop-2026-10-23/release-classpath.txt
python Artifacts/Redesign-Antislop-2026-10-23/final-evidence-check.py
```

Rekonsiliasi terakhir `FINAL-EVIDENCE.json` PASS: 491 tes Java, 19 suite JS, 15 suite Python, matriks dan kontras sesuai denominator, sumber paket cocok, serta kegagalan percobaan lama tetap disimpan. File ini menyertakan path run PASS yang benar per suite, termasuk retry dan supplement. Laporan tidak menghitung run ganda sebagai suite baru.

- `53e51a8`: mandat pemilik, DESIGN dan peta fitur.
- `7a9efc2`: isolasi runner dan baseline yang jujur.
- `2ace63f`: implementasi seluruh presentasi dan navigasi empat tab.
- `42ea200`: perbaikan Java/fixture terbatas setelah izin pemilik.
- `ae39a48`: persistensi margin dan short sebelum acknowledgement.
- `5eedb8f`: jalur UI short, snapshot baca, label aksesibel dan hasil review.
- `37fa57a`: lebar isi toast pada teks200%, dengan bukti delta CSS terbatas.
- `850db6d`: migrasi tes serta runner/journey/state/klik mendalam.

Tidak ada pertanyaan desain atau blocker implementasi browser yang menunggu pemilik. APK kandidat lokal tersedia, belum dipasang pada perangkat. Next action pengguna: uji bermain kandidat di Android melalui alur instalasi yang dipilih pemilik, lalu catat temuan perangkat. Tidak melakukan inspeksi ADB baru atau push. Commit akhir tes/dokumentasi mencatat paket bukti ini; hash commit dapat dilihat pada branch lokal.
