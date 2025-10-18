package com.raizesvivas.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.raizesvivas.core.domain.model.Member
import java.time.format.DateTimeFormatter

/**
 * Componente para exibir card de membro
 * 
 * Mostra informações básicas do membro com foto,
 * nome, parentesco e elemento visual da árvore.
 */
@Composable
fun MemberCard(
    member: Member,
    kinship: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto do membro
            Box(
                modifier = Modifier.size(56.dp)
            ) {
                AsyncImage(
                    model = member.fotoUrl,
                    contentDescription = "Foto de ${member.nomeCompleto}",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                // Elemento da árvore como overlay
                if (member.elementosVisuais.isNotEmpty()) {
                    TreeElementIcon(
                        element = member.elementosVisuais.first(),
                        modifier = Modifier.align(Alignment.BottomEnd),
                        size = 20.dp,
                        showBackground = true
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Informações do membro
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = member.nomeCompleto,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (kinship != null) {
                    Text(
                        text = kinship,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                member.dataNascimento?.let { date ->
                    Text(
                        text = formatDate(date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                member.profissao?.let { profession ->
                    if (profession.isNotBlank()) {
                        Text(
                            text = profession,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Formata data para exibição
 */
private fun formatDate(date: java.time.LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}
