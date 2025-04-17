package net.discdd.k9.onboarding.ui.pending

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import net.discdd.k9.onboarding.repository.AuthRepository
import net.discdd.k9.onboarding.repository.AuthRepository.AuthState
import net.discdd.k9.onboarding.ui.pending.PendingContract.Event
import net.discdd.k9.onboarding.ui.pending.PendingContract.Effect

import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class PendingViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _effectFlow = MutableSharedFlow<Effect>(replay = 1)
    val effectFlow: SharedFlow<Effect> = _effectFlow.asSharedFlow()
    fun event(event: Event) {
        when (event){
            Event.OnRedoLoginClick -> redoLogin()
            Event.CheckAuthState  -> checkAuthState()
        }
    }

    private fun checkAuthState() {
        val (state, ackAdu) = authRepository.getState()
        Log.d("PendingViewModel", "state " + state)

        if (state == AuthState.LOGGED_OUT) {
            navigateLogIn()
        }
    }

    private fun navigateLogIn() {
        Log.d("k9", "navigate login")
        viewModelScope.coroutineContext.cancelChildren()
        viewModelScope.launch {
            _effectFlow.emit(Effect.OnRedoLoginState)
        }
    }

    private fun redoLogin() {
        authRepository.deleteState()
        authRepository.deleteAuthAdu()
        checkAuthState()
    }
}
