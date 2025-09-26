package com.anuress.pokedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.anuress.pokedex.ui.pokedex.PokedexScreen
import com.anuress.pokedex.ui.pokemondetail.PokemonDetailScreen
import com.anuress.pokedex.ui.theme.PokedexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokedexApp() // Changed to a new root composable for clarity
        }
    }
}

@Composable
fun PokedexApp() {
    PokedexTheme {
        val navController: NavHostController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = PokedexDestinations.POKEDEX_LIST_ROUTE
        ) {
            composable(PokedexDestinations.POKEDEX_LIST_ROUTE) {
                // Pass navController to PokedexScreen so it can navigate
                PokedexScreen(navController = navController)
            }
            composable(
                route = PokedexDestinations.POKEMON_DETAIL_ROUTE_WITH_ARG,
                arguments = listOf(
                    navArgument(PokedexDestinations.POKEMON_ID_ARG) { type = NavType.IntType },
                    navArgument(PokedexDestinations.POKEMON_COLOR_ARG) {
                        type = NavType.StringType // Changed to StringType
                        nullable = true
                        defaultValue = null // Explicitly null default
                    }
                )
            ) {
                // Pass navController for potential back navigation from detail screen
                PokemonDetailScreen(navController = navController)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() { // Renamed GreetingPreview to DefaultPreview or AppPreview
    PokedexApp() // Preview the whole app with navigation
}
