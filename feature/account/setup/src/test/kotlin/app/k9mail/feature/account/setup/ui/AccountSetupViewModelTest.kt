package app.k9mail.feature.account.setup.ui

import app.k9mail.autodiscovery.api.AutoDiscoveryResult
import app.k9mail.autodiscovery.api.ImapServerSettings
import app.k9mail.autodiscovery.api.SmtpServerSettings
import app.k9mail.core.common.net.toHostname
import app.k9mail.core.common.net.toPort
import app.k9mail.core.ui.compose.testing.MainDispatcherRule
import app.k9mail.core.ui.compose.testing.mvi.assertThatAndMviTurbinesConsumed
import app.k9mail.core.ui.compose.testing.mvi.turbinesWithInitialStateCheck
import app.k9mail.feature.account.setup.data.InMemoryAccountSetupStateRepository
import app.k9mail.feature.account.setup.domain.entity.AccountOptions
import app.k9mail.feature.account.setup.domain.entity.AccountSetupState
import app.k9mail.feature.account.setup.domain.entity.EmailCheckFrequency
import app.k9mail.feature.account.setup.domain.entity.EmailDisplayCount
import app.k9mail.feature.account.setup.domain.entity.MailConnectionSecurity
import app.k9mail.feature.account.setup.domain.input.StringInputField
import app.k9mail.feature.account.setup.ui.AccountSetupContract.Effect
import app.k9mail.feature.account.setup.ui.AccountSetupContract.SetupStep
import app.k9mail.feature.account.setup.ui.AccountSetupContract.State
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract
import app.k9mail.feature.account.setup.ui.options.AccountOptionsContract
import app.k9mail.feature.account.setup.ui.options.FakeAccountOptionsViewModel
import app.k9mail.feature.account.setup.ui.outgoing.toServerSettings
import app.k9mail.feature.account.setup.ui.outgoing.toValidationState
import app.k9mail.feature.account.setup.ui.validation.FakeAccountValidationViewModel
import app.k9mail.feature.account.setup.ui.validation.InMemoryAuthStateStorage
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import com.fsck.k9.mail.AuthType
import com.fsck.k9.mail.ServerSettings
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import app.k9mail.autodiscovery.api.AuthenticationType as AutoDiscoveryAuthenticationType
import app.k9mail.autodiscovery.api.ConnectionSecurity as AutoDiscoveryConnectionSecurity

@Suppress("LongMethod")
class AccountSetupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val outgoingValidationViewModel = FakeAccountValidationViewModel()
    private val optionsViewModel = FakeAccountOptionsViewModel()
    private val authStateStorage = InMemoryAuthStateStorage()

    @Test
    fun `should forward step state on next event`() = runTest {
        var createAccountEmailAddress: String? = null
        var createAccountIncomingServerSettings: ServerSettings? = null
        var createAccountOutgoingServerSettings: ServerSettings? = null
        var createAccountAuthorizationState: String? = null
        var createAccountOptions: AccountOptions? = null
        val accountSetupStateRepository = InMemoryAccountSetupStateRepository()
        val viewModel = AccountSetupViewModel(
            createAccount = { emailAddress, incomingServerSettings, outgoingServerSettings, authState, options ->
                createAccountEmailAddress = emailAddress
                createAccountIncomingServerSettings = incomingServerSettings
                createAccountOutgoingServerSettings = outgoingServerSettings
                createAccountAuthorizationState = authState
                createAccountOptions = options

                "accountUuid"
            },
            optionsViewModel = optionsViewModel,
            authStateStorage = authStateStorage,
            accountSetupStateRepository = accountSetupStateRepository,
        )
        val turbines = turbinesWithInitialStateCheck(viewModel, State(setupStep = SetupStep.AUTO_CONFIG))

        // FIXME autoDiscoveryViewModel.initState(AUTODISCOVERY_STATE)
        viewModel.event(
            AccountSetupContract.Event.OnAutoDiscoveryFinished(
                state = AUTODISCOVERY_STATE,
                isAutomaticConfig = false,
            ),
        )

        val expectedAccountSetupState = AccountSetupState(
            emailAddress = "test@domain.example",
            incomingServerSettings = ServerSettings(
                type = "imap",
                host = INCOMING_SERVER_NAME,
                port = INCOMING_SERVER_PORT,
                connectionSecurity = MailConnectionSecurity.SSL_TLS_REQUIRED,
                authenticationType = AuthType.CRAM_MD5,
                username = USERNAME,
                password = PASSWORD,
                clientCertificateAlias = null,
                extra = emptyMap(),
            ),
            outgoingServerSettings = ServerSettings(
                type = "smtp",
                host = OUTGOING_SERVER_NAME,
                port = OUTGOING_SERVER_PORT,
                connectionSecurity = MailConnectionSecurity.SSL_TLS_REQUIRED,
                authenticationType = AuthType.CRAM_MD5,
                username = USERNAME,
                password = PASSWORD,
                clientCertificateAlias = null,
                extra = emptyMap(),
            ),
            authorizationState = null,
            options = null,
        )

        assertThat(accountSetupStateRepository.getState()).isEqualTo(expectedAccountSetupState)

        assertThat(optionsViewModel.state.value).isEqualTo(
            AccountOptionsContract.State(
                accountName = StringInputField(EMAIL_ADDRESS),
            ),
        )

        assertThatAndMviTurbinesConsumed(
            actual = turbines.stateTurbine.awaitItem(),
            turbines = turbines,
        ) {
            prop(State::setupStep).isEqualTo(SetupStep.INCOMING_CONFIG)
        }

        viewModel.event(AccountSetupContract.Event.OnNext)

        // FIXME
//        assertThat(incomingValidationViewModel.state.value).isEqualTo(expectedIncomingConfigState.toValidationState())

        assertThatAndMviTurbinesConsumed(
            actual = turbines.stateTurbine.awaitItem(),
            turbines = turbines,
        ) {
            prop(State::setupStep).isEqualTo(SetupStep.INCOMING_VALIDATION)
        }

        viewModel.event(AccountSetupContract.Event.OnNext)

        assertThatAndMviTurbinesConsumed(
            actual = turbines.stateTurbine.awaitItem(),
            turbines = turbines,
        ) {
            prop(State::setupStep).isEqualTo(SetupStep.OUTGOING_CONFIG)
        }

        viewModel.event(AccountSetupContract.Event.OnNext)

        assertThat(outgoingValidationViewModel.state.value).isEqualTo(expectedOutgoingConfigState.toValidationState())

        assertThatAndMviTurbinesConsumed(
            actual = turbines.stateTurbine.awaitItem(),
            turbines = turbines,
        ) {
            prop(State::setupStep).isEqualTo(SetupStep.OUTGOING_VALIDATION)
        }

        viewModel.event(AccountSetupContract.Event.OnNext)

        assertThatAndMviTurbinesConsumed(
            actual = turbines.stateTurbine.awaitItem(),
            turbines = turbines,
        ) {
            prop(State::setupStep).isEqualTo(SetupStep.OPTIONS)
        }

        optionsViewModel.initState(
            optionsViewModel.state.value.copy(
                accountName = StringInputField("account name"),
                displayName = StringInputField("display name"),
                emailSignature = StringInputField("signature"),
                checkFrequency = EmailCheckFrequency.EVERY_15_MINUTES,
                messageDisplayCount = EmailDisplayCount.MESSAGES_100,
                showNotification = true,
            ),
        )

        viewModel.event(AccountSetupContract.Event.OnNext)

        assertThatAndMviTurbinesConsumed(
            actual = turbines.effectTurbine.awaitItem(),
            turbines = turbines,
        ) {
            isEqualTo(Effect.NavigateNext("accountUuid"))
        }

        assertThat(createAccountEmailAddress).isEqualTo(EMAIL_ADDRESS)
        // FIXME
//        assertThat(createAccountIncomingServerSettings).isEqualTo(expectedIncomingConfigState.toServerSettings())
        assertThat(createAccountOutgoingServerSettings).isEqualTo(expectedOutgoingConfigState.toServerSettings())
        assertThat(createAccountAuthorizationState).isNull()
        assertThat(createAccountOptions).isEqualTo(
            AccountOptions(
                accountName = "account name",
                displayName = "display name",
                emailSignature = "signature",
                checkFrequencyInMinutes = 15,
                messageDisplayCount = 100,
                showNotification = true,
            ),
        )
    }

    @Test
    fun `should rewind step state on back event`() = runTest {
        val initialState = State(setupStep = SetupStep.OPTIONS)
        val viewModel = AccountSetupViewModel(
            createAccount = { _, _, _, _, _ -> "accountUuid" },
            optionsViewModel = FakeAccountOptionsViewModel(),
            authStateStorage = authStateStorage,
            accountSetupStateRepository = InMemoryAccountSetupStateRepository(),
            initialState = initialState,
        )
        val turbines = turbinesWithInitialStateCheck(viewModel, initialState)

        viewModel.event(AccountSetupContract.Event.OnBack)

        assertThatAndMviTurbinesConsumed(
            actual = turbines.stateTurbine.awaitItem(),
            turbines = turbines,
        ) {
            prop(State::setupStep).isEqualTo(SetupStep.OUTGOING_CONFIG)
        }

        viewModel.event(AccountSetupContract.Event.OnBack)

        assertThatAndMviTurbinesConsumed(
            actual = turbines.stateTurbine.awaitItem(),
            turbines = turbines,
        ) {
            prop(State::setupStep).isEqualTo(SetupStep.INCOMING_CONFIG)
        }

        viewModel.event(AccountSetupContract.Event.OnBack)

        assertThatAndMviTurbinesConsumed(
            actual = turbines.stateTurbine.awaitItem(),
            turbines = turbines,
        ) {
            prop(State::setupStep).isEqualTo(SetupStep.AUTO_CONFIG)
        }

        viewModel.event(AccountSetupContract.Event.OnBack)

        assertThatAndMviTurbinesConsumed(
            actual = turbines.effectTurbine.awaitItem(),
            turbines = turbines,
        ) {
            isEqualTo(Effect.NavigateBack)
        }
    }

    @Test
    fun `should go back from OPTIONS step on back event when isAutomaticConfig enabled`() = runTest {
        val initialState = State(
            setupStep = SetupStep.OPTIONS,
            isAutomaticConfig = true,
        )
        val viewModel = AccountSetupViewModel(
            createAccount = { _, _, _, _, _ -> "accountUuid" },
            optionsViewModel = FakeAccountOptionsViewModel(),
            authStateStorage = authStateStorage,
            accountSetupStateRepository = InMemoryAccountSetupStateRepository(),
            initialState = initialState,
        )
        val turbines = turbinesWithInitialStateCheck(viewModel, initialState)

        viewModel.event(AccountSetupContract.Event.OnBack)

        assertThatAndMviTurbinesConsumed(
            actual = turbines.stateTurbine.awaitItem(),
            turbines = turbines,
        ) {
            prop(State::setupStep).isEqualTo(SetupStep.AUTO_CONFIG)
        }

        viewModel.event(AccountSetupContract.Event.OnBack)

        assertThatAndMviTurbinesConsumed(
            actual = turbines.effectTurbine.awaitItem(),
            turbines = turbines,
        ) {
            isEqualTo(Effect.NavigateBack)
        }
    }

    @Test
    fun `should go back from OUTGOING_VALIDATION step state on back event when isAutomaticConfig enabled`() = runTest {
        val initialState = State(
            setupStep = SetupStep.OUTGOING_VALIDATION,
            isAutomaticConfig = true,
        )
        val viewModel = AccountSetupViewModel(
            createAccount = { _, _, _, _, _ -> "accountUuid" },
            optionsViewModel = FakeAccountOptionsViewModel(),
            authStateStorage = authStateStorage,
            accountSetupStateRepository = InMemoryAccountSetupStateRepository(),
            initialState = initialState,
        )
        val turbines = turbinesWithInitialStateCheck(viewModel, initialState)

        viewModel.event(AccountSetupContract.Event.OnBack)

        assertThatAndMviTurbinesConsumed(
            actual = turbines.stateTurbine.awaitItem(),
            turbines = turbines,
        ) {
            prop(State::setupStep).isEqualTo(SetupStep.AUTO_CONFIG)
        }

        viewModel.event(AccountSetupContract.Event.OnBack)

        assertThatAndMviTurbinesConsumed(
            actual = turbines.effectTurbine.awaitItem(),
            turbines = turbines,
        ) {
            isEqualTo(Effect.NavigateBack)
        }
    }

    @Test
    fun `should go back from INCOMING_VALIDATION step state on back event when isAutomaticConfig enabled`() = runTest {
        val initialState = State(
            setupStep = SetupStep.OUTGOING_VALIDATION,
            isAutomaticConfig = true,
        )
        val viewModel = AccountSetupViewModel(
            createAccount = { _, _, _, _, _ -> "accountUuid" },
            optionsViewModel = FakeAccountOptionsViewModel(),
            authStateStorage = authStateStorage,
            accountSetupStateRepository = InMemoryAccountSetupStateRepository(),
            initialState = initialState,
        )
        val turbines = turbinesWithInitialStateCheck(viewModel, initialState)

        viewModel.event(AccountSetupContract.Event.OnBack)

        assertThatAndMviTurbinesConsumed(
            actual = turbines.stateTurbine.awaitItem(),
            turbines = turbines,
        ) {
            prop(State::setupStep).isEqualTo(SetupStep.AUTO_CONFIG)
        }

        viewModel.event(AccountSetupContract.Event.OnBack)

        assertThatAndMviTurbinesConsumed(
            actual = turbines.effectTurbine.awaitItem(),
            turbines = turbines,
        ) {
            isEqualTo(Effect.NavigateBack)
        }
    }

    companion object {
        private const val EMAIL_ADDRESS = "test@domain.example"
        private const val USERNAME = EMAIL_ADDRESS
        private const val PASSWORD = "password"
        private const val INCOMING_SERVER_NAME = "imap.domain.example"
        private const val INCOMING_SERVER_PORT = 993
        private const val OUTGOING_SERVER_NAME = "smtp.domain.example"
        private const val OUTGOING_SERVER_PORT = 465

        private val INCOMING_SERVER_SETTINGS = ImapServerSettings(
            hostname = INCOMING_SERVER_NAME.toHostname(),
            port = INCOMING_SERVER_PORT.toPort(),
            connectionSecurity = AutoDiscoveryConnectionSecurity.TLS,
            authenticationTypes = listOf(AutoDiscoveryAuthenticationType.PasswordEncrypted),
            username = USERNAME,
        )

        private val OUTGOING_SERVER_SETTINGS = SmtpServerSettings(
            hostname = OUTGOING_SERVER_NAME.toHostname(),
            port = OUTGOING_SERVER_PORT.toPort(),
            connectionSecurity = AutoDiscoveryConnectionSecurity.TLS,
            authenticationTypes = listOf(AutoDiscoveryAuthenticationType.PasswordEncrypted),
            username = USERNAME,
        )

        private val AUTODISCOVERY_RESULT = AutoDiscoveryResult.Settings(
            incomingServerSettings = INCOMING_SERVER_SETTINGS,
            outgoingServerSettings = OUTGOING_SERVER_SETTINGS,
            isTrusted = true,
            source = "test",
        )

        private val AUTODISCOVERY_STATE = AccountAutoDiscoveryContract.State(
            configStep = AccountAutoDiscoveryContract.ConfigStep.PASSWORD,
            emailAddress = StringInputField(EMAIL_ADDRESS),
            password = StringInputField(PASSWORD),
            autoDiscoverySettings = AUTODISCOVERY_RESULT,
        )
    }
}
