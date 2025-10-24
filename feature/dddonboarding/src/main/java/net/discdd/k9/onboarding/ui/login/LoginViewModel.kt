package net.discdd.k9.onboarding.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.k9mail.feature.account.common.domain.entity.AccountDisplayOptions
import app.k9mail.feature.account.common.domain.entity.AccountState
import app.k9mail.feature.account.setup.AccountSetupExternalContract.AccountCreator.AccountCreatorResult
import app.k9mail.feature.account.setup.domain.entity.AccountUuid
import app.k9mail.feature.account.setup.domain.usecase.CreateAccount
import com.fsck.k9.Account
import com.fsck.k9.Preferences
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.discdd.app.k9.common.ControlAdu
import net.discdd.k9.onboarding.repository.AuthRepository
import net.discdd.k9.onboarding.repository.AuthRepository.AuthState
import net.discdd.k9.onboarding.ui.login.LoginContract.Effect
import net.discdd.k9.onboarding.ui.login.LoginContract.Event
import net.discdd.k9.onboarding.ui.login.LoginContract.State
import net.discdd.k9.onboarding.util.CreateAccountConstants

@Suppress("TooManyFunctions")
class LoginViewModel(
    initialState: State = State(),
    private val createAccount: CreateAccount,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()
    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()
    private val _effectFlow = MutableSharedFlow<Effect>(replay = 1)
    val effectFlow: SharedFlow<Effect> = _effectFlow.asSharedFlow()

    fun event(event: Event) {
        when (event) {
            is Event.EmailAddressChanged -> setEmailAddress(event.emailAddress)
            is Event.PasswordChanged -> setPassword(event.password)
            is Event.OnClickLogin -> login(email = event.emailAddress, password = event.password)
            Event.CheckAuthState -> viewModelScope.launch { checkAuthState() }
        }
    }

    private suspend fun checkAuthState() {
        if (!(authRepository.checkClientStatus())) {
            Log.d("checkAuthState", "Client Does Not Exist")
            clientError()
        }
        val (state, adu) = authRepository.getState()
        if (state == AuthState.PENDING) {
            Log.d("LoginViewModel", "state $state")
            navigatePending()
        } else if (state == AuthState.LOGGED_IN && adu != null && adu is ControlAdu.EmailAck) {
            _lastError.value = null
            createAccount(adu)
        } else if (state == AuthState.ERROR) {
            _lastError.value = adu?.let {
                when (it) {
                    is ControlAdu.EmailAck -> it.message()
                    else -> it.toString()
                }
            } ?: "Unknown error"
            navigateLogin()
        } else {
            navigateLogin()
        }
    }

    fun clearLastError() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    private fun createAccount(adu: ControlAdu.EmailAck) {
        val id = adu.email()
        val username = id.substringBefore("@")
        val pattern = "^([a-zA-Z]+)(\\d+[a-zA-Z]*\\d+)([a-zA-Z]+)$".toRegex()
        val matches = pattern.find(username)
        val prefix = matches?.groups?.get(1)?.value ?: username

        @Suppress("MagicNumber")
        val suffix = matches?.groups?.get(3)?.value ?: ""
        val accountState = AccountState(
            emailAddress = id,
            incomingServerSettings = CreateAccountConstants.INCOMING_SERVER_SETTINGS,
            outgoingServerSettings = CreateAccountConstants.OUTGOING_SERVER_SETTINGS,
            specialFolderSettings = CreateAccountConstants.SPECIAL_FOLDER_SETTINGS,
            syncOptions = CreateAccountConstants.SYNC_OPTIONS,
            displayOptions = AccountDisplayOptions(
                accountName = "DiscDD",
                displayName = "$prefix $suffix",
                emailSignature = "-- from a disconnected device: discdd.net --",
            ),
        )

        viewModelScope.launch {
            when (val result = createAccount.execute(accountState)) {
                is AccountCreatorResult.Success -> {
                    // turn on pushes to the inbox from DDD Client
                    val preferences = Preferences.getPreferences()
                    preferences.getAccount(result.accountUuid)?.apply {
                        this.folderPushMode = Account.FolderMode.ALL
                        preferences.saveAccount(this)
                    }
                    showSuccess(AccountUuid(result.accountUuid))
                }
                is AccountCreatorResult.Error -> showError(Error(result.message))
            }
        }
    }

    private fun showSuccess(accountUuid: AccountUuid) {
        viewModelScope.launch {
            navigateLoggedIn(accountUuid)
        }
    }

    private fun showError(error: Error) {
        viewModelScope.launch {
            _effectFlow.emit(Effect.OnError(error))
        }
    }

    private fun setEmailAddress(email: String) {
        _state.update {
            it.copy(
                emailAddress = it.emailAddress.updateValue(email),
            )
        }
    }

    private fun setPassword(password: String) {
        _state.update {
            it.copy(
                password = it.password.updateValue(password),
            )
        }
    }

    private fun login(email: String, password: String) {
        val loginAdu = ControlAdu.LoginControlAdu(
            mapOf(
                Pair("email", email),
                Pair("password", password),
            ),
        )
        viewModelScope.launch {
            authRepository.insertAdu(loginAdu, AuthState.PENDING)
            checkAuthState()
        }
    }

    private fun clientError() {
        Log.d("k9", "client error")
        viewModelScope.coroutineContext.cancelChildren()
        viewModelScope.launch {
            _effectFlow.emit(Effect.OnErrorState)
        }
    }
    private fun navigatePending() {
        Log.d("k9", "navigate pending")
        viewModelScope.coroutineContext.cancelChildren()
        viewModelScope.launch {
            _effectFlow.emit(Effect.OnPendingState)
        }
    }

    private fun navigateLogin() {
        Log.d("k9", "navigate login")
        viewModelScope.coroutineContext.cancelChildren()
    }

    private fun navigateLoggedIn(accountUuid: AccountUuid) {
        Log.d("k9", "navigate loggedin")
        viewModelScope.coroutineContext.cancelChildren()
        viewModelScope.launch {
            _effectFlow.emit(Effect.OnLoggedInState(accountUuid))
        }
    }
}
