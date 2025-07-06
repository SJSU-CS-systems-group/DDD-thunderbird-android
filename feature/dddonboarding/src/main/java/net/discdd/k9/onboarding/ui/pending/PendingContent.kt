package net.discdd.k9.onboarding.ui.pending

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.k9mail.core.ui.compose.designsystem.atom.Surface
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonFilledTonal
import app.k9mail.core.ui.compose.designsystem.atom.text.TextDisplaySmall
import kotlinx.coroutines.launch
import net.discdd.app.k9.common.ControlAdu

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun PendingContent(
    modifier: Modifier = Modifier,
    viewModel: PendingViewModel = viewModel(),
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val onRefresh: () -> Unit = {
        isRefreshing = true
        coroutineScope.launch {
            viewModel.checkState()
            isRefreshing = false
        }
    }
    val refreshingState = rememberPullRefreshState(isRefreshing, onRefresh = onRefresh)
    val showDialog = remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
    ) {
        LaunchedEffect(Unit) {
            viewModel.checkState()
        }
        val message = viewModel.lastAdu.collectAsState(null).value.let { adu ->
            when (adu) {
                is ControlAdu.LoginControlAdu -> "Waiting for ${adu.email()} to login"
                is ControlAdu.RegisterControlAdu -> "Waiting to register ${adu.prefix()}+${adu.suffix()}"
                else -> ""
            }
        }

        if (showDialog.value) DontAbortDialog(showDialog = showDialog)

        Box {
            LazyColumn(
                modifier = Modifier.pullRefresh(refreshingState, enabled = true).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item { TextDisplaySmall(text = "Waiting for server response", textAlign = TextAlign.Center) }
                item {
                    Text(
                        text = "${message}\n\nPlease use the DDD Client to exchange data with DDD transports",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                }
                item {
                    ButtonFilledTonal(
                        text = "Abort Login",
                        onClick = {
                            showDialog.value = true
                            // queue up a whoami request in case something is stuck
                            viewModel.whoAmI()
                        },
                    )
                }
            }
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = refreshingState,
                modifier = Modifier.align(Alignment.Center),
            )
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
