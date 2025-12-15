package com.raizesvivas.app.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.local.BiometricCrypto
import com.raizesvivas.app.data.local.BiometricPreferences
import com.raizesvivas.app.data.local.BiometricService
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.repository.GamificacaoRepository
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.domain.model.TipoAcao
import com.raizesvivas.app.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel para a tela de Login
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authService: AuthService,
    private val biometricService: BiometricService,
    private val biometricPreferences: BiometricPreferences,
    private val biometricCrypto: BiometricCrypto,
    private val gamificacaoRepository: GamificacaoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val notificacaoRepository: com.raizesvivas.app.data.repository.NotificacaoRepository,
    private val syncManager: com.raizesvivas.app.data.sync.SyncManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()
    
    init {
        // Carregar último email e verificar se biometria está disponível
        viewModelScope.launch {
            val lastEmail = biometricPreferences.getLastEmailSync()
            val biometricAvailable = biometricService.isBiometricAvailable()
            val biometricEnabled = biometricPreferences.isBiometricEnabledSync()
            val keepConnected = biometricPreferences.isKeepConnectedSync()
            val lastAuthTimestamp = biometricPreferences.getLastAuthTimestampSync()
            
            Timber.d("🔐 Biometria - lastEmail: $lastEmail, available: $biometricAvailable, enabled: $biometricEnabled, keepConnected: $keepConnected")
            
            // Verificar expiração de sessão (24h)
            val currentTime = System.currentTimeMillis()
            val isSessionValid = keepConnected && (currentTime - lastAuthTimestamp < 24 * 60 * 60 * 1000)
            
            if (isSessionValid && authService.currentUser != null) {
                Timber.d("✅ Sessão válida e 'Manter conectado' ativo - Login automático imediato")
                _state.update {
                    it.copy(
                        lastEmail = lastEmail,
                        biometricAvailable = biometricAvailable,
                        biometricEnabled = biometricEnabled && biometricAvailable,
                        keepConnected = true,
                        loginSuccess = true // Pula login e biometria
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        lastEmail = lastEmail,
                        biometricAvailable = biometricAvailable,
                        biometricEnabled = biometricEnabled && biometricAvailable,
                        keepConnected = keepConnected
                    )
                }
            }
        }
    }
    
    /**
     * Atualiza o email digitado
     */
    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }
    
    /**
     * Atualiza a senha digitada
     */
    fun onSenhaChanged(senha: String) {
        _state.update { it.copy(senha = senha, senhaError = null) }
    }
    
    /**
     * Atualiza a preferência "Manter conectado"
     */
    fun onKeepConnectedChanged(enabled: Boolean) {
        _state.update { it.copy(keepConnected = enabled) }
    }
    
    /**
     * Valida campos e faz login
     */
    fun login() {
        // Limpar erros anteriores
        _state.update { it.copy(emailError = null, senhaError = null, error = null) }
        
        // Validar email
        val emailValidation = ValidationUtils.validarEmail(_state.value.email)
        if (!emailValidation.isValid) {
            _state.update { it.copy(emailError = emailValidation.errorMessage) }
            return
        }
        
        // Validar senha
        if (_state.value.senha.isBlank()) {
            _state.update { it.copy(senhaError = "Senha é obrigatória") }
            return
        }
        
        // Fazer login
        _state.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            val result = authService.login(
                email = _state.value.email.trim(),
                senha = _state.value.senha
            )
            
            result.onSuccess { user ->
                Timber.d("✅ Login bem-sucedido: ${user.uid}")
                
                // Renovar cache do Firestore para garantir dados atualizados
                Timber.d("🔄 Renovando cache do Firestore após login...")
                viewModelScope.launch {
                    syncManager.syncIncremental(forceSync = true).collect { syncResult ->
                        when (syncResult) {
                            is com.raizesvivas.app.data.sync.SyncResult.Success -> {
                                Timber.d("✅ Cache renovado com sucesso: ${syncResult.message}")
                            }
                            is com.raizesvivas.app.data.sync.SyncResult.Error -> {
                                Timber.w("⚠️ Erro ao renovar cache: ${syncResult.message}")
                                // Continuar mesmo com erro - sincronização em tempo real vai compensar
                            }
                            is com.raizesvivas.app.data.sync.SyncResult.InProgress -> {
                                Timber.d("🔄 Sincronizando cache: ${syncResult.progress}%")
                            }
                        }
                    }
                }
                
                // Registrar ação de primeiro login para gamificação
                viewModelScope.launch {
                    gamificacaoRepository.registrarAcao(user.uid, TipoAcao.PRIMEIRO_LOGIN)
                }
                
                // Obter e salvar token FCM
                viewModelScope.launch {
                    try {
                        val token = notificacaoRepository.getFCMToken()
                        if (token != null) {
                            notificacaoRepository.updateFCMToken(token)
                            Timber.d("✅ Token FCM obtido e salvo: $token")
                            
                            // Log extra para garantir visibilidade
                            android.util.Log.d("FCM_LOGIN", "════════════════════════════════════════")
                            android.util.Log.d("FCM_LOGIN", "Token FCM: $token")
                            android.util.Log.d("FCM_LOGIN", "════════════════════════════════════════")
                        } else {
                            Timber.w("⚠️ Token FCM não pôde ser obtido")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Erro ao obter/salvar token FCM")
                    }
                }
                
                // IMPORTANTE: Usar o email do Firebase primeiro, pois é a fonte de verdade
                // Se não houver email no Firebase, usar o email digitado
                val emailFromFirebase = user.email?.trim()?.lowercase()
                val emailFromState = _state.value.email.trim().lowercase()
                val email = emailFromFirebase ?: emailFromState
                
                Timber.d("🔐 Email do Firebase: $emailFromFirebase")
                Timber.d("🔐 Email do estado: $emailFromState")
                Timber.d("🔐 Email final usado: $email")
                
                // Verificar novamente se biometria está disponível (pode ter mudado desde init)
                val biometricAvailableNow = biometricService.isBiometricAvailable()
                Timber.d("🔐 Biometria disponível agora: $biometricAvailableNow")
                
                // Salvar timestamp da autenticação e preferência de manter conectado
                val currentTime = System.currentTimeMillis()
                biometricPreferences.saveLastAuthTimestamp(currentTime)
                biometricPreferences.saveKeepConnected(_state.value.keepConnected)

                // Sempre salvar email primeiro para garantir consistência
                // O email será normalizado dentro do saveLastEmail
                biometricPreferences.saveLastEmail(email)
                Timber.d("🔐 Email salvo no BiometricPreferences: $email")
                
                // Verificar se o email foi salvo corretamente
                val savedEmail = biometricPreferences.getLastEmailSync()
                Timber.d("🔐 Email recuperado do BiometricPreferences: $savedEmail")
                if (savedEmail != email) {
                    Timber.e("❌ ERRO: Email salvo ($savedEmail) não corresponde ao email esperado ($email)")
                    Timber.e("❌ Isso pode causar problemas ao recuperar a senha")
                }
                
                // Sempre salvar senha se biometria estiver disponível
                // Isso garante que a senha estará disponível para login biométrico futuro
                if (biometricAvailableNow) {
                    Timber.d("🔐 Biometria disponível - salvando senha para login futuro")
                    Timber.d("🔐 Email usado para salvar senha: '$email'")
                    Timber.d("🔐 Senha tem ${_state.value.senha.length} caracteres")
                    
                    try {
                        // Salvar senha usando o mesmo email que será usado para recuperar
                        biometricCrypto.savePassword(email, _state.value.senha)
                        
                        // Aguardar um pouco para garantir que a senha foi salva
                        kotlinx.coroutines.delay(500)
                        
                        // Verificar se foi salva corretamente usando o MESMO email
                        val savedPasswordCheck = biometricCrypto.getPassword(email)
                        if (savedPasswordCheck != null && savedPasswordCheck == _state.value.senha) {
                            Timber.d("✅ Confirmação: Senha foi salva e pode ser recuperada corretamente")
                            Timber.d("✅ Senha recuperada tem ${savedPasswordCheck.length} caracteres")
                            Timber.d("✅ Email usado para salvar e recuperar: '$email'")
                        } else {
                            Timber.e("❌ ERRO CRÍTICO: Senha não pode ser recuperada após salvar!")
                            Timber.e("❌ Isso pode causar problemas no login biométrico futuro")
                            Timber.e("❌ Email usado: '$email'")
                            if (savedPasswordCheck != null) {
                                Timber.e("❌ Senha recuperada tem ${savedPasswordCheck.length} caracteres, esperado ${_state.value.senha.length}")
                                Timber.e("❌ Senhas são iguais: ${savedPasswordCheck == _state.value.senha}")
                            } else {
                                Timber.e("❌ Senha recuperada é null")
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Erro ao salvar senha para biometria")
                        // Continuar com o login mesmo se falhar ao salvar senha
                        // O usuário ainda pode fazer login manualmente
                    }
                    
                    // Se biometria não estava habilitada, habilitar automaticamente
                    if (!_state.value.biometricEnabled) {
                        Timber.d("🔐 Habilitando biometria automaticamente")
                        biometricPreferences.setBiometricEnabled(email, true)
                        _state.update { 
                            it.copy(
                                isLoading = false, 
                                loginSuccess = true,
                                biometricEnabled = true,
                                biometricAvailable = true,
                                lastEmail = email
                            )
                        }
                    } else {
                        // Se já estava habilitada, apenas atualizar estado
                        _state.update { 
                            it.copy(
                                isLoading = false, 
                                loginSuccess = true,
                                biometricAvailable = true,
                                lastEmail = email
                            ) 
                        }
                    }
                } else {
                    // Biometria não disponível - apenas fazer login
                    Timber.d("⚠️ Biometria não disponível - senha não será salva")
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            loginSuccess = true,
                            lastEmail = email
                        ) 
                    }
                }
            }
            
            result.onFailure { error ->
                Timber.e(error, "❌ Erro no login")
                
                // EXCEÇÃO ESPECIAL: robgomez.sir@gmail.com sempre pode fazer login se for admin
                // Verificar diretamente se o usuário existe e é admin, independente de outros admins
                val email = _state.value.email.trim().lowercase()
                val isRobgomez = email == "robgomez.sir@gmail.com"
                val isPermissionDenied = error.message?.contains("PERMISSION_DENIED") == true ||
                    error.message?.contains("Cadastro permitido apenas por convite") == true
                
                Timber.d("🔐 Verificando exceção para robgomez: isRobgomez=$isRobgomez, isPermissionDenied=$isPermissionDenied")
                
                if (isRobgomez && isPermissionDenied) {
                    Timber.d("🔐 EXCEÇÃO ATIVADA: robgomez.sir@gmail.com tentando login - verificando se usuário existe e é admin no Firestore")
                    
                    viewModelScope.launch {
                        // Buscar todos os usuários para encontrar o robgomez
                        val resultadoUsuarios = usuarioRepository.buscarTodosUsuarios()
                        resultadoUsuarios.onSuccess { usuarios ->
                            Timber.d("🔐 Total de usuários encontrados: ${usuarios.size}")
                            val usuarioRobgomez = usuarios.find { 
                                it.email.trim().lowercase() == "robgomez.sir@gmail.com" 
                            }
                            
                            Timber.d("🔐 Usuário robgomez encontrado: ${usuarioRobgomez != null}")
                            if (usuarioRobgomez != null) {
                                Timber.d("🔐 Usuário robgomez - ehAdministrador: ${usuarioRobgomez.ehAdministrador}, ehAdministradorSenior: ${usuarioRobgomez.ehAdministradorSenior}")
                            }
                            
                            if (usuarioRobgomez != null && 
                                (usuarioRobgomez.ehAdministrador || usuarioRobgomez.ehAdministradorSenior)) {
                                Timber.d("✅ Usuário robgomez.sir@gmail.com encontrado e é admin - permitindo login automático")
                                
                                // Verificar se o usuário já está autenticado no Firebase Auth
                                val currentUser = authService.currentUser
                                if (currentUser != null && currentUser.email?.trim()?.lowercase() == "robgomez.sir@gmail.com") {
                                    // Usuário já está autenticado, prosseguir com o fluxo normal
                                    Timber.d("✅ Usuário já autenticado - prosseguindo com login automático")
                                    // Simular sucesso de login
                                    _state.update { 
                                        it.copy(
                                            isLoading = false, 
                                            loginSuccess = true,
                                            lastEmail = email
                                        ) 
                                    }
                                } else {
                                    // Firebase Auth bloqueou, mas usuário é admin no Firestore
                                    // Como é uma exceção especial, vamos tentar permitir o login mesmo assim
                                    // Verificando se podemos usar o token de autenticação existente
                                    Timber.w("⚠️ Firebase Auth bloqueou login, mas usuário é admin - tentando contornar")
                                    
                                    // Como não podemos contornar o bloqueio do Firebase Auth diretamente,
                                    // vamos mostrar uma mensagem mais clara e sugerir verificar a Cloud Function
                                    _state.update { 
                                        it.copy(
                                            isLoading = false,
                                            error = "Seu usuário é administrador, mas a Cloud Function está bloqueando o login. Por favor, verifique as configurações da Cloud Function do Firebase para permitir login de administradores existentes."
                                        ) 
                                    }
                                }
                            } else {
                                // Usuário não encontrado ou não é admin
                                val errorMessage = if (usuarioRobgomez == null) {
                                    "Usuário não encontrado no sistema. Por favor, faça o cadastro primeiro."
                                } else {
                                    "Usuário encontrado mas não possui permissões de administrador."
                                }
                                _state.update { it.copy(isLoading = false, error = errorMessage) }
                            }
                        }
                        
                        resultadoUsuarios.onFailure {
                            // Se não conseguir buscar, mostrar mensagem padrão
                            val errorMessage = when {
                                error.message?.contains("PERMISSION_DENIED") == true ||
                                error.message?.contains("Cadastro permitido apenas por convite") == true -> {
                                    val message = error.message ?: ""
                                    val jsonMatch = Regex("""["']message["']\s*:\s*["']([^"']+)""").find(message)
                                    jsonMatch?.groupValues?.getOrNull(1) ?: "Acesso negado. Verifique as configurações do Firebase."
                                }
                                else -> error.message ?: "Erro ao fazer login. Tente novamente"
                            }
                            _state.update { it.copy(isLoading = false, error = errorMessage) }
                        }
                    }
                    return@onFailure
                }
                
                // Tratamento de erro padrão
                val errorMessage = when {
                    // Erro de permissão (Cloud Function bloqueando login)
                    error.message?.contains("PERMISSION_DENIED") == true ||
                    error.message?.contains("Cadastro permitido apenas por convite") == true -> {
                        // Tentar extrair a mensagem do JSON do erro
                        val message = error.message ?: ""
                        // Procurar por "message":"..." no JSON
                        val jsonMatch = Regex("""["']message["']\s*:\s*["']([^"']+)""").find(message)
                        val extractedMessage = jsonMatch?.groupValues?.getOrNull(1)
                        extractedMessage ?: "Acesso negado. Entre em contato com o administrador."
                    }
                    error.message?.contains("password") == true ||
                    error.message?.contains("wrong-password") == true ||
                    error.message?.contains("invalid-credential") == true -> "Senha incorreta"
                    error.message?.contains("user-not-found") == true -> "Usuário não encontrado"
                    error.message?.contains("network") == true ||
                    error.message?.contains("network_error") == true -> "Erro de conexão. Verifique sua internet"
                    error.message?.contains("too-many-requests") == true -> "Muitas tentativas. Aguarde alguns minutos"
                    else -> error.message ?: "Erro ao fazer login. Tente novamente"
                }
                _state.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }
    
    /**
     * Tenta fazer login usando biometria
     * 
     * @param activity Activity necessária para o BiometricPrompt
     * @param onBiometricSuccess Callback quando biometria é autenticada (ainda precisa fazer login)
     */
    fun loginWithBiometric(
        activity: android.app.Activity,
        onBiometricSuccess: () -> Unit
    ) {
        Timber.d("🔐 loginWithBiometric chamado")
        val lastEmail = _state.value.lastEmail
        if (lastEmail == null) {
            Timber.e("❌ Nenhum email salvo para biometria")
            _state.update { it.copy(error = "Nenhum email salvo para biometria") }
            return
        }
        
        // Verificar se biometria ainda está disponível
        if (!_state.value.biometricAvailable) {
            Timber.e("❌ Biometria não está mais disponível")
            _state.update { it.copy(error = "Biometria não está disponível") }
            return
        }
        
        viewModelScope.launch {
            Timber.d("🔐 Chamando biometricService.authenticate para email: $lastEmail")
            val result = biometricService.authenticate(
                activity = activity,
                title = "Entrar com Biometria",
                subtitle = "Use sua impressão digital ou rosto para entrar",
                negativeButtonText = "Usar senha"
            )
            
            result.onSuccess {
                Timber.d("✅ Biometria autenticada com sucesso")

                // Salvar timestamp da autenticação (renova a sessão de 24h)
                val currentTime = System.currentTimeMillis()
                biometricPreferences.saveLastAuthTimestamp(currentTime)
                // Manter conectado deve ser true se usuario usou biometria com sucesso, 
                // assumindo que ele quer continuar logado, ou ler do estado atual?
                // Vamos ler do estado atual, mas se ele nao logou ainda, talvez devamos prescrever o que estava salvo?
                // Se ele usou biometria, ele entrou. Vamos renovar o timestamp apenas. 
                // A preferência keepConnected ja deve ter sido lida no init.
                // Mas se ele alterar o checkbox NA TELA e depois usar biometria, devemos salvar o novo valor.
                biometricPreferences.saveKeepConnected(_state.value.keepConnected)
                
                // IMPORTANTE: Usar o email salvo no BiometricPreferences como fonte de verdade
                // Ele já está normalizado e é o mesmo usado para salvar a senha
                val savedEmailFromPrefs = biometricPreferences.getLastEmailSync()
                val emailToUse = savedEmailFromPrefs ?: lastEmail.trim().lowercase()
                
                Timber.d("🔐 LastEmail do estado: $lastEmail")
                Timber.d("🔐 Email salvo no BiometricPreferences: $savedEmailFromPrefs")
                Timber.d("🔐 Email final usado para buscar senha: '$emailToUse'")
                
                // Normalizar o email para garantir consistência
                val normalizedEmail = emailToUse.trim().lowercase()
                
                // Após biometria, fazer login automático com email e senha salvos
                val savedPassword = biometricCrypto.getPassword(normalizedEmail)
                if (savedPassword != null && savedPassword.isNotBlank()) {
                    Timber.d("✅ Senha encontrada, fazendo login automático para: '$normalizedEmail'")
                    Timber.d("🔐 Senha recuperada tem ${savedPassword.length} caracteres")
                    
                    // Atualizar estado primeiro
                    _state.update { 
                        it.copy(
                            email = normalizedEmail,
                            senha = savedPassword,
                            emailError = null,
                            senhaError = null,
                            error = null
                        )
                    }
                    
                    // Aguardar um pouco para garantir que o estado foi atualizado
                    kotlinx.coroutines.delay(300)
                    
                    // Fazer login automaticamente
                    Timber.d("🔐 Chamando login() após biometria")
                    login()
                } else {
                    Timber.e("❌ Senha não encontrada para email: '$normalizedEmail'")
                    Timber.e("❌ Isso não deveria acontecer se a senha foi salva corretamente")
                    Timber.e("❌ Senha recuperada: ${if (savedPassword == null) "null" else "vazia (${savedPassword.length} chars)"}")
                    
                    // Tentar buscar com variações do email
                    val variations = listOf(
                        lastEmail.trim().lowercase(),
                        savedEmailFromPrefs ?: "",
                        emailToUse
                    ).distinct().filter { it.isNotBlank() && it != normalizedEmail }
                    
                    variations.forEach { emailVariation ->
                        Timber.d("🔍 Tentando buscar com variação: '$emailVariation'")
                        val passwordVariation = biometricCrypto.getPassword(emailVariation)
                        if (passwordVariation != null && passwordVariation.isNotBlank()) {
                            Timber.d("✅ Senha encontrada com variação '$emailVariation'!")
                            _state.update { 
                                it.copy(
                                    email = emailVariation,
                                    senha = passwordVariation,
                                    emailError = null,
                                    senhaError = null,
                                    error = null
                                )
                            }
                            kotlinx.coroutines.delay(300)
                            Timber.d("🔐 Chamando login() após biometria (com email alternativo)")
                            login()
                            return@onSuccess
                        }
                    }
                    
                    // Se não houver senha salva, apenas preenche o email e pede login manual
                    _state.update { 
                        it.copy(
                            email = normalizedEmail,
                            error = "Senha não encontrada. Por favor, faça login manualmente."
                        ) 
                    }
                    onBiometricSuccess()
                }
            }
            
            result.onFailure { error ->
                Timber.e(error, "❌ Erro na autenticação biométrica: ${error.message}")
                // Não mostrar erro se foi cancelado pelo usuário
                if (error.message != null && 
                    !error.message!!.contains("cancel", ignoreCase = true) &&
                    !error.message!!.contains("Cancel", ignoreCase = true)) {
                    _state.update { it.copy(error = "Erro na autenticação biométrica: ${error.message}") }
                } else {
                    Timber.d("🔐 Autenticação biométrica cancelada pelo usuário")
                }
            }
        }
    }
    
    /**
     * Habilita ou desabilita biometria para o email atual
     */
    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val email = _state.value.email.ifBlank { _state.value.lastEmail }
            if (email != null) {
                biometricPreferences.setBiometricEnabled(email, enabled)
                _state.update { it.copy(biometricEnabled = enabled && biometricService.isBiometricAvailable()) }
            }
        }
    }
}

/**
 * Estado da tela de Login
 */
data class LoginState(
    val email: String = "",
    val senha: String = "",
    val emailError: String? = null,
    val senhaError: String? = null,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val error: String? = null,
    val lastEmail: String? = null,
    val biometricAvailable: Boolean = false,
    val biometricEnabled: Boolean = false,
    val keepConnected: Boolean = false
)

