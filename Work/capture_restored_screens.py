import subprocess
import os

edge_path = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
preview_dir = r"C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM\Artifacts\Audit-Before-After"

screens = [
    ("07-home-restored-full.png", "http://127.0.0.1:18917/?no_welcome=1"),
    ("08-pasar-porto-combined.png", "http://127.0.0.1:18917/?no_welcome=1&tab=pasar_porto"),
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
    if os.path.exists(out):
        print(f"Captured {filename}: {os.path.getsize(out)} bytes")
    else:
        print(f"Failed to capture {filename}")
