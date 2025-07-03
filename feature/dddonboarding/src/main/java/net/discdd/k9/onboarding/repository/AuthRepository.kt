package net.discdd.k9.onboarding.repository

import android.net.Uri
import net.discdd.app.k9.common.ControlAdu

interface AuthRepository {
    enum class AuthState {
        LOGGED_IN,
        PENDING,
        LOGGED_OUT,
    }

    val contentProviderUri: Uri

    fun getState(): Pair<AuthState, String?>

    fun logout()
    fun getId(): String?
    fun insertAdu(adu: ControlAdu): Boolean
}
