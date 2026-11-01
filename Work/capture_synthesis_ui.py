import subprocess, os

edge_path = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
preview_dir = r"C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM\Artifacts\Audit-Before-After"

screens = [
    ("16-macro-badge-header.png", "http://127.0.0.1:18917/?no_welcome=1&tab=beranda&dismiss_absence=1"),
    ("17-stock-detail-fundamental-strip.png", "http://127.0.0.1:18917/?no_welcome=1&stock=IDX:BBCA"),
]

for filename, url in screens:
    out = os.path.join(preview_dir, filename)
    cmd = [
        edge_path,
        "--headless",
        "--disable-gpu",
        "--hide-scrollbars",
        "--window-size=480,1200",
        "--virtual-time-budget=3500",
        f"--screenshot={out}",
        url
    ]
    subprocess.run(cmd, check=True)
    if os.path.exists(out):
        print(f"Captured {filename}: {os.path.getsize(out)} bytes")
    else:
        print(f"Failed {filename}")
