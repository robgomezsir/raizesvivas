# FASE 3: PARENTESCO E RELACIONAMENTOS - RAÍZES VIVAS

## 🎯 OBJETIVO DA FASE

Implementar o sistema completo de parentesco e relacionamentos, incluindo o algoritmo de parentesco, sistema de relacionamentos familiares e validações de genealogia.

**Duração**: 3 semanas (15 dias úteis)  
**Entregável**: Sistema completo de parentesco e relacionamentos funcionando

---

## 📋 ENTREGÁVEIS DETALHADOS

### 1. Algoritmo de Parentesco
- [ ] Implementação completa do algoritmo
- [ ] Cálculo de graus de parentesco
- [ ] Determinação de tipos de parentesco
- [ ] Otimização para performance
- [ ] 100% de cobertura de testes

### 2. Sistema de Relacionamentos
- [ ] CRUD de relacionamentos familiares
- [ ] Validação de relacionamentos válidos
- [ ] Prevenção de loops genealógicos
- [ ] Cálculo automático de parentesco
- [ ] Cache de parentescos calculados

### 3. Validações de Genealogia
- [ ] Validação de datas de nascimento
- [ ] Prevenção de relacionamentos impossíveis
- [ ] Validação de gerações
- [ ] Alertas de inconsistências
- [ ] Correção automática de problemas

### 4. Interface de Relacionamentos
- [ ] Tela de adicionar relacionamento
- [ ] Visualização de parentesco
- [ ] Lista de relacionamentos
- [ ] Edição de relacionamentos
- [ ] Busca por parentesco

---

## 🏗️ ESTRUTURA DE MÓDULOS

### Novos Módulos a Criar

```
raizes-vivas/
├── :feature:relationship/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/raizesvivas/feature/relationship/
│       ├── presentation/screen/
│       │   ├── AddRelationshipScreen.kt
│       │   ├── EditRelationshipScreen.kt
│       │   ├── RelationshipListScreen.kt
│       │   └── KinshipDisplayScreen.kt
│       ├── presentation/viewmodel/
│       │   ├── AddRelationshipViewModel.kt
│       │   ├── EditRelationshipViewModel.kt
│       │   ├── RelationshipListViewModel.kt
│       │   └── RelationshipState.kt
│       └── data/repository/RelationshipRepositoryImpl.kt
│
└── :feature:kinship/
    ├── build.gradle.kts
    └── src/main/kotlin/com/raizesvivas/feature/kinship/
        ├── presentation/screen/
        │   ├── KinshipCalculatorScreen.kt
        │   ├── KinshipResultScreen.kt
        │   └── KinshipHistoryScreen.kt
        ├── presentation/viewmodel/
        │   ├── KinshipCalculatorViewModel.kt
        │   ├── KinshipResultViewModel.kt
        │   └── KinshipState.kt
        └── data/repository/KinshipRepositoryImpl.kt
```

### Atualizações nos Módulos Existentes

```
:core:utils/
└── src/main/kotlin/com/raizesvivas/core/utils/
    ├── algorithms/
    │   ├── KinshipCalculator.kt
    │   ├── KinshipValidator.kt
    │   └── GenealogyValidator.kt
    └── cache/
        └── KinshipCache.kt

:core:domain/
└── src/main/kotlin/com/raizesvivas/core/domain/
    ├── model/
    │   ├── Relationship.kt
    │   ├── Kinship.kt
    │   ├── KinshipType.kt
    │   └── KinshipDegree.kt
    ├── repository/
    │   ├── RelationshipRepository.kt
    │   └── KinshipRepository.kt
    └── usecase/
        ├── relationship/
        │   ├── AddRelationshipUseCase.kt
        │   ├── UpdateRelationshipUseCase.kt
        │   ├── DeleteRelationshipUseCase.kt
        │   ├── GetRelationshipsUseCase.kt
        │   └── ValidateRelationshipUseCase.kt
        └── kinship/
            ├── CalculateKinshipUseCase.kt
            ├── GetKinshipUseCase.kt
            ├── CacheKinshipUseCase.kt
            └── ValidateGenealogyUseCase.kt

:core:data/
└── src/main/kotlin/com/raizesvivas/core/data/
    ├── entity/
    │   ├── RelationshipEntity.kt
    │   └── KinshipEntity.kt
    ├── dao/
    │   ├── RelationshipDao.kt
    │   └── KinshipDao.kt
    └── mapper/
        ├── RelationshipMapper.kt
        └── KinshipMapper.kt
```

---

## 📅 CRONOGRAMA DETALHADO

### **DIA 1-3: Algoritmo de Parentesco**

#### Dia 1: Estrutura Base do Algoritmo
- [ ] Criar KinshipCalculator.kt
- [ ] Implementar busca de ancestrais comuns
- [ ] Implementar cálculo de distância geracional
- [ ] Testes unitários básicos

#### Dia 2: Tipos de Parentesco
- [ ] Implementar matriz de decisão
- [ ] Implementar todos os tipos de parentesco
- [ ] Validações de casos especiais
- [ ] Testes abrangentes

#### Dia 3: Otimização e Performance
- [ ] Implementar cache de parentesco
- [ ] Otimizar queries do banco
- [ ] Implementar cálculo em lote
- [ ] Testes de performance

### **DIA 4-6: Sistema de Relacionamentos**

#### Dia 4: Schema e Models
- [ ] Criar tabela `relacionamentos`
- [ ] Criar tabela `parentescos_calculados`
- [ ] Implementar Relationship e Kinship models
- [ ] Configurar RLS

#### Dia 5: Repository e Use Cases
- [ ] Implementar RelationshipRepository
- [ ] Implementar KinshipRepository
- [ ] Criar todos os Use Cases
- [ ] Testes de integração

#### Dia 6: Validações de Genealogia
- [ ] Implementar GenealogyValidator
- [ ] Prevenção de loops genealógicos
- [ ] Validação de datas
- [ ] Validação de relacionamentos

### **DIA 7-10: Interface de Relacionamentos**

#### Dia 7: Adicionar Relacionamento
- [ ] AddRelationshipScreen
- [ ] AddRelationshipViewModel
- [ ] Formulário de relacionamento
- [ ] Validações em tempo real

#### Dia 8: Editar e Visualizar
- [ ] EditRelationshipScreen
- [ ] RelationshipListScreen
- [ ] Visualização de parentesco
- [ ] Navegação entre telas

#### Dia 9: Calculadora de Parentesco
- [ ] KinshipCalculatorScreen
- [ ] KinshipResultScreen
- [ ] Histórico de cálculos
- [ ] Compartilhamento de resultados

#### Dia 10: Testes e Validações
- [ ] Testes unitários completos
- [ ] Testes de integração
- [ ] Testes de performance
- [ ] Documentação

### **DIA 11-15: Polimento e Otimização**

#### Dia 11-12: Cache e Performance
- [ ] Implementar cache inteligente
- [ ] Otimizar queries complexas
- [ ] Implementar paginação
- [ ] Monitoramento de performance

#### Dia 13-14: Validações Avançadas
- [ ] Validações de consistência
- [ ] Correção automática de problemas
- [ ] Alertas de inconsistências
- [ ] Relatórios de genealogia

#### Dia 15: Documentação e Testes
- [ ] Documentação completa
- [ ] Testes de aceitação
- [ ] Validação final
- [ ] Preparação para Fase 4

---

## 🗄️ SCHEMA DO BANCO DE DADOS

### Tabela `relacionamentos`
```sql
CREATE TABLE IF NOT EXISTS relacionamentos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    membro_1_id UUID NOT NULL REFERENCES membros(id) ON DELETE CASCADE,
    membro_2_id UUID NOT NULL REFERENCES membros(id) ON DELETE CASCADE,
    tipo_relacionamento VARCHAR(50) NOT NULL,
    data_inicio DATE,
    data_fim DATE,
    observacoes TEXT,
    ativo BOOLEAN DEFAULT TRUE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT membros_diferentes CHECK (membro_1_id != membro_2_id),
    CONSTRAINT data_fim_apos_inicio CHECK (
        data_fim IS NULL OR data_fim >= data_inicio
    ),
    CONSTRAINT tipo_relacionamento_valido CHECK (
        tipo_relacionamento IN (
            'pai', 'mae', 'filho', 'filha', 'irmao', 'irma',
            'avo', 'avó', 'neto', 'neta', 'tio', 'tia',
            'sobrinho', 'sobrinha', 'primo', 'prima',
            'esposo', 'esposa', 'cunhado', 'cunhada',
            'sogro', 'sogra', 'genro', 'nora'
        )
    )
);

-- Índices para performance
CREATE INDEX idx_relacionamentos_membro_1_id ON relacionamentos(membro_1_id);
CREATE INDEX idx_relacionamentos_membro_2_id ON relacionamentos(membro_2_id);
CREATE INDEX idx_relacionamentos_tipo ON relacionamentos(tipo_relacionamento);
CREATE INDEX idx_relacionamentos_ativo ON relacionamentos(ativo);
CREATE INDEX idx_relacionamentos_user_id ON relacionamentos(user_id);

-- Índice único para evitar relacionamentos duplicados
CREATE UNIQUE INDEX idx_relacionamentos_unicos ON relacionamentos(
    LEAST(membro_1_id, membro_2_id),
    GREATEST(membro_1_id, membro_2_id),
    tipo_relacionamento,
    user_id
) WHERE ativo = TRUE;
```

### Tabela `parentescos_calculados`
```sql
CREATE TABLE IF NOT EXISTS parentescos_calculados (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    membro_1_id UUID NOT NULL REFERENCES membros(id) ON DELETE CASCADE,
    membro_2_id UUID NOT NULL REFERENCES membros(id) ON DELETE CASCADE,
    tipo_parentesco VARCHAR(50) NOT NULL,
    grau_parentesco INTEGER NOT NULL,
    distancia_geracional INTEGER NOT NULL,
    familia_referencia_id UUID NOT NULL REFERENCES familias(id) ON DELETE CASCADE,
    data_calculo TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    ativo BOOLEAN DEFAULT TRUE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT membros_diferentes CHECK (membro_1_id != membro_2_id),
    CONSTRAINT grau_parentesco_positivo CHECK (grau_parentesco > 0),
    CONSTRAINT distancia_geracional_positiva CHECK (distancia_geracional >= 0),
    CONSTRAINT tipo_parentesco_valido CHECK (
        tipo_parentesco IN (
            'pai', 'mae', 'filho', 'filha', 'irmao', 'irma',
            'avo', 'avó', 'neto', 'neta', 'tio', 'tia',
            'sobrinho', 'sobrinha', 'primo', 'prima',
            'esposo', 'esposa', 'cunhado', 'cunhada',
            'sogro', 'sogra', 'genro', 'nora',
            'bisavo', 'bisavó', 'bisneto', 'bisneta',
            'tio_avo', 'tia_avó', 'sobrinho_neto', 'sobrinha_neta',
            'primo_segundo', 'prima_segunda'
        )
    )
);

-- Índices para performance
CREATE INDEX idx_parentescos_membro_1_id ON parentescos_calculados(membro_1_id);
CREATE INDEX idx_parentescos_membro_2_id ON parentescos_calculados(membro_2_id);
CREATE INDEX idx_parentescos_tipo ON parentescos_calculados(tipo_parentesco);
CREATE INDEX idx_parentescos_grau ON parentescos_calculados(grau_parentesco);
CREATE INDEX idx_parentescos_familia_ref ON parentescos_calculados(familia_referencia_id);
CREATE INDEX idx_parentescos_ativo ON parentescos_calculados(ativo);
CREATE INDEX idx_parentescos_user_id ON parentescos_calculados(user_id);

-- Índice único para evitar cálculos duplicados
CREATE UNIQUE INDEX idx_parentescos_unicos ON parentescos_calculados(
    LEAST(membro_1_id, membro_2_id),
    GREATEST(membro_1_id, membro_2_id),
    familia_referencia_id,
    user_id
) WHERE ativo = TRUE;
```

### Triggers de Validação
```sql
-- Função para prevenir loops genealógicos
CREATE OR REPLACE FUNCTION prevent_genealogical_loop()
RETURNS TRIGGER AS $$
BEGIN
    -- Verificar se o relacionamento criaria um loop
    IF EXISTS (
        WITH RECURSIVE genealogy_path AS (
            SELECT NEW.membro_1_id as ancestor_id, NEW.membro_2_id as descendant_id, 1 as depth
            UNION ALL
            SELECT r.membro_1_id, gp.descendant_id, gp.depth + 1
            FROM relacionamentos r
            JOIN genealogy_path gp ON r.membro_2_id = gp.ancestor_id
            WHERE gp.depth < 10 -- Limite de profundidade para evitar loops infinitos
        )
        SELECT 1 FROM genealogy_path 
        WHERE ancestor_id = NEW.membro_2_id AND descendant_id = NEW.membro_1_id
    ) THEN
        RAISE EXCEPTION 'Relacionamento criaria um loop genealógico';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para prevenir loops genealógicos
CREATE TRIGGER prevent_genealogical_loop_trigger
    BEFORE INSERT OR UPDATE ON relacionamentos
    FOR EACH ROW
    EXECUTE FUNCTION prevent_genealogical_loop();
```

---

## 🔧 ALGORITMO DE PARENTESCO

### KinshipCalculator.kt
```kotlin
package com.raizesvivas.core.utils.algorithms

import com.raizesvivas.core.domain.model.Member
import com.raizesvivas.core.domain.model.Kinship
import com.raizesvivas.core.domain.model.KinshipType
import com.raizesvivas.core.domain.model.KinshipDegree
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Calculadora de parentesco para o sistema Raízes Vivas
 * 
 * Este é o coração do sistema de genealogia. Calcula o parentesco
 * entre dois membros baseado na família-zero como referência.
 */
class KinshipCalculator {
    
    /**
     * Calcula o parentesco entre dois membros
     * 
     * @param member1 Primeiro membro
     * @param member2 Segundo membro
     * @param familyZeroId ID da família-zero como referência
     * @return Resultado do cálculo de parentesco
     */
    suspend fun calculate(
        member1: Member,
        member2: Member,
        familyZeroId: String
    ): Result<Kinship> = withContext(Dispatchers.Default) {
        try {
            // Validações básicas
            if (member1.id == member2.id) {
                return@withContext Result.success(
                    Kinship(
                        member1 = member1,
                        member2 = member2,
                        type = KinshipType.SELF,
                        degree = KinshipDegree.ZERO,
                        generationalDistance = 0,
                        description = "Mesma pessoa"
                    )
                )
            }
            
            // Buscar ancestrais comuns
            val commonAncestors = findCommonAncestors(member1, member2, familyZeroId)
            
            if (commonAncestors.isEmpty()) {
                return@withContext Result.success(
                    Kinship(
                        member1 = member1,
                        member2 = member2,
                        type = KinshipType.UNRELATED,
                        degree = KinshipDegree.UNKNOWN,
                        generationalDistance = -1,
                        description = "Sem parentesco conhecido"
                    )
                )
            }
            
            // Calcular distância geracional
            val generationalDistance = calculateGenerationalDistance(
                member1, member2, commonAncestors
            )
            
            // Determinar tipo de parentesco
            val kinshipType = determineKinshipType(
                member1, member2, generationalDistance
            )
            
            // Calcular grau de parentesco
            val kinshipDegree = calculateKinshipDegree(
                member1, member2, generationalDistance
            )
            
            // Gerar descrição
            val description = generateKinshipDescription(
                kinshipType, kinshipDegree, generationalDistance
            )
            
            Result.success(
                Kinship(
                    member1 = member1,
                    member2 = member2,
                    type = kinshipType,
                    degree = kinshipDegree,
                    generationalDistance = generationalDistance,
                    description = description
                )
            )
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Busca ancestrais comuns entre dois membros
     */
    private suspend fun findCommonAncestors(
        member1: Member,
        member2: Member,
        familyZeroId: String
    ): List<Member> {
        // Implementar busca de ancestrais comuns
        // Esta é uma implementação simplificada
        return emptyList()
    }
    
    /**
     * Calcula a distância geracional entre dois membros
     */
    private fun calculateGenerationalDistance(
        member1: Member,
        member2: Member,
        commonAncestors: List<Member>
    ): Int {
        // Implementar cálculo de distância geracional
        return 0
    }
    
    /**
     * Determina o tipo de parentesco baseado na distância geracional
     */
    private fun determineKinshipType(
        member1: Member,
        member2: Member,
        generationalDistance: Int
    ): KinshipType {
        return when (generationalDistance) {
            0 -> KinshipType.SIBLING
            1 -> KinshipType.PARENT_CHILD
            2 -> KinshipType.GRANDPARENT_GRANDCHILD
            3 -> KinshipType.GREAT_GRANDPARENT_GRANDCHILD
            else -> KinshipType.DISTANT_RELATIVE
        }
    }
    
    /**
     * Calcula o grau de parentesco
     */
    private fun calculateKinshipDegree(
        member1: Member,
        member2: Member,
        generationalDistance: Int
    ): KinshipDegree {
        return when (generationalDistance) {
            0 -> KinshipDegree.FIRST
            1 -> KinshipDegree.FIRST
            2 -> KinshipDegree.SECOND
            3 -> KinshipDegree.THIRD
            else -> KinshipDegree.DISTANT
        }
    }
    
    /**
     * Gera descrição do parentesco
     */
    private fun generateKinshipDescription(
        type: KinshipType,
        degree: KinshipDegree,
        generationalDistance: Int
    ): String {
        return when (type) {
            KinshipType.SIBLING -> "Irmãos"
            KinshipType.PARENT_CHILD -> "Pai/Filho"
            KinshipType.GRANDPARENT_GRANDCHILD -> "Avô/Neto"
            KinshipType.GREAT_GRANDPARENT_GRANDCHILD -> "Bisavô/Bisneto"
            KinshipType.DISTANT_RELATIVE -> "Parente distante"
            else -> "Parentesco desconhecido"
        }
    }
}
```

---

## 🧪 TESTES OBRIGATÓRIOS

### Testes do Algoritmo de Parentesco
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class KinshipCalculatorTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var calculator: KinshipCalculator
    private lateinit var mockRepository: MockKinshipRepository
    
    @Before
    fun setup() {
        mockRepository = MockKinshipRepository()
        calculator = KinshipCalculator(mockRepository)
    }
    
    @Test
    fun `calculate kinship between siblings - success`() = runTest {
        // Arrange
        val member1 = createTestMember("João", "1990-01-01")
        val member2 = createTestMember("Maria", "1992-01-01")
        val familyZeroId = "family-zero-id"
        
        // Act
        val result = calculator.calculate(member1, member2, familyZeroId)
        
        // Assert
        assertTrue(result.isSuccess)
        val kinship = result.getOrNull()
        assertNotNull(kinship)
        assertEquals(KinshipType.SIBLING, kinship?.type)
        assertEquals(KinshipDegree.FIRST, kinship?.degree)
    }
    
    @Test
    fun `calculate kinship between parent and child - success`() = runTest {
        // Arrange
        val parent = createTestMember("João", "1970-01-01")
        val child = createTestMember("Maria", "1990-01-01")
        val familyZeroId = "family-zero-id"
        
        // Act
        val result = calculator.calculate(parent, child, familyZeroId)
        
        // Assert
        assertTrue(result.isSuccess)
        val kinship = result.getOrNull()
        assertNotNull(kinship)
        assertEquals(KinshipType.PARENT_CHILD, kinship?.type)
        assertEquals(KinshipDegree.FIRST, kinship?.degree)
    }
    
    @Test
    fun `calculate kinship between same person - self`() = runTest {
        // Arrange
        val member = createTestMember("João", "1990-01-01")
        val familyZeroId = "family-zero-id"
        
        // Act
        val result = calculator.calculate(member, member, familyZeroId)
        
        // Assert
        assertTrue(result.isSuccess)
        val kinship = result.getOrNull()
        assertNotNull(kinship)
        assertEquals(KinshipType.SELF, kinship?.type)
        assertEquals(KinshipDegree.ZERO, kinship?.degree)
    }
    
    @Test
    fun `calculate kinship between unrelated members - unrelated`() = runTest {
        // Arrange
        val member1 = createTestMember("João", "1990-01-01")
        val member2 = createTestMember("Pedro", "1990-01-01")
        val familyZeroId = "family-zero-id"
        
        // Act
        val result = calculator.calculate(member1, member2, familyZeroId)
        
        // Assert
        assertTrue(result.isSuccess)
        val kinship = result.getOrNull()
        assertNotNull(kinship)
        assertEquals(KinshipType.UNRELATED, kinship?.type)
        assertEquals(KinshipDegree.UNKNOWN, kinship?.degree)
    }
}
```

---

## ✅ CHECKLIST DE VALIDAÇÃO DA FASE 3

### Algoritmo de Parentesco
- [ ] Algoritmo implementado e funcionando
- [ ] Cálculo de graus de parentesco correto
- [ ] Determinação de tipos de parentesco correta
- [ ] Performance adequada (< 1 segundo por cálculo)
- [ ] 100% de cobertura de testes
- [ ] Cache de parentesco funcionando

### Sistema de Relacionamentos
- [ ] CRUD de relacionamentos funcionando
- [ ] Validação de relacionamentos válidos
- [ ] Prevenção de loops genealógicos
- [ ] Cálculo automático de parentesco
- [ ] Cache de parentescos calculados

### Validações de Genealogia
- [ ] Validação de datas de nascimento
- [ ] Prevenção de relacionamentos impossíveis
- [ ] Validação de gerações
- [ ] Alertas de inconsistências
- [ ] Correção automática de problemas

### Interface e UX
- [ ] Telas responsivas e funcionais
- [ ] Loading states implementados
- [ ] Feedback visual adequado
- [ ] Navegação fluida
- [ ] Acessibilidade básica

### Qualidade
- [ ] Testes unitários passando (100% para algoritmo)
- [ ] Testes de integração passando
- [ ] Performance adequada
- [ ] Documentação completa
- [ ] Código limpo e organizado

---

## 🚀 PRÓXIMOS PASSOS

Após completar a Fase 3, estaremos prontos para:

**Fase 4: Árvore Visual e Subfamílias**
- Renderização de árvore genealógica
- Sistema de elementos visuais
- Criação de subfamílias
- Navegação entre visualizações

---

## ⚠️ RISCOS E MITIGAÇÕES

### Riscos Identificados
1. **Complexidade do Algoritmo**: Algoritmo de parentesco é crítico
   - *Mitigação*: Testes exaustivos e validação manual

2. **Performance**: Cálculos complexos podem ser lentos
   - *Mitigação*: Cache inteligente e otimização de queries

3. **Validações de Genealogia**: Regras complexas de validação
   - *Mitigação*: Testes abrangentes para cada regra

### Critérios de Sucesso
- ✅ Algoritmo de parentesco 100% funcional
- ✅ Sistema de relacionamentos funcionando
- ✅ Validações implementadas
- ✅ Performance adequada
- ✅ Testes passando
- ✅ Interface intuitiva

---

**Esta fase implementa o coração do sistema de genealogia. O algoritmo de parentesco deve ser testado exaustivamente antes de prosseguir para a Fase 4.**
