package net.discdd.k9.onboarding.ui.login

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.discdd.k9.onboarding.ui.login.LoginContract.Effect
import net.discdd.k9.onboarding.ui.login.LoginContract.Event
import net.discdd.k9.onboarding.util.showToast

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel,
    onPendingState: () -> Unit,
    onFinish: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event(Event.CheckAuthState)
    }

    val context = LocalContext.current
    LaunchedEffect(viewModel.effectFlow) {
        viewModel.effectFlow.collect { effect ->
            Log.d("k9", "in effect: $effect")
            when (effect) {
                Effect.OnPendingState -> onPendingState()
                is Effect.OnLoggedInState -> onFinish(effect.accountUuid.value)
                is Effect.OnError -> {
                    showToast(context, effect.error.message)
                }
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
