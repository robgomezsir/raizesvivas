package com.raizesvivas.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

/**
 * Barra de pesquisa animada que expande suavemente ao ser ativada.
 *
 * @param query Texto atual da pesquisa
 * @param onQueryChange Callback para mudança de texto
 * @param isSearchActive Se a pesquisa está ativa (expandida)
 * @param onSearchActiveChange Callback para ativar/desativar pesquisa
 * @param placeholder Texto de placeholder
 * @param modifier Modificador
 */
@Composable
fun AnimatedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar..."
) {
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterEnd
    ) {
        // Botão de pesquisa (visível quando inativo)
        androidx.compose.animation.AnimatedVisibility(
            visible = !isSearchActive,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            IconButton(onClick = { onSearchActiveChange(true) }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar"
                )
            }
        }

        // Campo de pesquisa (visível quando ativo) - expande horizontalmente e verticalmente
        androidx.compose.animation.AnimatedVisibility(
            visible = isSearchActive,
            enter = expandHorizontally(
                animationSpec = tween(durationMillis = 300),
                expandFrom = Alignment.End
            ) + expandVertically(
                animationSpec = tween(durationMillis = 300),
                expandFrom = Alignment.Top
            ) + fadeIn(),
            exit = shrinkHorizontally(
                animationSpec = tween(durationMillis = 300),
                shrinkTowards = Alignment.End
            ) + shrinkVertically(
                animationSpec = tween(durationMillis = 300),
                shrinkTowards = Alignment.Top
            ) + fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopStart
            ) {
                RaizesVivasTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    label = "",
                    placeholder = { 
                        Text(
                            text = placeholder,
                            style = TextStyle(fontSize = 14.sp)
                        ) 
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            if (query.isNotEmpty()) {
                                onQueryChange("")
                            } else {
                                onSearchActiveChange(false)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar"
                            )
                        }
                    },
                    hideBorder = true,
                    isSearchExpanded = isSearchActive  // Novo parâmetro para indicar expansão
                )
            }
        }
    }

    // Focar automaticamente quando expandir
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }
}
