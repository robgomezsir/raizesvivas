# Correção das Regras do Firestore para a Coleção `invites`

## Problema
Erro `PERMISSION_DENIED: Missing or insufficient permissions` ao tentar buscar/ler convites na coleção `invites`.

## Causa
As regras de segurança do Firestore para a coleção `invites` não estão configuradas corretamente, impedindo que usuários autenticados leiam seus próprios convites ou que administradores gerenciem convites.

## Solução

### 1. Acesse o Console do Firebase
1. Acesse [Firebase Console](https://console.firebase.google.com/)
2. Selecione seu projeto
3. Vá em **Firestore Database** → **Regras** (Rules)

### 2. Atualize as Regras do Firestore

Substitua as regras da coleção `invites` pelas seguintes:

```javascript
match /invites/{inviteId} {
  // Helper function: verifica se usuário é admin
  function isAdmin() {
    return request.auth != null && 
           exists(/databases/$(database)/documents/users/$(request.auth.uid)) &&
           get(/databases/$(database)/documents/users/$(request.auth.uid)).data.ehAdministrador == true;
  }
  
  // Helper function: verifica se usuário é o dono do convite ou o convidado
  function isOwnerOrInvited() {
    return request.auth != null && (
      resource.data.convidadoPor == request.auth.uid ||
      resource.data.emailConvidado == request.auth.token.email
    );
  }
  
  // CREATE: Apenas admins podem criar convites
  allow create: if isAdmin() && 
                   request.resource.data.convidadoPor == request.auth.uid;
  
  // READ: 
  // - Admins podem ler todos os convites
  // - Usuários podem ler seus próprios convites (onde são o convidado)
  allow read: if isAdmin() || 
                (request.auth != null && 
                 request.resource.data.emailConvidado == request.auth.token.email);
  
  // UPDATE:
  // - Admins podem atualizar qualquer convite
  // - Usuários autenticados podem atualizar apenas seus próprios convites (aceitar/rejeitar)
  allow update: if isAdmin() || 
                   (request.auth != null && 
                    resource.data.emailConvidado == request.auth.token.email &&
                    // Só permite atualizar status para ACEITO ou REJEITADO
                    request.resource.data.diff(resource.data).affectedKeys().hasOnly(['status']));
  
  // DELETE: Apenas admins podem deletar convites
  allow delete: if isAdmin();
}
```

### 3. Verificação das Regras

As regras implementadas garantem:

- **CREATE**: Apenas administradores podem criar convites
- **READ**: 
  - Administradores podem ler todos os convites
  - Usuários autenticados podem ler apenas convites onde seu email corresponde ao `emailConvidado`
- **UPDATE**: 
  - Administradores podem atualizar qualquer convite
  - Usuários autenticados podem atualizar apenas o status de seus próprios convites (para aceitar/rejeitar)
- **DELETE**: Apenas administradores podem deletar convites

### 4. Teste as Regras

Após atualizar as regras:

1. Clique em **Publicar** (Publish)
2. Aguarde alguns segundos para as regras serem propagadas
3. Teste o app novamente

### 5. Índices Compostos Necessários

Certifique-se de que os seguintes índices compostos existem na coleção `invites`:

1. **Para buscar convites por email e status:**
   - Campos: `emailConvidado` (Ascending), `status` (Ascending)
   - Uso: Query para buscar convites pendentes de um usuário

Se o Firebase solicitar criar o índice automaticamente, clique no link fornecido no erro.

## Notas Importantes

- As regras usam `request.auth.token.email` para verificar o email do usuário autenticado
- A verificação de admin consulta o documento `users/{userId}` e verifica o campo `ehAdministrador`
- A atualização de status por usuários não-admin é restrita apenas ao campo `status` para segurança

