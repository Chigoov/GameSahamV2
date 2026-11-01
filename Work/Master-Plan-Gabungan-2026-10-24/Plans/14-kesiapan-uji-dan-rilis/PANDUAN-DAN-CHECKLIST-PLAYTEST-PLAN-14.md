# PANDUAN INSTALASI & DAFTAR PEMERIKSAAN PLAYTEST PRIBADI (PLAN 14 — HASIL REMEDIASI)

Dokumen ini disusun untuk memandu pengujian mandiri oleh pemilik pada ponsel Android pribadi dengan kehati-hatian tinggi terhadap kelestarian data progres.

---

## I. Rekonsiliasi Berkas Biner APK Pengujian

Tabel berikut mencantumkan seluruh berkas biner hasil kompilasi yang tersimpan di direktori `Artifacts/Longtask-APK11-2026-10-28/apk/`:

| Nama Berkas APK | Ukuran Eksak (Bytes) | SHA-256 Checksum | Skema Tanda Tangan | Keterangan Snapshot |
|---|---|---|---|---|
| **`GameSaham-APK16-plan13.apk`** | **16.405.060 byte** | `1f88f90e17b113e9d962b19f021078fb98cbb47d5120d5a174c4059fe9c521db` | v2 (Debug Key) | Hasil akhir Plan 13 (29 Okt 02:55) |
| **`GameSaham-APK17-plan14-candidate.apk`** | **16.405.941 byte** | `3a7bb20a18a933ceb55f37f46eff47801df5acd60a5c9c890f02a1ef11072d81` | v2 (Debug Key) | Hasil awal Plan 14 (29 Okt 03:39) |
| **`GameSaham-APK18-plan14-remediated.apk`** | **16.534.646 byte** | `04f5a66b6e71682032e165a89c35b70410b92c4c22989ed9b658f79659542dc7` | v2 (Debug Key) | **Kandidat Terkini Remediasi Review** (29 Okt 04:02) |

---

## II. Panduan Pemasangan Mandiri (Sideloading) & Proteksi Data

### Catatan Keamanan Tanda Tangan (Penting):
Meskipun APK-16, APK-17, dan APK-18 dibangun menggunakan keystore debug lokal yang sama, **kondisi tanda tangan dari aplikasi yang saat ini terpasang di ponsel fisik Anda belum diperiksa melalui ADB** (karena mematuhi batas izin per tindakan). Oleh karena itu:

1. **Wajib Simpan Cadangan Manual Sebelum Update:**
   - Buka aplikasi Game Saham di ponsel Anda.
   - Masuk ke menu **Lainnya** -> **Penyimpanan Permainan & Backup**.
   - Tekan tombol **"Simpan Permainan"** untuk memastikan save terbaru tersinkronisasi.
2. **Transfer Berkas APK-18:**
   - Salin `GameSaham-APK18-plan14-remediated.apk` dari komputer ke ponsel Anda.
3. **Pemasangan Update Langsung (In-Place Update):**
   - Buka berkas APK di ponsel dan pilih **"Update"** / **"Perbarui"** (jangan pilih uninstall).
   - *Jika Android menolak dengan pesan konflik signature:* **JANGAN copot pemasangan secara tergesa-gesa**. Laporkan kepada kami agar metode ekspor data progres via bridge dapat disiapkan terlebih dahulu.
4. **Verifikasi Peluncuran:**
   - Pastikan aplikasi terbuka dengan tema Tycoon Sinematik gelap tanpa force close.

---

## III. Daftar Pemeriksaan (Checklist) Playtest Mandiri 10 Skenario Kunci

| No | Skenario Uji | Tindakan Pengujian Mandiri | Hasil yang Diharapkan | Status Pemeriksaan Host |
|---|---|---|---|---|
| 1 | **Tampilan Beranda & Header** | Buka layar Beranda di ponsel | Kartu kekayaan tampil, chip kas tidak terpotong, jam bursa aktif | Terverifikasi di Host (06-portofolio-480w) |
| 2 | **Siklus Jam Bursa 24×** | Amati pergerakan jam game | 1 hari bursa berjalan ~1 jam nyata; jam bursa 09:00–16:00 WIB | Teruji di GameClock Domain |
| 3 | **Order All-Or-None (AON)** | Beli saham BBCA 3 lot (300 lembar) | Order terisi penuh 300 lembar atau mengantre utuh; 0 parsial fill | Lulus di Test Suite & ProbeAon |
| 4 | **Batal Order Pasar Tutup** | Pasang limit order saat bursa tutup, lalu batalkan | Order batal dan dana tertahan kembali ke kas bebas 100% seketika | Lulus di Plan14GateTwoEconomicTest |
| 5 | **Pusat IPO & Pembatalan** | Buka Pusat IPO, pesan 5 lot, lalu klik Batalkan | Muncul di kartu Pesanan Saya; dana tertahan dilepas saat dibatalkan | Terverifikasi di Bridge & UI |
| 6 | **Leaderboard & Profil NPC** | Buka Liga, ketuk avatar anime NPC | Profil 4 Tab terbuka (Peringkat Kekayaan vs Peringkat Return terpisah) | Terverifikasi di Host & Web |
| 7 | **Terminal Riset Satu Pintu** | Buka menu Lainnya -> Terminal Riset | Header menampilkan status paket jujur, dropdown memuat Radar NPC | Terverifikasi di UI & Bridge |
| 8 | **Penghentian Auto-Renewal** | Langganan Pro $250k, lalu klik Hentikan Auto-Renewal | Akses Pro tetap aktif sampai hari ke-30; saat hari ke-31 turun Basic tanpa debit | Teruji di Plan14GateTwoEconomicTest |
| 9 | **Galeri Koleksi Grid 2-Kolom**| Buka Bisnis -> Koleksi / Galeri | Tampil tata letak 2 kolom bergambar transparan asli dengan badge kelangkaan | Terverifikasi di style.css |
| 10 | **Skenario Offline Catchup** | Tutup aplikasi 10–15 menit, lalu buka lagi | Dialog rekonsiliasi menampilkan ringkasan waktu & mutasi tanpa duplikasi | Lulus di Plan14GateTwoEconomicTest |

---

## IV. Rekomendasi Kesiapan Rilis

- **Siap Uji Pribadi Pemilik:** **YA (READY)** — Biner `GameSaham-APK18-plan14-remediated.apk` bebas dari regresi host (146 tests PASS) dan siap untuk dipasang mandiri oleh pemilik.
- **Siap Uji Terbatas / Beta:** **MENUNGGU HASIL PLAYTEST PEMILIK** — Menunggu catatan umpan balik sesi playtest pemilik di perangkat fisik.
- **Siap Rilis Publik:** **BELUM SIAP** — Belum menggunakan keystore rilis produksi, belum ada izin publikasi, dan beberapa rincian warta/saldo kurang masih terbuka untuk keputusan produk.
