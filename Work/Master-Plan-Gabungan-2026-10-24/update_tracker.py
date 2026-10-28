import csv
from pathlib import Path

csv_path = Path('C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/Master-Plan-Gabungan-2026-10-24/TRACKER.csv')
rows = []

with open(csv_path, 'r', encoding='utf-8', newline='') as f:
    reader = csv.reader(f)
    header = next(reader)
    rows.append(header)
    for row in reader:
        if not row: continue
        task_id = row[0]
        pkg = row[1]
        group = row[2]
        
        if group == 'G02' and pkg in ['N03', 'N05', 'N06', 'N14']:
            row[4] = 'PASS_WITHIN_SCOPE'
            row[10] = 'SELF_REVIEW_VERIFIED; independent agent prohibited'
            row[11] = 'Complete within PLAN-02. Verified against fresh domain and browser evidence.'
            
            if pkg == 'N03':
                row[7] = 'Shared selectMarketMovers ranking helper with tie-breaking, bounds protection, and active filter alignment'
                row[8] = 'test-n03-movers-unit.cjs 8/8 PASS; test-movers-browser.cjs PASS'
                row[9] = 'Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/test-n03-movers-unit.cjs; test-movers-browser.cjs; market-highlights-element.png'
            elif pkg == 'N05':
                row[7] = 'Alternative OHLCV table and toggle button removed; dead CSS pruned; non-color accessibility scrub slider active'
                row[8] = 'test-chart-browser.cjs PASS; chart-matrix-ui.cjs 20/20 PASS'
                row[9] = 'Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/test-chart-browser.cjs; chart-matrix-output/results.json'
            elif pkg == 'N06':
                row[7] = 'Technical data source block and duplicate timeframe notice removed; clean bottom spacing; error alert retained'
                row[8] = 'test-chart-browser.cjs PASS; chart-matrix-ui.cjs 20/20 PASS'
                row[9] = 'Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/test-chart-browser.cjs; chart-matrix-output/results.json'
            elif pkg == 'N14':
                row[7] = 'Four chart ranges (R6H, R1D, R7D, R30D) with automatic intervals; persistent selection; numeric price protection'
                row[8] = 'ChartHistoryRangeTest.java 7/7 PASS; g02-target-suite 106/106 PASS; chart-matrix-ui.cjs 20/20 PASS'
                row[9] = 'Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/02-pasar-dan-grafik/chart-expanded/junit-latest.log; g02-target-suite; chart-matrix-output'
                
        rows.append(row)

with open(csv_path, 'w', encoding='utf-8', newline='') as f:
    writer = csv.writer(f)
    writer.writerows(rows)

print(f"TRACKER.csv berhasil diperbarui. Total baris: {len(rows)}")
