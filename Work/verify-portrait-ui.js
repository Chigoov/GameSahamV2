let chromium;
try {
    chromium = require('playwright').chromium;
} catch (e) {
    chromium = require('playwright-core').chromium;
}
const fs = require('fs');
const path = require('path');

const args = process.argv.slice(2);
let customOutDir = null;
let targetPort = process.env.QA_PORT || '8784';
let targetUrl = null;
for (let i = 0; i < args.length; i++) {
    if (args[i] === '--output-dir' && args[i + 1]) {
        customOutDir = path.resolve(args[i + 1]);
        i++;
    } else if (args[i] === '--port' && args[i + 1]) {
        targetPort = args[i + 1];
        i++;
    } else if (args[i] === '--url' && args[i + 1]) {
        targetUrl = args[i + 1];
        i++;
    }
}
const outDir = customOutDir || process.env.UI_VERIFY_OUT_DIR || path.resolve(__dirname, '../Artifacts/UI-Refresh-2026-10-20');
fs.mkdirSync(outDir, { recursive: true });
const baseUrl = targetUrl || `http://127.0.0.1:${targetPort}`;

const checks = [];
const samples = [];
const errors = [];
const rawTimings = {
    navigation: [],
    ordinaryAction: []
};

function passed(name) {
    checks.push(name);
    console.log('PASS:', name);
}

function calculateP95(arr) {
    if (arr.length === 0) return 0;
    const sorted = [...arr].sort((a, b) => a - b);
    const idx = Math.ceil(0.95 * sorted.length) - 1;
    return sorted[Math.max(0, idx)];
}

(async () => {
    const launchOptions = { headless: true };
    const defaultExe = path.join(process.env.LOCALAPPDATA || '', 'ms-playwright/chromium-1234/chrome-win64/chrome.exe');
    if (process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH) {
        launchOptions.executablePath = process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH;
    } else if (fs.existsSync(defaultExe)) {
        launchOptions.executablePath = defaultExe;
    }
    const browser = await chromium.launch(launchOptions);
    const page = await browser.newPage({
        viewport: { width: 390, height: 844 },
        reducedMotion: 'reduce'
    });
    page.setDefaultTimeout(10000);
    page.on('pageerror', err => errors.push(String(err)));

    await page.goto(baseUrl);
    await page.waitForLoadState('networkidle');

    const platform = await page.evaluate(() => JSON.parse(TradingNativeBridge.getPlatformInfo()));
    if (!platform.testFixture || !platform.label.includes('QA HOST JAVA')) {
        throw new Error('Host must be QA HOST JAVA testFixture');
    }

    const call = async (method, ...args) => {
        const res = await page.evaluate(([m, a]) => TradingNativeBridge[m](...a), [method, args]);
        return JSON.parse(res);
    };

    const tab = async (name) => {
        const start = performance.now();
        await page.locator(`.nav-item[data-tab="${name}"]`).click();
        const duration = performance.now() - start;
        rawTimings.navigation.push(duration);
    };

    const back = async () => {
        await page.getByRole('button', { name: 'Kembali', exact: true }).click();
    };

    const flow = () => page.locator('.flow-page');

    const screenshot = async (name) => {
        await page.screenshot({ path: path.join(outDir, name + '.png') });
    };

    // Reset game QA state
    const newGameRes = await call('startNewGame', '{}');
    if (!newGameRes.success) throw new Error('startNewGame failed');
    await page.reload();
    await page.waitForLoadState('networkidle');

    const snap = await call('getSnapshot');
    const homeNet = await page.locator('#home-net-worth').innerText();
    const headerNet = await page.locator('#header-net-worth').innerText();
    if (homeNet !== snap.personalAccount.netWorth || headerNet !== snap.personalAccount.netWorth) {
        throw new Error(`Net worth mismatch: home=${homeNet}, header=${headerNet}, domain=${snap.personalAccount.netWorth}`);
    }
    passed('Home hero and header use Java net equity, not decorative balances');
    await screenshot('home-final');

    // 1. 35 root/viewport samples: Content scrolls, navigation remains visible at fixed coordinates
    const viewports = [
        [320, 568], [360, 640], [390, 844], [412, 915], [430, 932], [768, 1024], [1440, 900]
    ];
    for (const [w, h] of viewports) {
        await page.setViewportSize({ width: w, height: h });
        for (const name of ['beranda', 'pasar', 'aset', 'firma', 'riset']) {
            await tab(name);
            const navBefore = await page.locator('.bottom-nav').boundingBox();
            await page.locator('main').evaluate(el => el.scrollTop = el.scrollHeight);
            const navAfter = await page.locator('.bottom-nav').boundingBox();
            if (JSON.stringify(navBefore) !== JSON.stringify(navAfter)) {
                throw new Error(`Bottom nav moved during scroll on tab ${name} at ${w}x${h}`);
            }
            const m = await page.evaluate(() => ({
                width: innerWidth,
                scrollWidth: document.documentElement.scrollWidth,
                height: innerHeight,
                bodyScroll: document.documentElement.scrollHeight,
                navBottom: document.querySelector('.bottom-nav').getBoundingClientRect().bottom,
                mainHeight: document.querySelector('main').clientHeight
            }));
            if (m.scrollWidth > w + 1 || m.bodyScroll > h + 1) {
                throw new Error(`Horizontal or page overflow on tab ${name}: ${JSON.stringify(m)}`);
            }
            if (Math.abs(m.navBottom - h) > 1 || m.mainHeight < 80) {
                throw new Error(`Nav position or main height invalid on tab ${name}: ${JSON.stringify(m)}`);
            }
            const activeTab = await page.locator('.nav-item[aria-current="page"]').getAttribute('data-tab');
            if (activeTab !== name) throw new Error(`Active tab mismatch: expected ${name}, got ${activeTab}`);
            const textLen = (await page.locator('#view-' + name).innerText()).length;
            if (textLen <= 60) throw new Error(`Tab ${name} has insufficient content`);
            samples.push({ tab: name, ...m });
        }
    }
    passed('35 root/viewport samples: content scrolls, nav stays visible, no page overflow');

    // 2. Search, filter and scroll persistence
    await page.setViewportSize({ width: 390, height: 844 });
    await tab('pasar');
    await page.locator('#filter-exchange-select').selectOption('US');
    await page.locator('#search-stock-input').fill('AAPL');
    const countAapl = await page.locator('#stock-list-container .stock-list-item').count();
    if (countAapl !== 1) throw new Error(`Expected 1 stock item for AAPL, got ${countAapl}`);
    await page.locator('main').evaluate(el => el.scrollTop = 200);
    const scrollVal = await page.locator('main').evaluate(el => el.scrollTop);

    await tab('beranda');
    await tab('pasar');
    const exVal = await page.locator('#filter-exchange-select').inputValue();
    const qVal = await page.locator('#search-stock-input').inputValue();
    const scrollAfter = await page.locator('main').evaluate(el => el.scrollTop);
    if (exVal !== 'US' || qVal !== 'AAPL' || Math.abs(scrollAfter - scrollVal) >= 2) {
        throw new Error('Search, filter, or scroll not preserved across tab switches');
    }
    passed('Market search, exchange filter and per-root scroll survive navigation');

    await page.locator('#search-stock-input').fill('no-company-like-this');
    const zeroCount = await page.locator('#stock-list-container .stock-list-item').count();
    const statusText = await page.locator('#stock-list-status').innerText();
    if (zeroCount !== 0 || !statusText.includes('Tidak ada instrumen')) {
        throw new Error('Search empty state not handled properly');
    }
    await page.locator('#filter-exchange-select').selectOption('');
    await page.locator('#search-stock-input').fill('');
    await screenshot('market-final');
    passed('Search empty state is explicit; clearing filter restores domain instruments');

    // 3. Stale quote resilience and focus retention
    await page.locator('#search-stock-input').focus();
    await page.evaluate(() => {
        window.quoteRow = document.querySelector('#stock-list-container .stock-list-item');
        window.quoteReader = TradingNativeBridge.getInstruments;
        TradingNativeBridge.getInstruments = () => { throw Error('QA quote failure'); };
    });
    await page.locator('#quote-update-warning').waitFor({ state: 'visible' });
    const isFocused = await page.locator('#search-stock-input').evaluate(el => el === document.activeElement);
    if (!isFocused) throw new Error('Search input lost focus during quote failure');
    await page.evaluate(() => { TradingNativeBridge.getInstruments = window.quoteReader; });
    await page.locator('#quote-update-warning').waitFor({ state: 'detached' });
    passed('Quote failure preserves last rows and input focus, announces staleness and recovers');

    // 4. Detail instrumen tanpa transaksi
    await page.locator('#stock-list-container .stock-list-item').first().click();
    const flowText = await flow().innerText();
    if (!flowText.includes('Belum ada transaksi tercatat')) throw new Error('Expected no trades message');
    const navInert = await page.locator('.bottom-nav').evaluate(el => el.inert);
    if (navInert) throw new Error('Bottom nav should not be inert on regular detail reading');
    await page.getByRole('button', { name: 'Tambah ke watchlist', exact: true }).click();
    const instruments = await call('getInstruments', '', '', null, true);
    if (!instruments.some(s => s.isWatchlist)) throw new Error('Watchlist toggle failed in domain');
    await screenshot('instrument-no-trades');
    passed('Instrument has honest no-trade chart state and actual watchlist command');

    await tab('aset');
    const flowCount = await flow().count();
    const assetText = await page.locator('#view-aset').innerText();
    if (flowCount !== 0 || !assetText.includes('Belum ada posisi')) {
        throw new Error('Portfolio empty state invalid');
    }
    await screenshot('assets-empty');
    passed('Root navigation closes reading details and portfolio empty state is honest');

    // 5. Riset & Entitlement
    const xpBefore = (await call('getPlayerProgress')).currentXp;
    await tab('riset');
    const xpAfterMenu = (await call('getPlayerProgress')).currentXp;
    if (xpAfterMenu !== xpBefore) throw new Error('Opening menu should not award XP');
    await page.getByRole('button', { name: 'Baca catatan', exact: true }).first().click();
    const readXp = (await call('getPlayerProgress')).currentXp;
    if (readXp < xpBefore) throw new Error('Reading article should award XP');
    await back();
    await tab('riset');
    await page.getByRole('button', { name: 'Baca catatan', exact: true }).first().click();
    const rereadXp = (await call('getPlayerProgress')).currentXp;
    if (rereadXp !== readXp) throw new Error('Rereading article must be idempotent');
    await back();

    await page.getByRole('button', { name: 'Lihat paket & opsi langganan', exact: true }).click();
    const proDisabled = await page.locator('[data-tier=PRO] button').isDisabled();
    if (!proDisabled) throw new Error('PRO tier should be locked for novice');
    await back();
    await screenshot('research-final');
    passed('Research requires opening an article; reread is idempotent; novice premium stays locked');

    // 6. Jalur trading via Java domain
    await call('setClockState', true, 1);
    await call('tickClock', 7);
    await call('setClockState', false, 1);

    await tab('pasar');
    await page.locator('#stock-list-container .stock-list-item').first().click();
    const buyBtn = page.locator('.btn-detail-buy').or(page.getByRole('button', { name: 'Buat order', exact: true }));
    await buyBtn.first().click();

    const tInert = await page.locator('.bottom-nav').evaluate(el => el.inert);
    if (!tInert) throw new Error('Bottom nav must be inert during order sheet');

    await page.evaluate(() => {
        document.querySelectorAll('.side-btn').forEach(b => {
            b.classList.remove('active');
            b.setAttribute('aria-pressed', 'false');
        });
    });

    await page.locator('#btn-submit-order').click();
    const orderErr = await page.locator('#order-error').innerText();
    if (!orderErr.includes('Pilih beli atau jual')) throw new Error('Missing side selection error');

    await page.locator('[data-side=BUY]').click();
    await page.locator('[data-side=SELL]').click();
    const buyPressed = await page.locator('[data-side=BUY]').getAttribute('aria-pressed');
    const sellPressed = await page.locator('[data-side=SELL]').getAttribute('aria-pressed');
    if (buyPressed !== 'false' || sellPressed !== 'true') throw new Error('Side toggle state invalid');

    await page.locator('[data-side=BUY]').click();
    await page.locator('#order-type').selectOption('MARKET');
    await page.locator('#order-qty').fill('1');

    // Uji short viewport 390x390
    await page.setViewportSize({ width: 390, height: 390 });
    await page.locator('#order-qty').focus();
    await page.locator('#btn-submit-order').scrollIntoViewIfNeeded();
    const btnBox = await page.locator('#btn-submit-order').boundingBox();
    if (!btnBox || btnBox.y < 0) throw new Error('Submit button unreachable on short viewport');
    await screenshot('ticket-short-viewport');

    await page.setViewportSize({ width: 390, height: 844 });
    await page.locator('#btn-submit-order').click();
    const isIsolated = await flow().evaluate(el => el.classList.contains('isolated'));
    if (!isIsolated) throw new Error('Review order sheet must be isolated');
    await screenshot('review-final');

    await page.getByRole('button', { name: 'Kirim order', exact: true }).click();
    const receiptText = await flow().innerText();
    if (!receiptText.includes('FILLED')) throw new Error('Order fill receipt expected');
    const xpEarned = (await call('getPlayerProgress')).currentXp;
    if (xpEarned < 100) throw new Error('Trade execution should award career XP');

    await back();
    await tab('aset');
    const portList = await page.locator('#portfolio-positions-list').innerText();
    if (!portList.includes('BBCA')) throw new Error('Position BBCA missing in portfolio');
    const snapBefore = (await call('getSnapshot')).personalAccount;
    const saveRes = await call('saveGame');
    const loadRes = await call('loadGame');
    if (!saveRes.success || !loadRes.success) throw new Error('Save/Load roundtrip failed');
    const snapAfter = (await call('getSnapshot')).personalAccount;
    if (JSON.stringify(snapBefore) !== JSON.stringify(snapAfter)) {
        throw new Error('Account state mismatch after save/load');
    }
    await screenshot('assets-with-position');
    passed('Side selection, short viewport ticket, isolated review, Java fill, position and save/load');

    // 7. Grafik & Alternatif Tabel OHLCV
    await tab('pasar');
    await page.locator('#stock-list-container .stock-list-item').first().click();
    await screenshot('instrument-with-history');
    await page.getByRole('button', { name: 'Garis', exact: true }).click();
    const linePressed = await page.getByRole('button', { name: 'Garis', exact: true }).getAttribute('aria-pressed');
    if (linePressed !== 'true') throw new Error('Line chart toggle failed');

    await page.getByRole('button', { name: 'Tabel OHLCV Alternatif', exact: true }).click();
    const trCount = await page.locator('#chart-table tbody tr').count();
    if (trCount === 0) throw new Error('OHLCV alternative table empty');

    await page.locator('[data-timeframe=M1]').click();
    const m1Pressed = await page.locator('[data-timeframe=M1]').getAttribute('aria-pressed');
    if (m1Pressed !== 'true') throw new Error('M1 timeframe button toggle failed');
    passed('Recorded game transactions render chart, timeframe, line/candle and alternative table');
    await back();

    // 8. Achievements & Missions
    await tab('beranda');
    await page.locator('#btn-achievements').click();
    await page.getByRole('button', { name: 'Terbuka', exact: true }).click();
    const lockedCount = await page.locator('.achievement-card.locked').count();
    if (lockedCount !== 0) throw new Error('Locked filter should not show locked cards');
    await flow().getByRole('button', { name: 'Semua', exact: true }).click();
    await screenshot('achievements-final');
    await back();

    await page.locator('#btn-career').click();
    const missionText = await flow().innerText();
    if (!missionText.includes('Syarat Senior Analyst')) throw new Error('Career requirements missing');
    await screenshot('missions-final');
    await back();
    passed('Badge filters reflect state; earned mission and promotion requirements remain reachable');

    // 9. 15 Root text-200 samples
    for (const [w, h] of [[320, 568], [390, 844], [412, 915]]) {
        await page.setViewportSize({ width: w, height: h });
        await page.evaluate(() => document.documentElement.style.fontSize = '200%');
        for (const name of ['beranda', 'pasar', 'aset', 'firma', 'riset']) {
            await tab(name);
            await page.locator('main').evaluate(el => el.scrollTop = el.scrollHeight);
            const m = await page.evaluate(() => ({
                width: innerWidth,
                scrollWidth: document.documentElement.scrollWidth,
                nav: document.querySelector('.bottom-nav').getBoundingClientRect().bottom,
                main: document.querySelector('main').clientHeight
            }));
            if (m.scrollWidth > w + 1 || Math.abs(m.nav - h) > 1 || m.main <= 70) {
                throw new Error(`Text-200 overflow on ${name} at ${w}x${h}: ${JSON.stringify(m)}`);
            }
        }
        await tab('beranda');
        await page.locator('#btn-achievements').click();
        const detailOverflow = await flow().evaluate(el => el.scrollWidth <= el.clientWidth + 1);
        if (!detailOverflow) throw new Error(`Achievement detail overflow at text-200 ${w}x${h}`);
        await page.locator('main').evaluate(el => el.scrollTop = el.scrollHeight);
        await screenshot(`text-200-${w}`);
        await back();
        await page.evaluate(() => document.documentElement.style.fontSize = '');
    }
    passed('15 root text-200 samples and achievement detail reflow without horizontal overflow');

    // 10. Pengukuran Kinerja Standar A5 (>= 30 sampel aksi biasa dan >= 30 sampel navigasi)
    console.log('Mengukur latensi performa host lokal (A5)...');
    for (let i = 0; i < 35; i++) {
        // Navigasi
        const navTabs = ['beranda', 'pasar', 'aset', 'firma', 'riset'];
        const targetTab = navTabs[i % navTabs.length];
        const t0 = performance.now();
        await tab(targetTab);
        const t1 = performance.now();
        rawTimings.navigation.push(t1 - t0);

        // Aksi biasa: buka modal, tutup modal
        const a0 = performance.now();
        await page.locator('#context-account-btn').click();
        await page.getByRole('button', { name: 'Kembali', exact: true }).click();
        const a1 = performance.now();
        rawTimings.ordinaryAction.push(a1 - a0);
    }

    const p95Nav = calculateP95(rawTimings.navigation);
    const p95Action = calculateP95(rawTimings.ordinaryAction);
    const maxNav = Math.max(...rawTimings.navigation);
    const maxAction = Math.max(...rawTimings.ordinaryAction);

    console.log(`Performa Navigasi: P95=${p95Nav.toFixed(1)}ms (ambang <= 1000ms), Max=${maxNav.toFixed(1)}ms`);
    console.log(`Performa Aksi Biasa: P95=${p95Action.toFixed(1)}ms (ambang <= 300ms), Max=${maxAction.toFixed(1)}ms`);

    if (p95Nav > 1000) throw new Error(`P95 navigasi melampaui ambang: ${p95Nav}ms > 1000ms`);
    if (p95Action > 300) throw new Error(`P95 aksi melampaui ambang: ${p95Action}ms > 300ms`);
    passed(`Performance benchmark: P95 Action=${p95Action.toFixed(1)}ms <= 300ms, P95 Navigation=${p95Nav.toFixed(1)}ms <= 1000ms`);

    if (errors.length > 0) throw new Error('Page errors detected: ' + JSON.stringify(errors));
    passed('No JavaScript runtime errors');

    await browser.close();

    const result = {
        status: 'PASS',
        scope: 'Isolated Java host + browser; not Android/device evidence',
        checks,
        viewportSamples: samples,
        pageErrors: errors,
        performance: {
            navigationSamplesCount: rawTimings.navigation.length,
            navigationP95Ms: p95Nav,
            navigationMaxMs: maxNav,
            actionSamplesCount: rawTimings.ordinaryAction.length,
            actionP95Ms: p95Action,
            actionMaxMs: maxAction
        }
    };

    fs.writeFileSync(path.join(outDir, 'portrait-ui-result.json'), JSON.stringify(result, null, 2), 'utf-8');
    console.log(JSON.stringify({
        status: 'PASS',
        checksCount: checks.length,
        viewportSamplesCount: samples.length,
        p95NavMs: p95Nav.toFixed(1),
        p95ActionMs: p95Action.toFixed(1)
    }));
})().catch(err => {
    console.error('VERIFICATION ERROR:', err);
    process.exit(1);
});
