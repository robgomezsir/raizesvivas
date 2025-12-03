package com.raizesvivas.app.utils

import java.util.Locale

/**
 * Utilitário para formatação de textos seguindo regras de escrita
 * Capitaliza textos respeitando pontuação, vírgulas e regras gramaticais
 */
object TextUtils {
    
    /**
     * Capitaliza um texto seguindo as regras de escrita:
     * - Primeira letra da primeira palavra em maiúscula
     * - Primeira letra após ponto, exclamação ou interrogação em maiúscula
     * - Mantém vírgulas e pontuação
     * - Preserva espaços e quebras de linha
     * 
     * @param texto Texto a ser capitalizado
     * @return Texto capitalizado seguindo as regras de escrita
     */
    fun capitalizarTexto(texto: String): String {
        if (texto.isBlank()) return texto
        
        val textoTrimmed = texto.trim()
        if (textoTrimmed.isEmpty()) return texto
        
        val builder = StringBuilder()
        var proximaLetraMaiuscula = true
        var i = 0
        
        while (i < textoTrimmed.length) {
            val char = textoTrimmed[i]
            
            when {
                // Espaços, tabs, quebras de linha - manter e continuar
                char.isWhitespace() -> {
                    builder.append(char)
                    // Se for quebra de linha, considerar nova frase
                    if (char == '\n') {
                        proximaLetraMaiuscula = true
                    }
                }
                
                // Pontuação que indica fim de frase - próxima letra deve ser maiúscula
                char == '.' || char == '!' || char == '?' -> {
                    builder.append(char)
                    proximaLetraMaiuscula = true
                }
                
                // Dois pontos - pode iniciar nova frase em alguns contextos
                char == ':' -> {
                    builder.append(char)
                    // Se seguido de espaço, próxima letra pode ser maiúscula
                    if (i + 1 < textoTrimmed.length && textoTrimmed[i + 1].isWhitespace()) {
                        proximaLetraMaiuscula = true
                    }
                }
                
                // Vírgula, ponto e vírgula - manter minúscula após
                char == ',' || char == ';' -> {
                    builder.append(char)
                    proximaLetraMaiuscula = false
                }
                
                // Letra - capitalizar se necessário
                char.isLetter() -> {
                    if (proximaLetraMaiuscula) {
                        builder.append(char.uppercaseChar())
                        proximaLetraMaiuscula = false
                    } else {
                        builder.append(char.lowercaseChar())
                    }
                }
                
                // Números, símbolos, etc - manter como está
                else -> {
                    builder.append(char)
                }
            }
            
            i++
        }
        
        return builder.toString()
    }
    
    /**
     * Capitaliza apenas a primeira letra do texto, mantendo o resto como está
     * Útil para textos que já estão formatados mas precisam apenas da primeira letra maiúscula
     * 
     * @param texto Texto a ser capitalizado
     * @return Texto com primeira letra em maiúscula
     */
    fun capitalizarPrimeiraLetra(texto: String): String {
        if (texto.isBlank()) return texto
        
        return texto.trim().let { trimmed ->
            if (trimmed.isEmpty()) texto
            else trimmed[0].uppercaseChar() + trimmed.substring(1).lowercase(Locale.getDefault())
        }
    }
}

