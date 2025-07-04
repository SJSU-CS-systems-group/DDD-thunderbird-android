package net.discdd.k9.onboarding.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger
import net.discdd.app.k9.common.ControlAdu
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

    override fun getState(): Pair<AuthState, ControlAdu?> {
        var adu = getAckAdu()
        if (adu != null) {
            if (adu is ControlAdu.EmailAck) {
                if (adu.success()) {
                    authStateConfig.writeState(AuthState.LOGGED_IN, adu)
                } else {
                    authStateConfig.writeState(AuthState.ERROR, adu)
                }
            }
        }
        return authStateConfig.readState()
    }

    override fun logout() {
        authStateConfig.writeState(AuthState.LOGGED_OUT)
    }

    @Suppress("NestedBlockDepth")
    private fun getAckAdu(): ControlAdu? {
        return context.contentResolver.query(contentProviderUri, null, null, null, null)?.use { cursor ->
            var lastSeenAduId: String? = null
            var ack: ControlAdu? = null
            if (cursor.moveToFirst()) {
                do {
                    val data = cursor.getBlob(cursor.getColumnIndexOrThrow("data"))
                    val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
                    lastSeenAduId = id
                    Log.d("dddEmail", "adu id: $id => $data")
                    // delete adu if exists
                    if (ControlAdu.isControlAdu(data)) {
                        ack = ControlAdu.fromBytes(data)
                    }
                } while (ack == null && cursor.moveToNext())
            }
            if (lastSeenAduId != null) {
                context.contentResolver.delete(contentProviderUri, "deleteAllADUsUpto", arrayOf(lastSeenAduId))
            }
            ack
        }
    }

    override fun getId(): String? {
        return authStateConfig.readState().second?.let {
            if (it is ControlAdu.LoginAckControlAdu) {
                it.email()
            } else if (it is ControlAdu.RegisterAckControlAdu) {
                it.email()
            } else {
                null
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override fun insertAdu(adu: ControlAdu): Boolean {
        val values = ContentValues().apply {
            put(RESOLVER_COLUMNS[0], adu.toBytes())
        }

        try {
            val resolver = context.contentResolver
            val uri = resolver.insert(contentProviderUri, values)
            Log.d("DDDOnboarding", "uri: $uri")
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
