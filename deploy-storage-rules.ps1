# Script para fazer deploy das Storage Rules do Firebase
# Uso: .\deploy-storage-rules.ps1

Write-Host "🚀 Deploy das Storage Rules do Firebase" -ForegroundColor Cyan
Write-Host ""

# Verificar se Firebase CLI está instalado
Write-Host "📋 Verificando Firebase CLI..." -ForegroundColor Yellow
try {
    $firebaseVersion = firebase --version 2>&1
    Write-Host "✅ Firebase CLI encontrado: $firebaseVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Firebase CLI não encontrado!" -ForegroundColor Red
    Write-Host "   Instale com: npm install -g firebase-tools" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Verificar se está logado
Write-Host "🔐 Verificando autenticação..." -ForegroundColor Yellow
$loginStatus = firebase projects:list 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Não está logado no Firebase!" -ForegroundColor Red
    Write-Host "   Execute: firebase login" -ForegroundColor Yellow
    exit 1
}
Write-Host "✅ Autenticado no Firebase" -ForegroundColor Green

Write-Host ""

# Verificar se storage.rules existe
Write-Host "📁 Verificando arquivo storage.rules..." -ForegroundColor Yellow
if (-not (Test-Path "storage.rules")) {
    Write-Host "❌ Arquivo storage.rules não encontrado!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Arquivo storage.rules encontrado" -ForegroundColor Green

Write-Host ""

# Verificar se firebase.json existe
Write-Host "📁 Verificando arquivo firebase.json..." -ForegroundColor Yellow
if (-not (Test-Path "firebase.json")) {
    Write-Host "❌ Arquivo firebase.json não encontrado!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Arquivo firebase.json encontrado" -ForegroundColor Green

Write-Host ""

# Mostrar projeto atual
Write-Host "📦 Projeto Firebase atual:" -ForegroundColor Yellow
firebase use
Write-Host ""

# Confirmar deploy
Write-Host "⚠️  Deseja fazer deploy das Storage Rules?" -ForegroundColor Yellow
$confirmation = Read-Host "   Digite 'sim' para continuar"
if ($confirmation -ne "sim") {
    Write-Host "❌ Deploy cancelado" -ForegroundColor Red
    exit 0
}

Write-Host ""
Write-Host "🚀 Iniciando deploy..." -ForegroundColor Cyan
Write-Host ""

# Fazer deploy
firebase deploy --only storage

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Deploy concluído com sucesso!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📝 Próximos passos:" -ForegroundColor Yellow
    Write-Host "   1. Verifique as rules no Console Firebase" -ForegroundColor White
    Write-Host "   2. Teste o upload de fotos no app" -ForegroundColor White
    Write-Host "   3. Monitore os logs para erros" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "❌ Erro no deploy!" -ForegroundColor Red
    Write-Host "   Verifique os erros acima e tente novamente" -ForegroundColor Yellow
    exit 1
}

