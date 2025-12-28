/**
 * Script para validar apenas os arquivos JSON importantes do projeto
 * Ignora node_modules e schemas
 */

const fs = require('fs');
const path = require('path');

// Lista de arquivos JSON importantes para validar
const filesToValidate = [
    'firebase.json',
    'firestore.indexes.json',
    'google-services.json',
    'functions/package.json',
    'functions/tsconfig.json',
    'scripts/package.json'
];

let hasErrors = false;

console.log('🔍 Validando arquivos JSON do projeto...\n');

filesToValidate.forEach(file => {
    const filePath = path.join(__dirname, file);

    if (!fs.existsSync(filePath)) {
        console.log(`⚠️  Arquivo não encontrado: ${file}`);
        return;
    }

    try {
        const content = fs.readFileSync(filePath, 'utf8');
        JSON.parse(content);
        console.log(`✅ ${file} - Válido`);
    } catch (error) {
        console.log(`❌ ${file} - ERRO: ${error.message}`);
        hasErrors = true;
    }
});

console.log('\n' + '='.repeat(50));

if (hasErrors) {
    console.log('❌ Validação falhou! Corrija os erros acima.');
    process.exit(1);
} else {
    console.log('✅ Todos os arquivos JSON do projeto são válidos!');
    process.exit(0);
}
