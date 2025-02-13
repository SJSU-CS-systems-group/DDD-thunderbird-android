package net.discdd.k9.onboarding

import app.k9mail.feature.account.setup.domain.usecase.CreateAccount
import com.example.dddonboarding.repository.AuthRepositoryImpl
import com.example.dddonboarding.repository.AuthRepository
import com.example.dddonboarding.ui.login.LoginViewModel
import com.example.dddonboarding.ui.register.RegisterViewModel
import com.example.dddonboarding.util.AuthStateConfig
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val featureDddOnboardingModule: Module = module {
    single<AuthStateConfig> { AuthStateConfig(get()) }
    single<AuthRepository> { AuthRepositoryImpl(
        authStateConfig = get(),
        context = get()
    ) }

    factory<CreateAccount> {
        CreateAccount(
            accountCreator = get(),
        )
    }

    viewModel{
        LoginViewModel (
            createAccount = get(),
            authRepository = get()
        )
    }

    viewModel{
        RegisterViewModel (
            authRepository = get()
        )
    }
}
