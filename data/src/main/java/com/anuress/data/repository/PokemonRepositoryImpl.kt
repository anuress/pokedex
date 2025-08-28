package com.anuress.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.anuress.data.model.Pokemon // Domain model for list item
// Import domain models that will be returned by the repository
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
import com.anuress.data.model.PokemonMove as DomainPokemonMove // <<< ADDED FOR MOVES

// Import network service and models used for fetching
import com.anuress.data.network.PokeApiService
import com.anuress.data.network.POKEMON_PAGE_SIZE
import com.anuress.data.network.PokemonPagingSource
import com.anuress.data.network.PokemonDetail as NetworkPokemonDetail
import com.anuress.data.network.PokemonSpecies as NetworkPokemonSpecies
import com.anuress.data.network.PokemonAbilityEntry as NetworkPokemonAbilityEntry
import com.anuress.data.network.PokemonSprites as NetworkPokemonSprites
import com.anuress.data.network.OtherSprites as NetworkOtherSprites
import com.anuress.data.network.OfficialArtworkSprite as NetworkOfficialArtworkSprite
import com.anuress.data.network.PokemonTypeEntry as NetworkPokemonTypeEntry
import com.anuress.data.network.PokemonStatEntry as NetworkPokemonStatEntry
import com.anuress.data.network.GenusEntry as NetworkGenusEntry
import com.anuress.data.network.NamedAPIResource as NetworkNamedAPIResource
import com.anuress.data.network.PokemonMoveEntry as NetworkPokemonMoveEntry // <<< ADDED FOR MOVES

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Locale // <<< ADDED FOR STRING FORMATTING

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
                val networkDetail = pokeApiService.getPokemonDetail(id)
                networkDetail.toDomain()
            }.onFailure {
                // Optionally log the error
            }
        }
    }

    override suspend fun getPokemonSpecies(id: Int): Result<DomainPokemonSpecies> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val networkSpecies = pokeApiService.getPokemonSpecies(id)
                networkSpecies.toDomain()
            }.onFailure {
                // Optionally log the error
            }
        }
    }
}

// --- Mapper Functions ---

private fun NetworkNamedAPIResource.toDomain(): DomainNamedAPIResource {
    return DomainNamedAPIResource(name = this.name, url = this.url)
}

private fun NetworkPokemonAbilityEntry.toDomain(): DomainPokemonAbility {
    return DomainPokemonAbility(
        ability = this.ability.toDomain(),
        isHidden = this.isHidden,
        slot = this.slot
    )
}

private fun NetworkOfficialArtworkSprite.toDomain(): DomainOfficialArtworkSprite {
    return DomainOfficialArtworkSprite(frontDefault = this.frontDefault)
}

private fun NetworkOtherSprites.toDomain(): DomainOtherSprites {
    return DomainOtherSprites(officialArtwork = this.officialArtwork?.toDomain())
}

private fun NetworkPokemonSprites.toDomain(): DomainPokemonSprites {
    return DomainPokemonSprites(other = this.other?.toDomain())
}

private fun NetworkPokemonTypeEntry.toDomain(): DomainPokemonType {
    return DomainPokemonType(
        slot = this.slot,
        type = this.type.toDomain()
    )
}

private fun NetworkPokemonStatEntry.toDomain(): DomainPokemonStat {
    return DomainPokemonStat(
        stat = this.stat.toDomain(),
        baseStat = this.baseStat,
        effort = this.effort
    )
}

// Mapper for Pokemon Moves
private fun NetworkPokemonMoveEntry.toDomain(): DomainPokemonMove {
    val formattedMoveName = this.move.name
        .replace("-", " ")
        .split(' ')
        .joinToString(" ") { part -> part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }

    var description = "Unknown"
    var finalLevelLearnedAt: Int? = null

    // Prioritize level-up moves with the lowest positive level
    val levelUpEntry = this.versionGroupDetails
        .filter { it.moveLearnMethod.name == "level-up" && it.levelLearnedAt > 0 }
        .minByOrNull { it.levelLearnedAt }

    if (levelUpEntry != null) {
        finalLevelLearnedAt = levelUpEntry.levelLearnedAt
        description = "Level $finalLevelLearnedAt"
    } else {
        // If no positive level-up, check other methods
        val methodPriority = listOf("machine", "egg", "tutor")
        var foundOtherMethod = false
        for (methodName in methodPriority) {
            val entry = this.versionGroupDetails.firstOrNull { it.moveLearnMethod.name == methodName }
            if (entry != null) {
                description = when (methodName) {
                    "machine" -> "Machine (TM/TR)"
                    "egg" -> "Egg Move"
                    "tutor" -> "Tutor"
                    else -> methodName.replaceFirstChar { it.titlecase(Locale.getDefault()) } // Should not be reached here
                }
                foundOtherMethod = true
                break
            }
        }

        if (!foundOtherMethod) {
            // Fallback for level 0 learn (start/evolution) or other unprioritized methods
            val levelZeroEntry = this.versionGroupDetails.firstOrNull { it.moveLearnMethod.name == "level-up" && it.levelLearnedAt == 0 }
            if (levelZeroEntry != null) {
                description = "Start / Evolution"
                finalLevelLearnedAt = 0 // Represent level 0
            } else if (this.versionGroupDetails.isNotEmpty()) {
                // Generic fallback to the first detail available
                val firstDetail = this.versionGroupDetails.first()
                description = firstDetail.moveLearnMethod.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
            // If versionGroupDetails is empty, description remains "Unknown" and finalLevelLearnedAt remains null
        }
    }

    return DomainPokemonMove(
        name = formattedMoveName,
        learnMethodDescription = description,
        levelLearnedAt = finalLevelLearnedAt
    )
}

private fun NetworkPokemonDetail.toDomain(): DomainPokemonDetail {
    return DomainPokemonDetail(
        id = this.id,
        name = this.name,
        height = this.height,
        weight = this.weight,
        abilities = this.abilities.map { it.toDomain() },
        species = this.species.toDomain(),
        sprites = this.sprites.toDomain(),
        types = this.types.map { it.toDomain() },
        stats = this.stats.map { it.toDomain() },
        moves = this.moves.map { it.toDomain() } // Map network moves to domain moves
            .sortedWith(compareBy( // Sort the domain moves
                { it.levelLearnedAt ?: Int.MAX_VALUE }, // Sort by level (nulls/non-level last)
                { it.name } // Then by name
            ))
    )
}

private fun NetworkGenusEntry.toDomain(): DomainGenus {
    return DomainGenus(
        genus = this.genus,
        language = this.language.toDomain()
    )
}

private fun NetworkPokemonSpecies.toDomain(): DomainPokemonSpecies {
    return DomainPokemonSpecies(
        id = this.id,
        name = this.name,
        genderRate = this.genderRate,
        hatchCounter = this.hatchCounter,
        eggGroups = this.eggGroups.map { it.toDomain() },
        genera = this.genera.map { it.toDomain() }
    )
}
