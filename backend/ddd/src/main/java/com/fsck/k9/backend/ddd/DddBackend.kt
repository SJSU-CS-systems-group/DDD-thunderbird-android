package com.fsck.k9.backend.ddd

import android.content.Context
import android.util.Log
import com.fsck.k9.backend.api.Backend
import com.fsck.k9.backend.api.BackendFolder
import com.fsck.k9.backend.api.BackendPusher
import com.fsck.k9.backend.api.BackendPusherCallback
import com.fsck.k9.backend.api.BackendStorage
import com.fsck.k9.backend.api.FolderInfo
import com.fsck.k9.backend.api.SyncConfig
import com.fsck.k9.backend.api.SyncListener
import com.fsck.k9.backend.api.updateFolders
import com.fsck.k9.mail.BodyFactory
import com.fsck.k9.mail.Flag
import com.fsck.k9.mail.Message
import com.fsck.k9.mail.MessageDownloadState
import com.fsck.k9.mail.Part
import com.fsck.k9.mail.internet.MimeMessage
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.discdd.adapter.DDDClientAdapter
import okio.buffer
import okio.source

class DddBackend(
    context: Context,
    accountName: String,
    backendStorage: BackendStorage,
) : Backend {
    private lateinit var dddAdapter: DDDClientAdapter
    private val messageStoreInfo by lazy { readMessageStoreInfo() }
    private val backendStorage = backendStorage

    override val supportsFlags = false
    override val supportsExpunge = false
    override val supportsMove = false
    override val supportsCopy = false
    override val supportsUpload = false
    override val supportsTrashFolder = false
    override val supportsSearchByDate = false
    override val supportsFolderSubscriptions = false
    override val isPushCapable = false

    init {
        CoroutineScope(Dispatchers.Main).launch {
            // this disgusting reflection is needed due to build problems trying to
            // declare a dependency on androidx.lifecycle
            val plo = "androidx.lifecycle.ProcessLifecycleOwner"
            val lifecycleOwner = Class.forName(plo).getMethod("get").invoke(null)
            val lifecycle = lifecycleOwner.javaClass.getField("lifecycle").get(lifecycleOwner)
            dddAdapter = Class.forName("net.discdd.adapter.DDDClientAdapter")
                .constructors[0].newInstance(context, lifecycle, {}) as DDDClientAdapter
        }
    }

    override fun refreshFolderList() {
        val localFolderServerIds = backendStorage.getFolderServerIds().toSet()

        backendStorage.updateFolders {
            val remoteFolderServerIds = messageStoreInfo.keys
            val foldersServerIdsToCreate = remoteFolderServerIds - localFolderServerIds
            val foldersToCreate = foldersServerIdsToCreate.mapNotNull { folderServerId ->
                messageStoreInfo[folderServerId]?.let { folderData ->
                    FolderInfo(folderServerId, folderData.name, folderData.type)
                }
            }
            createFolders(foldersToCreate)

            val folderServerIdsToRemove = (localFolderServerIds - remoteFolderServerIds).toList()
            deleteFolders(folderServerIdsToRemove)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun readMessageStoreInfo(): MessageStoreInfo {
        return getResourceAsStream("/contents_ddd.json").source().buffer().use { bufferedSource ->
            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter<MessageStoreInfo>()
            adapter.fromJson(bufferedSource)
        } ?: error("Couldn't read message store info for ddd")
    }

    private fun getResourceAsStream(name: String): InputStream {
        return DddBackend::class.java.getResourceAsStream(name) ?: error("Resource '$name' not found")
    }

    @Throws(NullPointerException::class)
    private fun loadMessage(folderServerId: String, messageServerId: String): Message {
        val aduId = messageServerId.toLong()

        Log.d("loadMsg ", messageServerId)

        val messageBytes: ByteArray = dddAdapter.receiveAdu(aduId).use { it.readAllBytes() }

        val inputStream = messageBytes.inputStream()
        val mimeMessage = MimeMessage.parseMimeMessage(inputStream, false).apply { uid = messageServerId }

        return mimeMessage
    }

    override fun sync(folderServerId: String, syncConfig: SyncConfig, listener: SyncListener) {
        listener.syncStarted(folderServerId)
        val folderData = messageStoreInfo["inbox"]
        if (folderData == null) {
            listener.syncFailed(folderServerId, "Folder $folderServerId doesn't exist", null)
            return
        }

        val backendFolder = backendStorage.getFolder(folderServerId)

        try {
            // TO-DO:
            // we might need to delete mails one at a time, after calling the saveMessage.
            // This implementation might process the same message multiple times
            val mailIdsToSync = dddAdapter.getIncomingAduIds()
            var lastMsgServerIdProcessed = 0L
            for (messageServerId in mailIdsToSync) {
                val message = loadMessage(folderServerId, messageServerId.toString())
                backendFolder.saveMessage(message, MessageDownloadState.FULL)
                listener.syncNewMessage(folderServerId, messageServerId.toString(), isOldMessage = false)
                val msId = messageServerId
                if (lastMsgServerIdProcessed < msId) {
                    lastMsgServerIdProcessed = msId
                }
            }

            dddAdapter.deleteReceivedAdusUpTo(lastMsgServerIdProcessed)
            backendFolder.setMoreMessages(BackendFolder.MoreMessages.FALSE)
            listener.syncFinished(folderServerId)
        } catch (e: Exception) {
            e.printStackTrace()
            listener.syncFailed(folderServerId, "Unable to complete Inbox folder sync from the bundle client", e)
        }
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

    private fun createNewServerId() = UUID.randomUUID().toString()

    override fun moveMessages(
        sourceFolderServerId: String,
        targetFolderServerId: String,
        messageServerIds: List<String>,
    ): Map<String, String> {
        // We do just enough to simulate a successful operation on the server.
        return messageServerIds.associateWith { createNewServerId() }
    }

    override fun moveMessagesAndMarkAsRead(
        sourceFolderServerId: String,
        targetFolderServerId: String,
        messageServerIds: List<String>,
    ): Map<String, String> {
        // We do just enough to simulate a successful operation on the server.
        return messageServerIds.associateWith { createNewServerId() }
    }

    override fun copyMessages(
        sourceFolderServerId: String,
        targetFolderServerId: String,
        messageServerIds: List<String>,
    ): Map<String, String> {
        // We do just enough to simulate a successful operation on the server.
        return messageServerIds.associateWith { createNewServerId() }
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
        return createNewServerId()
    }

    override fun sendMessage(message: Message) {
//        smtpTransport.sendMessage(message)
        if (message.size > 0) {
            return
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        message.writeTo(byteArrayOutputStream)

        dddAdapter.createAduToSend().use { os ->
            os.write(byteArrayOutputStream.toByteArray())
        }
    }

    override fun createPusher(callback: BackendPusherCallback): BackendPusher {
        throw UnsupportedOperationException("not implemented")
    }
}
