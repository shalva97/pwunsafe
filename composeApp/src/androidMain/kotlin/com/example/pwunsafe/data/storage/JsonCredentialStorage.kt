package com.example.pwunsafe.data.storage

import android.content.Context
import com.example.pwunsafe.data.model.Credential
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

class JsonCredentialStorage(context: Context) {

    private val file = File(context.filesDir, "credentials.json")
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

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
