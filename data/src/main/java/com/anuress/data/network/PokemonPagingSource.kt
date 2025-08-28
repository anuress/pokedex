package com.anuress.data.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anuress.data.model.Pokemon // Import the moved Pokemon data class
import java.io.IOException
import retrofit2.HttpException

private const val POKEMON_STARTING_PAGE_OFFSET = 0 // PokeAPI uses 0-based offset
const val POKEMON_PAGE_SIZE = 20 // Number of items to load per page

class PokemonPagingSource(
    private val pokeApiService: PokeApiService
) : PagingSource<Int, Pokemon>() { // Int is the type of the key (offset)

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pokemon> {
        val currentOffset = params.key ?: POKEMON_STARTING_PAGE_OFFSET

        return try {
            // params.loadSize is how many items Paging wants to load for this page
            val response = pokeApiService.getPokemonList(limit = params.loadSize, offset = currentOffset)
            val pokemonListItems = response.results

            val pokemons = pokemonListItems.map { listItem ->
                // Extract ID from URL (e.g., "https://pokeapi.co/api/v2/pokemon/1/")
                val id = listItem.url.split("/").last { it.isNotEmpty() }.toInt()
                val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
                Pokemon(id = id, name = listItem.name, imageUrl = imageUrl)
            }

            // Calculate the previous key. If currentOffset is the start, there's no previous page.
            val prevKey = if (currentOffset == POKEMON_STARTING_PAGE_OFFSET) {
                null
            } else {
                // Calculate offset for the previous page. Ensure it's not negative.
                (currentOffset - params.loadSize).coerceAtLeast(POKEMON_STARTING_PAGE_OFFSET)
            }

            // Calculate the next key. If response.next is null, it means no more pages.
            val nextKey = if (response.next != null) {
                currentOffset + params.loadSize
            } else {
                null
            }

            LoadResult.Page(
                data = pokemons,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (exception: IOException) { // Network errors
            LoadResult.Error(exception)
        } catch (exception: HttpException) { // HTTP errors
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Pokemon>): Int? {
        // This method is called when Paging needs to refresh the data.
        // We need to return the key (offset) of the page that should be loaded
        // to restart the load from the user's current scroll position.
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(POKEMON_PAGE_SIZE)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(POKEMON_PAGE_SIZE)
        }
    }
}
