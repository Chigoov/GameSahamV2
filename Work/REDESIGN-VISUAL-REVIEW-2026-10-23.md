# Review visual independen redesain

Status catatan awal: **PARTIAL**. Addendum final di bawah memperbarui bukti sumber stabil; hasil awal tetap dipertahankan sebagai riwayat. Reviewer awal menulis CSS dan meninjau hasil integrasi secara terpisah dari writer HTML/JS. Tanggal konteks pengguna: 23 Oktober 2026.

## Cakupan dan bukti

- Host Java terisolasi `http://127.0.0.1:8897`, save `qa-save-after`. Tidak memanggil reset, tick waktu, klaim, submit order, atau mutasi keuangan. Navigasi root dan fokus keyboard menggunakan UI nyata. Polling waktu/save otomatis aplikasi tetap berjalan.
- Lima tujuan ter-render: Ringkasan, Pasar, Aset, Karier, serta Berita dan riset sebagai tujuan sekunder Pasar. Jumlah tab utama tetap empat.
- Delapan viewport: 320×568, 360×640, 360×800, 390×844, 412×915, 430×932, 768×1024, 1440×900. Font root benar-benar diubah ke 100%/200% di browser, bukan screenshot diperbesar.
- `Artifacts/Redesign-Antislop-2026-10-23/visual-review/review_roots.py`: pemeriksaan read-only yang dapat dijalankan ulang. Hasil di `roots-review.json`, 80 kombinasi, 160 screenshot viewport atas/bawah. Screenshot bawah menggunakan scroll container aplikasi tanpa mengubah ukuran layout.
- Enam belas contact sheet root dibuka melalui image viewer: `roots-sheet-{viewport}-{font}.jpg`. Ini pemeriksaan komposisi atas/bawah semua 80 kombinasi, bukan bukti seluruh bagian tengah setiap layar atau seluruh state.
- `root-keyboard.json`: Tab aktual pada lima tujuan × 320/390 × 100/200. Semua target yang tampak pada root Pasar/Karier/Riset tercapai dengan outline solid 3px. Daftar target awal Ringkasan/Aset ikut menghitung anak details tertutup; 3/12 target yang tidak tercapai adalah konten details tertutup, bukan kontrol tampak yang dilewati. Pemeriksaan details terbuka dan Enter/Escape seluruh flow masih pending.
- Hash served dan local direkam. Perbedaan index host telah ditelusuri: host menyisipkan hanya `<script src="/browser-bridge.js"></script>` sebelum penutup head.

## Hasil sementara

80 kombinasi root terakhir tidak menemukan horizontal overflow, teks terlihat yang diklip, touch target di bawah 48px, teks kontras di bawah AA, atau batas kontrol yang diperiksa di bawah 3:1. Nav mencapai bawah viewport dan dapat disentuh saat konten digulir sampai akhir. Pengukuran menyertakan seluruh konten root ter-render, bukan hanya bagian yang tampak pada screenshot atas. Elemen `.sr-only` memang disembunyikan untuk pembaca layar dan dikecualikan dari kegagalan clipping.

**Bukti belum final:** source JS masih berubah selama capture terakhir (`sameSourceThroughout: false`). Semua angka di atas adalah hasil scope root pada respons yang dimuat saat itu, bukan kelulusan source akhir. Capture harus diulang setelah coordinator menyatakan source stabil; contact sheet yang sudah dibuka berasal capture sebelumnya dan perlu disegarkan setelah perbaikan berikut.

## Temuan dan perbaikan

1. **Chrome teks 200% menyingkirkan nav dari viewport.** Diperbaiki bersama coordinator: header/konteks/konten berada dalam `.app-scroll`, nav berada di luar scroll container. Header memakai grid, judul header yang mengulang h1 disembunyikan, kas tetap tersedia. CSS tidak mengklip konten header. Status: geometri retest PASS pada matriks root; pemeriksaan source final pending.
2. **Nilai narasi rata kanan sulit dibaca saat baris menyempit.** Terlihat pada Karier dan riset, termasuk font 200%. Diperbaiki pada sumber CSS: container query max 30rem menumpuk label/nilai dan meratakan nilai ke kiri. Aturan berlaku pada root serta flow bersama. Status: source diperbaiki dan geometri recapture bersih; inspeksi gambar hasil perbaikan pending.
3. **Screenshot root saja tidak membuktikan semua layar.** Matriks 84 route × 16 kombinasi, form/review/receipt, details terbuka, loading/error/offline, high contrast, reduced motion dan perjalanan klik lengkap masih pending bukti koordinator/reviewer kontrak. Tidak diberi PASS otomatis dari delapan root viewport.

## Kontras token yang dihitung

Perhitungan memakai fungsi WCAG dari `antislop-human/contrast-check.py`. Minimum teks utama 12,692:1, sekunder 7,557:1, aksen 6,261:1 pada permukaan terdaftar. Teks pada tombol ungu 8,230:1, hover 10,202:1, pressed 7,078:1. Laba/rugi/peringatan pada background semantiknya 7,413 / 6,437 / 7,946:1. Batas kontrol minimum 4,393:1, fokus minimum 7,762:1. Pasangan token high contrast terdaftar lulus perhitungan. Ini bukan bukti semua inline/theme/state sudah diperiksa runtime.

## Next action

Tunggu source JS stabil, ulang capture root dan keyboard dengan target details yang benar, buka gambar hasil retest, lalu baca contact sheet seluruh 84 route × 16 ketika runner reviewer menyediakan hasil. Catat denominator gambar yang sungguh dibuka dan state yang belum tersedia. Delivery Gate antislop empat blok tetap belum diluluskan oleh laporan parsial ini.

## Addendum final sumber stabil

Matriks akhir `after/release-stable`:1.344/1.344PASS,84tujuan,3.200gambar. Koordinator membuka14contact sheet `review-release/routes-{100,200}-{1..7}.jpg`, mencakup168viewport atas untuk seluruh84tujuan pada320×568. Ini tidak berarti seluruh3.200gambar dibuka manusia. Gambar angka/Profil/tiket pada200% serta viewport awal/akhir kewajiban short dan toast final juga diperiksa terpisah. Pemisahan label viewport asli dengan expanded-scroll dipertahankan.

Audit runtime final `contrast-all-routes.json`:504sampel,52.007pengukuran teks, minimum6,274:1, nol kegagalan. Audit form-name pada seluruhmatriks final nol field tanpa nama aksesibel; ID input IPO kini unik perpenawaran. `root-review-final`80kombinasi stabil sebelumnya tetap mendukung pemeriksaan batas kontrol, sedangkan matriks final memperbarui geometri semua layar setelah perubahan terakhir.

Review independen menemukan dan koordinator memperbaiki: chart mengganti DOM/fokus saat polling; aria subtab; jam pause/speed; angka keuangan pecah; asosiasi label form; short belum mempunyai UI; persistensi short/margin sebelum acknowledgement; toast bertumpuk pada200%. Regression checks final memakai state domain nyata dan fault acknowledgement berlabel, tanpa membuat saldo/kewajiban/hasil palsu. Toast final satu, maksimum40dvh, dapat digulir dan ditutup dengan tombol bernama; actual320×568/200% diukur227,1875px dari batas227,2px.

Tidak ada finding visual terbuka yang diketahui dalam scope browser yang diperiksa. Native popup select, TalkBack, instalasi/lifecycle Android dan hardware tetap NOT_TESTED. Delivery Gate final milik koordinator berada pada laporan penyerahan; laporan ini tidak menggantikannya.
