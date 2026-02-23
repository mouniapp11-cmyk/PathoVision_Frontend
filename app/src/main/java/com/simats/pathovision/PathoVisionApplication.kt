package com.simats.pathovision

import android.app.Application
import com.simats.pathovision.network.TokenProvider
import com.simats.pathovision.utils.Constants
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import android.content.Context

// Single canonical DataStore instance for the entire app (also used by TokenManager via this same delegate)
val Context.appDataStore by preferencesDataStore(name = Constants.PREFS_NAME)

@HiltAndroidApp
class PathoVisionApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Wire the JWT token from DataStore into the OkHttp AuthInterceptor
        TokenProvider.getToken = {
            runBlocking {
                appDataStore.data
                    .map { prefs -> prefs[stringPreferencesKey(Constants.JWT_TOKEN_KEY)] }
                    .firstOrNull()
            }
        }
    }
}
