package com.gymlog.app.data.repository

import com.gymlog.app.data.local.dao.ExerciseDao
import com.gymlog.app.data.local.dao.TemplateDao
import com.gymlog.app.data.local.entity.TemplateEntity
import com.gymlog.app.data.local.entity.TemplateExerciseEntity
import com.gymlog.app.domain.model.*
import com.gymlog.app.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepositoryImpl @Inject constructor(
    private val dao: TemplateDao,
    private val exerciseDao: ExerciseDao
) : TemplateRepository {

    override fun getAll(): Flow<List<TrainingTemplate>> = dao.getAll().map { list ->
        list.map { tpl ->
            val exs = dao.getExercises(tpl.id).map { te ->
                val ex = exerciseDao.getById(te.exerciseId)
                TemplateExercise(
                    id = te.id, templateId = te.templateId, exerciseId = te.exerciseId,
                    exerciseName = ex?.name ?: "", sortOrder = te.sortOrder,
                    targetSets = te.targetSets, targetReps = te.targetReps,
                    targetWeightKg = te.targetWeightKg, targetSetType = te.targetSetType,
                    supersetGroupId = te.supersetGroupId
                )
            }
            TrainingTemplate(
                id = tpl.id, name = tpl.name, tag = tpl.tag,
                estimatedDurationMin = tpl.estimatedDurationMin,
                exercises = exs, createdAt = tpl.createdAt
            )
        }
    }

    override suspend fun getById(id: String): TrainingTemplate? {
        val tpl = dao.getById(id) ?: return null
        val exs = dao.getExercises(tpl.id).map { te ->
            val ex = exerciseDao.getById(te.exerciseId)
            TemplateExercise(
                id = te.id, templateId = te.templateId, exerciseId = te.exerciseId,
                exerciseName = ex?.name ?: "", sortOrder = te.sortOrder,
                targetSets = te.targetSets, targetReps = te.targetReps,
                targetWeightKg = te.targetWeightKg, targetSetType = te.targetSetType,
                supersetGroupId = te.supersetGroupId
            )
        }
        return TrainingTemplate(
            id = tpl.id, name = tpl.name, tag = tpl.tag,
            estimatedDurationMin = tpl.estimatedDurationMin,
            exercises = exs, createdAt = tpl.createdAt
        )
    }

    override suspend fun save(template: TrainingTemplate) {
        val tplId = if (template.id.isBlank()) UUID.randomUUID().toString() else template.id
        dao.insert(TemplateEntity(
            id = tplId, name = template.name, tag = template.tag,
            estimatedDurationMin = template.estimatedDurationMin, createdAt = template.createdAt
        ))
        dao.deleteExercises(tplId)
        dao.insertExercises(template.exercises.map { te ->
            TemplateExerciseEntity(
                id = if (te.id.isBlank()) UUID.randomUUID().toString() else te.id,
                templateId = tplId, exerciseId = te.exerciseId, sortOrder = te.sortOrder,
                targetSets = te.targetSets, targetReps = te.targetReps,
                targetWeightKg = te.targetWeightKg, targetSetType = te.targetSetType,
                supersetGroupId = te.supersetGroupId
            )
        })
    }

    override suspend fun delete(id: String) {
        dao.deleteExercises(id)
        dao.deleteById(id)
    }
}
