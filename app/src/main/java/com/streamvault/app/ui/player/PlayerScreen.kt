package com.streamvault.app.ui.player

import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.os.Build
import android.util.Rational
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import com.streamvault.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    type: String,
    streamId: String,
    title: String,
    onBack: () -> Unit,
    isTv: Boolean = false,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val player by viewModel.player.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val showControls by viewModel.showControls.collectAsState()
    val channels by viewModel.channels.collectAsState()
    val showChannelGrid by viewModel.showChannelGrid.collectAsState()
    val audioTracks by viewModel.audioTracks.collectAsState()
    val subtitleTracks by viewModel.subtitleTracks.collectAsState()
    val showTrackSelector by viewModel.showTrackSelector.collectAsState()

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(type, streamId) {
        viewModel.initializePlayer(type, streamId.toIntOrNull() ?: 0, title)
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Auto-hide controls
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(5000)
            viewModel.showControls(false)
        }
    }

    // Handle remote/keyboard input
    fun handleKey(keyCode: Int): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                viewModel.togglePlayPause()
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                viewModel.seekBackward()
                viewModel.showControls(true)
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                viewModel.seekForward()
                viewModel.showControls(true)
                true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                viewModel.showControls(true)
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                viewModel.toggleChannelGrid()
                true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                viewModel.togglePlayPause()
                true
            }
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                viewModel.seekForward()
                true
            }
            KeyEvent.KEYCODE_MEDIA_REWIND -> {
                viewModel.seekBackward()
                true
            }
            else -> false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    handleKey(event.nativeKeyEvent.keyCode)
                } else false
            }
    ) {
        // Video Surface
        val currentPlayer = player
        if (currentPlayer != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = currentPlayer
                        useController = false
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Controls Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PlayerControlsOverlay(
                title = title,
                subtitle = playbackState.subtitle,
                isPlaying = playbackState.isPlaying,
                currentPosition = playbackState.currentPosition,
                duration = playbackState.duration,
                type = type,
                onPlayPause = { viewModel.togglePlayPause() },
                onSeekForward = { viewModel.seekForward() },
                onSeekBackward = { viewModel.seekBackward() },
                onChannelGrid = { viewModel.toggleChannelGrid() },
                onPip = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val params = PictureInPictureParams.Builder()
                            .setAspectRatio(Rational(16, 9))
                            .build()
                        (context as? android.app.Activity)?.enterPictureInPictureMode(params)
                    }
                },
                onBack = onBack,
                onToggleSubtitles = { viewModel.toggleSubtitles() },
                onToggleAudio = { viewModel.toggleAudioTrack() },
                onShowTrackSelector = { viewModel.showTrackSelector(true) },
                isTv = isTv
            )
        }

        // Channel Grid Overlay
        if (showChannelGrid) {
            ChannelGridOverlay(
                channels = channels,
                currentStreamId = streamId.toIntOrNull() ?: 0,
                onChannelSelect = { id, name ->
                    viewModel.switchChannel(id, name)
                },
                onDismiss = { viewModel.toggleChannelGrid() }
            )
        }

        // Track Selector
        if (showTrackSelector) {
            TrackSelectorDialog(
                audioTracks = audioTracks,
                subtitleTracks = subtitleTracks,
                onAudioSelected = { viewModel.selectAudioTrack(it) },
                onSubtitleSelected = { viewModel.selectSubtitleTrack(it) },
                onDismiss = { viewModel.showTrackSelector(false) }
            )
        }

        // Click to toggle controls
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .noRippleClickable {
                    viewModel.showControls(!showControls)
                }
        )
    }
}

@Composable
private fun PlayerControlsOverlay(
    title: String,
    subtitle: String,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    type: String,
    onPlayPause: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onChannelGrid: () -> Unit,
    onPip: () -> Unit,
    onBack: () -> Unit,
    onToggleSubtitles: () -> Unit,
    onToggleAudio: () -> Unit,
    onShowTrackSelector: () -> Unit,
    isTv: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.7f),
                        Color.Transparent,
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.7f)
                    )
                )
            )
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // PiP button
            if (!isTv) {
                IconButton(onClick = onPip) {
                    Icon(
                        imageVector = Icons.Filled.PictureInPictureAlt,
                        contentDescription = "PiP",
                        tint = Color.White
                    )
                }
            }
        }

        // Center controls
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Seek backward
            PlayerControlButton(
                icon = Icons.Filled.Replay10,
                onClick = onSeekBackward,
                size = 48.dp
            )

            // Play/Pause
            PlayerControlButton(
                icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                onClick = onPlayPause,
                size = 72.dp,
                isPrimary = true
            )

            // Seek forward
            PlayerControlButton(
                icon = Icons.Filled.Forward30,
                onClick = onSeekForward,
                size = 48.dp
            )
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // Progress bar (only for VOD)
            if (type != "live" && duration > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(currentPosition),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                    Slider(
                        value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                        onValueChange = { /* seek */ },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = CyanPrimary,
                            activeTrackColor = CyanPrimary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                    Text(
                        text = formatTime(duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (type == "live") {
                    ControlChip(
                        icon = Icons.Filled.GridView,
                        label = "Canales",
                        onClick = onChannelGrid
                    )
                }
                ControlChip(
                    icon = Icons.Filled.Subtitles,
                    label = "Subtítulos",
                    onClick = onToggleSubtitles
                )
                ControlChip(
                    icon = Icons.Filled.AudioFile,
                    label = "Audio",
                    onClick = onToggleAudio
                )
                ControlChip(
                    icon = Icons.Filled.Tune,
                    label = "Ajustes",
                    onClick = onShowTrackSelector
                )
                ControlChip(
                    icon = Icons.Filled.ScreenLockPortrait,
                    label = "Bloquear",
                    onClick = { /* lock screen */ }
                )
            }
        }
    }
}

@Composable
private fun PlayerControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp,
    isPrimary: Boolean = false
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .background(
                color = if (isPrimary) CyanPrimary.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.2f),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isPrimary) DarkBackground else Color.White,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}

@Composable
private fun ControlChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ChannelGridOverlay(
    channels: List<com.streamvault.app.data.models.LiveStream>,
    currentStreamId: Int,
    onChannelSelect: (Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📺 Canales",
                    style = MaterialTheme.typography.headlineSmall,
                    color = CyanPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(120.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(channels.size) { index ->
                    val channel = channels[index]
                    val isCurrentChannel = channel.streamId == currentStreamId

                    Surface(
                        onClick = {
                            onChannelSelect(channel.streamId, channel.name)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCurrentChannel) CyanPrimary.copy(alpha = 0.2f) else DarkSurfaceVariant,
                        border = if (isCurrentChannel) {
                            BorderStroke(1.dp, CyanPrimary)
                        } else null
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = channel.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCurrentChannel) CyanPrimary else Color.White,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackSelectorDialog(
    audioTracks: List<String>,
    subtitleTracks: List<String>,
    onAudioSelected: (Int) -> Unit,
    onSubtitleSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Ajustes de pista",
                color = CyanPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "🔊 Audio",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                audioTracks.forEachIndexed { index, track ->
                    Surface(
                        onClick = {
                            onAudioSelected(index)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurfaceVariant
                    ) {
                        Text(
                            text = track,
                            modifier = Modifier.padding(12.dp),
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "💬 Subtítulos",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    onClick = {
                        onSubtitleSelected(-1) // disable
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurfaceVariant
                ) {
                    Text(
                        text = "Desactivar subtítulos",
                        modifier = Modifier.padding(12.dp),
                        color = ErrorRed
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                subtitleTracks.forEachIndexed { index, track ->
                    Surface(
                        onClick = {
                            onSubtitleSelected(index)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurfaceVariant
                    ) {
                        Text(
                            text = track,
                            modifier = Modifier.padding(12.dp),
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = CyanPrimary)
            }
        }
    )
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier {
    return this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onClick
        )
    )
}
