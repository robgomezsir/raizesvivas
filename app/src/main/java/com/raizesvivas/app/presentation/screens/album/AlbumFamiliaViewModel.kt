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
import com.raizesvivas.app.domain.model.ComentarioFoto
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
    
    // Comentários por foto
    private val _comentariosPorFoto = MutableStateFlow<Map<String, List<ComentarioFoto>>>(emptyMap())
    val comentariosPorFoto = _comentariosPorFoto.asStateFlow()
    
    // Usuário atual (para verificar permissões)
    private val _usuarioAtual = MutableStateFlow<com.raizesvivas.app.domain.model.Usuario?>(null)
    val usuarioAtual = _usuarioAtual.asStateFlow()
    
    private val minhaFamiliaId = MutableStateFlow<String?>(null)
    private var observacaoAtiva: Job? = null
    private val observacoesComentarios = mutableMapOf<String, Job>()
    
    init {
        carregarDados()
    }
    
    /**
     * Carrega pessoas e fotos do álbum
     * App colaborativo: TODOS os usuários autenticados podem ver TODAS as fotos
     */
    private fun carregarDados() {
        viewModelScope.launch {
            try {
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
                
                // Salvar usuário atual para verificação de permissões
                _usuarioAtual.value = usuario
                
                Timber.d("✅ Usuário autenticado: ${usuario.nome}")
                Timber.d("📸 Iniciando observação de TODAS as fotos do álbum (sem filtro de hierarquia)")
                
                // Observar TODAS as fotos - app colaborativo permite acesso global
                observarTodasFotos()
                
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
     * Observa TODAS as fotos do álbum em tempo real
     * App colaborativo: todos os usuários autenticados veem todas as fotos
     */
    private fun observarTodasFotos() {
        // Cancelar observação anterior se existir
        observacaoAtiva?.cancel()
        
        observacaoAtiva = viewModelScope.launch {
            Timber.d("👀 Iniciando observação de TODAS as fotos do álbum (acesso global)")
            fotoAlbumRepository.observarTodasFotos()
                .catch { e ->
                    Timber.e(e, "❌ Erro ao observar todas as fotos do álbum")
                    Timber.e(e, "   Stack trace: ${e.stackTraceToString()}")
                }
                .collect { fotosList ->
                    Timber.d("📸 Fotos atualizadas: ${fotosList.size} fotos recebidas (todas as fotos do álbum)")
                    
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
                    Timber.e("   Caminho absoluto: ${arquivo.absolutePath}")
                    Timber.e("   Arquivo existe: ${arquivo.exists()}")
                    Timber.e("   É arquivo: ${arquivo.isFile}")
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
                
                Timber.d("🔍 RESUMO - FamiliaId da pessoa: ${pessoa.familias.firstOrNull()}, final: $familiaId")
                
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
                
                // Comprimir imagem automaticamente até 300KB
                Timber.d("🗜️ Comprimindo imagem para álbum (máximo 300KB)...")
                val compressedFile = ImageCompressor.compressToFile(
                    imagePath, 
                    targetSizeKB = 300, 
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
                
                // Verificar tamanho após compressão (deve estar <= 300KB)
                val tamanhoKB = compressedFile.length() / 1024
                val tamanhoMaximoKB = 300
                Timber.d("✅ Imagem comprimida: ${compressedFile.absolutePath} (${tamanhoKB}KB)")
                
                // Se ainda estiver acima do limite, tentar comprimir novamente
                var finalFile = compressedFile
                if (tamanhoKB > tamanhoMaximoKB) {
                    Timber.w("⚠️ Imagem comprimida ainda está acima do limite: ${tamanhoKB}KB > ${tamanhoMaximoKB}KB")
                    Timber.d("🔄 Tentando comprimir novamente automaticamente...")
                    
                    // Tentar comprimir novamente a partir do arquivo já comprimido
                    val recompressedFile = ImageCompressor.compressToFile(
                        compressedFile.absolutePath,
                        targetSizeKB = 300,
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
                    Timber.e("   Tipo de erro: ${exception?.javaClass?.simpleName}")
                    Timber.e("   Mensagem: ${exception?.message}")
                    Timber.e("   Tamanho do arquivo: ${finalFile.length() / 1024}KB")
                    Timber.e("   Caminho do arquivo: ${finalFile.absolutePath}")
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
                val saveResult = fotoAlbumRepository.salvarFoto(foto, firebaseUser?.uid)
                saveResult.fold(
                    onSuccess = {
                        Timber.d("✅ Foto salva com sucesso no Firestore")
                        Timber.d("📸 Foto salva com familiaId: $familiaIdFinal")
                        
                        // Limpar arquivo temporário
                        finalFile.delete()
                        
                        // Fotos serão atualizadas automaticamente via observeTodasFotos (sem filtro de familiaId)
                        Timber.d("✅ Foto será atualizada automaticamente via observação de todas as fotos")
                        
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
     * Adiciona ou atualiza um apoio em uma foto
     */
    fun adicionarApoio(foto: FotoAlbum, tipoApoio: com.raizesvivas.app.domain.model.TipoApoioFoto) {
        viewModelScope.launch {
            try {
                val firebaseUser = authService.currentUser
                if (firebaseUser == null) {
                    _state.update { it.copy(erro = "Usuário não autenticado") }
                    return@launch
                }
                
                val resultado = fotoAlbumRepository.adicionarApoio(foto.id, firebaseUser.uid, tipoApoio)
                resultado.fold(
                    onSuccess = {
                        Timber.d("✅ Apoio adicionado com sucesso")
                        _state.update { it.copy(mostrarModalApoio = false, fotoSelecionadaParaApoio = null) }
                    },
                    onFailure = { e ->
                        Timber.e(e, "❌ Erro ao adicionar apoio")
                        _state.update { 
                            it.copy(
                                erro = "Erro ao adicionar apoio: ${e.message}",
                                mostrarModalApoio = false,
                                fotoSelecionadaParaApoio = null
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Erro ao adicionar apoio")
                _state.update { 
                    it.copy(
                        erro = "Erro ao adicionar apoio: ${e.message}",
                        mostrarModalApoio = false,
                        fotoSelecionadaParaApoio = null
                    )
                }
            }
        }
    }
    
    /**
     * Remove um apoio de uma foto
     */
    fun removerApoio(foto: FotoAlbum) {
        viewModelScope.launch {
            try {
                val firebaseUser = authService.currentUser
                if (firebaseUser == null) {
                    _state.update { it.copy(erro = "Usuário não autenticado") }
                    return@launch
                }
                
                val resultado = fotoAlbumRepository.removerApoio(foto.id, firebaseUser.uid)
                resultado.fold(
                    onSuccess = {
                        Timber.d("✅ Apoio removido com sucesso")
                    },
                    onFailure = { e ->
                        Timber.e(e, "❌ Erro ao remover apoio")
                        _state.update { it.copy(erro = "Erro ao remover apoio: ${e.message}") }
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Erro ao remover apoio")
                _state.update { it.copy(erro = "Erro ao remover apoio: ${e.message}") }
            }
        }
    }
    
    /**
     * Abre modal de seleção de apoio ou remove apoio se já existir
     */
    fun abrirModalApoio(foto: FotoAlbum) {
        val firebaseUser = authService.currentUser
        if (firebaseUser == null) {
            _state.update { it.copy(erro = "Usuário não autenticado") }
            return
        }
        
        // Se o usuário já deu apoio, remove diretamente
        if (foto.usuarioDeuApoio(firebaseUser.uid)) {
            removerApoio(foto)
        } else {
            // Caso contrário, abre o modal para escolher o tipo de emoção
            _state.update { 
                it.copy(
                    mostrarModalApoio = true,
                    fotoSelecionadaParaApoio = foto
                )
            }
        }
    }
    
    /**
     * Fecha modal de seleção de apoio
     */
    fun fecharModalApoio() {
        _state.update { 
            it.copy(
                mostrarModalApoio = false,
                fotoSelecionadaParaApoio = null
            )
        }
    }
    
    /**
     * Adiciona um comentário em uma foto
     */
    fun adicionarComentario(foto: FotoAlbum, texto: String) {
        viewModelScope.launch {
            try {
                val firebaseUser = authService.currentUser
                if (firebaseUser == null) {
                    _state.update { it.copy(erro = "Usuário não autenticado") }
                    return@launch
                }
                
                // Buscar dados do usuário
                val usuarioResult = firestoreService.buscarUsuario(firebaseUser.uid)
                val usuario = usuarioResult.getOrNull()
                
                if (usuario == null) {
                    _state.update { it.copy(erro = "Dados do usuário não encontrados") }
                    return@launch
                }
                
                val comentario = ComentarioFoto(
                    fotoId = foto.id,
                    usuarioId = firebaseUser.uid,
                    usuarioNome = usuario.nome,
                    usuarioFotoUrl = usuario.fotoUrl,
                    texto = texto.trim(),
                    criadoEm = java.util.Date()
                )
                
                if (!comentario.validar()) {
                    _state.update { it.copy(erro = "Comentário inválido. Deve ter entre 1 e 500 caracteres.") }
                    return@launch
                }
                
                val resultado = fotoAlbumRepository.adicionarComentario(comentario)
                resultado.fold(
                    onSuccess = {
                        Timber.d("✅ Comentário adicionado com sucesso")
                        _state.update { it.copy(erro = null) }
                        // Expandir comentários e garantir que estejam sendo observados após adicionar
                        if (!_state.value.fotosComComentariosExpandidos.contains(foto.id)) {
                            expandirComentarios(foto.id)
                        } else {
                            // Se já estiver expandido, garantir que a observação esteja ativa
                            observarComentarios(foto.id)
                        }
                    },
                    onFailure = { e ->
                        Timber.e(e, "❌ Erro ao adicionar comentário")
                        _state.update { it.copy(erro = "Erro ao adicionar comentário: ${e.message}") }
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Erro ao adicionar comentário")
                _state.update { it.copy(erro = "Erro ao adicionar comentário: ${e.message}") }
            }
        }
    }
    
    /**
     * Deleta um comentário
     */
    fun deletarComentario(foto: FotoAlbum, comentarioId: String) {
        viewModelScope.launch {
            try {
                val resultado = fotoAlbumRepository.deletarComentario(foto.id, comentarioId)
                resultado.fold(
                    onSuccess = {
                        Timber.d("✅ Comentário deletado com sucesso")
                    },
                    onFailure = { e ->
                        Timber.e(e, "❌ Erro ao deletar comentário")
                        _state.update { it.copy(erro = "Erro ao deletar comentário: ${e.message}") }
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Erro ao deletar comentário")
                _state.update { it.copy(erro = "Erro ao deletar comentário: ${e.message}") }
            }
        }
    }
    
    /**
     * Observa comentários de uma foto
     */
    fun observarComentarios(fotoId: String) {
        // Cancelar observação anterior se existir
        observacoesComentarios[fotoId]?.cancel()
        
        val job = viewModelScope.launch {
            fotoAlbumRepository.observarComentarios(fotoId)
                .collect { comentarios ->
                    // Buscar fotos de perfil para comentários que não têm
                    val comentariosComFoto = comentarios.map { comentario ->
                        if (comentario.usuarioFotoUrl.isNullOrBlank() && comentario.usuarioId.isNotBlank()) {
                            // Buscar foto do usuário
                            val usuarioResult = firestoreService.buscarUsuario(comentario.usuarioId)
                            usuarioResult.getOrNull()?.let { usuario ->
                                if (!usuario.fotoUrl.isNullOrBlank()) {
                                    comentario.copy(usuarioFotoUrl = usuario.fotoUrl)
                                } else {
                                    comentario
                                }
                            } ?: comentario
                        } else {
                            comentario
                        }
                    }
                    
                    _comentariosPorFoto.value = _comentariosPorFoto.value.toMutableMap().apply { 
                        put(fotoId, comentariosComFoto) 
                    }
                }
        }
        
        observacoesComentarios[fotoId] = job
    }
    
    /**
     * Para de observar comentários de uma foto
     */
    fun pararObservarComentarios(fotoId: String) {
        observacoesComentarios[fotoId]?.cancel()
        observacoesComentarios.remove(fotoId)
    }
    
    /**
     * Retorna comentários de uma foto
     */
    fun obterComentarios(fotoId: String): List<ComentarioFoto> {
        return _comentariosPorFoto.value[fotoId] ?: emptyList()
    }
    
    /**
     * Expande comentários de uma foto
     */
    fun expandirComentarios(fotoId: String) {
        _state.update { 
            it.copy(
                fotosComComentariosExpandidos = it.fotosComComentariosExpandidos + fotoId
            )
        }
        // Iniciar observação de comentários quando expandir
        observarComentarios(fotoId)
    }
    
    /**
     * Contrai comentários de uma foto
     */
    fun contrairComentarios(fotoId: String) {
        _state.update { 
            it.copy(
                fotosComComentariosExpandidos = it.fotosComComentariosExpandidos - fotoId
            )
        }
        // Parar observação quando contrair (economizar recursos)
        pararObservarComentarios(fotoId)
    }
    
    /**
     * Verifica se os comentários de uma foto estão expandidos
     */
    fun comentariosExpandidos(fotoId: String): Boolean {
        return _state.value.fotosComComentariosExpandidos.contains(fotoId)
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
    val fotoSelecionadaParaDeletar: FotoAlbum? = null,
    val mostrarModalApoio: Boolean = false,
    val fotoSelecionadaParaApoio: FotoAlbum? = null,
    val fotosComComentariosExpandidos: Set<String> = emptySet() // IDs das fotos com comentários expandidos
)

