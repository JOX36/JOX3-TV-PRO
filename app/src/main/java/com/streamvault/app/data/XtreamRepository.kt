package com.streamvault.app.data

import com.streamvault.app.api.XtreamApi
import com.streamvault.app.data.models.*
import com.streamvault.app.di.DynamicUrlInterceptor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XtreamRepository @Inject constructor(
    private val api: XtreamApi,
    private val favoritesDao: FavoritesDao,
    private val historyDao: WatchHistoryDao,
    private val configDao: ServerConfigDao,
    private val dynamicUrlInterceptor: DynamicUrlInterceptor
) {

    private var currentConfig: ServerConfig? = null

    fun getConfig(): ServerConfig? = currentConfig

    suspend fun setActiveConfig(config: ServerConfig) {
        currentConfig = config
        dynamicUrlInterceptor.baseUrl = config.baseUrl
    }

    suspend fun getActiveDbConfig(): ServerConfigEntity? = configDao.getActiveConfig()

    fun getAllServerConfigs(): Flow<List<ServerConfigEntity>> = configDao.getAllConfigs()

    suspend fun saveServerConfig(config: ServerConfigEntity) {
        configDao.insert(config)
    }

    suspend fun deleteServerConfig(id: Long) = configDao.deleteById(id)

    suspend fun activateServer(config: ServerConfigEntity) {
        configDao.deactivateAll()
        configDao.update(config.copy(isActive = true))

        currentConfig = ServerConfig(
            name = config.name,
            url = config.url,
            username = config.username,
            password = config.password
        )
        dynamicUrlInterceptor.baseUrl = currentConfig!!.baseUrl
    }

    private fun requireConfig(): ServerConfig =
        currentConfig ?: throw IllegalStateException("No server configured")

    // ─── Auth ──────────────────────────────────────────────────

    suspend fun authenticate(config: ServerConfig): UserAuthResponse {
        currentConfig = config
        dynamicUrlInterceptor.baseUrl = config.baseUrl
        return api.authenticate(config.username, config.password)
    }

    // ─── Live ──────────────────────────────────────────────────

    fun getLiveCategories(): Flow<Resource<List<Category>>> = flow {
        emit(Resource.Loading)
        try {
            val cfg = requireConfig()
            val result = api.getLiveCategories(cfg.username, cfg.password)
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error"))
        }
    }

    fun getLiveStreams(): Flow<Resource<List<LiveStream>>> = flow {
        emit(Resource.Loading)
        try {
            val cfg = requireConfig()
            val result = api.getLiveStreams(cfg.username, cfg.password)
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error"))
        }
    }

    fun getLiveStreamsByCategory(categoryId: String): Flow<Resource<List<LiveStream>>> = flow {
        emit(Resource.Loading)
        try {
            val cfg = requireConfig()
            val result = api.getLiveStreamsByCategory(cfg.username, cfg.password, categoryId = categoryId)
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error"))
        }
    }

    // ─── VOD ───────────────────────────────────────────────────

    fun getVodCategories(): Flow<Resource<List<Category>>> = flow {
        emit(Resource.Loading)
        try {
            val cfg = requireConfig()
            val result = api.getVodCategories(cfg.username, cfg.password)
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error"))
        }
    }

    fun getVodStreams(): Flow<Resource<List<VodStream>>> = flow {
        emit(Resource.Loading)
        try {
            val cfg = requireConfig()
            val result = api.getVodStreams(cfg.username, cfg.password)
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error"))
        }
    }

    fun getVodStreamsByCategory(categoryId: String): Flow<Resource<List<VodStream>>> = flow {
        emit(Resource.Loading)
        try {
            val cfg = requireConfig()
            val result = api.getVodStreamsByCategory(cfg.username, cfg.password, categoryId = categoryId)
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error"))
        }
    }

    suspend fun getVodInfo(vodId: String): VodInfo {
        val cfg = requireConfig()
        return api.getVodInfo(cfg.username, cfg.password, vodId = vodId)
    }

    // ─── Series ────────────────────────────────────────────────

    fun getSeriesCategories(): Flow<Resource<List<Category>>> = flow {
        emit(Resource.Loading)
        try {
            val cfg = requireConfig()
            val result = api.getSeriesCategories(cfg.username, cfg.password)
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error"))
        }
    }

    fun getSeries(): Flow<Resource<List<SeriesItem>>> = flow {
        emit(Resource.Loading)
        try {
            val cfg = requireConfig()
            val result = api.getSeries(cfg.username, cfg.password)
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error"))
        }
    }

    fun getSeriesByCategory(categoryId: String): Flow<Resource<List<SeriesItem>>> = flow {
        emit(Resource.Loading)
        try {
            val cfg = requireConfig()
            val result = api.getSeriesByCategory(cfg.username, cfg.password, categoryId = categoryId)
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error"))
        }
    }

    suspend fun getSeriesInfo(seriesId: String): SeriesInfo {
        val cfg = requireConfig()
        return api.getSeriesInfo(cfg.username, cfg.password, seriesId = seriesId)
    }

    // ─── EPG ───────────────────────────────────────────────────

    suspend fun getShortEpg(streamId: String): EpgResponse {
        val cfg = requireConfig()
        return api.getShortEpg(cfg.username, cfg.password, streamId = streamId)
    }

    // ─── Favorites ─────────────────────────────────────────────

    fun getAllFavorites(): Flow<List<FavoriteEntity>> = favoritesDao.getAllFavorites()

    fun getFavoritesByType(type: String): Flow<List<FavoriteEntity>> = favoritesDao.getFavoritesByType(type)

    suspend fun isFavorite(id: Int): Boolean = favoritesDao.isFavorite(id)

    suspend fun toggleFavorite(fav: FavoriteEntity) {
        if (favoritesDao.isFavorite(fav.id)) {
            favoritesDao.deleteById(fav.id)
        } else {
            favoritesDao.insert(fav)
        }
    }

    // ─── History ───────────────────────────────────────────────

    fun getRecentHistory(limit: Int = 50): Flow<List<WatchHistoryEntity>> = historyDao.getRecentHistory(limit)

    suspend fun addToHistory(item: WatchHistoryEntity) = historyDao.insert(item)

    suspend fun clearHistory() = historyDao.clearAll()

    // ─── Stream URL Builder ────────────────────────────────────

    fun buildStreamUrl(type: String, streamId: Int, extension: String = "m3u8"): String {
        val cfg = requireConfig()
        return cfg.streamUrl(type, extension, streamId)
    }
}

sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}
