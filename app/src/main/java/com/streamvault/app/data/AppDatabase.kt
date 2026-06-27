package com.streamvault.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.streamvault.app.data.models.FavoriteEntity
import com.streamvault.app.data.models.WatchHistoryEntity
import com.streamvault.app.data.models.ServerConfigEntity

@Database(
    entities = [FavoriteEntity::class, WatchHistoryEntity::class, ServerConfigEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun serverConfigDao(): ServerConfigDao
}
