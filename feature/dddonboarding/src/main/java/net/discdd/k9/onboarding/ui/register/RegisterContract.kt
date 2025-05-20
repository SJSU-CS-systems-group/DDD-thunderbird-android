package net.discdd.k9.onboarding.ui.register

import app.k9mail.feature.account.common.domain.input.StringInputField

interface RegisterContract {
    data class State(
        val prefix1: StringInputField = StringInputField(),
        val prefix2: StringInputField = StringInputField(),
        val prefix3: StringInputField = StringInputField(),
        val suffix1: StringInputField = StringInputField(),
        val suffix2: StringInputField = StringInputField(),
        val suffix3: StringInputField = StringInputField(),
        val password: StringInputField = StringInputField(),
    ) {
        val readyToRegister
            get(): Boolean {
                return validPrefix && validSuffix && validPassword
            }

        val validPrefix
            get(): Boolean {
                val prefixString = prefix1.value
                return prefixString.length in 3..8 && prefixString.all { it in 'a'..'z' }
            }
        val validSuffix
            get(): Boolean {
                val suffixString = suffix1.value
                return suffixString.length in 3..8 && suffixString.all { it in 'a'..'z' }
            }
        val validPassword
            get(): Boolean {
                val passwordString = password.value
                return passwordString.length >= 8 && passwordString.count { it.isDigit() } >= 1 &&
                    passwordString.toSet().size >= 3
            }
    }

    sealed interface Event {
        data class Prefix1Changed(val prefix: String) : Event
        data class Prefix2Changed(val prefix: String) : Event
        data class Prefix3Changed(val prefix: String) : Event
        data class Suffix1Changed(val suffix: String) : Event
        data class Suffix2Changed(val suffix: String) : Event
        data class Suffix3Changed(val suffix: String) : Event
        data class PasswordChanged(val password: String) : Event
        data class OnClickRegister(
            val prefix1: String,
            val prefix2: String,
            val prefix3: String,
            val suffix1: String,
            val suffix2: String,
            val suffix3: String,
            val password: String,
        ) : Event
    }

    sealed interface Effect {
        data object OnPendingState : Effect
        data object OnLoggedInState : Effect
    }
}
