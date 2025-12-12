package com.raizesvivas.app.data.remote.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.raizesvivas.app.domain.model.NoticiaFamilia
import com.raizesvivas.app.domain.model.TipoNoticiaFamilia
import timber.log.Timber
import java.util.Date as JavaDate

/**
 * Extensão para converter DocumentSnapshot em NoticiaFamilia
 */
fun DocumentSnapshot.toNoticia(): NoticiaFamilia? {
    return try {
        val data = this.data ?: return null
        
        NoticiaFamilia(
            id = this.id,
            tipo = TipoNoticiaFamilia.valueOf(data["tipo"] as? String ?: "NOVA_PESSOA"),
            titulo = data["titulo"] as? String ?: "",
            descricao = data["descricao"] as? String,
            autorId = data["autorId"] as? String ?: "",
            autorNome = data["autorNome"] as? String ?: "",
            pessoaRelacionadaId = data["pessoaRelacionadaId"] as? String,
            pessoaRelacionadaNome = data["pessoaRelacionadaNome"] as? String,
            recursoId = data["recursoId"] as? String,
            criadoEm = (data["criadoEm"] as? com.google.firebase.Timestamp)?.toDate() ?: JavaDate(),
            lida = data["lida"] as? Boolean ?: false
        )
    } catch (e: Exception) {
        Timber.e(e, "Erro ao converter documento para NoticiaFamilia: ${this.id}")
        null
    }
}
