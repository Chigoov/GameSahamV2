import subprocess, os
from pathlib import Path

root = Path("C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM").resolve()
gradlew = root / "android/gradlew.bat"

env = os.environ.copy()
env["JAVA_HOME"] = "C:/Program Files/Android/Android Studio/jbr"
env["PATH"] = "C:/Program Files/Android/Android Studio/jbr/bin;" + env.get("PATH", "")

print("Compiling Java with javac...")
res_compile = subprocess.run([str(gradlew), "compileDebugJavaWithJavac"], cwd=str(root / "android"), env=env)
if res_compile.returncode != 0:
    print("Compilation failed!")
    exit(1)

print("Building Debug APK candidate...")
res_apk = subprocess.run([str(gradlew), "assembleDebug"], cwd=str(root / "android"), env=env)
print("Assemble Debug exit code:", res_apk.returncode)
