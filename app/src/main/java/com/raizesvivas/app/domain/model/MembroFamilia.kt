package com.raizesvivas.app.domain.model

/**
 * Modelo representando a relação de um membro com uma família
 * 
 * Um membro pode pertencer a múltiplas famílias com papéis diferentes:
 * - Na Família Zero: pode ser "filho"
 * - Na Subfamília X: pode ser "pai"
 */
data class MembroFamilia(
    val id: String = "", // ID único desta relação
    val membroId: String = "", // ID da pessoa
    val familiaId: String = "", // ID da família (Família Zero ou Subfamília)
    val papelNaFamilia: PapelFamilia = PapelFamilia.FILHO,
    val elementoNestaFamilia: ElementoArvore = ElementoArvore.GALHO,
    val geracaoNaFamilia: Int = 0 // Geração nesta família específica (0 = referência da família)
) {
    /**
     * Verifica se o membro é fundador da família
     */
    val ehFundador: Boolean
        get() = papelNaFamilia == PapelFamilia.PAI || papelNaFamilia == PapelFamilia.MAE
}

/**
 * Enum para papéis na família
 */
enum class PapelFamilia(val descricao: String) {
    PAI("Pai"),
    MAE("Mãe"),
    FILHO("Filho"),
    FILHA("Filha"),
    AVO_PATERNO("Avô Paterno"),
    AVO_PATERNA("Avó Paterna"),
    AVO_MATERNO("Avô Materno"),
    AVO_MATERNA("Avó Materna"),
    BISAVO("Bisavô"),
    BISAVO_F("Bisavó"),
    OUTRO("Outro")
}

/**
 * Enum para elementos da árvore (metáfora botânica)
 */
enum class ElementoArvore(val descricao: String, val cor: String) {
    RAIZ("Raízes", "#5D4037"), // Bisavós e anteriores
    CASCA("Casca", "#8D6E63"), // Avós
    CAULE("Caule", "#A1887F"), // Pais
    GALHO("Galhos", "#689F38"), // Filhos
    FOLHA("Folhas", "#8BC34A"), // Netos
    FLOR("Flores", "#E91E63"), // Bisnetos e posteriores
    POLINIZADOR("Polinizador", "#FFA726"), // Cônjuges
    PASSARO("Pássaro", "#42A5F5"), // Amigos da família
    OUTRO("Outro", "#9E9E9E") // Outros membros
}
