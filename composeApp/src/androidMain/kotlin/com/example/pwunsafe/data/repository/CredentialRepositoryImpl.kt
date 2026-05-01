package com.example.pwunsafe.data.repository

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

    init {
        _credentials.value = storage.load()
    }

    override suspend fun add(credential: Credential) = withContext(Dispatchers.IO) {
        val updated = _credentials.value + credential
        storage.save(updated)
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
