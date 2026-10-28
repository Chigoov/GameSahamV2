/**
 * Automated Playwright browser verification for Tahap 4:
 * 1. Beranda Agenda & Quick actions
 * 2. Misi Periodik (Checklist Misi Harian, Mingguan, Karier)
 * 3. DNA Gaya Investor (Archetype diagnosis)
 * 4. Liga & Rival NPC (Weekly standings & competitor)
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

    page.on('dialog', async dialog => {
        console.log(`Dialog: [${dialog.type()}] ${dialog.message()}`);
        await dialog.accept();
    });

    await page.goto(`http://127.0.0.1:${port}/`);
    await page.waitForSelector('nav.bottom-nav', { timeout: 10000 });

    // 1. Uji Buka Misi Periodik dari Beranda
    console.log("Opening Misi Periodik modal from Beranda...");
    await page.locator('#btn-missions').click();
    await page.waitForSelector('.flow-page[aria-label="Pusat Misi & Target"]', { timeout: 5000 });

    const missionTabs = page.locator('.mission-tabs-nav button');
    console.log("Mission tabs count:", await missionTabs.count());
    if (await missionTabs.count() < 3) throw new Error("Pusat Misi harus memiliki 3 tab!");
    await page.locator('.back-button').click();
    await page.waitForTimeout(300);

    // 2. Uji Buka DNA Investor dari Beranda
    console.log("Opening DNA Investor modal from Beranda...");
    await page.locator('#btn-home-archetype').click();
    await page.waitForSelector('.flow-page[aria-label="Gaya Investor (Archetype)"]', { timeout: 5000 });

    const dnaText = await page.locator('.flow-page[aria-label="Gaya Investor (Archetype)"]').textContent();
    console.log("DNA modal text snippet:", dnaText.substring(0, 150));
    if (!dnaText.includes("Gaya Investasimu Sedang Terbentuk") && !dnaText.includes("Indikator")) {
        throw new Error("DNA Investor harus menampilkan profil risiko berbasis perilaku pasar!");
    }
    await page.locator('.back-button').click();
    await page.waitForTimeout(300);

    // 3. Uji Buka Liga & Rival dari Beranda
    console.log("Opening Liga & Rival modal from Beranda...");
    await page.locator('#btn-league').click();
    await page.waitForSelector('.flow-page[aria-label="Liga & rival"]', { timeout: 5000 });

    const leagueText = await page.locator('.flow-page[aria-label="Liga & rival"]').textContent();
    console.log("League modal snippet:", leagueText.substring(0, 150));
    if (!leagueText.includes("Divisi Perunggu") && !leagueText.includes("Rival")) {
        throw new Error("Modal Liga & Rival harus memuat divisi dan status rival!");
    }

    // Tangkap screenshot bukti Tahap 4
    const shotPath = path.join(evidenceDir, 'Tahap4-beranda-missions-dna-league.png');
    await page.screenshot({ path: shotPath });
    console.log("Captured screenshot:", shotPath);

    await browser.close();
    console.log("=== TAHAP 4 BERANDA, MISSIONS, DNA & LEAGUE VERIFICATION SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Test failed:", err);
    process.exit(1);
});
