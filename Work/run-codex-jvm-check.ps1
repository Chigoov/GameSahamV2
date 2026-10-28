param([string[]]$Tests, [string]$OutputDirectory = 'Artifacts/Codex-Resume-2026-10-20')
$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
if ((git -C $repo rev-parse --show-toplevel).Replace('\','/') -ne $repo.Replace('\','/')) { throw 'Wrong production root' }
Push-Location $repo
try {
    $output = [IO.Path]::GetFullPath((Join-Path $repo $OutputDirectory))
    if (!$output.StartsWith($repo + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { throw 'Output must stay inside production root' }
    New-Item -ItemType Directory -Force -Path "$output/classes" | Out-Null
    $javaBin = 'C:/Program Files/Android/Android Studio/jbr/bin'
    $jars = @(rg --files "$env:USERPROFILE/.gradle/caches/modules-2/files-2.1" | Where-Object { $_ -match '(junit-4.13.2|hamcrest-core-1.3|json-20240303)\.jar$' })
    if ($jars.Count -ne 3) { throw 'Expected existing JUnit, Hamcrest and JSON jars' }
    $cp = ($jars + "$env:LOCALAPPDATA/Android/Sdk/platforms/android-36/android.jar") -join ';'
    $sources = @(rg --files android/app/src/main/java/com/tradingglobal3d/core -g '*.java') + @('android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java', 'android/app/src/main/java/com/tradingglobal3d/mobile/MainActivity.java') + @(rg --files android/app/src/test/java -g '*.java')
    $sources | ForEach-Object { '"'+(Join-Path $repo $_).Replace('\','/')+'"' } | Set-Content "$output/sources.txt"
    & "$javaBin/javac.exe" -J-Xmx256m -encoding UTF-8 -cp $cp -d "$output/classes" "@$output/sources.txt"
    if ($LASTEXITCODE -ne 0) { throw "Compilation failed: $LASTEXITCODE" }
    if (!$Tests) { $Tests = @(rg --files android/app/src/test/java -g '*Test.java' | ForEach-Object { $_.Substring($_.IndexOf('com\')).Replace('\','.').Replace('.java','') }) }
    # ponytail: isolate hardcoded relative test outputs and temp saves without changing domain tests.
    $sandbox = Join-Path $output ('test-run-' + [Guid]::NewGuid().ToString('N'))
    New-Item -ItemType Directory -Force -Path "$sandbox/src/main/assets", "$sandbox/tmp" | Out-Null
    Copy-Item -LiteralPath "$repo/android/app/src/main/assets/data" -Destination "$sandbox/src/main/assets/data" -Recurse
    Set-Location $sandbox
    # Expected recovery diagnostics on native stderr must not abort JUnit before its exit code.
    $previousErrorAction = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        & "$javaBin/java.exe" -Xmx384m "-Djava.io.tmpdir=$sandbox/tmp" -cp "$output/classes;$cp" org.junit.runner.JUnitCore @Tests 2>&1 | Tee-Object "$output/junit-latest.log"
        $testExit = $LASTEXITCODE
    } finally { $ErrorActionPreference = $previousErrorAction }
    if ($testExit -ne 0) { throw "JUnit failed: $testExit" }
} finally { Pop-Location }
