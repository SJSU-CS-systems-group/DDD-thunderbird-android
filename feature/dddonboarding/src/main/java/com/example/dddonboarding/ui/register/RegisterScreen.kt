package com.example.dddonboarding.ui.login

import androidx.compose.runtime.Composable
import com.example.dddonboarding.ui.register.RegisterContent

@Composable
fun RegisterScreen(
    onLoginClick: () -> Unit
) {
    RegisterContent(
        onLoginClick = onLoginClick
    )
}
