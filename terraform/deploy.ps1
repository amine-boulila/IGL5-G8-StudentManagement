param(
    [switch]$Destroy
)

$ErrorActionPreference = "Stop"

$terraformCommand = Get-Command terraform -ErrorAction SilentlyContinue
$terraformExecutable = if ($null -ne $terraformCommand) {
    $terraformCommand.Source
} else {
    $installRoot = Join-Path $env:LOCALAPPDATA "Programs\HashiCorp\Terraform"
    $localTerraform = Get-ChildItem -LiteralPath $installRoot -Filter "terraform.exe" -File -Recurse -ErrorAction SilentlyContinue |
        Sort-Object { [version]$_.Directory.Name } -Descending |
        Select-Object -First 1
    if ($null -eq $localTerraform) {
        throw "Terraform CLI is not installed or is not in PATH. Install Terraform 1.6 or later, then reopen PowerShell."
    }
    $localTerraform.FullName
}

$projectRoot = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $projectRoot ".env"
if (-not (Test-Path -LiteralPath $envFile)) {
    throw "Missing .env file. Create it from .env.example and set the MySQL values first."
}

$values = @{}
foreach ($line in Get-Content -LiteralPath $envFile) {
    if ($line -match '^\s*([^#=]+)=(.*)$') {
        $values[$matches[1].Trim()] = $matches[2].Trim()
    }
}

foreach ($name in @("MYSQL_DATABASE", "MYSQL_USER", "MYSQL_PASSWORD", "MYSQL_ROOT_PASSWORD")) {
    if (-not $values.ContainsKey($name) -or [string]::IsNullOrWhiteSpace($values[$name])) {
        throw "Required variable $name is missing or empty in .env."
    }
}

if ($values["MYSQL_USER"] -eq "root") {
    throw "MYSQL_USER must be a non-root account, such as studentmanagement."
}

$terraformVariableNames = @("mysql_database", "mysql_user", "mysql_password", "mysql_root_password")
$previousTerraformVariables = @{}
foreach ($name in $terraformVariableNames) {
    $envName = "TF_VAR_$name"
    $sourceName = $name.ToUpperInvariant()
    $previousTerraformVariables[$envName] = [Environment]::GetEnvironmentVariable($envName, "Process")
    [Environment]::SetEnvironmentVariable($envName, $values[$sourceName], "Process")
}

$terraformDirectory = $PSScriptRoot
Push-Location $terraformDirectory
try {
    & $terraformExecutable init -input=false
    if ($LASTEXITCODE -ne 0) { throw "terraform init failed with exit code $LASTEXITCODE" }

    & $terraformExecutable fmt -check
    if ($LASTEXITCODE -ne 0) { throw "terraform fmt -check failed with exit code $LASTEXITCODE" }

    & $terraformExecutable validate
    if ($LASTEXITCODE -ne 0) { throw "terraform validate failed with exit code $LASTEXITCODE" }

    if ($Destroy) {
        & $terraformExecutable destroy -input=false
        if ($LASTEXITCODE -ne 0) { throw "terraform destroy failed with exit code $LASTEXITCODE" }
    } else {
        & $terraformExecutable plan -input=false "-out=student-management.tfplan"
        if ($LASTEXITCODE -ne 0) { throw "terraform plan failed with exit code $LASTEXITCODE" }

        & $terraformExecutable apply -input=false student-management.tfplan
        if ($LASTEXITCODE -ne 0) { throw "terraform apply failed with exit code $LASTEXITCODE" }

        & $terraformExecutable output
        if ($LASTEXITCODE -ne 0) { throw "terraform output failed with exit code $LASTEXITCODE" }
    }
}
finally {
    Remove-Item -LiteralPath (Join-Path $terraformDirectory "student-management.tfplan") -Force -ErrorAction SilentlyContinue
    foreach ($name in $terraformVariableNames) {
        $envName = "TF_VAR_$name"
        if ($null -eq $previousTerraformVariables[$envName]) {
            Remove-Item "Env:$envName" -ErrorAction SilentlyContinue
        } else {
            [Environment]::SetEnvironmentVariable($envName, $previousTerraformVariables[$envName], "Process")
        }
    }
    Pop-Location
}
