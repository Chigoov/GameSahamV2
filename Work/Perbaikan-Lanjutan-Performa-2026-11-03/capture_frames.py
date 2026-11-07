"""Capture and inspect one 20s QA SurfaceFlinger frame window; no device settings changed."""
import csv
import hashlib
import json
import math
from pathlib import Path
import subprocess
import sys
import time

task = Path(__file__).resolve().parent
label = sys.argv[1]
assert label.replace('-', '').isalnum(), 'Simple unique label required'
out = task/'logs/device-session-2026-11-03'/label
out.mkdir()
base = ['C:/Users/HYPE AMD/AppData/Local/Android/Sdk/platform-tools/adb.exe', '-s', 'AYW4LJZHOZI7TOZP']
activity = subprocess.check_output(base+['shell','dumpsys','activity','activities'],text=True)
assert any('com.tradingglobal3d.mobile.qa/com.tradingglobal3d.mobile.MainActivity' in x for x in activity.splitlines() if 'topResumedActivity=' in x)
pid = subprocess.check_output(base+['shell','pidof','com.tradingglobal3d.mobile.qa'],text=True).strip()
assert pid.isdigit()
config = out/'config.pbtxt'
config.write_text('buffers { size_kb: 16384 fill_policy: RING_BUFFER }\nduration_ms: 20000\ndata_sources { config { name: "android.surfaceflinger.frametimeline" } }\n',encoding='utf8')
remote_config='/data/misc/perfetto-configs/gamesaham-qa-'+label+'.pbtxt'
remote_trace='/data/misc/perfetto-traces/gamesaham-qa-'+label+'.trace'
commands=[]
started=time.time()
for args in [['push',str(config),remote_config],['shell','perfetto','--txt','-c',remote_config,'-o',remote_trace],['pull',remote_trace,str(out/'trace.perfetto-trace')]]:
    result=subprocess.run(base+args,capture_output=True,text=True,timeout=45)
    commands.append({'command':base+args,'exitCode':result.returncode,'stdout':result.stdout,'stderr':result.stderr})
    (out/'commands.json').write_text(json.dumps(commands,indent=2),encoding='utf8')
    assert result.returncode==0,result.stderr
sql=out/'frames.sql'
sql.write_text("SELECT a.ts,a.dur,a.surface_frame_token,a.layer_name,a.present_type,a.on_time_finish,a.jank_type,p.pid,e.ts AS expected_ts,e.dur AS expected_dur,(a.ts+a.dur)-(e.ts+e.dur) AS late_ns FROM actual_frame_timeline_slice a JOIN process p USING(upid) LEFT JOIN expected_frame_timeline_slice e ON a.upid=e.upid AND a.surface_frame_token=e.surface_frame_token WHERE p.pid="+pid+" ORDER BY a.ts;",encoding='utf8')
subprocess.run([sys.executable,str(task/'read_trace.py'),str(out/'trace.perfetto-trace'),str(sql),str(out/'frames')],check=True,timeout=100)
rows=list(csv.DictReader((out/'frames.tsv').open(),delimiter='\t'))
assert rows and all(r['pid']==pid and 'com.tradingglobal3d.mobile.qa' in r['layer_name'] for r in rows)
durations=sorted(int(r['dur'])/1e6 for r in rows if int(r['dur'])>=0)
from collections import Counter
summary={'scope':'Actual QA app SurfaceFlinger frame durations and presentation; not FPS inferred from frame count in idle periods',
         'startedUnix':started,'endedUnix':time.time(),'pid':int(pid),'traceSha256':hashlib.sha256((out/'trace.perfetto-trace').read_bytes()).hexdigest(),
         'frameCount':len(rows),'p50ms':durations[math.ceil(.5*len(durations))-1],'p95ms':durations[math.ceil(.95*len(durations))-1],
         'p99ms':durations[math.ceil(.99*len(durations))-1],'maxms':max(durations),'above700ms':sum(d>700 for d in durations),
         'presentation':dict(Counter(r['present_type'] for r in rows)),'jank':dict(Counter(r['jank_type'] for r in rows))}
(out/'summary.json').write_text(json.dumps(summary,indent=2),encoding='utf8')
print(json.dumps(summary,indent=2))
