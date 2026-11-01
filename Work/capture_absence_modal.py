import subprocess, os

edge_path = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
preview_dir = r"C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM\Artifacts\Audit-Before-After"
out = os.path.join(preview_dir, "15-absence-report-modal.png")

url = "http://127.0.0.1:18917/?no_welcome=1&test_absence=1"

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
print("Captured 15-absence-report-modal.png:", os.path.exists(out), os.path.getsize(out))
