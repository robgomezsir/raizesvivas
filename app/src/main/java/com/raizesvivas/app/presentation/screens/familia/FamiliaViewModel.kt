package com.raizesvivas.app.presentation.screens.familia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.repository.AmigoRepository
import com.raizesvivas.app.data.repository.FamiliaPersonalizadaRepository
import com.raizesvivas.app.data.repository.FamiliaZeroRepository
import com.raizesvivas.app.data.repository.GamificacaoRepository
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.domain.model.Amigo
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.domain.model.FamiliaPersonalizada
import com.raizesvivas.app.domain.model.FamiliaZero
import com.raizesvivas.app.domain.model.Genero
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.domain.model.TipoAcao
import com.raizesvivas.app.domain.model.Usuario
import com.raizesvivas.app.presentation.components.FamiliaGrupo
import com.raizesvivas.app.presentation.components.agruparPessoasPorFamilias
import com.raizesvivas.app.presentation.components.agruparPessoasPorFamiliasComPendentes
import com.raizesvivas.app.presentation.components.FamiliaMonoparentalPendente
import com.raizesvivas.app.presentation.components.TreeNodeData
import com.raizesvivas.app.utils.TreeBuilder
import com.raizesvivas.app.utils.ParentescoCalculator
import com.raizesvivas.app.utils.FamiliaMonoparentalPreferences
import com.raizesvivas.app.utils.FamiliaOrdemPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    private val authService: AuthService,
    private val gamificacaoRepository: GamificacaoRepository,
    private val amigoRepository: AmigoRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val expandedFamilias = MutableStateFlow<Set<String>>(emptySet())
    private val familiasMonoparentaisConfirmadas = MutableStateFlow<Set<String>>(emptySet()) // IDs de pais confirmados para criar família monoparental
    private val familiasMonoparentaisRejeitadas = MutableStateFlow<Set<String>>(emptySet()) // IDs de pais rejeitados
    private val ordemFamilias = MutableStateFlow<List<String>>(emptyList()) // Ordem personalizada das famílias

    private val _state = MutableStateFlow(FamiliaState(isLoading = true))
    val state: StateFlow<FamiliaState> = _state

    init {
        carregarPreferenciasRejeitadas()
        carregarOrdemFamilias()
        observarDados()
        registrarVisualizacaoArvore()
    }
    
    /**
     * Carrega as preferências de famílias monoparentais rejeitadas
     */
    private fun carregarPreferenciasRejeitadas() {
        viewModelScope.launch {
            try {
                val rejeitados = FamiliaMonoparentalPreferences.obterRejeitados(context)
                familiasMonoparentaisRejeitadas.value = rejeitados
                Timber.d("📋 Carregadas ${rejeitados.size} famílias monoparentais rejeitadas")
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao carregar preferências de famílias rejeitadas")
            }
        }
    }
    
    /**
     * Carrega a ordem personalizada das famílias
     */
    private fun carregarOrdemFamilias() {
        viewModelScope.launch {
            try {
                val ordem = FamiliaOrdemPreferences.obterOrdem(context)
                ordemFamilias.value = ordem
                Timber.d("📋 Carregada ordem de ${ordem.size} famílias")
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao carregar ordem das famílias")
            }
        }
    }
    
    /**
     * Registra a visualização da árvore genealógica pela primeira vez
     * Verifica no banco de dados se já visualizou antes de registrar novamente
     */
    private fun registrarVisualizacaoArvore() {
        viewModelScope.launch {
            val usuarioId = authService.currentUser?.uid
            if (usuarioId == null) return@launch
            
            try {
                // Verificar se já existe progresso para a conquista "explorador_curioso"
                val progressoAtual = gamificacaoRepository.observarProgressoConquista("explorador_curioso", usuarioId).first()
                
                // Se não tem progresso ou progresso é 0, registrar a visualização
                if (progressoAtual == null || progressoAtual.progresso == 0) {
                    gamificacaoRepository.registrarAcao(usuarioId, TipoAcao.EXPLORAR_ARVORE_PRIMEIRA_VEZ)
                    Timber.d("🎯 Registrada visualização da árvore pela primeira vez")
                } else {
                    Timber.d("ℹ️ Árvore já foi visualizada anteriormente (progresso: ${progressoAtual.progresso})")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao registrar visualização da árvore")
            }
        }
    }

    private fun observarDados() {
        viewModelScope.launch {
            // Combinar os primeiros 5 flows
            val dadosParciaisFlow = combine(
                pessoaRepository.observarTodasPessoas(),
                familiaZeroRepository.observar(),
                familiaPersonalizadaRepository.observarTodas(),
                observarUsuarioAtual(),
                familiasMonoparentaisRejeitadas
            ) { pessoas: List<Pessoa>, familiaZero: FamiliaZero?, personalizadas: List<FamiliaPersonalizada>, usuario: Usuario?, rejeitados: Set<String> ->
                Triple(pessoas, familiaZero, Triple(personalizadas, usuario, rejeitados))
            }
            
            // Combinar com o último flow
            val dadosFamiliaFlow = combine(
                dadosParciaisFlow,
                amigoRepository.observarTodosAmigos()
            ) { parcial, amigos ->
                val (pessoas, familiaZero, dadosExtras) = parcial
                val (personalizadas, usuario, rejeitados) = dadosExtras
                
                val montagem = montarFamilias(
                    pessoas = pessoas,
                    familiaZero = familiaZero,
                    nomesPersonalizados = personalizadas
                )

                val outrosFamiliares = pessoas.filter { pessoa ->
                    pessoa.id.isNotBlank() && pessoa.id !in montagem.membrosAssociados
                }.distinctBy { pessoa -> pessoa.id }

                // Montar famílias rejeitadas com informações completas
                val familiasRejeitadas = montarFamiliasRejeitadas(pessoas, rejeitados)

                DadosFamilia(
                    familias = montagem.familias,
                    outrosFamiliares = outrosFamiliares,
                    usuarioEhAdmin = usuario?.ehAdministrador == true,
                    familiasPendentes = montagem.familiasPendentes,
                    familiasRejeitadas = familiasRejeitadas,
                    amigos = amigos
                )
            }

            combine(dadosFamiliaFlow, expandedFamilias, ordemFamilias) { dados, expandidas, ordem ->
                Triple(dados, expandidas, ordem)
            }.collect { (dados, expandidas, ordem) ->
                // Aplicar ordem personalizada às famílias
                val familiasOrdenadas = aplicarOrdemFamilias(dados.familias, ordem)
                
                _state.update { atual ->
                    val primeiraPendente = dados.familiasPendentes.firstOrNull()
                    atual.copy(
                        familias = familiasOrdenadas,
                        outrosFamiliares = dados.outrosFamiliares,
                        usuarioEhAdmin = dados.usuarioEhAdmin,
                        expandedFamilias = expandidas,
                        isLoading = false,
                        familiasMonoparentaisPendentes = dados.familiasPendentes,
                        mostrarDialogFamiliaPendente = primeiraPendente != null && !atual.mostrarDialogFamiliaPendente,
                        familiaPendenteAtual = primeiraPendente,
                        familiasRejeitadas = dados.familiasRejeitadas,
                        amigos = dados.amigos
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

    /**
     * Confirma a criação de uma família monoparental com pai + filhos
     */
    fun confirmarCriarFamiliaMonoparental() {
        _state.value.familiaPendenteAtual?.let { pendente ->
            // Adicionar o ID do pai à lista de confirmados
            familiasMonoparentaisConfirmadas.update { atual ->
                atual + pendente.responsavel.id
            }
        }
        
        _state.update { atual ->
            val pendente = atual.familiaPendenteAtual
            if (pendente != null) {
                // Remover a pendência da lista e fechar o diálogo
                val novasPendentes = atual.familiasMonoparentaisPendentes.drop(1)
                val proximaPendente = novasPendentes.firstOrNull()
                atual.copy(
                    familiasMonoparentaisPendentes = novasPendentes,
                    mostrarDialogFamiliaPendente = proximaPendente != null,
                    familiaPendenteAtual = proximaPendente
                )
            } else {
                atual.copy(mostrarDialogFamiliaPendente = false, familiaPendenteAtual = null)
            }
        }
        // As famílias serão recalculadas automaticamente pelo flow e a família será criada
    }

    /**
     * Cancela a criação de uma família monoparental com pai + filhos
     * Persiste a decisão para que não seja sugerida novamente
     */
    fun cancelarCriarFamiliaMonoparental() {
        _state.value.familiaPendenteAtual?.let { pendente ->
            val paiId = pendente.responsavel.id
            // Adicionar à lista de rejeitados e persistir
            familiasMonoparentaisRejeitadas.update { atual ->
                atual + paiId
            }
            viewModelScope.launch {
                try {
                    FamiliaMonoparentalPreferences.adicionarRejeitado(context, paiId)
                    Timber.d("💾 Família monoparental rejeitada persistida para pai: $paiId")
                } catch (e: Exception) {
                    Timber.e(e, "❌ Erro ao persistir rejeição de família monoparental")
                }
            }
        }
        
        _state.update { atual ->
            val novasPendentes = atual.familiasMonoparentaisPendentes.drop(1)
            val proximaPendente = novasPendentes.firstOrNull()
            atual.copy(
                familiasMonoparentaisPendentes = novasPendentes,
                mostrarDialogFamiliaPendente = proximaPendente != null,
                familiaPendenteAtual = proximaPendente
            )
        }
    }
    
    /**
     * Permite que o usuário solicite explicitamente a criação de uma família monoparental
     * que foi previamente rejeitada, removendo-a da lista de rejeitados.
     * Isso fará com que a família seja sugerida novamente na próxima vez que o agrupamento for recalculado.
     */
    fun solicitarCriarFamiliaMonoparental(paiId: String) {
        viewModelScope.launch {
            try {
                FamiliaMonoparentalPreferences.removerRejeitado(context, paiId)
                familiasMonoparentaisRejeitadas.update { atual ->
                    atual - paiId
                }
                Timber.d("✅ Família monoparental removida da lista de rejeitados: $paiId")
                // O flow observará a mudança e recalculará automaticamente
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao remover família monoparental da lista de rejeitados")
            }
        }
    }

    /**
     * Fecha o diálogo de família pendente
     */
    fun fecharDialogFamiliaPendente() {
        _state.update { it.copy(mostrarDialogFamiliaPendente = false) }
    }
    
    /**
     * Aplica a ordem personalizada às famílias
     * Família Zero sempre fica na primeira posição
     */
    private fun aplicarOrdemFamilias(
        familias: List<FamiliaUiModel>,
        ordemPersonalizada: List<String>
    ): List<FamiliaUiModel> {
        if (ordemPersonalizada.isEmpty()) {
            // Se não há ordem salva, retornar ordem padrão (Família Zero primeiro)
            val familiaZero = familias.firstOrNull { it.ehFamiliaZero }
            val outrasFamilias = familias.filter { !it.ehFamiliaZero }
            return listOfNotNull(familiaZero) + outrasFamilias
        }
        
        // Separar Família Zero (sempre primeira)
        val familiaZero = familias.firstOrNull { it.ehFamiliaZero }
        val outrasFamilias = familias.filter { !it.ehFamiliaZero }
        
        // Criar mapa para busca rápida
        val familiasMap = outrasFamilias.associateBy { it.id }
        
        // Ordenar outras famílias conforme ordem salva
        val familiasOrdenadas = ordemPersonalizada.mapNotNull { id ->
            familiasMap[id]
        }
        
        // Adicionar famílias que não estão na ordem salva (novas famílias)
        val familiasNaOrdem = ordemPersonalizada.toSet()
        val familiasNovas = outrasFamilias.filter { it.id !in familiasNaOrdem }
        
        return listOfNotNull(familiaZero) + familiasOrdenadas + familiasNovas
    }
    
    /**
     * Reordena as famílias e persiste a nova ordem
     */
    fun reordenarFamilias(novaOrdem: List<String>) {
        viewModelScope.launch {
            try {
                // Remover Família Zero da ordem (ela sempre fica na primeira posição)
                val ordemSemFamiliaZero = novaOrdem.filter { familiaId ->
                    val familia = _state.value.familias.find { it.id == familiaId }
                    familia?.ehFamiliaZero != true
                }
                
                FamiliaOrdemPreferences.salvarOrdem(context, ordemSemFamiliaZero)
                ordemFamilias.value = ordemSemFamiliaZero
                Timber.d("✅ Ordem das famílias salva: ${ordemSemFamiliaZero.size} famílias")
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao salvar ordem das famílias")
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
            resultado.onSuccess { ParentescoCalculator.limparCache() }

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

            // Verificar se a pessoa está na família (no campo familias ou na árvore)
            val familiaAtual = _state.value.familias.firstOrNull { it.id == familiaId }
            
            // Normalizar comparação considerando variações do familiaId (ex: "familia_zero" vs ID real)
            val pessoaEstaNoCampoFamilias = pessoa.familias.any { familiaIdNoCampo ->
                familiaIdNoCampo == familiaId || 
                familiaIdNoCampo.equals(familiaId, ignoreCase = true) ||
                // Para Família Zero, considerar tanto "familia_zero" quanto o ID real
                (familiaAtual?.ehFamiliaZero == true && 
                 (familiaIdNoCampo == "familia_zero" || familiaId == "familia_zero" || 
                  familiaIdNoCampo.equals("familia_zero", ignoreCase = true)))
            }
            
            val pessoaEstaNaArvore = familiaAtual?.let { familia ->
                val idsNaFamilia = mutableSetOf<String>().apply {
                    familia.conjuguePrincipal?.id?.takeIf { it.isNotBlank() }?.let { add(it) }
                    familia.conjugueSecundario?.id?.takeIf { it.isNotBlank() }?.let { add(it) }
                    familia.membrosFlatten.forEach { item ->
                        item.pessoa.id.takeIf { it.isNotBlank() }?.let { add(it) }
                        item.conjuge?.id?.takeIf { it.isNotBlank() }?.let { add(it) }
                    }
                }
                pessoaId in idsNaFamilia
            } ?: false

            if (!pessoaEstaNoCampoFamilias && !pessoaEstaNaArvore) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        erro = "Familiar não está vinculado a esta família."
                    )
                }
                return@launch
            }

            // Se a pessoa está apenas na árvore (sem o familiaId no campo familias),
            // verificar se ela realmente tem vínculos genealógicos ativos que a conectam à família.
            // Se não tiver mais vínculos, permitir a remoção mesmo que apareça na árvore.
            if (!pessoaEstaNoCampoFamilias && pessoaEstaNaArvore) {
                // Buscar todas as pessoas para verificar vínculos
                val todasPessoas = pessoaRepository.observarTodasPessoas().first()
                val pessoasMap = todasPessoas.associateBy { it.id }
                
                // Coletar IDs de pessoas na família
                val idsNaFamilia = familiaAtual?.let { familia ->
                    mutableSetOf<String>().apply {
                        familia.conjuguePrincipal?.id?.takeIf { it.isNotBlank() }?.let { add(it) }
                        familia.conjugueSecundario?.id?.takeIf { it.isNotBlank() }?.let { add(it) }
                        familia.membrosFlatten.forEach { item ->
                            item.pessoa.id.takeIf { it.isNotBlank() }?.let { add(it) }
                            item.conjuge?.id?.takeIf { it.isNotBlank() }?.let { add(it) }
                        }
                    }
                } ?: mutableSetOf()
                
                // Verificar se a pessoa tem vínculos genealógicos ativos que a conectam à família
                val vinculosDetectados = mutableListOf<String>()
                
                if (idsNaFamilia.isNotEmpty()) {
                    // Verificar se tem pai ou mãe na família
                    val temPaiNaFamilia = pessoa.pai?.takeIf { it.isNotBlank() }?.let { it in idsNaFamilia } == true
                    val temMaeNaFamilia = pessoa.mae?.takeIf { it.isNotBlank() }?.let { it in idsNaFamilia } == true
                    if (temPaiNaFamilia) vinculosDetectados.add("pai")
                    if (temMaeNaFamilia) vinculosDetectados.add("mãe")
                    
                    // Verificar se tem filhos na família (verificar se os filhos realmente existem e estão na família)
                    val filhosNaFamilia = pessoa.filhos.filter { filhoId ->
                        filhoId.isNotBlank() && filhoId in idsNaFamilia && pessoasMap[filhoId] != null
                    }
                    if (filhosNaFamilia.isNotEmpty()) {
                        vinculosDetectados.add("filhos (${filhosNaFamilia.size})")
                    }
                    
                    // Verificar se é cônjuge de alguém na família (verificar se o cônjuge realmente existe)
                    val conjugeId = pessoa.conjugeAtual?.takeIf { it.isNotBlank() }
                    val ehConjugeDeAlguemNaFamilia = conjugeId?.let { 
                        it in idsNaFamilia && pessoasMap[it] != null 
                    } == true
                    if (ehConjugeDeAlguemNaFamilia) {
                        vinculosDetectados.add("cônjuge")
                    }
                    
                    // Verificar se alguém na família tem esta pessoa como pai/mãe (verificar se essas pessoas realmente existem)
                    val pessoasComEstaPessoaComoPaiOuMae = idsNaFamilia.filter { idNaFamilia ->
                        val pessoaNaFamilia = pessoasMap[idNaFamilia]
                        pessoaNaFamilia != null && (pessoaNaFamilia.pai == pessoaId || pessoaNaFamilia.mae == pessoaId)
                    }
                    if (pessoasComEstaPessoaComoPaiOuMae.isNotEmpty()) {
                        vinculosDetectados.add("pessoas que têm esta pessoa como pai/mãe (${pessoasComEstaPessoaComoPaiOuMae.size})")
                    }
                    
                    // Verificar se alguém na família tem esta pessoa na lista de filhos (referência órfã)
                    val pessoasComEstaPessoaComoFilho = idsNaFamilia.filter { idNaFamilia ->
                        val pessoaNaFamilia = pessoasMap[idNaFamilia]
                        pessoaNaFamilia != null && pessoaNaFamilia.filhos.contains(pessoaId)
                    }
                    if (pessoasComEstaPessoaComoFilho.isNotEmpty()) {
                        vinculosDetectados.add("referências órfãs na lista de filhos (${pessoasComEstaPessoaComoFilho.size})")
                    }
                }
                
                val temVinculosAtivos = vinculosDetectados.isNotEmpty()
                
                if (temVinculosAtivos) {
                    // Se há apenas referências órfãs, limpar automaticamente e permitir remoção
                    val apenasReferenciasOrfas = vinculosDetectados.all { it.contains("referências órfãs") }
                    
                    if (apenasReferenciasOrfas) {
                        // Limpar referências órfãs automaticamente
                        idsNaFamilia.forEach { idNaFamilia ->
                            val pessoaNaFamilia = pessoasMap[idNaFamilia]
                            if (pessoaNaFamilia != null && pessoaNaFamilia.filhos.contains(pessoaId)) {
                                val filhosAtualizados = pessoaNaFamilia.filhos.filterNot { it == pessoaId }
                                if (filhosAtualizados != pessoaNaFamilia.filhos) {
                                    val pessoaAtualizada = pessoaNaFamilia.copy(
                                        filhos = filhosAtualizados,
                                        modificadoPor = authService.currentUser?.uid ?: pessoaNaFamilia.modificadoPor,
                                        modificadoEm = Date()
                                    )
                                    pessoaRepository.atualizar(pessoaAtualizada, ehAdmin)
                                }
                            }
                        }
                        // Limpar cache e continuar com a remoção
                        ParentescoCalculator.limparCache()
                    } else {
                        // Há vínculos reais, mostrar erro específico
                        val vinculosTexto = vinculosDetectados.joinToString(", ")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                erro = "Este familiar está vinculado à família através de: $vinculosTexto. Para removê-lo, é necessário editar essas relações no cadastro da pessoa."
                            )
                        }
                        return@launch
                    }
                } else {
                    // Não tem mais vínculos ativos, mas aparece na árvore (provavelmente por estar no campo familias de outra pessoa ou por referência órfã)
                    // Limpar referências órfãs: remover esta pessoa da lista de filhos de pessoas na família
                    idsNaFamilia.forEach { idNaFamilia ->
                        val pessoaNaFamilia = pessoasMap[idNaFamilia]
                        if (pessoaNaFamilia != null && pessoaNaFamilia.filhos.contains(pessoaId)) {
                            val filhosAtualizados = pessoaNaFamilia.filhos.filterNot { it == pessoaId }
                            if (filhosAtualizados != pessoaNaFamilia.filhos) {
                                val pessoaAtualizada = pessoaNaFamilia.copy(
                                    filhos = filhosAtualizados,
                                    modificadoPor = authService.currentUser?.uid ?: pessoaNaFamilia.modificadoPor,
                                    modificadoEm = Date()
                                )
                                pessoaRepository.atualizar(pessoaAtualizada, ehAdmin)
                            }
                        }
                    }
                    // Limpar cache para forçar reconstrução da árvore
                    ParentescoCalculator.limparCache()
                    // Continuar com a remoção do campo familias abaixo
                }
            }

            // Remover do campo familias (considerando variações do familiaId)
            // Se a pessoa não está no campo familias mas estava na árvore sem vínculos ativos,
            // ainda tentamos remover para garantir consistência
            val familiasAtualizadas = pessoa.familias.filterNot { familiaIdNoCampo ->
                familiaIdNoCampo == familiaId || 
                familiaIdNoCampo.equals(familiaId, ignoreCase = true) ||
                // Para Família Zero, remover tanto "familia_zero" quanto o ID real
                (familiaAtual?.ehFamiliaZero == true && 
                 (familiaIdNoCampo == "familia_zero" || familiaId == "familia_zero" || 
                  familiaIdNoCampo.equals("familia_zero", ignoreCase = true)))
            }
            
            // Se não há mudanças no campo familias mas a pessoa não tem vínculos ativos,
            // ainda assim consideramos a remoção bem-sucedida (a pessoa será removida da árvore na próxima reconstrução)
            if (familiasAtualizadas == pessoa.familias && !pessoaEstaNoCampoFamilias && pessoaEstaNaArvore) {
                // Não há nada para remover do campo familias, mas a pessoa não tem vínculos ativos
                // A remoção é considerada bem-sucedida (a pessoa será removida da árvore na próxima reconstrução)
                // Limpar cache para forçar reconstrução da árvore
                ParentescoCalculator.limparCache()
                _state.update {
                    it.copy(
                        isLoading = false,
                        erro = null
                    )
                }
                return@launch
            }

            val usuarioId = authService.currentUser?.uid
            val pessoaAtualizada = pessoa.copy(
                familias = familiasAtualizadas,
                modificadoPor = usuarioId ?: pessoa.modificadoPor,
                modificadoEm = Date()
            )
            val resultado = pessoaRepository.atualizar(pessoaAtualizada, ehAdmin)
            resultado.onSuccess { ParentescoCalculator.limparCache() }

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
    
    /**
     * Vincula um familiar a um amigo
     */
    fun vincularFamiliarAoAmigo(amigoId: String, familiarId: String) {
        viewModelScope.launch {
            try {
                val amigo = amigoRepository.buscarPorId(amigoId)
                if (amigo != null) {
                    val familiaresAtualizados = amigo.familiaresVinculados.toMutableList()
                    if (familiarId !in familiaresAtualizados) {
                        familiaresAtualizados.add(familiarId)
                        val amigoAtualizado = amigo.copy(
                            familiaresVinculados = familiaresAtualizados,
                            modificadoEm = Date()
                        )
                        amigoRepository.salvar(amigoAtualizado)
                        Timber.d("✅ Familiar vinculado ao amigo: ${amigo.nome}")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao vincular familiar ao amigo")
            }
        }
    }

    /**
     * Remove um familiar vinculado de um amigo
     */
    fun removerFamiliarDoAmigo(amigoId: String, familiarId: String) {
        viewModelScope.launch {
            try {
                val amigo = amigoRepository.buscarPorId(amigoId)
                if (amigo != null) {
                    val familiaresAtualizados = amigo.familiaresVinculados.filterNot { it == familiarId }
                    if (familiaresAtualizados != amigo.familiaresVinculados) {
                        val amigoAtualizado = amigo.copy(
                            familiaresVinculados = familiaresAtualizados,
                            modificadoEm = Date()
                        )
                        amigoRepository.salvar(amigoAtualizado)
                        Timber.d("✅ Familiar removido do amigo: ${amigo.nome}")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao remover familiar do amigo")
            }
        }
    }

    /**
     * Atualiza os dados do amigo (nome e telefone)
     */
    fun atualizarAmigo(amigoId: String, novoNome: String, novoTelefone: String?) {
        val nomeLimpo = novoNome.trim()
        if (nomeLimpo.isEmpty()) {
            _state.update { it.copy(erro = "Nome do amigo não pode ficar vazio") }
            return
        }
        viewModelScope.launch {
            try {
                val amigo = amigoRepository.buscarPorId(amigoId)
                if (amigo != null) {
                    val amigoAtualizado = amigo.copy(
                        nome = nomeLimpo,
                        telefone = novoTelefone?.trim()?.ifBlank { null },
                        modificadoEm = Date()
                    )
                    amigoRepository.salvar(amigoAtualizado)
                    Timber.d("✅ Amigo atualizado: ${amigoAtualizado.nome}")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao atualizar amigo")
                _state.update { it.copy(erro = "Erro ao atualizar amigo: ${e.message}") }
            }
        }
    }
    
    /**
     * Exclui um amigo
     */
    fun excluirAmigo(amigoId: String) {
        viewModelScope.launch {
            try {
                val resultado = amigoRepository.deletar(amigoId)
                resultado.onSuccess {
                    Timber.d("✅ Amigo excluído: $amigoId")
                }.onFailure { erro ->
                    Timber.e(erro, "❌ Erro ao excluir amigo")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao excluir amigo")
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
        if (pessoas.isEmpty()) return FamiliaMontagem(emptyList(), emptySet(), emptyList())

        val pessoasMap = pessoas.associateBy { it.id }
        val resultado = agruparPessoasPorFamiliasComPendentes(
            pessoas, 
            pessoasMap,
            familiasMonoparentaisConfirmadas = familiasMonoparentaisConfirmadas.value,
            familiasMonoparentaisRejeitadas = familiasMonoparentaisRejeitadas.value
        )
        val grupos = resultado.familias
        val familiasPendentes = resultado.familiasPendentes
        val membrosAssociados = mutableSetOf<String>()

        val familias = grupos.mapNotNull { grupo ->
            val familiaId = calcularFamiliaId(grupo, familiaZero)
            val conjuguePrincipal = grupo.conjugue1 ?: grupo.conjugue2 ?: grupo.filhos.firstOrNull()
            val conjugueSecundario = when {
                grupo.conjugue1 != null && grupo.conjugue1.id != conjuguePrincipal?.id -> grupo.conjugue1
                grupo.conjugue2 != null && grupo.conjugue2.id != conjuguePrincipal?.id -> grupo.conjugue2
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

            // Converter parentes colaterais para lista plana
            val parentesColateraisFlatten = grupo.parentesColaterais.values.flatten()
            
            val idsAssociados = mutableSetOf<String>().apply {
                raiz.id.takeIf { it.isNotBlank() }?.let { add(it) }
                conjugueSecundario?.id?.takeIf { it.isNotBlank() }?.let { add(it) }
                grupo.filhos.forEach { filho ->
                    filho.id.takeIf { it.isNotBlank() }?.let { add(it) }
                }
            }
            
            // Membros vinculados manualmente (via campo pessoa.familias)
            val membrosManuais = pessoas.filter { pessoa ->
                pessoa.familias.any { it == familiaId || it.equals(familiaId, ignoreCase = true) } &&
                    pessoa.id.isNotBlank() &&
                    pessoa.id !in idsAssociados
            }.distinctBy { it.id }
            
            // Combinar parentes colaterais e membros manuais, removendo duplicatas
            val todosMembrosExtras = (parentesColateraisFlatten + membrosManuais)
                .distinctBy { it.id }
                .filter { it.id !in idsAssociados } // Garantir que não estão já no núcleo
                .sortedBy { it.nome.lowercase(Locale.getDefault()) }
            
            val familiaBase = FamiliaUiModel(
                id = familiaId,
                nomeExibicao = nomeExibicao,
                nomePadrao = nomePadrao,
                ehFamiliaZero = grupo.ehFamiliaZero,
                ehFamiliaMonoparental = grupo.ehFamiliaMonoparental,
                ehFamiliaReconstituida = grupo.ehFamiliaReconstituida,
                tipoNucleoFamiliar = grupo.tipoNucleoFamiliar,
                conjuguePrincipal = conjuguePrincipal,
                conjugueSecundario = conjugueSecundario,
                treeRoot = treeRoot,
                membrosExtras = todosMembrosExtras
            )

            // Atualizar idsAssociados incluindo membros extras
            familiaBase.membrosFlatten.forEach { item ->
                item.pessoa.id.takeIf { it.isNotBlank() }?.let { idsAssociados.add(it) }
                item.conjuge?.id?.takeIf { it.isNotBlank() }?.let { idsAssociados.add(it) }
            }
            todosMembrosExtras.forEach { membro ->
                membro.id.takeIf { it.isNotBlank() }?.let { idsAssociados.add(it) }
            }

            val familia = familiaBase

            atualizarMembrosAssociados(membrosAssociados, familia)
            familia
        }.sortedWith(
            compareByDescending<FamiliaUiModel> { it.ehFamiliaZero }
                .thenBy { it.nomeExibicao.lowercase(Locale.getDefault()) }
        )

        return FamiliaMontagem(
            familias = familias,
            membrosAssociados = membrosAssociados,
            familiasPendentes = familiasPendentes
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

    /**
     * Monta as informações completas das famílias monoparentais rejeitadas
     */
    private fun montarFamiliasRejeitadas(
        pessoas: List<Pessoa>,
        rejeitados: Set<String>
    ): List<FamiliaMonoparentalPendente> {
        if (rejeitados.isEmpty()) return emptyList()
        
        val pessoasMap = pessoas.associateBy { it.id }
        val familiasRejeitadas = mutableListOf<FamiliaMonoparentalPendente>()
        
        rejeitados.forEach { paiId ->
            val pai = pessoasMap[paiId]
            if (pai != null && pai.genero == Genero.MASCULINO) {
                // Buscar filhos do pai
                val filhosIds = pai.filhos.toMutableSet()
                
                // Filhos também podem ser identificados por terem este pai como pai
                val filhosPorRelacao = pessoas.filter { filho ->
                    filho.pai == paiId || filho.mae == paiId
                }
                filhosIds.addAll(filhosPorRelacao.map { it.id })
                
                val filhos = filhosIds.mapNotNull { pessoasMap[it] }
                    .filter { filho ->
                        filho.pai == paiId || filho.mae == paiId
                    }
                
                if (filhos.isNotEmpty()) {
                    familiasRejeitadas.add(
                        FamiliaMonoparentalPendente(
                            responsavel = pai,
                            filhos = filhos,
                            parentesColaterais = emptyMap()
                        )
                    )
                }
            }
        }
        
        return familiasRejeitadas
    }

    /**
     * Gera nome padrão para a família
     * Suporta famílias monoparentais e casais (incluindo homoafetivos)
     */
    private fun gerarNomePadrao(
        grupo: FamiliaGrupo,
        conjuguePrincipal: Pessoa,
        conjugueSecundario: Pessoa?
    ): String {
        // Família monoparental: usar nome do responsável
        if (grupo.ehFamiliaMonoparental) {
            val primeiroNome = conjuguePrincipal.nome.split(" ").firstOrNull() ?: conjuguePrincipal.nome
            return if (grupo.filhos.size == 1) {
                "$primeiroNome e ${grupo.filhos.firstOrNull()?.nome?.split(" ")?.firstOrNull() ?: "filho"}"
            } else {
                "$primeiroNome e filhos"
            }
        }
        
        // Casal: usar sobrenome comum ou nomes dos cônjuges
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
    val membrosAssociados: Set<String>,
    val familiasPendentes: List<FamiliaMonoparentalPendente> = emptyList()
)

private data class DadosFamilia(
    val familias: List<FamiliaUiModel>,
    val outrosFamiliares: List<Pessoa>,
    val usuarioEhAdmin: Boolean,
    val familiasPendentes: List<FamiliaMonoparentalPendente> = emptyList(),
    val familiasRejeitadas: List<FamiliaMonoparentalPendente> = emptyList(),
    val amigos: List<Amigo> = emptyList()
)

data class FamiliaUiModel(
    val id: String,
    val nomeExibicao: String,
    val nomePadrao: String,
    val ehFamiliaZero: Boolean,
    val ehFamiliaMonoparental: Boolean = false,
    val ehFamiliaReconstituida: Boolean = false,
    val tipoNucleoFamiliar: com.raizesvivas.app.domain.model.TipoNucleoFamiliar = com.raizesvivas.app.domain.model.TipoNucleoFamiliar.PARENTESCO,
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
    val usuarioEhAdmin: Boolean = false,
    val familiasMonoparentaisPendentes: List<FamiliaMonoparentalPendente> = emptyList(),
    val mostrarDialogFamiliaPendente: Boolean = false,
    val familiaPendenteAtual: FamiliaMonoparentalPendente? = null,
    val familiasRejeitadas: List<FamiliaMonoparentalPendente> = emptyList(),
    val amigos: List<Amigo> = emptyList()
)

private fun TreeNodeData.flatten(): List<FamiliaPessoaItem> {
    val resultado = mutableListOf<FamiliaPessoaItem>()
    val pessoasProcessadas = mutableSetOf<String>()
    
    fun adicionarSeNaoDuplicado(item: FamiliaPessoaItem) {
        // Verificar se a pessoa principal já foi processada
        if (item.pessoa.id.isNotBlank() && item.pessoa.id !in pessoasProcessadas) {
            resultado.add(item)
            pessoasProcessadas.add(item.pessoa.id)
        }
        // Verificar se o cônjuge já foi processado (se não for nulo e não estiver na lista)
        item.conjuge?.let { conjuge ->
            if (conjuge.id.isNotBlank() && conjuge.id !in pessoasProcessadas) {
                // O cônjuge será incluído como parte do item, então apenas marcamos como processado
                pessoasProcessadas.add(conjuge.id)
            }
        }
    }
    
    fun processarRecursivo(node: TreeNodeData) {
        val item = FamiliaPessoaItem(
            pessoa = node.pessoa,
            conjuge = node.conjuge,
            nivel = node.nivel
        )
        adicionarSeNaoDuplicado(item)
        
        // Processar filhos recursivamente
        node.children.forEach { filho ->
            processarRecursivo(filho)
        }
    }
    
    processarRecursivo(this)
    return resultado
}

