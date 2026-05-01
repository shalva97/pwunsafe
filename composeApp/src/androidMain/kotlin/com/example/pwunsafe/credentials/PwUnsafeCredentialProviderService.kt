package com.example.pwunsafe.credentials

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.CancellationSignal
import android.os.OutcomeReceiver
import androidx.annotation.RequiresApi
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.provider.*
import com.example.pwunsafe.data.repository.CredentialRepository
import org.koin.android.ext.android.inject

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class PwUnsafeCredentialProviderService : CredentialProviderService() {

    private val repository: CredentialRepository by inject()

    override fun onBeginGetCredentialRequest(
        request: BeginGetCredentialRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<BeginGetCredentialResponse, GetCredentialException>,
    ) {
        val entries = mutableListOf<PublicKeyCredentialEntry>()

        request.beginGetCredentialOptions.forEach { option ->
            if (option is BeginGetPublicKeyCredentialOption) {
                val rpId = parseRpIdFromJson(option.requestJson)
                if (rpId != null) {
                    repository.findPasskeysByRpId(rpId).forEach { cred ->
                        val intent = Intent(this, GetCredentialActivity::class.java).apply {
                            putExtra(GetCredentialActivity.EXTRA_CREDENTIAL_ID, cred.id)
                            putExtra(GetCredentialActivity.EXTRA_REQUEST_JSON, option.requestJson)
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            this,
                            cred.id.hashCode(),
                            intent,
                            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                        )
                        entries.add(
                            PublicKeyCredentialEntry.Builder(
                                context = this,
                                username = cred.username,
                                pendingIntent = pendingIntent,
                                beginGetPublicKeyCredentialOption = option,
                            ).setDisplayName(cred.service).build(),
                        )
                    }
                }
            }
        }

        callback.onResult(BeginGetCredentialResponse(credentialEntries = entries))
    }

    override fun onBeginCreateCredentialRequest(
        request: BeginCreateCredentialRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<BeginCreateCredentialResponse, CreateCredentialException>,
    ) {
        if (request !is BeginCreatePublicKeyCredentialRequest) {
            callback.onResult(BeginCreateCredentialResponse())
            return
        }

        val intent = Intent(this, CreateCredentialActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val createEntry = CreateEntry.Builder("pwunsafe", pendingIntent).build()
        callback.onResult(BeginCreateCredentialResponse(createEntries = listOf(createEntry)))
    }

    override fun onClearCredentialStateRequest(
        request: ProviderClearCredentialStateRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<Void?, ClearCredentialException>,
    ) {
        callback.onResult(null)
    }

    private fun parseRpIdFromJson(json: String): String? = try {
        val start = json.indexOf("\"rpId\"")
        if (start == -1) return null
        val colon = json.indexOf(':', start) + 1
        val quote1 = json.indexOf('"', colon) + 1
        val quote2 = json.indexOf('"', quote1)
        json.substring(quote1, quote2)
    } catch (_: Exception) {
        null
    }
}
