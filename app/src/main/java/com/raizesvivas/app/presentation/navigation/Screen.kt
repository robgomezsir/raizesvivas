package com.raizesvivas.app.presentation.navigation

/**
 * Sealed class definindo todas as rotas de navegação do app
 */
sealed class Screen(val route: String) {
    // Autenticação
    object Login : Screen("login")
    object Cadastro : Screen("cadastro")
    object RecuperarSenha : Screen("recuperar_senha")
    
    // Onboarding
    object FamiliaZero : Screen("familia_zero")
    
    // Principal
    object Home : Screen("home")
    object Arvore : Screen("arvore")
    object Conquistas : Screen("conquistas")
    object Mural : Screen("mural")
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
    
    // Edições Pendentes
    object GerenciarEdicoes : Screen("gerenciar_edicoes")
    
    // Duplicatas
    object ResolverDuplicatas : Screen("resolver_duplicatas")
}

