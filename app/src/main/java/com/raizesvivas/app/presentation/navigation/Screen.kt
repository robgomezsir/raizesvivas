package com.raizesvivas.app.presentation.navigation

/**
 * Sealed class definindo todas as rotas de navegação do app
 */
sealed class Screen(val route: String) {
    // Autenticação
    object Login : Screen("login")
    object Cadastro : Screen("cadastro")
    object RecuperarSenha : Screen("recuperar_senha")
    object RedefinirSenha : Screen("redefinir_senha/{oobCode}") {
        fun createRoute(oobCode: String) = "redefinir_senha/$oobCode"
    }
    
    // Onboarding
    object FamiliaZero : Screen("familia_zero")
    
    // Principal
    object Home : Screen("home")
    object Familia : Screen("familia")
    object ArvoreHierarquica : Screen("arvore_hierarquica")
    object Conquistas : Screen("conquistas")
    object Mural : Screen("mural")
    object AlbumFamilia : Screen("album_familia?fotoId={fotoId}") {
        fun createRoute(fotoId: String? = null) = if (fotoId != null) "album_familia?fotoId=$fotoId" else "album_familia"
    }
    object CadastroPessoa : Screen("cadastro_pessoa")
    object EditarPessoa : Screen("cadastro_pessoa/{pessoaId}") {
        fun createRoute(pessoaId: String) = "cadastro_pessoa/$pessoaId"
    }
    object Perfil : Screen("perfil")
    object DetalhesPessoa : Screen("detalhes_pessoa/{pessoaId}") {
        fun createRoute(pessoaId: String) = "detalhes_pessoa/$pessoaId"
    }
    
    // Convites
    object AceitarConvites : Screen("aceitar_convites")
    object GerenciarConvites : Screen("gerenciar_convites")
    object PedirConvite : Screen("pedir_convite")
    
    // Edições Pendentes
    object GerenciarEdicoes : Screen("gerenciar_edicoes")
    
    // Duplicatas
    object ResolverDuplicatas : Screen("resolver_duplicatas")
    
    // Gerenciar Usuários (Admin)
    object GerenciarUsuarios : Screen("gerenciar_usuarios")
    
    // Configurações (Admin Sênior)
    object Configuracoes : Screen("configuracoes")
    
    // Chat
    object ChatContacts : Screen("chat_contacts")
    object ChatConversation : Screen("chat_conversation/{destinatarioId}/{destinatarioNome}") {
        fun createRoute(destinatarioId: String, destinatarioNome: String) = 
            "chat_conversation/$destinatarioId/${destinatarioNome.replace("/", "_")}"
    }
    
    // Amigo da Família
    object AdicionarAmigo : Screen("adicionar_amigo")
    
    // Eventos da Família
    object Eventos : Screen("eventos")
    object CadastroEvento : Screen("cadastro_evento")
    object EditarEvento : Screen("cadastro_evento/{eventoId}") {
        fun createRoute(eventoId: String) = "cadastro_evento/$eventoId"
    }
    object DetalhesEvento : Screen("detalhes_evento/{eventoId}") {
        fun createRoute(eventoId: String) = "detalhes_evento/$eventoId"
    }
    
    // Privacidade
    object PoliticaPrivacidade : Screen("politica_privacidade")
}

