package com.raizesvivas.core.data.source.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Cliente Supabase para o projeto Raízes Vivas
 * 
 * Este objeto configura e fornece acesso ao cliente Supabase
 * com todas as funcionalidades necessárias para autenticação
 * e acesso ao banco de dados.
 */
object SupabaseClient {
    
    // TODO: Substituir pelas credenciais reais do Supabase
    private const val SUPABASE_URL = "YOUR_SUPABASE_URL"
    private const val SUPABASE_ANON_KEY = "YOUR_SUPABASE_ANON_KEY"
    
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }
}
