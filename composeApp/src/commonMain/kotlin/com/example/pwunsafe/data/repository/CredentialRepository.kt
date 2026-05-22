package com.example.pwunsafe.data.repository

import com.example.pwunsafe.data.model.Credential
import kotlinx.coroutines.flow.StateFlow

interface CredentialRepository {
    val credentials: StateFlow<List<Credential>>
    val fileExists: StateFlow<Boolean>
    suspend fun add(credential: Credential)
    suspend fun update(credential: Credential)
    suspend fun delete(id: String)
    fun findByPackage(pkg: String): List<Credential>
    fun findByDomain(domain: String): List<Credential>
    fun findPasskeysByRpId(rpId: String): List<Credential>
}
