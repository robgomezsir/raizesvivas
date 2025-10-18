# CONFIGURAÇÃO DO SUPABASE - RAÍZES VIVAS

## 📋 PASSOS PARA CONFIGURAR O SUPABASE

### 1. Criar Projeto no Supabase
1. Acesse [supabase.com](https://supabase.com)
2. Faça login ou crie uma conta
3. Clique em "New Project"
4. Preencha os dados:
   - **Name**: Raizes Vivas
   - **Database Password**: [senha forte]
   - **Region**: [escolha a mais próxima]
5. Clique em "Create new project"

### 2. Obter Credenciais
1. No painel do projeto, vá para **Settings** > **API**
2. Copie as seguintes informações:
   - **Project URL**: `https://your-project.supabase.co`
   - **Anon public key**: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`

### 3. Configurar Autenticação
1. Vá para **Authentication** > **Settings**
2. Configure **Site URL**: `http://localhost:3000` (para desenvolvimento)
3. Em **Email**, configure:
   - **Enable email confirmations**: Desabilitado (para desenvolvimento)
   - **Enable email change confirmations**: Desabilitado (para desenvolvimento)

### 4. Atualizar Código
Substitua as credenciais no arquivo `core/data/src/main/kotlin/com/raizesvivas/core/data/source/remote/SupabaseClient.kt`:

```kotlin
private const val SUPABASE_URL = "SUA_URL_AQUI"
private const val SUPABASE_ANON_KEY = "SUA_CHAVE_AQUI"
```

### 5. Testar Conexão
1. Compile o projeto
2. Execute o aplicativo
3. Tente fazer login/registro
4. Verifique os logs para erros de conexão

## 🔧 CONFIGURAÇÕES ADICIONAIS

### Row Level Security (RLS)
O RLS será configurado automaticamente quando criarmos as tabelas na Fase 2.

### Schema do Banco
O schema completo será criado na Fase 2 com todas as tabelas necessárias.

## ⚠️ IMPORTANTE

- **NUNCA** commite as credenciais reais no repositório
- Use variáveis de ambiente para produção
- Mantenha as chaves seguras
- Configure domínios corretos para produção

## 📚 DOCUMENTAÇÃO

- [Supabase Android](https://supabase.com/docs/reference/android)
- [Supabase Auth](https://supabase.com/docs/guides/auth)
- [Supabase Database](https://supabase.com/docs/guides/database)
