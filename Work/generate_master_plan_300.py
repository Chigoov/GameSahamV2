import json
import os

repo = r"C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM"
json_path = os.path.join(repo, "Work/AUDIT-300-TEMUAN-PARIPURNA.json")
out_md = os.path.join(repo, "Work/PLAN-EKSEKUSI-300-PERBAIKAN-UI-DAN-BUG.md")

with open(json_path, "r", encoding="utf-8") as f:
    items = json.load(f)

lines = [
    "# PLAN EKSEKUSI LENGKAP: 300 PERBAIKAN UI, PEMUTAKHIRAN KALIMAT & RESOLUSI BUG SISTEM",
    "## Dokumen Rencana Kerja Komprehensif Berbasis Bukti (Unbundled Execution Plan)",
    f"**Target Proyek:** `{repo}`  ",
    "**Arsitektur Runtime:** Android Native Java (API 36, Java 21) + WebView UI (HTML5, CSS3, ES6)  ",
    "**Tanggal Terbit Dokumen:** 31 Oktober 2026  ",
    f"**Total Scope Terpetakan:** Tepat {len(items)} Item Nyata  ",
    "**Prinsip Kerja:** *Zero Residual* (Tuntas Tanpa Sisa), Preservasi Save Game Pemain, dan *Verification Before Completion*.  ",
    "",
    "---",
    "",
    "## I. STRUKTUR 6 GELOMBANG EKSEKUSI (WAVES)",
    "",
    "Seluruh 300 temuan dibagi secara deterministik ke dalam 6 gelombang kerja:",
    "",
    "| Gelombang | Rentang Item | Fokus Utama Area Kerja | Estimasi File Terdampak |",
    "|---|:---:|---|---|",
    "| **Gelombang 1** | `AUDIT-001 s/d AUDIT-050` | **Header Shell, Navigasi Utama, Jam Bursa & Tab Beranda** (Pembersihan duplikasi kas, penghapusan slogan kosong, reposisi dock JUAL/BELI di atas tab, fix media query). | `style.css`, `index.html`, `app.js` |",
    "| **Gelombang 2** | `AUDIT-051 s/d AUDIT-100` | **Tab Pasar, Detail Saham, Grafik 3x3, Tiket Order & Radar Riset** (Fix limit hardcoded 10250, debounce input pencarian, tombol MAX berjarak aman, format titik ribuan). | `index.html`, `style.css`, `app.js` |",
    "| **Gelombang 3** | `AUDIT-101 s/d AUDIT-150` | **Tab Portofolio, Mutasi Kas, Performa/Lag & e-IPO Stockbit** (Banner roket e-IPO, filter 3 status, timeline 5-tahap, eliminasi serialisasi ganda snapshot). | `app.js`, `style.css`, `TradingFacade.java` |",
    "| **Gelombang 4** | `AUDIT-151 s/d AUDIT-200` | **Tab Bisnis Operasional, Holding, SDM & Modal Dialog** (Moral pegawai, jam kerja, kapasitas output, pencegahan crash slider 120%, dialog humanis). | `app.js`, `style.css`, `OperatingCompany.java` |",
    "| **Gelombang 5** | `AUDIT-201 s/d AUDIT-250` | **Akselerasi Kecepatan Harga Live, Pasar Offline & Matching Engine** (Tick per 1 detik, drift pasar offline 20 emiten, Stop Loss koridor ARB, RUPS abstain). | `TradingFacade.java`, `NpcAgent.java`, `MarketEngine.java` |",
    "| **Gelombang 6** | `AUDIT-251 s/d AUDIT-300` | **Katalog Multi-Aset, Donut Sektor, Aksesibilitas & Pengaturan** (Obligasi FR0091, Donut Chart proporsional, High Contrast border, slider volume audio). | `app.js`, `style.css`, `index.html` |",
    "",
    "---",
    "",
    "## II. CHECKLIST & PEMETAAN DETAIL 300 TASK EKSEKUSI",
    ""
]

waves = [
    ("GELOMBANG 1: HEADER SHELL, NAVIGASI, JAM BURSA & BERANDA (001 - 050)", items[0:50]),
    ("GELOMBANG 2: TAB PASAR, DETAIL SAHAM, TIKET ORDER & RADAR (051 - 100)", items[50:100]),
    ("GELOMBANG 3: TAB PORTOFOLIO, MUTASI KAS, PERFORMA & E-IPO (101 - 150)", items[100:150]),
    ("GELOMBANG 4: BISNIS OPERASIONAL, HOLDING, SDM & MODAL DIALOG (151 - 200)", items[150:200]),
    ("GELOMBANG 5: AKSELERASI HARGA LIVE, OFFLINE & MATCHING ENGINE (201 - 250)", items[200:250]),
    ("GELOMBANG 6: MULTI-ASET, DONUT SEKTOR, AKSESIBILITAS & SISTEM (251 - 300)", items[250:300])
]

for w_title, w_items in waves:
    lines.append(f"### {w_title}")
    lines.append("")
    lines.append("| ID Task | Prioritas | Kategori | Lokasi Kode | Ringkasan Sasaran Perbaikan |")
    lines.append("|---|:---:|---|---|---|")
    for it in w_items:
        lines.append(f"| `[{it['id']}]` | **{it['priority']}** | `{it['category']}` | `{it['location']}` | {it['title']} |")
    lines.append("")
    lines.append("---")
    lines.append("")

lines.extend([
    "## III. PROTOKOL VERIFIKASI SEBELUM TUNTAS (VERIFICATION BEFORE COMPLETION)",
    "",
    "Sesuai mandat ketat verifikasi rekayasa perangkat lunak:",
    "1. **Integritas Save Game:** Memastikan skema persistensi `save_game_v2.json` tidak mengalami kerusakan dan lolos validasi atomic recovery.",
    "2. **Uji Suite Pengujian Sintesa:** Mengeksekusi `python Work/run_synthesis_tests.py` dan memastikan 17/17 tes domain berstatus `OK (17 tests)` tanpa kegagalan.",
    "3. **Kompilasi & Build APK Android:** Menjalankan `./gradlew.bat assembleDebug` hingga berstatus `BUILD SUCCESSFUL`.",
    "4. **Inspeksi Visual Headless & Live Browser:** Menangkap tangkapan layar tampilan nyata pada resolusi mobile 412x915px dan mengonfirmasi bahwa tidak ada pemotongan layout atau tumpang tindih elemen visual.",
    ""
])

with open(out_md, "w", encoding="utf-8") as f:
    f.write("\n".join(lines))

print(f"Master Plan written to {out_md} ({len(lines)} lines)")
