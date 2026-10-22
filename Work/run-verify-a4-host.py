"""Compile and run VerifyA4DeterministicHost."""
from pathlib import Path
import os
import subprocess

root = Path(__file__).resolve().parent.parent
run_dir = root / 'Artifacts/8-Anotasi-2026-10-22/run-01'
classes = run_dir / 'compile/classes'
classes.mkdir(parents=True, exist_ok=True)

json_jar = next((Path.home() / '.gradle/caches/modules-2/files-2.1/org.json/json/20240303').rglob('*.jar'))
android_jar = Path(os.environ['LOCALAPPDATA']) / 'Android/Sdk/platforms/android-36/android.jar'

cp = os.pathsep.join(map(str, [classes, json_jar, android_jar]))
jbin = Path('C:/Program Files/Android/Android Studio/jbr/bin')

# Compile
print("Compiling VerifyA4DeterministicHost.java...")
subprocess.run([
    str(jbin / 'javac.exe'),
    '-encoding', 'UTF-8',
    '-cp', cp,
    '-d', str(classes),
    str(root / 'Work/VerifyA4DeterministicHost.java')
], check=True)

# Run
print("Running VerifyA4DeterministicHost...")
res = subprocess.run([
    str(jbin / 'java.exe'),
    '-cp', cp,
    'com.tradingglobal3d.diagnostic.VerifyA4DeterministicHost'
], cwd=str(root))

print(f"Exit code: {res.returncode}")
if res.returncode != 0:
    raise SystemExit(f"Execution failed with code {res.returncode}")
