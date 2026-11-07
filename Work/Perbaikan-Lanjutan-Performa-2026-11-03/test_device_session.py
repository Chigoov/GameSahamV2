"""Host-only APK identity check; never contacts a device."""
import hashlib
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import device_session


class DeviceSessionIdentityTest(unittest.TestCase):
    def test_only_matching_installed_qa_apk_is_accepted(self):
        with tempfile.TemporaryDirectory() as directory:
            out = Path(directory)
            (out / 'installed-base.apk').write_bytes(b'QA expected')
            expected = hashlib.sha256(b'QA expected').hexdigest()
            with patch.object(device_session, 'OUT', out), patch.object(device_session, 'adb') as adb:
                adb.side_effect = lambda *args, **kwargs: (
                    'package:/data/app/qa/base.apk' if args[:3] == ('shell', 'pm', 'path') else '')
                self.assertEqual(expected, device_session.verify_installed_apk(expected))
                with self.assertRaisesRegex(RuntimeError, 'differs'):
                    device_session.verify_installed_apk('wrong-hash')
                adb.return_value = ''
                adb.side_effect = None
                with self.assertRaisesRegex(RuntimeError, 'missing or ambiguous'):
                    device_session.verify_installed_apk(expected)
                self.assertTrue(all('com.tradingglobal3d.mobile' not in call.args for call in adb.call_args_list))


if __name__ == '__main__':
    unittest.main()
