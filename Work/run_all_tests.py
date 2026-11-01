import subprocess
import glob
import os

repo = "C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM"
os.chdir(repo)

output_classes = "Artifacts/Test-Plan14-Full/classes"
json_jar = "C:/Users/HYPE AMD/.gradle/caches/modules-2/files-2.1/org.json/json/20240303/ebb88e8fb5122b7506d5cf1d69f1ccdb790d22a/json-20240303.jar"
junit_jar = "C:/Users/HYPE AMD/.gradle/caches/modules-2/files-2.1/junit/junit/4.13.2/8ac9e16d933b6fb43bc7f576336b8f4d7eb5ba12/junit-4.13.2.jar"
hamcrest_jar = "C:/Users/HYPE AMD/.gradle/caches/modules-2/files-2.1/org.hamcrest/hamcrest-core/1.3/42a25dc3219429f0e5d060061f71acb49bf010a0/hamcrest-core-1.3.jar"
android_jar = "C:/Users/HYPE AMD/AppData/Local/Android/Sdk/platforms/android-36/android.jar"
cp = f"{output_classes};{json_jar};{junit_jar};{hamcrest_jar};{android_jar}"

java_exe = "C:/Program Files/Android/Android Studio/jbr/bin/java.exe"

# Gather all *Test.java that exist in output_classes
tests = []
test_files = glob.glob("android/app/src/test/java/**/*Test.java", recursive=True)
for tf in test_files:
    norm = tf.replace(os.sep, "/")
    idx = norm.find("com/")
    if idx >= 0:
        cls_name = norm[idx:].replace("/", ".").replace(".java", "")
        cls_file = os.path.join(output_classes, norm[idx:].replace("/", os.sep).replace(".java", ".class"))
        if os.path.exists(cls_file):
            tests.append(cls_name)

print(f"Running {len(tests)} test classes...")

cmd = [
    java_exe, "-Xmx512m",
    "-Djava.io.tmpdir=Artifacts/Test-Plan14-Full/tmp",
    "-cp", cp,
    "org.junit.runner.JUnitCore"
] + tests

p = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
print(p.stdout[-500:] if len(p.stdout) > 500 else p.stdout)
print("Exit code:", p.returncode)
