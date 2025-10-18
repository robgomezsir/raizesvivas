package com.raizesvivas.core.data.mapper

import com.raizesvivas.core.data.entity.KinshipEntity
import com.raizesvivas.core.domain.model.Kinship
import com.raizesvivas.core.domain.model.KinshipDegree
import com.raizesvivas.core.domain.model.KinshipType

/**
 * Mapper para converter entre KinshipEntity e Kinship
 */
object KinshipMapper {
    
    /**
     * Converte KinshipEntity para Kinship
     */
    fun toDomain(entity: KinshipEntity): Kinship {
        return Kinship(
            id = entity.id,
            membro1Id = entity.membro1Id,
            membro2Id = entity.membro2Id,
            tipoParentesco = KinshipType.fromValue(entity.tipoParentesco),
            grauParentesco = KinshipDegree.fromValue(entity.grauParentesco),
            distanciaGeracional = entity.distanciaGeracional,
            familiaReferenciaId = entity.familiaReferenciaId,
            dataCalculo = entity.dataCalculo,
            ativo = entity.ativo,
            userId = entity.userId,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
    
    /**
     * Converte Kinship para KinshipEntity
     */
    fun toEntity(domain: Kinship): KinshipEntity {
        return KinshipEntity(
            id = domain.id,
            membro1Id = domain.membro1Id,
            membro2Id = domain.membro2Id,
            tipoParentesco = domain.tipoParentesco.value,
            grauParentesco = domain.grauParentesco.value,
            distanciaGeracional = domain.distanciaGeracional,
            familiaReferenciaId = domain.familiaReferenciaId,
            dataCalculo = domain.dataCalculo,
            ativo = domain.ativo,
            userId = domain.userId,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
