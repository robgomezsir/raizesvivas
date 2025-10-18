package com.raizesvivas.core.data.mapper

import com.raizesvivas.core.data.entity.FamilyEntity
import com.raizesvivas.core.domain.model.Family
import com.raizesvivas.core.domain.model.FamilyType

/**
 * Mapper para converter entre FamilyEntity e Family
 */
object FamilyMapper {
    
    /**
     * Converte FamilyEntity para Family
     */
    fun toDomain(entity: FamilyEntity): Family {
        return Family(
            id = entity.id,
            nome = entity.nome,
            tipo = FamilyType.fromValue(entity.tipo),
            familiaPaiId = entity.familiaPaiId,
            criadaPorCasamento = entity.criadaPorCasamento,
            membroOrigem1Id = entity.membroOrigem1Id,
            membroOrigem2Id = entity.membroOrigem2Id,
            dataCriacao = entity.dataCriacao,
            iconeArvore = entity.iconeArvore,
            nivelHierarquico = entity.nivelHierarquico,
            ativa = entity.ativa,
            userId = entity.userId,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
    
    /**
     * Converte Family para FamilyEntity
     */
    fun toEntity(domain: Family): FamilyEntity {
        return FamilyEntity(
            id = domain.id,
            nome = domain.nome,
            tipo = domain.tipo.value,
            familiaPaiId = domain.familiaPaiId,
            criadaPorCasamento = domain.criadaPorCasamento,
            membroOrigem1Id = domain.membroOrigem1Id,
            membroOrigem2Id = domain.membroOrigem2Id,
            dataCriacao = domain.dataCriacao,
            iconeArvore = domain.iconeArvore,
            nivelHierarquico = domain.nivelHierarquico,
            ativa = domain.ativa,
            userId = domain.userId,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
