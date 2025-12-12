import { initializeApp } from "firebase-admin/app";
import { getFirestore, Timestamp } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";
import { beforeUserCreated as beforeUserCreatedFn } from "firebase-functions/v2/identity";
import { onDocumentCreated, onDocumentWritten, onDocumentUpdated } from "firebase-functions/v2/firestore";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { HttpsError } from "firebase-functions/v2/https";
import { logger } from "firebase-functions";
import nodemailer from "nodemailer";

const app = initializeApp();
const db = getFirestore(app);
const messaging = getMessaging(app);

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

  // Enviar notificação push se o usuário já tiver conta
  try {
    const userSnapshot = await db
      .collection("usuarios")
      .where("email", "==", email)
      .limit(1)
      .get();

    if (!userSnapshot.empty) {
      const userId = userSnapshot.docs[0].id;
      await sendPushNotification(
        userId,
        "Você foi convidado!",
        "Seu pedido de convite foi aprovado! Acesse o app para aceitar.",
        "convite",
        snap.id
      );
    }
  } catch (error) {
    logger.warn("Erro ao enviar push de convite:", error);
  }
});


// ============================================================
// FUNÇÕES AUXILIARES PARA NOTIFICAÇÕES PUSH
// ============================================================

/**
 * Envia notificação push para um usuário específico
 */
async function sendPushNotification(
  userId: string,
  title: string,
  body: string,
  type: string,
  relatedId?: string,
  imageUrl?: string
): Promise<void> {
  try {
    // Buscar token FCM do usuário
    const userDoc = await db
      .collection("usuarios")
      .doc(userId)
      .get();

    const fcmToken = userDoc.data()?.fcmToken;

    if (!fcmToken) {
      logger.warn(`⚠️ Usuário ${userId} não possui token FCM registrado - notificação não enviada`);

      // Registrar em analytics para monitoramento
      await db.collection("analytics_notificacoes").add({
        userId: userId,
        type: type,
        title: title,
        sentAt: Timestamp.now(),
        success: false,
        error: "NO_FCM_TOKEN",
      });

      return;
    }

    // Montar payload da notificação
    const message: any = {
      token: fcmToken,
      notification: {
        title: title,
        body: body,
      },
      data: {
        type: type,
        targetUserId: userId,
        relatedId: relatedId || "",
        timestamp: Date.now().toString(),
      },
      android: {
        priority: "high",
        notification: {
          channelId: getChannelId(type),
          sound: "default",
          priority: "high",
          defaultVibrateTimings: true,
        },
      },
      apns: {
        payload: {
          aps: {
            sound: "default",
            badge: 1,
          },
        },
      },
    };

    // Adicionar imagem se fornecida
    if (imageUrl) {
      message.notification.imageUrl = imageUrl;
    }

    // Enviar notificação
    const response = await messaging.send(message);
    logger.info(`✅ Notificação enviada para ${userId}: ${response}`);

    // Registrar analytics
    await db
      .collection("analytics_notificacoes")
      .add({
        userId: userId,
        type: type,
        sentAt: Timestamp.now(),
        success: true,
      });
  } catch (error: any) {
    logger.error(`❌ Erro ao enviar notificação para ${userId}:`, error);

    // Se o token for inválido, removê-lo do Firestore
    if (
      error.code === "messaging/invalid-registration-token" ||
      error.code === "messaging/registration-token-not-registered"
    ) {
      await db
        .collection("usuarios")
        .doc(userId)
        .update({ fcmToken: null });

      logger.log(`Token FCM inválido removido para usuário ${userId}`);
    }
  }
}

/**
 * Retorna o ID do canal de notificação baseado no tipo
 */
function getChannelId(type: string): string {
  const channels: { [key: string]: string } = {
    mensagem: "raizes_vivas_messages",
    edicao_aprovada: "raizes_vivas_edits",
    edicao_rejeitada: "raizes_vivas_edits",
    conquista: "raizes_vivas_achievements",
    aniversario: "raizes_vivas_birthdays",
    convite: "raizes_vivas_invites",
    recado: "raizes_vivas_messages",
    novo_membro: "raizes_vivas_default",
    comentario_foto: "raizes_vivas_messages",
    reacao_recado: "raizes_vivas_default",
    nova_foto: "raizes_vivas_photos",
    casamento: "raizes_vivas_events",
    nascimento: "raizes_vivas_events",
    reacao: "raizes_vivas_reactions",
    atualizacao_app: "raizes_vivas_updates"
  };
  return channels[type] || "raizes_vivas_default";
}

/**
 * Busca nome de um usuário
 */
async function getUserName(userId: string): Promise<string> {
  try {
    const userDoc = await db.collection("usuarios").doc(userId).get();

    return userDoc.data()?.nome || "Alguém";
  } catch (error) {
    logger.error("Erro ao buscar nome do usuário:", error);
    return "Alguém";
  }
}

/**
 * Busca nome de uma pessoa
 */
async function getPersonName(personId: string): Promise<string> {
  try {
    const personDoc = await db.collection("pessoas").doc(personId).get();

    return personDoc.data()?.nome || "Uma pessoa";
  } catch (error) {
    logger.error("Erro ao buscar nome da pessoa:", error);
    return "Uma pessoa";
  }
}

// ============================================================
// TRIGGERS DE NOTIFICAÇÃO
// ============================================================

/**
 * TRIGGER: Nova mensagem de chat
 */
export const onMessageCreated = onDocumentCreated(
  "mensagens_chat/{messageId}",
  async (event) => {
    const snap = event.data;
    if (!snap) return;

    const data = snap.data() as any;
    const remetenteId = data?.remetenteId;
    const destinatarioId = data?.destinatarioId;
    const mensagem = data?.mensagem || "";

    if (!remetenteId || !destinatarioId) {
      logger.warn("Mensagem sem remetente ou destinatário");
      return;
    }

    // Buscar nome do remetente
    const remetenteName = await getUserName(remetenteId);

    // Enviar notificação para o destinatário
    await sendPushNotification(
      destinatarioId,
      `Nova mensagem de ${remetenteName}`,
      mensagem.substring(0, 100), // Limitar tamanho
      "mensagem",
      snap.id
    );
  }
);

/**
 * TRIGGER: Status de edição mudou
 */
export const onEdicaoStatusChanged = onDocumentCreated(
  "edicoes_pendentes/{edicaoId}",
  async (event) => {
    const snap = event.data;
    if (!snap) return;

    const data = snap.data() as any;
    const status = data?.status;
    const autorId = data?.autorId;
    const tipo = data?.tipo;

    if (!autorId || !status) return;

    // Apenas enviar notificação se foi aprovada ou rejeitada
    if (status === "aprovada" || status === "rejeitada") {
      const title =
        status === "aprovada" ? "Edição aprovada!" : "Edição rejeitada";
      const body =
        status === "aprovada"
          ? `Sua edição de ${tipo} foi aprovada.`
          : `Sua edição de ${tipo} foi rejeitada.`;

      await sendPushNotification(
        autorId,
        title,
        body,
        `edicao_${status}`,
        snap.id
      );
    }
  }
);

/**
 * TRIGGER: Novo recado criado
 */
export const onRecadoCreated = onDocumentCreated(
  "recados/{recadoId}",
  async (event) => {
    const snap = event.data;
    if (!snap) return;

    const data = snap.data() as any;
    const autorId = data?.autorId;
    const mensagem = data?.mensagem || "";

    if (!autorId) return;

    // Buscar nome do autor
    const autorName = await getUserName(autorId);

    // Buscar todos os usuários para enviar notificação
    const usersSnapshot = await db.collection("usuarios").get();

    // Filtrar usuários com token FCM e que não sejam o autor
    const usuariosComToken = usersSnapshot.docs.filter((doc) => {
      const data = doc.data();
      return doc.id !== autorId && data.fcmToken != null;
    });

    logger.info(`📊 Enviando recado para ${usuariosComToken.length}/${usersSnapshot.size - 1} usuários com token FCM`);

    // Enviar notificação para todos exceto o autor
    const promises = usuariosComToken.map((doc) =>
      sendPushNotification(
        doc.id,
        `Novo recado de ${autorName}`,
        mensagem.substring(0, 100),
        "recado",
        snap.id
      )
    );

    await Promise.all(promises);
  }
);

// ============================================================
// NOVOS TRIGGERS DE NOTIFICAÇÃO
// ============================================================

/**
 * TRIGGER: Aniversários (Scheduled - Diário às 9h)
 * Notifica todos os usuários sobre aniversariantes do dia
 */
export const onAniversarioAgendado = onSchedule(
  {
    schedule: "0 9 * * *",
    timeZone: "America/Sao_Paulo",
  },
  async (event) => {
    const hoje = new Date();
    const dia = hoje.getDate();
    const mes = hoje.getMonth() + 1; // JavaScript months are 0-indexed

    logger.info(`🎂 Verificando aniversários para ${dia}/${mes}`);

    try {
      // Buscar todas as pessoas
      const pessoasSnapshot = await db.collection("pessoas").get();

      const aniversariantes: any[] = [];

      // Filtrar pessoas com aniversário hoje
      pessoasSnapshot.docs.forEach((doc) => {
        const pessoa = doc.data();
        if (pessoa.dataNascimento) {
          const dataNasc = pessoa.dataNascimento.toDate();
          if (dataNasc.getDate() === dia && dataNasc.getMonth() + 1 === mes) {
            aniversariantes.push({ id: doc.id, ...pessoa });
          }
        }
      });

      if (aniversariantes.length === 0) {
        logger.info("Nenhum aniversariante hoje");
        return;
      }

      logger.info(`🎉 ${aniversariantes.length} aniversariante(s) encontrado(s)`);

      // Buscar todos os usuários
      const usuariosSnapshot = await db.collection("usuarios").get();

      // Filtrar apenas usuários com token FCM
      const usuariosComToken = usuariosSnapshot.docs.filter(doc => {
        const data = doc.data();
        return data.fcmToken != null;
      });

      logger.info(`📊 ${usuariosComToken.length}/${usuariosSnapshot.size} usuários com token FCM`);

      // Enviar notificação para cada aniversariante
      for (const pessoa of aniversariantes) {
        const promises = usuariosComToken.map((userDoc) =>
          sendPushNotification(
            userDoc.id,
            `🎂 Aniversário de ${pessoa.nome}!`,
            `Hoje é aniversário de ${pessoa.nome}. Envie seus parabéns!`,
            "aniversario",
            pessoa.id
          )
        );

        await Promise.all(promises);
      }

      logger.info("✅ Notificações de aniversário enviadas");
    } catch (error) {
      logger.error("❌ Erro ao processar aniversários:", error);
    }
  }
);

/**
 * TRIGGER: Conquista Desbloqueada
 * Notifica usuário quando conquista é concluída
 */
export const onConquistaDesbloqueada = onDocumentWritten(
  "usuarios/{userId}/conquistasProgresso/{conquistaId}",
  async (event) => {
    const before = event.data?.before?.data();
    const after = event.data?.after?.data();

    // Detectar se conquista foi desbloqueada
    if (!before?.concluida && after?.concluida) {
      const userId = event.params.userId;
      const conquistaId = event.params.conquistaId;

      logger.info(`🏆 Conquista desbloqueada: ${conquistaId} para ${userId}`);

      try {
        // Buscar dados da conquista
        const conquistaDoc = await db
          .collection("conquistasDisponiveis")
          .doc(conquistaId)
          .get();

        const conquista = conquistaDoc.data();

        if (!conquista) {
          logger.warn(`Conquista ${conquistaId} não encontrada`);
          return;
        }

        await sendPushNotification(
          userId,
          "🏆 Conquista Desbloqueada!",
          `Parabéns! Você desbloqueou: ${conquista.titulo || "Nova conquista"}`,
          "conquista",
          conquistaId
        );
      } catch (error) {
        logger.error("❌ Erro ao notificar conquista:", error);
      }
    }
  }
);

/**
 * TRIGGER: Novo Membro Cadastrado
 * Notifica admins quando novo usuário se cadastra
 */
export const onNovoMembroCadastrado = onDocumentCreated(
  "usuarios/{userId}",
  async (event) => {
    const novoUsuario = event.data?.data();
    const userId = event.params.userId;

    if (!novoUsuario) return;

    logger.info(`👋 Novo membro cadastrado: ${novoUsuario.nome} (${userId})`);

    try {
      // Buscar todos os admins
      const adminsSnapshot = await db
        .collection("users")
        .where("ehAdministrador", "==", true)
        .get();

      if (adminsSnapshot.empty) {
        logger.warn("Nenhum admin encontrado para notificar");
        return;
      }

      // Filtrar apenas admins com token FCM
      const adminsComToken = adminsSnapshot.docs.filter(doc => {
        const data = doc.data();
        return data.fcmToken != null;
      });

      logger.info(`📊 ${adminsComToken.length}/${adminsSnapshot.size} admins com token FCM`);

      // Notificar cada admin
      const promises = adminsComToken.map((adminDoc) =>
        sendPushNotification(
          adminDoc.id,
          "👋 Novo Membro!",
          `${novoUsuario.nome} acabou de se cadastrar no app`,
          "novo_membro",
          userId
        )
      );

      await Promise.all(promises);
    } catch (error) {
      logger.error("❌ Erro ao notificar novo membro:", error);
    }
  }
);

/**
 * TRIGGER: Comentário em Foto
 * Notifica dono da foto quando recebe comentário
 */
export const onComentarioFotoCreated = onDocumentCreated(
  "fotos_album/{fotoId}/comentarios/{comentarioId}",
  async (event) => {
    const comentario = event.data?.data();
    const fotoId = event.params.fotoId;

    if (!comentario || comentario.deletado) return;

    logger.info(`💬 Novo comentário na foto ${fotoId}`);

    try {
      // Buscar dados da foto
      const fotoDoc = await db.collection("fotos_album").doc(fotoId).get();
      const foto = fotoDoc.data();

      if (!foto) {
        logger.warn(`Foto ${fotoId} não encontrada`);
        return;
      }

      // Não notificar se o comentário é do próprio dono
      if (comentario.usuarioId === foto.criadoPor) {
        logger.info("Comentário do próprio dono, não notificar");
        return;
      }

      // Notificar dono da foto
      await sendPushNotification(
        foto.criadoPor,
        "💬 Novo Comentário",
        `${comentario.usuarioNome} comentou na foto de ${foto.pessoaNome}`,
        "comentario_foto",
        fotoId
      );
    } catch (error) {
      logger.error("❌ Erro ao notificar comentário:", error);
    }
  }
);

/**
 * TRIGGER: Reação em Recado
 * Notifica autor quando recado recebe nova reação
 */
export const onReacaoRecadoCreated = onDocumentUpdated(
  "recados/{recadoId}",
  async (event) => {
    const before = event.data?.before?.data();
    const after = event.data?.after?.data();

    if (!before || !after) return;

    // Detectar se houve nova reação (apoiosFamiliares é array)
    const apoiosAntes = (before.apoiosFamiliares as string[] || []).length;
    const apoiosDepois = (after.apoiosFamiliares as string[] || []).length;

    if (apoiosDepois > apoiosAntes) {
      logger.info(`❤️ Nova reação no recado ${event.params.recadoId}`);

      try {
        // Encontrar quem deu a reação
        const apoiosAntesSet = new Set(before.apoiosFamiliares as string[] || []);
        const novosApoios = (after.apoiosFamiliares as string[] || []).filter(
          (userId) => !apoiosAntesSet.has(userId)
        );

        if (novosApoios.length === 0) return;

        const userId = novosApoios[0];

        // Não notificar se a reação é do próprio autor
        if (userId === after.autorId) {
          logger.info("Reação do próprio autor, não notificar");
          return;
        }

        // Buscar nome do usuário que reagiu
        const userName = await getUserName(userId);

        // Notificar autor do recado
        await sendPushNotification(
          after.autorId,
          "❤️ Nova Reação",
          `${userName} reagiu ao seu recado`,
          "reacao_recado",
          event.params.recadoId
        );
      } catch (error) {
        logger.error("❌ Erro ao notificar reação:", error);
      }
    }
  }
);

/**
 * TRIGGER: Nova Notícia Familiar
 * Notifica todos os usuários quando uma nova notícia é criada
 */
export const onNoticiaFamiliaCreated = onDocumentCreated(
  "noticias_familia/{noticiaId}",
  async (event) => {
    const noticia = event.data?.data();
    const noticiaId = event.params.noticiaId;

    if (!noticia) return;

    logger.info(`📰 Nova notícia criada: ${noticia.tipo} - ${noticia.titulo}`);

    try {
      // Buscar todos os usuários
      const usuariosSnapshot = await db.collection("usuarios").get();

      // Filtrar apenas usuários com token FCM e que não sejam o autor
      const usuariosComToken = usuariosSnapshot.docs.filter((doc) => {
        const data = doc.data();
        return doc.id !== noticia.autorId && data.fcmToken != null;
      });

      logger.info(`📊 Enviando notícia para ${usuariosComToken.length}/${usuariosSnapshot.size - 1} usuários com token FCM`);

      // Determinar título e corpo da notificação baseado no tipo
      let title = "";
      let body = "";
      let channelType = "default";

      switch (noticia.tipo) {
        case "NOVA_PESSOA":
          title = "👤 Novo Membro na Família!";
          body = noticia.titulo || `${noticia.pessoaRelacionadaNome} foi adicionado(a) à família`;
          channelType = "novo_membro";
          break;

        case "NOVA_FOTO":
          title = "📸 Nova Foto!";
          body = noticia.titulo || `${noticia.autorNome} adicionou uma foto`;
          channelType = "nova_foto";
          break;

        case "NOVO_COMENTARIO":
          title = "💬 Novo Comentário!";
          body = noticia.titulo || `${noticia.autorNome} comentou em uma foto`;
          channelType = "comentario_foto";
          break;

        case "APOIO_FAMILIAR":
          title = "❤️ Nova Reação!";
          body = noticia.titulo || `${noticia.autorNome} reagiu a uma foto`;
          channelType = "reacao";
          break;

        case "CASAMENTO":
          title = "💒 Casamento!";
          body = noticia.titulo || `${noticia.pessoaRelacionadaNome} se casou`;
          channelType = "casamento";
          break;

        case "NASCIMENTO":
          title = "👶 Nascimento!";
          body = noticia.titulo || `${noticia.pessoaRelacionadaNome} nasceu!`;
          channelType = "nascimento";
          break;

        default:
          title = "📰 Nova Atividade";
          body = noticia.titulo || "Há novidades na família";
          break;
      }

      // Enviar notificação para todos os usuários
      const promises = usuariosComToken.map((doc) =>
        sendPushNotification(
          doc.id,
          title,
          body,
          channelType,
          noticiaId
        )
      );

      await Promise.all(promises);

      logger.info(`✅ Notificações de ${noticia.tipo} enviadas`);
    } catch (error) {
      logger.error("❌ Erro ao notificar nova notícia:", error);
    }
  }
);

/**
 * TRIGGER: Eventos Futuros (Scheduled - Diário às 8h)
 * Notifica usuários sobre eventos que acontecerão amanhã (casamentos, nascimentos)
 */
export const onEventosFuturosAgendado = onSchedule(
  {
    schedule: "0 8 * * *",
    timeZone: "America/Sao_Paulo",
  },
  async (event) => {
    const hoje = new Date();
    const amanha = new Date(hoje);
    amanha.setDate(amanha.getDate() + 1);

    const diaAmanha = amanha.getDate();
    const mesAmanha = amanha.getMonth() + 1;

    logger.info(`📅 Verificando eventos para amanhã: ${diaAmanha}/${mesAmanha}`);

    try {
      // Buscar todas as pessoas
      const pessoasSnapshot = await db.collection("pessoas").get();

      const eventosCasamento: any[] = [];
      const eventosNascimento: any[] = [];

      // Filtrar eventos que acontecem amanhã
      pessoasSnapshot.docs.forEach((doc) => {
        const pessoa = doc.data();

        // Verificar casamentos
        if (pessoa.dataCasamento) {
          const dataCasamento = pessoa.dataCasamento.toDate();
          if (dataCasamento.getDate() === diaAmanha && dataCasamento.getMonth() + 1 === mesAmanha) {
            eventosCasamento.push({ id: doc.id, ...pessoa });
          }
        }

        // Verificar nascimentos futuros
        if (pessoa.dataNascimento) {
          const dataNasc = pessoa.dataNascimento.toDate();
          // Se a data é no futuro e é amanhã
          if (dataNasc > hoje && dataNasc.getDate() === diaAmanha && dataNasc.getMonth() + 1 === mesAmanha) {
            eventosNascimento.push({ id: doc.id, ...pessoa });
          }
        }
      });

      if (eventosCasamento.length === 0 && eventosNascimento.length === 0) {
        logger.info("Nenhum evento amanhã");
        return;
      }

      logger.info(`💒 ${eventosCasamento.length} casamento(s) amanhã`);
      logger.info(`👶 ${eventosNascimento.length} nascimento(s) amanhã`);

      // Buscar todos os usuários com token FCM
      const usuariosSnapshot = await db.collection("usuarios").get();
      const usuariosComToken = usuariosSnapshot.docs.filter((doc) => {
        const data = doc.data();
        return data.fcmToken != null;
      });

      logger.info(`📊 ${usuariosComToken.length}/${usuariosSnapshot.size} usuários com token FCM`);

      // Notificar casamentos
      for (const pessoa of eventosCasamento) {
        const promises = usuariosComToken.map((userDoc) =>
          sendPushNotification(
            userDoc.id,
            "💒 Casamento Amanhã!",
            `Amanhã é o casamento de ${pessoa.nome}!`,
            "casamento",
            pessoa.id
          )
        );
        await Promise.all(promises);
      }

      // Notificar nascimentos
      for (const pessoa of eventosNascimento) {
        const promises = usuariosComToken.map((userDoc) =>
          sendPushNotification(
            userDoc.id,
            "👶 Nascimento Amanhã!",
            `${pessoa.nome} deve nascer amanhã!`,
            "nascimento",
            pessoa.id
          )
        );
        await Promise.all(promises);
      }

      logger.info("✅ Notificações de eventos futuros enviadas");
    } catch (error) {
      logger.error("❌ Erro ao processar eventos futuros:", error);
    }
  }
);

