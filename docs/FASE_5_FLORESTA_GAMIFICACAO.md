# FASE 5: FLORESTA E GAMIFICAÇÃO - RAÍZES VIVAS

## 🎯 OBJETIVO DA FASE

Implementar o sistema completo de visualização de múltiplas árvores (floresta), sistema de conquistas, gamificação e relatórios avançados para engajar os usuários.

**Duração**: 3 semanas (15 dias úteis)  
**Entregável**: Sistema completo de floresta e gamificação funcionando

---

## 📋 ENTREGÁVEIS DETALHADOS

### 1. Sistema de Floresta
- [ ] Visualização de múltiplas árvores
- [ ] Navegação entre árvores
- [ ] Comparação de árvores
- [ ] Estatísticas da floresta
- [ ] Exportação de dados

### 2. Sistema de Conquistas
- [ ] Conquistas por completude
- [ ] Conquistas por descoberta
- [ ] Conquistas por conectividade
- [ ] Sistema de badges
- [ ] Progresso de conquistas

### 3. Gamificação e Engajamento
- [ ] Sistema de pontuação
- [ ] Rankings e competições
- [ ] Desafios mensais
- [ ] Compartilhamento social
- [ ] Notificações de progresso

### 4. Relatórios e Estatísticas
- [ ] Relatórios de genealogia
- [ ] Estatísticas de família
- [ ] Análise de padrões
- [ ] Exportação de relatórios
- [ ] Dashboard de progresso

---

## 🏗️ ESTRUTURA DE MÓDULOS

### Novos Módulos a Criar

```
raizes-vivas/
├── :feature:forest/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/raizesvivas/feature/forest/
│       ├── presentation/screen/
│       │   ├── ForestViewScreen.kt
│       │   ├── ForestNavigationScreen.kt
│       │   ├── ForestComparisonScreen.kt
│       │   └── ForestStatsScreen.kt
│       ├── presentation/viewmodel/
│       │   ├── ForestViewViewModel.kt
│       │   ├── ForestNavigationViewModel.kt
│       │   └── ForestState.kt
│       └── data/repository/ForestRepositoryImpl.kt
│
├── :feature:achievements/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/raizesvivas/feature/achievements/
│       ├── presentation/screen/
│       │   ├── AchievementsScreen.kt
│       │   ├── AchievementDetailScreen.kt
│       │   ├── BadgesScreen.kt
│       │   └── ProgressScreen.kt
│       ├── presentation/viewmodel/
│       │   ├── AchievementsViewModel.kt
│       │   ├── AchievementDetailViewModel.kt
│       │   └── AchievementState.kt
│       └── data/repository/AchievementRepositoryImpl.kt
│
├── :feature:gamification/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/raizesvivas/feature/gamification/
│       ├── presentation/screen/
│       │   ├── LeaderboardScreen.kt
│       │   ├── ChallengesScreen.kt
│       │   ├── PointsScreen.kt
│       │   └── SocialScreen.kt
│       ├── presentation/viewmodel/
│       │   ├── LeaderboardViewModel.kt
│       │   ├── ChallengesViewModel.kt
│       │   └── GamificationState.kt
│       └── data/repository/GamificationRepositoryImpl.kt
│
└── :feature:reports/
    ├── build.gradle.kts
    └── src/main/kotlin/com/raizesvivas/feature/reports/
        ├── presentation/screen/
        │   ├── ReportsScreen.kt
        │   ├── GenealogyReportScreen.kt
        │   ├── FamilyStatsScreen.kt
        │   └── ExportScreen.kt
        ├── presentation/viewmodel/
        │   ├── ReportsViewModel.kt
        │   ├── GenealogyReportViewModel.kt
        │   └── ReportState.kt
        └── data/repository/ReportRepositoryImpl.kt
```

### Atualizações nos Módulos Existentes

```
:core:domain/
└── src/main/kotlin/com/raizesvivas/core/domain/
    ├── model/
    │   ├── Forest.kt
    │   ├── Achievement.kt
    │   ├── Badge.kt
    │   ├── Challenge.kt
    │   ├── Leaderboard.kt
    │   └── Report.kt
    ├── repository/
    │   ├── ForestRepository.kt
    │   ├── AchievementRepository.kt
    │   ├── GamificationRepository.kt
    │   └── ReportRepository.kt
    └── usecase/
        ├── forest/
        │   ├── GetForestUseCase.kt
        │   ├── CompareTreesUseCase.kt
        │   └── GetForestStatsUseCase.kt
        ├── achievements/
        │   ├── GetAchievementsUseCase.kt
        │   ├── UnlockAchievementUseCase.kt
        │   └── GetProgressUseCase.kt
        ├── gamification/
        │   ├── UpdateScoreUseCase.kt
            ├── GetLeaderboardUseCase.kt
            └── CompleteChallengeUseCase.kt
        └── reports/
            ├── GenerateReportUseCase.kt
            ├── ExportReportUseCase.kt
            └── GetFamilyStatsUseCase.kt
```

---

## 📅 CRONOGRAMA DETALHADO

### **DIA 1-3: Sistema de Floresta**

#### Dia 1: Estrutura Base da Floresta
- [ ] Criar ForestViewScreen
- [ ] Implementar visualização de múltiplas árvores
- [ ] Sistema de navegação entre árvores
- [ ] Testes básicos de floresta

#### Dia 2: Comparação e Estatísticas
- [ ] Implementar ForestComparisonScreen
- [ ] Sistema de comparação de árvores
- [ ] Estatísticas da floresta
- [ ] Análise de padrões

#### Dia 3: Navegação e Exportação
- [ ] Implementar ForestNavigationScreen
- [ ] Sistema de navegação avançada
- [ ] Exportação de dados
- [ ] Testes de navegação

### **DIA 4-6: Sistema de Conquistas**

#### Dia 4: Schema e Models
- [ ] Criar tabela `conquistas`
- [ ] Implementar Achievement model
- [ ] Sistema de badges
- [ ] Configurar RLS

#### Dia 5: Lógica de Conquistas
- [ ] Implementar AchievementRepository
- [ ] GetAchievementsUseCase
- [ ] UnlockAchievementUseCase
- [ ] GetProgressUseCase

#### Dia 6: Interface de Conquistas
- [ ] AchievementsScreen
- [ ] AchievementDetailScreen
- [ ] BadgesScreen
- [ ] ProgressScreen

### **DIA 7-10: Gamificação e Engajamento**

#### Dia 7: Sistema de Pontuação
- [ ] Implementar sistema de pontos
- [ ] UpdateScoreUseCase
- [ ] Cálculo de pontuação
- [ ] Persistência de pontos

#### Dia 8: Rankings e Competições
- [ ] LeaderboardScreen
- [ ] GetLeaderboardUseCase
- [ ] Sistema de rankings
- [ ] Competições mensais

#### Dia 9: Desafios e Social
- [ ] ChallengesScreen
- [ ] CompleteChallengeUseCase
- [ ] Sistema de desafios
- [ ] Compartilhamento social

#### Dia 10: Testes e Validações
- [ ] Testes unitários
- [ ] Testes de integração
- [ ] Testes de gamificação
- [ ] Validações de pontuação

### **DIA 11-15: Relatórios e Polimento**

#### Dia 11-12: Sistema de Relatórios
- [ ] ReportsScreen
- [ ] GenerateReportUseCase
- [ ] ExportReportUseCase
- [ ] Relatórios de genealogia

#### Dia 13-14: Estatísticas e Dashboard
- [ ] FamilyStatsScreen
- [ ] GetFamilyStatsUseCase
- [ ] Dashboard de progresso
- [ ] Análise de dados

#### Dia 15: Documentação e Testes
- [ ] Documentação completa
- [ ] Testes de aceitação
- [ ] Validação final
- [ ] Preparação para entrega

---

## 🗄️ SCHEMA DO BANCO DE DADOS

### Tabela `conquistas`
```sql
CREATE TABLE IF NOT EXISTS conquistas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(255) NOT NULL,
    descricao TEXT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    requisitos JSONB NOT NULL,
    pontos INTEGER DEFAULT 0,
    badge_url TEXT,
    ativa BOOLEAN DEFAULT TRUE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT tipo_conquista_valido CHECK (
        tipo IN ('completude', 'descoberta', 'conectividade', 'tempo', 'especial')
    ),
    CONSTRAINT pontos_positivos CHECK (pontos >= 0)
);

-- Índices para performance
CREATE INDEX idx_conquistas_tipo ON conquistas(tipo);
CREATE INDEX idx_conquistas_ativa ON conquistas(ativa);
CREATE INDEX idx_conquistas_user_id ON conquistas(user_id);
```

### Tabela `conquistas_desbloqueadas`
```sql
CREATE TABLE IF NOT EXISTS conquistas_desbloqueadas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conquista_id UUID NOT NULL REFERENCES conquistas(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    data_desbloqueio TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    pontos_ganhos INTEGER DEFAULT 0,
    
    UNIQUE(conquista_id, user_id)
);

-- Índices para performance
CREATE INDEX idx_conquistas_desbloqueadas_user_id ON conquistas_desbloqueadas(user_id);
CREATE INDEX idx_conquistas_desbloqueadas_data ON conquistas_desbloqueadas(data_desbloqueio);
```

### Tabela `pontuacoes`
```sql
CREATE TABLE IF NOT EXISTS pontuacoes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    pontos_totais INTEGER DEFAULT 0,
    pontos_mes_atual INTEGER DEFAULT 0,
    nivel INTEGER DEFAULT 1,
    experiencia INTEGER DEFAULT 0,
    data_atualizacao TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    UNIQUE(user_id)
);

-- Índices para performance
CREATE INDEX idx_pontuacoes_pontos_totais ON pontuacoes(pontos_totais DESC);
CREATE INDEX idx_pontuacoes_nivel ON pontuacoes(nivel DESC);
CREATE INDEX idx_pontuacoes_experiencia ON pontuacoes(experiencia DESC);
```

### Tabela `desafios`
```sql
CREATE TABLE IF NOT EXISTS desafios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(255) NOT NULL,
    descricao TEXT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    requisitos JSONB NOT NULL,
    recompensa_pontos INTEGER DEFAULT 0,
    data_inicio TIMESTAMP WITH TIME ZONE NOT NULL,
    data_fim TIMESTAMP WITH TIME ZONE NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT tipo_desafio_valido CHECK (
        tipo IN ('mensal', 'semanal', 'diario', 'especial')
    ),
    CONSTRAINT data_fim_apos_inicio CHECK (data_fim > data_inicio),
    CONSTRAINT pontos_positivos CHECK (recompensa_pontos >= 0)
);

-- Índices para performance
CREATE INDEX idx_desafios_tipo ON desafios(tipo);
CREATE INDEX idx_desafios_data_inicio ON desafios(data_inicio);
CREATE INDEX idx_desafios_data_fim ON desafios(data_fim);
CREATE INDEX idx_desafios_ativo ON desafios(ativo);
CREATE INDEX idx_desafios_user_id ON desafios(user_id);
```

---

## 🎨 COMPONENTES DE UI

### ForestViewScreen
```kotlin
@Composable
fun ForestViewScreen(
    viewModel: ForestViewViewModel = hiltViewModel(),
    onNavigateToTree: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    when (state) {
        is ForestState.Loading -> {
            LoadingScreen(message = "Carregando floresta...")
        }
        is ForestState.Error -> {
            ErrorScreen(
                message = state.exception.message ?: "Erro ao carregar floresta",
                onRetry = { viewModel.loadForest() }
            )
        }
        is ForestState.Success -> {
            ForestViewContent(
                forest = state.forest,
                onTreeClick = onNavigateToTree,
                onCompareTrees = { tree1, tree2 ->
                    viewModel.compareTrees(tree1, tree2)
                },
                onShowStats = { viewModel.showStats() }
            )
        }
    }
}

@Composable
fun ForestViewContent(
    forest: Forest,
    onTreeClick: (String) -> Unit,
    onCompareTrees: (Tree, Tree) -> Unit,
    onShowStats: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header com estatísticas
        ForestHeader(
            forest = forest,
            onShowStats = onShowStats,
            modifier = Modifier.padding(16.dp)
        )
        
        // Grid de árvores
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(forest.trees) { tree ->
                TreeCard(
                    tree = tree,
                    onClick = { onTreeClick(tree.id) },
                    onCompare = { onCompareTrees(tree, forest.trees.first()) }
                )
            }
        }
    }
}
```

### AchievementsScreen
```kotlin
@Composable
fun AchievementsScreen(
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    when (state) {
        is AchievementState.Loading -> {
            LoadingScreen(message = "Carregando conquistas...")
        }
        is AchievementState.Error -> {
            ErrorScreen(
                message = state.exception.message ?: "Erro ao carregar conquistas",
                onRetry = { viewModel.loadAchievements() }
            )
        }
        is AchievementState.Success -> {
            AchievementsContent(
                achievements = state.achievements,
                unlockedAchievements = state.unlockedAchievements,
                progress = state.progress
            )
        }
    }
}

@Composable
fun AchievementsContent(
    achievements: List<Achievement>,
    unlockedAchievements: List<Achievement>,
    progress: AchievementProgress
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progresso geral
        AchievementProgressCard(
            progress = progress,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Lista de conquistas
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(achievements) { achievement ->
                AchievementCard(
                    achievement = achievement,
                    isUnlocked = unlockedAchievements.contains(achievement),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
```

### LeaderboardScreen
```kotlin
@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    when (state) {
        is GamificationState.Loading -> {
            LoadingScreen(message = "Carregando ranking...")
        }
        is GamificationState.Error -> {
            ErrorScreen(
                message = state.exception.message ?: "Erro ao carregar ranking",
                onRetry = { viewModel.loadLeaderboard() }
            )
        }
        is GamificationState.Success -> {
            LeaderboardContent(
                leaderboard = state.leaderboard,
                userRank = state.userRank
            )
        }
    }
}

@Composable
fun LeaderboardContent(
    leaderboard: Leaderboard,
    userRank: UserRank
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Ranking do usuário
        UserRankCard(
            userRank = userRank,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Lista de ranking
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(leaderboard.entries) { entry ->
                LeaderboardEntryCard(
                    entry = entry,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
```

---

## 🧪 TESTES OBRIGATÓRIOS

### Testes de Floresta
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class ForestViewViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var viewModel: ForestViewViewModel
    private lateinit var getForestUseCase: FakeGetForestUseCase
    
    @Before
    fun setup() {
        getForestUseCase = FakeGetForestUseCase()
        viewModel = ForestViewViewModel(getForestUseCase)
    }
    
    @Test
    fun `load forest - success`() = runTest {
        // Arrange
        val expectedForest = createTestForest()
        getForestUseCase.setForest(expectedForest)
        
        // Act
        viewModel.loadForest()
        
        // Assert
        assertTrue(viewModel.state.value is ForestState.Success)
        val state = viewModel.state.value as ForestState.Success
        assertEquals(expectedForest, state.forest)
    }
    
    @Test
    fun `compare trees - success`() = runTest {
        // Arrange
        val tree1 = createTestTree("tree1")
        val tree2 = createTestTree("tree2")
        
        // Act
        viewModel.compareTrees(tree1, tree2)
        
        // Assert
        assertTrue(viewModel.state.value is ForestState.Success)
        val state = viewModel.state.value as ForestState.Success
        assertNotNull(state.comparison)
    }
}
```

### Testes de Conquistas
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class UnlockAchievementUseCaseTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var useCase: UnlockAchievementUseCase
    private lateinit var achievementRepository: FakeAchievementRepository
    
    @Before
    fun setup() {
        achievementRepository = FakeAchievementRepository()
        useCase = UnlockAchievementUseCase(achievementRepository)
    }
    
    @Test
    fun `unlock achievement - success`() = runTest {
        // Arrange
        val achievement = createTestAchievement("Primeira Família")
        val userId = "user-id"
        
        // Act
        val result = useCase(achievement, userId)
        
        // Assert
        assertTrue(result is Result.Success)
        val unlockedAchievement = (result as Result.Success).data
        assertEquals(achievement.id, unlockedAchievement.achievementId)
        assertEquals(userId, unlockedAchievement.userId)
    }
    
    @Test
    fun `unlock already unlocked achievement - error`() = runTest {
        // Arrange
        val achievement = createTestAchievement("Primeira Família")
        val userId = "user-id"
        achievementRepository.unlockAchievement(achievement, userId)
        
        // Act
        val result = useCase(achievement, userId)
        
        // Assert
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AchievementAlreadyUnlockedException)
    }
}
```

---

## ✅ CHECKLIST DE VALIDAÇÃO DA FASE 5

### Sistema de Floresta
- [ ] Visualização de múltiplas árvores funcionando
- [ ] Navegação entre árvores
- [ ] Comparação de árvores
- [ ] Estatísticas da floresta
- [ ] Exportação de dados

### Sistema de Conquistas
- [ ] Conquistas por completude funcionando
- [ ] Conquistas por descoberta funcionando
- [ ] Conquistas por conectividade funcionando
- [ ] Sistema de badges funcionando
- [ ] Progresso de conquistas

### Gamificação e Engajamento
- [ ] Sistema de pontuação funcionando
- [ ] Rankings e competições
- [ ] Desafios mensais
- [ ] Compartilhamento social
- [ ] Notificações de progresso

### Relatórios e Estatísticas
- [ ] Relatórios de genealogia funcionando
- [ ] Estatísticas de família
- [ ] Análise de padrões
- [ ] Exportação de relatórios
- [ ] Dashboard de progresso

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

Após completar a Fase 5, o projeto estará completo e pronto para:

**Entrega Final**
- Testes de aceitação completos
- Documentação final
- Deploy em produção
- Lançamento do aplicativo

---

## ⚠️ RISCOS E MITIGAÇÕES

### Riscos Identificados
1. **Performance da Floresta**: Muitas árvores podem ser lentas
   - *Mitigação*: Paginação e cache inteligente

2. **Complexidade de Gamificação**: Muitas funcionalidades podem confundir
   - *Mitigação*: Interface intuitiva e tutoriais

3. **Engajamento**: Usuários podem não se engajar
   - *Mitigação*: Sistema de recompensas atrativo

### Critérios de Sucesso
- ✅ Sistema de floresta funcionando
- ✅ Sistema de conquistas funcionando
- ✅ Gamificação implementada
- ✅ Relatórios funcionando
- ✅ Performance adequada
- ✅ Interface intuitiva
- ✅ Testes passando

---

**Esta fase completa o projeto Raízes Vivas com todas as funcionalidades avançadas. O sistema deve ser testado exaustivamente antes da entrega final.**
