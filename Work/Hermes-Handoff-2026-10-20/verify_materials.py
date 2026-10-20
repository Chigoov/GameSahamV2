"""Validate handoff integrity and expected contracts, never game implementation."""
from pathlib import Path
import argparse
import csv
import hashlib
import json
import re

ROOT = Path(__file__).resolve().parents[2]
PACK = Path(__file__).resolve().parent


def validate_file(item):
    path = (ROOT / item['path']).resolve()
    if not path.is_relative_to(ROOT):
        raise ValueError('Manifest path escapes production root')
    data = path.read_bytes()
    if len(data) != item['bytes'] or hashlib.sha256(data).hexdigest() != item['sha256']:
        raise ValueError('Material changed: ' + item['path'])


def rows(path):
    with path.open(encoding='utf-8-sig', newline='') as stream:
        return list(csv.DictReader(stream))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--self-test', action='store_true', help='Also verify tamper/path negative controls')
    args = parser.parse_args()
    manifest = json.loads((PACK / 'MATERIALS.json').read_text(encoding='utf-8-sig'))
    items = manifest['files']
    assert items and len({item['path'] for item in items}) == len(items), 'Duplicate/empty inventory'
    for item in items:
        validate_file(item)

    policy = json.loads((PACK / 'OWNER-DECISIONS.json').read_text(encoding='utf-8-sig'))
    clock = policy['clock']
    assert clock['foreground_real_seconds_per_game_day_at_1x'] == 300
    assert clock['inactive_real_seconds_per_game_day'] == 3600
    assert clock['inactive_inherits_foreground_speed'] is False
    assert clock['lifecycle_background_auto_pause'] is False
    execution = policy['execution']
    assert execution['multi_agent_preference'] == 'AUTO_PREFER_WHEN_AVAILABLE'
    assert execution['capability_detection_evidence_required'] is True
    assert execution['single_writer_per_file_and_shared_state'] is True
    assert execution['shared_QA_save_port_and_build_output_single_owner'] is True
    assert execution['coordinator_owns_integration_and_completion'] is True
    assert 1 <= execution['suggested_max_workers_excluding_coordinator'] <= 3
    quality = policy['phase_quality']
    assert quality['required_phases'] == ['%02d' % number for number in range(14)]
    assert quality['all_applicable_mandatory_criteria_must_pass'] is True
    assert quality['known_open_bugs_allowed'] == {'critical': 0, 'major': 0, 'minor': 0}
    assert quality['critical_sequence_tests']['distinct_sequences_min'] == 10
    assert quality['critical_sequence_tests']['meaningful_actions_per_sequence_min'] == 100
    assert quality['critical_sequence_tests']['invariant_after_each_action'] is True
    assert quality['ui_viewports'] == [[320,568],[360,640],[390,844],[412,915],[430,932],[768,1024],[1440,900]]
    assert quality['ui_text_scales'] == [1, 2]
    performance = quality['local_host_browser_performance']
    assert performance['ordinary_action_p95_ms_max'] == 300
    assert performance['navigation_ms_max'] == 1000
    assert performance['heavy_work_visible_feedback_ms_max'] == 300
    assert performance['heavy_work_must_not_freeze_UI'] is True
    assert quality['different_reviewer_each_phase_when_available'] is True
    assert quality['coordinator_verifies_integrated_result'] is True
    phase_definitions = re.findall(r'^## Phase (\d{2}) ', (PACK / 'PHASE-QUALITY-STANDARDS.md').read_text(encoding='utf-8'), re.MULTILINE)
    assert phase_definitions == quality['required_phases'], 'Missing or duplicate phase definition'
    day = clock['seconds_per_game_day']
    fg = clock['foreground_game_seconds_per_real_second_at_1x']
    bg = clock['inactive_game_seconds_per_real_second']
    assert day == 86400 and fg * 300 == day and bg * 3600 == day
    time_cases = rows(PACK / 'TIME-ACCEPTANCE.csv')
    assert time_cases and len({row['id'] for row in time_cases}) == len(time_cases)
    for case in time_cases:
        expected = int(case['foreground_elapsed_ms']) * fg * int(case['foreground_speed'])
        expected += int(case['inactive_elapsed_ms']) * bg
        assert expected == int(case['expected_game_elapsed_ms']), case['id']
        assert case['runtime_status'] == 'NOT_TESTED', 'Material arithmetic is not runtime evidence'

    coverage = rows(PACK / 'FEATURE-ACCEPTANCE.csv')
    by_id = {row['id']: row for row in coverage}
    assert len(by_id) == len(coverage), 'Duplicate coverage IDs'
    requirements = rows(ROOT / 'Work/REQUIREMENTS.csv')
    reference = rows(ROOT / 'Work/REA-FEATURE-MAP.csv')
    assert len(requirements) == 228 and len(reference) == 26, 'Inspect changed baseline coverage'
    for requirement in requirements:
        entry = by_id['PRD:' + requirement['requirement_id']]
        assert entry['requirement'] == requirement['requirement'], 'Original text lost'
    for feature in reference:
        assert 'REF:' + feature['id'] in by_id, feature['id']
    assert sum(row['origin'] == 'OWNER' for row in coverage) == 23
    assert len(coverage) == 277 and all(row['status'] == 'NOT_TESTED' for row in coverage)

    if args.self_test:
        bad_hash = dict(items[0], sha256='0' * 64)
        bad_path = dict(items[0], path='../outside-production-root')
        missing = dict(items[0], path='Work/Hermes-Handoff-2026-10-20/__missing_material__')
        for invalid in (bad_hash, bad_path, missing):
            try:
                validate_file(invalid)
            except (ValueError, OSError):
                pass
            else:
                raise AssertionError('Negative control was accepted')
    print(json.dumps({
        'material_integrity': 'PASS', 'files': len(items),
        'coverage_rows': len(coverage), 'time_examples': len(time_cases),
        'quality_phase_definitions_checked': len(phase_definitions),
        'phase_execution_status': 'NOT_TESTED',
        'negative_controls': 'PASS' if args.self_test else 'NOT_RUN',
        'gameplay_and_APK_verification': 'NOT_RUN_BY_THIS_TOOL'
    }, ensure_ascii=False))


if __name__ == '__main__':
    main()
