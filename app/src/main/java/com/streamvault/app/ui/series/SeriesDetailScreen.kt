package com.streamvault.app.ui.series

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.streamvault.app.data.models.*
import com.streamvault.app.ui.components.*
import com.streamvault.app.ui.theme.*

@Composable
fun SeriesDetailScreen(
    seriesId: String,
    onBack: () -> Unit,
    onPlay: (String, String, String) -> Unit,
    isTv: Boolean = false,
    viewModel: SeriesDetailViewModel = hiltViewModel()
) {
    val seriesInfo by viewModel.seriesInfo.collectAsState()
    val selectedSeason by viewModel.selectedSeason.collectAsState()

    LaunchedEffect(seriesId) {
        viewModel.loadSeriesInfo(seriesId)
    }

    val info = seriesInfo

    if (info == null) {
        LoadingIndicator()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        StreamVaultTopBar(
            title = info.info?.name ?: "Series",
            showBack = true,
            onBack = onBack
        )

        LazyColumn {
            // Backdrop & Info
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    AsyncImage(
                        model = info.info?.backdropPath?.firstOrNull() ?: info.info?.cover,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        DarkBackground.copy(alpha = 0.3f),
                                        DarkBackground
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = info.info?.name ?: "",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            info.info?.rating?.let {
                                Badge(it, Icons.Filled.Star, WarningYellow)
                            }
                            info.info?.genre?.let {
                                Badge(it, Icons.Filled.Category, CyanPrimary)
                            }
                            info.info?.releaseDate?.let {
                                Badge(it, Icons.Filled.CalendarToday, ElectricBlue)
                            }
                        }
                    }
                }
            }

            // Plot
            info.info?.plot?.let { plot ->
                item {
                    Text(
                        text = plot,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // Season selector
            val seasons = info.seasons ?: emptyList()
            if (seasons.isNotEmpty()) {
                item {
                    Text(
                        text = "Temporadas",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(seasons) { index, season ->
                            FilterChip(
                                selected = selectedSeason == index,
                                onClick = { viewModel.selectSeason(index) },
                                label = {
                                    Text(season.name ?: "T${season.seasonNumber}")
                                },
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
            }

            // Episodes
            val episodes = info.episodes?.values?.toList()?.getOrNull(selectedSeason) ?: emptyList()
            if (episodes.isNotEmpty()) {
                item {
                    Text(
                        text = "Episodios",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
                items(episodes) { episode ->
                    WideMediaCard(
                        title = "E${episode.episodeNum} - ${episode.title}",
                        imageUrl = episode.info?.movieImage,
                        subtitle = episode.info?.duration,
                        onClick = {
                            val ext = episode.containerExtension ?: "mp4"
                            onPlay("series", episode.id, "E${episode.episodeNum} - ${episode.title}")
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun Badge(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = DarkSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
