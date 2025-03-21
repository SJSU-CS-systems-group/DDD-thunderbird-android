package net.discdd.k9.onboarding.ui.pending

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PendingScreen(
    onRedoLogin: () -> Unit,
    modifier: Modifier = Modifier
){
    Log.d("DDDOnboarding", "In pending")
    PendingContent(
        onRedoLogin = onRedoLogin,
        modifier = modifier
    )
}
