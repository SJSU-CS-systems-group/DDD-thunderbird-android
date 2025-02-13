package com.example.dddonboarding.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dddonboarding.ui.register.RegisterContent
import com.example.dddonboarding.ui.register.RegisterViewModel
import com.example.dddonboarding.ui.register.RegisterContract.Effect

@Composable
fun RegisterScreen(
    onLoginClick: () -> Unit,
    viewModel: RegisterViewModel,
    onPendingState: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effectFlow) {
        viewModel.effectFlow.collect { effect ->
            when (effect) {
                Effect.OnPendingState -> onPendingState()
                Effect.OnLoggedInState -> onPendingState()
            }
        }
    }

    RegisterContent(
        state = state.value,
        onEvent = { viewModel.event(it) },
        modifier = modifier,
        onLoginClick = onLoginClick
    )
}
