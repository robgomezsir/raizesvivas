package com.raizesvivas.core.domain.usecase.member

import com.raizesvivas.core.domain.model.Member
import com.raizesvivas.core.domain.repository.MemberRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case para obter membros
 * 
 * Obtém todos os membros do usuário.
 */
class GetMembersUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    
    operator fun invoke(userId: String): Flow<List<Member>> {
        return memberRepository.getAllMembers(userId)
    }
}
