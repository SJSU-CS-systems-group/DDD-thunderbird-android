package app.k9mail.feature.account.setup.ui

import app.k9mail.core.ui.compose.common.mvi.UnidirectionalViewModel
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract
import app.k9mail.feature.account.setup.ui.options.AccountOptionsContract

interface AccountSetupContract {

    enum class SetupStep {
        AUTO_CONFIG,
        INCOMING_CONFIG,
        INCOMING_VALIDATION,
        OUTGOING_CONFIG,
        OUTGOING_VALIDATION,
        OPTIONS,
    }

    interface ViewModel : UnidirectionalViewModel<State, Event, Effect> {
        val optionsViewModel: AccountOptionsContract.ViewModel
    }

    data class State(
        val setupStep: SetupStep = SetupStep.AUTO_CONFIG,
        val isAutomaticConfig: Boolean = false,
    )

    sealed interface Event {
        object OnNext : Event

        data class OnAutoDiscoveryFinished(
            val state: AccountAutoDiscoveryContract.State,
            val isAutomaticConfig: Boolean,
        ) : Event

        object OnBack : Event
    }

    sealed interface Effect {

        data class NavigateNext(
            val accountUuid: String,
        ) : Effect

        object NavigateBack : Effect
    }
}
