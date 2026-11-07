"""Audit a finished device run; no live device commands and no inferred full-plan PASS."""
import hashlib
import json
import math
from pathlib import Path
import sys

task = Path(__file__).resolve().parent
root = task.parents[1]
run = Path(sys.argv[1]).resolve()
assert run.is_relative_to(task/'logs/device-session-2026-11-03')
state = json.loads((run/'session.json').read_text())
issues = []
if state.get('status') != 'PASS' or state.get('elapsedSeconds', 0) < 3600:
    issues.append('No completed 60-minute run')
if state.get('pageErrors'):
    issues.append('Browser page errors')
if {event['kind'] for event in state.get('lifecycle', [])} != {'background30s','savedForceStop8s'}:
    issues.append('Required lifecycle observations missing')
if {sample['label'] for sample in state.get('samples', [])} != {'start','middle','end'}:
    issues.append('Missing beginning/middle/end samples')
if state.get('initialRunId') != state.get('finalRunId'):
    issues.append('Run identity changed or final run unavailable')
identity = json.loads((run/'snapshot-identity.json').read_text())
drift = [name for name, sha in identity['sourceManifest'].items()
         if hashlib.sha256((root/name).read_bytes()).hexdigest() != sha]
if drift:
    issues.append('Source or host verifier changed during device attribution')
if identity.get('sessionApkSha256') != state.get('qaApkSha256'):
    issues.append('Session APK identity mismatch')
groups = {}
for action in state.get('actions', []):
    if action.get('clickToTwoBrowserFramesMs') is not None:
        groups.setdefault(str(action['kind']), []).append(action['clickToTwoBrowserFramesMs'])
metrics = {}
for kind, samples in groups.items():
    values = sorted(samples)
    metrics[kind] = {'n':len(values),'p50ms':values[math.ceil(.5*len(values))-1],
                     'p95ms':values[math.ceil(.95*len(values))-1],'maxms':max(values)}
if state.get('finalGameTimestamp'):
    rate = (state['finalGameTimestamp']-state['initialGameTimestamp'])/(1000*state['elapsedSeconds'])
    if not 47.5 <= rate <= 48.5:
        issues.append('Observed game-time advancement inconsistent with 48x')
else:
    rate = None
report = {'scope':'Device-run identity, behavior and elapsed evidence; click-to-two-browser-frames excludes hardware input-to-photon',
          'session':str(run.relative_to(root)),'qaApkSha256':state.get('qaApkSha256'),
          'enduranceIssues':issues,'sourceDrift':drift,'actions':len(state.get('actions', [])),
          'observedGameSecondsPerRealSecond':rate,'clickMetricsByActionKind':metrics,
          'performanceReviewRequired':[kind for kind, value in metrics.items() if value['p95ms']>100],
          'fullPlanComplete':False}
(run/'audit.json').write_text(json.dumps(report,indent=2),encoding='utf8')
print(json.dumps(report,indent=2))
raise SystemExit(1 if issues else 0)
