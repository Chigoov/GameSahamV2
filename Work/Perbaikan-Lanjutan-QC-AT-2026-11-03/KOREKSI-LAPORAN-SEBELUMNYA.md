# KOREKSI RESMI ATAS LAPORAN SEBELUMNYA

**Tanggal:** 3 November 2026, Asia/Jakarta  
**Direktori Dokumen:** `Work/Perbaikan-Lanjutan-QC-AT-2026-11-03/`  
**Rujukan:** Laporan Hasil Sesi 2 November 2026 (`Work/Perbaikan-Gabungan-QC-AT-2026-11-02/LAPORAN-HASIL.md` dan `Work/Perbaikan-AU01-AU55-2026-11-02/LAPORAN-AKHIR-AU01-AU55.md`)

---

## 1. Latar Belakang Koreksi

Pemeriksaan independen pasca-eksekusi terhadap hasil laporan sesi 2 November 2026 menemukan beberapa klaim verifikasi yang melampaui bukti aktual atau memerlukan pengujian/koreksi tambahan yang belum tuntas pada pengujian sebelumnya. Dokumen ini mendokumentasikan koreksi tersebut secara jujur dan transparan demi menjaga integritas rekayasa perangkat lunak.

---

## 2. Rincian Koreksi per Temuan

### A. Koreksi atas Klaim Simulasi Paralel NV01
- **Klaim Sebelumnya:** Laporan sesi 2 November mengklaim bahwa `testNV01` telah membuktikan pencegahan race condition penyimpanan konkuren antara dua instance Activity.
- **Koreksi Nyata:**
  - Pengujian `testNV01` terdahulu hanya memanggil `f1.save()` satu kali secara sekuensial. Itu membuktikan berkas `.tmp` yang dibuat `File.createTempFile` berhasil ditulis dan diganti secara atomik, namun **belum menguji persaingan dua thread/instance secara simultan** dan **belum mencegah snapshot lama menggantikan snapshot yang lebih baru**.
  - **Perbaikan Nyata pada Sesi 3 November (R04):**
    1. Menambahkan *Generation Timestamp Fence* pada `SaveManager.save()`: jika berkas di disk memiliki `savedTimestamp` yang lebih baru daripada snapshot yang akan ditulis, `SaveManager` langsung melempar `IllegalStateException` dan membatalkan penimpaan (*overwrite rejection*).
    2. Menambahkan *Active Bridge Epoch Fence* pada `TradingWebBridge`: setiap Activity baru menaikkan `activeBridgeEpoch`, dan worker Activity lama yang tertinggal mendeteksi bahwa dirinya telah kedaluwarsa (`myEpoch != activeBridgeEpoch.get()`) sehingga secara proaktif membatalkan penyimpanan ke disk.
    3. Menguji secara deterministik melalui `testR04_SaveGenerationFenceRejectsObsoleteDelayedSnapshotFromOverwritingNewerDiskSave` bahwa snapshot lama tertolak dan berkas disk tetap memuat snapshot terbaru.
    4. **Batas Bukti:** Terbukti dan lulus pada lingkungan host Java. Bukti on-device Android fisik tetap berstatus `NEEDS_VALIDATION` karena pengujian perangkat fisik belum diizinkan.

---

### B. Koreksi atas Cakupan Pengujian AT02 (Rebalance Dana Indeks)
- **Klaim Sebelumnya:** Laporan sebelumnya menyatakan order rebalance dana indeks telah terbukti bertahan dan dieksekusi.
- **Koreksi Nyata:**
  - Pengujian `testAT02` terdahulu hanya menjalankan simulasi sampai pukul 08:30 WIB (sebelum bursa buka) dan hanya memeriksa status order masih `OPEN`. Pengujian tersebut **belum membuktikan eksekusi transaksi (fill) pada sesi perdagangan aktif pagi hari**.
  - **Perbaikan & Pembuktian Nyata pada Sesi 3 November (R06):**
    1. Menguji siklus penuh melalui `testR06_FundRebalanceOrderSurvivesOvernightAndExecutesFullyInMorningSession`.
    2. Melewati cutoff 16:00 WIB (order rebalance BUY BBCA terbit), melewati tengah malam (order bertahan tanpa terhapus `expireDayOrders`), hingga pukul 09:05 WIB saat bursa IDX buka.
    3. Memasukkan kuotasi likuiditas jual pada harga limit rebalance, memicu eksekusi matching engine riil, membuktikan status order berubah menjadi `FILLED`, saham BBCA benar-benar masuk ke akun pool reksa dana (`ACC-FUND-FUND-EQ-01`), kas pool terdebit sesuai nilai transaksi, dan NAB reksa dana terhitung konsisten.

---

### C. Koreksi atas Penggunaan Kuota Booster Bebas Komisi (QC04 / R01)
- **Klaim Sebelumnya:** Laporan sebelumnya mengklaim pemotongan kuota booster bebas komisi sudah terikat pada order yang terisi.
- **Koreksi Nyata:**
  - Implementasi sebelumnya menempelkan diskon 100% pada saat order diajukan dan memotong kuota pada `TradingFacade.submitOrder`. Akibatnya:
    - Order pemain yang masuk ke buku antrean (*resting maker order*) dan terisi belakangan oleh NPC tidak memotong kuota booster (karena `submitOrder` dipanggil oleh akun NPC).
    - Order antrean pemain tetap menikmati bebas komisi bahkan setelah kuota booster dihabiskan oleh order lain.
  - **Perbaikan Nyata pada Sesi 3 November (R01):**
    1. Memindahkan resolusi diskon dan konsumsi kuota booster ke titik temu penyelesaian transaksi (*fill settlement time*) di `MatchingEngine` via `FillFeeSettlementHook`.
    2. Order yang resting di buku order tidak mengurangi kuota saat antre.
    3. Ketika terisi oleh siapapun (taker pemain maupun maker pemain yang disambar NPC), tepat 1 kuota booster dikonsumsi, order ditandai `boosterApplied = true`, dan komisi broker 0% diterapkan (pajak penjualan 0.10% tetap berlaku).
    4. Jika kuota booster habis, order antrean berikutnya yang terisi secara otomatis dikenakan tarif komisi normal.

---

### D. Koreksi atas Keterjangkauan Pembentukan Holding 4 Sektor (AT04 / R02)
- **Klaim Sebelumnya:** Laporan sebelumnya menyatakan validasi 4 sektor holding telah diperbaiki dan lulus pengujian.
- **Koreksi Nyata:**
  - Pengujian terdahulu mendirikan 4 perusahaan menggunakan kode sektor mentah di tes Java, namun pada formulir antarmuka produksi (`openCompanyFoundingModal` di `app.js`), pilihan dropdown hanya menyediakan `JASA`, `RITEL`, dan `MANUFAKTUR`. Pemain di aplikasi nyata tidak memiliki opsi untuk mendirikan anak usaha sektor `ENERGY` dan `TECHNOLOGY`, sehingga holding 4 sektor secara praktis tidak dapat dibentuk melalui jalur produksi normal.
  - **Perbaikan Nyata pada Sesi 3 November (R02):**
    1. Memperbarui dropdown `openCompanyFoundingModal` di `app.js` dengan opsi sektor kanonikal lengkap:
       - `JASA` (Keuangan & Jasa Finansial -> `FINANCIAL`)
       - `ENERGY` (Energi & Sumber Daya -> `ENERGY`)
       - `RITEL` (Ritel & Konsumer -> `CONSUMER`)
       - `TECHNOLOGY` (Teknologi & Digital -> `TECHNOLOGY`)
       - `MANUFAKTUR` (Manufaktur & Produksi -> `CONSUMER`)
    2. Menyelaraskan `OperatingCompany.getSectorModel()` untuk mendukung pemetaan konsumer dan teknologi secara stabil.
    3. Membuktikan alur penuh pendirian 4 anak usaha hingga peresmian holding company dan persistensi save/reload melalui `testR02`.

---

### E. Koreksi atas Biaya Sewa Short Setelah Penambahan Tunggakan (QC03 / R03)
- **Klaim Sebelumnya:** Laporan sebelumnya menambahkan tunggakan sewa short ke `ShortPosition.calculateLiability`.
- **Koreksi Nyata:**
  - Metode `calculateDailyBorrowFee` memanggil `calculateLiability`, sehingga tunggakan sewa terhitung sebagai dasar pengenaan biaya sewa harian berikutnya (bunga berbunga / *compounding on arrears*), dan tetap menagih biaya sewa bahkan saat sisa saham pinjaman sudah 0 lembar.
  - Selain itu, perulangan pembersihan posisi short di `RiskComplianceService` menggunakan pernyataan `break;` yang berisiko melewatkan posisi short lainnya milik akun yang sama.
- **Perbaikan Nyata pada Sesi 3 November (R03):**
  1. Memisahkan `calculateBorrowedStockValue` (khusus nilai saham pinjaman) dari `calculateLiability` (nilai saham + tunggakan).
  2. `calculateDailyBorrowFee` kini secara eksklusif menggunakan `calculateBorrowedStockValue`. Jika `borrowedShares == 0`, biaya sewa harian pasti Rp 0.
  3. Menghapus pernyataan `break;` pada pembersihan posisi di `RiskComplianceService` dan menggantinya dengan daftar penghapusan bersih agar seluruh posisi akun diproses tuntas.

---

### F. Koreksi atas Kelayakan Kejadian Ekonomi Warta Whale (QC05 / R05)
- **Klaim Sebelumnya:** Laporan sebelumnya menyatakan warta Whale telah diikat ke `MarketEvent`.
- **Koreksi Nyata:**
  - Logika sebelumnya hanya memeriksa dampak `VERY_POSITIVE` tanpa memvalidasi status `isPublished` dan kelayakan waktu (misal kejadian jauh di masa depan). Jika sektor kejadian tidak memiliki saham di bursa, sistem memilih saham acak secara sembarangan (*arbitrary fallback*).
  - **Perbaikan Nyata pada Sesi 3 November (R05):**
    1. Memvalidasi bahwa `MarketEvent` harus telah terbit resmi (`isPublished == true`), berada dalam rentang waktu yang relevan (maksimal H-1 warta dini hingga H+7), dan memiliki saham yang cocok di sektor terkait.
    2. Menghapus *fallback* acak: jika tidak ada emiten yang relevan di sektor tersebut, warta Whale tidak diterbitkan.

---

Dokumen ini menjadi catatan koreksi permanen yang meluruskan seluruh riwayat pengujian terdahulu dan mengonfirmasi bahwa seluruh perbaikan telah disempurnakan pada tingkat akar masalah.
