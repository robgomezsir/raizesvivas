package com.raizesvivas.app.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.DocumentSnapshot
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.data.remote.firebase.PagedResult
import timber.log.Timber
import javax.inject.Inject

class PessoasPagingSource(
    private val firestoreService: FirestoreService
) : PagingSource<DocumentSnapshot, Pessoa>() {

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, Pessoa>): DocumentSnapshot? {
        // Como o Firestore usa cursores (DocumentSnapshot), é difícil calcular
        // uma chave de refresh exata baseada na posição (anchorPosition).
        // Retornando null faz com que o reload comece do início.
        return null
    }

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, Pessoa> {
        return try {
            // O loadSize inicial do Paging 3 é 3x o pageSize configurado.
            // Podemos respeitar ou limitar se acharmos muito grande.
            val limit = params.loadSize
            val startAfter = params.key

            Timber.d("🔄 PagingSource: Carregando página (limit: $limit, startAfter: ${startAfter?.id})")

            val result = firestoreService.buscarPessoasPaginado(limit, startAfter)
            
            var loadResult: LoadResult<DocumentSnapshot, Pessoa>? = null
            
            result.onSuccess { pagedResult ->
                val nextKey = if (pagedResult.hasMore) pagedResult.lastDocument else null
                
                Timber.d("✅ PagingSource: Sucesso! ${pagedResult.data.size} itens. HasMore: ${pagedResult.hasMore}")
                
                loadResult = LoadResult.Page(
                    data = pagedResult.data,
                    prevKey = null, // Paginação do Firestore é apenas forward-only geralmente
                    nextKey = nextKey
                )
            }
            
            result.onFailure { error ->
                Timber.e(error, "❌ PagingSource: Erro ao carregar página")
                loadResult = LoadResult.Error(error)
            }
            
            loadResult ?: LoadResult.Error(Exception("Resultado desconhecido"))
            
        } catch (e: Exception) {
            Timber.e(e, "❌ PagingSource: Erro inesperado")
            LoadResult.Error(e)
        }
    }
}
