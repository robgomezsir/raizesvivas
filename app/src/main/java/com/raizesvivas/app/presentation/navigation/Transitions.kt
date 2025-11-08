package com.raizesvivas.app.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry

/**
 * Configurações de animação para transições entre telas
 */
object Transitions {
    
    /**
     * Animação de entrada para telas principais
     */
    fun enterTransition(): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300))
    }
    
    /**
     * Animação de saída para telas principais
     */
    fun exitTransition(): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300))
    }
    
    /**
     * Animação de entrada para telas de modal (popup)
     */
    fun modalEnterTransition(): EnterTransition {
        return slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300))
    }
    
    /**
     * Animação de saída para telas de modal
     */
    fun modalExitTransition(): ExitTransition {
        return slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300))
    }
    
    /**
     * Animação de entrada reversa (quando voltando)
     */
    fun popEnterTransition(): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300))
    }
    
    /**
     * Animação de saída reversa (quando voltando)
     */
    fun popExitTransition(): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300))
    }
}

