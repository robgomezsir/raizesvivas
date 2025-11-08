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
    HISTORIA("História", "📚"),
    CONEXOES("Conexões", "🔗"),
    EXPLORADOR("Explorador", "🌳"),
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
    ADICIONAR_MEMBROS, // Adicionar X membros
    ADICIONAR_FOTOS, // Adicionar fotos para X membros
    COMPLETAR_MEMBROS, // Completar dados de X membros
    REGISTRAR_CASAMENTOS, // Registrar X casamentos
    MAPEAR_GERACOES, // Mapear X gerações
    CRIAR_SUBFAMILIAS, // Criar X subfamílias
    DESCOBRIR_PARENTESCO_DISTANTE, // Descobrir parentesco de X grau
    ADICIONAR_MEMBROS_TOTAL, // Adicionar X membros no total
    MAPEAR_ANOS, // Mapear X anos de história
    MEMBRO_IDADE, // Membro com mais de X anos
    MAPEAR_GERACOES_TOTAL, // Mapear X gerações no total
    CRIAR_FAMILIA_ZERO, // Criar Família Zero (conquista instantânea)
    VISUALIZAR_FLORESTA // Visualizar floresta pela primeira vez (conquista instantânea)
}

/**
 * Progresso do usuário em uma conquista
 */
data class ProgressoConquista(
    val conquistaId: String,
    val desbloqueada: Boolean,
    val desbloqueadaEm: java.util.Date?,
    val progressoAtual: Int,
    val progressoTotal: Int
)

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

