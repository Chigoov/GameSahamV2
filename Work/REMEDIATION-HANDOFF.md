# Checkpoint aktif R01–R08 — PARTIAL

Repo: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`; branch `codex/rebuild-2d-portrait`; sesi `20261019_012813_c4c8b3`. Bukan repo TradingGlobal3D lama. Pertahankan semua perubahan lokal; tidak reset/clean/push/ADB/perangkat/signing baru.

## Progres dan acceptance

**1/8 selesai:** R01 COMPLETE. R02/R03/R04/R06/R07/R08 PARTIAL; R05 BLOCKED. R04 aktif; seluruh 228 requirement, 40 tujuan desain, dan 26 cakupan lama tetap scope. Bukan 14/14 produk accepted.

Pemetaan terkini: `Artifacts/Remediation/R04/design-screen-coverage-current.csv` dan `coverage-current-summary.json`:19 UI PARTIAL,20 MISSING,1 UNAVAILABLE_NOTICE;16 Java-host subset,22 NOT_TESTED,1 honest empty,1 fixture subset. Tidak ada full design acceptance. Work requirement tetap3 PASS statis/225 NOT_TESTED; bukti subset jangan mass-PASS.

## Selesai sejak checkpoint sebelumnya

- Sampai113tes JUnit/Gradle: posisi ID bursa:ticker, P/L realized gross persisted (legacy unknown=null), mark seluruh pemegang, debt IDR/USD dan negative repayment guard, continuous/auction preflight untuk own crossing/maker rusak, reserve fee rounding per lot. `R02/STEP-INTEGRITY.md` merekam RED/GREEN dan batasnya.
- Runner evidence membedakan EXECUTED/CACHED/NOT_RUN/MISSING_RESULTS; compile gagal tidak lagi menyalin XML lama. `verify_runner.py` RED→GREEN. Artefak `auction-preflight-green` gagal compile dan XML-nya stale; bukan tes baru, bukan PASS.
- Host Java snapshot111 telah rerun trading/fill/portfolio/ledger/save-load/briefing/play-pause/history/cancel/FX/firma read-only/misi/laporan. Fixture trading/recovery/static exit0. Bukan WebView.
- Label pencarian/bursa dan scroll viewport pendek RED→GREEN.31 sampel responsive dan10 scroll subset lulus; screenshot dibaca vision. Lihat `R03/integrity-111/REPORT.md`.
- `.gitignore` kini mengecualikan cache/build/local.properties Android. Belum ada commit lokal pada checkpoint ini.

## Kandidat nyata terbaru pada checkpoint

`Artifacts/Remediation/R07/margin-risk-116/game-saham-remediation-candidate.apk`

- Build `assembleDebug lintDebug testDebugUnitTest --rerun-tasks` exit 0, 116 tes EXECUTED/0 failure/error/skipped.
- 183587 byte; SHA-256 `ddf98bcd852adbabc50ba45507429417d46b308c2f125e7a96bd16de68d2baab`.
- Source manifest `0855596a59d30a51d0c2a8e31c8cfd86c9d8a3fc67dde37111532a78784a530c`; HEAD `614015cc4bcc1210393c9fd3ce0081e7377cea33` + source lokal.
- 5 aset exact, ZIP/DEX/debug signature valid. AppID `com.tradingglobal3d.mobile`, version 1 / `1.0.0-2d-portrait`, min 26 / target 36, portrait/debug.
- Lint 0 error / 8 warning. Report dan inspection dalam folder sama. Perubahan source berikutnya mengusangkan snapshot ini.

## Blocker dan sisa

- Dataset OHLCV nyata/hak distribusi/periode/resolusi/raw-adjusted/provenance/replay binding belum terbukti; OQ02–05 terbuka. Tidak mengarang data atau lisensi.
- OQ ekonomi/clock/career/mandate/multiasset masih sementara; bukan diputuskan diam-diam.
- Native installation/WebView/Back/keyboard/lifecycle/performa/update tetap PENDING, fase tanpa ADB.
- Entry/new/continue/tutorial/index/watchlist UI/research/client actions/fund/multiasset/corporate/risk/lab/replay belum lengkap. Short/shared liquidity, lelang semua level/price rule, corporate idempotency, migrasi/run/RNG identity masih gap.

## Estimasi baseline pasca-R01

Koreksi bukti/QA12–24jam(sedang),bug32–64jam(tinggi),fitur120–240jam(sangat tinggi); subtotal independen164–328jam effort satu writer, bukan ETA kalender. Data setelah keputusan16–40jam dan Android setelah lingkup dibuka8–24jam, tidak termasuk waktu menunggu. Ini baseline `R01/AUDIT.md`, bukan sisa baru yang dikurangi berdasarkan jumlah tes/milestone.

## next_action

1. Verifikasi/read-back tracking/laporan; checkpoint Git lokal allowlist berkas reviewed (tanpa cache/build/output/credentials/key/save/dataset berhak tidak jelas), jangan push.
2. Lanjut slice watchlist UI/domain existing dengan regresi bridge+browser: tambah/hapus/filter dan save/reload, tanpa perhitungan ekonomi JS. Periksa unknown-ID toggle yang dapat mengklaim true walau katalog menolak.
3. Restart hanya server QA Java sendiri sesudah compile baru sebelum retest. Aset8765,Java8766; fixture-save/qa-classes bukan data pemain dan jangan commit.
4. Setelah slice source berubah, build/inspect kandidat unique folder; checkpoint lagi dan lanjut fitur independen.
