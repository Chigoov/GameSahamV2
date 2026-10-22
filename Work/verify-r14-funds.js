/**
 * Automated Playwright browser verification for R14 (24 Reksa Dana & Factsheet Modal).
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
    const context = await browser.newContext({ viewport: { width: 438, height: 627 } });
    const page = await context.newPage();

    await page.goto(`http://127.0.0.1:${port}/`);
    await page.waitForSelector('nav.bottom-nav', { timeout: 10000 });

    console.log("Navigating to Pasar tab...");
    await page.click('button.nav-item[data-tab="pasar"]');
    await page.waitForSelector('#btn-reksa-dana', { timeout: 5000 });

    console.log("Opening Reksa Dana modal...");
    await page.locator('#btn-reksa-dana').click();
    await page.waitForSelector('.flow-page[aria-label="Pasar Reksa Dana (Unit/NAB)"]', { timeout: 5000 });

    const modal = page.locator('.flow-page[aria-label="Pasar Reksa Dana (Unit/NAB)"]');

    // 1. Verifikasi Jumlah Total 24 Produk Reksa Dana
    const cards = modal.locator('article.card');
    const fundCount = await cards.count();
    console.log("Total mutual fund cards rendered:", fundCount);
    if (fundCount !== 24) {
        throw new Error(`Total produk reksa dana harus tepat 24 (aktual: ${fundCount})!`);
    }

    // 2. Verifikasi Factsheet / Informasi Produk
    const firstCard = cards.first();
    const title = await firstCard.locator('h3').textContent();
    const cardText = await firstCard.textContent();
    console.log("First Fund title:", title);
    console.log("First Fund details:", cardText.substring(0, 140));

    if (!cardText.includes("NAB per unit") || !cardText.includes("AUM") || !cardText.includes("Manajer Investasi")) {
        throw new Error("Kartu reksa dana harus memuat NAB, AUM, dan Manajer Investasi!");
    }

    // Tangkap screenshot bukti R14
    const shotPath = path.join(evidenceDir, 'R14-mutual-funds-24.png');
    await page.screenshot({ path: shotPath });
    console.log("Captured screenshot:", shotPath);

    await browser.close();
    console.log("=== R14 MUTUAL FUNDS 24 BROWSER VERIFICATION SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
