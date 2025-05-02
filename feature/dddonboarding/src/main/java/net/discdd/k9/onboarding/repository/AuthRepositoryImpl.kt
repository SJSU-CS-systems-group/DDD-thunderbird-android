package net.discdd.k9.onboarding.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import net.discdd.k9.onboarding.model.AcknowledgementRegisterAdu
import net.discdd.k9.onboarding.model.Adu
import net.discdd.k9.onboarding.util.AuthStateConfig
import net.discdd.k9.onboarding.repository.AuthRepository.AuthState

class AuthRepositoryImpl(
    private val authStateConfig: AuthStateConfig,
    private val context: Context
): AuthRepository {
    private val RESOLVER_COLUMNS = arrayOf("data")
    override val CONTENT_URL: Uri = Uri.parse("content://net.discdd.provider.datastoreprovider/messages");

    override fun getState(): Pair<AuthState, net.discdd.k9.onboarding.model.AcknowledgementAdu?> {
        var state = authStateConfig.readState()
        if (state == AuthState.PENDING) {
            val ackAdu = getAckAdu() ?: return Pair(AuthState.PENDING, null)

            if (ackAdu.success) {
                setState(AuthState.LOGGED_IN)
                return Pair(AuthState.LOGGED_IN, ackAdu)
            }

        }

        authStateConfig.deleteState()
        return Pair(AuthState.LOGGED_OUT, null)
    }

    private fun getAckAdu(): net.discdd.k9.onboarding.model.AcknowledgementAdu? {
        val cursor = context.contentResolver.query(CONTENT_URL, null, null, null, null)
        var ack: net.discdd.k9.onboarding.model.AcknowledgementAdu? = null;
        var lastSeenAduId: String? = null;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                var data = cursor.getString(cursor.getColumnIndexOrThrow("data"))
                var id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
                lastSeenAduId = id
                Log.d("k9", "adu id: $id")
                // delete adu if exists
                if (data.startsWith("login-ack")){
                    ack = net.discdd.k9.onboarding.model.AcknowledgementLoginAdu.toAckLoginAdu(data)
                } else if (data.startsWith("register-ack")) {
                    ack = AcknowledgementRegisterAdu.toAckRegisterAdu(data)
                }
            } while (ack==null && cursor.moveToNext())
        }

        if (lastSeenAduId!=null) {
            context.contentResolver.delete(CONTENT_URL, "deleteAllADUsUpto", arrayOf(lastSeenAduId))
        }
        return ack;
    }

    override fun setState(state: AuthState){
        authStateConfig.writeState(state)
    }

    override fun insertAdu(adu: net.discdd.k9.onboarding.model.Adu): Boolean {
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

    override fun deleteState(): Boolean {
        return try {
            authStateConfig.deleteState()
            Log.d("DDDOnboarding", "Deleting Auth State")
            true
        } catch (e: Exception) {
            Log.e("DDDOnboarding", "Failed to delete state", e)
            false
        }
    }

    override fun deleteAuthAdu(): Boolean {
        return try {
            val cursor = context.contentResolver.query(CONTENT_URL, null, null, null, null)
            if (cursor != null) {
                Log.d("DDDOnboarding", "Found ${cursor.count} rows at $CONTENT_URL")

                val columnNames = cursor.columnNames
                Log.d("DDDOnboarding", "Columns: ${columnNames.joinToString()}")

                while (cursor.moveToNext()) {
                    val rowData = buildString {
                        columnNames.forEach { column ->
                            val value = cursor.getString(cursor.getColumnIndexOrThrow(column))
                            append("$column: $value, ")
                        }
                    }
                    Log.d("DDDOnboarding", "Row: $rowData")
                }

                cursor.close()
            } else {
                Log.d("DDDOnboarding", "Cursor is null at $CONTENT_URL")
            }

            val resolver = context.contentResolver
            val rowsDeleted = resolver.delete(CONTENT_URL, null, null)
            Log.d("DDDOnboarding", "Deleted $rowsDeleted ADU rows")
            rowsDeleted > 0
        } catch (e: Exception) {
            Log.e("DDDOnboarding", "Failed to delete ADU", e)
            false
        }
    }

}
