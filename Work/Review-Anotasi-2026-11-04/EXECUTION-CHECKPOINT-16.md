# Checkpoint 16 — valuasi portofolio dan IPO teks besar

Goal aktif, turn PROGRESS. Root GAME SAHAM/branch sebelumnya diverifikasi dan perubahan lokal dipertahankan. Melanjutkan E03/E05/E09/E10/E11. Tidak ada ADB, instalasi, commit, publish atau perubahan global.

## Perubahan dan reproduksi

TradingWebBridge.getPortfolio dahulu mengambil grossAssets/P&L/rows dari cached Position.currentPrice, tetapi equity dari RiskComplianceService yang membaca katalog. Fixture distress membuktikan saham150juta vs risk135juta dan liabilitas salah115juta vs utang100juta. Matching biasanya memperbarui keduanya, namun cached position dari save atau jalur lain tetap dapat berbeda. OwnerMarginDeadlineTest.portfolioUsesTheSameLiveMarkAsMarginWithoutChangingHoldings merah expected9000 actual10000 di1793795602383365800/test.log.

getPortfolio sekarang memakai Account.copy existing, memperbarui market mark salinan untuk instrumen katalog lalu seluruh field dihasilkan dari salinan yang sama. Kuantitas/averageCost/biaya/realizedPnL/posisi asli tidak dimutasi pada read. Aset tanpa katalog, termasuk posisi sebelum listing dan multi-aset, tetap memakai mark posisi yang tersedia. Tidak menambahkan kelas/dependency atau menulis save saat read.

TradingWebBridgeTest portfolioPositionsExposeCatalogUnitsAndCurrencyMatchedValuation semula mengharapkan cached10000/110 dari posisi yang diinjeksi. Suite merah38tes1gagal tercatat bridge-valuation-before.log. Ekspektasi kini harga katalog BBCA10250/AAPL235.00, nilai1.025.000/1.175USD dan P&L125.000/675USD. Semua assertion lot/currency/quantity/average/realized tetap. Tes owner memeriksa gross135m/liability100m/equity35m dan memastikan posisi asli unchanged.

IPO pada320px+font200 menampilkan Ongoing/Upcoming/Past patah huruf dan tickerAGRO patahAG/RO. app.js menandai filterBar ipo-filters; CSS label nowrap/flex-shrink0 sehingga filter bisa discroll. Header kartu membungkus harga ke baris berikut jika tidak cukup ruang, ticker nowrap. Perubahan mengikuti layout responsif yang sudah disetujui.

## Bukti

-48tes core lulus pada1793795639634669100/test.log,10,288detik. OwnerAnnotationPolicyTest+OwnerMarginDeadlineTest+ChartHistoryRangeTest+FirmClientMandateFeeTest.
-38tes TradingWebBridgeTest akhir lulus,5,815detik, bridge-valuation-after.log di direktori sama. Ini suite aktual bridge lengkap pada sourceJava final, bukan klaim run106tes sebelumnya.
-Keempat check-*.cjs, syntax app.js dan git diff check lulus setelah koreksi IPO. APK akhir gradlew --offline assembleDebug exit0,2detik.465aset www identik dengan source, HTTPJS/CSS kedua preview juga cocok. source-apk-parity-16.json di browser-final-20261104 mencatat hash. APK80b1fb6835ce6e22860473d79fae82476e637ab942d0f209c3e8880020f7ac64.
-Player SHA tetap59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed.

## Browser dan preview

Tab4/5 awal melaporkan browser.user unavailable. Troubleshooting dibaca, listTabs masih menemukan keduanya. Rebind4 gagal, tetapi membuat tab6 di browser2 yang sama berhasil. Ini stale-handle recovery, bukan global setting/bypass atau blocker akhir.

Tab6 qaTextScale200 pada320px menampilkan root32px. Screenshot sebelum koreksi diamati, sesudah koreksi tersimpan ipo-text200-320.png. Matrix320/360/399/430/768 font32px seluruh documentWidth==viewport, filterOverflowtrue pada ponsel danfalse pada768, tickerNoWrapnowrap. ipo-text200-matrix.json. Matriks terbatas daftarIPO, tidak semuaUI.

Preview Java utama64224 session43658, margin64225 session52976 kini classes1793795639634669100, masing-masing saveQA sama seperti15. PID/task-owned sebelumnya dicek commandline sebelum dihentikan. Tab7 dibuat dari64225 untuk proof final. Portfolio lewat UI menunjukkan saham135juta, kas15juta, unrealized-15juta, liability90juta setelah pelunasan sebelumnya, net60juta. screenshot portfolio-live-mark-399.png. Kartu kecil menampilkan modal150juta sebagai invested, aria-label nilai posisi135juta; jangan salah menyebut modal sebagai mark.

Viewport reset, tab6 kembali URLnormal64224 dan markDeliverable, tab7markHandoff. Tab4/5 lama masih dapat terdaftar tetapi handles stale. Untuk lanjutan pakai6/7 atau documented newtab recovery pada browser2.

## Sisa

Logo otomatis berjalan untuk ticker baru;62baseemiten punya aset resmi/referensi, sisanya simbol otomatis. Kelengkapan resmi belum penuh. Visual/text200 matriks selainIPO, seluruhstate riset/feeFirma UI, dan nativeAndroid/pinch masih membutuhkan bukti. Goal tidak complete. Next action gunakan tab6/7 source akhir, perluas pemeriksaan layar yang belum tercakup dan lengkapi logo dari sumber/hak yang sah. Tidak mengulang green suites tanpa perubahan/gap baru.
