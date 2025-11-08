# Como Corrigir o Erro de Permissão na Coleção familia_zero

## Problema
O erro `PERMISSION_DENIED: Missing or insufficient permissions` ocorre ao tentar criar o documento `familia_zero/raiz` porque a regra do Firestore está verificando um campo diferente do que o código está enviando.

## Solução: Atualizar as Regras do Firestore

### Passo 1: Acessar as Regras do Firestore

1. Acesse o [Firebase Console](https://console.firebase.google.com/)
2. Selecione o projeto **suasraizesvivas**
3. Vá em **Firestore Database**
4. Clique na aba **Regras**

### Passo 2: Corrigir a Regra da Coleção familia_zero

A regra atual está verificando `criadoPor`, mas o código envia `fundadoPor`. Atualize a regra para:

```javascript
// ============================================
// COLEÇÃO: familia_zero
// ============================================
match /familia_zero/{familiaId} {
  // Qualquer usuário autenticado pode ler
  allow read: if isAuthenticated();
  
  // Apenas usuário autenticado pode criar (primeiro acesso)
  // CORRIGIDO: Verifica fundadoPor em vez de criadoPor
  allow create: if isAuthenticated() && 
                 request.resource.data.fundadoPor == request.auth.uid &&
                 request.resource.data.keys().hasAll(['pai', 'mae', 'fundadoPor', 'fundadoEm', 'locked', 'arvoreNome']);
  
  // Apenas admin pode atualizar/deletar
  allow update, delete: if isAdmin();
}
```

### Passo 3: Regras Completas Corrigidas

Substitua toda a seção de regras no Firestore Console por:

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper function: verifica se usuário está autenticado
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Helper function: verifica se usuário é admin
    function isAdmin() {
      return isAuthenticated() && 
             get(/databases/$(database)/documents/users/$(request.auth.uid)).data.ehAdministrador == true;
    }
    
    // Helper function: verifica se usuário é o dono do documento
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    // ============================================
    // COLEÇÃO: users
    // ============================================
    match /users/{userId} {
      // Qualquer usuário autenticado pode ler qualquer usuário
      allow read: if isAuthenticated();
      
      // Usuário pode criar/atualizar apenas seu próprio documento
      // CORRIGIDO: Usando a função isOwner() que já está definida
      allow create, update: if isOwner(userId) || isAdmin();
      
      // Apenas admin pode deletar usuários
      allow delete: if isAdmin();
    }
    
    // ============================================
    // COLEÇÃO: people
    // ============================================
    match /people/{pessoaId} {
      // Qualquer usuário autenticado pode ler pessoas
      allow read: if isAuthenticated();
      
      // Usuário autenticado pode criar pessoas
      allow create: if isAuthenticated() && 
                     request.resource.data.keys().hasAll(['nome', 'criadoPor', 'criadoEm']);
      
      // Admin pode atualizar/deletar qualquer pessoa
      // Usuário comum pode atualizar apenas se criou ou se pessoa vinculada
      allow update: if isAuthenticated() && 
                     (isAdmin() || 
                      request.resource.data.criadoPor == request.auth.uid ||
                      get(/databases/$(database)/documents/users/$(request.auth.uid)).data.pessoaVinculada == pessoaId);
      
      // Apenas admin pode deletar pessoas
      allow delete: if isAdmin();
    }
    
    // ============================================
    // COLEÇÃO: familia_zero
    // ============================================
    match /familia_zero/{familiaId} {
      // Qualquer usuário autenticado pode ler
      allow read: if isAuthenticated();
      
      // CORRIGIDO: Verifica fundadoPor e valida campos obrigatórios
      allow create: if isAuthenticated() && 
                     request.resource.data.fundadoPor == request.auth.uid &&
                     request.resource.data.locked == true;
      
      // Apenas admin pode atualizar/deletar
      allow update, delete: if isAdmin();
    }
    
    // ============================================
    // COLEÇÃO: invites
    // ============================================
    match /invites/{conviteId} {
      // Usuário pode ler seus próprios convites (enviados ou recebidos)
      allow read: if isAuthenticated() && 
                   (resource.data.convidadoPor == request.auth.uid ||
                    resource.data.emailConvidado == get(/databases/$(database)/documents/users/$(request.auth.uid)).data.email);
      
      // Usuário autenticado pode criar convites
      allow create: if isAuthenticated();
      
      // Apenas o destinatário pode atualizar (aceitar/rejeitar)
      allow update: if isAuthenticated() && 
                      (resource.data.emailConvidado == get(/databases/$(database)/documents/users/$(request.auth.uid)).data.email ||
                       isAdmin());
      
      // Admin pode deletar convites
      allow delete: if isAdmin();
    }
    
    // ============================================
    // COLEÇÃO: pending_edits
    // ============================================
    match /pending_edits/{edicaoId} {
      // Usuário pode ler suas próprias edições pendentes
      // Admin pode ler todas
      allow read: if isAuthenticated() && 
                   (resource.data.editadoPor == request.auth.uid || isAdmin());
      
      // Usuário autenticado pode criar edições pendentes
      allow create: if isAuthenticated();
      
      // Apenas admin pode atualizar (aprovar/rejeitar)
      allow update: if isAdmin();
      
      // Admin pode deletar edições
      allow delete: if isAdmin();
    }
    
    // ============================================
    // COLEÇÃO: duplicates
    // ============================================
    match /duplicates/{duplicataId} {
      // Apenas admin pode ler
      allow read: if isAdmin();
      
      // Sistema pode criar (via admin ou app)
      allow create: if isAuthenticated();
      
      // Apenas admin pode atualizar/deletar
      allow update, delete: if isAdmin();
    }
  }
}
```

### Passo 4: Publicar as Regras

1. Copie as regras completas acima
2. Cole no editor de regras do Firestore Console
3. Clique em **"Publicar"**
4. Aguarde a confirmação (pode levar alguns segundos)

### Passo 5: Verificar se o Usuário Está Autenticado

Antes de criar a Família Zero, certifique-se de que:
1. O usuário fez login com sucesso
2. O usuário está autenticado no Firebase Auth
3. O UID do usuário está sendo passado corretamente no campo `fundadoPor`

### Passo 6: Testar Novamente

Após publicar as regras:
1. Faça logout e login novamente no app
2. Tente criar a Família Zero novamente
3. O erro `PERMISSION_DENIED` deve ser resolvido

## Resumo da Correção

**Problema:** A regra verificava `criadoPor` mas o código envia `fundadoPor`

**Solução:** Atualizar a regra para verificar `fundadoPor`:

```javascript
allow create: if isAuthenticated() && 
               request.resource.data.fundadoPor == request.auth.uid &&
               request.resource.data.locked == true;
```

## Nota Importante

Se o erro persistir após atualizar as regras:
1. Aguarde alguns minutos (as regras podem levar tempo para propagar)
2. Certifique-se de que o usuário está autenticado (não apenas logado no app)
3. Verifique se o campo `fundadoPor` está sendo enviado corretamente no código

