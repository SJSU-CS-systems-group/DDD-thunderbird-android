package net.discdd.k9.onboarding.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.discdd.adapter.DDDClientAdapter
import net.discdd.app.k9.common.ControlAdu
import net.discdd.k9.onboarding.repository.AuthRepository.AuthState
import net.discdd.k9.onboarding.util.AuthStateConfig

class AuthRepositoryImpl(
    private val authStateConfig: AuthStateConfig,
    private val context: Context,
) : AuthRepository {
    override var authRepositoryListener: AuthRepository.AuthRepositoryListener? = null
        set(value) {
            if (value == null) {
                // unregister if there is no listener
                dddClientAdapter.unregisterForAduAdditions()
            } else {
                // someone is listening, register for ADU additions
                dddClientAdapter.registerForAduAdditions()
            }
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

    override suspend fun logout() = withContext(Dispatchers.IO) {
        authStateConfig.deleteState()
    }

    @Suppress("TooGenericExceptionCaught")
    private fun getAckAdu(): Pair<ControlAdu.EmailAck, Long>? {
        var lastSeenAdu: ControlAdu.EmailAck? = null
        var lastSeenAduId = -1L
        for (i in dddClientAdapter.incomingAduIds ?: emptyList()) {
            dddClientAdapter.receiveAdu(i)?.readAllBytes().apply {
                try {
                    lastSeenAdu = ControlAdu.fromBytes(this) as ControlAdu.EmailAck
                } catch (e: Exception) {
                    Log.w("dddEmail", "Failed to read EmailAck for ID $i", e)
                }
                lastSeenAduId = i
            }
        }
        return lastSeenAdu?.let { Pair(it, lastSeenAduId) }
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
    override suspend fun insertAdu(adu: ControlAdu, state: AuthState?): Boolean = withContext(Dispatchers.IO) {
        try {
            if (state != null) {
                authStateConfig.writeState(state, adu)
            }
            dddClientAdapter.createAduToSend().use { out ->
                out.write(adu.toBytes())
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
