package com.example.pwunsafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pwunsafe.data.model.Credential
import com.example.pwunsafe.data.repository.CredentialRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CredentialViewModel(private val repository: CredentialRepository) : ViewModel() {

    val credentials: StateFlow<List<Credential>> = repository.credentials

    fun add(credential: Credential) = viewModelScope.launch { repository.add(credential) }

    fun update(credential: Credential) = viewModelScope.launch { repository.update(credential) }

    fun delete(id: String) = viewModelScope.launch { repository.delete(id) }
}
