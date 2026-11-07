param([Parameter(Mandatory)][string]$Reference)
$ErrorActionPreference = 'Stop'
$ref = (Resolve-Path -LiteralPath $Reference).Path
$repo = (git rev-parse --show-toplevel).Trim().Replace('/', '\')
if (!$ref.StartsWith($repo + '\Artifacts\', [StringComparison]::OrdinalIgnoreCase)) {
    throw 'Reference must be an isolated run inside this checkout'
}
$out = Join-Path (Split-Path $ref -Parent) ('repeated-checkpoint-30-' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff'))
New-Item -ItemType Directory -Path "$out/probe" | Out-Null
$command = Get-Content "$ref/bridge-30-reload-command.json" -Raw | ConvertFrom-Json
$command.args[2] = "$out/probe;" + $command.args[2]
$command.args[4] = "$out/qa-save"
$command.args[6] = 'restart'
$javaDir = Split-Path $command.exe
& "$javaDir/javac.exe" -cp $command.args[2] -d "$out/probe" "$PSScriptRoot/ColdResumeProbe.java"
if ($LASTEXITCODE) { throw 'Restart probe compilation failed' }
Copy-Item -Recurse -LiteralPath "$ref/seed" -Destination "$out/qa-save"
Get-FileHash -LiteralPath "$PSScriptRoot/ColdResumeProbe.java", "$PSCommandPath", "$PSScriptRoot/compare-resume-economy.py", "$out/qa-save/save_game_v2.json" |
    ConvertTo-Json | Set-Content "$out/input-hashes.json"
$results = @()
Push-Location $command.cwd
try {
    for ($attempt = 1; $attempt -le 6; $attempt++) {
        $command | ConvertTo-Json | Set-Content "$out/attempt-$attempt-command.json"
        $runArgs = $command.args
        & $command.exe @runArgs 2>&1 | Tee-Object "$out/attempt-$attempt.log"
        $code = $LASTEXITCODE
        $results += @{ attempt = $attempt; exitCode = $code; primaryHash = (Get-FileHash "$out/qa-save/save_game_v2.json").Hash }
        $results | ConvertTo-Json | Set-Content "$out/attempts.json"
        if ($code -eq 0) { break }
        if ($code -ne 71) { throw "Unexpected restart failure $code" }
    }
    if ($code -ne 0) { throw 'Repeated restart did not finish' }
    $expected = (Get-Content "$ref/bridge-30.log" | Where-Object { $_ -like 'state *' }) -replace ' ledgerSha256=.*$'
    $actual = (Get-Content "$out/attempt-$attempt.log" | Where-Object { $_ -like 'state *' }) -replace ' ledgerSha256=.*$'
    if (!$actual -or $expected -cne $actual) { throw 'Repeated restart economics differ' }
    $true | Set-Content "$out/economy-match.txt"
} finally { Pop-Location }
python "$PSScriptRoot/compare-resume-economy.py" "$ref/bridge-30/save_game_v2.json" "$out/qa-save/save_game_v2.json" 2>&1 |
    Tee-Object "$out/market-match.json"
$code = $LASTEXITCODE
$code | Set-Content "$out/market-match-exit.txt"
if ($code) { throw 'Repeated restart market or position state differs' }
