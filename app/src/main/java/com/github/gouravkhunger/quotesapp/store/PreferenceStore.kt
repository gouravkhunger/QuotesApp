/*
 * MIT License
 *
 * Copyright (c) 2024 Gourav Khunger
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.gouravkhunger.quotesapp.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private const val PREFERENCES_NAME = "settings"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

data class Preference<T>(
    val key: Preferences.Key<T>,
    val default: T
) {
    companion object {
        val ASK_NOTIF_PERM = Preference(booleanPreferencesKey("ask_notif_perm"), true)
        val CHECK_FOR_UPDATES = Preference(booleanPreferencesKey("check_for_updates"), false)
    }
}

interface IPreferenceStore {
    suspend fun <T> putPreference(preference: Preference<T>, value: T)
    suspend fun <T> getPreference(preference: Preference<T>): T
}

class PreferenceStore @Inject constructor(
    private val context: Context
): IPreferenceStore {
    override suspend fun <T> putPreference(preference: Preference<T>, value: T) {
        context.dataStore.edit { prefs ->
            prefs[preference.key] = value
        }
    }

    override suspend fun <T> getPreference(preference: Preference<T>): T {
        val prefs = context.dataStore.data.first()
        return (prefs[preference.key] ?: preference.default)
    }
}
