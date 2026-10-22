"""Compile only the QA host against already compiled production classes, then run it."""
from pathlib import Path
import os
import subprocess

root = Path(__file__).resolve().parent.parent
out = root / 'Artifacts/Wire-Integration-2026-10-21'
classes = out / 'compile/classes'
classes.mkdir(parents=True, exist_ok=True)

app_classes = root / 'android/app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes'
if app_classes.is_dir():
    import shutil
    shutil.copytree(app_classes, classes, dirs_exist_ok=True)

if not (classes / 'com/tradingglobal3d/mobile/TradingWebBridge.class').is_file():
    raise SystemExit('Compile actual domain/bridge first with existing runner -OutputDirectory Artifacts/Wire-Integration-2026-10-21/compile')
json_jar = next((Path.home() / '.gradle/caches/modules-2/files-2.1/org.json/json/20240303').rglob('*.jar'))
cp = os.pathsep.join(map(str, [classes, json_jar, Path(os.environ['LOCALAPPDATA']) / 'Android/Sdk/platforms/android-36/android.jar']))
jbin = Path('C:/Program Files/Android/Android Studio/jbr/bin')
subprocess.run([str(jbin/'javac.exe'), '-encoding', 'UTF-8', '-cp', cp, '-d', str(classes), str(root/'Work/WireQaServer.java')], check=True)
subprocess.run([str(jbin/'java.exe'), '-Djdk.net.unixdomain.tmpdir='+str(root/'.socket-qa'), '-cp', cp, 'com.tradingglobal3d.mobile.WireQaServer', str(root/'android/app/src/main/assets'), str(out/'qa-save')], cwd=root, check=True)
