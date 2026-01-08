# HsmCrypt Multi-Module Build Script
# Builds both hsmcrypt library and hsmcrypt-example

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "HsmCrypt Multi-Module Build" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set JAVA_HOME to JDK 17 if available
$jdk17Path = "C:\d\dev\jdk-17"
if (Test-Path $jdk17Path) {
    $env:JAVA_HOME = $jdk17Path
    $env:Path = "$jdk17Path\bin;$env:Path"
    Write-Host "JAVA_HOME set to $jdk17Path" -ForegroundColor Cyan
} else {
    Write-Host "Warning: JDK 17 not found at $jdk17Path. Using system default JAVA_HOME." -ForegroundColor Yellow
}
Write-Host ""


$modules = @("hsmcrypt", "hsmcrypt-example")
$buildCommand = "mvn clean install -DskipTests"

$successCount = 0

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
        Pop-Location
        exit 1
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

Write-Host ""
Write-Host "All modules built successfully!" -ForegroundColor Green
Write-Host ""