package net.discdd.k9.onboarding.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.discdd.adapter.DDDClientAdapter
import net.discdd.app.k9.common.ControlAdu
import net.discdd.k9.onboarding.repository.AuthRepository.AuthState
import net.discdd.k9.onboarding.util.AuthStateConfig

private const val BUNDLE_CLIENT_PACKAGE = "net.discdd.bundleclient"
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

    override suspend fun checkClientStatus(): Boolean {
        return dddClientAdapter.getClientId() != null || isBundleClientInstalled(context)
    }

    private fun isBundleClientInstalled(context: Context): Boolean {
        val pm = context.packageManager
        return try {
            if (Build.VERSION.SDK_INT >= 33) {
                // ✅ use the nested flags type
                pm.getPackageInfo(
                    BUNDLE_CLIENT_PACKAGE,
                    PackageManager.PackageInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(BUNDLE_CLIENT_PACKAGE, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    override suspend fun getState(): Pair<AuthState, ControlAdu?> = withContext(Dispatchers.IO) {
        getAckAdu()?.run {
            val (adu, id) = this
            if (adu.success()) {
                authStateConfig.writeState(AuthState.LOGGED_IN, adu as ControlAdu)
            } else {
                authStateConfig.writeState(AuthState.ERROR, adu as ControlAdu)
            }
            dddClientAdapter.deleteReceivedAdusUpTo(id)
        }
        authStateConfig.readState()
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        authStateConfig.deleteState()
    }

    /**
     * this function will return the last seen EmailAck and the ID of the ADU to delete
     * to. THIS MAY BE LESS THAN THE LAST ACK ADU RECEIVED. If there are messages in
     * between, we don't want to delete the messages.
     */
    @Suppress("TooGenericExceptionCaught", "[NestedBlockDepth]")
    private fun getAckAdu(): Pair<ControlAdu.EmailAck, Long>? {
        var lastSeenAdu: ControlAdu.EmailAck? = null
        var lastSeenAduId = -1L
        var messageSeen = false
        dddClientAdapter.incomingAduIds?.forEach { i ->
            dddClientAdapter.receiveAdu(i)?.readAllBytes().apply {
                if (ControlAdu.isControlAdu(this)) {
                    lastSeenAdu = ControlAdu.fromBytes(this) as ControlAdu.EmailAck
                } else {
                    messageSeen = true
                }
                if (!messageSeen) {
                    lastSeenAduId = i
                }
            }
        }
        return lastSeenAdu?.let { Pair(it, lastSeenAduId) }
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
