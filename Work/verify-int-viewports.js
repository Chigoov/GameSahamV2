/**
 * Grand Integration & Multi-Viewport Suite (INT).
 * Menguji 7 ukuran layar (Mobile portrait, Compact, Foldable, Landscape tablet, Desktop)
 * dan preferensi aksesibilitas font 200%.
 */
const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

const viewports = [
    { name: 'Mobile-Compact', width: 360, height: 640 },
    { name: 'Mobile-Standard', width: 412, height: 915 },
    { name: 'Xiaomi-Portrait', width: 438, height: 627 },
    { name: 'Foldable-Main', width: 673, height: 841 },
    { name: 'Tablet-Portrait', width: 768, height: 1024 },
    { name: 'Tablet-Landscape', width: 1024, height: 768 },
    { name: 'Desktop-FHD', width: 1280, height: 800 }
];

async function run() {
    const port = process.env.QA_PORT || '8794';
    const evidenceDir = path.resolve(__dirname, '../Artifacts/Owner-Revision-14-2026-10-22/run-01/evidence');
    if (!fs.existsSync(evidenceDir)) fs.mkdirSync(evidenceDir, { recursive: true });

    console.log(`Connecting to http://127.0.0.1:${port}/...`);
    const browser = await chromium.launch({ headless: true });

    for (const vp of viewports) {
        console.log(`Testing viewport ${vp.name} (${vp.width}x${vp.height})...`);
        const context = await browser.newContext({ viewport: { width: vp.width, height: vp.height } });
        const page = await context.newPage();

        await page.goto(`http://127.0.0.1:${port}/`);
        await page.waitForSelector('nav.bottom-nav', { timeout: 10000 });

        // Screenshot Beranda
        await page.screenshot({ path: path.join(evidenceDir, `INT-viewport-${vp.name}-home.png`) });

        // Navigasi ke Pasar
        await page.click('button.nav-item[data-tab="pasar"]');
        await page.waitForSelector('#view-pasar', { timeout: 5000 });
        await page.screenshot({ path: path.join(evidenceDir, `INT-viewport-${vp.name}-market.png`) });

        await context.close();
    }

    // Uji Aksesibilitas Font 200%
    console.log("Testing Accessibility Text Size 200%...");
    const fontContext = await browser.newContext({ viewport: { width: 438, height: 627 } });
    const fontPage = await fontContext.newPage();
    await fontPage.goto(`http://127.0.0.1:${port}/`);
    await fontPage.waitForSelector('nav.bottom-nav', { timeout: 10000 });

    await fontPage.evaluate(() => {
        document.documentElement.style.fontSize = '200%';
    });
    await fontPage.waitForTimeout(400);
    await fontPage.screenshot({ path: path.join(evidenceDir, 'INT-font-200-percent.png') });
    await fontContext.close();

    await browser.close();
    console.log("=== INT MULTI-VIEWPORT SUITE COMPLETED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
