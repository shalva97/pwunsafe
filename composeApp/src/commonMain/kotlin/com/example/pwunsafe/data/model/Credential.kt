package com.example.pwunsafe.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Credential(
    val id: String,
    val service: String,
    val packageName: String? = null,
    val domain: String? = null,
    val username: String,
    val password: String? = null,
    val passkey: PasskeyData? = null,
)

@Serializable
data class PasskeyData(
    val rpId: String,
    val credentialId: String,
    val privateKeyPkcs8: String,
    val publicKeyCose: String,
    val userHandle: String,
    val signCount: Int = 0,
)
