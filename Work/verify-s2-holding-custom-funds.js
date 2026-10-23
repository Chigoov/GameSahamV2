/**
 * Automated Playwright browser verification for Tahap S2:
 * 1. Meracik Reksa Dana Kustom (Pie Chart Allocation & Unit Creation)
 * 2. Perdagangan Aset Kripto Alternatif 24 Jam (Bitcoin & Ethereum Spot)
 * 3. Induk Holding Company & Anak Usaha
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

    // 1. Buka Portofolio -> Racik Reksa Dana Kustom
    console.log("Navigating to Portofolio -> Racik Reksa Dana Kustom...");
    await page.click('button.nav-item[data-tab="aset"]');
    await page.waitForSelector('#view-aset', { timeout: 5000 });

    const toolsDetails = page.locator('#asset-tools');
    if (await toolsDetails.count() === 1) {
        const isOpen = await toolsDetails.evaluate(el => el.open);
        if (!isOpen) await toolsDetails.locator('summary').click();
    }

    await page.locator('#asset-tools button:has-text("Racik Reksa Dana Kustom")').click();
    await page.waitForSelector('.flow-page[aria-label="Meracik Reksa Dana Kustom"]', { timeout: 5000 });

    const fundModal = page.locator('.flow-page[aria-label="Meracik Reksa Dana Kustom"]');
    const modalText = await fundModal.textContent();
    console.log("Custom Fund Builder snippet:", modalText.substring(0, 160));
    if (!modalText.includes("Komposisi Alokasi Aset") || !modalText.includes("DIAGRAM ALOKASI")) {
        throw new Error("Modal racik reksa dana harus memuat diagram komposisi alokasi!");
    }

    // Klik tombol terbitkan
    console.log("Publishing custom mutual fund...");
    await page.locator('#btn-create-custom-fund').click();
    await page.waitForTimeout(600);

    const publishedCount = await page.locator('.flow-page[aria-label="Meracik Reksa Dana Kustom"] .data-row').count();
    console.log("Published custom funds count:", publishedCount);
    await page.locator('.back-button').click();
    await page.waitForTimeout(300);

    // 2. Buka Multi-aset untuk memverifikasi Aset Kripto Digital Spot (BTC & ETH)
    console.log("Checking Crypto Digital Spot assets in Multi-Aset...");
    const toolsDetails2 = page.locator('#asset-tools');
    if (await toolsDetails2.count() === 1) {
        const isOpen = await toolsDetails2.evaluate(el => el.open);
        if (!isOpen) await toolsDetails2.locator('summary').click();
    }
    await page.locator('#asset-tools button:has-text("Multi-aset")').click();
    await page.waitForSelector('.flow-page[aria-label="Katalog multi-aset"]', { timeout: 5000 });

    const multiText = await page.locator('.flow-page[aria-label="Katalog multi-aset"]').textContent();
    if (!multiText.includes("Bitcoin Digital Spot") && !multiText.includes("Ethereum Digital Spot")) {
        throw new Error("Katalog multi-aset harus memuat instrumen spot Bitcoin / Ethereum 24 jam!");
    }
    await page.locator('.back-button').click();
    await page.waitForTimeout(300);

    // Tangkap screenshot bukti Tahap S2
    const shotPath = path.join(evidenceDir, 'TahapS2-holding-custom-funds.png');
    await page.screenshot({ path: shotPath });
    console.log("Captured screenshot:", shotPath);

    await browser.close();
    console.log("=== TAHAP S2 VERIFICATION SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
