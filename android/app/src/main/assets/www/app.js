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
    let anchorRate = 0;
    let clockRunId = null;
    let latestDailySponsor = null;
    let clockSyncRunning = false;
    let clockSyncBlocked = false;
    let lastKnownSessionLabel = null;
    let clockSyncPromise = null;
    let clockSyncRegions = null;
    let lastClockSyncPerf = -Infinity;

    function requestClockSync(foreground = !document.hidden, isManual = false) {
        if (!bridge.beginClockSync) return Promise.resolve();
        if (clockSyncRunning) { bridge.beginClockSync(foreground); return clockSyncPromise; }
        clockSyncRunning = true;

        if (isManual) {
            clockSyncBlocked = true;
            const heavy = performance.now() - lastClockSyncPerf > 5000;
            const regions = [...document.querySelectorAll('.app-header, .app-scroll, .bottom-nav, #order-modal')];
            if (!clockSyncRegions) clockSyncRegions = regions.map(region => ({region, inert: region.inert}));
            const focused = document.activeElement;
            const lockHeavy = () => regions.forEach(region => { region.inert = true; });
            if (heavy) lockHeavy();
            let deferredClick = null;
            const blockInteraction = event => {
                if (event.target.closest('#clock-sync-status, .back-button, #btn-close-modal, .toast-dismiss')) {
                    return;
                }
                event.preventDefault(); event.stopImmediatePropagation();
                if (event.type === 'click' && !heavy && !regions.some(region => region.inert)) {
                    deferredClick = event.target.closest('button, a, input, select, textarea, [role="button"]');
                }
            };
            document.addEventListener('click', blockInteraction, true);
            document.addEventListener('submit', blockInteraction, true);
            const notice = document.getElementById('clock-sync-status') || node('section', 'clock-sync-status');
            notice.id = 'clock-sync-status';
            notice.setAttribute('role', 'status');
            notice.replaceChildren(node('strong', '', 'Menyelaraskan waktu permainan'), node('p', '', 'Aktivitas selama kamu pergi sedang diproses. Transaksi tersedia kembali setelah progres tersimpan.'));
            notice.hidden = !heavy;
            if (!notice.isConnected) document.body.append(notice);
            clockSyncPromise = new Promise((resolve, reject) => {
                const reveal = setTimeout(() => { lockHeavy(); notice.hidden = false; }, 150);
                requestAnimationFrame(() => setTimeout(() => {
                    try { bridge.beginClockSync(foreground); } catch (error) { finish(error); return; }
                    poll();
                }, 0));
                function finish(error) {
                    clearTimeout(reveal);
                    document.removeEventListener('click', blockInteraction, true);
                    document.removeEventListener('submit', blockInteraction, true);
                    clockSyncRunning = false;
                    clockSyncBlocked = false;
                    if (clockSyncRegions) {
                        clockSyncRegions.forEach(({region, inert}) => { region.inert = inert; });
                        clockSyncRegions = null;
                    }
                    if (notice.isConnected) notice.remove();
                    if (error) {
                        reject(error);
                        return;
                    }
                    if (focused && focused.isConnected && !focused.closest('[inert]')) focused.focus({preventScroll: true});
                    lastClockSyncPerf = performance.now();
                    resolve();
                    if (deferredClick && deferredClick.isConnected) queueMicrotask(() => deferredClick.click());
                }
                function poll() {
                    try {
                        const state = JSON.parse(bridge.getClockSyncStatus());
                        if (state.status === 'PENDING') { setTimeout(poll, 50); return; }
                        if (state.success === false || state.status !== 'COMPLETED') throw new Error(state.message || 'Progres belum dapat diselaraskan.');
                        finish();
                    } catch (error) { finish(error); }
                }
            });
            return clockSyncPromise;
        }

        // Background non-blocking heartbeat
        clockSyncPromise = new Promise((resolve, reject) => {
            try { bridge.beginClockSync(foreground); } catch (error) { clockSyncRunning = false; reject(error); return; }
            function pollBackground() {
                try {
                    const state = JSON.parse(bridge.getClockSyncStatus());
                    if (state.status === 'PENDING') { setTimeout(pollBackground, 50); return; }
                    clockSyncRunning = false;
                    if (state.success === false || state.status !== 'COMPLETED') {
                        reject(new Error(state.message || 'Penyelarasan waktu latar belakang gagal'));
                        return;
                    }
                    lastClockSyncPerf = performance.now();
                    resolve();
                } catch (error) {
                    clockSyncRunning = false;
                    reject(error);
                }
            }
            pollBackground();
        });
        return clockSyncPromise;
    }

    function renderFlowingClock() {
        if (!anchorGameTimestamp || document.hidden) return;
        const elapsedRealMs = performance.now() - anchorPerfNow;
        const currentSimMs = anchorGameTimestamp + elapsedRealMs * anchorRate;
        // Waktu game dihitung dalam offset WIB (UTC+7)
        const date = new Date(currentSimMs + 7 * 3600000);
        const hours = String(date.getUTCHours()).padStart(2, '0');
        const mins = String(date.getUTCMinutes()).padStart(2, '0');
        if (clockDisplay) {
            clockDisplay.textContent = `${hours}:${mins} WIB`;
        }
    }

    let newsRealHighWater = 0;
    let newsRealPerfAnchor = performance.now();

    function newsEffectLabel(news) {
        if (news.effectStatus === 'NOT_APPLICABLE_REPLAY') return 'Tidak berlaku pada replay';
        if (news.effectStatus === 'SCHEDULED') {
            const gameNow = anchorGameTimestamp + Math.max(0, performance.now() - anchorPerfNow) * anchorRate;
            const remaining = Math.max(0, Number(news.publishedAt) - gameNow);
            if (!Number.isFinite(remaining)) return news.countdownLabel || 'Terjadwal';
            const minutes = Math.ceil(remaining / 60000);
            return minutes ? `Rilis dalam ${Math.floor(minutes / 60)}j ${minutes % 60}m (waktu game)` : 'Menunggu konfirmasi rilis';
        }
        const ends = Number(news.effectEndsRealAt);
        const asOf = Number(news.effectAsOfRealAt);
        if (!Number.isFinite(ends) || ends < 0 || !Number.isFinite(asOf)) return 'Waktu pengaruh belum tersedia';
        const perf = performance.now();
        newsRealHighWater = Math.max(asOf, Date.now(), newsRealHighWater + Math.max(0, perf - newsRealPerfAnchor));
        newsRealPerfAnchor = perf;
        const remaining = Math.max(0, ends - newsRealHighWater);
        if (remaining === 0) return 'Berakhir';
        const minutes = Math.ceil(remaining / 60000);
        const hours = Math.floor(minutes / 60);
        return `Aktif · sisa ${hours ? hours + 'j ' : ''}${minutes % 60}m nyata`;
    }

    const visibleNews = new Map();
    let homeNewsSignature = '';

    function rememberNews(news) {
        news.forEach(article => visibleNews.set(article.id, article));
    }
    function newsEffectNode(news, className = '') {
        visibleNews.set(news.id, news);
        const label = node('span', className, newsEffectLabel(news));
        label.dataset.newsEffectId = news.id;
        return label;
    }
    function updateNewsEffects() {
        if (document.hidden) return;
        document.querySelectorAll('[data-news-effect-id]').forEach(label => {
            const news = visibleNews.get(label.dataset.newsEffectId);
            if (news) {
                const text = newsEffectLabel(news);
                if (label.textContent !== text) label.textContent = text;
            }
        });
    }
    function appendNewsTiming(panel, news) {
        const grid = node('div', 'news-timing-grid');
        const appendCell = (label, valueNode) => {
            const cell = node('div', 'timing-cell');
            cell.append(node('small', '', label), valueNode);
            grid.append(cell);
        };
        appendCell('Status pengaruh', newsEffectNode(news, 'change-pill'));
        appendCell('Durasi nyata', node('strong', '', Number.isFinite(Number(news.effectDurationRealMs)) ? `${Number(news.effectDurationRealMs) / 60000} menit` : 'Belum tersedia'));
        appendCell('Perkiraan dampak', node('strong', '', `${news.direction || 'NETRAL'}${news.estimatedRange ? ' · ' + news.estimatedRange : ''}`));
        for (const [key, label] of [['effectStartsRealAt', 'Mulai nyata (WIB)'], ['effectEndsRealAt', 'Berakhir nyata (WIB)']]) {
            appendCell(label, node('strong', '', Number(news[key]) >= 0 ? new Date(Number(news[key])).toLocaleString('id-ID', {timeZone: 'Asia/Jakarta'}) : 'Belum tersedia'));
        }
        panel.append(grid);
    }
    function openNewsDetail(news, returnTo = null) {
        const current = visibleNews.get(news.id) || news;
        const detail = showFlow('Kabar & Dampak Pasar');
        if (returnTo) {
            detail.newsReturn = returnTo;
        }
        detail.append(node('h2', '', current.headline));
        appendNewsTiming(detail, current);
        flowText(detail, 'Kategori & Sektor', `${current.categoryLabel || 'Pasar'} · ${current.affectedSector || 'Umum'}`);
        if (current.relatedTicker) flowText(detail, 'Emiten terkait', current.relatedTicker);
        if (current.confidence) flowText(detail, 'Keyakinan', current.confidence);
        flowText(detail, 'Publikasi (waktu game)', current.countdownLabel || 'Terjadwal');
        detail.append(node('p', 'article-copy', current.content));
        if (current.isPublished && bridge.recordResearchRead) bridge.recordResearchRead(current.id);
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
                if (claimSponsorBtn) {
                    claimSponsorBtn.disabled = false;
                    claimSponsorBtn.textContent = 'Klaim Bantuan Sponsor';
                }
                if (qaSponsor) {
                    qaSponsor.disabled = false;
                    qaSponsor.setAttribute('aria-label', 'Klaim bantuan sponsor harian');
                }
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
    // Bounded in-memory session LRU cache for deduplicating repeated callbacks within the active session (capacity 500)
    const settledEventKeys = new Set();
    const toastNotificationHistory = [];
    window.getToastHistory = () => toastNotificationHistory.slice();
    const toastQueue = [];

    function processToastQueue() {
        const container = document.getElementById('toast-container');
        if (!container || toastQueue.length === 0) return;
        while (container.children.length < 3 && toastQueue.length > 0) {
            // Priority queue: critical messages (error/alert) take precedence over info/success
            let idx = toastQueue.findIndex(t => t.isCritical);
            if (idx === -1) idx = 0;
            const [next] = toastQueue.splice(idx, 1);
            renderToast(next.type, next.title, next.message);
        }
    }

    function renderToast(type, title, message) {
        const container = document.getElementById('toast-container');
        if (!container) return;
        const isIncomingCritical = type === 'error' || type === 'alert';
        const toast = document.createElement('div');
        toast.className = `game-toast ${type || 'info'}`;
        toast.setAttribute('role', isIncomingCritical ? 'alert' : 'status');
        const icon = node('div', 'toast-icon', type === 'success' || type === 'achievement' ? '✓' : 'i');
        icon.setAttribute('aria-hidden', 'true');
        const body = node('div', 'toast-body'); body.append(node('strong', '', title), node('small', '', message));
        const dismiss = node('button', 'toast-dismiss', '×'); dismiss.setAttribute('aria-label', 'Tutup pemberitahuan');
        const removeToast = () => {
            if (!toast.isConnected) return;
            toast.classList.remove('visible');
            setTimeout(() => {
                toast.remove();
                processToastQueue();
            }, 250);
        };
        dismiss.onclick = removeToast;
        toast.append(icon, body, dismiss);
        container.appendChild(toast);
        requestAnimationFrame(() => toast.classList.add('visible'));

        // Non-critical toasts auto-dismiss after 3200ms. Critical toasts stay until dismissed by player!
        if (!isIncomingCritical) {
            setTimeout(removeToast, 3200);
        }
    }

    // K-018: Tracking in-flight collection intents so retries after lost response reuse requestId & recover receipt
    const pendingCollectionIntents = new Map(); // key: (buy ? 'BUY:' : 'SELL:') + itemId -> { requestId, expectedPrice }

    function loadPersistedCollectionIntents() {
        try {
            const raw = (typeof sessionStorage !== 'undefined') ? sessionStorage.getItem('game_pending_col_intents') : null;
            if (raw) {
                const parsed = JSON.parse(raw);
                for (const [k, v] of Object.entries(parsed)) {
                    pendingCollectionIntents.set(k, v);
                }
            }
        } catch(e) {}
    }
    loadPersistedCollectionIntents();

    function persistCollectionIntents() {
        try {
            if (typeof sessionStorage !== 'undefined') {
                const obj = {};
                for (const [k, v] of pendingCollectionIntents.entries()) {
                    obj[k] = v;
                }
                sessionStorage.setItem('game_pending_col_intents', JSON.stringify(obj));
            }
        } catch(e) {}
    }

    function getOrCreateCollectionRequestId(itemId, buy, expectedPrice) {
        const key = (buy ? 'BUY:' : 'SELL:') + itemId;
        const existing = pendingCollectionIntents.get(key);
        if (existing && existing.expectedPrice === expectedPrice) {
            return existing.requestId;
        }
        const requestId = 'COL-' + (buy ? 'BUY' : 'SELL') + '-' + itemId + '-' + Date.now();
        pendingCollectionIntents.set(key, { requestId, expectedPrice, itemId, buy });
        persistCollectionIntents();
        return requestId;
    }

    function clearCollectionIntent(itemId, buy) {
        const key = (buy ? 'BUY:' : 'SELL:') + itemId;
        pendingCollectionIntents.delete(key);
        persistCollectionIntents();
    }

    function checkAndRecoverPendingCollectionIntent(item) {
        if (!item || !bridge.transactCollection) return;
        const buyKey = 'BUY:' + item.id;
        const sellKey = 'SELL:' + item.id;
        const isOwned = Boolean(item.isOwned || item.owned);
        if (isOwned && pendingCollectionIntents.has(buyKey)) {
            const intent = pendingCollectionIntents.get(buyKey);
            try {
                const req = { requestId: intent.requestId, itemId: item.id, buy: true, expectedPrice: intent.expectedPrice };
                const res = JSON.parse(bridge.transactCollection(JSON.stringify(req)));
                if (res && res.success) {
                    clearCollectionIntent(item.id, true);
                    const transAmount = res.amount ? formatMoney(res.amount) : formatMoney(intent.expectedPrice);
                    showGameToast('success', 'Pembelian Dipulihkan!', `${item.name} berhasil dibeli seharga ${transAmount}. Status telah dikonfirmasi dari receipt domain.`);
                    return res;
                }
            } catch(e) {}
        }
        if (!isOwned && pendingCollectionIntents.has(sellKey)) {
            const intent = pendingCollectionIntents.get(sellKey);
            try {
                const req = { requestId: intent.requestId, itemId: item.id, buy: false, expectedPrice: intent.expectedPrice };
                const res = JSON.parse(bridge.transactCollection(JSON.stringify(req)));
                if (res && res.success) {
                    clearCollectionIntent(item.id, false);
                    const transAmount = res.amount ? formatMoney(res.amount) : formatMoney(intent.expectedPrice);
                    showGameToast('success', 'Penjualan Dipulihkan!', `${item.name} berhasil dijual seharga ${transAmount}. Status telah dikonfirmasi dari receipt domain.`);
                    return res;
                }
            } catch(e) {}
        }
    }

    function showGameToast(type, title, message, dedupeKey) {
        if (dedupeKey) {
            if (settledEventKeys.has(dedupeKey)) return;
            settledEventKeys.add(dedupeKey);
            if (settledEventKeys.size > 500) {
                const oldest = settledEventKeys.values().next().value;
                settledEventKeys.delete(oldest);
            }
        } else {
            const key = title + ':' + message;
            if (activeToastKeys.has(key)) return;
            activeToastKeys.add(key);
            setTimeout(() => { activeToastKeys.delete(key); }, 5000);
        }

        // Bounded notification history (inbox) for recovery trail
        toastNotificationHistory.push({ type: type || 'info', title, message, dedupeKey: dedupeKey || null, timestamp: Date.now() });
        if (toastNotificationHistory.length > 50) toastNotificationHistory.shift();

        const container = document.getElementById('toast-container');
        if (!container) return;

        const isIncomingCritical = type === 'error' || type === 'alert';

        if (container.children.length >= 3) {
            // Priority eviction: evict oldest non-alert/non-error toast first
            let evictCandidate = null;
            for (let i = 0; i < container.children.length; i++) {
                const child = container.children[i];
                if (!child.classList.contains('error') && !child.classList.contains('alert')) {
                    evictCandidate = child;
                    break;
                }
            }
            if (evictCandidate) {
                evictCandidate.remove();
                renderToast(type, title, message);
                return;
            } else {
                // All 3 slots currently contain critical (error/alert) messages!
                // Critical protection: NEVER evict an active critical toast!
                // Queue the incoming toast in toastQueue so it is shown when one is dismissed
                toastQueue.push({ type, title, message, isCritical: isIncomingCritical });
                if (toastQueue.length > 20) toastQueue.shift();
                return;
            }
        }

        renderToast(type, title, message);
    }
    window.showGameToast = showGameToast;

    function applyPlayerPreferences(preferences) {
        document.documentElement.style.fontSize = ''; // Mengikuti skala teks sistem perangkat bawaan (A12)
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
        if (clockSyncBlocked) return;
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
                const presenterNow = performance.now();
                const sameRun = clockRunId === snap.runId;
                if (!sameRun) {
                    newsRealHighWater = 0;
                    newsRealPerfAnchor = presenterNow;
                    visibleNews.clear();
                    homeNewsSignature = '';
                }
                const displayedTimestamp = anchorGameTimestamp + Math.max(0, presenterNow - anchorPerfNow) * anchorRate;
                const domainTimestamp = Number(snap.clock.timestamp) || anchorGameTimestamp;
                anchorGameTimestamp = sameRun && !snap.clock.isPaused ? Math.max(domainTimestamp, displayedTimestamp) : domainTimestamp;
                clockRunId = snap.runId;
                anchorPerfNow = presenterNow;
                anchorRate = snap.clock.isPaused ? 0 : (Number.isFinite(Number(snap.clock.rate)) ? Number(snap.clock.rate) : 0);
                if (clockDisplay) clockDisplay.textContent = snap.clock.time;
                if (sessionLabel) sessionLabel.textContent = snap.clock.sessionLabel;
                if (sessionDot) sessionDot.className = 'status-dot ' + (snap.clock.isTradingOpen ? 'open' : 'halt');
                if (lastKnownSessionLabel !== null && snap.clock.sessionLabel && lastKnownSessionLabel !== snap.clock.sessionLabel) {
                    showGameToast('info', 'Sesi Bursa Berganti', `Sesi pasar kini beralih ke ${snap.clock.sessionLabel}.`, 'SESSION_CHANGE:' + snap.clock.sessionLabel + ':' + (snap.clock.date || ''));
                }
                lastKnownSessionLabel = snap.clock.sessionLabel;
                renderFlowingClock();
            }
            if (snap.offlineCatchupReport && snap.offlineCatchupReport.timestamp) {
                const catchupTs = snap.offlineCatchupReport.timestamp;
                const pnl = snap.offlineCatchupReport.pnl || 'Rp 0';
                showGameToast('info', 'Penyelarasan Waktu Offline', `Simulasi mengejar waktu offline. Perubahan nilai: ${pnl}.`, 'OFFLINE_CATCHUP:' + catchupTs);
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
            if (feed.success === false || !Array.isArray(feed.news)) throw new Error(feed.message || 'Kabar pasar belum dapat dimuat.');
            const allNews = feed.news;
            rememberNews(allNews);
            const signature = homeNewsFilter + JSON.stringify(allNews.map(n => [n.id, n.isPublished, n.headline, n.content, n.direction, n.estimatedRange]));
            if (signature === homeNewsSignature && container.childElementCount) { updateNewsEffects(); return; }
            homeNewsSignature = signature;
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
                top.append(node('span', `change-pill ${isGood ? 'positive' : isBad ? 'negative' : ''}`, `Perkiraan dampak: ${n.direction || 'belum tersedia'}${n.estimatedRange ? ' (' + n.estimatedRange + ')' : ''}`));
                const timing = node('div', 'flex-row');
                timing.append(newsEffectNode(n), node('span', '', 'Baca rincian'));
                if (n.confidence) timing.append(node('span', '', `Keyakinan: ${n.confidence}`));
                card.append(top, node('h3', '', n.headline), node('p', '', n.content ? n.content.substring(0, 110) + (n.content.length > 110 ? '…' : '') : 'Isi berita belum tersedia.'), timing);
                card.onclick = () => openNewsDetail(n);
                card.onkeydown = event => { if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); card.click(); } };
                container.appendChild(card);
            });
        } catch(e) {
            container.replaceChildren(node('p', 'notice', 'Gagal memuat kabar pasar: ' + e.message));
        }
    }

    function formatPositionMoney(value, currency) {
        const amount = Number(value);
        if (!Number.isFinite(amount)) return 'Belum tersedia';
        if (currency === 'USD') return '$' + amount.toLocaleString('en-US', {minimumFractionDigits:2, maximumFractionDigits:2});
        if (currency === 'IDR') return 'Rp ' + amount.toLocaleString('id-ID', {minimumFractionDigits:0, maximumFractionDigits:0});
        return `${currency || ''} ${amount.toLocaleString('id-ID')}`.trim();
    }

    function positionPnlDisplay(pos) {
        const amount = Number(pos.unrealizedPnLRaw || 0);
        const status = amount > 0 ? 'Untung' : amount < 0 ? 'Rugi' : 'Impas';
        const pnlClass = amount > 0 ? 'positive' : amount < 0 ? 'negative' : 'neutral';
        const sign = amount > 0 ? '+' : '';
        return {pnlClass, text:`${status} · ${sign}${pos.unrealizedPnL} (${sign}${pos.unrealizedPnLPercent || '0.00'}%)`};
    }

    function renderPositionCard(pos) {
        const card = node('article', 'position-card position-row');
        card.dataset.instrumentId = pos.instrumentId;
        card.tabIndex = 0;
        card.setAttribute('role', 'button');
        const lotInfo = pos.lotSize > 0 ? ` (${pos.lots} lot)` : '';
        const sharesText = `${Number(pos.quantity).toLocaleString('id-ID')} saham${lotInfo}`;
        card.setAttribute('aria-label', `Posisi ${pos.ticker || pos.instrumentId} ${pos.exchange || ''}, ${sharesText}, nilai ${pos.marketValue}, laba rugi ${pos.unrealizedPnL} (${pos.unrealizedPnLPercent || '0.00'}%)`);
        card.onclick = () => openPositionDetailModal(pos.instrumentId);
        card.onkeydown = e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); openPositionDetailModal(pos.instrumentId); } };

        const header = node('div', 'pos-card-header');
        const idCol = node('div', 'pos-id-col');
        const badge = node('span', 'pos-exchange-badge', pos.exchange || (pos.instrumentId.startsWith('US:') ? 'US' : 'IDX'));
        const tickerLine = node('div');
        tickerLine.append(node('strong', 'pos-ticker', pos.ticker || pos.instrumentId), badge);
        idCol.append(tickerLine);
        if (pos.name) idCol.append(node('small', 'pos-name', pos.name));

        const valCol = node('div', 'pos-val-col');
        valCol.append(node('strong', 'pos-market-val', pos.marketValue));
        const pnl = positionPnlDisplay(pos);
        valCol.append(node('span', `pos-pnl ${pnl.pnlClass}`, pnl.text));

        header.append(idCol, valCol);

        const details = node('div', 'pos-card-details');
        const quantityCell = node('div', 'pos-detail-cell');
        quantityCell.append(node('small', '', 'Jumlah'), node('span', 'pos-qty', sharesText));
        const averageCell = node('div', 'pos-detail-cell');
        averageCell.append(node('small', '', 'Rata-rata'), node('span', 'pos-avg', formatPositionMoney(pos.averageCost, pos.currency)));
        const marketPriceCell = node('div', 'pos-detail-cell');
        marketPriceCell.append(node('small', '', 'Harga pasar'), node('span', 'pos-cur', formatPositionMoney(pos.currentPrice, pos.currency)));
        details.append(quantityCell, averageCell, marketPriceCell);

        card.append(header, details);
        return card;
    }

    function updatePositionCard(card, pos) {
        const valEl = card.querySelector('.pos-market-val');
        if (valEl && valEl.textContent !== pos.marketValue) valEl.textContent = pos.marketValue;

        const pnlEl = card.querySelector('.pos-pnl');
        if (pnlEl) {
            const pnl = positionPnlDisplay(pos);
            pnlEl.className = `pos-pnl ${pnl.pnlClass}`;
            if (pnlEl.textContent !== pnl.text) pnlEl.textContent = pnl.text;
        }

        const qtyEl = card.querySelector('.pos-qty');
        const lotInfo = pos.lotSize > 0 ? ` (${pos.lots} lot)` : '';
        const sharesText = `${Number(pos.quantity).toLocaleString('id-ID')} saham${lotInfo}`;
        if (qtyEl && qtyEl.textContent !== sharesText) qtyEl.textContent = sharesText;

        const avgEl = card.querySelector('.pos-avg');
        const avgText = formatPositionMoney(pos.averageCost, pos.currency);
        if (avgEl && avgEl.textContent !== avgText) avgEl.textContent = avgText;

        const curEl = card.querySelector('.pos-cur');
        const curText = formatPositionMoney(pos.currentPrice, pos.currency);
        if (curEl && curEl.textContent !== curText) curEl.textContent = curText;

        card.setAttribute('aria-label', `Posisi ${pos.ticker || pos.instrumentId} ${pos.exchange || ''}, ${sharesText}, nilai ${pos.marketValue}, laba rugi ${pos.unrealizedPnL} (${pos.unrealizedPnLPercent || '0.00'}%)`);
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
                if (key === 'averageCost' || key === 'currentPrice') val = formatPositionMoney(position[key], position.currency);
                if (key === 'quantity') {
                    const lotInfo = position.lotSize > 0 && Number(position.quantity) > 0 ? ` (${position.lots} lot)` : '';
                    val = `${position.quantity} saham${lotInfo}`;
                }
                flowText(panel, label, val);
            });
            flowText(panel, 'Catatan', isClosed
                ? 'Posisi telah ditutup sepenuhnya. Nilai laba/rugi terealisasi di atas belum dikurangi biaya transaksi.'
                : 'Nilai laba/rugi terealisasi di atas belum dikurangi biaya transaksi.');

            // Tombol tindakan ke tiket order / pasar
            flowButton(panel, 'Beli / Jual saham ini', () => {
                const stock = {
                    id: position.instrumentId,
                    ticker: position.ticker || position.instrumentId.split(':')[1] || position.instrumentId,
                    exchange: position.exchange || (position.instrumentId.startsWith('US:') ? 'US' : 'IDX'),
                    lotSize: position.lotSize || 100,
                    currency: position.currency || 'IDR',
                    currentPriceRaw: position.currentPrice,
                    currentPrice: formatMoney(Number(position.currentPrice), position.currency)
                };
                openOrderTicket(stock, null);
            });
        } catch(e) {
            console.warn('Gagal membuka detail posisi:', e);
        }
    }

    function renderPortfolioEmpty(parent) {
        emptyState(parent, 'Belum ada posisi', 'Pilih saham di Pasar dan tinjau order pertamamu.');
        const search = node('button', 'btn-primary', 'Cari saham');
        search.type = 'button';
        search.addEventListener('click', () => switchTab('pasar'));
        parent.querySelector('.empty-state').append(search);
    }

    function setPortfolioDataStale(stale) {
        const existing = document.getElementById('portfolio-data-status');
        if (!stale) { existing?.remove(); return; }
        if (existing || document.getElementById('asset-summary')?.dataset.portfolioLoaded !== 'true') return;
        const notice = node('section', 'notice');
        notice.id = 'portfolio-data-status';
        notice.setAttribute('role', 'status');
        notice.append(node('p', '', 'Portofolio belum dapat diperbarui. Angka yang tampil adalah snapshot terakhir yang berhasil dimuat.'));
        const retry = node('button', 'btn-primary', 'Coba perbarui portofolio');
        retry.type = 'button';
        retry.addEventListener('click', () => syncPortfolioLive());
        notice.append(retry);
        document.querySelector('#view-aset .page-heading')?.after(notice);
    }

    function syncPortfolioLive() {
        if (currentTab !== 'aset') return;
        const posContainer = document.getElementById('portfolio-positions-list');
        const summary = document.getElementById('asset-summary');
        if (!posContainer || !summary || !currentAccount || !bridge.getPortfolio) return;
        try {
            const portfolio = JSON.parse(bridge.getPortfolio(currentAccount.id));
            if (!portfolio || !portfolio.success) { setPortfolioDataStale(true); return; }
            setPortfolioDataStale(false);
            if (summary.dataset.portfolioLoaded !== 'true') { loadAsetView(); return; }

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
                    if (span.textContent === 'Laba/Rugi Mengambang (Unrealized)') strong.textContent = portfolio.totalUnrealizedPnL;
                    if (span.textContent === 'Hasil terealisasi sebelum biaya') strong.textContent = portfolio.totalRealizedPnL;
                    if (span.textContent === 'Posisi saham (valuasi IDR)') strong.textContent = portfolio.positionsValueIdr;
                    if (span.textContent === 'Kewajiban') strong.textContent = portfolio.liabilities;
                    if (span.textContent === 'Kurs valuasi USD/IDR' && portfolio.valuationRateUsdIdr) strong.textContent = portfolio.valuationRateUsdIdr;
                });
            }

            // Reconcile Positions List in place
            if (portfolio.positions) {
                const activePositions = portfolio.positions.filter(p => Number(p.quantity) > 0);
                const currentCards = posContainer.querySelectorAll('.position-card, .position-row');
                if (activePositions.length === 0) {
                    if (currentCards.length > 0 || !posContainer.querySelector('.empty-state')) {
                        posContainer.replaceChildren();
                        renderPortfolioEmpty(posContainer);
                    }
                } else {
                    const empty = posContainer.querySelector('.empty-state');
                    if (empty) empty.remove();
                    const existingCards = Array.from(currentCards);
                    const currentIds = existingCards.map(c => c.dataset.instrumentId);
                    const activeIds = activePositions.map(p => p.instrumentId);
                    const cardsById = new Map(existingCards.map(card => [card.dataset.instrumentId, card]));
                    const isSameList = currentIds.length === activeIds.length && currentIds.every((id, idx) => id === activeIds[idx]);
                    if (isSameList) {
                        activePositions.forEach(pos => {
                            const card = cardsById.get(pos.instrumentId);
                            if (card) updatePositionCard(card, pos);
                        });
                    } else {
                        const focusedInstrumentId = document.activeElement?.closest('.position-card')?.dataset.instrumentId;
                        const nextCards = activePositions.map(pos => {
                            const card = cardsById.get(pos.instrumentId) || renderPositionCard(pos);
                            if (cardsById.has(pos.instrumentId)) updatePositionCard(card, pos);
                            return card;
                        });
                        posContainer.replaceChildren(...nextCards);
                        const focusedCard = nextCards.find(card => card.dataset.instrumentId === focusedInstrumentId);
                        if (focusedCard) focusedCard.focus({preventScroll:true});
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
                const zeroVal = formatPositionMoney(0, isUsd ? 'USD' : 'IDR');

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
                        if (lbl === 'Jumlah saham') str.textContent = `${Number(curPos.quantity).toLocaleString('id-ID')} saham${curPos.lotSize > 0 && Number(curPos.quantity) > 0 ? ` (${curPos.lots} lot)` : ''}`;
                        if (lbl === 'Mata uang') str.textContent = String(curPos.currency);
                        if (lbl === 'Harga / saham') str.textContent = formatPositionMoney(curPos.currentPrice, curPos.currency);
                        if (lbl === 'Biaya rata-rata / saham') str.textContent = formatPositionMoney(curPos.averageCost, curPos.currency);
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
            setPortfolioDataStale(true);
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

    function receiptRow(panel, label, value) {
        let row = Array.from(panel.querySelectorAll('.data-row')).find(item => item.querySelector('span')?.textContent === label);
        if (!row) row = flowText(panel, label, value);
        else {
            const strong = row.querySelector('strong');
            if (strong) strong.textContent = value;
        }
    }

    function renderReceiptAmounts(panel, receipt) {
        const currency = receipt.currency === 'USD' ? 'USD' : 'IDR';
        const amount = raw => formatMoney(Number(raw), currency);
        if (receipt.averageFillPriceRaw != null && Number(receipt.filledQuantity) > 0)
            receiptRow(panel, 'Harga rata-rata terisi', amount(receipt.averageFillPriceRaw));
        if (receipt.filledValueRaw != null && Number(receipt.filledQuantity) > 0)
            receiptRow(panel, 'Nilai transaksi terisi', amount(receipt.filledValueRaw));
        if (receipt.feeDataAvailable) {
            receiptRow(panel, 'Biaya transaksi aktual', amount(receipt.actualFeeRaw));
            receiptRow(panel, receipt.cashImpactDirection === 'INFLOW' ? 'Kas bersih diterima' : 'Kas bersih dibayarkan', amount(receipt.cashImpactRaw));
        } else if (Number(receipt.filledQuantity) > 0) {
            receiptRow(panel, 'Biaya transaksi aktual', 'Tidak tersedia pada data transaksi lama');
        }
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
            renderReceiptAmounts(flowPanel, statusObj);
            if (statusObj.status === 'CANCELLED') {
                receiptRow(flowPanel, 'Sisa dibatalkan', `${statusObj.cancelledQuantity ?? statusObj.remainingQuantity ?? 0} saham belum terisi; fill sebelumnya tetap tercatat.`);
            }

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
                        } else if (statusObj.status === 'PENDING_TRIGGER') {
                            showGameToast('info', 'Order Pemicu Aktif', `${statusObj.instrumentId}: Menunggu kondisi harga pemicu terpenuhi`, 'RECEIPT_TRIGGER:' + reqId);
                        } else if (statusObj.status === 'CANCELLED') {
                            showGameToast('info', 'Order Dibatalkan', `${statusObj.instrumentId}: Sisa order dibatalkan`, 'RECEIPT_CANCEL:' + reqId);
                        } else if (statusObj.status === 'EXPIRED') {
                            showGameToast('info', 'Order Kedaluwarsa', `${statusObj.instrumentId}: Masa berlaku order telah berakhir`, 'RECEIPT_EXPIRED:' + reqId);
                        } else if (statusObj.status === 'REJECTED') {
                            showGameToast('info', 'Order Ditolak', `${statusObj.instrumentId}: Order ditolak bursa`, 'RECEIPT_REJECTED:' + reqId);
                        }
                        if (!['OPEN', 'PARTIALLY_FILLED', 'PENDING_TRIGGER'].includes(statusObj.status)) {
                            flowPanel.querySelectorAll('button').forEach(b => {
                                if (b.textContent.includes('Batalkan')) b.remove();
                            });
                        }
                    }
                }
                if (label === 'Terisi / sisa' || label === 'Terisi / tidak terisi') {
                    const cancelled = statusObj.status === 'CANCELLED';
                    r.querySelector('span').textContent = cancelled ? 'Terisi / tidak terisi' : 'Terisi / sisa';
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

    function updateIssuerOwnership(panel, stockId) {
        if (!panel?.isConnected) return;
        const status = panel.querySelector('.ownership-status');
        const meta = panel.querySelector('.ownership-meta');
        const list = panel.querySelector('.ownership-list');
        try {
            const snapshot = bridgeResult('getIssuerOwnership', stockId);
            if (snapshot.instrumentId !== stockId) throw new Error('Identitas emiten tidak cocok');
            panel.dataset.coverage = snapshot.coverage;
            panel.dataset.refreshCount = String(Number(panel.dataset.refreshCount || 0) + 1);
            status.removeAttribute('role');
            status.textContent = snapshot.coverage === 'INCONSISTENT' ? 'Perlu rekonsiliasi: jumlah tercatat melebihi saham beredar.'
                : snapshot.coverage === 'UNKNOWN' ? 'Porsi belum tersedia: penyebut saham beredar belum sah atau belum direkonsiliasi setelah aksi korporasi.'
                : snapshot.coverage === 'COMPLETE' ? 'Seluruh saham beredar sudah terpetakan pada akun pemegang.'
                : 'Parsial: belum seluruh saham beredar terpetakan.';
            meta.replaceChildren();
            flowText(meta, 'Saham beredar', Number(snapshot.sharesOutstanding) > 0 ? `${Number(snapshot.sharesOutstanding).toLocaleString('id-ID')} saham` : 'Belum tersedia');
            if (snapshot.issuedShares != null) flowText(meta, 'Saham diterbitkan', `${Number(snapshot.issuedShares).toLocaleString('id-ID')} saham`);
            if (Number(snapshot.treasuryShares) > 0) flowText(meta, 'Treasury · di luar saham beredar', `${Number(snapshot.treasuryShares).toLocaleString('id-ID')} saham`);
            flowText(meta, 'Saham tercatat', `${Number(snapshot.mappedShares).toLocaleString('id-ID')} saham`);
            if (snapshot.unknownLegacyShares != null) flowText(meta, 'Pemegang legacy belum diketahui', `${Number(snapshot.unknownLegacyShares).toLocaleString('id-ID')} saham`);
            if (snapshot.unmappedShares != null) flowText(meta, 'Belum terpetakan', `${Number(snapshot.unmappedShares).toLocaleString('id-ID')} saham`);
            if (Number(snapshot.recordedShortShares) > 0) flowText(meta, 'Saham dipinjam · kewajiban short', `${Number(snapshot.recordedShortShares).toLocaleString('id-ID')} saham; bukan pemegang tambahan`);
            flowText(meta, 'Waktu game', new Date(snapshot.asOf).toLocaleString('id-ID', {timeZone:'Asia/Jakarta'}));
            const oldRows = new Map(Array.from(list.querySelectorAll('[data-holder-id]'), row => [row.dataset.holderId, row]));
            const holders = snapshot.holders || [];
            const rows = holders.map(holder => {
                const row = oldRows.get(holder.holderId) || node('div', 'data-row ownership-holder');
                row.dataset.holderId = holder.holderId;
                if (!row.firstChild) row.append(node('span'), node('strong'));
                row.firstChild.textContent = holder.name + (holder.isTreasury ? ' · Treasury' : holder.isPersonalPlayer ? ' · Saya' : holder.isClientAccount ? ' · Akun klien' : '');
                row.lastChild.textContent = `${Number(holder.quantity).toLocaleString('id-ID')} saham${holder.percent == null ? '' : ' · ' + Number(holder.percent).toLocaleString('id-ID', {maximumFractionDigits:8}) + '%'}`;
                return row;
            });
            if (!holders.length) {
                list.replaceChildren();
                emptyState(list, 'Belum ada pemegang yang terpetakan', 'Daftar hanya memuat pemegang yang tercatat, bukan pemilik rekaan.');
            } else if (rows.length !== list.children.length || rows.some((row, index) => row !== list.children[index])) list.replaceChildren(...rows);
        } catch (error) {
            status.setAttribute('role', 'alert');
            status.textContent = `${panel.dataset.refreshCount ? 'Daftar belum dapat diperbarui; data terakhir tetap ditampilkan.' : 'Pemegang saham gagal dimuat.'} ${error.message}`;
        }
        panel.removeAttribute('aria-busy');
    }

    function mountIssuerOwnership(parent, stockId) {
        const panel = node('section', 'issuer-ownership');
        panel.id = 'issuer-ownership-panel';
        panel.dataset.instrumentId = stockId;
        panel.setAttribute('aria-label', 'Pemegang saham dunia game');
        panel.setAttribute('aria-busy', 'true');
        const status = node('p', 'notice ownership-status', 'Memuat pemegang saham…');
        status.setAttribute('role', 'status');
        const retry = node('button', 'btn-secondary', 'Perbarui pemegang saham');
        retry.type = 'button';
        retry.addEventListener('click', () => updateIssuerOwnership(panel, stockId));
        panel.append(status, node('div', 'ownership-meta'), node('div', 'ownership-list'), retry);
        parent.append(panel);
        requestAnimationFrame(() => requestAnimationFrame(() => updateIssuerOwnership(panel, stockId)));
    }

    function updateInstrumentDetailLive(stockId) {
        if (!flowPanel || !stockId) return;
        const detailAttr = flowPanel.getAttribute('data-stock-id') || flowPanel.closest('.flow-page')?.getAttribute('data-stock-id');
        if (detailAttr !== stockId) return;
        const ownershipPanel = flowPanel.querySelector('#issuer-ownership-panel');
        if (ownershipPanel && !ownershipPanel.hasAttribute('aria-busy')) updateIssuerOwnership(ownershipPanel, stockId);
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

    function selectMarketMovers(stocks) {
        const valid = [...new Map(stocks.filter(stock => stock.id && stock.dailyChangePercent !== null
            && stock.dailyChangePercent !== '' && Number.isFinite(Number(stock.dailyChangePercent)))
            .map(stock => [stock.id, stock])).values()];
        const tie = (a, b) => String(a.ticker).localeCompare(String(b.ticker)) || String(a.id).localeCompare(String(b.id));
        return {
            gainers: valid.filter(stock => Number(stock.dailyChangePercent) > 0)
                .sort((a, b) => Number(b.dailyChangePercent) - Number(a.dailyChangePercent) || tie(a, b)).slice(0, 3),
            losers: valid.filter(stock => Number(stock.dailyChangePercent) < 0)
                .sort((a, b) => Number(a.dailyChangePercent) - Number(b.dailyChangePercent) || tie(a, b)).slice(0, 3)
        };
    }

    function renderMarketHighlights(stocks) {
        const highlights = document.getElementById('market-highlights');
        const activeId = highlights.querySelector(':focus')?.dataset.instrumentId;
        const groups = selectMarketMovers(stocks);
        highlights.replaceChildren();
        [['gainers', '3 Penguat'], ['losers', '3 Pelemah']].forEach(([key, title]) => {
            const section = node('section', 'highlight-group');
            section.append(node('h3', '', title));
            groups[key].forEach((stock, index) => {
                const button = node('button', 'highlight' + (key === 'losers' ? ' loss' : ''));
                button.type = 'button'; button.dataset.instrumentId = stock.id;
                const percent = Number(stock.dailyChangePercent);
                button.append(node('span', '', `${index + 1}. ${stock.ticker}`), node('strong', '', `${percent > 0 ? '+' : ''}${percent.toLocaleString('id-ID', {maximumFractionDigits: 2})}%`));
                button.setAttribute('aria-label', `Detail ${stock.ticker} ${stock.exchange || ''}, ${percent > 0 ? 'naik' : 'turun'} ${Math.abs(percent)} persen`);
                button.onclick = () => openInstrumentDetail(stock); section.append(button);
            });
            if (groups[key].length < 3) section.append(node('p', 'small muted', groups[key].length
                ? `Hanya ${groups[key].length} emiten ${key === 'gainers' ? 'menguat' : 'melemah'} di daftar ini.`
                : `Belum ada ${key === 'gainers' ? 'penguat' : 'pelemah'} di daftar ini.`));
            highlights.append(section);
        });
        if (activeId) [...highlights.querySelectorAll('button')].find(button => button.dataset.instrumentId === activeId)?.focus({preventScroll: true});
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

        renderMarketHighlights(stocks);
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
                        ${st.isSectorLocked ? `<span class="badge-locked" style="background:#FEF9C3; color:#854D0E; border:1px solid #CA8A04; border-radius:6px; padding:2px 6px; font-size:0.65rem; font-weight:700;">🔒 Lv.${st.requiredLevel}</span>` : ''}
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
            if (currentTab === 'pasar') {
                const query = document.getElementById('search-stock-input').value;
                const exchange = document.getElementById('filter-exchange-select').value;
                const sector = document.getElementById('filter-sector-select').value;
                const filtered = JSON.parse(bridge.getInstruments(query, exchange, sector || null, true));
                const watchlistOnly = document.getElementById('filter-list-select').value === 'watchlist';
                renderMarketHighlights(watchlistOnly ? filtered.filter(stock => stock.isWatchlist) : filtered);
            }
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
        document.querySelectorAll('#portfolio-data-status, [data-portfolio-load-error]').forEach(el => el.remove());
        const posContainer = document.getElementById('portfolio-positions-list');
        posContainer.replaceChildren();
        document.getElementById('asset-tools')?.remove();
        let summary = document.getElementById('asset-summary');
        if (!summary) { summary = document.querySelector('#view-aset .card'); summary.id = 'asset-summary'; }
        summary.replaceChildren(); summary.className = 'asset-overview';
        const categoryHost = views.aset.querySelector('#portfolio-categories') || node('section', 'card portfolio-categories');
        categoryHost.id = 'portfolio-categories';
        if (!categoryHost.isConnected) views.aset.append(categoryHost);
        const positionsSection = posContainer.closest('.card');
        const pageHeading = views.aset.querySelector('.page-heading');
        if (pageHeading) {
            pageHeading.after(summary, positionsSection, categoryHost);
        } else {
            views.aset.append(summary, positionsSection, categoryHost);
        }
        let activePortfolioCategory = null;
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
                        showGameToast('info', 'Akun Aktif Beralih', `Sekarang mengelola ${chosen.name} (${chosen.type === 'CLIENT' ? 'Akun Klien' : 'Akun Pribadi'}).`);
                        loadAsetView();
                    }
                };
                summary.appendChild(accSelect);
            }

            let portfolio = null;
            let portfolioError = null;
            try {
                portfolio = JSON.parse(bridge.getPortfolio(currentAccount.id));
                if (!portfolio || !portfolio.success) throw new Error(portfolio?.message || 'Snapshot akun gagal');
            } catch (error) {
                portfolioError = error;
            }
            summary.dataset.portfolioLoaded = portfolioError ? 'false' : 'true';
            if (portfolioError) {
                summary.textContent = 'Ringkasan nilai dan kas belum tersedia karena data portofolio gagal dimuat.';
                emptyState(posContainer, 'Portofolio belum dapat dimuat', 'Coba muat ulang; data ini berbeda dari keadaan tanpa posisi.');
            } else {
                const title = node('div','section-heading'); title.append(node('h2','',portfolio.accountType === 'CLIENT' ? 'Akun klien' : 'Kekayaan saya'),node('span','live-badge',portfolio.accountType === 'CLIENT' ? 'Dana terpisah' : 'Pribadi')); summary.append(title);
                metricCard(summary, 'NILAI BERSIH · IDR', portfolio.netWorth || (portfolio.accountType === 'PERSONAL' ? latestSnapshot.personalAccount.netWorth : 'Belum tersedia'),portfolio.accountName);
                const allocation = node('section','card'); allocation.append(node('h2','card-title','Komposisi aset'));
                flowText(allocation,'Kas (valuasi IDR)',portfolio.cashValueIdr);
                flowText(allocation,'Posisi saham (valuasi IDR)',portfolio.positionsValueIdr);
                if (portfolio.totalUnrealizedPnL) {
                    flowText(allocation, 'Laba/Rugi Mengambang (Unrealized)', portfolio.totalUnrealizedPnL);
                }
                if (portfolio.totalRealizedPnL) {
                    flowText(allocation, 'Hasil terealisasi sebelum biaya', portfolio.totalRealizedPnL);
                }
                if (portfolio.positionsSharePercent != null) {
                    const bar = node('div','allocation-bar'); bar.setAttribute('role','img'); bar.setAttribute('aria-label',`${portfolio.positionsSharePercent}% aset berupa posisi saham`);
                    const fill = node('span'); fill.style.width = `${Math.max(0,Math.min(100,Number(portfolio.positionsSharePercent)))}%`; bar.append(fill); allocation.append(bar);
                }
                flowText(allocation,'Kewajiban',portfolio.liabilities);
                summary.append(allocation);
                const wallets = node('details','card'); wallets.append(node('summary','','Dompet IDR & USD'));
                flowText(wallets, 'Total Kekayaan Bersih Konsolidasi (A21)', portfolio.netWorth || (latestSnapshot && latestSnapshot.personalAccount ? latestSnapshot.personalAccount.netWorth : 'Belum tersedia'));
                flowText(wallets, 'Total Valuasi Seluruh Kas', portfolio.cashValueIdr);
                for (const currency of ['IDR', 'USD']) {
                    const cash = portfolio.cash[currency];
                    flowText(wallets, `${currency} total`, cash.total);
                    flowText(wallets, `${currency} tersedia`, cash.available);
                    flowText(wallets, `${currency} tertahan`, cash.reserved);
                }
                const btnAlloc = node('button', 'btn-secondary', 'Lihat Rincian Alokasi Dana Tertahan (A20)');
                btnAlloc.style.width = '100%';
                btnAlloc.style.marginTop = '8px';
                btnAlloc.style.minHeight = '48px';
                btnAlloc.onclick = () => openReservedCashAllocationModal();
                wallets.append(btnAlloc);
                summary.append(wallets);

                const activePositions = (portfolio.positions || []).filter(p => Number(p.quantity) > 0);
                if (!activePositions.length) {
                    renderPortfolioEmpty(posContainer);
                } else {
                    posContainer.replaceChildren(...activePositions.map(renderPositionCard));
                }
            }

            // N11/N17: satu peta route kategori eksplisit; membuka kategori hanya navigasi.
            document.getElementById('asset-tools')?.remove();
            const categories = [
                { id: 'saham-order', title: 'Saham & Order', desc: 'Posisi saham, order aktif, dan riwayat transaksi saham', icon: '📈', routes: [
                    { id: 'order-history', label: 'Order aktif & riwayat', run: () => openPortfolioOrders() },
                    { id: 'corporate-actions', label: 'Aksi korporasi & voting RUPS', run: () => openCorporateActionModal() },
                    { id: 'historical-replay', label: 'Replay historis pasar', run: () => openHistoricalReplayModal() }
                ] },
                { id: 'dompet-valuta', title: 'Dompet & Valuta', desc: 'Saldo IDR/USD, dana tertahan, pertukaran valuta, dan riwayat kas', icon: '💵', routes: [
                    { id: 'fx', label: 'Tukar IDR / USD', run: () => openFxForm() },
                    { id: 'account-ledger', label: 'Riwayat kas & transaksi akun', run: () => openTransactionHistoryModal() }
                ] },
                { id: 'investasi-lain', title: 'Investasi Lain', desc: 'Reksa dana, obligasi, deposito, dan komoditas', icon: '🏛️', routes: [
                    { id: 'my-funds', label: 'Reksa dana saya', run: () => openMyMutualFundsModal() },
                    { id: 'multi-assets', label: 'Obligasi, deposito & komoditas', run: () => openMultiAssets() }
                ] },
                { id: 'kewajiban-proteksi', title: 'Kewajiban & Proteksi', desc: 'Pinjaman, margin, perlindungan, dan laporan kepatuhan', icon: '🛡️', routes: [
                    { id: 'loans', label: 'Utang & pinjaman tenor', run: () => openLoansModal() },
                    { id: 'turnaround', label: 'Restrukturisasi darurat', run: () => executeBailoutAction() },
                    { id: 'risk', label: 'Fasilitas margin & risiko', run: () => openRiskModal() },
                    { id: 'insurance', label: 'Asuransi portofolio', run: () => openPortfolioInsuranceModal() },
                    { id: 'tax', label: 'Audit pajak & kepatuhan', run: () => openTaxAuditModal() },
                    { id: 'wealth', label: 'Rincian kekayaan bersih konsolidasi', run: () => openConsolidatedWealthModal() }
                ] },
                { id: 'properti-koleksi', title: 'Properti & Koleksi', desc: 'Koleksi dan aset properti dengan valuasi', icon: '💎', routes: [
                    { id: 'collections', label: 'Galeri barang koleksi & properti', run: () => openCollectionsGalleryModal() }
                ] },
                { id: 'bisnis-dana-kelolaan', title: 'Usaha & Dana Kelolaan', desc: 'Shortcut usaha, klien, dan dana ke halaman Bisnis', icon: '🏢', routes: [
                    { id: 'business', label: 'Perusahaan operasional', run: () => { switchTab('firma'); loadFirmaView('usaha'); } },
                    { id: 'managed-funds', label: 'Dana kelolaan', run: () => { switchTab('firma'); loadFirmaView('dana'); } },
                    { id: 'fund-builder', label: 'Racik dana kelolaan', run: () => openCustomFundBuilderModal() }
                ] }
            ];
            const categoryChildren = {
                'investasi-lain': [
                    { id: 'reksa-dana', title: 'Reksa Dana', desc: 'Kepemilikan unit/NAB dan transaksi akun terpilih', routes: ['my-funds'] },
                    { id: 'obligasi-deposito-komoditas', title: 'Obligasi, Deposito & Komoditas', desc: 'Produk investasi non-ekuitas di katalog multi-aset', routes: ['multi-assets'] }
                ],
                'kewajiban-proteksi': [
                    { id: 'pinjaman-cicilan', title: 'Pinjaman & Cicilan', desc: 'Pinjaman tenor, agunan, jadwal, dan restrukturisasi', routes: ['loans','turnaround'] },
                    { id: 'margin-proteksi', title: 'Margin & Proteksi', desc: 'Margin health, pinjaman saham, dan asuransi yang berlaku', routes: ['risk','insurance'] },
                    { id: 'pajak-laporan', title: 'Pajak & Laporan', desc: 'Audit, ledger, dan laporan kepatuhan akun', routes: ['tax','wealth'] }
                ]
            };
            const catSection = categoryHost;
            const renderCategoryLanding = () => {
                activePortfolioCategory = null;
                catSection.dataset.view = 'landing';
                catSection.removeAttribute('data-category-id');
                catSection.replaceChildren(node('h2', 'card-title', 'Kategori Portofolio'));
                categories.forEach(cat => {
                    const button = node('button', 'category-launcher-item');
                    button.type = 'button';
                    button.dataset.categoryId = cat.id;
                    button.setAttribute('aria-label', `${cat.title}: ${cat.desc}`);
                    button.setAttribute('aria-expanded', 'false');
                    const body = node('span', 'cat-body');
                    body.append(node('strong', '', cat.title), node('small', '', cat.desc));
                    button.append(
                        node('span', 'cat-icon', cat.icon),
                        body,
                        node('span', 'cat-chevron', '›')
                    );
                    button.addEventListener('click', () => categoryChildren[cat.id]
                        ? renderSubcategoryPage(cat)
                        : renderCategoryPage(cat, cat.routes));
                    catSection.append(button);
                });
            };
            const addCategoryBack = (label, onBack) => {
                const back = node('button', 'portfolio-category-back', `‹  ${label}`);
                back.type = 'button';
                back.setAttribute('aria-label', label);
                back.addEventListener('click', onBack);
                return back;
            };
            const restoreCategoryScroll = () => {
                document.querySelector('.app-scroll').scrollTop = scrollPositions.aset || 0;
            };
            const renderSubcategoryPage = cat => {
                activePortfolioCategory = cat.id;
                catSection.dataset.view = 'category';
                catSection.dataset.categoryId = cat.id;
                catSection.removeAttribute('data-subcategory-id');
                catSection.replaceChildren(
                    addCategoryBack('Kembali ke Portofolio', () => {
                        renderCategoryLanding();
                        restoreCategoryScroll();
                        catSection.querySelector(`.category-launcher-item[data-category-id="${cat.id}"]`)?.focus({preventScroll:true});
                    }),
                    node('h2', 'card-title', cat.title),
                    node('p', 'portfolio-category-description', cat.desc)
                );
                categoryChildren[cat.id].forEach(child => {
                    const button = node('button', 'category-launcher-item');
                    button.type = 'button';
                    button.dataset.subcategoryId = child.id;
                    button.setAttribute('aria-label', `${child.title}: ${child.desc}`);
                    button.setAttribute('aria-expanded', 'false');
                    const body = node('span', 'cat-body');
                    body.append(node('strong', '', child.title), node('small', '', child.desc));
                    button.append(body, node('span', 'cat-chevron', '›'));
                    button.addEventListener('click', () => renderCategoryPage(
                        cat, child.routes.map(id => cat.routes.find(route => route.id === id)), child));
                    catSection.append(button);
                });
                catSection.querySelector('.portfolio-category-back').focus({preventScroll:true});
                const main = document.querySelector('.app-scroll');
                main.scrollTop = Math.min(main.scrollTop, catSection.offsetTop);
            };
            const renderCategoryPage = (cat, routes, subcategory = null) => {
                activePortfolioCategory = cat.id;
                catSection.dataset.view = 'category';
                catSection.dataset.categoryId = cat.id;
                if (subcategory) catSection.dataset.subcategoryId = subcategory.id;
                else catSection.removeAttribute('data-subcategory-id');
                catSection.replaceChildren();
                const onBack = () => {
                    if (subcategory || categoryChildren[cat.id]) {
                        renderSubcategoryPage(cat);
                        if (subcategory) catSection.querySelector(`.category-launcher-item[data-subcategory-id="${subcategory.id}"]`)?.focus({preventScroll:true});
                        else catSection.querySelector('.category-launcher-item')?.focus({preventScroll:true});
                    } else {
                        renderCategoryLanding();
                        restoreCategoryScroll();
                        catSection.querySelector(`.category-launcher-item[data-category-id="${cat.id}"]`)?.focus({preventScroll:true});
                    }
                };
                catSection.append(
                    addCategoryBack(categoryChildren[cat.id] ? `Kembali ke ${cat.title}` : 'Kembali ke Portofolio', onBack),
                    node('h2', 'card-title', subcategory?.title || cat.title),
                    node('p', 'portfolio-category-description', subcategory?.desc || cat.desc)
                );
                routes.forEach(route => {
                    const button = node('button', 'portfolio-category-route');
                    button.append(node('span', 'portfolio-route-label', route.label), node('span', 'cat-chevron', '›'));
                    button.type = 'button';
                    button.dataset.portfolioRoute = route.id;
                    button.addEventListener('click', route.run);
                    catSection.append(button);
                });
                catSection.querySelector('.portfolio-category-back').focus({preventScroll:true});
                const main = document.querySelector('.app-scroll');
                main.scrollTop = Math.min(main.scrollTop, catSection.offsetTop);
            };
            renderCategoryLanding();
            if (portfolioError) {
                const errorMessage = node('p', 'notice', `Portofolio gagal dimuat: ${portfolioError.message}`);
                errorMessage.setAttribute('role', 'alert');
                const retry = node('button', 'btn-primary', 'Coba muat ulang portofolio');
                retry.type = 'button';
                retry.addEventListener('click', () => loadAsetView());
                errorMessage.dataset.portfolioLoadError = 'true';
                retry.dataset.portfolioLoadError = 'true';
                pageHeading?.after(errorMessage, retry);
            }
        } catch (error) { summary.textContent = 'Portofolio gagal dimuat: ' + error.message; }
    }

    function openPortfolioOrders() {
        refreshSnapshot();
        if (latestSnapshot && latestSnapshot.recoveryRequired) return;
        const panel = showFlow('Order Aktif & Riwayat');
        flowText(panel, 'Akun', currentAccount ? `${currentAccount.name} (${currentAccount.id})` : 'Belum tersedia');
        if (!bridge.getOrders || !currentAccount) { flowText(panel, 'Riwayat', 'Bridge riwayat domain belum tersedia.'); return; }
        
        const filterControls = node('div', 'filter-chips');
        filterControls.style.marginBottom = '12px';
        const labelActive = node('label');
        const activeOnly = document.createElement('input'); activeOnly.type = 'checkbox'; activeOnly.checked = true;
        labelActive.append(activeOnly, document.createTextNode(' Hanya order aktif'));
        filterControls.append(labelActive);
        panel.append(filterControls);

        const list = document.createElement('div');
        list.className = 'order-history-list';
        panel.append(list);

        const render = () => {
            list.replaceChildren();
            try {
                const orders = JSON.parse(bridge.getOrders(currentAccount.id, activeOnly.checked));
                if (!orders.length) {
                    emptyState(list, 'Belum ada order', activeOnly.checked ? 'Tidak ada order yang sedang aktif saat ini.' : 'Belum ada riwayat transaksi order.');
                    return;
                }
                orders.forEach(order => {
                    const card = node('article', 'card order-history-card');
                    card.style.marginBottom = '10px';
                    card.style.border = '1px solid var(--border)';
                    card.style.borderRadius = '10px';
                    card.style.padding = '12px 14px';

                    const isBuy = order.side === 'BUY';
                    const sidePillClass = isBuy ? 'badge-available' : 'badge-pledged';
                    const sideLabel = isBuy ? 'BELI' : 'JUAL';
                    const statusLabel = formatOrderStatus(order.status);

                    const topRow = node('div', 'flex-row');
                    topRow.style.justifyContent = 'space-between';
                    topRow.style.marginBottom = '6px';
                    topRow.innerHTML = `
                        <div style="display:flex; align-items:center; gap:8px;">
                            <strong style="font-size:1rem; color:var(--text);">${order.instrumentId}</strong>
                            <span class="${sidePillClass}" style="font-size:0.75rem; font-weight:700;">${sideLabel}</span>
                        </div>
                        <span class="badge-status" style="font-size:0.75rem;">${statusLabel}</span>
                    `;
                    card.append(topRow);

                    const infoRow = node('div');
                    infoRow.style.fontSize = '0.85rem';
                    infoRow.style.color = 'var(--text-secondary)';
                    infoRow.style.marginBottom = '8px';
                    infoRow.textContent = `Terisi: ${order.filledQuantity ?? 0} / ${order.quantity} saham · ${order.type === 'LIMIT' ? 'Limit ' + (order.limitPrice || '') : order.type}`;
                    card.append(infoRow);

                    const btnRow = node('div');
                    btnRow.style.display = 'flex';
                    btnRow.style.gap = '8px';

                    const btnReceipt = node('button', 'btn-secondary', 'Lihat Rincian');
                    btnReceipt.style.flex = '1';
                    btnReceipt.style.minHeight = '48px';
                    btnReceipt.onclick = () => {
                        activeRequestId = order.requestId;
                        orderDraft = null;
                        orderPreview = null;
                        showOrderReceipt(order);
                    };

                    const btnStock = node('button', 'btn-secondary', 'Buka di Pasar');
                    btnStock.style.flex = '1';
                    btnStock.style.minHeight = '48px';
                    btnStock.onclick = () => {
                        try {
                            const stocks = JSON.parse(bridge.getInstruments('', '', null, true));
                            const target = stocks.find(s => s.id === order.instrumentId || s.ticker === order.instrumentId);
                            if (target) {
                                openInstrumentDetail(target);
                            } else {
                                showGameToast('info', 'Saham Pasar', `Membuka pasar untuk ${order.instrumentId}`);
                                switchTab('pasar');
                            }
                        } catch(e) {
                            switchTab('pasar');
                        }
                    };

                    btnRow.append(btnReceipt, btnStock);
                    card.append(btnRow);
                    list.append(card);
                });
            } catch (error) { flowText(list, 'Riwayat gagal dimuat', error.message); }
        };
        activeOnly.addEventListener('change', render); render();
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

        let companies = [];
        if (bridge.getCompanies) {
            try {
                const compRes = JSON.parse(bridge.getCompanies());
                companies = compRes.companies || [];
                const comps = companies;
                if (comps.length === 0) {
                    const empty = node('div', 'empty-state');
                    empty.append(
                        node('strong', '', 'Belum memiliki perusahaan operasional'),
                        node('p', '', 'Dirikan anak usaha pertama untuk menghasilkan pendapatan harian dan mendiversifikasi aset bisnis di luar pasar modal. Modal pendirian minimal Rp 25.000.000 dari kas pribadi.')
                    );
                    const createCta = node('button', 'btn-primary', '+ Dirikan Perusahaan Baru (Min. Rp 25.000.000)');
                    createCta.type = 'button';
                    createCta.style.marginTop = '12px';
                    createCta.onclick = () => openFoundCompanyModal();
                    empty.append(createCta);
                    compSection.append(empty);
                } else {
                    const foundBtn = node('button', 'btn-primary', '+ Dirikan Perusahaan Baru');
                    foundBtn.type = 'button';
                    foundBtn.style.marginBottom = '14px';
                    foundBtn.onclick = () => openFoundCompanyModal();
                    compSection.append(foundBtn);

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
                        meta.append(
                            node('span', 'eyebrow', sectorIndo),
                            node('span', 'badge', c.level == null ? 'Level belum tersedia' : `Level ${c.level}`),
                            node('span', 'badge-status', c.isHalted ? 'Tersuspensi' : 'Operasional Normal')
                        );
                        cCard.append(meta, node('h3', '', c.name));
                        flowText(cCard, 'Status operasional', c.isHalted ? 'Tersuspensi / Butuh Perhatian' : 'Beroperasi Normal');
                        flowText(cCard, 'Kas operasional', formatMoney(c.cash));
                        const profitRow = flowText(cCard, 'Perkiraan laba bersih', Number.isFinite(netProfit) ? `${netProfit >= 0 ? '+' : ''}${formatMoney(netProfit)} / hari` : null);
                        if (Number.isFinite(netProfit) && profitRow) profitRow.querySelector('strong').className = netProfit >= 0 ? 'positive' : 'negative';
                        flowText(cCard, 'Staf & eksekutif', `${c.employeeCount == null ? 'Jumlah pegawai belum tersedia' : c.employeeCount + ' pegawai'} · CEO: ${c.ceoName || 'Belum tersedia'}`);

                        const btnOpen = document.createElement('button');
                        btnOpen.className = 'btn-primary';
                        btnOpen.style.marginTop = '12px';
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
        renderHoldingSection(compSection, companies);
        container.append(compSection);
    }

    function renderHoldingSection(container, companies) {
        const card = document.createElement('section');
        card.className = 'card';
        card.append(node('h2', 'card-title', 'Holding & anak usaha lintas sektor'));
        flowText(card, 'Kriteria pembentukan', 'Pilih empat perusahaan yang ada untuk membentuk satu grup: Finansial, Energi, Konsumer, dan Teknologi. Transfer/dividen internal memakai ledger dua sisi tanpa pajak bursa.');
        let holdings = [];
        try { holdings = bridge.getHoldingCompanies ? JSON.parse(bridge.getHoldingCompanies()) : []; }
        catch (error) { flowText(card, 'Holding gagal dimuat', error.message); }
        if (holdings.length) {
            holdings.forEach(holding => {
                const group = node('article', 'card');
                group.append(node('h3', '', holding.holdingName));
                holding.subsidiaries.forEach(sub => flowText(group, sub.sector, sub.companyName));
                flowButton(group, 'Injeksi modal / tarik dividen internal', () => openHoldingTransferModal(holding));
                card.append(group);
            });
        } else {
            flowText(card, 'Status grup', 'Belum ada holding. Perusahaan harus sudah berdiri sebelum dipilih sebagai anak usaha.');
            const create = document.createElement('button');
            create.className = 'btn-primary';
            create.type = 'button';
            create.textContent = 'Bentuk Holding 4 Sektor';
            create.disabled = companies.length < 4 || !bridge.createHoldingCompany;
            create.onclick = () => openHoldingCreationModal(companies);
            card.append(create);
        }
        container.append(card);
    }

    function openHoldingCreationModal(companies) {
        const panel = showFlow('Bentuk Holding Perusahaan');
        flowText(panel, 'Empat sektor wajib', 'Pilih satu anak usaha yang telah berdiri untuk setiap sektor. Satu perusahaan tidak dapat mengisi dua sektor.');
        const name = document.createElement('input');
        name.className = 'input-field';
        name.setAttribute('aria-label', 'Nama holding');
        name.placeholder = 'Nama holding';
        name.value = 'Nusantara Konglomerasi';
        panel.append(name);
        const sectors = [['FINANCIAL','Finansial'],['ENERGY','Energi'],['CONSUMER','Konsumer'],['TECHNOLOGY','Teknologi']];
        const selectors = [];
        for (const [id,label] of sectors) {
            const fieldLabel = document.createElement('label');
            fieldLabel.textContent = `Anak usaha sektor ${label}`;
            const select = document.createElement('select');
            select.className = 'input-field';
            select.setAttribute('aria-label', fieldLabel.textContent);
            select.append(new Option('Pilih perusahaan…',''));
            companies.forEach(company => select.append(new Option(`${company.name} · ${company.sector}`, company.id)));
            panel.append(fieldLabel, select);
            selectors.push({sector:id,select});
        }
        const status = node('p','notice','');
        flowButton(panel,'Bentuk Holding',() => {
            try {
                const values = selectors.map(item => item.select.value);
                if (!name.value.trim() || values.some(value => !value) || new Set(values).size !== 4)
                    throw new Error('Nama dan empat anak usaha berbeda wajib dipilih.');
                const subsidiaries = selectors.map(item => ({sector:item.sector,companyId:item.select.value}));
                const result = JSON.parse(bridge.createHoldingCompany(JSON.stringify({holdingId:`HOLD-${Date.now()}`,holdingName:name.value.trim(),subsidiaries})));
                if (result.success === false) throw new Error(result.message || 'Holding ditolak.');
                flowText(panel,'Status','Holding terbentuk dan tersimpan.');
                loadFirmaView('usaha');
            } catch(error) { status.textContent = error.message; }
        });
        panel.append(status);
    }

    function openHoldingTransferModal(holding) {
        const panel = showFlow(`Transfer Internal · ${holding.holdingName}`);
        flowText(panel,'Tujuan','Suntikkan modal atau pindahkan dividen antar anak usaha dalam holding yang sama. Satu hari biaya payroll/operasi tetap harus tersedia.');
        const from = document.createElement('select'), to = document.createElement('select'), kind = document.createElement('select');
        [from,to].forEach(select => {
            select.className = 'input-field';
            holding.subsidiaries.forEach(sub => select.append(new Option(`${sub.sector} · ${sub.companyName}`,sub.companyId)));
        });
        from.setAttribute('aria-label','Perusahaan sumber');
        to.setAttribute('aria-label','Perusahaan tujuan');
        kind.className = 'input-field';
        kind.setAttribute('aria-label','Jenis transfer internal');
        kind.append(new Option('Injeksi modal','CAPITAL_INJECTION'),new Option('Dividen internal','DIVIDEND'));
        const amount = document.createElement('input');
        amount.type='number'; amount.min='1'; amount.step='1'; amount.value='5000000'; amount.className='input-field'; amount.setAttribute('aria-label','Nominal IDR');
        panel.append(from,to,kind,amount);
        const status=node('p','notice','');
        flowButton(panel,'Tinjau & kirim transfer internal',() => {
            try {
                if (!from.value || !to.value || from.value===to.value || Number(amount.value)<=0) throw new Error('Pilih dua anak usaha berbeda dan nominal positif.');
                const result=JSON.parse(bridge.transferHoldingCapital(JSON.stringify({requestId:`HOLD-TRF-${crypto.randomUUID()}`,holdingId:holding.holdingId,fromCompanyId:from.value,toCompanyId:to.value,amount:String(amount.value),transferType:kind.value})));
                if (!result.success) throw new Error(result.message || 'Transfer holding ditolak.');
                flowText(panel,'Receipt',`${result.transferType} ${result.amount} terselesaikan antar anak usaha tanpa pajak bursa.`);
                refreshSnapshot();
            } catch(error) { status.textContent=error.message; }
        });
        panel.append(status);
    }

    function openCompanyDetailModal(company, activeSubtab = 'ringkasan') {
        const panel = showFlow(company.name);
        panel.setAttribute('data-company-detail-id', company.id);
        flowText(panel, 'Unit Usaha Operasional', `${company.name} · Sektor ${company.sectorModel || company.sector || 'Bisnis'}`);

        const navWrap = document.createElement('div');
        navWrap.className = 'company-local-nav';
        navWrap.style.display = 'flex';
        navWrap.style.gap = '8px';
        navWrap.style.margin = '10px 0 14px';
        navWrap.style.flexWrap = 'wrap';

        const subtabs = [
            { id: 'ringkasan', label: 'Ringkasan' },
            { id: 'tim', label: 'Tim & SDM' },
            { id: 'fasilitas', label: 'Fasilitas & moral' },
            { id: 'keuangan', label: 'Keuangan' }
        ];

        const contentBox = document.createElement('div');
        contentBox.className = 'company-subtab-box';

        function selectCompanySubtab(currentTabId) {
            navWrap.querySelectorAll('.subtab-pill').forEach(btn => {
                const isActive = btn.dataset.tabId === currentTabId;
                btn.classList.toggle('active', isActive);
                btn.setAttribute('aria-pressed', String(isActive));
            });
            renderCompanySubtabContent(currentTabId);
        }

        subtabs.forEach(tab => {
            const btn = document.createElement('button');
            btn.className = 'subtab-pill' + (activeSubtab === tab.id ? ' active' : '');
            btn.dataset.tabId = tab.id;
            btn.textContent = tab.label;
            btn.type = 'button';
            btn.onclick = () => selectCompanySubtab(tab.id);
            btn.setAttribute('aria-pressed', String(activeSubtab === tab.id));
            navWrap.appendChild(btn);
        });
        panel.append(navWrap, contentBox);

        function renderCompanySubtabContent(currentTabId) {
            contentBox.replaceChildren();

            let c = company;
            if (bridge.getCompanies) {
                try {
                    const compRes = JSON.parse(bridge.getCompanies());
                    const fresh = (compRes.companies || []).find(x => x.id === company.id);
                    if (fresh) c = fresh;
                } catch(e) {}
            }

            if (currentTabId === 'ringkasan') {
                const card = document.createElement('section');
                card.className = 'card';
                card.innerHTML = `<h2 class="card-title">Ringkasan Operasional Perusahaan</h2>`;
                flowText(card, 'Tingkat skala', `Level ${c.level} (${c.sectorModel || c.sector})`);
                flowText(card, 'Saldo kas perusahaan', `Rp ${Number(c.cash || 0).toLocaleString('id-ID')}`);
                flowText(card, 'CEO / pimpinan', c.ceoName);
                flowText(card, 'Proyeksi pendapatan', `+Rp ${Number(c.dailyRevenue || 0).toLocaleString('id-ID')} / hari`);
                flowText(card, 'Beban gaji (payroll)', `-Rp ${Number(c.dailyPayroll || 0).toLocaleString('id-ID')} / hari`);
                flowText(card, 'Biaya operasional & fasilitas', `-Rp ${Number(c.dailyOperatingCost || 0).toLocaleString('id-ID')} / hari`);
                const net = Number(c.dailyNetProfit != null ? c.dailyNetProfit : (Number(c.dailyRevenue || 0) - Number(c.dailyPayroll || 0) - Number(c.dailyOperatingCost || 0)));
                flowText(card, 'Laba bersih harian', `Rp ${net.toLocaleString('id-ID')} / hari`);

                const upBtn = document.createElement('button');
                upBtn.className = 'btn-primary';
                upBtn.style.marginTop = '12px';
                upBtn.textContent = `Tingkatkan skala ke level ${c.level + 1}`;
                upBtn.onclick = () => {
                    const request = { companyId: c.id, action: 'UPGRADE', workHours: c.workHours || 8, benefitTier: c.benefitTier || 'STANDARD' };
                    reviewCompanyAction(request);
                };
                card.append(upBtn);
                contentBox.append(card);

            } else if (currentTabId === 'tim') {
                const card = document.createElement('section');
                card.className = 'card';
                card.innerHTML = `<h2 class="card-title">Divisi SDM & Struktur Organisasi</h2>`;
                flowText(card, 'Total karyawan aktif', `${c.employeeCount || (c.employees ? c.employees.length : 0)} pegawai`);
                flowText(card, 'Pengeluaran payroll', `Rp ${Number(c.dailyPayroll || 0).toLocaleString('id-ID')} / hari`);

                const hireBtn = document.createElement('button');
                hireBtn.className = 'btn-primary';
                hireBtn.style.marginTop = '10px';
                hireBtn.textContent = 'Rekrut pegawai & eksekutif';
                hireBtn.onclick = () => openCompanyStaffModal(c);
                card.append(hireBtn);

                const staffList = document.createElement('div');
                staffList.style.marginTop = '12px';
                const emps = c.employees || [];
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

            } else if (currentTabId === 'fasilitas') {
                const card = document.createElement('section');
                card.className = 'card';
                card.innerHTML = `<h2 class="card-title">Kebijakan Kerja, Fasilitas & Moral</h2>`;
                flowText(card, 'Kebijakan jam kerja', c.workHours == null ? null : `${c.workHours} jam per hari`);
                flowText(card, 'Tingkat tunjangan fasilitas', c.benefitTier);
                flowText(card, 'Indeks kepuasan moral', c.morale == null ? null : `${c.morale}%`);

                const polBtn = document.createElement('button');
                polBtn.className = 'btn-primary';
                polBtn.style.marginTop = '12px';
                polBtn.textContent = 'Ubah kebijakan & fasilitas kerja';
                polBtn.onclick = () => openCompanyPolicyModal(c);
                card.append(polBtn);
                contentBox.append(card);

            } else if (currentTabId === 'keuangan') {
                const card = document.createElement('section');
                card.className = 'card';
                card.innerHTML = `<h2 class="card-title">Keuangan, Modal & Laporan Usaha</h2>`;
                flowText(card, 'Saldo kas usaha', `Rp ${Number(c.cash || 0).toLocaleString('id-ID')}`);
                flowText(card, 'Tunggakan gaji', `Rp ${Number(c.payrollArrears || 0).toLocaleString('id-ID')}`);

                const actionGroup = document.createElement('div');
                actionGroup.className = 'shortcut-grid';

                const capBtn = document.createElement('button');
                capBtn.className = 'btn-primary';
                capBtn.textContent = 'Suntik modal / tarik dividen';
                capBtn.onclick = () => openCompanyCapitalModal(c);

                const loanBtn = document.createElement('button');
                loanBtn.className = 'btn-secondary';
                loanBtn.textContent = 'Ajukan pinjaman modal kerja';
                loanBtn.onclick = () => openDisburseLoanModal('LOAN-CORP-01', c.id);

                actionGroup.append(capBtn, loanBtn);
                card.append(actionGroup);

                // Riwayat Laporan Harian
                const reportHeading = document.createElement('h3');
                reportHeading.className = 'card-title';
                reportHeading.textContent = 'Riwayat Laporan Keuangan Harian';
                card.append(reportHeading);

                const reports = c.operationalReports || [];
                if (!reports.length) {
                    flowText(card, 'Laporan keuangan', 'Belum ada siklus hari operasional yang diselesaikan.');
                } else {
                    reports.slice().reverse().forEach(rep => {
                        const rRow = document.createElement('div');
                        rRow.className = 'card';
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

        renderCompanySubtabContent(activeSubtab);
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

            function transactCollectionItem(item, buy) {
                let currentRequestId = null;
                try {
                    let price = Number(item.currentValuation != null ? item.currentValuation : (item.basePrice || 0));
                    if (bridge.getCollections) {
                        try {
                            const freshRes = JSON.parse(bridge.getCollections());
                            const list = Array.isArray(freshRes) ? freshRes : (freshRes.collections || freshRes.items || []);
                            const fresh = list.find(x => x.id === item.id);
                            if (fresh) {
                                const freshPrice = Number(fresh.currentValuation != null ? fresh.currentValuation : (fresh.basePrice || 0));
                                if (freshPrice !== price) {
                                    clearCollectionIntent(item.id, buy);
                                    showGameToast('info', 'Valuasi Koleksi Berubah', `Harga saat ini telah diperbarui menjadi ${formatMoney(freshPrice)}. Silakan tinjau kembali.`);
                                    renderCollectionsGallery(container, isModal);
                                    return;
                                }
                                price = freshPrice;
                            }
                        } catch(ignored) {}
                    }
                    currentRequestId = getOrCreateCollectionRequestId(item.id, buy, price);
                    const req = { requestId: currentRequestId, itemId: item.id, buy, expectedPrice: price };
                    const res = JSON.parse(bridge.transactCollection(JSON.stringify(req)));
                    if (!res.success) {
                        if (res.status === 'STALE_VALUATION') {
                            clearCollectionIntent(item.id, buy);
                            showGameToast('info', 'Valuasi Koleksi Berubah', res.message || 'Harga telah diperbarui. Silakan tinjau kembali.');
                            renderCollectionsGallery(container, isModal);
                            return;
                        }
                        if (/kas.*tidak mencukupi|bukan milik|diagunkan/i.test(res.message || '')) {
                            clearCollectionIntent(item.id, buy);
                        }
                        throw new Error(res.message);
                    }
                    clearCollectionIntent(item.id, buy);
                    const transAmount = res.amount ? formatMoney(res.amount) : formatMoney(price);
                    showGameToast('success', buy ? 'Koleksi Berhasil Dibeli!' : 'Koleksi Berhasil Terjual!', buy ? `${item.name} berhasil dibeli seharga ${transAmount}. Masuk ke galeri aset mewah.` : `${item.name} berhasil dijual seharga ${transAmount}.`);
                    refreshSnapshot();
                    renderCollectionsGallery(container, isModal);
                } catch(err) {
                    showGameToast('info', buy ? 'Pembelian Gagal' : 'Penjualan Gagal', err.message);
                }
            }

            function createCollectionCard(item, isOwned) {
                const card = document.createElement('article');
                card.className = 'card collection-item-card' + (isOwned ? ' owned-luxury' : '');
                card.setAttribute('data-collection-id', item.id);
                card.tabIndex = 0;
                card.setAttribute('role', 'article');

                const catIndo = categoryLabels[(item.category || '').toUpperCase()] || item.category || 'Aset Mewah';
                const rarityIndo = rarityLabels[(item.rarity || '').toUpperCase()] || item.rarityLabel || item.rarity || 'Standar';

                const isPledged = Boolean(item.isPledged || item.pledgedLoanId);
                const personalCash = latestSnapshot?.personalAccount?.cash?.IDR?.available
                    ? (parseInt(String(latestSnapshot.personalAccount.cash.IDR.available).replace(/[^0-9]/g, ''), 10) || 0)
                    : 0;
                const valuationPrice = Number(item.currentValuation != null ? item.currentValuation : (item.basePrice || 0));
                const canAfford = personalCash >= valuationPrice;

                card.setAttribute('aria-label', `Koleksi ${item.name}, kategori ${catIndo}, kelangkaan ${rarityIndo}, status ${isOwned ? 'dimiliki' : 'tersedia'}`);

                const headerDiv = document.createElement('div');
                headerDiv.className = 'section-heading';
                
                const leftMeta = document.createElement('div');
                leftMeta.className = 'flex-row';
                leftMeta.append(node('span', 'eyebrow', catIndo));
                if (isOwned) {
                    leftMeta.append(node('span', 'badge-owned', '✓ Dimiliki'));
                } else if (canAfford) {
                    leftMeta.append(node('span', 'badge-available', '○ Dapat Dibeli'));
                } else {
                    leftMeta.append(node('span', 'badge-locked', '🔒 Kas Kurang'));
                }
                if (isPledged) {
                    leftMeta.append(node('span', 'badge-pledged', 'Diagunkan'));
                }

                headerDiv.append(leftMeta);
                headerDiv.append(node('span', 'change-pill', rarityIndo));
                card.appendChild(headerDiv);

                const visualWrap = node('div', 'collection-visual-frame');
                visualWrap.style.background = 'radial-gradient(circle at center, #1e2d42 0%, #0d1522 100%)';
                visualWrap.style.borderRadius = '10px';
                visualWrap.style.overflow = 'hidden';
                visualWrap.style.display = 'flex';
                visualWrap.style.alignItems = 'center';
                visualWrap.style.justifyContent = 'center';
                visualWrap.style.minHeight = '140px';
                visualWrap.style.marginBottom = '8px';

                const img = document.createElement('img');
                img.src = `images/collections/${item.id}.webp`;
                img.alt = item.name;
                img.loading = 'lazy';
                img.style.maxWidth = '100%';
                img.style.maxHeight = '140px';
                img.style.objectFit = 'contain';
                img.onerror = () => {
                    img.onerror = () => { visualWrap.textContent = `${catIndo} · ${rarityIndo}`; };
                    img.src = `images/collections/${item.id}.png`;
                };
                visualWrap.appendChild(img);
                card.appendChild(visualWrap);

                const title = document.createElement('h3');
                title.className = 'card-title';
                title.textContent = item.name;
                card.appendChild(title);

                flowText(card, 'Status kepemilikan', isOwned ? (isPledged ? '✓ Dimiliki (Diagunkan pada pinjaman)' : '✓ Dimiliki Pemain') : (canAfford ? '○ Tersedia di Galeri' : '🔒 Belum Terbeli (Kas tidak cukup)'));
                flowText(card, 'Valuasi terkini', formatMoney(item.currentValuation));
                flowText(card, 'Biaya pemeliharaan', item.dailyMaintenanceCost == null ? null : `${formatMoney(item.dailyMaintenanceCost)} / hari`);
                flowText(card, 'Bonus reputasi', item.reputationBonus == null ? null : `+${item.reputationBonus} poin reputasi`);

                const actionsRow = document.createElement('div');
                actionsRow.className = 'collection-actions-row';

                const detailBtn = document.createElement('button');
                detailBtn.className = 'btn-secondary btn-collection-detail';
                detailBtn.type = 'button';
                detailBtn.textContent = 'Lihat rincian';
                detailBtn.onclick = (e) => {
                    e.stopPropagation();
                    openCollectionItemDetailModal(item);
                };
                actionsRow.appendChild(detailBtn);

                if (isOwned) {
                    if (isPledged) {
                        const actBtn = document.createElement('button');
                        actBtn.className = 'btn-secondary';
                        actBtn.disabled = true;
                        actBtn.textContent = 'Diagunkan (tidak dapat dijual)';
                        actionsRow.appendChild(actBtn);
                    } else {
                        const actBtn = document.createElement('button');
                        actBtn.className = 'btn-secondary btn-collection-sell';
                        actBtn.type = 'button';
                        actBtn.textContent = `Jual Koleksi (${formatMoney(item.currentValuation)})`;
                        actBtn.onclick = (e) => {
                            e.stopPropagation();
                            transactCollectionItem(item, false);
                        };
                        actionsRow.appendChild(actBtn);
                    }
                } else {
                    const actBtn = document.createElement('button');
                    actBtn.className = 'btn-primary btn-collection-buy';
                    actBtn.type = 'button';
                    if (canAfford) {
                        actBtn.textContent = `Beli Koleksi (${formatMoney(valuationPrice)})`;
                        actBtn.onclick = (e) => {
                            e.stopPropagation();
                            transactCollectionItem(item, true);
                        };
                    } else {
                        actBtn.disabled = true;
                        actBtn.textContent = 'Kas pribadi belum cukup';
                    }
                    actionsRow.appendChild(actBtn);
                }

                card.appendChild(actionsRow);

                card.onclick = (e) => {
                    if (e.target.closest('button')) return;
                    openCollectionItemDetailModal(item);
                };
                card.onkeydown = (e) => {
                    if ((e.key === 'Enter' || e.key === ' ') && e.target === card) {
                        e.preventDefault();
                        openCollectionItemDetailModal(item);
                    }
                };
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
                // 1. Panel Pasif: Ringkasan Status Karier & AUM Firma
                const summary = document.createElement('section');
                summary.className = 'firm-summary card';
                const fTitle = document.createElement('h2');
                fTitle.className = 'card-title';
                fTitle.textContent = 'Firma Investasi & Status AUM';
                summary.append(fTitle);

                metricCard(summary, 'DANA KELOLAAN KLIEN · IDR', firm.aumIdr, `${firm.activeClientCount} akun mandat aktif · terpisah dari kekayaan pribadi`);
                flowText(summary, 'Kapasitas AUM firma', firm.aumCapacityIdr || 'Belum tersedia');
                flowText(summary, 'Pemakaian kapasitas AUM', firm.aumCapacityUtilizationPct == null ? '0.00%' : `${Number(firm.aumCapacityUtilizationPct).toFixed(2)}% · tambahan kapasitas dari riset ${firm.aumScaleBonusPct || 0}%`);
                flowText(summary, 'Kas pribadi IDR (bukan AUM)', firm.personalCashIdr);
                flowText(summary, 'Klien aktif', `${firm.activeClientCount} akun kelolaan`);
                flowText(summary, 'Pemisahan dana', 'Portofolio dan kas klien dikelola dalam rekening terpisah 100% dari kekayaan pribadi manajer investasi.');
                container.append(summary);

                // 2. Launcher Terpisah: Fitur Pengelolaan & Alokasi Dana
                const launcherCard = document.createElement('section');
                launcherCard.className = 'card firm-launchers-section';
                launcherCard.innerHTML = `<h2 class="card-title">Fitur Pengelolaan & Alokasi Dana</h2>`;
                const launchersGrid = document.createElement('div');
                launchersGrid.className = 'firm-launchers-grid';
                flowButton(launchersGrid, 'Fund Builder & alokasi portofolio', () => openFundBuilder(), 'fundBuilder');
                flowButton(launchersGrid, 'Laporan klien & fee mandat', () => openClientReports());
                flowButton(launchersGrid, 'Bursa Headhunting Staf Analis Elit', () => openHeadhuntingModal());
                launcherCard.append(launchersGrid);
                container.append(launcherCard);

                // 3. Section Mandat Klien
                const mandatesSection = document.createElement('section');
                mandatesSection.className = 'card firm-mandates-section';
                mandatesSection.innerHTML = `<h2 class="card-title">Klien & Tawaran Mandat</h2>`;
                flowText(mandatesSection, 'Ketentuan mandat', 'Tarif fee tahunan dan performance fee dihitung otomatis saat tutup hari bursa. Dana klien dikelola pada akun terpisah.');

                const isMandateUnlocked = latestSnapshot?.featureAccess?.mandates?.unlocked !== false;
                if (!isMandateUnlocked) {
                    const lockNotice = node('div', 'notice');
                    lockNotice.append(
                        node('strong', '', 'Pengelolaan Mandat Klien Terkunci'),
                        node('p', '', 'Pangkat saat ini belum memenuhi syarat untuk mengelola dana pihak ketiga. Capai jabatan Portfolio Manager untuk membuka hak kelola mandat.')
                    );
                    const reqBtn = node('button', 'btn-secondary', 'Lihat syarat promosi karier');
                    reqBtn.type = 'button';
                    reqBtn.style.marginTop = '8px';
                    reqBtn.onclick = () => openPlayerProfileModal();
                    lockNotice.append(reqBtn);
                    mandatesSection.append(lockNotice);
                }

                const mandates = firm.mandates || [];
                if (!mandates.length) {
                    const empty = document.createElement('p');
                    empty.className = 'empty-state';
                    empty.textContent = 'Belum ada tawaran mandat klien. Tawaran baru akan masuk seiring peningkatan reputasi dan kenaikan pangkat karier.';
                    mandatesSection.append(empty);
                } else {
                    mandates.forEach(mandate => {
                        const mCard = document.createElement('article');
                        mCard.className = 'card mandate-card';
                        mCard.setAttribute('data-mandate-id', mandate.id);

                        const top = node('div', 'section-heading');
                        top.append(node('span', 'eyebrow', mandate.clientName), node('span', 'badge-status', mandate.statusLabel || mandate.status));
                        mCard.append(top, node('h3', '', `${mandate.clientName} · ${mandate.riskProfile}`));
                        flowText(mCard, 'Setoran modal awal', mandate.initialDeposit);
                        flowText(mCard, 'Indeks acuan (benchmark)', mandate.benchmark);
                        flowText(mCard, 'Tarif management fee', mandate.managementFeeAnnualRate != null ? `${(Number(mandate.managementFeeAnnualRate) * 100).toFixed(1)}% p.a.` : '-');
                        flowText(mCard, 'Tarif performance fee', mandate.performanceFeeRate != null ? `${(Number(mandate.performanceFeeRate) * 100).toFixed(0)}% di atas HWM` : '-');

                        const btnReview = node('button', mandate.status === 'OFFERED' ? 'btn-primary' : 'btn-secondary', 'Tinjau rincian mandat');
                        btnReview.type = 'button';
                        btnReview.style.marginTop = '10px';
                        btnReview.onclick = () => openMandateDetailModal(mandate);
                        mCard.append(btnReview);
                        mandatesSection.append(mCard);
                    });
                }
                container.append(mandatesSection);
            }
        } catch (error) { flowText(container, 'Firma gagal dimuat', error.message); }
    }

    function openMandateDetailModal(mandate) {
        const panel = showFlow('Detail Mandat: ' + mandate.clientName);
        panel.setAttribute('data-mandate-detail-id', mandate.id);

        const card = node('section', 'card');
        const meta = node('div', 'section-heading');
        meta.append(node('span', 'eyebrow', `Klien ${mandate.riskProfile}`), node('span', 'badge-status', mandate.statusLabel || mandate.status));
        card.append(meta, node('h2', 'card-title', mandate.clientName));

        flowText(card, 'ID Mandat', mandate.id);
        flowText(card, 'Status mandat', mandate.statusLabel || mandate.status);
        flowText(card, 'Akun klien terpisah', mandate.accountId || 'Belum ada akun klien');
        flowText(card, 'Profil risiko & benchmark', `${mandate.riskProfile} (Benchmark: ${mandate.benchmark})`);
        flowText(card, 'Setoran modal awal', mandate.initialDeposit);
        flowText(card, 'Batas konsentrasi per saham', mandate.maxConcentration != null ? `${(Number(mandate.maxConcentration) * 100).toFixed(0)}% maksimal per emiten` : '-');
        flowText(card, 'Cadangan kas minimum', mandate.minCashReserve != null ? `${(Number(mandate.minCashReserve) * 100).toFixed(0)}% kas likuid` : '-');
        flowText(card, 'Tarif management fee tahunan', mandate.managementFeeAnnualRate != null ? `${(Number(mandate.managementFeeAnnualRate) * 100).toFixed(1)}% p.a.` : '-');
        flowText(card, 'Tarif performance fee', mandate.performanceFeeRate != null ? `${(Number(mandate.performanceFeeRate) * 100).toFixed(0)}% di atas HWM` : '-');
        flowText(card, 'High-Water Mark (HWM)', mandate.highWaterMark || '-');
        flowText(card, 'Skor kepercayaan klien (Trust)', `${mandate.trust} / 100`);
        panel.append(card);

        const actions = node('section', 'card');
        actions.append(node('h3', 'card-title', 'Tindakan Mandat'));

        const isMandateUnlocked = latestSnapshot?.featureAccess?.mandates?.unlocked !== false;

        if (mandate.status === 'OFFERED') {
            if (isMandateUnlocked && bridge.acceptClientMandate) {
                flowButton(actions, 'Terima Mandat Kelolaan Klien', () => {
                    try {
                        const res = JSON.parse(bridge.acceptClientMandate(mandate.id));
                        if (!res.success) throw new Error(res.message);
                        showGameToast('success', 'Mandat Diterima!', `${res.message}. Akun kelolaan: ${res.accountId}`);
                        refreshSnapshot();
                        closeFlow();
                        loadFirmaView('dana');
                    } catch (err) {
                        showGameToast('info', 'Penerimaan Mandat Ditolak', err.message);
                    }
                });
            } else {
                const lockP = node('p', 'notice', 'Penerimaan mandat klien terkunci. Memerlukan jabatan Portfolio Manager untuk membuka hak mengelola portofolio pihak ketiga.');
                const viewReqBtn = node('button', 'btn-secondary', 'Lihat syarat promosi karier');
                viewReqBtn.type = 'button';
                viewReqBtn.style.marginTop = '8px';
                viewReqBtn.onclick = () => openPlayerProfileModal();
                actions.append(lockP, viewReqBtn);
            }
        } else if (mandate.status === 'ACTIVE') {
            actions.append(node('p', 'notice', `Mandat aktif dikelola. Akun kelolaan: ${mandate.accountId}. Saldo dan posisi saham terpisah 100% dari rekening pribadi manajer. Transaksi dilakukan melalui menu Pasar dengan memilih akun klien.`));
            const viewReportsBtn = node('button', 'btn-secondary', 'Lihat laporan performa & fee klien');
            viewReportsBtn.type = 'button';
            viewReportsBtn.style.marginTop = '8px';
            viewReportsBtn.onclick = () => openClientReports();
            actions.append(viewReportsBtn);
        }
        panel.append(actions);
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

    function createHubGroup(title, eyebrow) {
        const card = node('section', 'card hub-group-card');
        const header = node('div', 'section-heading');
        header.append(node('span', 'eyebrow', eyebrow), node('h2', 'card-title', title));
        card.append(header);
        const list = node('div', 'hub-item-list');
        card.append(list);
        return { card, list };
    }

    function createHubRow(icon, title, desc, badgeText, onClick) {
        const row = document.createElement('button');
        row.type = 'button';
        row.className = 'hub-item-row';
        row.onclick = onClick;

        const left = node('div', 'hub-item-left');
        const iconDiv = node('div', 'hub-item-icon', icon);
        const textDiv = node('div', 'hub-item-text');
        textDiv.append(node('div', 'hub-item-title', title), node('div', 'hub-item-desc', desc));
        left.append(iconDiv, textDiv);

        const right = node('div', 'hub-item-right');
        if (badgeText) {
            right.append(node('span', 'hub-item-badge', badgeText));
        }
        right.append(node('span', 'hub-item-chevron', '›'));

        row.append(left, right);
        return row;
    }

    function loadRisetView() {
        refreshSnapshot();
        const view = views.riset;
        view.replaceChildren();

        // 0. Progres Hero Banner
        const banner = node('section', 'card hub-progres-banner');
        const pHeader = node('div', 'hub-progres-header');
        
        let pName = 'Investor';
        let pRank = 'Novice Trader';
        let pRep = 0;
        let pLevel = 1;
        let pXp = 0;
        
        try {
            if (bridge.getPlayerProfile) {
                const prof = JSON.parse(bridge.getPlayerProfile());
                if (prof.name) pName = prof.name;
                if (prof.careerRankLabel || prof.careerRank) pRank = prof.careerRankLabel || prof.careerRank;
                if (prof.reputation != null) pRep = prof.reputation;
            }
            if (bridge.getPlayerProgress) {
                const prog = JSON.parse(bridge.getPlayerProgress());
                if (prog.level != null) pLevel = prog.level;
                if (prog.currentXp != null) pXp = prog.currentXp;
            }
        } catch(e) {}

        pHeader.append(
            node('div', '', `${pName} · Level ${pLevel}`),
            node('span', 'badge-owned', pRank)
        );
        banner.append(pHeader);

        const pGrid = node('div', 'hub-progres-grid');
        const p1 = node('div', 'hub-progres-item');
        p1.append(node('span', '', 'Reputasi Karier'), node('strong', '', `${pRep} Poin`));
        const p2 = node('div', 'hub-progres-item');
        p2.append(node('span', '', 'Total Pengalaman'), node('strong', '', `${pXp} XP`));
        const p3 = node('div', 'hub-progres-item');
        p3.append(node('span', '', 'Status Pasar'), node('strong', '', latestSnapshot?.clock?.sessionLabel || 'Sesi Game'));
        pGrid.append(p1, p2, p3);
        banner.append(pGrid);
        view.append(banner);

        // Group 1: Profil & Progres
        const g1 = createHubGroup('Profil & Progres', 'PROFIL & PRESTISE');
        g1.list.append(
            createHubRow('👤', 'Profil Saya & Identitas', 'Kelola nama, statistik performa, dan identitas investor', pRank, () => openPlayerProfileModal()),
            createHubRow('🏆', 'Level & Pencapaian', 'Progres XP dan lencana prestasi trading yang telah diraih', `Level ${pLevel}`, () => openAchievementsModal()),
            createHubRow('🎯', 'Gaya & Arketipe Investor', 'Karakteristik strategi dan kebiasaan trading di bursa', 'Arketipe', () => openInvestorArchetypeModal()),
            createHubRow('🌟', 'Era Baru & Prestise', 'Mulai siklus baru dengan multiplier reputasi & modal ekstra', 'Era Baru', () => openPrestigeModal())
        );
        view.append(g1.card);

        // Group 2: Misi & Kompetisi
        const g2 = createHubGroup('Misi & Kompetisi', 'TARGET & TANTANGAN');
        g2.list.append(
            createHubRow('📋', 'Misi Periodik', 'Target harian dan mingguan berhadiah uang tunai & XP', 'Target', () => openPeriodicMissionsModal()),
            createHubRow('🥇', 'Liga & Peringkat Rival', 'Kompetisi imbal hasil portofolio melawan rival pasar', 'Divisi Liga', () => openLeagueAndRivalModal()),
            createHubRow('🎁', 'Sponsor Harian & Kehadiran', 'Klaim dana bantuan modal sponsor setiap hari bursa', 'Sponsor', () => openDailySponsorModal()),
            createHubRow('⚔️', 'Duel Bos Taipan Pasar', 'Tantang 4 bos pasar modal dalam duel imbal hasil 1-on-1', 'Tantangan', () => openMarketBossModal())
        );
        view.append(g2.card);

        // Group 3: Riset Pasar & Pengembangan
        let currentTier = 'BASIC';
        try {
            if (bridge.getResearchFeed) {
                const rf = JSON.parse(bridge.getResearchFeed());
                if (rf.tier) currentTier = rf.tier;
            }
        } catch(e) {}

        const g3 = createHubGroup('Riset & Intelijen Pasar', 'ANALISIS & TEKNOLOGI');
        g3.list.append(
            createHubRow('📊', 'Terminal Riset & Konsensus', 'Analisis fundamental emiten, rasio keuangan, dan konsensus analis', 'Terminal', () => openResearchTerminal()),
            createHubRow('📰', 'Berita Pasar & Sentimen', 'Warta bursa terkini dan estimasi arah pergerakan harga', 'Berita', () => openResearchNewsFeedModal()),
            createHubRow('💳', 'Paket Langganan Terminal', 'Kelola tingkat akses tier BASIC, PRO, dan INSTITUTIONAL', `Tier ${currentTier}`, () => openResearchTiersModal()),
            createHubRow('🌳', 'Pohon Riset Finansial (Tech Tree)', 'Riset teknologi order, analisis pasar, dan fitur bursa', 'Teknologi', () => openTechTreeModal()),
            createHubRow('⚡', 'Kartu Booster Riset R&D', 'Aktivasi booster untuk mempercepat hasil dan akurasi riset', 'Booster', () => openResearchBoostersModal()),
            createHubRow('⏪', 'Replay Historis Perdagangan', 'Uji strategi pada data pergerakan pasar historis nyata', 'Simulasi', () => openHistoricalReplayModal())
        );
        view.append(g3.card);

        // Group 4: Pengaturan & Sistem
        const g4 = createHubGroup('Pengaturan & Sistem', 'PREFERENSI & PENYIMPANAN');
        g4.list.append(
            createHubRow('📖', 'Panduan Bermain & Bantuan', 'Mekanisme bursa 24x, manajemen risiko, dan panduan fitur', 'Edukasi', () => openTutorialModal()),
            createHubRow('⏱️', 'Sinkronisasi Jam Bursa', 'Periksa waktu sesi bursa 24x dan status sinkronisasi server', 'Waktu Game', () => openClockSyncModal()),
            createHubRow('💾', 'Penyimpanan Permainan & Backup', 'Simpan permainan secara manual dan periksa integritas cadangan', 'Simpan', () => openSaveManagementModal()),
            createHubRow('🔔', 'Riwayat Notifikasi & Pesan', 'Daftar pesan penting, kesalahan transaksi, dan warta sistem', 'Pesan', () => openNotificationHistoryModal()),
            createHubRow('🔄', 'Mulai Permainan Baru (Reset Run)', 'Mulai ulang permainan dari awal dengan konfirmasi ganda aman', 'Atur Ulang', () => openNewGameConfirmationModal())
        );
        view.append(g4.card);
    }

    function openPrestigeModal() {
        const panel = showFlow('Era Baru & Prestise');
        panel.setAttribute('data-modal-id', 'prestige-modal');
        flowText(panel, 'Informasi Era Baru', 'Masuki Era Baru untuk mereset perjalanan aktif dan membuka multiplier reputasi, perolehan XP lebih cepat, serta modal awal yang lebih besar.');

        if (!bridge.getPrestigeStatus) {
            flowText(panel, 'Status', 'Layanan prestise belum tersedia.');
            return;
        }

        try {
            const pres = JSON.parse(bridge.getPrestigeStatus());
            const isEligible = Boolean(pres.eligible);

            const card = node('section', 'card');
            const meta = node('div', 'section-heading');
            meta.append(node('span', 'eyebrow', 'Status Kelayakan'), node('span', isEligible ? 'badge-owned' : 'badge-pledged', isEligible ? 'Memenuhi Syarat' : 'Belum Terbuka'));
            card.append(meta, node('h2', 'card-title', isEligible ? 'Siap Memulai Era Baru' : 'Persyaratan Belum Terpenuhi'));

            flowText(card, 'Pangkat yang diperlukan', pres.requiredRank || 'Belum tersedia');
            flowText(card, 'Hari perjalanan berjalan', `${pres.runDays || 0} hari game`);
            flowText(card, 'Bonus Era Baru', pres.prestigeBonus || 'Multiplier Reputasi & Kas Awal');
            panel.append(card);

            const checklistCard = node('section', 'card');
            checklistCard.append(node('h2', 'card-title', 'Checklist Persyaratan'));
            const blockers = pres.blockers || [];
            if (blockers.length === 0) {
                checklistCard.append(node('p', 'notice', isEligible ? 'Seluruh persyaratan telah terpenuhi. Anda dapat memulai Era Baru kapan saja.' : 'Rincian persyaratan belum tersedia.'));
            } else {
                blockers.forEach(b => {
                    const row = node('div', 'data-row');
                    row.append(node('span', 'negative', 'Belum Terpenuhi'), node('strong', '', b));
                    checklistCard.append(row);
                });
            }
            panel.append(checklistCard);

            const impactCard = node('section', 'card');
            impactCard.append(node('h2', 'card-title', 'Dampak Siklus Era Baru'));
            flowText(impactCard, 'Yang direset', pres.reset || 'Kas pribadi, posisi portofolio, anak usaha, dan level XP run berjalan.');
            flowText(impactCard, 'Yang dipertahankan', pres.retained || 'Nama identitas pemain, preferensi akun, statistik lifetime, dan lencana pencapaian.');
            flowText(impactCard, 'Proteksi data', 'Sistem secara otomatis membuat cadangan aman sebelum reset dijalankan.');
            panel.append(impactCard);

            const actions = node('section', 'card');
            actions.append(node('h3', 'card-title', 'Tindakan Prestise'));

            if (isEligible) {
                const confirmBtn = node('button', 'btn-primary', 'Konfirmasi & Masuk Era Baru');
                confirmBtn.type = 'button';
                confirmBtn.style.marginTop = '10px';
                confirmBtn.onclick = () => {
                    if (!confirm('PERINGATAN ERA BARU:\nApakah Anda yakin ingin memulai Era Baru sekarang? Portofolio dan anak usaha saat ini akan direset untuk siklus prestise baru.')) return;
                    confirmBtn.disabled = true;
                    confirmBtn.textContent = 'Memproses Era Baru...';
                    try {
                        const reqId = 'PRESTIGE-' + Date.now();
                        const res = JSON.parse(bridge.confirmPrestige(JSON.stringify({ requestId: reqId, runId: pres.runId })));
                        if (res && res.success !== false) {
                            showGameToast('success', 'Era Baru Dimulai!', res.message || 'Progres telah diperbarui ke siklus prestise.');
                            refreshSnapshot();
                            setTimeout(() => { closeFlow(); loadRisetView(); }, 1200);
                        } else {
                            throw new Error(res.message || 'Gagal memulai Era Baru');
                        }
                    } catch(err) {
                        confirmBtn.disabled = false;
                        confirmBtn.textContent = 'Konfirmasi & Masuk Era Baru';
                        showGameToast('info', 'Prestise Ditolak', err.message);
                    }
                };
                actions.append(confirmBtn);
            } else {
                actions.append(node('p', 'notice', 'Pintu Era Baru masih terkunci. Tingkatkan pangkat karier dan penuhi seluruh checklist persyaratan di atas untuk membuka opsi ini.'));
            }
            panel.append(actions);
        } catch(e) {
            flowText(panel, 'Gagal memuat status prestise', e.message);
        }
    }

    function openDailySponsorModal() {
        const panel = showFlow('Hadiah Sponsor Harian');
        panel.setAttribute('data-modal-id', 'daily-sponsor-modal');
        flowText(panel, 'Bantuan Modal Bursa', 'Klaim dana bantuan operasional dari sponsor bursa resmi sekali setiap pergantian hari game.');

        const card = node('section', 'card');
        card.append(node('h2', 'card-title', 'Status Kehadiran & Sponsor'));

        const sponsor = latestDailySponsor || {};
        const isClaimed = Boolean(sponsor.alreadyClaimed);
        const streak = sponsor.streakCount || 1;
        const amount = sponsor.grantAmountFormatted || sponsor.amount || 'Rp 5.000.000';

        flowText(card, 'Streak Kehadiran', `${streak} hari beruntun`);
        flowText(card, 'Nominal Bantuan Sponsor', amount);
        flowText(card, 'Status Hari Ini', isClaimed ? '✓ Sudah Diklaim' : '○ Siap Diklaim');
        panel.append(card);

        const actions = node('section', 'card');
        actions.append(node('h3', 'card-title', 'Tindakan Klaim'));

        if (!isClaimed) {
            const claimBtn = node('button', 'btn-primary', `Klaim Bantuan Sponsor (${amount})`);
            claimBtn.type = 'button';
            claimBtn.style.marginTop = '10px';
            let claiming = false;
            claimBtn.onclick = () => {
                if (claiming) return;
                claiming = true;
                claimBtn.disabled = true;
                claimBtn.textContent = 'Mengklaim dana...';
                try {
                    const reqId = 'SPONSOR-' + Date.now();
                    const res = JSON.parse(bridge.claimDailySponsor(JSON.stringify({ requestId: reqId })));
                    if (!res.success) throw new Error(res.message);
                    showGameToast('success', 'Sponsor Berhasil Diklaim!', res.message || `${amount} berhasil masuk ke kas pribadi.`);
                    refreshSnapshot();
                    setTimeout(() => { closeFlow(); loadRisetView(); }, 800);
                } catch(err) {
                    claiming = false;
                    claimBtn.disabled = false;
                    claimBtn.textContent = `Klaim Bantuan Sponsor (${amount})`;
                    showGameToast('info', 'Klaim Gagal', err.message);
                }
            };
            actions.append(claimBtn);
        } else {
            actions.append(node('p', 'notice', 'Anda telah mengklaim jatah sponsor hari ini. Bantuan berikutnya akan tersedia pada pembukaan hari bursa baru.'));
        }
        panel.append(actions);
    }

    function openTutorialModal() {
        const panel = showFlow('Panduan Bermain & Bantuan');
        panel.setAttribute('data-modal-id', 'tutorial-modal');
        flowText(panel, 'Edukasi Mekanisme', 'Pelajari mekanisme pasar modal 24x, aturan bursa, dan strategi pengembangan kekayaan.');

        const guides = [
            {
                title: '1. Dunia Otomatis 24x Tanpa Pause',
                desc: 'Waktu game berjalan terus menerus secara deterministik: 1 hari bursa game setara dengan 1 jam nyata. Tidak ada tombol pause atau manipulasi waktu manual pemain, menjaga integritas simulasi.'
            },
            {
                title: '2. Dua Pasar Bursa Utama (IDX & US)',
                desc: 'Bursa Indonesia (IDX) buka pada pagi-sore hari dengan satuan 1 lot = 100 lembar. Bursa Amerika (US) buka pada malam-dini hari dengan satuan 1 unit = 1 lembar dalam valuta USD.'
            },
            {
                title: '3. Jenis Order & Eksekusi',
                desc: 'Gunakan Market Order untuk eksekusi instan pada kuotasi terbaik, Limit Order untuk menetapkan harga batas, serta Stop Loss dan Take Profit untuk manajemen risiko otomatis saat menyentuh trigger.'
            },
            {
                title: '4. Fasilitas Margin & Likuidasi',
                desc: 'Fasilitas margin memungkinkan peminjaman modal trading dengan bunga harian. Pertahankan rasio ekuitas di atas batas pemeliharaan (30%) untuk menghindari likuidasi paksa portofolio pada level 20%.'
            },
            {
                title: '5. Pemisahan Dana Kelolaan Klien',
                desc: 'Pada firma investasi, dana kelolaan klien (AUM) dikelola pada rekening terpisah 100% dari kekayaan pribadi manajer. Manajer memperoleh pendapatan dari management fee tahunan dan performance fee di atas High-Water Mark.'
            }
        ];

        guides.forEach(g => {
            const card = node('article', 'card');
            card.append(node('h3', 'card-title', g.title), node('p', '', g.desc));
            panel.append(card);
        });
    }

    function openClockSyncModal() {
        const panel = showFlow('Sinkronisasi Jam Bursa Game');
        panel.setAttribute('data-modal-id', 'clock-sync-modal');
        flowText(panel, 'Waktu Dunia Terpadu', 'Jam bursa game diatur secara terpusat oleh engine Java dengan siklus 24x kecepatan waktu riil.');

        const card = node('section', 'card');
        card.append(node('h2', 'card-title', 'Status Jam Game Terkini'));

        const clock = latestSnapshot?.clock || {};
        const rate = clock.rate != null ? Number(clock.rate) : 24;
        flowText(card, 'Sesi Pasar Saat Ini', clock.sessionLabel || 'Sesi Game');
        flowText(card, 'Laju Waktu', `${rate}x kecepatan waktu nyata`);
        flowText(card, 'Status Perdagangan IDX', clock.isIdxTradingOpen ? 'Buka untuk Trading' : 'Tutup');
        flowText(card, 'Status Perdagangan US', clock.isUsTradingOpen ? 'Buka untuk Trading' : 'Tutup');
        flowText(card, 'Waktu Sinkronisasi Terakhir', new Date().toLocaleTimeString('id-ID'));
        panel.append(card);

        const actions = node('section', 'card');
        actions.append(node('h3', 'card-title', 'Tindakan Sinkronisasi'));
        const syncBtn = node('button', 'btn-primary', 'Sinkronkan Jam Sekarang');
        syncBtn.type = 'button';
        syncBtn.style.marginTop = '10px';
        syncBtn.onclick = () => {
            syncBtn.disabled = true;
            syncBtn.textContent = 'Menyinkronkan...';
            const syncOpId = 'SYNC-' + Date.now() + '-' + Math.random().toString(36).substring(2, 7);
            requestClockSync(true, true)
                .then(() => {
                    showGameToast('success', 'Waktu Disinkronkan', 'Jam permainan telah diselaraskan dengan engine waktu game.', syncOpId + ':OK');
                    refreshSnapshot();
                    closeFlow();
                    loadRisetView();
                })
                .catch(err => {
                    syncBtn.disabled = false;
                    syncBtn.textContent = 'Sinkronkan Jam Sekarang';
                    showGameToast('info', 'Sinkronisasi Gagal', err.message, syncOpId + ':ERR');
                });
        };
        actions.append(syncBtn);
        panel.append(actions);
    }

    function openNotificationHistoryModal() {
        const panel = showFlow('Riwayat Notifikasi & Pesan');
        panel.setAttribute('data-modal-id', 'notification-history-modal');
        flowText(panel, 'Pusat Pesan', 'Riwayat pemberitahuan sistem, kesalahan transaksi, dan warta penting yang diterima selama sesi permainan ini.');

        const card = node('section', 'card');
        card.append(node('h2', 'card-title', 'Pemberitahuan Terkini'));
        const list = getToastHistory();
        if (!list.length) {
            emptyState(card, 'Belum ada riwayat pemberitahuan', 'Pemberitahuan dan warta penting yang muncul selama sesi akan tercatat di sini.');
        } else {
            const container = node('div', 'notification-history-list');
            list.slice().reverse().forEach(item => {
                const row = node('div', 'data-row notification-history-item');
                row.style.marginBottom = '8px';
                row.style.display = 'block';
                const typeLabel = item.type === 'error' ? 'ERROR' : item.type === 'alert' ? 'PERINGATAN' : item.type === 'success' ? 'SUKSES' : 'INFO';
                const typeBadgeClass = item.type === 'error' ? 'badge-locked' : item.type === 'alert' ? 'badge-pledged' : item.type === 'success' ? 'badge-owned' : 'badge';
                const timeStr = item.timestamp ? new Date(item.timestamp).toLocaleTimeString('id-ID') : '';
                row.innerHTML = `
                    <div style="display:flex; justify-content:space-between; align-items:center; width:100%; margin-bottom:4px;">
                        <span class="${typeBadgeClass}" style="font-size:0.7rem; font-weight:700;">[${typeLabel}] ${item.title}</span>
                        <small style="color:#475569; font-size:0.75rem;">${timeStr}</small>
                    </div>
                    <p style="margin:0; font-size:0.875rem; color:#0F172A; width:100%; word-break:break-word;">${item.message}</p>
                `;
                container.appendChild(row);
            });
            card.appendChild(container);
        }
        panel.append(card);
    }
    window.openNotificationHistoryModal = openNotificationHistoryModal;

    let lastSavedTimeFormatted = null;

    function openSaveManagementModal() {
        const panel = showFlow('Penyimpanan Permainan & Backup');
        panel.setAttribute('data-modal-id', 'save-management-modal');
        flowText(panel, 'Integritas Berkas Save', 'Sistem menggunakan mekanisme penyimpanan ganda (save utama dan backup otomatis) untuk mencegah kehilangan data.');

        let meta = null;
        if (bridge.getSaveMetadata) {
            try { meta = JSON.parse(bridge.getSaveMetadata()); } catch(ignored) {}
        }
        if (!meta && latestSnapshot?.saveMetadata) {
            meta = latestSnapshot.saveMetadata;
        }

        const card = node('section', 'card');
        card.append(node('h2', 'card-title', 'Status Berkas Penyimpanan'));
        flowText(card, 'Format Berkas', meta?.format || 'JSON Standar (Skema V2, UTF-8)');

        let lastSavedText = 'Belum tersedia';
        if (meta?.primaryLastModified) {
            lastSavedText = new Date(meta.primaryLastModified).toLocaleTimeString('id-ID');
        } else if (lastSavedTimeFormatted) {
            lastSavedText = lastSavedTimeFormatted;
        } else if (latestSnapshot?.savedTimestamp) {
            lastSavedText = new Date(latestSnapshot.savedTimestamp).toLocaleTimeString('id-ID');
        }
        flowText(card, 'Penyimpanan Terakhir', lastSavedText);
        flowText(card, 'Mekanisme Rotasi', 'Aktif (Rotasi Atomik)');
        flowText(card, 'Cadangan Otomatis (.bak)', meta?.backupStatusLabel || (meta?.backupFileExists ? 'Tersedia' : 'Belum tersedia'));
        panel.append(card);

        const actions = node('section', 'card');
        actions.append(node('h3', 'card-title', 'Tindakan Penyimpanan'));
        const saveBtn = node('button', 'btn-primary', 'Simpan Permainan Sekarang');
        saveBtn.type = 'button';
        saveBtn.style.marginTop = '10px';
        saveBtn.onclick = () => {
            saveBtn.disabled = true;
            saveBtn.textContent = 'Menyimpan berkas...';
            try {
                if (bridge.saveGame) {
                    const res = JSON.parse(bridge.saveGame());
                    if (res && res.success === false) throw new Error(res.message || 'Gagal menyimpan');
                }
                lastSavedTimeFormatted = new Date().toLocaleTimeString('id-ID');
                showGameToast('success', 'Permainan Tersimpan!', 'Seluruh progres, saldo, dan posisi berhasil disimpan dengan aman.', 'SAVE_GAME_OK:' + Date.now());
                refreshSnapshot();
                setTimeout(() => {
                    saveBtn.disabled = false;
                    saveBtn.textContent = 'Simpan Permainan Sekarang';
                    openSaveManagementModal();
                }, 800);
            } catch(e) {
                saveBtn.disabled = false;
                saveBtn.textContent = 'Simpan Permainan Sekarang';
                showGameToast('info', 'Penyimpanan Gagal', e.message, 'SAVE_GAME_FAIL:' + Date.now());
            }
        };
        actions.append(saveBtn);
        panel.append(actions);
    }

    function openNewGameConfirmationModal() {
        const panel = showFlow('Mulai Permainan Baru (Reset Run)');
        panel.setAttribute('data-modal-id', 'new-game-modal');

        const card = node('section', 'card');
        card.style.borderLeft = '4px solid var(--negative)';
        card.append(node('h2', 'card-title', 'Peringatan Penghapusan Progres'));
        card.append(node('p', 'notice', 'PERINGATAN: Memulai permainan baru akan mereset kas, portofolio saham, bisnis operasional, dan level perjalanan aktif Anda saat ini dari awal. Tindakan ini tidak dapat dibatalkan.'));
        flowText(card, 'Yang akan direset', 'Kas pribadi, kepemilikan saham, anak usaha, utang margin, dan XP perjalanan.');
        flowText(card, 'Pilihan mode awal', 'Pasar Internal Reguler (Modal Awal Rp 50.000.000)');
        panel.append(card);

        const actions = node('section', 'card');
        actions.append(node('h3', 'card-title', 'Konfirmasi Mulai Baru'));

        const confirmBtn = node('button', 'btn-primary', 'Konfirmasi Reset & Mulai Baru');
        confirmBtn.type = 'button';
        confirmBtn.style.background = 'var(--negative)';
        confirmBtn.style.marginTop = '10px';

        confirmBtn.onclick = () => {
            if (!confirm('APAKAH ANDA YAKIN INGIN MERESET PERMAINAN?\nSeluruh kemajuan saat ini akan dihapus dan permainan akan dimulai dari awal.')) return;
            confirmBtn.disabled = true;
            confirmBtn.textContent = 'Mereset permainan...';
            try {
                const req = JSON.stringify({ mode: 'INTERNAL_MARKET' });
                const res = JSON.parse(bridge.startNewGame ? bridge.startNewGame(req) : '{"success":false,"message":"Bridge startNewGame belum tersedia"}');
                if (!res.success) throw new Error(res.message);
                showGameToast('success', 'Permainan Direset', 'Perjalanan baru telah dimulai.');
                refreshSnapshot();
                setTimeout(() => {
                    closeFlow();
                    switchTab('beranda');
                }, 1200);
            } catch(e) {
                confirmBtn.disabled = false;
                confirmBtn.textContent = 'Konfirmasi Reset & Mulai Baru';
                showGameToast('info', 'Gagal Memulai Baru', e.message);
            }
        };
        actions.append(confirmBtn);
        panel.append(actions);
    }

    function openResearchNewsFeedModal() {
        const panel = showFlow('Berita Pasar & Riset Terkini');
        panel.setAttribute('data-modal-id', 'research-news-feed-modal');
        flowText(panel, 'Warta Ekonomi Simulasi', 'Catatan peristiwa dan rilis data bursa yang telah terbit resmi pada waktu permainan.');

        if (!bridge.getResearchFeed) {
            flowText(panel, 'Berita', 'Feed berita belum dapat dimuat.');
            return;
        }

        try {
            const feed = JSON.parse(bridge.getResearchFeed());
            const news = feed.news || [];
            if (!news.length) {
                const empty = node('div', 'empty-state');
                empty.append(node('strong', '', 'Belum ada berita terbit'), node('p', '', 'Berita ekonomi makro dan emiten akan diterbitkan seiring berjalannya waktu game.'));
                panel.append(empty);
            } else {
                news.forEach(n => {
                    const card = node('article', 'card news-card');
                    const topRow = node('div', 'section-heading');
                    topRow.style.margin = '0 0 6px 0';
                    topRow.append(
                        node('span', 'eyebrow', `${n.categoryLabel || 'Bursa'}${n.relatedTicker ? ' · ' + n.relatedTicker : ''}`),
                        node('span', 'change-pill', n.publishedAt == null ? 'Waktu belum tersedia' : new Date(n.publishedAt).toLocaleString('id-ID', {timeZone:'Asia/Jakarta'}))
                    );
                    const h3 = node('h3', '', n.headline);
                    h3.style.margin = '4px 0 8px';
                    const impactRow = node('div', 'flex-row');
                    const impactBadge = node('span', 'change-pill', `${n.affectedSector || n.categoryLabel || 'Sektor'} · Perkiraan dampak: ${n.direction || 'netral'}${n.estimatedRange ? ' · ' + n.estimatedRange : ''}`);
                    impactRow.append(impactBadge, newsEffectNode(n, 'change-pill'));
                    card.append(topRow, h3, impactRow);

                    const btn = flowButton(card, 'Baca catatan lengkap', () => openNewsDetail(n, () => openResearchNewsFeedModal()));
                    btn.className = 'text-button';
                    panel.append(card);
                });
            }
        } catch(e) {
            flowText(panel, 'Gagal memuat berita', e.message);
        }
    }

    let flowPanel = null;
    let activeRequestId = null;
    let orderDraft = null;
    let orderPreview = null;
    function showFlow(title, isolated = false) {
        if (!flowPanel) flowOpener = document.activeElement;
        else document.querySelectorAll('.flow-page').forEach(fp => fp.remove());
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
        if (!flowPanel && !document.querySelector('.flow-page')) return false;
        if (flowPanel && flowPanel.getAttribute('aria-label') === 'Pemulihan save diperlukan' && recovered !== true) return true;
        activeDetailStock = null;
        activeReceiptRequestId = null;
        activeDetailPositionInstrumentId = null;
        const newsReturn = flowPanel?.newsReturn;
        document.querySelectorAll('.flow-page').forEach(fp => fp.remove());
        flowPanel = null;
        if (newsReturn) { newsReturn(); return true; }
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
        const panel = showFlow('Detail Indeks');
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

            // Kartu ringkas indeks (Anotasi A & Pilihan Pemilik)
            const indexCard = node('section', 'card prototype-index-card');
            indexCard.style.padding = '16px';
            indexCard.style.borderRadius = '12px';
            indexCard.style.marginBottom = '12px';
            indexCard.style.background = 'var(--surface-raised)';
            indexCard.innerHTML = `
                <p style="font-size:0.95rem; font-weight:700; color:var(--text); margin:0 0 4px;">${detail.ticker} · ${detail.name} (${detail.exchange})</p>
                <strong style="font-size:1.6rem; color:var(--action); display:block; margin:6px 0;">${detail.value} ${detail.unit}</strong>
                <p style="font-size:0.75rem; color:var(--text-secondary); margin:0;">Indeks simulasi game · ${detail.gameDate} ${detail.gameTime}</p>
            `;
            panel.append(indexCard);

            const details = node('details', 'card');
            details.style.marginBottom = '12px';
            details.innerHTML = `<summary style="font-weight:700; cursor:pointer; padding:4px 0;">Lihat informasi & metodologi</summary>`;
            flowText(details, 'Sumber', detail.sourceLabel);
            flowText(details, 'Metode', detail.method);
            flowText(details, 'Periode berlaku', detail.validFrom);
            flowText(details, 'Batas', detail.limitations);
            if (!detail.historyAvailable) flowText(details, 'Grafik', 'Histori indeks belum tersedia; tidak menampilkan grafik contoh.');
            panel.append(details);

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
        let currentTimeframe = 'R1D';
        let currentChartType = 'candle';
        let chartSignature = null;
        const chartWrapper = document.createElement('div');
        chartWrapper.id = 'instrument-chart-section';
        chartWrapper.style.margin = '12px 0';
        panel.appendChild(chartWrapper);

        function renderChart() {
            const focused = chartWrapper.contains(document.activeElement) ? document.activeElement.dataset.chartControl : null;

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
                [['R6H', '6 jam'], ['R1D', '1 hari'], ['R7D', '7 hari'], ['R30D', '1 bulan']].forEach(([tf, label]) => {
                    const btn = document.createElement('button');
                    btn.textContent = label;
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
                    emptyState(chartWrapper, 'Belum ada riwayat pada rentang ini', 'Riwayat ditampilkan dari transaksi yang tercatat. Rentang memakai waktu game; 1 bulan berarti 30 hari terakhir.');
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

                let selectedBar = bars.length - 1;
                const describeBar = () => {
                    const bar = bars[selectedBar];
                    readout.replaceChildren(node('span', '', `${bar.time}${bar.partial ? ' · belum lengkap' : ''}`),
                        node('span', '', `Open ${bar.open} · High ${bar.high} · Low ${bar.low} · Close ${bar.close} · Volume ${bar.volume}`));
                };
                const selection = node('input'); selection.type = 'range'; selection.min = 0;
                selection.max = bars.length - 1; selection.value = selectedBar;
                selection.dataset.chartControl = 'selected-candle';
                selection.setAttribute('aria-label', 'Pilih candle untuk membaca harga dan volume');
                selection.style.width = '100%'; selection.style.minHeight = '48px';
                selection.oninput = () => { selectedBar = Number(selection.value); describeBar(); };
                chartWrapper.append(selection); describeBar();
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
            } else if (tabId === 'pemegang') {
                fundContent.append(node('h3', '', 'Pemegang saham dunia game'));
                mountIssuerOwnership(fundContent, stock.id);
            }

            const provNotice = document.createElement('div');
            provNotice.className = 'notice';
            provNotice.textContent = tabId === 'pemegang'
                ? 'Pemegang dan jumlah saham berasal dari akun yang tercatat dalam domain game, bukan daftar pemegang perusahaan di dunia nyata. Unit reksa dana dan kewajiban short tidak dihitung sebagai pemegang tambahan.'
                : `Sumber identitas: ${fundamentals?.identitySource || companyProfile?.source || 'Belum tersedia'}. Sumber angka: ${fundamentals?.financialSource || 'Belum tersedia'}. Periode: ${fundamentals?.periodType || 'Belum tersedia'}. Angka fundamental berasal dari state simulasi game.`;
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

        const issuerNews = node('section', 'card issuer-news');
        issuerNews.append(node('h2', 'card-title', 'Berita terkait'));
        try {
            const feed = JSON.parse(bridge.getResearchFeed());
            if (feed.success === false || !Array.isArray(feed.news)) throw new Error(feed.message || 'Berita belum dapat dimuat.');
            const related = feed.news.filter(news => news.relatedTicker === stock.ticker || news.affectedSector === stock.sector);
            rememberNews(related);
            if (!related.length) emptyState(issuerNews, 'Belum ada berita terkait', 'Berita yang sudah tersedia akan muncul di sini.');
            related.forEach(news => {
                const item = node('article', 'news-card');
                item.append(node('h3', '', news.headline), node('p', '', `${news.categoryLabel} · ${news.affectedSector} · Perkiraan dampak: ${news.direction}${news.estimatedRange ? ' · ' + news.estimatedRange : ''}`), newsEffectNode(news));
                flowButton(item, 'Baca ' + news.headline, () => openNewsDetail(news, () => openInstrumentDetail(stock)));
                issuerNews.append(item);
            });
        } catch (error) { issuerNews.append(node('p', 'notice', 'Berita terkait belum dapat dimuat. Coba buka kembali rincian emiten.')); }
        panel.append(issuerNews);

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
                } else if (result.status === 'PENDING_TRIGGER') {
                    showGameToast('info', 'Order Pemicu Tersimpan', `${draft.side === 'BUY' ? 'Beli' : 'Jual'} ${draft.quantity} saham ${draft.instrumentId} menunggu harga menyentuh ${draft.triggerPrice || ''}`, 'ORDER_TRIGGER:' + draft.requestId);
                } else if (result.status === 'REJECTED' || result.success === false) {
                    showGameToast('error', 'Order Ditolak', result.message || 'Pesanan ditolak oleh bursa.', 'ORDER_REJECT:' + draft.requestId);
                } else if (result.status === 'CANCELLED') {
                    showGameToast('info', 'Order Dibatalkan', result.message || 'Pesanan dibatalkan.', 'ORDER_CANCEL:' + draft.requestId);
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
        flowText(panel, 'Instrumen / mata uang', `${result.instrumentId || (orderDraft && orderDraft.instrumentId) || '-'} / ${result.currency || orderPreview && orderPreview.currency || '-'}`);
        flowText(panel, 'Pesan', result.message || 'Status dari domain');
        if (result.status === 'CANCELLED') flowText(panel, 'Sisa dibatalkan', `${result.cancelledQuantity ?? result.remainingQuantity ?? 0} saham belum terisi; fill sebelumnya tetap tercatat.`);
        flowText(panel, result.status === 'CANCELLED' ? 'Terisi / tidak terisi' : 'Terisi / sisa', `${result.filledQuantity ?? '-'} / ${result.remainingQuantity ?? '-'} saham`);
        renderReceiptAmounts(panel, result);
        const targetInst = result.instrumentId || (orderDraft && orderDraft.instrumentId);
        if (targetInst && bridge.getInstruments) {
            flowButton(panel, `Lihat ${targetInst} di Pasar`, () => {
                try {
                    const stocks = JSON.parse(bridge.getInstruments('', '', null, true));
                    const target = stocks.find(s => s.id === targetInst || s.ticker === targetInst);
                    if (target) {
                        openInstrumentDetail(target);
                    } else {
                        switchTab('pasar');
                    }
                } catch(e) {
                    switchTab('pasar');
                }
            });
        }
        if (bridge.getOrderStatus) flowButton(panel, 'Periksa status', () => showOrderReceipt(JSON.parse(bridge.getOrderStatus(activeRequestId))));
        if (bridge.cancelOrder && ['OPEN','PARTIALLY_FILLED','PENDING_TRIGGER'].includes(result.status)) {
            flowButton(panel, 'Batalkan sisa', () => {
                if (window.confirm(`Batalkan hanya sisa ${result.remainingQuantity} saham?`)) {
                    try {
                        const cancelRes = JSON.parse(bridge.cancelOrder(activeRequestId));
                        showOrderReceipt(cancelRes);
                        refreshSnapshot();
                        if (cancelRes.success) {
                            const isSell = (orderDraft && orderDraft.side === 'SELL') || cancelRes.side === 'SELL' || result.side === 'SELL';
                            const assetDesc = isSell ? 'Saham tertahan dipulihkan ke posisi.' : 'Kas tertahan dipulihkan ke saldo.';
                            showGameToast('info', 'Order Dibatalkan', `Sisa ${cancelRes.cancelledQuantity ?? result.remainingQuantity} saham dibatalkan. ${assetDesc}`, 'ORDER_CANCEL:' + activeRequestId);
                        } else {
                            showGameToast('info', 'Pembatalan Gagal', cancelRes.message || 'Order tidak dapat dibatalkan.', 'ORDER_CANCEL_FAIL:' + activeRequestId);
                        }
                    } catch(err) {
                        showGameToast('info', 'Pembatalan Gagal', err.message, 'ORDER_CANCEL_ERR:' + activeRequestId);
                    }
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
                            flowText(dCard, 'Tenor', `${d.tenorSessions || d.tenorDays} sesi`);
                            flowText(dCard, 'Bunga berjalan (Accrued)', d.accruedInterest);
                            flowText(dCard, 'Status', d.isMatured ? 'SUDAH JATUH TEMPO (Bunga Penuh)' : 'Sedang Berjalan');

                            const btnWithdraw = document.createElement('button');
                            btnWithdraw.className = d.isMatured ? 'btn-primary' : 'btn-secondary';
                            btnWithdraw.style.marginTop = '6px';
                            btnWithdraw.textContent = d.isMatured ? 'Cairkan Pokok & Bunga' : 'Tarik Dini (Seluruh bunga hangus)';
                            btnWithdraw.onclick = () => {
                                if (!d.isMatured && !confirm('Jika dicairkan sebelum jatuh tempo, seluruh bunga yang sudah terakru hangus. Pokok deposito tetap kembali. Lanjutkan penarikan?')) return;
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
                const empty = node('div', 'empty-state');
                empty.append(
                    node('strong', '', 'Belum ada laporan klien'),
                    node('p', '', 'Belum ada akun mandat klien aktif yang menghasilkan rekapitulasi kinerja atau pemotongan fee berkala.')
                );
                const cta = node('button', 'btn-primary', 'Tinjau tawaran mandat');
                cta.type = 'button';
                cta.style.marginTop = '12px';
                cta.onclick = () => {
                    closeFlow();
                    switchTab('firma');
                    loadFirmaView('dana');
                };
                empty.append(cta);
                panel.append(empty);
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
        const panel = showFlow('Pengajuan Fasilitas Pembiayaan');
        flowText(panel, 'Ketentuan Pembiayaan (A16)', 'Game merekomendasikan opsi terbaik berdasarkan profil Anda. Tanpa agunan barang mewah, suku bunga harian 0.05%, provisi 0.5%, dan denda keterlambatan 5% sederhana setiap 24 jam nyata atas cicilan tertunggak.');

        const pSelectLabel = document.createElement('label');
        pSelectLabel.textContent = 'Jenis produk pembiayaan';
        const pSelect = document.createElement('select');
        pSelect.id = 'loan-product'; pSelectLabel.htmlFor = pSelect.id;
        pSelect.className = 'input-field';
        pSelect.innerHTML = `
            <option value="LOAN-PERS-02" selected>Pembiayaan Tunai Personal (Tanpa Agunan, Plafon s/d Rp 50 Juta)</option>
            <option value="LOAN-CORP-01" ${defaultProductId === 'LOAN-CORP-01' ? 'selected' : ''}>Kredit Modal Kerja Perusahaan (Plafon Rp 5 - 100 Juta)</option>
        `;

        const tenorLabel = document.createElement('label');
        tenorLabel.textContent = 'Pilihan jangka waktu (tenor)';
        const tenorSelect = document.createElement('select');
        tenorSelect.id = 'loan-tenor'; tenorLabel.htmlFor = tenorSelect.id;
        tenorSelect.className = 'input-field';
        tenorSelect.innerHTML = `
            <option value="7">7 Hari Game (Singkat)</option>
            <option value="14" selected>14 Hari Game (Rekomendasi)</option>
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
        companySection.style.display = 'none';

        const collateralSection = document.createElement('div');
        collateralSection.style.display = 'none'; // Agunan dilepas (A16)
        const collateralSelect = document.createElement('select');
        collateralSelect.id = 'loan-collateral';
        collateralSelect.innerHTML = '<option value="">(Tanpa Agunan)</option>';
        collateralSection.append(collateralSelect);

        pSelect.onchange = () => {
            const isCorp = pSelect.value === 'LOAN-CORP-01';
            companySection.style.display = isCorp ? '' : 'none';
        };

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
                const isPersonal = pSelect.value === 'LOAN-PERS-01' || pSelect.value === 'LOAN-PERS-02';
                const selectedCollateral = null;
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
                        '<strong>Rincian & Rekomendasi Pembiayaan</strong><br>' +
                        'Pokok Pinjaman: ' + formatMoney(quoteRes.principalRaw) + '<br>' +
                        'Biaya Provisi (0.5%): ' + formatMoney(quoteRes.feeRaw) + '<br>' +
                        'Dana Bersih Diterima: ' + formatMoney(quoteRes.netCashRaw) + '<br>' +
                        'Jangka Waktu (Tenor): ' + quoteRes.tenorDays + ' hari game<br>' +
                        'Suku Bunga Harian: 0.05% atas sisa pokok<br>' +
                        'Denda Keterlambatan: 5% sederhana per 24 jam nyata (non-majemuk)<br>' +
                        'Agunan: Tanpa Agunan (A16)<br>' +
                        'Aturan kelayakan: ' + (quoteRes.eligibilityRule || 'Rekomendasi disetujui');
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
                            if (pSelect.value !== quoteReq.productId || String(Number(amtInput.value)) !== quoteReq.principal || Number(tenorSelect.value) !== quoteReq.tenorDays || (!isPersonal && companySelect.value !== quoteReq.companyId)) throw new Error('Input berubah. Tinjau ulang quote sebelum konfirmasi.');
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
        try {
            if (!bridge.previewTurnaroundBailout) throw new Error('Pratinjau fase pemulihan belum tersedia.');
            const preview = JSON.parse(bridge.previewTurnaroundBailout());
            if (!preview.eligible) {
                alert(`Fase pemulihan belum tersedia. ${preview.reason}`);
                return;
            }
            const auctionLines = (preview.auctionItems || []).map(item => `• ${item.name}: ${item.estimatedValue}`).join('\n');
            const auctionSummary = auctionLines || '• Tidak ada koleksi layak lelang';
            const confirmation = `Pratinjau pemulihan insolvensi\nKas: ${preview.cash}\nKekayaan bersih: ${preview.netWorth}\nUtang: ${preview.totalDebt}\nPerkiraan hasil lelang: ${preview.estimatedAuctionProceeds}\n\nKoleksi yang akan dilelang:\n${auctionSummary}\n\n${preview.warning}\nKonsultan menggunakan jadwal ulang 30 hari untuk pinjaman tenor dan aturan restrukturisasi margin yang berlaku. Lanjutkan?`;
            if (!confirm(confirmation)) return;
            const reqId = 'BAILOUT-' + Date.now();
            const res = JSON.parse(bridge.executeTurnaroundBailout ? bridge.executeTurnaroundBailout(reqId) : '{"success":false,"message":"Layanan belum tersedia"}');
            if (res.success) {
                alert(`Turnaround selesai. ${res.auctionedCollections} koleksi dilelang; hasil ${res.auctionProceeds} dipakai membayar utang. Tidak ada hibah kas. Reputasi turun ${res.reputationPenalty} poin.`);
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

                const highContrastLabel = node('label', 'check-row');
                const highContrast = node('input'); highContrast.type = 'checkbox';
                highContrast.checked = Boolean(p.preferences && p.preferences.highContrast);
                highContrastLabel.append(highContrast, node('span', '', 'Kontras tinggi'));

                const reducedMotionLabel = node('label', 'check-row');
                const reducedMotion = node('input'); reducedMotion.type = 'checkbox';
                reducedMotion.checked = Boolean(p.preferences && p.preferences.reducedMotion);
                reducedMotionLabel.append(reducedMotion, node('span', '', 'Kurangi gerakan'));

                const fontNotice = node('p', 'notice', 'Ukuran teks otomatis mengikuti pengaturan sistem perangkat Anda.');

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

                prefCard.append(nameLabel, nameInput, highContrastLabel, reducedMotionLabel, fontNotice, saveBtn, profileStatus);
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

    function openCollectionItemDetailModal(item) {
        checkAndRecoverPendingCollectionIntent(item);
        const panel = showFlow('Detail Koleksi: ' + item.name);
        panel.setAttribute('data-collection-detail-id', item.id);

        const categoryLabels = {
            VEHICLE: 'Kendaraan mewah', REAL_ESTATE: 'Properti & vila',
            JEWELRY: 'Perhiasan & berlian', ART_AND_WATCHES: 'Seni & jam tangan',
            MOTOR: 'Motor & kendaraan roda dua', MOBIL: 'Mobil kolektor',
            RUMAH: 'Hunian & properti', KOMERSIAL: 'Properti komersial & lahan',
            JAM: 'Jam tangan', PERMATA: 'Perhiasan & batu mulia', SENI: 'Karya seni',
            ANTIK: 'Barang antik', KAPAL: 'Yacht & kapal', PESAWAT: 'Jet & helikopter',
            TEKNO: 'Teknologi & aset digital', MEMO: 'Memorabilia'
        };
        const rarityLabels = {
            'STANDARD': 'Umum', 'UMUM': 'Umum', 'TIDAK_UMUM': 'Tidak Umum',
            'RARE': 'Langka', 'LANGKA': 'Langka', 'EPIK': 'Epik',
            'LEGENDARY': 'Legendaris', 'LEGENDARIS': 'Legendaris'
        };

        const isOwned = Boolean(item.isOwned || item.owned);
        const isPledged = Boolean(item.isPledged || item.pledgedLoanId);
        const personalCash = latestSnapshot?.personalAccount?.cash?.IDR?.available
            ? (parseInt(String(latestSnapshot.personalAccount.cash.IDR.available).replace(/[^0-9]/g, ''), 10) || 0)
            : 0;
        const valuationPrice = Number(item.currentValuation != null ? item.currentValuation : (item.basePrice || 0));
        const canAfford = personalCash >= valuationPrice;

        const catIndo = categoryLabels[(item.category || '').toUpperCase()] || item.category || 'Aset Mewah';
        const rarityIndo = rarityLabels[(item.rarity || '').toUpperCase()] || item.rarityLabel || item.rarity || 'Standar';

        const card = node('section', 'card');
        const meta = node('div', 'section-heading');
        meta.append(node('span', 'eyebrow', `${catIndo} · ${rarityIndo}`));
        if (isOwned && isPledged) meta.append(node('span', 'badge-pledged', 'Diagunkan'));
        else if (isOwned) meta.append(node('span', 'badge-owned', '✓ Dimiliki'));
        else if (canAfford) meta.append(node('span', 'badge-available', '○ Dapat Dibeli'));
        else meta.append(node('span', 'badge-locked', '🔒 Kas Kurang'));

        const detailImgWrap = node('div', 'collection-visual-frame');
        detailImgWrap.style.background = 'radial-gradient(circle at center, #1e2d42 0%, #0d1522 100%)';
        detailImgWrap.style.borderRadius = '12px';
        detailImgWrap.style.overflow = 'hidden';
        detailImgWrap.style.display = 'flex';
        detailImgWrap.style.alignItems = 'center';
        detailImgWrap.style.justifyContent = 'center';
        detailImgWrap.style.minHeight = '180px';
        detailImgWrap.style.margin = '10px 0';

        const dImg = document.createElement('img');
        dImg.src = `images/collections/${item.id}.webp`;
        dImg.alt = item.name;
        dImg.style.maxWidth = '100%';
        dImg.style.maxHeight = '180px';
        dImg.style.objectFit = 'contain';
        dImg.onerror = () => { dImg.src = `images/collections/${item.id}.png`; };
        detailImgWrap.appendChild(dImg);

        card.append(meta, detailImgWrap, node('h2', 'card-title', item.name));

        flowText(card, 'Status kepemilikan', isOwned ? (isPledged ? '✓ Dimiliki (Diagunkan pada pinjaman)' : '✓ Dimiliki Pemain') : (canAfford ? '○ Tersedia di Galeri' : '🔒 Belum Terbeli (Kas tidak cukup)'));
        flowText(card, 'Valuasi terkini', formatMoney(item.currentValuation));
        flowText(card, 'Harga beli dasar', formatMoney(item.basePrice));
        flowText(card, 'Biaya pemeliharaan harian', item.dailyMaintenanceCost == null ? '-' : `${formatMoney(item.dailyMaintenanceCost)} / hari`);
        flowText(card, 'Bonus reputasi karier', item.reputationBonus == null ? '-' : `+${item.reputationBonus} poin reputasi`);
        flowText(card, 'Status agunan pinjaman', isPledged ? `Diagunkan pada pinjaman ${item.pledgedLoanId} (tidak dapat dijual)` : 'Bebas agunan (dapat dijual sewaktu-waktu)');
        if (item.maintenanceArrears && Number(item.maintenanceArrears) > 0) {
            flowText(card, 'Tunggakan pemeliharaan', formatMoney(item.maintenanceArrears));
        }
        flowText(card, 'Kas pribadi tersedia', formatMoney(personalCash));
        panel.append(card);

        const actions = node('section', 'card');
        actions.append(node('h3', 'card-title', 'Tindakan Transaksi'));
        if (!isOwned) {
            if (canAfford) {
                flowButton(actions, `Konfirmasi Beli Koleksi (${formatMoney(valuationPrice)})`, () => {
                    let reqId = null;
                    try {
                        let actualPrice = valuationPrice;
                        if (bridge.getCollections) {
                            try {
                                const freshRes = JSON.parse(bridge.getCollections());
                                const list = Array.isArray(freshRes) ? freshRes : (freshRes.collections || freshRes.items || []);
                                const fresh = list.find(x => x.id === item.id);
                                if (fresh) {
                                    const freshPrice = Number(fresh.currentValuation != null ? fresh.currentValuation : (fresh.basePrice || 0));
                                    if (freshPrice !== actualPrice) {
                                        clearCollectionIntent(item.id, true);
                                        showGameToast('info', 'Valuasi Koleksi Berubah', `Harga saat ini telah diperbarui menjadi ${formatMoney(freshPrice)}. Tinjau kembali sebelum konfirmasi.`);
                                        openCollectionItemDetailModal(fresh);
                                        return;
                                    }
                                    actualPrice = freshPrice;
                                }
                            } catch(e) {}
                        }
                        reqId = getOrCreateCollectionRequestId(item.id, true, actualPrice);
                        const res = JSON.parse(bridge.transactCollection(JSON.stringify({requestId: reqId, itemId: item.id, buy: true, expectedPrice: actualPrice})));
                        if (!res.success) {
                            if (res.status === 'STALE_VALUATION') {
                                clearCollectionIntent(item.id, true);
                                showGameToast('info', 'Valuasi Koleksi Berubah', res.message || 'Harga telah diperbarui. Tinjau kembali sebelum konfirmasi.');
                                if (bridge.getCollections) {
                                    try {
                                        const freshRes = JSON.parse(bridge.getCollections());
                                        const list = Array.isArray(freshRes) ? freshRes : (freshRes.collections || freshRes.items || []);
                                        const fresh = list.find(x => x.id === item.id);
                                        if (fresh) { openCollectionItemDetailModal(fresh); return; }
                                    } catch(e) {}
                                }
                                refreshSnapshot();
                                return;
                            }
                            if (/kas.*tidak mencukupi|bukan milik|diagunkan/i.test(res.message || '')) {
                                clearCollectionIntent(item.id, true);
                            }
                            throw new Error(res.message);
                        }
                        clearCollectionIntent(item.id, true);
                        const transAmount = res.amount ? formatMoney(res.amount) : formatMoney(actualPrice);
                        showGameToast('success', 'Koleksi Berhasil Dibeli!', `${item.name} berhasil dibeli seharga ${transAmount}. Masuk ke galeri aset mewah.`);
                        refreshSnapshot();
                        closeFlow();
                        loadFirmaView('koleksi');
                    } catch(err) {
                        showGameToast('info', 'Pembelian Gagal', err.message);
                    }
                });
            } else {
                actions.append(node('p', 'notice', `Kas pribadi Anda (${formatMoney(personalCash)}) belum mencukupi untuk membeli ${item.name} pada valuasi terkini (${formatMoney(valuationPrice)}). Tambah saldo kas dari hasil trading atau dividen usaha.`));
            }
        } else {
            if (isPledged) {
                actions.append(node('p', 'notice', `Barang koleksi ini sedang dijadikan agunan untuk pinjaman ${item.pledgedLoanId}. Lunasi pinjaman terlebih dahulu untuk melepaskan hak jual.`));
            } else {
                flowButton(actions, `Konfirmasi Jual Koleksi (Terima ${formatMoney(item.currentValuation)})`, () => {
                    let reqId = null;
                    try {
                        let actualPrice = Number(item.currentValuation != null ? item.currentValuation : (item.basePrice || 0));
                        if (bridge.getCollections) {
                            try {
                                const freshRes = JSON.parse(bridge.getCollections());
                                const list = Array.isArray(freshRes) ? freshRes : (freshRes.collections || freshRes.items || []);
                                const fresh = list.find(x => x.id === item.id);
                                if (fresh) {
                                    const freshPrice = Number(fresh.currentValuation != null ? fresh.currentValuation : (fresh.basePrice || 0));
                                    if (freshPrice !== actualPrice) {
                                        clearCollectionIntent(item.id, false);
                                        showGameToast('info', 'Valuasi Koleksi Berubah', `Harga jual saat ini telah diperbarui menjadi ${formatMoney(freshPrice)}. Tinjau kembali sebelum konfirmasi.`);
                                        openCollectionItemDetailModal(fresh);
                                        return;
                                    }
                                    actualPrice = freshPrice;
                                }
                            } catch(e) {}
                        }
                        reqId = getOrCreateCollectionRequestId(item.id, false, actualPrice);
                        const res = JSON.parse(bridge.transactCollection(JSON.stringify({requestId: reqId, itemId: item.id, buy: false, expectedPrice: actualPrice})));
                        if (!res.success) {
                            if (res.status === 'STALE_VALUATION') {
                                clearCollectionIntent(item.id, false);
                                showGameToast('info', 'Valuasi Koleksi Berubah', res.message || 'Harga telah diperbarui. Tinjau kembali sebelum konfirmasi.');
                                if (bridge.getCollections) {
                                    try {
                                        const freshRes = JSON.parse(bridge.getCollections());
                                        const list = Array.isArray(freshRes) ? freshRes : (freshRes.collections || freshRes.items || []);
                                        const fresh = list.find(x => x.id === item.id);
                                        if (fresh) { openCollectionItemDetailModal(fresh); return; }
                                    } catch(e) {}
                                }
                                refreshSnapshot();
                                return;
                            }
                            if (/bukan milik|diagunkan/i.test(res.message || '')) {
                                clearCollectionIntent(item.id, false);
                            }
                            throw new Error(res.message);
                        }
                        clearCollectionIntent(item.id, false);
                        const transAmount = res.amount ? formatMoney(res.amount) : formatMoney(actualPrice);
                        showGameToast('success', 'Koleksi Berhasil Terjual!', `${item.name} berhasil dijual seharga ${transAmount}.`);
                        refreshSnapshot();
                        closeFlow();
                        loadFirmaView('koleksi');
                    } catch(err) {
                        showGameToast('info', 'Penjualan Gagal', err.message);
                    }
                });
            }
        }
        panel.append(actions);
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

    function selectedOrderType() {
        return document.querySelector('input[name="order-type"]:checked')?.value || 'LIMIT';
    }

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
                    showGameToast('info', 'Akun Aktif Beralih', `Sekarang mengelola ${chosen.name} (${chosen.type === 'CLIENT' ? 'Akun Klien' : 'Akun Pribadi'}).`);
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
        updateOrderEstimate();
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
    window.showGameToast = showGameToast;

    btnCloseModal.addEventListener('click', closeOrderTicket);

    function updateOrderEstimate() {
        if (!selectedInstrument) {
            estTotalDisplay.textContent = 'Biaya dan perkiraan kas akan ditampilkan sebelum order dikirim.';
            return;
        }
        const availEl = document.getElementById('order-cash-avail');
        const reservedEl = document.getElementById('order-cash-reserved');
        if (currentAccount && latestSnapshot?.personalAccount?.cash && selectedInstrument) {
            const cur = selectedInstrument.currency || 'IDR';
            const cashObj = latestSnapshot.personalAccount.cash[cur];
            if (availEl && cashObj) availEl.textContent = cashObj.available || '-';
            if (reservedEl && cashObj) reservedEl.textContent = cashObj.reserved || '-';
        }
        const type = selectedOrderType();
        inputOrderLimit.disabled = type === 'MARKET';
        inputOrderLimit.hidden = type === 'MARKET';
        document.getElementById('order-price-label').hidden = type === 'MARKET';
        const side = document.querySelector('.side-btn.active')?.dataset.side;
        document.getElementById('order-price-label').textContent =
            `${type === 'STOP_LOSS' || type === 'TAKE_PROFIT' ? 'Harga pemicu' : side === 'SELL' ? 'Harga jual minimal' : 'Harga beli maksimal'} (${selectedInstrument.currency})`;
        const qtyLots = Number(inputOrderQty.value);
        const price = type === 'MARKET' ? Number(selectedInstrument.currentPriceRaw) : Number(inputOrderLimit.value);
        const reviewButton = document.getElementById('btn-submit-order');
        reviewButton.textContent = side === 'SELL' ? 'Tinjau jual' : side === 'BUY' ? 'Tinjau beli' : 'Pilih beli atau jual';
        reviewButton.disabled = !side || !currentAccount || !Number.isSafeInteger(qtyLots) || qtyLots <= 0 || !(price > 0);
        if (reviewButton.disabled) { estTotalDisplay.textContent = 'Pilih sisi dan isi jumlah yang valid.'; return; }
        const draft = {accountId: currentAccount.id, instrumentId: selectedInstrument.id, side, type,
            quantity: qtyLots * selectedInstrument.lotSize, requestId: 'PREVIEW-TICKET'};
        if (type === 'LIMIT') draft.limitPrice = inputOrderLimit.value;
        if (type === 'STOP_LOSS' || type === 'TAKE_PROFIT') draft.triggerPrice = inputOrderLimit.value;
        try {
            const quote = bridgeResult('previewOrder', JSON.stringify(draft));
            estTotalDisplay.replaceChildren(node('strong', '', `Estimasi kas ${side === 'BUY' ? 'keluar' : 'masuk'}: ${quote.estimatedCashImpact}`),
                node('small', '', `Biaya ${quote.estimatedFee} · Kas tersedia ${quote.availableCash} · Perkiraan terisi ${quote.estimatedFillQuantity} saham`));
        } catch (error) {
            reviewButton.disabled = true;
            estTotalDisplay.textContent = error.message || 'Estimasi belum dapat dimuat. Periksa input kembali.';
        }
    }
    document.querySelectorAll('input[name="order-type"]').forEach(option => option.addEventListener('change', updateOrderEstimate));

    inputOrderQty.addEventListener('input', updateOrderEstimate);
    inputOrderLimit.addEventListener('input', updateOrderEstimate);

    // Tombol Cepat Porsi Modal (25%, 50%, 75%, MAX)
    document.querySelectorAll('.btn-order-pct').forEach(btn => {
        btn.addEventListener('click', () => {
            if (!selectedInstrument) return;
            const pct = Number(btn.dataset.pct) || 100;
            const selectedSide = document.querySelector('.side-btn.active');
            const side = selectedSide?.dataset.side;
            if (!side || !currentAccount) { document.getElementById('order-error').textContent = 'Pilih akun dan sisi order terlebih dahulu.'; return; }
            try {
                const capacity = bridgeResult('getOrderCapacity', currentAccount.id, selectedInstrument.id, side, selectedOrderType(), inputOrderLimit.value);
                inputOrderQty.value = Math.floor(Number(capacity.maxLots) * pct / 100);
                document.getElementById('order-error').textContent = Number(inputOrderQty.value) === 0 ? 'Belum ada jumlah yang dapat dipesan untuk porsi ini.' : '';
            } catch (error) {
                inputOrderQty.value = 0;
                document.getElementById('order-error').textContent = error.message;
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
        inputOrderLimit.setCustomValidity(selectedOrderType() === 'MARKET' || Number(limitP) > 0 ? '' : 'Harga perlu positif.');
        if (!inputOrderQty.reportValidity() || !inputOrderLimit.reportValidity()) return;
        const totalShares = qtyLots * selectedInstrument.lotSize;

        const type = selectedOrderType();
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
            if (!preview.success) {
                errorDisplay.textContent = preview.message || 'Preview ditolak domain';
                errorDisplay.focus();
                showGameToast('error', 'Penempatan Order Ditolak', preview.message || 'Order ditolak oleh bursa / aturan risiko.');
                return;
            }
            showOrderReview(preview, payload);
        } catch (error) {
            errorDisplay.textContent = 'Preview gagal: ' + error.message;
            errorDisplay.focus();
            showGameToast('error', 'Gagal Menyiapkan Order', error.message || 'Kesalahan komunikasi bridge.');
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

    document.getElementById('btn-order-history').addEventListener('click', openPortfolioOrders);

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
                step: '1. Dunia game berjalan otomatis',
                desc: 'Waktu, pasar, dan bisnis terus berkembang. Saat aktif, 1 hari game berlangsung 5 menit nyata. Saat aplikasi ditutup, 1 hari game berlangsung 1 jam nyata dan hasil aktivitas diperbarui saat kamu kembali.',
                action: 'Buka Pasar',
                handler: () => {
                    closeFlow();
                    switchTab('pasar');
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
            if (bridge.getAlgorithmicMarketSignal) {
                const signalCard = node('section', 'card');
                flowText(signalCard, 'Sinyal algoritma sesi berjalan', 'Menggunakan perubahan harga sesi saat ini saja; tidak membaca data masa depan dan bukan rekomendasi investasi.');
                flowButton(signalCard, 'Hitung sinyal BBCA', () => {
                    try {
                        const signal = JSON.parse(bridge.getAlgorithmicMarketSignal('IDX:BBCA'));
                        if (!signal.available) throw new Error(signal.reason || 'Jalur algoritma belum terbuka');
                        flowText(signalCard, `Arah: ${signal.direction}`, `Perubahan sesi ${Number(signal.observedChangePercent).toFixed(2)}% · bonus sinyal ${signal.signalBonusPercent}% · ambang ${Number(signal.triggerThresholdPercent).toFixed(2)}%`);
                    } catch(error) { flowText(signalCard, 'Sinyal tidak tersedia', error.message); }
                });
                panel.append(signalCard);
            }
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
                            showGameToast('success', 'Staf Berhasil Direkrut!', `${res.name} berhasil dibajak ke firma Anda.`);
                            refreshSnapshot();
                            openHeadhuntingModal();
                        } catch(err) {
                            showGameToast('info', 'Gagal Membajak Staf', err.message);
                        }
                    };
                    card.appendChild(poachBtn);
                }

                panel.appendChild(card);
            });
        } catch(e) {
            flowText(panel, 'Galat', e.message);
        }
    }

    function openDailyStreakModal() {
        const panel = showFlow('Jalur Disiplin Harian (Streak)');
        flowText(panel, 'Kedisiplinan & Multiplier Modal', 'Lakukan aktivitas transaksi atau login setiap sesi pasar berturut-turut untuk melipatgandakan bonus modal harian hingga 2.0x lipat.');

        if (!bridge.getDailyStreak) {
            flowText(panel, 'Status', 'Layanan streak harian belum tersedia.');
            return;
        }

        try {
            const streak = JSON.parse(bridge.getDailyStreak());
            const card = node('section', 'card');
            card.innerHTML = `
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                    <span class="eyebrow" style="color:#FF7300; font-weight:700;">🔥 ${streak.statusLabel}</span>
                    <span class="badge" style="background:rgba(255, 115, 0, 0.18); color:#FF7300; font-weight:700;">Bonus ${streak.rewardMultiplier}</span>
                </div>
                <div class="data-row"><span>Streak Saat Ini</span><strong>${streak.currentStreak} Hari Beruntun</strong></div>
                <div class="data-row"><span>Rekor Streak Terpanjang</span><strong>${streak.longestStreak} Hari</strong></div>
                <div class="data-row"><span>Pengali Hadiah Modal</span><strong style="color:var(--teal);">${streak.rewardMultiplier}</strong></div>
                <p class="notice" style="margin-top:10px; font-size:0.8rem;">Pertahankan disiplin trading harian Anda. Melewatkan 1 hari pasar akan memicu Broken Streak dan mereset pengali kembali ke awal.</p>
            `;
            panel.append(card);
        } catch(e) { flowText(panel, 'Galat', e.message); }
    }

    function openSectorDominanceModal() {
        const panel = showFlow('Status Dominasi Sektor Bursa');
        flowText(panel, 'Penguasa & Pengaruh Sektor Industri', 'Akumulasi kepemilikan saham di atas 25% kapitalisasi pasar satu sektor untuk dinobatkan sebagai Penguasa Sektor dengan hak veto dan diskon komisi 25%.');

        if (!bridge.getSectorDominance) {
            flowText(panel, 'Status', 'Layanan dominasi sektor belum tersedia.');
            return;
        }

        try {
            const sectors = JSON.parse(bridge.getSectorDominance());
            sectors.forEach(s => {
                const card = node('section', 'card');
                card.style.marginBottom = '10px';
                if (s.isDominant) {
                    card.style.borderColor = 'rgba(20, 241, 149, 0.4)';
                    card.style.background = 'linear-gradient(135deg, rgba(20, 241, 149, 0.08) 0%, rgba(18, 23, 40, 0.95) 100%)';
                }
                card.innerHTML = `
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
                        <span class="eyebrow" style="color:${s.isDominant ? 'var(--teal)' : 'var(--text-muted)'}; font-weight:700;">${s.title}</span>
                        <span class="badge" style="background:${s.isDominant ? 'rgba(20, 241, 149, 0.18)' : 'rgba(255, 255, 255, 0.08)'}; color:${s.isDominant ? 'var(--teal)' : 'var(--text-muted)'};">${s.isDominant ? 'PENGUASA' : 'REGULER'}</span>
                    </div>
                    <h3 style="font-size:1.05rem; color:#FFF; margin:0 0 6px;">${s.sectorName}</h3>
                    <div class="data-row"><span>Porsi Kepemilikan Pemain</span><strong>${s.playerOwnershipPct} (Ambang: 25.00%)</strong></div>
                    <div class="data-row"><span>Hak Veto Kebijakan</span><strong>${s.vetoPowerActive ? 'AKTIF ✓' : 'Tidak Aktif'}</strong></div>
                    <div class="data-row"><span>Diskon Komisi Transaksi</span><strong style="color:var(--teal);">${s.feeDiscountPct}</strong></div>
                `;
                panel.append(card);
            });
        } catch(e) { flowText(panel, 'Galat', e.message); }
    }

    function openAccountPicker() {
        if (!latestSnapshot || !latestSnapshot.tradingAccounts) return;
        const panel = showFlow('Pilih Akun Transaksi');
        flowText(panel, 'Keterangan', 'Pilih akun yang digunakan untuk portofolio dan penempatan order. Kekayaan pribadi di kanan atas tetap terpisah dari dana klien (AUM).');
        latestSnapshot.tradingAccounts.forEach(acc => {
            const isSelected = currentAccount && currentAccount.id === acc.id;
            flowButton(panel, `${acc.name} (${acc.type})${isSelected ? ' [Aktif]' : ''}`, () => {
                currentAccount = { id: acc.id, name: acc.name, type: acc.type };
                showGameToast('info', 'Akun Aktif Beralih', `Sekarang mengelola ${acc.name} (${acc.type === 'CLIENT' ? 'Akun Klien' : 'Akun Pribadi'}).`);
                closeFlow();
                refreshSnapshot();
                if (currentTab === 'aset') loadAsetView();
            });
        });
    }

    function openReservedCashAllocationModal() {
        refreshSnapshot();
        const panel = showFlow('Rincian Dana Tertahan');
        flowText(panel, 'Dana Tertahan', 'Kas atau efek yang sedang dicadangkan untuk order antrean aktif, penawaran korporasi, atau transaksi in-flight.');
        const reservedTotal = latestSnapshot?.personalAccount?.cash?.IDR?.reserved || 'Rp 0';
        flowText(panel, 'Total Kas Tertahan (IDR)', reservedTotal);

        const listCard = node('section', 'card');
        listCard.append(node('h2', 'card-title', 'Alokasi per Order Aktif'));
        try {
            if (bridge.getOrders && currentAccount) {
                const orders = JSON.parse(bridge.getOrders(currentAccount.id, true));
                const reservedOrders = orders.filter(o => o.side === 'BUY' && ['OPEN', 'PARTIALLY_FILLED', 'PENDING_TRIGGER'].includes(o.status));
                if (!reservedOrders.length) {
                    emptyState(listCard, 'Tidak ada alokasi tertahan', 'Saat ini tidak ada kas yang sedang dicadangkan untuk order.');
                } else {
                    reservedOrders.forEach(o => {
                        const row = node('div', 'data-row');
                        row.style.padding = '8px 0';
                        const isTrigger = o.status === 'PENDING_TRIGGER';
                        const statusTxt = isTrigger ? 'Menunggu Pemicu' : 'Mengantre di Bursa';
                        const labelDiv = node('div');
                        labelDiv.innerHTML = `<strong>${o.instrumentId} (Beli)</strong><br><small style="color:var(--text-secondary);">${o.remainingQuantity} saham · ${statusTxt}</small>`;
                        const valDiv = node('div');
                        valDiv.style.textAlign = 'right';
                        const btn = node('button', 'btn-secondary', 'Lihat Tiket');
                        btn.style.minHeight = '38px';
                        btn.style.padding = '4px 10px';
                        btn.style.fontSize = '0.75rem';
                        btn.onclick = () => {
                            activeRequestId = o.requestId;
                            showOrderReceipt(o);
                        };
                        valDiv.append(btn);
                        row.append(labelDiv, valDiv);
                        listCard.append(row);
                    });
                }
            }
        } catch(e) {
            flowText(listCard, 'Gagal memuat alokasi', e.message);
        }
        panel.append(listCard);
    }

    const resCashBtn = document.getElementById('home-reserved-cash-btn');
    if (resCashBtn) {
        resCashBtn.addEventListener('click', openReservedCashAllocationModal);
        resCashBtn.addEventListener('keydown', e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); openReservedCashAllocationModal(); } });
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
    const strkBtn = document.getElementById('btn-home-streak');
    if (strkBtn) strkBtn.addEventListener('click', openDailyStreakModal);
    const domBtn = document.getElementById('btn-home-dominance');
    if (domBtn) domBtn.addEventListener('click', openSectorDominanceModal);
    const insBtn = document.getElementById('btn-home-insurance');
    if (insBtn) insBtn.addEventListener('click', openPortfolioInsuranceModal);

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
        flowText(panel, 'Ukuran teks', 'Aplikasi mengikuti skala teks sistem bawaan perangkat Anda.');

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
    if (bridge.beginClockSync) requestClockSync(!document.hidden).then(() => { refreshSnapshot(); loadHomeMarket(); }).catch(() => {});
    else { if (bridge.onAppLifecycleChanged) bridge.onAppLifecycleChanged(!document.hidden); refreshSnapshot(); loadHomeMarket(); }

    window.showNativeSaveFailure = () => {
        if (document.getElementById('native-save-warning')) return;
        const warning = document.createElement('p');
        warning.id = 'native-save-warning'; warning.setAttribute('role', 'alert');
        warning.className = 'mock-banner';
        warning.textContent = 'Progres terakhir belum tersimpan. Buka Pengaturan untuk mencoba simpan ulang.';
        document.body.prepend(warning);
    };

    // Force-close resilience: autosave on backgrounding, swipe-away, or pagehide
    const autoSaveOnExit = () => {
        try {
            if (bridge && bridge.beginClockSync) { bridge.beginClockSync(false); return; }
            if (bridge && bridge.onAppLifecycleChanged) bridge.onAppLifecycleChanged(false);
            if (bridge && bridge.saveGame) bridge.saveGame();
        } catch(e) {}
    };
    document.addEventListener('visibilitychange', () => {
        if (document.hidden) {
            autoSaveOnExit();
        } else {
            if (bridge.beginClockSync) {
                requestClockSync(true).then(() => { refreshSnapshot(); loadHomeMarket(); }).catch(() => {});
                return;
            }
            if (bridge && bridge.onAppLifecycleChanged) bridge.onAppLifecycleChanged(true);
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
        updateNewsEffects();
    }, 250);

    setInterval(() => {
        try {
            if (document.hidden || clockSyncBlocked) return;
            if (bridge.beginClockSync) {
                requestClockSync(true).then(() => {
                    document.getElementById('clock-error')?.remove();
                    refreshSnapshot();
                    if (currentTab === 'aset') syncPortfolioLive();
                    if (activeReceiptRequestId) updateReceiptLive(activeReceiptRequestId);
                    if (activeDetailStock) updateInstrumentDetailLive(activeDetailStock.id);
                    if (['beranda', 'pasar'].includes(currentTab) && !flowPanel) updateMarketQuotes();
                }).catch(cause => {
                    const error = document.getElementById('clock-error') || node('p');
                    error.id = 'clock-error'; error.setAttribute('role', 'alert');
                    error.textContent = 'Penyelarasan waktu bursa otomatis terhenti: ' + (cause.message || 'Status gagal') + '. Data terakhir tetap ditampilkan.';
                    if (!error.isConnected) document.querySelector('main')?.prepend(error);
                });
                return;
            }
            if (bridge && bridge.syncClock) {
                const tick = JSON.parse(bridge.syncClock());
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

    window.__qaOrder = {
        showOrderReceipt,
        updateReceiptLive,
        showOrderReview,
        setActiveRequestId: id => { activeRequestId = id; }
    };
})();
