"""Ten actual QA Activity replacements after endurance; never launches the production package."""
import json
from pathlib import Path
import subprocess
import time
from playwright.sync_api import sync_playwright

task=Path(__file__).resolve().parent
base=['C:/Users/HYPE AMD/AppData/Local/Android/Sdk/platform-tools/adb.exe','-s','AYW4LJZHOZI7TOZP']
package='com.tradingglobal3d.mobile.qa'
component=package+'/com.tradingglobal3d.mobile.MainActivity'
out=task/'logs/device-session-2026-11-03'/('relaunch-'+str(time.time_ns()))
out.mkdir()
records=[]

def adb(*args):
    return subprocess.check_output(base+list(args),text=True,encoding='utf8',errors='replace',timeout=30)

def snapshot(pw):
    pid=adb('shell','pidof',package).strip()
    assert pid.isdigit()
    port=adb('forward','tcp:0','localabstract:webview_devtools_remote_'+pid).strip()
    try:
        browser=pw.chromium.connect_over_cdp('http://127.0.0.1:'+port)
        pages=[p for c in browser.contexts for p in c.pages if p.url=='file:///android_asset/www/index.html']
        assert len(pages)==1
        page=pages[0]
        page.wait_for_function('''() => typeof TradingNativeBridge!=='undefined' &&
          document.getElementById('clock-display') && JSON.parse(TradingNativeBridge.getClockSyncStatus()).status==='COMPLETED' ''',timeout=30000)
        result=page.evaluate('''() => {const s=JSON.parse(TradingNativeBridge.getSnapshot());return {snapshot:s,
          portfolio:JSON.parse(TradingNativeBridge.getPortfolio(s.personalAccount.id)),save:JSON.parse(TradingNativeBridge.getSaveMetadata()),
          warnings:[...document.querySelectorAll('#snapshot-status,#clock-error,#native-save-warning,#native-save-warning')].map(e=>e.textContent)};}''')
        result['pid']=int(pid)
        return result
    finally:
        adb('forward','--remove','tcp:'+port)

with sync_playwright() as pw:
    before=snapshot(pw)
    assert not before['snapshot'].get('recoveryRequired'), 'QA already needs recovery'
    for i in range(10):
        foreground=adb('shell','dumpsys','activity','activities')
        assert any(component in line for line in foreground.splitlines() if 'topResumedActivity=' in line), 'Stop: another app is foreground'
        # Real onPause/onDestroy/onCreate transitions; no fake lifecycle call or clock manipulation.
        adb('shell','input','keyevent','KEYCODE_HOME')
        launch=adb('shell','am','start','-W','--activity-new-task','--activity-clear-task','-n',component)
        after=snapshot(pw)
        a,b=before['snapshot'],after['snapshot']
        assert a['runId']==b['runId'], 'Run changed on Activity replacement'
        assert b['clock']['timestamp']>=a['clock']['timestamp'], 'Game time rolled back'
        assert a['personalAccount']['cash']==b['personalAccount']['cash'], 'QA cash changed unexpectedly on Activity replacement'
        quantities=lambda p: sorted((x['instrumentId'],str(x['quantity'])) for x in p.get('positions',[]))
        assert quantities(before['portfolio'])==quantities(after['portfolio']), 'QA holdings changed on Activity replacement'
        assert not after['warnings'], 'QA save/clock warning: '+str(after['warnings'])
        records.append({'cycle':i+1,'launch':launch,'before':before,'after':after})
        (out/'results.json').write_text(json.dumps({'scope':'Actual ten QA Activity replacements; host deterministic race test separately proves obsolete-writer rejection','cycles':records},indent=2),encoding='utf8')
        print('Relaunch',i+1,'PASS',flush=True)
        before=after
print('Evidence:',out)
