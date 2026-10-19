"""Verify local reference APK acquisition and REA evidence, without ADB or network."""
from pathlib import Path
import csv
import hashlib
import json
import zipfile

root = Path(__file__).resolve().parents[1]
rea = root / "Artifacts/REA"


def read(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def digest(path):
    with path.open("rb") as stream:
        return hashlib.file_digest(stream, "sha256").hexdigest()


acquisition = read(rea / "acquisition-2026-10-20/acquisition.json")
artifacts = acquisition["artifacts"]
assert len(artifacts) == 5
hashes = set()
for artifact in artifacts:
    path = Path(artifact["local_path"])
    assert path.is_relative_to(rea.resolve()), path
    assert path.stat().st_size == artifact["bytes"]
    assert digest(path) == artifact["sha256"] == artifact["device_sha256"]
    with zipfile.ZipFile(path) as archive:
        assert archive.testzip() is None
    hashes.add(artifact["sha256"])

names = ["flutter-build", "dart-aot", "base-artifact", "android-package",
         "android-app-classes", "android-main-activity", "android-engine-method"]
for name in names:
    envelope = read(rea / f"evidence/{name}.json")
    assert envelope["ok"] is True, name
    evidence = envelope["data"]
    assert evidence["evidence_id"].startswith("ev_"), name
    assert evidence["subject"]["digest"]["sha256"] in hashes, name
    assert evidence["authority"] == "shipped-artifact", name

package = read(rea / "evidence/android-package.json")["data"]["normalized_result"]
assert (package["package_name"], package["version_name"], package["version_code"]) == (
    "com.lydia.tycoongame", "1.4.6", "33")
assert digest(rea / "tools/jadx-headless-mcp-0.7.1-all.jar") == (
    "6e5eacf500b64292bfb73c49797c1958f6ee44646e43e868039ae7feb573ff75")

signatures = read(rea / "evidence/signature-summary.json")
assert signatures["all_five_verified"] and signatures["same_signer"]
assert len(signatures["artifacts"]) == 5
assert len({item["signer_sha256"] for item in signatures["artifacts"]}) == 1
uris = read(rea / "analysis/app-package-uris.json")
strings = read(rea / "analysis/feature-string-candidates.json")["matches"]
with (root / "Work/REA-FEATURE-MAP.csv").open(encoding="utf-8", newline="") as stream:
    features = list(csv.DictReader(stream))
assert len(features) == 26
for feature in features:
    anchor = feature["old_static_anchor"]
    if anchor.startswith("string:"):
        assert any(row["text"] == anchor[7:] for row in strings), anchor
    else:
        assert any(uri.endswith("/" + anchor) for uri in uris), anchor
    assert feature["old_logic_status"] == "NOT_RECOVERED", feature
    source = feature["new_source"]
    if source != "NONE":
        assert (root / source).is_file(), source

result = {"apk_files": 5, "rea_records": len(names), "mapped_feature_groups": len(features),
          "artifact_checks": "PASS", "adb_or_network_used": False,
          "does_not_verify": ["old game formulas", "backend behavior", "new game acceptance"]}
(rea / "verification.json").write_text(json.dumps(result, indent=2), encoding="utf-8")
print(json.dumps(result))
