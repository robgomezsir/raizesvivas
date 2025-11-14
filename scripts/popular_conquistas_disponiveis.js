/**
 * Script de Migração: Conquistas Disponíveis (EXPANDIDO)
 * 
 * Este script popula a coleção conquistasDisponiveis no Firestore
 * com 80+ conquistas organizadas por categoria.
 * 
 * Execução:
 * 1. Via Firebase Console → Cloud Functions
 * 2. Via Firebase Admin SDK (Node.js)
 * 3. Via script local com credenciais de admin
 * 
 * IMPORTANTE: Execute este script ANTES de migrar os dados dos usuários
 * 
 * COMO EXECUTAR:
 * ```bash
 * # Via Node.js (requer Firebase Admin SDK)
 * node scripts/popular_conquistas_disponiveis.js
 * 
 * # Ou via Cloud Function
 * # Deploy da função e execute via Firebase Console
 * ```
 */

const admin = require('firebase-admin');

// Inicializar Admin SDK (ajustar conforme seu ambiente)
// admin.initializeApp();

const db = admin.firestore();

/**
 * Conquistas disponíveis expandidas (80+ conquistas)
 * Baseado em conquistas_expandidas.md
 */
const conquistasDisponiveis = [
    // ========================================
    // CATEGORIA: BEM-VINDO (Onboarding)
    // ========================================
    {
        id: "bem_vindo",
        titulo: "Bem-vindo à Família!",
        descricao: "Faça seu primeiro login no app",
        icone: "👋",
        categoria: "Bem-vindo",
        criterio: 1,
        pontosRecompensa: 10
    },
    {
        id: "primeiro_passo",
        titulo: "Primeiro Passo",
        descricao: "Complete seu perfil com nome e foto",
        icone: "👤",
        categoria: "Bem-vindo",
        criterio: 1,
        pontosRecompensa: 20
    },
    {
        id: "explorador_curioso",
        titulo: "Explorador Curioso",
        descricao: "Visualize a árvore genealógica pela primeira vez",
        icone: "🔍",
        categoria: "Bem-vindo",
        criterio: 1,
        pontosRecompensa: 15
    },
    {
        id: "tutorial_completo",
        titulo: "Aprendiz Rápido",
        descricao: "Complete o tutorial do aplicativo",
        icone: "🎓",
        categoria: "Bem-vindo",
        criterio: 1,
        pontosRecompensa: 30
    },
    {
        id: "primeira_visita_semanal",
        titulo: "Visitante Assíduo",
        descricao: "Acesse o app por 3 dias seguidos",
        icone: "📅",
        categoria: "Bem-vindo",
        criterio: 3,
        pontosRecompensa: 50
    },
    
    // ========================================
    // CATEGORIA: CONSTRUTOR (Adicionar Membros)
    // ========================================
    {
        id: "primeiro_membro",
        titulo: "Primeira Raiz",
        descricao: "Adicione o primeiro membro à sua árvore",
        icone: "🌱",
        categoria: "Construtor",
        criterio: 1,
        pontosRecompensa: 15
    },
    {
        id: "familia_nuclear",
        titulo: "Família Nuclear",
        descricao: "Adicione seus pais e irmãos (3 membros)",
        icone: "👨‍👩‍👧",
        categoria: "Construtor",
        criterio: 3,
        pontosRecompensa: 30
    },
    {
        id: "construtor_iniciante",
        titulo: "Construtor Iniciante",
        descricao: "Adicione 5 membros à sua árvore",
        icone: "👥",
        categoria: "Construtor",
        criterio: 5,
        pontosRecompensa: 50
    },
    {
        id: "duas_geracoes",
        titulo: "Duas Gerações",
        descricao: "Adicione membros de pelo menos 2 gerações diferentes",
        icone: "👨‍👩‍👧‍👦",
        categoria: "Construtor",
        criterio: 2,
        pontosRecompensa: 40
    },
    {
        id: "construtor_intermediario",
        titulo: "Construtor Intermediário",
        descricao: "Adicione 15 membros à sua árvore",
        icone: "👨‍👩‍👧‍👦",
        categoria: "Construtor",
        criterio: 15,
        pontosRecompensa: 100
    },
    {
        id: "tres_geracoes",
        titulo: "Três Gerações",
        descricao: "Conecte 3 gerações da família",
        icone: "👴👨👶",
        categoria: "Construtor",
        criterio: 3,
        pontosRecompensa: 75
    },
    {
        id: "arvore_crescendo",
        titulo: "Árvore Crescendo",
        descricao: "Adicione 25 membros à sua árvore",
        icone: "🌳",
        categoria: "Construtor",
        criterio: 25,
        pontosRecompensa: 200
    },
    {
        id: "construtor_avancado",
        titulo: "Construtor Avançado",
        descricao: "Adicione 50 membros à sua árvore",
        icone: "🌲",
        categoria: "Construtor",
        criterio: 50,
        pontosRecompensa: 350
    },
    {
        id: "quatro_geracoes",
        titulo: "Quatro Gerações",
        descricao: "Conecte 4 gerações da família",
        icone: "👴👨👦👶",
        categoria: "Construtor",
        criterio: 4,
        pontosRecompensa: 150
    },
    {
        id: "construtor_mestre",
        titulo: "Mestre Construtor",
        descricao: "Adicione 100 membros à sua árvore",
        icone: "🌴",
        categoria: "Construtor",
        criterio: 100,
        pontosRecompensa: 1000
    },
    {
        id: "cinco_geracoes",
        titulo: "Cinco Gerações",
        descricao: "Conecte 5 gerações da família",
        icone: "👴👨👦👶👼",
        categoria: "Construtor",
        criterio: 5,
        pontosRecompensa: 300
    },
    {
        id: "raizes_plantadas",
        titulo: "Raízes Plantadas",
        descricao: "Crie sua primeira família",
        icone: "🌱",
        categoria: "Construtor",
        criterio: 1,
        pontosRecompensa: 50
    },
    
    // ========================================
    // CATEGORIA: HISTORIADOR (Adicionar Informações)
    // ========================================
    {
        id: "primeira_foto",
        titulo: "Primeira Memória",
        descricao: "Adicione a primeira foto a um membro",
        icone: "📷",
        categoria: "Historiador",
        criterio: 1,
        pontosRecompensa: 15
    },
    {
        id: "primeira_data",
        titulo: "Marcador de Tempo",
        descricao: "Adicione data de nascimento a 3 membros",
        icone: "🎂",
        categoria: "Historiador",
        criterio: 3,
        pontosRecompensa: 25
    },
    {
        id: "historiador_iniciante",
        titulo: "Historiador Iniciante",
        descricao: "Adicione informações completas a 5 membros",
        icone: "📝",
        categoria: "Historiador",
        criterio: 5,
        pontosRecompensa: 50
    },
    {
        id: "fotografo_familiar",
        titulo: "Fotógrafo Familiar",
        descricao: "Adicione fotos a 5 membros diferentes",
        icone: "📸",
        categoria: "Historiador",
        criterio: 5,
        pontosRecompensa: 40
    },
    {
        id: "primeira_biografia",
        titulo: "Primeira História",
        descricao: "Escreva a primeira biografia",
        icone: "📖",
        categoria: "Historiador",
        criterio: 1,
        pontosRecompensa: 30
    },
    {
        id: "colecionador_memorias",
        titulo: "Colecionador de Memórias",
        descricao: "Adicione 15 fotos à árvore",
        icone: "🖼️",
        categoria: "Historiador",
        criterio: 15,
        pontosRecompensa: 100
    },
    {
        id: "biografo",
        titulo: "Biógrafo",
        descricao: "Escreva biografias para 5 membros",
        icone: "📚",
        categoria: "Historiador",
        criterio: 5,
        pontosRecompensa: 120
    },
    {
        id: "detalhista",
        titulo: "Detalhista",
        descricao: "Adicione local de nascimento a 10 membros",
        icone: "📍",
        categoria: "Historiador",
        criterio: 10,
        pontosRecompensa: 80
    },
    {
        id: "historiador_avancado",
        titulo: "Historiador Avançado",
        descricao: "Adicione 50 fotos à árvore",
        icone: "📷",
        categoria: "Historiador",
        criterio: 50,
        pontosRecompensa: 300
    },
    {
        id: "escritor_familiar",
        titulo: "Escritor Familiar",
        descricao: "Escreva biografias detalhadas para 15 membros",
        icone: "✍️",
        categoria: "Historiador",
        criterio: 15,
        pontosRecompensa: 250
    },
    {
        id: "arquivista_mestre",
        titulo: "Arquivista Mestre",
        descricao: "Adicione 100 fotos à árvore",
        icone: "📁",
        categoria: "Historiador",
        criterio: 100,
        pontosRecompensa: 800
    },
    {
        id: "cronista_familiar",
        titulo: "Cronista Familiar",
        descricao: "Preencha TODOS os campos de 25 membros",
        icone: "✅",
        categoria: "Historiador",
        criterio: 25,
        pontosRecompensa: 600
    },
    
    // ========================================
    // CATEGORIA: CONECTOR (Interação Social)
    // ========================================
    {
        id: "primeira_mensagem",
        titulo: "Primeira Conversa",
        descricao: "Envie sua primeira mensagem no chat",
        icone: "💬",
        categoria: "Conector",
        criterio: 1,
        pontosRecompensa: 10
    },
    {
        id: "sociavel",
        titulo: "Sociável",
        descricao: "Envie mensagens para 3 parentes diferentes",
        icone: "👥",
        categoria: "Conector",
        criterio: 3,
        pontosRecompensa: 30
    },
    {
        id: "primeiro_recado",
        titulo: "Primeiro Recado",
        descricao: "Publique seu primeiro recado no mural",
        icone: "📢",
        categoria: "Conector",
        criterio: 1,
        pontosRecompensa: 15
    },
    {
        id: "conector_iniciante",
        titulo: "Conector Iniciante",
        descricao: "Envie 10 mensagens no chat",
        icone: "💭",
        categoria: "Conector",
        criterio: 10,
        pontosRecompensa: 50
    },
    {
        id: "apoiador",
        titulo: "Apoiador",
        descricao: "Dê 5 apoios familiares em recados",
        icone: "❤️",
        categoria: "Conector",
        criterio: 5,
        pontosRecompensa: 25
    },
    {
        id: "comunicador",
        titulo: "Comunicador",
        descricao: "Envie 50 mensagens no chat",
        icone: "💬",
        categoria: "Conector",
        criterio: 50,
        pontosRecompensa: 150
    },
    {
        id: "publicador",
        titulo: "Publicador",
        descricao: "Crie 10 recados no mural",
        icone: "📰",
        categoria: "Conector",
        criterio: 10,
        pontosRecompensa: 100
    },
    {
        id: "rede_social",
        titulo: "Rede Social",
        descricao: "Converse com 10 parentes diferentes",
        icone: "🌐",
        categoria: "Conector",
        criterio: 10,
        pontosRecompensa: 120
    },
    {
        id: "conector_avancado",
        titulo: "Conector Avançado",
        descricao: "Envie 200 mensagens no chat",
        icone: "💬",
        categoria: "Conector",
        criterio: 200,
        pontosRecompensa: 400
    },
    {
        id: "influencer_familiar",
        titulo: "Influencer Familiar",
        descricao: "Receba 50 apoios familiares em seus recados",
        icone: "👍",
        categoria: "Conector",
        criterio: 50,
        pontosRecompensa: 250
    },
    {
        id: "conector_mestre",
        titulo: "Mestre Conector",
        descricao: "Envie 1000 mensagens no chat",
        icone: "💬",
        categoria: "Conector",
        criterio: 1000,
        pontosRecompensa: 1000
    },
    {
        id: "celebridade_familiar",
        titulo: "Celebridade Familiar",
        descricao: "Receba 200 apoios familiares",
        icone: "⭐",
        categoria: "Conector",
        criterio: 200,
        pontosRecompensa: 600
    },
    
    // ========================================
    // CATEGORIA: EXPLORADOR (Navegação)
    // ========================================
    {
        id: "primeira_exploracao",
        titulo: "Primeira Exploração",
        descricao: "Visualize 5 perfis de membros diferentes",
        icone: "🔍",
        categoria: "Explorador",
        criterio: 5,
        pontosRecompensa: 20
    },
    {
        id: "curioso",
        titulo: "Curioso",
        descricao: "Abra a árvore genealógica 10 vezes",
        icone: "👀",
        categoria: "Explorador",
        criterio: 10,
        pontosRecompensa: 30
    },
    {
        id: "descobridor_parentesco",
        titulo: "Descobridor de Parentesco",
        descricao: "Visualize o cálculo de parentesco pela primeira vez",
        icone: "🧮",
        categoria: "Explorador",
        criterio: 1,
        pontosRecompensa: 25
    },
    {
        id: "explorador_ativo",
        titulo: "Explorador Ativo",
        descricao: "Visualize 25 perfis diferentes",
        icone: "🗺️",
        categoria: "Explorador",
        criterio: 25,
        pontosRecompensa: 100
    },
    {
        id: "navegador",
        titulo: "Navegador",
        descricao: "Navegue pela árvore 50 vezes",
        icone: "🧭",
        categoria: "Explorador",
        criterio: 50,
        pontosRecompensa: 120
    },
    {
        id: "conhecedor_familia",
        titulo: "Conhecedor da Família",
        descricao: "Visualize perfis de 50 membros diferentes",
        icone: "👨‍👩‍👧‍👦",
        categoria: "Explorador",
        criterio: 50,
        pontosRecompensa: 250
    },
    {
        id: "explorador_mestre",
        titulo: "Explorador Mestre",
        descricao: "Navegue pela árvore 200 vezes",
        icone: "🌍",
        categoria: "Explorador",
        criterio: 200,
        pontosRecompensa: 400
    },
    
    // ========================================
    // CATEGORIA: ASSIDUIDADE (Engajamento Temporal)
    // ========================================
    {
        id: "primeira_semana",
        titulo: "Primeira Semana",
        descricao: "Use o app por 7 dias seguidos",
        icone: "📅",
        categoria: "Assiduidade",
        criterio: 7,
        pontosRecompensa: 100
    },
    {
        id: "madrugador",
        titulo: "Madrugador",
        descricao: "Acesse o app antes das 8h da manhã",
        icone: "🌅",
        categoria: "Assiduidade",
        criterio: 1,
        pontosRecompensa: 20
    },
    {
        id: "noturno",
        titulo: "Coruja Noturna",
        descricao: "Acesse o app depois das 22h",
        icone: "🌙",
        categoria: "Assiduidade",
        criterio: 1,
        pontosRecompensa: 20
    },
    {
        id: "usuario_mensal",
        titulo: "Usuário Mensal",
        descricao: "Use o app por 30 dias seguidos",
        icone: "📆",
        categoria: "Assiduidade",
        criterio: 30,
        pontosRecompensa: 300
    },
    {
        id: "fim_de_semana",
        titulo: "Fim de Semana Ativo",
        descricao: "Acesse o app em 10 fins de semana",
        icone: "🎉",
        categoria: "Assiduidade",
        criterio: 10,
        pontosRecompensa: 150
    },
    {
        id: "veterano",
        titulo: "Veterano",
        descricao: "Use o app por 100 dias seguidos",
        icone: "🎖️",
        categoria: "Assiduidade",
        criterio: 100,
        pontosRecompensa: 1000
    },
    {
        id: "lenda",
        titulo: "Lenda Familiar",
        descricao: "Use o app por 365 dias seguidos",
        icone: "👑",
        categoria: "Assiduidade",
        criterio: 365,
        pontosRecompensa: 5000
    },
    
    // ========================================
    // CATEGORIA: EVENTOS ESPECIAIS
    // ========================================
    {
        id: "aniversariante",
        titulo: "Parabéns!",
        descricao: "Acesse o app no seu aniversário",
        icone: "🎂",
        categoria: "Especial",
        criterio: 1,
        pontosRecompensa: 50
    },
    {
        id: "natal_familiar",
        titulo: "Espírito Natalino",
        descricao: "Acesse o app no Natal",
        icone: "🎄",
        categoria: "Especial",
        criterio: 1,
        pontosRecompensa: 30
    },
    {
        id: "ano_novo",
        titulo: "Feliz Ano Novo!",
        descricao: "Acesse o app no Réveillon",
        icone: "🎆",
        categoria: "Especial",
        criterio: 1,
        pontosRecompensa: 40
    },
    {
        id: "dia_das_maes",
        titulo: "Homenagem à Mãe",
        descricao: "Acesse o app no Dia das Mães",
        icone: "💐",
        categoria: "Especial",
        criterio: 1,
        pontosRecompensa: 30
    },
    {
        id: "dia_dos_pais",
        titulo: "Homenagem ao Pai",
        descricao: "Acesse o app no Dia dos Pais",
        icone: "🤝",
        categoria: "Especial",
        criterio: 1,
        pontosRecompensa: 30
    },
    
    // ========================================
    // CATEGORIA: ÉPICAS (Raras e Difíceis)
    // ========================================
    {
        id: "perfeccionista",
        titulo: "Perfeccionista",
        descricao: "Complete 100% de informações em 50 membros",
        icone: "✅",
        categoria: "Épica",
        criterio: 50,
        pontosRecompensa: 2000
    },
    {
        id: "genealogista_profissional",
        titulo: "Genealogista Profissional",
        descricao: "Complete TODAS as conquistas de Construtor",
        icone: "🏆",
        categoria: "Épica",
        criterio: 1,
        pontosRecompensa: 1500
    },
    {
        id: "historiador_mestre_epico",
        titulo: "Historiador Mestre",
        descricao: "Complete TODAS as conquistas de Historiador",
        icone: "📚",
        categoria: "Épica",
        criterio: 1,
        pontosRecompensa: 1500
    },
    {
        id: "unificador_familiar",
        titulo: "Unificador Familiar",
        descricao: "Conecte 10 subfamílias diferentes",
        icone: "🔗",
        categoria: "Épica",
        criterio: 10,
        pontosRecompensa: 1000
    },
    {
        id: "lenda_viva",
        titulo: "Lenda Viva",
        descricao: "Alcance nível 50",
        icone: "💎",
        categoria: "Épica",
        criterio: 50,
        pontosRecompensa: 5000
    },
    {
        id: "colecionador_supremo",
        titulo: "Colecionador Supremo",
        descricao: "Desbloqueie TODAS as conquistas do app",
        icone: "⭐",
        categoria: "Épica",
        criterio: 1,
        pontosRecompensa: 10000
    }
];

/**
 * Popula conquistas disponíveis no Firestore
 */
async function popularConquistasDisponiveis() {
    console.log('🚀 Iniciando população de conquistas disponíveis...');
    console.log(`📊 Total de conquistas: ${conquistasDisponiveis.length}`);
    
    let batch = db.batch();
    let count = 0;
    const batchSize = 500; // Limite do Firestore
    
    for (let i = 0; i < conquistasDisponiveis.length; i++) {
        const conquista = conquistasDisponiveis[i];
        const docRef = db.collection('conquistasDisponiveis').doc(conquista.id);
        batch.set(docRef, conquista);
        count++;
        
        // Firestore tem limite de 500 operações por batch
        if (count >= batchSize || i === conquistasDisponiveis.length - 1) {
            await batch.commit();
            console.log(`✅ ${count} conquistas adicionadas ao batch ${Math.floor(i / batchSize) + 1}`);
            if (i < conquistasDisponiveis.length - 1) {
                batch = db.batch();
                count = 0;
            }
        }
    }
    
    console.log(`✨ ${conquistasDisponiveis.length} conquistas disponíveis populadas com sucesso!`);
    
    // Estatísticas
    const categorias = {};
    let totalPontos = 0;
    conquistasDisponiveis.forEach(c => {
        categorias[c.categoria] = (categorias[c.categoria] || 0) + 1;
        totalPontos += c.pontosRecompensa;
    });
    
    console.log('\n📊 Estatísticas:');
    console.log(`   Total de conquistas: ${conquistasDisponiveis.length}`);
    console.log(`   Pontuação máxima: ${totalPontos} pontos`);
    console.log('\n📋 Por categoria:');
    Object.entries(categorias).forEach(([cat, qtd]) => {
        console.log(`   ${cat}: ${qtd} conquistas`);
    });
    
    return conquistasDisponiveis.length;
}

// Executar se chamado diretamente
if (require.main === module) {
    popularConquistasDisponiveis()
        .then((total) => {
            console.log(`\n✅ Migração concluída! ${total} conquistas populadas.`);
            process.exit(0);
        })
        .catch((error) => {
            console.error('❌ Erro na migração:', error);
            process.exit(1);
        });
}

module.exports = { popularConquistasDisponiveis };
