/**
 * Automated Playwright browser verification for R07 (% Harian) and R08 (Fundamental 4-Subtab).
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
    await page.waitForSelector('#stock-list-container .stock-list-item', { timeout: 5000 });

    // 1. Verifikasi Persentase Harian R07 pada baris saham
    console.log("[1] Testing R07: Daily % Change on stock rows...");
    const firstRow = page.locator('#stock-list-container .stock-list-item').first();
    const changePill = firstRow.locator('.change-pill');
    const changeText = (await changePill.textContent()).trim();
    console.log("First stock change pill:", changeText);
    if (!changeText.includes('%')) {
        throw new Error("Change pill harus memuat format persentase!");
    }

    // 2. Buka Detail Saham BBCA
    console.log("[2] Opening BBCA detail for R08 Fundamentals 4-Subtab...");
    await page.fill('#search-stock-input', 'BBCA');
    await page.waitForTimeout(500);
    const bbcaRow = page.locator('#stock-list-container .stock-list-item:has-text("BBCA")');
    await bbcaRow.click();
    await page.waitForSelector('#detail-fundamentals-container', { timeout: 5000 });

    // Verifikasi 4 Subtab Fundamental
    const fundContainer = page.locator('#detail-fundamentals-container');
    const tabRingkasan = fundContainer.locator('button.subtab-pill:has-text("Ringkasan")');
    const tabValuasi = fundContainer.locator('button.subtab-pill:has-text("Valuasi")');
    const tabKeuangan = fundContainer.locator('button.subtab-pill:has-text("Laporan Keuangan")');
    const tabPemegang = fundContainer.locator('button.subtab-pill:has-text("Pemegang Saham")');

    if (await tabRingkasan.count() !== 1 || await tabValuasi.count() !== 1 ||
        await tabKeuangan.count() !== 1 || await tabPemegang.count() !== 1) {
        throw new Error("Keempat subtab fundamental (Ringkasan, Valuasi, Keuangan, Pemegang Saham) harus lengkap!");
    }

    // Tab 1: Ringkasan (aktif default)
    const ringkasanText = await page.locator('#fund-content-box').textContent();
    console.log("Ringkasan text length:", ringkasanText.length);
    if (!ringkasanText.includes("Bank Central Asia")) throw new Error("Profil ringkasan BBCA tidak sesuai!");

    // Tab 2: Valuasi & Rasio
    console.log("Testing Valuasi & Rasio subtab...");
    await tabValuasi.click();
    await page.waitForTimeout(300);
    const valuasiContent = await page.locator('#fund-content-box').textContent();
    if (!valuasiContent.toLowerCase().includes("kapitalisasi pasar") || !valuasiContent.includes("PER") || !valuasiContent.includes("PBV") || !valuasiContent.includes("ROE")) {
        throw new Error("Valuasi & Rasio harus memuat Market Cap, PER, PBV, ROE!");
    }

    // Tab 3: Laporan Keuangan (Neraca, Laba Rugi, Arus Kas)
    console.log("Testing Laporan Keuangan subtab...");
    await tabKeuangan.click();
    await page.waitForTimeout(300);
    const keuanganContent = await page.locator('#fund-content-box').textContent();
    if (!keuanganContent.toLowerCase().includes("neraca keuangan") || !keuanganContent.toLowerCase().includes("laba bersih") || !keuanganContent.toLowerCase().includes("arus kas operasional")) {
        throw new Error("Laporan Keuangan harus memuat Neraca, Laba Rugi, dan Arus Kas!");
    }

    // Tab 4: Pemegang Saham (Free Float & Struktur)
    console.log("Testing Pemegang Saham subtab...");
    await tabPemegang.click();
    await page.waitForTimeout(300);
    const pemegangContent = await page.locator('#fund-content-box').textContent();
    if (!pemegangContent.toLowerCase().includes("saham beredar") || !pemegangContent.toLowerCase().includes("free float") || !pemegangContent.toLowerCase().includes("jumlah pemegang saham")) {
        throw new Error("Struktur Pemegang Saham harus memuat Saham Beredar, Free Float, dan Jumlah Investor!");
    }

    // Tangkap screenshot bukti R08
    const shotPath = path.join(evidenceDir, 'R08-fundamentals-4subtabs.png');
    await page.screenshot({ path: shotPath });
    console.log("Captured screenshot:", shotPath);

    await browser.close();
    console.log("=== R07 & R08 BROWSER VERIFICATION SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
