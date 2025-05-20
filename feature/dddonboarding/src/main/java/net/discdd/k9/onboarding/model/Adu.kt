package net.discdd.k9.onboarding.model

interface Adu {
    val password: String
    fun toByteArray(): ByteArray
}

interface AcknowledgementAdu {
    companion object {
        const val LOGIN_ACK_ADU = "login-ack"
        const val REGISTER_ACK_ADU = "register-ack"
        const val SUCCESS_ACK_ADU = "success"
        const val ERROR_ACK_ADU = "error"
        const val EMAIL_INDEX = 3
        const val PASSWORD_INDEX = 4
        const val MESSAGE_INDEX = 2
        const val SUCCESS_INDEX = 1
        const val TYPE_INDEX = 0

    }
    val email: String?
    val password: String?
    val success: Boolean
    val message: String?
}
