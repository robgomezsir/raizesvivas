package com.raizesvivas.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.raizesvivas.app.data.local.Converters
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.domain.model.EstadoCivil
import com.raizesvivas.app.domain.model.Genero
import java.util.Date

/**
 * Entity Room para armazenar pessoas localmente (cache)
 * 
 * Esta entity espelha os dados do Firestore para permitir
 * funcionamento offline do app.
 */
@Entity(tableName = "pessoas")
@TypeConverters(Converters::class)
data class PessoaEntity(
    @PrimaryKey
    val id: String,
    val nome: String,
    val dataNascimento: Date?,
    val dataFalecimento: Date?,
    val localNascimento: String?,
    val localResidencia: String?,
    val profissao: String?,
    val biografia: String?,
    val estadoCivil: EstadoCivil?,
    val genero: Genero?,
    
    // Relacionamentos
    val pai: String?,
    val mae: String?,
    val conjugeAtual: String?,
    val exConjuges: List<String>,
    val filhos: List<String>,
    
    // Metadados
    val fotoUrl: String?,
    val criadoPor: String,
    val criadoEm: Date,
    val modificadoPor: String,
    val modificadoEm: Date,
    val aprovado: Boolean,
    val versao: Int,
    
    // Flags especiais
    val ehFamiliaZero: Boolean,
    val distanciaFamiliaZero: Int,
    
    // Controle de sincronização
    val sincronizadoEm: Date = Date(),
    val precisaSincronizar: Boolean = false
)

/**
 * Converte de Entity para Model de Domínio
 */
fun PessoaEntity.toDomain(): Pessoa {
    return Pessoa(
        id = id,
        nome = nome,
        dataNascimento = dataNascimento,
        dataFalecimento = dataFalecimento,
        localNascimento = localNascimento,
        localResidencia = localResidencia,
        profissao = profissao,
        biografia = biografia,
        estadoCivil = estadoCivil,
        genero = genero,
        pai = pai,
        mae = mae,
        conjugeAtual = conjugeAtual,
        exConjuges = exConjuges,
        filhos = filhos,
        fotoUrl = fotoUrl,
        criadoPor = criadoPor,
        criadoEm = criadoEm,
        modificadoPor = modificadoPor,
        modificadoEm = modificadoEm,
        aprovado = aprovado,
        versao = versao,
        ehFamiliaZero = ehFamiliaZero,
        distanciaFamiliaZero = distanciaFamiliaZero
    )
}

/**
 * Converte de Model de Domínio para Entity
 */
fun Pessoa.toEntity(
    precisaSincronizar: Boolean = false
): PessoaEntity {
    return PessoaEntity(
        id = id,
        nome = nome,
        dataNascimento = dataNascimento,
        dataFalecimento = dataFalecimento,
        localNascimento = localNascimento,
        localResidencia = localResidencia,
        profissao = profissao,
        biografia = biografia,
        estadoCivil = estadoCivil,
        genero = genero,
        pai = pai,
        mae = mae,
        conjugeAtual = conjugeAtual,
        exConjuges = exConjuges,
        filhos = filhos,
        fotoUrl = fotoUrl,
        criadoPor = criadoPor,
        criadoEm = criadoEm,
        modificadoPor = modificadoPor,
        modificadoEm = modificadoEm,
        aprovado = aprovado,
        versao = versao,
        ehFamiliaZero = ehFamiliaZero,
        distanciaFamiliaZero = distanciaFamiliaZero,
        sincronizadoEm = Date(),
        precisaSincronizar = precisaSincronizar
    )
}

