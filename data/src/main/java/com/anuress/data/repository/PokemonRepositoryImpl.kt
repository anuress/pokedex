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
import com.anuress.data.model.NamedAPIResource as DomainNamedAPIResource // It's important this matches structure

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
import com.anuress.data.network.NamedAPIResource as NetworkNamedAPIResource // It's important this matches structure

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

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
                networkDetail.toDomain() // Call the mapper
            }.onFailure {
                // Optionally log the error: Log.e("PokemonRepository", "Failed to fetch/map detail", it)
            }
        }
    }

    override suspend fun getPokemonSpecies(id: Int): Result<DomainPokemonSpecies> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val networkSpecies = pokeApiService.getPokemonSpecies(id)
                networkSpecies.toDomain() // Call the mapper
            }.onFailure {
                // Optionally log the error: Log.e("PokemonRepository", "Failed to fetch/map species", it)
            }
        }
    }
}

// --- Mapper Functions ---

// Assumes NetworkNamedAPIResource and DomainNamedAPIResource are structurally identical
// or a specific mapper would be needed if they differ. For now, direct use.
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
        stats = this.stats.map { it.toDomain() }
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
