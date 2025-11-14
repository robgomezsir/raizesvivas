package com.raizesvivas.app.domain.model

/**
 * Sistema de Conquistas pré-definidas
 * 
 * Contém todas as 66 conquistas disponíveis no sistema
 * Baseado em conquistas_expandidas.md
 */
object SistemaConquistas {
    
    /**
     * Todas as conquistas do sistema (66 conquistas)
     */
    fun obterTodas(): List<Conquista> {
        return listOf(
            // ========================================
            // CATEGORIA: BEM-VINDO (5 conquistas)
            // ========================================
            Conquista(
                id = "bem_vindo",
                nome = "Bem-vindo à Família!",
                descricao = "Faça seu primeiro login no app",
                categoria = CategoriaConquista.BEM_VINDO,
                recompensaXP = 10,
                icone = "👋",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.PRIMEIRO_LOGIN,
                    valor = 1,
                    descricaoCondicao = "Fazer primeiro login"
                ),
                ordem = 1
            ),
            Conquista(
                id = "primeiro_passo",
                nome = "Primeiro Passo",
                descricao = "Complete seu perfil com nome e foto",
                categoria = CategoriaConquista.BEM_VINDO,
                recompensaXP = 20,
                icone = "👤",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.COMPLETAR_PERFIL,
                    valor = 1,
                    descricaoCondicao = "Completar perfil"
                ),
                ordem = 2
            ),
            Conquista(
                id = "explorador_curioso",
                nome = "Explorador Curioso",
                descricao = "Visualize a árvore genealógica pela primeira vez",
                categoria = CategoriaConquista.BEM_VINDO,
                recompensaXP = 15,
                icone = "🔍",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.EXPLORAR_ARVORE_PRIMEIRA_VEZ,
                    valor = 1,
                    descricaoCondicao = "Visualizar árvore pela primeira vez"
                ),
                ordem = 3
            ),
            Conquista(
                id = "tutorial_completo",
                nome = "Aprendiz Rápido",
                descricao = "Complete o tutorial do aplicativo",
                categoria = CategoriaConquista.BEM_VINDO,
                recompensaXP = 30,
                icone = "🎓",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.COMPLETAR_TUTORIAL,
                    valor = 1,
                    descricaoCondicao = "Completar tutorial"
                ),
                ordem = 4
            ),
            Conquista(
                id = "primeira_visita_semanal",
                nome = "Visitante Assíduo",
                descricao = "Acesse o app por 3 dias seguidos",
                categoria = CategoriaConquista.BEM_VINDO,
                recompensaXP = 50,
                icone = "📅",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ACESSO_DIARIO,
                    valor = 3,
                    descricaoCondicao = "Acessar por 3 dias seguidos"
                ),
                ordem = 5
            ),
            
            // ========================================
            // CATEGORIA: CONSTRUTOR (13 conquistas)
            // ========================================
            Conquista(
                id = "primeiro_membro",
                nome = "Primeira Raiz",
                descricao = "Adicione o primeiro membro à sua árvore",
                categoria = CategoriaConquista.CONSTRUTOR,
                recompensaXP = 15,
                icone = "🌱",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_MEMBROS,
                    valor = 1,
                    descricaoCondicao = "Adicionar primeiro membro"
                ),
                ordem = 6
            ),
            Conquista(
                id = "familia_nuclear",
                nome = "Família Nuclear",
                descricao = "Adicione seus pais e irmãos (3 membros)",
                categoria = CategoriaConquista.CONSTRUTOR,
                recompensaXP = 30,
                icone = "👨‍👩‍👧",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_PAIS_IRMAOS,
                    valor = 3,
                    descricaoCondicao = "Adicionar pais e irmãos"
                ),
                ordem = 7
            ),
            Conquista(
                id = "construtor_iniciante",
                nome = "Construtor Iniciante",
                descricao = "Adicione 5 membros à sua árvore",
                categoria = CategoriaConquista.CONSTRUTOR,
                recompensaXP = 50,
                icone = "👥",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_MEMBROS,
                    valor = 5,
                    descricaoCondicao = "Adicionar 5 membros"
                ),
                ordem = 8
            ),
            Conquista(
                id = "duas_geracoes",
                nome = "Duas Gerações",
                descricao = "Adicione membros de pelo menos 2 gerações diferentes",
                categoria = CategoriaConquista.CONSTRUTOR,
                recompensaXP = 40,
                icone = "👨‍👩‍👧‍👦",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_DUAS_GERACOES,
                    valor = 2,
                    descricaoCondicao = "Adicionar 2 gerações"
                ),
                ordem = 9
            ),
            Conquista(
                id = "construtor_intermediario",
                nome = "Construtor Intermediário",
                descricao = "Adicione 15 membros à sua árvore",
                categoria = CategoriaConquista.CONSTRUTOR,
                recompensaXP = 100,
                icone = "👨‍👩‍👧‍👦",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_MEMBROS,
                    valor = 15,
                    descricaoCondicao = "Adicionar 15 membros"
                ),
                ordem = 10
            ),
            Conquista(
                id = "tres_geracoes",
                nome = "Três Gerações",
                descricao = "Conecte 3 gerações da família",
                categoria = CategoriaConquista.CONSTRUTOR,
                recompensaXP = 75,
                icone = "👴👨👶",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_TRES_GERACOES,
                    valor = 3,
                    descricaoCondicao = "Conectar 3 gerações"
                ),
                ordem = 11
            ),
            Conquista(
                id = "arvore_crescendo",
                nome = "Árvore Crescendo",
                descricao = "Adicione 25 membros à sua árvore",
                categoria = CategoriaConquista.CONSTRUTOR,
                recompensaXP = 200,
                icone = "🌳",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_MEMBROS,
                    valor = 25,
                    descricaoCondicao = "Adicionar 25 membros"
                ),
                ordem = 12
            ),
            Conquista(
                id = "construtor_avancado",
                nome = "Construtor Avançado",
                descricao = "Adicione 50 membros à sua árvore",
                categoria = CategoriaConquista.CONSTRUTOR,
                recompensaXP = 350,
                icone = "🌲",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_MEMBROS,
                    valor = 50,
                    descricaoCondicao = "Adicionar 50 membros"
                ),
                ordem = 13
            ),
            Conquista(
                id = "quatro_geracoes",
                nome = "Quatro Gerações",
                descricao = "Conecte 4 gerações da família",
                categoria = CategoriaConquista.CONSTRUTOR,
                recompensaXP = 150,
                icone = "👴👨👦👶",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_QUATRO_GERACOES,
                    valor = 4,
                    descricaoCondicao = "Conectar 4 gerações"
                ),
                ordem = 14
            ),
            Conquista(
                id = "construtor_mestre",
                nome = "Mestre Construtor",
                descricao = "Adicione 100 membros à sua árvore",
                categoria = CategoriaConquista.CONSTRUTOR,
                recompensaXP = 1000,
                icone = "🌴",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_MEMBROS_TOTAL,
                    valor = 100,
                    descricaoCondicao = "Adicionar 100 membros"
                ),
                ordem = 15
            ),
            Conquista(
                id = "cinco_geracoes",
                nome = "Cinco Gerações",
                descricao = "Conecte 5 gerações da família",
                categoria = CategoriaConquista.CONSTRUTOR,
                recompensaXP = 300,
                icone = "👴👨👦👶👼",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_CINCO_GERACOES,
                    valor = 5,
                    descricaoCondicao = "Conectar 5 gerações"
                ),
                ordem = 16
            ),
            Conquista(
                id = "raizes_plantadas",
                nome = "Raízes Plantadas",
                descricao = "Crie sua primeira família",
                categoria = CategoriaConquista.CONSTRUTOR,
                recompensaXP = 50,
                icone = "🌱",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.CRIAR_FAMILIA_ZERO,
                    valor = 1,
                    descricaoCondicao = "Criar primeira família"
                ),
                ordem = 17
            ),
            
            // ========================================
            // CATEGORIA: HISTORIADOR (13 conquistas)
            // ========================================
            Conquista(
                id = "primeira_foto",
                nome = "Primeira Memória",
                descricao = "Adicione a primeira foto a um membro",
                categoria = CategoriaConquista.HISTORIADOR,
                recompensaXP = 15,
                icone = "📷",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_FOTOS,
                    valor = 1,
                    descricaoCondicao = "Adicionar primeira foto"
                ),
                ordem = 18
            ),
            Conquista(
                id = "primeira_data",
                nome = "Marcador de Tempo",
                descricao = "Adicione data de nascimento a 3 membros",
                categoria = CategoriaConquista.HISTORIADOR,
                recompensaXP = 25,
                icone = "🎂",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_DATA_NASCIMENTO,
                    valor = 3,
                    descricaoCondicao = "Adicionar data a 3 membros"
                ),
                ordem = 19
            ),
            Conquista(
                id = "historiador_iniciante",
                nome = "Historiador Iniciante",
                descricao = "Adicione informações completas a 5 membros",
                categoria = CategoriaConquista.HISTORIADOR,
                recompensaXP = 50,
                icone = "📝",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.PREENCHER_COMPLETO,
                    valor = 5,
                    descricaoCondicao = "Completar 5 membros"
                ),
                ordem = 20
            ),
            Conquista(
                id = "fotografo_familiar",
                nome = "Fotógrafo Familiar",
                descricao = "Adicione fotos a 5 membros diferentes",
                categoria = CategoriaConquista.HISTORIADOR,
                recompensaXP = 40,
                icone = "📸",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_FOTOS,
                    valor = 5,
                    descricaoCondicao = "Adicionar fotos a 5 membros"
                ),
                ordem = 21
            ),
            Conquista(
                id = "primeira_biografia",
                nome = "Primeira História",
                descricao = "Escreva a primeira biografia",
                categoria = CategoriaConquista.HISTORIADOR,
                recompensaXP = 30,
                icone = "📖",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_BIOGRAFIA,
                    valor = 1,
                    descricaoCondicao = "Escrever primeira biografia"
                ),
                ordem = 22
            ),
            Conquista(
                id = "colecionador_memorias",
                nome = "Colecionador de Memórias",
                descricao = "Adicione 15 fotos à árvore",
                categoria = CategoriaConquista.HISTORIADOR,
                recompensaXP = 100,
                icone = "🖼️",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_FOTOS,
                    valor = 15,
                    descricaoCondicao = "Adicionar 15 fotos"
                ),
                ordem = 23
            ),
            Conquista(
                id = "biografo",
                nome = "Biógrafo",
                descricao = "Escreva biografias para 5 membros",
                categoria = CategoriaConquista.HISTORIADOR,
                recompensaXP = 120,
                icone = "📚",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_BIOGRAFIA,
                    valor = 5,
                    descricaoCondicao = "Escrever 5 biografias"
                ),
                ordem = 24
            ),
            Conquista(
                id = "detalhista",
                nome = "Detalhista",
                descricao = "Adicione local de nascimento a 10 membros",
                categoria = CategoriaConquista.HISTORIADOR,
                recompensaXP = 80,
                icone = "📍",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_LOCAL_NASCIMENTO,
                    valor = 10,
                    descricaoCondicao = "Adicionar local a 10 membros"
                ),
                ordem = 25
            ),
            Conquista(
                id = "historiador_avancado",
                nome = "Historiador Avançado",
                descricao = "Adicione 50 fotos à árvore",
                categoria = CategoriaConquista.HISTORIADOR,
                recompensaXP = 300,
                icone = "📷",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_FOTOS,
                    valor = 50,
                    descricaoCondicao = "Adicionar 50 fotos"
                ),
                ordem = 26
            ),
            Conquista(
                id = "escritor_familiar",
                nome = "Escritor Familiar",
                descricao = "Escreva biografias detalhadas para 15 membros",
                categoria = CategoriaConquista.HISTORIADOR,
                recompensaXP = 250,
                icone = "✍️",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_BIOGRAFIA,
                    valor = 15,
                    descricaoCondicao = "Escrever 15 biografias"
                ),
                ordem = 27
            ),
            Conquista(
                id = "arquivista_mestre",
                nome = "Arquivista Mestre",
                descricao = "Adicione 100 fotos à árvore",
                categoria = CategoriaConquista.HISTORIADOR,
                recompensaXP = 800,
                icone = "📁",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_FOTOS,
                    valor = 100,
                    descricaoCondicao = "Adicionar 100 fotos"
                ),
                ordem = 28
            ),
            Conquista(
                id = "cronista_familiar",
                nome = "Cronista Familiar",
                descricao = "Preencha TODOS os campos de 25 membros",
                categoria = CategoriaConquista.HISTORIADOR,
                recompensaXP = 600,
                icone = "✅",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.PREENCHER_COMPLETO,
                    valor = 25,
                    descricaoCondicao = "Completar 25 membros"
                ),
                ordem = 29
            ),
            
            // ========================================
            // CATEGORIA: CONECTOR (13 conquistas)
            // ========================================
            Conquista(
                id = "primeira_mensagem",
                nome = "Primeira Conversa",
                descricao = "Envie sua primeira mensagem no chat",
                categoria = CategoriaConquista.CONECTOR,
                recompensaXP = 10,
                icone = "💬",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ENVIAR_MENSAGEM,
                    valor = 1,
                    descricaoCondicao = "Enviar primeira mensagem"
                ),
                ordem = 30
            ),
            Conquista(
                id = "sociavel",
                nome = "Sociável",
                descricao = "Envie mensagens para 3 parentes diferentes",
                categoria = CategoriaConquista.CONECTOR,
                recompensaXP = 30,
                icone = "👥",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ENVIAR_MENSAGEM_DIFERENTES_PARENTES,
                    valor = 3,
                    descricaoCondicao = "Conversar com 3 parentes"
                ),
                ordem = 31
            ),
            Conquista(
                id = "primeiro_recado",
                nome = "Primeiro Recado",
                descricao = "Publique seu primeiro recado no mural",
                categoria = CategoriaConquista.CONECTOR,
                recompensaXP = 15,
                icone = "📢",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.CRIAR_RECADO,
                    valor = 1,
                    descricaoCondicao = "Criar primeiro recado"
                ),
                ordem = 32
            ),
            Conquista(
                id = "conector_iniciante",
                nome = "Conector Iniciante",
                descricao = "Envie 10 mensagens no chat",
                categoria = CategoriaConquista.CONECTOR,
                recompensaXP = 50,
                icone = "💭",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ENVIAR_MENSAGEM,
                    valor = 10,
                    descricaoCondicao = "Enviar 10 mensagens"
                ),
                ordem = 33
            ),
            Conquista(
                id = "apoiador",
                nome = "Apoiador",
                descricao = "Dê 5 apoios familiares em recados",
                categoria = CategoriaConquista.CONECTOR,
                recompensaXP = 25,
                icone = "❤️",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.DAR_APOIO_FAMILIAR,
                    valor = 5,
                    descricaoCondicao = "Dar 5 apoios"
                ),
                ordem = 34
            ),
            Conquista(
                id = "comunicador",
                nome = "Comunicador",
                descricao = "Envie 50 mensagens no chat",
                categoria = CategoriaConquista.CONECTOR,
                recompensaXP = 150,
                icone = "💬",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ENVIAR_MENSAGEM,
                    valor = 50,
                    descricaoCondicao = "Enviar 50 mensagens"
                ),
                ordem = 35
            ),
            Conquista(
                id = "publicador",
                nome = "Publicador",
                descricao = "Crie 10 recados no mural",
                categoria = CategoriaConquista.CONECTOR,
                recompensaXP = 100,
                icone = "📰",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.CRIAR_RECADO,
                    valor = 10,
                    descricaoCondicao = "Criar 10 recados"
                ),
                ordem = 36
            ),
            Conquista(
                id = "rede_social",
                nome = "Rede Social",
                descricao = "Converse com 10 parentes diferentes",
                categoria = CategoriaConquista.CONECTOR,
                recompensaXP = 120,
                icone = "🌐",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ENVIAR_MENSAGEM_DIFERENTES_PARENTES,
                    valor = 10,
                    descricaoCondicao = "Conversar com 10 parentes"
                ),
                ordem = 37
            ),
            Conquista(
                id = "conector_avancado",
                nome = "Conector Avançado",
                descricao = "Envie 200 mensagens no chat",
                categoria = CategoriaConquista.CONECTOR,
                recompensaXP = 400,
                icone = "💬",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ENVIAR_MENSAGEM,
                    valor = 200,
                    descricaoCondicao = "Enviar 200 mensagens"
                ),
                ordem = 38
            ),
            Conquista(
                id = "influencer_familiar",
                nome = "Influencer Familiar",
                descricao = "Receba 50 apoios familiares em seus recados",
                categoria = CategoriaConquista.CONECTOR,
                recompensaXP = 250,
                icone = "👍",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.RECEBER_APOIO_FAMILIAR,
                    valor = 50,
                    descricaoCondicao = "Receber 50 apoios"
                ),
                ordem = 39
            ),
            Conquista(
                id = "conector_mestre",
                nome = "Mestre Conector",
                descricao = "Envie 1000 mensagens no chat",
                categoria = CategoriaConquista.CONECTOR,
                recompensaXP = 1000,
                icone = "💬",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ENVIAR_MENSAGEM,
                    valor = 1000,
                    descricaoCondicao = "Enviar 1000 mensagens"
                ),
                ordem = 40
            ),
            Conquista(
                id = "celebridade_familiar",
                nome = "Celebridade Familiar",
                descricao = "Receba 200 apoios familiares",
                categoria = CategoriaConquista.CONECTOR,
                recompensaXP = 600,
                icone = "⭐",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.RECEBER_APOIO_FAMILIAR,
                    valor = 200,
                    descricaoCondicao = "Receber 200 apoios"
                ),
                ordem = 41
            ),
            
            // ========================================
            // CATEGORIA: EXPLORADOR (8 conquistas)
            // ========================================
            Conquista(
                id = "primeira_exploracao",
                nome = "Primeira Exploração",
                descricao = "Visualize 5 perfis de membros diferentes",
                categoria = CategoriaConquista.EXPLORADOR,
                recompensaXP = 20,
                icone = "🔍",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.VISUALIZAR_MEMBRO,
                    valor = 5,
                    descricaoCondicao = "Visualizar 5 perfis"
                ),
                ordem = 42
            ),
            Conquista(
                id = "curioso",
                nome = "Curioso",
                descricao = "Abra a árvore genealógica 10 vezes",
                categoria = CategoriaConquista.EXPLORADOR,
                recompensaXP = 30,
                icone = "👀",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.VISUALIZAR_ARVORE,
                    valor = 10,
                    descricaoCondicao = "Abrir árvore 10 vezes"
                ),
                ordem = 43
            ),
            Conquista(
                id = "descobridor_parentesco",
                nome = "Descobridor de Parentesco",
                descricao = "Visualize o cálculo de parentesco pela primeira vez",
                categoria = CategoriaConquista.EXPLORADOR,
                recompensaXP = 25,
                icone = "🧮",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.VISUALIZAR_PARENTESCO,
                    valor = 1,
                    descricaoCondicao = "Visualizar parentesco"
                ),
                ordem = 44
            ),
            Conquista(
                id = "explorador_ativo",
                nome = "Explorador Ativo",
                descricao = "Visualize 25 perfis diferentes",
                categoria = CategoriaConquista.EXPLORADOR,
                recompensaXP = 100,
                icone = "🗺️",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.VISUALIZAR_MEMBRO,
                    valor = 25,
                    descricaoCondicao = "Visualizar 25 perfis"
                ),
                ordem = 45
            ),
            Conquista(
                id = "navegador",
                nome = "Navegador",
                descricao = "Navegue pela árvore 50 vezes",
                categoria = CategoriaConquista.EXPLORADOR,
                recompensaXP = 120,
                icone = "🧭",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.VISUALIZAR_ARVORE,
                    valor = 50,
                    descricaoCondicao = "Navegar 50 vezes"
                ),
                ordem = 46
            ),
            Conquista(
                id = "conhecedor_familia",
                nome = "Conhecedor da Família",
                descricao = "Visualize perfis de 50 membros diferentes",
                categoria = CategoriaConquista.EXPLORADOR,
                recompensaXP = 250,
                icone = "👨‍👩‍👧‍👦",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.VISUALIZAR_MEMBRO,
                    valor = 50,
                    descricaoCondicao = "Visualizar 50 perfis"
                ),
                ordem = 47
            ),
            Conquista(
                id = "explorador_mestre",
                nome = "Explorador Mestre",
                descricao = "Navegue pela árvore 200 vezes",
                categoria = CategoriaConquista.EXPLORADOR,
                recompensaXP = 400,
                icone = "🌍",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.VISUALIZAR_ARVORE,
                    valor = 200,
                    descricaoCondicao = "Navegar 200 vezes"
                ),
                ordem = 48
            ),
            
            // ========================================
            // CATEGORIA: ASSIDUIDADE (7 conquistas)
            // ========================================
            Conquista(
                id = "primeira_semana",
                nome = "Primeira Semana",
                descricao = "Use o app por 7 dias seguidos",
                categoria = CategoriaConquista.ASSIDUIDADE,
                recompensaXP = 100,
                icone = "📅",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ACESSO_DIARIO,
                    valor = 7,
                    descricaoCondicao = "Usar por 7 dias seguidos"
                ),
                ordem = 49
            ),
            Conquista(
                id = "madrugador",
                nome = "Madrugador",
                descricao = "Acesse o app antes das 8h da manhã",
                categoria = CategoriaConquista.ASSIDUIDADE,
                recompensaXP = 20,
                icone = "🌅",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ACESSO_MANHA,
                    valor = 1,
                    descricaoCondicao = "Acessar antes das 8h"
                ),
                ordem = 50
            ),
            Conquista(
                id = "noturno",
                nome = "Coruja Noturna",
                descricao = "Acesse o app depois das 22h",
                categoria = CategoriaConquista.ASSIDUIDADE,
                recompensaXP = 20,
                icone = "🌙",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ACESSO_NOITE,
                    valor = 1,
                    descricaoCondicao = "Acessar depois das 22h"
                ),
                ordem = 51
            ),
            Conquista(
                id = "usuario_mensal",
                nome = "Usuário Mensal",
                descricao = "Use o app por 30 dias seguidos",
                categoria = CategoriaConquista.ASSIDUIDADE,
                recompensaXP = 300,
                icone = "📆",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ACESSO_DIARIO,
                    valor = 30,
                    descricaoCondicao = "Usar por 30 dias seguidos"
                ),
                ordem = 52
            ),
            Conquista(
                id = "fim_de_semana",
                nome = "Fim de Semana Ativo",
                descricao = "Acesse o app em 10 fins de semana",
                categoria = CategoriaConquista.ASSIDUIDADE,
                recompensaXP = 150,
                icone = "🎉",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ACESSO_FIM_SEMANA,
                    valor = 10,
                    descricaoCondicao = "Acessar em 10 fins de semana"
                ),
                ordem = 53
            ),
            Conquista(
                id = "veterano",
                nome = "Veterano",
                descricao = "Use o app por 100 dias seguidos",
                categoria = CategoriaConquista.ASSIDUIDADE,
                recompensaXP = 1000,
                icone = "🎖️",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ACESSO_DIARIO,
                    valor = 100,
                    descricaoCondicao = "Usar por 100 dias seguidos"
                ),
                ordem = 54
            ),
            Conquista(
                id = "lenda",
                nome = "Lenda Familiar",
                descricao = "Use o app por 365 dias seguidos",
                categoria = CategoriaConquista.ASSIDUIDADE,
                recompensaXP = 5000,
                icone = "👑",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ACESSO_DIARIO,
                    valor = 365,
                    descricaoCondicao = "Usar por 365 dias seguidos"
                ),
                ordem = 55
            ),
            
            // ========================================
            // CATEGORIA: ESPECIAL (5 conquistas)
            // ========================================
            Conquista(
                id = "aniversariante",
                nome = "Parabéns!",
                descricao = "Acesse o app no seu aniversário",
                categoria = CategoriaConquista.ESPECIAL,
                recompensaXP = 50,
                icone = "🎂",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ACESSO_ANIVERSARIO,
                    valor = 1,
                    descricaoCondicao = "Acessar no aniversário"
                ),
                ordem = 56
            ),
            Conquista(
                id = "natal_familiar",
                nome = "Espírito Natalino",
                descricao = "Acesse o app no Natal",
                categoria = CategoriaConquista.ESPECIAL,
                recompensaXP = 30,
                icone = "🎄",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ACESSO_NATAL,
                    valor = 1,
                    descricaoCondicao = "Acessar no Natal"
                ),
                ordem = 57
            ),
            Conquista(
                id = "ano_novo",
                nome = "Feliz Ano Novo!",
                descricao = "Acesse o app no Réveillon",
                categoria = CategoriaConquista.ESPECIAL,
                recompensaXP = 40,
                icone = "🎆",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ACESSO_ANO_NOVO,
                    valor = 1,
                    descricaoCondicao = "Acessar no Réveillon"
                ),
                ordem = 58
            ),
            Conquista(
                id = "dia_das_maes",
                nome = "Homenagem à Mãe",
                descricao = "Acesse o app no Dia das Mães",
                categoria = CategoriaConquista.ESPECIAL,
                recompensaXP = 30,
                icone = "💐",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ACESSO_DIA_MAES,
                    valor = 1,
                    descricaoCondicao = "Acessar no Dia das Mães"
                ),
                ordem = 59
            ),
            Conquista(
                id = "dia_dos_pais",
                nome = "Homenagem ao Pai",
                descricao = "Acesse o app no Dia dos Pais",
                categoria = CategoriaConquista.ESPECIAL,
                recompensaXP = 30,
                icone = "🤝",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ACESSO_DIA_PAIS,
                    valor = 1,
                    descricaoCondicao = "Acessar no Dia dos Pais"
                ),
                ordem = 60
            ),
            
            // ========================================
            // CATEGORIA: ÉPICA (6 conquistas)
            // ========================================
            Conquista(
                id = "perfeccionista",
                nome = "Perfeccionista",
                descricao = "Complete 100% de informações em 50 membros",
                categoria = CategoriaConquista.EPICA,
                recompensaXP = 2000,
                icone = "✅",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.PREENCHER_COMPLETO,
                    valor = 50,
                    descricaoCondicao = "Completar 50 membros"
                ),
                rara = true,
                ordem = 61
            ),
            Conquista(
                id = "genealogista_profissional",
                nome = "Genealogista Profissional",
                descricao = "Complete TODAS as conquistas de Construtor",
                categoria = CategoriaConquista.EPICA,
                recompensaXP = 1500,
                icone = "🏆",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.TODAS_CONSTRUTOR,
                    valor = 1,
                    descricaoCondicao = "Completar todas de Construtor"
                ),
                rara = true,
                ordem = 62
            ),
            Conquista(
                id = "historiador_mestre_epico",
                nome = "Historiador Mestre",
                descricao = "Complete TODAS as conquistas de Historiador",
                categoria = CategoriaConquista.EPICA,
                recompensaXP = 1500,
                icone = "📚",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.TODAS_HISTORIADOR,
                    valor = 1,
                    descricaoCondicao = "Completar todas de Historiador"
                ),
                rara = true,
                ordem = 63
            ),
            Conquista(
                id = "unificador_familiar",
                nome = "Unificador Familiar",
                descricao = "Conecte 10 subfamílias diferentes",
                categoria = CategoriaConquista.EPICA,
                recompensaXP = 1000,
                icone = "🔗",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.CRIAR_SUBFAMILIAS,
                    valor = 10,
                    descricaoCondicao = "Conectar 10 subfamílias"
                ),
                rara = true,
                ordem = 64
            ),
            Conquista(
                id = "lenda_viva",
                nome = "Lenda Viva",
                descricao = "Alcance nível 50",
                categoria = CategoriaConquista.EPICA,
                recompensaXP = 5000,
                icone = "💎",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ALCANCAR_NIVEL,
                    valor = 50,
                    descricaoCondicao = "Alcançar nível 50"
                ),
                rara = true,
                ordem = 65
            ),
            Conquista(
                id = "colecionador_supremo",
                nome = "Colecionador Supremo",
                descricao = "Desbloqueie TODAS as conquistas do app",
                categoria = CategoriaConquista.EPICA,
                recompensaXP = 10000,
                icone = "⭐",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.TODAS_CONQUISTAS,
                    valor = 1,
                    descricaoCondicao = "Desbloquear todas as conquistas"
                ),
                rara = true,
                ordem = 66
            )
        )
    }
    
    /**
     * Calcula XP necessário para um nível
     */
    fun calcularXPProximoNivel(nivel: Int): Int {
        // Fórmula: XP = nivel * 500
        return nivel * 500
    }
    
    /**
     * Calcula nível baseado em XP total
     */
    fun calcularNivel(xpTotal: Int): Int {
        var nivel = 1
        var xpAcumulado = 0
        
        while (xpAcumulado + calcularXPProximoNivel(nivel) <= xpTotal) {
            xpAcumulado += calcularXPProximoNivel(nivel)
            nivel++
        }
        
        return nivel
    }
    
    /**
     * Calcula XP necessário para próximo nível
     */
    fun obterXPProximoNivel(nivel: Int): Int {
        return calcularXPProximoNivel(nivel)
    }
    
    /**
     * Calcula XP atual no nível atual
     */
    fun calcularXPNoNivel(xpTotal: Int, nivel: Int): Int {
        var xpAcumulado = 0
        for (i in 1 until nivel) {
            xpAcumulado += calcularXPProximoNivel(i)
        }
        return xpTotal - xpAcumulado
    }
}
