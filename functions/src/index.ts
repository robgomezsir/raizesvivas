import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { beforeUserCreated as beforeUserCreatedFn } from "firebase-functions/v2/identity";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { HttpsError } from "firebase-functions/v2/https";
import { logger } from "firebase-functions";
import nodemailer from "nodemailer";

const app = initializeApp();
const db = getFirestore(app);

/**
 * Auth Blocking Function - before user is created (v2)
 * Só permite cadastro se existir invite aprovado para o e-mail.
 * 
 * ⚠️ ATENÇÃO: Esta função requer que o projeto tenha GCIP (Google Cloud Identity Platform) habilitado.
 * Para habilitar: Firebase Console → Authentication → Settings → Advanced → Enable Google Cloud Identity Platform
 */
export const beforeUserCreated = beforeUserCreatedFn(async (event) => {
  const user = event.data;
  const email = user?.email?.trim().toLowerCase();
  if (!email) {
    throw new HttpsError(
      "failed-precondition",
      "E-mail é obrigatório para cadastro."
    );
  }

  // Verificar se existe convite (pendente ou aceito)
  // Convites PENDENTE são criados quando um admin aprova um pedido de convite
  // Convites ACEITO são convites que o usuário já aceitou
  const invitesSnap = await db
    .collection("invites")
    .where("emailConvidado", "==", email)
    .where("status", "in", [
      "PENDENTE", "Pendente", "pendente",
      "accepted", "aprovado", "approved", "ACEITO", "Aceito"
    ])
    .limit(1)
    .get();

  // Se encontrou convite, verificar se não expirou (apenas para PENDENTE)
  if (!invitesSnap.empty) {
    const inviteData = invitesSnap.docs[0].data();
    const status = inviteData?.status?.toString() || "";
    const expiraEm = inviteData?.expiraEm;
    
    // Se for convite PENDENTE, verificar se não expirou
    if (status.toUpperCase() === "PENDENTE" && expiraEm) {
      let expiraTimestamp: number;
      
      // Tratar diferentes formatos de Timestamp do Firestore
      if (expiraEm.toMillis) {
        expiraTimestamp = expiraEm.toMillis();
      } else if (expiraEm._seconds) {
        expiraTimestamp = expiraEm._seconds * 1000 + (expiraEm._nanoseconds || 0) / 1000000;
      } else if (expiraEm.seconds) {
        expiraTimestamp = expiraEm.seconds * 1000 + (expiraEm.nanoseconds || 0) / 1000000;
      } else {
        // Se não conseguir determinar, assumir que não expirou
        logger.warn(`Não foi possível determinar expiração do convite para ${email}, permitindo cadastro`);
        return;
      }
      
      const agora = Date.now();
      
      if (agora > expiraTimestamp) {
        logger.warn(`Convite expirado para ${email} (expirou em ${new Date(expiraTimestamp).toISOString()})`);
        // Convite expirado, continuar para verificar se é usuário existente
      } else {
        logger.info(`Convite válido encontrado para ${email} (status: ${status}, expira em ${new Date(expiraTimestamp).toISOString()})`);
        return; // Convite válido, permitir cadastro
      }
    } else {
      // Convite ACEITO ou outro status válido, permitir
      logger.info(`Convite aceito encontrado para ${email} (status: ${status})`);
      return;
    }
  }

  // Se não houver convite válido, verificar se é um usuário existente no Firestore
  // (chegamos aqui se não encontrou convite OU se encontrou mas expirou)
  {
    // Verificar se já existe um usuário com este email no Firestore (qualquer usuário, não apenas admin)
    const usersSnap = await db
      .collection("users")
      .where("email", "==", email)
      .limit(1)
      .get();
    
    // Se for usuário existente, permitir recriação da conta no Firebase Auth
    // Isso permite que usuários que já existem no DB possam acessar o app normalmente
    if (!usersSnap.empty) {
      const userData = usersSnap.docs[0].data();
      const isAdmin = userData?.ehAdministrador === true || userData?.ehAdministradorSenior === true;
      logger.info(
        `Permitindo recriação de conta para usuário existente: ${email} ` +
        `(Admin: ${isAdmin})`
      );
      return;
    }

    // Caso contrário, bloquear (apenas novos usuários precisam de convite)
    throw new HttpsError(
      "permission-denied",
      "Cadastro permitido apenas por convite ou aprovação do administrador."
    );
  }
});

/**
 * Envia e-mail automaticamente quando um convite é criado (v2)
 * Requer configuração de SMTP via variáveis de ambiente:
 * SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASS, SMTP_FROM
 * 
 * Configure via Firebase Console → Functions → Configurações → Environment variables
 * OU via CLI: firebase functions:secrets:set SMTP_PASS
 */
export const onInviteCreated = onDocumentCreated("invites/{inviteId}", async (event) => {
  const snap = event.data;
  if (!snap) return;
  const data = snap.data() as any;
  const email = (data?.emailConvidado || data?.email || "").toString();
  if (!email) {
    logger.warn("Convite criado sem emailConvidado");
    return;
  }

  // Lê configurações SMTP de variáveis de ambiente (com valores padrão)
  const host = process.env.SMTP_HOST || "smtp.gmail.com";
  const port = Number(process.env.SMTP_PORT || "587");
  const user = process.env.SMTP_USER || "robgomez.sir@gmail.com";
  const pass = process.env.SMTP_PASS || "";
  const from = process.env.SMTP_FROM || "robgomez.sir@gmail.com";

  if (!host || !user || !pass) {
    logger.warn("SMTP não configurado. Apenas logando o convite.");
    logger.info(`Enviar convite para: ${email}`);
    return;
  }

  const transporter = nodemailer.createTransport({
    host: host,
    port: port,
    secure: port === 465,
    auth: { user: user, pass: pass },
  });

  const pessoaVinculada = data?.pessoaVinculada
    ? `\n\nVocê será vinculado(a) a: ${data.pessoaVinculada}`
    : "";
  const body = [
    "Olá,",
    "",
    "Seu pedido de convite foi aprovado! Você foi convidado(a) para participar da árvore genealógica no aplicativo Raízes Vivas!",
    pessoaVinculada,
    "",
    "Para acessar o aplicativo:",
    "1. Baixe o aplicativo Raízes Vivas (se ainda não tiver)",
    "2. CRIE SUA CONTA usando este e-mail: " + email,
    "   - Vá na tela de Cadastro",
    "   - Use este mesmo e-mail: " + email,
    "   - Crie uma senha",
    "   - Preencha seu nome completo",
    "3. Após criar a conta, faça login",
    '4. Vá em "Aceitar Convites" e confirme seu convite',
    "",
    "⚠️ IMPORTANTE: Você precisa criar uma conta primeiro antes de fazer login!",
    "",
    "Este convite expira em 7 dias.",
    "",
    "Aguardamos sua participação!",
    "",
    "Atenciosamente,",
    "Equipe Raízes Vivas",
  ].join("\n");

  await transporter.sendMail({
    from: from,
    to: email,
    subject: "Convite para Árvore Genealógica - Raízes Vivas",
    text: body,
  });
  logger.info(`Convite enviado por email para ${email}`);
});

