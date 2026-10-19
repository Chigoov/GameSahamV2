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
    let marketUniverse = [];
    let marketIndices = [];
    let latestSnapshot = null;

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
        currentTab = tab;
        document.querySelectorAll('.nav-item').forEach(b => {
            b.classList.toggle('active', b.dataset.tab === tab);
        });
        Object.keys(views).forEach(key => {
            views[key].classList.toggle('active', key === tab);
        });

        if (tab === 'pasar') loadPasarView();
        if (tab === 'aset') loadAsetView();
        if (tab === 'firma') loadFirmaView();
        if (tab === 'riset') loadRisetView();
        if (tab === 'beranda') refreshSnapshot();
    }

    // 4. Data Refreshers
    function refreshSnapshot() {
        try {
            const raw = bridge.getSnapshot();
            const snap = JSON.parse(raw);
            latestSnapshot = snap;
            document.getElementById('home-clock-state').textContent = snap.clock
                ? `${snap.clock.date} — ${snap.clock.sessionLabel}; ${snap.clock.isPaused ? 'dijeda' : 'berjalan'} ${snap.clock.speed}×`
                : 'Waktu domain belum tersedia.';
            document.getElementById('home-career-state').textContent = snap.career
                ? `${snap.career.title} — hari ${snap.career.days}; reputasi ${snap.career.reputation}. Parameter sementara.`
                : 'Progres karier domain belum tersedia pada pratinjau ini.';
            if (snap.recoveryRequired) {
                currentAccount = null;
                if (!flowPanel || flowPanel.getAttribute('aria-label') !== 'Pemulihan save diperlukan') {
                    const panel = showFlow('Pemulihan save diperlukan');
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
                document.getElementById('home-total-cash').textContent = idrCash.total;
                document.getElementById('home-avail-cash').textContent = idrCash.available;
                document.getElementById('home-reserved-cash').textContent = idrCash.reserved;
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
            const idxContainer = document.getElementById('indices-row');
            idxContainer.innerHTML = '';
            indices.forEach(idx => {
                const item = document.createElement('div');
                item.className = 'card';
                item.style.flex = '1';
                item.style.margin = '0 4px';
                item.style.padding = '10px';
                item.innerHTML = `
                    <div style="font-size:0.75rem; color:var(--text-muted);">${idx.ticker}</div>
                    <div style="font-size:1.1rem; font-weight:600; color:var(--action);">${idx.value} poin</div>
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

        stocks.forEach(st => {
            const item = document.createElement('div');
            item.className = 'stock-list-item';
            const changeVal = parseFloat(st.dailyChangePercent);
            const isPos = changeVal >= 0;
            const changeClass = isPos ? 'positive' : 'negative';
            const changeSign = isPos ? '+' : '';

            item.innerHTML = `
                <div>
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

    function loadAsetView() {
        refreshSnapshot();
        const posContainer = document.getElementById('portfolio-positions-list');
        posContainer.replaceChildren();
        const summary = document.querySelector('#view-aset .card');
        summary.replaceChildren();
        if (!currentAccount || !bridge.getPortfolio) {
            summary.textContent = 'Portofolio domain belum tersedia pada pratinjau ini.'; return;
        }
        try {
            if (latestSnapshot && latestSnapshot.tradingAccounts && latestSnapshot.tradingAccounts.length > 1) {
                const accSelect = document.createElement('select');
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
            flowText(summary, 'Akun', `${portfolio.accountName} (${portfolio.accountId})`);
            for (const currency of ['IDR', 'USD']) {
                const cash = portfolio.cash[currency];
                flowText(summary, `${currency} total`, cash.total);
                flowText(summary, `${currency} tersedia`, cash.available);
                flowText(summary, `${currency} tertahan`, cash.reserved);
            }
            flowText(summary, 'Kurs valuasi USD/IDR', portfolio.valuationRateUsdIdr);
            flowButton(summary, 'Tukar IDR / USD', () => openFxForm());
            flowButton(summary, 'Fasilitas margin & risiko', () => openRiskModal());
            flowButton(summary, 'Aksi korporasi & voting RUPS', () => openCorporateActionModal());
            flowButton(summary, 'Uji stres portofolio (S37)', () => openStressTestModal());
            flowButton(summary, 'Lab strategi & sandbox what-if (S38)', () => openStrategyLabModal());
            flowButton(summary, 'Replay historis (S39)', () => openHistoricalReplayModal());
            if (!portfolio.positions.length) flowText(posContainer, 'Posisi', 'Belum ada kepemilikan');
            portfolio.positions.forEach(position => {
                const button = flowButton(posContainer, `${position.instrumentId}: ${position.quantity} saham — ${position.marketValue}`, () => {
                    const panel = showFlow('Detail posisi');
                    flowText(panel, 'Akun', portfolio.accountName);
                    Object.entries(position).forEach(([label, value]) => flowText(panel, label, value));
                    flowText(panel, 'Batas', 'Realized P/L dan kewajiban lengkap masih dalam audit domain.');
                });
                button.className = 'btn-secondary';
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
        } catch (error) { summary.textContent = 'Portofolio gagal dimuat: ' + error.message; }
    }

    function loadFirmaView() {
        refreshSnapshot();
        const view = views.firma;
        view.replaceChildren();
        if (!bridge.getFirmSnapshot) { flowText(view, 'Firma', 'Snapshot domain belum tersedia pada pratinjau ini.'); return; }
        try {
            const firm = JSON.parse(bridge.getFirmSnapshot());
            if (!firm.success) throw new Error(firm.message || 'Snapshot firma belum tersedia');
            const summary = document.createElement('section'); summary.className = 'card';
            flowText(summary, 'AUM klien aktif (IDR)', firm.aumIdr);
            flowText(summary, 'Kas pribadi IDR (bukan AUM)', firm.personalCashIdr);
            flowText(summary, 'Klien aktif', firm.activeClientCount);
            flowText(summary, 'Kurs valuasi USD/IDR', firm.valuationRateUsdIdr);
            flowText(summary, 'Snapshot perangkat', new Date(firm.timestamp).toLocaleString('id-ID'));
            flowButton(summary, 'Fund Builder & alokasi portofolio', () => openFundBuilder());
            flowButton(summary, 'Laporan klien & fee mandat', () => openClientReports());
            flowText(summary, 'Batas', 'Parameter mandat existing masih sementara (OQ-11). Ringkasan kewajiban dan pendapatan manajer belum lengkap.');
            view.append(summary);
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
                    flowButton(panel, 'Terima Mandat Kelolaan Klien (S20 & S21)', () => {
                        try {
                            const res = JSON.parse(bridge.acceptClientMandate(mandate.id));
                            if (!res.success) throw new Error(res.message);
                            flowText(panel, 'Status Penerimaan', `${res.message} (Akun: ${res.accountId})`);
                            refreshSnapshot();
                            loadFirmaView();
                        } catch (err) {
                            flowText(panel, 'Penerimaan Mandat Ditolak', err.message);
                        }
                    });
                } else if (mandate.status === 'ACTIVE') {
                    flowText(panel, 'Status Mandat', `Mandat aktif dikelola. Akun kelolaan: ${mandate.accountId}`);
                }
            }));
        } catch (error) { flowText(view, 'Firma gagal dimuat', error.message); }
    }

    function openResearchTiersModal() {
        const panel = showFlow('Paket riset & langganan terminal (S29 & S30)');
        flowText(panel, 'Informasi', 'Tingkatan terminal riset finansial berjenjang (FR-10.04). Biaya langganan bulanan didebit dari saldo kas USD game melalui buku besar.');

        if (!bridge.getResearchTiers || !bridge.subscribeResearchTier) {
            flowText(panel, 'Paket', 'Bridge paket riset belum tersedia.'); return;
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

        if (!bridge.getResearchFeed) {
            flowText(view, 'Riset', 'Bridge riset domain belum tersedia.'); return;
        }

        try {
            const feed = JSON.parse(bridge.getResearchFeed());

            // 1. Status Terminal & Paket (S29 & S30)
            const tierSection = document.createElement('section');
            tierSection.className = 'card';
            const tierTitle = document.createElement('h2');
            tierTitle.className = 'card-title';
            tierTitle.textContent = 'Paket terminal riset aktif (S29 & S30)';
            tierSection.append(tierTitle);
            flowText(tierSection, 'Tingkat paket', `${feed.tierTitle} (${feed.tier})`);
            flowText(tierSection, 'Masa berlaku', feed.tier === 'BASIC' ? 'Permanen (Gratis)' : new Date(feed.expiresAt).toLocaleDateString('id-ID'));
            flowButton(tierSection, 'Lihat paket & opsi langganan', () => openResearchTiersModal());
            view.append(tierSection);

            // 2. Berita Pasar (S26)
            const newsSection = document.createElement('section');
            newsSection.className = 'card';
            const newsTitle = document.createElement('h2');
            newsTitle.className = 'card-title';
            newsTitle.textContent = 'Berita pasar & riset terkini (S26)';
            newsSection.append(newsTitle);
            flowText(newsSection, 'Anti-Future Leak', 'Seluruh berita terbit sesuai timestamp simulasi (FR-10.01), mencegah kebocoran informasi masa depan.');

            if (!feed.news || !feed.news.length) {
                flowText(newsSection, 'Berita', 'Belum ada berita terbit saat ini.');
            } else {
                feed.news.forEach(n => {
                    const card = document.createElement('article');
                    card.className = 'card';
                    const h = document.createElement('h3');
                    h.textContent = n.headline;
                    card.append(h);
                    flowText(card, 'Kategori & emiten', `${n.categoryLabel} ${n.relatedTicker ? '— ' + n.relatedTicker : ''}`);
                    flowText(card, 'Isi berita', n.content);
                    const sent = n.sentimentImpact > 0 ? `+${(n.sentimentImpact * 100).toFixed(0)}% (Positif)` : `${(n.sentimentImpact * 100).toFixed(0)}% (Netral/Negatif)`;
                    flowText(card, 'Sentimen pasar', sent);
                    newsSection.append(card);
                });
            }
            view.append(newsSection);

            // 3. Konsensus Analis (S27)
            const analystSection = document.createElement('section');
            analystSection.className = 'card';
            const analystTitle = document.createElement('h2');
            analystTitle.className = 'card-title';
            analystTitle.textContent = 'Konsensus analis & target harga (S27)';
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

            // 4. Kalender Finansial (S28)
            const calSection = document.createElement('section');
            calSection.className = 'card';
            const calTitle = document.createElement('h2');
            calTitle.className = 'card-title';
            calTitle.textContent = 'Kalender bursa & ekonomi makro (S28)';
            calSection.append(calTitle);

            if (!feed.calendar || !feed.calendar.length) {
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

        } catch (e) { flowText(view, 'Gagal memuat feed riset', e.message); }
    }

    let flowPanel = null;
    let activeRequestId = null;
    let orderDraft = null;
    let orderPreview = null;
    function showFlow(title) {
        if (flowPanel) flowPanel.remove();
        flowPanel = document.createElement('section');
        flowPanel.className = 'flow-page';
        flowPanel.setAttribute('aria-label', title);
        const back = document.createElement('button');
        back.className = 'btn-secondary'; back.textContent = 'Kembali';
        back.onclick = closeFlow;
        const heading = document.createElement('h1'); heading.textContent = title; heading.tabIndex = -1;
        const scope = document.createElement('p'); scope.textContent = scopeLabel;
        flowPanel.append(back, heading, scope);
        document.body.append(flowPanel);
        document.querySelectorAll('body > :not(.flow-page):not(script)').forEach(node => node.inert = true);
        heading.focus();
        return flowPanel;
    }
    function closeFlow(recovered = false) {
        if (!flowPanel) return false;
        if (flowPanel.getAttribute('aria-label') === 'Pemulihan save diperlukan' && recovered !== true) return true;
        flowPanel.remove(); flowPanel = null;
        document.querySelectorAll('body > :not(script)').forEach(node => node.inert = false);
        return true;
    }
    function flowText(panel, label, value) {
        const p = document.createElement('p'); p.textContent = `${label}: ${value == null ? 'Belum tersedia' : value}`;
        panel.append(p);
    }
    function flowButton(panel, label, action) {
        const button = document.createElement('button'); button.className = 'btn-primary';
        button.textContent = label; button.onclick = action; panel.append(button); return button;
    }
    function openIndexDetail(index) {
        const panel = showFlow('Detail indeks');
        if (!bridge.getIndexDetail) {
            flowText(panel, 'Indeks', 'Bridge detail indeks belum tersedia pada pratinjau ini.'); return;
        }
        try {
            const detail = JSON.parse(bridge.getIndexDetail(index.id));
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
        const panel = showFlow('Detail instrumen');
        flowText(panel, 'Instrumen', `${stock.ticker} — ${stock.name} (${stock.exchange})`);
        flowText(panel, 'Mode', isNativeHost ? modeLabel.textContent : 'Pratinjau UI — bukan data ekonomi');
        flowText(panel, 'Mata uang / unit', `${stock.currency} / ${stock.lotSize} saham per unit`);
        flowText(panel, 'Harga pasar game', stock.currentPrice);
        flowText(panel, 'Grafik', 'Riwayat harga domain belum tersedia; tidak menampilkan grafik contoh.');
        if (bridge.getOrderBook) {
            try {
                const book = JSON.parse(bridge.getOrderBook(stock.id));
                flowText(panel, 'Status pasar', book.haltState);
                for (const [key, name] of [['bids', 'Bid'], ['asks', 'Ask']]) {
                    flowText(panel, name, (book[key] || []).map(level => `${level.price} ${stock.currency} × ${level.quantity} saham`).join('; ') || 'Belum ada antrean');
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
        } else flowText(panel, 'Watchlist', 'Bridge watchlist belum tersedia pada pratinjau ini.');
        const create = flowButton(panel, 'Buat order', () => { closeFlow(); openOrderTicket(stock); });
        create.disabled = !stock.isActiveTrading;
    }
    function showOrderReview(preview, draft) {
        closeOrderTicket();
        orderDraft = draft; orderPreview = preview;
        const panel = showFlow('Tinjau order');
        flowText(panel, 'Akun', `${preview.accountName} (${preview.accountId})`);
        flowText(panel, 'Mode', modeLabel.textContent);
        flowText(panel, 'Instrumen', `${draft.instrumentId} — ${preview.currency}`);
        flowText(panel, 'Order', `${draft.side} ${draft.type} ${draft.quantity} saham`);
        flowText(panel, 'Harga / pemicu', `${draft.limitPrice || draft.triggerPrice || 'Market sesuai likuiditas'} ${preview.currency} per saham`);
        flowText(panel, 'Estimasi fill / sisa', `${preview.estimatedFillQuantity} / ${preview.remainingQuantity} saham`);
        flowText(panel, 'Estimasi VWAP / slippage %', `${preview.estimatedVwap ?? 'Tidak ada fill'} / ${preview.slippagePercent ?? 'Tidak ada fill'}`);
        flowText(panel, 'Estimasi fee', preview.estimatedFee);
        flowText(panel, 'Estimasi dampak kas', preview.estimatedCashImpact);
        flowText(panel, 'Kas diperlukan', preview.requiredCash);
        flowText(panel, 'Kas tersedia', preview.availableCash);
        flowText(panel, 'Estimasi pada', new Date(preview.timestamp).toLocaleString('id-ID') + ' (waktu perangkat untuk umur preview)');
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
        const panel = showFlow('Status order');
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
        flowText(panel, 'Informasi', 'Hak aksi korporasi (Dividen, Stock Split) dan hak suara RUPS berdasarkan kepemilikan saham pada recording date (FR-12 & FR-14).');

        const corpSection = document.createElement('section');
        corpSection.className = 'card';
        const corpTitle = document.createElement('h2');
        corpTitle.className = 'card-title';
        corpTitle.textContent = 'Aksi korporasi terjadwal (FR-12)';
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
        voteTitle.textContent = 'Agenda voting RUPS (FR-14)';
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
        flowText(panel, 'Penawaran IPO', 'Pemesanan saham perdana (FR-13). Dana tertahan (reserve) saat pemesanan; saat penjatahan (allotment), sisa dana dikembalikan utuh (refund).');
        if (!bridge.getIpoOfferings) {
            flowText(panel, 'IPO', 'Bridge penawaran IPO belum tersedia.'); return;
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
        const panel = showFlow('Katalog multi-aset (S24)');
        flowText(panel, 'Informasi', 'Instrumen non-ekuitas (FR-11.01): Obligasi Negara & Komoditas Emas Fisik. Perdagangan aktif sekunder bersifat provisi/simulasi katalog.');

        if (!bridge.getMultiAssets) {
            flowText(panel, 'Multi-aset', 'Bridge multi-aset belum tersedia.'); return;
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
        const panel = showFlow('Fund Builder & alokasi portofolio (S23)');
        flowText(panel, 'Konsep', 'Rancang portofolio terdiversifikasi (FR-11.05). Menyimpan alokasi target tanpa mutasi saldo fisik. Eksekusi rebalancing menerbitkan order penyeimbang secara atomik.');

        if (!bridge.getFundPlans || !bridge.saveFundPlan) {
            flowText(panel, 'Fund Builder', 'Bridge Fund Builder belum tersedia.'); return;
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
        const panel = showFlow('Laporan klien & fee mandat (S22)');
        flowText(panel, 'Transparansi', 'Rekapitulasi akun mandat klien institusi/individu (FR-11.02 & FR-11.04). Management fee tahunan dan performance fee dihitung terhadap High-Water Mark.');

        if (!bridge.getClientReports) {
            flowText(panel, 'Laporan klien', 'Bridge laporan klien belum tersedia.'); return;
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
                    flowText(card, 'Profil risiko & benchmark', `${r.riskProfile} (Benchmark: ${r.benchmark || 'IHSG'})`);
                    flowText(card, 'Setoran modal awal', r.initialDeposit || r.initialCapital || 'Rp 0');
                    flowText(card, 'High-Water Mark (HWM)', r.highWaterMark || 'Rp 0');
                    flowText(card, 'Skor kepercayaan klien (Trust)', `${r.trustScore || 70} / 100`);
                    flowText(card, 'Tarif management fee', `${(Number(r.managementFeeRate || r.managementFeeAnnualRate || 0.015) * 100).toFixed(1)}% p.a.`);
                    flowText(card, 'Tarif performance fee', `${(Number(r.performanceFeeRate || 0.20) * 100).toFixed(0)}% di atas HWM`);
                    panel.append(card);
                });
            }
        } catch (e) { flowText(panel, 'Gagal memuat laporan klien', e.message); }
    }

    function openStressTestModal() {
        const panel = showFlow('Uji stres portofolio makro (S37)');
        flowText(panel, 'Metodologi', 'Simulasi skenario goncangan pasar ekstrem terhadap portofolio (FR-17.01 & FR-17.02). Memproyeksikan potensi penurunan nilai, risiko margin call, dan kecukupan modal.');

        if (!bridge.getStressTestScenarios || !bridge.runStressTest) {
            flowText(panel, 'Uji stres', 'Bridge uji stres belum tersedia.'); return;
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
        const panel = showFlow('Lab strategi & sandbox what-if (S38)');
        flowText(panel, 'Isolasi terjamin', 'Eksperimen strategi trading di lingkungan terisolasi penuh (FR-17.03 & FR-17.04). Segala transaksi beli/jual di lab menggunakan kloning akun dan TIDAK AKAN mengubah saldo kas atau posisi akun riil Anda.');

        if (!bridge.createStrategyLab || !bridge.simulateLabTrade) {
            flowText(panel, 'Lab strategi', 'Bridge lab strategi belum tersedia.'); return;
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
        const panel = showFlow('Replay historis (S39)');
        flowText(panel, 'Status dataset resmi', 'Fitur Replay Historis memerlukan dataset pasar historis berlisensi (OQ02 s/d OQ05). Saat ini manifest dataset berstatus UNAVAILABLE secara jujur untuk mematuhi regulasi integritas data.');
        const card = document.createElement('section');
        card.className = 'card';
        const h = document.createElement('h2');
        h.className = 'card-title';
        h.textContent = 'Informasi lisensi & integritas';
        card.append(h);
        flowText(card, 'Kepatuhan audit', 'Aplikasi tidak menampilkan data historis fiktif tanpa bukti hak distribusi.');
        card.style.borderColor = 'var(--warning, #f59e0b)';
        panel.append(card);
    }

    function openRiskModal() {
        const panel = showFlow('Fasilitas margin & risiko');
        if (!bridge.getRiskSnapshot) {
            flowText(panel, 'Risiko', 'Bridge risiko domain belum tersedia.'); return;
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
            flowText(panel, 'Batas fasilitas', 'Fasilitas margin pinjaman simulasi internal game (FR-15). Maintenance margin 30%, liquidation 20%.');

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
            logTitle.textContent = 'Jejak audit kepatuhan & risiko (FR-15.10)';
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
        flowText(panel, 'Parameter sementara', 'Spread dan periode quote mengikuti implementasi existing; belum final OQ-07. Seluruhnya uang game.');
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
        const panel = showFlow('Tinjau konversi FX');
        flowText(panel, 'Akun', `${preview.accountName} (${preview.accountId})`);
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
                const receipt = showFlow('Hasil konversi FX');
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
        document.querySelectorAll('.side-btn').forEach(button => { button.classList.remove('active'); button.style.background = ''; button.style.color = ''; });
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
            const controls = Array.from(orderModal.querySelectorAll('button, input')).filter(node => !node.disabled);
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
        estTotalDisplay.textContent = 'Biaya, estimasi fill dan dampak kas dihitung domain pada langkah review.';
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
            document.querySelectorAll('.side-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            btn.style.background = btn.dataset.side === 'BUY' ? 'var(--positive)' : 'var(--negative)';
            btn.style.color = '#0F1720';
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
        flowText(panel, 'Batas informasi', 'Atribusi berita, kontribusi portofolio dan kalender belum terhubung. Tidak menyimpulkan penyebab pergerakan pasar.');
        flowText(panel, 'Aturan waktu', 'Kontrol memakai clock game existing 1×/2×/5×; kebijakan ekonomi dan background masih sementara (OQ-06).');
        if (bridge.setClockState && clock) {
            const speed = document.createElement('select'); speed.className = 'input-field';
            speed.id = 'briefing-speed';
            const label = document.createElement('label'); label.htmlFor = speed.id; label.textContent = 'Kecepatan waktu';
            [1, 2, 5].forEach(value => { const option = document.createElement('option'); option.value = value; option.textContent = `${value}×`; speed.append(option); });
            speed.value = clock.speed; panel.append(label, speed);
            const updateClock = playing => {
                try {
                    const result = JSON.parse(bridge.setClockState(playing, Number(speed.value)));
                    if (!result.clock) throw new Error(result.message || 'Clock belum terkonfirmasi');
                    refreshSnapshot();
                    flowText(panel, 'Waktu terkonfirmasi', `${result.clock.isPaused ? 'Dijeda' : 'Berjalan'} ${result.clock.speed}×`);
                } catch (error) { flowText(panel, 'Kontrol waktu gagal', error.message); }
            };
            flowButton(panel, 'Mulai / lanjutkan waktu', () => updateClock(true));
            flowButton(panel, 'Jeda waktu', () => updateClock(false));
        } else flowText(panel, 'Kontrol', 'Bridge kontrol domain belum tersedia pada pratinjau ini.');
        if (bridge.closeDay) {
            flowButton(panel, 'Tutup Hari Bursa & Evaluasi Sesi (S08)', () => {
                const reqId = 'REQ-DAY-CLOSE-' + Date.now();
                try {
                    const res = JSON.parse(bridge.closeDay(reqId));
                    if (!res.success) throw new Error(res.message || 'Penutupan hari gagal');
                    flowText(panel, 'Rekapitulasi Sesi Berhasil (S08)',
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
        flowText(panel, 'Pangkat / reputasi', `${career.title} / ${career.reputation}`);
        flowText(panel, 'Hari', career.days);
        flowText(panel, 'Parameter sementara', 'Reward dan ambang karier berasal implementasi existing, belum keputusan final OQ-10. Membuka layar tidak menyelesaikan misi.');
        career.missions.forEach(mission => {
            const section = document.createElement('article'); section.className = 'card'; section.dataset.missionId = mission.id;
            const title = document.createElement('h2'); title.textContent = mission.title; section.append(title);
            flowText(section, 'Tujuan', mission.description);
            flowText(section, 'Status', mission.claimed ? 'Sudah diklaim' : mission.completed ? 'Selesai; dapat ditinjau untuk klaim' : 'Belum selesai');
            flowText(section, 'Reward sementara', mission.reward);
            if (mission.completed && !mission.claimed && bridge.claimMission) {
                flowButton(section, `Tinjau klaim ${mission.id}`, () => {
                    const review = showFlow('Tinjau klaim misi');
                    flowText(review, 'Akun', currentAccount.name);
                    flowText(review, 'Misi', `${mission.id} — ${mission.title}`);
                    flowText(review, 'Imbalan sementara', mission.reward);
                    flowText(review, 'Akibat', 'Kredit uang game sekali dan perubahan reputasi menurut aturan existing, belum final OQ-10.');
                    flowButton(review, 'Klaim imbalan', event => {
                        event.currentTarget.disabled = true;
                        try {
                            const result = JSON.parse(bridge.claimMission(mission.id));
                            const receipt = showFlow('Hasil klaim misi');
                            flowText(receipt, 'Status', result.success ? result.status : 'DITOLAK');
                            flowText(receipt, 'Akun', `${currentAccount.name} (${currentAccount.id})`);
                            flowText(receipt, 'Misi', mission.id);
                            flowText(receipt, 'Imbalan sementara (OQ-10)', result.reward);
                            flowText(receipt, 'Pesan', result.message || 'Tercatat pada ledger akun pribadi');
                            refreshSnapshot();
                        } catch (error) { flowText(review, 'Klaim belum terkonfirmasi', 'Periksa status misi sebelum mencoba kembali.'); }
                    });
                });
            }
            panel.append(section);
        });
        flowText(panel, 'Batas', 'Pemenuhan otomatis misi, syarat promosi lengkap, klaim dan recovery karier masih dalam remediasi.');
    });

    document.getElementById('btn-reports').addEventListener('click', () => {
        refreshSnapshot();
        if (latestSnapshot && latestSnapshot.recoveryRequired) return;
        const panel = showFlow('Laporan tersimpan');
        if (!bridge.getSessionReports) { flowText(panel, 'Laporan', 'Bridge laporan domain belum tersedia.'); return; }
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
        const panel = showFlow('Mulai & kelola permainan (S01)');
        flowText(panel, 'Informasi', 'Kelola sesi permainan trading saham. Anda dapat melanjutkan checkpoint aktif, memulai karir baru, atau mempelajari tutorial interaktif.');

        const slotSection = document.createElement('section');
        slotSection.className = 'card';
        const slotTitle = document.createElement('h2');
        slotTitle.className = 'card-title';
        slotTitle.textContent = 'Slot simpanan aktif';
        slotSection.append(slotTitle);

        refreshSnapshot();
        if (latestSnapshot && latestSnapshot.accounts && latestSnapshot.accounts.length) {
            const acc = latestSnapshot.accounts[0];
            flowText(slotSection, 'Akun aktif', `${acc.name} (${acc.id})`);
            flowText(slotSection, 'Mode simulasi', latestSnapshot.gameMode || 'Pasar Internal Game');
            flowText(slotSection, 'Kas tersedia IDR', acc.cashIdr);
            flowText(slotSection, 'Kas tersedia USD', acc.cashUsd);
            flowText(slotSection, 'Total ekuitas/portofolio', acc.totalPortfolioIdr);
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
        newGameTitle.textContent = 'Mulai permainan baru (S02)';
        newGameSection.append(newGameTitle);
        flowText(newGameSection, 'Profil baru', 'Konfigurasikan modal awal dan mode pasar untuk memulai siklus karir dari nol.');
        flowButton(newGameSection, 'Konfigurasi permainan baru (S02)', () => {
            openNewGameModal();
        });
        panel.append(newGameSection);

        const tutSection = document.createElement('section');
        tutSection.className = 'card';
        const tutTitle = document.createElement('h2');
        tutTitle.className = 'card-title';
        tutTitle.textContent = 'Panduan & tutorial interaktif (S03)';
        tutSection.append(tutTitle);
        flowText(tutSection, 'Tutorial', 'Pelajari alur pasar, pembacaan order book, tiket beli/jual, mitigasi risiko, dan evaluasi harian.');
        flowButton(tutSection, 'Buka panduan tutorial trading (S03)', () => {
            openTutorialModal();
        });
        panel.append(tutSection);
    }

    function openNewGameModal() {
        const panel = showFlow('Mulai permainan baru (S02)');
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
        const panel = showFlow('Panduan tutorial interaktif (S03)');
        flowText(panel, 'Pengantar', 'Alur 5 tahap trading saham komprehensif (FR-01 s/d FR-08). Ikuti setiap panduan untuk mencapai profitabilitas konsisten.');

        const steps = [
            {
                step: '1. Siklus Sesi & Briefing Pasar',
                desc: 'Setiap hari bursa dimulai dengan Market Briefing pada pukul 08:30 WIB. Periksa sentimen berita makro dan emiten sebelum bel pembukaan pasar 09:00 WIB (FR-01).',
                action: 'Buka Briefing Pasar (S04)',
                handler: () => {
                    closeFlow();
                    document.getElementById('btn-briefing').click();
                }
            },
            {
                step: '2. Pembacaan Order Book Bid & Ask',
                desc: 'Order Book menampilkan antrean beli (Bid) dan antrean jual (Ask) terbaik. Kuotasi bergerak dinamis berdasarkan permintaan pasar dan kuotasi Market Maker (FR-02 & FR-04).',
                action: 'Buka Menu Pasar (S05 & S06)',
                handler: () => {
                    closeFlow();
                    switchTab('pasar');
                }
            },
            {
                step: '3. Tiket Order & Eksekusi',
                desc: 'Pilih order LIMIT untuk mengantre di harga tertentu, atau MARKET untuk eksekusi instan di harga pasar terbaik. Kas tertahan secara otomatis beserta estimasi komisi (FR-03 & FR-05).',
                action: 'Buka Tiket Order Saham (S07)',
                handler: () => {
                    closeFlow();
                    switchTab('pasar');
                    try {
                        const stocks = JSON.parse(bridge.getInstruments('ALL', true));
                        if (stocks && stocks.length > 0) openOrderTicket(stocks[0]);
                    } catch (e) {}
                }
            },
            {
                step: '4. Portofolio, Risiko & Margin',
                desc: 'Pantau posisi terbuka, keuntungan/kerugian mengambang, serta fasilitas pinjaman margin multi-mata uang IDR dan USD dengan batas rasio kesehatan ekuitas (FR-06 & FR-11).',
                action: 'Periksa Portofolio & Risiko (S10 & S11)',
                handler: () => {
                    closeFlow();
                    switchTab('aset');
                }
            },
            {
                step: '5. Penutupan Hari & Rekapitulasi Karir',
                desc: 'Pasar reguler ditutup pukul 16:00 WIB. Lakukan tutup hari untuk menerima gaji pokok harian, memverifikasi laba/rugi terealisasi, dan membuka tingkatan karir baru (FR-08).',
                action: 'Buka Evaluasi Karir & Misi (S09 & S15)',
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

    document.getElementById('btn-settings').addEventListener('click', () => {
        const panel = showFlow('Pengaturan / save');
        flowText(panel, 'Batas kandidat', 'Save lokal masih dalam remediasi. Histori nyata belum tersedia; jangan gunakan sebagai update progres APK lama.');
        flowButton(panel, 'Menu awal & mulai baru (S01 & S02)', openEntryPicker);
        flowButton(panel, 'Panduan tutorial interaktif (S03)', openTutorialModal);
        flowButton(panel, 'Simpan permainan', () => {
            try {
                const result = JSON.parse(bridge.saveGame());
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

    // Initial Load
    refreshSnapshot();

    // Loop tick otomatis (setiap 3 detik untuk simulasi pasar)
    setInterval(() => {
        if (bridge && bridge.tickClock) {
            bridge.tickClock(5); // majukan 5 detik game
            if (currentTab === 'beranda') refreshSnapshot();
        }
    }, 3000);

})();
