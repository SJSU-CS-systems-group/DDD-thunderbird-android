package net.discdd.k9.onboarding.ui.login

import app.k9mail.feature.account.common.domain.input.StringInputField
import app.k9mail.feature.account.setup.domain.entity.AccountUuid

val EMAIL_USER_LETTERS = ('a'..'z') + ('0'..'9')
val EMAIL_DOMAIN_LETTERS = (EMAIL_USER_LETTERS) + '.'

private const val DDD_MIN_PASSWORD_LEN = 8

interface LoginContract {
    // interface ViewModel: UnidirectionalViewModel<State, Event, Effect> {}

    data class State(
        val emailAddress: StringInputField = StringInputField(),
        val password: StringInputField = StringInputField(),
    ) {
        val validEmail
            get(): Boolean
            {
                val parts = emailAddress.value.split('@', limit = 2)
                if (parts.size != 2) return false
                val (u, d) = parts
                return u.all { it in EMAIL_USER_LETTERS } && d.all { it in EMAIL_DOMAIN_LETTERS } &&
                    u.isNotEmpty() && d.isNotEmpty() && d.count { it == '.' } >= 1
            }
        val validPassword
            get(): Boolean
            {
                val suffixString = password.value
                return suffixString.length >= DDD_MIN_PASSWORD_LEN
            }
    }

    sealed interface Event {
        data class EmailAddressChanged(val emailAddress: String) : Event
        data class PasswordChanged(val password: String) : Event
        data class OnClickLogin(val emailAddress: String, val password: String) : Event
        data object CheckAuthState : Event
    }

    sealed interface Effect {
        data object OnPendingState : Effect
        data class OnLoggedInState(val accountUuid: AccountUuid) : Effect
        data class OnError(val error: kotlin.Error) : LoginContract.Effect
    }
}
