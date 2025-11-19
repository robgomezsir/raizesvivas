# Configurar SMTP no Firebase Functions v2

## Método 1: Via Firebase Console (Recomendado)

1. Acesse: https://console.firebase.google.com/project/suasraizesvivas/functions
2. Vá em **Secrets** (ou **Configurações** → **Secrets**)
3. Clique em **Add secret** para cada variável:

   - **SMTP_HOST**: `smtp.gmail.com`
   - **SMTP_PORT**: `587`
   - **SMTP_USER**: `robgomez.sir@gmail.com`
   - **SMTP_PASS**: `oqwetblakbdrclec` (marque como secret)
   - **SMTP_FROM**: `robgomez.sir@gmail.com`

## Método 2: Via CLI (PowerShell)

```powershell
# Na raiz do projeto
cd C:\Users\robgo\raizesvivas

# Configurar secret para a senha
echo "oqwetblakbdrclec" | firebase functions:secrets:set SMTP_PASS

# Para variáveis normais (defineString), você pode usar:
# (Mas no v2, é melhor configurar via Console ou usar secrets para todas)
```

## Após configurar

Faça o deploy novamente:

```powershell
firebase deploy --only functions
```

