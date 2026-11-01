import subprocess, os

edge_path = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
preview_dir = r"C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM\Artifacts\Audit-Before-After"
out = os.path.join(preview_dir, "14-profile-settings-toggle.png")

# Open with profile modal on PENGATURAN tab
url = "http://127.0.0.1:18917/?no_welcome=1&open_profile=PENGATURAN"

cmd = [
    edge_path,
    "--headless",
    "--disable-gpu",
    "--hide-scrollbars",
    "--window-size=480,1400",
    "--virtual-time-budget=3500",
    f"--screenshot={out}",
    url
]
subprocess.run(cmd, check=True)
print("Captured 14-profile-settings-toggle.png:", os.path.exists(out), os.path.getsize(out))
