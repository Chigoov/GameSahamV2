import json
import os

repo = r"C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM"
json_path = os.path.join(repo, "Work/AUDIT-300-TEMUAN-PARIPURNA.json")
out_md = os.path.join(repo, "Work/LAPORAN-BEFORE-AFTER-300-ITEM.md")

with open(json_path, "r", encoding="utf-8") as f:
    items = json.load(f)

lines = [
    "# LAPORAN PERBANDINGAN LENGKAP BEFORE VS AFTER (300 ITEM GAME SAHAM)",
    "## Dokumentasi Menyeluruh Seluruh Elemen UI, Kalimat, dan Logika Sistem yang Berubah",
    f"**Target Proyek:** `{repo}`  ",
    "**Tanggal Terbit Laporan:** 31 Oktober 2026  ",
    f"**Total Item Terverifikasi:** Tepat {len(items)} Item  ",
    "",
    "---",
    "",
    "## DAFTAR RINCI BEFORE VS AFTER (ITEM 001 s/d 300)",
    ""
]

for idx, it in enumerate(items):
    lines.append(f"### [{it['id']}] {it['title']}")
    lines.append(f"- **Kategori:** `{it['category']}` | **Prioritas:** **{it['priority']}**")
    lines.append(f"- **Lokasi Berkas:** `{it['location']}`")
    lines.append("- **Kondisi SEBELUM (Before):**")
    lines.append(f"  > `{it['before']}`")
    lines.append(f"- **Akar Masalah:** {it['issue']}")
    lines.append(f"- **Dampak terhadap Pemain:** {it['impact']}")
    lines.append("- **Kondisi SESUDAH (After):**")
    lines.append(f"  > `{it['after']}`")
    lines.append("")
    lines.append("---")
    lines.append("")

with open(out_md, "w", encoding="utf-8") as f:
    f.write("\n".join(lines))

print(f"Before-After report written to {out_md} ({len(lines)} lines)")
