package com.example.dddonboarding.ui.login

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.k9mail.core.ui.compose.common.mvi.observe
import app.k9mail.core.ui.compose.common.mvi.observeWithoutEffect
import com.example.dddonboarding.ui.login.LoginContract.Event
import com.example.dddonboarding.ui.login.LoginContract.Effect
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel,
    onPendingState: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    /*val (state, dispatch) = viewModel.observe { effect ->
        when (effect) {
            Effect.OnPendingState -> onPendingState()
            Effect.OnLoggedInState -> onPendingState()
        }
    }
     */
    val state = viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        //dispatch(Event.CheckAuthState)
        viewModel.event(Event.CheckAuthState)
    }

    LaunchedEffect(viewModel.effectFlow) {
        viewModel.effectFlow.collect { effect ->
            when (effect) {
                Effect.OnPendingState -> onPendingState()
                Effect.OnLoggedInState -> onPendingState()
            }
        }
    }

    LoginContent(
        onRegisterClick = onRegisterClick,
        state = state.value,
        onEvent = { viewModel.event(it) },
        modifier = modifier,
    )
}
