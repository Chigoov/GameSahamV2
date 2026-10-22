/**
 * Automated Playwright browser verification for A11 Market Redesign & Realtime Integration.
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

    // 1. Uji Viewport 438x627 (Xiaomi Landscape / Tablet Ratio Portrait)
    const context = await browser.newContext({ viewport: { width: 438, height: 627 } });
    const page = await context.newPage();
    page.on('console', msg => console.log('PAGE LOG:', msg.text()));

    await page.goto(`http://127.0.0.1:${port}/`);
    await page.waitForSelector('nav.bottom-nav', { timeout: 10000 });

    console.log("Navigating to Pasar tab...");
    await page.click('button.nav-item[data-tab="pasar"]');
    await page.waitForSelector('#stock-list-container', { timeout: 5000 });

    // 1. Verifikasi Header Pasar Ringkas & Sesi
    const sessionEyebrow = await page.locator('#market-session-eyebrow').textContent();
    console.log("Market session eyebrow:", sessionEyebrow);
    if (!sessionEyebrow || !sessionEyebrow.includes('Bursa')) {
        throw new Error(`Eyebrow sesi pasar belum valid: ${sessionEyebrow}`);
    }

    // 2. Verifikasi Indeks Ribbon Kompak
    const idxCards = page.locator('#indices-row .index-card');
    const idxCount = await idxCards.count();
    console.log(`Indices count: ${idxCount}`);
    if (idxCount < 3) throw new Error(`Indeks harus ada minimal 3, aktual: ${idxCount}`);

    const ihsgCard = page.locator('#indices-row .index-card[data-ticker="IHSG"]');
    const lq45Card = page.locator('#indices-row .index-card[data-ticker="LQ45"]');
    const spCard = page.locator('#indices-row .index-card[data-ticker="SP500"]');
    if (await ihsgCard.count() !== 1 || await lq45Card.count() !== 1 || await spCard.count() !== 1) {
        throw new Error("Kartu IHSG, LQ45, dan SP500 harus hadir di ribbon indeks!");
    }

    // 3. Verifikasi Daftar Saham (100 Emiten) & Touch Target >= 48dp
    const stockItems = page.locator('#stock-list-container .stock-list-item');
    const initialStockCount = await stockItems.count();
    console.log(`Initial stock count: ${initialStockCount}`);
    if (initialStockCount !== 100) throw new Error(`Harus ada 100 saham aktif, aktual: ${initialStockCount}`);

    // Bounding box height check
    const firstStockBox = await stockItems.first().boundingBox();
    console.log(`First stock row height: ${firstStockBox.height}px (Target >= 48px)`);
    if (firstStockBox.height < 48) throw new Error(`Tinggi baris saham ${firstStockBox.height}px < 48px target sentuh!`);

    // 4. Uji Pencarian Ticker / Nama
    console.log("Testing search query 'BBCA'...");
    await page.fill('#search-stock-input', 'BBCA');
    await page.waitForTimeout(500);
    const searchResultCount = await page.locator('#stock-list-container .stock-list-item').count();
    console.log(`Search result count for 'BBCA': ${searchResultCount}`);
    if (searchResultCount !== 1) throw new Error(`Pencarian BBCA harus menghasilkan tepat 1 saham, aktual: ${searchResultCount}`);
    await page.fill('#search-stock-input', '');
    await page.waitForTimeout(500);

    // 5. Uji Filter Bursa (US vs IDX)
    console.log("Testing filter exchange 'US'...");
    await page.selectOption('#filter-exchange-select', 'US');
    await page.waitForTimeout(500);
    const usCount = await page.locator('#stock-list-container .stock-list-item').count();
    console.log(`US stocks count: ${usCount}`);
    if (usCount !== 50) throw new Error(`Saham bursa US harus tepat 50, aktual: ${usCount}`);

    console.log("Testing filter exchange 'IDX'...");
    await page.selectOption('#filter-exchange-select', 'IDX');
    await page.waitForTimeout(500);
    const idxStockCount = await page.locator('#stock-list-container .stock-list-item').count();
    console.log(`IDX stocks count: ${idxStockCount}`);
    if (idxStockCount !== 50) throw new Error(`Saham bursa IDX harus tepat 50, aktual: ${idxStockCount}`);

    await page.selectOption('#filter-exchange-select', '');
    await page.waitForTimeout(500);

    // 6. Uji Tombol Watchlist Bintang Langsung di Baris
    console.log("Testing Watchlist Star button on row...");
    const firstRow = page.locator('#stock-list-container .stock-list-item').first();
    const starBtn = firstRow.locator('.watchlist-star-btn');
    await starBtn.click();
    await page.waitForTimeout(500);
    const starText = await starBtn.textContent();
    console.log(`Star text after click: ${starText.trim()}`);
    if (starText.trim() !== '★') throw new Error("Bintang watchlist harus menjadi ★ aktif setelah diklik!");

    // Filter watchlist
    await page.selectOption('#filter-list-select', 'watchlist');
    await page.waitForTimeout(500);
    const watchedCount = await page.locator('#stock-list-container .stock-list-item').count();
    console.log(`Watchlist count: ${watchedCount}`);
    if (watchedCount < 1) throw new Error("Saham yang dipantau harus tampil di filter watchlist!");
    await page.selectOption('#filter-list-select', 'all');
    await page.waitForTimeout(500);

    // 7. Ambil Screenshot Bukti A11 pada 438x627
    const shot438 = path.join(evidenceDir, 'A11-market-redesign-438x627.png');
    await page.screenshot({ path: shot438 });
    console.log("Captured screenshot 438x627:", shot438);

    // 8. Ambil Screenshot Bukti pada 360x640 (Mobile Standar)
    const ctx360 = await browser.newContext({ viewport: { width: 360, height: 640 } });
    const page360 = await ctx360.newPage();
    await page360.goto(`http://127.0.0.1:${port}/`);
    await page360.click('button.nav-item[data-tab="pasar"]');
    await page360.waitForSelector('#stock-list-container', { timeout: 5000 });
    const shot360 = path.join(evidenceDir, 'A11-market-redesign-360x640.png');
    await page360.screenshot({ path: shot360 });
    console.log("Captured screenshot 360x640:", shot360);
    await ctx360.close();

    await browser.close();
    console.log("=== BROWSER PLAYWRIGHT A11 MARKET REDESIGN SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
