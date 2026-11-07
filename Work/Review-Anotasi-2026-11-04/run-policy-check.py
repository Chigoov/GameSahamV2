from pathlib import Path
import os
import subprocess
import time
import sys
import shutil

ROOT = Path(__file__).resolve().parents[2]
out = ROOT / 'Artifacts/Annotation-Execution' / str(time.time_ns())
classes = out / 'classes'
classes.mkdir(parents=True)
jdk = Path('C:/Program Files/Android/Android Studio/jbr/bin')
jars = [next((Path.home() / '.gradle/caches/modules-2/files-2.1' / artifact / version).rglob('*.jar'))
        for artifact, version in [('org.json/json', '20240303'), ('junit/junit', '4.13.2'), ('org.hamcrest/hamcrest-core', '1.3')]]
android_jar = Path(os.environ['LOCALAPPDATA']) / 'Android/Sdk/platforms/android-36/android.jar'
cp = os.pathsep.join(map(str, [classes, *jars, android_jar]))
sources = list((ROOT / 'android/app/src/main/java/com/tradingglobal3d/core').rglob('*.java'))
sources += [ROOT / 'android/app/src/test/java/com/tradingglobal3d/core/OwnerAnnotationPolicyTest.java']
sources += [ROOT / 'android/app/src/main/java/com/tradingglobal3d/mobile' / name
            for name in ['TradingWebBridge.java', 'MainActivity.java']]
suite_names = ['OwnerAnnotationPolicyTest', *sys.argv[1:]]
for name in suite_names:
    if not name.isidentifier():
        raise ValueError('Invalid test class name')
sources += [ROOT / 'android/app/src/test/java/com/tradingglobal3d/core' / (name + '.java') for name in suite_names[1:]]
sources += [ROOT / 'android/app/src/test/java/com/tradingglobal3d/core/GameTestSupport.java']
shutil.copytree(ROOT / 'android/app/src/main/assets/data', out / 'src/main/assets/data')
(out / 'tmp').mkdir()
argfile = out / 'sources.txt'
argfile.write_text('\n'.join('"' + p.as_posix() + '"' for p in sources), encoding='utf8')
with (out / 'compile.log').open('w', encoding='utf8') as log:
    result = subprocess.run([str(jdk/'javac.exe'), '-J-Xmx384m', '-encoding', 'UTF-8', '-cp', cp,
                             '-d', str(classes), '@'+str(argfile)], stdout=log, stderr=log, cwd=ROOT)
if result.returncode:
    print((out/'compile.log').read_text(encoding='utf8'))
    raise SystemExit(result.returncode)
result = subprocess.run([str(jdk/'java.exe'), '-Xmx384m', '-Djava.io.tmpdir='+str(out/'tmp'), '-Dannotation.root='+str(ROOT), '-cp', cp, 'org.junit.runner.JUnitCore',
                         *['com.tradingglobal3d.core.' + name for name in suite_names]], capture_output=True, text=True, cwd=out)
(out/'test.log').write_text(result.stdout+result.stderr, encoding='utf8')
print(result.stdout)
print('EVIDENCE='+str(out))
raise SystemExit(result.returncode)
