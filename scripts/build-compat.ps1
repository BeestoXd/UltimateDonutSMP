param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("Paper", "Folia")]
    [string]$Target
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$projectRoot = Split-Path -Parent $PSScriptRoot
$userAgent = "UltimateDonutSmp-build/1.3.3"

function Invoke-Maven {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    & mvn @Arguments | Out-Host
    if ($LASTEXITCODE -ne 0) {
        throw "Maven failed with exit code $LASTEXITCODE."
    }
}

function Get-LatestMavenVersion {
    param(
        [Parameter(Mandatory = $true)]
        [string]$MetadataUrl
    )

    [xml]$metadata = (Invoke-WebRequest `
        -UseBasicParsing `
        -Headers @{ "User-Agent" = $userAgent } `
        -Uri $MetadataUrl `
        -TimeoutSec 30).Content

    $latest = [string]$metadata.metadata.versioning.latest
    if ([string]::IsNullOrWhiteSpace($latest)) {
        throw "No latest Maven version was published at $MetadataUrl."
    }

    return $latest
}

function Publish-Artifact {
    $artifact = Get-ChildItem -LiteralPath (Join-Path $projectRoot "target") `
        -Filter "UltimateDonutSmp-*.jar" `
        -File |
        Select-Object -First 1

    if ($null -eq $artifact) {
        throw "The compatibility build completed without producing an UltimateDonutSmp jar."
    }

    $distDirectory = Join-Path $projectRoot "dist"
    New-Item -ItemType Directory -Path $distDirectory -Force | Out-Null
    $destination = Join-Path $distDirectory $artifact.Name
    Copy-Item -LiteralPath $artifact.FullName -Destination $destination -Force
    return $destination
}

function Build-PaperSpigot {
    $spigotMetadata = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/org/spigotmc/spigot-api/maven-metadata.xml"
    $paperMetadata = "https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/maven-metadata.xml"
    $latestSpigot = Get-LatestMavenVersion -MetadataUrl $spigotMetadata
    $latestPaper = Get-LatestMavenVersion -MetadataUrl $paperMetadata

    Write-Host "Checking Paper/Spigot source against latest APIs:"
    Write-Host "  Spigot API: $latestSpigot"
    Write-Host "  Paper API:  $latestPaper"

    Invoke-Maven -Arguments @(
        "-B",
        "-DskipTests",
        "clean",
        "compile",
        "-Ppaper-mirror",
        "-Dspigot-api.version=$latestSpigot"
    )

    Invoke-Maven -Arguments @(
        "-B",
        "-DskipTests",
        "clean",
        "compile",
        "-Ppaper-mirror",
        "-Dserver-api.groupId=io.papermc.paper",
        "-Dserver-api.artifactId=paper-api",
        "-Dserver-api.version=$latestPaper"
    )

    Write-Host "Building the backward-compatible Paper/Spigot artifact with the 1.21.10 API."
    Invoke-Maven -Arguments @("-B", "clean", "package", "-Ppaper-mirror")
    return Publish-Artifact
}

function Build-Folia {
    $foliaMetadata = "https://repo.papermc.io/repository/maven-public/dev/folia/folia-api/maven-metadata.xml"
    $latestFolia = Get-LatestMavenVersion -MetadataUrl $foliaMetadata

    Write-Host "Checking Folia source against latest API: $latestFolia"
    Invoke-Maven -Arguments @(
        "-B",
        "-DskipTests",
        "clean",
        "compile",
        "-Pfolia-mirror",
        "-Dfolia-api.version=$latestFolia"
    )

    Write-Host "Building the backward-compatible Folia artifact with the 1.21.11 API."
    Invoke-Maven -Arguments @("-B", "clean", "package", "-Pfolia-mirror")
    return Publish-Artifact
}

Push-Location $projectRoot
try {
    if ($Target -eq "Paper") {
        $artifactPath = Build-PaperSpigot
    } else {
        $artifactPath = Build-Folia
    }

    Write-Host ""
    Write-Host "Compatibility build completed. Artifact:"
    Write-Host $artifactPath
} finally {
    Pop-Location
}
