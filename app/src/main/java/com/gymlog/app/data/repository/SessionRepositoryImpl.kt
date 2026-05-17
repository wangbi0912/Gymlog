package com.gymlog.app.data.repository

import com.gymlog.app.data.local.dao.ExerciseDao
import com.gymlog.app.data.local.dao.SessionDao
import com.gymlog.app.data.local.entity.*
import com.gymlog.app.domain.model.*
import com.gymlog.app.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
    private val exerciseDao: ExerciseDao
) : SessionRepository {

    override fun getAllSessions(): Flow<List<TrainingSession>> =
        sessionDao.getAllSessions().map { list -> list.map { it.toDomain(emptyList()) } }

    override suspend fun getById(id: String): TrainingSession? {
        val session = sessionDao.getSessionById(id) ?: return null
        return session.toFullDomain(sessionDao, exerciseDao)
    }

    override suspend fun getInProgressSession(): TrainingSession? {
        val session = sessionDao.getInProgressSession() ?: return null
        return session.toFullDomain(sessionDao, exerciseDao)
    }

    override suspend fun getCompletedSessions(): List<TrainingSession> {
        return sessionDao.getCompletedSessions().map { it.toDomain(emptyList()) }
    }

    override suspend fun getSessionsInRange(from: Long, to: Long): List<TrainingSession> {
        return sessionDao.getSessionsInRange(from, to).map { it.toDomain(emptyList()) }
    }

    override suspend fun getSessionsInRangeAsc(from: Long, to: Long): List<TrainingSession> {
        return sessionDao.getSessionsInRangeAsc(from, to).map { it.toDomain(emptyList()) }
    }

    override suspend fun getRecentByBodyPart(bodyPart: BodyPart, limit: Int): List<TrainingSession> {
        return sessionDao.getRecentByBodyPart(bodyPart, limit).map { it.toFullDomain(sessionDao, exerciseDao) }
    }

    override suspend fun getSessionCount(from: Long, to: Long): Int {
        return sessionDao.getSessionCountInRange(from, to)
    }

    override suspend fun getTotalDuration(from: Long, to: Long): Long {
        return sessionDao.getTotalDurationInRange(from, to) ?: 0L
    }

    override suspend fun getSessionSummary(id: String): SessionSummary? {
        val s = sessionDao.getSessionById(id) ?: return null
        val exercises = sessionDao.getExercisesForSession(id)
        val mainEx = exercises.firstOrNull()
        val exEntity = mainEx?.let { exerciseDao.getById(it.exerciseId) }
        val sets = sessionDao.getSetsForExercise(mainEx?.id ?: return null)
        return SessionSummary(
            id = s.id, date = s.startTime, bodyPart = s.bodyPart,
            totalVolumeKg = exercises.sumOf { ex ->
                sessionDao.getSetsForExercise(ex.id)
                    .filter { it.weightKg != null && it.reps != null }
                    .sumOf { (it.weightKg!! * it.reps!!).toDouble() }
            }.toFloat(),
            workingSetCount = exercises.sumOf { ex ->
                sessionDao.getSetsForExercise(ex.id).count { it.setType == SetType.WORKING || it.setType == SetType.FAILURE }
            },
            overallRpe = s.overallRpe,
            durationMin = ((s.endTime ?: System.currentTimeMillis()) - s.startTime) / 60_000,
            mainExercise = exEntity?.name ?: "未知动作"
        )
    }

    override suspend fun getSessionSummaries(from: Long, to: Long): List<SessionSummary> {
        return sessionDao.getSessionsInRange(from, to).mapNotNull { s ->
            val exercises = sessionDao.getExercisesForSession(s.id)
            if (exercises.isEmpty()) return@mapNotNull null
            val mainEx = exerciseDao.getById(exercises.first().exerciseId)
            val allSets = sessionDao.getAllSetsForSession(s.id)
            SessionSummary(
                id = s.id, date = s.startTime, bodyPart = s.bodyPart,
                totalVolumeKg = allSets
                    .filter { it.weightKg != null && it.reps != null }
                    .sumOf { (it.weightKg!! * it.reps!!).toDouble() }.toFloat(),
                workingSetCount = allSets.count { it.setType == SetType.WORKING || it.setType == SetType.FAILURE },
                overallRpe = s.overallRpe,
                durationMin = ((s.endTime ?: System.currentTimeMillis()) - s.startTime) / 60_000,
                mainExercise = mainEx?.name ?: "未知动作"
            )
        }
    }

    override suspend fun save(session: TrainingSession): String {
        val sessionId = if (session.id.isBlank()) UUID.randomUUID().toString() else session.id
        sessionDao.insertSession(
            SessionEntity(
                id = sessionId, startTime = session.startTime, endTime = session.endTime,
                bodyPart = session.bodyPart, overallRpe = session.overallRpe, note = session.note,
                gymLocation = session.gymLocation, templateId = session.templateId, status = session.status
            )
        )
        session.exercises.forEach { ex ->
            val exId = if (ex.id.isBlank()) UUID.randomUUID().toString() else ex.id
            sessionDao.insertExercise(
                SessionExerciseEntity(
                    id = exId, sessionId = sessionId, exerciseId = ex.exerciseId,
                    sortOrder = ex.sortOrder, supersetGroupId = ex.supersetGroupId,
                    note = ex.note, overallRpe = ex.overallRpe
                )
            )
            ex.sets.forEach { set ->
                sessionDao.insertSet(
                    SessionSetEntity(
                        id = if (set.id.isBlank()) UUID.randomUUID().toString() else set.id,
                        sessionExerciseId = exId, setNumber = set.setNumber,
                        weightKg = set.weightKg, reps = set.reps,
                        durationSec = set.durationSec, distanceM = set.distanceM,
                        setType = set.setType, rpe = set.rpe, isCompleted = set.isCompleted
                    )
                )
            }
        }
        return sessionId
    }

    override suspend fun update(session: TrainingSession) {
        sessionDao.updateSession(
            SessionEntity(
                id = session.id, startTime = session.startTime, endTime = session.endTime,
                bodyPart = session.bodyPart, overallRpe = session.overallRpe, note = session.note,
                gymLocation = session.gymLocation, templateId = session.templateId, status = session.status
            )
        )
    }

    override suspend fun delete(id: String) {
        val session = sessionDao.getSessionById(id) ?: return
        sessionDao.deleteSession(session)
    }
}

private suspend fun SessionEntity.toFullDomain(sessionDao: SessionDao, exerciseDao: ExerciseDao): TrainingSession {
    val exercises = sessionDao.getExercisesForSession(id).map { ex ->
        val exEntity = exerciseDao.getById(ex.exerciseId)
        val sets = sessionDao.getSetsForExercise(ex.id).map { set ->
            SessionSet(
                id = set.id, sessionExerciseId = set.sessionExerciseId,
                setNumber = set.setNumber, weightKg = set.weightKg,
                reps = set.reps, durationSec = set.durationSec,
                distanceM = set.distanceM, setType = set.setType,
                rpe = set.rpe, isCompleted = set.isCompleted
            )
        }
        SessionExercise(
            id = ex.id, sessionId = ex.sessionId, exerciseId = ex.exerciseId,
            exerciseName = exEntity?.name ?: "未知动作", sortOrder = ex.sortOrder,
            supersetGroupId = ex.supersetGroupId, note = ex.note, overallRpe = ex.overallRpe,
            sets = sets
        )
    }
    return TrainingSession(
        id = id, startTime = startTime, endTime = endTime, bodyPart = bodyPart,
        overallRpe = overallRpe, note = note, gymLocation = gymLocation,
        templateId = templateId, status = status, exercises = exercises
    )
}

private fun SessionEntity.toDomain(exercises: List<SessionExercise>) = TrainingSession(
    id = id, startTime = startTime, endTime = endTime, bodyPart = bodyPart,
    overallRpe = overallRpe, note = note, gymLocation = gymLocation,
    templateId = templateId, status = status, exercises = exercises
)
