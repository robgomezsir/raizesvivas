# Implementação de Visualizações: Lista vs Árvore

## Mudanças Realizadas

### 1. Separação de Componentes de Card

Para atender ao requisito de separar e dar tratamento visual diferenciado aos cards, criamos dois componentes distintos:

*   **[FamiliaCardLista](file:///c:/Users/robgo/raizesvivas/app/src/main/java/com/raizesvivas/app/presentation/screens/familia/FamiliaScreen.kt#2151-2497)**:
    *   **Design Rico**: Uso de cores diferenciadas baseadas no tipo de família (Zero, Monoparental, Anterior, etc.).
    *   **Badges**: Chips visuais para identificar características (Homoafetiva, Residencial, Emocional, etc.).
    *   **Bordas e Sombras**: Maior destaque visual com elevação de 4dp e bordas coloridas quando aplicável.
    *   **Foco**: Visualização detalhada para navegação em lista.

*   **`FamiliaCardHierarquia`**:
    *   **Design Compacto**: Visual mais limpo e minimalista.
    *   **Cores Suaves**: Uso de cores de superfície com menor ênfase para não poluir a visualização em árvore.
    *   **Foco**: Estrutura hierárquica e conexões.

### 2. Toggle de Visualização

Implementamos um mecanismo para alternar entre os modos de visualização:

*   **Enum [ModoVisualizacao](file:///c:/Users/robgo/raizesvivas/app/src/main/java/com/raizesvivas/app/presentation/screens/familia/FamiliaScreen.kt#2653-2657)**: Define os estados `LISTA` e `ARVORE`.
*   **Estado na Screen**: `var modoVisualizacao by rememberSaveable { mutableStateOf(ModoVisualizacao.LISTA) }` mantém a escolha do usuário.
*   **Botão na Toolbar**: Adicionado ícone na TopAppBar para alternar dinamicamente entre os modos.
    *   Ícone `AccountTree` quando em modo Lista (para mudar para Árvore).
    *   Ícone `ViewList` quando em modo Árvore (para mudar para Lista).

### 3. Wrapper Inteligente

O componente [FamiliaCard](file:///c:/Users/robgo/raizesvivas/app/src/main/java/com/raizesvivas/app/presentation/screens/familia/FamiliaScreen.kt#2131-2435) original foi refatorado para atuar como um wrapper:

```kotlin
@Composable
private fun FamiliaCard(...) {
    when (modoVisualizacao) {
        ModoVisualizacao.LISTA -> FamiliaCardLista(...)
        ModoVisualizacao.ARVORE -> FamiliaCardHierarquia(...)
    }
}
```

Isso permitiu manter a lógica da `LazyColumn` inalterada, apenas delegando a renderização para o card apropriado.

## Correções Adicionais

*   **Imports**: Adicionados imports faltantes para `Icons.Filled.ViewList`, `Icons.Filled.AccountTree` e `Icons.Filled.PhotoLibrary`.
*   **Limpeza**: Removidos imports não utilizados e código morto.

## Próximos Passos Sugeridos

*   Validar a experiência de uso com o toggle.
*   Refinar as cores do [FamiliaCardLista](file:///c:/Users/robgo/raizesvivas/app/src/main/java/com/raizesvivas/app/presentation/screens/familia/FamiliaScreen.kt#2151-2497) se necessário, baseado no feedback visual real.
