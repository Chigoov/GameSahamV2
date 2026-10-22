"""Run A4 diagnosis on isolated QA runner."""
from pathlib import Path
import os
import subprocess

root = Path(__file__).resolve().parent.parent
run_dir = root / 'Artifacts/8-Anotasi-2026-10-22/run-01'
classes = run_dir / 'compile/classes'
classes.mkdir(parents=True, exist_ok=True)

app_classes = root / 'android/app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes'
if app_classes.is_dir():
    import shutil
    shutil.copytree(app_classes, classes, dirs_exist_ok=True)

json_jar = next((Path.home() / '.gradle/caches/modules-2/files-2.1/org.json/json/20240303').rglob('*.jar'))
android_jar = Path(os.environ['LOCALAPPDATA']) / 'Android/Sdk/platforms/android-36/android.jar'

cp = os.pathsep.join(map(str, [classes, json_jar, android_jar]))
jbin = Path('C:/Program Files/Android/Android Studio/jbr/bin')

# Find all changed/core production java files
core_srcs = [
    root / 'android/app/src/main/java/com/tradingglobal3d/core/clock/GameClock.java',
    root / 'android/app/src/main/java/com/tradingglobal3d/core/npc/NpcAgent.java',
    root / 'android/app/src/main/java/com/tradingglobal3d/core/npc/MarketEngine.java',
    root / 'android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java',
]

print("Compiling updated core classes...")
subprocess.run([
    str(jbin / 'javac.exe'),
    '-encoding', 'UTF-8',
    '-cp', cp,
    '-d', str(classes)] + [str(s) for s in core_srcs
], check=True)

# Compile
print("Compiling DiagnoseA4MarketTrace.java...")
subprocess.run([
    str(jbin / 'javac.exe'),
    '-encoding', 'UTF-8',
    '-cp', cp,
    '-d', str(classes),
    str(root / 'Work/DiagnoseA4MarketTrace.java')
], check=True)

# Run
print("Running DiagnoseA4MarketTrace...")
res = subprocess.run([
    str(jbin / 'java.exe'),
    '-cp', cp,
    'com.tradingglobal3d.diagnostic.DiagnoseA4MarketTrace'
], cwd=str(root))

print(f"Exit code: {res.returncode}")
