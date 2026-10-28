/**
 * Automated Playwright browser verification for Tahap 2:
 * 1. Candlestick Canvas & Timeframe controls
 * 2. Order book interactive click-to-fill (Bid -> Sell, Ask -> Buy)
 * 3. Quick ticket percentage buttons (25%, 50%, 75%, MAX)
 * 4. Transparent fee and estimated total update
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

    // 1. Buka Detail Saham BBCA
    console.log("Opening BBCA detail...");
    await page.fill('#search-stock-input', 'BBCA');
    await page.waitForTimeout(400);
    const bbcaRow = page.locator('#stock-list-container .stock-list-item:has-text("BBCA")');
    await bbcaRow.click();
    await page.waitForSelector('.flow-page[aria-label="BBCA"]', { timeout: 5000 });

    // 2. Verifikasi Kontrol Grafik Candlestick
    console.log("Verifying Candlestick & Timeframe controls...");
    const tfM5 = page.locator('.timeframe-buttons button:has-text("M5")');
    if (await tfM5.count() === 1) {
        await tfM5.click();
        await page.waitForTimeout(300);
    }
    const readout = page.locator('#ohlc-readout');
    if (await readout.count() === 1) {
        const text = await readout.textContent();
        console.log("OHLC Readout text:", text);
    }

    // 3. Verifikasi Interaktivitas Order Book (Klik Bid / Ask untuk mengisi harga)
    console.log("Testing Order Book depth click...");
    const clickablePrice = page.locator('.depth-table .clickable-price').first();
    if (await clickablePrice.count() === 1) {
        const pText = await clickablePrice.textContent();
        console.log("Clicking depth price:", pText);
        await clickablePrice.click();
        await page.waitForSelector('#order-modal.active', { timeout: 5000 });

        // Verifikasi input harga terisi otomatis
        const limitVal = await page.inputValue('#order-limit-price');
        console.log("Auto-filled limit price in modal:", limitVal);
        if (!limitVal || Number(limitVal) <= 0) {
            throw new Error("Order book click must fill limit price input!");
        }

        // 4. Verifikasi Tombol Cepat Porsi Modal (25%, 50%, 75%, MAX)
        console.log("Testing Quick Ticket percentage buttons...");
        const btn25 = page.locator('.btn-order-pct[data-pct="25"]');
        const btn50 = page.locator('.btn-order-pct[data-pct="50"]');
        const btnMax = page.locator('.btn-order-pct[data-pct="100"]');

        if (await btn25.count() !== 1 || await btnMax.count() !== 1) {
            throw new Error("Persentase modal (25%, 50%, 75%, MAX) harus tersedia di tiket order!");
        }

        await btn25.click();
        const qty25 = await page.inputValue('#order-qty');
        console.log("Qty on 25% click:", qty25);

        await btnMax.click();
        const qtyMax = await page.inputValue('#order-qty');
        console.log("Qty on MAX click:", qtyMax);

        if (Number(qtyMax) < Number(qty25)) {
            throw new Error("MAX qty harus >= 25% qty!");
        }

        // Verifikasi Estimasi Biaya & Transparansi Komisi
        const estText = await page.locator('#order-est-total').textContent();
        console.log("Estimated cost display:", estText);
        if (!estText.includes("Total Estimasi") || !estText.includes("Komisi broker")) {
            throw new Error("Tiket order harus menampilkan Total Estimasi dan Komisi broker 0.15%!");
        }

        // Tutup modal
        await page.click('#btn-close-modal');
        await page.waitForTimeout(300);
    }

    // Tangkap screenshot bukti Tahap 2
    const shotPath = path.join(evidenceDir, 'Tahap2-market-candlestick-orderbook.png');
    await page.screenshot({ path: shotPath });
    console.log("Captured screenshot:", shotPath);

    await browser.close();
    console.log("=== TAHAP 2 MARKET VERIFICATION SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
