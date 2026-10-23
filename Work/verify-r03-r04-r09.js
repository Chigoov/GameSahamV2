/**
 * Automated Playwright browser verification for R03 (Profil Risiko), R04 (Pusat Misi 3-Tab), and R09 (Terminologi Emiten).
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

    // =========================================================================
    // 1. Verifikasi R04: Pusat Misi Tiga Tab (Harian, Mingguan, Karier)
    // =========================================================================
    console.log("[1] Testing R04: Pusat Misi 3-Tab...");
    await page.click('#btn-missions');
    await page.waitForSelector('.flow-page[aria-label="Pusat Misi & Target"]', { timeout: 5000 });

    const nav = page.locator('.mission-tabs-nav');
    const tabDaily = nav.locator('button:has-text("Harian")');
    const tabWeekly = nav.locator('button:has-text("Mingguan")');
    const tabCareer = nav.getByRole('button', { name: /Target karier/i });

    if (await tabDaily.count() !== 1 || await tabWeekly.count() !== 1 || await tabCareer.count() !== 1) {
        throw new Error("Semua 3 tab misi (Harian, Mingguan, Karier) harus ada di Pusat Misi!");
    }

    // Klik tab Karier
    console.log("Checking Career Mission tab...");
    await tabCareer.click();
    await page.waitForTimeout(500);

    const careerCards = page.locator('.mission-content-box .mission-card');
    const cCount = await careerCards.count();
    console.log(`Career missions count: ${cCount}`);
    if (cCount < 1) throw new Error("Target misi karier harus tampil di tab Karier!");

    const progress = careerCards.first().locator('.data-row').filter({hasText:'Progres'});
    if (await progress.count() !== 1 || !(await progress.locator('strong').innerText()).trim()) throw new Error('Progres misi dari state harus ditampilkan tanpa persentase buatan');

    // Tangkap screenshot Pusat Misi
    const missionShot = path.join(evidenceDir, 'R04-mission-3tabs.png');
    await page.screenshot({ path: missionShot });
    console.log("Captured mission screenshot:", missionShot);

    await page.click('.flow-page button.back-button');
    await page.waitForTimeout(500);

    // =========================================================================
    // 2. Verifikasi R03: Profil Risiko Berbasis Perilaku (5 Indikator, Tanpa Kuis)
    // =========================================================================
    console.log("[2] Testing R03: Profil Risiko & 5 Indikator...");
    await page.locator('[data-tab="beranda"]').click();
    await page.locator('.home-secondary > summary').click();
    await page.click('#btn-home-archetype');
    await page.waitForSelector('.flow-page[aria-label="Gaya Investor (Archetype)"]', { timeout: 5000 });

    const heroTitle = await page.locator('.flow-page[aria-label="Gaya Investor (Archetype)"] .wealth-hero .eyebrow').textContent();
    console.log("Archetype hero title:", heroTitle);
    if (!heroTitle.includes("Gaya Investasimu Sedang Terbentuk") && !heroTitle.includes("Konservatif") && !heroTitle.includes("Seimbang") && !heroTitle.includes("Agresif")) {
        throw new Error(`Title gaya investor tidak valid: '${heroTitle}'`);
    }

    // Pastikan 5 indikator tampil
    const indicatorRows = page.locator('.flow-page section.card').filter({has: page.getByRole('heading', {name: 'Indikator yang diamati', exact: true})}).locator('.data-row');
    const indCount = await indicatorRows.count();
    console.log(`Observed indicators count: ${indCount}`);
    if (indCount !== 5) throw new Error(`Harus ada tepat 5 indikator yang diamati, aktual: ${indCount}`);

    const riskShot = path.join(evidenceDir, 'R03-risk-archetype-5indicators.png');
    await page.screenshot({ path: riskShot });
    console.log("Captured archetype screenshot:", riskShot);

    await page.click('.flow-page button.back-button');
    await page.waitForTimeout(500);

    // =========================================================================
    // 3. Verifikasi R09: Terminologi Emiten pada Konteks Saham
    // =========================================================================
    console.log("[3] Testing R09: Terminologi Emiten...");
    await page.click('button.nav-item[data-tab="pasar"]');
    await page.waitForSelector('#stock-list-container', { timeout: 5000 });

    const searchLabel = await page.locator('label[for="search-stock-input"]').textContent();
    const filterListLabel = await page.locator('label[for="filter-list-select"]').textContent();
    const statusText = await page.locator('#stock-list-status').textContent();

    console.log(`Labels: search='${searchLabel}', list='${filterListLabel}', status='${statusText}'`);
    if (!searchLabel.includes("emiten") || !filterListLabel.includes("emiten") || !statusText.includes("emiten")) {
        throw new Error("Label dan teks pada konteks saham harus menggunakan istilah 'emiten'!");
    }

    const emitenShot = path.join(evidenceDir, 'R09-emiten-terminology.png');
    await page.screenshot({ path: emitenShot });
    console.log("Captured emiten screenshot:", emitenShot);

    await browser.close();
    console.log("=== R03, R04, R09 TESTS SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
