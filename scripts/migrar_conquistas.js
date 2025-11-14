/**
 * Script de Migração: Dados de Progresso de Conquistas
 * 
 * Este script migra dados de:
 * - users/{userId}/conquistas/{conquistaId} (estrutura antiga)
 * Para:
 * - usuarios/{userId}/conquistasProgresso/{conquistaId} (estrutura nova)
 * 
 * Mudanças de campos:
 * - desbloqueada → concluida
 * - progressoAtual → progresso
 * - Adiciona: nivel (default: 1)
 * - Adiciona: pontuacaoTotal (default: 0)
 * 
 * Execução:
 * 1. Via Firebase Console → Cloud Functions
 * 2. Via Firebase Admin SDK (Node.js)
 * 3. Via script local com credenciais de admin
 * 
 * IMPORTANTE: 
 * - Faça backup antes de executar
 * - Execute em ambiente de desenvolvimento primeiro
 * - Verifique os dados após migração
 */

const admin = require('firebase-admin');

// Inicializar Admin SDK (ajustar conforme seu ambiente)
// admin.initializeApp();

const db = admin.firestore();

/**
 * Migra progresso de conquistas de um usuário
 */
async function migrarConquistasUsuario(userId) {
    console.log(`🔄 Migrando conquistas do usuário: ${userId}`);
    
    try {
        // 1. Ler dados da estrutura antiga
        const conquistasAntigas = await db
            .collection('users')
            .doc(userId)
            .collection('conquistas')
            .get();
        
        if (conquistasAntigas.empty) {
            console.log(`⚠️ Nenhuma conquista encontrada para usuário: ${userId}`);
            return { migradas: 0, erros: 0 };
        }
        
        console.log(`📥 Encontradas ${conquistasAntigas.size} conquistas para migrar`);
        
        // 2. Migrar para nova estrutura
        const batch = db.batch();
        let migradas = 0;
        let erros = 0;
        
        conquistasAntigas.forEach((doc) => {
            try {
                const data = doc.data();
                
                // Converter campos antigos para novos
                const novoData = {
                    conquistaId: data.conquistaId || doc.id,
                    concluida: data.desbloqueada || false,
                    desbloqueadaEm: data.desbloqueadaEm || null,
                    progresso: data.progressoAtual || 0,
                    progressoTotal: data.progressoTotal || 0,
                    nivel: data.nivel || 1,
                    pontuacaoTotal: data.pontuacaoTotal || 0
                };
                
                // Validar dados
                if (!novoData.conquistaId) {
                    console.error(`❌ Conquista sem ID: ${doc.id}`);
                    erros++;
                    return;
                }
                
                // Escrever na nova estrutura
                const novoDocRef = db
                    .collection('usuarios')
                    .doc(userId)
                    .collection('conquistasProgresso')
                    .doc(novoData.conquistaId);
                
                batch.set(novoDocRef, novoData);
                migradas++;
                
            } catch (error) {
                console.error(`❌ Erro ao migrar conquista ${doc.id}:`, error);
                erros++;
            }
        });
        
        // 3. Commit do batch
        if (migradas > 0) {
            await batch.commit();
            console.log(`✅ ${migradas} conquistas migradas para usuário: ${userId}`);
        }
        
        if (erros > 0) {
            console.log(`⚠️ ${erros} erros durante migração para usuário: ${userId}`);
        }
        
        return { migradas, erros };
        
    } catch (error) {
        console.error(`❌ Erro ao migrar conquistas do usuário ${userId}:`, error);
        throw error;
    }
}

/**
 * Migra conquistas de todos os usuários
 */
async function migrarTodasConquistas() {
    console.log('🚀 Iniciando migração de todas as conquistas...');
    
    try {
        // 1. Listar todos os usuários que têm conquistas
        const usersSnapshot = await db.collection('users').get();
        
        console.log(`📋 Encontrados ${usersSnapshot.size} usuários para verificar`);
        
        let totalMigradas = 0;
        let totalErros = 0;
        let usuariosProcessados = 0;
        
        // 2. Migrar conquistas de cada usuário
        for (const userDoc of usersSnapshot.docs) {
            const userId = userDoc.id;
            
            try {
                // Verificar se usuário tem conquistas
                const conquistasSnapshot = await db
                    .collection('users')
                    .doc(userId)
                    .collection('conquistas')
                    .limit(1)
                    .get();
                
                if (!conquistasSnapshot.empty) {
                    const resultado = await migrarConquistasUsuario(userId);
                    totalMigradas += resultado.migradas;
                    totalErros += resultado.erros;
                }
                
                usuariosProcessados++;
                
                // Log de progresso a cada 10 usuários
                if (usuariosProcessados % 10 === 0) {
                    console.log(`📊 Progresso: ${usuariosProcessados}/${usersSnapshot.size} usuários processados`);
                }
                
            } catch (error) {
                console.error(`❌ Erro ao processar usuário ${userId}:`, error);
                totalErros++;
            }
        }
        
        console.log('\n✅ Migração concluída!');
        console.log(`📊 Estatísticas:`);
        console.log(`   - Usuários processados: ${usuariosProcessados}`);
        console.log(`   - Conquistas migradas: ${totalMigradas}`);
        console.log(`   - Erros: ${totalErros}`);
        
        return {
            usuariosProcessados,
            totalMigradas,
            totalErros
        };
        
    } catch (error) {
        console.error('❌ Erro na migração:', error);
        throw error;
    }
}

/**
 * Valida migração comparando dados antigos e novos
 */
async function validarMigracao(userId) {
    console.log(`🔍 Validando migração para usuário: ${userId}`);
    
    try {
        // Ler dados antigos
        const antigas = await db
            .collection('users')
            .doc(userId)
            .collection('conquistas')
            .get();
        
        // Ler dados novos
        const novas = await db
            .collection('usuarios')
            .doc(userId)
            .collection('conquistasProgresso')
            .get();
        
        console.log(`   Antigas: ${antigas.size}, Novas: ${novas.size}`);
        
        if (antigas.size !== novas.size) {
            console.warn(`⚠️ Diferença no número de conquistas!`);
        }
        
        // Comparar cada conquista
        const antigasMap = new Map();
        antigas.forEach(doc => {
            antigasMap.set(doc.id, doc.data());
        });
        
        let validas = 0;
        let invalidas = 0;
        
        novas.forEach(doc => {
            const novaData = doc.data();
            const antigaData = antigasMap.get(doc.id);
            
            if (!antigaData) {
                console.warn(`⚠️ Conquista ${doc.id} não encontrada na estrutura antiga`);
                invalidas++;
                return;
            }
            
            // Validar conversão
            const concluidaCorreta = novaData.concluida === (antigaData.desbloqueada || false);
            const progressoCorreto = novaData.progresso === (antigaData.progressoAtual || 0);
            
            if (concluidaCorreta && progressoCorreto) {
                validas++;
            } else {
                console.warn(`⚠️ Conquista ${doc.id} com dados incorretos`);
                invalidas++;
            }
        });
        
        console.log(`   ✅ Válidas: ${validas}, ⚠️ Inválidas: ${invalidas}`);
        
        return { validas, invalidas };
        
    } catch (error) {
        console.error(`❌ Erro na validação:`, error);
        throw error;
    }
}

// Executar se chamado diretamente
if (require.main === module) {
    const args = process.argv.slice(2);
    const comando = args[0];
    const userId = args[1];
    
    if (comando === 'usuario' && userId) {
        migrarConquistasUsuario(userId)
            .then(() => validarMigracao(userId))
            .then(() => {
                console.log('✅ Migração e validação concluídas!');
                process.exit(0);
            })
            .catch((error) => {
                console.error('❌ Erro:', error);
                process.exit(1);
            });
    } else if (comando === 'todos') {
        migrarTodasConquistas()
            .then(() => {
                console.log('✅ Migração concluída!');
                process.exit(0);
            })
            .catch((error) => {
                console.error('❌ Erro:', error);
                process.exit(1);
            });
    } else {
        console.log('Uso:');
        console.log('  node migrar_conquistas.js usuario <userId>  - Migra conquistas de um usuário');
        console.log('  node migrar_conquistas.js todos             - Migra conquistas de todos os usuários');
        process.exit(1);
    }
}

module.exports = { migrarConquistasUsuario, migrarTodasConquistas, validarMigracao };

