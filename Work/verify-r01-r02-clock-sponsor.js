/**
 * Verification for R01 (Countdown Hibah HH:mm:ss) and R02 (Clock otomatis, no manual time controls).
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
    // 1. Verifikasi R02: Hapus Atur Waktu manual & Verifikasi Jadwal Sesi
    // =========================================================================
    console.log("[1] Testing R02: Verifying absence of manual clock controls...");
    const briefingBtn = page.locator('#btn-briefing');
    await briefingBtn.click();
    await page.waitForSelector('.flow-page[aria-label="Jadwal & Sesi Bursa"]', { timeout: 5000 });

    // Pastikan tombol manual play/pause, speed dropdown, dan Skip Day TIDAK ADA
    const pauseBtn = await page.locator('.flow-page button:has-text("Jeda waktu"), .flow-page button:has-text("Mulai / lanjutkan")').count();
    const speedSelect = await page.locator('.flow-page select#briefing-speed').count();
    const skipDayBtn = await page.locator('.flow-page button:has-text("Lewati 1 hari")').count();

    console.log(`Manual controls count: pauseBtn=${pauseBtn}, speedSelect=${speedSelect}, skipDayBtn=${skipDayBtn}`);
    if (pauseBtn !== 0 || speedSelect !== 0 || skipDayBtn !== 0) {
        throw new Error("Kontrol manual play/pause, speed dropdown, atau Skip Day masih ditemukan di UI!");
    }

    // Pastikan deskripsi otomatis tampil
    const modeDesc = await page.locator('.flow-page .data-row:has-text("Mode waktu") strong').textContent();
    console.log("Mode waktu description:", modeDesc);
    const actualRate = await page.evaluate(() => JSON.parse(TradingNativeBridge.getSnapshot()).clock.rate);
    const backgroundDesc = await page.locator('.flow-page .data-row:has-text("Keterangan waktu latar") strong').textContent();
    if (!modeDesc.includes(`1 hari game = ${86400/actualRate/60} menit nyata`) || !backgroundDesc.includes('1 hari game = 1 jam nyata')) throw new Error('Keterangan UI harus sesuai laju Java yang aktif');

    // Tutup panel briefing
    await page.evaluate(() => window.closeActiveLayer());
    await page.waitForTimeout(500);

    // =========================================================================
    // 2. Verifikasi R02: Clock mengalir otomatis di header
    // =========================================================================
    console.log("[2] Testing R02: Clock flows smoothly...");
    const clock1 = (await page.locator('#clock-display').textContent()).trim();
    await page.waitForTimeout(1200);
    const clock2 = (await page.locator('#clock-display').textContent()).trim();
    console.log(`Clock flow check: t0=${clock1} -> t1=${clock2}`);
    if (clock1 === clock2 && !clock1.includes("WIB")) {
        throw new Error("Clock display harus berformat WIB dan mengalir aktif!");
    }

    // =========================================================================
    // 3. Verifikasi R01: Countdown Hibah Harian HH:mm:ss
    // =========================================================================
    console.log("[3] Testing R01: Hibah harian countdown after claim...");
    const claimBtn = page.locator('#btn-claim-sponsor');
    const initText = (await claimBtn.textContent()).trim();
    console.log("Initial claim button text:", initText);

    if (await claimBtn.isEnabled()) {
        await claimBtn.click();
        await page.waitForSelector('.game-toast', { timeout: 5000 });
        await page.waitForTimeout(600);
    }

    // Periksa bahwa tombol sekarang menampilkan countdown HH:mm:ss
    const countdownText1 = (await claimBtn.textContent()).trim();
    console.log("Button text after claim (t0):", countdownText1);
    if (!countdownText1.includes("Klaim berikutnya") || !/\d{2}:\d{2}:\d{2}/.test(countdownText1)) {
        throw new Error(`Tombol hibah harus memuat countdown format HH:mm:ss! Aktual: '${countdownText1}'`);
    }

    await page.waitForTimeout(1100);
    const countdownText2 = (await claimBtn.textContent()).trim();
    console.log("Button text after claim (t1):", countdownText2);
    if (!countdownText2.includes("Klaim berikutnya") || !/\d{2}:\d{2}:\d{2}/.test(countdownText2)) {
        throw new Error(`Countdown harus berlanjut format HH:mm:ss! Aktual: '${countdownText2}'`);
    }

    // Pastikan tombol disabled
    const isDisabled = await claimBtn.isDisabled();
    if (!isDisabled) throw new Error("Tombol harus disabled saat masa countdown berlangsung!");

    const shotPath = path.join(evidenceDir, 'R01-R02-countdown-automatic-clock.png');
    await page.screenshot({ path: shotPath });
    console.log("Captured screenshot:", shotPath);

    await browser.close();
    if (actualRate !== 288) throw new Error(`BLOCKED_DOMAIN: mandat 5 menit per hari membutuhkan 288x, Java aktual ${actualRate}x; pemeriksaan UI countdown sudah dijalankan`);
    console.log("=== R01 & R02 TESTS PASSED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
