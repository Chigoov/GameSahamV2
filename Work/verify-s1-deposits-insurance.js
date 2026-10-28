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

    const dialogs = [];
    page.on('dialog', async dialog => {
        dialogs.push({ type: dialog.type(), message: dialog.message() });
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
    const bailoutDialog = dialogs.find(d => d.type === 'alert' && d.message.includes('Fase pemulihan belum tersedia'));
    if (!bailoutDialog || dialogs.some(d => d.message.includes('suntikan modal darurat Rp 25.000.000'))) {
        throw new Error('Akun sehat harus ditolak sebelum lelang/restrukturisasi dan tidak boleh dijanjikan hibah kas.');
    }
    console.log('Healthy account bailout gate verified; no free-cash promise.');
    dialogs.length = 0;

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
    for (const label of ['Tenor 10 Sesi (+10% Bonus)', 'Tenor 25 Sesi (+25% Bonus)', 'Tenor 50 Sesi (+50% Bonus)']) {
        if (!depText.includes(label)) throw new Error(`Katalog deposito tidak menampilkan ${label}`);
    }
    const opened = await page.evaluate(() => JSON.parse(window.TradingNativeBridge.openTimeDeposit(
        JSON.stringify({ productId: 'DEP-TENOR-10', amount: '10000000' }))));
    if (!opened.success) throw new Error(`Deposito QA gagal ditempatkan: ${opened.message}`);
    await page.locator('.back-button').click();
    await page.waitForTimeout(250);
    await page.locator('#asset-tools button:has-text("Multi-aset")').click();
    await page.waitForSelector('.flow-page[aria-label="Katalog multi-aset"]', { timeout: 5000 });
    const activeDeposit = page.locator('.flow-page[aria-label="Katalog multi-aset"] article.card:has-text("Deposito Tenor 10 Sesi")').first();
    if (!(await activeDeposit.textContent()).includes('Seluruh bunga hangus'))
        throw new Error('Label pencairan dini harus menyebut seluruh bunga hangus');
    await page.screenshot({ path: path.join(evidenceDir, 'TahapS1-deposits-insurance.png') });
    console.log("Captured screenshot:", path.join(evidenceDir, 'TahapS1-deposits-insurance.png'));

    await browser.close();
    console.log("=== TAHAP S1 VERIFICATION SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});

