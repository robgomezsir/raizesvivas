package com.raizesvivas.app.domain.model

import java.util.Date

/**
 * Enum para estados civis
 */
enum class EstadoCivil(val label: String) {
    SOLTEIRO("Solteiro(a)"),
    CASADO("Casado(a)"),
    DIVORCIADO("Divorciado(a)"),
    VIUVO("Viúvo(a)"),
    UNIAO_ESTAVEL("União Estável"),
    SEPARADO("Separado(a)")
}

/**
 * Enum para gênero
 */
enum class Genero(val label: String) {
    MASCULINO("Masculino"),
    FEMININO("Feminino"),
    OUTRO("Outro")
}

/**
 * Modelo de domínio representando uma pessoa na árvore genealógica
 * 
 * Esta classe representa uma pessoa com todos os seus dados e relacionamentos.
 * É a entidade central do sistema.
 */
data class Pessoa(
    val id: String = "",
    val nome: String = "",
    val dataNascimento: Date? = null,
    val dataFalecimento: Date? = null,
    val localNascimento: String? = null,
    val localResidencia: String? = null,
    val profissao: String? = null,
    val biografia: String? = null,
    val estadoCivil: EstadoCivil? = null,   // Estado civil da pessoa
    val genero: Genero? = null,              // Gênero da pessoa
    
    // Relacionamentos
    val pai: String? = null,              // ID do pai
    val mae: String? = null,              // ID da mãe
    val conjugeAtual: String? = null,     // ID do cônjuge atual
    val exConjuges: List<String> = emptyList(), // IDs de ex-cônjuges
    val filhos: List<String> = emptyList(),     // IDs dos filhos
    
    // Metadados
    val fotoUrl: String? = null,
    val criadoPor: String = "",           // UserID de quem criou
    val criadoEm: Date = Date(),
    val modificadoPor: String = "",       // UserID de quem modificou por último
    val modificadoEm: Date = Date(),
    val aprovado: Boolean = false,        // Se foi aprovado por admin
    val versao: Int = 1,                  // Controle de versão para conflitos
    
    // Flags especiais
    val ehFamiliaZero: Boolean = false,   // True apenas para o casal raiz
    val distanciaFamiliaZero: Int = 0,    // Número de graus até a Família Zero
    
    // Relacionamentos com famílias
    val familias: List<String> = emptyList(), // IDs das famílias que esta pessoa pertence
    
    // Informações adicionais
    val tipoFiliacao: TipoFiliacao? = null, // Biológica ou adotiva
    val tipoNascimento: TipoNascimento? = null, // Normal ou gemelar
    val grupoGemelarId: String? = null, // ID compartilhado por gêmeos/trigêmeos
    val ordemNascimento: Int? = null, // Ordem de nascimento (1, 2, 3 para gêmeos)
    val dataCasamento: Date? = null // Data de casamento (para eventos automáticos)
) {
    /**
     * Nome normalizado para busca (sem acentos, minúsculas)
     */
    val nomeNormalizado: String
        get() = nome.lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ã", "a")
            .replace("õ", "o")
            .replace("ç", "c")
    
    /**
     * Verifica se a pessoa está viva
     */
    val estaVivo: Boolean
        get() = dataFalecimento == null
    
    /**
     * Calcula idade atual ou idade ao falecer
     */
    fun calcularIdade(): Int? {
        val dataNasc = dataNascimento ?: return null
        val dataReferencia = dataFalecimento ?: Date()
        
        val diff = dataReferencia.time - dataNasc.time
        val anos = diff / (1000L * 60 * 60 * 24 * 365)
        
        return anos.toInt()
    }
    
    /**
     * Retorna nome formatado para exibição
     */
    fun getNomeExibicao(): String {
        return if (nome.isNotBlank()) nome else "Sem nome"
    }
    
    /**
     * Valida se a pessoa está completa e válida
     */
    fun validar(): ValidationResult {
        return when {
            nome.isBlank() -> ValidationResult(
                isValid = false,
                errors = listOf("Nome é obrigatório")
            )
            nome.length < 3 -> ValidationResult(
                isValid = false,
                errors = listOf("Nome deve ter pelo menos 3 caracteres")
            )
            dataNascimento != null && dataFalecimento != null -> {
                if (dataFalecimento.before(dataNascimento)) {
                    ValidationResult(
                        isValid = false,
                        errors = listOf("Data de falecimento não pode ser anterior à data de nascimento")
                    )
                } else {
                    ValidationResult(isValid = true)
                }
            }
            else -> ValidationResult(isValid = true)
        }
    }
}

/**
 * Resultado de validação
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
)

