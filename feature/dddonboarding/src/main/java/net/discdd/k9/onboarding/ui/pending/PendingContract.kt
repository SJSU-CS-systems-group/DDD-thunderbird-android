package net.discdd.k9.onboarding.ui.pending

interface PendingContract {
    sealed interface Event {
        data object OnRedoLoginClick: Event

        data object CheckAuthState: Event
    }

    sealed interface Effect {
        data object OnRedoLoginState: Effect
    }
}
