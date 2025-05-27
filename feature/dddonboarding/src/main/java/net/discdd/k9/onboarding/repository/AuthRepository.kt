package net.discdd.k9.onboarding.repository

import android.net.Uri

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
    fun insertAdu(adu: net.discdd.k9.onboarding.model.Adu): Boolean
}
