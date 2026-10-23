/**
 * Automated Playwright browser verification for A1 & A3 Collections Gallery.
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
    await page.locator('.home-secondary > summary').click();
    await page.waitForSelector('#btn-home-collections', { timeout: 10000 });
    const catalogue = await page.evaluate(() => JSON.parse(TradingNativeBridge.getCollections()).collections);
    const initialOwned = catalogue.filter(item => item.owned || item.isOwned).length;
    const initialUnowned = catalogue.length - initialOwned;

    // 1. Buka Modal Galeri Koleksi
    console.log("Opening Collections Gallery modal...");
    await page.click('#btn-home-collections');
    await page.waitForSelector('.collections-gallery-wrap', { timeout: 5000 });

    // 2. Verifikasi A1: Dua bagian berjudul Belum Dimiliki dan Sudah Dimiliki
    const unownedSec = page.locator('.collection-section-group:has-text("Belum Dimiliki")');
    const ownedSec = page.locator('.collection-section-group:has-text("Sudah Dimiliki")');
    if (await unownedSec.count() !== 1 || await ownedSec.count() !== 1) {
        throw new Error("Seksi Belum Dimiliki dan Sudah Dimiliki harus ada di tampilan Semua Status");
    }

    console.log("Card list innerHTML:", await page.locator('#collections-card-list').innerHTML());
    const unownedBadge = (await unownedSec.locator('.badge').first().textContent()).trim();
    const ownedBadge = (await ownedSec.locator('h2 .badge-owned').textContent()).trim();
    console.log(`Initial counts - Belum Dimiliki: ${unownedBadge}, Sudah Dimiliki: ${ownedBadge}`);
    if (Number(unownedBadge) !== initialUnowned || Number(ownedBadge) !== initialOwned) {
        throw new Error(`Initial count salah: Belum=${unownedBadge}, Sudah=${ownedBadge}`);
    }

    // 3. Verifikasi Filter Kategori (misal Kendaraan)
    const category = catalogue[0].category.toUpperCase();
    await page.locator('.category-pills button').nth(1).click();
    await page.waitForTimeout(500);
    const vehicleCards = page.locator('.collection-item-card');
    const vCount = await vehicleCards.count();
    console.log(`Vehicle category count: ${vCount}`);
    const expectedVehicles = catalogue.filter(item => item.category.toUpperCase() === category);
    if (vCount !== expectedVehicles.length) throw new Error(`Kategori ${category} harus cocok domain: ${expectedVehicles.length}, aktual: ${vCount}`);
    for (const item of expectedVehicles) if (await page.locator(`.collection-item-card[data-collection-id="${item.id}"]`).count() !== 1) throw new Error(`Aset kategori hilang: ${item.id}`);

    // Kembali ke Semua Kategori
    await page.locator('.category-pills button:has-text("Semua")').click();
    await page.waitForTimeout(500);

    // 4. Beli satu koleksi (A3 verification)
    const firstCard = page.locator('.collection-item-card').first();
    const itemTitle = (await firstCard.locator('h3').textContent()).trim();
    const buyBtn = firstCard.locator('button.btn-primary:has-text("Beli Koleksi")');
    console.log(`Buying item: ${itemTitle}...`);
    await buyBtn.click();
    await page.waitForSelector('.game-toast:has-text("Koleksi Berhasil Dibeli")', { timeout: 5000 });
    await page.waitForTimeout(1000);

    // Verifikasi setelah beli: card berpindah ke Sudah Dimiliki
    const unownedAfter = (await page.locator('.collection-section-group:has-text("Belum Dimiliki") h2 .badge').textContent()).trim();
    const ownedAfter = (await page.locator('.collection-section-group:has-text("Sudah Dimiliki") h2 .badge-owned').textContent()).trim();
    console.log(`After buy counts - Belum Dimiliki: ${unownedAfter}, Sudah Dimiliki: ${ownedAfter}`);
    if (Number(unownedAfter) !== initialUnowned - 1 || Number(ownedAfter) !== initialOwned + 1) {
        throw new Error(`After buy count mismatch: Belum=${unownedAfter}, Sudah=${ownedAfter}`);
    }

    // Periksa kartu di Sudah Dimiliki memiliki badge ✓ Dimiliki dan tombol Jual Koleksi
    const ownedCard = page.locator('.collection-section-group:has-text("Sudah Dimiliki") .collection-item-card.owned-luxury');
    if (await ownedCard.count() !== 1) throw new Error("Harus ada 1 kartu dengan class owned-luxury di Sudah Dimiliki");
    const badgeOwned = await ownedCard.locator('.badge-owned:has-text("Dimiliki")').count();
    if (badgeOwned !== 1) throw new Error("Kartu harus memiliki badge ✓ Dimiliki");

    // Tangkap screenshot bukti A1 & A3
    const screenshotPath = path.join(evidenceDir, 'A1-A3-collections.png');
    await page.screenshot({ path: screenshotPath });
    console.log("Captured screenshot:", screenshotPath);

    // 5. Uji Filter Status "✓ Dimiliki"
    console.log("Testing filter '✓ Dimiliki'...");
    await page.locator('.collections-gallery-wrap button:has-text("✓ Dimiliki")').click();
    await page.waitForTimeout(500);
    const filteredOwned = await page.locator('.collection-item-card').count();
    console.log(`Filtered owned count: ${filteredOwned}`);
    if (filteredOwned !== 1) throw new Error(`Filter Dimiliki harus tampil tepat 1 item, aktual: ${filteredOwned}`);

    // 6. Uji Filter Status "○ Belum Dimiliki"
    console.log("Testing filter '○ Belum Dimiliki'...");
    await page.locator('.collections-gallery-wrap button:has-text("○ Belum Dimiliki")').click();
    await page.waitForTimeout(500);
    const filteredUnowned = await page.locator('.collection-item-card').count();
    console.log(`Filtered unowned count: ${filteredUnowned}`);
    if (filteredUnowned !== initialUnowned - 1) throw new Error(`Filter Belum Dimiliki harus cocok domain sesudah beli, aktual: ${filteredUnowned}`);

    // 7. Kembalikan ke Semua Status dan Jual kembali
    console.log("Selling item back...");
    await page.locator('.collections-gallery-wrap button:has-text("Semua Status")').click();
    await page.waitForTimeout(500);
    const sellBtn = page.locator('.collection-section-group:has-text("Sudah Dimiliki") button.btn-secondary:has-text("Jual Koleksi")');
    await sellBtn.click();
    await page.waitForSelector('.game-toast:has-text("Koleksi Berhasil Terjual")', { timeout: 5000 });
    await page.waitForTimeout(1000);

    const finalUnowned = (await page.locator('.collection-section-group:has-text("Belum Dimiliki") h2 .badge').textContent()).trim();
    const finalOwned = (await page.locator('.collection-section-group:has-text("Sudah Dimiliki") h2 .badge-owned').textContent()).trim();
    console.log(`After sell counts - Belum Dimiliki: ${finalUnowned}, Sudah Dimiliki: ${finalOwned}`);
    if (Number(finalUnowned) !== initialUnowned || Number(finalOwned) !== initialOwned) {
        throw new Error(`After sell count mismatch: Belum=${finalUnowned}, Sudah=${finalOwned}`);
    }

    await browser.close();
    console.log("=== BROWSER PLAYWRIGHT A1 & A3 SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
