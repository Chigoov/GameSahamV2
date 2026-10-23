"""Run existing browser suites with isolated host, save and output per suite."""
import argparse
import json
import os
import re
from pathlib import Path
import shutil
import socket
import subprocess
import time
import urllib.request

ROOT = Path(__file__).resolve().parent.parent
parser = argparse.ArgumentParser()
parser.add_argument('--assets', required=True)
parser.add_argument('--output', required=True)
parser.add_argument('--classpath', required=True)
parser.add_argument('--only', nargs='*')
parser.add_argument('--language', choices=['js', 'python'], default='js')
parser.add_argument('--deep', action='store_true', help='Include optional existing wired-feature journeys and matrices')
args = parser.parse_args()
out = Path(args.output).resolve()
assert out.is_relative_to(ROOT), 'Output must stay in production root'
out.mkdir(parents=True, exist_ok=True)
assets = Path(args.assets).resolve()
assert assets.is_relative_to(ROOT)
classpath = Path(args.classpath).read_text(encoding='utf-8-sig').strip()
jbin = Path('C:/Program Files/Android/Android Studio/jbr/bin/java.exe')
env = os.environ.copy()
env['NODE_PATH'] = str(Path.home()/'.hermes/hermes-agent/node_modules')
env['JAVA_TOOL_OPTIONS'] = '-Djdk.net.unixdomain.tmpdir='+str(ROOT/'.socket-qa')
env['JAVA_HOME'] = str(jbin.parent.parent)
env['PYTHONIOENCODING'] = 'utf-8'
env['QA_ASSETS'] = str(assets)
names = sorted(p.name for p in (ROOT/'Work').glob('*.'+('js' if args.language == 'js' else 'py')) if 'playwright' in p.read_text(encoding='utf-8-sig') and p.name not in ['verify-redesign-screens.py', Path(__file__).name])
if args.only:
    assert set(args.only) <= set(names), 'Unknown existing browser suite'
    names = args.only
results = []
for name in names:
    suite = out/Path(name).stem
    work = suite/'Work'
    work.mkdir(parents=True, exist_ok=True)
    with socket.socket() as sock:
        sock.bind(('127.0.0.1', 0))
        port = sock.getsockname()[1]
    code = (ROOT/'Work'/name).read_text(encoding='utf-8-sig')
    # ponytail: routing-only sandbox copies preserve every existing assertion.
    routed = re.sub(r'http://127\.0\.0\.1:(8767|8784|8794)', f'http://127.0.0.1:{port}', code)
    if args.language == 'python':
        routed = routed.replace('subprocess.check_output("netstat -ano | grep 8784 || true", shell=True)', 'b"LISTENING isolated-harness"')
        if name == 'test-a1-a3-collections-browser.py':
            start, end = routed.index('# Recompile WireQaServer'), routed.index('\ntry:', routed.index('# Recompile WireQaServer'))
            routed = routed[:start] + '# Host lifecycle is owned by isolated parent harness.\nserver_proc = None\n' + routed[end:]
            routed = routed.replace('server_proc.terminate()', 'None').replace('server_proc.wait(timeout=3)', 'None').replace('server_proc.kill()', 'None')
        if name == 'test-sustainability-and-storage.py':
            android = suite/'android'
            android.mkdir(exist_ok=True)
            for entry in ['build.gradle', 'settings.gradle', 'gradle.properties']:
                shutil.copy2(ROOT/'android'/entry, android/entry)
            shutil.copytree(ROOT/'android/gradle', android/'gradle', dirs_exist_ok=True)
            (android/'app').mkdir(exist_ok=True)
            shutil.copy2(ROOT/'android/app/build.gradle', android/'app/build.gradle')
            shutil.copytree(ROOT/'android/app/src', android/'app/src', dirs_exist_ok=True)
            (android/'local.properties').write_text('sdk.dir='+str(Path(os.environ['LOCALAPPDATA'])/'Android/Sdk').replace('\\','/')+'\n', encoding='utf-8')
    (work/name).write_text(routed, encoding='utf-8')
    env['QA_PORT'] = str(port)
    env['UI_VERIFY_OUT_DIR'] = str(suite/'evidence')
    command = [shutil.which('node') if args.language == 'js' else shutil.which('python'), str(work/name)]
    if name == 'verify-portrait-ui.js':
        command += ['--url', f'http://127.0.0.1:{port}', '--output-dir', str(suite/'evidence')]
    elif name == 'verify-portrait-ui.py':
        command += ['--url', f'http://127.0.0.1:{port}', '--output', str(suite/'evidence')]
    elif args.language == 'python' and '--output-dir' in code:
        command += ['--output-dir', str(suite/'evidence')]
    if args.deep and name == 'verify-wired-features-2026-10-21.py':
        command += ['--journeys','--matrix','--flow-matrix']
    if code != routed:
        import difflib
        (suite/'infrastructure-routing.diff').write_text(''.join(difflib.unified_diff(code.splitlines(True), routed.splitlines(True), fromfile=name, tofile='sandbox/'+name)), encoding='utf-8')
    with (suite/'host.log').open('w', encoding='utf-8') as host_log:
        host = subprocess.Popen([str(jbin), '-Xmx256m', '-XX:ReservedCodeCacheSize=64m', '-Djdk.net.unixdomain.tmpdir='+str(ROOT/'.socket-qa'), '-cp', classpath, 'com.tradingglobal3d.mobile.WireQaServer', str(assets), str(suite/'qa-save'), str(port)], cwd=ROOT, stdout=host_log, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW)
        try:
            for attempt in range(100):
                try:
                    with urllib.request.urlopen(f'http://127.0.0.1:{port}/qa-meta', timeout=2) as response:
                        meta = json.load(response)
                    assert meta['platform']['testFixture'] and not meta['platform']['nativeHost']
                    break
                except OSError:
                    if host.poll() is not None:
                        raise RuntimeError('Isolated host exited; inspect host.log')
                    time.sleep(.1)
            else:
                raise RuntimeError('Isolated host did not become ready')
            with (suite/'test.log').open('w', encoding='utf-8') as log:
                try:
                    result = subprocess.run(command, cwd=work, env=env, stdout=log, stderr=subprocess.STDOUT, timeout=180)
                    status = 'PASS' if result.returncode == 0 else 'FAIL'
                    exit_code = result.returncode
                except subprocess.TimeoutExpired:
                    status, exit_code = 'TIMEOUT', None
            results.append({'suite': name, 'status': status, 'exitCode': exit_code, 'command': command, 'routingOnlyChanges': code != routed, 'log': str(suite/'test.log'), 'save': str(suite/'qa-save')})
        except Exception as error:
            results.append({'suite': name, 'status': 'BLOCKED', 'error': str(error)})
        finally:
            host.terminate()
            try:
                host.wait(timeout=5)
            except subprocess.TimeoutExpired:
                host.kill(); host.wait()
    (out/'results.json').write_text(json.dumps(results, indent=2), encoding='utf-8')
    print(name, results[-1]['status'], flush=True)
print(json.dumps({'suites': len(results), 'pass': sum(r['status'] == 'PASS' for r in results)}))
raise SystemExit(0 if all(r['status'] == 'PASS' for r in results) else 1)
