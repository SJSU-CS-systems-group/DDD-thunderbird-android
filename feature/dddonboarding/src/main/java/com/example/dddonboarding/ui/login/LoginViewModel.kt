package com.example.dddonboarding.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.k9mail.core.ui.compose.common.mvi.BaseViewModel
import com.example.dddonboarding.model.LoginAdu
import com.example.dddonboarding.model.RegisterAdu
import com.example.dddonboarding.repository.AuthRepository
import com.example.dddonboarding.repository.AuthRepository.AuthState
import com.example.dddonboarding.ui.login.LoginContract.State
import com.example.dddonboarding.ui.login.LoginContract.Event
import com.example.dddonboarding.ui.login.LoginContract.Effect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    initialState: State = State(),
    private val authRepository: AuthRepository
): ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()
    private val _effectFlow = MutableSharedFlow<Effect>(replay = 1)
    val effectFlow: SharedFlow<Effect> = _effectFlow.asSharedFlow()

    fun event(event: Event) {
        when (event){
            is Event.EmailAddressChanged -> setEmailAddress(event.emailAddress)
            is Event.PasswordChanged -> setPassword(event.password)
            is Event.OnClickLogin -> login(email=event.emailAddress, password = event.password)
            Event.CheckAuthState  -> checkAuthState()
        }
    }

    private fun checkAuthState() {
        val (state, ackAdu) = authRepository.getState()
        if (state == AuthState.PENDING){
            Log.d("DDDOnboarding", "state is pending")
            //emitEffect(Effect.OnPendingState)
            viewModelScope.launch {
                Log.d("DDDOnboarding", "emitting")
                _effectFlow.emit(Effect.OnPendingState)
            }
        } else if (state == AuthState.LOGGED_IN) {
            Log.d("DDDOnboarding", "state is logged in")
            // create account
            //emitEffect(Effect.OnLoggedInState)
        } else {
            Log.d("DDDOnboarding", "state is logged out")
        }
    }

    private fun setEmailAddress(email: String) {
        _state.update {
            it.copy (
                emailAddress = it.emailAddress.updateValue(email)
            )
        }
    }

    private fun setPassword(password: String) {
        _state.update {
            it.copy(
                password = it.password.updateValue(password)
            )
        }
    }

    private fun login(email: String, password: String) {
        authRepository.insertAdu(LoginAdu(email=email, password=password))
        checkAuthState()
    }
}
