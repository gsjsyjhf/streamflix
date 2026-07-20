package com.streamflix.app.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamflix.app.util.AppLanguage
import com.streamflix.app.util.FontSizeScale
import com.streamflix.app.util.PlaybackQuality
import com.streamflix.app.util.SettingsDataStore
import com.streamflix.app.util.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val fontScale: FontSizeScale = FontSizeScale.MEDIUM,
    val language: AppLanguage = AppLanguage.AR,
    val playbackQuality: PlaybackQuality = PlaybackQuality.AUTO
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val store: SettingsDataStore
) : ViewModel() {

    val state: StateFlow<SettingsState> = combine(
        store.themeMode, store.fontScale, store.language, store.playbackQuality
    ) { theme, font, lang, quality ->
        SettingsState(theme, font, lang, quality)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { store.setTheme(mode) }
    fun setFontScale(scale: FontSizeScale) = viewModelScope.launch { store.setFontScale(scale) }
    fun setLanguage(lang: AppLanguage) = viewModelScope.launch { store.setLanguage(lang) }
    fun setQuality(q: PlaybackQuality) = viewModelScope.launch { store.setQuality(q) }
}
