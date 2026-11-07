# Rencana optimasi performa Game Saham pada Poco X8 Pro

Tanggal: 3 November 2026, Asia/Jakarta.

Status: DRAF SIAP DITINJAU, BELUM DITUNJUK UNTUK EKSEKUSI.

Dokumen ini menyiapkan pekerjaan performa setelah checkpoint perbaikan aktif. Pembuatan file tidak mengganti plan aktif, menyetujui perubahan UI, atau mengizinkan tindakan perangkat. Belum ada hasil pengukuran performa perangkat dalam dokumen ini.

## 1. Tujuan dan perangkat sasaran

Membuat scroll, navigasi, grafik, order book, dan respons input terasa lancar pada Poco X8 Pro, sambil mempertahankan kebenaran transaksi dan progres. Keberhasilan harus dibuktikan pada APK dan skenario yang tercatat; bukan jaminan untuk semua perangkat atau semua kondisi.

- Model sasaran diberikan pemilik: Poco X8 Pro.
- Varian RAM, versi Android, versi WebView, refresh rate aktif, dan mode daya: BELUM DICATAT. Ambil saat pengujian diizinkan; jangan menebak spesifikasi.
- Root: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`.
- Branch saat penyusunan: `codex/classic-ui-new-assets`; periksa ulang saat mulai.
- Produksi tetap Java + Android WebView + HTML/CSS/JS lokal.
- Rasa pasar hidup berasal dari data domain yang konsisten. Jangan membuat harga, candle, volume, atau fill rekaan untuk menyamarkan data yang tidak diperbarui.

## 2. Ketergantungan dan batas kerja

1. Baca AGENTS.md, keputusan produk aktif, DESIGN.md jika tampilan tersentuh, dan handoff perbaikan terbaru. Pemilik menunjuk satu plan aktif; mulai implementasi hanya setelah penunjukan dan checkpoint pelaksana sebelumnya jelas.
2. Verifikasi hasil R01–R07 pada `Work/Perbaikan-Lanjutan-QC-AT-2026-11-03/`, serta status perbaikan kompatibilitas loader Android dan indeks (R08/R09 jika sudah dimasukkan). Klaim laporan bukan bukti source/runtime.
3. NV01/R04 masih memerlukan bukti lifecycle Android menurut plan yang dibaca saat penyusunan. Jangan menyamakan pengujian performa dengan penutupan risiko kehilangan progres. Jika blocker tidak menghalangi pengukuran independen, catat batasnya dan lanjutkan bagian yang sah.
4. Pertahankan perubahan lokal. Tidak reset/clean/stash, penghapusan massal, peluncuran agent paralel, pesan lintas chat, push/publish, atau perubahan global.
5. Tidak ADB, instalasi, profiling atau tindakan emulator/perangkat tanpa izin yang berlaku untuk tindakan itu. Penunjukan plan ini kelak tidak otomatis menjadi izin perangkat.
6. Tidak dependency/framework/engine baru. Gunakan pengukuran dan helper existing; tambahkan hanya instrumentasi minimum yang dibutuhkan untuk membedakan sebab.
7. Pertahankan 48×, sesi bursa, AON, anti-self-trade, ledger, save/backup, dan kewajiban ekonomi saat offline. Jangan memangkas simulasi atau mengubah balancing demi angka performa.
8. Optimasi internal yang mempertahankan tampilan dan perilaku dapat dikerjakan dalam lingkup penunjukan. Perubahan tampilan, teks, navigasi, frekuensi yang mengubah pengalaman produk, atau alur memerlukan preview dan persetujuan sesuai AGENTS.md. Lanjutkan pekerjaan independen saat menunggu.
9. Save pemain `Artifacts/LocalPreview/preview-save/save_game_v2.json` hanya boleh di-hash. Jangan membaca, menyalin, memuat, atau memigrasikannya. Referensi SHA-256: `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`. Gunakan save QA baru terisolasi; perubahan hash tidak mengizinkan pemulihan paksa.

## 3. Sasaran awal dan cara mengukurnya

Angka berikut adalah target evaluasi, belum hasil atau janji kelulusan. Catat metodologi dan kesesuaian alat untuk WebView. Jangan menganggap frame JavaScript sama dengan seluruh frame yang benar-benar tampil di layar.

| Bagian | Sasaran awal | Bukti yang diperlukan |
|---|---|---|
| Interaksi berkelanjutan pada mode 60 Hz | Target 60 FPS; sekurangnya 95% interval frame valid dalam anggaran sekitar 16,7 ms | Distribusi waktu frame, proporsi jank, p95/p99 dan jeda terburuk pada skenario yang sama; pisahkan startup |
| Tombol dan input | Umpan balik visual p95 <=100 ms | Waktu input sampai respons visual; pisahkan acknowledgement dari keberhasilan settlement |
| Harga/order book | Target siklus yang saat ini sekitar 1 detik tetap konsisten | Latensi dari snapshot domain siap sampai tampilan selesai, jumlah render per snapshot, serta urutan versi data; bukan hanya interval timer |
| Grafik | Tidak berkedip, tidak kehilangan zoom/posisi geser saat pembaruan | Skenario data baru ketika zoom, pan, dan scroll; bukti sebelum/sesudah |
| Jeda berat | Tidak ada frame beku >700 ms pada interaksi berkelanjutan yang diuji | Trace dan daftar jeda terburuk; startup dan rekonsiliasi dilaporkan terpisah |
| Memori | Heap Java memenuhi batas proyek <384 MB pada skenario relevan, tanpa pertumbuhan tak terbatas | Heap Java, memori proses/WebView dan PSS jika tersedia dicatat terpisah; jangan menyamakan metrik |
| Sesi panjang | Sesi 60 menit selesai tanpa crash/ANR atau penurunan yang tidak dijelaskan | Performa awal/tengah/akhir, memori, kondisi suhu/daya yang tersedia, dan error log |
| Ketepatan domain | Semua invariant transaksi/save yang terdampak tetap lulus | Regression checks dan perbandingan state QA; FPS tidak menggantikan bukti ekonomi |

Panduan frame dan jank: https://developer.android.com/topic/performance/issues/render

## 4. Tahapan pekerjaan

| ID | Pekerjaan dan hasil wajib | Status |
|---|---|---|
| PF00 | Checkpoint, source/APK baseline, status perbaikan, persiapan QA dan izin yang tersedia | DIPERBAIKI_DAN_DIUJI_HOST |
| PF01 | Ukur baseline pada skenario tetap; petakan biaya Java, bridge, parsing, DOM/layout dan chart | DIPERBAIKI_DAN_DIUJI_HOST |
| PF02 | Hilangkan pekerjaan ganda yang terbukti pada snapshot dan render | DIPERBAIKI_DAN_DIUJI_HOST |
| PF03 | Optimalkan pembaruan grafik, order book, dan daftar yang terlihat berdasarkan hasil ukur | DIPERBAIKI_DAN_DIUJI_HOST |
| PF04 | Periksa sinkronisasi, autosave, background/resume dan catch-up agar tidak menahan input | DIPERBAIKI_DAN_DIUJI_HOST |
| PF05 | Regresi, build, perbandingan perangkat, sesi 60 menit, dan penyerahan | NEEDS_VALIDATION (Host: DIPERBAIKI_DAN_DIUJI_HOST; Device Hardware Test on Poco X8 Pro: OPEN / PENDING by policy) |

### PF00: checkpoint yang dapat dibandingkan

- Catat root, branch, HEAD dan dirty state; commit saja tidak mewakili source lokal.
- Rekam identitas source yang diuji, hash APK, jenis build, versi toolchain, dan file berubah dalam lingkup. Jangan menyalin data pemain.
- Siapkan save QA sintetis berlabel dan seed/input yang dapat diulang. Catat jumlah instrumen, order aktif, posisi, panjang histori, dan durasi offline pada setiap skenario.
- Pisahkan baseline build debug dari build lain. Perbandingan harus memakai konfigurasi setara; jangan mengklaim performa release dari APK debug.
- Tentukan alat lokal yang tersedia tanpa memasang dependency baru. Jika pengukuran perangkat belum diizinkan, kerjakan pemetaan source/host yang sah dan biarkan gate perangkat terbuka.

### PF01: baseline dan kandidat hambatan

Kandidat dari penelusuran source saat penyusunan, BUKAN bottleneck yang sudah terukur:

1. `assets/www/app.js`: `requestClockSync.finish()` memanggil `refreshSnapshot(snap)`, sedangkan sejumlah callback sesudahnya memanggil fungsi yang sama lagi. Ukur apakah snapshot yang sama dirender dua kali dan apa efek samping tiap caller sebelum menghapus panggilan.
2. `updateInstrumentDetailLive`: pembaruan candle langsung diikuti pemanggilan `flowPanel.refreshChart()`. Ukur apakah terjadi penggambaran ulang berlebihan dan pastikan sumbu/rentang tetap benar.
3. Timer sekitar satu detik memicu sinkronisasi, snapshot dan beberapa panel. Ukur tumpukan kerja, jumlah panggilan bridge, parsing, layout dan durasi render per siklus.
4. Penundaan render saat scroll: periksa penerapan data terbaru setelah scroll berakhir agar tidak melompat atau tertinggal.
5. `TradingFacade.getSnapshotJson`, bridge dan worker: ukur ukuran payload dan waktu tunggu lock/sinkronisasi. Jangan menghapus sinkronisasi domain hanya untuk mengurangi waktu tunggu.

Mulai dari tiga pengulangan skenario pendek yang setara. Catat variasi dan outlier, lalu prioritaskan biaya terbesar yang dapat direproduksi. Hindari logging setiap frame secara permanen yang justru menambah beban.

### PF02–PF04: optimasi minimum

- Telusuri semua caller fungsi yang diubah. Reuse state/helper existing; tidak membuat lapisan cache atau event bus baru sebelum kebutuhan terukur.
- Render hanya nilai/komponen yang perlu diperbarui dan batasi pekerjaan panel yang tidak terlihat, sambil mempertahankan simulasi ekonomi.
- Jika menggabungkan pembaruan, jaga urutan snapshot. Respons lama tidak boleh menimpa state baru; receipt dan hasil transaksi tidak boleh dibuang sebagai pembaruan visual biasa.
- Pertahankan zoom, pan, posisi scroll, fokus input, aksesibilitas dan pilihan akun saat data berubah.
- Jangan menjalankan kerja domain atau penulisan save per frame. Jangan pula menunda save wajib atau menghapus backup demi kelancaran.
- Tambahkan satu runnable regression check minimum per akar masalah nontrivial. Perubahan performa harus mempunyai pengukuran sebelum/sesudah pada skenario yang sama.
- Jika kandidat tidak terbukti memberi dampak, catat hasil dan hindari refactor spekulatif.

## 5. Skenario verifikasi

1. Buka aplikasi dari keadaan tertutup dan muat save QA; pisahkan cold start dari warm resume.
2. Daftar saham: scroll berulang sambil kuotasi diperbarui, cari saham, lalu berpindah tab.
3. Detail saham: candle baru, order book berubah, zoom/pan, ubah timeframe, buka/tutup detail berulang.
4. Trading: preview dan konfirmasi order, AON likuiditas kurang/cukup, order antre lalu terisi, cancel/retry, perpindahan akun pribadi/klien. Data dan receipt harus tetap benar.
5. Portofolio: pembaruan uang, reservasi, posisi dan kewajiban ketika panel lain juga aktif.
6. Background/resume dan offline QA: jeda pendek, lintas sesi/hari, lalu catch-up besar yang relevan. Ukur respons dan durasi selesai secara terpisah; periksa settlement dan cutoff.
7. Teks besar 200% dan mode aksesibilitas yang relevan tidak boleh rusak setelah optimasi.
8. Jalankan 10x100 aksi pada alur kritis yang berlaku menurut standar proyek, dengan reset QA terkontrol dan request ID sah. Catat jenis aksi; jangan menganggap loop artifisial membuktikan semua alur.
9. Sesi 60 menit pada perangkat sasaran ketika diizinkan. Bandingkan awal/tengah/akhir dan ulangi skenario yang sebelumnya tersendat.

## 6. Bukti dan penyerahan

Saat plan ditunjuk untuk eksekusi, simpan di folder ini:

- `baseline.md`: source/APK, perangkat, skenario, alat, metode dan hasil awal.
- `LAPORAN-HASIL.md`: tabel sebelum/sesudah, perubahan per PF, hasil regresi dan batas bukti.
- `logs/`: log mentah perintah, exit code, hasil pengujian/build dan trace minimum yang relevan tanpa data sensitif.

Untuk tiap PF gunakan status PENDING, IN_PROGRESS, DIPERBAIKI_DAN_DIUJI_HOST, DIVERIFIKASI_BROWSER, DIVERIFIKASI_ANDROID, NEEDS_VALIDATION atau MENUNGGU_KEPUTUSAN sesuai bukti. Buat catatan jika kandidat ditolak dengan hasil ukur.

Gate penyerahan: diff dalam lingkup, check relevan lulus, hash save pemain diperiksa, konfigurasi pengukuran sebanding, hasil perangkat dilaporkan apa adanya. APK atau rata-rata FPS saja tidak menutup gate. Penilaian akhir hanya berlaku untuk APK, perangkat, kondisi dan skenario yang tercatat; jangan menulis jaminan lancar 100%.

Next action: pemilik/pelaksana menetapkan checkpoint perbaikan sebelumnya dan menunjuk plan ini. Mulai PF00 setelah itu. Status pengukuran saat dokumen dibuat: BELUM DIJALANKAN.
