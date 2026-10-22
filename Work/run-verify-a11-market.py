"""Run verify-a11-market-redesign.js with isolated WireQaServer."""
from pathlib import Path
import os
import subprocess
import time

root = Path(__file__).resolve().parent.parent
run_dir = root / 'Artifacts/8-Anotasi-2026-10-22/run-01'
classes = run_dir / 'compile/classes'
qa_save = run_dir / 'qa-save'
qa_save.mkdir(parents=True, exist_ok=True)

# Clean qa_save before test
for f in qa_save.iterdir():
    if f.is_file():
        f.unlink()

json_jar = next((Path.home() / '.gradle/caches/modules-2/files-2.1/org.json/json/20240303').rglob('*.jar'))
android_jar = Path(os.environ['LOCALAPPDATA']) / 'Android/Sdk/platforms/android-36/android.jar'
cp = os.pathsep.join(map(str, [classes, json_jar, android_jar]))
jbin = Path('C:/Program Files/Android/Android Studio/jbr/bin')

# Start QA server on port 8794
server_proc = subprocess.Popen([
    str(jbin / 'java.exe'),
    '-Dqa.port=8794',
    '-cp', cp,
    'com.tradingglobal3d.mobile.WireQaServer',
    str(root / 'android/app/src/main/assets'),
    str(qa_save),
    '8794'
], cwd=str(root))

print("Started WireQaServer on port 8794, PID:", server_proc.pid)
time.sleep(2)

try:
    node_exe = Path.home() / '.hermes/tools/node-26.7.0-win32-x64/node.exe'
    node_modules = Path.home() / '.hermes/hermes-agent/node_modules'
    env = os.environ.copy()
    env['NODE_PATH'] = str(node_modules)
    env['QA_PORT'] = '8794'

    print("Running verify-a11-market-redesign.js...")
    res = subprocess.run([
        str(node_exe),
        str(root / 'Work/verify-a11-market-redesign.js')
    ], env=env, check=True)
    print("Node verification exit code:", res.returncode)

finally:
    server_proc.terminate()
    try:
        server_proc.wait(timeout=3)
    except:
        server_proc.kill()
    print("WireQaServer terminated.")
