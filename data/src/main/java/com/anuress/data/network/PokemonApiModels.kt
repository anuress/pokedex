package com.anuress.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonListResponse(
    @SerialName("count") val count: Int,
    @SerialName("next") val next: String?,
    @SerialName("previous") val previous: String?,
    @SerialName("results") val results: List<PokemonListItem>
)

@Serializable
data class PokemonListItem(
    @SerialName("name") val name: String,
    // The URL from which we can fetch detailed data for this specific Pokemon
    @SerialName("url") val url: String
)

// Added data classes below

@Serializable
data class NamedAPIResource(
    @SerialName("name") val name: String,
    @SerialName("url") val url: String
)

// For Pokemon Detail Endpoint
@Serializable
data class PokemonDetail(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("height") val height: Int, // In decimetres
    @SerialName("weight") val weight: Int, // In hectograms
    @SerialName("sprites") val sprites: PokemonSprites,
    @SerialName("types") val types: List<PokemonTypeEntry>,
    @SerialName("stats") val stats: List<PokemonStatEntry>,
    @SerialName("abilities") val abilities: List<PokemonAbilityEntry>,
    @SerialName("species") val species: NamedAPIResource // URL to fetch PokemonSpecies data
)

@Serializable
data class PokemonSprites(
    @SerialName("other") val other: OtherSprites?
    // You can add more sprite categories here if needed, e.g., front_default directly
)

@Serializable
data class OtherSprites(
    @SerialName("official-artwork") val officialArtwork: OfficialArtworkSprite?
)

@Serializable
data class OfficialArtworkSprite(
    @SerialName("front_default") val frontDefault: String?
)

@Serializable
data class PokemonTypeEntry(
    @SerialName("slot") val slot: Int,
    @SerialName("type") val type: NamedAPIResource
)

@Serializable
data class PokemonStatEntry(
    @SerialName("stat") val stat: NamedAPIResource,
    @SerialName("base_stat") val baseStat: Int,
    @SerialName("effort") val effort: Int
)

@Serializable
data class PokemonAbilityEntry(
    @SerialName("ability") val ability: NamedAPIResource,
    @SerialName("is_hidden") val isHidden: Boolean,
    @SerialName("slot") val slot: Int
)

// For Pokemon Species Endpoint
@Serializable
data class PokemonSpecies(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("gender_rate") val genderRate: Int, // Chance of being female in eigths (e.g. 1 means 1/8 = 12.5%). -1 for genderless.
    @SerialName("hatch_counter") val hatchCounter: Int?, // Number of cycles for hatching an egg
    @SerialName("egg_groups") val eggGroups: List<NamedAPIResource>,
    @SerialName("genera") val genera: List<GenusEntry> // Name in different languages
    // Add other fields like flavor_text_entries, evolution_chain if needed later
)

@Serializable
data class GenusEntry(
    @SerialName("genus") val genus: String, // e.g., "Seed Pokémon"
    @SerialName("language") val language: NamedAPIResource // e.g., language.name == "en"
)
