package com.raizesvivas.app.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Migration logic for creating new tables
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS subfamilias (
                    id TEXT NOT NULL PRIMARY KEY,
                    nome TEXT NOT NULL,
                    tipo TEXT NOT NULL,
                    familiaPaiId TEXT NOT NULL,
                    membroOrigem1Id TEXT NOT NULL,
                    membroOrigem2Id TEXT NOT NULL,
                    nivelHierarquico INTEGER NOT NULL,
                    criadoEm INTEGER NOT NULL,
                    criadoPor TEXT NOT NULL,
                    descricao TEXT,
                    ativa INTEGER NOT NULL,
                    sincronizadoEm INTEGER NOT NULL,
                    precisaSincronizar INTEGER NOT NULL
                )
            """)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS sugestoes_subfamilias (
                    id TEXT NOT NULL PRIMARY KEY,
                    membro1Id TEXT NOT NULL,
                    membro2Id TEXT NOT NULL,
                    nomeSugerido TEXT NOT NULL,
                    membrosIncluidos TEXT NOT NULL,
                    status TEXT NOT NULL,
                    criadoEm INTEGER NOT NULL,
                    processadoEm INTEGER,
                    usuarioId TEXT NOT NULL,
                    familiaZeroId TEXT NOT NULL,
                    sincronizadoEm INTEGER NOT NULL,
                    precisaSincronizar INTEGER NOT NULL
                )
            """)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS membros_familias (
                    membroId TEXT NOT NULL,
                    familiaId TEXT NOT NULL,
                    papelNaFamilia TEXT NOT NULL,
                    elementoNestaFamilia TEXT NOT NULL,
                    geracaoNaFamilia INTEGER NOT NULL,
                    PRIMARY KEY(membroId, familiaId)
                )
            """)
        }
    }
    
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS notificacoes (
                    id TEXT NOT NULL PRIMARY KEY,
                    tipo TEXT NOT NULL,
                    titulo TEXT NOT NULL,
                    mensagem TEXT NOT NULL,
                    lida INTEGER NOT NULL,
                    criadaEm INTEGER NOT NULL,
                    relacionadoId TEXT,
                    dadosExtras TEXT NOT NULL,
                    sincronizadoEm INTEGER NOT NULL,
                    precisaSincronizar INTEGER NOT NULL
                )
            """)
            db.execSQL("""
                CREATE INDEX IF NOT EXISTS index_notificacoes_lida ON notificacoes(lida)
            """)
            db.execSQL("""
                CREATE INDEX IF NOT EXISTS index_notificacoes_criadaEm ON notificacoes(criadaEm)
            """)
        }
    }
    
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Tabela de progresso de conquistas
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS progresso_conquistas (
                    conquistaId TEXT NOT NULL PRIMARY KEY,
                    desbloqueada INTEGER NOT NULL,
                    desbloqueadaEm INTEGER,
                    progressoAtual INTEGER NOT NULL,
                    progressoTotal INTEGER NOT NULL,
                    sincronizadoEm INTEGER NOT NULL,
                    precisaSincronizar INTEGER NOT NULL
                )
            """)
            
            // Tabela de perfil de gamificação
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS perfil_gamificacao (
                    usuarioId TEXT NOT NULL PRIMARY KEY,
                    nivel INTEGER NOT NULL,
                    xpTotal INTEGER NOT NULL,
                    conquistasDesbloqueadas INTEGER NOT NULL,
                    totalConquistas INTEGER NOT NULL,
                    sincronizadoEm INTEGER NOT NULL,
                    precisaSincronizar INTEGER NOT NULL
                )
            """)
            
            // Índices
            db.execSQL("""
                CREATE INDEX IF NOT EXISTS index_conquistas_desbloqueada ON progresso_conquistas(desbloqueada)
            """)
            db.execSQL("""
                CREATE INDEX IF NOT EXISTS index_perfil_gamificacao_usuarioId ON perfil_gamificacao(usuarioId)
            """)
        }
    }
    
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Adicionar campos estadoCivil e genero na tabela pessoas
            db.execSQL("""
                ALTER TABLE pessoas ADD COLUMN estadoCivil TEXT
            """)
            db.execSQL("""
                ALTER TABLE pessoas ADD COLUMN genero TEXT
            """)
        }
    }
    
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Migração: Adicionar usuarioId à tabela progresso_conquistas
            // Como não podemos saber a qual usuário pertencem as conquistas existentes,
            // vamos deletá-las e recriar a tabela com a nova estrutura
            
            // 1. Criar nova tabela temporária com a estrutura correta
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS progresso_conquistas_new (
                    conquistaId TEXT NOT NULL,
                    usuarioId TEXT NOT NULL,
                    desbloqueada INTEGER NOT NULL,
                    desbloqueadaEm INTEGER,
                    progressoAtual INTEGER NOT NULL,
                    progressoTotal INTEGER NOT NULL,
                    sincronizadoEm INTEGER NOT NULL,
                    precisaSincronizar INTEGER NOT NULL,
                    PRIMARY KEY(conquistaId, usuarioId)
                )
            """)
            
            // 2. Deletar tabela antiga (dados serão perdidos, mas isso é esperado)
            db.execSQL("DROP TABLE IF EXISTS progresso_conquistas")
            
            // 3. Renomear tabela nova para o nome original
            db.execSQL("ALTER TABLE progresso_conquistas_new RENAME TO progresso_conquistas")
            
            // 4. Criar índices
            db.execSQL("""
                CREATE INDEX IF NOT EXISTS index_progresso_conquistas_usuarioId 
                ON progresso_conquistas(usuarioId)
            """)
            db.execSQL("""
                CREATE INDEX IF NOT EXISTS index_progresso_conquistas_desbloqueada 
                ON progresso_conquistas(desbloqueada)
            """)
            db.execSQL("""
                CREATE INDEX IF NOT EXISTS index_progresso_conquistas_usuarioId_desbloqueada 
                ON progresso_conquistas(usuarioId, desbloqueada)
            """)
        }
    }
    
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS familias_personalizadas (
                    familiaId TEXT NOT NULL PRIMARY KEY,
                    nome TEXT NOT NULL,
                    conjuguePrincipalId TEXT,
                    conjugueSecundarioId TEXT,
                    ehFamiliaZero INTEGER NOT NULL,
                    atualizadoPor TEXT,
                    atualizadoEm INTEGER NOT NULL,
                    sincronizadoEm INTEGER,
                    precisaSincronizar INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE pessoas RENAME TO pessoas_old")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS pessoas (
                    id TEXT NOT NULL PRIMARY KEY,
                    nome TEXT NOT NULL,
                    dataNascimento INTEGER,
                    dataFalecimento INTEGER,
                    localNascimento TEXT,
                    localResidencia TEXT,
                    profissao TEXT,
                    biografia TEXT,
                    estadoCivil TEXT,
                    genero TEXT,
                    pai TEXT,
                    mae TEXT,
                    conjugeAtual TEXT,
                    exConjuges TEXT NOT NULL,
                    filhos TEXT NOT NULL,
                    familias TEXT NOT NULL,
                    fotoUrl TEXT,
                    criadoPor TEXT NOT NULL,
                    criadoEm INTEGER NOT NULL,
                    modificadoPor TEXT NOT NULL,
                    modificadoEm INTEGER NOT NULL,
                    aprovado INTEGER NOT NULL,
                    versao INTEGER NOT NULL,
                    ehFamiliaZero INTEGER NOT NULL,
                    distanciaFamiliaZero INTEGER NOT NULL,
                    sincronizadoEm INTEGER NOT NULL,
                    precisaSincronizar INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                INSERT INTO pessoas (
                    id,
                    nome,
                    dataNascimento,
                    dataFalecimento,
                    localNascimento,
                    localResidencia,
                    profissao,
                    biografia,
                    estadoCivil,
                    genero,
                    pai,
                    mae,
                    conjugeAtual,
                    exConjuges,
                    filhos,
                    familias,
                    fotoUrl,
                    criadoPor,
                    criadoEm,
                    modificadoPor,
                    modificadoEm,
                    aprovado,
                    versao,
                    ehFamiliaZero,
                    distanciaFamiliaZero,
                    sincronizadoEm,
                    precisaSincronizar
                )
                SELECT
                    id,
                    nome,
                    dataNascimento,
                    dataFalecimento,
                    localNascimento,
                    localResidencia,
                    profissao,
                    biografia,
                    estadoCivil,
                    genero,
                    pai,
                    mae,
                    conjugeAtual,
                    exConjuges,
                    filhos,
                    '[]' AS familias,
                    fotoUrl,
                    criadoPor,
                    criadoEm,
                    modificadoPor,
                    modificadoEm,
                    aprovado,
                    versao,
                    ehFamiliaZero,
                    distanciaFamiliaZero,
                    sincronizadoEm,
                    precisaSincronizar
                FROM pessoas_old
                """.trimIndent()
            )

            db.execSQL("DROP TABLE pessoas_old")
        }
    }
    
    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Adicionar campo telefone na tabela pessoas
            db.execSQL("""
                ALTER TABLE pessoas ADD COLUMN telefone TEXT
            """)
        }
    }

    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Migração: Renomear colunas desbloqueada → concluida e progressoAtual → progresso
            // Adicionar novas colunas: nivel e pontuacaoTotal
            
            // SQLite não suporta RENAME COLUMN diretamente, então precisamos:
            // 1. Criar nova tabela com estrutura correta
            // 2. Copiar dados convertendo campos
            // 3. Deletar tabela antiga
            // 4. Renomear nova tabela
            
            // 1. Criar nova tabela com estrutura correta
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS progresso_conquistas_new (
                    conquistaId TEXT NOT NULL,
                    usuarioId TEXT NOT NULL,
                    concluida INTEGER NOT NULL,
                    desbloqueadaEm INTEGER,
                    progresso INTEGER NOT NULL,
                    progressoTotal INTEGER NOT NULL,
                    nivel INTEGER NOT NULL DEFAULT 1,
                    pontuacaoTotal INTEGER NOT NULL DEFAULT 0,
                    sincronizadoEm INTEGER NOT NULL,
                    precisaSincronizar INTEGER NOT NULL,
                    PRIMARY KEY(conquistaId, usuarioId)
                )
            """)
            
            // 2. Copiar dados convertendo campos antigos para novos
            db.execSQL("""
                INSERT INTO progresso_conquistas_new 
                SELECT 
                    conquistaId,
                    usuarioId,
                    desbloqueada AS concluida,
                    desbloqueadaEm,
                    progressoAtual AS progresso,
                    progressoTotal,
                    1 AS nivel,
                    0 AS pontuacaoTotal,
                    sincronizadoEm,
                    precisaSincronizar
                FROM progresso_conquistas
            """)
            
            // 3. Deletar tabela antiga
            db.execSQL("DROP TABLE IF EXISTS progresso_conquistas")
            
            // 4. Renomear nova tabela
            db.execSQL("ALTER TABLE progresso_conquistas_new RENAME TO progresso_conquistas")
            
            // 5. Recriar índices com novos nomes de colunas
            db.execSQL("""
                CREATE INDEX IF NOT EXISTS index_progresso_conquistas_usuarioId 
                ON progresso_conquistas(usuarioId)
            """)
            db.execSQL("""
                CREATE INDEX IF NOT EXISTS index_progresso_conquistas_concluida 
                ON progresso_conquistas(concluida)
            """)
            db.execSQL("""
                CREATE INDEX IF NOT EXISTS index_progresso_conquistas_usuarioId_concluida 
                ON progresso_conquistas(usuarioId, concluida)
            """)
        }
    }

    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                ALTER TABLE pessoas ADD COLUMN apelido TEXT
                """.trimIndent()
            )
        }
    }

    val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Adicionar campo ehAdministradorSenior na tabela usuarios
            db.execSQL(
                """
                ALTER TABLE usuarios ADD COLUMN ehAdministradorSenior INTEGER NOT NULL DEFAULT 0
                """.trimIndent()
            )
        }
    }

    val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Criar tabela amigos
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS amigos (
                    id TEXT NOT NULL PRIMARY KEY,
                    nome TEXT NOT NULL,
                    telefone TEXT,
                    familiaresVinculados TEXT NOT NULL,
                    criadoPor TEXT NOT NULL,
                    criadoEm INTEGER NOT NULL,
                    modificadoEm INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Adicionar índices otimizados na tabela pessoas para melhor performance
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_pessoas_aprovado 
                ON pessoas(aprovado)
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_pessoas_dataNascimento 
                ON pessoas(dataNascimento)
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_pessoas_pai_mae 
                ON pessoas(pai, mae)
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_pessoas_genero 
                ON pessoas(genero)
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_pessoas_ehFamiliaZero 
                ON pessoas(ehFamiliaZero)
                """.trimIndent()
            )
        }
    }

    val MIGRATION_14_15 = object : Migration(14, 15) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Criar tabela familias_excluidas para rastrear famílias deletadas por ADMIN SR
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS familias_excluidas (
                    familiaId TEXT NOT NULL PRIMARY KEY,
                    excluidoPor TEXT NOT NULL,
                    excluidoEm INTEGER NOT NULL,
                    motivo TEXT,
                    sincronizadoEm INTEGER,
                    precisaSincronizar INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10,
            MIGRATION_10_11,
            MIGRATION_11_12,
            MIGRATION_12_13,
            MIGRATION_13_14,
            MIGRATION_14_15
        )
    }
}
