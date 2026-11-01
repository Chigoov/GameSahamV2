const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

async function run() {
    const port = process.env.QA_PORT || '8794';
    const evidenceDir = path.resolve(__dirname, '../Artifacts/Overall-Preview-2026-10-23');
    if (!fs.existsSync(evidenceDir)) fs.mkdirSync(evidenceDir, { recursive: true });

    console.log(`Connecting to http://127.0.0.1:${port}/...`);
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext({ viewport: { width: 412, height: 860 } });
    const page = await context.newPage();

    await page.goto(`http://127.0.0.1:${port}/`);
    await page.waitForSelector('nav.bottom-nav', { timeout: 10000 });

    // 1. Periksa respons getChartData untuk BBCA dan AAPL dari domain Java
    const chartBBCA = await page.evaluate(() => {
        return window.TradingNativeBridge && window.TradingNativeBridge.getChartData 
            ? JSON.parse(window.TradingNativeBridge.getChartData('IDX:BBCA', 'D1'))
            : null;
    });
    console.log("BBCA D1 Chart:", JSON.stringify({
        success: chartBBCA?.success,
        ticker: chartBBCA?.ticker,
        barsCount: chartBBCA?.bars?.length,
        source: chartBBCA?.source,
        sampleBar: chartBBCA?.bars?.[chartBBCA?.bars?.length - 1]
    }));

    const chartAAPL = await page.evaluate(() => {
        return window.TradingNativeBridge && window.TradingNativeBridge.getChartData 
            ? JSON.parse(window.TradingNativeBridge.getChartData('US:AAPL', 'D1'))
            : null;
    });
    console.log("AAPL D1 Chart:", JSON.stringify({
        success: chartAAPL?.success,
        ticker: chartAAPL?.ticker,
        barsCount: chartAAPL?.bars?.length,
        source: chartAAPL?.source,
        sampleBar: chartAAPL?.bars?.[chartAAPL?.bars?.length - 1]
    }));

    // 2. Buka layar detail saham BBCA
    console.log("Navigating to Pasar -> BBCA detail...");
    await page.click('button.nav-item[data-tab="pasar"]');
    await page.waitForTimeout(400);
    await page.fill('#search-stock-input', 'BBCA');
    await page.waitForTimeout(300);
    await page.locator('#stock-list-container .stock-list-item:has-text("BBCA")').first().click();
    await page.waitForSelector('.flow-page[aria-label="BBCA"]', { timeout: 5000 });

    // Gulir langsung ke area grafik
    await page.locator('.chart-controls').scrollIntoViewIfNeeded();
    await page.waitForTimeout(400);

    const shot1 = path.join(evidenceDir, 'chart-bbca-controls-and-plot.png');
    await page.screenshot({ path: shot1 });
    console.log("Captured BBCA Chart Controls & Plot:", shot1);

    // 3. Uji tombol ganti tipe grafik: Garis (Line Chart)
    console.log("Switching to Line Chart (Garis)...");
    await page.locator('.chart-type button:has-text("Garis")').click();
    await page.waitForTimeout(400);

    const shot2 = path.join(evidenceDir, 'chart-bbca-line-mode.png');
    await page.screenshot({ path: shot2 });
    console.log("Captured BBCA Line Mode:", shot2);

    // 4. Buka Tabel OHLCV Alternatif
    console.log("Toggling alternative OHLCV table...");
    await page.locator('button:has-text("Tabel OHLCV Alternatif")').click();
    await page.waitForTimeout(400);

    const shot3 = path.join(evidenceDir, 'chart-bbca-ohlcv-table.png');
    await page.screenshot({ path: shot3 });
    console.log("Captured BBCA OHLCV Table:", shot3);

    await browser.close();
    console.log("=== CHART EMITEN VERIFICATION SUCCEEDED ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
