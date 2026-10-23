/**
 * Automated Playwright browser verification for Tahap S4:
 * 1. Jalur Disiplin Harian (Streak Strip & Multiplier)
 * 2. Status Dominasi & Pengaruh Sektor Bursa
 * 3. Guncangan Pasar Mendadak (Market Shock Event)
 */
const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

async function run() {
    const port = process.env.QA_PORT || '8794';
    const evidenceDir = path.resolve(__dirname, '../Artifacts/Overall-Preview-2026-10-23');
    if (!fs.existsSync(evidenceDir)) fs.mkdirSync(evidenceDir, { recursive: true });

    console.log(`Connecting to http://127.0.0.1:${port}/...`);
    const browser = await chromium.launch({
        headless: true,
        args: ['--disable-dev-shm-usage', '--no-sandbox', '--disable-gpu']
    });
    const context = await browser.newContext({ viewport: { width: 412, height: 860 } });
    const page = await context.newPage();

    page.on('dialog', async dialog => {
        console.log(`Dialog: [${dialog.type()}] ${dialog.message()}`);
        await dialog.accept();
    });

    await page.goto(`http://127.0.0.1:${port}/`);
    await page.waitForSelector('nav.bottom-nav', { timeout: 10000 });

    // 1. Buka Modal Disiplin Harian dari Beranda
    console.log("Opening Jalur Disiplin Harian modal from Beranda...");
    await page.locator('#btn-home-streak').click();
    await page.waitForSelector('.flow-page[aria-label="Jalur Disiplin Harian (Streak)"]', { timeout: 5000 });

    const streakText = await page.locator('.flow-page[aria-label="Jalur Disiplin Harian (Streak)"]').textContent();
    console.log("Streak snippet:", streakText.substring(0, 160));
    if (!streakText.includes("Streak Saat Ini") || !streakText.includes("Pengali Hadiah Modal")) {
        throw new Error("Modal streak harus memuat hari streak dan pengali hadiah modal!");
    }
    await page.locator('.back-button').click();
    await page.waitForTimeout(300);

    // 2. Buka Modal Dominasi Sektor dari Beranda
    console.log("Opening Dominasi Sektor modal from Beranda...");
    await page.locator('#btn-home-dominance').click();
    await page.waitForSelector('.flow-page[aria-label="Status Dominasi Sektor Bursa"]', { timeout: 5000 });

    const sectorCards = page.locator('.flow-page[aria-label="Status Dominasi Sektor Bursa"] .card');
    const secCount = await sectorCards.count();
    console.log("Sector cards count:", secCount);
    if (secCount < 3) {
        throw new Error(`Minimal harus ada 3 sektor industri (aktual: ${secCount})!`);
    }

    // 3. Uji Pemicu Guncangan Pasar Mendadak (Market Shock) via Bridge
    console.log("Triggering Black Swan Market Shock via bridge...");
    const shockRes = await page.evaluate(() => {
        return window.TradingNativeBridge && window.TradingNativeBridge.triggerMarketShockEvent
            ? JSON.parse(window.TradingNativeBridge.triggerMarketShockEvent('BLACK_SWAN_SHOCK'))
            : null;
    });
    console.log("Shock response:", JSON.stringify(shockRes));
    if (!shockRes || !shockRes.success) {
        throw new Error("Pemicu guncangan pasar Black Swan harus berhasil!");
    }

    // Tangkap screenshot bukti Tahap S4
    const shotPath = path.join(evidenceDir, 'TahapS4-streak-dominance.png');
    await page.screenshot({ path: shotPath });
    console.log("Captured screenshot:", shotPath);

    await browser.close();
    console.log("=== TAHAP S4 VERIFICATION SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
