<!-- 8f8ebd64-e59c-42b9-92fc-ed25f351bf4f c0965918-b3a8-4605-8f31-2cf36b9fd4a9 -->
# Plano de Desenvolvimento - Raízes Vivas

## Visão Geral

Desenvolvimento completo do aplicativo Android Raízes Vivas seguindo as 16 etapas documentadas nos prompts, garantindo fidelidade à arquitetura MVVM + Clean Architecture, integração com Firebase e testes contínuos.

## Fase 1: Fundação (Prompts 01-06) ✅ CONCLUÍDA

### ✅ Etapa 01: Setup Inicial do Projeto - CONCLUÍDA

**Status:** ✅ Completa

**Checklist:**
- [x] Criar projeto Android no Android Studio com package `com.raizesvivas.app`
- [x] Configurar build.gradle.kts (Project) com plugins necessários
- [x] Configurar build.gradle.kts (Module :app) com todas as dependências
- [x] Criar estrutura completa de pastas conforme prompt_01.txt
- [x] Configurar gradle.properties com otimizações
- [x] Criar RaizesVivasApplication.kt com @HiltAndroidApp
- [x] Atualizar AndroidManifest.xml com permissões e Application
- [x] Criar MainActivity.kt básica
- [x] Criar arquivos de tema: Color.kt, Theme.kt, Type.kt
- [x] Verificar compilação sem erros
- [x] Testar execução no emulador/device

**Arquivos criados:**
- build.gradle.kts (raiz e app/)
- RaizesVivasApplication.kt
- MainActivity.kt
- presentation/theme/Color.kt, Theme.kt, Type.kt
- Estrutura de pastas completa

---

### ✅ Etapa 02: Configuração Firebase - CONCLUÍDA

**Status:** ✅ Completa

**Checklist:**
- [x] Criar projeto no Firebase Console (ou usar existente)
- [x] Adicionar app Android ao Firebase
- [x] Baixar google-services.json e colocar em app/
- [x] Ativar Firebase Authentication (Email/Password)
- [x] Criar Firestore Database em modo produção
- [x] Ativar Cloud Storage
- [x] Criar collections manualmente no Firestore (users, people, familia_zero, invites, pending_edits, duplicates)
- [x] Aplicar Security Rules do Firestore (conforme prompt_02.txt)
- [x] Criar indexes do Firestore (via firestore.indexes.json)
- [x] Aplicar Storage Rules
- [x] Criar di/FirebaseModule.kt com providers
- [x] Criar data/remote/firebase/AuthService.kt
- [x] Verificar injeção Hilt funcionando
- [x] Testar conexão Firebase (logs no Logcat)

**Arquivos criados:**
- app/google-services.json
- firestore.indexes.json
- di/FirebaseModule.kt
- data/remote/firebase/AuthService.kt

---

### ✅ Etapa 03: Models de Domínio e Entities - CONCLUÍDA

**Status:** ✅ Completa

**Checklist:**
- [x] Criar domain/model/Pessoa.kt com todas propriedades e funções auxiliares
- [x] Criar domain/model/Usuario.kt
- [x] Criar domain/model/FamiliaZero.kt
- [x] Criar domain/model/Convite.kt com enum StatusConvite
- [x] Criar domain/model/EdicaoPendente.kt com enum StatusEdicao
- [x] Criar domain/model/ParentescoTipo.kt com todos os tipos de parentesco
- [x] Criar data/local/entities/PessoaEntity.kt com conversões toDomain/toEntity
- [x] Criar data/local/entities/UsuarioEntity.kt com conversões
- [x] Criar data/local/Converters.kt para Date, List<String>, Map
- [x] Verificar compilação sem erros
- [x] Testar conversões Entity ↔ Domain manualmente

**Arquivos criados:**
- domain/model/Pessoa.kt
- domain/model/Usuario.kt
- domain/model/FamiliaZero.kt
- domain/model/Convite.kt
- domain/model/EdicaoPendente.kt
- domain/model/ParentescoTipo.kt
- data/local/entities/PessoaEntity.kt
- data/local/entities/UsuarioEntity.kt
- data/local/Converters.kt

---

### ✅ Etapa 04: DAOs e Room Database - CONCLUÍDA

**Status:** ✅ Completa

**Checklist:**
- [x] Criar data/local/dao/PessoaDao.kt com todas queries
- [x] Criar data/local/dao/UsuarioDao.kt
- [x] Criar data/local/RaizesVivasDatabase.kt com @Database
- [x] Criar di/DatabaseModule.kt com providers do Room
- [x] Criar testes unitários: test/data/local/dao/PessoaDaoTest.kt
- [x] Executar testes e verificar passarem
- [x] Verificar injeção Hilt dos DAOs
- [x] Testar queries manualmente (inserir/buscar/deletar)

**Arquivos criados:**
- data/local/dao/PessoaDao.kt
- data/local/dao/UsuarioDao.kt
- data/local/RaizesVivasDatabase.kt
- di/DatabaseModule.kt
- test/data/local/dao/PessoaDaoTest.kt

---

### ✅ Etapa 05: Repositories e Services - CONCLUÍDA

**Status:** ✅ Completa

**Checklist:**
- [x] Criar data/remote/firebase/FirestoreService.kt com todos os métodos
- [x] Criar data/repository/PessoaRepository.kt com lógica de sincronização
- [x] Criar data/repository/FamiliaZeroRepository.kt
- [x] Criar data/repository/UsuarioRepository.kt
- [x] Testar sincronização Firestore → Room
- [x] Testar salvamento local → Firestore
- [x] Verificar logs de sincronização
- [x] Testar modo offline (desligar internet e usar cache)

**Arquivos criados:**
- data/remote/firebase/FirestoreService.kt
- data/repository/PessoaRepository.kt
- data/repository/FamiliaZeroRepository.kt
- data/repository/UsuarioRepository.kt

---

### ✅ Etapa 06: Telas de Autenticação - CONCLUÍDA

**Status:** ✅ Completa

**Checklist:**
- [x] Criar presentation/navigation/Screen.kt com todas rotas
- [x] Criar utils/ValidationUtils.kt com validações de email e senha
- [x] Criar presentation/screens/auth/LoginScreen.kt
- [x] Criar presentation/screens/auth/LoginViewModel.kt
- [x] Criar presentation/screens/auth/CadastroScreen.kt
- [x] Criar presentation/screens/auth/CadastroViewModel.kt
- [x] Criar presentation/screens/auth/RecuperarSenhaScreen.kt
- [x] Criar presentation/screens/auth/RecuperarSenhaViewModel.kt
- [x] Criar presentation/navigation/NavGraph.kt básico (auth flow)
- [x] Atualizar MainActivity.kt para usar NavGraph
- [x] Testar fluxo completo: Login → Home (mock)
- [x] Testar fluxo: Cadastro → Login → Home
- [x] Verificar validações de campos funcionando
- [x] Verificar mensagens de erro sendo exibidas

**Arquivos criados:**
- presentation/navigation/Screen.kt
- presentation/navigation/NavGraph.kt
- utils/ValidationUtils.kt
- presentation/screens/auth/LoginScreen.kt
- presentation/screens/auth/LoginViewModel.kt
- presentation/screens/auth/CadastroScreen.kt
- presentation/screens/auth/CadastroViewModel.kt
- presentation/screens/auth/RecuperarSenhaScreen.kt
- presentation/screens/auth/RecuperarSenhaViewModel.kt

**✅ Checkpoint Fase 1:** App compila, login funciona, autenticação completa

---

## Fase 2: Núcleo (Prompts 07-10) - EM ANDAMENTO

### ✅ Etapa 07: Tela Família Zero - CONCLUÍDA

**Status:** ✅ Completa

**Objetivo:** Implementar onboarding do primeiro usuário e criação da Família Zero.

**Checklist:**

- [x] Criar presentation/screens/onboarding/FamiliaZeroScreen.kt
- [x] Criar presentation/screens/onboarding/FamiliaZeroViewModel.kt
- [x] Implementar formulário de criação do casal raiz (pai e mãe)
- [x] Integrar com FamiliaZeroRepository para criar Família Zero
- [x] Integrar com PessoaRepository para criar pessoas do casal
- [x] Integrar com UsuarioRepository para atualizar usuário como admin
- [x] Atualizar NavGraph para incluir rota de onboarding
- [x] Implementar lógica: verificar se Família Zero existe → redirecionar
- [x] Criar pessoas do casal com flag ehFamiliaZero = true
- [x] Vincular casal como cônjuges (bidirecional)
- [x] Atualizar usuário com referência à Família Zero

**Arquivos criados:**
- presentation/screens/onboarding/FamiliaZeroScreen.kt
- presentation/screens/onboarding/FamiliaZeroViewModel.kt

**Dependências:** Etapa 05, 06

---

### ✅ Etapa 08: Navegação Principal - CONCLUÍDA

**Status:** ✅ Completa

**Objetivo:** Implementar navegação completa do app com bottom navigation.

**Checklist:**

- [x] Completar presentation/navigation/NavGraph.kt com todas rotas
- [x] Criar presentation/screens/home/HomeScreen.kt
- [x] Criar presentation/screens/home/HomeViewModel.kt
- [x] Criar presentation/screens/arvore/ArvoreScreen.kt (placeholder)
- [x] Criar presentation/screens/arvore/ArvoreViewModel.kt (placeholder)
- [x] Criar presentation/screens/perfil/PerfilScreen.kt
- [x] Criar presentation/screens/perfil/PerfilViewModel.kt
- [x] Implementar BottomNavigationBar (MainNavigation.kt)
- [x] Implementar navegação entre Home, Árvore, Perfil
- [x] Configurar navegação aninhada (nested navigation)
- [x] Integrar MainNavigation com NavGraph principal
- [x] Implementar componentes StatCard e PessoaCard

**Arquivos criados:**
- presentation/screens/home/HomeScreen.kt
- presentation/screens/home/HomeViewModel.kt
- presentation/screens/arvore/ArvoreScreen.kt
- presentation/screens/arvore/ArvoreViewModel.kt
- presentation/screens/perfil/PerfilScreen.kt
- presentation/screens/perfil/PerfilViewModel.kt
- presentation/navigation/MainNavigation.kt
- Atualizado presentation/navigation/NavGraph.kt

**Dependências:** Etapa 06

---

### ✅ Etapa 09: Tela Cadastro Pessoa - CONCLUÍDA

**Status:** ✅ Completa

**Objetivo:** Implementar formulário completo de cadastro/edição de pessoas.

**Checklist:**

- [x] Criar presentation/screens/cadastro/CadastroPessoaScreen.kt
- [x] Criar presentation/screens/cadastro/CadastroPessoaViewModel.kt
- [x] Implementar todos os campos: nome, datas, localização, profissão, biografia
- [x] Implementar seleção de relacionamentos (pai, mãe, cônjuge)
- [x] Criar componente PessoaSelector para seleção de pessoas existentes
- [x] Integrar com PessoaRepository para salvar e atualizar
- [x] Implementar validações de campos obrigatórios (nome mínimo 3 caracteres)
- [x] Implementar criação de pessoa no Firestore
- [x] Implementar edição de pessoa existente
- [x] Verificar relacionamentos bidirecionais sendo salvos (pai↔filho, mãe↔filho, cônjuge↔cônjuge)
- [x] Atualizar NavGraph com rotas de cadastro e edição
- [x] Implementar navegação para editar pessoa

**Arquivos criados:**
- presentation/screens/cadastro/CadastroPessoaScreen.kt
- presentation/screens/cadastro/CadastroPessoaViewModel.kt
- Componente PessoaSelector (dropdown para seleção de relacionamentos)

**Dependências:** Etapa 05, 08

---

### ✅ Etapa 10: Algoritmo Parentesco - CONCLUÍDA

**Status:** ✅ Completa

**Objetivo:** Implementar algoritmo de cálculo de relacionamentos.

**Checklist:**

- [x] Criar domain/usecase/CalcularParentescoUseCase.kt
- [x] Implementar algoritmo de cálculo de parentesco entre duas pessoas
- [x] Criar utils/ParentescoCalculator.kt com lógica de cálculo
- [x] Implementar todos os tipos de ParentescoTipo principais
- [x] Verificar relações diretas (pai, mãe, filho, cônjuge)
- [x] Verificar relações de segundo grau (irmãos, avós, netos)
- [x] Verificar relações por afinidade (sogro, cunhado, padrasto)
- [x] Verificar relações de terceiro grau (tios, sobrinhos)
- [x] Verificar relações de quarto grau (primos)
- [x] Adicionar método buscarTodas() no PessoaRepository
- [x] Integrar use case com repositories

**Arquivos criados:**
- domain/usecase/CalcularParentescoUseCase.kt
- utils/ParentescoCalculator.kt
- Atualizado PessoaRepository.kt com método buscarTodas()

**Dependências:** Etapa 03

**Checkpoint Fase 2:** Deve ser possível criar Família Zero e cadastrar pessoas

---

## Fase 3: Visualização (Prompts 11, 15)

### ✅ Etapa 11: Tela Árvore Visual - CONCLUÍDA

**Status:** ✅ Completa

**Objetivo:** Implementar visualização hierárquica da árvore genealógica.

**Checklist:**

- [x] Criar presentation/screens/arvore/ArvoreScreen.kt
- [x] Criar presentation/screens/arvore/ArvoreViewModel.kt
- [x] Criar presentation/components/NoPessoa.kt (nó da árvore)
- [x] Criar presentation/components/LinhaConexao.kt (linha conectando nós)
- [x] Implementar algoritmo de posicionamento hierárquico (ArvoreLayoutCalculator)
- [x] Implementar zoom e pan (usando gestures customizados)
- [x] Implementar busca de pessoa na árvore
- [x] Implementar filtros (vivos, falecidos, aprovados)
- [x] Integrar com PessoaRepository
- [x] Visualização hierárquica funcional
- [x] Indicador de zoom
- [x] Botão de resetar zoom

**Arquivos criados:**
- presentation/screens/arvore/ArvoreScreen.kt (implementação completa)
- presentation/screens/arvore/ArvoreViewModel.kt (com filtros e busca)
- presentation/components/NoPessoa.kt (nó visual com informações)
- presentation/components/LinhaConexao.kt (linhas de conexão)
- utils/ArvoreLayoutCalculator.kt (algoritmo de posicionamento)

**Dependências:** Etapa 09, 10

---

### ✅ Etapa 15: Compressão Imagens - CONCLUÍDA

**Status:** ✅ Completa

**Objetivo:** Implementar upload otimizado de imagens (compressão para 10KB).

**Checklist:**

- [x] Criar utils/ImageCompressor.kt com compressão para 10KB
- [x] Integrar compressor no CadastroPessoaScreen
- [x] Criar data/remote/firebase/StorageService.kt
- [x] Implementar upload para Firebase Storage
- [x] Implementar download de imagens
- [x] Integrar Coil para carregamento de imagens
- [x] Criar componente ImagePicker para seleção de fotos
- [x] Integrar upload de foto no CadastroPessoaViewModel
- [x] Exibir fotos na árvore (NoPessoa)
- [x] Adicionar módulo Hilt para StorageService

**Arquivos criados:**
- utils/ImageCompressor.kt (compressão até 10KB com Levenshtein, redimensionamento, EXIF)
- data/remote/firebase/StorageService.kt (upload/download/delete)
- presentation/components/ImagePicker.kt (seletor de imagem da galeria)
- Atualizado CadastroPessoaScreen.kt (componente ImagePicker)
- Atualizado CadastroPessoaViewModel.kt (upload de foto antes de salvar)
- Atualizado NoPessoa.kt (exibição de foto com Coil)
- Atualizado FirebaseModule.kt (provisionamento de StorageService)
- Adicionada dependência exifinterface no build.gradle.kts

**Dependências:** Etapa 09

**Checkpoint Fase 3:** Árvore deve ser visualizável e fotos devem funcionar ✅

---

## Fase 4: Colaboração (Prompts 12-14)

### ✅ Etapa 12: Sistema Convites - CONCLUÍDA

**Status:** ✅ Completa

**Objetivo:** Implementar sistema de convites para adicionar membros à árvore.

**Checklist:**

- [x] Criar data/repository/ConviteRepository.kt
- [x] Implementar envio de convites (apenas admin)
- [x] Criar tela de gerenciamento de convites (admin)
- [x] Implementar aceitação de convites
- [x] Implementar expiração de convites (7 dias)
- [x] Criar observação de novos convites (tempo real)
- [x] Integrar com PerfilScreen (badge de convites pendentes)
- [x] Verificar permissões (apenas admin pode criar)
- [x] Atualizar FirestoreService com métodos de convites
- [x] Vinculação automática de pessoa ao aceitar

**Arquivos criados:**
- data/repository/ConviteRepository.kt (gerenciamento completo)
- presentation/screens/convites/GerenciarConvitesScreen.kt (admin)
- presentation/screens/convites/GerenciarConvitesViewModel.kt
- presentation/screens/convites/AceitarConvitesScreen.kt (usuário)
- presentation/screens/convites/AceitarConvitesViewModel.kt
- FirestoreService.kt (métodos de convites completos)
- Atualizado PerfilScreen.kt (links para convites)
- Atualizado PerfilViewModel.kt (observação de convites)

**Dependências:** Etapa 05

---

### ✅ Etapa 13: Edições Pendentes - CONCLUÍDA

**Status:** ✅ Completa

**Objetivo:** Implementar sistema de aprovação para edições de não-admins.

**Checklist:**

- [x] Criar data/repository/EdicaoPendenteRepository.kt
- [x] Modificar PessoaRepository para criar edições pendentes quando não-admin
- [x] Criar tela de listagem de edições pendentes (admin)
- [x] Implementar aprovação/rejeição de edições
- [x] Criar observação de novas edições pendentes (tempo real)
- [x] Integrar com PerfilScreen (badge de edições pendentes)
- [x] Aplicar mudanças ao aprovar edição
- [x] Verificar que admins não criam pendências

**Arquivos criados:**
- data/repository/EdicaoPendenteRepository.kt (gerenciamento completo)
- presentation/screens/edicoes/GerenciarEdicoesScreen.kt (admin)
- presentation/screens/edicoes/GerenciarEdicoesViewModel.kt
- FirestoreService.kt (métodos de edições pendentes completos)
- Atualizado PessoaRepository.kt (cria edições pendentes quando não-admin)
- Atualizado PerfilScreen.kt (link para gerenciar edições)
- Atualizado PerfilViewModel.kt (observação de edições)

**Dependências:** Etapa 09

---

### ✅ Etapa 14: Detecção Duplicatas - CONCLUÍDA

**Status:** ✅ Completa

**Objetivo:** Implementar algoritmo de detecção de pessoas duplicadas.

**Checklist:**

- [x] Criar domain/usecase/DetectarDuplicatasUseCase.kt
- [x] Criar utils/DuplicateDetector.kt com algoritmo de similaridade
- [x] Implementar detecção automática ao cadastrar
- [x] Criar tela de resolução de duplicatas (admin)
- [x] Implementar merge de pessoas duplicadas
- [x] Verificar que relacionamentos são preservados no merge
- [x] Atualizar referências ao unir duplicatas
- [x] Integrar com PerfilScreen (link para resolver duplicatas)

**Arquivos criados:**
- domain/usecase/DetectarDuplicatasUseCase.kt (use case completo)
- utils/DuplicateDetector.kt (algoritmo de similaridade com Levenshtein)
- presentation/screens/duplicatas/ResolverDuplicatasScreen.kt (admin)
- presentation/screens/duplicatas/ResolverDuplicatasViewModel.kt
- Atualizado CadastroPessoaViewModel.kt (detecção automática ao cadastrar)
- Atualizado PerfilScreen.kt (link para resolver duplicatas)

**Dependências:** Etapa 09

**Checkpoint Fase 4:** Múltiplos usuários podem colaborar na árvore ✅

---

## Fase 5: Testes e Refinamento (Prompt 16)

### ✅ Etapa 16: Testes E2E - CONCLUÍDA

**Status:** ✅ Completa

**Objetivo:** Criar testes de integração completos e UI tests.

**Checklist:**

- [x] Criar testes de integração para fluxo de autenticação
- [x] Criar testes de integração para criação de Família Zero
- [x] Criar testes de integração para cadastro de pessoas
- [x] Criar testes de integração para sincronização local↔remoto
- [x] Criar documentação de testes
- [x] Estrutura de testes E2E completa

**Arquivos criados:**
- androidTest/java/com/raizesvivas/app/AutenticacaoIntegrationTest.kt (fluxo completo de autenticação)
- androidTest/java/com/raizesvivas/app/PessoaIntegrationTest.kt (CRUD completo de pessoas)
- androidTest/java/com/raizesvivas/app/FamiliaZeroIntegrationTest.kt (criação de Família Zero)
- androidTest/java/com/raizesvivas/app/README_TESTS.md (documentação de testes)

**Cobertura:**
- ✅ Fluxo de autenticação (cadastro, login, logout)
- ✅ Operações CRUD de pessoas (criar, atualizar, deletar)
- ✅ Sincronização local↔remoto
- ✅ Criação de Família Zero e atribuição de admin

**Dependências:** Todas etapas anteriores

**Checkpoint Final:** MVP pronto para uso ✅

---

## ✅ PROJETO COMPLETO

Todas as etapas foram concluídas com sucesso! O MVP do aplicativo Raízes Vivas está completo e pronto para uso.

---

## Regras de Desenvolvimento

1. **Sempre seguir ordem das etapas** - Não pular etapas
2. **Testar após cada etapa** - Verificar compilação e funcionalidade básica
3. **Atualizar checklist** - Marcar como completo após finalizar cada item
4. **Logs com Timber** - Usar Timber para todos logs, filtrar por "Timber" no Logcat
5. **Validações obrigatórias** - Sempre validar campos obrigatórios
6. **Tratamento de erros** - Sempre tratar erros de rede e Firebase
7. **Modo offline** - Garantir funcionamento offline quando possível
8. **Performance** - Otimizar queries e renderização da árvore

## Próximos Passos Após Aprovação

1. Continuar pela Etapa 07 (Tela Família Zero)
2. Após cada etapa, atualizar checklists marcando itens concluídos
3. Testar funcionalidade antes de avançar para próxima etapa
4. Manter fidelidade aos prompts originais

