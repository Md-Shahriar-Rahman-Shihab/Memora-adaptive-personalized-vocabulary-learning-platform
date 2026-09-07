# Memora Backend Startup Script
# Automatically frees port 8080 if needed, loads environment configuration, and starts Spring Boot

$conn = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
if ($conn) {
    Write-Host "[Memora] Port 8080 is currently occupied by PID $($conn.OwningProcess). Releasing port..." -ForegroundColor Yellow
    Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 1
}

# Load environment configuration from .env safely without printing secrets
$envFile = Join-Path $PSScriptRoot ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#")) {
            $parts = $line -split "=", 2
            if ($parts.Length -eq 2) {
                $k = $parts[0].Trim()
                $v = $parts[1].Trim()
                if (($v.StartsWith('"') -and $v.EndsWith('"')) -or ($v.StartsWith("'") -and $v.EndsWith("'"))) {
                    if ($v.Length -ge 2) {
                        $v = $v.Substring(1, $v.Length - 2)
                    }
                }
                if (-not [System.Environment]::GetEnvironmentVariable($k, "Process")) {
                    [System.Environment]::SetEnvironmentVariable($k, $v, "Process")
                }
            }
        }
    }
}

$dbConfigured = [string]::IsNullOrWhiteSpace($env:DB_URL) -eq $false
if ($dbConfigured) {
    Write-Host "[Memora] Database configuration: CONFIGURED" -ForegroundColor Green
    Write-Host "[Memora] Database type: PostgreSQL" -ForegroundColor Green
} else {
    Write-Host "[Memora] Database configuration: NOT CONFIGURED (using application.yml default)" -ForegroundColor Yellow
    Write-Host "[Memora] Database type: PostgreSQL" -ForegroundColor Yellow
}

if (-not $env:MEMORA_AI_TIMEOUT_MS) {
    $env:MEMORA_AI_TIMEOUT_MS = "10000"
}

Write-Host "[Memora] Starting Spring Boot backend on PostgreSQL..." -ForegroundColor Green
.\mvnw.cmd spring-boot:run

