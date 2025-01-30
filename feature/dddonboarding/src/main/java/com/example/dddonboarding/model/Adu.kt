package com.example.dddonboarding.model

interface Adu {
    val password: String
    fun toByteArray(): ByteArray
}

interface AcknowledgementAdu {
    val email: String?
    val password: String?
    val success:Boolean
    val message: String?
}
