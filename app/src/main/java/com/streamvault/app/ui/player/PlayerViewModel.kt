package com.streamvault.app.ui.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.SubtitleConfiguration
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.streamvault.app.data.XtreamRepository
import com.streamvault.app.data.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaybackUiState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val title: String = "",
    val subtitle: String = "",
    val quality: String = "",
    val audioTrack: String = "",
    val subtitleTrack: String = ""
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: XtreamRepository
) : ViewModel() {

    private var exoPlayer: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null

    private val _player = MutableStateFlow<ExoPlayer?>(null)
    val player: StateFlow<ExoPlayer?> = _player

    private val _playbackState = MutableStateFlow(PlaybackUiState())
    val playbackState: StateFlow<PlaybackUiState> = _playbackState

    private val _showControls = MutableStateFlow(true)
    val showControls: StateFlow<Boolean> = _showControls

    private val _channels = MutableStateFlow<List<LiveStream>>(emptyList())
    val channels: StateFlow<List<LiveStream>> = _channels

    private val _showChannelGrid = MutableStateFlow(false)
    val showChannelGrid: StateFlow<Boolean> = _showChannelGrid

    private val _audioTracks = MutableStateFlow<List<String>>(emptyList())
    val audioTracks: StateFlow<List<String>> = _audioTracks

    private val _subtitleTracks = MutableStateFlow<List<String>>(emptyList())
    val subtitleTracks: StateFlow<List<String>> = _subtitleTracks

    private val _showTrackSelector = MutableStateFlow(false)
    val showTrackSelector: StateFlow<Boolean> = _showTrackSelector

    private var currentType = "live"
    private var currentStreamId = 0
    private var currentTitle = ""

    fun initializePlayer(type: String, streamId: Int, title: String) {
        currentType = type
        currentStreamId = streamId
        currentTitle = title

        releasePlayer()

        trackSelector = DefaultTrackSelector(context).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }

        exoPlayer = ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector!!)
            .build()
            .apply {
                val extension = when (type) {
                    "live" -> "m3u8"
                    "movie" -> "mp4"
                    "series" -> "mp4"
                    else -> "m3u8"
                }
                val url = repository.buildStreamUrl(type, streamId, extension)

                val mediaItem = MediaItem.Builder()
                    .setUri(url)
                    .build()

                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        updatePlaybackState()
                        if (playbackState == Player.STATE_READY) {
                            extractTracks()
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        // Handle error
                    }
                })
            }

        _player.value = exoPlayer
        _playbackState.value = _playbackState.value.copy(title = title)

        // Load channels for live TV
        if (type == "live") {
            loadChannels()
        }

        // Start position updater
        viewModelScope.launch {
            while (true) {
                delay(1000)
                updatePlaybackState()
            }
        }
    }

    private fun loadChannels() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getLiveStreams().collect { resource ->
                    if (resource is com.streamvault.app.data.Resource.Success) {
                        _channels.value = resource.data
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun updatePlaybackState() {
        exoPlayer?.let { player ->
            _playbackState.value = _playbackState.value.copy(
                currentPosition = player.currentPosition,
                duration = player.duration.coerceAtLeast(0),
                isPlaying = player.isPlaying
            )
        }
    }

    private fun extractTracks() {
        exoPlayer?.let { player ->
            val audioList = mutableListOf<String>()
            val subtitleList = mutableListOf<String>()

            for (i in 0 until player.currentTracks.groups.size) {
                val group = player.currentTracks.groups[i]
                when (group.type) {
                    C.TRACK_TYPE_AUDIO -> {
                        for (j in 0 until group.length) {
                            val format = group.getTrackFormat(j)
                            audioList.add(format.label ?: "Audio ${j + 1}")
                        }
                    }
                    C.TRACK_TYPE_TEXT -> {
                        for (j in 0 until group.length) {
                            val format = group.getTrackFormat(j)
                            subtitleList.add(format.label ?: "Subtítulo ${j + 1}")
                        }
                    }
                }
            }

            _audioTracks.value = audioList
            _subtitleTracks.value = subtitleList
        }
    }

    fun togglePlayPause() {
        exoPlayer?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun seekForward() {
        exoPlayer?.seekForward()
    }

    fun seekBackward() {
        exoPlayer?.seekBack()
    }

    fun showControls(show: Boolean) {
        _showControls.value = show
    }

    fun toggleChannelGrid() {
        _showChannelGrid.value = !_showChannelGrid.value
    }

    fun switchChannel(streamId: Int, name: String) {
        currentStreamId = streamId
        currentTitle = name
        initializePlayer("live", streamId, name)
    }

    fun toggleSubtitles() {
        // Toggle subtitle visibility
        trackSelector?.let { selector ->
            val params = selector.parameters
            if (params.disabledTextTrackSelectionFlags > 0) {
                selector.setParameters(params.buildUpon().setRendererDisabled(C.TRACK_TYPE_TEXT, false))
            } else {
                selector.setParameters(params.buildUpon().setRendererDisabled(C.TRACK_TYPE_TEXT, true))
            }
        }
    }

    fun toggleAudioTrack() {
        // Cycle through audio tracks
    }

    fun selectAudioTrack(index: Int) {
        trackSelector?.let { selector ->
            selector.setParameters(
                selector.parameters.buildUpon()
                    .setPreferredAudioLanguage("und")
            )
        }
    }

    fun selectSubtitleTrack(index: Int) {
        if (index == -1) {
            trackSelector?.setParameters(
                trackSelector!!.parameters.buildUpon()
                    .setRendererDisabled(C.TRACK_TYPE_TEXT, true)
            )
        } else {
            trackSelector?.setParameters(
                trackSelector!!.parameters.buildUpon()
                    .setRendererDisabled(C.TRACK_TYPE_TEXT, false)
            )
        }
    }

    fun showTrackSelector(show: Boolean) {
        _showTrackSelector.value = show
    }

    private fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
        _player.value = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}
