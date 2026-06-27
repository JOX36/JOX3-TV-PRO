package com.streamvault.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamvault.app.data.Resource
import com.streamvault.app.data.models.*
import com.streamvault.app.ui.components.*
import com.streamvault.app.ui.theme.*

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onPlay: (String, String, String) -> Unit,
    isTv: Boolean = false,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val liveResults by viewModel.liveResults.collectAsState()
    val movieResults by viewModel.movieResults.collectAsState()
    val seriesResults by viewModel.seriesResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        StreamVaultTopBar(
            title = "Buscar",
            showBack = !isTv,
            onBack = onBack
        )

        SearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.updateSearch(it) },
            onSearch = { viewModel.performSearch(it) },
            active = false,
            onActiveChange = {},
            placeholder = { Text("Buscar canales, películas, series...") },
            leadingIcon = { Icon(Icons.Filled.Search, null, tint = CyanPrimary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearSearch() }) {
                        Icon(Icons.Filled.Close, null, tint = TextSecondary)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = SearchBarDefaults.colors(containerColor = DarkSurfaceVariant)
        ) {}

        if (isSearching) {
            LoadingIndicator()
        } else if (searchQuery.length >= 2) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Live results
                if (liveResults.isNotEmpty()) {
                    item {
                        SectionHeader(title = "📺 Canales en vivo (${liveResults.size})")
                    }
                    items(liveResults.take(10)) { stream ->
                        WideMediaCard(
                            title = stream.name,
                            imageUrl = stream.streamIcon,
                            showLiveBadge = true,
                            onClick = {
                                onPlay("live", stream.streamId.toString(), stream.name)
                            }
                        )
                    }
                }

                // Movie results
                if (movieResults.isNotEmpty()) {
                    item {
                        SectionHeader(title = "🎬 Películas (${movieResults.size})")
                    }
                    items(movieResults.take(10)) { movie ->
                        WideMediaCard(
                            title = movie.name,
                            imageUrl = movie.streamIcon,
                            subtitle = movie.rating?.let { "⭐ $it" },
                            onClick = {
                                onPlay("movie", movie.streamId.toString(), movie.name)
                            }
                        )
                    }
                }

                // Series results
                if (seriesResults.isNotEmpty()) {
                    item {
                        SectionHeader(title = "📺 Series (${seriesResults.size})")
                    }
                    items(seriesResults.take(10)) { series ->
                        WideMediaCard(
                            title = series.name,
                            imageUrl = series.cover,
                            subtitle = series.rating?.let { "⭐ $it" },
                            onClick = {
                                onPlay("series", series.seriesId.toString(), series.name)
                            }
                        )
                    }
                }

                // No results
                if (liveResults.isEmpty() && movieResults.isEmpty() && seriesResults.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Filled.SearchOff,
                            title = "Sin resultados",
                            message = "No se encontraron resultados para \"$searchQuery\""
                        )
                    }
                }
            }
        } else {
            EmptyState(
                icon = Icons.Filled.Search,
                title = "Buscar contenido",
                message = "Escribe al menos 2 caracteres para buscar"
            )
        }
    }
}
