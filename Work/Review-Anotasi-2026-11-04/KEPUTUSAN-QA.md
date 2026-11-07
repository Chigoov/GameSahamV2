# Jawaban pemilik — 20 pertanyaan anotasi

Diterima 4 November 2026 melalui balasan formulir chat. Ini keputusan kebutuhan, bukan persetujuan semua desain prototype. Berlaku lebih baru daripada aturan sesi bursa historis yang bertentangan.

| No. | Keputusan / status |
|---|---|
| 1 | IDX dan US bertransaksi 24 jam setiap hari game. |
| 2 | Pergantian hari/acuan harian 00.00 WIB game; perdagangan tidak berhenti. Perlu telusuri dampak terhadap berita, referensi harga dan laporan. |
| 3 | Beranda konsisten: Hari 1 · 19 Oktober 2026. |
| 4 | Penutupan setiap emiten wajib berbeda minimal satu fraksi harga dari penutupan sebelumnya. Implementasi harus mempertahankan fraksi harga, halt, konsistensi transaksi dan aturan berita; bila bertabrakan, munculkan kasus konkret untuk keputusan, jangan mengubah diam-diam. |
| 5 | Ribuan sampai puluhan ribu lot transaksi per emiten per hari game. |
| 6 | Volume harus besar dan disesuaikan jenis iklim pasar. Ini interpretasi kata “iklimna”; tidak menetapkan angka per iklim atau melonggarkan AON. |
| 7 | Pemilik akan mengirim video/foto referensi chart. MENUNGGU_REFERENSI. |
| 8 | Chart mendukung pilih waktu, geser histori, cubit zoom, kembali ke terbaru, dan layar penuh. |
| 9 | Tampilkan preview pengganti slider dahulu; belum memilih bentuk final. |
| 10 | Bacaan utama jam dan harga; OHLC/volume tersedia saat detail dibuka. |
| 11 | Pemilik akan mengirim referensi IPO. MENUNGGU_REFERENSI. |
| 12 | Upcoming pantau; Ongoing pesan bila dibuka; Past hasil/alokasi atau saham setelah listing. |
| 13 | Jelaskan fungsi Firma Investasi dahulu. Belum menetapkan hierarki UI final. |
| 14 | Buktikan mendirikan firma → mendapat klien → kelola dana → fee → naik tahap, termasuk simpan/muat. |
| 15 | Meminta usulan manfaat riset lain. Belum memilih manfaat baru. |
| 16 | Pertanyaan apakah 33 adalah jumlah saham terbuka yang menyesuaikan level. Jawaban source: bukan demikian; itu total gapRate positif, dengan filter isTradable tetapi tanpa bukti filter level di loop. Preferensi bersyarat belum menjadi izin mengubah makna jumlah. Perlu membedakan total saham terbuka dan saham diskon di antaranya. |
| 17 | Periksa syarat level ketika berita ditawarkan dan dibuka; jangan menjual manfaat berita untuk saham terkunci. |
| 18 | Semua riset/paket/langganan di menu Riset pada tab Keuangan. Mengungguli lokasi usulan sebelumnya untuk scope ini. |
| 19 | Detail indeks memuat semua saham di bursanya, disertai filter anggota indeks. |
| 20 | Urutan Chart & pasar → IPO → Riset → Firma → Indeks; persetujuan preview tiap bagian sebelum implementasi. |

## Preview revisi chart

Prototype terpisah memakai jam+harga, detail candle opsional, mode baca/geser histori, zoom tombol, gesture pinch yang perlu validasi sentuhan, kembali terbaru dan perluasan chart di dalam panel browser. Fixture 30 hari, 15 menit per candle, 24 jam. Ini demo UI; tidak membuktikan harga/volume domain atau gerakan persis referensi yang belum diterima.

Usulan manfaat riset baru (belum disetujui): peta likuiditas/spread lintas emiten, rotasi sektor dan market breadth, simulasi risiko atas rencana transaksi, serta riwayat akurasi kabar yang sudah selesai. Transparansi biaya dan penolakan order dasar tetap fitur semua pemain; jangan memindahkannya ke paywall.

## Pemeriksaan revisi 2

- Browser memperlihatkan 96 candle/24 jam. Tombol zoom mengubahnya menjadi 48 candle/12 jam dan tetap memilih 23.45 WIB setelah koreksi anchor zoom.
- Tombol candle sebelumnya mengubah waktu dan detail harga; detail OHLC/volume dapat dibuka.
- Layar penuh terbuka; geser pointer ke kanan mengubah jendela 12.00–23.45 menjadi 09.30–21.15 WIB.
- Screenshot: Artifacts/Preview-Anotasi-2026-11-04/chart-revisi-2.png. Tab revisi 2 dipertahankan, tab anotasi sebelumnya tidak dimuat ulang setelah ditemukan komentar belum tersimpan.
- Pinch dua jari belum diverifikasi di perangkat/sentuhan. Semua bukti adalah prototype browser dengan data sintetis, bukan domain/APK.
- Catatan ANALISIS.md bagian validasi pertama merujuk versi sebelum revisi 2; batas “belum ada pan” dan rentang label-only di sana sudah digantikan untuk prototype ini.
