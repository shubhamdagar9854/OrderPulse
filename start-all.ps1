$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$logsDir = "$rootDir\logs"
New-Item -ItemType Directory -Force -Path $logsDir | Out-Null

Write-Host ""
Write-Host " =================================================" -ForegroundColor Cyan
Write-Host "  Starting OrderPulse - All Services" -ForegroundColor Cyan
Write-Host " =================================================" -ForegroundColor Cyan
Write-Host ""

$services = @(
    @{Name="user-service"; Port=8081},
    @{Name="product-service"; Port=8082},
    @{Name="order-service"; Port=8083},
    @{Name="payment-service"; Port=8084},
    @{Name="notification-service"; Port=8085},
    @{Name="api-gateway"; Port=8080}
)

foreach ($svc in $services) {
    $name = $svc.Name
    $port = $svc.Port
    $svcDir = "$rootDir\$name"
    $logFile = "$logsDir\$name.log"
    $mvnw = "$svcDir\mvnw.cmd"

    Write-Host "  Starting $name on port $port..." -ForegroundColor Gray

    if (Test-Path $mvnw) {
        $proc = Start-Process -WindowStyle Hidden -PassThru -FilePath "cmd.exe" -ArgumentList "/c cd /d `"$svcDir`" && `"$mvnw`" spring-boot:run > `"$logFile`" 2>&1"
    } else {
        $proc = Start-Process -WindowStyle Hidden -PassThru -FilePath "cmd.exe" -ArgumentList "/c cd /d `"$svcDir`" && mvn spring-boot:run > `"$logFile`" 2>&1"
    }
    Start-Sleep -Seconds 2
}

# Start Frontend
Write-Host ""
Write-Host "  Starting Frontend on port 5500..." -ForegroundColor Gray

# Check python command
$pythonCmd = "python"
try { $null = Get-Command "python3" -ErrorAction Stop; $pythonCmd = "python3" } catch {}

$frontendProc = Start-Process -WindowStyle Hidden -PassThru -FilePath $pythonCmd -ArgumentList "-m http.server 5500" -WorkingDirectory "$rootDir\frontend"

Write-Host ""
Write-Host " =================================================" -ForegroundColor Cyan
Write-Host "  ALL SERVICES ARE STARTING" -ForegroundColor Cyan
Write-Host " =================================================" -ForegroundColor Cyan
Write-Host "  Wait 1-2 min for all services to start" -ForegroundColor Yellow
Write-Host "  Then open http://localhost:5500 in your browser" -ForegroundColor White
Write-Host ""
Write-Host "  Login:  admin@orderpulse.com / admin123" -ForegroundColor Green
Write-Host ""
Write-Host "  Check logs:  Get-Content $logsDir\<service-name>.log -Tail 20" -ForegroundColor Gray
Write-Host "  Stop all:   .\stop-all.ps1" -ForegroundColor Gray
Write-Host " =================================================" -ForegroundColor Cyan
