package net.discdd.k9.onboarding.model

import net.discdd.k9.onboarding.model.AcknowledgementAdu.Companion.EMAIL_INDEX
import net.discdd.k9.onboarding.model.AcknowledgementAdu.Companion.MESSAGE_INDEX
import net.discdd.k9.onboarding.model.AcknowledgementAdu.Companion.PASSWORD_INDEX
import net.discdd.k9.onboarding.model.AcknowledgementAdu.Companion.REGISTER_ACK_ADU
import net.discdd.k9.onboarding.model.AcknowledgementAdu.Companion.SUCCESS_ACK_ADU

data class RegisterAdu(
    val prefix1: String,
    val prefix2: String,
    val prefix3: String,
    val suffix1: String,
    val suffix2: String,
    val suffix3: String,
    override val password: String,
) : Adu {
    override fun toByteArray(): ByteArray {
        return "register\n$prefix1,$prefix2,$prefix3\n$suffix1,$suffix2,$suffix3\n$password".toByteArray()
    }
}

data class AcknowledgementRegisterAdu(
    override val email: String?,
    override val password: String?,
    override val success: Boolean,
    override val message: String?,
) : AcknowledgementAdu {
    companion object {
        fun toAckRegisterAdu(data: String): AcknowledgementRegisterAdu? {
            val dataArr = data.split("\n")
            if (dataArr.size < PASSWORD_INDEX || dataArr[0] != REGISTER_ACK_ADU) return null

            return if (dataArr[1] == SUCCESS_ACK_ADU) {
                AcknowledgementRegisterAdu(
                    email = dataArr[EMAIL_INDEX],
                    password = dataArr[PASSWORD_INDEX],
                    success = true,
                    message = null,
                )
            } else {
                AcknowledgementRegisterAdu(
                    email = null,
                    password = null,
                    success = false,
                    message = dataArr[MESSAGE_INDEX],
                )
            }
        }
    }
}
