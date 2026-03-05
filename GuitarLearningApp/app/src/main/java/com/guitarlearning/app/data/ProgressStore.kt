package com.guitarlearning.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "guitar_progress")

class ProgressStore(private val context: Context) {

    private val COMPLETED_LESSONS = stringSetPreferencesKey("completed_lessons")

    val completedLessonIds: Flow<Set<Int>> = context.dataStore.data.map { prefs ->
        prefs[COMPLETED_LESSONS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }

    suspend fun markLessonComplete(lessonId: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[COMPLETED_LESSONS] ?: emptySet()
            prefs[COMPLETED_LESSONS] = current + lessonId.toString()
        }
    }

    suspend fun markLessonIncomplete(lessonId: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[COMPLETED_LESSONS] ?: emptySet()
            prefs[COMPLETED_LESSONS] = current - lessonId.toString()
        }
    }
}
