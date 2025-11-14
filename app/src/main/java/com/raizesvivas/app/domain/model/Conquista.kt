package com.raizesvivas.app.domain.model

/**
 * Modelo de Conquista
 * 
 * Representa uma conquista/gamificação que pode ser desbloqueada pelo usuário
 */
data class Conquista(
    val id: String,
    val nome: String,
    val descricao: String,
    val categoria: CategoriaConquista,
    val recompensaXP: Int,
    val icone: String? = null, // Nome do ícone ou emoji
    val condicao: CondicaoConquista,
    val rara: Boolean = false, // Conquista rara/oculta
    val ordem: Int = 0 // Ordem de exibição
)

/**
 * Categorias de conquistas
 */
enum class CategoriaConquista(val descricao: String, val icone: String) {
    BEM_VINDO("Bem-vindo", "👋"),
    CONSTRUTOR("Construtor", "👥"),
    HISTORIADOR("Historiador", "📖"),
    CONECTOR("Conector", "💬"),
    EXPLORADOR("Explorador", "🔍"),
    ASSIDUIDADE("Assiduidade", "⏰"),
    ESPECIAL("Especial", "⭐"),
    EPICA("Épica", "👑"),
    // Mantido para compatibilidade
    HISTORIA("História", "📚"),
    CONEXOES("Conexões", "🔗"),
    ESPECIAIS("Especiais", "⭐")
}

/**
 * Condições para desbloquear conquista
 */
data class CondicaoConquista(
    val tipo: TipoCondicao,
    val valor: Int, // Valor alvo para a condição
    val valorAtual: Int = 0, // Valor atual do progresso
    val descricaoCondicao: String // Descrição da condição
)

/**
 * Tipos de condições
 */
enum class TipoCondicao {
    // Bem-vindo
    PRIMEIRO_LOGIN, // Primeiro login
    COMPLETAR_PERFIL, // Completar perfil
    EXPLORAR_ARVORE_PRIMEIRA_VEZ, // Visualizar árvore pela primeira vez
    COMPLETAR_TUTORIAL, // Completar tutorial
    ACESSO_DIARIO, // Acessar por X dias seguidos
    
    // Construtor
    ADICIONAR_MEMBROS, // Adicionar X membros
    ADICIONAR_MEMBROS_TOTAL, // Adicionar X membros no total
    ADICIONAR_PAIS_IRMAOS, // Adicionar pais e irmãos (3 membros)
    ADICIONAR_DUAS_GERACOES, // Adicionar 2 gerações
    ADICIONAR_TRES_GERACOES, // Adicionar 3 gerações
    ADICIONAR_QUATRO_GERACOES, // Adicionar 4 gerações
    ADICIONAR_CINCO_GERACOES, // Adicionar 5 gerações
    CRIAR_FAMILIA_ZERO, // Criar Família Zero (conquista instantânea)
    CRIAR_SUBFAMILIAS, // Criar X subfamílias
    
    // Historiador
    ADICIONAR_FOTOS, // Adicionar fotos para X membros
    ADICIONAR_DATA_NASCIMENTO, // Adicionar data de nascimento a X membros
    ADICIONAR_BIOGRAFIA, // Escrever biografias para X membros
    ADICIONAR_LOCAL_NASCIMENTO, // Adicionar local de nascimento a X membros
    COMPLETAR_MEMBROS, // Completar dados de X membros (todos os campos)
    PREENCHER_COMPLETO, // Preencher todos os campos de X membros
    
    // Conector
    ENVIAR_MENSAGEM, // Enviar X mensagens
    ENVIAR_MENSAGEM_DIFERENTES_PARENTES, // Enviar mensagens para X parentes diferentes
    CRIAR_RECADO, // Criar X recados
    DAR_APOIO_FAMILIAR, // Dar X apoios familiares
    RECEBER_APOIO_FAMILIAR, // Receber X apoios familiares
    
    // Explorador
    VISUALIZAR_MEMBRO, // Visualizar X perfis de membros diferentes
    VISUALIZAR_ARVORE, // Abrir árvore X vezes
    VISUALIZAR_PARENTESCO, // Visualizar cálculo de parentesco
    VISUALIZAR_FLORESTA, // Visualizar floresta completa pela primeira vez
    
    // Assiduidade
    ACESSO_MANHA, // Acessar antes das 8h
    ACESSO_NOITE, // Acessar depois das 22h
    ACESSO_FIM_SEMANA, // Acessar em X fins de semana
    
    // Especiais
    ACESSO_ANIVERSARIO, // Acessar no aniversário
    ACESSO_NATAL, // Acessar no Natal
    ACESSO_ANO_NOVO, // Acessar no Réveillon
    ACESSO_DIA_MAES, // Acessar no Dia das Mães
    ACESSO_DIA_PAIS, // Acessar no Dia dos Pais
    
    // Épicas
    TODAS_CONSTRUTOR, // Completar todas conquistas de Construtor
    TODAS_HISTORIADOR, // Completar todas conquistas de Historiador
    ALCANCAR_NIVEL, // Alcançar nível X
    TODAS_CONQUISTAS, // Desbloquear todas as conquistas
    
    // Legado (mantido para compatibilidade)
    REGISTRAR_CASAMENTOS, // Registrar X casamentos
    MAPEAR_GERACOES, // Mapear X gerações
    DESCOBRIR_PARENTESCO_DISTANTE, // Descobrir parentesco de X grau
    MAPEAR_ANOS, // Mapear X anos de história
    MEMBRO_IDADE, // Membro com mais de X anos
    MAPEAR_GERACOES_TOTAL // Mapear X gerações no total
}

/**
 * Progresso do usuário em uma conquista
 * 
 * Armazenado em: usuarios/{userId}/conquistasProgresso/{conquistaId}
 */
data class ProgressoConquista(
    val conquistaId: String,
    val concluida: Boolean, // Renomeado de "desbloqueada" para "concluida"
    val desbloqueadaEm: java.util.Date?,
    val progresso: Int, // Renomeado de "progressoAtual" para "progresso"
    val progressoTotal: Int,
    val nivel: Int = 1, // Novo campo
    val pontuacaoTotal: Int = 0 // Novo campo (XP total ganho com esta conquista)
) {
    /**
     * Compatibilidade com código antigo (deprecated)
     * @deprecated Use 'concluida' ao invés de 'desbloqueada'
     */
    @Deprecated("Use 'concluida' ao invés de 'desbloqueada'", ReplaceWith("concluida"))
    val desbloqueada: Boolean
        get() = concluida
    
    /**
     * Compatibilidade com código antigo (deprecated)
     * @deprecated Use 'progresso' ao invés de 'progressoAtual'
     */
    @Deprecated("Use 'progresso' ao invés de 'progressoAtual'", ReplaceWith("progresso"))
    val progressoAtual: Int
        get() = progresso
    
    /**
     * Construtor de compatibilidade para migração gradual
     */
    constructor(
        conquistaId: String,
        desbloqueada: Boolean,
        desbloqueadaEm: java.util.Date?,
        progressoAtual: Int,
        progressoTotal: Int
    ) : this(
        conquistaId = conquistaId,
        concluida = desbloqueada,
        desbloqueadaEm = desbloqueadaEm,
        progresso = progressoAtual,
        progressoTotal = progressoTotal,
        nivel = 1,
        pontuacaoTotal = 0
    )
}

/**
 * Perfil de gamificação do usuário
 */
data class PerfilGamificacao(
    val usuarioId: String,
    val nivel: Int,
    val xpAtual: Int,
    val xpProximoNivel: Int,
    val conquistasDesbloqueadas: Int,
    val totalConquistas: Int,
    val historicoXP: List<HistoricoXP> = emptyList()
)

/**
 * Histórico de ganho de XP
 */
data class HistoricoXP(
    val data: java.util.Date,
    val xp: Int,
    val motivo: String, // Descrição do motivo (ex: "Conquista desbloqueada: Raízes Plantadas")
    val conquistaId: String? = null
)

