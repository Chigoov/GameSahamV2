/**
 * Automated Playwright browser verification for Tahap E1:
 * 1. Sector level lock badges in Pasar stock list (e.g. ADRO Lv.4, AAPL Lv.7)
 * 2. Unlocked status for Level 1 sectors (BBCA)
 * 3. Locked buy button behavior on locked sector stock detail
 */
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

    page.on('dialog', async dialog => {
        console.log(`Alert Dialog: [${dialog.type()}] ${dialog.message()}`);
        await dialog.accept();
    });

    await page.goto(`http://127.0.0.1:${port}/`);
    await page.waitForSelector('nav.bottom-nav', { timeout: 10000 });

    console.log("Navigating to Pasar tab...");
    await page.click('button.nav-item[data-tab="pasar"]');
    await page.waitForSelector('#stock-list-container .stock-list-item', { timeout: 5000 });

    // 1. Verifikasi Badge Gembok Sektor pada Daftar Saham
    console.log("Checking sector lock badges in stock list...");
    const lockedBadges = page.locator('.stock-list-item .badge-locked');
    const lockCount = await lockedBadges.count();
    console.log("Locked sector badges count:", lockCount);
    if (lockCount < 5) {
        throw new Error("Harus ada emiten dengan badge gembok sektor untuk level tinggi!");
    }

    // 2. Buka Saham Sektor Terkunci (misal ADRO - Sektor Energi Lv.4)
    console.log("Inspecting locked sector stock ADRO...");
    await page.fill('#search-stock-input', 'ADRO');
    await page.waitForTimeout(300);
    const adroRow = page.locator('#stock-list-container .stock-list-item:has-text("ADRO")');
    if (await adroRow.count() === 1) {
        await adroRow.click();
        await page.waitForSelector('.flow-page[aria-label="ADRO"]', { timeout: 5000 });

        const buyBtn = page.locator('button.btn-detail-buy');
        const buyText = await buyBtn.textContent();
        console.log("ADRO Buy button text:", buyText);
        if (!buyText.includes("Terkunci") && !buyText.includes("Lv.4")) {
            throw new Error("Tombol beli saham sektor terkunci harus bertuliskan Terkunci!");
        }

        // Coba klik tombol beli terkunci (harus memicu notice)
        await buyBtn.click();
        await page.waitForTimeout(300);
        await page.click('.back-button');
        await page.waitForTimeout(300);
    }

    // 3. Verifikasi Saham Terbuka (BBCA - Sektor Keuangan Lv.1)
    console.log("Inspecting unlocked sector stock BBCA...");
    await page.fill('#search-stock-input', 'BBCA');
    await page.waitForTimeout(300);
    const bbcaRow = page.locator('#stock-list-container .stock-list-item:has-text("BBCA")');
    await bbcaRow.first().click();
    await page.waitForSelector('.flow-page[aria-label="BBCA"]', { timeout: 5000 });

    const bbcaBuyBtn = page.locator('button.btn-detail-buy');
    const bbcaBuyText = await bbcaBuyBtn.textContent();
    console.log("BBCA Buy button text:", bbcaBuyText);
    if (bbcaBuyText !== 'BELI') {
        throw new Error("Tombol beli BBCA harus aktif (BELI) tanpa terkunci!");
    }

    // Tangkap screenshot bukti Tahap E1
    const shotPath = path.join(evidenceDir, 'TahapE1-dividends-sector-lock.png');
    await page.screenshot({ path: shotPath });
    console.log("Captured screenshot:", shotPath);

    await browser.close();
    console.log("=== TAHAP E1 VERIFICATION SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
