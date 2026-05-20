package com.example.pwunsafe.data.repository

import android.os.Build
import android.os.Environment
import android.os.FileObserver
import com.example.pwunsafe.data.model.Credential
import com.example.pwunsafe.data.storage.JsonCredentialStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class CredentialRepositoryImpl(
    private val storage: JsonCredentialStorage,
) : CredentialRepository {

    private val _credentials = MutableStateFlow<List<Credential>>(emptyList())
    override val credentials: StateFlow<List<Credential>> = _credentials

    private val _fileExists = MutableStateFlow(storage.fileExists)
    override val fileExists: StateFlow<Boolean> = _fileExists

    // FileObserver fires on the inotify thread — MutableStateFlow.value is thread-safe.
    // Null when the file already exists at startup — nothing to wait for.
    private val downloadsObserver: FileObserver? = if (storage.fileExists) null else run {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        // Extension lambda so stopWatching() resolves to FileObserver.stopWatching().
        val onEvent: FileObserver.(String?) -> Unit = { path ->
            if (path == "pwunsafe_credentials.json") {
                stopWatching()
                _credentials.value = storage.load()
                _fileExists.value = true
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            object : FileObserver(dir, CLOSE_WRITE or CREATE) {
                override fun onEvent(event: Int, path: String?) = onEvent(path)
            }
        } else {
            @Suppress("DEPRECATION")
            object : FileObserver(dir.absolutePath, CLOSE_WRITE or CREATE) {
                override fun onEvent(event: Int, path: String?) = onEvent(path)
            }
        }
    }

    init {
        _credentials.value = storage.load()
        downloadsObserver?.startWatching()
    }

    override suspend fun add(credential: Credential) = withContext(Dispatchers.IO) {
        val updated = _credentials.value + credential
        storage.save(updated)
        _fileExists.value = true
        _credentials.value = updated
    }

    override suspend fun update(credential: Credential) = withContext(Dispatchers.IO) {
        val updated = _credentials.value.map { if (it.id == credential.id) credential else it }
        storage.save(updated)
        _credentials.value = updated
    }

    override suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        val updated = _credentials.value.filter { it.id != id }
        storage.save(updated)
        _credentials.value = updated
    }

    override fun findByPackage(pkg: String): List<Credential> =
        _credentials.value.filter { it.packageName == pkg }

    override fun findByDomain(domain: String): List<Credential> =
        _credentials.value.filter { it.domain?.equals(domain, ignoreCase = true) == true }

    override fun findPasskeysByRpId(rpId: String): List<Credential> =
        _credentials.value.filter { it.passkey?.rpId == rpId }
}
