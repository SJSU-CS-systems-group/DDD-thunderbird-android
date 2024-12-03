package com.example.dddonboarding.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import com.example.dddonboarding.ui.login.LoginScreen
import com.example.dddonboarding.ui.login.LoginViewModel
import com.example.dddonboarding.ui.login.RegisterScreen
import com.example.dddonboarding.ui.pending.PendingScreen
import com.example.dddonboarding.ui.register.RegisterViewModel
import org.koin.androidx.compose.koinViewModel

private const val NESTED_NAVIGATION_ROUTE_REGISTER = "register"
private const val NESTED_NAVIGATION_ROUTE_LOGIN = "login"
private const val NESTED_NAVIGATION_ROUTE_PENDING = "pending"

private fun NavController.navigateToRegister() {
    navigate(NESTED_NAVIGATION_ROUTE_REGISTER)
}

private fun NavController.navigateToLogin() {
    navigate(NESTED_NAVIGATION_ROUTE_LOGIN)
}

private fun NavController.navigateToPending() {
    Log.d("DDDOnboarding", "navigating to pending")
    navigate(NESTED_NAVIGATION_ROUTE_PENDING)
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
                onRegisterClick = { navController.navigateToRegister() },
                viewModel =  koinViewModel<LoginViewModel>(),
                onPendingState = { navController.navigateToPending() },
                onFinish = { onFinish }
            )
        }
        composable(route = NESTED_NAVIGATION_ROUTE_REGISTER){
            RegisterScreen(
                onRegisterClick = { navController.navigateToLogin() },
                viewModel =  koinViewModel<RegisterViewModel>(),
                onPendingState = { navController.navigateToPending() },
                onFinish = { onFinish }
            )
        }
        composable(route = NESTED_NAVIGATION_ROUTE_PENDING){
            PendingScreen()
        }
    }
}
