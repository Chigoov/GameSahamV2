from pathlib import Path
import os, subprocess, time

root = Path("C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM").resolve()
run_dir = root / "Artifacts/Owner-Revision-14-2026-10-22/run-01"
classes = (run_dir / "compile/classes").resolve()
qa_save = (run_dir / "qa-save").resolve()
qa_save.mkdir(parents=True, exist_ok=True)

for f in qa_save.iterdir():
    if f.is_file(): f.unlink()

json_jar = next((Path.home() / ".gradle/caches/modules-2/files-2.1/org.json/json/20240303").rglob("*.jar")).resolve()
android_jar = (Path(os.environ["LOCALAPPDATA"]) / "Android/Sdk/platforms/android-36/android.jar").resolve()
cp = os.pathsep.join(map(str, [classes, json_jar, android_jar]))
jbin = Path("C:/Program Files/Android/Android Studio/jbr/bin")

server_proc = subprocess.Popen([
    str(jbin / "java.exe"), "-Dqa.port=8794", "-cp", cp,
    "com.tradingglobal3d.mobile.WireQaServer",
    str(root / "android/app/src/main/assets"), str(qa_save), "8794"
], cwd=str(root))
time.sleep(2)

try:
    node_exe = Path.home() / ".hermes/tools/node-26.7.0-win32-x64/node.exe"
    node_modules = Path.home() / ".hermes/hermes-agent/node_modules"
    env = os.environ.copy()
    env["NODE_PATH"] = str(node_modules)
    env["QA_PORT"] = "8794"

    res = subprocess.run([str(node_exe), str(root / "Work/verify-e4-boosters-perks.js")], env=env, check=True)
    print("Tahap E4 Browser verification exit code:", res.returncode)
finally:
    server_proc.kill()
