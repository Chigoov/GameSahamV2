/**
 * Automated Playwright browser verification for Tahap S3:
 * 1. Pohon Riset Finansial Tech Tree (Algo, Efficiency, Scale)
 * 2. Bursa Headhunting Staf Analis Elit from Tab Bisnis
 */
const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

async function run() {
    const port = process.env.QA_PORT || '8794';
    const evidenceDir = path.resolve(__dirname, '../Artifacts/Overall-Preview-2026-10-23');
    if (!fs.existsSync(evidenceDir)) fs.mkdirSync(evidenceDir, { recursive: true });

    console.log(`Connecting to http://127.0.0.1:${port}/...`);
    const browser = await chromium.launch({
        headless: true,
        args: ['--disable-dev-shm-usage', '--no-sandbox', '--disable-gpu']
    });
    const context = await browser.newContext({ viewport: { width: 412, height: 860 } });
    const page = await context.newPage();

    page.on('dialog', async dialog => {
        console.log(`Dialog: [${dialog.type()}] ${dialog.message()}`);
        await dialog.accept();
    });

    await page.goto(`http://127.0.0.1:${port}/`);
    await page.waitForSelector('nav.bottom-nav', { timeout: 10000 });

    // 1. Buka Tab Lainnya -> Pohon Riset Finansial Tech Tree
    console.log("Navigating to Lainnya -> Pohon Riset Finansial...");
    await page.click('button.nav-item[data-tab="riset"]');
    await page.waitForSelector('#view-riset', { timeout: 5000 });

    await page.locator('#view-riset button:has-text("Pohon Riset Finansial Tech Tree")').click();
    await page.waitForSelector('.flow-page[aria-label="Pohon Riset Finansial Tech Tree"]', { timeout: 5000 });

    const nodes = page.locator('.flow-page[aria-label="Pohon Riset Finansial Tech Tree"] .tech-node-card');
    const nodeCount = await nodes.count();
    console.log("Tech Tree nodes count:", nodeCount);
    if (nodeCount < 4) {
        throw new Error(`Harus ada minimal 4 node riset (aktual: ${nodeCount})!`);
    }
    const signalButton = page.locator('.flow-page[aria-label="Pohon Riset Finansial Tech Tree"] button:has-text("Hitung sinyal BBCA")');
    if (await signalButton.count() !== 1) throw new Error("Tech Tree harus memberi jalur sinyal untuk instrumen.");
    await signalButton.click();
    const signalCard = page.locator('.flow-page[aria-label="Pohon Riset Finansial Tech Tree"] .card:has-text("Sinyal algoritma sesi berjalan")');
    if (!(await signalCard.textContent()).includes("Arah:")) throw new Error("Sinyal algoritma harus menampilkan arah hasil domain.");
    console.log("Current-session BBCA signal UI verified:", (await signalCard.textContent()).slice(-140));

    // Upgrade salah satu node yang terkunci
    const upgradeBtn = page.locator('.flow-page[aria-label="Pohon Riset Finansial Tech Tree"] button:has-text("Riset Automasi Eksekusi Order")');
    if (await upgradeBtn.count() === 1) {
        console.log("Upgrading Tech Node Automasi Eksekusi Order...");
        await upgradeBtn.click();
        await page.waitForTimeout(500);
    }
    await page.locator('.back-button').click();
    await page.waitForTimeout(400);

    // 2. Buka Tab Bisnis -> Kelola Dana -> Bursa Headhunting Staf Analis
    console.log("Navigating to Bisnis -> Kelola Dana -> Headhunting...");
    await page.click('button.nav-item[data-tab="firma"]');
    await page.waitForTimeout(400);

    await page.locator('#firma-subtab-dana').click();
    await page.waitForTimeout(400);
    const aumCapacityText = await page.locator('#firma-subtab-content').textContent();
    if (!aumCapacityText.includes('Kapasitas AUM firma') || !aumCapacityText.includes('Pemakaian kapasitas AUM'))
        throw new Error('UI Kelola Dana harus menampilkan baseline kapasitas AUM dan pemakaiannya.');
    console.log('Firm AUM capacity and scale bonus visible in UI.');

    await page.locator('button:has-text("Bursa Headhunting Staf Analis Elit")').click();
    await page.waitForSelector('.flow-page[aria-label="Bursa Headhunting Staf Elit"]', { timeout: 5000 });

    const candidates = page.locator('.flow-page[aria-label="Bursa Headhunting Staf Elit"] .headhunt-card');
    const candCount = await candidates.count();
    console.log("Headhunting candidates count:", candCount);
    if (candCount < 3) {
        throw new Error(`Harus ada minimal 3 kandidat analis elit (aktual: ${candCount})!`);
    }

    // Bajak kandidat pertama
    const poachBtn = candidates.first().locator('button:has-text("Bajak")');
    if (await poachBtn.count() === 1) {
        console.log("Poaching candidate...");
        await poachBtn.click();
        await page.waitForTimeout(500);
    }

    // Tangkap screenshot bukti Tahap S3
    const shotPath = path.join(evidenceDir, 'TahapS3-techtree-headhunt.png');
    await page.screenshot({ path: shotPath });
    console.log("Captured screenshot:", shotPath);

    await browser.close();
    console.log("=== TAHAP S3 VERIFICATION SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
