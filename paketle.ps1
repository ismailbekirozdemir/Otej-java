$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "1/3 - Maven ile proje ve bagimliliklar derlenip paketleniyor..." -ForegroundColor Cyan
& .\mvnw.cmd clean package
if ($LASTEXITCODE -ne 0) {
    throw "Maven paketleme basarisiz oldu!"
}

$shadedJar = Join-Path $PSScriptRoot "target\Otel-Otomasyon-1.0-SNAPSHOT-app.jar"
if (-not (Test-Path $shadedJar)) {
    throw "Shaded JAR bulunamadi: $shadedJar"
}

Write-Host "2/3 - jpackage girdi klasoru hazirlaniyor..." -ForegroundColor Cyan
$jpackageInput = Join-Path $PSScriptRoot "target\jpackage-input"
if (Test-Path $jpackageInput) {
    Remove-Item -Recurse -Force $jpackageInput
}
New-Item -ItemType Directory -Path $jpackageInput | Out-Null
Copy-Item $shadedJar -Destination $jpackageInput

$distDir = Join-Path $PSScriptRoot "dist"
$appImageDir = Join-Path $distDir "OtelOtomasyon"
if (Test-Path $appImageDir) {
    Remove-Item -Recurse -Force $appImageDir
}
if (-not (Test-Path $distDir)) {
    New-Item -ItemType Directory -Path $distDir | Out-Null
}

Write-Host "3/3 - Bagimsiz Windows masaustu paketi (.exe + gomulu Java) olusturuluyor..." -ForegroundColor Cyan
# Not: Bilgisayarda WiX Toolset kuruluysa '--type app-image' yerine '--type exe' veya '--type msi'
# kullanilarak klasik Setup kurulum sihirbazi da uretilebilir.
$jpackageArgs = @(
    "--type", "app-image",
    "--name", "OtelOtomasyon",
    "--app-version", "1.0.0",
    "--vendor", "Otel Otomasyon",
    "--input", $jpackageInput,
    "--main-jar", "Otel-Otomasyon-1.0-SNAPSHOT-app.jar",
    "--main-class", "com.example.otelotomasyon.Launcher",
    "--dest", $distDir,
    "--add-modules", "java.base,java.sql,java.logging,java.desktop,java.xml,java.scripting,java.naming,java.management,jdk.unsupported"
)

$icoPath = Join-Path $PSScriptRoot "src\main\resources\hotel-icon.ico"
if (Test-Path $icoPath) {
    $jpackageArgs += @("--icon", $icoPath)
}

& jpackage @jpackageArgs
if ($LASTEXITCODE -ne 0) {
    throw "jpackage basarisiz oldu!"
}

Write-Host ""
Write-Host "==================================================================" -ForegroundColor Green
Write-Host "PAKETLEME BASARIYLA TAMAMLANDI!" -ForegroundColor Green
Write-Host "Calistirilabilir dosya: $appImageDir\OtelOtomasyon.exe" -ForegroundColor Yellow
Write-Host "Baska bir Windows bilgisayara gondermek icin 'dist\OtelOtomasyon' klasorunu"
Write-Host ".zip yapip paylasabilirsiniz (Java kurulu olmasina gerek yoktur)."
Write-Host "Veritabani (Otel.db) her kullanicinin kendi klasorunde saklanir:"
Write-Host "  %USERPROFILE%\OtelOtomasyon\Otel.db"
Write-Host "==================================================================" -ForegroundColor Green
