package net.discdd.k9.onboarding.ui.pending

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import net.discdd.k9.onboarding.ui.pending.PendingContract.Effect
import org.koin.androidx.compose.koinViewModel

@Composable
fun PendingScreen(
    refreshState: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<PendingViewModel>()

    LaunchedEffect(Unit) {
        viewModel.monitorAuthState()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.unMonitorAuthState()
        }
    }

    LaunchedEffect(viewModel.effectFlow) {
        viewModel.effectFlow.collect { effect ->
            Log.d("k9", "in effect: $effect")
            when (effect) {
                Effect.OnRedoLoginState -> refreshState()
            }
        }
    }

    PendingContent(
        modifier = modifier,
    )
}
