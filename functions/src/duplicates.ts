import { onDocumentWritten } from "firebase-functions/v2/firestore";
import { getFirestore, Timestamp } from "firebase-admin/firestore";
import { logger } from "firebase-functions";

const db = getFirestore();

interface Pessoa {
  id: string;
  nome: string;
  dataNascimento?: Timestamp;
  pai?: string;
  mae?: string;
  // Outros campos relevantes
}

interface DuplicateRecord {
  pessoa1Id: string;
  pessoa2Id: string;
  pessoa1Nome: string;
  pessoa2Nome: string;
  score: number;
  razoes: string[];
  createdAt: Timestamp;
  status: 'PENDING' | 'RESOLVED' | 'IGNORED';
}

/**
 * Trigger para detectar duplicatas quando uma pessoa é criada ou atualizada
 */
export const detectarDuplicatas = onDocumentWritten("pessoas/{pessoaId}", async (event) => {
  const pessoaId = event.params.pessoaId;
  const pessoa = event.data?.after?.data() as Pessoa | undefined;

  // Se a pessoa foi deletada, não faz nada (ou limpa duplicatas existentes envolvendo ela)
  if (!pessoa) {
    await limparDuplicatasEnvolvendo(pessoaId);
    return;
  }

  // Se a pessoa acabou de ser criada ou teve campos relevantes alterados
  const before = event.data?.before?.data() as Pessoa | undefined;
  if (before && !houveMudancaRelevante(before, pessoa)) {
    return;
  }

  logger.info(`🔍 Iniciando detecção de duplicatas para: ${pessoa.nome} (${pessoaId})`);

  try {
    // Buscar todas as outras pessoas para comparar
    // NOTA: Em produção com muitos usuários, isso deve ser otimizado (ex: buscar apenas com mesmo sobrenome ou data prox)
    const snapshot = await db.collection("pessoas").get();
    const outrasPessoas = snapshot.docs
      .map(doc => ({ id: doc.id, ...doc.data() } as Pessoa))
      .filter(p => p.id !== pessoaId);

    const duplicatasEncontradas: DuplicateRecord[] = [];

    for (const outraPessoa of outrasPessoas) {
      const resultado = calcularSimilaridade(pessoa, outraPessoa);
      
      if (resultado.score >= 80) { // Threshold de 80%
        logger.info(`⚠️ Possível duplicata encontrada: ${pessoa.nome} x ${outraPessoa.nome} (Score: ${resultado.score})`);
        
        duplicatasEncontradas.push({
          pessoa1Id: pessoaId < outraPessoa.id ? pessoaId : outraPessoa.id, // Ordenar IDs para consistência
          pessoa2Id: pessoaId < outraPessoa.id ? outraPessoa.id : pessoaId,
          pessoa1Nome: pessoaId < outraPessoa.id ? pessoa.nome : outraPessoa.nome,
          pessoa2Nome: pessoaId < outraPessoa.id ? outraPessoa.nome : pessoa.nome,
          score: resultado.score,
          razoes: resultado.razoes,
          createdAt: Timestamp.now(),
          status: 'PENDING'
        });
      }
    }

    // Salvar duplicatas encontradas
    for (const duplicata of duplicatasEncontradas) {
      // Verificar se já existe registro dessa duplicata (mesmo ignorada ou resolvida)
      const idDuplicata = `${duplicata.pessoa1Id}_${duplicata.pessoa2Id}`;
      const docRef = db.collection("duplicates").doc(idDuplicata);
      const docSnap = await docRef.get();

      if (!docSnap.exists) {
        await docRef.set(duplicata);
      } else {
        // Se já existe mas estava PENDING, atualiza score/razoes
        // Se estava IGNORED ou RESOLVED, não sobrescreve status automaticamente, a menos que score aumente muito?
        // Por simplificação, se já existe, apenas atualiza score e razoes, mantendo status se for IGNORED
        const existingData = docSnap.data();
        if (existingData?.status !== 'RESOLVED') {
           await docRef.update({
             score: duplicata.score,
             razoes: duplicata.razoes,
             updatedAt: Timestamp.now()
           });
        }
      }
    }

  } catch (error) {
    logger.error("❌ Erro ao detectar duplicatas:", error);
  }
});

/**
 * Remove registros de duplicatas quando uma pessoa é excluída
 */
async function limparDuplicatasEnvolvendo(pessoaId: string) {
  const batch = db.batch();
  
  // Buscar duplicatas onde pessoaId é pessoa1
  const snapshot1 = await db.collection("duplicates").where("pessoa1Id", "==", pessoaId).get();
  snapshot1.docs.forEach(doc => batch.delete(doc.ref));

  // Buscar duplicatas onde pessoaId é pessoa2
  const snapshot2 = await db.collection("duplicates").where("pessoa2Id", "==", pessoaId).get();
  snapshot2.docs.forEach(doc => batch.delete(doc.ref));

  if (!snapshot1.empty || !snapshot2.empty) {
    await batch.commit();
    logger.info(`♻️ Limpou duplicatas envolvendo pessoa excluída: ${pessoaId}`);
  }
}

function houveMudancaRelevante(antes: Pessoa, depois: Pessoa): boolean {
  return antes.nome !== depois.nome ||
         !mesmaData(antes.dataNascimento, depois.dataNascimento) ||
         antes.pai !== depois.pai ||
         antes.mae !== depois.mae;
}

function mesmaData(d1?: Timestamp, d2?: Timestamp): boolean {
  if (!d1 && !d2) return true;
  if (!d1 || !d2) return false;
  return d1.toMillis() === d2.toMillis();
}

/**
 * Calcula score de similaridade entre duas pessoas
 */
function calcularSimilaridade(p1: Pessoa, p2: Pessoa): { score: number, razoes: string[] } {
  let score = 0;
  const razoes: string[] = [];

  // 1. Nome exato ou muito similar (40 pontos)
  if (p1.nome.trim().toLowerCase() === p2.nome.trim().toLowerCase()) {
    score += 40;
    razoes.push("Nome idêntico");
  } else if (p1.nome.toLowerCase().includes(p2.nome.toLowerCase()) || p2.nome.toLowerCase().includes(p1.nome.toLowerCase())) {
     // Check parcial simples
     score += 20;
     razoes.push("Nome similar");
  }

  // 2. Data de Nascimento (30 pontos)
  if (p1.dataNascimento && p2.dataNascimento) {
    const d1 = p1.dataNascimento.toDate();
    const d2 = p2.dataNascimento.toDate();
    
    if (d1.getDate() === d2.getDate() && 
        d1.getMonth() === d2.getMonth() && 
        d1.getFullYear() === d2.getFullYear()) {
      score += 30;
      razoes.push("Mesma data de nascimento");
    }
  }

  // 3. Mesmos Pais (30 pontos - 15 cada)
  if (p1.pai && p2.pai && p1.pai === p2.pai) {
    score += 15;
    razoes.push("Mesmo pai");
  }
  if (p1.mae && p2.mae && p1.mae === p2.mae) {
    score += 15;
    razoes.push("Mesma mãe");
  }

  return { score, razoes };
}
