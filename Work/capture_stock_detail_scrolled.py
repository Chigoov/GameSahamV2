import subprocess
import os

edge_path = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
preview_dir = r"C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM\Artifacts\Audit-Before-After"
out = os.path.join(preview_dir, "10-stock-detail-scrolled.png")

# Use a node script with puppeteer/playwright or edge with window.scrollBy
# Or edge with taller window size (e.g. 412x1400)
cmd = [
    edge_path,
    "--headless",
    "--disable-gpu",
    "--hide-scrollbars",
    "--window-size=412,1600",
    "--virtual-time-budget=3500",
    f"--screenshot={out}",
    "http://127.0.0.1:18917/?no_welcome=1&stock=IDX:BBCA"
]
subprocess.run(cmd, check=True)
if os.path.exists(out):
    print(f"Captured {out}: {os.path.getsize(out)} bytes")
else:
    print("Capture failed")
