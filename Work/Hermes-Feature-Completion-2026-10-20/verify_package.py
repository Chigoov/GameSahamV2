"""Validate handoff materials only. Never executes the game or edits player data."""
from __future__ import annotations

import argparse
import copy
import csv
import hashlib
import json
import re
from pathlib import Path
from urllib.parse import unquote

PACKAGE = Path(__file__).resolve().parent
ROOT = PACKAGE.parents[1]
GROUPS = {
    'shell', 'wealth', 'market', 'chart', 'trade', 'allocation', 'health',
    'loans', 'ipo', 'funds', 'deposits', 'missions', 'achievements', 'ranks',
    'rivals', 'leaderboard', 'collection', 'empire', 'employees', 'morale',
    'empire-invest', 'prestige', 'archetype', 'daily-report', 'news', 'profile',
}
STATUSES = {'NOT_TESTED', 'IN_PROGRESS', 'FAIL', 'BLOCKED', 'PASS'}


def digest(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def safe_path(relative: str) -> Path:
    path = (ROOT / relative).resolve()
    if not path.is_relative_to(ROOT.resolve()):
        raise ValueError(f'Path outside production root: {relative}')
    return path


def read_rows(path: Path) -> list[dict[str, str]]:
    with path.open(encoding='utf-8-sig', newline='') as stream:
        return list(csv.DictReader(stream))


def validate_rows(rows: list[dict[str, str]], external_ids: set[str]) -> None:
    ids = [row['id'] for row in rows]
    if len(ids) != len(set(ids)):
        raise ValueError('Duplicate delta IDs')
    by_id = {row['id']: row for row in rows}
    groups = {row['feature_id'] for row in rows if row['origin'] == 'REFERENCE_GROUP'}
    if groups != GROUPS:
        raise ValueError('Coverage must contain exactly the 26 reference groups')
    required = {f'AUDIT:F{i}' for i in range(1, 10)}
    required |= {f'NEWOWNER:{i:02}' for i in range(1, 6)}
    required |= {f'QUALITY:{i:02}' for i in range(1, 9)}
    if not required <= by_id.keys():
        raise ValueError('Missing audit, owner-answer or quality contracts')
    for row in rows:
        for key in ('id', 'feature_id', 'title', 'origin', 'baseline_status',
                    'mandatory', 'priority', 'phases', 'source_reference',
                    'work_required', 'acceptance', 'verification_status'):
            if not row.get(key, '').strip():
                raise ValueError(f'{row["id"]}: empty {key}')
        if row['verification_status'] not in STATUSES:
            raise ValueError(f'{row["id"]}: invalid verification status')
        if row['priority'] not in {'P0', 'P1', 'P2'}:
            raise ValueError('Invalid priority')
        if row['mandatory'] not in {'YES', 'EVIDENCE_REVIEW'}:
            raise ValueError('Mandatory scope may not be silently removed')
        if not set(row['phases'].split(';')) <= {f'{i:02}' for i in range(14)}:
            raise ValueError(f'{row["id"]}: invalid existing phase')
        parent = row['parent_id']
        if parent and parent not in by_id and parent not in external_ids:
            raise ValueError(f'{row["id"]}: unknown parent {parent}')
        for dep in filter(None, row['depends_on'].split(';')):
            if dep not in by_id or dep == row['id']:
                raise ValueError(f'{row["id"]}: unknown/self dependency {dep}')
        if row['verification_status'] == 'PASS':
            if not all(row.get(k, '').strip() for k in ('evidence_path', 'source_revision', 'reviewer')):
                raise ValueError(f'{row["id"]}: PASS without evidence/revision/reviewer')
            for dep in filter(None, row['depends_on'].split(';')):
                if by_id[dep]['verification_status'] != 'PASS':
                    raise ValueError(f'{row["id"]}: PASS with unpassed dependency')
            if any(child['verification_status'] != 'PASS'
                   for child in rows if child['parent_id'] == row['id']):
                raise ValueError(f'{row["id"]}: PASS with unpassed child')
    visiting, visited = set(), set()

    def walk(node: str) -> None:
        if node in visiting:
            raise ValueError(f'Dependency cycle at {node}')
        if node in visited:
            return
        visiting.add(node)
        for dep in filter(None, by_id[node]['depends_on'].split(';')):
            walk(dep)
        visiting.remove(node)
        visited.add(node)

    for node in ids:
        walk(node)


def self_test(rows: list[dict[str, str]], external_ids: set[str]) -> int:
    validate_rows(rows, external_ids)
    cases = []
    broken = copy.deepcopy(rows)
    broken.append(copy.deepcopy(broken[0]))
    cases.append(broken)
    for field, value in [('parent_id', 'REF:DOES_NOT_EXIST'),
                         ('depends_on', 'GAP:DOES_NOT_EXIST'),
                         ('phases', '19'), ('verification_status', 'COMPLETE'),
                         ('mandatory', 'OPTIONAL'), ('acceptance', '')]:
        broken = copy.deepcopy(rows)
        broken[0][field] = value
        cases.append(broken)
    broken = copy.deepcopy(rows)
    broken[0]['verification_status'] = 'PASS'
    cases.append(broken)
    for missing in ('GAP:shell', 'AUDIT:F9', 'NEWOWNER:04', 'QUALITY:08'):
        cases.append([copy.deepcopy(r) for r in rows if r['id'] != missing])
    broken = copy.deepcopy(rows)
    next(r for r in broken if r['id'] == 'AUDIT:F2')['depends_on'] = 'AUDIT:F1'
    cases.append(broken)
    for number, broken in enumerate(cases, 1):
        try:
            validate_rows(broken, external_ids)
        except ValueError:
            continue
        raise AssertionError(f'Negative control {number} was accepted')
    return len(cases)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--self-test', action='store_true')
    parser.add_argument('--strict-source', action='store_true',
                        help='Also fail on source drift; never restore source to match the old snapshot')
    parser.add_argument('--json-output', help='Optional report path inside production root')
    args = parser.parse_args()
    manifest = json.loads((PACKAGE / 'MATERIALS.json').read_text(encoding='utf-8'))
    if ROOT.resolve() != Path(manifest['production_root']).resolve():
        raise ValueError('Unexpected production root')
    seed = read_rows(safe_path(manifest['existing_seed']))
    if len(seed) != manifest['existing_seed_rows'] or len({r['id'] for r in seed}) != len(seed):
        raise ValueError('Existing 277-row seed missing or duplicated')
    if {r['id'] for r in seed if r['origin'] == 'PRD'} != set(manifest['existing_prd_ids']):
        raise ValueError('Original PRD IDs changed; inspect scope rather than discarding requirements')
    for relative, expected in manifest['package_files'].items():
        if digest(safe_path(relative)) != expected:
            raise ValueError(f'Package changed since preparation: {relative}; inspect legitimate edits, do not restore source blindly')
    rows = read_rows(PACKAGE / 'GAP-MATRIX.csv')
    validate_rows(rows, {r['id'] for r in seed})
    for relative in manifest['reference_files']:
        if not safe_path(relative).is_file():
            raise ValueError(f'Missing referenced material: {relative}')
    for row in rows:
        if not safe_path(row['source_reference']).is_file():
            raise ValueError(f'Missing source reference: {row["source_reference"]}')
    for doc in PACKAGE.glob('*.md'):
        for target in re.findall(r'!?\[[^\]]*\]\(([^)]+)\)', doc.read_text(encoding='utf-8')):
            target = unquote(target.strip('<>')).split('#', 1)[0]
            if not target or re.match(r'^[a-z]+://', target):
                continue
            path = (doc.parent / target).resolve()
            if not path.is_relative_to(ROOT.resolve()) or not path.exists():
                raise ValueError(f'Broken/outside local link in {doc.name}: {target}')
    drift = [p for p, old in manifest['source_snapshot'].items()
             if not safe_path(p).is_file() or digest(safe_path(p)) != old]
    if args.strict_source and drift:
        raise ValueError(f'Source changed since audit snapshot: {drift}')
    negative_controls = self_test(rows, {r['id'] for r in seed}) if args.self_test else 0
    result = {
        'materials_status': 'PASS', 'game_status': 'NOT_VERIFIED_BY_THIS_VALIDATOR',
        'delta_rows': len(rows), 'reference_groups': len(GROUPS),
        'existing_seed_rows': len(seed), 'existing_prd_rows': len(manifest['existing_prd_ids']),
        'audit_findings': 9, 'owner_answers': 5, 'quality_contracts': 8,
        'negative_controls_rejected': negative_controls,
        'source_drift': drift,
        'note': 'No game, browser, APK or player-save mutation performed. Source drift requires reinspection, not rollback.',
    }
    if args.json_output:
        output = safe_path(args.json_output)
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(result, indent=2), encoding='utf-8')
    print(json.dumps(result, ensure_ascii=True))


if __name__ == '__main__':
    main()
