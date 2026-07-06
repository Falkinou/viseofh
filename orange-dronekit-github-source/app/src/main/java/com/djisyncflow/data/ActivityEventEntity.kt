package com.djisyncflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

object ActivityEventLevel {
    const val INFO = "Info"
    const val SUCCESS = "Succes"
    const val WARNING = "Alerte"
    const val ERROR = "Erreur"
}

@Entity(tableName = "activity_events")
data class ActivityEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val level: String = ActivityEventLevel.INFO,
    val message: String,
)
