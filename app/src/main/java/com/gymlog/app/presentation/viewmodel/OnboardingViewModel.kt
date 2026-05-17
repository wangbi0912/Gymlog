package com.gymlog.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymlog.app.domain.model.*
import com.gymlog.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPrefsRepo: UserPreferencesRepository
) : ViewModel() {

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep

    private val _gender = MutableStateFlow(Gender.MALE)
    val gender: StateFlow<Gender> = _gender

    private val _birthYear = MutableStateFlow("1995")
    val birthYear: StateFlow<String> = _birthYear

    private val _heightCm = MutableStateFlow("175")
    val heightCm: StateFlow<String> = _heightCm

    private val _weightKg = MutableStateFlow("75")
    val weightKg: StateFlow<String> = _weightKg

    private val _trainingExperience = MutableStateFlow(TrainingExperience.ONE_TO_2Y)
    val trainingExperience: StateFlow<TrainingExperience> = _trainingExperience

    private val _trainingGoal = MutableStateFlow(TrainingGoal.HYPERTROPHY)
    val trainingGoal: StateFlow<TrainingGoal> = _trainingGoal

    private val _weeklyFrequency = MutableStateFlow(4)
    val weeklyFrequency: StateFlow<Int> = _weeklyFrequency

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey

    private val _selectedProvider = MutableStateFlow(LLMProvider.OPENAI)
    val selectedProvider: StateFlow<LLMProvider> = _selectedProvider

    private val _selectedModel = MutableStateFlow("gpt-4o-mini")
    val selectedModel: StateFlow<String> = _selectedModel

    fun setStep(step: Int) { _currentStep.value = step }
    fun nextStep() { if (_currentStep.value < 2) _currentStep.value++ }
    fun prevStep() { if (_currentStep.value > 0) _currentStep.value-- }
    fun setGender(g: Gender) { _gender.value = g }
    fun setBirthYear(y: String) { _birthYear.value = y }
    fun setHeightCm(h: String) { _heightCm.value = h }
    fun setWeightKg(w: String) { _weightKg.value = w }
    fun setTrainingExperience(e: TrainingExperience) { _trainingExperience.value = e }
    fun setTrainingGoal(g: TrainingGoal) { _trainingGoal.value = g }
    fun setWeeklyFrequency(f: Int) { _weeklyFrequency.value = f }
    fun setApiKey(k: String) { _apiKey.value = k }
    fun setProvider(p: LLMProvider) { _selectedProvider.value = p }
    fun setModel(m: String) { _selectedModel.value = m }

    fun complete() {
        viewModelScope.launch {
            userPrefsRepo.updateProfile(UserProfile(
                gender = _gender.value,
                birthYear = _birthYear.value.toIntOrNull() ?: 1995,
                heightCm = _heightCm.value.toFloatOrNull() ?: 175f,
                trainingExperience = _trainingExperience.value,
                goal = _trainingGoal.value,
                weeklyFrequency = _weeklyFrequency.value
            ))
            userPrefsRepo.updateLlmConfig(LLMConfig(
                provider = _selectedProvider.value,
                apiKey = _apiKey.value,
                modelName = _selectedModel.value
            ))
            userPrefsRepo.setOnboardingComplete()
        }
    }

    fun skip() {
        viewModelScope.launch {
            userPrefsRepo.setOnboardingComplete()
        }
    }
}
