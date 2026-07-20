package com.streamflix.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.streamflix.app.data.local.dao.FavoriteDao
import com.streamflix.app.data.local.dao.LastClickedSearchDao
import com.streamflix.app.data.local.dao.SearchHistoryDao
import com.streamflix.app.data.local.dao.WatchHistoryDao
import com.streamflix.app.data.local.entity.FavoriteEntity
import com.streamflix.app.data.local.entity.LastClickedSearchEntity
import com.streamflix.app.data.local.entity.SearchHistoryEntity
import com.streamflix.app.data.local.entity.WatchHistoryEntity

@Database(
    entities = [
        FavoriteEntity::class,
        WatchHistoryEntity::class,
        SearchHistoryEntity::class,
        LastClickedSearchEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class StreamFlixDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun lastClickedSearchDao(): LastClickedSearchDao

    companion object {
        const val NAME = "streamflix.db"
    }
}
