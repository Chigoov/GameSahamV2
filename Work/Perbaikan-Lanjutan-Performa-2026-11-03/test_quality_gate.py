"""Negative controls: a green claim cannot hide missing tests or changed inputs."""
import unittest
import json
from pathlib import Path
from tempfile import TemporaryDirectory
from unittest.mock import patch
import quality_gate as gate
from quality_gate import EXPECTED_SAVE, REQUIRED, validate_cases, validate_report


class GateNegativeControls(unittest.TestCase):
    def test_missing_failed_duplicate_and_page_errors_are_rejected(self):
        good = {'cases': [{'case': name, 'status': 'PASS'} for name in sorted(REQUIRED)], 'pageErrors': []}
        self.assertEqual([], validate_cases(good))
        self.assertTrue(validate_cases({'cases': good['cases'][:-1]}))
        self.assertTrue(validate_cases({'cases': good['cases'] + [good['cases'][0]]}))
        failed = {'cases': [dict(c) for c in good['cases']]}
        failed['cases'][0]['status'] = 'FAIL'
        self.assertTrue(validate_cases(failed))
        self.assertTrue(validate_cases({**good, 'pageErrors': ['boom']}))

    def test_source_drift_failed_runner_and_save_drift_are_rejected(self):
        source = {'app.js': 'original'}
        good = {'schema': 1, 'sourceManifest': source, 'saveBefore': EXPECTED_SAVE,
                'saveAfter': EXPECTED_SAVE, 'jobs': [{'kind': k, 'exitCode': 0} for k in ('domain', 'browser')], 'issues': []}
        self.assertEqual([], validate_report(good, source))
        self.assertTrue(validate_report(good, {'app.js': 'edited'}))
        self.assertTrue(validate_report({**good, 'jobs': [{'kind': 'domain', 'exitCode': 1}]}, source))
        self.assertTrue(validate_report({**good, 'saveAfter': 'changed'}, source))

    def test_green_regression_never_certifies_full_plan(self):
        # Synthetic verifier input only, never written as execution evidence.
        with TemporaryDirectory() as temporary:
            task = Path(temporary)
            (task / 'logs').mkdir()
            path = task / 'logs/gate.json'
            source = {'app.js': 'original'}
            report = {'schema': 1, 'sourceManifest': source, 'saveBefore': EXPECTED_SAVE,
                      'saveAfter': EXPECTED_SAVE, 'jobs': [{'kind': k, 'exitCode': 0} for k in ('domain', 'browser')],
                      'issues': [], 'artifactHashes': {'logs/result.log': 'hash'}, 'openGates': ['device']}
            path.write_text(json.dumps(report), encoding='utf8')
            with patch.object(gate, 'TASK', task), patch.object(gate, 'manifest', return_value=source), \
                 patch.object(gate, 'task_file', return_value=task / 'result.log'), \
                 patch.object(gate, 'digest', side_effect=lambda p: EXPECTED_SAVE if p == gate.SAVE else 'hash'):
                self.assertEqual(0, gate.check_gate(path, False))
                self.assertEqual(2, gate.check_gate(path, True))


if __name__ == '__main__':
    unittest.main()
