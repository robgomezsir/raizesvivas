/**
 * Script para consultar e exibir o ranking de conquistas
 * Usa Firebase Admin SDK com chave de serviço ou Application Default Credentials
 * 
 * INSTRUÇÕES:
 * 1. Baixe uma chave de serviço do Firebase Console:
 *    - Vá em Project Settings > Service Accounts
 *    - Clique em "Generate New Private Key"
 *    - Salve o arquivo JSON como serviceAccountKey.json na pasta scripts/
 * 
 * 2. OU configure Application Default Credentials:
 *    - Instale Google Cloud SDK: https://cloud.google.com/sdk/docs/install
 *    - Execute: gcloud auth application-default login
 * 
 * Execução:
 * node scripts/consultar_ranking.js
 */

const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

// Verificar se firebase-admin está disponível
let db;

try {
    // Tentar inicializar com chave de serviço primeiro
    const serviceAccountPath = path.join(__dirname, 'serviceAccountKey.json');
    
    if (fs.existsSync(serviceAccountPath)) {
        const serviceAccount = require(serviceAccountPath);
        admin.initializeApp({
            credential: admin.credential.cert(serviceAccount),
            projectId: 'suasraizesvivas'
        });
        console.log('✅ Usando chave de serviço para autenticação');
    } else {
        // Tentar usar Application Default Credentials
        admin.initializeApp({
            projectId: 'suasraizesvivas'
        });
        console.log('✅ Usando Application Default Credentials');
    }
    
    db = admin.firestore();
} catch (error) {
    console.error('❌ Erro ao inicializar Firebase Admin SDK:');
    console.error('   Detalhes:', error.message);
    console.error('');
    console.error('SOLUÇÕES:');
    console.error('   1. Baixe uma chave de serviço do Firebase Console e salve como serviceAccountKey.json');
    console.error('   2. OU instale Google Cloud SDK e execute: gcloud auth application-default login');
    process.exit(1);
}

/**
 * Busca ranking de usuários ordenado por XP total
 */
async function buscarRanking() {
    try {
        console.log('📊 Buscando ranking de conquistas...\n');
        
        // Buscar todos os usuários
        const usuariosSnapshot = await db.collection('users')
            .orderBy('nome')
            .limit(100)
            .get();
        
        const ranking = [];
        
        // Para cada usuário, buscar perfil de gamificação
        for (const usuarioDoc of usuariosSnapshot.docs) {
            const usuarioData = usuarioDoc.data();
            const usuarioId = usuarioDoc.id;
            
            try {
                // Buscar perfil de gamificação
                const perfilDoc = await db
                    .collection('usuarios')
                    .doc(usuarioId)
                    .collection('perfilGamificacao')
                    .doc('perfil')
                    .get();
                
                if (perfilDoc.exists) {
                    const perfilData = perfilDoc.data();
                    const xpTotal = perfilData?.xpTotal || 0;
                    const nivel = perfilData?.nivel || 1;
                    const conquistasDesbloqueadas = perfilData?.conquistasDesbloqueadas || 0;
                    
                    ranking.push({
                        usuarioId: usuarioId,
                        nome: usuarioData.nome || 'Sem nome',
                        fotoUrl: usuarioData.fotoUrl || null,
                        xpTotal: xpTotal,
                        nivel: nivel,
                        conquistasDesbloqueadas: conquistasDesbloqueadas
                    });
                }
            } catch (error) {
                // Ignorar erros de permissão ou documentos não encontrados
                if (!error.message.includes('PERMISSION_DENIED')) {
                    console.warn(`⚠️  Erro ao buscar perfil de ${usuarioData.nome || usuarioId}: ${error.message}`);
                }
            }
        }
        
        // Ordenar por XP total (decrescente)
        ranking.sort((a, b) => b.xpTotal - a.xpTotal);
        
        // Atribuir posições (com tratamento de empates)
        let posicaoAtual = 1;
        let xpAnterior = Infinity;
        
        const rankingComPosicoes = ranking.map((usuario, index) => {
            if (usuario.xpTotal < xpAnterior) {
                posicaoAtual = index + 1;
                xpAnterior = usuario.xpTotal;
            }
            return {
                ...usuario,
                posicao: posicaoAtual
            };
        });
        
        // Exibir ranking formatado
        console.log('═══════════════════════════════════════════════════════════════');
        console.log('🏆 RANKING DE CONQUISTAS COM TAREFAS REALIZADAS');
        console.log('═══════════════════════════════════════════════════════════════\n');
        console.log('Posição | Nome | Pontuação (XP Total)');
        console.log('───────────────────────────────────────────────────────────────');
        
        if (rankingComPosicoes.length === 0) {
            console.log('Nenhum usuário encontrado no ranking.');
        } else {
            rankingComPosicoes.forEach((usuario) => {
                const nomeFormatado = usuario.nome.padEnd(30);
                const pontuacaoFormatada = usuario.xpTotal.toString().padStart(8);
                console.log(`   ${usuario.posicao.toString().padStart(2)}   | ${nomeFormatado} | ${pontuacaoFormatada}`);
            });
        }
        
        console.log('\n═══════════════════════════════════════════════════════════════');
        console.log(`Total de usuários no ranking: ${rankingComPosicoes.length}`);
        console.log('═══════════════════════════════════════════════════════════════\n');
        
        return rankingComPosicoes;
        
    } catch (error) {
        console.error('❌ Erro ao buscar ranking:', error.message);
        throw error;
    }
}

// Executar script
if (require.main === module) {
    buscarRanking()
        .then(() => {
            console.log('✅ Consulta concluída!');
            process.exit(0);
        })
        .catch((error) => {
            console.error('❌ Erro fatal:', error);
            process.exit(1);
        });
}

module.exports = { buscarRanking };
