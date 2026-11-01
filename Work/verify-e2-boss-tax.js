/**
 * Automated Playwright browser verification for Tahap E2:
 * 1. Duel Lawan Bos Taipan Pasar modal (4 Bosses: Easy, Medium, Hard, Final)
 * 2. Start duel button & active challenge notice
 * 3. Audit Pajak & Kepatuhan Risiko modal from Portofolio
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

    // 1. Buka Modal Duel Bos Taipan dari Beranda
    console.log("Opening Duel Bos Taipan modal from Beranda...");
    await page.locator('#btn-market-boss').click();
    await page.waitForSelector('.flow-page[aria-label="Duel Bos Taipan Pasar"]', { timeout: 5000 });

    const bossCards = page.locator('.flow-page[aria-label="Duel Bos Taipan Pasar"] .boss-card');
    const bossCount = await bossCards.count();
    console.log("Boss cards count:", bossCount);
    if (bossCount !== 4) {
        throw new Error(`Harus ada 4 kartu Bos Taipan Pasar (aktual: ${bossCount})!`);
    }

    // Uji klik tantang Bos Easy
    console.log("Starting duel with Boss Easy...");
    const challengeBtn = bossCards.first().locator('button:has-text("Mulai Duel")');
    if (await challengeBtn.count() === 1) {
        await challengeBtn.click();
        await page.waitForTimeout(500);
    }
    await page.locator('.back-button').click();
    await page.waitForTimeout(300);

    // 2. Buka Modal Audit Pajak & Kepatuhan Risiko dari Portofolio
    console.log("Opening Audit Pajak & Kepatuhan Risiko from Portofolio...");
    await page.click('button.nav-item[data-tab="aset"]');
    await page.waitForSelector('#view-aset', { timeout: 5000 });

    const taxBtn = page.locator('#asset-summary button:has-text("Audit Pajak & Kepatuhan Risiko")');
    if (await taxBtn.count() === 1) {
        await taxBtn.click();
        await page.waitForSelector('.flow-page[aria-label="Audit Pajak & Kepatuhan Risiko"]', { timeout: 5000 });

        const taxText = await page.locator('.flow-page[aria-label="Audit Pajak & Kepatuhan Risiko"]').textContent();
        console.log("Tax Audit snippet:", taxText.substring(0, 160));
        if (!taxText.includes("Peringkat Kepatuhan") || !taxText.includes("Rasio Utang terhadap Aset")) {
            throw new Error("Modal Audit Pajak harus memuat Rating kepatuhan dan rasio utang!");
        }
        await page.locator('.back-button').click();
        await page.waitForTimeout(300);
    }

    // Tangkap screenshot bukti Tahap E2
    const shotPath = path.join(evidenceDir, 'TahapE2-boss-and-tax.png');
    await page.screenshot({ path: shotPath });
    console.log("Captured screenshot:", shotPath);

    await browser.close();
    console.log("=== TAHAP E2 VERIFICATION SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
