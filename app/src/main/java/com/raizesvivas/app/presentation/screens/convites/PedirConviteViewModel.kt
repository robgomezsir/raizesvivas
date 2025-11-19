package com.raizesvivas.app.presentation.screens.convites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class PedirConviteViewModel @Inject constructor(
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _state = MutableStateFlow(PedirConviteState())
    val state = _state.asStateFlow()

    fun atualizarEmail(email: String) {
        _state.update { it.copy(email = email, erro = null) }
    }

    fun atualizarNome(nome: String) {
        _state.update { it.copy(nome = nome) }
    }

    fun atualizarTelefone(telefone: String) {
        _state.update { it.copy(telefone = telefone) }
    }

    fun enviarPedido() {
        val email = _state.value.email.trim()
        if (email.isBlank() || !email.contains("@")) {
            _state.update { it.copy(erro = "Informe um e-mail válido") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, erro = null, sucesso = null) }
            try {
                val resultado = firestoreService.salvarPedidoConvite(
                    email = email,
                    nome = _state.value.nome.trim().ifBlank { null },
                    telefone = _state.value.telefone.trim().ifBlank { null }
                )
                resultado.onSuccess {
                    _state.update { it.copy(isLoading = false, sucesso = "Pedido enviado com sucesso!") }
                }.onFailure { e ->
                    Timber.e(e, "❌ Erro ao enviar pedido de convite")
                    _state.update { it.copy(isLoading = false, erro = "Erro ao enviar pedido: ${e.message}") }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro inesperado ao enviar pedido de convite")
                _state.update { it.copy(isLoading = false, erro = "Erro inesperado: ${e.message}") }
            }
        }
    }
}

data class PedirConviteState(
    val email: String = "",
    val nome: String = "",
    val telefone: String = "",
    val isLoading: Boolean = false,
    val erro: String? = null,
    val sucesso: String? = null
)


