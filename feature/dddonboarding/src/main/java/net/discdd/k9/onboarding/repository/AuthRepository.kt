package net.discdd.k9.onboarding.repository

import android.net.Uri
import net.discdd.app.k9.common.ControlAdu

interface AuthRepository {
    enum class AuthState {
        LOGGED_IN,
        PENDING,
        LOGGED_OUT,
        ERROR,
        UNKNOWN,
        ;

        companion object {
            fun fromString(state: String): AuthState {
                return try {
                    AuthState.valueOf(state)
                } catch (ignore: IllegalArgumentException) {
                    // If the state is not recognized, return UNKNOWN
                    UNKNOWN
                }
            }
        }
    }

    interface AuthRepositoryListener {
        fun onAuthStateChanged()
    }

    var authRepositoryListener: AuthRepositoryListener?

    suspend fun getState(): Pair<AuthState, ControlAdu?>

    suspend fun logout()
    suspend fun getId(): String?
    suspend fun insertAdu(adu: ControlAdu): Boolean
}
