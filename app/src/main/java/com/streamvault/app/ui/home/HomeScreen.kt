package com.streamvault.app.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamvault.app.data.Resource
import com.streamvault.app.data.models.*
import com.streamvault.app.ui.components.*
import com.streamvault.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onPlay: (String, String, String) -> Unit,
    isTv: Boolean = false,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val liveStreams by viewModel.liveStreams.collectAsState()
    val vodStreams by viewModel.vodStreams.collectAsState()
    val seriesItems by viewModel.seriesItems.collectAsState()
    val history by viewModel.watchHistory.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

    if (isTv) {
        TvHomeContent(
            liveStreams = liveStreams,
            vodStreams = vodStreams,
            seriesItems = seriesItems,
            history = history,
            onNavigate = onNavigate,
            onPlay = onPlay
        )
    } else {
        MobileHomeContent(
            liveStreams = liveStreams,
            vodStreams = vodStreams,
            seriesItems = seriesItems,
            history = history,
            onNavigate = onNavigate,
            onPlay = onPlay
        )
    }
}

@Composable
private fun MobileHomeContent(
    liveStreams: Resource<List<LiveStream>>,
    vodStreams: Resource<List<VodStream>>,
    seriesItems: Resource<List<SeriesItem>>,
    history: List<WatchHistoryEntity>,
    onNavigate: (String) -> Unit,
    onPlay: (String, String, String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Hero Carousel
            item {
                HeroCarousel(
                    vodStreams = (vodStreams as? Resource.Success)?.data?.take(10) ?: emptyList(),
                    onPlay = onPlay
                )
            }

            // Quick Actions
            item {
                QuickActions(onNavigate = onNavigate)
            }

            // Continue Watching
            if (history.isNotEmpty()) {
                item {
                    SectionHeader(title = "▶ Continuar viendo")
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(history.take(10)) { item ->
                            MediaCard(
                                title = item.name,
                                imageUrl = item.icon,
                                showProgress = item.duration > 0,
                                progress = if (item.duration > 0) (item.lastPosition.toFloat() / item.duration) else 0f,
                                onClick = {
                                    onPlay(item.type, item.itemId.toString(), item.name)
                                }
                            )
                        }
                    }
                }
            }

            // Live TV Section
            item {
                SectionHeader(
                    title = "📺 TV en Vivo",
                    subtitle = "Canales en directo",
                    onSeeAll = { onNavigate("live") }
                )
            }
            item {
                when (liveStreams) {
                    is Resource.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = CyanPrimary)
                        }
                    }
                    is Resource.Success -> {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(liveStreams.data.take(20)) { stream ->
                                MediaCard(
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
                    is Resource.Error -> {
                        ErrorMessage(message = liveStreams.message)
                    }
                }
            }

            // Movies Section
            item {
                SectionHeader(
                    title = "🎬 Películas",
                    subtitle = "Las mejores películas",
                    onSeeAll = { onNavigate("movies") }
                )
            }
            item {
                when (vodStreams) {
                    is Resource.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = CyanPrimary)
                        }
                    }
                    is Resource.Success -> {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(vodStreams.data.take(20)) { movie ->
                                MediaCard(
                                    title = movie.name,
                                    imageUrl = movie.streamIcon,
                                    rating = movie.rating,
                                    onClick = {
                                        onPlay("movie", movie.streamId.toString(), movie.name)
                                    }
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        ErrorMessage(message = vodStreams.message)
                    }
                }
            }

            // Series Section
            item {
                SectionHeader(
                    title = "📺 Series",
                    subtitle = "Series y temporadas",
                    onSeeAll = { onNavigate("series") }
                )
            }
            item {
                when (seriesItems) {
                    is Resource.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = CyanPrimary)
                        }
                    }
                    is Resource.Success -> {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(seriesItems.data.take(20)) { series ->
                                MediaCard(
                                    title = series.name,
                                    imageUrl = series.cover,
                                    rating = series.rating,
                                    onClick = {
                                        onNavigate("series_detail/${series.seriesId}")
                                    }
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        ErrorMessage(message = seriesItems.message)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Bottom bar
        StreamVaultBottomBar(
            currentRoute = "home",
            onNavigate = onNavigate,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun TvHomeContent(
    liveStreams: Resource<List<LiveStream>>,
    vodStreams: Resource<List<VodStream>>,
    seriesItems: Resource<List<SeriesItem>>,
    history: List<WatchHistoryEntity>,
    onNavigate: (String) -> Unit,
    onPlay: (String, String, String) -> Unit
) {
    // TV version uses larger cards and focus-based navigation
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(48.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "StreamVault",
                style = MaterialTheme.typography.displaySmall,
                color = CyanPrimary
            )
        }

        // Quick nav for TV
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TvQuickButton("📺 TV Live", Icons.Filled.LiveTv) { onNavigate("live") }
                TvQuickButton("🎬 Películas", Icons.Filled.Movie) { onNavigate("movies") }
                TvQuickButton("📺 Series", Icons.Filled.Tv) { onNavigate("series") }
                TvQuickButton("❤️ Favoritos", Icons.Filled.Favorite) { onNavigate("favorites") }
                TvQuickButton("🔍 Buscar", Icons.Filled.Search) { onNavigate("search") }
                TvQuickButton("⚙️ Ajustes", Icons.Filled.Settings) { onNavigate("settings") }
            }
        }

        // Continue watching
        if (history.isNotEmpty()) {
            item {
                Text(
                    text = "▶ Continuar viendo",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(history.take(10)) { item ->
                        MediaCard(
                            title = item.name,
                            imageUrl = item.icon,
                            showProgress = item.duration > 0,
                            progress = if (item.duration > 0) (item.lastPosition.toFloat() / item.duration) else 0f,
                            onClick = {
                                onPlay(item.type, item.itemId.toString(), item.name)
                            }
                        )
                    }
                }
            }
        }

        // Live preview
        item {
            Text(
                text = "📺 TV en Vivo",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
        }
        item {
            when (liveStreams) {
                is Resource.Success -> {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(liveStreams.data.take(15)) { stream ->
                            MediaCard(
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
                else -> {}
            }
        }

        // Movies
        item {
            Text(
                text = "🎬 Películas",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
        }
        item {
            when (vodStreams) {
                is Resource.Success -> {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(vodStreams.data.take(15)) { movie ->
                            MediaCard(
                                title = movie.name,
                                imageUrl = movie.streamIcon,
                                rating = movie.rating,
                                onClick = {
                                    onPlay("movie", movie.streamId.toString(), movie.name)
                                }
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun HeroCarousel(
    vodStreams: List<VodStream>,
    onPlay: (String, String, String) -> Unit
) {
    if (vodStreams.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { vodStreams.size })
    val scope = rememberCoroutineScope()

    // Auto-scroll
    LaunchedEffect(pagerState) {
        while (true) {
            delay(5000)
            val nextPage = (pagerState.currentPage + 1) % vodStreams.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) { page ->
            val movie = vodStreams[page]
            CarouselCard(
                title = movie.name,
                imageUrl = movie.streamIcon,
                subtitle = movie.rating?.let { "⭐ $it" },
                onClick = {
                    onPlay("movie", movie.streamId.toString(), movie.name)
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Page indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(vodStreams.size.coerceAtMost(10)) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(
                            width = if (index == pagerState.currentPage) 24.dp else 8.dp,
                            height = 8.dp
                        )
                        .clip(MaterialTheme.shapes.small)
                        .background(
                            if (index == pagerState.currentPage) CyanPrimary
                            else TextTertiary.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

@Composable
private fun QuickActions(onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionButton("📺", "TV Live") { onNavigate("live") }
        QuickActionButton("🎬", "Películas") { onNavigate("movies") }
        QuickActionButton("📺", "Series") { onNavigate("series") }
        QuickActionButton("❤️", "Favoritos") { onNavigate("favorites") }
        QuickActionButton("🔍", "Buscar") { onNavigate("search") }
    }
}

@Composable
private fun QuickActionButton(emoji: String, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            shape = MaterialTheme.shapes.medium,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = DarkSurfaceVariant,
                contentColor = CyanPrimary
            )
        ) {
            Text(text = emoji, style = MaterialTheme.typography.titleLarge)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun TvQuickButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = DarkSurfaceVariant,
            contentColor = CyanPrimary
        )
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}
