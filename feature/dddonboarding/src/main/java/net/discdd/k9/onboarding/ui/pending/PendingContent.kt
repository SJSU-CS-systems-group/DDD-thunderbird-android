package net.discdd.k9.onboarding.ui.pending

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
    whoAmI: () -> Unit,
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
                var showDialog = remember { mutableStateOf(false) }

                if (showDialog.value) DontAbortDialog(showDialog)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),

                ) {
                    item {
                        TextDisplaySmall(
                            text = "Waiting for server response",
                            textAlign = TextAlign.Center,
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
                                onClick = {
                                    showDialog.value = true
                                    // queue up a whoami request in case something is stuck
                                    whoAmI()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DontAbortDialog(showDialog: MutableState<Boolean>) {
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        confirmButton = {
            Button(
                onClick = {
                    showDialog.value = false
                },
            ) {
                Text("OK")
            }
        },
        title = { Text("Cannot abort pending operation") },
        text = {
            Text(
                """
                    We are waiting for a response from the server.
                    We cannot do anything until we have heard back.

                    If you really want to abort, you can clear the storage of the DDD mail app using Android settings.
                """.trimIndent(),
            )
        },
    )
}
