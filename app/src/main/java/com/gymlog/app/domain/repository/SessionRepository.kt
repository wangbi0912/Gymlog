package com.gymlog.app.domain.repository

import com.gymlog.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getAllSessions(): Flow<List<TrainingSession>>
    suspend fun getById(id: String): TrainingSession?
    suspend fun getInProgressSession(): TrainingSession?
    suspend fun getCompletedSessions(): List<TrainingSession>
    suspend fun getSessionsInRange(from: Long, to: Long): List<TrainingSession>
    suspend fun getSessionsInRangeAsc(from: Long, to: Long): List<TrainingSession>
    suspend fun getRecentByBodyPart(bodyPart: BodyPart, limit: Int): List<TrainingSession>
    suspend fun getSessionCount(from: Long, to: Long): Int
    suspend fun getTotalDuration(from: Long, to: Long): Long
    suspend fun getSessionSummary(id: String): SessionSummary?
    suspend fun getSessionSummaries(from: Long, to: Long): List<SessionSummary>
    suspend fun save(session: TrainingSession): String
    suspend fun update(session: TrainingSession)
    suspend fun delete(id: String)
}
