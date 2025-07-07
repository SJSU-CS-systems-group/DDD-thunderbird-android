package com.fsck.k9.backend.ddd

import android.content.Context
import com.fsck.k9.backend.api.Backend
import com.fsck.k9.backend.api.BackendFolder
import com.fsck.k9.backend.api.BackendPusher
import com.fsck.k9.backend.api.BackendPusherCallback
import com.fsck.k9.backend.api.BackendStorage
import com.fsck.k9.backend.api.FolderInfo
import com.fsck.k9.backend.api.SyncConfig
import com.fsck.k9.backend.api.SyncListener
import com.fsck.k9.backend.api.updateFolders
import com.fsck.k9.logging.Timber.logger
import com.fsck.k9.mail.BodyFactory
import com.fsck.k9.mail.Flag
import com.fsck.k9.mail.Message
import com.fsck.k9.mail.MessageDownloadState
import com.fsck.k9.mail.Part
import com.fsck.k9.mail.internet.MimeMessage
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.discdd.adapter.DDDClientAdapter
import net.discdd.app.k9.common.ControlAdu
import okio.buffer
import okio.source

// we CONTROL_HEADER sie plus we need 1 byte for the \n and 3 just in case :)
const val CONTROL_HEADER_PEEK_SIZE = ControlAdu.CONTROL_HEADER.length + 4

@Suppress("UnusedParameter", "UnusedPrivateProperty", "TooManyFunctions")
class DddBackend(
    val factory: DDDFactory,
    val uuid: String,
    private val backendStorage: BackendStorage,
) : Backend {
    interface DDDFactory {
        val context: Context
        fun changeEmailAddress(uuid: String, email: String)
    }
    private lateinit var dddAdapter: DDDClientAdapter
    private val messageStoreInfo by lazy { readMessageStoreInfo() }

    override val supportsFlags = false
    override val supportsExpunge = false
    override val supportsMove = false
    override val supportsCopy = false
    override val supportsUpload = false
    override val supportsTrashFolder = false
    override val supportsSearchByDate = false
    override val supportsFolderSubscriptions = false
    override val isPushCapable = true

    // to know if we should register when the backend is created
    private var dddAdapterShouldRegisterForAduAdditions = false

    init {
        CoroutineScope(Dispatchers.Main).launch {
            dddAdapter = DDDClientAdapter(
                factory.context,
            ) {
                logger.i("Notified of new ADU addition")
                val folderServerId = backendStorage.getFolderServerIds().firstOrNull()
                callback?.onPushEvent(folderServerId ?: "inbox")
            }
            if (dddAdapterShouldRegisterForAduAdditions) {
                dddAdapter.registerForAduAdditions()
            }
        }
    }

    override fun removeBackend() {
        val dddDir: File = factory.context.filesDir.resolve("ddd")
        val configFile: File = dddDir.resolve("auth.state")
        configFile.delete()
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
    @Override
    private fun loadMessage(aduId: String, messageInputStream: InputStream): Message {
        val mimeMessage = MimeMessage.parseMimeMessage(messageInputStream, false).apply { uid = aduId }
        return mimeMessage
    }

    @SuppressWarnings("TooGenericExceptionCaught")
    @Override
    override fun sync(folderServerId: String, syncConfig: SyncConfig, listener: SyncListener) {
        listener.syncStarted(folderServerId)
        val folderData = messageStoreInfo["inbox"]
        if (folderData == null) {
            listener.syncFailed(folderServerId, "Folder $folderServerId doesn't exist", null)
            return
        }

        val backendFolder = backendStorage.getFolder(folderServerId)

        try {
            val mailIdsToSync = dddAdapter.incomingAduIds
            var lastMsgServerIdProcessed = 0L
            for (aduId in mailIdsToSync) {
                val peekableInputStream = BufferedInputStream(dddAdapter.receiveAdu(aduId))
                if (isControlAdu(peekableInputStream)) {
                    try {
                        val controlAdu = ControlAdu.fromBytes(peekableInputStream.readAllBytes())
                        logger.w("Received control ADU: $controlAdu")
                        if (controlAdu is ControlAdu.EmailAck) {
                            if (controlAdu.success()) {
                                logger.i("Received EmailAck email ${controlAdu.email()} changing address")
                                factory.changeEmailAddress(uuid, controlAdu.email())
                            }
                        } else {
                            logger.w("Received unexpected control ADU type: ${controlAdu.javaClass.simpleName}")
                        }
                    } catch (e: Exception) {
                        logger.e(e, "Failed to parse control ADU for ID $aduId")
                    }
                } else {
                    val aduIdString = aduId.toString()
                    val message = loadMessage(aduIdString, peekableInputStream)
                    backendFolder.saveMessage(message, MessageDownloadState.FULL)
                    listener.syncNewMessage(folderServerId, aduIdString, isOldMessage = false)
                }
                if (lastMsgServerIdProcessed < aduId) {
                    lastMsgServerIdProcessed = aduId
                }
            }

            dddAdapter.deleteReceivedAdusUpTo(lastMsgServerIdProcessed)
            backendFolder.setMoreMessages(BackendFolder.MoreMessages.FALSE)
            listener.syncFinished(folderServerId)
        } catch (e: Exception) {
            logger.e(e, "Unable to complete Inbox folder sync from the bundle client")
            listener.syncFailed(folderServerId, "Unable to complete Inbox folder sync from the bundle client", e)
        }
    }

    private fun isControlAdu(peekableInputStream: BufferedInputStream): Boolean {
        peekableInputStream.mark(CONTROL_HEADER_PEEK_SIZE)
        val header = ByteArray(CONTROL_HEADER_PEEK_SIZE)
        peekableInputStream.read(header)
        peekableInputStream.reset()
        return ControlAdu.isControlAdu(header)
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

    var callback: BackendPusherCallback? = null
    override fun createPusher(callback: BackendPusherCallback): BackendPusher {
        this.callback = callback
        return object : BackendPusher {
            override fun start() {
                logger.i("DddBackend - Starting pusher for DDD backend")
                dddAdapterShouldRegisterForAduAdditions = true
                if (::dddAdapter.isInitialized) {
                    dddAdapter.registerForAduAdditions()
                }
            }

            @Suppress("EmptyFunctionBlock")
            override fun updateFolders(folderServerIds: Collection<String>) {
            }

            override fun stop() {
                logger.i("DddBackend - Stopped pusher for DDD backend")
                dddAdapterShouldRegisterForAduAdditions = false
                if (::dddAdapter.isInitialized) {
                    dddAdapter.unregisterForAduAdditions()
                }
            }

            @Suppress("EmptyFunctionBlock")
            override fun reconnect() {
            }
        }
    }
}
