package com.gymlog.app.domain.repository

import com.gymlog.app.domain.model.BodyPart
import com.gymlog.app.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getAll(): Flow<List<Exercise>>
    suspend fun getAllList(): List<Exercise>
    suspend fun getById(id: String): Exercise?
    suspend fun getByIds(ids: List<String>): List<Exercise>
    suspend fun search(query: String, limit: Int = 20): List<Exercise>
    fun getByBodyPart(bodyPart: BodyPart): Flow<List<Exercise>>
    suspend fun insert(exercise: Exercise)
    suspend fun setHidden(id: String, hidden: Boolean)
    suspend fun saveCustom(exercise: Exercise)
    suspend fun getLastExerciseData(exerciseId: String): Triple<Float?, Int?, Long?>
}
