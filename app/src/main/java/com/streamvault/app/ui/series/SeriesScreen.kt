package com.streamvault.app.ui.series

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamvault.app.data.Resource
import com.streamvault.app.data.models.*
import com.streamvault.app.ui.components.*
import com.streamvault.app.ui.theme.*

@Composable
fun SeriesScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    isTv: Boolean = false,
    viewModel: SeriesViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val series by viewModel.series.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadData() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        StreamVaultTopBar(
            title = "Series",
            subtitle = selectedCategory?.categoryName ?: "Todas las series",
            showBack = !isTv,
            onBack = onBack,
            actions = {
                IconButton(onClick = { viewModel.showSearch(!viewModel.showSearchBar.value) }) {
                    Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = CyanPrimary)
                }
            }
        )

        if (viewModel.showSearchBar.collectAsState().value) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearch(it) },
                onSearch = {},
                active = false,
                onActiveChange = {},
                placeholder = { Text("Buscar series...") },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = CyanPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearch("") }) {
                            Icon(Icons.Filled.Close, null, tint = TextSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = SearchBarDefaults.colors(containerColor = DarkSurfaceVariant)
            ) {}
        }

        val categoriesState = categories
        when (categoriesState) {
            is Resource.Success -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text("Todas") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = DarkBackground,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }

                    items(categoriesState.data) { cat ->
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

        val seriesState = series
        when (seriesState) {
            is Resource.Loading -> LoadingIndicator()
            is Resource.Error -> ErrorMessage(message = seriesState.message, onRetry = { viewModel.loadData() })
            is Resource.Success -> {
                val filtered = if (searchQuery.isNotEmpty()) {
                    seriesState.data.filter { it.name.contains(searchQuery, ignoreCase = true) }
                } else seriesState.data

                if (filtered.isEmpty()) {
                    EmptyState(
                        icon = Icons.Filled.Tv,
                        title = "Sin series",
                        message = "No se encontraron series"
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered.chunked(2)) { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                row.forEach { item ->
                                    MediaCard(
                                        title = item.name,
                                        imageUrl = item.cover,
                                        rating = item.rating,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            onNavigate("series_detail/${item.seriesId}")
                                        }
                                    )
                                }
                                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
