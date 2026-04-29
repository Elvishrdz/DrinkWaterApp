package com.eahm.drinkwaterapp.core.data.confetti

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.module.Module
import org.koin.dsl.module

val confettiModule: Module = module {
    single<ConfettiController> { ConfettiController() }
}

class ConfettiController {

    private var _state = MutableStateFlow(false)
    var state: StateFlow<Boolean> = _state

    fun showConfetti() {
        _state.value = true
    }

    fun onFinished() {
        _state.value = false
    }
}
