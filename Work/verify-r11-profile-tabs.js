/**
 * Automated Playwright browser verification for R11 (Profil Ringkasan + Tab Kinerja/Keuangan/Pencapaian).
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

    console.log("Opening Profil Saya modal...");
    await page.locator('#btn-profile').click();
    await page.waitForSelector('.flow-page[aria-label="Profil Saya"]', { timeout: 5000 });

    // 1. Verifikasi Ringkasan Singkat di Atas
    const heroTitle = await page.locator('.flow-page[aria-label="Profil Saya"] .wealth-hero h2').textContent();
    const heroNetWorth = await page.locator('.flow-page[aria-label="Profil Saya"] .wealth-hero .metric-hero').textContent();
    console.log(`Hero summary: name='${heroTitle}', netWorth='${heroNetWorth}'`);
    if (!heroTitle || !heroNetWorth.includes("Rp")) {
        throw new Error("Ringkasan singkat di atas harus memuat nama trader dan kekayaan bersih!");
    }

    // 2. Verifikasi 3 Tab Navigasi
    const nav = page.locator('.profile-subtabs-nav');
    const tabKinerja = nav.locator('button:has-text("Kinerja")');
    const tabKeuangan = nav.locator('button:has-text("Keuangan")');
    const tabPencapaian = nav.locator('button:has-text("Pencapaian")');

    if (await tabKinerja.count() !== 1 || await tabKeuangan.count() !== 1 || await tabPencapaian.count() !== 1) {
        throw new Error("Profil Saya harus memiliki 3 tab: Kinerja, Keuangan, Pencapaian!");
    }

    // Cek isi Tab 1: Kinerja (aktif secara default)
    const kinerjaTitle = await page.locator('.flow-page .card-title:has-text("Statistik Trading & Return")').count();
    console.log("Kinerja card count:", kinerjaTitle);
    if (kinerjaTitle !== 1) throw new Error("Tab Kinerja harus memuat Statistik Trading & Return!");

    // Klik Tab 2: Keuangan
    console.log("Clicking Keuangan tab...");
    await tabKeuangan.click();
    await page.waitForTimeout(400);
    const keuanganTitle = await page.locator('.flow-page .card-title:has-text("Portofolio & Struktur Aset/Utang")').count();
    console.log("Keuangan card count:", keuanganTitle);
    if (keuanganTitle !== 1) throw new Error("Tab Keuangan harus memuat Struktur Aset & Utang!");

    // Klik Tab 3: Pencapaian
    console.log("Clicking Pencapaian tab...");
    await tabPencapaian.click();
    await page.waitForTimeout(400);
    const pencapaianTitle = await page.locator('.flow-page .card-title:has-text("Pencapaian & Peringkat Liga")').count();
    console.log("Pencapaian card count:", pencapaianTitle);
    if (pencapaianTitle !== 1) throw new Error("Tab Pencapaian harus memuat Pencapaian & Peringkat Liga!");

    // Tangkap screenshot bukti R11
    const shotPath = path.join(evidenceDir, 'R11-profile-3tabs.png');
    await page.screenshot({ path: shotPath });
    console.log("Captured screenshot:", shotPath);

    await browser.close();
    console.log("=== R11 PROFIL 3-TAB VERIFICATION SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
