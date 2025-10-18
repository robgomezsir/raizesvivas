# Script para resetar e commitar tudo do projeto
Set-Location "C:\Users\robgo\raizesvivas"

Write-Host "=== RESETANDO GIT E COMMITANDO TUDO ===" -ForegroundColor Green

Write-Host "1. Removendo todos os arquivos do cache do Git..." -ForegroundColor Yellow
git rm -r --cached .

Write-Host "2. Adicionando todos os arquivos novamente..." -ForegroundColor Yellow
git add .

Write-Host "3. Verificando status..." -ForegroundColor Yellow
git status

Write-Host "4. Fazendo commit completo..." -ForegroundColor Yellow
git commit -m "feat: Commit completo do estado atual do projeto - Todas as 5 fases implementadas, Gradle corrigido, estrutura organizada"

Write-Host "5. Fazendo push forçado..." -ForegroundColor Yellow
git push origin main --force

Write-Host "=== GIT ATUALIZADO COM SUCESSO! ===" -ForegroundColor Green
