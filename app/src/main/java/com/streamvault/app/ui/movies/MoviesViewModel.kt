package com.streamvault.app.ui.movies

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
class MoviesViewModel @Inject constructor(
    private val repository: XtreamRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<Resource<List<Category>>>(Resource.Loading)
    val categories: StateFlow<Resource<List<Category>>> = _categories

    private val _streams = MutableStateFlow<Resource<List<VodStream>>>(Resource.Loading)
    val streams: StateFlow<Resource<List<VodStream>>> = _streams

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _showSearchBar = MutableStateFlow(false)
    val showSearchBar: StateFlow<Boolean> = _showSearchBar

    fun loadData() {
        viewModelScope.launch {
            repository.getVodCategories().collect { _categories.value = it }
        }
        viewModelScope.launch {
            repository.getVodStreams().collect { _streams.value = it }
        }
    }

    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
        viewModelScope.launch {
            if (category == null) {
                repository.getVodStreams().collect { _streams.value = it }
            } else {
                repository.getVodStreamsByCategory(category.categoryId).collect { _streams.value = it }
            }
        }
    }

    fun updateSearch(query: String) { _searchQuery.value = query }

    fun showSearch(show: Boolean) {
        _showSearchBar.value = show
        if (!show) _searchQuery.value = ""
    }
}
