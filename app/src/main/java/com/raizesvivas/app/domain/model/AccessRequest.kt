package com.raizesvivas.app.domain.model

import java.util.Date

data class AccessRequest(
    val id: String,
    val email: String,
    val nome: String?,
    val telefone: String?,
    val status: String,
    val criadoEm: Date
)


