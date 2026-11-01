"""Record a visually reviewed image; never generate or alter a final image."""
import csv, hashlib, json, shutil, sys
from pathlib import Path
from PIL import Image, ImageDraw

WORK = Path(__file__).resolve().parent
ROOT = WORK.parents[1]
ART = ROOT / 'Artifacts/Collection-Art-150-2026-10-22'

def record(item_id, source, prompt, qa):
    rows = list(csv.DictReader((WORK / 'MANIFEST.csv').open(encoding='utf-8-sig', newline='')))
    matches = [r for r in rows if r['item_id'] == item_id]
    assert len(matches) == 1, 'Unknown or duplicate ID'
    source = Path(source).resolve()
    with Image.open(source) as im:
        assert im.format == 'PNG' and im.mode == 'RGBA'
        w, h = im.size
        assert w == h
        alpha = im.getchannel('A')
        assert alpha.getextrema() == (0, 255), 'No genuine alpha'
        box = alpha.point(lambda value: 255 if value > 16 else 0).getbbox()
        assert box and box[0] > 0 and box[1] > 0 and box[2] < w and box[3] < h, 'Touches canvas edge'
        metrics = {'width': w, 'height': h, 'alpha_extrema': [0, 255], 'alpha_bbox': box,
                   'raw_alpha_bbox': alpha.getbbox(), 'crop_threshold': 16,
                   'transparent_pixels': alpha.histogram()[0], 'visual_review': qa}
        # ponytail: review composites only; final bytes are copied unchanged.
        preview = Image.new('RGB', (640, 380), '#121a32')
        preview.paste(Image.new('RGB', (320, 380), '#f2f0ed'), (320, 0))
        thumb = im.copy()
        thumb.thumbnail((300, 320))
        for x in (0, 320):
            preview.paste(thumb, (x + (320-thumb.width)//2, 32+(320-thumb.height)//2), thumb)
        ImageDraw.Draw(preview).text((12, 8), item_id, fill='white')
        (ART / 'review').mkdir(parents=True, exist_ok=True)
        preview.save(ART / 'review' / f'{item_id}.jpg')
    target = ART / 'images' / f'{item_id}.png'
    target.parent.mkdir(parents=True, exist_ok=True)
    if target.exists():
        old_hash = hashlib.sha256(target.read_bytes()).hexdigest()
        assert old_hash == matches[0]['sha256'] and matches[0]['generation_provenance'].startswith('built-in image_gen;'), 'Refusing unrelated overwrite'
        (ART / 'variants').mkdir(exist_ok=True)
        shutil.copy2(target, ART / 'variants' / f'{item_id}-{old_hash[:12]}.png')
    shutil.copy2(source, target)
    digest = hashlib.sha256(target.read_bytes()).hexdigest()
    assert digest == hashlib.sha256(source.read_bytes()).hexdigest()
    row = matches[0]
    row.update(status='GENERATED_PENDING_VISUAL_QA', final_path=str(target), width_px=w, height_px=h,
               sha256=digest, generation_provenance='built-in image_gen; source='+str(source),
               prompt_path=str(Path(prompt).resolve()), visual_qa=qa, notes=json.dumps(metrics))
    with (WORK / 'MANIFEST.csv').open('w', encoding='utf-8', newline='') as f:
        writer = csv.DictWriter(f, fieldnames=rows[0].keys()); writer.writeheader(); writer.writerows(rows)
    (WORK / f'{item_id}-qa.json').write_text(json.dumps(metrics, indent=2), encoding='utf-8')
    (WORK / 'CHECKPOINT.json').write_text(json.dumps({
        'last_recorded_item': item_id,
        'generated_files': sum(bool(r['final_path']) for r in rows),
        'verified': sum(r['status'] == 'GENERATED_VERIFIED' for r in rows),
        'pending_ids': [r['item_id'] for r in rows if r['status'] != 'GENERATED_VERIFIED'],
        'next_action': 'Inspect generated previews, resolve QA findings, continue individual imagegen calls',
    }, indent=2), encoding='utf-8')
    print(json.dumps({'item_id': item_id, 'path': str(target), 'sha256': digest, **metrics}))

def review(status, qa, ids):
    assert status in ('GENERATED_VERIFIED', 'GENERATED_PENDING_FRAMING', 'GENERATED_PENDING_DESIGN')
    rows = list(csv.DictReader((WORK / 'MANIFEST.csv').open(encoding='utf-8-sig', newline='')))
    selected = [r for r in rows if r['item_id'] in ids]
    assert len(selected) == len(set(ids)) and all(Path(r['final_path']).is_file() for r in selected)
    for row in selected:
        row.update(status=status, visual_qa=qa)
        path = WORK / (row['item_id'] + '-qa.json')
        metrics = json.loads(path.read_text(encoding='utf-8'))
        metrics['visual_review'] = qa
        row['notes'] = json.dumps(metrics)
        path.write_text(json.dumps(metrics, indent=2), encoding='utf-8')
    with (WORK / 'MANIFEST.csv').open('w', encoding='utf-8', newline='') as f:
        writer = csv.DictWriter(f, fieldnames=rows[0].keys()); writer.writeheader(); writer.writerows(rows)
    checkpoint = json.loads((WORK / 'CHECKPOINT.json').read_text(encoding='utf-8'))
    checkpoint.update(generated_files=sum(bool(r['final_path']) for r in rows),
                      verified=sum(r['status'] == 'GENERATED_VERIFIED' for r in rows),
                      pending_ids=[r['item_id'] for r in rows if r['status'] != 'GENERATED_VERIFIED'])
    (WORK / 'CHECKPOINT.json').write_text(json.dumps(checkpoint, indent=2), encoding='utf-8')
    print(f'Review recorded for {len(selected)} real assets: {status}')

if __name__ == '__main__':
    if len(sys.argv) > 4 and sys.argv[1] == '--qa':
        review(sys.argv[2], sys.argv[3], sys.argv[4:])
    else:
        assert len(sys.argv) == 5, 'Usage: record_asset.py ID SOURCE PROMPT VISUAL_QA; or --qa STATUS QA ID...'
        record(*sys.argv[1:])
