package net.discdd.k9.onboarding.ui.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import app.k9mail.core.ui.compose.designsystem.atom.Surface
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonFilledTonal
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonText
import app.k9mail.core.ui.compose.designsystem.atom.text.TextDisplayMedium
import app.k9mail.core.ui.compose.designsystem.atom.textfield.TextFieldOutlined
import app.k9mail.core.ui.compose.designsystem.molecule.input.PasswordInput
import app.k9mail.core.ui.compose.designsystem.template.LazyColumnWithHeaderFooter
import app.k9mail.core.ui.compose.designsystem.template.ResponsiveContent
import app.k9mail.core.ui.compose.theme2.MainTheme
import com.example.dddonboarding.R
import net.discdd.k9.onboarding.navigation.navigateToLogin
import net.discdd.k9.onboarding.ui.register.RegisterContract.Event
import net.discdd.k9.onboarding.ui.register.RegisterContract.State

@Composable
internal fun RegisterContent(
    state: State,
    onEvent: (Event) -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
    ) {
        ResponsiveContent {
            LazyColumnWithHeaderFooter(
                modifier = Modifier.fillMaxSize(),
                header = {
                    TextDisplayMedium(
                        text = "Register Screen",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 50.dp),
                        textAlign = TextAlign.Center,
                    )
                },
                footer = {
                    RegisterFooter(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = MainTheme.spacings.quadruple),
                        onLoginClick = { navController.navigateToLogin() },
                    )
                },
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                item { RegisterInputs(state = state, onEvent = onEvent) }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        ButtonFilledTonal(
                            text = "Register",
                            enabled = state.readyToRegister,
                            onClick = {
                                onEvent(
                                    Event.OnClickRegister(
                                        state.prefix.value,
                                        state.suffix.value,
                                        state.password.value,
                                    ),
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegisterFooter(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        ButtonText(text = "Go login", onClick = onLoginClick)
    }
}

@Composable
private fun RegisterInputs(
    state: State,
    onEvent: (Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.email_address_formation))
        if (!state.validPrefix) {
            Text(
                text = stringResource(R.string.prefix_must_be_between_3_and_8_letters),
                color = MainTheme.colors.error,
            )
        }

        TextFieldOutlined(
            value = state.prefix.value,
            onValueChange = { onEvent(Event.PrefixChanged(it)) },
            label = "Prefix",
        )

        if (!state.validSuffix) {
            Text(
                text = stringResource(R.string.suffix_must_be_between_3_and_8_letters),
                color = MainTheme.colors.error,
            )
        }

        TextFieldOutlined(
            value = state.suffix.value,
            onValueChange = { onEvent(Event.SuffixChanged(it)) },
            label = "Suffix",
        )
        if (!state.validPassword) {
            Text(
                text = stringResource(R.string.password_requirements),
                color = MainTheme.colors.error,
            )
        }
        PasswordInput(password = state.password.value, onPasswordChange = { onEvent(Event.PasswordChanged(it)) })
    }
}
