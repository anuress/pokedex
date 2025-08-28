package com.anuress.pokedex.ui.pokemondetail

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.anuress.data.model.NamedAPIResource
import com.anuress.data.model.PokemonAbility
import com.anuress.data.model.PokemonDetail
import com.anuress.data.model.PokemonSpecies
import com.anuress.data.model.PokemonStat
import com.anuress.data.model.PokemonMove
import com.anuress.pokedex.ui.theme.PokedexTheme
import org.koin.androidx.compose.koinViewModel
import java.util.Locale
import kotlin.math.roundToInt

// --- Helper Functions for Formatting ---
private fun formatHeight(decimetres: Int): String {
    val meters = decimetres / 10.0
    val feet = meters * 3.28084
    val inches = (feet - feet.toInt()) * 12
    return String.format(Locale.US, "%.2f m (%d' %.1f\")", meters, feet.toInt(), inches)
}

private fun formatWeight(hectograms: Int): String {
    val kilograms = hectograms / 10.0
    val pounds = kilograms * 2.20462
    return String.format(Locale.US, "%.1f kg (%.1f lbs)", kilograms, pounds)
}

private fun formatAbilities(abilities: List<PokemonAbility>): String {
    return abilities.joinToString { ability ->
        ability.ability.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } +
                if (ability.isHidden) " (Hidden)" else ""
    }
}

private fun formatGender(genderRate: Int): String {
    return when (genderRate) {
        -1 -> "Genderless"
        else -> {
            val femalePercentage = (genderRate / 8.0 * 100).roundToInt()
            "♀ $femalePercentage% \t ♂ ${100 - femalePercentage}%"
        }
    }
}

private fun formatEggGroups(eggGroups: List<NamedAPIResource>): String {
    return eggGroups.joinToString { eggGroup -> eggGroup.name.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString() } }
}

private fun formatEggCycle(hatchCounter: Int?): String {
    return hatchCounter?.let {
        val steps = it * 256
        "$it cycles ($steps steps)"
    } ?: "Unknown"
}

// --- Helper Functions for Base Stats ---
private fun formatStatName(statName: String): String {
    return when (statName.lowercase(Locale.getDefault())) {
        "hp" -> "HP"
        "attack" -> "Attack"
        "defense" -> "Defense"
        "special-attack" -> "Sp. Atk"
        "special-defense" -> "Sp. Def"
        "speed" -> "Speed"
        else -> statName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}

private const val MAX_STAT_VALUE = 255f

private fun getStatColor(statName: String): Color {
    return when (statName.lowercase(Locale.getDefault())) {
        "hp" -> Color(0xFFDF2140)
        "attack" -> Color(0xFFF79F37)
        "defense" -> Color(0xFFF3D23D)
        "special-attack" -> Color(0xFF5D7BF1)
        "special-defense" -> Color(0xFF6EDDD0)
        "speed" -> Color(0xFFEF5682)
        else -> Color.Gray
    }
}

fun getTypeColor(typeName: String): Color {
    return when (typeName.lowercase(Locale.getDefault())) {
        "grass" -> Color(0xFF78C850)
        "poison" -> Color(0xFFA040A0)
        "fire" -> Color(0xFFF08030)
        "water" -> Color(0xFF6890F0)
        "electric" -> Color(0xFFF8D030)
        "normal" -> Color(0xFFA8A878)
        "fighting" -> Color(0xFFC03028)
        "flying" -> Color(0xFFA890F0)
        "ground" -> Color(0xFFE0C068)
        "rock" -> Color(0xFFB8A038)
        "bug" -> Color(0xFFA8B820)
        "ghost" -> Color(0xFF705898)
        "steel" -> Color(0xFFB8B8D0)
        "psychic" -> Color(0xFFF85888)
        "ice" -> Color(0xFF98D8D8)
        "dragon" -> Color(0xFF7038F8)
        "dark" -> Color(0xFF705848)
        "fairy" -> Color(0xFFEE99AC)
        else -> Color.Gray
    }
}

fun getTextColorForBackground(backgroundColor: Color): Color {
    val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
    return if (luminance < 0.5) Color.White else Color.Black
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    navController: NavHostController,
    viewModel: PokemonDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val defaultBackgroundColor = MaterialTheme.colorScheme.surface

    // Initialize dominantColor with the value from navigation if available
    var dominantColor by remember(uiState.initialColorInt) {
        mutableStateOf(uiState.initialColorInt?.let { Color(it) })
    }

    val currentTopSectionBackgroundColor = dominantColor ?: defaultBackgroundColor
    val topSectionTextColor = getTextColorForBackground(currentTopSectionBackgroundColor)

    // LaunchedEffect to extract color from the detail image
    // This will run if initialColorInt was null, or potentially refine the color if one was passed
    LaunchedEffect(uiState.pokemonDetail?.sprites?.other?.officialArtwork?.frontDefault) {
        val imageUrl = uiState.pokemonDetail?.sprites?.other?.officialArtwork?.frontDefault
        if (imageUrl == null) {
            // If there's no image URL, and no initial color was set, don't try to load.
            // If an initial color was set, we keep it.
            if (dominantColor == null) dominantColor = null // Explicitly set to null if no image and no initial
            return@LaunchedEffect
        }

        val imageLoader = context.imageLoader
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false) // Important for Palette
            .build()
        try {
            when (val result = imageLoader.execute(request)) {
                is SuccessResult -> {
                    if (result.drawable is BitmapDrawable) {
                        val bitmap = (result.drawable as BitmapDrawable).bitmap
                        Palette.from(bitmap).generate { palette ->
                            dominantColor = palette?.dominantSwatch?.rgb?.let { Color(it) }
                        }
                    }
                }
                else -> {
                    // If image loading fails, and no initial color was set, set to null.
                    // If an initial color was set, we might choose to keep it or clear it.
                    // For now, let's clear it to indicate an issue with the detail image's color.
                    // Alternatively, we could preserve uiState.initialColorInt if it was valid.
                    if (uiState.initialColorInt == null) dominantColor = null
                }
            }
        } catch (_: Exception) {
            // If exception occurs, and no initial color was set, set to null.
            if (uiState.initialColorInt == null) dominantColor = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = currentTopSectionBackgroundColor,
                    navigationIconContentColor = topSectionTextColor,
                    titleContentColor = topSectionTextColor
                )
            )
        }
    ) { paddingValues ->
        PokemonDetailScreenContent(
            uiState = uiState,
            modifier = Modifier.padding(paddingValues),
            currentTopSectionBackgroundColor = currentTopSectionBackgroundColor,
            topSectionTextColor = topSectionTextColor
        )
    }
}

@Composable
fun PokemonTypeChip(typeName: String) {
    val typeColor = getTypeColor(typeName)
    val textColor = getTextColorForBackground(typeColor)
    Surface(
        color = typeColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = typeName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreenContent(
    uiState: PokemonDetailScreenState,
    modifier: Modifier = Modifier,
    currentTopSectionBackgroundColor: Color,
    topSectionTextColor: Color
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("About", "Base Stats", "Evolution", "Moves")

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        when {
            uiState.isLoading && uiState.initialColorInt == null -> { // Show loading only if no initial color
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }
            uiState.pokemonDetail != null && uiState.pokemonSpecies != null -> {
                val detail = uiState.pokemonDetail
                val species = uiState.pokemonSpecies
                val imageUrl = detail.sprites.other?.officialArtwork?.frontDefault

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Section: Image, Name, ID, Types
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(currentTopSectionBackgroundColor)
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (uiState.isLoading && imageUrl == null) { // Still show loader if image specifically is loading
                             CircularProgressIndicator(modifier = Modifier.size(100.dp).align(Alignment.CenterHorizontally).padding(bottom=50.dp))
                        } else {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = detail.name,
                                modifier = Modifier.size(200.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = detail.name.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                                },
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                color = topSectionTextColor,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "#${detail.id.toString().padStart(3, '0')}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = topSectionTextColor.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            detail.types.forEach { pokemonType ->
                                PokemonTypeChip(typeName = pokemonType.type.name)
                            }
                        }
                    }

                    PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title, style = MaterialTheme.typography.titleSmall) }
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                        when (selectedTabIndex) {
                            0 -> PokemonAboutSection(detail = detail, species = species)
                            1 -> PokemonBaseStatsSection(detail = detail)
                            2 -> Text("Evolution Content - Coming Soon!", style = MaterialTheme.typography.bodyLarge)
                            3 -> PokemonMovesSection(detail = detail)
                        }
                    }
                }
            }
            // Fallback for when data is not loading, no error, but also no details (should ideally not happen if isLoading is handled correctly)
            // Or if isLoading is true but an initialColorInt was provided, we show content with potential shimmer/placeholder for image.
            else -> {
                 if (uiState.isLoading) { // Content is displayed because of initialColorInt, but data is still loading
                     Column(modifier = Modifier.fillMaxSize()) { /* Show skeleton or current content */ }
                 } else {
                     Text(
                        "No Pokémon data available.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                 }
            }
        }
    }
}

@Composable
fun PokemonAboutSection(detail: PokemonDetail, species: PokemonSpecies) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AboutDetailItem(label = "Species", value = species.genera.firstOrNull { it.language.name == "en" }?.genus ?: "Unknown")
        AboutDetailItem(label = "Height", value = formatHeight(detail.height))
        AboutDetailItem(label = "Weight", value = formatWeight(detail.weight))
        AboutDetailItem(label = "Abilities", value = formatAbilities(detail.abilities))

        Spacer(modifier = Modifier.height(8.dp))
        Text("Breeding", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        AboutDetailItem(label = "Gender", value = formatGender(species.genderRate))
        AboutDetailItem(label = "Egg Groups", value = formatEggGroups(species.eggGroups))
        AboutDetailItem(label = "Egg Cycle", value = formatEggCycle(species.hatchCounter))
    }
}

@Composable
fun AboutDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.width(100.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PokemonBaseStatsSection(detail: PokemonDetail) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        detail.stats.forEach { statEntry ->
            StatRow(stat = statEntry)
        }
    }
}

@Composable
fun StatRow(stat: PokemonStat) {
    val statValue = stat.baseStat
    val statName = formatStatName(stat.stat.name)
    val statColor = getStatColor(stat.stat.name)
    val progress = statValue / MAX_STAT_VALUE

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = statName,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = statValue.toString().padStart(3),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = statColor,
            trackColor = statColor.copy(alpha = 0.3f),
            strokeCap = StrokeCap.Round
        )
    }
}

// --- MOVES SECTION ---
@Composable
fun PokemonMovesSection(detail: PokemonDetail, modifier: Modifier = Modifier) {
    if (detail.moves.isEmpty()) {
        Text(
            text = "No moves information available for this Pokémon.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier.padding(16.dp).fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        return
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(detail.moves) { move ->
            MoveRow(move = move)
            HorizontalDivider()
        }
    }
}

@Composable
fun MoveRow(move: PokemonMove) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = move.name,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = move.learnMethodDescription,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


// --- Previews ---
@Preview(showBackground = true)
@Composable
fun PokemonDetailScreenLoadingPreview() {
    PokedexTheme {
        val defaultBg = MaterialTheme.colorScheme.surface
        val defaultText = getTextColorForBackground(defaultBg)
        PokemonDetailScreenContent(
            uiState = PokemonDetailScreenState(isLoading = true, initialColorInt = null),
            currentTopSectionBackgroundColor = defaultBg,
            topSectionTextColor = defaultText
        )
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 760)
@Composable
fun PokemonDetailScreenDataPreview() {
    PokedexTheme {
        val previewColorInt = Color.Magenta.hashCode() // Example initial color for preview
        val previewColor = Color(previewColorInt)
        val previewTextColor = getTextColorForBackground(previewColor)

        PokemonDetailScreenContent(
            uiState = PokemonDetailScreenState(
                isLoading = false,
                pokemonDetail = PokemonDetail(
                    id = 1, name = "bulbasaur", height = 7, weight = 69,
                    abilities = listOf(
                        PokemonAbility(NamedAPIResource("overgrow", ""), false, 1),
                        PokemonAbility(NamedAPIResource("chlorophyll", ""), true, 3)
                    ),
                    species = NamedAPIResource("bulbasaur", ""),
                    sprites = com.anuress.data.model.PokemonSprites(
                        other = com.anuress.data.model.OtherSprites(
                            officialArtwork = com.anuress.data.model.OfficialArtworkSprite(
                                "https.raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png"
                            )
                        )
                    ),
                    types = listOf(
                        com.anuress.data.model.PokemonType(1, NamedAPIResource("grass", "")),
                        com.anuress.data.model.PokemonType(2, NamedAPIResource("poison", ""))
                    ),
                    stats = listOf(
                        PokemonStat(NamedAPIResource("hp", ""), 45, 0),
                        PokemonStat(NamedAPIResource("attack", ""), 49, 0),
                        PokemonStat(NamedAPIResource("defense", ""), 49, 0),
                        PokemonStat(NamedAPIResource("special-attack", ""), 65, 0),
                        PokemonStat(NamedAPIResource("special-defense", ""), 65, 0),
                        PokemonStat(NamedAPIResource("speed", ""), 45, 0)
                    ),
                    moves = listOf(
                        PokemonMove("Tackle", "Level 1", 1),
                        PokemonMove("Growl", "Level 1", 1),
                        PokemonMove("Vine Whip", "Level 5", 5),
                        PokemonMove("Razor Leaf", "Machine (TM/TR)", null),
                        PokemonMove("Seed Bomb", "Egg Move", null)
                    )
                ),
                pokemonSpecies = PokemonSpecies(
                    id = 1, name = "bulbasaur", genderRate = 1, hatchCounter = 20,
                    eggGroups = listOf(NamedAPIResource("monster", ""), NamedAPIResource("grass", "")),
                    genera = listOf(com.anuress.data.model.Genus("Seed Pokémon", NamedAPIResource("en", "")))
                ),
                errorMessage = null,
                initialColorInt = previewColorInt
            ),
            currentTopSectionBackgroundColor = previewColor, // Use the passed color for preview
            topSectionTextColor = previewTextColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PokemonDetailScreenErrorPreview() {
    PokedexTheme {
        val defaultBg = MaterialTheme.colorScheme.surface
        val defaultText = getTextColorForBackground(defaultBg)
        PokemonDetailScreenContent(
            uiState = PokemonDetailScreenState(
                isLoading = false,
                errorMessage = "Failed to load Pokémon details. Please try again.",
                initialColorInt = null
            ),
            currentTopSectionBackgroundColor = defaultBg,
            topSectionTextColor = defaultText
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PokemonTypeChipPreview() {
    PokedexTheme { Row { PokemonTypeChip(typeName = "Grass"); PokemonTypeChip(typeName = "Poison") } }
}

@Preview(showBackground = true)
@Composable
fun PokemonAboutSectionPreview() {
    PokedexTheme {
        val sampleDetail = PokemonDetail(
            id = 1, name = "bulbasaur", height = 7, weight = 69,
            abilities = listOf(
                PokemonAbility(NamedAPIResource("overgrow", ""), false, 1),
                PokemonAbility(NamedAPIResource("chlorophyll", ""), true, 3)
            ),
            species = NamedAPIResource("",""),
            sprites = com.anuress.data.model.PokemonSprites(),
            types = emptyList(),
            stats = emptyList(),
            moves = emptyList()
        )
        val sampleSpecies = PokemonSpecies(
            id = 1, name = "bulbasaur", genderRate = 1, hatchCounter = 20,
            eggGroups = listOf(NamedAPIResource("monster", ""), NamedAPIResource("grass", "")),
            genera = listOf(com.anuress.data.model.Genus("Seed Pokémon", NamedAPIResource("en", "")))
        )
        PokemonAboutSection(detail = sampleDetail, species = sampleSpecies)
    }
}

@Preview(showBackground = true)
@Composable
fun PokemonBaseStatsSectionPreview() {
    PokedexTheme {
        val sampleDetail = PokemonDetail(
            id = 1, name = "bulbasaur", height = 7, weight = 69,
            abilities = emptyList(), species = NamedAPIResource("",""),
            sprites = com.anuress.data.model.PokemonSprites(), types = emptyList(),
            stats = listOf(
                PokemonStat(NamedAPIResource("hp", ""), 45, 0),
                PokemonStat(NamedAPIResource("attack", ""), 49, 0),
                PokemonStat(NamedAPIResource("defense", ""), 49, 0),
                PokemonStat(NamedAPIResource("special-attack", ""), 65, 0),
                PokemonStat(NamedAPIResource("special-defense", ""), 65, 0),
                PokemonStat(NamedAPIResource("speed", ""), 45, 0)
            ),
            moves = emptyList()
        )
        Surface(modifier = Modifier.padding(16.dp)) {
            PokemonBaseStatsSection(detail = sampleDetail)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PokemonMovesSectionPreview() {
    PokedexTheme {
        val sampleDetail = PokemonDetail(
            id = 1, name = "bulbasaur", height = 7, weight = 69,
            abilities = emptyList(),
            species = NamedAPIResource("", ""),
            sprites = com.anuress.data.model.PokemonSprites(),
            types = emptyList(),
            stats = emptyList(),
            moves = listOf(
                PokemonMove("Tackle", "Level 1", 1),
                PokemonMove("Growl", "Level 1", 1),
                PokemonMove("Vine Whip", "Level 5", 5),
                PokemonMove("Leech Seed", "Level 9", 9),
                PokemonMove("Razor Leaf", "Machine (TM/TR)", null),
                PokemonMove("Seed Bomb", "Egg Move", null),
                PokemonMove ("Solar Beam", "Tutor", null),
                PokemonMove("Sleep Powder", "Level 13", 13),
                PokemonMove("Poison Powder", "Level 13", 13),
                PokemonMove("Take Down", "Level 15", 15),
                PokemonMove("Sweet Scent", "Level 20", 20)
            )
        )
        Surface(modifier = Modifier.padding(16.dp)) {
            PokemonMovesSection(detail = sampleDetail)
        }
    }
}
