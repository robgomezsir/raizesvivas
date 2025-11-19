# Script para configurar SMTP no Firebase Functions v2
# Execute este script na raiz do projeto (não dentro de functions/)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Configurar SMTP - Firebase Functions v2" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "IMPORTANTE: No Firebase Functions v2, use o Firebase Console para configurar:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Acesse: https://console.firebase.google.com/project/suasraizesvivas/functions" -ForegroundColor White
Write-Host "2. Vá em 'Secrets' (ou Configurações → Secrets)" -ForegroundColor White
Write-Host "3. Clique em 'Add secret' e configure:" -ForegroundColor White
Write-Host ""
Write-Host "   SMTP_HOST = smtp.gmail.com" -ForegroundColor Gray
Write-Host "   SMTP_PORT = 587" -ForegroundColor Gray
Write-Host "   SMTP_USER = robgomez.sir@gmail.com" -ForegroundColor Gray
Write-Host "   SMTP_PASS = oqwetblakbdrclec (marque como secret)" -ForegroundColor Gray
Write-Host "   SMTP_FROM = robgomez.sir@gmail.com" -ForegroundColor Gray
Write-Host ""
Write-Host "OU use o comando CLI abaixo para configurar apenas o secret da senha:" -ForegroundColor Yellow
Write-Host ""
Write-Host "  echo 'oqwetblakbdrclec' | firebase functions:secrets:set SMTP_PASS" -ForegroundColor Cyan
Write-Host ""
Write-Host "NOTA: As outras variáveis já têm valores padrão no código." -ForegroundColor Gray
Write-Host "      Se quiser alterá-las, configure via Console ou use secrets." -ForegroundColor Gray
Write-Host ""
Write-Host "Após configurar, faça o deploy:" -ForegroundColor Yellow
Write-Host "  firebase deploy --only functions" -ForegroundColor Cyan
Write-Host ""

