package com.gymlog.app.data.repository

import com.gymlog.app.data.local.dao.ExerciseDao
import com.gymlog.app.data.local.entity.ExerciseEntity
import com.gymlog.app.domain.model.BodyPart
import com.gymlog.app.domain.model.Exercise
import com.gymlog.app.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val dao: ExerciseDao,
    private val sessionDao: com.gymlog.app.data.local.dao.SessionDao
) : ExerciseRepository {

    override fun getAll(): Flow<List<Exercise>> = dao.getAll().map { it.map { e -> e.toDomain() } }

    override suspend fun getAllList(): List<Exercise> = dao.getAllList().map { it.toDomain() }

    override suspend fun getById(id: String): Exercise? = dao.getById(id)?.toDomain()

    override suspend fun getByIds(ids: List<String>): List<Exercise> = dao.getByIds(ids).map { it.toDomain() }

    override suspend fun search(query: String, limit: Int): List<Exercise> =
        dao.search(query, limit).map { it.toDomain() }

    override fun getByBodyPart(bodyPart: BodyPart): Flow<List<Exercise>> =
        dao.getByBodyPart(bodyPart).map { it.map { e -> e.toDomain() } }

    override suspend fun insert(exercise: Exercise) {
        dao.insert(exercise.toEntity())
    }

    override suspend fun setHidden(id: String, hidden: Boolean) {
        dao.setHidden(id, hidden)
    }

    override suspend fun saveCustom(exercise: Exercise) {
        dao.insert(exercise.copy(id = UUID.randomUUID().toString(), isBuiltIn = false).toEntity())
    }

    override suspend fun getLastExerciseData(exerciseId: String): Triple<Float?, Int?, Long?> {
        val data = sessionDao.getLastExerciseData(exerciseId)
        return Triple(data?.weightKg, data?.reps, data?.startTime)
    }
}

private fun ExerciseEntity.toDomain() = Exercise(
    id = id, name = name, bodyPart = bodyPart, equipment = equipment,
    category = category, defaultUnit = defaultUnit, isBuiltIn = isBuiltIn, isHidden = isHidden
)

private fun Exercise.toEntity() = ExerciseEntity(
    id = id, name = name, bodyPart = bodyPart, equipment = equipment,
    category = category, defaultUnit = defaultUnit, isBuiltIn = isBuiltIn, isHidden = isHidden
)
