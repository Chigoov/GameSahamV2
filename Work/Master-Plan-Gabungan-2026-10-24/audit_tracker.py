import csv
import sys
import os

CANONICAL_126_IDS = (
    'N01-01', 'N01-02', 'N01-03', 'N01-04', 'N01-05', 'N01-06',
    'N02-01', 'N02-02', 'N02-03', 'N02-04', 'N02-05', 'N02-06', 'N02-07',
    'N03-01', 'N03-02', 'N03-03', 'N03-04', 'N03-05',
    'N05-01', 'N05-02', 'N05-03', 'N05-04', 'N05-05',
    'N06-01', 'N06-02', 'N06-03', 'N06-04',
    'N14-01', 'N14-02', 'N14-03', 'N14-04', 'N14-05', 'N14-06', 'N14-07', 'N14-08',
    'N07-01', 'N07-02', 'N07-03', 'N07-04', 'N07-05', 'N07-06', 'N07-07',
    'N08-01', 'N08-02', 'N08-03', 'N08-04', 'N08-05', 'N08-06',
    'N09-01', 'N09-02', 'N09-03', 'N09-04', 'N09-05', 'N09-06',
    'N11-01', 'N11-02', 'N11-03', 'N11-04', 'N11-05', 'N11-06',
    'N17-01', 'N17-02', 'N17-03', 'N17-04', 'N17-05', 'N17-06',
    'N10-01', 'N10-02', 'N10-03', 'N10-04', 'N10-05', 'N10-06', 'N10-07',
    'N15-01', 'N15-02', 'N15-03', 'N15-04', 'N15-05', 'N15-06', 'N15-07', 'N15-08',
    'N12-01', 'N12-02', 'N12-03', 'N12-04', 'N12-05', 'N12-06', 'N12-07',
    'N13-01', 'N13-02', 'N13-03', 'N13-04', 'N13-05', 'N13-06',
    'N18-01', 'N18-02', 'N18-03', 'N18-04', 'N18-05', 'N18-06',
    'N19-01', 'N19-02', 'N19-03', 'N19-04', 'N19-05', 'N19-06', 'N19-07',
    'N04-01', 'N04-02', 'N04-03', 'N04-04', 'N04-05',
    'N16-01', 'N16-02', 'N16-03', 'N16-04', 'N16-05', 'N16-06', 'N16-07', 'N16-08',
    'N20-01', 'N20-02', 'N20-03', 'N20-04', 'N20-05', 'N20-06'
)

EXPECTED_CANONICAL_SET = set(CANONICAL_126_IDS)
EXPECTED_PACKAGES = {f'N{i:02d}' for i in range(1, 21)}
EXPECTED_GROUPS = {f'G{i:02d}' for i in range(1, 9)}

def audit_tracker_file(filepath):
    if not os.path.exists(filepath):
        return False, f"Tracker file not found: {filepath}"

    with open(filepath, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        rows = list(reader)

    errors = []

    if len(rows) != 126:
        errors.append(f"Row count mismatch: expected 126 rows, found {len(rows)}")

    seen_ids = set()
    duplicates = []
    non_pass = []
    packages = set()
    groups = set()

    for idx, r in enumerate(rows, start=1):
        sub_id = r.get('id')
        if not sub_id:
            errors.append(f"Row {idx} missing 'id' column")
            continue

        if sub_id in seen_ids:
            duplicates.append(sub_id)
        seen_ids.add(sub_id)

        st = r.get('status')
        if st != 'PASS_WITHIN_SCOPE':
            non_pass.append((sub_id, st))

        pkg = r.get('package')
        if pkg:
            packages.add(pkg)

        grp = r.get('execution_group')
        if grp:
            groups.add(grp)

    if duplicates:
        errors.append(f"Duplicate IDs found ({len(duplicates)}): {duplicates}")

    missing_ids = EXPECTED_CANONICAL_SET - seen_ids
    if missing_ids:
        errors.append(f"Missing canonical IDs ({len(missing_ids)}): {sorted(list(missing_ids))}")

    unexpected_ids = seen_ids - EXPECTED_CANONICAL_SET
    if unexpected_ids:
        errors.append(f"Unexpected non-canonical IDs ({len(unexpected_ids)}): {sorted(list(unexpected_ids))}")

    missing_pkgs = EXPECTED_PACKAGES - packages
    if missing_pkgs:
        errors.append(f"Missing packages ({len(missing_pkgs)}): {sorted(list(missing_pkgs))}")

    missing_grps = EXPECTED_GROUPS - groups
    if missing_grps:
        errors.append(f"Missing execution groups ({len(missing_grps)}): {sorted(list(missing_grps))}")

    if non_pass:
        errors.append(f"Non-PASS subtasks found ({len(non_pass)}): {non_pass}")

    if errors:
        error_msg = "; ".join(errors)
        print(f"FAILED: {error_msg}")
        return False, error_msg

    summary = "All 126 canonical subtasks verified PASS_WITHIN_SCOPE across 20 packages and 8 groups"
    print(f"SUCCESS: {summary}")
    return True, summary

if __name__ == '__main__':
    target_path = None
    if len(sys.argv) > 1 and not sys.argv[1].startswith('--'):
        target_path = sys.argv[1]

    if target_path:
        print(f"--- Auditing Specified Tracker File: {target_path} ---")
        ok, msg = audit_tracker_file(target_path)
        if not ok:
            print(f"AUDIT GATE NON-ZERO EXIT: Tracker failed verification -> {msg}")
            sys.exit(1)
        sys.exit(0)

    print("--- Running Negative Control Self-Test for Tracker Audit Gate ---")

    test_rows_dup = [
        {'id': 'N01-01', 'package': 'N01', 'execution_group': 'G01', 'status': 'PASS_WITHIN_SCOPE'},
        {'id': 'N01-01', 'package': 'N01', 'execution_group': 'G01', 'status': 'PASS_WITHIN_SCOPE'}
    ]
    neg_path = 'temp_neg_tracker.csv'
    with open(neg_path, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=['id', 'package', 'execution_group', 'status'])
        writer.writeheader()
        writer.writerows(test_rows_dup)

    ok, msg = audit_tracker_file(neg_path)
    if os.path.exists(neg_path):
        os.remove(neg_path)
    if ok:
        print("ERROR: Negative control failed to reject duplicate fixture!")
        sys.exit(1)
    print("Negative Control 1 PASS: Duplicate fixture correctly rejected with failure!")

    test_rows_pending = [{'id': cid, 'package': cid.split('-')[0], 'execution_group': 'G01', 'status': 'PENDING'} for cid in CANONICAL_126_IDS]
    with open(neg_path, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=['id', 'package', 'execution_group', 'status'])
        writer.writeheader()
        writer.writerows(test_rows_pending)

    ok, msg = audit_tracker_file(neg_path)
    if os.path.exists(neg_path):
        os.remove(neg_path)
    if ok:
        print("ERROR: Negative control failed to reject PENDING fixture!")
        sys.exit(1)
    print("Negative Control 2 PASS: Non-PASS status correctly rejected with failure!")

    test_rows_corrupt_ids = [{'id': cid, 'package': cid.split('-')[0], 'execution_group': 'G01', 'status': 'PASS_WITHIN_SCOPE'} for cid in CANONICAL_126_IDS[1:]]
    test_rows_corrupt_ids.append({'id': 'N99-99', 'package': 'N99', 'execution_group': 'G99', 'status': 'PASS_WITHIN_SCOPE'})
    with open(neg_path, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=['id', 'package', 'execution_group', 'status'])
        writer.writeheader()
        writer.writerows(test_rows_corrupt_ids)

    ok, msg = audit_tracker_file(neg_path)
    if os.path.exists(neg_path):
        os.remove(neg_path)
    if ok:
        print("ERROR: Negative control failed to reject missing/unexpected ID fixture!")
        sys.exit(1)
    print("Negative Control 3 PASS: Missing/unexpected ID fixture correctly rejected!")

    default_tracker = 'C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/Master-Plan-Gabungan-2026-10-24/TRACKER.csv'
    print(f"\n--- Auditing Canonical TRACKER.csv: {default_tracker} ---")
    ok, msg = audit_tracker_file(default_tracker)
    if not ok:
        print(f"AUDIT GATE FAILED: {msg}")
        sys.exit(1)

    print("AUDIT GATE SUCCESS: Canonical tracker verified 100%!")
    sys.exit(0)
