package com.raizesvivas.core.domain.model

import java.time.LocalDateTime

/**
 * Modelo de parentesco do domínio
 * 
 * Representa o resultado do cálculo de parentesco
 * entre dois membros no sistema Raízes Vivas.
 */
data class Kinship(
    val id: String,
    val membro1Id: String,
    val membro2Id: String,
    val tipoParentesco: KinshipType,
    val grauParentesco: KinshipDegree,
    val distanciaGeracional: Int,
    val familiaReferenciaId: String,
    val dataCalculo: LocalDateTime,
    val ativo: Boolean = true,
    val userId: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Tipos de parentesco calculado
 */
enum class KinshipType(val value: String, val description: String) {
    // Relacionamentos diretos
    PAI("pai", "Pai"),
    MAE("mae", "Mãe"),
    FILHO("filho", "Filho"),
    FILHA("filha", "Filha"),
    
    // Relacionamentos colaterais
    IRMAO("irmao", "Irmão"),
    IRMA("irma", "Irmã"),
    
    // Relacionamentos avós/netos
    AVO("avo", "Avô"),
    AVO_FEMININO("avó", "Avó"),
    NETO("neto", "Neto"),
    NETA("neta", "Neta"),
    
    // Relacionamentos tios/sobrinhos
    TIO("tio", "Tio"),
    TIA("tia", "Tia"),
    SOBRINHO("sobrinho", "Sobrinho"),
    SOBRINHA("sobrinha", "Sobrinha"),
    
    // Relacionamentos primos
    PRIMO("primo", "Primo"),
    PRIMA("prima", "Prima"),
    
    // Relacionamentos conjugais
    ESPOSO("esposo", "Esposo"),
    ESPOSA("esposa", "Esposa"),
    
    // Relacionamentos por casamento
    CUNHADO("cunhado", "Cunhado"),
    CUNHADA("cunhada", "Cunhada"),
    SOGRO("sogro", "Sogro"),
    SOGRA("sogra", "Sogra"),
    GENRO("genro", "Genro"),
    NORA("nora", "Nora"),
    
    // Relacionamentos distantes
    BISAVO("bisavo", "Bisavô"),
    BISAVO_FEMININO("bisavó", "Bisavó"),
    BISNETO("bisneto", "Bisneto"),
    BISNETA("bisneta", "Bisneta"),
    TIO_AVO("tio_avo", "Tio-avô"),
    TIA_AVO("tia_avó", "Tia-avó"),
    SOBRINHO_NETO("sobrinho_neto", "Sobrinho-neto"),
    SOBRINHA_NETA("sobrinha_neta", "Sobrinha-neta"),
    PRIMO_SEGUNDO("primo_segundo", "Primo segundo"),
    PRIMA_SEGUNDA("prima_segunda", "Prima segunda");
    
    companion object {
        fun fromValue(value: String): KinshipType {
            return values().find { it.value == value }
                ?: throw IllegalArgumentException("Tipo de parentesco inválido: $value")
        }
    }
}

/**
 * Graus de parentesco
 */
enum class KinshipDegree(val value: Int, val description: String) {
    ZERO(0, "Mesmo indivíduo"),
    FIRST(1, "Primeiro grau"),
    SECOND(2, "Segundo grau"),
    THIRD(3, "Terceiro grau"),
    FOURTH(4, "Quarto grau"),
    DISTANT(5, "Parentesco distante");
    
    companion object {
        fun fromValue(value: Int): KinshipDegree {
            return values().find { it.value == value }
                ?: DISTANT
        }
    }
}
