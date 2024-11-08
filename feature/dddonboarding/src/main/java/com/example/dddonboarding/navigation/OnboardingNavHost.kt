package com.example.dddonboarding.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import com.example.dddonboarding.ui.login.LoginScreen
import com.example.dddonboarding.ui.login.RegisterScreen

private const val NESTED_NAVIGATION_ROUTE_REGISTER = "register"
private const val NESTED_NAVIGATION_ROUTE_LOGIN = "login"

private fun NavController.navigateToRegister() {
    navigate(NESTED_NAVIGATION_ROUTE_REGISTER)
}

private fun NavController.navigateToLogin() {
    navigate(NESTED_NAVIGATION_ROUTE_LOGIN)
}

@Composable
fun OnboardingNavHost(
    onFinish: (String?) -> Unit
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = NESTED_NAVIGATION_ROUTE_LOGIN
    ) {
        composable(route = NESTED_NAVIGATION_ROUTE_LOGIN){
            LoginScreen(
                onRegisterClick = { navController.navigateToRegister() }
            )
        }
        composable(route = NESTED_NAVIGATION_ROUTE_REGISTER){
            RegisterScreen(
                onLoginClick = { navController.navigateToLogin() }
            )
        }
    }
}
