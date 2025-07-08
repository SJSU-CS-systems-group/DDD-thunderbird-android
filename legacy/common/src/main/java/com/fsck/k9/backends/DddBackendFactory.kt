package com.fsck.k9.backends

import android.content.Context
import com.fsck.k9.Account
import com.fsck.k9.Preferences
import com.fsck.k9.backend.BackendFactory
import com.fsck.k9.backend.api.Backend
import com.fsck.k9.backend.ddd.DddBackend
import com.fsck.k9.mailstore.K9BackendStorageFactory

class DddBackendFactory(
    override val context: Context,
    private val backendStorageFactory: K9BackendStorageFactory,
    private val preferences: Preferences,
) : BackendFactory, DddBackend.DDDFactory {
    override fun createBackend(account: Account): Backend {
        val backendStorage = backendStorageFactory.createBackendStorage(account)
        val uuid = account.uuid
        return DddBackend(this, uuid, backendStorage)
    }

    override fun changeEmailAddress(uuid: String, email: String) {
        val account = preferences.getAccount(uuid)
        account?.apply {
            this.email = email
            preferences.saveAccount(account)
        }
    }
}
