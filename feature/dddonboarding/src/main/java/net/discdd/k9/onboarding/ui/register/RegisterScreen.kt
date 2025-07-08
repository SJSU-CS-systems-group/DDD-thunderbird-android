package net.discdd.k9.onboarding.ui.register

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import net.discdd.k9.onboarding.navigation.navigateToLogin
import net.discdd.k9.onboarding.ui.register.RegisterContract.Effect

@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: RegisterViewModel,
    onPendingState: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effectFlow) {
        viewModel.effectFlow.collect { effect ->
            when (effect) {
                Effect.OnPendingState -> onPendingState()
                Effect.OnLoggedInState -> onPendingState()
                Effect.OnErrorState -> navController.navigateToLogin()
            }
        }
    }

    RegisterContent(
        state = state.value,
        onEvent = { viewModel.event(it) },
        modifier = modifier,
        navController = navController,
    )
}
