# FASE 4: ÁRVORE VISUAL E SUBFAMÍLIAS - RAÍZES VIVAS

## 🎯 OBJETIVO DA FASE

Implementar o sistema completo de visualização de árvores genealógicas, renderização de elementos visuais, criação de subfamílias e navegação entre diferentes visualizações.

**Duração**: 3 semanas (15 dias úteis)  
**Entregável**: Sistema completo de visualização de árvores e subfamílias funcionando

---

## 📋 ENTREGÁVEIS DETALHADOS

### 1. Sistema de Renderização de Árvore
- [ ] Renderização SVG da árvore genealógica
- [ ] Sistema de elementos visuais (raiz, tronco, galhos, folhas, flores)
- [ ] Posicionamento automático de membros
- [ ] Zoom e pan na árvore
- [ ] Responsividade para diferentes tamanhos de tela

### 2. Sistema de Subfamílias
- [ ] Criação de subfamílias
- [ ] Navegação entre subfamílias
- [ ] Visualização hierárquica
- [ ] Gerenciamento de subfamílias
- [ ] Validações de subfamílias

### 3. Elementos Visuais e Temas
- [ ] Sistema de elementos visuais por membro
- [ ] Temas visuais (clássico, moderno, colorido)
- [ ] Personalização de elementos
- [ ] Animações e transições
- [ ] Acessibilidade visual

### 4. Interface de Visualização
- [ ] Tela principal da árvore
- [ ] Controles de navegação
- [ ] Painel de informações
- [ ] Menu de opções
- [ ] Modo de edição visual

---

## 🏗️ ESTRUTURA DE MÓDULOS

### Novos Módulos a Criar

```
raizes-vivas/
├── :feature:tree/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/raizesvivas/feature/tree/
│       ├── presentation/screen/
│       │   ├── TreeViewScreen.kt
│       │   ├── TreeEditScreen.kt
│       │   ├── TreeSettingsScreen.kt
│       │   └── TreeNavigationScreen.kt
│       ├── presentation/viewmodel/
│       │   ├── TreeViewViewModel.kt
│       │   ├── TreeEditViewModel.kt
│       │   └── TreeState.kt
│       └── data/repository/TreeRepositoryImpl.kt
│
├── :feature:subfamily/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/raizesvivas/feature/subfamily/
│       ├── presentation/screen/
│       │   ├── CreateSubfamilyScreen.kt
│       │   ├── SubfamilyListScreen.kt
│       │   ├── SubfamilyDetailScreen.kt
│       │   └── SubfamilyNavigationScreen.kt
│       ├── presentation/viewmodel/
│       │   ├── CreateSubfamilyViewModel.kt
│       │   ├── SubfamilyListViewModel.kt
│       │   └── SubfamilyState.kt
│       └── data/repository/SubfamilyRepositoryImpl.kt
│
└── :feature:visual/
    ├── build.gradle.kts
    └── src/main/kotlin/com/raizesvivas/feature/visual/
        ├── presentation/screen/
        │   ├── VisualElementsScreen.kt
        │   ├── ThemeSelectionScreen.kt
        │   └── CustomizationScreen.kt
        ├── presentation/viewmodel/
        │   ├── VisualElementsViewModel.kt
        │   ├── ThemeSelectionViewModel.kt
        │   └── VisualState.kt
        └── data/repository/VisualRepositoryImpl.kt
```

### Atualizações nos Módulos Existentes

```
:core:ui/
└── src/main/kotlin/com/raizesvivas/core/ui/
    ├── components/
    │   ├── TreeElementIcon.kt
    │   ├── MemberCard.kt
    │   ├── LoadingScreen.kt
    │   └── ErrorScreen.kt
    ├── theme/
    │   ├── TreeTheme.kt
    │   ├── VisualElements.kt
    │   └── Animations.kt
    └── utils/
        ├── TreeLayoutCalculator.kt
        └── TreePositioning.kt

:core:domain/
└── src/main/kotlin/com/raizesvivas/core/domain/
    ├── model/
    │   ├── TreeElement.kt
    │   ├── TreeTheme.kt
    │   ├── Subfamily.kt
    │   └── VisualElement.kt
    ├── repository/
    │   ├── TreeRepository.kt
    │   ├── SubfamilyRepository.kt
    │   └── VisualRepository.kt
    └── usecase/
        ├── tree/
        │   ├── RenderTreeUseCase.kt
        │   ├── CalculateTreeLayoutUseCase.kt
        │   └── UpdateTreePositionUseCase.kt
        ├── subfamily/
        │   ├── CreateSubfamilyUseCase.kt
        │   ├── GetSubfamiliesUseCase.kt
        │   └── NavigateSubfamilyUseCase.kt
        └── visual/
            ├── ApplyThemeUseCase.kt
            ├── CustomizeElementUseCase.kt
            └── SaveVisualPreferencesUseCase.kt
```

---

## 📅 CRONOGRAMA DETALHADO

### **DIA 1-3: Sistema de Renderização de Árvore**

#### Dia 1: Estrutura Base da Árvore
- [ ] Criar TreeViewScreen
- [ ] Implementar renderização SVG básica
- [ ] Sistema de posicionamento de membros
- [ ] Testes básicos de renderização

#### Dia 2: Elementos Visuais
- [ ] Implementar TreeElementIcon
- [ ] Sistema de elementos visuais
- [ ] Renderização de conexões
- [ ] Animações básicas

#### Dia 3: Navegação e Controles
- [ ] Implementar zoom e pan
- [ ] Controles de navegação
- [ ] Responsividade
- [ ] Testes de navegação

### **DIA 4-6: Sistema de Subfamílias**

#### Dia 4: Schema e Models
- [ ] Atualizar schema para subfamílias
- [ ] Implementar Subfamily model
- [ ] Criar relacionamentos de subfamílias
- [ ] Configurar RLS

#### Dia 5: CRUD de Subfamílias
- [ ] Implementar SubfamilyRepository
- [ ] CreateSubfamilyUseCase
- [ ] GetSubfamiliesUseCase
- [ ] NavigateSubfamilyUseCase

#### Dia 6: Interface de Subfamílias
- [ ] CreateSubfamilyScreen
- [ ] SubfamilyListScreen
- [ ] SubfamilyNavigationScreen
- [ ] Validações de subfamílias

### **DIA 7-10: Elementos Visuais e Temas**

#### Dia 7: Sistema de Temas
- [ ] Implementar TreeTheme
- [ ] Temas pré-definidos
- [ ] Aplicação de temas
- [ ] Persistência de preferências

#### Dia 8: Personalização de Elementos
- [ ] VisualElementsScreen
- [ ] CustomizationScreen
- [ ] Personalização por membro
- [ ] Salvamento de preferências

#### Dia 9: Animações e Transições
- [ ] Implementar animações
- [ ] Transições suaves
- [ ] Feedback visual
- [ ] Performance de animações

#### Dia 10: Testes e Validações
- [ ] Testes unitários
- [ ] Testes de integração
- [ ] Testes de performance
- [ ] Validações visuais

### **DIA 11-15: Polimento e Otimização**

#### Dia 11-12: Otimização de Performance
- [ ] Otimizar renderização SVG
- [ ] Implementar cache de árvore
- [ ] Otimizar animações
- [ ] Monitoramento de performance

#### Dia 13-14: Acessibilidade e UX
- [ ] Implementar acessibilidade
- [ ] Melhorar UX
- [ ] Feedback visual
- [ ] Instruções de uso

#### Dia 15: Documentação e Testes
- [ ] Documentação completa
- [ ] Testes de aceitação
- [ ] Validação final
- [ ] Preparação para Fase 5

---

## 🗄️ SCHEMA DO BANCO DE DADOS

### Atualizações na Tabela `familias`
```sql
-- Adicionar colunas para subfamílias
ALTER TABLE familias ADD COLUMN IF NOT EXISTS familia_pai_id UUID REFERENCES familias(id) ON DELETE CASCADE;
ALTER TABLE familias ADD COLUMN IF NOT EXISTS criada_por_casamento BOOLEAN DEFAULT FALSE;
ALTER TABLE familias ADD COLUMN IF NOT EXISTS membro_origem_1_id UUID;
ALTER TABLE familias ADD COLUMN IF NOT EXISTS membro_origem_2_id UUID;
ALTER TABLE familias ADD COLUMN IF NOT EXISTS icone_arvore TEXT;
ALTER TABLE familias ADD COLUMN IF NOT EXISTS nivel_hierarquico INTEGER DEFAULT 0;

-- Atualizar constraint para tipo
ALTER TABLE familias DROP CONSTRAINT IF EXISTS familias_tipo_check;
ALTER TABLE familias ADD CONSTRAINT familias_tipo_check CHECK (tipo IN ('zero', 'subfamilia'));

-- Adicionar constraint para família-zero
ALTER TABLE familias ADD CONSTRAINT familia_zero_sem_pai CHECK (
    (tipo = 'zero' AND familia_pai_id IS NULL) OR tipo = 'subfamilia'
);
```

### Tabela `sugestoes_subfamilias`
```sql
CREATE TABLE IF NOT EXISTS sugestoes_subfamilias (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    membro_1_id UUID NOT NULL REFERENCES membros(id) ON DELETE CASCADE,
    membro_2_id UUID NOT NULL REFERENCES membros(id) ON DELETE CASCADE,
    tipo_relacionamento VARCHAR(50) NOT NULL,
    confianca REAL DEFAULT 0.5,
    motivo TEXT,
    aceita BOOLEAN DEFAULT FALSE,
    ativa BOOLEAN DEFAULT TRUE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT membros_diferentes CHECK (membro_1_id != membro_2_id),
    CONSTRAINT confianca_valida CHECK (confianca >= 0.0 AND confianca <= 1.0)
);

-- Índices para performance
CREATE INDEX idx_sugestoes_membro_1_id ON sugestoes_subfamilias(membro_1_id);
CREATE INDEX idx_sugestoes_membro_2_id ON sugestoes_subfamilias(membro_2_id);
CREATE INDEX idx_sugestoes_ativa ON sugestoes_subfamilias(ativa);
CREATE INDEX idx_sugestoes_user_id ON sugestoes_subfamilias(user_id);
```

---

## 🎨 COMPONENTES DE UI

### TreeViewScreen
```kotlin
@Composable
fun TreeViewScreen(
    viewModel: TreeViewViewModel = hiltViewModel(),
    onNavigateToMember: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    when (state) {
        is TreeState.Loading -> {
            LoadingScreen(message = "Carregando árvore genealógica...")
        }
        is TreeState.Error -> {
            ErrorScreen(
                message = state.exception.message ?: "Erro ao carregar árvore",
                onRetry = { viewModel.loadTree() }
            )
        }
        is TreeState.Success -> {
            TreeViewContent(
                tree = state.tree,
                theme = state.theme,
                onMemberClick = onNavigateToMember,
                onZoomIn = { viewModel.zoomIn() },
                onZoomOut = { viewModel.zoomOut() },
                onResetZoom = { viewModel.resetZoom() }
            )
        }
    }
}

@Composable
fun TreeViewContent(
    tree: Tree,
    theme: TreeTheme,
    onMemberClick: (String) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Árvore SVG
        TreeSvgRenderer(
            tree = tree,
            theme = theme,
            onMemberClick = onMemberClick,
            modifier = Modifier.fillMaxSize()
        )
        
        // Controles de navegação
        TreeNavigationControls(
            onZoomIn = onZoomIn,
            onZoomOut = onZoomOut,
            onResetZoom = onResetZoom,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )
        
        // Painel de informações
        TreeInfoPanel(
            tree = tree,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
    }
}
```

### CreateSubfamilyScreen
```kotlin
@Composable
fun CreateSubfamilyScreen(
    viewModel: CreateSubfamilyViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Criar Subfamília",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        CreateSubfamilyForm(
            state = state,
            onNameChange = viewModel::updateName,
            onDescriptionChange = viewModel::updateDescription,
            onMember1Change = viewModel::updateMember1,
            onMember2Change = viewModel::updateMember2,
            onSubmit = viewModel::createSubfamily
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = { viewModel.createSubfamily() },
            enabled = state.isFormValid && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Criar Subfamília")
        }
    }
}
```

### TreeElementIcon
```kotlin
@Composable
fun TreeElementIcon(
    element: TreeElement,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val iconResource = when (element) {
        TreeElement.ROOT -> R.drawable.ic_root
        TreeElement.TRUNK -> R.drawable.ic_trunk
        TreeElement.BRANCH -> R.drawable.ic_branch
        TreeElement.LEAF -> R.drawable.ic_leaf
        TreeElement.FLOWER -> R.drawable.ic_flower
        TreeElement.POLLINATOR -> R.drawable.ic_pollinator
        TreeElement.BIRD -> R.drawable.ic_bird
    }
    
    val color = when (element) {
        TreeElement.ROOT -> FamilyZeroGold
        TreeElement.TRUNK -> TrunkBeige
        TreeElement.BRANCH -> BranchGreen
        TreeElement.LEAF -> LeafGreen
        TreeElement.FLOWER -> FlowerPink
        TreeElement.POLLINATOR -> PollinatorOrange
        TreeElement.BIRD -> BirdBlue
    }
    
    Icon(
        painter = painterResource(iconResource),
        contentDescription = element.description,
        tint = color,
        modifier = modifier.size(size)
    )
}
```

---

## 🧪 TESTES OBRIGATÓRIOS

### Testes de Renderização de Árvore
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class TreeViewViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var viewModel: TreeViewViewModel
    private lateinit var renderTreeUseCase: FakeRenderTreeUseCase
    
    @Before
    fun setup() {
        renderTreeUseCase = FakeRenderTreeUseCase()
        viewModel = TreeViewViewModel(renderTreeUseCase)
    }
    
    @Test
    fun `load tree - success`() = runTest {
        // Arrange
        val expectedTree = createTestTree()
        renderTreeUseCase.setTree(expectedTree)
        
        // Act
        viewModel.loadTree()
        
        // Assert
        assertTrue(viewModel.state.value is TreeState.Success)
        val state = viewModel.state.value as TreeState.Success
        assertEquals(expectedTree, state.tree)
    }
    
    @Test
    fun `zoom in - success`() = runTest {
        // Arrange
        viewModel.loadTree()
        
        // Act
        viewModel.zoomIn()
        
        // Assert
        assertTrue(viewModel.state.value is TreeState.Success)
        val state = viewModel.state.value as TreeState.Success
        assertTrue(state.zoomLevel > 1.0f)
    }
    
    @Test
    fun `zoom out - success`() = runTest {
        // Arrange
        viewModel.loadTree()
        viewModel.zoomIn()
        
        // Act
        viewModel.zoomOut()
        
        // Assert
        assertTrue(viewModel.state.value is TreeState.Success)
        val state = viewModel.state.value as TreeState.Success
        assertEquals(1.0f, state.zoomLevel)
    }
}
```

### Testes de Subfamílias
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class CreateSubfamilyUseCaseTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var useCase: CreateSubfamilyUseCase
    private lateinit var subfamilyRepository: FakeSubfamilyRepository
    
    @Before
    fun setup() {
        subfamilyRepository = FakeSubfamilyRepository()
        useCase = CreateSubfamilyUseCase(subfamilyRepository)
    }
    
    @Test
    fun `create subfamily - success`() = runTest {
        // Arrange
        val member1 = createTestMember("João", "1970-01-01")
        val member2 = createTestMember("Maria", "1972-01-01")
        val familyZeroId = "family-zero-id"
        
        // Act
        val result = useCase(member1, member2, familyZeroId)
        
        // Assert
        assertTrue(result is Result.Success)
        val subfamily = (result as Result.Success).data
        assertEquals("Subfamília de João e Maria", subfamily.nome)
        assertEquals("subfamilia", subfamily.tipo)
        assertEquals(familyZeroId, subfamily.familiaPaiId)
    }
    
    @Test
    fun `create subfamily with invalid members - error`() = runTest {
        // Arrange
        val member1 = createTestMember("João", "1970-01-01")
        val member2 = createTestMember("João", "1970-01-01") // Mesmo membro
        val familyZeroId = "family-zero-id"
        
        // Act
        val result = useCase(member1, member2, familyZeroId)
        
        // Assert
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is InvalidSubfamilyMembersException)
    }
}
```

---

## ✅ CHECKLIST DE VALIDAÇÃO DA FASE 4

### Sistema de Renderização de Árvore
- [ ] Renderização SVG funcionando
- [ ] Sistema de elementos visuais implementado
- [ ] Posicionamento automático de membros
- [ ] Zoom e pan funcionando
- [ ] Responsividade para diferentes telas
- [ ] Performance adequada

### Sistema de Subfamílias
- [ ] Criação de subfamílias funcionando
- [ ] Navegação entre subfamílias
- [ ] Visualização hierárquica
- [ ] Gerenciamento de subfamílias
- [ ] Validações de subfamílias

### Elementos Visuais e Temas
- [ ] Sistema de elementos visuais por membro
- [ ] Temas visuais funcionando
- [ ] Personalização de elementos
- [ ] Animações e transições
- [ ] Acessibilidade visual

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
- [ ] Documentação completa
- [ ] Código limpo e organizado

---

## 🚀 PRÓXIMOS PASSOS

Após completar a Fase 4, estaremos prontos para:

**Fase 5: Floresta e Gamificação**
- Visualização de múltiplas árvores
- Sistema de conquistas
- Gamificação e engajamento
- Relatórios e estatísticas

---

## ⚠️ RISCOS E MITIGAÇÕES

### Riscos Identificados
1. **Performance de Renderização**: SVG pode ser lento
   - *Mitigação*: Otimizar renderização e usar cache

2. **Complexidade de Navegação**: Muitas subfamílias podem confundir
   - *Mitigação*: Interface intuitiva e navegação clara

3. **Responsividade**: Diferentes tamanhos de tela
   - *Mitigação*: Testar em vários dispositivos

### Critérios de Sucesso
- ✅ Renderização de árvore funcionando
- ✅ Sistema de subfamílias funcionando
- ✅ Elementos visuais implementados
- ✅ Performance adequada
- ✅ Interface intuitiva
- ✅ Testes passando

---

**Esta fase implementa a visualização completa das árvores genealógicas. A renderização deve ser otimizada e testada em diferentes dispositivos antes de prosseguir para a Fase 5.**
