# Correção do Package Name no Firebase

## Problema
O arquivo `google-services.json` está configurado para o package name `com.raizesvivas`, mas o app usa `com.raizesvivas.app`.

## Solução Recomendada: Adicionar App no Firebase Console

### Passo 1: Acesse o Firebase Console
1. Vá para https://console.firebase.google.com/
2. Selecione o projeto: **raizesvivasempre**

### Passo 2: Adicionar Novo App Android
1. No menu lateral, clique em **⚙️ Configurações do projeto**
2. Role até a seção **Seus apps**
3. Clique em **Adicionar app** → Escolha **Android**
4. Preencha os dados:
   - **Nome do pacote Android**: `com.raizesvivas.app`
   - **Apelido do app** (opcional): Raizes Vivas App
   - **Certificado de depuração SHA-1** (opcional para agora)
5. Clique em **Registrar app**

### Passo 3: Baixar o novo google-services.json
1. Após registrar o app, você verá instruções para baixar o `google-services.json`
2. Clique em **Baixar google-services.json**
3. Substitua o arquivo `app/google-services.json` pelo novo arquivo baixado

### Passo 4: Verificar se funciona
Execute o build novamente:
```bash
./gradlew clean build
```

## Solução Alternativa: Atualizar Package Name no App

Se preferir manter o Firebase como está (`com.raizesvivas`), você pode alterar o package name do app:

### Arquivo: `app/build.gradle.kts`
```kotlin
android {
    namespace = "com.raizesvivas.app"  // Manter para Kotlin
    // ...
    defaultConfig {
        applicationId = "com.raizesvivas"  // Mudar para corresponder ao Firebase
        // ...
    }
}
```

**NOTA**: Esta abordagem requer refatorar muitos arquivos e pode causar problemas com referências existentes. **NÃO É RECOMENDADO**.

## Recomendação
Use a **Solução Recomendada** (adicionar novo app no Firebase) para manter a consistência do package name em todo o projeto.

