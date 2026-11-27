package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.clickwiseDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "clickwise_preferences"
)

data class UpdatePreferenceSnapshot(
    val lastCheckEpochMs: Long = 0L,
    val dismissedVersionCode: Int = 0,
    val lastKnownLatestVersionName: String? = null,
    val lastKnownLatestVersionCode: Int = 0
)

class PreferencesRepository(private val context: Context) : com.example.domain.update.UpdatePreferencesStore {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("app_language")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val LAST_UPDATE_CHECK_EPOCH_MS = longPreferencesKey("last_update_check_epoch_ms")
        val DISMISSED_UPDATE_VERSION_CODE = intPreferencesKey("dismissed_update_version_code")
        val LAST_KNOWN_LATEST_VERSION_NAME = stringPreferencesKey("last_known_latest_version_name")
        val LAST_KNOWN_LATEST_VERSION_CODE = intPreferencesKey("last_known_latest_version_code")
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

    override val updatePreferencesFlow: Flow<UpdatePreferenceSnapshot> = context.clickwiseDataStore.data.map { prefs ->
        UpdatePreferenceSnapshot(
            lastCheckEpochMs = prefs[Keys.LAST_UPDATE_CHECK_EPOCH_MS] ?: 0L,
            dismissedVersionCode = prefs[Keys.DISMISSED_UPDATE_VERSION_CODE] ?: 0,
            lastKnownLatestVersionName = prefs[Keys.LAST_KNOWN_LATEST_VERSION_NAME],
            lastKnownLatestVersionCode = prefs[Keys.LAST_KNOWN_LATEST_VERSION_CODE] ?: 0
        )
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

    override suspend fun recordSuccessfulUpdateCheck(
        checkedAtEpochMs: Long,
        latestVersionName: String,
        latestVersionCode: Int
    ) {
        context.clickwiseDataStore.edit { prefs ->
            prefs[Keys.LAST_UPDATE_CHECK_EPOCH_MS] = checkedAtEpochMs
            prefs[Keys.LAST_KNOWN_LATEST_VERSION_NAME] = latestVersionName
            prefs[Keys.LAST_KNOWN_LATEST_VERSION_CODE] = latestVersionCode
        }
    }

    override suspend fun setDismissedUpdateVersionCode(versionCode: Int) {
        context.clickwiseDataStore.edit { prefs ->
            prefs[Keys.DISMISSED_UPDATE_VERSION_CODE] = versionCode
        }
    }
}
