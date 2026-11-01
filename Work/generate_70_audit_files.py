import json
import os

with open("Work/AUDIT-70-TEMUAN.json", "r", encoding="utf-8") as f:
    findings = json.load(f)

# 1. Build Markdown Report
md_lines = [
    "# LAPORAN AUDIT LENGKAP 70 TEMUAN UI & KONTEN GAME SAHAM",
    "**Proyek:** Game Saham 2D Portrait  ",
    "**Lokasi Root:** `C:\\Users\\HYPE AMD\\Downloads\\VIBE CODING\\GAME SAHAM`  ",
    "**Tanggal Audit:** 30 Oktober 2026  ",
    "**Status Audit:** SELESAI (Deteksi & Rekomendasi 70 Perubahan Signifikan Tanpa Mengganggu Gameplay)  ",
    "**Dokumen Acuan:** `PROMPT-AUDIT-UI-DAN-KONTEN.md`, `AGENTS.md`, `DESIGN.md`, `MASTER-PLAN-OFFLINE-DAN-REALISME-2026-10-28.md`  ",
    "**Hash Save Pemain Asli:** `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed` (100% Aman & Terverifikasi Utuh)  ",
    "",
    "---",
    "",
    "## I. Ringkasan Eksekutif & Statistik 70 Temuan",
    "",
    "Audit menyeluruh terhadap seluruh 5 tab utama dan 82 alur layar telah mengidentifikasi **tepat 70 elemen antarmuka, teks berlebih, slogan kosong, kebocoran istilah teknis developer, dan redundansi data** yang memenuhi layar pemain namun minim fungsi.",
    "",
    "| Prioritas | Jumlah Temuan | Fokus Masalah Utama |",
    "|---|:---:|---|",
    "| **TINGGI (Kritis UX)** | **18 Temuan** | Slogan pemakan ruang, duplikasi navigasi 80%, kode tiket sprint developer, form dropdown kuno, dan popup toast autosave mengganggu. |",
    "| **SEDANG (Redundansi & Clutter)** | **36 Temuan** | Pengulangan nominal kas/emiten berkali-kali, esai pembuka panjang di 15+ modal, tabel 20 rasio mentah tanpa lipatan, dan kalimat penjelas kaku. |",
    "| **RENDAH (Minor Polish)** | **16 Temuan** | Tombol mati tersembunyi di DOM, istilah kurung konsep developer, placeholder statis, dan pembersihan ikon emoji. |",
    "| **TOTAL** | **70 Temuan** | **Seluruhnya dapat ditinjau satu per satu tanpa mengganggu logika pasar atau data pemain.** |",
    "",
    "---",
    "",
    "## II. Daftar Rinci 70 Temuan Audit (AUDIT-01 s/d AUDIT-70)",
    ""
]

for item in findings:
    md_lines.append(f"### [{item['id']}] {item['title']}")
    md_lines.append(f"1. **Lokasi:** {item['location']}")
    md_lines.append(f"2. **Konten Saat Ini:** `{item['current']}`")
    md_lines.append(f"3. **Masalah:** {item['problem']}")
    md_lines.append(f"4. **Dampak terhadap Pemain:** {item['impact']}")
    md_lines.append(f"5. **Rekomendasi:** {item['recommendation']}")
    md_lines.append(f"6. **Usulan Konten Baru (After):** `{item['proposed']}`")
    md_lines.append(f"7. **Prioritas:** **{item['priority'].upper()}**")
    md_lines.append(f"8. **Kategori:** `{item['category']}`")
    md_lines.append("")
    md_lines.append("---")
    md_lines.append("")

with open("Work/AUDIT-UI-DAN-KONTEN-70-TEMUAN.md", "w", encoding="utf-8") as f:
    f.write("\n".join(md_lines))

print("Created Work/AUDIT-UI-DAN-KONTEN-70-TEMUAN.md")

# 2. Build Interactive HTML Dashboard
cards_html = []
for item in findings:
    p_class = item['priority'].lower()
    p_label = item['priority'].upper()
    cat_str = item['category']
    
    card = f"""
    <div class="finding-card" data-priority="{p_class}" data-category="{cat_str}">
      <div class="finding-header">
        <div class="finding-id">
          <span>[{item['id']}]</span>
          <span>{item['title']}</span>
        </div>
        <span class="badge-priority {p_class}">{p_label}</span>
      </div>
      <div class="finding-body">
        <div class="finding-meta">📍 Lokasi: {item['location']}</div>
        <div class="compare-grid">
          <div class="compare-col before">
            <div class="compare-title">❌ SEBELUM (BEFORE)</div>
            <div class="quote-box">{item['current']}</div>
            <p>{item['problem']}</p>
          </div>
          <div class="compare-col after">
            <div class="compare-title">✨ SESUDAH (AFTER)</div>
            <div class="quote-box">{item['proposed']}</div>
            <p>{item['recommendation']}</p>
          </div>
        </div>
        <div class="finding-detail-grid">
          <div class="fd-item"><strong>Dampak:</strong><p>{item['impact']}</p></div>
          <div class="fd-item"><strong>Kategori Area:</strong><p>{cat_str.upper()}</p></div>
        </div>
      </div>
    </div>
    """
    cards_html.append(card)

html_template = f"""<!DOCTYPE html>
<html lang="id">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Dashboard Lengkap 70 Temuan Audit UI & Konten — Game Saham</title>
<style>
:root {{
  --bg-dark: #0d1117;
  --bg-card: #161b22;
  --bg-subtle: #21262d;
  --border: #30363d;
  --accent: #E3BD7F;
  --text-main: #f0f6fc;
  --text-muted: #8b949e;
  --positive: #14F195;
  --negative: #FF5252;
  --warning: #F59E0B;
}}
* {{ box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }}
body {{ background: var(--bg-dark); color: var(--text-main); padding: 20px; line-height: 1.5; }}
.container {{ max-width: 1140px; margin: 0 auto; }}

.header-banner {{ background: var(--bg-card); border: 1px solid var(--border); border-radius: 16px; padding: 24px; margin-bottom: 24px; }}
.tag-badge {{ display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 0.75rem; font-weight: 700; background: var(--accent); color: #151515; margin-bottom: 10px; }}
h1 {{ font-size: 1.7rem; color: #fff; margin-bottom: 6px; }}
p.lead {{ color: var(--text-muted); font-size: 0.92rem; margin-bottom: 16px; }}

.stats-grid {{ display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-top: 14px; }}
@media (max-width: 768px) {{ .stats-grid {{ grid-template-columns: 1fr 1fr; }} }}
.stat-box {{ background: var(--bg-subtle); border: 1px solid var(--border); border-radius: 10px; padding: 12px 14px; }}
.stat-box small {{ display: block; font-size: 0.72rem; color: var(--text-muted); text-transform: uppercase; }}
.stat-box strong {{ font-size: 1.3rem; font-weight: 700; }}

/* Filter Tabs */
.filter-bar {{ display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 24px; position: sticky; top: 10px; z-index: 100; background: rgba(13, 17, 23, 0.95); backdrop-filter: blur(10px); padding: 10px 0; }}
.filter-btn {{ padding: 8px 14px; border-radius: 8px; border: 1px solid var(--border); background: var(--bg-card); color: var(--text-main); font-size: 0.8rem; font-weight: 600; cursor: pointer; transition: all 0.15s ease; }}
.filter-btn:hover {{ border-color: var(--accent); }}
.filter-btn.active {{ background: var(--accent); color: #151515; border-color: var(--accent); }}

/* Finding Card */
.finding-list {{ display: flex; flex-direction: column; gap: 16px; }}
.finding-card {{ background: var(--bg-card); border: 1px solid var(--border); border-radius: 14px; overflow: hidden; transition: border-color 0.2s; }}
.finding-card:hover {{ border-color: #484f58; }}

.finding-header {{ padding: 12px 18px; background: var(--bg-subtle); border-bottom: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center; }}
.finding-id {{ font-weight: 700; font-size: 0.95rem; color: #fff; display: flex; align-items: center; gap: 8px; }}
.badge-priority {{ padding: 3px 8px; border-radius: 6px; font-size: 0.7rem; font-weight: 700; text-transform: uppercase; }}
.badge-priority.tinggi {{ background: rgba(255, 82, 82, 0.15); color: var(--negative); border: 1px solid rgba(255, 82, 82, 0.4); }}
.badge-priority.sedang {{ background: rgba(245, 158, 11, 0.15); color: var(--warning); border: 1px solid rgba(245, 158, 11, 0.4); }}
.badge-priority.rendah {{ background: rgba(56, 139, 253, 0.15); color: #58a6ff; border: 1px solid rgba(56, 139, 253, 0.4); }}

.finding-body {{ padding: 16px 18px; }}
.finding-meta {{ font-size: 0.78rem; color: var(--accent); margin-bottom: 12px; }}

.compare-grid {{ display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 14px; }}
@media (max-width: 768px) {{ .compare-grid {{ grid-template-columns: 1fr; }} }}

.compare-col {{ border-radius: 10px; padding: 12px; font-size: 0.82rem; }}
.compare-col.before {{ background: rgba(255, 82, 82, 0.08); border: 1px solid rgba(255, 82, 82, 0.25); }}
.compare-col.after {{ background: rgba(20, 241, 149, 0.08); border: 1px solid rgba(20, 241, 149, 0.25); }}

.compare-title {{ font-weight: 700; font-size: 0.75rem; text-transform: uppercase; margin-bottom: 6px; display: flex; justify-content: space-between; }}
.compare-col.before .compare-title {{ color: #ff7b72; }}
.compare-col.after .compare-title {{ color: var(--positive); }}

.quote-box {{ background: rgba(0,0,0,0.3); padding: 8px 10px; border-radius: 6px; font-family: monospace; font-size: 0.8rem; margin: 6px 0; word-break: break-all; }}
.compare-col.before .quote-box {{ border-left: 3px solid #ff7b72; color: #ff7b72; text-decoration: line-through; }}
.compare-col.after .quote-box {{ border-left: 3px solid var(--positive); color: var(--positive); }}

.finding-detail-grid {{ display: grid; grid-template-columns: 1fr 1fr; gap: 12px; background: rgba(0,0,0,0.2); padding: 12px; border-radius: 8px; font-size: 0.8rem; border: 1px solid rgba(255,255,255,0.05); }}
@media (max-width: 600px) {{ .finding-detail-grid {{ grid-template-columns: 1fr; }} }}
.fd-item strong {{ display: block; color: #fff; font-size: 0.72rem; text-transform: uppercase; margin-bottom: 2px; }}
.fd-item p {{ color: var(--text-muted); margin: 0; }}
</style>
</head>
<body>

<div class="container">

  <!-- Header Banner -->
  <div class="header-banner">
    <span class="tag-badge">AUDIT KOMPREHENSIF UI & KONTEN · 30 OKTOBER 2026</span>
    <h1>Daftar Lengkap 70 Temuan Elemen Berlebih & Perbandingan Before vs After</h1>
    <p class="lead">Berikut adalah seluruh 70 temuan audit yang telah diidentifikasi pada 5 tab utama dan 82 alur layar. Anda dapat memfilter berdasarkan tingkat prioritas atau area layar untuk meninjaunya satu per satu.</p>
    
    <div class="stats-grid">
      <div class="stat-box">
        <small>Total Temuan Audit</small>
        <strong>70 Item</strong>
      </div>
      <div class="stat-box">
        <small>Prioritas Tinggi (Kritis UX)</small>
        <strong style="color: var(--negative);">18 Item</strong>
      </div>
      <div class="stat-box">
        <small>Prioritas Sedang (Redundan)</small>
        <strong style="color: var(--warning);">36 Item</strong>
      </div>
      <div class="stat-box">
        <small>Prioritas Rendah (Minor Polish)</small>
        <strong style="color: #58a6ff;">16 Item</strong>
      </div>
    </div>
  </div>

  <!-- Filter Bar -->
  <div class="filter-bar">
    <button class="filter-btn active" onclick="filterFindings('all', event)">Semua (70)</button>
    <button class="filter-btn" onclick="filterFindings('tinggi', event)">🔥 Prioritas Tinggi (18)</button>
    <button class="filter-btn" onclick="filterFindings('sedang', event)">⚠️ Prioritas Sedang (36)</button>
    <button class="filter-btn" onclick="filterFindings('rendah', event)">ℹ️ Prioritas Rendah (16)</button>
    <button class="filter-btn" onclick="filterFindings('beranda', event)">Tab Beranda (10)</button>
    <button class="filter-btn" onclick="filterFindings('pasar', event)">Tab Pasar (8)</button>
    <button class="filter-btn" onclick="filterFindings('portofolio', event)">Tab Portofolio (8)</button>
    <button class="filter-btn" onclick="filterFindings('bisnis', event)">Tab Bisnis (9)</button>
    <button class="filter-btn" onclick="filterFindings('lainnya', event)">Tab Lainnya (7)</button>
    <button class="filter-btn" onclick="filterFindings('detail', event)">Detail Saham (7)</button>
    <button class="filter-btn" onclick="filterFindings('order', event)">Tiket Order (5)</button>
    <button class="filter-btn" onclick="filterFindings('modal', event)">Modal & Dialog (11)</button>
  </div>

  <!-- Finding Cards Container -->
  <div class="finding-list" id="findings-container">
    {"".join(cards_html)}
  </div>
</div>

<script>
function filterFindings(category, e) {{
  const cards = document.querySelectorAll('.finding-card');
  const buttons = document.querySelectorAll('.filter-btn');
  buttons.forEach(b => b.classList.remove('active'));
  if (e && e.target) e.target.classList.add('active');

  cards.forEach(card => {{
    if (category === 'all') {{
      card.style.display = 'block';
    }} else if (category === 'tinggi' || category === 'sedang' || category === 'rendah') {{
      card.style.display = card.getAttribute('data-priority') === category ? 'block' : 'none';
    }} else {{
      const cats = card.getAttribute('data-category') || '';
      card.style.display = cats.includes(category) ? 'block' : 'none';
    }}
  }});
}}
</script>

</body>
</html>
"""

with open("Artifacts/Audit-Before-After/dashboard-70-temuan-lengkap.html", "w", encoding="utf-8") as f:
    f.write(html_template)

print("Created Artifacts/Audit-Before-After/dashboard-70-temuan-lengkap.html")
