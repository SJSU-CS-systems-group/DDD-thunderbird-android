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
    fun writeState(state: AuthState, adu: ControlAdu) {
        if (!dddDir.exists()) dddDir.mkdirs()
        FileOutputStream(configFile).use { os ->
            os.write("${state.name}\n".toByteArray())
            os.write(adu.toBytes() ?: byteArrayOf())
        }
    }

    fun readState(): Pair<AuthState, ControlAdu?> {
        if (!configFile.exists()) return Pair(AuthState.LOGGED_OUT, null)
        // the first line is the state, the rest are properties
        val stateAndId = configFile.readBytes()
        val lines = stateAndId.decodeToString().lines()
        val state = lines.first()
        val bytes = lines.drop(1).joinToString("\n").toByteArray()
        val adu = if (ControlAdu.isControlAdu(bytes))ControlAdu.fromBytes(bytes) else null
        return Pair(AuthState.valueOf(state), adu)
    }

    fun deleteState() {
        if (configFile.delete()) {
            Log.d("DDDOnboarding", "Deleted auth state config file successfully")
        }
        Log.d("DDDOnboarding", "auth state config file failed to delete")
    }
}
