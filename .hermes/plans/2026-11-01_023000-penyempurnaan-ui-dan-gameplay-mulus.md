# Master Plan: Penyempurnaan UI Ergonomis, Navigasi Mobile, & Alur Gameplay Mulus
**Path File:** `.hermes/plans/2026-11-01_023000-penyempurnaan-ui-dan-gameplay-mulus.md`  
**Proyek:** Game Saham 2D Portrait Android (`com.tradingglobal3d`)  
**Target:** Android Native Java (API 36, Java 21) + WebView HTML5/CSS3/ES6  
**Tanggal Rilis Rencana:** 1 November 2026  

---

## 1. Goal (Tujuan Utama)
Menyempurnakan antarmuka pengguna (UI) dan alur permainan (gameplay) agar berjalan mulus 60 FPS di ponsel Android, menghilangkan interaksi yang macet (dialog browser, tombol back fisik, pembaruan kuotasi tab kombinasi), dan membuka blokir fitur kunci (mandat klien AUM dan asuransi portofolio) tanpa mengubah arsitektur inti.

---

## 2. Current Context & Assumptions
1. **Status Baseline Terkini:**
   - Transaksi Jual/Beli utama, auto-replenishment Market Maker, lilin grafik aktif realtime, format `Money` Rp 0, dan pemuatan 24 reksa dana sudah aktif dan lulus 17 unit test JUnit (`17/17 PASS`).
2. **Kekurangan Nyata yang Masih Mengganjal Kehalusan (*Smoothness Gaps*):**
   - **Tab Kombinasi `pasar_porto`:** Timer live update di `app.js` masih memeriksa `currentTab === 'pasar'` dan `'aset'`, sehingga saat pemain berada di tab kedua, pergerakan kuotasi harga dan portofolio live berhenti berdetak.
   - **Dialog Native Browser (`prompt`, `confirm`, `alert`):** Menu Deposito Bank dan Riset masih memanggil `prompt()` dan `confirm()` yang diblokir bisu oleh Android WebView karena tidak diimplementasikan di `WebChromeClient`.
   - **Navigasi Tombol Back Fisik Android:** Menekan tombol Back fisik Android di tab Beranda langsung mematikan aplikasi secara mendadak tanpa konfirmasi keluar ramah.
   - **Header Subtab Melayang Hilang saat Scroll:** Header switch "Pasar" vs "Portofolio" tergulung keluar layar saat scroll panjang saham.
   - **Bug Kapasitas AUM Klien Pertama:** Perhitungan kapasitas dasar mandat mengembalikan Rp 0 jika belum ada klien, memblokir penerimaan klien pertama selamanya (`0 * 1.5 = 0`).
   - **Asuransi Portofolio Terblokir 24 Jam:** Pengecekan `isAnyMarketOpen() == false` memblokir pembelian polis asuransi karena pasar IDX aktif 24 jam.
   - **Residu Average Cost Saham:** `averageCost` di `Position.java` tidak di-reset ke nol saat saham dijual habis.

---

## 3. Architecture & Proposed Approach
- **Frontend Layer (`app.js`, `index.html`, `style.css`):**
  Menggunakan elemen DOM bottom-sheet kustom untuk menggantikan seluruh interaksi dialog primitif (`prompt/confirm`), menyinkronkan event loop timer dengan status tab `pasar_porto`, membuat header subtab sticky dengan CSS murni, serta menambahkan *double-back press exit guard* pada navigasi Android.
- **Backend Domain Layer (`TradingFacade.java`, `Position.java`, `PortfolioInsuranceService.java`):**
  Memberikan modal kapasitas AUM awal minimal Rp 100.000.000 bagi firma baru, mengizinkan pembelian asuransi lindung nilai berbasis sesi game kalender, dan me-reset nilai persediaan rata-rata posisi saat kuantitas mencapai nol.
- **Android Native Bridge (`MainActivity.java`):**
  Melengkapi `WebChromeClient` dengan handler resmi untuk dialog web sebagai pengaman ganda lapis kedua.

---

## 4. Step-by-Step Implementation Tasks

### Task 1: Pemulihan Detak Harga & Portofolio Live pada Tab Kombinasi `pasar_porto`
- **File Target:** `android/app/src/main/assets/www/app.js`
- **Masalah:** Baris 10519–10522 dan 10503–10504 memeriksa `if (['beranda', 'pasar'].includes(currentTab))` dan `if (currentTab === 'aset')`. Saat berada di tab `pasar_porto`, kedua pemanggilan ini terlewat.
- **Langkah Perbaikan:**
  Ubah blok timer di `app.js` sekitar baris 10518 menjadi:
  ```javascript
  if (!isUserScrolling) {
      refreshSnapshot(snap);
      if (activeReceiptRequestId) updateReceiptLive(activeReceiptRequestId);
      if (activeDetailStock) updateInstrumentDetailLive(activeDetailStock.id);
      
      // Sinkronisasi Tab Utama & Tab Kombinasi (pasar_porto)
      if (['beranda', 'pasar'].includes(currentTab)) {
          updateMarketQuotes();
      } else if (currentTab === 'aset') {
          syncPortfolioLive();
      } else if (currentTab === 'pasar_porto') {
          if (currentPasarPortoSubtab === 'pasar') {
              updateMarketQuotes();
          } else if (currentPasarPortoSubtab === 'porto') {
              syncPortfolioLive();
          }
      }
  }
  ```
- **Verifikasi:**
  Buka pratinjau browser, masuk ke tab `Pasar & Porto`, pastikan harga saham berkedip hijau/merah dan berganti setiap 1 detik.

---

### Task 2: Sticky Header Subtab Switcher "Pasar" vs "Portofolio"
- **File Target:** `android/app/src/main/assets/www/style.css`
- **Masalah:** Saat menggulir 50 saham di bursa, tombol switch subtab tergulung ke atas sehingga pemain harus scroll balik ke pucuk layar untuk membuka portofolio.
- **Langkah Perbaikan:**
  Tambahkan aturan CSS sticky pada `.pasar-porto-header`:
  ```css
  .pasar-porto-header {
      position: -webkit-sticky;
      position: sticky;
      top: 0;
      z-index: 120;
      background: var(--background);
      background-image: var(--bg-gradient);
      padding-top: 6px;
      padding-bottom: 8px;
      margin-bottom: 10px;
  }
  ```
- **Verifikasi:**
  Gulir daftar saham di subtab Pasar, pastikan baris tombol `[Pasar] [Portofolio]` tetap menempel di bagian paling atas layar dan dapat disentuh kapan saja.

---

### Task 3: Penggantian Dialog `prompt()`, `confirm()`, dan `alert()` dengan Custom DOM Modal di Tab Keuangan
- **File Target:** `android/app/src/main/assets/www/app.js`
- **Masalah:** `btnOpen.onclick` pada Deposito memanggil `prompt()`, `btnW.onclick` memanggil `confirm()`, dan pembatalan langganan terminal memanggil `window.confirm()`. Di Android WebView, pemanggilan ini langsung gagal/batal seketika.
- **Langkah Perbaikan:**
  1. Buat helper dialog kustom di `app.js`:
     ```javascript
     function showPromptModal(title, message, defaultValue, onConfirm) {
         const panel = showFlow(title, true);
         flowText(panel, 'Petunjuk', message);
         const input = document.createElement('input');
         input.type = 'number';
         input.className = 'input-field';
         input.value = defaultValue;
         input.style.margin = '12px 0';
         panel.appendChild(input);
         flowButton(panel, 'Lanjutkan Transaksi', () => {
             const val = input.value.trim();
             if (!val || Number(val) <= 0) return;
             closeFlow();
             onConfirm(val);
         });
     }

     function showConfirmModal(title, message, onConfirm) {
         const panel = showFlow(title, true);
         flowText(panel, 'Konfirmasi', message);
         flowButton(panel, 'Ya, Lanjutkan', () => {
             closeFlow();
             onConfirm();
         });
     }
     ```
  2. Ganti pemanggilan `prompt()` pada baris 4476:
     ```javascript
     btnOpen.onclick = () => {
         showPromptModal('Buka Deposito Bank', `Masukkan nominal penempatan deposito di ${p.bankName} (Minimal ${p.minPlacement}):`, '10000000', (nominal) => {
             try {
                 const res = JSON.parse(bridge.openTimeDeposit(JSON.stringify({ productId: p.id, amount: String(nominal) })));
                 if (res.success) {
                     showGameToast('success', 'Deposito Berhasil Dibuka', `Penempatan ${p.bankName} sebesar ${formatMoney(nominal)} aktif.`);
                     refreshSnapshot();
                     loadKeuanganView('bank');
                 } else {
                     showGameToast('error', 'Gagal Membuka Deposito', res.message || 'Penempatan ditolak bank.');
                 }
             } catch (err) {
                 showGameToast('error', 'Galat Deposito', err.message);
             }
         });
     };
     ```
  3. Ganti pemanggilan `confirm()` pada baris 4437 untuk pencairan dini deposito:
     ```javascript
     btnW.onclick = () => {
         const proceedWithdrawal = () => {
             try {
                 const res = JSON.parse(bridge.withdrawTimeDeposit(JSON.stringify({ depositId: d.id })));
                 if (res.success) {
                     showGameToast('success', 'Pencairan Berhasil', 'Pokok deposito berhasil diproses ke kas IDR Anda.');
                     refreshSnapshot();
                     loadKeuanganView('bank');
                 } else {
                     showGameToast('error', 'Pencairan Gagal', res.message);
                 }
             } catch (err) {
                 showGameToast('error', 'Galat Pencairan', err.message);
             }
         };

         if (!d.isMatured) {
             showConfirmModal('Tarik Deposito Lebih Awal', 'Jika dicairkan sebelum jatuh tempo, seluruh bunga akrual akan hangus. Tetap cairkan?', proceedWithdrawal);
         } else {
             proceedWithdrawal();
         }
     };
     ```
  4. Ganti pemanggilan `window.confirm()` pada pembatalan perpanjangan otomatis paket riset (baris 3941).

---

### Task 4: Fail-Safe WebChromeClient di `MainActivity.java`
- **File Target:** `android/app/src/main/java/com/tradingglobal3d/mobile/MainActivity.java`
- **Masalah:** Jika ada pustaka atau kode skrip lain yang memanggil alert/confirm, WebView Android langsung mengabaikannya tanpa respons.
- **Langkah Perbaikan:**
  Perluas konfigurasi `WebChromeClient` di `MainActivity.java` baris 140:
  ```java
  webView.setWebChromeClient(new WebChromeClient() {
      @Override
      public boolean onConsoleMessage(ConsoleMessage message) {
          if (message.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
              android.util.Log.e("GameWebView", message.sourceId() + ":" + message.lineNumber() + " " + message.message());
          }
          return true;
      }
      @Override
      public boolean onJsAlert(WebView view, String url, String message, android.webkit.JsResult result) {
          Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
          result.confirm();
          return true;
      }
      @Override
      public boolean onJsConfirm(WebView view, String url, String message, android.webkit.JsResult result) {
          new android.app.AlertDialog.Builder(MainActivity.this)
                  .setTitle("Konfirmasi")
                  .setMessage(message)
                  .setPositiveButton("Lanjutkan", (d, w) -> result.confirm())
                  .setNegativeButton("Batal", (d, w) -> result.cancel())
                  .setOnCancelListener(d -> result.cancel())
                  .show();
          return true;
      }
  });
  ```
- **Verifikasi:**
  Jalankan `./gradlew compileDebugJavaWithJavac` untuk memastikan kode terkompilasi bersih tanpa peringatan deprecation.

---

### Task 5: Penanganan Tombol Back Fisik Android yang Halus (*Double-Back to Exit*)
- **File Target:** `android/app/src/main/assets/www/app.js`
- **Masalah:** Menekan tombol Back fisik di halaman utama Beranda langsung mematikan proses game secara mendadak.
- **Langkah Perbaikan:**
  Perbarui `window.closeActiveLayer` di `app.js` baris 9338:
  ```javascript
  let lastBackPressTime = 0;
  window.closeActiveLayer = () => {
      // 1. Tutup modal order tiket jika sedang aktif
      if (orderModal && orderModal.classList.contains('active')) {
          closeOrderTicket();
          return true;
      }
      // 2. Tutup sub-halaman flow jika sedang terbuka
      if (flowPanel || document.querySelector('.flow-page')) {
          return closeFlow();
      }
      // 3. Jika sedang di tab selain Beranda, navigasikan kembali ke Beranda terlebih dahulu
      if (currentTab !== 'beranda') {
          switchTab('beranda');
          return true;
      }
      // 4. Jika sudah di Beranda, minta konfirmasi sentuhan kedua sebelum keluar aplikasi
      const now = Date.now();
      if (now - lastBackPressTime < 2500) {
          return false; // Izinkan Activity memanggil finish()
      }
      lastBackPressTime = now;
      showGameToast('info', 'Keluar Game', 'Tekan sekali lagi untuk keluar dari Game Saham.');
      return true;
  };
  ```
- **Verifikasi:**
  Buka game di emulator/perangkat, tekan tombol Back saat di tab Bisnis (berpindah ke Beranda), tekan Back sekali di Beranda (muncul toast), tekan Back sekali lagi dalam 2 detik (keluar dari game).

---

### Task 6: Pembukaan Kunci Mandat Klien Pertama (*Baseline AUM Capacity Fix*)
- **File Target:** `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java`
- **Masalah:** Baris 650 menginisialisasi `base = BigDecimal.ZERO`. Jika pemain belum memiliki mandat aktif, kapasitas dasar bernilai Rp 0, sehingga klien pertama ditolak karena setoran > 0.
- **Langkah Perbaikan:**
  Ubah baris 650 di `TradingFacade.java`:
  ```java
  // Kapasitas dasar awal firma: Rp 100.000.000 (100 Juta IDR) agar pemain dapat menerima klien perdana
  BigDecimal base = new BigDecimal("100000000");
  for (ClientMandate mandate : firmManager.getAllMandates()) {
      if (mandate.getStatus() == ClientMandate.MandateStatus.TERMINATED) continue;
      Money deposit = mandate.getInitialDeposit();
      base = base.add(deposit.getCurrency() == Currency.IDR ? deposit.getAmount() : deposit.getAmount().multiply(rate));
  }
  ```
- **Verifikasi:**
  Buka tab Bisnis $\rightarrow$ Kelola Dana $\rightarrow$ Terima Mandat Nasabah. Mandat perdana harus langsung berhasil diterima dan membuka akun kelolaan nasabah.

---

### Task 7: Pembelian Polis Asuransi Portofolio Dinamis
- **File Target:** `android/app/src/main/java/com/tradingglobal3d/core/risk/PortfolioInsuranceService.java`
- **Masalah:** Baris 105 melempar `IllegalStateException("Polis asuransi hanya dapat dibeli sebelum sesi pasar dibuka")` karena jam bursa IDX aktif 24 jam nonstop.
- **Langkah Perbaikan:**
  Hapus pengecekan kaku jam bursa tutup, dan ganti dengan perlindungan masa berlaku polis berbasis nomor sesi kalender game:
  ```java
  // Polis lindung nilai dapat dibeli kapan saja; proteksi aktif terhitung sejak sesi pembelian hingga sesi kadaluwarsa
  int duration = Math.max(1, durationSessions);
  ```
- **Verifikasi:**
  Buka Tab Bisnis / Lainnya $\rightarrow$ Asuransi Portofolio $\rightarrow$ Beli Polis. Pembelian harus berhasil memotong kas premi dan status polis aktif tercatat.

---

### Task 8: Pembersihan Residu `averageCost` saat Saham Terjual Habis
- **File Target:** `android/app/src/main/java/com/tradingglobal3d/core/model/Position.java`
- **Masalah:** Baris 129: saat `quantity` menjadi 0 pasca penjualan penuh, `averageCost` lama tidak di-reset ke nol. Jika pemain membeli saham yang sama di harga baru berbulan-bulan kemudian, rumus rata-rata lama berpotensi menimbulkan deviasi.
- **Langkah Perbaikan:**
  Tambahkan pembersihan nilai di baris 130 `Position.java`:
  ```java
  this.quantity -= soldQty;
  if (this.quantity == 0) {
      this.averageCost = BigDecimal.ZERO;
      this.acquisitionFeesRemaining = Money.zero(currency);
      this.acquisitionFeeQuantity = 0L;
  } else {
      acquisitionFeesRemaining = acquisitionFeesRemaining.subtract(allocatedFee);
      acquisitionFeeQuantity -= soldQty;
  }
  ```
- **Verifikasi:**
  Beli 100 lembar BBCA di Rp 10.000, jual 100 lembar. Nilai `averageCost` harus tercatat `Rp 0`. Beli kembali di Rp 11.000, `averageCost` harus tepat Rp 11.000 tanpa terpengaruh harga lama.

---

### Task 9: Penambahan Atribut Validasi & Inputmode pada Seluruh Form Input
- **File Target:** `android/app/src/main/assets/www/index.html`
- **Langkah Perbaikan:**
  1. Pasang `inputmode="numeric"` dan `pattern="[0-9]*"` pada input lot `#order-qty`.
  2. Pasang `inputmode="numeric"` pada `#order-limit-price`.
  3. Pasang `maxlength="24"` dan `autocomplete="off"` pada formulir profil pemain.
- **Verifikasi:**
  Buka keyboard virtual di ponsel Android; keyboard angka harus muncul secara otomatis saat kolom jumlah lot ditekan.

---

### Task 10: Sinkronisasi dan Ekspor Status Penghematan Daya
- **File Target:** `android/app/src/main/assets/www/app.js` & `style.css`
- **Masalah:** Mode hemat daya (*power-save mode*) mematikan animasi kompleks, namun transisi warna tombol tetap menyedot GPU.
- **Langkah Perbaikan:**
  Pastikan saat kelas `.power-save` aktif di elemen `html`:
  ```css
  html.power-save * {
      animation: none !important;
      transition: none !important;
      box-shadow: none !important;
  }
  ```

---

## 5. Verification & Testing Matrix

| Tahap Pengujian | Perintah / Prosedur Uji | Kriteria Sukses (Exit Code / Expected Output) |
| :--- | :--- | :--- |
| **Unit Test Sintesa** | `python3 Work/run_synthesis_tests.py` | `OK (17 tests)` / Exit code: 0 |
| **Kompilasi Java** | `./gradlew compileDebugJavaWithJavac --no-daemon` | `BUILD SUCCESSFUL` / 0 compilation errors |
| **Build APK Android** | `./gradlew assembleDebug --no-daemon` | `BUILD SUCCESSFUL` / Berkas APK 16 MB ter-update |
| **Uji Tombol Back** | Jalankan di emulator / HP, tekan Back di Beranda | Toast muncul di tekan pertama, keluar di tekan kedua |
| **Uji Deposito Bank** | Masuk Tab Keuangan $\rightarrow$ Buka Deposito Bank BCA | Muncul bottom sheet DOM cantik tanpa error prompt null |
| **Uji Mandat Klien** | Masuk Tab Bisnis $\rightarrow$ Terima Mandat Perdana | Mandat perdana sukses diterima tanpa error kapasitas AUM |
| **Uji Live Quote** | Buka tab `Pasar & Porto` | Ticker harga dan persentase berkedip dan berganti setiap 1 detik |

---

## 6. Risks, Tradeoffs & Mitigations
- **Risiko 1: Benturan CSS Sticky pada WebView Versi Lama**  
  *Mitigasi:* Menggunakan deklarasi ganda `position: -webkit-sticky; position: sticky;` dengan fallback normal flow.
- **Risiko 2: Kompatibilitas Save Game Lama pada Baseline AUM Capacity**  
  *Mitigasi:* Nilai dasar Rp 100 Juta hanya digunakan sebagai batas dasar (*floor*), tidak mengubah saldo atau nilai simpanan akun yang sudah tercatat.
- **Risiko 3: Dialog Browser Masih Terpanggil di Skrip Pihak Ketiga**  
  *Mitigasi:* Penggantian di `app.js` didukung oleh fail-safe resmi di `MainActivity.java` (`WebChromeClient`).

---
*Rencana ini telah disusun secara komprehensif, bite-sized, dan siap diserahkan kepada agen pelaksana untuk dieksekusi tanpa keraguan teknis.*
