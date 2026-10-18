# Rencana kerja Hermes Game Saham 2D Portrait

Seluruh 14 paket wajib dituntaskan sesuai PRD. Repo disiapkan untuk implementasi baru; seluruh status awal PENDING. Kerjakan menurut dependensi, dengan satu writer dan checkpoint nyata.

## Urutan baca

1. [Aturan repo](../AGENTS.md)
2. [Konsep](../Docs/Product/01-Konsep-Game-2D-Portrait.md)
3. [PRD lengkap](../Docs/Product/02-PRD-Game-Saham-2D-Portrait.md)
4. [Keputusan terbuka](DECISIONS.md)
5. [Checkpoint](STATUS.json), [requirement](REQUIREMENTS.csv), [cakupan layar](SCREEN-COVERAGE.csv), dan [gate akhir](ACCEPTANCE.md)

## Paket pekerjaan

| ID | Pekerjaan | Dependensi | FR utama |
|---|---|---|---|
| 00 | [Preflight dan keputusan](00-preflight-dan-keputusan/TASK.md) | - | FR-01, FR-18, FR-19, FR-20 |
| 01 | [Fondasi domain akun waktu dan save](01-domain-akun-waktu-save/TASK.md) | 00 | FR-01, FR-05, FR-19 |
| 02 | [Data katalog indeks dan kontrak historis](02-data-katalog-indeks/TASK.md) | 00, 01 | FR-02, FR-01 |
| 03 | [Pasar NPC order dan halt](03-pasar-npc-order-halt/TASK.md) | 01, 02 | FR-03, FR-04, FR-16 |
| 04 | [UX portrait dan host Android](04-ux-portrait-dan-host-android/TASK.md) | 00, 01 | FR-18, FR-20 |
| 05 | [Trading portofolio dan FX](05-trading-portofolio-fx/TASK.md) | 02, 03, 04 | FR-04, FR-05, FR-06 |
| 06 | [Karier siklus hari dan lifecycle](06-karier-siklus-hari-lifecycle/TASK.md) | 01, 04, 05 | FR-01, FR-07, FR-19 |
| 07 | [Firma klien mandat dan fee](07-firma-klien-mandat-fee/TASK.md) | 05, 06 | FR-08 |
| 08 | [Berita riset kalender dan terminal](08-berita-riset-kalender-terminal/TASK.md) | 02, 03, 04, 06 | FR-09 |
| 09 | [Multi aset dan fund builder](09-multi-aset-fund-builder/TASK.md) | 02, 05, 07 | FR-10, FR-11 |
| 10 | [Aksi korporasi IPO M&A dan voting](10-aksi-korporasi-ipo-ma-voting/TASK.md) | 02, 05, 07 | FR-12, FR-13, FR-14 |
| 11 | [Risiko margin short dan compliance](11-risiko-margin-short-compliance/TASK.md) | 03, 05, 07, 09 | FR-04, FR-15, FR-16 |
| 12 | [Replay historis lab dan stress test](12-replay-historis-lab-stress/TASK.md) | 02, 03, 05, 08, 11 | FR-01, FR-17 |
| 13 | [QA performa dan APK rilis](13-qa-performa-apk-rilis/TASK.md) | 00, 01, 02, 03, 04, 05, 06, 07, 08, 09, 10, 11, 12 | Semua FR |

## Urutan pembuktian

00 memverifikasi lingkungan dan keputusan; 01-04 menghasilkan fondasi, data, pasar dan bukti host/APK awal. 05-08 menyelesaikan trading, siklus hari, karier, klien serta riset. 09-12 menuntaskan seluruh fitur lanjutan dan historis. 13 menyatukan regresi, UX/performa dan APK akhir.

Bukti APK awal tidak berarti produk lengkap. Dataset historis dan OQ material tidak boleh dilewati agar checklist tampak selesai. Jangan menunggu akhir proyek untuk membuktikan host Android.

## Penggunaan direktori

- Folder Work/NN-* menyimpan instruksi bagian pekerjaan; source implementasi bukan ditempelkan ke setiap TASK.md.
- Struktur source dan runner dipilih minimum pada 00 dan didokumentasikan. Jangan membuat 14 aplikasi atau 14 mesin ekonomi.
- Artifacts/NN/ untuk log/test/screenshot; Artifacts/release/ untuk APK dan checksum. Binary dan data lokal tidak di-track secara default.
- Histori nyata disimpan sesuai keputusan sumber/hak/distribusi; jangan memberi label nyata pada dataset buatan.

## Pembaruan checkpoint

Perbarui STATUS.json saat mulai/selesai subpekerjaan. Status task: PENDING, IN_PROGRESS, PARTIAL, BLOCKED, COMPLETE. Status bukti: NOT_TESTED, PASS, FAIL, BLOCKED. COMPLETE membutuhkan evidence yang memenuhi TASK.md serta PRD. Jika gate data/perangkat masih pending, proyek tetap PARTIAL/BLOCKED.

Laporan di chat: selesai x/14; berjalan; pending; file berubah; command/hasil dan path bukti; blocker; next_action. Resume memakai exact Hermes session ID bila tersedia. Tidak ada launcher, auto-resume, timer atau scheduler yang dipasang oleh paket ini.
