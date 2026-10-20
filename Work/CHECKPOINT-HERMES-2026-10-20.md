# Checkpoint — penuntasan fitur Game Saham (20 Oktober 2026)

Dibuat oleh koordinator Hermes sebelum berhenti sesuai instruksi pemilik. Dokumen ini agar agent lain dapat melanjutkan tepat di titik yang sama tanpa pencarian ulang. Baca dokumen ini dulu, lalu lanjutkan.

## 1. Identitas dan root

- Root produksi (SATU-SATUNYA boleh ditulis): `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`
- Repo referensi historis (JANGAN ditulis/reset/jadikan checkout): `C:/Users/HYPE AMD/Downloads/VIBE CODING/Game Trading Saham`
- Git branch: `codex/owner-audit-portrait-v8`. Snapshot paket pada `196a49d` (baseline audit, BUKAN target rollback).
- HEAD saat checkpoint: `f467607` (commit chore: record setup findings). Sebelumnya `5fe86fb` (integrasi R0–R7 claim).
- Working tree: BERSIH dari mutasi source. Hanya 3 file untracked (artifact design-loop, BUKAN source): `Work/.agents/tasks/design.md`, `Work/.agents/tasks/design-review.md`, `Work/.agents/tasks/design-verdict.json`. Tidak ada source code diubah setengah jadi — aman dilanjutkan/ditinggalkan.
- Save pribadi yang WAJIB dilindungi (jangan reset/ubah untuk tes destruktif): `Artifacts/LocalPreview/preview-save/save_game_v2.json`, SHA-256 `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`. Tes QA wajib pakai save direktori terisolasi.

## 2. Spesifikasi & bahan wajib (baca sebelum lanjut)

Paket penuntasan (sumber otoritas utama untuk keputusan tambahan pemilik):
- `Work/Hermes-Feature-Completion-2026-10-20/START-HERMES.md` — prompt mulai.
- `Work/Hermes-Feature-Completion-2026-10-20/SPECIFICATION.md` — kontrak 26 kelompok, D01–D16, audit F1–F9, R0–R7, QA. (GAP-MATRIX.csv 148 delta rows, 277 seed existing.)
- `Work/Hermes-Feature-Completion-2026-10-20/README.md`, `MATERIALS.json`.
- `AGENTS.md` (root) — instruksi aktif, ponytail minimum, larangan.
- Audit independen (JANGAN ditimpa): `Artifacts/Independent-Review-2026-10-20/REPORT.md`, `probe-results.json`, `verification.json`, `AuditProbe.java`, `jvm-rerun.log`, `apk-signature.txt`.
- Standar: `Work/Hermes-Handoff-2026-10-20/PHASE-QUALITY-STANDARDS.md`, `OWNER-DECISIONS.json`, `FEATURE-ACCEPTANCE.csv` (277 seed, jangan dikecilkan).
- Validator sudah PASS: `python Work/Hermes-Feature-Completion-2026-10-20/verify_package.py --self-test` → materials_status PASS (148 delta, 26 group, 9 audit, source_drift 10 file — drift SAH, jangan rollback).

## 3. Workflow yang sedang berjalan (status: PAUSED — bisa di-resume, bukan abort)

- Workflow ID: `wf_d482a4ef826b4c54`. Dirancang oleh workflow-creator; pakai recipe workflow-from-prompt.
- Struktur langkah: setup ✅ → design-loop (design ↔ design-review, maks 2 iterasi) → plan → implement-and-review-loop → finalize-and-verify.
- Posisi saat di-pause: **design-loop iterasi #1 (yang ke-2 dari 2)**. `design` (iterasi #1) SELESAI menghasilkan `design.md` v1.1.0 mengklaim menanggapi 11 temuan. `design-review` (iterasi #1) belum jalan saat di-pause.
- Artifacts workflow: `Work/.agents/tasks/setup-report.md`, `design.md`, `design-review.md`, `design-verdict.json`.
- Untuk lanjut: `inspect_workflow wf_d482a4ef826b4c54` lalu `update_workflow` action `update_status` status `running` (resume). Atau biarkan design-review iterasi #1 berjalan untuk menilai v1.1.0.

## 4. Hasil VALIDASI temuan review terhadap source aktual (INTI — kerja intelektual koordinator)

Reviewer design menghasilkan CHANGES_REQUESTED dengan 11 temuan (H1:6 M3:3 NIT:1). Sesuai instruksi pemilik, koordinator memvalidasi setiap temuan yang menyebut klaim spesifik terhadap source saat ini — JANGAN otomatis terima asumsi reviewer bila tidak cocok kode. Hasil:

| Temuan | Klaim reviewer | Validasi terhadap source | Putusan & arah |
|---|---|---|---|
| 1 save protection | Design tak sebut isolasi save pribadi | Omission design, bukan klaim source | VALID — design wajib sebut isolasi QA save; tidak sentuh source |
| 2 cutoff reksa dana | "bug equality `toSecondOfDay()==16*3600`, step tak pakai secondsToCutoff" | `TradingFacade.advanceWorldSeconds` (baris 1062): loop hanya proses midnight (`settleDay`+`processCalendarActions`) + market open. **TIDAK ada cutoff 16:00 tersambung sama sekali**; hanya `mutualFundManager.advanceDay(0.0)` di `settleDay` (akrual harian, bukan settlement cutoff). Premise "equality bug" reviewer TIDAK cocok dengan TradingFacade. | VALID prinsipnya (D06 cutoff tak tersambung), tapi premise tidak akurat. Arah perbaikan benar: sambungkan cutoff 16:00 ke loop dengan interval-crossing, BUKAN equality. |
| 3 API staff perusahaan | Bridge/facade/CompanyManager tak punya staff API | Grep `getCompanyStaff/hire/train/assign/dismiss` di TradingWebBridge+TradingFacade+CompanyManager = 0 hasil. `Employee.java` model ada, `OperatingCompany.calculateDailyPayroll` pakai `employees`. | VALID — gap implementasi nyata: bridge/facade perlu API staff + UI modal `openCompanyStaffModal`. |
| 4 tenor loan | design.md Table 5.2 arang 14/30/60, 0.04%/0.75% | `InstallmentLoanManager.java`: enforce `tenorDays != 7 && != 14 && != 30`, `dailyRate "0.05%/hari"`, fee 0.5%. Sudah benar sesuai D05. | Design SALAH, source BENAR. Design harus selaras ke D05/source (7/14/30, 0.05%, 0.50%), JANGAN re-implement/ubah source yang benar. |
| 5 katalog koleksi | design.md ganti katalog jadi item Rp 350M+ | `CollectionManager.initDefaultCatalog`: tepat 12 item, 4 kategori (VEHICLE/REAL_ESTATE/JEWELRY/ART_AND_WATCHES), 3 per kategori, raritas STANDARD/RARE/LEGENDARY. Harga Rp15M (Rolex) → Rp25B (Villa). Sudah memenuhi D10. | Design SALAH (klaim perlu ekspansi), source BENAR. Design harus reuse 12 item existing; hanya tambah collateral pledge flag/lock. Catatan: "Lukisan Raden Saleh" = pelukis historis (wafat), bukan tokoh hidup — boleh sebagai atribusi karya. |
| 6 revenue perusahaan | OperatingCompany hardcode Rp25M/level tanpa demand/cost | `OperatingCompany.calculateDailyRevenue`: `basePerLevel=25_000_000L`; `finalRevenue = basePerLevel * level * prod * moraleFactor`. Tidak ada sektor/demand/operating cost. | VALID — melanggar D11/SPEC §21 ("jangan fixed Rp25jt/hari semua sektor tanpa model biaya, demand"). Butuh perbaikan source nyata: 3 sektor (jasa/ritel/manufaktur), formula demand/capacity/cost, payroll arrears. |
| 7 queue fund dealing | Design tak sebut queue persistence/cancel deadline | `subscribeMutualFund`/`redeemMutualFund` di TradingFacade eksekusi langsung (tidak queue). D06 minta queue + cutoff + cancel deadline. | VALID — butuh queue `PendingFundOrder` di SaveState + `cancelFundOrder` bridge + deadline 15:30 WIB. |
| 8 cakupan 26 grup/D-rules | Design tak lengkap (D01,D03,D04,D12,D14,D16, korporasi) | Omission design | VALID — design harus lengkapi; tetap pakai SPECIFICATION sebagai sumber. |
| 9 nama NPC liga | LeagueManager pakai nama tokoh nyata | `LeagueManager.initializeNpcRoster` baris 56: `addNpc("NPC-01","Prajogo Pangestu",...)` padahal komen baris 55 bilang "NPC Fiktif (D09)". Melanggar SPEC §19 "Nama tokoh nyata tidak menjadi default". | VALID — bug source nyata. Ganti roster 20 nama fiktif + archetype di LeagueManager.java. |
| 10 validasi bridge | Design tak sebut error code/bounds mutasi | Omission design | VALID — design tambah input validation matrix. |
| 11 (NIT) F3/F5 stale | Design F3/F5 problem statement stale vs repo | Reviewer sendiri akui design mengklaim `initializeNewGame` tak reset & app.js tak punya 6 modal, padahal sebagian sudah ada (reset manager di `initializeNewGame` baris 355-359: companyManager/collectionManager/mutualFundManager/installmentLoanManager/periodicMissionManager.reset()). | VALID — design harus akurat ke repo; F3 fokus ke verifikasi tes `CompanyCleanResetTest`, F5 fokus `openCompanyStaffModal` (yang belum ada). |

Ringkasan: temuan campuran — sebagian design.md salah (F4, F5: harus selaras ke source yang benar, JANGAN re-implement), sebagian bug source nyata (F3, F6, F7, F9: butuh perbaikan kode), sebagiani omission design (F1, F8, F10), satu premise tidak akurat tapi arah benar (F2). Saat menilai design.md v1.1.0, cek apakah revisi BENAR-BENAR selaras dengan tabel ini — terutama F4/F5 jangan sampai design "memperbaiki" source yang sudah benar.

## 5. Yang sudah selesai (bukan klaim fitur, hanya kerja koordinator)

- Preflight R0 mekanis: validator paket PASS; Git root/branch/HEAD/diverifikasi; save pribadi, runner Gradle (`gradlew.bat` di `android/`, 27 file tes), AuditProbe, 14 TASK fase 00–13 terinventaris.
- Validasi 11 temuan review terhadap source (tabel di atas) — ini menentukan apa yang benar-benar perlu diubah.
- Workflow di-pause dengan aman (tidak ada mutasi source setengah jadi).

## 6. Yang BELUM selesai (prioritas lanjutan)

1. **Lanjutkan/resume workflow `wf_d482a4ef826b4c54`.** Biarkan design-review iterasi #1 menilai design.md v1.1.0.
2. Saat verdict masuk: cek konsistensi dengan tabel §4. Tolak bagian design yang "memperbaiki" source benar (F4/F5). Terima & pastikan perbaikan source nyata untuk F3/F6/F7/F9 dan penyambungan cutoff F2.
3. Setelah design APPROVED → plan (wf-planner) → implement-and-review-loop (wf-coder ↔ wf-code-reviewer, maks 2 iter) → finalize-and-verify.
4. Implementasi mengikuti R0–R7 (SPEC §32): R0 reproduksi F1–F9 ✅ validasi → R1 canonical time/save/reset/wealth(D02) → R2 hub Bisnis/Lainnya → R3 perusahaan/staff/policy/cash/koleksi → R4 reksa dana unit/NAB(D06)+loan tenor(D05) → R5 misi/badge/liga/rival/leaderboard/archetype/profil/prestige → R6 replay nyata+sisa PRD → R7 regresi/UX 7 viewport×2 scale/performa 30 sampel/APK fresh.
5. Perbaiki `Work/STATUS.json` (F9): status atas `COMPLETE_FOR_APK_HANDOFF` padahal 14 tasks `PARTIAL`, REQUIREMENTS.csv 225 NOT_TESTED, SCREEN-COVERAGE 26 NOT_TESTED, GAP-MATRIX seluruhnya NOT_TESTED. Samakan ke bukti aktual.
6. Reproduksi F1–F9 asli terhadap source (bukan asumsi): lihat `Artifacts/Independent-Review-2026-10-20/REPORT.md` untuk detail tiap F (line number snapshot). F2/F3/F4/F5 sebagian mungkin sudah diperbaiki commit sebelumnya — BUKTIKAN dengan tes runtime baru, jangan asumsi.

## 7. Batasan & larangan wajib (dari AGENTS.md + START-HERMES)

- Java = satu pemilik ekonomi/waktu/progres/save; JS hanya presentasi/input. Reuse helper/kontrak existing; telusuri caller; perbaiki root cause; hindari framework/arsitektur baru.
- JANGAN push/publish/bayar/kirim pesan pihak/ubah global Hermes/model/provider/credentials/buat scheduler global.
- JANGAN ADB/emulator/instalasi — native WebView/IME/Back/lifecycle/FPS/RAM/instalasi = PENDING terpisah (nyatakan, jangan APK-pass).
- JANGAN reset/clean/checkout paksa; jangan hapus/ubah save pribadi; jangan commit secrets/data pemain/binary besar; jangan overwrite audit independen.
- Commit lokal selektif hanya file tugas yang ditinjau+diperiksa. Pertahankan appID `com.tradingglobal3d.mobile` + signing.
- Standar mutu tidak boleh diturunkan: 10×100 aksi bermakna untuk uang/order/time/save (invariant tiap aksi); 30 sampel/jenis aksi P95≤300ms nav≤1000ms heavy≤300ms; 7 viewport×2 text scale; earned journey tanpa fixture kaya; tes harus GAGAL bila perilaku rusak.
- Setelah 2 kegagalan sama tanpa diagnosis baru: PERLU PERHATIAN + ganti pendekatan. Setelah 3: kerjakan bagian independen / catat BLOCKED. Jangan loop tanpa diagnosis.

## 8. Catatan untuk agent lanjutan

- Jangan mulai workflow baru; resume `wf_d482a4ef826b4c54` (status: paused). Pakai `inspect_workflow` lalu `update_workflow status=running` untuk resume.
- Tabel §4 adalah hasil validasi paling bernilai — pakai sebagai filter saat menilai design-review v1.1.0. Jangan percaya design.md yang mengarang ulang source yang sudah benar (F4 tenor, F5 katalog).
- Source key paths: domain `android/app/src/main/java/com/tradingglobal3d/core/` (business/, loans/, league/, funds/, collection/, facade/TradingFacade.java, player/), bridge `mobile/TradingWebBridge.java`, UI `android/app/src/main/assets/www/{app.js,index.html,style.css}`, tes `android/app/src/test/java/com/tradingglobal3d/`. Build: `android/gradlew.bat -p android testDebugUnitTest`. APK: `android/app/build/outputs/apk/debug/`.
- Bug source nyata terkonfirmasi perlu perbaikan: `OperatingCompany.calculateDailyRevenue` (F6), `LeagueManager.initializeNpcRoster` nama tokoh nyata (F9), staff API hilang di bridge/facade (F3), fund cutoff tak tersambung (F2), fund queue/cancel (F7).
