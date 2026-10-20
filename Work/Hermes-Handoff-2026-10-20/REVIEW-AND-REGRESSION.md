# Protokol eksekusi, perbaikan dan validasi Hermes

Ini acceptance/prosedur pengujian untuk mandat final, bukan laporan tests sudah dijalankan. Gunakan domain Java existing dan runner existing. Jangan membangun mesin ekonomi tiruan agar tes terlihat lulus.

Standar pemilik terbaru wajib dibaca penuh pada `PHASE-QUALITY-STANDARDS.md`: setiap fase memiliki gerbang kelulusan, nol bug terbuka diketahui termasuk minor, seluruh layar/state/viewport dengan teks normal/200%, minimal 10×100 aksi kritis, threshold performa lokal dan review setiap fase. Ketentuan tersebut menggantikan toleransi minor atau review hanya fase kritis pada catatan lama.

## 1. Siklus wajib setiap irisan pekerjaan

1. Pilih ID requirement/bug, perilaku yang diharapkan dan evidence yang akan membuktikannya.
2. Trace UI → bridge → domain → ledger/save → snapshot, termasuk seluruh caller shared function.
3. Reproduksi baseline; untuk bug nontrivial simpan expected/actual dan check yang gagal sebelum fix bila praktis.
4. Pilih perubahan terkecil yang memperbaiki akar masalah tanpa memangkas requirement.
5. Implementasikan; uji syntax/compile dan perilaku unit/domain.
6. Periksa assertion, log dan actual state. Exit code saja tidak cukup bila runner tidak mencakup kasusnya.
7. Jika gagal: diagnosis baru → fix → jalankan kasus gagal dan sibling caller → periksa ulang invariant.
8. Setelah integrasi: jalankan regresi/bridge/browser yang terdampak. Periksa visual bila UI berubah.
9. Reviewer berbeda memeriksa setiap fase bila tersedia. Tutup seluruh temuan dan lakukan fix/retest yang diperlukan; koordinator memverifikasi hasil setelah integrasi.
10. Checkpoint dengan source revision/hash, file, hasil, evidence, bug dan next action; lanjut task berikut.

Jangan mengulang suite hijau tanpa perubahan atau melonggarkan assertion demi PASS. Setelah dua kegagalan sama tanpa diagnosis/bukti baru, kirim PERLU PERHATIAN dan ganti pendekatan; setelah tiga, pindah tugas independen atau BLOCKED jika benar-benar membutuhkan faktor luar. Tidak memasang scheduler atau menggandakan launcher.

## 2. Pengaman sebelum tes

- Buktikan root/branch/status Git. Inventaris perubahan lokal.
- Backup salinan save pribadi dan validasi lokasi save QA. Tidak melakukan startNewGame/reset pada preview 8766.
- Host 8767 harus terverifikasi loopback, `testFixture: true`, label QA HOST JAVA, dan save terpisah.
- Pisahkan pengujian earned progression dari fixture rank/saldo. Fixture dipakai untuk isolasi kasus, bukan bukti bahwa karier dapat ditempuh.
- Riset dataset harus mengecek lisensi/terms sumber primer. Hasil pencarian atau manifest kosong bukan bukti hak.
- Periksa exit code tiap child command segera, bukan hanya command terakhir. Periksa hasil/XML aktual dan timestamp/source; artefak lama/cache bukan bukti run baru berhasil.
- Multi-agent harus mempunyai work order dan ownership file/port/save/output yang jelas. Jangan paralel menjalankan dua runner yang mereset host QA/save sama. Koordinator meninjau patch dan mengulang tes setelah integrasi; worker tidak memberi completion proyek.
- Verifier bahan untuk snapshot awal. Seed FEATURE-ACCEPTANCE.csv tidak diubah menjadi status implementasi; kemajuan dicatat pada matriks operasional yang ditautkan. Drift dokumen sah setelah pekerjaan bukan alasan reset source atau memalsukan hash.

## 3. Kasus waktu dan lifecycle

Gunakan seluruh TIME-ACCEPTANCE.csv sebagai expected numerik. Rumus bahan tidak cukup; jalankan melalui domain produksi yang sama dengan APK.

Fase uang/order/waktu/save harus menjalankan minimal 10 variasi urutan, masing-masing 100 aksi bermakna, dengan seed/urutan tercatat dan invariant sesudah setiap aksi. Sertakan boundary, proses terputus dan save gagal; no-op/read berulang tidak memenuhi minimum. Jangan mengganti semua kasus wajib dengan sekadar hitungan aksi.

| ID | Skenario tambahan | Bukti yang harus diperiksa |
|---|---|---|
| L01 | Aktif 1× → background → aktif lagi | Tiap interval memakai laju yang benar, tidak overlap atau hilang. |
| L02 | Aktif 5× → background 1 jam | Background hanya 1 hari game, bukan 5 hari. Pilihan foreground kembali sesuai kontrol yang jelas. |
| L03 | onPause/onStop/resume dipanggil berulang | Anchor/event idempotent; tidak menghitung elapsed yang sama dua kali. |
| L04 | Proses mati sesudah mutasi ekonomi tetapi sebelum commit anchor | Atomicity/checkpoint mencegah replay charge/fill ganda; recovery sah. |
| L05 | Proses mati sesudah commit sebagian catch-up | Resume hanya sisa interval; tidak memotong sisa atau mengulang committed chunk. |
| L06 | Write/save gagal saat catch-up | State gagal/recovery jelas; tidak mengklaim sukses atau silent reset. |
| L07 | Save lama tidak mempunyai anchor/rate | Migrasi memulai kebijakan baru tanpa utang retroaktif dari timestamp yang tidak sah. |
| L08 | Jam perangkat mundur, maju, timezone berubah/reboot | Tidak ada negative elapsed, duplikasi event/reward atau trusted anti-cheat palsu. Aturan terdokumentasi. |
| L09 | Waktu dibagi menjadi banyak fragmen pendek | Fractional remainder terjaga; total sama dengan satu langkah setara. |
| L10 | Skip 24 jam setelah pending offline elapsed | Elapsed diselesaikan sekali; lompatan Skip tidak dihitung lagi ketika resume. |
| L11 | Double tap/retry Skip dengan request ID sama | Receipt sama, waktu dan ekonomi tidak maju lagi. |
| L12 | Offline 30 hari nyata atau lebih | Seluruh interval diproses, tanpa cap tersembunyi, overflow, hilangnya order, expiry atau biaya. |
| L13 | Forward normal vs Skip/offline, seed/input setara | Ledger, posisi, fees, event dan hasil ekonomi setara pada game time yang sama, sesuai aturan RNG. |
| L14 | Bursa tutup/weekend/libur, halt/reopen | Semua jalur matching/trigger/NPC mematuhi kalender; bukan hanya label UI. |
| L15 | Masa aktif paket, misi/league, coupon/payroll/dividend melintas banyak periode | Tepat sekali tiap periode/event; tidak hanya periode terakhir atau reward berulang. |
| L16 | Perpindahan mode internal/replay dan akhir data historis | Anchor/state tidak bocor; akhir dataset bukan tamat game; tidak membuat harga masa depan palsu. |
| L17 | Input tickClock elapsed invalid atau callback JS diulang cepat | Domain memvalidasi kontrak; UI tidak menjadi otoritas bebas mencetak waktu/uang. |
| L18 | Pause manual dan lifecycle | Semantik kontrol eksplisit terdokumentasi; penutupan aplikasi tidak diam-diam membekukan dunia. |
| L19 | Proses mati tanpa callback background, flag terakhir ACTIVE/5x | Interval belum committed sejak anchor terakhir direkonsiliasi sebagai tidak aktif 24x; satu jam menjadi satu hari game, bukan memakai laju foreground. Segmen committed tidak diulang. |

Gunakan clock yang dikendalikan untuk tes, bukan menunggu hari nyata. Kalau normal advance bergantung jumlah panggilan RNG, perbaiki/shared-kan aturan step agar hasil tidak berubah hanya karena chunk elapsed. Jangan menutup mismatch dengan membandingkan timestamp saja.

## 4. Kasus uang, order dan akun

| ID | Kasus | Assertion utama |
|---|---|---|
| F01 | Market/limit dengan fill parsial, cancel sisa, reload | Reservasi, kas, posisi, fee dan receipt terekonsiliasi. |
| F02 | Quote berubah/expired setelah preview | Submit mengikuti kontrak validasi; tidak memakai biaya/harga stale tanpa aturan. |
| F03 | Duplicate command, retry setelah timeout, account mismatch | Tidak ada duplikasi atau mutasi akun yang salah. |
| F04 | FX bolak-balik, jumlah kecil/besar, rounding | Spread/fee eksplisit; tidak ada free-money loop dari pembulatan. |
| F05 | Kas nol, negatif ilegal, NaN/Infinity, overflow/batas quantity | Ditolak sebelum mutasi; error state konsisten. |
| F06 | Loan/margin/short, bunga, repayment, liquidation | Liabilitas/collateral/posisi dan ledger benar; net worth bukan kas pinjaman saja. |
| F07 | IPO reserve, partial allocation, cancel/refund, reload | Tidak ada kas hilang atau refund ganda. |
| F08 | Split/dividend/rights/M&A lintas akun/order | Eligibility, basis, fraksi dan aksi terjadi tepat sekali. |
| F09 | Client/firm/company/personal transfers and fees | Akun terisolasi; AUM bukan wealth pribadi; HWM/fee tidak berulang. |
| F10 | Rebalance sebagian gagal/partial fill | Rencana tidak mengarang posisi; sisa kas/order dijelaskan. |
| F11 | Koleksi beli/jual, biaya/valuasi, payroll/benefit | Ledger dan wealth konsisten; efek ekonomi bukan angka UI saja. |
| F12 | Crash, corrupt save, write failure, migrasi dan backup recovery | Tidak silent reset; tidak ada state finansial setengah committed. |
| F13 | Stress/strategy lab/replay isolated runs | Hasil sandbox tidak menambah kas/XP/save utama tanpa aturan. |

## 5. Journey produk dan balancing

Wajib diuji melalui UI/bridge/domain yang sama, dengan save terpisah:

1. Pemain baru: onboarding → tutorial → transaksi valid → portfolio/ledger → misi → promosi earned → akses klien. Fixture rank tinggi tidak menggantikan journey ini.
2. Research: daftar → baca artikel → XP idempoten → paket → manfaat/entitlement → expiry/renewal → riwayat biaya.
3. Firma: menerima mandat → deposit → investasi/rebalance → laporan periode → trust/fee → withdrawal/termination.
4. Perusahaan: pendirian → CEO/pegawai → kebijakan jam kerja/benefit → otomatisasi → payroll/revenue → offline → report dan keputusan besar tetap manual.
5. Koleksi/rival: beli/jual → wealth/risiko → liga/rival NPC → pergantian periode → save/load dengan identitas/progres konsisten.
6. Recovery: kerugian berat/kas nol/utang → pekerjaan earned → restrukturisasi dengan syarat → dapat bertransaksi/berkembang lagi tanpa uang penyelamat otomatis.
7. Tidak ada tamat: sesudah jabatan tertinggi/target kekayaan/league berulang, dunia dan bisnis tetap berjalan. Prestige hanya setelah konfirmasi opsional dan backup.
8. Histori nyata: pilih periode/sumber → validasi → chart/trade → skip/offline → report → save/replay ulang → akhir cakupan tanpa mengakhiri game utama.
9. Balancing: sesi aktif setara 15–30 menit mempunyai tujuan/progres yang dapat dicapai dari tindakan sah. Tidak menjamin profit atau mengubah semua risiko menjadi reward otomatis.

## 6. Browser dan review visual

- Seluruh 47 route existing dipetakan ke navigasi baru; fitur baru ditambahkan ke matriks layar/journey.
- Lima root, semua aksi finansial, form, dialog, loading/error/empty/locked/selected/stale/success.
- Seluruh pasangan layar/state berlaku pada viewport 320×568, 360×640, 390×844, 412×915, 430×932, 768×1024 dan 1440×900, masing-masing pada teks normal serta 200%.
- Viewport pendek, focus modal/input, keyboard navigation, reduced motion, kontras dan status tidak bergantung warna. Semua bug visual/interaksi yang ditemukan ditutup termasuk minor.
- Konten scroll, nav tetap, isi akhir/CTA tidak tertutup, tanpa horizontal overflow tak sengaja.
- Search/filter/scroll/draft sesuai aturan tersimpan; refresh quote tidak mencuri fokus atau menghilangkan pilihan.
- Wealth/fee/position/XP sesuai snapshot domain. Tidak ada chart/reference bar dekoratif dipresentasikan sebagai transaksi.
- Buka screenshot hasil dan bandingkan dengan PNG HP yang benar. Jangan menganggap HTML yang ada atau screenshot yang belum dilihat sebagai bukti UX lulus.
- Native IME/Back/insets/TalkBack/dp/lifecycle/performa dicatat tersendiri; browser tidak meluluskan Android.
- P95 per aksi biasa ≤300 ms, seluruh sampel navigasi yang disyaratkan ≤1.000 ms, feedback heavy work terlihat ≤300 ms dan UI tetap responsif. Minimal 30 sampel per aksi, raw timings/environment disimpan; metode lengkap pada standar fase.

## 7. Review independen dan bug ledger

Untuk setiap bug catat ID/requirement, severity, reproduksi/seed, expected/actual, akar masalah, caller lain, source fix, regression command dan hasil. Reviewer berbeda memeriksa setiap fase bila tersedia. Jika single-agent, lakukan pass review terpisah pada setiap fase dengan bukti fallback yang sah.

Periksa apakah tes dapat mendeteksi bug, bukan sekadar mirror implementasi. Bila mengubah kontrak lama, tautkan keputusan pemilik dan revisi assertion secara sah. Jangan menghilangkan tes sulit, menambah skip, atau mengubah exception menjadi sukses tanpa state yang benar.

## 8. Gate final

1. Review final diff dan matriks seluruh requirement; semua fitur wajib ada dan evidence tepat.
2. Jalankan suite relevan pada source terintegrasi final. Kumpulkan hasil/XML/log aktual, bukan run lama.
3. Periksa screenshot, browser errors/network assets, serta invariants/journey.
4. Nol bug terbuka yang diketahui dalam scope penyerahan, termasuk kritis/mayor/minor, dan tanpa blocker fitur wajib. Seluruh gate fase lulus; tidak menurunkan severity/memindahkan bug ke backlog untuk meloloskan gerbang.
5. Build APK final, periksa app ID/version/signature/asset identity/hash dan cocokkan source yang diuji.
6. Simpan APK/checksum di Artifacts/release/, laporan source revision/evidence/known limits di Work/.
7. Nyatakan COMPLETE_FOR_APK_HANDOFF hanya sesuai prompt; instalasi/native/perangkat tetap NOT_TESTED bila belum dijalankan. Tidak mengklaim bug nol mutlak.
