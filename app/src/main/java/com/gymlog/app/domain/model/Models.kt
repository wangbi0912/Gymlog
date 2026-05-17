package com.gymlog.app.domain.model

data class UserProfile(
    val gender: Gender = Gender.MALE,
    val birthYear: Int = 1995,
    val heightCm: Float = 175f,
    val trainingExperience: TrainingExperience = TrainingExperience.ONE_TO_2Y,
    val goal: TrainingGoal = TrainingGoal.HYPERTROPHY,
    val weeklyFrequency: Int = 4
)

data class Exercise(
    val id: String,
    val name: String,
    val bodyPart: BodyPart,
    val equipment: Equipment,
    val category: ExerciseCategory,
    val defaultUnit: ExerciseUnit,
    val isBuiltIn: Boolean = true,
    val isHidden: Boolean = false,
    val lastWeightKg: Float? = null,
    val lastReps: Int? = null,
    val lastDate: Long? = null
)

data class TrainingTemplate(
    val id: String,
    val name: String,
    val tag: String? = null,
    val estimatedDurationMin: Int? = null,
    val exercises: List<TemplateExercise> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class TemplateExercise(
    val id: String,
    val templateId: String,
    val exerciseId: String,
    val exerciseName: String = "",
    val sortOrder: Int,
    val targetSets: Int? = null,
    val targetReps: Int? = null,
    val targetWeightKg: Float? = null,
    val targetSetType: SetType? = null,
    val supersetGroupId: String? = null
)

data class TrainingSession(
    val id: String,
    val startTime: Long,
    val endTime: Long? = null,
    val bodyPart: BodyPart,
    val overallRpe: Int? = null,
    val note: String? = null,
    val gymLocation: String? = null,
    val templateId: String? = null,
    val status: SessionStatus = SessionStatus.IN_PROGRESS,
    val exercises: List<SessionExercise> = emptyList()
) {
    val totalVolumeKg: Float get() = exercises.sumOf { it.totalVolumeKg }.toFloat()
    val workingSetCount: Int get() = exercises.sumOf { ex ->
        ex.sets.count { it.setType == SetType.WORKING || it.setType == SetType.FAILURE }
    }
    val durationMin: Long get() {
        val end = endTime ?: System.currentTimeMillis()
        return (end - startTime) / 60_000
    }
}

data class SessionExercise(
    val id: String,
    val sessionId: String,
    val exerciseId: String,
    val exerciseName: String = "",
    val sortOrder: Int,
    val supersetGroupId: String? = null,
    val note: String? = null,
    val overallRpe: Int? = null,
    val sets: List<SessionSet> = emptyList()
) {
    val totalVolumeKg: Double get() = sets
        .filter { it.weightKg != null && it.reps != null }
        .sumOf { (it.weightKg!! * it.reps!!).toDouble() }
}

data class SessionSet(
    val id: String,
    val sessionExerciseId: String,
    val setNumber: Int,
    val weightKg: Float? = null,
    val reps: Int? = null,
    val durationSec: Int? = null,
    val distanceM: Float? = null,
    val setType: SetType = SetType.WORKING,
    val rpe: Int? = null,
    val isCompleted: Boolean = true
)

data class ReviewRequest(
    val id: String,
    val sessionId: String,
    val status: ReviewStatus = ReviewStatus.PENDING,
    val provider: LLMProvider = LLMProvider.OPENAI,
    val modelName: String = "gpt-4o-mini",
    val requestPrompt: String = "",
    val rawResponse: String? = null,
    val overallComment: String? = null,
    val score: Int? = null,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val errorMessage: String? = null,
    val retryCount: Int = 0,
    val items: List<ReviewItem> = emptyList()
)

data class ReviewItem(
    val id: String,
    val reviewRequestId: String,
    val category: ReviewCategory,
    val content: String,
    val sortOrder: Int = 0,
    val userAction: UserAction? = null,
    val userReply: String? = null
)

data class BodyMeasurement(
    val id: String,
    val date: Long,
    val weightKg: Float,
    val chestCm: Float? = null,
    val waistCm: Float? = null,
    val hipCm: Float? = null,
    val armCm: Float? = null,
    val thighCm: Float? = null
)

data class ReminderConfig(
    val enabled: Boolean = false,
    val daysOfWeek: Set<Int> = setOf(1, 3, 5),
    val timeHour: Int = 8,
    val timeMinute: Int = 0,
    val inactivityReminderDays: Int = 5
)

data class LLMConfig(
    val provider: LLMProvider = LLMProvider.OPENAI,
    val apiKey: String = "",
    val modelName: String = "gpt-4o-mini",
    val customBaseUrl: String? = null
)

data class SessionSummary(
    val id: String,
    val date: Long,
    val bodyPart: BodyPart,
    val totalVolumeKg: Float,
    val workingSetCount: Int,
    val overallRpe: Int?,
    val durationMin: Long,
    val mainExercise: String
)

data class UserActionSummary(
    val action: String,
    val content: String
)
