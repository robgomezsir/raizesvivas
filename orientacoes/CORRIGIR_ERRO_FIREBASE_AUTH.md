# Como Corrigir o Erro CONFIGURATION_NOT_FOUND do Firebase Auth

## Problema
O erro `CONFIGURATION_NOT_FOUND` ocorre quando o Firebase Authentication não consegue encontrar a configuração necessária para criar usuários. Os logs mostram:
- `Creating user with ... with empty reCAPTCHA token`
- `Error getting App Check token; using placeholder token instead`

## Solução: Configurar SHA-1 e SHA-256 no Firebase Console

### Passo 1: Obter SHA-1 e SHA-256 do seu projeto

No terminal (na raiz do projeto), execute:

**Windows (PowerShell):**
```powershell
cd app
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Se não encontrar o keystore de debug:**
O keystore de debug padrão geralmente está em:
- Windows: `C:\Users\<seu_usuario>\.android\debug.keystore`
- Linux/Mac: `~/.android/debug.keystore`

**Para obter SHA-1 e SHA-256 de forma mais fácil:**

No Android Studio:
1. Abra o painel **Gradle** (barra lateral direita)
2. Expanda: `app` > `Tasks` > `android`
3. Execute `signingReport`
4. Procure por **SHA1** e **SHA256** na saída

Ou use este comando direto:
```bash
gradlew signingReport
```

### Passo 2: Adicionar SHA-1 e SHA-256 no Firebase Console

1. Acesse [Firebase Console](https://console.firebase.google.com/)
2. Selecione o projeto **suasraizesvivas**
3. Vá em **Project Settings** (ícone de engrenagem)
4. Role até **Your apps** e selecione o app Android (`com.raizesvivas.app`)
5. Na seção **SHA certificate fingerprints**, clique em **Add fingerprint**
6. Cole o **SHA-1** e clique em **Save**
7. Clique novamente em **Add fingerprint** e cole o **SHA-256**
8. Salve as alterações

### Passo 3: Verificar se Authentication está habilitado

1. No Firebase Console, vá em **Authentication**
2. Na aba **Sign-in method**
3. Verifique se **Email/Password** está **Enabled**
4. Se não estiver, clique em **Email/Password** e habilite

### Passo 4: Baixar o google-services.json atualizado

1. No Firebase Console, vá em **Project Settings**
2. Em **Your apps**, clique no app Android
3. Clique em **Download google-services.json**
4. Substitua o arquivo `app/google-services.json` pelo novo
5. Faça um **Clean Project** e **Rebuild** no Android Studio

### Passo 5: Limpar cache e rebuild

No Android Studio:
1. **Build** > **Clean Project**
2. **Build** > **Rebuild Project**
3. Desinstale o app do dispositivo/emulador
4. Execute novamente

## Verificação Adicional

### Se o erro persistir:

1. **Verificar se o package name está correto:**
   - `applicationId` em `app/build.gradle.kts` deve ser: `com.raizesvivas.app`
   - Deve corresponder ao package name no Firebase Console

2. **Verificar google-services.json:**
   - O arquivo deve estar em `app/google-services.json`
   - O `package_name` no JSON deve ser `com.raizesvivas.app`
   - O `project_id` deve ser `suasraizesvivas`

3. **Verificar dependências do Firebase:**
   - Certifique-se de que as dependências do Firebase estão atualizadas em `app/build.gradle.kts`

## Nota sobre App Check

O warning sobre App Check (`No AppCheckProvider installed`) não é crítico e pode ser ignorado para desenvolvimento. O App Check é necessário apenas para produção com proteção adicional contra abuso.

## Resumo dos Passos Críticos

1. ✅ Obter SHA-1 e SHA-256 do projeto
2. ✅ Adicionar ambos no Firebase Console (Project Settings > Your apps)
3. ✅ Verificar se Email/Password está habilitado
4. ✅ Baixar novo `google-services.json` se necessário
5. ✅ Clean e Rebuild do projeto
6. ✅ Desinstalar app antigo e executar novamente

Após seguir esses passos, o erro `CONFIGURATION_NOT_FOUND` deve ser resolvido e você conseguirá criar contas normalmente.

