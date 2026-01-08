# HsmCrypt Multi-Module Build Script
# Builds both hsmcrypt library and hsmcrypt-example

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "HsmCrypt Multi-Module Build" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set JAVA_HOME to JDK 1.8 if available
$jdk8Path = "C:\d\dev\jdk-8x64"
if (Test-Path $jdk8Path) {
    $env:JAVA_HOME = $jdk8Path
    $env:Path = "$jdk8Path\bin;$env:Path"
    Write-Host "JAVA_HOME set to $jdk8Path" -ForegroundColor Cyan
} else {
    Write-Host "Warning: JDK 1.8 not found at $jdk8Path. Using system default JAVA_HOME." -ForegroundColor Yellow
}
Write-Host ""


$modules = @("hsmcrypt", "hsmcrypt-example")
$buildCommand = "mvn clean install -DskipTests"

$successCount = 0
$failedModules = @()

foreach ($module in $modules) {
    $moduleNumber = $modules.IndexOf($module) + 1
    Write-Host "[$moduleNumber/$($modules.Count)] Building module: $module" -ForegroundColor Yellow
    Write-Host "Command: $buildCommand" -ForegroundColor Gray
    Write-Host ""
    
    Push-Location
    try {
        Set-Location $module
        
        Invoke-Expression $buildCommand
        
        if ($LASTEXITCODE -ne 0) {
            throw "Build failed with exit code $LASTEXITCODE"
        }
        
        Write-Host ""
        Write-Host "✓ $module - Build successful" -ForegroundColor Green
        Write-Host ""
        $successCount++
    }
    catch {
        Write-Host ""
        Write-Host "✗ $module - Build failed: $_" -ForegroundColor Red
        Write-Host ""
        $failedModules += $module
    }
    finally {
        Pop-Location
    }
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Build Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Total modules: $($modules.Count)" -ForegroundColor White
Write-Host "Successful: $successCount" -ForegroundColor Green
Write-Host "Failed: $($failedModules.Count)" -ForegroundColor $(if ($failedModules.Count -gt 0) { "Red" } else { "Green" })

if ($failedModules.Count -gt 0) {
    Write-Host ""
    Write-Host "Failed modules:" -ForegroundColor Red
    foreach ($module in $failedModules) {
        Write-Host "  - $module" -ForegroundColor Red
    }
    Write-Host ""
    exit 1
}

Write-Host ""
Write-Host "All modules built successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "To run the example application:" -ForegroundColor Cyan
Write-Host "  cd hsmcrypt-example" -ForegroundColor White
Write-Host "  .\run.ps1" -ForegroundColor White
Write-Host ""
