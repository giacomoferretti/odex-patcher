/*
 * Copyright 2020 Giacomo Ferretti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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