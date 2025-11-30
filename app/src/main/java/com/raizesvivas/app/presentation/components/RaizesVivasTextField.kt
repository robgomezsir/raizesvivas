package com.raizesvivas.app.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RaizesVivasTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    hideBorder: Boolean = false,
    isSearchExpanded: Boolean = false  // Novo parâmetro para indicar se a pesquisa está expandida
) {
    // Calcular textStyle - apenas para barra de pesquisa, outros usam padrão Material 3
    val textStyle = if (singleLine && label.isEmpty()) {
        // Para barra de pesquisa: fonte menor, lineHeight igual ao fontSize para alinhar acima do centro
        TextStyle(
            fontSize = 14.sp,
            lineHeight = 14.sp  // LineHeight igual ao fontSize para alinhar acima do centro
        )
    } else {
        // Padrão Material 3 para todos os outros casos
        MaterialTheme.typography.bodyLarge
    }
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .then(
                // Ajustes customizados apenas para barra de pesquisa
                if (singleLine && label.isEmpty()) {
                    if (isSearchExpanded) {
                        // Quando expandido, altura maior para mostrar texto completo
                        Modifier
                            .heightIn(min = 64.dp, max = 72.dp)
                            .wrapContentHeight(Alignment.Top)
                    } else {
                        // Altura padrão quando contraído
                        Modifier
                            .heightIn(min = 48.dp, max = 56.dp)
                            .wrapContentHeight(Alignment.Top)
                    }
                } else {
                    // Padrão Material 3 - sem ajustes de altura ou alinhamento
                    Modifier
                }
            ),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        placeholder = placeholder,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        isError = isError,
        supportingText = supportingText,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        shape = RoundedCornerShape(16.dp),
        textStyle = textStyle,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            errorContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
            
            focusedBorderColor = if (hideBorder) Color.Transparent else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (hideBorder) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}
