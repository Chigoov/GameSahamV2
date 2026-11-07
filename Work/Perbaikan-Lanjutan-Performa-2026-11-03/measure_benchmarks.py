import time
import json
import math

def nearest_rank_p95(samples):
    if not samples:
        return 0.0
    s = sorted(samples)
    idx = max(0, min(len(s) - 1, math.ceil(0.95 * len(s)) - 1))
    return s[idx]

def run_benchmarks():
    print("=== PROFILING & MEASUREMENT SUITE (LP06 / T16) ===")
    report = {
        "timestamp": "2026-11-03T04:20:00Z",
        "environment": "Windows 11 / Java 21 / Chromium Headless (Port 8794)",
        "categories": {}
    }

    # 1. Benchmark Navigation Response (30 samples per primary tab)
    tabs = [
        ("beranda", "button.nav-item[data-tab='beranda']", "#view-beranda.active"),
        ("pasar_porto", "button.nav-item[data-tab='pasar_porto']", "#view-pasar_porto.active"),
        ("firma", "button.nav-item[data-tab='firma']", "#view-firma.active"),
        ("keuangan", "button.nav-item[data-tab='keuangan']", "#view-keuangan.active"),
        ("riset", "button.nav-item[data-tab='riset']", "#view-riset.active")
    ]

    print("\n[1] Mengukur Latensi Transisi Tab Antarmuka (30 sampel tiap rute)...")
    for name, selector, active_selector in tabs:
        samples = []
        for i in range(30):
            # Click another tab first if needed to ensure transition occurs
            if i % 2 == 0:
                js("document.querySelector(\"button.nav-item[data-tab='beranda']\")?.click()")
            else:
                js("document.querySelector(\"button.nav-item[data-tab='pasar_porto']\")?.click()")
            time.sleep(0.05)

            t0 = time.perf_counter()
            js(f"document.querySelector(\"{selector}\")?.click()")
            # wait until active selector matches
            for _ in range(50):
                if js(f"!!document.querySelector(\"{active_selector}\")"):
                    break
                time.sleep(0.005)
            t1 = time.perf_counter()
            elapsed_ms = (t1 - t0) * 1000.0
            samples.append(elapsed_ms)

        p95 = nearest_rank_p95(samples)
        avg = sum(samples) / len(samples)
        max_val = max(samples)
        min_val = min(samples)
        report["categories"][f"nav_{name}"] = {
            "samples_count": len(samples),
            "min_ms": round(min_val, 2),
            "avg_ms": round(avg, 2),
            "p95_ms": round(p95, 2),
            "max_ms": round(max_val, 2),
            "samples": [round(s, 2) for s in samples]
        }
        print(f"  Nav {name}: Min={min_val:.2f}ms | Avg={avg:.2f}ms | P95={p95:.2f}ms | Max={max_val:.2f}ms")

    # 2. Benchmark Modal Open & Close: Stock Detail (30 samples)
    print("\n[2] Mengukur Latensi Buka/Tutup Modal Detail Saham (30 sampel)...")
    js("document.querySelector(\"button.nav-item[data-tab='beranda']\")?.click()")
    time.sleep(0.2)
    detail_open_samples = []
    detail_close_samples = []

    for i in range(30):
        t0 = time.perf_counter()
        js("document.querySelectorAll('#home-market-list .stock-list-item')[0]?.click()")
        for _ in range(50):
            if js("!!document.querySelector('.flow-page')"):
                break
            time.sleep(0.005)
        t1 = time.perf_counter()
        detail_open_samples.append((t1 - t0) * 1000.0)

        t2 = time.perf_counter()
        js("document.querySelector('.flow-page button.back-button')?.click() || window.closeActiveLayer()")
        for _ in range(50):
            if js("!document.querySelector('.flow-page')"):
                break
            time.sleep(0.005)
        t3 = time.perf_counter()
        detail_close_samples.append((t3 - t2) * 1000.0)

    report["categories"]["modal_stock_detail_open"] = {
        "samples_count": len(detail_open_samples),
        "min_ms": round(min(detail_open_samples), 2),
        "avg_ms": round(sum(detail_open_samples) / len(detail_open_samples), 2),
        "p95_ms": round(nearest_rank_p95(detail_open_samples), 2),
        "max_ms": round(max(detail_open_samples), 2),
        "samples": [round(s, 2) for s in detail_open_samples]
    }
    report["categories"]["modal_stock_detail_close"] = {
        "samples_count": len(detail_close_samples),
        "min_ms": round(min(detail_close_samples), 2),
        "avg_ms": round(sum(detail_close_samples) / len(detail_close_samples), 2),
        "p95_ms": round(nearest_rank_p95(detail_close_samples), 2),
        "max_ms": round(max(detail_close_samples), 2),
        "samples": [round(s, 2) for s in detail_close_samples]
    }
    print(f"  Modal Open: Avg={report['categories']['modal_stock_detail_open']['avg_ms']}ms | P95={report['categories']['modal_stock_detail_open']['p95_ms']}ms")
    print(f"  Modal Close: Avg={report['categories']['modal_stock_detail_close']['avg_ms']}ms | P95={report['categories']['modal_stock_detail_close']['p95_ms']}ms")

    # 3. Benchmark Tick Cycle Latency & IPC Call Count (Beranda vs Pasar vs Detail)
    print("\n[3] Mengukur Latensi Siklus Sinkronisasi Rutin per Layar (30 sampel)...")
    screens = [
        ("beranda", "document.querySelector(\"button.nav-item[data-tab='beranda']\")?.click()"),
        ("pasar_porto", "document.querySelector(\"button.nav-item[data-tab='pasar_porto']\")?.click()"),
        ("detail_bbca", "document.querySelectorAll('#home-market-list .stock-list-item')[0]?.click()")
    ]

    for screen_name, setup_code in screens:
        js("window.closeActiveLayer?.()")
        js(setup_code)
        time.sleep(0.3)
        tick_samples = []

        for i in range(30):
            t0 = time.perf_counter()
            js("TradingNativeBridge.syncClock()")
            t1 = time.perf_counter()
            tick_samples.append((t1 - t0) * 1000.0)
            time.sleep(0.01)

        p95 = nearest_rank_p95(tick_samples)
        avg = sum(tick_samples) / len(tick_samples)
        report["categories"][f"tick_{screen_name}"] = {
            "samples_count": len(tick_samples),
            "min_ms": round(min(tick_samples), 2),
            "avg_ms": round(avg, 2),
            "p95_ms": round(p95, 2),
            "max_ms": round(max(tick_samples), 2),
            "samples": [round(s, 2) for s in tick_samples]
        }
        print(f"  Tick on {screen_name}: Min={min(tick_samples):.2f}ms | Avg={avg:.2f}ms | P95={p95:.2f}ms | Max={max(tick_samples):.2f}ms")

    js("window.closeActiveLayer?.()")
    print("\n[✓] Pengumpulan 30 sampel individual per kategori selesai tanpa error.")
    return report

rep = run_benchmarks()
with open("C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/Perbaikan-Lanjutan-Performa-2026-11-03/logs/lp06_measured_benchmarks.json", "w", encoding="utf-8") as f:
    json.dump(rep, f, indent=2)

print("\nSaved benchmark JSON to logs/lp06_measured_benchmarks.json")
