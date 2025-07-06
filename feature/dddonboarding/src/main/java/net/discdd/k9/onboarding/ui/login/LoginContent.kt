package net.discdd.k9.onboarding.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.k9mail.core.ui.compose.designsystem.atom.Surface
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonFilledTonal
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonText
import app.k9mail.core.ui.compose.designsystem.atom.text.TextDisplayMedium
import app.k9mail.core.ui.compose.designsystem.atom.text.TextDisplaySmall
import app.k9mail.core.ui.compose.designsystem.molecule.input.EmailAddressInput
import app.k9mail.core.ui.compose.designsystem.molecule.input.PasswordInput
import app.k9mail.core.ui.compose.designsystem.template.LazyColumnWithHeaderFooter
import app.k9mail.core.ui.compose.designsystem.template.ResponsiveContent
import app.k9mail.core.ui.compose.theme2.MainTheme
import net.discdd.k9.onboarding.ui.login.LoginContract.Event
import net.discdd.k9.onboarding.ui.login.LoginContract.State

@Composable
internal fun LoginContent(
    state: State,
    onEvent: (Event) -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(),
) {
    val lastError = viewModel.lastError.collectAsState()
    Surface(
        modifier = modifier,
    ) {
        ResponsiveContent {
            LazyColumnWithHeaderFooter(
                modifier = Modifier.fillMaxSize(),
                header = {
                    TextDisplayMedium(
                        text = "Login Screen",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 50.dp),
                        textAlign = TextAlign.Center,
                    )
                },
                footer = {
                    LoginFooter(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = MainTheme.spacings.quadruple),
                        onRegisterClick = onRegisterClick,
                    )
                },
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                val error = lastError.value
                if (error != null) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            TextDisplaySmall(text = error, color = MainTheme.colors.error)
                        }
                    }
                }
                item { LoginInputs(state = state, onEvent = onEvent) }
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        ButtonFilledTonal(
                            text = "Login",
                            onClick = {
                                onEvent(Event.OnClickLogin(state.emailAddress.value, state.password.value))
                            },
                            enabled = state.validEmail && state.validPassword,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginFooter(
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(),
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        ButtonText(text = "Go register", onClick = {
            viewModel.clearLastError()
            onRegisterClick()
        })
    }
}

@Composable
private fun LoginInputs(
    state: State,
    onEvent: (Event) -> Unit,
) {
    if (!state.validEmail) {
        Text(
            text = "Email must have the form name@domain",
            color = MainTheme.colors.error,
        )
    }

    EmailAddressInput(
        emailAddress = state.emailAddress.value,
        onEmailAddressChange = { onEvent(Event.EmailAddressChanged(it)) },
    )

    if (!state.validPassword) {
        Text(
            text = "Password must be at least 8 characters long",
            color = MainTheme.colors.error,
        )
    }
    PasswordInput(
        password = state.password.value,
        onPasswordChange = { onEvent(Event.PasswordChanged(it)) },
    )
}
