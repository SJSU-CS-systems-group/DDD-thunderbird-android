package net.discdd.k9.onboarding.ui.pending

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import net.discdd.app.k9.common.ControlAdu
import net.discdd.k9.onboarding.repository.AuthRepository
import net.discdd.k9.onboarding.repository.AuthRepository.AuthState
import net.discdd.k9.onboarding.ui.pending.PendingContract.Effect

class PendingViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _effectFlow = MutableSharedFlow<Effect>(replay = 1)
    val effectFlow: SharedFlow<Effect> = _effectFlow.asSharedFlow()

    private fun refreshScreen() {
        // only recheck login if we aren't pending
        if (authRepository.getState().first != AuthState.PENDING) {
            Log.d("k9", "navigate login")
            viewModelScope.coroutineContext.cancelChildren()
            viewModelScope.launch {
                _effectFlow.emit(Effect.OnRedoLoginState)
            }
        }
    }

    fun whoAmI() {
        authRepository.insertAdu(ControlAdu.WhoAmIControlAdu())
    }

    fun checkState() {
        refreshScreen()
    }
}
