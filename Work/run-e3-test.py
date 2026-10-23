from pathlib import Path
import os, subprocess

root = Path("C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM").resolve()
run_dir = root / "Artifacts/Owner-Revision-14-2026-10-22/run-01"
classes = (run_dir / "compile/classes").resolve()
test_classes = (root / "android/app/build/intermediates/javac/debugUnitTest/compileDebugUnitTestJavaWithJavac/classes").resolve()
json_jar = next((Path.home() / ".gradle/caches/modules-2/files-2.1/org.json/json/20240303").rglob("*.jar")).resolve()
hamcrest_jar = next((Path.home() / ".gradle/caches/modules-2/files-2.1/org.hamcrest/hamcrest-core/1.3").rglob("*.jar")).resolve()
junit_jar = next((Path.home() / ".gradle/caches/modules-2/files-2.1/junit/junit/4.13.2").rglob("*.jar")).resolve()
android_jar = (Path(os.environ["LOCALAPPDATA"]) / "Android/Sdk/platforms/android-36/android.jar").resolve()
cp = os.pathsep.join(map(str, [classes, test_classes, junit_jar, hamcrest_jar, json_jar, android_jar]))
jbin = Path("C:/Program Files/Android/Android Studio/jbr/bin")

# Recompile modified classes
subprocess.run([
    str(jbin / "javac.exe"), "-encoding", "UTF-8", "-cp", cp, "-d", str(classes),
    str(root / "android/app/src/main/java/com/tradingglobal3d/core/player/PlayerProfileManager.java"),
    str(root / "android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java"),
    str(root / "android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java"),
    str(root / "android/app/src/test/java/com/tradingglobal3d/core/AchievementsAndCosmeticsTahapE3Test.java")
], check=True)

res = subprocess.run([
    str(jbin / "java.exe"), "-cp", cp, "org.junit.runner.JUnitCore",
    "com.tradingglobal3d.core.AchievementsAndCosmeticsTahapE3Test"
], cwd=str(root / "android/app"))
print("Tahap E3 Test Exit code:", res.returncode)
