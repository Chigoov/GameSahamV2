# Checkpoint serah-terima long task — 21 Oktober 2026

Waktu pemeriksaan: sekitar 07:06–07:10 WIB. Status: **PARTIAL / WIP INTEGRATION**. Dokumen ini bukan kelulusan produk atau perintah menjalankan ulang agen lama.

## Tujuan dan keputusan terbaru

Pemilik meminta fitur yang belum masuk dilengkapi terlebih dahulu, lalu langsung dilakukan pengujian menyeluruh. Keluaran percakapan terbaru adalah prompt long task lengkap untuk agen pelaksana. Gunakan `Prompts/Longtask-Penuntasan-Fitur-Pengujian-2026-10-21.md` dan spesifikasi produk existing; jangan kembali ke implementasi kosong.

Root produksi: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`. Repo `Game Trading Saham` adalah checkout referensi yang berbeda, bukan tujuan edit.

HEAD saat pemeriksaan terakhir: `7dd0526dd1f9cc0ba7730de3025c54f69f6b2d6d`. Ini berbeda dari laporan Session 3 awal `68bbd2c`; beberapa commit lokal telah muncul selama integrasi. Koordinator percakapan ini tidak membuat commit tersebut. Identifikasi writer/proses sebelum melanjutkan dan revalidasi HEAD, jangan rollback ke hash laporan lama. Terdapat banyak perubahan lokal dan test baru yang wajib dipertahankan.

Save pribadi `Artifacts/LocalPreview/preview-save/save_game_v2.json`: SHA-256 masih `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`. Tidak ada instalasi, ADB, emulator atau push dilakukan oleh koordinator dalam pekerjaan ini.

## Bukti terbaru dan batasnya

- Baseline Session 3: hasil XML terdahulu 387 tes, nol gagal/error/skipped; APK 815303 byte. Itu baseline lama sebelum seluruh perubahan di bawah.
- Pemeriksaan 07:06: runner existing mengompilasi seluruh core, bridge dan test source; kemudian menjalankan **15 tes terpilih** (Business, Research, Lab), PASS. Log: `Artifacts/Feature-Completion-2026-10-21/handoff-compile/junit-latest.log`.
- Ini bukan full regression, build Android, browser acceptance atau APK baru. `MainActivity` tidak dikompilasi oleh runner JVM subset tersebut.
- Worker domain meninggalkan hasil tes terarah terpisah pada `Artifacts/FeatureCompletionBusiness-2026-10-21/`, `FeatureCompletionResearch-2026-10-21/`, `FeatureCompletionLab-2026-10-21/` dan `Artifacts/Feature-Completion-2026-10-21/`. Sebagian perubahan fee-basis paling akhir belum memiliki hasil final yang dikonfirmasi koordinator. Baca log, mtime dan source snapshot; jangan menjumlahkan hasil subset sebagai jumlah tes final unik.
- `Work/STATUS.json` masih menunjuk handoff lama dan belum direkonsiliasi. Jangan menganggapnya bukti bahwa pekerjaan di source hilang atau sudah lulus.

## Domain yang sudah ditambahkan, tetapi integrasi akhir masih wajib

1. **Perusahaan:** quote/eksekusi expansion berbayar dan policy versioned/idempotent, histori kebijakan, preview/execute transfer modal dengan operating reserve, laporan operasi harian tersimpan. API `CompanyManager.previewCompanyAction`, `executeCompanyAction`, `previewCapitalTransfer`, `executeCapitalTransfer`.
2. **Koleksi:** valuasi kategori dengan anchor kalender tersimpan; tidak mengikuti wall clock. API `CollectionManager.updateValuations(timestamp)`.
3. **Profil/statistik:** `recordFillResult(fillId, realizedNetPnLIdrOrNull)` dengan dedup; win rate transaksi tertutup, extrema benar dan identitas Unicode. `hasRecordedFill(fillId)` tersedia untuk mencegah konversi ulang pada FX baru.
4. **Gaya investor:** overload classifier dari List<TradeFill>, run days, account, wealth, margin debt, FX, asOf. Menggunakan exposure, concentration, leverage, holding dan turnover, bukan label dari saldo/umur akun saja.
5. **Reksa dana:** schema 4, kontrak backing deposito/obligasi, akun penerbit canonical, principal/receivable/liability/accrual, kupon dua sisi, default/liquidity/rate risk. `registerPoolAccounts` sebelum `bindAccounts` saat load untuk migrasi sah.
6. **Liga NPC:** akun/posisi canonical untuk 20 peserta, tidak mengubah wealth dengan angka random; `bindMarket`, `getNpcOrders(now)`, `refreshNpcValuations`. Nullable ROI tidak boleh menjadi ranking palsu. Akun internal bertambah dan harus disembunyikan dari account picker pemain.
7. **Multi-aset:** `MultiAssetTradingService` quote/execute, finite dealer inventory, fee, expiry quote, kupon/maturity, harga berdasarkan domain FX/basket, save/restore. Hubungkan `updateValuations`, `settleCoupon`, snapshot portofolio/wealth, UI review dan transaksi. Helper coupon lama yang dapat mencetak uang ditolak; test lama terkait diarahkan ke penolakan plus replacement tests yang sah.
8. **Riset:** domain `getConsensus`, `getAlternativeData`, `getAnalystInbox`, `compareInstruments`; publikasi sesuai waktu dan entitlement PRO/ENTERPRISE. Belum ada semua wrapper facade/bridge/UI.
9. **Lab:** konstruktor empat argumen dengan game timestamp/FX; validated decisions, deep isolation, history/reset/replay dan export/restore. Belum semua bridge/UI/persistence disambungkan.
10. **Replay:** loader legal US:AAPL 61 bar harian 2016-05-09–2016-08-03, asset opener Android, checksum, exact volume, date alignment, closed-bar visibility, instrument restriction dan fail-closed save. Provenance di `android/app/src/main/assets/data/historical/replay_provenance.json`. Manifest global dan isi APK lama perlu ditinjau ulang; file IDX/Yahoo lama tidak otomatis berhak didistribusikan hanya karena loader menolaknya.
11. **Korporasi:** rights issue, buyback, merger tender, finite treasury, reservation/cancel/settle, frozen entitlements/vote deadline/result/consequence, dividend idempotency, precise reverse split, IPO request-ID/cap/accounting. API corporate baru tersedia; facade/bridge/UI belum seluruhnya terhubung.
12. **Return:** `PerformancePeriod` Modified Dietz dan baseline weekly/run/history ditambahkan, namun klasifikasi cashflow, FX historis, daily report dan konsistensi lintas layar belum selesai direview.
13. **Fee transaksi:** `Position` actual acquisition-fee basis, allocation ketika partial sale, persistence dan matching routes sedang diperbaiki. Review `MatchingEngine`, `Position`, `SaveState`, `StrategyLabSandbox` serta transformasi posisi corporate agar basis tidak hilang.

## Integrasi shared yang sudah mulai dibuat

- `TradingFacade`: perusahaan review/execute, multiAssetOrder, profile fill callback, performance report, replay mode save terpisah, mode resume, clock replay dipilih sebelum world init, nullable weekly ROI, load fund issuer dan liga, minute NPC dispatch.
- `TradingWebBridge`: company action/capital, multi-asset order, performance query, historical dataset catalog dan resumeSavedMode; legacy policy ditolak, legacy loan ditolak facade. Tidak semua domain tambahan punya wrapper/handler.
- `app.js`: shared command review, company upgrade/policy/capital flows, laporan operasional, buy/sell multi-aset. Ada sambungan UI yang masih perlu diperbaiki dan diuji; jangan klaim fitur final.
- `MainActivity`: HistoricalDatasetLoader AssetSource dari Android assets telah ditambahkan, perlu build Android untuk pembuktian.
- Runner `Work/run-codex-jvm-check.ps1` sekarang menerima `-OutputDirectory` supaya log baru tidak menimpa bukti lama.

## Temuan/integrasi yang wajib ditutup atau diverifikasi ulang

### UI dan idempotency

- `openCompanyCapitalModal`: select memakai `TO_COMPANY` / `TO_PERSONAL`, tetapi request pernah membaca `=== 'INJECT'`. Pada pemeriksaan 07:06 mismatch masih ada. Uji suntik/tarik modal dari UI; jangan meluluskan hanya karena bridge dapat dipanggil langsung.
- Loan confirmation masih pernah membuat requestId baru di dalam setiap klik; retry setelah hasil tidak pasti harus mempertahankan identitas transaksi yang sama. Edit input harus membatalkan quote lama, bukan mengeksekusi draft berbeda.
- Replay UI masih memuat pilihan IDX/Yahoo dan klaim lisensi hardcoded pada snapshot terakhir; ganti dengan query `getHistoricalDatasets()` dan metadata domain aktual.
- Company operational report UI, research terminal, corporate offers/cancel/voting, multi-aset holdings/kupon, nullable league ROI/history, lab decision replay dan performance report harus dapat ditemukan serta dipakai.

### Profil, P/L dan return

- `evaluateRecordedCareerProgress` mengonversi ulang USD fill menggunakan FX saat query; jika fill yang sama sudah diproses, skip sebelum conversion atau simpan kurs/nilai IDR saat fill. Jangan mengganti payload fill lama ketika FX berubah.
- Record fee aktual ketika buy, alokasikan basis fee ketika partial/final sale; seeded/legacy holdings tidak boleh dikenai buy fee fiktif. Save/migrasi/split/merger harus menjaga basis biaya.
- Margin lama ditulis DEPOSIT/WITHDRAWAL. `PerformancePeriod` sudah ditambah guard request prefix MARGIN-LOAN-/MARGIN-REPAY-, tetapi telusuri semua caller dan daily report agar utang tidak menjadi return. Jangan menyamakan gaji/reward dengan profit investasi.
- `getConsolidatedWealthBreakdown` sudah mulai mengambil net worth canonical; pastikan klasifikasi debt USD, short liability, company debt, collection arrears, reserve dan alokasi tetap transparan serta rekonsiliasi.
- Nullable/zero-denominator ROI harus tampil tidak tersedia, bukan 0%, NaN, ranking atau promotion palsu. Test Monday rollover tepat sekali, game clock, FX, legacy baseline dan period archives.

### Replay dan penyimpanan mode

- Dealer replay pernah self-cross ketika bid/ask sama; kini quote spread satu tick mulai dipakai. Periksa harga pembanding cleanup agar order tidak dibatalkan/ditumpuk tiap menit dan budget/inventory tetap finite. Jelaskan spread model, jangan menyebutnya book historis nyata.
- Gating harus mendahului order registration/trigger return, menolak wrong instrument/weekend/tanggal tanpa bar/end-dataset. Active/pending order dan trigger tidak boleh fill memakai future high/low atau stale quotes.
- Init clock replay sudah memakai epoch dataset sebelum domain init. Buktikan semua ledger/career/collection/fund/league/research anchors tidak tersisa di tahun 2026 dan skip/reload tidak menolak waktu mundur.
- Reset reference price agar high/low/previous close tidak memakai harga masa kini; sembunyikan fundamental modern yang tidak tercakup dataset; calendar/news/corporate synthetic tidak boleh masuk replay historis.
- Pisahkan primary `save_game_v2.json` dan `save_replay_v2.json`, UI switch/resume, validation dan failed-save recovery. Jangan mengubah state aktif sebelum target tervalidasi; jangan menimpa mode lain ketika membuat new game atau prestige.
- Loader sudah mempunyai AssetSource Android, tetapi build/class contract, package hash, resume dan end-of-dataset harus diuji.

### Korporasi, multi-aset dan saham tersimpan

- Hubungkan `CorporateActionService.processDate` dengan game date dan event ordering; record date sebelum entitlement dipakai. Respect approval PENDING/REJECTED dan cancellation; panggil split overload dengan ledger.
- Gunakan timestamp game untuk IPO/voting/risk/fee ekonomi; `System.currentTimeMillis()` masih ada di sejumlah caller finansial dan harus ditinjau.
- Tender reservation harus melindungi order baru, pending trigger dan resting sell yang sudah ada; cancel/revalidate sebelum reserve/split. Settlement tidak boleh menjual saham yang sudah dialihkan.
- Panggil IPO overload requestId dan listing catalog instrument; saham IPO harus bisa dijual setelah listing, bukan posisi yatim.
- Equity/holding snapshot harus memasukkan bond/gold dengan harga canonical, kupon prorata, maturitas dan risiko dealer; jangan memasukkan 20 akun NPC/issuer pool ke aset atau pemilih akun pemain.
- Corporate split/merger yang membuat Position baru wajib menjaga gross realized dan actual acquisition-fee basis. Mutasi tersimpan/receipt tetap idempotent setelah crash/retry.

## Langkah pertama agen penerus

1. Pastikan tidak ada writer lain; inventaris diff dan bukti saat ini. Jangan reset.
2. Baca prompt long task baru dan spesifikasi, buat peta requirement→implementasi→bukti yang aktual.
3. Kompilasi source terbaru, jalankan test terarah pada WIP, reproduksi temuan di atas, kemudian tuntaskan shared integration.
4. Lengkapi semua fitur wajib yang masih hilang; setelah itu langsung lakukan full regression, browser journeys/matriks/performa, review ulang, APK baru dan laporan. Jangan mengganti syarat selesai menjadi 'subset test PASS'.

Mode kerja ini ditutup sebagai bahan handoff. Ini bukan bukti bahwa seluruh fitur sudah lengkap, seluruh known bug sudah tertutup, atau APK terbaru sudah dibuat dari WIP ini.
