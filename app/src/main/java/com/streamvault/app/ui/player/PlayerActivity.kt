package com.streamvault.app.ui.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.theme.StreamVaultTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val type = intent.getStringExtra("type") ?: "live"
        val streamId = intent.getStringExtra("streamId") ?: "0"
        val title = intent.getStringExtra("title") ?: ""

        setContent {
            StreamVaultTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlayerScreen(
                        type = type,
                        streamId = streamId,
                        title = title,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}
