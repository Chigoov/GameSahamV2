# LAPORAN STATUS PENYIAPAN PENGUJIAN ANDROID & LINGKUNGAN BERSYARAT (M7)

**Status:** `BELUM_DIUJI (MENUNGGU IZIN PER TINDAKAN DARI PEMILIK)`  
**Run ID:** `RUN-STRESS-20261029-01`  
**Paket Biner Terkait:** `GameSaham-APK19-plan14-final.apk` (16.414.779 byte, SHA-256 `5a26523bc44c2c9babfb1fc737b33d0111b9b406328d47ab3dcf3a529f836a61`)

---

## I. Kepatuhan Batas Akses & Keamanan Perangkat
Sesuai instruksi khusus pemilik:
> *"Saya memilih ponsel Android saya untuk pengujian, tetapi minta izin per tindakan sebelum menggunakan ADB, memasang APK, atau melakukan tindakan lain pada perangkat. Siapkan skenario offline terlebih dahulu. Jika akses belum diizinkan, tandai bagian tersebut belum diuji dan lanjutkan pekerjaan independen."*

Agen Utama **TIDAK menjalankan perintah ADB atau menginstal APK ke perangkat fisik secara otomatis** tanpa instruksi eksplisit per tindakan dari pemilik. Status pengujian perangkat fisik tetap dicatat secara jujur sebagai `BELUM_DIUJI`.

---

## II. Skenario Pengujian Android yang Telah Disiapkan

1. **Skenario Pemasangan & Pembaruan Tanpa Hapus Progres:**
   - Package ID: `com.tradingglobal3d.mobile`
   - Kunci Tanda Tangan: `Android Debug Key`
   - Prosedur: Pemasangan pembaruan langsung (*in-place update*) via File Manager ponsel untuk menjaga direktori data `/data/data/com.tradingglobal3d.mobile/files/` tetap utuh.
2. **Skenario Gangguan Siklus Hidup (Lifecycle Interruption):**
   - Menekan tombol Home saat transaksi order berlangsung.
   - Panggilan masuk / beralih ke aplikasi lain (backgrounding) untuk memverifikasi autosave via `onAppLifecycleChanged`.
   - Membuka kembali aplikasi setelah 15 menit untuk memvalidasi rekonsiliasi waktu offline 24×.
3. **Skenario Stabilitas Input Layar Sentuh:**
   - Mengetuk cepat tombol tab navigasi bawah secara berulang (Beranda -> Pasar -> Portofolio -> Bisnis -> Lainnya).
   - Membuka modal profil NPC 4 tab dan memeriksa fluiditas scroll 1.000 peserta.
4. **Skenario Izin Notifikasi Sistem (Android 13+):**
   - Penanganan penolakan izin `POST_NOTIFICATIONS` agar tidak memicu crash, dan verifikasi ketersediaan 5 channel notifikasi sistem.

---

## III. Kesimpulan Kesiapan M7
Skenario pengujian offline dan prosedur teknis telah selesai disiapkan. Pelaksanaan pengujian pada ponsel fisik Android menunggu instruksi dan izin eksplisit per tindakan dari pemilik.
