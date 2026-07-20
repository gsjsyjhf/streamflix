package com.streamflix.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val contentId: String,
    val title: String,
    val posterUrl: String?,
    val contentType: String,
    val addedAt: Long
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val id: String,
    val contentId: String,
    val title: String,
    val posterUrl: String?,
    val contentType: String,
    val progressSeconds: Long,
    val durationSeconds: Long,
    val updatedAt: Long
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val query: String,
    val searchedAt: Long
)

/**
 * آخر فيلم نقر عليه المستخدم من نتائج البحث
 * مثل Cinemana - عند فتح البحث تظهر آخر نتيجة نقر عليها
 */
@Entity(tableName = "last_clicked_search")
data class LastClickedSearchEntity(
    @PrimaryKey val id: String = "singleton",
    val movieId: String,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val query: String,
    val clickedAt: Long
)
