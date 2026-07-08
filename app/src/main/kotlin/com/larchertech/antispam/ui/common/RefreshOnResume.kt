package com.larchertech.antispam.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * Contador que incrementa a cada ON_RESUME. Use como chave de `remember(...)` pra recalcular
 * status de permissões/papéis do sistema quando o usuário volta de uma tela de Configurações.
 */
@Composable
fun rememberRefreshOnResume(): State<Int> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val counter = remember { mutableIntStateOf(0) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) counter.intValue++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return counter
}
