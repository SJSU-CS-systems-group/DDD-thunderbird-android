package net.discdd.k9.onboarding.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger
import net.discdd.k9.onboarding.model.AcknowledgementRegisterAdu
import net.discdd.k9.onboarding.repository.AuthRepository.AuthState
import net.discdd.k9.onboarding.util.AuthStateConfig
import net.discdd.k9.onboarding.util.showToast

class AuthRepositoryImpl(
    private val authStateConfig: AuthStateConfig,
    private val context: Context,
) : AuthRepository {
    companion object {
        private val logger: Logger = Logger.getLogger(AuthRepositoryImpl::class.java.name)
        private val RESOLVER_COLUMNS = arrayOf("data")
    }

    override val contentProviderUri: Uri = Uri.parse("content://net.discdd.provider.datastoreprovider/messages")

    override fun getState(): Pair<AuthState, net.discdd.k9.onboarding.model.AcknowledgementAdu?> {
        try {
            var state = authStateConfig.readState()
            return when (state) {
                AuthState.LOGGED_IN -> Pair(AuthState.LOGGED_IN, null)
                AuthState.PENDING -> {
                    val ackAdu = getAckAdu()
                    if (ackAdu == null) {
                        Pair(AuthState.PENDING, null)
                    } else if (ackAdu.success) {
                        setState(AuthState.LOGGED_IN)
                        Pair(AuthState.LOGGED_IN, ackAdu)
                    } else {
                        authStateConfig.deleteState()
                        Pair(AuthState.LOGGED_OUT, null)
                    }
                }
                AuthState.LOGGED_OUT -> Pair(AuthState.LOGGED_OUT, null)
            }
        } catch (e: IOException) {
            showToast(context, e.message)
            return Pair(AuthState.LOGGED_OUT, null)
        }
    }

    private fun getAckAdu(): net.discdd.k9.onboarding.model.AcknowledgementAdu? {
        val cursor = context.contentResolver.query(contentProviderUri, null, null, null, null)
        var ack: net.discdd.k9.onboarding.model.AcknowledgementAdu? = null
        var lastSeenAduId: String? = null
        if (cursor != null && cursor.moveToFirst()) {
            do {
                var data = cursor.getString(cursor.getColumnIndexOrThrow("data"))
                var id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
                lastSeenAduId = id
                Log.d("k9", "adu id: $id")
                // delete adu if exists
                if (data.startsWith("login-ack")) {
                    ack = net.discdd.k9.onboarding.model.AcknowledgementLoginAdu.toAckLoginAdu(data)
                } else if (data.startsWith("register-ack")) {
                    ack = AcknowledgementRegisterAdu.toAckRegisterAdu(data)
                }
            } while (ack == null && cursor.moveToNext())
        }

        if (lastSeenAduId != null) {
            context.contentResolver.delete(contentProviderUri, "deleteAllADUsUpto", arrayOf(lastSeenAduId))
        }
        return ack
    }

    override fun setState(state: AuthState) {
        authStateConfig.writeState(state)
    }

    @Suppress("TooGenericExceptionCaught")
    override fun insertAdu(adu: net.discdd.k9.onboarding.model.Adu): Boolean {
        val values = ContentValues().apply {
            put(RESOLVER_COLUMNS[0], adu.toByteArray())
        }

        try {
            val resolver = context.contentResolver
            val uri = resolver.insert(contentProviderUri, values)
            Log.d("DDDOnboarding", "uri: " + uri)
            if (uri == null) {
                throw IOException("Adu not inserted")
            }
            authStateConfig.writeState(AuthState.PENDING)
            return true
        } catch (e: Exception) {
            showToast(context, "Failed communication to DDD Client: ${e.message}")
            logger.log(Level.SEVERE, "DDDOnboarding", "Failed to insert Adu: ${e.message}")
            return false
        }
    }
}
