/**
 * Automated Playwright browser verification for R10 (Produk multi-aset & deposito perbankan).
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

    // Auto-accept alert/confirm dialogs
    page.on('dialog', async dialog => {
        console.log(`Dialog message: [${dialog.type()}] ${dialog.message()}`);
        await dialog.accept();
    });

    await page.goto(`http://127.0.0.1:${port}/`);
    await page.waitForSelector('nav.bottom-nav', { timeout: 10000 });

    console.log("Navigating to Pasar tab...");
    await page.click('button.nav-item[data-tab="pasar"]');
    await page.waitForSelector('#btn-multiasset', { timeout: 5000 });

    console.log("Opening Multi-aset modal...");
    await page.locator('#btn-multiasset').click();
    await page.waitForSelector('.flow-page[aria-label="Katalog multi-aset"]', { timeout: 5000 });

    const modal = page.locator('.flow-page[aria-label="Katalog multi-aset"]');

    // 1. Verifikasi Seksi Obligasi
    const bondTitle = await modal.locator('h2:has-text("Obligasi negara")').count();
    console.log("Bond section count:", bondTitle);
    if (bondTitle !== 1) throw new Error("Seksi Obligasi negara harus tersedia!");

    // 2. Verifikasi Seksi Komoditas
    const commTitle = await modal.locator('h2:has-text("Komoditas fisik")').count();
    console.log("Commodity section count:", commTitle);
    if (commTitle !== 1) throw new Error("Seksi Komoditas fisik harus tersedia!");

    // 3. Verifikasi Seksi Deposito
    const depTitle = await modal.locator('h2:has-text("Deposito Berjangka Perbankan")').count();
    console.log("Deposit section count:", depTitle);
    if (depTitle !== 1) throw new Error("Seksi Deposito Berjangka Perbankan harus tersedia!");

    // Coba Buka Deposito Baru
    console.log("Testing Open Time Deposit...");
    const openBtn = modal.locator('button:has-text("Buka Deposito")').first();
    await openBtn.click();
    await page.waitForTimeout(600);

    // Verifikasi Deposito Aktif muncul
    const activeDepText = await modal.locator('h3:has-text("Deposito Aktif")').textContent();
    console.log("Active deposit header:", activeDepText);
    if (!activeDepText.includes("Deposito Aktif")) {
        throw new Error("Penempatan deposito aktif harus muncul setelah pembukaan!");
    }

    // Tangkap screenshot bukti R10
    const shotPath = path.join(evidenceDir, 'R10-multiasset-deposit.png');
    await page.screenshot({ path: shotPath });
    console.log("Captured screenshot:", shotPath);

    await browser.close();
    console.log("=== R10 MULTI-ASSET & DEPOSIT BROWSER VERIFICATION SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
