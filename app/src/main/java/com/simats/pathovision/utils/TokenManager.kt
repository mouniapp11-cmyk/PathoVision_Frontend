package com.simats.pathovision.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.simats.pathovision.appDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Uses the single canonical DataStore instance from PathoVisionApplication
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TOKEN_KEY = stringPreferencesKey(Constants.JWT_TOKEN_KEY)
    private val ROLE_KEY = stringPreferencesKey(Constants.ROLE_KEY)

    val token: Flow<String?> = context.appDataStore.data.map { prefs -> prefs[TOKEN_KEY] }
    val role: Flow<String?> = context.appDataStore.data.map { prefs -> prefs[ROLE_KEY] }

    suspend fun saveToken(token: String) {
        context.appDataStore.edit { prefs -> prefs[TOKEN_KEY] = token }
    }

    suspend fun saveRole(role: String) {
        context.appDataStore.edit { prefs -> prefs[ROLE_KEY] = role }
    }

    suspend fun clearAll() {
        context.appDataStore.edit { prefs -> prefs.clear() }
    }
}
