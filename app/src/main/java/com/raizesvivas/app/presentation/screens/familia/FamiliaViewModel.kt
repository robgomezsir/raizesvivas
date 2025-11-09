package com.raizesvivas.app.presentation.screens.familia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.repository.FamiliaPersonalizadaRepository
import com.raizesvivas.app.data.repository.FamiliaZeroRepository
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.domain.model.FamiliaPersonalizada
import com.raizesvivas.app.domain.model.FamiliaZero
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.domain.model.Usuario
import com.raizesvivas.app.presentation.components.FamiliaGrupo
import com.raizesvivas.app.presentation.components.agruparPessoasPorFamilias
import com.raizesvivas.app.presentation.components.TreeNodeData
import com.raizesvivas.app.utils.TreeBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class FamiliaViewModel @Inject constructor(
    private val pessoaRepository: PessoaRepository,
    private val familiaZeroRepository: FamiliaZeroRepository,
    private val familiaPersonalizadaRepository: FamiliaPersonalizadaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authService: AuthService
) : ViewModel() {

    private val expandedFamilias = MutableStateFlow<Set<String>>(emptySet())

    private val _state = MutableStateFlow(FamiliaState(isLoading = true))
    val state: StateFlow<FamiliaState> = _state

    init {
        observarDados()
    }

    private fun observarDados() {
        viewModelScope.launch {
            val dadosFamiliaFlow = combine(
                pessoaRepository.observarTodasPessoas(),
                familiaZeroRepository.observar(),
                familiaPersonalizadaRepository.observarTodas(),
                observarUsuarioAtual()
            ) { pessoas, familiaZero, personalizadas, usuario ->
                val montagem = montarFamilias(
                    pessoas = pessoas,
                    familiaZero = familiaZero,
                    nomesPersonalizados = personalizadas
                )

                val outrosFamiliares = pessoas.filter { pessoa ->
                    pessoa.id.isNotBlank() && pessoa.id !in montagem.membrosAssociados
                }

                DadosFamilia(
                    familias = montagem.familias,
                    outrosFamiliares = outrosFamiliares,
                    usuarioEhAdmin = usuario?.ehAdministrador == true
                )
            }

            combine(dadosFamiliaFlow, expandedFamilias) { dados, expandidas ->
                dados to expandidas
            }.collect { (dados, expandidas) ->
                _state.update { atual ->
                    atual.copy(
                        familias = dados.familias,
                        outrosFamiliares = dados.outrosFamiliares,
                        usuarioEhAdmin = dados.usuarioEhAdmin,
                        expandedFamilias = expandidas,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observarUsuarioAtual() =
        authService.currentUser?.uid?.let { userId ->
            usuarioRepository.observarPorId(userId)
        } ?: flowOf<Usuario?>(null)

    fun toggleFamilia(familiaId: String) {
        expandedFamilias.update { atuais ->
            if (atuais.contains(familiaId)) {
                atuais - familiaId
            } else {
                atuais + familiaId
            }
        }
    }

    fun adicionarMembro(familiaId: String, pessoaId: String) {
        if (familiaId.isBlank() || pessoaId.isBlank()) return

        viewModelScope.launch {
            val ehAdmin = _state.value.usuarioEhAdmin
            _state.update { it.copy(isLoading = true, erro = null) }

            val pessoa = pessoaRepository.buscarPorId(pessoaId)
            if (pessoa == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        erro = "Pessoa não encontrada para adicionar à família."
                    )
                }
                return@launch
            }

            val novasFamilias = (pessoa.familias + familiaId).distinct()
            if (novasFamilias == pessoa.familias) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        erro = "Familiar já vinculado a esta família."
                    )
                }
                return@launch
            }

            val usuarioId = authService.currentUser?.uid
            val pessoaAtualizada = pessoa.copy(
                familias = novasFamilias,
                modificadoPor = usuarioId ?: pessoa.modificadoPor,
                modificadoEm = Date()
            )
            val resultado = pessoaRepository.atualizar(pessoaAtualizada, ehAdmin)

            _state.update {
                it.copy(
                    isLoading = false,
                    erro = resultado.exceptionOrNull()?.message
                )
            }
        }
    }

    fun removerMembro(familiaId: String, pessoaId: String) {
        if (familiaId.isBlank() || pessoaId.isBlank()) return

        viewModelScope.launch {
            val ehAdmin = _state.value.usuarioEhAdmin
            _state.update { it.copy(isLoading = true, erro = null) }

            val pessoa = pessoaRepository.buscarPorId(pessoaId)
            if (pessoa == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        erro = "Pessoa não encontrada para remover da família."
                    )
                }
                return@launch
            }

            val novasFamilias = pessoa.familias.filterNot { it == familiaId }
            if (novasFamilias == pessoa.familias) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        erro = "Familiar não está vinculado a esta família."
                    )
                }
                return@launch
            }

            val usuarioId = authService.currentUser?.uid
            val pessoaAtualizada = pessoa.copy(
                familias = novasFamilias,
                modificadoPor = usuarioId ?: pessoa.modificadoPor,
                modificadoEm = Date()
            )
            val resultado = pessoaRepository.atualizar(pessoaAtualizada, ehAdmin)

            _state.update {
                it.copy(
                    isLoading = false,
                    erro = resultado.exceptionOrNull()?.message
                )
            }
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, erro = null) }

            val resultadoPessoas = pessoaRepository.recarregarDoFirestore()
            val resultadoFamilias = familiaPersonalizadaRepository.sincronizar()

            val erro = resultadoPessoas.exceptionOrNull()
                ?: resultadoFamilias.exceptionOrNull()

            _state.update {
                it.copy(
                    isRefreshing = false,
                    erro = erro?.message
                )
            }
        }
    }

    fun atualizarNomeFamilia(familia: FamiliaUiModel, novoNome: String) {
        val nomeLimpo = novoNome.trim()
        if (nomeLimpo.isEmpty()) {
            _state.update {
                it.copy(erro = "Nome da família não pode ficar vazio")
            }
            return
        }

        if (!_state.value.usuarioEhAdmin) {
            _state.update {
                it.copy(erro = "Apenas administradores podem renomear famílias")
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, erro = null) }

            val resultado = if (familia.ehFamiliaZero) {
                atualizarNomeFamiliaZero(nomeLimpo)
            } else {
                val usuarioId = authService.currentUser?.uid
                val familiaPersonalizada = FamiliaPersonalizada(
                    familiaId = familia.id,
                    nome = nomeLimpo,
                    conjuguePrincipalId = familia.conjuguePrincipal?.id,
                    conjugueSecundarioId = familia.conjugueSecundario?.id,
                    ehFamiliaZero = false,
                    atualizadoPor = usuarioId
                )
                familiaPersonalizadaRepository.salvar(familiaPersonalizada)
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    erro = resultado.exceptionOrNull()?.message
                )
            }
        }
    }

    private suspend fun atualizarNomeFamiliaZero(nome: String): Result<Unit> {
        val familiaZeroAtual = familiaZeroRepository.buscar()
            ?: return Result.failure(IllegalStateException("Família Zero não encontrada"))

        val atualizada = familiaZeroAtual.copy(arvoreNome = nome)
        return familiaZeroRepository.salvar(atualizada)
    }

    private fun montarFamilias(
        pessoas: List<Pessoa>,
        familiaZero: FamiliaZero?,
        nomesPersonalizados: List<FamiliaPersonalizada>
    ): FamiliaMontagem {
        if (pessoas.isEmpty()) return FamiliaMontagem(emptyList(), emptySet())

        val pessoasMap = pessoas.associateBy { it.id }
        val grupos = agruparPessoasPorFamilias(pessoas, pessoasMap)
        val membrosAssociados = mutableSetOf<String>()

        val familias = grupos.mapNotNull { grupo ->
            val familiaId = calcularFamiliaId(grupo, familiaZero)
            val conjuguePrincipal = grupo.conjugue1 ?: grupo.conjugue2 ?: grupo.filhos.firstOrNull()
            val conjugueSecundario = when {
                grupo.conjugue1 != null && grupo.conjugue1?.id != conjuguePrincipal?.id -> grupo.conjugue1
                grupo.conjugue2 != null && grupo.conjugue2?.id != conjuguePrincipal?.id -> grupo.conjugue2
                else -> null
            }

            val raiz = conjuguePrincipal ?: return@mapNotNull null
            val treeRoot = TreeBuilder.buildTree(
                pessoas = pessoas,
                casalFamiliaZero = Pair(raiz, conjugueSecundario),
                nosExpandidos = emptySet()
            )

            val nomePadrao = gerarNomePadrao(grupo, raiz, conjugueSecundario)
            val nomePersonalizado = when {
                grupo.ehFamiliaZero -> familiaZero?.arvoreNome?.takeIf { it.isNotBlank() }
                else -> nomesPersonalizados.firstOrNull { it.familiaId == familiaId }?.nome
            }
            val nomeExibicao = nomePersonalizado?.takeIf { it.isNotBlank() } ?: nomePadrao

            val familiaBase = FamiliaUiModel(
                id = familiaId,
                nomeExibicao = nomeExibicao,
                nomePadrao = nomePadrao,
                ehFamiliaZero = grupo.ehFamiliaZero,
                conjuguePrincipal = conjuguePrincipal,
                conjugueSecundario = conjugueSecundario,
                treeRoot = treeRoot
            )

            val idsAssociados = mutableSetOf<String>().apply {
                familiaBase.conjuguePrincipal?.id?.takeIf { it.isNotBlank() }?.let { add(it) }
                familiaBase.conjugueSecundario?.id?.takeIf { it.isNotBlank() }?.let { add(it) }
                familiaBase.membrosFlatten.forEach { item ->
                    item.pessoa.id.takeIf { it.isNotBlank() }?.let { add(it) }
                    item.conjuge?.id?.takeIf { it.isNotBlank() }?.let { add(it) }
                }
            }

            val membrosExtras = pessoas.filter { pessoa ->
                pessoa.familias.any { it == familiaId || it.equals(familiaId, ignoreCase = true) } &&
                    pessoa.id.isNotBlank() &&
                    pessoa.id !in idsAssociados
            }.sortedBy { it.nome.lowercase(Locale.getDefault()) }

            val familia = familiaBase.copy(membrosExtras = membrosExtras)

            atualizarMembrosAssociados(membrosAssociados, familia)
            familia
        }.sortedWith(
            compareByDescending<FamiliaUiModel> { it.ehFamiliaZero }
                .thenBy { it.nomeExibicao.lowercase(Locale.getDefault()) }
        )

        return FamiliaMontagem(
            familias = familias,
            membrosAssociados = membrosAssociados
        )
    }

    private fun atualizarMembrosAssociados(
        membrosAssociados: MutableSet<String>,
        familia: FamiliaUiModel
    ) {
        familia.conjuguePrincipal?.id?.takeIf { it.isNotBlank() }?.let(membrosAssociados::add)
        familia.conjugueSecundario?.id?.takeIf { it.isNotBlank() }?.let(membrosAssociados::add)

        familia.membrosFlatten.forEach { item ->
            item.pessoa.id.takeIf { it.isNotBlank() }?.let(membrosAssociados::add)
            item.conjuge?.id?.takeIf { it.isNotBlank() }?.let(membrosAssociados::add)
        }
        familia.membrosExtras.forEach { extra ->
            extra.id.takeIf { it.isNotBlank() }?.let(membrosAssociados::add)
        }
    }

    private fun calcularFamiliaId(
        grupo: FamiliaGrupo,
        familiaZero: FamiliaZero?
    ): String {
        if (grupo.ehFamiliaZero) {
            return familiaZero?.id ?: "familia_zero"
        }
        val idsConjuges = buildList {
            grupo.conjugue1?.id?.let { add(it) }
            grupo.conjugue2?.id?.let { add(it) }
        }

        if (idsConjuges.isNotEmpty()) {
            return idsConjuges.sorted().joinToString("_")
        }

        val primeiroFilhoId = grupo.filhos.firstOrNull()?.id
        return primeiroFilhoId ?: "familia_${grupo.id}"
    }

    private fun gerarNomePadrao(
        grupo: FamiliaGrupo,
        conjuguePrincipal: Pessoa,
        conjugueSecundario: Pessoa?
    ): String {
        val candidato = conjuguePrincipal.nome
            .takeIf { it.isNotBlank() }
            ?: conjugueSecundario?.nome
            ?: grupo.filhos.firstOrNull()
                ?.nome
            ?: "Família"

        val sobrenome = candidato.trim().split(" ").lastOrNull() ?: candidato
        return "Família ${sobrenome.uppercase(Locale.getDefault())}"
    }
}

private data class FamiliaMontagem(
    val familias: List<FamiliaUiModel>,
    val membrosAssociados: Set<String>
)

private data class DadosFamilia(
    val familias: List<FamiliaUiModel>,
    val outrosFamiliares: List<Pessoa>,
    val usuarioEhAdmin: Boolean
)

data class FamiliaUiModel(
    val id: String,
    val nomeExibicao: String,
    val nomePadrao: String,
    val ehFamiliaZero: Boolean,
    val conjuguePrincipal: Pessoa?,
    val conjugueSecundario: Pessoa?,
    val treeRoot: TreeNodeData?,
    val membrosExtras: List<Pessoa> = emptyList()
) {
    val membrosFlatten: List<FamiliaPessoaItem> =
        treeRoot?.flatten() ?: emptyList()
}

data class FamiliaPessoaItem(
    val pessoa: Pessoa,
    val conjuge: Pessoa?,
    val nivel: Int
)

data class FamiliaState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val erro: String? = null,
    val outrosFamiliares: List<Pessoa> = emptyList(),
    val familias: List<FamiliaUiModel> = emptyList(),
    val expandedFamilias: Set<String> = emptySet(),
    val usuarioEhAdmin: Boolean = false
)

private fun TreeNodeData.flatten(): List<FamiliaPessoaItem> {
    val atual = FamiliaPessoaItem(
        pessoa = pessoa,
        conjuge = conjuge,
        nivel = nivel
    )
    val filhos = children.flatMap { it.flatten() }
    return listOf(atual) + filhos
}

