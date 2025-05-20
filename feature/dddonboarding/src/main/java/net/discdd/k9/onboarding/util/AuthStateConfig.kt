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
    private fun createConfig() {
        if (!dddDir.exists()) {
            dddDir.mkdirs()
        }
        configFile.createNewFile()
    }

    @Throws(IOException::class)
    fun writeState(state: AuthState) {
        createConfig()
        var os = FileOutputStream(configFile)
        os.write(state.name.toByteArray())
        os.close()
    }

    fun readState(): AuthState {
        if (!configFile.exists()) return AuthState.LOGGED_OUT
        var state: String? = configFile.readLines().firstOrNull()

        return when (state) {
            "PENDING" -> AuthState.PENDING
            "LOGGED_IN" -> AuthState.LOGGED_IN
            "LOGGED_OUT" -> AuthState.LOGGED_OUT
            else -> AuthState.LOGGED_OUT

        }
    }

    fun deleteState() {
        if (configFile.delete()) {
            Log.d("DDDOnboarding", "Deleted auth state config file successfully")
        }
        Log.d("DDDOnboarding", "auth state config file failed to delete")
    }
}
