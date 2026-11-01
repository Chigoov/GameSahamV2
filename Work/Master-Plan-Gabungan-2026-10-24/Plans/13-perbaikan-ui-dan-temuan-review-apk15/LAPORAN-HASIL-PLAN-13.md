# LAPORAN LENGKAP EKSEKUSI PLAN 13: PERBAIKAN UI & TEMUAN REVIEW APK-15

**Tanggal Penyusunan:** 29 Oktober 2026  
**Direktori Produksi:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`  
**File Rujukan Plan:** `Work/Master-Plan-Gabungan-2026-10-24/Plans/13-perbaikan-ui-dan-temuan-review-apk15/plan.md`  
**Status Pelaksanaan:** Seluruh pekerjaan independen, kode, uji host (141 tests), investigasi layout, dan biner APK telah selesai penuh. Persetujuan teks tutorial & desain galeri telah diperoleh dan diterapkan ke source code. Hal-hal yang masih memerlukan keputusan lanjutan disajikan di akhir laporan.

---

## I. Ringkasan Eksekutif & Perlindungan Save Pemain

| Parameter | Baseline Awal (APK-15 Review) | Hasil Aktual Plan 13 | Status Verifikasi |
|---|---|---|---|
| **Save Pemain Asli** (`save_game_v2.json`) | `59b41e24c5a0a3b6f87d...` | `59b41e24c5a0a3b6f87d...` | **AMAN 100% (Identik)** |
| **Rangkaian Uji Host Core JVM** | 136 Uji Lulus (Plan 12) | **141 / 141 LULUS (0 Failures)** | **OK (141 tests) — 49,61 dtk** |
| **Unit Test Baru Mandiri** | Belum ada | `Plan13FixesAndAuditsTest.java` (5 uji) | **PASS 100%** |
| **Auto-Renewal Lintas Export/Reload** | Rusak (enrolledTier hilang) | Disimpan di `exportState` & `restoreState` | **TERBUKTI & LULUS UJI** |
| **Status Scope `currentTier` (K01)** | Risiko `ReferenceError` di `app.js` | Dibaca via `bridge.getResearchFeed()` | **TERPERBAIKI & AMAN** |
| **Harga Paket Terminal Riset (K02)** | Pro $250k / Whale $1M (Keputusan) | Diselaraskan di domain, bridge & tes | **TERVERIFIKASI PENUH** |
| **Teks Tutorial Waktu & Jam (K03)** | "5 menit aktif / 1 jam offline" | Diselaraskan: 24×, 09:00–16:00 otomatis | **DISETUJUI & DITERAPKAN** |
| **Tema Tycoon Sinematik (UI)** | Belum teraplikasi di `style.css` | Teraplikasi penuh (Obsidian Bronze Gold) | **LIVE & TERVERIFIKASI** |
| **Empty State "Belum ada posisi"** | Kotak putih terang bocor | Berubah menjadi Dark Bronze elegan | **TERVERIFIKASI VISION** |
| **Galeri Koleksi Mewah (150 Aset)** | Belum diterapkan di `style.css` | Opsi 1 (Showcase Grid 2-Kolom) diterapkan | **DISETUJUI & DITERAPKAN** |
| **Resolusi Potongan Kanan Portofolio** | Teks kanan terpotong di mobile | Padding & viewport diselaraskan | **TERVERIFIKASI VISION UTUH** |
| **Paket Biner Android Baru** | APK-15 Debug | **GameSaham-APK16-plan13.apk** (16 MB) | **Signature Scheme v2 Valid** |
| **Live Web Server** | Offline | `WireQaServer` Aktif di Port 18917 | 146 API Native Terhubung |

---

## II. Rincian Perbaikan Kode & Temuan Kritis (K01–K05)

### 1. K01 — Terminal Riset Satu Pintu & Penanganan State Paket
- **Akar Masalah:** Di `app.js:3239`, fungsi `openResearchTerminal()` memanggil `currentTier`, sedangkan variabel tersebut dideklarasikan secara lokal di dalam fungsi lain (`loadRisetView()`). Ini menimbulkan risiko crash runtime `ReferenceError` serta perilaku salah yang menebak status paket sebagai `BASIC` saat bridge gagal.
- **Perbaikan Nyata:** Di `app.js:3235`, status paket aktif kini dibaca secara aman langsung dari jembatan domain melalui `bridge.getResearchFeed()` ke variabel lokal `activeTier`. Jika pembacaan gagal atau bridge tidak tersedia, antarmuka menyajikan pesan jujur: `Status Tidak Dapat Dimuat` beserta alasan teknisnya, tanpa menebak `BASIC`.

### 2. K02 — Harga USD 250k / 1M & Proteksi Siklus Auto-Renewal Lintas Save/Reload
- **Harga Paket (Keputusan):** Pada `TerminalTier.java`, tarif Pro dipatok definitif sebesar **USD 250.000** dan Whale sebesar **USD 1.000.000** per 30 hari game. Seluruh rangkaian tes regresi (`OwnerAuditRegressionTest`, `TradingWebBridgeTest`, `ResearchNewsTerminalTest`, dan `ResearchTerminalBillingCriticalSequenceTest`) diselaraskan dengan mendepositkan kas USD yang representatif (USD 500.000).
- **Akar Masalah Auto-Renewal Lintas Reload:** Review menemukan bahwa field `enrolledTier` tidak diekspor ke JSON oleh `exportState()` dan tidak dipulihkan oleh `restoreState()`. Akibatnya, saat game disimpan dan dimuat ulang, `enrolledTier` kembali ke default `BASIC`, sehingga ketika pemain melewati masa aktif 30 hari, auto-renewal gagal mendebit dan paket turun ke Basic meskipun kas USD pemain melimpah.
- **Perbaikan Nyata:**
  1. `exportState()` kini secara persisten menyimpan field `"enrolledTier": enrolledTier.name()`.
  2. `restoreState()` memulihkan field `enrolledTier`. Jika memuat save lama tanpa field tersebut, sistem menerapkan fallback aman ke `currentTier` (jika bukan Basic) untuk kompatibilitas mundur.
  3. Diuji tuntas pada `Plan13FixesAndAuditsTest.testAutoRenewalSurvivesExportAndRestoreState()`: pemain dengan saldo USD $600k berlangganan Pro ($250k), diekspor ke JSON, dimuat ke objek baru, diuji aksesnya saat expired, lalu auto-renewal dijalankan: paket terbukti sukses diperpanjang kembali ke PRO dan kas terdebit $250k menjadi $100k.

### 3. K03 — Penyelarasan Waktu 24× dan Teks Tutorial Otomatis
- **Akar Masalah:** Teks tutorial lama menyuruh pemain *"Lakukan penutupan hari"*, memberi kesan keliru seolah pemain harus menutup hari secara manual.
- **Persetujuan & Perbaikan:** Teks baru diajukan dan telah disetujui pemilik:
  - **Langkah 1:** *"Waktu, pasar, dan bisnis terus berkembang 24× (1 hari game berlangsung sekitar 1 jam nyata), baik saat aplikasi dibuka maupun saat ditutup. Bursa buka setiap hari game pukul 09:00–16:00 WIB dan seluruh hasil perdagangan otomatis direkonsiliasi saat kamu kembali."*
  - **Langkah 5:** *"Bursa buka setiap hari game pukul 09:00–16:00 WIB. Hari pasar dan mutasi perdagangan bergulir secara otomatis 24× tanpa perlu penutupan manual; rekapitulasi gaji, bunga harian, dan evaluasi karir langsung diproses otomatis saat pergantian hari."*
  - Tombol aksi diperbarui menjadi: **"Lihat Evaluasi Karir & Misi"**.

### 4. K04 & K05 — Validasi NPC 1.000, IPO 3 Hari, AON Multi-Lawan & Profil
- **Validasi NPC 1.000 (K04-A):** Top 20 konglomerat NPC disemai portofolio saham nyata melalui mutasi buku besar (*ledger*) yang seimbang. `reconcileAll()` terbukti bernilai `true` (seimbang 100%).
- **Jendela IPO 3 Hari Game (K04-B):** `CorporateActionService.orderIpo` menolak pemesanan sebelum tanggal penawaran buka (`Masa penawaran belum dibuka`) dan setelah penawaran tutup (`Masa pemesanan sudah ditutup`).
- **Pencegahan Parsial Fill AON Multi-Lawan (K04-C):** `MatchingEngine.java` mengevaluasi ketersediaan volume lawan agregat. Satuan dilaporkan secara presisi: order beli 300 lembar (3 lot) pemain terbukti melewati maker NPC 100 lembar (1 lot) ke order client 300 lembar (3 lot), sehingga pemain terisi utuh 300 lembar tanpa terpotong sebagian.
- **Peringkat Ganda Terpisah (K05):** Endpoint `getNpcProfile` mengembalikan `netWorthRank` (urutan modal/kekayaan) dan `weeklyReturnRank` (urutan imbal hasil mingguan) yang independen.

### 5. UI — Penerapan Tema Tycoon Sinematik, Empty State & Galeri Opsi 1
- **Aplikasi Tema Tycoon Sinematik:** `:root` di `style.css` diperbarui menggunakan palet gelap mewah Obsidian Charcoal (`#141416`), panel Dark Luxury Bronze (`#1C1916`), teks Warm Ivory Gold (`#F8EEDB`), border tembaga (`#3A3228`), dan tombol aksi emas kaya (`#D4AF37` s/d `#FFDF80`).
- **Perbaikan Kotak Putih Empty State:** Menghapus deklarasi hardcoded `background: #FFFFFF !important; color: #162332 !important;` pada `.empty-state` dan `.index-card.unavailable`. Kotak *"Belum ada posisi"* di layar portofolio kini menyatu sempurna dengan latar belakang perunggu gelap bertabur teks putih gading dan tombol kapsul emas.
- **Penerapan Galeri Opsi 1 (Luxury Showcase Grid 2-Kolom):** Berdasarkan persetujuan pemilik, tata letak `#collections-card-list` di `style.css` kini menggunakan grid 2-kolom responsif dengan kartu vertikal 1:1, bingkai pendaran keemasan, badge kelangkaan, dan visual aset transparan 1254x1254 nyata dari direktori `images/collections/`.
- **Investigasi & Resolusi Potongan Kanan Portofolio:** Teks yang sebelumnya tampak terpotong di kanan layar disebabkan oleh window border desktop headless tanpa scrollbar padding. Padding `.app-header` diselaraskan menjadi `12px 14px 8px` dan `.wealth-chip` menggunakan `max-width: 60%`. Uji visual `06-portofolio-480w.png` mengonfirmasi seluruh teks kanan (chip kas, tombol PRIBADI, dan angka Rp 75.000.000) tampil **100% utuh tanpa horizontal clipping**.

---

## III. Rekonsiliasi Rangkaian Uji Host JVM (Tepat 141 Uji Lulus)

Rekonsiliasi matematis antara kelas uji dan output JUnit `OK (141 tests)`:
1. `com.tradingglobal3d.core.Plan13FixesAndAuditsTest` -> **5 tests**
2. `com.tradingglobal3d.core.Apk11IndependentReviewFixesTest` -> **7 tests**
3. `com.tradingglobal3d.core.Apk10IndependentReviewFixesTest` -> **5 tests**
4. `com.tradingglobal3d.core.NewGameAndResetRegressionTest` -> **4 tests**
5. `com.tradingglobal3d.core.LoanFinancingPolicy2026RegressionTest` -> **5 tests**
6. `com.tradingglobal3d.core.MarketClosedOrderQueueRegressionTest` -> **3 tests**
7. `com.tradingglobal3d.core.OrderMatrixCombinationsTest` -> **3 tests**
8. `com.tradingglobal3d.core.OrderReceiptActualsTest` -> **8 tests**
9. `com.tradingglobal3d.core.NewsRealDeadlineTest` -> **9 tests**
10. `com.tradingglobal3d.core.CompanyOperationsRegressionTest` -> **7 tests**
11. `com.tradingglobal3d.core.CorporateActionIpoVotingTest` -> **5 tests**
12. `com.tradingglobal3d.core.IpoAllotment6040AndCancellationTest` -> **3 tests**
13. `com.tradingglobal3d.core.League1000NpcLeaderboardRegressionTest` -> **2 tests**
14. `com.tradingglobal3d.core.CatalogExpansionRegressionTest` -> **6 tests**
15. `com.tradingglobal3d.core.OwnerAuditRegressionTest` -> **18 tests**
16. `com.tradingglobal3d.mobile.TradingWebBridgeTest` -> **38 tests**
17. `com.tradingglobal3d.core.IpoScheduleAndAllotmentTest` -> **2 tests**
18. `com.tradingglobal3d.core.ResearchNewsTerminalTest` -> **4 tests**
19. `com.tradingglobal3d.core.WireIntegrationDomainTest` -> **7 tests**  
**Total Eksak:** 5 + 7 + 5 + 4 + 5 + 3 + 3 + 8 + 9 + 7 + 5 + 3 + 2 + 6 + 18 + 38 + 2 + 4 + 7 = **141 tests (100% Cocok Presisi dengan Log Runner `OK (141 tests)`)**.

---

## IV. Rekonsiliasi Pemetaan Riset Plan 12 Sesuai plan.md

Pemetaan riset pada laporan sebelumnya telah diselaraskan dengan dokumen kanonis `plan.md`:
- **R06:** Komposisi Portofolio & Alokasi Multi-Aset (Bukan galeri).
- **R08:** Penamaan Order yang Jujur & Eksekusi Pasar Tutup (Limit & Market, bukan AON).
- **R13:** Notifikasi yang Bisa Diatur (Saluran Android & sakelar preferensi, bukan tarif paket).
- **R15:** Dampak Keputusan Karier & Bisnis (Proyeksi laba & beban usaha sebelum ekspansi, bukan terminal satu pintu).
- **R16:** Visual dan Aksesibilitas (Tema Tycoon Sinematik, kontras & target sentuh 48dp, bukan benchmark).
- **Struktur CSV Plan 12:** Berkas `MATRIKS-CAKUPAN.csv` Plan 12 telah diperbaiki di 9 baris yang sebelumnya bergeser kolomnya (baris 42, 61, 69, 77, 94, 98, 101, 107, 109); kini seluruh 116 baris memiliki tepat 8 kolom standar.

---

## V. Benchmark Kinerja 1.000 NPC Host JVM

Hasil pengukuran skrip `Benchmark1000Npc.java` yang tersimpan di `benchmark-1000npc-report.json`:
- **Latensi Pengurutan Leaderboard 1.001 Entitas:** p50: **9,55 ms** · p95: **13,93 ms** (Sangat responsif).
- **Latensi Pengambilan Profil Detail NPC:** p50: **0,61 ms** · p95: **1,13 ms**.
- **Latensi Pembuatan Order per Siklus Tick:** p50: **0,63 ms** · p95: **0,80 ms**.
- **Latensi Serialisasi Simpan Save Game JSON:** p50: **35,60 ms** · p95: **65,84 ms**.
- **Ukuran File Save Game Lengkap:** **844 KB** (Ringan, memori heap JVM stabil pada 50–65 MB).

*Catatan Lingkungan:* Pengukuran ini dilakukan di lingkungan host komputer (Windows 11, OpenJDK 21), bukan tolok ukur render WebView perangkat fisik Android.

---

## VI. Spesifikasi Biner Android APK-16

- **Lokasi Berkas:** `Artifacts/Longtask-APK11-2026-10-28/apk/GameSaham-APK16-plan13.apk`
- **Ukuran File Aktual:** 16.892.650 byte (~16,11 MB)
- **SHA-256 Checksum:** `1f88f90e17b113e9d962b19f021078fb98cbb47d5120d5a174c4059fe9c521db`
- **Verifikasi apksigner (`apksigner.jar`):**
  - Status: **VERIFIES**
  - Skema Tanda Tangan: **APK Signature Scheme v2 (True)**
  - Penandatangan: Android Debug Key (siap untuk sideloading uji mandiri).

---

## VII. Status Keputusan & Persetujuan Pemilik

1. **Persetujuan yang Telah Diberikan & Diterapkan:**
   - **Teks Tutorial Otomatis (Langkah 5):** Disetujui ("Setuju") -> Diterapkan di `app.js:7659-7667`.
   - **Pilihan Desain Galeri Mewah:** Disetujui ("Opsi 1 — Luxury Showcase Grid 2-Kolom") -> Diterapkan di `style.css:#collections-card-list`.
   - **Tema Tycoon Sinematik:** Disetujui -> Diterapkan di `style.css:root`.

2. **Daftar Hal yang Masih Terbuka & Memerlukan Keputusan Lanjutan:**
   - **Kebijakan Saat Saldo Kas USD Kurang untuk Auto-Renewal:** Saat ini saldo yang kurang otomatis menurunkan paket ke `BASIC` tanpa minus. Apakah pemilik menghendaki masa tenggang (*grace period*) 1–2 hari game sebelum fitur terkunci?
   - **Kuota Pasti Warta Berita Pro dan Whale per Hari Bursa:** Saat ini warta intelijen tersedia pada setiap pembukaan bursa dengan frekuensi sedang. Berapa artikel pasti yang Anda kehendaki per hari bursa?
   - **Batas Pengujian Perangkat Fisik:** Seluruh 141 uji coba host JVM telah lulus 100%, server lokal live port 18917 aktif, dan APK-16 berhasil dibangun serta ditandatangani. Pengujian interaksi langsung pada perangkat fisik Android (layar sentuh, rotasi, performa GPU WebView) belum dijalankan karena mematuhi batas proteksi tanpa izin eksplisit ADB/instalasi dari Anda.

---

*Laporan pelaksanaan Plan 13 telah selesai disusun secara lengkap, jujur, dan terverifikasi. Sesuai instruksi penugasan, saya berhenti di sini dan menunggu arahan Anda selanjutnya.*
