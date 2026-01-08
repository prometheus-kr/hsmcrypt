
$ErrorActionPreference = "Stop"

# Run build first
Write-Host "Running build.ps1..." -ForegroundColor Cyan
& ".\build.ps1"

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "✗ Build failed. Aborting run." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Running Application" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$module = "hsmcrypt-example"
$runCommand = "mvn spring-boot:run"

# Set JAVA_HOME to JDK 21 if available
$jdk21Path = "C:\d\dev\jdk-21"
if (Test-Path $jdk21Path) {
    $env:JAVA_HOME = $jdk21Path
    $env:Path = "$jdk21Path\bin;$env:Path"
    Write-Host "JAVA_HOME set to $jdk21Path" -ForegroundColor Cyan
}
Write-Host ""

Push-Location
try {
    Set-Location $module
    
    Invoke-Expression $runCommand
    
    if ($LASTEXITCODE -ne 0) {
        throw "Run failed with exit code $LASTEXITCODE"
    }
    
    Write-Host ""
    Write-Host "✓ $module - Run successful" -ForegroundColor Green
}
catch {
    Write-Host ""
    Write-Host "✗ $module - Run failed: $_" -ForegroundColor Red
    Write-Host ""
    exit 1
}
finally {
    Pop-Location
}
