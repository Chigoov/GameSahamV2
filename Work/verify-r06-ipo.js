/**
 * Automated Playwright browser verification for R06 (Minimal 3 IPO per bulan, bookbuilding, pesan lot).
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
    await page.waitForSelector('#view-pasar', { timeout: 5000 });

    console.log("Opening Pusat IPO modal...");
    await page.locator('#btn-ipo-center').click();
    await page.waitForSelector('.flow-page[aria-label="Pusat IPO (Pasar perdana)"]', { timeout: 5000 });

    // Verifikasi minimal 3 kartu IPO tampil di modal
    const ipoCards = page.locator('.flow-page[aria-label="Pusat IPO (Pasar perdana)"] section.card');
    const ipoCount = await ipoCards.count();
    console.log("Active IPO offerings count:", ipoCount);
    if (ipoCount < 3) {
        throw new Error(`Minimal harus ada 3 penawaran IPO aktif (aktual: ${ipoCount})!`);
    }

    // Periksa rincian kartu IPO pertama
    const firstCard = ipoCards.first();
    const title = await firstCard.locator('h2').textContent();
    const cardText = await firstCard.textContent();
    console.log("First IPO title:", title);
    console.log("First IPO card text:", cardText.substring(0, 150));

    if (!cardText.includes("Harga penawaran") || !cardText.includes("Saham ditawarkan") || !cardText.includes("Tanggal listing")) {
        throw new Error("Kartu IPO harus memuat harga, jumlah saham, dan tanggal listing!");
    }

    // Pastikan ada tombol / input pemesanan lot
    const orderBtn = firstCard.locator('button:has-text("Pesan saham IPO")');
    if (await orderBtn.count() !== 1) {
        throw new Error("Kartu IPO aktif harus memiliki tombol 'Pesan saham IPO'!");
    }

    // Tangkap screenshot bukti R06
    const shotPath = path.join(evidenceDir, 'R06-ipo-offerings.png');
    await page.screenshot({ path: shotPath });
    console.log("Captured screenshot:", shotPath);

    await browser.close();
    console.log("=== R06 IPO BROWSER VERIFICATION SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
