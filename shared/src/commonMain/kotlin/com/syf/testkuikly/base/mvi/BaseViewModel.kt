package com.syf.testkuikly.base.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface MviIntent
interface MviState
interface MviEffect

abstract class BaseViewModel<S : MviState, I : MviIntent, E : MviEffect>(
    initialState: S
) {
    private val _viewState = MutableStateFlow(initialState)
    val viewState: StateFlow<S> = _viewState.asStateFlow()
    val currentState: S get() = _viewState.value

    private val _effect = MutableSharedFlow<E>(extraBufferCapacity = 10)
    val effect: SharedFlow<E> = _effect.asSharedFlow()

    fun sendIntent(intent: I) {
        handleIntent(intent)
    }

    protected abstract fun handleIntent(intent: I)

    protected fun reduce(reducer: S.() -> S) {
        _viewState.value = currentState.reducer()
    }

    protected fun sendEffect(effect: E) {
        _effect.tryEmit(effect)
    }

    protected fun launch(block: suspend CoroutineScope.() -> Unit) {
        CoroutineScope(Dispatchers.Default).launch(block = block)
    }
}
