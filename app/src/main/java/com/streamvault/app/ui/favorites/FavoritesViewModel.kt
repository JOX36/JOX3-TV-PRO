package com.streamvault.app.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.data.XtreamRepository
import com.streamvault.app.data.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: XtreamRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab

    val favorites: StateFlow<List<FavoriteEntity>> = _selectedTab.flatMapLatest { tab ->
        when (tab) {
            1 -> repository.getFavoritesByType("live")
            2 -> repository.getFavoritesByType("movie")
            3 -> repository.getFavoritesByType("series")
            else -> repository.getAllFavorites()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun removeFavorite(fav: FavoriteEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(fav)
        }
    }
}
