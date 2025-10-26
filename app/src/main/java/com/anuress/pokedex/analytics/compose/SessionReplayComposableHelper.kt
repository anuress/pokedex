package com.anuress.pokedex.analytics.compose

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.mixpanel.android.sessionreplay.MPSessionReplay

@Composable
fun SessionReplaySideEffect(screenName: String) {
    MixpanelSessionReplaySideEffect()
}

@Composable
fun MixpanelSessionReplaySideEffect() {
    DisposableEffect(Unit) {
        try {
            MPSessionReplay.getInstance()?.startRecording()
        } catch (e: Exception) {
            Log.e("MixpanelSessionReplaySideEffect", "Error starting Mixpanel Session Replay", e)
        }

        onDispose {
            try {
                MPSessionReplay.getInstance()?.stopRecording()
            } catch (e: Exception) {
                Log.e("MixpanelSessionReplaySideEffect", "Error stopping Mixpanel Session Replay", e)
            }
        }
    }
}