package com.raizesvivas.app.presentation.screens.privacy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

/**
 * Tela de Contrato de Privacidade e Termos de Uso (LGPD)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoliticaPrivacidadeScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contrato de Privacidade") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "CONTRATO DE PRIVACIDADE E TERMOS DE USO DE DADOS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            PrivacySection(
                title = "1. CONTROLADOR DOS DADOS",
                content = "O aplicativo Raízes Vivas é o controlador dos dados pessoais. O objetivo do aplicativo é o gerenciamento e preservação de histórias familiares através de árvores genealógicas digitais."
            )

            PrivacySection(
                title = "2. DADOS COLETADOS",
                content = buildAnnotatedString {
                    append("Para o funcionamento correto do sistema, coletamos e processamos os seguintes dados:\n\n")
                    
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("• Dados de Registro: ") }
                    append("Nome completo, e-mail e senha (criptografada).\n")
                    
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("• Dados Genealógicos: ") }
                    append("Informações sobre você e seus familiares (Nomes, datas de nascimento/falecimento, locais, parentesco, biografias).\n")
                    
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("• Mídia: ") }
                    append("Fotos e documentos carregados para os álbuns de família.\n")
                    
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("• Biometria: ") }
                    append("Utilizada exclusivamente no dispositivo local para autenticação rápida. Não armazenamos impressão digital ou reconhecimento facial em nossos servidores.\n")
                    
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("• Comunicações: ") }
                    append("Mensagens trocadas no chat interno entre membros da família.")
                }
            )

            PrivacySection(
                title = "3. FINALIDADE DO TRATAMENTO",
                content = buildAnnotatedString {
                    append("Os dados são utilizados estritamente para:\n\n")
                    append("• Construção visual da árvore genealógica.\n")
                    append("• Sincronização de dados entre membros da mesma família.\n")
                    append("• Notificações de aniversários e eventos importantes.\n")
                    append("• Autenticação e segurança da conta.")
                }
            )

            PrivacySection(
                title = "4. COMPARTILHAMENTO DE DADOS",
                content = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("• Grupo Familiar: ") }
                    append("Os dados cadastrados na árvore são visíveis para outros membros convidados para a mesma família.\n")
                    
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("• Terceiros: ") }
                    append("Não vendemos nem compartilhamos dados pessoais com anunciantes ou terceiros desconhecidos.\n")
                    
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("• Armazenamento: ") }
                    append("Utilizamos serviços em nuvem (Firebase/Google Cloud) com criptografia para armazenar os dados com segurança.")
                }
            )

            PrivacySection(
                title = "5. SEUS DIREITOS (LGPD)",
                content = buildAnnotatedString {
                    append("Você tem direito a:\n\n")
                    append("• Confirmar a existência de tratamento de dados.\n")
                    append("• Acessar seus dados a qualquer momento.\n")
                    append("• Corrigir dados incompletos ou desatualizados.\n")
                    append("• Solicitar a exclusão de sua conta e dados pessoais (conforme opção \"Excluir Conta\" nas configurações).")
                }
            )

            PrivacySection(
                title = "6. SEGURANÇA",
                content = "Adotamos medidas técnicas (criptografia, HTTPS) e administrativas para proteger seus dados contra acessos não autorizados."
            )

            PrivacySection(
                title = "7. CONSENTIMENTO",
                content = "Ao utilizar o Raízes Vivas, você concorda com este tratamento de dados para os fins genealógicos descritos."
            )
            
            // Espaço extra no fim para evitar corte em telas pequenas
            Text(
                text = "Versão 1.0 - Dezembro/2025",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
            )
        }
    }
}

@Composable
fun PrivacySection(
    title: String,
    content: Any
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
    
    when (content) {
        is String -> {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        is androidx.compose.ui.text.AnnotatedString -> {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
