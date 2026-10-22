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
    await page.click('button.nav-item[data-tab="riset"]');
    await page.waitForSelector('#view-riset', { timeout: 5000 });

    // Verifikasi tombol shortcut Riwayat Transaksi
    const historyBtn = page.locator('#view-riset button.shortcut:has-text("Riwayat Transaksi")');
    if (await historyBtn.count() !== 1) throw new Error("Tombol shortcut 'Riwayat Transaksi' harus ada di menu Lainnya");

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
    const lbCard = page.locator('#view-riset section.card:has-text("Liga & Peringkat")');
    if (await lbCard.count() !== 1) throw new Error("Card ringkasan 'Liga & Peringkat' harus ada di menu Lainnya");
    const seeRankBtn = lbCard.locator('button:has-text("Lihat Peringkat Lengkap & Rival")');
    if (await seeRankBtn.count() !== 1) throw new Error("Tombol 'Lihat Peringkat Lengkap & Rival' harus ada di kartu Liga");

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
    await page.locator('#view-riset button.shortcut:has-text("Profil Saya")').click();
    await page.waitForSelector('.flow-page[aria-label="Profil Saya"]', { timeout: 5000 });

    // Periksa keberadaan 5 seksi
    const sec1 = await page.locator('.flow-page h2:has-text("1. Identitas & Karier")').count();
    const sec2 = await page.locator('.flow-page h2:has-text("2. Ringkasan Keuangan")').count();
    const sec3 = await page.locator('.flow-page h2:has-text("3. Statistik Trading")').count();
    const sec4 = await page.locator('.flow-page h2:has-text("4. Performa & Pencapaian")').count();
    const sec5 = await page.locator('.flow-page h2:has-text("5. Pengaturan Identitas & Aksesibilitas")').count();

    console.log(`Profil Sections: Sec1=${sec1}, Sec2=${sec2}, Sec3=${sec3}, Sec4=${sec4}, Sec5=${sec5}`);
    if (sec1 !== 1 || sec2 !== 1 || sec3 !== 1 || sec4 !== 1 || sec5 !== 1) {
        throw new Error("Semua 5 seksi Profil Saya harus tampil lengkap!");
    }

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
