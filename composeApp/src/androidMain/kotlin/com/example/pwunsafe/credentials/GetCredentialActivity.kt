package com.example.pwunsafe.credentials

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialUnknownException
import androidx.credentials.provider.PendingIntentHandler
import com.example.pwunsafe.data.repository.CredentialRepository
import org.json.JSONObject
import org.koin.android.ext.android.inject

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class GetCredentialActivity : ComponentActivity() {

    private val repository: CredentialRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val credentialId = intent.getStringExtra(EXTRA_CREDENTIAL_ID)
        val requestJsonStr = intent.getStringExtra(EXTRA_REQUEST_JSON)

        if (credentialId == null || requestJsonStr == null) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        val cred = repository.credentials.value.firstOrNull { it.id == credentialId }
        val passkey = cred?.passkey

        if (passkey == null) {
            val errorData = android.content.Intent()
            PendingIntentHandler.setGetCredentialException(
                errorData,
                GetCredentialUnknownException("Credential not found"),
            )
            setResult(RESULT_OK, errorData)
            finish()
            return
        }

        try {
            val requestJson = JSONObject(requestJsonStr)
            val challengeBase64 = requestJson.getString("challenge")
            val challenge = WebAuthnHelper.base64UrlDecode(challengeBase64)

            val authenticatorData = WebAuthnHelper.buildAuthenticatorDataForAssertion(
                rpId = passkey.rpId,
                signCount = passkey.signCount,
            )

            val origin = "android:apk-key-hash:${packageName}"
            val clientDataJson = """{"type":"webauthn.get","challenge":"${WebAuthnHelper.base64Url(challenge)}","origin":"$origin","androidPackageName":"$packageName"}"""
            val clientDataJsonBytes = clientDataJson.toByteArray()

            val signature = WebAuthnHelper.signAssertion(
                privateKeyPkcs8Base64 = passkey.privateKeyPkcs8,
                authenticatorData = authenticatorData,
                clientDataJsonBytes = clientDataJsonBytes,
            )

            val credIdBytes = WebAuthnHelper.base64UrlDecode(passkey.credentialId)
            val responseJson = JSONObject().apply {
                put("id", WebAuthnHelper.base64Url(credIdBytes))
                put("rawId", WebAuthnHelper.base64Url(credIdBytes))
                put("type", "public-key")
                put("response", JSONObject().apply {
                    put("clientDataJSON", WebAuthnHelper.base64Url(clientDataJsonBytes))
                    put("authenticatorData", WebAuthnHelper.base64Url(authenticatorData))
                    put("signature", WebAuthnHelper.base64Url(signature))
                    put("userHandle", passkey.userHandle)
                })
            }.toString()

            val resultData = android.content.Intent()
            PendingIntentHandler.setGetCredentialResponse(
                resultData,
                GetCredentialResponse(PublicKeyCredential(responseJson)),
            )
            setResult(RESULT_OK, resultData)
        } catch (_: Exception) {
            setResult(RESULT_CANCELED)
        }

        finish()
    }

    companion object {
        const val EXTRA_CREDENTIAL_ID = "credential_id"
        const val EXTRA_REQUEST_JSON = "request_json"
    }
}
