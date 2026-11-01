/**
 * Automated Playwright browser verification for Tahap 5:
 * 1. Bisnis & Firma: Usaha, Kelola Dana & Klien (AUM separation)
 * 2. Status Era Baru (Legacy Rebirth)
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
        console.log(`Dialog: [${dialog.type()}] ${dialog.message()}`);
        await dialog.accept();
    });

    await page.goto(`http://127.0.0.1:${port}/`);
    await page.waitForSelector('nav.bottom-nav', { timeout: 10000 });

    // 1. Verifikasi Tab Bisnis & Kelola Dana
    console.log("Navigating to Bisnis tab...");
    await page.click('button.nav-item[data-tab="firma"]');
    await page.waitForSelector('#view-firma', { timeout: 5000 });

    const firmaText = await page.locator('#view-firma').textContent();
    console.log("Bisnis tab snippet:", firmaText.substring(0, 160));
    if (!firmaText.includes("Kelola Dana") && !firmaText.includes("DANA KELOLAAN KLIEN")) {
        throw new Error("Tab Bisnis harus memuat Kelola Dana & AUM Klien!");
    }

    // 2. Verifikasi Tab Lainnya & Status Era Baru
    console.log("Navigating to Lainnya tab...");
    await page.click('button.nav-item[data-tab="riset"]');
    await page.waitForSelector('#view-riset', { timeout: 5000 });

    const risetText = await page.locator('#view-riset').textContent();
    console.log("Lainnya tab snippet:", risetText.substring(0, 160));
    if (!risetText.includes("Era baru") && !risetText.includes("Pangkat yang diperlukan")) {
        throw new Error("Tab Lainnya harus memuat modul Era Baru / Prestige Rebirth!");
    }

    // Tangkap screenshot bukti Tahap 5
    const shotPath = path.join(evidenceDir, 'Tahap5-firm-prestige.png');
    await page.screenshot({ path: shotPath });
    console.log("Captured screenshot:", shotPath);

    await browser.close();
    console.log("=== TAHAP 5 FIRM & PRESTIGE VERIFICATION SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
