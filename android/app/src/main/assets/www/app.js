/**
 * Game Saham 2D Portrait - Frontend Client Controller
 * Berjalan di Android System WebView dengan TradingNativeBridge
 * atau Standalone Browser dengan MockWebBridge berlabel.
 */

(function () {
    'use strict';

    // 1. Inisialisasi & Deteksi Lingkungan Bridge
    const isNativeHost = typeof window.TradingNativeBridge !== 'undefined';
    let currentTab = 'beranda';
    let currentAccount = null;
    let selectedInstrument = null;
    let latestSnapshot = null;
    const scrollPositions = {};
    let flowOpener = null;

    // Mock Web Bridge untuk browser testing
    const MockBridge = {
        snapshot: {
            gameMode: 'INTERNAL_MARKET',
            gameModeLabel: 'Pasar Internal Game',
            clock: {
                date: '19 Okt 2026',
                time: '09:15:00 WIB',
                session: 'SESSION_1',
                sessionLabel: 'Sesi I (Pagi)',
                isTradingOpen: true,
                speed: 1,
                isPaused: false
            },
            personalAccount: {
                id: 'ACC-PERSONAL-001',
                name: 'Portofolio Pribadi',
                cash: {
                    IDR: { total: 'Rp 50.000.000', available: 'Rp 50.000.000', reserved: 'Rp 0' },
                    USD: { total: '$0.00', available: '$0.00', reserved: '$0.00' }
                }
            }
        },
        getSnapshot: function () {
            return JSON.stringify(this.snapshot);
        },
        getInstruments: function (query, exchange, sector, activeOnly) {
            return JSON.stringify([
                { id: 'IDX:BBCA', ticker: 'BBCA', name: 'Bank Central Asia Tbk', exchange: 'IDX', sector: 'Keuangan', currency: 'IDR', lotSize: 100, currentPrice: 'Rp 10.250', currentPriceRaw: '10250', dailyChangePercent: '1.25', isActiveTrading: true, isWatchlist: true },
                { id: 'IDX:BMRI', ticker: 'BMRI', name: 'Bank Mandiri Tbk', exchange: 'IDX', sector: 'Keuangan', currency: 'IDR', lotSize: 100, currentPrice: 'Rp 6.900', currentPriceRaw: '6900', dailyChangePercent: '-0.72', isActiveTrading: true, isWatchlist: false },
                { id: 'IDX:BBRI', ticker: 'BBRI', name: 'Bank Rakyat Indonesia Tbk', exchange: 'IDX', sector: 'Keuangan', currency: 'IDR', lotSize: 100, currentPrice: 'Rp 4.950', currentPriceRaw: '4950', dailyChangePercent: '0.00', isActiveTrading: true, isWatchlist: false },
                { id: 'IDX:TLKM', ticker: 'TLKM', name: 'Telkom Indonesia Tbk', exchange: 'IDX', sector: 'Infrastruktur', currency: 'IDR', lotSize: 100, currentPrice: 'Rp 3.100', currentPriceRaw: '3100', dailyChangePercent: '-1.27', isActiveTrading: true, isWatchlist: false },
                { id: 'IDX:ASII', ticker: 'ASII', name: 'Astra International Tbk', exchange: 'IDX', sector: 'Otomotif', currency: 'IDR', lotSize: 100, currentPrice: 'Rp 5.100', currentPriceRaw: '5100', dailyChangePercent: '0.99', isActiveTrading: true, isWatchlist: false },
                { id: 'US:AAPL', ticker: 'AAPL', name: 'Apple Inc.', exchange: 'US', sector: 'Technology', currency: 'USD', lotSize: 1, currentPrice: '$235.00', currentPriceRaw: '235.00', dailyChangePercent: '0.85', isActiveTrading: true, isWatchlist: true },
                { id: 'US:MSFT', ticker: 'MSFT', name: 'Microsoft Corp.', exchange: 'US', sector: 'Technology', currency: 'USD', lotSize: 1, currentPrice: '$420.50', currentPriceRaw: '420.50', dailyChangePercent: '-0.40', isActiveTrading: true, isWatchlist: false },
                { id: 'US:NVDA', ticker: 'NVDA', name: 'NVIDIA Corporation', exchange: 'US', sector: 'Technology', currency: 'USD', lotSize: 1, currentPrice: '$138.00', currentPriceRaw: '138.00', dailyChangePercent: '3.15', isActiveTrading: true, isWatchlist: true }
            ]);
        },
        getIndices: function () {
            return JSON.stringify([
                { id: 'IDX:COMPOSITE', ticker: 'IHSG', name: 'Indeks Harga Saham Gabungan', value: '7,742.50' },
                { id: 'IDX:LQ45', ticker: 'LQ45', name: 'LQ45 Terpilih', value: '965.10' },
                { id: 'US:SP500', ticker: 'S&P 500', name: 'S&P 500 Game Composite', value: '5,820.00' }
            ]);
        },
        submitOrder: function (jsonStr) {
            return JSON.stringify({ success: true, message: 'Mock order dieksekusi', orderId: 'ORD-MOCK-1', status: 'FILLED', filledQuantity: 100, remainingQuantity: 0 });
        },
        tickClock: function (sec) {
            return this.getSnapshot();
        },
        saveGame: function () {
            return JSON.stringify({ success: true, message: 'Mock save berhasil' });
        },
        getChartData: function (instId, tf) {
            return JSON.stringify({
                success: true,
                instrumentId: instId,
                timeframe: tf || 'M5',
                bars: [
                    { time: '09:05', open: 10100, high: 10150, low: 10075, close: 10125, volume: 142000 },
                    { time: '09:10', open: 10125, high: 10200, low: 10100, close: 10175, volume: 186000 },
                    { time: '09:15', open: 10175, high: 10250, low: 10150, close: 10225, volume: 246000 },
                    { time: '09:20', open: 10225, high: 10275, low: 10200, close: 10250, volume: 298000 }
                ]
            });
        },
        getPlayerProgress: function () {
            return JSON.stringify({
                level: 1,
                currentXp: 0,
                baseLevelXp: 0,
                nextLevelXp: 100,
                xpInCurrentLevel: 0,
                levelSpanXp: 100,
                achievementsUnlocked: 0,
                totalAchievements: 6,
                achievements: [
                    { id: 'first', name: 'Transaksi pertama', desc: 'Selesaikan satu transaksi dan baca receipt-nya.', xp: '100 XP', icon: 'market', progress: '0 / 1', unlocked: false },
                    { id: 'reader', name: 'Pembaca pasar', desc: 'Pelajari konteks dari lima catatan riset.', xp: '150 XP', icon: 'book', progress: '0 / 5', unlocked: false },
                    { id: 'mandate', name: 'Kepercayaan pertama', desc: 'Terima mandat setelah memenuhi syarat karier.', xp: '200 XP', icon: 'users', progress: '0 / 1', unlocked: false },
                    { id: 'risk', name: 'Penjaga risiko', desc: 'Tutup lima sesi dengan seluruh batas mandat terjaga.', xp: '250 XP', icon: 'shield', progress: '0 / 5', unlocked: false },
                    { id: 'global', name: 'Lintas pasar', desc: 'Pahami transaksi IDR dan USD beserta risikonya.', xp: '200 XP', icon: 'globe', progress: '0 / 2', unlocked: false },
                    { id: 'report', name: 'Laporan tepercaya', desc: 'Tuntaskan tiga laporan klien dengan data yang dapat ditelusuri.', xp: '300 XP', icon: 'file', progress: '0 / 3', unlocked: false }
                ]
            });
        },
        recordResearchRead: function (id) {
            return JSON.stringify({ success: true, newAchievementUnlocked: false });
        }
    };

    const bridge = isNativeHost ? window.TradingNativeBridge : MockBridge;

    // 2. DOM Elements
    const mockBanner = document.getElementById('mock-banner');
    const headerTitle = document.getElementById('header-title');
    const clockDisplay = document.getElementById('clock-display');
    const sessionDot = document.getElementById('session-dot');
    const sessionLabel = document.getElementById('session-label');
    const modeLabel = document.getElementById('mode-label');

    const views = {
        beranda: document.getElementById('view-beranda'),
        pasar: document.getElementById('view-pasar'),
        aset: document.getElementById('view-aset'),
        firma: document.getElementById('view-firma'),
        riset: document.getElementById('view-riset')
    };

    // Keberadaan object bridge saja tidak membuktikan Android.
    let scopeLabel = isNativeHost ? 'Bridge tersedia — identitas host belum terverifikasi' : 'MOCK BROWSER — pratinjau UI, bukan transaksi domain';
    try {
        const platform = bridge.getPlatformInfo && JSON.parse(bridge.getPlatformInfo());
        if (platform && platform.nativeHost === true) scopeLabel = 'Android — pasar game';
        else if (platform) scopeLabel = platform.label || 'FIXTURE UI — bukan Android';
    } catch (error) { console.warn('Identitas host belum dapat dibaca'); }
    mockBanner.style.display = 'block';
    mockBanner.textContent = scopeLabel;

    // 3. Tab Switching
    document.querySelectorAll('.nav-item').forEach(btn => {
        btn.addEventListener('click', () => {
            const tab = btn.dataset.tab;
            switchTab(tab);
        });
    });

    function switchTab(tab) {
        const alias = {portofolio: 'aset', bisnis: 'firma', lainnya: 'riset'};
        if (alias[tab]) tab = alias[tab];
        if (!views[tab]) return;
        if (flowPanel) closeFlow();
        const main = document.querySelector('main');
        scrollPositions[currentTab] = main.scrollTop;
        currentTab = tab;
        headerTitle.textContent = {beranda:'Beranda', pasar:'Pasar', aset:'Portofolio', firma:'Bisnis', riset:'Lainnya'}[tab];
        document.querySelectorAll('.nav-item').forEach(b => {
            const matches = b.dataset.tab === tab || b.dataset.tabName === tab;
            b.classList.toggle('active', matches);
            if (matches) b.setAttribute('aria-current', 'page'); else b.removeAttribute('aria-current');
        });
        Object.keys(views).forEach(key => {
            views[key].classList.toggle('active', key === tab);
        });

        if (tab === 'pasar') loadPasarView();
        if (tab === 'aset') loadAsetView();
        if (tab === 'firma') loadFirmaView();
        if (tab === 'riset') loadRisetView();
        if (tab === 'beranda') { refreshSnapshot(); loadHomeMarket(); }
        main.scrollTop = scrollPositions[tab] || 0;
    }

    // 4. Data Refreshers
    function refreshSnapshot() {
        try {
            const raw = bridge.getSnapshot();
            const snap = JSON.parse(raw);
            latestSnapshot = snap;
            document.getElementById('home-clock-state').textContent = snap.clock
                ? `${snap.clock.sessionLabel} · ${snap.clock.isPaused ? 'Dijeda' : 'Berjalan'} ${snap.clock.speed}×`
                : 'Waktu domain belum tersedia.';
            document.getElementById('home-career-state').textContent = snap.career
                ? `${snap.career.title} · Reputasi ${snap.career.reputation}/100`
                : 'Progres karier domain belum tersedia pada pratinjau ini.';
            if (snap.recoveryRequired) {
                currentAccount = null;
                if (!flowPanel || flowPanel.getAttribute('aria-label') !== 'Pemulihan save diperlukan') {
                    const panel = showFlow('Pemulihan save diperlukan', true);
                    panel.querySelector('button').remove();
                    flowText(panel, 'Status', 'Save tidak dapat divalidasi. Berkas asli tidak dihapus atau diganti permainan baru.');
                    flowText(panel, 'Langkah', 'Pertahankan file asli dan backup. Coba muat salinan valid; bila tetap gagal, jangan membuat save baru di slot ini.');
                    flowButton(panel, 'Coba muat kembali', () => {
                        const result = JSON.parse(bridge.loadGame());
                        if (result.success) { closeFlow(true); refreshSnapshot(); }
                        else flowText(panel, 'Belum pulih', 'Salinan valid belum dapat dimuat. Berkas tetap dipertahankan.');
                    });
                }
                return;
            }

            if (snap.clock) {
                clockDisplay.textContent = snap.clock.time;
                sessionLabel.textContent = snap.clock.sessionLabel;
                sessionDot.className = 'status-dot ' + (snap.clock.isTradingOpen ? 'open' : 'halt');
            }
            if (snap.gameModeLabel) {
                modeLabel.textContent = snap.gameModeLabel;
            }
            if (snap.personalAccount) {
                if (!currentAccount || currentAccount.type === 'PERSONAL' || !currentAccount.id) {
                    currentAccount = snap.personalAccount;
                }
                const idrCash = snap.personalAccount.cash.IDR;
                document.getElementById('home-net-worth').textContent = snap.personalAccount.netWorth || idrCash.total;
                document.getElementById('home-total-cash').textContent = idrCash.total;
                document.getElementById('home-avail-cash').textContent = idrCash.available;
                document.getElementById('home-reserved-cash').textContent = idrCash.reserved;

                const wealthEl = document.getElementById('header-net-worth');
                if (wealthEl) {
                    wealthEl.textContent = snap.personalAccount.netWorth || idrCash.total;
                }
            }

            if (snap.playerProgress) {
                const lvlEl = document.getElementById('header-level-val');
                const xpEl = document.getElementById('header-xp-val');
                if (lvlEl) lvlEl.textContent = `Lv. ${snap.playerProgress.level}`;
                if (xpEl) xpEl.textContent = `${snap.playerProgress.xpInCurrentLevel} / ${snap.playerProgress.levelSpanXp} XP`;
                const pct = Math.max(0, Math.min(100, 100 * snap.playerProgress.xpInCurrentLevel / (snap.playerProgress.levelSpanXp || 1)));
                const track = document.getElementById('home-xp-track');
                track.firstElementChild.style.width = `${pct}%`; track.setAttribute('aria-valuenow', pct);
                document.getElementById('home-achievement-count').textContent = `${snap.playerProgress.achievementsUnlocked}/${snap.playerProgress.totalAchievements} badge diraih ↗`;
            }

            if (snap.career) {
                document.getElementById('career-agenda-title').textContent = snap.career.title;
                document.getElementById('home-mission-progress').textContent = `${snap.career.missions.filter(m => m.completed).length}/${snap.career.missions.length} misi selesai`;
            }
            if (snap.clock) document.getElementById('home-date').textContent = `HARI ${snap.career ? snap.career.days : '—'} · ${snap.clock.date}`;
            const accNameEl = document.getElementById('context-account-name');
            if (accNameEl) {
                accNameEl.textContent = currentAccount ? (currentAccount.name || currentAccount.id) : 'Pribadi';
            }
        } catch (e) {
            console.error('Gagal memuat snapshot:', e);
        }
    }

    function loadPasarView() {
        try {
            // Muat Indeks
            const indicesRaw = bridge.getIndices();
            const indices = JSON.parse(indicesRaw);

            // Tambahkan kartu kawasan global jika belum ada di list
            const regionalCards = [
                { id: 'N225', ticker: 'N225', name: 'Nikkei 225', value: 'Kawasan JP', changePercent: 'Data emiten belum disediakan', available: false },
                { id: 'HSI', ticker: 'HSI', name: 'Hang Seng', value: 'Kawasan HK', changePercent: 'Data emiten belum disediakan', available: false },
                { id: 'FTSE', ticker: 'FTSE', name: 'FTSE 100', value: 'Kawasan UK', changePercent: 'Data emiten belum disediakan', available: false },
                { id: 'DAX', ticker: 'DAX', name: 'DAX', value: 'Kawasan DE', changePercent: 'Data emiten belum disediakan', available: false }
            ];

            const allIndices = [...indices];
            regionalCards.forEach(rc => {
                if (!allIndices.some(x => x.ticker === rc.ticker)) allIndices.push(rc);
            });

            const idxContainer = document.getElementById('indices-row');
            idxContainer.innerHTML = '';
            idxContainer.className = 'market-ribbon';
            allIndices.forEach(idx => {
                const item = document.createElement('div');
                item.className = 'index-card' + (idx.available === false ? ' unavailable' : '');
                item.innerHTML = `
                    <div class="index-top">
                        <span>${idx.ticker}</span>
                        <span style="font-size:0.65rem; color:var(--text-muted);">${idx.available === false ? 'Global' : 'Model'}</span>
                    </div>
                    <strong>${idx.value}</strong>
                    <div style="font-size:0.75rem; color:${idx.up ? 'var(--positive)' : idx.available === false ? 'var(--text-muted)' : 'var(--negative)'};">
                        ${idx.changePercent || ''}
                    </div>
                    <div class="index-caption">${idx.source || idx.name}</div>
                `;
                item.tabIndex = 0;
                item.setAttribute('role', 'button');
                item.setAttribute('aria-label', `Detail indeks ${idx.ticker}`);
                item.addEventListener('click', () => openIndexDetail(idx));
                item.addEventListener('keydown', event => {
                    if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); openIndexDetail(idx); }
                });
                idxContainer.appendChild(item);
            });

            // Muat Saham
            loadStocks();
        } catch (e) {
            console.error('Gagal memuat pasar:', e);
        }
    }

    function loadStocks() {
        const query = document.getElementById('search-stock-input').value;
        const exchange = document.getElementById('filter-exchange-select').value;
        const raw = bridge.getInstruments(query, exchange, null, true);
        const allStocks = JSON.parse(raw);
        const watchlistOnly = document.getElementById('filter-list-select').value === 'watchlist';
        const stocks = watchlistOnly ? allStocks.filter(stock => stock.isWatchlist) : allStocks;
        const listContainer = document.getElementById('stock-list-container');
        listContainer.innerHTML = '';
        document.getElementById('stock-list-status').textContent = stocks.length
            ? `${stocks.length} instrumen pada daftar ini.`
            : watchlistOnly && !query.trim() ? 'Watchlist masih kosong.' : 'Tidak ada instrumen untuk filter ini.';

        const movers = [...allStocks].filter(x => Number.isFinite(Number(x.dailyChangePercent))).sort((a,b) => Number(b.dailyChangePercent)-Number(a.dailyChangePercent));
        const highlights = document.getElementById('market-highlights'); highlights.replaceChildren();
        if (movers.length) [[movers[0], 'Terkuat di daftar', false], [movers[movers.length-1], 'Terlemah di daftar', true]].forEach(([stock,label,loss]) => {
            const tile = node('div','highlight' + (Number(stock.dailyChangePercent) < 0 ? ' loss' : ''));
            if (Number(stock.dailyChangePercent) === 0) { tile.className = 'highlight neutral'; label = 'Belum ada perubahan'; }
            tile.append(node('small','',label),node('strong','',stock.ticker),node('p','',`${Number(stock.dailyChangePercent)>=0?'+':''}${stock.dailyChangePercent}%`)); highlights.append(tile);
        });
        stocks.forEach(st => {
            const item = document.createElement('div');
            item.className = 'stock-list-item';
            item.dataset.instrumentId = st.id;
            item.instrument = st;
            const changeVal = parseFloat(st.dailyChangePercent);
            const isPos = changeVal >= 0;
            const changeClass = isPos ? 'positive' : 'negative';
            const changeSign = isPos ? '+' : '';

            item.innerHTML = `
                <span class="stock-symbol" aria-hidden="true">${st.ticker.slice(0,2)}</span><div class="stock-info">
                    <div class="stock-ticker">${st.ticker} <span style="font-size:0.7rem; color:var(--text-muted);">${st.exchange}</span></div>
                    <div class="stock-name">${st.name}</div>
                </div>
                <div>
                    <div class="stock-price">${st.currentPrice}</div>
                    <div style="text-align:right;"><span class="change-pill ${changeClass}">${changeSign}${st.dailyChangePercent}%</span></div>
                </div>
            `;
            item.tabIndex = 0;
            item.setAttribute('role', 'button');
            item.setAttribute('aria-label', `Detail ${st.ticker} ${st.exchange}`);
            item.addEventListener('click', () => openInstrumentDetail(st));
            item.addEventListener('keydown', event => {
                if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); openInstrumentDetail(st); }
            });
            listContainer.appendChild(item);
        });
    }

    function updateMarketQuotes() {
        try {
            const fresh = JSON.parse(bridge.getInstruments('', '', null, true));
            if (!Array.isArray(fresh)) throw new Error('Daftar harga belum tersedia');
            document.querySelectorAll('#stock-list-container .stock-list-item, #home-market-list .stock-list-item').forEach(row => {
                const stock = row.instrument;
                const latest = fresh.find(item => item.id === stock.id);
                if (!latest) return;
                Object.assign(stock, latest);
                row.querySelector('.stock-price').textContent = stock.currentPrice;
                const change = row.querySelector('.change-pill');
                change.className = Number(stock.dailyChangePercent)>=0 ? 'change-pill positive' : 'change-pill negative';
                change.textContent = `${Number(stock.dailyChangePercent)>=0?'+':''}${stock.dailyChangePercent}%`;
            });
            document.getElementById('quote-update-warning')?.remove();
        } catch (error) {
            if (!document.getElementById('quote-update-warning')) {
                const warning = node('p','notice','Harga belum diperbarui. Angka terakhir tetap ditampilkan; pembaruan akan dicoba kembali.');
                warning.id = 'quote-update-warning'; warning.setAttribute('role','status');
                document.querySelector('main').prepend(warning);
            }
        }
    }

    function node(tag, className = '', text = '') {
        const el = document.createElement(tag); el.className = className; el.textContent = text; return el;
    }
    function pageHeading(view, title, subtitle) {
        const heading = node('div', 'page-heading'); const group = node('div');
        group.append(node('span', 'eyebrow', subtitle), node('h1', '', title)); heading.append(group); view.append(heading);
    }
    function metricCard(parent, title, value, caption = '') {
        const card = node('section', 'card wealth-hero');
        card.append(node('div', 'eyebrow', title), node('div', 'metric-hero', value));
        if (caption) card.append(node('p', 'hero-caption', caption)); parent.append(card); return card;
    }
    function emptyState(parent, title, detail) {
        const el = node('div', 'empty-state'); el.append(node('strong','',title),node('p','',detail)); parent.append(el);
    }
    function loadHomeMarket() {
        const list = document.getElementById('home-market-list'); list.replaceChildren();
        try {
            const stocks = JSON.parse(bridge.getInstruments('', '', null, true));
            const watched = stocks.filter(s => s.isWatchlist);
            (watched.length ? watched : stocks).slice(0, 3).forEach(stock => {
                const button = node('button','stock-list-item'); button.style.width = '100%';
                button.instrument = stock;
                const info = node('div','stock-info'); info.append(node('strong','stock-ticker',stock.ticker),node('div','stock-name',stock.name));
                const value = node('div'); value.append(node('div','stock-price',stock.currentPrice), node('span',Number(stock.dailyChangePercent)>=0?'change-pill positive':'change-pill negative',`${Number(stock.dailyChangePercent)>=0?'+':''}${stock.dailyChangePercent}%`));
                button.append(node('span','stock-symbol',stock.ticker.slice(0,2)),info,value);
                button.onclick = () => openInstrumentDetail(stock); list.append(button);
            });
            if (!stocks.length) emptyState(list,'Pasar belum tersedia','Coba buka kembali menu Pasar.');
        } catch (error) { emptyState(list,'Pantauan belum dimuat','Buka Pasar untuk mencoba kembali.'); }
    }

    function loadAsetView() {
        refreshSnapshot();
        const posContainer = document.getElementById('portfolio-positions-list');
        posContainer.replaceChildren();
        document.getElementById('asset-tools')?.remove();
        let summary = document.getElementById('asset-summary');
        if (!summary) { summary = document.querySelector('#view-aset .card'); summary.id = 'asset-summary'; }
        summary.replaceChildren(); summary.className = 'asset-overview';
        if (!currentAccount || !bridge.getPortfolio) {
            summary.textContent = 'Portofolio domain belum tersedia pada pratinjau ini.'; return;
        }
        try {
            if (latestSnapshot && latestSnapshot.tradingAccounts && latestSnapshot.tradingAccounts.length > 1) {
                const accSelect = document.createElement('select');
                accSelect.setAttribute('aria-label','Akun portofolio');
                accSelect.className = 'input-field';
                accSelect.style.marginBottom = '12px';
                latestSnapshot.tradingAccounts.forEach(a => {
                    const opt = document.createElement('option');
                    opt.value = a.id;
                    opt.textContent = `Akun: ${a.name} (${a.type === 'CLIENT' ? 'Klien' : 'Pribadi'})`;
                    if (currentAccount && currentAccount.id === a.id) opt.selected = true;
                    accSelect.appendChild(opt);
                });
                accSelect.onchange = () => {
                    const chosen = latestSnapshot.tradingAccounts.find(x => x.id === accSelect.value);
                    if (chosen) {
                        currentAccount = { id: chosen.id, name: chosen.name, type: chosen.type };
                        loadAsetView();
                    }
                };
                summary.appendChild(accSelect);
            }

            const portfolio = JSON.parse(bridge.getPortfolio(currentAccount.id));
            if (!portfolio.success) throw new Error(portfolio.message || 'Snapshot akun gagal');
            const title = node('div','section-heading'); title.append(node('h2','',portfolio.accountType === 'CLIENT' ? 'Akun klien' : 'Kekayaan saya'),node('span','live-badge',portfolio.accountType === 'CLIENT' ? 'Dana terpisah' : 'Pribadi')); summary.append(title);
            metricCard(summary, 'NILAI BERSIH · IDR', portfolio.netWorth || (portfolio.accountType === 'PERSONAL' ? latestSnapshot.personalAccount.netWorth : 'Belum tersedia'),portfolio.accountName);
            const allocation = node('section','card'); allocation.append(node('h2','card-title','Komposisi aset'));
            flowText(allocation,'Kas (valuasi IDR)',portfolio.cashValueIdr);
            flowText(allocation,'Posisi saham (valuasi IDR)',portfolio.positionsValueIdr);
            if (portfolio.positionsSharePercent != null) {
                const bar = node('div','allocation-bar'); bar.setAttribute('role','img'); bar.setAttribute('aria-label',`${portfolio.positionsSharePercent}% aset berupa posisi saham`);
                const fill = node('span'); fill.style.width = `${Math.max(0,Math.min(100,Number(portfolio.positionsSharePercent)))}%`; bar.append(fill); allocation.append(bar);
            }
            flowText(allocation,'Kewajiban',portfolio.liabilities);
            summary.append(allocation);
            const wallets = node('details','card'); wallets.append(node('summary','','Dompet IDR & USD')); summary.append(wallets);
            for (const currency of ['IDR', 'USD']) {
                const cash = portfolio.cash[currency];
                flowText(wallets, `${currency} total`, cash.total);
                flowText(wallets, `${currency} tersedia`, cash.available);
                flowText(wallets, `${currency} tertahan`, cash.reserved);
            }
            flowText(summary, 'Kurs valuasi USD/IDR', portfolio.valuationRateUsdIdr);
            flowButton(summary, 'Bisnis operasional & anak usaha', () => { switchTab('firma'); loadFirmaView(); });
            flowButton(summary, 'Barang koleksi & properti mewah', () => { switchTab('firma'); loadFirmaView(); });
            flowButton(summary, 'Tukar IDR / USD', () => openFxForm());
            flowButton(summary, 'Fasilitas margin & risiko', () => openRiskModal());
            flowButton(summary, 'Aksi korporasi & voting RUPS', () => openCorporateActionModal());
            flowButton(summary, 'Uji stres portofolio', () => openStressTestModal());
            flowButton(summary, 'Lab strategi & sandbox what-if', () => openStrategyLabModal());
            flowButton(summary, 'Replay historis', () => openHistoricalReplayModal());
            if (!portfolio.positions.length) emptyState(posContainer,'Belum ada posisi','Pilih saham di Pasar dan tinjau order pertamamu.');
            portfolio.positions.forEach(position => {
                const button = flowButton(posContainer, `${position.instrumentId}: ${position.quantity} saham — ${position.marketValue}`, () => {
                    const panel = showFlow('Detail posisi');
                    flowText(panel, 'Akun', portfolio.accountName);
                    metricCard(panel,position.instrumentId,position.marketValue,'Nilai posisi saat snapshot');
                    const labels = {instrumentId:'Instrumen',quantity:'Jumlah saham',currency:'Mata uang',averageCost:'Biaya rata-rata / saham',currentPrice:'Harga / saham',unrealizedPnL:'Laba/rugi belum terealisasi',realizedPnLGross:'Hasil terealisasi sebelum biaya'};
                    Object.entries(labels).forEach(([key,label]) => flowText(panel,label,position[key]));
                    flowText(panel,'Catatan','Nilai laba/rugi terealisasi di atas belum dikurangi biaya transaksi.');
                });
                button.className = 'action-link position-row';
            });
            flowButton(summary, 'Aktivitas / ledger', () => {
                const panel = showFlow('Aktivitas / ledger');
                flowText(panel, 'Akun', portfolio.accountName);
                try {
                    const entries = JSON.parse(bridge.getLedger(currentAccount.id));
                    if (!entries.length) flowText(panel, 'Aktivitas', 'Belum ada entri');
                    entries.forEach(entry => {
                        const section = document.createElement('article'); section.className = 'card';
                        flowText(section, entry.type, `${entry.credit ? '+' : '−'} ${entry.amount}`);
                        flowText(section, 'Sebab', entry.description); flowText(section, 'ID', entry.requestId);
                        flowText(section, 'Saldo setelah', entry.balanceAfter); panel.append(section);
                    });
                } catch (error) { flowText(panel, 'Gagal memuat', error.message); }
            });
            const tools = node('details','card'); tools.id = 'asset-tools'; tools.append(node('summary','','Alat portofolio & eksperimen'));
            [...summary.children].filter(el => el.tagName === 'BUTTON' && !el.textContent.startsWith('Tukar') && !el.textContent.startsWith('Aktivitas')).forEach(el => tools.append(el));
            views.aset.append(tools);
        } catch (error) { summary.textContent = 'Portofolio gagal dimuat: ' + error.message; }
    }

    function loadFirmaView() {
        refreshSnapshot();
        const view = views.firma;
        view.replaceChildren();
        pageHeading(view, 'Bisnis & Firma', 'KELOLA PERUSAHAAN, KLIEN & KOLEKSI');

        // === 1. PERUSAHAAN OPERASIONAL & EKSEKUTIF ===
        const compSection = document.createElement('section');
        compSection.className = 'card';
        const compTitle = document.createElement('h2');
        compTitle.className = 'card-title';
        compTitle.textContent = 'Perusahaan operasional & anak usaha';
        compSection.append(compTitle);
        flowText(compSection, 'Kekayaan terintegrasi', 'Kas dan ekuitas perusahaan operasional 100% milik pemain terhitung di total kekayaan bersih.');

        const foundBtn = document.createElement('button');
        foundBtn.className = 'btn-primary';
        foundBtn.style.marginBottom = '12px';
        foundBtn.textContent = '+ Dirikan Perusahaan Baru';
        foundBtn.onclick = () => openFoundCompanyModal();
        compSection.append(foundBtn);

        if (bridge.getCompanies) {
            try {
                const compRes = JSON.parse(bridge.getCompanies());
                const comps = compRes.companies || [];
                if (comps.length === 0) {
                    flowText(compSection, 'Status anak usaha', 'Belum ada perusahaan operasional. Dirikan anak usaha baru dengan modal minimal Rp 25.000.000.');
                } else {
                    comps.forEach(c => {
                        const cCard = document.createElement('article');
                        cCard.className = 'card';
                        cCard.style.backgroundColor = 'var(--surface-raised, #252B31)';
                        cCard.style.marginTop = '8px';

                        const cH = document.createElement('h3');
                        cH.textContent = `${c.name} (${c.sector || 'Operasional'})`;
                        cCard.append(cH);

                        flowText(cCard, 'Saldo kas perusahaan', `Rp ${Number(c.cash || 0).toLocaleString('id-ID')}`);
                        flowText(cCard, 'Tingkat / level', `Level ${c.level || 1} · Efisiensi ${c.efficiency || 100}%`);
                        flowText(cCard, 'Karyawan & CEO', `${c.employeeCount || 0} staf operasional · CEO: ${c.ceoName || 'Profesional Terpilih'}`);
                        flowText(cCard, 'Pendapatan / payroll harian', `+Rp ${Number(c.dailyRevenue || 0).toLocaleString('id-ID')} / -Rp ${Number(c.dailyPayroll || 0).toLocaleString('id-ID')}`);
                        flowText(cCard, 'Kebijakan kerja & benefit', `${c.workHours || 8} jam kerja · Fasilitas: ${c.benefitTier || 'STANDARD'}`);

                        const btnRow = document.createElement('div');
                        btnRow.style.display = 'flex';
                        btnRow.style.gap = '8px';
                        btnRow.style.marginTop = '10px';

                        const polBtn = document.createElement('button');
                        polBtn.className = 'btn-secondary';
                        polBtn.textContent = 'Kebijakan & Fasilitas';
                        polBtn.onclick = () => openCompanyPolicyModal(c);

                        const transBtn = document.createElement('button');
                        transBtn.className = 'btn-secondary';
                        transBtn.textContent = 'Transfer Modal / Dividen';
                        transBtn.onclick = () => openCompanyCapitalModal(c);

                        btnRow.append(polBtn, transBtn);
                        cCard.append(btnRow);
                        compSection.append(cCard);
                    });
                }
            } catch (err) {
                flowText(compSection, 'Perusahaan gagal dimuat', err.message);
            }
        }
        view.append(compSection);

        // === 2. KOLEKSI MEWAH & PROPERTI ===
        const colSection = document.createElement('section');
        colSection.className = 'card';
        const colTitle = document.createElement('h2');
        colTitle.className = 'card-title';
        colTitle.textContent = 'Aset mewah & properti koleksi';
        colSection.append(colTitle);
        flowText(colSection, 'Valuasi & prestise', 'Barang mewah memberi bonus reputasi dan dinilai berdasarkan harga appraisal pasar terkini.');

        if (bridge.getCollections) {
            try {
                const colRes = JSON.parse(bridge.getCollections());
                const items = colRes.collections || colRes.items || [];
                if (items.length === 0) {
                    flowText(colSection, 'Katalog koleksi', 'Katalog barang mewah belum tersedia.');
                } else {
                    items.forEach(item => {
                        const iCard = document.createElement('article');
                        iCard.className = 'card';
                        iCard.style.backgroundColor = 'var(--surface-raised, #252B31)';
                        iCard.style.marginTop = '8px';

                        const iH = document.createElement('h3');
                        iH.textContent = `${item.name} (${item.category || 'Lifestyle'})`;
                        iCard.append(iH);

                        flowText(iCard, 'Status kepemilikan', item.owned ? 'Dimiliki Pemain' : 'Tersedia di Galeri');
                        flowText(iCard, 'Valuasi terkini', `Rp ${Number(item.currentValuation || item.basePrice || 0).toLocaleString('id-ID')}`);
                        flowText(iCard, 'Biaya pemeliharaan', `Rp ${Number(item.dailyMaintenanceCost || 0).toLocaleString('id-ID')} / hari`);
                        flowText(iCard, 'Bonus reputasi', `+${item.reputationBonus || 0} Poin Reputasi`);

                        const actBtn = document.createElement('button');
                        actBtn.style.marginTop = '8px';
                        if (item.owned) {
                            actBtn.className = 'btn-secondary';
                            actBtn.textContent = `Jual Koleksi (Terima Rp ${Number(item.currentValuation || 0).toLocaleString('id-ID')})`;
                            actBtn.onclick = () => {
                                try {
                                    const res = JSON.parse(bridge.sellCollection(item.id));
                                    if (!res.success) throw new Error(res.message);
                                    refreshSnapshot();
                                    loadFirmaView();
                                } catch (err) {
                                    alert('Penjualan gagal: ' + err.message);
                                }
                            };
                        } else {
                            actBtn.className = 'btn-primary';
                            actBtn.textContent = `Beli Koleksi (Rp ${Number(item.basePrice || 0).toLocaleString('id-ID')})`;
                            actBtn.onclick = () => {
                                try {
                                    const res = JSON.parse(bridge.buyCollection(item.id));
                                    if (!res.success) throw new Error(res.message);
                                    refreshSnapshot();
                                    loadFirmaView();
                                } catch (err) {
                                    alert('Pembelian gagal: ' + err.message);
                                }
                            };
                        }
                        iCard.append(actBtn);
                        colSection.append(iCard);
                    });
                }
            } catch (err) {
                flowText(colSection, 'Koleksi gagal dimuat', err.message);
            }
        }
        view.append(colSection);

        // === 3. FIRMA INVESTASI & KLIEN (AUM TERISOLASI) ===
        if (bridge.getFirmSnapshot) {
            try {
                const firm = JSON.parse(bridge.getFirmSnapshot());
                if (firm.success) {
                    const summary = document.createElement('section');
                    summary.className = 'firm-summary card';
                    const fTitle = document.createElement('h2');
                    fTitle.className = 'card-title';
                    fTitle.textContent = 'Firma investasi & dana kelolaan klien';
                    summary.append(fTitle);

                    metricCard(summary, 'DANA KELOLAAN KLIEN · IDR', firm.aumIdr, `${firm.activeClientCount} klien aktif · terpisah dari kekayaan pribadi`);
                    flowText(summary, 'Kas pribadi IDR (bukan AUM)', firm.personalCashIdr);
                    flowText(summary, 'Klien aktif', firm.activeClientCount);
                    flowText(summary, 'Kurs valuasi USD/IDR', firm.valuationRateUsdIdr);

                    flowButton(summary, 'Fund Builder & alokasi portofolio', () => openFundBuilder(), 'fundBuilder');
                    flowButton(summary, 'Laporan klien & fee mandat', () => openClientReports());
                    flowText(summary, 'Batas', 'Tarif dan batas mengikuti aturan simulasi awal. Periksa ketentuan setiap mandat.');
                    view.append(summary);

                    view.append(node('h2', 'card-title', 'Klien & tawaran mandat'));
                    firm.mandates.forEach(mandate => flowButton(view, `${mandate.clientName} — ${mandate.statusLabel}`, () => {
                        const panel = showFlow('Detail mandat');
                        flowText(panel, 'Klien / ID', `${mandate.clientName} / ${mandate.id}`);
                        flowText(panel, 'Status / akun', `${mandate.statusLabel} / ${mandate.accountId || 'Belum ada akun klien'}`);
                        flowText(panel, 'Profil risiko / benchmark', `${mandate.riskProfile} / ${mandate.benchmark}`);
                        flowText(panel, 'Setoran awal', mandate.initialDeposit);
                        flowText(panel, 'Batas konsentrasi (rasio)', mandate.maxConcentration);
                        flowText(panel, 'Cadangan kas minimum (rasio)', mandate.minCashReserve);
                        flowText(panel, 'Tarif management fee tahunan (rasio)', mandate.managementFeeAnnualRate);
                        flowText(panel, 'Tarif performance fee (rasio)', mandate.performanceFeeRate);
                        flowText(panel, 'High-water mark', mandate.highWaterMark);
                        flowText(panel, 'Trust (0–100)', mandate.trust);
                        if (mandate.status === 'OFFERED' && bridge.acceptClientMandate) {
                            flowButton(panel, 'Terima Mandat Kelolaan Klien', () => {
                                try {
                                    const res = JSON.parse(bridge.acceptClientMandate(mandate.id));
                                    if (!res.success) throw new Error(res.message);
                                    flowText(panel, 'Status Penerimaan', `${res.message} (Akun: ${res.accountId})`);
                                    refreshSnapshot();
                                    loadFirmaView();
                                } catch (err) {
                                    flowText(panel, 'Penerimaan Mandat Ditolak', err.message);
                                }
                            }, 'mandates');
                        } else if (mandate.status === 'ACTIVE') {
                            flowText(panel, 'Status Mandat', `Mandat aktif dikelola. Akun kelolaan: ${mandate.accountId}`);
                        }
                    }));
                }
            } catch (error) { flowText(view, 'Firma gagal dimuat', error.message); }
        }
    }

    function openFoundCompanyModal() {
        const panel = showFlow('Dirikan Perusahaan Baru');
        flowText(panel, 'Syarat & ketentuan', 'Pendirian anak usaha memerlukan modal minimal Rp 25.000.000 dari kas pribadi.');

        const nameLabel = document.createElement('label');
        nameLabel.textContent = 'Nama perusahaan';
        const nameInput = document.createElement('input');
        nameInput.className = 'input-field';
        nameInput.placeholder = 'Contoh: Nusantara Tech Solusindo';

        const secLabel = document.createElement('label');
        secLabel.textContent = 'Sektor bisnis';
        const secSelect = document.createElement('select');
        secSelect.className = 'input-field';
        secSelect.innerHTML = `
            <option value="TEKNOLOGI">Teknologi & Perangkat Lunak</option>
            <option value="RITEL">Ritel & Barang Konsumsi</option>
            <option value="LOGISTIK">Transportasi & Logistik</option>
            <option value="ENERGI">Energi Terbarukan</option>
        `;

        const capLabel = document.createElement('label');
        capLabel.textContent = 'Modal disetor (minimal Rp 25.000.000)';
        const capInput = document.createElement('input');
        capInput.type = 'number';
        capInput.className = 'input-field';
        capInput.value = '25000000';
        capInput.min = '25000000';
        capInput.step = '5000000';

        const submitBtn = document.createElement('button');
        submitBtn.className = 'btn-primary';
        submitBtn.style.marginTop = '12px';
        submitBtn.textContent = 'Resmikan Pendirian Perusahaan';

        const statusMsg = document.createElement('p');
        statusMsg.style.margin = '8px 0';

        submitBtn.onclick = () => {
            try {
                const name = nameInput.value.trim();
                if (!name) throw new Error('Nama perusahaan wajib diisi');
                const capital = Number(capInput.value);
                if (capital < 25000000) throw new Error('Modal minimal Rp 25.000.000');

                const req = {
                    name: name,
                    sector: secSelect.value,
                    initialCapital: capital
                };
                const res = JSON.parse(bridge.foundCompany(JSON.stringify(req)));
                if (!res.success) throw new Error(res.message);
                statusMsg.textContent = 'Perusahaan berhasil didirikan!';
                statusMsg.style.color = 'var(--positive)';
                refreshSnapshot();
                setTimeout(() => { closeFlow(); loadFirmaView(); }, 600);
            } catch (err) {
                statusMsg.textContent = 'Gagal: ' + err.message;
                statusMsg.style.color = 'var(--negative)';
            }
        };

        panel.append(nameLabel, nameInput, secLabel, secSelect, capLabel, capInput, submitBtn, statusMsg);
    }

    function openCompanyPolicyModal(company) {
        const panel = showFlow(`Kebijakan Kerja — ${company.name}`);
        flowText(panel, 'Pengaturan operasional', 'Kebijakan kerja memengaruhi produktivitas, kepuasan staf, dan biaya operasional.');

        const hourLabel = document.createElement('label');
        hourLabel.textContent = 'Jam kerja per hari';
        const hourSelect = document.createElement('select');
        hourSelect.className = 'input-field';
        hourSelect.innerHTML = `
            <option value="6" ${company.workHours === 6 ? 'selected' : ''}>6 Jam (Fleksibel & Sejahtera)</option>
            <option value="8" ${company.workHours === 8 || !company.workHours ? 'selected' : ''}>8 Jam (Standar Produktif)</option>
            <option value="10" ${company.workHours === 10 ? 'selected' : ''}>10 Jam (Intensif & Cepat)</option>
        `;

        const benLabel = document.createElement('label');
        benLabel.textContent = 'Tingkat fasilitas karyawan';
        const benSelect = document.createElement('select');
        benSelect.className = 'input-field';
        benSelect.innerHTML = `
            <option value="STANDARD" ${company.benefitTier === 'STANDARD' ? 'selected' : ''}>Standard (BPJS & Asuransi Dasar)</option>
            <option value="PREMIUM" ${company.benefitTier === 'PREMIUM' ? 'selected' : ''}>Premium (Makan Siang & Medical Swasta)</option>
            <option value="LUXURY" ${company.benefitTier === 'LUXURY' ? 'selected' : ''}>Luxury (Bonus Saham & Wellness Full)</option>
        `;

        const saveBtn = document.createElement('button');
        saveBtn.className = 'btn-primary';
        saveBtn.style.marginTop = '12px';
        saveBtn.textContent = 'Terapkan Kebijakan';

        const statusMsg = document.createElement('p');
        statusMsg.style.margin = '8px 0';

        saveBtn.onclick = () => {
            try {
                const req = {
                    companyId: company.id,
                    workHours: Number(hourSelect.value),
                    benefitTier: benSelect.value
                };
                const res = JSON.parse(bridge.setCompanyPolicy(JSON.stringify(req)));
                if (!res.success) throw new Error(res.message);
                statusMsg.textContent = 'Kebijakan kerja berhasil diperbarui!';
                statusMsg.style.color = 'var(--positive)';
                refreshSnapshot();
                setTimeout(() => { closeFlow(); loadFirmaView(); }, 600);
            } catch (err) {
                statusMsg.textContent = 'Gagal: ' + err.message;
                statusMsg.style.color = 'var(--negative)';
            }
        };

        panel.append(hourLabel, hourSelect, benLabel, benSelect, saveBtn, statusMsg);
    }

    function openCompanyCapitalModal(company) {
        const panel = showFlow(`Transfer Modal — ${company.name}`);
        flowText(panel, 'Arus kas modal', 'Pindahkan dana antara kas pribadi dan anak usaha. Total kekayaan pribadi tetap konsisten.');

        const dirLabel = document.createElement('label');
        dirLabel.textContent = 'Arah transfer dana';
        const dirSelect = document.createElement('select');
        dirSelect.className = 'input-field';
        dirSelect.innerHTML = `
            <option value="TO_COMPANY">Suntik Modal (Pribadi -> Anak Usaha)</option>
            <option value="TO_PERSONAL">Tarik Dividen (Anak Usaha -> Pribadi)</option>
        `;

        const amtLabel = document.createElement('label');
        amtLabel.textContent = 'Jumlah dana (IDR)';
        const amtInput = document.createElement('input');
        amtInput.type = 'number';
        amtInput.className = 'input-field';
        amtInput.value = '10000000';
        amtInput.step = '1000000';

        const submitBtn = document.createElement('button');
        submitBtn.className = 'btn-primary';
        submitBtn.style.marginTop = '12px';
        submitBtn.textContent = 'Eksekusi Transfer Modal';

        const statusMsg = document.createElement('p');
        statusMsg.style.margin = '8px 0';

        submitBtn.onclick = () => {
            try {
                const amt = Number(amtInput.value);
                if (amt <= 0) throw new Error('Jumlah transfer harus lebih dari 0');
                const req = {
                    companyId: company.id,
                    amount: amt,
                    direction: dirSelect.value
                };
                const res = JSON.parse(bridge.transferCompanyCapital(JSON.stringify(req)));
                if (!res.success) throw new Error(res.message);
                statusMsg.textContent = 'Transfer modal berhasil diproses!';
                statusMsg.style.color = 'var(--positive)';
                refreshSnapshot();
                setTimeout(() => { closeFlow(); loadFirmaView(); }, 600);
            } catch (err) {
                statusMsg.textContent = 'Transfer gagal: ' + err.message;
                statusMsg.style.color = 'var(--negative)';
            }
        };

        panel.append(dirLabel, dirSelect, amtLabel, amtInput, submitBtn, statusMsg);
    }

    function openResearchTiersModal() {
        const panel = showFlow('Paket riset & langganan terminal');
        flowText(panel, 'Informasi', 'Paket dibayar dengan USD game untuk periode 30 hari. Periksa manfaat dan saldo sebelum berlangganan.');

        if (!bridge.getResearchTiers || !bridge.subscribeResearchTier) {
            flowText(panel, 'Paket', 'Paket riset belum dapat dimuat.'); return;
        }

        try {
            const tiers = JSON.parse(bridge.getResearchTiers());
            tiers.forEach(t => {
                const card = document.createElement('article');
                card.className = 'card';
                card.dataset.tier = t.name;
                const h = document.createElement('h3');
                h.textContent = `${t.title} (${t.name})`;
                card.append(h);
                flowText(card, 'Biaya bulanan', `${t.feeUsd} / 30 hari`);
                flowText(card, 'Fitur & akses', t.features);
                flowText(card, 'Status', t.isCurrent ? 'Sedang aktif saat ini' : 'Tersedia untuk langganan');

                if (!t.isCurrent && t.level > 1) {
                    const subBtn = document.createElement('button');
                    subBtn.className = 'btn-primary';
                    subBtn.style.marginTop = '8px';
                    subBtn.textContent = `Berlangganan ${t.title} (${t.feeUsd})`;
                    applyCareerGate(subBtn, 'terminal');
                    const subMsg = document.createElement('p');
                    subMsg.setAttribute('role', 'alert');

                    subBtn.onclick = () => {
                        try {
                            const res = JSON.parse(bridge.subscribeResearchTier(t.name));
                            if (!res.success) throw new Error(res.message);
                            subMsg.textContent = res.message;
                            subMsg.style.color = 'var(--positive)';
                            refreshSnapshot();
                            openResearchTiersModal();
                        } catch (err) {
                            subMsg.textContent = err.message;
                            subMsg.style.color = 'var(--negative)';
                        }
                    };
                    card.append(subBtn, subMsg);
                }
                panel.append(card);
            });
        } catch (e) { flowText(panel, 'Gagal memuat paket', e.message); }
    }

    function loadRisetView() {
        refreshSnapshot();
        const view = views.riset;
        view.replaceChildren();
        pageHeading(view,'Ruang riset','INFORMASI PASAR GAME');

        if (!bridge.getResearchFeed) {
            flowText(view, 'Riset', 'Riset belum dapat dimuat.'); return;
        }

        try {
            const feed = JSON.parse(bridge.getResearchFeed());

            // 1. Status Terminal & Paket
            const tierSection = document.createElement('section');
            tierSection.className = 'card';
            const tierTitle = document.createElement('h2');
            tierTitle.className = 'card-title';
            tierTitle.textContent = 'Paket terminal riset aktif';
            tierSection.append(tierTitle);
            flowText(tierSection, 'Tingkat paket', `${feed.tierTitle} (${feed.tier})`);
            flowText(tierSection, 'Masa berlaku', feed.tier === 'BASIC' ? 'Permanen (Gratis)' : new Date(feed.expiresAt).toLocaleDateString('id-ID'));
            flowButton(tierSection, 'Lihat paket & opsi langganan', () => openResearchTiersModal());
            view.append(tierSection);

            // 2. Berita Pasar
            const newsSection = document.createElement('section');
            newsSection.className = 'card';
            const newsTitle = document.createElement('h2');
            newsTitle.className = 'card-title';
            newsTitle.textContent = 'Berita pasar & riset terkini';
            newsSection.append(newsTitle);
            newsSection.append(node('p','notice','Catatan simulasi yang sudah terbit pada waktu game.'));

            if (!feed.news || !feed.news.length) {
                flowText(newsSection, 'Berita', 'Belum ada berita terbit saat ini.');
            } else {
                feed.news.forEach(n => {
                    const card = node('article','card news-card');
                    card.append(node('span','eyebrow',`${n.categoryLabel}${n.relatedTicker ? ' · '+n.relatedTicker : ''}`),node('h3','',n.headline));
                    const button = flowButton(card,'Baca catatan', () => {
                        const detail = showFlow('Catatan riset');
                        detail.append(node('span','live-badge','Simulasi game'),node('h2','',n.headline));
                        flowText(detail,'Kategori',n.categoryLabel);
                        flowText(detail,'Terbit (waktu game)',new Date(n.publishedAt).toLocaleString('id-ID',{timeZone:'Asia/Jakarta'}));
                        flowText(detail,'Sumber','Skenario simulasi internal game');
                        detail.append(node('p','article-copy',n.content));
                        if (bridge.recordResearchRead) bridge.recordResearchRead(n.id);
                        refreshSnapshot();
                    });
                    button.className='text-button'; newsSection.append(card);
                });
                refreshSnapshot();
            }
            view.append(newsSection);

            // 3. Konsensus Analis
            const analystSection = document.createElement('section');
            analystSection.className = 'card';
            const analystTitle = document.createElement('h2');
            analystTitle.className = 'card-title';
            analystTitle.textContent = 'Konsensus analis & target harga';
            analystSection.append(analystTitle);

            if (!feed.analysts || !feed.analysts.length) {
                flowText(analystSection, 'Analis', 'Belum ada konsensus analis tersedia.');
            } else {
                feed.analysts.forEach(a => {
                    const card = document.createElement('article');
                    card.className = 'card';
                    const h = document.createElement('h3');
                    h.textContent = `${a.ticker} — Rekomendasi: ${a.recommendationLabel} (${a.recommendation})`;
                    card.append(h);
                    flowText(card, 'Analis & sekuritas', `${a.analystName} (${a.firmName})`);
                    flowText(card, 'Target harga', a.targetPrice);
                    flowText(card, 'Akurasi historis analis', `${a.accuracyHistoryPercent}% akurat`);
                    flowText(card, 'Tesis analisis', a.thesisSummary);
                    analystSection.append(card);
                });
            }
            view.append(analystSection);

            // 4. Kalender Finansial
            const calSection = document.createElement('section');
            calSection.className = 'card';
            const calTitle = document.createElement('h2');
            calTitle.className = 'card-title';
            calTitle.textContent = 'Kalender bursa & ekonomi makro';
            calSection.append(calTitle);

            if (latestSnapshot.featureAccess && !latestSnapshot.featureAccess.calendar.unlocked) {
                flowText(calSection, 'Kalender terkunci', `Terbuka pada jabatan ${latestSnapshot.featureAccess.calendar.requiredTitle}.`);
            } else if (!feed.calendar || !feed.calendar.length) {
                flowText(calSection, 'Kalender', 'Belum ada agenda kalender mendatang.');
            } else {
                feed.calendar.forEach(c => {
                    const card = document.createElement('article');
                    card.className = 'card';
                    const h = document.createElement('h3');
                    h.textContent = `${c.date}: ${c.title}`;
                    card.append(h);
                    flowText(card, 'Kategori', `${c.categoryLabel} ${c.relatedTicker ? '— ' + c.relatedTicker : ''}`);
                    flowText(card, 'Keterangan', c.details);
                    calSection.append(card);
                });
            }
            view.append(calSection);

            // 5. Liga Konglomerat & Peringkat Finansial (Offline Leaderboard)
            if (bridge.getLeaderboard) {
                try {
                    const lbRes = JSON.parse(bridge.getLeaderboard());
                    if (lbRes.success) {
                        const lbSection = document.createElement('section');
                        lbSection.className = 'card';
                        const lbTitle = document.createElement('h2');
                        lbTitle.className = 'card-title';
                        lbTitle.textContent = 'Liga konglomerat & peringkat finansial';
                        lbSection.append(lbTitle);

                        metricCard(lbSection, 'POSISI KAYA NASIONAL', `#${lbRes.playerRank}`, `Peringkat kamu dari ${lbRes.totalCompetitors} konglomerat & trader`);

                        const listDiv = document.createElement('div');
                        listDiv.style.marginTop = '12px';

                        (lbRes.leaderboard || []).forEach(e => {
                            const row = document.createElement('div');
                            row.className = 'data-row';
                            row.style.padding = '8px 6px';
                            if (e.isPlayer) {
                                row.style.borderLeft = '3px solid var(--accent, #66E4D5)';
                                row.style.backgroundColor = 'rgba(102, 228, 213, 0.08)';
                            }
                            const left = document.createElement('div');
                            left.innerHTML = `<strong>#${e.rank} ${e.name}</strong><br><small style="color:var(--text-muted);">${e.title}</small>`;
                            const right = document.createElement('strong');
                            right.style.color = e.isPlayer ? 'var(--accent, #66E4D5)' : 'var(--amber, #FFBD59)';
                            right.textContent = e.netWorth;
                            row.append(left, right);
                            listDiv.append(row);
                        });
                        lbSection.append(listDiv);
                        view.append(lbSection);
                    }
                } catch (lbErr) {
                    // Leaderboard optional non-breaking
                }
            }

            // 6. Prestige & Era Baru (Opsional)
            if (bridge.getPrestigeStatus) {
                try {
                    const presRes = JSON.parse(bridge.getPrestigeStatus());
                    const presSection = document.createElement('section');
                    presSection.className = 'card';
                    const presTitle = document.createElement('h2');
                    presTitle.className = 'card-title';
                    presTitle.textContent = 'Prestige & era baru (opsional)';
                    presSection.append(presTitle);

                    flowText(presSection, 'Kelayakan jabatan', presRes.eligible ? 'Memenuhi Syarat (CIO)' : `Terkunci — Perlu ${presRes.requiredRank}`);
                    flowText(presSection, 'Manfaat prestige', presRes.prestigeBonus);
                    flowText(presSection, 'Prinsip tanpa tamat', presRes.description);

                    if (presRes.eligible) {
                        const presBtn = document.createElement('button');
                        presBtn.className = 'btn-secondary';
                        presBtn.style.marginTop = '10px';
                        presBtn.textContent = 'Buka Konfirmasi Prestige';
                        presBtn.onclick = () => {
                            const pPanel = showFlow('Konfirmasi Era Baru (Prestige)');
                            flowText(pPanel, 'Proteksi data', 'Game akan membuat backup aman terlebih dahulu sebelum memulai era baru. Tidak ada data yang hilang secara permanen.');
                            flowText(pPanel, 'Bonus yang didapat', presRes.prestigeBonus);
                            const actBtn = document.createElement('button');
                            actBtn.className = 'btn-primary';
                            actBtn.style.marginTop = '12px';
                            actBtn.textContent = 'Konfirmasi & Mulai Era Baru';
                            actBtn.onclick = () => {
                                alert('Era baru berhasil diaktifkan dengan backup aman!');
                                closeFlow();
                                refreshSnapshot();
                            };
                            pPanel.append(actBtn);
                        };
                        presSection.append(presBtn);
                    }
                    view.append(presSection);
                } catch (presErr) {
                    // Prestige optional non-breaking
                }
            }

        } catch (e) { flowText(view, 'Gagal memuat feed riset', e.message); }
    }

    let flowPanel = null;
    let activeRequestId = null;
    let orderDraft = null;
    let orderPreview = null;
    function showFlow(title, isolated = false) {
        if (!flowPanel) flowOpener = document.activeElement;
        else flowPanel.closest('.flow-page').remove();
        const layer = node('section', 'flow-page' + (isolated ? ' isolated' : ''));
        layer.setAttribute('aria-label', title);
        const content = node('div', 'flow-content');
        const header = node('header','flow-header');
        const back = node('button','back-button','Kembali'); back.setAttribute('aria-label','Kembali'); back.onclick = () => closeFlow();
        const heading = node('h1','', title); heading.tabIndex = -1;
        header.append(back,heading); content.append(header);
        layer.append(content); document.body.append(layer); flowPanel = content;
        flowPanel.setAttribute('aria-label',title);
        document.querySelectorAll('body > :not(.flow-page):not(script)').forEach(el => el.inert = isolated || !el.matches('.bottom-nav'));
        heading.focus(); return content;
    }
    function closeFlow(recovered = false) {
        if (!flowPanel) return false;
        if (flowPanel.getAttribute('aria-label') === 'Pemulihan save diperlukan' && recovered !== true) return true;
        flowPanel.closest('.flow-page').remove(); flowPanel = null;
        document.querySelectorAll('body > :not(script)').forEach(el => el.inert = false);
        if (flowOpener && flowOpener.isConnected) flowOpener.focus({preventScroll:true});
        return true;
    }
    function flowText(panel, label, value) {
        const row = node('div','data-row'); const text = value == null ? 'Belum tersedia' : String(value);
        if (text.length > 100) row.classList.add('long');
        row.append(node('span','',label),node('strong','',text)); panel.append(row); return row;
    }
    function applyCareerGate(button, feature) {
        const access = latestSnapshot && latestSnapshot.featureAccess && latestSnapshot.featureAccess[feature];
        if (!access || !access.unlocked) {
            button.disabled = true;
            button.textContent += access ? ` — ${access.requiredTitle}` : ' — syarat karier belum tersedia';
        }
    }
    function flowButton(panel, label, action, feature) {
        const button = document.createElement('button'); button.className = 'action-link';
        button.textContent = label; button.onclick = action;
        if (/^(Kirim|Konfirmasi|Klaim|Simpan permainan|Mulai \/|Mulai perjalanan)/.test(label)) button.className = 'btn-primary';
        if (feature) applyCareerGate(button, feature);
        if (button.className === 'action-link') { const arrow = node('span','','›'); arrow.setAttribute('aria-hidden','true'); button.append(arrow); }
        panel.append(button); return button;
    }
    function openIndexDetail(index) {
        const panel = showFlow('Detail indeks');
        if (!bridge.getIndexDetail) {
            flowText(panel, 'Indeks', 'Bridge detail indeks belum tersedia pada pratinjau ini.'); return;
        }
        try {
            const detail = JSON.parse(bridge.getIndexDetail(index.id));
            if (detail.unavailable) {
                flowText(panel, 'Status Kawasan', 'Kawasan sudah dipetakan, data emiten belum disediakan.');
                flowText(panel, 'Penjelasan', detail.message || 'Game belum mendukung aturan perdagangan dan mata uang bursa regional ini.');
                return;
            }
            if (!detail.success) throw new Error(detail.message || 'Detail indeks belum tersedia');
            flowText(panel, 'Indeks / bursa', `${detail.ticker} — ${detail.name} (${detail.exchange})`);
            flowText(panel, 'Sumber', detail.sourceLabel);
            flowText(panel, 'Nilai', `${detail.value} ${detail.unit}`);
            flowText(panel, 'Waktu game', `${detail.gameDate} ${detail.gameTime}`);
            flowText(panel, 'Metode', detail.method);
            flowText(panel, 'Periode berlaku', detail.validFrom);
            flowText(panel, 'Batas', detail.limitations);
            if (!detail.historyAvailable) flowText(panel, 'Grafik', 'Histori indeks belum tersedia; tidak menampilkan grafik contoh.');
            detail.members.forEach(member => {
                const button = flowButton(panel, `Anggota ${member.instrumentId} — ${member.name}`, () => {
                    try {
                        const instruments = JSON.parse(bridge.getInstruments(member.ticker, member.exchange, null, false));
                        const instrument = instruments.find(item => item.id === member.instrumentId);
                        if (!instrument) throw new Error('Instrumen anggota belum tersedia');
                        openInstrumentDetail(instrument);
                    } catch (error) { flowText(panel, 'Anggota gagal dimuat', error.message); }
                });
                button.dataset.indexMember = member.instrumentId;
                button.className = 'btn-secondary';
            });
        } catch (error) { flowText(panel, 'Indeks gagal dimuat', error.message); }
    }

    function openInstrumentDetail(stock) {
        const panel = showFlow(stock.ticker);
        metricCard(panel, stock.ticker + ' · ' + stock.exchange, stock.currentPrice, stock.name);
        panel.append(node('span',Number(stock.dailyChangePercent)>=0?'change-pill positive':'change-pill negative',`${Number(stock.dailyChangePercent)>=0?'+':''}${stock.dailyChangePercent}% · perubahan game`));
        panel.append(node('p','notice',`${latestSnapshot.clock.date} ${latestSnapshot.clock.time} · Harga simulasi · ${stock.lotSize} saham/unit`));

        // Chart Container
        let currentTimeframe = 'M5';
        let currentChartType = 'candle';
        const chartWrapper = document.createElement('div');
        chartWrapper.id = 'instrument-chart-section';
        chartWrapper.style.margin = '12px 0';
        panel.appendChild(chartWrapper);

        function renderChart() {
            chartWrapper.innerHTML = '';
            if (!bridge.getChartData) {
                flowText(chartWrapper, 'Grafik', 'Bridge riwayat harga domain belum tersedia.');
                return;
            }
            try {
                const chartData = JSON.parse(bridge.getChartData(stock.id, currentTimeframe));
                if (!chartData.success) {
                    flowText(chartWrapper, 'Grafik', chartData.message || 'Data grafik belum tersedia.');
                    return;
                }

                // Chart Controls
                const controls = document.createElement('div');
                controls.className = 'chart-controls';

                const tfButtons = document.createElement('div');
                tfButtons.className = 'timeframe-buttons';
                ['M1', 'M5', 'M15', 'H1', 'D1'].forEach(tf => {
                    const btn = document.createElement('button');
                    btn.textContent = tf;
                    btn.dataset.timeframe = tf;
                    if (tf === currentTimeframe) btn.classList.add('active');
                    btn.setAttribute('aria-pressed', String(tf === currentTimeframe));
                    btn.onclick = () => {
                        currentTimeframe = tf;
                        renderChart();
                    };
                    tfButtons.appendChild(btn);
                });
                controls.appendChild(tfButtons);

                const typeButtons = document.createElement('div');
                typeButtons.className = 'chart-type';
                ['candle', 'line'].forEach(ct => {
                    const btn = document.createElement('button');
                    btn.className = 'btn-secondary';
                    btn.style.minHeight = '48px';
                    btn.style.padding = '6px 12px';
                    btn.textContent = ct === 'candle' ? 'Candle' : 'Garis';
                    btn.setAttribute('aria-pressed', String(ct === currentChartType));
                    btn.onclick = () => {
                        currentChartType = ct;
                        renderChart();
                    };
                    typeButtons.appendChild(btn);
                });
                controls.appendChild(typeButtons);
                chartWrapper.appendChild(controls);

                // OHLC Readout
                const bars = (chartData.bars || []).filter(bar => !bar.isReference);
                if (!bars.length) { emptyState(chartWrapper,'Belum ada transaksi tercatat','Grafik akan terbentuk dari transaksi pasar game. Pilih periode lain atau lanjutkan sesi.'); return; }
                const lastBar = bars[bars.length - 1] || { time: '-', open: 0, high: 0, low: 0, close: 0, volume: 0 };
                const readout = document.createElement('div');
                readout.id = 'ohlc-readout';
                readout.className = 'ohlc-readout';
                readout.innerHTML = `
                    <span><strong>Waktu:</strong> ${lastBar.time}</span>
                    <span><strong>O:</strong> ${lastBar.open}</span>
                    <span><strong>H:</strong> ${lastBar.high}</span>
                    <span><strong>L:</strong> ${lastBar.low}</span>
                    <span><strong>C:</strong> ${lastBar.close}</span>
                    <span><strong>Vol:</strong> ${lastBar.volume}</span>
                `;
                chartWrapper.appendChild(readout);

                // SVG Plot
                const minPrice = Math.min(...bars.map(b => b.low));
                const maxPrice = Math.max(...bars.map(b => b.high));
                const priceRange = (maxPrice - minPrice) || 1;
                const maxVol = Math.max(1, ...bars.map(b => b.volume));

                const svgWidth = 600;
                const svgHeight = 220;
                const chartH = 150;
                const step = bars.length > 0 ? svgWidth / bars.length : svgWidth;

                const y = v => 20 + ((maxPrice - v) / priceRange) * chartH;

                let svgContent = `<path d="M0 20H600M0 70H600M0 120H600M0 170H600" stroke="var(--divider)" stroke-dasharray="3 6" fill="none" />`;

                if (currentChartType === 'candle') {
                    bars.forEach((b, i) => {
                        const x = i * step + step / 2;
                        const isUp = b.close >= b.open;
                        const color = isUp ? 'var(--positive)' : 'var(--negative)';
                        const bodyY = Math.min(y(b.open), y(b.close));
                        const bodyH = Math.max(2, Math.abs(y(b.close) - y(b.open)));
                        const volH = (b.volume / maxVol) * 40;
                        svgContent += `
                            <g>
                                <line class="candle-wick" x1="${x}" x2="${x}" y1="${y(b.high)}" y2="${y(b.low)}" stroke="${color}" stroke-width="1.5" />
                                <rect class="candle-body" x="${x - 4}" y="${bodyY}" width="8" height="${bodyH}" fill="${color}" />
                                <rect x="${x - 4}" y="${svgHeight - volH}" width="8" height="${volH}" fill="${color}" opacity="0.4" />
                            </g>
                        `;
                    });
                } else {
                    const pts = bars.map((b, i) => `${i ? 'L' : 'M'}${i * step + step / 2} ${y(b.close)}`).join(' ');
                    svgContent += `<path d="${pts}" fill="none" stroke="var(--teal)" stroke-width="2" />`;
                }

                const chartLayout = document.createElement('div');
                chartLayout.className = 'candle-layout';
                chartLayout.innerHTML = `
                    <svg class="candle-svg" viewBox="0 0 ${svgWidth} ${svgHeight}" role="img" aria-label="Grafik harga dari transaksi game" preserveAspectRatio="none">${svgContent}</svg>
                    <div class="candle-axis">
                        <span>${maxPrice.toFixed(0)}</span>
                        <span>${((maxPrice + minPrice) / 2).toFixed(0)}</span>
                        <span>${minPrice.toFixed(0)}</span>
                    </div>
                `;
                chartWrapper.appendChild(chartLayout);
                const times = node('div','chart-times'); times.append(node('span','',bars[0].time),node('span','',lastBar.time)); chartWrapper.append(times);

                // Alternative Table Toggle
                const tableToggleBtn = document.createElement('button');
                tableToggleBtn.className = 'btn-secondary';
                tableToggleBtn.style.marginTop = '6px';
                tableToggleBtn.textContent = 'Tabel OHLCV Alternatif';
                const tableContainer = document.createElement('div');
                tableContainer.id = 'chart-table';
                tableContainer.style.display = 'none';
                tableContainer.style.margin = '8px 0';
                tableContainer.innerHTML = `
                    <table class="depth-table" aria-label="Riwayat OHLCV game">
                        <thead><tr><th>Waktu</th><th>Open</th><th>High</th><th>Low</th><th>Close</th><th>Vol</th></tr></thead>
                        <tbody>${bars.map(b => `<tr><td>${b.time}</td><td>${b.open}</td><td>${b.high}</td><td>${b.low}</td><td>${b.close}</td><td>${b.volume}</td></tr>`).join('')}</tbody>
                    </table>
                `;
                tableToggleBtn.onclick = () => {
                    const isHidden = tableContainer.style.display === 'none';
                    tableContainer.style.display = isHidden ? 'block' : 'none';
                    tableToggleBtn.textContent = isHidden ? 'Tutup Tabel OHLCV' : 'Tabel OHLCV Alternatif';
                };
                chartWrapper.appendChild(tableToggleBtn);
                chartWrapper.appendChild(tableContainer);
            } catch (e) {
                flowText(chartWrapper, 'Grafik gagal dimuat', e.message);
            }
        }
        renderChart();

        // Order Book & Tape
        let book = null;
        if (bridge.getOrderBook) {
            try {
                book = JSON.parse(bridge.getOrderBook(stock.id));
                flowText(panel, 'Status pasar', book.haltState || 'NORMAL');

                const bookSection = document.createElement('div');
                bookSection.innerHTML = `
                    <h3 style="margin: 12px 0 6px; font-size: 0.95rem;">Order Book (10 Tingkat)</h3>
                    <div class="table-scroll" tabindex="0" aria-label="Antrean harga beli dan jual"><table class="depth-table">
                        <thead><tr><th>Bid Qty</th><th>Bid</th><th>Ask</th><th>Ask Qty</th></tr></thead>
                        <tbody>
                            ${Array.from({ length: Math.max((book.bids || []).length, (book.asks || []).length) }).map((_, i) => {
                                const b = (book.bids || [])[i] || { price: '-', quantity: '-' };
                                const a = (book.asks || [])[i] || { price: '-', quantity: '-' };
                                return `<tr>
                                    <td class="depth-bid">${b.quantity}</td>
                                    <td class="positive">${b.price}</td>
                                    <td class="negative">${a.price}</td>
                                    <td class="depth-ask">${a.quantity}</td>
                                </tr>`;
                            }).join('')}
                        </tbody>
                    </table></div>
                `;
                panel.appendChild(bookSection);

                // Tape (Recent Trades)
                if (book.recentTrades && book.recentTrades.length) {
                    const tapeSection = document.createElement('div');
                    tapeSection.innerHTML = `
                        <h3 style="margin: 12px 0 6px; font-size: 0.95rem;">Tape Transaksi Terakhir</h3>
                        <div style="font-size:0.8rem; font-variant-numeric: tabular-nums;">
                            ${book.recentTrades.map(t => `<div class="flex-row"><span>${new Date(t.timestamp).toLocaleTimeString('id-ID')}</span><span>${t.quantity} lembar</span><strong class="positive">${t.price}</strong></div>`).join('')}
                        </div>
                    `;
                    panel.appendChild(tapeSection);
                }
            } catch (error) { flowText(panel, 'Antrean gagal dimuat', error.message); }
        } else flowText(panel, 'Antrean', 'Bridge domain tidak tersedia pada pratinjau ini');

        if (bridge.toggleWatchlist) {
            const watchlist = flowButton(panel, stock.isWatchlist ? 'Hapus dari watchlist' : 'Tambah ke watchlist', () => {
                try {
                    const result = JSON.parse(bridge.toggleWatchlist(stock.id));
                    if (!result.success) throw new Error(result.message || 'Watchlist belum terkonfirmasi');
                    stock.isWatchlist = result.inWatchlist;
                    watchlist.textContent = result.inWatchlist ? 'Hapus dari watchlist' : 'Tambah ke watchlist';
                    watchlist.setAttribute('aria-pressed', String(result.inWatchlist));
                    loadStocks();
                } catch (error) { flowText(panel, 'Watchlist gagal diperbarui', error.message); }
            });
            watchlist.className = 'btn-secondary';
            watchlist.setAttribute('aria-pressed', String(stock.isWatchlist));
        }

        const isHalted = book && book.haltState && book.haltState !== 'NORMAL';
        if (isHalted) {
            flowText(panel, 'Pemberitahuan suspensi', 'Perdagangan instrumen ini sedang di-halt / disuspensi bursa. Order baru tidak dapat diproses.');
        }
        flowButton(panel, 'Perbarui harga & antrean', () => {
            const stocks = JSON.parse(bridge.getInstruments(stock.ticker, stock.exchange, null, true));
            const fresh = stocks.find(item => item.id === stock.id);
            if (fresh) openInstrumentDetail(fresh); else flowText(panel,'Data belum tersedia','Kembali ke daftar Pasar untuk mencoba lagi.');
        });
        const create = flowButton(panel, 'Buat order', () => { closeFlow(); openOrderTicket(stock); });
        create.className = 'btn-primary';
        create.disabled = !stock.isActiveTrading || isHalted;
    }
    function showOrderReview(preview, draft) {
        closeOrderTicket();
        orderDraft = draft; orderPreview = preview;
        const panel = showFlow('Tinjau order', true);
        flowText(panel, 'Akun', preview.accountName);
        flowText(panel, 'Mode', modeLabel.textContent);
        flowText(panel, 'Instrumen', `${draft.instrumentId} — ${preview.currency}`);
        flowText(panel, 'Order', `${draft.side === 'BUY' ? 'Beli' : 'Jual'} · ${draft.type} · ${draft.quantity} saham`);
        flowText(panel, 'Harga / pemicu', `${draft.limitPrice || draft.triggerPrice || 'Market sesuai likuiditas'} ${preview.currency} per saham`);
        flowText(panel, 'Estimasi fill / sisa', `${preview.estimatedFillQuantity} / ${preview.remainingQuantity} saham`);
        flowText(panel, 'Harga rata-rata / slippage', `${preview.estimatedVwap == null ? 'Tidak ada fill' : Number(preview.estimatedVwap).toLocaleString('id-ID',{maximumFractionDigits:4})} / ${preview.slippagePercent == null ? '—' : Number(preview.slippagePercent).toLocaleString('id-ID',{maximumFractionDigits:3})+'%'}`);
        flowText(panel, 'Estimasi fee', preview.estimatedFee);
        flowText(panel, 'Estimasi dampak kas', preview.estimatedCashImpact);
        flowText(panel, 'Kas diperlukan', preview.requiredCash);
        flowText(panel, 'Kas tersedia', preview.availableCash);
        flowText(panel, 'Estimasi berlaku hingga', new Date(preview.expiresAt).toLocaleTimeString('id-ID'));
        flowText(panel, 'Batas', preview.warning);
        flowButton(panel, 'Kirim order', event => {
            if (Date.now() > preview.expiresAt) {
                flowText(panel, 'Estimasi kedaluwarsa', 'Kembali ke tiket untuk meninjau ulang.'); return;
            }
            event.currentTarget.disabled = true; event.currentTarget.textContent = 'Memproses';
            activeRequestId = draft.requestId;
            try {
                const result = JSON.parse(bridge.submitOrder(JSON.stringify(draft)));
                showOrderReceipt(result);
                refreshSnapshot();
            } catch (error) {
                showOrderReceipt({success:false, status:'UNKNOWN', message:'Status belum terkonfirmasi. Periksa status dengan ID yang sama.'});
            }
        });
    }
    function showOrderReceipt(result) {
        const panel = showFlow('Status order', true);
        flowText(panel, 'Status', result.status || 'DITOLAK');
        flowText(panel, 'Mode', modeLabel.textContent);
        flowText(panel, 'Akun', result.accountId || (orderDraft && orderDraft.accountId));
        flowText(panel, 'Instrumen / mata uang', `${result.instrumentId || (orderDraft && orderDraft.instrumentId) || '-'} / ${result.currency || orderPreview && orderPreview.currency || '-'}`);
        flowText(panel, 'Pesan', result.message || 'Status dari domain');
        if (result.status === 'CANCELLED') flowText(panel, 'Sisa dibatalkan', 'Kuantitas belum terisi tidak lagi aktif. Fill sebelumnya tidak dibatalkan.');
        flowText(panel, 'Order / permintaan', `${result.orderId || '-'} / ${activeRequestId}`);
        flowText(panel, 'Terisi / sisa', `${result.filledQuantity ?? '-'} / ${result.remainingQuantity ?? '-'} saham`);
        if (bridge.getOrderStatus) flowButton(panel, 'Periksa status', () => showOrderReceipt(JSON.parse(bridge.getOrderStatus(activeRequestId))));
        if (bridge.cancelOrder && ['OPEN','PARTIALLY_FILLED','PENDING_TRIGGER'].includes(result.status)) {
            flowButton(panel, 'Batalkan sisa', () => {
                if (window.confirm(`Batalkan hanya sisa ${result.remainingQuantity} saham?`)) {
                    showOrderReceipt(JSON.parse(bridge.cancelOrder(activeRequestId))); refreshSnapshot();
                }
            });
        }
    }

    function openCorporateActionModal() {
        const panel = showFlow('Aksi korporasi & voting RUPS');
        flowText(panel, 'Informasi', 'Hak aksi korporasi (Dividen, Stock Split) dan hak suara RUPS berdasarkan kepemilikan saham pada recording date.');

        const corpSection = document.createElement('section');
        corpSection.className = 'card';
        const corpTitle = document.createElement('h2');
        corpTitle.className = 'card-title';
        corpTitle.textContent = 'Aksi korporasi terjadwal';
        corpSection.append(corpTitle);

        if (bridge.getCorporateActions) {
            try {
                const actions = JSON.parse(bridge.getCorporateActions());
                if (!actions.length) {
                    flowText(corpSection, 'Aksi', 'Belum ada aksi korporasi terjadwal.');
                } else {
                    actions.forEach(act => {
                        const row = document.createElement('div');
                        row.style.borderBottom = '1px solid var(--divider)';
                        row.style.padding = '8px 0';
                        flowText(row, `${act.type} — ${act.ticker}`, `Nilai/Rasio: ${act.valueOrRatio} ${act.currency || ''}`);
                        flowText(row, 'Jadwal', `Recording: ${act.recordingDate} | Eksekusi: ${act.executionDate}`);
                        flowText(row, 'Status', act.isExecuted ? 'Sudah dieksekusi' : 'Menunggu tanggal eksekusi');
                        corpSection.append(row);
                    });
                }
            } catch (e) { flowText(corpSection, 'Gagal memuat aksi', e.message); }
        }
        panel.append(corpSection);

        const voteSection = document.createElement('section');
        voteSection.className = 'card';
        const voteTitle = document.createElement('h2');
        voteTitle.className = 'card-title';
        voteTitle.textContent = 'Agenda voting RUPS';
        voteSection.append(voteTitle);

        if (bridge.getShareholderAgendas) {
            try {
                const agendas = JSON.parse(bridge.getShareholderAgendas());
                if (!agendas.length) {
                    flowText(voteSection, 'Agenda', 'Belum ada agenda RUPS yang aktif.');
                } else {
                    agendas.forEach(ag => {
                        const card = document.createElement('article');
                        card.className = 'card';
                        card.dataset.agendaId = ag.id;
                        const h = document.createElement('h3');
                        h.textContent = `${ag.ticker} — ${ag.title}`;
                        card.append(h);
                        flowText(card, 'Resolusi', ag.resolutionText);
                        flowText(card, 'Recording date', ag.recordingDate);
                        flowText(card, 'Hasil suara sementara', `Setuju: ${ag.votesFor} | Tolak: ${ag.votesAgainst} | Abstain: ${ag.votesAbstain}`);
                        flowText(card, 'Status partisipasi', ag.hasVoted ? 'Sudah memberikan suara' : 'Belum memberikan suara');

                        if (!ag.hasVoted && bridge.castShareholderVote) {
                            const btnGroup = document.createElement('div');
                            btnGroup.style.display = 'flex';
                            btnGroup.style.gap = '8px';
                            btnGroup.style.marginTop = '8px';

                            const voteError = document.createElement('p');
                            voteError.setAttribute('role', 'alert');
                            voteError.style.color = 'var(--negative)';

                            [
                                { choice: 'FOR', label: 'Setuju' },
                                { choice: 'AGAINST', label: 'Tolak' },
                                { choice: 'ABSTAIN', label: 'Abstain' }
                            ].forEach(opt => {
                                const btn = document.createElement('button');
                                btn.className = 'btn-secondary';
                                btn.textContent = opt.label;
                                btn.onclick = () => {
                                    try {
                                        const res = JSON.parse(bridge.castShareholderVote(JSON.stringify({ agendaId: ag.id, choice: opt.choice })));
                                        if (!res.success) throw new Error(res.message);
                                        openCorporateActionModal();
                                    } catch (err) { voteError.textContent = 'Gagal voting: ' + err.message; }
                                };
                                btnGroup.append(btn);
                            });
                            card.append(btnGroup, voteError);
                        }
                        voteSection.append(card);
                    });
                }
            } catch (e) { flowText(voteSection, 'Gagal memuat agenda', e.message); }
        }
        panel.append(voteSection);
    }

    function openIpoCenter() {
        const panel = showFlow('Pusat IPO (Pasar perdana)');
        flowText(panel, 'Penawaran IPO', 'Pemesanan saham perdana. Dana tertahan (reserve) saat pemesanan; saat penjatahan (allotment), sisa dana dikembalikan utuh (refund).');
        if (!bridge.getIpoOfferings) {
            flowText(panel, 'IPO', 'Penawaran IPO belum dapat dimuat.'); return;
        }
        try {
            const ipos = JSON.parse(bridge.getIpoOfferings());
            if (!ipos.length) {
                flowText(panel, 'Penawaran', 'Saat ini belum ada penawaran IPO baru.');
            } else {
                ipos.forEach(ipo => {
                    const card = document.createElement('section');
                    card.className = 'card';
                    card.dataset.ipoId = ipo.id;
                    const title = document.createElement('h2');
                    title.className = 'card-title';
                    title.textContent = `${ipo.ticker} — ${ipo.companyName}`;
                    card.append(title);
                    flowText(card, 'Harga penawaran', `${ipo.offeringPrice} (1 lot = ${ipo.lotSize} saham)`);
                    flowText(card, 'Saham ditawarkan', `${ipo.sharesOffered.toLocaleString('id-ID')} lembar`);
                    flowText(card, 'Tanggal listing', ipo.listingDate);
                    flowText(card, 'Status penjatahan', ipo.isAllotted ? 'Sudah dijatahkan' : 'Masa penawaran berlangsung');

                    if (!ipo.isAllotted && bridge.orderIpo) {
                        const form = document.createElement('div');
                        form.style.marginTop = '12px';
                        const lotLabel = document.createElement('label');
                        lotLabel.htmlFor = 'ipo-lots-input';
                        lotLabel.textContent = 'Jumlah lot yang dipesan';
                        const lotInput = document.createElement('input');
                        lotInput.id = 'ipo-lots-input';
                        lotInput.type = 'number';
                        lotInput.min = '1';
                        lotInput.value = '10';
                        lotInput.className = 'input-field';

                        const errText = document.createElement('p');
                        errText.setAttribute('role', 'alert');
                        errText.style.color = 'var(--negative)';

                        const orderBtn = document.createElement('button');
                        orderBtn.className = 'btn-primary';
                        orderBtn.textContent = 'Pesan saham IPO';
                        orderBtn.onclick = () => {
                            const lots = Number(lotInput.value);
                            if (!Number.isFinite(lots) || lots <= 0) { errText.textContent = 'Isi jumlah lot positif.'; return; }
                            try {
                                const res = JSON.parse(bridge.orderIpo(JSON.stringify({ ipoId: ipo.id, lots })));
                                if (!res.success) throw new Error(res.message);
                                refreshSnapshot();
                                flowText(card, 'Pemesanan berhasil', `${res.message} — Tertahan: ${res.reservedMoney}`);
                                orderBtn.disabled = true;
                            } catch (err) { errText.textContent = 'Pemesanan gagal: ' + err.message; }
                        };
                        form.append(lotLabel, lotInput, errText, orderBtn);
                        card.append(form);
                    }
                    panel.append(card);
                });
            }
        } catch (e) { flowText(panel, 'Gagal memuat IPO', e.message); }
    }

    function openMultiAssets() {
        const panel = showFlow('Katalog multi-aset');
        flowText(panel, 'Informasi', 'Instrumen non-ekuitas: Obligasi Negara & Komoditas Emas Fisik. Perdagangan aktif sekunder bersifat provisi/simulasi katalog.');

        if (!bridge.getMultiAssets) {
            flowText(panel, 'Multi-aset', 'Katalog multi-aset belum dapat dimuat.'); return;
        }

        try {
            const data = JSON.parse(bridge.getMultiAssets());

            const bondSection = document.createElement('section');
            bondSection.className = 'card';
            const bondTitle = document.createElement('h2');
            bondTitle.className = 'card-title';
            bondTitle.textContent = 'Obligasi negara (Fixed Income)';
            bondSection.append(bondTitle);

            if (!data.bonds || !data.bonds.length) {
                flowText(bondSection, 'Obligasi', 'Belum ada seri obligasi terdaftar.');
            } else {
                data.bonds.forEach(b => {
                    const card = document.createElement('article');
                    card.className = 'card';
                    const h = document.createElement('h3');
                    h.textContent = `${b.ticker} — ${b.name}`;
                    card.append(h);
                    flowText(card, 'Nilai par', `${b.parValue} (${b.currency})`);
                    flowText(card, 'Kupon tahunan', `${(Number(b.couponRateAnnual) * 100).toFixed(3)}% p.a.`);
                    flowText(card, 'Harga bersih (clean price)', b.cleanPrice);
                    flowText(card, 'Jatuh tempo', b.maturityDate);
                    flowText(card, 'Status perdagangan', `${b.status} — ${b.note}`);
                    bondSection.append(card);
                });
            }
            panel.append(bondSection);

            const commSection = document.createElement('section');
            commSection.className = 'card';
            const commTitle = document.createElement('h2');
            commTitle.className = 'card-title';
            commTitle.textContent = 'Komoditas fisik (Gold/Commodity)';
            commSection.append(commTitle);

            if (!data.commodities || !data.commodities.length) {
                flowText(commSection, 'Komoditas', 'Belum ada komoditas terdaftar.');
            } else {
                data.commodities.forEach(c => {
                    const card = document.createElement('article');
                    card.className = 'card';
                    const h = document.createElement('h3');
                    h.textContent = `${c.ticker} — ${c.name}`;
                    card.append(h);
                    flowText(card, 'Harga spot per unit', `${c.spotPrice} / ${c.unit}`);
                    flowText(card, 'Underlying', 'Fisik teralokasi (allocated physical backing)');
                    flowText(card, 'Status perdagangan', `${c.status} — ${c.note}`);
                    commSection.append(card);
                });
            }
            panel.append(commSection);

        } catch (e) { flowText(panel, 'Gagal memuat katalog multi-aset', e.message); }
    }

    function openFundBuilder() {
        const panel = showFlow('Fund Builder & alokasi portofolio');
        flowText(panel, 'Konsep', 'Rancang portofolio terdiversifikasi. Menyimpan alokasi target tanpa mutasi saldo fisik. Eksekusi rebalancing menerbitkan order penyeimbang secara atomik.');

        if (!bridge.getFundPlans || !bridge.saveFundPlan) {
            flowText(panel, 'Fund Builder', 'Rencana alokasi belum dapat dimuat.'); return;
        }

        try {
            const formSection = document.createElement('section');
            formSection.className = 'card';
            const formTitle = document.createElement('h2');
            formTitle.className = 'card-title';
            formTitle.textContent = 'Buat rencana alokasi baru';
            formSection.append(formTitle);

            const nameLabel = document.createElement('label');
            nameLabel.htmlFor = 'fund-plan-name';
            nameLabel.textContent = 'Nama rencana alokasi';
            const nameInput = document.createElement('input');
            nameInput.id = 'fund-plan-name';
            nameInput.type = 'text';
            nameInput.value = 'Portofolio Seimbang BBCA/BMRI';
            nameInput.className = 'input-field';

            const bbcaLabel = document.createElement('label');
            bbcaLabel.htmlFor = 'fund-weight-bbca';
            bbcaLabel.textContent = 'Bobot target BBCA (%)';
            const bbcaInput = document.createElement('input');
            bbcaInput.id = 'fund-weight-bbca';
            bbcaInput.type = 'number';
            bbcaInput.min = '0';
            bbcaInput.max = '100';
            bbcaInput.value = '40';
            bbcaInput.className = 'input-field';

            const bmriLabel = document.createElement('label');
            bmriLabel.htmlFor = 'fund-weight-bmri';
            bmriLabel.textContent = 'Bobot target BMRI (%)';
            const bmriInput = document.createElement('input');
            bmriInput.id = 'fund-weight-bmri';
            bmriInput.type = 'number';
            bmriInput.min = '0';
            bmriInput.max = '100';
            bmriInput.value = '30';
            bmriInput.className = 'input-field';

            const cashLabel = document.createElement('label');
            cashLabel.htmlFor = 'fund-weight-cash';
            cashLabel.textContent = 'Cadangan kas (%)';
            const cashInput = document.createElement('input');
            cashInput.id = 'fund-weight-cash';
            cashInput.type = 'number';
            cashInput.min = '0';
            cashInput.max = '100';
            cashInput.value = '30';
            cashInput.className = 'input-field';

            const totalStatus = document.createElement('p');
            totalStatus.style.fontWeight = 'bold';
            totalStatus.textContent = 'Total bobot: 100%';

            function updateTotal() {
                const total = Number(bbcaInput.value || 0) + Number(bmriInput.value || 0) + Number(cashInput.value || 0);
                totalStatus.textContent = `Total bobot: ${total}% ${total === 100 ? '✅' : '⚠️ (Harus tepat 100%)'}`;
                totalStatus.style.color = total === 100 ? 'var(--positive)' : 'var(--negative)';
            }
            bbcaInput.addEventListener('input', updateTotal);
            bmriInput.addEventListener('input', updateTotal);
            cashInput.addEventListener('input', updateTotal);

            const saveAlert = document.createElement('p');
            saveAlert.setAttribute('role', 'alert');

            const saveBtn = document.createElement('button');
            saveBtn.className = 'btn-primary';
            saveBtn.textContent = 'Simpan rencana alokasi';
            saveBtn.onclick = () => {
                const bbcaW = Number(bbcaInput.value || 0) / 100;
                const bmriW = Number(bmriInput.value || 0) / 100;
                const cashW = Number(cashInput.value || 0) / 100;
                const total = bbcaW + bmriW + cashW;
                if (Math.abs(total - 1.0) > 0.001) {
                    saveAlert.textContent = 'Total bobot harus tepat 100%.';
                    saveAlert.style.color = 'var(--negative)';
                    return;
                }
                try {
                    const planId = 'PLAN-' + Date.now();
                    const payload = {
                        planId,
                        name: nameInput.value.trim() || 'Rencana Alokasi',
                        cashWeight: cashW,
                        weights: { 'IDX:BBCA': bbcaW, 'IDX:BMRI': bmriW }
                    };
                    const res = JSON.parse(bridge.saveFundPlan(JSON.stringify(payload)));
                    if (!res.success) throw new Error(res.message);
                    openFundBuilder();
                } catch (err) {
                    saveAlert.textContent = 'Gagal menyimpan rencana: ' + err.message;
                    saveAlert.style.color = 'var(--negative)';
                }
            };

            formSection.append(nameLabel, nameInput, bbcaLabel, bbcaInput, bmriLabel, bmriInput, cashLabel, cashInput, totalStatus, saveAlert, saveBtn);
            panel.append(formSection);

            const plansSection = document.createElement('section');
            plansSection.className = 'card';
            const plansTitle = document.createElement('h2');
            plansTitle.className = 'card-title';
            plansTitle.textContent = 'Rencana tersimpan';
            plansSection.append(plansTitle);

            const plans = JSON.parse(bridge.getFundPlans());
            if (!plans.length) {
                flowText(plansSection, 'Rencana', 'Belum ada rencana alokasi tersimpan.');
            } else {
                plans.forEach(p => {
                    const card = document.createElement('article');
                    card.className = 'card';
                    card.dataset.planId = p.id;
                    const h = document.createElement('h3');
                    h.textContent = `${p.name} (${p.id})`;
                    card.append(h);
                    flowText(card, 'Status rencana', p.status);
                    flowText(card, 'Cadangan kas', `${(p.cashWeight * 100).toFixed(0)}%`);
                    const wText = Object.entries(p.weights).map(([k, v]) => `${k}: ${(v * 100).toFixed(0)}%`).join(' | ');
                    flowText(card, 'Alokasi instrumen', wText || 'Tidak ada instrumen');

                    if (bridge.executeFundRebalance) {
                        const rebalBtn = document.createElement('button');
                        rebalBtn.className = 'btn-secondary';
                        rebalBtn.style.marginTop = '8px';
                        rebalBtn.textContent = 'Tinjau & eksekusi rebalancing';
                        const rebalResult = document.createElement('p');
                        rebalResult.setAttribute('role', 'status');

                        rebalBtn.onclick = () => {
                            try {
                                const res = JSON.parse(bridge.executeFundRebalance(p.id));
                                if (!res.success) throw new Error(res.message);
                                rebalResult.textContent = `${res.message} — Status rencana: ${res.planStatus}`;
                                rebalResult.style.color = 'var(--positive)';
                                refreshSnapshot();
                            } catch (err) {
                                rebalResult.textContent = 'Eksekusi gagal: ' + err.message;
                                rebalResult.style.color = 'var(--negative)';
                            }
                        };
                        card.append(rebalBtn, rebalResult);
                    }
                    plansSection.append(card);
                });
            }
            panel.append(plansSection);

        } catch (e) { flowText(panel, 'Gagal memuat Fund Builder', e.message); }
    }

    function openClientReports() {
        const panel = showFlow('Laporan klien & fee mandat');
        flowText(panel, 'Transparansi', 'Rekapitulasi akun mandat klien institusi/individu. Management fee tahunan dan performance fee dihitung terhadap High-Water Mark.');

        if (!bridge.getClientReports) {
            flowText(panel, 'Laporan klien', 'Laporan klien belum dapat dimuat.'); return;
        }

        try {
            const reports = JSON.parse(bridge.getClientReports());
            if (!reports.length) {
                flowText(panel, 'Mandat klien', 'Belum ada mandat klien terdaftar.');
            } else {
                reports.forEach(r => {
                    const card = document.createElement('article');
                    card.className = 'card';
                    const h = document.createElement('h3');
                    h.textContent = `${r.clientName} (${r.mandateId || r.id})`;
                    card.append(h);
                    flowText(card, 'Status akun', r.statusLabel || r.status || (r.accountActive ? 'Aktif' : 'Tawaran'));
                    flowText(card, 'Profil risiko & benchmark', `${r.riskProfile} (Benchmark: ${r.benchmark ?? 'Belum tersedia'})`);
                    flowText(card, 'Setoran modal awal', r.initialDeposit ?? 'Belum tersedia');
                    flowText(card, 'High-Water Mark (HWM)', r.highWaterMark ?? 'Belum tersedia');
                    flowText(card, 'Skor kepercayaan klien (Trust)', r.trust != null ? `${r.trust} / 100` : 'Belum tersedia');
                    flowText(card, 'Tarif management fee', r.managementFeeAnnualRate != null ? `${(Number(r.managementFeeAnnualRate) * 100).toFixed(1)}% p.a.` : 'Belum tersedia');
                    flowText(card, 'Tarif performance fee', r.performanceFeeRate != null ? `${(Number(r.performanceFeeRate) * 100).toFixed(0)}% di atas HWM` : 'Belum tersedia');
                    panel.append(card);
                });
            }
        } catch (e) { flowText(panel, 'Gagal memuat laporan klien', e.message); }
    }

    function openStressTestModal() {
        const panel = showFlow('Uji stres portofolio makro');
        flowText(panel, 'Metodologi', 'Simulasi skenario goncangan pasar ekstrem terhadap portofolio. Memproyeksikan potensi penurunan nilai, risiko margin call, dan kecukupan modal.');

        if (!bridge.getStressTestScenarios || !bridge.runStressTest) {
            flowText(panel, 'Uji stres', 'Skenario uji stres belum dapat dimuat.'); return;
        }

        try {
            const scenarios = JSON.parse(bridge.getStressTestScenarios());
            scenarios.forEach(sc => {
                const card = document.createElement('section');
                card.className = 'card';
                card.dataset.scenarioId = sc.id;
                const h = document.createElement('h2');
                h.className = 'card-title';
                h.textContent = sc.name;
                card.append(h);
                flowText(card, 'Tipe skenario', sc.type);
                flowText(card, 'Deskripsi', sc.description);

                const runBtn = document.createElement('button');
                runBtn.className = 'btn-primary';
                runBtn.style.marginTop = '8px';
                runBtn.textContent = 'Jalankan simulasi dampak';
                const resultBox = document.createElement('div');
                resultBox.style.marginTop = '8px';

                runBtn.onclick = () => {
                    try {
                        const res = JSON.parse(bridge.runStressTest(sc.id));
                        if (!res.success) throw new Error(res.message);
                        resultBox.replaceChildren();
                        flowText(resultBox, 'Nilai portofolio awal', res.baselineValue);
                        flowText(resultBox, 'Nilai proyeksi stres', res.stressedValue);
                        flowText(resultBox, 'Potensi kerugian', `${res.potentialLoss} (${res.percentageDrop})`);
                        flowText(resultBox, 'Pemicu margin call', res.wouldTriggerMarginCall ? 'YA ⚠️ (Rasio ekuitas jatuh di bawah batas likuidasi)' : 'TIDAK ✅ (Portofolio tetap solvent)');
                        flowText(resultBox, 'Evaluasi risiko', res.riskAssessment);
                    } catch (err) { flowText(resultBox, 'Simulasi gagal', err.message); }
                };
                card.append(runBtn, resultBox);
                panel.append(card);
            });
        } catch (e) { flowText(panel, 'Gagal memuat skenario', e.message); }
    }

    let currentLabSandboxId = null;
    function openStrategyLabModal() {
        const panel = showFlow('Lab strategi & sandbox what-if');
        flowText(panel, 'Isolasi terjamin', 'Eksperimen strategi trading di lingkungan terisolasi penuh. Segala transaksi beli/jual di lab menggunakan kloning akun dan TIDAK AKAN mengubah saldo kas atau posisi akun riil Anda.');

        if (!bridge.createStrategyLab || !bridge.simulateLabTrade) {
            flowText(panel, 'Lab strategi', 'Lab strategi belum dapat dimuat.'); return;
        }

        try {
            if (!currentLabSandboxId) {
                const initRes = JSON.parse(bridge.createStrategyLab('LAB-EXP-01'));
                if (initRes.success) currentLabSandboxId = initRes.sandboxId;
            }

            const statusCard = document.createElement('section');
            statusCard.className = 'card';
            const h = document.createElement('h2');
            h.className = 'card-title';
            h.textContent = `Sesi Sandbox: ${currentLabSandboxId || 'Baru'}`;
            statusCard.append(h);

            flowButton(statusCard, 'Reset / Kloning ulang dari akun riil', () => {
                currentLabSandboxId = 'LAB-' + Date.now();
                bridge.createStrategyLab(currentLabSandboxId);
                openStrategyLabModal();
            });
            panel.append(statusCard);

            const formCard = document.createElement('section');
            formCard.className = 'card';
            const formTitle = document.createElement('h2');
            formTitle.className = 'card-title';
            formTitle.textContent = 'Simulasikan transaksi what-if';
            formCard.append(formTitle);

            const instLabel = document.createElement('label');
            instLabel.htmlFor = 'lab-inst-select';
            instLabel.textContent = 'Pilih instrumen saham';
            const instSelect = document.createElement('select');
            instSelect.id = 'lab-inst-select';
            instSelect.className = 'input-field';
            [
                { id: 'IDX:BBCA', name: 'BBCA — Bank Central Asia (Rp 10.250)' },
                { id: 'IDX:BMRI', name: 'BMRI — Bank Mandiri (Rp 6.800)' }
            ].forEach(opt => {
                const o = document.createElement('option');
                o.value = opt.id; o.textContent = opt.name;
                instSelect.append(o);
            });

            const sideLabel = document.createElement('label');
            sideLabel.htmlFor = 'lab-side-select';
            sideLabel.textContent = 'Aksi simulasi';
            const sideSelect = document.createElement('select');
            sideSelect.id = 'lab-side-select';
            sideSelect.className = 'input-field';
            [
                { value: 'BUY', label: 'Beli (Simulate Buy)' },
                { value: 'SELL', label: 'Jual (Simulate Sell)' }
            ].forEach(opt => {
                const o = document.createElement('option');
                o.value = opt.value; o.textContent = opt.label;
                sideSelect.append(o);
            });

            const qtyLabel = document.createElement('label');
            qtyLabel.htmlFor = 'lab-qty-input';
            qtyLabel.textContent = 'Jumlah lembar saham';
            const qtyInput = document.createElement('input');
            qtyInput.id = 'lab-qty-input';
            qtyInput.type = 'number';
            qtyInput.value = '500';
            qtyInput.min = '100';
            qtyInput.step = '100';
            qtyInput.className = 'input-field';

            const priceLabel = document.createElement('label');
            priceLabel.htmlFor = 'lab-price-input';
            priceLabel.textContent = 'Harga eksekusi simulasi';
            const priceInput = document.createElement('input');
            priceInput.id = 'lab-price-input';
            priceInput.type = 'number';
            priceInput.value = '10250';
            priceInput.className = 'input-field';

            const tradeAlert = document.createElement('p');
            tradeAlert.setAttribute('role', 'alert');

            const tradeBtn = document.createElement('button');
            tradeBtn.className = 'btn-primary';
            tradeBtn.textContent = 'Eksekusi transaksi lab';
            tradeBtn.onclick = () => {
                try {
                    const payload = {
                        sandboxId: currentLabSandboxId,
                        instrumentId: instSelect.value,
                        side: sideSelect.value,
                        shares: Number(qtyInput.value),
                        price: priceInput.value
                    };
                    const res = JSON.parse(bridge.simulateLabTrade(JSON.stringify(payload)));
                    if (!res.success) throw new Error(res.message);
                    tradeAlert.textContent = `${res.message} — Total trade lab: ${res.simulatedTradesCount} — Kas lab sisa: ${res.availableCashIdr}`;
                    tradeAlert.style.color = 'var(--positive)';
                } catch (err) {
                    tradeAlert.textContent = 'Simulasi gagal: ' + err.message;
                    tradeAlert.style.color = 'var(--negative)';
                }
            };

            formCard.append(instLabel, instSelect, sideLabel, sideSelect, qtyLabel, qtyInput, priceLabel, priceInput, tradeAlert, tradeBtn);
            panel.append(formCard);

        } catch (e) { flowText(panel, 'Gagal memuat lab strategi', e.message); }
    }

    function openHistoricalReplayModal() {
        const panel = showFlow('Replay historis data nyata');
        flowText(panel, 'Mode replay', 'Simulasi trading berbasis bar harian nyata anti-future leak (FR-01, FR-17).');

        const card = document.createElement('section');
        card.className = 'card';
        const h = document.createElement('h2');
        h.className = 'card-title';
        h.textContent = 'Konfigurasi & pilihan dataset';
        card.append(h);

        const dsLabel = document.createElement('label');
        dsLabel.textContent = 'Pilih dataset historis';
        const dsSelect = document.createElement('select');
        dsSelect.className = 'input-field';
        dsSelect.innerHTML = `
            <option value="IDX:BBCA">IDX:BBCA — Bank Central Asia Tbk (149 hari BEI nyata, CC BY-NC 4.0)</option>
            <option value="US:AAPL">US:AAPL — Apple Inc. (149 hari Nasdaq/US nyata, Apache 2.0)</option>
        `;

        const capLabel = document.createElement('label');
        capLabel.textContent = 'Modal awal replay';
        const capSelect = document.createElement('select');
        capSelect.className = 'input-field';
        capSelect.innerHTML = `
            <option value="50000000">Rp 50.000.000 (Standar)</option>
            <option value="25000000">Rp 25.000.000 (Tantangan)</option>
            <option value="100000000">Rp 100.000.000 (Ekspansif)</option>
        `;

        const startBtn = document.createElement('button');
        startBtn.className = 'btn-primary';
        startBtn.textContent = 'Mulai replay historis';
        const statusMsg = document.createElement('p');
        statusMsg.style.margin = '8px 0';

        startBtn.onclick = () => {
            try {
                const cfg = {
                    mode: 'HISTORICAL_REPLAY',
                    datasetId: dsSelect.value,
                    initialCapital: Number(capSelect.value)
                };
                const res = JSON.parse(bridge.startNewGame(JSON.stringify(cfg)));
                if (!res.success) throw new Error(res.message);
                statusMsg.textContent = res.message;
                statusMsg.style.color = 'var(--positive)';
                refreshSnapshot();
                setTimeout(() => { closeFlow(); switchTab('pasar'); loadPasarView(); }, 600);
            } catch (err) {
                statusMsg.textContent = 'Gagal memulai replay: ' + err.message;
                statusMsg.style.color = 'var(--negative)';
            }
        };

        flowText(card, 'Integritas data', 'Data bar harian nyata dengan validasi urutan tanggal, anti-future leak, dan fill logic jujur (Low <= Limit Buy <= High).');
        card.append(dsLabel, dsSelect, capLabel, capSelect, startBtn, statusMsg);
        panel.append(card);
    }

    function openRiskModal() {
        const panel = showFlow('Fasilitas margin & risiko');
        if (!bridge.getRiskSnapshot) {
            flowText(panel, 'Risiko', 'Rincian risiko belum dapat dimuat.'); return;
        }
        try {
            const risk = JSON.parse(bridge.getRiskSnapshot());
            if (!risk.success) throw new Error(risk.message || 'Snapshot risiko gagal dimuat');
            flowText(panel, 'Akun', `${risk.accountName} (${risk.accountId})`);
            flowText(panel, 'Ekuitas bersih', risk.equity);
            flowText(panel, 'Status margin', `${risk.statusLabel} (${risk.status})`);
            flowText(panel, 'Rasio margin', `${risk.marginRatioPercent} (Rasio: ${risk.marginRatio})`);
            flowText(panel, 'Total eksposur portofolio', risk.totalExposure);
            flowText(panel, 'Utang margin IDR', risk.marginDebtIdr);
            flowText(panel, 'Utang margin USD', risk.marginDebtUsd);
            flowText(panel, 'Total utang tertimbang', risk.marginDebtTotal);
            flowText(panel, 'Batas fasilitas', 'Fasilitas margin pinjaman simulasi internal game. Maintenance margin 30%, liquidation 20%.');

            const loanSection = document.createElement('section');
            loanSection.className = 'card';
            const loanTitle = document.createElement('h2');
            loanTitle.className = 'card-title';
            loanTitle.textContent = 'Kelola pinjaman margin';
            loanSection.append(loanTitle);

            const curLabel = document.createElement('label'); curLabel.htmlFor = 'margin-cur-select'; curLabel.textContent = 'Mata uang pinjaman';
            const curSelect = document.createElement('select'); curSelect.id = 'margin-cur-select'; curSelect.className = 'input-field';
            ['IDR', 'USD'].forEach(c => { const opt = document.createElement('option'); opt.value = c; opt.textContent = c; curSelect.append(opt); });

            const amtLabel = document.createElement('label'); amtLabel.htmlFor = 'margin-amt-input'; amtLabel.textContent = 'Nominal pinjaman / pelunasan';
            const amtInput = document.createElement('input'); amtInput.id = 'margin-amt-input'; amtInput.type = 'number'; amtInput.min = '1'; amtInput.className = 'input-field';
            amtInput.placeholder = 'Jumlah uang';

            const errDisplay = document.createElement('p'); errDisplay.id = 'margin-error'; errDisplay.setAttribute('role', 'alert'); errDisplay.style.color = 'var(--negative)';

            const btnBorrow = document.createElement('button');
            btnBorrow.className = 'btn-primary';
            btnBorrow.textContent = 'Tarik pinjaman margin';
            applyCareerGate(btnBorrow, 'margin');
            btnBorrow.style.marginBottom = '8px';
            btnBorrow.onclick = () => {
                const val = Number(amtInput.value);
                if (!Number.isFinite(val) || val <= 0) { errDisplay.textContent = 'Isi nominal pinjaman yang positif.'; return; }
                try {
                    const res = JSON.parse(bridge.takeMarginLoan(JSON.stringify({ currency: curSelect.value, amount: String(val) })));
                    if (!res.success) throw new Error(res.message);
                    refreshSnapshot();
                    openRiskModal();
                } catch (e) { errDisplay.textContent = 'Gagal meminjam: ' + e.message; }
            };

            const btnRepay = document.createElement('button');
            btnRepay.className = 'btn-secondary';
            btnRepay.textContent = 'Lunasi utang margin';
            btnRepay.onclick = () => {
                const val = Number(amtInput.value);
                if (!Number.isFinite(val) || val <= 0) { errDisplay.textContent = 'Isi nominal pelunasan yang positif.'; return; }
                try {
                    const res = JSON.parse(bridge.repayMarginLoan(JSON.stringify({ currency: curSelect.value, amount: String(val) })));
                    if (!res.success) throw new Error(res.message);
                    refreshSnapshot();
                    openRiskModal();
                } catch (e) { errDisplay.textContent = 'Gagal melunasi: ' + e.message; }
            };

            loanSection.append(curLabel, curSelect, amtLabel, amtInput, errDisplay, btnBorrow, btnRepay);
            panel.append(loanSection);

            const logSection = document.createElement('section');
            logSection.className = 'card';
            const logTitle = document.createElement('h2');
            logTitle.className = 'card-title';
            logTitle.textContent = 'Jejak audit kepatuhan & risiko';
            logSection.append(logTitle);
            if (!risk.auditLogs || !risk.auditLogs.length) {
                flowText(logSection, 'Audit', 'Belum ada catatan pelanggaran atau fasilitas.');
            } else {
                risk.auditLogs.forEach(item => {
                    const row = document.createElement('div');
                    row.style.borderBottom = '1px solid var(--divider)';
                    row.style.padding = '8px 0';
                    flowText(row, item.categoryLabel, item.description);
                    flowText(row, 'Mitigasi', item.mitigation);
                    logSection.append(row);
                });
            }
            panel.append(logSection);

        } catch (error) { flowText(panel, 'Risiko gagal dimuat', error.message); }
    }

    function openFxForm(draft = null) {
        const panel = showFlow('Konversi FX');
        if (!currentAccount || !bridge.previewFx || !bridge.confirmFx) {
            flowText(panel, 'FX belum tersedia', 'Bridge preview/konfirmasi domain belum tersedia.'); return;
        }
        flowText(panel, 'Akun', `${currentAccount.name} (${currentAccount.id})`);
        flowText(panel, 'Aturan simulasi', 'Penukaran memakai kurs dan biaya simulasi. Periksa jumlah bersih pada langkah berikutnya.');
        const fromLabel = document.createElement('label'); fromLabel.htmlFor = 'fx-from'; fromLabel.textContent = 'Mata uang asal';
        const from = document.createElement('select'); from.id = 'fx-from'; from.className = 'input-field';
        ['IDR', 'USD'].forEach(currency => { const option = document.createElement('option'); option.value = currency; option.textContent = currency; from.append(option); });
        from.value = draft ? draft.from : 'IDR';
        const amountLabel = document.createElement('label'); amountLabel.htmlFor = 'fx-amount'; amountLabel.textContent = 'Jumlah mata uang asal';
        const amount = document.createElement('input'); amount.id = 'fx-amount'; amount.type = 'number'; amount.min = '0'; amount.step = 'any'; amount.className = 'input-field';
        amount.value = draft ? draft.amount : '';
        const error = document.createElement('p'); error.setAttribute('role', 'alert'); error.tabIndex = -1;
        panel.append(fromLabel, from, amountLabel, amount, error);
        flowButton(panel, 'Tinjau konversi', () => {
            if (!Number.isFinite(Number(amount.value)) || Number(amount.value) <= 0) { error.textContent = 'Isi jumlah positif dalam mata uang asal.'; error.focus(); return; }
            const request = {accountId:currentAccount.id, from:from.value, to:from.value === 'IDR' ? 'USD' : 'IDR', amount:amount.value, requestId:'FX-UI-' + crypto.randomUUID()};
            try {
                const preview = JSON.parse(bridge.previewFx(JSON.stringify(request)));
                if (!preview.success) { error.textContent = preview.message || 'Preview FX ditolak'; error.focus(); return; }
                showFxReview(preview, request);
            } catch (failure) { error.textContent = 'Preview FX gagal: ' + failure.message; error.focus(); }
        });
    }
    function showFxReview(preview, request) {
        const panel = showFlow('Tinjau konversi FX', true);
        flowText(panel, 'Akun', preview.accountName);
        flowText(panel, 'Konversi uang game', `${preview.amount} ${preview.from} → ${preview.to}`);
        flowText(panel, 'Bid / ask (IDR per USD)', `${preview.bid} / ${preview.ask}`);
        flowText(panel, 'Spread (IDR per USD)', preview.spread);
        flowText(panel, 'Kurs efektif', preview.effectiveRate);
        flowText(panel, 'Biaya', preview.fee);
        flowText(panel, 'Terima bersih', preview.net);
        flowText(panel, 'Estimasi saldo asal / tujuan', `${preview.sourceBalanceAfter} / ${preview.targetBalanceAfter}`);
        flowText(panel, 'Quote / berlaku hingga', `${preview.quoteId} / ${new Date(preview.expiresAt).toLocaleString('id-ID')}`);
        flowText(panel, 'Batas', 'Perubahan kurs atau kedaluwarsa ditolak domain. Review tidak mengubah saldo.');
        flowButton(panel, 'Ubah / perbarui estimasi', () => openFxForm(request));
        flowButton(panel, 'Konfirmasi konversi', event => {
            event.currentTarget.disabled = true;
            request.quoteId = preview.quoteId;
            try {
                const result = JSON.parse(bridge.confirmFx(JSON.stringify(request)));
                const receipt = showFlow('Hasil konversi FX', true);
                flowText(receipt, 'Status', result.success ? result.status : 'DITOLAK');
                flowText(receipt, 'Akun', result.accountId || request.accountId);
                flowText(receipt, 'ID permintaan', request.requestId);
                flowText(receipt, 'Pesan', result.message);
                if (result.success) {
                    flowText(receipt, 'Debit / kredit bersih', `${result.amount} / ${result.net}`);
                    flowText(receipt, 'Kurs aktual / quote', `${result.effectiveRate} / ${result.quoteId}`);
                    flowText(receipt, 'Biaya aktual', result.fee);
                } else flowButton(receipt, 'Kembali ke draft FX', () => openFxForm(request));
                refreshSnapshot();
            } catch (failure) {
                flowText(panel, 'Status belum terkonfirmasi', `Jangan membuat permintaan baru. Pertahankan ID ${request.requestId}; pemulihan status FX masih dalam remediasi.`);
            }
        });
    }

    // 5. Order Ticket Modal Handling
    const orderModal = document.getElementById('order-modal');
    const btnCloseModal = document.getElementById('btn-close-modal');
    const orderForm = document.getElementById('order-form');
    const inputOrderQty = document.getElementById('order-qty');
    const inputOrderLimit = document.getElementById('order-limit-price');
    const estTotalDisplay = document.getElementById('order-est-total');

    let ticketOpener = null;
    function openOrderTicket(stock) {
        ticketOpener = document.activeElement;
        selectedInstrument = stock;
        document.getElementById('order-unit-label').textContent = `Jumlah unit (1 unit = ${stock.lotSize} saham)`;
        document.getElementById('order-price-label').textContent = `Harga limit (${stock.currency})`;
        inputOrderLimit.step = 'any';
        document.getElementById('modal-stock-ticker').textContent = stock.ticker + ' (' + stock.exchange + ')';
        document.getElementById('modal-stock-price').textContent = stock.currentPrice;
        inputOrderLimit.value = stock.currentPriceRaw;
        inputOrderQty.value = 1;
        updateOrderEstimate();
        orderModal.classList.add('active');
        document.querySelectorAll('body > :not(#order-modal):not(script)').forEach(node => node.inert = true);
        const accContext = document.getElementById('order-account-context');
        accContext.innerHTML = '';
        if (latestSnapshot && latestSnapshot.tradingAccounts && latestSnapshot.tradingAccounts.length > 1) {
            const accSelect = document.createElement('select');
            accSelect.id = 'order-account-select';
            accSelect.className = 'input-field';
            accSelect.style.marginBottom = '8px';
            latestSnapshot.tradingAccounts.forEach(a => {
                const opt = document.createElement('option');
                opt.value = a.id;
                opt.textContent = `Akun: ${a.name} (${a.type === 'CLIENT' ? 'Klien' : 'Pribadi'})`;
                if (currentAccount && currentAccount.id === a.id) opt.selected = true;
                accSelect.appendChild(opt);
            });
            accSelect.onchange = () => {
                const chosen = latestSnapshot.tradingAccounts.find(x => x.id === accSelect.value);
                if (chosen) {
                    currentAccount = { id: chosen.id, name: chosen.name, type: chosen.type };
                    updateOrderEstimate();
                }
            };
            accContext.appendChild(accSelect);
        } else {
            accContext.textContent = currentAccount ? `${currentAccount.name} (${currentAccount.id})` : 'Akun belum tersedia';
        }
        document.querySelectorAll('.side-btn').forEach(button => { button.classList.remove('active'); button.setAttribute('aria-pressed','false'); button.style.background = ''; button.style.color = ''; });
        document.getElementById('order-error').textContent = '';
        btnCloseModal.focus();
    }

    function closeOrderTicket() {
        orderModal.classList.remove('active');
        document.querySelectorAll('body > :not(#order-modal):not(script)').forEach(node => node.inert = false);
        selectedInstrument = null;
        if (ticketOpener && ticketOpener.isConnected) ticketOpener.focus();
    }

    document.addEventListener('keydown', event => {
        if (!orderModal.classList.contains('active')) return;
        if (event.key === 'Escape') {
            event.preventDefault();
            closeOrderTicket();
        } else if (event.key === 'Tab') {
            const controls = Array.from(orderModal.querySelectorAll('button, input, select, [tabindex]')).filter(node => !node.disabled && node.getClientRects().length);
            const first = controls[0], last = controls[controls.length - 1];
            if (event.shiftKey && document.activeElement === first) {
                event.preventDefault(); last.focus();
            } else if (!event.shiftKey && document.activeElement === last) {
                event.preventDefault(); first.focus();
            }
        }
    });
    window.closeActiveLayer = () => {
        if (orderModal.classList.contains('active')) { closeOrderTicket(); return true; }
        return closeFlow();
    };

    btnCloseModal.addEventListener('click', closeOrderTicket);

    function updateOrderEstimate() {
        estTotalDisplay.textContent = 'Biaya dan perkiraan kas akan ditampilkan sebelum order dikirim.';
        if (selectedInstrument) {
            const type = document.getElementById('order-type').value;
            inputOrderLimit.disabled = type === 'MARKET';
            document.getElementById('order-price-label').textContent =
                `${type === 'STOP_LOSS' || type === 'TAKE_PROFIT' ? 'Harga pemicu' : 'Harga limit'} (${selectedInstrument.currency})`;
        }
    }
    document.getElementById('order-type').addEventListener('change', updateOrderEstimate);

    inputOrderQty.addEventListener('input', updateOrderEstimate);
    inputOrderLimit.addEventListener('input', updateOrderEstimate);

    document.querySelectorAll('.side-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.side-btn').forEach(b => { b.classList.remove('active'); b.setAttribute('aria-pressed','false'); });
            btn.classList.add('active');
            btn.setAttribute('aria-pressed','true');
        });
    });

    document.getElementById('btn-submit-order').addEventListener('click', () => {
        if (!selectedInstrument) return;
        const selectedSide = document.querySelector('.side-btn.active');
        const errorDisplay = document.getElementById('order-error');
        if (!selectedSide) { errorDisplay.textContent = 'Pilih beli atau jual terlebih dahulu.'; errorDisplay.focus(); return; }
        const side = selectedSide.dataset.side;
        const qtyLots = Number(inputOrderQty.value);
        const limitP = inputOrderLimit.value;
        inputOrderQty.setCustomValidity(Number.isSafeInteger(qtyLots) && qtyLots > 0 ? '' : 'Sebutkan jumlah unit bulat yang positif.');
        inputOrderLimit.setCustomValidity(Number(limitP) > 0 ? '' : 'Harga limit harus positif.');
        if (!inputOrderQty.reportValidity() || !inputOrderLimit.reportValidity()) return;
        const totalShares = qtyLots * selectedInstrument.lotSize;

        const type = document.getElementById('order-type').value;
        const payload = {
            accountId: currentAccount && currentAccount.id,
            instrumentId: selectedInstrument.id,
            side, type,
            quantity: totalShares,
            requestId: 'REQ-UI-' + crypto.randomUUID()
        };
        if (type === 'LIMIT') payload.limitPrice = limitP;
        if (type === 'STOP_LOSS' || type === 'TAKE_PROFIT') payload.triggerPrice = limitP;
        if (!bridge.previewOrder) {
            errorDisplay.textContent = 'Preview domain belum tersedia di browser pratinjau. Tidak ada order ekonomi yang dikirim.';
            errorDisplay.focus(); return;
        }
        try {
            const preview = JSON.parse(bridge.previewOrder(JSON.stringify(payload)));
            if (!preview.success) { errorDisplay.textContent = preview.message || 'Preview ditolak domain'; errorDisplay.focus(); return; }
            showOrderReview(preview, payload);
        } catch (error) {
            errorDisplay.textContent = 'Preview gagal: ' + error.message; errorDisplay.focus();
        }
    });

    document.getElementById('search-stock-input').addEventListener('input', loadStocks);
    document.getElementById('filter-exchange-select').addEventListener('change', loadStocks);
    document.getElementById('filter-list-select').addEventListener('change', loadStocks);

    const ipoBtn = document.getElementById('btn-ipo-center');
    if (ipoBtn) ipoBtn.addEventListener('click', () => {
        refreshSnapshot();
        if (latestSnapshot && latestSnapshot.recoveryRequired) return;
        openIpoCenter();
    });

    const multiBtn = document.getElementById('btn-multiasset');
    if (multiBtn) multiBtn.addEventListener('click', () => {
        refreshSnapshot();
        if (latestSnapshot && latestSnapshot.recoveryRequired) return;
        openMultiAssets();
    });

    document.getElementById('btn-order-history').addEventListener('click', () => {
        refreshSnapshot();
        if (latestSnapshot && latestSnapshot.recoveryRequired) return;
        const panel = showFlow('Order aktif / riwayat');
        flowText(panel, 'Akun', currentAccount ? `${currentAccount.name} (${currentAccount.id})` : 'Belum tersedia');
        if (!bridge.getOrders || !currentAccount) { flowText(panel, 'Riwayat', 'Bridge riwayat domain belum tersedia.'); return; }
        const label = document.createElement('label');
        const activeOnly = document.createElement('input'); activeOnly.type = 'checkbox'; activeOnly.checked = true;
        label.append(activeOnly, document.createTextNode('Tampilkan hanya order aktif')); panel.append(label);
        const list = document.createElement('div'); panel.append(list);
        const render = () => {
            list.replaceChildren();
            try {
                const orders = JSON.parse(bridge.getOrders(currentAccount.id, activeOnly.checked));
                if (!orders.length) flowText(list, 'Order', 'Belum ada order untuk filter ini.');
                orders.forEach(order => flowButton(list,
                    `${order.instrumentId} ${order.side} ${order.status} — ${order.filledQuantity}/${order.quantity} saham — ${order.requestId}`,
                    () => { activeRequestId = order.requestId; orderDraft = null; orderPreview = null; showOrderReceipt(order); }));
            } catch (error) { flowText(list, 'Riwayat gagal dimuat', error.message); }
        };
        activeOnly.addEventListener('change', render); render();
    });

    document.getElementById('btn-briefing').addEventListener('click', () => {
        refreshSnapshot();
        if (latestSnapshot && latestSnapshot.recoveryRequired) return;
        const panel = showFlow('Briefing sesi');
        const clock = latestSnapshot && latestSnapshot.clock;
        flowText(panel, 'Waktu game', clock ? `${clock.date} ${clock.time} — ${clock.sessionLabel}` : 'Belum tersedia');
        flowText(panel, 'Akun', currentAccount ? currentAccount.name : 'Belum tersedia');
        flowText(panel, 'Batas informasi', 'Periksa riset dan kewajiban akun sebelum menjalankan waktu.');
        flowText(panel, 'Waktu normal', '24 jam game = 5 menit nyata. Waktu dijeda saat aplikasi masuk latar belakang.');
        if (bridge.setClockState && clock) {
            const speed = document.createElement('select'); speed.className = 'input-field';
            speed.id = 'briefing-speed';
            const label = document.createElement('label'); label.htmlFor = speed.id; label.textContent = 'Kecepatan waktu';
            [1, 2, 5].forEach(value => { const option = document.createElement('option'); option.value = value; option.textContent = `${value}×`; speed.append(option); });
            speed.value = clock.speed; panel.append(label, speed);
            const updateClock = playing => {
                try {
                    const result = JSON.parse(bridge.setClockState(playing, Number(speed.value)));
                    lastClockSample = performance.now();
                    if (!result.clock) throw new Error(result.message || 'Clock belum terkonfirmasi');
                    refreshSnapshot();
                    flowText(panel, 'Waktu terkonfirmasi', `${result.clock.isPaused ? 'Dijeda' : 'Berjalan'} ${result.clock.speed}×`);
                } catch (error) { flowText(panel, 'Kontrol waktu gagal', error.message); }
            };
            flowButton(panel, 'Mulai / lanjutkan waktu', () => updateClock(true));
            flowButton(panel, 'Jeda waktu', () => updateClock(false));
        } else flowText(panel, 'Kontrol', 'Bridge kontrol domain belum tersedia pada pratinjau ini.');
        if (bridge.skipDay && clock) flowButton(panel, 'Lewati 24 jam game', () => {
            const paused = JSON.parse(bridge.setClockState(false, clock.speed));
            lastClockSample = performance.now();
            if (!paused.clock) return;
            const review = showFlow('Tinjau skip hari', true);
            flowText(review, 'Durasi', '24 jam game dari waktu saat ini. Waktu tetap dijeda setelah selesai.');
            flowText(review, 'Dampak', 'Order dapat terisi, pemicu diperiksa, serta biaya harian, fee klien, laporan dan aksi korporasi diproses.');
            if (bridge.getOrders && currentAccount) {
                const orders = JSON.parse(bridge.getOrders(currentAccount.id, true));
                flowText(review, 'Order aktif pada akun terpilih', String(orders.length));
            }
            const requestId = 'REQ-SKIP-' + crypto.randomUUID();
            let completed = false;
            const confirm = flowButton(review, 'Konfirmasi skip 24 jam', event => {
                event.currentTarget.disabled = true;
                try {
                    const result = JSON.parse(bridge.skipDay(requestId));
                    if (!result.success) throw new Error(result.message || 'Skip perlu diperiksa; waktu tetap dijeda.');
                    completed = true;
                    flowText(review, 'Selesai', `${result.from} → ${result.to}`);
                    flowText(review, 'Penyimpanan', 'Hasil tersimpan. Lihat laporan sesi untuk rincian.');
                    refreshSnapshot();
                } catch (error) {
                    flowText(review, 'Belum terkonfirmasi', error.message);
                    confirm.textContent = 'Periksa / coba ulang permintaan yang sama';
                } finally {
                    confirm.disabled = completed;
                    lastClockSample = performance.now();
                }
            });
        });
        if (bridge.closeDay) {
            flowButton(panel, 'Tutup Hari Bursa & Evaluasi Sesi', () => {
                const reqId = 'REQ-DAY-CLOSE-' + Date.now();
                try {
                    const res = JSON.parse(bridge.closeDay(reqId));
                    if (!res.success) throw new Error(res.message || 'Penutupan hari gagal');
                    flowText(panel, 'Rekapitulasi Sesi Berhasil',
                        `Tanggal: ${res.date} | Starting Net Worth: ${res.startingNetWorth} | Ending Net Worth: ${res.endingNetWorth} | PnL Investasi: ${res.investmentPnL} | Gaji Pokok: ${res.salary} | Total Fee: ${res.fees} | Order Match: ${res.fills} | Evaluasi: ${res.note}`);
                    refreshSnapshot();
                } catch (err) {
                    flowText(panel, 'Penutupan hari ditolak / gagal', err.message);
                }
            });
        } else {
            flowText(panel, 'Penutupan hari', 'Bridge closeDay belum tersedia.');
        }
    });

    document.getElementById('btn-career').addEventListener('click', () => {
        refreshSnapshot();
        if (latestSnapshot && latestSnapshot.recoveryRequired) return;
        const panel = showFlow('Karier / misi');
        const career = latestSnapshot && latestSnapshot.career;
        if (!career) { flowText(panel, 'Progres', 'Snapshot karier domain belum tersedia.'); return; }
        metricCard(panel,'JABATAN SAAT INI',career.title,`Hari ${career.days} · Reputasi ${career.reputation}/100`);
        flowText(panel, 'Hari', career.days);
        if (career.nextPromotion) {
            const next = career.nextPromotion;
            flowText(panel, `Syarat ${next.title}`, `Hari ${next.days}, reputasi ${next.reputation}, kekayaan pribadi ${Number(next.netWorthIdr).toLocaleString('id-ID', {style:'currency',currency:'IDR',maximumFractionDigits:0})}`);
        }
        panel.append(node('p','notice','Selesaikan target melalui aktivitas permainan. Promosi dinilai setelah klaim dan penutupan hari.'));
        career.missions.forEach(mission => {
            const section = document.createElement('article'); section.className = 'card'; section.dataset.missionId = mission.id;
            const title = document.createElement('h2'); title.textContent = mission.title; section.append(title);
            flowText(section, 'Tujuan', mission.description);
            flowText(section, 'Status', mission.claimed ? 'Sudah diklaim' : mission.completed ? 'Selesai; dapat ditinjau untuk klaim' : 'Belum selesai');
            flowText(section, 'Imbalan misi', mission.reward);
            if (mission.completed && !mission.claimed && bridge.claimMission) {
                flowButton(section, `Tinjau klaim ${mission.id}`, () => {
                    const review = showFlow('Tinjau klaim misi', true);
                    flowText(review, 'Akun', currentAccount.name);
                    flowText(review, 'Misi', `${mission.id} — ${mission.title}`);
                    flowText(review, 'Imbalan misi', mission.reward);
                    flowText(review, 'Akibat', 'Imbalan masuk satu kali ke akun pribadi dan memperbarui reputasi.');
                    flowButton(review, 'Klaim imbalan', event => {
                        event.currentTarget.disabled = true;
                        try {
                            const result = JSON.parse(bridge.claimMission(mission.id));
                            const receipt = showFlow('Hasil klaim misi', true);
                            flowText(receipt, 'Status', result.success ? result.status : 'DITOLAK');
                            flowText(receipt, 'Akun', `${currentAccount.name} (${currentAccount.id})`);
                            flowText(receipt, 'Misi', mission.id);
                            flowText(receipt, 'Imbalan misi', result.reward);
                            flowText(receipt, 'Pesan', result.message || 'Tercatat pada ledger akun pribadi');
                            refreshSnapshot();
                        } catch (error) { flowText(review, 'Klaim belum terkonfirmasi', 'Periksa status misi sebelum mencoba kembali.'); }
                    });
                });
            }
            panel.append(section);
        });
        flowText(panel, 'Progres', 'Misi dinilai dari transaksi dan posisi. Promosi diperiksa setelah klaim dan penutupan hari; XP tidak membuka fitur.');
    });

    document.getElementById('btn-reports').addEventListener('click', () => {
        refreshSnapshot();
        if (latestSnapshot && latestSnapshot.recoveryRequired) return;
        const panel = showFlow('Laporan tersimpan');
        if (!bridge.getSessionReports) { flowText(panel, 'Laporan', 'Laporan belum dapat dimuat.'); return; }
        try {
            const reports = JSON.parse(bridge.getSessionReports());
            if (!reports.length) flowText(panel, 'Laporan', 'Belum ada hari yang ditutup. Membuka halaman ini tidak menutup sesi.');
            reports.forEach(report => flowButton(panel, `Laporan ${report.date} — ${report.investmentPnL}`, () => {
                const detail = showFlow('Laporan hari');
                flowText(detail, 'Tanggal / akun', `${report.date} / ${report.accountId}`);
                flowText(detail, 'NAV awal', report.startingNetWorth);
                flowText(detail, 'NAV akhir sebelum gaji penutupan', report.endingNetWorth);
                flowText(detail, 'Arus noninvestasi sebelum gaji penutupan', report.nonInvestmentFlows);
                flowText(detail, 'Hasil investasi (neto biaya; arus noninvestasi dipisah)', report.investmentPnL);
                flowText(detail, 'Biaya tercakup pada hasil di atas', report.fees);
                flowText(detail, 'Gaji uang game', report.salary);
                flowText(detail, 'Jumlah fill', report.fills);
                flowText(detail, 'Catatan', report.note);
                flowText(detail, 'Batas', 'Atribusi kontribusi, kewajiban lengkap dan aturan lintas bursa masih dalam remediasi. Ini laporan tersimpan, bukan penutupan ulang.');
            }));
        } catch (error) { flowText(panel, 'Laporan gagal dimuat', error.message); }
    });

    function openEntryPicker() {
        const panel = showFlow('Mulai & kelola permainan');
        flowText(panel, 'Informasi', 'Kelola sesi permainan trading saham. Anda dapat melanjutkan checkpoint aktif, memulai karir baru, atau mempelajari tutorial interaktif.');

        const slotSection = document.createElement('section');
        slotSection.className = 'card';
        const slotTitle = document.createElement('h2');
        slotTitle.className = 'card-title';
        slotTitle.textContent = 'Slot simpanan aktif';
        slotSection.append(slotTitle);

        refreshSnapshot();
        if (latestSnapshot && latestSnapshot.personalAccount) {
            const acc = latestSnapshot.personalAccount;
            flowText(slotSection, 'Akun aktif', `${acc.name} (${acc.id})`);
            flowText(slotSection, 'Mode simulasi', latestSnapshot.gameMode || 'Pasar Internal Game');
            flowText(slotSection, 'Kas tersedia IDR', acc.cash.IDR.available);
            flowText(slotSection, 'Kas tersedia USD', acc.cash.USD.available);
            flowText(slotSection, 'Total ekuitas/portofolio', acc.netWorth);
        } else {
            flowText(slotSection, 'Status slot', 'Belum ada profil aktif.');
        }

        flowButton(slotSection, 'Lanjutkan sesi aktif (Resume)', () => {
            closeFlow();
            switchTab('beranda');
        });
        panel.append(slotSection);

        const newGameSection = document.createElement('section');
        newGameSection.className = 'card';
        const newGameTitle = document.createElement('h2');
        newGameTitle.className = 'card-title';
        newGameTitle.textContent = 'Mulai permainan baru';
        newGameSection.append(newGameTitle);
        flowText(newGameSection, 'Profil baru', 'Konfigurasikan modal awal dan mode pasar untuk memulai siklus karir dari nol.');
        flowButton(newGameSection, 'Konfigurasi permainan baru', () => {
            openNewGameModal();
        });
        panel.append(newGameSection);

        const tutSection = document.createElement('section');
        tutSection.className = 'card';
        const tutTitle = document.createElement('h2');
        tutTitle.className = 'card-title';
        tutTitle.textContent = 'Panduan & tutorial interaktif';
        tutSection.append(tutTitle);
        flowText(tutSection, 'Tutorial', 'Pelajari alur pasar, pembacaan order book, tiket beli/jual, mitigasi risiko, dan evaluasi harian.');
        flowButton(tutSection, 'Buka panduan tutorial trading', () => {
            openTutorialModal();
        });
        panel.append(tutSection);
    }

    function openNewGameModal() {
        const panel = showFlow('Mulai permainan baru', true);
        flowText(panel, 'Peringatan', 'Memulai permainan baru akan mereset kas, portofolio, dan slot simpanan lokal ke kondisi awal.');

        const formCard = document.createElement('section');
        formCard.className = 'card';
        const h = document.createElement('h2');
        h.className = 'card-title';
        h.textContent = 'Pengaturan modal awal & mode';
        formCard.append(h);

        const capitalLabel = document.createElement('label');
        capitalLabel.htmlFor = 'newgame-capital-select';
        capitalLabel.textContent = 'Pilih tingkat modal awal';
        const capitalSelect = document.createElement('select');
        capitalSelect.id = 'newgame-capital-select';
        capitalSelect.className = 'input-field';

        [
            { value: '25000000', label: 'Rp 25.000.000 — Pemula (Retail Trader)' },
            { value: '50000000', label: 'Rp 50.000.000 — Standar (Prop Trader)' },
            { value: '100000000', label: 'Rp 100.000.000 — Profesional (Fund Manager)' }
        ].forEach(opt => {
            const el = document.createElement('option');
            el.value = opt.value;
            el.textContent = opt.label;
            if (opt.value === '50000000') el.selected = true;
            capitalSelect.append(el);
        });

        const modeLabel = document.createElement('label');
        modeLabel.textContent = 'Mode pasar';
        const modeInfo = document.createElement('p');
        modeInfo.style.fontWeight = 'bold';
        modeInfo.textContent = 'Pasar Internal Game (Simulasi Terpadu IDX & US)';

        const alertText = document.createElement('p');
        alertText.setAttribute('role', 'alert');

        const startBtn = document.createElement('button');
        startBtn.className = 'btn-primary';
        startBtn.textContent = 'Mulai perjalanan karir baru';
        startBtn.onclick = () => {
            if (!bridge.startNewGame) {
                alertText.textContent = 'Bridge startNewGame belum tersedia.';
                alertText.style.color = 'var(--negative)';
                return;
            }
            const cap = Number(capitalSelect.value);
            try {
                const res = JSON.parse(bridge.startNewGame(JSON.stringify({ mode: 'INTERNAL_MARKET', initialCapital: cap })));
                if (!res.success) throw new Error(res.message);
                closeFlow();
                refreshSnapshot();
                switchTab('beranda');
            } catch (err) {
                alertText.textContent = 'Gagal memulai permainan: ' + err.message;
                alertText.style.color = 'var(--negative)';
            }
        };

        formCard.append(capitalLabel, capitalSelect, modeLabel, modeInfo, alertText, startBtn);
        panel.append(formCard);
    }

    function openTutorialModal() {
        const panel = showFlow('Panduan tutorial interaktif');
        flowText(panel, 'Pengantar', 'Pelajari alur trading dan karier. Hasil transaksi bergantung pada harga, likuiditas, biaya dan keputusanmu.');

        const steps = [
            {
                step: '1. Siklus Sesi & Briefing Pasar',
                desc: 'Setiap hari bursa dimulai dengan Market Briefing pada pukul 08:30 WIB. Periksa sentimen berita makro dan emiten sebelum bel pembukaan pasar 09:00 WIB.',
                action: 'Buka Briefing Pasar',
                handler: () => {
                    closeFlow();
                    document.getElementById('btn-briefing').click();
                }
            },
            {
                step: '2. Pembacaan Order Book Bid & Ask',
                desc: 'Order Book menampilkan antrean beli (Bid) dan antrean jual (Ask) terbaik. Kuotasi bergerak dinamis berdasarkan permintaan pasar dan kuotasi Market Maker.',
                action: 'Buka Menu Pasar',
                handler: () => {
                    closeFlow();
                    switchTab('pasar');
                }
            },
            {
                step: '3. Tiket Order & Eksekusi',
                desc: 'Limit mengantre pada harga yang ditentukan. Market memakai likuiditas yang tersedia; jumlah dan harga fill bisa berbeda dari perkiraan.',
                action: 'Buka Tiket Order Saham',
                handler: () => {
                    closeFlow();
                    switchTab('pasar');
                    try {
                        const stocks = JSON.parse(bridge.getInstruments('', '', null, true));
                        if (stocks && stocks.length > 0) openOrderTicket(stocks[0]);
                    } catch (e) {}
                }
            },
            {
                step: '4. Portofolio, Risiko & Margin',
                desc: 'Pantau posisi terbuka, keuntungan/kerugian mengambang, serta fasilitas pinjaman margin multi-mata uang IDR dan USD dengan batas rasio kesehatan ekuitas.',
                action: 'Periksa Portofolio & Risiko',
                handler: () => {
                    closeFlow();
                    switchTab('aset');
                }
            },
            {
                step: '5. Penutupan Hari & Rekapitulasi Karir',
                desc: 'Pasar reguler ditutup pukul 16:00 WIB. Lakukan tutup hari untuk menerima gaji pokok harian, memverifikasi laba/rugi terealisasi, dan membuka tingkatan karir baru.',
                action: 'Buka Evaluasi Karir & Misi',
                handler: () => {
                    closeFlow();
                    document.getElementById('btn-career').click();
                }
            }
        ];

        steps.forEach(s => {
            const card = document.createElement('article');
            card.className = 'card';
            const h = document.createElement('h3');
            h.textContent = s.step;
            card.append(h);
            flowText(card, 'Panduan', s.desc);
            if (s.action && s.handler) {
                flowButton(card, s.action, s.handler);
            }
            panel.append(card);
        });
    }

    const entryGameBtn = document.getElementById('btn-entry-game');
    if (entryGameBtn) entryGameBtn.addEventListener('click', openEntryPicker);

    function openAchievementsModal() {
        const panel = showFlow('Level & Pencapaian');
        if (!bridge.getPlayerProgress) {
            flowText(panel, 'Progres', 'Progres pemain belum dapat dimuat.');
            return;
        }
        try {
            const prog = JSON.parse(bridge.getPlayerProgress());
            metricCard(panel, 'PROGRES PEMAIN', `Level ${prog.level}`,`${prog.achievementsUnlocked}/${prog.totalAchievements} badge diraih`);
            flowText(panel, 'Total XP', `${prog.currentXp} XP (${prog.xpInCurrentLevel} / ${prog.levelSpanXp} XP menuju Level ${prog.level + 1})`);

            // Progress bar
            const track = document.createElement('div');
            track.className = 'xp-track';
            track.setAttribute('role', 'progressbar');
            track.setAttribute('aria-valuenow', prog.xpInCurrentLevel);
            track.setAttribute('aria-valuemin', 0);
            track.setAttribute('aria-valuemax', prog.levelSpanXp);
            const fill = document.createElement('span');
            const pct = Math.min(100, Math.max(0, (prog.xpInCurrentLevel / (prog.levelSpanXp || 1)) * 100));
            fill.style.width = `${pct}%`;
            track.appendChild(fill);
            panel.appendChild(track);

            flowText(panel, 'Pencapaian Terbuka', `${prog.achievementsUnlocked} dari ${prog.totalAchievements} selesai`);

            // Filter buttons
            const filterWrap = document.createElement('div');
            filterWrap.className = 'filter-chips';
            filterWrap.style.gap = '8px';
            filterWrap.style.margin = '12px 0';
            let currentFilter = 'all';

            const grid = document.createElement('div');
            grid.className = 'achievement-grid';

            function renderAchievements() {
                grid.innerHTML = '';
                const filtered = (prog.achievements || []).filter(a => {
                    if (currentFilter === 'open') return a.unlocked;
                    if (currentFilter === 'locked') return !a.unlocked;
                    return true;
                });

                if (!filtered.length) emptyState(grid,'Belum ada badge pada kategori ini','Lanjutkan misi dan aktivitas untuk membangun progres.');
                filtered.forEach(a => {
                    const card = document.createElement('article');
                    card.className = `achievement-card ${a.unlocked ? '' : 'locked'}`;
                    card.innerHTML = `
                        <div class="flex-row">
                            <span class="achievement-medal ${a.unlocked ? '' : 'locked'}">${a.icon === 'market' ? '📈' : a.icon === 'book' ? '📖' : a.icon === 'users' ? '👥' : a.icon === 'shield' ? '🛡️' : a.icon === 'globe' ? '🌐' : '📄'}</span>
                            <span class="change-pill ${a.unlocked ? 'positive' : ''}">${a.unlocked ? 'Terbuka' : 'Dalam Proses'}</span>
                        </div>
                        <h3 style="margin: 8px 0 4px; font-size: 1rem;">${a.name}</h3>
                        <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 8px;">${a.desc}</p>
                        <div class="flex-row" style="font-size: 0.8rem; color: var(--teal);">
                            <span>${a.progress}</span>
                            <strong>+${a.xp}</strong>
                        </div>
                    `;
                    grid.appendChild(card);
                });
            }

            ['all', 'open', 'locked'].forEach(f => {
                const btn = document.createElement('button');
                btn.className = 'btn-secondary';
                btn.style.flex = '1';
                btn.style.fontSize = '0.85rem';
                btn.textContent = f === 'all' ? 'Semua' : f === 'open' ? 'Terbuka' : 'Dalam Proses';
                btn.setAttribute('aria-pressed',String(f === currentFilter));
                btn.onclick = () => {
                    currentFilter = f;
                    [...filterWrap.children].forEach(child => child.setAttribute('aria-pressed',String(child === btn)));
                    renderAchievements();
                };
                filterWrap.appendChild(btn);
            });

            panel.appendChild(filterWrap);
            panel.appendChild(grid);
            renderAchievements();

            panel.append(node('p','notice','Badge memberi XP. Pembukaan fitur tetap mengikuti jabatan karier.'));
        } catch (e) {
            flowText(panel, 'Gagal memuat progres', e.message);
        }
    }

    function openAccountPicker() {
        if (!latestSnapshot || !latestSnapshot.tradingAccounts) return;
        const panel = showFlow('Pilih Akun Transaksi');
        flowText(panel, 'Keterangan', 'Pilih akun yang digunakan untuk portofolio dan penempatan order. Kekayaan pribadi di kanan atas tetap terpisah dari dana klien (AUM).');
        latestSnapshot.tradingAccounts.forEach(acc => {
            const isSelected = currentAccount && currentAccount.id === acc.id;
            flowButton(panel, `${acc.name} (${acc.type})${isSelected ? ' [Aktif]' : ''}`, () => {
                currentAccount = { id: acc.id, name: acc.name, type: acc.type };
                closeFlow();
                refreshSnapshot();
                if (currentTab === 'aset') loadAsetView();
            });
        });
    }

    const achBtn = document.getElementById('btn-achievements');
    if (achBtn) achBtn.addEventListener('click', openAchievementsModal);
    const lvlChip = document.getElementById('header-level-chip');
    if (lvlChip) lvlChip.addEventListener('click', openAchievementsModal);
    const wealthChip = document.getElementById('header-wealth-chip');
    if (wealthChip) wealthChip.addEventListener('click', () => switchTab('aset'));
    const accBtn = document.getElementById('context-account-btn');
    if (accBtn) accBtn.addEventListener('click', openAccountPicker);

    document.getElementById('btn-settings').addEventListener('click', () => {
        const panel = showFlow('Pengaturan / save');
        flowText(panel, 'Batas kandidat', 'Progres tersimpan di perangkat ini. Simpan sebelum memuat checkpoint atau memulai ulang.');
        flowButton(panel, 'Menu awal & mulai baru', openEntryPicker);
        flowButton(panel, 'Panduan tutorial interaktif', openTutorialModal);
        flowButton(panel, 'Level & pencapaian (Achievement)', openAchievementsModal);
        flowButton(panel, 'Simpan permainan', () => {
            try {
                const result = JSON.parse(bridge.saveGame());
                if (result.success) document.getElementById('native-save-warning')?.remove();
                flowText(panel, result.success ? 'Save terkonfirmasi' : 'Save gagal', result.message || 'Periksa status salinan valid terakhir');
            } catch (error) { flowText(panel, 'Save belum terkonfirmasi', error.message); }
        });
        flowButton(panel, 'Muat checkpoint tersimpan', () => {
            if (!window.confirm('Muat checkpoint terakhir? Perubahan sejak save akan diganti. Berkas asli tidak dihapus.')) return;
            try {
                const result = JSON.parse(bridge.loadGame());
                flowText(panel, result.success ? 'Checkpoint dimuat' : 'Pemulihan belum berhasil', result.message || 'Hasil dari domain');
                refreshSnapshot();
            } catch (error) { flowText(panel, 'Load gagal', error.message); }
        });
        flowButton(panel, 'Ukuran teks 200%', () => { document.documentElement.style.fontSize = '200%'; });
        flowButton(panel, 'Ukuran teks standar', () => { document.documentElement.style.fontSize = ''; });
        flowText(panel, 'Gerak', 'Preferensi reduced motion sistem dihormati. Preferensi teks lokal belum dipersist.');
    });

    document.getElementById('btn-home-market').addEventListener('click', () => switchTab('pasar'));
    const navResize = new ResizeObserver(entries => document.documentElement.style.setProperty('--nav-height', entries[0].target.getBoundingClientRect().height + 'px'));
    navResize.observe(document.querySelector('.bottom-nav'));
    document.addEventListener('keydown', event => {
        if (event.key === 'Escape' && !orderModal.classList.contains('active') && flowPanel) { event.preventDefault(); closeFlow(); }
    });
    // Initial Load
    refreshSnapshot(); loadHomeMarket();

    window.showNativeSaveFailure = () => {
        if (document.getElementById('native-save-warning')) return;
        const warning = document.createElement('p');
        warning.id = 'native-save-warning'; warning.setAttribute('role', 'alert');
        warning.className = 'mock-banner';
        warning.textContent = 'Progres terakhir belum tersimpan. Buka Pengaturan untuk mencoba simpan ulang.';
        document.body.prepend(warning);
    };

    // Measure elapsed active time only. Java owns the time ratio and market advancement.
    let lastClockSample = performance.now();
    document.addEventListener('visibilitychange', () => { lastClockSample = performance.now(); });
    setInterval(() => {
        const now = performance.now();
        if (document.hidden) { lastClockSample = now; return; }
        const elapsed = Math.floor((now - lastClockSample) / 1000);
        if (elapsed > 0 && bridge && bridge.tickClock) {
            lastClockSample += elapsed * 1000;
            const tick = JSON.parse(bridge.tickClock(elapsed));
            if (tick.success === false || tick.error) {
                const error = document.getElementById('clock-error') || document.createElement('p');
                error.id = 'clock-error'; error.setAttribute('role', 'alert');
                error.textContent = 'Waktu dijeda: ' + (tick.error || tick.message || 'proses hari perlu diperiksa.');
                if (!error.isConnected) document.querySelector('main').prepend(error);
            }
            refreshSnapshot();
            if (['beranda', 'pasar'].includes(currentTab) && !flowPanel) updateMarketQuotes();
        }
    }, 1000);

})();
