# Configuração de Inspeções do IntelliJ IDEA
# Este arquivo contém as instruções para configurar o IntelliJ IDEA
# para ignorar erros de JSON em node_modules e schemas

## Passos para Configurar

### Opção 1: Configuração Manual (Recomendada)

1. Abra o IntelliJ IDEA
2. Vá em **File → Settings** (ou **Ctrl+Alt+S**)
3. Navegue até **Editor → Inspections**
4. No painel esquerdo, expanda **JSON and JSON5**
5. Clique em **Compliance with JSON standard**
6. No painel direito, clique no ícone de **pasta** ao lado de "Scope"
7. Selecione **Custom scope** e clique em **...** para criar um novo escopo
8. Crie um novo escopo chamado "Project Files (excluding node_modules)"
9. Use o seguinte padrão:
   ```
   file:*.json&&!file:**/node_modules//*&&!file:**/schemas//*
   ```
10. Clique em **OK** e aplique as mudanças

### Opção 2: Desabilitar Inspeção para Diretórios Específicos

1. No **Project Explorer**, clique com o botão direito na pasta `functions/node_modules`
2. Selecione **Mark Directory as → Excluded**
3. Repita para `scripts/node_modules`
4. Clique com o botão direito na pasta `app/schemas`
5. Selecione **Mark Directory as → Excluded**

### Opção 3: Desabilitar Inspeção Globalmente (Menos Recomendada)

1. Abra **File → Settings → Editor → Inspections**
2. Desmarque **JSON and JSON5 → Compliance with JSON standard**
3. Clique em **Apply** e **OK**

## Verificação

Após aplicar qualquer uma das opções acima:

1. Clique em **Code → Inspect Code**
2. Verifique que o número de erros de JSON diminuiu significativamente
3. Os arquivos JSON do seu projeto (firebase.json, firestore.indexes.json, etc.) ainda serão validados

## Arquivos JSON do Projeto (Validados)

Estes arquivos continuarão sendo validados:
- ✅ firebase.json
- ✅ firestore.indexes.json
- ✅ functions/package.json
- ✅ functions/tsconfig.json
- ✅ google-services.json
- ✅ scripts/package.json

## Arquivos Excluídos da Validação

Estes arquivos serão ignorados:
- ❌ functions/node_modules/**/*.json (dependências)
- ❌ scripts/node_modules/**/*.json (dependências)
- ❌ app/schemas/**/*.json (schemas gerados automaticamente)
