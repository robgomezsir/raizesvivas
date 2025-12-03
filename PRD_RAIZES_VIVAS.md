# PRD - Raízes Vivas
## Product Requirements Document

**Versão:** 1.0  
**Data:** 2025-01-27  
**Status:** Em Produção

---

## 📋 Sumário Executivo

**Raízes Vivas** é um aplicativo mobile Android para construção colaborativa de árvores genealógicas familiares. A plataforma permite que famílias inteiras colaborem na construção e manutenção de sua história familiar, com recursos de gamificação, comunicação, álbum de fotos e visualizações interativas da árvore genealógica.

### Objetivo do Produto
Facilitar a construção colaborativa e preservação da história genealógica familiar através de uma plataforma moderna, gamificada e intuitiva que incentiva a participação de todos os membros da família.

### Público-Alvo
- Famílias que desejam construir e preservar sua árvore genealógica
- Membros de famílias grandes que precisam colaborar na manutenção de dados genealógicos
- Usuários interessados em descobrir e documentar suas raízes familiares

---

## 🎯 Objetivos de Negócio

1. **Preservação da História Familiar**: Facilitar a documentação e preservação da história genealógica de famílias
2. **Engajamento Familiar**: Incentivar a participação de todos os membros através de gamificação e recursos sociais
3. **Colaboração**: Permitir que múltiplos membros da família contribuam com informações e fotos
4. **Acessibilidade**: Tornar a genealogia acessível a pessoas sem conhecimento técnico

---

## 👥 Personas

### Persona 1: Maria (60 anos) - Matriarca da Família
- **Necessidades**: Documentar a história da família, compartilhar fotos antigas, manter contato com parentes distantes
- **Habilidades Técnicas**: Básicas (usa smartphone para WhatsApp)
- **Motivações**: Preservar memórias para futuras gerações

### Persona 2: João (35 anos) - Filho Interessado em Genealogia
- **Necessidades**: Organizar informações genealógicas, visualizar árvore completa, descobrir parentescos
- **Habilidades Técnicas**: Intermediárias (usa apps regularmente)
- **Motivações**: Entender melhor suas raízes e conectar com a família

### Persona 3: Ana (25 anos) - Neto Digital
- **Necessidades**: Interface moderna, gamificação, compartilhamento social, notificações
- **Habilidades Técnicas**: Avançadas (nativo digital)
- **Motivações**: Engajamento através de recursos modernos e interativos

---

## 🏗️ Arquitetura do Sistema

### Stack Tecnológico

#### Frontend (Android)
- **Linguagem**: Kotlin
- **UI Framework**: Jetpack Compose
- **Arquitetura**: MVVM (Model-View-ViewModel)
- **Injeção de Dependência**: Hilt (Dagger)
- **Navegação**: Navigation Compose
- **Banco Local**: Room Database
- **Gerenciamento de Estado**: ViewModel + StateFlow/Flow
- **Carregamento de Imagens**: Coil
- **Animações**: Lottie

#### Backend (Firebase)
- **Autenticação**: Firebase Authentication
- **Banco de Dados**: Cloud Firestore
- **Armazenamento**: Firebase Storage
- **Cloud Functions**: Node.js/TypeScript
- **Notificações**: Firebase Cloud Messaging (FCM)
- **Analytics**: Firebase Analytics

#### Infraestrutura
- **Regras de Segurança**: Firestore Security Rules
- **Storage Rules**: Firebase Storage Rules
- **Email**: Nodemailer (via Cloud Functions)

---

## 📱 Funcionalidades Principais

### 1. Autenticação e Gerenciamento de Usuários

#### 1.1. Autenticação
- **Login**: Email e senha
- **Cadastro**: Com validação de convite obrigatória
- **Recuperação de Senha**: Via email
- **Autenticação Biométrica**: Suporte a biometria (opcional)
- **Persistência de Sessão**: Login automático

#### 1.2. Perfis de Usuário
- **Informações Básicas**: Nome, email, foto de perfil
- **Vínculo com Pessoa**: Associar usuário a uma pessoa na árvore genealógica
- **Níveis de Permissão**:
  - **Familiar**: Acesso básico, pode visualizar e sugerir edições
  - **Administrador**: Pode aprovar edições, gerenciar pessoas e fotos
  - **Administrador Sênior**: Acesso total, pode gerenciar usuários e configurações

#### 1.3. Onboarding
- **Primeiro Acesso**: Tutorial e configuração inicial
- **Seleção de Família Zero**: Definir o casal raiz da árvore genealógica
- **Vínculo com Pessoa**: Associar usuário a uma pessoa existente ou criar nova

---

### 2. Árvore Genealógica

#### 2.1. Gerenciamento de Pessoas
- **Cadastro de Pessoas**:
  - Nome completo, apelido
  - Data e local de nascimento
  - Data e local de falecimento (opcional)
  - Gênero, estado civil
  - Profissão, biografia
  - Telefone
  - Foto de perfil
  - Tipo de filiação (biológica, adotiva)
  - Tipo de nascimento (normal, gemelar)
  - Data de casamento

- **Relacionamentos**:
  - Pai e mãe
  - Cônjuge atual
  - Ex-cônjuges
  - Filhos
  - Famílias personalizadas

- **Metadados**:
  - Criado por, data de criação
  - Modificado por, data de modificação
  - Versão (controle de conflitos)
  - Aprovação (para edições pendentes)
  - Distância até Família Zero

#### 2.2. Visualizações da Árvore

##### 2.2.1. Árvore Hierárquica
- Visualização tradicional de árvore genealógica
- Layout vertical com gerações
- Navegação por scroll e zoom
- Cards clicáveis para cada pessoa
- Indicadores visuais de relacionamentos

##### 2.2.2. Árvore Radial
- Visualização circular com Família Zero no centro
- Anéis concêntricos representando gerações
- Conexões visuais entre parentes
- Zoom e rotação interativos

##### 2.2.3. Mapa Mental
- Visualização em formato de mapa mental
- Foco em uma pessoa central
- Expansão de ramos familiares
- Navegação intuitiva

##### 2.2.4. Lista Hierárquica
- Lista organizada por gerações
- Agrupamento por família
- Busca e filtros
- Navegação rápida

#### 2.3. Detalhes de Pessoa
- **Informações Completas**: Todos os dados cadastrados
- **Galeria de Fotos**: Fotos associadas à pessoa
- **Relacionamentos**: Visualização de parentes diretos
- **Linha do Tempo**: Eventos importantes (nascimento, casamento, falecimento)
- **Edição**: Sugestão de edições (para não-admins) ou edição direta (para admins)

#### 2.4. Cálculo de Parentesco
- **Algoritmo de Parentesco**: Cálculo automático de grau de parentesco
- **Exibição de Relacionamentos**: "Primo de 2º grau", "Tio-avô", etc.
- **Distância até Família Zero**: Cálculo de gerações

---

### 3. Sistema de Gamificação

#### 3.1. Sistema de Níveis e XP
- **Níveis**: Progressão de nível baseada em XP total
- **XP por Ações**:
  - Adicionar pessoa: +50 XP
  - Adicionar foto: +25 XP
  - Completar perfil: +30 XP
  - Aprovar edição: +20 XP
  - Comentar em foto: +5 XP
  - Enviar recado: +10 XP

- **XP Atual e Próximo Nível**: Exibição de progresso
- **Barra de Progresso**: Visualização do XP necessário para próximo nível

#### 3.2. Conquistas (Achievements)
- **Sistema de Conquistas**:
  - Conquistas disponíveis (definidas por admins)
  - Progresso individual por conquista
  - Desbloqueio automático ao atingir objetivos
  - Notificações ao desbloquear

- **Tipos de Conquistas**:
  - **Quantidade**: "Adicione 10 pessoas", "Adicione 50 fotos"
  - **Completude**: "Complete 20 perfis", "Adicione 5 biografias"
  - **Social**: "Envie 10 recados", "Comente em 20 fotos"
  - **Tempo**: "Use o app por 30 dias consecutivos"
  - **Especiais**: "Complete a árvore até 5 gerações"

- **Progresso de Conquistas**:
  - Rastreamento de progresso (ex: 7/10 pessoas adicionadas)
  - Níveis de conquistas (Bronze, Prata, Ouro)
  - Pontuação total de conquistas

#### 3.3. Ranking
- **Ranking Global**: Lista de usuários ordenados por XP total
- **Posição no Ranking**: Exibição da posição do usuário
- **Atualização em Tempo Real**: Sincronização automática
- **Filtros**: Ranking por período (semanal, mensal, total)

---

### 4. Álbum de Fotos da Família

#### 4.1. Gerenciamento de Fotos
- **Upload de Fotos**: Upload para Firebase Storage
- **Compressão Automática**: Otimização de imagens antes do upload
- **Associação com Pessoas**: Vincular fotos a pessoas específicas
- **Descrição**: Adicionar descrição opcional às fotos
- **Ordem**: Definir ordem de exibição
- **Metadados**: Data de criação, criador, família associada

#### 4.2. Visualização
- **Galeria**: Visualização em grid ou lista
- **Visualização Individual**: Foto em tela cheia
- **Zoom**: Zoom em fotos
- **Navegação**: Swipe entre fotos
- **Filtros**: Por pessoa, por família, por data

#### 4.3. Interações Sociais
- **Comentários**: Comentar em fotos
- **Apoios Familiares**: Sistema de "curtidas" (apoios)
- **Soft Delete**: Usuários podem "deletar" seus próprios comentários (soft delete)
- **Moderação**: Admins podem editar/deletar qualquer comentário

#### 4.4. Permissões
- **Visualização**: Todos os usuários autenticados podem ver todas as fotos
- **Upload**: Apenas admins podem fazer upload
- **Interação**: Todos podem comentar e dar apoio

---

### 5. Sistema de Comunicação

#### 5.1. Chat Privado
- **Conversas 1-para-1**: Chat entre dois usuários
- **Lista de Contatos**: Lista de todos os usuários da família
- **Mensagens em Tempo Real**: Sincronização via Firestore
- **Status de Leitura**: Indicador de mensagem lida/não lida
- **Notificações**: Push notifications para novas mensagens
- **Histórico**: Persistência de histórico de mensagens

#### 5.2. Mural de Recados
- **Recados Públicos**: Mensagens visíveis para toda a família
- **Recados Direcionados**: Mensagens direcionadas a usuários específicos
- **Apoios Familiares**: Sistema de "curtidas" em recados
- **Edição e Exclusão**: Autor pode editar/excluir seus recados
- **Filtros**: Por autor, por data, por direcionamento

---

### 6. Sistema de Convites

#### 6.1. Pedido de Convite
- **Solicitação**: Usuários não cadastrados podem solicitar convite
- **Informações**: Nome, email, telefone (opcional)
- **Status**: Pendente, Aprovado, Rejeitado

#### 6.2. Gerenciamento de Convites (Admins)
- **Lista de Pedidos**: Visualizar todos os pedidos pendentes
- **Aprovação/Rejeição**: Aprovar ou rejeitar pedidos
- **Criação de Convite**: Criar convite diretamente para um email
- **Vínculo com Pessoa**: Associar convite a uma pessoa na árvore
- **Expiração**: Convites expiram em 7 dias

#### 6.3. Aceitação de Convites
- **Lista de Convites**: Usuário autenticado vê seus convites pendentes
- **Aceitação**: Aceitar convite e vincular-se à pessoa
- **Notificações**: Email automático ao criar convite (via Cloud Function)

#### 6.4. Validação de Cadastro
- **Bloqueio de Cadastro**: Apenas usuários com convite válido podem se cadastrar
- **Cloud Function**: Validação via `beforeUserCreated` trigger
- **Usuários Existentes**: Usuários já cadastrados podem recriar conta

---

### 7. Sistema de Edições Pendentes

#### 7.1. Sugestão de Edições
- **Para Não-Admins**: Sugerir edições em pessoas existentes
- **Campos Editáveis**: Qualquer campo pode ser sugerido para edição
- **Justificativa**: Opcional, explicar o motivo da edição
- **Status**: Pendente, Aprovado, Rejeitado

#### 7.2. Aprovação de Edições (Admins)
- **Lista de Pendências**: Visualizar todas as edições pendentes
- **Comparação**: Ver dados atuais vs. dados sugeridos
- **Aprovação**: Aprovar e aplicar edição
- **Rejeição**: Rejeitar com motivo opcional
- **Notificações**: Notificar usuário sobre aprovação/rejeição

#### 7.3. Histórico de Edições
- **Registro**: Todas as edições são registradas no histórico
- **Auditoria**: Rastreamento de quem editou e quando
- **Reversão**: Possibilidade de reverter edições (futuro)

---

### 8. Sistema de Notificações

#### 8.1. Tipos de Notificações
- **Mensagens**: Nova mensagem de chat
- **Edições**: Edição aprovada/rejeitada
- **Conquistas**: Nova conquista desbloqueada
- **Aniversários**: Lembrete de aniversário
- **Convites**: Novo convite recebido
- **Recados**: Novo recado direcionado
- **Comentários**: Novo comentário em foto (futuro)

#### 8.2. Canais de Notificação
- **Push Notifications**: Via FCM
- **In-App**: Drawer de notificações dentro do app
- **Email**: Para eventos importantes (via Cloud Functions)

#### 8.3. Gerenciamento
- **Marcar como Lida**: Usuário pode marcar notificações como lidas
- **Exclusão**: Deletar notificações antigas
- **Filtros**: Filtrar por tipo, por status (lida/não lida)
- **Limpeza Automática**: Notificações lidas antigas são removidas automaticamente

---

### 9. Sistema de Amigos da Família

#### 9.1. Cadastro de Amigos
- **Informações**: Nome, telefone (opcional)
- **Vínculos**: Associar amigos a familiares específicos
- **Colaborativo**: Todos os usuários podem adicionar/editar amigos
- **Metadados**: Criado por, data de criação/modificação

#### 9.2. Visualização
- **Lista de Amigos**: Lista de todos os amigos cadastrados
- **Busca**: Buscar por nome
- **Filtros**: Por familiar vinculado

---

### 10. Família Zero e Famílias Personalizadas

#### 10.1. Família Zero
- **Definição**: Casal raiz da árvore genealógica
- **Seleção**: Durante onboarding ou configurações
- **Bloqueio**: Família Zero fica bloqueada após definição
- **Referência**: Todas as pessoas têm distância calculada até Família Zero

#### 10.2. Famílias Personalizadas
- **Criação**: Admins podem criar famílias personalizadas
- **Membros**: Associar cônjuges principais e secundários
- **Uso**: Organizar subfamílias ou ramos específicos
- **Visualização**: Filtrar árvore por família personalizada

---

### 11. Sistema de Duplicatas

#### 11.1. Detecção Automática
- **Algoritmo**: Detecta possíveis duplicatas baseado em nome e datas
- **Sugestões**: Lista de possíveis duplicatas para revisão
- **Confiança**: Score de similaridade

#### 11.2. Resolução
- **Merge Manual**: Admins podem mesclar duplicatas
- **Escolha de Dados**: Selecionar quais dados manter
- **Histórico**: Registro de merges realizados

---

### 12. Aniversários e Eventos

#### 12.1. Detecção de Aniversários
- **Cálculo Automático**: Identifica aniversários baseado em datas de nascimento
- **Worker em Background**: Verificação diária via WorkManager
- **Notificações**: Lembretes de aniversários próximos

#### 12.2. Eventos Especiais
- **Aniversários**: Exibição de aniversários do dia/mês
- **Falecimentos**: Lembretes de datas de falecimento (futuro)
- **Casamentos**: Lembretes de aniversários de casamento (futuro)

---

### 13. Sincronização e Offline

#### 13.1. Sincronização com Firestore
- **Tempo Real**: Sincronização automática via Firestore listeners
- **Cache Local**: Room Database para cache offline
- **Estratégia de Cache**: Cache de dados frequentemente acessados

#### 13.2. Modo Offline
- **Leitura Offline**: Visualizar dados em cache quando offline
- **Queue de Operações**: Operações de escrita são enfileiradas quando offline
- **Sincronização ao Voltar**: Sincronização automática ao voltar online

#### 13.3. Resolução de Conflitos
- **Controle de Versão**: Campo `versao` em documentos
- **Última Escrita Vence**: Estratégia simples de resolução
- **Notificações**: Alertar sobre conflitos quando necessário

---

### 14. Configurações e Preferências

#### 14.1. Preferências do Usuário
- **Tema**: Modo claro/escuro
- **Notificações**: Configurar tipos de notificações
- **Privacidade**: Configurações de privacidade (futuro)

#### 14.2. Configurações da Árvore
- **Ordem de Exibição**: Preferências de ordenação
- **Filtros Padrão**: Filtros salvos
- **Visualização Preferida**: Tipo de visualização padrão

---

### 15. Recursos Administrativos

#### 15.1. Gerenciamento de Usuários
- **Lista de Usuários**: Visualizar todos os usuários
- **Alteração de Permissões**: Promover/rebaixar usuários
- **Vínculos**: Gerenciar vínculos usuário-pessoa

#### 15.2. Gerenciamento de Conquistas
- **Criação de Conquistas**: Definir novas conquistas disponíveis
- **Edição**: Modificar conquistas existentes
- **Estatísticas**: Ver estatísticas de desbloqueios

#### 15.3. Sincronização de Relações
- **Worker Manual**: Trigger manual de sincronização de relações
- **Recalcular Distâncias**: Recalcular distâncias até Família Zero
- **Validação**: Validar integridade dos dados

---

## 🔒 Segurança e Privacidade

### Regras de Segurança (Firestore)

#### Coleções Públicas (Todos Autenticados)
- `conquistasDisponiveis`: Leitura pública
- `fotos_album`: Leitura pública, escrita apenas admins
- `amigos`: Leitura e escrita públicas
- `recados`: Leitura e escrita públicas

#### Coleções Privadas (Apenas Dono)
- `usuarios/{userId}/conquistasProgresso`: Apenas o próprio usuário
- `usuarios/{userId}/perfilGamificacao`: Perfil público, outros dados privados
- `usuarios/{userId}/notificacoes`: Apenas o próprio usuário

#### Coleções Administrativas
- `access_requests`: Leitura apenas admin sênior
- `invites`: Leitura para admins e convidado
- `pending_edits`: Leitura para admins e autor
- `edicoes_historico`: Apenas admins

### Validações
- **Validação de Dados**: Regras do Firestore validam estrutura de dados
- **Queries Eficientes**: Limitação de queries sem filtros/ordenação
- **Rate Limiting**: Proteção contra abuso (via Cloud Functions)

### Privacidade
- **Dados Sensíveis**: Telefones e emails são privados
- **Fotos**: Todas as fotos são públicas para a família (todos autenticados)
- **Biografia**: Informações pessoais visíveis apenas para família

---

## 📊 Modelos de Dados Principais

### Pessoa
```kotlin
data class Pessoa(
    val id: String,
    val nome: String,
    val apelido: String?,
    val dataNascimento: Date?,
    val dataFalecimento: Date?,
    val localNascimento: String?,
    val localResidencia: String?,
    val profissao: String?,
    val biografia: String?,
    val telefone: String?,
    val estadoCivil: EstadoCivil?,
    val genero: Genero?,
    val pai: String?,
    val mae: String?,
    val conjugeAtual: String?,
    val exConjuges: List<String>,
    val filhos: List<String>,
    val fotoUrl: String?,
    val criadoPor: String,
    val criadoEm: Date,
    val modificadoPor: String,
    val modificadoEm: Date,
    val aprovado: Boolean,
    val versao: Int,
    val ehFamiliaZero: Boolean,
    val distanciaFamiliaZero: Int,
    val familias: List<String>,
    val tipoFiliacao: TipoFiliacao?,
    val tipoNascimento: TipoNascimento?,
    val grupoGemelarId: String?,
    val ordemNascimento: Int?,
    val dataCasamento: Date?
)
```

### Usuario
```kotlin
data class Usuario(
    val id: String, // Firebase Auth UID
    val nome: String,
    val email: String,
    val fotoUrl: String?,
    val posicaoRanking: Int?,
    val pessoaVinculada: String?,
    val ehAdministrador: Boolean,
    val ehAdministradorSenior: Boolean,
    val familiaZeroPai: String?,
    val familiaZeroMae: String?,
    val primeiroAcesso: Boolean,
    val criadoEm: Date
)
```

### Conquista
```kotlin
data class ConquistaDisponivel(
    val id: String,
    val titulo: String,
    val descricao: String,
    val tipo: TipoConquista,
    val objetivo: Int,
    val nivel: NivelConquista,
    val icone: String?,
    val ativa: Boolean
)

data class ConquistaProgresso(
    val conquistaId: String,
    val progresso: Int,
    val progressoTotal: Int,
    val concluida: Boolean,
    val desbloqueadaEm: Date?,
    val nivel: Int?,
    val pontuacaoTotal: Int?
)
```

### PerfilGamificacao
```kotlin
data class PerfilGamificacao(
    val nivel: Int,
    val xpTotal: Int,
    val xpAtual: Int,
    val xpProximoNivel: Int,
    val conquistasDesbloqueadas: Int,
    val totalConquistas: Int,
    val atualizadoEm: Date?
)
```

---

## 🎨 Design e UX

### Princípios de Design
1. **Simplicidade**: Interface limpa e intuitiva
2. **Acessibilidade**: Suporte a leitores de tela e contraste adequado
3. **Consistência**: Componentes reutilizáveis e padrões consistentes
4. **Feedback Visual**: Animações e transições suaves
5. **Hierarquia Visual**: Uso de elevação e cores para hierarquia

### Sistema de Design

#### Cores
- **Primária**: Verde-floresta (herança)
- **Secundária**: Terracota (crescimento)
- **Terciária**: Ametista (legado)
- **Semânticas**: 
  - Heritage (marrom-madeira)
  - Growth (verde-vida)
  - Legacy (dourado)
  - Connection (azul-céu)

#### Tipografia
- **Títulos**: Playfair Display (elegante, serifada)
- **Corpo**: Inter (moderna, sans-serif)

#### Componentes
- **Cards**: `RaizesVivasCard` com elevações consistentes
- **Botões**: `GradientButton` para ações primárias
- **Avatares**: `PersonAvatar` com gradiente único por pessoa
- **Estados Vazios**: `EmptyState` com mensagens amigáveis
- **Animações**: `AnimatedCard` para listas, `ShimmerCard` para loading

---

## 📈 Métricas e Analytics

### Métricas de Engajamento
- **Usuários Ativos Diários (DAU)**
- **Usuários Ativos Mensais (MAU)**
- **Taxa de Retenção**: D1, D7, D30
- **Tempo Médio de Sessão**
- **Ações por Sessão**: Pessoas adicionadas, fotos enviadas, etc.

### Métricas de Produto
- **Pessoas Cadastradas**: Total e por período
- **Fotos Enviadas**: Total e por período
- **Conquistas Desbloqueadas**: Taxa de desbloqueio
- **Edições Aprovadas**: Taxa de aprovação
- **Mensagens Enviadas**: Volume de comunicação

### Métricas Técnicas
- **Taxa de Erro**: Erros de sincronização, crashes
- **Performance**: Tempo de carregamento, latência
- **Uso de Storage**: Consumo de Firebase Storage
- **Uso de Firestore**: Leitura/escrita de documentos

---

## 🚀 Roadmap Futuro

### Fase 2 (Curto Prazo)
- [ ] Exportação de árvore genealógica (PDF, GEDCOM)
- [ ] Importação de dados (GEDCOM)
- [ ] Busca avançada de pessoas
- [ ] Filtros avançados na árvore
- [ ] Compartilhamento de fotos via link
- [ ] Modo de apresentação (slideshow de fotos)

### Fase 3 (Médio Prazo)
- [ ] App iOS
- [ ] Versão Web
- [ ] API pública para integrações
- [ ] Árvore genealógica colaborativa entre famílias
- [ ] DNA e testes genéticos (integração)
- [ ] Mapas de migração familiar

### Fase 4 (Longo Prazo)
- [ ] IA para sugestão de parentescos
- [ ] Reconhecimento facial em fotos
- [ ] Timeline interativa de eventos
- [ ] Histórias e memórias (texto longo)
- [ ] Integração com redes sociais
- [ ] Marketplace de serviços genealógicos

---

## 🐛 Limitações Conhecidas

1. **Apenas Android**: Não há versão iOS ou Web atualmente
2. **Idioma**: Apenas português brasileiro
3. **Escalabilidade**: Firestore pode ter limites de custo com muitas pessoas
4. **Offline**: Funcionalidades offline são limitadas
5. **Busca**: Busca de pessoas não é full-text search avançada

---

## 📝 Notas de Implementação

### Arquitetura
- **Clean Architecture**: Separação em camadas (data, domain, presentation)
- **Repository Pattern**: Abstração de fontes de dados
- **Use Cases**: Lógica de negócio isolada
- **Dependency Injection**: Hilt para injeção de dependências

### Performance
- **Lazy Loading**: Carregamento sob demanda de dados
- **Pagination**: Listas paginadas para grandes volumes
- **Image Optimization**: Compressão antes do upload
- **Cache Strategy**: Cache agressivo de dados estáticos

### Testes
- **Unit Tests**: Testes de use cases e utilitários
- **Integration Tests**: Testes de repositórios
- **UI Tests**: Testes de componentes Compose (futuro)

---

## 📞 Suporte e Contato

### Canais de Suporte
- **Email**: suporte@raizesvivas.com (exemplo)
- **Documentação**: Guia de componentes e boas práticas
- **FAQ**: Perguntas frequentes (futuro)

---

## 📄 Anexos

### A. Glossário
- **Família Zero**: Casal raiz da árvore genealógica
- **XP**: Pontos de experiência no sistema de gamificação
- **Conquista**: Achievement desbloqueável por ações
- **Edição Pendente**: Sugestão de alteração aguardando aprovação
- **Soft Delete**: Marcação de exclusão sem remoção física

### B. Referências
- Firebase Documentation
- Jetpack Compose Guidelines
- Material Design 3
- Genealogical Data Communication (GEDCOM) Standard

---

**Documento mantido por**: Equipe de Desenvolvimento Raízes Vivas  
**Última atualização**: 2025-01-27  
**Próxima revisão**: 2025-04-27

