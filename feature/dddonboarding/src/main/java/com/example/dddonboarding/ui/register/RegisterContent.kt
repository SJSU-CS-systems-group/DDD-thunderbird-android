package com.example.dddonboarding.ui.register

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.k9mail.core.ui.compose.designsystem.atom.Surface
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonText
import app.k9mail.core.ui.compose.designsystem.atom.text.TextDisplayMedium
import app.k9mail.core.ui.compose.designsystem.template.ResponsiveContent

@Composable
internal fun RegisterContent(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Surface(
        modifier = modifier
    ){
        ResponsiveContent {
            TextDisplayMedium(
                text = "Register Screen",
            )
            ButtonText(text = "Go login", onClick = onLoginClick)
        }
    }
}
