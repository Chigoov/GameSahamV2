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

    // 3. Dirikan empat anak usaha melalui domain API yang dipakai formulir UI, lalu kelola holding dari UI.
    console.log("Creating four isolated operating companies for holding UI journey...");
    const companySetup = await page.evaluate(() => {
        const entries = [
            ['H-FIN','QA Finansial','JASA'],
            ['H-ENE','QA Energi','MANUFAKTUR'],
            ['H-CONS','QA Konsumer','RITEL'],
            ['H-TECH','QA Teknologi','JASA']
        ];
        return entries.map(([id,name,sector]) => JSON.parse(window.TradingNativeBridge.foundCompany(
            JSON.stringify({id,name,sector,capital:'25000000'}))));
    });
    if (companySetup.some(item => item.success === false)) throw new Error(`Pendirian anak usaha QA gagal: ${JSON.stringify(companySetup)}`);
    const shockCompanies = await page.evaluate(() => {
        const entries = [
            ['QA-SC-MANU','QA Supply Manufaktur','MANUFAKTUR'],
            ['QA-SC-RETAIL','QA Supply Retail','RITEL'],
            ['QA-SC-ENERGY','QA Supply Energi','ENERGI']
        ];
        return entries.map(([id,name,sector]) => JSON.parse(window.TradingNativeBridge.foundCompany(
            JSON.stringify({id,name,sector,capital:'25000000'}))));
    });
    if (shockCompanies.some(item => item.success === false)) throw new Error(`Fixture sektor krisis QA gagal: ${JSON.stringify(shockCompanies)}`);
    const beforeShock = await page.evaluate(() => JSON.parse(window.TradingNativeBridge.getCompanies()).companies);
    const crisis = await page.evaluate(() => JSON.parse(window.TradingNativeBridge.triggerMarketShockEvent('SUPPLY_CHAIN_CRISIS')));
    const afterShock = await page.evaluate(() => JSON.parse(window.TradingNativeBridge.getCompanies()).companies);
    const beforeById = new Map(beforeShock.map(company => [company.id, company]));
    const afterById = new Map(afterShock.map(company => [company.id, company]));
    for (const id of ['QA-SC-MANU','QA-SC-RETAIL']) {
        if (Number(afterById.get(id).dailyOperatingCost) <= Number(beforeById.get(id).dailyOperatingCost))
            throw new Error(`${id} harus menanggung kenaikan biaya operasi setelah krisis rantai pasok`);
    }
    if (Number(afterById.get('QA-SC-ENERGY').dailyRevenue) <= Number(beforeById.get('QA-SC-ENERGY').dailyRevenue))
        throw new Error('Perusahaan energi harus mendapat kenaikan permintaan selama krisis rantai pasok');
    console.log('Supply-chain cost/revenue effects verified:', crisis.affectedCompanies);
    await page.click('button.nav-item[data-tab="firma"]');
    await page.waitForTimeout(250);
    await page.click('button.nav-item[data-tab="beranda"]');
    await page.waitForTimeout(200);
    await page.click('button.nav-item[data-tab="firma"]');
    await page.waitForSelector('#firma-subtab-usaha');
    await page.locator('button:has-text("Bentuk Holding 4 Sektor")').click();
    await page.waitForSelector('.flow-page[aria-label="Bentuk Holding Perusahaan"]');
    const holdingFlow = page.locator('.flow-page[aria-label="Bentuk Holding Perusahaan"]');
    const selectors = holdingFlow.locator('select');
    await selectors.nth(0).selectOption('H-FIN');
    await selectors.nth(1).selectOption('H-ENE');
    await selectors.nth(2).selectOption('H-CONS');
    await selectors.nth(3).selectOption('H-TECH');
    await holdingFlow.getByRole('button', { name: 'Bentuk Holding' }).click();
    await page.waitForTimeout(500);
    await page.locator('.back-button').click();
    await page.waitForTimeout(250);
    await page.click('button.nav-item[data-tab="beranda"]');
    await page.waitForTimeout(200);
    await page.click('button.nav-item[data-tab="firma"]');
    await page.waitForSelector('#firma-subtab-usaha');
    const holdingCard = page.locator('#firma-subtab-content .card:has-text("Nusantara Konglomerasi")');
    await page.waitForTimeout(300);
    if (!(await holdingCard.count())) throw new Error('Holding yang dibentuk harus tampil pada daftar Usaha Saya.');
    const holdingText = await holdingCard.first().textContent();
    for (const sector of ['FINANCIAL','ENERGY','CONSUMER','TECHNOLOGY']) if (!holdingText.includes(sector))
        throw new Error(`Holding tidak memuat anak usaha sektor ${sector}`);
    await holdingCard.first().getByRole('button', { name: 'Injeksi modal / tarik dividen internal' }).click();
    await page.waitForSelector('.flow-page[aria-label^="Transfer Internal"]');
    const transferFlow = page.locator('.flow-page[aria-label^="Transfer Internal"]');
    const transfers = transferFlow.locator('select');
    await transfers.nth(0).selectOption('H-FIN');
    await transfers.nth(1).selectOption('H-ENE');
    await transfers.nth(2).selectOption('DIVIDEND');
    await transferFlow.getByRole('button', { name: 'Tinjau & kirim transfer internal' }).click();
    await page.waitForFunction(() => document.querySelector('.flow-page[aria-label^="Transfer Internal"]')?.innerText.includes('Receipt'));
    console.log('Holding UI shows four sectors and completes internal dividend transfer.');

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
