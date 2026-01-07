
$ErrorActionPreference = "Stop"

$module = "hsmcrypt-example"
$runCommand = "mvn spring-boot:run"

Push-Location
try {
    Set-Location $module
    
    Invoke-Expression $runCommand
    
    if ($LASTEXITCODE -ne 0) {
        throw "Run failed with exit code $LASTEXITCODE"
    }
    
    Write-Host ""
    Write-Host "✓ $module - Run successful" -ForegroundColor Green
    Write-Host ""
    $successCount++
}
catch {
    Write-Host ""
    Write-Host "✗ $module - Run failed: $_" -ForegroundColor Red
    Write-Host ""
    $failedModules += $module
}
finally {
    Pop-Location
}
