$ErrorActionPreference = "Stop"

# 현재 스크립트가 위치한 경로
$baseDir = Split-Path -Path $MyInvocation.MyCommand.Path

# 모든 하위 폴더 중 pom.xml이 존재하는 디렉토리 찾기
$pomPaths = Get-ChildItem -Path $baseDir -Recurse -Filter "pom.xml"

if ($pomPaths.Count -eq 0) {
    Write-Host "❌ 하위 폴더에서 pom.xml을 찾을 수 없습니다."
    exit 1
}

foreach ($pomPath in $pomPaths) {
    $projectDir = Split-Path $pomPath.FullName
    Write-Host "`n📦 처리 중: $projectDir"

    try {
        [xml]$pom = Get-Content $pomPath.FullName

        $artifactId = $pom.project.artifactId
        $version = $pom.project.version
        $groupId = $pom.project.groupId

        if (-not $artifactId -or -not $version -or -not $groupId) {
            Write-Warning "⚠️ groupId, artifactId 또는 version 을 찾을 수 없습니다."
            continue
        }

        $bundleName = "$artifactId-$version"
        $targetDir = Join-Path $projectDir "target"
        $bundleDir = Join-Path $targetDir "bundle"

        if (!(Test-Path $targetDir)) {
            Write-Warning "⚠️ target 폴더가 존재하지 않습니다: $targetDir"
            continue
        }

        if (Test-Path $bundleDir) {
            Remove-Item -Recurse -Force $bundleDir
        }
        New-Item -ItemType Directory -Path $bundleDir | Out-Null

        $fileBaseNames = @(
            "$bundleName.jar",
            "$bundleName.pom",
            "$bundleName-sources.jar",
            "$bundleName-javadoc.jar"
        )

        foreach ($file in $fileBaseNames) {
            $src = Join-Path $targetDir $file
            $asc = "$src.asc"

            if (Test-Path $src) {
                Copy-Item $src $bundleDir
                if (Test-Path $asc) {
                    Copy-Item $asc $bundleDir
                }
            }
        }

        # 체크섬 생성
        $filesToHash = Get-ChildItem -Path $bundleDir -File | Where-Object {
            $_.Extension -in ".jar", ".pom"
        }
        foreach ($file in $filesToHash) {
            $md5Hash = (Get-FileHash -Path $file.FullName -Algorithm MD5).Hash.ToLower()
            Set-Content -Encoding ASCII -Path "$($file.FullName).md5" -Value $md5Hash

            $sha1Hash = (Get-FileHash -Path $file.FullName -Algorithm SHA1).Hash.ToLower()
            Set-Content -Encoding ASCII -Path "$($file.FullName).sha1" -Value $sha1Hash
        }

        # groupId/artifactId/version 계층 구조 생성
        $groupPath = $groupId -replace '\.', [IO.Path]::DirectorySeparatorChar
        $artifactPath = Join-Path $groupPath $artifactId
        $versionPath = Join-Path $artifactPath $version
        $finalDir = Join-Path $baseDir $versionPath

        # 상위 groupId 디렉토리 전체 삭제(중복 방지)
        $groupDir = Join-Path $baseDir ($groupPath.Split([IO.Path]::DirectorySeparatorChar)[0])
        if (Test-Path $groupDir) { Remove-Item -Recurse -Force $groupDir }

        New-Item -ItemType Directory -Path $finalDir -Force | Out-Null

        # bundleDir의 모든 파일을 $finalDir로 복사
        Get-ChildItem -Path $bundleDir -File | ForEach-Object {
            Copy-Item $_.FullName $finalDir
        }

        # zip 압축 (groupId 최상위 디렉토리 기준)
        $zipPath = Join-Path $baseDir "$bundleName-bundle.zip"
        if (Test-Path $zipPath) { Remove-Item $zipPath }
        Push-Location $baseDir
        $topGroupDir = $groupPath.Split([IO.Path]::DirectorySeparatorChar)[0]
        Write-Host "📦 번들 zip 생성 중: $topGroupDir"
        Compress-Archive -Path $topGroupDir -DestinationPath $zipPath
        Pop-Location

        Write-Host "✅ 번들 zip 생성 완료: $zipPath"
    } catch {
        Write-Error "❌ 오류 발생: $_"
    }
}