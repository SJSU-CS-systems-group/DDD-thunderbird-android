package net.discdd.k9.onboarding.ui.pending

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import app.k9mail.core.ui.compose.designsystem.atom.Surface
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonFilledTonal
import app.k9mail.core.ui.compose.designsystem.atom.text.TextDisplaySmall
import app.k9mail.core.ui.compose.designsystem.template.ResponsiveContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PendingContent(
    refreshState: () -> Unit,
    abortLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
    ) {
        var refreshing = remember { mutableStateOf(false) }

        ResponsiveContent {
            PullToRefreshBox(
                onRefresh = { refreshState() },
                isRefreshing = refreshing.value,
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),

                ) {
                    item {
                        TextDisplaySmall(
                            text = "Waiting for server response",
                            textAlign = TextAlign.Center
                        )
                        Text(text = "Please use the DDD Client to exchange data with DDD transports")
                    }
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            ButtonFilledTonal(
                                text = "Abort Login",
                                onClick = { abortLogin() }
                            )
                        }
                    }
                }
            }
        }
    }
}
