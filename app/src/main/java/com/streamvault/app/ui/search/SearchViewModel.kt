package com.streamvault.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.data.Resource
import com.streamvault.app.data.XtreamRepository
import com.streamvault.app.data.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: XtreamRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _liveResults = MutableStateFlow<List<LiveStream>>(emptyList())
    val liveResults: StateFlow<List<LiveStream>> = _liveResults

    private val _movieResults = MutableStateFlow<List<VodStream>>(emptyList())
    val movieResults: StateFlow<List<VodStream>> = _movieResults

    private val _seriesResults = MutableStateFlow<List<SeriesItem>>(emptyList())
    val seriesResults: StateFlow<List<SeriesItem>> = _seriesResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private var allLiveStreams = listOf<LiveStream>()
    private var allMovies = listOf<VodStream>()
    private var allSeries = listOf<SeriesItem>()
    private var searchJob: Job? = null

    init {
        // Pre-load all data for local search
        viewModelScope.launch {
            repository.getLiveStreams().collect {
                if (it is Resource.Success) allLiveStreams = it.data
            }
        }
        viewModelScope.launch {
            repository.getVodStreams().collect {
                if (it is Resource.Success) allMovies = it.data
            }
        }
        viewModelScope.launch {
            repository.getSeries().collect {
                if (it is Resource.Success) allSeries = it.data
            }
        }
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
        if (query.length >= 2) {
            performSearch(query)
        } else {
            clearResults()
        }
    }

    fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(300) // debounce

            val q = query.lowercase()
            _liveResults.value = allLiveStreams.filter {
                it.name.lowercase().contains(q)
            }
            _movieResults.value = allMovies.filter {
                it.name.lowercase().contains(q)
            }
            _seriesResults.value = allSeries.filter {
                it.name.lowercase().contains(q)
            }

            _isSearching.value = false
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        clearResults()
    }

    private fun clearResults() {
        _liveResults.value = emptyList()
        _movieResults.value = emptyList()
        _seriesResults.value = emptyList()
    }
}
