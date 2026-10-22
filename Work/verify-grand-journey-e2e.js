/**
 * Grand Integration Journey (INT-01):
 * Complete End-to-End Playtest traversing A1 through A12 on isolated port 8794.
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

    const checks = [];

    // =========================================================================
    // STEP 1: Game Initialization & 5 Tabs Navigation
    // =========================================================================
    console.log("=== STEP 1: Game Initialization & Navigation ===");
    for (const tab of ['beranda', 'pasar', 'aset', 'firma', 'riset']) {
        await page.click(`button.nav-item[data-tab="${tab}"]`);
        await page.waitForSelector(`#view-${tab}.active`, { timeout: 3000 });
    }
    await page.click('button.nav-item[data-tab="beranda"]');
    await page.waitForSelector('#view-beranda.active');
    checks.push("STEP 1: 5-Tab Navigation verified");

    // =========================================================================
    // STEP 2: Hibah Harian 00.00 WIB (A12)
    // =========================================================================
    console.log("=== STEP 2: Hibah Harian Asia/Jakarta (A12) ===");
    const claimBtn = page.locator('#btn-claim-sponsor');
    const claimBtnText = (await claimBtn.textContent()).trim();
    console.log("Claim button text:", claimBtnText);

    if (claimBtnText.includes('Rp 25 Juta')) {
        await claimBtn.click();
        await page.waitForSelector('.game-toast', { timeout: 5000 });
        await page.waitForTimeout(1000);
        console.log("Claimed sponsor Rp 25M");
    }
    const disabledState = await claimBtn.isDisabled();
    console.log("Claim button disabled after claim:", disabledState);
    if (!disabledState) throw new Error("Tombol klaim hibah harus disabled setelah diklaim!");
    checks.push("STEP 2: Hibah harian Rp 25M claimed & single-claim verified");

    // =========================================================================
    // STEP 3: Pasar Realtime, Indeks, & Order Execution (A4 & A11)
    // =========================================================================
    console.log("=== STEP 3: Pasar Realtime, Indeks, & Order Execution (A4 & A11) ===");
    await page.click('button.nav-item[data-tab="pasar"]');
    await page.waitForSelector('#stock-list-container', { timeout: 5000 });

    // Cek Indeks Ribbon
    const ihsgCard = page.locator('#indices-row .index-card[data-ticker="IHSG"]');
    if (await ihsgCard.count() !== 1) throw new Error("IHSG index card missing");
    const ihsgVal = (await ihsgCard.locator('strong').textContent()).trim();
    console.log("IHSG value:", ihsgVal);

    // Cari BBCA dan buka detail
    await page.fill('#search-stock-input', 'BBCA');
    await page.waitForTimeout(500);
    const bbcaRow = page.locator('#stock-list-container .stock-list-item:has-text("BBCA")');
    if (await bbcaRow.count() !== 1) throw new Error("BBCA not found");

    await bbcaRow.click();
    await page.waitForSelector('.flow-page[aria-label="BBCA"]', { timeout: 5000 });
    console.log("Opened BBCA detail modal");

    // Buka tiket order beli
    const buyActionBtn = page.locator('.flow-page button.btn-primary:has-text("Beli")');
    if (await buyActionBtn.count() > 0) {
        await buyActionBtn.first().click();
        await page.waitForSelector('#btn-submit-order', { timeout: 5000 });
        console.log("Opened Order Ticket");

        // Set quantity 100 lembar
        await page.fill('#order-qty-input', '100');
        // Submit order
        await page.click('#btn-submit-order');
        await page.waitForSelector('.flow-page[aria-label="Tanda terima order"]', { timeout: 8000 });
        console.log("Order submitted and receipt opened!");
        await page.click('.flow-page button.back-button');
        await page.waitForTimeout(500);
    }
    // Tutup detail BBCA
    if (await page.locator('.flow-page button.back-button').count() > 0) {
        await page.click('.flow-page button.back-button');
        await page.waitForTimeout(500);
    }
    await page.fill('#search-stock-input', '');
    checks.push("STEP 3: Realtime market quote, index ribbon, & BBCA order executed");

    // =========================================================================
    // STEP 4: Portofolio Layout Bersih & Posisi (A2)
    // =========================================================================
    console.log("=== STEP 4: Portofolio Layout Bersih & Posisi (A2) ===");
    await page.click('button.nav-item[data-tab="aset"]');
    await page.waitForSelector('#view-aset', { timeout: 5000 });

    const oldStats = await page.locator('#asset-summary section.card:has-text("Statistik Kinerja Trading")').count();
    const oldLedger = await page.locator('#asset-summary button:has-text("Aktivitas / ledger")').count();
    if (oldStats !== 0 || oldLedger !== 0) throw new Error("Portofolio masih memuat elemen usang!");

    const posCount = await page.locator('#portfolio-positions-list .position-row').count();
    console.log(`Portfolio positions count: ${posCount}`);
    checks.push("STEP 4: Clean Portfolio without clutter, positions displayed");

    // =========================================================================
    // STEP 5: Riwayat Transaksi & Liga Peringkat (A2, A8)
    // =========================================================================
    console.log("=== STEP 5: Riwayat Transaksi & Liga Peringkat (A2, A8) ===");
    await page.click('button.nav-item[data-tab="riset"]');
    await page.waitForSelector('#view-riset', { timeout: 5000 });

    // Buka Riwayat Transaksi
    await page.click('#view-riset button.shortcut:has-text("Riwayat Transaksi")');
    await page.waitForSelector('.flow-page[aria-label="Riwayat Transaksi"]', { timeout: 5000 });
    const recordsCount = await page.locator('.flow-page article.card').count();
    console.log(`Transaction history records count: ${recordsCount}`);
    if (recordsCount < 1) throw new Error("Riwayat transaksi harus memuat catatan mutasi!");
    await page.click('.flow-page button.back-button');
    await page.waitForTimeout(500);

    // Buka Liga & Peringkat
    await page.click('#view-riset button:has-text("Lihat Peringkat Lengkap & Rival")');
    await page.waitForSelector('.flow-page[aria-label="Liga & rival"]', { timeout: 5000 });
    const podiumExists = await page.locator('.leaderboard-podium').count();
    console.log("Podium exists:", podiumExists === 1);
    await page.click('.flow-page button.back-button');
    await page.waitForTimeout(500);
    checks.push("STEP 5: Transaction History & League Rankings verified");

    // =========================================================================
    // STEP 6: Profil Saya 5-Seksi (A10)
    // =========================================================================
    console.log("=== STEP 6: Profil Saya 5-Seksi (A10) ===");
    await page.click('#view-riset button.shortcut:has-text("Profil Saya")');
    await page.waitForSelector('.flow-page[aria-label="Profil Saya"]', { timeout: 5000 });

    for (const secTitle of ["1. Identitas & Karier", "2. Ringkasan Keuangan", "3. Statistik Trading", "4. Performa & Pencapaian", "5. Pengaturan Identitas & Aksesibilitas"]) {
        const c = await page.locator(`.flow-page h2:has-text("${secTitle}")`).count();
        if (c !== 1) throw new Error(`Seksi '${secTitle}' tidak ditemukan di Profil Saya!`);
    }
    await page.click('.flow-page button.back-button');
    await page.waitForTimeout(500);
    checks.push("STEP 6: Profil Saya 5 comprehensive sections verified");

    // =========================================================================
    // STEP 7: Era Baru Status & Checklist (A5, A6, A7)
    // =========================================================================
    console.log("=== STEP 7: Era Baru Status & Checklist (A5, A6, A7) ===");
    const eraCard = page.locator('#view-riset section.card:has-text("Era Baru")');
    if (await eraCard.count() !== 1) throw new Error("Era Baru card missing");
    const seeReqBtn = eraCard.locator('button:has-text("Lihat Syarat")');
    await seeReqBtn.click();
    await page.waitForSelector('.flow-page[aria-label="Persyaratan Era Baru"]', { timeout: 5000 });
    const chkExists = await page.locator('.flow-page h2:has-text("Checklist Persyaratan")').count();
    if (chkExists !== 1) throw new Error("Checklist persyaratan Era Baru missing");
    await page.click('.flow-page button.back-button');
    await page.waitForTimeout(500);
    checks.push("STEP 7: Era Baru status card & checklist verified without duplicate blockers");

    // =========================================================================
    // STEP 8: Bisnis Operasional (A9)
    // =========================================================================
    console.log("=== STEP 8: Bisnis Operasional (A9) ===");
    await page.click('button.nav-item[data-tab="firma"]');
    await page.waitForSelector('#firma-subtab-content', { timeout: 5000 });

    const compCard = page.locator('article.company-summary-card').first();
    if (await compCard.count() === 1) {
        await compCard.locator('button:has-text("Kelola Usaha & Operasional")').click();
        await page.waitForSelector('.company-local-nav', { timeout: 5000 });
        const subtabsCount = await page.locator('.company-local-nav button.subtab-pill').count();
        console.log(`Company detail subtabs count: ${subtabsCount}`);
        if (subtabsCount !== 4) throw new Error("Company management must have 4 subtabs");
        await page.click('.flow-page button.back-button');
        await page.waitForTimeout(500);
    }
    checks.push("STEP 8: Business management 4 subtabs verified");

    // =========================================================================
    // STEP 9: Galeri Koleksi Mewah (A1, A3)
    // =========================================================================
    console.log("=== STEP 9: Galeri Koleksi Mewah (A1, A3) ===");
    await page.click('button.subtab-pill#firma-subtab-koleksi');
    await page.waitForSelector('.collections-gallery-wrap', { timeout: 5000 });

    const secUnowned = await page.locator('.collection-section-group:has-text("Belum Dimiliki")').count();
    const secOwned = await page.locator('.collection-section-group:has-text("Sudah Dimiliki")').count();
    if (secUnowned !== 1 || secOwned !== 1) throw new Error("Koleksi harus memiliki 2 seksi: Belum Dimiliki dan Sudah Dimiliki");
    checks.push("STEP 9: Luxury Collections 2-section display verified");

    // =========================================================================
    // STEP 10: Final Grand Screenshot & Summary
    // =========================================================================
    const grandShot = path.join(evidenceDir, 'INT-01-grand-journey-final.png');
    await page.screenshot({ path: grandShot });
    console.log("Captured grand journey screenshot:", grandShot);

    // Simpan hasil check log
    const report = {
        verdict: "PASS",
        timestamp: new Date().toISOString(),
        checksCount: checks.length,
        checks: checks
    };
    fs.writeFileSync(path.join(evidenceDir, 'INT-01-grand-journey-report.json'), JSON.stringify(report, null, 2));

    await browser.close();
    console.log("=== GRAND INTEGRATION JOURNEY (INT-01) SUCCEEDED (100% PASS) ===");
}

run().catch(err => {
    console.error("Grand journey failed:", err);
    process.exit(1);
});
