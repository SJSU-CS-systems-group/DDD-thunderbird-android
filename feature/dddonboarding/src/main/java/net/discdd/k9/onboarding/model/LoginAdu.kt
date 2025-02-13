package net.discdd.k9.onboarding.model

data class LoginAdu(
    val email: String,
    override val password: String
): net.discdd.k9.onboarding.model.Adu {
    override fun toByteArray(): ByteArray {
        return "login\n$email\n$password".toByteArray()
    }
}

data class AcknowledgementLoginAdu(
    override val email: String?,
    override val password: String?,
    override val success: Boolean,
    override val message: String?
): net.discdd.k9.onboarding.model.AcknowledgementAdu {
    companion object {
        fun toAckLoginAdu(data: String): net.discdd.k9.onboarding.model.AcknowledgementLoginAdu? {
            val dataArr = data.split("\n")
            if (dataArr.size < 4 || dataArr[0] != "login-ack") return null;

            if (dataArr[1] == "success") {
                return net.discdd.k9.onboarding.model.AcknowledgementLoginAdu(
                    email = dataArr[3],
                    password = dataArr[4],
                    success = true,
                    message = null
                )
            }
            return net.discdd.k9.onboarding.model.AcknowledgementLoginAdu(
                email = null,
                password = null,
                success = false,
                message = dataArr[2]
            )
        }
    }
}
