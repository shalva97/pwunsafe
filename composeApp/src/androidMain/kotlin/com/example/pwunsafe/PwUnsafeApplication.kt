package com.example.pwunsafe

import android.app.Application
import com.example.pwunsafe.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PwUnsafeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PwUnsafeApplication)
            modules(appModule)
        }
    }
}
