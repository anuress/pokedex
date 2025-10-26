package com.anuress.pokedex

import android.app.Application
import android.util.Log
import com.anuress.data.network.networkModule
import com.anuress.data.repository.repositoryModule
import com.anuress.pokedex.analytics.analyticsModule
import com.anuress.pokedex.ui.pokedex.viewModelModule
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.mixpanel.android.sessionreplay.MPSessionReplay
import com.mixpanel.android.sessionreplay.models.MPSessionReplayConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class PokedexApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Koin
        startKoin {
            androidLogger()
            androidContext(this@PokedexApplication)
            modules(
                networkModule,
                analyticsModule,
                repositoryModule, // Added repository module
                viewModelModule,   // Added ViewModel module
            )
        }

        initializeMixpanel()
    }

    fun initializeMixpanel() {
        val mixpanelToken = BuildConfig.MIXPANEL_PROJECT_TOKEN
        if (mixpanelToken.isNotEmpty()) { // Check if token is not empty or the placeholder
            val mixpanel = MixpanelAPI.getInstance(this, mixpanelToken, true)

            // Initialize Mixpanel Session Replay
//            try {
//                val config = MPSessionReplayConfig(
//                    wifiOnly = false,
//                    enableLogging = true,
//                    recordingSessionsPercent = 100.0
//                )
//                MPSessionReplay.initialize(this, mixpanelToken, mixpanel.distinctId, config)
//            } catch (e: Exception) {
//                Log.e("PokedexApplication", "Error initializing Mixpanel Session Replay", e)
//            }

        } else {
            Log.w("PokedexApplication", "Mixpanel project token not found or is empty in BuildConfig. Mixpanel (and Session Replay) not initialized. Make sure it's set in local.properties.")
        }
    }
}
