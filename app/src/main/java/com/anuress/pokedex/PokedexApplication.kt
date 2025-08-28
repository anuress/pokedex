package com.anuress.pokedex

import android.app.Application
import com.anuress.data.network.networkModule
import com.anuress.data.repository.repositoryModule // Import repository module
import com.anuress.pokedex.ui.pokedex.viewModelModule // Import ViewModel module
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class PokedexApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@PokedexApplication)
            modules(
                networkModule,
                repositoryModule, // Added repository module
                viewModelModule   // Added ViewModel module
            )
        }
    }
}
