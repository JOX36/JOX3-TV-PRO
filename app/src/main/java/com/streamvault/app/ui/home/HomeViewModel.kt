package com.streamvault.app.ui.home

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
class HomeViewModel @Inject constructor(
    private val repository: XtreamRepository
) : ViewModel() {

    private val _liveStreams = MutableStateFlow<Resource<List<LiveStream>>>(Resource.Loading)
    val liveStreams: StateFlow<Resource<List<LiveStream>>> = _liveStreams

    private val _vodStreams = MutableStateFlow<Resource<List<VodStream>>>(Resource.Loading)
    val vodStreams: StateFlow<Resource<List<VodStream>>> = _vodStreams

    private val _seriesItems = MutableStateFlow<Resource<List<SeriesItem>>>(Resource.Loading)
    val seriesItems: StateFlow<Resource<List<SeriesItem>>> = _seriesItems

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = repository.getRecentHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadHomeData() {
        viewModelScope.launch {
            repository.getLiveStreams().collect { _liveStreams.value = it }
        }
        viewModelScope.launch {
            repository.getVodStreams().collect { _vodStreams.value = it }
        }
        viewModelScope.launch {
            repository.getSeries().collect { _seriesItems.value = it }
        }
    }
}
