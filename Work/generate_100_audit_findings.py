import os, re, json

repo = r"C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM"
html_path = os.path.join(repo, "android/app/src/main/assets/www/index.html")
js_path = os.path.join(repo, "android/app/src/main/assets/www/app.js")
css_path = os.path.join(repo, "android/app/src/main/assets/www/style.css")

with open(html_path, "r", encoding="utf-8") as f:
    html = f.read()

with open(js_path, "r", encoding="utf-8") as f:
    js = f.read()

with open(css_path, "r", encoding="utf-8") as f:
    css = f.read()

print("Files successfully read.")
