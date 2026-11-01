import json, os

repo = r"C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM"
json_path = os.path.join(repo, "Work/AUDIT-100-TEMUAN.json")
out_html1 = os.path.join(repo, "Work/preview-100-audit-before-after.html")
out_html2 = os.path.join(repo, "android/app/src/main/assets/www/dashboard-100-temuan-lengkap.html")

with open(json_path, "r", encoding="utf-8") as f:
    findings = json.load(f)

html_content = f"""<!DOCTYPE html>
<html lang="id">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Dashboard Interaktif 100 Temuan: Before vs After UI Game Saham</title>
<style>
:root {{
  --bg-base: #0F172A;
  --bg-card: #1E293B;
  --bg-card-subtle: #334155;
  --accent: #F59E0B;
  --accent-gold: #E3BD7F;
  --text-main: #F8FAFC;
  --text-muted: #94A3B8;
  --border: #334155;
  --border-focus: #38BDF8;
  --positive: #10B981;
  --negative: #EF4444;
  --warning: #F59E0B;
  --navy-dark: #0B1120;
}}
* {{ box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }}
body {{ background: var(--bg-base); color: var(--text-main); padding: 18px; line-height: 1.5; font-size: 14px; }}
.container {{ max-width: 1100px; margin: 0 auto; }}

/* Header Summary */
.header-box {{
  background: linear-gradient(135deg, #1E293B 0%, #0F172A 100%);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.4);
}}
.badge-gold {{ display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 0.75rem; font-weight: 700; background: var(--accent-gold); color: #000; margin-bottom: 10px; }}
h1 {{ font-size: 1.7rem; color: #FFF; margin-bottom: 6px; }}
.subheading {{ color: var(--text-muted); font-size: 0.92rem; margin-bottom: 18px; }}

.stats-grid {{ display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px; margin-top: 14px; }}
.stat-card {{ background: rgba(15, 23, 42, 0.6); border: 1px solid var(--border); border-radius: 12px; padding: 14px; text-align: center; }}
.stat-num {{ font-size: 1.8rem; font-weight: 800; color: #FFF; }}
.stat-num.bug {{ color: var(--negative); }}
.stat-num.ui {{ color: var(--border-focus); }}
.stat-num.copy {{ color: var(--accent-gold); }}
.stat-label {{ font-size: 0.78rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; margin-top: 2px; }}

/* Filter & Controls */
.controls-bar {{
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}}
.filter-row {{ display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }}
.filter-btn {{
  padding: 6px 14px;
  border-radius: 20px;
  border: 1px solid var(--border);
  background: var(--bg-base);
  color: var(--text-muted);
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}}
.filter-btn:hover {{ border-color: var(--border-focus); color: #FFF; }}
.filter-btn.active {{ background: var(--border-focus); color: #000; border-color: var(--border-focus); font-weight: 700; }}

.search-box {{
  width: 100%;
  padding: 10px 14px;
  background: var(--bg-base);
  border: 1px solid var(--border);
  border-radius: 10px;
  color: #FFF;
  font-size: 0.88rem;
  outline: none;
}}
.search-box:focus {{ border-color: var(--border-focus); }}

/* Findings List */
.findings-container {{ display: flex; flex-direction: column; gap: 16px; }}
.finding-card {{
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 14px;
  overflow: hidden;
  transition: border-color 0.2s ease;
}}
.finding-card:hover {{ border-color: rgba(56, 189, 248, 0.4); }}

.card-top {{
  padding: 14px 18px;
  background: rgba(15, 23, 42, 0.5);
  border-bottom: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}}
.fid-title {{ display: flex; align-items: center; gap: 10px; }}
.fid-tag {{ font-family: monospace; font-weight: 700; color: var(--accent-gold); font-size: 0.9rem; background: rgba(227, 189, 127, 0.15); padding: 3px 8px; border-radius: 6px; }}
.finding-card h3 {{ font-size: 0.98rem; font-weight: 700; color: #FFF; }}

.tags-row {{ display: flex; gap: 6px; align-items: center; }}
.pill {{ padding: 3px 8px; border-radius: 6px; font-size: 0.7rem; font-weight: 700; text-transform: uppercase; }}
.pill.cat-bug {{ background: rgba(239, 68, 68, 0.2); color: var(--negative); border: 1px solid rgba(239, 68, 68, 0.4); }}
.pill.cat-ui {{ background: rgba(56, 189, 248, 0.2); color: var(--border-focus); border: 1px solid rgba(56, 189, 248, 0.4); }}
.pill.cat-copy {{ background: rgba(227, 189, 127, 0.2); color: var(--accent-gold); border: 1px solid rgba(227, 189, 127, 0.4); }}
.pill.prio-tinggi {{ background: #EF4444; color: #FFF; }}
.pill.prio-sedang {{ background: #F59E0B; color: #000; }}
.pill.prio-rendah {{ background: #64748B; color: #FFF; }}

.card-body {{ padding: 18px; }}
.loc-text {{ font-size: 0.78rem; color: var(--text-muted); margin-bottom: 12px; font-family: monospace; }}

.comparison-box {{
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-bottom: 14px;
}}
@media (max-width: 768px) {{ .comparison-box {{ grid-template-columns: 1fr; }} }}

.col-before, .col-after {{
  border-radius: 10px;
  padding: 12px 14px;
  font-size: 0.83rem;
  display: flex;
  flex-direction: column;
  gap: 6px;
}}
.col-before {{ background: rgba(239, 68, 68, 0.08); border: 1px solid rgba(239, 68, 68, 0.25); }}
.col-after {{ background: rgba(16, 185, 129, 0.08); border: 1px solid rgba(16, 185, 129, 0.25); }}

.col-label {{ font-size: 0.72rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; }}
.col-before .col-label {{ color: #FCA5A5; }}
.col-after .col-label {{ color: #6EE7B7; }}

.code-snippet {{ font-family: monospace; font-size: 0.8rem; padding: 6px 10px; border-radius: 6px; word-break: break-all; }}
.col-before .code-snippet {{ background: rgba(0, 0, 0, 0.35); color: #F87171; text-decoration: line-through; }}
.col-after .code-snippet {{ background: rgba(0, 0, 0, 0.35); color: #34D399; font-weight: 600; }}

.meta-expl {{ font-size: 0.8rem; color: var(--text-muted); line-height: 1.4; }}
.meta-expl strong {{ color: var(--text-main); }}
</style>
</head>
<body>
<div class="container">
  <div class="header-box">
    <span class="badge-gold">PORTRAIT AUDIT & ERGONOMICS</span>
    <h1>Visual Preview 100 Temuan: Before vs After</h1>
    <p class="subheading">Inventaris komprehensif audit bug sistem, perbaikan tata letak UI, dan pembersihan redundansi teks pada Game Saham 2D Portrait.</p>
    
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-num bug">15</div>
        <div class="stat-label">Bug & Logika Sistem</div>
      </div>
      <div class="stat-card">
        <div class="stat-num ui">35</div>
        <div class="stat-label">Kesalahan UI & Layout</div>
      </div>
      <div class="stat-card">
        <div class="stat-num copy">50</div>
        <div class="stat-label">Pemborosan Teks & Jargon</div>
      </div>
      <div class="stat-card">
        <div class="stat-num" style="color:var(--positive);">100</div>
        <div class="stat-label">Total Temuan Terverifikasi</div>
      </div>
    </div>
  </div>

  <div class="controls-bar">
    <div class="filter-row">
      <span style="font-size:0.8rem; font-weight:700; color:var(--text-muted); margin-right:4px;">Kategori:</span>
      <button class="filter-btn active" data-cat="all">Semua (100)</button>
      <button class="filter-btn" data-cat="BUG">Bug (15)</button>
      <button class="filter-btn" data-cat="KESALAHAN UI / LAYOUT">Kesalahan UI (35)</button>
      <button class="filter-btn" data-cat="PEMBOROSAN UI & REDUNDANSI TEKS">Pemborosan Teks (50)</button>
    </div>
    <div class="filter-row">
      <span style="font-size:0.8rem; font-weight:700; color:var(--text-muted); margin-right:4px;">Prioritas:</span>
      <button class="filter-btn active" data-prio="all">Semua Prioritas</button>
      <button class="filter-btn" data-prio="TINGGI">Tinggi (22)</button>
      <button class="filter-btn" data-prio="SEDANG">Sedang (62)</button>
      <button class="filter-btn" data-prio="RENDAH">Rendah (16)</button>
    </div>
    <input type="text" id="searchInput" class="search-box" placeholder="🔍 Cari berdasarkan ID ([AUDIT-XX]), kata kunci, berkas, atau teks...">
  </div>

  <div class="findings-container" id="findingsList">
"""

for item in findings:
    cat_class = "cat-bug" if "BUG" in item["category"] else ("cat-ui" if "UI" in item["category"] else "cat-copy")
    prio_class = "prio-tinggi" if item["priority"] == "TINGGI" else ("prio-sedang" if item["priority"] == "SEDANG" else "prio-rendah")
    
    html_content += f"""
    <div class="finding-card" data-cat="{item['category']}" data-prio="{item['priority']}" data-search="{item['id']} {item['title']} {item['location']} {item['before']} {item['after']}".toLowerCase()>
      <div class="card-top">
        <div class="fid-title">
          <span class="fid-tag">[{item['id']}]</span>
          <h3>{item['title']}</h3>
        </div>
        <div class="tags-row">
          <span class="pill {cat_class}">{item['category']}</span>
          <span class="pill {prio_class}">{item['priority']}</span>
        </div>
      </div>
      <div class="card-body">
        <div class="loc-text">📍 Lokasi Berkas: {item['location']}</div>
        <div class="comparison-box">
          <div class="col-before">
            <span class="col-label">🔴 Kondisi Eksisting (Before)</span>
            <div class="code-snippet">{item['before']}</div>
            <p class="meta-expl"><strong>Masalah:</strong> {item['issue']}</p>
            <p class="meta-expl"><strong>Dampak:</strong> {item['impact']}</p>
          </div>
          <div class="col-after">
            <span class="col-label">🟢 Usulan Perbaikan (After)</span>
            <div class="code-snippet">{item['after']}</div>
            <p class="meta-expl"><strong>Tujuan:</strong> Meningkatkan ergonomi, kenyamanan, atau keakuratan logika transaksi.</p>
          </div>
        </div>
      </div>
    </div>
    """

html_content += """
  </div>
</div>

<script>
const searchInput = document.getElementById('searchInput');
const catBtns = document.querySelectorAll('[data-cat]');
const prioBtns = document.querySelectorAll('[data-prio]');
const cards = document.querySelectorAll('.finding-card');

let activeCat = 'all';
let activePrio = 'all';

function filterCards() {
  const query = searchInput.value.toLowerCase().trim();
  cards.forEach(card => {
    const cat = card.dataset.cat;
    const prio = card.dataset.prio;
    const searchData = card.dataset.search;

    const matchCat = (activeCat === 'all' || cat.includes(activeCat));
    const matchPrio = (activePrio === 'all' || prio === activePrio);
    const matchQuery = (!query || searchData.includes(query));

    if (matchCat && matchPrio && matchQuery) {
      card.style.display = 'block';
    } else {
      card.style.display = 'none';
    }
  });
}

catBtns.forEach(btn => {
  btn.addEventListener('click', () => {
    catBtns.forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    activeCat = btn.dataset.cat;
    filterCards();
  });
});

prioBtns.forEach(btn => {
  btn.addEventListener('click', () => {
    prioBtns.forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    activePrio = btn.dataset.prio;
    filterCards();
  });
});

searchInput.addEventListener('input', filterCards);
</script>
</body>
</html>
"""

with open(out_html1, "w", encoding="utf-8") as f:
    f.write(html_content)

with open(out_html2, "w", encoding="utf-8") as f:
    f.write(html_content)

print(f"Generated {out_html1} and {out_html2} successfully.")
