"""Check packaged web assets and reject the local QA server's actual DEX classes."""
import fnmatch
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import time
import zipfile

ROOT = Path(__file__).resolve().parents[2]
TASK = Path(__file__).resolve().parent


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('apk', nargs='?', type=Path, default=ROOT / 'android/app/build/outputs/apk/debug/app-debug.apk')
    apk = parser.parse_args().apk.resolve()
    if not apk.is_relative_to(ROOT):
        parser.error('APK must be inside the task workspace')
    tool = Path(os.environ['LOCALAPPDATA']) / 'Android/Sdk/build-tools/36.1.0/dexdump.exe'
    out = TASK / 'logs' / ('apk-check-' + str(time.time_ns()))
    out.mkdir(parents=True)
    digest = lambda data: hashlib.sha256(data).hexdigest()
    test_classes = set()
    for source in (ROOT / 'android/app/src/test/java').rglob('*.java'):
        package = re.search(r'package\s+([\w.]+)\s*;', source.read_text(encoding='utf-8-sig'))
        if package:
            test_classes.add('L' + package[1].replace('.', '/') + '/' + source.stem + ';')
    assets, excluded, dex, issues = [], [], [], []
    with zipfile.ZipFile(apk) as archive:
        for source in sorted((ROOT / 'android/app/src/main/assets/www').rglob('*')):
            if not source.is_file():
                continue
            member = 'assets/www/' + source.relative_to(ROOT / 'android/app/src/main/assets/www').as_posix()
            if any(fnmatch.fnmatch(source.name, p) for p in ['dashboard-*.html', 'preview-before-after.html']):
                excluded.append(member)
                continue
            packaged = digest(archive.read(member)) if member in archive.namelist() else None
            assets.append({'path': member, 'sourceSha256': digest(source.read_bytes()), 'apkSha256': packaged})
            if assets[-1]['sourceSha256'] != packaged:
                issues.append('Asset mismatch: ' + member)
        for member in archive.namelist():
            if not re.fullmatch(r'classes\d*\.dex', member):
                continue
            extracted = out / member
            extracted.write_bytes(archive.read(member))
            command = [str(tool), str(extracted)]
            result = subprocess.run(command, capture_output=True, text=True, encoding='utf8', errors='replace')
            definitions = re.findall(r"Class descriptor\s*:\s*'([^']+)'", result.stdout)
            qa = [name for name in definitions if '/WireQaServer' in name]
            tests = [name for name in definitions if name.split('$')[0].rstrip(';') + ';' in test_classes]
            dex.append({'path': member, 'sha256': digest(extracted.read_bytes()), 'command': command,
                        'exitCode': result.returncode, 'classDefinitions': len(definitions), 'qaClasses': qa, 'testClasses': tests})
            if result.returncode or not definitions:
                issues.append('DEX inspection failed: ' + member)
            if qa:
                issues.append('QA server packaged: ' + ', '.join(qa))
            if tests:
                issues.append('Test classes packaged: ' + ', '.join(tests))
    if not dex:
        issues.append('No DEX found')
    result = {'apk': str(apk), 'apkSha256': digest(apk.read_bytes()), 'bytes': apk.stat().st_size,
              'verifierSha256': digest(Path(__file__).read_bytes()),
              'assets': assets, 'intentionalExclusions': excluded, 'dex': dex, 'issues': issues,
              'scope': 'Asset byte equality and actual DEX class definitions; not Android runtime or all possible QA code'}
    report = out / 'apk.json'
    report.write_text(json.dumps(result, indent=2), encoding='utf8')
    print('Evidence:', report)
    print('Assets:', len(assets), '| DEX:', len(dex), '|', 'FAIL' if issues else 'PASS')
    for issue in issues:
        print(issue)
    return 1 if issues else 0


if __name__ == '__main__':
    raise SystemExit(main())
