# Panduan pembetulan source dan UI — AU-01 sampai AU-04

Tanggal: 2 November 2026, Asia/Jakarta.

Root produksi: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`.

Status dokumen: **PANDUAN, BELUM DIEKSEKUSI**. Pembuatan dokumen ini tidak berarti kode sudah diperbaiki dan tidak otomatis menunjuk plan eksekusi baru. Ikuti AGENTS.md dan instruksi terbaru pemilik sebelum mulai implementasi. Nomor baris merupakan snapshot saat dokumen dibuat; cari nama fungsi jika source bergeser.

## 1. Cakupan, urutan, dan batas kerja

| Urutan | ID | Keparahan | Status temuan | Tujuan |
|---|---|---|---|---|
| 1 | AU-01 | Tinggi | Terbukti pada domain Java dengan akun/save QA sintetis | Stop-Loss/Take-Profit tidak aktif ketika bursa instrumen tutup. |
| 2 | AU-04 | Sedang | Terbukti pada fault write host Windows | Respons gagal save selalu JSON valid. |
| 3 | AU-02 | Sedang | Terbukti memakai fungsi UI produksi dan data sintetis | Identitas perusahaan peminjam terlihat dan dapat dipilih sejak form pertama dibuka. |
| 4 | AU-03 | Sedang | Terbukti memakai fungsi kalender produksi dan agenda sintetis | Daftar agenda sesuai tanggal pilihan, bukan hanya judul kalender. |

Tidak ada crash, kehilangan save, atau bug fatal yang dibuktikan oleh empat temuan ini. Jangan memperluas klaim menjadi audit seluruh repo sudah tuntas. Ini bukan pengulangan implementasi R01–R12/A01 atau B01–B05; pertahankan perbaikan yang sudah benar.

Aturan untuk pelaksana:

- Buktikan cwd/Git root, branch/HEAD dan status staged/unstaged/untracked sebelum edit. Pertahankan perubahan lokal pihak lain. Jangan reset, clean, stash, atau revert massal.
- Ubah hanya fungsi/caller, regression check, dan dokumentasi yang diperlukan bagi AU-01–AU-04. Telusuri seluruh caller; hindari refactor umum, penggantian framework, dependency baru, atau balancing sambilan.
- Domain Java tetap pemilik waktu, uang, aset dan save; UI hanya menyajikan data/input. Dunia tetap otomatis 48× dan pemain tetap AON.
- Save pemain `Artifacts/LocalPreview/preview-save/save_game_v2.json` **hanya di-hash**. Jangan parse, salin, migrasikan, atau pakai sebagai fixture. Hash acuan audit: `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`. Jika berbeda, laporkan; jangan memulihkannya secara paksa.
- Gunakan save QA baru dan output terpisah; periksa tujuan tulis runner. Jelaskan perintah sebelum compile/test/build/run, gunakan dependency lokal, dan jangan install/fetch tanpa izin.
- Preview AU-02/AU-03 yang mencerminkan perubahan visibilitas/alur/data UI harus diperlihatkan dan disetujui sebelum penerapan produksi, sesuai gate proyek. Jangan meminta ulang persetujuan yang sudah jelas diberikan pada sesi eksekusi terkait.
- Jangan meluncurkan agent lain, push/publish, mengirim pesan eksternal, mengubah pengaturan global, memakai ADB atau memasang APK tanpa izin yang berlaku.

## 2. AU-01 — Trigger dieksekusi ketika bursa tutup

### Lokasi dan bukti

- `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2459–2471`, `evaluateTriggers()`; pemanggilan aktivasi pada baris 2469.
- `android/app/src/main/java/com/tradingglobal3d/core/trading/OrderManager.java:117`, `checkAndActivateTriggers()`.
- Pencarian caller produksi saat audit menemukan aktivasi tersebut dipanggil dari facade. Cari ulang seluruh caller sebelum edit.

Potongan relevan:

```java
for (Instrument inst : catalog.getActiveUniverse()) {
    // Pemeriksaan sesi yang ada di sini hanya membatasi HISTORICAL_REPLAY.
    orderManager.checkAndActivateTriggers(inst, marketEngine, ledger, accounts, now);
}
```

Komentar di atas adalah penjelasan dokumen, bukan salinan komentar source. Pada mode INTERNAL_MARKET belum ada pemeriksaan sesi per exchange di loop tersebut. OrderManager mengevaluasi harga trigger dan mengirim order ke MarketEngine; MarketEngine.submitOrder tidak menerima clock/sesi.

Probe audit menghasilkan `marketOpen=false`, waktu `08:30`, Stop-Loss SELL `FILLED`, quantity `100`. Akun, posisi dan bid adalah data QA sintetis. Tersedianya bid lawan merupakan prasyarat reproduksi; tidak semua trigger pasti terisi ketika tutup.

### Reproduksi

1. Buat game QA INTERNAL_MARKET dan satu instrumen IDX dengan lot sah.
2. Pada 08:30, siapkan posisi pemain 100 saham dan bid lawan yang sah pada order book. Ini merepresentasikan antrean yang tersedia sebelum buka.
3. Pasang STOP_LOSS SELL 100 saham dengan kondisi harga yang sudah terpenuhi.
4. Panggil jalur evaluasi trigger domain.
5. Sebelum perbaikan: order dapat FILLED walaupun bursa IDX tutup.

### Pembetulan yang disarankan

Gunakan pemeriksaan sesi existing milik clock pada boundary aktivasi facade. Jangan mengubah seluruh aturan MatchingEngine atau membatalkan order hanya karena sesi sedang tutup. Contoh arah perubahan, sesuaikan dengan flow replay yang sudah ada:

```java
if (gameMode != GameMode.HISTORICAL_REPLAY
        && !clock.isTradingOpenFor(inst.getExchange())) continue;
```

Tempatkan sebelum aktivasi trigger, pertahankan validasi replay existing, dan pastikan clock null ditangani sesuai invariant facade. Ketika tutup, order tetap PENDING_TRIGGER dan klaim saham yang valid tetap dipertahankan. Jangan memilih sesi IDX untuk seluruh instrumen; US memiliki sesi berbeda.

### Acceptance

- STOP_LOSS dan TAKE_PROFIT tidak mengubah uang/saham/fill/ledger saat IDX sebelum 09:00, istirahat 12:00–13:30, atau mulai 16:00; pending tetap utuh.
- US diperiksa terpisah: buka 21:30–04:00 WIB; tutup mulai 04:00. Gunakan timestamp game/fixed clock.
- Ketika sesi relevan buka dan harga masih memenuhi, order diproses sesuai AON dan likuiditas, tepat sekali. Ketika harga tidak memenuhi, tetap pending.
- Halt, pembatalan, komitmen saham, save/reload dan evaluasi berulang tidak menghasilkan fill ganda atau pelepasan klaim keliru.
- Uji caller dunia aktif dan catch-up; jangan hanya mengetes fungsi clock secara terpisah.

## 3. AU-04 — JSON error save tidak di-escape

### Lokasi dan bukti

`android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java:2296–2305`, `saveGame()`:

```java
return "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}";
```

Pesan exception dimasukkan langsung ke JSON. Probe fault write pada save QA menghasilkan pesan berisi path Windows; `new JSONObject(response)` gagal. Efek konkretnya: UI gagal memparse respons dan pesan penyebab save gagal tidak tersampaikan dengan benar. Ini tidak membuktikan progres pemain hilang. Reproduksi perangkat Android belum dilakukan.

### Reproduksi

1. Buat save QA baru yang valid.
2. Buat direktori penghalang pada path temporary save QA, sehingga penulisan berikutnya gagal.
3. Panggil bridge.saveGame(), kemudian parse hasilnya sebagai JSON.
4. Tambahkan check pesan berisi backslash, kutip dan newline tanpa memakai data pengguna.

### Pembetulan yang disarankan

Gunakan serializer JSON existing, jangan membuat fungsi escaping sendiri. Contoh ekspresi di dalam flow error yang menangani JSONException sesuai API proyek:

```java
new JSONObject().put("success", false)
    .put("message", e.getMessage() == null ? "Penyimpanan gagal" : e.getMessage())
    .toString();
```

Pertahankan notifySaveFailed(true), blokir transaksi pada domain, serta proteksi primary/backup. Jika helper error serializer yang sesuai sudah ada, gunakan helper itu. Jangan memasukkan isi save atau detail sensitif ke pesan/log. Pesan produksi baru yang mengubah UI mengikuti persetujuan yang berlaku.

### Acceptance

- Respons gagal parseable oleh JSONObject dan JSON.parse untuk pesan dengan `\`, `"`, newline, teks biasa, dan pesan null.
- `success` tetap false; karakter pesan yang diizinkan bertahan round-trip.
- Save gagal tidak dianggap sukses; primary/backup valid dipertahankan dan transaksi tetap diblokir sesuai kebijakan existing.
- Sesudah blocker QA dihapus, retry berhasil dan alur pemulihan existing tetap bekerja.

## 4. AU-02 — Pemilih perusahaan tersembunyi pada pembukaan awal

### Lokasi dan bukti

`android/app/src/main/assets/www/app.js`:

- Baris 2880: tombol perusahaan membuka `openDisburseLoanModal('LOAN-CORP-01', c.id)`.
- Baris 7347: fungsi `openDisburseLoanModal()`.
- Baris 7390: `companySection.style.display = 'none'`.
- Baris 7399–7401: handler onchange mengatur display, tetapi tidak dijalankan saat inisialisasi.
- Baris 7486–7487: updater lain hanya mengatur hidden, serta menganggap personal hanya LOAN-PERS-01.

```js
companySection.style.display = 'none';
// ...
const updateLoanKind = () => {
    const personal = pSelect.value === 'LOAN-PERS-01';
    collateralSection.hidden = !personal;
    companySection.hidden = personal;
};
```

Probe fungsi produksi: produk LOAN-CORP-01 dan perusahaan CO-B sudah terpilih, tetapi pemilih tidak terlihat. Ganti produk ke personal lalu kembali ke perusahaan membuatnya terlihat. Belum ada bukti dana masuk ke perusahaan yang salah.

### Pembetulan yang disarankan

Satukan updater visibilitas existing, panggil saat inisialisasi dan onchange, dan jangan mencampur inline display yang tidak direset dengan hidden. Arah minimum:

```js
const updateLoanKind = () => {
    const corporate = pSelect.value === 'LOAN-CORP-01';
    companySection.hidden = !corporate;
};
```

Hapus atau selaraskan `display:none`/handler lama untuk bagian perusahaan agar hidden benar-benar mengontrolnya. Pertahankan keputusan produk tanpa agunan existing; jangan menghidupkan produk/agunan lain hanya karena kode historis masih ada. Produk personal yang tampil sekarang LOAN-PERS-02 juga harus dianggap personal.

### Acceptance

- Pembukaan langsung dari perusahaan A/B menampilkan perusahaan yang benar sejak frame awal, tanpa perlu mengganti produk.
- Pemain dapat memilih perusahaan lain yang sah; quote dan konfirmasi memakai pilihan itu.
- Produk personal menyembunyikan bagian perusahaan tanpa memblokir pengajuan personal.
- Berganti jenis produk berulang tidak menghasilkan hidden/display yang bertentangan atau handler ganda.
- Tidak ada perusahaan: beri state yang dapat dipahami tanpa mengirim companyId kosong. Perubahan teks/alur diperlihatkan melalui preview.
- Input berubah setelah quote tetap meminta review ulang; data penerima tidak berubah diam-diam pada konfirmasi.

## 5. AU-03 — Tanggal pilihan kalender tidak menyaring daftar

### Lokasi dan bukti

`android/app/src/main/assets/www/app.js`:

- Baris 4023: `currentKalenderDayOffset`.
- Baris 4529: `renderKeuanganKalender()`.
- Baris 4571: memilih offset tanggal dan merender ulang.
- Baris 4607–4609: tanggal pilihan dipakai pada judul.
- Baris 4647–4654: loop agenda riset memfilter kategori, belum tanggal.

```js
rf.calendar.forEach(ev => {
    // Filter kategori DIV/EARN/MACRO yang ada belum memeriksa ev.date.
});
```

Probe fungsi produksi dengan tanggal game 2 November, agenda 1 November dan 3 November: kedua agenda muncul pada HARI INI dan tetap sama pada BESOK. Perbaikan tanggal ribbon sebelumnya tetap benar; masalah ada pada isi daftar.

### Pembetulan yang disarankan

- Turunkan satu tanggal pilihan dari clock domain + offset dalam WIB, memakai helper existing bila ada. Hindari tanggal komputer sebagai sumber utama.
- Pada BY_DAY, cocokkan tanggal kejadian dengan tanggal pilihan sebelum membuat kartu.
- Pada ALL_DAYS, jangan gunakan filter tanggal pilihan; urutkan agenda berdasarkan tanggal, dengan urutan stabil untuk tanggal sama.
- Contoh filter berikut hanya arah untuk agenda yang ev.date-nya sudah diverifikasi sebagai ISO YYYY-MM-DD; jangan menebak format data:

```js
if (currentKalenderViewMode === 'BY_DAY' && ev.date !== selectedDateKey) return;
```

- Telusuri juga kartu IPO dan deposito dalam renderer yang sama. Untuk kejadian satu tanggal, gunakan tanggal domain yang relevan. IPO bisa berupa rentang penawaran; deposito perlu field jatuh tempo yang sah. Jika makna tanggal/rentang belum ditetapkan, ajukan pilihan konkret kepada pemilik, jangan mengarang aturan baru.
- Penanda titik dan jumlah kategori harus berasal dari kejadian yang benar atau diberi status sesuai data; jangan menambah angka hardcoded baru.
- Gunakan empty state existing ketika tanggal/kategori tidak memiliki agenda. Preview menampilkan contoh hari kosong, hari berisi agenda, dan ALL_DAYS sebelum penerapan.

### Acceptance

- Agenda kemarin tidak muncul sebagai agenda hari ini/besok pada BY_DAY; agenda besok muncul hanya pada tanggal yang benar.
- ALL_DAYS menampilkan kejadian yang sama dalam urutan tanggal yang benar, tanpa kehilangan item atau duplikasi.
- Filter kategori dan tanggal berlaku bersamaan.
- Crossing tengah malam WIB, bulan/tahun, offset negatif/positif, dan perbedaan timezone host tidak menggeser tanggal.
- Uji fixture lintas 31 Desember–1 Januari serta data tanggal invalid/kosong; jangan diam-diam menganggapnya agenda hari ini.
- Terapkan aturan tanggal konsisten pada seluruh sumber agenda yang sudah mempunyai keputusan jelas; catat yang memerlukan keputusan.

## 6. Milestone dan bukti penyerahan

| Milestone | Cakupan | Status saat panduan dibuat |
|---|---|---|
| M0 | Baseline source/Git, perlindungan save, reproduksi | Reproduksi audit tersedia; baseline eksekusi wajib diverifikasi ulang. |
| M1 | AU-01 + regresi sesi/trigger | BELUM_DIKERJAKAN |
| M2 | AU-04 + fault/retry JSON | BELUM_DIKERJAKAN |
| M3 | Preview AU-02/AU-03 dan keputusan tanggal yang diperlukan | BELUM_DIKERJAKAN |
| M4 | Implementasi UI yang disetujui + check produksi | BELUM_DIKERJAKAN |
| M5 | Regresi relevan, build bila diperlukan, hash save dan laporan | BELUM_DIKERJAKAN |

Pakai runner yang sudah tersedia. Perubahan domain perlu kompilasi fresh dan satu runnable regression minimum per invariant. UI harus diuji memakai fungsi/CSS produksi; preview sintetis saja bukan bukti integrasi. Jika menjalankan build, catat versi toolchain dan hash APK serta cocokkan asset dengan source; jangan memasang ke perangkat tanpa izin. Jangan menghitung tes overlap sebagai total unik.

Laporan akhir minimal:

| ID | Status | File/fungsi/baris akhir | Reproduksi sebelum | Check sesudah + log/snapshot | Batas/next_action |
|---|---|---|---|---|---|
| AU-01 | Belum diperbaiki | evaluateTriggers/checkAndActivateTriggers | FILLED 100 pada 08:30, IDX tutup | Diisi pelaksana | Sesi normal/istirahat/US/aktif/offline |
| AU-04 | Belum diperbaiki | TradingWebBridge.saveGame | Respons fault write tidak valid JSON pada Windows | Diisi pelaksana | Native belum terbukti |
| AU-02 | Belum diperbaiki | openDisburseLoanModal | Perusahaan terpilih tetapi pemilih tersembunyi | Diisi pelaksana | Preview/UI produksi |
| AU-03 | Belum diperbaiki | renderKeuanganKalender | Agenda sama pada HARI INI/BESOK | Diisi pelaksana | Makna tanggal/rentang per sumber |

Sertakan file berubah dan alasan per ID, command/exit code, bukti persetujuan UI, hasil sebenarnya termasuk failure, hash save sebelum/sesudah, dan batas browser/host/APK/Android. Jangan mengklaim selesai dari file dokumen, screenshot, tes subset, atau APK saja. Sesudah penyerahan berhenti sesuai scope; temuan lain dicatat terpisah, bukan diperbaiki diam-diam.
