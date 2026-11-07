# Eksekusi disetujui pemilik

Persetujuan chat: “eksekusi semua task ... AKU SUDAH SETUJU SEMUA NYA”. Ini plan aktif lingkup anotasi, seluruh preview dan keputusan lanjutannya. Tidak membatasi pekerjaan menjadi satu subset.

## Requirement dan bukti yang diperlukan

| ID | Hasil wajib | Bukti penyerahan |
|---|---|---|
| E01 | IDX/US 24 jam, 48×, cutoff harian 00.00, tanggal lengkap konsisten | Uji seluruh menit kedua bursa, crossing midnight, jalur transaksi/news/settlement/offline dan UI |
| E02 | Pergerakan harga/fraksi sah setiap penutupan saham aktif, volume ribuan–puluhan ribu sesuai iklim | Simulasi multi-hari emiten, fill/OHLC/volume/dana NPC/catch-up/heap konsisten; halt dikecualikan |
| E03 | Semua daftar saham sesuai referensi, logo, sparkline dari 00.00, harga/perubahan, saham terkunci | Domain data nyata, IDX/US/pantauan/anggota indeks, UI responsif/keyboard/teks besar |
| E04 | Chart pilih jam/harga, detail opsional, pan/zoom/pinch/fullscreen/latest; hilangkan slider visual | Browser perilaku pada history nyata, akses keyboard, layar sentuh perlu bukti terpisah |
| E05 | IPO daftar ringkas dan detail timeline semua status; tindakan pantau/pesan/hasil | State upcoming/ongoing/past, order/alokasi/refund nyata dan navigasi |
| E06 | Riset satu tempat di Keuangan, perpanjangan, hitungan saham jelas, katalis hanya saham terbuka | Uji jumlah nol/level berubah, tier/expiry/renew, offer/open news/access; UI tanpa usulan yang ditolak |
| E07 | Firma AUM → klien → kinerja/fee; siklus penuh dan simpan/muat | Mendirikan/akses, mandat, transaksi, fee, progres, recovery dan isolasi uang klien |
| E08 | Pasar → indeks → hanya anggota dengan bentuk baris sama | Membership domain dan navigasi detail saham |
| E09 | Leverage maksimum sementara 3x; jaminan 30%; berdasarkan saham+kelayakan; 3x total buying power | Validasi pinjaman, pembelian, limit bersama caller, ledger/save/UI |
| E10 | Bunga 0,02%/hari digunakan, kapitalisasi bila kas kurang, 120 menit nyata margin call sebelum likuidasi | Daily idempotence/restart/offline/clock-backward/partial cash/repayment, deadline persisten dan likuidasi |
| E11 | Build APK dan pemeriksaan akhir perubahan | Regresi relevan, build/sumber cocok, host/browser/APK terpisah; tidak instalasi perangkat tanpa izin per tindakan |

Baseline checkout kotor dipertahankan, branch codex/classic-ui-new-assets. Save pemain hash-only baseline 59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED. Semua eksekusi memakai data QA baru. Tidak reset/stash/clean/push/publish/agen lain.

Status awal: seluruh E01–E11 IN_PROGRESS atau PENDING. Preview bukan bukti implementasi. Mulai dari clock/tarif bersama, lanjut cutoff ekonomi dan cakupan lain; tidak menyatakan selesai sebelum semua requirement diverifikasi.

- Berita US — ditunda atas arahan pemilik. Perlu cakupan Pro/Whale bagi emiten US yang dapat ditransaksikan dan target harga dengan ID bursa serta emiten, misalnya US:AAPL.
