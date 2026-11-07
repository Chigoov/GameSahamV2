# BASELINE PENGERJAAN: PERBAIKAN LANJUTAN PERFORMA & KETEPATAN UI

Tanggal: 3 November 2026, Asia/Jakarta  
Direktori Proyek: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`  
Dokumen Rencana: `Work/Perbaikan-Lanjutan-Performa-2026-11-03/plan.md`  
Status: **LP00 SELESAI (DIPERBAIKI_DAN_DIUJI_HOST)**

---

## 1. Identitas Baseline Source Code & Repositori

- **Root Direktori:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`
- **Git Branch:** `codex/classic-ui-new-assets`
- **Git HEAD Commit:** `9653f7a992405b4dfaf28acddbf1caf158d03957` (dengan seluruh modifikasi lokal dipertahankan)
- **Integritas Simpanan Pemain Asli:**
  - File: `Artifacts/LocalPreview/preview-save/save_game_v2.json`
  - Referensi SHA-256: `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`
  - Verifikasi SHA-256: `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED` (Bit-perfect, tidak pernah dibuka/disalin)
- **Paket APK Debug Baseline:**
  - File: `android/app/build/outputs/apk/debug/app-debug.apk`
  - Ukuran: `16.407.312 bytes` (15,65 MB)
  - SHA-256: `611cc0adad496a9ee137fd7bff7beab9639bb5b6080582a0b198aa47fd5682fa`

---

## 2. Inventaris Masalah Teridentifikasi untuk Dituntaskan

Berdasarkan pembacaan source code terkini terhadap plan lanjutan:

1. **LP01 (Beranda & Penguat/Pelemah Tab Gabungan):**
   - Di `refreshSnapshot()`: Pemanggilan `updateMarketQuotes()` sebelumnya hanya aktif jika `currentTab === 'pasar' || (currentTab === 'pasar_porto' && currentPasarPortoSubtab === 'pasar')`. Ketika pengguna di tab `beranda`, kuotasi kartu saham `#home-market-list` dan indeks tidak disegarkan setiap detik.
   - Di `updateMarketQuotes()`: Blok filter dan perhitungan `renderMarketHighlights(filtered)` masih dikunci oleh syarat lama `if (currentTab === 'pasar')`. Padahal navigasi produksi telah beralih ke tab gabungan `pasar_porto` (`currentTab === 'pasar_porto' && currentPasarPortoSubtab === 'pasar'`). Akibatnya, pada navigasi produksi, penguat/pelemah tidak diperbarui saat kuotasi berubah!
2. **LP02 (Pilihan Candle, Readout, & Fokus):**
   - Di `renderChart()`: Slider candle `#instrument-chart-section input[data-chart-control="selected-candle"]` dihapus dan dibuat ulang saat refresh grafik, dan `selectedBar` selalu di-reset ke `bars.length - 1` (candle terakhir).
   - Di `updateInstrumentDetailLive()`: Kode live update selalu menimpa `#ohlc-readout` dengan data bar terakhir (`lastBar`), sehingga jika pengguna sedang meneliti candle historis masa lalu, teks readout dipaksa melompat kembali ke candle terkini.
   - Pilihan candle pengguna tidak memiliki pelacakan identitas berbasis timestamp atau waktu game candle.
3. **LP03 (Ketepatan Grafik, Garis vs Candle, & Bounds):**
   - `updateInstrumentDetailLive()` hanya memodifikasi elemen SVG candle (`#live-candle-wick`, `#live-candle-body`), sementara mode grafik garis (`currentChartType === 'line'`) tidak memiliki elemen live wick/body sehingga mode garis tertinggal dan tidak merespons perubahan harga secara dinamis.
   - Jika harga bergerak melampaui `minPrice` atau `maxPrice` dari rentang saat ini, skala sumbu Y (`priceRange`) tidak disesuaikan sehingga candle atau garis terpotong keluar dari canvas grafik.
   - Pembaruan bar terakhir tidak memvalidasi bucket waktu; jika interval waktu candle telah berganti, harga baru tidak boleh menimpa candle lama yang sudah selesai.
4. **LP04 (Cache Struktur Pemegang Saham):**
   - Di `updateInstrumentDetailLive()`: Penanda `ownershipPanel.dataset.loaded = 'true'` dipasang sebelum memastikan pemanggilan berhasil. Jika terjadi kegagalan jaringan/bridge sementara, panel terkunci selamanya dan tidak pernah dicoba muat kembali.
   - Tidak ada pemisahan status: memuat, berhasil, atau gagal dengan tombol coba lagi.
5. **LP08 (Konteks Cache, Overlay, & Duplikasi Pembacaan):**
   - Ketika modal detail saham dibuka di atas tab Beranda/Pasar, kedua panel memanggil `bridge.getInstruments('', '', null, true)` secara terpisah dalam siklus yang sama.
   - `lastMacroCheckDate` dan cache pembacaan bridge perlu divalidasi agar tidak terbawa melintasi new game atau reset sesi.
6. **LP05 & LP06 (Verifikasi Gabungan T01–T15 & Koreksi Laporan PF):**
   - Koreksi klaim PF terdahulu: membedakan pengukuran riil Java, pengujian browser/DOM riil, serta estimasi alokasi memori secara transparan tanpa klaim 100% sebelum pembuktian perangkat fisik.

---

## 3. Strategi Pengujian & Harness Terfokus

- Membuat runner pengujian perilaku terfokus menggunakan Node.js dan browser (`browser_exec`) pada aset produksi `android/app/src/main/assets/www/` untuk memverifikasi T01–T17 secara objektif.
- Menguji invariant domain Java via JUnit 4 runner host Java 21 untuk memastikan tidak ada aturan trading, AON, atau save yang terganggu.
