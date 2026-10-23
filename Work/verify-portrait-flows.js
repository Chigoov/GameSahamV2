const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

const outDir = path.resolve(__dirname, '../Artifacts/UI-Refresh-2026-10-20');
fs.mkdirSync(outDir, { recursive: true });

const checks = [];
const errors = [];

(async () => {
    const browser = await chromium.launch({ headless: true });
    const page = await browser.newPage({
        viewport: { width: 390, height: 844 },
        reducedMotion: 'reduce'
    });
    page.setDefaultTimeout(10000);
    page.on('pageerror', err => errors.push(String(err)));

    await page.goto('http://127.0.0.1:8767');
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
        await page.locator(`.nav-item[data-tab="${name}"]`).click();
    };

    const back = async () => {
        await page.getByRole('button', { name: 'Kembali', exact: true }).click();
    };

    const opened = async (name, marker) => {
        const layer = page.locator('.flow-page');
        const text = await layer.innerText();
        if (!text.includes(marker)) throw new Error(`Marker "${marker}" not found in ${name}`);
        const noHorizOverflow = await layer.evaluate(el => el.scrollWidth <= el.clientWidth + 1);
        if (!noHorizOverflow) throw new Error(`Horizontal overflow in ${name}`);
        await layer.evaluate(el => el.scrollTop = el.scrollHeight);
        await page.screenshot({ path: path.join(outDir, name + '.png') });
        if (errors.length > 0) throw new Error('Page errors: ' + JSON.stringify(errors));
        checks.push(name);
        console.log('PASS Flow:', name);
    };

    // 1. Reset game
    const initRes = await call('startNewGame', '{}');
    if (!initRes.success) throw new Error('startNewGame failed');
    await page.reload();
    await page.waitForLoadState('networkidle');

    // 2. Flows Pasar & Modals
    await tab('pasar');
    await page.locator('#indices-row .index-card').first().click();
    await opened('index-detail', 'Anggota');
    await back();

    await page.getByRole('button', { name: 'Detail indeks N225', exact: true }).click();
    await opened('index-unavailable', 'belum');
    await back();

    await page.locator('#btn-ipo-center').click();
    await opened('ipo-center', 'IPO');
    await back();

    await page.locator('#btn-multiasset').click();
    await opened('multiasset', 'Obligasi');
    await back();

    await page.locator('#btn-order-history').click();
    await opened('orders-empty', 'Belum ada order');
    await back();

    // 3. Flows Aset / FX / Tools
    await tab('aset');
    await page.getByRole('button', { name: 'Tukar IDR / USD', exact: true }).click();
    await page.locator('#fx-amount').fill('1000000');
    await page.getByRole('button', { name: 'Tinjau konversi', exact: true }).click();
    await opened('fx-review', 'Terima bersih');

    await page.getByRole('button', { name: 'Konfirmasi konversi', exact: true }).click();
    await opened('fx-receipt', 'Debit / kredit bersih');
    await back();

    await tab('aset');
    await page.locator('#asset-tools > summary').click();
    await page.getByRole('button', { name: 'Fasilitas margin & risiko', exact: true }).click();
    await opened('risk-locked', 'Ekuitas bersih');
    const loanDisabled = await page.getByRole('button', { name: /^Tarik pinjaman margin/ }).isDisabled();
    if (!loanDisabled) throw new Error('Margin loan button should be disabled for novice');
    await back();

    await tab('aset');
    await page.locator('#asset-tools > summary').click();
    await page.getByRole('button', { name: 'Aksi korporasi & voting RUPS', exact: true }).click();
    await opened('corporate-voting', 'Agenda voting');
    await back();

    await tab('aset');
    await page.locator('#asset-tools > summary').click();
    await page.getByRole('button', { name: 'Uji stres portofolio', exact: true }).click();
    await page.getByRole('button', { name: 'Jalankan simulasi dampak', exact: true }).first().click();
    await opened('stress-results', 'Potensi kerugian');
    await back();

    await tab('aset');
    await page.locator('#asset-tools > summary').click();
    await page.getByRole('button', { name: 'Lab strategi & sandbox what-if', exact: true }).click();
    const snapBeforeLab = (await call('getSnapshot')).personalAccount;
    await page.getByRole('button', { name: 'Eksekusi transaksi lab', exact: true }).click();
    await opened('lab-result', 'Transaksi lab');
    const snapAfterLab = (await call('getSnapshot')).personalAccount;
    if (JSON.stringify(snapBeforeLab) !== JSON.stringify(snapAfterLab)) {
        throw new Error('Lab trade must not mutate primary personal account');
    }
    await back();

    await tab('aset');
    await page.locator('#asset-tools > summary').click();
    await page.getByRole('button', { name: 'Replay historis', exact: true }).click();
    await opened('replay-datasets', 'Dataset');
    const datasets = (await call('getHistoricalDatasets')).datasets || [];
    if (await page.locator('#replay-dataset option').count() !== datasets.length) throw new Error('Replay dataset choices must match Java');
    if (await page.getByRole('button', {name:'Mulai replay baru',exact:true}).isEnabled() !== Boolean(datasets.length)) throw new Error('Replay availability must match Java');
    await back();

    // 4. Flows Beranda
    await tab('beranda');
    await page.locator('#btn-briefing').click();
    await opened('briefing', 'Mode waktu');
    await back();

    await page.locator('#btn-reports').click();
    await opened('reports-empty', 'Belum ada hari');
    await back();

    await page.locator('#btn-entry-game').click();
    await opened('entry-checkpoint', 'Portofolio Pribadi');

    await page.getByRole('button', { name: 'Konfigurasi permainan baru', exact: true }).click();
    await opened('newgame-config', 'mereset');
    await back();

    await page.locator('#btn-settings').click();
    await opened('settings', 'Simpan permainan');

    await page.getByRole('button', { name: 'Panduan tutorial interaktif', exact: true }).click();
    await opened('tutorial', '5. Penutupan');
    await back();

    await page.locator('#context-account-btn').click();
    await opened('account-picker', 'Portofolio Pribadi');
    await back();

    // 5. Earn career rank PORTFOLIO_MANAGER naturally via fills, claims and skips
    await call('setClockState', true, 1);
    await page.evaluate(() => {const clock=JSON.parse(TradingNativeBridge.getSnapshot()).clock,d=new Date(clock.timestamp+7*3600000),seconds=d.getUTCHours()*3600+d.getUTCMinutes()*60+d.getUTCSeconds();TradingNativeBridge.tickClock(Math.ceil(((9*3600+30*60-seconds+86400)%86400)/clock.rate));});
    await call('setClockState', false, 1);

    for (const ticker of ['BBCA', 'TLKM']) {
        const payload = {
            requestId: 'UI-FLOWS-' + ticker,
            instrumentId: 'IDX:' + ticker,
            side: 'BUY',
            type: 'MARKET',
            quantity: 100
        };
        const res = await call('submitOrder', JSON.stringify(payload));
        if (res.status !== 'FILLED') throw new Error('Two real fills are required for diversification: ' + JSON.stringify(res));
    }
    await call('claimMission', 'MIS-01');
    await call('claimMission', 'MIS-02');

    for (let i = 0; i < 45; i++) {
        const currentRank = (await call('getSnapshot')).career.rank;
        if (currentRank === 'PORTFOLIO_MANAGER') break;
        await call('skipDay', 'UI-FLOWS-DAY-' + i);
    }
    const finalRank = (await call('getSnapshot')).career.rank;
    if (finalRank !== 'PORTFOLIO_MANAGER') {
        throw new Error('Failed to reach PORTFOLIO_MANAGER rank: currently ' + finalRank);
    }

    // 6. Firma / Bisnis flows with earned rank
    await tab('firma');
    await page.locator('#firma-subtab-dana').click();
    await page.getByRole('button', { name: 'Fund Builder & alokasi portofolio', exact: true }).click();
    await opened('fund-builder', 'Rencana tersimpan');
    await back();

    await tab('firma');
    await page.locator('#firma-subtab-dana').click();
    await page.getByRole('button', { name: /^Yayasan Dana Sejahtera/ }).click();
    await opened('mandate-offer', 'High-water mark');

    await page.getByRole('button', { name: 'Terima Mandat Kelolaan Klien', exact: true }).click();
    const snapWithClient = await call('getSnapshot');
    if (!snapWithClient.tradingAccounts.some(a => a.type === 'CLIENT')) {
        throw new Error('Client account missing after mandate acceptance');
    }
    await back();

    await tab('firma');
    await page.locator('#firma-subtab-dana').click();
    await page.getByRole('button', { name: 'Laporan klien & fee mandat', exact: true }).click();
    await opened('client-reports', 'Trust');
    await back();

    await tab('riset');
    await page.getByRole('button', { name: 'Lihat paket & opsi langganan', exact: true }).click();
    await opened('research-tiers', 'Basic');
    await back();

    if (errors.length > 0) throw new Error('Errors detected: ' + JSON.stringify(errors));

    await browser.close();

    const result = {
        status: 'PASS',
        checks,
        errors,
        scope: 'Browser + isolated Java; not native evidence'
    };
    fs.writeFileSync(path.join(outDir, 'extended-flow-result.json'), JSON.stringify(result, null, 2), 'utf-8');
    console.log(JSON.stringify({ status: 'PASS', extendedFlows: checks.length }));
})().catch(err => {
    console.error('FLOW VERIFICATION ERROR:', err);
    process.exit(1);
});
