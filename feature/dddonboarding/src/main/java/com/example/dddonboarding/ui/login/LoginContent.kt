package com.example.dddonboarding.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.k9mail.core.ui.compose.designsystem.atom.Surface
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonFilledTonal
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonText
import app.k9mail.core.ui.compose.designsystem.atom.text.TextDisplayMedium
import app.k9mail.core.ui.compose.designsystem.molecule.input.EmailAddressInput
import app.k9mail.core.ui.compose.designsystem.molecule.input.PasswordInput
import app.k9mail.core.ui.compose.designsystem.template.LazyColumnWithHeaderFooter
import app.k9mail.core.ui.compose.designsystem.template.ResponsiveContent
import app.k9mail.core.ui.compose.theme2.MainTheme
import com.example.dddonboarding.ui.login.LoginContract.State
import com.example.dddonboarding.ui.login.LoginContract.Event

@Composable
internal fun LoginContent(
    state: State,
    onEvent: (Event) -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Surface(
        modifier = modifier
    ){
        ResponsiveContent {
            LazyColumnWithHeaderFooter(
                modifier = Modifier.fillMaxSize(),
                header = {
                    TextDisplayMedium(
                        text = "Login Screen",
                    )
                },
                footer = {
                    LoginFooter(
                        modifer = Modifier
                            .fillMaxWidth()
                            .padding(top = MainTheme.spacings.quadruple),
                        onRegisterClick = onRegisterClick
                    )
                },
                verticalArrangement =  Arrangement.SpaceEvenly
            ){
                item {
                    LoginInputs(
                        state = state,
                        onEvent = onEvent
                    )
                }
                item {
                    ButtonFilledTonal(text = "Login", onClick = {onEvent(Event.OnClickLogin(state.emailAddress.value, state.password.value))})
                }
            }
        }
    }
}

@Composable
private fun LoginFooter(
    onRegisterClick: () -> Unit,
    modifer: Modifier = Modifier,
) {
    ButtonText(text = "Go register", onClick = onRegisterClick)
}

@Composable
private fun LoginInputs(
    state: State,
    onEvent: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    EmailAddressInput(
        emailAddress = state.emailAddress.value,
        onEmailAddressChange = {onEvent(Event.EmailAddressChanged(it))},
    )
    PasswordInput(
        password = state.password.value,
        onPasswordChange = {onEvent(Event.PasswordChanged(it))},
    )
}
