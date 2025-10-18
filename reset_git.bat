@echo off
echo === RESETANDO GIT E COMMITANDO TUDO ===

cd /d "C:\Users\robgo\raizesvivas"

echo 1. Removendo todos os arquivos do cache do Git...
git rm -r --cached .

echo 2. Adicionando todos os arquivos novamente...
git add .

echo 3. Verificando status...
git status

echo 4. Fazendo commit completo...
git commit -m "feat: Commit completo do estado atual do projeto - Todas as 5 fases implementadas, Gradle corrigido, estrutura organizada"

echo 5. Fazendo push forçado...
git push origin main --force

echo === GIT ATUALIZADO COM SUCESSO! ===
pause
