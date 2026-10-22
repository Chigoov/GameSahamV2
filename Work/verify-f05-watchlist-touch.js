/**
 * Verification for F05: Watchlist star button touch target >= 48x48px and keyboard handling.
 */
const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

async function run() {
    const port = process.env.QA_PORT || '8794';
    const evidenceDir = path.resolve(__dirname, '../Artifacts/Owner-Revision-14-2026-10-22/run-01/evidence');
    if (!fs.existsSync(evidenceDir)) fs.mkdirSync(evidenceDir, { recursive: true });

    console.log(`Connecting to http://127.0.0.1:${port}/...`);
    const browser = await chromium.launch({ headless: true });
    // Viewport 360x640 as reported in F05 finding
    const context = await browser.newContext({ viewport: { width: 360, height: 640 } });
    const page = await context.newPage();

    await page.goto(`http://127.0.0.1:${port}/`);
    await page.waitForSelector('nav.bottom-nav', { timeout: 10000 });

    console.log("Navigating to Pasar tab...");
    await page.click('button.nav-item[data-tab="pasar"]');
    await page.waitForSelector('#stock-list-container .stock-list-item', { timeout: 5000 });

    const firstRow = page.locator('#stock-list-container .stock-list-item').first();
    const starBtn = firstRow.locator('.watchlist-star-btn');

    // 1. Ukur Touch Target (Bounding Box)
    const box = await starBtn.boundingBox();
    console.log(`Watchlist Star Touch Target on 360x640: ${box.width}px x ${box.height}px`);
    if (box.width < 47.9 || box.height < 47.9) {
        throw new Error(`Watchlist star button target too small: ${box.width}x${box.height}px < 48x48px!`);
    }

    // 2. Keyboard Space: Fokus pada tombol bintang dan tekan Space
    console.log("Testing Space key on star button...");
    await starBtn.focus();
    await page.keyboard.press('Space');
    await page.waitForTimeout(300);

    // Pastikan modal detail TIDAK terbuka karena Space di tombol bintang
    const detailModalCount = await page.locator('.flow-page[aria-label^="Detail"]').count();
    console.log(`Detail modal count after Space on star: ${detailModalCount}`);
    if (detailModalCount !== 0) {
        throw new Error("Menekan Space pada tombol bintang tidak boleh membuka detail modal (keydown bubble terdeteksi)!");
    }

    const ariaPressed = await starBtn.getAttribute('aria-pressed');
    console.log("Aria pressed after Space:", ariaPressed);
    if (ariaPressed !== 'true') {
        throw new Error("Aria-pressed harus 'true' setelah tombol ditekan!");
    }

    // 3. Keyboard Enter: Tekan Enter untuk toggle kembali
    console.log("Testing Enter key on star button...");
    await page.keyboard.press('Enter');
    await page.waitForTimeout(300);

    const detailModalCount2 = await page.locator('.flow-page[aria-label^="Detail"]').count();
    if (detailModalCount2 !== 0) {
        throw new Error("Menekan Enter pada tombol bintang tidak boleh membuka detail modal!");
    }
    const ariaPressed2 = await starBtn.getAttribute('aria-pressed');
    console.log("Aria pressed after Enter:", ariaPressed2);
    if (ariaPressed2 !== 'false') {
        throw new Error("Aria-pressed harus 'false' setelah di-untoggle!");
    }

    // 4. Klik pada baris saham di luar tombol bintang -> harus membuka detail modal!
    console.log("Clicking stock row outside star button...");
    await firstRow.click();
    await page.waitForSelector('.flow-page[aria-label^="BBCA"], .flow-page[aria-label^="Detail"]', { timeout: 5000 });
    console.log("Detail modal opened successfully on row click!");

    const shotPath = path.join(evidenceDir, 'F05-watchlist-touch-target.png');
    await page.screenshot({ path: shotPath });
    console.log("Captured screenshot:", shotPath);

    await browser.close();
    console.log("=== F05 WATCHLIST TOUCH TARGET & KEYBOARD TEST PASSED (PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
