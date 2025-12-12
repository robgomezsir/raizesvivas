# Guia de Configuração do Firebase Storage

Este guia explica como configurar e aplicar as regras do Firebase Storage usando o Firebase CLI.

## 📋 Pré-requisitos

1. **Node.js instalado** (versão 14 ou superior)
2. **Firebase CLI instalado**
3. **Conta Google com acesso ao projeto Firebase**

## 🔧 Instalação do Firebase CLI

### Windows (PowerShell)
```powershell
npm install -g firebase-tools
```

### Verificar instalação
```powershell
firebase --version
```

## 🔐 Login no Firebase

1. **Fazer login no Firebase:**
```powershell
firebase login
```

Isso abrirá o navegador para autenticação. Após o login, você estará autenticado.

2. **Verificar projetos disponíveis:**
```powershell
firebase projects:list
```

## 📁 Estrutura de Arquivos

Seu projeto já está configurado corretamente:

```
raizesvivas/
├── .firebaserc          # Configuração do projeto Firebase
├── firebase.json        # Configuração dos serviços Firebase
└── storage.rules        # Regras de segurança do Storage
```

**Arquivos de configuração:**

- `.firebaserc`: Define o projeto padrão (`suasraizesvivas`)
- `firebase.json`: Configura os serviços (Storage, Firestore, Functions)
- `storage.rules`: Regras de segurança do Storage

## 🚀 Aplicar as Storage Rules

### Opção 1: Deploy apenas das Storage Rules

```powershell
firebase deploy --only storage
```

### Opção 2: Deploy de todas as regras (Storage + Firestore)

```powershell
firebase deploy --only firestore:rules,storage
```

### Opção 3: Deploy completo (Storage + Firestore + Functions)

```powershell
firebase deploy
```

## ✅ Verificar se as Rules foram Aplicadas

### 1. Via Console do Firebase

1. Acesse: https://console.firebase.google.com/
2. Selecione o projeto `suasraizesvivas`
3. Vá em **Storage** → **Rules**
4. Verifique se as regras estão atualizadas

### 2. Via CLI

```powershell
firebase storage:rules:get
```

## 🧪 Testar as Rules Localmente (Opcional)

### 1. Iniciar emulador local

```powershell
firebase emulators:start --only storage
```

### 2. Testar com o emulador

O emulador estará disponível em: `http://localhost:9199`

## 📝 Comandos Úteis do Firebase CLI

### Verificar configuração atual
```powershell
firebase use
```

### Trocar de projeto
```powershell
firebase use <project-id>
```

### Ver logs de deploy
```powershell
firebase deploy --only storage --debug
```

### Validar rules sem fazer deploy
```powershell
firebase storage:rules:validate
```

## 🔍 Verificar Configuração do Storage

### 1. Verificar firebase.json

O arquivo `firebase.json` já está configurado:

```json
{
  "storage": {
    "rules": "storage.rules"
  }
}
```

### 2. Verificar storage.rules

As regras atuais permitem:
- ✅ Qualquer usuário autenticado pode **ler** fotos
- ✅ Qualquer usuário autenticado pode **fazer upload** de fotos
- ✅ Validação de tipo de arquivo (JPEG, PNG, WebP)
- ✅ Limite de tamanho: 5MB

## 🛠️ Solução de Problemas

### Erro: "Permission denied"
- Verifique se você está logado: `firebase login`
- Verifique se tem permissões no projeto Firebase

### Erro: "Project not found"
- Verifique o projeto em `.firebaserc`
- Use: `firebase use suasraizesvivas`

### Erro: "Rules file not found"
- Verifique se `storage.rules` existe na raiz do projeto
- Verifique o caminho em `firebase.json`

### Erro de sintaxe nas rules
- Use: `firebase storage:rules:validate` para validar
- Verifique a sintaxe em: https://firebase.google.com/docs/storage/security

## 📚 Recursos Adicionais

- [Documentação Firebase Storage](https://firebase.google.com/docs/storage)
- [Documentação Storage Rules](https://firebase.google.com/docs/storage/security)
- [Firebase CLI Reference](https://firebase.google.com/docs/cli)

## ✅ Checklist de Configuração

- [ ] Firebase CLI instalado
- [ ] Login realizado (`firebase login`)
- [ ] Projeto configurado (`.firebaserc`)
- [ ] `firebase.json` configurado
- [ ] `storage.rules` atualizado
- [ ] Rules aplicadas (`firebase deploy --only storage`)
- [ ] Rules verificadas no Console Firebase

## 🎯 Próximos Passos

Após aplicar as rules:

1. **Teste no app**: Tente fazer upload de uma foto de perfil
2. **Monitore logs**: Verifique se há erros de permissão
3. **Ajuste se necessário**: Modifique `storage.rules` e faça novo deploy

---

**Nota**: As regras são aplicadas imediatamente após o deploy. Não é necessário reiniciar o app.

