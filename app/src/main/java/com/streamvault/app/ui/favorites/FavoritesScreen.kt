package com.streamvault.app.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamvault.app.data.models.*
import com.streamvault.app.ui.components.*
import com.streamvault.app.ui.theme.*

@Composable
fun FavoritesScreen(
    onBack: () -> Unit,
    onPlay: (String, String, String) -> Unit,
    isTv: Boolean = false,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    val tabs = listOf("Todos", "TV Live", "Películas", "Series")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        StreamVaultTopBar(
            title = "Favoritos",
            showBack = !isTv,
            onBack = onBack
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = CyanPrimary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { viewModel.selectTab(index) },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) CyanPrimary else TextSecondary
                        )
                    }
                )
            }
        }

        if (favorites.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.FavoriteBorder,
                title = "Sin favoritos",
                message = "Agrega canales, películas o series a tus favoritos para verlos aquí"
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favorites) { fav ->
                    WideMediaCard(
                        title = fav.name,
                        imageUrl = fav.icon,
                        subtitle = when (fav.type) {
                            "live" -> "📺 Canal en vivo"
                            "movie" -> "🎬 Película"
                            "series" -> "📺 Serie"
                            else -> ""
                        },
                        onClick = {
                            onPlay(fav.type, fav.id.toString(), fav.name)
                        },
                        trailing = {
                            IconButton(onClick = { viewModel.removeFavorite(fav) }) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Quitar favorito",
                                    tint = ErrorRed
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
