package com.example.saka.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {
    companion object {
        val SELECTED_DISTRIBUTOR_KEY = stringPreferencesKey("selected_distributor")
    }

    suspend fun saveSelectedDistributor(distributor: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_DISTRIBUTOR_KEY] = distributor
        }
    }

    suspend fun getSelectedDistributor(): String {
        return context.dataStore.data
            .map { it[SELECTED_DISTRIBUTOR_KEY] ?: "" }
            .first()
    }
}
