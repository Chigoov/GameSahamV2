# Checkpoint 03 — IPO, daftar bersama, grafik mini

Goal E01–E11 tetap aktif; previous goal turn progress, bukan blocked. Root/branch terverifikasi seperti checkpoint02. Semua persetujuan preview tetap berlaku; pertanyaan jaminan4x masih pending, tidak mengulang izin UI.

## Implementasi

E05: openIpoOfferingDetail menjadi tujuan klik setiap kartu IPO. Timeline hanya ada di detail, memakai offeringStart/End, allotmentDate dan listingDate domain. Tidak membuat tanggal bookbuilding yang tidak ada. Upcoming Pantau, ongoing Pesan bila canSubscribe, past Lihat saham sesudah listing. Pesanan hanya ditampilkan bila benar-benar ada. Detail pesanan memakai timeline bersama dan refund nyata, menghapus fallback ID pesanan palsu dan nilai alokasi yang keliru memakai dana tertahan. Hero pemasaran IPO dibuang. Kartu sekarang button native; kategori pending allotment tetap ongoing, bukan upcoming palsu.

Pantauan IPO: CorporateActionService menyimpan set ID ke state, restore menolak ID asing/duplikat, reset membersihkan. Facade memvalidasi lalu persist; TradingWebBridge.toggleIpoWatch menyediakan tindakan yang dipakai UI. getIpoOfferings mengirim isWatched. OwnerAnnotationPolicyTest membuktikan toggle/save/load/unknown-ID; hasil9tes exit0 sebelum perubahan sparkline: Artifacts/Annotation-Execution/1793729978926528700/test.log.

E03/E08: pembuat createStockListItem dipakai loadStocks dan detail anggota indeks. Indeks membaca instrumen satu kali, filter berdasarkan instrumentId anggota domain, pencarian dan baris harga/perubahan yang sama. Metodologi tetap dilipat di bawah, bukan menghalangi daftar.

E03: susunan baris datar monogram, identitas, grafik mini, kuotasi rata kanan. Nominal perubahan ditampilkan bersama persen dan diperbarui pada live refresh. Pantauan di detail emiten tetap ada; bintang per baris dihapus sesuai referensi. Quote refresh juga mengunjungi daftar indeks. CSS container query menyusun ulang baris saat ruang/font tidak cukup. Logo resmi belum dibundel; monogram adalah fallback, bukan klaim match logo. Badge leverage belum dipalsukan saat aturan belum ditetapkan.

Grafik mini berasal dari getIntradaySparkline: hanya fill timestamp 00.00 WIB hari itu sampai timestamp game saat ini. Maks48bucket30menit + referensi previousClose pada awal hari; ambil fill terakhir per bucket. Tidak menambahkan future price/fixture atau melakukan permintaan bridge satu per baris. Jika tidak ada fill, array kosong dan tidak menggambar grafik fiktif. Test proyeksi memeriksa previous-day/future exclusion, bucket terbaru dan rentang waktu.

## Validasi dan kegagalan yang ditindaklanjuti

- node --check app.js exit0.
- check-chart-density.cjs exit0: kepadatan/konservasi data chart tetap lulus setelah refactor daftar.
- check-market-ipo-ui.cjs exit0: menjalankan fungsi produksi pada Node dengan adapter elemen minimal; Upcoming watch, ongoing form callback, past stock callback, sub detail callback, quote sign, flat sparkline. Ini kontrak kontrol, tidak membuktikan rendered layout/WebView.
- Regresi IPO pertama29tes/1gagal: Artifacts/Annotation-Execution/1793730038692438400/test.log. testAutomaticDividendAndIpoAllotmentOnDayClose masih skip16:01 via closeDayAndAdvance; diganti advanceWorldSecondsForTest sampai midnight. Assertion dividen/ledger/alokasi100%/posisi/refund/reconcile tetap ada; menghapus penggunaan skip yang bertentangan aturan24jam.
- Suite ulang aktif saat checkpoint: exec session39457, python run-policy-check.py IpoScheduleAndAllotmentTest IpoAllotment6040AndCancellationTest CorporateActionIpoVotingTest CorporateActionIpoVotingCriticalSequenceTest ChartHistoryRangeTest. Poll handle ini; jangan memulai duplikat karena timeout observasi.
- apply_patch style.css sesekali gagal tulis; append Python berhasil, apply_patch dengan anchor di akhir kemudian berhasil. Tidak ada cleanup/reset/overwrite paksa; CSS utama akhirnya tercatat dalam file existing.

## Belum tercapai / next action

Poll39457. Verifikasi visual browser masih unavailable dari Work settings; tidak mencoba bypass. E03 perlu logo/badge serta validasi layout; E04 interaksi chart produksi belum lengkap; E02 volume/pergerakan aktif+offline belum diimplementasi; E06 Riset tunggal UI belum selesai; E07 siklus firma/UI belum selesai; E09/E10 collateral dan deadline persisten belum; E11 buildAPK/gate akhir belum. Tidak menandai goal complete dari tes ini.

## Hasil akhir

Session39457 terminal exit0: 39tes lulus, JUnit96,493detik. Bukti Artifacts/Annotation-Execution/1793730245545102500/test.log. Mencakup10tes kebijakan anotasi (termasuk pantauanIPO persisten dan sparkline observed-only) serta lima suite IPO/corporate/chart yang disebut di atas. Source Java tidak berubah selama run; perubahan JS/CSS sesudah start hanya tercakup pemeriksaan Node, bukan bukti visual. next_action langsung lanjut requirement pending, tidak perlu polling39457 lagi.
