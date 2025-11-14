# Validação de Duplicatas - Implementação e Melhorias

## 📋 Resumo da Implementação

Foi implementado um sistema robusto de validação de duplicatas para prevenir cadastros duplicados de pessoas no sistema. A validação ocorre **ANTES** de salvar, impedindo a criação de registros duplicados.

## ✅ Funcionalidades Implementadas

### 1. **UseCase de Validação (`ValidarDuplicataUseCase`)**

Criado um UseCase que implementa validação em **3 níveis de rigor**:

#### **Nível 1 - CRÍTICO (Bloqueia cadastro)**
- **Critérios**: Nome completo idêntico (normalizado) + Data de nascimento idêntica
- **Ação**: **BLOQUEIA** o cadastro completamente
- **Mensagem**: "Já existe uma pessoa cadastrada com o mesmo nome completo e data de nascimento. Por favor, verifique se não é a mesma pessoa."

#### **Nível 2 - ALTO (Avisa e pede confirmação)**
- **Critérios**: 
  - Nome muito similar (>= 90% de similaridade)
  - Data de nascimento próxima (dentro de 1 ano)
  - Mesmos pais
- **Ação**: **PAUSA** o cadastro e mostra diálogo pedindo confirmação do usuário
- **Mensagem**: "Foram encontradas pessoas muito similares. Por favor, confirme se não são duplicatas antes de continuar."

#### **Nível 3 - MÉDIO (Avisa mas permite)**
- **Critérios**: Score de similaridade alto (>= 75%) usando algoritmo existente
- **Ação**: **AVISA** mas permite continuar com confirmação
- **Mensagem**: "Foram encontradas pessoas com características similares. Revise antes de continuar."

### 2. **Integração no ViewModel**

- Validação ocorre **antes** de salvar no banco de dados
- Quando duplicata é encontrada, o cadastro é **pausado** e um diálogo é exibido
- Usuário pode:
  - **Confirmar**: Continuar com o cadastro mesmo com duplicata
  - **Cancelar**: Cancelar o cadastro e revisar os dados

### 3. **Normalização de Dados**

- **Nomes**: Normalizados removendo acentos, caracteres especiais e convertendo para minúsculas
- **Datas**: Comparação exata ou com tolerância configurável (padrão: 0 dias = exato)
- **Comparação**: Usa algoritmo Levenshtein para similaridade de nomes

## 🔧 Arquivos Criados/Modificados

### Novos Arquivos:
- `app/src/main/java/com/raizesvivas/app/domain/usecase/ValidarDuplicataUseCase.kt`

### Arquivos Modificados:
- `app/src/main/java/com/raizesvivas/app/presentation/screens/cadastro/CadastroPessoaViewModel.kt`
  - Adicionada validação antes de salvar
  - Adicionados campos no `CadastroPessoaState` para gerenciar duplicatas
  - Adicionadas funções para confirmar/cancelar duplicatas

## 💡 Sugestões de Melhorias Adicionais

### 1. **Validação com Múltiplos Campos (Mais Robusto)**

Além de nome + data de nascimento, considerar:

```kotlin
// Critérios adicionais para validação crítica:
- Nome completo idêntico
- Data de nascimento idêntica
- E (pai idêntico OU mãe idêntica)  // Pelo menos um parente igual
- OU local de nascimento idêntico
```

**Vantagem**: Reduz falsos positivos quando há pessoas com mesmo nome e data, mas pais diferentes.

### 2. **Fuzzy Matching para Datas**

Implementar tolerância inteligente para datas:

```kotlin
// Tolerância baseada em contexto:
- Se data tem dia/mês/ano completo: tolerância 0 dias (exato)
- Se data tem apenas ano: tolerância 365 dias (mesmo ano)
- Se data tem mês/ano: tolerância 30 dias (mesmo mês)
```

**Vantagem**: Captura casos onde a data foi digitada com pequenas diferenças.

### 3. **Validação de Apelidos e Nomes Alternativos**

Considerar apelidos e variações de nomes:

```kotlin
// Comparar também:
- Apelido vs Nome completo
- Nome completo vs Apelido
- Variações comuns (ex: "José" vs "Zé", "Maria" vs "Maria da Silva")
```

**Vantagem**: Detecta duplicatas mesmo quando pessoa foi cadastrada com nome diferente.

### 4. **Cache de Validações**

Implementar cache para evitar validações repetidas:

```kotlin
// Cache em memória:
- Chave: hash(nome_normalizado + data_nascimento)
- Valor: resultado da validação
- TTL: 5 minutos
```

**Vantagem**: Melhora performance quando usuário tenta salvar múltiplas vezes.

### 5. **Validação Assíncrona em Tempo Real**

Validar enquanto usuário digita (debounce):

```kotlin
// Validar após 1 segundo de inatividade:
- Nome + Data de nascimento preenchidos
- Mostrar aviso discreto se duplicata encontrada
- Não bloquear, apenas avisar
```

**Vantagem**: Usuário sabe antes de tentar salvar se há duplicata.

### 6. **Sugestão de Fusão de Registros**

Quando duplicata é encontrada, oferecer opção de fusão:

```kotlin
// Opções no diálogo:
1. "Esta é a mesma pessoa - Mesclar registros"
2. "São pessoas diferentes - Continuar cadastro"
3. "Cancelar"
```

**Vantagem**: Facilita correção de duplicatas existentes.

### 7. **Validação com Machine Learning (Futuro)**

Para sistemas com muitos dados:

```kotlin
// Treinar modelo com:
- Histórico de duplicatas confirmadas
- Características de pessoas duplicadas
- Padrões de nomes similares
```

**Vantagem**: Melhora precisão ao longo do tempo.

### 8. **Validação de Relacionamentos Familiares**

Verificar se duplicata já está relacionada:

```kotlin
// Se duplicata encontrada:
- Verificar se já é pai/mãe/filho/cônjuge da pessoa sendo cadastrada
- Se sim, provavelmente é a mesma pessoa
- Se não, pode ser parente com mesmo nome (ex: pai e filho)
```

**Vantagem**: Reduz falsos positivos em famílias com nomes repetidos.

### 9. **Log de Tentativas de Duplicatas**

Registrar tentativas de cadastro duplicado:

```kotlin
// Log para análise:
- ID da pessoa duplicada
- ID do usuário que tentou cadastrar
- Timestamp
- Ação tomada (bloqueado/confirmado)
```

**Vantagem**: Permite análise de padrões e melhorias no algoritmo.

### 10. **Validação em Lote (Admin)**

Para administradores, permitir validação de todas as duplicatas:

```kotlin
// Tela de administração:
- Listar todas as duplicatas potenciais
- Permitir mesclar/remover em lote
- Estatísticas de duplicatas
```

**Vantagem**: Facilita limpeza de dados existentes.

## 🎯 Priorização de Melhorias

### **Alta Prioridade** (Implementar em breve):
1. ✅ Validação com múltiplos campos (pai/mãe/local)
2. ✅ Fuzzy matching para datas
3. ✅ Validação assíncrona em tempo real

### **Média Prioridade** (Próximas sprints):
4. Cache de validações
5. Sugestão de fusão de registros
6. Validação de relacionamentos familiares

### **Baixa Prioridade** (Futuro):
7. Validação com ML
8. Log de tentativas
9. Validação em lote

## 📊 Métricas de Sucesso

Para medir a eficácia da implementação:

- **Taxa de bloqueio**: % de cadastros bloqueados por duplicata crítica
- **Taxa de falsos positivos**: % de bloqueios que eram falsos positivos
- **Taxa de confirmação**: % de usuários que confirmam duplicatas altas/médias
- **Tempo de validação**: Tempo médio para validar duplicatas

## 🔒 Considerações de Segurança

- Validação ocorre **antes** de salvar, evitando criação de registros inválidos
- Dados normalizados não são armazenados, apenas usados para comparação
- Logs de tentativas não devem expor dados sensíveis

## 📝 Notas de Implementação

- A validação **não** bloqueia edições de pessoas existentes
- Apenas novos cadastros são validados
- Tolerância de datas é configurável (padrão: 0 = exato)
- Algoritmo Levenshtein é usado para similaridade de nomes

