package com.anuress.pokedex

import androidx.compose.foundation.clickable // Added import
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
// import androidx.compose.foundation.lazy.grid.items // Using foundation items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavHostController // Added import
import androidx.palette.graphics.Palette
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.anuress.data.model.Pokemon
import com.anuress.pokedex.ui.pokedex.PokedexViewModel
import com.anuress.pokedex.ui.theme.PokedexTheme
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

fun getTextColorForBackground(backgroundColor: Color): Color {
    val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
    return if (luminance < 0.5) Color.White else Color.Black
}

@Composable
fun PokemonCard(
    pokemon: Pokemon,
    modifier: Modifier = Modifier,
    onPokemonClick: (Pokemon) -> Unit // Added callback for click
) {
    var cardColor by remember { mutableStateOf(Color.LightGray) }
    val textColor = getTextColorForBackground(cardColor)
    val context = LocalContext.current

    Card(
        modifier = modifier
            .width(160.dp)
            .height(200.dp)
            .padding(4.dp)
            .clickable { onPokemonClick(pokemon) }, // Made card clickable
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
                            }
                        }
                    })
                    .build(),
                contentDescription = pokemon.name,
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 12.dp)
            )
            Text(
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
    navController: NavHostController, // Added NavController parameter
    viewModel: PokedexViewModel = koinViewModel()
) {
    val pokemonPagingItems: LazyPagingItems<Pokemon> = viewModel.pokemonPagingFlow.collectAsLazyPagingItems()

    PokedexTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Pokedex", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                if (pokemonPagingItems.loadState.refresh is LoadState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                else if (pokemonPagingItems.loadState.refresh is LoadState.Error) {
                    val error = pokemonPagingItems.loadState.refresh as LoadState.Error
                    Text(
                        text = "Error loading Pokémon: ${error.error.localizedMessage}",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
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
                                    onPokemonClick = { selectedPokemon ->
                                        navController.navigate(
                                            "${PokedexDestinations.POKEMON_DETAIL_ROUTE_BASE}/${selectedPokemon.id}"
                                        )
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
                                val error = pokemonPagingItems.loadState.append as LoadState.Error
                                Text(
                                    text = "Error loading more: ${error.error.localizedMessage}",
                                    modifier = Modifier.fillMaxWidth().padding(16.dp).wrapContentWidth(Alignment.CenterHorizontally),
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

@Preview(showBackground = true)
@Composable
fun PokemonCardPreview() {
    PokedexTheme {
        PokemonCard(
            pokemon = Pokemon(1, "Bulbasaur", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png"),
            onPokemonClick = {} // Pass empty lambda for preview
        )
    }
}

// PokedexScreenPreview would need a NavController, e.g., rememberNavController()
// @Preview(showBackground = true, widthDp = 380, heightDp = 760)
// @Composable
// fun PokedexScreenPreview() {
//    val navController = rememberNavController() // Example for preview
//    PokedexScreen(navController = navController)
// }
