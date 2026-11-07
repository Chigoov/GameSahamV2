param(
    [Parameter(Mandatory)][string]$Compiled,
    [switch]$Traded,
    [switch]$Combined,
    [switch]$SecondReopen,
    [ValidateRange(384, 1536)][int]$HeapMiB = 384,
    [string[]]$Cases = @('bridge-7', 'domain-7', 'bridge-1', 'bridge-2', 'bridge-30', 'domain-30')
)
$ErrorActionPreference = 'Stop'
$repo = (git rev-parse --show-toplevel).Trim()
$compiledRoot = (Resolve-Path -LiteralPath $Compiled).Path
$fixtureRoot = (Get-ChildItem -LiteralPath $compiledRoot -Directory -Filter 'test-run-*' | Select-Object -Last 1).FullName
$out = Join-Path $compiledRoot ('cold-resume-' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff'))
New-Item -ItemType Directory -Path "$out/probe" | Out-Null
$javaBin = 'C:/Program Files/Android/Android Studio/jbr/bin'
$jars = @(rg --files "$env:USERPROFILE/.gradle/caches/modules-2/files-2.1" | Where-Object { $_ -match '(junit-4.13.2|hamcrest-core-1.3|json-20240303)\.jar$' })
$androidJar = "$env:LOCALAPPDATA/Android/Sdk/platforms/android-36/android.jar"
$cp = (@("$out/probe", "$compiledRoot/classes") + $jars + $androidJar) -join ';'
$probe = Join-Path $PSScriptRoot 'ColdResumeProbe.java'
$catalog = Join-Path $fixtureRoot 'src/main/assets/data/market_universe.json'
$heap = "-Xmx${HeapMiB}m"
$sourceHashes = Join-Path $compiledRoot 'source-hashes.json'
git rev-parse HEAD | Set-Content "$out/head.txt"
$inputs = @($probe, $PSCommandPath, $sourceHashes, "$repo/Work/run-codex-jvm-check.ps1", "$javaBin/java.exe", $androidJar) +
    $jars + @(rg --files -uuu "$fixtureRoot/src/main/assets/data" "$compiledRoot/classes")
Get-FileHash -LiteralPath $inputs | ConvertTo-Json | Set-Content "$out/input-hashes.json"
& "$javaBin/javac.exe" -cp $cp -d "$out/probe" $probe
if ($LASTEXITCODE) { throw 'Probe compilation failed' }
Push-Location $fixtureRoot
try {
    $seedMode = if ($Combined) { 'seed-combined' } elseif ($Traded) { 'seed-traded' } else { 'seed' }
    $initial = & "$javaBin/java.exe" $heap -cp $cp ColdResumeProbe "$out/seed" 0 $seedMode $catalog
    if ($LASTEXITCODE) { throw 'Seed failed' }
    $initial | Set-Content "$out/seed-timestamp.txt"
    Get-FileHash "$out/seed/save_game_v2.json" | ConvertTo-Json | Set-Content "$out/seed-hash.json"
    $failedCases = @()
    foreach ($case in $Cases) {
        $mode, $days = $case.Split('-')
        if ($mode -notin @('bridge', 'domain', 'interrupt') -or $days -notmatch '^[0-9]+$') { throw "Invalid case $case" }
        Copy-Item -Recurse -LiteralPath "$out/seed" -Destination "$out/$case"
        $gcLog = "../$(Split-Path $out -Leaf)/gc-$case.log"
        $flags = @($heap, "-Xlog:gc*:file=$gcLog",
            "-XX:StartFlightRecording=filename=$out/$case.jfr,settings=profile,dumponexit=true,jdk.JavaErrorThrow#enabled=true")
        $argsRun = $flags + @('-cp', $cp, 'ColdResumeProbe', "$out/$case", $days, $mode, $catalog, $initial)
        @{ exe = "$javaBin/java.exe"; cwd = $fixtureRoot; args = $argsRun } |
            ConvertTo-Json | Set-Content "$out/$case-command.json"
        $ErrorActionPreference = 'Continue'
        & "$javaBin/java.exe" @argsRun 2>&1 | Tee-Object "$out/$case.log"
        $LASTEXITCODE | Set-Content "$out/$case-exit.txt"
        $ErrorActionPreference = 'Stop'
        if ($mode -eq 'interrupt') {
            if ((Get-Content "$out/$case-exit.txt") -ne '71') { throw 'Expected halt at second commit' }
            $argsRun = @($heap, '-cp', $cp, 'ColdResumeProbe', "$out/$case", $days, 'bridge', $catalog, $initial)
            @{ exe = "$javaBin/java.exe"; cwd = $fixtureRoot; args = $argsRun } |
                ConvertTo-Json | Set-Content "$out/$case-recover-command.json"
            $ErrorActionPreference = 'Continue'
            & "$javaBin/java.exe" @argsRun 2>&1 | Tee-Object "$out/$case-recover.log"
            $LASTEXITCODE | Set-Content "$out/$case-recover-exit.txt"
            $ErrorActionPreference = 'Stop'
            if ((Get-Content "$out/$case-recover-exit.txt") -ne '0') { throw 'Restart from checkpoint failed' }
            $referenceLog = "$out/bridge-$days.log"
            if (Test-Path -LiteralPath $referenceLog) {
                $reference = Get-Content $referenceLog | Where-Object { $_ -like 'state *' }
                $recovered = Get-Content "$out/$case-recover.log" | Where-Object { $_ -like 'state *' }
                # Independent runs generate new transaction IDs. Compare the economic state without their hashes.
                $equal = [bool]$reference -and ($reference -replace ' ledgerSha256=.*$') -ceq ($recovered -replace ' ledgerSha256=.*$')
                $equal | Set-Content "$out/$case-reference-match.txt"
                if (!$equal) { throw 'Interrupted run differs from uninterrupted reference' }
            }
            continue
        }
        if ((Get-Content "$out/$case-exit.txt") -eq '0') {
            $argsRun = @($heap, '-cp', $cp, 'ColdResumeProbe', "$out/$case", $days, 'reload', $catalog, $initial)
            @{ exe = "$javaBin/java.exe"; cwd = $fixtureRoot; args = $argsRun } |
                ConvertTo-Json | Set-Content "$out/$case-reload-command.json"
            $ErrorActionPreference = 'Continue'
            & "$javaBin/java.exe" @argsRun 2>&1 | Tee-Object "$out/$case-reload.log"
            $LASTEXITCODE | Set-Content "$out/$case-reload-exit.txt"
            $ErrorActionPreference = 'Stop'
            if ((Get-Content "$out/$case-reload-exit.txt") -eq '0') {
                $before = Get-Content "$out/$case.log" | Where-Object { $_ -like 'state *' }
                $after = Get-Content "$out/$case-reload.log" | Where-Object { $_ -like 'state *' }
                $equal = [bool]$before -and $before -ceq $after
                $equal | Set-Content "$out/$case-round-trip.txt"
                if (!$equal) { throw "Round-trip state differs for $case" }
                if ($SecondReopen) {
                    $argsRun[6] = 'bridge'
                    @{ exe = "$javaBin/java.exe"; cwd = $fixtureRoot; args = $argsRun } |
                        ConvertTo-Json | Set-Content "$out/$case-second-reopen-command.json"
                    $ErrorActionPreference = 'Continue'
                    & "$javaBin/java.exe" @argsRun 2>&1 | Tee-Object "$out/$case-second-reopen.log"
                    $LASTEXITCODE | Set-Content "$out/$case-second-reopen-exit.txt"
                    $ErrorActionPreference = 'Stop'
                    $reopened = Get-Content "$out/$case-second-reopen.log" | Where-Object { $_ -like 'state *' }
                    if ((Get-Content "$out/$case-second-reopen-exit.txt") -ne '0' -or $before -cne $reopened) {
                        throw "Second reopen differs for $case"
                    }
                }
            } else {
                $failedCases += "$case-reload"
            }
        } else {
            $failedCases += $case
        }
    }
    if ($failedCases.Count) { throw "Cold resume cases failed: $($failedCases -join ', ')" }
} finally { Pop-Location }
