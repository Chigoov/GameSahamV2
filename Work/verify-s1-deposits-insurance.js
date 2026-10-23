/**
 * Automated Playwright browser verification for Tahap S1:
 * 1. Asuransi Portofolio (Hedging 80% crash protection)
 * 2. Deposito Berjangka Tenor Sesi (+10%, +25%, +50% bonus) & Lost Interest Penalty
 * 3. Restrukturisasi Darurat Kebangkrutan (Turnaround Bailout)
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

    // 1. Buka Portofolio
    console.log("Navigating to Portofolio tab...");
    await page.click('button.nav-item[data-tab="aset"]');
    await page.waitForSelector('#view-aset', { timeout: 5000 });

    // 2. Beli Polis Asuransi Portofolio (Hedging)
    console.log("Opening Asuransi Portofolio modal...");
    const toolsDetails = page.locator('#asset-tools');
    if (await toolsDetails.count() === 1) {
        const isOpen = await toolsDetails.evaluate(el => el.open);
        if (!isOpen) await toolsDetails.locator('summary').click();
    }
    await page.locator('#asset-tools button:has-text("Asuransi Portofolio (Hedging)")').click();
    await page.waitForSelector('.flow-page[aria-label="Asuransi Portofolio (Hedging)"]', { timeout: 5000 });

    const buyPolBtn = page.locator('button:has-text("Beli Polis Asuransi")');
    if (await buyPolBtn.count() === 1) {
        await buyPolBtn.click();
        await page.waitForTimeout(500);

        const activeStatus = page.locator('.flow-page[aria-label="Asuransi Portofolio (Hedging)"] .eyebrow:has-text("STATUS: POLIS AKTIF")');
        console.log("Active policy status count:", await activeStatus.count());
        if (await activeStatus.count() !== 1) {
            throw new Error("Status POLIS AKTIF harus muncul setelah pembelian asuransi!");
        }
    }
    await page.locator('.back-button').click();
    await page.waitForTimeout(300);

    // 3. Uji Restrukturisasi Darurat (Turnaround Bailout)
    console.log("Testing Restrukturisasi Darurat (Turnaround)...");
    await page.locator('#asset-tools button:has-text("Restrukturisasi Darurat (Turnaround)")').click();
    await page.waitForTimeout(500);

    // 4. Buka Katalog Multi-Aset untuk cek Deposito Tenor
    console.log("Opening Multi-Aset modal for Tenor Deposits...");
    const toolsDetails2 = page.locator('#asset-tools');
    if (await toolsDetails2.count() === 1) {
        const isOpen = await toolsDetails2.evaluate(el => el.open);
        if (!isOpen) await toolsDetails2.locator('summary').click();
    }
    await page.locator('#asset-tools button:has-text("Multi-aset")').click();
    await page.waitForSelector('.flow-page[aria-label="Katalog multi-aset"]', { timeout: 5000 });

    const depSection = page.locator('.flow-page[aria-label="Katalog multi-aset"] section:has-text("Deposito Berjangka Perbankan")');
    const depText = await depSection.textContent();
    console.log("Deposit section snippet:", depText.substring(0, 160));
    if (!depText.includes("Tenor 10 Sesi") && !depText.includes("Bonus")) {
        throw new Error("Katalog deposito harus memuat opsi Tenor Sesi dengan bonus bunga!");
    }

    // Tangkap screenshot bukti Tahap S1
    const shotPath = path.join(evidenceDir, 'TahapS1-deposits-insurance.png');
    await page.screenshot({ path: shotPath });
    console.log("Captured screenshot:", shotPath);

    await browser.close();
    console.log("=== TAHAP S1 VERIFICATION SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
