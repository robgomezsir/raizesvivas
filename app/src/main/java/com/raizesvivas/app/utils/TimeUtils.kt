package com.raizesvivas.app.utils

import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Utilitário para formatação de timestamps relativos
 * Converte datas em formato legível (ex: "há 2 horas", "há 3 dias")
 */
object TimeUtils {
    
    /**
     * Formata um timestamp em formato relativo
     * @param date Data a ser formatada
     * @return String formatada (ex: "há 2h", "há 3d", "agora")
     */
    fun formatRelativeTime(date: Date): String {
        val now = Date()
        val diffInMillis = now.time - date.time
        
        // Se for no futuro ou muito recente (menos de 1 minuto), retorna "agora"
        if (diffInMillis < TimeUnit.MINUTES.toMillis(1)) {
            return "agora"
        }
        
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)
        val weeks = days / 7
        val months = days / 30
        val years = days / 365
        
        return when {
            years > 0 -> if (years == 1L) "há 1 ano" else "há $years anos"
            months > 0 -> if (months == 1L) "há 1 mês" else "há $months meses"
            weeks > 0 -> if (weeks == 1L) "há 1 semana" else "há $weeks semanas"
            days > 0 -> if (days == 1L) "há 1 dia" else "há $days dias"
            hours > 0 -> if (hours == 1L) "há 1h" else "há ${hours}h"
            minutes > 0 -> if (minutes == 1L) "há 1 min" else "há ${minutes} min"
            else -> "agora"
        }
    }
    
    /**
     * Formata um timestamp em formato relativo curto
     * @param date Data a ser formatada
     * @return String formatada de forma abreviada (ex: "2h", "3d", "agora")
     */
    fun formatRelativeTimeShort(date: Date): String {
        val now = Date()
        val diffInMillis = now.time - date.time
        
        if (diffInMillis < TimeUnit.MINUTES.toMillis(1)) {
            return "agora"
        }
        
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)
        val weeks = days / 7
        val months = days / 30
        val years = days / 365
        
        return when {
            years > 0 -> "${years}a"
            months > 0 -> "${months}m"
            weeks > 0 -> "${weeks}sem"
            days > 0 -> "${days}d"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}min"
            else -> "agora"
        }
    }
}
