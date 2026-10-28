"""Validate every delivered native asset and its recorded provenance; no image edits."""
import csv, hashlib, json
from collections import Counter
from pathlib import Path
from PIL import Image
work = Path(__file__).resolve().parent
root = work.parents[1]
art = root / 'Artifacts/Collection-Art-150-2026-10-22'
catalog_path = root / 'Work/Owner-Revision-14-2026-10-22/KATALOG-KOLEKSI-150.csv'
assert hashlib.sha256(catalog_path.read_bytes()).hexdigest() == '83d3c7951b8054f1e1d2ffcf2e551d25d68ecefa3b3cecb29a21558b010d9fc5'
catalog = list(csv.DictReader(catalog_path.open(encoding='utf-8-sig')))
rows = list(csv.DictReader((work/'MANIFEST.csv').open(encoding='utf-8-sig')))
assert len(rows) == len(catalog) == len({r['item_id'] for r in rows}) == 150
hashes, dimensions, margins = [], Counter(), {}
for row, original in zip(rows, catalog):
    ident = row['item_id']
    assert all(row[k] == original[k] for k in ('item_id', 'nama', 'kategori', 'rarity')), ident
    assert row['status'] == 'GENERATED_VERIFIED', ident
    target = art/'images'/f'{ident}.png'
    assert target.resolve() == Path(row['final_path']).resolve(), ident
    digest = hashlib.sha256(target.read_bytes()).hexdigest()
    assert digest == row['sha256'], ident
    hashes.append(digest)
    provenance = row['generation_provenance']
    assert provenance.startswith('built-in image_gen; source='), ident
    source = Path(provenance.split('source=', 1)[1])
    assert source.is_file() and hashlib.sha256(source.read_bytes()).hexdigest() == digest, ident
    assert Path(row['prompt_path']).is_file() and Path(row['prompt_path']).stat().st_size > 100, ident
    assert (art/'review'/f'{ident}.jpg').is_file(), ident
    with Image.open(target) as im:
        assert im.format == 'PNG' and im.mode == 'RGBA' and im.width == im.height, ident
        assert (im.width, im.height) == (int(row['width_px']), int(row['height_px'])), ident
        alpha = im.getchannel('A')
        assert alpha.getextrema() == (0,255), ident
        box = alpha.point(lambda a: 255 if a > 16 else 0).getbbox()
        assert box and min(box[0], box[1], im.width-box[2], im.height-box[3]) > 0, ident
        margins[ident] = round(min(box[0], box[1], im.width-box[2], im.height-box[3])/im.width, 4)
        dimensions[f'{im.width}x{im.height}'] += 1
    qa = json.loads((work/f'{ident}-qa.json').read_text(encoding='utf-8'))
    assert qa['visual_review'] == row['visual_qa'], ident
    # ponytail: keep one authoritative visual review in both existing metadata locations.
    row['notes'] = json.dumps(qa)
assert len(set(hashes)) == 150
assert len(list((art/'images').glob('*.png'))) == 150
assert Counter(r['rarity'] for r in rows) == {'UMUM':25,'TIDAK_UMUM':29,'LANGKA':35,'EPIK':29,'LEGENDARIS':32}
assert len({r['kategori'] for r in rows}) == 12
with (work/'MANIFEST.csv').open('w',encoding='utf-8',newline='') as f:
    writer = csv.DictWriter(f, fieldnames=rows[0].keys()); writer.writeheader(); writer.writerows(rows)
result = dict(result='PASS',generated_files=150,verified=150,unique_sha256=150,
              category_counts=dict(Counter(r['kategori'] for r in rows)),rarity_counts=dict(Counter(r['rarity'] for r in rows)),
              native_dimensions=dict(dimensions),catalog_sha256=hashlib.sha256(catalog_path.read_bytes()).hexdigest(),
              smallest_meaningful_alpha_margin=min(margins.values()),margins=margins,
              provenance='Every final is byte-identical to its actual returned builtin image_gen source; previews only are composited.')
(work/'FINAL-QA.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
(work/'CHECKPOINT.json').write_text(json.dumps(dict(generated_files=150,verified=150,pending_ids=[],next_action='Complete: all individual assets reviewed and audit passed'),indent=2),encoding='utf-8')
print(json.dumps({k:v for k,v in result.items() if k != 'margins'},indent=2))

