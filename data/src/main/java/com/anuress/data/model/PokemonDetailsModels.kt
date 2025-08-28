package com.anuress.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Commonly used in PokeAPI for referencing other resources
@Serializable
data class NamedAPIResource(
    val name: String,
    val url: String
)

// For the main /pokemon/{id} endpoint
@Serializable
data class PokemonDetail(
    val id: Int,
    val name: String,
    val height: Int, // In decimetres
    val weight: Int, // In hectograms
    val abilities: List<PokemonAbility>,
    val species: NamedAPIResource,
    val sprites: PokemonSprites,
    val types: List<PokemonType>,
    val stats: List<PokemonStat>,
    val moves: List<PokemonMove> // <<< ADDED THIS LINE
)

@Serializable
data class PokemonAbility(
    val ability: NamedAPIResource,
    @SerialName("is_hidden") val isHidden: Boolean,
    val slot: Int
)

@Serializable
data class PokemonSprites(
    val other: OtherSprites? = null
)

@Serializable
data class OtherSprites(
    @SerialName("official-artwork") val officialArtwork: OfficialArtworkSprite? = null
)

@Serializable
data class OfficialArtworkSprite(
    @SerialName("front_default") val frontDefault: String?
)

@Serializable
data class PokemonType(
    val slot: Int,
    val type: NamedAPIResource
)

@Serializable
data class PokemonStat(
    val stat: NamedAPIResource,
    @SerialName("base_stat") val baseStat: Int,
    val effort: Int
)

// Added for Moves tab
@Serializable
data class PokemonMove(
    val name: String,
    val learnMethodDescription: String, // e.g., "Level 16", "TM", "Egg"
    val levelLearnedAt: Int? // Useful for sorting, can be null if not level-up
)

// For the /pokemon-species/{id} endpoint
@Serializable
data class PokemonSpecies(
    val id: Int,
    val name: String,
    @SerialName("gender_rate") val genderRate: Int, // Chance to be female in eighths (e.g., 1 for 12.5% female, -1 for genderless)
    @SerialName("hatch_counter") val hatchCounter: Int? = null, // Steps to hatch egg (egg cycle related)
    @SerialName("egg_groups") val eggGroups: List<NamedAPIResource>,
    val genera: List<Genus> // Contains "Seed Pokémon" in different languages
)

@Serializable
data class Genus(
    val genus: String,
    val language: NamedAPIResource
)
