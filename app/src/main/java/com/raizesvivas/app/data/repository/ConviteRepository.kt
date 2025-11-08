package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.Convite
import com.raizesvivas.app.domain.model.StatusConvite
import com.raizesvivas.app.domain.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gerenciar convites
 * 
 * Coordena operações de envio, aceitação e gerenciamento de convites
 */
@Singleton
class ConviteRepository @Inject constructor(
    private val firestoreService: FirestoreService,
    private val authService: AuthService
) {
    
    /**
     * Observa convites pendentes para o usuário atual
     */
    fun observarConvitesPendentes(): Flow<List<Convite>> {
        val currentUser = authService.currentUser
        return if (currentUser != null) {
            firestoreService.observarConvitesPendentes(currentUser.email ?: "")
        } else {
            flow { emit(emptyList()) }
        }
    }
    
    /**
     * Busca convites pendentes por email
     */
    suspend fun buscarConvitesPendentes(email: String): Result<List<Convite>> {
        return try {
            val resultado = firestoreService.buscarConvitesPorEmail(email)
            resultado.onSuccess { convites ->
                // Filtrar apenas convites válidos (não expirados)
                val validos = convites.filter { it.estaValido }
                if (validos.size < convites.size) {
                    Timber.d("⚠️ ${convites.size - validos.size} convites expirados encontrados")
                }
                return Result.success(validos)
            }
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar convites pendentes")
            Result.failure(e)
        }
    }
    
    /**
     * Busca todos os convites (apenas para admin)
     */
    suspend fun buscarTodosConvites(): Result<List<Convite>> {
        return try {
            firestoreService.buscarTodosConvites()
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar todos os convites")
            Result.failure(e)
        }
    }
    
    /**
     * Cria um novo convite (apenas admin)
     */
    suspend fun criarConvite(
        emailConvidado: String,
        pessoaVinculada: String? = null
    ): Result<Convite> {
        return try {
            val currentUser = authService.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuário não autenticado"))
            }
            
            // Verificar se é admin (deve ser feito no ViewModel/UseCase)
            // Por enquanto, assumimos que a verificação já foi feita
            
            val convite = Convite(
                id = UUID.randomUUID().toString(),
                emailConvidado = emailConvidado,
                convidadoPor = currentUser.uid,
                pessoaVinculada = pessoaVinculada,
                status = StatusConvite.PENDENTE,
                criadoEm = Date(),
                expiraEm = Date(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)) // 7 dias
            )
            
            val resultado = firestoreService.criarConvite(convite)
            
            resultado.onSuccess {
                Timber.d("✅ Convite criado: ${convite.id} para $emailConvidado")
            }
            
            resultado.map { convite }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar convite")
            Result.failure(e)
        }
    }
    
    /**
     * Aceita um convite
     */
    suspend fun aceitarConvite(
        conviteId: String,
        pessoaId: String? = null
    ): Result<Unit> {
        return try {
            val currentUser = authService.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuário não autenticado"))
            }
            
            // Buscar convite para validar
            val conviteResult = firestoreService.buscarConvite(conviteId)
            val convite = conviteResult.getOrNull()
            
            if (convite == null) {
                return Result.failure(Exception("Convite não encontrado"))
            }
            
            if (!convite.estaValido) {
                return Result.failure(Exception("Convite expirado ou inválido"))
            }
            
            if (convite.emailConvidado != currentUser.email) {
                return Result.failure(Exception("Convite não é para este usuário"))
            }
            
            // Aceitar convite
            val resultado = firestoreService.aceitarConvite(
                conviteId = conviteId,
                userId = currentUser.uid,
                pessoaId = pessoaId ?: convite.pessoaVinculada
            )
            
            resultado.onSuccess {
                Timber.d("✅ Convite $conviteId aceito por ${currentUser.uid}")
            }
            
            resultado
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao aceitar convite")
            Result.failure(e)
        }
    }
    
    /**
     * Rejeita um convite
     */
    suspend fun rejeitarConvite(conviteId: String): Result<Unit> {
        return try {
            val currentUser = authService.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuário não autenticado"))
            }
            
            // Buscar convite para validar
            val conviteResult = firestoreService.buscarConvite(conviteId)
            val convite = conviteResult.getOrNull()
            
            if (convite == null) {
                return Result.failure(Exception("Convite não encontrado"))
            }
            
            if (convite.emailConvidado != currentUser.email) {
                return Result.failure(Exception("Convite não é para este usuário"))
            }
            
            firestoreService.rejeitarConvite(conviteId)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao rejeitar convite")
            Result.failure(e)
        }
    }
    
    /**
     * Deleta convite (apenas admin)
     */
    suspend fun deletarConvite(conviteId: String): Result<Unit> {
        return try {
            firestoreService.deletarConvite(conviteId)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao deletar convite")
            Result.failure(e)
        }
    }
    
    /**
     * Marca convites expirados
     */
    suspend fun marcarConvitesExpirados(): Result<Int> {
        return try {
            val resultado = firestoreService.buscarTodosConvites()
            
            resultado.onSuccess { convites ->
                val expirados = convites.filter { 
                    it.expirou && it.status == StatusConvite.PENDENTE 
                }
                
                expirados.forEach { convite ->
                    firestoreService.atualizarStatusConvite(
                        convite.id,
                        StatusConvite.EXPIRADO
                    )
                }
                
                if (expirados.isNotEmpty()) {
                    Timber.d("⚠️ ${expirados.size} convites marcados como expirados")
                }
                
                return Result.success(expirados.size)
            }
            
            resultado.map { 0 }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao marcar convites expirados")
            Result.failure(e)
        }
    }
}

