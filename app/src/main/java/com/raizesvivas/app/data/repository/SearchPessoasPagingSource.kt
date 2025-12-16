package com.raizesvivas.app.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.DocumentSnapshot
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.domain.model.PessoaFilter
import timber.log.Timber

class SearchPessoasPagingSource(
    private val firestoreService: FirestoreService,
    private val filtro: PessoaFilter
) : PagingSource<DocumentSnapshot, Pessoa>() {

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, Pessoa>): DocumentSnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, Pessoa> {
        return try {
            val limit = params.loadSize.toLong()
            val startAfter = params.key

            Timber.d("🔍 SearchPagingSource: Buscando com filtro (limit: $limit)")

            val result = firestoreService.buscarPessoasComFiltros(filtro, limit, startAfter)
            
            var loadResult: LoadResult<DocumentSnapshot, Pessoa>? = null
            
            result.onSuccess { pagedResult ->
                val nextKey = if (pagedResult.hasMore) pagedResult.lastDocument else null
                
                loadResult = LoadResult.Page(
                    data = pagedResult.data,
                    prevKey = null,
                    nextKey = nextKey
                )
            }
            
            result.onFailure { error ->
                Timber.e(error, "❌ SearchPagingSource: Erro")
                loadResult = LoadResult.Error(error)
            }
            
            loadResult ?: LoadResult.Error(Exception("Erro desconhecido na busca"))
            
        } catch (e: Exception) {
            Timber.e(e, "❌ SearchPagingSource: Erro inesperado")
            LoadResult.Error(e)
        }
    }
}
