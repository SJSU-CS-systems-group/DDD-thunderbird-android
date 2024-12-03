package com.example.dddonboarding

import com.example.dddonboarding.repository.AuthRepositoryImpl
import com.example.dddonboarding.repository.AuthRepository
import com.example.dddonboarding.ui.login.LoginViewModel
import com.example.dddonboarding.util.AuthStateConfig
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.core.qualifier.named

val featureDddOnboardingModule: Module = module {
    single<AuthStateConfig> { AuthStateConfig(get()) }
    single<AuthRepository> { AuthRepositoryImpl(
        authStateConfig = get(),
        context = get()
    ) }

    viewModel{
        LoginViewModel (
            authRepository = get()
        )
    }
}
