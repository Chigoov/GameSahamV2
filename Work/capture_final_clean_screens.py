import subprocess
import os
import time

edge_path = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
preview_dir = r"C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM\Artifacts\Audit-Before-After"

screens = [
    ("01-home-screen-clean.png", "http://127.0.0.1:18917/"),
]

for filename, url in screens:
    out = os.path.join(preview_dir, filename)
    cmd = [
        edge_path,
        "--headless",
        "--disable-gpu",
        "--hide-scrollbars",
        "--window-size=412,915",
        f"--screenshot={out}",
        url
    ]
    subprocess.run(cmd, check=True)
    print(f"Captured {filename}, size: {os.path.getsize(out)} bytes")
