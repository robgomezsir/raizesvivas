# PLANO DE DESENVOLVIMENTO COMPLETO - RAÍZES VIVAS

## 1. VISÃO GERAL DO PROJETO

### 1.1 Conceito Central
Aplicativo Android de árvore genealógica gamificada que usa metáfora botânica para representar relações familiares, com sistema hierárquico baseado em uma família-zero como núcleo central e subfamílias derivadas.

### 1.2 Pilares Fundamentais
- **Família-Zero**: Núcleo central imutável, ponto de referência para todos os parentescos
- **Subfamílias**: Ramificações criadas quando membros formam novos núcleos familiares
- **Parentesco Automático**: Sistema inteligente de cálculo de graus de parentesco
- **Metáfora Visual**: Árvores literais com elementos botânicos representando gerações
- **Gamificação**: Sistema de recompensas e conquistas (a ser detalhado posteriormente)

---

## 2. ARQUITETURA DE DADOS

### 2.1 Estrutura do Supabase

#### Tabela: `familias`
- `id` (UUID, PK)
- `nome` (String)
- `tipo` (Enum: 'zero' | 'subfamilia')
- `familia_pai_id` (UUID, FK para familias) - NULL se for família-zero
- `criada_por_casamento` (Boolean)
- `membro_origem_1_id` (UUID, FK para membros) - cônjuge 1
- `membro_origem_2_id` (UUID, FK para membros) - cônjuge 2
- `data_criacao` (Timestamp)
- `icone_arvore` (String) - URL da representação visual
- `nivel_hierarquico` (Integer) - distância da família-zero
- `ativa` (Boolean)
- `user_id` (UUID, FK para auth.users)

#### Tabela: `membros`
- `id` (UUID, PK)
- `familia_principal_id` (UUID, FK para familias) - família onde nasceu/foi adicionado
- `nome_completo` (String)
- `data_nascimento` (Date)
- `cidade_nascimento` (String)
- `estado_civil` (Enum: 'solteiro', 'casado', 'divorciado', 'viuvo')
- `pai_id` (UUID, FK para membros, nullable)
- `mae_id` (UUID, FK para membros, nullable)
- `falecido` (Boolean)
- `data_falecimento` (Date, nullable)
- `endereco_atual` (String)
- `foto_url` (String)
- `elemento_arvore` (Enum: 'raiz', 'casca', 'caule', 'galho', 'folha', 'flor', 'fruto', 'polinizador', 'passaro')
- `geracao_na_familia_zero` (Integer) - geração em relação à família-zero
- `created_at` (Timestamp)
- `updated_at` (Timestamp)

#### Tabela: `relacionamentos`
- `id` (UUID, PK)
- `membro_1_id` (UUID, FK para membros)
- `membro_2_id` (UUID, FK para membros)
- `tipo_relacao` (Enum: 'casamento', 'uniao_estavel', 'pais_filho', 'irmaos')
- `data_inicio` (Date)
- `data_fim` (Date, nullable)
- `familia_formada_id` (UUID, FK para familias, nullable) - subfamília criada
- `ativo` (Boolean)
- `confirmado_por_ambos` (Boolean) - para casamentos

#### Tabela: `membros_familias`
- `id` (UUID, PK)
- `membro_id` (UUID, FK para membros)
- `familia_id` (UUID, FK para familias)
- `papel_na_familia` (Enum: 'pai', 'mae', 'filho', 'conjugue', 'agregado')
- `elemento_nesta_familia` (Enum) - pode ser diferente em cada família

#### Tabela: `parentescos_calculados`
- `id` (UUID, PK)
- `membro_origem_id` (UUID, FK para membros)
- `membro_destino_id` (UUID, FK para membros)
- `grau_parentesco` (String) - ex: "tio-avô", "sobrinho-neto"
- `distancia_geracional` (Integer)
- `tipo_parentesco` (Enum: 'consanguineo', 'afim', 'civil')
- `caminho_parentesco` (JSONB) - array de IDs mostrando a ligação
- `calculado_em` (Timestamp)
- `familia_referencia_id` (UUID, FK) - família-zero sempre

#### Tabela: `sugestoes_subfamilias`
- `id` (UUID, PK)
- `membro_1_id` (UUID, FK)
- `membro_2_id` (UUID, FK)
- `nome_sugerido` (String)
- `status` (Enum: 'pendente', 'aceita', 'rejeitada')
- `data_sugestao` (Timestamp)
- `membros_incluidos` (JSONB) - array de IDs que serão transferidos
- `visualizada` (Boolean)

#### Tabela: `conquistas` (para gamificação futura)
- `id` (UUID, PK)
- `user_id` (UUID, FK)
- `tipo_conquista` (String)
- `data_obtencao` (Timestamp)
- `detalhes` (JSONB)

---

## 3. LÓGICA DE PARENTESCO

### 3.1 Algoritmo de Cálculo de Parentesco

#### Princípios Base:
1. **Referência absoluta**: Todos os parentescos são calculados em relação à família-zero
2. **Cálculo bidirecional**: Sempre calcular parentesco de A→B e B→A
3. **Atualização em cascata**: Ao adicionar membro, recalcular todos os parentescos afetados
4. **Prioridade consanguínea**: Parentescos de sangue têm prioridade sobre afinidade

#### Fluxo de Cálculo:

**Passo 1: Identificar Geração**
- Calcular distância geracional em relação ao(s) membro(s) fundador(es) da família-zero
- Bisavós = geração -2
- Avós = geração -1
- Pais = geração 0 (referência)
- Filhos = geração +1
- Netos = geração +2
- Bisnetos = geração +3

**Passo 2: Determinar Linha de Parentesco**
- Linha direta: ancestral → descendente direto
- Linha colateral: irmãos, tios, primos, sobrinhos
- Linha por afinidade: cônjuges e parentes do cônjuge

**Passo 3: Calcular Grau**
- Contar "saltos" entre membros através de ancestrais comuns
- Irmãos: 2º grau (2 saltos através dos pais)
- Tios/sobrinhos: 3º grau (3 saltos)
- Primos: 4º grau (4 saltos)

**Passo 4: Nomear Parentesco**

Lista de parentescos a serem identificados:

**Linha Direta Ascendente:**
- Pai/Mãe
- Avô/Avó
- Bisavô/Bisavó
- Trisavô/Trisavó

**Linha Direta Descendente:**
- Filho/Filha
- Neto/Neta
- Bisneto/Bisneta
- Trineto/Trineta

**Linha Colateral (mesma geração):**
- Irmão/Irmã
- Meio-irmão/Meia-irmã
- Primo de 1º grau (primo-irmão)
- Primo de 2º grau
- Primo de 3º grau

**Linha Colateral (gerações diferentes):**
- Tio/Tia
- Tio-avô/Tia-avó
- Tio-bisavô/Tia-bisavó
- Sobrinho/Sobrinha
- Sobrinho-neto/Sobrinha-neta
- Sobrinho-bisneto/Sobrinha-bisneta

**Linha por Afinidade:**
- Cônjuge/Esposo/Esposa
- Sogro/Sogra
- Genro/Nora
- Cunhado/Cunhada
- Concunhado/Concunhada
- Padrasto/Madrasta
- Enteado/Enteada

**Casos Especiais:**
- Primo segundo (filho de primo dos pais)
- Tio postiço (cônjuge do tio consanguíneo)
- Primo afim (primo do cônjuge)

### 3.2 Matriz de Decisão para Parentesco

```
SE membro_A é pai/mãe de membro_B
  ENTÃO B→A = "pai"/"mãe"
  E A→B = "filho"/"filha"

SE membro_A e membro_B têm mesmo pai E mesma mãe
  ENTÃO são "irmãos"

SE membro_A e membro_B têm apenas um genitor em comum
  ENTÃO são "meio-irmãos"

SE membro_A é pai/mãe de pai/mãe de membro_B
  ENTÃO A→B = "avô"/"avó"
  E B→A = "neto"/"neta"

SE membro_A é irmão/irmã de pai/mãe de membro_B
  ENTÃO A→B = "tio"/"tia"
  E B→A = "sobrinho"/"sobrinha"

SE membro_A é filho de irmão de avô/avó de membro_B
  ENTÃO são "primos de 1º grau"
```

### 3.3 Função de Atualização

**Triggers para recálculo:**
1. Adição de novo membro
2. Definição/alteração de pai ou mãe
3. Criação de relacionamento de casamento
4. Criação de subfamília
5. Movimentação de membro entre famílias

**Processo:**
1. Identificar todos os membros afetados
2. Limpar parentescos antigos desses membros
3. Recalcular seguindo algoritmo
4. Salvar em `parentescos_calculados`
5. Atualizar elemento da árvore baseado no parentesco

---

## 4. LÓGICA DE SUBFAMÍLIAS

### 4.1 Detecção de Casamentos

**Condições para sugestão:**
1. Membro A define estado civil como "casado" E indica membro B como cônjuge
2. Membro B define estado civil como "casado" E indica membro A como cônjuge
3. Ambos confirmam o relacionamento (campo `confirmado_por_ambos`)

**Processo de detecção:**
- Sistema monitora tabela `relacionamentos`
- Quando `confirmado_por_ambos` = true E tipo = 'casamento'
- Verifica se já existe subfamília para esse casal
- Se não existir, cria registro em `sugestoes_subfamilias`

### 4.2 Criação de Subfamília

**Etapas da criação:**

**1. Preparação da Sugestão:**
- Identificar todos os descendentes diretos do casal (filhos)
- Identificar ascendentes de cada cônjuge (pais)
- Gerar nome sugerido: `Família [Sobrenome Cônjuge1]-[Sobrenome Cônjuge2]`
- Listar todos os membros que serão incluídos
- Exibir notificação ao usuário

**2. Interface de Confirmação:**
- Mostrar nome sugerido (editável)
- Listar membros que serão incluídos:
  - Cônjuge 1 (com papel "pai" ou "mãe")
  - Cônjuge 2 (com papel "pai" ou "mãe")
  - Filhos do casal (com papel "filho")
  - Pais de ambos (com papel "avô"/"avó")
- Opção de adicionar/remover membros
- Botões: "Criar Subfamília" | "Agora Não" | "Nunca Sugerir"

**3. Execução da Criação:**
- Criar registro em tabela `familias`
- Definir `tipo` = 'subfamilia'
- Definir `familia_pai_id` = família-zero (ou família anterior)
- Registrar `membro_origem_1_id` e `membro_origem_2_id`
- Calcular `nivel_hierarquico` = nivel_familia_pai + 1

**4. Vinculação de Membros:**
- Para cada membro incluído:
  - Criar registro em `membros_familias`
  - Definir `papel_na_familia` correto
  - Atualizar `elemento_nesta_familia` baseado no papel
  - **IMPORTANTE**: Membro mantém registro na família-zero E ganha registro na subfamília

**5. Recálculo de Parentescos:**
- Recalcular parentescos dentro da nova subfamília
- Atualizar papéis:
  - Na família-zero: João continua "filho"
  - Na subfamília: João passa a "pai"
- Manter referência à família-zero para cálculos globais

### 4.3 Múltiplos Papéis de um Membro

**Sistema de Contexto:**
Cada membro pode ter diferentes papéis dependendo da família visualizada:

```
João (ID: 123)
├─ Na Família Zero: 
│  ├─ Papel: "filho"
│  ├─ Elemento: "galho"
│  └─ Geração: +1
│
└─ Na Subfamília "Silva-Santos":
   ├─ Papel: "pai"
   ├─ Elemento: "caule"
   └─ Geração: 0 (referência local)
```

**Regra de exibição:**
- Quando usuário está visualizando Família-Zero → mostrar papel na Família-Zero
- Quando usuário está visualizando Subfamília X → mostrar papel na Subfamília X
- Na floresta geral → mostrar linha conectando mesma pessoa em diferentes árvores

---

## 5. ESTRUTURA DE TELAS E NAVEGAÇÃO

### 5.1 Fluxo de Autenticação

#### TELA: Login/Registro
**Componentes:**
- Input: Email
- Input: Senha
- Botão: "Entrar"
- Botão: "Criar Conta"
- Link: "Esqueci minha senha"

**Lógica:**
- Autenticação via Supabase Auth
- Ao criar conta, verificar se usuário já possui família-zero
- Se não, direcionar para criação da família-zero
- Se sim, direcionar para tela principal

---

### 5.2 Configuração Inicial

#### TELA: Criar Família-Zero (exibida apenas uma vez)

**Seção 1: Informações da Família**
- Input: Nome da família (obrigatório)
- Text: "Esta será sua família principal. Todas as outras famílias serão derivadas desta."
- Botão: "Continuar"

**Seção 2: Adicionar Primeiro Membro**
- Radio: "Eu mesmo" | "Outra pessoa"
- Se "Eu mesmo": pré-preencher com dados da conta
- Formulário de membro (detalhado abaixo)
- Botão: "Criar Família e Adicionar Membro"

**Lógica:**
- Criar registro em `familias` com tipo='zero'
- Criar primeiro membro
- Definir geração deste membro como 0 (referência)
- Redirecionar para tela principal

---

### 5.3 Tela Principal (Home)

#### LAYOUT GERAL:
**Header fixo:**
- Logo "Raízes Vivas"
- Ícone: Notificações (badge com contador de sugestões pendentes)
- Ícone: Menu hambúrguer

**Abas inferiores (Navigation Bar):**
1. 🏡 Família (default)
2. 🌳 Floresta
3. ➕ Adicionar
4. 🏆 Conquistas
5. 👤 Perfil

---

### 5.4 ABA: FAMÍLIA

#### Vista: Seletor de Família
**Componentes:**
- Dropdown: Lista de famílias
  - Família-Zero (sempre primeiro)
  - Subfamílias (ordenadas por data de criação)
- Badge: Indicador de família atual
- Botão flutuante: ℹ️ "Sobre esta família"

#### Vista: Árvore Visual da Família Selecionada

**Modo de Visualização:**
- Toggle: "Árvore" | "Lista"

**Modo Árvore:**
- Renderização visual da árvore com elementos botânicos
- Cada membro representado pelo seu elemento:
  - Raízes (bisavós+): ícone de raízes profundas
  - Casca (avós): ícone de tronco texturizado
  - Caule (pais): ícone de tronco principal
  - Galhos (filhos): ícone de galhos divergentes
  - Folhas (netos): ícone de folhas verdes
  - Flores (bisnetos): ícone de flores coloridas
  - Polinizadores (cônjuges): ícone de abelha/borboleta
  - Pássaros (amigos): ícone de pássaro
- Linhas conectando pais→filhos
- Linhas pontilhadas conectando cônjuges
- Zoom e pan habilitados
- Tap em membro: abre card de detalhes

**Modo Lista:**
- Agrupado por geração
- Seções expansíveis:
  - ▼ Bisavós e ancestrais (X membros)
  - ▼ Avós (X membros)
  - ▼ Pais (X membros)
  - ▼ Você e irmãos (X membros) - se aplicável
  - ▼ Filhos (X membros)
  - ▼ Netos (X membros)
  - ▼ Bisnetos (X membros)
  - ▼ Cônjuges (X membros)
  - ▼ Amigos da família (X membros)
- Cada item mostra:
  - Foto (ou ícone placeholder)
  - Nome
  - Elemento da árvore (ícone pequeno)
  - Parentesco com você
- Tap: abre detalhes do membro

#### Card de Detalhes do Membro (Modal)

**Seção Superior:**
- Foto grande (centralizada)
- Nome completo
- Datas: nascimento - falecimento (se aplicável)
- Elemento da árvore (ícone decorativo)

**Seção: Informações Básicas**
- 📍 Cidade de nascimento
- 💍 Estado civil
- 🏠 Endereço atual
- 🎂 Idade (calculada)

**Seção: Relações**
- 👨 Pai: [Nome] (tap para ver)
- 👩 Mãe: [Nome] (tap para ver)
- 💑 Cônjuge: [Nome] (tap para ver)
- Lista de filhos (se houver)

**Seção: Parentesco**
- "Seu parentesco com [Nome]:"
- Texto grande: "Tio-avô" (ou parentesco calculado)
- Ícone ilustrativo do parentesco
- Botão: "Ver caminho de parentesco" (abre diagrama)

**Seção: Famílias**
- Lista de famílias que o membro pertence:
  - Família Zero: filho
  - Família Silva-Santos: pai
- Tap em família: muda contexto e visualiza aquela família

**Botões de Ação:**
- ✏️ Editar
- 🔗 Ver Conexões
- 🗑️ Remover (com confirmação)
- ↗️ Compartilhar

---

### 5.5 ABA: FLORESTA

#### Conceito:
Vista interativa mostrando TODAS as famílias como árvores literais em uma floresta 3D/2D.

**Componentes:**

**Vista Floresta:**
- Renderização de múltiplas árvores
- Árvore da Família-Zero no centro (maior)
- Subfamílias ao redor (menores, conectadas)
- Linhas de vida (raízes conectadas) ligando:
  - Membros que aparecem em múltiplas famílias
  - Famílias-mãe com famílias-filhas

**Interatividade:**
- Pan: arrastar para mover
- Zoom: pinch ou scroll
- Tap em árvore: destaca e mostra nome da família
- Tap longo: abre família na Aba Família

**Legenda Flutuante:**
- Mostrar código de cores/tamanhos
- Família-Zero: árvore dourada/grande
- Subfamílias: árvores verdes/menores
- Linhas: conexões familiares

**Filtros (botão no topo):**
- ☐ Mostrar apenas famílias ativas
- ☐ Mostrar linhas de conexão
- ☐ Destacar família selecionada

**Estatísticas (card inferior deslizável):**
- Total de famílias: X
- Total de membros: X
- Gerações mapeadas: X
- Árvore mais antiga: [Nome]
- Árvore mais recente: [Nome]

---

### 5.6 ABA: ADICIONAR

#### Seletor: O que deseja adicionar?

**Opção 1: ➕ Novo Membro**
- Abre formulário de membro

**Opção 2: 👥 Nova Subfamília**
- Abre assistente de criação manual de subfamília

**Opção 3: 🤝 Novo Relacionamento**
- Abre formulário de relacionamento

**Opção 4: 🐦 Amigo da Família**
- Formulário simplificado para adicionar amigos

---

### 5.7 FORMULÁRIO: Adicionar Membro

**Seção: A qual família este membro pertence?**
- Dropdown: Selecionar família (default: família atualmente visualizada)
- Info: "Você poderá adicionar a outras famílias depois"

**Seção: Informações Básicas**
- Input: Nome completo* (obrigatório)
- Input: Data de nascimento* (date picker)
- Input: Cidade de nascimento*
- Botão: 📷 Adicionar foto (câmera ou galeria)

**Seção: Filiação**
- Dropdown: Pai (lista de membros masculinos + opção "Não sei" + "Adicionar novo")
- Dropdown: Mãe (lista de membros femininos + opção "Não sei" + "Adicionar novo")
- Checkbox: ☐ Pais desconhecidos

**Lógica inteligente:**
- Se família já tem membros na geração de pais: sugerir como opções
- Se usuário seleciona pai/mãe, sistema calcula geração automaticamente
- Se nenhum pai/mãe: perguntar geração manualmente

**Seção: Estado Civil**
- Radio: 
  - ○ Solteiro(a)
  - ○ Casado(a)
  - ○ Divorciado(a)
  - ○ Viúvo(a)
- Se "Casado(a)": mostrar campo adicional
  - Dropdown: Cônjuge (lista de membros + "Adicionar novo")

**Seção: Situação Vital**
- Toggle: Falecido? (Não/Sim)
- Se Sim: Input data de falecimento (date picker)

**Seção: Localização Atual**
- Input: Endereço completo (opcional)
- Botão: 📍 Usar localização atual

**Botões:**
- "Cancelar" (volta)
- "Salvar e Adicionar Outro"
- "Salvar" (principal, destaque)

**Lógica pós-salvamento:**
1. Salvar membro no banco
2. Se pai/mãe selecionados: criar relacionamentos
3. Calcular geração baseado em pais ou input manual
4. Atribuir elemento da árvore baseado na geração
5. Executar algoritmo de cálculo de parentesco
6. Verificar se formou condição para sugestão de subfamília
7. Mostrar toast: "Membro adicionado! Parentescos calculados."
8. Se houver sugestão de subfamília: mostrar notificação imediatamente

---

### 5.8 FORMULÁRIO: Adicionar Relacionamento

**Seção: Tipo de Relacionamento**
- Radio:
  - ○ Casamento
  - ○ União Estável
  - ○ Filiação (pai/mãe → filho)
  - ○ Irmandade

**Se Casamento/União:**
- Dropdown: Pessoa 1*
- Dropdown: Pessoa 2*
- Input: Data de início
- Checkbox: ☐ Ainda ativo

**Se Filiação:**
- Dropdown: Pai ou Mãe*
- Dropdown: Filho*
- Info: "Este relacionamento será usado para calcular parentescos"

**Se Irmandade:**
- Dropdown: Irmão 1*
- Dropdown: Irmão 2*
- Radio: Tipo
  - ○ Irmãos (mesmos pais)
  - ○ Meio-irmãos (um pai em comum)

**Botões:**
- "Cancelar"
- "Salvar Relacionamento"

**Lógica pós-salvamento:**
1. Criar registro em `relacionamentos`
2. Se casamento: verificar se outro cônjuge já confirmou
3. Se ambos confirmaram: criar sugestão de subfamília
4. Recalcular parentescos afetados
5. Toast: "Relacionamento adicionado!"

---

### 5.9 ASSISTENTE: Criar Subfamília Manualmente

**Passo 1: Selecionar Casal Fundador**
- Text: "Quem são os fundadores desta família?"
- Dropdown: Pessoa 1*
- Dropdown: Pessoa 2*
- Verificação: devem ter relacionamento de casamento
- Botão: "Continuar"

**Passo 2: Nome da Subfamília**
- Input: Nome* (sugestão pré-preenchida)
- Info: "Ex: Família Silva-Santos"
- Botão: "Continuar"

**Passo 3: Adicionar Membros**
- Text: "Quem faz parte desta família?"
- Seção: Fundadores (já inclusos, não editável)
  - ✓ [Pessoa 1] - Pai/Mãe
  - ✓ [Pessoa 2] - Pai/Mãe
  
- Seção: Filhos (sugestão automática)
  - Lista de filhos do casal detectados
  - Checkbox para cada: ☐ Incluir
  
- Seção: Ascendentes (pais dos fundadores)
  - ☐ Pai de [Pessoa 1]
  - ☐ Mãe de [Pessoa 1]
  - ☐ Pai de [Pessoa 2]
  - ☐ Mãe de [Pessoa 2]
  
- Botão: "➕ Adicionar outro membro"
- Botão: "Continuar"

**Passo 4: Revisão**
- Text: "Revise as informações da nova família"
- Card resumo:
  - Nome: Família Silva-Santos
  - Fundadores: João Silva e Maria Santos
  - Membros: 8 pessoas
  - Geração: 2ª subfamília
- Lista de membros com papéis
- Botões: "← Voltar" | "Criar Subfamília"

**Lógica pós-criação:**
1. Criar registro de família
2. Criar vínculos em `membros_familias`
3. Recalcular parentescos no contexto da subfamília
4. Atualizar elementos de árvore
5. Redirecionar para visualização da nova família
6. Toast: "Subfamília criada com sucesso!"

---

### 5.10 NOTIFICAÇÕES E SUGESTÕES

#### Central de Notificações (ícone no header)

**Badge:** Contador de notificações não lidas

**Ao clicar:** Abre drawer lateral com lista de notificações

**Tipos de Notificação:**

**1. Sugestão de Subfamília**
- Ícone: 🌱
- Título: "Nova família detectada!"
- Texto: "João e Maria podem formar a Família Silva-Santos"
- Botões: "Ver Sugestão" | "Ignorar"

**2. Parentesco Calculado**
- Ícone: 🔗
- Título: "Novos parentescos identificados"
- Texto: "Adicionamos 5 novos parentes ao mapa"
- Botão: "Ver"

**3. Conquista Desbloqueada** (gamificação futura)
- Ícone: 🏆
- Título: "[Nome da Conquista]"
- Texto: Descrição
- Botão: "Ver Conquistas"

**4. Atualização de Membro**
- Ícone: ℹ️
- Título: "Informações atualizadas"
- Texto: "[Membro] teve suas informações atualizadas"
- Botão: "Ver Perfil"

#### Modal: Sugestão de Subfamília

**Header:**
- Ícone: 🌱
- Título: "Criar nova família?"

**Conteúdo:**
- Card do casal:
  - Fotos lado a lado
  - [Nome 1] ❤️ [Nome 2]
  - Data de casamento
  
- Text: "Detectamos que este casal pode formar uma nova família:"

- Seção: Membros que serão incluídos
  - ✓ [Nome 1] como Pai/Mãe
  - ✓ [Nome 2] como Pai/Mãe
  - ✓ [Filho 1] como Filho
  - ✓ [Filho 2] como Filho
  - ✓ [Pai 1] como Avô
  - ... (lista completa)

- Input: Nome da subfamília
  - Valor sugerido: "Família Silva-Santos"
  - Editável

**Botões:**
- "Agora Não" (fecha modal, marca sugestão como pendente)
- "Nunca Sugerir" (exclui sugestão permanentemente)
- "Criar Família" (botão principal, destaque)

**Lógica ao criar:**
1. Executar processo de criação de subfamília
2. Marcar sugestão como 'aceita'
3. Remover da lista de notificações
4. Mostrar animação de sucesso
5. Redirecionar para visualização da nova família

---

### 5.11 ABA: CONQUISTAS (Gamificação)

#### Header da Aba:
- Título: "Conquistas"
- Subtítulo: "Continue mapeando sua história familiar"

#### Seção: Progresso Geral
- Barra de progresso circular
- Centro: "Nível [X]"
- Texto: "X/Y conquistas desbloqueadas"
- XP atual / XP para próximo nível

#### Categorias de Conquistas (Tabs horizontais)
- 📖 História
- 👨‍👩‍👧‍👦 Conexões
- 🌳 Explorador
- 💎 Especiais

**Categoria: HISTÓRIA**
Conquistas relacionadas a documentar a família

- ☐ **Raízes Plantadas**
  - Descrição: Crie sua primeira família
  - Recompensa: 50 XP
  - Status: ✓ Desbloqueado

- ☐ **Cronista Familiar**
  - Descrição: Adicione 10 membros
  - Progresso: 7/10
  - Recompensa: 100 XP

- ☐ **Guardião da Memória**
  - Descrição: Adicione fotos para 20 membros
  - Progresso: 3/20
  - Recompensa: 150 XP

- ☐ **Historiador**
  - Descrição: Complete 50 membros com todos os dados
  - Progresso: 0/50
  - Recompensa: 500 XP

**Categoria: CONEXÕES**
Conquistas sobre relacionamentos

- ☐ **Cupido Genealógico**
  - Descrição: Registre 5 casamentos
  - Progresso: 1/5
  - Recompensa: 80 XP

- ☐ **Tecelão de Laços**
  - Descrição: Mapeie 3 gerações completas
  - Progresso: 1/3
  - Recompensa: 200 XP

- ☐ **União Sagrada**
  - Descrição: Crie sua primeira subfamília
  - Recompensa: 150 XP
  - Status: 🔒 Bloqueado

- ☐ **Arquiteto de Dinastias**
  - Descrição: Crie 5 subfamílias
  - Progresso: 0/5
  - Recompensa: 400 XP

**Categoria: EXPLORADOR**
Conquistas sobre descoberta e expansão

- ☐ **Desbravador**
  - Descrição: Descubra um parentesco de 5º grau ou mais distante
  - Recompensa: 120 XP

- ☐ **Mestre da Floresta**
  - Descrição: Visualize a floresta completa pela primeira vez
  - Recompensa: 50 XP

- ☐ **Colecionador de Histórias**
  - Descrição: Adicione 100 membros
  - Progresso: 7/100
  - Recompensa: 1000 XP

- ☐ **Centenário**
  - Descrição: Mapeie 100 anos de história familiar
  - Recompensa: 800 XP

**Categoria: ESPECIAIS**
Conquistas raras e ocultas

- ☐ **Phoenix**
  - Descrição: ???
  - Condição oculta: Registre um membro com mais de 100 anos
  - Recompensa: 500 XP

- ☐ **Raízes Profundas**
  - Descrição: ???
  - Condição oculta: Mapeie 7 gerações
  - Recompensa: 1500 XP

- ☐ **Grande Família**
  - Descrição: ???
  - Condição oculta: Alcance 200 membros
  - Recompensa: 2000 XP

#### Sistema de XP e Níveis

**Tabela de Níveis:**
- Nível 1: 0-100 XP → "Iniciante"
- Nível 2: 100-300 XP → "Aprendiz"
- Nível 3: 300-600 XP → "Genealogista"
- Nível 4: 600-1000 XP → "Cronista"
- Nível 5: 1000-1500 XP → "Historiador"
- Nível 10: 5000+ XP → "Mestre das Raízes"

**Recompensas por Nível:**
- Nível 2: Desbloqueia filtro especial na floresta
- Nível 3: Desbloqueia exportação de árvore em PDF
- Nível 5: Desbloqueia temas personalizados
- Nível 10: Desbloqueia modo "Árvore 3D"

---

### 5.12 ABA: PERFIL

#### Seção: Informações do Usuário
- Avatar (editável)
- Nome do usuário
- Email
- Data de cadastro
- Botão: "✏️ Editar Perfil"

#### Seção: Estatísticas Pessoais
Cards em grid 2x2:

**Card 1: Sua Família**
- Ícone: 🏡
- Número grande: "[Nome da Família-Zero]"
- Texto: "Criada em [data]"

**Card 2: Membros**
- Ícone: 👥
- Número grande: "X"
- Texto: "membros mapeados"

**Card 3: Gerações**
- Ícone: 📊
- Número grande: "X"
- Texto: "gerações registradas"

**Card 4: Subfamílias**
- Ícone: 🌳
- Número grande: "X"
- Texto: "subfamílias criadas"

#### Seção: Configurações

**Lista de opções:**

**🔔 Notificações**
- Tap: abre tela de configurações de notificações
  - Toggle: Sugestões de subfamílias
  - Toggle: Novos parentescos
  - Toggle: Conquistas
  - Toggle: Atualizações do app

**🌍 Idioma**
- Tap: abre seletor de idioma
  - ○ Português (Brasil)
  - ○ Português (Portugal)
  - ○ English
  - ○ Español

**🎨 Aparência**
- Tap: abre opções de tema
  - ○ Claro
  - ○ Escuro
  - ○ Automático (sistema)

**📥 Exportar Dados**
- Tap: abre opções de exportação
  - Botão: "Exportar Árvore como PDF"
  - Botão: "Exportar Dados como CSV"
  - Botão: "Exportar Árvore como Imagem"

**🔐 Privacidade**
- Tap: abre configurações de privacidade
  - Toggle: Permitir compartilhamento
  - Toggle: Mostrar aniversários
  - Botão: "Gerenciar Dados"

**❓ Ajuda e Suporte**
- Tap: abre menu de ajuda
  - Tutorial do app
  - Perguntas frequentes
  - Reportar problema
  - Contato

**ℹ️ Sobre**
- Tap: abre tela sobre o app
  - Versão do app
  - Termos de uso
  - Política de privacidade
  - Créditos

**🚪 Sair**
- Tap: confirmação e logout

---

## 6. FLUXOS CRÍTICOS DETALHADOS

### 6.1 FLUXO: Adicionar Primeiro Membro à Família-Zero

**Cenário:** Usuário acabou de criar conta

**Etapa 1: Criação da Família-Zero**
1. Sistema detecta que usuário não tem família
2. Exibe tela de boas-vindas:
   - "Bem-vindo ao Raízes Vivas!"
   - "Vamos começar criando sua família principal"
3. Usuário insere nome da família
4. Sistema cria registro de família com tipo='zero'

**Etapa 2: Definição do Membro Referência**
1. Sistema pergunta: "Quem será o ponto de partida?"
2. Opções:
   - "Eu mesmo" → pré-preenche com dados da conta
   - "Meu pai/minha mãe" → abre formulário limpo
   - "Outro parente" → abre formulário limpo
3. Usuário preenche formulário básico
4. Sistema salva membro com geração=0 (referência)

**Etapa 3: Primeira Expansão**
1. Sistema mostra tutorial rápido:
   - "Ótimo! Agora vamos adicionar mais membros"
   - "Você pode adicionar pais, filhos, irmãos..."
2. Sugere ações:
   - "➕ Adicionar pais de [Nome]"
   - "➕ Adicionar irmãos"
   - "➕ Adicionar cônjuge"
   - "⏭️ Fazer depois"

**Etapa 4: Cálculo Inicial**
1. Para cada novo membro adicionado:
   - Sistema calcula geração relativa ao membro referência
   - Define elemento da árvore
   - Calcula parentesco com referência
   - Atualiza visualização em tempo real

---

### 6.2 FLUXO: Detecção e Criação Automática de Subfamília

**Cenário:** João e Maria são casados e têm filhos

**Etapa 1: Detecção de Condição**

**Momento A:** João é adicionado à Família-Zero
- Estado civil: casado
- Sistema pergunta: "Com quem João é casado?"
- Opções: [Lista de membros] | "Adicionar novo cônjuge"
- João seleciona: "Adicionar novo cônjuge"

**Momento B:** Maria é adicionada
- Formulário de Maria é preenchido
- Estado civil: casado
- Sistema pergunta: "Com quem Maria é casada?"
- Opções aparecem, incluindo "João Silva"
- Sistema detecta: João já disse ser casado com Maria
- Sistema marca relacionamento como `confirmado_por_ambos=true`

**Etapa 2: Verificação de Descendentes**
1. Sistema busca em `membros` onde pai_id=João OU mae_id=Maria
2. Encontra: Pedro (filho), Ana (filha)
3. Sistema conta: 2 filhos

**Etapa 3: Criação da Sugestão**
1. Sistema cria registro em `sugestoes_subfamilias`:
   ```
   {
     membro_1: João,
     membro_2: Maria,
     nome_sugerido: "Família Silva-Santos",
     membros_incluidos: [João, Maria, Pedro, Ana, Pai_João, Mae_João, Pai_Maria, Mae_Maria],
     status: 'pendente'
   }
   ```
2. Sistema incrementa contador de notificações

**Etapa 4: Notificação ao Usuário**
1. Badge no ícone de notificações passa para (1)
2. Se usuário estiver ativo, mostra toast:
   - "💡 Nova família detectada! João e Maria podem formar uma subfamília."
   - Botão: "Ver Sugestão"

**Etapa 5: Revisão da Sugestão**
1. Usuário clica em "Ver Sugestão"
2. Modal abre mostrando:
   - Casal fundador com fotos
   - Lista de membros que serão incluídos
   - Nome sugerido (editável)
   - Preview de como ficará a estrutura

**Etapa 6: Confirmação e Criação**
1. Usuário edita nome para "Família Silva-Santos da Praia"
2. Usuário clica "Criar Família"
3. Sistema executa:

```
INÍCIO TRANSAÇÃO

a) Criar família:
   INSERT INTO familias (
     nome='Família Silva-Santos da Praia',
     tipo='subfamilia',
     familia_pai_id=[ID Família-Zero],
     membro_origem_1_id=João,
     membro_origem_2_id=Maria,
     nivel_hierarquico=1
   )

b) Vincular membros:
   Para cada membro em membros_incluidos:
     INSERT INTO membros_familias (
       membro_id=[ID],
       familia_id=[Nova Subfamília],
       papel_na_familia=[calculado],
       elemento_nesta_familia=[calculado]
     )

c) Definir papéis:
   - João: papel='pai', elemento='caule'
   - Maria: papel='mae', elemento='caule'
   - Pedro: papel='filho', elemento='galho'
   - Ana: papel='filho', elemento='galho'
   - Pai_João: papel='avo', elemento='casca'
   - ...

d) Recalcular parentescos:
   PARA cada par de membros na nova família:
     CALCULAR parentesco no contexto desta família
     INSERT INTO parentescos_calculados

e) Atualizar sugestão:
   UPDATE sugestoes_subfamilias 
   SET status='aceita'

FIM TRANSAÇÃO
```

**Etapa 7: Feedback Visual**
1. Animação de sucesso: árvore brotando
2. Toast: "✅ Família Silva-Santos da Praia criada!"
3. Sistema redireciona para visualização da nova subfamília
4. Na vista de floresta, nova árvore aparece conectada à Família-Zero

---

### 6.3 FLUXO: Visualização de Parentesco com Caminho

**Cenário:** Usuário quer entender como é primo de alguém

**Etapa 1: Abertura do Card de Membro**
1. Usuário está na árvore da Família-Zero
2. Clica em "Carlos Santos"
3. Card de detalhes abre

**Etapa 2: Visualização de Parentesco**
1. Card mostra:
   ```
   Seu parentesco com Carlos:
   
   PRIMO DE 1º GRAU
   (4º grau de parentesco)
   ```
2. Usuário clica em "Ver caminho de parentesco"

**Etapa 3: Modal de Caminho**
1. Modal abre com visualização em árvore horizontal:

```
        [Você: Ana Silva]
               |
               ↓
        [Seu Pai: João Silva]
               |
               ↓
        [Avó Comum: Helena Silva]
               |
               ↓
        [Tio: Paulo Santos]
               |
               ↓
        [Carlos Santos]
```

2. Cada nó mostra:
   - Foto pequena
   - Nome
   - Relação no caminho

3. Texto explicativo embaixo:
   "Carlos é filho do seu tio Paulo, que é irmão do seu pai João. Ambos são filhos da sua avó Helena. Isso faz de vocês primos de primeiro grau."

**Etapa 4: Interatividade**
1. Usuário pode tocar em qualquer membro do caminho
2. Abre card de detalhes daquele membro
3. Botão "Voltar ao Caminho" retorna à visualização

---

### 6.4 FLUXO: Membro com Múltiplos Papéis

**Cenário:** João aparece em duas famílias com papéis diferentes

**Situação Inicial:**
- Família-Zero: João é "filho" de Pedro e Rosa
- Subfamília Silva-Santos: João é "pai" de Ana e Carlos

**Etapa 1: Visualização na Família-Zero**
1. Usuário está visualizando Família-Zero
2. João aparece como elemento "Galho" (filho)
3. Card mostra:
   ```
   João Silva
   🌿 Galho (Filho)
   
   Nesta família:
   Filho de Pedro e Rosa
   Irmão de Maria
   ```

**Etapa 2: Navegação para Subfamília**
1. No card de João, seção "Famílias":
   ```
   Este membro pertence a:
   • Família Zero → filho 🌿
   • Família Silva-Santos → pai 🌳
   ```
2. Usuário toca em "Família Silva-Santos"

**Etapa 3: Mudança de Contexto**
1. Transição animada (árvore se transforma)
2. Agora visualizando Subfamília Silva-Santos
3. João aparece como elemento "Caule" (pai)
4. Card atualiza:
   ```
   João Silva
   🌳 Caule (Pai)
   
   Nesta família:
   Pai de Ana e Carlos
   Casado com Maria Santos
   ```

**Etapa 4: Conexão Visual na Floresta**
1. Na vista "Floresta"
2. João aparece como:
   - Um galho na árvore da Família-Zero
   - Um caule na árvore da Subfamília
3. Linha de vida (raiz dourada) conecta ambas as posições
4. Ao tocar na linha: destaca ambas as posições simultaneamente

---

### 6.5 FLUXO: Adição em Cascata (Pai → Filho → Neto)

**Cenário:** Usuário adiciona gerações sequencialmente

**Etapa 1: Adicionar Avô**
1. Usuário clica "➕ Adicionar Membro"
2. Preenche: "José Silva", nascido em 1940
3. Pais: "Não sei" (não tem cadastrados)
4. Sistema pergunta: "Qual a geração de José?"
   - Opções: Bisavô | Avô | Pai | Você | Filho | Neto
5. Usuário seleciona: "Avô"
6. Sistema:
   - Define geração = -1
   - Define elemento = "Casca" (avô)
   - Salva

**Etapa 2: Adicionar Pai (filho do avô)**
1. Sistema detecta que José não tem filhos
2. Mostra sugestão: "➕ Adicionar filhos de José?"
3. Usuário aceita
4. Formulário abre com:
   - Pai: "José Silva" (pré-selecionado)
   - Campo Mãe: vazio
5. Usuário preenche: "Pedro Silva", nascido em 1965
6. Sistema:
   - Calcula geração automaticamente: -1 + 1 = 0
   - Define elemento = "Caule" (pai)
   - Cria relacionamento pai-filho: José → Pedro
   - Salva

**Etapa 3: Cálculo de Parentesco**
Sistema recalcula para todos os membros existentes:
```
Se usuário referência é Carlos (geração 0):
- José (geração -1): "avô"
- Pedro (geração 0): "irmão" OU "pai" (dependendo de quem é a referência)
```

**Etapa 4: Adicionar Neto**
1. Usuário adiciona "Lucas Silva", filho de Pedro
2. Sistema:
   - Calcula geração: 0 + 1 = +1
   - Define elemento = "Galho" (filho)
   - Recalcula parentescos:
     - José → Lucas: "neto"
     - Pedro → Lucas: "filho"
     - Carlos → Lucas: "sobrinho" (se Carlos for irmão de Pedro)

**Etapa 5: Visualização Atualizada**
1. Árvore visual atualiza em tempo real
2. Mostra 3 gerações:
   ```
        🌳 Casca: José
            |
        🌳 Caule: Pedro
            |
        🌿 Galho: Lucas
   ```
3. Toast: "3 gerações mapeadas! 🎉"

---

## 7. LÓGICA DE ELEMENTOS DA ÁRVORE

### 7.1 Atribuição Automática de Elementos

**Algoritmo de Atribuição:**

```
FUNÇÃO atribuir_elemento(membro, familia_contexto):
  
  SE familia_contexto é familia-zero:
    geração = membro.geracao_na_familia_zero
  SENÃO:
    geração = calcular_geracao_relativa(membro, familia_contexto)
  
  papel = obter_papel_em_familia(membro, familia_contexto)
  
  // Prioridade 1: Papel específico
  SE papel == 'conjugue':
    RETORNAR 'polinizador'
  
  SE membro.tipo == 'amigo':
    RETORNAR 'passaro'
  
  // Prioridade 2: Geração
  SE geração <= -2:
    RETORNAR 'raiz'  // Bisavós e anteriores
  
  SE geração == -1:
    RETORNAR 'casca'  // Avós
  
  SE geração == 0:
    SE papel == 'pai' OU papel == 'mae':
      RETORNAR 'caule'  // Pais na família própria
    SENÃO:
      RETORNAR 'galho'  // Irmãos, na família dos pais
  
  SE geração == 1:
    RETORNAR 'galho'  // Filhos
  
  SE geração == 2:
    RETORNAR 'folha'  // Netos
  
  SE geração >= 3:
    RETORNAR 'flor'  // Bisnetos e posteriores
```

### 7.2 Representação Visual dos Elementos

**Especificações de Design:**

**Raízes (Bisavós+)**
- Cor: Marrom escuro (#5D4037)
- Ícone: Raízes entrelaçadas underground
- Animação: Pulsação lenta
- Posição na árvore: Base, underground

**Casca (Avós)**
- Cor: Marrom médio (#8D6E63)
- Ícone: Textura de tronco com anéis
- Animação: Nenhuma (solidez)
- Posição na árvore: Tronco inferior

**Caule (Pais)**
- Cor: Marrom claro (#A1887F)
- Ícone: Tronco principal forte
- Animação: Balanço suave ao vento
- Posição na árvore: Tronco principal

**Galhos (Filhos)**
- Cor: Verde musgo (#689F38)
- Ícone: Galhos divergentes
- Animação: Balanço médio
- Posição na árvore: Ramificações do tronco

**Folhas (Netos)**
- Cor: Verde vivo (#8BC34A)
- Ícone: Folhas agrupadas
- Animação: Tremulação constante
- Posição na árvore: Pontas dos galhos

**Flores (Bisnetos)**
- Cor: Colorido variado (rosa, amarelo, roxo)
- Ícone: Flores desabrochando
- Animação: Florescimento periódico
- Posição na árvore: Entre folhas

**Polinizadores (Cônjuges)**
- Cor: Amarelo/laranja (#FFA726)
- Ícone: Abelha ou borboleta
- Animação: Voo entre elementos
- Posição: Conectando membros casados

**Pássaros (Amigos)**
- Cor: Azul celeste (#42A5F5)
- Ícone: Pássaro pousado
- Animação: Pouso e decolagem ocasional
- Posição: Nos galhos, não conectado

### 7.3 Interações com Elementos

**Comportamentos ao Tocar:**

1. **Tap simples**: Abre card de detalhes
2. **Tap longo**: Menu contextual
   - Ver parentesco com você
   - Editar membro
   - Ver família
   - Remover
3. **Swipe horizontal sobre elemento**: Navega entre famílias do membro
4. **Pinch sobre elemento**: Zoom localizado

**Indicadores Visuais:**

- **Borda dourada**: Membro referência da família
- **Borda pulsante**: Novo membro adicionado recentemente
- **Opacidade 50%**: Membro falecido
- **Ícone ❤️ sobreposto**: Membro com aniversário hoje/esta semana
- **Ícone 🔔**: Membro com atualização pendente

---

## 8. SISTEMA DE BUSCA E FILTROS

### 8.1 Busca Global

**Localização:** Lupa no header (todas as abas)

**Funcionalidades:**

**Campo de Busca:**
- Placeholder: "Buscar por nome, parentesco..."
- Autocomplete em tempo real
- Mostra resultados agrupados:
  - 👤 Membros (nome, foto, parentesco)
  - 🏡 Famílias (nome, número de membros)
  - 🔗 Parentescos (ex: "buscar tios" mostra todos os tios)

**Filtros Avançados (ícone ao lado da busca):**

**Seção: Tipo**
- ☐ Membros
- ☐ Famílias
- ☐ Relacionamentos

**Seção: Família**
- ○ Todas as famílias
- ○ Apenas Família-Zero
- ○ Apenas Subfamílias
- Dropdown: Selecionar família específica

**Seção: Geração**
- ☐ Bisavós e ancestrais
- ☐ Avós
- ☐ Pais
- ☐ Irmãos
- ☐ Filhos
- ☐ Netos
- ☐ Bisnetos

**Seção: Estado**
- ☐ Vivos
- ☐ Falecidos
- ☐ Apenas casados
- ☐ Com foto
- ☐ Sem foto

**Seção: Idade**
- Slider: de X a Y anos

**Botões:**
- "Limpar Filtros"
- "Aplicar"

### 8.2 Ordenação

**Opções de Ordenação (dropdown):**
- Por nome (A-Z)
- Por nome (Z-A)
- Por idade (crescente)
- Por idade (decrescente)
- Por data de adição (recente primeiro)
- Por geração (ascendente)
- Por geração (descendente)
- Por proximidade de parentesco

---

## 9. FUNCIONALIDADES AVANÇADAS

### 9.1 Exportação de Árvore

**Formatos Disponíveis:**

**1. PDF Detalhado**
- Gera documento multipágina
- Capa com nome da família e logo
- Índice de gerações
- Uma página por membro com:
  - Foto
  - Dados completos
  - Parentescos principais
- Árvore visual completa
- Estatísticas finais

**2. Imagem (PNG/JPG)**
- Opções de resolução:
  - Baixa (para compartilhar)
  - Média (para impressão A4)
  - Alta (para poster)
- Estilos:
  - Árvore visual colorida
  - Árvore minimalista P&B
  - Diagrama formal de parentesco

**3. CSV (Dados Brutos)**
- Tabela com colunas:
  - ID, Nome, Data Nascimento, Cidade, Pai, Mãe, Estado Civil, Falecido, Família, Geração, Elemento

**4. GEDCOM (Padrão Genealógico)**
- Formato universal de árvores genealógicas
- Compatível com outros softwares de genealogia
- Inclui todos os relacionamentos

### 9.2 Compartilhamento

**Opções de Compartilhamento:**

**1. Compartilhar Membro Específico**
- Gera card visual do membro
- Inclui foto, nome, datas, parentesco
- Opções: WhatsApp, Instagram Story, Email, Salvar Imagem

**2. Compartilhar Família Completa**
- Gera link público (opcional: com senha)
- Destinatário pode visualizar árvore somente leitura
- Configurações de privacidade:
  - ☐ Mostrar endereços
  - ☐ Mostrar datas de nascimento completas
  - ☐ Mostrar falecidos
  - ☐ Permitir download

**3. Convite para Colaborar**
- Gera convite para outro usuário contribuir
- Níveis de permissão:
  - Visualizador: apenas ver
  - Editor: adicionar/editar membros
  - Administrador: todas as permissões

### 9.3 Timeline Familiar (Funcionalidade Futura)

**Conceito:**
Linha do tempo com eventos marcantes da família

**Eventos Automáticos:**
- Nascimentos
- Casamentos
- Falecimentos
- Criação de subfamílias

**Eventos Manuais:**
- Usuário pode adicionar:
  - Mudanças de cidade
  - Conquistas (formatura, prêmios)
  - Eventos familiares (reuniões, viagens)
- Cada evento tem:
  - Data
  - Descrição
  - Fotos
  - Membros envolvidos

**Visualização:**
- Linha horizontal com marcadores
- Filtros por tipo de evento
- Zoom temporal (década, ano, mês)
- Agrupamento inteligente

### 9.4 Árvore Interativa 3D (Funcionalidade Premium)

**Conceito:**
Renderização 3D da floresta de árvores

**Características:**
- Motor de renderização: Three.js ou Unity WebGL
- Câmera livre (WASD + mouse)
- Árvores com modelagem realista
- Animações ambientais (vento, pássaros voando)
- Dia/noite baseado em hora real
- Clima baseado em localização

**Interações:**
- Click em elemento: Zoom suave até o membro
- Hover sobre elemento: Tooltip com nome e parentesco
- Modo "Passear pela floresta": Câmera em primeira pessoa
- Modo "Vista de pássaro": Câmera aérea com visão panorâmica
- Fotos dos membros aparecem como "retratos" pendurados nos galhos

**Efeitos Especiais:**
- Novos membros: Brotamento animado
- Casamentos: Polinizadores voando entre árvores
- Aniversários: Flores especiais no elemento do membro
- Falecimento: Folhas caindo suavemente, elemento ganha tom sépia

---

## 10. VALIDAÇÕES E REGRAS DE NEGÓCIO

### 10.1 Validações de Integridade

**Ao Adicionar Membro:**

1. **Nome obrigatório**
   - Mínimo 2 caracteres
   - Não pode ser apenas números

2. **Data de nascimento**
   - Não pode ser futura
   - Se pai/mãe definidos: deve ser posterior ao nascimento dos pais
   - Alerta se diferença de idade pai→filho < 15 anos

3. **Falecimento**
   - Data não pode ser anterior ao nascimento
   - Se falecido: não pode ter estado civil "casado" sem cônjuge registrado como viúvo

4. **Cônjuge**
   - Não pode ser pai/mãe do próprio membro
   - Não pode ser irmão/irmã
   - Alerta se diferença de idade > 30 anos

5. **Pais**
   - Um membro não pode ser pai/mãe de si mesmo
   - Não pode ter mais de 2 pais biológicos
   - Sistema permite adicionar padrasto/madrasta como relacionamento separado

**Ao Criar Relacionamento:**

1. **Casamento**
   - Ambos os membros devem existir
   - Nenhum pode estar casado com outra pessoa (verificar relacionamentos ativos)
   - Alerta se já existe relacionamento de parentesco consanguíneo próximo

2. **Filiação**
   - Filho não pode ser mais velho que pai/mãe
   - Um membro não pode ser filho de descendente dele mesmo (evita loops)

**Ao Criar Subfamília:**

1. **Casal fundador**
   - Deve haver relacionamento de casamento confirmado
   - Pelo menos um deve pertencer à família-pai
   - Não podem formar subfamília se já são fundadores de outra ativa

2. **Membros incluídos**
   - Filhos: devem ter ambos os fundadores como pais
   - Avós: devem ser pais de um dos fundadores

### 10.2 Prevenção de Inconsistências

**Sistema de Detecção de Conflitos:**

**Conflito Tipo 1: Loop Genealógico**
```
Exemplo: João é pai de Maria, Maria é mãe de Ana, Ana é mãe de João

Detecção:
- Ao definir parentesco pai→filho
- Verificar recursivamente se filho não é ancestral do pai
- Se detectado: bloquear e mostrar erro
```

**Conflito Tipo 2: Múltiplos Casamentos Simultâneos**
```
Exemplo: João casado com Maria E casado com Ana ao mesmo tempo

Detecção:
- Ao criar relacionamento de casamento
- Verificar se já existe relacionamento ativo
- Se detectado: perguntar:
  "João já é casado com Maria. Deseja:
  - Finalizar casamento com Maria (divórcio/viuvez)
  - Cancelar novo casamento
  - Registrar como união paralela (com alerta ético)"
```

**Conflito Tipo 3: Datas Impossíveis**
```
Exemplo: Filho nascido antes do pai

Detecção:
- Ao salvar membro com pai/mãe definidos
- Comparar data_nascimento_filho com data_nascimento_pais
- Se data_filho < data_pai: bloquear e mostrar erro
```

**Conflito Tipo 4: Árvore Desconectada**
```
Exemplo: Membro sem conexão com família-zero

Detecção:
- Periodicamente (job noturno)
- Identificar membros "órfãos" (sem pais E sem filhos)
- Notificar usuário: "5 membros não estão conectados à família principal"
- Sugerir ações: conectar ou criar nova subfamília
```

### 10.3 Regras de Privacidade

**Níveis de Visibilidade:**

**Membro Privado:**
- Apenas nome e foto visíveis
- Parentesco mostrado genericamente: "Parente"
- Dados sensíveis ocultos

**Membro Público:**
- Todos os dados visíveis
- Aparece em buscas
- Pode ser compartilhado

**Membro Falecido:**
- Configuração padrão: público após 10 anos do falecimento
- Usuário pode alterar manualmente

**Dados Sensíveis:**
- Endereço: nunca incluído em exportações públicas
- Data nascimento completa: apenas ano em modo público
- Fotos: usuário define quais podem ser públicas

---

## 11. SINCRONIZAÇÃO E OFFLINE

### 11.1 Estratégia de Sincronização

**Arquitetura:**
- Supabase Realtime para sincronização em tempo real
- Cache local (Room Database) para modo offline
- Fila de operações pendentes

**Fluxo Online:**
1. Usuário faz alteração (adicionar membro)
2. Alteração salva localmente (Room)
3. Alteração enviada ao Supabase
4. Supabase confirma e retorna ID definitivo
5. Atualização local com ID definitivo
6. Broadcast para outros dispositivos via Realtime

**Fluxo Offline:**
1. Usuário faz alteração sem internet
2. Alteração salva localmente com ID temporário
3. Operação adicionada à fila de pendências
4. Interface mostra badge "Sincronização pendente"
5. Quando conexão restaurada:
   - Processar fila em ordem
   - Resolver conflitos se necessário
   - Atualizar IDs temporários

### 11.2 Resolução de Conflitos

**Cenário 1: Edição Simultânea do Mesmo Membro**
```
Dispositivo A (offline): Edita nome "João Silva" → "João Pedro Silva"
Dispositivo B (online): Edita nome "João Silva" → "João Carlos Silva"

Resolução:
1. B sincroniza primeiro (está online)
2. A tenta sincronizar quando reconecta
3. Sistema detecta conflito (versões diferentes)
4. Mostra modal ao usuário A:
   "Conflito detectado: O nome de João foi alterado por outro dispositivo
   - Sua versão: João Pedro Silva
   - Versão atual: João Carlos Silva
   - Escolha: [Manter sua versão] [Aceitar versão atual] [Mesclar manualmente]"
```

**Cenário 2: Adição Duplicada**
```
Dispositivo A (offline): Adiciona "Maria Santos"
Dispositivo B (offline): Adiciona "Maria Santos" (mesma pessoa)

Resolução:
1. Sistema detecta possível duplicata (nome + data nascimento similares)
2. Ao sincronizar, mostra:
   "Possível duplicata detectada
   - Maria Santos, nascida em 15/03/1980, filha de José
   - Maria Santos, nascida em 15/03/1980, filha de José
   - Escolha: [São pessoas diferentes] [É a mesma pessoa - mesclar]"
3. Se mesclar: preserva dados mais completos, mantém apenas um registro
```

### 11.3 Modo Offline

**Funcionalidades Disponíveis Offline:**
- ✅ Visualizar árvore (dados em cache)
- ✅ Ver detalhes de membros
- ✅ Adicionar novos membros
- ✅ Editar membros existentes
- ✅ Criar relacionamentos
- ✅ Navegar entre famílias
- ❌ Buscar membros (apenas cache local)
- ❌ Ver sugestões de subfamílias novas
- ❌ Exportar (requer processamento online)

**Indicadores Visuais:**
- Badge "Offline" no header (laranja)
- Contador de operações pendentes
- Ícone de sincronização com animação quando conectando

---

## 12. NOTIFICAÇÕES E ENGAJAMENTO

### 12.1 Sistema de Notificações Push

**Tipos de Notificação:**

**1. Sugestões de Subfamília**
- Título: "💡 Nova família detectada!"
- Corpo: "João e Maria podem formar a Família Silva-Santos"
- Ação: Abrir modal de sugestão
- Frequência: Imediata quando detectado

**2. Aniversários**
- Título: "🎂 Aniversário hoje!"
- Corpo: "José Silva completa 75 anos"
- Ação: Abrir perfil do membro
- Frequência: 9h da manhã do dia
- Configurável: Notificar 1 dia antes, no dia, ou nunca

**3. Conquistas**
- Título: "🏆 Conquista desbloqueada!"
- Corpo: "Você alcançou [Nome da Conquista]"
- Ação: Abrir aba de conquistas
- Frequência: Imediata quando desbloqueada

**4. Lembrete de Completar Dados**
- Título: "📝 Complete sua árvore"
- Corpo: "5 membros ainda não têm foto"
- Ação: Abrir lista de membros incompletos
- Frequência: Semanal (configurável)

**5. Colaboração**
- Título: "👥 Novo colaborador"
- Corpo: "[Nome] agora pode editar sua árvore"
- Ação: Abrir configurações de compartilhamento
- Frequência: Imediata

**6. Atualizações do App**
- Título: "✨ Novidades no Raízes Vivas"
- Corpo: "Novos recursos disponíveis!"
- Ação: Abrir changelog
- Frequência: A cada update

### 12.2 Estratégias de Reengajamento

**Notificações Inteligentes:**

**Usuário Novo (primeira semana):**
- Dia 1: "Bem-vindo! Adicione seu primeiro parente"
- Dia 3: "Continue mapeando! Você já tem X membros"
- Dia 7: "🎉 Uma semana preservando memórias!"

**Usuário Ativo:**
- Semanal: "Sua árvore cresceu X% esta semana"
- Mensal: "Relatório mensal: X novos membros, Y conquistas"

**Usuário Inativo (não abre há 7 dias):**
- Dia 7: "Sentimos sua falta! Continue sua árvore"
- Dia 14: "💡 Sugestão: Adicione fotos aos membros"
- Dia 30: "🌳 Sua floresta espera por você"

**Datas Especiais:**
- Dia das Mães/Pais: "Celebre adicionando uma memória especial"
- Fim de ano: "Reuna a família e complete sua árvore"

### 12.3 In-App Messaging

**Dicas Contextuais:**
- Tooltip ao adicionar primeiro membro: "Comece por você ou pelos mais velhos"
- Banner ao criar 3ª subfamília: "💡 Dica: Use a vista Floresta para visualizar tudo"
- Modal ao alcançar 50 membros: "Parabéns! Considere exportar um backup"

**Sugestões Proativas:**
```
Sistema analisa árvore e sugere:

- "Maria não tem foto. Adicionar agora?"
- "José tem 2 irmãos. Quer adicionar os pais deles?"
- "Detectamos 3 casais sem filhos registrados. Eles têm filhos?"
- "Seu bisavô não tem pais. Pesquisar ancestrais?"
```

---

## 13. SEGURANÇA E BACKUP

### 13.1 Autenticação e Autorização

**Níveis de Acesso:**

**Proprietário (Criador da Família-Zero):**
- Todas as permissões
- Pode deletar família inteira
- Pode transferir propriedade
- Gerencia colaboradores

**Administrador:**
- Adicionar/editar/remover membros
- Criar subfamílias
- Convidar colaboradores (não administradores)
- Não pode deletar família

**Editor:**
- Adicionar/editar membros
- Criar relacionamentos
- Não pode deletar membros
- Não pode criar subfamílias

**Visualizador:**
- Apenas leitura
- Pode exportar para uso pessoal
- Não pode fazer alterações

### 13.2 Sistema de Backup

**Backup Automático:**
- Frequência: Diário (3h da manhã)
- Retenção: 30 dias
- Armazenamento: Supabase Storage
- Formato: JSON completo + fotos

**Backup Manual:**
- Botão em Perfil > Exportar Dados
- Gera arquivo ZIP contendo:
  - familias.json
  - membros.json
  - relacionamentos.json
  - parentescos.json
  - /fotos/ (todas as imagens)
- Usuário baixa ou salva no Google Drive

**Restauração:**
- Opção em Configurações > Avançado
- Upload de arquivo de backup
- Preview das alterações antes de confirmar
- Opções:
  - Substituir tudo (apaga dados atuais)
  - Mesclar (mantém dados atuais, adiciona do backup)
  - Apenas membros faltantes

### 13.3 Criptografia e Privacidade

**Dados Criptografados:**
- Endereços residenciais
- Dados sensíveis marcados como privados
- Fotos (opcional, configurável)

**Criptografia:**
- Em trânsito: TLS 1.3
- Em repouso: AES-256
- Chave gerenciada por usuário (senha forte obrigatória)

**LGPD/GDPR Compliance:**
- Direito ao esquecimento: Deletar conta remove todos os dados
- Exportação de dados: Formato legível (JSON + CSV)
- Consentimento explícito para compartilhamento
- Anonimização de dados ao compartilhar publicamente

---

## 14. PERFORMANCE E OTIMIZAÇÃO

### 14.1 Estratégias de Cache

**Cache de Árvore Visual:**
- Renderização em bitmap salva localmente
- Regenera apenas quando há mudanças
- Diferentes resoluções cacheadas (thumb, média, alta)

**Cache de Parentescos:**
- Tabela `parentescos_calculados` serve como cache
- Invalidação parcial: apenas recalcula afetados
- Calculo lazy: só calcula quando visualizado

**Cache de Imagens:**
- Fotos originais no Supabase Storage
- Thumbnails gerados e cacheados
- Formatos: 50x50, 150x150, 500x500
- WebP para economia de banda

### 14.2 Otimização de Queries

**Paginação:**
- Lista de membros: 20 por vez
- Scroll infinito carrega mais
- Pré-carrega próxima página

**Indexes no Supabase:**
```sql
CREATE INDEX idx_membros_familia ON membros(familia_principal_id);
CREATE INDEX idx_membros_pais ON membros(pai_id, mae_id);
CREATE INDEX idx_relacionamentos_membros ON relacionamentos(membro_1_id, membro_2_id);
CREATE INDEX idx_parentescos_origem ON parentescos_calculados(membro_origem_id);
```

**Queries Otimizadas:**
- Usar `select` específico (não `*`)
- Joins eficientes
- Limitar resultados com `limit`
- Filtrar no banco, não no app

### 14.3 Renderização da Árvore

**Para Famílias Pequenas (<50 membros):**
- Renderização completa em tela
- SVG nativo do Android
- Animações suaves

**Para Famílias Médias (50-200 membros):**
- Virtualização: renderiza apenas membros visíveis
- LOD (Level of Detail): simplifica elementos distantes
- Canvas em vez de SVG

**Para Famílias Grandes (200+ membros):**
- Renderização por chunks
- Zoom progressivo (mais detalhes ao aproximar)
- Modo simplificado por padrão
- Opção de "Vista Completa" (pode demorar)

---

## 15. TESTES E QUALIDADE

### 15.1 Casos de Teste Críticos

**Teste 1: Cálculo de Parentesco Básico**
```
Dado:
  - João é pai de Maria
  - Maria é mãe de Pedro
Quando:
  - Sistema calcula parentesco João ↔ Pedro
Então:
  - João → Pedro = "Avô"
  - Pedro → João = "Neto"
```

**Teste 2: Prevenção de Loop**
```
Dado:
  - João é pai de Maria
  - Maria é mãe de Ana
Quando:
  - Usuário tenta definir Ana como mãe de João
Então:
  - Sistema bloqueia com erro: "Loop genealógico detectado"
```

**Teste 3: Subfamília Automática**
```
Dado:
  - João (casado) indica Maria como cônjuge
  - Maria (casada) indica João como cônjuge
  - João e Maria têm 2 filhos
Quando:
  - Ambos confirmam relacionamento
Então:
  - Sistema cria sugestão de subfamília
  - Notificação aparece
  - Sugestão inclui 2 filhos
```

**Teste 4: Múltiplos Papéis**
```
Dado:
  - João é filho na Família-Zero
  - João é pai na Subfamília Silva
Quando:
  - Usuário visualiza Família-Zero
Então:
  - João aparece como "Galho" (filho)
Quando:
  - Usuário muda para Subfamília Silva
Então:
  - João aparece como "Caule" (pai)
```

**Teste 5: Sincronização Offline**
```
Dado:
  - App está offline
  - Usuário adiciona 3 membros
Quando:
  - Conexão restaurada
Então:
  - 3 membros sincronizam
  - IDs temporários são substituídos
  - Nenhum dado perdido
```

### 15.2 Testes de Performance

**Teste de Carga:**
- Criar família com 500 membros
- Medir tempo de renderização
- Meta: < 3 segundos para tela inicial

**Teste de Memória:**
- Navegar por todas as telas
- Monitorar uso de RAM
- Meta: < 200MB em uso normal

**Teste de Bateria:**
- Usar app por 1 hora contínua
- Medir consumo
- Meta: < 5% de bateria

### 15.3 Testes de Usabilidade

**Fluxo Novo Usuário:**
- Da criação de conta até primeiro membro: < 2 minutos
- Taxa de completude: > 80%

**Compreensão de Parentesco:**
- Usuários testam identificar "tio-avô"
- Taxa de acerto: > 70%

**Criação de Subfamília:**
- Usuários conseguem criar sem ajuda
- Taxa de sucesso: > 60%

---

## 16. ROADMAP DE DESENVOLVIMENTO

### 16.1 Fase 1: MVP (Mínimo Produto Viável) - 8 semanas

**Semana 1-2: Setup e Autenticação**
- Configurar projeto Kotlin + Jetpack Compose
- Integrar Supabase
- Implementar login/registro
- Tela de boas-vindas

**Semana 3-4: Família-Zero e Membros**
- Criar estrutura de dados no Supabase
- Formulário de adicionar membro
- Lista de membros (modo lista)
- Visualização de detalhes

**Semana 5-6: Parentesco e Relacionamentos**
- Implementar algoritmo de cálculo de parentesco
- Formulário de relacionamentos
- Exibição de parentesco em cards
- Atualização automática ao adicionar membros

**Semana 7-8: Árvore Visual Básica**
- Renderização SVG da árvore
- Elementos visuais básicos (sem animações)
- Navegação por tap
- Sistema de elementos (raiz, caule, galho, etc.)

**Entregáveis do MVP:**
- ✅ Criação de família-zero
- ✅ Adicionar/editar/remover membros
- ✅ Cálculo automático de parentesco básico
- ✅ Visualização em lista e árvore simples
- ✅ 10 tipos de parentesco identificados

---

### 16.2 Fase 2: Subfamílias - 4 semanas

**Semana 9-10: Detecção e Sugestão**
- Lógica de detecção de casamentos
- Sistema de sugestões
- Notificações básicas
- Modal de criação de subfamília

**Semana 11-12: Múltiplos Contextos**
- Implementar múltiplos papéis por membro
- Seletor de família
- Navegação entre famílias
- Recálculo de parentesco por contexto

**Entregáveis Fase 2:**
- ✅ Criação automática e manual de subfamílias
- ✅ Membros aparecem em múltiplas famílias
- ✅ Parentesco contextual funcional
- ✅ Sistema de notificações básico

---

### 16.3 Fase 3: Floresta e Gamificação - 4 semanas

**Semana 13-14: Vista Floresta**
- Renderização de múltiplas árvores
- Conexões entre famílias
- Interatividade (zoom, pan)
- Filtros e estatísticas

**Semana 15-16: Sistema de Conquistas**
- Definir 30 conquistas
- Sistema de XP e níveis
- Aba de conquistas
- Notificações de desbloqueio
- Animações de recompensa

**Entregáveis Fase 3:**
- ✅ Visualização de floresta interativa
- ✅ 30 conquistas implementadas
- ✅ Sistema de progressão
- ✅ Engajamento gamificado

---

### 16.4 Fase 4: Polimento e Avançado - 4 semanas

**Semana 17-18: Exportação e Compartilhamento**
- Exportar PDF
- Exportar imagem
- Compartilhamento via link
- Sistema de permissões

**Semana 19-20: Offline e Performance**
- Modo offline completo
- Sincronização inteligente
- Otimizações de renderização
- Cache avançado

**Entregáveis Fase 4:**
- ✅ Múltiplos formatos de exportação
- ✅ Compartilhamento funcional
- ✅ App funciona 100% offline
- ✅ Performance otimizada

---

### 16.5 Fase 5: Recursos Premium (Futuro)

**Funcionalidades Premium:**
- 🌳 Árvore 3D interativa
- 📅 Timeline de eventos
- 🎨 Temas personalizados avançados
- 📊 Relatórios genealógicos detalhados
- 🔄 Sincronização multi-dispositivo ilimitada
- ☁️ Backup em nuvem ilimitado
- 👥 Colaboradores ilimitados
- 🎭 Modo "Árvore Real" (fotos substituem ícones)

**Modelo de Monetização:**
- Versão gratuita: até 50 membros, 2 subfamílias
- Premium mensal: R$ 9,90 - recursos ilimitados
- Premium anual: R$ 89,90 (25% desconto)
- Premium vitalício: R$ 249,90 (compra única)

---

## 17. TECNOLOGIAS E ARQUITETURA

### 17.1 Stack Tecnológico

**Frontend (Android):**
- **Linguagem**: Kotlin 1.9+
- **UI**: Jetpack Compose
- **Navegação**: Compose Navigation
- **Injeção de Dependência**: Hilt
- **Async**: Coroutines + Flow
- **Imagens**: Coil
- **Gráficos**: Canvas Compose / Custom SVG

**Backend (Supabase):**
- **Database**: PostgreSQL
- **Auth**: Supabase Auth
- **Storage**: Supabase Storage (fotos)
- **Realtime**: Supabase Realtime (sincronização)
- **Edge Functions**: Para cálculos complexos (opcional)

**Local:**
- **Cache**: Room Database
- **Preferências**: DataStore
- **Arquivos**: File System (imagens offline)

### 17.2 Arquitetura do App

**Padrão: Clean Architecture + MVVM**

```
Camadas:

┌─────────────────────────────┐
│   Presentation (UI)         │
│   - Composables             │
│   - ViewModels              │
│   - UI States               │
└─────────────────────────────┘
            ↓↑
┌─────────────────────────────┐
│   Domain (Lógica)           │
│   - Use Cases               │
│   - Entities                │
│   - Repository Interfaces   │
└─────────────────────────────┘
            ↓↑
┌─────────────────────────────┐
│   Data (Fontes)             │
│   - Repository Impl         │
│   - Supabase Client         │
│   - Room Database           │
│   - Mappers                 │
└─────────────────────────────┘
```

**Módulos Principais:**

1. **:app** - Aplicação principal
2. **:feature-family** - Funcionalidades de família
3. **:feature-member** - Funcionalidades de membros
4. **:feature-forest** - Vista floresta
5. **:feature-achievements** - Gamificação
6. **:core-data** - Repositórios e fontes de dados
7. **:core-domain** - Lógica de negócio
8. **:core-ui** - Componentes reutilizáveis
9. **:core-utils** - Utilitários gerais

### 17.3 Estrutura de Pacotes

```
com.raizesvivas/
├── app/
│   ├── MainActivity
│   ├── RaizesVivasApp
│   └── navigation/
│
├── feature/
│   ├── family/
│   │   ├── presentation/
│   │   │   ├── list/
│   │   │   ├── detail/
│   │   │   └── tree/
│   │   ├── domain/
│   │   └── data/
│   │
│   ├── member/
│   │   ├── presentation/
│   │   ├── domain/
│   │   └── data/
│   │
│   └── forest/
│       ├── presentation/
│       ├── domain/
│       └── data/
│
├── core/
│   ├── data/
│   │   ├── repository/
│   │   ├── source/
│   │   └── mapper/
│   │
│   ├── domain/
│   │   ├── model/
│   │   ├── usecase/
│   │   └── repository/
│   │
│   ├── ui/
│   │   ├── components/
│   │   ├── theme/
│   │   └── utils/
│   │
│   └── utils/
│       ├── extensions/
│       ├── validators/
│       └── algorithms/
│
└── di/
    └── AppModule
```

---

## 18. CASOS ESPECIAIS E EDGE CASES

### 18.1 Casos de Família Complexos

#### Caso 1: Família Recomposta (Padrastos/Madrastas)

**Cenário:**
- João foi casado com Maria, tiveram filho Pedro
- João divorciou de Maria
- João casou com Ana
- Ana tem filha Clara de relacionamento anterior
- João e Ana tiveram filho Lucas juntos

**Solução no App:**

**Estrutura de Dados:**
```
Família-Zero:
├─ João (pai biológico)
├─ Maria (mãe biológica de Pedro)
├─ Pedro (filho de João e Maria)
├─ Ana (madrasta de Pedro, mãe de Clara e Lucas)
├─ Clara (enteada de João)
└─ Lucas (filho de João e Ana)

Relacionamentos:
- João ↔ Maria: casamento (inativo, data_fim: XX/XX/XXXX)
- João ↔ Ana: casamento (ativo)
- Relacionamento especial: Ana ↔ Pedro: "madrasta-enteado"
- Relacionamento especial: João ↔ Clara: "padrasto-enteada"
```

**Cálculo de Parentesco:**
```
Pedro ↔ Lucas: "Meio-irmão" (mesmo pai, mães diferentes)
Pedro ↔ Clara: "Sem parentesco consanguíneo" (mas são enteados do mesmo conjunto)
Maria ↔ Lucas: "Sem parentesco direto" (ex-esposa do pai)
```

**Interface:**
- Card de Pedro mostra:
  - Pai: João Silva
  - Mãe: Maria Santos
  - Madrasta: Ana Costa
  - Irmãos: Lucas (meio-irmão)
  - Família: Família Silva (com João e Ana)

**Lógica de Subfamília:**
- Subfamília "Família Silva-Costa" inclui:
  - João e Ana (fundadores)
  - Lucas (filho de ambos)
  - Pedro (filho de João, enteado de Ana)
  - Clara (filha de Ana, enteada de João)

---

#### Caso 2: Adoção

**Cenário:**
- Casal João e Maria adotam Ana
- Ana é filha biológica de pais desconhecidos

**Solução no App:**

**Campo Adicional no Formulário:**
- Checkbox: ☐ Filho(a) adotivo(a)
- Se marcado, mostra campos:
  - Pais biológicos conhecidos? Sim/Não
  - Se Sim: campos para adicionar
  - Data da adoção

**Estrutura de Dados:**
```
Ana:
  - pai_adotivo_id: João
  - mae_adotiva_id: Maria
  - pai_biologico_id: NULL (ou ID se conhecido)
  - mae_biologica_id: NULL
  - tipo_filiacao: 'adotiva'
```

**Cálculo de Parentesco:**
- Parentescos são calculados através dos pais adotivos
- Ana é considerada irmã dos filhos biológicos de João e Maria
- Elemento da árvore: mesmo que filho biológico (galho)
- Badge especial no card: "👶 Adotivo(a)" (opcional, configurável)

**Privacidade:**
- Usuário pode ocultar informação de adoção em compartilhamentos públicos
- Pais biológicos (se cadastrados) não aparecem em árvore pública

---

#### Caso 3: Gêmeos e Trigêmeos

**Cenário:**
- Maria teve gêmeos: João e José
- Mesmo pai, mesma mãe, mesma data de nascimento

**Solução no App:**

**Detecção Automática:**
```
AO salvar membro:
  SE mesmos pais E mesma data_nascimento:
    PERGUNTAR: "Detectamos outro membro com mesmos pais e data.
                São gêmeos? [Sim] [Não]"
```

**Se confirmado:**
- Campo adicional: `tipo_nascimento: 'gemelar'`
- Campo: `grupo_gemelar_id: [UUID compartilhado]`
- Campo: `ordem_nascimento: 1 / 2 / 3...`

**Visualização:**
- Na árvore: Ícone especial 👯 ao lado do nome
- Card mostra: "Gêmeo de José Silva"
- Elementos aparecem lado a lado, mesma altura
- Linha conectora especial (dupla) dos pais

**Parentesco:**
- Entre gêmeos: "Irmão gêmeo" / "Irmã gêmea"
- Com outros: calculado normalmente

---

#### Caso 4: Casamentos Consanguíneos (Primos que se casam)

**Cenário:**
- João e Maria são primos de 1º grau
- Decidem se casar

**Solução no App:**

**Validação ao Criar Relacionamento:**
```
AO criar casamento entre João e Maria:
  CALCULAR parentesco_atual
  
  SE parentesco_atual é consanguíneo E grau <= 3:
    MOSTRAR alerta:
    "⚠️ Atenção: João e Maria são primos.
     Casamentos consanguíneos podem ter implicações.
     Deseja continuar? [Sim] [Cancelar]"
```

**Se confirmado:**
- Relacionamento é criado normalmente
- Badge no card: "💑 Casamento consanguíneo"
- Cálculo de parentesco dos filhos considera ambas as linhas:
  - Filho é 2º grau de um lado E 3º grau do outro
  - Sistema mostra: "Parentesco múltiplo"

**Visualização na Árvore:**
- Linha conectora do casamento em cor diferente (laranja)
- Tooltip explicativo ao passar o mouse

---

### 18.2 Casos de Dados Incompletos

#### Caso 1: Pais Desconhecidos

**Cenário:**
- Usuário quer adicionar avô, mas não sabe quem são os pais do avô

**Solução:**

**Opção no Formulário:**
- "Pais desconhecidos" → não pede pais
- Sistema pergunta: "Qual a geração deste membro?"
- Usuário define manualmente
- Membro fica "solto" na árvore, sem conexão ascendente
- Ícone especial: 🔍 "Ancestrais desconhecidos"

**Incentivo à Pesquisa:**
- Conquista: "Detetive Genealógico" - Complete 5 ancestrais desconhecidos
- Notificação periódica: "💡 Pesquise sobre os ancestrais de José"

---

#### Caso 2: Datas Aproximadas

**Cenário:**
- Usuário sabe que bisavô nasceu "por volta de 1900", mas não data exata

**Solução:**

**Campo de Data com Opções:**
- Toggle: "Data exata" / "Data aproximada"
- Se aproximada:
  - Input: Ano (obrigatório)
  - Dropdown: Mês (opcional)
  - Checkbox: ☐ Incerto

**Visualização:**
- Data mostrada como: "~1900" (com til)
- Card mostra: "Nascimento: circa 1900"
- Cálculo de idade: "aproximadamente X anos"

**Validações:**
- Menos rígidas para datas aproximadas
- Aceita inconsistências pequenas (ex: pai nascido 10 anos antes do filho)

---

#### Caso 3: Sobrenomes Variantes

**Cenário:**
- Família tem sobrenome registrado de formas diferentes ao longo do tempo
- "Silva" / "da Silva" / "Sylva"

**Solução:**

**Campo Adicional:**
- Nome completo: João Silva
- Variantes do nome: [+ Adicionar variante]
  - João da Silva
  - João Sylva

**Busca Inteligente:**
- Ao buscar "Silva", encontra todas as variantes
- Sugestão automática ao adicionar membro:
  "Detectamos sobrenomes similares: Silva, Sylva. É o mesmo?"

**Agrupamento:**
- Opção de agrupar variantes para estatísticas
- "Família Silva/Sylva: 15 membros"

---

### 18.3 Casos de Múltiplas Nacionalidades

#### Caso: Família Imigrante

**Cenário:**
- Bisavô nasceu na Itália
- Avô nasceu no Brasil
- Família tem membros em 3 países

**Solução no App:**

**Campos Adicionais:**
- Nacionalidade(s): [Dropdown múltiplo]
- Cidade de nascimento: [Input + País]
- Cidades onde viveu: [+ Adicionar cidade]
  - São Paulo, Brasil (1950-1980)
  - Roma, Itália (1980-2000)
  - Lisboa, Portugal (2000-atual)

**Visualização:**
- Bandeirinha 🇧🇷 ao lado do nome
- Mapa interativo (funcionalidade futura):
  - Mostra distribuição geográfica da família
  - Linhas de migração

**Estatísticas:**
- Card: "Países representados: 3"
- "Maior concentração: Brasil (45 membros)"

---

### 18.4 Casos de Falecimento

#### Caso 1: Membro Recentemente Falecido

**Cenário:**
- Usuário precisa registrar falecimento de um familiar

**Solução:**

**Atualização Sensível:**
1. Ao editar membro, toggle "Falecido"
2. Se ativado, campos aparecem:
   - Data de falecimento
   - Causa (opcional, privado)
   - Local de sepultamento (opcional)
3. Sistema pergunta: "Deseja adicionar uma homenagem?"
   - Campo de texto livre
   - Adicionar fotos de lembrança

**Notificação Respeitosa:**
- Não cria notificação de conquista
- Apenas toast discreto: "Informações atualizadas"

**Visualização:**
- Foto com borda preta fina
- Datas: "1950 - 2024 (74 anos)"
- Elemento da árvore em tons de sépia
- Opção de adicionar epitáfio

**Homenagens:**
- Outros membros da família podem adicionar mensagens
- Seção especial no card: "Memórias e Homenagens"
- Galeria de fotos em memoriam

---

#### Caso 2: Aniversários de Falecimento

**Cenário:**
- Completam-se X anos do falecimento de um familiar

**Solução:**

**Notificação Especial:**
- "🕊️ Hoje faz X anos do falecimento de [Nome]"
- Ação: "Ver perfil" | "Adicionar homenagem"

**Destaque no App:**
- Membro aparece destacado na árvore com brilho suave
- Card tem fundo em tom pastel
- Sugestão: "Compartilhe uma memória sobre [Nome]"

---

### 18.5 Casos de Privacidade e Sensibilidade

#### Caso 1: Menor de Idade

**Cenário:**
- Usuário adiciona criança de 5 anos

**Proteção Automática:**
```
SE data_nascimento indica < 18 anos:
  - privacidade = 'restrita' (automático)
  - Compartilhamento público: BLOQUEADO
  - Apenas nome e parentesco visíveis externamente
  - Foto: não exportável
  - Endereço: oculto obrigatoriamente
```

**Alerta ao Usuário:**
- "🔒 Este membro é menor de idade. Dados protegidos automaticamente."

---

#### Caso 2: Situações Delicadas (Divórcio Conflituoso)

**Cenário:**
- Usuário divorciado não quer ver ex-cônjuge na árvore

**Solução:**

**Opção de Ocultação:**
- Card do membro: Menu → "Ocultar da visualização"
- Membro continua no banco de dados
- Parentescos continuam calculados
- Na árvore: aparece como "👤 Membro oculto"
- Apenas usuário que ocultou não vê; outros usuários veem normalmente

**Relacionamento Inativo:**
- Casamento marcado como `ativo=false`
- Data de fim registrada
- Não sugere subfamília

---

#### Caso 3: Segredo Familiar (Filho não reconhecido)

**Cenário:**
- Existe filho que apenas alguns membros sabem

**Solução:**

**Níveis de Visibilidade:**
- Membro marcado como "Confidencial"
- Configuração: "Visível apenas para:"
  - [X] Você
  - [X] João Silva
  - [X] Maria Santos
  - [ ] Outros membros

**Implementação:**
- Row Level Security no Supabase
- Queries filtram baseado em permissões
- Parentescos calculados apenas para quem tem acesso

---

## 19. INTEGRAÇÕES FUTURAS

### 19.1 Integração com Redes Sociais

**Facebook:**
- Importar árvore genealógica do Facebook (se ativada)
- Sincronizar fotos de perfil automaticamente
- Compartilhar conquistas no feed

**Instagram:**
- Exportar árvore como Story interativo
- Template especial "Minha Árvore Genealógica"
- Hashtag #RaízesVivas

**WhatsApp:**
- Criar grupo da família automaticamente
- Bot que anuncia aniversários no grupo
- Compartilhar atualizações da árvore

---

### 19.2 Integração com Serviços de Genealogia

**FamilySearch:**
- Importar dados do FamilySearch
- Exportar para FamilySearch
- Sincronização bidirecional

**MyHeritage:**
- Importar árvores existentes
- Comparar e mesclar dados
- Sugerir possíveis parentes

**Ancestry.com:**
- Importar pesquisas de DNA
- Sugerir conexões baseadas em DNA
- Importar documentos históricos

---

### 19.3 Integração com Documentos

**Google Drive:**
- Backup automático para Drive
- Armazenar certidões e documentos
- Galeria de fotos antigas

**Dropbox:**
- Alternativa ao Google Drive
- Mesmas funcionalidades

**Scanner de Documentos:**
- Integração com apps de scanner
- OCR para extrair dados de certidões
- Sugestão automática de dados extraídos

---

### 19.4 Integração com IA

**GPT para Análise Genealógica:**
- "Conte a história da minha família" → GPT gera narrativa
- "Quem é meu parente mais distante?" → GPT analisa e responde
- "Sugira quem eu deveria adicionar" → GPT analisa gaps

**Reconhecimento Facial:**
- Upload de foto antiga → IA tenta identificar quem são as pessoas
- Sugestão: "Esta pessoa parece com João Silva. É ele?"
- Agrupamento de fotos por pessoa

**Geração de Relatórios:**
- "Gere relatório da Família Silva" → IA cria documento formatado
- Inclui estatísticas, curiosidades, árvore visual
- Estilo personalizável (formal, casual, narrativo)

---

## 20. ACESSIBILIDADE

### 20.1 Requisitos de Acessibilidade

**Suporte a TalkBack (Leitores de Tela):**
- Todos os elementos com `contentDescription`
- Navegação por toque acessível
- Anúncios contextuais ao navegar

**Contraste de Cores:**
- Ratio mínimo: 4.5:1 (texto normal)
- Ratio mínimo: 3:1 (texto grande)
- Modo alto contraste disponível

**Tamanho de Fonte:**
- Respeitar configurações do sistema
- Escala de 100% a 200%
- Layout responsivo a mudanças de fonte

**Navegação por Teclado:**
- Todos os botões acessíveis via Tab
- Atalhos de teclado para ações comuns
- Indicação visual de foco

**Modo Daltônico:**
- Alternativas ao uso apenas de cores
- Ícones + cores
- Padrões em vez de cores quando crítico

---

### 20.2 Simplificação para Idosos

**Modo Simplificado:**
- Toggle em Configurações: "Modo Simples"
- Quando ativado:
  - Fontes maiores (125% padrão)
  - Botões maiores (48dp mínimo)
  - Menos opções por tela
  - Tutoriais mais frequentes
  - Assistente de voz opcional

**Assistente de Voz:**
- "Diga o nome do membro que deseja adicionar"
- "Para quem ele/ela é filho/filha?"
- Confirmação por voz antes de salvar

**Modo Leitura:**
- Árvore em lista simples
- Texto grande, sem ícones decorativos
- Apenas informações essenciais

---

## 21. LOCALIZAÇÃO E INTERNACIONALIZAÇÃO

### 21.1 Idiomas Suportados (Fase 1)

**Português (Brasil):**
- Idioma padrão
- Termos de parentesco brasileiros
- Formatos de data BR (DD/MM/AAAA)

**Português (Portugal):**
- Variações de vocabulário
- Formatos de data PT
- Termos de parentesco portugueses

---

### 21.2 Idiomas Futuros

**Espanhol:**
- Termos de parentesco em espanhol
- Suporte a nomes com duplo sobrenome
- Formatos de data (DD/MM/AAAA)

**Inglês:**
- Termos de parentesco em inglês
- Formatos de data (MM/DD/YYYY)
- Sistema imperial (opcional)

---

### 21.3 Adaptações Culturais

**Estruturas Familiares Diferentes:**
- Culturas com mais de 2 pais (poliamorismo)
- Culturas matriarcais vs patriarcais
- Sistemas de nomeação diferentes (islâmico, chinês)

**Eventos Religiosos:**
- Batismo, bar mitzvah, etc.
- Casamento religioso vs civil
- Rituais de passagem

---

## 22. MÉTRICAS E ANALYTICS

### 22.1 Eventos a Rastrear

**Eventos de Engajamento:**
- app_opened
- familia_created
- membro_added
- subfamilia_created
- arvore_visualized
- floresta_visualized
- conquista_unlocked
- membro_exported

**Eventos de Conversão (se premium):**
- trial_started
- premium_purchased
- subscription_renewed
- subscription_cancelled

**Eventos de Retenção:**
- daily_active_user
- weekly_active_user
- monthly_active_user
- session_duration
- features_used

---

### 22.2 Métricas de Produto

**Engajamento:**
- DAU/MAU ratio
- Tempo médio por sessão
- Frequência de uso
- Profundidade de uso (features utilizadas)

**Qualidade dos Dados:**
- % membros com foto
- % membros com dados completos
- Média de membros por família
- Média de gerações por família

**Performance:**
- Tempo de carregamento médio
- Taxa de crash
- Taxa de erro de sincronização
- Uso de memória/CPU

---

### 22.3 Dashboards

**Dashboard Executivo:**
- Usuários ativos (D/W/M)
- Taxa de retenção
- Receita (se premium)
- NPS (Net Promoter Score)

**Dashboard de Produto:**
- Features mais/menos usadas
- Fluxos de abandono
- Tempo em cada tela
- Heatmaps de interação

**Dashboard de Qualidade:**
- Crash-free rate
- Performance médio
- Bugs reportados
- Satisfação do usuário

---

## 23. ESTRATÉGIA DE LANÇAMENTO

### 23.1 Soft Launch (Beta Fechado)

**Objetivo:**
- Testar com 100 usuários reais
- Coletar feedback qualitativo
- Identificar bugs críticos

**Duração:** 4 semanas

**Critérios de Sucesso:**
- 80% dos usuários adicionam 5+ membros
- NPS > 30
- Crash-free rate > 98%
- Bugs críticos: 0

---

### 23.2 Beta Aberto

**Objetivo:**
- Escalar para 1000 usuários
- Testar carga no servidor
- Validar modelo de negócio

**Duração:** 8 semanas

**Ações:**
- Divulgação em grupos de genealogia
- Posts em redes sociais
- Parcerias com influenciadores de história familiar

**Critérios de Sucesso:**
- Crescimento orgânico > 30%
- Retenção D7 > 40%
- Avaliação na loja > 4.5

---

### 23.3 Lançamento Global

**Objetivo:**
- Disponibilizar para todos
- Atingir 10k usuários em 3 meses

**Estratégia de Marketing:**
- ASO (App Store Optimization)
- Anúncios em redes sociais
- Parcerias com sites de genealogia
- Press release
- Influencer marketing

**Promoção de Lançamento:**
- 50% de desconto no premium (primeiro mês)
- Recursos premium grátis por 30 dias
- Concurso: "Maior árvore genealógica"

---

## 24. SUPORTE E COMUNIDADE

### 24.1 Central de Ajuda

**Estrutura:**
- FAQ (Perguntas Frequentes)
- Tutoriais em vídeo
- Guias passo a passo
- Glossário de parentesco
- Troubleshooting

**Tópicos Principais:**
- "Como adicionar meu primeiro membro?"
- "O que são subfamílias?"
- "Como calcular parentesco?"
- "Como exportar minha árvore?"
- "Como convidar familiares?"

---

### 24.2 Suporte ao Usuário

**Canais:**
- In-app chat (suporte via chatbot + humano)
- Email: suporte@raizesvivas.com
- WhatsApp: (71) 9XXXX-XXXX
- Redes sociais: @raizesvivas

**SLA:**
- Resposta inicial: < 24h
- Resolução de bugs críticos: < 48h
- Resolução de problemas menores: < 7 dias

---

### 24.3 Comunidade

**Forum Online:**
- Espaço para usuários trocarem dicas
- Seções:
  - Dúvidas gerais
  - Pesquisa genealógica
  - Compartilhe sua árvore
  - Sugestões de features
  - Histórias familiares

**Grupos no Facebook:**
- Grupo oficial "Raízes Vivas - Genealogia"
- Grupos regionais (por estado/país)
- Grupo de power users

**Eventos:**
- Webinars mensais: "Como pesquisar ancestrais"
- Encontros virtuais de famílias
- Concursos temáticos (ex: "Árvore mais antiga")

---

## 25. PLANO DE CONTINGÊNCIA

### 25.1 Cenário: Perda de Dados

**Prevenção:**
- Backups diários automatizados
- Replicação geográfica (Supabase)
- Backups locais no dispositivo
- Exportação regular incentivada

**Resposta:**
1. Identificar extensão da perda
2. Restaurar do backup mais recente
3. Notificar usuários afetados
4. Oferecer suporte prioritário
5. Post-mortem público e transparente

---

### 25.2 Cenário: Bug Crítico em Produção

**Identificação:**
- Monitoramento 24/7 (Firebase Crashlytics)
- Alertas automáticos para crash rate > 5%

**Resposta:**
1. Rollback para versão anterior (se possível)
2. Hotfix emergencial
3. Teste em ambiente de staging
4. Deploy de correção
5. Comunicação com usuários

**Comunicação:**
- In-app: Banner informativo
- Email: Apenas se afetou dados do usuário
- Redes sociais: Status atualizado

---

### 25.3 Cenário: Sobrecarga de Servidor

**Prevenção:**
- Auto-scaling no Supabase
- CDN para assets estáticos
- Cache agressivo
- Rate limiting

**Resposta:**
1. Escalar recursos imediatamente
2. Ativar modo de degradação graciosa
   - Desabilitar features não essenciais
   - Modo somente leitura temporário
3. Comunicar transparência
4. Investigar causa raiz

---

## 26. ROADMAP DE LONGO PRAZO (1-2 anos)

### 26.1 Ano 1

**Q1:**
- ✅ Lançamento do MVP
- ✅ Subfamílias
- ✅ Floresta interativa
- ✅ Gamificação básica

**Q2:**
- Exportação avançada
- Timeline de eventos
- Modo offline completo
- Compartilhamento colaborativo

**Q3:**
- Árvore 3D (premium)
- Integração com redes sociais
- Reconhecimento facial (IA)
- Suporte a múltiplos idiomas

**Q4:**
- Integração com serviços de genealogia
- Análise de DNA (parceria)
- Relatórios automáticos (IA)
- Marketplace de temas

---

### 26.2 Ano 2

**Q1:**
- App para iOS
- Versão Web
- Sincronização multi-plataforma
- Colaboração em tempo real

**Q2:**
- Realidade aumentada (AR)
  - Visualizar árvore em 3D no ambiente
  - Fotos dos membros flutuando
- Assistente genealógico por voz
- Integração com smart displays

**Q3:**
- Modo "Árvore Viva"
  - Livestream de eventos familiares
  - Atualização em tempo real
- Gamificação avançada
  - Desafios semanais
  - Competições entre famílias

**Q4:**
- Blockchain para certificação
  - Árvores certificadas imutáveis
  - NFTs de membros históricos
- Legado digital
  - Mensagens para o futuro
  - Cápsulas do tempo digitais

---

## 27. CONSIDERAÇÕES FINAIS

### 27.1 Diferenciais do App

1. **Metáfora Visual Única**: Uso de elementos botânicos para representar gerações
2. **Contexto Múltiplo**: Mesmo membro tem papéis diferentes em famílias diferentes
3. **Automação Inteligente**: Sugestões proativas de subfamílias
4. **Gamificação Natural**: Conquistas motivam completar a árvore
5. **Floresta Interativa**: Visualização única de múltiplas famílias conectadas

### 27.2 Desafios Técnicos Principais

1. **Algoritmo de Parentesco**: Complexidade aumenta exponencialmente
2. **Performance de Renderização**: Árvores grandes podem travar
3. **Sincronização Conflitos**: Múltiplos dispositivos editando simultaneamente
4. **UX de Subfamílias**: Conceito pode confundir usuários inicialmente
5. **Gestão de Memória**: Muitas fotos podem consumir muita RAM

### 27.3 Métricas de Sucesso

**Engajamento:**
- Tempo médio na sessão: > 10 minutos
- Frequência de uso: 3x por semana
- Taxa de retenção D7: > 40%
- Taxa de retenção D30: > 20%

**Qualidade dos Dados:**
- Membros com dados completos: > 60%
- Membros com foto: > 40%
- Famílias com 3+ gerações: > 30%

**Conversão (se premium):**
- Teste gratuito iniciado: > 20%
- Conversão para pago: > 5%
- Churn mensal: < 10%

---

## 28. DOCUMENTAÇÃO ADICIONAL NECESSÁRIA

Para implementação completa, criar:

1. **Documento de Design Visual**
   - Paleta de cores completa
   - Tipografia
   - Iconografia
   - Componentes UI detalhados
   - Animações e transições

2. **Documento de API Supabase**
   - Schema completo SQL
   - Row Level Security policies
   - Edge Functions (se necessário)
   - Triggers e procedures

3. **Guia de Estilo de Código**
   - Convenções Kotlin
   - Estrutura de Composables
   - Patterns de ViewModel
   - Tratamento de erros

4. **Manual de Testes**
   - Casos de teste unitários
   - Casos de teste de integração
   - Casos de teste E2E
   - Cenários de teste manual

5. **Documentação de Algoritmo de Parentesco**
   - Pseudocódigo detalhado
   - Árvores de decisão
   - Casos extremos e edge cases
   - Tabelas de referência de graus

---

## CONCLUSÃO

Este plano de desenvolvimento completo para o **Raízes Vivas** fornece uma base sólida para implementação do aplicativo Android de árvore genealógica gamificada usando Kotlin + Supabase.

### Próximos Passos Imediatos:

1. **Validar conceito com usuários** (entrevistas, protótipo)
2. **Criar design visual** (UI/UX detalhado)
3. **Configurar ambiente de desenvolvimento** (projeto Kotlin, Supabase)
4. **Implementar MVP** seguindo Fase 1 (8 semanas)
5. **Testar com beta fechado** (100 usuários)
6. **Iterar baseado em feedback**
7. **Lançar beta aberto**

### Diferenciais Competitivos:

- ✨ Metáfora visual única (elementos de árvore)
- 🌳 Vista de floresta interativa
- 🎮 Gamificação natural e motivadora
- 🤖 Automação inteligente (subfamílias, parentescos)
- 📱 Experiência mobile-first
- 🔄 Sincronização perfeita offline/online

**Raízes Vivas** tem potencial para se tornar referência em aplicativos de genealogia, combinando utilidade prática com experiência visual única e engajamento através de gamificação.

---

**Documento criado em: 18 Outubro de 2025  
**Versão:** 1.0  
**Autor:** Plano de Desenvolvimento Completo  
**Última atualização:** 18/10/2025