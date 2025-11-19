package com.raizesvivas.app.presentation.screens.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.remote.firebase.StorageService
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.data.repository.FotoAlbumRepository
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.domain.model.FotoAlbum
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.utils.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * ViewModel para a tela de Álbum de Família
 */
@HiltViewModel
class AlbumFamiliaViewModel @Inject constructor(
    private val fotoAlbumRepository: FotoAlbumRepository,
    private val pessoaRepository: PessoaRepository,
    private val storageService: StorageService,
    private val authService: AuthService,
    private val firestoreService: FirestoreService
) : ViewModel() {
    
    private val _state = MutableStateFlow(AlbumFamiliaState())
    val state = _state.asStateFlow()
    
    private val _fotos = MutableStateFlow<List<FotoAlbum>>(emptyList())
    val fotos = _fotos.asStateFlow()
    
    private val _pessoas = MutableStateFlow<List<Pessoa>>(emptyList())
    val pessoas = _pessoas.asStateFlow()
    
    private val minhaFamiliaId = MutableStateFlow<String?>(null)
    private var observacaoAtiva: Job? = null
    
    init {
        carregarDados()
    }
    
    /**
     * Carrega pessoas e fotos do álbum
     */
    private fun carregarDados() {
        viewModelScope.launch {
            try {
                // Obter ID da família do usuário atual
                _state.update { it.copy(carregando = true, erro = null) }
                val firebaseUser = authService.currentUser
                if (firebaseUser == null) {
                    _state.update { it.copy(erro = "Usuário não autenticado", carregando = false) }
                    return@launch
                }
                
                // Buscar dados do usuário no Firestore
                val usuarioResult = firestoreService.buscarUsuario(firebaseUser.uid)
                val usuario = usuarioResult.getOrNull()
                
                if (usuario == null) {
                    _state.update { it.copy(erro = "Dados do usuário não encontrados", carregando = false) }
                    return@launch
                }
                
                // Buscar pessoa vinculada ao usuário para obter familiaId
                val pessoaVinculada = usuario.pessoaVinculada
                if (pessoaVinculada != null && pessoaVinculada.isNotBlank()) {
                    val pessoa = pessoaRepository.buscarPorId(pessoaVinculada)
                    pessoa?.let {
                        var familiaId = it.familias.firstOrNull() ?: ""
                        Timber.d("🔍 FamiliaId direto da pessoa vinculada: $familiaId")
                        
                        // Se não encontrou, buscar através de relacionamentos (mesma lógica de adicionar foto)
                        if (familiaId.isBlank()) {
                            Timber.d("🔍 FamiliaId não encontrado diretamente, buscando através de relacionamentos...")
                            
                            // Tentar através do pai
                            if (pessoa.pai != null && pessoa.pai.isNotBlank()) {
                                val pai = pessoaRepository.buscarPorId(pessoa.pai)
                                familiaId = pai?.familias?.firstOrNull() ?: ""
                                Timber.d("🔍 FamiliaId do pai: $familiaId")
                            }
                            
                            // Tentar através da mãe
                            if (familiaId.isBlank() && pessoa.mae != null && pessoa.mae.isNotBlank()) {
                                val mae = pessoaRepository.buscarPorId(pessoa.mae)
                                familiaId = mae?.familias?.firstOrNull() ?: ""
                                Timber.d("🔍 FamiliaId da mãe: $familiaId")
                            }
                            
                            // Tentar através do cônjuge
                            if (familiaId.isBlank() && pessoa.conjugeAtual != null && pessoa.conjugeAtual.isNotBlank()) {
                                val conjuge = pessoaRepository.buscarPorId(pessoa.conjugeAtual)
                                familiaId = conjuge?.familias?.firstOrNull() ?: ""
                                Timber.d("🔍 FamiliaId do cônjuge: $familiaId")
                            }
                            
                            // Tentar através dos filhos (primeiro filho)
                            if (familiaId.isBlank() && pessoa.filhos.isNotEmpty()) {
                                val primeiroFilho = pessoaRepository.buscarPorId(pessoa.filhos.first())
                                familiaId = primeiroFilho?.familias?.firstOrNull() ?: ""
                                Timber.d("🔍 FamiliaId do primeiro filho: $familiaId")
                            }
                            
                            // Se ainda não encontrou, tentar busca recursiva
                            if (familiaId.isBlank()) {
                                Timber.d("🔍 Tentando busca recursiva...")
                                familiaId = buscarFamiliaIdRecursivo(pessoa, pessoaRepository, mutableSetOf(pessoa.id)) ?: ""
                            }
                        }
                        
                        minhaFamiliaId.value = familiaId
                        Timber.d("✅ FamiliaId final para observação: $familiaId")
                        
                        if (familiaId.isNotBlank()) {
                            observarFotos(familiaId)
                        } else {
                            Timber.w("⚠️ Nenhum familiaId encontrado após todas as tentativas")
                            _state.update { it.copy(erro = "Usuário não vinculado a uma família", carregando = false) }
                        }
                    } ?: _state.update { it.copy(erro = "Pessoa vinculada não encontrada", carregando = false) }
                } else {
                    _state.update { it.copy(erro = "Usuário não vinculado a uma pessoa", carregando = false) }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar dados do álbum")
                _state.update { it.copy(erro = "Erro ao carregar álbum: ${e.message}", carregando = false) }
            }
        }
        
        // Observar todas as pessoas em corrotina separada
        viewModelScope.launch {
            pessoaRepository.observarTodasPessoas()
                .collect { pessoasList ->
                    _pessoas.value = pessoasList
                }
        }
    }
    
    /**
     * Observa fotos do álbum em tempo real
     */
    private fun observarFotos(familiaId: String) {
        // Cancelar observação anterior se existir
        observacaoAtiva?.cancel()
        
        observacaoAtiva = viewModelScope.launch {
            Timber.d("👀 Iniciando observação de fotos para família: $familiaId")
            fotoAlbumRepository.observarFotosPorFamilia(familiaId)
                .catch { e ->
                    Timber.e(e, "❌ Erro ao observar fotos para familiaId: $familiaId")
                    Timber.e(e, "   Stack trace: ${e.stackTraceToString()}")
                }
                .collect { fotosList ->
                    Timber.d("📸 Fotos atualizadas: ${fotosList.size} fotos recebidas para familiaId: $familiaId")
                    
                    // Deduplicar fotos por ID para evitar cache duplicado
                    val fotosDeduplicadas = fotosList.distinctBy { it.id }
                    if (fotosDeduplicadas.size != fotosList.size) {
                        Timber.w("⚠️ Fotos duplicadas detectadas: ${fotosList.size} -> ${fotosDeduplicadas.size}")
                    }
                    
                    fotosDeduplicadas.forEach { foto ->
                        Timber.d("   - Foto: ${foto.id}, pessoa: ${foto.pessoaNome}, familiaId: ${foto.familiaId}, URL: ${foto.url.take(50)}...")
                    }
                    _fotos.value = fotosDeduplicadas
                    _state.update { it.copy(carregando = false) }
                }
        }
    }
    
    /**
     * Verifica se uma pessoa pode receber mais fotos (limite de 5)
     */
    fun podeAdicionarFoto(pessoaId: String): Boolean {
        val fotosDaPessoa = _fotos.value.count { it.pessoaId == pessoaId }
        return fotosDaPessoa < 5
    }
    
    /**
     * Conta quantas fotos uma pessoa já tem
     */
    fun contarFotosPessoa(pessoaId: String): Int {
        return _fotos.value.count { it.pessoaId == pessoaId }
    }
    
    /**
     * Adiciona uma nova foto ao álbum
     */
    fun adicionarFoto(imagePath: String, pessoaId: String, descricao: String) {
        viewModelScope.launch {
            try {
                Timber.d("📸 Iniciando adição de foto - pessoaId: $pessoaId, imagePath: $imagePath")
                _state.update { it.copy(carregando = true, erro = null) }
                
                // Validar se o arquivo existe
                val arquivo = File(imagePath)
                Timber.d("🔍 Verificando arquivo: $imagePath, existe: ${arquivo.exists()}")
                if (!arquivo.exists()) {
                    Timber.e("❌ Arquivo de imagem não existe: $imagePath")
                    _state.update { 
                        it.copy(
                            carregando = false,
                            erro = "Arquivo de imagem não encontrado. Tente selecionar a imagem novamente.",
                            mostrarModalAdicionar = false
                        )
                    }
                    return@launch
                }
                
                // Validar limite de 5 fotos
                val fotosAtuais = contarFotosPessoa(pessoaId)
                Timber.d("📊 Fotos atuais da pessoa: $fotosAtuais/5")
                if (!podeAdicionarFoto(pessoaId)) {
                    Timber.w("⚠️ Limite de fotos atingido para pessoa: $pessoaId")
                    _state.update { 
                        it.copy(
                            carregando = false,
                            erro = "Esta pessoa já possui 5 fotos. Remova uma foto antes de adicionar outra.",
                            mostrarModalAdicionar = false
                        )
                    }
                    return@launch
                }
                
                // Buscar dados da pessoa
                Timber.d("🔍 Buscando pessoa: $pessoaId")
                val pessoa = pessoaRepository.buscarPorId(pessoaId)
                if (pessoa == null) {
                    Timber.e("❌ Pessoa não encontrada: $pessoaId")
                    _state.update { 
                        it.copy(
                            carregando = false,
                            erro = "Pessoa não encontrada",
                            mostrarModalAdicionar = false
                        )
                    }
                    return@launch
                }
                Timber.d("✅ Pessoa encontrada: ${pessoa.nome}")
                Timber.d("🔍 Pessoa tem pai: ${pessoa.pai != null}, mãe: ${pessoa.mae != null}, cônjuge: ${pessoa.conjugeAtual != null}, filhos: ${pessoa.filhos.size}")
                
                // Tentar encontrar familiaId de várias formas
                var familiaId = pessoa.familias.firstOrNull()
                Timber.d("🔍 FamiliaId direto da pessoa: $familiaId")
                
                // Se não encontrou na pessoa, tentar através de relacionamentos
                if (familiaId.isNullOrBlank()) {
                    Timber.d("🔍 FamiliaId não encontrado diretamente na pessoa, buscando através de relacionamentos...")
                    
                    // Tentar através do pai
                    pessoa.pai?.let { paiId ->
                        Timber.d("🔍 Buscando familiaId através do pai: $paiId")
                        val pai = pessoaRepository.buscarPorId(paiId)
                        if (pai != null) {
                            val familiaIdPai = pai.familias.firstOrNull()
                            Timber.d("🔍 FamiliaId do pai: $familiaIdPai")
                            if (!familiaIdPai.isNullOrBlank()) {
                                familiaId = familiaIdPai
                                Timber.d("✅ FamiliaId encontrado através do pai: $familiaId")
                            }
                        } else {
                            Timber.w("⚠️ Pai não encontrado: $paiId")
                        }
                    }
                    
                    // Tentar através da mãe
                    if (familiaId.isNullOrBlank()) {
                        pessoa.mae?.let { maeId ->
                            Timber.d("🔍 Buscando familiaId através da mãe: $maeId")
                            val mae = pessoaRepository.buscarPorId(maeId)
                            if (mae != null) {
                                val familiaIdMae = mae.familias.firstOrNull()
                                Timber.d("🔍 FamiliaId da mãe: $familiaIdMae")
                                if (!familiaIdMae.isNullOrBlank()) {
                                    familiaId = familiaIdMae
                                    Timber.d("✅ FamiliaId encontrado através da mãe: $familiaId")
                                }
                            } else {
                                Timber.w("⚠️ Mãe não encontrada: $maeId")
                            }
                        }
                    }
                    
                    // Tentar através do cônjuge
                    if (familiaId.isNullOrBlank()) {
                        pessoa.conjugeAtual?.let { conjugeId ->
                            Timber.d("🔍 Buscando familiaId através do cônjuge: $conjugeId")
                            val conjuge = pessoaRepository.buscarPorId(conjugeId)
                            if (conjuge != null) {
                                val familiaIdConjuge = conjuge.familias.firstOrNull()
                                Timber.d("🔍 FamiliaId do cônjuge: $familiaIdConjuge")
                                if (!familiaIdConjuge.isNullOrBlank()) {
                                    familiaId = familiaIdConjuge
                                    Timber.d("✅ FamiliaId encontrado através do cônjuge: $familiaId")
                                }
                            } else {
                                Timber.w("⚠️ Cônjuge não encontrado: $conjugeId")
                            }
                        }
                    }
                    
                    // Tentar através dos filhos (primeiro filho)
                    if (familiaId.isNullOrBlank() && pessoa.filhos.isNotEmpty()) {
                        val primeiroFilhoId = pessoa.filhos.first()
                        Timber.d("🔍 Buscando familiaId através do primeiro filho: $primeiroFilhoId")
                        val primeiroFilho = pessoaRepository.buscarPorId(primeiroFilhoId)
                        if (primeiroFilho != null) {
                            val familiaIdFilho = primeiroFilho.familias.firstOrNull()
                            Timber.d("🔍 FamiliaId do filho: $familiaIdFilho")
                            if (!familiaIdFilho.isNullOrBlank()) {
                                familiaId = familiaIdFilho
                                Timber.d("✅ FamiliaId encontrado através do filho: $familiaId")
                            }
                        } else {
                            Timber.w("⚠️ Filho não encontrado: $primeiroFilhoId")
                        }
                    }
                }
                
                // Se ainda não encontrou, usar minhaFamiliaId (família do usuário logado)
                if (familiaId.isNullOrBlank()) {
                    Timber.d("🔍 Tentando usar minhaFamiliaId: ${minhaFamiliaId.value}")
                    familiaId = minhaFamiliaId.value
                    if (!familiaId.isNullOrBlank()) {
                        Timber.d("✅ Usando FamiliaId do usuário logado: $familiaId")
                    } else {
                        Timber.w("⚠️ minhaFamiliaId também está vazio")
                    }
                }
                
                // Se ainda não encontrou, tentar buscar recursivamente através de toda a árvore genealógica
                if (familiaId.isNullOrBlank()) {
                    Timber.d("🔍 Nenhum familiaId encontrado, tentando busca recursiva na árvore genealógica...")
                    familiaId = buscarFamiliaIdRecursivo(pessoa, pessoaRepository, mutableSetOf(pessoa.id))
                }
                
                // Se ainda não encontrou, usar o próprio ID da pessoa como familiaId (cria uma família individual)
                if (familiaId.isNullOrBlank()) {
                    Timber.w("⚠️ Nenhum familiaId encontrado após todas as tentativas. Usando ID da pessoa como familiaId temporário.")
                    familiaId = pessoa.id
                    Timber.d("✅ Usando ID da pessoa como familiaId: $familiaId")
                }
                
                Timber.d("🔍 RESUMO - FamiliaId da pessoa: ${pessoa.familias.firstOrNull()}, minhaFamiliaId: ${minhaFamiliaId.value}, final: $familiaId")
                
                // Criar uma variável val para evitar problemas de smart cast
                val familiaIdFinal = familiaId ?: ""
                if (familiaIdFinal.isBlank()) {
                    Timber.e("❌ Família não encontrada para pessoa: ${pessoa.nome}")
                    _state.update { 
                        it.copy(
                            carregando = false,
                            erro = "Família não encontrada. A pessoa precisa estar vinculada a uma família para adicionar fotos ao álbum.",
                            mostrarModalAdicionar = false
                        )
                    }
                    return@launch
                }
                
                Timber.d("✅ FamiliaId encontrado: $familiaIdFinal")
                
                // Comprimir imagem automaticamente até 500KB
                Timber.d("🗜️ Comprimindo imagem para álbum (máximo 500KB)...")
                val compressedFile = ImageCompressor.compressToFile(
                    imagePath, 
                    targetSizeKB = 500, 
                    paraPerfil = false,
                    paraAlbum = true
                )
                if (compressedFile == null) {
                    Timber.e("❌ Erro ao comprimir imagem")
                    _state.update { 
                        it.copy(
                            carregando = false,
                            erro = "Erro ao comprimir imagem. Verifique se o arquivo é uma imagem válida.",
                            mostrarModalAdicionar = false
                        )
                    }
                    return@launch
                }
                
                // Verificar tamanho após compressão (deve estar <= 500KB)
                val tamanhoKB = compressedFile.length() / 1024
                val tamanhoMaximoKB = 500
                Timber.d("✅ Imagem comprimida: ${compressedFile.absolutePath} (${tamanhoKB}KB)")
                
                // Se ainda estiver acima do limite, tentar comprimir novamente
                var finalFile = compressedFile
                if (tamanhoKB > tamanhoMaximoKB) {
                    Timber.w("⚠️ Imagem comprimida ainda está acima do limite: ${tamanhoKB}KB > ${tamanhoMaximoKB}KB")
                    Timber.d("🔄 Tentando comprimir novamente automaticamente...")
                    
                    // Tentar comprimir novamente a partir do arquivo já comprimido
                    val recompressedFile = ImageCompressor.compressToFile(
                        compressedFile.absolutePath,
                        targetSizeKB = 500,
                        paraPerfil = false,
                        paraAlbum = true
                    )
                    
                    // Deletar arquivo anterior
                    compressedFile.delete()
                    
                    if (recompressedFile == null || recompressedFile.length() / 1024 > tamanhoMaximoKB) {
                        recompressedFile?.delete()
                        _state.update { 
                            it.copy(
                                carregando = false,
                                erro = "Não foi possível comprimir a imagem para o tamanho máximo de ${tamanhoMaximoKB}KB. " +
                                       "A imagem pode ser muito complexa. Tente usar uma imagem menor ou com menos detalhes.",
                                mostrarModalAdicionar = false
                            )
                        }
                        return@launch
                    }
                    
                    finalFile = recompressedFile
                    val finalTamanhoKB = finalFile.length() / 1024
                    Timber.d("✅ Imagem recomprimida automaticamente com sucesso: ${finalTamanhoKB}KB")
                }
                
                // Gerar ID único para a foto
                val fotoId = UUID.randomUUID().toString()
                Timber.d("📤 Fazendo upload da foto para Storage...")
                
                // Upload para Storage
                val uploadResult = storageService.uploadFotoAlbum(finalFile, pessoaId, fotoId)
                val fotoUrl = uploadResult.getOrNull()
                
                if (fotoUrl == null) {
                    val exception = uploadResult.exceptionOrNull()
                    Timber.e(exception, "❌ Erro ao fazer upload da foto")
                    finalFile.delete()
                    _state.update { 
                        it.copy(
                            carregando = false,
                            erro = "Erro ao fazer upload da foto: ${exception?.message ?: "Erro desconhecido"}",
                            mostrarModalAdicionar = false
                        )
                    }
                    return@launch
                }
                Timber.d("✅ Upload concluído: $fotoUrl")
                
                // Salvar no Firestore
                val firebaseUser = authService.currentUser
                val foto = FotoAlbum(
                    id = fotoId, // Usar o mesmo ID gerado para Storage
                    familiaId = familiaIdFinal,
                    pessoaId = pessoaId,
                    pessoaNome = pessoa.nome,
                    url = fotoUrl,
                    descricao = descricao,
                    criadoPor = firebaseUser?.uid ?: "",
                    criadoEm = Date(),
                    ordem = _fotos.value.size
                )
                
                Timber.d("💾 Salvando foto no Firestore...")
                val saveResult = fotoAlbumRepository.salvarFoto(foto)
                saveResult.fold(
                    onSuccess = {
                        Timber.d("✅ Foto salva com sucesso no Firestore")
                        Timber.d("📸 Foto salva com familiaId: $familiaIdFinal")
                        Timber.d("👀 Observando fotos para familiaId: ${minhaFamiliaId.value}")
                        
                        // Limpar arquivo temporário
                        finalFile.delete()
                        
                        // Verificar se estamos observando o familiaId correto
                        // Se minhaFamiliaId está vazio OU diferente do familiaId da foto, atualizar observação
                        val precisaAtualizarObservacao = minhaFamiliaId.value.isNullOrBlank() || 
                                                          familiaIdFinal != minhaFamiliaId.value
                        
                        if (precisaAtualizarObservacao) {
                            if (minhaFamiliaId.value.isNullOrBlank()) {
                                Timber.w("⚠️ minhaFamiliaId está vazio! Atualizando para: $familiaIdFinal")
                            } else {
                                Timber.w("⚠️ Foto salva com familiaId diferente do observado!")
                                Timber.w("   Foto familiaId: $familiaIdFinal")
                                Timber.w("   Observando familiaId: ${minhaFamiliaId.value}")
                            }
                            Timber.w("   🔄 Atualizando observação para usar familiaId da foto...")
                            
                            // Atualizar minhaFamiliaId e reiniciar observação
                            minhaFamiliaId.value = familiaIdFinal
                            observarFotos(familiaIdFinal)
                            
                            // Aguardar um pouco para a observação atualizar
                            kotlinx.coroutines.delay(1000)
                            
                            // Verificar se a foto aparece agora
                            val fotoJaExiste = _fotos.value.any { it.id == fotoId }
                            if (!fotoJaExiste) {
                                Timber.w("   📸 Foto ainda não encontrada. Recarregando manualmente...")
                                // Recarregar fotos manualmente como fallback
                                viewModelScope.launch {
                                    val fotosResult = fotoAlbumRepository.buscarFotosPorFamilia(familiaIdFinal)
                                    fotosResult.fold(
                                        onSuccess = { fotos ->
                                            Timber.d("✅ Fotos recarregadas manualmente: ${fotos.size} fotos")
                                            _fotos.value = fotos
                                        },
                                        onFailure = { e ->
                                            Timber.e(e, "❌ Erro ao recarregar fotos")
                                        }
                                    )
                                }
                            } else {
                                Timber.d("✅ Foto encontrada na lista após atualizar observação!")
                            }
                        } else {
                            Timber.d("✅ Foto salva com o mesmo familiaId que estamos observando. Deve aparecer automaticamente.")
                        }
                        
                        // Fotos serão atualizadas automaticamente via observeFotos
                        
                        Timber.d("✅ Fechando modal de adicionar foto após sucesso")
                        _state.update { 
                            it.copy(
                                carregando = false,
                                mostrarModalAdicionar = false
                            )
                        }
                        Timber.d("✅ Estado atualizado: mostrarModalAdicionar = false")
                    },
                    onFailure = { e ->
                        Timber.e(e, "❌ Erro ao salvar foto no Firestore")
                        // Tentar deletar foto do Storage se falhou no Firestore
                        try {
                            val caminho = storageService.gerarCaminhoFotoAlbum(pessoaId, fotoId)
                            storageService.deletarImagem(caminho)
                            Timber.d("🗑️ Foto removida do Storage após falha no Firestore")
                        } catch (deleteError: Exception) {
                            Timber.e(deleteError, "❌ Erro ao deletar foto do Storage")
                        }
                        
                        _state.update { 
                            it.copy(
                                carregando = false,
                                erro = "Erro ao salvar foto: ${e.message}"
                            )
                        }
                    }
                )
                
            } catch (e: Exception) {
                Timber.e(e, "Erro ao adicionar foto")
                _state.update { 
                    it.copy(
                        carregando = false,
                        erro = "Erro ao adicionar foto: ${e.message}",
                        mostrarModalAdicionar = false
                    )
                }
            }
        }
    }
    
    /**
     * Deleta uma foto do álbum
     */
    fun deletarFoto(foto: FotoAlbum) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(carregando = true, erro = null) }
                
                // Deletar do Firestore
                val deleteResult = fotoAlbumRepository.deletarFoto(foto.id)
                deleteResult.fold(
                    onSuccess = {
                        Timber.d("🗑️ Foto deletada do Firestore: ${foto.id}")
                        
                        // Remover foto da lista local imediatamente (otimização)
                        _fotos.value = _fotos.value.filter { it.id != foto.id }
                        Timber.d("🗑️ Foto removida da lista local. Fotos restantes: ${_fotos.value.size}")
                        
                        // Deletar do Storage
                        try {
                            val caminho = storageService.gerarCaminhoFotoAlbum(foto.pessoaId, foto.id)
                            storageService.deletarImagem(caminho)
                            Timber.d("🗑️ Foto deletada do Storage: $caminho")
                        } catch (e: Exception) {
                            Timber.e(e, "⚠️ Erro ao deletar foto do Storage, mas continuando...")
                        }
                        
                        // Fotos serão atualizadas automaticamente via observeFotos
                        // Mas já removemos localmente para feedback imediato
                        
                        Timber.d("✅ Fechando modal de deletar foto após sucesso")
                        _state.update { 
                            it.copy(
                                carregando = false,
                                mostrarModalDeletar = false,
                                fotoSelecionadaParaDeletar = null
                            )
                        }
                        Timber.d("✅ Estado atualizado: mostrarModalDeletar = false")
                    },
                    onFailure = { e ->
                        _state.update { 
                            it.copy(
                                carregando = false,
                                erro = "Erro ao deletar foto: ${e.message}",
                                mostrarModalDeletar = false,
                                fotoSelecionadaParaDeletar = null
                            )
                        }
                    }
                )
                
            } catch (e: Exception) {
                Timber.e(e, "Erro ao deletar foto")
                _state.update { 
                    it.copy(
                        carregando = false,
                        erro = "Erro ao deletar foto: ${e.message}",
                        mostrarModalDeletar = false,
                        fotoSelecionadaParaDeletar = null
                    )
                }
            }
        }
    }
    
    /**
     * Abre modal de adicionar foto
     */
    fun abrirModalAdicionar() {
        _state.update { it.copy(mostrarModalAdicionar = true) }
    }
    
    /**
     * Fecha modal de adicionar foto
     */
    fun fecharModalAdicionar() {
        _state.update { it.copy(mostrarModalAdicionar = false) }
    }
    
    /**
     * Abre modal de deletar foto
     */
    fun abrirModalDeletar(foto: FotoAlbum) {
        _state.update { 
            it.copy(
                mostrarModalDeletar = true,
                fotoSelecionadaParaDeletar = foto
            )
        }
    }
    
    /**
     * Fecha modal de deletar foto
     */
    fun fecharModalDeletar() {
        _state.update { 
            it.copy(
                mostrarModalDeletar = false,
                fotoSelecionadaParaDeletar = null
            )
        }
    }
    
    /**
     * Limpa mensagem de erro
     */
    fun limparErro() {
        _state.update { it.copy(erro = null) }
    }
    
    /**
     * Busca familiaId recursivamente através da árvore genealógica
     * Evita loops infinitos usando um conjunto de IDs visitados
     */
    private suspend fun buscarFamiliaIdRecursivo(
        pessoa: Pessoa,
        pessoaRepository: PessoaRepository,
        visitados: MutableSet<String>,
        profundidade: Int = 0
    ): String? {
        // Limitar profundidade para evitar loops muito profundos
        if (profundidade > 5) {
            Timber.w("⚠️ Profundidade máxima atingida na busca recursiva")
            return null
        }
        
        // Verificar se já visitamos esta pessoa
        if (visitados.contains(pessoa.id)) {
            return null
        }
        visitados.add(pessoa.id)
        
        // Verificar se a pessoa tem familiaId
        val familiaId = pessoa.familias.firstOrNull()
        if (!familiaId.isNullOrBlank()) {
            Timber.d("✅ FamiliaId encontrado recursivamente na profundidade $profundidade: $familiaId")
            return familiaId
        }
        
        // Buscar recursivamente nos relacionamentos
        val relacionamentos = mutableListOf<String?>()
        relacionamentos.add(pessoa.pai)
        relacionamentos.add(pessoa.mae)
        relacionamentos.add(pessoa.conjugeAtual)
        relacionamentos.addAll(pessoa.filhos)
        
        for (relacionadoId in relacionamentos.filterNotNull()) {
            if (!visitados.contains(relacionadoId)) {
                val relacionado = pessoaRepository.buscarPorId(relacionadoId)
                relacionado?.let {
                    val resultado = buscarFamiliaIdRecursivo(it, pessoaRepository, visitados, profundidade + 1)
                    if (!resultado.isNullOrBlank()) {
                        return resultado
                    }
                }
            }
        }
        
        return null
    }
}

/**
 * Estado da tela de Álbum de Família
 */
data class AlbumFamiliaState(
    val carregando: Boolean = true,
    val erro: String? = null,
    val mostrarModalAdicionar: Boolean = false,
    val mostrarModalDeletar: Boolean = false,
    val fotoSelecionadaParaDeletar: FotoAlbum? = null
)

