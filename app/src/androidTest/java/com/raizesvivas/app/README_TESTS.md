# Testes E2E - Raízes Vivas

## Estrutura de Testes

Este diretório contém testes de integração e UI tests para o aplicativo Raízes Vivas.

## Testes de Integração

### AutenticacaoIntegrationTest.kt
Testa o fluxo completo de autenticação:
- ✅ Cadastro de usuário
- ✅ Login de usuário
- ✅ Logout
- ✅ Login com credenciais inválidas

### PessoaIntegrationTest.kt
Testa operações CRUD de pessoas:
- ✅ Criação de pessoa
- ✅ Atualização de pessoa
- ✅ Deleção de pessoa
- ✅ Sincronização local↔remoto

### FamiliaZeroIntegrationTest.kt
Testa criação de Família Zero:
- ✅ Criação de Família Zero
- ✅ Verificação de atribuição de admin

## Como Executar

### Executar todos os testes
```bash
./gradlew connectedAndroidTest
```

### Executar testes específicos
```bash
./gradlew connectedAndroidTest --tests "com.raizesvivas.app.AutenticacaoIntegrationTest"
```

### Executar via Android Studio
1. Abra o arquivo de teste
2. Clique com botão direito no arquivo ou método
3. Selecione "Run 'TestName'"

## Requisitos

- Dispositivo Android ou Emulador conectado
- Firebase configurado (`google-services.json`)
- Internet conectada para testes com Firebase

## Dados de Teste

Os testes criam usuários e dados temporários com prefixo "TESTE -" que são automaticamente limpos após a execução.

## Cobertura

- Fluxo de autenticação: ✅ Completo
- Operações CRUD de pessoas: ✅ Completo
- Criação de Família Zero: ✅ Completo
- Sincronização local↔remoto: ✅ Completo

## Notas

- Os testes usam Firebase em modo de desenvolvimento/teste
- Certifique-se de que o projeto Firebase está configurado corretamente
- Alguns testes podem falhar se o Firebase estiver inacessível

