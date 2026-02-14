# PRD - Raízes Vivas

## 1. Visão Geral do Produto
O **Raízes Vivas** é um aplicativo Android colaborativo projetado para que famílias possam construir, visualizar e preservar sua árvore genealógica de forma digital e interativa. O foco do app é a facilidade de uso, a colaboração entre parentes e a preservação da memória familiar em uma plataforma moderna e segura.

### 1.1 Missão
Fortalecer os laços familiares através da descoberta e documentação da ancestralidade, permitindo que cada geração contribua para o legado da família.

### 1.2 Público-Alvo
- Membros de famílias interessados em genealogia.
- Parentes que desejam manter contato e compartilhar histórias/fotos.
- Usuários que buscam uma alternativa digital e colaborativa aos álbuns de fotos físicos.

---

## 2. Funcionalidades Principais

### 2.1 Autenticação e Gestão de Usuários
- **Login/Cadastro**: Via e-mail e senha.
- **Perfis**: Cada usuário possui um perfil vinculado ao seu registro na árvore.
- **Sessão Persistente**: O aplicativo mantém o usuário conectado para acesso rápido.

### 2.2 Família Zero (Onboarding)
- **Criação da Raiz**: O primeiro usuário a se cadastrar cria a "Família Zero", estabelecendo o casal raiz da árvore.
- **Imutabilidade da Raiz**: O casal raiz é a base do sistema e não pode ser deletado, garantindo a integridade da árvore.

### 2.3 Gestão da Árvore Genealógica
- **Cadastro de Pessoas**: Formulário completo incluindo nome, data de nascimento/falecimento, local e foto.
- **Relacionamentos**: Sistema inteligente para vincular pais, filhos e cônjuges de forma bidirecional.
- **Upload de Fotos**: Integração com Firebase Storage para fotos de perfil das pessoas na árvore.

### 2.4 Visualização da Árvore
- **Interface Gráfica**: Visualização hierárquica e fluida.
- **Navegação Interativa**: Suporte a gestos de zoom e pan para explorar grandes árvores.
- **Filtros e Busca**: Localização rápida de membros da família por nome ou parentesco.

### 2.5 Sistema de Colaboração
- **Convites**: Usuários administradores podem convidar outros membros da família via link ou e-mail.
- **Controle de Acesso**: Distinção entre administradores (que podem validar edições) e membros colaboradores.
- **Edições Pendentes**: Sistema de moderação onde mudanças feitas por colaboradores aguardam aprovação de um admin.
- **Detecção de Duplicatas**: Algoritmo para identificar registros similares e evitar redundâncias na árvore.

---

## 3. Experiência do Usuário (UX) & Design

### 3.1 Design System
- **Estética**: Design premium, moderno e acolhedor.
- **Paleta de Cores**: Tons orgânicos (Verde Musgo, Marrom Terra, Creme) para evocar a natureza e a ancestralidade.
  - Primária: `#2D5016` (Verde Musgo)
  - Fundo: `#F5E6D3` (Creme)
- **Tipografia**: Uso de fontes legíveis e modernas (Roboto/Poppins).

### 3.2 Temática Visual
O app utiliza ícones e metáforas visuais relacionadas à natureza:
- 🌱 **Raiz**: Elementos fundamentais e início.
- 🌳 **Tronco/Galhos**: Crescimento e conexões da árvore.

---

## 4. Arquitetura Técnica

### 4.1 Stack Tecnológica
- **Linguagem**: Kotlin.
- **UI**: Jetpack Compose (Declarativa e moderna).
- **Arquitetura**: MVVM + Clean Architecture para separação de responsabilidades.
- **Injeção de Dependência**: Hilt (Dagger).

### 4.2 Backend (Firebase)
- **Authentication**: Gestão de identidade.
- **Firestore**: Banco de dados NoSQL em tempo real para sincronização entre dispositivos.
- **Storage**: Armazenamento otimizado de fotos.

### 4.3 Banco de Dados Local (Offline-First)
- **Room Database**: Cache local para garantir que o app funcione sem internet.
- **Sincronização**: Estratégia de sincronização local ↔ remoto automática.

---

## 5. Requisitos de Performance e Segurança
- **Segurança**: Regras de acesso (Firestore Rules) para garantir que apenas membros da família acessem seus dados.
- **Otimização de Imagens**: Redução automática do tamanho das fotos para garantir carregamento rápido e economia de armazenamento (meta: ~300KB por foto).
- **Offline**: Capacidade de visualização de dados previamente carregados sem conectividade.

---

## 6. Roadmap de Desenvolvimento

### Fase 1: Fundação
- Setup inicial, login, cadastro e integração Firebase.

### Fase 2: Núcleo do Sistema
- Família Zero, formulário de cadastro de pessoas e relacionamentos básicos.

### Fase 3: Visualização e Mídia
- Implementação da árvore visual interativa e upload de fotos com compressão.

### Fase 4: Colaboração Ativa
- Notificações push, sistema de convites, moderação de edições e detecção de duplicatas.

---

## 7. Métricas de Sucesso
- Número de membros ativos por família.
- Quantidade de registros criados (tamanho da árvore).
- Taxa de retenção de usuários.
- Tempo médio de construção de uma árvore básica (onboarding).
