package com.gymlog.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gymlog.app.data.local.dao.*
import com.gymlog.app.data.local.entity.*

@Database(
    entities = [
        ExerciseEntity::class,
        TemplateEntity::class,
        TemplateExerciseEntity::class,
        SessionEntity::class,
        SessionExerciseEntity::class,
        SessionSetEntity::class,
        ReviewRequestEntity::class,
        ReviewItemEntity::class,
        BodyMeasurementEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class GymLogDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun sessionDao(): SessionDao
    abstract fun templateDao(): TemplateDao
    abstract fun reviewDao(): ReviewDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
}
