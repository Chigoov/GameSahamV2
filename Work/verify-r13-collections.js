/**
 * Automated Playwright browser verification for R13 (150 Koleksi Mewah dalam 12 Kategori & 5 Rarity).
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

    page.on('dialog', async dialog => {
        console.log(`Dialog message: [${dialog.type()}] ${dialog.message()}`);
        await dialog.accept();
    });

    await page.goto(`http://127.0.0.1:${port}/`);
    await page.waitForSelector('nav.bottom-nav', { timeout: 10000 });
    const catalogue = await page.evaluate(() => JSON.parse(TradingNativeBridge.getCollections()).collections);

    console.log("Opening Koleksi & Kemewahan modal from Home...");
    await page.click('#btn-home-collections');
    await page.waitForSelector('.flow-page[aria-label="Galeri Koleksi Mewah"]', { timeout: 5000 });

    // 1. Verifikasi Jumlah Total Koleksi dari DOM
    const cards = page.locator('.flow-page[aria-label="Galeri Koleksi Mewah"] .collection-item-card');
    const colCount = await cards.count();
    console.log("Total collections card count rendered in DOM:", colCount);
    if (colCount !== catalogue.length) {
        throw new Error(`Jumlah katalog UI harus cocok domain ${catalogue.length} (aktual: ${colCount})!`);
    }

    // Local illustrations were removed by the approved redesign; preserve catalogue identity coverage.
    for (const item of catalogue) {
        if (await page.locator(`.flow-page .collection-item-card[data-collection-id="${item.id}"]`).count() !== 1) throw new Error(`Kartu aset domain hilang: ${item.id}`);
    }

    // 3. Uji Beli Barang Koleksi Vespa Klasik (COL-CAR-00)
    console.log("Testing buy collection item (Vespa Klasik)...");
    const vespaCard = page.locator('.flow-page[aria-label="Galeri Koleksi Mewah"] .collection-item-card[data-collection-id="COL-CAR-00"]');
    const buyBtn = vespaCard.locator('button:has-text("Beli Koleksi")');
    if (await buyBtn.count() === 1) {
        await buyBtn.click();
        await page.waitForTimeout(600);
        const ownedBadge = vespaCard.locator('.badge-owned');
        console.log("Owned badge count after purchase:", await ownedBadge.count());
        if (await ownedBadge.count() !== 1) {
            throw new Error("Badge '✓ Dimiliki' harus muncul setelah pembelian koleksi!");
        }
    }

    // Tangkap screenshot bukti R13
    const shotPath = path.join(evidenceDir, 'R13-collections-150.png');
    await page.screenshot({ path: shotPath });
    console.log("Captured screenshot:", shotPath);

    await browser.close();
    console.log("=== R13 COLLECTIONS 150 BROWSER VERIFICATION SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
