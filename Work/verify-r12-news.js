/**
 * Automated Playwright browser verification for R12 (News berjalan, countdown, clue sektor/arah/rentang/horizon).
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

    // Cek kartu kabar pasar di Beranda
    console.log("Checking News Cards on Beranda...");
    await page.waitForSelector('#home-news-list .news-card', { timeout: 5000 });

    const cards = page.locator('#home-news-list .news-card');
    const cardCount = await cards.count();
    console.log("News cards count on Home:", cardCount);
    if (cardCount < 2) throw new Error("Minimal harus ada 2 kartu berita pasar di Beranda!");

    // Ambil konten kartu pertama
    const firstCardText = await cards.first().textContent();
    console.log("First card summary:", firstCardText.substring(0, 120));
    if (!firstCardText.includes("Keyakinan:") || (!firstCardText.includes("POSITIF") && !firstCardText.includes("NEGATIF") && !firstCardText.includes("NETRAL"))) {
        throw new Error("Kartu berita harus memuat arah sentimen dan tingkat keyakinan!");
    }

    // Klik kartu berita untuk membuka modal detail
    console.log("Clicking news card to view detail modal...");
    await cards.first().click();
    await page.waitForSelector('.flow-page[aria-label="Kabar & Dampak Pasar"]', { timeout: 5000 });

    const modal = page.locator('.flow-page[aria-label="Kabar & Dampak Pasar"]');
    const modalText = await modal.textContent();
    console.log("Modal detail header:", modalText.substring(0, 150));

    if (!modalText.includes("Kategori & Sektor") || !modalText.includes("Arah & Estimasi Dampak") || !modalText.includes("Tingkat Keyakinan") || !modalText.includes("Horizon Waktu")) {
        throw new Error("Modal Kabar & Dampak Pasar harus memuat rincian lengkap R12!");
    }

    // Tangkap screenshot bukti R12
    const shotPath = path.join(evidenceDir, 'R12-news-impact.png');
    await page.screenshot({ path: shotPath });
    console.log("Captured screenshot:", shotPath);

    await browser.close();
    console.log("=== R12 NEWS BROWSER VERIFICATION SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
