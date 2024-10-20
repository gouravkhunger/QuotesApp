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

enum class Preference(val key: String, val default: Any) {
    CHECK_FOR_UPDATES("check_for_updates", false),
}

interface IPreferenceStore {
    suspend fun putBoolean(preference: Preference, value: Boolean)
    suspend fun getBoolean(preference: Preference): Boolean
}

class PreferenceStore @Inject constructor(
    private val context: Context
): IPreferenceStore {
    override suspend fun putBoolean(preference: Preference, value: Boolean) {
        val prefKey = booleanPreferencesKey(preference.key)

        context.dataStore.edit { prefs ->
            prefs[prefKey] = value
        }
    }

    override suspend fun getBoolean(preference: Preference): Boolean {
        val prefKey = booleanPreferencesKey(preference.key)
        val prefs = context.dataStore.data.first()

        return prefs[prefKey] ?: preference.default as Boolean
    }
}
