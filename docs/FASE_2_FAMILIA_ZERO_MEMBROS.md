# FASE 2: FAMÍLIA-ZERO E MEMBROS - RAÍZES VIVAS

## 🎯 OBJETIVO DA FASE

Implementar o sistema de famílias e membros, com foco especial na criação e gestão da família-zero (raiz da árvore genealógica) e CRUD completo de membros.

**Duração**: 2 semanas (10 dias úteis)  
**Entregável**: Sistema completo de gestão de famílias e membros funcionando

---

## 📋 ENTREGÁVEIS DETALHADOS

### 1. Sistema de Famílias
- [ ] Criação automática de família-zero
- [ ] Validação de família-zero única por usuário
- [ ] Interface para visualizar família-zero
- [ ] Proteção contra exclusão da família-zero

### 2. CRUD de Membros
- [ ] Adicionar novo membro
- [ ] Editar informações do membro
- [ ] Excluir membro (com validações)
- [ ] Visualizar lista de membros
- [ ] Buscar membros

### 3. Validações e Regras de Negócio
- [ ] Validação de datas de nascimento
- [ ] Prevenção de loops genealógicos
- [ ] Validação de relacionamentos
- [ ] Regras de exclusão de membros

### 4. Interface de Usuário
- [ ] Tela de criação de família-zero
- [ ] Tela de adicionar membro
- [ ] Tela de edição de membro
- [ ] Lista de membros com filtros
- [ ] Detalhes do membro

---

## 🏗️ ESTRUTURA DE MÓDULOS

### Novos Módulos a Criar

```
raizes-vivas/
├── :feature:family/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/raizesvivas/feature/family/
│       ├── presentation/screen/
│       │   ├── FamilyZeroSetupScreen.kt
│       │   ├── FamilyOverviewScreen.kt
│       │   └── FamilySettingsScreen.kt
│       ├── presentation/viewmodel/
│       │   ├── FamilyZeroViewModel.kt
│       │   ├── FamilyOverviewViewModel.kt
│       │   └── FamilyState.kt
│       └── data/repository/FamilyRepositoryImpl.kt
│
└── :feature:member/
    ├── build.gradle.kts
    └── src/main/kotlin/com/raizesvivas/feature/member/
        ├── presentation/screen/
        │   ├── AddMemberScreen.kt
        │   ├── EditMemberScreen.kt
        │   ├── MemberListScreen.kt
        │   ├── MemberDetailScreen.kt
        │   └── SearchMemberScreen.kt
        ├── presentation/viewmodel/
        │   ├── AddMemberViewModel.kt
        │   ├── EditMemberViewModel.kt
        │   ├── MemberListViewModel.kt
        │   └── MemberState.kt
        └── data/repository/MemberRepositoryImpl.kt
```

### Atualizações nos Módulos Existentes

```
:core:domain/
└── src/main/kotlin/com/raizesvivas/core/domain/
    ├── model/
    │   ├── Family.kt
    │   ├── Member.kt
    │   ├── TreeElement.kt
    │   └── KinshipType.kt
    ├── repository/
    │   ├── FamilyRepository.kt
    │   └── MemberRepository.kt
    └── usecase/
        ├── family/
        │   ├── CreateFamilyZeroUseCase.kt
        │   ├── GetFamilyZeroUseCase.kt
        │   └── UpdateFamilyUseCase.kt
        └── member/
            ├── AddMemberUseCase.kt
            ├── UpdateMemberUseCase.kt
            ├── DeleteMemberUseCase.kt
            ├── GetMemberUseCase.kt
            └── SearchMembersUseCase.kt

:core:data/
└── src/main/kotlin/com/raizesvivas/core/data/
    ├── entity/
    │   ├── FamilyEntity.kt
    │   └── MemberEntity.kt
    ├── dao/
    │   ├── FamilyDao.kt
    │   └── MemberDao.kt
    └── mapper/
        ├── FamilyMapper.kt
        └── MemberMapper.kt
```

---

## 📅 CRONOGRAMA DETALHADO

### **DIA 1-2: Schema do Banco e Models**

#### Dia 1: Schema Supabase
- [ ] Criar tabelas `familias` e `membros`
- [ ] Configurar RLS para famílias e membros
- [ ] Criar triggers de validação
- [ ] Configurar índices para performance

#### Dia 2: Models e Entities
- [ ] Criar Family e Member domain models
- [ ] Criar FamilyEntity e MemberEntity
- [ ] Implementar mappers
- [ ] Criar TreeElement enum

### **DIA 3-4: Repositories e Use Cases**

#### Dia 3: Family Repository
- [ ] Implementar FamilyRepository
- [ ] CreateFamilyZeroUseCase
- [ ] GetFamilyZeroUseCase
- [ ] UpdateFamilyUseCase

#### Dia 4: Member Repository
- [ ] Implementar MemberRepository
- [ ] AddMemberUseCase
- [ ] UpdateMemberUseCase
- [ ] DeleteMemberUseCase
- [ ] GetMemberUseCase
- [ ] SearchMembersUseCase

### **DIA 5-7: Interface de Família-Zero**

#### Dia 5: Tela de Setup
- [ ] FamilyZeroSetupScreen
- [ ] FamilyZeroViewModel
- [ ] Lógica de criação automática
- [ ] Validações de família-zero

#### Dia 6: Tela de Overview
- [ ] FamilyOverviewScreen
- [ ] FamilyOverviewViewModel
- [ ] Visualização da família-zero
- [ ] Estatísticas básicas

#### Dia 7: Configurações
- [ ] FamilySettingsScreen
- [ ] Edição de informações
- [ ] Proteção contra exclusão
- [ ] Validações de segurança

### **DIA 8-10: CRUD de Membros**

#### Dia 8: Adicionar Membro
- [ ] AddMemberScreen
- [ ] AddMemberViewModel
- [ ] Formulário completo
- [ ] Validações de dados

#### Dia 9: Editar e Visualizar
- [ ] EditMemberScreen
- [ ] MemberDetailScreen
- [ ] MemberListScreen
- [ ] SearchMemberScreen

#### Dia 10: Testes e Validações
- [ ] Testes unitários completos
- [ ] Testes de integração
- [ ] Validações de regras de negócio
- [ ] Documentação

---

## 🗄️ SCHEMA DO BANCO DE DADOS

### Tabela `familias`
```sql
CREATE TABLE IF NOT EXISTS familias (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(255) NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('zero', 'subfamilia')),
    familia_pai_id UUID REFERENCES familias(id) ON DELETE CASCADE,
    criada_por_casamento BOOLEAN DEFAULT FALSE,
    membro_origem_1_id UUID,
    membro_origem_2_id UUID,
    data_criacao TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    icone_arvore TEXT,
    nivel_hierarquico INTEGER DEFAULT 0,
    ativa BOOLEAN DEFAULT TRUE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT familia_pai_nao_pode_ser_ela_mesma CHECK (id != familia_pai_id),
    CONSTRAINT familia_zero_sem_pai CHECK (
        (tipo = 'zero' AND familia_pai_id IS NULL) OR tipo = 'subfamilia'
    )
);

-- Índices para performance
CREATE INDEX idx_familias_user_id ON familias(user_id);
CREATE INDEX idx_familias_tipo ON familias(tipo);
CREATE INDEX idx_familias_familia_pai_id ON familias(familia_pai_id);
CREATE INDEX idx_familias_ativa ON familias(ativa);
```

### Tabela `membros`
```sql
CREATE TABLE IF NOT EXISTS membros (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome_completo VARCHAR(255) NOT NULL,
    nome_abreviado VARCHAR(100),
    data_nascimento DATE,
    data_falecimento DATE,
    local_nascimento VARCHAR(255),
    local_falecimento VARCHAR(255),
    profissao VARCHAR(255),
    observacoes TEXT,
    foto_url TEXT,
    elementos_visuais JSONB DEFAULT '[]'::jsonb,
    nivel_na_arvore INTEGER DEFAULT 0,
    posicao_x REAL,
    posicao_y REAL,
    ativo BOOLEAN DEFAULT TRUE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT data_falecimento_apos_nascimento CHECK (
        data_falecimento IS NULL OR data_falecimento >= data_nascimento
    ),
    CONSTRAINT nivel_na_arvore_positivo CHECK (nivel_na_arvore >= 0)
);

-- Índices para performance
CREATE INDEX idx_membros_user_id ON membros(user_id);
CREATE INDEX idx_membros_nome_completo ON membros(nome_completo);
CREATE INDEX idx_membros_data_nascimento ON membros(data_nascimento);
CREATE INDEX idx_membros_ativo ON membros(ativo);
CREATE INDEX idx_membros_nivel_na_arvore ON membros(nivel_na_arvore);
```

### RLS (Row Level Security)
```sql
-- Políticas para familias
ALTER TABLE familias ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view their own families" ON familias
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own families" ON familias
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own families" ON familias
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete their own families" ON familias
    FOR DELETE USING (auth.uid() = user_id);

-- Políticas para membros
ALTER TABLE membros ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view their own members" ON membros
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own members" ON membros
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own members" ON membros
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete their own members" ON membros
    FOR DELETE USING (auth.uid() = user_id);
```

---

## 🎨 COMPONENTES DE UI

### FamilyZeroSetupScreen
```kotlin
@Composable
fun FamilyZeroSetupScreen(
    viewModel: FamilyZeroViewModel = hiltViewModel(),
    onNavigateToFamilyOverview: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    when (state) {
        is FamilyZeroState.Loading -> {
            LoadingScreen(message = "Criando sua família-zero...")
        }
        is FamilyZeroState.Success -> {
            LaunchedEffect(Unit) {
                onNavigateToFamilyOverview()
            }
        }
        is FamilyZeroState.Error -> {
            ErrorScreen(
                message = state.exception.message ?: "Erro ao criar família-zero",
                onRetry = { viewModel.createFamilyZero() }
            )
        }
        is FamilyZeroState.Initial -> {
            FamilyZeroSetupContent(
                onCreateFamily = { name ->
                    viewModel.createFamilyZero(name)
                }
            )
        }
    }
}
```

### AddMemberScreen
```kotlin
@Composable
fun AddMemberScreen(
    viewModel: AddMemberViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Adicionar Novo Membro",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        AddMemberForm(
            state = state,
            onNameChange = viewModel::updateName,
            onBirthDateChange = viewModel::updateBirthDate,
            onLocationChange = viewModel::updateLocation,
            onProfessionChange = viewModel::updateProfession,
            onObservationsChange = viewModel::updateObservations,
            onSubmit = viewModel::addMember
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = { viewModel.addMember() },
            enabled = state.isFormValid && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Adicionar Membro")
        }
    }
}
```

---

## 🧪 TESTES OBRIGATÓRIOS

### Testes de Use Cases
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class CreateFamilyZeroUseCaseTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var useCase: CreateFamilyZeroUseCase
    private lateinit var familyRepository: FakeFamilyRepository
    
    @Before
    fun setup() {
        familyRepository = FakeFamilyRepository()
        useCase = CreateFamilyZeroUseCase(familyRepository)
    }
    
    @Test
    fun `create family zero - success`() = runTest {
        // Arrange
        val familyName = "Família Silva"
        
        // Act
        val result = useCase(familyName)
        
        // Assert
        assertTrue(result is Result.Success)
        val family = (result as Result.Success).data
        assertEquals(familyName, family.nome)
        assertEquals("zero", family.tipo)
        assertNull(family.familiaPaiId)
    }
    
    @Test
    fun `create family zero when already exists - error`() = runTest {
        // Arrange
        familyRepository.createFamilyZero("Família Existente")
        val familyName = "Nova Família"
        
        // Act
        val result = useCase(familyName)
        
        // Assert
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is FamilyZeroAlreadyExistsException)
    }
}
```

### Testes de ViewModels
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class AddMemberViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var viewModel: AddMemberViewModel
    private lateinit var addMemberUseCase: FakeAddMemberUseCase
    
    @Before
    fun setup() {
        addMemberUseCase = FakeAddMemberUseCase()
        viewModel = AddMemberViewModel(addMemberUseCase)
    }
    
    @Test
    fun `add member with valid data - success`() = runTest {
        // Arrange
        viewModel.updateName("João Silva")
        viewModel.updateBirthDate(LocalDate.of(1990, 1, 1))
        
        // Act
        viewModel.addMember()
        
        // Assert
        assertTrue(viewModel.state.value is AddMemberState.Success)
    }
    
    @Test
    fun `add member with empty name - error`() = runTest {
        // Arrange
        viewModel.updateName("")
        
        // Act
        viewModel.addMember()
        
        // Assert
        assertTrue(viewModel.state.value is AddMemberState.Error)
        assertTrue(viewModel.state.value.nameError != null)
    }
}
```

---

## ✅ CHECKLIST DE VALIDAÇÃO DA FASE 2

### Sistema de Famílias
- [ ] Família-zero criada automaticamente no primeiro acesso
- [ ] Apenas uma família-zero por usuário
- [ ] Família inventada criada automaticamente
- [ ] Proteção contra exclusão da família-zero
- [ ] Interface para visualizar família-zero

### CRUD de Membros
- [ ] Adicionar membro funcionando
- [ ] Editar membro funcionando
- [ ] Excluir membro funcionando
- [ ] Listar membros funcionando
- [ ] Buscar membros funcionando
- [ ] Validações de dados implementadas

### Validações e Regras
- [ ] Datas de nascimento validadas
- [ ] Prevenção de loops genealógicos
- [ ] Validação de relacionamentos
- [ ] Regras de exclusão implementadas
- [ ] Mensagens de erro claras

### Interface e UX
- [ ] Telas responsivas e funcionais
- [ ] Loading states implementados
- [ ] Feedback visual adequado
- [ ] Navegação fluida
- [ ] Acessibilidade básica

### Qualidade
- [ ] Testes unitários passando (>80% cobertura)
- [ ] Testes de integração passando
- [ ] Performance adequada
- [ ] Documentação atualizada
- [ ] Código limpo e organizado

---

## 🚀 PRÓXIMOS PASSOS

Após completar a Fase 2, estaremos prontos para:

**Fase 3: Parentesco e Relacionamentos**
- Algoritmo de parentesco
- Sistema de relacionamentos
- Cálculo de graus de parentesco
- Validações de genealogia

---

## ⚠️ RISCOS E MITIGAÇÕES

### Riscos Identificados
1. **Complexidade do Schema**: Muitas tabelas e relacionamentos
   - *Mitigação*: Implementar passo a passo com testes

2. **Validações de Negócio**: Regras complexas de genealogia
   - *Mitigação*: Criar testes abrangentes para cada regra

3. **Performance**: Queries complexas no banco
   - *Mitigação*: Usar índices e otimizar queries

### Critérios de Sucesso
- ✅ Família-zero criada automaticamente
- ✅ CRUD de membros funcionando
- ✅ Validações implementadas
- ✅ Interface intuitiva
- ✅ Testes passando
- ✅ Performance adequada

---

**Esta fase implementa o coração do sistema de famílias e membros. Cada validação e regra de negócio deve ser testada exaustivamente antes de prosseguir para a Fase 3.**
