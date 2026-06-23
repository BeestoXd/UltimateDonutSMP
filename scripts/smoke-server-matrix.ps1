param(
    [int]$StartupTimeoutSeconds = 180
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$projectRoot = Split-Path -Parent $PSScriptRoot
$smokeRoot = Join-Path ([IO.Path]::GetTempPath()) "UltimateDonutSmp-server-smoke"
$cacheRoot = Join-Path $smokeRoot "cache"
$runsRoot = Join-Path $smokeRoot "runs"
$userAgent = "UltimateDonutSmp-smoke/1.3"
$placeholderApiVersion = "2.12.2"
$placeholderApiJar = Join-Path $cacheRoot "PlaceholderAPI-$placeholderApiVersion.jar"

Add-Type -AssemblyName System.IO.Compression.FileSystem

$matrix = @(
    @{
        Name = "paper-1.21.10"
        Project = "paper"
        Version = "1.21.10"
        Plugin = "UltimateDonutSmp-1.3-paper-spigot-1.21.10-26.2.jar"
    },
    @{
        Name = "paper-26.2"
        Project = "paper"
        Version = "26.2"
        Plugin = "UltimateDonutSmp-1.3-paper-spigot-1.21.10-26.2.jar"
    },
    @{
        Name = "folia-1.21.11"
        Project = "folia"
        Version = "1.21.11"
        Plugin = "UltimateDonutSmp-1.3-folia-1.21.11-26.1.2.jar"
    },
    @{
        Name = "folia-26.1.2"
        Project = "folia"
        Version = "26.1.2"
        Plugin = "UltimateDonutSmp-1.3-folia-1.21.11-26.1.2.jar"
    }
)

function Assert-SafeRunPath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    $resolvedRunsRoot = [IO.Path]::GetFullPath($runsRoot).TrimEnd("\") + "\"
    $resolvedPath = [IO.Path]::GetFullPath($Path)
    if (!$resolvedPath.StartsWith($resolvedRunsRoot, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to clean smoke-test path outside $resolvedRunsRoot"
    }
}

function Get-ServerDownload {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Project,
        [Parameter(Mandatory = $true)]
        [string]$Version
    )

    $buildsUrl = "https://fill.papermc.io/v3/projects/$Project/versions/$Version/builds"
    $builds = Invoke-RestMethod `
        -Headers @{ "User-Agent" = $userAgent } `
        -Uri $buildsUrl `
        -TimeoutSec 30

    $build = $builds | Select-Object -First 1
    if ($null -eq $build -or $null -eq $build.downloads."server:default".url) {
        throw "No server download is available for $Project $Version."
    }

    return @{
        Build = $build.id
        Channel = $build.channel
        Url = $build.downloads."server:default".url
    }
}

function Test-JarArchive {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    if (!(Test-Path -LiteralPath $Path)) {
        return $false
    }

    try {
        $archive = [IO.Compression.ZipFile]::OpenRead($Path)
        try {
            return $archive.Entries.Count -gt 0
        } finally {
            $archive.Dispose()
        }
    } catch {
        return $false
    }
}

function Install-PlaceholderApi {
    if (Test-JarArchive -Path $placeholderApiJar) {
        return
    }

    $releaseUrl = "https://api.github.com/repos/PlaceholderAPI/PlaceholderAPI/releases/tags/$placeholderApiVersion"
    $release = Invoke-RestMethod `
        -Headers @{ "User-Agent" = $userAgent } `
        -Uri $releaseUrl `
        -TimeoutSec 30
    $asset = $release.assets |
        Where-Object { $_.name -eq "PlaceholderAPI-$placeholderApiVersion.jar" } |
        Select-Object -First 1
    if ($null -eq $asset) {
        throw "PlaceholderAPI $placeholderApiVersion release jar was not found."
    }

    Write-Host "Downloading PlaceholderAPI $placeholderApiVersion release jar"
    & curl.exe `
        --fail `
        --location `
        --silent `
        --show-error `
        --retry 3 `
        --connect-timeout 30 `
        --max-time 180 `
        --user-agent $userAgent `
        --output $placeholderApiJar `
        $asset.browser_download_url
    if ($LASTEXITCODE -ne 0 -or !(Test-JarArchive -Path $placeholderApiJar)) {
        throw "Failed to download a valid PlaceholderAPI $placeholderApiVersion release jar."
    }
}

function Invoke-SmokeServer {
    param(
        [Parameter(Mandatory = $true)]
        [hashtable]$Entry
    )

    $pluginJar = Join-Path (Join-Path $projectRoot "dist") $Entry.Plugin
    if (!(Test-Path -LiteralPath $pluginJar)) {
        throw "Missing plugin artifact: $pluginJar"
    }

    $download = Get-ServerDownload -Project $Entry.Project -Version $Entry.Version
    $serverJar = Join-Path $cacheRoot "$($Entry.Name)-build-$($download.Build).jar"
    $cachedServer = Get-Item -LiteralPath $serverJar -ErrorAction SilentlyContinue
    if ($null -eq $cachedServer -or $cachedServer.Length -eq 0 -or !(Test-JarArchive -Path $serverJar)) {
        if ($null -ne $cachedServer) {
            Remove-Item -LiteralPath $serverJar -Force
        }
        Write-Host "Downloading $($Entry.Project) $($Entry.Version) build $($download.Build) ($($download.Channel))"
        & curl.exe `
            --fail `
            --location `
            --silent `
            --show-error `
            --retry 3 `
            --connect-timeout 30 `
            --max-time 180 `
            --user-agent $userAgent `
            --output $serverJar `
            $download.Url
        if ($LASTEXITCODE -ne 0) {
            throw "Failed to download $($Entry.Project) $($Entry.Version)."
        }
        $cachedServer = Get-Item -LiteralPath $serverJar
        if ($cachedServer.Length -eq 0 -or !(Test-JarArchive -Path $serverJar)) {
            throw "Downloaded server jar is empty or corrupt: $serverJar"
        }
    }

    $runDirectory = Join-Path $runsRoot $Entry.Name
    Assert-SafeRunPath -Path $runDirectory
    if (Test-Path -LiteralPath $runDirectory) {
        Remove-Item -LiteralPath $runDirectory -Recurse -Force
    }

    $pluginsDirectory = Join-Path $runDirectory "plugins"
    New-Item -ItemType Directory -Path $pluginsDirectory -Force | Out-Null
    Copy-Item -LiteralPath $pluginJar -Destination $pluginsDirectory
    Copy-Item -LiteralPath $placeholderApiJar -Destination $pluginsDirectory
    Set-Content -LiteralPath (Join-Path $runDirectory "eula.txt") -Value "eula=true" -Encoding Ascii
    Set-Content -LiteralPath (Join-Path $runDirectory "server.properties") -Encoding Ascii -Value @(
        "online-mode=false"
        "server-port=0"
        "motd=UltimateDonutSmp compatibility smoke test"
        "max-players=1"
        "view-distance=2"
        "simulation-distance=2"
    )

    $process = [Diagnostics.Process]::new()
    $process.StartInfo = [Diagnostics.ProcessStartInfo]@{
        FileName = "java"
        Arguments = "-Xms512M -Xmx1024M -jar `"$serverJar`" --nogui"
        WorkingDirectory = $runDirectory
        UseShellExecute = $false
        CreateNoWindow = $true
        RedirectStandardInput = $true
        RedirectStandardOutput = $false
        RedirectStandardError = $false
    }

    Write-Host "Starting $($Entry.Name)"
    if (!$process.Start()) {
        throw "Failed to start $($Entry.Name)."
    }

    $deadline = [DateTime]::UtcNow.AddSeconds($StartupTimeoutSeconds)
    $enabled = $false
    $serverReady = $false
    $latestLog = Join-Path $runDirectory "logs\latest.log"
    $logText = ""

    try {
        while (!$process.HasExited -and [DateTime]::UtcNow -lt $deadline) {
            if (Test-Path -LiteralPath $latestLog) {
                $logText = Get-Content -LiteralPath $latestLog -Raw
                $enabled = $logText -match "UltimateDonutSmp enabled successfully\."
                $serverReady = $logText -match 'Done \(.+\)! For help'
            }

            if ($enabled -and $serverReady) {
                break
            }
            Start-Sleep -Milliseconds 100
        }

        if (!$process.HasExited) {
            $process.StandardInput.WriteLine("stop")
            $process.StandardInput.Flush()
            if (!$process.WaitForExit(30000)) {
                $process.Kill($true)
                $process.WaitForExit()
            }
        }
    } finally {
        if (!$process.HasExited) {
            try {
                $process.StandardInput.WriteLine("stop")
                $process.StandardInput.Flush()
            } catch {
                # The process may already be shutting down.
            }
            if (!$process.WaitForExit(10000)) {
                $process.Kill($true)
                $process.WaitForExit()
            }
        }
        $process.Dispose()
    }

    if (!$enabled) {
        if (Test-Path -LiteralPath $latestLog) {
            $logText = Get-Content -LiteralPath $latestLog -Raw
        }
        $relevantLog = $logText -split "`r?`n" |
            Where-Object { $_ -match "UltimateDonutSmp|Exception|ERROR|Could not load|Unsupported" } |
            Select-Object -Last 30
        throw "$($Entry.Name) did not enable UltimateDonutSmp.`n$($relevantLog -join "`n")"
    }

    return [pscustomobject]@{
        Server = $Entry.Name
        Build = $download.Build
        Channel = $download.Channel
        Enabled = $enabled
        Ready = $serverReady
        Log = $latestLog
    }
}

New-Item -ItemType Directory -Path $cacheRoot -Force | Out-Null
New-Item -ItemType Directory -Path $runsRoot -Force | Out-Null
Install-PlaceholderApi

$results = foreach ($entry in $matrix) {
    Invoke-SmokeServer -Entry $entry
}

$results | Format-Table -AutoSize
