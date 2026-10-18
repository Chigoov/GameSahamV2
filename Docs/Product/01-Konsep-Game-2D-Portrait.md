# Konsep Game Saham 2D Portrait

Tanggal: 18 Oktober 2026. Acuan detail: [PRD lengkap](02-PRD-Game-Saham-2D-Portrait.md).

Game Android tentang trading dan perjalanan karier menjadi fund manager. Kedua pengalaman sama penting. Pemain memulai dari akun pribadi, mempelajari pasar, membangun reputasi, menjadi analis, menerima mandat klien, lalu mengelola investasi dengan tanggung jawab risiko yang meningkat.

## Keputusan pemilik

1. Semua kelompok fitur game sebelumnya wajib hadir dalam rilis pertama. Urutan pengerjaan bukan izin menunda fitur wajib ke v2.
2. Perusahaan nyata digunakan dalam pasar internal game. Replay data historis nyata juga wajib tersedia.
3. Tampilan 2D portrait. Ruang, kamera, meja, dan nuansa 3D dihapus; tidak memakai Unity atau Three.js.
4. Fokus pada pengalaman pengguna ponsel dan beban aplikasi yang lebih ringan.
5. Hasil akhir berupa APK Android. Kriteria perangkat penerimaan dan batas performa tetap terbuka dalam PRD.

Pengguna telah menyatakan trading dan karier sama penting. Sasaran penggunaan pribadi/publik dan tingkat pengetahuan pemain belum diputuskan.

## Putaran permainan

Briefing dan agenda -> memilih peluang -> membaca harga, antrean, berita, serta risiko -> meninjau dan mengirim order -> mengelola posisi dan kewajiban -> menutup hari -> membaca laporan -> menyelesaikan tugas dan melanjutkan karier.

Pada tahap fund manager, putaran ini mencakup mandat, akun klien terpisah, kepercayaan, kebutuhan likuiditas, laporan, serta pendapatan manajer.

## Cakupan yang dipertahankan

- Saham Indonesia dan AS, indeks, katalog, pencarian, watchlist, grafik, antrean, serta riwayat transaksi.
- Market dan limit order, partial fill, pembatalan, stop-loss, take-profit, biaya, slippage, dan dampak likuiditas.
- Dompet IDR/USD, FX, portofolio, valuasi, laba/rugi, serta ledger.
- NPC investor dan analis, berita makro/emiten, riset, kalender, data alternatif, serta paket terminal dengan uang game.
- Tutorial, misi, imbalan, reputasi, promosi, dan jalur pemulihan setelah rugi.
- Klien, mandat, firma, fund builder, arus dana, management/performance fee, dan high-water mark.
- Saham, obligasi, komoditas, dan FX dengan definisi instrumen yang jelas.
- Dividen, split/reverse split, rights issue, buyback, IPO, M&A, serta voting.
- Risiko, kepatuhan, halt/lelang pembukaan kembali, margin, pinjaman saham, short selling, likuidasi, dan short squeeze.
- Stress test, lab strategi, replay keputusan, replay historis, save/load, backup, serta migrasi.

Cakupan 26 layar konsep dapat digabung menjadi halaman, tab, dan dialog portrait. Tidak perlu membuat 26 halaman mandiri.

## Pengalaman portrait

Usulan navigasi bawah: Beranda, Pasar, Aset, Firma, Riset. Daftar saham membuka halaman detail; grafik, antrean, dan berita ditampilkan bertahap. Tiket order memperlihatkan akun, mata uang, unit, biaya, dan risiko sebelum konfirmasi. Tidak memadatkan dashboard landscape ke layar vertikal.

Target sentuh setara minimal 48 dp, pembesaran teks, safe area, keyboard, dan Android Back harus diuji. Warna status selalu disertai teks atau ikon.

## Realisme yang harus dijaga

Satu pasar internal dipakai pemain, NPC, dan klien. Order tanpa fill tidak mengubah harga transaksi terakhir. Uang pribadi tidak tercampur AUM. Biaya dan perubahan saldo tercatat. Market impact tidak dihitung dua kali. Peristiwa, prediksi, dan laporan tidak membocorkan masa depan. Lab tidak mengubah permainan utama.

Replay historis bukan label untuk data buatan. Dataset harus menyebut sumber, periode, resolusi, gap, dan batas model eksekusi. Pilihan apakah jalur harga tetap historis atau bercabang karena keputusan pemain masih OQ-03; jangan memutuskannya diam-diam.

## Batas produk dan teknis

Seluruh uang adalah uang game. Baseline mengikuti dokumen lama: offline setelah data tersedia, tanpa broker, uang nyata, multiplayer, layanan AI berbayar wajib, atau publikasi toko.

Repo ini disiapkan dari kosong untuk Hermes. Belum ada source game, dataset historis, tests game, atau APK di dalamnya. Baseline teknologi dalam PRD adalah C# + Android MAUI HybridWebView + HTML/CSS/JS lokal; Hermes harus memverifikasi kelayakan dan toolchain sebelum menetapkannya. Satu pemilik domain ekonomi tetap wajib.

Repo lama berada di C:/Users/HYPE AMD/Downloads/VIBE CODING/Game Trading Saham. Dokumennya menjadi referensi konsep, bukan bukti fitur di repo baru sudah berfungsi. Jangan mengubah repo lama atau menyalin source, build, credentials, dan save secara otomatis.

## Dokumen rujukan

- [PRD lengkap](02-PRD-Game-Saham-2D-Portrait.md)
- [Rencana kerja Hermes](../../Work/README.md)
- [Keputusan terbuka](../../Work/DECISIONS.md)
- [Prompt eksekusi](../../Prompts/Hermes-Mulai-Game-Saham-2D.md)

Konsep awal dirangkum dari Docs/Concept/Game-Concept.md serta Docs/Tasks/Hermes-No-Unity-Session-Plan-2026-10-10.md di repo lama, lalu diselaraskan dengan keputusan pemilik 18 Oktober 2026. Kewajiban landscape/3D dan sasaran hardware 3D lama tidak berlaku untuk produk ini.
