package net.discdd.k9.onboarding.repository

import net.discdd.app.k9.common.ControlAdu

interface AuthRepository {
    enum class AuthState {
        LOGGED_IN,
        PENDING,
        ERROR,
        LOGGED_OUT,
        UNKNOWN,
    }

    interface AuthRepositoryListener {
        fun onAuthStateChanged()
    }

    var authRepositoryListener: AuthRepositoryListener?

    suspend fun getState(): Pair<AuthState, ControlAdu?>

    suspend fun logout()
    suspend fun getId(): String?
    suspend fun insertAdu(adu: ControlAdu, state: AuthState?): Boolean
}
