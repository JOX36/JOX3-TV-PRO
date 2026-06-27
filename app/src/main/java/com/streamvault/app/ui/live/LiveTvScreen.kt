package com.streamvault.app.ui.live

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
fun LiveTvScreen(
    onBack: () -> Unit,
    onPlay: (String, String, String) -> Unit,
    isTv: Boolean = false,
    viewModel: LiveViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val streams by viewModel.streams.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        StreamVaultTopBar(
            title = "TV en Vivo",
            subtitle = selectedCategory?.categoryName ?: "Todos los canales",
            showBack = !isTv,
            onBack = onBack,
            actions = {
                IconButton(onClick = { viewModel.toggleView() }) {
                    Icon(
                        imageVector = if (isGridView) Icons.Filled.List else Icons.Filled.GridView,
                        contentDescription = "Cambiar vista",
                        tint = CyanPrimary
                    )
                }
                IconButton(onClick = { viewModel.showSearch(!searchQuery.isNotEmpty()) }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Buscar",
                        tint = CyanPrimary
                    )
                }
            }
        )

        // Search bar
        if (searchQuery.isNotEmpty() || viewModel.showSearchBar.collectAsState().value) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearch(it) },
                onSearch = { viewModel.updateSearch(it) },
                active = false,
                onActiveChange = {},
                placeholder = { Text("Buscar canales...") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = CyanPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearch("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Limpiar", tint = TextSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = SearchBarDefaults.colors(
                    containerColor = DarkSurfaceVariant
                )
            ) {}
        }

        // Category chips
        when (categories) {
            is Resource.Success -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text("Todos") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = DarkBackground,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                    items(categories.data) { cat ->
                        FilterChip(
                            selected = selectedCategory?.categoryId == cat.categoryId,
                            onClick = { viewModel.selectCategory(cat) },
                            label = { Text(cat.categoryName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = DarkBackground,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }
            else -> {}
        }

        // Content
        when (streams) {
            is Resource.Loading -> LoadingIndicator()
            is Resource.Error -> ErrorMessage(
                message = streams.message,
                onRetry = { viewModel.loadData() }
            )
            is Resource.Success -> {
                val filteredStreams = if (searchQuery.isNotEmpty()) {
                    streams.data.filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    }
                } else {
                    streams.data
                }

                if (filteredStreams.isEmpty()) {
                    EmptyState(
                        icon = Icons.Filled.Tv,
                        title = "Sin canales",
                        message = "No se encontraron canales en esta categoría"
                    )
                } else if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(100.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredStreams) { stream ->
                            ChannelGridItem(
                                name = stream.name,
                                logo = stream.streamIcon,
                                onClick = {
                                    onPlay("live", stream.streamId.toString(), stream.name)
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredStreams) { stream ->
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
                }
            }
        }
    }
}
