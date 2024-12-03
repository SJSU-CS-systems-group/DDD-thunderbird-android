package com.example.dddonboarding.model

import java.io.OutputStream

data class LoginAdu(
    val email: String,
    override val password: String
): Adu {
    override fun toByteArray(): ByteArray {
        return "login\n$email\n$password".toByteArray()
    }
}

data class AcknowledgementLoginAdu(
    override val email: String?,
    override val password: String?,
    override val success: Boolean
): AcknowledgementAdu {
    companion object {
        fun toAckLoginAdu(data: String): AcknowledgementLoginAdu? {
            val dataArr = data.split("\n")
            if (dataArr.size >= 2 || dataArr[0] != "login-ack") return null;

            if (dataArr[1] == "success") {
                return AcknowledgementLoginAdu(email = dataArr[2], password = dataArr[3], success = true)
            }
            return AcknowledgementLoginAdu(email = null, password = null, success = true)
        }
    }
}
