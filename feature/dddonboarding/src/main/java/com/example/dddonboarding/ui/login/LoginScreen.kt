package com.example.dddonboarding.ui.login

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import app.k9mail.core.ui.compose.common.mvi.observe
import app.k9mail.core.ui.compose.common.mvi.observeWithoutEffect
import com.example.dddonboarding.ui.login.LoginContract.ViewModel
import com.example.dddonboarding.ui.login.LoginContract.Event
import com.example.dddonboarding.ui.login.LoginContract.Effect
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    viewModel: ViewModel,
    onPendingState: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (state, dispatch) = viewModel.observe { effect ->
        when (effect) {
            Effect.OnPendingState -> onPendingState()
            Effect.OnLoggedInState -> onPendingState()
        }
    }

    LaunchedEffect(Unit) {
        dispatch(Event.CheckAuthState)
    }

    LoginContent(
        onRegisterClick = onRegisterClick,
        state = state.value,
        onEvent = { dispatch(it) },
        modifier = modifier
    )
}
