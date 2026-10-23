/**
 * Automated Playwright browser verification for A2, A8, and A10.
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

    // =========================================================================
    // 1. Verifikasi A2: Portofolio Lebih Ramping & Pemindahan Riwayat
    // =========================================================================
    console.log("[1] Testing A2: Portofolio Layout & History Entrypoint...");
    await page.click('button.nav-item[data-tab="aset"]');
    await page.waitForSelector('#view-aset', { timeout: 5000 });

    // Pastikan 4 kartu statistik kosong TIDAK ADA di Portofolio
    const oldStats = await page.locator('#asset-summary section.card:has-text("Statistik Kinerja Trading")').count();
    console.log("Old stats card count in portfolio:", oldStats);
    if (oldStats !== 0) throw new Error("Statistik kinerja trading harus dipindahkan dari Portofolio ke Profil!");

    // Pastikan tombol 'Aktivitas / ledger' TIDAK ADA di asset-summary
    const oldLedgerBtn = await page.locator('#asset-summary button:has-text("Aktivitas / ledger")').count();
    console.log("Old 'Aktivitas / ledger' button count:", oldLedgerBtn);
    if (oldLedgerBtn !== 0) throw new Error("Tombol 'Aktivitas / ledger' tidak boleh berada di ringkasan Portofolio!");

    // Tangkap screenshot portofolio bersih
    const portScreenshot = path.join(evidenceDir, 'A2-portfolio-clean.png');
    await page.screenshot({ path: portScreenshot });
    console.log("Captured clean portfolio screenshot:", portScreenshot);

    // =========================================================================
    // 2. Verifikasi A2 & A8: Menu Lainnya & Riwayat Transaksi
    // =========================================================================
    console.log("[2] Testing A2 & A8: Menu Lainnya & Riwayat Transaksi...");
    await page.locator('#asset-tools > summary').click();

    // Verifikasi tombol shortcut Riwayat Transaksi
    const historyBtn = page.locator('#asset-tools button:has-text("Riwayat Transaksi")');
    if (await historyBtn.count() !== 1) throw new Error("Riwayat Transaksi harus terjangkau dari alat Aset");

    // Buka Riwayat Transaksi
    await historyBtn.click();
    await page.waitForSelector('.flow-page[aria-label="Riwayat Transaksi"]', { timeout: 5000 });
    const historyTitle = await page.locator('.flow-page[aria-label="Riwayat Transaksi"] h1').first().textContent();
    console.log("History modal opened:", historyTitle);

    // Ambil screenshot riwayat transaksi
    const histScreenshot = path.join(evidenceDir, 'A2-history-modal.png');
    await page.screenshot({ path: histScreenshot });

    // Tutup flow panel riwayat
    await page.click('.flow-page button.back-button');
    await page.waitForTimeout(500);

    // Verifikasi A8: Liga & Peringkat di Lainnya berupa ringkasan proporsional
    await page.click('button.nav-item[data-tab="firma"]');
    const seeRankBtn = page.locator('#career-overview button:has-text("Liga & Rival")');
    if (await seeRankBtn.count() !== 1) throw new Error("Liga & Rival harus terjangkau dari ringkasan Karier");

    // Klik buka modal Liga lengkap
    await seeRankBtn.click();
    await page.waitForSelector('.flow-page[aria-label="Liga & rival"]', { timeout: 5000 });
    const podiumCount = await page.locator('.leaderboard-podium').count();
    console.log("Leaderboard podium count in full modal:", podiumCount);
    if (podiumCount !== 1) throw new Error("Podium peringkat harus tampil pada modal Liga lengkap");

    const lbScreenshot = path.join(evidenceDir, 'A8-league-modal.png');
    await page.screenshot({ path: lbScreenshot });

    // Tutup modal Liga
    await page.click('.flow-page button.back-button');
    await page.waitForTimeout(500);

    // =========================================================================
    // 3. Verifikasi A10: Halaman Profil Saya 5-Seksi
    // =========================================================================
    console.log("[3] Testing A10: Halaman Profil Saya...");
    // Buka dari shortcut menu Lainnya
    await page.locator('#btn-profile').click();
    await page.waitForSelector('.flow-page[aria-label="Profil Saya"]', { timeout: 5000 });

    const profile = await page.evaluate(() => JSON.parse(TradingNativeBridge.getPlayerProfile()));
    if ((await page.locator('.flow-page .wealth-hero h2').textContent()).trim() !== profile.playerName) throw new Error('Identitas profil berbeda dari domain');
    for (const [tab, title, metrics] of [
        ['Kinerja', 'Statistik Trading & Return', ['Total order terealisasi', 'Transaksi untung / rugi', 'Rasio menang', 'Transaksi terbaik', 'Transaksi terburuk', 'ROI mingguan']],
        ['Keuangan', 'Portofolio & Struktur Aset/Utang', ['Kas pribadi IDR', 'Kas pribadi USD', 'Portofolio saham', 'Koleksi aset mewah', 'Reksa dana NAB', 'Kewajiban utang margin', 'Dana kelolaan klien']],
        ['Pencapaian', 'Pencapaian & Peringkat Liga', ['Puncak kekayaan tertinggi', 'Hari perjalanan era aktif', 'Total hari bermain', 'Peringkat liga saat ini']]
    ]) {
        await page.locator('.profile-subtabs-nav').getByRole('button', {name: tab, exact: true}).click();
        if (await page.locator('.flow-page').getByRole('heading', {name: title, exact: true}).count() !== 1) throw new Error(`Seksi profil ${title} hilang`);
        const text = await page.locator('.flow-page').innerText();
        for (const metric of metrics) if (!text.includes(metric)) throw new Error(`Metric profil hilang: ${metric}`);
    }
    if (await page.locator('#player-text-size').count() !== 1 || await page.getByRole('button', {name: 'Simpan Profil & Preferensi', exact: true}).count() !== 1) throw new Error('Pengaturan identitas dan aksesibilitas harus tetap tersedia');

    // Ambil screenshot Profil Saya
    const profScreenshot = path.join(evidenceDir, 'A10-profile-modal.png');
    await page.screenshot({ path: profScreenshot });
    console.log("Captured Profil screenshot:", profScreenshot);

    await browser.close();
    console.log("=== BROWSER PLAYWRIGHT A2, A8, A10 SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
