package com.anuress.pokedex.analytics

import android.content.Context
import android.util.Log
import com.mixpanel.android.sessionreplay.utils.MPTracker
import org.json.JSONObject

/**
 * A helper object for tracking Mixpanel events consistently throughout the application.
 */
object AnalyticsHelper {

    private const val TAG = "AnalyticsHelper" // For logging

    // Private function to safely get the Mixpanel instance.
    // Assumes Mixpanel has been initialized in the Application class.

    // --- Screen View Events ---

    fun trackViewedPokedexScreen() {
        val eventName = "Viewed Pokedex Screen"
        // No specific properties for this simple view event, but you could add some
        // (e.g., entry point like "AppOpen", "FromNotification")
        // val properties = JSONObject().apply { put("EntryPoint", "AppOpen") }
        MPTracker.track(eventName /*, properties */)
        Log.d(TAG, "Tracked Event: $eventName")
    }

    fun trackViewedPokemonDetailScreen(
        pokemonId: Int,
        pokemonName: String,
        dominantColorAvailable: Boolean // Example: to know if the color was passed
    ) {
        val eventName = "Viewed Pokemon Detail Screen"
        val properties = JSONObject().apply {
            put("Pokemon ID", pokemonId)
            put("Pokemon Name", pokemonName)
            put("Dominant Color Available", dominantColorAvailable)
        }
        MPTracker.track(eventName, properties)
        Log.d(TAG, "Tracked Event: $eventName, Properties: $properties")
    }

    // --- Interaction Events ---

    fun trackClickedPokemonCard(
        pokemonId: Int,
        pokemonName: String
        // You could add listIndex if it's relevant and available
    ) {
        val eventName = "Clicked Pokemon Card"
        val properties = JSONObject().apply {
            put("Pokemon ID", pokemonId)
            put("Pokemon Name", pokemonName)
        }
        MPTracker.track(eventName, properties)
        Log.d(TAG, "Tracked Event: $eventName, Properties: $properties")
    }

    // --- Data Load State Events ---

    fun trackPokedexListLoaded(
        itemCount: Int,
        loadSource: String = "PokedexScreen" // e.g., "PokedexScreen", "FavoritesScreen"
    ) {
        val eventName = "Pokedex List Loaded"
        val properties = JSONObject().apply {
            put("ItemCount", itemCount)
            put("Load Source", loadSource)
        }
        MPTracker.track(eventName, properties)
        Log.d(TAG, "Tracked Event: $eventName, Properties: $properties")
    }

    fun trackPokedexListLoadFailed(
        errorMessage: String?,
        loadSource: String = "PokedexScreen"
    ) {
        val eventName = "Pokedex List Load Failed"
        val properties = JSONObject().apply {
            put("Error Message", errorMessage ?: "Unknown error")
            put("Load Source", loadSource)
        }
        MPTracker.track(eventName, properties)
        Log.d(TAG, "Tracked Event: $eventName, Properties: $properties")
    }
}
