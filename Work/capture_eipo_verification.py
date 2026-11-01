import time
import subprocess

# Capture the live UI on http://localhost:8794/
# 1. Capture base view
cmd1 = [
    "C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe",
    "--headless", "--disable-gpu", "--hide-scrollbars",
    "--window-size=412,892",
    "--screenshot=C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Artifacts/Audit-Before-After/19-live-eipo-stockbit.png",
    "http://localhost:8794/"
]
subprocess.run(cmd1, capture_output=True)
print("Captured 19-live-eipo-stockbit.png")
