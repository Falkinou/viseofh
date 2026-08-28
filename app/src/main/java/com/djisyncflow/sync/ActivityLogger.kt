package com.djisyncflow.sync

import android.content.Context
import com.djisyncflow.data.ActivityEventEntity
import com.djisyncflow.data.ActivityEventLevel
import com.djisyncflow.data.AppDatabase

class ActivityLogger(context: Context) {
    private val dao = AppDatabase.get(context).activityEventDao()

    suspend fun info(message: String) = log(ActivityEventLevel.INFO, message)
    suspend fun success(message: String) = log(ActivityEventLevel.SUCCESS, message)
    suspend fun warning(message: String) = log(ActivityEventLevel.WARNING, message)
    suspend fun error(message: String) = log(ActivityEventLevel.ERROR, message)

    private suspend fun log(level: String, message: String) {
        dao.insert(ActivityEventEntity(level = level, message = message.take(500)))
        dao.trim()
    }
}
