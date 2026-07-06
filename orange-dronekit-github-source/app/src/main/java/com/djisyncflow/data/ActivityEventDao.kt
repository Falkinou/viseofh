package com.djisyncflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityEventDao {
    @Query("SELECT * FROM activity_events ORDER BY createdAtMillis DESC LIMIT 80")
    fun observeRecent(): Flow<List<ActivityEventEntity>>

    @Insert
    suspend fun insert(event: ActivityEventEntity)

    @Query("DELETE FROM activity_events WHERE id NOT IN (SELECT id FROM activity_events ORDER BY createdAtMillis DESC LIMIT 200)")
    suspend fun trim()
}
