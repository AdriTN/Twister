# Obtén la lista de dispositivos conectados y filtra los IDs válidos
$devices = adb devices | Select-String "device$" | ForEach-Object { ($_ -split '\s+')[0] }

# Verifica si hay dispositivos encontrados
if (-not $devices) {
    Write-Host "No devices found." -ForegroundColor Yellow
    exit
}

# Itera sobre cada dispositivo y aplica los comandos adb reverse
foreach ($device in $devices) {
    Write-Host "Applying reverse port forwarding to $device"
    adb -s $device reverse tcp:3000 tcp:3000
    adb -s $device reverse tcp:3001 tcp:3001
}