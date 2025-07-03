package net.discdd.k9.onboarding.ui.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.discdd.app.k9.common.ControlAdu
import net.discdd.k9.onboarding.repository.AuthRepository
import net.discdd.k9.onboarding.repository.AuthRepository.AuthState
import net.discdd.k9.onboarding.ui.register.RegisterContract.Effect
import net.discdd.k9.onboarding.ui.register.RegisterContract.Event
import net.discdd.k9.onboarding.ui.register.RegisterContract.State

class RegisterViewModel(
    initialState: State = State(),
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()
    private val _effectFlow = MutableSharedFlow<Effect>(replay = 1)
    val effectFlow: SharedFlow<Effect> = _effectFlow.asSharedFlow()

    fun event(event: Event) {
        when (event) {
            is Event.PrefixChanged -> setPrefix(event.prefix)
            is Event.SuffixChanged -> setSuffix(event.suffix)
            is Event.PasswordChanged -> setPassword(event.password)
            is Event.OnClickRegister -> register(
                event.prefix,
                event.suffix,
                password = event.password,
            )
        }
    }

    private fun checkAuthState() {
        val (state, ackAdu) = authRepository.getState()
        if (state == AuthState.PENDING) {
            viewModelScope.launch {
                Log.d("DDDOnboarding", "emitting")
                _effectFlow.emit(Effect.OnPendingState)
            }
        }
    }

    private fun setPrefix(prefix: String) {
        _state.update {
            it.copy(
                prefix = it.prefix.updateValue(prefix),
            )
        }
    }

    private fun setSuffix(suffix: String) {
        _state.update {
            it.copy(
                suffix = it.suffix.updateValue(suffix),
            )
        }
    }

    private fun setPassword(password: String) {
        _state.update {
            it.copy(
                password = it.password.updateValue(password),
            )
        }
    }

    private fun register(
        prefix: String,
        suffix: String,
        password: String,
    ) {
        authRepository.insertAdu(
            ControlAdu.RegisterControlAdu(
                mapOf(
                    Pair("prefix", prefix),
                    Pair("suffix", suffix),
                    Pair("password", password),
                )
            )
        )
        checkAuthState()
    }
}
