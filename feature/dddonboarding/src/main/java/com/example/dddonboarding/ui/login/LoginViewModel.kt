package com.example.dddonboarding.ui.login

import android.util.Log
import app.k9mail.core.ui.compose.common.mvi.BaseViewModel
import com.example.dddonboarding.model.LoginAdu
import com.example.dddonboarding.model.RegisterAdu
import com.example.dddonboarding.repository.AuthRepository
import com.example.dddonboarding.repository.AuthRepository.AuthState
import com.example.dddonboarding.ui.login.LoginContract.State
import com.example.dddonboarding.ui.login.LoginContract.Event
import com.example.dddonboarding.ui.login.LoginContract.Effect

internal class LoginViewModel(
    initialState: State = State(),
    private val authRepository: AuthRepository
): BaseViewModel<State, Event, Effect>(initialState), LoginContract.ViewModel {

    override fun event(event: Event) {
        when (event){
            is Event.EmailAddressChanged -> setEmailAddress(event.emailAddress)
            is Event.PasswordChanged -> setPassword(event.password)
            is Event.OnClickLogin -> Unit
            Event.CheckAuthState  -> checkAuthState()
        }
    }

    private fun checkAuthState() {
        val inserted = authRepository.insertAdu(LoginAdu(email="manas@ravlyk.com", password="password"))
        if (inserted) {
            Log.d("DDDOnboarding", "inserted adu")
        } else {
            Log.d("DDDOnboarding", "adu insertion failed")
        }
        val (state, ackAdu) = authRepository.getState()
        if (state == AuthState.PENDING){
            Log.d("DDDOnboarding", "state is pending")
            emitEffect(Effect.OnPendingState)
        } else if (state == AuthState.LOGGED_IN) {
            Log.d("DDDOnboarding", "state is logged in")
            // create account
            //emitEffect(Effect.OnLoggedInState)
        } else {
            Log.d("DDDOnboarding", "state is logged out")
        }
    }

    private fun setEmailAddress(email: String) {
        updateState {
            it.copy (
                emailAddress = it.emailAddress.updateValue(email)
            )
        }
    }

    private fun setPassword(password: String) {
        updateState {
            it.copy(
                password = it.password.updateValue(password)
            )
        }
    }
}
