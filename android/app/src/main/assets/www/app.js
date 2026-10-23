/**
 * Game Saham 2D Portrait - Frontend Client Controller
 * Berjalan di Android System WebView dengan TradingNativeBridge
 * atau host QA Java dengan penyimpanan terisolasi.
 */

(function () {
    'use strict';

    // 1. Inisialisasi & Deteksi Lingkungan Bridge
    const isNativeHost = typeof window.TradingNativeBridge !== 'undefined';
    if (!isNativeHost) {
        document.querySelector('.bottom-nav').hidden = true;
        document.querySelector('.context-strip').hidden = true;
        document.querySelector('.app-header').hidden = true;
        const main = document.getElementById('main-content');
        main.replaceChildren();
        const state = document.createElement('section');
        state.className = 'state-panel';
        const title = document.createElement('h1'); title.textContent = 'Permainan belum terhubung';
        const detail = document.createElement('p'); detail.textContent = 'Buka aplikasi Android atau host QA Java untuk memuat progres. Halaman ini tidak membuat saldo dan harga contoh.';
        const retry = document.createElement('button'); retry.className = 'btn-primary'; retry.textContent = 'Coba hubungkan kembali'; retry.onclick = () => location.reload();
        state.append(title, detail, retry); main.append(state);
        return;
    }
    let currentTab = 'beranda';
    let currentAccount = null;
    let selectedInstrument = null;
    let latestSnapshot = null;
    const scrollPositions = {};
    let flowOpener = null;
    function formatMoney(value, currency = 'IDR', fractionDigits = currency === 'USD' ? 2 : 0) {
        if (value == null || value === '') return 'Belum tersedia';
        const amount=Number(value);
        return Number.isFinite(amount) ? (currency === 'IDR' ? 'Rp' : currency === 'USD' ? 'US$' : currency) + ' ' + amount.toLocaleString('id-ID',{minimumFractionDigits:fractionDigits, maximumFractionDigits:fractionDigits}) : 'Belum tersedia';
    }

    const bridge = window.TradingNativeBridge;

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
    let scopeLabel = 'Bridge tersedia · identitas host belum terverifikasi';
    try {
        const platform = bridge.getPlatformInfo && JSON.parse(bridge.getPlatformInfo());
        if (platform && platform.nativeHost === true) scopeLabel = 'Android · pasar game';
        else if (platform) scopeLabel = platform.label || 'Host pengujian · bukan Android';
    } catch (error) { console.warn('Identitas host belum dapat dibaca'); }
    mockBanner.hidden = !scopeLabel.includes('QA') && !scopeLabel.includes('FIXTURE');
    mockBanner.textContent = scopeLabel.replaceAll('\u2014', '·');

    // 3. Tab Switching
    document.querySelectorAll('.nav-item').forEach(btn => {
        btn.addEventListener('click', () => {
            const tab = btn.dataset.tab;
            switchTab(tab);
        });
    });

    function switchTab(tab) {
        const alias = {ringkasan: 'beranda', portofolio: 'aset', bisnis: 'firma', karier: 'firma', lainnya: 'riset'};
        if (alias[tab]) tab = alias[tab];
        if (!views[tab]) return;
        if (flowPanel) closeFlow();
        const main = document.querySelector('.app-scroll');
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

    // 4. Data Refreshers & Realtime Sync System
    let preferenceRunId = null;
    let lastKnownLevel = null;
    let lastKnownAchievements = null;
    let activeDetailStock = null;
    let anchorGameTimestamp = 0;
    let anchorPerfNow = performance.now();
    let anchorRate = 96;
    let latestDailySponsor = null;

    function renderFlowingClock() {
        if (!anchorGameTimestamp) return;
        const elapsedRealMs = performance.now() - anchorPerfNow;
        const currentSimMs = anchorGameTimestamp + elapsedRealMs * anchorRate;
        // Waktu game dihitung dalam offset WIB (UTC+7)
        const date = new Date(currentSimMs + 7 * 3600000);
        const hours = String(date.getUTCHours()).padStart(2, '0');
        const mins = String(date.getUTCMinutes()).padStart(2, '0');
        const secs = String(date.getUTCSeconds()).padStart(2, '0');
        if (clockDisplay) {
            clockDisplay.textContent = `${hours}:${mins}:${secs} WIB`;
        }
    }

    function updateSponsorCountdown() {
        const claimSponsorBtn = document.getElementById('btn-claim-sponsor');
        const qaSponsor = document.getElementById('btn-qa-sponsor');
        if (!latestDailySponsor) return;
        if (latestDailySponsor.alreadyClaimed || !latestDailySponsor.eligible) {
            const nextMs = latestDailySponsor.nextClaimAt;
            if (!Number.isFinite(nextMs)) {
                if (claimSponsorBtn) { claimSponsorBtn.disabled = true; claimSponsorBtn.textContent = 'Waktu klaim belum tersedia'; }
                return;
            }
            const remaining = Math.max(0, nextMs - Date.now());
            if (remaining === 0) {
                refreshSnapshot();
                return;
            }
            const hours = Math.floor(remaining / 3600000);
            const mins = Math.floor((remaining % 3600000) / 60000);
            const secs = Math.floor((remaining % 60000) / 1000);
            const pad = n => String(n).padStart(2, '0');
            const countdownStr = `${pad(hours)}:${pad(mins)}:${pad(secs)}`;
            if (claimSponsorBtn) {
                claimSponsorBtn.disabled = true;
                claimSponsorBtn.textContent = `Klaim berikutnya ${countdownStr}`;
            }
            if (qaSponsor) {
                qaSponsor.disabled = true;
                qaSponsor.setAttribute('aria-label', `Sudah diklaim hari ini. Klaim berikutnya ${countdownStr}`);
            }
        } else {
            if (claimSponsorBtn) {
                claimSponsorBtn.disabled = false;
                claimSponsorBtn.textContent = `Klaim hibah ${latestDailySponsor.grantAmountFormatted || ''}`.trim();
            }
            if (qaSponsor) {
                qaSponsor.disabled = false;
                qaSponsor.setAttribute('aria-label', `Klaim Hibah Sponsor ${latestDailySponsor.grantAmountFormatted || ''}`.trim());
            }
        }
    }
    let activeReceiptRequestId = null;
    let activeDetailPositionInstrumentId = null;
    const activeToastKeys = new Set();

    function showGameToast(type, title, message, dedupeKey) {
        const key = dedupeKey || (title + ':' + message);
        if (activeToastKeys.has(key)) return;
        activeToastKeys.add(key);
        setTimeout(() => { activeToastKeys.delete(key); }, 5000);

        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `game-toast ${type || 'info'}`;
        const icon = node('div', 'toast-icon', type === 'success' || type === 'achievement' ? '✓' : 'i');
        icon.setAttribute('aria-hidden', 'true');
        const body = node('div', 'toast-body'); body.append(node('strong', '', title), node('small', '', message));
        const dismiss = node('button', 'toast-dismiss', '×'); dismiss.setAttribute('aria-label', 'Tutup pemberitahuan'); dismiss.onclick = () => toast.remove();
        toast.append(icon, body, dismiss);
        container.replaceChildren(toast);
        requestAnimationFrame(() => toast.classList.add('visible'));
        setTimeout(() => {
            toast.classList.remove('visible');
            setTimeout(() => toast.remove(), 250);
        }, 3200);
    }

    function applyPlayerPreferences(preferences) {
        document.documentElement.style.fontSize = preferences.textSize === '200%' ? '200%' : '';
        document.documentElement.classList.toggle('high-contrast', preferences.highContrast === true);
        document.documentElement.classList.toggle('reduced-motion', preferences.reducedMotion === true);
    }
    function persistPlayerPreferences(patch) {
        const profile=bridgeResult('getPlayerProfile');
        const preferences=Object.assign({},profile.preferences || {},patch);
        const result=bridgeResult('updatePlayerProfile',JSON.stringify({playerName:profile.playerName,preferences}));
        applyPlayerPreferences(result.preferences || preferences);
        return result;
    }
    function refreshSnapshot() {
        try {
            const raw = bridge.getSnapshot();
            const snap = JSON.parse(raw);
            if (snap.success === false || snap.error) throw new Error(snap.message || snap.error || 'Progres tidak dapat dimuat');
            document.getElementById('snapshot-status')?.remove();
            latestSnapshot = snap;
            if (snap.runId && preferenceRunId !== snap.runId && bridge.getPlayerProfile) {
                const profile=JSON.parse(bridge.getPlayerProfile());
                if (profile.success !== false && profile.preferences) { applyPlayerPreferences(profile.preferences); preferenceRunId=snap.runId; }
            }
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
                anchorGameTimestamp = snap.clock.timestamp || anchorGameTimestamp;
                anchorPerfNow = performance.now();
                anchorRate = snap.clock.isPaused ? 0 : (Number.isFinite(Number(snap.clock.rate)) ? Number(snap.clock.rate) * (Number(snap.clock.speed) || 1) : 0);
                if (clockDisplay) clockDisplay.textContent = snap.clock.time;
                sessionLabel.textContent = snap.clock.sessionLabel;
                sessionDot.className = 'status-dot ' + (snap.clock.isTradingOpen ? 'open' : 'halt');
                document.getElementById('home-clock-state').textContent = `${snap.clock.sessionLabel} · ${snap.clock.isPaused ? 'Dijeda' : 'Berjalan otomatis'}`;
                renderFlowingClock();
            }
            if (snap.dailySponsor) {
                latestDailySponsor = snap.dailySponsor;
                updateSponsorCountdown();
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
                    wealthEl.textContent = idrCash.available;
                }

                // Update Liabilities / Utang
                const liabEl = document.getElementById('home-liabilities');
                if (liabEl && bridge.getLoans) {
                    try {
                        const loansObj = JSON.parse(bridge.getLoans());
                        liabEl.textContent = loansObj.totalPersonalDebt || 'Belum tersedia';
                        liabEl.style.color = (loansObj.totalPersonalDebt && loansObj.totalPersonalDebt !== 'Rp 0') ? 'var(--negative)' : 'var(--text-muted)';
                    } catch(e) {}
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
                document.getElementById('home-achievement-count').textContent = `${snap.playerProgress.achievementsUnlocked}/${snap.playerProgress.totalAchievements} pencapaian diraih`;

                // Notifikasi perayaan progres jika naik level / unlock badge
                if (lastKnownLevel !== null && snap.playerProgress.level > lastKnownLevel) {
                    showGameToast('achievement', 'Level Naik!', `Selamat! Kamu telah mencapai Level ${snap.playerProgress.level}`);
                }
                lastKnownLevel = snap.playerProgress.level;

                if (lastKnownAchievements !== null && snap.playerProgress.achievementsUnlocked > lastKnownAchievements) {
                    showGameToast('achievement', 'Pencapaian Terbuka!', `Badge baru diraih! (${snap.playerProgress.achievementsUnlocked}/${snap.playerProgress.totalAchievements})`);
                }
                lastKnownAchievements = snap.playerProgress.achievementsUnlocked;
            }

            if (snap.career) {
                document.getElementById('career-agenda-title').textContent = snap.career.title;
                document.getElementById('home-mission-progress').textContent = `${snap.career.missions.filter(m => m.completed).length}/${snap.career.missions.length} misi selesai`;
            }
            if (snap.clock) document.getElementById('home-date').textContent = `${snap.career ? 'Hari ' + snap.career.days + ' · ' : ''}${snap.clock.date}`;
            const accNameEl = document.getElementById('context-account-name');
            if (accNameEl) {
                accNameEl.textContent = currentAccount ? (currentAccount.type === 'CLIENT' ? 'Klien: ' + currentAccount.name : (currentAccount.name || 'Pribadi')) : 'Pribadi';
            }

            // Live Sync ke sub-layar yang sedang aktif
            if (currentTab === 'aset') syncPortfolioLive();
            if (currentTab === 'firma') syncBusinessLive();
            if (activeReceiptRequestId) updateReceiptLive(activeReceiptRequestId);
            if (activeDetailStock) updateInstrumentDetailLive(activeDetailStock.id);

            if (bridge.getInvestorArchetype) {
                try {
                    const arch = JSON.parse(bridge.getInvestorArchetype());
                    const archEl = document.getElementById('home-archetype-name');
                    if (archEl && arch && arch.label) archEl.textContent = arch.label;
                } catch(e) {}
            }
            if (bridge.getCollections) {
                try {
                    const colRes = JSON.parse(bridge.getCollections());
                    const list = Array.isArray(colRes) ? colRes : (colRes.collections || []);
                    const owned = list.filter(c => c.isOwned).length;
                    const colEl = document.getElementById('home-collection-count');
                    if (colEl) colEl.textContent = `${owned}/${list.length} koleksi dimiliki`;
                } catch(e) {}
            }
            renderHomeNews();
        } catch (e) {
            let status = document.getElementById('snapshot-status');
            if (!status) {
                status = node('section', 'state-panel'); status.id = 'snapshot-status'; status.setAttribute('role', 'alert');
                document.querySelector('main').prepend(status);
            }
            status.replaceChildren(node('h2', '', 'Progres belum dapat diperbarui'), node('p', '', (latestSnapshot ? 'Data terakhir tetap ditampilkan. ' : '') + e.message));
            flowButton(status, 'Coba muat progres kembali', refreshSnapshot);
        }
    }

    let homeNewsFilter = 'all';

    function renderHomeNews() {
        const container = document.getElementById('home-news-list');
        if (!container) return;
        if (!bridge.getResearchFeed) {
            container.innerHTML = '<p style="font-size:0.85rem; color:var(--text-muted); margin:0;">Layanan kabar pasar belum terhubung.</p>';
            return;
        }
        try {
            const feed = JSON.parse(bridge.getResearchFeed());
            const allNews = feed.news || [];
            if (!allNews.length) {
                container.innerHTML = '<p style="font-size:0.85rem; color:var(--text-muted); margin:0;">Belum ada kabar pasar yang terbit saat ini.</p>';
                return;
            }

            let filtered = allNews;
            if (homeNewsFilter === 'good') {
                filtered = allNews.filter(n => Number(n.sentimentImpact) > 0);
            } else if (homeNewsFilter === 'bad') {
                filtered = allNews.filter(n => Number(n.sentimentImpact) < 0);
            }

            if (!filtered.length) {
                const label = homeNewsFilter === 'good' ? 'kabar baik' : 'kabar buruk';
                container.innerHTML = `<p style="font-size:0.85rem; color:var(--text-muted); margin:0;">Belum ada ${label} yang terbit hari ini.</p>`;
                return;
            }

            container.innerHTML = '';
            filtered.forEach(n => {
                const card = document.createElement('article');
                card.className = 'card news-card';
                card.tabIndex = 0; card.setAttribute('role', 'button'); card.setAttribute('aria-label', `Baca ${n.headline}`);

                const isGood = n.direction === 'POSITIF' || Number(n.sentimentImpact) > 0;
                const isBad = n.direction === 'NEGATIF' || Number(n.sentimentImpact) < 0;

                const top = node('div', 'section-heading');
                top.append(node('span', 'eyebrow', `${n.categoryLabel || 'Pasar'}${n.affectedSector ? ' · ' + n.affectedSector : ''}${n.relatedTicker ? ' (' + n.relatedTicker + ')' : ''}`));
                top.append(node('span', `change-pill ${isGood ? 'positive' : isBad ? 'negative' : ''}`, `${n.direction || 'Dampak belum tersedia'}${n.estimatedRange ? ' (' + n.estimatedRange + ')' : ''}`));
                const timing = node('div', 'flex-row');
                timing.append(node('span', '', n.countdownLabel || (n.publishedAt == null ? 'Waktu belum tersedia' : new Date(n.publishedAt).toLocaleString('id-ID', {timeZone: 'Asia/Jakarta'}))), node('span', '', 'Baca rincian'));
                if (n.confidence) timing.append(node('span', '', `Keyakinan: ${n.confidence}`));
                card.append(top, node('h3', '', n.headline), node('p', '', n.content ? n.content.substring(0, 110) + (n.content.length > 110 ? '…' : '') : 'Isi berita belum tersedia.'), timing);
                card.onclick = () => {
                    const detail = showFlow('Kabar & Dampak Pasar');
                    detail.append(node('h2', '', n.headline));
                    flowText(detail, 'Kategori & Sektor', `${n.categoryLabel || 'Pasar'} · ${n.affectedSector || 'Umum'}`);
                    if (n.relatedTicker) flowText(detail, 'Emiten terkait', n.relatedTicker);
                    flowText(detail, 'Arah & Estimasi Dampak', `${n.direction || 'NETRAL'}${n.estimatedRange ? ' (Rentang perkiraan: ' + n.estimatedRange + ')' : ''}`);
                    flowText(detail, 'Tingkat Keyakinan (Confidence)', n.confidence);
                    flowText(detail, 'Horizon Waktu', n.horizonDays == null ? null : `${n.horizonDays} hari permainan`);
                    flowText(detail, 'Status Waktu Game', n.countdownLabel || 'Terjadwal');
                    detail.append(node('p', 'article-copy', n.content));
                    if (bridge.recordResearchRead) bridge.recordResearchRead(n.id);
                    refreshSnapshot();
                };
                card.onkeydown = event => { if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); card.click(); } };
                container.appendChild(card);
            });
        } catch(e) {
            container.replaceChildren(node('p', 'notice', 'Gagal memuat kabar pasar: ' + e.message));
        }
    }

    function openPositionDetailModal(instrumentId) {
        if (!currentAccount || !bridge.getPortfolio) return;
        try {
            const portfolio = JSON.parse(bridge.getPortfolio(currentAccount.id));
            const position = (portfolio.positions || []).find(p => p.instrumentId === instrumentId);
            if (!position) return;
            activeDetailPositionInstrumentId = instrumentId;
            const isClosed = position.quantity === 0;
            const panel = showFlow('Detail posisi');
            panel.setAttribute('data-position-instrument-id', instrumentId);
            panel.closest('.flow-page')?.setAttribute('data-position-instrument-id', instrumentId);
            flowText(panel, 'Akun', portfolio.accountName);
            metricCard(panel, position.instrumentId, isClosed ? `${position.marketValue} (Posisi ditutup)` : position.marketValue, isClosed ? 'Posisi telah ditutup · Nilai pasar nol' : 'Nilai posisi saat snapshot');
            const labels = {
                instrumentId: 'Instrumen',
                quantity: 'Jumlah saham',
                currency: 'Mata uang',
                averageCost: 'Biaya rata-rata / saham',
                currentPrice: 'Harga / saham',
                unrealizedPnL: 'Laba/rugi belum terealisasi',
                realizedPnLGross: 'Hasil terealisasi sebelum biaya'
            };
            Object.entries(labels).forEach(([key, label]) => {
                let val = String(position[key] ?? '-');
                if (key === 'quantity') val = `${position.quantity} saham`;
                flowText(panel, label, val);
            });
            flowText(panel, 'Catatan', isClosed
                ? 'Posisi telah ditutup sepenuhnya. Nilai laba/rugi terealisasi di atas belum dikurangi biaya transaksi.'
                : 'Nilai laba/rugi terealisasi di atas belum dikurangi biaya transaksi.');
        } catch(e) {
            console.warn('Gagal membuka detail posisi:', e);
        }
    }

    function syncPortfolioLive() {
        if (currentTab !== 'aset') return;
        const posContainer = document.getElementById('portfolio-positions-list');
        const summary = document.getElementById('asset-summary');
        if (!posContainer || !summary || !currentAccount || !bridge.getPortfolio) return;
        try {
            const portfolio = JSON.parse(bridge.getPortfolio(currentAccount.id));
            if (!portfolio || !portfolio.success) return;

            // Update Net Worth di summary
            const nwEl = summary.querySelector('.metric-hero');
            if (nwEl && portfolio.netWorth) {
                nwEl.textContent = portfolio.netWorth;
            }

            // Update Allocation Bar
            const barFill = summary.querySelector('.allocation-bar span');
            if (barFill && portfolio.positionsSharePercent != null) {
                barFill.style.width = `${Math.max(0, Math.min(100, Number(portfolio.positionsSharePercent)))}%`;
            }

            // Update Cash Values di Dompet (IDR dan USD)
            if (portfolio.cash) {
                const rows = summary.querySelectorAll('.data-row');
                rows.forEach(r => {
                    const span = r.querySelector('span');
                    const strong = r.querySelector('strong');
                    if (!span || !strong) return;
                    if (portfolio.cash.IDR) {
                        if (span.textContent === 'Kas (valuasi IDR)') strong.textContent = portfolio.cashValueIdr;
                        if (span.textContent === 'IDR total') strong.textContent = portfolio.cash.IDR.total;
                        if (span.textContent === 'IDR tersedia') strong.textContent = portfolio.cash.IDR.available;
                        if (span.textContent === 'IDR tertahan') strong.textContent = portfolio.cash.IDR.reserved;
                    }
                    if (portfolio.cash.USD) {
                        if (span.textContent === 'USD total') strong.textContent = portfolio.cash.USD.total;
                        if (span.textContent === 'USD tersedia') strong.textContent = portfolio.cash.USD.available;
                        if (span.textContent === 'USD tertahan') strong.textContent = portfolio.cash.USD.reserved;
                    }
                    if (span.textContent === 'Posisi saham (valuasi IDR)') strong.textContent = portfolio.positionsValueIdr;
                    if (span.textContent === 'Kewajiban') strong.textContent = portfolio.liabilities;
                    if (span.textContent === 'Kurs valuasi USD/IDR' && portfolio.valuationRateUsdIdr) strong.textContent = portfolio.valuationRateUsdIdr;
                });
            }

            // Reconcile Positions List in place
            if (portfolio.positions) {
                const currentRows = posContainer.querySelectorAll('.position-row');
                if (portfolio.positions.length === 0) {
                    if (currentRows.length > 0 || !posContainer.querySelector('.empty-state')) {
                        posContainer.replaceChildren();
                        emptyState(posContainer, 'Belum ada posisi', 'Pilih saham di Pasar dan tinjau order pertamamu.');
                    }
                } else {
                    const empty = posContainer.querySelector('.empty-state');
                    if (empty) empty.remove();
                    if (currentRows.length === portfolio.positions.length) {
                        portfolio.positions.forEach((pos, idx) => {
                            const btn = currentRows[idx];
                            const isPosClosed = pos.quantity === 0;
                            const text = `${pos.instrumentId}: ${pos.quantity} saham ${isPosClosed ? '(Ditutup)' : ''} · ${pos.marketValue}`;
                            btn.dataset.instrumentId = pos.instrumentId;
                            btn.onclick = () => openPositionDetailModal(pos.instrumentId);
                            const arrow = btn.querySelector('span[aria-hidden="true"]');
                            btn.textContent = text;
                            if (arrow) btn.append(arrow);
                        });
                    } else {
                        posContainer.replaceChildren();
                        portfolio.positions.forEach(position => {
                            const isPosClosed = position.quantity === 0;
                            const button = flowButton(posContainer, `${position.instrumentId}: ${position.quantity} saham ${isPosClosed ? '(Ditutup)' : ''} · ${position.marketValue}`, () => {
                                openPositionDetailModal(position.instrumentId);
                            });
                            button.className = 'action-link position-row';
                            button.dataset.instrumentId = position.instrumentId;
                        });
                    }
                }
            }

            // Update open Position Detail modal if currently visible
            if (activeDetailPositionInstrumentId && flowPanel && (flowPanel.getAttribute('data-position-instrument-id') === activeDetailPositionInstrumentId || flowPanel.closest('.flow-page')?.getAttribute('data-position-instrument-id') === activeDetailPositionInstrumentId)) {
                const curPos = (portfolio.positions || []).find(p => p.instrumentId === activeDetailPositionInstrumentId);
                const metricHero = flowPanel.querySelector('.metric-hero');
                const heroCaption = flowPanel.querySelector('.hero-caption');
                const rows = flowPanel.querySelectorAll('.data-row');
                const isUsd = activeDetailPositionInstrumentId.startsWith('US:');
                const zeroVal = isUsd ? '$0.00' : 'Rp 0';

                if (curPos) {
                    const isClosed = curPos.quantity === 0;
                    if (metricHero) {
                        metricHero.textContent = isClosed ? `${curPos.marketValue || zeroVal} (Posisi ditutup)` : curPos.marketValue;
                    }
                    if (heroCaption) {
                        heroCaption.textContent = isClosed ? 'Posisi telah ditutup · Nilai pasar nol' : 'Nilai posisi saat snapshot';
                    }
                    rows.forEach(r => {
                        const lbl = r.querySelector('span')?.textContent;
                        const str = r.querySelector('strong');
                        if (!lbl || !str) return;
                        if (lbl === 'Jumlah saham') str.textContent = `${curPos.quantity} saham`;
                        if (lbl === 'Mata uang') str.textContent = String(curPos.currency);
                        if (lbl === 'Harga / saham') str.textContent = String(curPos.currentPrice);
                        if (lbl === 'Biaya rata-rata / saham') str.textContent = String(curPos.averageCost);
                        if (lbl === 'Laba/rugi belum terealisasi') str.textContent = String(curPos.unrealizedPnL);
                        if (lbl === 'Hasil terealisasi sebelum biaya') str.textContent = String(curPos.realizedPnLGross ?? '-');
                        if (lbl === 'Catatan') {
                            str.textContent = isClosed 
                                ? 'Posisi telah ditutup sepenuhnya. Nilai laba/rugi terealisasi di atas belum dikurangi biaya transaksi.'
                                : 'Nilai laba/rugi terealisasi di atas belum dikurangi biaya transaksi.';
                        }
                    });
                } else {
                    if (metricHero) metricHero.textContent = `${zeroVal} (Posisi ditutup)`;
                    if (heroCaption) heroCaption.textContent = 'Posisi telah ditutup · Nilai pasar nol';
                    rows.forEach(r => {
                        const lbl = r.querySelector('span')?.textContent;
                        const str = r.querySelector('strong');
                        if (!lbl || !str) return;
                        if (lbl === 'Jumlah saham') str.textContent = '0 saham';
                        if (lbl === 'Laba/rugi belum terealisasi') str.textContent = zeroVal;
                        if (lbl === 'Catatan') str.textContent = 'Posisi telah ditutup sepenuhnya. Nilai laba/rugi terealisasi di atas belum dikurangi biaya transaksi.';
                    });
                }
            }
        } catch(e) {
            console.warn('Portfolio sync error:', e);
        }
    }

    function syncBusinessLive() {
        if (currentTab !== 'firma') return;
        if (!bridge.getCompanies) return;
        try {
            const compRes = JSON.parse(bridge.getCompanies());
            const comps = compRes.companies || [];
            comps.forEach(c => {
                const cCard = document.querySelector(`[data-company-id="${c.id}"]`);
                if (!cCard) return;
                const rows = cCard.querySelectorAll('.data-row');
                rows.forEach(r => {
                    const label = r.querySelector('span')?.textContent;
                    const strong = r.querySelector('strong');
                    if (!label || !strong) return;
                    if (label === 'Saldo kas perusahaan' || label === 'Kas operasional') strong.textContent = formatMoney(c.cash);
                    if (label === 'Perkiraan laba bersih') {
                        strong.textContent = c.dailyNetProfit == null ? 'Belum tersedia' : `${Number(c.dailyNetProfit) >= 0 ? '+' : ''}${formatMoney(c.dailyNetProfit)} / hari`;
                        strong.className = c.dailyNetProfit == null ? '' : Number(c.dailyNetProfit) >= 0 ? 'positive' : 'negative';
                    }
                    if (label === 'Staf & eksekutif') strong.textContent = `${c.employeeCount == null ? 'Jumlah pegawai belum tersedia' : c.employeeCount + ' pegawai'} · CEO: ${c.ceoName || 'Belum tersedia'}`;
                    if (label === 'Tingkat / level') strong.textContent = `Level ${c.level} · ${c.sectorModel}`;
                    if (label === 'Karyawan & CEO') strong.textContent = `${c.employeeCount == null ? 'Jumlah staf belum tersedia' : c.employeeCount + ' staf operasional'} · CEO: ${c.ceoName || 'Belum tersedia'}`;
                    if (label === 'Pendapatan / payroll harian') strong.textContent = `+Rp ${Number(c.dailyRevenue || 0).toLocaleString('id-ID')} / -Rp ${Number(c.dailyPayroll || 0).toLocaleString('id-ID')}`;
                    if (label === 'Kebijakan kerja & benefit') strong.textContent = `${c.workHours == null ? 'Jam kerja belum tersedia' : c.workHours + ' jam kerja'} · Fasilitas: ${c.benefitTier || 'Belum tersedia'}`;
                });
            });

            if (bridge.getCollections) {
                const colRes = JSON.parse(bridge.getCollections());
                const items = colRes.collections || colRes.items || [];
                items.forEach(item => {
                    const iCard = document.querySelector(`[data-collection-id="${item.id}"]`);
                    if (!iCard) return;
                    const rows = iCard.querySelectorAll('.data-row');
                    rows.forEach(r => {
                        const label = r.querySelector('span')?.textContent;
                        const strong = r.querySelector('strong');
                        if (!label || !strong) return;
                        if (label === 'Status kepemilikan') strong.textContent = item.isOwned ? 'Dimiliki pemain' : 'Tersedia di galeri';
                        if (label === 'Valuasi terkini') strong.textContent = formatMoney(item.currentValuation);
                        if (label === 'Biaya pemeliharaan') strong.textContent = item.dailyMaintenanceCost == null ? 'Belum tersedia' : `${formatMoney(item.dailyMaintenanceCost)} / hari`;
                    });
                });
            }
        } catch(e) {
            console.warn('Business sync error:', e);
        }
    }

    function formatOrderStatus(status) {
        const map = {
            'FILLED': 'Terisi penuh',
            'PARTIALLY_FILLED': 'Terisi sebagian',
            'OPEN': 'Mengantre',
            'CANCELLED': 'Dibatalkan',
            'REJECTED': 'Ditolak',
            'EXPIRED': 'Kedaluwarsa',
            'PENDING_TRIGGER': 'Menunggu pemicu'
        };
        return map[status] || status;
    }

    const lastRecordedReceiptFilled = new Map();
    function updateReceiptLive(reqId) {
        if (!flowPanel || !reqId) return;
        const receiptAttr = flowPanel.getAttribute('data-receipt-request-id');
        if (receiptAttr !== reqId) return;
        if (!bridge.getOrderStatus) return;
        try {
            const raw = bridge.getOrderStatus(reqId);
            const statusObj = JSON.parse(raw);
            if (!statusObj || !statusObj.success) return;

            const rows = flowPanel.querySelectorAll('.data-row');
            rows.forEach(r => {
                const label = r.querySelector('span')?.textContent;
                const strong = r.querySelector('strong');
                if (!label || !strong) return;
                if (label === 'Status') {
                    const statusLabel = formatOrderStatus(statusObj.status);
                    if (strong.textContent !== statusLabel) {
                        strong.textContent = statusLabel;
                        strong.setAttribute('data-status', statusObj.status);
                        if (statusObj.status === 'FILLED') {
                            showGameToast('success', 'Order Terisi Penuh!', `${statusObj.instrumentId}: ${statusObj.filledQuantity} saham telah terisi`, 'RECEIPT_FILL:' + reqId + ':' + statusObj.filledQuantity);
                            flowPanel.querySelectorAll('button').forEach(b => {
                                if (b.textContent.includes('Batalkan')) b.remove();
                            });
                        } else if (statusObj.status === 'CANCELLED') {
                            flowPanel.querySelectorAll('button').forEach(b => {
                                if (b.textContent.includes('Batalkan')) b.remove();
                            });
                        }
                    }
                }
                if (label === 'Terisi / sisa') {
                    const newText = `${statusObj.filledQuantity ?? '-'} / ${statusObj.remainingQuantity ?? '-'} saham`;
                    if (strong.textContent !== newText) {
                        strong.textContent = newText;
                        const prevFilled = lastRecordedReceiptFilled.get(reqId) || 0;
                        if (statusObj.filledQuantity > prevFilled && statusObj.status === 'PARTIALLY_FILLED') {
                            showGameToast('info', 'Order Terisi Sebagian', `${statusObj.instrumentId}: ${statusObj.filledQuantity} saham terisi (sisa ${statusObj.remainingQuantity})`, 'RECEIPT_PARTIAL:' + reqId + ':' + statusObj.filledQuantity);
                        }
                        lastRecordedReceiptFilled.set(reqId, statusObj.filledQuantity || 0);
                    }
                }
                if (label === 'Pesan' && statusObj.message) {
                    strong.textContent = statusObj.message;
                }
            });
        } catch(e) {
            console.warn('Receipt sync error:', e);
        }
    }

    function renderTape(container, trades) {
        if (trades && trades.length) {
            container.innerHTML = `
                <h3 style="margin: 12px 0 6px; font-size: 0.95rem;">Tape Transaksi Terakhir</h3>
                <div style="font-size:0.8rem; font-variant-numeric: tabular-nums;">
                    ${trades.map(t => `<div class="flex-row"><span>${new Date(t.timestamp).toLocaleTimeString('id-ID')}</span><span>${t.quantity} lembar</span><strong class="positive">${t.price}</strong></div>`).join('')}
                </div>
            `;
        } else {
            container.innerHTML = `
                <h3 style="margin: 12px 0 6px; font-size: 0.95rem;">Tape Transaksi Terakhir</h3>
                <p style="font-size:0.8rem; color:var(--text-muted); margin:4px 0;">Belum ada transaksi terkini di tape.</p>
            `;
        }
    }

    function updateInstrumentDetailLive(stockId) {
        if (!flowPanel || !stockId) return;
        const detailAttr = flowPanel.getAttribute('data-stock-id') || flowPanel.closest('.flow-page')?.getAttribute('data-stock-id');
        if (detailAttr !== stockId) return;
        try {
            const stocks = JSON.parse(bridge.getInstruments('', '', null, true));
            const fresh = stocks.find(s => s.id === stockId);
            if (!fresh) return;

            const metricHero = flowPanel.querySelector('.metric-hero');
            if (metricHero && metricHero.textContent !== fresh.currentPrice) {
                const oldVal = parseFloat(metricHero.textContent.replace(/[^0-9.-]+/g,"")) || 0;
                const newVal = parseFloat(fresh.currentPrice.replace(/[^0-9.-]+/g,"")) || 0;
                metricHero.textContent = fresh.currentPrice;
                metricHero.classList.remove('price-flash-up', 'price-flash-down');
                void metricHero.offsetWidth;
                metricHero.classList.add(newVal >= oldVal ? 'price-flash-up' : 'price-flash-down');
            }

            const pill = flowPanel.querySelector('.change-pill');
            if (pill) {
                const isPos = Number(fresh.dailyChangePercent) >= 0;
                pill.className = isPos ? 'change-pill positive' : 'change-pill negative';
                pill.textContent = `${isPos ? '+' : ''}${fresh.dailyChangePercent}% · perubahan game`;
            }

            const book = bridge.getOrderBook ? JSON.parse(bridge.getOrderBook(stockId)) : null;
            const isHalted = book && book.haltState && book.haltState !== 'NORMAL';

            // Refresh status pasar text in-place
            const statusStrong = flowPanel.querySelector('#detail-market-status-row strong');
            if (statusStrong && book) {
                statusStrong.textContent = book.haltState || 'NORMAL';
            }

            // Sync suspension notice
            let suspNotice = flowPanel.querySelector('#detail-suspension-notice');
            if (isHalted) {
                if (!suspNotice) {
                    suspNotice = node('p', 'notice', 'Pemberitahuan: Perdagangan instrumen ini sedang di-halt / disuspensi bursa. Order baru tidak dapat diproses.');
                    suspNotice.id = 'detail-suspension-notice';
                    flowPanel.appendChild(suspNotice);
                }
            } else if (suspNotice) {
                suspNotice.remove();
            }

            // Holdings update / clear if 0
            const holdingsCard = document.getElementById('detail-holdings-card');
            if (holdingsCard && currentAccount && bridge.getPortfolio) {
                const port = JSON.parse(bridge.getPortfolio(currentAccount.id));
                const myPos = (port.positions || []).find(p => p.instrumentId === stockId);
                if (myPos && myPos.quantity > 0) {
                    holdingsCard.replaceChildren();
                    flowText(holdingsCard, 'Kepemilikan di portofolio', `${myPos.quantity} saham${fresh.lotSize > 0 ? ' (' + Math.floor(myPos.quantity / fresh.lotSize) + ' lot)' : ''}`);
                    flowText(holdingsCard, 'Nilai posisi', myPos.marketValue);
                } else {
                    holdingsCard.replaceChildren(node('p', 'notice', 'Belum memiliki posisi pada instrumen ini.'));
                }
            }

            // Sync dual action button state
            const btnSell = flowPanel.querySelector('.btn-detail-sell');
            const btnBuy = flowPanel.querySelector('.btn-detail-buy');
            if (btnSell) btnSell.disabled = !fresh.isActiveTrading || isHalted;
            if (btnBuy) btnBuy.disabled = !fresh.isActiveTrading || isHalted;

            // Order Book update
            const depthTable = flowPanel.querySelector('.depth-table');
            if (depthTable && book && book.bids && book.asks) {
                const tbody = depthTable.querySelector('tbody');
                if (tbody) {
                    const rowCount = Math.max(book.bids.length, book.asks.length);
                    tbody.innerHTML = Array.from({ length: rowCount }).map((_, i) => {
                        const b = book.bids[i] || { price: '-', quantity: '-' };
                        const a = book.asks[i] || { price: '-', quantity: '-' };
                        return `<tr>
                            <td class="depth-bid">${b.quantity}</td>
                            <td class="positive">${b.price}</td>
                            <td class="negative">${a.price}</td>
                            <td class="depth-ask">${a.quantity}</td>
                        </tr>`;
                    }).join('');
                }
            }

            // Tape update
            const tapeSec = flowPanel.querySelector('#detail-tape-section');
            if (tapeSec && book) {
                renderTape(tapeSec, book.recentTrades || []);
            }

            // Refresh chart in-place
            if (typeof flowPanel.refreshChart === 'function') {
                flowPanel.refreshChart();
            }
        } catch(e) {
            console.warn('Detail sync error:', e);
        }
    }

    function loadPasarView() {
        try {
            if (latestSnapshot && latestSnapshot.clock) {
                const c = latestSnapshot.clock;
                const eb = document.getElementById('market-session-eyebrow');
                if (eb) {
                    if (c.isIdxTradingOpen) eb.textContent = 'IDX Buka (Sesi 1/2) · US Tutup';
                    else if (c.isUsTradingOpen) eb.textContent = 'US Buka (Reguler) · IDX Tutup';
                    else eb.textContent = 'Semua Bursa Tutup · ' + (c.sessionLabel || 'Pra-Sesi');
                }
            }

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
            idxContainer.className = 'market-ribbon table-scroll';
            allIndices.forEach(idx => {
                const item = document.createElement('div');
                item.className = 'index-card' + (idx.available === false ? ' unavailable' : '');
                item.dataset.ticker = idx.ticker;
                item.innerHTML = `
                    <div class="index-top">
                        <span>${idx.ticker}</span>
                        <span class="eyebrow">${idx.available === false ? 'Global' : 'Model'}</span>
                    </div>
                    <strong>${idx.value}</strong>
                    <div class="index-change-val ${idx.available === false ? '' : idx.up ? 'positive' : 'negative'}">
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
            const sectorSelect = document.getElementById('filter-sector-select');
            const selectedSector = sectorSelect.value;
            const sectors = [...new Set(JSON.parse(bridge.getInstruments('', '', null, true))
                .map(stock => stock.sector).filter(Boolean))].sort((a, b) => a.localeCompare(b, 'id'));
            // ponytail: kategori mengikuti katalog domain, tanpa daftar sektor kedua di UI.
            sectorSelect.replaceChildren(new Option('Semua kategori', ''), ...sectors.map(sector => new Option(sector, sector)));
            sectorSelect.value = sectors.includes(selectedSector) ? selectedSector : '';
            loadStocks();
        } catch (e) {
            console.error('Gagal memuat pasar:', e);
        }
    }

    function loadStocks() {
        const query = document.getElementById('search-stock-input').value;
        const exchange = document.getElementById('filter-exchange-select').value;
        const sector = document.getElementById('filter-sector-select').value;
        const raw = bridge.getInstruments(query, exchange, sector || null, true);
        const allStocks = JSON.parse(raw);
        const watchlistOnly = document.getElementById('filter-list-select').value === 'watchlist';
        const stocks = watchlistOnly ? allStocks.filter(stock => stock.isWatchlist) : allStocks;
        const listContainer = document.getElementById('stock-list-container');
        listContainer.innerHTML = '';
        document.getElementById('stock-list-status').textContent = stocks.length
            ? `${stocks.length} emiten pada daftar ini.`
            : watchlistOnly && !query.trim() && !exchange && !sector ? 'Watchlist masih kosong.' : 'Tidak ada emiten untuk filter ini.';

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
            const isFav = Boolean(st.isWatchlist);

            item.innerHTML = `
                <span class="stock-symbol" aria-hidden="true">${st.ticker.slice(0,2)}</span>
                <div class="stock-info">
                    <div class="stock-ticker flex-row">
                        <span>${st.ticker}</span>
                        <span class="badge">${st.exchange}</span>
                        ${st.isSectorLocked ? `<span class="badge-locked" style="background:rgba(255,160,0,0.18); color:#FFA000; border:1px solid rgba(255,160,0,0.4); border-radius:6px; padding:2px 6px; font-size:0.65rem; font-weight:700;">🔒 Lv.${st.requiredLevel}</span>` : ''}
                    </div>
                    <div class="stock-name">${st.name}</div>
                    <div class="stock-sector">${st.sector || ''}</div>
                </div>
                <div>
                    <div class="stock-price">${st.currentPrice}</div>
                    <div><span class="change-pill ${changeClass}">${changeSign}${st.dailyChangePercent}%</span></div>
                    <button class="watchlist-star-btn" type="button" aria-label="${isFav ? 'Hapus pantauan ' + st.ticker : 'Pantau ' + st.ticker}" aria-pressed="${isFav}">
                        <span aria-hidden="true">${isFav ? '★' : '☆'}</span>
                    </button>
                </div>
            `;

            const starBtn = item.querySelector('.watchlist-star-btn');
            if (starBtn) {
                const toggleWatchlistAction = (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    if (bridge.toggleWatchlist) {
                        try {
                            const res = JSON.parse(bridge.toggleWatchlist(st.id));
                            st.isWatchlist = Boolean(res.inWatchlist !== undefined ? res.inWatchlist : res.isWatchlist);
                            const starSpan = starBtn.querySelector('span') || starBtn;
                            starSpan.textContent = st.isWatchlist ? '★' : '☆';
                            starBtn.setAttribute('aria-pressed', String(st.isWatchlist));
                            starBtn.setAttribute('aria-label', (st.isWatchlist ? 'Hapus pantauan ' : 'Pantau ') + st.ticker);
                        } catch(err) {}
                    }
                };
                starBtn.addEventListener('click', toggleWatchlistAction);
                starBtn.addEventListener('keydown', (e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                        toggleWatchlistAction(e);
                    }
                });
            }

            item.tabIndex = 0;
            item.setAttribute('role', 'button');
            item.setAttribute('aria-label', `Detail ${st.ticker} ${st.exchange}, ${st.sector}`);
            item.addEventListener('click', (e) => {
                if (e.target.closest('.watchlist-star-btn')) return;
                openInstrumentDetail(st);
            });
            item.addEventListener('keydown', event => {
                if (event.target !== item) return;
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
            if (bridge.getIndices) {
                try {
                    const freshIndices = JSON.parse(bridge.getIndices());
                    if (Array.isArray(freshIndices)) {
                        freshIndices.forEach(idx => {
                            const card = document.querySelector(`.index-card[data-ticker="${idx.ticker}"]`);
                            if (card) {
                                const valEl = card.querySelector('strong');
                                if (valEl) valEl.textContent = idx.value;
                                const chgEl = card.querySelector('.index-change-val');
                                if (chgEl) {
                                    chgEl.textContent = idx.changePercent || '';
                                    chgEl.style.color = idx.up ? 'var(--positive)' : 'var(--negative)';
                                }
                            }
                        });
                    }
                } catch (idxErr) {}
            }
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
            if (portfolio.totalUnrealizedPnL) {
                flowText(allocation, 'Laba/Rugi Mengambang (Unrealized)', portfolio.totalUnrealizedPnL);
            }
            if (portfolio.totalRealizedPnL) {
                flowText(allocation, 'Laba Bersih Terealisasi (Realized)', portfolio.totalRealizedPnL);
            }
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

            flowButton(summary, 'Rincian Kekayaan Bersih Konsolidasi', () => openConsolidatedWealthModal());
            flowButton(summary, 'Reksa Dana Berbasis NAB', () => openMyMutualFundsModal());
            flowButton(summary, 'Utang & Pinjaman Tenor', () => openLoansModal());
            flowButton(summary, 'Bisnis operasional & anak usaha', () => { switchTab('firma'); loadFirmaView(); });
            flowButton(summary, 'Barang koleksi & properti mewah', () => openCollectionsGalleryModal());
            flowButton(summary, 'Multi-aset', () => openMultiAssets());
            flowButton(summary, 'Riwayat Transaksi Akun', () => openTransactionHistoryModal());
            flowButton(summary, 'Tukar IDR / USD', () => openFxForm());
            flowButton(summary, 'Fasilitas margin & risiko', () => openRiskModal());
            flowButton(summary, 'Audit Pajak & Kepatuhan Risiko', () => openTaxAuditModal());
            flowButton(summary, 'Asuransi Portofolio (Hedging)', () => openPortfolioInsuranceModal());
            flowButton(summary, 'Restrukturisasi Darurat (Turnaround)', () => executeBailoutAction());
            flowButton(summary, 'Racik Reksa Dana Kustom', () => openCustomFundBuilderModal());
            flowButton(summary, 'Aksi korporasi & voting RUPS', () => openCorporateActionModal());
            flowButton(summary, 'Uji stres portofolio', () => openStressTestModal());
            flowButton(summary, 'Lab strategi & sandbox what-if', () => openStrategyLabModal());
            flowButton(summary, 'Replay historis', () => openHistoricalReplayModal());
            if (!portfolio.positions.length) emptyState(posContainer,'Belum ada posisi','Pilih saham di Pasar dan tinjau order pertamamu.');
            portfolio.positions.forEach(position => {
                const button = flowButton(posContainer, `${position.instrumentId}: ${position.quantity} saham · ${position.marketValue}`, () => {
                    openPositionDetailModal(position.instrumentId);
                });
                button.className = 'action-link position-row';
                button.dataset.instrumentId = position.instrumentId;
            });
            const tools = node('details','card'); tools.id = 'asset-tools'; tools.append(node('summary','','Alat portofolio & eksperimen'));
            [...summary.children].filter(el => el.tagName === 'BUTTON' && !el.textContent.startsWith('Tukar')).forEach(el => tools.append(el));
            views.aset.append(tools);
        } catch (error) { summary.textContent = 'Portofolio gagal dimuat: ' + error.message; }
    }

    let currentFirmaSubtab = 'usaha';
    let collectionFilterCategory = 'ALL';
    let collectionFilterOwnership = 'ALL';

    function loadFirmaView(activeSubtab) {
        if (['usaha', 'koleksi', 'dana'].includes(activeSubtab)) currentFirmaSubtab = activeSubtab;
        refreshSnapshot();
        const view = views.firma;
        const overview = document.getElementById('career-overview');
        if (overview) {
            overview.replaceChildren();
            if (latestSnapshot?.career) {
                metricCard(overview, 'Jabatan saat ini', latestSnapshot.career.title, `Hari ${latestSnapshot.career.days} · Reputasi ${latestSnapshot.career.reputation}/100`);
            }
            const links = node('div', 'hub-links');
            flowButton(links, 'Misi Periodik', () => openPeriodicMissionsModal());
            flowButton(links, 'Liga & Rival', () => openLeagueAndRivalModal());
            flowButton(links, 'Level & pencapaian', openAchievementsModal);
            flowButton(links, 'Profil Saya & Statistik Lengkap', () => openPlayerProfileModal());
            overview.append(links);
            appendPrestigeSection(overview);
        }
        let subnav = view.querySelector('.firma-subnav');
        let content = document.getElementById('firma-subtab-content');

        if (!subnav || !content) {
            view.replaceChildren();
            pageHeading(view, 'Karier', 'Tujuan, usaha dan mandat');
            subnav = document.createElement('div');
            subnav.className = 'firma-subnav';
            subnav.innerHTML = `
                <button class="subtab-pill" id="firma-subtab-usaha" data-firma-subtab="usaha">Usaha Saya</button>
                <button class="subtab-pill" id="firma-subtab-koleksi" data-firma-subtab="koleksi">Koleksi</button>
                <button class="subtab-pill" id="firma-subtab-dana" data-firma-subtab="dana">Kelola Dana</button>
            `;
            content = document.createElement('div');
            content.id = 'firma-subtab-content';
            view.append(subnav, content);
        }

        const btnUsaha = subnav.querySelector('#firma-subtab-usaha');
        const btnKoleksi = subnav.querySelector('#firma-subtab-koleksi');
        const btnDana = subnav.querySelector('#firma-subtab-dana');
        if (btnUsaha) btnUsaha.onclick = () => loadFirmaView('usaha');
        if (btnKoleksi) btnKoleksi.onclick = () => loadFirmaView('koleksi');
        if (btnDana) btnDana.onclick = () => loadFirmaView('dana');

        subnav.querySelectorAll('.subtab-pill').forEach(btn => {
            const isTarget = btn.getAttribute('data-firma-subtab') === currentFirmaSubtab;
            btn.classList.toggle('active', isTarget);
            btn.setAttribute('aria-pressed', String(isTarget));
        });

        content.replaceChildren();

        if (currentFirmaSubtab === 'usaha') {
            renderFirmaUsahaSection(content);
        } else if (currentFirmaSubtab === 'koleksi') {
            renderCollectionsGallery(content, false);
        } else if (currentFirmaSubtab === 'dana') {
            renderFirmaDanaSection(content);
        }
    }

    function renderFirmaUsahaSection(container) {
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
                        cCard.className = 'card company-summary-card';
                        cCard.setAttribute('data-company-id', c.id);

                        const sectorLabelMap = {
                            'MANUFAKTUR': 'Manufaktur & Produksi',
                            'RITEL': 'Ritel & Distribusi',
                            'JASA': 'Jasa & Konsultasi'
                        };
                        const sectorIndo = sectorLabelMap[(c.sectorModel || c.sector || '').toUpperCase()] || c.sector || 'Usaha Operasional';

                        const netProfit = c.dailyNetProfit == null ? NaN : Number(c.dailyNetProfit);
                        const meta = node('div', 'section-heading');
                        meta.append(node('span', 'eyebrow', sectorIndo), node('span', 'badge', c.level == null ? 'Level belum tersedia' : `Level ${c.level}`));
                        cCard.append(meta, node('h3', '', c.name));
                        flowText(cCard, 'Kas operasional', formatMoney(c.cash));
                        const profitRow = flowText(cCard, 'Perkiraan laba bersih', Number.isFinite(netProfit) ? `${netProfit >= 0 ? '+' : ''}${formatMoney(netProfit)} / hari` : null);
                        if (Number.isFinite(netProfit)) profitRow.querySelector('strong').className = netProfit >= 0 ? 'positive' : 'negative';
                        flowText(cCard, 'Staf & eksekutif', `${c.employeeCount == null ? 'Jumlah pegawai belum tersedia' : c.employeeCount + ' pegawai'} · CEO: ${c.ceoName || 'Belum tersedia'}`);

                        const btnOpen = document.createElement('button');
                        btnOpen.className = 'btn-primary';
                        btnOpen.style.marginTop = '10px';
                        btnOpen.textContent = 'Kelola usaha & operasional';
                        btnOpen.onclick = () => openCompanyDetailModal(c);
                        cCard.appendChild(btnOpen);

                        compSection.append(cCard);
                    });
                }
            } catch (err) {
                flowText(compSection, 'Perusahaan gagal dimuat', err.message);
            }
        }
        container.append(compSection);
    }

    function openCompanyDetailModal(company, activeSubtab = 'ringkasan') {
        const panel = showFlow(company.name);
        flowText(panel, 'Unit Usaha Operasional', `${company.name} · Sektor ${company.sectorModel || company.sector || 'Bisnis'}`);

        const navWrap = document.createElement('div');
        navWrap.className = 'company-local-nav';
        const subtabs = [
            { id: 'ringkasan', label: 'Ringkasan' },
            { id: 'tim', label: 'Tim & SDM' },
            { id: 'fasilitas', label: 'Fasilitas & moral' },
            { id: 'keuangan', label: 'Keuangan' }
        ];

        const contentBox = document.createElement('div');
        contentBox.className = 'company-subtab-box';

        subtabs.forEach(tab => {
            const btn = document.createElement('button');
            btn.className = 'subtab-pill' + (activeSubtab === tab.id ? ' active' : '');
            btn.textContent = tab.label;
            btn.type = 'button';
            btn.onclick = () => openCompanyDetailModal(company, tab.id);
            btn.setAttribute('aria-pressed', String(activeSubtab === tab.id));
            navWrap.appendChild(btn);
        });
        panel.append(navWrap, contentBox);

        if (activeSubtab === 'ringkasan') {
            const card = document.createElement('section');
            card.className = 'card';
            card.innerHTML = `<h2 class="card-title">Ringkasan Operasional Perusahaan</h2>`;
            flowText(card, 'Tingkat skala', `Level ${company.level} (${company.sectorModel || company.sector})`);
            flowText(card, 'Saldo kas perusahaan', `Rp ${Number(company.cash || 0).toLocaleString('id-ID')}`);
            flowText(card, 'CEO / pimpinan', company.ceoName);
            flowText(card, 'Proyeksi pendapatan', `+Rp ${Number(company.dailyRevenue || 0).toLocaleString('id-ID')} / hari`);
            flowText(card, 'Beban gaji (payroll)', `-Rp ${Number(company.dailyPayroll || 0).toLocaleString('id-ID')} / hari`);
            flowText(card, 'Biaya operasional & fasilitas', `-Rp ${Number(company.dailyOperatingCost || 0).toLocaleString('id-ID')} / hari`);
            const net = Number(company.dailyNetProfit != null ? company.dailyNetProfit : (Number(company.dailyRevenue || 0) - Number(company.dailyPayroll || 0) - Number(company.dailyOperatingCost || 0)));
            flowText(card, 'Laba bersih harian', `Rp ${net.toLocaleString('id-ID')} / hari`);

            const upBtn = document.createElement('button');
            upBtn.className = 'btn-primary';
            upBtn.style.marginTop = '12px';
            upBtn.textContent = `Tingkatkan skala ke level ${company.level + 1}`;
            upBtn.onclick = () => {
                const request = { companyId: company.id, action: 'UPGRADE', workHours: company.workHours || 8, benefitTier: company.benefitTier || 'STANDARD' };
                reviewCompanyAction(request);
            };
            card.append(upBtn);
            contentBox.append(card);

        } else if (activeSubtab === 'tim') {
            const card = document.createElement('section');
            card.className = 'card';
            card.innerHTML = `<h2 class="card-title">Divisi SDM & Struktur Organisasi</h2>`;
            flowText(card, 'Total karyawan aktif', `${company.employeeCount || (company.employees ? company.employees.length : 0)} pegawai`);
            flowText(card, 'Pengeluaran payroll', `Rp ${Number(company.dailyPayroll || 0).toLocaleString('id-ID')} / hari`);

            const hireBtn = document.createElement('button');
            hireBtn.className = 'btn-primary';
            hireBtn.style.marginTop = '10px';
            hireBtn.textContent = 'Rekrut pegawai & eksekutif';
            hireBtn.onclick = () => openCompanyStaffModal(company);
            card.append(hireBtn);

            const staffList = document.createElement('div');
            staffList.style.marginTop = '12px';
            const emps = company.employees || [];
            if (!emps.length) {
                flowText(card, 'Staf operasional', 'Belum ada pegawai direkrut. Rekrut staf untuk meningkatkan kapasitas produksi dan moral.');
            } else {
                emps.forEach(emp => {
                    const row = document.createElement('div');
                    row.className = 'data-row';
                    const identity = node('div');
                    identity.append(node('strong', '', emp.name), node('small', '', `${emp.roleLabel || emp.role} · Level ${emp.level}`));
                    row.append(identity, node('strong', '', `${formatMoney(emp.dailySalary)} / hari`));
                    staffList.append(row);
                });
                card.append(staffList);
            }
            contentBox.append(card);

        } else if (activeSubtab === 'fasilitas') {
            const card = document.createElement('section');
            card.className = 'card';
            card.innerHTML = `<h2 class="card-title">Kebijakan Kerja, Fasilitas & Moral</h2>`;
            flowText(card, 'Kebijakan jam kerja', company.workHours == null ? null : `${company.workHours} jam per hari`);
            flowText(card, 'Tingkat tunjangan fasilitas', company.benefitTier);
            flowText(card, 'Indeks kepuasan moral', company.morale == null ? null : `${company.morale}%`);

            const polBtn = document.createElement('button');
            polBtn.className = 'btn-primary';
            polBtn.style.marginTop = '12px';
            polBtn.textContent = 'Ubah kebijakan & fasilitas kerja';
            polBtn.onclick = () => openCompanyPolicyModal(company);
            card.append(polBtn);
            contentBox.append(card);

        } else if (activeSubtab === 'keuangan') {
            const card = document.createElement('section');
            card.className = 'card';
            card.innerHTML = `<h2 class="card-title">Keuangan, Modal & Laporan Usaha</h2>`;
            flowText(card, 'Saldo kas usaha', `Rp ${Number(company.cash || 0).toLocaleString('id-ID')}`);
            flowText(card, 'Tunggakan gaji', `Rp ${Number(company.payrollArrears || 0).toLocaleString('id-ID')}`);

            const actionGroup = document.createElement('div');
            actionGroup.className = 'shortcut-grid';

            const capBtn = document.createElement('button');
            capBtn.className = 'btn-primary';
            capBtn.textContent = 'Suntik modal / tarik dividen';
            capBtn.onclick = () => openCompanyCapitalModal(company);

            const loanBtn = document.createElement('button');
            loanBtn.className = 'btn-secondary';
            loanBtn.textContent = 'Ajukan pinjaman modal kerja';
            loanBtn.onclick = () => openDisburseLoanModal('LOAN-CORP-01', company.id);

            actionGroup.append(capBtn, loanBtn);
            card.append(actionGroup);

            // Riwayat Laporan Harian
            const reportHeading = document.createElement('h3');
            reportHeading.className = 'card-title';
            reportHeading.textContent = 'Riwayat Laporan Keuangan Harian';
            card.append(reportHeading);

            const reports = company.operationalReports || [];
            if (!reports.length) {
                flowText(card, 'Laporan keuangan', 'Belum ada siklus hari operasional yang diselesaikan.');
            } else {
                reports.slice().reverse().forEach(rep => {
                    const rRow = document.createElement('div');
                    rRow.className = 'card';
                    rRow.classList.add('card');
                    flowText(rRow, 'Hari / Tanggal', `Hari #${rep.day || rep.date || '-'}`);
                    flowText(rRow, 'Pendapatan usaha', `+Rp ${Number(rep.revenue || 0).toLocaleString('id-ID')}`);
                    flowText(rRow, 'Beban operasi & gaji', `-Rp ${(Number(rep.operatingCost || 0) + Number(rep.payrollPaid || 0)).toLocaleString('id-ID')}`);
                    flowText(rRow, 'Arus kas bersih', `Rp ${Number(rep.netCashflow || 0).toLocaleString('id-ID')}`);
                    card.append(rRow);
                });
            }
            contentBox.append(card);
        }
    }

    function renderCollectionsGallery(container, isModal = false) {
        const wrap = document.createElement('div');
        wrap.className = 'collections-gallery-wrap';
        const categoryLabels = {
            VEHICLE: 'Kendaraan mewah', REAL_ESTATE: 'Properti & vila',
            JEWELRY: 'Perhiasan & berlian', ART_AND_WATCHES: 'Seni & jam tangan',
            MOTOR: 'Motor & kendaraan roda dua', MOBIL: 'Mobil kolektor',
            RUMAH: 'Hunian & properti', KOMERSIAL: 'Properti komersial & lahan',
            JAM: 'Jam tangan', PERMATA: 'Perhiasan & batu mulia', SENI: 'Karya seni',
            ANTIK: 'Barang antik', KAPAL: 'Yacht & kapal', PESAWAT: 'Jet & helikopter',
            TEKNO: 'Teknologi & aset digital', MEMO: 'Memorabilia'
        };
        let items;
        try {
            if (!bridge.getCollections) throw new Error('Layanan koleksi belum tersedia.');
            const response = JSON.parse(bridge.getCollections());
            if (response.success === false) throw new Error(response.message || 'Katalog gagal dimuat.');
            items = Array.isArray(response) ? response : response.collections || response.items || [];
        } catch (error) {
            flowText(wrap, 'Koleksi belum dapat dimuat', error.message);
            container.replaceChildren(wrap); return;
        }

        const controls = document.createElement('div');
        controls.className = 'collection-controls';

        const catRow = document.createElement('div');
        catRow.className = 'category-pills';
        const categories = [...new Set(items.map(item => String(item.category || '').toUpperCase()).filter(Boolean))];
        const cats = [{ id: 'ALL', label: `Semua (${items.length})` }, ...categories.map(id => ({ id, label: `${categoryLabels[id] || id} (${items.filter(item => String(item.category || '').toUpperCase() === id).length})` }))];
        if (!cats.some(category => category.id === collectionFilterCategory)) collectionFilterCategory = 'ALL';
        cats.forEach(c => {
            const btn = document.createElement('button');
            btn.className = 'subtab-pill' + (collectionFilterCategory === c.id ? ' active' : '');
            btn.textContent = c.label;
            btn.type = 'button';
            btn.setAttribute('aria-pressed', String(collectionFilterCategory === c.id));
            btn.onclick = () => {
                collectionFilterCategory = c.id;
                renderCollectionsGallery(container, isModal);
            };
            catRow.appendChild(btn);
        });

        const statusRow = document.createElement('div');
        statusRow.className = 'filter-chips';
        const statuses = [
            { id: 'ALL', label: 'Semua Status' },
            { id: 'OWNED', label: '✓ Dimiliki' },
            { id: 'UNOWNED', label: '○ Belum Dimiliki' }
        ];
        statuses.forEach(s => {
            const btn = document.createElement('button');
            btn.className = 'subtab-pill' + (collectionFilterOwnership === s.id ? ' active' : '');
            btn.textContent = s.label;
            btn.type = 'button';
            btn.onclick = () => {
                collectionFilterOwnership = s.id;
                renderCollectionsGallery(container, isModal);
            };
            statusRow.appendChild(btn);
        });

        controls.append(catRow, statusRow);
        wrap.appendChild(controls);

        const cardList = document.createElement('div');
        cardList.id = 'collections-card-list';
        wrap.appendChild(cardList);

        try {
            if (!items.length) {
                flowText(cardList, 'Koleksi', 'Katalog koleksi belum tersedia.');
                container.replaceChildren(wrap);
                return;
            }

            let filtered = items;
            if (collectionFilterCategory !== 'ALL') {
                filtered = filtered.filter(i => (i.category || '').toUpperCase() === collectionFilterCategory);
            }
            const rarityLabels = {
                'STANDARD': 'Umum',
                'UMUM': 'Umum',
                'TIDAK_UMUM': 'Tidak Umum',
                'RARE': 'Langka',
                'LANGKA': 'Langka',
                'EPIK': 'Epik',
                'LEGENDARY': 'Legendaris',
                'LEGENDARIS': 'Legendaris'
            };

            function createCollectionCard(item, isOwned) {
                const card = document.createElement('article');
                card.className = 'card collection-item-card' + (isOwned ? ' owned-luxury' : '');
                card.setAttribute('data-collection-id', item.id);

                const catIndo = categoryLabels[(item.category || '').toUpperCase()] || item.category || 'Aset Mewah';
                const rarityIndo = rarityLabels[(item.rarity || '').toUpperCase()] || item.rarityLabel || item.rarity || 'Standar';

                const headerDiv = document.createElement('div');
                headerDiv.className = 'section-heading';
                
                const leftMeta = document.createElement('div');
                leftMeta.className = 'flex-row';
                leftMeta.append(node('span', 'eyebrow', catIndo));
                if (isOwned) {
                    leftMeta.append(node('span', 'badge-owned', 'Dimiliki'));
                }
                if (item.isPledged || item.pledgedLoanId) {
                    leftMeta.append(node('span', 'badge-pledged', 'Diagunkan'));
                }

                headerDiv.append(leftMeta);
                headerDiv.append(node('span', 'change-pill', rarityIndo));
                card.appendChild(headerDiv);

                const visualWrap = node('div', 'collection-visual-frame', `${catIndo} · ${rarityIndo}`);
                card.appendChild(visualWrap);

                const title = document.createElement('h3');
                title.className = 'card-title';
                title.textContent = item.name;
                card.appendChild(title);

                flowText(card, 'Status kepemilikan', isOwned ? '✓ Dimiliki Pemain' : '○ Tersedia di Galeri');
                flowText(card, 'Valuasi terkini', formatMoney(item.currentValuation));
                flowText(card, 'Biaya pemeliharaan', item.dailyMaintenanceCost == null ? null : `${formatMoney(item.dailyMaintenanceCost)} / hari`);
                flowText(card, 'Bonus reputasi', item.reputationBonus == null ? null : `+${item.reputationBonus} poin reputasi`);

                const actBtn = document.createElement('button');
                actBtn.style.marginTop = '10px';

                if (isOwned) {
                    if (item.isPledged || item.pledgedLoanId) {
                        actBtn.className = 'btn-secondary';
                        actBtn.disabled = true;
                        actBtn.textContent = 'Diagunkan (tidak dapat dijual)';
                    } else {
                        actBtn.className = 'btn-secondary';
                        actBtn.textContent = `Jual Koleksi (Terima Rp ${Number(item.currentValuation || 0).toLocaleString('id-ID')})`;
                        let selling = false;
                        actBtn.onclick = () => {
                            if (selling) return;
                            selling = true;
                            actBtn.disabled = true;
                            actBtn.textContent = 'Memproses penjualan...';
                            try {
                                const res = JSON.parse(bridge.transactCollection(JSON.stringify({requestId: 'COL-SELL-' + Date.now(), itemId: item.id, buy: false})));
                                if (!res.success) throw new Error(res.message);
                                showGameToast('success', 'Koleksi Berhasil Terjual!', `${item.name} berhasil dijual`);
                                refreshSnapshot();
                                renderCollectionsGallery(container, isModal);
                            } catch (err) {
                                selling = false;
                                actBtn.disabled = false;
                                actBtn.textContent = `Jual Koleksi (Terima Rp ${Number(item.currentValuation || 0).toLocaleString('id-ID')})`;
                                showGameToast('info', 'Penjualan Gagal', err.message);
                            }
                        };
                    }
                } else {
                    actBtn.className = 'btn-primary';
                    actBtn.textContent = `Beli Koleksi (Rp ${Number(item.basePrice || 0).toLocaleString('id-ID')})`;
                    let buying = false;
                    actBtn.onclick = () => {
                        if (buying) return;
                        buying = true;
                        actBtn.disabled = true;
                        actBtn.textContent = 'Memproses pembelian...';
                        try {
                            const res = JSON.parse(bridge.transactCollection(JSON.stringify({requestId: 'COL-BUY-' + Date.now(), itemId: item.id, buy: true})));
                            if (!res.success) throw new Error(res.message);
                            showGameToast('success', 'Koleksi Berhasil Dibeli!', `${item.name} masuk ke galeri aset mewah`);
                            refreshSnapshot();
                            renderCollectionsGallery(container, isModal);
                        } catch (err) {
                            buying = false;
                            actBtn.disabled = false;
                            actBtn.textContent = `Beli Koleksi (Rp ${Number(item.basePrice || 0).toLocaleString('id-ID')})`;
                            showGameToast('info', 'Pembelian Gagal', err.message);
                        }
                    };
                }
                card.appendChild(actBtn);
                return card;
            }

            const unownedItems = filtered.filter(i => !Boolean(i.isOwned || i.owned));
            const ownedItems = filtered.filter(i => Boolean(i.isOwned || i.owned));

            if (collectionFilterOwnership === 'ALL') {
                // Tampilan Semua Status: dua kelompok (Belum Dimiliki & Sudah Dimiliki)
                const secUnowned = document.createElement('div');
                secUnowned.className = 'collection-section-group';
                secUnowned.style.marginBottom = '20px';
                secUnowned.innerHTML = `<h2 class="card-title" style="margin:12px 0 8px; font-size:0.95rem; display:flex; justify-content:space-between; align-items:center;">
                    <span>Belum Dimiliki</span>
                    <span class="badge">${unownedItems.length}</span>
                </h2>`;
                if (!unownedItems.length) {
                    const empty = document.createElement('p');
                    empty.className = 'empty-state';
                    empty.textContent = 'Seluruh koleksi pada kategori ini telah Anda miliki.';
                    secUnowned.appendChild(empty);
                } else {
                    unownedItems.forEach(item => secUnowned.appendChild(createCollectionCard(item, false)));
                }
                cardList.appendChild(secUnowned);

                const secOwned = document.createElement('div');
                secOwned.className = 'collection-section-group';
                secOwned.style.marginBottom = '20px';
                secOwned.innerHTML = `<h2 class="card-title" style="margin:16px 0 8px; font-size:0.95rem; display:flex; justify-content:space-between; align-items:center;">
                    <span>Sudah Dimiliki</span>
                    <span class="badge-owned">${ownedItems.length}</span>
                </h2>`;
                if (!ownedItems.length) {
                    const empty = document.createElement('p');
                    empty.className = 'empty-state';
                    empty.textContent = 'Belum ada barang koleksi yang dimiliki pada kategori ini. Beli barang di atas untuk menambah portofolio kemewahan!';
                    secOwned.appendChild(empty);
                } else {
                    ownedItems.forEach(item => secOwned.appendChild(createCollectionCard(item, true)));
                }
                cardList.appendChild(secOwned);
            } else if (collectionFilterOwnership === 'OWNED') {
                const secOwned = document.createElement('div');
                secOwned.innerHTML = `<h2 class="card-title" style="margin:12px 0 8px; font-size:0.95rem; display:flex; justify-content:space-between; align-items:center;">
                    <span>Sudah Dimiliki</span>
                    <span class="badge-owned">${ownedItems.length}</span>
                </h2>`;
                if (!ownedItems.length) {
                    const empty = document.createElement('p');
                    empty.className = 'empty-state';
                    empty.textContent = 'Belum ada koleksi yang dimiliki pada kategori ini.';
                    secOwned.appendChild(empty);
                } else {
                    ownedItems.forEach(item => secOwned.appendChild(createCollectionCard(item, true)));
                }
                cardList.appendChild(secOwned);
            } else {
                // UNOWNED
                const secUnowned = document.createElement('div');
                secUnowned.innerHTML = `<h2 class="card-title" style="margin:12px 0 8px; font-size:0.95rem; display:flex; justify-content:space-between; align-items:center;">
                    <span>Belum Dimiliki</span>
                    <span class="badge">${unownedItems.length}</span>
                </h2>`;
                if (!unownedItems.length) {
                    const empty = document.createElement('p');
                    empty.className = 'empty-state';
                    empty.textContent = 'Semua koleksi pada kategori ini telah Anda miliki.';
                    secUnowned.appendChild(empty);
                } else {
                    unownedItems.forEach(item => secUnowned.appendChild(createCollectionCard(item, false)));
                }
                cardList.appendChild(secUnowned);
            }
        } catch(e) {
            flowText(cardList, 'Koleksi gagal dimuat', e.message);
        }
        container.replaceChildren(wrap);
    }

    function renderFirmaDanaSection(container) {
        if (!bridge.getFirmSnapshot) {
            flowText(container, 'Kelola Dana', 'Bridge dana kelolaan belum tersedia.');
            return;
        }
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

                flowButton(summary, 'Fund Builder & alokasi portofolio', () => openFundBuilder(), 'fundBuilder');
                flowButton(summary, 'Laporan klien & fee mandat', () => openClientReports());
                flowButton(summary, 'Bursa Headhunting Staf Analis Elit', () => openHeadhuntingModal());
                flowText(summary, 'Batas', 'Tarif dan batas mengikuti aturan simulasi awal. Periksa ketentuan setiap mandat.');
                container.append(summary);

                container.append(node('h2', 'card-title', 'Klien & tawaran mandat'));
                firm.mandates.forEach(mandate => flowButton(container, `${mandate.clientName} · ${mandate.statusLabel}`, () => {
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
                                loadFirmaView('dana');
                            } catch (err) {
                                flowText(panel, 'Penerimaan Mandat Ditolak', err.message);
                            }
                        }, 'mandates');
                    } else if (mandate.status === 'ACTIVE') {
                        flowText(panel, 'Status Mandat', `Mandat aktif dikelola. Akun kelolaan: ${mandate.accountId}`);
                    }
                }));
            }
        } catch (error) { flowText(container, 'Firma gagal dimuat', error.message); }
    }

    function openFoundCompanyModal() {
        const panel = showFlow('Dirikan Perusahaan Baru');
        flowText(panel, 'Syarat & ketentuan', 'Pendirian anak usaha memerlukan modal minimal Rp 25.000.000 dari kas pribadi.');

        const nameLabel = document.createElement('label');
        nameLabel.textContent = 'Nama perusahaan'; nameLabel.htmlFor = 'company-name';
        const nameInput = document.createElement('input'); nameInput.id = 'company-name';
        nameInput.className = 'input-field';
        nameInput.placeholder = 'Contoh: Nusantara Tech Solusindo';

        const secLabel = document.createElement('label');
        secLabel.textContent = 'Sektor bisnis'; secLabel.htmlFor = 'company-sector';
        const secSelect = document.createElement('select'); secSelect.id = 'company-sector';
        secSelect.className = 'input-field';
        secSelect.innerHTML = `
            <option value="JASA">Jasa & Konsultasi</option>
            <option value="RITEL">Ritel & Barang Konsumsi</option>
            <option value="MANUFAKTUR">Manufaktur & Produksi</option>
        `;

        const capLabel = document.createElement('label');
        capLabel.textContent = 'Modal disetor (minimal Rp 25.000.000)'; capLabel.htmlFor = 'company-capital';
        const capInput = document.createElement('input'); capInput.id = 'company-capital';
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

        const companyId = 'COMP-' + crypto.randomUUID();
        submitBtn.onclick = () => {
            try {
                const name = nameInput.value.trim();
                if (!name) throw new Error('Nama perusahaan wajib diisi');
                const capital = Number(capInput.value);
                if (capital < 25000000) throw new Error('Modal minimal Rp 25.000.000');

                const req = {
                    id: companyId,
                    name: name,
                    sector: secSelect.value,
                    capital: capInput.value
                };
                const res = JSON.parse(bridge.foundCompany(JSON.stringify(req)));
                if (!res.success) throw new Error(res.message);
                submitBtn.disabled = true;
                statusMsg.textContent = 'Perusahaan berhasil didirikan!';
                statusMsg.style.color = 'var(--positive)';
                showGameToast('success', 'Perusahaan Berhasil Didirikan!', `${name} resmi beroperasi.`);
                refreshSnapshot();
                setTimeout(() => { closeFlow(); loadFirmaView('usaha'); }, 600);
            } catch (err) {
                statusMsg.textContent = 'Gagal: ' + err.message;
                statusMsg.style.color = 'var(--negative)';
            }
        };

        panel.append(nameLabel, nameInput, secLabel, secSelect, capLabel, capInput, submitBtn, statusMsg);
    }

    function openCompanyStaffModal(company) {
        const panel = showFlow(`Pegawai · ${company.name}`);
        try {
            const staff = JSON.parse(bridge.getCompanyStaff(company.id));
            if (!staff.success) throw new Error(staff.message);
            flowText(panel, 'Payroll harian', `Rp ${staff.dailyPayroll}`);
            flowText(panel, 'Tunggakan gaji', `Rp ${staff.payrollArrears}`);
            flowText(panel, 'Waktu berlaku', 'Perubahan dihitung pada penyelesaian operasi harian berikutnya.');
            for (const employee of staff.employees) {
                const card = node('section', 'card');
                flowText(card, employee.name, `${employee.department} · ${employee.roleLabel} · Level ${employee.level}`);
                flowText(card, 'Gaji harian', `Rp ${employee.salary}`);
                const train = flowButton(card, 'Latih pegawai', () => openCompanyStaffReview(company, 'TRAIN', employee.id));
                train.disabled = employee.level >= 5;
                if (train.disabled) train.textContent = 'Level maksimum';
                if (employee.role !== 'CEO') {
                    for (const vacancy of staff.candidates.filter(candidate => candidate.available))
                        flowButton(card, `Tempatkan: ${vacancy.roleLabel}`, () => openCompanyStaffReview(company, 'ASSIGN', employee.id, vacancy.role));
                    flowButton(card, 'Berhentikan pegawai', () => openCompanyStaffReview(company, 'DISMISS', employee.id));
                }
                panel.append(card);
            }
            for (const candidate of staff.candidates.filter(candidate => candidate.available)) {
                const card = node('section', 'card');
                flowText(card, candidate.name, `${candidate.department} · ${candidate.roleLabel}`);
                flowText(card, 'Gaji harian', `Rp ${candidate.salary}`);
                flowButton(card, 'Tinjau perekrutan', () => openCompanyStaffReview(company, 'HIRE', candidate.id, candidate.role));
                panel.append(card);
            }
        } catch (error) { flowText(panel, 'Pegawai belum dapat dimuat', error.message); }
    }

    function openCompanyStaffReview(company, action, employeeId, role) {
        const panel = showFlow('Tinjau perubahan pegawai', true);
        const request = {companyId:company.id, action, employeeId, role, requestId:'STAFF-' + crypto.randomUUID()};
        try {
            const quote = JSON.parse(bridge.previewCompanyStaffAction(JSON.stringify(request)));
            if (!quote.success) throw new Error(quote.message);
            request.expectedVersion = quote.staffVersion;
            flowText(panel, 'Perusahaan', company.name);
            flowText(panel, 'Biaya satu kali', `Rp ${quote.cost}`);
            flowText(panel, 'Payroll harian', `Rp ${quote.dailyPayrollBefore} → Rp ${quote.dailyPayrollAfter}`);
            flowText(panel, 'Gaji pegawai', `Rp ${quote.salaryBefore} → Rp ${quote.salaryAfter}`);
            flowText(panel, 'Mulai berlaku', 'Operasi harian berikutnya; tunggakan lama tetap wajib dibayar.');
            const status = node('p'); status.setAttribute('role','status'); panel.append(status);
            const submit = flowButton(panel, 'Konfirmasi perubahan pegawai', () => {
                try {
                    const receipt = JSON.parse(bridge.manageCompanyStaff(JSON.stringify(request)));
                    if (!receipt.success) throw new Error(receipt.message);
                    submit.disabled = true;
                    status.textContent = `Perubahan tersimpan. Bukti: ${receipt.receiptId}`;
                    refreshSnapshot();
                    flowButton(panel, 'Kembali ke pegawai', () => openCompanyStaffModal(company));
                } catch (error) { status.textContent = error.message; }
            });
        } catch (error) { flowText(panel, 'Perubahan ditolak', error.message); }
    }

    function openCompanyPolicyModal(company) {
        const panel = showFlow(`Kebijakan kerja · ${company.name}`);
        flowText(panel, 'Pengaturan operasional', 'Kebijakan kerja memengaruhi produktivitas, kepuasan staf, dan biaya operasional.');

        const hourLabel = document.createElement('label');
        hourLabel.textContent = 'Jam kerja per hari';
        const hourSelect = document.createElement('select');
        hourSelect.id = 'company-work-hours'; hourLabel.htmlFor = hourSelect.id;
        hourSelect.className = 'input-field';
        hourSelect.innerHTML = `
            <option value="6" ${company.workHours === 6 ? 'selected' : ''}>6 Jam (Fleksibel & Sejahtera)</option>
            <option value="8" ${company.workHours === 8 || !company.workHours ? 'selected' : ''}>8 Jam (Standar Produktif)</option>
            <option value="10" ${company.workHours === 10 ? 'selected' : ''}>10 Jam (Intensif & Cepat)</option>
        `;

        const benLabel = document.createElement('label');
        benLabel.textContent = 'Tingkat fasilitas karyawan';
        const benSelect = document.createElement('select');
        benSelect.id = 'company-benefits'; benLabel.htmlFor = benSelect.id;
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
                reviewCompanyAction(Object.assign(req, {action:'POLICY'}));
            } catch (err) {
                statusMsg.textContent = 'Gagal: ' + err.message;
                statusMsg.style.color = 'var(--negative)';
            }
        };

        panel.append(hourLabel, hourSelect, benLabel, benSelect, saveBtn, statusMsg);
    }

    function openCompanyCapitalModal(company) {
        const panel = showFlow(`Transfer modal · ${company.name}`);
        flowText(panel, 'Arus kas modal', 'Pindahkan dana antara kas pribadi dan anak usaha. Total kekayaan pribadi tetap konsisten.');

        const dirLabel = document.createElement('label');
        dirLabel.textContent = 'Arah transfer dana';
        const dirSelect = document.createElement('select');
        dirSelect.id = 'company-transfer-direction'; dirLabel.htmlFor = dirSelect.id;
        dirSelect.className = 'input-field';
        dirSelect.innerHTML = `
            <option value="TO_COMPANY">Suntik Modal (Pribadi -> Anak Usaha)</option>
            <option value="TO_PERSONAL">Tarik Dividen (Anak Usaha -> Pribadi)</option>
        `;

        const amtLabel = document.createElement('label');
        amtLabel.textContent = 'Jumlah dana (IDR)';
        const amtInput = document.createElement('input');
        amtInput.id = 'company-transfer-amount'; amtLabel.htmlFor = amtInput.id;
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
                    amount: String(amt),
                    toCompany: dirSelect.value === 'TO_COMPANY'
                };
                const quote = JSON.parse(bridge.companyCapitalAction(JSON.stringify(req)));
                commandReview('Review transfer modal', quote, [
                    ['Jumlah', formatMoney(req.amount)], ['Arah', req.toCompany ? 'Pribadi → Perusahaan' : 'Perusahaan → Pribadi'],
                    ['Cadangan operasi', formatMoney(quote.reserve || quote.reserveRaw || 0)]
                ], (requestId) => bridge.companyCapitalAction(JSON.stringify(Object.assign({}, req, {requestId,expectedVersion:quote.version}))), loadFirmaView);
            } catch (err) {
                statusMsg.textContent = 'Transfer gagal: ' + err.message;
                statusMsg.style.color = 'var(--negative)';
            }
        };

        panel.append(dirLabel, dirSelect, amtLabel, amtInput, submitBtn, statusMsg);
    }

    function commandReview(title, quote, rows, execute, after) {
        if (!quote || quote.success === false || quote.eligible === false) throw new Error((quote && quote.message) || 'Syarat transaksi belum terpenuhi');
        const panel = showFlow(title);
        rows.forEach(([label,value]) => flowText(panel,label,String(value)));
        const requestId = 'CMD-' + Date.now() + '-' + Math.random().toString(36).slice(2,10);
        const status = node('p'); status.setAttribute('role','status'); panel.append(status);
        const button = flowButton(panel, 'Konfirmasi', () => {
            button.disabled = true; status.textContent = 'Memproses…';
            try {
                const result = JSON.parse(execute(requestId));
                if (!result || result.success === false) throw new Error((result && result.message) || 'Hasil belum terkonfirmasi');
                status.textContent = 'Tindakan selesai dan tersimpan.';
                refreshSnapshot();
                flowButton(panel, 'Selesai', () => { closeFlow(); if (after) after(); });
            } catch (error) { button.disabled = false; status.textContent = error.message + ' Ulangi dengan permintaan yang sama.'; }
        });
    }

    function reviewCompanyAction(request) {
        try {
        const quote = bridgeResult('previewCompanyAction',JSON.stringify(request));
        commandReview(request.action === 'UPGRADE' ? 'Review peningkatan perusahaan' : 'Review kebijakan kerja', quote, [
            ['Level', `${quote.levelBefore} → ${quote.levelAfter}`], ['Biaya', formatMoney(quote.cost)],
            ['Gaji harian', `${formatMoney(quote.dailyPayrollBefore)} → ${formatMoney(quote.dailyPayrollAfter)}`],
            ['Biaya operasi', `${formatMoney(quote.dailyOperatingCostBefore)} → ${formatMoney(quote.dailyOperatingCostAfter)}`],
            ['Berlaku', 'Pada perhitungan operasional berikutnya']
        ], requestId => bridge.executeCompanyAction(JSON.stringify(Object.assign({}, request,{requestId,expectedVersion:quote.version}))), loadFirmaView);
        } catch(error) { const panel=showFlow('Perubahan perusahaan belum tersedia');flowText(panel,'Alasan',error.message); }
    }

    function openMultiAssetOrder(product, buy) {
        const panel = showFlow(`${buy ? 'Beli' : 'Jual'} ${product.ticker}`);
        const label = node('label','','Jumlah unit'); const input = node('input','input-field');
        input.type='number'; input.min='1'; input.step='1'; input.value='1'; input.id='multiasset-quantity'; label.htmlFor=input.id;
        panel.append(label,input); const status=node('p'); status.setAttribute('role','status'); panel.append(status);
        flowButton(panel,'Tinjau transaksi',()=>{
            try {
                const request={instrumentId:product.id,buy,quantity:Number(input.value)};
                const quote=JSON.parse(bridge.multiAssetOrder(JSON.stringify(request)));
                commandReview('Review transaksi multi-aset',quote,[['Produk',product.name],['Jumlah',request.quantity],['Harga / unit',formatMoney(quote.unitPrice)],['Biaya',formatMoney(quote.fee)],['Perubahan kas',formatMoney(quote.cashChange)]],
                    requestId=>bridge.multiAssetOrder(JSON.stringify(Object.assign({},request,{requestId,quoteVersion:quote.quoteVersion}))),loadAsetView);
            } catch(error) { status.textContent=error.message; }
        });
    }

    function bridgeResult(method, ...args) {
        if (typeof bridge[method] !== 'function') throw new Error('Fitur belum dapat dihubungkan. Buka kembali aplikasi.');
        const result = JSON.parse(bridge[method](...args));
        if (!result || result.success === false) throw new Error((result && result.message) || 'Permintaan belum terkonfirmasi');
        return result;
    }

    function instrumentSelect(panel, label, id, multiple = false) {
        const caption = node('label', '', label); caption.htmlFor = id;
        const select = node('select', 'input-field'); select.id = id; select.multiple = multiple;
        const stocks = JSON.parse(bridge.getInstruments('', '', null, true));
        stocks.forEach(stock => {
            const option = node('option', '', `${stock.ticker} · ${stock.exchange} · ${stock.name}`);
            option.value = stock.id; select.append(option);
        });
        if (multiple) { select.size = 5; flowText(panel, 'Pilihan', 'Pilih 2–4 instrumen. Gunakan Ctrl/Command di desktop untuk memilih lebih dari satu.'); }
        panel.append(caption, select); return select;
    }

    function openResearchTerminal() {
        const panel = showFlow('Terminal riset');
        const label = node('label', '', 'Jenis riset'); label.htmlFor = 'terminal-kind';
        const kind = node('select', 'input-field'); kind.id = 'terminal-kind';
        [['inbox','Kotak masuk analis'],['consensus','Konsensus analis · Pro'],['comparison','Bandingkan instrumen · Pro'],['alternative','Data alternatif · Enterprise']].forEach(([value,title]) => {
            const option = node('option','',title); option.value=value; kind.append(option);
        });
        panel.append(label,kind);
        const selection = node('section'); panel.append(selection);
        let selected;
        const changeKind = () => {
            selection.replaceChildren(); selected = null;
            if (kind.value !== 'inbox') selected = instrumentSelect(selection, kind.value === 'comparison' ? 'Instrumen pembanding' : 'Instrumen', 'terminal-instruments', kind.value === 'comparison');
        };
        kind.onchange=changeKind; changeKind();
        const status=node('p'); status.setAttribute('role','status');
        const results=node('section');
        flowButton(panel,'Tampilkan riset',()=>{
            status.textContent='Memuat…'; results.replaceChildren();
            try {
                const request={kind:kind.value};
                if (selected && kind.value === 'comparison') request.instrumentIds=Array.from(selected.selectedOptions, option=>option.value);
                else if (selected) request.instrumentId=selected.value;
                const data=bridgeResult('researchQuery',JSON.stringify(request));
                status.textContent='';
                flowText(results,'Waktu data',new Date(data.asOf).toLocaleString('id-ID',{timeZone:'Asia/Jakarta'}));
                if (data.source) flowText(results,'Sumber',data.source);
                if (kind.value === 'inbox') {
                    if (!(data.messages || []).length) emptyState(results,'Belum ada pesan','Laporan analis yang sudah terbit akan muncul di sini.');
                    (data.messages || []).forEach(message=>{
                        const card=node('article','card'); results.append(card);
                        card.append(node('h2','',`${message.ticker} · ${message.analyst}`));
                        flowText(card,'Perusahaan riset',message.firm); flowText(card,'Opini',message.recommendation);
                        flowText(card,'Target',`${message.currency} ${message.targetPrice}`);
                        flowText(card,'Terbit',new Date(message.publishedAt).toLocaleString('id-ID',{timeZone:'Asia/Jakarta'}));
                        card.append(node('p','article-copy',message.thesis));
                    });
                } else if (kind.value === 'comparison') {
                    (data.instruments || []).forEach(item=>{
                        const card=node('article','card'); results.append(card); card.append(node('h2','',item.name));
                        flowText(card,'Instrumen',item.instrumentId); flowText(card,'Sektor',item.sector);
                        flowText(card,'Harga',`${item.currency} ${item.price}`); flowText(card,'Perubahan sesi',`${Number(item.dailyChangePercent).toFixed(2)}%`);
                        flowText(card,'Volume sesi',item.sessionVolume); flowText(card,'Jumlah opini',item.consensus.sampleCount);
                        flowText(card,'Target rata-rata',item.consensus.meanTarget == null ? 'Belum tersedia' : `${item.currency} ${item.consensus.meanTarget}`);
                    });
                } else if (kind.value === 'consensus') {
                    flowText(results,'Jumlah opini',data.sampleCount);
                    [['meanTarget','Target rata-rata'],['medianTarget','Target median'],['minTarget','Target terendah'],['maxTarget','Target tertinggi']].forEach(([key,title])=>flowText(results,title,data[key] == null ? 'Belum tersedia' : `${data.currency} ${data[key]}`));
                    flowText(results,'Sebaran opini',`Beli ${data.buy} · Tahan ${data.hold} · Jual ${data.sell}`);
                    (data.reports || []).forEach(report=>flowText(results,`${report.analyst} · ${report.firm}`,`${report.recommendation} · ${report.currency} ${report.targetPrice}`));
                } else {
                    flowText(results,'Jendela berita',`${data.observationWindowDays} hari game`); flowText(results,'Jumlah sumber',data.sampleCount);
                    flowText(results,'Sentimen berita',data.newsSentiment); flowText(results,'Volume sesi',data.observedSessionVolume);
                    flowText(results,'Perubahan harga',`${Number(data.priceChangePercent).toFixed(2)}%`);
                    (data.sources || []).forEach(source=>flowText(results,new Date(source.publishedAt).toLocaleDateString('id-ID',{timeZone:'Asia/Jakarta'}),source.headline));
                }
                if (data.uncertainty) results.append(node('p','notice',data.uncertainty));
            } catch(error) { status.textContent=error.message; }
        });
        flowButton(panel,'Lihat paket terminal',openResearchTiersModal);
        panel.append(status,results);
    }

    function openPerformanceReport() {
        const panel=showFlow('Hasil investasi & arus kas');
        try {
            const data=bridgeResult('getPerformanceReport');
            [['weekly','Minggu berjalan'],['run','Perjalanan saat ini']].forEach(([key,title])=>{
                const period=data[key]; if (!period) return;
                const card=node('section','card'); panel.append(card); card.append(node('h2','',title));
                flowText(card,'Awal periode',new Date(period.startTimestamp).toLocaleString('id-ID',{timeZone:'Asia/Jakarta'}));
                flowText(card,'Kekayaan awal',formatMoney(period.openingRaw)); flowText(card,'Kekayaan akhir',formatMoney(period.closingRaw));
                flowText(card,'Arus noninvestasi',formatMoney(period.nonInvestmentFlowsRaw)); flowText(card,'Hasil investasi',formatMoney(period.investmentResultRaw));
                flowText(card,'Return',period.returnAvailable ? `${Number(period.returnPercent).toFixed(2)}%` : 'Belum tersedia');
                flowText(card,'Metode','Modified Dietz'); if (!period.returnAvailable) flowText(card,'Alasan',period.reason);
            });
            const history=data.weeklyHistory || [];
            if (history.length) {
                const archived=node('details','card'); archived.append(node('summary','','Riwayat minggu selesai')); panel.append(archived);
                history.slice().reverse().forEach(period=>flowText(archived,new Date(period.endTimestamp).toLocaleDateString('id-ID',{timeZone:'Asia/Jakarta'}),period.returnAvailable ? `${Number(period.returnPercent).toFixed(2)}%` : 'Return tidak tersedia'));
            }
        } catch(error) { flowText(panel,'Laporan tidak dapat dimuat',error.message); }
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
        pageHeading(view, 'Lainnya & Riset', 'PROFIL, MISI, LIGA & TERMINAL');
        const hub = node('div','quick-grid');
        [['Profil Saya',openPlayerProfileModal],['Misi Periodik',openPeriodicMissionsModal],['Liga & Rival',openLeagueAndRivalModal],['Galeri Koleksi',openCollectionsGalleryModal],['Riwayat Transaksi',openTransactionHistoryModal]].forEach(([label,open])=>{
            const button=flowButton(hub,label,()=>open()); button.className='shortcut';
        });
        view.append(hub);
        appendPrestigeSection(view);
        flowButton(view,'Terminal: analis, konsensus & perbandingan',openResearchTerminal);
        flowButton(view,'Hasil investasi & arus kas',openPerformanceReport);
        flowButton(view, 'Lab strategi & sandbox what-if', openStrategyLabModal);
        flowButton(view, 'Replay historis', openHistoricalReplayModal);
        flowButton(view, 'Kartu Booster Riset R&D', openResearchBoostersModal);
        flowButton(view, 'Pohon Riset Finansial Tech Tree', openTechTreeModal);

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
                const topRow = node('div', 'section-heading');
                topRow.style.margin = '0 0 6px 0';
                topRow.append(
                    node('span','eyebrow',`${n.categoryLabel}${n.relatedTicker ? ' · '+n.relatedTicker : ''}`),
                    node('span', 'change-pill', n.publishedAt == null ? 'Waktu belum tersedia' : new Date(n.publishedAt).toLocaleString('id-ID', {timeZone:'Asia/Jakarta'}))
                );
                const h3 = node('h3','',n.headline);
                h3.style.margin = '4px 0 8px';
                const impactRow = node('div');
                impactRow.className = 'flex-row';
                const impactBadge = node('span', 'change-pill', n.relatedTicker ? `Emiten: ${n.relatedTicker}` : n.categoryLabel);
                impactRow.append(impactBadge);
                card.append(topRow, h3, impactRow);
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
                    h.textContent = `${a.ticker} · Rekomendasi: ${a.recommendationLabel} (${a.recommendation})`;
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
                    flowText(card, 'Kategori', `${c.categoryLabel}${c.relatedTicker ? ' · ' + c.relatedTicker : ''}`);
                    flowText(card, 'Keterangan', c.details);
                    calSection.append(card);
                });
            }
            view.append(calSection);

        } catch (e) { flowText(view, 'Gagal memuat feed riset', e.message); }
    }

    function appendPrestigeSection(view) {
            if (bridge.getPrestigeStatus) {
                try {
                    const presRes = JSON.parse(bridge.getPrestigeStatus());
                    const presSection = document.createElement('section');
                    presSection.className = 'card';
                    const presHeader = document.createElement('div');
                    presHeader.className = 'section-heading';
                    presHeader.append(node('h2', 'card-title', 'Era baru'), node('span', presRes.eligible ? 'badge-owned' : 'badge-pledged', presRes.eligible ? 'Memenuhi syarat' : 'Belum terbuka'));
                    presSection.append(presHeader);

                    flowText(presSection, 'Pangkat yang diperlukan', presRes.requiredRank);
                    flowText(presSection, 'Hari perjalanan', presRes.runDays);
                    flowText(presSection, 'Manfaat era baru', presRes.prestigeBonus);

                    const btnAction = document.createElement('button');
                    btnAction.className = presRes.eligible ? 'btn-primary' : 'btn-secondary';
                    btnAction.style.marginTop = '10px';
                    btnAction.textContent = presRes.eligible ? 'Tinjau era baru' : 'Lihat syarat & progres';
                    btnAction.onclick = () => {
                        const pPanel = showFlow(presRes.eligible ? 'Tinjauan Era Baru (Prestige)' : 'Persyaratan Era Baru');
                        flowText(pPanel, 'Era baru', 'Tinjau syarat dan perubahan progres sebelum memulai perjalanan baru.');

                        const checklistCard = document.createElement('section');
                        checklistCard.className = 'card';
                        checklistCard.innerHTML = '<h2 class="card-title">Checklist Persyaratan</h2>';

                        const blockersList = presRes.blockers || [];
                        if (blockersList.length === 0) {
                            checklistCard.append(node('p', 'notice', presRes.eligible ? 'Seluruh persyaratan telah terpenuhi.' : 'Rincian persyaratan belum tersedia.'));
                        } else {
                            blockersList.forEach(b => {
                                const bRow = document.createElement('div');
                                bRow.className = 'data-row';
                                bRow.append(node('span', 'negative', 'Belum terpenuhi'), node('strong', '', b));
                                checklistCard.appendChild(bRow);
                            });
                        }
                        pPanel.append(checklistCard);

                        if (presRes.eligible) {
                            flowText(pPanel, 'Yang direset', presRes.reset || 'Kas, posisi, bisnis, XP, dan karier perjalanan.');
                            flowText(pPanel, 'Yang dipertahankan', presRes.retained || 'Identitas, preferensi, statistik lifetime, riwayat prestige.');
                            flowText(pPanel, 'Proteksi data', 'Game akan membuat backup aman terlebih dahulu.');

                            const actBtn = document.createElement('button');
                            actBtn.className = 'btn-primary';
                            actBtn.style.marginTop = '12px';
                            actBtn.textContent = 'Konfirmasi & Mulai Era Baru';
                            const prestigeRunId = presRes.runId;
                            actBtn.onclick = () => {
                                actBtn.disabled = true;
                                actBtn.textContent = 'Memproses...';
                                try {
                                    const reqId = 'PRESTIGE-' + Date.now();
                                    const confirmRes = JSON.parse(bridge.confirmPrestige(JSON.stringify({ requestId: reqId, runId: prestigeRunId })));
                                    if (confirmRes && confirmRes.success !== false) {
                                        showGameToast('success', 'Era baru dimulai', confirmRes.message || 'Progres telah diperbarui.');
                                        setTimeout(() => { closeFlow(); refreshSnapshot(); }, 1500);
                                    } else {
                                        actBtn.disabled = false;
                                        actBtn.textContent = 'Konfirmasi & Mulai Era Baru';
                                        flowText(pPanel, 'Gagal', confirmRes.message || 'Tidak dapat memulai era baru');
                                    }
                                } catch (e) {
                                    actBtn.disabled = false;
                                    actBtn.textContent = 'Konfirmasi & Mulai Era Baru';
                                    flowText(pPanel, 'Error', e.message);
                                }
                            };
                            pPanel.append(actBtn);
                        }
                    };
                    presSection.append(btnAction);
                    view.append(presSection);
                } catch (presErr) { flowText(view, 'Era baru belum dapat dimuat', presErr.message); }
            }

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
        document.querySelectorAll('body > :not(.flow-page):not(script):not(#toast-container)').forEach(el => el.inert = isolated || !el.matches('.bottom-nav'));
        heading.focus(); return content;
    }
    function closeFlow(recovered = false) {
        if (!flowPanel) return false;
        if (flowPanel.getAttribute('aria-label') === 'Pemulihan save diperlukan' && recovered !== true) return true;
        activeDetailStock = null;
        activeReceiptRequestId = null;
        activeDetailPositionInstrumentId = null;
        flowPanel.closest('.flow-page').remove(); flowPanel = null;
        document.querySelectorAll('body > :not(script)').forEach(el => el.inert = false);
        if (flowOpener && flowOpener.isConnected) flowOpener.focus({preventScroll:true});
        return true;
    }
    function flowText(panel, label, value) {
        const row = node('div','data-row'); const text = value == null ? 'Belum tersedia' : String(value).replaceAll('\u2014', ' · ');
        if (text.length > 100) row.classList.add('long');
        row.append(node('span','',label),node('strong','',text)); panel.append(row); return row;
    }
    function applyCareerGate(button, feature) {
        const access = latestSnapshot && latestSnapshot.featureAccess && latestSnapshot.featureAccess[feature];
        if (!access || !access.unlocked) {
            button.disabled = true;
            button.textContent += access ? ` · ${access.requiredTitle}` : ' · syarat karier belum tersedia';
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
            flowText(panel, 'Indeks / bursa', `${detail.ticker} · ${detail.name} (${detail.exchange})`);
            flowText(panel, 'Sumber', detail.sourceLabel);
            flowText(panel, 'Nilai', `${detail.value} ${detail.unit}`);
            flowText(panel, 'Waktu game', `${detail.gameDate} ${detail.gameTime}`);
            flowText(panel, 'Metode', detail.method);
            flowText(panel, 'Periode berlaku', detail.validFrom);
            flowText(panel, 'Batas', detail.limitations);
            if (!detail.historyAvailable) flowText(panel, 'Grafik', 'Histori indeks belum tersedia; tidak menampilkan grafik contoh.');
            detail.members.forEach(member => {
                const button = flowButton(panel, `Anggota ${member.instrumentId} · ${member.name}`, () => {
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
        activeDetailStock = stock;
        const panel = showFlow(stock.ticker);
        panel.setAttribute('data-stock-id', stock.id);
        panel.closest('.flow-page')?.setAttribute('data-stock-id', stock.id);
        metricCard(panel, stock.ticker + ' · ' + stock.exchange, stock.currentPrice, stock.name);
        flowText(panel, 'Kategori / sektor', stock.sector);
        panel.append(node('span',Number(stock.dailyChangePercent)>=0?'change-pill positive':'change-pill negative',`${Number(stock.dailyChangePercent)>=0?'+':''}${stock.dailyChangePercent}% · perubahan game`));
        panel.append(node('p','notice',`${latestSnapshot.clock.date} ${latestSnapshot.clock.time} · Harga simulasi · ${stock.lotSize} saham/unit`));

        // Rincian Kepemilikan Pengguna (Referensi Visual 01 & 02)
        const holdingsCard = document.createElement('div');
        holdingsCard.id = 'detail-holdings-card';
        holdingsCard.classList.add('card');
        if (currentAccount && bridge.getPortfolio) {
            try {
                const port = JSON.parse(bridge.getPortfolio(currentAccount.id));
                const myPos = (port.positions || []).find(p => p.instrumentId === stock.id);
                if (myPos) {
                    flowText(holdingsCard, 'Kepemilikan di portofolio', `${myPos.quantity} saham${stock.lotSize > 0 ? ' (' + Math.floor(myPos.quantity / stock.lotSize) + ' lot)' : ''}`);
                    flowText(holdingsCard, 'Nilai posisi', myPos.marketValue);
                } else {
                    holdingsCard.append(node('p', 'notice', 'Belum memiliki posisi pada instrumen ini.'));
                }
            } catch(e) { holdingsCard.append(node('p', 'notice', 'Kepemilikan belum dapat dimuat.')); }
        }
        panel.appendChild(holdingsCard);

        // Chart Container
        let currentTimeframe = 'D1';
        let currentChartType = 'candle';
        let chartTableOpen = false;
        let chartSignature = null;
        const chartWrapper = document.createElement('div');
        chartWrapper.id = 'instrument-chart-section';
        chartWrapper.style.margin = '12px 0';
        panel.appendChild(chartWrapper);

        function renderChart() {
            const focused = chartWrapper.contains(document.activeElement) ? document.activeElement.dataset.chartControl : null;
            const tableScrollLeft = chartWrapper.querySelector('#chart-table')?.scrollLeft || 0;
            if (!bridge.getChartData) {
                chartWrapper.replaceChildren();
                flowText(chartWrapper, 'Grafik', 'Bridge riwayat harga domain belum tersedia.');
                return;
            }
            try {
                const chartData = JSON.parse(bridge.getChartData(stock.id, currentTimeframe));
                if (!chartData.success) throw new Error(chartData.message || 'Data grafik belum tersedia.');
                chartWrapper.querySelector('.chart-warning')?.remove();
                const signature = JSON.stringify([chartData, currentTimeframe, currentChartType]);
                if (chartSignature === signature) return;
                chartSignature = signature;
                chartWrapper.replaceChildren();

                // Chart Controls
                const controls = document.createElement('div');
                controls.className = 'chart-controls';

                const tfButtons = document.createElement('div');
                tfButtons.className = 'timeframe-buttons';
                ['M1', 'M5', 'M15', 'H1', 'D1'].forEach(tf => {
                    const btn = document.createElement('button');
                    btn.textContent = tf;
                    btn.dataset.timeframe = tf;
                    btn.dataset.chartControl = 'timeframe-' + tf;
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
                    btn.style.whiteSpace = 'nowrap';
                    btn.textContent = ct === 'candle' ? 'Candle' : 'Garis';
                    btn.dataset.chartControl = 'type-' + ct;
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
                const bars = chartData.bars || [];
                if (!bars.length) {
                    emptyState(chartWrapper, 'Grafik Belum Tersedia', 'Data grafik untuk periode ' + currentTimeframe + ' belum tersedia. Pilih periode D1 untuk melihat acuan harian atau lakukan transaksi.');
                    return;
                }
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
                tableToggleBtn.textContent = chartTableOpen ? 'Tutup Tabel OHLCV' : 'Tabel OHLCV Alternatif';
                tableToggleBtn.dataset.chartControl = 'toggle-table';
                tableToggleBtn.setAttribute('aria-expanded', String(chartTableOpen));
                tableToggleBtn.setAttribute('aria-controls', 'chart-table');
                const tableContainer = document.createElement('div');
                tableContainer.id = 'chart-table';
                tableContainer.tabIndex = 0;
                tableContainer.dataset.chartControl = 'table';
                tableContainer.setAttribute('aria-label', 'Riwayat OHLCV, gulir horizontal untuk semua kolom');
                tableContainer.style.display = chartTableOpen ? 'block' : 'none';
                tableContainer.style.margin = '8px 0';
                tableContainer.innerHTML = `
                    <table class="depth-table" aria-label="Riwayat OHLCV game">
                        <thead><tr><th>Waktu</th><th>Open</th><th>High</th><th>Low</th><th>Close</th><th>Vol</th></tr></thead>
                        <tbody>${bars.map(b => `<tr><td>${b.time}</td><td>${b.open}</td><td>${b.high}</td><td>${b.low}</td><td>${b.close}</td><td>${b.volume}</td></tr>`).join('')}</tbody>
                    </table>
                `;
                tableToggleBtn.onclick = () => {
                    chartTableOpen = !chartTableOpen;
                    tableContainer.style.display = chartTableOpen ? 'block' : 'none';
                    tableToggleBtn.textContent = chartTableOpen ? 'Tutup Tabel OHLCV' : 'Tabel OHLCV Alternatif';
                    tableToggleBtn.setAttribute('aria-expanded', String(chartTableOpen));
                };
                chartWrapper.appendChild(tableToggleBtn);
                chartWrapper.appendChild(tableContainer);
                tableContainer.scrollLeft = tableScrollLeft;

                const provNotice = document.createElement('div');
                provNotice.className = 'notice flex-row';
                provNotice.innerHTML = `<span>Sumber data: ${chartData.source || 'Simulasi pasar game'}</span><span>Timeframe: ${currentTimeframe}</span>`;
                chartWrapper.appendChild(provNotice);
            } catch (e) {
                chartSignature = null;
                const warning = chartWrapper.querySelector('.chart-warning') || node('p', 'notice chart-warning');
                warning.setAttribute('role', 'alert');
                warning.textContent = 'Pembaruan grafik gagal. Data terakhir dipertahankan. ' + e.message;
                if (!warning.isConnected) chartWrapper.append(warning);
            } finally {
                if (focused) Array.from(chartWrapper.querySelectorAll('[data-chart-control]')).find(control => control.dataset.chartControl === focused)?.focus({preventScroll:true});
            }
        }
        renderChart();
        panel.refreshChart = renderChart;

        // Order Book & Tape
        let book = null;
        if (bridge.getOrderBook) {
            try {
                book = JSON.parse(bridge.getOrderBook(stock.id));
                const statusRow = flowText(panel, 'Status pasar', book ? (book.haltState || 'NORMAL') : 'NORMAL');
                statusRow.id = 'detail-market-status-row';

                const bookSection = document.createElement('div');
                bookSection.innerHTML = `
                    <h3 style="margin: 12px 0 6px; font-size: 0.95rem;">Order Book (10 Tingkat)</h3>
                    <div class="table-scroll" tabindex="0" aria-label="Antrean harga beli dan jual"><table class="depth-table">
                        <thead><tr><th>Bid Qty</th><th>Bid (Klik Jual)</th><th>Ask (Klik Beli)</th><th>Ask Qty</th></tr></thead>
                        <tbody>
                            ${Array.from({ length: Math.max((book.bids || []).length, (book.asks || []).length) }).map((_, i) => {
                                const b = (book.bids || [])[i] || { price: '-', quantity: '-' };
                                const a = (book.asks || [])[i] || { price: '-', quantity: '-' };
                                return `<tr>
                                    <td class="depth-bid">${b.quantity}</td>
                                    <td class="positive clickable-price" data-side="SELL" data-price="${b.price}" style="cursor:pointer;" title="Klik untuk jual pada harga antrean bid">${b.price}</td>
                                    <td class="negative clickable-price" data-side="BUY" data-price="${a.price}" style="cursor:pointer;" title="Klik untuk beli pada harga antrean ask">${a.price}</td>
                                    <td class="depth-ask">${a.quantity}</td>
                                </tr>`;
                            }).join('')}
                        </tbody>
                    </table></div>
                `;
                bookSection.querySelectorAll('.clickable-price').forEach(cell => {
                    cell.addEventListener('click', () => {
                        const rawPrice = cell.dataset.price;
                        if (rawPrice && rawPrice !== '-') {
                            const side = cell.dataset.side || 'BUY';
                            openOrderTicket(stock, side);
                            const cleanP = rawPrice.replace(/[^0-9.]/g, '');
                            if (cleanP) inputOrderLimit.value = cleanP;
                            updateOrderEstimate();
                        }
                    });
                });
                panel.appendChild(bookSection);

                // Tape (Recent Trades)
                const tapeSection = document.createElement('div');
                tapeSection.id = 'detail-tape-section';
                renderTape(tapeSection, book ? book.recentTrades : []);
                panel.appendChild(tapeSection);
            } catch (error) { flowText(panel, 'Antrean gagal dimuat', error.message); }
        } else flowText(panel, 'Antrean', 'Bridge domain tidak tersedia pada pratinjau ini');

        // Data Fundamental & Profil Emiten (R08)
        let fundamentals = null;
        let companyProfile = null;
        if (bridge.getIssuerFundamentals) {
            try {
                const fRes = JSON.parse(bridge.getIssuerFundamentals(stock.id));
                if (fRes && fRes.success) fundamentals = fRes;
            } catch(e) {}
        }
        if (bridge.getCompanyProfile) {
            try {
                const profRes = JSON.parse(bridge.getCompanyProfile(stock.id));
                if (profRes && profRes.success) companyProfile = profRes;
            } catch(e) {}
        }

        const fundContainer = document.createElement('section');
        fundContainer.id = 'detail-fundamentals-container';
        fundContainer.className = 'card';
        fundContainer.style.marginTop = '12px';

        const fundNav = document.createElement('div');
        const fundTabs = [
            { id: 'ringkasan', label: 'Ringkasan' },
            { id: 'valuasi', label: 'Valuasi & Rasio' },
            { id: 'keuangan', label: 'Laporan Keuangan' },
            { id: 'pemegang', label: 'Pemegang Saham' }
        ];

        const fundContent = document.createElement('div');
        fundContent.id = 'fund-content-box';

        function renderFundTab(tabId) {
            fundNav.querySelectorAll('button').forEach(b => {
                const active = b.dataset.tabId === tabId;
                b.className = 'subtab-pill' + (active ? ' active' : '');
                b.setAttribute('aria-pressed', String(active));
            });
            fundContent.replaceChildren();

            if (tabId === 'ringkasan') {
                if (companyProfile) {
                    const desc = document.createElement('p');
                    desc.className = 'article-copy';
                    desc.textContent = companyProfile.description;
                    fundContent.appendChild(desc);
                    flowText(fundContent, 'Nama resmi', companyProfile.name);
                    flowText(fundContent, 'Bursa & negara', `${companyProfile.exchange} · ${companyProfile.country}`);
                    flowText(fundContent, 'Sektor', companyProfile.sector);
                    flowText(fundContent, 'Mata uang', companyProfile.currency);
                    if (companyProfile.website) {
                        const webRow = document.createElement('div');
                        webRow.className = 'data-row';
                        webRow.append(node('span', '', 'Situs resmi'));
                        const website = node('a', '', companyProfile.website);
                        if (/^https?:\/\//i.test(companyProfile.website)) {
                            website.href = companyProfile.website; website.target = '_blank'; website.rel = 'noopener';
                        }
                        webRow.append(website);
                        fundContent.appendChild(webRow);
                    }
                }
            } else if (tabId === 'valuasi' && fundamentals) {
                const v = fundamentals.valuationRatios || {};
                flowText(fundContent, 'Kapitalisasi pasar (market cap)', formatMoney(v.marketCap, fundamentals.currency));
                flowText(fundContent, 'Laba per saham (EPS)', formatMoney(v.eps, fundamentals.currency, 2));
                flowText(fundContent, 'Price-to-Earnings Ratio (PER)', v.per || 'N/A');
                flowText(fundContent, 'Price-to-Book Value (PBV)', v.pbv || 'N/A');
                flowText(fundContent, 'Return on Equity (ROE)', v.roe || 'N/A');
                flowText(fundContent, 'Return on Assets (ROA)', v.roa || 'N/A');
                flowText(fundContent, 'Debt-to-Equity Ratio (DER)', v.der || 'N/A');
                flowText(fundContent, 'Dividen per saham', formatMoney(v.dividendPerShare, fundamentals.currency, 2));
                flowText(fundContent, 'Rasio pembagian dividen', v.dividendPayoutRatio);
                flowText(fundContent, 'Dividend yield', v.dividendYield);
            } else if (tabId === 'keuangan' && fundamentals) {
                const bs = fundamentals.balanceSheet || {};
                const is = fundamentals.incomeStatement || {};
                const cf = fundamentals.cashFlow || {};

                const hNeraca = document.createElement('h4');
                hNeraca.className = 'card-title';
                hNeraca.textContent = '1. Neraca Keuangan (Aset = Liabilitas + Ekuitas)';
                fundContent.appendChild(hNeraca);
                flowText(fundContent, 'Total aset', formatMoney(bs.totalAssets, fundamentals.currency));
                flowText(fundContent, 'Total liabilitas (kewajiban)', formatMoney(bs.totalLiabilities, fundamentals.currency));
                flowText(fundContent, 'Total ekuitas bersih', formatMoney(bs.totalEquity, fundamentals.currency));

                const hLaba = document.createElement('h4');
                hLaba.className = 'card-title';
                hLaba.textContent = '2. Laporan Laba Rugi (Pendapatan & Laba Bersih)';
                fundContent.appendChild(hLaba);
                flowText(fundContent, 'Total pendapatan (revenue)', formatMoney(is.totalRevenue, fundamentals.currency));
                flowText(fundContent, 'Laba usaha (operating profit)', formatMoney(is.operatingProfit, fundamentals.currency));
                flowText(fundContent, 'Laba bersih (net profit)', formatMoney(is.netProfit, fundamentals.currency));

                const hArus = document.createElement('h4');
                hArus.className = 'card-title';
                hArus.textContent = '3. Laporan Arus Kas (CFO + CFI + CFF)';
                fundContent.appendChild(hArus);
                flowText(fundContent, 'Arus kas operasional (CFO)', formatMoney(cf.cfo, fundamentals.currency));
                flowText(fundContent, 'Arus kas investasi capex (CFI)', formatMoney(cf.cfi, fundamentals.currency));
                flowText(fundContent, 'Arus kas pendanaan (CFF)', formatMoney(cf.cff, fundamentals.currency));
                flowText(fundContent, 'Arus kas bersih', formatMoney(cf.netCashflow, fundamentals.currency));
            } else if (tabId === 'pemegang' && fundamentals) {
                const sh = fundamentals.shareStructure || {};
                flowText(fundContent, 'Saham beredar (outstanding)', sh.sharesOutstanding == null ? null : `${Number(sh.sharesOutstanding).toLocaleString('id-ID')} lembar`);
                flowText(fundContent, 'Porsi saham publik (free float)', sh.freeFloatShares == null || sh.freeFloatPercent == null ? null : `${Number(sh.freeFloatShares).toLocaleString('id-ID')} lembar (${sh.freeFloatPercent}%)`);
                flowText(fundContent, 'Jumlah pemegang saham', sh.totalShareholdersCount == null ? null : `${Number(sh.totalShareholdersCount).toLocaleString('id-ID')} investor`);
                flowText(fundContent, 'Struktur pemegang utama', sh.majorShareholders);
            }

            const provNotice = document.createElement('div');
            provNotice.className = 'notice';
            provNotice.textContent = `Sumber identitas: ${fundamentals?.identitySource || companyProfile?.source || 'Belum tersedia'}. Sumber angka: ${fundamentals?.financialSource || 'Belum tersedia'}. Periode: ${fundamentals?.periodType || 'Belum tersedia'}. Angka fundamental berasal dari state simulasi game.`;
            fundContent.appendChild(provNotice);
        }

        fundTabs.forEach(t => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.dataset.tabId = t.id;
            btn.className = 'subtab-pill' + (t.id === 'ringkasan' ? ' active' : '');
            btn.textContent = t.label;
            btn.onclick = () => renderFundTab(t.id);
            fundNav.appendChild(btn);
        });

        fundContainer.append(fundNav, fundContent);
        panel.appendChild(fundContainer);
        renderFundTab('ringkasan');

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
            const suspNotice = node('p', 'notice', 'Pemberitahuan: Perdagangan instrumen ini sedang di-halt / disuspensi bursa. Order baru tidak dapat diproses.');
            suspNotice.id = 'detail-suspension-notice';
            panel.appendChild(suspNotice);
        }
        flowButton(panel, 'Perbarui harga & antrean', () => {
            const stocks = JSON.parse(bridge.getInstruments(stock.ticker, stock.exchange, null, true));
            const fresh = stocks.find(item => item.id === stock.id);
            if (fresh) openInstrumentDetail(fresh); else flowText(panel,'Data belum tersedia','Kembali ke daftar Pasar untuk mencoba lagi.');
        });

        // Dual Actions Dock: JUAL & BELI (Referensi Visual 01 & 02)
        const dock = node('div', 'detail-actions-dock');
        const btnSell = node('button', 'btn-detail-sell', 'JUAL');
        btnSell.setAttribute('aria-label', `Jual ${stock.ticker}`);
        btnSell.disabled = !stock.isActiveTrading || isHalted;
        btnSell.onclick = () => openOrderTicket(stock, 'SELL');

        const btnBuy = node('button', 'btn-detail-buy', 'BELI');
        btnBuy.setAttribute('aria-label', `Beli ${stock.ticker}`);
        const isLocked = Boolean(stock.isSectorLocked);
        btnBuy.disabled = !stock.isActiveTrading || isHalted || isLocked;
        if (isLocked) {
            btnBuy.title = `Sektor ${stock.sector || ''} terkunci. Capai Level ${stock.requiredLevel || 4} untuk membuka transaksi emiten ini.`;
            btnBuy.style.opacity = '0.5';
            btnBuy.textContent = `Terkunci (Lv.${stock.requiredLevel || 4})`;
        }
        btnBuy.onclick = () => {
            if (isLocked) {
                alert(`Sektor ${stock.sector} terkunci. Capai Level ${stock.requiredLevel} untuk membuka transaksi emiten ini.`);
                return;
            }
            openOrderTicket(stock, 'BUY');
        };

        dock.append(btnSell, btnBuy);
        panel.appendChild(dock);
    }
    function showOrderReview(preview, draft) {
        closeOrderTicket();
        orderDraft = draft; orderPreview = preview;
        const reviewCreatedTime = Date.now();
        const panel = showFlow('Tinjau order', true);
        flowText(panel, 'Akun', preview.accountName);
        flowText(panel, 'Mode', modeLabel.textContent);
        flowText(panel, 'Instrumen', `${draft.instrumentId} · ${preview.currency}`);
        flowText(panel, 'Order', `${draft.side === 'BUY' ? 'Beli' : 'Jual'} · ${draft.type} · ${draft.quantity} saham`);
        flowText(panel, 'Harga / pemicu', `${draft.limitPrice || draft.triggerPrice || 'Market sesuai likuiditas'} ${preview.currency} per saham`);
        flowText(panel, 'Estimasi fill / sisa', `${preview.estimatedFillQuantity} / ${preview.remainingQuantity} saham`);
        flowText(panel, 'Harga rata-rata / slippage', `${preview.estimatedVwap == null ? 'Tidak ada fill' : Number(preview.estimatedVwap).toLocaleString('id-ID',{maximumFractionDigits:4})} / ${preview.slippagePercent == null ? 'Belum tersedia' : Number(preview.slippagePercent).toLocaleString('id-ID',{maximumFractionDigits:3})+'%'}`);
        flowText(panel, 'Estimasi fee', preview.estimatedFee);
        flowText(panel, 'Estimasi dampak kas', preview.estimatedCashImpact);
        flowText(panel, 'Kas diperlukan', preview.requiredCash);
        flowText(panel, 'Kas tersedia', preview.availableCash);
        flowText(panel, 'Estimasi berlaku hingga', new Date(reviewCreatedTime + 30000).toLocaleTimeString('id-ID'));
        flowText(panel, 'Batas', preview.warning);
        flowButton(panel, 'Kirim order', event => {
            if (Date.now() > reviewCreatedTime + 30000) {
                flowText(panel, 'Estimasi kedaluwarsa', 'Kembali ke tiket untuk meninjau ulang.'); return;
            }
            event.currentTarget.disabled = true; event.currentTarget.textContent = 'Memproses';
            activeRequestId = draft.requestId;
            try {
                const result = JSON.parse(bridge.submitOrder(JSON.stringify(draft)));
                showOrderReceipt(result);
                refreshSnapshot();
                const filledShares = (result.filledQuantity != null) ? result.filledQuantity : draft.quantity;
                if (result.status === 'FILLED') {
                    showGameToast('success', 'Order Terisi Penuh!', `${draft.side === 'BUY' ? 'Beli' : 'Jual'} ${filledShares} saham ${draft.instrumentId}`, 'ORDER_FILL:' + draft.requestId);
                } else if (result.status === 'PARTIALLY_FILLED') {
                    showGameToast('info', 'Order Terisi Sebagian', `${draft.side === 'BUY' ? 'Beli' : 'Jual'} ${result.filledQuantity} saham ${draft.instrumentId} (sisa ${result.remainingQuantity} saham)`, 'ORDER_PARTIAL:' + draft.requestId + ':' + result.filledQuantity);
                } else if (result.status === 'OPEN') {
                    showGameToast('info', 'Order Masuk Antrean', `${draft.side === 'BUY' ? 'Beli' : 'Jual'} ${draft.quantity} saham ${draft.instrumentId} (${draft.type === 'LIMIT' ? 'Limit ' + (draft.limitPrice || '') : 'Market'})`, 'ORDER_OPEN:' + draft.requestId);
                }
            } catch (error) {
                showOrderReceipt({success:false, status:'UNKNOWN', message:'Status belum terkonfirmasi. Periksa status dengan ID yang sama.'});
                showGameToast('info', 'Status Order Belum Terkonfirmasi', error.message, 'ORDER_ERR:' + draft.requestId);
            }
        });
    }
    function showOrderReceipt(result) {
        activeReceiptRequestId = activeRequestId;
        const panel = showFlow('Status order', true);
        panel.setAttribute('data-receipt-request-id', activeRequestId);
        panel.closest('.flow-page')?.setAttribute('data-receipt-request-id', activeRequestId);
        const statusLabel = formatOrderStatus(result.status || 'DITOLAK');
        const statusRow = flowText(panel, 'Status', statusLabel);
        statusRow.querySelector('strong')?.setAttribute('data-status', result.status || 'DITOLAK');
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
                    showOrderReceipt(JSON.parse(bridge.cancelOrder(activeRequestId)));
                    refreshSnapshot();
                    showGameToast('info', 'Order Dibatalkan', `Sisa ${result.remainingQuantity} saham dibatalkan. Kas tertahan dipulihkan.`, 'ORDER_CANCEL:' + activeRequestId);
                }
            });
        }
    }

    function appendCorporateOffers(panel) {
        const section=node('section','card'); section.append(node('h2','','Rights issue & tender')); panel.append(section);
        try {
            const data=bridgeResult('corporateOfferAction',JSON.stringify({action:'list'}));
            if (!(data.offers || []).length) emptyState(section,'Belum ada penawaran','Penawaran korporasi yang tersedia akan muncul di sini.');
            (data.offers || []).forEach(offer=>{
                const card=node('article','card'); section.append(card);
                const names={RIGHTS_ISSUE:'Rights issue',BUYBACK_TENDER:'Tender buyback',MERGER_TENDER:'Tender merger'};
                card.append(node('h3','',`${names[offer.kind] || offer.kind} · ${offer.instrumentId}`));
                flowText(card,'Status',({UPCOMING:'Belum dibuka',OPEN:'Dibuka',CLOSED:'Ditutup'})[offer.status] || offer.status);
                flowText(card,'Record date',offer.recordDate); flowText(card,'Batas partisipasi',offer.deadline); flowText(card,'Penyelesaian',offer.settlementDate);
                flowText(card,'Hak tersedia',offer.availableShares == null ? offer.entitledShares : offer.availableShares);
                if (offer.kind === 'MERGER_TENDER') flowText(card,'Konversi',`${offer.targetInstrumentId || offer.targetInstrument} · rasio ${offer.exchangeRatio}`);
                else flowText(card,offer.kind === 'RIGHTS_ISSUE' ? 'Harga tebus / saham' : 'Harga tender / saham',`${offer.currency || 'IDR'} ${offer.cashPrice}`);
                flowButton(card,'Lihat penawaran',()=>openCorporateOfferDetail(offer));
            });
        } catch(error) { flowText(section,'Penawaran belum dapat dimuat',error.message); }
    }

    function openCorporateOfferDetail(offer) {
        const panel=showFlow(`Penawaran · ${offer.instrumentId}`);
        flowText(panel,'Jenis',({RIGHTS_ISSUE:'Rights issue',BUYBACK_TENDER:'Tender buyback',MERGER_TENDER:'Tender merger'})[offer.kind] || offer.kind);
        flowText(panel,'Hak tercatat',offer.entitledShares); flowText(panel,'Hak terpakai',offer.usedShares || 0);
        flowText(panel,'Batas partisipasi',offer.deadline); flowText(panel,'Penyelesaian',offer.settlementDate);
        const label=node('label','','Jumlah saham'); label.htmlFor='corporate-shares';
        const input=node('input','input-field'); input.id='corporate-shares'; input.type='number'; input.min='1'; input.step='1'; input.value='1';
        const status=node('p'); status.setAttribute('role','status'); panel.append(label,input,status);
        const review=flowButton(panel,'Tinjau partisipasi',()=>{
            try {
                const request={action:'preview',offerId:offer.id || offer.offerId,shares:Number(input.value)};
                const quote=bridgeResult('corporateOfferAction',JSON.stringify(request));
                commandReview('Review partisipasi korporasi',quote,[['Instrumen',offer.instrumentId],['Jumlah saham',request.shares],['Nilai kas',`${quote.currency || 'IDR'} ${quote.amount || quote.reservedCash || '0'}`],['Penyelesaian',offer.settlementDate]],
                    requestId=>bridge.corporateOfferAction(JSON.stringify(Object.assign({},request,{action:'participate',requestId}))),openCorporateActionModal);
            } catch(error) { status.textContent=error.message; }
        });
        if (offer.status !== 'OPEN' || Number(offer.availableShares) <= 0) { review.disabled=true; status.textContent=offer.status !== 'OPEN' ? 'Partisipasi hanya tersedia selama jendela penawaran terbuka.' : 'Tidak ada hak partisipasi yang tersedia pada penawaran ini.'; }
        const participations=offer.participations || offer.requests || [];
        if (!participations.length) flowText(panel,'Partisipasi','Belum ada permintaan untuk penawaran ini.');
        participations.forEach(receipt=>{
            const card=node('section','card'); panel.append(card);
            flowText(card,'ID permintaan',receipt.requestId); flowText(card,'Jumlah',receipt.shares); flowText(card,'Status',receipt.status);
            if (receipt.status === 'RESERVED' && offer.status === 'OPEN') flowButton(card,'Batalkan partisipasi',()=>{
                try {
                    bridgeResult('corporateOfferAction',JSON.stringify({action:'cancel',requestId:receipt.requestId}));
                    refreshSnapshot(); openCorporateActionModal();
                } catch(error) { status.textContent=error.message; }
            });
        });
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
                        flowText(row, `${act.type} · ${act.ticker}`, `Nilai/rasio: ${act.valueOrRatio} ${act.currency || ''}`);
                        flowText(row, 'Jadwal', `Recording: ${act.recordingDate} | Eksekusi: ${act.executionDate}`);
                        flowText(row, 'Status', act.isExecuted ? 'Sudah dieksekusi' : act.isCancelled ? 'Dibatalkan' : 'Menunggu tanggal eksekusi');
                        corpSection.append(row);
                    });
                }
            } catch (e) { flowText(corpSection, 'Gagal memuat aksi', e.message); }
        }
        panel.append(corpSection);
        appendCorporateOffers(panel);

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
                        h.textContent = `${ag.ticker} · ${ag.title}`;
                        card.append(h);
                        flowText(card, 'Resolusi', ag.resolutionText);
                        flowText(card, 'Recording date', ag.recordingDate);
                        flowText(card, 'Tenggat voting', ag.deadline);
                        flowText(card, 'Hak suara tercatat', ag.entitledShares == null ? 'Belum ditetapkan' : ag.entitledShares);
                        if (ag.closed || ag.isClosed) { flowText(card,'Hasil',ag.result); flowText(card,'Konsekuensi',ag.consequence); }
                        flowText(card, 'Hasil suara sementara', `Setuju: ${ag.votesFor} | Tolak: ${ag.votesAgainst} | Abstain: ${ag.votesAbstain}`);
                        flowText(card, 'Status partisipasi', ag.hasVoted ? 'Sudah memberikan suara' : 'Belum memberikan suara');

                        if (!ag.hasVoted && ag.canVote !== false && !ag.closed && !ag.isClosed && bridge.castShareholderVote) {
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
                    title.textContent = `${ipo.ticker} · ${ipo.companyName}`;
                    card.append(title);
                    flowText(card, 'Harga penawaran', `${ipo.offeringPrice} (1 lot = ${ipo.lotSize} saham)`);
                    flowText(card, 'Saham ditawarkan', `${ipo.sharesOffered.toLocaleString('id-ID')} lembar`);
                    flowText(card, 'Tanggal listing', ipo.listingDate);
                    flowText(card, 'Status penjatahan', ipo.isAllotted ? 'Sudah dijatahkan' : 'Masa penawaran berlangsung');

                    if (!ipo.isAllotted && bridge.orderIpo) {
                        const form = document.createElement('div');
                        form.style.marginTop = '12px';
                        const lotLabel = document.createElement('label');
                        lotLabel.htmlFor = 'ipo-lots-' + ipo.id;
                        lotLabel.textContent = 'Jumlah lot yang dipesan';
                        const lotInput = document.createElement('input');
                        lotInput.id = lotLabel.htmlFor;
                        lotInput.type = 'number';
                        lotInput.min = '1';
                        lotInput.value = '10';
                        lotInput.className = 'input-field';

                        const errText = document.createElement('p');
                        errText.setAttribute('role', 'alert');
                        errText.style.color = 'var(--negative)';

                        lotInput.oninput = () => { card._ipoReqId = null; errText.textContent = ''; };

                        const orderBtn = document.createElement('button');
                        orderBtn.className = 'btn-primary';
                        orderBtn.textContent = 'Pesan saham IPO';
                        orderBtn.onclick = () => {
                            const lots = Number(lotInput.value);
                            if (!Number.isFinite(lots) || lots <= 0) { errText.textContent = 'Isi jumlah lot positif.'; return; }
                            try {
                                if (!card._ipoReqId) card._ipoReqId = 'REQ-IPO-' + Date.now() + '-' + Math.floor(Math.random() * 10000);
                                const res = JSON.parse(bridge.orderIpo(JSON.stringify({ ipoId: ipo.id, lots, requestId: card._ipoReqId })));
                                if (!res.success) throw new Error(res.message);
                                refreshSnapshot();
                                flowText(card, 'Pemesanan berhasil', `${res.message} · Tertahan: ${res.reservedMoney}`);
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
                    h.textContent = `${b.ticker} · ${b.name}`;
                    card.append(h);
                    flowText(card, 'Nilai par', `${b.parValue} (${b.currency})`);
                    flowText(card, 'Kupon tahunan', `${(Number(b.couponRateAnnual) * 100).toFixed(3)}% p.a.`);
                    flowText(card, 'Harga bersih (clean price)', b.cleanPrice);
                    flowText(card, 'Jatuh tempo', b.maturityDate);
                    flowText(card, 'Kepemilikan portofolio', `${b.quantity} unit (Nilai pasar: ${b.marketValue})`);
                    flowText(card, 'Status perdagangan', `${b.status} · ${b.note}`);
                    flowButton(card,'Beli obligasi',()=>openMultiAssetOrder(b,true));
                    flowButton(card,'Jual obligasi',()=>openMultiAssetOrder(b,false));
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
                    h.textContent = `${c.ticker} · ${c.name}`;
                    card.append(h);
                    flowText(card, 'Harga spot per unit', `${c.spotPrice} / ${c.unit}`);
                    flowText(card, 'Kepemilikan portofolio', `${c.quantity} unit (Nilai pasar: ${c.marketValue})`);
                    flowText(card, 'Unit', 'Kepemilikan emas dalam game, tanpa pengiriman fisik.');
                    flowText(card, 'Status perdagangan', `${c.status} · ${c.note}`);
                    flowButton(card,'Beli emas',()=>openMultiAssetOrder(c,true));
                    flowButton(card,'Jual emas',()=>openMultiAssetOrder(c,false));
                    commSection.append(card);
                });
            }
            panel.append(commSection);

            // 3. Deposito Berjangka Perbankan (R10)
            const depSection = document.createElement('section');
            depSection.className = 'card';
            const depTitle = document.createElement('h2');
            depTitle.className = 'card-title';
            depTitle.textContent = 'Deposito Berjangka Perbankan (Time Deposit)';
            depSection.append(depTitle);

            if (bridge.getTimeDepositProducts) {
                try {
                    const products = JSON.parse(bridge.getTimeDepositProducts());
                    const activeDeps = bridge.getActiveTimeDeposits ? JSON.parse(bridge.getActiveTimeDeposits()) : [];

                    // Rincian Deposito Aktif
                    if (activeDeps.length > 0) {
                        const hActive = document.createElement('h3');
                        hActive.className = 'card-title';
                        hActive.textContent = `Deposito Aktif (${activeDeps.length} Penempatan)`;
                        depSection.append(hActive);

                        activeDeps.forEach(d => {
                            const dCard = document.createElement('article');
                            dCard.className = 'card';
                            flowText(dCard, 'Bank penerbit', d.bankName);
                            flowText(dCard, 'Nominal pokok', d.principal);
                            flowText(dCard, 'Suku bunga p.a.', d.annualInterestRate);
                            flowText(dCard, 'Tenor', `${d.tenorDays} hari game`);
                            flowText(dCard, 'Bunga berjalan (Accrued)', d.accruedInterest);
                            flowText(dCard, 'Status', d.isMatured ? 'SUDAH JATUH TEMPO (Bunga Penuh)' : 'Sedang Berjalan');

                            const btnWithdraw = document.createElement('button');
                            btnWithdraw.className = d.isMatured ? 'btn-primary' : 'btn-secondary';
                            btnWithdraw.style.marginTop = '6px';
                            btnWithdraw.textContent = d.isMatured ? 'Cairkan Pokok & Bunga' : 'Tarik Dini (Penalti Bunga 50%)';
                            btnWithdraw.onclick = () => {
                                if (!d.isMatured && !confirm('Penarikan sebelum jatuh tempo akan dikenakan penalti bunga 50%. Lanjutkan penarikan?')) return;
                                try {
                                    const res = JSON.parse(bridge.withdrawTimeDeposit(JSON.stringify({ depositId: d.id })));
                                    if (res.success) {
                                        alert('Pencairan deposito berhasil diproses ke saldo kas IDR Anda.');
                                        refreshSnapshot();
                                        openMultiAssets();
                                    } else {
                                        alert('Gagal mencairkan: ' + res.message);
                                    }
                                } catch (err) { alert('Galat penarikan: ' + err.message); }
                            };
                            dCard.append(btnWithdraw);
                            depSection.append(dCard);
                        });
                    }

                    // Pilihan Produk Penempatan Baru
                    const hProd = document.createElement('h3');
                    hProd.className = 'card-title';
                    hProd.textContent = 'Pilihan Produk Deposito Bank';
                    depSection.append(hProd);

                    products.forEach(p => {
                        const pCard = document.createElement('article');
                        pCard.className = 'card';
                        pCard.style.marginBottom = '8px';
                        const h = document.createElement('h4');
                        h.style.margin = '0 0 6px';
                        h.textContent = `${p.bankName} (${p.tenorDays} Hari)`;
                        pCard.append(h);
                        flowText(pCard, 'Suku bunga tahunan', p.annualInterestRate + ' p.a.');
                        flowText(pCard, 'Minimal penempatan', p.minPlacement);

                        const formBox = document.createElement('div');
                        formBox.className = 'filter-chips';

                        const amtInput = document.createElement('input');
                        amtInput.type = 'number';
                        amtInput.className = 'input-field';
                        amtInput.placeholder = 'Nominal IDR';
                        amtInput.setAttribute('aria-label', `Nominal penempatan ${p.bankName}, ${p.tenorDays} hari, dalam IDR`);
                        amtInput.value = '10000000';

                        const openBtn = document.createElement('button');
                        openBtn.className = 'btn-primary';
                        openBtn.style.padding = '6px 14px';
                        openBtn.textContent = 'Buka Deposito';
                        openBtn.onclick = () => {
                            const val = parseFloat(amtInput.value);
                            if (isNaN(val) || val <= 0) { alert('Nominal penempatan tidak sah'); return; }
                            try {
                                const res = JSON.parse(bridge.openTimeDeposit(JSON.stringify({ productId: p.id, amount: String(val) })));
                                if (res.success) {
                                    alert(`Berhasil membuka deposito di ${p.bankName} sebesar Rp ${val.toLocaleString('id-ID')}!`);
                                    refreshSnapshot();
                                    openMultiAssets();
                                } else {
                                    alert('Gagal membuka deposito: ' + (res.message || 'Periksa saldo kas Anda'));
                                }
                            } catch(err) { alert('Galat penempatan: ' + err.message); }
                        };

                        formBox.append(amtInput, openBtn);
                        pCard.append(formBox);
                        depSection.append(pCard);
                    });

                } catch (e) {
                    flowText(depSection, 'Deposito', 'Layanan deposito belum terhubung: ' + e.message);
                }
            }
            panel.append(depSection);

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
                totalStatus.textContent = `Total bobot: ${total}% · ${total === 100 ? 'Sesuai' : 'Harus tepat 100%'}`;
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
                                rebalResult.textContent = `${res.message} · Status rencana: ${res.planStatus}`;
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
                        flowText(resultBox, 'Pemicu margin call', res.wouldTriggerMarginCall ? 'Ya (rasio ekuitas jatuh di bawah batas likuidasi)' : 'Tidak (portofolio tetap solven)');
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
        const panel=showFlow('Lab strategi & replay keputusan');
        panel.append(node('p','notice','Eksperimen memakai salinan akun terpisah. Transaksi lab tidak mengubah permainan utama.'));
        try {
            const list=bridgeResult('strategyLab',JSON.stringify({action:'list'}));
            const saved=list.sandboxes || [];
            if (currentLabSandboxId && !saved.some(item=>(typeof item === 'string' ? item : item.sandboxId) === currentLabSandboxId)) currentLabSandboxId=null;
            if (!currentLabSandboxId && saved.length) currentLabSandboxId=typeof saved[0] === 'string' ? saved[0] : saved[0].sandboxId;
            if (!currentLabSandboxId) {
                currentLabSandboxId='LAB-' + Date.now();
                bridgeResult('strategyLab',JSON.stringify({action:'create',sandboxId:currentLabSandboxId}));
            }
            const summary=node('section','card'); panel.append(summary);
            const history=node('section','card');
            const status=node('p'); status.setAttribute('role','status');
            const loadHistory=()=>{
                const data=bridgeResult('strategyLab',JSON.stringify({action:'history',sandboxId:currentLabSandboxId}));
                const state=data.state || data;
                summary.replaceChildren(node('h2','','Ringkasan lab'));
                flowText(summary,'ID lab',currentLabSandboxId);
                flowText(summary,'Kas IDR',state.cashIdr); flowText(summary,'Kas USD',state.cashUsd);
                flowText(summary,'Transaksi lab',state.simulatedTradesCount == null ? (data.decisions || []).length : state.simulatedTradesCount);
                flowText(summary,'Hasil terealisasi IDR',state.simulatedRealizedPnL ?? state.realizedNetPnLIdr);
                history.replaceChildren(node('h2','','Riwayat keputusan'));
                if (!(data.decisions || []).length) emptyState(history,'Belum ada keputusan','Transaksi percobaan akan tercatat di sini dan dapat diulang.');
                (data.decisions || []).forEach((decision,i)=>flowText(history,`${i+1}. ${decision.action} ${decision.instrumentId}`,`${decision.shares} saham × ${decision.currency} ${decision.price}`));
            };
            if (saved.length > 1) {
                const label=node('label','','Lab tersimpan');label.htmlFor='lab-saved';
                const select=node('select','input-field');select.id='lab-saved';
                saved.forEach(item=>{const id=typeof item === 'string' ? item : item.sandboxId;const option=node('option','',id);option.value=id;select.append(option);});
                select.value=currentLabSandboxId;select.onchange=()=>{currentLabSandboxId=select.value;loadHistory();};panel.append(label,select);
            }
            flowButton(panel,'Kloning akun ke lab baru',()=>{
                try { currentLabSandboxId='LAB-'+Date.now();bridgeResult('strategyLab',JSON.stringify({action:'create',sandboxId:currentLabSandboxId}));openStrategyLabModal(); }
                catch(error){status.textContent=error.message;}
            });
            const form=node('section','card'); form.append(node('h2','','Transaksi percobaan'));panel.append(form);
            const instrument=instrumentSelect(form,'Instrumen','lab-inst-select');
            const sideLabel=node('label','','Aksi');sideLabel.htmlFor='lab-side-select';
            const side=node('select','input-field');side.id='lab-side-select';
            [['BUY','Beli'],['SELL','Jual']].forEach(([value,title])=>{const option=node('option','',title);option.value=value;side.append(option);});
            const qtyLabel=node('label','','Jumlah saham');qtyLabel.htmlFor='lab-qty-input';
            const qty=node('input','input-field');qty.id='lab-qty-input';qty.type='number';qty.min='1';qty.step='1';qty.value='100';
            const priceLabel=node('label','','Harga percobaan');priceLabel.htmlFor='lab-price-input';
            const price=node('input','input-field');price.id='lab-price-input';price.type='number';price.min='0.01';price.step='any';
            const updatePrice=()=>{const stocks=JSON.parse(bridge.getInstruments('', '', null, true));const stock=stocks.find(item=>item.id===instrument.value);price.value=stock ? stock.currentPriceRaw : '';};
            instrument.onchange=updatePrice;updatePrice();
            form.append(sideLabel,side,qtyLabel,qty,priceLabel,price);
            flowButton(form,'Eksekusi transaksi lab',()=>{
                try {
                    bridgeResult('strategyLab',JSON.stringify({action:'trade',sandboxId:currentLabSandboxId,instrumentId:instrument.value,side:side.value,shares:Number(qty.value),price:price.value}));
                    status.textContent='Transaksi lab tercatat dan tersimpan.';loadHistory();
                }catch(error){status.textContent=error.message;}
            });
            flowButton(panel,'Putar ulang keputusan',()=>{
                try{const result=bridgeResult('strategyLab',JSON.stringify({action:'replay',sandboxId:currentLabSandboxId}));status.textContent=`Keputusan diputar ulang: ${result.replayed == null ? (result.decisions || []).length : result.replayed}.`;loadHistory();}
                catch(error){status.textContent=error.message;}
            });
            flowButton(panel,'Kosongkan keputusan lab',()=>{
                try{bridgeResult('strategyLab',JSON.stringify({action:'reset',sandboxId:currentLabSandboxId}));status.textContent='Lab kembali ke keadaan awal.';loadHistory();}
                catch(error){status.textContent=error.message;}
            });
            panel.append(status,history);loadHistory();
        }catch(error){flowText(panel,'Lab belum dapat dimuat',error.message);}
    }

    function openHistoricalReplayModal() {
        const panel=showFlow('Replay historis data nyata');
        panel.append(node('p','notice','Replay disimpan terpisah. Bar harian lengkap terbuka setelah penutupan model; pergerakan intraday tidak direkonstruksi.'));
        const status=node('p');status.setAttribute('role','status');
        try {
            const data=bridgeResult('getHistoricalDatasets');
            const datasets=data.datasets || [];
            const label=node('label','','Dataset');label.htmlFor='replay-dataset';
            const select=node('select','input-field');select.id='replay-dataset';
            datasets.forEach(dataset=>{const option=node('option','',`${dataset.id} · ${dataset.start}–${dataset.end} · ${dataset.bars} bar`);option.value=dataset.id;select.append(option);});
            const detail=node('section','card');
            const renderDataset=()=>{detail.replaceChildren();const dataset=datasets.find(item=>item.id===select.value);if(!dataset)return;flowText(detail,'Sumber',dataset.source);flowText(detail,'Resolusi',dataset.resolution);flowText(detail,'Model eksekusi',dataset.executionModel || 'Referensi pembukaan harian dengan spread model; bukan order book historis.');};
            select.onchange=renderDataset;renderDataset();panel.append(label,select,detail);
            const capLabel=node('label','','Modal replay · IDR');capLabel.htmlFor='replay-capital';
            const capital=node('select','input-field');capital.id='replay-capital';
            [50000000,25000000,100000000].forEach(value=>{const option=node('option','',formatMoney(value));option.value=String(value);capital.append(option);});
            const consentLabel=node('label','check-row');const consent=node('input');consent.type='checkbox';consent.id='replay-new-confirm';
            consentLabel.append(consent,node('span','','Mulai replay baru dan ganti slot replay sebelumnya. Progres pasar internal tetap disimpan.'));
            panel.append(capLabel,capital,consentLabel);
            const start=flowButton(panel,'Mulai replay baru',()=>{
                if(!consent.checked){status.textContent='Centang persetujuan untuk mengganti slot replay.';return;}
                start.disabled=true;
                try{bridgeResult('startNewGame',JSON.stringify({mode:'HISTORICAL_REPLAY',datasetId:select.value,initialCapital:Number(capital.value)}));refreshSnapshot();closeFlow();switchTab('pasar');}
                catch(error){start.disabled=false;status.textContent=error.message;}
            });
            start.disabled=!datasets.length;
            if(!datasets.length)emptyState(panel,'Dataset belum tersedia','Belum ada dataset dengan sumber dan hak yang terverifikasi.');
            [['HISTORICAL_REPLAY','Lanjutkan replay tersimpan'],['INTERNAL_MARKET','Kembali ke permainan utama']].forEach(([mode,title])=>flowButton(panel,title,()=>{
                try{bridgeResult('resumeSavedMode',mode);refreshSnapshot();closeFlow();switchTab('beranda');}
                catch(error){status.textContent=error.message;}
            }));
        }catch(error){flowText(panel,'Replay belum dapat dimuat',error.message);}
        panel.append(status);
    }

    function openConsolidatedWealthModal() {
        const panel = showFlow('Kekayaan Bersih Konsolidasi');
        flowText(panel, 'Perhitungan kekayaan', 'Total kekayaan = Aset pribadi + Nilai ekuitas anak usaha + Koleksi mewah − Liabilitas (utang margin & pinjaman).');

        if (!bridge.getConsolidatedWealth) {
            flowText(panel, 'Status', 'Bridge kekayaan konsolidasi belum tersedia.');
            return;
        }

        try {
            const w = JSON.parse(bridge.getConsolidatedWealth());
            if (!w.success) throw new Error(w.message || 'Gagal memuat kekayaan');

            metricCard(panel, 'TOTAL KEKAYAAN BERSIH', w.totalNetWorth, 'Aset bruto dikurangi seluruh kewajiban');
            flowButton(panel, 'Tandai tinjauan kesehatan selesai', () => {
                try {
                    const receipt = JSON.parse(bridge.reviewPortfolioHealth());
                    if (!receipt.success) throw new Error(receipt.message);
                    flowText(panel, 'Tinjauan tersimpan', 'Kekayaan dan kewajiban telah ditinjau untuk hari game ini.');
                } catch (error) { flowText(panel, 'Tinjauan belum tersimpan', error.message); }
            });

            const assetCard = document.createElement('section');
            assetCard.className = 'card';
            const aTitle = document.createElement('h2');
            aTitle.className = 'card-title';
            aTitle.textContent = `Aset Bruto (${w.grossAssets})`;
            assetCard.append(aTitle);
            flowText(assetCard, 'Kas pribadi (IDR & USD)', w.components.cash);
            flowText(assetCard, 'Portofolio saham', w.components.securities);
            flowText(assetCard, 'Reksa dana (unit NAB)', w.components.mutualFunds);
            flowText(assetCard, 'Barang koleksi & properti', w.components.collections);
            flowText(assetCard, 'Ekuitas anak usaha operasional', w.components.companiesEquity);
            panel.append(assetCard);

            const liabCard = document.createElement('section');
            liabCard.className = 'card';
            const lTitle = document.createElement('h2');
            lTitle.className = 'card-title';
            lTitle.textContent = `Total Kewajiban / Utang (${w.totalLiabilities})`;
            liabCard.append(lTitle);
            flowText(liabCard, 'Utang fasilitas margin', w.liabilities.marginDebt);
            flowText(liabCard, 'Pokok, bunga & penalti pinjaman', w.liabilities.installmentDebt);
            panel.append(liabCard);

            flowText(panel, 'Rekonsiliasi', '100% terekonsiliasi ke buku besar (double-entry ledger) tanpa double-counting AUM klien.');
        } catch (e) {
            flowText(panel, 'Gagal memuat', e.message);
        }
    }

    function openMyMutualFundsModal() {
        openMutualFundsMarketModal();
    }

    function openMutualFundsMarketModal() {
        const panel = showFlow('Pasar Reksa Dana (Unit/NAB)');
        flowText(panel, 'Jadwal transaksi', 'Cutoff Senin–Jumat pukul 16:00 WIB. Pembatalan sebelum 15:30 WIB. Harga final memakai NAB cutoff.');

        if (!bridge.getMutualFunds) {
            flowText(panel, 'Status', 'Bridge reksa dana belum tersedia.');
            return;
        }

        try {
            const res = JSON.parse(bridge.getMutualFunds());
            if (!res.success) throw new Error(res.message || 'Gagal memuat reksa dana');

            flowText(panel, 'Total nilai kepemilikan saya', res.totalHoldingsValue);

            (res.funds || []).forEach(f => {
                const card = document.createElement('article');
                card.className = 'card';
                card.style.marginTop = '10px';

                const h = document.createElement('h3');
                h.textContent = `${f.name} (${f.typeLabel})`;
                card.append(h);

                flowText(card, 'Manajer Investasi', f.managerName);
                flowText(card, 'Tingkat risiko', f.riskLevel);
                flowText(card, 'NAB per unit', `Rp ${f.navPerUnit}`);
                flowText(card, 'Total dana kelolaan (AUM)', f.totalAum);
                flowText(card, 'Unit saya miliki', `${f.myUnits} unit (${f.myHoldingsValue})`);
                flowText(card, 'Biaya beli / jual', `${f.entryFee} / ${f.exitFee}`);

                const btnRow = document.createElement('div');
                btnRow.style.display = 'flex';
                btnRow.style.gap = '8px';
                btnRow.style.flexWrap = 'wrap';
                btnRow.style.marginTop = '10px';

                const subBtn = document.createElement('button');
                subBtn.className = 'btn-primary';
                subBtn.textContent = 'Beli Unit (Subscribe)';
                subBtn.onclick = () => openSubscribeFundModal(f);

                const redBtn = document.createElement('button');
                redBtn.className = 'btn-secondary';
                redBtn.textContent = 'Cairkan Unit (Redeem)';
                redBtn.onclick = () => openRedeemFundModal(f);

                btnRow.append(subBtn, redBtn);
                card.append(btnRow);
                for (const order of f.orders || []) {
                    flowText(card, order.side === 'SUBSCRIBE' ? 'Pembelian unit' : 'Pencairan unit', order.message);
                    flowText(card, 'Jadwal penyelesaian', new Date(order.expectedSettlement).toLocaleString('id-ID', {timeZone:'Asia/Jakarta'}) + ' WIB');
                    flowText(card, 'Batas pembatalan', new Date(order.cancellationDeadline).toLocaleString('id-ID', {timeZone:'Asia/Jakarta'}) + ' WIB');
                    if (order.status === 'ACCEPTED' || order.status.startsWith('PENDING_')) {
                        const cancel = flowButton(card, 'Batalkan permintaan', () => {
                            try {
                                const receipt = JSON.parse(bridge.cancelFundOrder(JSON.stringify({fundId:f.id, orderId:order.orderId})));
                                if (!receipt.success) throw new Error(receipt.message);
                                refreshSnapshot(); openMutualFundsMarketModal();
                            } catch (error) { flowText(card, 'Pembatalan ditolak', error.message); }
                        });
                        cancel.disabled = res.gameTimestamp >= order.cancellationDeadline;
                        if (cancel.disabled) cancel.textContent = 'Batas pembatalan telah lewat';
                    }
                }
                panel.append(card);
            });
        } catch (e) {
            flowText(panel, 'Gagal memuat', e.message);
        }
    }

    function openSubscribeFundModal(fund) {
        const panel = showFlow(`Beli unit · ${fund.name}`);
        flowText(panel, 'NAB acuan', `Rp ${fund.navPerUnit} per unit (Entry fee: 0.1%)`);

        const amtLabel = document.createElement('label');
        amtLabel.textContent = 'Nominal pembelian (IDR, minimal Rp 10.000)';
        const amtInput = document.createElement('input');
        amtInput.id = 'fund-subscribe-amount'; amtLabel.htmlFor = amtInput.id;
        amtInput.type = 'number';
        amtInput.className = 'input-field';
        amtInput.value = '1000000';
        amtInput.min = '10000';
        amtInput.step = '100000';

        const buyBtn = document.createElement('button');
        buyBtn.className = 'btn-primary';
        buyBtn.style.marginTop = '12px';
        buyBtn.textContent = 'Konfirmasi Pembelian Unit';

        const statusMsg = document.createElement('p');
        statusMsg.style.margin = '8px 0';

        const subscriptionId = 'FUND-SUB-' + crypto.randomUUID();
        buyBtn.onclick = () => {
            try {
                const amt = Number(amtInput.value);
                if (amt < 10000) throw new Error('Minimal pembelian Rp 10.000');
                const req = { fundId: fund.id, amount: amtInput.value, requestId:subscriptionId };
                const res = JSON.parse(bridge.subscribeMutualFund(JSON.stringify(req)));
                if (!res.success) throw new Error(res.message);
                statusMsg.textContent = res.message;
                statusMsg.style.color = 'var(--positive)';
                refreshSnapshot();
                flowButton(panel, 'Lihat antrean reksa dana', () => openMutualFundsMarketModal());
            } catch (err) {
                statusMsg.textContent = 'Gagal: ' + err.message;
                statusMsg.style.color = 'var(--negative)';
            }
        };

        panel.append(amtLabel, amtInput, buyBtn, statusMsg);
    }

    function openRedeemFundModal(fund) {
        const panel = showFlow(`Cairkan unit · ${fund.name}`);
        flowText(panel, 'Unit dimiliki', `${fund.myUnits} unit (${fund.myHoldingsValue})`);
        flowText(panel, 'Biaya pencairan', 'Exit fee: 0.1%');

        const unitLabel = document.createElement('label');
        unitLabel.textContent = 'Jumlah unit yang dicairkan';
        const unitInput = document.createElement('input');
        unitInput.id = 'fund-redeem-units'; unitLabel.htmlFor = unitInput.id;
        unitInput.type = 'number';
        unitInput.className = 'input-field';
        unitInput.value = Number(fund.myUnits) > 0 ? fund.myUnits : '100';
        unitInput.step = '1';

        const redBtn = document.createElement('button');
        redBtn.className = 'btn-primary';
        redBtn.style.marginTop = '12px';
        redBtn.textContent = 'Konfirmasi Pencairan Unit';

        const statusMsg = document.createElement('p');
        statusMsg.style.margin = '8px 0';

        const redemptionId = 'FUND-RED-' + crypto.randomUUID();
        redBtn.onclick = () => {
            try {
                const units = Number(unitInput.value);
                if (units <= 0) throw new Error('Jumlah unit harus lebih dari 0');
                const req = { fundId: fund.id, units: unitInput.value, requestId:redemptionId };
                const res = JSON.parse(bridge.redeemMutualFund(JSON.stringify(req)));
                if (!res.success) throw new Error(res.message);
                statusMsg.textContent = res.message;
                statusMsg.style.color = 'var(--positive)';
                refreshSnapshot();
                flowButton(panel, 'Lihat antrean reksa dana', () => openMutualFundsMarketModal());
            } catch (err) {
                statusMsg.textContent = 'Gagal: ' + err.message;
                statusMsg.style.color = 'var(--negative)';
            }
        };

        panel.append(unitLabel, unitInput, redBtn, statusMsg);
    }

    function openLoansModal() {
        const panel = showFlow('Utang & Pinjaman Tenor');
        flowText(panel, 'Spesifikasi D05', 'Fasilitas pinjaman tenor 7/14/30 hari game dengan bunga harian 0.05% atas sisa pokok dan provisi cair 0.5%.');

        if (!bridge.getLoans || !bridge.getLoanProducts) {
            flowText(panel, 'Status', 'Bridge pinjaman belum tersedia.');
            return;
        }

        try {
            const lRes = JSON.parse(bridge.getLoans());
            flowText(panel, 'Total sisa pokok pinjaman pribadi', lRes.totalPersonalDebt);

            const addBtn = document.createElement('button');
            addBtn.className = 'btn-primary';
            addBtn.style.marginBottom = '12px';
            addBtn.textContent = '+ Ajukan Pinjaman Baru';
            addBtn.onclick = () => openDisburseLoanModal('LOAN-PERS-01');
            panel.append(addBtn);

            const activeLoans = lRes.loans || [];
            if (activeLoans.length === 0) {
                flowText(panel, 'Status pinjaman', 'Belum ada pinjaman aktif.');
            } else {
                activeLoans.forEach(l => {
                    const card = document.createElement('article');
                    card.className = 'card';
                    card.style.marginTop = '10px';

                    const h = document.createElement('h3');
                    h.textContent = `${l.productId === 'LOAN-PERS-01' ? 'Pinjaman Pribadi Multiguna' : 'Kredit Modal Kerja Usaha'} (#${l.id})`;
                    card.append(h);

                    flowText(card, 'Status', l.status);
                    flowText(card, 'Plafon awal', l.originalPrincipal);
                    flowText(card, 'Sisa pokok utang', l.remainingPrincipal);
                    flowText(card, 'Sisa tenor', `${l.remainingDays} dari ${l.tenorDays} hari game`);
                    flowText(card, 'Bunga harian terakumulasi', l.accruedInterest);
                    flowText(card, 'Penalti keterlambatan', l.accruedPenalty);
                    flowText(card, 'Total pelunasan penuh', l.totalPayoff);
                    flowText(card, 'Estimasi angsuran harian', l.dailyEstimate);

                    if (!l.isClosed) {
                        const btnRow = document.createElement('div');
                        btnRow.style.display = 'flex';
                        btnRow.style.flexWrap = 'wrap';
                        btnRow.style.gap = '8px';
                        btnRow.style.marginTop = '10px';

                        const payBtn = document.createElement('button');
                        payBtn.className = 'btn-primary';
                        payBtn.textContent = 'Bayar Cicilan';
                        payBtn.onclick = () => openPayInstallmentModal(l);

                        const prepayBtn = document.createElement('button');
                        prepayBtn.className = 'btn-secondary';
                        prepayBtn.textContent = 'Pelunasan Awal (Prepay)';
                        const prepayRequestId = 'LOAN-PREPAY-' + crypto.randomUUID();
                        prepayBtn.onclick = () => {
                            if (confirm(`Lunasi seluruh sisa pinjaman ${l.totalPayoff}?`)) {
                                try {
                                    const r = JSON.parse(bridge.prepayLoanRequest(JSON.stringify({loanId:l.id, requestId:prepayRequestId})));
                                    if (!r.success) throw new Error(r.message);
                                    showGameToast('success', 'Pelunasan Berhasil', r.message, 'LOAN_PREPAY:' + l.id);
                                    refreshSnapshot();
                                    openLoansModal();
                                } catch (err) { showGameToast('info', 'Pelunasan Gagal', err.message); }
                            }
                        };

                        const restBtn = document.createElement('button');
                        restBtn.className = 'btn-secondary';
                        restBtn.textContent = 'Restrukturisasi Tenor';
                        restBtn.onclick = () => {
                            const addDays = prompt('Masukkan jumlah hari perpanjangan tenor:', '7');
                            if (addDays && Number(addDays) > 0) {
                                try {
                                    const r = JSON.parse(bridge.restructureLoan(JSON.stringify({ loanId: l.id, additionalDays: Number(addDays) })));
                                    if (!r.success) throw new Error(r.message);
                                    showGameToast('success', 'Restrukturisasi Berhasil', r.message, 'LOAN_RESTRUCTURE:' + l.id);
                                    refreshSnapshot();
                                    openLoansModal();
                                } catch (err) { showGameToast('info', 'Restrukturisasi Gagal', err.message); }
                            }
                        };

                        btnRow.append(payBtn, prepayBtn, restBtn);
                        card.append(btnRow);
                    }
                    panel.append(card);
                });
            }
        } catch (e) {
            flowText(panel, 'Gagal memuat pinjaman', e.message);
        }
    }

    function openDisburseLoanModal(defaultProductId, companyId = null) {
        const panel = showFlow('Pengajuan Pinjaman Tenor');
        flowText(panel, 'Ketentuan simulasi', 'Suku bunga harian 0.05% atas sisa pokok, biaya provisi pencairan 0.5%. Pokok utang tetap utuh dan dicicil harian.');

        const pSelectLabel = document.createElement('label');
        pSelectLabel.textContent = 'Jenis produk pembiayaan';
        const pSelect = document.createElement('select');
        pSelect.id = 'loan-product'; pSelectLabel.htmlFor = pSelect.id;
        pSelect.className = 'input-field';
        pSelect.innerHTML = `
            <option value="LOAN-PERS-01" ${defaultProductId === 'LOAN-PERS-01' ? 'selected' : ''}>Pinjaman Pribadi Multiguna (Plafon Rp 1 - 50 Juta)</option>
            <option value="LOAN-CORP-01" ${defaultProductId === 'LOAN-CORP-01' ? 'selected' : ''}>Kredit Modal Kerja Perusahaan (Plafon Rp 5 - 100 Juta)</option>
        `;

        const tenorLabel = document.createElement('label');
        tenorLabel.textContent = 'Pilihan jangka waktu (tenor)';
        const tenorSelect = document.createElement('select');
        tenorSelect.id = 'loan-tenor'; tenorLabel.htmlFor = tenorSelect.id;
        tenorSelect.className = 'input-field';
        tenorSelect.innerHTML = `
            <option value="7">7 Hari Game</option>
            <option value="14" selected>14 Hari Game (Standar)</option>
            <option value="30">30 Hari Game (Ekspansif)</option>
        `;

        const amtLabel = document.createElement('label');
        amtLabel.textContent = 'Plafon pinjaman (IDR, minimal Rp 1.000.000)';
        const amtInput = document.createElement('input');
        amtInput.id = 'loan-amount'; amtLabel.htmlFor = amtInput.id;
        amtInput.type = 'number';
        amtInput.className = 'input-field';
        amtInput.value = '10000000';
        amtInput.step = '1000000';

        const companySection=node('section');
        const companyLabel=node('label','','Perusahaan peminjam');companyLabel.htmlFor='loan-company';
        const companySelect=node('select','input-field');companySelect.id='loan-company';
        try {
            const data=bridgeResult('getCompanies');
            (data.companies || []).forEach(company=>{const option=node('option','',company.name);option.value=company.id;companySelect.append(option);});
            if(companyId)companySelect.value=companyId;
        }catch(error){companySection.append(node('p','notice',error.message));}
        companySection.append(companyLabel,companySelect);
        // Pemilih agunan (hanya untuk pinjaman pribadi)
        const collateralSection = document.createElement('div');
        collateralSection.style.margin = '8px 0';
        const collateralLabel = document.createElement('label');
        collateralLabel.textContent = 'Agunan koleksi (wajib untuk pinjaman pribadi)';
        const collateralSelect = document.createElement('select');
        collateralSelect.id = 'loan-collateral'; collateralLabel.htmlFor = collateralSelect.id;
        collateralSelect.className = 'input-field';
        collateralSelect.innerHTML = '<option value="">Memuat koleksi...</option>';
        collateralSection.append(collateralLabel, collateralSelect);

        const collateralInfo = document.createElement('div');
        collateralInfo.className = 'notice';
        collateralSection.append(collateralInfo);

        function loadCollateralOptions() {
            try {
                const collectionResponse = JSON.parse(bridge.getCollections());
                const collections = Array.isArray(collectionResponse) ? collectionResponse : (collectionResponse.collections || []);
                const eligible = [];
                if (Array.isArray(collections)) {
                    collections.forEach(c => {
                        if (c.isOwned && !c.pledgedLoanId && latestSnapshot && latestSnapshot.personalAccount && c.ownerAccountId === latestSnapshot.personalAccount.id) {
                            eligible.push(c);
                        }
                    });
                }
                collateralSelect.innerHTML = '';
                if (eligible.length === 0) {
                    collateralSelect.innerHTML = '<option value="">Tidak ada koleksi yang dapat dijaminkan</option>';
                    collateralInfo.textContent = 'Beli koleksi melalui Aset → Koleksi untuk dijadikan agunan.';
                } else {
                    collateralSelect.innerHTML = '<option value="">Pilih agunan</option>';
                    eligible.forEach(c => {
                        const opt = document.createElement('option');
                        opt.value = c.id;
                        const valuation = c.currentValuation ? formatMoney(c.currentValuation) : '';
                        opt.textContent = (c.name || c.id) + (valuation ? ' (' + valuation + ')' : '');
                        collateralSelect.append(opt);
                    });
                    collateralInfo.textContent = 'Koleksi yang dijaminkan tidak dapat dijual sampai pinjaman lunas.';
                }
            } catch (e) {
                collateralSelect.innerHTML = '<option value="">Gagal memuat koleksi</option>';
                collateralInfo.textContent = e.message || 'Error';
            }
        }

        // Load kolateral saat buka dan saat ganti produk
        loadCollateralOptions();
        pSelect.onchange = () => {
            const isPersonalProduct = pSelect.value === 'LOAN-PERS-01';
            collateralSection.style.display = isPersonalProduct ? '' : 'none';
        };
        collateralSection.style.display = (defaultProductId === 'LOAN-PERS-01' || !defaultProductId) ? '' : 'none';

        const submitBtn = document.createElement('button');
        submitBtn.className = 'btn-primary';
        submitBtn.style.marginTop = '12px';
        submitBtn.textContent = 'Ajukan & Cairkan Pinjaman';

        const statusMsg = document.createElement('p');
        statusMsg.style.margin = '8px 0';

        submitBtn.onclick = () => {
            try {
                const amt = Number(amtInput.value);
                if (amt < 1000000) throw new Error('Plafon minimal Rp 1.000.000');
                const isPersonal = pSelect.value === 'LOAN-PERS-01';
                const selectedCollateral = isPersonal ? (collateralSelect.value || null) : null;
                if (isPersonal && !selectedCollateral) throw new Error('Pilih agunan koleksi untuk pinjaman pribadi');
                const quoteReq = {
                    productId: pSelect.value,
                    isPersonal: isPersonal,
                    companyId: isPersonal ? null : companySelect.value,
                    principal: String(amt),
                    tenorDays: Number(tenorSelect.value),
                    collateralItemId: selectedCollateral
                };
                // Step 1: Get quote
                if (bridge.getLoanQuote) {
                    const quoteRes = JSON.parse(bridge.getLoanQuote(JSON.stringify(quoteReq)));
                    if (!quoteRes.success) throw new Error(quoteRes.message || 'Quote gagal');
                    if (!quoteRes.eligible) throw new Error('Pokok melampaui batas: maks ' + formatMoney(quoteRes.maxPrincipalRaw));

                    // Show review panel
                    statusMsg.innerHTML = '';
                    const reviewDiv = document.createElement('div');
                    reviewDiv.className = 'card';
                    reviewDiv.style.margin = '8px 0';
                    reviewDiv.innerHTML =
                        '<strong>Review Pinjaman</strong><br>' +
                        'Pokok: ' + formatMoney(quoteRes.principalRaw) + '<br>' +
                        'Biaya pencairan: ' + formatMoney(quoteRes.feeRaw) + '<br>' +
                        'Dana cair bersih: ' + formatMoney(quoteRes.netCashRaw) + '<br>' +
                        'Tenor: ' + quoteRes.tenorDays + ' hari<br>' +
                        'Aturan kelayakan: ' + quoteRes.eligibilityRule;
                    statusMsg.append(reviewDiv);

                    const confirmBtn = document.createElement('button');
                    confirmBtn.className = 'btn-primary';
                    confirmBtn.style.marginTop = '8px';
                    confirmBtn.textContent = 'Konfirmasi & Cairkan';
                    const reviewedRequestId='LOAN-' + Date.now() + '-' + Math.random().toString(36).substring(2,8);
                    const confirmationStatus=node('p');confirmationStatus.setAttribute('role','status');
                    confirmBtn.onclick = () => {
                        confirmBtn.disabled = true;
                        confirmBtn.textContent = 'Memproses...';
                        try {
                            if (pSelect.value !== quoteReq.productId || String(Number(amtInput.value)) !== quoteReq.principal || Number(tenorSelect.value) !== quoteReq.tenorDays || (isPersonal && collateralSelect.value !== quoteReq.collateralItemId) || (!isPersonal && companySelect.value !== quoteReq.companyId)) throw new Error('Input berubah. Tinjau ulang quote sebelum konfirmasi.');
                            const disburseReq = Object.assign({}, quoteReq, {
                                requestId: reviewedRequestId,
                                expectedQuoteVersion: quoteRes.quoteVersion
                            });
                            const disburseRes = JSON.parse(bridge.disburseReviewedLoan(JSON.stringify(disburseReq)));
                            if (!disburseRes.success) throw new Error(disburseRes.message || 'Hasil pencairan belum terkonfirmasi');
                            confirmationStatus.textContent='Pinjaman berhasil dicairkan dan tersimpan.';
                            refreshSnapshot();
                            setTimeout(() => { closeFlow(); openLoansModal(); }, 600);
                        } catch (dErr) {
                            confirmBtn.disabled = false;
                            confirmBtn.textContent = 'Konfirmasi & Cairkan';
                            confirmationStatus.textContent='Gagal: '+dErr.message;
                        }
                    };
                    statusMsg.append(confirmBtn,confirmationStatus);
                } else {
                    throw new Error('Fitur quote pinjaman belum tersedia. Hubungi pengembang.');
                }
            } catch (err) {
                statusMsg.textContent = 'Pencairan gagal: ' + err.message;
                statusMsg.style.color = 'var(--negative)';
            }
        };

        const updateLoanKind=()=>{const personal=pSelect.value==='LOAN-PERS-01';collateralSection.hidden=!personal;companySection.hidden=personal;};
        pSelect.addEventListener('change',updateLoanKind);updateLoanKind();
        panel.append(pSelectLabel, pSelect, companySection, collateralSection, tenorLabel, tenorSelect, amtLabel, amtInput, submitBtn, statusMsg);
    }

    function openPayInstallmentModal(loan) {
        const panel = showFlow(`Bayar cicilan · #${loan.id}`);
        flowText(panel, 'Sisa pokok utang', loan.remainingPrincipal);
        flowText(panel, 'Bunga + penalti tertunggak', `${loan.accruedInterest} + ${loan.accruedPenalty}`);
        flowText(panel, 'Total pelunasan penuh', loan.totalPayoff);
        flowText(panel, 'Estimasi angsuran harian', loan.dailyEstimate);

        const amtLabel = document.createElement('label');
        amtLabel.textContent = 'Jumlah pembayaran cicilan (IDR)';
        const amtInput = document.createElement('input');
        amtInput.id = 'loan-payment-amount'; amtLabel.htmlFor = amtInput.id;
        amtInput.type = 'number';
        amtInput.className = 'input-field';
        amtInput.value = '1000000';
        amtInput.step = '100000';

        const payBtn = document.createElement('button');
        payBtn.className = 'btn-primary';
        payBtn.style.marginTop = '12px';
        payBtn.textContent = 'Setor Pembayaran Cicilan';

        const statusMsg = document.createElement('p');
        statusMsg.style.margin = '8px 0';

        const paymentRequestId = 'LOAN-PAY-' + crypto.randomUUID();
        payBtn.onclick = () => {
            try {
                const amt = Number(amtInput.value);
                if (amt <= 0) throw new Error('Nominal bayar harus lebih dari 0');
                const req = { loanId: loan.id, amount: amtInput.value, requestId:paymentRequestId };
                const res = JSON.parse(bridge.payLoan(JSON.stringify(req)));
                if (!res.success) throw new Error(res.message);
                payBtn.disabled = true;
                amtInput.disabled = true;
                statusMsg.textContent = res.message;
                statusMsg.style.color = 'var(--positive)';
                refreshSnapshot();
                setTimeout(() => { closeFlow(); openLoansModal(); }, 600);
            } catch (err) {
                statusMsg.textContent = 'Pembayaran gagal: ' + err.message;
                statusMsg.style.color = 'var(--negative)';
            }
        };

        panel.append(amtLabel, amtInput, payBtn, statusMsg);
    }

    function openPeriodicMissionsModal(activeTab = 'DAILY') {
        if (!['DAILY', 'WEEKLY', 'CAREER'].includes(activeTab)) activeTab = 'DAILY';
        const panel = showFlow('Pusat Misi & Target');
        flowText(panel, 'Pusat Misi Terpadu', 'Selesaikan target harian, mingguan, dan misi karier untuk memperoleh hadiah modal kas dan reputasi.');

        const navWrap = document.createElement('div');
        navWrap.className = 'mission-tabs-nav';

        const tabs = [
            { id: 'DAILY', label: 'Misi harian' },
            { id: 'WEEKLY', label: 'Misi mingguan' },
            { id: 'CAREER', label: 'Target karier' }
        ];

        tabs.forEach(t => {
            const btn = document.createElement('button');
            btn.className = 'subtab-pill' + (activeTab === t.id ? ' active' : '');
            btn.setAttribute('aria-pressed', String(activeTab === t.id));
            btn.textContent = t.label;
            btn.type = 'button';
            btn.onclick = () => openPeriodicMissionsModal(t.id);
            navWrap.appendChild(btn);
        });
        panel.appendChild(navWrap);

        const contentBox = document.createElement('div');
        contentBox.className = 'mission-content-box';
        panel.appendChild(contentBox);

        try {
            if (activeTab === 'CAREER') {
                const snap = latestSnapshot;
                const missions = (snap && snap.career && snap.career.missions) || [];
                if (!missions.length) {
                    flowText(contentBox, 'Target Karier', 'Belum ada misi karier yang tersedia saat ini.');
                    return;
                }

                // Sort: siap diklaim -> belum selesai -> sudah diklaim
                const sorted = missions.slice().sort((a, b) => {
                    const score = m => (m.completed && !m.claimed) ? 0 : (!m.completed ? 1 : 2);
                    return score(a) - score(b);
                });

                sorted.forEach(m => {
                    const card = document.createElement('article');
                    card.className = 'card mission-card';

                    const isReady = m.completed && !m.claimed;
                    const isClaimed = Boolean(m.claimed);

                    const meta = node('div', 'section-heading');
                    meta.append(node('span', 'eyebrow', 'Target karier'), node('span', isReady ? 'badge-owned' : 'badge', isClaimed ? 'Sudah diklaim' : isReady ? 'Selesai · siap klaim' : 'Dalam proses'));
                    card.append(meta, node('h3', '', m.title), node('p', '', m.description));
                    flowText(card, 'Progres', m.progress || (isReady || isClaimed ? 'Selesai' : 'Belum selesai'));
                    flowText(card, 'Imbalan misi', m.reward);

                    if (isReady) {
                        const claimBtn = document.createElement('button');
                        claimBtn.className = 'btn-primary';
                        claimBtn.style.marginTop = '10px';
                        claimBtn.textContent = 'Klaim hadiah karier';
                        let claiming = false;
                        claimBtn.onclick = () => {
                            if (claiming) return;
                            claiming = true;
                            claimBtn.disabled = true;
                            claimBtn.textContent = 'Mengklaim...';
                            try {
                                const r = JSON.parse(bridge.claimMission(m.id));
                                if (!r.success) throw new Error(r.message);
                                showGameToast('success', 'Misi Karier Selesai!', r.message || 'Hadiah berhasil diklaim');
                                refreshSnapshot();
                                openPeriodicMissionsModal('CAREER');
                            } catch (err) {
                                claiming = false;
                                claimBtn.disabled = false;
                                claimBtn.textContent = 'Klaim hadiah karier';
                                showGameToast('info', 'Klaim Gagal', err.message);
                            }
                        };
                        card.appendChild(claimBtn);
                    }
                    contentBox.appendChild(card);
                });

            } else {
                // DAILY or WEEKLY
                if (!bridge.getPeriodicMissions) {
                    flowText(contentBox, 'Status', 'Bridge misi periodik belum tersedia.');
                    return;
                }
                const mRes = JSON.parse(bridge.getPeriodicMissions());
                if (!mRes.success) throw new Error(mRes.message || 'Gagal memuat misi');

                const allList = mRes.activeMissions || [];
                const filtered = allList.filter(m => (m.period || '').toUpperCase() === activeTab);

                // Header countdown
                const headerNotice = document.createElement('div');
                headerNotice.className = 'card';
                flowText(headerNotice, 'Periode misi', activeTab === 'DAILY' ? 'Harian dalam kalender game' : 'Mingguan dalam kalender game');
                contentBox.appendChild(headerNotice);

                if (!filtered.length) {
                    flowText(contentBox, 'Misi aktif', `Belum ada misi ${activeTab === 'DAILY' ? 'harian' : 'mingguan'} untuk periode ini.`);
                    return;
                }

                // Sort: siap klaim -> belum selesai -> sudah diklaim
                const sorted = filtered.slice().sort((a, b) => {
                    const score = m => (m.completed && !m.claimed) ? 0 : (!m.completed ? 1 : 2);
                    return score(a) - score(b);
                });

                sorted.forEach(m => {
                    const card = document.createElement('article');
                    card.className = 'card mission-card';

                    const isReady = m.completed && !m.claimed;
                    const isClaimed = Boolean(m.claimed);

                    const meta = node('div', 'section-heading');
                    meta.append(node('span', 'eyebrow', m.periodLabel || (activeTab === 'DAILY' ? 'Harian' : 'Mingguan')), node('span', isReady ? 'badge-owned' : 'badge', isClaimed ? 'Sudah diklaim' : isReady ? 'Selesai · siap klaim' : 'Dalam proses'));
                    card.append(meta, node('h3', '', m.title), node('p', '', m.description));
                    flowText(card, 'Progres', m.progress || (m.currentProgress == null || m.targetProgress == null ? null : `${m.currentProgress} / ${m.targetProgress}`));
                    flowText(card, 'Hadiah', m.cashReward == null || m.xpReward == null ? null : `${m.cashReward} + ${m.xpReward}`);

                    if (isReady) {
                        const claimBtn = document.createElement('button');
                        claimBtn.className = 'btn-primary';
                        claimBtn.style.marginTop = '10px';
                        claimBtn.textContent = `Klaim hadiah (${m.cashReward} + ${m.xpReward})`;
                        let claiming = false;
                        claimBtn.onclick = () => {
                            if (claiming) return;
                            claiming = true;
                            claimBtn.disabled = true;
                            claimBtn.textContent = 'Mengklaim...';
                            try {
                                const r = JSON.parse(bridge.claimPeriodicMission(m.id));
                                if (!r.success) throw new Error(r.message);
                                showGameToast('success', 'Misi Selesai!', r.message || 'Hadiah berhasil diklaim', 'MISSION_CLAIM:' + m.id);
                                refreshSnapshot();
                                openPeriodicMissionsModal(activeTab);
                            } catch (err) {
                                claiming = false;
                                claimBtn.disabled = false;
                                claimBtn.textContent = `Klaim hadiah (${m.cashReward} + ${m.xpReward})`;
                                showGameToast('info', 'Klaim Gagal', err.message);
                            }
                        };
                        card.appendChild(claimBtn);
                    }
                    contentBox.appendChild(card);
                });
            }
        } catch (e) {
            flowText(contentBox, 'Gagal memuat misi', e.message);
        }
    }

    function openLeagueAndRivalModal(mode = 'NET_WORTH') {
        if (!['NET_WORTH', 'WEEKLY_RETURN'].includes(mode)) mode = 'NET_WORTH';
        const panel=showFlow('Liga & rival');
        try {
            const rival=bridgeResult('getRivalStatus');
            const card=node('section','card'); panel.append(card);card.append(node('h2','',`Rival: ${rival.rivalName}`));
            flowText(card,'Gaya',`${rival.rivalStyle} · ${rival.rivalTitle}`);
            flowText(card,'Kekayaan rival / kamu',`${rival.rivalNetWorth} / ${rival.playerNetWorth}`);
            flowText(card,'Return rival / kamu',`${rival.rivalWeeklyReturn} / ${rival.playerWeeklyReturn}`);
            flowText(card,'Selisih return',rival.returnDifference);if(rival.summary)card.append(node('p','notice',rival.summary));
            const label=node('label','','Urutkan peringkat');label.htmlFor='league-mode';
            const filter=node('select','input-field');filter.id='league-mode';
            [['NET_WORTH','Total kekayaan'],['WEEKLY_RETURN','Return minggu berjalan']].forEach(([value,title])=>{const option=node('option','',title);option.value=value;filter.append(option);});
            filter.value=mode;filter.onchange=()=>openLeagueAndRivalModal(filter.value);panel.append(label,filter);
            const data=bridgeResult('getLeagueLeaderboard',mode);
            panel.append(node('h2','',`${data.tierLabel} · Minggu ${data.seasonWeek}`));
            metricCard(panel,'Peringkat kamu',data.playerRank == null ? 'Belum tersedia' : `#${data.playerRank}`,data.playerRank == null ? 'Return memerlukan periode dan modal tertimbang yang valid.' : `${data.totalCompetitors} peserta`);
            const lb = data.leaderboard || [];
            if (!lb.length) emptyState(panel, 'Peringkat belum tersedia', 'Belum ada peserta yang dapat ditampilkan untuk periode ini.');
            if (lb.length >= 3) {
                const podium = node('div', 'leaderboard-podium');
                // Order: 2 (left), 1 (center), 3 (right)
                const top3 = [lb[1], lb[0], lb[2]];
                top3.forEach(entry => {
                    const r = entry.rank;
                    const pItem = node('div', `podium-item rank-${r}`);
                    if (entry.isPlayer) pItem.classList.add('player-row');
                    const avatar = node('div', 'podium-avatar');
                    avatar.append(node('span', '', (entry.name || '?').charAt(0)));
                    avatar.append(node('span', 'podium-rank-badge', `#${r}`));
                    pItem.append(avatar, node('div', 'podium-name', entry.name), node('div', 'podium-score', mode === 'WEEKLY_RETURN' ? (entry.weeklyReturn || entry.weeklyReturnPercent || 'Belum tersedia') : entry.netWorth));
                    podium.append(pItem);
                });
                panel.append(podium);
            }
            lb.forEach(entry=>{
                const row=node('section','data-row');if(entry.isPlayer)row.classList.add('player-row');
                const identity=node('div');identity.append(node('strong','',`#${entry.rank} ${entry.name}`),node('small','',`${entry.title} · ${entry.style}`));
                row.append(identity,node('strong','',mode === 'WEEKLY_RETURN' ? (entry.weeklyReturn || entry.weeklyReturnPercent || 'Belum tersedia') : entry.netWorth));panel.append(row);
            });
            flowButton(panel,'Riwayat hasil mingguan',openPerformanceReport);
        }catch(error){flowText(panel,'Liga belum dapat dimuat',error.message);}
    }

    function openMarketBossModal() {
        const panel = showFlow('Duel Bos Taipan Pasar');
        flowText(panel, 'Pertarungan 1-on-1', 'Tantang 4 Bos Taipan legendaris untuk meraih uang hadiah tunai dan gelar prestise tertinggi.');

        if (!bridge.getMarketBosses) {
            flowText(panel, 'Status', 'Layanan duel bos belum tersedia.');
            return;
        }

        try {
            const bosses = JSON.parse(bridge.getMarketBosses());
            bosses.forEach(b => {
                const card = document.createElement('section');
                card.className = 'card boss-card';
                card.style.cssText = 'margin-bottom:12px; border:1px solid rgba(255, 69, 58, 0.35); background:linear-gradient(135deg, rgba(255, 69, 58, 0.08) 0%, rgba(18, 23, 40, 0.95) 100%);';

                const tierColors = {
                    'EASY': '#14F195',
                    'MEDIUM': '#00E5FF',
                    'HARD': '#FFA000',
                    'FINAL': '#FF453A'
                };
                const color = tierColors[b.tier] || '#FF453A';

                card.innerHTML = `
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                        <span class="eyebrow" style="color:${color}; font-weight:700;">TINGKAT: ${b.tierTitle || b.tier}</span>
                        <span class="badge" style="background:${color}22; color:${color}; font-size:0.7rem; font-weight:700; border:1px solid ${color}44;">${b.statusLabel}</span>
                    </div>
                    <h3 style="font-size:1.15rem; color:#FFF; margin:0 0 4px;">${b.name}</h3>
                    <div class="data-row"><span>Target Cuan Bos</span><strong>${b.targetReturn}</strong></div>
                    <div class="data-row"><span>Durasi Tantangan</span><strong>${b.durationSessions} Sesi Bursa</strong></div>
                    <div class="data-row"><span>Performa Bos Saat Ini</span><strong style="color:${color};">${b.bossCurrentReturn}</strong></div>
                    <div class="data-row"><span>Cuan Kamu Saat Ini</span><strong style="color:var(--teal);">${b.playerCurrentReturn}</strong></div>
                    <div class="data-row"><span>Hadiah Kemenangan</span><strong style="color:var(--gold);">${b.rewardCash}</strong></div>
                `;

                if (!b.isDefeated && !b.isActive) {
                    const challengeBtn = document.createElement('button');
                    challengeBtn.className = 'btn-primary';
                    challengeBtn.style.cssText = 'margin-top:10px; background:linear-gradient(135deg, #FF453A 0%, #D70015 100%); color:#FFF;';
                    challengeBtn.textContent = 'Mulai Duel Lawan ' + b.name;
                    challengeBtn.onclick = () => {
                        try {
                            const res = JSON.parse(bridge.startBossDuel(b.bossId));
                            alert(`Duel melawan ${b.name} dimulai! Kalahkan performa return ${b.targetReturn} dalam ${b.durationSessions} sesi bursa.`);
                            refreshSnapshot();
                            openMarketBossModal();
                        } catch(err) { alert('Gagal memulai duel: ' + err.message); }
                    };
                    card.appendChild(challengeBtn);
                } else if (b.isActive) {
                    const activeNotice = document.createElement('p');
                    activeNotice.style.cssText = 'color:#FFA000; font-size:0.8rem; font-weight:600; margin-top:8px;';
                    activeNotice.textContent = `⚔️ Duel aktif berlangsung! Sisa ${b.remainingSessions} sesi bursa.`;
                    card.appendChild(activeNotice);
                }

                panel.appendChild(card);
            });
        } catch(e) {
            flowText(panel, 'Galat', e.message);
        }
    }

    function openTaxAuditModal() {
        const panel = showFlow('Audit Pajak & Kepatuhan Risiko');
        flowText(panel, 'Kepatuhan & Pengawasan Bursa', 'Evaluasi kesehatan struktur modal, eksposur leverage, dan penilaian risiko perpajakan pasar modal.');

        if (!bridge.getTaxAuditStatus) {
            flowText(panel, 'Status', 'Layanan audit risiko belum tersedia.');
            return;
        }

        try {
            const tax = JSON.parse(bridge.getTaxAuditStatus());
            const card = document.createElement('section');
            card.className = 'card';
            card.innerHTML = `
                <h3 style="font-size:1.05rem; color:#FFF; margin-bottom:8px;">Laporan Hasil Audit Kepatuhan</h3>
                <div class="data-row"><span>Peringkat Kepatuhan (Rating)</span><strong style="font-size:1.1rem; color:${tax.complianceRating === 'A' ? 'var(--teal)' : (tax.complianceRating === 'B' ? '#FFA000' : 'var(--negative)')}; font-weight:800;">Grade ${tax.complianceRating}</strong></div>
                <div class="data-row"><span>Rasio Utang terhadap Aset</span><strong>${tax.debtToAssetRatio} (Batas Aman: 40.00%)</strong></div>
                <div class="data-row"><span>Diskon Pajak Dividen</span><strong style="color:var(--teal);">${tax.dividendTaxDiscount}</strong></div>
                <div class="data-row"><span>Status Audit Berjalan</span><strong>${tax.isAuditTriggered ? 'TERKENA AUDIT MENDADAK' : 'Patuh Standar'}</strong></div>
                ${tax.isAuditTriggered ? `<div class="data-row"><span>Denda Penalti Pajak</span><strong style="color:var(--negative);">${tax.penaltyAmount}</strong></div>` : ''}
                <p class="notice" style="margin-top:12px; font-size:0.8rem; line-height:1.4;">${tax.statusMessage}</p>
            `;
            panel.appendChild(card);
        } catch(e) {
            flowText(panel, 'Galat', e.message);
        }
    }

    function openPortfolioInsuranceModal() {
        const panel = showFlow('Asuransi Portofolio (Hedging)');
        flowText(panel, 'Lindung Nilai Modal Saham', 'Lindungi modal portofolio Anda dari risiko kejatuhan pasar ekstrem (> 5%) dengan proteksi penggantian 80% nilai kerugian.');

        if (!bridge.getPortfolioInsuranceStatus) {
            flowText(panel, 'Status', 'Layanan asuransi belum tersedia.');
            return;
        }

        try {
            const ins = JSON.parse(bridge.getPortfolioInsuranceStatus());
            const card = document.createElement('section');
            card.className = 'card';
            if (ins.hasActivePolicy && ins.policy) {
                const pol = ins.policy;
                card.innerHTML = `
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                        <span class="eyebrow" style="color:var(--teal); font-weight:700;">STATUS: POLIS AKTIF</span>
                        <span class="badge" style="background:rgba(20, 241, 149, 0.18); color:var(--teal); font-size:0.75rem; font-weight:700;">Proteksi 80%</span>
                    </div>
                    <div class="data-row"><span>ID Polis</span><strong>${pol.policyId}</strong></div>
                    <div class="data-row"><span>Premi Dibayar</span><strong>${pol.premiumPaid}</strong></div>
                    <div class="data-row"><span>Sisa Masa Berlaku</span><strong>${pol.remainingSessions} Sesi Bursa</strong></div>
                    <p class="notice" style="margin-top:10px; font-size:0.8rem;">Polis akan otomatis mencairkan klaim 80% ganti rugi jika terjadi crash pasar > 5%.</p>
                `;
            } else {
                card.innerHTML = `
                    <h3 style="font-size:1.05rem; color:#FFF; margin-bottom:8px;">Pengajuan Polis Asuransi Baru</h3>
                    <div class="data-row"><span>Cakupan Penggantian</span><strong>80% dari Total Kerugian Crash</strong></div>
                    <div class="data-row"><span>Biaya Premi</span><strong>1.0% dari Nilai Portofolio (Min Rp 500.000)</strong></div>
                    <div class="data-row"><span>Masa Perlindungan</span><strong>5 Sesi Bursa Mendatang</strong></div>
                `;
                const buyPolBtn = document.createElement('button');
                buyPolBtn.className = 'btn-primary';
                buyPolBtn.style.marginTop = '12px';
                buyPolBtn.textContent = 'Beli Polis Asuransi Portofolio (5 Sesi)';
                buyPolBtn.onclick = () => {
                    try {
                        const res = JSON.parse(bridge.purchasePortfolioInsurance(5));
                        if (res.success) {
                            alert('Polis asuransi portofolio lindung nilai 80% berhasil dibeli!');
                            refreshSnapshot();
                            openPortfolioInsuranceModal();
                        } else {
                            alert('Gagal membeli polis: ' + res.message);
                        }
                    } catch(err) { alert('Galat pembelian asuransi: ' + err.message); }
                };
                card.appendChild(buyPolBtn);
            }
            panel.appendChild(card);
        } catch(e) {
            flowText(panel, 'Galat', e.message);
        }
    }

    function executeBailoutAction() {
        if (!confirm('Ajukan penyelamatan darurat & restrukturisasi utang (Turnaround Specialist)? Anda akan menerima suntikan modal darurat Rp 25.000.000 untuk memulihkan likuiditas.')) return;
        try {
            const reqId = 'BAILOUT-' + Date.now();
            const res = JSON.parse(bridge.executeTurnaroundBailout ? bridge.executeTurnaroundBailout(reqId) : '{"success":false,"message":"Layanan belum tersedia"}');
            if (res.success) {
                alert(`Restrukturisasi berhasil! ${res.bailoutRelief} dana talangan darurat telah dikreditkan ke kas pribadi Anda.`);
                refreshSnapshot();
                if (currentTab === 'aset') loadAsetView();
            } else {
                alert('Restrukturisasi belum dapat diproses: ' + (res.message || 'Periksa kondisi keuangan Anda'));
            }
        } catch(e) { alert('Galat restrukturisasi: ' + e.message); }
    }

    function openCustomFundBuilderModal() {
        const panel = showFlow('Meracik Reksa Dana Kustom');
        flowText(panel, 'Rekayasa Portofolio Mandiri', 'Rancang alokasi aset campuran (Saham, Komoditas, Kripto, Kas) dan terbitkan unit reksa dana untuk menarik dana kelolaan nasabah.');

        const card = node('section', 'card');
        card.innerHTML = `
            <h3 style="font-size:1.05rem; color:#FFF; margin-bottom:10px;">Komposisi Alokasi Aset (Total 100%)</h3>
            <label for="fund-name-input">Nama Produk Reksa Dana</label>
            <input id="fund-name-input" class="input-field" type="text" value="Nusantara Racikan Mandiri" style="margin-bottom:12px;">

            <div class="data-row"><span>Saham Ekuitas (%)</span><input id="alloc-eq" class="input-field" type="number" value="40" style="max-width:100px;"></div>
            <div class="data-row"><span>Komoditas Emas (%)</span><input id="alloc-comm" class="input-field" type="number" value="30" style="max-width:100px;"></div>
            <div class="data-row"><span>Aset Kripto (%)</span><input id="alloc-crypto" class="input-field" type="number" value="20" style="max-width:100px;"></div>
            <div class="data-row"><span>Cadangan Kas (%)</span><input id="alloc-cash" class="input-field" type="number" value="10" style="max-width:100px;"></div>

            <div style="margin-top:14px; padding:12px; background:rgba(255,255,255,0.03); border-radius:12px; border:1px solid rgba(255,255,255,0.08);">
                <div style="font-size:0.8rem; color:var(--text-muted); margin-bottom:6px;">DIAGRAM ALOKASI (PIE / BAR PREVIEW)</div>
                <div style="display:flex; height:20px; border-radius:8px; overflow:hidden;">
                    <span style="background:var(--teal); width:40%;" title="Saham 40%"></span>
                    <span style="background:var(--gold); width:30%;" title="Komoditas 30%"></span>
                    <span style="background:#9945FF; width:20%;" title="Kripto 20%"></span>
                    <span style="background:#71819A; width:10%;" title="Kas 10%"></span>
                </div>
                <div style="display:flex; justify-content:space-between; font-size:0.68rem; color:var(--text-muted); margin-top:4px;">
                    <span style="color:var(--teal);">■ Saham 40%</span>
                    <span style="color:var(--gold);">■ Komoditas 30%</span>
                    <span style="color:#C4A1FF;">■ Kripto 20%</span>
                    <span style="color:#A0AEC0;">■ Kas 10%</span>
                </div>
            </div>

            <button id="btn-create-custom-fund" class="btn-primary" style="margin-top:14px;">Terbitkan Reksa Dana Kustom</button>
        `;
        panel.appendChild(card);

        // Daftar produk yang sudah diterbitkan
        if (bridge.getCustomMutualFunds) {
            try {
                const funds = JSON.parse(bridge.getCustomMutualFunds());
                if (funds && funds.length) {
                    const listCard = node('section', 'card');
                    listCard.style.marginTop = '12px';
                    listCard.append(node('h3', '', `Reksa Dana Kustom Terbit (${funds.length} Produk)`));
                    funds.forEach(f => {
                        const row = node('div', 'data-row');
                        row.append(
                            node('div', '', `${f.fundName} (Saham ${f.equityWeightPct}% · Emas ${f.commodityWeightPct}% · Kripto ${f.cryptoWeightPct}% · Kas ${f.cashWeightPct}%)`),
                            node('strong', '', f.totalAum || 'Rp 25.000.000')
                        );
                        listCard.append(row);
                    });
                    panel.appendChild(listCard);
                }
            } catch(e) {}
        }

        setTimeout(() => {
            const btn = document.getElementById('btn-create-custom-fund');
            if (btn) {
                btn.onclick = () => {
                    const name = document.getElementById('fund-name-input').value.trim();
                    const eq = Number(document.getElementById('alloc-eq').value) || 0;
                    const comm = Number(document.getElementById('alloc-comm').value) || 0;
                    const crypto = Number(document.getElementById('alloc-crypto').value) || 0;
                    const cash = Number(document.getElementById('alloc-cash').value) || 0;
                    if (Math.abs(eq + comm + crypto + cash - 100) > 0.1) {
                        alert('Total alokasi harus persis 100%! Saat ini: ' + (eq + comm + crypto + cash) + '%');
                        return;
                    }
                    try {
                        const payload = { fundName: name, equityWeightPct: eq, commodityWeightPct: comm, cryptoWeightPct: crypto, cashWeightPct: cash, aum: "25000000" };
                        const res = JSON.parse(bridge.createCustomMutualFund(JSON.stringify(payload)));
                        alert(`Reksa dana kustom ${res.fundName} berhasil diterbitkan dengan dana kelolaan awal ${res.totalAum}!`);
                        refreshSnapshot();
                        openCustomFundBuilderModal();
                    } catch(err) { alert('Gagal menerbitkan: ' + err.message); }
                };
            }
        }, 100);
    }

    function openTransactionHistoryModal(selectedAccountId = null) {
        const panel = showFlow('Riwayat Transaksi');
        const accId = selectedAccountId || (currentAccount ? currentAccount.id : 'ACC-PERSONAL-001');

        flowText(panel, 'Catatan Mutasi Akun', 'Catatan transaksi perdagangan saham, dividen, biaya, pendapatan, dan pendanaan akun.');

        if (!bridge.getLedger) {
            flowText(panel, 'Status', 'Bridge buku besar belum tersedia.');
            return;
        }

        try {
            if (latestSnapshot && latestSnapshot.tradingAccounts && latestSnapshot.tradingAccounts.length > 1) {
                const accSelect = document.createElement('select');
                accSelect.className = 'input-field';
                accSelect.style.marginBottom = '12px';
                accSelect.setAttribute('aria-label', 'Pilih akun riwayat');
                latestSnapshot.tradingAccounts.forEach(a => {
                    const opt = document.createElement('option');
                    opt.value = a.id;
                    opt.textContent = `Akun: ${a.name} (${a.type === 'CLIENT' ? 'Klien' : 'Pribadi'})`;
                    if (a.id === accId) opt.selected = true;
                    accSelect.appendChild(opt);
                });
                accSelect.onchange = () => openTransactionHistoryModal(accSelect.value);
                panel.appendChild(accSelect);
            }

            const entries = JSON.parse(bridge.getLedger(accId));
            if (!Array.isArray(entries) || !entries.length) {
                const empty = document.createElement('div');
                empty.className = 'empty-state';
                empty.innerHTML = '<strong>Belum Ada Transaksi</strong><p>Semua mutasi finansial akun ini akan tercatat rapi di sini.</p>';
                panel.appendChild(empty);
                return;
            }

            const typeLabels = {
                'INITIAL_CAPITAL': 'Modal Awal',
                'DEPOSIT': 'Setor Dana',
                'WITHDRAWAL': 'Tarik Dana',
                'TRADE_BUY': 'Beli Saham',
                'TRADE_SELL': 'Jual Saham',
                'DIVIDEND': 'Bagi Hasil Dividen',
                'TRANSACTION_FEE': 'Biaya Transaksi / Pajak',
                'MANAGEMENT_FEE': 'Fee Pengelolaan Portofolio',
                'PERFORMANCE_FEE': 'Fee Kinerja / Insentif',
                'EXPENSE': 'Biaya Operasional',
                'SALARY': 'Gaji Karier',
                'BAILOUT': 'Dana Hibah Sponsor',
                'INTEREST': 'Bunga Tabungan / Deposito',
                'LOAN_DISBURSEMENT': 'Pencairan Pinjaman',
                'LOAN_REPAYMENT': 'Pelunasan Pinjaman'
            };

            const listWrap = document.createElement('div');

            entries.slice().reverse().forEach(entry => {
                const card = document.createElement('article');
                card.className = 'card';

                const label = typeLabels[entry.type] || entry.type;
                const isCredit = Boolean(entry.credit);
                const sign = isCredit ? '+' : '−';
                const color = isCredit ? 'var(--positive)' : 'var(--negative)';

                const top = document.createElement('div');
                top.className = 'section-heading';
                top.innerHTML = `
                    <span class="eyebrow">${label}</span>
                    <strong style="color:${color}; font-size:0.95rem;">${sign} ${entry.amount}</strong>
                `;

                const desc = document.createElement('div');
                desc.textContent = entry.description || 'Mutasi akun';

                const bottom = document.createElement('div');
                bottom.className = 'data-row';
                bottom.append(node('span', '', `Saldo: ${entry.balanceAfter}`), node('strong', '', entry.requestId || 'ID transaksi belum tersedia'));

                card.append(top, desc, bottom);
                listWrap.appendChild(card);
            });
            panel.appendChild(listWrap);

        } catch (e) {
            flowText(panel, 'Gagal memuat riwayat', e.message);
        }
    }

    function openPlayerProfileModal(activeSubtab = 'KINERJA') {
        if (!['KINERJA', 'KEUANGAN', 'PENCAPAIAN'].includes(activeSubtab)) activeSubtab = 'KINERJA';
        const panel = showFlow('Profil Saya');
        flowText(panel, 'Identitas & Rekam Jejak', 'Statistik lengkap performa, portofolio finansial, dan pencapaian karier Anda.');

        if (!bridge.getPlayerProfile) {
            flowText(panel, 'Status', 'Bridge profil belum tersedia.');
            return;
        }

        try {
            const p = JSON.parse(bridge.getPlayerProfile());
            const fin = p.financialSummary || {};

            // Header Ringkasan Singkat di Atas
            const headerHero = document.createElement('section');
            headerHero.className = 'card wealth-hero';
            headerHero.append(node('span', 'eyebrow', 'Identitas pemain'), node('h2', '', p.playerName || 'Nama belum tersedia'));
            flowText(headerHero, 'Karier', p.careerRank);
            flowText(headerHero, 'Level', p.currentLevel);
            headerHero.append(node('div', 'eyebrow', 'Kekayaan bersih pribadi'), node('div', 'metric-hero', fin.personalNetWorth || p.netWorth || 'Belum tersedia'));
            panel.append(headerHero);

            // 3 Sub-Tabs Navigasi: Kinerja / Keuangan / Pencapaian
            const navWrap = document.createElement('div');
            navWrap.className = 'profile-subtabs-nav';
            const subtabs = [
                { id: 'KINERJA', label: 'Kinerja' },
                { id: 'KEUANGAN', label: 'Keuangan' },
                { id: 'PENCAPAIAN', label: 'Pencapaian' }
            ];
            subtabs.forEach(tab => {
                const btn = document.createElement('button');
                btn.className = 'subtab-pill' + (activeSubtab === tab.id ? ' active' : '');
                btn.setAttribute('aria-pressed', String(activeSubtab === tab.id));
                btn.textContent = tab.label;
                btn.type = 'button';
                btn.onclick = () => openPlayerProfileModal(tab.id);
                navWrap.appendChild(btn);
            });
            panel.append(navWrap);

            const contentBox = document.createElement('div');
            panel.append(contentBox);

            if (activeSubtab === 'KINERJA') {
                const tradeCard = document.createElement('section');
                tradeCard.className = 'card';
                tradeCard.innerHTML = `<h2 class="card-title">Statistik Trading & Return</h2>`;
                flowText(tradeCard, 'Total order terealisasi', p.totalTrades == null ? null : `${p.totalTrades} transaksi`);
                flowText(tradeCard, 'Transaksi untung / rugi', p.profitableTrades == null || p.lossTrades == null ? null : `${p.profitableTrades} untung · ${p.lossTrades} rugi`);
                flowText(tradeCard, 'Rasio menang (Win Rate)', p.winRate || 'Belum Ada Data');
                flowText(tradeCard, 'Transaksi terbaik', p.bestTradePnL);
                flowText(tradeCard, 'Transaksi terburuk', p.worstTradePnL);

                const weeklyRet = p.performance && p.performance.weekly && p.performance.weekly.returnAvailable ? `${p.performance.weekly.returnPercent >= 0 ? '+' : ''}${Number(p.performance.weekly.returnPercent).toFixed(1)}%` : 'Belum tersedia';
                flowText(tradeCard, 'ROI mingguan', weeklyRet);
                flowButton(tradeCard, 'Lihat Laporan Kinerja Lengkap', openPerformanceReport);
                contentBox.append(tradeCard);

            } else if (activeSubtab === 'KEUANGAN') {
                const finCard = document.createElement('section');
                finCard.className = 'card';
                finCard.innerHTML = `<h2 class="card-title">Portofolio & Struktur Aset/Utang</h2>`;
                flowText(finCard, 'Kas pribadi IDR', fin.personalCashIdr);
                flowText(finCard, 'Kas pribadi USD', fin.personalCashUsd);
                flowText(finCard, 'Portofolio saham (IDR)', fin.securitiesValueIdr);
                flowText(finCard, 'Koleksi aset mewah (IDR)', fin.collectionsValueIdr);
                flowText(finCard, 'Reksa dana NAB (IDR)', fin.mutualFundsValueIdr);
                flowText(finCard, 'Kewajiban utang margin', fin.liabilitiesIdr);
                flowText(finCard, 'Dana kelolaan klien (AUM)', fin.clientAumIdr == null ? null : `${fin.clientAumIdr} (terpisah dari uang pribadi)`);
                flowButton(finCard, 'Rincian Kekayaan Bersih Konsolidasi', openConsolidatedWealthModal);
                contentBox.append(finCard);

            } else if (activeSubtab === 'PENCAPAIAN') {
                const achCard = document.createElement('section');
                achCard.className = 'card';
                achCard.innerHTML = `<h2 class="card-title">Pencapaian & Peringkat Liga</h2>`;
                flowText(achCard, 'Puncak kekayaan tertinggi', p.lifetimePeakNetWorth || p.netWorth);
                flowText(achCard, 'Hari perjalanan era aktif', p.runDaysPlayed == null ? null : `${p.runDaysPlayed} hari`);
                flowText(achCard, 'Total hari bermain', p.lifetimeDaysPlayed == null ? null : `${p.lifetimeDaysPlayed} hari`);
                flowText(achCard, 'Peringkat liga saat ini', p.playerLeagueRank == null ? 'Belum tersedia' : `#${p.playerLeagueRank}`);
                flowButton(achCard, 'Lihat Peringkat & Rival', openLeagueAndRivalModal);
                flowButton(achCard, 'Buka Penghargaan & Medali', openAchievementsModal);
                flowButton(achCard, 'Kustomisasi Avatar & Bingkai Profil', openAvatarCustomizerModal);
                contentBox.append(achCard);

                // Form Preferensi Aksesibilitas
                const prefCard = document.createElement('section');
                prefCard.className = 'card';
                prefCard.style.marginTop = '10px';
                prefCard.innerHTML = `<h2 class="card-title">Pengaturan Identitas & Aksesibilitas</h2>`;

                const nameLabel = document.createElement('label');
                nameLabel.textContent = 'Ubah nama trader';
                const nameInput = document.createElement('input');
                nameInput.id = 'player-name'; nameLabel.htmlFor = nameInput.id;
                nameInput.className = 'input-field';
                nameInput.value = p.playerName || '';

                const textScaleLabel = document.createElement('label');
                textScaleLabel.textContent = 'Ukuran teks tampilan';
                const textScaleSelect = document.createElement('select');
                textScaleSelect.id = 'player-text-size'; textScaleLabel.htmlFor = textScaleSelect.id;
                textScaleSelect.className = 'input-field';
                textScaleSelect.innerHTML = `
                    <option value="100%" ${p.preferences && p.preferences.textSize === '100%' ? 'selected' : ''}>100%</option>
                    <option value="200%" ${p.preferences && p.preferences.textSize === '200%' ? 'selected' : ''}>200%</option>
                `;

                const highContrastLabel = node('label', 'check-row');
                const highContrast = node('input'); highContrast.type = 'checkbox';
                highContrast.checked = Boolean(p.preferences && p.preferences.highContrast);
                highContrastLabel.append(highContrast, node('span', '', 'Kontras tinggi'));

                const reducedMotionLabel = node('label', 'check-row');
                const reducedMotion = node('input'); reducedMotion.type = 'checkbox';
                reducedMotion.checked = Boolean(p.preferences && p.preferences.reducedMotion);
                reducedMotionLabel.append(reducedMotion, node('span', '', 'Kurangi gerakan'));

                const profileStatus = node('p'); profileStatus.setAttribute('role', 'status');
                const saveBtn = document.createElement('button');
                saveBtn.className = 'btn-primary';
                saveBtn.style.marginTop = '12px';
                saveBtn.textContent = 'Simpan Profil & Preferensi';

                saveBtn.onclick = () => {
                    try {
                        const req = {
                            playerName: nameInput.value.trim(),
                            preferences: {
                                textSize: textScaleSelect.value,
                                highContrast: highContrast.checked,
                                reducedMotion: reducedMotion.checked
                            }
                        };
                        const res = bridgeResult('updatePlayerProfile', JSON.stringify(req));
                        applyPlayerPreferences(res.preferences || req.preferences);
                        profileStatus.textContent = 'Profil dan preferensi tersimpan.';
                        refreshSnapshot();
                    } catch (err) {
                        profileStatus.textContent = 'Gagal menyimpan: ' + err.message;
                    }
                };

                prefCard.append(nameLabel, nameInput, textScaleLabel, textScaleSelect, highContrastLabel, reducedMotionLabel, saveBtn, profileStatus);
                contentBox.append(prefCard);
            }

        } catch (e) {
            flowText(panel, 'Gagal memuat profil', e.message);
        }
    }

    function openInvestorArchetypeModal() {
        const panel = showFlow('Gaya Investor (Archetype)');
        flowText(panel, 'Evaluasi Gaya Berbasis Perilaku', 'Menganalisis eksposur aset, tingkat leverage, pola risiko, dan durasi kepemilikan dari aktivitas perdagangan riil Anda.');

        if (!bridge.getInvestorArchetype) {
            flowText(panel, 'Status', 'Bridge gaya investor belum tersedia.');
            return;
        }

        try {
            const a = JSON.parse(bridge.getInvestorArchetype());
            metricCard(panel, a.label || 'Gaya investor', a.confidence || 'Belum tersedia', a.progressText || 'Progres evaluasi belum tersedia');

            const descCard = document.createElement('section');
            descCard.className = 'card';
            descCard.innerHTML = `<h2 class="card-title">Karakteristik & Status Evaluasi</h2>`;
            flowText(descCard, 'Deskripsi gaya', a.description || 'Gaya investasi sedang dievaluasi otomatis tanpa kuis buatan.');
            flowText(descCard, 'Analisis perilaku', a.reasoning || 'Belum cukup aktivitas perdagangan.');
            panel.append(descCard);

            const indCard = document.createElement('section');
            indCard.className = 'card';
            indCard.append(node('h2', 'card-title', 'Indikator yang diamati'));
            const inds = a.observedIndicators || [];
            if (!inds.length) indCard.append(node('p', 'notice', 'Indikator belum tersedia.'));
            inds.forEach(ind => {
                const row = document.createElement('div');
                row.className = 'data-row';
                row.style.padding = '6px 0';
                row.append(node('span', '', ind), node('strong', '', 'Dipantau'));
                indCard.append(row);
            });
            panel.append(indCard);
        } catch (e) {
            flowText(panel, 'Gagal memuat gaya investor', e.message);
        }
    }

    function openCollectionsGalleryModal(initialCategory = 'ALL', initialOwnership = 'ALL') {
        collectionFilterCategory = (typeof initialCategory === 'string') ? initialCategory : 'ALL';
        collectionFilterOwnership = (typeof initialOwnership === 'string') ? initialOwnership : 'ALL';
        const panel = showFlow('Galeri Koleksi Mewah');
        flowText(panel, 'Koleksi Aset Mewah', 'Koleksi kendaraan, properti, permata, seni & jam tangan mewah memberi peningkatan reputasi dan diversifikasi aset.');
        const gallery = document.createElement('div');
        panel.append(gallery);
        renderCollectionsGallery(gallery, true);
    }

    function openRiskModal() {
        const panel = showFlow('Fasilitas margin & risiko');
        const executeRisk = (method, request, section, status) => {
            const controls = [...section.querySelectorAll('button')].map(button => [button, button.disabled]);
            controls.forEach(([button]) => button.disabled = true);
            const uncertain = () => { status.textContent = 'Hasil atau penyimpanan belum terkonfirmasi. Buka kembali layar risiko dan periksa utang serta kewajiban sebelum mencoba lagi.'; };
            let result;
            try { result = JSON.parse(bridge[method](JSON.stringify(request))); }
            catch (error) { uncertain(); return; }
            if (!result.success) {
                if (/penyimpanan|serialisasi|save/i.test(result.message || '')) { uncertain(); return; }
                controls.forEach(([button, disabled]) => button.disabled = disabled);
                status.textContent = result.message || 'Tindakan ditolak domain.'; return;
            }
            refreshSnapshot(); openRiskModal();
            showGameToast('success', 'Risiko diperbarui', result.message);
        };
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
                executeRisk('takeMarginLoan', {currency: curSelect.value, amount: String(val)}, loanSection, errDisplay);
            };

            const btnRepay = document.createElement('button');
            btnRepay.className = 'btn-secondary';
            btnRepay.textContent = 'Lunasi utang margin';
            btnRepay.onclick = () => {
                const val = Number(amtInput.value);
                if (!Number.isFinite(val) || val <= 0) { errDisplay.textContent = 'Isi nominal pelunasan yang positif.'; return; }
                executeRisk('repayMarginLoan', {currency: curSelect.value, amount: String(val)}, loanSection, errDisplay);
            };

            loanSection.append(curLabel, curSelect, amtLabel, amtInput, errDisplay, btnBorrow, btnRepay);
            panel.append(loanSection);

            const shortSection = node('section', 'card');
            shortSection.append(node('h2', '', 'Pinjaman saham & short'));
            shortSection.append(node('p', '', 'Hasil penjualan ditahan sebagai jaminan. Harga beli kembali dan biaya pinjaman dapat menambah kewajiban; ketersediaan saham diperiksa saat eksekusi.'));
            (risk.shortPositions || []).forEach(position => {
                const row = node('section', 'data-row');
                row.append(node('h3', '', `${position.ticker} · ${position.exchange}`));
                flowText(row, 'Saham wajib dikembalikan', position.shares);
                flowText(row, 'Jaminan ditahan', position.collateral);
                flowText(row, 'Kewajiban pada harga saat ini', position.liability);
                flowText(row, 'Laba/rugi belum terealisasi', position.unrealizedPnl);
                flowText(row, 'Biaya pinjaman harian', position.dailyBorrowFee);
                shortSection.append(row);
            });
            if (!(risk.shortPositions || []).length) emptyState(shortSection, 'Tidak ada posisi short', 'Kewajiban dan jaminan posisi aktif akan tercatat di sini.');
            const shortInstrument = instrumentSelect(shortSection, 'Saham untuk short / beli kembali', 'short-instrument');
            const shortLabel = node('label', '', 'Jumlah saham'); shortLabel.htmlFor = 'short-shares';
            const shortShares = node('input', 'input-field'); shortShares.id = 'short-shares'; shortShares.type = 'number'; shortShares.min = '1'; shortShares.step = '1';
            const shortStatus = node('p'); shortStatus.setAttribute('role', 'status');
            shortSection.append(shortLabel, shortShares, shortStatus);
            [['Buka posisi short', 'openShortPosition'], ['Beli kembali saham short', 'closeShortPosition']].forEach(([label, method]) => {
                const button = flowButton(shortSection, label, () => {
                    const shares = Number(shortShares.value), ticker = shortInstrument.value.split(':')[1];
                    if (!ticker || !Number.isSafeInteger(shares) || shares <= 0) { shortStatus.textContent = 'Pilih saham dan isi jumlah saham bulat yang positif.'; shortShares.focus(); return; }
                    if (!window.confirm(`${label}: ${shares} saham ${ticker} pada harga domain saat eksekusi? Perubahan harga dan biaya memengaruhi kas serta jaminan.`)) return;
                    executeRisk(method, {ticker, shares}, shortSection, shortStatus);
                });
                if (method === 'openShortPosition') applyCareerGate(button, 'short');
            });
            panel.append(shortSection);

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
    function openOrderTicket(stock, preselectedSide) {
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
        orderModal.inert = false;
        orderModal.classList.add('active');
        document.querySelectorAll('body > :not(#order-modal):not(script):not(#toast-container)').forEach(node => node.inert = true);
        const accContext = document.getElementById('order-account-context');
        accContext.innerHTML = '';
        if (latestSnapshot && latestSnapshot.tradingAccounts && latestSnapshot.tradingAccounts.length > 1) {
            const accSelect = document.createElement('select');
            accSelect.id = 'order-account-select';
            accSelect.setAttribute('aria-label', 'Akun order');
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
        document.querySelectorAll('.side-btn').forEach(button => {
            const match = preselectedSide && button.dataset.side === preselectedSide;
            button.classList.toggle('active', !!match);
            button.setAttribute('aria-pressed', String(!!match));
            button.style.background = '';
            button.style.color = '';
        });
        document.getElementById('order-error').textContent = '';
        btnCloseModal.focus();
    }

    function closeOrderTicket() {
        orderModal.classList.remove('active');
        document.querySelectorAll('body > :not(#order-modal):not(script)').forEach(node => {
            node.inert = Boolean(flowPanel) && !node.matches('.flow-page, #toast-container') && (flowPanel.closest('.flow-page').classList.contains('isolated') || !node.matches('.bottom-nav'));
        });
        selectedInstrument = null;
        if (ticketOpener && ticketOpener.isConnected) ticketOpener.focus();
    }

    document.addEventListener('keydown', event => {
        if (!orderModal.classList.contains('active')) return;
        if (event.key === 'Escape') {
            event.preventDefault();
            event.stopImmediatePropagation();
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
        if (!selectedInstrument) {
            estTotalDisplay.textContent = 'Biaya dan perkiraan kas akan ditampilkan sebelum order dikirim.';
            return;
        }
        const type = document.getElementById('order-type').value;
        inputOrderLimit.disabled = type === 'MARKET';
        document.getElementById('order-price-label').textContent =
            `${type === 'STOP_LOSS' || type === 'TAKE_PROFIT' ? 'Harga pemicu' : 'Harga limit'} (${selectedInstrument.currency})`;

        const selectedSide = document.querySelector('.side-btn.active');
        const side = selectedSide ? selectedSide.dataset.side : 'BUY';
        const qtyLots = Number(inputOrderQty.value) || 0;
        const price = type === 'MARKET' ? (Number(selectedInstrument.currentPriceRaw) || 0) : (Number(inputOrderLimit.value) || 0);

        if (qtyLots > 0 && price > 0) {
            const lotSize = selectedInstrument.lotSize || 100;
            const gross = price * qtyLots * lotSize;
            const feeRate = 0.0015; // 0.15% komisi broker bursa
            const brokerFee = gross * feeRate;
            const total = side === 'BUY' ? gross + brokerFee : Math.max(0, gross - brokerFee);
            const cur = selectedInstrument.currency || 'IDR';
            estTotalDisplay.innerHTML = `<strong>Total Estimasi: ${cur} ${total.toLocaleString('id-ID', {maximumFractionDigits:2})}</strong><br><small style="color:var(--text-muted);">(Nilai kotor: ${cur} ${gross.toLocaleString('id-ID')} · Komisi broker 0.15%: ${cur} ${brokerFee.toLocaleString('id-ID', {maximumFractionDigits:2})})</small>`;
        } else {
            estTotalDisplay.textContent = 'Biaya dan perkiraan kas akan ditampilkan sebelum order dikirim.';
        }
    }
    document.getElementById('order-type').addEventListener('change', updateOrderEstimate);

    inputOrderQty.addEventListener('input', updateOrderEstimate);
    inputOrderLimit.addEventListener('input', updateOrderEstimate);

    // Tombol Cepat Porsi Modal (25%, 50%, 75%, MAX)
    document.querySelectorAll('.btn-order-pct').forEach(btn => {
        btn.addEventListener('click', () => {
            if (!selectedInstrument) return;
            const pct = Number(btn.dataset.pct) || 100;
            const selectedSide = document.querySelector('.side-btn.active');
            const side = selectedSide ? selectedSide.dataset.side : 'BUY';
            const price = Number(inputOrderLimit.value) || Number(selectedInstrument.currentPriceRaw) || 1;
            const lotSize = selectedInstrument.lotSize || 100;

            if (side === 'BUY') {
                let availCash = 0;
                try {
                    const snap = bridge.getSnapshot ? JSON.parse(bridge.getSnapshot()) : latestSnapshot;
                    if (snap && snap.personalAccount) {
                        availCash = Number(snap.personalAccount.cashBalanceRaw || snap.personalAccount.availableCashRaw || 50000000);
                    }
                } catch(e) { availCash = 50000000; }
                const unitCost = price * lotSize * 1.0015;
                const maxLots = Math.max(1, Math.floor(availCash / unitCost));
                const targetLots = Math.max(1, Math.floor(maxLots * (pct / 100)));
                inputOrderQty.value = targetLots;
            } else {
                let heldShares = 0;
                try {
                    const portRes = bridge.getPortfolio ? JSON.parse(bridge.getPortfolio(currentAccount ? currentAccount.id : '')) : null;
                    if (portRes && portRes.positions) {
                        const pos = portRes.positions.find(p => p.instrumentId === selectedInstrument.id || p.ticker === selectedInstrument.ticker);
                        if (pos) heldShares = Number(pos.quantity) || 0;
                    }
                } catch(e) {}
                const maxLots = Math.max(1, Math.floor(heldShares / lotSize));
                const targetLots = Math.max(1, Math.floor(maxLots * (pct / 100)));
                inputOrderQty.value = targetLots;
            }
            updateOrderEstimate();
        });
    });

    document.querySelectorAll('.side-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.side-btn').forEach(b => { b.classList.remove('active'); b.setAttribute('aria-pressed','false'); });
            btn.classList.add('active');
            btn.setAttribute('aria-pressed','true');
            updateOrderEstimate();
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
    document.getElementById('filter-sector-select').addEventListener('change', loadStocks);
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
                    `${order.instrumentId} ${order.side} ${order.status} · ${order.filledQuantity}/${order.quantity} saham · ${order.requestId}`,
                    () => { activeRequestId = order.requestId; orderDraft = null; orderPreview = null; showOrderReceipt(order); }));
            } catch (error) { flowText(list, 'Riwayat gagal dimuat', error.message); }
        };
        activeOnly.addEventListener('change', render); render();
    });

    document.getElementById('btn-briefing').addEventListener('click', () => {
        refreshSnapshot();
        if (latestSnapshot && latestSnapshot.recoveryRequired) return;
        const panel = showFlow('Jadwal & Sesi Bursa');
        const clock = latestSnapshot && latestSnapshot.clock;
        flowText(panel, 'Waktu game saat ini', clock ? `${clock.date} · ${clock.time}` : 'Belum tersedia');
        flowText(panel, 'Mode waktu', clock && Number(clock.rate) > 0 ? `Laju aktif dasar: 1 hari game = ${(86400 / Number(clock.rate) / 60).toLocaleString('id-ID')} menit nyata.` : 'Laju aktif belum tersedia.');
        flowText(panel, 'Sesi IDX (Indonesia)', '09:00 - 11:30 (Sesi 1) · 13:30 - 16:00 (Sesi 2)');
        flowText(panel, 'Sesi US (Wall Street)', '21:30 - 04:00 WIB (Sesi Reguler)');
        flowText(panel, 'Status sesi aktif', clock ? clock.sessionLabel : 'Belum tersedia');
        flowText(panel, 'Keterangan waktu latar', 'Saat aplikasi ditutup, waktu berlanjut dengan rasio 1 hari game = 1 jam nyata.');
        flowButton(panel, 'Pusat Misi & Kalender Bursa', () => openPeriodicMissionsModal());

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
                    flowText(review, 'Misi', `${mission.id} · ${mission.title}`);
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
            reports.forEach(report => flowButton(panel, `Laporan ${report.date} · ${report.investmentPnL}`, () => {
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
            { value: '25000000', label: 'Rp 25.000.000 · modal kecil' },
            { value: '50000000', label: 'Rp 50.000.000 · modal standar' },
            { value: '100000000', label: 'Rp 100.000.000 · modal besar' }
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
                    card.append(node('span', `change-pill ${a.unlocked ? 'positive' : ''}`, a.unlocked ? 'Terbuka' : 'Dalam proses'), node('h3', '', a.name), node('p', '', a.desc));
                    flowText(card, 'Progres', a.progress);
                    flowText(card, 'Hadiah XP', a.xp);
                    grid.appendChild(card);
                });
            }

            ['all', 'open', 'locked'].forEach(f => {
                const btn = document.createElement('button');
                btn.className = 'btn-secondary';
                btn.style.flex = '1';
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

    function openAvatarCustomizerModal() {
        const panel = showFlow('Kustomisasi Avatar & Bingkai');
        flowText(panel, 'Identitas & Profil Prestise', 'Pilih foto avatar investor dan bingkai unik untuk memperindah tampilan profil trader Anda.');

        const avatars = [
            { id: 'avatar_trader_muda.png', name: 'Trader Muda Ambisius', rarity: 'Umum' },
            { id: 'avatar_analis_senior.png', name: 'Analis Senior Wall St', rarity: 'Tidak Umum' },
            { id: 'avatar_fund_maestro.png', name: 'Hedge Fund Maestro', rarity: 'Langka' },
            { id: 'avatar_taipan_sultan.png', name: 'Taipan Sultan Nusantara', rarity: 'Legendaris' }
        ];

        const frames = [
            { id: 'frame_steel.png', name: 'Baja Klasik Halus', rarity: 'Umum' },
            { id: 'frame_bronze_champ.png', name: 'Perunggu Juara Liga', rarity: 'Tidak Umum' },
            { id: 'frame_gold_tycoon.png', name: 'Emas Manajer Konglomerat', rarity: 'Langka' },
            { id: 'frame_emperor_dragon.png', name: 'Naga Emas Kaisar', rarity: 'Legendaris' }
        ];

        const avCard = node('section', 'card');
        avCard.append(node('h3', '', 'Pilihan Avatar Investor'));
        avatars.forEach(a => {
            const row = node('div', 'data-row');
            row.style.cursor = 'pointer';
            row.style.padding = '8px 0';
            row.append(node('span', '', `${a.name} (${a.rarity})`), node('strong', '', 'Pasang'));
            row.onclick = () => {
                if (bridge.equipCosmetic) {
                    bridge.equipCosmetic('AVATAR', a.id);
                    alert(`Avatar ${a.name} berhasil dipasang!`);
                    refreshSnapshot();
                }
            };
            avCard.append(row);
        });
        panel.append(avCard);

        const frCard = node('section', 'card');
        frCard.append(node('h3', '', 'Pilihan Bingkai Ornamen'));
        frames.forEach(f => {
            const row = node('div', 'data-row');
            row.style.cursor = 'pointer';
            row.style.padding = '8px 0';
            row.append(node('span', '', `${f.name} (${f.rarity})`), node('strong', '', 'Pasang'));
            row.onclick = () => {
                if (bridge.equipCosmetic) {
                    bridge.equipCosmetic('FRAME', f.id);
                    alert(`Bingkai ${f.name} berhasil dipasang!`);
                    refreshSnapshot();
                }
            };
            frCard.append(row);
        });
        panel.append(frCard);
    }

    function openResearchBoostersModal() {
        const panel = showFlow('Kartu Booster Riset R&D');
        flowText(panel, 'Booster Efisiensi Sementara', 'Aktifkan kartu booster riset untuk meningkatkan akurasi sinyal berita, diskon komisi, atau memperluas plafon likuiditas.');

        if (!bridge.getResearchBoosters) {
            flowText(panel, 'Status', 'Layanan kartu booster belum tersedia.');
            return;
        }

        try {
            const boosters = JSON.parse(bridge.getResearchBoosters());
            boosters.forEach(b => {
                const card = document.createElement('section');
                card.className = 'card booster-card';
                card.style.cssText = 'margin-bottom:12px; border:1px solid rgba(0, 229, 255, 0.35); background:linear-gradient(135deg, rgba(0, 229, 255, 0.08) 0%, rgba(18, 23, 40, 0.95) 100%);';

                card.innerHTML = `
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                        <span class="eyebrow" style="color:var(--teal); font-weight:700;">${b.type}</span>
                        <span class="badge" style="background:rgba(0, 229, 255, 0.18); color:var(--teal); font-size:0.7rem; font-weight:700;">${b.statusLabel}</span>
                    </div>
                    <h3 style="font-size:1.1rem; color:#FFF; margin:0 0 6px;">${b.title}</h3>
                    <p style="font-size:0.82rem; color:var(--text-secondary); margin:0 0 8px; line-height:1.4;">${b.description}</p>
                    <div class="data-row"><span>Sisa Sesi / Pemakaian</span><strong>${b.remainingUses} kali</strong></div>
                `;

                if (!b.isActive && b.remainingUses > 0) {
                    const actBtn = document.createElement('button');
                    actBtn.className = 'btn-primary';
                    actBtn.style.marginTop = '10px';
                    actBtn.textContent = 'Aktifkan Kartu Booster';
                    actBtn.onclick = () => {
                        try {
                            const res = JSON.parse(bridge.activateResearchBooster(b.boosterId));
                            alert(`Kartu ${b.title} berhasil diaktifkan!`);
                            refreshSnapshot();
                            openResearchBoostersModal();
                        } catch(err) { alert('Gagal mengaktifkan booster: ' + err.message); }
                    };
                    card.appendChild(actBtn);
                } else if (b.isActive) {
                    const activeP = document.createElement('p');
                    activeP.style.cssText = 'color:var(--teal); font-weight:600; font-size:0.8rem; margin-top:8px;';
                    activeP.textContent = '⚡ Kartu Booster sedang aktif!';
                    card.appendChild(activeP);
                }

                panel.appendChild(card);
            });
        } catch(e) {
            flowText(panel, 'Galat', e.message);
        }
    }

    function openTechTreeModal() {
        const panel = showFlow('Pohon Riset Finansial Tech Tree');
        flowText(panel, 'R&D Finansial Berjenjang', 'Tingkatkan kemampuan analisis, efisiensi komisi, dan kapasitas AUM firma secara permanen.');

        if (!bridge.getTechTree) {
            flowText(panel, 'Status', 'Layanan pohon riset belum tersedia.');
            return;
        }

        try {
            const nodes = JSON.parse(bridge.getTechTree());
            nodes.forEach(n => {
                const card = document.createElement('section');
                card.className = 'card tech-node-card';
                card.style.cssText = 'margin-bottom:12px; border:1px solid rgba(153, 69, 255, 0.35); background:linear-gradient(135deg, rgba(153, 69, 255, 0.08) 0%, rgba(18, 23, 40, 0.95) 100%);';

                card.innerHTML = `
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                        <span class="eyebrow" style="color:var(--accent-purple); font-weight:700;">JALUR: ${n.category} · TIER ${n.tierLevel}</span>
                        <span class="badge" style="background:${n.isUnlocked ? 'rgba(20, 241, 149, 0.18)' : 'rgba(255, 255, 255, 0.08)'}; color:${n.isUnlocked ? 'var(--teal)' : 'var(--text-muted)'}; font-weight:700;">${n.statusLabel}</span>
                    </div>
                    <h3 style="font-size:1.1rem; color:#FFF; margin:0 0 6px;">${n.nodeName}</h3>
                    <div class="data-row"><span>Efek Bonus</span><strong>+${n.statBonusValue}% (${n.statBonusType})</strong></div>
                    <div class="data-row"><span>Biaya Riset</span><strong>${n.upgradeCost}</strong></div>
                `;

                if (!n.isUnlocked) {
                    const upBtn = document.createElement('button');
                    upBtn.className = 'btn-primary';
                    upBtn.style.marginTop = '10px';
                    upBtn.textContent = 'Riset ' + n.nodeName;
                    upBtn.onclick = () => {
                        try {
                            const res = JSON.parse(bridge.upgradeTechNode(n.nodeId));
                            alert(`Riset ${res.nodeName} berhasil diselesaikan!`);
                            refreshSnapshot();
                            openTechTreeModal();
                        } catch(err) { alert('Gagal riset: ' + err.message); }
                    };
                    card.appendChild(upBtn);
                }

                panel.appendChild(card);
            });
        } catch(e) {
            flowText(panel, 'Galat', e.message);
        }
    }

    function openHeadhuntingModal() {
        const panel = showFlow('Bursa Headhunting Staf Elit');
        flowText(panel, 'Perang Bakat & Rekrutmen Kompetitor', 'Bajak analis kuantitatif dan manajer risiko bintang dari firma pesaing untuk mendongkrak return AUM.');

        if (!bridge.getHeadhuntingCandidates) {
            flowText(panel, 'Status', 'Layanan headhunting belum tersedia.');
            return;
        }

        try {
            const cands = JSON.parse(bridge.getHeadhuntingCandidates());
            cands.forEach(c => {
                const card = document.createElement('section');
                card.className = 'card headhunt-card';
                card.style.cssText = 'margin-bottom:12px; border:1px solid rgba(255, 184, 0, 0.35); background:linear-gradient(135deg, rgba(255, 184, 0, 0.08) 0%, rgba(18, 23, 40, 0.95) 100%);';

                card.innerHTML = `
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                        <span class="eyebrow" style="color:var(--gold); font-weight:700;">RATING: ${c.skillRating} / 100</span>
                        <span class="badge" style="background:${c.isHired ? 'rgba(20, 241, 149, 0.18)' : 'rgba(255, 184, 0, 0.18)'}; color:${c.isHired ? 'var(--teal)' : 'var(--gold)'}; font-weight:700;">${c.statusLabel}</span>
                    </div>
                    <h3 style="font-size:1.1rem; color:#FFF; margin:0 0 4px;">${c.name}</h3>
                    <p style="font-size:0.8rem; color:var(--text-muted); margin:0 0 8px;">${c.roleTitle} · Majikan: ${c.currentEmployer}</p>
                    <div class="data-row"><span>Biaya Jasa Headhunter</span><strong>${c.poachCost}</strong></div>
                    <div class="data-row"><span>Ekspektasi Gaji Bulanan</span><strong>${c.expectedSalary}</strong></div>
                `;

                if (!c.isHired) {
                    const poachBtn = document.createElement('button');
                    poachBtn.className = 'btn-primary';
                    poachBtn.style.marginTop = '10px';
                    poachBtn.textContent = 'Bajak ' + c.name;
                    poachBtn.onclick = () => {
                        try {
                            const res = JSON.parse(bridge.poachStaffCandidate(c.candidateId));
                            alert(`${res.name} berhasil dibajak ke firma Anda!`);
                            refreshSnapshot();
                            openHeadhuntingModal();
                        } catch(err) { alert('Gagal membajak staf: ' + err.message); }
                    };
                    card.appendChild(poachBtn);
                }

                panel.appendChild(card);
            });
        } catch(e) {
            flowText(panel, 'Galat', e.message);
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
    document.getElementById('btn-profile')?.addEventListener('click', () => openPlayerProfileModal());
    document.getElementById('btn-market-research')?.addEventListener('click', () => switchTab('riset'));
    document.getElementById('btn-market-corporate')?.addEventListener('click', openCorporateActionModal);
    const accBtn = document.getElementById('context-account-btn');
    if (accBtn) accBtn.addEventListener('click', openAccountPicker);

    const misBtn = document.getElementById('btn-missions');
    if (misBtn) misBtn.addEventListener('click', openPeriodicMissionsModal);
    const leaBtn = document.getElementById('btn-league');
    if (leaBtn) leaBtn.addEventListener('click', openLeagueAndRivalModal);
    const bossBtn = document.getElementById('btn-market-boss');
    if (bossBtn) bossBtn.addEventListener('click', openMarketBossModal);

    const claimSponsorBtn = document.getElementById('btn-claim-sponsor');
    const handleClaimSponsor = () => {
        try {
            const reqId = 'SPONSOR-' + Date.now();
            const res = JSON.parse(bridge.claimDailySponsor ? bridge.claimDailySponsor(JSON.stringify({requestId: reqId})) : '{"success":false,"message":"Fitur belum tersedia"}');
            if (res.success) {
                showGameToast('success', 'Hibah diklaim', res.message || `${latestDailySponsor?.grantAmountFormatted || 'Dana hibah'} masuk ke kas pribadi.`);
                refreshSnapshot();
                if (claimSponsorBtn) {
                    claimSponsorBtn.disabled = true;
                    claimSponsorBtn.textContent = 'Sudah Diklaim Hari Ini ✓';
                }
            } else {
                showGameToast('info', 'Dana Hibah', res.message || 'Belum dapat diklaim');
            }
        } catch(e) {
            showGameToast('info', 'Klaim Gagal', e.message);
        }
    };
    if (claimSponsorBtn) claimSponsorBtn.addEventListener('click', handleClaimSponsor);

    // Pintasan Bar Aksi Cepat Beranda (Referensi Visual 01 & 03)
    const qaBuy = document.getElementById('btn-qa-buy');
    if (qaBuy) qaBuy.addEventListener('click', () => { switchTab('pasar'); loadStocks(); });
    const qaPort = document.getElementById('btn-qa-portfolio');
    if (qaPort) qaPort.addEventListener('click', () => { switchTab('aset'); loadAsetView(); });
    const qaBiz = document.getElementById('btn-qa-business');
    if (qaBiz) qaBiz.addEventListener('click', () => { switchTab('firma'); loadFirmaView('usaha'); });
    const qaSponsor = document.getElementById('btn-qa-sponsor');
    if (qaSponsor) qaSponsor.addEventListener('click', handleClaimSponsor);
    const qaLoans = document.getElementById('btn-qa-loans');
    if (qaLoans) qaLoans.addEventListener('click', openLoansModal);

    const homeColBtn = document.getElementById('btn-home-collections');
    if (homeColBtn) homeColBtn.addEventListener('click', openCollectionsGalleryModal);

    const homeArchBtn = document.getElementById('btn-home-archetype');
    if (homeArchBtn) homeArchBtn.addEventListener('click', openInvestorArchetypeModal);
    const rdBtn = document.getElementById('btn-reksa-dana');
    if (rdBtn) rdBtn.addEventListener('click', openMutualFundsMarketModal);

    // Event Listeners Filter Kabar Pasar Hari Ini (T5)
    const newsFilterAll = document.getElementById('news-filter-all');
    const newsFilterGood = document.getElementById('news-filter-good');
    const newsFilterBad = document.getElementById('news-filter-bad');
    const btnSeeAllNews = document.getElementById('btn-see-all-news');
    if (newsFilterAll) newsFilterAll.onclick = () => {
        homeNewsFilter = 'all';
        [newsFilterAll, newsFilterGood, newsFilterBad].forEach(b => { b?.classList.remove('active'); b?.setAttribute('aria-pressed', String(b === newsFilterAll)); });
        newsFilterAll.classList.add('active');
        renderHomeNews();
    };
    if (newsFilterGood) newsFilterGood.onclick = () => {
        homeNewsFilter = 'good';
        [newsFilterAll, newsFilterGood, newsFilterBad].forEach(b => { b?.classList.remove('active'); b?.setAttribute('aria-pressed', String(b === newsFilterGood)); });
        newsFilterGood.classList.add('active');
        renderHomeNews();
    };
    if (newsFilterBad) newsFilterBad.onclick = () => {
        homeNewsFilter = 'bad';
        [newsFilterAll, newsFilterGood, newsFilterBad].forEach(b => { b?.classList.remove('active'); b?.setAttribute('aria-pressed', String(b === newsFilterBad)); });
        newsFilterBad.classList.add('active');
        renderHomeNews();
    };
    if (btnSeeAllNews) btnSeeAllNews.onclick = () => switchTab('riset');

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
        flowButton(panel, 'Ukuran teks 200%', () => { try { persistPlayerPreferences({textSize:'200%'}); } catch(error) { flowText(panel,'Preferensi belum tersimpan',error.message); } });
        flowButton(panel, 'Ukuran teks standar', () => { try { persistPlayerPreferences({textSize:'100%'}); } catch(error) { flowText(panel,'Preferensi belum tersimpan',error.message); } });

        const isPowerSave = document.documentElement.classList.contains('power-save');
        const psBtn = flowButton(panel, isPowerSave ? 'Hemat daya: aktif' : 'Hemat daya: nonaktif', () => {
            const next = !document.documentElement.classList.contains('power-save');
            document.documentElement.classList.toggle('power-save', next);
            try { localStorage.setItem('game_power_save', next ? 'true' : 'false'); } catch(e) {}
            psBtn.textContent = next ? 'Hemat daya: aktif' : 'Hemat daya: nonaktif';
            psBtn.setAttribute('aria-pressed', String(next));
        });
        psBtn.id = 'toggle-power-save'; psBtn.setAttribute('aria-pressed', String(isPowerSave));

        flowText(panel, 'Gerak', 'Preferensi kurangi gerakan sistem dihormati. Hemat daya menonaktifkan transisi visual.');
    });

    document.getElementById('btn-home-market').addEventListener('click', () => switchTab('pasar'));
    const navResize = new ResizeObserver(entries => document.documentElement.style.setProperty('--nav-height', entries[0].target.getBoundingClientRect().height + 'px'));
    navResize.observe(document.querySelector('.bottom-nav'));
    document.addEventListener('keydown', event => {
        if (event.key === 'Escape' && !orderModal.classList.contains('active') && flowPanel) { event.preventDefault(); closeFlow(); }
    });
    // Initial Load & Power Save preference restore
    try {
        if (localStorage.getItem('game_power_save') === 'true') {
            document.documentElement.classList.add('power-save');
        }
    } catch(e) {}
    refreshSnapshot(); loadHomeMarket();

    window.showNativeSaveFailure = () => {
        if (document.getElementById('native-save-warning')) return;
        const warning = document.createElement('p');
        warning.id = 'native-save-warning'; warning.setAttribute('role', 'alert');
        warning.className = 'mock-banner';
        warning.textContent = 'Progres terakhir belum tersimpan. Buka Pengaturan untuk mencoba simpan ulang.';
        document.body.prepend(warning);
    };

    // Force-close resilience: autosave on backgrounding, swipe-away, or pagehide
    let lastClockSample = performance.now();
    const autoSaveOnExit = () => {
        try {
            if (bridge && bridge.saveGame) bridge.saveGame();
        } catch(e) {}
    };
    document.addEventListener('visibilitychange', () => {
        lastClockSample = performance.now();
        if (document.hidden) {
            autoSaveOnExit();
        } else {
            refreshSnapshot();
            if (['beranda', 'pasar'].includes(currentTab)) updateMarketQuotes();
            if (currentTab === 'aset') syncPortfolioLive();
        }
    });
    window.addEventListener('pagehide', autoSaveOnExit);
    window.addEventListener('beforeunload', autoSaveOnExit);
    setInterval(() => {
        renderFlowingClock();
        updateSponsorCountdown();
    }, 250);

    setInterval(() => {
        try {
            const now = performance.now();
            if (document.hidden) { lastClockSample = now; return; }
            const elapsed = Math.floor((now - lastClockSample) / 1000);
            if (elapsed > 0 && bridge && bridge.tickClock) {
                lastClockSample += elapsed * 1000;
                const tick = JSON.parse(bridge.tickClock(elapsed));
                if (tick.success === false || tick.error) throw new Error(tick.error || tick.message || 'Proses hari perlu diperiksa.');
                document.getElementById('clock-error')?.remove();
                refreshSnapshot();
                if (currentTab === 'aset') syncPortfolioLive();
                if (activeReceiptRequestId) updateReceiptLive(activeReceiptRequestId);
                if (activeDetailStock) updateInstrumentDetailLive(activeDetailStock.id);
                if (['beranda', 'pasar'].includes(currentTab) && !flowPanel) updateMarketQuotes();
            }
        } catch (cause) {
            const error = document.getElementById('clock-error') || node('p');
            error.id = 'clock-error'; error.setAttribute('role', 'alert');
            error.textContent = 'Pembaruan waktu belum terkonfirmasi. Data terakhir tetap ditampilkan. ' + cause.message;
            if (!error.isConnected) document.querySelector('main').prepend(error);
            refreshSnapshot();
        }
    }, 1000);

})();
