"""Three repeated native-QA navigation measurements, 30 samples per screen."""
import json
from pathlib import Path
import subprocess
import sys
import time
from playwright.sync_api import sync_playwright

task=Path(__file__).resolve().parent
label=sys.argv[1]
assert label.replace('-','').isalnum()
out=task/'logs/device-session-2026-11-03'/label
out.mkdir()
base=['C:/Users/HYPE AMD/AppData/Local/Android/Sdk/platform-tools/adb.exe','-s','AYW4LJZHOZI7TOZP']
component='com.tradingglobal3d.mobile.qa/com.tradingglobal3d.mobile.MainActivity'
def adb(*args):return subprocess.check_output(base+list(args),text=True,encoding='utf8',errors='replace',timeout=30)
records=[]
errors=[]
status='RUNNING'
failure=None
cleanup=None
port=None
try:
    pid=adb('shell','pidof','com.tradingglobal3d.mobile.qa').strip()
    assert pid.isdigit()
    port=adb('forward','tcp:0','localabstract:webview_devtools_remote_'+pid).strip()
    with sync_playwright() as pw:
        b=pw.chromium.connect_over_cdp('http://127.0.0.1:'+port)
        page=b.contexts[0].pages[0]
        cdp=page.context.new_cdp_session(page)
        page.on('pageerror',lambda error:errors.append(str(error)))
        for repeat in range(3):
            for tab in (sys.argv[2:] or ['beranda','pasar_porto','firma','riset']):
                samples=[]
                for i in range(-3,30):
                    active=adb('shell','dumpsys','activity','activities')
                    assert any(component in line for line in active.splitlines() if 'topResumedActivity=' in line),'Another app foreground; stop input'
                    selector='.nav-item[data-tab="'+tab+'"]'
                    box=page.locator(selector).bounding_box()
                    assert box
                    page.evaluate('''() => {window.__qaMeasure=new Promise(resolve=>document.addEventListener('click',event=>{
                      const start=performance.now(),target=event.composedPath().find(e=>e.dataset?.tab)?.dataset.tab;
                      requestAnimationFrame(()=>requestAnimationFrame(()=>resolve({ms:performance.now()-start,target})));
                    },{once:true,capture:true}));}''')
                    cdp.send('Input.dispatchTouchEvent',{'type':'touchStart','touchPoints':[{'x':box['x']+box['width']/2,'y':box['y']+box['height']/2}]})
                    cdp.send('Input.dispatchTouchEvent',{'type':'touchEnd','touchPoints':[]})
                    result=page.evaluate('window.__qaMeasure')
                    active=page.locator('.screen-view.active').get_attribute('id')
                    if result.get('target')!=tab or active!='view-'+tab:
                        (out/'unexpected-input.json').write_text(json.dumps({'targetRequested':tab,'event':result,'active':active,'box':box},indent=2),encoding='utf8')
                        raise AssertionError('Requested '+tab+', click target '+str(result.get('target'))+', active '+str(active))
                    if i>=0:samples.append({'ms':result['ms'],'unix':time.time()})
                    page.wait_for_timeout(350)
                records.append({'repeat':repeat+1,'screen':tab,'samples':samples})
                (out/'measurements.json').write_text(json.dumps({'scope':'Actual Android WebView synthetic touch; click-to-two-browser-frames measured inside page, not physical input-to-photon','pid':int(pid),'runs':records,'pageErrors':errors},indent=2),encoding='utf8')
                print(repeat+1,tab,'30 samples',flush=True)
        cdp.detach()
        assert not errors,errors
        status='PASS'
except Exception as error:
    status='INCOMPLETE'
    failure=str(error)
    raise
finally:
    if port:
        cleanup=subprocess.run(base+['forward','--remove','tcp:'+port],capture_output=True,text=True)
    (out/'completion.json').write_text(json.dumps({'status':status,'failure':failure,'completedGroups':len(records),
        'expectedGroups':3*len(sys.argv[2:] or ['beranda','pasar_porto','firma','riset']),
        'pageErrors':errors,'cleanup':{'exitCode':cleanup.returncode,'stdout':cleanup.stdout,'stderr':cleanup.stderr} if cleanup else None,
        'scope':'Incomplete/disconnected measurements are never a completed comparison; cleanup cannot hide the primary failure'},indent=2),encoding='utf8')
print('Evidence:',out)
