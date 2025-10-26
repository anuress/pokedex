package com.anuress.pokedex.ui.pokedex

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.anuress.data.model.Pokemon
import com.anuress.pokedex.PokedexDestinations
import com.anuress.pokedex.analytics.AnalyticsHelper
import com.anuress.pokedex.analytics.compose.SessionReplaySideEffect
import com.anuress.pokedex.ui.theme.PokedexTheme
import com.mixpanel.android.sessionreplay.extensions.mpReplaySensitive
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

fun getTextColorForBackground(backgroundColor: Color): Color {
    val luminance =
        (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
    return if (luminance < 0.5) Color.White else Color.Black
}

@Composable
fun PokemonCard(
    pokemon: Pokemon,
    modifier: Modifier = Modifier,
    onPokemonClick: (Pokemon, Int?) -> Unit // Updated callback for click with color
) {
    var cardColor by remember { mutableStateOf(Color.LightGray) }
    var cardColorInt by remember { mutableStateOf<Int?>(null) } // Store color as Int for navigation
    val textColor = getTextColorForBackground(cardColor)
    val context = LocalContext.current

    Card(
        modifier = modifier
            .width(160.dp)
            .height(200.dp)
            .padding(4.dp)
            .clickable {
                AnalyticsHelper.trackClickedPokemonCard(pokemon.id, pokemon.name)
                onPokemonClick(pokemon, cardColorInt)
            }, // Pass pokemon and colorInt
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(pokemon.imageUrl)
                    .allowHardware(false)
                    .listener(onSuccess = { _, result ->
                        val bitmap = result.drawable.toBitmap()
                        Palette.from(bitmap).generate { palette ->
                            palette?.dominantSwatch?.rgb?.let {
                                cardColor = Color(it)
                                cardColorInt = it // Store the Int value
                            }
                        }
                    })
                    .build(),
                contentDescription = pokemon.name,
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 12.dp)
                    .mpReplaySensitive(false)
            )
            Text(
                modifier = Modifier.mpReplaySensitive(false),
                text = pokemon.name.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokedexScreen(
    navController: NavHostController,
    viewModel: PokedexViewModel = koinViewModel()
) {
    SessionReplaySideEffect("PokedexScreen")

    val pokemonPagingItems: LazyPagingItems<Pokemon> =
        viewModel.pokemonPagingFlow.collectAsLazyPagingItems()

    val viewedPokemonIds = remember { mutableSetOf<Int>() }

    val lazyGridState = rememberLazyGridState()

    LaunchedEffect(lazyGridState, pokemonPagingItems.itemCount) {
        snapshotFlow { lazyGridState.layoutInfo }
            .map { layoutInfo ->
                // Get the indices of items that are currently visible
                layoutInfo.visibleItemsInfo.mapNotNull { itemInfo ->
                    // itemInfo.index is the absolute index in the list
                    // Ensure the index is valid for pokemonPagingItems
                    if (itemInfo.index < pokemonPagingItems.itemCount) {
                        pokemonPagingItems.peek(itemInfo.index) // Use peek to avoid triggering load
                    } else {
                        null
                    }
                }
            }
            .distinctUntilChanged() // Only proceed if the list of visible Pokemon changes
            .collect { visiblePokemon ->
                for (pokemon in visiblePokemon) {
                    if (!viewedPokemonIds.contains(pokemon.id)) {
                        AnalyticsHelper.trackPokemonItemViewed( // Assumes you add this to AnalyticsHelper
                            pokemonId = pokemon.id,
                            pokemonName = pokemon.name
                        )
                        viewedPokemonIds.add(pokemon.id)
                        Log.d("PokedexScreen", "Tracked view for Pokemon: ${pokemon.name}")
                    }
                }
            }
    }


    LaunchedEffect(Unit) {
        AnalyticsHelper.trackViewedPokedexScreen()
    }

    PokedexTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Pokedex",
                            modifier = Modifier.mpReplaySensitive(false),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .mpReplaySensitive(false)
            ) {
                when (pokemonPagingItems.loadState.refresh) {
                    is LoadState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is LoadState.Error -> {
                        val error = pokemonPagingItems.loadState.refresh as LoadState.Error
                        Text(
                            text = "Error loading Pokémon: ${error.error.localizedMessage}",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            state = lazyGridState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(pokemonPagingItems.itemCount) { index ->
                                val pokemon = pokemonPagingItems[index]
                                if (pokemon != null) {
                                    PokemonCard(
                                        pokemon = pokemon,
                                        onPokemonClick = { selectedPokemon, colorInt ->
                                            var route =
                                                "${PokedexDestinations.POKEMON_DETAIL_ROUTE_BASE}/${selectedPokemon.id}"
                                            if (colorInt != null) {
                                                route += "?color=$colorInt"
                                            }
                                            navController.navigate(route)
                                        }
                                    )
                                }
                            }

                            if (pokemonPagingItems.loadState.append is LoadState.Loading) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                            .wrapContentWidth(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                            if (pokemonPagingItems.loadState.append is LoadState.Error) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    val error =
                                        pokemonPagingItems.loadState.append as LoadState.Error
                                    Text(
                                        text = "Error loading more: ${error.error.localizedMessage}",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                            .wrapContentWidth(Alignment.CenterHorizontally),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PokemonCardPreview() {
    PokedexTheme {
        PokemonCard(
            pokemon = Pokemon(
                1,
                "Bulbasaur",
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png"
            ),
            onPokemonClick = { _, _ -> } // Pass empty lambda for preview
        )
    }
}
