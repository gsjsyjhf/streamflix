package com.streamflix.app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "streamflix_settings")

enum class ThemeMode { LIGHT, DARK, SYSTEM }
enum class FontSizeScale(val scale: Float) { SMALL(0.88f), MEDIUM(1.0f), LARGE(1.18f) }
enum class AppLanguage(val code: String) { AR("ar"), EN("en") }
enum class PlaybackQuality(val label: String) { AUTO("تلقائي"), HD("720p"), FHD("1080p"), FOUR_K("4K") }

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val FONT = stringPreferencesKey("font_size")
        val LANG = stringPreferencesKey("language")
        val QUALITY = stringPreferencesKey("playback_quality")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map {
        runCatching { ThemeMode.valueOf(it[Keys.THEME] ?: ThemeMode.DARK.name) }.getOrDefault(ThemeMode.DARK)
    }
    val fontScale: Flow<FontSizeScale> = context.dataStore.data.map {
        runCatching { FontSizeScale.valueOf(it[Keys.FONT] ?: FontSizeScale.MEDIUM.name) }.getOrDefault(FontSizeScale.MEDIUM)
    }
    val language: Flow<AppLanguage> = context.dataStore.data.map {
        runCatching { AppLanguage.valueOf(it[Keys.LANG] ?: AppLanguage.AR.name) }.getOrDefault(AppLanguage.AR)
    }
    val playbackQuality: Flow<PlaybackQuality> = context.dataStore.data.map {
        runCatching { PlaybackQuality.valueOf(it[Keys.QUALITY] ?: PlaybackQuality.AUTO.name) }.getOrDefault(PlaybackQuality.AUTO)
    }

    suspend fun setTheme(mode: ThemeMode) = context.dataStore.edit { it[Keys.THEME] = mode.name }
    suspend fun setFontScale(scale: FontSizeScale) = context.dataStore.edit { it[Keys.FONT] = scale.name }
    suspend fun setLanguage(lang: AppLanguage) = context.dataStore.edit { it[Keys.LANG] = lang.name }
    suspend fun setQuality(q: PlaybackQuality) = context.dataStore.edit { it[Keys.QUALITY] = q.name }
}
