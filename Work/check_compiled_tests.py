import glob, os

classes_dir = 'Artifacts/Test-Plan14-Full/classes'
test_files = glob.glob('android/app/src/test/java/**/*Test.java', recursive=True)
present = []
missing = []
for tf in test_files:
    norm = tf.replace(os.sep, '/')
    idx = norm.find('com/')
    if idx >= 0:
        cls_name = norm[idx:].replace('/', '.').replace('.java', '')
        cls_file = os.path.join(classes_dir, norm[idx:].replace('/', os.sep).replace('.java', '.class'))
        if not os.path.exists(cls_file):
            missing.append(cls_name)
        else:
            present.append(cls_name)
print('Present:', len(present))
print('Missing:', len(missing), missing[:5])
