# Handoff audit logika dan respons UI — 1 November 2026

Status: temuan dan instruksi verifikasi untuk agent pemilik. Belum ada perbaikan source dari audit ini. Dokumen ini bukan bukti lulus runtime, bukan penunjukan otomatis Plan 14, dan bukan persetujuan perubahan tampilan.

**File eksekusi pendamping:** `Work/Eksekusi-Audit-Logika-UI-2026-11-01/plan.md` pada root produksi. File tersebut menyatukan urutan kerja, keputusan terbaru, gate UI/save, pengujian dan format penyerahan. Handoff ini tetap menjadi sumber bukti detail per temuan. Keduanya siap diserahkan; belum ada agent diluncurkan atau perbaikan dijalankan hanya karena file disiapkan.

**Keputusan terbaru — order besar:** pemilik secara eksplisit memilih dukungan tambahan likuiditas sesuai kebutuhan order besar yang valid, tetap All-Or-None. Cadangan 2.000 lot adalah cadangan dasar, bukan batas maksimum order atau dukungan likuiditas. Target kas Rp10 miliar/US$1 juta juga merupakan target dasar; jika menjadi lawan jual pemain yang valid, kebutuhan kas eksekusi harus dihitung sesuai order. Aturan tambahan di bagian 12 menggantikan pembacaan angka dasar sebagai plafon keras. Source game dan AGENTS.md belum diubah oleh pencatatan keputusan ini.

**Pembaruan setelah sesi 10 pertanyaan dan keputusan lanjutan:** keputusan pemilik dicatat pada bagian 11. Bagian tersebut menjadi rujukan keputusan untuk perbaikan; temuan dan bukti audit tetap dipertahankan. Pemilik memilih cakupan 17 temuan utama serta catatan tambahan yang sudah mempunyai keputusan jelas. Untuk Q08, pemilik memilih data terakhir tetap terlihat dengan transaksi terkunci sementara; panel hilang otomatis setelah pemrosesan dan penyimpanan berhasil. Setelah melihat contoh animasi, pemilik secara eksplisit memilih varian `Kalender dan jam` untuk handoff agent. Q02 juga telah diputuskan: saat penulisan save sungguh tidak tersedia setelah pemulihan otomatis dicoba, blokir transaksi baru, tetap izinkan melihat data/peringatan/coba simpan ulang, pertahankan progres di memori dan salinan valid terakhir; waktu dunia tetap 48x. Aturan suplai NPC juga telah ditetapkan: kas berupa suntikan modal eksternal simulasi tanpa utang/bunga; saham berupa penerbitan baru dengan dilusi yang diterima; angka dan pengulangan tercantum pada Q04. Jangan meminta ulang keputusan yang sudah diberikan; pilihan komponen ini tidak merupakan persetujuan perubahan layar lain.

## 1. Lingkup, bukti, dan batas kerja

- Permintaan pemilik: simpan hasil audit untuk diperbaiki agent, kemudian periksa kembali kesalahan logika dan UI yang tidak merespons ketika dipanggil.
- Root produksi: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`.
- Branch saat pemeriksaan: `codex/classic-ui-new-assets`.
- HEAD: `2b6ee5cb1bd59dcefd8d6b0410446bce5d18b736`. Worktree kotor; HEAD saja tidak mengidentifikasi snapshot audit.
- Acuan: instruksi langsung pemilik, termasuk konfirmasi tetap memakai AGENTS.md: dunia 48x, tanpa pause/speed/skip pemain. AGENTS.md di disk juga dibaca ulang; optimisasi fast-forward batas kejadian tidak mengizinkan membuang durasi offline.
- Hanya dokumen ini yang ditulis oleh pekerjaan handoff. Source, konfigurasi, test, APK, dan save tidak diubah.
- Save pemain hanya di-hash: `Artifacts/LocalPreview/preview-save/save_game_v2.json`, SHA-256 `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`.
- Semua path dan nomor baris di bawah relatif terhadap root produksi. Baris mengacu pada snapshot yang di-hash di tabel berikut; cari ulang simbol bila source berubah.
- Singkatan lokasi: `core/...` dan `mobile/...` berada di `android/app/src/main/java/com/tradingglobal3d/`; `assets/www/...` dan `index.html` berada di `android/app/src/main/` dan direktori `assets/www/` tersebut. Cuplikan dengan `...` atau nama hasil ekstraksi adalah ilustrasi arah perbaikan, bukan patch siap tempel.

| Source | SHA-256 snapshot |
|---|---|
| `android/app/src/main/assets/www/app.js` | `3D359B91B1BD16EBED5A990F42FDF63E4B33A1CE46EC800C2BAECD21F0593844` |
| `android/app/src/main/assets/www/index.html` | `578623FDA2ADC062965248F2D26215A46B736E3E98C232579954320A60A63D1E` |
| `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java` | `96ED95BFCF6CEF7DB15E15B6CB8BD96C45605D67EAFBB61854354D253FC1072E` |
| `android/app/src/main/java/com/tradingglobal3d/core/trading/MatchingEngine.java` | `132FB7C080403AFF5E9539F174C429B47E6AE7255BAF282695E84C8EE87CFA19` |
| `android/app/src/main/java/com/tradingglobal3d/core/storage/SaveManager.java` | `242F1926B43E39F7CF762B9A877186DAF2B9FC1152FB0BAFCD9BF37D1428195E` |

Definisi status:

- **TERBUKTI_STATIS:** cacat dan jalur pemanggilannya terlihat pada source. Bukan klaim telah mengklik UI atau menguji APK.
- **POTENSIAL_PERLU_RUNTIME:** jalur risiko terlihat, tetapi dampak akhir memerlukan reproduksi terisolasi.
- **BELUM_DIUJI:** langkah penerimaan yang belum dijalankan. Jangan menggantinya dengan PASS berdasarkan keberadaan kode/test.

Audit tidak menjalankan game, build, JUnit, server QA, browser, emulator, atau perangkat. Panduan Security Audit yang dipakai mensyaratkan isolasi OS dengan jaringan dibatasi, target/toolchain read-only, area tulis khusus, environment allowlist dan batas resource sebelum target code dijalankan. Fasilitas lengkap itu tidak tersedia dalam sesi audit. Parsing source dengan Node/Acorn dan HTMLParser tidak mengeksekusi source game.

## 2. Instruksi untuk agent pelaksana

1. Baca AGENTS.md dan keputusan pemilik terbaru. Buktikan cwd, branch, root Git, dan diff lokal sebelum menulis. Jangan memakai status laporan lama sebagai bukti snapshot kini.
2. Revalidasi setiap temuan terhadap source terkini. Jika sudah diperbaiki, tunjukkan perubahan dan hasil cek yang benar-benar menutup invariant; jangan mengedit ulang hanya untuk memenuhi daftar.
3. Kerjakan shared root cause dan semua caller. Pakai helper/pola/runner yang ada, tanpa framework, dependency, atau abstraksi baru yang tidak diperlukan.
4. Pemilik menunjuk lingkup/plan yang dikerjakan. Dokumen ini bahan handoff, bukan izin mengeksekusi seluruh plan lama. Jangan meluncurkan agent lain atau mengirim pesan lintas chat.
5. Perbaikan UI, teks, navigasi, dan alur tetap memerlukan preview serta persetujuan eksplisit per perubahan. Pemindahan helper internal yang memulihkan perilaku yang sudah ada harus dijelaskan sebagai perbaikan fungsi; jangan sekaligus mendesain ulang layar.
6. Jangan reset/clean/stash, menyentuh repo lama, commit perubahan pihak lain, push/publish, ADB, instalasi atau tindakan perangkat tanpa izin yang berlaku.
7. Semua fixture/save pengujian harus baru dan terisolasi. Save pemain di atas tetap hanya di-hash, bukan dibaca isinya, disalin sebagai fixture, atau dimigrasi.
8. Tinggalkan satu runnable regression check yang relevan untuk logika nontrivial memakai runner yang ada. Bedakan hasil host, browser, APK, emulator, dan ponsel.
9. Untuk setiap ID laporkan: status sebelum/sesudah, file berubah, perintah yang benar-benar dijalankan, hasil, batas bukti, blocker, dan next_action. Jangan menyatakan selesai hanya karena compile/sintaks/APK berhasil.

Urutan yang disarankan: **B01 -> A01 -> A02/A03 -> A04/A05/A10 -> A07/B02/B03/A08/A06 -> B04/B05/B06/B07/A09**, kemudian catatan C01-C12 sesuai dampak dan keputusan yang diperlukan. Ini urutan prioritas teknis, bukan pengganti penunjukan lingkup oleh pemilik.

## 3. Ringkasan pekerjaan

| ID | Keparahan | Status bukti | Inti masalah | Uji penerimaan |
|---|---|---|---|---|
| B01 | Tinggi, penghambat trading UI | TERBUKTI_STATIS | `parseCleanPrice` berada pada scope yang tidak dapat diakses tiket order/IPO/live update | BELUM_DIUJI |
| A01 | Fatal potensial | POTENSIAL_PERLU_RUNTIME | Estimasi maker dan settlement berbeda; posisi berubah sebelum debit berhasil | BELUM_DIUJI |
| A02 | Tinggi | TERBUKTI_STATIS | Gagal validasi domain primary tidak melanjutkan pemilihan backup | BELUM_DIUJI |
| A03 | Tinggi potensial | POTENSIAL_PERLU_RUNTIME | Validator rotasi backup lebih lemah daripada validator load lengkap | BELUM_DIUJI |
| A04 | Tinggi | TERBUKTI_STATIS | Pokok deposito tidak masuk kekayaan bersih | BELUM_DIUJI |
| A05 | Tinggi | TERBUKTI_STATIS | Drift/shock mengubah instrumen tanpa memperbarui harga posisi saham | BELUM_DIUJI |
| A06 | Tinggi | TERBUKTI_STATIS | Reset sesi otomatis hanya IDX; acuan harian US tertinggal | BELUM_DIUJI |
| A07 | Tinggi | TERBUKTI_STATIS | Catch-up dipangkas tujuh hari game dan sisa waktu dibuang | BELUM_DIUJI |
| A08 | Sedang | TERBUKTI_STATIS | Skip sesi masih diekspos ke pemain | BELUM_DIUJI |
| A09 | Sedang | TERBUKTI_STATIS | Cursor ukuran riwayat tidak bekerja setelah cap 500 fill | BELUM_DIUJI |
| A10 | Sedang | TERBUKTI_STATIS | Plafon margin mengabaikan utang mata uang lainnya | BELUM_DIUJI |
| B02 | Tinggi | TERBUKTI_STATIS | Daily drift diterapkan per resume, termasuk jeda singkat dan di luar jam bursa | BELUM_DIUJI |
| B03 | Tinggi | TERBUKTI_STATIS | Preferensi pemain dapat mematikan waktu offline, berlawanan dengan dunia otomatis | BELUM_DIUJI |
| B04 | Sedang | TERBUKTI_STATIS | Error pembatalan auto-renewal ditulis ke elemen yang sudah dilepas dari DOM | BELUM_DIUJI |
| B05 | Rendah | TERBUKTI_STATIS | Counter koleksi memanggil method bridge yang tidak tersedia | BELUM_DIUJI |
| B06 | Rendah | TERBUKTI_STATIS | Prompt nominal invalid berhenti diam tanpa feedback | BELUM_DIUJI |
| B07 | Sedang | TERBUKTI_STATIS | Teks prestige menjanjikan manfaat yang bertentangan dengan respons domain | BELUM_DIUJI |

Jumlah: 17 temuan utama unik; 15 TERBUKTI_STATIS dan 2 POTENSIAL_PERLU_RUNTIME. Keparahan: 1 fatal potensial, 9 tinggi (termasuk 1 potensial), 5 sedang, 2 rendah. Tidak ada temuan yang dinyatakan telah lulus reproduksi runtime. Selain daftar utama, bagian 9 menyimpan 12 catatan optimisasi, kebutuhan verifikasi, dan keputusan produk; catatan itu tidak dijumlahkan sebagai bug terbukti.

## 4. Sepuluh temuan awal — ditinjau ulang

### A01 — Settlement market buy dapat setengah jadi

**Lokasi kode:**

- `android/app/src/main/java/com/tradingglobal3d/core/trading/MatchingEngine.java` — baris 169-183, 348-369, 675-705.
- `android/app/src/main/java/com/tradingglobal3d/core/trading/OrderManager.java` — baris 58-70.
- `android/app/src/main/java/com/tradingglobal3d/core/trading/OrderBook.java` — baris 45-47.

**Simbol pencarian:** `MatchingEngine.submitOrder, canFillPlayerGreedily, matchBuyOrder; OrderManager.restoreState`.

**Bukti:** `android/app/src/main/java/com/tradingglobal3d/core/trading/MatchingEngine.java:169-183` menghitung reservasi dengan `Math.min(remaining, maker.getRemainingQuantity())`; baris `363-369` dan `675-680` justru melewati maker pemain yang lebih besar daripada kebutuhan taker. Baris `701-705` menjalankan `buyerPos.addBuy(...)` sebelum dua debit ledger. Periksa juga jalur simetris jual dan `executePlannedFills`.

```java
// Reservasi: maker pemain besar tidak dilewati.
long units = Math.min(remaining, maker.getRemainingQuantity());
// Eksekusi: maker itu dapat dilewati.
if (isPlayerAccount(seller) && buyOrder.getRemainingQuantity() < makerSell.getRemainingQuantity()) { ... }
// Posisi berubah sebelum pembayaran.
buyerPos.addBuy(matchQty, askPrice, buyFee);
ledger.recordMutation(..., tradeValue, false, ...);
```

**Dampak potensial:** saham tanpa pembayaran, reservasi berbeda dengan order, order MARKET tetap nonfinal. `OrderManager.java:68-70` mencoba memasukkan order nonfinal ke `addLimitOrder`, sedangkan `OrderBook.java:45-47` menolak harga null; save setelah kegagalan berpotensi gagal restore.

**Reproduksi QA yang harus dijalankan:** instrumen lot 100 dan harga referensi 100; pembeli PERSONAL berkantong Rp10.500; maker CLIENT menjual 200 saham @100, maker NPC 100 saham @110; market buy PERSONAL 100 saham. Harga 100 dan 110 berada dalam band. Estimasi dapat memakai 100+fee tetapi eksekusi memilih 110+fee. Uji melalui domain/fixture agar tidak bergantung pada B01. Verifikasi apakah exception debit terjadi setelah posisi bertambah; jangan mencoba pada save pemain.

**Perbaikan:** samakan kandidat, filter band, AON, self-trade dan kuantitas untuk preview/reservasi/preflight/eksekusi. Validasi seluruh rencana settlement sebelum mutasi pertama. Jangan memperbaiki hanya dengan memindah satu debit ke atas; order, seller, fee, dan reservasi juga harus konsisten saat gagal. Jangan menghidupkan pencarian subset rekursif mati sebagai jalan pintas tanpa kebutuhan.

**Acceptance:** saldo/posisi/reservasi tidak berubah pada penolakan; status order final atau antrean valid; tidak ada MARKET aktif berhaga null dalam save; save-reload dan rekonsiliasi tetap berhasil; uji kasus simetris maker yang dilewati pada order jual.

### A02 — Backup tidak dipilih bila primary gagal validasi domain

**Lokasi kode:**

- `android/app/src/main/java/com/tradingglobal3d/core/storage/SaveManager.java` — baris 132-164.
- `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java` — baris 2583-2585, 2612-2774.

**Simbol pencarian:** `SaveManager.load; TradingFacade.load`.

**Bukti:** `core/storage/SaveManager.java:140-157` di bawah direktori Java produksi mengembalikan PRIMARY segera setelah `SaveState.fromJsonString(content)`. `core/facade/TradingFacade.java:2583-2585` menerima kandidat itu, kemudian `2612-2774` memvalidasi graph; catch pada `2772-2774` hanya `return false`.

```java
SaveState state = SaveState.fromJsonString(content);
return new LoadResult(state, LoadSource.PRIMARY, ...);
// Validasi facade berikutnya:
catch (Exception invalidState) { ...; return false; }
```

**Dampak:** permainan tidak dapat dimuat melalui jalur biasa walau backup valid tersedia. Tidak berarti file sudah hilang.

**Reproduksi:** primary QA berisi JSON dan kas valid tetapi order menunjuk instrumen tidak dikenal; backup tetap valid. `startOrResume()` harus menghasilkan penawaran backup, bukan hanya gagal berulang.

**Perbaikan:** ekstrak validator detached yang sudah ada dan gunakan pada pemilihan kandidat primary/backup. Primary invalid penuh -> evaluasi backup penuh -> tawarkan konfirmasi pemulihan. Jangan menimpa primary/backup sebelum persetujuan. Hindari dua validator graph yang terpisah.

**Acceptance:** kasus primary syntax-invalid, primary domain-invalid, backup invalid, keduanya invalid, serta penolakan/penerimaan tawaran; hash file tidak berubah sebelum persetujuan.

### A03 — Rotasi backup dapat menerima primary yang tidak bisa dimuat

**Lokasi kode:**

- `android/app/src/main/java/com/tradingglobal3d/core/storage/SaveManager.java` — baris 65-94, 241-264.

**Simbol pencarian:** `SaveManager.save, validateSaveIntegrity`.

**Bukti:** `core/storage/SaveManager.java:65-89` menyalin primary ke backup jika `validateSaveIntegrity` true. Validator `241-260` hanya membaca sebagian header dan enum akun/ledger, bukan nilai kas/posisi/graph runtime lengkap.

```java
isTargetValid = validateSaveIntegrity(existingContent);
// Validator hanya mengecek enum akun, bukan cash.IDR:
AccountType.valueOf(acc.getString("type"));
```

**Dampak potensial:** backup last-good tertimpa primary yang secara JSON valid tetapi nilai/graph-nya rusak. Dampak kehilangan pemulihan memerlukan fault injection; belum terjadi atau dibuktikan pada save pemain.

**Reproduksi:** setelah game QA dimuat, ubah `cash.IDR` primary menjadi teks nonangka tanpa merusak JSON, lalu save state live yang valid. Bandingkan hash dan kelayakan backup sebelum/sesudah.

**Perbaikan:** gunakan validator penuh hasil A02 sebelum rotasi. Jika primary invalid, pertahankan backup valid. Penulisan tmp, fsync dan atomic replace yang sudah ada tetap dipertahankan.

**Acceptance:** backup valid tidak berubah jika primary gagal validasi angka, posisi, order, atau runtime graph. Primary baru tetap bisa disimpan tanpa menghancurkan last-good.

### A04 — Deposito tidak dihitung sebagai aset

**Lokasi kode:**

- `android/app/src/main/java/com/tradingglobal3d/core/multiasset/TimeDepositManager.java` — baris 100-125.
- `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java` — baris 3077-3114, 4420-4485.

**Simbol pencarian:** `TimeDepositManager.openDeposit; TradingFacade.getPersonalNetWorth, getConsolidatedWealthBreakdown`.

**Bukti:** `core/multiasset/TimeDepositManager.java:113-125` mendebit principal dan menyimpan deposito aktif. `core/facade/TradingFacade.java:3077-3114` menghitung net worth tanpa deposito; `4420-4485` juga tidak menyajikan komponennya.

```java
ledger.recordMutation(..., principal, false, ...);
activeDeposits.put(depositId, dep);
// getPersonalNetWorth() menambahkan perusahaan/koleksi/fund, tetapi bukan deposito.
```

**Dampak:** penempatan mengurangi net worth seolah pokok hilang; memengaruhi return, promosi, margin dan kelayakan pemulihan.

**Reproduksi:** kas Rp50 juta -> deposito Rp10 juta -> kas Rp40 juta; net worth harus tetap Rp50 juta sebelum biaya/hasil lain.

**Perbaikan minimum:** jumlahkan `getPrincipal()` deposito aktif di TimeDepositManager, tambahkan pada kekayaan dan komponennya. Jangan menggandakan kas yang hanya direservasi (misalnya pending subscription fund). Kebijakan pengakuan bunga berjalan terpisah dari kewajiban menghitung pokok.

**Keputusan pemilik Q03:** bunga yang belum jatuh tempo hanya estimasi terpisah; baru menambah kekayaan bersih ketika jatuh tempo dan menjadi hak pemain. Pokok aktif tetap aset. Terapkan keputusan ini pada ringkasan, return, kelayakan bailout dan save/reload agar tidak ada bunga diakui dua kali.

**Acceptance:** tempatkan, tarik dini, jatuh tempo, save/reload; principal tidak hilang atau dihitung dua kali. Uji dua penempatan dan sisa deposito setelah salah satunya dicairkan.

### A05 — Harga posisi saham tertinggal dari harga instrumen

**Lokasi kode:**

- `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java` — baris 2102-2108, 3771.
- `android/app/src/main/java/com/tradingglobal3d/core/risk/RiskComplianceService.java` — baris 465-484.
- `android/app/src/main/java/com/tradingglobal3d/core/trading/MatchingEngine.java` — baris 877-881.

**Simbol pencarian:** `simulateDailyMarketMovement, triggerMarketShockEvent, evaluateMarginHealth, recordMarketPrice`.

**Bukti:** `core/facade/TradingFacade.java:2102-2108` (offline drift) dan `3771` (shock) memanggil `inst.updatePrice` tanpa memperbarui posisi saham. `core/risk/RiskComplianceService.java:478-484` menghitung long memakai `p.getCurrentPrice()`. Jalur normal `core/trading/MatchingEngine.java:877-881` memperbarui posisi setelah update instrumen.

```java
inst.updatePrice(newPrice, volumeLots); // drift
BigDecimal val = p.getCurrentPrice().multiply(BigDecimal.valueOf(p.getQuantity()));
```

**Dampak:** layar pasar, portofolio, equity/margin, dan save dapat menyimpan mark berbeda.

**Reproduksi:** miliki saham, terapkan drift/shock yang benar-benar mengubah harga, bandingkan harga instrumen dan seluruh posisi terkait sebelum trade berikutnya.

**Perbaikan:** semua caller perubahan harga harus memakai pola bersama yang sudah dilakukan settlement: update instrumen lalu tandai posisi dengan `inst.getCurrentPrice()` aktual setelah clamp band. Periksa split/corporate action secara terpisah agar tidak membagi harga dua kali.

**Acceptance:** harga posisi dan instrumen konsisten segera setelah drift, shock, settlement dan reload; net worth berubah menurut jumlah saham dan perubahan harga aktual.

### A06 — Reset sesi US hilang dari jalur otomatis

**Lokasi kode:**

- `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java` — baris 2181-2196.
- `android/app/src/main/java/com/tradingglobal3d/core/market/Instrument.java` — baris 197-201.
- `android/app/src/main/java/com/tradingglobal3d/core/clock/GameClock.java` — baris 123-138.

**Simbol pencarian:** `TradingFacade.settleDay; Instrument.startNewTradingDay; GameClock.getMarketSession`.

**Bukti:** `core/facade/TradingFacade.java:2181-2189` pada midnight hanya memanggil `startNewTradingDay` untuk IDX. Reset semua instrumen pada `2191-2196` adalah cabang advanceToMorning, bukan reset akhir sesi US otomatis. Tidak ditemukan caller otomatis pengganti pada 04:00. `core/market/Instrument.java:197-201` mengubah previous close, high, low, dan volume.

```java
if (inst.getExchange() == MarketExchange.IDX) inst.startNewTradingDay();
```

**Dampak:** acuan perubahan harian dan band US tetap mengikuti sesi lama pada permainan otomatis.

**Reproduksi:** ubah harga US, lewati penutupan 04:00 dan masuk sesi US berikutnya, cek previousClose/dayVolume/high/low dan band.

**Perbaikan:** tangani batas penutupan US tepat sekali per sesi; deteksi crossing pada lompatan, bukan hanya equality jam. Integrasikan ke batas kejadian catch-up yang disahkan. Jangan reset US di tengah sesi pada midnight.

**Acceptance:** foreground dan offline melintasi penutupan dengan hasil reset sama; jangan mengulang reset ketika sync berikutnya masih dalam sesi sama.

### A07 — Durasi offline dipangkas dan sisanya dibuang

**Lokasi kode:**

- `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java` — baris 87-94, 267-320, 367-385.

**Simbol pencarian:** `reconcileOfflineElapsed, resumeWorld, onAppLifecycleChanged`.

**Bukti:** `core/facade/TradingFacade.java:280-289` membatasi `elapsedRealMs` menjadi `7L * 1800L * 1000L`; `319`, caller `resumeWorld:89-90`, dan lifecycle `379-383` memajukan anchor ke sekarang.

```java
long cappedRealMs = Math.min(elapsedRealMs, 7L * 1800L * 1000L);
long gameSeconds = clock.advanceRealMs(cappedRealMs, false);
lastRealTimeAnchor = Math.max(lastRealTimeAnchor, realTimeClock.millis());
```

**Dampak:** offline 24 jam nyata hanya memproses 7 hari game, bukan 48 hari; sisa 41 hari tidak tertunda untuk diproses. Jangan salah menganggap optimisasi step ke cutoff/midnight sebagai bug: yang bermasalah adalah penghapusan sisa durasi.

**Perbaikan:** proses batch terbatas dengan sisa waktu persisten, atau algoritme catch-up terukur yang mencakup seluruh durasi. Anchor hanya mengakui bagian yang benar-benar diproses; telusuri seluruh caller yang menimpanya. Pertahankan deadline berita real-time.

**Acceptance:** 24 jam -> 48 hari game; tutup/buka saat batch berlangsung tidak menghilangkan atau menggandakan durasi; batas CPU/memori dicatat; kewajiban harian tepat sekali.

### A08 — Skip sesi masih dapat dipanggil pemain

**Lokasi kode:**

- `android/app/src/main/assets/www/app.js` — baris 315-346.
- `android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java` — baris 1465-1467, 2273-2274.
- `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java` — baris 332-363.

**Simbol pencarian:** `renderFlowingClock; TradingWebBridge.skipToNextMarketSession; TradingFacade.skipToNextMarketSession`.

**Bukti:** `assets/www/app.js:315-346` menampilkan tombol dan memanggil `bridge.skipToNextMarketSession()`. `core/facade/TradingFacade.java:332-363` memajukan dunia. `mobile/TradingWebBridge.java:1465-1467` mengekspos caller itu.

```javascript
const res = JSON.parse(bridge.skipToNextMarketSession());
```

**Dampak:** pemain melewati waktu nyata yang wajib menurut keputusan 48x tanpa kontrol skip.

**Koreksi laporan awal:** native `TradingWebBridge.skipDay` pada baris `2273-2274` SUDAH menolak skip. Jangan menyatakan kedua endpoint masih aktif. Yang tetap aktif adalah `skipToNextMarketSession`; method domain legacy harus diaudit caller-nya, bukan dihapus sembarang.

**Perbaikan:** tutup jalur skip pemain di native/domain boundary; rencana penghapusan/penyembunyian tombol perlu preview dan persetujuan sesuai aturan UI.

**Acceptance:** panggilan endpoint pemain tidak mengubah waktu, kas atau jumlah hari; semua native endpoint serupa diperiksa ulang.

### A09 — Statistik berhenti pada cap 500 fill

**Lokasi kode:**

- `android/app/src/main/java/com/tradingglobal3d/core/trading/OrderBook.java` — baris 68-74.
- `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java` — baris 1460-1474.
- `android/app/src/main/java/com/tradingglobal3d/core/player/PlayerProfileManager.java` — baris 163-175.

**Simbol pencarian:** `OrderBook.recordFill; evaluateRecordedCareerProgress; PlayerProfileManager.recordFillResult`.

**Bukti:** `core/trading/OrderBook.java:68-72` mempertahankan ukuran 500 dengan `remove(0)`. `core/facade/TradingFacade.java:1463-1474` memakai ukuran sebagai cursor.

```java
for (int i = evaluatedFillCounts.getOrDefault(book, 0); i < fills.size(); i++) { ... }
evaluatedFillCounts.put(book, fills.size());
```

**Dampak:** setelah cursor 500, fill baru tidak dicatat ke statistik profil walaupun daftar bergeser.

**Reproduksi:** evaluasi 500 fill pada satu instrumen, kemudian proses fill ke-501 yang melibatkan pemain, cek statistik dan profitable/best/worst trade.

**Perbaikan minimum:** baca ulang histori terbatas dan gunakan `playerProfileManager.hasRecordedFill` yang sudah ada. Jangan mengandalkan ukuran daftar yang di-trim. Bila perlu optimisasi, ukur dulu sebelum membuat sequence baru.

**Acceptance:** fill 501+, reload, dan evaluasi berulang mencatat setiap fill pemain tepat sekali; fill NPC tidak masuk statistik pemain.

### A10 — Plafon margin tidak menggabungkan IDR dan USD

**Lokasi kode:**

- `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java` — baris 1919-1934.

**Simbol pencarian:** `TradingFacade.takeMarginLoan`.

**Bukti:** `core/facade/TradingFacade.java:1919-1933` membaca `getMarginDebt(personalId, amount.getCurrency())`, hanya mengonversi calon utang mata uang itu dan membandingkan dengan plafon.

```java
Money currentDebt = riskService.getMarginDebt(personal.getId(), amount.getCurrency());
Money debtCandidate = currentDebt.add(amount);
```

**Dampak:** peminjam dapat mengambil plafon penuh dalam dua mata uang; total melampaui multiplier yang diumumkan.

**Reproduksi:** net worth Rp100 juta, plafon 2x, utang IDR Rp200 juta; pengajuan USD setara Rp200 juta berikutnya tidak boleh lolos.

**Perbaikan:** hitung `debtIDR + debtUSD * fx + requestConvertedToIDR` sebelum mutasi. Pertahankan multiplier ekonomi yang sudah disahkan, jangan memilih nilai baru.

**Acceptance:** IDR->USD dan USD->IDR, kurs berbeda, batas tepat, penolakan tanpa mutasi, save/reload.

## 5. Temuan pemeriksaan lanjutan — logika dan respons UI

### B01 — Helper harga berada di scope salah; tiket trading dan IPO gagal dibuka

**Lokasi kode:**

- `android/app/src/main/assets/www/app.js` — baris 5355, 5587-5601, 1694-1695, 1776, 6294, 9295-9307.
- `android/app/src/main/assets/www/index.html` — baris 264.

**Simbol pencarian:** `parseCleanPrice, openInstrumentDetail, openOrderTicket, openIpoOrderModal, updateInstrumentDetailLive`.

**Keparahan:** Tinggi; perbaikan pertama karena menghambat alur inti. **Status:** TERBUKTI_STATIS, belum click-through runtime.

**Bukti:** `assets/www/app.js:5355` membuka fungsi `openInstrumentDetail(stock)`. Satu-satunya deklarasi `parseCleanPrice` ada sebagai fungsi lokal di `5587-5601` di dalam fungsi tersebut. Call di `openOrderTicket:9303`, `openIpoOrderModal:6294`, dan `updateInstrumentDetailLive:1694-1695,1776` berada di luar scope itu. `index.html:264` hanya memuat app.js, tidak ada deklarasi script lain untuk helper ini.

```javascript
function openInstrumentDetail(stock) {
    // ...
    function parseCleanPrice(val) { /* ... */ }
}
function openOrderTicket(stock, preselectedSide) {
    // ...
    inputOrderLimit.value = parseCleanPrice(stock.currentPriceRaw || stock.currentPrice);
    // Baris pembukaan modal berada sesudah call yang gagal.
    orderModal.classList.add('active');
}
```

**Dampak:** lexical lookup JavaScript tidak menemukan helper; pemanggilan tiket berhenti sebelum modal diaktifkan. IPO berhenti sebelum formulir lengkap disusun. Pada live update, exception dapat tertangkap sehingga detail berhenti diperbarui tanpa akar masalah terlihat di layar.

**Cara verifikasi:** pada salinan QA buka Pasar -> detail saham -> Beli/Jual; buka pusat IPO -> pesan IPO; biarkan detail harga diperbarui. Rekam pageerror/console; pastikan helper dapat diakses semua caller. Membuka detail saham terlebih dahulu TIDAK menjadikan fungsi lokal tersedia secara global.

**Perbaikan minimum:** pindahkan satu deklarasi helper yang sudah ada ke scope IIFE bersama (misalnya dekat `formatMoney`); hapus deklarasi lokalnya. Jangan menduplikasi parser di setiap caller. Utamakan field angka raw dari bridge; fallback format harus mempertimbangkan mata uang dan format desimal.

**Contoh arah perubahan (bukan patch yang sudah diterapkan):**

```javascript
(function () {
    'use strict';
    function parseCleanPrice(val) { /* pindahkan isi helper existing ke sini */ }
    function openInstrumentDetail(stock) { /* tanpa helper lokal duplikat */ }
    function openOrderTicket(stock, preselectedSide) { /* caller existing */ }
})();
```

**Acceptance:** tiket beli, tiket jual, formulir IPO dan live detail semuanya merespons; tidak ada `ReferenceError: parseCleanPrice is not defined`; nilai raw IDR/USD tepat; tidak ada duplikasi fungsi. Pemeriksaan sintaks saja TIDAK mendeteksi bug lexical scope ini.

### B02 — Harga berubah berdasarkan jumlah resume, bukan hanya waktu yang berlalu

**Lokasi kode:**

- `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java` — baris 288-293, 2072-2108.

**Simbol pencarian:** `reconcileOfflineElapsed, simulateDailyMarketMovement`.

**Keparahan:** Tinggi. **Status:** TERBUKTI_STATIS; besar perubahan/kemudahan eksploit perlu runtime.

**Bukti:** `core/facade/TradingFacade.java:288-293` menjalankan `simulateDailyMarketMovement` setiap catch-up menghasilkan detik game, termasuk jeda 1 detik nyata. Method `2072-2108` memakai seed tanggal, menerapkan drift persentase ke harga saat ini, dan membuat TradeFill baru; tidak memeriksa apakah hari/sesi baru sudah diproses atau bursa sedang buka.

```java
if (gameSeconds > 0) {
    advanceWorldSeconds(gameSeconds);
    simulateDailyMarketMovement(clock.getCurrentDate());
}
// Di helper:
Random rnd = new Random(Objects.hash(date.toString(), "MARKET_DRIFT_2026"));
inst.updatePrice(newPrice, volumeLots);
book.recordFill(new TradeFill(...));
```

**Dampak:** dua resume singkat dapat menerapkan drift dua kali, sedangkan satu jeda dengan total durasi sama hanya sekali. Harga dan tape juga dapat berubah sebelum jam perdagangan. TradeFill drift memakai ID order dan akun tanpa settlement kas/saham yang menyertai pemanggilan itu. Ini berbeda dari A05: A05 sinkronisasi mark, B02 pemicu/makna kejadian pasar.

**Reproduksi QA minimum:** dua world identik pada 08:30 (kedua bursa tertutup). World A `reconcileOfflineElapsed(2000)`; World B `reconcileOfflineElapsed(1000)` dua kali. Clock seharusnya sama, tetapi jumlah penerapan drift berbeda. Gunakan real clock tetap dan katalog yang identik; bandingkan harga/tape/ledger. Tidak perlu mengejar profit atau mencoba save asli.

**Perbaikan:** jangan menambahkan daily drift sebagai efek samping resume. Jadwalkan per kejadian/sesi yang dilewati, dengan penanda persisten agar tidak terulang saat reopen. Pakai jalur harga/settlement yang konsisten; jika data agregasi simulasi diperlukan, pisahkan dari fill transaksi sungguhan dan jangan mengarang settlement. Pilihan model ekonomi baru membutuhkan keputusan pemilik.

**Acceptance:** split durasi offline vs durasi gabungan menghasilkan kejadian yang sesuai aturan yang sama; tidak ada drift tambahan karena minimize/reopen; tidak ada fill transaksi tanpa kewajiban kas/saham yang sah; A05 tetap diuji setelah perubahan.

### B03 — Preferensi offline menyediakan pause yang dilarang

**Lokasi kode:**

- `android/app/src/main/assets/www/app.js` — baris 8760-8783.
- `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java` — baris 270-273.

**Simbol pencarian:** `openPlayerProfileModal; reconcileOfflineElapsed`.

**Keparahan:** Tinggi. **Status:** TERBUKTI_STATIS.

**Bukti UI:** `assets/www/app.js:8760-8765` membuat checkbox dan teks bahwa simulasi membeku bila dimatikan; `8775-8783` mengirim `offlineProgressEnabled` ke native. **Bukti domain:** `core/facade/TradingFacade.java:270-273` mengembalikan null tanpa memajukan dunia lalu memajukan anchor ke sekarang ketika preferensi false.

```javascript
offlineProgressEnabled: offlineProgressCheck.checked
```

```java
if (!playerProfileManager.isOfflineProgressEnabled()) {
    lastRealTimeAnchor = Math.max(lastRealTimeAnchor, realTimeClock.millis());
    return null;
}
```

**Dampak:** pemain dapat membekukan waktu dengan preferensi. Konfirmasi pemilik pada chat ini menetapkan dunia tetap 48x tanpa pause, jadi ini bukan keputusan balancing yang boleh dipertahankan sebagai asumsi baru. A07 tetap masalah terpisah karena juga terjadi ketika toggle true.

**Reproduksi:** Pengaturan -> matikan checkbox offline -> simpan -> background/tutup -> kembali setelah jeda QA terukur; periksa kalender dan operasi harian.

**Perbaikan:** domain harus menaati aturan 48x meski save lama memuat false. Tentukan penanganan field legacy tanpa menghapus/migrasi save pemain secara diam-diam; perubahan checkbox/teks perlu preview dan persetujuan. Jangan mengubah kebijakan hanya di UI sementara native tetap menerima false.

**Acceptance:** preferensi/save legacy false tidak menghentikan waktu yang diwajibkan; elapsed tetap diproses sekali; preferensi aksesibilitas lain tetap utuh.

### B04 — Error pembatalan auto-renewal hilang karena target DOM sudah dilepas

**Lokasi kode:**

- `android/app/src/main/assets/www/app.js` — baris 3940-3948, 5229-5256, 5275-5281.

**Simbol pencarian:** `openResearchTiersModal, showConfirmModal, showFlow, closeFlow`.

**Keparahan:** Sedang. **Status:** TERBUKTI_STATIS pada jalur error.

**Bukti:** `assets/www/app.js:3940-3948` membuka `showConfirmModal`, lalu catch menulis `flowText(card, 'Gagal', error.message)`. `showConfirmModal:5275-5280` memakai `showFlow` dan menutup flow sebelum callback. `showFlow:5230-5231` menghapus flow lama; `card` paket langganan sudah detached saat catch berjalan.

```javascript
showConfirmModal(..., () => {
    try { /* native cancel */ }
    catch (error) { flowText(card, 'Gagal', error.message); }
});
// Helper mengganti parent flow lama:
document.querySelectorAll('.flow-page').forEach(fp => fp.remove());
```

**Dampak:** bila native mengembalikan gagal atau save gagal, pemain keluar dari konfirmasi tanpa melihat penjelasan; pesan ditambahkan ke elemen yang tidak terlihat. Keberhasilan tetap membuka kembali daftar paket; jangan menyebut happy path selalu gagal.

**Reproduksi:** di host QA, buat `cancelTerminalAutoRenewal` mengembalikan JSON success:false atau error save; buka paket berbayar auto-renew aktif -> hentikan -> konfirmasi. Periksa pesan pada elemen yang `isConnected` true.

**Perbaikan minimum:** gunakan `showGameToast('error', ...)` yang sudah ada dan/atau buka kembali paket lalu tampilkan status pada panel baru. Jangan mengirim mutasi ulang dengan ID baru bila kegagalan penyimpanan membuat hasil belum pasti.

**Acceptance:** pesan gagal benar-benar terlihat/terbaca; pengguna tahu cara memeriksa status; tidak ada debit/pembatalan ganda; Back dan fokus kembali masuk akal.

### B05 — Counter koleksi memakai nama method/shape response yang tidak ada

**Lokasi kode:**

- `android/app/src/main/assets/www/app.js` — baris 2533-2540.
- `android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java` — baris 1100-1107.

**Simbol pencarian:** `loadFirmaView; TradingWebBridge.getCollections`.

**Keparahan:** Rendah. **Status:** TERBUKTI_STATIS.

**Bukti:** `assets/www/app.js:2533-2540` mengecek/memanggil `bridge.getCollectionItems()` dan membaca `colData.items`. Tidak ada @JavascriptInterface bernama itu. Method aktual `mobile/TradingWebBridge.java:1100-1104` adalah `getCollections()` dengan payload `{success:true, collections:[...]}`; renderer koleksi lain sudah memakainya.

```javascript
if (bridge.getCollectionItems) {
    const colData = JSON.parse(bridge.getCollectionItems() || '{}');
    const ownedCount = (colData.items || []).filter(x => x.owned).length;
}
```

**Dampak:** guard melewati update counter tanpa error. Tombol Koleksi sendiri mempunyai handler; ini counter diam, bukan bukti seluruh galeri tidak bisa dibuka.

**Perbaikan:** gunakan `getCollections`, periksa `success`, lalu `collections`. Jangan menambah endpoint alias native untuk mempertahankan typo frontend.

```javascript
const colData = JSON.parse(bridge.getCollections());
if (colData.success === false) throw new Error(colData.message);
const ownedCount = (colData.collections || []).filter(x => x.owned).length;
```

**Acceptance:** hitungan awal, setelah beli/jual, setelah reload dan error endpoint benar; backend tidak ditambah API duplikat.

### B06 — Tombol lanjut nominal invalid berhenti tanpa respons yang dapat dipahami

**Lokasi kode:**

- `android/app/src/main/assets/www/app.js` — baris 5258-5273, 4484-4494.

**Simbol pencarian:** `showPromptModal; renderKeuanganBank`.

**Keparahan:** Rendah. **Status:** TERBUKTI_STATIS.

**Bukti:** `assets/www/app.js:5258-5272`, dipakai pembukaan deposito pada `4484-4487`.

```javascript
const val = input.value.trim();
if (!val || Number(val) <= 0) return;
```

Tidak ada form submit/native validation atau status error pada return tersebut.

**Dampak:** kosong/0/negatif -> tekan Lanjutkan Transaksi -> modal diam. Domain tetap menjaga uang; ini kegagalan feedback, bukan penerimaan transaksi invalid.

**Perbaikan minimum:** gunakan validasi input native dan feedback yang tersedia; cek finite dan positif sebelum callback. Contoh arah:

```javascript
const amount = Number(input.value);
input.setCustomValidity(Number.isFinite(amount) && amount > 0 ? '' : 'Isi nominal yang lebih besar dari nol.');
if (!input.reportValidity()) return;
```

Pastikan pesan invalid dihapus/dihitung ulang pada input berikutnya; minta persetujuan teks UI yang akan diterapkan.

**Acceptance:** kosong/0/negatif menampilkan alasan serta fokus yang jelas; nominal sah melanjutkan sekali; tidak ada request native saat invalid.

### B07 — Teks prestige bertentangan dengan manfaat yang dinyatakan domain

**Lokasi kode:**

- `android/app/src/main/assets/www/app.js` — baris 4734, 4767-4770, 4788.
- `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java` — baris 4557-4566, 4587-4593.

**Simbol pencarian:** `openPrestigeModal`, `loadRisetView`, `getPrestigeStatus`, `confirmPrestige`.

**Keparahan:** Sedang. **Status:** TERBUKTI_STATIS sebagai ketidaksesuaian kontrak teks; bukan bukti runtime seluruh mekanisme prestige.

**Bukti:** pengantar UI menyatakan multiplier reputasi, XP lebih cepat, serta modal awal lebih besar. Respons domain justru memberi `prestigeBonus = "Badge kosmetik Era Baru"` dan reset kas Rp50 juta; konfirmasi menginisialisasi kas Rp50 juta. Baris UI 4788 memakai respons domain untuk satu baris bonus, tetapi tidak menghilangkan pengantar yang bertentangan.

```javascript
flowText(panel, 'Informasi Era Baru', 'Masuki Era Baru untuk mereset perjalanan aktif dan membuka multiplier reputasi, perolehan XP lebih cepat, serta modal awal yang lebih besar.');
```

```java
.put("prestigeBonus", "Badge kosmetik Era Baru")
.put("reset", "Kas kembali Rp50 juta; ...")
initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
```

**Dampak:** pemain dapat menyetujui reset perjalanan dengan harapan manfaat yang berbeda dari ringkasan aturan domain. Tidak ada alasan membangun bonus ekonomi baru hanya untuk membenarkan teks UI.

**Reproduksi:** buka Lainnya -> Warisan Prestise; bandingkan pengantar, baris Bonus Era Baru, dan dampak reset dengan payload `getPrestigeStatus()`. Konfirmasi reset hanya pada fixture QA dan hanya bila diperlukan; kontradiksi teks sudah dapat diperiksa tanpa mereset perjalanan.

**Perbaikan:** gunakan `pres.description`, `pres.prestigeBonus` dan aturan reset domain sebagai sumber presentasi; singkirkan janji manfaat yang belum disahkan/diimplementasikan. Bila pemilik memang menginginkan multiplier, minta keputusan produk terpisah sebelum mengubah ekonomi. Preview teks baru tetap diperlukan.

**Keputusan pemilik Q06:** pertahankan badge kosmetik dan reset kas Rp50 juta; perbaiki teks UI. Tidak ada mandat menambahkan multiplier reputasi/XP, bonus modal, atau bonus ekonomi permanen dari sesi ini. Teks pengganti tetap perlu preview sebelum diterapkan.

**Acceptance:** judul menu, pengantar, bonus, dampak reset, dan konfirmasi konsisten; tidak ada janji XP/reputasi/modal tambahan tanpa aturan dan bukti implementasi; backup dan proteksi prestige existing tidak berubah.

## 6. Hasil pemeriksaan sambungan UI dan kandidat yang ditolak

Pemeriksaan source yang benar-benar dilakukan:

- `node --check android/app/src/main/assets/www/app.js`: exit 0, tidak ada error sintaks. Diulang saat verifikasi final; ini tidak membuktikan resolusi nama runtime.
- Parser Acorn bawaan Node (read-only, `--expose-internals`) membangun AST 10.605 baris JS; analisis scope membandingkan 13.900 referensi identifier dalam 2.627 scope. Angka ini ukuran pemeriksaan, bukan jumlah test yang lulus.
- Inventaris direct native calls: 191 call, 157 nama method native unik yang ditemukan dari @JavascriptInterface. Tidak ditemukan mismatch jumlah argumen pada direct calls. Satu nama method tidak tersedia: `getCollectionItems`, sudah ditangani B05.
- 23 call `bridgeResult('nama-literal', ...)` dicocokkan terhadap nama @JavascriptInterface; tidak ditemukan nama hilang. Dua jalur `bridge[method]` juga ditelusuri ke helper bridgeResult dan operasi risiko; ini bukan bukti semua payload valid runtime.
- HTMLParser stdlib membaca 42 elemen button pada index.html. Handler langsung, handler class nav/side/pct, ID filter yang dibangun dinamis, dan subtab ditelusuri. Hasil ini tidak mencakup semua tombol dinamis yang dibuat setelah runtime.
- Filter chips pasar ternyata memiliki handler ID dinamis pada `app.js:9519-9533`; tidak dilaporkan sebagai tombol mati hanya karena pencarian literal ID tidak menemukannya.
- `appendPrestigeSection` tidak didefinisikan dan ada call pada `app.js:2501`, tetapi dibungkus `if (overview)` untuk ID `career-overview` yang tidak ada pada index.html dan tidak ditemukan dibuat oleh JS. Dicatat sebagai kode sisa/risiko bila elemen ditambahkan; TIDAK dihitung sebagai kerusakan tab Bisnis sekarang.
- `getToastHistory` disediakan lewat `window.getToastHistory` pada `app.js:591`; unqualified lookup browser tidak otomatis berarti fungsi hilang. Kandidat ReferenceError ini ditolak.
- `showPromptModal` dan `showConfirmModal` benar-benar didefinisikan pada `app.js:5258,5275`; keduanya bukan fungsi hilang. Masalahnya dijelaskan B04/B06.
- Native `skipDay` sudah menolak skip; laporan tidak meminta perbaikan ulang jalur yang sudah menolak. A08 menyasar endpoint skip sesi yang masih berjalan.

Perlindungan yang ditemukan dan tidak boleh dihapus saat memperbaiki:

- BigDecimal untuk uang, saldo tersedia sebelum debit/reservasi, validasi lot/harga/currency, deteksi request ID dengan payload berbeda.
- Subscription fund mereservasi kas; jangan menambahnya lagi ke net worth sebagai aset baru di luar total cash.
- Quote FX mengecek expiry dan kas tersedia; pinjaman reviewed mengecek quote/payload.
- Cancel auto-renewal mempertahankan akses sampai expiry; jangan mengembalikan bug debit ulang langganan.
- Save memakai tmp/fsync/backup; prestige membuat backup sebelum reset dan mencoba rollback.
- Batas akses akun native menolak pengoperasian akun internal oleh pemain.

## 7. Urutan verifikasi dan bukti penyerahan agent

1. Revalidasi ID yang dikerjakan pada snapshot baru; catat hash source dan status Git.
2. Untuk B01, lakukan minimal click-through beli, jual, IPO, live detail dan pemeriksaan console/pageerror. Periksa error path juga; sintaks hijau tidak cukup.
3. Untuk A01-A03, uji kegagalan di boundary sebelum/sesudah mutasi, backup valid/invalid dan save-reload. Jangan menciptakan fixture dari save pemain.
4. Untuk A04/A05/A10, tulis assertion ekonomi yang independen dari implementasi: nilai total tetap saat pindah bentuk aset; harga posisi sama dengan harga instrumen; total debt lintas mata uang tidak melewati plafon.
5. Untuk A06/A07/B02/B03, uji crossing sesi dan waktu dengan fixed clock, durasi gabungan vs durasi terbagi, close/reopen, dan deadline berita nyata. Optimisasi batch tidak boleh membuang kewajiban.
6. Untuk B04/B06, endpoint gagal/input invalid harus menghasilkan feedback terlihat, bukan sekadar logger/elemen detached. Untuk B05/A09, hitungan berubah tepat sekali dan tetap benar setelah reload.
7. Jalankan cek yang sesuai pada lingkungan terisolasi. Build offline di salinan, bila seluruh dependency sudah tersedia, dapat memakai `gradlew.bat --offline --no-daemon testDebugUnitTest assembleDebug`. Perintah ini BELUM dijalankan audit; jangan menjalankannya di repo asli selama hanya audit read-only yang diizinkan.
8. Periksa runner: `Work/run_all_tests.py` hanya memasukkan class test yang sudah mempunyai .class; jangan menyimpulkan full suite dari daftar itu tanpa compile snapshot baru. Dua katalog `src/main/assets/data/market_universe.json` dan `android/app/src/main/assets/data/market_universe.json` berbeda hash; pastikan fixture memakai katalog yang benar. Runner existing `Work/run-codex-jvm-check.ps1:30` menyalin data Android ke fixture-nya, tetapi konfigurasi output runner itu menulis ke repo dan tidak boleh dipakai dalam audit read-only ini.
9. Laporkan hasil host/browser/APK/emulator/perangkat terpisah. Pengujian perangkat tetap membutuhkan izin per tindakan; tidak ada instalasi atau ADB yang diizinkan oleh dokumen ini.

Checklist penyerahan:

- [ ] ID scope yang ditunjuk pemilik dicatat, bukan denominator historis 14/20/126/500.
- [ ] Tidak ada perubahan source/save di luar lingkup.
- [ ] Semua caller fungsi yang diperbaiki ditinjau.
- [ ] Satu regression check bermakna per root cause nontrivial atau kelompok invariant yang sesuai.
- [ ] Preview/persetujuan UI yang diperlukan tersedia sebelum penerapan.
- [ ] Bukti command, input, output dan snapshot baru tersedia.
- [ ] Limitasi runtime/perangkat ditulis jujur.
- [ ] Save pemain hash tetap sama.
- [ ] Tidak ada status PASS yang diturunkan hanya dari dokumen, mock, parse, compile atau keberadaan APK.

## 8. Hal yang tetap belum dibuktikan

Tidak ada click-through browser/WebView atau fault injection pada sesi audit ini. Tidak ada klaim performa P95, heap aktual, kebocoran memori, bebas ANR, atau kompatibilitas font 200%/seluruh perangkat. B01 terbukti pada lexical scope source, sedangkan reproduksi UI tetap harus dilakukan setelah agent memperoleh lingkungan QA yang sesuai. A01/A03 tetap potensial sampai skenario end-to-end dijalankan. Keputusan model suplai market maker, definisi bailout likuiditas vs insolvency, konflik berita dan pembulatan tidak ditentukan sepihak.

## 9. Catatan optimisasi, verifikasi, dan keputusan — jangan hilang dari backlog

Catatan C01-C12 melengkapi seluruh saran nonutama yang pernah disampaikan pada audit. Status di sini harus dipertahankan: pengukuran yang belum dilakukan bukan bukti masalah performa, dan keputusan produk yang belum jelas bukan izin mengubah ekonomi. Masing-masing tetap memiliki lokasi, tindakan, dan bukti penutupan agar dapat ditangani agent.

### C01 — Dua katalog berbeda dapat membuat fixture berbeda dari produksi

**Status:** PERBEDAAN_FILE_TERBUKTI; dampak pada tiap run test PERLU_VERIFIKASI.

**Lokasi kode/data:** `src/main/assets/data/market_universe.json`; `android/app/src/main/assets/data/market_universe.json`; `android/app/src/test/java/com/tradingglobal3d/core/MarketTradingTahap2Test.java:38-42` (pemilihan fixture); `Work/run-codex-jvm-check.ps1:30` (penyalinan data Android ke fixture).

**Bukti:** SHA-256 katalog root `73931424A1DC01B035C32BB88E7E027F0776B1C9094526683829CA0F96339AE0`, katalog Android `3FF2A1F8AE77E983DAC77A36B8316FA6011F7968D5C189D9BAC1CA279BE7FE54`. Test tersebut memilih file root jika ada. Runner Codex menyalin data Android ke fixture-nya; jangan mengklaim semua runner pasti menggunakan data lama.

**Rekomendasi:** tetapkan katalog Android sebagai sumber fixture produksi, catat hash input pada laporan test, dan periksa setiap fallback path. Jangan menghapus/menimpa katalog root tanpa mengetahui pemakainya.

**Verifikasi penutupan:** hash fixture run yang benar-benar dijalankan sama dengan katalog snapshot APK yang diuji; seluruh test tetap ditemukan dan memakai katalog yang dimaksud.

### C02 — Runner dapat melewatkan test yang belum dikompilasi

**Status:** TERBUKTI_STATIS pada pemilihan test; jumlah run yang pernah terdampak tidak diverifikasi.

**Lokasi kode:** `Work/run_all_tests.py:17-29`, khususnya `if os.path.exists(cls_file): tests.append(cls_name)` pada baris 26-27.

**Bukti:** file sumber test hanya dimasukkan bila `.class` sudah tersedia pada direktori output; skrip ini tidak mengompilasi source dalam blok tersebut.

**Rekomendasi:** compile snapshot yang sama sebelum discovery eksekusi; bila test sumber tidak mempunyai class hasil compile yang sesuai, fail atau laporkan SKIPPED eksplisit. Laporkan ditemukan/dikompilasi/dijalankan/dilewati, bukan satu angka kelulusan.

**Verifikasi penutupan:** satu test sumber baru tanpa class tidak dapat terlewat diam-diam; perubahan source memicu kompilasi yang sesuai sebelum kelulusan dilaporkan.

### C03 — Pembaruan DOM satu detik perlu diukur dan dibatasi pada perubahan nyata

**Status:** JALUR_BERULANG_TERBUKTI; dampak performa BELUM_DIUKUR.

**Lokasi kode:** `android/app/src/main/assets/www/app.js:785` (`refreshSnapshot`), `10556-10596` (heartbeat satu detik), khususnya `10562-10567` yang memperbarui snapshot, portofolio, receipt, detail dan quotes.

**Rekomendasi:** ukur durasi bridge/serialisasi/DOM pada layar aktif, bandingkan nilai sebelum menulis DOM, dan gunakan update terarah yang sudah tersedia. Jangan mengubah tempo domain 48x atau mengurangi kebenaran waktu demi frame rate.

**Verifikasi penutupan:** bandingkan profil sebelum/sesudah pada perangkat/lingkungan yang sama; fokus, input, posisi scroll dan data aktual tetap benar. Tidak ada klaim P95 atau peningkatan persentase tanpa pengukuran.

### C04 — Aset dashboard audit ikut masuk direktori produksi

**Status:** UKURAN_FILE_TERBUKTI; dampak ukuran APK setelah kompresi BELUM_DIUKUR.

**Lokasi:** `android/app/src/main/assets/www/dashboard-25-temuan-lengkap.html`, `dashboard-70-temuan-lengkap.html`, `dashboard-100-temuan-lengkap.html`, `dashboard-160-temuan-lengkap.html`, `dashboard-200-temuan-lengkap.html`, `dashboard-300-temuan-lengkap.html`, dan `preview-before-after.html`; konfigurasi assets `android/app/build.gradle:35-40`.

**Bukti:** jumlah tujuh file tersebut 1.732.951 byte mentah (sekitar 1,65 MiB). Ini bukan ukuran APK atau jaminan seluruh byte itu terpakai pada suatu APK lama.

**Rekomendasi:** setelah memastikan tidak ada caller produksi, kecualikan dashboard/preview dari packaging release. Pertahankan bukti di repo; jangan menghapus file audit untuk sekadar mengurangi APK.

**Verifikasi penutupan:** daftar entry APK release tidak memuat aset audit; index/app/style dan aset yang dipakai game tetap tersedia; catat ukuran APK aktual sebelum/sesudah.

### C05 — Planner subset yang tidak dipanggil merupakan kandidat penghapusan

**Status:** TIDAK_ADA_CALLER_AKTIF_DITEMUKAN; bukan bug freeze terbukti.

**Lokasi kode:** `android/app/src/main/java/com/tradingglobal3d/core/trading/MatchingEngine.java:391-497`, simbol `planFillsForPlayerOrder` dan `searchSubset`.

**Bukti:** pencarian caller hanya menemukan deklarasi planner dan rekursi/helper yang dipanggil dari planner itu. Jalur submit aktif memakai metode lain.

**Rekomendasi:** cari ulang semua caller pada snapshot agent; hapus kode mati jika benar tidak diperlukan. Jangan menghidupkan rekursi subset sebagai solusi A01 tanpa analisis aturan AON dan biaya pencarian.

**Verifikasi penutupan:** compile dan regression matching aktif tetap benar; tidak mengklaim peningkatan runtime dari penghapusan jalur yang memang tidak dijalankan.

### C06 — Riwayat ledger dan serialisasi save memerlukan benchmark perjalanan panjang

**Status:** RIWAYAT_TERSIMPAN_TERBUKTI; bottleneck/memory leak BELUM_DIUKUR.

**Lokasi kode:** `android/app/src/main/java/com/tradingglobal3d/core/economy/EconomyLedger.java:15` (`entries`), `91-93` (`getEntriesForAccount`); `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2479-2549` (`save`), `1447-1457` (pembacaan ledger untuk progres).

**Bukti:** daftar seluruh entry dipertahankan; pembaca akun membuat salinan daftar; save mengekspor banyak domain. Cap cache request idempotency bukan cap seluruh riwayat ledger.

**Rekomendasi:** ukur ukuran save, waktu snapshot/ekspor, penulisan, load dan heap pada perjalanan panjang. Optimalkan salinan/ekspor yang terbukti dominan. Jangan memangkas ledger/receipt hingga merusak rekonsiliasi atau idempotency, dan jangan menambah database baru tanpa kebutuhan terukur.

**Verifikasi penutupan:** hasil benchmark terikat hash source dan dataset QA; saldo dan idempotency sama sebelum/sesudah; save panjang tetap dapat dimuat.

### C07 — Lifecycle cleanup WebView/worker perlu diuji

**Status:** PERLU_VERIFIKASI_RUNTIME; tidak dilaporkan sebagai memory leak terbukti.

**Lokasi kode:** `android/app/src/main/java/com/tradingglobal3d/mobile/MainActivity.java:28,88,208-241` (WebView/lifecycle); `android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java:48-52` (`clockWorker`). Tidak ditemukan override `onDestroy`/pemanggilan shutdown pada kedua file saat pemeriksaan.

**Rekomendasi:** profilkan buka-tutup Activity berulang dan periksa thread, referensi, serta pekerjaan save yang masih berjalan. Jika perlu cleanup, hentikan worker pada pemilik lifecycle yang benar dan lepaskan WebView setelah operasi penting diselesaikan/ditangani; jangan membatalkan save begitu saja.

**Verifikasi penutupan:** thread/heap tidak terus tumbuh, tidak ada task mengakses WebView yang telah dihancurkan, progres tersimpan dan resume tetap benar. Tindakan emulator/perangkat perlu izin yang berlaku.

### C08 — Banner kegagalan save dapat tertinggal setelah autosave berikutnya sukses

**Status:** TERBUKTI_STATIS untuk perbedaan jalur UI; reproduksi error/sukses berurutan BELUM_DIUJI.

**Lokasi kode:** `android/app/src/main/java/com/tradingglobal3d/mobile/MainActivity.java:224-240` (`notifySaveFailed`, `showSaveFailure`); `android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java:91,2282` (notifikasi sukses); `android/app/src/main/assets/www/app.js:10512-10518` (pembuatan banner) dan `8800-8804` (save manual).

**Koreksi penting:** save manual SUDAH menghapus `native-save-warning` pada success di baris 8803. Catatan awal bahwa banner tidak memiliki jalur penghapusan sama sekali terlalu luas. Yang belum melakukan penghapusan adalah jalur notifikasi sukses otomatis: flag native menjadi false tanpa menghapus banner JS yang sudah tampil.

**Rekomendasi:** satukan pemberitahuan status save ke UI; autosave sukses menghapus warning lama tanpa toast sukses berulang. Jangan menyembunyikan warning sebelum penulisan benar-benar berhasil.

**Verifikasi penutupan:** paksa satu kegagalan QA -> banner terlihat -> autosave sukses -> banner hilang; kegagalan berulang tetap terlihat; tidak ada notifikasi sukses yang mengganggu.

### C09 — Suplai uang/saham market maker memerlukan keputusan ekonomi

**Status:** PERILAKU_SOURCE_TERBUKTI; aturan umum, pemicu dan ambang/target kas serta saham DIPUTUSKAN_Q04. Tambahan lewat kejadian tercatat ketika cadangan di bawah ambang: kas tersedia IDR < Rp5 miliar -> target Rp10 miliar; kas tersedia USD < US$500 ribu -> target US$1 juta; persediaan saham bebas per instrumen < 50 lot -> target 2.000 lot. Tiap mata uang/instrumen dievaluasi terpisah dan tambahan hanya selisih menuju target. Pengisian berulang diizinkan ketika cadangan kembali di bawah ambang tanpa jeda minimum; saham tambahan selalu diterbitkan baru dengan dilusi yang diterima pemilik; kas tambahan merupakan suntikan modal eksternal simulasi tanpa utang/bunga. Aturan utama telah diputuskan. Bukan exploit pemain tak terbatas yang sudah dibuktikan.

**Lokasi kode:** `android/app/src/main/java/com/tradingglobal3d/core/npc/NpcAgent.java:61-75`, simbol `generateOrders`, `replenish`, `account.creditCash(minCash)`; `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2463-2470` (pengecualian rekonsiliasi NPC).

**Bukti:** inventory dan kas market maker utama diisi ulang saat melewati ambang; sumber pengisian itu tidak menggunakan ledger pada blok tersebut.

**Rekomendasi sesuai Q04:** pemilik mengizinkan kas dan jumlah saham bertambah melalui kejadian ekonomi yang jelas dan tercatat. Kaitkan tambahan kas pada kejadian dan ledger; tambahan saham pada kejadian perubahan supply yang sah serta pencatatan kepemilikan. Cegah pengulangan kejadian setelah retry/load. Jangan mempertahankan pengisian tanpa asal-usul, menghapus likuiditas secara sepihak, atau mengklaim ekonomi tertutup. Pemicu, besaran target, pengulangan, penerbitan saham dengan dilusi dan suntikan modal kas tanpa utang telah ditetapkan di bagian 11; gunakan keputusan tersebut. Aturan ekonomi tambahan di luar itu tidak boleh dipilih sepihak.

**Verifikasi penutupan:** aturan suplai yang disahkan, invariant ownership/ledger yang sesuai, serta uji likuiditas dan ekonomi jangka panjang. Tidak ada pembelian/layanan eksternal diperlukan.

### C10 — Definisi bailout perlu diselaraskan dengan seluruh aset

**Status:** SYARAT_SOURCE_TERBUKTI; definisi fasilitas DIPUTUSKAN_Q05: hanya saat benar-benar bangkrut. Jangan memutuskan ambang kas atau nominal hibah baru sendiri.

**Lokasi kode:** `android/app/src/main/java/com/tradingglobal3d/core/career/CareerManager.java:217-240` (`requestBailout`); `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:2042-2045`, `3521-3565` (bailout lama vs turnaround).

**Bukti:** bailout lama memeriksa kas IDR tersedia dan apakah map positions kosong; bukan net worth seluruh aset. Fasilitas hanya sekali pakai dan memiliki penalti. Karena itu tidak boleh disebut hibah tak terbatas berdasarkan blok ini.

**Rekomendasi sesuai Q05:** hibah sekali pakai hanya tersedia saat kekayaan bersih tidak positif DAN kas tidak mencukupi. Syarat harus mempertimbangkan saham, USD, deposito, reksa dana, bisnis, koleksi dan seluruh kewajiban secara konsisten setelah A04/A05 diperbaiki. Memindahkan kas ke deposito tidak boleh membuat akun kaya memenuhi syarat bailout. Pertahankan sekali pakai dan penalti yang disahkan; sesi ini tidak memilih angka baru untuk ambang kas atau hibah.

**Verifikasi penutupan:** kasus kas IDR kecil dengan aset lain besar, posisi saham kuantitas nol yang masih tercatat, serta akun benar-benar insolven; hasil harus sesuai keputusan pemilik, bukan asumsi agent.

### C11 — Navigasi yang ada berbeda dari lima tab dalam instruksi desain

**Status:** PERBEDAAN_SOURCE_DOKUMEN_TERBUKTI; PERSETUJUAN_PERUBAHAN_PERLU_VERIFIKASI.

**Lokasi kode/dokumen:** `android/app/src/main/assets/www/index.html:200-213`; `android/app/src/main/assets/www/app.js:106-153` (`switchTab`); `DESIGN.md:3-7` dan bagian arah visual AGENTS.md.

**Bukti:** source menampilkan Beranda, Pasar & Porto, Bisnis, Keuangan, Lainnya. Instruksi desain menyebut Beranda, Pasar, Portofolio, Bisnis, Lainnya. Handler navigasi ditemukan; ini bukan bukti tombol tab mati.

**Rekomendasi:** pemilik/agent memeriksa bukti persetujuan layout gabungan dan tab Keuangan. Bila belum disahkan, siapkan preview pemetaan sesuai keputusan terbaru sebelum mengubah. Jangan mengembalikan navigasi lama diam-diam atau memangkas akses fitur.

**Verifikasi penutupan:** rujukan persetujuan spesifik atau preview yang disetujui, seluruh rute lama tetap terlayani, Back/fokus/teks besar diuji setelah perubahan.

### C12 — Dependensi/config dicatat tanpa tuduhan versi bermasalah yang belum terbukti

**Status:** INVENTARIS_SOURCE; tidak ditemukan konflik dependensi fatal yang terbukti dalam audit ini. Dependency resolution/build BELUM_DIJALANKAN.

**Lokasi kode:** `android/build.gradle` (AGP 8.13.0); `android/gradle/wrapper/gradle-wrapper.properties` (Gradle 8.14.3); `android/app/build.gradle:7-15,30-33,52-54` (SDK, Java 21, JUnit 4.13.2, JSON test 20240303).

**Rekomendasi:** verifikasi dependency resolution offline dan build dengan toolchain yang sesuai pada salinan QA yang diizinkan. Jangan memperbarui semua versi hanya karena terlihat lama atau menganggap dependency test sebagai library tambahan produksi. Jika hendak mengklaim kerentanan, gunakan advisory primer dan buktikan versi/jalur yang relevan terlebih dahulu.

**Verifikasi penutupan:** konfigurasi/toolchain dan output build pada snapshot baru; tidak ada dependency baru atau akses jaringan tanpa kebutuhan/izin.

## 10. Kelengkapan catatan untuk pelaksana

- Semua A01-A10 dan B01-B07 mempunyai lokasi file eksplisit, baris, nama fungsi/simbol pencarian, bukti, dampak, rekomendasi, dan acceptance criteria.
- Semua saran optimisasi awal, keputusan ekonomi, serta koreksi klaim terlalu luas ditempatkan pada C01-C12; statusnya tidak boleh disamakan dengan bug fatal terbukti.
- Kandidat yang ditolak, perlindungan yang harus dijaga, dan batas runtime tetap disimpan pada bagian 6-8 agar agent tidak menghabiskan waktu memperbaiki jalur yang benar.
- Catat kembali nomor baris/hash setelah agent mengubah source. Menandai dokumen ini lengkap berarti catatan audit tersedia, bukan berarti game atau seluruh backlog telah selesai diperbaiki.

## 11. Keputusan sesi 10 pertanyaan — 1 November 2026

### 11A. Rangkuman jawaban pemilik

Catatan ini berasal dari jawaban eksplisit pemilik pada chat yang sama. Sesi tanya jawab merupakan pengambilan keputusan untuk handoff agent, bukan pelaksanaan perbaikan oleh auditor. Nomor Q01-Q10 adalah nomor pertanyaan sesi; tidak mengubah ID A/B/C.

| Pertanyaan | Keputusan / tanggapan pemilik | Status | Kaitan |
|---|---|---|---|
| Q01 — Cakupan tahap perbaikan | Tangani 17 temuan utama beserta catatan tambahan yang sudah memiliki keputusan jelas. | DIPUTUSKAN | A01-A10, B01-B07; C01-C12 sesuai kejelasan keputusan |
| Q02 — Keandalan save | Target autosave stabil. Keputusan darurat eksplisit: blokir transaksi baru ketika penulisan benar-benar ditolak setelah pemulihan otomatis; tetap izinkan melihat data, peringatan dan coba simpan ulang; pertahankan progres di memori serta salinan valid terakhir. Dunia tetap 48x. | TARGET_DAN_KEBIJAKAN_DARURAT_DIPUTUSKAN | A01-A03, B04, C08 |
| Q03 — Bunga deposito | Baru menambah kekayaan bersih saat jatuh tempo dan menjadi hak pemain; sebelumnya hanya estimasi terpisah. Pokok tetap aset. | DIPUTUSKAN | A04, tampilan deposito dan perhitungan kekayaan |
| Q04 — Tambahan kas/saham NPC | Target dasar: IDR < Rp5 miliar -> Rp10 miliar; USD < US$500 ribu -> US$1 juta; saham bebas <50 lot -> 2.000 lot. Tambahan sebesar kekurangan; boleh berulang. Keputusan terbaru: dukungan likuiditas sesuai kebutuhan order besar yang valid, melampaui cadangan dasar bila perlu, tetap AON. Saham selalu penerbitan baru dengan dilusi diterima; kas suntikan modal eksternal simulasi tanpa utang/bunga. | ATURAN_SUPLAI_DASAR_DAN_ORDER_BESAR_DIPUTUSKAN | C09, supply/ledger dan bagian 12 |
| Q05 — Bailout | Hanya saat benar-benar bangkrut: kekayaan bersih tidak positif dan kas tidak mencukupi. | DIPUTUSKAN | C10, A04/A05 sebagai prasyarat valuasi |
| Q06 — Prestige | Pertahankan badge kosmetik dan reset kas Rp50 juta; perbaiki teks UI agar sesuai. | DIPUTUSKAN | B07 |
| Q07 — Market order saat bursa tutup | Terima sebagai antrean untuk sesi berikutnya; eksekusi memakai harga saat bursa buka dengan All-Or-None, harga estimasi tidak dijamin. | DIPUTUSKAN | A01, B01, antrean order/lifecycle bursa |
| Q08 — UI penyelarasan saat kembali | Data terakhir tetap terlihat dengan label sedang diperbarui; transaksi terkunci sementara lalu aktif; panel hilang otomatis setelah pemrosesan dan penyimpanan berhasil. Pilihan desain eksplisit setelah preview: `Kalender dan jam`. | ALUR_DIPUTUSKAN; DESAIN_KALENDER_DAN_JAM_DIPILIH | A07, B02, B03 dan presentasi catch-up |
| Q09 — Berita bertentangan | Gabungkan persentase berita yang masih aktif terhadap harga acuan yang sama: +5% dan -3% menjadi target bersih +2% selama keduanya aktif. | DIPUTUSKAN | ResearchService, NewsPriceTarget, interaksi harga |
| Q10 — Pembulatan target berita | Bulatkan searah target: target naik ke atas, target turun ke bawah pada fraksi harga sah. Contoh Rp9.500 +3% = Rp9.785 -> Rp9.800 jika fraksi Rp25. | DIPUTUSKAN | Pembentukan target berita dan fraksi harga |

### 11B. Arahan penerapan dan kriteria verifikasi

**Q01 — cakupan:** jalankan perbaikan dalam urutan prioritas yang dicatat, setelah lingkup/plan pelaksana ditunjuk sesuai aturan repo. Catatan dengan kebutuhan keputusan spesifik tetap tidak boleh diputuskan sepihak. Pilihan cakupan ini tidak mengizinkan ADB, instalasi, publikasi atau perubahan UI tanpa persetujuan yang berlaku.

**Q02 — target save andal dan kebijakan darurat sudah diputuskan:** gunakan penulisan atomik, backup tervalidasi, pemulihan dan retry yang menjaga progres. Buktikan tutup paksa, background/resume dan reload tanpa kehilangan progres pada lingkungan QA yang sesuai. Pengalaman pemilik merupakan target kualitas dan UX; jangan mengubahnya menjadi klaim bahwa sistem ini sudah lulus pengujian. Setelah pemulihan otomatis dicoba tetapi perangkat benar-benar masih menolak penulisan, blokir transaksi baru; pemain tetap boleh melihat data, menerima peringatan yang jelas, dan mencoba simpan ulang. Pertahankan progres terbaru di memori dan salinan valid terakhir di disk. Dunia tetap berjalan 48x, sehingga pembatasan input transaksi bukan pause simulasi. Jangan menghapus state memori dengan memuat checkpoint lama sebagai retry otomatis atau menampilkan sukses palsu.

**Penerapan dan acceptance Q02:** guard transaksi baru harus berlaku pada native/domain boundary bersama, termasuk caller selain tombol yang dinonaktifkan. Retry menyimpan state terkini tanpa mengulangi mutasi uang/aset yang sudah diterima. Buka kembali transaksi hanya setelah penyimpanan state yang relevan benar-benar berhasil; hapus warning lama tanpa toast sukses berulang (C08). Uji kegagalan penulisan -> penolakan transaksi baru tanpa mutasi -> data tetap terbaca -> retry gagal tetap terlindungi -> retry berhasil -> transaksi tersedia; cek salinan valid tetap dapat dimuat, request tidak tergandakan dan tidak ada rollback progres memori diam-diam. Progres yang hanya ada di memori belum boleh disebut tersimpan tahan tutup paksa.

**Q03 — deposito:** saat penempatan, kas berkurang dan pokok deposito bertambah dengan nilai sama; net worth tidak turun akibat perpindahan aset. Bunga berjalan boleh ditampilkan sebagai estimasi, tetapi tidak memengaruhi net worth, return, plafon atau eligibility sebelum menjadi hak pemain. Pada jatuh tempo, akui pokok dan bunga yang menjadi hak sesuai kontrak existing tepat sekali; pada pencairan dini, estimasi bunga hangus tidak boleh sebelumnya dianggap pendapatan yang sudah dimiliki. Periksa pajak/biaya existing tanpa memilih tarif baru.

**Q04 — kejadian suplai NPC:** catat identitas kejadian, asal dan besaran kas/saham, waktu, akun penerima, perubahan saham beredar/kepemilikan serta ledger yang relevan. Gunakan mekanisme share-supply/corporate event dan idempotency existing jika cocok. Verifikasi replay request/save-reload tidak menambah supply dua kali. Angka, pengulangan serta sumber kas/saham kemudian diputuskan pada lanjutan 3-8 di bawah; gunakan rangkuman final Q04. Parameter ekonomi baru di luar keputusan tersebut tetap memerlukan rincian pemilik. Teks/penyajian kejadian untuk pemain memerlukan preview.

**Keputusan lanjutan 3 untuk Q04:** pemilik secara eksplisit memilih tambahan dipicu saat cadangan kas atau saham turun di bawah ambang minimum yang akan ditetapkan. Jadwal rutin maupun gabungan jadwal/darurat tidak dipilih. Kekurangan satu jenis cadangan tidak otomatis mengizinkan tambahan keduanya sekaligus; evaluasi kebutuhan kas dan saham masing-masing agar tidak memicu penambahan berulang yang tidak diperlukan. Jawaban pemicu ini semula belum menentukan nominal, target, frekuensi dan mekanisme; keputusan lanjutan 4-8 kini melengkapinya. Jangan menyalin angka source sebagai persetujuan pemilik.

**Keputusan lanjutan 4 untuk Q04 — angka kas dipilih eksplisit:** NPC penyedia likuiditas utama memperoleh pengisian kas ketika kas tersedia (total kas dikurangi reservasi order) **kurang dari** Rp5.000.000.000 untuk IDR atau US$500.000 untuk USD. Target setelah pengisian adalah kas tersedia Rp10.000.000.000 atau US$1.000.000. Periksa mata uang secara terpisah; hanya mata uang yang memenuhi pemicu memperoleh tambahan. Tambahan = target kas tersedia - kas tersedia saat keputusan diterapkan, bukan menambahkan nominal target penuh. Contoh IDR tersedia Rp4,9 miliar -> tambahan Rp5,1 miliar -> tersedia Rp10 miliar. Tepat Rp5 miliar/US$500 ribu tidak memenuhi pemicu karena aturan menggunakan "di bawah". Simpan kejadian serta mutasi ledger tepat sekali dan evaluasi ulang secara atomik agar reservasi bersamaan tidak menyebabkan pengisian ganda. Ini angka awal yang dipilih untuk diuji keseimbangannya, bukan klaim sudah seimbang atau izin mengubah angka tanpa keputusan baru.

**Acceptance angka kas:** uji masing-masing mata uang sedikit di bawah/tepat/di atas ambang, reservasi order besar, kedua mata uang di bawah ambang, hanya satu mata uang di bawah ambang, retry kejadian dan save/reload. Angka cash target adalah kas tersedia; saldo total dapat lebih tinggi karena reservasi yang tetap dipertahankan. Pelepasan reservasi berikutnya bukan alasan menghapus kas pemain/NPC atau memicu pengisian baru di luar kebijakan. Jawaban kas ini sendiri tidak menetapkan angka saham; keputusan saham berikutnya dicatat terpisah di bawah.

**Keputusan lanjutan 5 untuk Q04 — angka saham dipilih eksplisit:** per instrumen yang dilayani NPC penyedia likuiditas utama, jika persediaan saham bebas kurang dari 50 lot, isi hingga persediaan bebas 2.000 lot. Persediaan bebas mengecualikan saham yang terikat order jual atau kewajiban lain. Satu lot mengikuti ukuran lot instrumen, bukan selalu 100 lembar. Tambahan hanya selisih menuju target; tidak menambahkan 2.000 lot penuh setiap pemeriksaan. Contoh ukuran lot 100, persediaan bebas 4.900 lembar (=49 lot): target 200.000 lembar -> tambahan 195.100 lembar. Persediaan bebas tepat 50 lot tidak memicu pengisian. Jangan membulatkan 49,5 lot menjadi 50 saat memeriksa ambang; bandingkan kuantitas lembar dengan hasil perkalian ukuran lot secara aman.

**Acceptance angka saham:** uji bebas 0/49/50/51 lot dan kuantitas pecahan lot yang mungkin berasal corporate action; ukuran lot IDX dan US; sebagian stok terikat order jual/kewajiban pinjam; dua instrumen berbeda; retry serta reload. Target adalah persediaan bebas: total saham akun setelah pengisian dapat lebih besar daripada target karena saham terikat tetap dipertahankan. Jangan menghitung kewajiban dua kali bila kuantitas posisi sudah berkurang akibat peminjaman; telusuri representasi domain aktual. Perubahan supply/ownership harus sah dan kejadian hanya diterapkan sekali. Pengulangan diatur dalam keputusan lanjutan 6; sumber saham ditetapkan pada lanjutan 7 dan sumber kas pada lanjutan 8. Jawaban angka saham ini sendiri bukan dasar mengabaikan pencatatan penerbitan.

**Q05 — bailout:** gunakan kekayaan bersih seluruh aset dikurangi kewajiban dan pemeriksaan kas yang konsisten, bukan hanya kecilnya saldo IDR. Tetap hibah sekali pakai dengan penalti yang telah disahkan. Tes akun berdeposito/bisnis/koleksi bernilai positif tetapi kas tipis harus tidak memenuhi syarat hanya karena tidak likuid. Tidak ada angka ambang baru dari sesi ini.

**Q06 — prestige:** manfaat yang dijanjikan, pengantar, konfirmasi dan hasil harus sama dengan badge kosmetik serta kas reset Rp50 juta. Jangan menambah multiplier XP/reputasi atau modal lebih besar untuk membenarkan copy lama. Backup perjalanan lama dan aturan blocker tetap dijaga. Preview teks baru belum disetujui hanya karena keputusan manfaat sudah jelas.

**Q07 — antrean market order:** pertahankan identitas/type MARKET, request ID dan kondisi antrean lintas save/reload. Jangan diam-diam mengubahnya menjadi limit pada harga saat dimasukkan. Saat sesi bursa terkait buka, lakukan validasi ulang kas, saham, fee dan likuiditas pada harga eksekusi saat itu, lalu preflight AON sebelum mutasi. Mengikuti aturan aktif: bila tidak dapat terisi penuh ketika diproses sebagai market order, batalkan utuh dan lepaskan reservasi yang relevan. Hindari reservasi/debit ganda. Limit order tetap antre OPEN jika belum dapat dipenuhi penuh. Penjelasan harga estimasi tidak dijamin dan keadaan menunggu sesi perlu preview UI.

Pembukaan IDX/US harus menjadi batas kejadian yang diproses ketika ada order menunggu, termasuk jika catch-up melintasi jam buka. Jangan hanya menangani cutoff 16:00 dan midnight lalu kehilangan kesempatan pemrosesan pada sesi pembukaan yang dipilih pemilik. Uji antrean sebelum tutup, save/reload, pembukaan IDX, pembukaan US, likuiditas tidak cukup, kas/fee berubah dan pembatalan sebelum sesi buka.

Lokasi integrasi Q07 yang perlu ditelusuri ulang agent:

- `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:1158-1289`, terutama blok bursa tutup `1252-1284`.
- `android/app/src/main/java/com/tradingglobal3d/core/trading/MatchingEngine.java:133-254` untuk reservasi/market AON dan jalur settlement A01.
- `android/app/src/main/java/com/tradingglobal3d/core/trading/OrderManager.java:50-70` untuk restore order nonfinal; jangan menghasilkan MARKET aktif berharga null yang diperlakukan sebagai resting limit tanpa representasi antrean yang sesuai.
- `android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java:722-777` serta UI tiket/review/receipt pada `android/app/src/main/assets/www/app.js`.

**Q08 — alur diputuskan pada pertanyaan lanjutan:** pemilik secara eksplisit memilih "Lihat data terakhir dengan label sedang diperbarui; transaksi terkunci sementara, lalu aktif setelah selesai" pada pertanyaan yang juga menetapkan panel hilang otomatis setelah pemrosesan DAN penyimpanan sukses, tanpa notifikasi sukses berulang. Ini menggantikan status Q08 yang semula belum dipilih. Data terakhir boleh dibaca selama proses; jangan menyajikannya sebagai data terbaru. Seluruh durasi tetap diproses; tampilan tidak boleh membuang waktu offline.

**Pilihan animasi pemilik setelah preview:** `Kalender dan jam`, melalui jawaban eksplisit pada pertanyaan pilihan desain untuk agent. Kalender berganti hari dan jam mengejar timestamp yang sedang diproses; ini animasi visual penyelarasan, bukan kontrol speed/pause/skip dunia. Laju domain tetap 48x. Varian `Jam ringkas` tetap referensi alternatif, bukan pilihan akhir. Dasar keputusan ini adalah jawaban pemilik, bukan sekadar pergantian varian di preview. Source game belum diimplementasikan oleh auditor; persetujuan ini terbatas pada komponen penyelarasan yang diperlihatkan.

**Arahan untuk rancangan animasi:** kaitkan tanggal/jam/progress pada timestamp dan pekerjaan yang benar-benar sudah diproses domain. Publikasikan status kemajuan yang aman antar-thread bila bridge belum menyediakannya; jangan memaksa UI membaca seluruh snapshot lewat lock yang sedang dipakai catch-up. Jangan membuat loop kalender tak berujung atau persen palsu untuk menyamarkan proses macet. Saat waktu target tercapai tetapi save masih berlangsung, tampilkan tahap menyimpan dan pertahankan penguncian transaksi. Sembunyikan panel hanya setelah hasil penyimpanan sukses dikonfirmasi, lalu tampilkan snapshot terbaru. Bila pekerjaan selesai cepat, animasi tidak boleh sengaja memperpanjang waktu tunggu. Hormati preferensi reduced-motion: pembaruan angka/tanggal tetap terbaca tanpa efek gerak cepat. Source game belum diubah oleh contoh ini.

Referensi preview Q08 (di luar repo produksi, seluruh angka merupakan data simulasi):

- `C:/Users/HYPE AMD/.codex/visualizations/2026/11/01/01a1964c-3cf3-7302-9ed3-74392fa65ea2/preview-penyelarasan-game.html`.
- Nama varian: `Lihat data terakhir` dan `Layar penyelarasan`.
- Host preview saja telah diperiksa: navigasi lokal, transaksi terkunci/aktif, pergantian kondisi selesai, viewport 320/390/736px, teks contoh diperbesar, dan tidak ada pageerror pada cek tersebut. Ini bukan bukti browser game, APK, emulator atau perangkat game telah diuji.
- Mode Anti Slop DURING dipilih untuk sesi ini; tidak mengubah preferensi global dan tidak merupakan persetujuan implementasi UI.

Referensi tambahan preview animasi Q08:

- `C:/Users/HYPE AMD/.codex/visualizations/2026/11/01/01a1964c-3cf3-7302-9ed3-74392fa65ea2/preview-penyelarasan-animasi.html`.
- Seluruh data dan durasi merupakan simulasi preview; tombol `Putar contoh penyelarasan` bukan kontrol waktu game.
- Pemeriksaan host contoh: kalender/jam bergerak menuju Hari 29 08.30, transaksi tetap terkunci termasuk saat tahap menyimpan, panel menghilang setelah sukses simulasi dan tombol aktif; kedua varian diperiksa pada 320/390/736px tanpa pageerror. Hasil ini tidak membuktikan catch-up atau autosave game produksi.

**Q09 — konflik berita:** jumlahkan persentase bertanda dari berita yang masih aktif untuk target terkait, menggunakan acuan yang sama; jangan mencampur target harga dari acuan berbeda lalu menganggapnya penjumlahan persentase. Contoh yang disepakati +5% + (-3%) = +2%, bukan perkalian berurutan 1,05 x 0,97. Simpan data yang diperlukan agar reload menghasilkan target sama dan perubahan himpunan berita aktif tidak menggandakan efek. Uji berita positif/negatif, salah satu efek kedaluwarsa, save/reload dan perpindahan hari. Deadline nyata 60/120 menit dan batas harian yang telah disahkan tetap berlaku; keputusan ini tidak mengizinkan menimpa atau memperpanjangnya. Detail harga acuan lintas hari yang belum ditetapkan tidak boleh ditebak.

**Q10 — fraksi harga:** bentuk target dari acuan dan persentase bersih yang disepakati, lalu bulatkan searah target memakai fraksi instrumen/bursa yang sah. Untuk target naik, gunakan harga sah terdekat di atas atau tepat target; untuk target turun, gunakan harga sah terdekat di bawah atau tepat target. Jika target sudah tepat fraksi, jangan geser lagi. Jika efek bersih nol, jangan menciptakan arah baru dari pembulatan. Periksa perubahan ukuran fraksi di ambang harga dan hindari NaN/harga nonpositif. Jangan melewati band harian yang berlaku untuk memaksakan target; jika target tidak dapat dieksekusi pada hari itu, ikuti aturan tenggat/lintas hari yang sudah disahkan dan tandai detail yang masih menunggu keputusan.

Lokasi awal Q09/Q10 untuk revalidasi, bukan jaminan seluruh caller sudah tercakup:

- `android/app/src/main/java/com/tradingglobal3d/core/research/ResearchService.java` — cari `getActiveNewsImpact`, `getActiveNewsTarget`, `evaluateNewsTargets`, pemilihan target dan export/restore state.
- `android/app/src/main/java/com/tradingglobal3d/core/research/NewsPriceTarget.java` — pembentukan target, acuan, arah, status dan persistence.
- `android/app/src/main/java/com/tradingglobal3d/core/market/Instrument.java:212-235,250-265` — band dan helper fraksi `getIdxTickSize`/`roundUpToIdxTick`; jangan memakai pembulatan naik untuk target turun.
- `android/app/src/main/java/com/tradingglobal3d/core/market/MarketExchange.java` — `getTickSize`; periksa konsistensi fraksi pada batas harga.
- Semua caller target pada facade dan `android/app/src/main/java/com/tradingglobal3d/core/npc/MarketEngine.java` harus ditelusuri sebelum perbaikan shared logic.

### 11C. Keputusan lanjutan suplai NPC dan hal yang masih terbuka

**Keputusan lanjutan 6 untuk Q04 — pengulangan:** jawaban pemilik atas pilihan frekuensi adalah "sering saja tidak apa apa". Dalam kelanjutan sesi, auditor memaknainya sebagai izin pengisian kembali setiap cadangan benar-benar kembali di bawah ambang, tanpa cooldown minimum atau kuota harian baru. Pengisian tetap bersyarat dan hanya selisih menuju target; ini bukan penambahan tanpa henti pada setiap frame/tick. Jangan memberi cooldown/kuota baru tanpa keputusan pemilik. Periksa kondisi dan terapkan event secara atomik; pemeriksaan simultan/retry/reload untuk kejadian yang sama harus tepat sekali. Bila setelah pengisian belum ada perubahan cadangan, pemeriksaan berikutnya tidak boleh menambah lagi. Saat ada penurunan baru di bawah ambang akibat order/kewajiban, kejadian baru dapat dipicu. Izin pengulangan ini dilengkapi keputusan sumber saham dan kas pada lanjutan 7-8; jangan menambah jenis penerbitan/kewajiban ekonomi lain di luar keputusan tersebut.

**Acceptance pengulangan:** beberapa pemeriksaan tanpa perubahan saldo/posisi menghasilkan satu pengisian saja; konsumsi cadangan berikutnya di bawah ambang memungkinkan kejadian baru, termasuk pada hari game yang sama. Retry save/reload tidak menggandakan kejadian lama. Catat jumlah tambahan kumulatif dan uji tidak ada loop karena nilai bebas/kewajiban salah dihitung. Persetujuan frekuensi bukan bukti model sudah seimbang; sumber kejadian dan dampak ownership mengikuti keputusan lanjutan 7-8.

**Keputusan lanjutan 7 untuk Q04 — sumber saham:** jawaban eksplisit pemilik: "Selalu terbitkan saham baru untuk memenuhi kekurangan menuju target 2.000 lot; saya menerima pengenceran kepemilikan." Setiap pengisian saham NPC utama menggunakan penerbitan saham baru sebesar kekurangan persediaan bebas menuju target, sesuai ambang dan pengulangan yang sudah dipilih. Tidak memakai transfer dari pemegang lain/cadangan saham lama sebagai pengganti tanpa keputusan baru. Pemegang lama termasuk pemain tetap memiliki jumlah lembar yang sama, tetapi persentase kepemilikannya dihitung ulang terhadap jumlah saham beredar yang bertambah. Jangan mempertahankan denominator lama, mengurangi lembar pemain, atau menerapkan stock split untuk menyamarkan penerbitan ini.

**Penerapan sumber saham:** identitas kejadian persisten mengikat instrumen, akun NPC penerima, timestamp game, jumlah lembar baru dan supply sebelum/sesudah. Tambahkan tepat satu kali ke issued shares dan posisi NPC melalui mekanisme share-supply yang sesuai; treasury shares hanya berubah jika memang ada aksi lain yang sah. Supply dan posisi penerima harus diterapkan konsisten, bukan posisi bertambah tanpa issued shares atau sebaliknya. Source event kas dan saham berbeda; jawaban ini tidak menetapkan pembayaran/langganan, harga penerbitan atau debitur baru. Jangan diam-diam mendebit kas NPC/pemain atau menambah utang untuk membeli saham baru sebelum mekanisme tersebut disahkan. Model valuasi, harga referensi, dan denominator fundamental yang terpengaruh harus diperiksa; jangan memilih repricing otomatis atau kompensasi pemain sebagai aturan ekonomi baru.

**Acceptance penerbitan/dilusi:** misalnya pemain memegang 1.000 dari 10.000 lembar beredar (=10%); penerbitan 2.000 lembar kepada NPC membuat beredar 12.000, pemain tetap 1.000 lembar dan porsinya sekitar 8,3333%. Gunakan contoh fixture ini untuk memeriksa rumus, bukan memaksakan jumlah penerbitan 2.000 lembar pada semua instrumen (target sebenarnya 2.000 LOT bebas). Pastikan issued/treasury/outstanding dan posisi NPC konsisten, indikator ownership serta syarat berbasis persentase menggunakan denominator baru, retry/load tidak menerbitkan ulang, dan pengujian overflow menolak sebelum mutasi parsial. Audit ekonomi tidak boleh lagi mengklasifikasikan dilusi yang disetujui ini sebagai penerbitan tidak sah semata-mata karena jumlah saham bertambah; yang tetap wajib adalah pencatatan, konsistensi dan penerapan tepat sekali.

**Keputusan lanjutan 8 untuk Q04 — sumber kas, dipilih eksplisit:** gunakan suntikan modal fasilitas likuiditas pasar simulasi, tanpa utang, bunga atau kewajiban pengembalian. Catat sebagai arus dana eksternal ke akun NPC penyedia likuiditas utama. Tetap hanya dipicu saat kas tersedia mata uang terkait di bawah ambang; tambahan sebesar selisih menuju target yang telah disahkan. Tidak mengubah kepemilikan pemain dan tidak langsung mengkredit kas pemain. Kejadian kas ini bukan pinjaman, bukan pendapatan trading, bukan setoran pemain dan bukan tambahan modal pada emiten saham pemain.

**Penerapan sumber kas:** gunakan jenis mutasi ledger existing yang semantiknya benar bila tersedia; jika tidak, perubahan minimum pada jenis mutasi harus menjelaskan asal dana eksternal secara eksplisit dan tetap dapat di-save/load. Jangan memalsukan debit dari akun sumber yang sebenarnya tidak mempunyai dana hanya untuk menyamarkan suplai eksternal. Sumber fasilitas adalah mekanisme simulasi, bukan koneksi layanan keuangan nyata. Identitas kejadian, currency, amount, akun penerima, timestamp, saldo sebelum/sesudah dan receipt tepat sekali harus persisten. Jangan membuat sistem pinjaman/bunga/pelunasan NPC baru atau mengambil dana pemain tanpa mandat. Ikuti Q02 bila penyimpanan gagal, dengan retry receipt yang sama tanpa penambahan kas kedua.

**Acceptance sumber kas:** contoh IDR tersedia Rp4,9 miliar -> event eksternal Rp5,1 miliar -> tersedia Rp10 miliar, utang/bunga tetap tidak bertambah dan kas pemain tidak berubah. Mata uang USD diuji terpisah. Verifikasi ledger mengakui aliran dana eksternal, retry/save-reload tepat sekali, reservasi tetap utuh, saldo tersedia di ambang tidak memicu, dan pengisian baru hanya setelah kebutuhan baru yang memenuhi kondisi. Uji kekayaan pribadi tidak naik hanya karena fasilitas mendanai NPC. Pasokan uang game boleh bertambah melalui mekanisme yang dipilih; jangan mengklaim ekonomi tertutup atau keseimbangan teruji. Ukur tambahan kumulatif dan uji round-trip trading agar fasilitas tidak menutupi bug penciptaan aset/settlement.

**Ringkasan final Q04:** (1) pemicu di bawah ambang, bukan jadwal; (2) IDR tersedia < Rp5 miliar -> target Rp10 miliar; (3) USD tersedia < US$500 ribu -> target US$1 juta; (4) saham bebas per instrumen <50 lot -> target 2.000 lot; (5) tambahan hanya selisih menuju target; (6) boleh berulang ketika kembali menipis tanpa jeda minimum; (7) saham selalu penerbitan baru, dilusi pemegang lama diterima; (8) kas berupa suntikan modal eksternal simulasi tanpa utang/bunga; (9) semua kejadian tercatat, persisten dan diterapkan tepat sekali. Ini keputusan produk untuk pelaksana, bukan bukti implementasi atau hasil uji keseimbangan.

**Pembaruan yang melengkapi ringkasan Q04:** pemicu cadangan minimum di atas tetap berlaku untuk pengisian rutin berbasis kebutuhan cadangan, bukan jadwal. Selain itu, order besar valid dapat memicu dukungan sesuai kebutuhan tanpa harus menunggu saham bebas turun di bawah 50 lot atau kas turun di bawah ambang dasar. Baca bagian 12 sebagai aturan terbaru untuk jalur tersebut; angka dasar bukan batas order atau plafon pengisian khusus order besar.

Q08 sudah mempunyai pilihan alur dan desain `Kalender dan jam`; tidak lagi menunggu pemilihan varian. Penerapan dan pembuktian runtime tetap tugas pelaksana sesuai lingkup yang ditunjuk, bukan hasil yang sudah tercapai dari preview.

Q02 juga sudah memiliki target keandalan dan kebijakan darurat yang dipilih eksplisit; jangan mengajukan ulang opsi fallback yang sama.

Tiga kelompok yang sebelumnya diminta pemilik untuk ditetapkan sekarang sudah mempunyai keputusan: Q08 tampilan penyelarasan, Q02 fallback penyimpanan, serta Q04 pemicu/angka/pengulangan/sumber tambahan kas dan saham. Jangan mengajukan ulang pilihan yang sama atau membiarkan statusnya tertulis belum diputuskan.

1. Detail aturan lama di luar keputusan ini, misalnya acuan berita lintas hari jika belum disahkan, tetap mengikuti keputusan terakhir yang benar-benar tercatat. Jangan menganggap Q09/Q10 menjawab seluruh desain berita. Detail baru yang mengubah valuasi/harga penerbitan atau menambahkan kewajiban pembayaran tidak boleh ditebak.
2. Preview/persetujuan perubahan teks, tampilan, navigasi dan alur lain tetap diperlukan per perubahan. Tidak ada persetujuan perangkat/build/publish baru dari jawaban sesi ini.

**Status penyerahan:** rangkuman 10 topik telah dicatat; pilihan yang belum diberikan tetap ditandai, bukan diisi dengan rekomendasi auditor. Source game, test, konfigurasi dan save pemain tidak diubah oleh penutupan sesi ini. Agent dapat memakai keputusan yang sudah jelas untuk menyiapkan lingkup perbaikan tanpa menebak keputusan yang masih terbuka.

## 12. Pemeriksaan dampak order besar dan sinkronisasi AGENTS.md

**Pemicu tinjauan:** pemilik menanyakan apakah kebijakan cadangan baru menghalangi pembelian 10.000-50.000 lot dan apakah keputusan sudah mengubah AGENTS.md. Bagian ini mencatat konsekuensi kebijakan yang perlu ditentukan, bukan membatalkan keputusan Q04 atau menerapkan batas order baru.

**Bukti source yang diperiksa ulang:**

- `android/app/src/main/java/com/tradingglobal3d/core/trading/MatchingEngine.java:39-55` memeriksa jumlah positif, kelipatan lot, harga dan konteks; blok ini tidak menetapkan batas 2.000 lot per order.
- `android/app/src/main/assets/www/app.js:9467-9496` menerima jumlah lot sebagai bilangan bulat positif yang aman, lalu membentuk kuantitas saham. Ini bukan bukti runtime seluruh jalur order besar telah lulus, khususnya karena B01 masih harus diperbaiki.
- `android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java:1045-1091`, `getOrderCapacity`, mengaitkan kapasitas dengan kas/kepemilikan dan likuiditas untuk jenis order terkait; tombol MAX tidak otomatis menjamin jumlah besar dapat dieksekusi.
- `android/app/src/main/java/com/tradingglobal3d/core/npc/NpcAgent.java:112-145` menempatkan dua level per sisi berukuran 25 lot pada pemanggilan pembentukan quote tersebut. Persediaan akun NPC tidak sama dengan jumlah saham yang benar-benar ditawarkan di buku order; ini tidak membuktikan total buku seluruh NPC hanya 50 lot karena order lain dapat tersedia.
- `android/app/src/main/java/com/tradingglobal3d/core/trading/MatchingEngine.java:221-236` menerapkan AON: market yang tidak dapat dipenuhi penuh dibatalkan, limit menunggu jika likuiditas penuh tidak tersedia.

**Konsekuensi kebijakan Q04:** cadangan bebas target 2.000 lot pada NPC utama bukan batas jumlah order pemain. Namun jika gabungan order lawan yang sah hanya tersedia 2.000 lot, order 10.000 atau 50.000 lot tidak bisa dieksekusi penuh. Order limit dapat tetap menunggu; market dibatalkan utuh. Pengisian ulang tanpa jeda bukan solusi otomatis: jika tidak ada fill karena AON, persediaan belum turun di bawah 50 lot sehingga pengisian tidak terpicu. Likuiditas tambahan dari pemegang/NPC lain mungkin menutup kebutuhan, tetapi tidak dijamin oleh kebijakan ini. Kas NPC target Rp10 miliar juga bukan jaminan dapat membeli penjualan pemain yang lebih besar nilainya; misalnya 50.000 lot IDX x100 saham xRp10.000 bernilai Rp50 miliar sebelum fee.

**Keputusan pemilik setelah penjelasan dampak:** "Dukung kebutuhan order besar yang valid dengan tambahan likuiditas sesuai kebutuhan, tetap All-Or-None; aturan cadangan 2.000 lot menjadi cadangan dasar." Dukungan order besar bukan lagi sekadar rekomendasi; sudah dipilih eksplisit. Jalur ini harus dapat mendukung skala 10.000-50.000 lot setelah validasi dan tidak boleh macet hanya karena target dasar 2.000 lot. Angka 50.000 lot merupakan skala yang diminta untuk dukungan/pengujian, bukan maksimum order baru yang boleh ditetapkan diam-diam.

**Arahan perbaikan minimum:** telusuri preflight matching, inventory NPC, pembentukan quote buku, kapasitas/order preview, reservasi kas/saham, settlement dan persistence sebagai satu alur. Periksa kas pemain, kepemilikan penjual, harga/fraksi/band, fee, komitmen, sesi bursa/halt, supply, sumber dana dan request idempotency sebelum menghitung bantuan. Gunakan likuiditas existing yang sah terlebih dahulu dalam rencana matching sesuai kebijakan prioritas yang berlaku; hitung tambahan hanya atas kekurangan yang perlu ditutup. Pengisian inventaris saja tidak cukup bila quote tetap hanya 25 lot per level. Jangan membuat beberapa transaksi partial yang terlihat ke pemain untuk menyamarkan ketidakmampuan memenuhi AON.

**Stok dan kas adaptif:** beli pemain membutuhkan saham penjual NPC; jual pemain membutuhkan kas pembeli NPC termasuk fee yang berlaku. Target stok/uang khusus order harus cukup untuk komitmen rencana settlement yang valid, tanpa menghabiskan alokasi yang sudah dimiliki order lain. Target dasar tetap digunakan ketika kebutuhan order tidak melebihinya. Jangan memperlakukan Rp10 miliar/US$1 juta sebagai plafon dukungan untuk penjualan besar, atau mencetak penuh kuantitas request jika sebagian sudah ditutup maker lain. Jumlah penerbitan tetap selisih kebutuhan, melalui supply event tercatat dengan dilusi sesuai persetujuan sebelumnya; kas tetap suntikan modal eksternal simulasi tanpa utang/bunga. Keputusan dukungan ini tidak memilih spread/harga khusus, kompensasi dilusi, repricing emiten atau diskon biaya baru.

**Batas perlindungan:** permintaan invalid, saldo pemain tidak cukup, saham penjual tidak cukup, duplikat request, pembatasan save Q02, serta bursa tutup/halt tidak boleh dilewati hanya karena ada fasilitas likuiditas. Untuk market yang diantre saat bursa tutup, dukungan baru dinilai pada pemrosesan sesi yang sah. Limit harus tetap menghormati harga limit dan aturan prioritas; tidak ada jaminan fill pada harga yang melanggar aturan. Jika pengisian/settlement/save terputus, retry tidak boleh menggandakan kas, saham, fill atau jumlah saham beredar. Jangan mengulang loop pengisian 2.000 lot sambil menunggu AON yang tidak pernah mengurangi stok; bentuk rencana kebutuhan yang utuh sebelum menerapkannya.

**Verifikasi pelaksana setelah kebijakan skala dipilih:** QA 10.000 dan 50.000 lot untuk beli/jual, market/limit, IDX/US, kas cukup/tidak cukup, kedalaman buku kecil/besar, beberapa maker, harga multi-level, batas harian, cancel, retry serta save/reload. Tegaskan terpisah jumlah yang boleh diinput, kapasitas kas, persediaan bebas, kedalaman quote, dan hasil eksekusi. Jangan hanya membesarkan angka stok menjadi 50.000 lot lalu mengklaim seluruh alur beres.

**AGENTS.md:** auditor belum mengubah file instruksi itu selama rangkaian audit, handoff dan Q&A; keputusan baru berada dalam dokumen ini. Pembacaan saat tinjauan masih menemukan teks "konflik dan pembulatan menunggu keputusan" pada `AGENTS.md:17`, sementara Q09/Q10 sudah diputuskan di sini. Git menandai AGENTS.md modified, tetapi perubahan lokal tersebut sudah ada dan tidak boleh diatribusikan ke penulisan auditor tanpa bukti. Sinkronisasi keputusan terbaru ke AGENTS.md belum dilakukan dan harus mempertahankan perubahan lokal yang sudah ada. Jawaban pemilik dalam chat tetap berlaku; jangan menafsirkan ketidaksinkronan dokumen sebagai pembatalan keputusan baru atau izin menjalankan plan/source.
