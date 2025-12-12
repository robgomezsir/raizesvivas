package com.raizesvivas.app.data.remote.firebase

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Resultado paginado de uma query do Firestore
 * 
 * @param T Tipo dos dados retornados
 * @param data Lista de dados da página atual
 * @param hasMore Indica se há mais páginas disponíveis
 * @param lastDocument Último documento da página (usado para paginação)
 */
data class PagedResult<T>(
    val data: List<T>,
    val hasMore: Boolean,
    val lastDocument: DocumentSnapshot?
) {
    companion object {
        /**
         * Cria um resultado vazio (sem mais páginas)
         */
        fun <T> empty(): PagedResult<T> {
            return PagedResult(
                data = emptyList(),
                hasMore = false,
                lastDocument = null
            )
        }
    }
}

