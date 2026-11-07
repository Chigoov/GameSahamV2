"""Run current domain/browser regressions and verify a source-bound handoff.

Exit 0: requested scope verified; 1: failed/stale evidence; 2: full-plan claim
is incomplete. No model-written PASS file is accepted as a substitute for a run.
"""
import argparse
from datetime import datetime, timezone
import hashlib
import json
from pathlib import Path
import subprocess
import sys
import time

ROOT = Path(__file__).resolve().parents[2]
TASK = Path(__file__).resolve().parent
SAVE = ROOT / 'Artifacts/LocalPreview/preview-save/save_game_v2.json'
EXPECTED_SAVE = '59b41e24c5a0a3b6f87d127d41c18b7f3f0ebE371f4dd2d277a31ca4b5f867ed'.lower()
REQUIRED = {
    'T07_closed_bar_not_mutated', 'T08_line_updates_from_canonical_bars',
    'T13_same_clock_pending_snapshot', 'T10_owner_cache_preserves_nodes',
    'T10_owner_retry_and_nonplayer_supply_change', 'T04_T05_selection_and_node_identity',
    'T06_T07_T09_canonical_buckets_and_no_rebuild', 'T06_axis_volume_and_empty_recovery',
    'T01_T02_home_and_movers_update_in_place', 'T03_filter_empty_then_restore',
    'T03_filter_remains_after_tick', 'T03_exchange_sector_watchlist',
    'T11_T14_detached_panel_and_repeated_close', 'T12_scroll_main_and_detail_flush_current',
    'T15_market_preview_and_limit_preservation',
    'T04_browser_keyboard', 'T04_browser_pointer_during_refresh',
    'T14_persistent_listener_bounds', 'T15_real_java_account_switch',
    'T15_real_java_restore_removes_selected_account', 'T15_real_java_new_run_same_date',
    'T13_real_java_same_clock_order_reservation',
    'LP08_menu_badge_reuses_canonical_snapshot',
    'LP09_clock_does_not_rewind_on_sync', 'LP09_date_rollover_and_single_format',
    'LP09_calendar_filters_by_WIB_day', 'LP09_detail_latest_daily_prices',
}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def manifest():
    # ponytail: hash owning inputs, not generated outputs or player save contents.
    paths = list((ROOT / 'android/app/src/main/java').rglob('*.java'))
    paths += list((ROOT / 'android/app/src/test/java').rglob('*.java'))
    paths += list((ROOT / 'android/app/src/main/assets').rglob('*'))
    paths += [ROOT / 'android/app/build.gradle', ROOT / 'Work/WireQaServer.java',
              TASK / 'verify_ui.py', TASK / 'verify_apk.py', Path(__file__), TASK / 'test_quality_gate.py']
    return {p.relative_to(ROOT).as_posix(): digest(p) for p in sorted(set(paths)) if p.is_file()}


def validate_cases(data):
    issues = []
    cases = data.get('cases', [])
    ids = [c.get('case') for c in cases]
    if len(set(ids)) != len(ids):
        issues.append('Duplicate browser case IDs')
    for name in sorted(REQUIRED - set(ids)):
        issues.append('Missing browser check: ' + name)
    for c in cases:
        if c.get('status') != 'PASS':
            issues.append('Browser failure: ' + str(c.get('case')))
    if data.get('pageErrors'):
        issues.append('Browser page errors')
    return issues


def validate_report(report, current):
    issues = []
    if report.get('schema') != 1 or not report.get('sourceManifest'):
        issues.append('Missing gate schema/source identity')
    if report.get('sourceManifest') != current:
        issues.append('Source/test/runner changed: rerun gate; old green result is stale')
    if report.get('saveBefore') != EXPECTED_SAVE or report.get('saveAfter') != EXPECTED_SAVE:
        issues.append('Protected save hash differs; do not repair/reset player data')
    jobs = report.get('jobs', [])
    if {j.get('kind') for j in jobs} != {'domain', 'browser'}:
        issues.append('Both domain and browser execution are required')
    for job in jobs:
        if job.get('exitCode') != 0:
            issues.append('Runner failed: ' + str(job.get('kind')))
    issues.extend(report.get('issues', []))
    return sorted(set(issues))


def task_file(relative):
    path = (ROOT / relative).resolve()
    if not path.is_relative_to((TASK / 'logs').resolve()) or not path.is_file():
        raise ValueError('Evidence must be an existing file in task logs')
    return path


def run_gate():
    run_id = 'gate-' + str(time.time_ns())
    out = TASK / 'logs' / run_id
    out.mkdir(parents=True)
    before = manifest()
    save_before = digest(SAVE)  # hash only: never parse, copy, restore or load it.
    jobs = []
    issues = []
    artifacts = {}
    runner = TASK / 'verify_ui.py'
    if save_before != EXPECTED_SAVE:
        issues.append('Protected save differs from approved baseline; run refused')
    else:
        for kind, flags in [('domain', ['--host']), ('browser', [])]:
            label = run_id + '-' + kind
            command = [sys.executable, str(runner), '--label', label, *flags]
            logfile = out / (kind + '.log')
            started = time.monotonic()
            try:
                with logfile.open('w', encoding='utf8') as log:
                    result = subprocess.run(command, cwd=ROOT, stdout=log, stderr=log, timeout=900)
                code = result.returncode
            except subprocess.TimeoutExpired:
                code = 124
                issues.append(kind + ' timeout; inspect owned runner before retrying')
            jobs.append({'kind': kind, 'command': command, 'exitCode': code,
                         'seconds': time.monotonic() - started})
            evidence = TASK / 'logs' / ('verified-' + label)
            for p in [logfile, evidence / ('host-results.json' if kind == 'domain' else 'results.json'),
                      evidence / 'source-manifest.json']:
                if p.is_file():
                    artifacts[p.relative_to(ROOT).as_posix()] = digest(p)
            try:
                if kind == 'browser':
                    issues.extend(validate_cases(json.loads((evidence / 'results.json').read_text(encoding='utf8'))))
                else:
                    suites = json.loads((evidence / 'host-results.json').read_text(encoding='utf8'))
                    names = [s.get('suite') for s in suites]
                    expected = {p.stem for p in (ROOT / 'android/app/src/test/java').rglob('Package*Test.java')}
                    expected.update({'ChartHistoryRangeTest', 'OrderReceiptActualsTest', 'OrderMatrixCombinationsTest',
                                     'OrderCapacityQuoteTest', 'MarketClosedOrderQueueRegressionTest',
                                     'Plan15SaveAndRecoveryRegressionTest', 'StartupPerformanceRegressionTest', 'SaveWritersConcurrencyRegressionTest'})
                    if not suites or len(names) != len(set(names)) or not expected.issubset(names):
                        issues.append('Missing/duplicate required domain suites')
                    if any(s.get('exitCode') != 0 for s in suites):
                        issues.append('Domain suite failure')
            except (OSError, ValueError, TypeError, KeyError) as error:
                issues.append('Missing/invalid ' + kind + ' evidence: ' + str(error))
            print(kind, 'PASS' if code == 0 else 'FAIL', flush=True)
    if manifest() != before:
        issues.append('Inputs changed during run; result invalid')
    report = {'schema': 1, 'createdUtc': datetime.now(timezone.utc).isoformat(),
              'scope': 'Selected domain suites + controlled Chromium UI regressions; not device performance',
              'sourceManifest': before, 'saveBefore': save_before, 'saveAfter': digest(SAVE),
              'jobs': jobs, 'artifactHashes': artifacts, 'issues': issues,
              'openGates': ['T16: repeated comparable performance measurements require separate review',
                            'T17: current APK/source equivalence and build evidence require separate review',
                            'Poco X8 Pro: physical frame/input/memory measurements and 60-minute session',
                            'Any UI/product decision still awaiting owner approval in active plan'],
              'fullPlanComplete': False}
    report['regressionStatus'] = 'FAIL' if validate_report(report, manifest()) else 'PASS'
    path = out / 'gate.json'
    path.write_text(json.dumps(report, indent=2), encoding='utf8')
    print('Evidence:', path)
    print('Regression:', report['regressionStatus'], '| Full plan: NOT VERIFIED')
    return 0 if report['regressionStatus'] == 'PASS' else 1


def check_gate(path, full):
    path = path.resolve()
    if not path.is_relative_to((TASK / 'logs').resolve()):
        raise ValueError('Gate report must be inside this task logs directory')
    report = json.loads(path.read_text(encoding='utf8'))
    issues = validate_report(report, manifest())
    if not report.get('artifactHashes'):
        issues.append('No hashed execution artifacts')
    for relative, expected in report.get('artifactHashes', {}).items():
        try:
            artifact = task_file(relative)
            if digest(artifact) != expected:
                issues.append('Evidence was modified: ' + relative)
            if artifact.name == 'results.json':
                issues.extend(validate_cases(json.loads(artifact.read_text(encoding='utf8'))))
            if artifact.name == 'host-results.json':
                suites = json.loads(artifact.read_text(encoding='utf8'))
                if not suites or any(s.get('exitCode') != 0 for s in suites):
                    issues.append('Domain execution artifact records failure')
        except (OSError, ValueError) as error:
            issues.append(str(error))
    if digest(SAVE) != report.get('saveAfter'):
        issues.append('Protected save changed after run')
    if issues:
        print('\n'.join(sorted(set(issues))))
        return 1
    print('PASS: current source-bound regression evidence')
    if full:
        print('NEEDS_VALIDATION: this gate does not certify full-plan completion')
        print('\n'.join(report.get('openGates', [])))
        return 2
    return 0


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest='action', required=True)
    sub.add_parser('run', help='compile and execute selected Java + actual-browser regressions')
    check = sub.add_parser('check', help='reject stale source, altered artifacts and unsupported completion claims')
    check.add_argument('report', type=Path)
    check.add_argument('--claim', choices=['regression', 'full'], default='regression')
    args = parser.parse_args()
    try:
        return run_gate() if args.action == 'run' else check_gate(args.report, args.claim == 'full')
    except (OSError, ValueError, TypeError, KeyError) as error:
        print('FAIL:', error)
        return 1


if __name__ == '__main__':
    sys.exit(main())
