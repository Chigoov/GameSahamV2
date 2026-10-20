# Laporan Gerbang Kelulusan Phase 06 — Waktu Dunia, Karier, Misi, XP, dan Recovery

- **Phase ID**: 06
- **Nama Fase**: Waktu dunia, karier, misi, XP, dan recovery
- **Waktu Pemeriksaan**: 20 Oktober 2026, Asia/Jakarta
- **Root Produksi**: `C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM`
- **Review Mode**: `SEPARATE_REVIEW_PASS_SINGLE_AGENT`

## 1. Kriteria Wajib dan Bukti

1. **Siklus Hari Bursa & Waktu Dunia**:
   - Kemajuan waktu normal, Skip Day (86.400 detik game), dan offline catch-up (rasio 24×) mematuhi invariant ekonomi domain yang sama.
   - Penutupan hari bursa (`closeDayAndAdvance`) mewajibkan waktu pasar selesai (16:01 WIB), menghasilkan laporan harian (`DailySessionReport`) yang memisahkan arus kas/setoran modal dari hasil investasi murni (P&L).
   - Volume transaksi harian saham di-reset pada hari bursa baru, harga penutupan sebelumnya (`previousClosePrice`) tercatat tepat.

2. **Karier, Misi & Gaji Analis**:
   - Gaji harian dibayarkan sesuai level karier (misal Senior Analyst Rp 500.000, Portfolio Manager Rp 2.500.000).
   - Promosi karier diperoleh secara earned melalui syarat hari trading, skor reputasi, dan kekayaan bersih.
   - Misi karier (misal MIS-01, MIS-02) diselesaikan melalui event domain dan reward misi bersifat idempoten (klaim ulang ditolak tanpa menduplikasi uang).

3. **Mekanisme Pemulihan (Recovery) Solvabilitas Mandat Pemilik**:
   - Pemulihan tanpa uang otomatis: pemain yang modalnya kritis dapat bekerja mengumpulkan gaji harian analis dan menyelesaikan misi.
   - Restrukturisasi utang (`restructureDebt`): fasilitas formal yang meringankan pokok utang margin dengan penalti reputasi tanpa memberikan suntikan uang tunai instan gratis.

4. **Uji Kritis Standar A3 (Minimal 10 Urutan × 100 Aksi Bermakna)**:
   - File: `CareerLifecycleRecoveryCriticalSequenceTest.java`
   - 10 Seed: 6001, 6002, 6003, 6004, 6005, 6006, 6007, 6008, 6009, 6010.
   - Total aksi bermakna: 1.000 aksi (100 aksi per seed mencakup advance time, skip day idempoten, offline background multi-jam, tutup hari bursa & gaji, misi claim idempoten, promosi karier, margin loan & restructure debt, dan save/reload).
   - Invariant diaudit dan lulus setelah SETIAP aksi:
     - `facade.reconcileAll() == true`
     - Saldo kas IDR & USD tidak pernah negatif
     - Available + Reserved == Total cash
     - Hari trading >= 1
     - Reputasi berada pada rentang 0..100
   - **Hasil**: 10/10 sequences PASS.

5. **Pemeriksaan dan Status Bug**:
   - Unit test suite: 20 suites, 233 tests, 0 failures, 0 errors, 0 skipped.
   - Sintaks JS dan Git diff: PASS.
   - Bug terbuka diketahui: 0 kritis, 0 mayor, 0 minor.

## 2. Keputusan Kelulusan
- **Gate Result**: PASS.
