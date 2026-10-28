/**
 * Performance Benchmark (INT-02):
 * Collects >= 30 samples per action, calculating nearest-rank P95.
 * Asserts:
 * - P95 local action <= 300ms
 * - Max navigation <= 1000ms
 */
const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

function nearestRankP95(samples) {
    if (!samples.length) return 0.0;
    const sorted = [...samples].sort((a, b) => a - b);
    const idx = Math.max(0, Math.min(sorted.length - 1, Math.ceil(0.95 * sorted.length) - 1));
    return sorted[idx];
}

async function run() {
    const port = process.env.QA_PORT || '8794';
    const evidenceDir = path.resolve(__dirname, '../Artifacts/8-Anotasi-2026-10-22/run-01/evidence');
    if (!fs.existsSync(evidenceDir)) fs.mkdirSync(evidenceDir, { recursive: true });

    console.log(`Connecting benchmark to http://127.0.0.1:${port}/...`);
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext({ viewport: { width: 438, height: 627 } });
    const page = await context.newPage();

    await page.goto(`http://127.0.0.1:${port}/`);
    await page.waitForSelector('nav.bottom-nav', { timeout: 10000 });

    const results = {
        timestamp: new Date().toISOString(),
        hostEnvironment: "Windows 11 / Node 26.7.0 / Chromium Headless (Port " + port + ")",
        categories: {}
    };

    // Five classic primary tabs, thirty samples each.
    console.log("Benchmarking navigation routes (30 samples each)...");
    const tabs = ['beranda', 'pasar', 'aset', 'firma', 'riset'];
    for (const t of tabs) {
        const timings = [];
        for (let i = 0; i < 30; i++) {
            const start = performance.now();
            await page.click(`button.nav-item[data-tab="${t}"]`);
            await page.waitForSelector(`#view-${t}.active`);
            timings.push(performance.now() - start);
        }
        const p95 = nearestRankP95(timings);
        const max = Math.max(...timings);
        console.log(`Nav ${t}: P95=${p95.toFixed(2)}ms, Max=${max.toFixed(2)}ms (Limit: Max <= 1000ms, P95 <= 300ms)`);
        if (p95 > 300.0 || max > 1000.0) throw new Error(`Latency violation on nav_${t}: P95=${p95}, Max=${max}`);
        results.categories[`nav_${t}`] = { count: timings.length, p95Ms: p95, maxMs: max, raw: timings };
    }

    // 2. Benchmark Modal Open & Close: Profil Saya (30 sampel)
    console.log("Benchmarking Profil Saya modal (30 samples)...");
    const profTimings = [];
    for (let i = 0; i < 30; i++) {
        const start = performance.now();
        await page.locator('#view-riset').getByRole('button', {name: 'Profil Saya', exact: true}).click();
        await page.waitForSelector('.flow-page[aria-label="Profil Saya"]');
        await page.click('.flow-page button.back-button');
        await page.waitForTimeout(20);
        profTimings.push(performance.now() - start);
    }
    const profP95 = nearestRankP95(profTimings);
    console.log(`Modal Profil Saya: P95=${profP95.toFixed(2)}ms (Limit: <= 300ms)`);
    if (profP95 > 300.0) throw new Error(`Modal Profil Saya P95 ${profP95}ms > 300ms!`);
    results.categories['modal_profil_saya'] = { count: profTimings.length, p95Ms: profP95, raw: profTimings };

    // 3. Benchmark Modal Open & Close: Riwayat Transaksi (30 sampel)
    console.log("Benchmarking Riwayat Transaksi modal (30 samples)...");
    await page.click('button.nav-item[data-tab="aset"]');
    await page.locator('#asset-tools').evaluate(el => el.open = true);
    const histTimings = [];
    for (let i = 0; i < 30; i++) {
        const start = performance.now();
        await page.click('#asset-tools button:has-text("Riwayat Transaksi")');
        await page.waitForSelector('.flow-page[aria-label="Riwayat Transaksi"]');
        await page.click('.flow-page button.back-button');
        await page.waitForTimeout(20);
        histTimings.push(performance.now() - start);
    }
    const histP95 = nearestRankP95(histTimings);
    console.log(`Modal Riwayat Transaksi: P95=${histP95.toFixed(2)}ms (Limit: <= 300ms)`);
    if (histP95 > 300.0) throw new Error(`Modal Riwayat Transaksi P95 ${histP95}ms > 300ms!`);
    results.categories['modal_riwayat_transaksi'] = { count: histTimings.length, p95Ms: histP95, raw: histTimings };

    // 4. Benchmark Pencarian & Filter Saham (30 sampel)
    console.log("Benchmarking Search & Filter input (30 samples)...");
    await page.click('button.nav-item[data-tab="pasar"]');
    await page.waitForSelector('#stock-list-container');
    const searchTimings = [];
    const queryList = ['BBCA', 'BMRI', 'AAPL', 'TLKM', 'BBNI', 'MSFT', 'ASII'];
    for (let i = 0; i < 30; i++) {
        const q = queryList[i % queryList.length];
        const start = performance.now();
        await page.fill('#search-stock-input', q);
        await page.waitForTimeout(10);
        searchTimings.push(performance.now() - start);
    }
    await page.fill('#search-stock-input', '');
    const searchP95 = nearestRankP95(searchTimings);
    console.log(`Pencarian Saham: P95=${searchP95.toFixed(2)}ms (Limit: <= 300ms)`);
    if (searchP95 > 300.0) throw new Error(`Pencarian Saham P95 ${searchP95}ms > 300ms!`);
    results.categories['stock_search_filter'] = { count: searchTimings.length, p95Ms: searchP95, raw: searchTimings };

    results.verdict = "PASS_WITHIN_HOST_SCOPE";
    fs.writeFileSync(path.join(evidenceDir, 'INT-02-benchmark-report.json'), JSON.stringify(results, null, 2));

    await browser.close();
    console.log("=== BENCHMARK PERFORMA (INT-02) SUCCEEDED (ALL PASS) ===");
}

run().catch(err => {
    console.error("Benchmark failed:", err);
    process.exit(1);
});
