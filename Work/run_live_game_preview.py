"""Launcher server live game interaktif di port 8794 dengan TradingNativeBridge."""
from pathlib import Path
import os, sys, subprocess, shutil, time, urllib.request

root = Path(__file__).resolve().parent.parent
out = root / 'Artifacts/LiveGamePreview'
classes = out / 'classes'
save_dir = out / 'qa-save'
classes.mkdir(parents=True, exist_ok=True)
save_dir.mkdir(parents=True, exist_ok=True)

app_classes = root / 'android/app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes'
if app_classes.is_dir():
    shutil.copytree(app_classes, classes, dirs_exist_ok=True)

json_jar = next((Path.home() / '.gradle/caches/modules-2/files-2.1/org.json/json/20240303').rglob('*.jar'))
android_jar = Path(os.environ['LOCALAPPDATA']) / 'Android/Sdk/platforms/android-36/android.jar'
cp = os.pathsep.join(map(str, [classes, json_jar, android_jar]))
jbin = Path('C:/Program Files/Android/Android Studio/jbr/bin')

# Compile WireQaServer
subprocess.run([str(jbin/'javac.exe'), '-encoding', 'UTF-8', '-cp', cp, '-d', str(classes), str(root/'Work/WireQaServer.java')], check=True)

port = "8794"
assets_path = root / 'android/app/src/main/assets'
socket_dir = root / '.socket-qa'
socket_dir.mkdir(parents=True, exist_ok=True)

cmd = [
    str(jbin/'java.exe'),
    f"-Djdk.net.unixdomain.tmpdir={socket_dir}",
    "-cp", cp,
    "com.tradingglobal3d.mobile.WireQaServer",
    str(assets_path),
    str(save_dir),
    port
]

print(f"Starting WireQaServer on port {port}...")
proc = subprocess.Popen(cmd, cwd=str(root))

# Wait for server ready
for _ in range(15):
    time.sleep(1)
    try:
        req = urllib.request.urlopen(f"http://127.0.0.1:{port}/qa-meta")
        if req.status == 200:
            print(f"Server READY at http://127.0.0.1:{port}/")
            break
    except Exception:
        pass

# Keep process alive
proc.wait()
