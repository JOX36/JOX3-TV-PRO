package com.streamvault.app.data

import androidx.room.*
import com.streamvault.app.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE type = :type ORDER BY addedAt DESC")
    fun getFavoritesByType(type: String): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    suspend fun isFavorite(id: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Delete
    suspend fun delete(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 50): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE type = :type ORDER BY watchedAt DESC LIMIT :limit")
    fun getHistoryByType(type: String, limit: Int = 50): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE itemId = :itemId LIMIT 1")
    suspend fun getHistoryItem(itemId: Int): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}

@Dao
interface ServerConfigDao {
    @Query("SELECT * FROM server_configs ORDER BY addedAt DESC")
    fun getAllConfigs(): Flow<List<ServerConfigEntity>>

    @Query("SELECT * FROM server_configs WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveConfig(): ServerConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: ServerConfigEntity)

    @Update
    suspend fun update(config: ServerConfigEntity)

    @Query("UPDATE server_configs SET isActive = 0")
    suspend fun deactivateAll()

    @Query("DELETE FROM server_configs WHERE id = :id")
    suspend fun deleteById(id: Long)
}
