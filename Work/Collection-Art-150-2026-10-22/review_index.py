"""Build a review index and validate the existing manifest; no image generation."""
import csv, hashlib, html
from pathlib import Path

work = Path(__file__).resolve().parent
root = work.parents[1]
art = root / 'Artifacts/Collection-Art-150-2026-10-22'
rows = list(csv.DictReader((work/'MANIFEST.csv').open(encoding='utf-8-sig')))
catalog = list(csv.DictReader((root/'Work/Owner-Revision-14-2026-10-22/KATALOG-KOLEKSI-150.csv').open(encoding='utf-8-sig')))
assert len(rows) == len(catalog) == len({r['item_id'] for r in rows}) == 150
hashes = []
cards = []
for row, original in zip(rows, catalog):
    assert all(row[k] == original[k] for k in ('item_id', 'nama', 'kategori', 'rarity'))
    if not row['final_path']:
        continue
    path = Path(row['final_path'])
    assert path.is_file() and hashlib.sha256(path.read_bytes()).hexdigest() == row['sha256']
    hashes.append(row['sha256'])
    esc = html.escape
    cards.append(f'<article><h2>{esc(row["nama"])}</h2><p>{esc(row["item_id"])} · {esc(row["rarity"])}</p><a href="images/{esc(row["item_id"])}.png"><img src="review/{esc(row["item_id"])}.jpg"></a><p>{esc(row["status"])}</p><small>{esc(row["visual_qa"])}</small></article>')
assert len(hashes) == len(set(hashes)), 'Duplicate final bytes'
verified = sum(r['status'] == 'GENERATED_VERIFIED' for r in rows)
(art/'index.html').write_text('<!doctype html><meta charset="utf-8"><title>Collection Art Review</title><style>body{font:16px system-ui;background:#0f1529;color:#eee;padding:24px}main{display:grid;grid-template-columns:repeat(auto-fit,minmax(360px,1fr));gap:20px}article{background:#1d2641;padding:16px;border-radius:12px}h2{font-size:20px}img{width:100%}small{line-height:1.6}</style>'+f'<h1>Collection Art: {verified}/150 verified</h1><p>Individual final images; previews compare navy and light backgrounds. Native dimensions are recorded in the manifest.</p><main>'+''.join(cards)+'</main>', encoding='utf-8')
print(f'{verified}/150 verified; {len(hashes)} individual files; identity and unique hash checks passed')
