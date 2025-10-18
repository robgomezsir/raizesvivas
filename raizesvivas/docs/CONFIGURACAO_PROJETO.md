# ⚙️ Configuração do Projeto - Raízes Vivas

Este documento contém todas as informações necessárias para configurar e executar o projeto Raízes Vivas.

## 🎯 **Visão Geral**

O Raízes Vivas é um sistema completo de genealogia gamificada que combina:
- **Árvores Genealógicas** interativas e visuais
- **Algoritmo de Parentesco** preciso e confiável
- **Sistema de Gamificação** com conquistas e pontos
- **Floresta de Famílias** para visualização global

## 🏗️ **Arquitetura do Projeto**

### **Estrutura de Módulos**
```
raizes-vivas/
├── app/                    # Aplicação principal
├── core/                   # Funcionalidades compartilhadas
│   ├── data/              # Fontes de dados (Room + Supabase)
│   ├── domain/            # Lógica de negócio (Models + UseCases)
│   ├── ui/                # Componentes UI reutilizáveis
│   └── utils/             # Utilitários e algoritmos
└── feature/               # Funcionalidades específicas
    ├── auth/              # Autenticação
    ├── family/            # Gestão de famílias
    ├── member/            # Gestão de membros
    ├── relationship/      # Relacionamentos
    ├── tree/              # Visualização da árvore
    └── gamification/      # Gamificação
```

### **Tecnologias Utilizadas**
- **Android**: Kotlin + Jetpack Compose
- **Backend**: Supabase (PostgreSQL + Auth + Storage)
- **Local**: Room Database
- **Arquitetura**: Clean Architecture + MVVM
- **DI**: Hilt
- **Navegação**: Navigation Compose
- **UI**: Material 3 + Compose

## 🚀 **Configuração Inicial**

### **Pré-requisitos**
- Android Studio Arctic Fox ou superior
- JDK 8 ou superior
- Conta no Supabase
- Git

### **Passos de Configuração**

#### 1. **Clone do Repositório**
```bash
git clone https://github.com/raizesvivas/raizes-vivas.git
cd raizes-vivas
```

#### 2. **Configuração do Supabase**
1. Crie um projeto no [Supabase](https://supabase.com)
2. Configure as variáveis de ambiente
3. Execute as migrations (veja [`SUPABASE_CONFIG.md`](./SUPABASE_CONFIG.md))
4. Configure RLS policies

#### 3. **Configuração do Android Studio**
1. Abra o projeto no Android Studio
2. Sincronize as dependências
3. Configure o SDK do Android
4. Execute o projeto

## 📱 **Configuração do Aplicativo**

### **Variáveis de Ambiente**
```kotlin
// core/data/src/main/kotlin/com/raizesvivas/core/data/source/remote/SupabaseClient.kt
object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "YOUR_SUPABASE_URL", // Substituir pela URL do Supabase
        supabaseKey = "YOUR_SUPABASE_ANON_KEY" // Substituir pela chave anon do Supabase
    ) {
        install(GoTrue)
        install(Postgrest)
        install(Storage)
    }
}
```

### **Configuração do Supabase**
1. **URL do Projeto**: `https://your-project.supabase.co`
2. **Chave Anônima**: `your-anon-key`
3. **Configuração de Auth**: Habilitar email/password
4. **Configuração de Storage**: Habilitar para fotos

## 🗄️ **Configuração do Banco de Dados**

### **Schema Principal**
- **Tabela `usuarios`** - Dados dos usuários
- **Tabela `familias`** - Famílias e subfamílias
- **Tabela `membros`** - Membros das famílias
- **Tabela `relacionamentos`** - Relacionamentos familiares
- **Tabela `parentescos_calculados`** - Parentescos calculados
- **Tabela `conquistas`** - Conquistas do sistema
- **Tabela `conquistas_usuario`** - Conquistas dos usuários
- **Tabela `pontos_usuario`** - Pontos e níveis dos usuários

### **Configuração de RLS**
- **Políticas de segurança** configuradas
- **Acesso por usuário** implementado
- **Proteção de dados** garantida

## 🔧 **Configuração de Desenvolvimento**

### **Gradle**
```kotlin
// build.gradle.kts (Project)
plugins {
    id("com.android.application") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
}
```

### **Dependências Principais**
- **Compose BOM**: `2023.10.01`
- **Hilt**: `2.48`
- **Supabase**: `2.1.3`
- **Navigation**: `2.7.5`

### **Configuração de Build**
```kotlin
// gradle.properties
android.useComposeConfigurationV2=true
```

## 🧪 **Configuração de Testes**

### **Estrutura de Testes**
```
test/
├── unit/                  # Testes unitários
├── integration/           # Testes de integração
└── ui/                   # Testes de UI
```

### **Cobertura de Testes**
- **Lógica de Negócio**: 80%+
- **Algoritmo de Parentesco**: 100%
- **Use Cases**: 90%+

## 📊 **Configuração de Performance**

### **Otimizações Implementadas**
- **Índices de banco** para queries rápidas
- **Paginação** para listas grandes
- **Cache** de dados locais
- **Lazy loading** de componentes

### **Métricas de Performance**
- **Tela inicial**: < 3 segundos
- **Carregamento de árvore**: < 2 segundos
- **Cálculo de parentesco**: < 1 segundo

## 🔒 **Configuração de Segurança**

### **Autenticação**
- **Supabase Auth** implementado
- **JWT tokens** para autenticação
- **Refresh tokens** automáticos

### **Proteção de Dados**
- **RLS policies** no Supabase
- **Validação** de entrada de dados
- **Sanitização** de dados sensíveis

## 📱 **Configuração de Build**

### **Variantes de Build**
- **Debug**: Desenvolvimento
- **Release**: Produção
- **Staging**: Testes

### **Configuração de Release**
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

## 🚀 **Deploy e Distribuição**

### **Google Play Store**
1. **Gerar APK/AAB** de release
2. **Assinar** com chave de release
3. **Upload** para Play Console
4. **Configurar** metadados

### **Testes Internos**
1. **Build** de debug
2. **Instalar** em dispositivos de teste
3. **Validar** funcionalidades
4. **Corrigir** bugs encontrados

## 🔧 **Configuração de Ambiente**

### **Desenvolvimento**
- **Android Studio**: Arctic Fox+
- **SDK**: API 34
- **Emulador**: Pixel 6 Pro
- **Dispositivo**: Android 7.0+

### **Produção**
- **SDK**: API 24+
- **Dispositivos**: Android 7.0+
- **Orientação**: Portrait/Landscape
- **Idiomas**: Português (BR)

## 📋 **Checklist de Configuração**

### **Configuração Inicial**
- [ ] Projeto clonado
- [ ] Supabase configurado
- [ ] Android Studio configurado
- [ ] Dependências sincronizadas
- [ ] Projeto compilando

### **Configuração do Supabase**
- [ ] Projeto criado
- [ ] URL configurada
- [ ] Chave anônima configurada
- [ ] Auth habilitado
- [ ] Storage configurado
- [ ] Migrations executadas

### **Configuração do App**
- [ ] Variáveis de ambiente configuradas
- [ ] Supabase client configurado
- [ ] Auth funcionando
- [ ] Banco de dados conectado
- [ ] Upload de fotos funcionando

### **Testes**
- [ ] Testes unitários passando
- [ ] Testes de integração passando
- [ ] Testes de UI passando
- [ ] Cobertura de testes adequada

## 🆘 **Troubleshooting**

### **Problemas Comuns**
1. **Erro de compilação**: Verificar dependências
2. **Erro de Supabase**: Verificar configuração
3. **Erro de Auth**: Verificar chaves
4. **Erro de banco**: Verificar migrations

### **Logs Úteis**
- **Android Studio**: Logcat
- **Supabase**: Dashboard logs
- **App**: Console logs

## 📞 **Suporte**

### **Documentação**
- [README Principal](../README.md)
- [Documentação Completa](./README.md)
- [Configuração Supabase](./SUPABASE_CONFIG.md)

### **Links Úteis**
- [Supabase Docs](https://supabase.com/docs)
- [Android Docs](https://developer.android.com)
- [Compose Docs](https://developer.android.com/jetpack/compose)

---

**Última atualização**: 25 de Outubro de 2024  
**Versão**: 1.0.0  
**Status**: ✅ COMPLETO
