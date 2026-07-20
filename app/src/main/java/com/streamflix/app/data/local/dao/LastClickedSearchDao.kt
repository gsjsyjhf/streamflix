package com.streamflix.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.streamflix.app.data.local.entity.LastClickedSearchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LastClickedSearchDao {
    @Query("SELECT * FROM last_clicked_search WHERE id = 'singleton' LIMIT 1")
    fun observe(): Flow<LastClickedSearchEntity?>

    @Query("SELECT * FROM last_clicked_search WHERE id = 'singleton' LIMIT 1")
    suspend fun get(): LastClickedSearchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: LastClickedSearchEntity)

    @Query("DELETE FROM last_clicked_search")
    suspend fun clear()
}
