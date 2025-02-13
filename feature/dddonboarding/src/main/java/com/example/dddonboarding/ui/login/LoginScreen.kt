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
    onFinish: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event(Event.CheckAuthState)
    }

    LaunchedEffect(viewModel.effectFlow) {
        viewModel.effectFlow.collect { effect ->
            Log.d("k9", "in effect: $effect")
            when (effect) {
                Effect.OnPendingState -> onPendingState()
                is Effect.OnLoggedInState -> onFinish(effect.accountUuid.value)
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
