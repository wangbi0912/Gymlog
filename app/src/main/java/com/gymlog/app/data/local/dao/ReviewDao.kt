package com.gymlog.app.data.local.dao

import androidx.room.*
import com.gymlog.app.data.local.entity.ReviewItemEntity
import com.gymlog.app.data.local.entity.ReviewRequestEntity
import com.gymlog.app.domain.model.ReviewStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM review_requests WHERE sessionId = :sessionId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getBySessionId(sessionId: String): ReviewRequestEntity?

    @Query("SELECT * FROM review_requests ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ReviewRequestEntity>>

    @Query("SELECT * FROM review_requests WHERE id = :id")
    suspend fun getById(id: String): ReviewRequestEntity?

    @Query("SELECT * FROM review_requests WHERE status IN ('QUEUED', 'REVIEWING') ORDER BY createdAt ASC")
    suspend fun getPendingReviews(): List<ReviewRequestEntity>

    @Query("SELECT * FROM review_requests WHERE sessionId = :sessionId ORDER BY createdAt DESC LIMIT 1")
    fun getLatestBySessionId(sessionId: String): Flow<ReviewRequestEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: ReviewRequestEntity)

    @Update
    suspend fun update(request: ReviewRequestEntity)

    // Review items
    @Query("SELECT * FROM review_items WHERE reviewRequestId = :reviewId ORDER BY sortOrder")
    suspend fun getItems(reviewId: String): List<ReviewItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ReviewItemEntity>)

    @Update
    suspend fun updateItem(item: ReviewItemEntity)

    @Query("SELECT * FROM review_items WHERE reviewRequestId IN (SELECT id FROM review_requests WHERE sessionId = :sessionId) AND userAction IS NOT NULL")
    suspend fun getResolvedItemsForSession(sessionId: String): List<ReviewItemEntity>
}
