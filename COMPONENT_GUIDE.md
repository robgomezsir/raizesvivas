# Guia de Uso - Componentes Raízes Vivas

## 📚 Componentes Disponíveis

Este guia documenta todos os componentes reutilizáveis criados para o app Raízes Vivas.

---

## 🎨 Cards

### RaizesVivasCard
Card padrão com elevação consistente.

```kotlin
import com.raizesvivas.app.presentation.components.RaizesVivasCard

RaizesVivasCard {
    Text("Conteúdo do card")
}

// Com elevação customizada
RaizesVivasCard(elevation = RaizesElevation.cardElevated) {
    Text("Card com mais destaque")
}

// Clicável
RaizesVivasCard(onClick = { /* ação */ }) {
    Text("Card clicável")
}
```

### FamiliaZeroCard
Card especial para Família Zero com gradiente.

```kotlin
import com.raizesvivas.app.presentation.components.FamiliaZeroCard

FamiliaZeroCard {
    Text("Família Zero")
    Text("Com gradiente de destaque")
}
```

---

## 👤 Avatares

### PersonAvatar
Avatar com gradiente único baseado no ID.

```kotlin
import com.raizesvivas.app.presentation.components.PersonAvatar

PersonAvatar(
    personId = pessoa.id,
    personName = pessoa.nome,
    size = 48.dp
)
```

---

## 🎭 Backgrounds e Botões

### GradientBackground
Background com gradiente sutil para telas.

```kotlin
import com.raizesvivas.app.presentation.components.GradientBackground

GradientBackground {
    // Conteúdo da tela
    Column {
        Text("Tela com background gradiente")
    }
}
```

### GradientButton
Botão com gradiente para ações primárias.

```kotlin
import com.raizesvivas.app.presentation.components.GradientButton

GradientButton(
    text = "Salvar",
    onClick = { /* ação */ }
)

// Desabilitado
GradientButton(
    text = "Salvar",
    onClick = { /* ação */ },
    enabled = false
)
```

---

## ✨ Animações

### ShimmerCard
Loading com efeito shimmer.

```kotlin
import com.raizesvivas.app.presentation.components.ShimmerCard

if (isLoading) {
    ShimmerCard(height = 100.dp)
} else {
    // Conteúdo real
}
```

### AnimatedCard
Card com animação de entrada.

```kotlin
import com.raizesvivas.app.presentation.components.AnimatedCard

LazyColumn {
    items(lista.size) { index ->
        AnimatedCard(delay = index * 50) {
            RaizesVivasCard {
                Text("Item ${index + 1}")
            }
        }
    }
}
```

---

## 🪟 Superfícies Premium

### GlassmorphicSurface
Surface com efeito glassmorphism.

```kotlin
import com.raizesvivas.app.presentation.components.GlassmorphicSurface

GlassmorphicSurface {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Modal com efeito vidro")
    }
}
```

---

## 📭 Estados Vazios

### EmptyState
Componente genérico para estados vazios.

```kotlin
import com.raizesvivas.app.presentation.components.EmptyState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox

EmptyState(
    icon = Icons.Default.Inbox,
    title = "Nenhum dado",
    description = "Descrição opcional",
    actionText = "Adicionar",
    onActionClick = { /* ação */ }
)
```

### Estados Pré-configurados

```kotlin
import com.raizesvivas.app.presentation.components.EmptyStates

// Sem dados genérico
EmptyStates.NoData(onActionClick = { /* adicionar */ })

// Sem resultados de busca
EmptyStates.NoResults(searchQuery = "João")

// Sem pessoas
EmptyStates.NoPeople(onAddClick = { /* adicionar pessoa */ })

// Sem fotos
EmptyStates.NoPhotos(onAddClick = { /* adicionar foto */ })
```

---

## 🎨 Sistema de Cores

### Cores Principais

```kotlin
MaterialTheme.colorScheme.primary        // Verde-floresta
MaterialTheme.colorScheme.secondary      // Terracota
MaterialTheme.colorScheme.tertiary       // Ametista
```

### Cores Semânticas

```kotlin
import com.raizesvivas.app.presentation.theme.*

Heritage    // Marrom-madeira (herança)
Growth      // Verde-vida (crescimento)
Legacy      // Dourado (legado)
Connection  // Azul-céu (conexões)
```

---

## 📏 Elevações

```kotlin
import com.raizesvivas.app.presentation.theme.RaizesElevation

RaizesElevation.cardSecondary  // 3dp
RaizesElevation.cardDefault    // 6dp
RaizesElevation.cardElevated   // 8dp
RaizesElevation.fab            // 12dp
RaizesElevation.modal          // 16dp
```

---

## 🔤 Tipografia

### Títulos (Playfair Display)

```kotlin
MaterialTheme.typography.displayLarge    // 57sp
MaterialTheme.typography.headlineMedium  // 28sp
```

### Corpo (Inter)

```kotlin
MaterialTheme.typography.bodyLarge   // 16sp
MaterialTheme.typography.bodyMedium  // 14sp
MaterialTheme.typography.labelLarge  // 14sp (botões)
```

---

## ✅ Boas Práticas

### ✅ Fazer
- Usar `RaizesVivasCard` para consistência
- Usar `MaterialTheme.colorScheme` para cores
- Usar `RaizesElevation` para elevações
- Usar `EmptyStates` para estados vazios
- Usar `AnimatedCard` em listas

### ❌ Evitar
- Cores hardcoded (`Color.White`, `Color(0xFF...)`)
- Elevações hardcoded (`4.dp` direto)
- Cards genéricos sem estilo
- Estados vazios sem mensagem amigável

---

## 📱 Exemplos Completos

### Tela com Lista Animada

```kotlin
@Composable
fun MinhaTelaScreen(pessoas: List<Pessoa>) {
    GradientBackground {
        if (pessoas.isEmpty()) {
            EmptyStates.NoPeople(
                onAddClick = { /* navegar para adicionar */ }
            )
        } else {
            LazyColumn {
                items(pessoas.size) { index ->
                    AnimatedCard(delay = index * 50) {
                        RaizesVivasCard(
                            onClick = { /* abrir detalhes */ }
                        ) {
                            Row {
                                PersonAvatar(
                                    personId = pessoas[index].id,
                                    personName = pessoas[index].nome
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(pessoas[index].nome)
                            }
                        }
                    }
                }
            }
        }
    }
}
```

### Modal Premium

```kotlin
@Composable
fun ModalPremium(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        GlassmorphicSurface {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Título do Modal",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))
                Text("Conteúdo do modal")
                Spacer(Modifier.height(24.dp))
                GradientButton(
                    text = "Confirmar",
                    onClick = onDismiss
                )
            }
        }
    }
}
```

---

**Última atualização**: 2025-11-24
