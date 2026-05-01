package com.example.pwunsafe.autofill

import android.os.CancellationSignal
import android.service.autofill.*
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.example.pwunsafe.data.model.Credential
import com.example.pwunsafe.data.repository.CredentialRepository
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import java.util.*

class PwUnsafeAutofillService : AutofillService() {

    private val repository: CredentialRepository by inject()

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback,
    ) {
        val context = request.fillContexts.lastOrNull() ?: run {
            callback.onSuccess(null)
            return
        }

        val parser = AssistStructureParser(context.structure)
        val usernameId = parser.usernameAutofillId
        val passwordId = parser.passwordAutofillId

        if (usernameId == null && passwordId == null) {
            callback.onSuccess(null)
            return
        }

        val packageName = context.structure.activityComponent.packageName
        val webDomain = parser.webDomain

        val matches = when {
            webDomain != null -> repository.findByDomain(webDomain)
                .ifEmpty { repository.findByPackage(packageName) }
            else -> repository.findByPackage(packageName)
        }.filter { it.passkey == null }

        val saveInfo = buildSaveInfo(usernameId, passwordId)
        val responseBuilder = FillResponse.Builder()
        if (saveInfo != null) responseBuilder.setSaveInfo(saveInfo)

        if (matches.isEmpty()) {
            callback.onSuccess(if (saveInfo != null) responseBuilder.build() else null)
            return
        }

        matches.forEach { cred ->
            responseBuilder.addDataset(buildDataset(cred, usernameId, passwordId))
        }

        callback.onSuccess(responseBuilder.build())
    }

    private fun buildSaveInfo(usernameId: AutofillId?, passwordId: AutofillId?): SaveInfo? {
        val requiredId = passwordId ?: usernameId ?: return null
        var type = 0
        if (usernameId != null) type = type or SaveInfo.SAVE_DATA_TYPE_USERNAME
        if (passwordId != null) type = type or SaveInfo.SAVE_DATA_TYPE_PASSWORD
        return SaveInfo.Builder(type, arrayOf(requiredId)).build()
    }

    private fun buildDataset(
        cred: Credential,
        usernameId: android.view.autofill.AutofillId?,
        passwordId: android.view.autofill.AutofillId?,
    ): Dataset {
        val label = "${cred.service} — ${cred.username}"
        val presentation = RemoteViews(packageName, android.R.layout.simple_list_item_1).apply {
            setTextViewText(android.R.id.text1, label)
        }

        return Dataset.Builder().apply {
            usernameId?.let { setValue(it, AutofillValue.forText(cred.username), presentation) }
            passwordId?.let { setValue(it, AutofillValue.forText(cred.password ?: ""), presentation) }
        }.build()
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        val context = request.fillContexts.lastOrNull() ?: run {
            callback.onSuccess()
            return
        }

        val parser = AssistStructureParser(context.structure)
        val username = parser.usernameValue?.takeIf { it.isNotBlank() } ?: ""
        val password = parser.passwordValue?.takeIf { it.isNotBlank() }

        if (username.isBlank() && password.isNullOrBlank()) {
            callback.onSuccess()
            return
        }

        val packageName = context.structure.activityComponent.packageName
        val webDomain = parser.webDomain
        val credential = Credential(
            id = UUID.randomUUID().toString(),
            service = webDomain ?: packageName,
            packageName = packageName,
            domain = webDomain,
            username = username,
            password = password,
        )

        runBlocking { repository.add(credential) }
        callback.onSuccess()
    }
}
