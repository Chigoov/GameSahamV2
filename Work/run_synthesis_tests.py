"""Run synthesis tests against freshly compiled production sources and isolated QA saves."""
from pathlib import Path
import shutil
import subprocess
import uuid

repo = Path(__file__).resolve().parent.parent
shell = shutil.which("pwsh") or shutil.which("powershell")
if not shell:
    raise SystemExit("PowerShell is required by the existing JVM test runner")
tests = sorted("com.tradingglobal3d.core.synthesis." + source.stem
               for source in (repo / "android/app/src/test/java/com/tradingglobal3d/core/synthesis").glob("*Test.java"))
if not tests:
    raise SystemExit("No synthesis tests found")

def ps_literal(value):
    return "'" + str(value).replace("'", "''") + "'"

output = "Artifacts/Synthesis-Simulation-Tests/run-" + uuid.uuid4().hex
command = "& " + ps_literal(repo / "Work/run-codex-jvm-check.ps1")
command += " -Tests @(" + ",".join(map(ps_literal, tests)) + ")"
command += " -OutputDirectory " + ps_literal(output)
print(f"Compiling current production and test sources; running {len(tests)} synthesis suites", flush=True)
subprocess.run([shell, "-NoProfile", "-Command", command], cwd=repo, check=True)
