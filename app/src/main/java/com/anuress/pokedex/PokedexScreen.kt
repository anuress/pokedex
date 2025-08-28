package com.anuress.pokedex

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.palette.graphics.Palette
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.anuress.data.model.Pokemon // Assuming this path is correct after module move
import com.anuress.pokedex.ui.pokedex.PokedexViewModel
import com.anuress.pokedex.ui.theme.PokedexTheme
import org.koin.androidx.compose.koinViewModel
import java.util.Locale // Added for titlecase

fun getTextColorForBackground(backgroundColor: Color): Color {
    val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
    return if (luminance < 0.5) Color.White else Color.Black
}

@Composable
fun PokemonCard(pokemon: Pokemon, modifier: Modifier = Modifier) {
    var cardColor by remember { mutableStateOf(Color.LightGray) }
    val textColor = getTextColorForBackground(cardColor)
    val context = LocalContext.current

    Card(
        modifier = modifier
            .width(160.dp) // Card width can remain the same or be adjusted
            .height(200.dp) // Increased height to accommodate larger image
            .padding(4.dp),
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
                    .size(120.dp) // Increased image size
                    .padding(bottom = 12.dp)
            )
            Text(
                text = pokemon.name.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }, // Capitalized name
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
    viewModel: PokedexViewModel = koinViewModel() // Inject ViewModel
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
                // Handle initial loading state
                if (pokemonPagingItems.loadState.refresh is LoadState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                // Handle error state for initial load
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
                                PokemonCard(pokemon = pokemon)
                            }
                        }

                        // Handle loading state for appending more items
                        if (pokemonPagingItems.loadState.append is LoadState.Loading) {
                            item(span = { GridItemSpan(maxLineSpan) }) { // Corrected span
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                )
                            }
                        }
                        // Handle error state for appending more items
                        if (pokemonPagingItems.loadState.append is LoadState.Error) {
                             item(span = { GridItemSpan(maxLineSpan) }) { // Corrected span
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
        // Updated to use the Pokemon from data.model with explicit data
        PokemonCard(pokemon = Pokemon(1, "Bulbasaur", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png"))
    }
}

// PokedexScreenPreview might be complex to set up with a fake PagingData flow.
// For now, it's removed. If needed, a more elaborate preview with fake data source can be created.
// @Preview(showBackground = true, widthDp = 380, heightDp = 760)
// @Composable
// fun PokedexScreenPreview() {
//    PokedexScreen() // This would require a Koin context or a fake ViewModel
// }
