package com.anuress.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.anuress.data.model.Pokemon // Domain model for list item

// Domain models (aliased with "Domain" prefix)
import com.anuress.data.model.PokemonDetail as DomainPokemonDetail
import com.anuress.data.model.PokemonSpecies as DomainPokemonSpecies
import com.anuress.data.model.PokemonAbility as DomainPokemonAbility
import com.anuress.data.model.PokemonSprites as DomainPokemonSprites
import com.anuress.data.model.OtherSprites as DomainOtherSprites
import com.anuress.data.model.OfficialArtworkSprite as DomainOfficialArtworkSprite
import com.anuress.data.model.PokemonType as DomainPokemonType
import com.anuress.data.model.PokemonStat as DomainPokemonStat
import com.anuress.data.model.Genus as DomainGenus
import com.anuress.data.model.NamedAPIResource as DomainNamedAPIResource
import com.anuress.data.model.PokemonMove as DomainPokemonMove

// Network service and models
import com.anuress.data.network.PokeApiService
import com.anuress.data.network.POKEMON_PAGE_SIZE
import com.anuress.data.network.PokemonPagingSource
// Network models - primary responses aliased to their name without "Response" suffix
import com.anuress.data.network.PokemonDetailResponse as PokemonDetail
import com.anuress.data.network.PokemonSpeciesResponse as PokemonSpecies
// Other network models - imported directly
import com.anuress.data.network.PokemonAbilityEntry
import com.anuress.data.network.PokemonSprites
import com.anuress.data.network.OtherSprites
import com.anuress.data.network.OfficialArtworkSprite
import com.anuress.data.network.PokemonTypeEntry
import com.anuress.data.network.PokemonStatEntry
import com.anuress.data.network.GenusEntry
import com.anuress.data.network.NamedAPIResource // Network version, domain is DomainNamedAPIResource
import com.anuress.data.network.PokemonMoveEntry

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Locale

class PokemonRepositoryImpl(
    private val pokeApiService: PokeApiService
) : PokemonRepository {

    override fun getPokemonStream(): Flow<PagingData<Pokemon>> {
        return Pager(
            config = PagingConfig(
                pageSize = POKEMON_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PokemonPagingSource(pokeApiService) }
        ).flow
    }

    override suspend fun getPokemonDetail(id: Int): Result<DomainPokemonDetail> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val pokemonDetailResponse = pokeApiService.getPokemonDetail(id) // Type is PokemonDetailResponse
                pokemonDetailResponse.toDomain() // Uses new alias PokemonDetail in toDomain()
            }.onFailure {
                // Optionally log the error
            }
        }
    }

    override suspend fun getPokemonSpecies(id: Int): Result<DomainPokemonSpecies> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val pokemonSpeciesResponse = pokeApiService.getPokemonSpecies(id) // Type is PokemonSpeciesResponse
                pokemonSpeciesResponse.toDomain() // Uses new alias PokemonSpecies in toDomain()
            }.onFailure {
                // Optionally log the error
            }
        }
    }
}

// --- Mapper Functions ---

// Receiver type is now the direct/aliased network type
private fun NamedAPIResource.toDomain(): DomainNamedAPIResource {
    return DomainNamedAPIResource(name = this.name, url = this.url)
}

private fun PokemonAbilityEntry.toDomain(): DomainPokemonAbility {
    return DomainPokemonAbility(
        ability = this.ability.toDomain(), // this.ability is NamedAPIResource (network)
        isHidden = this.isHidden,
        slot = this.slot
    )
}

private fun OfficialArtworkSprite.toDomain(): DomainOfficialArtworkSprite {
    return DomainOfficialArtworkSprite(frontDefault = this.frontDefault)
}

private fun OtherSprites.toDomain(): DomainOtherSprites {
    return DomainOtherSprites(officialArtwork = this.officialArtwork?.toDomain())
}

private fun PokemonSprites.toDomain(): DomainPokemonSprites {
    return DomainPokemonSprites(other = this.other?.toDomain())
}

private fun PokemonTypeEntry.toDomain(): DomainPokemonType {
    return DomainPokemonType(
        slot = this.slot,
        type = this.type.toDomain() // this.type is NamedAPIResource (network)
    )
}

private fun PokemonStatEntry.toDomain(): DomainPokemonStat {
    return DomainPokemonStat(
        stat = this.stat.toDomain(), // this.stat is NamedAPIResource (network)
        baseStat = this.baseStat,
        effort = this.effort
    )
}

private fun PokemonMoveEntry.toDomain(): DomainPokemonMove {
    val formattedMoveName = this.move.name // this.move is NamedAPIResource (network)
        .replace("-", " ")
        .split(' ')
        .joinToString(" ") { part -> part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }

    var description = "Unknown"
    var finalLevelLearnedAt: Int? = null

    val levelUpEntry = this.versionGroupDetails
        .filter { it.moveLearnMethod.name == "level-up" && it.levelLearnedAt > 0 }
        .minByOrNull { it.levelLearnedAt }

    if (levelUpEntry != null) {
        finalLevelLearnedAt = levelUpEntry.levelLearnedAt
        description = "Level $finalLevelLearnedAt"
    } else {
        val methodPriority = listOf("machine", "egg", "tutor")
        var foundOtherMethod = false
        for (methodName in methodPriority) {
            val entry = this.versionGroupDetails.firstOrNull { it.moveLearnMethod.name == methodName }
            if (entry != null) {
                description = when (methodName) {
                    "machine" -> "Machine (TM/TR)"
                    "egg" -> "Egg Move"
                    "tutor" -> "Tutor"
                    else -> methodName.replaceFirstChar { it.titlecase(Locale.getDefault()) }
                }
                foundOtherMethod = true
                break
            }
        }
        if (!foundOtherMethod) {
            val levelZeroEntry = this.versionGroupDetails.firstOrNull { it.moveLearnMethod.name == "level-up" && it.levelLearnedAt == 0 }
            if (levelZeroEntry != null) {
                description = "Start / Evolution"
                finalLevelLearnedAt = 0
            } else if (this.versionGroupDetails.isNotEmpty()) {
                val firstDetail = this.versionGroupDetails.first()
                description = firstDetail.moveLearnMethod.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
        }
    }

    return DomainPokemonMove(
        name = formattedMoveName,
        learnMethodDescription = description,
        levelLearnedAt = finalLevelLearnedAt
    )
}

// Receiver type is PokemonDetail (alias for com.anuress.data.network.PokemonDetailResponse)
private fun PokemonDetail.toDomain(): DomainPokemonDetail {
    return DomainPokemonDetail(
        id = this.id,
        name = this.name,
        height = this.height,
        weight = this.weight,
        abilities = this.abilities.map { it.toDomain() },
        species = this.species.toDomain(), // this.species is NamedAPIResource (network)
        sprites = this.sprites.toDomain(), // this.sprites is PokemonSprites (network)
        types = this.types.map { it.toDomain() },
        stats = this.stats.map { it.toDomain() },
        moves = this.moves.map { it.toDomain() }
            .sortedWith(compareBy(
                { it.levelLearnedAt ?: Int.MAX_VALUE },
                { it.name }
            ))
    )
}

private fun GenusEntry.toDomain(): DomainGenus {
    return DomainGenus(
        genus = this.genus,
        language = this.language.toDomain() // this.language is NamedAPIResource (network)
    )
}

// Receiver type is PokemonSpecies (alias for com.anuress.data.network.PokemonSpeciesResponse)
private fun PokemonSpecies.toDomain(): DomainPokemonSpecies {
    return DomainPokemonSpecies(
        id = this.id,
        name = this.name,
        genderRate = this.genderRate,
        hatchCounter = this.hatchCounter,
        eggGroups = this.eggGroups.map { it.toDomain() }, // this.eggGroups is List<NamedAPIResource (network)>
        genera = this.genera.map { it.toDomain() }
    )
}
