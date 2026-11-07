"""Run one read-only SQL query with the installed Android Studio trace processor."""
from pathlib import Path
import os
import re
import subprocess
import sys

task = Path(__file__).resolve().parent
root = task.parents[1]
studio = Path('C:/Program Files/Android/Android Studio')
classes = task / 'logs/profiling-tools/classes'
classes.mkdir(parents=True, exist_ok=True)
classpath = os.pathsep.join([str(studio/'plugins/android/lib/*'), str(studio/'lib/*'), str(classes)])
source = task/'ReadFrameTrace.java'
if not (classes/'ReadFrameTrace.class').exists() or source.stat().st_mtime > (classes/'ReadFrameTrace.class').stat().st_mtime:
    subprocess.run([str(studio/'jbr/bin/javac.exe'), '-proc:none', '-cp', classpath, '-d', str(classes), str(source)], check=True)
trace, sql, output = map(lambda p: Path(p).resolve(), sys.argv[1:])
assert all(p.is_relative_to(task/'logs') for p in [trace,sql,output]), 'Task-local trace evidence only'
server = subprocess.Popen([str(studio/'plugins/android/resources/trace_processor_server/trace-processor.exe'),
                           '--timeout=120'], stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                          text=True, creationflags=getattr(subprocess,'CREATE_NO_WINDOW',0))
try:
    line = server.stdout.readline()
    match = re.search(r"Bound on port '(\d+)'", line)
    if not match:
        raise RuntimeError('Trace server did not report a port: '+line)
    command = [str(studio/'jbr/bin/java.exe'), '-Djdk.net.unixdomain.tmpdir='+str(root/'.socket-qa'),
               '-cp', classpath, 'ReadFrameTrace', match[1], str(trace), str(sql), str(output)]
    result = subprocess.run(command, capture_output=True, text=True, timeout=90)
    Path(str(output)+'.log').write_text(result.stdout+result.stderr,encoding='utf8')
    print(result.stdout+result.stderr)
    raise SystemExit(result.returncode)
finally:
    server.terminate()
    server.wait(timeout=10)
