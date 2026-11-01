# Jejak source dan kebijakan transisi N01/N02

## Pemilik waktu dan caller

- `GameClock.convertRealMsToGameMs` → `advanceRealMs`, digunakan facade `tick`, `advanceRealMs`, `reconcileOfflineElapsed`.
- `TradingFacade.syncClock` membaca injected real clock dan anchor; dipanggil bridge `syncClock`, timer UI per detik.
- Startup/load: `startOrResume` → `load` → `resumeWorld` → catchup → persist. Mode switch: `saveBeforeModeSwitch`, `resumeSavedMode`, `startHistoricalReplay`. `loadGame` bridge memakai startOrResume.
- Native lifecycle: MainActivity `onPause`, `onResume`, `onStop`; DOM: visibilitychange/pagehide/beforeunload → lifecycle/save. Callback state yang sama tidak memproses elapsed dua kali.
- Presenter: app.js `refreshSnapshot` mengambil timestamp/rate → `renderFlowingClock`. Interpolasi memakai performance.now, tanpa panggilan Java per frame. HH:mm menghindari lompatan digit detik yang tidak membantu pada 24x.
- API produk lama `tickClock`, `skipDay`, `closeDay` ditolak eksplisit. `setClockState` mempertahankan otomatis/speed=1. Facade low-level advance/skip/settle tetap dipertahankan untuk scheduler dan fixture existing, tidak menjadi kontrol pemain.
- SaveState menyimpan timestamp dunia, anchor nyata, remainder ms, lifecycle dan speed legacy. SaveManager menulis temp/fsync/rotasi backup tervalidasi. Facade runtime menambah `worldClockVersion=1` tanpa reset skema/uang.

## Kebijakan migrasi waktu

Tanggal/jam dunia, remainder, saldo, posisi dan ledger tidak dikonversi ulang. Elapsed belum diproses sejak anchor save memakai inactive 24x, sama dengan kontrak tertutup sebelumnya; setelah itu seluruh lifecycle memakai 24x. Speed save lama 1/2/5 dibaca/validasi, dinormalisasi 1 saat restore. Tidak merekonstruksi masa lalu pada multiplier baru. Anchor high-water tidak turun pada jam perangkat mundur. Tidak mengklaim otoritas waktu server; manipulasi maju jauh masih batas local-only.

## Temuan regresi yang diperbaiki

Tes migrasi membandingkan seluruh akun mengungkap BTC/ETH dinilai dengan konstanta harga emas di `MultiAssetTradingService.updateValuations`. Loop rumus emas dibatasi ke COMM:GOLD; pemutakhiran posisi tetap berjalan per ID instrumen. Bukti merah di `N01-regression-01`/`N01-migration-diagnostic`, hijau `N01-regression-02` dan suite berikutnya. Dugaan awal urutan array tidak benar; diagnostic membuktikan isi currentPrice yang berubah.

## Berita dan konsumen

- NewsArticle: publishedAt tetap waktu game, effectStartsRealAt/duration adalah nyata. Default 60 menit; overload event eksplisit 120 menit. Aktif starts <= now < ends, tidak membatalkan fill yang sudah terjadi.
- ResearchService.observeWorldTime menetapkan start satu kali; publikasi di antara dua observasi dipetakan linier menurut interval yang benar-benar diproses. real high-water mencegah deadline diperpanjang saat clock mundur.
- Facade advanceWorldSeconds memetakan event game ke waktu nyata segmen; MarketEngine.tick membaca getActiveNewsImpact pada clock observasi langkah itu. Bukan satu now login untuk semua event offline.
- Export/restore subscription mencakup newsEffects dan observation anchors. Save lama tanpa timestamp nyata mendapat satu masa transisi pada waktu migrasi, disimpan pada save normal berikutnya; tidak mengarang timestamp publikasi nyata lama.
- Feed adalah pembacaan murni: tidak observe/mengubah start. DTO membawa deadline/sisa/as-of/status yang sama. Bridge getResearchFeed memakai facade feed bersama. Replay tidak menerapkan sentimen berita internal; horizon analis/langganan/booster tidak diganti global.
- UI Beranda/Riset memakai newsEffectLabel dan deadline, detail memakai mulai/berakhir nyata WIB. Countdown Java tetap otoritas status, JS hanya presentasi.

## Bukti dan sisa

- N01 RED: 6 tes/5 gagal, lalu GREEN 6/6.
- N01 regresi termasuk migrasi, automatic lifecycle/save 10x100, canonical: 21/21 setelah perbaikan valuasi.
- N02 RED behavior 3/3 gagal (sebelumnya compile fail kontrak belum ada); GREEN 24/24 gabungan; perluasan publikasi/reading: 26/26.
- Browser N01 run-03 lulus cakupan DTO/rate, HH:mm, kontrol lama ditolak, reload continuity, reduced motion. Run-01 timeout dan run-02 backlog baseline gagal tetap disimpan. Tidak meluluskan performa/native.
- Belum: full regression fixture lama, 10x100 integrasi order+berita+save lengkap, bukti engine/facade lintas deadline, recovery failure berita, semua state layar/kontras/motion, benchmark catchup, APK.
