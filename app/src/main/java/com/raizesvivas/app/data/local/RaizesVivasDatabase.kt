package com.raizesvivas.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.raizesvivas.app.data.local.dao.*
import com.raizesvivas.app.data.local.entities.*

/**
 * Banco de dados local do app Raízes Vivas
 * 
 * Este banco usa Room para armazenar dados localmente,
 * permitindo funcionamento offline e melhor performance.
 * 
 * Versão 1: Schema inicial com pessoas e usuários
 * Versão 2: Adiciona subfamílias, sugestões e membros de famílias
 * Versão 3: Adiciona notificações
 * Versão 4: Adiciona sistema de gamificação (conquistas e perfil)
 * Versão 5: Adiciona campos estadoCivil e genero na tabela pessoas
 * Versão 6: Conquistas agora são exclusivas por usuário (adiciona usuarioId à tabela progresso_conquistas)
 */
@Database(
    entities = [
        PessoaEntity::class,
        UsuarioEntity::class,
        SubfamiliaEntity::class,
        SugestaoSubfamiliaEntity::class,
        MembroFamiliaEntity::class,
        NotificacaoEntity::class,
        ConquistaEntity::class,
        PerfilGamificacaoEntity::class
    ],
    version = 6,
    exportSchema = true,
    autoMigrations = []
)
@TypeConverters(Converters::class)
abstract class RaizesVivasDatabase : RoomDatabase() {
    
    /**
     * DAO para operações com pessoas
     */
    abstract fun pessoaDao(): PessoaDao
    
    /**
     * DAO para operações com usuários
     */
    abstract fun usuarioDao(): UsuarioDao
    
    /**
     * DAO para operações com subfamílias
     */
    abstract fun subfamiliaDao(): SubfamiliaDao
    
    /**
     * DAO para operações com sugestões de subfamílias
     */
    abstract fun sugestaoSubfamiliaDao(): SugestaoSubfamiliaDao
    
    /**
     * DAO para operações com membros de famílias
     */
    abstract fun membroFamiliaDao(): MembroFamiliaDao
    
    /**
     * DAO para operações com notificações
     */
    abstract fun notificacaoDao(): NotificacaoDao
    
    /**
     * DAO para operações com progresso de conquistas
     */
    abstract fun conquistaDao(): ConquistaDao
    
    /**
     * DAO para operações com perfil de gamificação
     */
    abstract fun perfilGamificacaoDao(): PerfilGamificacaoDao
    
    companion object {
        /**
         * Nome do arquivo do banco de dados
         */
        const val DATABASE_NAME = "raizes_vivas.db"
    }
}

