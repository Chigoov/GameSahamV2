const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

async function run() {
    const port = '8794';
    const evidenceDir = path.resolve(__dirname, '../Artifacts/Overall-Preview-2026-10-23');

    console.log("Launching browser...");
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext({ viewport: { width: 412, height: 860 } });
    const page = await context.newPage();

    await page.goto(`http://127.0.0.1:${port}/`);
    await page.waitForSelector('nav.bottom-nav', { timeout: 10000 });

    console.log("Navigating to Pasar -> BBCA...");
    await page.click('button.nav-item[data-tab="pasar"]');
    await page.waitForTimeout(400);
    await page.fill('#search-stock-input', 'BBCA');
    await page.waitForTimeout(300);
    await page.locator('#stock-list-container .stock-list-item:has-text("BBCA")').first().click();
    await page.waitForSelector('.flow-page[aria-label="BBCA"]', { timeout: 5000 });

    // Cek isi chart wrapper di DOM
    const wrapperInfo = await page.evaluate(() => {
        const wrap = document.getElementById('instrument-chart-section');
        const svg = wrap ? wrap.querySelector('.candle-svg') : null;
        const rects = svg ? svg.querySelectorAll('rect.candle-body').length : 0;
        const lines = svg ? svg.querySelectorAll('line.candle-wick').length : 0;
        return {
            hasWrap: !!wrap,
            wrapHTML: wrap ? wrap.innerHTML.substring(0, 300) : null,
            hasSvg: !!svg,
            bodyCount: rects,
            wickCount: lines,
            svgInnerLength: svg ? svg.innerHTML.length : 0
        };
    });
    console.log("Chart DOM Info:", JSON.stringify(wrapperInfo, null, 2));

    // Ambil screenshot langsung pada elemen #instrument-chart-section
    const chartElem = page.locator('#instrument-chart-section');
    const shotPath = path.join(evidenceDir, 'element-chart-bbca.png');
    await chartElem.screenshot({ path: shotPath });
    console.log("Captured #instrument-chart-section element screenshot:", shotPath);

    // Ambil juga screenshot #instrument-chart-section untuk AAPL
    await page.click('.back-button');
    await page.waitForTimeout(300);
    await page.fill('#search-stock-input', 'AAPL');
    await page.waitForTimeout(300);
    await page.locator('#stock-list-container .stock-list-item:has-text("AAPL")').first().click();
    await page.waitForSelector('.flow-page[aria-label="AAPL"]', { timeout: 5000 });

    const chartElemAapl = page.locator('#instrument-chart-section');
    const shotAapl = path.join(evidenceDir, 'element-chart-aapl.png');
    await chartElemAapl.screenshot({ path: shotAapl });
    console.log("Captured AAPL #instrument-chart-section screenshot:", shotAapl);

    await browser.close();
}

run().catch(err => {
    console.error("Test error:", err);
    process.exit(1);
});
