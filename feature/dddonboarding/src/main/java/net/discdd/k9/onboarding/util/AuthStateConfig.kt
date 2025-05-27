package net.discdd.k9.onboarding.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import net.discdd.k9.onboarding.repository.AuthRepository.AuthState

class AuthStateConfig(
    private val context: Context,
    private val dddDir: File = context.filesDir.resolve("ddd"),
    private val configFile: File = dddDir.resolve("auth.state"),
) {
    @Throws(IOException::class)
    fun writeState(state: AuthState, id: String? = null) {
        if (!dddDir.exists()) dddDir.mkdirs()
        FileOutputStream(configFile).use { os ->
            os.write("${state.name}\n${id ?: ""}".toByteArray())
        }
    }

    fun readState(): Pair<AuthState, String?> {
        if (!configFile.exists()) return Pair(AuthState.LOGGED_OUT, null)
        val stateAndId = configFile.readLines()

        return when (stateAndId[0]) {
            "PENDING" -> Pair(AuthState.PENDING, null)
            "LOGGED_IN" -> Pair(AuthState.LOGGED_IN, stateAndId[1])
            "LOGGED_OUT" -> Pair(AuthState.LOGGED_OUT, null)
            else -> Pair(AuthState.LOGGED_OUT, null)
        }
    }

    fun deleteState() {
        if (configFile.delete()) {
            Log.d("DDDOnboarding", "Deleted auth state config file successfully")
        }
        Log.d("DDDOnboarding", "auth state config file failed to delete")
    }
}
