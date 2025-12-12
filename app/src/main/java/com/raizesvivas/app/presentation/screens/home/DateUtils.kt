package com.raizesvivas.app.presentation.screens.home

import java.util.Calendar
import java.util.Date

/**
 * Formata data para exibição em eventos
 */
fun formatarData(data: Date): String {
    val calendar = Calendar.getInstance().apply { time = data }
    val dia = calendar.get(Calendar.DAY_OF_MONTH)
    val mes = when (calendar.get(Calendar.MONTH)) {
        0 -> "Jan"
        1 -> "Fev"
        2 -> "Mar"
        3 -> "Abr"
        4 -> "Mai"
        5 -> "Jun"
        6 -> "Jul"
        7 -> "Ago"
        8 -> "Set"
        9 -> "Out"
        10 -> "Nov"
        11 -> "Dez"
        else -> ""
    }
    return "$dia de $mes."
}
