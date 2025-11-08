package com.raizesvivas.app.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.raizesvivas.app.domain.model.*
import java.util.Date

/**
 * Converters do Room para tipos complexos
 * 
 * Room não suporta nativamente alguns tipos como Date, List<String> e Enums.
 * Estes converters transformam esses tipos em tipos primitivos
 * que o Room consegue armazenar.
 */
class Converters {
    
    private val gson = Gson()
    
    /**
     * Converte Date para Long (timestamp)
     */
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    
    /**
     * Converte Long (timestamp) para Date
     */
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
    
    /**
     * Converte List<String> para String JSON
     */
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return gson.toJson(list)
    }
    
    /**
     * Converte String JSON para List<String>
     */
    @TypeConverter
    fun toStringList(json: String?): List<String>? {
        if (json == null) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    /**
     * Converte Map<String, Any> para String JSON
     */
    @TypeConverter
    fun fromMap(map: Map<String, Any>?): String? {
        return gson.toJson(map)
    }
    
    /**
     * Converte String JSON para Map<String, Any>
     */
    @TypeConverter
    fun toMap(json: String?): Map<String, Any>? {
        if (json == null) return emptyMap()
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }
    
    // ============================================
    // CONVERSORES DE ENUMS
    // ============================================
    
    @TypeConverter
    fun fromTipoFamilia(tipo: TipoFamilia?): String? {
        return tipo?.name
    }
    
    @TypeConverter
    fun toTipoFamilia(name: String?): TipoFamilia? {
        return name?.let { TipoFamilia.valueOf(it) }
    }
    
    @TypeConverter
    fun fromStatusSugestao(status: StatusSugestao?): String? {
        return status?.name
    }
    
    @TypeConverter
    fun toStatusSugestao(name: String?): StatusSugestao? {
        return name?.let { StatusSugestao.valueOf(it) }
    }
    
    @TypeConverter
    fun fromPapelFamilia(papel: PapelFamilia?): String? {
        return papel?.name
    }
    
    @TypeConverter
    fun toPapelFamilia(name: String?): PapelFamilia? {
        return name?.let { PapelFamilia.valueOf(it) }
    }
    
    @TypeConverter
    fun fromElementoArvore(elemento: ElementoArvore?): String? {
        return elemento?.name
    }
    
    @TypeConverter
    fun toElementoArvore(name: String?): ElementoArvore? {
        return name?.let { ElementoArvore.valueOf(it) }
    }
    
    @TypeConverter
    fun fromTipoFiliacao(tipo: TipoFiliacao?): String? {
        return tipo?.name
    }
    
    @TypeConverter
    fun toTipoFiliacao(name: String?): TipoFiliacao? {
        return name?.let { TipoFiliacao.valueOf(it) }
    }
    
    @TypeConverter
    fun fromTipoNascimento(tipo: TipoNascimento?): String? {
        return tipo?.name
    }
    
    @TypeConverter
    fun toTipoNascimento(name: String?): TipoNascimento? {
        return name?.let { TipoNascimento.valueOf(it) }
    }
    
    @TypeConverter
    fun fromTipoNotificacao(tipo: TipoNotificacao?): String? {
        return tipo?.name
    }
    
    @TypeConverter
    fun toTipoNotificacao(name: String?): TipoNotificacao? {
        return name?.let { TipoNotificacao.valueOf(it) }
    }
    
    @TypeConverter
    fun fromEstadoCivil(estadoCivil: EstadoCivil?): String? {
        return estadoCivil?.name
    }
    
    @TypeConverter
    fun toEstadoCivil(name: String?): EstadoCivil? {
        return name?.let { EstadoCivil.valueOf(it) }
    }
    
    @TypeConverter
    fun fromGenero(genero: Genero?): String? {
        return genero?.name
    }
    
    @TypeConverter
    fun toGenero(name: String?): Genero? {
        return name?.let { Genero.valueOf(it) }
    }
}

