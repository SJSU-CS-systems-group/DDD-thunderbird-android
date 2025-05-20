package net.discdd.k9.onboarding.ui.pending

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.k9mail.core.ui.compose.designsystem.atom.Surface
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonFilledTonal
import app.k9mail.core.ui.compose.designsystem.atom.text.TextDisplayMedium
import app.k9mail.core.ui.compose.designsystem.template.ResponsiveContent

@Composable
internal fun PendingContent(
    onRedoLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
    ) {
        ResponsiveContent {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),

            ) {
                item {
                    TextDisplayMedium(text = "Waiting for server response")
                    TextDisplayMedium(text = "Status = PENDING")
                }
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        ButtonFilledTonal(
                            text = "Redo Login",
                            onClick = onRedoLoginClick,
                        )
                    }
                }
            }
        }
    }
}
