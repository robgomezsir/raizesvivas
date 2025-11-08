# Configuração Completa do Firebase - Raízes Vivas

Este documento detalha o passo a passo completo para configurar o Firebase para o aplicativo Raízes Vivas.

---

## 📋 Pré-requisitos

- Conta Google (para acessar Firebase Console)
- Projeto Android criado no Android Studio
- Android Studio atualizado

---

## 🔥 Passo 1: Criar Projeto no Firebase

1. Acesse o [Firebase Console](https://console.firebase.google.com/)
2. Clique em **"Adicionar projeto"** ou **"Criar um projeto"**
3. Preencha o nome do projeto: `raizes-vivas` (ou seu nome preferido)
4. **Desabilite** o Google Analytics (opcional, pode habilitar depois)
5. Clique em **"Criar projeto"**
6. Aguarde a criação (pode levar alguns minutos)

---

## 📱 Passo 2: Adicionar App Android ao Firebase

1. No painel do projeto Firebase, clique no ícone **Android** (`</>`)
2. Preencha:
   - **Nome do pacote Android**: `com.raizesvivas.app`
   - **Apelido do aplicativo** (opcional): `Raízes Vivas`
   - **Certificado de assinatura** (opcional, deixe em branco)
3. Clique em **"Registrar app"**
4. **Baixe o arquivo `google-services.json`**
5. Copie o arquivo para `app/google-services.json` no seu projeto Android
6. Verifique se o arquivo `build.gradle.kts` (projeto) tem o plugin:
   ```kotlin
   plugins {
       id("com.google.gms.google-services") version "4.4.0" apply false
   }
   ```
7. Verifique se o arquivo `app/build.gradle.kts` aplica o plugin:
   ```kotlin
   plugins {
       id("com.google.gms.google-services")
   }
   ```

---

## 🔐 Passo 3: Configurar Firebase Authentication

1. No menu lateral do Firebase Console, clique em **"Authentication"**
2. Clique em **"Começar"** (primeira vez)
3. Na aba **"Sign-in method"**, clique em **"Email/Password"**
4. **Habilite** o primeiro switch (Email/Password)
5. Clique em **"Salvar"**

**Métodos de autenticação ativados:**
- ✅ Email/Password

---

## 💾 Passo 4: Criar Firestore Database

1. No menu lateral, clique em **"Firestore Database"**
2. Clique em **"Criar banco de dados"**
3. Escolha o modo:
   - **Produção** (recomendado para produção)
   - **Modo de teste** (apenas para desenvolvimento, regras permissivas)
4. Escolha a localização (ex: `southamerica-east1` para Brasil)
5. Clique em **"Habilitar"**
6. Aguarde a criação do banco (pode levar alguns minutos)

---

## 📦 Passo 5: Criar Coleções no Firestore

As coleções serão criadas automaticamente quando o app começar a usar, mas você pode criar manualmente para melhor organização:

### Coleção: `users`
**Descrição:** Armazena informações dos usuários do aplicativo

**Campos esperados:**
```
users/{userId}
  ├── nome: string
  ├── email: string
  ├── fotoUrl: string? (nullable)
  ├── pessoaVinculada: string? (nullable, ID da pessoa vinculada)
  ├── ehAdministrador: boolean
  ├── familiaZeroPai: string? (nullable, ID da pessoa "pai" da família zero)
  ├── familiaZeroMae: string? (nullable, ID da pessoa "mãe" da família zero)
  ├── primeiroAcesso: boolean
  └── criadoEm: timestamp
```

**Como criar:**
1. No Firestore Console, clique em **"Iniciar coleção"**
2. **ID da coleção**: `users`
3. **ID do documento**: `test_user_1` (temporário para estrutura)
4. Adicione os campos acima (pode deletar o documento depois)

---

### Coleção: `people`
**Descrição:** Armazena todas as pessoas da árvore genealógica

**Campos esperados:**
```
people/{pessoaId}
  ├── nome: string
  ├── nomeNormalizado: string (nome em lowercase para buscas)
  ├── dataNascimento: timestamp? (nullable)
  ├── dataFalecimento: timestamp? (nullable)
  ├── localNascimento: string? (nullable)
  ├── localResidencia: string? (nullable)
  ├── profissao: string? (nullable)
  ├── biografia: string? (nullable)
  ├── pai: string? (nullable, ID da pessoa pai)
  ├── mae: string? (nullable, ID da pessoa mãe)
  ├── conjugeAtual: string? (nullable, ID da pessoa cônjuge)
  ├── exConjuges: array<string> (IDs de ex-cônjuges)
  ├── filhos: array<string> (IDs dos filhos)
  ├── fotoUrl: string? (nullable)
  ├── criadoPor: string (ID do usuário que criou)
  ├── criadoEm: timestamp
  ├── modificadoPor: string? (nullable)
  ├── modificadoEm: timestamp? (nullable)
  ├── aprovado: boolean
  ├── versao: number
  ├── ehFamiliaZero: boolean
  └── distanciaFamiliaZero: number
```

**Como criar:**
1. Clique em **"Iniciar coleção"**
2. **ID da coleção**: `people`
3. **ID do documento**: `test_person_1` (temporário)
4. Adicione os campos acima

---

### Coleção: `familia_zero`
**Descrição:** Armazena a informação da Família Zero (primeira família criada)

**Campos esperados:**
```
familia_zero/{familiaId}  // ID sempre "raiz" (singleton)
  ├── pai: string (ID do patriarca)
  ├── mae: string (ID da matriarca)
  ├── fundadoPor: string (ID do usuário que fundou)
  ├── fundadoEm: timestamp
  ├── locked: boolean (sempre true)
  └── arvoreNome: string (nome da árvore, ex: "Família Silva")
```

**Importante:** 
- O ID do documento é sempre `"raiz"` (singleton)
- Apenas **um documento** deve existir nesta coleção
- O campo `locked` é sempre `true` para impedir deleção acidental
- Esta coleção é criada automaticamente pelo app quando o usuário cria a Família Zero

**Como criar:**
1. Clique em **"Iniciar coleção"** (opcional - será criada automaticamente pelo app)
2. **ID da coleção**: `familia_zero`
3. **ID do documento**: `raiz` (fixo, sempre este ID)
4. Adicione os campos acima (ou deixe o app criar automaticamente quando o primeiro usuário criar a Família Zero)

---

### Coleção: `invites`
**Descrição:** Armazena convites para participar da árvore genealógica

**Campos esperados:**
```
invites/{conviteId}
  ├── id: string
  ├── emailConvidado: string
  ├── pessoaVinculada: string? (nullable, ID da pessoa que o convite está vinculado)
  ├── convidadoPor: string (ID do usuário que enviou o convite)
  ├── status: string (enum: "PENDENTE" | "ACEITO" | "REJEITADO" | "EXPIRADO")
  ├── criadoEm: timestamp
  └── expiraEm: timestamp (data de expiração, 7 dias após criação)
```

**Como criar:**
1. Clique em **"Iniciar coleção"**
2. **ID da coleção**: `invites`
3. **ID do documento**: `test_invite_1` (temporário)
4. Adicione os campos acima

---

### Coleção: `pending_edits`
**Descrição:** Armazena edições pendentes de aprovação (para usuários não-admin)

**Campos esperados:**
```
pending_edits/{edicaoId}
  ├── id: string
  ├── pessoaId: string (ID da pessoa sendo editada)
  ├── camposAlterados: map<string, any> (mapa com campos alterados)
  ├── editadoPor: string (ID do usuário que editou)
  ├── status: string (enum: "PENDENTE" | "APROVADA" | "REJEITADA")
  ├── criadoEm: timestamp
  ├── revisadoPor: string? (nullable, ID do admin que revisou)
  └── revisadoEm: timestamp? (nullable)
```

**Como criar:**
1. Clique em **"Iniciar coleção"**
2. **ID da coleção**: `pending_edits`
3. **ID do documento**: `test_edit_1` (temporário)
4. Adicione os campos acima

---

### Coleção: `duplicates`
**Descrição:** Armazena registros de duplicatas detectadas (opcional, para histórico)

**Campos esperados:**
```
duplicates/{duplicataId}
  ├── id: string
  ├── pessoa1Id: string (ID da primeira pessoa)
  ├── pessoa2Id: string (ID da segunda pessoa)
  ├── scoreSimilaridade: number (0.0 a 1.0)
  ├── razoes: array<string> (razões da similaridade)
  ├── status: string (enum: "DETECTADA" | "RESOLVIDA" | "IGNORADA")
  ├── detectadoEm: timestamp
  └── resolvidoEm: timestamp? (nullable)
```

**Como criar:**
1. Clique em **"Iniciar coleção"**
2. **ID da coleção**: `duplicates`
3. **ID do documento**: `test_duplicate_1` (temporário)
4. Adicione os campos acima

---

## 🔒 Passo 6: Configurar Security Rules do Firestore

1. No Firestore Console, clique na aba **"Regras"**
2. Substitua as regras por:

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
      allow create, update: if isAuthenticated() && 
                              (request.auth.uid == userId || isAdmin());
      
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
      
      // Apenas usuário autenticado pode criar (primeiro acesso)
      allow create: if isAuthenticated() && 
                     request.resource.data.criadoPor == request.auth.uid;
      
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

3. Clique em **"Publicar"**
4. **Importante:** Em modo de produção, revise cuidadosamente as regras antes de publicar.

---

## 📊 Passo 7: Criar Índices Compostos no Firestore

O Firestore criará índices automaticamente quando necessário, mas você pode criar manualmente ou importar via arquivo JSON.

### Opção 1: Importar via Arquivo JSON (Recomendado)

1. No Firestore Console, clique na aba **"Índices"**
2. Clique em **"Implantar índice do arquivo"** (ou **"Import indexes"**)
3. Faça upload do arquivo `firestore.indexes.json` (já incluído no projeto)
4. Aguarde a criação dos índices (pode levar alguns minutos)

### Opção 2: Criar Manualmente

#### Índice 1: `people` - Busca por nome normalizado e aprovação
**Uso:** Buscar pessoas aprovadas por nome (case-insensitive)

**Campos:**
- `nomeNormalizado` (Ascending)
- `aprovado` (Ascending)

**Como criar:**
1. No Firestore Console, clique na aba **"Índices"**
2. Clique em **"Criar índice"**
3. **Coleção ID**: `people`
4. Adicione campos:
   - Campo: `nomeNormalizado`, Ordem: `Ascendente`
   - Campo: `aprovado`, Ordem: `Ascendente`
5. Clique em **"Criar"**

---

#### Índice 2: `people` - Buscar pessoas aprovadas por data de criação
**Uso:** Ordenar pessoas aprovadas por data de criação

**Campos:**
- `aprovado` (Ascending)
- `criadoEm` (Descending)

**Como criar:**
1. Clique em **"Criar índice"**
2. **Coleção ID**: `people`
3. Adicione campos:
   - Campo: `aprovado`, Ordem: `Ascendente`
   - Campo: `criadoEm`, Ordem: `Descendente`
4. Clique em **"Criar"**

---

#### Índice 3: `people` - Buscar por relacionamentos (pai e mãe)
**Uso:** Buscar filhos de um casal específico

**Campos:**
- `pai` (Ascending)
- `mae` (Ascending)
- `dataNascimento` (Ascending)

**Como criar:**
1. Clique em **"Criar índice"**
2. **Coleção ID**: `people`
3. Adicione campos:
   - Campo: `pai`, Ordem: `Ascendente`
   - Campo: `mae`, Ordem: `Ascendente`
   - Campo: `dataNascimento`, Ordem: `Ascendente`
4. Clique em **"Criar"**

---

#### Índice 4: `invites` - Buscar convites por email e status
**Uso:** Buscar convites pendentes por email

**Campos:**
- `emailConvidado` (Ascending)
- `status` (Ascending)

**Como criar:**
1. Clique em **"Criar índice"**
2. **Coleção ID**: `invites`
3. Adicione campos:
   - Campo: `emailConvidado`, Ordem: `Ascendente`
   - Campo: `status`, Ordem: `Ascendente`
4. Clique em **"Criar"**

---

#### Índice 5: `pending_edits` - Buscar edições pendentes por status
**Uso:** Buscar edições pendentes ordenadas por data

**Campos:**
- `status` (Ascending)
- `criadoEm` (Descending)

**Como criar:**
1. Clique em **"Criar índice"**
2. **Coleção ID**: `pending_edits`
3. Adicione campos:
   - Campo: `status`, Ordem: `Ascendente`
   - Campo: `criadoEm`, Ordem: `Descendente`
4. Clique em **"Criar"**

---

#### Índice 6: `duplicates` - Buscar duplicatas por data de resolução
**Uso:** Buscar duplicatas não resolvidas

**Campos:**
- `resolvidoEm` (Ascending)

**Como criar:**
1. Clique em **"Criar índice"**
2. **Coleção ID**: `duplicates`
3. Adicione campos:
   - Campo: `resolvidoEm`, Ordem: `Ascendente`
4. Clique em **"Criar"**

---

### Arquivo firestore.indexes.json

O projeto já inclui um arquivo `firestore.indexes.json` na raiz com todos os índices necessários. Você pode importá-lo diretamente no Firebase Console.

---

## 📦 Passo 8: Configurar Firebase Storage

1. No menu lateral, clique em **"Storage"**
2. Clique em **"Começar"** (primeira vez)
3. Escolha o modo:
   - **Modo de produção** (recomendado)
   - **Modo de teste** (apenas para desenvolvimento)
4. Escolha a localização (mesma do Firestore)
5. Clique em **"Concluído"**

---

## 🗂️ Passo 9: Configurar Storage Rules

1. Na aba **"Regras"** do Storage, substitua por:

```javascript
rules_version = '2';

service firebase.storage {
  match /b/{bucket}/o {
    
    // Helper function: verifica se usuário está autenticado
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Helper function: verifica se usuário é admin
    function isAdmin() {
      return isAuthenticated() && 
             firestore.get(/databases/(default)/documents/users/$(request.auth.uid)).data.ehAdministrador == true;
    }
    
    // ============================================
    // PASTA: pessoas/{pessoaId}/
    // ============================================
    match /pessoas/{pessoaId}/{fileName} {
      // Qualquer usuário autenticado pode ler fotos
      allow read: if isAuthenticated();
      
      // Apenas admin ou dono da pessoa pode fazer upload
      allow write: if isAuthenticated() && 
                     (isAdmin() || 
                      firestore.get(/databases/(default)/documents/people/$(pessoaId)).data.criadoPor == request.auth.uid);
      
      // Validar tipo de arquivo (apenas imagens)
      allow write: if request.resource.contentType.matches('image/.*');
      
      // Validar tamanho máximo (5MB)
      allow write: if request.resource.size < 5 * 1024 * 1024;
    }
  }
}
```

2. Clique em **"Publicar"**

---

## ✅ Passo 10: Verificar Configuração

### Checklist Final:

- [ ] Projeto Firebase criado
- [ ] App Android adicionado ao Firebase
- [ ] Arquivo `google-services.json` copiado para `app/`
- [ ] Firebase Authentication ativado (Email/Password)
- [ ] Firestore Database criado
- [ ] Todas as 6 coleções criadas (ou estrutura conhecida)
- [ ] Security Rules do Firestore configuradas e publicadas
- [ ] Índices compostos criados
- [ ] Firebase Storage ativado
- [ ] Storage Rules configuradas
- [ ] Projeto Android compila sem erros
- [ ] Teste de conexão Firebase bem-sucedido

---

## 🧪 Passo 11: Testar Conexão Firebase

1. Execute o app no emulador/dispositivo
2. Verifique os logs do Logcat (filtre por "Firebase" ou "Timber")
3. Você deve ver logs como:
   ```
   ✅ Firebase inicializado
   ✅ Conexão com Firestore estabelecida
   ```

---

## 📝 Notas Importantes

### Segurança
- **Nunca** exponha suas chaves de API em código público
- Revise as Security Rules antes de publicar em produção
- Use modo de teste apenas durante desenvolvimento

### Performance
- Os índices compostos são criados automaticamente quando necessário
- Aguarde alguns minutos após criar índices para que fiquem ativos
- Para coleções grandes, considere usar paginação

### Custos
- Firestore oferece plano gratuito com limites generosos
- Monitore o uso no Console Firebase
- Configure alertas de orçamento

---

## 🔗 Links Úteis

- [Firebase Console](https://console.firebase.google.com/)
- [Documentação Firestore](https://firebase.google.com/docs/firestore)
- [Documentação Firebase Auth](https://firebase.google.com/docs/auth)
- [Documentação Firebase Storage](https://firebase.google.com/docs/storage)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)

---

## 🆘 Troubleshooting

### Erro: "google-services.json não encontrado"
- Verifique se o arquivo está em `app/google-services.json`
- Verifique se o package name está correto

### Erro: "Permission denied" ao ler/escrever
- Verifique as Security Rules do Firestore
- Verifique se o usuário está autenticado
- Verifique se o usuário tem permissão conforme as regras

### Erro: "Index required" ao fazer query
- O Firestore mostrará um link para criar o índice automaticamente
- Clique no link e aguarde alguns minutos

### Erro: "Storage permission denied"
- Verifique as Storage Rules
- Verifique se o arquivo é uma imagem
- Verifique o tamanho do arquivo (máx 5MB)

---

**Última atualização:** 2024
**Versão do documento:** 1.0

