package com.gymlog.app.data.local.dao

import androidx.room.*
import com.gymlog.app.data.local.entity.*
import com.gymlog.app.domain.model.BodyPart
import com.gymlog.app.domain.model.SessionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: String): SessionEntity?

    @Query("SELECT * FROM sessions WHERE status = 'IN_PROGRESS' LIMIT 1")
    suspend fun getInProgressSession(): SessionEntity?

    @Query("SELECT * FROM sessions WHERE status = 'COMPLETED' ORDER BY startTime DESC")
    suspend fun getCompletedSessions(): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE status = 'COMPLETED' AND startTime BETWEEN :from AND :to ORDER BY startTime DESC")
    suspend fun getSessionsInRange(from: Long, to: Long): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE status = 'COMPLETED' AND bodyPart = :bodyPart ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentByBodyPart(bodyPart: BodyPart, limit: Int = 5): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE status = 'COMPLETED' AND startTime BETWEEN :from AND :to ORDER BY startTime ASC")
    suspend fun getSessionsInRangeAsc(from: Long, to: Long): List<SessionEntity>

    @Query("SELECT COUNT(*) FROM sessions WHERE status = 'COMPLETED' AND startTime BETWEEN :from AND :to")
    suspend fun getSessionCountInRange(from: Long, to: Long): Int

    @Query("SELECT SUM(endTime - startTime) FROM sessions WHERE status = 'COMPLETED' AND startTime BETWEEN :from AND :to")
    suspend fun getTotalDurationInRange(from: Long, to: Long): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    // Session exercises
    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId ORDER BY sortOrder")
    suspend fun getExercisesForSession(sessionId: String): List<SessionExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: SessionExerciseEntity)

    @Update
    suspend fun updateExercise(exercise: SessionExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: SessionExerciseEntity)

    @Query("DELETE FROM session_exercises WHERE id = :id")
    suspend fun deleteExerciseById(id: String)

    // Sets
    @Query("SELECT * FROM session_sets WHERE sessionExerciseId = :exerciseId ORDER BY setNumber")
    suspend fun getSetsForExercise(exerciseId: String): List<SessionSetEntity>

    @Query("SELECT * FROM session_sets WHERE sessionExerciseId IN (SELECT id FROM session_exercises WHERE sessionId = :sessionId)")
    suspend fun getAllSetsForSession(sessionId: String): List<SessionSetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: SessionSetEntity)

    @Update
    suspend fun updateSet(set: SessionSetEntity)

    @Delete
    suspend fun deleteSet(set: SessionSetEntity)

    @Query("DELETE FROM session_sets WHERE sessionExerciseId = :exerciseId")
    suspend fun deleteSetsForExercise(exerciseId: String)

    // Get last exercise data for reference
    @Query("""
        SELECT ss.weightKg, ss.reps, s.startTime
        FROM session_sets ss
        JOIN session_exercises se ON ss.sessionExerciseId = se.id
        JOIN sessions s ON se.sessionId = s.id
        WHERE se.exerciseId = :exerciseId AND s.status = 'COMPLETED' AND ss.setType = 'WORKING'
        ORDER BY s.startTime DESC LIMIT 1
    """)
    suspend fun getLastExerciseData(exerciseId: String): LastExerciseData?

    data class LastExerciseData(
        val weightKg: Float?,
        val reps: Int?,
        val startTime: Long?
    )
}
