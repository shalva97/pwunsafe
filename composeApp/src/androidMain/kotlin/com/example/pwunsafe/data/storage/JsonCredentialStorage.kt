package com.example.pwunsafe.data.storage

import android.content.Context
import android.os.Environment
import com.example.pwunsafe.data.model.Credential
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

class JsonCredentialStorage(@Suppress("UnusedParameter") context: Context) {

    private val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "pwunsafe_credentials.json",
    )
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    val fileExists: Boolean get() = file.exists()

    fun load(): List<Credential> {
        if (!file.exists()) return emptyList()
        return try {
            json.decodeFromString(ListSerializer(Credential.serializer()), file.readText())
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(credentials: List<Credential>) {
        file.writeText(json.encodeToString(ListSerializer(Credential.serializer()), credentials))
    }
}
