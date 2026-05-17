package com.gymlog.app.domain.repository

import com.gymlog.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userProfile: Flow<UserProfile>
    val llmConfig: Flow<LLMConfig>
    val reminderConfig: Flow<ReminderConfig>
    val isOnboardingComplete: Flow<Boolean>
    val restTimerSeconds: Flow<Int>
    val enableReviewScore: Flow<Boolean>

    suspend fun updateProfile(profile: UserProfile)
    suspend fun updateLlmConfig(config: LLMConfig)
    suspend fun updateReminderConfig(config: ReminderConfig)
    suspend fun setOnboardingComplete()
    suspend fun setRestTimerSeconds(seconds: Int)
    suspend fun setEnableReviewScore(enable: Boolean)
}
