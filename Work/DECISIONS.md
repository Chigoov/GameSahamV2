# Keputusan dan pertanyaan terbuka

Keputusan terkonfirmasi: trading + karier sama penting; semua fitur lama wajib; perusahaan nyata dengan pasar internal + replay historis; 2D portrait tanpa nuansa 3D; hasil akhir APK.

Semua OQ di bawah berstatus OPEN saat paket disiapkan. PRD lengkap tetap sumber requirement. Menyetujui mulai coding tidak menetapkan jawaban angka ekonomi, sumber data, fidelity replay, target perangkat, atau gate distribusi.

## Kebijakan keputusan

Keputusan teknis rutin yang reversibel boleh dibuat Hermes dan dicatat dengan alasan serta pemeriksaan. Parameter eksperimen pengembangan harus berlabel sementara dan tidak boleh meluluskan acceptance yang memerlukan keputusan produk. Jangan menambah framework untuk mengantisipasi semua jawaban OQ.

Untuk keputusan produk/data yang mengubah perilaku, tulis rekomendasi konkret dan dampaknya, minta jawaban yang diperlukan, lalu kerjakan bagian independen. Jangan menganggap diam sebagai persetujuan dan jangan menghapus fitur wajib untuk menghindari blocker.

## Catatan keputusan baru

Keputusan desain 18 Oktober 2026: sesuai mandat pemilik untuk mengambil keputusan desain, arah kerja ditetapkan dalam Docs/Design/03-Design-Brief-Game-Saham-2D-Portrait.md. Gunakan jurnal investasi 2D dengan navy/putih hangat/emas, IBM Plex Sans, lima root, flow review sebelum transaksi serta seluruh state/responsive/accessibility yang dirinci. Ini keputusan desain kerja, bukan hasil usability test atau jawaban terhadap OQ ekonomi/data/perangkat. OQ-20 memiliki baseline desain untuk implementasi; preferensi tambahan pemilik masih dapat mengoreksinya.

Keputusan arsitektur 18 Oktober 2026 (Task 00):
- Toolchain: OpenJDK 21 + Android SDK 36 + Gradle 8.14.3 + AGP 8.13.0 terpasang dan terverifikasi lokal.
- Host Android: Native Android WebView Application (`com.tradingglobal3d.mobile`) dengan `TradingWebBridge` (@JavascriptInterface).
- Core Engine: Single source of truth logika ekonomi (Java domain di host Android, disertai runner pengujian terisolasi).
- UI: HTML5/CSS3/Vanilla JS 2D Portrait di `assets/www/` (zero external framework, zero CDN).
- Bukti awal: Smoke APK debug berhasil dibangun di `Artifacts/00/smoke-test.apk` (10.8 KB, SHA256: `3f9729a9903d8ba2acaa619d578b31325fe5a6014df5458809398dc8a2f1fe2c`).

Tambahkan tanggal, ID OQ/ADR bila relevan, status PROPOSED/CONFIRMED, pemberi keputusan, alasan, serta pengaruh pada requirement. Jangan mengubah OPEN menjadi CONFIRMED tanpa bukti keputusan.

## Triage Rekomendasi Teknis untuk Keputusan Terbuka (OQ-01 s/d OQ-20)

Status seluruh OQ di bawah tetap **OPEN** untuk konfirmasi final pemilik. Rekomendasi di bawah berfungsi sebagai baseline implementasi default agar pengerjaan independen dapat berjalan tanpa terhambat:

| ID | Status Pemilik | Rekomendasi Default Sementara untuk Pengerjaan Independen |
|---|---|---|
| OQ-01 | OPEN | UI Bahasa Indonesia natural dengan tooltip istilah; mode offline-first tanpa registrasi eksternal. |
| OQ-02 | OPEN | Universe aktif: 10 emiten IDX (LQ45 representatif) dan 10 emiten US (S&P/Nasdaq). Emiten lain katalog pasif. |
| OQ-03 | OPEN | Replay historis: jalur harga data nyata deterministik; order pemain dieksekusi via model likuiditas volume bar. |
| OQ-04 | OPEN | Dataset historis: Daily OHLCV nyata publik berlisensi terbuka; antrean direkonstruksi via model likuiditas berlabel. |
| OQ-05 | OPEN | Dataset dibundel lokal di dalam aset aplikasi (`assets/data/`) agar 100% offline ready tanpa download. |
| OQ-06 | OPEN | Waktu game diskrit per sesi (Sesi 1, Istirahat, Sesi 2); kontrol Play/Pause/Speed; saat background/tutup otomatis pause. |
| OQ-07 | OPEN | Modal awal Rp 50.000.000 & $0 USD; fee transaksi 0,15% beli, 0,25% jual; spread FX 0,5%; settlement T+0 simulasi. |
| OQ-08 | OPEN | Order: Market, Limit, Stop-Loss, Take-Profit; masa berlaku Day Order; antrean halt masuk call auction pembukaan. |
| OQ-09 | OPEN | 4 tipe NPC: Market Maker (likuiditas), Value Investor, Momentum Trader, Retail Noise. Keputusan per tick pasar. |
| OQ-10 | OPEN | Jenjang karier: Novice Trader -> Senior Analyst -> Portfolio Manager -> CIO. Pemulihan bangkrut: dana magang sekali pakai. |
| OQ-11 | OPEN | Management fee 1.5% p.a., Performance fee 20% dengan High-Water Mark (HWM) sejati; penarikan wajib cadangan kas 10%. |
| OQ-12 | OPEN | Multi-aset: Obligasi Negara kupon tetap par 100 dan Emas Spot game kuotasi gram IDR. |
| OQ-13 | OPEN | Fasilitas margin/short dibuka di tier Portfolio Manager (leverage 1:2, maintenance margin 30%, borrow fee 3% p.a.). |
| OQ-14 | OPEN | Peristiwa korporasi fiktif berlabel "Simulasi Game", membedakan dengan tegas fakta historis dan event game. |
| OQ-15 | OPEN | Paket terminal riset: Basic (Free), Pro ($500 USD game/bulan), Enterprise ($2000 USD game/bulan) dengan fitur bertingkat. |
| OQ-16 | OPEN | Target Android 8.0+ (API 26 s/d 36), APK < 25 MB, cold start < 2 detik, memori hemat (< 150 MB heap). |
| OQ-17 | OPEN | Format save modern v2 JSON atomic; converter kompatibilitas jika ditemukan format legacy. |
| OQ-18 | OPEN | Gate APK standalone lulus di CI/host; status pengujian fisik tetap PENDING hingga ada perangkat adb tersambung. |
| OQ-19 | OPEN | Dual mode: Mode Karier (progresi bertahap) dan Mode Lab Strategi (sandbox bebas instrumen terbuka). |
| OQ-20 | OPEN | 5 menu root portrait (Beranda, Pasar, Aset, Firma, Riset), palet Deep Navy/Teal/Amber, target sentuh >= 48dp. |

## Daftar keputusan dari PRD

Pertanyaan berikut adalah daftar keputusan yang masih terbuka. Jawaban tidak diisi menggunakan asumsi tersembunyi.

| ID | Keputusan yang belum ditetapkan | Dampak |
|---|---|---|
| OQ-01 | Apakah pengguna hanya pemilik, kelompok terbatas, atau publik? Seberapa paham mereka tentang saham? | Bahasa tutorial, kedalaman penjelasan, pengujian UX, dan distribusi |
| OQ-02 | Bursa, indeks, instrumen, dan jumlah emiten aktif apa saja yang wajib tersedia? Apakah seluruh anggota setiap indeks harus dapat diperdagangkan? | Batas cakupan data, beban pasar, dan penerimaan fitur global |
| OQ-03 | Dalam replay, apakah jalur harga harus persis historis dengan eksekusi pemain sebagai model terpisah, atau histori menjadi awal skenario yang kemudian dapat berubah? | Definisi replay, market impact, dan pembuktian realisme |
| OQ-04 | Dataset historis berasal dari mana, mencakup tahun berapa, dan beresolusi apa? Apakah tersedia transaksi individual, antrean, fundamental, dan berita bertanggal? | Fitur yang dapat direpresentasikan secara jujur |
| OQ-05 | Bagaimana hak penggunaan dan distribusi dataset untuk APK ditetapkan? Apakah data dibundel, diimpor, atau disediakan sebagai paket tambahan? | Ketersediaan offline, ukuran APK, dan distribusi |
| OQ-06 | Apakah waktu mengikuti dunia nyata atau waktu game? Apakah dapat dijeda/dipercepat? Apa yang terjadi saat background atau aplikasi ditutup? | Sesi, billing, bunga, agenda, dan fair play |
| OQ-07 | Berapa modal awal, biaya transaksi, spread FX, pajak game, settlement, serta aturan pembulatan? | Ekonomi dasar dan rekonsiliasi |
| OQ-08 | Jenis serta masa berlaku order apa yang dipilih? Bagaimana stop/TP, sisa market order, antrean saat halt, dan lelang diproses? | Kontrak eksekusi dan kasus ekstrem |
| OQ-09 | Berapa populasi NPC, ragam strategi, frekuensi keputusan, dan aturan pembentukan likuiditas? | Realisme, determinisme, dan performa |
| OQ-10 | Berapa jenjang, syarat promosi, reward, gaji, penalti, serta jalur pemulihan saat kehabisan modal? | Perjalanan karier dan keseimbangan game |
| OQ-11 | Bagaimana tarif fee, high-water mark, kebutuhan kas, kepercayaan, penarikan, dan penghentian mandat ditentukan? | Ekonomi firma dan konsekuensi klien |
| OQ-12 | Apa bentuk obligasi dan komoditas yang diperdagangkan? Bagaimana unit, kupon, jatuh tempo, settlement, dan konversinya? | Definisi multi-aset yang dapat diimplementasikan |
| OQ-13 | Kapan margin/short dibuka, berapa jaminan dan biaya, serta bagaimana recall dan likuidasi ditangani? | Risiko lanjutan dan potensi kerugian |
| OQ-14 | Apakah skenario korporasi, berita, dan IPO fiktif boleh dilekatkan pada perusahaan nyata dalam mode internal? | Konten dan pembedaan fakta historis dari dunia game |
| OQ-15 | Apa paket terminal, manfaat, harga USD game, periode, dan kebijakan perpanjangannya? | Akses informasi serta keseimbangan progres |
| OQ-16 | Perangkat Android mana yang menjadi target minimum dan penerimaan? Berapa batas APK, memori, waktu buka, dan kelancaran? | Definisi terukur “lebih ringan” |
| OQ-17 | Apakah progres APK lama wajib dapat dilanjutkan seluruhnya? Versi save dan APK lama mana yang menjadi acuan migrasi? | Pengujian kompatibilitas dan perlindungan data |
| OQ-18 | Apakah penerimaan akhir cukup APK terbangun, atau wajib terpasang dan lulus journey pada HP pemilik? | Batas serah terima dan bukti selesai |
| OQ-19 | Apakah seluruh fitur dapat dibuka bertahap melalui karier, atau harus ada mode bebas yang menyediakan semuanya sejak awal? | Onboarding, akses fitur, dan pengujian |
| OQ-20 | Apakah struktur lima menu dan arah visual 2D perlu penyesuaian preferensi khusus? | Finalisasi desain antarmuka |

**Keputusan yang menghalangi spesifikasi implementasi final:** cakupan data dan instrumen, perilaku replay, sistem waktu, parameter ekonomi, definisi instrumen multi-aset, aturan margin/short, serta perangkat dan kriteria penerimaan.

**Status akhir dokumen:** PRD mencakup seluruh fitur wajib yang diminta. Produk dan APK belum dibangun melalui pekerjaan ini. Estimasi jadwal, ukuran APK, dan performa belum dapat ditetapkan sebagai komitmen sebelum keputusan terbuka yang memengaruhinya diselesaikan.
