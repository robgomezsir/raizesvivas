package com.raizesvivas.core.domain.usecase.member

import com.raizesvivas.core.domain.model.Member
import com.raizesvivas.core.domain.repository.MemberRepository
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * Use Case para adicionar membro
 * 
 * Adiciona um novo membro à família do usuário.
 */
class AddMemberUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    
    suspend operator fun invoke(
        userId: String,
        nomeCompleto: String,
        nomeAbreviado: String? = null,
        dataNascimento: String? = null,
        localNascimento: String? = null,
        profissao: String? = null,
        observacoes: String? = null
    ): Result<Member> {
        return try {
            // Validar dados obrigatórios
            if (nomeCompleto.isBlank()) {
                return Result.failure(
                    InvalidMemberDataException("Nome completo é obrigatório")
                )
            }
            
            // Criar novo membro
            val member = Member(
                id = UUID.randomUUID().toString(),
                nomeCompleto = nomeCompleto.trim(),
                nomeAbreviado = nomeAbreviado?.trim(),
                dataNascimento = parseDate(dataNascimento),
                localNascimento = localNascimento?.trim(),
                profissao = profissao?.trim(),
                observacoes = observacoes?.trim(),
                nivelNaArvore = 0,
                ativo = true,
                userId = userId,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            
            memberRepository.addMember(member)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun parseDate(dateString: String?): java.time.LocalDate? {
        if (dateString.isNullOrBlank()) return null
        
        return try {
            java.time.LocalDate.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Exceção para dados inválidos de membro
 */
class InvalidMemberDataException(message: String) : Exception(message)
