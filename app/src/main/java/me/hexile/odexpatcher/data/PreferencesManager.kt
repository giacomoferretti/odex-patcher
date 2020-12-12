package me.hexile.odexpatcher.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * Probably not the best implementation of DataStorage, but I don't care for now.
 *
 * It should be asynchronous, but I need to get the Boolean and not the Flow<Boolean> for certain
 * things.
 */
class PreferencesManager(context: Context) {

    private val dataStore = context.createDataStore(name = "settings_pref")

    private val ignoreWarning = dataStore.data.map { preferences ->
        preferences[IGNORE_WARNING] ?: false
    }

    fun getIgnoreWarning(): Flow<Boolean> {
        return ignoreWarning
    }

    suspend fun setIgnoreWarning(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[IGNORE_WARNING] = value
        }
    }

    fun getIgnoreWarningBlocking(): Boolean {
        var result: Boolean
        runBlocking {
            result = ignoreWarning.first()
        }
        return result
    }

    fun setIgnoreWarningBlocking(value: Boolean) {
        runBlocking {
            setIgnoreWarning(value)
        }
    }

    companion object {
        val IGNORE_WARNING = preferencesKey<Boolean>("ignore_warning")
    }
}