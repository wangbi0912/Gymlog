package com.gymlog.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymlog.app.domain.model.*
import com.gymlog.app.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPrefsRepo: UserPreferencesRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    val llmConfig: StateFlow<LLMConfig> = userPrefsRepo.llmConfig
        .stateIn(viewModelScope, SharingStarted.Eagerly, LLMConfig())

    val reminderConfig: StateFlow<ReminderConfig> = userPrefsRepo.reminderConfig
        .stateIn(viewModelScope, SharingStarted.Eagerly, ReminderConfig())

    val restTimerSeconds: StateFlow<Int> = userPrefsRepo.restTimerSeconds
        .stateIn(viewModelScope, SharingStarted.Eagerly, 90)

    val enableReviewScore: StateFlow<Boolean> = userPrefsRepo.enableReviewScore
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _testResult = MutableStateFlow<String?>(null)
    val testResult: StateFlow<String?> = _testResult

    fun updateApiKey(key: String) {
        viewModelScope.launch {
            userPrefsRepo.updateLlmConfig(llmConfig.value.copy(apiKey = key))
        }
    }

    fun updateProvider(provider: LLMProvider) {
        viewModelScope.launch {
            userPrefsRepo.updateLlmConfig(llmConfig.value.copy(
                provider = provider,
                modelName = when (provider) {
                    LLMProvider.OPENAI -> "gpt-4o-mini"
                    LLMProvider.ANTHROPIC -> "claude-3-haiku-20240307"
                    LLMProvider.DEEPSEEK -> "deepseek-chat"
                    LLMProvider.GEMINI -> "gemini-1.5-flash"
                }
            ))
        }
    }

    fun updateModel(model: String) {
        viewModelScope.launch {
            userPrefsRepo.updateLlmConfig(llmConfig.value.copy(modelName = model))
        }
    }

    fun updateCustomBaseUrl(url: String) {
        viewModelScope.launch {
            userPrefsRepo.updateLlmConfig(llmConfig.value.copy(customBaseUrl = url.ifBlank { null }))
        }
    }

    fun updateRestTimer(seconds: Int) {
        viewModelScope.launch { userPrefsRepo.setRestTimerSeconds(seconds) }
    }

    fun updateEnableScore(enable: Boolean) {
        viewModelScope.launch { userPrefsRepo.setEnableReviewScore(enable) }
    }

    fun updateReminderConfig(config: ReminderConfig) {
        viewModelScope.launch { userPrefsRepo.updateReminderConfig(config) }
    }

    fun exportData(): String {
        // Placeholder - would generate JSON from all data
        return "{}"
    }

    fun clearTestResult() { _testResult.value = null }
}
