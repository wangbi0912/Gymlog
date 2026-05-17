package com.gymlog.app.data.local.entity

import androidx.room.TypeConverter
import com.gymlog.app.domain.model.*

class Converters {
    @TypeConverter
    fun fromBodyPart(value: BodyPart) = value.name

    @TypeConverter
    fun toBodyPart(value: String) = BodyPart.valueOf(value)

    @TypeConverter
    fun fromEquipment(value: Equipment) = value.name

    @TypeConverter
    fun toEquipment(value: String) = Equipment.valueOf(value)

    @TypeConverter
    fun fromExerciseCategory(value: ExerciseCategory) = value.name

    @TypeConverter
    fun toExerciseCategory(value: String) = ExerciseCategory.valueOf(value)

    @TypeConverter
    fun fromExerciseUnit(value: ExerciseUnit) = value.name

    @TypeConverter
    fun toExerciseUnit(value: String) = ExerciseUnit.valueOf(value)

    @TypeConverter
    fun fromSessionStatus(value: SessionStatus) = value.name

    @TypeConverter
    fun toSessionStatus(value: String) = SessionStatus.valueOf(value)

    @TypeConverter
    fun fromSetType(value: SetType) = value.name

    @TypeConverter
    fun toSetType(value: String) = SetType.valueOf(value)

    @TypeConverter
    fun fromReviewStatus(value: ReviewStatus) = value.name

    @TypeConverter
    fun toReviewStatus(value: String) = ReviewStatus.valueOf(value)

    @TypeConverter
    fun fromLLMProvider(value: LLMProvider) = value.name

    @TypeConverter
    fun toLLMProvider(value: String) = LLMProvider.valueOf(value)

    @TypeConverter
    fun fromReviewCategory(value: ReviewCategory) = value.name

    @TypeConverter
    fun toReviewCategory(value: String) = ReviewCategory.valueOf(value)

    @TypeConverter
    fun fromUserAction(value: UserAction?) = value?.name

    @TypeConverter
    fun toUserAction(value: String?) = value?.let { UserAction.valueOf(it) }
}
