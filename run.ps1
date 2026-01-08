
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

# Set JAVA_HOME to JDK 17 if available
$jdk17Path = "C:\d\dev\jdk-17"
if (Test-Path $jdk17Path) {
    $env:JAVA_HOME = $jdk17Path
    $env:Path = "$jdk17Path\bin;$env:Path"
    Write-Host "JAVA_HOME set to $jdk17Path" -ForegroundColor Cyan
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
