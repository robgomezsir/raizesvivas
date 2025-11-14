package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.EdicaoPendente
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.domain.model.StatusEdicao
import com.raizesvivas.app.utils.RetryHelper
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gerenciar edições pendentes
 * 
 * Coordena operações de criação, aprovação e rejeição de edições
 */
@Singleton
class EdicaoPendenteRepository @Inject constructor(
    private val firestoreService: FirestoreService,
    private val authService: AuthService
) {
    
    /**
     * Observa edições pendentes em tempo real
     */
    fun observarEdicoesPendentes(): Flow<List<EdicaoPendente>> {
        return firestoreService.observarEdicoesPendentes()
            .catch { error ->
                Timber.e(error, "Erro ao observar edições pendentes")
                emit(emptyList())
            }
    }
    
    /**
     * Busca todas as edições pendentes
     */
    suspend fun buscarTodasEdicoesPendentes(): Result<List<EdicaoPendente>> {
        return try {
            firestoreService.buscarTodasEdicoesPendentes()
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar edições pendentes")
            Result.failure(e)
        }
    }
    
    /**
     * Busca edições pendentes por pessoa
     */
    suspend fun buscarEdicoesPorPessoa(pessoaId: String): Result<List<EdicaoPendente>> {
        return try {
            firestoreService.buscarEdicoesPorPessoa(pessoaId)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar edições por pessoa")
            Result.failure(e)
        }
    }
    
    /**
     * Cria uma nova edição pendente (quando não-admin edita)
     */
    suspend fun criarEdicaoPendente(
        pessoaOriginal: Pessoa,
        pessoaEditada: Pessoa
    ): Result<EdicaoPendente> {
        return try {
            val currentUser = authService.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuário não autenticado"))
            }
            
            // Calcular campos alterados
            val camposAlterados = calcularCamposAlterados(pessoaOriginal, pessoaEditada)
            
            if (camposAlterados.isEmpty()) {
                return Result.failure(Exception("Nenhum campo foi alterado"))
            }
            
            val edicaoPendente = EdicaoPendente(
                id = UUID.randomUUID().toString(),
                pessoaId = pessoaOriginal.id,
                camposAlterados = camposAlterados,
                editadoPor = currentUser.uid,
                status = StatusEdicao.PENDENTE,
                criadoEm = Date()
            )
            
            val resultado = firestoreService.criarEdicaoPendente(edicaoPendente)
            
            resultado.onSuccess {
                Timber.d("✅ Edição pendente criada: ${edicaoPendente.id} para pessoa ${pessoaOriginal.id}")
            }
            
            resultado.map { edicaoPendente }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar edição pendente")
            Result.failure(e)
        }
    }
    
    /**
     * Aprova uma edição pendente (apenas admin)
     */
    suspend fun aprovarEdicao(edicaoId: String): Result<Unit> {
        return try {
            val currentUser = authService.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuário não autenticado"))
            }
            
            val resultado = firestoreService.aprovarEdicao(
                edicaoId = edicaoId,
                revisadoPor = currentUser.uid
            )
            
            resultado.onSuccess {
                Timber.d("✅ Edição $edicaoId aprovada por ${currentUser.uid}")
            }
            
            resultado
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao aprovar edição")
            Result.failure(e)
        }
    }
    
    /**
     * Rejeita uma edição pendente (apenas admin)
     */
    suspend fun rejeitarEdicao(edicaoId: String): Result<Unit> {
        return try {
            val currentUser = authService.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuário não autenticado"))
            }
            
            val resultado = firestoreService.rejeitarEdicao(
                edicaoId = edicaoId,
                revisadoPor = currentUser.uid
            )
            
            resultado.onSuccess {
                Timber.d("✅ Edição $edicaoId rejeitada por ${currentUser.uid}")
            }
            
            resultado
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao rejeitar edição")
            Result.failure(e)
        }
    }
    
    /**
     * Calcula campos alterados entre duas versões de pessoa
     */
    private fun calcularCamposAlterados(
        pessoaOriginal: Pessoa,
        pessoaEditada: Pessoa
    ): Map<String, com.raizesvivas.app.domain.model.AlteracaoCampo> {
        val camposAlterados = mutableMapOf<String, com.raizesvivas.app.domain.model.AlteracaoCampo>()
        
        if (pessoaOriginal.nome != pessoaEditada.nome) {
            camposAlterados["nome"] = com.raizesvivas.app.domain.model.AlteracaoCampo(
                valorAnterior = pessoaOriginal.nome,
                valorNovo = pessoaEditada.nome
            )
        }
        if (pessoaOriginal.dataNascimento != pessoaEditada.dataNascimento) {
            camposAlterados["dataNascimento"] = com.raizesvivas.app.domain.model.AlteracaoCampo(
                valorAnterior = pessoaOriginal.dataNascimento,
                valorNovo = pessoaEditada.dataNascimento
            )
        }
        if (pessoaOriginal.dataFalecimento != pessoaEditada.dataFalecimento) {
            camposAlterados["dataFalecimento"] = com.raizesvivas.app.domain.model.AlteracaoCampo(
                valorAnterior = pessoaOriginal.dataFalecimento,
                valorNovo = pessoaEditada.dataFalecimento
            )
        }
        if (pessoaOriginal.localNascimento != pessoaEditada.localNascimento) {
            camposAlterados["localNascimento"] = com.raizesvivas.app.domain.model.AlteracaoCampo(
                valorAnterior = pessoaOriginal.localNascimento,
                valorNovo = pessoaEditada.localNascimento
            )
        }
        if (pessoaOriginal.localResidencia != pessoaEditada.localResidencia) {
            camposAlterados["localResidencia"] = com.raizesvivas.app.domain.model.AlteracaoCampo(
                valorAnterior = pessoaOriginal.localResidencia,
                valorNovo = pessoaEditada.localResidencia
            )
        }
        if (pessoaOriginal.profissao != pessoaEditada.profissao) {
            camposAlterados["profissao"] = com.raizesvivas.app.domain.model.AlteracaoCampo(
                valorAnterior = pessoaOriginal.profissao,
                valorNovo = pessoaEditada.profissao
            )
        }
        if (pessoaOriginal.biografia != pessoaEditada.biografia) {
            camposAlterados["biografia"] = com.raizesvivas.app.domain.model.AlteracaoCampo(
                valorAnterior = pessoaOriginal.biografia,
                valorNovo = pessoaEditada.biografia
            )
        }
        if (pessoaOriginal.pai != pessoaEditada.pai) {
            camposAlterados["pai"] = com.raizesvivas.app.domain.model.AlteracaoCampo(
                valorAnterior = pessoaOriginal.pai,
                valorNovo = pessoaEditada.pai
            )
        }
        if (pessoaOriginal.mae != pessoaEditada.mae) {
            camposAlterados["mae"] = com.raizesvivas.app.domain.model.AlteracaoCampo(
                valorAnterior = pessoaOriginal.mae,
                valorNovo = pessoaEditada.mae
            )
        }
        if (pessoaOriginal.conjugeAtual != pessoaEditada.conjugeAtual) {
            camposAlterados["conjugeAtual"] = com.raizesvivas.app.domain.model.AlteracaoCampo(
                valorAnterior = pessoaOriginal.conjugeAtual,
                valorNovo = pessoaEditada.conjugeAtual
            )
        }
        if (pessoaOriginal.filhos != pessoaEditada.filhos) {
            camposAlterados["filhos"] = com.raizesvivas.app.domain.model.AlteracaoCampo(
                valorAnterior = pessoaOriginal.filhos,
                valorNovo = pessoaEditada.filhos
            )
        }
        
        return camposAlterados
    }
}

