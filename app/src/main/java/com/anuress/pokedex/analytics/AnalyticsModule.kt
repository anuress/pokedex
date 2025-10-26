package com.anuress.pokedex.analytics

import com.anuress.pokedex.BuildConfig
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val analyticsModule = module {
    single<MixpanelAPI> {
        val mixpanelToken = BuildConfig.MIXPANEL_PROJECT_TOKEN
        MixpanelAPI.getInstance(androidApplication(), mixpanelToken, false).apply {
            setEnableLogging(true)
        }
    }
}