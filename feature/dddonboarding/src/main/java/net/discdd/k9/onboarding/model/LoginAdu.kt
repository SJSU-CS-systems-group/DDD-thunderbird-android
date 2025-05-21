package net.discdd.k9.onboarding.model

import net.discdd.k9.onboarding.model.AcknowledgementAdu.Companion.EMAIL_INDEX
import net.discdd.k9.onboarding.model.AcknowledgementAdu.Companion.LOGIN_ACK_ADU
import net.discdd.k9.onboarding.model.AcknowledgementAdu.Companion.MESSAGE_INDEX
import net.discdd.k9.onboarding.model.AcknowledgementAdu.Companion.PASSWORD_INDEX
import net.discdd.k9.onboarding.model.AcknowledgementAdu.Companion.SUCCESS_ACK_ADU
import net.discdd.k9.onboarding.model.AcknowledgementAdu.Companion.SUCCESS_INDEX
import net.discdd.k9.onboarding.model.AcknowledgementAdu.Companion.TYPE_INDEX

data class LoginAdu(
    val email: String,
    override val password: String,
) : Adu {
    override fun toByteArray(): ByteArray {
        return "login\n$email\n$password".toByteArray()
    }
}

data class AcknowledgementLoginAdu(
    override val email: String?,
    override val password: String?,
    override val success: Boolean,
    override val message: String?,
) : AcknowledgementAdu {
    companion object {
        fun toAckLoginAdu(data: String): AcknowledgementLoginAdu? {
            val dataArr = data.split("\n")
            if (dataArr.size < PASSWORD_INDEX || dataArr[TYPE_INDEX] != LOGIN_ACK_ADU) return null

            return if (dataArr[SUCCESS_INDEX] == SUCCESS_ACK_ADU) {
                AcknowledgementLoginAdu(
                    email = dataArr[EMAIL_INDEX],
                    password = dataArr[PASSWORD_INDEX],
                    success = true,
                    message = null,
                )
            } else {
                AcknowledgementLoginAdu(
                    email = null,
                    password = null,
                    success = false,
                    message = dataArr[MESSAGE_INDEX],
                )
            }
        }
    }
}
