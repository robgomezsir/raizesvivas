package com.raizesvivas.core.data.source.remote

import com.raizesvivas.core.domain.model.User
import com.raizesvivas.core.domain.repository.AuthRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.gotrue.AuthResponse
import io.github.jan.supabase.gotrue.user.UserInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cliente de autenticação Supabase
 * 
 * Implementa o AuthRepository usando o Supabase Auth
 * para gerenciar autenticação de usuários.
 */
@Singleton
class SupabaseAuthClient @Inject constructor() : AuthRepository {
    
    private val auth = SupabaseClient.client.auth
    
    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val response = auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            Result.success(response.user.toDomainUser())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun signUp(email: String, password: String): Result<User> {
        return try {
            val response = auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            Result.success(response.user.toDomainUser())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val user = auth.currentUserOrNull()
            Result.success(user?.toDomainUser())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun isSignedIn(): Boolean {
        return auth.currentUserOrNull() != null
    }
    
    /**
     * Converte UserInfo do Supabase para User do domínio
     */
    private fun UserInfo.toDomainUser(): User {
        return User(
            id = id,
            email = email ?: "",
            name = userMetadata?.get("name") as? String ?: "",
            createdAt = createdAt ?: "",
            lastSignInAt = lastSignInAt ?: ""
        )
    }
}
