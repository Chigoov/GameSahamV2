# Gerbang penyerahan yang dapat dijalankan ulang

Gunakan dari root repository yang ditunjuk AGENTS.md:

```powershell
python -m unittest discover -s Work/Perbaikan-Lanjutan-Performa-2026-11-03 -p test_quality_gate.py
python Work/Perbaikan-Lanjutan-Performa-2026-11-03/quality_gate.py run
python Work/Perbaikan-Lanjutan-Performa-2026-11-03/quality_gate.py check <path-gate.json>
python Work/Perbaikan-Lanjutan-Performa-2026-11-03/quality_gate.py check <path-gate.json> --claim full
```

Ganti `<path-gate.json>` dengan lokasi hasil yang dicetak perintah run. Setiap run memiliki folder unik; hasil lama dipertahankan. Jangan menjalankan dua compiler/runner yang memakai `logs/qa-classes` secara bersamaan.

`run` memakai `verify_ui.py` existing: kompilasi source terkini, suite Java terpilih dengan save QA, kemudian browser Chromium dengan aset produksi dan input bridge QA terkontrol. Tidak ada dependency baru, akses layanan eksternal, instalasi APK, ADB, atau pemakaian save pemain. Override dan hook QA hanya di browser tes, tidak ditulis ke aset produksi.

Status dan exit code:

| Hasil | Makna |
|---|---|
| 0 | Lingkup regresi domain/browser yang diperiksa lulus pada source dan runner tercatat |
| 1 | Ada kegagalan, bukti kurang/berubah, source drift, atau hash save berbeda |
| 2 | Permintaan klaim seluruh plan belum dapat diverifikasi oleh gate ini |

Gate saat ini memeriksa 27 kelompok assertion browser wajib (beberapa mencakup lebih dari satu ID T), termasuk keyboard/pointer slider, akun/restore, tanggal/bulan dan tabel statistik harian sesuai klarifikasi terakhir. Daftar authoritative ada pada `REQUIRED` di quality_gate.py. Suite domain, page errors, exit code, hash input sebelum/sesudah, hash artefak dan hash save pemain juga diperiksa. Ia tidak menerima matriks PASS buatan sebagai pengganti hasil eksekusi.

Kelulusan regresi tidak membuktikan benchmark T16, build/source APK terbaru, pilihan produk yang belum disahkan, atau pengujian perangkat T17. Gate ini sengaja menolak klaim penuh: buka `openGates` pada hasil dan tinjau bukti yang diperlukan secara terpisah. Jangan menghapus daftar gate terbuka demi membuat laporan hijau.

Hash bukan tanda tangan dan bukan perlindungan terhadap pelaksana yang sengaja memalsukan runner, tes, dan bukti sekaligus. Kontrol utamanya tetap peninjauan diff verifier dan eksekusi ulang oleh pemeriksa. Jangan menjanjikan agent tidak dapat berbuat salah hanya karena gate dipasang.

Negative controls pada `test_quality_gate.py` membuktikan penolakan kasus hilang/duplikat/gagal, page error, source drift, kegagalan runner, dan perubahan hash save. Tambahkan kasus regresi ketika ada akar masalah baru; jangan mengubah assertion agar implementasi yang salah diterima.
