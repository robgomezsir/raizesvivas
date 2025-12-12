package com.raizesvivas.app.domain.usecase

import com.raizesvivas.app.data.repository.NoticiaFamiliaRepository
import com.raizesvivas.app.domain.model.NoticiaFamilia
import com.raizesvivas.app.domain.model.TipoNoticiaFamilia
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case para gerar notícias automáticas quando ações acontecem no app
 */
@Singleton
class GerarNoticiaUseCase @Inject constructor(
    private val noticiaFamiliaRepository: NoticiaFamiliaRepository
) {
    
    /**
     * Gera notícia quando um comentário é adicionado
     */
    suspend fun novoComentario(
        autorId: String,
        autorNome: String,
        fotoId: String,
        comentarioTexto: String,
        pessoaRelacionadaNome: String? = null
    ): Result<Unit> {
        return try {
            val noticia = NoticiaFamilia(
                tipo = TipoNoticiaFamilia.NOVO_COMENTARIO,
                titulo = "$autorNome comentou",
                descricao = if (pessoaRelacionadaNome != null) {
                    "em foto de $pessoaRelacionadaNome: \"${comentarioTexto.take(50)}${if (comentarioTexto.length > 50) "..." else ""}\""
                } else {
                    "\"${comentarioTexto.take(50)}${if (comentarioTexto.length > 50) "..." else ""}\""
                },
                autorId = autorId,
                autorNome = autorNome,
                pessoaRelacionadaNome = pessoaRelacionadaNome,
                recursoId = fotoId,
                criadoEm = Date()
            )
            
            noticiaFamiliaRepository.criarNoticia(noticia)
            Timber.d("✅ Notícia de comentário criada")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar notícia de comentário")
            Result.failure(e)
        }
    }
    
    /**
     * Gera notícia quando uma foto é adicionada
     */
    suspend fun novaFoto(
        autorId: String,
        autorNome: String,
        fotoId: String,
        familiaOuPessoaNome: String? = null
    ): Result<Unit> {
        return try {
            val noticia = NoticiaFamilia(
                tipo = TipoNoticiaFamilia.NOVA_FOTO,
                titulo = "$autorNome adicionou uma foto",
                descricao = familiaOuPessoaNome?.let { "ao álbum da $it" },
                autorId = autorId,
                autorNome = autorNome,
                recursoId = fotoId,
                criadoEm = Date()
            )
            
            noticiaFamiliaRepository.criarNoticia(noticia)
            Timber.d("✅ Notícia de nova foto criada")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar notícia de nova foto")
            Result.failure(e)
        }
    }
    
    /**
     * Gera notícia quando uma pessoa é cadastrada
     */
    suspend fun novaPessoa(
        autorId: String,
        autorNome: String,
        pessoaId: String,
        pessoaNome: String
    ): Result<Unit> {
        return try {
            val noticia = NoticiaFamilia(
                tipo = TipoNoticiaFamilia.NOVA_PESSOA,
                titulo = "Nova pessoa:",
                descricao = pessoaNome,
                autorId = autorId,
                autorNome = autorNome,
                pessoaRelacionadaId = pessoaId,
                pessoaRelacionadaNome = pessoaNome,
                criadoEm = Date()
            )
            
            noticiaFamiliaRepository.criarNoticia(noticia)
            Timber.d("✅ Notícia de nova pessoa criada")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar notícia de nova pessoa")
            Result.failure(e)
        }
    }
    
    /**
     * Gera notícia quando um apoio familiar é dado
     */
    suspend fun apoioFamiliar(
        autorId: String,
        autorNome: String,
        recursoId: String,
        tipoRecurso: String = "publicação"
    ): Result<Unit> {
        return try {
            val noticia = NoticiaFamilia(
                tipo = TipoNoticiaFamilia.APOIO_FAMILIAR,
                titulo = "$autorNome deu apoio",
                descricao = "em uma $tipoRecurso",
                autorId = autorId,
                autorNome = autorNome,
                recursoId = recursoId,
                criadoEm = Date()
            )
            
            noticiaFamiliaRepository.criarNoticia(noticia)
            Timber.d("✅ Notícia de apoio familiar criada")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar notícia de apoio familiar")
            Result.failure(e)
        }
    }
    
    /**
     * Gera notícia quando um recado é publicado
     */
    suspend fun novoRecado(
        autorId: String,
        autorNome: String,
        recadoId: String,
        recadoTexto: String
    ): Result<Unit> {
        return try {
            val noticia = NoticiaFamilia(
                tipo = TipoNoticiaFamilia.NOVO_RECADO,
                titulo = "$autorNome publicou um recado",
                descricao = "\"${recadoTexto.take(50)}${if (recadoTexto.length > 50) "..." else ""}\"",
                autorId = autorId,
                autorNome = autorNome,
                recursoId = recadoId,
                criadoEm = Date()
            )
            
            noticiaFamiliaRepository.criarNoticia(noticia)
            Timber.d("✅ Notícia de novo recado criada")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar notícia de novo recado")
            Result.failure(e)
        }
    }
    
    /**
     * Gera notícia quando uma subfamília é criada
     */
    suspend fun novaSubfamilia(
        autorId: String,
        autorNome: String,
        subfamiliaNome: String
    ): Result<Unit> {
        return try {
            val noticia = NoticiaFamilia(
                tipo = TipoNoticiaFamilia.NOVA_SUBFAMILIA,
                titulo = "$autorNome criou a subfamília",
                descricao = subfamiliaNome,
                autorId = autorId,
                autorNome = autorNome,
                criadoEm = Date()
            )
            
            noticiaFamiliaRepository.criarNoticia(noticia)
            Timber.d("✅ Notícia de nova subfamília criada")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar notícia de nova subfamília")
            Result.failure(e)
        }
    }
    
    /**
     * Gera notícia quando uma edição é aprovada
     */
    suspend fun edicaoAprovada(
        autorId: String,
        autorNome: String,
        pessoaEditadaNome: String
    ): Result<Unit> {
        return try {
            val noticia = NoticiaFamilia(
                tipo = TipoNoticiaFamilia.EDICAO_APROVADA,
                titulo = "Edição aprovada",
                descricao = "de $pessoaEditadaNome",
                autorId = autorId,
                autorNome = autorNome,
                criadoEm = Date()
            )
            
            noticiaFamiliaRepository.criarNoticia(noticia)
            Timber.d("✅ Notícia de edição aprovada criada")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar notícia de edição aprovada")
            Result.failure(e)
        }
    }
    
    /**
     * Gera notícia quando uma conquista é desbloqueada
     */
    suspend fun conquistaDesbloqueada(
        autorId: String,
        autorNome: String,
        conquistaNome: String
    ): Result<Unit> {
        return try {
            val noticia = NoticiaFamilia(
                tipo = TipoNoticiaFamilia.CONQUISTA_DESBLOQUEADA,
                titulo = "$autorNome desbloqueou",
                descricao = "\"$conquistaNome\"",
                autorId = autorId,
                autorNome = autorNome,
                criadoEm = Date()
            )
            
            noticiaFamiliaRepository.criarNoticia(noticia)
            Timber.d("✅ Notícia de conquista desbloqueada criada")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar notícia de conquista")
            Result.failure(e)
        }
    }
    
    /**
     * Gera notícia quando um membro é vinculado
     */
    suspend fun membroVinculado(
        autorId: String,
        autorNome: String,
        pessoaVinculadaNome: String
    ): Result<Unit> {
        return try {
            val noticia = NoticiaFamilia(
                tipo = TipoNoticiaFamilia.MEMBRO_VINCULADO,
                titulo = "$autorNome se vinculou",
                descricao = "a $pessoaVinculadaNome",
                autorId = autorId,
                autorNome = autorNome,
                pessoaRelacionadaNome = pessoaVinculadaNome,
                criadoEm = Date()
            )
            
            noticiaFamiliaRepository.criarNoticia(noticia)
            Timber.d("✅ Notícia de membro vinculado criada")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar notícia de vínculo")
            Result.failure(e)
        }
    }
}
