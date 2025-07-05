package net.discdd.k9.onboarding.ui.pending

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.discdd.app.k9.common.ControlAdu
import net.discdd.k9.onboarding.repository.AuthRepository
import net.discdd.k9.onboarding.repository.AuthRepository.AuthState
import net.discdd.k9.onboarding.ui.pending.PendingContract.Effect

class PendingViewModel(
    private val authRepository: AuthRepository,
) : ViewModel(), AuthRepository.AuthRepositoryListener {
    private val _effectFlow = MutableSharedFlow<Effect>(replay = 1)
    val effectFlow: SharedFlow<Effect> = _effectFlow.asSharedFlow()

    private fun refreshScreen() {
        viewModelScope.launch {
        // only recheck login if we aren't pending
        if (authRepository.getState().first != AuthState.PENDING) {
            Log.d("k9", "navigate login")
            viewModelScope.coroutineContext.cancelChildren()
            viewModelScope.launch {
                _effectFlow.emit(Effect.OnRedoLoginState)
            }
        }
    }
    }

    fun whoAmI() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Log.d("k9", "whoAmI called")
            }
            authRepository.insertAdu(ControlAdu.WhoAmIControlAdu())
        }
    }

    fun checkState() {
        refreshScreen()
    }

    fun monitorAuthState() {
        authRepository.authRepositoryListener = this
    }

    fun unMonitorAuthState() {
        val currentListener = authRepository.authRepositoryListener
        if (currentListener == null || currentListener == this) authRepository.authRepositoryListener = null
    }

    override fun onAuthStateChanged() {
        checkState()
    }
}
