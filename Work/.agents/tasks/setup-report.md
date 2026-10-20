# Environment Setup & Baseline Verification Report

**Date:** 2026-10-20  
**Project:** Game Saham 2D Portrait  
**Workspace Root:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM` (Exclusive write target)  
**Agent:** Setup Agent (workflow `wf_d482a4ef826b4c54`)

---

## 1. Directory Structure Verification

- **Task Directory:** `Work/.agents/tasks`
- **Status:** Verified & Created via `New-Item -ItemType Directory -Force -Path "Work/.agents/tasks"`
- **Path:** `C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM\Work\.agents\tasks`

---

## 2. Git Status and Working Tree Baseline

- **Current Branch:** `codex/owner-audit-portrait-v8`
- **HEAD Commit:** `5fe86fb`
- **Commit Message:** `feat(integration): integrate mutual funds, installment loans, periodic missions, league 20 NPC, profile & archetype into facade, bridge and UI; verify all R0-R7 milestones with 296 JVM tests and fresh APK candidate`
- **Working Tree:** Clean (`nothing to commit, working tree clean`)
- **Safety Policy:**
  - `git reset --hard`, `git clean`, and `git checkout --force` were NOT executed and are strictly prohibited.
  - All local changes and untracked artifacts are preserved.

---

## 3. Personal Save File Protection

- **File Path:** `Artifacts/LocalPreview/preview-save/save_game_v2.json`
- **File Existence:** Verified present
- **File Size:** 36,000 bytes
- **SHA-256 Hash:** `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`
- **Protection Policy:**
  - Critical player save data.
  - Must remain strictly untouched across all steps, tests, and preview servers.
  - Never modify, overwrite with test fixtures, or delete this file.

---

## 4. Build and Test Runner Environment

### Java & Gradle Environment
- **JAVA_HOME Configuration:**
  - Default environment did not export `java` into user PATH.
  - Located Android Studio JetBrains Runtime (JBR) OpenJDK 21 at:
    `C:\Program Files\Android\Android Studio\jbr`
  - JVM Version: OpenJDK 21.0.10 (build 21.0.10+-14961533-b1163.108), 64-Bit Server VM.
  - When configuring commands, set `$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"` and prepend `$env:JAVA_HOME\bin` to PATH.
- **Gradle Version Output (`android/gradlew.bat -p android --version`):**
  - Gradle: 8.14.3
  - Kotlin: 2.0.21
  - Groovy: 3.0.24
  - Ant: Apache Ant 1.10.15
  - Launcher JVM: 21.0.10 (JetBrains s.r.o.)
  - OS: Windows 11 10.0 amd64

### Unit Test Verification
- Executed: `android/gradlew.bat -p android testDebugUnitTest --rerun-tasks`
- Outcome: **BUILD SUCCESSFUL**
- Test Results:
  - Total Suites: 26
  - Total Tests: 296
  - Failures: 0
  - Errors: 0
  - Skipped: 0

### Auxiliary Tooling
- Python: Python 3.14.5
- Node.js: v24.16.0
- Materials Verification:
  - Executed: `python Work/Hermes-Feature-Completion-2026-10-20/verify_package.py --self-test`
  - Result: `materials_status: PASS`, 148 delta rows, 26 reference groups, 277 existing seed rows, 228 existing PRD rows, 9 audit findings, 5 owner answers.

---

## 5. QA Preview Ports & Process Check

The standard preview and QA port assignments:
- **Port 8766:** Personal preview server (`http://127.0.0.1:8766/`, save `Artifacts/LocalPreview/preview-save/save_game_v2.json`)
- **Port 8767:** QA Java host server (`http://127.0.0.1:8767/`, isolated test fixture)
- **General Web/Dev Ports:** 8000, 8080, 3000, 5000, 5173, 8081

**Inspection Result:**
- Evaluated via `Get-NetTCPConnection -State Listen`.
- All target ports (8766, 8767, 8000, 8080, 3000, 5000, 5173, 8081) are completely idle and free of conflicting processes.

---

## 6. Independent Review Artifacts Integrity

Directory: `Artifacts/Independent-Review-2026-10-20/`

All original independent review files are verified intact and recorded below:
- `apk-signature.txt`: 351 bytes | SHA-256: `B9C6879E48D25A9786D2BEB8A15738C06EDCE7D06F2722DD860894618592BD09`
- `AuditProbe.java`: 3,683 bytes | SHA-256: `84703B2F4049F1E0692D6CEB63E1D3F673266BF9A194D079CFE4853F5EE454ED`
- `jvm-rerun.log`: 2,287 bytes | SHA-256: `AFB03A59E0C64333F474E6DAFBEBBECE55C43D30BAEA00530529EC256B1D06DF`
- `probe-results.json`: 653 bytes | SHA-256: `BD2C3E62402E060904681A1676E1508A70EEBA3821CD84834BAC78AAFF083287`
- `REPORT.md`: 12,105 bytes | SHA-256: `8179B0A578EEAC20A92293AA8DCEB9DC15675DAFA2417A16E5984225D9EABC8D`
- `verification.json`: 1,385 bytes | SHA-256: `529FE0190BD6833E3376385C8D85FC642409164D36DCED9DF438C797037DB1DB`
- `classes/AuditProbe.class`: 6,611 bytes | SHA-256: `EFFF0805DEDC81119BAF21189364AE5DB4ED3E23580A7E64A5AC82CE85A3637A`

**Protection Guarantee:**
Original independent review files remain strictly read-only and must never be altered or overwritten.

---

## 7. Setup Summary

All 7 preflight tasks completed successfully. The environment is verified, stable, reproducible, and ready for workflow execution.
