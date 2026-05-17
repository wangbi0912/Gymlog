package com.gymlog.app.domain.usecase

import com.gymlog.app.data.remote.LLMApiClient
import com.gymlog.app.data.remote.ReviewPromptTemplate
import com.gymlog.app.domain.model.*
import com.gymlog.app.domain.repository.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.util.UUID
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewUseCase @Inject constructor(
    private val sessionRepo: SessionRepository,
    private val reviewRepo: ReviewRepository,
    private val userPrefsRepo: UserPreferencesRepository,
    private val bodyMeasurementRepo: BodyMeasurementRepository,
    private val llmClient: LLMApiClient,
    private val gson: Gson
) {
    suspend fun submitReview(sessionId: String): String {
        val session = sessionRepo.getById(sessionId) ?: throw IllegalStateException("Session not found")
        val config = userPrefsRepo.llmConfig.firstOrNull() ?: LLMConfig()
        val profile = userPrefsRepo.userProfile.firstOrNull() ?: UserProfile()

        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L
        val history7d = sessionRepo.getSessionSummaries(now - 7 * dayMs, now)
        val history30d = sessionRepo.getSessionSummaries(now - 30 * dayMs, now)
            .filter { it.bodyPart == session.bodyPart }
        val previousActions = reviewRepo.getUserActionsForSession(sessionId)

        val latestWeight = bodyMeasurementRepo.getLatest()
        val weightTrend = latestWeight?.let { "${it.weightKg}kg (${formatDate(it.date)})" }

        val prompt = ReviewPromptTemplate.build(
            currentSession = session,
            history7d = history7d,
            history30dSamePart = history30d,
            previousReviewActions = previousActions,
            userProfile = profile,
            bodyWeightTrend = weightTrend
        )

        val baseUrl = config.customBaseUrl ?: config.provider.baseUrl

        val reviewId = UUID.randomUUID().toString()
        val review = ReviewRequest(
            id = reviewId,
            sessionId = sessionId,
            status = ReviewStatus.QUEUED,
            provider = config.provider,
            modelName = config.modelName,
            requestPrompt = prompt
        )
        reviewRepo.save(review)
        return reviewId
    }

    suspend fun executeReview(reviewId: String) {
        val review = reviewRepo.getById(reviewId) ?: return
        val config = userPrefsRepo.llmConfig.firstOrNull() ?: return

        reviewRepo.update(review.copy(status = ReviewStatus.REVIEWING))

        val result = llmClient.sendReviewRequest(
            prompt = review.requestPrompt,
            apiKey = config.apiKey,
            provider = config.provider,
            model = config.modelName,
            customBaseUrl = config.customBaseUrl
        )

        result.fold(
            onSuccess = { response ->
                val parsed = parseReviewResponse(response)
                reviewRepo.update(review.copy(
                    status = ReviewStatus.COMPLETED,
                    rawResponse = response,
                    overallComment = parsed.first,
                    score = parsed.second,
                    tags = parsed.third,
                    completedAt = System.currentTimeMillis(),
                    items = parsed.fourth.mapIndexed { i, item ->
                        ReviewItem(
                            id = UUID.randomUUID().toString(),
                            reviewRequestId = reviewId,
                            category = item.first,
                            content = item.second,
                            sortOrder = i
                        )
                    }
                ))
            },
            onFailure = { error ->
                reviewRepo.update(review.copy(
                    status = ReviewStatus.FAILED,
                    errorMessage = error.message,
                    retryCount = review.retryCount + 1
                ))
            }
        )
    }

    private fun parseReviewResponse(response: String): Quadruple<String?, Int?, List<String>, List<Pair<ReviewCategory, String>>> {
        return try {
            val cleaned = response
                .replace("```json", "")
                .replace("```", "")
                .trim()
            val json = gson.fromJson(cleaned, JsonObject::class.java)
            val overall = json.get("overall")?.asString
            val score = json.get("score")?.asInt
            val tags = json.getAsJsonArray("tags")?.map { it.asString } ?: emptyList()

            val items = mutableListOf<Pair<ReviewCategory, String>>()
            json.getAsJsonArray("strengths")?.forEach { items.add(ReviewCategory.STRENGTH to it.asString) }
            json.getAsJsonArray("issues")?.forEach { items.add(ReviewCategory.ISSUE to it.asString) }
            json.getAsJsonArray("suggestions")?.forEach { items.add(ReviewCategory.SUGGESTION to it.asString) }

            Quadruple(overall, score, tags, items)
        } catch (e: Exception) {
            Quadruple(response.take(500), null, emptyList(), listOf(ReviewCategory.SUGGESTION to "无法解析审查结果，请查看原始回复"))
        }
    }

    private fun formatDate(epoch: Long): String {
        val sdf = java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(epoch))
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
