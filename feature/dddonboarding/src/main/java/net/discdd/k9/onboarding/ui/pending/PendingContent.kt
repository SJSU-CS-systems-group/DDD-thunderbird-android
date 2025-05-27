package net.discdd.k9.onboarding.ui.pending

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.k9mail.core.ui.compose.designsystem.atom.Surface
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonFilledTonal
import app.k9mail.core.ui.compose.designsystem.atom.text.TextDisplayMedium
import app.k9mail.core.ui.compose.designsystem.template.ResponsiveContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PendingContent(
    onRedoLoginClick: () -> Unit,
    viewModel: PendingViewModel,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
    ) {
        var refreshing = remember { mutableStateOf(false) }

        ResponsiveContent {
            PullToRefreshBox(
                onRefresh = {
                    Log.d("DDDOnboarding", "refreshing")
                    viewModel.checkState()
                },
                isRefreshing = refreshing.value
            ) {

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
}
