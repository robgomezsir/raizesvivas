package com.raizesvivas.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.raizesvivas.app.data.local.Converters
import com.raizesvivas.app.domain.model.Usuario
import java.util.Date

/**
 * Entity Room para armazenar dados do usuário localmente
 */
@Entity(tableName = "usuarios")
@TypeConverters(Converters::class)
data class UsuarioEntity(
    @PrimaryKey
    val id: String,
    val nome: String,
    val email: String,
    val fotoUrl: String?,
    val pessoaVinculada: String?,
    val ehAdministrador: Boolean,
    val ehAdministradorSenior: Boolean,
    val familiaZeroPai: String?,
    val familiaZeroMae: String?,
    val primeiroAcesso: Boolean,
    val criadoEm: Date
)

fun UsuarioEntity.toDomain(): Usuario {
    return Usuario(
        id = id,
        nome = nome,
        email = email,
        fotoUrl = fotoUrl,
        pessoaVinculada = pessoaVinculada,
        ehAdministrador = ehAdministrador,
        ehAdministradorSenior = ehAdministradorSenior,
        familiaZeroPai = familiaZeroPai,
        familiaZeroMae = familiaZeroMae,
        primeiroAcesso = primeiroAcesso,
        criadoEm = criadoEm
    )
}

fun Usuario.toEntity(): UsuarioEntity {
    return UsuarioEntity(
        id = id,
        nome = nome,
        email = email,
        fotoUrl = fotoUrl,
        pessoaVinculada = pessoaVinculada,
        ehAdministrador = ehAdministrador,
        ehAdministradorSenior = ehAdministradorSenior,
        familiaZeroPai = familiaZeroPai,
        familiaZeroMae = familiaZeroMae,
        primeiroAcesso = primeiroAcesso,
        criadoEm = criadoEm
    )
}

