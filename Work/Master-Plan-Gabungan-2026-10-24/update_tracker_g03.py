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
        
        if group == 'G03' and pkg in ['N07', 'N08', 'N09']:
            row[4] = 'PASS_WITHIN_SCOPE'
            row[10] = 'SELF_REVIEW_VERIFIED; independent agent prohibited'
            row[11] = 'Complete within PLAN-03. Verified against fresh domain, browser, and APK evidence.'
            
            if pkg == 'N07':
                row[7] = 'Native radio cards (MARKET, LIMIT, STOP_LOSS, TAKE_PROFIT), dynamic price label/field, domain quote/capacity'
                row[8] = 'OrderMatrixCombinationsTest 3/3 PASS; test-orders-and-notifications.cjs PASS'
                row[9] = 'Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/order-matrix; test-orders-and-notifications.cjs; order-ticket-360x800.png; order-review-360x800.png'
            elif pkg == 'N08':
                row[7] = 'Human-readable receipt without raw UUIDs, actual fees/VWAP/gross/net, live cancel releasing reservation, production expiry lifecycle'
                row[8] = 'OrderReceiptActualsTest 8/8 PASS; OrderCapacityQuoteTest 1/1 PASS; test-orders-and-notifications.cjs PASS'
                row[9] = 'Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/receipt-capacity; order-receipt-360x800.png; order-cancelled-receipt-360x800.png'
            elif pkg == 'N09':
                row[7] = 'Toast queue (max 3 stacked), accessible status/alert roles, inline error feedback, stable event/request deduplication'
                row[8] = 'test-orders-and-notifications.cjs PASS; visual review 360x800 at 100% and 200%'
                row[9] = 'Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/03-order-dan-notifikasi/test-orders-and-notifications.cjs; order-cancelled-receipt-360x800.png; order-receipt-360x800-200.png'
                
        rows.append(row)

with open(csv_path, 'w', encoding='utf-8', newline='') as f:
    writer = csv.writer(f)
    writer.writerows(rows)

print(f"TRACKER.csv berhasil diperbarui untuk G03. Total baris: {len(rows)}")
