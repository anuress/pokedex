package com.anuress.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.anuress.data.model.Pokemon

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

import com.anuress.data.network.PokeApiService
import com.anuress.data.network.POKEMON_PAGE_SIZE
import com.anuress.data.network.PokemonPagingSource
import com.anuress.data.network.PokemonDetailResponse as PokemonDetail
import com.anuress.data.network.PokemonSpeciesResponse as PokemonSpecies
import com.anuress.data.network.PokemonAbilityEntry
import com.anuress.data.network.PokemonSprites
import com.anuress.data.network.OtherSprites
import com.anuress.data.network.OfficialArtworkSprite
import com.anuress.data.network.PokemonTypeEntry
import com.anuress.data.network.PokemonStatEntry
import com.anuress.data.network.GenusEntry
import com.anuress.data.network.NamedAPIResource
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
                pokeApiService.getPokemonDetail(id).toDomain()
            }
        }
    }

    override suspend fun getPokemonSpecies(id: Int): Result<DomainPokemonSpecies> {
        return withContext(Dispatchers.IO) {
            runCatching {
                pokeApiService.getPokemonSpecies(id).toDomain()
            }
        }
    }
}

private fun NamedAPIResource.toDomain(): DomainNamedAPIResource {
    return DomainNamedAPIResource(name = this.name, url = this.url)
}

private fun PokemonAbilityEntry.toDomain(): DomainPokemonAbility {
    return DomainPokemonAbility(
        ability = this.ability.toDomain(),
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
        type = this.type.toDomain()
    )
}

private fun PokemonStatEntry.toDomain(): DomainPokemonStat {
    return DomainPokemonStat(
        stat = this.stat.toDomain(),
        baseStat = this.baseStat,
        effort = this.effort
    )
}

private fun PokemonMoveEntry.toDomain(): DomainPokemonMove {
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
        // If no positive level-up, check other common methods in a preferred order
        val methodPriority = listOf("machine", "egg", "tutor")
        var foundOtherMethod = false
        for (methodName in methodPriority) {
            val entry = this.versionGroupDetails.firstOrNull { it.moveLearnMethod.name == methodName }
            if (entry != null) {
                description = when (methodName) {
                    "machine" -> "Machine (TM/TR)"
                    "egg" -> "Egg Move"
                    "tutor" -> "Tutor"
                    else -> methodName.replaceFirstChar { it.titlecase(Locale.getDefault()) } // Fallback, though current list is exhaustive
                }
                foundOtherMethod = true
                break
            }
        }
        // Handle level 0 (start/evolution) or other unprioritized methods as a last resort
        if (!foundOtherMethod) {
            val levelZeroEntry = this.versionGroupDetails.firstOrNull { it.moveLearnMethod.name == "level-up" && it.levelLearnedAt == 0 }
            if (levelZeroEntry != null) {
                description = "Start / Evolution"
                finalLevelLearnedAt = 0
            } else if (this.versionGroupDetails.isNotEmpty()) {
                // Generic fallback to the first detail available if no other recognized method is found
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

private fun PokemonDetail.toDomain(): DomainPokemonDetail {
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
        language = this.language.toDomain()
    )
}

private fun PokemonSpecies.toDomain(): DomainPokemonSpecies {
    return DomainPokemonSpecies(
        id = this.id,
        name = this.name,
        genderRate = this.genderRate,
        hatchCounter = this.hatchCounter,
        eggGroups = this.eggGroups.map { it.toDomain() },
        genera = this.genera.map { it.toDomain() }
    )
}
