Write-Host ""
Write-Host " Stopping all services..." -ForegroundColor Yellow

$ports = @(8761, 8080, 8081, 8082, 8083, 8084, 8085, 5500)
foreach ($port in $ports) {
    $connections = netstat -ano | Select-String ":$port\s"
    foreach ($line in $connections) {
        $procId = ($line.ToString() -split '\s+')[-1]
        if ($procId -and $procId -ne "0") {
            Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
        }
    }
}

Write-Host " All services stopped." -ForegroundColor Cyan
Write-Host ""
