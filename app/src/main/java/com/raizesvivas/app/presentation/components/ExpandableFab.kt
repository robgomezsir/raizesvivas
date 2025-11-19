package com.raizesvivas.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import com.raizesvivas.app.presentation.ui.theme.FabDefaults
import com.raizesvivas.app.presentation.ui.theme.fabContainerColor
import com.raizesvivas.app.presentation.ui.theme.fabContentColor
import com.raizesvivas.app.presentation.ui.theme.fabElevation
import com.raizesvivas.app.presentation.ui.theme.fabSecondaryContainerColor
import com.raizesvivas.app.presentation.ui.theme.fabSecondaryContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class FabAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val containerColor: Color? = null,
    val contentColor: Color? = null
)

@Composable
fun ExpandableFab(
    actions: List<FabAction>,
    modifier: Modifier = Modifier,
    mainContainerColor: Color = MaterialTheme.colorScheme.primary,
    mainContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    spacing: Int = 12
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "fab-rotation"
    )

    Column(
        modifier = modifier
            .padding(end = 4.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(spacing.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(spacing.dp)
            ) {
                actions.forEach { action ->
                    SmallFloatingActionButton(
                        onClick = {
                            action.onClick()
                            expanded = false
                        },
                        containerColor = action.containerColor ?: fabSecondaryContainerColor(),
                        contentColor = action.contentColor ?: fabSecondaryContentColor(),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = action.label,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = if (mainContainerColor == MaterialTheme.colorScheme.primary) {
                fabContainerColor()
            } else {
                mainContainerColor
            },
            contentColor = if (mainContentColor == MaterialTheme.colorScheme.onPrimary) {
                fabContentColor()
            } else {
                mainContentColor
            },
            elevation = fabElevation(),
            content = {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = if (expanded) "Fechar ações" else "Abrir ações",
                    modifier = Modifier.rotate(rotation)
                )
            }
        )
    }
}


