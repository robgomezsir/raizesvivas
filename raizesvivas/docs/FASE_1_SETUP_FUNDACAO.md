# FASE 1: SETUP E FUNDAÇÃO - RAÍZES VIVAS

## 🎯 OBJETIVO DA FASE

Estabelecer a base sólida do projeto com configuração completa do ambiente de desenvolvimento, autenticação via Supabase e estrutura modular seguindo Clean Architecture.

**Duração**: 2 semanas (10 dias úteis)  
**Entregável**: Projeto Android funcional com autenticação e estrutura base

---

## 📋 ENTREGÁVEIS DETALHADOS

### 1. Setup do Projeto Android
- [ ] Projeto Kotlin + Jetpack Compose configurado
- [ ] Estrutura de módulos criada
- [ ] Dependências configuradas (Hilt, Navigation, etc.)
- [ ] Projeto compila sem erros

### 2. Configuração do Supabase
- [ ] Projeto Supabase criado e configurado
- [ ] Schema de banco de dados criado
- [ ] RLS (Row Level Security) configurado
- [ ] Cliente Supabase integrado ao Android

### 3. Sistema de Autenticação
- [ ] Login/Registro funcionando
- [ ] Gerenciamento de sessão
- [ ] Navegação baseada em estado de autenticação
- [ ] Testes de autenticação

### 4. Estrutura Base
- [ ] Clean Architecture implementada
- [ ] Módulos organizados
- [ ] Injeção de dependência configurada
- [ ] Navegação base funcionando

---

## 🏗️ ESTRUTURA DE MÓDULOS

### Módulos a Criar

```
raizes-vivas/
├── settings.gradle.kts
├── build.gradle.kts (root)
├── gradle.properties
│
├── :app/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/raizesvivas/
│       ├── RaizesVivasApp.kt
│       ├── MainActivity.kt
│       └── di/AppModule.kt
│
├── :core:data/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/raizesvivas/core/data/
│       ├── source/remote/SupabaseClient.kt
│       └── source/remote/SupabaseAuthClient.kt
│
├── :core:domain/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/raizesvivas/core/domain/
│       ├── model/User.kt
│       └── repository/AuthRepository.kt
│
├── :core:ui/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/raizesvivas/core/ui/
│       ├── theme/Color.kt
│       ├── theme/Theme.kt
│       ├── theme/Type.kt
│       └── components/LoadingScreen.kt
│
└── :feature:auth/
    ├── build.gradle.kts
    └── src/main/kotlin/com/raizesvivas/feature/auth/
        ├── presentation/screen/
        │   ├── LoginScreen.kt
        │   └── RegisterScreen.kt
        ├── presentation/viewmodel/
        │   ├── AuthViewModel.kt
        │   └── AuthState.kt
        └── data/repository/AuthRepositoryImpl.kt
```

---

## 📅 CRONOGRAMA DETALHADO

### **DIA 1-2: Setup do Projeto**

#### Dia 1: Configuração Inicial
- [ ] Criar projeto Android no Android Studio
- [ ] Configurar Kotlin 1.9+ e Compose BOM
- [ ] Configurar estrutura de módulos
- [ ] Configurar dependências básicas

#### Dia 2: Configuração de Módulos
- [ ] Criar módulos :core:data, :core:domain, :core:ui
- [ ] Configurar Hilt para injeção de dependência
- [ ] Configurar Navigation Compose
- [ ] Testar compilação de todos os módulos

### **DIA 3-4: Configuração do Supabase**

#### Dia 3: Setup Supabase
- [ ] Criar projeto no Supabase
- [ ] Configurar autenticação (email/password)
- [ ] Criar schema inicial de tabelas
- [ ] Configurar RLS básico

#### Dia 4: Integração Android-Supabase
- [ ] Adicionar dependência do Supabase Android
- [ ] Configurar cliente Supabase
- [ ] Criar AuthRepository
- [ ] Testar conexão

### **DIA 5-7: Sistema de Autenticação**

#### Dia 5: Tela de Login
- [ ] Criar LoginScreen com Compose
- [ ] Implementar AuthViewModel
- [ ] Conectar com Supabase Auth
- [ ] Validação de campos

#### Dia 6: Tela de Registro
- [ ] Criar RegisterScreen
- [ ] Implementar registro de usuário
- [ ] Validação de email e senha
- [ ] Feedback de erros

#### Dia 7: Navegação e Estado
- [ ] Configurar navegação baseada em auth
- [ ] Gerenciar estado de autenticação
- [ ] Implementar logout
- [ ] Persistir sessão

### **DIA 8-10: Polimento e Testes**

#### Dia 8: Testes Unitários
- [ ] Testes para AuthViewModel
- [ ] Testes para AuthRepository
- [ ] Mocks para Supabase
- [ ] Cobertura > 80%

#### Dia 9: UI/UX e Acessibilidade
- [ ] Implementar tema do Raízes Vivas
- [ ] Loading states
- [ ] Mensagens de erro amigáveis
- [ ] Acessibilidade básica

#### Dia 10: Documentação e Validação
- [ ] Atualizar README
- [ ] Documentar configuração
- [ ] Validação completa da fase
- [ ] Preparar para Fase 2

---

## 🔧 CONFIGURAÇÕES TÉCNICAS

### Dependências Principais

#### build.gradle.kts (root)
```kotlin
plugins {
    id("com.android.application") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
```

#### build.gradle.kts (:app)
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.raizesvivas"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.raizesvivas"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Supabase
    implementation("io.github.jan-tennert.supabase:auth-kt:2.1.3")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.1.3")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.1.3")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Modules
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(project(":feature:auth"))
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

### Configuração do Supabase

#### SupabaseClient.kt
```kotlin
package com.raizesvivas.core.data.source.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "YOUR_SUPABASE_URL",
        supabaseKey = "YOUR_SUPABASE_ANON_KEY"
    ) {
        install(Auth)
        install(Postgrest)
    }
}
```

---

## 🎨 DESIGN SYSTEM INICIAL

### Cores do Raízes Vivas
```kotlin
// core/ui/theme/Color.kt
val FamilyZeroGold = Color(0xFFFFD700)
val SubfamilyGreen = Color(0xFF4CAF50)
val RootBrown = Color(0xFF5D4037)
val BarkBrown = Color(0xFF8D6E63)
val TrunkBeige = Color(0xFFA1887F)
val BranchGreen = Color(0xFF689F38)
val LeafGreen = Color(0xFF8BC34A)
val FlowerPink = Color(0xFFE91E63)
val PollinatorOrange = Color(0xFFFFA726)
val BirdBlue = Color(0xFF42A5F5)
```

### Tema Principal
```kotlin
// core/ui/theme/Theme.kt
private val LightColorScheme = lightColorScheme(
    primary = BranchGreen,
    onPrimary = Color.White,
    secondary = TrunkBeige,
    onSecondary = Color.White,
    tertiary = FlowerPink,
    background = Color(0xFFFFFBFE),
    surface = Color.White,
    error = Color(0xFFB00020)
)
```

---

## 🧪 TESTES OBRIGATÓRIOS

### Testes de Autenticação
```kotlin
// feature/auth/src/test/kotlin/AuthViewModelTest.kt
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var viewModel: AuthViewModel
    private lateinit var authRepository: FakeAuthRepository
    
    @Before
    fun setup() {
        authRepository = FakeAuthRepository()
        viewModel = AuthViewModel(authRepository)
    }
    
    @Test
    fun `login with valid credentials - success`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        
        // Act
        viewModel.login(email, password)
        
        // Assert
        assertTrue(viewModel.state.value is AuthState.Authenticated)
    }
    
    @Test
    fun `login with invalid credentials - error`() = runTest {
        // Arrange
        val email = "invalid@example.com"
        val password = "wrongpassword"
        
        // Act
        viewModel.login(email, password)
        
        // Assert
        assertTrue(viewModel.state.value is AuthState.Error)
    }
}
```

---

## ✅ CHECKLIST DE VALIDAÇÃO DA FASE 1

### Setup do Projeto
- [ ] Projeto compila sem erros ou warnings
- [ ] Todos os módulos criados e configurados
- [ ] Dependências resolvidas corretamente
- [ ] Estrutura de pastas organizada

### Supabase
- [ ] Projeto Supabase criado e configurado
- [ ] Autenticação funcionando (email/password)
- [ ] Cliente Android conectado
- [ ] RLS básico configurado

### Autenticação
- [ ] Login funcionando
- [ ] Registro funcionando
- [ ] Logout funcionando
- [ ] Persistência de sessão
- [ ] Navegação baseada em auth

### Qualidade
- [ ] Testes unitários passando (>80% cobertura)
- [ ] Testes de integração passando
- [ ] Loading states implementados
- [ ] Mensagens de erro claras
- [ ] Acessibilidade básica

### Documentação
- [ ] README atualizado
- [ ] Comentários em código crítico
- [ ] Changelog da fase
- [ ] Instruções de setup

---

## 🚀 PRÓXIMOS PASSOS

Após completar a Fase 1, estaremos prontos para:

**Fase 2: Família-Zero e Membros**
- Criação de família-zero
- CRUD de membros
- Validações de dados
- Primeira visualização

---

## ⚠️ RISCOS E MITIGAÇÕES

### Riscos Identificados
1. **Configuração do Supabase**: Pode ter problemas de conectividade
   - *Mitigação*: Testar configuração em ambiente isolado primeiro

2. **Dependências**: Versões incompatíveis
   - *Mitigação*: Usar versões estáveis e testadas

3. **Autenticação**: Complexidade do fluxo
   - *Mitigação*: Implementar passo a passo com testes

### Critérios de Sucesso
- ✅ Projeto compila e roda
- ✅ Login/registro funcionando
- ✅ Navegação fluida
- ✅ Testes passando
- ✅ Documentação completa

---

**Esta fase estabelece a fundação sólida para todo o projeto Raízes Vivas. Cada item deve ser completado e validado antes de prosseguir para a Fase 2.**
