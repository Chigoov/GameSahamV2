import subprocess
import re

classes = [
    'com.tradingglobal3d.core.Plan13FixesAndAuditsTest',
    'com.tradingglobal3d.core.Apk11IndependentReviewFixesTest',
    'com.tradingglobal3d.core.Apk10IndependentReviewFixesTest',
    'com.tradingglobal3d.core.NewGameAndResetRegressionTest',
    'com.tradingglobal3d.core.LoanFinancingPolicy2026RegressionTest',
    'com.tradingglobal3d.core.MarketClosedOrderQueueRegressionTest',
    'com.tradingglobal3d.core.OrderMatrixCombinationsTest',
    'com.tradingglobal3d.core.OrderReceiptActualsTest',
    'com.tradingglobal3d.core.NewsRealDeadlineTest',
    'com.tradingglobal3d.core.CompanyOperationsRegressionTest',
    'com.tradingglobal3d.core.CorporateActionIpoVotingTest',
    'com.tradingglobal3d.core.IpoAllotment6040AndCancellationTest',
    'com.tradingglobal3d.core.League1000NpcLeaderboardRegressionTest',
    'com.tradingglobal3d.core.CatalogExpansionRegressionTest',
    'com.tradingglobal3d.core.OwnerAuditRegressionTest',
    'com.tradingglobal3d.mobile.TradingWebBridgeTest',
    'com.tradingglobal3d.core.IpoScheduleAndAllotmentTest',
    'com.tradingglobal3d.core.ResearchNewsTerminalTest',
    'com.tradingglobal3d.core.WireIntegrationDomainTest'
]

total = 0
for cls in classes:
    cmd = f'powershell.exe -ExecutionPolicy Bypass -Command "& \'Work/run-codex-jvm-check.ps1\' -Tests @(\'{cls}\') -OutputDirectory \'Artifacts/CountTmp\'"'
    res = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    m = re.search(r'OK \((\d+) test', res.stdout)
    if m:
        c = int(m.group(1))
        print(f"{cls}: {c} tests")
        total += c
    else:
        print(f"FAILED on {cls}:\n{res.stdout}\n{res.stderr}")

print(f"\nTOTAL EXACT: {total}")
