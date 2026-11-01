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
    function safeUUID() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }
    function generateSafeId(prefix = 'REQ') {
        return `${prefix}-${safeUUID()}`;
    }
    function formatMoney(value, currency = 'IDR', fractionDigits = currency === 'USD' ? 2 : 0) {
        if (value == null || value === '') return 'Belum tersedia';
        const amount=Number(value);
        return Number.isFinite(amount) ? (currency === 'IDR' ? 'Rp' : currency === 'USD' ? 'US$' : currency) + ' ' + amount.toLocaleString('id-ID',{minimumFractionDigits:fractionDigits, maximumFractionDigits:fractionDigits}) : 'Belum tersedia';
    }

    function parseCleanPrice(val) {
        if (val == null) return 0;
        if (typeof val === 'number') return val;
        let str = String(val).trim().replace(/[^0-9.,]/g, '');
        if (str.includes('.') && str.includes(',')) {
            return parseFloat(str.replace(/\./g, '').replace(',', '.')) || 0;
        }
        if (str.includes('.')) {
            const parts = str.split('.');
            if (parts.length > 1 && parts[parts.length - 1].length === 3) {
                return parseFloat(str.replace(/\./g, '')) || 0;
            }
        }
        return parseFloat(str) || 0;
    }

    let clockSyncRunning = false;
    let clockSyncBlocked = false;
    const bridgeReadCache = new Map();
    const bridge = new Proxy(window.TradingNativeBridge, {
        get(target, method) {
            const native = target[method];
            if (typeof native !== 'function') return native;
            return (...args) => {
                const control = ['beginClockSync', 'getClockSyncStatus', 'saveGame', 'getPendingBackupRecoveryOffer',
                    'confirmBackupRecovery', 'rejectBackupRecovery', 'getPlatformInfo', 'onAppLifecycleChanged'].includes(method);
                const read = /^(get|preview|quote|list|can)(?=[A-Z])/.test(method);
                const key = JSON.stringify([method, args]);
                if ((clockSyncRunning || clockSyncBlocked) && !control) {
                    if (read) return bridgeReadCache.get(key) || '{}';
                    throw new Error('Transaksi menunggu penyelarasan dan penyimpanan progres.');
                }
                const result = native.apply(target, args);
                if (read) {
                    bridgeReadCache.set(key, result);
                    if (bridgeReadCache.size > 200) bridgeReadCache.delete(bridgeReadCache.keys().next().value);
                }
                return result;
            };
        }
    });

    // 2. DOM Elements
    const mockBanner = document.getElementById('mock-banner');
    const headerTitle = document.getElementById('header-title');
    const clockDisplay = document.getElementById('clock-display');
    const sessionDot = document.getElementById('session-dot');
    const sessionLabel = document.getElementById('session-label');
    const modeLabel = document.getElementById('mode-label');

    const views = {
        beranda: document.getElementById('view-beranda'),
        pasar_porto: document.getElementById('view-pasar_porto'),
        pasar: document.getElementById('subtab-content-pasar') || document.getElementById('view-pasar'),
        aset: document.getElementById('subtab-content-porto') || document.getElementById('view-aset'),
        firma: document.getElementById('view-firma'),
        keuangan: document.getElementById('view-keuangan'),
        riset: document.getElementById('view-riset')
    };

    let currentPasarPortoSubtab = 'pasar';

    function setPasarPortoSubtab(sub) {
        currentPasarPortoSubtab = sub;
        const btnPasar = document.getElementById('subtab-btn-pasar');
        const btnPorto = document.getElementById('subtab-btn-porto');
        const cPasar = document.getElementById('subtab-content-pasar');
        const cPorto = document.getElementById('subtab-content-porto');
        if (btnPasar) btnPasar.classList.toggle('active', sub === 'pasar');
        if (btnPorto) btnPorto.classList.toggle('active', sub === 'porto');
        if (cPasar) cPasar.style.display = sub === 'pasar' ? 'block' : 'none';
        if (cPorto) cPorto.style.display = sub === 'porto' ? 'block' : 'none';
        if (sub === 'pasar') loadPasarView();
        else loadAsetView();
    }

    const btnSubPasar = document.getElementById('subtab-btn-pasar');
    if (btnSubPasar) btnSubPasar.onclick = () => setPasarPortoSubtab('pasar');
    const btnSubPorto = document.getElementById('subtab-btn-porto');
    if (btnSubPorto) btnSubPorto.onclick = () => setPasarPortoSubtab('porto');

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
        const alias = {
            ringkasan: 'beranda',
            portofolio: 'aset',
            bisnis: 'firma',
            karier: 'firma',
            lainnya: 'riset'
        };
        if (alias[tab]) tab = alias[tab];

        // Routing otomatis Pasar & Portofolio ke combined tab (Mockup 4 Tabs)
        if (tab === 'pasar') {
            currentPasarPortoSubtab = 'pasar';
            tab = 'pasar_porto';
        } else if (tab === 'aset') {
            currentPasarPortoSubtab = 'porto';
            tab = 'pasar_porto';
        }

        if (!views[tab]) return;
        if (flowPanel) closeFlow();
        const main = document.querySelector('.app-scroll');
        scrollPositions[currentTab] = main.scrollTop;
        currentTab = tab;
        headerTitle.textContent = {
            beranda: 'Beranda',
            pasar_porto: 'Pasar & Portofolio',
            firma: 'Bisnis',
            keuangan: 'Pusat Keuangan',
            riset: 'Lainnya'
        }[tab] || 'Game Saham';

        document.querySelectorAll('.nav-item').forEach(b => {
            const matches = b.dataset.tab === tab || b.dataset.tabName === tab;
            b.classList.toggle('active', matches);
            if (matches) b.setAttribute('aria-current', 'page'); else b.removeAttribute('aria-current');
        });
        Object.keys(views).forEach(key => {
            views[key].classList.toggle('active', key === tab);
        });

        if (tab === 'pasar_porto') {
            setPasarPortoSubtab(currentPasarPortoSubtab);
        }
        if (tab === 'firma') loadFirmaView();
        if (tab === 'keuangan') loadKeuanganView();
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
    let lastKnownSessionLabel = null;
    let clockSyncPromise = null;
    let clockChaseFrom = 0;
    let clockChaseTo = 0;
    let clockChasePerf = 0;
    let clockChaseFrame = 0;
    let lastSnapshotCacheAt = -Infinity;

    let recoveryOfferVisible = false;
    let recoveryOfferRejected = false;
    function showPendingBackupRecovery() {
        if (recoveryOfferVisible || recoveryOfferRejected || !bridge.getPendingBackupRecoveryOffer) return;
        try {
            const result = JSON.parse(bridge.getPendingBackupRecoveryOffer());
            if (!result.offerAvailable || !result.offer) return;
            recoveryOfferVisible = true;
            const panel = showFlow('Pemulihan save diperlukan', true);
            const when = new Date(result.offer.backupLastModified).toLocaleString('id-ID');
            flowText(panel, 'Save utama rusak', `Cadangan terakhir: ${when}. Pemulihan dapat menghilangkan progres setelah waktu tersebut.`);
            const status = node('p'); status.setAttribute('role', 'alert'); panel.append(status);
            flowButton(panel, 'Pulihkan cadangan', () => {
                try {
                    const restored = JSON.parse(bridge.confirmBackupRecovery());
                    if (!restored.success) throw new Error(restored.message || restored.error || 'Pemulihan gagal');
                    recoveryOfferVisible = false; closeFlow(true);
                    requestClockSync(true).then(() => refreshSnapshot()).catch(() => {});
                } catch (error) { status.textContent = error.message; }
            });
            flowButton(panel, 'Jangan pulihkan', () => {
                try {
                    const rejected = JSON.parse(bridge.rejectBackupRecovery());
                    if (!rejected.success) throw new Error(rejected.message || 'Pilihan gagal disimpan');
                    recoveryOfferVisible = false; recoveryOfferRejected = true;
                    panel.replaceChildren(node('h2', '', 'Pemulihan ditunda'), node('p', '', 'Save utama dan cadangan tetap dipertahankan. Tutup aplikasi untuk meninjau pilihan pemulihan.'));
                } catch (error) { status.textContent = error.message; }
            });
        } catch (error) { console.error('Penawaran pemulihan tidak dapat dimuat', error); }
    }

    function requestClockSync(foreground = !document.hidden, isManual = false) {
        if (!bridge.beginClockSync) return Promise.resolve();
        if (clockSyncRunning) { bridge.beginClockSync(foreground); return clockSyncPromise; }
        clockChaseFrom = clockChaseTo = latestSnapshot?.clock?.timestamp || anchorGameTimestamp;
        clockChasePerf = performance.now();
        clockSyncRunning = true;
        clockSyncBlocked = true;
        if (clockDisplay) clockDisplay.setAttribute('aria-busy', 'true');
        cancelAnimationFrame(clockChaseFrame);
        const draw = () => {
            renderFlowingClock();
            if (clockSyncRunning && !document.hidden) clockChaseFrame = requestAnimationFrame(draw);
        };
        draw();
        clockSyncPromise = new Promise((resolve, reject) => {
            function finish(error, snap) {
                clockSyncRunning = false;
                cancelAnimationFrame(clockChaseFrame);
                if (clockDisplay) clockDisplay.removeAttribute('aria-busy');
                if (error) {
                    anchorGameTimestamp = clockChaseTo || anchorGameTimestamp;
                    anchorPerfNow = performance.now();
                    anchorRate = 0;
                    window.showNativeSaveFailure?.();
                    showPendingBackupRecovery();
                    reject(error);
                    return;
                }
                clockSyncBlocked = false;
                if (snap) {
                    anchorGameTimestamp = Number(snap.clock?.timestamp) || clockChaseTo;
                    anchorPerfNow = performance.now();
                    anchorRate = Number(snap.clock?.rate) || 48;
                    refreshSnapshot(snap);
                    if (performance.now() - lastSnapshotCacheAt >= 30000) {
                        try {
                            localStorage.setItem('game_last_snapshot', JSON.stringify(snap));
                            lastSnapshotCacheAt = performance.now();
                        } catch (unavailableCache) { /* Cached presentation is optional; domain save owns progress. */ }
                    }
                }
                resolve(snap);
            }
            function poll() {
                try {
                    const state = JSON.parse(bridge.getClockSyncStatus());
                    if (state.status === 'PENDING') {
                        const progress = state.progress;
                        if (progress && Number.isFinite(Number(progress.currentGameTimestamp))) {
                            clockChaseFrom = clockChaseTo || Number(progress.fromGameTimestamp);
                            clockChaseTo = Number(progress.currentGameTimestamp);
                            clockChasePerf = performance.now();
                            renderFlowingClock();
                        }
                        setTimeout(poll, 100);
                        return;
                    }
                    if (state.success === false || state.status !== 'COMPLETED')
                        throw new Error(state.message || 'Progres belum dapat diselaraskan.');
                    finish(null, state.snapshot);
                } catch (error) { finish(error); }
            }
            try { bridge.beginClockSync(foreground); poll(); }
            catch (error) { finish(error); }
        });
        return clockSyncPromise;
    }

    function renderFlowingClock() {
        if ((!anchorGameTimestamp && !clockChaseTo) || document.hidden) return;
        const elapsedRealMs = performance.now() - anchorPerfNow;
        const reducedMotion = document.documentElement.classList.contains('reduced-motion')
                || latestSnapshot?.playerProfile?.preferences?.reducedMotion
                || matchMedia('(prefers-reduced-motion: reduce)').matches;
        const chaseFraction = reducedMotion ? 1 : Math.min(1, Math.max(0, performance.now() - clockChasePerf) / 100);
        const currentSimMs = clockSyncRunning
                ? clockChaseFrom + (clockChaseTo - clockChaseFrom) * chaseFraction
                : anchorGameTimestamp + elapsedRealMs * anchorRate;
        // Waktu game dihitung dalam offset WIB (UTC+7)
        const date = new Date(currentSimMs + 7 * 3600000);
        const hours = String(date.getUTCHours()).padStart(2, '0');
        const mins = String(date.getUTCMinutes()).padStart(2, '0');
        if (clockDisplay) {
            const timeStr = `${hours}:${mins} WIB`;
            if (clockDisplay.textContent !== timeStr) {
                clockDisplay.textContent = timeStr;
            }
        }

        if (clockSyncRunning && currentSimMs > 0) {
            const dateEl = document.getElementById('home-date');
            const base = Number(latestSnapshot?.clock?.timestamp) || currentSimMs;
            const daysAdvanced = Math.floor((currentSimMs + 7*3600000) / 86400000) - Math.floor((base + 7*3600000) / 86400000);
            const day = Number(latestSnapshot?.career?.days);
            const calendar = new Date(currentSimMs).toLocaleDateString('id-ID', {timeZone:'Asia/Jakarta', day:'2-digit', month:'long', year:'numeric'});
            if (dateEl) dateEl.textContent = (Number.isFinite(day) ? 'Hari ' + (day + daysAdvanced) + ' · ' : '') + calendar;
        }

        // A08: Pintasan waktu dinonaktifkan sesuai keputusan dunia bursa berjalan otomatis 48x
        const btnSkip = document.getElementById('btn-skip-break');
        if (btnSkip) {
            btnSkip.style.display = 'none';
        }
    }

    // =========================================================================
    // TYCOON CHILL LOUNGE AMBIENT BGM (Procedural Web Audio API Synthesizer)
    // =========================================================================
    let audioCtx = null;
    let bgmPlaying = false;
    let bgmMasterGain = null;
    let bgmTimer = null;

    function initAudioContext() {
        if (!audioCtx) {
            const AudioContext = window.AudioContext || window.webkitAudioContext;
            if (AudioContext) {
                audioCtx = new AudioContext();
                bgmMasterGain = audioCtx.createGain();
                bgmMasterGain.gain.value = 0.22; // Volume latar belakang santai
                bgmMasterGain.connect(audioCtx.destination);
            }
        }
        if (audioCtx && audioCtx.state === 'suspended') {
            audioCtx.resume();
        }
    }

    // Akor Jazz/Lounge Santai (Cmaj9, Am9, Dm9, G13, Em7)
    const loungeChords = [
        [261.63, 329.63, 392.00, 493.88, 587.33], // Cmaj9
        [220.00, 261.63, 329.63, 392.00, 493.88], // Am9
        [293.66, 349.23, 440.00, 523.25, 659.25], // Dm9
        [196.00, 246.94, 293.66, 349.23, 440.00, 659.25], // G13
        [164.81, 246.94, 329.63, 392.00, 493.88]  // Em7
    ];
    let chordIndex = 0;

    function playLoungeChord(notes) {
        if (!audioCtx || !bgmPlaying) return;
        const now = audioCtx.currentTime;
        notes.forEach((freq, idx) => {
            const osc = audioCtx.createOscillator();
            const noteGain = audioCtx.createGain();
            const filter = audioCtx.createBiquadFilter();

            osc.type = idx === 0 ? 'sine' : 'triangle';
            osc.frequency.setValueAtTime(freq, now);

            filter.type = 'lowpass';
            filter.frequency.setValueAtTime(750 + idx * 70, now);

            noteGain.gain.setValueAtTime(0.0001, now);
            noteGain.gain.exponentialRampToValueAtTime(0.07 / notes.length, now + 0.18 + idx * 0.05);
            noteGain.gain.exponentialRampToValueAtTime(0.0001, now + 3.8);

            osc.connect(filter);
            filter.connect(noteGain);
            noteGain.connect(bgmMasterGain);

            osc.start(now + idx * 0.05);
            osc.stop(now + 4.0);
        });
    }

    function toggleBgm() {
        initAudioContext();
        if (bgmTimer) {
            clearTimeout(bgmTimer);
            bgmTimer = null;
        }
        bgmPlaying = !bgmPlaying;
        const btn = document.getElementById('btn-toggle-bgm');
        if (bgmPlaying) {
            if (btn) {
                btn.innerHTML = '🎵 Musik: ON';
                btn.style.borderColor = 'var(--positive)';
                btn.style.color = 'var(--positive)';
            }
            showGameToast('success', 'Backsound Aktif', 'Musik santai bursa diputar di latar belakang.');
            const loop = () => {
                if (!bgmPlaying) return;
                playLoungeChord(loungeChords[chordIndex % loungeChords.length]);
                chordIndex++;
                bgmTimer = setTimeout(loop, 3600);
            };
            loop();
        } else {
            if (btn) {
                btn.innerHTML = '🎵 Musik';
                btn.style.borderColor = 'var(--border)';
                btn.style.color = '';
            }
            showGameToast('info', 'Musik Disenyapkan', 'Backsound dijeda.');
            if (bgmTimer) clearTimeout(bgmTimer);
        }
    }

    const btnBgmEl = document.getElementById('btn-toggle-bgm');
    if (btnBgmEl) {
        btnBgmEl.onclick = () => toggleBgm();
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

    let cachedClaimSponsorBtn = null;
    let cachedQaSponsor = null;
    function updateSponsorCountdown() {
        if (!cachedClaimSponsorBtn) cachedClaimSponsorBtn = document.getElementById('btn-claim-sponsor');
        if (!cachedQaSponsor) cachedQaSponsor = document.getElementById('btn-qa-sponsor');
        const claimSponsorBtn = cachedClaimSponsorBtn;
        const qaSponsor = cachedQaSponsor;
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
                claimSponsorBtn.textContent = `Klaim ${latestDailySponsor.grantAmountFormatted || 'Rp 25.000.000'}`.trim();
            }
            if (qaSponsor) {
                qaSponsor.disabled = false;
                qaSponsor.setAttribute('aria-label', `Klaim Hibah Sponsor ${latestDailySponsor.grantAmountFormatted || ''}`.trim());
            }
            const sponsorCard = document.getElementById('home-sponsor-card');
            if (sponsorCard && latestDailySponsor.grantAmountFormatted) {
                const pill = sponsorCard.querySelector('.change-pill');
                if (pill) pill.textContent = '+' + latestDailySponsor.grantAmountFormatted;
                const pDesc = sponsorCard.querySelector('p');
                if (pDesc) pDesc.textContent = `Klaim modal gratis ${latestDailySponsor.grantAmountFormatted} (termasuk bonus streak login) setiap hari bursa untuk modal trading.`;
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
        const theme = (preferences.theme || 'EMAS_KLASIK').toUpperCase();
        if (theme === 'MIDNIGHT_NEON') {
            document.documentElement.setAttribute('data-theme', 'midnight-neon');
        } else {
            document.documentElement.setAttribute('data-theme', 'emas-klasik');
        }
    }
    function persistPlayerPreferences(patch) {
        const profile=bridgeResult('getPlayerProfile');
        const preferences=Object.assign({},profile.preferences || {},patch);
        const result=bridgeResult('updatePlayerProfile',JSON.stringify({playerName:profile.playerName,preferences}));
        applyPlayerPreferences(result.preferences || preferences);
        return result;
    }
    function refreshSnapshot(providedSnap) {
        if (clockSyncRunning && !providedSnap) return;
        try {
            const snap = providedSnap || (bridge.getSnapshot ? JSON.parse(bridge.getSnapshot()) : null);
            if (!snap) return;
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
                    flowText(panel, 'Status', 'Data sesi permainan belum dapat dimuat.');
                    flowText(panel, 'Petunjuk', 'Berkas cadangan tersimpan aman. Tekan tombol di bawah untuk memuat cadangan valid terakhir.');
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
                if (pnl !== 'Rp 0' && pnl !== 'Rp0') {
                    showGameToast('info', 'Penyelarasan Waktu Offline', `Simulasi mengejar waktu offline. Perubahan nilai: ${pnl}.`, 'OFFLINE_CATCHUP:' + catchupTs);
                }
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

            // SINTESA MODUL 5: Status Siklus Makroekonomi
            if (bridge.getMacroAndSynthesisState) {
                try {
                    const macro = JSON.parse(bridge.getMacroAndSynthesisState());
                    const mBadge = document.getElementById('macro-badge');
                    if (mBadge && macro.macroIndex != null) {
                        const idx = macro.macroIndex;
                        let label = `Makro Lv.${idx}`;
                        let cls = 'neutral';
                        if (idx <= 3) {
                            label = `Resesi (Lv.${idx})`;
                            cls = 'neg';
                        } else if (idx >= 7) {
                            label = `Booming (Lv.${idx})`;
                            cls = 'pos';
                        } else {
                            label = `Normal (Lv.${idx})`;
                            cls = 'neutral';
                        }
                        mBadge.textContent = label;
                        mBadge.className = `m-tag ${cls}`;
                        mBadge.title = `Siklus: ${macro.macroLabel} · Bunga Deposito ${Number(macro.depositRateAnnual).toFixed(2)}% · Pinjaman ${Number(macro.loanRateAnnual).toFixed(2)}%`;
                    }
                } catch(e) {}
            }

            if (snap.playerProgress) {
                const lvlEl = document.getElementById('header-level-val');
                const xpEl = document.getElementById('header-xp-val');
                if (lvlEl) lvlEl.textContent = `Lv. ${snap.playerProgress.level}`;
                if (xpEl) xpEl.textContent = `${snap.playerProgress.xpInCurrentLevel} / ${snap.playerProgress.levelSpanXp} XP`;
                const pct = Math.max(0, Math.min(100, 100 * snap.playerProgress.xpInCurrentLevel / (snap.playerProgress.levelSpanXp || 1)));
                const track = document.getElementById('home-xp-track');
                if (track && track.firstElementChild) {
                    track.firstElementChild.style.width = `${pct}%`;
                    track.setAttribute('aria-valuenow', pct);
                }
                const achCountEl = document.getElementById('home-achievement-count');
                if (achCountEl) achCountEl.textContent = `${snap.playerProgress.achievementsUnlocked}/${snap.playerProgress.totalAchievements} pencapaian diraih`;

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
                const cTitleEl = document.getElementById('career-agenda-title');
                if (cTitleEl) cTitleEl.textContent = snap.career.title;
                const cMisEl = document.getElementById('home-mission-progress');
                if (cMisEl) cMisEl.textContent = `${snap.career.missions.filter(m => m.completed).length}/${snap.career.missions.length} misi selesai`;
            }
            if (snap.clock) {
                const dateEl = document.getElementById('home-date');
                if (dateEl) dateEl.textContent = `${snap.career ? 'Hari ' + snap.career.days + ' · ' : ''}${snap.clock.date}`;
            }
            const accNameEl = document.getElementById('context-account-name');
            if (accNameEl) {
                accNameEl.textContent = currentAccount ? (currentAccount.type === 'CLIENT' ? 'Klien: ' + currentAccount.name : (currentAccount.name || 'Pribadi')) : 'Pribadi';
            }

            // Live Sync ke sub-layar yang sedang aktif
            if (currentTab === 'aset' || (currentTab === 'pasar_porto' && currentPasarPortoSubtab === 'porto')) syncPortfolioLive();
            if (currentTab === 'pasar' || (currentTab === 'pasar_porto' && currentPasarPortoSubtab === 'pasar')) updateMarketQuotes();
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

    function navigateToStockMarketDetail(instrumentId, ticker, exchange) {
        if (!bridge || !bridge.getInstruments) return;
        try {
            const stocks = JSON.parse(bridge.getInstruments(ticker, exchange, null, true));
            const found = stocks.find(s => s.id === instrumentId || s.ticker === ticker);
            if (found) { openInstrumentDetail(found); return; }
        } catch(e) {}
        try {
            const all = JSON.parse(bridge.getInstruments(null, null, null, false));
            const found = all.find(s => s.id === instrumentId || s.ticker === ticker);
            if (found) { openInstrumentDetail(found); return; }
        } catch(e) {}
    }

    function renderPositionCard(pos) {
        const card = node('article', 'position-card position-row compact-stockbit-row');
        card.dataset.instrumentId = pos.instrumentId;
        card.tabIndex = 0;
        card.setAttribute('role', 'button');
        const lotInfo = pos.lotSize > 0 ? ` (${pos.lots} lot)` : '';
        const sharesText = `${Number(pos.quantity).toLocaleString('id-ID')} saham${lotInfo}`;
        card.setAttribute('aria-label', `Posisi ${pos.ticker || pos.instrumentId} ${pos.exchange || ''}, ${sharesText}, nilai ${pos.marketValue}, laba rugi ${pos.unrealizedPnL} (${pos.unrealizedPnLPercent || '0.00'}%)`);

        // Ketika diklik, langsung menuju ke pasar emitennya
        card.onclick = () => navigateToStockMarketDetail(pos.instrumentId, pos.ticker, pos.exchange);
        card.onkeydown = e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); navigateToStockMarketDetail(pos.instrumentId, pos.ticker, pos.exchange); } };

        // Kolom 1: Ticker & Total Modal (Invested)
        const col1 = node('div', 'port-col-ticker');
        col1.append(node('strong', 'pos-ticker', pos.ticker || pos.instrumentId));
        const investedAmount = Number(pos.averageCost || 0) * Number(pos.quantity || 0);
        col1.append(node('small', 'pos-invested', formatPositionMoney(investedAmount, pos.currency)));

        // Kolom 2: Nominal P&L & Label P&L
        const pnlRaw = Number(pos.unrealizedPnLRaw || 0);
        const pnlClass = pnlRaw > 0 ? 'val-pos' : pnlRaw < 0 ? 'val-neg' : 'val-neutral';
        const sign = pnlRaw > 0 ? '+' : '';
        const col2 = node('div', 'port-col-pnl');
        col2.append(node('strong', `pos-pnl-val ${pnlClass}`, `${sign}${pos.unrealizedPnL}`));
        col2.append(node('small', 'pos-pnl-label', 'P&L'));

        // Kolom 3: Persentase P&L & Status (Gain / Loss / Impas)
        const col3 = node('div', 'port-col-pct');
        const statusLabel = pnlRaw > 0 ? 'Gain' : pnlRaw < 0 ? 'Loss' : 'Impas';
        col3.append(node('strong', `pos-pct-val ${pnlClass}`, `${sign}${pos.unrealizedPnLPercent || '0.00'}%`));
        col3.append(node('small', `pos-status-label ${pnlClass}`, statusLabel));

        // Chevron
        const chevron = node('div', 'port-chevron', '›');

        card.append(col1, col2, col3, chevron);
        return card;
    }

    function updatePositionCard(card, pos) {
        const investedEl = card.querySelector('.pos-invested');
        if (investedEl) {
            const investedAmount = Number(pos.averageCost || 0) * Number(pos.quantity || 0);
            investedEl.textContent = formatPositionMoney(investedAmount, pos.currency);
        }

        const pnlRaw = Number(pos.unrealizedPnLRaw || 0);
        const pnlClass = pnlRaw > 0 ? 'val-pos' : pnlRaw < 0 ? 'val-neg' : 'val-neutral';
        const sign = pnlRaw > 0 ? '+' : '';

        const pnlValEl = card.querySelector('.pos-pnl-val');
        if (pnlValEl) {
            pnlValEl.className = `pos-pnl-val ${pnlClass}`;
            pnlValEl.textContent = `${sign}${pos.unrealizedPnL}`;
        }

        const pctValEl = card.querySelector('.pos-pct-val');
        if (pctValEl) {
            pctValEl.className = `pos-pct-val ${pnlClass}`;
            pctValEl.textContent = `${sign}${pos.unrealizedPnLPercent || '0.00'}%`;
        }

        const statusEl = card.querySelector('.pos-status-label');
        if (statusEl) {
            statusEl.className = `pos-status-label ${pnlClass}`;
            statusEl.textContent = pnlRaw > 0 ? 'Gain' : pnlRaw < 0 ? 'Loss' : 'Impas';
        }

        const lotInfo = pos.lotSize > 0 ? ` (${pos.lots} lot)` : '';
        const sharesText = `${Number(pos.quantity).toLocaleString('id-ID')} saham${lotInfo}`;
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
        if (container) container.innerHTML = '';
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
            if (status) status.style.display = 'none';

            const totalShares = Number(snapshot.sharesOutstanding || snapshot.issuedShares || 0);
            const rawHolders = snapshot.holders || [];

            // Tentukan Pemegang Pengendali & Publik Resmi
            let ctrlName = 'PT Dwimuria Investama (Pengendali)';
            let ctrlPct = 54.94;
            if (stockId.includes('BMRI') || stockId.includes('BBRI') || stockId.includes('TLKM')) {
                ctrlName = 'Negara Republik Indonesia (Pengendali)';
                ctrlPct = 52.00;
            } else if (stockId.includes('ASII')) {
                ctrlName = 'Jardine Cycle & Carriage Ltd (Pengendali)';
                ctrlPct = 50.11;
            } else if (stockId.includes('AMRT')) {
                ctrlName = 'PT Sigmantara Alfindo (Pengendali)';
                ctrlPct = 52.74;
            } else if (stockId.includes('UNTR')) {
                ctrlName = 'PT Astra International Tbk (Pengendali)';
                ctrlPct = 59.50;
            } else if (stockId.includes('NUSA')) {
                ctrlName = 'PT Nusantara Mega Holding (Pengendali)';
                ctrlPct = 51.00;
            } else if (stockId.includes('ARTA')) {
                ctrlName = 'Artha Graha Sentosa Tbk (Pengendali)';
                ctrlPct = 53.20;
            }

            const ctrlShares = Math.round(totalShares * (ctrlPct / 100));
            let mappedTotal = 0;
            rawHolders.forEach(h => { mappedTotal += Number(h.quantity || 0); });
            const publicShares = Math.max(0, totalShares - ctrlShares - mappedTotal);
            const publicPct = Number((100 - ctrlPct).toFixed(2));

            meta.replaceChildren();
            const summaryBox = document.createElement('div');
            summaryBox.style.cssText = 'background:var(--surface); border:1px solid rgba(227, 189, 127, 0.2); border-radius:12px; padding:10px 12px; margin-bottom:12px; box-sizing:border-box; width:100%; min-width:0;';
            const totalSharesDisplay = totalShares >= 1000000000
                ? (totalShares / 1000000000).toFixed(2) + ' Miliar Lbr'
                : (totalShares > 0 ? totalShares.toLocaleString('id-ID') + ' Lbr' : 'Belum tersedia');

            summaryBox.innerHTML = `
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px; min-width:0;">
                    <span style="font-size:0.75rem; color:var(--text-secondary); text-transform:uppercase; font-weight:700;">Total Saham Beredar</span>
                    <strong style="color:var(--text); font-size:0.88rem; white-space:nowrap;">${totalSharesDisplay}</strong>
                </div>
                <div style="height:10px; border-radius:5px; overflow:hidden; display:flex; margin:8px 0 6px; background:rgba(255,255,255,0.08); width:100%; box-sizing:border-box;">
                    <div style="width:${ctrlPct}%; background:#F59E0B;" title="Pengendali ${ctrlPct}%"></div>
                    <div style="width:${publicPct}%; background:#10B981;" title="Publik ${publicPct}%"></div>
                </div>
                <div style="display:flex; flex-wrap:wrap; gap:10px; font-size:0.72rem; color:var(--text-secondary);">
                    <span><strong style="color:#F59E0B;">■</strong> Pengendali (${ctrlPct.toFixed(1)}%)</span>
                    <span><strong style="color:#10B981;">■</strong> Publik (${publicPct.toFixed(1)}%)</span>
                </div>
            `;
            meta.appendChild(summaryBox);

            list.replaceChildren();
            const displayCards = [];

            displayCards.push({
                name: ctrlName,
                quantity: ctrlShares,
                percent: ctrlPct,
                tag: 'Pengendali Utama',
                tagBg: 'rgba(245,158,11,0.2)',
                tagColor: '#F59E0B'
            });

            displayCards.push({
                name: 'Masyarakat / Investor Publik',
                quantity: publicShares,
                percent: publicPct,
                tag: 'Free Float',
                tagBg: 'rgba(16,185,129,0.2)',
                tagColor: '#10B981'
            });

            let otherSmallHoldersShares = 0;
            rawHolders.forEach(holder => {
                const qty = Number(holder.quantity || 0);
                if (qty > 0) {
                    const pct = (qty / totalShares) * 100;
                    // Aturan Bursa & Pemilik: Pemegang saham di bawah 1% tidak ditampilkan namanya (kecuali pemain sendiri)
                    if (pct >= 1.0 || holder.isPersonalPlayer) {
                        displayCards.push({
                            name: holder.name + (holder.isPersonalPlayer ? ' (Saya)' : ''),
                            quantity: qty,
                            percent: pct,
                            tag: holder.isPersonalPlayer ? 'Portofolio Saya' : 'Pemegang Saham >1%',
                            tagBg: holder.isPersonalPlayer ? 'rgba(20,241,149,0.2)' : 'rgba(255,255,255,0.08)',
                            tagColor: holder.isPersonalPlayer ? 'var(--teal)' : 'var(--text-muted)'
                        });
                    } else {
                        otherSmallHoldersShares += qty;
                    }
                }
            });

            if (otherSmallHoldersShares > 0) {
                const otherPct = (otherSmallHoldersShares / totalShares) * 100;
                displayCards.push({
                    name: 'Pemegang Saham Publik Lainnya (< 1%)',
                    quantity: otherSmallHoldersShares,
                    percent: otherPct,
                    tag: 'Ritel & Publik',
                    tagBg: 'rgba(255,255,255,0.06)',
                    tagColor: 'var(--text-muted)'
                });
            }

            displayCards.forEach(item => {
                const card = document.createElement('div');
                card.className = 'shareholder-card';
                card.style.cssText = 'background:var(--surface); border:1px solid rgba(227, 189, 127, 0.15); border-radius:10px; padding:10px 12px; margin-bottom:8px; box-sizing:border-box; width:100%; min-width:0;';
                
                const sharesFormatted = item.quantity >= 1000000000
                    ? (item.quantity / 1000000000).toFixed(2) + ' Miliar Lbr'
                    : (item.quantity >= 1000000
                        ? (item.quantity / 1000000).toFixed(2) + ' Juta Lbr'
                        : Number(item.quantity).toLocaleString('id-ID') + ' Lbr');

                card.innerHTML = `
                    <div style="display:flex; justify-content:space-between; align-items:center; gap:8px; margin-bottom:4px; min-width:0;">
                        <strong style="color:#FFF; font-size:0.86rem; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; min-width:0; flex:1;">${item.name}</strong>
                        <span class="badge" style="background:${item.tagBg}; color:${item.tagColor}; font-weight:700; font-size:0.68rem; flex-shrink:0; white-space:nowrap;">${item.tag}</span>
                    </div>
                    <div style="display:flex; justify-content:space-between; align-items:center; font-variant-numeric:tabular-nums; min-width:0;">
                        <span style="color:var(--text-secondary); font-size:0.78rem;">${sharesFormatted}</span>
                        <strong style="color:var(--text); font-size:0.95rem; font-weight:700; flex-shrink:0;">${item.percent < 0.01 ? '<0.01%' : item.percent.toFixed(2) + '%'}</strong>
                    </div>
                `;
                list.appendChild(card);
            });
        } catch (error) {
            if (status) {
                status.style.display = 'block';
                status.setAttribute('role', 'alert');
                status.textContent = 'Pemegang saham gagal dimuat: ' + error.message;
            }
        }
        panel.removeAttribute('aria-busy');
    }

    function mountIssuerOwnership(parent, stockId) {
        const panel = node('section', 'issuer-ownership');
        panel.id = 'issuer-ownership-panel';
        panel.dataset.instrumentId = stockId;
        panel.setAttribute('aria-label', 'Struktur Pemegang Saham');
        panel.setAttribute('aria-busy', 'true');
        const status = node('p', 'notice ownership-status', 'Memuat pemegang saham…');
        status.setAttribute('role', 'status');
        panel.append(status, node('div', 'ownership-meta'), node('div', 'ownership-list'));
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
                const oldVal = parseCleanPrice(metricHero.textContent);
                const newVal = parseCleanPrice(fresh.currentPriceRaw || fresh.currentPrice);
                metricHero.textContent = fresh.currentPrice;
                metricHero.classList.remove('price-flash-up', 'price-flash-down');
                void metricHero.offsetWidth;
                metricHero.classList.add(newVal >= oldVal ? 'price-flash-up' : 'price-flash-down');
            }

            const pill = flowPanel.querySelector('.change-pill');
            if (pill) {
                const isPos = Number(fresh.dailyChangePercent) >= 0;
                pill.className = isPos ? 'change-pill positive' : 'change-pill negative';
                pill.textContent = `${isPos ? '+' : ''}${fresh.dailyChangePercent}% (Hari Ini)`;
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

            // SINTESA REALTIME CANDLESTICK & OHLC UPDATE (GAYA REA TYCOON)
            const curPNum = parseCleanPrice(fresh.currentPriceRaw || fresh.currentPrice);
            if (curPNum > 0 && flowPanel._chartBars && flowPanel._chartBars.length > 0) {
                const lastBar = flowPanel._chartBars[flowPanel._chartBars.length - 1];
                if (lastBar) {
                    lastBar.close = curPNum;
                    if (curPNum > lastBar.high) lastBar.high = curPNum;
                    if (curPNum < lastBar.low) lastBar.low = curPNum;

                    // Update OHLC Readout Live
                    const readout = flowPanel.querySelector('#ohlc-readout');
                    if (readout) {
                        readout.innerHTML = `
                            <span><strong>Waktu:</strong> ${lastBar.time}</span>
                            <span><strong>O:</strong> ${lastBar.open}</span>
                            <span><strong>H:</strong> ${lastBar.high}</span>
                            <span><strong>L:</strong> ${lastBar.low}</span>
                            <span><strong>C:</strong> ${lastBar.close}</span>
                            <span><strong>Vol:</strong> ${lastBar.volume}</span>
                        `;
                    }

                    // Update SVG Live Active Candle
                    const liveWick = flowPanel.querySelector('#live-candle-wick');
                    const liveBody = flowPanel.querySelector('#live-candle-body');
                    if (liveWick && liveBody && typeof flowPanel._calcY === 'function') {
                        const isUp = lastBar.close >= lastBar.open;
                        const col = isUp ? 'var(--positive)' : 'var(--negative)';
                        const yHigh = flowPanel._calcY(lastBar.high);
                        const yLow = flowPanel._calcY(lastBar.low);
                        const yOpen = flowPanel._calcY(lastBar.open);
                        const yClose = flowPanel._calcY(lastBar.close);
                        const bodyY = Math.min(yOpen, yClose);
                        const bodyH = Math.max(2, Math.abs(yClose - yOpen));
                        liveWick.setAttribute('y1', yHigh);
                        liveWick.setAttribute('y2', yLow);
                        liveWick.setAttribute('stroke', col);
                        liveBody.setAttribute('y', bodyY);
                        liveBody.setAttribute('height', bodyH);
                        liveBody.setAttribute('fill', col);
                    }
                }
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
                const captionText = (idx.ticker === 'IHSG') ? 'Bursa Efek Indonesia' : ((idx.ticker === 'LQ45') ? '45 Emiten Terlikuid' : (idx.name || 'Indeks Saham'));
                item.innerHTML = `
                    <div class="index-top">
                        <span>${idx.ticker}</span>
                        <span class="eyebrow">${idx.available === false ? 'Global' : 'Saham'}</span>
                    </div>
                    <strong>${idx.value}</strong>
                    <div class="index-change-val ${idx.available === false ? '' : idx.up ? 'positive' : 'negative'}">
                        ${idx.changePercent || ''}
                    </div>
                    <div class="index-caption">${captionText}</div>
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
                const priceStr = stock.currentPrice ? ` · ${stock.currentPrice}` : '';
                button.append(node('span', '', `${index + 1}. ${stock.ticker}${priceStr}`), node('strong', '', `${percent > 0 ? '+' : ''}${percent.toLocaleString('id-ID', {maximumFractionDigits: 2})}%`));
                button.setAttribute('aria-label', `Detail ${stock.ticker} ${stock.exchange || ''}, ${percent > 0 ? 'naik' : 'turun'} ${Math.abs(percent)} persen`);
                button.onclick = () => openInstrumentDetail(stock); section.append(button);
            });
            if (groups[key].length === 0) {
                section.append(node('p', 'small muted', 'Belum ada fluktuasi signifikan pada sesi ini.'));
            }
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
        const listStatus = document.getElementById('stock-list-status');
        if (listStatus) {
            listStatus.textContent = stocks.length
                ? ''
                : watchlistOnly && !query.trim() && !exchange && !sector ? 'Watchlist masih kosong.' : 'Tidak ada emiten untuk filter ini.';
        }

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
                    <div class="stock-ticker flex-row" style="align-items:center; gap:6px;">
                        <span>${st.ticker}</span>
                        <span class="badge">${st.exchange}</span>
                        ${st.isSectorLocked ? `<span class="badge-locked" style="background:rgba(227,189,127,0.18); color:var(--gold); border:1px solid var(--gold); border-radius:6px; padding:2px 6px; font-size:0.65rem; font-weight:700;">🔒 Lv.${st.requiredLevel}</span>` : ''}
                        <button class="watchlist-star-btn" type="button" aria-label="${isFav ? 'Hapus pantauan ' + st.ticker : 'Pantau ' + st.ticker}" aria-pressed="${isFav}" style="background:none; border:none; color:var(--gold); font-size:1.15rem; cursor:pointer; padding:0 2px; line-height:1; min-height:auto; min-width:auto;">
                            <span aria-hidden="true">${isFav ? '★' : '☆'}</span>
                        </button>
                    </div>
                    <div class="stock-name" style="font-size:0.75rem; color:var(--text-secondary); white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${st.name}</div>
                </div>
                <div style="text-align:right; flex-shrink:0;">
                    <div class="stock-price" style="font-weight:700; font-size:0.95rem;">${st.currentPrice}</div>
                    <div style="margin-top:2px;"><span class="change-pill ${changeClass}">${changeSign}${st.dailyChangePercent}%</span></div>
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
                const query = document.getElementById('search-stock-input').value.toLowerCase().trim();
                const exchange = document.getElementById('filter-exchange-select').value;
                const sector = document.getElementById('filter-sector-select').value;
                const watchlistOnly = document.getElementById('filter-list-select').value === 'watchlist';
                const filtered = fresh.filter(stock => {
                    const matchQ = !query || stock.ticker.toLowerCase().includes(query) || (stock.name && stock.name.toLowerCase().includes(query));
                    const matchEx = !exchange || stock.exchange === exchange;
                    const matchSec = !sector || (stock.sector && stock.sector.toLowerCase() === sector.toLowerCase());
                    const matchWatch = !watchlistOnly || stock.isWatchlist;
                    return matchQ && matchEx && matchSec && matchWatch;
                });
                renderMarketHighlights(filtered);
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
        let summary = document.getElementById('asset-summary') || document.getElementById('portfolio-summary-card') || (views.aset && views.aset.querySelector('.card'));
        if (!summary) {
            summary = node('div', 'card asset-overview');
            summary.id = 'asset-summary';
            if (views.aset) views.aset.prepend(summary);
        } else {
            summary.id = 'asset-summary';
            summary.replaceChildren();
            summary.className = 'asset-overview';
        }
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
                const allocation = node('section','card'); allocation.append(node('h2','card-title','Komposisi Aset'));
                flowText(allocation,'Kas Tunai',portfolio.cashValueIdr);
                flowText(allocation,'Investasi Saham',portfolio.positionsValueIdr);
                if (portfolio.totalUnrealizedPnL) {
                    flowText(allocation, 'Laba/Rugi Mengambang', portfolio.totalUnrealizedPnL);
                }
                if (portfolio.totalRealizedPnL) {
                    flowText(allocation, 'Keuntungan Terealisasi', portfolio.totalRealizedPnL);
                }
                if (portfolio.positionsSharePercent != null) {
                    const pctSaham = Math.round(Number(portfolio.positionsSharePercent) || 0);
                    const pctKas = Math.max(0, 100 - pctSaham);
                    const pctLabel = node('div', '', `Porsi Saham: ${pctSaham}% · Kas: ${pctKas}%`);
                    pctLabel.style.fontSize = '0.78rem';
                    pctLabel.style.color = 'var(--text-secondary)';
                    pctLabel.style.marginBottom = '6px';
                    allocation.append(pctLabel);

                    const bar = node('div','allocation-bar'); bar.setAttribute('role','img'); bar.setAttribute('aria-label',`${portfolio.positionsSharePercent}% aset berupa posisi saham`);
                    const fill = node('span'); fill.style.width = `${Math.max(0,Math.min(100,Number(portfolio.positionsSharePercent)))}%`; bar.append(fill); allocation.append(bar);
                }
                flowText(allocation,'Kewajiban',portfolio.liabilities);
                summary.append(allocation);
                const wallets = node('section','card');
                wallets.id = 'portfolio-wallets-card';
                wallets.append(node('h2','card-title','Dompet IDR & USD'));
                flowText(wallets, 'Total Kekayaan Bersih Konsolidasi', portfolio.netWorth || (latestSnapshot && latestSnapshot.personalAccount ? latestSnapshot.personalAccount.netWorth : 'Belum tersedia'));
                for (const currency of ['IDR', 'USD']) {
                    const cash = portfolio.cash[currency];
                    flowText(wallets, `${currency} Total`, cash.total);
                    flowText(wallets, `${currency} Tersedia`, cash.available);
                    flowText(wallets, `${currency} Tertahan`, cash.reserved);
                }
                const btnAlloc = node('button', 'btn-secondary', 'Lihat Rincian Dana Tertahan');
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
                    { id: 'corporate-actions', label: 'Aksi korporasi & voting RUPS', run: () => openCorporateActionModal() }
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
        if (!bridge.getOrders || !currentAccount) { flowText(panel, 'Riwayat', 'Riwayat transaksi belum tersedia.'); return; }
        
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
        if (btnUsaha) {
            btnUsaha.onclick = () => loadFirmaView('usaha');
            if (bridge.getCompanies) {
                try {
                    const cList = JSON.parse(bridge.getCompanies() || '[]');
                    btnUsaha.textContent = `🏢 Usaha (${cList.length})`;
                } catch(e) {}
            }
        }
        if (btnKoleksi) {
            btnKoleksi.onclick = () => loadFirmaView('koleksi');
            if (bridge.getCollections) {
                try {
                    const colData = JSON.parse(bridge.getCollections() || '{}');
                    const list = Array.isArray(colData) ? colData : (colData.collections || []);
                    const ownedCount = list.filter(x => x.isOwned).length;
                    btnKoleksi.textContent = `💎 Koleksi (${ownedCount})`;
                } catch(e) {}
            }
        }
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
        flowText(compSection, 'Kekayaan Terintegrasi', 'Ekuitas dan kas anak usaha otomatis terkonsolidasi ke kekayaan Anda.');

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
                        node('p', '', 'Dirikan anak usaha untuk menghasilkan dividen harian dan mendiversifikasi aset bisnis. Modal awal mulai Rp 25 Juta.')
                    );
                    const createCta = node('button', 'btn-primary', '+ Dirikan Perusahaan Baru');
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
                            node('span', 'badge', c.level == null ? 'Level 1' : `Level ${c.level}`),
                            node('span', 'badge-status', c.isHalted ? 'Tersuspensi' : 'Operasional Normal')
                        );
                        cCard.append(meta, node('h3', '', c.name));
                        flowText(cCard, 'Kas operasional', formatMoney(c.cash));
                        const profitRow = flowText(cCard, 'Perkiraan laba bersih', Number.isFinite(netProfit) ? `${netProfit >= 0 ? '+' : ''}${formatMoney(netProfit)} / hari` : null);
                        if (Number.isFinite(netProfit) && profitRow) profitRow.querySelector('strong').className = netProfit >= 0 ? 'positive' : 'negative';
                        flowText(cCard, 'Staf & eksekutif', `${c.employeeCount == null ? '0 pegawai' : c.employeeCount + ' pegawai'} · CEO: ${c.ceoName || 'Belum ditunjuk'}`);

                        const btnOpen = document.createElement('button');
                        btnOpen.className = 'btn-primary';
                        btnOpen.style.marginTop = '12px';
                        btnOpen.textContent = 'Kelola Usaha';
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
        flowText(card, 'Kriteria pembentukan', 'Bentuk konglomerasi holding dari 4 sektor usaha. Nikmati fasilitas transfer modal dan dividen internal antar-anak usaha tanpa potongan pajak bursa.');
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
        const panel = showFlow('Transfer Modal Holding');
        flowText(panel,'Ketentuan','Pindahkan kas modal/dividen antar anak usaha. Cadangan payroll harian wajib tersisa.');
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
        flowButton(panel,'Kirim Transfer Modal',() => {
            try {
                if (!from.value || !to.value || from.value===to.value || Number(amount.value)<=0) throw new Error('Pilih dua anak usaha berbeda dan nominal positif.');
                const result=JSON.parse(bridge.transferHoldingCapital(JSON.stringify({requestId:generateSafeId('HOLD-TRF'),holdingId:holding.holdingId,fromCompanyId:from.value,toCompanyId:to.value,amount:String(amount.value),transferType:kind.value})));
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
        catRow.style.cssText = 'display:flex; gap:6px; overflow-x:auto; padding-bottom:8px; margin-bottom:10px; scrollbar-width:none; flex-wrap:nowrap;';
        
        // Filter Rarity Sesuai Catatan Pemilik (Comment 1)
        const rarities = [
            { id: 'ALL', label: `Semua (${items.length})` },
            { id: 'UMUM', label: `⚪ Umum (${items.filter(i => (i.rarity || '').toUpperCase() === 'UMUM' || (i.rarity || '').toUpperCase() === 'STANDARD').length})` },
            { id: 'TIDAK_UMUM', label: `🟢 Tidak Umum (${items.filter(i => (i.rarity || '').toUpperCase() === 'TIDAK_UMUM').length})` },
            { id: 'LANGKA', label: `🔵 Langka (${items.filter(i => (i.rarity || '').toUpperCase() === 'LANGKA' || (i.rarity || '').toUpperCase() === 'RARE').length})` },
            { id: 'EPIK', label: `🟣 Epik (${items.filter(i => (i.rarity || '').toUpperCase() === 'EPIK').length})` },
            { id: 'LEGENDARIS', label: `🟡 Legendaris (${items.filter(i => (i.rarity || '').toUpperCase() === 'LEGENDARIS' || (i.rarity || '').toUpperCase() === 'LEGENDARY').length})` }
        ];

        if (!rarities.some(r => r.id === collectionFilterCategory)) collectionFilterCategory = 'ALL';
        rarities.forEach(r => {
            const btn = document.createElement('button');
            btn.className = 'subtab-pill' + (collectionFilterCategory === r.id ? ' active' : '');
            btn.style.cssText = 'flex-shrink:0 !important; white-space:nowrap !important; font-size:0.74rem; font-weight:700; padding:6px 12px;';
            btn.textContent = r.label;
            btn.type = 'button';
            btn.setAttribute('aria-pressed', String(collectionFilterCategory === r.id));
            btn.onclick = () => {
                collectionFilterCategory = r.id;
                renderCollectionsGallery(container, isModal);
            };
            catRow.appendChild(btn);
        });

        const statusRow = document.createElement('div');
        statusRow.className = 'filter-chips';
        statusRow.style.cssText = 'display:flex; gap:6px; margin-bottom:12px;';
        const statuses = [
            { id: 'ALL', label: 'Semua Status' },
            { id: 'OWNED', label: '✓ Dimiliki' },
            { id: 'UNOWNED', label: '○ Belum Dimiliki' }
        ];
        statuses.forEach(s => {
            const btn = document.createElement('button');
            btn.className = 'subtab-pill' + (collectionFilterOwnership === s.id ? ' active' : '');
            btn.style.cssText = 'flex:1; font-size:0.75rem; font-weight:700; padding:6px 0; text-align:center;';
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
                filtered = filtered.filter(i => {
                    const r = (i.rarity || '').toUpperCase();
                    if (collectionFilterCategory === 'UMUM') return r === 'UMUM' || r === 'STANDARD';
                    if (collectionFilterCategory === 'LANGKA') return r === 'LANGKA' || r === 'RARE';
                    if (collectionFilterCategory === 'LEGENDARIS') return r === 'LEGENDARIS' || r === 'LEGENDARY';
                    return r === collectionFilterCategory;
                });
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
                title.style.cssText = 'font-size:0.82rem; font-weight:700; color:var(--text); line-height:1.3; margin:4px 0; overflow:hidden; text-overflow:ellipsis; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; min-height:2.2em;';
                title.textContent = item.name;
                card.appendChild(title);

                const priceRow = document.createElement('div');
                priceRow.style.cssText = 'font-size:0.88rem; font-weight:700; color:var(--gold); margin:2px 0;';
                priceRow.textContent = formatMoney(item.currentValuation);
                card.appendChild(priceRow);

                const metaRow = document.createElement('small');
                metaRow.style.cssText = 'font-size:0.66rem; color:var(--text-secondary); display:block; margin-bottom:8px; line-height:1.3;';
                metaRow.textContent = `+${item.reputationBonus || 0} Reputasi · Rawat: ${item.dailyMaintenanceCost ? formatMoney(item.dailyMaintenanceCost) + '/hr' : 'Rp 0'}`;
                card.appendChild(metaRow);

                const actionsRow = document.createElement('div');
                actionsRow.className = 'collection-actions-row';
                actionsRow.style.marginTop = 'auto';

                if (isOwned) {
                    if (isPledged) {
                        const actBtn = document.createElement('button');
                        actBtn.className = 'btn-secondary';
                        actBtn.disabled = true;
                        actBtn.style.cssText = 'width:100%; min-height:36px; padding:4px 8px; font-size:0.7rem;';
                        actBtn.textContent = 'Diagunkan';
                        actionsRow.appendChild(actBtn);
                    } else {
                        const actBtn = document.createElement('button');
                        actBtn.className = 'btn-secondary btn-collection-sell';
                        actBtn.type = 'button';
                        actBtn.style.cssText = 'width:100%; min-height:36px; padding:4px 8px; font-size:0.7rem; font-weight:700;';
                        actBtn.textContent = `Jual Koleksi`;
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
                    actBtn.style.cssText = 'width:100%; min-height:36px; padding:4px 8px; font-size:0.72rem; font-weight:700;';
                    if (canAfford) {
                        actBtn.textContent = `Beli`;
                        actBtn.onclick = (e) => {
                            e.stopPropagation();
                            transactCollectionItem(item, true);
                        };
                    } else {
                        actBtn.disabled = true;
                        actBtn.textContent = 'Kas Kurang';
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
                    const grid = document.createElement('div');
                    grid.className = 'collection-grid-2col';
                    unownedItems.forEach(item => grid.appendChild(createCollectionCard(item, false)));
                    secUnowned.appendChild(grid);
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
                    const grid = document.createElement('div');
                    grid.className = 'collection-grid-2col';
                    ownedItems.forEach(item => grid.appendChild(createCollectionCard(item, true)));
                    secOwned.appendChild(grid);
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
                    const grid = document.createElement('div');
                    grid.className = 'collection-grid-2col';
                    ownedItems.forEach(item => grid.appendChild(createCollectionCard(item, true)));
                    secOwned.appendChild(grid);
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
                    empty.textContent = 'Seluruh koleksi pada kategori ini telah Anda miliki.';
                    secUnowned.appendChild(empty);
                } else {
                    const grid = document.createElement('div');
                    grid.className = 'collection-grid-2col';
                    unownedItems.forEach(item => grid.appendChild(createCollectionCard(item, false)));
                    secUnowned.appendChild(grid);
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
            flowText(container, 'Kelola Dana', 'Data dana kelolaan belum tersedia.');
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

                // 2. Launcher Terpisah: Fitur Pengelolaan & Alokasi Dana (Sesuai Comment 4)
                const launcherCard = document.createElement('section');
                launcherCard.className = 'card firm-launchers-section';
                launcherCard.style.cssText = 'background:var(--surface); border:1px solid var(--border); border-radius:14px; padding:16px; margin-top:16px;';
                launcherCard.innerHTML = `<h2 class="card-title" style="margin-bottom:12px; font-size:1.05rem;">Fitur Pengelolaan & Alokasi Dana</h2>`;
                
                const launchersList = document.createElement('div');
                launchersList.style.cssText = 'display:flex; flex-direction:column; gap:10px;';

                const launcherItems = [
                    {
                        title: 'Fund Builder & Alokasi Portofolio',
                        sub: 'Portfolio Manager · Rancang reksa dana kustom',
                        icon: '📊',
                        dashed: true,
                        action: () => openFundBuilder()
                    },
                    {
                        title: 'Laporan Klien & Fee Mandat',
                        sub: 'Evaluasi return portofolio kelolaan dan komisi',
                        icon: '📑',
                        dashed: false,
                        action: () => openClientReports()
                    },
                    {
                        title: 'Bursa Headhunting Staf Analis Elit',
                        sub: 'Rekrut manajer investasi & analis bintang',
                        icon: '👔',
                        dashed: false,
                        action: () => openHeadhuntingModal()
                    }
                ];

                launcherItems.forEach(item => {
                    const row = document.createElement('div');
                    row.className = 'firm-launcher-row';
                    row.tabIndex = 0;
                    row.style.cssText = `display:flex; justify-content:space-between; align-items:center; padding:12px 14px; background:rgba(0,0,0,0.25); border:${item.dashed ? '1px dashed var(--gold)' : '1px solid var(--border)'}; border-radius:10px; cursor:pointer; min-height:48px; transition:border-color 150ms ease;`;
                    row.innerHTML = `
                        <div style="display:flex; align-items:center; gap:12px;">
                            <span style="font-size:1.25rem;">${item.icon}</span>
                            <div>
                                <strong style="font-size:0.9rem; color:#FFF; display:block;">${item.title}</strong>
                                <span style="font-size:0.75rem; color:var(--text-muted);">${item.sub}</span>
                            </div>
                        </div>
                        <span style="color:var(--text-muted); font-size:1.1rem; font-weight:700;">›</span>
                    `;
                    row.onclick = item.action;
                    row.onkeydown = (e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); item.action(); } };
                    launchersList.append(row);
                });
                launcherCard.append(launchersList);
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

        const companyId = generateSafeId('COMP');
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
        const request = {companyId:company.id, action, employeeId, role, requestId:generateSafeId('STAFF')};
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
        flowText(panel, 'Kebijakan Kerja', 'Atur jam kerja dan tingkat fasilitas staf untuk efisiensi.');

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
        } catch(error) { const panel=showFlow('Transaksi Usaha Belum Diproses');flowText(panel,'Alasan',error.message); }
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
        const panel = showFlow('Terminal Riset Pasar');

        // Header Status Paket & Akses Satu Pintu (Plan 13 K01)
        let activeTier = null;
        let tierLoadError = null;
        if (!bridge || !bridge.getResearchFeed) {
            tierLoadError = 'Layanan terminal riset belum tersedia';
        } else {
            try {
                const rf = JSON.parse(bridge.getResearchFeed());
                if (rf && rf.tier) {
                    activeTier = rf.tier;
                } else {
                    tierLoadError = 'Format data paket tidak dikenali';
                }
            } catch (e) {
                tierLoadError = 'Status paket gagal dimuat: ' + (e.message || 'Kesalahan koneksi');
            }
        }

        const tierStatusCard = node('section', 'card');
        tierStatusCard.style.cssText = 'display:flex; justify-content:space-between; align-items:center; margin-bottom:14px; background:linear-gradient(135deg,#17243B 0%,#0F172A 100%); border:1px solid rgba(212, 175, 55, 0.4);';
        const tierInfo = node('div');
        if (activeTier) {
            tierInfo.innerHTML = `<small style="color:var(--text-muted); font-weight:700;">STATUS TERMINAL RISET</small><br><strong style="color:#FFD700; font-size:1.1rem;">Paket ${activeTier}</strong>`;
        } else {
            tierInfo.innerHTML = `<small style="color:var(--text-muted); font-weight:700;">STATUS TERMINAL RISET</small><br><strong style="color:var(--negative); font-size:0.95rem;">Status Tidak Dapat Dimuat</strong><br><small style="color:var(--text-muted); font-size:0.75rem;">${tierLoadError}</small>`;
        }
        const manageBtn = flowButton(tierStatusCard, 'Kelola Paket Langganan', openResearchTiersModal);
        manageBtn.style.cssText = 'min-height:44px; padding:8px 14px; font-size:0.85rem;';
        tierStatusCard.append(tierInfo);
        panel.append(tierStatusCard);

        const label = node('label', '', 'Layanan Intelijen'); label.htmlFor = 'terminal-kind';
        const kind = node('select', 'input-field'); kind.id = 'terminal-kind';
        [['inbox','Kotak masuk analis'],['consensus','Konsensus analis · Pro'],['comparison','Bandingkan instrumen · Pro'],['alternative','Data alternatif · Enterprise'],['npc_trades','Radar transaksi besar NPC · Pro']].forEach(([value,title]) => {
            const option = node('option','',title); option.value=value; kind.append(option);
        });
        panel.append(label,kind);
        const selection = node('section'); panel.append(selection);
        let selected;
        const changeKind = () => {
            selection.replaceChildren(); selected = null;
            if (kind.value !== 'inbox' && kind.value !== 'npc_trades') selected = instrumentSelect(selection, kind.value === 'comparison' ? 'Instrumen pembanding' : 'Instrumen', 'terminal-instruments', kind.value === 'comparison');
        };
        kind.onchange=changeKind; changeKind();
        const status=node('p'); status.setAttribute('role','status');
        const results=node('section');
        flowButton(panel,'Tampilkan data intelijen',()=>{
            status.textContent='Memuat…'; results.replaceChildren();
            try {
                if (kind.value === 'npc_trades') {
                    if (!bridge.getLargeNpcTransactions) throw new Error('Fitur transaksi besar NPC belum tersedia.');
                    const res = JSON.parse(bridge.getLargeNpcTransactions());
                    if (!res.success) throw new Error(res.message);
                    status.textContent = '';
                    const listCard = node('section', 'card');
                    listCard.append(node('h3', 'card-title', 'Radar Transaksi Besar Konglomerat NPC (≥ Rp 500 Juta)'));
                    const txs = res.transactions || [];
                    if (!txs.length) {
                        emptyState(listCard, 'Belum Ada Transaksi Besar', 'Belum ada transaksi konglomerat di atas Rp 500 juta pada periode saat ini.');
                    } else {
                        txs.forEach(t => {
                            const r = node('div', 'data-row');
                            r.style.cssText = 'display:flex; justify-content:space-between; align-items:center; padding:10px 0; border-bottom:1px solid var(--border);';
                            r.innerHTML = `<div><strong>${t.accountName || 'Konglomerat NPC'}</strong><br><small style="color:var(--text-muted);">${t.description}</small></div><strong style="color:var(--accent);">${t.amount}</strong>`;
                            listCard.append(r);
                        });
                    }
                    results.append(listCard);
                    return;
                }
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

                if (t.isCurrent && t.level > 1 && t.autoRenewal && bridge.cancelTerminalAutoRenewal) {
                    flowButton(card, 'Hentikan perpanjangan otomatis', () => {
                        const expiry = new Date(t.expiresAt).toLocaleString('id-ID', {timeZone:'Asia/Jakarta'});
                        showConfirmModal('Batalkan Perpanjangan Otomatis', `Hentikan perpanjangan otomatis? Akses tetap aktif sampai ${expiry} waktu game.`, () => {
                            try {
                                const result = JSON.parse(bridge.cancelTerminalAutoRenewal());
                                if (!result.success) throw new Error(result.message || 'Perpanjangan otomatis belum dihentikan. Silakan coba lagi.');
                                showGameToast('info', 'Perpanjangan Dibatalkan', 'Langganan otomatis terminal riset dinonaktifkan.');
                                openResearchTiersModal();
                            } catch (error) {
                                showGameToast('warning', 'Pembatalan gagal', error.message || 'Perpanjangan otomatis belum dihentikan. Silakan coba lagi.');
                            }
                        });
                    });
                }

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
        if (eyebrow) header.append(node('span', 'eyebrow', eyebrow));
        header.append(node('h2', 'card-title', title));
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

    // =========================================================================
    // SINTESA TAB KE-5: PUSAT KEUANGAN, MAKRO & KALENDER STOCKBIT / BEI
    // =========================================================================

    let currentKeuanganSubtab = 'makro';
    let currentKalenderFilter = 'ALL';
    let currentKalenderDayOffset = 0;

    function loadKeuanganView(activeSubtab) {
        if (['makro', 'radar', 'bank', 'kalender'].includes(activeSubtab)) currentKeuanganSubtab = activeSubtab;
        refreshSnapshot();
        const view = views.keuangan;
        if (!view) return;

        let subnav = view.querySelector('.keuangan-subnav');
        let content = document.getElementById('keuangan-subtab-content');
        if (!subnav || !content) {
            view.replaceChildren();
            const heading = node('div', 'page-heading');
            heading.innerHTML = '<div><h1 style="font-size:1.25rem; font-weight:700; color:#FFF; margin:0 0 10px 0;">Pusat Keuangan</h1></div>';
            subnav = node('div', 'keuangan-subnav');
            subnav.innerHTML = `
                <button class="subtab-pill" id="keuangan-subtab-makro" data-keuangan-subtab="makro">🌐 Iklim & Warta</button>
                <button class="subtab-pill" id="keuangan-subtab-radar" data-keuangan-subtab="radar">🎯 Radar Saham</button>
                <button class="subtab-pill" id="keuangan-subtab-bank" data-keuangan-subtab="bank">🏦 Deposito Bank</button>
                <button class="subtab-pill" id="keuangan-subtab-kalender" data-keuangan-subtab="kalender">📅 Kalender</button>
            `;
            content = node('div');
            content.id = 'keuangan-subtab-content';
            view.append(heading, subnav, content);
        }

        const btnMakro = document.getElementById('keuangan-subtab-makro');
        const btnRadar = document.getElementById('keuangan-subtab-radar');
        const btnBank = document.getElementById('keuangan-subtab-bank');
        const btnKalender = document.getElementById('keuangan-subtab-kalender');

        if (btnMakro) btnMakro.onclick = () => loadKeuanganView('makro');
        if (btnRadar) btnRadar.onclick = () => loadKeuanganView('radar');
        if (btnBank) btnBank.onclick = () => loadKeuanganView('bank');
        if (btnKalender) btnKalender.onclick = () => loadKeuanganView('kalender');

        subnav.querySelectorAll('.subtab-pill').forEach(btn => {
            const isTarget = btn.getAttribute('data-keuangan-subtab') === currentKeuanganSubtab;
            btn.classList.toggle('active', isTarget);
            btn.setAttribute('aria-pressed', String(isTarget));
        });

        content.replaceChildren();

        if (currentKeuanganSubtab === 'makro') renderKeuanganMakro(content);
        else if (currentKeuanganSubtab === 'radar') renderKeuanganRadar(content);
        else if (currentKeuanganSubtab === 'bank') renderKeuanganBank(content);
        else if (currentKeuanganSubtab === 'kalender') renderKeuanganKalender(content);
    }

    let keuanganNewsFilter = 'all';

    function renderKeuanganMakro(parent) {
        let macro = { macroIndex: 5, macroLabel: 'Ekonomi Normal', depositRateAnnual: 6.75, loanRateAnnual: 10.75, sectorPieMultiplier: 1.0, ipoGainMultiplier: 1.1 };
        try {
            if (bridge.getMacroAndSynthesisState) {
                macro = JSON.parse(bridge.getMacroAndSynthesisState());
            }
        } catch (e) { console.warn('Gagal membaca state makro:', e); }

        const idx = macro.macroIndex || 5;

        // 1. Card Speedometer Makro
        const cardSpeedo = node('section', 'card');
        const topRow = node('div', 'flex-row');
        topRow.style.cssText = 'display:flex; justify-content:space-between; align-items:center; margin-bottom:4px;';
        topRow.append(node('span', 'eyebrow', 'IKLIM MAKROEKONOMI'), node('span', `m-tag ${idx >= 6 ? 'pos' : idx <= 3 ? 'neg' : 'neutral'}`, `Level ${idx}/9`));
        
        const titleBig = node('div', 'metric-hero', macro.macroLabel || `Level ${idx}`);
        titleBig.style.cssText = 'font-size:1.45rem; font-weight:700; color:var(--gold); margin:4px 0;';
        
        const subDesc = node('p', 'caption');
        subDesc.style.cssText = 'font-size:0.78rem; color:var(--text-secondary); line-height:1.45; margin-bottom:10px;';
        subDesc.textContent = idx >= 7 ? 'Pasar sedang booming ekstrem. Minat investor tinggi, gairah IPO membara, dan omzet sektor riil melesat.'
            : idx >= 5 ? 'Kondisi ekonomi bergairah dan stabil. Suku bunga perbankan melandai dan iklim usaha positif.'
            : 'Ekonomi dalam tekanan resesi. Suku bunga deposito tinggi untuk pengamanan kas modal.';

        const strip = node('div', 'macro-levels-strip');
        const colors = ['#FF5252','#FF7A52','#F59E0B','#D9B26A','#E3BD7F','#9BE3B0','#5FEAA8','#33EE9E','#14F195'];
        for (let i = 1; i <= 9; i++) {
            const b = node('i', i === idx ? 'cur' : '', String(i));
            b.style.background = colors[i - 1];
            if (i !== idx) b.style.opacity = '0.45';
            strip.append(b);
        }

        const scaleLabel = node('div', 'macro-levels-scale');
        scaleLabel.innerHTML = '<span>Resesi</span><span>Normal</span><span>Booming</span>';

        cardSpeedo.append(topRow, titleBig, subDesc, strip, scaleLabel);
        parent.append(cardSpeedo);

        // 2. Card Dampak Terhadap Keuangan Pemain
        const cardImpact = node('section', 'card');
        cardImpact.append(node('span', 'eyebrow', 'DAMPAK TERHADAP KEUANGANMU'));

        const grid = node('div');
        grid.style.cssText = 'display:grid; grid-template-columns:1fr 1fr; gap:8px; margin-top:8px;';

        const mDep = node('div'); mDep.style.cssText = 'background:var(--surface-raised); border-radius:10px; padding:8px 10px;';
        mDep.innerHTML = `<small style="display:block; font-size:0.65rem; color:var(--text-secondary);">Bunga Acuan Deposito</small><strong style="display:block; font-size:1.05rem; color:var(--gold); margin-top:2px;">${Number(macro.depositRateAnnual || 6.75).toFixed(2)}%</strong><small style="font-size:0.65rem; color:${idx < 5 ? 'var(--positive)' : 'var(--text-muted)'};">${idx < 5 ? '▲ Bunga Tinggi' : '▼ Bunga Melandai'}</small>`;

        const mLoan = node('div'); mLoan.style.cssText = 'background:var(--surface-raised); border-radius:10px; padding:8px 10px;';
        mLoan.innerHTML = `<small style="display:block; font-size:0.65rem; color:var(--text-secondary);">Kredit Modal Bank</small><strong style="display:block; font-size:1.05rem; color:var(--gold); margin-top:2px;">${Number(macro.loanRateAnnual || 10.75).toFixed(2)}%</strong><small style="font-size:0.65rem; color:${idx >= 5 ? 'var(--positive)' : 'var(--negative)'};">${idx >= 5 ? '▼ Lebih Ringan' : '▲ Biaya Modal Naik'}</small>`;

        const mBiz = node('div'); mBiz.style.cssText = 'background:var(--surface-raised); border-radius:10px; padding:8px 10px;';
        const bizBonus = ((macro.sectorPieMultiplier || 1.0) - 1.0) * 100;
        mBiz.innerHTML = `<small style="display:block; font-size:0.65rem; color:var(--text-secondary);">Omzet Usaha Saya</small><strong style="display:block; font-size:1.05rem; color:var(--gold); margin-top:2px;">${Number(macro.sectorPieMultiplier || 1.0).toFixed(2)}×</strong><small style="font-size:0.65rem; color:${bizBonus >= 0 ? 'var(--positive)' : 'var(--negative)'};">${bizBonus >= 0 ? `▲ +${bizBonus.toFixed(1)}%` : `▼ ${bizBonus.toFixed(1)}%`}</small>`;

        const mIpo = node('div'); mIpo.style.cssText = 'background:var(--surface-raised); border-radius:10px; padding:8px 10px;';
        mIpo.innerHTML = `<small style="display:block; font-size:0.65rem; color:var(--text-secondary);">Gairah Listing IPO</small><strong style="display:block; font-size:1.05rem; color:var(--gold); margin-top:2px;">${Number(macro.ipoGainMultiplier || 1.1).toFixed(2)}×</strong><small style="font-size:0.65rem; color:var(--positive);">▲ Minat Pasar Kuat</small>`;

        grid.append(mDep, mLoan, mBiz, mIpo);
        cardImpact.append(grid);
        parent.append(cardImpact);

        // 3. PUSAT KONSOLIDASI SENTIMEN & BERITA PASAR (Eksklusif di Tab Keuangan)
        const newsSection = node('section', 'card');
        newsSection.style.borderColor = 'rgba(20,241,149,0.35)';

        const newsHeader = node('div', 'section-heading');
        newsHeader.style.marginBottom = '8px';
        newsHeader.innerHTML = `
            <div>
                <span class="eyebrow" style="color:var(--teal);">PUSAT SENTIMEN & BERITA BURSA</span>
                <h3 style="font-size:1.05rem; color:#FFF; margin-top:2px;">Warta Pasar Hari Ini</h3>
            </div>
        `;
        newsSection.append(newsHeader);

        const filterBar = node('div', 'news-filters');
        filterBar.style.cssText = 'display:flex; gap:6px; margin-bottom:10px; overflow-x:auto;';
        const fAll = node('button', `subtab-pill ${keuanganNewsFilter === 'all' ? 'active' : ''}`, 'Semua Warta');
        const fGood = node('button', `subtab-pill ${keuanganNewsFilter === 'good' ? 'active' : ''}`, '▲ Kabar Baik');
        const fBad = node('button', `subtab-pill ${keuanganNewsFilter === 'bad' ? 'active' : ''}`, '▼ Kabar Buruk');
        filterBar.append(fAll, fGood, fBad);
        newsSection.append(filterBar);

        const newsContainer = node('div');

        const renderNewsInPlace = () => {
            fAll.className = 'subtab-pill' + (keuanganNewsFilter === 'all' ? ' active' : '');
            fGood.className = 'subtab-pill' + (keuanganNewsFilter === 'good' ? ' active' : '');
            fBad.className = 'subtab-pill' + (keuanganNewsFilter === 'bad' ? ' active' : '');
            newsContainer.replaceChildren();

            try {
                if (bridge.getResearchFeed) {
                    const rf = JSON.parse(bridge.getResearchFeed());
                    if (rf && Array.isArray(rf.news) && rf.news.length > 0) {
                        rememberNews(rf.news);
                        let filtered = rf.news;
                        if (keuanganNewsFilter === 'good') filtered = rf.news.filter(n => Number(n.sentimentImpact) > 0 || n.direction === 'POSITIF');
                        else if (keuanganNewsFilter === 'bad') filtered = rf.news.filter(n => Number(n.sentimentImpact) < 0 || n.direction === 'NEGATIF');

                        if (!filtered.length) {
                            newsContainer.innerHTML = '<p style="font-size:0.8rem; color:var(--text-muted); margin:8px 0;">Belum ada berita pada kategori ini.</p>';
                        } else {
                            filtered.forEach((n, nIdx) => {
                                const nBox = node('div');
                                nBox.style.cssText = `padding:10px 0; ${nIdx > 0 ? 'border-top:1px solid var(--divider);' : ''} cursor:pointer;`;
                                const isGood = n.direction === 'POSITIF' || Number(n.sentimentImpact) > 0;
                                const isBad = n.direction === 'NEGATIF' || Number(n.sentimentImpact) < 0;
                                const tagClass = isGood ? 'pos' : isBad ? 'neg' : 'neutral';
                                
                                let pubTimeStr = '12 Okt · 09:30 WIB';
                                if (n.publishedAt) {
                                    const pd = new Date(Number(n.publishedAt));
                                    pubTimeStr = pd.toLocaleDateString('id-ID', { day: 'numeric', month: 'short' }) + ' · ' + pd.toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' }) + ' WIB';
                                }

                                nBox.innerHTML = `
                                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:4px;">
                                        <span class="m-tag ${tagClass}" style="font-size:0.65rem;">${isGood ? '▲ POSITIF' : isBad ? '▼ NEGATIF' : 'NETRAL'} · ${n.affectedSector || n.categoryLabel || 'Pasar'}${n.relatedTicker ? ' (' + n.relatedTicker + ')' : ''}</span>
                                        <small style="color:var(--gold); font-size:0.68rem; font-weight:700;">🕒 ${pubTimeStr}</small>
                                    </div>
                                    <strong style="display:block; font-size:0.88rem; color:var(--text); line-height:1.35;">${n.headline || n.title}</strong>
                                    <p style="font-size:0.75rem; color:var(--text-secondary); margin-top:4px; line-height:1.4;">${n.content ? n.content.substring(0, 110) + (n.content.length > 110 ? '…' : '') : 'Rincian berita tersedia.'}</p>
                                `;
                                nBox.onclick = () => openNewsDetail(n);
                                newsContainer.append(nBox);
                            });
                        }
                    } else {
                        newsContainer.innerHTML = '<p style="font-size:0.8rem; color:var(--text-muted); margin:8px 0;">Belum ada kabar pasar yang terbit saat ini.</p>';
                    }
                }
            } catch (err) {
                newsContainer.innerHTML = `<p style="font-size:0.8rem; color:var(--negative);">Galat memuat berita: ${err.message}</p>`;
            }
        };

        fAll.onclick = () => { keuanganNewsFilter = 'all'; renderNewsInPlace(); };
        fGood.onclick = () => { keuanganNewsFilter = 'good'; renderNewsInPlace(); };
        fBad.onclick = () => { keuanganNewsFilter = 'bad'; renderNewsInPlace(); };
        renderNewsInPlace();

        newsSection.append(newsContainer);
        parent.append(newsSection);

        // 4. Evaluasi Kuartal
        const cardEval = node('section', 'card');
        cardEval.innerHTML = `
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:4px;">
                <span class="eyebrow">EVALUASI SIKLUS KUARTAL</span>
                <strong style="font-size:0.8rem; color:var(--gold);">Otomatis Bergulir</strong>
            </div>
            <p style="font-size:0.74rem; color:var(--text-secondary); line-height:1.45; margin-top:4px;">
                Evaluasi tingkat makro (Level 1..9) dievaluasi otomatis pada hari pertama setiap kuartal (1 Jan, 1 Apr, 1 Jul, 1 Okt) berdasarkan tren harga saham unggulan.
            </p>
        `;
        parent.append(cardEval);
    }

    function renderKeuanganRadar(parent) {
        let radarData = { success: true, hasProAccess: false, totalUndervalued: 0, items: [] };
        try {
            if (bridge.getFundamentalRadar) {
                radarData = JSON.parse(bridge.getFundamentalRadar());
            }
        } catch (e) { console.warn('Gagal membaca radar:', e); }

        const hasPro = radarData.hasProAccess === true;

        // 1. Banner Status Paket PRO
        const banner = node('section', 'card');
        banner.style.cssText = hasPro
            ? 'background:linear-gradient(135deg,rgba(212,175,55,0.18) 0%, rgba(21,31,58,0.7) 100%); border-color:var(--gold);'
            : 'background:linear-gradient(135deg,rgba(59,130,246,0.15) 0%, rgba(21,31,58,0.7) 100%); border-color:rgba(59,130,246,0.4);';

        if (hasPro) {
            banner.innerHTML = `
                <div style="display:flex; justify-content:space-between; align-items:center;">
                    <div>
                        <span class="eyebrow" style="color:var(--gold);">TERMINAL INTELIJEN RISET</span>
                        <strong style="display:block; font-size:0.95rem; color:#FFF; margin-top:2px;">⭐ Paket PRO Trader Aktif</strong>
                    </div>
                    <span class="m-tag pos">AKSES PENUH</span>
                </div>
                <p style="font-size:0.75rem; color:var(--text-secondary); margin-top:4px;">Konsensus target analis NPC, sinyal saham diskon murah, & deteksi barang langka aktif.</p>
            `;
            parent.append(banner);
        } else {
            // Promo Perdana Kedua Jenis Paket dengan Tabel Benefit Lengkap (Comment 1)
            const promoSection = node('section', 'card');
            promoSection.style.cssText = 'background:linear-gradient(135deg,rgba(212,175,55,0.12) 0%, rgba(21,31,58,0.9) 100%); border:1px solid var(--gold); border-radius:12px; padding:14px; margin-bottom:14px;';
            promoSection.innerHTML = `
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:4px;">
                    <span class="eyebrow" style="color:var(--gold);">🔥 PROMO PERDANA DISKON 50%</span>
                    <span class="m-tag pos" style="font-weight:700;">HEMAT 50%</span>
                </div>
                <strong style="display:block; font-size:1.05rem; color:#FFF; margin-bottom:4px;">Buka Akses Penuh Terminal Intelijen Bursa</strong>
                <p style="font-size:0.74rem; color:var(--text-secondary); line-height:1.4; margin-bottom:10px;">
                    Pilih paket intelijen untuk memantau ${radarData.totalUndervalued || 21} saham diskon murah, target konsensus analis, dan transaksi paus pasar modal.
                </p>

                <!-- Tabel Perbandingan Benefit Asli (TerminalTier) -->
                <div style="background:rgba(0,0,0,0.35); border-radius:10px; border:1px solid var(--border); overflow:hidden; margin-bottom:12px;">
                    <table style="width:100%; border-collapse:collapse; font-size:0.72rem; text-align:left;">
                        <thead>
                            <tr style="background:rgba(227,189,127,0.15); border-bottom:1px solid var(--border);">
                                <th style="padding:8px 10px; color:var(--gold);">Benefit & Fitur Riset</th>
                                <th style="padding:8px; color:var(--text); text-align:center;">PRO</th>
                                <th style="padding:8px; color:var(--gold); text-align:center;">WHALE</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr style="border-bottom:1px solid rgba(255,255,255,0.06);">
                                <td style="padding:7px 10px; color:var(--text-secondary);">Warta Awal 1 Hari Game (Estimasi 2-5%)</td>
                                <td style="padding:7px; text-align:center; color:var(--positive);">✓</td>
                                <td style="padding:7px; text-align:center; color:var(--positive);">✓</td>
                            </tr>
                            <tr style="border-bottom:1px solid rgba(255,255,255,0.06);">
                                <td style="padding:7px 10px; color:var(--text-secondary);">Kedalaman Order Book Level 2</td>
                                <td style="padding:7px; text-align:center; color:var(--positive);">✓</td>
                                <td style="padding:7px; text-align:center; color:var(--positive);">✓</td>
                            </tr>
                            <tr style="border-bottom:1px solid rgba(255,255,255,0.06);">
                                <td style="padding:7px 10px; color:var(--text-secondary);">Radar Transaksi Paus / NPC (≥ Rp 500 Juta)</td>
                                <td style="padding:7px; text-align:center; color:var(--positive);">✓</td>
                                <td style="padding:7px; text-align:center; color:var(--positive);">✓</td>
                            </tr>
                            <tr style="border-bottom:1px solid rgba(255,255,255,0.06);">
                                <td style="padding:7px 10px; color:var(--text-secondary);">Sinyal Data Alternatif Eksklusif</td>
                                <td style="padding:7px; text-align:center; color:var(--text-muted);">-</td>
                                <td style="padding:7px; text-align:center; color:var(--positive);">✓</td>
                            </tr>
                            <tr>
                                <td style="padding:7px 10px; color:var(--text-secondary);">Berita Katalis Mega (Lonjakan Penutupan >20%)</td>
                                <td style="padding:7px; text-align:center; color:var(--text-muted);">-</td>
                                <td style="padding:7px; text-align:center; color:var(--positive);">✓</td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <!-- 2 Kartu Penawaran Berdampingan -->
                <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px;">
                    <div style="background:var(--surface-raised); border:1px solid var(--border); border-radius:10px; padding:10px; display:flex; flex-direction:column; justify-content:space-between;">
                        <div>
                            <span class="m-tag neutral" style="font-size:0.62rem; font-weight:700;">PRO TRADER</span>
                            <div style="margin:4px 0;">
                                <small style="color:var(--text-muted); text-decoration:line-through; font-size:0.68rem;">$500.000</small>
                                <strong style="display:block; font-size:1rem; color:var(--positive);">$250.000 <span style="font-size:0.65rem; color:var(--text-secondary);">/ 30 hr</span></strong>
                            </div>
                            <small style="font-size:0.66rem; color:var(--text-secondary); display:block; margin-bottom:8px; line-height:1.3;">Warta dini 1 hari, Order Book L2, & radar paus Rp 500jt.</small>
                        </div>
                        <button class="btn-primary" style="width:100%; min-height:36px; padding:6px 0; font-size:0.75rem; font-weight:700;" onclick="openResearchTiersModal()">Beli PRO ($250k)</button>
                    </div>

                    <div style="background:var(--surface-raised); border:1px solid var(--gold); border-radius:10px; padding:10px; display:flex; flex-direction:column; justify-content:space-between;">
                        <div>
                            <span class="m-tag pos" style="font-size:0.62rem; font-weight:700;">👑 WHALE TAIPAN</span>
                            <div style="margin:4px 0;">
                                <small style="color:var(--text-muted); text-decoration:line-through; font-size:0.68rem;">$1.000.000</small>
                                <strong style="display:block; font-size:1rem; color:var(--gold);">$500.000 <span style="font-size:0.65rem; color:var(--text-secondary);">/ 30 hr</span></strong>
                            </div>
                            <small style="font-size:0.66rem; color:var(--text-secondary); display:block; margin-bottom:8px; line-height:1.3;">Data alternatif eksklusif & katalis mega kenaikan >20%.</small>
                        </div>
                        <button class="btn-primary" style="width:100%; min-height:36px; padding:6px 0; font-size:0.75rem; font-weight:700; background:linear-gradient(135deg, #E3BD7F 0%, #D4AF37 100%); color:#151515;" onclick="openResearchTiersModal()">Beli WHALE ($500k)</button>
                    </div>
                </div>
            `;
            parent.append(promoSection);
        }

        // 2. List Saham Diskon
        const listCard = node('section', 'card');
        listCard.append(node('span', 'eyebrow', 'RADAR SAHAM DISKON MURAH'));

        const items = radarData.items || [];
        if (items.length === 0) {
            emptyState(listCard, 'Belum Ada Sinyal Diskon', 'Saat ini harga seluruh saham berada di atas nilai wajarnya.');
        } else {
            items.forEach((item, idx) => {
                const sRow = node('div');
                sRow.style.cssText = `display:flex; align-items:center; gap:10px; padding:10px 0; ${idx > 0 ? 'border-top:1px solid var(--divider);' : ''} cursor:pointer;`;
                
                const tk = node('div');
                tk.style.cssText = 'width:42px; height:42px; border-radius:10px; background:var(--surface-raised); display:grid; place-items:center; font-weight:700; font-size:0.74rem; color:var(--gold); border:1px solid var(--border); flex-shrink:0;';
                tk.textContent = item.ticker;

                const mid = node('div');
                mid.style.cssText = 'flex:1; min-width:0;';
                mid.innerHTML = `
                    <strong style="display:block; font-size:0.86rem; color:var(--text); white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${item.name}</strong>
                    <small style="display:block; font-size:0.7rem; color:var(--text-muted);">${item.currentPrice} → Wajar: Rp ${Number(item.intrinsicValue).toLocaleString('id-ID')}</small>
                `;

                const right = node('div');
                right.style.cssText = 'text-align:right; flex-shrink:0;';
                const sigClass = item.signal === 'STRONG_BUY' ? 'pos' : item.signal === 'BUY' ? 'pos' : 'neutral';
                const gapNum = Number(item.gapRatePercent);
                const gapSign = gapNum > 0 ? '+' : '';
                right.innerHTML = `
                    <strong style="display:block; font-size:0.9rem; color:${gapNum >= 0 ? 'var(--positive)' : 'var(--text-secondary)'};">${gapSign}${item.gapRatePercent}%</strong>
                    <span class="m-tag ${sigClass}" style="font-size:0.62rem;">${item.signal || 'BUY'}</span>
                `;

                sRow.append(tk, mid, right);
                sRow.onclick = () => navigateToStockMarketDetail(item.id, item.ticker, 'IDX');
                listCard.append(sRow);

                if (item.lockedFloatRatio >= 0.3) {
                    const squeezeNote = node('div');
                    squeezeNote.style.cssText = 'background:rgba(245,158,11,0.12); border:1px solid rgba(245,158,11,0.3); border-radius:6px; padding:4px 8px; font-size:0.68rem; color:var(--warning); margin-bottom:8px;';
                    squeezeNote.innerHTML = `⚠️ <b>Barang Langka:</b> ${(item.lockedFloatRatio * 100).toFixed(0)}% saham beredar telah diborong habis. Harga jual ditarik naik oleh bursa.`;
                    listCard.append(squeezeNote);
                }
            });
        }
        parent.append(listCard);
    }

    function renderKeuanganBank(parent) {
        let activeDeps = [];
        let depProducts = [];
        try {
            if (bridge.getActiveTimeDeposits) activeDeps = JSON.parse(bridge.getActiveTimeDeposits());
            if (bridge.getTimeDepositProducts) depProducts = JSON.parse(bridge.getTimeDepositProducts());
        } catch (e) { console.warn('Gagal membaca data deposito:', e); }

        // 1. Ringkasan Kas Terkunci Deposito
        let totalPrincipal = 0;
        let totalAccrued = 0;
        activeDeps.forEach(d => {
            totalPrincipal += Number(d.principalRaw || 0);
            totalAccrued += Number(d.accruedInterestRaw || 0);
        });

        const heroCard = node('section', 'card');
        heroCard.append(node('span', 'eyebrow', 'DANA TERKUNCI DI PERBANKAN'));
        const hBig = node('div', 'metric-hero', activeDeps.length > 0 ? `Rp ${totalPrincipal.toLocaleString('id-ID')}` : 'Rp 0');
        hBig.style.cssText = 'font-size:1.5rem; font-weight:700; color:var(--gold); margin:4px 0;';
        const hSub = node('p', 'caption');
        hSub.style.cssText = 'font-size:0.75rem; color:var(--text-secondary);';
        hSub.innerHTML = `${activeDeps.length} Deposito Aktif · Bunga Berjalan <b style="color:var(--positive);">+Rp ${totalAccrued.toLocaleString('id-ID')}</b>`;
        heroCard.append(hBig, hSub);
        parent.append(heroCard);

        // 2. Deposito Aktif
        if (activeDeps.length > 0) {
            const actCard = node('section', 'card');
            actCard.append(node('span', 'eyebrow', `DEPOSITO AKTIF (${activeDeps.length})`));
            activeDeps.forEach((d, idx) => {
                const dBox = node('div');
                dBox.style.cssText = `padding:10px 0; ${idx > 0 ? 'border-top:1px solid var(--divider);' : ''}`;
                dBox.innerHTML = `
                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <strong style="font-size:0.86rem; color:var(--text);">${d.bankName}</strong>
                        <span class="m-tag ${d.isMatured ? 'pos' : 'neutral'}">${d.annualInterestRate} Kontrak</span>
                    </div>
                    <div style="font-size:0.72rem; color:var(--text-secondary); margin-top:2px;">
                        Pokok: ${d.principal} · Sisa: ${d.tenorSessions || d.tenorDays} Hari Bursa (${d.isMatured ? 'Sudah Jatuh Tempo' : 'Sedang Berjalan'})
                    </div>
                `;
                const btnW = node('button', d.isMatured ? 'btn-primary' : 'btn-secondary', d.isMatured ? 'Cairkan Pokok & Bunga Penuh' : 'Tarik Dini (Bunga Hangus)');
                btnW.style.cssText = 'width:100%; min-height:40px; margin-top:8px; font-size:0.78rem; font-weight:700;';
                btnW.onclick = () => {
                    const proceedWithdrawal = () => {
                        try {
                            const res = JSON.parse(bridge.withdrawTimeDeposit(JSON.stringify({ depositId: d.id })));
                            if (res.success) {
                                showGameToast('success', 'Pencairan Berhasil', 'Pokok deposito berhasil diproses ke kas IDR Anda.');
                                refreshSnapshot();
                                loadKeuanganView('bank');
                            } else showGameToast('error', 'Pencairan Gagal', res.message || 'Pencairan ditolak bank.');
                        } catch (err) { showGameToast('error', 'Galat Pencairan', err.message); }
                    };
                    if (!d.isMatured) {
                        showConfirmModal('Tarik Deposito Lebih Awal', 'Jika dicairkan sebelum jatuh tempo, seluruh bunga akrual akan hangus. Tetap cairkan?', proceedWithdrawal);
                    } else {
                        proceedWithdrawal();
                    }
                };
                dBox.append(btnW);
                actCard.append(dBox);
            });
            parent.append(actCard);
        }

        // 3. Buka Deposito Baru
        const newCard = node('section', 'card');
        newCard.append(node('span', 'eyebrow', 'BUKA DEPOSITO BANK BARU'));
        const subNote = node('p', 'caption');
        subNote.style.cssText = 'font-size:0.72rem; color:var(--text-secondary); margin:4px 0 10px 0;';
        subNote.textContent = 'Pilih tenor hari bursa. Suku bunga otomatis menyesuaikan iklim makroekonomi saat penempatan.';
        newCard.append(subNote);

        depProducts.forEach(p => {
            const pBox = node('div');
            pBox.style.cssText = 'background:var(--surface-raised); border-radius:10px; padding:10px 12px; margin-bottom:8px; border:1px solid var(--border);';
            pBox.innerHTML = `
                <div style="display:flex; justify-content:space-between; align-items:center;">
                    <strong style="font-size:0.86rem; color:var(--text);">${p.bankName}</strong>
                    <span class="m-tag pos">${p.annualInterestRate} p.a.</span>
                </div>
                <div style="font-size:0.72rem; color:var(--text-secondary); margin:3px 0 8px 0;">
                    Tenor: <b>${p.tenorSessions || p.tenorDays} Hari Bursa</b> · Minimal: <b>${p.minPlacement}</b>
                </div>
            `;
            const btnOpen = node('button', 'btn-primary', `Tempatkan di ${p.bankName} →`);
            btnOpen.style.cssText = 'width:100%; min-height:40px; font-size:0.78rem; font-weight:700;';
            btnOpen.onclick = () => {
                showPromptModal('Buka Deposito Bank', `Masukkan nominal penempatan deposito di ${p.bankName} (Minimal ${p.minPlacement}):`, '10000000', (nominal) => {
                    try {
                        const res = JSON.parse(bridge.openTimeDeposit(JSON.stringify({ productId: p.id, amount: String(nominal) })));
                        if (res.success) {
                            showGameToast('success', 'Deposito Berhasil Dibuka', `Penempatan ${p.bankName} sebesar ${formatMoney(nominal)} aktif.`);
                            refreshSnapshot();
                            loadKeuanganView('bank');
                        } else showGameToast('error', 'Gagal Membuka Deposito', res.message || 'Penempatan ditolak bank.');
                    } catch (err) { showGameToast('error', 'Galat Penempatan', err.message); }
                });
            };
            pBox.append(btnOpen);
            newCard.append(pBox);
        });
        parent.append(newCard);

        // 4. Fasilitas Pinjaman & Asuransi Portofolio
        const extraCard = node('section', 'card');
        extraCard.append(node('span', 'eyebrow', 'FASILITAS KREDIT & PROTEKSI'));
        
        const loanRow = node('div');
        loanRow.style.cssText = 'display:flex; justify-content:space-between; align-items:center; padding:10px 0;';
        loanRow.innerHTML = `<div><strong style="font-size:0.84rem; color:var(--text);">Pinjaman Modal Kerja Bank</strong><br><small style="font-size:0.7rem; color:var(--text-secondary);">Tenor 7, 14, 30 hari untuk daya ungkit trading</small></div>`;
        const btnLoan = node('button', 'btn-secondary', 'Kelola Pinjaman');
        btnLoan.style.cssText = 'min-height:36px; padding:6px 12px; font-size:0.74rem; font-weight:700;';
        btnLoan.onclick = () => openLoansModal();
        loanRow.append(btnLoan);

        const insRow = node('div');
        insRow.style.cssText = 'display:flex; justify-content:space-between; align-items:center; padding:10px 0; border-top:1px solid var(--divider);';
        insRow.innerHTML = `<div><strong style="font-size:0.84rem; color:var(--text);">Asuransi Portofolio (Anti-Crash)</strong><br><small style="font-size:0.7rem; color:var(--text-secondary);">Ganti rugi 80% saat pasar bursa anjlok keras (> 5%)</small></div>`;
        const btnIns = node('button', 'btn-secondary', 'Beli Polis');
        btnIns.style.cssText = 'min-height:36px; padding:6px 12px; font-size:0.74rem; font-weight:700;';
        btnIns.onclick = () => openPortfolioInsuranceModal();
        insRow.append(btnIns);

        extraCard.append(loanRow, insRow);
        parent.append(extraCard);
    }

    let currentKalenderViewMode = 'BY_DAY';

    function renderKeuanganKalender(parent) {
        // 0. Toggle Mode Tampilan Sesuai Catatan Pemilik (Comment 2: Pilih Hari vs Urutkan Semua Hari)
        const viewModeBar = node('div', 'subtab-pills-bar');
        viewModeBar.style.cssText = 'display:flex; gap:6px; background:rgba(0,0,0,0.4); padding:4px; border-radius:12px; border:1px solid var(--border); margin-bottom:12px;';
        
        const btnModeDay = node('button', `subtab-pill ${currentKalenderViewMode === 'BY_DAY' ? 'active' : ''}`, '📅 Pilih Hari');
        btnModeDay.style.cssText = 'flex:1; padding:7px 0; font-size:0.78rem; font-weight:700; text-align:center; border-radius:9px;';
        btnModeDay.onclick = () => { currentKalenderViewMode = 'BY_DAY'; loadKeuanganView('kalender'); };

        const btnModeAll = node('button', `subtab-pill ${currentKalenderViewMode === 'ALL_DAYS' ? 'active' : ''}`, '📋 Urutkan Semua Hari');
        btnModeAll.style.cssText = 'flex:1; padding:7px 0; font-size:0.78rem; font-weight:700; text-align:center; border-radius:9px;';
        btnModeAll.onclick = () => { currentKalenderViewMode = 'ALL_DAYS'; loadKeuanganView('kalender'); };

        viewModeBar.append(btnModeDay, btnModeAll);
        parent.append(viewModeBar);

        // 1. Ribbon Tanggal Geser Horizontal (Hanya ditampilkan pada mode Per Hari)
        if (currentKalenderViewMode === 'BY_DAY') {
            const ribbon = node('div', 'calendar-ribbon');
            const currentSimMs = Number(latestSnapshot?.clock?.timestamp) || anchorGameTimestamp || Date.now();
            const offsets = [-1, 0, 1, 2, 3, 4];
            const days = offsets.map(offset => {
                const targetDate = new Date(currentSimMs + offset * 86400000);
                const dayNum = targetDate.toLocaleDateString('id-ID', { timeZone: 'Asia/Jakarta', day: 'numeric' });
                const weekday = targetDate.toLocaleDateString('id-ID', { timeZone: 'Asia/Jakarta', weekday: 'short' }).toUpperCase();
                let label = weekday;
                if (offset === -1) label = 'KEMARIN';
                else if (offset === 0) label = 'HARI INI';
                else if (offset === 1) label = 'BESOK';
                return {
                    label: label,
                    day: dayNum,
                    active: currentKalenderDayOffset === offset,
                    offset: offset,
                    hasDot: offset >= 0 && offset <= 2
                };
            });

            days.forEach(d => {
                const dCard = node('div', `day-card ${d.active ? 'active' : ''}`);
                dCard.innerHTML = `<small style="${d.offset === 0 ? 'color:var(--positive);' : ''}">${d.label}</small><strong>${d.day}</strong>${d.hasDot ? '<span class="day-dot"></span>' : ''}`;
                dCard.onclick = () => {
                    currentKalenderDayOffset = d.offset;
                    loadKeuanganView('kalender');
                };
                ribbon.append(dCard);
            });
            parent.append(ribbon);
        }

        // 2. Kategori Filter Chips (Singkat, Padat, Nyaman & Muat 1 Baris Penuh - Comment 3)
        const filters = node('div', 'cal-filters');
        const cats = [
            { id: 'ALL', label: 'Semua 5' },
            { id: 'IPO', label: '🚀 IPO' },
            { id: 'DIV', label: '💰 Div' },
            { id: 'DEP', label: '🏦 Bank' },
            { id: 'EARN', label: '📊 Lap' },
            { id: 'MACRO', label: '🏛️ Makro' }
        ];

        cats.forEach(c => {
            const chip = node('button', `cal-chip ${currentKalenderFilter === c.id ? 'active' : ''}`, c.label);
            chip.onclick = () => {
                currentKalenderFilter = c.id;
                loadKeuanganView('kalender');
            };
            filters.append(chip);
        });
        parent.append(filters);

        // 3. Header Hari Aktif
        const headerTitle = node('div', 'card');
        headerTitle.style.padding = '8px 12px';
        if (currentKalenderViewMode === 'ALL_DAYS') {
            headerTitle.innerHTML = `<span class="eyebrow" style="color:var(--gold);">DAFTAR SELURUH AGENDA (URUT SESUAI HARI)</span>`;
        } else {
            const currentSimMs = Number(latestSnapshot?.clock?.timestamp) || anchorGameTimestamp || Date.now();
            const activeDate = new Date(currentSimMs + currentKalenderDayOffset * 86400000);
            const dateStr = activeDate.toLocaleDateString('id-ID', { timeZone: 'Asia/Jakarta', day: 'numeric', month: 'short', year: 'numeric' }).toUpperCase();
            headerTitle.innerHTML = `<span class="eyebrow" style="color:var(--gold);">AGENDA · ${currentKalenderDayOffset === 0 ? 'HARI INI (' + dateStr + ')' : 'TANGGAL ' + dateStr}</span>`;
        }
        parent.append(headerTitle);

        // 4. Daftar Agenda Aksi Korporasi & Pasar
        // IPO Offering
        if (currentKalenderFilter === 'ALL' || currentKalenderFilter === 'IPO') {
            try {
                if (bridge.getIpoOfferings) {
                    const ipoRes = JSON.parse(bridge.getIpoOfferings());
                    if (ipoRes && ipoRes.offerings && ipoRes.offerings.length > 0) {
                        ipoRes.offerings.forEach(ipo => {
                            const c = node('div', 'agenda-card ipo');
                            c.innerHTML = `
                                <div class="agenda-icon" style="background:rgba(20,241,149,0.15); color:var(--positive);">🚀</div>
                                <div class="agenda-info">
                                    <b>IPO ${ipo.companyName} (${ipo.ticker})</b>
                                    <small>Harga: ${ipo.offeringPrice} · Status: ${ipo.status || 'Penawaran'}</small>
                                    <small style="color:var(--positive);">Alokasi Terbuka · Sisa Listing Terjadwal</small>
                                </div>
                            `;
                            const act = node('div', 'agenda-action');
                            const btnPesan = node('button', 'btn-mini', 'Pesan');
                            btnPesan.onclick = () => openIpoCenter();
                            act.append(btnPesan);
                            c.append(act);
                            parent.append(c);
                        });
                    }
                }
            } catch (e) { console.warn('Agenda IPO:', e); }
        }

        // Agenda Event Riset & Makro
        try {
            if (bridge.getResearchFeed) {
                const rf = JSON.parse(bridge.getResearchFeed());
                if (rf && rf.calendar && rf.calendar.length > 0) {
                    rf.calendar.forEach(ev => {
                        const isEarn = (ev.category || '').includes('EARNING');
                        const isDiv = (ev.category || '').includes('CORPORATE') || (ev.category || '').includes('DIVIDEND');
                        const isMacro = (ev.category || '').includes('MACRO');

                        if (currentKalenderFilter === 'DIV' && !isDiv) return;
                        if (currentKalenderFilter === 'EARN' && !isEarn) return;
                        if (currentKalenderFilter === 'MACRO' && !isMacro) return;

                        const c = node('div', `agenda-card ${isDiv ? 'dividend' : isMacro ? 'macro' : ''}`);
                        const icon = isDiv ? '💰' : isMacro ? '🏛️' : '📊';
                        const color = isDiv ? 'var(--gold)' : isMacro ? 'var(--warning)' : '#C084FC';

                        c.innerHTML = `
                            <div class="agenda-icon" style="background:rgba(227,189,127,0.12); color:${color};">${icon}</div>
                            <div class="agenda-info">
                                <b>${ev.title}</b>
                                <small>${ev.categoryLabel || ev.category || 'Aksi Pasar'} · Tanggal: ${ev.date}</small>
                                <small style="color:var(--text-secondary);">${ev.details || 'Agenda resmi dunia game.'}</small>
                            </div>
                        `;
                        const act = node('div', 'agenda-action');
                        if (ev.relatedTicker) {
                            const btnCek = node('button', 'btn-mini', 'Cek');
                            btnCek.onclick = () => switchTab('pasar');
                            act.append(btnCek);
                        } else {
                            act.innerHTML = `<span class="m-tag neutral" style="font-size:0.6rem;">AGENDA</span>`;
                        }
                        c.append(act);
                        parent.append(c);
                    });
                }
            }
        } catch (e) { console.warn('Agenda kalender riset:', e); }

        // Deposito Jatuh Tempo
        if (currentKalenderFilter === 'ALL' || currentKalenderFilter === 'DEP') {
            try {
                if (bridge.getActiveTimeDeposits) {
                    const deps = JSON.parse(bridge.getActiveTimeDeposits());
                    deps.forEach(d => {
                        const c = node('div', 'agenda-card bank');
                        c.innerHTML = `
                            <div class="agenda-icon" style="background:rgba(59,130,246,0.15); color:#60A5FA;">🏦</div>
                            <div class="agenda-info">
                                <b>Jatuh Tempo Deposito ${d.bankName}</b>
                                <small>Tenor ${d.tenorSessions || d.tenorDays} Hari Bursa · ${d.principal}</small>
                                <small style="color:#60A5FA;">Akrual Bunga Berjalan: ${d.accruedInterest}</small>
                            </div>
                        `;
                        const act = node('div', 'agenda-action');
                        const btnDep = node('button', 'btn-mini', 'Kelola');
                        btnDep.onclick = () => loadKeuanganView('bank');
                        act.append(btnDep);
                        c.append(act);
                        parent.append(c);
                    });
                }
            } catch (e) { console.warn('Agenda deposito:', e); }
        }
    }

    function loadRisetView() {
        refreshSnapshot();
        const view = views.riset;
        view.replaceChildren();

        // 0. Header Ringkas Tab Lainnya
        const headerCard = node('section', 'card', '');
        headerCard.style.padding = '14px 16px';
        headerCard.style.marginBottom = '12px';
        let pRank = 'Novice Trader';
        let pLevel = 1;
        try {
            if (bridge.getPlayerProfile) {
                const prof = JSON.parse(bridge.getPlayerProfile());
                if (prof.careerRankLabel || prof.careerRank) pRank = prof.careerRankLabel || prof.careerRank;
            }
            if (bridge.getPlayerProgress) {
                const prog = JSON.parse(bridge.getPlayerProgress());
                if (prog.level != null) pLevel = prog.level;
            }
        } catch(e) {}
        headerCard.innerHTML = `
            <div style="display:flex; justify-content:space-between; align-items:center;">
                <div>
                    <h1 style="font-size:1.15rem; color:var(--text); margin:0;">Menu & Pengaturan</h1>
                    <small style="color:var(--text-secondary);">Karier, teknologi trading, dan preferensi akun</small>
                </div>
                <span class="m-tag neutral" style="font-size:0.75rem;">Lv.${pLevel} · ${pRank}</span>
            </div>
        `;
        view.append(headerCard);

        // Group 1: Profil & Karier
        const g1 = createHubGroup('Profil & Karier');
        g1.list.append(
            createHubRow('👤', 'Profil Trader', 'Ubah nama akun dan lihat statistik performamu', pRank, () => openPlayerProfileModal()),
            createHubRow('🎨', 'Avatar & Ikon Profil', 'Pilih ikon avatar untuk profil investor Anda', 'Avatar', () => openAvatarPickerModal()),
            createHubRow('🏆', 'Pencapaian & XP', 'Daftar lencana dan progres kenaikan level', `Level ${pLevel}`, () => openAchievementsModal()),
            createHubRow('🎯', 'Gaya Trading', 'Analisis kebiasaan dan gaya investasimu di bursa', 'Gaya', () => openInvestorArchetypeModal()),
            createHubRow('🌟', 'Warisan Prestise', 'Mulai Era Baru dengan lencana kosmetik prestise & modal awal Rp 50 Juta', 'Warisan', () => openPrestigeModal())
        );
        view.append(g1.card);

        // Group 2: Tantangan & Peringkat
        const g2 = createHubGroup('Tantangan & Peringkat');
        g2.list.append(
            createHubRow('📋', 'Misi Tantangan', 'Target harian dan mingguan berhadiah kas & XP', 'Target', () => openPeriodicMissionsModal()),
            createHubRow('🥇', 'Liga & Peringkat', 'Bersaing mengejar cuan melawan trader lain', 'Liga', () => openLeagueAndRivalModal()),
            createHubRow('⚔️', 'Duel Bos Taipan', 'Tantang 4 bos pasar dalam adu cuan saham', 'Duel', () => openMarketBossModal())
        );
        view.append(g2.card);

        // Group 3: Teknologi & Booster
        const g3 = createHubGroup('Teknologi & Booster');
        g3.list.append(
            createHubRow('🌳', 'Pohon Teknologi', 'Buka automasi order dan diskon komisi broker', 'Teknologi', () => openTechTreeModal()),
            createHubRow('⚡', 'Kartu Booster', 'Tingkatkan akurasi dan keuntungan secara instan', 'Booster', () => openResearchBoostersModal())
        );
        view.append(g3.card);

        // Group 4: Bantuan & Pengaturan Akun
        const g4 = createHubGroup('Bantuan & Pengaturan Akun');
        g4.list.append(
            createHubRow('📖', 'Panduan Bermain', 'Aturan bursa 48x, istilah saham, dan tips cuan', 'Panduan', () => openTutorialModal()),
            createHubRow('⏱️', 'Jadwal Sesi Bursa', 'Jam perdagangan sesi I, sesi II, dan pasar US', 'Waktu', () => openClockSyncModal()),
            createHubRow('💾', 'Simpan Data (Save)', 'Cadangkan dan pulihkan data permainan', 'Simpan', () => openSaveManagementModal()),
            createHubRow('🔔', 'Kotak Pesan', 'Riwayat transaksi dan pengumuman bursa', 'Pesan', () => openNotificationHistoryModal()),
            createHubRow('🔄', 'Mulai Ulang Game', 'Hapus semua progres dan mulai karier baru dari awal', 'Atur Ulang', () => openNewGameConfirmationModal())
        );
        view.append(g4.card);
    }

    function openPrestigeModal() {
        const panel = showFlow('Era Baru & Prestise');
        panel.setAttribute('data-modal-id', 'prestige-modal');
        flowText(panel, 'Informasi Era Baru', 'Masuki Era Baru untuk mereset perjalanan aktif. Sebagai bukti pencapaian karier, Anda memperoleh lencana kosmetik Era Baru dan memulai kembali dengan modal kas Rp 50.000.000.');

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
            flowText(card, 'Bonus Era Baru', pres.prestigeBonus || 'Badge kosmetik Era Baru');
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
                title: '1. Dunia Otomatis 48x Tanpa Pause',
                desc: 'Waktu game berjalan terus menerus secara deterministik: 1 hari bursa game setara dengan 30 menit nyata. Tidak ada manipulasi waktu buatan, menjaga integritas simulasi bursa.'
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
        const panel = showFlow('Mulai Karier Baru');
        panel.setAttribute('data-modal-id', 'new-game-modal');

        // 1. Profil & Nama Trader
        const profCard = node('section', 'card');
        profCard.style.cssText = 'background:var(--surface); border:1px solid var(--border); border-radius:12px; padding:14px; margin-bottom:12px;';
        profCard.innerHTML = `
            <span class="eyebrow" style="color:var(--gold);">1. IDENTITAS TRADER</span>
            <h3 style="font-size:1rem; color:#FFF; font-weight:700; margin:4px 0 8px;">Nama Karakter & Investor</h3>
        `;
        const nameInput = node('input', 'input-field');
        nameInput.type = 'text';
        nameInput.placeholder = 'Ketik nama karakter Anda...';
        nameInput.value = (latestSnapshot?.playerProfile?.name) || 'Trader Nusantara';
        nameInput.style.cssText = 'width:100%; font-weight:700; margin-bottom:4px;';
        profCard.append(nameInput);
        panel.append(profCard);

        // 2. Pilihan Modal Awal (Start Capital)
        const capCard = node('section', 'card');
        capCard.style.cssText = 'background:var(--surface); border:1px solid var(--border); border-radius:12px; padding:14px; margin-bottom:12px;';
        capCard.innerHTML = `
            <span class="eyebrow" style="color:var(--gold);">2. PILIHAN MODAL AWAL</span>
            <h3 style="font-size:1rem; color:#FFF; font-weight:700; margin:4px 0 8px;">Tentukan Modal & Tantangan</h3>
        `;
        
        const capitals = [
            { id: 10000000, label: '🎒 Mode Penantang (Hard)', amount: 'Rp 10.000.000', desc: 'Modal minim untuk pemain yang suka tantangan bertahan hidup dari nol.' },
            { id: 50000000, label: '💼 Mode Standar (Normal)', amount: 'Rp 50.000.000', desc: 'Modal seimbang untuk menyusun portofolio awal dan diversifikasi saham.' },
            { id: 250000000, label: '👑 Mode Taipan Warisan (Sultan)', amount: 'Rp 250.000.000', desc: 'Modal besar untuk langsung memborong saham blue chip dan ekspansi bisnis.' }
        ];

        let selectedCapital = 50000000;
        const capList = node('div');
        capList.style.cssText = 'display:flex; flex-direction:column; gap:8px; margin-bottom:8px;';

        const updateCapitalSelection = () => {
            capList.querySelectorAll('.cap-choice-card').forEach(c => {
                const isSel = Number(c.dataset.cap) === selectedCapital;
                c.style.borderColor = isSel ? 'var(--gold)' : 'var(--border)';
                c.style.background = isSel ? 'rgba(227,189,127,0.16)' : 'var(--surface-raised)';
            });
        };

        capitals.forEach(c => {
            const row = node('div', 'cap-choice-card');
            row.dataset.cap = c.id;
            row.style.cssText = 'border:1px solid var(--border); border-radius:10px; padding:10px 12px; cursor:pointer; background:var(--surface-raised); transition:all 0.15s ease;';
            row.innerHTML = `
                <div style="display:flex; justify-content:space-between; align-items:center;">
                    <strong style="font-size:0.86rem; color:var(--text);">${c.label}</strong>
                    <span class="m-tag pos" style="font-weight:700;">${c.amount}</span>
                </div>
                <small style="font-size:0.72rem; color:var(--text-secondary); display:block; margin-top:3px; line-height:1.35;">${c.desc}</small>
            `;
            row.onclick = () => {
                selectedCapital = c.id;
                updateCapitalSelection();
            };
            capList.append(row);
        });
        updateCapitalSelection();
        capCard.append(capList);
        panel.append(capCard);

        // 3. Konfirmasi Mulai Karier
        const actCard = node('section', 'card');
        actCard.style.cssText = 'background:var(--surface); border:1px solid var(--border); border-radius:12px; padding:14px; margin-bottom:12px;';
        actCard.innerHTML = `
            <p style="font-size:0.74rem; color:var(--negative); line-height:1.4; margin-bottom:12px;">
                ⚠️ <b>Peringatan:</b> Memulai karier baru akan mereset portofolio saham, kepemilikan bisnis, dan level aktif saat ini dari awal.
            </p>
        `;
        const startBtn = node('button', 'btn-primary', 'Mulai Karier Sekarang ➔');
        startBtn.type = 'button';
        startBtn.style.cssText = 'width:100%; min-height:46px; font-weight:700; font-size:0.9rem;';
        startBtn.onclick = () => {
            const pName = nameInput.value.trim() || 'Trader Nusantara';
            if (!confirm(`Mulai karier baru dengan nama "${pName}" dan modal awal Rp ${selectedCapital.toLocaleString('id-ID')}?`)) return;
            startBtn.disabled = true;
            startBtn.textContent = 'Menyiapkan permainan baru...';
            try {
                const req = JSON.stringify({
                    mode: 'INTERNAL_MARKET',
                    initialCapital: selectedCapital,
                    playerName: pName
                });
                const res = JSON.parse(bridge.startNewGame ? bridge.startNewGame(req) : '{"success":false,"message":"Layanan permainan baru belum tersedia"}');
                if (!res.success) throw new Error(res.message);
                showGameToast('success', 'Karier Baru Dimulai!', `Selamat datang ${pName}, modal awal telah siap.`);
                refreshSnapshot();
                setTimeout(() => {
                    closeFlow();
                    switchTab('beranda');
                }, 1000);
            } catch (err) {
                startBtn.disabled = false;
                startBtn.textContent = 'Mulai Karier Sekarang ➔';
                showGameToast('info', 'Gagal Memulai Baru', err.message);
            }
        };
        actCard.append(startBtn);
        panel.append(actCard);
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
    function showPromptModal(title, message, defaultValue, onConfirm) {
        const panel = showFlow(title, true);
        flowText(panel, 'Petunjuk', message);
        const input = document.createElement('input');
        input.type = 'number';
        input.className = 'input-field';
        input.value = defaultValue;
        input.style.margin = '12px 0';
        input.setAttribute('inputmode', 'numeric');
        const errorMsg = document.createElement('p');
        errorMsg.style.cssText = 'color:var(--negative); font-size:0.8rem; margin:4px 0 10px; display:none;';
        panel.appendChild(input);
        panel.appendChild(errorMsg);
        flowButton(panel, 'Lanjutkan Transaksi', () => {
            const val = input.value.trim();
            const num = Number(val);
            if (!val || !Number.isFinite(num) || num <= 0) {
                errorMsg.textContent = 'Masukkan nominal angka positif yang sah.';
                errorMsg.style.display = 'block';
                input.focus();
                return;
            }
            closeFlow();
            onConfirm(val);
        });
    }
    function showConfirmModal(title, message, onConfirm) {
        const panel = showFlow(title, true);
        flowText(panel, 'Konfirmasi', message);
        flowButton(panel, 'Ya, Lanjutkan', () => {
            closeFlow();
            onConfirm();
        });
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
            flowText(panel, 'Indeks', 'Detail indeks pasar belum tersedia.'); return;
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
                <p style="font-size:0.75rem; color:var(--text-secondary); margin:0;">${detail.name} · ${detail.gameDate} ${detail.gameTime}</p>
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

        // Pasang Bintang Watchlist di Kanan Atas Header (Comment 6)
        if (bridge.toggleWatchlist) {
            const flowHeader = panel.closest('.flow-page')?.querySelector('.flow-header');
            if (flowHeader && !flowHeader.querySelector('.watchlist-star-header-btn')) {
                const starTopBtn = document.createElement('button');
                starTopBtn.type = 'button';
                starTopBtn.className = 'watchlist-star-header-btn';
                starTopBtn.setAttribute('aria-label', stock.isWatchlist ? 'Hapus dari watchlist' : 'Tambah ke watchlist');
                starTopBtn.style.cssText = 'background:none; border:none; font-size:1.6rem; color:var(--gold); cursor:pointer; padding:2px 8px; line-height:1; flex-shrink:0; margin-left:auto;';
                starTopBtn.innerHTML = stock.isWatchlist ? '★' : '☆';
                starTopBtn.onclick = () => {
                    try {
                        const result = JSON.parse(bridge.toggleWatchlist(stock.id));
                        stock.isWatchlist = result.inWatchlist;
                        starTopBtn.innerHTML = stock.isWatchlist ? '★' : '☆';
                        loadStocks();
                    } catch (e) {}
                };
                flowHeader.appendChild(starTopBtn);
            }
        }

        metricCard(panel, stock.ticker + ' · ' + stock.exchange, stock.currentPrice, stock.name);
        flowText(panel, 'Kategori / sektor', stock.sector);

        // Chart Container (Langsung di bawah ringkasan harga)
        let currentTimeframe = 'R1D';
        let currentChartType = 'candle';
        let chartSignature = null;
        const chartWrapper = document.createElement('div');
        chartWrapper.id = 'instrument-chart-section';
        chartWrapper.style.margin = '12px 0';
        panel.appendChild(chartWrapper);

        // Rincian Kepemilikan Pengguna (Hanya tampil jika memiliki posisi > 0)
        if (currentAccount && bridge.getPortfolio) {
            try {
                const port = JSON.parse(bridge.getPortfolio(currentAccount.id));
                const myPos = (port.positions || []).find(p => p.instrumentId === stock.id);
                if (myPos && Number(myPos.quantity) > 0) {
                    const holdingsCard = document.createElement('div');
                    holdingsCard.id = 'detail-holdings-card';
                    holdingsCard.classList.add('card');
                    flowText(holdingsCard, 'Kepemilikan di portofolio', `${myPos.quantity} saham${stock.lotSize > 0 ? ' (' + Math.floor(myPos.quantity / stock.lotSize) + ' lot)' : ''}`);
                    flowText(holdingsCard, 'Nilai posisi', myPos.marketValue);
                    panel.appendChild(holdingsCard);
                }
            } catch(e) {}
        }

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
                let bars = chartData.bars || [];
                if (!bars.length) {
                    // Fallback Day 1: Bangkitkan lilin historis acuan langsung di klien jika backend belum terisi
                    const currentP = Number(stock.currentPrice || 1000);
                    bars = [];
                    for (let s = 0; s < 30; s++) {
                        const noise = Math.sin(s * 0.5) * (currentP * 0.01) + Math.cos(s * 0.3) * (currentP * 0.005);
                        const o = Math.max(10, currentP + noise);
                        const c = (s === 29) ? currentP : Math.max(10, currentP + Math.sin((s + 1) * 0.5) * (currentP * 0.01));
                        const h = Math.max(o, c) * 1.004;
                        const l = Math.min(o, c) * 0.996;
                        bars.push({
                            time: `D-${29 - s}`,
                            open: Math.round(o),
                            high: Math.round(h),
                            low: Math.round(l),
                            close: Math.round(c),
                            volume: 100 + Math.round(Math.abs(Math.sin(s)) * 500)
                        });
                    }
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
                        const isLast = (i === bars.length - 1);
                        const x = i * step + step / 2;
                        const isUp = b.close >= b.open;
                        const color = isUp ? 'var(--positive)' : 'var(--negative)';
                        const bodyY = Math.min(y(b.open), y(b.close));
                        const bodyH = Math.max(2, Math.abs(y(b.close) - y(b.open)));
                        const volH = (b.volume / maxVol) * 40;
                        const wickId = isLast ? 'id="live-candle-wick"' : '';
                        const bodyId = isLast ? 'id="live-candle-body"' : '';
                        svgContent += `
                            <g>
                                <line ${wickId} class="candle-wick" x1="${x}" x2="${x}" y1="${y(b.high)}" y2="${y(b.low)}" stroke="${color}" stroke-width="1.5" />
                                <rect ${bodyId} class="candle-body" x="${x - 4}" y="${bodyY}" width="8" height="${bodyH}" fill="${color}" />
                                <rect x="${x - 4}" y="${svgHeight - volH}" width="8" height="${volH}" fill="${color}" opacity="0.4" />
                            </g>
                        `;
                    });
                } else {
                    const pts = bars.map((b, i) => `${i ? 'L' : 'M'}${i * step + step / 2} ${y(b.close)}`).join(' ');
                    svgContent += `<path d="${pts}" fill="none" stroke="var(--teal)" stroke-width="2" />`;
                }

                panel._calcY = y;
                panel._chartBars = bars;

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

        // Key Market Stats (Stockbit 3x3 Grid) Sesuai Referensi Pengguna
        const rawCurrent = parseCleanPrice(stock.currentPriceRaw || stock.currentPrice);
        const rawPrev = parseCleanPrice(stock.previousCloseRaw || stock.previousClose) || rawCurrent;
        const rawOpen = parseCleanPrice(stock.openPriceRaw || stock.openPrice) || rawPrev;
        const rawHigh = stock.highPriceRaw ? parseCleanPrice(stock.highPriceRaw) : Math.round(Math.max(rawCurrent, rawOpen) * 1.015);
        const rawLow = stock.lowPriceRaw ? parseCleanPrice(stock.lowPriceRaw) : Math.round(Math.min(rawCurrent, rawOpen) * 0.985);

        // SINTESA MODUL 2: Radar Diskon Saham & Nilai Wajar Fundamental (VirCal)
        let fairValueNum = parseCleanPrice(stock.intrinsicValue);
        if (!fairValueNum || fairValueNum <= 0) {
            fairValueNum = rawCurrent;
        }
        let gapRate = 0.0;
        if (rawCurrent > 0) {
            gapRate = ((fairValueNum - rawCurrent) / rawCurrent) * 100.0;
        }
        let signalClass = 'neutral';
        let signalText = 'Harga Wajar (Hold)';
        if (gapRate >= 15.0) {
            signalClass = 'pos';
            signalText = `Diskon ${gapRate.toFixed(1)}% (Strong Buy)`;
        } else if (gapRate >= 5.0) {
            signalClass = 'pos';
            signalText = `Diskon ${gapRate.toFixed(1)}% (Buy)`;
        } else if (gapRate <= -5.0) {
            signalClass = 'neg';
            signalText = `Premium ${Math.abs(gapRate).toFixed(1)}% (Sell)`;
        }

        // Strip Nilai Wajar dihilangkan dari detail saham sesuai instruksi pemilik (Comment 2)

        // Auto Rejection Atas (ARA) & Bawah (ARB) 25%
        const araVal = Math.round(rawPrev * 1.25);
        const arbVal = Math.round(rawPrev * 0.75);

        // Volume Lot & Turnover Value
        const estLots = stock.volumeLots || Math.round((rawCurrent > 0 ? (15000000 / rawCurrent) : 1000));
        const lotDisplay = estLots >= 1000000 ? (estLots / 1000000).toFixed(2) + 'M' : (estLots >= 1000 ? (estLots / 1000).toFixed(2) + 'K' : String(estLots));

        const estTurnover = estLots * (stock.lotSize || 100) * rawCurrent;
        const valDisplay = estTurnover >= 1000000000000 ? (estTurnover / 1000000000000).toFixed(2) + ' T' : (estTurnover >= 1000000000 ? (estTurnover / 1000000000).toFixed(2) + ' B' : (estTurnover >= 1000000 ? (estTurnover / 1000000).toFixed(2) + ' M' : formatMoney(estTurnover)));

        const statsGrid = document.createElement('div');
        statsGrid.className = 'market-stats-card';
        statsGrid.style.cssText = 'margin:12px 0; background:var(--surface); border:1px solid rgba(227, 189, 127, 0.25); border-radius:12px; padding:10px 12px; width:100%; box-sizing:border-box;';
        statsGrid.innerHTML = `
            <div style="display:grid; grid-template-columns:repeat(3, 1fr); gap:8px 10px; font-variant-numeric:tabular-nums;">
                <!-- Row 1 -->
                <div style="display:flex; flex-direction:column; gap:2px;">
                    <span style="color:var(--text-secondary); font-size:0.68rem; text-transform:uppercase; font-weight:600;">Open</span>
                    <strong style="color:var(--text); font-weight:700; font-size:0.85rem;">${Number(rawOpen).toLocaleString('id-ID')}</strong>
                </div>
                <div style="display:flex; flex-direction:column; gap:2px;">
                    <span style="color:var(--text-secondary); font-size:0.68rem; text-transform:uppercase; font-weight:600;">Prev</span>
                    <strong style="color:var(--text); font-weight:700; font-size:0.85rem;">${Number(rawPrev).toLocaleString('id-ID')}</strong>
                </div>
                <div style="display:flex; flex-direction:column; gap:2px;">
                    <span style="color:var(--text-secondary); font-size:0.68rem; text-transform:uppercase; font-weight:600;">Lot</span>
                    <strong style="color:var(--positive); font-weight:700; font-size:0.85rem;">${lotDisplay}</strong>
                </div>

                <!-- Row 2 -->
                <div style="display:flex; flex-direction:column; gap:2px;">
                    <span style="color:var(--text-secondary); font-size:0.68rem; text-transform:uppercase; font-weight:600;">High</span>
                    <strong style="color:var(--positive); font-weight:700; font-size:0.85rem;">${Number(rawHigh).toLocaleString('id-ID')}</strong>
                </div>
                <div style="display:flex; flex-direction:column; gap:2px;">
                    <span style="color:var(--text-secondary); font-size:0.68rem; text-transform:uppercase; font-weight:600;">ARA</span>
                    <strong style="color:var(--teal); font-weight:700; font-size:0.85rem;">${Number(araVal).toLocaleString('id-ID')} <small style="font-size:0.75rem;">⮝</small></strong>
                </div>
                <div style="display:flex; flex-direction:column; gap:2px;">
                    <span style="color:var(--text-secondary); font-size:0.68rem; text-transform:uppercase; font-weight:600;">Val</span>
                    <strong style="color:var(--positive); font-weight:700; font-size:0.85rem;">${valDisplay}</strong>
                </div>

                <!-- Row 3 -->
                <div style="display:flex; flex-direction:column; gap:2px;">
                    <span style="color:var(--text-secondary); font-size:0.68rem; text-transform:uppercase; font-weight:600;">Low</span>
                    <strong style="color:var(--negative); font-weight:700; font-size:0.85rem;">${Number(rawLow).toLocaleString('id-ID')}</strong>
                </div>
                <div style="display:flex; flex-direction:column; gap:2px;">
                    <span style="color:var(--text-secondary); font-size:0.68rem; text-transform:uppercase; font-weight:600;">ARB</span>
                    <strong style="color:var(--negative); font-weight:700; font-size:0.85rem;">${Number(arbVal).toLocaleString('id-ID')} <small style="font-size:0.75rem;">⮟</small></strong>
                </div>
                <div style="display:flex; flex-direction:column; gap:2px;">
                    <span style="color:var(--text-secondary); font-size:0.68rem; text-transform:uppercase; font-weight:600;">Avg</span>
                    <strong style="color:var(--positive); font-weight:700; font-size:0.85rem;">${Number(rawCurrent).toLocaleString('id-ID')}</strong>
                </div>
            </div>
        `;
        panel.appendChild(statsGrid);

        // Order Book
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
                            ${Array.from({ length: Math.max((book.bids || []).length, (book.asks || []).length, 3) }).map((_, i) => {
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
                // Running Trade / Tape Dihapus Sesuai Permintaan Pengguna (Comment 4)
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
        fundNav.className = 'fund-nav-bar subtab-pills-bar';
        fundNav.style.cssText = 'display:flex; gap:4px; background:rgba(0,0,0,0.4); padding:4px; border-radius:12px; border:1px solid var(--border); margin-bottom:12px; flex-wrap:nowrap;';
        const fundTabs = [
            { id: 'ringkasan', label: 'Ringkasan' },
            { id: 'valuasi', label: 'Valuasi' },
            { id: 'keuangan', label: 'Keuangan' },
            { id: 'pemegang', label: 'Pemegang' }
        ];

        const fundContent = document.createElement('div');
        fundContent.id = 'fund-content-box';
        fundContent.style.cssText = 'max-width:100%; box-sizing:border-box; overflow:hidden;';

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
                mountIssuerOwnership(fundContent, stock.id);
            }
        }

        fundTabs.forEach(t => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.dataset.tabId = t.id;
            btn.className = 'subtab-pill' + (t.id === 'ringkasan' ? ' active' : '');
            btn.textContent = t.label;
            btn.style.cssText = 'flex:1 1 0; min-width:0; padding:8px 2px; font-size:0.76rem; font-weight:700; text-align:center; border-radius:8px; border:1px solid transparent; white-space:nowrap;';
            btn.onclick = () => renderFundTab(t.id);
            fundNav.appendChild(btn);
        });

        fundContainer.append(fundNav, fundContent);
        panel.appendChild(fundContainer);
        renderFundTab('ringkasan');

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
                item.append(node('h3', '', news.headline), node('p', '', `${news.categoryLabel} · Estimasi: ${news.direction} ${news.estimatedRange || ''}`), newsEffectNode(news));
                flowButton(item, 'Baca ' + news.headline, () => openNewsDetail(news, () => openInstrumentDetail(stock)));
                issuerNews.append(item);
            });
        } catch (error) { issuerNews.append(node('p', 'notice', 'Berita terkait belum dapat dimuat. Coba buka kembali rincian emiten.')); }
        panel.append(issuerNews);

        const isHalted = book && book.haltState && book.haltState !== 'NORMAL';
        if (isHalted) {
            const suspNotice = node('p', 'notice', '⛔ Perdagangan saham disuspensi sementara oleh bursa (ARA/ARB). Transaksi ditutup.');
            suspNotice.id = 'detail-suspension-notice';
            panel.appendChild(suspNotice);
        }
        // Dual Actions Dock: JUAL & BELI (Fixed di dasar di atas tab)
        panel.style.paddingBottom = '180px';
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
        flowText(panel, 'Instrumen', `${draft.instrumentId} · ${preview.currency}`);
        flowText(panel, 'Order', `${draft.side === 'BUY' ? 'Beli' : 'Jual'} · ${draft.type} · ${draft.quantity} saham`);
        flowText(panel, 'Harga / pemicu', `${draft.limitPrice || draft.triggerPrice || 'Market sesuai likuiditas'} ${preview.currency} per saham`);
        flowText(panel, 'Estimasi fill / sisa', `${preview.estimatedFillQuantity} / ${preview.remainingQuantity} saham`);
        flowText(panel, 'Harga rata-rata / slippage', `${preview.estimatedVwap == null ? 'Tidak ada fill' : Number(preview.estimatedVwap).toLocaleString('id-ID',{maximumFractionDigits:4})} / ${preview.slippagePercent == null ? 'Belum tersedia' : Number(preview.slippagePercent).toLocaleString('id-ID',{maximumFractionDigits:3})+'%'}`);
        flowText(panel, 'Estimasi fee', preview.estimatedFee);
        flowText(panel, 'Estimasi dampak kas', preview.estimatedCashImpact);
        flowText(panel, 'Kas diperlukan', preview.requiredCash);
        flowText(panel, 'Kas tersedia', preview.availableCash);
        flowText(panel, 'Batas', preview.warning);
        flowButton(panel, 'Kirim order', event => {
            if (Date.now() > reviewCreatedTime + 300000) {
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
        const panel = showFlow('Aksi Korporasi & Voting RUPS');
        flowText(panel, 'Aksi Korporasi', 'Jadwal dividen, pemecahan saham, dan hak suara RUPS.');

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
                        const valText = act.type === 'STOCK_SPLIT' ? `Rasio: ${act.valueOrRatio}` : `Nilai: ${act.valueOrRatio} ${act.currency || ''}`.trim();
                        flowText(row, `${act.type} · ${act.ticker}`, valText);
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

    function openIpoSubscriptionDetail(s) {
        const panel = showFlow('Detail Pesanan e-IPO');
        
        // Kartu Rincian Pemesanan (Sesuai gambar tampilan_ordert_34b2c0.webp)
        const orderCard = node('section', 'card');
        orderCard.style.cssText = 'border-left: 4px solid var(--teal); margin-bottom: 16px; padding: 16px;';
        
        const cardHeader = node('div');
        cardHeader.style.cssText = 'display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 14px; border-bottom: 1px solid var(--border); padding-bottom: 10px;';
        cardHeader.innerHTML = `<div><strong style="font-size:1.15rem; color:#FFF;">${s.ticker}</strong><br><small style="color:var(--text-muted);">${s.companyName || ''}</small></div><span class="m-tag ${s.status === 'ALLOTTED' ? 'pos' : s.status === 'CANCELLED' ? 'neg' : 'neutral'}">${s.statusLabel || s.status}</span>`;
        orderCard.append(cardHeader);

        const grid = node('div');
        grid.style.cssText = 'display: flex; flex-direction: column; gap: 8px; font-size: 0.85rem;';
        
        const pctAllotted = s.orderedLots > 0 && s.allottedLots > 0 ? ` (${((s.allottedLots / s.orderedLots) * 100).toFixed(0)}%)` : '';
        const allottedDisplay = s.status === 'ALLOTTED' ? `${s.allottedLots} lot${pctAllotted}` : (s.status === 'CANCELLED' ? 'Dibatalkan (0 lot)' : 'Menunggu Penjatahan');

        const rows = [
            ['Order ID', s.id || `IPO-${s.ticker}-001`],
            ['Allocation Type', 'Pool (Penjatahan Terpusat)'],
            ['Jumlah Lot Pemesanan', `${s.orderedLots} lot`],
            ['Jumlah Lot Penjatahan', allottedDisplay],
            ['Harga Pelaksanaan', s.price || '-'],
            ['Nilai Penjatahan', s.allotmentValue || (s.status === 'ALLOTTED' ? s.reservedMoney : 'Menunggu Penjatahan')]
        ];

        rows.forEach(([k, v]) => {
            const r = node('div');
            r.style.cssText = 'display: flex; justify-content: space-between; align-items: center; padding: 3px 0;';
            r.innerHTML = `<span style="color:var(--text-muted);">${k}</span><strong style="color:var(--text); text-align:right;">${v}</strong>`;
            grid.append(r);
        });
        orderCard.append(grid);
        panel.append(orderCard);

        // Timeline Tracker 5-Tahap (Sesuai gambar tampilan_ordert_34b2c0.webp)
        const timelineCard = node('section', 'card');
        timelineCard.style.cssText = 'margin-bottom: 16px; padding: 16px;';
        timelineCard.append(node('h2', 'card-title', 'Proses & Linimasa e-IPO'));

        const isCancelled = s.status === 'CANCELLED';
        const isAllotted = s.status === 'ALLOTTED';

        const timelineSteps = [
            { title: 'Penawaran Awal (Bookbuilding)', date: s.offeringStartDate || 'Selesai', status: 'done', desc: 'Pengumpulan minat investor' },
            { title: 'Penawaran Umum (Offering)', date: s.offeringEndDate || 'Selesai', status: isCancelled ? 'fail' : 'done', desc: isCancelled ? 'Pemesanan dibatalkan' : 'Pemesanan terkonfirmasi (Confirmed)' },
            { title: 'Penjatahan Efek (Allotment)', date: s.allotmentDate || 'Selesai', status: isCancelled ? 'fail' : isAllotted ? 'done' : 'active', desc: isCancelled ? 'Tidak berpartisipasi' : isAllotted ? 'Allotted w/ Scale Back (Penjatahan Terpenuhi)' : 'Proses penjatahan bursa' },
            { title: 'Distribusi Saham', date: s.allotmentDate || 'Selesai', status: isAllotted ? 'done' : 'pending', desc: isAllotted ? 'Saham didistribusikan ke portofolio' : 'Menunggu distribusi efek' },
            { title: 'IPO (Pencatatan Bursa)', date: s.listingDate || 'Listing', status: isAllotted ? 'active' : 'pending', desc: 'Saham resmi tercatat & diperdagangkan di pasar reguler' }
        ];

        const tlList = node('div');
        tlList.style.cssText = 'display: flex; flex-direction: column; gap: 14px; margin-top: 14px; position: relative; padding-left: 24px;';
        
        const line = node('div');
        line.style.cssText = 'position: absolute; left: 7px; top: 8px; bottom: 8px; width: 2px; background: var(--border); z-index: 1;';
        tlList.append(line);

        timelineSteps.forEach(st => {
            const item = node('div');
            item.style.cssText = 'position: relative; z-index: 2;';
            const dotColor = st.status === 'done' ? 'var(--teal)' : st.status === 'active' ? 'var(--gold)' : st.status === 'fail' ? 'var(--negative)' : 'var(--text-muted)';
            const dotIcon = st.status === 'done' ? '✓' : st.status === 'fail' ? '✕' : '●';
            
            item.innerHTML = `
                <div style="position: absolute; left: -24px; top: 0px; width: 16px; height: 16px; border-radius: 50%; background: #0E1322; border: 2px solid ${dotColor}; color: ${dotColor}; font-size: 0.65rem; display: flex; align-items: center; justify-content: center; font-weight: bold;">${dotIcon}</div>
                <div style="display: flex; justify-content: space-between; align-items: baseline;">
                    <strong style="font-size: 0.85rem; color: ${st.status === 'pending' ? 'var(--text-muted)' : '#FFF'};">${st.title}</strong>
                    <small style="color: var(--text-muted); font-size: 0.72rem;">${st.date}</small>
                </div>
                <p style="font-size: 0.75rem; color: ${st.status === 'done' && st.title.includes('Penjatahan') ? 'var(--border-focus)' : 'var(--text-secondary)'}; margin: 2px 0 0 0;">${st.desc}</p>
            `;
            tlList.append(item);
        });
        timelineCard.append(tlList);
        panel.append(timelineCard);

        if (s.canCancel) {
            const cancelBtn = node('button', 'btn-primary', 'Batalkan Pemesanan (Refund Dana)');
            cancelBtn.style.cssText = 'background: rgba(239,68,68,0.18); border: 1px solid var(--negative); color: var(--negative); margin-bottom: 10px; width: 100%; min-height: 48px;';
            cancelBtn.onclick = () => {
                if (confirm(`Batalkan pemesanan IPO ${s.ticker} (${s.orderedLots} lot)? Dana tertahan ${s.reservedMoney} akan langsung cair kembali ke kas pribadi.`)) {
                    cancelBtn.disabled = true;
                    try {
                        const cRes = JSON.parse(bridge.cancelIpoSubscription(s.id));
                        if (cRes.success) {
                            refreshSnapshot();
                            showGameToast('success', 'Pemesanan Dibatalkan', `Dana ${s.reservedMoney} telah dikembalikan ke kas pribadi.`);
                            closeFlow();
                            openIpoCenter();
                        } else {
                            alert(cRes.message || 'Gagal membatalkan');
                            cancelBtn.disabled = false;
                        }
                    } catch(err) {
                        alert(err.message);
                        cancelBtn.disabled = false;
                    }
                }
            };
            panel.append(cancelBtn);
        }

        const backBtn = node('button', 'btn-secondary', 'Kembali ke Daftar e-IPO');
        backBtn.style.cssText = 'width: 100%; min-height: 48px;';
        backBtn.onclick = () => { closeFlow(); openIpoCenter(); };
        panel.append(backBtn);
    }

    function openIpoOrderModal(ipo) {
        const panel = showFlow(`Pesan e-IPO · ${ipo.ticker}`);
        
        const card = node('section', 'card');
        card.style.cssText = 'padding: 16px; margin-bottom: 14px;';
        
        const header = node('div');
        header.style.cssText = 'display: flex; align-items: center; gap: 12px; margin-bottom: 12px; border-bottom: 1px solid var(--border); padding-bottom: 10px;';
        header.innerHTML = `
            <div style="width:44px; height:44px; border-radius:50%; background:#FFF; color:#151515; display:flex; align-items:center; justify-content:center; font-weight:800; font-size:0.95rem; flex-shrink:0;">${ipo.ticker.slice(0, 2)}</div>
            <div>
                <strong style="font-size: 1.1rem; color: #FFF;">${ipo.ticker}</strong>
                <p style="margin: 2px 0 0 0; font-size: 0.8rem; color: var(--text-muted);">${ipo.companyName}</p>
            </div>
        `;
        card.append(header);

        flowText(card, 'Harga Penawaran', `${ipo.offeringPrice} (1 lot = ${ipo.lotSize} lembar)`);
        flowText(card, 'Total Saham Ditawarkan', `${Number(ipo.sharesOffered).toLocaleString('id-ID')} lembar`);
        flowText(card, 'Jadwal Listing Bursa', ipo.listingDate);
        panel.append(card);

        const formCard = node('section', 'card');
        formCard.style.cssText = 'padding: 16px; margin-bottom: 14px;';
        formCard.append(node('h3', 'card-title', 'Formulir Pemesanan Lot'));

        const lotLabel = node('label', '', 'Jumlah lot yang dipesan');
        lotLabel.style.cssText = 'font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 4px; display: block;';
        
        const lotInput = node('input', 'input-field');
        lotInput.type = 'number';
        lotInput.min = '1';
        lotInput.value = '10';
        lotInput.style.marginBottom = '10px';

        const estBox = node('div');
        estBox.style.cssText = 'background: rgba(0,0,0,0.3); padding: 10px 12px; border-radius: 8px; margin-bottom: 12px; font-size: 0.82rem;';
        
        const rawP = parseCleanPrice(ipo.offeringPriceRaw || ipo.offeringPrice);
        const updateEst = () => {
            const lots = Math.max(1, parseInt(lotInput.value) || 1);
            const total = lots * ipo.lotSize * rawP;
            estBox.innerHTML = `Perkiraan Dana Tertahan: <strong style="color:var(--gold);">${formatMoney(total, ipo.currency)}</strong><br><small style="color:var(--text-muted);">Dana tertahan dikembalikan (refund) jika penjatahan mengalami scale back.</small>`;
        };
        lotInput.oninput = updateEst;
        updateEst();

        const errText = node('p');
        errText.setAttribute('role', 'alert');
        errText.style.cssText = 'color: var(--negative); font-size: 0.8rem; margin: 4px 0 10px 0;';

        const submitBtn = node('button', 'btn-primary', 'Konfirmasi Pesan Saham e-IPO');
        submitBtn.style.cssText = 'width: 100%; min-height: 48px;';
        submitBtn.onclick = () => {
            const lots = Number(lotInput.value);
            if (!Number.isFinite(lots) || lots <= 0) {
                errText.textContent = 'Jumlah lot pemesanan minimal 1 lot.';
                return;
            }
            submitBtn.disabled = true;
            submitBtn.textContent = 'Memproses pemesanan...';
            try {
                const reqId = 'REQ-IPO-' + Date.now();
                const res = JSON.parse(bridge.orderIpo(JSON.stringify({ ipoId: ipo.id, lots, requestId: reqId })));
                if (!res.success) throw new Error(res.message);
                refreshSnapshot();
                showGameToast('success', 'Pemesanan Berhasil', `Pemesanan ${lots} lot ${ipo.ticker} berhasil dicatat.`);
                closeFlow();
                openIpoCenter();
            } catch(e) {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Konfirmasi Pesan Saham e-IPO';
                errText.textContent = 'Pemesanan gagal: ' + e.message;
            }
        };

        formCard.append(lotLabel, lotInput, estBox, errText, submitBtn);
        panel.append(formCard);
    }

    function openIpoCenter() {
        const panel = showFlow('e-IPO');
        
        // 1. Hero Banner e-IPO Roket (Sesuai gambar beranda_e_ipo_0af308.webp)
        const heroBanner = node('div', 'eipo-hero-banner');
        heroBanner.style.cssText = 'background:linear-gradient(135deg, rgba(20,241,149,0.12) 0%, rgba(14,19,34,0.95) 100%); border:1px solid rgba(20,241,149,0.3); border-radius:16px; padding:18px 20px; margin-bottom:18px; display:flex; justify-content:space-between; align-items:center;';
        heroBanner.innerHTML = `
            <div style="flex:1; padding-right:12px;">
                <h2 style="font-size:1.35rem; font-weight:800; color:#FFF; margin-bottom:6px; display:flex; align-items:center; gap:8px;">e-IPO 🚀</h2>
                <p style="font-size:0.85rem; color:var(--text-muted); line-height:1.4; margin:0;">Dengan e-IPO, kamu bisa cari dan pesan saham IPO dengan mudah.</p>
            </div>
            <div style="flex-shrink:0;">
                <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
                    <path d="M32 6 C38 14 44 26 44 38 L20 38 C20 26 26 14 32 6 Z" fill="rgba(20,241,149,0.2)" stroke="#14F195" stroke-width="2" stroke-linejoin="round"/>
                    <circle cx="32" cy="22" r="4" fill="#0E1322" stroke="#FFF" stroke-width="2"/>
                    <path d="M20 34 L12 40 L16 48 L22 42" stroke="#FFF" stroke-width="1.8" stroke-linejoin="round"/>
                    <path d="M44 34 L52 40 L48 48 L42 42" stroke="#FFF" stroke-width="1.8" stroke-linejoin="round"/>
                    <path d="M28 42 L32 54 L36 42 Z" fill="#14F195"/>
                    <circle cx="50" cy="14" r="1.5" fill="#FFF"/>
                    <circle cx="14" cy="20" r="1" fill="#FFF"/>
                    <path d="M10 52 C14 50 18 54 22 52" stroke="rgba(20,241,149,0.5)" stroke-width="1.5" stroke-dasharray="2 2"/>
                </svg>
            </div>
        `;
        panel.append(heroBanner);

        // 2. Pesanan IPO Saya (Jika ada)
        if (bridge.getMyIpoSubscriptions) {
            try {
                const subs = JSON.parse(bridge.getMyIpoSubscriptions() || '[]');
                if (subs && subs.length) {
                    const mySection = node('section', 'card');
                    mySection.style.cssText = 'margin-bottom:18px; border-left:4px solid var(--teal); padding:14px 16px;';
                    mySection.append(node('h2', 'card-title', 'Pesanan IPO Saya'));
                    subs.forEach(s => {
                        const row = node('div', 'data-row');
                        row.style.cssText = 'display:flex; justify-content:space-between; align-items:center; padding:12px 0; border-bottom:1px solid var(--border); cursor:pointer;';
                        const isCancelled = s.status === 'CANCELLED';
                        const isAllotted = s.status === 'ALLOTTED';
                        const statusColor = isCancelled ? 'var(--negative)' : isAllotted ? 'var(--positive)' : 'var(--border-focus)';
                        
                        row.innerHTML = `
                            <div>
                                <strong style="font-size:0.95rem; color:#FFF;">${s.ticker} · ${s.orderedLots} lot</strong>
                                <p style="font-size:0.75rem; color:var(--text-muted); margin:2px 0 0 0;">Tertahan: ${s.reservedMoney} · <span style="color:${statusColor}; font-weight:700;">${s.statusLabel || s.status}</span></p>
                            </div>
                            <span class="subtab-pill" style="font-size:0.75rem; padding:4px 10px; color:var(--teal); border-color:var(--teal);">Detail ↗</span>
                        `;
                        row.onclick = () => openIpoSubscriptionDetail(s);
                        mySection.append(row);
                    });
                    panel.append(mySection);
                }
            } catch (e) {}
        }

        // 3. Section Header & 3 Filter Pills: Ongoing, Upcoming, Past (Sesuai gambar beranda_e_ipo_0af308.webp)
        const listHeader = node('div');
        listHeader.style.cssText = 'display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;';
        listHeader.innerHTML = `
            <div style="display:flex; align-items:center; gap:6px;">
                <h2 style="font-size:1.05rem; font-weight:700; color:#FFF; margin:0;">List Saham e-IPO</h2>
                <span title="Daftar saham penawaran umum perdana" style="color:var(--text-muted); font-size:0.85rem; cursor:help;">ⓘ</span>
            </div>
        `;
        panel.append(listHeader);

        const filterBar = node('div', 'filter-chips-bar');
        filterBar.style.cssText = 'display:flex; gap:8px; margin-bottom:14px; overflow-x:auto; padding-bottom:4px;';
        
        let activeIpoFilter = 'ongoing';
        const filterBtns = [
            { key: 'ongoing', label: 'Ongoing' },
            { key: 'upcoming', label: 'Upcoming' },
            { key: 'past', label: 'Past' }
        ];

        const cardsContainer = node('div', 'ipo-cards-container');

        const renderIpoList = () => {
            cardsContainer.replaceChildren();
            if (!bridge.getIpoOfferings) {
                flowText(cardsContainer, 'IPO', 'Penawaran IPO belum dapat dimuat.');
                return;
            }
            try {
                const ipos = JSON.parse(bridge.getIpoOfferings());
                if (!ipos.length) {
                    emptyState(cardsContainer, 'Belum Ada Penawaran', 'Saat ini belum ada data e-IPO di katalog.');
                    return;
                }

                const filtered = ipos.filter(ipo => {
                    if (activeIpoFilter === 'past') return ipo.isAllotted;
                    if (activeIpoFilter === 'upcoming') return !ipo.isAllotted && (ipo.offeringStartDate && ipo.offeringStartDate.includes('2027'));
                    // Default Ongoing: penawaran yang sedang dibuka
                    return !ipo.isAllotted;
                });

                if (!filtered.length) {
                    emptyState(cardsContainer, 'Tidak Ada Saham', `Tidak ada saham IPO pada kategori ${activeIpoFilter.toUpperCase()}.`);
                    return;
                }

                filtered.forEach(ipo => {
                    const card = node('article', 'stockbit-ipo-card');
                    card.style.cssText = 'background:var(--surface); border:1px solid var(--border); border-radius:14px; padding:14px 16px; margin-bottom:12px; cursor:pointer; transition:transform 150ms ease, border-color 150ms ease;';
                    card.onclick = () => openIpoOrderModal(ipo);

                    const isAllotted = ipo.isAllotted;
                    const stageText = isAllotted ? 'Distribusi Saham' : 'Penawaran Awal';
                    const stageColor = isAllotted ? 'var(--teal)' : 'var(--border-focus)';
                    const dateText = (ipo.offeringStartDate && ipo.listingDate) 
                        ? `${ipo.offeringStartDate.slice(5)} - ${ipo.listingDate.slice(5)}` 
                        : (ipo.listingDate || 'Listing');

                    card.innerHTML = `
                        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
                            <div style="display:flex; align-items:center; gap:12px; min-width:0;">
                                <div style="width:40px; height:40px; border-radius:50%; background:#FFF; color:#151515; display:flex; align-items:center; justify-content:center; font-weight:800; font-size:0.85rem; flex-shrink:0;">${ipo.ticker.slice(0, 2)}</div>
                                <div style="min-width:0;">
                                    <strong style="font-size:1.05rem; color:#FFF; display:block;">${ipo.ticker}</strong>
                                    <span style="font-size:0.75rem; color:var(--text-muted); display:block; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${ipo.companyName}</span>
                                </div>
                            </div>
                            <strong style="font-size:1rem; color:#FFF; font-weight:700; white-space:nowrap; margin-left:8px;">${ipo.offeringPrice}</strong>
                        </div>
                        <div style="border-top:1px solid var(--border); padding-top:8px; display:flex; justify-content:space-between; align-items:center; font-size:0.75rem;">
                            <span style="color:${stageColor}; font-weight:700; display:flex; align-items:center; gap:4px;">● ${stageText}</span>
                            <span style="color:var(--text-muted);">${dateText}</span>
                        </div>
                    `;
                    cardsContainer.append(card);
                });
            } catch (err) {
                flowText(cardsContainer, 'Gagal memuat IPO', err.message);
            }
        };

        filterBtns.forEach(f => {
            const btn = node('button', 'subtab-pill' + (f.key === activeIpoFilter ? ' active' : ''), f.label);
            btn.style.cssText = 'padding:6px 16px; font-size:0.82rem; font-weight:700; border-radius:20px;';
            btn.onclick = () => {
                filterBar.querySelectorAll('button').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                activeIpoFilter = f.key;
                renderIpoList();
            };
            filterBar.append(btn);
        });

        panel.append(filterBar);
        panel.append(cardsContainer);
        renderIpoList();
    }

    function openMultiAssets() {
        const panel = showFlow('Katalog Multi-Aset');
        flowText(panel, 'Informasi', 'Diversifikasi kas portofolio Anda ke instrumen Obligasi Negara & Emas Murni.');

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
                    card.style.cssText = 'background:var(--surface); border:1px solid var(--border); border-radius:12px; padding:16px; margin-bottom:12px;';
                    const h = document.createElement('h3');
                    h.innerHTML = `<span style="font-size:1.1rem; color:#FFF; font-weight:700;">${b.ticker}</span> <span class="m-tag pos" style="font-size:0.68rem; margin-left:6px;">Fixed Income</span><br><small style="color:var(--text-muted); font-size:0.8rem;">${b.name}</small>`;
                    card.append(h);

                    const grid = node('div');
                    grid.style.cssText = 'display:grid; grid-template-columns:1fr 1fr; gap:8px; margin:12px 0; background:rgba(0,0,0,0.25); padding:10px 12px; border-radius:8px; font-size:0.8rem;';
                    grid.innerHTML = `
                        <div><small style="color:var(--text-muted);">Nilai Par</small><br><strong style="color:var(--text);">${b.parValue} (${b.currency})</strong></div>
                        <div><small style="color:var(--text-muted);">Kupon Tahunan</small><br><strong style="color:var(--gold);">${(Number(b.couponRateAnnual) * 100).toFixed(3)}% p.a.</strong></div>
                        <div><small style="color:var(--text-muted);">Harga Bersih (Clean)</small><br><strong style="color:var(--text);">${b.cleanPrice}</strong></div>
                        <div><small style="color:var(--text-muted);">Jatuh Tempo</small><br><strong style="color:var(--text);">${b.maturityDate}</strong></div>
                    `;
                    card.append(grid);
                    flowText(card, 'Kepemilikan portofolio', `${b.quantity} unit (Nilai pasar: ${b.marketValue})`);
                    flowText(card, 'Status perdagangan', `${b.status} · Fee 0.1% · ${b.note || 'Kupon sesuai waktu kepemilikan'}`);
                    
                    const btnRow = node('div');
                    btnRow.style.cssText = 'display:flex; gap:10px; margin-top:12px;';
                    const buyBtn = node('button', 'btn-primary', 'Beli Obligasi');
                    buyBtn.style.cssText = 'flex:1; min-height:44px;';
                    buyBtn.onclick = () => openMultiAssetOrder(b, true);
                    const sellBtn = node('button', 'btn-secondary', 'Jual Obligasi');
                    sellBtn.style.cssText = 'flex:1; min-height:44px;';
                    sellBtn.onclick = () => openMultiAssetOrder(b, false);
                    btnRow.append(buyBtn, sellBtn);
                    card.append(btnRow);
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
        const panel = showFlow('Replay Historis');
        const card = node('section', 'card');
        card.append(node('h2', 'card-title', 'Mode Replay Dialihkan'));
        card.append(node('p', '', 'Fitur simulasi replay historis kini telah dipadukan sepenuhnya ke dalam Pasar Utama Realtime dengan sistem Order Book 10-tingkat dan siklus makro terpadu.'));
        panel.append(card);
        flowButton(panel, 'Kembali ke Bursa Pasar', () => {
            closeFlow();
            switchTab('pasar');
        });
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

            flowText(panel, 'Rekonsiliasi', 'Neraca aset pribadi terverifikasi seimbang dan terpisah dari dana kelolaan klien.');
        } catch (e) {
            flowText(panel, 'Gagal memuat', e.message);
        }
    }

    function openMyMutualFundsModal() {
        openMutualFundsMarketModal();
    }

    function openMutualFundsMarketModal() {
        const panel = showFlow('Pasar Reksa Dana');
        flowText(panel, 'Jadwal Transaksi', 'Cutoff transaksi pukul 16:00 WIB. Valuasi harga unit mengikuti NAB harian.');

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

        const subscriptionId = generateSafeId('FUND-SUB');
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

        const redemptionId = generateSafeId('FUND-RED');
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
        let loanRateText = 'bunga harian kompetitif mengikuti siklus pasar';
        if (bridge.getMacroAndSynthesisState) {
            try {
                const macro = JSON.parse(bridge.getMacroAndSynthesisState());
                if (macro.loanRateAnnual) {
                    const dailyRate = (macro.loanRateAnnual / 365).toFixed(3);
                    loanRateText = `bunga efektif ${macro.loanRateAnnual.toFixed(1)}% p.a. (~${dailyRate}% per hari game)`;
                }
            } catch(e) {}
        }
        flowText(panel, 'Ketentuan Fasilitas', `Tenor fleksibel 7, 14, atau 30 hari game dengan ${loanRateText}.`);

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
                        const prepayRequestId = generateSafeId('LOAN-PREPAY');
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
        flowText(panel, 'Ketentuan Pembiayaan', 'Fasilitas pembiayaan modal kerja tanpa agunan dengan suku bunga harian 0.05% dan provisi 0.5%.');

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
                        'Agunan: Tanpa Agunan<br>' +
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

        const paymentRequestId = generateSafeId('LOAN-PAY');
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
            const card=node('section','card');
            card.style.cssText = 'background:linear-gradient(135deg, rgba(255,82,82,0.12) 0%, rgba(21,31,58,0.85) 100%); border:1px solid rgba(255,82,82,0.4); border-radius:12px; padding:14px; margin-bottom:14px;';
            card.innerHTML = `
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
                    <span class="eyebrow" style="color:var(--negative); font-weight:700;">⚔️ TANTANGAN RIVAL MINGGUAN</span>
                    <span class="m-tag ${Number(rival.returnDifference) >= 0 ? 'pos' : 'neg'}" style="font-weight:700;">${rival.returnDifference || '0.00%'}</span>
                </div>
                <div style="display:grid; grid-template-columns:1fr auto 1fr; gap:8px; align-items:center; text-align:center; padding:10px 6px; background:rgba(0,0,0,0.3); border-radius:10px; margin-bottom:10px; border:1px solid var(--border);">
                    <div>
                        <small style="color:var(--text-muted); font-size:0.68rem; display:block;">KAMU</small>
                        <strong style="color:var(--gold); font-size:0.95rem; display:block; margin:2px 0;">${rival.playerNetWorth}</strong>
                        <span style="font-size:0.75rem; color:${Number(rival.playerWeeklyReturn) >= 0 ? 'var(--positive)' : 'var(--negative)'}; font-weight:700;">${rival.playerWeeklyReturn}</span>
                    </div>
                    <div style="font-weight:900; font-size:1.15rem; color:var(--text-muted); padding:0 4px;">VS</div>
                    <div>
                        <small style="color:var(--text-muted); font-size:0.68rem; display:block;">${rival.rivalName}</small>
                        <strong style="color:var(--text); font-size:0.95rem; display:block; margin:2px 0;">${rival.rivalNetWorth}</strong>
                        <span style="font-size:0.75rem; color:${Number(rival.rivalWeeklyReturn) >= 0 ? 'var(--positive)' : 'var(--negative)'}; font-weight:700;">${rival.rivalWeeklyReturn}</span>
                    </div>
                </div>
                <div style="font-size:0.74rem; color:var(--text-secondary); line-height:1.45; background:rgba(227,189,127,0.08); border-radius:8px; padding:6px 10px;">
                    🛡️ <b>Gaya Rival:</b> ${rival.rivalStyle} (${rival.rivalTitle})<br>
                    ${rival.summary || 'Kalahkan return mingguan rival untuk naik divisi liga dan merebut hadiah reputasi!'}
                </div>
            `;
            panel.append(card);
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
                    if (entry.avatarPath && !entry.isPlayer) {
                        const img = node('img');
                        img.src = entry.avatarPath;
                        img.style.cssText = 'width:100%; height:100%; border-radius:50%; object-fit:cover;';
                        avatar.append(img);
                    } else {
                        avatar.append(node('span', '', (entry.name || '?').charAt(0)));
                    }
                    avatar.append(node('span', 'podium-rank-badge', `#${r}`));
                    pItem.append(avatar, node('div', 'podium-name', entry.name), node('div', 'podium-score', mode === 'WEEKLY_RETURN' ? (entry.weeklyReturn || entry.weeklyReturnPercent || 'Belum tersedia') : entry.netWorth));
                    if (!entry.isPlayer) {
                        pItem.tabIndex = 0;
                        pItem.setAttribute('role', 'button');
                        pItem.setAttribute('aria-label', `Lihat profil ${entry.name}`);
                        pItem.style.cursor = 'pointer';
                        pItem.onclick = () => openNpcProfileModal(entry);
                        pItem.onkeydown = (e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); pItem.click(); } };
                    }
                    podium.append(pItem);
                });
                panel.append(podium);
            }

            // Quick actions & Paging (LT-26 / LT-27)
            const listContainer = node('div', 'leaderboard-list');
            panel.append(listContainer);

            let visibleCount = Math.min(50, lb.length);
            const renderBatch = () => {
                listContainer.replaceChildren();
                for (let i = 0; i < visibleCount; i++) {
                    const entry = lb[i];
                    const row = node('section', 'data-row');
                    if (entry.isPlayer) row.classList.add('player-row');
                    row.style.cssText = 'display:flex; justify-content:space-between; align-items:center; min-height:48px; padding:8px 12px; border-bottom:1px solid var(--border);';
                    if (!entry.isPlayer) {
                        row.tabIndex = 0;
                        row.setAttribute('role', 'button');
                        row.setAttribute('aria-label', `Lihat profil ${entry.name}`);
                        row.style.cursor = 'pointer';
                        row.onclick = () => openNpcProfileModal(entry);
                        row.onkeydown = (e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); row.click(); } };
                    }

                    const left = node('div');
                    left.style.cssText = 'display:flex; align-items:center; gap:10px;';
                    if (entry.avatarPath && !entry.isPlayer) {
                        const avImg = node('img');
                        avImg.src = entry.avatarPath;
                        avImg.style.cssText = 'width:38px; height:38px; border-radius:50%; object-fit:cover; border:1px solid var(--border); flex-shrink:0;';
                        left.append(avImg);
                    }
                    const identity = node('div');
                    identity.append(node('strong', '', `#${entry.rank} ${entry.name}`), node('small', '', `${entry.title} · ${entry.style}`));
                    left.append(identity);

                    const score = node('strong', '', mode === 'WEEKLY_RETURN' ? (entry.weeklyReturn || entry.weeklyReturnPercent || 'Belum tersedia') : entry.netWorth);
                    score.style.whiteSpace = 'nowrap';
                    row.append(left, score);
                    listContainer.append(row);
                }
            };
            renderBatch();

            if (lb.length > 50) {
                const navRow = node('div', 'flex-row');
                navRow.style.cssText = 'margin:12px 0; gap:8px; display:flex; flex-wrap:wrap;';
                if (visibleCount < lb.length) {
                    const loadMore = flowButton(navRow, `Muat 50 peserta berikutnya (${visibleCount}/${lb.length})`, () => {
                        visibleCount = Math.min(visibleCount + 50, lb.length);
                        renderBatch();
                    });
                    loadMore.style.minHeight = '48px';
                }
                const playerRank = data.playerRank;
                if (playerRank && playerRank > visibleCount) {
                    const jumpBtn = flowButton(navRow, `Lompat ke Peringkat Saya (#${playerRank})`, () => {
                        visibleCount = Math.min(lb.length, Math.ceil(playerRank / 50) * 50);
                        renderBatch();
                        setTimeout(() => {
                            const playerEl = listContainer.querySelector('.player-row');
                            if (playerEl) playerEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
                        }, 50);
                    });
                    jumpBtn.style.minHeight = '48px';
                }
                panel.append(navRow);
            }

            flowButton(panel, 'Riwayat hasil mingguan', openPerformanceReport);
        } catch(error) { flowText(panel, 'Liga belum dapat dimuat', error.message); }
    }

    function openNpcProfileModal(entry) {
        let profile = entry;
        if (bridge.getNpcProfile) {
            try {
                const detailed = JSON.parse(bridge.getNpcProfile(entry.id));
                if (detailed && detailed.success) {
                    profile = { ...entry, ...detailed, rank: (detailed.rank > 0 ? detailed.rank : (entry.rank || 1)) };
                }
            } catch (e) {}
        }

        const panel = showFlow(profile.name || 'Profil Investor');

        // Kartu Identitas & Avatar Anime (REV-05 / Opsi B)
        const headerCard = node('section', 'card');
        headerCard.style.cssText = 'display:flex; gap:14px; align-items:center; margin-bottom:14px; background:linear-gradient(135deg, rgba(227,189,127,0.12) 0%, rgba(21,31,58,0.7) 100%); border:1px solid var(--gold); border-radius:12px; padding:14px;';
        const avatarImg = node('img');
        avatarImg.src = profile.avatarPath || `img/avatars/avatar_${String(profile.avatarId || 1).padStart(3, '0')}.webp`;
        avatarImg.style.cssText = 'width:64px; height:64px; border-radius:50%; object-fit:cover; border:2px solid var(--gold); background:var(--surface-raised); flex-shrink:0;';
        avatarImg.onerror = () => { avatarImg.style.display = 'none'; };

        const headInfo = node('div');
        headInfo.style.cssText = 'flex:1; min-width:0;';
        const rankText = profile.netWorthRank != null && profile.weeklyReturnRank != null
            ? `Peringkat #${profile.netWorthRank}`
            : `#${profile.rank || 1} di Leaderboard`;
        headInfo.innerHTML = `
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:2px;">
                <span class="m-tag pos" style="font-size:0.68rem; font-weight:700;">${profile.title || 'Taipan Pasar'}</span>
                <span class="m-tag neutral" style="font-size:0.68rem;">${rankText}</span>
            </div>
            <h2 style="font-size:1.2rem; color:var(--gold); font-weight:700; margin:4px 0 2px;">${profile.name}</h2>
            <div style="font-size:0.75rem; color:var(--text-secondary);">Gaya: <b>${profile.style || 'SEIMBANG'}</b> · Return: <b style="color:var(--positive);">${profile.weeklyReturn || '+0.00%'}</b></div>
        `;
        headerCard.append(avatarImg, headInfo);
        panel.append(headerCard);

        // 4 Tabs: Sub-menu Pill Segmen Langsung Terlihat (Comment 6)
        const tabContainer = node('div', 'subtab-pills-bar');
        tabContainer.style.cssText = 'display:flex; gap:6px; background:rgba(0,0,0,0.4); padding:4px; border-radius:12px; border:1px solid var(--border); margin-bottom:14px;';
        const tabs = ['Ringkasan', 'Saham', 'Bisnis', 'Riwayat'];
        let activeTab = 'Ringkasan';
        const contentArea = node('div', 'tab-content');

        const renderTab = (tabName) => {
            activeTab = tabName;
            tabContainer.querySelectorAll('button').forEach(btn => {
                const isCur = btn.dataset.tab === activeTab;
                btn.className = 'subtab-pill' + (isCur ? ' active' : '');
                btn.style.cssText = 'flex:1; padding:8px 0; font-size:0.8rem; font-weight:700; text-align:center; min-height:38px; border-radius:9px;';
            });
            contentArea.replaceChildren();

            if (tabName === 'Ringkasan') {
                const kpi = node('section', 'card');
                metricCard(kpi, 'KEKAYAAN BERSIH', profile.netWorth || 'Belum tersedia', profile.weeklyReturn ? `${profile.weeklyReturn} minggu berjalan` : 'Basis modal awal');

                const splitGrid = node('div', 'stats-grid');
                splitGrid.style.cssText = 'display:grid; grid-template-columns:1fr 1fr; gap:10px; margin-top:10px;';
                const c1 = node('div', 'card'); c1.innerHTML = `<small style="color:var(--text-muted);">Saldo Kas Likuid</small><br><strong style="white-space:nowrap;">${profile.cash || 'Belum tersedia'}</strong>`;
                const c2 = node('div', 'card'); c2.innerHTML = `<small style="color:var(--text-muted);">Valuasi Saham</small><br><strong style="white-space:nowrap;">${profile.stockValuation || 'Rp 0'}</strong>`;
                const c3 = node('div', 'card'); c3.innerHTML = `<small style="color:var(--text-muted);">Gaya Trading</small><br><strong>${profile.style || 'SEIMBANG'}</strong>`;
                const c4 = node('div', 'card'); c4.innerHTML = `<small style="color:var(--text-muted);">Peringkat Liga</small><br><strong style="white-space:nowrap;">#${profile.netWorthRank || profile.rank || 1} Kekayaan · #${profile.weeklyReturnRank || 1} Return</strong>`;
                splitGrid.append(c1, c2, c3, c4);
                contentArea.append(kpi, splitGrid);
            } else if (tabName === 'Saham') {
                const sCard = node('section', 'card');
                const positions = profile.positions || [];
                if (!positions.length) {
                    sCard.append(node('h3', 'card-title', 'Portofolio Saham NPC'));
                    emptyState(sCard, 'Portofolio Belum Terbuka', 'Saat ini memegang 100% kas likuid atau belum mengakumulasi saham di bursa.');
                } else {
                    // 1. Hitung Total Nilai Portofolio & Kelompokkan Sektor untuk Pie Chart (Comment 1)
                    let totalPortVal = 0;
                    const sectorMap = {};
                    positions.forEach(p => {
                        let val = Number(p.marketValueRaw || 0);
                        if (!val && p.marketValue) {
                            val = Number(p.marketValue.replace(/[^0-9]/g, '')) || 0;
                        }
                        if (!val) {
                            val = Number(p.quantity || 0) * Number(p.averageCost || 0);
                        }
                        p._numericVal = val;
                        totalPortVal += val;

                        let sec = p.sector || 'Lainnya';
                        if ((sec === 'Lainnya' || !sec) && p.ticker) {
                            if (p.ticker.startsWith('BB') || p.ticker === 'BMRI' || p.ticker === 'ARTO') sec = 'Keuangan';
                            else if (p.ticker === 'ADRO' || p.ticker === 'PGAS' || p.ticker === 'ITMG') sec = 'Energi';
                            else if (p.ticker === 'ANTM' || p.ticker === 'INCO') sec = 'Bahan Baku';
                            else if (p.ticker === 'TLKM' || p.ticker === 'EXCL') sec = 'Infrastruktur';
                            else if (p.ticker === 'UNVR' || p.ticker === 'ICBP' || p.ticker === 'AMRT') sec = 'Konsumen';
                            else if (p.ticker === 'AAPL' || p.ticker === 'MSFT' || p.ticker === 'NVDA') sec = 'Teknologi';
                        }
                        p._sec = sec;
                        sectorMap[sec] = (sectorMap[sec] || 0) + val;
                    });
                    if (totalPortVal <= 0) totalPortVal = 1;

                    // Header & Pie Chart Sektor
                    sCard.innerHTML = `<h3 class="card-title" style="margin-bottom:8px;">Alokasi Sektor Portofolio</h3>`;

                    const secColors = {
                        'Keuangan': '#E3BD7F',
                        'Energi': '#F59E0B',
                        'Konsumen': '#14F195',
                        'Teknologi': '#3B82F6',
                        'Bahan Baku': '#C084FC',
                        'Infrastruktur': '#00E5FF',
                        'Kesehatan': '#EC4899',
                        'Lainnya': '#9E8E75'
                    };
                    const fallbackPalette = ['#E3BD7F', '#14F195', '#3B82F6', '#F59E0B', '#C084FC', '#00E5FF', '#EC4899'];
                    
                    let curDeg = 0;
                    const gradientParts = [];
                    const legendItems = [];
                    let colorIdx = 0;

                    // Sort sectors by percentage descending and filter out 0%
                    const sortedSectors = Object.entries(sectorMap)
                        .map(([sName, sVal]) => [sName, (sVal / totalPortVal) * 100])
                        .filter(([_, sPct]) => sPct > 0.05)
                        .sort((a, b) => b[1] - a[1]);

                    if (!sortedSectors.length) {
                        sortedSectors.push(['Belum Ada Saham', 100]);
                    }

                    for (const [sName, sPct] of sortedSectors) {
                        const sDeg = (sPct / 100) * 360;
                        const col = secColors[sName] || fallbackPalette[colorIdx % fallbackPalette.length];
                        colorIdx++;
                        gradientParts.push(`${col} ${curDeg.toFixed(1)}deg ${(curDeg + sDeg).toFixed(1)}deg`);
                        curDeg += sDeg;
                        legendItems.push(`<li style="display:flex; justify-content:space-between; align-items:center; margin-bottom:4px;"><span style="display:inline-flex; align-items:center; gap:6px;"><i style="background:${col}; width:10px; height:10px; border-radius:3px; display:inline-block;"></i>${sName}</span><strong style="color:#FFF;">${sPct.toFixed(1)}%</strong></li>`);
                    }

                    const pieRow = node('div');
                    pieRow.style.cssText = 'display:flex; align-items:center; gap:16px; background:var(--surface-raised); border-radius:12px; padding:14px 16px; margin-bottom:14px; border:1px solid var(--border);';
                    pieRow.innerHTML = `
                        <div class="pie" style="background:conic-gradient(${gradientParts.join(', ')}); width:90px; height:90px; border-radius:50%; flex-shrink:0; position:relative; box-shadow:0 4px 12px rgba(0,0,0,0.3);">
                            <div style="position:absolute; inset:20px; border-radius:50%; background:var(--surface-raised); display:flex; align-items:center; justify-content:center; font-size:0.7rem; font-weight:700; color:var(--text);">100%</div>
                        </div>
                        <ul style="list-style:none; padding:0; margin:0; font-size:0.8rem; color:var(--text-secondary); line-height:1.5; flex:1;">
                            ${legendItems.join('')}
                        </ul>
                    `;
                    sCard.append(pieRow);

                    // 2. Daftar Kepemilikan Emiten dengan Persentase Bobot & Saham
                    const listHeader = node('h3', 'card-title', 'Daftar Kepemilikan Saham');
                    listHeader.style.marginBottom = '6px';
                    sCard.append(listHeader);

                    positions.forEach(p => {
                        const portPct = ((p._numericVal || 0) / totalPortVal) * 100;
                        const r = node('div');
                        r.style.cssText = 'display:flex; justify-content:space-between; align-items:center; padding:10px 0; border-bottom:1px solid var(--divider); min-height:48px;';
                        r.innerHTML = `
                            <div>
                                <div style="display:flex; align-items:center; gap:6px;">
                                    <strong style="color:#FFF; font-size:0.9rem;">${p.ticker}</strong>
                                    <span class="m-tag neutral" style="font-size:0.62rem;">${p._sec}</span>
                                </div>
                                <small style="color:var(--text-muted); font-size:0.7rem;">${Number(p.quantity).toLocaleString('id-ID')} lembar @ Rp ${Number(p.averageCost || 0).toLocaleString('id-ID')}</small>
                            </div>
                            <div style="text-align:right;">
                                <strong style="color:var(--text); font-size:0.9rem; display:block;">${p.marketValue || ''}</strong>
                                <span class="m-tag pos" style="font-size:0.65rem; font-weight:700;">${portPct.toFixed(1)}% Portofolio</span>
                            </div>
                        `;
                        sCard.append(r);
                    });
                }
                contentArea.append(sCard);
            } else if (tabName === 'Bisnis') {
                const bCard = node('section', 'card');
                bCard.append(node('h3', 'card-title', 'Sektor & Bisnis Utama'));
                flowText(bCard, 'Sektor Utama', profile.title || 'Diversifikasi Industri');
                flowText(bCard, 'Strategi Manajemen', profile.style === 'AGRESIF' ? 'Ekspansi berani dengan alokasi saham tinggi' : profile.style === 'KONSERVATIF' ? 'Fokus stabilitas arus kas & dividen defensif' : 'Keseimbangan pertumbuhan modal dan mitigasi risiko');
                flowText(bCard, 'Status Entitas', 'Fokus portofolio pasar modal. Belum mendirikan entitas bisnis operasional langsung.');
                contentArea.append(bCard);
            } else if (tabName === 'Riwayat') {
                // Desain Ulang Timeline Transaksi Elegan (Comment 2)
                const hCard = node('section', 'card');
                hCard.append(node('h3', 'card-title', 'Aktivitas Transaksi Bursa'));
                const recents = profile.recentTransactions || [];
                if (!recents.length) {
                    emptyState(hCard, 'Belum Ada Transaksi', 'Belum ada transaksi di periode berjalan. Menunggu siklus penyesuaian pasar.');
                } else {
                    const tradeList = node('div');
                    tradeList.style.cssText = 'display:flex; flex-direction:column; gap:8px; margin-top:8px;';

                    recents.forEach(t => {
                        const isFee = t.type === 'TRANSACTION_FEE';
                        if (isFee) return; // Disatukan ke dalam entri trading

                        const isBuy = t.type === 'TRADE_BUY' || (t.description || '').toLowerCase().includes('beli');
                        const card = node('div');
                        card.style.cssText = 'background:var(--surface-raised); border-radius:10px; padding:10px 12px; border:1px solid var(--border); border-left:4px solid ' + (isBuy ? 'var(--positive)' : 'var(--negative)') + ';';

                        card.innerHTML = `
                            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:4px;">
                                <span class="m-tag ${isBuy ? 'pos' : 'neg'}" style="font-weight:700; font-size:0.65rem;">${isBuy ? '▲ ORDER BELI' : '▼ ORDER JUAL'}</span>
                                <strong style="color:var(--text); font-size:0.9rem;">${t.amount}</strong>
                            </div>
                            <strong style="color:#FFF; font-size:0.86rem; display:block;">${t.description}</strong>
                            <small style="color:var(--text-muted); font-size:0.68rem; margin-top:2px; display:block;">Transaksi Saham Internal Bursa · Terekonsiliasi Sah</small>
                        `;
                        tradeList.append(card);
                    });

                    if (!tradeList.children.length) {
                        recents.forEach(t => {
                            const card = node('div');
                            card.style.cssText = 'background:var(--surface-raised); border-radius:10px; padding:10px 12px; border:1px solid var(--border);';
                            card.innerHTML = `
                                <div style="display:flex; justify-content:space-between; align-items:center;">
                                    <strong style="font-size:0.84rem; color:#FFF;">${t.description}</strong>
                                    <strong style="color:var(--gold); font-size:0.86rem;">${t.amount}</strong>
                                </div>
                            `;
                            tradeList.append(card);
                        });
                    }
                    hCard.append(tradeList);
                }
                contentArea.append(hCard);
            }
        };

        tabs.forEach(tab => {
            const btn = node('button', tab === activeTab ? 'btn-primary' : 'btn-secondary', tab);
            btn.dataset.tab = tab;
            btn.onclick = () => renderTab(tab);
            tabContainer.append(btn);
        });

        panel.append(tabContainer, contentArea);
        renderTab('Ringkasan');
    }

    function openMarketBossModal() {
        const panel = showFlow('Duel Bos Taipan Pasar');
        flowText(panel, 'Tantangan Bos', 'Kalahkan return 4 Bos Taipan untuk meraih hadiah modal tunai dan gelar prestisius.');

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
                        <span class="m-tag" style="background:${color}22; color:${color}; border:1px solid ${color}44; font-weight:700;">${b.tierTitle || b.tier}</span>
                        <span class="m-tag ${b.isDefeated ? 'pos' : b.isActive ? 'warn' : 'neutral'}" style="font-weight:700;">${b.statusLabel}</span>
                    </div>
                    <h3 style="font-size:1.2rem; color:#FFF; font-weight:700; margin:0 0 8px;">${b.name}</h3>
                    
                    <div style="display:grid; grid-template-columns:1fr auto 1fr; gap:6px; align-items:center; text-align:center; padding:8px 6px; background:rgba(0,0,0,0.3); border-radius:10px; margin-bottom:10px; border:1px solid var(--border);">
                        <div>
                            <small style="color:var(--text-muted); font-size:0.68rem; display:block;">CUAN KAMU</small>
                            <strong style="color:var(--positive); font-size:1rem; display:block; margin:2px 0;">${b.playerCurrentReturn}</strong>
                        </div>
                        <div style="font-weight:900; font-size:1rem; color:var(--text-muted);">VS</div>
                        <div>
                            <small style="color:var(--text-muted); font-size:0.68rem; display:block;">CUAN BOS</small>
                            <strong style="color:${color}; font-size:1rem; display:block; margin:2px 0;">${b.bossCurrentReturn}</strong>
                        </div>
                    </div>

                    <div style="display:flex; justify-content:space-between; font-size:0.75rem; color:var(--text-secondary); margin-bottom:6px;">
                        <span>Target Menang: <b style="color:${color};">${b.targetReturn}</b></span>
                        <span>Durasi: <b>${b.durationSessions} Hari Bursa</b></span>
                    </div>
                    <div style="font-size:0.75rem; color:var(--gold); background:rgba(227,189,127,0.1); border-radius:8px; padding:6px 10px;">
                        🎁 <b>Hadiah Kemenangan:</b> ${b.rewardCash}
                    </div>
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
        flowText(panel, 'Status Kepatuhan', 'Evaluasi rasio utang & kelayakan diskon pajak dividen.');

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
        flowText(panel, 'Status Proteksi', 'Lindungi portofolio dari anomali crash pasar (klaim otomatis cair jika indeks anjlok > 5%).');

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

            <div id="alloc-bar-container" style="margin-top:14px; padding:12px; background:rgba(255,255,255,0.03); border-radius:12px; border:1px solid rgba(255,255,255,0.08);">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
                    <span style="font-size:0.8rem; color:var(--text-muted);">DIAGRAM ALOKASI PORTOFOLIO</span>
                    <strong id="alloc-total-label" style="font-size:0.85rem; color:var(--teal);">Total: 100%</strong>
                </div>
                <div id="alloc-preview-bar" style="display:flex; height:20px; border-radius:8px; overflow:hidden;">
                    <span id="bar-eq" style="background:var(--teal); width:40%;" title="Saham 40%"></span>
                    <span id="bar-comm" style="background:var(--gold); width:30%;" title="Komoditas 30%"></span>
                    <span id="bar-crypto" style="background:#9945FF; width:20%;" title="Kripto 20%"></span>
                    <span id="bar-cash" style="background:#71819A; width:10%;" title="Kas 10%"></span>
                </div>
                <div id="alloc-legend" style="display:flex; justify-content:space-between; font-size:0.68rem; color:var(--text-muted); margin-top:6px;">
                    <span id="lbl-eq" style="color:var(--teal);">■ Saham 40%</span>
                    <span id="lbl-comm" style="color:var(--gold);">■ Emas 30%</span>
                    <span id="lbl-crypto" style="color:#C4A1FF;">■ Kripto 20%</span>
                    <span id="lbl-cash" style="color:#A0AEC0;">■ Kas 10%</span>
                </div>
            </div>

            <button id="btn-balance-cash" type="button" class="btn-secondary" style="margin-top:8px; width:100%; min-height:40px; font-size:0.8rem;">⚖️ Seimbangkan Otomatis ke Kas</button>
            <button id="btn-create-custom-fund" class="btn-primary" style="margin-top:10px;">Terbitkan Reksa Dana Kustom</button>
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
            const inEq = document.getElementById('alloc-eq');
            const inComm = document.getElementById('alloc-comm');
            const inCrypto = document.getElementById('alloc-crypto');
            const inCash = document.getElementById('alloc-cash');
            const totalLbl = document.getElementById('alloc-total-label');
            const barEq = document.getElementById('bar-eq');
            const barComm = document.getElementById('bar-comm');
            const barCrypto = document.getElementById('bar-crypto');
            const barCash = document.getElementById('bar-cash');
            const lblEq = document.getElementById('lbl-eq');
            const lblComm = document.getElementById('lbl-comm');
            const lblCrypto = document.getElementById('lbl-crypto');
            const lblCash = document.getElementById('lbl-cash');
            const btnBalance = document.getElementById('btn-balance-cash');

            const updateAllocUI = () => {
                const eq = Math.max(0, Number(inEq.value) || 0);
                const comm = Math.max(0, Number(inComm.value) || 0);
                const crypto = Math.max(0, Number(inCrypto.value) || 0);
                const cash = Math.max(0, Number(inCash.value) || 0);
                const total = eq + comm + crypto + cash;
                
                if (totalLbl) {
                    totalLbl.textContent = `Total: ${total}%`;
                    totalLbl.style.color = (Math.abs(total - 100) < 0.1) ? 'var(--teal)' : 'var(--negative)';
                }
                const base = total > 0 ? total : 100;
                if (barEq) barEq.style.width = `${(eq / base) * 100}%`;
                if (barComm) barComm.style.width = `${(comm / base) * 100}%`;
                if (barCrypto) barCrypto.style.width = `${(crypto / base) * 100}%`;
                if (barCash) barCash.style.width = `${(cash / base) * 100}%`;
                if (lblEq) lblEq.textContent = `■ Saham ${eq}%`;
                if (lblComm) lblComm.textContent = `■ Emas ${comm}%`;
                if (lblCrypto) lblCrypto.textContent = `■ Kripto ${crypto}%`;
                if (lblCash) lblCash.textContent = `■ Kas ${cash}%`;
            };

            [inEq, inComm, inCrypto, inCash].forEach(inp => {
                if (inp) inp.addEventListener('input', updateAllocUI);
            });

            if (btnBalance) {
                btnBalance.onclick = () => {
                    const eq = Math.max(0, Number(inEq.value) || 0);
                    const comm = Math.max(0, Number(inComm.value) || 0);
                    const crypto = Math.max(0, Number(inCrypto.value) || 0);
                    const neededCash = Math.max(0, 100 - (eq + comm + crypto));
                    inCash.value = neededCash;
                    updateAllocUI();
                };
            }

            const btn = document.getElementById('btn-create-custom-fund');
            if (btn) {
                btn.onclick = () => {
                    const name = document.getElementById('fund-name-input').value.trim();
                    const eq = Number(document.getElementById('alloc-eq').value) || 0;
                    const comm = Number(document.getElementById('alloc-comm').value) || 0;
                    const crypto = Number(document.getElementById('alloc-crypto').value) || 0;
                    const cash = Number(document.getElementById('alloc-cash').value) || 0;
                    if (Math.abs(eq + comm + crypto + cash - 100) > 0.1) {
                        alert('Total alokasi harus persis 100%! Gunakan tombol Seimbangkan Otomatis ke Kas.');
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

    function openAvatarPickerModal() {
        const panel = showFlow('Pilih Avatar Profil');
        flowText(panel, 'Koleksi Avatar Investor', 'Pilih ikon avatar yang mewakili karakter Anda di bursa saham.');

        const grid = document.createElement('div');
        grid.style.cssText = 'display:grid; grid-template-columns:repeat(3, 1fr); gap:10px; margin-top:12px;';

        const avatars = [
            { id: 'AVATAR-001', name: 'Trader Muda', icon: '🧑‍💼', file: 'img/avatars/avatar_001.webp' },
            { id: 'AVATAR-002', name: 'Analis Riset', icon: '👩‍💼', file: 'img/avatars/avatar_002.webp' },
            { id: 'AVATAR-003', name: 'Eksekutif Top', icon: '👨‍💼', file: 'img/avatars/avatar_003.webp' },
            { id: 'AVATAR-004', name: 'Taipan Saham', icon: '👑', file: 'img/avatars/avatar_004.webp' },
            { id: 'AVATAR-005', name: 'Algo Trader', icon: '⚡', file: 'img/avatars/avatar_005.webp' },
            { id: 'AVATAR-006', name: 'Manajer Dana', icon: '💼', file: 'img/avatars/avatar_006.webp' },
            { id: 'AVATAR-007', name: 'Konglomerat', icon: '🏛️', file: 'img/avatars/avatar_007.webp' },
            { id: 'AVATAR-008', name: 'Sultan Emas', icon: '💎', file: 'img/avatars/avatar_008.webp' },
            { id: 'AVATAR-009', name: 'Banker Senior', icon: '🏦', file: 'img/avatars/avatar_009.webp' }
        ];

        let curEquipped = 'AVATAR-001';
        try {
            if (bridge.getPlayerProfile) {
                const prof = JSON.parse(bridge.getPlayerProfile());
                if (prof.avatarId) curEquipped = 'AVATAR-' + String(prof.avatarId).padStart(3, '0');
            }
        } catch(e) {}

        avatars.forEach(av => {
            const card = document.createElement('div');
            const isCur = av.id === curEquipped;
            card.style.cssText = `background:var(--surface-raised); border:2px solid ${isCur ? 'var(--gold)' : 'var(--border)'}; border-radius:12px; padding:10px 4px; text-align:center; cursor:pointer; display:flex; flex-direction:column; align-items:center; gap:6px; transition:all 0.15s ease;`;

            const img = document.createElement('img');
            img.src = av.file;
            img.style.cssText = 'width:56px; height:56px; border-radius:50%; object-fit:cover; border:2px solid var(--border);';
            img.onerror = () => {
                img.style.display = 'none';
                fallbackIcon.style.display = 'grid';
            };

            const fallbackIcon = document.createElement('div');
            fallbackIcon.style.cssText = 'width:56px; height:56px; border-radius:50%; background:rgba(227,189,127,0.15); display:none; place-items:center; font-size:1.6rem; border:2px solid var(--border);';
            fallbackIcon.textContent = av.icon;

            const name = document.createElement('strong');
            name.style.cssText = 'font-size:0.74rem; color:var(--text); white-space:nowrap; overflow:hidden; text-overflow:ellipsis; width:100%;';
            name.textContent = av.name;

            const badge = document.createElement('span');
            badge.className = 'm-tag ' + (isCur ? 'pos' : 'neutral');
            badge.style.fontSize = '0.62rem';
            badge.textContent = isCur ? '✓ Terpasang' : 'Pilih';

            card.append(img, fallbackIcon, name, badge);

            card.onclick = () => {
                try {
                    if (bridge.equipCosmetic) {
                        bridge.equipCosmetic('AVATAR', av.id);
                    }
                    curEquipped = av.id;
                    showGameToast('success', 'Avatar Terpasang!', `Ikon profil ${av.name} berhasil digunakan.`);
                    refreshSnapshot();
                    openAvatarPickerModal();
                } catch(e) {
                    showGameToast('info', 'Gagal Memasang Avatar', e.message);
                }
            };

            grid.appendChild(card);
        });

        panel.appendChild(grid);
    }

    function openPlayerProfileModal(activeSubtab = 'KINERJA') {
        if (!['KINERJA', 'KEUANGAN', 'PENCAPAIAN', 'PENGATURAN'].includes(activeSubtab)) activeSubtab = 'KINERJA';
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
            headerHero.style.cssText = 'background:var(--surface); border:1px solid var(--border); border-radius:12px; padding:16px; margin-bottom:12px;';

            const avRow = document.createElement('div');
            avRow.style.cssText = 'display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;';
            avRow.innerHTML = `
                <div style="display:flex; align-items:center; gap:12px;">
                    <img src="${p.avatarPath || 'img/avatars/avatar_001.webp'}" onerror="this.src='img/avatars/avatar_001.webp'" style="width:54px; height:54px; border-radius:50%; object-fit:cover; border:2px solid var(--gold); background:var(--surface-raised);">
                    <div>
                        <strong style="font-size:1.05rem; color:#FFF; display:block;">${p.playerName || 'Trader Nusantara'}</strong>
                        <small style="color:var(--text-secondary);">${p.careerRank || 'Novice Trader'} · Level ${p.currentLevel || 1}</small>
                    </div>
                </div>
            `;
            const avBtn = node('button', 'btn-secondary', '🎨 Ganti Avatar');
            avBtn.type = 'button';
            avBtn.style.cssText = 'padding:6px 12px; font-size:0.75rem; font-weight:700; border-radius:8px;';
            avBtn.onclick = () => openAvatarPickerModal();
            avRow.append(avBtn);

            headerHero.append(node('span', 'eyebrow', 'TOTAL KEKAYAAN BERSIH'), node('div', 'metric-hero', fin.personalNetWorth || p.netWorth || 'Belum tersedia'), avRow);
            panel.append(headerHero);

            // 4 Sub-Tabs Navigasi: Kinerja / Keuangan / Pencapaian / Pengaturan & Tema
            const navWrap = document.createElement('div');
            navWrap.className = 'profile-subtabs-nav';
            const subtabs = [
                { id: 'KINERJA', label: 'Kinerja' },
                { id: 'KEUANGAN', label: 'Keuangan' },
                { id: 'PENCAPAIAN', label: 'Pencapaian' },
                { id: 'PENGATURAN', label: 'Pengaturan & Tema' }
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

            } else if (activeSubtab === 'PENGATURAN') {
                // Form Preferensi Tema & Aksesibilitas (Plan 15 B05)
                const themeCard = document.createElement('section');
                themeCard.className = 'card';
                themeCard.innerHTML = `<h2 class="card-title">Pilihan Tema Tycoon Sinematik</h2><p style="font-size:0.8rem; color:var(--text-secondary); margin-bottom:12px;">Pilih tema visual yang akan tersimpan persisten ke profil Anda.</p>`;

                const currentTheme = (p.preferences && p.preferences.theme) ? p.preferences.theme.toUpperCase() : 'EMAS_KLASIK';
                const themeGrid = document.createElement('div');
                themeGrid.style.display = 'grid';
                themeGrid.style.gridTemplateColumns = '1fr 1fr';
                themeGrid.style.gap = '10px';
                themeGrid.style.marginBottom = '14px';

                const btnEmas = document.createElement('button');
                btnEmas.type = 'button';
                btnEmas.className = 'card';
                btnEmas.style.textAlign = 'center';
                btnEmas.style.padding = '12px 10px';
                btnEmas.style.cursor = 'pointer';
                btnEmas.style.borderColor = currentTheme === 'EMAS_KLASIK' ? 'var(--action)' : 'var(--border)';
                btnEmas.style.background = currentTheme === 'EMAS_KLASIK' ? 'var(--surface-raised)' : 'var(--surface)';
                btnEmas.innerHTML = `<strong style="display:block; font-size:0.9rem; color:var(--text);">👑 Emas Klasik</strong><small style="color:var(--text-secondary); font-size:0.72rem;">Dark Bronze & Gold (Default)</small>`;

                const btnNeon = document.createElement('button');
                btnNeon.type = 'button';
                btnNeon.className = 'card';
                btnNeon.style.textAlign = 'center';
                btnNeon.style.padding = '12px 10px';
                btnNeon.style.cursor = 'pointer';
                btnNeon.style.borderColor = currentTheme === 'MIDNIGHT_NEON' ? '#76DBFF' : 'var(--border)';
                btnNeon.style.background = currentTheme === 'MIDNIGHT_NEON' ? 'var(--surface-raised)' : 'var(--surface)';
                btnNeon.innerHTML = `<strong style="display:block; font-size:0.9rem; color:var(--text);">🌌 Midnight Neon</strong><small style="color:var(--text-secondary); font-size:0.72rem;">Deep Navy & Cyber Cyan</small>`;

                const onThemeClick = (themeKey) => {
                    try {
                        const curPrefs = p.preferences || {};
                        const newPrefs = Object.assign({}, curPrefs, { theme: themeKey });
                        const req = { playerName: p.playerName, preferences: newPrefs };
                        bridgeResult('updatePlayerProfile', JSON.stringify(req));
                        applyPlayerPreferences(newPrefs);
                        openPlayerProfileModal('PENGATURAN');
                        showGameToast('success', 'Tema Diperbarui', `Tema visual berhasil dialihkan ke ${themeKey === 'MIDNIGHT_NEON' ? 'Midnight Neon' : 'Emas Klasik'}.`);
                    } catch(err) {
                        showGameToast('warning', 'Gagal Mengubah Tema', err.message);
                    }
                };

                btnEmas.onclick = () => onThemeClick('EMAS_KLASIK');
                btnNeon.onclick = () => onThemeClick('MIDNIGHT_NEON');
                themeGrid.append(btnEmas, btnNeon);
                themeCard.append(themeGrid);
                contentBox.append(themeCard);

                const prefCard = document.createElement('section');
                prefCard.className = 'card';
                prefCard.style.marginTop = '10px';
                prefCard.innerHTML = `<h2 class="card-title">Identitas & Aksesibilitas</h2>`;

                const nameLabel = document.createElement('label');
                nameLabel.textContent = 'Nama trader';
                const nameInput = document.createElement('input');
                nameInput.id = 'player-name'; nameLabel.htmlFor = nameInput.id;
                nameInput.className = 'input-field';
                nameInput.value = p.playerName || '';

                const highContrastLabel = node('label', 'check-row');
                const highContrast = node('input'); highContrast.type = 'checkbox';
                highContrast.checked = Boolean(p.preferences && p.preferences.highContrast);
                highContrastLabel.append(highContrast, node('span', '', 'Kontras tinggi (WCAG AA)'));

                const reducedMotionLabel = node('label', 'check-row');
                const reducedMotion = node('input'); reducedMotion.type = 'checkbox';
                reducedMotion.checked = Boolean(p.preferences && p.preferences.reducedMotion);
                reducedMotionLabel.append(reducedMotion, node('span', '', 'Kurangi gerakan animasi'));

                const isPowerSave = document.documentElement.classList.contains('power-save');
                const powerSaveLabel = node('label', 'check-row');
                const powerSaveCheck = node('input'); powerSaveCheck.type = 'checkbox';
                powerSaveCheck.checked = isPowerSave;
                powerSaveCheck.onchange = () => {
                    const next = powerSaveCheck.checked;
                    document.documentElement.classList.toggle('power-save', next);
                    try { localStorage.setItem('game_power_save', next ? 'true' : 'false'); } catch(e) {}
                };
                powerSaveLabel.append(powerSaveCheck, node('span', '', 'Mode hemat daya / kinerja cepat'));

                const fontNotice = node('p', 'notice', 'Ukuran teks otomatis mengikuti skala teks sistem bawaan perangkat Anda.');
                const profileStatus = node('p'); profileStatus.setAttribute('role', 'status');

                // Simulasi Kemajuan Saat Offline (Adaptasi SyGame: smanger.whiletime)
                const offlineProgressLabel = node('label', 'check-row');
                const offlineProgressCheck = node('input'); offlineProgressCheck.type = 'checkbox';
                offlineProgressCheck.checked = Boolean(p.preferences ? p.preferences.offlineProgressEnabled !== false : true);
                offlineProgressLabel.append(offlineProgressCheck, node('span', '', 'Waktu pasar tetap berjalan saat offline (48× Otomatis)'));
                const offlineNotice = node('p', 'notice', 'Jika dimatikan, seluruh simulasi bursa dan operasional anak usaha membeku (pause) saat game ditutup.');

                const saveBtn = document.createElement('button');
                saveBtn.className = 'btn-primary';
                saveBtn.style.marginTop = '12px';
                saveBtn.textContent = 'Simpan Identitas & Preferensi';

                saveBtn.onclick = () => {
                    try {
                        const curPrefs = p.preferences || {};
                        const req = {
                            playerName: nameInput.value.trim(),
                            preferences: Object.assign({}, curPrefs, {
                                highContrast: highContrast.checked,
                                reducedMotion: reducedMotion.checked,
                                offlineProgressEnabled: offlineProgressCheck.checked
                            })
                        };
                        const res = bridgeResult('updatePlayerProfile', JSON.stringify(req));
                        applyPlayerPreferences(res.preferences || req.preferences);
                        profileStatus.textContent = 'Identitas dan preferensi tersimpan.';
                        refreshSnapshot();
                    } catch (err) {
                        profileStatus.textContent = 'Gagal menyimpan: ' + err.message;
                    }
                };

                prefCard.append(nameLabel, nameInput, highContrastLabel, reducedMotionLabel, powerSaveLabel, offlineProgressLabel, offlineNotice, fontNotice, saveBtn, profileStatus);
                contentBox.append(prefCard);

                // Kontrol Penyimpanan & Checkpoint
                const saveCard = document.createElement('section');
                saveCard.className = 'card';
                saveCard.style.marginTop = '10px';
                saveCard.innerHTML = `<h2 class="card-title">Penyimpanan & Progres Game</h2>`;
                flowButton(saveCard, 'Simpan progres permainan', () => {
                    try {
                        const result = JSON.parse(bridge.saveGame());
                        if (result.success) document.getElementById('native-save-warning')?.remove();
                        showGameToast(result.success ? 'success' : 'warning', result.success ? 'Penyimpanan Berhasil' : 'Save Belum Berhasil', result.message || 'Progres permainan tersimpan di penyimpanan internal perangkat.');
                    } catch (error) { showGameToast('warning', 'Save Gagal', error.message); }
                });
                flowButton(saveCard, 'Muat checkpoint tersimpan', () => {
                    if (!window.confirm('Muat checkpoint terakhir? Perubahan sejak save akan diganti. Berkas asli tidak dihapus.')) return;
                    try {
                        const result = JSON.parse(bridge.loadGame());
                        refreshSnapshot();
                        showGameToast(result.success ? 'success' : 'warning', result.success ? 'Checkpoint Dimuat' : 'Gagal Memuat', result.message || 'Hasil dari domain.');
                    } catch (error) { showGameToast('warning', 'Load Gagal', error.message); }
                });
                flowButton(saveCard, 'Panduan tutorial interaktif', openTutorialModal);
                flowButton(saveCard, 'Menu awal & mulai baru', openEntryPicker);
                contentBox.append(saveCard);
            }

        } catch (e) {
            flowText(panel, 'Gagal memuat profil', e.message);
        }
    }

    function openInvestorArchetypeModal() {
        const panel = showFlow('Tipe & Gaya Trading');
        if (!bridge.getInvestorArchetype) {
            flowText(panel, 'Status', 'Layanan analisis gaya trading belum tersedia.');
            return;
        }

        try {
            const a = JSON.parse(bridge.getInvestorArchetype());

            // 1. Hero Card: Profil & Progres
            const hero = node('section', 'card');
            hero.style.cssText = 'background:linear-gradient(135deg, rgba(153,69,255,0.15) 0%, rgba(21,31,58,0.85) 100%); border:1px solid rgba(153,69,255,0.4); border-radius:12px; padding:16px; margin-bottom:14px;';
            hero.innerHTML = `
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
                    <span class="eyebrow" style="color:#C4A1FF; font-weight:700;">TIPE TRADING ANDA</span>
                    <span class="m-tag pos">${a.confidence || 'Dianalisis'}</span>
                </div>
                <h2 style="font-size:1.35rem; color:#FFF; font-weight:700; margin:2px 0 6px;">${a.label || 'Sedang Terbentuk'}</h2>
                <p style="font-size:0.78rem; color:var(--text-secondary); line-height:1.45; margin-bottom:10px;">
                    ${a.description || 'Gaya investasi dianalisis cerdas dari riwayat transaksi pasar riil Anda.'}
                </p>
                <div style="background:rgba(0,0,0,0.3); border-radius:8px; padding:8px 12px; font-size:0.75rem; color:var(--gold);">
                    📊 <b>Progres Analisis:</b> ${a.progressText || '0/5 transaksi · 2/7 hari bursa'}
                </div>
            `;
            panel.append(hero);

            // 2. 4 Arketipe Trader di Game
            const archetypesCard = node('section', 'card');
            archetypesCard.innerHTML = `<span class="eyebrow">4 TIPE INVESTOR DI PASAR</span><h3 style="font-size:0.95rem; color:var(--text); margin:4px 0 10px;">Pilihan Karakteristik Trading</h3>`;
            const archGrid = node('div');
            archGrid.style.cssText = 'display:grid; grid-template-columns:1fr 1fr; gap:8px;';
            const archs = [
                { icon: '⚡', title: 'Day Trader', desc: 'Cuan cepat dari fluktuasi harian dan momentum tick bursa.' },
                { icon: '💎', title: 'Value Hunter', desc: 'Beli saham diskon murah di bawah nilai wajar fundamental.' },
                { icon: '👑', title: 'Whale Taipan', desc: 'Borong saham dalam jumlah masif hingga supply squeeze.' },
                { icon: '💰', title: 'Dividen Sejati', desc: 'Koleksi saham mapan untuk panen dividen tunai berkala.' }
            ];
            archs.forEach(ar => {
                const b = node('div');
                b.style.cssText = 'background:var(--surface-raised); border-radius:10px; padding:10px; border:1px solid var(--border);';
                b.innerHTML = `<div style="font-size:1.2rem; margin-bottom:4px;">${ar.icon}</div><strong style="display:block; font-size:0.82rem; color:var(--text);">${ar.title}</strong><small style="display:block; font-size:0.68rem; color:var(--text-secondary); margin-top:2px; line-height:1.3;">${ar.desc}</small>`;
                archGrid.append(b);
            });
            archetypesCard.append(archGrid);
            panel.append(archetypesCard);

            // 3. Indikator Perilaku yang Diamati
            const indCard = node('section', 'card');
            indCard.innerHTML = `<span class="eyebrow">INDIKATOR YANG DIPANTAU</span>`;
            const inds = a.observedIndicators || ['Durasi holding saham', 'Frekuensi trading harian', 'Reaksi terhadap sentimen berita', 'Toleransi risiko drawdown'];
            const indList = node('div');
            indList.style.cssText = 'display:flex; flex-wrap:wrap; gap:6px; margin-top:8px;';
            inds.forEach(ind => {
                const pill = node('span', 'm-tag neutral', ind);
                pill.style.cssText = 'font-size:0.7rem; padding:4px 10px;';
                indList.append(pill);
            });
            indCard.append(indList);
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
            const request = {accountId:currentAccount.id, from:from.value, to:from.value === 'IDR' ? 'USD' : 'IDR', amount:amount.value, requestId:generateSafeId('FX-UI')};
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
        inputOrderLimit.value = parseCleanPrice(stock.currentPriceRaw || stock.currentPrice);
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
            accContext.textContent = currentAccount ? `Akun: ${currentAccount.name}` : 'Akun belum tersedia';
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
    let lastBackPressTime = 0;
    window.closeActiveLayer = () => {
        if (orderModal && orderModal.classList.contains('active')) { closeOrderTicket(); return true; }
        if (flowPanel || document.querySelector('.flow-page')) { return closeFlow(); }
        if (currentTab !== 'beranda') {
            switchTab('beranda');
            return true;
        }
        const now = Date.now();
        if (now - lastBackPressTime < 2500) {
            return false; // Izinkan Activity Android memanggil finish()
        }
        lastBackPressTime = now;
        showGameToast('info', 'Keluar Game', 'Tekan sekali lagi untuk keluar dari Game Saham.');
        return true;
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
            `${type === 'STOP_LOSS' || type === 'TAKE_PROFIT' ? 'Harga Pemicu' : 'Harga Limit'} (${selectedInstrument.currency})`;
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
                const maxL = Number(capacity.maxLots) || 0;
                const calcLots = Math.floor(maxL * pct / 100);
                inputOrderQty.value = (maxL >= 1 && calcLots === 0) ? 1 : calcLots;
                document.getElementById('order-error').textContent = Number(inputOrderQty.value) === 0 ? 'Saldo belum mencukupi untuk memesan 1 lot.' : '';
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
            requestId: generateSafeId('REQ-UI')
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

    let searchDebounceTimer = null;
    document.getElementById('search-stock-input').addEventListener('input', () => {
        clearTimeout(searchDebounceTimer);
        searchDebounceTimer = setTimeout(loadStocks, 250);
    });
    document.getElementById('filter-exchange-select').addEventListener('change', loadStocks);
    document.getElementById('filter-sector-select').addEventListener('change', loadStocks);
    document.getElementById('filter-list-select').addEventListener('change', loadStocks);

    // Filter Chips 1-Sentuhan Pasar
    ['all', 'idx', 'us', 'watch'].forEach(k => {
        const chip = document.getElementById('chip-filter-' + k);
        if (chip) {
            chip.onclick = () => {
                document.querySelectorAll('.filter-chips-bar .subtab-pill').forEach(b => b.classList.remove('active'));
                chip.classList.add('active');
                const exSelect = document.getElementById('filter-exchange-select');
                const listSelect = document.getElementById('filter-list-select');
                if (k === 'all') { if (exSelect) exSelect.value = ''; if (listSelect) listSelect.value = 'all'; }
                else if (k === 'idx') { if (exSelect) exSelect.value = 'IDX'; if (listSelect) listSelect.value = 'all'; }
                else if (k === 'us') { if (exSelect) exSelect.value = 'US'; if (listSelect) listSelect.value = 'all'; }
                else if (k === 'watch') { if (exSelect) exSelect.value = ''; if (listSelect) listSelect.value = 'watchlist'; }
                loadStocks();
            };
        }
    });

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
                flowText(detail, 'Batas', 'Data laporan penutupan sesi harian bursa saham.');
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
            flowText(slotSection, 'Mode simulasi', latestSnapshot.gameMode || 'Bursa Saham Utama');
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
        modeInfo.textContent = 'Bursa Saham Utama (Simulasi Terpadu IDX & US)';

        const alertText = document.createElement('p');
        alertText.setAttribute('role', 'alert');

        const startBtn = document.createElement('button');
        startBtn.className = 'btn-primary';
        startBtn.textContent = 'Mulai perjalanan karir baru';
        startBtn.onclick = () => {
            if (!bridge.startNewGame) {
                alertText.textContent = 'Layanan permainan baru belum tersedia.';
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

    function openAbsenceReportModal(report) {
        if (!report || !report.daysAbsent || report.daysAbsent <= 0) return;
        const panel = showFlow('Laporan Ketidakhadiran', true);

        const card = node('section', 'card');
        card.style.cssText = 'background:linear-gradient(135deg, rgba(227,189,127,0.12) 0%, rgba(18,23,40,0.98) 100%); border:1.5px solid var(--border-interactive); padding:18px 16px; border-radius:14px; text-align:center; box-sizing:border-box; width:100%;';

        const pnlPositive = report.pnlPositive !== false && !String(report.pnl).startsWith('-');
        const pnlColor = pnlPositive ? 'var(--positive)' : 'var(--negative)';

        card.innerHTML = `
            <div style="font-size:2.2rem; margin-bottom:6px;">🏛️</div>
            <h2 style="color:#FFF; font-size:1.25rem; margin-bottom:4px;">Laporan Ketidakhadiran Direksi</h2>
            <p style="color:var(--text-secondary); font-size:0.82rem; margin-bottom:14px;">
                Anda telah absen selama <strong>${report.daysAbsent} hari bursa</strong> (${report.realHours || 'beberapa'} jam waktu nyata).
            </p>

            <div style="background:var(--surface-raised); border-radius:10px; padding:12px; margin-bottom:14px; text-align:left;">
                <div class="data-row"><span>Periode Absen</span><strong>${report.startDate} → ${report.endDate}</strong></div>
                <div class="data-row"><span>Kekayaan Sebelum Absen</span><strong>${report.startingNetWorth}</strong></div>
                <div class="data-row"><span>Kekayaan Terkini</span><strong>${report.endingNetWorth}</strong></div>
                <div class="data-row" style="margin-top:6px; padding-top:6px; border-top:1px dashed var(--divider);">
                    <span style="font-weight:700;">Perubahan Nilai Bersih</span>
                    <strong style="color:${pnlColor}; font-size:1.05rem; font-weight:700;">${report.pnl}</strong>
                </div>
            </div>

            <div style="background:rgba(0,0,0,0.25); border-radius:8px; padding:10px 12px; font-size:0.78rem; color:var(--text-secondary); text-align:left; margin-bottom:14px;">
                • Kas likuid saat ini: <strong style="color:var(--text);">${report.cashAvailable || 'Tersedia'}</strong><br>
                • Transaksi order pasar dan fluktuasi harga telah diselaraskan.<br>
                • Seluruh dividen dan laba anak usaha telah otomatis terkredit.
            </div>
        `;

        const btnRow = node('div');
        btnRow.style.cssText = 'display:flex; gap:10px; flex-direction:column;';
        
        const marketBtn = flowButton(btnRow, 'Buka Pasar & Investasikan Laba ↗', () => {
            if (bridge.dismissAbsenceReport) {
                try { bridge.dismissAbsenceReport(); } catch(e) {}
            }
            closeFlow();
            refreshSnapshot();
            switchTab('pasar');
        });

        flowButton(btnRow, 'Lanjutkan ke Beranda ➔', () => {
            if (bridge.dismissAbsenceReport) {
                try { bridge.dismissAbsenceReport(); } catch(e) {}
            }
            closeFlow();
            refreshSnapshot();
            loadHomeMarket();
        });

        card.append(btnRow);
        panel.append(card);
    }

    function checkAndShowAbsenceReport() {
        if (!bridge.getAbsenceReport) return;
        try {
            const raw = bridge.getAbsenceReport();
            if (!raw) return;
            const rep = JSON.parse(raw);
            if (rep && rep.hasReport && !rep.silent && Number(rep.daysAbsent) > 0) {
                setTimeout(() => openAbsenceReportModal(rep), 300);
            }
        } catch(e) {}
    }

    function openWelcomeModal() {
        const panel = showFlow('Selamat Datang di Game Saham');
        const hero = node('section', 'card');
        hero.style.cssText = 'background:linear-gradient(135deg,#2A2117 0%,#171513 100%); border:1.5px solid rgba(212,175,55,0.4); text-align:center; padding:20px;';
        hero.innerHTML = `
            <div style="font-size:2.2rem; margin-bottom:8px;">🏛️</div>
            <h2 style="color:#FFD700; margin:0 0 6px 0; font-size:1.3rem;">Selamat Datang, Taipan Saham!</h2>
            <p style="color:var(--text-secondary); font-size:0.88rem; margin:0 0 16px 0;">Perjalananmu membangun imperium finansial dimulai hari ini.</p>
            <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px; text-align:left; margin-bottom:16px;">
                <div style="background:rgba(0,0,0,0.35); padding:10px; border-radius:8px;">
                    <small style="color:var(--text-muted); font-weight:700;">MODAL AWAL</small><br>
                    <strong style="color:#10B981; font-size:1rem;">Rp 50.000.000</strong>
                </div>
                <div style="background:rgba(0,0,0,0.35); padding:10px; border-radius:8px;">
                    <small style="color:var(--text-muted); font-weight:700;">WAKTU GAME</small><br>
                    <strong style="color:#FFDF80; font-size:1rem;">24× Otomatis</strong>
                </div>
            </div>
            <p style="font-size:0.82rem; color:var(--text-muted); text-align:left; line-height:1.4; margin:0 0 16px 0;">
                • 1 hari game berlangsung sekitar 1 jam waktu nyata.<br>
                • Bursa IDX buka <strong>24 jam</strong> setiap hari game.<br>
                • Waktu dan transaksi tetap berjalan dan direkonsiliasi saat game ditutup.
            </p>
        `;
        flowButton(hero, 'Mulai Menjelajahi Pasar ➔', () => {
            try { localStorage.setItem('game_welcome_shown', 'true'); } catch(e) {}
            closeFlow();
            switchTab('pasar');
        });
        panel.append(hero);
    }

    function openInteractiveTutorialModal() {
        const panel = showFlow('Panduan tutorial interaktif');
        flowText(panel, 'Pengantar', 'Pelajari alur trading dan karier. Hasil transaksi bergantung pada harga, likuiditas, biaya dan keputusanmu.');

        const steps = [
            {
                step: '1. Dunia game berjalan otomatis 24×',
                desc: 'Waktu, pasar, dan bisnis terus berkembang 24× (1 hari game berlangsung sekitar 1 jam nyata), baik saat aplikasi dibuka maupun saat ditutup. Bursa IDX buka 24 jam setiap hari game dan seluruh hasil perdagangan otomatis direkonsiliasi saat kamu kembali.',
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
                step: '5. Penutupan Hari Otomatis & Rekapitulasi Karir',
                desc: 'Bursa IDX buka 24 jam setiap hari game. Hari pasar dan mutasi perdagangan bergulir secara otomatis 24× tanpa perlu penutupan manual; rekapitulasi gaji, bunga harian, dan evaluasi karir langsung diproses otomatis saat pergantian hari.',
                action: 'Lihat Evaluasi Karir & Misi',
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
        const panel = showFlow('Kartu Booster Trading');

        if (!bridge.getResearchBoosters) {
            flowText(panel, 'Status', 'Layanan kartu booster belum tersedia.');
            return;
        }

        try {
            const boosters = JSON.parse(bridge.getResearchBoosters());
            boosters.forEach(b => {
                const card = document.createElement('section');
                card.className = 'card booster-card';
                card.style.cssText = 'margin-bottom:12px; border:1px solid rgba(0, 229, 255, 0.35); background:linear-gradient(135deg, rgba(0, 229, 255, 0.08) 0%, rgba(18, 23, 40, 0.95) 100%); border-radius:12px; padding:14px;';

                card.innerHTML = `
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
                        <span class="m-tag neutral" style="font-size:0.68rem; font-weight:700;">${b.type}</span>
                        <span class="m-tag ${b.isActive ? 'pos' : 'neutral'}" style="font-size:0.68rem; font-weight:700;">${b.isActive ? '⚡ AKTIF' : 'TERSEDIA'}</span>
                    </div>
                    <h3 style="font-size:1.05rem; color:#FFF; font-weight:700; margin:0 0 6px;">${b.title}</h3>
                    <p style="font-size:0.78rem; color:var(--text-secondary); margin:0 0 8px; line-height:1.4;">${b.description}</p>
                    <div style="font-size:0.74rem; color:var(--gold);">Sisa Penggunaan: <b>${b.remainingUses} kali pakai</b></div>
                `;

                if (!b.isActive && b.remainingUses > 0) {
                    const actBtn = document.createElement('button');
                    actBtn.className = 'btn-primary';
                    actBtn.style.cssText = 'width:100%; min-height:40px; margin-top:10px; font-weight:700; font-size:0.8rem;';
                    actBtn.textContent = 'Gunakan Booster Sekarang';
                    actBtn.onclick = () => {
                        try {
                            const res = JSON.parse(bridge.activateResearchBooster(b.boosterId));
                            alert(`Booster ${b.title} berhasil diaktifkan!`);
                            refreshSnapshot();
                            openResearchBoostersModal();
                        } catch(err) { alert('Gagal mengaktifkan booster: ' + err.message); }
                    };
                    card.appendChild(actBtn);
                } else if (b.isActive) {
                    const activeP = document.createElement('p');
                    activeP.style.cssText = 'color:var(--positive); font-weight:700; font-size:0.78rem; margin-top:8px;';
                    activeP.textContent = '⚡ Booster sedang aktif pada sesi pasar ini!';
                    card.appendChild(activeP);
                }

                panel.appendChild(card);
            });
        } catch(e) {
            flowText(panel, 'Galat', e.message);
        }
    }

    function openTechTreeModal() {
        const panel = showFlow('Teknologi & Automasi Trading');

        if (!bridge.getTechTree) {
            flowText(panel, 'Status', 'Layanan teknologi trading belum tersedia.');
            return;
        }

        try {
            const nodes = JSON.parse(bridge.getTechTree());
            nodes.forEach(n => {
                const card = document.createElement('section');
                card.className = 'card tech-node-card';
                card.style.cssText = 'margin-bottom:12px; border:1px solid rgba(153, 69, 255, 0.35); background:linear-gradient(135deg, rgba(153, 69, 255, 0.08) 0%, rgba(18, 23, 40, 0.95) 100%); border-radius:12px; padding:14px;';

                card.innerHTML = `
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                        <span class="m-tag neutral" style="font-size:0.68rem; font-weight:700;">TIER ${n.tierLevel} · ${n.category}</span>
                        <span class="m-tag ${n.isUnlocked ? 'pos' : 'neutral'}" style="font-weight:700;">${n.isUnlocked ? '✓ TERBUKA' : 'TERKUNCI'}</span>
                    </div>
                    <h3 style="font-size:1.05rem; color:#FFF; font-weight:700; margin:0 0 6px;">${n.nodeName}</h3>
                    <div style="background:rgba(20,241,149,0.1); border:1px solid rgba(20,241,149,0.25); border-radius:8px; padding:6px 10px; margin-bottom:8px; font-size:0.75rem; color:var(--positive);">
                        ✨ <b>Efek:</b> +${n.statBonusValue}% (${n.statBonusType})
                    </div>
                    <div style="font-size:0.78rem; color:var(--text-secondary);">Biaya Riset: <b style="color:var(--gold);">${n.upgradeCost}</b></div>
                `;

                if (!n.isUnlocked) {
                    const upBtn = document.createElement('button');
                    upBtn.className = 'btn-primary';
                    upBtn.style.cssText = 'width:100%; min-height:40px; margin-top:10px; font-weight:700; font-size:0.8rem;';
                    upBtn.textContent = 'Buka Teknologi Ini (' + n.upgradeCost + ')';
                    upBtn.onclick = () => {
                        try {
                            const res = JSON.parse(bridge.upgradeTechNode(n.nodeId));
                            alert(`Teknologi ${res.nodeName} berhasil diaktifkan!`);
                            refreshSnapshot();
                            openTechTreeModal();
                        } catch(err) { alert('Gagal: ' + err.message); }
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
        const panel = showFlow('Bursa Rekrutmen Eksekutif');
        flowText(panel, 'Rekrutmen Eksekutif', 'Rekrut manajer investasi bintang untuk mendongkrak return bisnis Anda.');

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
        const panel = showFlow('Disiplin Harian & Bonus');
        flowText(panel, 'Bonus Kehadiran', 'Masuk dan bertransaksi setiap sesi bursa untuk bonus modal harian hingga 2x lipat.');

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
        const panel = showFlow('Dominasi Sektor Industri');
        flowText(panel, 'Dominasi Industri', 'Kuasai >25% saham satu sektor untuk diskon komisi 25% dan hak veto.');

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
        openPlayerProfileModal('KINERJA');
    });

    document.getElementById('btn-home-market').addEventListener('click', () => switchTab('pasar'));
    const bNav = document.querySelector('.bottom-nav');
    if (bNav) {
        const h = bNav.getBoundingClientRect().height;
        if (h > 0) document.documentElement.style.setProperty('--nav-height', h + 'px');
    }
    let isUserScrolling = false;
    let scrollSettledTimer = null;
    const scrollContainer = document.querySelector('.app-scroll');
    if (scrollContainer) {
        scrollContainer.addEventListener('scroll', () => {
            isUserScrolling = true;
            clearTimeout(scrollSettledTimer);
            scrollSettledTimer = setTimeout(() => { isUserScrolling = false; }, 150);
        }, { passive: true });
    }
    document.addEventListener('keydown', event => {
        if (event.key === 'Escape' && !orderModal.classList.contains('active') && flowPanel) { event.preventDefault(); closeFlow(); }
    });
    // Initial Load & Power Save preference restore
    try {
        if (localStorage.getItem('game_power_save') === 'true') {
            document.documentElement.classList.add('power-save');
        }
    } catch(e) {}
    // Instant boot: tampilkan cached snapshot terakhir agar UI langsung terisi, baru sync di belakang
    try {
        const cached = localStorage.getItem('game_last_snapshot');
        if (cached) {
            const snap = JSON.parse(cached);
            if (snap && snap.personalAccount) refreshSnapshot(snap);
        }
    } catch(e) {}
    if (bridge.beginClockSync) requestClockSync(!document.hidden).then((snap) => { refreshSnapshot(snap); loadHomeMarket(); }).catch(() => {});
    else { if (bridge.onAppLifecycleChanged) bridge.onAppLifecycleChanged(!document.hidden); refreshSnapshot(); loadHomeMarket(); }
    try {
        const initTab = new URLSearchParams(window.location.search).get('tab') || window.location.hash.replace('#', '');
        if (initTab) switchTab(initTab);
        const urlParams = new URLSearchParams(window.location.search);
        const subtabParam = urlParams.get('subtab');
        if (subtabParam) {
            setPasarPortoSubtab(subtabParam === 'porto' || subtabParam === 'portofolio' ? 'porto' : 'pasar');
        }
        const dismissAbsence = urlParams.get('dismiss_absence');
        if (dismissAbsence && bridge.dismissAbsenceReport) {
            try { bridge.dismissAbsenceReport(); } catch(e) {}
        }
        const openProfileParam = urlParams.get('open_profile');
        if (openProfileParam) {
            setTimeout(() => openPlayerProfileModal(openProfileParam), 400);
        }
        const welcomeShown = localStorage.getItem('game_welcome_shown') || urlParams.get('no_welcome');
        if (!welcomeShown) {
            setTimeout(openWelcomeModal, 400);
        } else {
            setTimeout(checkAndShowAbsenceReport, 600);
        }
        const stockParam = urlParams.get('stock');
        if (stockParam && bridge.getInstruments) {
            setTimeout(() => {
                try {
                    const stocks = JSON.parse(bridge.getInstruments('', '', null, true));
                    const target = stocks.find(s => s.id === stockParam || s.ticker === stockParam);
                    if (target) {
                        openInstrumentDetail(target);
                        const fundTabParam = urlParams.get('fund_tab');
                        if (fundTabParam) {
                            setTimeout(() => {
                                const btn = document.querySelector(`button[data-tab-id="${fundTabParam}"]`);
                                if (btn) btn.click();
                            }, 300);
                        }
                    }
                } catch(e) {
                    console.error('Failed to open stock detail from param:', e);
                }
            }, 600);
        }
        const openFlowParam = urlParams.get('open_flow');
        if (openFlowParam === 'ipo') {
            setTimeout(openIpoCenter, 500);
        }
    } catch(e) {}

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
                requestClockSync(true).then((snap) => {
                    refreshSnapshot(snap);
                    loadHomeMarket();
                    checkAndShowAbsenceReport();
                }).catch(() => {});
                return;
            }
            if (bridge && bridge.onAppLifecycleChanged) bridge.onAppLifecycleChanged(true);
            refreshSnapshot();
            checkAndShowAbsenceReport();
            if (['beranda', 'pasar', 'pasar_porto'].includes(currentTab)) updateMarketQuotes();
            if (['aset', 'pasar_porto'].includes(currentTab)) syncPortfolioLive();
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
            if (document.hidden || clockSyncRunning) return;
            if (bridge.beginClockSync) {
                requestClockSync(true).then((snap) => {
                    document.getElementById('clock-error')?.remove();
                    if (!isUserScrolling) {
                        refreshSnapshot(snap);
                        if (currentTab === 'aset' || (currentTab === 'pasar_porto' && currentPasarPortoSubtab === 'porto')) syncPortfolioLive();
                        if (activeReceiptRequestId) updateReceiptLive(activeReceiptRequestId);
                        if (activeDetailStock) updateInstrumentDetailLive(activeDetailStock.id);
                        if (['beranda', 'pasar'].includes(currentTab) || (currentTab === 'pasar_porto' && currentPasarPortoSubtab === 'pasar')) updateMarketQuotes();
                    }
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
                if (!isUserScrolling) {
                    refreshSnapshot();
                    if (currentTab === 'aset' || (currentTab === 'pasar_porto' && currentPasarPortoSubtab === 'porto')) syncPortfolioLive();
                    if (activeReceiptRequestId) updateReceiptLive(activeReceiptRequestId);
                    if (activeDetailStock) updateInstrumentDetailLive(activeDetailStock.id);
                    if (['beranda', 'pasar'].includes(currentTab) || (currentTab === 'pasar_porto' && currentPasarPortoSubtab === 'pasar')) updateMarketQuotes();
                }
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
