package com.streamvault.app.ui.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.data.Resource
import com.streamvault.app.data.XtreamRepository
import com.streamvault.app.data.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val repository: XtreamRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<Resource<List<Category>>>(Resource.Loading)
    val categories: StateFlow<Resource<List<Category>>> = _categories

    private val _series = MutableStateFlow<Resource<List<SeriesItem>>>(Resource.Loading)
    val series: StateFlow<Resource<List<SeriesItem>>> = _series

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _showSearchBar = MutableStateFlow(false)
    val showSearchBar: StateFlow<Boolean> = _showSearchBar

    fun loadData() {
        viewModelScope.launch {
            repository.getSeriesCategories().collect { _categories.value = it }
        }
        viewModelScope.launch {
            repository.getSeries().collect { _series.value = it }
        }
    }

    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
        viewModelScope.launch {
            if (category == null) {
                repository.getSeries().collect { _series.value = it }
            } else {
                repository.getSeriesByCategory(category.categoryId).collect { _series.value = it }
            }
        }
    }

    fun updateSearch(query: String) { _searchQuery.value = query }

    fun showSearch(show: Boolean) {
        _showSearchBar.value = show
        if (!show) _searchQuery.value = ""
    }
}

@HiltViewModel
class SeriesDetailViewModel @Inject constructor(
    private val repository: XtreamRepository
) : ViewModel() {

    private val _seriesInfo = MutableStateFlow<SeriesInfo?>(null)
    val seriesInfo: StateFlow<SeriesInfo?> = _seriesInfo

    private val _selectedSeason = MutableStateFlow(0)
    val selectedSeason: StateFlow<Int> = _selectedSeason

    fun loadSeriesInfo(seriesId: String) {
        viewModelScope.launch {
            try {
                val info = repository.getSeriesInfo(seriesId)
                _seriesInfo.value = info
            } catch (_: Exception) {}
        }
    }

    fun selectSeason(index: Int) {
        _selectedSeason.value = index
    }

    fun buildEpisodeUrl(episode: Episode): String {
        val extension = episode.containerExtension ?: "mp4"
        return repository.buildStreamUrl("series", episode.id.toIntOrNull() ?: 0, extension)
    }
}
