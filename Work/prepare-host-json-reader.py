"""Copy the installed Android JsonReader implementation into isolated JVM QA classes."""
import argparse
import hashlib
import json
import zipfile
from pathlib import Path

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("classes", type=Path)
args = parser.parse_args()
classes = args.classes.resolve()
repo = Path(__file__).resolve().parent.parent
if not classes.is_relative_to(repo):
    parser.error("QA classes must be inside this checkout")
layout = Path("C:/Program Files/Android/Android Studio/plugins/design-tools/lib/layoutlib.jar")
names = [
    "android/util/JsonReader.class",
    "android/util/JsonReader$1.class",
    "android/util/JsonScope.class",
    "android/util/JsonToken.class",
    "android/util/JsonWriter.class",
    "android/util/JsonWriter$1.class",
    "android/util/MalformedJsonException.class",
    "com/android/internal/util/StringPool.class",
]
hashes = {}
with zipfile.ZipFile(layout) as archive:
    for name in names:
        data = archive.read(name)
        dest = classes / name
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_bytes(data)
        hashes[name] = hashlib.sha256(data).hexdigest()
(classes.parent / "host-json-reader.json").write_text(json.dumps({
    "source": str(layout), "classes": hashes,
    "helperSha256": hashlib.sha256(Path(__file__).read_bytes()).hexdigest(),
}, indent=2))
