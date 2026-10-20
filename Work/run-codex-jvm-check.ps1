param([string[]]$Tests)
$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
if ((git -C $repo rev-parse --show-toplevel).Replace('\','/') -ne $repo.Replace('\','/')) { throw 'Wrong production root' }
Push-Location $repo
try {
    $output = Join-Path $repo 'Artifacts/Codex-Resume-2026-10-20'
    New-Item -ItemType Directory -Force -Path "$output/classes" | Out-Null
    $javaBin = 'C:/Program Files/Android/Android Studio/jbr/bin'
    $jars = @(rg --files "$env:USERPROFILE/.gradle/caches/modules-2/files-2.1" | Where-Object { $_ -match '(junit-4.13.2|hamcrest-core-1.3|json-20240303)\.jar$' })
    if ($jars.Count -ne 3) { throw 'Expected existing JUnit, Hamcrest and JSON jars' }
    $cp = ($jars + "$env:LOCALAPPDATA/Android/Sdk/platforms/android-36/android.jar") -join ';'
    $sources = @(rg --files android/app/src/main/java/com/tradingglobal3d/core -g '*.java') + @('android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java') + @(rg --files android/app/src/test/java -g '*.java')
    $sources | ForEach-Object { '"'+(Join-Path $repo $_).Replace('\','/')+'"' } | Set-Content "$output/sources.txt"
    & "$javaBin/javac.exe" -encoding UTF-8 -cp $cp -d "$output/classes" "@$output/sources.txt"
    if ($LASTEXITCODE -ne 0) { throw "Compilation failed: $LASTEXITCODE" }
    if (!$Tests) { $Tests = @(rg --files android/app/src/test/java -g '*Test.java' | ForEach-Object { $_.Substring($_.IndexOf('com\')).Replace('\','.').Replace('.java','') }) }
    Set-Location android/app
    & "$javaBin/java.exe" -cp "$output/classes;$cp" org.junit.runner.JUnitCore @Tests 2>&1 | Tee-Object "$output/junit-latest.log"
    if ($LASTEXITCODE -ne 0) { throw "JUnit failed: $LASTEXITCODE" }
} finally { Pop-Location }
