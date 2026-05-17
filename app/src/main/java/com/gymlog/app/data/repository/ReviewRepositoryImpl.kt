package com.gymlog.app.data.repository

import com.gymlog.app.data.local.dao.ReviewDao
import com.gymlog.app.data.local.entity.ReviewItemEntity
import com.gymlog.app.data.local.entity.ReviewRequestEntity
import com.gymlog.app.domain.model.*
import com.gymlog.app.domain.repository.ReviewRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val dao: ReviewDao,
    private val gson: Gson
) : ReviewRepository {

    override fun getAll(): Flow<List<ReviewRequest>> = dao.getAll().map { list ->
        list.map { it.toDomain(dao) }
    }

    override suspend fun getBySessionId(sessionId: String): ReviewRequest? {
        return dao.getBySessionId(sessionId)?.toDomain(dao)
    }

    override suspend fun getById(id: String): ReviewRequest? {
        return dao.getById(id)?.toDomain(dao)
    }

    override fun getLatestBySessionId(sessionId: String): Flow<ReviewRequest?> =
        dao.getLatestBySessionId(sessionId).map { it?.toDomain(dao) }

    override suspend fun getPendingReviews(): List<ReviewRequest> {
        return dao.getPendingReviews().map { it.toDomain(dao) }
    }

    override suspend fun save(request: ReviewRequest) {
        val tagsJson = if (request.tags.isNotEmpty()) gson.toJson(request.tags) else null
        val id = if (request.id.isBlank()) UUID.randomUUID().toString() else request.id
        dao.insert(ReviewRequestEntity(
            id = id, sessionId = request.sessionId, status = request.status,
            provider = request.provider, modelName = request.modelName,
            requestPrompt = request.requestPrompt, rawResponse = request.rawResponse,
            overallComment = request.overallComment, score = request.score,
            tagsJson = tagsJson, createdAt = request.createdAt,
            completedAt = request.completedAt, errorMessage = request.errorMessage,
            retryCount = request.retryCount
        ))
        if (request.items.isNotEmpty()) {
            dao.insertItems(request.items.map { item ->
                ReviewItemEntity(
                    id = if (item.id.isBlank()) UUID.randomUUID().toString() else item.id,
                    reviewRequestId = id, category = item.category,
                    content = item.content, sortOrder = item.sortOrder,
                    userAction = item.userAction, userReply = item.userReply
                )
            })
        }
    }

    override suspend fun update(request: ReviewRequest) {
        val tagsJson = if (request.tags.isNotEmpty()) gson.toJson(request.tags) else null
        dao.update(ReviewRequestEntity(
            id = request.id, sessionId = request.sessionId, status = request.status,
            provider = request.provider, modelName = request.modelName,
            requestPrompt = request.requestPrompt, rawResponse = request.rawResponse,
            overallComment = request.overallComment, score = request.score,
            tagsJson = tagsJson, createdAt = request.createdAt,
            completedAt = request.completedAt, errorMessage = request.errorMessage,
            retryCount = request.retryCount
        ))
    }

    override suspend fun getUserActionsForSession(sessionId: String): List<Pair<String, String>> {
        return dao.getResolvedItemsForSession(sessionId).mapNotNull {
            if (it.userAction != null) Pair(it.userAction.name, it.content) else null
        }
    }
}

private suspend fun ReviewRequestEntity.toDomain(dao: ReviewDao): ReviewRequest {
    val items = dao.getItems(id).map { item ->
        ReviewItem(
            id = item.id, reviewRequestId = item.reviewRequestId,
            category = item.category, content = item.content,
            sortOrder = item.sortOrder, userAction = item.userAction,
            userReply = item.userReply
        )
    }
    val tags: List<String> = try {
        tagsJson?.let {
            Gson().fromJson(it, object : TypeToken<List<String>>() {}.type)
        } ?: emptyList()
    } catch (_: Exception) { emptyList() }

    return ReviewRequest(
        id = id, sessionId = sessionId, status = status, provider = provider,
        modelName = modelName, requestPrompt = requestPrompt, rawResponse = rawResponse,
        overallComment = overallComment, score = score, tags = tags,
        createdAt = createdAt, completedAt = completedAt,
        errorMessage = errorMessage, retryCount = retryCount, items = items
    )
}
