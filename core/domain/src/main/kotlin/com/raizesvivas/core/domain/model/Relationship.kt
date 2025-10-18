package com.raizesvivas.core.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Modelo de relacionamento do domínio
 * 
 * Representa um relacionamento familiar entre dois membros
 * no sistema Raízes Vivas.
 */
data class Relationship(
    val id: String,
    val membro1Id: String,
    val membro2Id: String,
    val tipoRelacionamento: RelationshipType,
    val dataInicio: LocalDate? = null,
    val dataFim: LocalDate? = null,
    val observacoes: String? = null,
    val ativo: Boolean = true,
    val userId: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Tipos de relacionamento familiar
 */
enum class RelationshipType(val value: String, val description: String) {
    // Relacionamentos diretos (pais/filhos)
    PAI("pai", "Pai"),
    MAE("mae", "Mãe"),
    FILHO("filho", "Filho"),
    FILHA("filha", "Filha"),
    
    // Relacionamentos colaterais (irmãos)
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
    NORA("nora", "Nora");
    
    companion object {
        fun fromValue(value: String): RelationshipType {
            return values().find { it.value == value }
                ?: throw IllegalArgumentException("Tipo de relacionamento inválido: $value")
        }
    }
}
