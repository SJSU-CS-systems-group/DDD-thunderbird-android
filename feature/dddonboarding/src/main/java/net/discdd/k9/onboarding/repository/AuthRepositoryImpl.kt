package net.discdd.k9.onboarding.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.discdd.adapter.DDDClientAdapter
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
    }

    override var authRepositoryListener: AuthRepository.AuthRepositoryListener? = null
        set(value) {
            if (value == null) dddClientAdapter.unregisterForAduAdditions() else dddClientAdapter.registerForAduAdditions()
            field = value
        }

    private val dddClientAdapter: DDDClientAdapter by lazy {
        DDDClientAdapter(context, {
            authRepositoryListener?.onAuthStateChanged()
        })
    }

    override suspend fun getState(): Pair<AuthState, ControlAdu?> = withContext(Dispatchers.IO) {
        getAckAdu()?.run {
            val (adu, id) = this
            if (adu.success()) {
                authStateConfig.writeState(AuthState.LOGGED_IN, adu as ControlAdu)
            } else {
                authStateConfig.writeState(AuthState.ERROR, adu as ControlAdu)
            }
            deleteUptoAduId(id)
        }
        authStateConfig.readState()
    }

    override suspend fun logout() = withContext(Dispatchers.IO){
        authStateConfig.writeState(AuthState.LOGGED_OUT)
    }

    private fun getAckAdu(): Pair<ControlAdu.EmailAck, Long>? {
        var lastSeenAdu: ControlAdu.EmailAck? = null
        var lastSeenAduId = -1L
        for (i in dddClientAdapter.incomingAduIds ?: emptyList()) {
            val adu = dddClientAdapter.receiveAdu(i)
            if (adu is ControlAdu.EmailAck) {
                lastSeenAdu = adu
            } else {
                Log.w("dddEmail", "Received non-EmailAck ADU: $adu")
            }
            lastSeenAduId = i
        }
        return lastSeenAdu?.let {Pair(it, lastSeenAduId)}
    }

    private fun deleteUptoAduId(aduId: Long) {
     dddClientAdapter.deleteReceivedAdusUpTo(aduId)
    }

    override suspend fun getId(): String? = withContext(Dispatchers.IO) {
        authStateConfig.readState().second?.let {
            if (it is ControlAdu.EmailAck) {
                it.email()
            } else {
                null
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun insertAdu(adu: ControlAdu): Boolean = withContext(Dispatchers.IO) {
        try {
            dddClientAdapter.createAduToSend().use { out ->
                out.write(adu.toBytes())
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
