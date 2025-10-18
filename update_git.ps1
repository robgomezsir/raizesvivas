# Script para atualizar o Git com as correções do projeto
Set-Location "C:\Users\robgo\raizesvivas"

Write-Host "Verificando status do Git..."
git status

Write-Host "Adicionando arquivos modificados..."
git add build.gradle.kts
git add gradlew.bat
git add gradle/wrapper/gradle-wrapper.properties
git add gradle/wrapper/gradle-wrapper.jar

Write-Host "Fazendo commit das correções..."
git commit -m "fix: Corrigir configuração do Gradle - Remove repositórios duplicados do build.gradle.kts e adiciona Gradle Wrapper"

Write-Host "Fazendo push para o GitHub..."
git push origin main

Write-Host "Git atualizado com sucesso!"
