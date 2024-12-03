package com.example.dddonboarding.ui.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.k9mail.core.ui.compose.designsystem.atom.Surface
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonText
import app.k9mail.core.ui.compose.designsystem.atom.text.TextDisplayMedium
import app.k9mail.core.ui.compose.designsystem.atom.textfield.TextFieldOutlined
import app.k9mail.core.ui.compose.designsystem.molecule.input.PasswordInput
import app.k9mail.core.ui.compose.designsystem.template.LazyColumnWithHeaderFooter
import app.k9mail.core.ui.compose.designsystem.template.ResponsiveContent
import app.k9mail.core.ui.compose.theme2.MainTheme

@Composable
internal fun RegisterContent(
    onLoginClick: () -> Unit,
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
                        onLoginClick = onLoginClick
                    )
                },
                verticalArrangement =  Arrangement.SpaceEvenly
            ){
                item {
                    RegisterInputs()
                }
            }
        }
    }
}

@Composable
private fun RegisterFooter(
    onLoginClick: () -> Unit,
    modifer: Modifier = Modifier
) {
    ButtonText(text = "Go login", onClick = onLoginClick)
}

@Composable
private fun RegisterInputs(
    modifier: Modifier = Modifier
) {
    TextFieldOutlined(value = "Prefix 1", onValueChange = {})
    TextFieldOutlined(value = "Prefix 2", onValueChange = {})
    TextFieldOutlined(value = "Prefix 3", onValueChange = {})
    TextFieldOutlined(value = "Suffix 1", onValueChange = {})
    TextFieldOutlined(value = "Suffix 2", onValueChange = {})
    TextFieldOutlined(value = "Suffix 3", onValueChange = {})
    PasswordInput(password = "Secret", onPasswordChange = {})
}
