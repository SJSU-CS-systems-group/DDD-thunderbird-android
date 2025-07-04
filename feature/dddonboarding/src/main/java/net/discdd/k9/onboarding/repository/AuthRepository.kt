package net.discdd.k9.onboarding.repository

import android.net.Uri
import net.discdd.app.k9.common.ControlAdu

interface AuthRepository {
    enum class AuthState {
        LOGGED_IN,
        PENDING,
        LOGGED_OUT,
        ERROR,
        UNKNOWN;
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

    val contentProviderUri: Uri

    fun getState(): Pair<AuthState, ControlAdu?>

    fun logout()
    fun getId(): String?
    fun insertAdu(adu: ControlAdu): Boolean
}
