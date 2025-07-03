package net.discdd.k9.onboarding.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import net.discdd.app.k9.common.ControlAdu
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

    fun readState(): Pair<AuthState, ControlAdu?> {
        if (!configFile.exists()) return Pair(AuthState.LOGGED_OUT, null)
        // the first line is the state, the rest are properties
        val stateAndId = configFile.readBytes()
        val lines = stateAndId.decodeToString().lines()
        val state = stateAndId.decodeToString().lines().first()
        val bytes = lines.drop(1).joinToString("\n").toByteArray()

        val adu = ControlAdu.fromBytes(bytes)

        return when (state) {
            "PENDING" -> Pair(AuthState.PENDING, null)
            "LOGGED_IN" -> Pair(AuthState.LOGGED_IN, adu)
            "LOGGED_OUT" -> Pair(AuthState.LOGGED_OUT, adu)
            else -> Pair(AuthState.LOGGED_OUT, adu)
        }
    }

    fun deleteState() {
        if (configFile.delete()) {
            Log.d("DDDOnboarding", "Deleted auth state config file successfully")
        }
        Log.d("DDDOnboarding", "auth state config file failed to delete")
    }
}
