/**
 * Automated Playwright browser verification for Tahap E3:
 * 1. Modal Penghargaan & Medali Prestasi (18 Achievements)
 * 2. Kustomisasi Avatar & Bingkai Profil Prestise (Avatars & Frames)
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

    // 1. Buka Modal Level & Pencapaian dari Beranda
    console.log("Opening Level & Pencapaian modal from Beranda...");
    await page.locator('#btn-achievements').click();
    await page.waitForSelector('.flow-page[aria-label="Level & Pencapaian"]', { timeout: 5000 });

    const achCards = page.locator('.flow-page[aria-label="Level & Pencapaian"] .achievement-card');
    const achCount = await achCards.count();
    console.log("Achievement cards count in grid:", achCount);
    if (achCount < 10) {
        throw new Error(`Minimal harus ada 10 kartu medali prestasi (aktual: ${achCount})!`);
    }
    await page.locator('.back-button').click();
    await page.waitForTimeout(300);

    // 2. Buka Kustomisasi Avatar & Bingkai dari Profil Saya
    console.log("Navigating to Lainnya -> Profil Saya...");
    await page.click('button.nav-item[data-tab="riset"]');
    await page.waitForTimeout(400);
    await page.locator('#view-riset button.shortcut:has-text("Profil Saya")').click();
    await page.waitForSelector('.flow-page[aria-label="Profil Saya"]', { timeout: 5000 });

    console.log("Switching to Pencapaian subtab...");
    await page.locator('.profile-subtabs-nav button:has-text("Pencapaian")').click();
    await page.waitForTimeout(400);

    console.log("Opening Kustomisasi Avatar & Bingkai Profil...");
    await page.locator('button:has-text("Kustomisasi Avatar & Bingkai Profil")').click();
    await page.waitForSelector('.flow-page[aria-label="Kustomisasi Avatar & Bingkai"]', { timeout: 5000 });

    const cosmeticRows = page.locator('.flow-page[aria-label="Kustomisasi Avatar & Bingkai"] .data-row');
    const rowCount = await cosmeticRows.count();
    console.log("Cosmetic items count (Avatars + Frames):", rowCount);
    if (rowCount < 8) {
        throw new Error(`Harus ada minimal 8 item kosmetik (aktual: ${rowCount})!`);
    }

    // Klik pasang salah satu avatar
    await cosmeticRows.first().click();
    await page.waitForTimeout(400);

    // Tangkap screenshot bukti Tahap E3
    const shotPath = path.join(evidenceDir, 'TahapE3-achievements-avatar.png');
    await page.screenshot({ path: shotPath });
    console.log("Captured screenshot:", shotPath);

    await browser.close();
    console.log("=== TAHAP E3 VERIFICATION SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
