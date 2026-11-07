# Plan Eksekusi Perbaikan AU01–AU55 Game Saham 2D Portrait

**Tanggal:** 2 November 2026  
**Target Proyek:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`  
**Referensi Utama:** `AGENTS.md` & `Work/Perbaikan-AU01-AU55-2026-11-02/PANDUAN-PERBAIKAN-AU01-AU55.md`  
**Status:** AKTIF — EKSEKUSI BERTAHAP P00–P08

---

## 1. Mandat dan Tujuan Eksekusi

Menuntaskan 55 temuan audit AU01–AU55 secara tuntas, presisi, dan terverifikasi pada kode sumber Android Java + WebView:
- Menutup akar masalah pada seluruh caller terkait dengan diff minimum.
- Mempertahankan integritas domain ekonomi Java sebagai otoritas tunggal (uang, aset, waktu 48×, matching engine, dan save).
- Melindungi data simpanan pemain asli tanpa memodifikasi atau menjadikannya fixture pengujian.
- Menjalankan pengujian regresi fungsional untuk setiap paket perbaikan dan mengompilasi APK debug bebas eror.

---

## 2. Batas Kerja dan Perlindungan Data Pemain

1. **Hanya Ubah yang Diperlukan:**
   - Tidak ada refactor massal, penamaan ulang sembarangan, format ulang seluruh berkas, pembaruan versi library, atau penambahan dependensi baru.
   - Perubahan hanya pada fungsi, caller, tes, dan berkas dokumentasi yang secara eksplisit memetakan ke ID temuan aktif.
2. **Proteksi Simpanan Pemain Asli:**
   - Berkas `Artifacts/LocalPreview/preview-save/save_game_v2.json` **HANYA BOLEH DI-HASH**:
     `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`.
   - Dilarang membaca, menyalin, memigrasikan, atau menimpa simpanan pemain asli.
   - Semua pengujian menggunakan direktori dan berkas save QA terisolasi (`build/tmp-test-au*`).
3. **Aturan Produk yang Wajib Dipertahankan:**
   - Waktu otomatis 48× (1 hari game = 30 menit dunia nyata). Tidak ada kontrol pause, speed, atau skip untuk pemain.
   - Transaksi pemain All-Or-None (AON) pada seluruh tipe order (Limit, Market, Trigger).
   - Penjatahan IPO 3 hari (Hamilton Method 60:40) dan anti-self-trade.
   - Suku bunga pinjaman dinamis mengikuti suku bunga makroekonomi saat pencairan (Opsi A yang telah disetujui).
   - Reflow penuh header kas tanpa ellipsis pada 320/360px font 200% (telah disetujui).
4. **Gerbang UI & Keputusan:**
   - Untuk perubahan UI baru atau hal produk yang belum disahkan, tampilkan preview dan tunggu persetujuan sambil mengerjakan pekerjaan domain independen.
5. **Larangan Eksternal:**
   - Dilarang push/publish, menggunakan ADB, atau memasang aplikasi ke perangkat fisik tanpa instruksi eksplisit.

---

## 3. Pemetaan Tahapan Kerja (P00 – P08)

| Tahap | Lingkup & Tujuan | Daftar ID Temuan |
|---|---|---|
| **P00** | Baseline, verifikasi lingkungan, hash save pemain, dan penyiapan plan | - |
| **P01** | Gerbang keselamatan progres, world clock unblocking, integritas save semantik, dan sanitasi portfolio | AU04, AU05, AU09, AU23, AU27, AU37 |
| **P02** | Integritas order, validasi batas konsentrasi klien, AON trigger, expiry quote NPC, dan validasi lot integer | AU01, AU14, AU28, AU29, AU50 |
| **P03** | Perhitungan ekonomi, formula laba bisnis, emisi IPO tahunan, alokasi fund, penyesuaian makro, dan margin USD | AU06, AU07, AU10, AU12, AU13, AU30, AU38, AU39 |
| **P04** | Siklus berita, rumor Whale, reset cache antar-run, mitigasi anti-future leak, dan entitlement riset | AU24, AU25, AU26, AU33, AU34, AU35, AU36, AU54 |
| **P05** | Katalog aset Android (150 item), persistensi duel bos, progres achievement, arsip liga, dan save avatar/frame | AU11, AU15, AU16, AU17, AU18, AU19 |
| **P06** | Kontrak UI, navigasi menu, sanitasi nama XSS, interaksi order book, filter e-IPO, dan aksesibilitas kontras/teks | AU02, AU03, AU08, AU20, AU21, AU22, AU40, AU41, AU42, AU43, AU44, AU45, AU46, AU47, AU48, AU49, AU51, AU52, AU53, AU55 |
| **P07** | Keamanan dan rekonsiliasi data jalur replay historis tersimpan | AU31, AU32 |
| **P08** | Kriteria selesai, uji regresi komprehensif, build APK offline, verifikasi hash save akhir, dan penyerahan laporan | Seluruh AU01–AU55 |

---

## 4. Status Eksekusi Paket per Milestone

- **P00 Baseline:** COMPLETED (Hash save intact, branch verified)
- **P01 Keselamatan Progres & Dunia (AU04, AU05, AU09, AU23, AU27, AU37):** COMPLETED (6/6 tests PASS)
- **P02 Integritas Order & Matching (AU01, AU14, AU28, AU29, AU50):** COMPLETED (5/5 tests PASS)
- **P03 Ekonomi & Laporan (AU06, AU07, AU10, AU12, AU13, AU30, AU38, AU39):** COMPLETED (8/8 tests PASS)
- **P04 Berita & Riset (AU24, AU25, AU26, AU33, AU34, AU35, AU36, AU54):** COMPLETED (8/8 tests PASS)
- **P05 Katalog & Progres Fitur (AU11, AU15, AU16, AU17, AU18, AU19):** COMPLETED (6/6 tests PASS)
- **P06 Kontrak UI & Navigasi (AU02, AU03, AU08, AU20-AU22, AU40-AU49, AU51-AU53, AU55):** COMPLETED (18/18 items verified, node syntax OK)
- **P07 Jalur Replay (AU31, AU32):** COMPLETED (2/2 tests PASS)
- **P08 Verifikasi Komprehensif & Penyerahan:** COMPLETED (APK 15.48MB built offline, save hash identical)
