package com.raizesvivas.core.data.mapper

import com.raizesvivas.core.data.entity.MemberEntity
import com.raizesvivas.core.domain.model.Member
import com.raizesvivas.core.domain.model.TreeElement
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Mapper para converter entre MemberEntity e Member
 */
object MemberMapper {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Converte MemberEntity para Member
     */
    fun toDomain(entity: MemberEntity): Member {
        return Member(
            id = entity.id,
            nomeCompleto = entity.nomeCompleto,
            nomeAbreviado = entity.nomeAbreviado,
            dataNascimento = entity.dataNascimento,
            dataFalecimento = entity.dataFalecimento,
            localNascimento = entity.localNascimento,
            localFalecimento = entity.localFalecimento,
            profissao = entity.profissao,
            observacoes = entity.observacoes,
            fotoUrl = entity.fotoUrl,
            elementosVisuais = parseTreeElements(entity.elementosVisuais),
            nivelNaArvore = entity.nivelNaArvore,
            posicaoX = entity.posicaoX,
            posicaoY = entity.posicaoY,
            ativo = entity.ativo,
            userId = entity.userId,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
    
    /**
     * Converte Member para MemberEntity
     */
    fun toEntity(domain: Member): MemberEntity {
        return MemberEntity(
            id = domain.id,
            nomeCompleto = domain.nomeCompleto,
            nomeAbreviado = domain.nomeAbreviado,
            dataNascimento = domain.dataNascimento,
            dataFalecimento = domain.dataFalecimento,
            localNascimento = domain.localNascimento,
            localFalecimento = domain.localFalecimento,
            profissao = domain.profissao,
            observacoes = domain.observacoes,
            fotoUrl = domain.fotoUrl,
            elementosVisuais = serializeTreeElements(domain.elementosVisuais),
            nivelNaArvore = domain.nivelNaArvore,
            posicaoX = domain.posicaoX,
            posicaoY = domain.posicaoY,
            ativo = domain.ativo,
            userId = domain.userId,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
    
    /**
     * Parse dos elementos visuais do JSON
     */
    private fun parseTreeElements(jsonString: String?): List<TreeElement> {
        if (jsonString.isNullOrBlank()) return emptyList()
        
        return try {
            val elements = json.decodeFromString<List<String>>(jsonString)
            elements.mapNotNull { value ->
                try {
                    TreeElement.fromValue(value)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Serializa os elementos visuais para JSON
     */
    private fun serializeTreeElements(elements: List<TreeElement>): String? {
        if (elements.isEmpty()) return null
        
        return try {
            val values = elements.map { it.value }
            json.encodeToString(values)
        } catch (e: Exception) {
            null
        }
    }
}
