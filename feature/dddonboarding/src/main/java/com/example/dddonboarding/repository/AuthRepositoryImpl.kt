package com.example.dddonboarding.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.dddonboarding.model.AcknowledgementAdu
import com.example.dddonboarding.model.AcknowledgementLoginAdu
import com.example.dddonboarding.model.AcknowledgementRegisterAdu
import com.example.dddonboarding.model.Adu
import com.example.dddonboarding.util.AuthStateConfig
import com.example.dddonboarding.repository.AuthRepository.AuthState

class AuthRepositoryImpl(
    private val authStateConfig: AuthStateConfig,
    private val context: Context
): AuthRepository {
    private val RESOLVER_COLUMNS = arrayOf("data")
    override val CONTENT_URL: Uri = Uri.parse("content://net.discdd.provider.datastoreprovider/mails");

    override fun getState(): Pair<AuthState, AcknowledgementAdu?> {
        var state = authStateConfig.readState()
        if (state == AuthState.PENDING) {
            val ackAdu = getAckAdu()

            if (ackAdu == null) {
                return Pair(AuthState.PENDING, null)
            }
            if (ackAdu.success) {
                setState(AuthState.LOGGED_IN)
                return Pair(AuthState.LOGGED_IN, ackAdu)
            }
            authStateConfig.deleteState()
            return Pair(AuthState.LOGGED_OUT, null)
        }

        return Pair(state, null)
    }

    private fun getAckAdu(): AcknowledgementAdu? {
        val cursor = context.contentResolver.query(CONTENT_URL, arrayOf("data"), "aduData", null, null)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                var data = String(cursor.getBlob(cursor.getColumnIndexOrThrow("data")))
                // delete adu if exists
                if (data.startsWith("login-ack")){
                    return AcknowledgementLoginAdu.toAckLoginAdu(data)
                } else if (data.startsWith("register-ack")) {
                    return AcknowledgementRegisterAdu.toAckRegisterAdu(data)
                }
            } while (cursor.moveToNext())
        }
        return null;
    }

    override fun setState(state: AuthState){
        authStateConfig.writeState(state)
    }

    override fun insertAdu(adu: Adu): Boolean {
        val values = ContentValues().apply {
            put(RESOLVER_COLUMNS[0], adu.toByteArray())
        }

        try {
            val resolver = context.contentResolver
            val uri = resolver.insert(CONTENT_URL, values)
            Log.d("DDDOnboarding", "uri: "+uri)
            if (uri == null) {
                throw Exception("Adu not inserted")
            }
            authStateConfig.writeState(AuthState.PENDING)
            return true
        } catch (e: Exception) {
            return false
        }
    }
}
