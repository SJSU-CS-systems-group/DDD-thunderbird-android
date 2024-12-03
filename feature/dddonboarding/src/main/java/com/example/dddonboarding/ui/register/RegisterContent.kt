package com.example.dddonboarding.ui.register

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
import app.k9mail.core.ui.compose.designsystem.atom.textfield.TextFieldOutlined
import app.k9mail.core.ui.compose.designsystem.molecule.input.PasswordInput
import app.k9mail.core.ui.compose.designsystem.template.LazyColumnWithHeaderFooter
import app.k9mail.core.ui.compose.designsystem.template.ResponsiveContent
import app.k9mail.core.ui.compose.theme2.MainTheme
import com.example.dddonboarding.ui.register.RegisterContract.State
import com.example.dddonboarding.ui.register.RegisterContract.Event

@Composable
internal fun RegisterContent(
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
                        text = "Register Screen",
                    )
                },
                footer = {
                    RegisterFooter(
                        modifer = Modifier
                            .fillMaxWidth()
                            .padding(top = MainTheme.spacings.quadruple),
                        onRegisterClick = onRegisterClick
                    )
                },
                verticalArrangement =  Arrangement.SpaceEvenly
            ){
                item {
                    RegisterInputs(
                        state = state,
                        onEvent = onEvent
                    )
                }
                item {
                    ButtonFilledTonal(text = "Register", onClick = { onEvent(Event.OnClickRegister(state.prefix1.value, state.prefix2.value, state.prefix3.value, state.suffix1.value, state.suffix2.value, state.suffix3.value, state.password.value)) })
                }
            }
        }
    }
}

@Composable
private fun RegisterFooter(
    onRegisterClick: () -> Unit,
    modifer: Modifier = Modifier
) {
    ButtonText(text = "Go login", onClick = onRegisterClick)
}

@Composable
private fun RegisterInputs(
    state: State,
    onEvent: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    TextFieldOutlined(value = state.prefix1.value, onValueChange = {onEvent(Event.Prefix1Changed(it))})
    TextFieldOutlined(value = state.prefix2.value, onValueChange = {onEvent(Event.Prefix2Changed(it))})
    TextFieldOutlined(value = state.prefix3.value, onValueChange = {onEvent(Event.Prefix3Changed(it))})
    TextFieldOutlined(value = state.suffix1.value, onValueChange = {onEvent(Event.Suffix1Changed(it))})
    TextFieldOutlined(value = state.suffix2.value, onValueChange = {onEvent(Event.Suffix2Changed(it))})
    TextFieldOutlined(value = state.suffix3.value, onValueChange = {onEvent(Event.Suffix3Changed(it))})
    PasswordInput(password = state.password.value, onPasswordChange = {onEvent(Event.PasswordChanged(it))})
}
