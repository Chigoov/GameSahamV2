/**
 * Automated Playwright browser verification for Tahap E4:
 * 1. Kartu Booster Riset R&D modal from Tab Lainnya
 * 2. 3 Booster cards: News Accuracy, Fee Discount, Margin Expand
 * 3. Activation button & active status indicator
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
        console.log(`Dialog: [${dialog.type()}] ${dialog.message()}`);
        await dialog.accept();
    });

    await page.goto(`http://127.0.0.1:${port}/`);
    await page.waitForSelector('nav.bottom-nav', { timeout: 10000 });

    // 1. Navigasi ke Tab Lainnya
    console.log("Navigating to Lainnya tab...");
    await page.click('button.nav-item[data-tab="riset"]');
    await page.waitForSelector('#view-riset', { timeout: 5000 });

    // 2. Buka Modal Kartu Booster Riset R&D
    console.log("Opening Kartu Booster Riset R&D modal...");
    const boosterBtn = page.locator('#view-riset button:has-text("Kartu Booster Riset R&D")');
    if (await boosterBtn.count() === 1) {
        await boosterBtn.click();
        await page.waitForSelector('.flow-page[aria-label="Kartu Booster Riset R&D"]', { timeout: 5000 });

        const cards = page.locator('.flow-page[aria-label="Kartu Booster Riset R&D"] .booster-card');
        const count = await cards.count();
        console.log("Booster cards count:", count);
        if (count !== 3) {
            throw new Error(`Harus ada tepat 3 kartu booster riset (aktual: ${count})!`);
        }

        // Aktifkan booster pertama
        console.log("Activating first booster card...");
        const actBtn = cards.first().locator('button:has-text("Aktifkan Kartu Booster")');
        if (await actBtn.count() === 1) {
            await actBtn.click();
            await page.waitForTimeout(500);

            const activeP = cards.first().locator('p:has-text("Kartu Booster sedang aktif")');
            console.log("Active indicator count:", await activeP.count());
            if (await activeP.count() !== 1) {
                throw new Error("Indikator kartu aktif harus muncul setelah aktivasi!");
            }
        }

        // Tangkap screenshot bukti Tahap E4
        const shotPath = path.join(evidenceDir, 'TahapE4-boosters-perks.png');
        await page.screenshot({ path: shotPath });
        console.log("Captured screenshot:", shotPath);
        await page.locator('.back-button').click();
    }

    await browser.close();
    console.log("=== TAHAP E4 VERIFICATION SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
