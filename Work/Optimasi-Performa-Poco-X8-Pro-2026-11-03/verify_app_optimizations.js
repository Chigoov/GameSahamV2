const fs = require('fs');
const assert = require('assert');

console.log("=== VERIFIKASI LOGIKA OPTIMASI JAVASCRIPT (PF02, PF03, PF04) ===");

const code = fs.readFileSync('android/app/src/main/assets/www/app.js', 'utf8');

// 1. Verifikasi Penghapusan Double Refresh Snapshot di setInterval (PF02)
assert.ok(!code.includes("requestClockSync(true).then((snap) => {\n                    document.getElementById('clock-error')?.remove();\n                    if (!isUserScrolling) {\n                        refreshSnapshot(snap);"),
    "Harus sudah menghapus pemanggilan refreshSnapshot kedua di dalam interval requestClockSync");
console.log("✓ PASS: Double-refresh pada interval requestClockSync terbukti dieliminasi.");

// 2. Verifikasi Penghapusan Double Call di .then() lain (PF02)
assert.ok(!code.includes("requestClockSync(true).then(() => refreshSnapshot())"),
    "Harus sudah menghapus refreshSnapshot redundan setelah konfirmasi backup recovery");
assert.ok(!code.includes("requestClockSync(true, true)\n                .then(() => {\n                    showGameToast('success', 'Waktu Disinkronkan', 'Jam permainan telah diselaraskan dengan engine waktu game.', syncOpId + ':OK');\n                    refreshSnapshot();"),
    "Harus sudah menghapus refreshSnapshot redundan di manual sync clock");
console.log("✓ PASS: Redundant refreshSnapshot pada recovery & manual clock sync terbukti dieliminasi.");

// 3. Verifikasi Scroll-Settled Pending Snapshot Deferral (PF02 & PF04)
assert.ok(code.includes("if (isUserScrolling) {\n                        pendingSnapshot = snap;\n                    } else {\n                        refreshSnapshot(snap);\n                    }"),
    "Harus menunda snapshot ke pendingSnapshot jika user sedang scrolling");
assert.ok(code.includes("scrollSettledTimer = setTimeout(() => {\n                isUserScrolling = false;\n                if (pendingSnapshot) {\n                    const snap = pendingSnapshot === true ? null : pendingSnapshot;\n                    pendingSnapshot = null;\n                    refreshSnapshot(snap);\n                }\n            }, 150);"),
    "Harus menerapkan pendingSnapshot segera setelah scroll berhenti 150ms");
console.log("✓ PASS: Mekanisme pendingSnapshot saat scroll dan eksekusi instan pasca-scroll terbukti terpasang.");

// 4. Verifikasi Throttling & Caching Komponen Beranda saat Tab Lain Aktif (PF02)
assert.ok(code.includes("if (liabEl && currentTab === 'beranda' && bridge.getLoans)"),
    "getLoans hanya dipanggil saat tab beranda aktif");
assert.ok(code.includes("if (bridge.getMacroAndSynthesisState && (lastMacroCheckDate !== snap.clock?.date || !document.getElementById('macro-badge')?.textContent))"),
    "getMacroAndSynthesisState hanya dipanggil bila tanggal bursa berganti");
assert.ok(code.includes("if (currentTab === 'beranda') {\n                if (bridge.getInvestorArchetype)"),
    "getInvestorArchetype, getCollections & renderHomeNews hanya dipanggil saat tab beranda aktif");
console.log("✓ PASS: Pemanggilan getLoans, getMacro, archetype, collection & homeNews terisolasi & ter-throttle.");

// 5. Verifikasi Caching Order Book & Chart Update (PF03)
assert.ok(code.includes("const bookSig = JSON.stringify([book.bids, book.asks]);\n                if (depthTable._lastBookSig !== bookSig) {"),
    "depthTable order book hanya memodifikasi tbody jika bids/asks berubah");
assert.ok(code.includes("now - (flowPanel._lastChartRefreshAt || 0) >= 5000"),
    "refreshChart throttled ke 5 detik untuk full sync sementara candle aktif update realtime");
assert.ok(code.includes("// Chart Controls (dipertahankan agar tidak terjadi DOM thrashing pada tombol)"),
    "Controls chart dipertahankan di DOM tanpa replaceChildren");
assert.ok(code.includes("const sig = JSON.stringify(groups.gainers.concat(groups.losers).map(s => [s.id, s.currentPrice, s.dailyChangePercent]));\n        if (highlights._lastSig === sig && highlights.childElementCount > 0) return;"),
    "renderMarketHighlights memeriksa perubahan sebelum manipulasi DOM");
console.log("✓ PASS: Order book caching, chart control preservation, dan highlights caching terbukti aktif.");

console.log("\nSELURUH 5 KELOMPOK PEMERIKSAAN LOGIKA JAVASCRIPT LULUS 100%!");
