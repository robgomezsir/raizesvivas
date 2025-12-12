package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.EventoFamilia
import com.raizesvivas.app.domain.model.TipoEventoFamilia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gerenciar eventos familiares
 * 
 * Gerencia eventos no Firestore e fornece flows observáveis
 */
@Singleton
class EventoRepository @Inject constructor(
    private val firestoreService: FirestoreService
) {
    
    /**
     * Observa todos os eventos em tempo real
     */
    fun observarTodosEventos(): Flow<List<EventoFamilia>> {
        return firestoreService.observarEventos()
            .map { eventos ->
                Timber.d("📅 Observando eventos: ${eventos.size} eventos recebidos")
                eventos
            }
            .catch { error ->
                Timber.e(error, "❌ Erro ao observar eventos")
                emit(emptyList())
            }
    }
    
    /**
     * Observa eventos próximos (próximos 30 dias)
     */
    fun observarEventosProximos(): Flow<List<EventoFamilia>> {
        return observarTodosEventos()
            .map { eventos ->
                val hoje = Date()
                val trintaDiasDepois = Calendar.getInstance().apply {
                    time = hoje
                    add(Calendar.DAY_OF_YEAR, 30)
                }.time
                
                eventos.filter { evento ->
                    evento.data.after(hoje) && evento.data.before(trintaDiasDepois)
                }.sortedBy { it.data }
            }
    }
    
    /**
     * Observa eventos de hoje
     */
    fun observarEventosHoje(): Flow<List<EventoFamilia>> {
        return observarTodosEventos()
            .map { eventos ->
                eventos.filter { it.ehHoje }
            }
    }
    
    /**
     * Busca evento por ID
     */
    suspend fun buscarPorId(eventoId: String): Result<EventoFamilia?> {
        return try {
            val evento = firestoreService.buscarEvento(eventoId).getOrNull()
            Result.success(evento)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar evento $eventoId")
            Result.failure(e)
        }
    }
    
    /**
     * Salva ou atualiza um evento
     */
    suspend fun salvar(evento: EventoFamilia): Result<Unit> {
        return try {
            firestoreService.salvarEvento(evento)
            Timber.d("✅ Evento salvo: ${evento.titulo}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao salvar evento")
            Result.failure(e)
        }
    }
    
    /**
     * Deleta um evento
     */
    suspend fun deletar(eventoId: String): Result<Unit> {
        return try {
            firestoreService.deletarEvento(eventoId)
            Timber.d("✅ Evento deletado: $eventoId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao deletar evento")
            Result.failure(e)
        }
    }
    
    /**
     * Busca eventos de uma pessoa específica
     */
    fun observarEventosPorPessoa(pessoaId: String): Flow<List<EventoFamilia>> {
        return observarTodosEventos()
            .map { eventos ->
                eventos.filter { it.pessoaRelacionadaId == pessoaId }
                    .sortedBy { it.data }
            }
    }
    
    /**
     * Busca eventos por tipo
     */
    fun observarEventosPorTipo(tipo: TipoEventoFamilia): Flow<List<EventoFamilia>> {
        return observarTodosEventos()
            .map { eventos ->
                eventos.filter { it.tipo == tipo }
                    .sortedBy { it.data }
            }
    }
}
