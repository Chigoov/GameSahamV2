# MASTER PLAN PERBAIKAN MENYELURUH: SISTEM TRADING, REALTIME CANDLESTICK & OPTIMASI PERFORMA

**Target Proyek:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM` (Game Saham 2D Portrait Android)  
**Dokumen:** Rencana Kerja Root-Cause & Eksekusi Perbaikan  
**Tanggal:** 31 Oktober 2026  
**Status:** Siap Dieksekusi Berdasarkan Temuan Nyata (*Evidence-Based Root Cause Investigation*)  

---

## I. RINGKASAN INVESTIGASI & AKAR PERMASALAHAN (ROOT CAUSE)

Setelah melakukan penelusuran mendalam terhadap runtime Android, logcat, jembatan bridge Java, dan berkas JavaScript/CSS, berikut adalah 3 akar masalah fundamental yang menyebabkan kendala di HP pengguna:

### 1. Masalah: Tidak Bisa Melakukan Aksi Jual dan Beli
- **Akar Masalah 1.1 (Order Book Kehabisan Asks / 0 Penjual):**  
  Pada buku order (`OrderBook.java`), Market Maker (`ACC-NPC-MM-01`) hanya diberikan modal awal 200 lot dan tidak pernah menambah kepemilikan. Ketika stok 200 lot tersebut habis terbeli, kondisi di `NpcAgent.java:122` (`pos != null && pos.getQuantity() >= sellLot`) bernilai `false`. Akibatnya, antrean jual (**Asks**) bernilai kosong (`asks: []`).
  - Efek saat **Beli Market:** Ditolak/dibatalkan otomatis oleh matching engine dengan alasan *"Likuiditas pasar tidak mencukupi"*.
  - Efek saat **Beli Limit:** Pesanan hanya tertahan di antrean beli (*Bids*) dan tidak pernah matched/terisi (*filled = 0*).
  - Efek saat **Jual:** Karena order beli pemain tidak pernah match, portofolio pemain tetap 0 lembar, sehingga aksi jual langsung ditolak *"Jumlah lembar saham tidak mencukupi"* atau *"Order bersilangan dengan akun sendiri"*.
- **Akar Masalah 1.2 (`crypto.randomUUID()` Tidak Didukung di Android WebView):**  
  Pada `app.js` (misalnya baris 9376 pada penyiapan tiket order), kode memanggil `requestId: 'REQ-UI-' + crypto.randomUUID()`. Karena `MainActivity.java` memuat halaman melalui `file:///android_asset/www/index.html`, browser WebView berada dalam konteks tidak aman (*insecure context / `window.isSecureContext === false`*). Pada mode ini, API `crypto.randomUUID` berstatus **UNDEFINED**, memicu `TypeError: crypto.randomUUID is not a function` yang menggagalkan klik tombol *"Tinjau order"*.
- **Akar Masalah 1.3 (Validasi ARA/ARB Mengunci Harga Saat Reference Price Null):**  
  Di `MatchingEngine.java`, jika `previousClosePrice` belum terinisialisasi pada hari pertama, `floor` dan `ceiling` sama-sama mengembalikan `currentPrice`. Hal ini membuat setiap order limit yang berjarak 1 tick dari harga pasar langsung ditolak dengan pesan berada di luar batas ARA/ARB.

### 2. Masalah: Aplikasi Mengalami Lag Parah (Tidak Optimal)
- **Akar Masalah 2.1 (Overload Simulasi Bot Tiap Detik):**  
  Pada perubahan sebelumnya di `TradingFacade.java:2348`, pemanggilan `generateNpcLiquidity()` dipasang di dalam setiap detik waktu berjalan. Hal ini memicu loop simulasi terhadap seluruh 20 emiten aktif, memanggil pencocokan matching engine, ledger mutation, dan array reallocation secara masif di thread utama.
- **Akar Masalah 2.2 (Badai Alokasi JSON & Jeda Garbage Collection 119ms):**  
  Setiap 1 detik, bridge mengirimkan snapshot raksasa (`getSnapshotJson()`) puluhan kilobyte, ditambah polling paralel `getInstruments()`, `getOrderBook()`, dan `getPortfolio()`. Ini membuang memori hingga 50MB–98MB per beberapa detik, memicu jeda ART Garbage Collection Android sebesar **119.178ms** (menjatuhkan 7–8 frame visual). Pengguna yang sedang menyentuh atau menggeser layar merasakan stuttering dan lag yang parah.
- **Akar Masalah 2.3 (Interval 250ms Tanpa Caching):**  
  Timer 250ms di `app.js` terus memanggil `document.getElementById` dan manipulasi DOM berulang pada teks jam dan tombol sponsor.

### 3. Masalah: Grafik, Harga, dan Persentase Tidak Bergerak Realtime
- **Akar Masalah 3.1 (Pembaruan Kuotasi Terblokir oleh Modal Detail):**  
  Pada `app.js:10398`, terdapat penjaga kode:  
  `if (['beranda', 'pasar'].includes(currentTab) && !flowPanel) updateMarketQuotes();`  
  Saat pemain membuka layar detail saham, `flowPanel` bernilai aktif (`true`), sehingga fungsi pembaruan harga bursa **berhenti total dan diblokir**!
- **Akar Masalah 3.2 (Ketiadaan Mesin Lilin Realtime Intraday):**  
  Grafik candlestick saat ini hanya mengandalkan agregasi bucket 15 menit dari backend (`R1D`). Jika belum ada trade baru dalam rentang 15 menit, backend mengirimkan array kosong yang memicu fallback data gelombang statis (`D-29` s/d `D-0`). Akibatnya, lilin di grafik tampak mati membeku.
- **Perbandingan dengan Game Acuan (Capitalism / Tycoon Game REA):**  
  Pada game acuan yang di-reverse engineer, setiap tick transaksi:
  1. Banner harga utama langsung berkedip hijau (naik) atau merah (turun).
  2. Persentase harian (`+X.XX%`) berubah seketika.
  3. Lilin aktif paling kanan (**current active candle**) bergerak dinamis: sumbu *high/low* dan badan *open/close* memanjang atau memendek di depan mata pemain secara realtime.
  4. Antrean bid/ask order book ikut berdenyut dinamis.

---

## II. MATRIKS RENCANA PERBAIKAN BERTAHAP (ACTION PLAN)

```
┌────────────────────────────────────────────────────────────────────────┐
│               MASTER PLAN PERBAIKAN SISTEM TRADING REALTIME            │
├────────────────────────────────────────────────────────────────────────┤
│ 1. SISTEM LIKUIDITAS PASAR & PERBAIKAN JUAL/BELI                       │
│    ├── Market Maker Infinite Liquidity Pool (MM Auto-Replenishment)    │
│    ├── Safe UUID Generator (Hilangkan dependensi crypto.randomUUID)    │
│    ├── Koreksi Validasi Rentang ARA/ARB pada Order Limit & Market      │
│    └── Instant Order Execution untuk Market Order (Fill Terjamin)      │
│                                                                        │
│ 2. OPTIMASI PERFORMA & ELIMINASI LAG (ANTI-STUTTER)                    │
│    ├── Throttling Heartbeat & Lightweight Delta Tick (Bukan Full JSON) │
│    ├── Asynchronous & Buffered Instrument Updates                      │
│    ├── Nonaktifkan Polling DOM Berlebih Saat Layar Diam                │
│    └── Eliminasi GC Pauses (Target: 60 FPS stabil pada HP Android)     │
│                                                                        │
│ 3. MESIN HARGA REALTIME & CANDLESTICK DINAMIS (GAYA REA TYCOON)        │
│    ├── Micro-Tick Price Engine: Denyut fluktuasi harga 1-2 detik       │
│    ├── Live Candlestick Renderer: Lilin aktif paling kanan bergerak    │
│    │   (High/Low/Close update realtime tanpa reload seluruh grafik)    │
│    ├── Visual Price Flash (Hijau saat naik, Merah saat turun)          │
│    └── Sinkronisasi Order Book & Running Tape Transaksi Seketika       │
└────────────────────────────────────────────────────────────────────────┘
```

---

## III. TAHAPAN EKSEKUSI DETAIL (PHASE BREAKDOWN)

### FASE 1: Perbaikan Mesin Transaksi Jual-Beli & Likuiditas Pasar
1. **Penyediaan Likuiditas Berkelanjutan Market Maker (`TradingFacade.java` & `MarketEngine.java`):**
   - Pastikan akun Market Maker (`ACC-NPC-MM-01`) memiliki mekanisme pengisian kembali (*auto-replenishment*) portofolio saham jika stok tersisa di bawah 1.000 lot. Market Maker tidak boleh kehabisan saham untuk antrean jual (*Ask*).
   - Inisialisasi awal Order Book: Saat game dimulai atau dimuat, Order Book wajib langsung terisi antrean 10-tingkat Bid dan Ask sehingga pemain bisa langsung membeli atau menjual tanpa antrean kosong.
2. **Penggantian `crypto.randomUUID()` dengan Generator Aman (`app.js`):**
   - Buat fungsi utilitas aman:
     ```javascript
     function generateSafeId(prefix = 'REQ') {
         return prefix + '-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
     }
     ```
   - Ganti seluruh 9 pemanggilan `crypto.randomUUID()` di `app.js` (tiket order, transfer holding, reksa dana, pinjaman, dan valas) agar 100% kompatibel di Android WebView `file:///`.
3. **Koreksi Eksekusi Market Order & Limit Order (`MatchingEngine.java`):**
   - Pastikan pesanan Beli di harga pasar (*Market Buy*) langsung dieksekusi melawan antrean Ask terbaik Market Maker dengan status `FILLED` dan saham langsung masuk ke portofolio.
   - Pastikan pesanan Jual (*Sell Order*) dapat langsung mengeksekusi Bid terbaik sehingga uang kas langsung masuk ke rekening pemain.

### FASE 2: Mesin Grafik Candlestick & Fluktuasi Harga Realtime (Gaya REA)
1. **Lilin Aktif Dinamis (*Live Candlestick Bar*):**
   - Di `app.js:renderChart`, pisahkan data lilin historis (lampau) dari lilin aktif berjalan (*current candle*).
   - Setiap kali terjadi transaksi atau perubahan harga pada emiten:
     - `currentCandle.close = currentPrice;`
     - `if (currentPrice > currentCandle.high) currentCandle.high = currentPrice;`
     - `if (currentPrice < currentCandle.low) currentCandle.low = currentPrice;`
     - `currentCandle.volume += tradeVolume;`
   - Perbarui elemen SVG lilin paling kanan secara instan (*DOM transform/attribute update*) tanpa merender ulang 30 lilin lainnya dari nol.
2. **Efek Kedip Harga (*Price Flash*) & Persentase Realtime:**
   - Ketika harga saham berubah, angka harga pada kartu emiten dan modal detail langsung berkedip hijau (naik) atau merah (turun) dengan animasi CSS halus (*price-flash-up / down*).
   - Label persentase perubahan harian (`+X.XX%`) langsung dihitung dan diperbarui seketika.
3. **Penyelarasan Kuotasi Saat Layar Detail Terbuka:**
   - Hapus pembatas `!flowPanel` pada interval pembaruan pasar, sehingga saat pemain membuka layar detail saham, harga dan buku order tetap ter-refresh dinamis setiap 1 detik.

### FASE 3: Optimasi Performa Ekstrem (Bebas Lag & Stutter 60 FPS)
1. **Minimalisasi Beban Bridge Java-to-JavaScript:**
   - Hilangkan serialisasi ganda snapshot pada `beginClockSync`.
   - Di `TradingWebBridge.java`, sediakan metode kuotasi ringan: `getLiveMarketQuotes()` yang hanya mengembalikan array ringkas `[ticker, lastPrice, changePct, volume]` tanpa membawa seluruh metadata katalog, deskripsi, dan aturan instrumen.
2. **Debounce & Throttling DOM Updates:**
   - Pindahkan timer 250ms (jam, sponsor, efek berita) ke loop cerdas berbasis `requestAnimationFrame` yang hanya berjalan saat ada perubahan nilai nyata (*Dirty Checking*).
   - Batasi render ulang tabel Order Book hanya jika antrean bid/ask mengalami perubahan kuantitas atau level harga.
3. **Optimasi CSS Rendering di Android:**
   - Hapus properti berat yang memicu paint berlebih pada WebView mobile.
   - Kunci kontainer grafik dengan `contain: layout style paint;` agar animasi lilin tidak memicu reflow pada seluruh halaman Beranda.

### FASE 4: Verifikasi Menyeluruh & Uji Lapangan di HP
1. **Uji Suite Simulasi Java:** Jalankan 17 unit test simulasi ekonomi biner dan validasi matching engine.
2. **Uji Transaksi Beli & Jual Nyata:** Pastikan order beli menghasilkan saham di portofolio dan order jual menghasilkan kas.
3. **Build APK Baru (APK36):** Kompilasi dengan Gradle dan pasang langsung ke HP pengguna via ADB.
4. **Pemeriksaan Visual & FPS:** Konfirmasi pergerakan lilin realtime dan scrolling mulus 60 FPS di layar ponsel.

---

## IV. KRITERIA PENERIMAAN (ACCEPTANCE CRITERIA)
1. Pemain dapat menekan tombol **BELI** $\rightarrow$ memilih kuantitas $\rightarrow$ konfirmasi $\rightarrow$ order langsung terisi (*FILLED*) dan saham bertambah di portofolio.
2. Pemain dapat menekan tombol **JUAL** $\rightarrow$ memilih kuantitas $\rightarrow$ konfirmasi $\rightarrow$ order terisi (*FILLED*) dan kas bertambah.
3. Pada layar detail saham, harga berfluktuasi aktif, dan lilin paling kanan di grafik bergerak naik-turun secara realtime mengikuti transaksi.
4. Aplikasi berjalan mulus di HP tanpa lag, scrolling tidak patah-patah, dan tidak terjadi jeda GC 119ms.
