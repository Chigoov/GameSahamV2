/**
 * Automated Playwright browser verification for A9 Business (Bisnis Operasional).
 */
const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

async function run() {
    const port = process.env.QA_PORT || '8794';
    const evidenceDir = path.resolve(__dirname, '../Artifacts/8-Anotasi-2026-10-22/run-01/evidence');
    if (!fs.existsSync(evidenceDir)) fs.mkdirSync(evidenceDir, { recursive: true });

    console.log(`Connecting to http://127.0.0.1:${port}/...`);
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext({ viewport: { width: 438, height: 627 } });
    const page = await context.newPage();
    page.on('console', msg => console.log('PAGE LOG:', msg.text()));

    await page.goto(`http://127.0.0.1:${port}/`);
    await page.waitForSelector('nav.bottom-nav', { timeout: 10000 });

    // 1. Pindah ke tab Bisnis & Firma
    console.log("Navigating to Bisnis & Firma tab...");
    await page.click('button.nav-item[data-tab="firma"]');
    await page.waitForSelector('#firma-subtab-content', { timeout: 5000 });

    // 2. Dirikan Perusahaan Baru
    console.log("Opening Found Company modal...");
    await page.click('button:has-text("+ Dirikan Perusahaan Baru")');
    await page.waitForSelector('.flow-page[aria-label="Dirikan Perusahaan Baru"]', { timeout: 5000 });

    const nameInput = page.locator('#company-name');
    await nameInput.fill('PT Nusantara Tech');

    const capInput = page.locator('#company-capital');
    await capInput.fill('35000000');

    console.log("Submitting company establishment...");
    await page.click('.flow-page button.btn-primary:has-text("Resmikan Pendirian Perusahaan")');
    await page.waitForTimeout(1500);

    // 3. Periksa Kartu Ringkas Usaha Saya
    console.log("Checking company card in Usaha Saya...");
    const compCard = page.locator('article.company-summary-card:has-text("PT Nusantara Tech")');
    if (await compCard.count() !== 1) throw new Error("Kartu 'PT Nusantara Tech' harus tampil di Usaha Saya!");

    const btnManage = compCard.locator('button:has-text("Kelola Usaha & Operasional")');
    if (await btnManage.count() !== 1) throw new Error("Tombol 'Kelola Usaha & Operasional ↗' harus ada!");

    // 4. Buka Manajemen Usaha Khusus (4-Subtab Navigasi)
    console.log("Opening Company Detail Management modal...");
    await btnManage.click();
    await page.waitForSelector('.flow-page[aria-label="PT Nusantara Tech"]', { timeout: 5000 });

    // Verifikasi keberadaan 4 subtab
    const nav = page.locator('.company-local-nav');
    const tabRingkasan = nav.locator('button:has-text("Ringkasan")');
    const tabTim = nav.locator('button:has-text("Tim & SDM")');
    const tabFasilitas = nav.locator('button:has-text("Fasilitas & Moral")');
    const tabKeuangan = nav.locator('button:has-text("Keuangan")');

    if (await tabRingkasan.count() !== 1 || await tabTim.count() !== 1 ||
        await tabFasilitas.count() !== 1 || await tabKeuangan.count() !== 1) {
        throw new Error("Semua 4 subtab lokal harus tersedia di manajemen perusahaan!");
    }

    // Periksa isi Subtab 1: Ringkasan
    const summaryCard = page.locator('.company-subtab-box h2:has-text("Ringkasan Operasional")');
    if (await summaryCard.count() !== 1) throw new Error("Seksi Ringkasan Operasional harus tampil!");

    // Klik Subtab 2: Tim & SDM
    console.log("Testing Subtab Tim & SDM...");
    await tabTim.click();
    await page.waitForTimeout(500);
    const timCard = page.locator('.company-subtab-box h2:has-text("Divisi SDM")');
    if (await timCard.count() !== 1) throw new Error("Seksi Divisi SDM harus tampil!");

    // Klik Subtab 3: Fasilitas & Moral
    console.log("Testing Subtab Fasilitas & Moral...");
    await tabFasilitas.click();
    await page.waitForTimeout(500);
    const fasCard = page.locator('.company-subtab-box h2:has-text("Kebijakan Kerja, Fasilitas & Moral")');
    if (await fasCard.count() !== 1) throw new Error("Seksi Fasilitas & Moral harus tampil!");

    // Klik Subtab 4: Keuangan & Laporan
    console.log("Testing Subtab Keuangan & Laporan...");
    await tabKeuangan.click();
    await page.waitForTimeout(500);
    const finCard = page.locator('.company-subtab-box h2:has-text("Keuangan, Modal & Laporan Usaha")');
    if (await finCard.count() !== 1) throw new Error("Seksi Keuangan & Laporan harus tampil!");

    // Tangkap screenshot bukti A9
    const bizScreenshot = path.join(evidenceDir, 'A9-business-management.png');
    await page.screenshot({ path: bizScreenshot });
    console.log("Captured Business Management screenshot:", bizScreenshot);

    await browser.close();
    console.log("=== BROWSER PLAYWRIGHT A9 BUSINESS SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
