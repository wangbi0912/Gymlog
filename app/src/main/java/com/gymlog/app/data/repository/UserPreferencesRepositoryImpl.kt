package com.gymlog.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.gymlog.app.domain.model.*
import com.gymlog.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gymlog_prefs")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    private val ds = context.dataStore

    override val userProfile: Flow<UserProfile> = ds.data.map { prefs ->
        val genderStr = prefs[KEY_GENDER] ?: "MALE"
        UserProfile(
            gender = try { Gender.valueOf(genderStr) } catch (_: Exception) { Gender.MALE },
            birthYear = prefs[KEY_BIRTH_YEAR] ?: 1995,
            heightCm = prefs[KEY_HEIGHT] ?: 175f,
            trainingExperience = try {
                TrainingExperience.valueOf(prefs[KEY_EXPERIENCE] ?: "ONE_TO_2Y")
            } catch (_: Exception) { TrainingExperience.ONE_TO_2Y },
            goal = try {
                TrainingGoal.valueOf(prefs[KEY_GOAL] ?: "HYPERTROPHY")
            } catch (_: Exception) { TrainingGoal.HYPERTROPHY },
            weeklyFrequency = prefs[KEY_FREQUENCY] ?: 4
        )
    }

    override val llmConfig: Flow<LLMConfig> = ds.data.map { prefs ->
        val providerStr = prefs[KEY_PROVIDER] ?: "OPENAI"
        LLMConfig(
            provider = try { LLMProvider.valueOf(providerStr) } catch (_: Exception) { LLMProvider.OPENAI },
            apiKey = prefs[KEY_API_KEY] ?: "",
            modelName = prefs[KEY_MODEL] ?: "gpt-4o-mini",
            customBaseUrl = prefs[KEY_BASE_URL]
        )
    }

    override val reminderConfig: Flow<ReminderConfig> = ds.data.map { prefs ->
        val daysStr = prefs[KEY_REMINDER_DAYS] ?: "1,3,5"
        ReminderConfig(
            enabled = prefs[KEY_REMINDER_ENABLED] ?: false,
            daysOfWeek = daysStr.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet(),
            timeHour = prefs[KEY_REMINDER_HOUR] ?: 8,
            timeMinute = prefs[KEY_REMINDER_MINUTE] ?: 0,
            inactivityReminderDays = prefs[KEY_INACTIVITY_DAYS] ?: 5
        )
    }

    override val isOnboardingComplete: Flow<Boolean> = ds.data.map { prefs ->
        prefs[KEY_ONBOARDING] ?: false
    }

    override val restTimerSeconds: Flow<Int> = ds.data.map { prefs ->
        prefs[KEY_REST_SECONDS] ?: 90
    }

    override val enableReviewScore: Flow<Boolean> = ds.data.map { prefs ->
        prefs[KEY_ENABLE_SCORE] ?: true
    }

    override suspend fun updateProfile(profile: UserProfile) {
        ds.edit { prefs ->
            prefs[KEY_GENDER] = profile.gender.name
            prefs[KEY_BIRTH_YEAR] = profile.birthYear
            prefs[KEY_HEIGHT] = profile.heightCm
            prefs[KEY_EXPERIENCE] = profile.trainingExperience.name
            prefs[KEY_GOAL] = profile.goal.name
            prefs[KEY_FREQUENCY] = profile.weeklyFrequency
        }
    }

    override suspend fun updateLlmConfig(config: LLMConfig) {
        ds.edit { prefs ->
            prefs[KEY_PROVIDER] = config.provider.name
            prefs[KEY_API_KEY] = config.apiKey
            prefs[KEY_MODEL] = config.modelName
            if (config.customBaseUrl != null) prefs[KEY_BASE_URL] = config.customBaseUrl
            else prefs.remove(KEY_BASE_URL)
        }
    }

    override suspend fun updateReminderConfig(config: ReminderConfig) {
        ds.edit { prefs ->
            prefs[KEY_REMINDER_ENABLED] = config.enabled
            prefs[KEY_REMINDER_DAYS] = config.daysOfWeek.joinToString(",")
            prefs[KEY_REMINDER_HOUR] = config.timeHour
            prefs[KEY_REMINDER_MINUTE] = config.timeMinute
            prefs[KEY_INACTIVITY_DAYS] = config.inactivityReminderDays
        }
    }

    override suspend fun setOnboardingComplete() {
        ds.edit { it[KEY_ONBOARDING] = true }
    }

    override suspend fun setRestTimerSeconds(seconds: Int) {
        ds.edit { it[KEY_REST_SECONDS] = seconds }
    }

    override suspend fun setEnableReviewScore(enable: Boolean) {
        ds.edit { it[KEY_ENABLE_SCORE] = enable }
    }

    companion object {
        private val KEY_GENDER = stringPreferencesKey("gender")
        private val KEY_BIRTH_YEAR = intPreferencesKey("birth_year")
        private val KEY_HEIGHT = floatPreferencesKey("height")
        private val KEY_EXPERIENCE = stringPreferencesKey("experience")
        private val KEY_GOAL = stringPreferencesKey("goal")
        private val KEY_FREQUENCY = intPreferencesKey("frequency")
        private val KEY_PROVIDER = stringPreferencesKey("provider")
        private val KEY_API_KEY = stringPreferencesKey("api_key")
        private val KEY_MODEL = stringPreferencesKey("model")
        private val KEY_BASE_URL = stringPreferencesKey("base_url")
        private val KEY_REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        private val KEY_REMINDER_DAYS = stringPreferencesKey("reminder_days")
        private val KEY_REMINDER_HOUR = intPreferencesKey("reminder_hour")
        private val KEY_REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        private val KEY_INACTIVITY_DAYS = intPreferencesKey("inactivity_days")
        private val KEY_ONBOARDING = booleanPreferencesKey("onboarding_done")
        private val KEY_REST_SECONDS = intPreferencesKey("rest_seconds")
        private val KEY_ENABLE_SCORE = booleanPreferencesKey("enable_score")
    }
}
