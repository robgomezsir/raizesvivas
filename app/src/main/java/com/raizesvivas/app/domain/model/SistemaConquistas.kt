package com.raizesvivas.app.domain.model

/**
 * Sistema de Conquistas pré-definidas
 * 
 * Contém todas as conquistas disponíveis no sistema
 */
object SistemaConquistas {
    
    /**
     * Todas as conquistas do sistema
     */
    fun obterTodas(): List<Conquista> {
        return listOf(
            // CATEGORIA: HISTÓRIA
            Conquista(
                id = "raizes_plantadas",
                nome = "Raízes Plantadas",
                descricao = "Crie sua primeira família",
                categoria = CategoriaConquista.HISTORIA,
                recompensaXP = 50,
                icone = "🌱",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.CRIAR_FAMILIA_ZERO,
                    valor = 1,
                    descricaoCondicao = "Criar Família Zero"
                ),
                ordem = 1
            ),
            Conquista(
                id = "cronista_familiar",
                nome = "Cronista Familiar",
                descricao = "Adicione 10 membros",
                categoria = CategoriaConquista.HISTORIA,
                recompensaXP = 100,
                icone = "📝",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_MEMBROS,
                    valor = 10,
                    descricaoCondicao = "Adicionar 10 membros"
                ),
                ordem = 2
            ),
            Conquista(
                id = "guardiao_memoria",
                nome = "Guardião da Memória",
                descricao = "Adicione fotos para 20 membros",
                categoria = CategoriaConquista.HISTORIA,
                recompensaXP = 150,
                icone = "📸",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_FOTOS,
                    valor = 20,
                    descricaoCondicao = "Adicionar fotos para 20 membros"
                ),
                ordem = 3
            ),
            Conquista(
                id = "historiador",
                nome = "Historiador",
                descricao = "Complete 50 membros com todos os dados",
                categoria = CategoriaConquista.HISTORIA,
                recompensaXP = 500,
                icone = "📚",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.COMPLETAR_MEMBROS,
                    valor = 50,
                    descricaoCondicao = "Completar dados de 50 membros"
                ),
                ordem = 4
            ),
            
            // CATEGORIA: CONEXÕES
            Conquista(
                id = "cupido_genealogico",
                nome = "Cupido Genealógico",
                descricao = "Registre 5 casamentos",
                categoria = CategoriaConquista.CONEXOES,
                recompensaXP = 80,
                icone = "💒",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.REGISTRAR_CASAMENTOS,
                    valor = 5,
                    descricaoCondicao = "Registrar 5 casamentos"
                ),
                ordem = 5
            ),
            Conquista(
                id = "tecelao_lacos",
                nome = "Tecelão de Laços",
                descricao = "Mapeie 3 gerações completas",
                categoria = CategoriaConquista.CONEXOES,
                recompensaXP = 200,
                icone = "🔗",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.MAPEAR_GERACOES,
                    valor = 3,
                    descricaoCondicao = "Mapear 3 gerações completas"
                ),
                ordem = 6
            ),
            Conquista(
                id = "uniao_sagrada",
                nome = "União Sagrada",
                descricao = "Crie sua primeira subfamília",
                categoria = CategoriaConquista.CONEXOES,
                recompensaXP = 150,
                icone = "🌿",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.CRIAR_SUBFAMILIAS,
                    valor = 1,
                    descricaoCondicao = "Criar primeira subfamília"
                ),
                ordem = 7
            ),
            Conquista(
                id = "arquiteto_dinastias",
                nome = "Arquiteto de Dinastias",
                descricao = "Crie 5 subfamílias",
                categoria = CategoriaConquista.CONEXOES,
                recompensaXP = 400,
                icone = "🏛️",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.CRIAR_SUBFAMILIAS,
                    valor = 5,
                    descricaoCondicao = "Criar 5 subfamílias"
                ),
                ordem = 8
            ),
            
            // CATEGORIA: EXPLORADOR
            Conquista(
                id = "desbravador",
                nome = "Desbravador",
                descricao = "Descubra um parentesco de 5º grau ou mais distante",
                categoria = CategoriaConquista.EXPLORADOR,
                recompensaXP = 120,
                icone = "🗺️",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.DESCOBRIR_PARENTESCO_DISTANTE,
                    valor = 5,
                    descricaoCondicao = "Descobrir parentesco de 5º grau ou mais"
                ),
                ordem = 9
            ),
            Conquista(
                id = "mestre_floresta",
                nome = "Mestre da Floresta",
                descricao = "Visualize a floresta completa pela primeira vez",
                categoria = CategoriaConquista.EXPLORADOR,
                recompensaXP = 50,
                icone = "🌲",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.VISUALIZAR_FLORESTA,
                    valor = 1, // Conquista instantânea ao visualizar floresta
                    descricaoCondicao = "Visualizar floresta completa"
                ),
                ordem = 10
            ),
            Conquista(
                id = "colecionador_historias",
                nome = "Colecionador de Histórias",
                descricao = "Adicione 100 membros",
                categoria = CategoriaConquista.EXPLORADOR,
                recompensaXP = 1000,
                icone = "📖",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.ADICIONAR_MEMBROS_TOTAL,
                    valor = 100,
                    descricaoCondicao = "Adicionar 100 membros"
                ),
                ordem = 11
            ),
            Conquista(
                id = "centenario",
                nome = "Centenário",
                descricao = "Mapeie 100 anos de história familiar",
                categoria = CategoriaConquista.EXPLORADOR,
                recompensaXP = 800,
                icone = "⏳",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.MAPEAR_ANOS,
                    valor = 100,
                    descricaoCondicao = "Mapear 100 anos de história"
                ),
                ordem = 12
            ),
            
            // CATEGORIA: ESPECIAIS (Raras)
            Conquista(
                id = "phoenix",
                nome = "Phoenix",
                descricao = "Registre um membro com mais de 100 anos",
                categoria = CategoriaConquista.ESPECIAIS,
                recompensaXP = 500,
                icone = "🔥",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.MEMBRO_IDADE,
                    valor = 100,
                    descricaoCondicao = "Registrar membro com mais de 100 anos"
                ),
                rara = true,
                ordem = 13
            ),
            Conquista(
                id = "raizes_profundas",
                nome = "Raízes Profundas",
                descricao = "Mapeie 7 gerações",
                categoria = CategoriaConquista.ESPECIAIS,
                recompensaXP = 1000,
                icone = "🌳",
                condicao = CondicaoConquista(
                    tipo = TipoCondicao.MAPEAR_GERACOES_TOTAL,
                    valor = 7,
                    descricaoCondicao = "Mapear 7 gerações"
                ),
                rara = true,
                ordem = 14
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

