package com.streamvault.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.data.XtreamRepository
import com.streamvault.app.data.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppInfo(
    val version: String = "1.0.0",
    val buildNumber: String = "1",
    val packageName: String = "com.streamvault.app"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: XtreamRepository
) : ViewModel() {

    val serverConfigs: StateFlow<List<ServerConfigEntity>> = repository.getAllServerConfigs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeConfig = MutableStateFlow<ServerConfigEntity?>(null)
    val activeConfig: StateFlow<ServerConfigEntity?> = _activeConfig

    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog

    private val _appInfo = MutableStateFlow(AppInfo())
    val appInfo: StateFlow<AppInfo> = _appInfo

    init {
        loadActiveConfig()
    }

    private fun loadActiveConfig() {
        viewModelScope.launch {
            val config = repository.getActiveDbConfig()
            _activeConfig.value = config
            if (config != null) {
                val serverConfig = ServerConfig(
                    name = config.name,
                    url = config.url,
                    username = config.username,
                    password = config.password
                )
                repository.setActiveConfig(serverConfig)
                try {
                    val auth = repository.authenticate(serverConfig)
                    _userInfo.value = auth.userInfo
                } catch (_: Exception) {}
            }
        }
    }

    fun showAddDialog(show: Boolean) {
        _showAddDialog.value = show
    }

    fun addServer(name: String, url: String, username: String, password: String) {
        viewModelScope.launch {
            val config = ServerConfigEntity(
                name = name,
                url = url,
                username = username,
                password = password,
                isActive = serverConfigs.value.isEmpty()
            )
            repository.saveServerConfig(config)
            if (serverConfigs.value.size <= 1) {
                activateServer(config)
            }
            _showAddDialog.value = false
        }
    }

    fun activateServer(config: ServerConfigEntity) {
        viewModelScope.launch {
            repository.activateServer(config)
            _activeConfig.value = config
            try {
                val serverConfig = ServerConfig(
                    name = config.name,
                    url = config.url,
                    username = config.username,
                    password = config.password
                )
                val auth = repository.authenticate(serverConfig)
                _userInfo.value = auth.userInfo
            } catch (_: Exception) {}
        }
    }

    fun deleteConfig(id: Long) {
        viewModelScope.launch {
            repository.deleteServerConfig(id)
            if (_activeConfig.value?.id == id) {
                _activeConfig.value = null
                _userInfo.value = null
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
