import subprocess
import os

edge_path = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
preview_dir = r"C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM\Artifacts\Audit-Before-After"
out = os.path.join(preview_dir, "09-stock-detail-3x3-stats.png")

url = "http://127.0.0.1:18917/?no_welcome=1&stock=IDX:BBCA"

cmd = [
    edge_path,
    "--headless",
    "--disable-gpu",
    "--hide-scrollbars",
    "--window-size=412,915",
    "--virtual-time-budget=3500",
    f"--screenshot={out}",
    url
]
subprocess.run(cmd, check=True)
if os.path.exists(out):
    print(f"Captured {out}: {os.path.getsize(out)} bytes")
else:
    print("Capture failed")
