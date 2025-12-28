package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.AuditLog
import com.raizesvivas.app.domain.model.TipoAcaoAudit
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditLogRepository @Inject constructor(
    private val firestoreService: FirestoreService,
    private val authService: AuthService,
    private val usuarioRepository: UsuarioRepository
) {
    
    /**
     * Registra uma ação de auditoria
     */
    suspend fun registrarAcao(
        acao: TipoAcaoAudit,
        entidade: String,
        entidadeId: String,
        entidadeNome: String,
        detalhes: String
    ): Result<Unit> {
        return try {
            val usuarioId = authService.currentUser?.uid
            if (usuarioId == null) {
                Timber.w("⚠️ Tentativa de registrar log sem usuário autenticado")
                return Result.success(Unit) // Não falhar, apenas não registrar
            }
            
            // Buscar informações do usuário
            val usuario = usuarioRepository.buscarPorId(usuarioId)
            
            val log = AuditLog(
                id = "", // Será gerado pelo Firestore
                usuarioId = usuarioId,
                usuarioNome = usuario?.nome ?: "Usuário Desconhecido",
                usuarioEmail = usuario?.email ?: authService.currentUser?.email ?: "",
                acao = acao,
                entidade = entidade,
                entidadeId = entidadeId,
                entidadeNome = entidadeNome,
                detalhes = detalhes,
                timestamp = Date(),
                ipAddress = null, // TODO: Capturar IP se necessário
                deviceInfo = android.os.Build.MODEL // Informação do dispositivo
            )
            
            firestoreService.registrarAuditLog(log)
            Timber.d("📝 Log de auditoria registrado: ${acao.name} - $entidadeNome")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao registrar log de auditoria")
            // Não falhar a operação principal por causa do log
            Result.success(Unit)
        }
    }
    
    /**
     * Busca todos os logs de auditoria (limitado aos últimos 100)
     */
    suspend fun buscarLogs(limit: Int = 100): Result<List<AuditLog>> {
        return firestoreService.buscarAuditLogs(limit)
    }
    
    /**
     * Observa logs de auditoria em tempo real
     */
    fun observarLogs(limit: Int = 100): Flow<List<AuditLog>> {
        return firestoreService.observarAuditLogs(limit)
    }
    
    /**
     * Busca logs por usuário específico
     */
    suspend fun buscarLogsPorUsuario(usuarioId: String, limit: Int = 100): Result<List<AuditLog>> {
        return firestoreService.buscarAuditLogsPorUsuario(usuarioId, limit)
    }
    
    /**
     * Busca logs por tipo de ação
     */
    suspend fun buscarLogsPorAcao(acao: TipoAcaoAudit, limit: Int = 100): Result<List<AuditLog>> {
        return firestoreService.buscarAuditLogsPorAcao(acao, limit)
    }
}
