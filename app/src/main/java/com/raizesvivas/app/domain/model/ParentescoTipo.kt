package com.raizesvivas.app.domain.model

/**
 * Enum representando todos os tipos possíveis de parentesco
 * 
 * Este enum é usado pelo algoritmo de cálculo de parentesco
 * para determinar a relação entre duas pessoas.
 */
enum class ParentescoTipo(val descricao: String, val grau: Int) {
    // Grau 0 - Mesma pessoa
    EU("Você mesmo", 0),
    
    // Grau 1 - Relações diretas
    PAI("Pai", 1),
    MAE("Mãe", 1),
    FILHO("Filho", 1),
    FILHA("Filha", 1),
    CONJUGE("Cônjuge", 1),
    EX_CONJUGE("Ex-cônjuge", 1),
    
    // Grau 2 - Irmãos e avós
    IRMAO("Irmão", 2),
    IRMA("Irmã", 2),
    MEIO_IRMAO("Meio-irmão", 2),
    MEIA_IRMA("Meia-irmã", 2),
    AVO_PATERNO("Avô paterno", 2),
    AVO_PATERNA("Avó paterna", 2),
    AVO_MATERNO("Avô materno", 2),
    AVO_MATERNA("Avó materna", 2),
    NETO("Neto", 2),
    NETA("Neta", 2),
    
    // Grau 3 - Tios e sobrinhos
    TIO_PATERNO("Tio paterno", 3),
    TIA_PATERNA("Tia paterna", 3),
    TIO_MATERNO("Tio materno", 3),
    TIA_MATERNA("Tia materna", 3),
    SOBRINHO("Sobrinho", 3),
    SOBRINHA("Sobrinha", 3),
    BISNETO("Bisneto", 3),
    BISNETA("Bisneta", 3),
    BISAVO("Bisavô", 3),
    BISAVO_F("Bisavó", 3),
    
    // Grau 4 - Primos
    PRIMO("Primo", 4),
    PRIMA("Prima", 4),
    
    // Por afinidade (casamento)
    SOGRO("Sogro", 2),
    SOGRA("Sogra", 2),
    GENRO("Genro", 2),
    NORA("Nora", 2),
    CUNHADO("Cunhado", 2),
    CUNHADA("Cunhada", 2),
    PADRASTO("Padrasto", 2),
    MADRASTA("Madrasta", 2),
    ENTEADO("Enteado", 2),
    ENTEADA("Enteada", 2),
    
    // Não identificado
    PARENTE_DISTANTE("Parente distante", 99),
    DESCONHECIDO("Relação desconhecida", 999);
    
    /**
     * Retorna descrição formatada para exibição
     */
    fun getDescricaoFormatada(nomePessoa: String): String {
        return "$nomePessoa é seu(sua) $descricao"
    }
}

