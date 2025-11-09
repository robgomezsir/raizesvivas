package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.Recado
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gerenciar recados do mural comunitário
 * 
 * Coordena operações de criação, leitura e remoção de recados
 */
@Singleton
class RecadoRepository @Inject constructor(
    private val firestoreService: FirestoreService,
    private val authService: AuthService,
    private val usuarioRepository: UsuarioRepository,
    private val pessoaRepository: PessoaRepository
) {
    
    /**
     * Observa todos os recados (gerais e direcionados ao usuário atual)
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarRecados(): Flow<List<Recado>> {
        val currentUser = authService.currentUser
        return if (currentUser != null) {
            // Observar pessoa vinculada do usuário e usar para filtrar recados
            usuarioRepository.observarPorId(currentUser.uid)
                .flatMapLatest { usuario ->
                    val pessoaVinculadaId = usuario?.pessoaVinculada
                    // Se o usuário tem pessoa vinculada, usar o ID da pessoa para filtrar
                    // Caso contrário, usar o userId como fallback
                    val filtroId = pessoaVinculadaId ?: currentUser.uid
                    Timber.d("👀 Observando recados com filtroId: $filtroId (pessoaVinculada: $pessoaVinculadaId), userId: ${currentUser.uid}")
                    firestoreService.observarRecados(filtroId, currentUser.uid)
                }
                .catch { error: Throwable ->
                    Timber.e(error, "❌ Erro no fluxo de observação de recados: %s", error.message)
                    emit(emptyList())
                }
        } else {
            Timber.w("⚠️ Usuário não autenticado, retornando lista vazia de recados")
            flow { emit(emptyList()) }
        }
    }
    
    /**
     * Busca todos os recados
     */
    suspend fun buscarRecados(): Result<List<Recado>> {
        return try {
            val currentUser = authService.currentUser
            if (currentUser != null) {
                // Buscar pessoa vinculada do usuário para filtrar recados direcionados
                val usuario = usuarioRepository.buscarPorId(currentUser.uid)
                val pessoaVinculadaId = usuario?.pessoaVinculada
                
                // Se o usuário tem pessoa vinculada, usar o ID da pessoa para filtrar
                // Caso contrário, apenas mostrar recados gerais
                val filtroId = pessoaVinculadaId ?: currentUser.uid
                firestoreService.buscarRecados(filtroId, currentUser.uid)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar recados")
            Result.failure(e)
        }
    }
    
    /**
     * Cria um novo recado
     */
    suspend fun criarRecado(
        titulo: String,
        mensagem: String,
        destinatarioId: String? = null,
        cor: String = "primary"
    ): Result<Recado> {
        return try {
            val currentUser = authService.currentUser
            if (currentUser == null) {
                Timber.e("❌ Usuário não autenticado ao tentar criar recado")
                return Result.failure(Exception("Usuário não autenticado"))
            }
            
            Timber.d("👤 Criando recado para usuário: ${currentUser.uid}")
            
            // Buscar nome do autor
            val usuario = usuarioRepository.buscarPorId(currentUser.uid)
            val autorNome = usuario?.nome?.takeIf { it.isNotBlank() } ?: "Usuário"
            
            Timber.d("👤 Autor: $autorNome")
            
            // Buscar nome do destinatário se houver (destinatarioId é o ID da pessoa)
            var destinatarioNome: String? = null
            if (destinatarioId != null && destinatarioId.isNotBlank()) {
                val pessoa = pessoaRepository.buscarPorId(destinatarioId)
                destinatarioNome = pessoa?.nome
                Timber.d("📨 Destinatário: $destinatarioNome (ID: $destinatarioId)")
            } else {
                Timber.d("📨 Recado geral (sem destinatário)")
            }
            
            val recado = Recado(
                autorId = currentUser.uid,
                autorNome = autorNome,
                destinatarioId = destinatarioId?.takeIf { it.isNotBlank() },
                destinatarioNome = destinatarioNome?.takeIf { it.isNotBlank() },
                titulo = titulo.trim(),
                mensagem = mensagem.trim(),
                cor = cor
            )
            
            Timber.d("💾 Chamando firestoreService.salvarRecado...")
            val resultado = firestoreService.salvarRecado(recado)
            
            resultado.onSuccess {
                Timber.d("✅ Recado criado com sucesso no repository: ${it.id}")
            }.onFailure { error ->
                Timber.e(error, "❌ Falha ao salvar recado no Firestore")
            }
            
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Exceção ao criar recado no repository: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Deleta um recado definitivamente do banco de dados
     * Admins podem deletar todos os recados
     * IMPORTANTE: Esta é uma exclusão permanente (hard delete)
     */
    suspend fun deletarRecado(recadoId: String): Result<Unit> {
        return try {
            val currentUser = authService.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuário não autenticado"))
            }
            
            // Verificar se é admin
            val usuario = usuarioRepository.buscarPorId(currentUser.uid)
            val isAdmin = usuario?.ehAdministrador == true
            
            firestoreService.deletarRecado(recadoId, currentUser.uid, isAdmin)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao deletar recado")
            Result.failure(e)
        }
    }
    
    /**
     * Atualiza um recado
     * Admins podem editar todos os recados
     */
    suspend fun atualizarRecado(recado: Recado): Result<Recado> {
        return try {
            val currentUser = authService.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuário não autenticado"))
            }
            
            // Verificar se é admin
            val usuario = usuarioRepository.buscarPorId(currentUser.uid)
            val isAdmin = usuario?.ehAdministrador == true
            
            // Verificar se o usuário é o autor ou é admin
            if (recado.autorId != currentUser.uid && !isAdmin) {
                return Result.failure(Exception("Apenas o autor ou um administrador pode editar o recado"))
            }
            
            firestoreService.atualizarRecado(recado)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao atualizar recado")
            Result.failure(e)
        }
    }
    
    /**
     * Fixa ou desfixa um recado (apenas admin)
     */
    suspend fun fixarRecado(
        recadoId: String,
        fixado: Boolean,
        fixadoAte: Date? = null
    ): Result<Unit> {
        return try {
            val currentUser = authService.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuário não autenticado"))
            }
            
            // Verificar se é admin
            val usuario = usuarioRepository.buscarPorId(currentUser.uid)
            if (usuario?.ehAdministrador != true) {
                return Result.failure(Exception("Apenas administradores podem fixar recados"))
            }
            
            firestoreService.fixarRecado(recadoId, fixado, fixadoAte, currentUser.uid)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao fixar recado")
            Result.failure(e)
        }
    }
    
    /**
     * Adiciona ou remove apoio familiar (curtida) de um recado
     */
    suspend fun curtirRecado(recadoId: String, curtir: Boolean): Result<Unit> {
        return try {
            val currentUser = authService.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuário não autenticado"))
            }
            
            firestoreService.curtirRecado(recadoId, currentUser.uid, curtir)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao ${if (curtir) "curtir" else "descurtir"} recado")
            Result.failure(e)
        }
    }
}

