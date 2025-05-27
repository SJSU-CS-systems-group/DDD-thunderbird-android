package net.discdd.k9.onboarding.ui.pending

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import net.discdd.k9.onboarding.ui.pending.PendingContract.Effect

@Composable
fun PendingScreen(
    onRedoLoginState: () -> Unit,
    viewModel: PendingViewModel,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        viewModel.checkState()
    }

    LaunchedEffect(viewModel.effectFlow) {
        viewModel.effectFlow.collect { effect ->
            Log.d("k9", "in effect: $effect")
            when (effect) {
                Effect.OnRedoLoginState -> onRedoLoginState()
            }
        }
    }

    Log.d("DDDOnboarding", "In pending")
    PendingContent(
        onRedoLoginClick = { viewModel.redoLogin() },
        viewModel = viewModel,
        modifier = modifier,
    )
}
