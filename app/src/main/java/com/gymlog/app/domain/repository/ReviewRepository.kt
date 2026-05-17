package com.gymlog.app.domain.repository

import com.gymlog.app.domain.model.ReviewRequest
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun getAll(): Flow<List<ReviewRequest>>
    suspend fun getBySessionId(sessionId: String): ReviewRequest?
    suspend fun getById(id: String): ReviewRequest?
    fun getLatestBySessionId(sessionId: String): Flow<ReviewRequest?>
    suspend fun getPendingReviews(): List<ReviewRequest>
    suspend fun save(request: ReviewRequest)
    suspend fun update(request: ReviewRequest)
    suspend fun getUserActionsForSession(sessionId: String): List<Pair<String, String>>
}
