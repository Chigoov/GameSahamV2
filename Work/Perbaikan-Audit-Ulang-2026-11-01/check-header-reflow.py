"""Check B05 against the production HTML, complete stylesheet and bundled font."""
import base64
import json
import re
from pathlib import Path
from playwright.sync_api import sync_playwright

root = Path(__file__).resolve().parents[2]
web = root / "android/app/src/main/assets/www"
out = root / "Artifacts/B05-Final-Reflow-2026-11-02"
out.mkdir(parents=True, exist_ok=True)
html = re.sub(r'<script\b[^>]*>[\s\S]*?</script>|<link\b[^>]*>', '',
              (web / "index.html").read_text(encoding="utf-8"), flags=re.I)
font = base64.b64encode((web / "assets/IBMPlexSans.ttf").read_bytes()).decode()
css = (web / "style.css").read_text(encoding="utf-8").replace(
    "url('assets/IBMPlexSans.ttf')", "url('data:font/ttf;base64," + font + "')")
results = []
with sync_playwright() as p:
    browser = p.chromium.launch(channel="chrome", headless=True)
    for width in (320, 360, 390, 412, 430, 768, 1440):
        for scale in (100, 200):
            page = browser.new_page(viewport={"width": width, "height": 900})
            page.route('**/*', lambda route: route.abort())
            page.set_content(html)
            page.add_style_tag(content=css)
            page.add_style_tag(content=f'html {{font-size:{scale}% !important}}')
            page.evaluate('document.fonts.ready')
            for amount in ('Rp 50.000.000', 'Rp 50.000.000.000', 'Rp 9.999.999.999.999'):
                result = page.evaluate('''amount => {
                    const e=document.querySelector('#header-net-worth');e.textContent=amount;
                    const range=document.createRange();range.selectNodeContents(e);
                    const box=e.getBoundingClientRect(), chip=e.closest('button').getBoundingClientRect();
                    const rects=[...range.getClientRects()].map(r=>({left:r.left,right:r.right,top:r.top,bottom:r.bottom}));
                    return {amount, wordBreak:getComputedStyle(e).wordBreak,
                        noEllipsis:getComputedStyle(e).textOverflow!=='ellipsis',
                        inside:rects.every(r=>r.left>=box.left-1&&r.right<=box.right+1&&
                            r.left>=0&&r.right<=innerWidth&&r.top>=chip.top-1&&r.bottom<=chip.bottom+1),rects};
                }''', amount)
                results.append({"width": width, "scale": scale, **result})
                assert result['noEllipsis'] and result['inside'], results[-1]
            if width in (320, 360) and scale == 200:
                page.screenshot(path=str(out / f'header-{width}-text200.png'))
            page.close()
    browser.close()
(out / 'checks.json').write_text(json.dumps(results, indent=2), encoding='utf-8')
print(f'PASS: {len(results)} production header cases; glyphs inside text box and viewport; no ellipsis. Browser presentation check, not Android runtime.')
