# File plan untuk dikirim satu per satu ke Hermes

Pemilik mengirim SATU file plan.md. Hermes menuntaskan file itu, menyerahkan hasil, lalu BERHENTI. Pemilik baru mengirim file berikut setelah meninjau penyelesaian sebelumnya. Pembuatan folder ini tidak menjalankan agent.

Target akhir tetap 20 paket / 126 subtask. Status saat penyusunan: 2 selesai, 54 parsial, 70 pending. Task belum tuntas tetap belum selesai meski ada tes parsial. Dua PASS N20-02/N20-03 tercatat pada file 08.

| Kirim ke- | File | Fokus | Subtask | Hasil wajib |
|---|---|---|---:|---|
| 01 | [plan.md](01-waktu-dan-berita/plan.md) | Waktu otomatis dan berita | 13 | APK-01 + laporan/bukti |
| 02 | [plan.md](02-pasar-dan-grafik/plan.md) | Pasar, detail saham dan grafik | 22 | Checkpoint + laporan/bukti |
| 03 | [plan.md](03-order-dan-notifikasi/plan.md) | Order, receipt dan notifikasi | 19 | APK-02 + laporan/bukti |
| 04 | [plan.md](04-portofolio-dan-kepemilikan/plan.md) | Portofolio dan kepemilikan | 27 | Checkpoint + laporan/bukti |
| 05 | [plan.md](05-bisnis-dan-dana/plan.md) | Bisnis, koleksi dan dana kelolaan | 7 | Checkpoint + laporan/bukti |
| 06 | [plan.md](06-lainnya-dan-riset/plan.md) | Lainnya, progres dan riset | 12 | APK-03 + laporan/bukti |
| 07 | [plan.md](07-audit-ui-dan-pembersihan/plan.md) | Audit seluruh UI dan pembersihan | 20 | Checkpoint + laporan/bukti |
| 08 | [plan.md](08-integrasi-dan-apk-final/plan.md) | Integrasi dan APK final | 6 | APK-04 + laporan/bukti |

## Cara mengirim

1. Kirim file 01 saja: lampirkan file atau tunjuk path-nya kepada Hermes.
2. Tunggu semua ID dan deliverable file itu tuntas. Tes sebagian lulus atau APK ada belum memenuhi seluruh acceptance.
3. Tinjau laporan/bukti. Bila belum tuntas, lanjutkan file yang sama; jangan mengirim file berikut untuk melompati pekerjaan.
4. Setelah file benar-benar selesai, kirim file selanjutnya dari tabel. Jangan kirim semua delapan file sekaligus.

Setiap file membawa scope, kontrak aktif, ID/status awal, konteks parsial, rincian acceptance, pemeriksaan dan instruksi berhenti. Status awal adalah snapshot 24 Oktober 2026; agent membaca tracker/source terbaru saat menerima file.

Master spesifikasi tetap master plan perbaikan.md di root produksi. Tracker tetap ../TRACKER.csv; laporan ../STATUS-REPORT.md dan checkpoint ../CHECKPOINT.md. File plan adalah potongan pelaksanaan yang dikirim manual, dengan seluruh requirement asli tetap.

APK bertahap: file 01 → APK-01; file 03 → APK-02; file 06 → APK-03; file 08 → APK-04 final. Hermes tetap berhenti setelah SETIAP file, termasuk file yang tidak membangun APK.
