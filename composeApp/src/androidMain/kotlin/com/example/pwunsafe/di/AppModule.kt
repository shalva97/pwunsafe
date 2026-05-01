package com.example.pwunsafe.di

import com.example.pwunsafe.data.repository.CredentialRepository
import com.example.pwunsafe.data.repository.CredentialRepositoryImpl
import com.example.pwunsafe.data.storage.JsonCredentialStorage
import com.example.pwunsafe.ui.viewmodel.CredentialViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { JsonCredentialStorage(androidContext()) }
    single<CredentialRepository> { CredentialRepositoryImpl(get()) }
    viewModel { CredentialViewModel(get()) }
}
