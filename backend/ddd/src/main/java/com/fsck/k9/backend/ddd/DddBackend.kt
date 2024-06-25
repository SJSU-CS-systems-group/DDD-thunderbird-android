package com.fsck.k9.backend.ddd

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import com.fsck.k9.backend.api.Backend
import com.fsck.k9.backend.api.BackendPusher
import com.fsck.k9.backend.api.BackendPusherCallback
import com.fsck.k9.backend.api.BackendStorage
import com.fsck.k9.backend.api.SyncConfig
import com.fsck.k9.backend.api.SyncListener
//import com.fsck.k9.backend.pop3.CommandRefreshFolderList
import com.fsck.k9.mail.BodyFactory
import com.fsck.k9.mail.Flag
import com.fsck.k9.mail.Message
import com.fsck.k9.mail.Part
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream


class DddBackend(
    context: Context,
    accountName: String,
    backendStorage: BackendStorage,
) : Backend {
//    private val commandRefreshFolderList = CommandRefreshFolderList(backendStorage)
    private val RESOLVER_COLUMNS = arrayOf("data")
    private val context = context

    companion object {
        val CONTENT_URL: Uri = Uri.parse("content://com.ddd.provider.datastoreprovider/mails")
    }
    override val supportsFlags = false
    override val supportsExpunge = false
    override val supportsMove = false
    override val supportsCopy = false
    override val supportsUpload = false
    override val supportsTrashFolder = false
    override val supportsSearchByDate = false
    override val supportsFolderSubscriptions = false
    override val isPushCapable = false

    override fun refreshFolderList() {
//        commandRefreshFolderList.refreshFolderList()
    }

    override fun sync(folderServerId: String, syncConfig: SyncConfig, listener: SyncListener) {
//        pop3Sync.sync(folderServerId, syncConfig, listener)
    }

    override fun downloadMessage(syncConfig: SyncConfig, folderServerId: String, messageServerId: String) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun downloadMessageStructure(folderServerId: String, messageServerId: String) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun downloadCompleteMessage(folderServerId: String, messageServerId: String) {
//        commandDownloadMessage.downloadCompleteMessage(folderServerId, messageServerId)
    }

    override fun setFlag(folderServerId: String, messageServerIds: List<String>, flag: Flag, newState: Boolean) {
//        commandSetFlag.setFlag(folderServerId, messageServerIds, flag, newState)
    }

    override fun markAllAsRead(folderServerId: String) {
        throw UnsupportedOperationException("not supported")
    }

    override fun expunge(folderServerId: String) {
        throw UnsupportedOperationException("not supported")
    }

    override fun deleteMessages(folderServerId: String, messageServerIds: List<String>) {
//        commandSetFlag.setFlag(folderServerId, messageServerIds, Flag.DELETED, true)
    }

    override fun deleteAllMessages(folderServerId: String) {
        throw UnsupportedOperationException("not supported")
    }

    override fun moveMessages(
        sourceFolderServerId: String,
        targetFolderServerId: String,
        messageServerIds: List<String>,
    ): Map<String, String>? {
        throw UnsupportedOperationException("not supported")
    }

    override fun copyMessages(
        sourceFolderServerId: String,
        targetFolderServerId: String,
        messageServerIds: List<String>,
    ): Map<String, String>? {
        throw UnsupportedOperationException("not supported")
    }

    override fun moveMessagesAndMarkAsRead(
        sourceFolderServerId: String,
        targetFolderServerId: String,
        messageServerIds: List<String>,
    ): Map<String, String>? {
        throw UnsupportedOperationException("not supported")
    }

    override fun search(
        folderServerId: String,
        query: String?,
        requiredFlags: Set<Flag>?,
        forbiddenFlags: Set<Flag>?,
        performFullTextSearch: Boolean,
    ): List<String> {
        throw UnsupportedOperationException("not supported")
    }

    override fun fetchPart(folderServerId: String, messageServerId: String, part: Part, bodyFactory: BodyFactory) {
        throw UnsupportedOperationException("not supported")
    }

    override fun findByMessageId(folderServerId: String, messageId: String): String? {
        return null
    }

    override fun uploadMessage(folderServerId: String, message: Message): String? {
        throw UnsupportedOperationException("not supported")
    }

    override fun sendMessage(message: Message) {
//        smtpTransport.sendMessage(message)
        if (message.size > 0) {
            return
        }
        val sampleString: String = "This is a sample string"
        val byteArrayOutputStream = ByteArrayOutputStream()
        ObjectOutputStream(byteArrayOutputStream).use { it.writeObject(sampleString) }

        val values = ContentValues().apply {
            put(RESOLVER_COLUMNS[0], byteArrayOutputStream.toByteArray())
        }

        try {
            val resolver = context.contentResolver
            val uri = resolver.insert(CONTENT_URL, values)
            if (uri == null) {
                throw Exception("Message not inserted")
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun createPusher(callback: BackendPusherCallback): BackendPusher {
        throw UnsupportedOperationException("not implemented")
    }
}
