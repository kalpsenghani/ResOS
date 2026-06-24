param(
    [string]$OutDir = ".\docker\jwt-keys"
)

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

$privateKey = Join-Path $OutDir "private.pem"
$publicKey = Join-Path $OutDir "public.pem"

openssl genpkey -algorithm RSA -out $privateKey -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in $privateKey -out $publicKey

Write-Host "Generated JWT keys:"
Write-Host "  Private: $privateKey"
Write-Host "  Public:  $publicKey"
Write-Host ""
Write-Host "Set in .env (single-line PEM or use a secrets manager):"
Write-Host "  RESOS_JWT_PRIVATE_KEY=<contents of private.pem>"
Write-Host "  RESOS_JWT_PUBLIC_KEY=<contents of public.pem>"
