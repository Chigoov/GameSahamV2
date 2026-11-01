import json, os, re

repo = r"C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM"
html_file = os.path.join(repo, "android/app/src/main/assets/www/index.html")
js_file = os.path.join(repo, "android/app/src/main/assets/www/app.js")
css_file = os.path.join(repo, "android/app/src/main/assets/www/style.css")

with open(html_file, "r", encoding="utf-8") as f:
    html = f.read()

with open(js_file, "r", encoding="utf-8") as f:
    js = f.read()

with open(css_file, "r", encoding="utf-8") as f:
    css = f.read()

findings = []

def add_finding(fid, title, category, priority, location, before, issue, impact, after):
    findings.append({
        "id": fid,
        "title": title,
        "category": category, # "BUG", "KESALAHAN UI / LAYOUT", "PEMBOROSAN UI & REDUNDANSI TEKS"
        "priority": priority, # "TINGGI", "SEDANG", "RENDAH"
        "location": location,
        "before": before,
        "issue": issue,
        "impact": impact,
        "after": after
    })

print("Helper ready.")
