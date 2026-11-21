package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.clickwiseDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "clickwise_preferences"
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("app_language")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
    }

    val languageFlow: Flow<AppLanguage> = context.clickwiseDataStore.data.map { prefs ->
        when (prefs[Keys.LANGUAGE]) {
            AppLanguage.UR.code -> AppLanguage.UR
            else -> AppLanguage.EN
        }
    }

    val reduceMotionFlow: Flow<Boolean> = context.clickwiseDataStore.data.map { prefs ->
        prefs[Keys.REDUCE_MOTION] ?: false
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.clickwiseDataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.code
        }
    }

    suspend fun setReduceMotion(reduceMotion: Boolean) {
        context.clickwiseDataStore.edit { prefs ->
            prefs[Keys.REDUCE_MOTION] = reduceMotion
        }
    }
}
