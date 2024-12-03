package com.example.dddonboarding.repository

import android.net.Uri
import com.example.dddonboarding.model.AcknowledgementAdu
import com.example.dddonboarding.model.Adu

interface AuthRepository {
    enum class AuthState {
        LOGGED_IN,
        PENDING,
        LOGGED_OUT
    }

    val CONTENT_URL: Uri;

    fun getState(): Pair<AuthState, AcknowledgementAdu?>

    fun setState(state: AuthState)

    fun insertAdu(adu: Adu): Boolean
}
