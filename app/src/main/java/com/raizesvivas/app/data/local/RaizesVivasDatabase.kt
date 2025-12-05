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
 * Versão 7: Adiciona tabela de famílias personalizadas para nomes customizados
 * Versão 8: Reestruturação da tabela pessoas (adiciona campo familias)
 * Versão 9: Adiciona campo telefone na tabela pessoas
 * Versão 10: Renomeia campos de conquistas (desbloqueada → concluida, progressoAtual → progresso) e adiciona nivel e pontuacaoTotal
 * Versão 11: Adiciona coluna apelido na tabela pessoas
 * Versão 12: Adiciona coluna ehAdministradorSenior na tabela usuarios
 * Versão 13: Adiciona tabela amigos para gerenciar amigos da família
 * Versão 14: Adiciona índices otimizados na tabela pessoas para melhor performance
 * Versão 15: Adiciona tabela familias_excluidas para rastrear famílias deletadas por ADMIN SR
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
        PerfilGamificacaoEntity::class,
        FamiliaPersonalizadaEntity::class,
        AmigoEntity::class,
        FamiliaExcluidaEntity::class
    ],
    version = 15,
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
    
    /**
     * DAO para nomes personalizados de famílias
     */
    abstract fun familiaPersonalizadaDao(): FamiliaPersonalizadaDao
    
    /**
     * DAO para operações com amigos da família
     */
    abstract fun amigoDao(): AmigoDao
    
    /**
     * DAO para operações com famílias excluídas
     */
    abstract fun familiaExcluidaDao(): FamiliaExcluidaDao
    
    companion object {
        /**
         * Nome do arquivo do banco de dados
         */
        const val DATABASE_NAME = "raizes_vivas.db"
    }
}

