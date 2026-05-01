package com.example.pwunsafe.credentials

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.exceptions.CreateCredentialUnknownException
import androidx.credentials.provider.PendingIntentHandler
import com.example.pwunsafe.data.model.Credential
import com.example.pwunsafe.data.model.PasskeyData
import com.example.pwunsafe.data.repository.CredentialRepository
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.util.*

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class CreateCredentialActivity : ComponentActivity() {

    private val repository: CredentialRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val providerRequest = PendingIntentHandler.retrieveProviderCreateCredentialRequest(intent)
        val createRequest = providerRequest?.callingRequest as? androidx.credentials.CreatePublicKeyCredentialRequest

        if (createRequest == null) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        try {
            val requestJson = JSONObject(createRequest.requestJson)
            val rpJson = requestJson.getJSONObject("rp")
            val rpId = rpJson.getString("id")
            val rpName = rpJson.optString("name", rpId)
            val userJson = requestJson.getJSONObject("user")
            val userId = userJson.getString("id")
            val userName = userJson.optString("name", "user")

            val (credentialIdBytes, privateKeyPkcs8Base64, coseBase64) = WebAuthnHelper.generateKeyPair()
            val credentialIdBase64Url = WebAuthnHelper.base64Url(credentialIdBytes)
            val coseBytes = Base64.getDecoder().decode(coseBase64)

            val authenticatorData = WebAuthnHelper.buildAuthenticatorDataForAttestation(
                rpId = rpId,
                credentialId = credentialIdBytes,
                cosePublicKey = coseBytes,
            )

            val origin = "android:apk-key-hash:${packageName}"
            val clientDataJson = """{"type":"webauthn.create","challenge":"${extractChallenge(requestJson)}","origin":"$origin","androidPackageName":"$packageName"}"""
            val clientDataJsonBytes = clientDataJson.toByteArray()

            val attestationResponseJson = JSONObject().apply {
                put("id", credentialIdBase64Url)
                put("rawId", credentialIdBase64Url)
                put("type", "public-key")
                put("response", JSONObject().apply {
                    put("clientDataJSON", WebAuthnHelper.base64Url(clientDataJsonBytes))
                    put("attestationObject", WebAuthnHelper.base64Url(buildNoneAttestationObject(authenticatorData)))
                })
            }.toString()

            val passkey = PasskeyData(
                rpId = rpId,
                credentialId = credentialIdBase64Url,
                privateKeyPkcs8 = privateKeyPkcs8Base64,
                publicKeyCose = coseBase64,
                userHandle = userId,
                signCount = 0,
            )
            val credential = Credential(
                id = java.util.UUID.randomUUID().toString(),
                service = rpName,
                packageName = providerRequest.callingAppInfo.packageName,
                username = userName,
                passkey = passkey,
            )

            runBlocking { repository.add(credential) }

            val resultData = android.content.Intent()
            PendingIntentHandler.setCreateCredentialResponse(
                resultData,
                CreatePublicKeyCredentialResponse(attestationResponseJson),
            )
            setResult(RESULT_OK, resultData)
        } catch (e: Exception) {
            val resultData = android.content.Intent()
            PendingIntentHandler.setCreateCredentialException(
                resultData,
                CreateCredentialUnknownException(e.message ?: "Unknown error"),
            )
            setResult(RESULT_OK, resultData)
        }

        finish()
    }

    private fun extractChallenge(requestJson: JSONObject): String =
        requestJson.optString("challenge", "")

    /**
     * Build a minimal CBOR-encoded attestation object with "none" attestation format.
     * Structure: {"fmt": "none", "attStmt": {}, "authData": <bytes>}
     */
    private fun buildNoneAttestationObject(authenticatorData: ByteArray): ByteArray {
        // CBOR map with 3 entries: {"fmt":"none","attStmt":{},"authData":<bytes>}
        return buildList<Byte> {
            // map(3)
            add(0xA3.toByte())
            // "fmt": "none"
            addAll(cborText("fmt"))
            addAll(cborText("none"))
            // "attStmt": {}
            addAll(cborText("attStmt"))
            add(0xA0.toByte()) // empty map
            // "authData": bytes
            addAll(cborText("authData"))
            addAll(cborBytes(authenticatorData))
        }.toByteArray()
    }

    private fun cborText(s: String): List<Byte> {
        val bytes = s.toByteArray()
        val header = if (bytes.size <= 23) byteArrayOf((0x60 or bytes.size).toByte())
        else byteArrayOf(0x78.toByte(), bytes.size.toByte())
        return (header + bytes).toList()
    }

    private fun cborBytes(data: ByteArray): List<Byte> {
        val header = when {
            data.size <= 23 -> byteArrayOf((0x40 or data.size).toByte())
            data.size <= 0xFF -> byteArrayOf(0x58.toByte(), data.size.toByte())
            else -> byteArrayOf(0x59.toByte(), (data.size shr 8).toByte(), data.size.toByte())
        }
        return (header + data).toList()
    }
}
