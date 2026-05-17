package com.gymlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gymlog.app.domain.model.*

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val bodyPart: BodyPart,
    val equipment: Equipment,
    val category: ExerciseCategory,
    val defaultUnit: ExerciseUnit,
    val isBuiltIn: Boolean = true,
    val isHidden: Boolean = false
)

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val tag: String? = null,
    val estimatedDurationMin: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "template_exercises")
data class TemplateExerciseEntity(
    @PrimaryKey val id: String,
    val templateId: String,
    val exerciseId: String,
    val sortOrder: Int,
    val targetSets: Int? = null,
    val targetReps: Int? = null,
    val targetWeightKg: Float? = null,
    val targetSetType: SetType? = null,
    val supersetGroupId: String? = null
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val startTime: Long,
    val endTime: Long? = null,
    val bodyPart: BodyPart,
    val overallRpe: Int? = null,
    val note: String? = null,
    val gymLocation: String? = null,
    val templateId: String? = null,
    val status: SessionStatus = SessionStatus.IN_PROGRESS
)

@Entity(tableName = "session_exercises")
data class SessionExerciseEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val exerciseId: String,
    val sortOrder: Int,
    val supersetGroupId: String? = null,
    val note: String? = null,
    val overallRpe: Int? = null
)

@Entity(tableName = "session_sets")
data class SessionSetEntity(
    @PrimaryKey val id: String,
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

@Entity(tableName = "review_requests")
data class ReviewRequestEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val status: ReviewStatus = ReviewStatus.PENDING,
    val provider: LLMProvider = LLMProvider.OPENAI,
    val modelName: String = "gpt-4o-mini",
    val requestPrompt: String = "",
    val rawResponse: String? = null,
    val overallComment: String? = null,
    val score: Int? = null,
    val tagsJson: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val errorMessage: String? = null,
    val retryCount: Int = 0
)

@Entity(tableName = "review_items")
data class ReviewItemEntity(
    @PrimaryKey val id: String,
    val reviewRequestId: String,
    val category: ReviewCategory,
    val content: String,
    val sortOrder: Int = 0,
    val userAction: UserAction? = null,
    val userReply: String? = null
)

@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey val id: String,
    val date: Long,
    val weightKg: Float,
    val chestCm: Float? = null,
    val waistCm: Float? = null,
    val hipCm: Float? = null,
    val armCm: Float? = null,
    val thighCm: Float? = null
)
