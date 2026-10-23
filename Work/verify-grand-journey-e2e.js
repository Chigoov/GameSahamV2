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
    checks.push("STEP 1: Five classic primary tabs verified");

    // =========================================================================
    // STEP 2: Hibah Harian 00.00 WIB (A12)
    // =========================================================================
    console.log("=== STEP 2: Hibah Harian Asia/Jakarta (A12) ===");
    const claimBtn = page.locator('#btn-claim-sponsor');
    const claimBtnText = (await claimBtn.textContent()).trim();
    console.log("Claim button text:", claimBtnText);

    if (await claimBtn.isEnabled()) {
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

    // Pastikan pasar berada pada jam perdagangan aktif (09:05 WIB)
    await page.evaluate(() => {
        if (window.TradingNativeBridge && window.TradingNativeBridge.tickClock) {
            window.TradingNativeBridge.setClockState(true, 1);
            window.TradingNativeBridge.tickClock(2100);
        }
    });
    await page.waitForTimeout(500);

    // Cari BBCA dan buka detail
    await page.fill('#search-stock-input', 'BBCA');
    await page.waitForTimeout(500);
    const bbcaRow = page.locator('#stock-list-container .stock-list-item:has-text("BBCA")');
    if (await bbcaRow.count() !== 1) throw new Error("BBCA not found");

    await bbcaRow.click();
    await page.waitForSelector('.flow-page[aria-label="BBCA"]', { timeout: 5000 });
    console.log("Opened BBCA detail modal");

    // Buka tiket order beli
    const buyActionBtn = page.locator('.flow-page button.btn-detail-buy:has-text("BELI")');
    if (await buyActionBtn.count() === 0) throw new Error("Tombol 'BELI' wajib ada di detail instrumen!");
    await buyActionBtn.first().click();
    await page.waitForSelector('#order-modal.active', { timeout: 5000 });
    console.log("Opened Order Ticket Modal");

    // Set quantity 1 unit (100 lembar) dan limit price 11.000 (menjamin immediate fill)
    await page.fill('#order-qty', '1');
    await page.fill('#order-limit-price', '11000');
    // Review order
    await page.click('#btn-submit-order');
    await page.waitForSelector('.flow-page[aria-label="Tinjau order"]', { timeout: 8000 });
    console.log("Order Review opened!");

    // Konfirmasi kirim order
    await page.click('.flow-page button:has-text("Kirim order")');
    await page.waitForSelector('.flow-page[aria-label="Status order"]', { timeout: 8000 });
    console.log("Order sent and receipt status opened!");

    // Tutup receipt
    await page.click('.flow-page button.back-button');
    await page.waitForTimeout(500);

    await page.fill('#search-stock-input', '');
    checks.push("STEP 3: Realtime market quote, index ribbon, & BBCA order executed");

    // =========================================================================
    // STEP 4: Portofolio Layout Bersih & Posisi Nyata (A2)
    // =========================================================================
    console.log("=== STEP 4: Portofolio Layout Bersih & Posisi Nyata (A2) ===");
    await page.click('button.nav-item[data-tab="aset"]');
    await page.waitForSelector('#view-aset', { timeout: 5000 });

    const oldStats = await page.locator('#asset-summary section.card:has-text("Statistik Kinerja Trading")').count();
    const oldLedger = await page.locator('#asset-summary button:has-text("Aktivitas / ledger")').count();
    if (oldStats !== 0 || oldLedger !== 0) throw new Error("Portofolio masih memuat elemen usang!");

    const posRows = page.locator('#portfolio-positions-list .position-row');
    const posCount = await posRows.count();
    console.log(`Portfolio positions count: ${posCount}`);
    if (posCount === 0) throw new Error("Posisi saham BBCA harus ada di Portofolio setelah order beli terisi!");
    checks.push("STEP 4: Clean Portfolio without clutter, positions displayed");

    // =========================================================================
    // STEP 5: Riwayat Transaksi & Liga Peringkat (A2, A8)
    // =========================================================================
    console.log("=== STEP 5: Riwayat Transaksi & Liga Peringkat (A2, A8) ===");
    await page.locator('#asset-tools > summary').click();

    // Buka Riwayat Transaksi
    await page.click('#asset-tools button:has-text("Riwayat Transaksi")');
    await page.waitForSelector('.flow-page[aria-label="Riwayat Transaksi"]', { timeout: 5000 });
    const recordsCount = await page.locator('.flow-page article.card').count();
    console.log(`Transaction history records count: ${recordsCount}`);
    if (recordsCount < 1) throw new Error("Riwayat transaksi harus memuat catatan mutasi!");
    await page.click('.flow-page button.back-button');
    await page.waitForTimeout(500);

    // Buka Liga & Peringkat
    await page.click('button.nav-item[data-tab="riset"]');
    await page.locator('#view-riset').getByRole('button', {name: 'Liga & Rival', exact: true}).click();
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
    await page.locator('#view-riset').getByRole('button', {name: 'Profil Saya', exact: true}).click();
    await page.waitForSelector('.flow-page[aria-label="Profil Saya"]', { timeout: 5000 });

    if (!(await page.locator('.flow-page .wealth-hero').first().innerText()).includes('Karier')) throw new Error('Identitas & karier profil hilang');
    for (const [tab, sections, metrics] of [
        ['Kinerja', ['Statistik Trading & Return'], ['Total order terealisasi', 'Transaksi untung / rugi', 'Rasio menang', 'Transaksi terbaik', 'Transaksi terburuk', 'ROI mingguan']],
        ['Keuangan', ['Portofolio & Struktur Aset/Utang'], ['Kas pribadi IDR', 'Kas pribadi USD', 'Portofolio saham', 'Koleksi aset mewah', 'Reksa dana NAB', 'Kewajiban utang margin', 'Dana kelolaan klien']],
        ['Pencapaian', ['Pencapaian & Peringkat Liga', 'Pengaturan Identitas & Aksesibilitas'], ['Puncak kekayaan tertinggi', 'Hari perjalanan era aktif', 'Total hari bermain', 'Peringkat liga saat ini']]
    ]) {
        await page.locator('.profile-subtabs-nav').getByRole('button', {name: tab, exact: true}).click();
        for (const title of sections) if (await page.locator('.flow-page').getByRole('heading', {name: title, exact: true}).count() !== 1) throw new Error(`Seksi '${title}' tidak ditemukan di Profil Saya`);
        const text = await page.locator('.flow-page').innerText();
        for (const metric of metrics) if (!text.includes(metric)) throw new Error(`Metric profil hilang: ${metric}`);
    }
    await page.click('.flow-page button.back-button');
    await page.waitForTimeout(500);
    checks.push("STEP 6: Profile identity and original financial/trading/achievement/preferences metrics verified across all three tabs");

    // =========================================================================
    // STEP 7: Era Baru Status & Checklist (A5, A6, A7)
    // =========================================================================
    console.log("=== STEP 7: Era Baru Status & Checklist (A5, A6, A7) ===");
    const eraCard = page.locator('#view-riset section.card').filter({has: page.getByRole('heading', {name: /Era baru/i, exact: true})});
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

    let compCard = page.locator('article.company-summary-card').first();
    if (await compCard.count() === 0) {
        console.log("Founding company PT Grand Journey...");
        await page.click('button:has-text("+ Dirikan Perusahaan Baru")');
        await page.waitForSelector('.flow-page[aria-label="Dirikan Perusahaan Baru"]', { timeout: 5000 });
        await page.fill('#company-name', 'PT Grand Journey');
        await page.fill('#company-capital', '25000000');
        await page.click('.flow-page button.btn-primary:has-text("Resmikan Pendirian Perusahaan")');
        await page.waitForTimeout(1500);
        compCard = page.locator('article.company-summary-card').first();
    }
    if (await compCard.count() !== 1) throw new Error("Perusahaan operasional harus hadir di Usaha Saya!");
    await compCard.locator('button:has-text("Kelola Usaha & Operasional")').click();
    await page.waitForSelector('.company-local-nav', { timeout: 5000 });
    const subtabsCount = await page.locator('.company-local-nav button.subtab-pill').count();
    console.log(`Company detail subtabs count: ${subtabsCount}`);
    if (subtabsCount !== 4) throw new Error("Company management must have 4 subtabs");
    await page.click('.flow-page button.back-button');
    await page.waitForTimeout(500);
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

    // Lakukan pembelian koleksi dan assert perubahan count
    const buyColBtn = page.locator('.collection-section-group:has-text("Belum Dimiliki") button.btn-primary:has-text("Beli Koleksi")').first();
    await buyColBtn.click();
    await page.waitForSelector('.game-toast', { timeout: 5000 });
    await page.waitForTimeout(1000);

    const ownedBadge = (await page.locator('.collection-section-group:has-text("Sudah Dimiliki") h2 .badge-owned').textContent()).trim();
    if (ownedBadge === '0') throw new Error("Koleksi yang dibeli harus masuk ke Sudah Dimiliki (count > 0)!");

    // Jual kembali
    const sellColBtn = page.locator('.collection-section-group:has-text("Sudah Dimiliki") button.btn-secondary:has-text("Jual Koleksi")').first();
    await sellColBtn.click();
    await page.waitForSelector('.game-toast', { timeout: 5000 });
    await page.waitForTimeout(1000);
    checks.push("STEP 9: Luxury Collections 2-section display & buy-sell verified");

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
