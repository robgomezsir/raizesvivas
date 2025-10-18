package com.raizesvivas.core.data.mapper

import com.raizesvivas.core.data.entity.RelationshipEntity
import com.raizesvivas.core.domain.model.Relationship
import com.raizesvivas.core.domain.model.RelationshipType

/**
 * Mapper para converter entre RelationshipEntity e Relationship
 */
object RelationshipMapper {
    
    /**
     * Converte RelationshipEntity para Relationship
     */
    fun toDomain(entity: RelationshipEntity): Relationship {
        return Relationship(
            id = entity.id,
            membro1Id = entity.membro1Id,
            membro2Id = entity.membro2Id,
            tipoRelacionamento = RelationshipType.fromValue(entity.tipoRelacionamento),
            dataInicio = entity.dataInicio,
            dataFim = entity.dataFim,
            observacoes = entity.observacoes,
            ativo = entity.ativo,
            userId = entity.userId,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
    
    /**
     * Converte Relationship para RelationshipEntity
     */
    fun toEntity(domain: Relationship): RelationshipEntity {
        return RelationshipEntity(
            id = domain.id,
            membro1Id = domain.membro1Id,
            membro2Id = domain.membro2Id,
            tipoRelacionamento = domain.tipoRelacionamento.value,
            dataInicio = domain.dataInicio,
            dataFim = domain.dataFim,
            observacoes = domain.observacoes,
            ativo = domain.ativo,
            userId = domain.userId,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
