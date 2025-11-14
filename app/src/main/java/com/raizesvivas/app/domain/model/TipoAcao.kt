package com.raizesvivas.app.domain.model

/**
 * Tipos de ações rastreáveis no sistema de conquistas
 * 
 * Cada ação pode desbloquear ou atualizar progresso de múltiplas conquistas.
 * O sistema mapeia automaticamente ações para conquistas relacionadas.
 */
enum class TipoAcao {
    // ========================================
    // BEM-VINDO: Onboarding
    // ========================================
    PRIMEIRO_LOGIN,
    COMPLETAR_PERFIL,
    EXPLORAR_ARVORE_PRIMEIRA_VEZ, // Visualizar árvore pela primeira vez
    COMPLETAR_TUTORIAL,
    ACESSO_DIARIO, // Para "Visitante Assíduo"
    
    // ========================================
    // CONSTRUTOR: Adicionar Membros
    // ========================================
    ADICIONAR_MEMBRO,
    ADICIONAR_PAIS_IRMAOS, // Para "Família Nuclear"
    ADICIONAR_DUAS_GERACOES,
    ADICIONAR_TRES_GERACOES,
    ADICIONAR_QUATRO_GERACOES,
    ADICIONAR_CINCO_GERACOES,
    CRIAR_FAMILIA_ZERO,
    CRIAR_SUBFAMILIA,
    
    // ========================================
    // HISTORIADOR: Adicionar Informações
    // ========================================
    ADICIONAR_FOTO,
    ADICIONAR_DATA_NASCIMENTO,
    ADICIONAR_BIOGRAFIA,
    ADICIONAR_LOCAL_NASCIMENTO,
    PREENCHER_COMPLETO, // Para "Historiador Iniciante", "Cronista Familiar", "Perfeccionista"
    
    // ========================================
    // CONECTOR: Interação Social
    // ========================================
    ENVIAR_MENSAGEM,
    ENVIAR_MENSAGEM_DIFERENTES_PARENTES, // Para "Sociável", "Rede Social"
    CRIAR_RECADO,
    DAR_APOIO_FAMILIAR,
    RECEBER_APOIO_FAMILIAR,
    
    // ========================================
    // EXPLORADOR: Navegação e Descoberta
    // ========================================
    VISUALIZAR_MEMBRO,
    VISUALIZAR_ARVORE,
    VISUALIZAR_PARENTESCO,
    
    // ========================================
    // ASSIDUIDADE: Engajamento Temporal
    // ========================================
    ACESSO_MANHA,
    ACESSO_NOITE,
    ACESSO_FIM_SEMANA,
    
    // ========================================
    // EVENTOS ESPECIAIS
    // ========================================
    ACESSO_ANIVERSARIO,
    ACESSO_NATAL,
    ACESSO_ANO_NOVO,
    ACESSO_DIA_MAES,
    ACESSO_DIA_PAIS,
    
    // ========================================
    // ÉPICAS (geralmente acionadas por outras conquistas ou níveis)
    // ========================================
    TODAS_CONSTRUTOR,
    TODAS_HISTORIADOR,
    TODAS_CONECTOR,
    TODAS_EXPLORADOR,
    TODAS_ASSIDUIDADE,
    TODAS_ESPECIAIS,
    ALCANCAR_NIVEL,
    TODAS_CONQUISTAS
}

