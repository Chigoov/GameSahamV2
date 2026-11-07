# Checkpoint10 — margin warning and large-save memory

Goal active, not complete. Previous09 and this10 are progress. Browser blocker resumed fresh: CUA listTabs reports Local browser use is disabled in Work settings this turn. Do not bypass settings or mark blocked from this first repeated condition in the new audit. No new screenshots; earlier09 screenshots remain evidence only of that source/state.

## Production changes

- openRiskModal previously rendered remaining minutes once, so an open screen retained120minutes while time passed. updateMarginDeadline now uses remaining milliseconds from domain plus performance.now elapsed, updated by the existing250ms presenter timer. Deadline date remains domain-sourced; expiry says to check liquidation status, not that liquidation has definitely completed. A refresh-status button reopens risk data.
- getSnapshotJson includes marginCall deadlineRealAt/remainingRealMs from RiskComplianceService. Main-content receives one actionable warning button while deadline is nonzero; it opens risk management, changes wording after expiry and disappears when domain clears call. It does not create a call, extend deadline or execute liquidation. Check covers creation, reuse, action, expiry and removal. UI visual review still pending due browser settings.
- Work/WireQaServer adds QA-only seedMarginCall: fresh debt-free account, synthetic CIO prerequisite and ledger-backed distressed BBCA holding. It creates a real domain margin deadline then saves. Fixture label explicitly synthetic, not progression evidence. Server64137 provides a reproducible UI state for next browser review.

## Newly reproduced save failure and fix

The main64134 process from09 was missing in live CIM. Restarting its existing QA save at384MB failed OutOfMemoryError at SaveManager.save line125: a second full JSONObject(existingContent) was constructed solely to retrieve savedTimestamp, despite that file already having passed validation/digest check. File length39,130,135bytes, backup39,038,737bytes. No player save was opened beyond hash.

SaveManager now stores the validated timestamp beside the existing SHA256 cache. Identical bytes under the same validator reuse both. Changed bytes still call readValidatedState and obtain timestamp from that validated state. Generation fencing still rejects older snapshots; invalid primary still cannot replace good backup. Backup copies validated disk file via Files.copy under existing process commit lock, rather than allocating another full UTF8bytearray from the existing string. No schema change, no ledger/history truncation or data deletion. Cache timestamp is set only after a successful atomic primary commit or valid primary load; invalidating digest makes timestamp unusable as before.

A copy of the large QA directory was made at Artifacts/Annotation-Execution/qa-save-large-memory-check-01. OriginalQA preserved for diagnosis. With fixed source and384MB, copy resumed successfully; two actual bridge.saveGame calls both success, primary39,256,969bytes. Evidence browser-resume-2026-11-04/large-save-memory-proof.json. This proves the reported size and flow, not unlimited save growth or Android runtime.

## Verification

- updateMarginDeadline check:120→119minutes after60000elapsed; expiry text atzero; missing node safe. Warning check adds/reuses/removes only one button and invokes openRiskModal. Existing check-research-firm-ui.cjs passed.
- OwnerMarginDeadlineTest now also verifies snapshot warning deadline and7200000remaining at initialcall. Full deadline/persistence/clockback/forced-fill tests retained.
-22tests after warning before memory fix:1793785220583423200/test.log, exit0.
-25tests after memory fix:1793785341760860600/test.log, exit0; includes SaveWritersConcurrencyRegressionTest and OwnerMarginDeadlineTest.
-32tests final:1793785402736316100/test.log, exit0,9.735s; OwnerAnnotationPolicyTest16, SaveBackupCorruptionTest, Plan15SaveAndRecoveryRegressionTest, SaveWritersConcurrencyRegressionTest, OwnerMarginDeadlineTest. No sourceJava changes after final run.
- Node syntax and chart-controls/market-ipo/research-firm checks exit0; diff-check relevant source exit0.
- Final build session32770 terminal exit0, BUILD SUCCESSFUL13s. APKsha320ca2dcd6aed0e66ed528b8bbc6443d5a1e219a5ac72b421c9f50be47db8fc7.19web/logo/provenance/license files match ZIP. Main64134 HTTPJS/CSS match. source-apk-parity-10.json stores hashes and marginsnapshot. Earlier build98972terminal14s predates memoryfix; do not call that latest.
- Save player hash remains59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED.

## Live processes and recovery detail

An initial restart script expected main+margin2processes but found only margin; guard exited before stopping anything. A subsequent attempted margin start failed bind because old margin was still alive. CIM identified exact task-owned2968 for64137, which was then stopped/restarted. These were not three identical failures or reason to loop/restart without diagnosis.

- Main preview64134 session15462, classes1793785341760860600, latest productionJava includingSaveManagerfix, Work harness latest. Save is now qa-save-large-memory-check-01 (preserved copy of old preview); not qa-save-current-preview. Confirmed159API and repeated save success. Final32testclasses are a separate compilation of identical source, no need restart purely to change directoryname.
- Margin64137 session61644, classes1793785220583423200: includes warning snapshot, before SaveManagermemoryfix. QA saveqa-save-margin-ui-20261104. Latest HTTPsnapshot margin deadline1793792342407 andremaining6890970atobservation. This is domain fixture proof, not screenshot.
-64135 process3860 and64136process14316 were observed alive at CIM; neither updated this turn. They have olderJava; read09/08 before using them. Avoid restarting64135 without preserving its artificial realClock offset semantics.
- No build/JUnit pending. Servers15462/61644 are long-running. Browser local currentlydisabled; no markDeliverable/viewport changes this turn. NoADB/install/push/commit/global settings changed.

## Remaining work

E03 logo coverage still18emiten. Additional downloads previously429; no attempt this turn. E10 new warning/countdown needs rendered QA once browser access returns. E04 pinch still contract only, not physical multi-touch. Audit full E01–E11 against implementation-plan remains required; existing tests/APK never substitute for missing visual/state evidence. Next safe action: resume browser on main64134 and marginfixture64137 when enabled, verify warning→risk→countdown→repayment/recovery, then continue missing logos/provenance and final coverage. Keep goal active; do not mark complete or revive old blocked count.
